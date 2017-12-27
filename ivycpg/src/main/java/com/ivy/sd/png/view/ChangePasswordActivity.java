package com.ivy.sd.png.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.login.LoginHelper;
import com.ivy.cpg.view.login.LoginScreen;
import com.ivy.lib.Utils;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import org.json.JSONObject;

import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Pattern;

public class ChangePasswordActivity extends IvyBaseActivityNoActionBar {

    private EditText edtNewPswd, edtRePswd, edtCurrPswd;
    private TextView passwordExpiryTV;
    private BusinessModel bmodel;
    private Button btnSubmit;//, btnClose;
    private Intent in;
    private boolean isExpired = false;
    private String mPasswordCreatedDated = "";
    private String Cpassword, Npassword;
    private boolean fromReset = false;
    private boolean isFromSettingScreen = false;

    private TextView btnClose, title_tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_change_password);
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        in = getIntent();
        if (in.getExtras() != null) {
            isExpired = in.getExtras().getBoolean("isExpired");
            isFromSettingScreen = in.getExtras().getBoolean("isFromSetting");
            fromReset = in.getExtras().getBoolean("resetpassword", false);
        }
        edtCurrPswd = (EditText) findViewById(R.id.edtCurrentPswd);
        edtNewPswd = (EditText) findViewById(R.id.edtNewPassword);
        edtRePswd = (EditText) findViewById(R.id.edtConfirmPassword);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
