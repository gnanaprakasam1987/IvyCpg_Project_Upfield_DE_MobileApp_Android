package com.ivy.cpg.view.sfdc;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.ivy.sd.png.provider.SynchronizationHelper;
import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.auth.AuthenticatorService;
import com.salesforce.androidsdk.auth.HttpAccess;
import com.salesforce.androidsdk.auth.OAuth2;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class RefreshAuthTokenAsync extends AsyncTask<String, String, String> {

    private Account account;
    private final String TAG = "Auth..Ser..:getAuthT..";
    private AccountManager mgr;
    private String loginServer, clientId, refreshToken, clientSecret, instServer, passcodeHash, username, userId, orgId;
    private SynchronizationHelper.VolleyResponseCallbackInterface volleySFDCnormalDownload;
    private Context context;

    public RefreshAuthTokenAsync(Context context, SynchronizationHelper.VolleyResponseCallbackInterface volleySFDCnormalDownload) {
        this.context=context;
        this.volleySFDCnormalDownload = volleySFDCnormalDownload;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Account[] accountArr = AccountManager.get(context).getAccountsByType(SalesforceSDKManager.getInstance().getAccountType());
        account = accountArr[0];
        mgr = AccountManager.get(context);

        passcodeHash = SalesforceSDKManager.getInstance().getPasscodeHash();
        refreshToken = SalesforceSDKManager.decryptWithPasscode(mgr.getPassword(account), passcodeHash);
        loginServer = SalesforceSDKManager.decryptWithPasscode(mgr.getUserData(account, AuthenticatorService.KEY_LOGIN_URL), passcodeHash);
        clientId = SalesforceSDKManager.decryptWithPasscode(mgr.getUserData(account, AuthenticatorService.KEY_CLIENT_ID), passcodeHash);
        instServer = SalesforceSDKManager.decryptWithPasscode(mgr.getUserData(account, AuthenticatorService.KEY_INSTANCE_URL), passcodeHash);
        userId = SalesforceSDKManager.decryptWithPasscode(mgr.getUserData(account, AuthenticatorService.KEY_USER_ID), passcodeHash);
        orgId = SalesforceSDKManager.decryptWithPasscode(mgr.getUserData(account, AuthenticatorService.KEY_ORG_ID), passcodeHash);
        username = SalesforceSDKManager.decryptWithPasscode(mgr.getUserData(account, AuthenticatorService.KEY_USERNAME), passcodeHash);
        final String encClientSecret = mgr.getUserData(account, AuthenticatorService.KEY_CLIENT_SECRET);
        clientSecret = null;
        if (encClientSecret != null) {
            clientSecret = SalesforceSDKManager.decryptWithPasscode(encClientSecret, passcodeHash);
        }
    }


    @Override
    protected String doInBackground(String... strings) {
        try {
            final OAuth2.TokenEndpointResponse tr = OAuth2.refreshAuthToken(HttpAccess.DEFAULT, new URI(loginServer), clientId, refreshToken, clientSecret);

            // Handle the case where the org has been migrated to a new instance, or has turned on my domains.
            if (!instServer.equalsIgnoreCase(tr.instanceUrl)) {
                mgr.setUserData(account, AuthenticatorService.KEY_INSTANCE_URL, SalesforceSDKManager.encryptWithPasscode(tr.instanceUrl, passcodeHash));
            }

            // Update auth token in account.
            mgr.setUserData(account, AccountManager.KEY_AUTHTOKEN, SalesforceSDKManager.encryptWithPasscode(tr.authToken, passcodeHash));

            SynchronizationHelper.access_token = tr.authToken;

        } catch (IOException | URISyntaxException e) {
            Log.w(TAG, "", e);
            try {
                throw new NetworkErrorException(e);
            } catch (NetworkErrorException e1) {
                e1.printStackTrace();
            }

        } catch (OAuth2.OAuthFailedException e) {

        }
        return "Success";
    }

    @Override
    protected void onPostExecute(String responseStatus) {
        super.onPostExecute(responseStatus);

        if (responseStatus.equals("Success")) {
            volleySFDCnormalDownload.onSuccess("Success");
        }
    }
}
