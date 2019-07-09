package com.ivy.cpg.view.login.password;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.core.IvyConstants;
import com.ivy.cpg.view.login.LoginHelper;
import com.ivy.lib.Utils;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.AppUtils;
import com.ivy.utils.FontUtils;
import com.ivy.utils.NetworkUtils;

import org.json.JSONObject;

import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Pattern;

public class ResetPasswordDialog extends Dialog {

    private EditText edtOTP;
    private BusinessModel bmodel;
    private String Npassword;
    private Context ctx;

    public ResetPasswordDialog(Context context) {
        super(context);
        ctx = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (getWindow() != null)
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.dialog_forgot_password);
        setCancelable(false);
        bmodel = (BusinessModel) ctx.getApplicationContext();
        bmodel.configurationMasterHelper.downloadPasswordPolicy();
        edtOTP = findViewById(R.id.edtOtp);
        EditText edtNewPswd = findViewById(R.id.edtNewPassword);
        EditText edtRePswd = findViewById(R.id.edtConfirmPassword);
        edtOTP.setTypeface(FontUtils.getFontRoboto(ctx, FontUtils.FontType.LIGHT));
        edtNewPswd.setTypeface(FontUtils.getFontRoboto(ctx, FontUtils.FontType.LIGHT));
        edtRePswd.setTypeface(FontUtils.getFontRoboto(ctx, FontUtils.FontType.LIGHT));
        Button btnSubmit = findViewById(R.id.btnSubmit);
        TextView lbl_forget_pswd = findViewById(R.id.lbl_forget_pswd);
        TextView lbl_login = findViewById(R.id.login);
        lbl_login.setTypeface(FontUtils.getFontRoboto(ctx, FontUtils.FontType.LIGHT));
        lbl_forget_pswd.setTypeface(FontUtils.getFontBalooHai(ctx, FontUtils.FontType.REGULAR));
        lbl_login.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        btnSubmit.setTypeface(FontUtils.getFontBalooHai(ctx, FontUtils.FontType.REGULAR));
        btnSubmit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (edtNewPswd.getText() != null) {
                    Npassword = edtNewPswd.getText().toString();
                    String password = edtNewPswd.getText().toString();
                    if (edtOTP.getText().toString().length() == 0) {
                        edtOTP.requestFocus();
                        edtOTP.setError(ctx.getResources().getString(R.string.enter_otp));
                    } else if (password.length() == 0) {
                        edtNewPswd.requestFocus();
                        edtNewPswd.setError(ctx.getResources().getString(R.string.enter_password));
                    } else if (edtNewPswd.getText().toString().contains(" ")) {
                        edtNewPswd.requestFocus();
                        edtNewPswd.setError(ctx.getResources().getString(R.string.space_not_allowed));
                    } else if (password.length() < bmodel.configurationMasterHelper.PSWD_MIN_LEN
                            || password.length() > bmodel.configurationMasterHelper.PSWD_MAX_LEN
                            && bmodel.configurationMasterHelper.PSWD_MIN_LEN != 0
                            && bmodel.configurationMasterHelper.PSWD_MAX_LEN != 0) {
                        edtNewPswd.requestFocus();
                        edtNewPswd.setError(ctx.getResources().getString(R.string.password_should_contain) + " "
                                + bmodel.configurationMasterHelper.PSWD_MIN_LEN
                                + "-"
                                + bmodel.configurationMasterHelper.PSWD_MAX_LEN
                                + " " + ctx.getResources().getString(R.string.letters));
                    } else if (bmodel.configurationMasterHelper.IS_CHARACTER
                            && bmodel.configurationMasterHelper.IS_UPPER_CASE
                            && !Pattern.compile("[A-Z]").matcher(password)
                            .find()) {
                        edtNewPswd.requestFocus();
                        edtNewPswd.setError(ctx.getResources().getString(R.string.upper_case_mandatory));
                    } else if (bmodel.configurationMasterHelper.IS_CHARACTER
                            && bmodel.configurationMasterHelper.IS_LOWER_CASE
                            && !Pattern.compile("[a-z]").matcher(password)
                            .find()) {
                        edtNewPswd.requestFocus();
                        edtNewPswd.setError(ctx.getResources().getString(R.string.lower_case_mandatory));
                    } else if (bmodel.configurationMasterHelper.IS_NUMERIC
                            && !Pattern.compile("[0-9]").matcher(password)
                            .find()) {
                        edtNewPswd.requestFocus();
                        edtNewPswd.setError(ctx.getResources().getString(R.string.numeric_mandatory));
                    } else if (bmodel.configurationMasterHelper.IS_SPECIAL_CASE
                            && !Pattern.compile("[/,:<>!~@#$%^&amp;*()+=?\"|\\-]")
                            .matcher(password).find()) {
                        edtNewPswd.requestFocus();
                        edtNewPswd.setError(ctx.getResources().getString(R.string.special_case_mandatory));
                    } else if (bmodel.configurationMasterHelper.IS_SAME_LOGIN
                            && bmodel.getAppDataProvider().getUser()
                            .getLoginName().equals(password)) {
                        edtNewPswd.requestFocus();
                        edtNewPswd.setError(ctx.getResources().getString(R.string.password_should_not_be_same));
                    } else if (edtRePswd.getText().toString().isEmpty()) {
                        edtRePswd.requestFocus();
                        edtRePswd.setError(ctx.getResources().getString(R.string.enter_confirm_password));
                    } else if (!edtRePswd.getText().toString().equals(password)) {
                        edtRePswd.requestFocus();
                        edtRePswd.setError(ctx.getResources().getString(R.string.password_not_matched));
                    } else {
                        if (NetworkUtils.isNetworkConnected(ctx)) {
                            Npassword = edtNewPswd.getText()
                                    .toString();
                            new uploadPassword().execute();
                        } else {
                            Toast.makeText(
                                    ctx,
                                    ctx.getResources().getString(
                                            R.string.no_network_connection), Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }

                }

            }

        });
    }

    class uploadPassword extends AsyncTask<Integer, Integer, Integer> {

        private ProgressDialog progressDialogue;
        private int downloadStatus = 0;
        private String OTP = "";

        protected void onPreExecute() {
            progressDialogue = ProgressDialog.show(ctx,
                    DataMembers.SD, ctx.getResources().getString(R.string.loading), true, false);
            OTP = edtOTP.getText().toString();

        }

        @Override
        protected Integer doInBackground(Integer... params) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("VersionCode",
                        bmodel.getApplicationVersionNumber());
                jsonObject.put("LoginId", bmodel.getAppDataProvider().getUserName().trim());
                jsonObject.put("MobileDateTime",
                        Utils.getDate("yyyy/MM/dd HH:mm:ss"));
                jsonObject.put("MobileUTCDateTime",
                        Utils.getGMTDateTime("yyyy/MM/dd HH:mm:ss"));
                // jsonObject.put("ActivityType", listid);
                jsonObject.put("OTPValue", OTP);

                jsonObject.put("NewPassword", Npassword);
                jsonObject.put(SynchronizationHelper.VERSION_NAME, AppUtils.getApplicationVersionName(ctx));
                Commons.printInformation("Reset password upload " + jsonObject.toString());
                String appendUrl = "/V1/ForgotPassword/Validate";
                Vector<String> responseVector = bmodel.synchronizationHelper.getUploadResponseForgotPassword(jsonObject, appendUrl, false);
                if (responseVector.size() > 0) {
                    for (String s : responseVector) {
                        JSONObject jsonObjectResponse = new JSONObject(s);

                        Iterator itr = jsonObjectResponse.keys();
                        while (itr.hasNext()) {
                            String key = (String) itr.next();
                            if (key.equals("Response")) {
                                SharedPreferences passwordlockSharedPreference = ctx.getSharedPreferences("passwordlock", Context.MODE_PRIVATE);
                                SharedPreferences.Editor edt = passwordlockSharedPreference.edit();
                                edt.putInt("lockcount", 0);
                                edt.apply();
                                downloadStatus = jsonObjectResponse.getInt("Response");
                                Commons.printInformation("Reset password upload Response " + jsonObject.toString());
                            } else if (key.equals("ErrorCode")) {
                                String tokenResponse = jsonObjectResponse.getString("ErrorCode");
                                Commons.printInformation("Reset password upload Error " + jsonObject.toString());
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
                            Toast.makeText(ctx, errorMsg, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ctx, ctx.getResources().getString(R.string.data_not_downloaded), Toast.LENGTH_SHORT).show();
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
                        ctx.getResources().getString(
                                R.string.communication_error_please_try_again),
                        0);
            } else if (result == 1) {
                if (LoginHelper.getInstance(ctx).IS_PASSWORD_LOCK) {
                    SharedPreferences mPasswordLockCountPref = ctx.getSharedPreferences("passwordlock", Context.MODE_PRIVATE);
                    SharedPreferences.Editor edt = mPasswordLockCountPref.edit();
                    edt.putInt("passwordlock", 0);
                    edt.apply();
                }
                bmodel.getAppDataProvider().setUserPassword(Npassword);

                bmodel.userMasterHelper.changePassword(bmodel.getAppDataProvider()
                        .getUser().getUserid(), bmodel.getAppDataProvider().getUserPassword());

                onCreateDialog(0, ctx.getResources().getString(R.string.password_changed_successfully)).show();
            } else if (result == -1) {
                onCreateDialog(1, ctx.getResources().getString(R.string.otp_expired)).show();
            } else if (result == -2) {
                onCreateDialog(1, ctx.getResources().getString(R.string.invalid_otp)).show();
            } else if (result == -3) {
                onCreateDialog(1, ctx.getResources().getString(R.string.activated_already)).show();
            } else if (result == -4) {
                onCreateDialog(1, ctx.getResources().getString(R.string.token_expired)).show();
            }
        }

    }

    protected Dialog onCreateDialog(int id, String message) {
        switch (id) {
            case 0:
                return new AlertDialog.Builder(ctx)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(message)
                        .setPositiveButton(R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        dismiss();
                                    }
                                }).create();

            case 1:
                return new AlertDialog.Builder(ctx)
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

    @Override
    public void onBackPressed() {

    }


}
