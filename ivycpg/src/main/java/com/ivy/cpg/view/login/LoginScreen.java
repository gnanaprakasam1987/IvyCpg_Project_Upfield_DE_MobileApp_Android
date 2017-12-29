package com.ivy.cpg.view.login;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.FileProvider;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.ivy.sd.png.asean.view.BuildConfig;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.ApplicationConfigs;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.DownloaderThread;
import com.ivy.sd.png.model.DownloaderThreadNew;
import com.ivy.sd.png.model.MyThread;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.About;
import com.ivy.sd.png.view.AttendanceActivity;
import com.ivy.sd.png.view.ChangePasswordActivity;
import com.ivy.sd.png.view.DistributorSelectionActivity;
import com.ivy.sd.png.view.PasswordLockDialogFragment;
import com.ivy.sd.png.view.ResetPasswordDialog;
import com.ivy.sd.png.view.UserSettingsActivity;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;



public class LoginScreen extends IvyBaseActivityNoActionBar implements ApplicationConfigs, LoginContractor.LoginView {

    private BusinessModel businessModel;

    private EditText editTextUserName, editTextPassword;

    private TextView mForgotPasswordTV;

    private AlertDialog alertDialog;

    private ProgressDialog progressDialog;

    private MyReceiver receiver;

    public LoginPresenterImpl loginPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        businessModel = (BusinessModel) getApplicationContext();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.loginscreen);

        /* Check the date if expiry is enabled.*/
        if (ApplicationConfigs.expiryEnable) {
            if ((SDUtil.compareDate(ApplicationConfigs.expiryDate,
                    SDUtil.now(SDUtil.DATE_GLOBAL), "yyyy/MM/dd") < 0))
                finish();
        } else {

            loginPresenter = new LoginPresenterImpl(getApplicationContext());
            loginPresenter.setView(this);

            loginPresenter.loadInitialData();

            //progressDialog = null;

            mForgotPasswordTV = (TextView) findViewById(R.id.txtResetPassword);
            mForgotPasswordTV.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

            editTextUserName = (EditText) findViewById(R.id.EditText011);
            editTextUserName.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

            editTextPassword = (EditText) findViewById(R.id.EditText022);
            editTextPassword.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));


            Button buttonLogin = (Button) findViewById(R.id.loginButton);
            buttonLogin.setTypeface(businessModel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

            loginPresenter.getSupportNo();

            updateImageViews();

            ImageView btn_setting = (ImageView) findViewById(R.id.iv_setting);
            btn_setting.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent i = new Intent(LoginScreen.this,
                            UserSettingsActivity.class);
                    i.putExtra("isFromLogin", true);
                    startActivity(i);

                }
            });

            /* Display version information on the login screen. */
            TextView version = (TextView) findViewById(R.id.version);
            version.setText(getResources().getString(R.string.version)
                    + businessModel.getApplicationVersionName());
            version.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

            LinearLayout ll_footer = (LinearLayout) findViewById(R.id.ll_footer);
            ll_footer.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {

                    startActivity(new Intent(LoginScreen.this, About.class));

                }
            });

            loginPresenter.checkDB();

            /* Register receiver to receive download status. */
            IntentFilter filter = new IntentFilter(MyReceiver.PROCESS_RESPONSE);
            filter.addCategory(Intent.CATEGORY_DEFAULT);
            receiver = new MyReceiver();
            registerReceiver(receiver, filter);

            loginPresenter.assignServerUrl();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loginPresenter.reloadActivity();
    }

    private void updateImageViews() {
        /* Update login screen background image*/
        try {
            RelativeLayout bg = (RelativeLayout) findViewById(R.id.loginbackground);
            File f = new File(
                    getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                            + "/"
                            + businessModel.userMasterHelper.getUserMasterBO()
                            .getUserid() + "APP");
            if (f.isDirectory()) {
                File files[] = f.listFiles(new FilenameFilter() {
                    public boolean accept(File directory, String fileName) {
                        return fileName.startsWith("bg_client_login");
                    }
                });
                if (files != null && files.length > 0) {
                    Bitmap bitmapImage = BitmapFactory.decodeFile(files[0]
                            .getAbsolutePath());
                    Drawable bgrImage = new BitmapDrawable(getApplicationContext().getResources(), bitmapImage);

                    int sdk = Build.VERSION.SDK_INT;

                    if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
                        bg.setBackgroundDrawable(bgrImage);
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        bg.setBackground(bgrImage);
                    }
                }

            }
        } catch (Exception e) {
            Commons.printException(e);
        }

        /* Update login user logo image*/
        try {
            ImageView user_logo = (ImageView) findViewById(R.id.user_logo);
            File f = new File(
                    getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                            + "/"
                            + businessModel.userMasterHelper.getUserMasterBO()
                            .getUserid() + "APP");
            if (f.isDirectory()) {
                File files[] = f.listFiles(new FilenameFilter() {
                    public boolean accept(File directory, String fileName) {
                        return fileName.startsWith("client_logo");
                    }
                });
                if (files != null && files.length > 0) {


                    user_logo.setImageBitmap(BitmapFactory.decodeFile(files[0]
                            .getAbsolutePath()));

                }
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    @Override
    public void showGPSDialog() {

        new CommonDialog(getApplicationContext(), this, "", getResources().getString(R.string.enable_gps), false, getResources().getString(R.string.ok), new CommonDialog.positiveOnClickListener() {
            @Override
            public void onPositiveButtonClick() {
                Intent myIntent = new Intent(
                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(myIntent);

            }
        }).show();
    }

    @Override
    public void requestLocation() {
        businessModel.requestLocation(this);
    }

    @Override
    public void goToChangePwd() {
        startActivity(new Intent(LoginScreen.this,
                ChangePasswordActivity.class));
    }

    @Override
    public void resetPassword() {
        editTextUserName.setText("");
        editTextPassword.setText("");
        editTextUserName.requestFocus();
        Intent in = new Intent(
                LoginScreen.this,
                ChangePasswordActivity.class);
        in.putExtra("resetpassword", true);
        startActivity(in);
    }

    @Override
    public void goToHomeScreen() {
        BusinessModel.loadActivity(LoginScreen.this,
                DataMembers.actHomeScreen);
    }

    @Override
    public void goToAttendance() {
        startActivity(new Intent(LoginScreen.this,
                AttendanceActivity.class));
    }

    /**
     * @param view login button view reference from xml
     */
    public void onLoginClick(View view) {
        try {
            businessModel.userNameTemp = (editTextUserName.getText().toString());
            businessModel.passwordTemp = (editTextPassword.getText().toString());

            if (businessModel.userNameTemp.equals("")) {
                editTextUserName.requestFocus();
                editTextUserName.setError(getResources().getString(R.string.enter_username));
                return;
            } else if (businessModel.passwordTemp.equals("")) {
                editTextPassword.requestFocus();
                editTextPassword.setError(getResources().getString(R.string.enter_password));
                return;
            }
            loginPresenter.onLoginClick();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }


    public Handler getHandler() {
        return handler;
    }

    private final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            businessModel = (BusinessModel) getApplicationContext();
            switch (msg.what) {
                case DataMembers.NOTIFY_USEREXIST:
                    dismissAlertDialog();
                    loginPresenter.checkLogin();
                    finish();
                    break;
                case DataMembers.NOTIFY_NOT_USEREXIST:
                    if (!LoginHelper.getInstance(getApplicationContext()).IS_PASSWORD_LOCK) {
                        dismissAlertDialog();
                        editTextPassword.setText("");
                        showAlert(
                                getResources().getString(
                                        R.string.please_check_username_and_password), false);
                    } else {
                        int count = loginPresenter.getPasswordLockCount();
                        mForgotPasswordTV.setVisibility(View.VISIBLE);
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
                            editTextPassword.setText("");
                            showAlert(
                                    getResources().getString(
                                            R.string.please_check_username_and_password), false);
                            Toast.makeText(LoginScreen.this, "Remaining Password Count " + (LoginHelper.getInstance(getApplicationContext()).MAXIMUM_ATTEMPT_COUNT - (count + 1)), Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                case DataMembers.NOTIFY_UPDATE:
                    showProgressDialog(msg.obj.toString());
                    break;
                case DataMembers.NOTIFY_CONNECTION_PROBLEM:
                    dismissAlertDialog();
                    showAlert(
                            getResources()
                                    .getString(R.string.no_network_connection), false);
                    break;

                default:
                    break;
            }

        }
    };

    /**
     * This is the Handler for this activity. It will receive messages from the
     * DownloaderThread and make the necessary updates to the UI.
     */
    private final Handler activityHandler = new Handler() {
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
                                .getString(R.string.progress_dialog_message_prefix_connecting) + " " + url, 0, Message.obtain(this,
                                DataMembers.MESSAGE_DOWNLOAD_CANCELED), false);
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
                                msg.arg1, Message.obtain(this,
                                        DataMembers.MESSAGE_DOWNLOAD_CANCELED), true);
                    }
                    break;

			/*
             * Handling MESSAGE_DOWNLOAD_COMPLETE: 1. Remove the progress bar
			 * from the screen. 2. Display Toast that says download is complete.
			 */
                case DataMembers.MESSAGE_DOWNLOAD_COMPLETE:
                    dismissCurrentProgressDialog();
                    if (msg.arg1 == DownloaderThread.APK_DOWNLOAD) {
                        LoginHelper.getInstance(LoginScreen.this).deleteAllValues();
                        businessModel.activationHelper.clearAppUrl();
                        businessModel.userMasterHelper.getUserMasterBO().setUserid(0);
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(FileProvider.getUriForFile(LoginScreen.this, BuildConfig.APPLICATION_ID + ".provider", new File(LoginScreen.this.
                                    getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                                    + "/" + DataMembers.fileName)), "application/vnd.android.package-archive");

                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            startActivity(intent);
                        } catch (Exception e) {
                            Commons.printException(e);
                        }
                    } else {
                        loginPresenter.checkLogin();
                        finishActivity();
                    }
                    break;

                case DataMembers.MESSAGE_DOWNLOAD_COMPLETE_DC:
                    dismissCurrentProgressDialog();
                    if (businessModel.configurationMasterHelper.IS_CATALOG_IMG_DOWNLOAD)
                        loginPresenter.callCatalogImageDownload();
                    loginPresenter.checkLogin();
                    finishActivity();
                    break;

			/*
             * Handling MESSAGE_DOWNLOAD_CANCELLED: 1. Interrupt the downloader
			 * thread. 2. Remove the progress bar from the screen. 3. Display
			 * Toast that says download is complete.
			 */
                case DataMembers.MESSAGE_DOWNLOAD_CANCELED:
                    loginPresenter.clearAmazonDownload();
                    dismissCurrentProgressDialog();
                    showAlert(getString(R.string.user_message_download_canceled), true);
                    //finish();
                    //businessModel.loadDashBordHome();
                    BusinessModel.loadActivity(LoginScreen.this,
                            DataMembers.actHomeScreen);

                    break;

			/*
             * Handling MESSAGE_ENCOUNTERED_ERROR: 1. Check the obj field of the
			 * message for the actual error message that will be displayed to
			 * the user. 2. Remove any progress bars from the screen. 3. Display
			 * a Toast with the error message.
			 */
                case DataMembers.MESSAGE_ENCOUNTERED_ERROR:
                    // obj will contain a string representing the error message
                    if (msg.obj != null && msg.obj instanceof String) {
                        String errorMessage = (String) msg.obj;
                        dismissCurrentProgressDialog();
                        showAlert(errorMessage, false);
                    }

                    if (msg.arg1 == DownloaderThread.APK_DOWNLOAD) {
                        try {
                            LoginHelper.getInstance(LoginScreen.this).deleteUserMaster();
                            startActivity(new Intent(LoginScreen.this, LoginScreen.class));
                            finish();
                            break;
                        } catch (Exception e) {
                            Commons.printException(e);
                        }
                    }

                    finish();

                    if (businessModel.isDigitalContentAvailable()) {
                        businessModel.configurationMasterHelper.setAmazonS3Credentials();
                        loginPresenter.initializeTransferUtility();
                        loginPresenter.downloadDigitalContents();
                    } else {
                        loginPresenter.checkLogin();
                        finishActivity();
                    }

                    break;

                case DataMembers.MESSAGE_ENCOUNTERED_ERROR_DC:
                    // obj will contain a string representing the error message
                    if (msg.obj != null && msg.obj instanceof String) {
                        String errorMessage = (String) msg.obj;
                        dismissCurrentProgressDialog();
                        showAlert(errorMessage, false);
                    }
                    if (businessModel.configurationMasterHelper.IS_CATALOG_IMG_DOWNLOAD)
                        loginPresenter.callCatalogImageDownload();
                    finish();
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

    public boolean onOptionsItemSelected(MenuItem item) {
        int i1 = item.getItemId();
        if (i1 == R.id.menu_settings) {
            Intent i = new Intent(LoginScreen.this, UserSettingsActivity.class);
            startActivity(i);

        }
        return true;
    }

    @Override
    public void finishActivity() {
        finish();
        System.gc();
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
    public void setSupportNoTV(String supportNo) {
        /* Display customer support number on the login screen. */
        TextView support = (TextView) findViewById(R.id.customerSupport);
        if (supportNo.length() > 0)
            support.setText(supportNo);
        else
            support.setVisibility(View.GONE);
    }

    @Override
    public void retrieveDBData() {
        editTextUserName.setText(businessModel.userMasterHelper
                .getUserMasterBO().getLoginName());
        editTextUserName.setEnabled(false);
        editTextPassword.requestFocus();
    }

    @Override
    public void showAlert(String msg, boolean isFinish) {
        businessModel.showAlert(msg, 0);
        if (isFinish) {
            finish();
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public void showForgotPassword() {
        mForgotPasswordTV.setVisibility(View.VISIBLE);
        mForgotPasswordTV.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editTextUserName.getText().toString().equals("")) {
                    businessModel.userNameTemp = editTextUserName.getText().toString();
                    loginPresenter.callForgetPassword();
                } else {
                    editTextUserName.setError(getResources().getString(R.string.enter_username));
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
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
                    updaterProgressMsg(updateTableCount + " " + String.format(getResources().getString(R.string.out_of), totalTableCount));
                    if (totalTableCount == (updateTableCount + 1)) {
                        updaterProgressMsg(getResources().getString(R.string.updating_tables));
                        loginPresenter.applyLastSyncPref();
                    }
                } else {
                    reDownloadAlert(bundle);
                    break;
                }
                break;
            case SynchronizationHelper.DISTRIBUTOR_WISE_DOWNLOAD_INSERT:
                if (errorCode != null && errorCode.equals(SynchronizationHelper.UPDATE_TABLE_SUCCESS_CODE)) {
                    updaterProgressMsg(updateTableCount + " " + String.format(getResources().getString(R.string.out_of), totalTableCount));
                    if (totalTableCount == (updateTableCount + 1)) {
                        updaterProgressMsg(getResources().getString(R.string.updating_tables));
                        loginPresenter.applyLastSyncPref();
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
                    updaterProgressMsg(updateTableCount + " " + String.format(getResources().getString(R.string.out_of), totalTableCount));
                    if (totalTableCount == (updateTableCount + 1)) {
                        updaterProgressMsg(getResources().getString(R.string.updating_tables));
                        loginPresenter.applyLastSyncPref();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }


    @Override
    public void showAppUpdateAlert(String msg) {

        new CommonDialog(getApplicationContext(), this, "", msg, false, getResources().getString(R.string.ok), new CommonDialog.positiveOnClickListener() {
            @Override
            public void onPositiveButtonClick() {
                Commons.printInformation(businessModel.getUpdateURL());
                Thread downloaderThread = new DownloaderThread(
                        LoginScreen.this, activityHandler, businessModel
                        .getUpdateURL(), false,
                        DownloaderThread.APK_DOWNLOAD);
                downloaderThread.start();
            }
        }).show();
    }

    @Override
    public void showDialog() {
        new CommonDialog(getApplicationContext(), LoginScreen.this,
                getResources().getString(R.string.deviceId_change_msg_title),
                getResources().getString(R.string.deviceId_change_msg),
                false, getResources().getString(R.string.yes),
                getResources().getString(R.string.no),
                new CommonDialog.positiveOnClickListener() {
                    @Override
                    public void onPositiveButtonClick() {

                        loginPresenter.callAuthentication(true);

                    }
                }, new CommonDialog.negativeOnClickListener() {
            @Override
            public void onNegativeButtonClick() {


            }
        }).show();
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
        loginPresenter.deleteTables(false);

        dismissAlertDialog();

        editTextPassword.setText("");
        editTextUserName.setText("");
        editTextUserName.requestFocus();
        if (errorDownloadMessage != null) {
            showAlert(errorDownloadMessage, false);
        } else {
            showAlert(getResources().getString(R.string.please_redownload_data), false);
        }
    }

    @Override
    public void callResetPassword() {
        ResetPasswordDialog dialog = new ResetPasswordDialog(LoginScreen.this);
        dialog.show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SynchronizationHelper.DISTRIBUTOR_SELECTION_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    loginPresenter.callDistributorDownload();
                }
        }
    }

    @Override
    public void showProgressDialog(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginScreen.this);
        customProgressDialog(builder, msg);
        alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void dismissAlertDialog() {
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
    }

    @Override
    public void setAlertDialogMessage(String msg) {
        if (alertDialog != null) {
            alertDialog.setMessage(msg);
        }
    }

    @Override
    public void sendUserNotExistToHandler() {
        this.getHandler().sendEmptyMessage(DataMembers.NOTIFY_NOT_USEREXIST);
    }

    @Override
    public void threadActions() {
        new MyThread(LoginScreen.this, DataMembers.LOCAL_LOGIN).start();
    }

    @Override
    public void goToDistributorSelection() {
        Intent intent = new Intent(LoginScreen.this, DistributorSelectionActivity.class);
        intent.putExtra("isFromLogin", true);
        startActivityForResult(intent, SynchronizationHelper.DISTRIBUTOR_SELECTION_REQUEST_CODE);
    }

    @Override
    public void downloadImagesThreadStart(HashMap<String, String> imgUrls, TransferUtility transferUtility) {
        Thread downloaderThread = new DownloaderThreadNew(LoginScreen.this,
                activityHandler, imgUrls,
                businessModel.userMasterHelper.getUserMasterBO()
                        .getUserid(), transferUtility);
        downloaderThread.start();
    }

    private void callProgressDialog(String title, String message, int maxValue, Message newMsg, boolean isHorizontalStyle) {
        progressDialog = new ProgressDialog(LoginScreen.this);
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
        progressDialog.setCancelMessage(newMsg);
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }
}

