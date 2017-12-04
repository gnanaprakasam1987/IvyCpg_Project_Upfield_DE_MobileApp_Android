package com.ivy.cpg.login;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.ivy.lib.Utils;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.ApplicationConfigs;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.LoginHelper;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static android.content.Context.MODE_PRIVATE;
import static com.ivy.sd.png.model.ApplicationConfigs.LANGUAGE;

/**
 * Created by ivyuser on 4/12/17.
 */

public class LoginPresenterImpl implements LoginContractor.LoginPresenter {
    private Context context;
    private BusinessModel businessModel;
    LoginContractor.LoginView loginView;
    private LoginHelper loginHelper;
    private boolean syncDone;
    private SharedPreferences mPasswordLockCountPref;

    public LoginPresenterImpl(Context context) {
        this.context = context;
        businessModel = (BusinessModel) context.getApplicationContext();
        loginHelper = LoginHelper.getInstance(context);

    }

    @Override
    public void setView(LoginContractor.LoginView loginView) {
        this.loginView = loginView;
    }

    @Override
    public void loadInitialData() {
        /* Show Forget password dialog.*/

        syncDone = businessModel.userMasterHelper.getSyncStatus();
        if (syncDone) {
            businessModel.configurationMasterHelper.loadConfigurationForLoginScreen();
            businessModel.configurationMasterHelper.loadPasswordConfiguration();
            businessModel.userMasterHelper.downloadDistributionDetails();
            if (businessModel.configurationMasterHelper.IS_PASSWORD_ENCRIPTED)
                businessModel.synchronizationHelper.setEncryptType();

            businessModel.configurationMasterHelper.downloadChangepasswordConfig();
            if (businessModel.configurationMasterHelper.SHOW_FORGET_PASSWORD) {

                loginView.showForgotPassword();

            }
        }
        businessModel.synchronizationHelper.loadErrorCode();
        /* Set default language */
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        String initialLanguage = "en";

        if (!Locale.getDefault().equals(
                sharedPrefs.getString("languagePref", LANGUAGE))) {
            initialLanguage = sharedPrefs.getString("languagePref", LANGUAGE);
            Locale locale = new Locale(sharedPrefs.getString("languagePref", LANGUAGE).substring(0, 2));
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            context.getResources().updateConfiguration(config,
                    context.getResources().getDisplayMetrics());

        }
        // Getting back date
        DataMembers.backDate = sharedPrefs.getString("backDate", "");
        /** When language preference is changed, recreate the activity.**/

        if (!initialLanguage.equals(sharedPrefs.getString("languagePref",
                LANGUAGE))) {
            loginView.reload();
        }

        /* Enable "Network Provider Date/Time". */
        businessModel.useNetworkProvidedValues();

        //mLastSyncSharedPref = context.getSharedPreferences("lastSync", MODE_PRIVATE);
        mPasswordLockCountPref = context.getSharedPreferences("passwordlock", MODE_PRIVATE);
        SharedPreferences.Editor edt = mPasswordLockCountPref.edit();
        edt.putInt("lockcount", mPasswordLockCountPref.getInt("lockcount", 0));

        edt.apply();

        if (businessModel.configurationMasterHelper.SHOW_GPS_ENABLE_DIALOG) {
            if (!businessModel.locationUtil.isGPSProviderEnabled()) {
                GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
                int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);
                if (resultCode == ConnectionResult.SUCCESS) {
                    loginView.requestLocation();
                } else {
                    loginView.onCreateDialog();
                }
            }
        }
    }

    @Override
    public void getSupportNo() {
        loginView.setSupportNoTV(loginHelper.getSupportNo());
    }

    @Override
    public void checkDB() {
        if (businessModel.synchronizationHelper.isExternalStorageAvailable()) {
            if (syncDone) {
                loginView.retrieveDBData();
            } else {
                try {
                    File backupDB = new File(
                            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                                    + "/pandg/" + DataMembers.DB_NAME);
                    if (backupDB.exists()) {
                        new RestoreDB().execute();
                    }
                } catch (Exception e) {
                }
            }
        } else {
            loginView.showAlert(context.getResources().getString(R.string.external_storage_not_avail), true);
        }
    }

    class RestoreDB extends AsyncTask<Integer, Integer, Boolean> {

        private ProgressDialog progressDialogue;

        protected void onPreExecute() {
            loginView.showProgressDialog(context.getResources().getString(R.string.Restoring_database));
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            if (businessModel.synchronizationHelper.reStoreDB()) {
                return true;

            } else {
                return false;
            }
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground
            loginView.dismissProgressDialog();
            if (result) {
                loginView.showAlert(context.getResources().getString(R.string.database_restored), false);
                syncDone = businessModel.userMasterHelper.getSyncStatus();
                if (syncDone) {
                    businessModel.userMasterHelper.downloadDistributionDetails();
                    loginView.retrieveDBData();
                } else {
                    loginView.showAlert(context.getResources().getString(R.string.database_not_restored), false);
                }
            }
        }
    }

    @Override
    public void copyAssetsProfile() {
        new AsyncCopyProfile().execute();
    }

    public class AsyncCopyProfile extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {
                copyAssets();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                Commons.printException(e);
            }
            return null;
        }

    }

    private void copyAssets() {
        AssetManager assetManager = context.getAssets();
        String[] files = {"datawedge.db"};
        for (String filename : files) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(filename);
                File outFile = new File(
                        "/enterprise/device/settings/datawedge/autoimport/",
                        "datawedge.db");

                outFile.setExecutable(true);
                outFile.setReadable(true);
                outFile.setWritable(true);
                out = new FileOutputStream(outFile);
                copyFile(in, out);
                try {
                    chmod(outFile, 0666);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    Commons.printException(e);
                }
                in.close();
                in = null;
                out.flush();
                out.close();
                out = null;

            } catch (IOException e) {
            }
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {

            out.write(buffer, 0, read);
        }
    }

    //
    private void chmod(File path, int mode) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class fileUtils = Class.forName("android.os.FileUtils");
        Method setPermissions = fileUtils.getMethod("setPermissions",
                String.class, int.class, int.class, int.class);
        setPermissions.invoke(null, path.getAbsolutePath(),
                mode, -1, -1);
    }

    @Override
    public void assignServerUrl() {
        // Assign server url
        if (ApplicationConfigs.withActivation) {
            DataMembers.SERVER_URL = PreferenceManager
                    .getDefaultSharedPreferences(context).getString("appUrlNew", "");
            DataMembers.ACTIVATION_KEY = PreferenceManager
                    .getDefaultSharedPreferences(context).getString("activationKey", "");
        }

         /* Display application Phase if the environment is other than live.*/
        String phase = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("application", "");
        if (phase.length() > 0)
            if (Pattern.compile(Pattern.quote("ivy"), Pattern.CASE_INSENSITIVE)
                    .matcher(phase).find()) {
                businessModel.synchronizationHelper.isInternalActivation = true;
            }
    }


    public void checkLogin() {
        if (businessModel.configurationMasterHelper.SHOW_CHANGE_PASSWORD) {
            String createdDate = businessModel.synchronizationHelper.getPasswordCreatedDate();
            if (createdDate != null && !createdDate.equals("")) {
                int result = SDUtil.compareDate(businessModel.configurationMasterHelper.getPasswordExpiryDate(createdDate),
                        businessModel.userMasterHelper.getUserMasterBO().getDownloadDate(), "yyyy/MM/dd");
                if (result == -1) {
                    loginView.goToChangePwd();
                } else {
                    checkAttendance();
                    //used for showing password expiring date
                    int days = (int) getDifferenceDays(businessModel.configurationMasterHelper.getPasswordExpiryDate(createdDate),
                            businessModel.userMasterHelper.getUserMasterBO().getDownloadDate(), "yyyy/MM/dd");
                    Commons.print("Password Expiry in " + days + " days");
                    if (days < (businessModel.configurationMasterHelper.PSWD_EXPIRY * 0.2)) {
                        loginView.showAlert(context.getResources().getQuantityString(R.plurals.password_expires, days, days), false);
                    }
                }
            } else {
                checkAttendance();
            }
        } else {
            checkAttendance();
        }
    }

    public static long getDifferenceDays(String firstDate, String secondDate,
                                         String format) {
        long diff = 0;
        SimpleDateFormat sf = new SimpleDateFormat(format);
        try {
            diff = sf.parse(firstDate).getTime() - sf.parse(secondDate).getTime();
        } catch (ParseException e) {
            Commons.printException(e);
        }
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
    }

    private void checkAttendance() {
        if (businessModel.configurationMasterHelper.SHOW_ATTENDANCE) {
            if (businessModel.mAttendanceHelper.loadAttendanceMaster()) {
                businessModel.loadDashBordHome();
                loginView.goToHomeScreen();
            } else {
                loginView.goToAttendance();
            }
        } else {
            businessModel.loadDashBordHome();
            loginView.goToHomeScreen();
        }
    }

    @Override
    public void onLoginClick() {
        if (syncDone) {
            if (ApplicationConfigs.checkUTCTime && businessModel.isOnline()) {
                new DownloadUTCTime().execute();
            } else {
                loginView.showProgressDialog(context.getResources().getString(R.string.loading_data));

                //handle password lock in off line based on reached maximum_attempt_count compare with mPasswordLockCountPref count
                int count = mPasswordLockCountPref.getInt("passwordlock", 0);
                if (count + 1 == businessModel.configurationMasterHelper.MAXIMUM_ATTEMPT_COUNT)
                    loginView.sendMessageToHandler(DataMembers.NOTIFY_NOT_USEREXIST);
                else
                    loginView.threadActions(DataMembers.LOCAL_LOGIN);
            }
        } else {
            loginView.showProgressDialog(context.getResources().getString(R.string.auth_and_downloading_masters));

            if (!DataMembers.SERVER_URL.equals("")) {
                loginView.mIterateCount.M_ITERATE_COUNT = businessModel.synchronizationHelper.getmRetailerWiseIterateCount();
                callAuthentication(false);
            } else {
                loginView.showAlert(context.getResources().getString(R.string.download_url_empty), false);
                loginView.dismissProgressDialog();
            }
        }
    }

    @Override
    public void callAuthentication(boolean isDeviceChanged) {
        new Authentication(isDeviceChanged).execute();
    }


    private class DownloadUTCTime extends
            AsyncTask<Integer, Integer, Integer> {

        private int UTCflag;

        protected void onPreExecute() {
            /*alertDialog = new ProgressDialog(LoginScreen.this);
            alertDialog.setMessage(getResources().getString(R.string.checking_time));
            alertDialog.setCancelable(false);
            alertDialog.show();*/
            loginView.dismissProgressDialog();
            loginView.showProgressDialog(context.getResources().getString(R.string.checking_time));
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        @Override
        protected Integer doInBackground(Integer... params) {
            try {
                UTCflag = businessModel.synchronizationHelper.getUTCDateTimeNew("/UTCDateTime");
            } catch (Exception e) {
                Commons.printException(e);
            }
            return UTCflag;
        }

        protected void onPostExecute(Integer result) {
            if (UTCflag == 2) {
                loginView.dismissProgressDialog();
                loginView.enableGPSDialog();
            } else {
                loginView.setAlertDialogMessage(context.getResources().getString(R.string.loading_data));
                loginView.threadActions(DataMembers.LOCAL_LOGIN);
            }
        }
    }

    /**
     * class is used to Authenticate application ang get token for Authorization
     */
    class Authentication extends AsyncTask<String, String, String> {
        JSONObject jsonObject;
        boolean changeDeviceId;

        public Authentication(boolean changeDeviceId) {
            this.changeDeviceId = changeDeviceId;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("LoginId", businessModel.userNameTemp);
                jsonObj.put("Password", businessModel.passwordTemp);
                jsonObj.put(SynchronizationHelper.VERSION_CODE,
                        businessModel.getApplicationVersionNumber());

                jsonObj.put("Model", Build.MODEL);
                jsonObj.put("Platform", "Android");
                jsonObj.put("OSVersion", android.os.Build.VERSION.RELEASE);
                jsonObj.put("FirmWare", "");
                jsonObj.put("DeviceId",
                        businessModel.activationHelper.getIMEINumber());
                jsonObj.put("RegistrationId", businessModel.regid);
                jsonObj.put("DeviceUniqueId", businessModel.activationHelper.getDeviceId());
                if (DataMembers.ACTIVATION_KEY != null && !DataMembers.ACTIVATION_KEY.isEmpty())
                    jsonObj.put("ActivationKey", DataMembers.ACTIVATION_KEY);
                jsonObj.put(SynchronizationHelper.MOBILE_DATE_TIME,
                        Utils.getDate("yyyy/MM/dd HH:mm:ss"));
                jsonObj.put(SynchronizationHelper.MOBILE_UTC_DATE_TIME,
                        Utils.getGMTDateTime("yyyy/MM/dd HH:mm:ss"));
                if (!DataMembers.backDate.isEmpty())
                    jsonObj.put(SynchronizationHelper.REQUEST_MOBILE_DATE_TIME,
                            SDUtil.now(SDUtil.DATE_TIME_NEW));
                this.jsonObject = jsonObj;
            } catch (JSONException jsonExpection) {
                Commons.print(jsonExpection.getMessage());
            }
        }

        @Override
        protected String doInBackground(String... params) {
            String LoginResponse = businessModel.synchronizationHelper.userAuthenticate(jsonObject, changeDeviceId);
            try {
                JSONObject jsonObject = new JSONObject(LoginResponse);
                Iterator itr = jsonObject.keys();
                while (itr.hasNext()) {
                    String key = (String) itr.next();
                    if (key.equals(SynchronizationHelper.ERROR_CODE)) {
                        String errorCode = jsonObject.getString(key);
                        if (errorCode.equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
                            businessModel.synchronizationHelper
                                    .parseJSONAndInsert(jsonObject, false);
                            businessModel.userMasterHelper.downloadUserDetails();
                            businessModel.userMasterHelper.downloadDistributionDetails();
                        }
                        return errorCode;
                    }
                }
            } catch (JSONException jsonExpection) {
                Commons.print(jsonExpection.getMessage());
            }
            return "E01";
        }

        @Override
        protected void onPostExecute(String output) {
            super.onPostExecute(output);
            loginView.dismissProgressDialog();
            if (output.equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
                new LoginScreen.CheckNewVersionTask().execute();
            } else {
                if (output.equals("E27")) {
                    loginView.showDialog();
                } else {
                    if (output.equals("E25")) {
                        loginView.showForgotPassword();
                    }

                    String ErrorMessage = businessModel.synchronizationHelper.getErrormessageByErrorCode().get(output);

                    if (ErrorMessage != null) {
                        loginView.showAlert(ErrorMessage, false);
                    } else {
                        loginView.showAlert("Connection Exception", false);
                    }
                }


            }

        }
    }
}
