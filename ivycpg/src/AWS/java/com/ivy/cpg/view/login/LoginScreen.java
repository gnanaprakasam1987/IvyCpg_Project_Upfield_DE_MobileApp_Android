package com.ivy.cpg.view.login;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ivy.apptutoriallibrary.AppTutorialPlugin;
import com.ivy.sd.png.asean.view.BuildConfig;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.ApkDownloaderThread;
import com.ivy.sd.png.model.ApplicationConfigs;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.cpg.view.settings.About;
import com.ivy.cpg.view.login.password.ChangePasswordActivity;
import com.ivy.sd.png.view.PasswordLockDialogFragment;
import com.ivy.cpg.view.login.password.ResetPasswordDialog;
import com.ivy.cpg.view.settings.UserSettingsActivity;
import com.ivy.utils.AppUtils;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FileUtils;
import com.ivy.utils.FontUtils;

import org.json.JSONArray;

import java.io.File;
import java.io.FilenameFilter;


public class LoginScreen extends LoginBaseActivity
        implements ApplicationConfigs, LoginContract.LoginView, PasswordLockDialogFragment.UpdatePasswordDialogInterface {

    private BusinessModel businessModel;

    private EditText editTextUserName, editTextPassword;

    private TextView mForgotPasswordTV;

    private ProgressDialog progressDialog;


    @Override
    public void initPresenter() {
        loginPresenter = new LoginPresenterImpl(getApplicationContext());
        loginPresenter.setView(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        businessModel = (BusinessModel) getApplicationContext();

        setContentView(R.layout.loginscreen);

        /* Check the date if expiry is enabled.*/
        if (ApplicationConfigs.expiryEnable) {
            if ((DateTimeUtils.compareDate(ApplicationConfigs.expiryDate,
                    DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), "yyyy/MM/dd") < 0))
                finish();
        }


        AppTutorialPlugin.getInstance().setCurrentScreen("login");


        //progressDialog = null;

        mForgotPasswordTV = findViewById(R.id.txtResetPassword);
        mForgotPasswordTV.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.LIGHT));

        editTextUserName = findViewById(R.id.EditText011);
        editTextUserName.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.LIGHT));

        editTextPassword = findViewById(R.id.EditText022);
        editTextPassword.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.LIGHT));

        Button buttonLogin = findViewById(R.id.loginButton);
        buttonLogin.setTypeface(FontUtils.getFontBalooHai(this,FontUtils.FontType.REGULAR));

        loginPresenter.getSupportNo();

        updateImageViews();

        ImageView btn_setting = findViewById(R.id.iv_setting);
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
        version.setText(getResources().getString(R.string.version) + AppUtils.getApplicationVersionName(this));
        version.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.LIGHT));

        LinearLayout ll_footer = (LinearLayout) findViewById(R.id.ll_footer);
        ll_footer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(LoginScreen.this, About.class));

            }
        });

        init();

        if (BuildConfig.DEBUG && BuildConfig.IS_AUTO_LOGIN_ENABLED) {
            editTextUserName.setText(BuildConfig.TEST_USER_NAME);
            editTextPassword.setText(BuildConfig.TEST_PASSWORD);
            onLoginClick(buttonLogin);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        businessModel = (BusinessModel) getApplicationContext();
        businessModel.setContext(this);
        loginPresenter.reloadActivity();

        try {
            String data = FileUtils.readFile(this, DataMembers.APP_TUTORIAL + ".txt", DataMembers.APP_TUTORIAL, getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "");
            JSONArray jsonArray = new JSONArray(data);
            AppTutorialPlugin.getInstance().setAppTutorialJsonArray(jsonArray);
        }
        catch (Exception ex){
            Commons.printException(ex);
        }


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
    public void goToChangePwd() {
        startActivity(new Intent(LoginScreen.this,
                ChangePasswordActivity.class));
    }

    @Override
    public void resetPassword() {
        setDefaults(true);
        editTextUserName.requestFocus();
        Intent in = new Intent(
                LoginScreen.this,
                ChangePasswordActivity.class);
        in.putExtra("resetpassword", true);
        startActivity(in);
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



    public boolean onOptionsItemSelected(MenuItem item) {
        int i1 = item.getItemId();
        if (i1 == R.id.menu_settings) {
            Intent i = new Intent(LoginScreen.this, UserSettingsActivity.class);
            startActivity(i);

        }
        return true;
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
        if (mForgotPasswordTV != null) {
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
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void callForgetPassDialog() {
        if (!editTextUserName.getText().toString().equals("")) {
            businessModel.userNameTemp = editTextUserName.getText().toString();
            loginPresenter.callForgetPassword();
        } else {
            editTextUserName.setError(getResources().getString(R.string.enter_username));
        }
    }




    @Override
    public void showAppUpdateAlert(String msg) {

        new CommonDialog(getApplicationContext(), this, "", msg, false, getResources().getString(R.string.ok), new CommonDialog.PositiveClickListener() {
            @Override
            public void onPositiveButtonClick() {
                Commons.printInformation(businessModel.getUpdateURL());
                Thread downloaderThread = new ApkDownloaderThread(
                        LoginScreen.this, fileDownloadHandler, businessModel
                        .getUpdateURL(), false,
                        ApkDownloaderThread.APK_DOWNLOAD);
                downloaderThread.start();
            }
        }).show();
    }

    @Override
    public void showDeviceLockedDialog() {
        new CommonDialog(getApplicationContext(), LoginScreen.this,
                getResources().getString(R.string.deviceId_change_msg_title),
                getResources().getString(R.string.deviceId_change_msg),
                false, getResources().getString(R.string.yes),
                getResources().getString(R.string.no),
                new CommonDialog.PositiveClickListener() {
                    @Override
                    public void onPositiveButtonClick() {

                        loginPresenter.callInitialAuthentication(true);

                    }
                }, new CommonDialog.negativeOnClickListener() {
            @Override
            public void onNegativeButtonClick() {


            }
        }).show();
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
    public void sendUserNotExistToHandler() {
        this.getHandler().sendEmptyMessage(DataMembers.NOTIFY_NOT_USEREXIST);
    }



    public void clearAppUrl() {
        SharedPreferences.Editor editor = PreferenceManager
                .getDefaultSharedPreferences(LoginScreen.this)
                .edit();
        editor.putString("appUrlNew", "");
        editor.putString("application", "");
        editor.putString("activationKey", "");
        editor.apply();
    }

    @Override
    public void setDefaults(boolean clearUserName) {
        if(clearUserName) {
            editTextUserName.setText("");
            editTextUserName.requestFocus();
        }
        editTextPassword.setText("");
        editTextPassword.requestFocus();

    }

    @Override
    public void handleForgotPassword() {
        if (mForgotPasswordTV != null) {
            showForgotPassword();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