//        btnClose = (Button) findViewById(R.id.btnClose);
        btnClose = (TextView) findViewById(R.id.btnClose);
        title_tv = (TextView) findViewById(R.id.title_tv);

        edtCurrPswd.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        edtNewPswd.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        edtRePswd.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        btnSubmit.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        btnClose.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        title_tv.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        if (isFromSettingScreen)
            btnClose.setText(getResources().getString(R.string.back));

        btnClose.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (!mPasswordCreatedDated.equals("")) {
                    int result = SDUtil.compareDate(LoginHelper.getInstance(getApplicationContext()).getPasswordExpiryDate(mPasswordCreatedDated), bmodel.userMasterHelper.getUserMasterBO().getDownloadDate(), "yyyy/MM/dd");
                    if (result == -1) {
                        startActivity(new Intent(ChangePasswordActivity.this,
                                LoginScreen.class));
                    } else {
                        finish();
                    }
                } else {
                    finishReset();
                    finish();
                }
            }
        });
        btnSubmit.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (edtNewPswd.getText() != null) {
                    String password = edtNewPswd.getText().toString();
                    if (edtCurrPswd.getText().toString().length() == 0) {
                        edtCurrPswd.requestFocus();
                        edtCurrPswd.setError(getResources().getString(R.string.enter_current_password));
                    } else if (password.length() == 0) {
                        edtNewPswd.requestFocus();
                        edtNewPswd.setError(getResources().getString(R.string.enter_new_password));
                    } else if (edtCurrPswd.getText().toString().equals(password)) {
                        edtNewPswd.requestFocus();
                        edtNewPswd.setError(getResources().getString(R.string.password_matched));
                    } else if (edtNewPswd.getText().toString().contains(" ")) {
                        edtNewPswd.requestFocus();
                        edtNewPswd.setError(getResources().getString(R.string.space_not_allowed));
                    } else if (password.length() < bmodel.configurationMasterHelper.PSWD_MIN_LEN
                            || password.length() > bmodel.configurationMasterHelper.PSWD_MAX_LEN
                            && bmodel.configurationMasterHelper.PSWD_MIN_LEN != 0
                            && bmodel.configurationMasterHelper.PSWD_MAX_LEN != 0) {
                        edtNewPswd.requestFocus();
                        edtNewPswd.setError(getResources().getString(R.string.password_should_contain) + " "
                                + bmodel.configurationMasterHelper.PSWD_MIN_LEN
                                + "-"
                                + bmodel.configurationMasterHelper.PSWD_MAX_LEN
                                + " " + getResources().getString(R.string.letters));
                    } else if (bmodel.configurationMasterHelper.IS_CHARACTER
                            && bmodel.configurationMasterHelper.IS_UPPER_CASE
                            && !Pattern.compile("[A-Z]").matcher(password)
                            .find()) {
                        edtNewPswd.requestFocus();
                        edtNewPswd.setError(getResources().getString(R.string.upper_case_mandatory));
                    } else if (bmodel.configurationMasterHelper.IS_CHARACTER
                            && bmodel.configurationMasterHelper.IS_LOWER_CASE
                            && !Pattern.compile("[a-z]").matcher(password)
                            .find()) {
                        edtNewPswd.requestFocus();
                        edtNewPswd.setError(getResources().getString(R.string.lower_case_mandatory));
                    } else if (bmodel.configurationMasterHelper.IS_NUMERIC
                            && !Pattern.compile("[0-9]").matcher(password)
                            .find()) {
                        edtNewPswd.requestFocus();
                        edtNewPswd.setError(getResources().getString(R.string.numeric_mandatory));
                    } else if (bmodel.configurationMasterHelper.IS_SPECIAL_CASE
                            && !Pattern.compile("[/,.:<>!~@#$%^&;*()+=?()\"|!\\-_]")
                            .matcher(password).find()) {
                        edtNewPswd.requestFocus();
                        edtNewPswd.setError(getResources().getString(R.string.special_case_mandatory));
                    } else if (bmodel.configurationMasterHelper.IS_SAME_LOGIN
                            && bmodel.userMasterHelper.getUserMasterBO()
                            .getLoginName().equals(password)) {
                        edtNewPswd.requestFocus();
                        edtNewPswd.setError(getResources().getString(R.string.password_should_not_be_same));
                    } else if (edtRePswd.getText().toString().isEmpty()) {
                        edtRePswd.requestFocus();
                        edtRePswd.setError(getResources().getString(R.string.enter_confirm_password));
                    } else if (!edtRePswd.getText().toString().equals(password)) {
                        edtRePswd.requestFocus();
                        edtRePswd.setError(getResources().getString(R.string.password_not_matched));
                    } else {
                        if (bmodel.isOnline()) {
                            Cpassword = edtCurrPswd.getText()
                                    .toString();
                            Npassword = edtNewPswd.getText()
                                    .toString();
                            new uploadPassword().execute();
                        } else {
                            Toast.makeText(
                                    ChangePasswordActivity.this,
                                    getResources().getString(
                                            R.string.no_network_connection), Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }

                }

            }

        });

        mPasswordCreatedDated = LoginHelper.getInstance(this).getPasswordCreatedDate();
        if (!mPasswordCreatedDated.equals("")) {
            int result = SDUtil.compareDate(LoginHelper.getInstance(this).getPasswordExpiryDate(mPasswordCreatedDated), bmodel.userMasterHelper.getUserMasterBO().getDownloadDate(), "yyyy/MM/dd");
            if (result == -1) {
                passwordExpiryTV = (TextView) findViewById(R.id.tv_password_expired);
                passwordExpiryTV.setVisibility(View.VISIBLE);
                passwordExpiryTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            }
        }


    }

    class uploadPassword extends AsyncTask<Integer, Integer, Integer> {

        private ProgressDialog progressDialogue;
        String errorMsg = "";

        protected void onPreExecute() {
            progressDialogue = ProgressDialog.show(ChangePasswordActivity.this,
                    DataMembers.SD, getResources().getString(R.string.loading), true, false);
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            int downloadStatus = 0;
            try {

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("DeviceId",
                        bmodel.activationHelper.getIMEINumber());
                jsonObject.put("VersionCode",
                        bmodel.getApplicationVersionNumber());
                jsonObject.put("UserId", bmodel.userMasterHelper
                        .getUserMasterBO().getUserid());

                jsonObject.put("OldPassword", Cpassword);
                jsonObject.put("NewPassword", Npassword);
                jsonObject.put("MobileDateTime",
                        Utils.getDate("yyyy/MM/dd HH:mm:ss"));
                jsonObject.put("MobileUTCDateTime",
                        Utils.getGMTDateTime("yyyy/MM/dd HH:mm:ss"));
                Commons.printInformation("Change password upload " + jsonObject.toString());
                final String appendUrl = DataMembers.CHANGE_PWD;
                Vector<String> responseVector = bmodel.synchronizationHelper.getUploadResponseForgotPassword(jsonObject, appendUrl, true);
                if (responseVector.size() > 0) {
                    for (String s : responseVector) {
                        JSONObject jsonObjectResponse = new JSONObject(s);

                        Iterator itr = jsonObjectResponse.keys();
                        while (itr.hasNext()) {
                            String key = (String) itr.next();
                            if (key.equals("Response")) {
                                downloadStatus = jsonObjectResponse.getInt("Response");
                                Commons.printInformation("Change password upload Response " + jsonObject.toString());
                            } else if (key.equals("ErrorCode")) {
                                String tokenResponse = jsonObjectResponse.getString("ErrorCode");
                                Commons.printInformation("Change password upload Error " + jsonObjectResponse.toString());
                                if (tokenResponse.equals(SynchronizationHelper.INVALID_TOKEN)
                                        || tokenResponse.equals(SynchronizationHelper.TOKEN_MISSINIG)
                                        || tokenResponse.equals(SynchronizationHelper.EXPIRY_TOKEN_CODE)) {
                                    errorMsg = jsonObjectResponse.getString("ErrorMsg");
                                    return -1;

                                }

                            }

                        }
                    }

                } else {
                    if (!bmodel.synchronizationHelper.getAuthErroCode().equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
                        String errorMsg = bmodel.synchronizationHelper.getErrormessageByErrorCode().get(bmodel.synchronizationHelper.getAuthErroCode());
                        if (errorMsg != null) {
                            Toast.makeText(ChangePasswordActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ChangePasswordActivity.this, getResources().getString(R.string.data_not_downloaded), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

            } catch (Exception e) {
                Commons.printException(e);
                return downloadStatus;
            }
            return downloadStatus; // Return your real result here
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Integer result) {
            // result is the value returned from doInBackground
            if (progressDialogue != null)
                progressDialogue.dismiss();
            if (result == 0 || result == 9) {
                bmodel.showAlert(
                        getResources().getString(
                                R.string.communication_error_please_try_again),
                        0);
            } else if (result == 1) {
                finishReset();
                bmodel.passwordTemp = edtNewPswd.getText().toString();
                bmodel.userMasterHelper.changePassword(bmodel.userMasterHelper
                        .getUserMasterBO().getUserid(), bmodel.passwordTemp);
                onCreateDialog(0, getResources().getString(R.string.password_changed_successfully));

            } else if (result == -2) {
                onCreateDialog(1, getResources().getString(R.string.invalid_password));
            } else if (result == -1) {
                onCreateDialog(2, errorMsg);
            }
        }

    }

    protected Dialog onCreateDialog(int id, String message) {
        switch (id) {
            case 0:
                return new AlertDialog.Builder(ChangePasswordActivity.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(message)
                        .setPositiveButton(R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        if (isExpired) {
                                            Intent in = new Intent(
                                                    ChangePasswordActivity.this,
                                                    LoginScreen.class);
                                            startActivity(in);
                                            finish();
                                        } else {
                                            finish();
                                        }
                                    }
                                }).show();

            case 1:
                return new AlertDialog.Builder(ChangePasswordActivity.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(message)
                        .setPositiveButton(R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                    }
                                }).show();

            case 2:
                return new AlertDialog.Builder(ChangePasswordActivity.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(message)
                        .setPositiveButton(R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                    }
                                }).show();

        }
        return null;
    }

    @Override
    public void onBackPressed() {

        // super.onBackPressed();
    }

    private void finishReset() {
        if (fromReset) {
            try {
                DBUtil db = new DBUtil(ChangePasswordActivity.this, DataMembers.DB_NAME,
                        DataMembers.DB_PATH);
                db.createDataBase();
                db.openDataBase();
                db.deleteSQL(DataMembers.tbl_userMaster, null, true);
                db.closeDB();
            } catch (Exception e) {
                Commons.printException(e);
            }
        }
    }
}
