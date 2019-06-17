package com.ivy.cpg.view.login;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.FileProvider;
import android.view.WindowManager;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.ivy.apptutoriallibrary.AppTutorialPlugin;
import com.ivy.cpg.view.attendance.AttendanceActivity;
import com.ivy.cpg.view.sync.UploadHelper;
import com.ivy.cpg.view.sync.catalogdownload.CatalogImageDownloadProvider;
import com.ivy.cpg.view.sync.largefiledownload.FileDownloadProvider;
import com.ivy.sd.png.asean.view.BuildConfig;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.DownloaderThreadNew;
import com.ivy.sd.png.model.MyThread;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.DistributorSelectionActivity;
import com.ivy.cpg.view.homescreen.HomeScreenActivity;
import com.ivy.sd.png.view.PasswordLockDialogFragment;
import com.ivy.utils.FileUtils;
import com.microsoft.azure.storage.blob.CloudBlobContainer;

import org.json.JSONArray;

import java.io.File;
import java.util.HashMap;
import java.util.UnknownFormatConversionException;

public abstract class LoginBaseActivity extends IvyBaseActivityNoActionBar implements LoginContract.LoginBaseView {

    private BusinessModel businessModel;

    private AlertDialog alertDialog;

    public LoginPresenterImpl loginPresenter;

    private MyReceiver receiver;

    private ProgressDialog progressDialog;

