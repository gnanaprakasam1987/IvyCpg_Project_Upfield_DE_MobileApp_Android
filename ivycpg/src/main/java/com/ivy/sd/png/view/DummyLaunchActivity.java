package com.ivy.sd.png.view;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.crashlytics.android.Crashlytics;
import com.ivy.cpg.view.login.LoginScreen;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.BuildConfig;
import com.ivy.sd.png.model.ApplicationConfigs;
import com.ivy.ui.activation.view.ActivationActivity;

import io.fabric.sdk.android.Fabric;

/**
 * Created by subramanian on 4/28/16.
 * This activity has no UI, it is responsible for redirecting to activation screen
 * or login screen based on the activation status.
 */
public class DummyLaunchActivity extends Activity {

    private SharedPreferences appPreferences;
    private String appUrl;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!BuildConfig.DEBUG)
            Fabric.with(this, new Crashlytics());
        DBUtil.isEncrypted = ApplicationConfigs.isEncrypted;
        if (ApplicationConfigs.withActivation && BuildConfig.FLAVOR.equals("aws")) {
            appPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            appUrl = appPreferences.getString("appUrlNew", "");
            if (appUrl.equals("")) {
                Intent in = new Intent(DummyLaunchActivity.this,
                        ActivationActivity.class);
                in.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(in);
                finish();
                return;
            }
        }
        Intent in = new Intent(DummyLaunchActivity.this,
                LoginScreen.class);
        in.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(in);
        finish();
    }
}
