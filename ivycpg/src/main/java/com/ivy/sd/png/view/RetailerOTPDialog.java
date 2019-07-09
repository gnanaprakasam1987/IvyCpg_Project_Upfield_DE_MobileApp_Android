package com.ivy.sd.png.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.core.IvyConstants;
import com.ivy.lib.Utils;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import org.json.JSONObject;

import java.util.Iterator;
import java.util.Vector;



@SuppressLint("ValidFragment")
public class RetailerOTPDialog extends DialogFragment implements View.OnClickListener {

    private BusinessModel bmodel;
    private EditText et_otp;
    private OTPListener otpListener;
    private String type;

    public interface OTPListener {
        void generateOTP();

        void dismissListener(String type, boolean isVerfied);
    }

    public RetailerOTPDialog(OTPListener callBack, String type) {
        this.otpListener = callBack;
        this.type = type;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        View view = inflater.inflate(R.layout.otp_mobile_email, container, false);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        DisplayMetrics outMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay()
                .getMetrics(outMetrics);

        Button btn_cancel = view.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(this);
        Button btn_ok = view.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(this);
        TextView tv_resend = view.findViewById(R.id.tv_resend);
        tv_resend.setOnClickListener(this);
        et_otp = view.findViewById(R.id.et_otp);

        return view;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();

        switch (i) {
            case R.id.btn_cancel:
                dismiss();
                otpListener.dismissListener(type, false);
                break;
            case R.id.btn_ok:
                if (!et_otp.getText().toString().isEmpty())
                    new UploadOTP().execute();
                else
                    Toast.makeText(getActivity(), getActivity().
                            getResources().getString(R.string.enter_otp), Toast.LENGTH_LONG).show();
                break;
            case R.id.tv_resend:
                if (otpListener != null) {
                    dismiss();
                    otpListener.generateOTP();
                }
                break;
            default:
                break;
        }
    }

    class UploadOTP extends AsyncTask<Integer, Integer, Integer> {

        private ProgressDialog progressDialogue;
        private int downloadStatus = 0;
        private String OTP = "";

        protected void onPreExecute() {
            progressDialogue = ProgressDialog.show(getActivity(),
                    DataMembers.SD, getActivity().getResources().getString(R.string.loading), true, false);
            OTP = et_otp.getText().toString();
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            try {
                int listid = bmodel.configurationMasterHelper.getActivtyType("RE");

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("VersionCode",
                        bmodel.getApplicationVersionNumber());
                jsonObject.put("UserId", bmodel.userMasterHelper
                        .getUserMasterBO().getUserid());
                jsonObject.put("RetailerId", bmodel.getRetailerMasterBO().getRetailerID());
                jsonObject.put("LoginId", bmodel.getAppDataProvider().getUserName().trim());
                jsonObject.put("MobileDateTime",
                        Utils.getDate("yyyy/MM/dd HH:mm:ss"));
                jsonObject.put("MobileUTCDateTime",
                        Utils.getGMTDateTime("yyyy/MM/dd HH:mm:ss"));
                jsonObject.put("OTPValue", OTP);
                jsonObject.put("ActivityType", listid);
                jsonObject.put(SynchronizationHelper.VERSION_NAME, bmodel.getApplicationVersionName());

                String appendUrl = "/OTPValidator/Validate";

                Vector<String> responseVector = bmodel.synchronizationHelper.getUploadResponseForgotPassword(jsonObject, appendUrl, false);
                if (responseVector.size() > 0) {
                    for (String s : responseVector) {
                        JSONObject jsonObjectResponse = new JSONObject(s);

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
                } else {
                    if (!bmodel.synchronizationHelper.getAuthErroCode().equals(IvyConstants.AUTHENTICATION_SUCCESS_CODE)) {
                        String errorMsg = bmodel.synchronizationHelper.getErrormessageByErrorCode().get(bmodel.synchronizationHelper.getAuthErroCode());
                        if (errorMsg != null) {
                            Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), getActivity().getResources().
                                    getString(R.string.data_not_downloaded), Toast.LENGTH_SHORT).show();
                        }
                    }

                }

            } catch (Exception e) {
                Commons.printException(e);
                return downloadStatus;
            }
            return downloadStatus;
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Integer result) {
            if (progressDialogue != null)
                progressDialogue.dismiss();
            if (result == 0 || result == 9) {
                bmodel.showAlert(
                        getActivity().getResources().getString(
                                R.string.communication_error_please_try_again),
                        0);
            } else if (result == 1) {
                onCreateDialog(0, "OTP Activated Successfully").show();
            } else if (result == -1) {
                onCreateDialog(1, getActivity().
                        getResources().getString(R.string.otp_expired)).show();
            } else if (result == -2) {
                onCreateDialog(1, getActivity().
                        getResources().getString(R.string.invalid_otp)).show();
            } else if (result == -3) {
                onCreateDialog(1, getActivity().
                        getResources().getString(R.string.activated_already)).show();
            } else if (result == -4) {
                onCreateDialog(1, getActivity().
                        getResources().getString(R.string.token_expired)).show();
            }
        }

    }

    protected Dialog onCreateDialog(int id, String message) {
        switch (id) {
            case 0:
                return new AlertDialog.Builder(getActivity(), R.style.DatePickerDialogStyle)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(message)
                        .setPositiveButton(R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        dismiss();
                                        otpListener.dismissListener(type, true);
                                    }
                                }).create();
            case 1:
                return new AlertDialog.Builder(getActivity(), R.style.DatePickerDialogStyle)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(message)
                        .setPositiveButton(R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                    }
                                }).create();

        }
        return null;
    }
}
