package com.ivy.sd.png.provider;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.ivy.lib.rest.MyKsoapConnection;
import com.ivy.lib.rest.MyKsoapConnection.ResponseListener;
import com.ivy.sd.png.bo.ActivationBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.ApplicationConfigs;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ActivationHelper {

    public final static int NOTIFY_ALREADY_NOT_ACTIVATED = 0;
    public final static int NOTIFY_ALREADY_ACTIVATED = 1;
    public final static int NOTIFY_CONNECTIVITY_ERROR = -1;
    public final static int NOTIFY_SERVER_ERROR = 2;
    public final static int NOTIFY_CONNECTION_PROBLEM = 3;
    public final static int NOTIFY_SUCESSFULLY_ACTIVATED = 4;
    public final static int NOTIFY_INVALID_KEY = 5;
    public final static int NOTIFY_ACTIVATION_FAILED = 6;
    public final static int NOTIFY_ACTIVATION_LIST = 7;
    public final static int NOTIFY_ACTIVATION_LIST_SINGLE = 8;
    public final static int NOTIFY_ACTIVATION_LIST_NULL = 9;
    public final static int NOTIFY_URL_EMPTY = 10;
    public final static int NOTIFY_NOT_VALID_URL = 11;
    public final static int NOTIFY_VALID_URL = 12;
    public final static int NOTIFY_SUCESSFULLY_ACTIVATED_EXTENDED = 13;
    public final static int NOTIFY_ACTIVATION_LIST_SINGLE_EXTEND = 14;
    public final static int NOTIFY_URL_NOT_MAPPED_ERROR = 15;
    public final static int NOTIFY_JSON_EXCEPTION = 16;
    public static final String SOAP_ACTION_SECURITYPOLICY1 = "http://tempuri.org/GetKeySyncURL";
    public static final String SOAP_ACTION_SECURITYPOLICY2 = "http://tempuri.org/GetIMEISyncURL";
    public static final String METHOD_NAME_SECURITYPOLICY1 = "GetKeySyncURL";
    public static final String METHOD_NAME_SECURITYPOLICY2 = "GetIMEISyncURL";

    public static final String NAMESPACE = "http://tempuri.org/";

    public String SERVER_URL;

    private Context context;
    private BusinessModel bmodel;
    private static ActivationHelper instance = null;
    public String activationKey, brokenKeyTemp;
    private List<ActivationBO> appUrls;

    protected ActivationHelper(Context context) {
        this.context = context;
        this.bmodel = (BusinessModel) context;
    }

    public static ActivationHelper getInstance(Context context) {
        if (instance == null) {
            instance = new ActivationHelper(context);
        }
        return instance;
    }

    public String getIMEINumber() {
        String deviceId = "";
        try {
            TelephonyManager telephonyManager = (TelephonyManager) bmodel
                    .getSystemService(Context.TELEPHONY_SERVICE);
            deviceId = telephonyManager.getDeviceId();
        } catch (Exception e) {
            return "0";
        }
        if (deviceId == null)
            return "0";
        else
            return deviceId;
    }
    public String getDeviceId(){
        String android_id = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        return android_id;
    }
    int downloadReponse = 2;

    /**
     * This method takes parameter LicenseKey, DeviceIMEI, VersionCode on
     * activate button click Url and Organization is stored in SharedPreference
     * appUrl, organization
     *
     * @return success failure response
     */
    public int doActivationAtHttp() {
        downloadReponse = 2;
        try {

            String imeiNumber = getIMEINumber();

            MyKsoapConnection ksp = new MyKsoapConnection();
            ksp.create(METHOD_NAME_SECURITYPOLICY1,
                    ApplicationConfigs.LICENSE_SOAP_URL,
                    SOAP_ACTION_SECURITYPOLICY1, NAMESPACE);
            Commons.printInformation(">>>>>>>>>>>Activation Start<<<<<<<<<<<");
            Commons.printInformation("URL "
                    + ApplicationConfigs.LICENSE_SOAP_URL);
            Commons.printInformation("LicenseKey " + activationKey);
            Commons.printInformation("DeviceIMEI " + imeiNumber);
            Commons.printInformation("VersionCode "
                    + bmodel.getApplicationVersionNumber());
            Commons.printInformation(SynchronizationHelper.VERSION_NAME + bmodel.getApplicationVersionName());
            ksp.addParam("LicenseKey", activationKey);
            ksp.addParam("VersionCode", bmodel.getApplicationVersionNumber());
            ksp.addParam("DeviceIMEI", imeiNumber);
            ksp.addParam(SynchronizationHelper.VERSION_NAME, bmodel.getApplicationVersionName());
            ksp.connectServer(new ResponseListener() {
                @Override
                public void onSucess(JSONObject jsonObj) {
                    Commons.printInformation("Activation onSucess Response"
                            + jsonObj.toString());
                    JSONArray jsonArray;
                    JSONObject jsonObject;
                    try {
                        jsonArray = (JSONArray) jsonObj.get("Table");
                        jsonObject = (JSONObject) jsonArray.get(0);

                        if (jsonObject.getString("SyncServiceURL").equals(""))
                            downloadReponse = 10;
                        else {
                            SharedPreferences.Editor editor = PreferenceManager
                                    .getDefaultSharedPreferences(context)
                                    .edit();
                            editor.putString("appUrlNew",
                                    jsonObject.getString("SyncServiceURL").replace(" ", ""));
                            editor.putString("application",
                                    jsonObject.getString("ApplicationName"));
                            editor.putString("activationKey",
                                    activationKey);
                            editor.commit();
                            Commons.printInformation("Sync Url>>>>>>>>>>"
                                    + jsonObject.getString("SyncServiceURL"));
                            downloadReponse = 4;
                        }
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        Commons.printException(e);
                        downloadReponse = 16;
                    } catch (Exception e) {
                        Commons.printException(e);
                        downloadReponse = 2;
                    }
                }

                @Override
                public void onFailure(int status, String message) {
                    Commons.printInformation("Activation"
                            + "onFailure Response->" + status + " Content : "
                            + message);
                    if (status == DataMembers.IVY_SERVER_ERROR) {
                        downloadReponse = SDUtil.convertToInt(message);
                    } else if (status == DataMembers.IVY_APP_INTERNAL_EXCEPTION) {
                        downloadReponse = SDUtil.convertToInt(message);
                    } else
                        downloadReponse = 2;
                }
            });
        } catch (Exception e) {
            Commons.printException("Exception", e);
            return 2;
        }

        return downloadReponse;
    }

    /**
     * This method takes parameter DeviceIMEI, VersionCode on activate refresh
     * click Url and Organization is stored in SharedPreference appUrl,
     * organization
     *
     * @return success failure response
     */
    public int doIMEIActivationAtHttp() {
        downloadReponse = 2;
        try {

            String imeiNumber = getIMEINumber();

            MyKsoapConnection ksp = new MyKsoapConnection();
            ksp.create(METHOD_NAME_SECURITYPOLICY2,
                    ApplicationConfigs.LICENSE_SOAP_URL,
                    SOAP_ACTION_SECURITYPOLICY2, NAMESPACE);
            ksp.addParam("DeviceIMEI", imeiNumber);
            ksp.addParam("VersionCode", bmodel.getApplicationVersionNumber());
            ksp.addParam(SynchronizationHelper.VERSION_NAME, bmodel.getApplicationVersionName());
            ksp.connectServer(new ResponseListener() {
                @Override
                public void onSucess(JSONObject jsonObj) {
                    Commons.printInformation("Activation" + "onSucess Response"
                            + jsonObj.toString());
                    JSONArray jsonArray;
                    JSONObject jsonObject;
                    try {
                        jsonArray = (JSONArray) jsonObj.get("Table");
                        if (jsonArray == null || jsonArray.length() == 0) {
                            downloadReponse = 9;
                        } else {
                            if (jsonArray.length() == 1) {
                                jsonObject = (JSONObject) jsonArray.get(0);
                                SharedPreferences.Editor editor = PreferenceManager
                                        .getDefaultSharedPreferences(context)
                                        .edit();

                                if (jsonObject.getString("SyncServiceURL")
                                        .equals(""))
                                    downloadReponse = 10;
                                else {
                                    editor.putString("appUrlNew", jsonObject
                                            .getString("SyncServiceURL").replace(" ", ""));
                                    editor.putString("application", jsonObject
                                            .getString("ApplicationName"));
                                    /*commented to use after deployment*/
                                   /* editor.putString("activationKey",
                                            jsonObject.getString("ActivationKey"));*/
                                    editor.commit();
                                    Commons.printInformation("Activation GetIMEISyncURL Sync Url>>>>>>>>>>"
                                            + jsonObject
                                            .getString("SyncServiceURL"));
                                    downloadReponse = 8;

                                }
                            } else {
                                if (getAppUrls() == null)
                                    setAppUrls(new ArrayList<ActivationBO>());
                                else
                                    getAppUrls().clear();
                                int size = jsonArray.length();
                                for (int i = 0; i < size; i++) {
                                    jsonObject = (JSONObject) jsonArray.get(i);
                                    ActivationBO bo = new ActivationBO();
                                    bo.setUrl(jsonObject
                                            .getString("SyncServiceURL").replace(" ", ""));
                                    bo.setEnviroinment(jsonObject
                                            .getString("ApplicationName"));
                                    getAppUrls().add(bo);
                                }
                                downloadReponse = 7;
                            }
                        }
                    } catch (Exception e) {
                        Commons.printException(e);
                        downloadReponse = 2;
                    }
                }

                @Override
                public void onFailure(int status, String message) {
                    Commons.printInformation("Activation GetIMEISyncURL"
                            + "onFailure Response->" + status + " Content : "
                            + message);

                    if (status == DataMembers.IVY_SERVER_ERROR) {
                        downloadReponse = SDUtil.convertToInt(message);
                    } else if (status == DataMembers.IVY_APP_INTERNAL_EXCEPTION) {
                        downloadReponse = SDUtil.convertToInt(message);
                    } else
                        downloadReponse = 2;
                }
            });
        } catch (Exception e) {
            Commons.printException("Exception", e);
            return 2;
        }

        return downloadReponse;
    }

    public boolean check200Status(String myUri) {
        try {
            URL urlobj = new URL(myUri);
            HttpURLConnection urlConnection = (HttpURLConnection) urlobj.openConnection();
            int responseCode = urlConnection.getResponseCode();
            Commons.print("Sync Url Success response code>>>>>>>>>>"
                    + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK)
                return true;
            else
                return false;
        } catch (Exception e) {
            Commons.printException(e);
            return false;
        }
    }

    public List<ActivationBO> getAppUrls() {
        return appUrls;
    }

    public void setAppUrls(List<ActivationBO> appUrls) {
        this.appUrls = appUrls;
    }



    public String getSERVER_URL() {
        return SERVER_URL;
    }



    public void setSERVER_URL(String sERVER_URL) {
        SERVER_URL = sERVER_URL.trim();
    }
    public void clearAppUrl() {
        SharedPreferences.Editor editor = PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit();
        editor.putString("appUrlNew", "");
        editor.putString("application", "");
        editor.putString("activationKey", "");
        editor.commit();
    }
}
