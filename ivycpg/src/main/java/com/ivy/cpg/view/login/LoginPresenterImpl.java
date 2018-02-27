package com.ivy.cpg.view.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferType;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.ivy.cpg.primarysale.bo.DistributorMasterBO;
import com.ivy.cpg.view.sync.catalogdownload.CatalogImageDownloadProvider;
import com.ivy.lib.Utils;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.ApplicationConfigs;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.AttendanceHelper;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.DateUtil;

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

public class LoginPresenterImpl implements LoginContractor.LoginPresenter {
    private final Context context;
    private final BusinessModel businessModel;
    private LoginContractor.LoginView loginView;
    private final LoginHelper loginHelper;
    private boolean syncDone;
    public SharedPreferences mPasswordLockCountPref;
    private SharedPreferences mLastSyncSharedPref;
    private int mIterateCount = 0;
    private TransferUtility transferUtility;
    private String initialLanguage = "en";
    private SharedPreferences sharedPrefs;

    LoginPresenterImpl(Context context) {
        this.context = context;
        businessModel = (BusinessModel) context.getApplicationContext();
        loginHelper = LoginHelper.getInstance(context);

    }

    @Override
    public void setView(LoginContractor.LoginView loginView) {
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

            if (loginHelper.SHOW_FORGET_PASSWORD) {
                loginView.showForgotPassword();
            }
        }
        businessModel.synchronizationHelper.loadErrorCode();
        /* Set default language */
        sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(context);

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

        reloadActivity();

