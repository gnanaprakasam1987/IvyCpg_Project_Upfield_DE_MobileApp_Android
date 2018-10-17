package com.ivy.cpg.view.sfdc;

import com.salesforce.androidsdk.analytics.security.Encryptor;
import com.salesforce.androidsdk.app.SalesforceSDKManager;

/**
 * Created by mayuri.v on 9/8/2017.
 */
public class NativeKeyImpl implements SalesforceSDKManager.KeyInterface {
    @Override
    public String getKey(String name) {
        return Encryptor.hash(name + "12s9adpahk;n12-97sdainkasd=012", name + "12kl0dsakj4-cxh1qewkjasdol8");
    }
}
