package com.ivy.cpg.view.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.ivy.cpg.primarysale.bo.DistributorMasterBO;
import com.ivy.cpg.view.attendance.AttendanceHelper;
import com.ivy.cpg.view.reports.performancereport.OutletPerfomanceHelper;
import com.ivy.cpg.view.sync.AWSConnectionHelper;
import com.ivy.cpg.view.sync.AzureConnectionHelper;
import com.ivy.cpg.view.sync.catalogdownload.CatalogImageDownloadProvider;
import com.ivy.lib.Utils;
import com.ivy.sd.png.asean.view.BuildConfig;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.model.ApplicationConfigs;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.DeviceUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static android.content.Context.MODE_PRIVATE;
import static com.ivy.sd.png.model.ApplicationConfigs.LANGUAGE;

/**
 * Created by dharmapriya.k on 4/12/17.
 */

public class LoginPresenterImpl implements LoginContract.LoginPresenter {
    private final Context context;
    private final BusinessModel businessModel;
    private LoginContract.LoginBaseView loginView;
    private final LoginHelper loginHelper;
    private boolean syncDone;
    public SharedPreferences mPasswordLockCountPref;
    private SharedPreferences mLastSyncSharedPref;
    private int mIterateCount = 0;
    private TransferUtility transferUtility;
    private String initialLanguage = "en";
    private SharedPreferences sharedPrefs;
    private int mTotalRetailerCount = 0;

    private boolean isSFDC;

    public LoginPresenterImpl(Context context) {
        this.context = context;
        businessModel = (BusinessModel) context.getApplicationContext();
        loginHelper = LoginHelper.getInstance(context);

        isSFDC = !BuildConfig.FLAVOR.equalsIgnoreCase("aws");

    }

    @Override
    public void setView(LoginContract.LoginBaseView loginView) {
        this.loginView = loginView;
    }