        mLastSyncSharedPref = context.getSharedPreferences("lastSync", MODE_PRIVATE);
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
        edt.putString("date", DateUtil.convertFromServerDateToRequestedFormat(
                SDUtil.now(SDUtil.DATE_GLOBAL),
                ConfigurationMasterHelper.outDateFormat));
        edt.putString("time", SDUtil.now(SDUtil.TIME));
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
        loginView.setSupportNoTV(loginHelper.getSupportNo(context));
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
                    e.printStackTrace();
                }
            }
        } else {
            loginView.showAlert(context.getResources().getString(R.string.external_storage_not_avail), true);
        }
    }

    /*
    * Async task to restore the DB from external storage
    * */
    class RestoreDB extends AsyncTask<Integer, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            loginView.showProgressDialog(context.getResources().getString(R.string.Restoring_database));
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            return loginHelper.reStoreDB(context);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            loginView.dismissAlertDialog();
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

    /*
    * Assigns server url from shared preference
    * */
    @Override
    public void assignServerUrl() {
        if (ApplicationConfigs.withActivation) {
            DataMembers.SERVER_URL = PreferenceManager
                    .getDefaultSharedPreferences(context).getString("appUrlNew", "");
            DataMembers.ACTIVATION_KEY = PreferenceManager
                    .getDefaultSharedPreferences(context).getString("activationKey", "");
        }

         /* Display application Phase if the environment is other than live.*/
        String phase = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("application", "");
        if (!phase.equals(""))
            if (Pattern.compile(Pattern.quote("ivy"), Pattern.CASE_INSENSITIVE)
                    .matcher(phase).find()) {
                businessModel.synchronizationHelper.isInternalActivation = true;
            }
    }


    public void checkLogin() {
        if (loginHelper.SHOW_CHANGE_PASSWORD) {
            String createdDate = loginHelper.getPasswordCreatedDate(context);
            if (createdDate != null && !createdDate.equals("")) {
                int result = SDUtil.compareDate(loginHelper.getPasswordExpiryDate(createdDate),
                        businessModel.userMasterHelper.getUserMasterBO().getDownloadDate(), "yyyy/MM/dd");
                if (result == -1) {
                    loginView.goToChangePwd();
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
        loginHelper.loadPasswordConfiguration(context);
        businessModel.userMasterHelper.downloadDistributionDetails();
        if (loginHelper.IS_PASSWORD_ENCRYPTED)
            businessModel.synchronizationHelper.setEncryptType();
        if (businessModel.configurationMasterHelper.SHOW_ATTENDANCE) {
            if (AttendanceHelper.getInstance(context).loadAttendanceMaster()) {
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
                    loginView.showAlert(context.getResources().getString(R.string.download_url_empty), false);
                    loginView.dismissAlertDialog();
                }
            } else {
                loginView.showAlert(context.getResources().getString(R.string.please_connect_to_internet), false);
            }
        }
    }

    private void proceedToLocalLogin() {
        loginView.showProgressDialog(context.getResources().getString(R.string.loading_data));

        //handle password lock in offline based on reached maximum_attempt_count compare with mPasswordLockCountPref count
        int count = mPasswordLockCountPref.getInt("passwordlock", 0);
        if (count + 1 == loginHelper.MAXIMUM_ATTEMPT_COUNT)
            loginView.sendUserNotExistToHandler();
        else
            loginView.threadActions();
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
            loginView.showProgressDialog(context.getResources().getString(R.string.checking_time));
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
            if (result == 2) {
                loginView.dismissAlertDialog();
                loginView.showAlert(context.getResources().getString(R.string.error_e24), true); // error_24 invalid date time
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
                        businessModel.activationHelper.getIMEINumber());
                jsonObj.put("RegistrationId", businessModel.regid);
                jsonObj.put("DeviceUniqueId", businessModel.activationHelper.getDeviceId());
                if (DataMembers.ACTIVATION_KEY != null && !DataMembers.ACTIVATION_KEY.isEmpty())
                    jsonObj.put("ActivationKey", DataMembers.ACTIVATION_KEY);
                jsonObj.put(SynchronizationHelper.MOBILE_DATE_TIME,
                        Utils.getDate("yyyy/MM/dd HH:mm:ss"));
                jsonObj.put(SynchronizationHelper.MOBILE_UTC_DATE_TIME,
                        Utils.getGMTDateTime("yyyy/MM/dd HH:mm:ss"));
                jsonObj.put(SynchronizationHelper.VERSION_NAME,businessModel.getApplicationVersionName());
                if (!DataMembers.backDate.isEmpty())
                    jsonObj.put(SynchronizationHelper.REQUEST_MOBILE_DATE_TIME,
                            SDUtil.now(SDUtil.DATE_TIME_NEW));
                this.jsonObject = jsonObj;
            } catch (JSONException jsonException) {
                Commons.print(jsonException.getMessage());
            }
        }

        @Override
        protected String doInBackground(String... params) {
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
            if (output.equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
                new CheckNewVersionTask().execute();
            } else {
                if (output.equals("E27")) {
                    loginView.showDeviceLockedDialog();
                } else {

                    if (output.equals("E25")) {
                        // User Account Locked
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

    /*
    * Checks if updated version of the application is available */
    class CheckNewVersionTask extends AsyncTask<Integer, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                if (businessModel.isOnline()) {
                    businessModel.synchronizationHelper.updateAuthenticateToken();
                    return businessModel.synchronizationHelper.checkForAutoUpdate();
                } else
                    return Boolean.FALSE;

            } catch (Exception e) {
                Commons.printException(e);
            }
            return Boolean.FALSE;
        }

        @Override
        protected void onPreExecute() {
            loginView.showProgressDialog(context.getResources().getString(R.string.checking_new_version));
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (businessModel.synchronizationHelper.getAuthErroCode().equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
                if (!result) {
                    if (loginHelper.isPasswordReset(context)) {
                        loginView.dismissAlertDialog();
                        loginView.resetPassword();
                    } else {
                        businessModel.synchronizationHelper.deleteUrlDownloadMaster();
                        new UrlMasterDownload().execute();
                    }

                } else {
                    loginView.dismissAlertDialog();
                    loginView.showAppUpdateAlert(context.getResources().getString(R.string.update_available));
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

    /**
     * UrlDownload Data class is download master mapping url from server
     * and insert into sqLite file
     */
    class UrlMasterDownload extends AsyncTask<String, String, String> {
        JSONObject jsonObject = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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
                    loginView.showAlert(context.getResources().getString(R.string.no_data_download), false);
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
                loginView.goToDistributorSelection();
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
            int mTotalRetailerCount;
            try {

                //if (mTotalRetailerCount == 0) {
                mTotalRetailerCount = businessModel.synchronizationHelper.getTotalRetailersCount();
                mIterateCount = mTotalRetailerCount / SynchronizationHelper.LAST_VISIT_TRAN_SPLIT_RETAILER_COUNT;
                final int remainder = mTotalRetailerCount % SynchronizationHelper.LAST_VISIT_TRAN_SPLIT_RETAILER_COUNT;
                if (remainder > 0) mIterateCount = mIterateCount + 1;

                businessModel.synchronizationHelper.setRetailerwiseTotalIterateCount(mIterateCount);
                businessModel.synchronizationHelper.setmRetailerWiseIterateCount(mIterateCount);
                //}
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
            String response = businessModel.synchronizationHelper.sendPostMethod(businessModel.synchronizationHelper.getSIHUrl(), json);
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
                businessModel.configurationMasterHelper.setAmazonS3Credentials();
                initializeTransferUtility();
                downloadDigitalContents();
            } else {

                if (businessModel.configurationMasterHelper.IS_CATALOG_IMG_DOWNLOAD) {
                        CatalogImageDownloadProvider.getInstance(businessModel).callCatalogImageDownload();
                }
                checkLogin();
                loginView.finishActivity();
            }
        }
    }

    public void downloadDigitalContents() {
        loginView.downloadImagesThreadStart(businessModel.getDigitalContentURLS(), transferUtility);
    }

    public void initializeTransferUtility() {
        BasicAWSCredentials myCredentials = new BasicAWSCredentials(ConfigurationMasterHelper.ACCESS_KEY_ID,
                ConfigurationMasterHelper.SECRET_KEY);
        AmazonS3Client s3 = new AmazonS3Client(myCredentials);
        transferUtility = new TransferUtility(s3, context);
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

                loginView.showProgressDialog(context.getResources().getString(R.string.loading));

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
                downloadOnDemandMasterUrl(true);
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
        if (businessModel.reportHelper.getPerformRptUrl().length() > 0) {
            SharedPreferences.Editor editor = PreferenceManager
                    .getDefaultSharedPreferences(context)
                    .edit();
            editor.putString("rpt_dwntime",
                    SDUtil.now(SDUtil.DATE_TIME_NEW));
            editor.apply();
        }
    }

    class ForgetPassword extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            return businessModel.synchronizationHelper.updateAuthenticateTokenWithoutPassword();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (!s.equals("")) {
                loginView.callResetPassword();
            } else {
                loginView.showAlert(context.getResources().getString(R.string.token_expired), false);
            }
        }
    }

    public void callForgetPassword() {
        new ForgetPassword().execute();
    }
}
