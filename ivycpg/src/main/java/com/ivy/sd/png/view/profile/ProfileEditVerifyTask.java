package com.ivy.sd.png.view.profile;

import com.ivy.lib.Utils;
import com.ivy.lib.rest.JSONFormatter;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.sd.png.util.Commons;

import org.jetbrains.annotations.NonNls;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Vector;

import io.fabric.sdk.android.services.concurrency.AsyncTask;

public class ProfileEditVerifyTask extends AsyncTask<Integer,Integer,Integer> {

    private IProfileEditCallback otpVerifyCompletedListener;
    private BusinessModel bmodel=null;
    private String value;
    private String type;
    private int downloadStatus = 0;

    public ProfileEditVerifyTask(BusinessModel bmodel, String value, String type,IProfileEditCallback taskCompletedListener) {
        this.bmodel = bmodel;
        this.value = value;
        this.type = type;
        this.otpVerifyCompletedListener =taskCompletedListener;
    }

    @Override
    protected Integer doInBackground(Integer... integers) {
        try {

            int listid = bmodel.configurationMasterHelper.getActivtyType("RE");

            JSONObject jsonData = new JSONObject();
            jsonData.put("UserId", bmodel.userMasterHelper.getUserMasterBO()
                    .getUserid());
            jsonData.put("RetailerId", bmodel.getRetailerMasterBO().getRetailerID());
            jsonData.put("ActivityType", listid);
            JSONObject notObj = new JSONObject();
            notObj.put("Type", type);
            notObj.put("Receiver", value);
            JSONArray notificationArray = new JSONArray();
            notificationArray.put(notObj);
            jsonData.put("Notification", notificationArray);


            @NonNls JSONFormatter jsonFormatter = new JSONFormatter("HeaderInformation");

            jsonFormatter.addParameter("UserId", bmodel.userMasterHelper
                    .getUserMasterBO().getUserid());
            jsonFormatter.addParameter("VersionCode",
                    bmodel.getApplicationVersionNumber());
            jsonFormatter.addParameter("LoginId", bmodel.userNameTemp.trim());
            jsonFormatter.addParameter("MobileDateTime",
                    Utils.getDate("yyyy/MM/dd HH:mm:ss"));
            jsonFormatter.addParameter("MobileUTCDateTime",
                    Utils.getGMTDateTime("yyyy/MM/dd HH:mm:ss"));
            jsonFormatter.addParameter("DeviceId",
                    bmodel.activationHelper.getIMEINumber());
            jsonFormatter.addParameter("VersionCode",
                    bmodel.getApplicationVersionNumber());
            jsonFormatter.addParameter(SynchronizationHelper.VERSION_NAME, bmodel.getApplicationVersionName());
            jsonFormatter.addParameter("OrganisationId", bmodel.userMasterHelper
                    .getUserMasterBO().getOrganizationId());

            String appendUrl = bmodel.synchronizationHelper.generateOtpUrl();

            Vector<String> responseVector = bmodel.synchronizationHelper.getOtpGenerateResponse(jsonFormatter.getDataInJson(),
                    jsonData.toString(), appendUrl);

            if (responseVector.size() > 0) {
                for (String s : responseVector) {
                    @NonNls JSONObject jsonObjectResponse = new JSONObject(s);

                    Iterator itr = jsonObjectResponse.keys();
                    while (itr.hasNext()) {
                        String key = (String) itr.next();
                        if (key.equals("Response")) {
                            downloadStatus = jsonObjectResponse.getInt("Response");
                        } else if (key.equals("ErrorCode")) {
                            String tokenResponse = jsonObjectResponse.getString("ErrorCode");
                            if (tokenResponse.equals(SynchronizationHelper.INVALID_TOKEN)
                                    || tokenResponse.equals(SynchronizationHelper.TOKEN_MISSINIG)
                                    || tokenResponse.equals(SynchronizationHelper.EXPIRY_TOKEN_CODE)) {

                                return -4;

                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            Commons.printException(e);
            return downloadStatus;
        }
        return downloadStatus;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        otpVerifyCompletedListener.onVerifyOTPCompleted(integer,type);
    }
}