    /**
     * Loads the initial set of data for the login screen
     */
    @Override
    public void loadInitialData() {
        syncDone = businessModel.userMasterHelper.getSyncStatus();
        if (syncDone) {
            loginHelper.loadPasswordConfiguration(context);
            businessModel.userMasterHelper.downloadDistributionDetails();
            if (loginHelper.IS_PASSWORD_ENCRYPTED)
                businessModel.synchronizationHelper.setEncryptType();

            if (loginHelper.SHOW_FORGET_PASSWORD && loginView instanceof LoginContract.LoginView) {
                ((LoginContract.LoginView) loginView).showForgotPassword();
            }
        }
        businessModel.synchronizationHelper.loadErrorCode();
        /* Set default language */
        sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(context.getApplicationContext());

        if (!Locale.getDefault().equals(
                sharedPrefs.getString("languagePref", LANGUAGE))) {
            initialLanguage = sharedPrefs.getString("languagePref", LANGUAGE);
            Locale locale = new Locale(sharedPrefs.getString("languagePref", LANGUAGE).substring(0, 2));
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            context.getApplicationContext().getResources().updateConfiguration(config,
                    context.getApplicationContext().getResources().getDisplayMetrics());

        }
        // Getting back date
        DataMembers.backDate = sharedPrefs.getString("backDate", "");

        reloadActivity();

        mLastSyncSharedPref = context.getApplicationContext().getSharedPreferences("lastSync", MODE_PRIVATE);
        mPasswordLockCountPref = context.getApplicationContext().getSharedPreferences("passwordlock", MODE_PRIVATE);
        SharedPreferences.Editor edt = mPasswordLockCountPref.edit();
        edt.putInt("lockcount", mPasswordLockCountPref.getInt("lockcount", 0));
        edt.apply();

        if (businessModel.configurationMasterHelper.SHOW_GPS_ENABLE_DIALOG) {
            if (!businessModel.locationUtil.isGPSProviderEnabled()) {
                GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
                int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context.getApplicationContext());
                if (resultCode == ConnectionResult.SUCCESS) {
                    ((LoginContract.LoginView) loginView).requestLocation();
                } else {
                    loginView.showGPSDialog();
                }
            }
        }
    }

    public void reloadActivity() {
        // When language preference is changed, recreate the activity.
        if (!initialLanguage.equals(sharedPrefs.getString("languagePref",
                LANGUAGE))) {
            loginView.reload();
        }
    }

    /**
     * Saves the last sync date and time in shared preferences
     */
    @Override
    public void applyLastSyncPref() {
        SharedPreferences.Editor edt = mLastSyncSharedPref.edit();
        edt.putString("date", DateTimeUtils.convertFromServerDateToRequestedFormat(
                DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                ConfigurationMasterHelper.outDateFormat));
        edt.putString("time", DateTimeUtils.now(DateTimeUtils.TIME));
        edt.apply();


    }

    @Override
    public void updateDownloadedTime() {

        SharedPreferences mLastUploadAndDownloadPref = context.getApplicationContext().getSharedPreferences("lastUploadAndDownload", MODE_PRIVATE);
        SharedPreferences.Editor edt = mLastUploadAndDownloadPref.edit();
        edt.putString("downloadDate",
                DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL));
        edt.putString("downloadTime", DateTimeUtils.now(DateTimeUtils.TIME));
        edt.apply();

    }

    /*
     * Saves the password lock count in shared preferences
     * */
    @Override
    public void applyPasswordLockCountPref() {
        SharedPreferences.Editor edt = mPasswordLockCountPref.edit();
        edt.putInt("passwordlock", (getPasswordLockCount() + 1));
        edt.apply();
    }

    /* Returns password lock count from shared preference
     * */
    @Override
    public int getPasswordLockCount() {
        return mPasswordLockCountPref.getInt("passwordlock", 0);
    }

    @Override
    public void getSupportNo() {
        ((LoginContract.LoginView) loginView).setSupportNoTV(loginHelper.getSupportNo(context.getApplicationContext()));
    }

    /*
     * Checks if external storage available or not
     *         not available then finishes the application
     *         available then checks if back up DB there.
     *         If backup available restores the old DB
     **/
    @Override
    public void checkDB() {
        if (businessModel.synchronizationHelper.isExternalStorageAvailable()) {
            if (syncDone) {
                if (!isSFDC)
                    ((LoginContract.LoginView) loginView).retrieveDBData();
            } else {
                try {
                    File backupDB = new File(
                            context.getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                                    + "/pandg/" + DataMembers.DB_NAME);
                    if (backupDB.exists()) {
                        new RestoreDB().execute();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            loginView.showAlert(context.getApplicationContext().getResources().getString(R.string.external_storage_not_avail), true);
        }
    }

    /*
     * Async task to restore the DB from external storage
     * */
    class RestoreDB extends AsyncTask<Integer, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            loginView.showProgressDialog(context.getApplicationContext().getResources().getString(R.string.Restoring_database));
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            return loginHelper.reStoreDB(context.getApplicationContext());
        }

        @Override
        protected void onPostExecute(Boolean result) {
            loginView.dismissAlertDialog();
            if (result) {
                loginView.showAlert(context.getApplicationContext().getResources().getString(R.string.database_restored), false);
                syncDone = businessModel.userMasterHelper.getSyncStatus();
                if (syncDone) {
                    businessModel.userMasterHelper.downloadDistributionDetails();
                    ((LoginContract.LoginView) loginView).retrieveDBData();
                } else {
                    loginView.showAlert(context.getApplicationContext().getResources().getString(R.string.database_not_restored), false);
                }
            }
        }
    }

    /*
     * Assigns server url from shared preference
     * */
    @Override
    public void assignServerUrl() {
        if (ApplicationConfigs.withActivation) {
            DataMembers.SERVER_URL = PreferenceManager
                    .getDefaultSharedPreferences(context.getApplicationContext()).getString("appUrlNew", "");
            DataMembers.ACTIVATION_KEY = PreferenceManager
                    .getDefaultSharedPreferences(context.getApplicationContext()).getString("activationKey", "");
        }

        /* Display application Phase if the environment is other than live.*/
        String phase = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext())
                .getString("application", "");
        if (!phase.equals(""))
            if (Pattern.compile(Pattern.quote("ivy"), Pattern.CASE_INSENSITIVE)
                    .matcher(phase).find()) {
                businessModel.synchronizationHelper.isInternalActivation = true;
            }
    }


    public void checkLogin() {
        if (loginHelper.SHOW_CHANGE_PASSWORD) {
            String createdDate = loginHelper.getPasswordCreatedDate(context.getApplicationContext());
            if (createdDate != null && !createdDate.equals("")) {
                int result = DateTimeUtils.compareDate(loginHelper.getPasswordExpiryDate(createdDate),
                        businessModel.userMasterHelper.getUserMasterBO().getDownloadDate(), "yyyy/MM/dd");
                if (result == -1) {
                    ((LoginContract.LoginView) loginView).goToChangePwd();
                } else {
                    checkAttendance();
                    //used for showing password expiring date
                    int days = (int) getDifferenceDays(loginHelper.getPasswordExpiryDate(createdDate),
                            businessModel.userMasterHelper.getUserMasterBO().getDownloadDate());
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

    private long getDifferenceDays(String firstDate, String secondDate) {
        long diff = 0;
        SimpleDateFormat sf = new SimpleDateFormat("yyyy/MM/dd");
        try {
            diff = sf.parse(firstDate).getTime() - sf.parse(secondDate).getTime();
        } catch (ParseException e) {
            Commons.printException(e);
        }
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
    }

    private void checkAttendance() {
        loginHelper.loadPasswordConfiguration(context.getApplicationContext());
        businessModel.userMasterHelper.downloadDistributionDetails();
        if (loginHelper.IS_PASSWORD_ENCRYPTED)
            businessModel.synchronizationHelper.setEncryptType();
        if (businessModel.configurationMasterHelper.SHOW_ATTENDANCE) {
            if (AttendanceHelper.getInstance(context).loadAttendanceMaster(context)) {
                loginView.goToHomeScreen();
            } else {
                loginView.goToAttendance();
            }
        } else {
            loginView.goToHomeScreen();
        }
    }

    @Override
    public void onLoginClick() {
        if (syncDone) {
            if (ApplicationConfigs.checkUTCTime && businessModel.isOnline()) {
                new DownloadUTCTime().execute();
            } else {
                proceedToLocalLogin();
            }
        } else {
            if (businessModel.isOnline()) {
                loginView.showProgressDialog(context.getResources().getString(R.string.auth_and_downloading_masters));

                if (!DataMembers.SERVER_URL.equals("")) {
                    mIterateCount = businessModel.synchronizationHelper.getmRetailerWiseIterateCount();
                    callInitialAuthentication(false);
                } else {
                    loginView.showAlert(context.getApplicationContext().getResources().getString(R.string.download_url_empty), false);
                    loginView.dismissAlertDialog();
                }
            } else {
                loginView.showAlert(context.getApplicationContext().getResources().getString(R.string.please_connect_to_internet), false);
            }
        }
    }

    private void proceedToLocalLogin() {
        loginView.showProgressDialog(context.getApplicationContext().getResources().getString(R.string.loading_data));

        //handle password lock in offline based on reached maximum_attempt_count compare with mPasswordLockCountPref count
        int count = mPasswordLockCountPref.getInt("passwordlock", 0);
        if (count + 1 == loginHelper.MAXIMUM_ATTEMPT_COUNT)
            ((LoginContract.LoginView) loginView).sendUserNotExistToHandler();
        else
            loginView.doLocalLogin();
    }


    @Override
    public void callInitialAuthentication(boolean isDeviceChanged) {
        new InitialAuthentication(isDeviceChanged).execute();
    }


    /*
     * get UTC date and time
     * */
    private class DownloadUTCTime extends
            AsyncTask<Integer, Integer, Integer> {


        @Override
        protected void onPreExecute() {
            loginView.dismissAlertDialog();
            loginView.showProgressDialog(context.getApplicationContext().getResources().getString(R.string.checking_time));
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            try {
                return businessModel.synchronizationHelper.getUTCDateTimeNew("/UTCDateTime");
            } catch (Exception e) {
                Commons.printException(e);
            }
            return 1;
        }

        @Override
        protected void onPostExecute(Integer result) {
            loginView.dismissAlertDialog();
            if (result == 2) {
                loginView.showAlert(context.getApplicationContext().getResources().getString(R.string.error_e24), true); // error_24 invalid date time
            } else {
                proceedToLocalLogin();
            }
        }
    }

    /**
     * class is used to Authenticate application ang get token for Authorization
     */
    class InitialAuthentication extends AsyncTask<String, String, String> {
        JSONObject jsonObject;
        final boolean isDeviceChanged;

        InitialAuthentication(boolean isDeviceChanged) {
            this.isDeviceChanged = isDeviceChanged;
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
                        DeviceUtils.getIMEINumber(context));
                jsonObj.put("RegistrationId", businessModel.regid);
                jsonObj.put("DeviceUniqueId", DeviceUtils.getDeviceId(context));
                if (DataMembers.ACTIVATION_KEY != null && !DataMembers.ACTIVATION_KEY.isEmpty())
                    jsonObj.put("ActivationKey", DataMembers.ACTIVATION_KEY);
                jsonObj.put(SynchronizationHelper.MOBILE_DATE_TIME,
                        Utils.getDate("yyyy/MM/dd HH:mm:ss"));
                jsonObj.put(SynchronizationHelper.MOBILE_UTC_DATE_TIME,
                        Utils.getGMTDateTime("yyyy/MM/dd HH:mm:ss"));
                jsonObj.put(SynchronizationHelper.VERSION_NAME, businessModel.getApplicationVersionName());
                if (!DataMembers.backDate.isEmpty())
                    jsonObj.put(SynchronizationHelper.REQUEST_MOBILE_DATE_TIME,
                            DateTimeUtils.now(DateTimeUtils.DATE_TIME_NEW));
                this.jsonObject = jsonObj;
            } catch (JSONException jsonException) {
                Commons.print(jsonException.getMessage());
            }
        }

        @Override
        protected String doInBackground(String... params) {

            if (isSFDC) {
                return "SFDC";
            }
            String LoginResponse = businessModel.synchronizationHelper.userInitialAuthenticate(jsonObject, isDeviceChanged);
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
            } catch (JSONException jsonException) {
                Commons.print(jsonException.getMessage());
            }
            return "E01";
        }

        @Override
        protected void onPostExecute(String output) {
            super.onPostExecute(output);
            loginView.dismissAlertDialog();
            if (isSFDC || output.equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
                new CheckNewVersionTask().execute();
            } else {
                switch (output) {
                    case "E27":
                        ((LoginContract.LoginView) loginView).showDeviceLockedDialog();
                        break;
                    case "E28":
                        businessModel.showAlert(context.getApplicationContext().getResources().
                                getString(R.string.user_already_assigned), 0);
                        break;
                    default:

                        if (output.equals("E25")) {
                            // User Account Locked
                            ((LoginContract.LoginView) loginView).showForgotPassword();
                        }

                        String ErrorMessage = businessModel.synchronizationHelper.getErrormessageByErrorCode().get(output);

                        if (ErrorMessage != null) {
                            loginView.showAlert(ErrorMessage, false);
                        } else {
                            loginView.showAlert("Connection Exception", false);
                        }
                        break;
                }


            }

        }
    }

    /*
     * Checks if updated version of the application is available */
    class CheckNewVersionTask extends AsyncTask<Integer, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                if (businessModel.isOnline()) {
                    if (!isSFDC) {
                        businessModel.synchronizationHelper.updateAuthenticateToken(false);
                        return businessModel.synchronizationHelper.checkForAutoUpdate();
                    }
                } else
                    return Boolean.FALSE;

            } catch (Exception e) {
                Commons.printException(e);
            }
            return Boolean.FALSE;
        }

        @Override
        protected void onPreExecute() {
            loginView.showProgressDialog(context.getApplicationContext().getResources().getString(R.string.checking_new_version));
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (isSFDC) {
                Commons.print("SFDC url download start");
                new UrlDownloadData().execute();  //used for sfdc purpose
            } else {
                if (businessModel.synchronizationHelper.getAuthErroCode().equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
                    if (!result) {
                        if (loginHelper.isPasswordReset(context.getApplicationContext())) {
                            loginView.dismissAlertDialog();
                            ((LoginContract.LoginView) loginView).resetPassword();
                        } else {
                            businessModel.synchronizationHelper.deleteUrlDownloadMaster();
                            new UrlMasterDownload().execute();
                        }

                    } else {
                        loginView.dismissAlertDialog();
                        ((LoginContract.LoginView) loginView).showAppUpdateAlert(context.getApplicationContext().getResources().getString(R.string.update_available));
                    }
                } else {
                    String errorMsg = businessModel.synchronizationHelper.getErrormessageByErrorCode().get(businessModel.synchronizationHelper.getAuthErroCode());
                    if (errorMsg != null) {
                        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, context.getResources().getString(R.string.data_not_downloaded), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

    }

    /**
     * UrlDownload Data class is download master mapping url from server
     * and insert into sqLite file
     */
    class UrlDownloadData extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

            businessModel.synchronizationHelper.getUrldownloadMasterSFDC(new SynchronizationHelper.VolleyResponseCallbackInterface() {
                @Override
                public String onSuccess(String result) {
                    Commons.print("SFDC URL download start 1");
                    String response = returnUrlDownloadResponse(result);//bmodel.synchronizationHelper.getUrlDownloadJson();
                    updateDeleteTableStatus(response);
                    return response;
                }

                @Override
                public String onFailure(String errorresult) {
                    String response = returnUrlDownloadResponse(errorresult);
                    updateDeleteTableStatus(response);
                    return response;
                }
            });

            return "E01";
        }

        @Override
        protected void onPostExecute(String errorCode) {
            super.onPostExecute(errorCode);
            if (!isSFDC) {
                updateDeleteTableStatus(errorCode);
            }
        }
    }


    private String returnUrlDownloadResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            Iterator itr = jsonObject.keys();
            while (itr.hasNext()) {
                String key = (String) itr.next();
                if (key.equals(SynchronizationHelper.ERROR_CODE)) {
                    String errorCode = jsonObject.getString(key);
                    if (errorCode.equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
                        businessModel.synchronizationHelper
                                .parseJSONAndInsert(jsonObject, true);
                        businessModel.synchronizationHelper.loadMasterUrlFromDB(true);

                    }
                    return errorCode;
                }
            }
        } catch (JSONException jsonExpection) {
            Commons.print(jsonExpection.getMessage());
        }
        return "E01";
    }


    private void updateDeleteTableStatus(String errorCode) {
        if (errorCode
                .equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
            deleteTables(true);
        } else {
            deleteTables(false);
            String errorMessage = businessModel.synchronizationHelper
                    .getErrormessageByErrorCode().get(errorCode);
            if (errorMessage != null) {
                loginView.showAlert(errorMessage, false);
            }
            loginView.dismissAlertDialog();
        }
    }


    public void deleteTables(boolean isDownloaded) {
        new DeleteTables(isDownloaded).execute();
    }

    private class DeleteTables extends
            AsyncTask<Integer, Integer, Integer> {

        final boolean isDownloaded;


        DeleteTables(boolean isDownloaded) {
            this.isDownloaded = isDownloaded;
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            businessModel.synchronizationHelper.deleteTables(false);
            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (isDownloaded) {
                if (businessModel.synchronizationHelper
                        .getUrlList().size() > 0) {
                    if (isSFDC) {
                        businessModel.synchronizationHelper.getAuthToken(new SynchronizationHelper.VolleyResponseCallbackInterface() {
                            @Override
                            public String onSuccess(String result) {
                                businessModel.synchronizationHelper.downloadMasterAtVolley(SynchronizationHelper.FROM_SCREEN.LOGIN, SynchronizationHelper.DownloadType.NORMAL_DOWNLOAD);
                                return "";
                            }

                            @Override
                            public String onFailure(String errorresult) {
                                return "";
                            }
                        });
                    } else {
                        businessModel.synchronizationHelper.downloadMasterAtVolley(SynchronizationHelper.FROM_SCREEN.LOGIN, SynchronizationHelper.DownloadType.NORMAL_DOWNLOAD);
                    }
                } else {
                    loginView.showAlert(context.getResources().getString(R.string.no_data_download), false);
                    loginView.dismissAlertDialog();
                }
            }
        }
    }


    /**
     * UrlDownload Data class is download master mapping url from server
     * and insert into sqLite file
     */
    class UrlMasterDownload extends AsyncTask<String, String, String> {
        JSONObject jsonObject = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!isSFDC)
                jsonObject = businessModel.synchronizationHelper.getCommonJsonObject();
        }

        @Override
        protected String doInBackground(String... params) {
            String response = businessModel.synchronizationHelper.sendPostMethod(SynchronizationHelper.URLDOWNLOAD_MASTER_APPEND_URL
                    , jsonObject);
            try {
                JSONObject jsonObject = new JSONObject(response);
                Iterator itr = jsonObject.keys();
                while (itr.hasNext()) {
                    String key = (String) itr.next();
                    if (key.equals(SynchronizationHelper.ERROR_CODE)) {
                        String errorCode = jsonObject.getString(key);
                        if (errorCode.equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
                            businessModel.synchronizationHelper
                                    .parseJSONAndInsert(jsonObject, true);
                            businessModel.synchronizationHelper.loadMasterUrlFromDB(true);
                        }
                        businessModel.synchronizationHelper.deleteTables(false);
                        return errorCode;
                    }
                }
            } catch (JSONException jsonException) {
                Commons.print(jsonException.getMessage());
            }
            businessModel.synchronizationHelper.deleteTables(false);
            return "E01";
        }

        @Override
        protected void onPostExecute(String errorCode) {
            super.onPostExecute(errorCode);
            if (errorCode
                    .equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
                if (businessModel.synchronizationHelper
                        .getUrlList().size() > 0) {
                    businessModel.synchronizationHelper.downloadMasterAtVolley(SynchronizationHelper.FROM_SCREEN.LOGIN, SynchronizationHelper.DownloadType.NORMAL_DOWNLOAD);
                } else {
                    loginView.showAlert(context.getApplicationContext().getResources().getString(R.string.no_data_download), false);
                    loginView.dismissAlertDialog();
                }
            } else {
                String errorMessage = businessModel.synchronizationHelper
                        .getErrormessageByErrorCode().get(errorCode);
                if (errorMessage != null) {
                    loginView.showAlert(errorMessage, false);
                }
                loginView.dismissAlertDialog();
            }
        }
    }

    @Override
    public void callUpdateFinish() {
        new UpdateFinish().execute();
    }

    /**
     * After download all data send acknowledge to server using this class
     */
    public class UpdateFinish extends AsyncTask<String, String, String> {
        JSONObject json = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            json = businessModel.synchronizationHelper.getCommonJsonObject();
        }

        @Override
        protected String doInBackground(String... params) {

            String response = businessModel.synchronizationHelper.sendPostMethod(SynchronizationHelper.UPDATE_FINISH_URL, json);
            try {
                JSONObject jsonObject = new JSONObject(response);
                Iterator itr = jsonObject.keys();
                while (itr.hasNext()) {
                    String key = (String) itr.next();
                    if (key.equals(SynchronizationHelper.VOLLEY_RESPONSE)) {
                        String errorCode = jsonObject.getString(key);
                        businessModel.configurationMasterHelper.isDistributorWiseDownload();
                        businessModel.configurationMasterHelper.downloadConfigForLoadLastVisit();
                        return errorCode;
                    }
                }
                return "1";
            } catch (Exception jsonException) {
                Commons.print(jsonException.getMessage());
            }
            return "1";

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            SynchronizationHelper.NEXT_METHOD next_method = businessModel.synchronizationHelper.checkNextSyncMethod();
            callNextTask(next_method);

        }
    }

    private void callNextTask(SynchronizationHelper.NEXT_METHOD response) {
        if (response == SynchronizationHelper.NEXT_METHOD.DISTRIBUTOR_DOWNLOAD) {

            businessModel.distributorMasterHelper.downloadDistributorsList();
            if (businessModel.distributorMasterHelper.getDistributors().size() > 0) {
                loginView.dismissAlertDialog();
                ((LoginContract.LoginView) loginView).goToDistributorSelection();
            } else {
                //No distributors, so downloading on demand url without distributor selection.
                downloadOnDemandMasterUrl(false);
            }

        } else if (response == SynchronizationHelper.NEXT_METHOD.NON_DISTRIBUTOR_DOWNLOAD) {
            downloadOnDemandMasterUrl(false);
        } else if (response == SynchronizationHelper.NEXT_METHOD.LAST_VISIT_TRAN_DOWNLOAD) {
            new InitiateRetailerDownload().execute();
        } else if (response == SynchronizationHelper.NEXT_METHOD.SIH_DOWNLOAD) {
            new SihDownloadTask().execute();
        } else if (response == SynchronizationHelper.NEXT_METHOD.DIGITAL_CONTENT_AVALILABLE
                || response == SynchronizationHelper.NEXT_METHOD.DEFAULT) {
            new LoadData().execute();
        }
    }

    private void downloadOnDemandMasterUrl(boolean isDistributorWise) {

        businessModel.synchronizationHelper.loadMasterUrlFromDB(false);

        if (businessModel.synchronizationHelper.getUrlList().size() > 0) {
            if (isDistributorWise) {
                businessModel.synchronizationHelper.downloadMasterAtVolley(SynchronizationHelper.FROM_SCREEN.LOGIN,
                        SynchronizationHelper.DownloadType.DISTRIBUTOR_WISE_DOWNLOAD);
            } else {
                businessModel.synchronizationHelper.downloadMasterAtVolley(SynchronizationHelper.FROM_SCREEN.LOGIN,
                        SynchronizationHelper.DownloadType.NORMAL_DOWNLOAD);
            }
        } else {
            //on demand url not available
            SynchronizationHelper.NEXT_METHOD next_method = businessModel.synchronizationHelper.checkNextSyncMethod();
            callNextTask(next_method);
        }

    }

    /**
     * Retailer wise Last visit transaction  data will be downloaded for following module
     * (Price check,Near Expiry,Stock check,Survey,promotion )if configuration enable.
     * This class is initiate retailer  wise last visit  download.we will send all
     * retailerId with userId and version code  to server.
     */
    class InitiateRetailerDownload extends AsyncTask<String, String, String> {
        JSONObject json;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {

                if (mTotalRetailerCount == 0) {
                    mTotalRetailerCount = businessModel.synchronizationHelper.getTotalRetailersCount();
                    mIterateCount = mTotalRetailerCount / SynchronizationHelper.LAST_VISIT_TRAN_SPLIT_RETAILER_COUNT;
                    final int remainder = mTotalRetailerCount % SynchronizationHelper.LAST_VISIT_TRAN_SPLIT_RETAILER_COUNT;
                    if (remainder > 0) mIterateCount = mIterateCount + 1;

                    businessModel.synchronizationHelper.setRetailerwiseTotalIterateCount(mIterateCount);
                    businessModel.synchronizationHelper.setmRetailerWiseIterateCount(mIterateCount);
                }
                final ArrayList<RetailerMasterBO> retailerIds = businessModel.synchronizationHelper.getRetailerIdsForDownloadTranSactionData(mIterateCount - 1);
                mIterateCount--;

                json = businessModel.synchronizationHelper.getCommonJsonObject();
                JSONArray jsonArray = new JSONArray();
                for (RetailerMasterBO retailerMasterBO : retailerIds) {
                    jsonArray.put(retailerMasterBO.getRetailerID());
                }
                json.put("RetailerIds", jsonArray);
            } catch (Exception jsonException) {
                Commons.print(jsonException.getMessage());
            }
        }

        @Override
        protected String doInBackground(String... params) {
            String response = businessModel.synchronizationHelper.sendPostMethod(SynchronizationHelper.INCREMENTAL_SYNC_INITIATE_URL, json);
            try {
                JSONObject jsonObject = new JSONObject(response);
                Iterator itr = jsonObject.keys();
                while (itr.hasNext()) {
                    String key = (String) itr.next();
                    if (key.equals(SynchronizationHelper.VOLLEY_RESPONSE)) {
                        return jsonObject.getString(key);
                    }
                }
                return "0";
            } catch (JSONException jsonException) {
                Commons.print(jsonException.getMessage());
            }
            return "0";
        }

        @Override
        protected void onPostExecute(String errorCode) {
            super.onPostExecute(errorCode);
            if (errorCode.equals("1")) {
                businessModel.synchronizationHelper.downloadTransactionUrl();
                if (businessModel.synchronizationHelper.getUrlList() != null && businessModel.synchronizationHelper.getUrlList().size() > 0) {
                    businessModel.synchronizationHelper.downloadLastVisitTranAtVolley(SynchronizationHelper.FROM_SCREEN.LOGIN, 1);
                } else {
                    businessModel.synchronizationHelper.isLastVisitTranDownloadDone = true;
                    SynchronizationHelper.NEXT_METHOD next_method = businessModel.synchronizationHelper.checkNextSyncMethod();
                    callNextTask(next_method);
                }
            } else {
                businessModel.synchronizationHelper.isLastVisitTranDownloadDone = true;
                SynchronizationHelper.NEXT_METHOD next_method = businessModel.synchronizationHelper.checkNextSyncMethod();
                callNextTask(next_method);
            }
        }
    }

    /**
     * download stock from stockInHandMaster web api
     */
    class SihDownloadTask extends AsyncTask<String, String, String> {
        JSONObject json = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            json = businessModel.synchronizationHelper.getCommonJsonObject();
        }

        @Override
        protected String doInBackground(String... params) {
            String responseCode = "E01";
            String response = businessModel.synchronizationHelper.sendPostMethod(businessModel.synchronizationHelper.getSIHUrl(), json);
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray jsonArray = jsonObject.getJSONArray(SynchronizationHelper.JSON_KEY);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject value = (JSONObject) jsonArray.get(i);
                    Iterator itr = value.keys();
                    while (itr.hasNext()) {
                        String key = (String) itr.next();
                        if (key.equals(SynchronizationHelper.ERROR_CODE)) {
                            String errorCode = value.getString(key);
                            if (errorCode.equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
                                businessModel.synchronizationHelper
                                        .parseJSONAndInsert(value, true);

                            }
                            responseCode = errorCode;
                        }
                    }
                }
            } catch (JSONException jsonException) {
                Commons.print(jsonException.getMessage());
            }
            return responseCode;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            SynchronizationHelper.NEXT_METHOD next_method = businessModel.synchronizationHelper.checkNextSyncMethod();
            callNextTask(next_method);
        }
    }

    /**
     * After download all data from server using this method to  update data from temporary table to
     * main table and load data from sqLite and update in objects
     */
    class LoadData extends AsyncTask<String, String, SynchronizationHelper.NEXT_METHOD> {


        @Override
        protected SynchronizationHelper.NEXT_METHOD doInBackground(String... params) {
            SynchronizationHelper.NEXT_METHOD next_method = businessModel.synchronizationHelper.checkNextSyncMethod();
            if (next_method == SynchronizationHelper.NEXT_METHOD.DIGITAL_CONTENT_AVALILABLE || next_method == SynchronizationHelper.NEXT_METHOD.DEFAULT) {
                final long startTime = System.nanoTime();
                businessModel.synchronizationHelper
                        .updateProductAndRetailerMaster();
                businessModel.synchronizationHelper.loadMethodsNew();
                long endTime = (System.nanoTime() - startTime) / 1000000;
                businessModel.synchronizationHelper.mTableList.put("temp table update**", endTime + "");
            }
            return next_method;
        }

        @Override
        protected void onPostExecute(SynchronizationHelper.NEXT_METHOD response) {
            super.onPostExecute(response);
            loginView.dismissAlertDialog();
            if (response == SynchronizationHelper.NEXT_METHOD.DIGITAL_CONTENT_AVALILABLE) {

                if (businessModel.configurationMasterHelper.IS_AZURE_CLOUD_STORAGE) {
                    AzureConnectionHelper.getInstance().setAzureCredentials(context);
                    try {
                        loginView.downloadImagesThreadStartFromAzure(businessModel.getDigitalContentURLS(), AzureConnectionHelper.getInstance().initializeAzureStorageConnection());

                    } catch (Exception e) {
                        Commons.printException(e);
                        loginView.showAlert(context.getString(R.string.error_message_general), false);
                    }
                } else if (businessModel.configurationMasterHelper.IS_S3_CLOUD_STORAGE) {
                    AWSConnectionHelper.getInstance().setAmazonS3Credentials(context);
                    transferUtility = new TransferUtility(AWSConnectionHelper.getInstance().getS3Connection(), context);
                    loginView.downloadImagesThreadStart(businessModel.getDigitalContentURLS(), transferUtility);
                }
            } else {

                if (businessModel.configurationMasterHelper.IS_CATALOG_IMG_DOWNLOAD) {
                    CatalogImageDownloadProvider.getInstance(businessModel).callCatalogImageDownload();
                }
                checkLogin();
                loginView.finishActivity();
            }
        }
    }

    public void callDistributorDownload() {
        new InitiateDistributorDownload().execute();
    }

    /**
     * Distributor wise master will be downloaded if configuration enable.
     * This class is initiate distributor wise master download.we will send all
     * distributor id with userId and version code  to server.
     */
    class InitiateDistributorDownload extends AsyncTask<String, String, String> {
        JSONObject json;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {

                loginView.showProgressDialog(context.getApplicationContext().getResources().getString(R.string.loading));

                ArrayList<DistributorMasterBO> distributorList = businessModel.distributorMasterHelper.getDistributors();
                json = businessModel.synchronizationHelper.getCommonJsonObject();
                JSONArray jsonArray = new JSONArray();
                for (DistributorMasterBO distributorBO : distributorList) {
                    if (distributorBO.isChecked()) {
                        jsonArray.put(distributorBO.getDId());

                        //update distributorId in userMaster
                        businessModel.userMasterHelper.updateDistributorId(distributorBO.getDId(), distributorBO.getParentID(), distributorBO.getDName());
                    }
                }
                json.put("DistributorIds", jsonArray);
            } catch (Exception jsonException) {
                Commons.print(jsonException.getMessage());
            }
        }

        @Override
        protected String doInBackground(String... params) {
            String response = businessModel.synchronizationHelper.sendPostMethod(SynchronizationHelper.INCREMENTAL_SYNC_INITIATE_URL, json);
            try {
                JSONObject jsonObject = new JSONObject(response);
                Iterator itr = jsonObject.keys();
                while (itr.hasNext()) {
                    String key = (String) itr.next();
                    if (key.equals(SynchronizationHelper.VOLLEY_RESPONSE)) {
                        return jsonObject.getString(key);
                    }
                }
                return "0";
            } catch (JSONException jsonException) {
                Commons.print(jsonException.getMessage());
            }
            return "0";
        }

        @Override
        protected void onPostExecute(String errorCode) {
            super.onPostExecute(errorCode);
            if (errorCode.equals("1")) {
                if (isSFDC) {
                    businessModel.synchronizationHelper.getAuthToken(new SynchronizationHelper.VolleyResponseCallbackInterface() {
                        @Override
                        public String onSuccess(String result) {
                            businessModel.synchronizationHelper.downloadMasterAtVolley(SynchronizationHelper.FROM_SCREEN.LOGIN, SynchronizationHelper.DownloadType.DISTRIBUTOR_WISE_DOWNLOAD);
                            return "";
                        }

                        @Override
                        public String onFailure(String errorresult) {
                            return "";
                        }
                    });
                } else {
                    downloadOnDemandMasterUrl(true);
                }
            }
        }
    }


    /**
     * After download all distributor wise data send acknowledge to server using this class
     */
    class UpdateDistributorFinish extends AsyncTask<String, String, String> {
        JSONObject json = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                json = businessModel.synchronizationHelper.getCommonJsonObject();
            } catch (Exception jsonException) {
                Commons.print(jsonException.getMessage());
            }
        }

        @Override
        protected String doInBackground(String... params) {
            String response = businessModel.synchronizationHelper.sendPostMethod(SynchronizationHelper.UPDATE_FINISH_URL, json);
            try {
                JSONObject jsonObject = new JSONObject(response);
                Iterator itr = jsonObject.keys();
                while (itr.hasNext()) {
                    String key = (String) itr.next();
                    if (key.equals(SynchronizationHelper.VOLLEY_RESPONSE)) {
                        return jsonObject.getString(key);
                    }
                }
                return "1";
            } catch (Exception jsonException) {
                Commons.print(jsonException.getMessage());
            }
            return "1";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            SynchronizationHelper.NEXT_METHOD next_method = businessModel.synchronizationHelper.checkNextSyncMethod();
            callNextTask(next_method);

        }
    }

    public void callDistributorFinish() {
        new UpdateDistributorFinish().execute();
    }

    public void callRetailerFinish() {
        new UpdateRetailerFinish().execute();
    }

    /**
     * After download all retailer wise last visit data send acknowledge to server using this class
     */
    class UpdateRetailerFinish extends AsyncTask<String, String, String> {
        JSONObject json = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                json = businessModel.synchronizationHelper.getCommonJsonObject();
            } catch (Exception jsonException) {
                Commons.print(jsonException.getMessage());
            }
        }

        @Override
        protected String doInBackground(String... params) {
            String response = businessModel.synchronizationHelper.sendPostMethod(SynchronizationHelper.UPDATE_FINISH_URL, json);
            try {
                JSONObject jsonObject = new JSONObject(response);
                Iterator itr = jsonObject.keys();
                while (itr.hasNext()) {
                    String key = (String) itr.next();
                    if (key.equals(SynchronizationHelper.VOLLEY_RESPONSE)) {
                        return jsonObject.getString(key);
                    }
                }
                return "1";
            } catch (Exception jsonException) {
                Commons.print(jsonException.getMessage());
            }
            return "1";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (mIterateCount <= 0) {
                SynchronizationHelper.NEXT_METHOD next_method = businessModel.synchronizationHelper.checkNextSyncMethod();
                callNextTask(next_method);
            } else {
                new InitiateRetailerDownload().execute();
            }
        }
    }

    public void applyOutletPerformancePref() {
        //outlet Performance
        if (OutletPerfomanceHelper.getInstance(context).getPerformRptUrl().length() > 0) {
            SharedPreferences.Editor editor = PreferenceManager
                    .getDefaultSharedPreferences(context.getApplicationContext())
                    .edit();
            editor.putString("rpt_dwntime",
                    DateTimeUtils.now(DateTimeUtils.DATE_TIME_NEW));
            editor.apply();
        }
    }

    class ForgetPassword extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            return businessModel.synchronizationHelper.updateAuthenticateTokenWithoutPassword("");
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (!s.equals("")) {
                ((LoginContract.LoginView) loginView).callResetPassword();
            } else {
                loginView.showAlert(context.getApplicationContext().getResources().getString(R.string.token_expired), false);
            }
        }
    }

    public void callForgetPassword() {
        new ForgetPassword().execute();
    }
}
