package com.ivy.cpg.view.sfdc;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;

import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.auth.AuthenticatorService;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.ui.SalesforceActivity;

/**
 * Created by mayuri.v on 9/13/2017.
 */
public class SalesforceAuthenticateActivity extends SalesforceActivity {
    private BusinessModel bmodel;
    private String refreshToken, clientId, userId, instServer, clientSecret, username;

    //private int resumeCount = 0;
    @Override
    public void onResume(RestClient client) {
        //Commons.print("Client info "+client.getClientInfo().accountName+", "+client.getClientInfo());
        checkAccount();
    }

    private void checkAccount() {
        String passcodeHash = SalesforceSDKManager.getInstance().getPasscodeHash();

        Account[] account = AccountManager.get(getApplicationContext()).getAccountsByType(SalesforceSDKManager.getInstance().getAccountType());
        AccountManager mgr = AccountManager.get(getApplicationContext());

        if (account != null && account.length > 0) {
            bmodel.passwordTemp = SalesforceSDKManager.decryptWithPasscode(mgr.getPassword(account[0]), passcodeHash);
            refreshToken = SalesforceSDKManager.decryptWithPasscode(mgr.getUserData(account[0], AccountManager.KEY_AUTHTOKEN), passcodeHash);
            clientId = SalesforceSDKManager.decryptWithPasscode(mgr.getUserData(account[0], AuthenticatorService.KEY_CLIENT_ID), passcodeHash);
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
            Intent i = new Intent();
            i.putExtra("Authenticated", true);
            setResult(1, i);
            finish();
        } else {
            Intent i = new Intent();
            i.putExtra("Authenticated", false);
            setResult(1, i);
            finish();
        }

        //resumeCount++;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
    }
}
