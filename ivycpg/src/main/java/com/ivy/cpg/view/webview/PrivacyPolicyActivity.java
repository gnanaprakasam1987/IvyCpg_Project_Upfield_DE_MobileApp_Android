package com.ivy.cpg.view.webview;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ivy.core.IvyConstants;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

/**
 * Created by ramkumard on 31/1/19.
 * To display company privacy policy using webview.
 */

public class PrivacyPolicyActivity extends IvyBaseActivityNoActionBar {

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getWindow().requestFeature(Window.FEATURE_PROGRESS);

        super.onCreate(savedInstanceState);

        BusinessModel bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        try {
            if (bmodel.configurationMasterHelper.MVPTheme == 0) {
                super.setTheme(bmodel.configurationMasterHelper.getMVPTheme());
            } else {
                super.setTheme(bmodel.configurationMasterHelper.MVPTheme);
            }
        } catch (Exception e) {
            Commons.printException(e);
        }


        setContentView(R.layout.activity_webview_plan);


        // Makes Progress bar Visible
        getWindow().setFeatureInt( Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);

        WebView webView = findViewById(R.id.webview);
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            // Set title to toolbar
            getSupportActionBar().setTitle(
                    getResources().getString(R.string.privacy_policy));
            // Used to on / off the back arrow icon
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Used to remove the app logo actionbar icon and set title as home
            // (title support click)
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.loadUrl(IvyConstants.PRIVACY_POLICY_URL);
        webView.setWebViewClient(new WebViewClient());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