    public abstract void initPresenter();
    public abstract void setDefaults(boolean clearUserName);
    public abstract void handleForgotPassword();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        businessModel = (BusinessModel) getApplicationContext();
        initPresenter();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public void init(){
        loginPresenter.loadInitialData();
        loginPresenter.checkDB();


        /* Register receiver to receive download status. */
        IntentFilter filter = new IntentFilter(MyReceiver.PROCESS_RESPONSE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new MyReceiver();
        registerReceiver(receiver, filter);

        loginPresenter.assignServerUrl();
    }

    @Override
    protected void onResume() {
        super.onResume();
        businessModel = (BusinessModel) getApplicationContext();
        businessModel.setContext(this);



    }

    @Override
    public void showAlert(String msg, boolean isFinish) {
        businessModel.showAlert(msg, 0);
        if (isFinish) {
            finish();
        }
    }

    @Override
    public void showProgressDialog(String msg) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (isDestroyed()) { // or call isFinishing() if min sdk version < 17
                return;
            }
        } else if (isFinishing()) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        customProgressDialog(builder, msg);
        alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void dismissAlertDialog() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (isDestroyed()) { // or call isFinishing() if min sdk version < 17
                return;
            }
        } else if (isFinishing()) {
            return;
        }
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
            alertDialog = null;
        }
    }

    @Override
    public void showGPSDialog() {
        new CommonDialog(getApplicationContext(), this, "", getResources().getString(R.string.enable_gps), false, getResources().getString(R.string.ok), new CommonDialog.PositiveClickListener() {
            @Override
            public void onPositiveButtonClick() {
                Intent myIntent = new Intent(
                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(myIntent);

            }
        }).show();
    }

    @Override
    public void setAlertDialogMessage(String msg) {
        if (alertDialog != null) {
            alertDialog.setMessage(msg);
        }
    }

    @Override
    public void goToHomeScreen() {

        LoginHelper loginHelper = LoginHelper.getInstance(this);
        loginHelper.downloadTermsAndConditions(this);


        if (businessModel.configurationMasterHelper.IS_SHOW_TERMS_COND && !loginHelper.isTermsAccepted()) {
            Intent intent = new Intent(this, TermsAndConditionsActivity.class);
            intent.putExtra("fromScreen", "login");
            startActivity(intent);
            overridePendingTransition(R.anim.zoom_enter, R.anim.hold);
        } else {
            Intent myIntent = new Intent(this, HomeScreenActivity.class);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(myIntent, 0);
        }

    }

    @Override
    public void goToAttendance() {
        startActivity(new Intent(this,
                AttendanceActivity.class));
    }

    @Override
    public void finishActivity() {
        if (businessModel.synchronizationHelper.checkDataForSyncLogUpload())
            new UploadSyncLog().execute();
        else
            finish();
    }

    @Override
    public void reload() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();

        overridePendingTransition(0, 0);
        startActivity(intent);
    }

    @Override
    public void requestLocation() {
        businessModel.requestLocation(this);
    }


    @Override
    public void doLocalLogin() {
        new MyThread(this, DataMembers.LOCAL_LOGIN).start();
    }


    public class MyReceiver extends BroadcastReceiver {
        public static final String PROCESS_RESPONSE = "com.ivy.intent.action.LOGIN";

        @Override
        public void onReceive(Context context, Intent intent) {
            updateReceiver(intent);
        }
    }

    private void updateReceiver(Intent intent) {
        Bundle bundle = intent.getExtras();
        int method = bundle != null ? bundle.getInt(SynchronizationHelper.SYNXC_STATUS, 0) : 0;
        String errorCode = bundle != null ? bundle.getString(SynchronizationHelper.ERROR_CODE) : "";
        int updateTableCount = bundle != null ? bundle.getInt("updateCount") : 0;
        int totalTableCount = bundle != null ? bundle.getInt("totalCount") : 0;
        switch (method) {
            case SynchronizationHelper.VOLLEY_DOWNLOAD_INSERT:
                if (errorCode != null && errorCode.equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)
                        && (totalTableCount == updateTableCount)) {
                    loginPresenter.applyOutletPerformancePref();
                    loginPresenter.callUpdateFinish();
                } else if (errorCode != null && errorCode.equals(SynchronizationHelper.UPDATE_TABLE_SUCCESS_CODE)) {
                    updateProgress(updateTableCount, totalTableCount);
                    if (totalTableCount == (updateTableCount + 1)) {
                        updaterProgressMsg(getResources().getString(R.string.updating_tables));
                        loginPresenter.applyLastSyncPref();
                        loginPresenter.updateDownloadedTime();
                    }
                } else {
                    reDownloadAlert(bundle);
                    break;
                }
                break;
            case SynchronizationHelper.DISTRIBUTOR_WISE_DOWNLOAD_INSERT:
                if (errorCode != null && errorCode.equals(SynchronizationHelper.UPDATE_TABLE_SUCCESS_CODE)) {
                    updateProgress(updateTableCount, totalTableCount);
                    if (totalTableCount == (updateTableCount + 1)) {
                        updaterProgressMsg(getResources().getString(R.string.updating_tables));
                        loginPresenter.applyLastSyncPref();
                        loginPresenter.updateDownloadedTime();
                    }
                } else if (errorCode != null && errorCode.equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
                    loginPresenter.callDistributorFinish();
                } else {
                    reDownloadAlert(bundle);
                    break;
                }
                break;
            case SynchronizationHelper.LAST_VISIT_TRAN_DOWNLOAD_INSERT:
                if (errorCode != null && errorCode.equals(SynchronizationHelper.UPDATE_TABLE_SUCCESS_CODE)) {
                    updateProgress(updateTableCount, totalTableCount);
                    if (totalTableCount == (updateTableCount + 1)) {
                        updaterProgressMsg(getResources().getString(R.string.updating_tables));
                        loginPresenter.applyLastSyncPref();
                        loginPresenter.updateDownloadedTime();
                    }
                } else if (errorCode != null && errorCode.equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
                    loginPresenter.callRetailerFinish();
                } else {
                    reDownloadAlert(bundle);
                    break;
                }
                break;
            default:
                break;
        }
    }

    private void updateProgress(int updateTableCount, int totalTableCount) {
        String formattedString = "";
        try {
            formattedString = String.format(getResources().getString(R.string.out_of), totalTableCount);
        } catch (UnknownFormatConversionException e) {
            e.printStackTrace();
        }
        updaterProgressMsg(updateTableCount + " " + formattedString);
    }


    /**
     * Server error is coming like 404 error  and IsMandatory is 1 for corresponding url delete
     * all table and show alert message to please re download
     *
     * @param bundle - bundle
     */
    private void reDownloadAlert(Bundle bundle) {
        String errorDownloadCode = bundle
                .getString(SynchronizationHelper.ERROR_CODE);
        String errorDownloadMessage = businessModel.synchronizationHelper
                .getErrormessageByErrorCode().get(errorDownloadCode);
        businessModel.synchronizationHelper.deleteTables(false);

        dismissAlertDialog();

        if (errorDownloadMessage != null) {
            showAlert(errorDownloadMessage, false);
        } else {
            showAlert(getResources().getString(R.string.please_redownload_data), false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        LoginHelper.getInstance(this).clearInstance();
        dismissAlertDialog();
        dismissCurrentProgressDialog();
    }

    @Override
    public void goToDistributorSelection() {
        Intent intent = new Intent(this, DistributorSelectionActivity.class);
        intent.putExtra("isFromLogin", true);
        startActivityForResult(intent, SynchronizationHelper.DISTRIBUTOR_SELECTION_REQUEST_CODE);
    }

    public Handler getHandler() {
        return handler;
    }


    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            businessModel = (BusinessModel) getApplicationContext();
            switch (msg.what) {
                case DataMembers.NOTIFY_USEREXIST:
                    dismissAlertDialog();
                    loginPresenter.checkLogin();

                    // This call will help to intiatiate catalog image download services.
                    CatalogImageDownloadProvider.getInstance(businessModel).checkCatalogDownload();

                    FileDownloadProvider.getInstance(businessModel).callFileDownload(getApplicationContext());

                    finish();
                    break;
                case DataMembers.NOTIFY_NOT_USEREXIST:
                    if (!LoginHelper.getInstance(getApplicationContext()).IS_PASSWORD_LOCK) {
                        dismissAlertDialog();
                        setDefaults(false);
                        showAlert(
                                getResources().getString(
                                        R.string.please_check_username_and_password), false);
                    } else {
                        int count = loginPresenter.getPasswordLockCount();
                        handleForgotPassword();
                        if (count + 1 == LoginHelper.getInstance(getApplicationContext()).MAXIMUM_ATTEMPT_COUNT) {
                            dismissAlertDialog();
                            FragmentManager fm = getSupportFragmentManager();
                            PasswordLockDialogFragment dialogFragment = new PasswordLockDialogFragment();
                            Bundle bundle = new Bundle();
                            bundle.putString("title", "Password Lock");
                            bundle.putString("textviewTitle", getResources().getString(R.string.exceed_pwd_retry_limit));
                            dialogFragment.setArguments(bundle);
                            dialogFragment.show(fm, "Sample Fragment");
                        } else {
                            loginPresenter.applyPasswordLockCountPref();
                            dismissAlertDialog();
                            setDefaults(false);
                            showAlert(
                                    getResources().getString(
                                            R.string.please_check_username_and_password), false);
                            Toast.makeText(LoginBaseActivity.this, "Remaining Password Count " + (LoginHelper.getInstance(getApplicationContext()).MAXIMUM_ATTEMPT_COUNT - (count + 1)), Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                case DataMembers.NOTIFY_UPDATE:
                    showProgressDialog(msg.obj.toString());
                    break;
                case DataMembers.NOTIFY_CONNECTION_PROBLEM:
                    dismissAlertDialog();
                    showAlert(getResources().getString(R.string.no_network_connection), false);
                    break;

                default:
                    break;
            }

        }
    };



    /**
     * This is the Handler for this activity. It will receive messages from the
     * ApkDownloaderThread and make the necessary updates to the UI.
     */
    @SuppressLint("HandlerLeak")
    public final Handler fileDownloadHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                /*
                 * Handling MESSAGE_UPDATE_PROGRESS_BAR: 1. Get the current
                 * progress, as indicated in the arg1 field of the Message. 2.
                 * Update the progress bar.
                 */
                case DataMembers.MESSAGE_UPDATE_PROGRESS_BAR:
                    if (progressDialog != null) {
                        int currentProgress = msg.arg1;
                        progressDialog.setProgress(currentProgress);
                    }
                    break;
                /*
                 * Handling MESSAGE_CONNECTING_STARTED: 1. Get the URL of the file
                 * being downloaded. This is stored in the obj field of the Message.
                 * 2. Create an indeterminate progress bar. 3. Set the message that
                 * should be sent if user cancels. 4. Show the progress bar.
                 */
                case DataMembers.MESSAGE_CONNECTING_STARTED:
                    if (msg.obj != null && msg.obj instanceof String) {
                        String url = (String) msg.obj;
                        // truncate the url
                        if (url.length() > 16) {
                            String tUrl = url.substring(0, 15);
                            tUrl += "...";
                            url = tUrl;
                        }

                        dismissCurrentProgressDialog();


                        callProgressDialog(getApplicationContext()
                                .getString(R.string.progress_dialog_title_connecting), getApplicationContext()
                                .getString(R.string.progress_dialog_message_prefix_connecting) + " " + url, 0, null, false);
                    }
                    break;
                /*
                 * Handling MESSAGE_DOWNLOAD_STARTED: 1. Create a progress bar with
                 * specified max value and current value 0; assign it to
                 * progressDialog. The arg1 field will contain the max value. 2. Set
                 * the title and text for the progress bar. The obj field of the
                 * Message will contain a String that represents the name of the
                 * file being downloaded. 3. Set the message that should be sent if
                 * dialog is canceled. 4. Make the progress bar visible.
                 */
                case DataMembers.MESSAGE_DOWNLOAD_STARTED:
                    // obj will contain a String representing the file name
                    if (msg.obj != null && msg.obj instanceof String) {
                        String fileName = (String) msg.obj;

                        dismissCurrentProgressDialog();
                        callProgressDialog(getApplicationContext()
                                        .getString(R.string.progress_dialog_title_downloading), getApplicationContext()
                                        .getString(R.string.progress_dialog_message_prefix_downloading) + " " + fileName,
                                msg.arg1, null, true);
                    }
                    break;

                /*
                 * Handling MESSAGE_APK_DOWNLOAD_COMPLETE: 1. Remove the progress bar
                 * from the screen. 2. Display Toast that says download is complete.
                 */
                case DataMembers.MESSAGE_APK_DOWNLOAD_COMPLETE:

                    dismissCurrentProgressDialog();

                    LoginHelper.getInstance(LoginBaseActivity.this).deleteAllValues(getApplicationContext());
                    clearAppUrl();
                    businessModel.userMasterHelper.getUserMasterBO().setUserid(0);
                    businessModel.codeCleanUpUtil.setUserId(0);
                    try {
                        Uri path;
                        if (Build.VERSION.SDK_INT >= 24) {
                            path = FileProvider.getUriForFile(LoginBaseActivity.this, BuildConfig.APPLICATION_ID + ".provider", new File(
                                    getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                                            + "/" + DataMembers.fileName));
                            Intent sintent = ShareCompat.IntentBuilder.from(LoginBaseActivity.this)
                                    .setStream(path) // uri from FileProvider
                                    .getIntent()
                                    .setAction(Intent.ACTION_VIEW)
                                    .setDataAndType(path, "application/vnd.android.package-archive")
                                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                            startActivity(sintent);

                        } else {
                            Intent intent = new Intent(Intent.ACTION_VIEW);

                            path = Uri.fromFile(new File(
                                    getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                                            + "/" + DataMembers.fileName));
                            intent.setDataAndType(path, "application/vnd.android.package-archive");
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }

                    } catch (Exception e) {
                        Commons.printException(e);
                    }

                    break;

                case DataMembers.MESSAGE_DOWNLOAD_COMPLETE_DC:
                    dismissCurrentProgressDialog();

                    FileDownloadProvider.getInstance(businessModel).callFileDownload(getApplicationContext());
                    if (businessModel.configurationMasterHelper.IS_CATALOG_IMG_DOWNLOAD)
                        CatalogImageDownloadProvider.getInstance(businessModel).callCatalogImageDownload();

                    String appTutorialAPi = businessModel.synchronizationHelper.downloadAppTutorialURL();
                    if(!appTutorialAPi.equals("")){
                        new DownloadAppTutorial().execute();
                    }
                    else {
                        finishActivity();
                        loginPresenter.checkLogin();
                    }


                    break;



                /*
                 * Handling MESSAGE_ENCOUNTERED_ERROR_APK: 1. Check the obj field of the
                 * message for the actual error message that will be displayed to
                 * the user. 2. Remove any progress bars from the screen. 3. Display
                 * a Toast with the error message.
                 */
                case DataMembers.MESSAGE_ENCOUNTERED_ERROR_APK:
                    // obj will contain a string representing the error message
                    if (msg.obj != null && msg.obj instanceof String) {
                        String errorMessage = (String) msg.obj;
                        dismissCurrentProgressDialog();
                        showAlert(errorMessage, false);
                    }

                    try {
                        LoginHelper.getInstance(LoginBaseActivity.this).deleteUserMaster(getApplicationContext());
                        startActivity(new Intent(LoginBaseActivity.this, LoginScreen.class));
                        finish();
                        break;
                    } catch (Exception e) {
                        Commons.printException(e);
                    }


                    break;

                case DataMembers.MESSAGE_ENCOUNTERED_ERROR_DC:
                    // obj will contain a string representing the error message
                    if (msg.obj != null && msg.obj instanceof String) {
                        String errorMessage = (String) msg.obj;
                        dismissCurrentProgressDialog();
                        showAlert(errorMessage, false);
                    }

                    FileDownloadProvider.getInstance(businessModel).callFileDownload(getApplicationContext());

                    if (businessModel.configurationMasterHelper.IS_CATALOG_IMG_DOWNLOAD)
                        CatalogImageDownloadProvider.getInstance(businessModel).callCatalogImageDownload();

                    finishActivity();
                    loginPresenter.checkLogin();


                    break;

                case DataMembers.THIRD_PARTY_INSTALLATION_ERROR:
                    // obj will contain a string representing the error message
                    if (msg.obj != null && msg.obj instanceof String) {
                        String errorMessage = (String) msg.obj;
                        dismissCurrentProgressDialog();
                        showAlert(errorMessage, false);
                    }
                    break;

                case DataMembers.SDCARD_NOT_AVAILABLE:
                    // obj will contain a string representing the error message
                    if (msg.obj != null && msg.obj instanceof String) {
                        String errorMessage = (String) msg.obj;
                        dismissCurrentProgressDialog();
                        showAlert(errorMessage, false);
                    }
                    loginPresenter.checkLogin();
                    finish();
                    break;

                default:
                    // nothing to do here
                    break;
            }
        }
    };

    /**
     * If there is a progress dialog, dismiss it and set progressDialog to null.
     */
    private void dismissCurrentProgressDialog() {
        if (progressDialog != null) {
            progressDialog.hide();
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @Override
    public void downloadImagesThreadStart(HashMap<String, String> imgUrls, TransferUtility transferUtility, HashMap<String, String> sfdcImgUrls) {
        Thread downloaderThread = new DownloaderThreadNew(LoginBaseActivity.this,
                fileDownloadHandler, imgUrls,
                businessModel.userMasterHelper.getUserMasterBO()
                        .getUserid(), transferUtility,sfdcImgUrls);
        downloaderThread.start();
    }

    @Override
    public void downloadImagesThreadStartFromAzure(HashMap<String, String> imgUrls, CloudBlobContainer cloudBlobContainer, HashMap<String, String> sfdcImgUrls) {
        Thread downloadThread = new DownloaderThreadNew(LoginBaseActivity.this,fileDownloadHandler,imgUrls,
                businessModel.userMasterHelper.getUserMasterBO().getUserid(),
                cloudBlobContainer,sfdcImgUrls);
        downloadThread.start();
    }

  
    private void callProgressDialog(String title, String message, int maxValue, Message newMsg, boolean isHorizontalStyle) {
        progressDialog = new ProgressDialog(LoginBaseActivity.this);
        progressDialog.setTitle(title);
        progressDialog.setMessage(message);

        if (isHorizontalStyle) {
            progressDialog
                    .setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setProgress(0);
            progressDialog.setMax(maxValue);
        } else {
            progressDialog
                    .setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setIndeterminate(true);
        }
        // set the message to be sent when this dialog is canceled
        //progressDialog.setCancelMessage(newMsg);
        // Don't allow cancellation
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    class UploadSyncLog extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog("Uploading Sync Log Details");
        }

        @Override
        protected Void doInBackground(Void... params) {
            UploadHelper mUploadHelper = UploadHelper.getInstance(LoginBaseActivity.this);
            mUploadHelper.uploadSyncLogDetails();
            return null;
        }

        @Override
        protected void onPostExecute(Void s) {
            super.onPostExecute(s);
            dismissAlertDialog();
            finish();
        }
    }

    class DownloadAppTutorial extends AsyncTask<Integer, Integer, Integer> {


        protected void onPreExecute() {
            showProgressDialog(getResources().getString(R.string.downloading_app_tutorial));
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            try {
                businessModel.synchronizationHelper.updateAuthenticateToken(false);
                if (businessModel.synchronizationHelper.getAuthErroCode().equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
                    String appTutorialAPi = businessModel.synchronizationHelper.downloadAppTutorialURL();
                    if (!appTutorialAPi.equals("")) {
                       String tutorialJSON= businessModel.synchronizationHelper.downLoadAppTutorial(LoginBaseActivity.this);
                       businessModel.writeToFile(tutorialJSON,DataMembers.APP_TUTORIAL,"/"+DataMembers.APP_TUTORIAL , getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)+"");

                            String data = FileUtils.readFile(LoginBaseActivity.this, DataMembers.APP_TUTORIAL + ".txt", DataMembers.APP_TUTORIAL, getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "");
                            JSONArray jsonArray = new JSONArray(data);
                            AppTutorialPlugin.getInstance().setAppTutorialJsonArray(jsonArray);

                    }

                }
                else {
                    return -1;
                }

            } catch (Exception e) {
                Commons.printException("" + e);
                return -1;
            }
            return 0;
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        @Override
        protected void onPostExecute(Integer status) {
            super.onPostExecute(status);
            dismissAlertDialog();
            if(status==-1){
                Toast.makeText(LoginBaseActivity.this, getResources().getString(R.string.error_in_downloading_tutorial), Toast.LENGTH_SHORT).show();
            }

            finishActivity();
            loginPresenter.checkLogin();

        }
    }


}
