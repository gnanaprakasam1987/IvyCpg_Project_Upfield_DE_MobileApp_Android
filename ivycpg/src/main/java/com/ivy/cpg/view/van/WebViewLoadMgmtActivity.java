package com.ivy.cpg.view.van;


import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.ApplicationConfigs;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.HomeScreenActivity;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by mansoor.k on 27-12-2017.
 */
public class WebViewLoadMgmtActivity extends IvyBaseActivityNoActionBar implements ApplicationConfigs {
    WebView webView;
    String token = "";
    HashMap<String, String> reqHeader;
    BusinessModel bmodel;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        try {
            if (bmodel.configurationMasterHelper.MVPTheme == 0) {
                super.setTheme(bmodel.configurationMasterHelper.getMVPTheme());
            } else {
                super.setTheme(bmodel.configurationMasterHelper.MVPTheme);
            }
            if (bmodel.configurationMasterHelper.fontSize.equals("")) {
                setFontStyle(bmodel.configurationMasterHelper.getFontSize());
            } else {
                setFontStyle(bmodel.configurationMasterHelper.fontSize);
            }

        } catch (Exception e) {
            Commons.printException(e);
        }


        Configuration config = new Configuration();
        Locale locale = config.locale;
        if (!Locale.getDefault().equals(
                sharedPrefs.getString("languagePref", LANGUAGE))) {
            locale = new Locale(sharedPrefs.getString("languagePref", LANGUAGE).substring(0, 2));
            Locale.setDefault(locale);
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());
        }
        setContentView(R.layout.activity_webview_plan);

        webView = (WebView) findViewById(R.id.webview);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        // token=getIntent().getExtras().getString("token");

        if (toolbar != null) {

            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(getIntent().getStringExtra("screentitle"));
//            // Used to on / off the back arrow icon
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//           // Used to remove the app logo actionbar icon and set title as home
//          // (title support click)
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


        bmodel.reportHelper.downloadWebViewPlanAuthUrl("WEBVIEW_LD_MGMT");
        if (!bmodel.reportHelper.getWebViewAuthUrl().equals(""))
            new DownloadToken().execute();
        else
            Toast.makeText(WebViewLoadMgmtActivity.this, getResources().getString(R.string.error_message_bad_url), Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        if (item.getItemId() == android.R.id.home) {
            Intent in = new Intent(WebViewLoadMgmtActivity.this, LoadManagementScreen.class);
            startActivity(in);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    AlertDialog alertDialog;

    class DownloadToken extends AsyncTask<String, Void, String> {

        private int downloadStatus = 0;
        AlertDialog.Builder builder;


        protected void onPreExecute() {

            builder = new AlertDialog.Builder(WebViewLoadMgmtActivity.this);

            customProgressDialog(builder, getResources().getString(R.string.Authenticating));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            return bmodel.synchronizationHelper.downloadSOVisitPlanToken(bmodel.reportHelper.getWebViewAuthUrl());

        }


        @Override
        protected void onPostExecute(String token) {
            super.onPostExecute(token);
            if ((alertDialog != null) && alertDialog.isShowing()) {
                alertDialog.dismiss();
            }

            if (!token.equals("")) {
                bmodel.reportHelper.downloadWebViewPlanUrl("WEBVIEW_LD_MGMT");
                if (!bmodel.reportHelper.getWebViewPlanUrl().equals("")) {

                    reqHeader = new HashMap<>();
                    reqHeader.put("SECURITY_TOKEN_KEY", token);

                    webView.getSettings().setJavaScriptEnabled(true);
                    webView.getSettings().setAllowFileAccess(false);
                    webView.getSettings().setDomStorageEnabled(true);
                    webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
                    webView.setWebChromeClient(new WebChromeClient());
                    webView.loadUrl(bmodel.reportHelper.getWebViewPlanUrl(), reqHeader);
                    webView.setWebViewClient(new WebViewClient());
                } else {
                    Toast.makeText(WebViewLoadMgmtActivity.this, getResources().getString(R.string.error_message_bad_url), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(WebViewLoadMgmtActivity.this, R.string.Error_Authentication, Toast.LENGTH_LONG).show();
            }

        }


    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }

    public void setFontStyle(String font) {
        if (font.equalsIgnoreCase("Small")) {
            getTheme().applyStyle(R.style.FontStyle_Small, true);
        } else if (font.equalsIgnoreCase("Medium")) {
            getTheme().applyStyle(R.style.FontStyle_Medium, true);
        } else if (font.equalsIgnoreCase("Large")) {
            getTheme().applyStyle(R.style.FontStyle_Large, true);
        } else {
            getTheme().applyStyle(R.style.FontStyle_Small, true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if ((alertDialog != null) && alertDialog.isShowing())
            alertDialog.dismiss();
        alertDialog = null;
    }

}
