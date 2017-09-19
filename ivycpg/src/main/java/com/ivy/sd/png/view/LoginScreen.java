package com.ivy.sd.png.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
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

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferType;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.ivy.cpg.primarysale.bo.DistributorMasterBO;
import com.ivy.lib.Utils;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.RetailerMasterBO;
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
import com.ivy.sd.png.util.DateUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class LoginScreen extends IvyBaseActivityNoActionBar implements OnClickListener,
        ApplicationConfigs {

    private BusinessModel bmodel;
    private EditText editTextUserName, editTextPassword;
    private boolean syncDone;
    private LoginScreen thisActivity;
    private Thread downloaderThread;

    private AlertDialog.Builder builder;
    private AlertDialog alertDialog;

    // Used for File downoad
    private ProgressDialog progressDialog;
    private boolean bool = false;
    private String initialLanguage = "en";

    private MyReceiver receiver;
    private SharedPreferences mLastSyncSharedPref;
    public SharedPreferences mPasswordLockCountPref;

    private TransferUtility transferUtility;
    private AmazonS3Client s3;
    private int mTotalRetailerCount = 0;
    private int mIterateCount;
    private TextView mForgotPasswordTV;
    LinearLayout ll_footer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        bmodel.configurationMasterHelper.loadConfigurationForLoginScreen();
        bmodel.configurationMasterHelper.loadPasswordConfiguration();


        /* Set default language */
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        Configuration config = new Configuration();
        Locale locale = config.locale;
        if (!Locale.getDefault().equals(
                sharedPrefs.getString("languagePref", LANGUAGE))) {
            initialLanguage = sharedPrefs.getString("languagePref", LANGUAGE);
            locale = new Locale(sharedPrefs.getString("languagePref", LANGUAGE).substring(0, 2));
            Locale.setDefault(locale);
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());

        }


        // Getting back date
        DataMembers.backDate = sharedPrefs.getString("backDate", "");


        mLastSyncSharedPref = getSharedPreferences("lastSync", MODE_PRIVATE);
        mPasswordLockCountPref = getSharedPreferences("passwordlock", MODE_PRIVATE);
        SharedPreferences.Editor edt = mPasswordLockCountPref.edit();
        edt.putInt("lockcount", mPasswordLockCountPref.getInt("lockcount", 0));

        edt.apply();
        setContentView(R.layout.loginscreen);

         /* Check the date if expiry is enabled.*/
        if (ApplicationConfigs.expiryEnable) {
            if ((SDUtil.compareDate(ApplicationConfigs.expiryDate,
                    SDUtil.now(SDUtil.DATE_GLOBAL), "yyyy/MM/dd") < 0))
                finish();
        }

        thisActivity = this;
        downloaderThread = null;
        progressDialog = null;

        /* Show Forget password dialog.*/
        syncDone = bmodel.userMasterHelper.getSyncStatus();
        bmodel.userMasterHelper.downloadDistributionDetails();
        mForgotPasswordTV = (TextView) findViewById(R.id.txtResetPassword);
        mForgotPasswordTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        if (syncDone) {
            if (bmodel.configurationMasterHelper.IS_PASSWORD_ENCRIPTED)
                bmodel.synchronizationHelper.setEncryptType();

            bmodel.configurationMasterHelper.downloadChangepasswordConfig();
            if (bmodel.configurationMasterHelper.SHOW_FORGET_PASSWORD) {

                mForgotPasswordTV.setVisibility(View.VISIBLE);

            }
        }

        mForgotPasswordTV.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editTextUserName.getText().toString().equals("")) {
                    bmodel.userNameTemp=editTextUserName.getText().toString();
                    new ForgetPassword().execute();
                } else {
                    editTextUserName.setError(getResources().getString(R.string.enter_username));
//                    Toast.makeText(LoginScreen.this, getResources().getString(R.string.enter_username), Toast.LENGTH_SHORT).show();
                }
            }
        });

        /* Display customer support number on the login screen. */
        String supportNo = bmodel.getSupportNo();
        TextView support = (TextView) findViewById(R.id.customerSupport);
        if (supportNo.length() > 0)
            support.setText(supportNo);
        else
            support.setVisibility(View.GONE);


        /* Update login screen background image*/
        try {
            RelativeLayout bg = (RelativeLayout) findViewById(R.id.loginbackground);
            File f = new File(
                    getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                            + "/"
                            + bmodel.userMasterHelper.getUserMasterBO()
                            .getUserid() + "APP");
            if (f.isDirectory()) {
                File files[] = f.listFiles(new FilenameFilter() {
                    public boolean accept(File directory, String fileName) {
                        return fileName.startsWith("bg_client_login");
                    }
                });
                for (File temp : files) {
                    Bitmap bitmapImage = BitmapFactory.decodeFile(temp
                            .getAbsolutePath());
                    Drawable bgrImage = new BitmapDrawable(getApplicationContext().getResources(), bitmapImage);

                    int sdk = Build.VERSION.SDK_INT;

                    if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
                        bg.setBackgroundDrawable(bgrImage);
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        bg.setBackground(bgrImage);
                    }

                    break;
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
                            + bmodel.userMasterHelper.getUserMasterBO()
                            .getUserid() + "APP");
            if (f.isDirectory()) {
                File files[] = f.listFiles(new FilenameFilter() {
                    public boolean accept(File directory, String fileName) {
                        return fileName.startsWith("client_logo");
                    }
                });
                if (files != null && files.length > 0) {
                    for (File temp : files) {

                        user_logo.setImageBitmap(BitmapFactory.decodeFile(temp
                                .getAbsolutePath()));
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Commons.printException(e);
        }


        editTextUserName = (EditText) findViewById(R.id.EditText011);
        editTextPassword = (EditText) findViewById(R.id.EditText022);
        editTextPassword.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        editTextUserName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

        Button buttonLogin = (Button) findViewById(R.id.loginButton);
        buttonLogin.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        buttonLogin.setOnClickListener(this);

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

       /* *//* Display version information on the login screen. */
        TextView version = (TextView) findViewById(R.id.version);
        version.setText(getResources().getString(R.string.version)
                + bmodel.getApplicationVersionName());
        version.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));

        ll_footer = (LinearLayout) findViewById(R.id.ll_footer);
        ll_footer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(LoginScreen.this, About.class));

            }
        });

        /* Enable "Network Provider Date/Time". */
        bmodel.useNetworkProvidedValues();

        if (bmodel.synchronizationHelper.isExternalStorageAvailable()) {
            if (syncDone) {
                editTextUserName.setText(bmodel.userMasterHelper
                        .getUserMasterBO().getLoginName());
                editTextUserName.setEnabled(false);
                editTextPassword.requestFocus();
            } else {
                try {
                    File backupDB = new File(
                            getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                                    + "/pandg/" + DataMembers.DB_NAME);
                    if (backupDB.exists()) {
                        new RestoreDB().execute();
                    }
                } catch (Exception e) {
                }
            }
        } else {
            bmodel.showAlert(getResources().getString(R.string.external_storage_not_avail), 0);
            finish();
        }



        /* Copy Datawedgi Profile for Motorola barcode scanner.*/
        if (ApplicationConfigs.hasMotoBarcodeScanner)
            new AsyncCopyProfile().execute();

        bmodel.synchronizationHelper.loadErrorCode();

        /* Register reciver to receive downlaod status. */
        IntentFilter filter = new IntentFilter(MyReceiver.PROCESS_RESPONSE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new MyReceiver();
        registerReceiver(receiver, filter);

        // Assign server url
        if (ApplicationConfigs.withActivation) {
            DataMembers.SERVER_URL = PreferenceManager
                    .getDefaultSharedPreferences(this).getString("appUrlNew", "");
            DataMembers.ACTIVATION_KEY = PreferenceManager
                    .getDefaultSharedPreferences(this).getString("activationKey", "");
        }
    }


    @Override
    protected void onResume() {
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        super.onResume();

        /** When language preference is changed, recreate the activity.**/
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        if (!initialLanguage.equals(sharedPrefs.getString("languagePref",
                LANGUAGE))) {
            reload();
        }

        if (bmodel.configurationMasterHelper.SHOW_GPS_ENABLE_DIALOG) {
            if (!bmodel.locationUtil.isGPSProviderEnabled()) {
                Integer resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
                if (resultCode == ConnectionResult.SUCCESS) {
                    bmodel.requestLocation(this);
                } else {
                    showDialog(0);
                }
            }
        }

    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {

            case 0:
                new CommonDialog(getApplicationContext(), this, "", getResources().getString(R.string.enable_gps), false, getResources().getString(R.string.ok), new CommonDialog.positiveOnClickListener() {
                    @Override
                    public void onPositiveButtonClick() {
                        Intent myIntent = new Intent(
                                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);

                    }
                }).show();
                break;

        }
        return null;
    }

    public void onClick(View comp) {
        if (!bool) {
            bool = true;
            try {
                bmodel.userNameTemp = (editTextUserName.getText().toString());
                bmodel.passwordTemp = (editTextPassword.getText().toString());

                if (bmodel.userNameTemp.equals("")) {
                    editTextUserName.requestFocus();
                    editTextUserName.setError(getResources().getString(R.string.enter_username));
                    bool = false;
                    return;
                } else if (bmodel.passwordTemp.equals("")) {
                    editTextPassword.requestFocus();
                    editTextPassword.setError(getResources().getString(R.string.enter_password));
                    bool = false;
                    return;
                }

                int i = comp.getId();

                if (i == R.id.loginButton) {
                    if (syncDone) {
                        if (ApplicationConfigs.checkUTCTime && bmodel.isOnline()) {
                            new DownloadUTCTime().execute();
                        } else {
                            builder = new AlertDialog.Builder(LoginScreen.this);
                            bmodel.customProgressDialog(alertDialog, builder, LoginScreen.this, getResources().getString(R.string.loading_data));
                            alertDialog = builder.create();
                            alertDialog.show();
                            //handle password lock in off line based on reached maximum_attempt_count compare with mPasswordLockCountPref count
                            int count = mPasswordLockCountPref.getInt("passwordlock", 0);
                            if (count + 1 == bmodel.configurationMasterHelper.MAXIMUM_ATTEMPT_COUNT)
                                this.getHandler().sendEmptyMessage(
                                        DataMembers.NOTIFY_NOT_USEREXIST);
                            else
                                new MyThread(LoginScreen.this, DataMembers.LOCAL_LOGIN).start();
                        }
                    } else {
                        builder = new AlertDialog.Builder(LoginScreen.this);
                        bmodel.customProgressDialog(alertDialog, builder, LoginScreen.this, getResources().getString(R.string.auth_and_downloading_masters));
                        alertDialog = builder.create();
                        alertDialog.show();

                        if (!DataMembers.SERVER_URL.equals("")) {
                            mIterateCount = bmodel.synchronizationHelper.getmRetailerWiseIterateCount();
                            new Authentication(false).execute();
                        } else {
                            bmodel.showAlert(getResources().getString(R.string.download_url_empty), 0);
                            alertDialog.dismiss();
                        }
                    }
                } else {
                    bool = false;
                }
            } catch (Exception e) {
                Commons.printException(e);
                bool = false;
            }
        }

    }

    private void checkLogin() {
        if (bmodel.configurationMasterHelper.SHOW_CHANGE_PASSWORD) {
            String createdDate = bmodel.synchronizationHelper.getPasswordCreatedDate();
            if (createdDate != null && !createdDate.equals("")) {
                int result = SDUtil.compareDate(bmodel.configurationMasterHelper.getPasswordExpiryDate(createdDate), bmodel.userMasterHelper.getUserMasterBO().getDownloadDate(), "yyyy/MM/dd");
                if (result == -1) {
                    startActivity(new Intent(LoginScreen.this,
                            ChangePasswordActivity.class));
                } else {
                    checkAttendance();
                    //used for showing password expiring date
                    int days = (int) getDifferenceDays(bmodel.configurationMasterHelper.getPasswordExpiryDate(createdDate),
                            bmodel.userMasterHelper.getUserMasterBO().getDownloadDate(), "yyyy/MM/dd");
                    Commons.print("Password Expiry in " + days + " days");
                    if (days < (bmodel.configurationMasterHelper.PSWD_EXPIRY * 0.2)) {
                        Resources res = getResources();
                        bmodel.showAlert(res.getQuantityString(R.plurals.password_expires, days, days), 0);
                    }
                }
            } else {
                checkAttendance();
            }
        } else {
            checkAttendance();
        }
    }

    private void checkAttendance() {
        if (bmodel.configurationMasterHelper.SHOW_ATTENDANCE) {
            if (bmodel.mAttendanceHelper.loadAttendanceMaster()) {
                bmodel.loadDashBordHome();
                BusinessModel.loadActivity(LoginScreen.this,
                        DataMembers.actHomeScreen);
            } else {
                startActivity(new Intent(LoginScreen.this,
                        AttendanceActivity.class));
            }
        } else {
            bmodel.loadDashBordHome();
            BusinessModel.loadActivity(LoginScreen.this,
                    DataMembers.actHomeScreen);
        }
    }

    public Handler getHandler() {
        return handler;
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            bmodel = (BusinessModel) getApplicationContext();
            bool = false;
            switch (msg.what) {
                case DataMembers.NOTIFY_USEREXIST:
                    if (alertDialog != null)
                        alertDialog.dismiss();
                    checkLogin();
                    finish();
                    break;
                case DataMembers.NOTIFY_NOT_USEREXIST:
                    if (!bmodel.configurationMasterHelper.IS_PASSWORD_LOCK) {
                        if (alertDialog != null)
                            alertDialog.dismiss();
                        editTextPassword.setText("");
                        bmodel.showAlert(
                                getResources().getString(
                                        R.string.please_check_username_and_password), 0);
                    } else {
                        int count = mPasswordLockCountPref.getInt("passwordlock", 0);
                        mForgotPasswordTV.setVisibility(View.VISIBLE);
                        if (count + 1 == bmodel.configurationMasterHelper.MAXIMUM_ATTEMPT_COUNT) {
                            if (alertDialog != null)
                                alertDialog.dismiss();
                            FragmentManager fm = getSupportFragmentManager();
                            PasswordLockDialogFragment dialogFragment = new PasswordLockDialogFragment();
                            Bundle bundle = new Bundle();
                            bundle.putString("title", "Password Lock");
                            bundle.putString("textviewTitle", getResources().getString(R.string.exceed_pwd_retry_limit));
                            dialogFragment.setArguments(bundle);
                            dialogFragment.show(fm, "Sample Fragment");
                        } else {
                            SharedPreferences.Editor edt = mPasswordLockCountPref.edit();
                            edt.putInt("passwordlock", count + 1);
                            edt.apply();
                            if (alertDialog != null)
                                alertDialog.dismiss();
                            editTextPassword.setText("");
                            bmodel.showAlert(
                                    getResources().getString(
                                            R.string.please_check_username_and_password), 0);
                            Toast.makeText(LoginScreen.this, "Remaining Password Count " + (bmodel.configurationMasterHelper.MAXIMUM_ATTEMPT_COUNT - (count + 1)), Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                case DataMembers.NOTIFY_UPDATE:
                    bmodel.setMessageInProgressDialog(alertDialog, builder, LoginScreen.this, msg.obj.toString());
                    break;
                case DataMembers.NOTIFY_CONNECTION_PROBLEM:
                    alertDialog.dismiss();
                    bmodel.showAlert(
                            getResources()
                                    .getString(R.string.no_network_connection), 0);
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
    private Handler activityHandler = new Handler() {
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
                        String pdTitle = thisActivity
                                .getString(R.string.progress_dialog_title_connecting);
                        String pdMsg = thisActivity
                                .getString(R.string.progress_dialog_message_prefix_connecting);
                        pdMsg += " " + url;

                        dismissCurrentProgressDialog();
                        progressDialog = new ProgressDialog(thisActivity);
                        progressDialog.setTitle(pdTitle);
                        progressDialog.setMessage(pdMsg);
                        progressDialog
                                .setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progressDialog.setIndeterminate(true);
                        // set the message to be sent when this dialog is canceled
                        Message newMsg = Message.obtain(this,
                                DataMembers.MESSAGE_DOWNLOAD_CANCELED);
                        progressDialog.setCancelMessage(newMsg);
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.show();
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
                        int maxValue = msg.arg1;
                        String fileName = (String) msg.obj;
                        String pdTitle = thisActivity
                                .getString(R.string.progress_dialog_title_downloading);
                        String pdMsg = thisActivity
                                .getString(R.string.progress_dialog_message_prefix_downloading);
                        pdMsg += " " + fileName;

                        dismissCurrentProgressDialog();
                        progressDialog = new ProgressDialog(thisActivity);
                        progressDialog.setTitle(pdTitle);
                        progressDialog.setMessage(pdMsg);
                        progressDialog
                                .setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        progressDialog.setProgress(0);
                        progressDialog.setMax(maxValue);
                        // set the message to be sent when this dialog is canceled
                        Message newMsg = Message.obtain(this,
                                DataMembers.MESSAGE_DOWNLOAD_CANCELED);
                        progressDialog.setCancelMessage(newMsg);
                        progressDialog.setCancelable(true);
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.show();
                    }
                    break;

			/*
             * Handling MESSAGE_DOWNLOAD_COMPLETE: 1. Remove the progress bar
			 * from the screen. 2. Display Toast that says download is complete.
			 */
                case DataMembers.MESSAGE_DOWNLOAD_COMPLETE:
                    dismissCurrentProgressDialog();
                    if (msg.arg1 == DownloaderThread.APK_DOWNLOAD) {
                        bmodel.deleteAllValues();
                        bmodel.activationHelper.clearAppUrl();
                        bmodel.userMasterHelper.getUserMasterBO().setUserid(0);
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(
                                    Uri.fromFile(new File(
                                            getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                                                    + "/" + DataMembers.fileName)),
                                    "application/vnd.android.package-archive");
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } catch (Exception e) {
                            Commons.printException(e);
                        }
                    } else {
                        checkLogin();
                        finish();
                        System.gc();
                    }
                    break;

                case DataMembers.MESSAGE_DOWNLOAD_COMPLETE_DC:
                    dismissCurrentProgressDialog();

                    checkLogin();
                    finish();
                    System.gc();
                    break;

			/*
             * Handling MESSAGE_DOWNLOAD_CANCELLED: 1. Interrupt the downloader
			 * thread. 2. Remove the progress bar from the screen. 3. Display
			 * Toast that says download is complete.
			 */
                case DataMembers.MESSAGE_DOWNLOAD_CANCELED:
                   /* if (downloaderThread != null) {
                        downloaderThread.interrupt();
                    }*/
                    clearAmazonDownload();
                    dismissCurrentProgressDialog();
                    displayMessage(getString(R.string.user_message_download_canceled));
                    finish();
                    bmodel.loadDashBordHome();
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
                        displayMessage(errorMessage);
                    }

                    if (msg.arg1 == DownloaderThread.APK_DOWNLOAD) {
                        try {
                            DBUtil db = new DBUtil(thisActivity, DataMembers.DB_NAME,
                                    DataMembers.DB_PATH);
                            db.createDataBase();
                            db.openDataBase();
                            db.deleteSQL(DataMembers.tbl_userMaster, null, true);
                            db.closeDB();
                            startActivity(new Intent(thisActivity, LoginScreen.class));
                            finish();
                            break;
                        } catch (Exception e) {
                            Commons.printException(e);
                        }
                    }

                    finish();

                    if (bmodel.isDigitalContentAvailable()) {
                        bmodel.configurationMasterHelper.setAmazonS3Credentials();
                        initializeTransferUtility();
                        downloaderThread = new DownloaderThreadNew(thisActivity,
                                activityHandler, bmodel.getDigitalContentURLS(),
                                bmodel.userMasterHelper.getUserMasterBO()
                                        .getUserid(), transferUtility);
                        downloaderThread.start();
                    } else {
                        checkLogin();
                        finish();
                        System.gc();
                    }

                    break;

                case DataMembers.MESSAGE_ENCOUNTERED_ERROR_DC:
                    // obj will contain a string representing the error message
                    if (msg.obj != null && msg.obj instanceof String) {
                        String errorMessage = (String) msg.obj;
                        dismissCurrentProgressDialog();
                        displayMessage(errorMessage);
                    }
                    finish();
                    checkLogin();
                    break;

                case DataMembers.THIRD_PARTY_INSTALLATION_ERROR:
                    // obj will contain a string representing the error message
                    if (msg.obj != null && msg.obj instanceof String) {
                        String errorMessage = (String) msg.obj;
                        dismissCurrentProgressDialog();
                        displayMessage(errorMessage);
                    }
                    break;

                case DataMembers.SDCARD_NOT_AVAILABLE:
                    // obj will contain a string representing the error message
                    if (msg.obj != null && msg.obj instanceof String) {
                        String errorMessage = (String) msg.obj;
                        dismissCurrentProgressDialog();
                        displayMessage(errorMessage);
                    }
                    checkLogin();
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

    /**
     * Displays a message to the user, in the form of a Toast.
     *
     * @param message Message to be displayed.
     */
    public void displayMessage(String message) {
        if (message != null) {
            bmodel.showAlert(message, 0);
//            Toast.makeText(thisActivity, message, Toast.LENGTH_SHORT).show();
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
    protected void onPause() {
        super.onPause();

    }

    private void reload() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();

        overridePendingTransition(0, 0);
        startActivity(intent);
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

    class RestoreDB extends AsyncTask<Integer, Integer, Boolean> {

        private ProgressDialog progressDialogue;

        protected void onPreExecute() {
            progressDialogue = ProgressDialog.show(LoginScreen.this,
                    DataMembers.SD,
                    getResources().getString(R.string.Restoring_database),
                    true, false);
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            if (bmodel.synchronizationHelper.reStoreDB()) {
                return true;

            } else {
                return false;
            }
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground
            progressDialogue.dismiss();
            if (result) {
                bmodel.showAlert(getResources().getString(R.string.database_restored), 0);
                syncDone = bmodel.userMasterHelper.getSyncStatus();
                bmodel.userMasterHelper.downloadDistributionDetails();
                if (syncDone) {
                    editTextUserName.setText(bmodel.userMasterHelper
                            .getUserMasterBO().getLoginName());
                    editTextUserName.setEnabled(false);
                    editTextPassword.requestFocus();

                } else {
                    bmodel.showAlert(getResources().getString(R.string.database_not_restored), 0);
                }
            }
        }
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
        AssetManager assetManager = getAssets();
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
    private int chmod(File path, int mode) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class fileUtils = Class.forName("android.os.FileUtils");
        Method setPermissions = fileUtils.getMethod("setPermissions",
                String.class, int.class, int.class, int.class);
        return (Integer) setPermissions.invoke(null, path.getAbsolutePath(),
                mode, -1, -1);
    }

    @Override
    public void onBackPressed() {
        BusinessModel.loginFlag = false;
        super.onBackPressed();
        finish();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
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
        int method = bundle.getInt(SynchronizationHelper.SYNXC_STATUS, 0);
        String errorCode = bundle.getString(SynchronizationHelper.ERROR_CODE);
        int updateTableCount = bundle.getInt("updateCount");
        int totalTableCount = bundle.getInt("totalCount");
        switch (method) {
            case SynchronizationHelper.VOLLEY_DOWNLOAD_INSERT:
                if (errorCode.equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)
                        && (totalTableCount == updateTableCount)) {
                    new UpdateFinish().execute();
                } else if (errorCode.equals(SynchronizationHelper.UPDATE_TABLE_SUCCESS_CODE)) {
                    bmodel.updaterProgressMsg(updateTableCount + " " + String.format(getResources().getString(R.string.out_of), totalTableCount));
                    if (totalTableCount == (updateTableCount + 1)) {
                        bmodel.updaterProgressMsg(getResources().getString(R.string.updating_tables));
                        SharedPreferences.Editor edt = mLastSyncSharedPref.edit();
                        edt.putString("date", DateUtil.convertFromServerDateToRequestedFormat(
                                SDUtil.now(SDUtil.DATE_GLOBAL),
                                bmodel.configurationMasterHelper.outDateFormat));
                        edt.putString("time", SDUtil.now(SDUtil.TIME));
                        edt.apply();
                    }
                } else {
                    reDownloadAlert(bundle);
                    break;
                }
                break;
            case SynchronizationHelper.DISTRIBUTOR_WISE_DOWNLOAD_INSERT:
                if (errorCode.equals(SynchronizationHelper.UPDATE_TABLE_SUCCESS_CODE)) {
                    bmodel.updaterProgressMsg(updateTableCount + " " + String.format(getResources().getString(R.string.out_of), totalTableCount));
                    if (totalTableCount == (updateTableCount + 1)) {
                        bmodel.updaterProgressMsg(getResources().getString(R.string.updating_tables));
                        SharedPreferences.Editor edt = mLastSyncSharedPref.edit();
                        edt.putString("date", DateUtil.convertFromServerDateToRequestedFormat(
                                SDUtil.now(SDUtil.DATE_GLOBAL),
                                bmodel.configurationMasterHelper.outDateFormat));
                        edt.putString("time", SDUtil.now(SDUtil.TIME));
                        edt.apply();
                    }
                } else if (errorCode.equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
                    new UpdateDistributorFinish().execute();
                } else {
                    reDownloadAlert(bundle);
                    break;
                }
                break;
            case SynchronizationHelper.LAST_VISIT_TRAN_DOWNLOAD_INSERT:
                if (errorCode.equals(SynchronizationHelper.UPDATE_TABLE_SUCCESS_CODE)) {
                    bmodel.updaterProgressMsg(updateTableCount + " " + String.format(getResources().getString(R.string.out_of), totalTableCount));
                    if (totalTableCount == (updateTableCount + 1)) {
                        bmodel.updaterProgressMsg(getResources().getString(R.string.updating_tables));
                        SharedPreferences.Editor edt = mLastSyncSharedPref.edit();
                        edt.putString("date", DateUtil.convertFromServerDateToRequestedFormat(
                                SDUtil.now(SDUtil.DATE_GLOBAL),
                                bmodel.configurationMasterHelper.outDateFormat));
                        edt.putString("time", SDUtil.now(SDUtil.TIME));
                        edt.commit();
                    }
                } else if (errorCode.equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
                    new UpdateRetailerFinish().execute();
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


    private class DownloadUTCTime extends
            AsyncTask<Integer, Integer, Integer> {

        private int UTCflag;

        protected void onPreExecute() {
            alertDialog = new ProgressDialog(LoginScreen.this);
            alertDialog.setMessage(getResources().getString(R.string.checking_time));
            alertDialog.setCancelable(false);
            alertDialog.show();
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        @Override
        protected Integer doInBackground(Integer... params) {
            try {
                UTCflag = bmodel.synchronizationHelper.getUTCDateTimeNew("/UTCDateTime");
            } catch (Exception e) {
                Commons.printException(e);
            }
            return UTCflag;
        }

        protected void onPostExecute(Integer result) {
            if (UTCflag == 2) {
                alertDialog.dismiss();

                new CommonDialog(getApplicationContext(), LoginScreen.this, "", getResources().getString(R.string.enable_gps), false, getResources().getString(R.string.ok), new CommonDialog.positiveOnClickListener() {
                    @Override
                    public void onPositiveButtonClick() {

                    }
                }).show();
                bool = false;
            } else {
                alertDialog.setMessage(getResources().getString(R.string.loading_data));
                new MyThread(LoginScreen.this, DataMembers.LOCAL_LOGIN).start();
            }
        }
    }

    private void clearAmazonDownload() {
        if (transferUtility != null) {
            transferUtility.cancelAllWithType(TransferType.DOWNLOAD);
        }
    }

    private void initializeTransferUtility() {
        BasicAWSCredentials myCredentials = new BasicAWSCredentials(ConfigurationMasterHelper.ACCESS_KEY_ID,
                ConfigurationMasterHelper.SECRET_KEY);
        s3 = new AmazonS3Client(myCredentials);
        transferUtility = new TransferUtility(s3, getApplicationContext());
    }

    private class DeleteTables extends
            AsyncTask<Integer, Integer, Integer> {
        boolean isDownloaded;

        DeleteTables() {

        }

        DeleteTables(boolean isDownloaded) {
            this.isDownloaded = isDownloaded;
        }


        protected void onPreExecute() {

        }

        protected void onProgressUpdate(Integer... progress) {

        }

        @Override
        protected Integer doInBackground(Integer... params) {
            bmodel.synchronizationHelper.deleteTables(false);
            return 0;
        }

        protected void onPostExecute(Integer result) {
            if (isDownloaded) {
                final ArrayList<String> urlList = bmodel.synchronizationHelper
                        .getUrlList();
                if (urlList.size() > 0) {
                    bmodel.synchronizationHelper.downloadMasterAtVolley(SynchronizationHelper.FROM_SCREEN.LOGIN, SynchronizationHelper.DownloadType.NORMAL_DOWNLOAD);
                } else {
                    bmodel.showAlert(getResources().getString(R.string.no_data_download), 0);
                    alertDialog.dismiss();
                    bool = false;
                }
            }
        }
    }

    class CheckNewVersionTask extends AsyncTask<Integer, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                if (bmodel.isOnline()) {
                    bmodel.synchronizationHelper.updateAuthenticateToken();
                    return bmodel.synchronizationHelper.checkForAutoUpdate();
                } else
                    return Boolean.FALSE;

            } catch (Exception e) {
                Commons.printException(e);
            }
            return Boolean.FALSE;
        }

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(LoginScreen.this);

            bmodel.customProgressDialog(alertDialog, builder, LoginScreen.this, getResources().getString(R.string.checking_new_version));
            alertDialog = builder.create();
            alertDialog.show();
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            if (!result) {
                if (isPasswordReset()) {
                    alertDialog.dismiss();
                    editTextUserName.setText("");
                    editTextPassword.setText("");
                    editTextUserName.requestFocus();
                    bool = false;
                    Intent in = new Intent(
                            thisActivity,
                            ChangePasswordActivity.class);
                    in.putExtra("resetpassword", true);
                    startActivity(in);
                } else {
                    bmodel.synchronizationHelper.deleteUrlDownloadMaster();
                    new UrlDownloadData().execute();
                }

            } else {
                bool = false;
                alertDialog.dismiss();
                showAlertOk(
                        getResources().getString(R.string.update_available),
                        DataMembers.NOTIFY_AUTOUPDATE_FOUND);
            }

        }

    }

    public void showAlertOk(String msg, int id) {
        final int idd = id;

        new CommonDialog(getApplicationContext(), this, "", msg, false, getResources().getString(R.string.ok), new CommonDialog.positiveOnClickListener() {
            @Override
            public void onPositiveButtonClick() {
                if (idd == DataMembers.NOTIFY_AUTOUPDATE_FOUND) {
                    Commons.printInformation(bmodel.getUpdateURL());
                    downloaderThread = new DownloaderThread(
                            thisActivity, activityHandler, bmodel
                            .getUpdateURL(), false,
                            DownloaderThread.APK_DOWNLOAD);
                    downloaderThread.start();
                }
            }
        }).show();
    }

    public boolean isPasswordReset() {
        boolean isReset = false;
        try {
            DBUtil db = new DBUtil(thisActivity, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Cursor c = db.selectSQL("select IsResetPassword from usermaster where loginid ='" + bmodel.userNameTemp + "' COLLATE NOCASE");
            if (c.getCount() > 0) {
                if (c.moveToNext()) {
                    isReset = c.getInt(0) > 0;
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return isReset;
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
                jsonObj.put("LoginId", bmodel.userNameTemp);
                jsonObj.put("Password", bmodel.passwordTemp);
                jsonObj.put(SynchronizationHelper.VERSION_CODE,
                        bmodel.getApplicationVersionNumber());

                jsonObj.put("Model", Build.MODEL);
                jsonObj.put("Platform", "Android");
                jsonObj.put("OSVersion", android.os.Build.VERSION.RELEASE);
                jsonObj.put("FirmWare", "");
                jsonObj.put("DeviceId",
                        bmodel.activationHelper.getIMEINumber());
                jsonObj.put("RegistrationId", bmodel.regid);
                jsonObj.put("DeviceUniqueId",bmodel.activationHelper.getDeviceId());
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
            String LoginResponse = bmodel.synchronizationHelper.userAuthenticate(jsonObject, changeDeviceId);
            try {
                JSONObject jsonObject = new JSONObject(LoginResponse);
                Iterator itr = jsonObject.keys();
                while (itr.hasNext()) {
                    String key = (String) itr.next();
                    if (key.equals(SynchronizationHelper.ERROR_CODE)) {
                        String errorCode = jsonObject.getString(key);
                        if (errorCode.equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
                            bmodel.synchronizationHelper
                                    .parseJSONAndInsert(jsonObject, false);
                            bmodel.userMasterHelper.downloadUserDetails();
                            bmodel.userMasterHelper.downloadDistributionDetails();
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
            alertDialog.dismiss();
            if (output.equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
                new CheckNewVersionTask().execute();
            } else {
                bool = false;

                if (output.equals("E27")) {
                    showDialog();
                } else {
                    if (output.equals("E25")) {
                        mForgotPasswordTV.setVisibility(View.VISIBLE);

                    }

                    String ErrorMessage = bmodel.synchronizationHelper.getErrormessageByErrorCode().get(output);

                    if (ErrorMessage != null) {
                        bmodel.showAlert(ErrorMessage, 0);
                    } else
                        bmodel.showAlert("Connection Exception ", 0);
                }


            }

        }
    }

    private void showDialog() {
        new CommonDialog(getApplicationContext(), LoginScreen.this,
                getResources().getString(R.string.deviceId_change_msg_title),
                getResources().getString(R.string.deviceId_change_msg),
                false, getResources().getString(R.string.yes),
                getResources().getString(R.string.no),
                new CommonDialog.positiveOnClickListener() {
                    @Override
                    public void onPositiveButtonClick() {

                        new Authentication(true).execute();

                    }
                }, new CommonDialog.negativeOnClickListener() {
            @Override
            public void onNegativeButtonClick() {


            }
        }).show();
    }

    /**
     * UrlDownload Data class is download master mapping url from server
     * and insert into sqlite file
     */
    class UrlDownloadData extends AsyncTask<String, String, String> {
        JSONObject jsonObject = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            jsonObject = bmodel.synchronizationHelper.getCommonJsonObject();
        }

        @Override
        protected String doInBackground(String... params) {
            String response = bmodel.synchronizationHelper.sendPostMethod(SynchronizationHelper.URLDOWNLOAD_MASTER_APPEND_URL, jsonObject);
            try {
                JSONObject jsonObject = new JSONObject(response);
                Iterator itr = jsonObject.keys();
                while (itr.hasNext()) {
                    String key = (String) itr.next();
                    if (key.equals(SynchronizationHelper.ERROR_CODE)) {
                        String errorCode = jsonObject.getString(key);
                        if (errorCode.equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
                            bmodel.synchronizationHelper
                                    .parseJSONAndInsert(jsonObject, true);
                            bmodel.synchronizationHelper.loadMasterUrlFromDB(true);
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
        protected void onPostExecute(String errorCode) {
            super.onPostExecute(errorCode);
            if (errorCode
                    .equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
                new DeleteTables(true).execute();
            } else {
                new DeleteTables().execute();
                String errorMessage = bmodel.synchronizationHelper
                        .getErrormessageByErrorCode().get(errorCode);
                if (errorMessage != null) {
                    bmodel.showAlert(errorMessage, 0);
                }
                alertDialog.dismiss();
                bool = false;
            }
        }
    }

    /**
     * After download all data send acknowledge to server using this class
     */
    public class UpdateFinish extends AsyncTask<String, String, String> {
        JSONObject json = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            json = bmodel.synchronizationHelper.getCommonJsonObject();
        }

        @Override
        protected String doInBackground(String... params) {

            String response = bmodel.synchronizationHelper.sendPostMethod(SynchronizationHelper.UPDATE_FINISH_URL, json);
            try {
                JSONObject jsonObject = new JSONObject(response);
                Iterator itr = jsonObject.keys();
                while (itr.hasNext()) {
                    String key = (String) itr.next();
                    if (key.equals(SynchronizationHelper.VOLLEY_RESPONSE)) {
                        String errorCode = jsonObject.getString(key);
                        bmodel.configurationMasterHelper.isDistributorWiseDownload();
                        bmodel.configurationMasterHelper.downloadConfigForLoadLastVisit();
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
            SynchronizationHelper.NEXT_METHOD next_method = bmodel.synchronizationHelper.checkNextSyncMethod();
            callNextTask(next_method);

        }
    }

    /**
     * Distributor wise master will be downloaded if configuration enable.
     * This class is initiate distributor wise master download.we will send all
     * distributor id with userid and version code  to server.
     */
    class InitiateDistributorDownload extends AsyncTask<String, String, String> {
        JSONObject json;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                bmodel.distributorMasterHelper.downloadDistributorsList();
                ArrayList<DistributorMasterBO> distributorList = bmodel.distributorMasterHelper.getDistributors();
                json = bmodel.synchronizationHelper.getCommonJsonObject();
                JSONArray jsonArray = new JSONArray();
                for (DistributorMasterBO distributorBO : distributorList) {
                    jsonArray.put(distributorBO.getDId());
                }
                json.put("DistributorIds", jsonArray);
            } catch (Exception jsonException) {
                Commons.print(jsonException.getMessage());
            }
        }

        @Override
        protected String doInBackground(String... params) {
            String response = bmodel.synchronizationHelper.sendPostMethod(SynchronizationHelper.UPDATE_FINISH_URL, json);
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
                bmodel.synchronizationHelper.loadMasterUrlFromDB(false);
                bmodel.synchronizationHelper.downloadMasterAtVolley(SynchronizationHelper.FROM_SCREEN.LOGIN, SynchronizationHelper.DownloadType.DISTRIBUTOR_WISE_DOWNLOAD);
            }
        }
    }

    /**
     * After download all distributore wise data send acknowledge to server using this class
     */
    class UpdateDistributorFinish extends AsyncTask<String, String, String> {
        JSONObject json = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                json = bmodel.synchronizationHelper.getCommonJsonObject();
            } catch (Exception jsonException) {
                Commons.print(jsonException.getMessage());
            }
        }

        @Override
        protected String doInBackground(String... params) {
            String response = bmodel.synchronizationHelper.sendPostMethod(SynchronizationHelper.UPDATE_FINISH_URL, json);
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
            SynchronizationHelper.NEXT_METHOD next_method = bmodel.synchronizationHelper.checkNextSyncMethod();
            callNextTask(next_method);

        }
    }

    /**
     * Retailer wise Last visit transaction  data will be downloaded for following module
     * (Price check,Near Expiry,Stock check,Survey,promotion )if configuration enable.
     * This class is initiate retailer  wise last visit  download.we will send all
     * retailerid with userid and version code  to server.
     */
    class InitiateRetailerDownload extends AsyncTask<String, String, String> {
        JSONObject json;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {

                if (mTotalRetailerCount == 0) {
                    mTotalRetailerCount = bmodel.synchronizationHelper.getTotalRetailersCount();
                    mIterateCount = mTotalRetailerCount / SynchronizationHelper.LAST_VISIT_TRAN_SPLIT_RETAILER_COUNT;
                    final int remainder = mTotalRetailerCount % SynchronizationHelper.LAST_VISIT_TRAN_SPLIT_RETAILER_COUNT;
                    if (remainder > 0) mIterateCount = mIterateCount + 1;

                    bmodel.synchronizationHelper.setRetailerwiseTotalIterateCount(mIterateCount);
                    bmodel.synchronizationHelper.setmRetailerWiseIterateCount(mIterateCount);
                }
                final ArrayList<RetailerMasterBO> retailerIds = bmodel.synchronizationHelper.getRetailerIdsForDownloadTranSactionData(mIterateCount - 1);
                mIterateCount--;

                json = bmodel.synchronizationHelper.getCommonJsonObject();
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
            String response = bmodel.synchronizationHelper.sendPostMethod(SynchronizationHelper.UPDATE_FINISH_URL, json);
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
                bmodel.synchronizationHelper.loadMasterUrlFromDB(false);
                if (bmodel.synchronizationHelper.getUrlList() != null && bmodel.synchronizationHelper.getUrlList().size() > 0) {
                    bmodel.synchronizationHelper.downloadLastVisitTranAtVolley(SynchronizationHelper.FROM_SCREEN.LOGIN, 1);
                } else {
                    bmodel.synchronizationHelper.isLastVisitTranDownloadDone = true;
                    SynchronizationHelper.NEXT_METHOD next_method = bmodel.synchronizationHelper.checkNextSyncMethod();
                    callNextTask(next_method);
                }
            } else {
                bmodel.synchronizationHelper.isLastVisitTranDownloadDone = true;
                SynchronizationHelper.NEXT_METHOD next_method = bmodel.synchronizationHelper.checkNextSyncMethod();
                callNextTask(next_method);
            }
        }
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
                json = bmodel.synchronizationHelper.getCommonJsonObject();
            } catch (Exception jsonException) {
                Commons.print(jsonException.getMessage());
            }
        }

        @Override
        protected String doInBackground(String... params) {
            String response = bmodel.synchronizationHelper.sendPostMethod(SynchronizationHelper.UPDATE_FINISH_URL, json);
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
                SynchronizationHelper.NEXT_METHOD next_method = bmodel.synchronizationHelper.checkNextSyncMethod();
                callNextTask(next_method);
            } else {
                new InitiateRetailerDownload().execute();
            }
        }
    }

    /**
     * download stock from stockinhandmaster web api
     */
    class SihDownloadTask extends AsyncTask<String, String, String> {
        JSONObject json = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            json = bmodel.synchronizationHelper.getCommonJsonObject();
        }

        @Override
        protected String doInBackground(String... params) {
            String response = bmodel.synchronizationHelper.sendPostMethod(bmodel.synchronizationHelper.getSIHUrl(), json);
            try {
                JSONObject jsonObject = new JSONObject(response);
                Iterator itr = jsonObject.keys();
                while (itr.hasNext()) {
                    String key = (String) itr.next();
                    if (key.equals(SynchronizationHelper.ERROR_CODE)) {
                        String errorCode = jsonObject.getString(key);
                        if (errorCode.equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
                            bmodel.synchronizationHelper
                                    .parseJSONAndInsert(jsonObject, true);

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
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            SynchronizationHelper.NEXT_METHOD next_method = bmodel.synchronizationHelper.checkNextSyncMethod();
            callNextTask(next_method);
        }
    }

    /**
     * After download all data from server using this method to  update data from temprorary table to
     * maing table and load data from sqlite and update in objects
     */
    class LoadData extends AsyncTask<String, String, SynchronizationHelper.NEXT_METHOD> {


        @Override
        protected SynchronizationHelper.NEXT_METHOD doInBackground(String... params) {
            SynchronizationHelper.NEXT_METHOD next_method = bmodel.synchronizationHelper.checkNextSyncMethod();
            if (next_method == SynchronizationHelper.NEXT_METHOD.DIGITAL_CONTENT_AVALILABLE || next_method == SynchronizationHelper.NEXT_METHOD.DEFAULT) {
                final long startTime = System.nanoTime();
                bmodel.synchronizationHelper
                        .updateProductAndRetailerMaster();
                bmodel.synchronizationHelper.loadMethodsNew();
                long endTime = (System.nanoTime() - startTime) / 1000000;
                bmodel.synchronizationHelper.mTableList.put("temp table update**", endTime + "");
            }
            return next_method;
        }

        @Override
        protected void onPostExecute(SynchronizationHelper.NEXT_METHOD response) {
            super.onPostExecute(response);
            alertDialog.dismiss();
            if (response == SynchronizationHelper.NEXT_METHOD.DIGITAL_CONTENT_AVALILABLE) {
                bmodel.configurationMasterHelper.setAmazonS3Credentials();
                initializeTransferUtility();
                downloaderThread = new DownloaderThreadNew(thisActivity,
                        activityHandler, bmodel.getDigitalContentURLS(),
                        bmodel.userMasterHelper.getUserMasterBO()
                                .getUserid(), transferUtility);
                downloaderThread.start();
            } else {

                checkLogin();
                finish();
                System.gc();

            }
        }
    }

    /**
     * call the next method from given response
     *
     * @param response - Next method to call
     */
    private void callNextTask(SynchronizationHelper.NEXT_METHOD response) {
        if (response == SynchronizationHelper.NEXT_METHOD.DISTRIBUTOR_DOWNLOAD) {
            new InitiateDistributorDownload().execute();
        } else if (response == SynchronizationHelper.NEXT_METHOD.LAST_VISIT_TRAN_DOWNLOAD) {
            new InitiateRetailerDownload().execute();
        } else if (response == SynchronizationHelper.NEXT_METHOD.SIH_DOWNLOAD) {
            new SihDownloadTask().execute();
        } else if (response == SynchronizationHelper.NEXT_METHOD.DIGITAL_CONTENT_AVALILABLE
                || response == SynchronizationHelper.NEXT_METHOD.DEFAULT) {
            new LoadData().execute();
        }
    }

    /**
     * Server error is coming like 404 error  and IsMantory is 1 for corresponding url delete
     * all table and show alert message to please redownload
     *
     * @param bundle - bundle
     */
    private void reDownloadAlert(Bundle bundle) {
        String errorDownlodCode = bundle
                .getString(SynchronizationHelper.ERROR_CODE);
        String errorDownloadMessage = bmodel.synchronizationHelper
                .getErrormessageByErrorCode().get(errorDownlodCode);
        if (errorDownloadMessage != null) {
            bmodel.showAlert(errorDownloadMessage, 0);
        }
        new DeleteTables().execute();

        alertDialog.dismiss();
        bmodel.showAlert(getResources().getString(R.string.please_redownload_data), 0);
        editTextPassword.setText("");
        editTextUserName.setText("");
        editTextUserName.requestFocus();
        bool = false;
    }

    class ForgetPassword extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            final String token = bmodel.synchronizationHelper.updateAuthenticateTokenWithoutPassword();
            return token;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (!s.equals("")) {
                ResetPasswordDialog dialog = new ResetPasswordDialog(LoginScreen.this);
                dialog.show();
            } else {
                bmodel.showAlert(getResources().getString(R.string.token_expired), 0);
            }
        }
    }


}

