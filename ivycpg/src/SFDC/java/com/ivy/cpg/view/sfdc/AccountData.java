package com.ivy.cpg.view.sfdc;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import com.salesforce.androidsdk.app.SalesforceSDKManager;

public class AccountData {

    private Context mContext;

    public AccountData(Context context) {
        mContext = context;
    }

    public boolean isUserAvailable() {
        Account[] account = AccountManager.get(mContext.getApplicationContext()).getAccountsByType(SalesforceSDKManager.getInstance().getAccountType());
        return account != null && account.length > 0;
    }
}
