package com.ivy.cpg.view.login;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ivy.cpg.view.sfdc.NativeKeyImpl;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.auth.AuthenticatorService;
import com.ivy.cpg.view.sfdc.SalesforceAuthenticateActivity;

public class LoginScreen extends LoginBaseActivity {


    private BusinessModel bmodel;
    private boolean syncDone;
    private String refreshToken;
    private String userId;
    private String instServer;
    private String clientSecret;
    private String username;
    Button sfdc_login_btn;


    @Override
    public void initPresenter() {
        loginPresenter = new LoginPresenterImpl(getApplicationContext());
        loginPresenter.setView(this);
    }

    @Override
    public void setDefaults(boolean clearUserName) {

    }

    @Override
    public void handleForgotPassword() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_sfdc);
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        SalesforceSDKManager.initNative(getApplicationContext(), new NativeKeyImpl(), LoginScreen.class);

        sfdc_login_btn = findViewById(R.id.sfdc_login_btn);
        syncDone = bmodel.userMasterHelper.getSyncStatus();

        init();

        checkAccount();
        if (!syncDone) {
            if (SynchronizationHelper.access_token != null && !SynchronizationHelper.access_token.equals("")) {
                sfdc_login_btn.setVisibility(View.GONE);

                loginPresenter.onLoginClick();
            } else {
                sfdc_login_btn.setVisibility(View.VISIBLE);
                removeAccounts();
            }
        } else {
            sfdc_login_btn.setVisibility(View.GONE);
            loginPresenter.onLoginClick();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        loginPresenter.reloadActivity();
    }

    private void removeAccounts() {
        AccountManager mgr = AccountManager.get(getApplicationContext());
        Account[] accounts = AccountManager.get(getApplicationContext()).getAccountsByType(SalesforceSDKManager.getInstance().getAccountType());

        if (accounts.length == 0) return;

        final Handler handler = new Handler();


        if (android.os.Build.VERSION.SDK_INT >= 22) {
            // use new account manager code
            AccountManagerCallback<Bundle> callback = new AccountManagerCallback<Bundle>() {
                @Override
                public void run(AccountManagerFuture<Bundle> arg0) {
                    // nada
                }
            };
            for (Account a : accounts) {
                mgr.removeAccount(a, this, callback, handler);
            }
        } else {
            //noinspection deprecation
            // use old account manager code, the above comment will omit the warning.
            AccountManagerCallback<Boolean> callback = new AccountManagerCallback<Boolean>() {
                @Override
                public void run(AccountManagerFuture<Boolean> arg0) {
                    // nada
                }
            };
            for (Account a : accounts) {
                mgr.removeAccount(a, callback, handler);
            }
        }

    }

    private void checkAccount() {

        String passcodeHash = SalesforceSDKManager.getInstance().getPasscodeHash();

        Account[] account = AccountManager.get(getApplicationContext()).getAccountsByType(SalesforceSDKManager.getInstance().getAccountType());
        AccountManager mgr = AccountManager.get(getApplicationContext());
        if (account.length > 0) {
            bmodel.passwordTemp = SalesforceSDKManager.decryptWithPasscode(mgr.getPassword(account[0]), passcodeHash);
            refreshToken = SalesforceSDKManager.decryptWithPasscode(mgr.getUserData(account[0], AccountManager.KEY_AUTHTOKEN), passcodeHash);
            String clientId = SalesforceSDKManager.decryptWithPasscode(mgr.getUserData(account[0], AuthenticatorService.KEY_CLIENT_ID), passcodeHash);
            instServer = SalesforceSDKManager.decryptWithPasscode(mgr.getUserData(account[0], AuthenticatorService.KEY_INSTANCE_URL), passcodeHash);
            userId = SalesforceSDKManager.decryptWithPasscode(mgr.getUserData(account[0], AuthenticatorService.KEY_USER_ID), passcodeHash);
            username = SalesforceSDKManager.decryptWithPasscode(mgr.getUserData(account[0], AuthenticatorService.KEY_USERNAME), passcodeHash);
            bmodel.getAppDataProvider().setUserName(username);
            bmodel.getAppDataProvider().setUserPassword(bmodel.passwordTemp);
            bmodel.userNameTemp = username;
            String encClientSecret = mgr.getUserData(account[0], AuthenticatorService.KEY_CLIENT_SECRET);
            clientSecret = null;
            if (encClientSecret != null) {
                clientSecret = SalesforceSDKManager.decryptWithPasscode(encClientSecret, passcodeHash);
            }

            //SynchronizationHelper.CLIENT_ID = clientId;
            SynchronizationHelper.CLIENT_SECRET = clientSecret;
            SynchronizationHelper.USER_NAME = userId;
            //SynchronizationHelperNew.PASSWORD="Hellothere123#";
            SynchronizationHelper.access_token = refreshToken;
            SynchronizationHelper.instance_url = instServer;
        }
        //resumeCount++;
    }


    public void loginSFDC(View v) {
        Intent i = new Intent(this, SalesforceAuthenticateActivity.class);
        startActivityForResult(i, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (1 == resultCode) {
            if (data.getBooleanExtra("Authenticated", false)) {
                loginPresenter.onLoginClick();
            } else {
                Toast.makeText(getApplicationContext(), "Authentication failed. Please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
