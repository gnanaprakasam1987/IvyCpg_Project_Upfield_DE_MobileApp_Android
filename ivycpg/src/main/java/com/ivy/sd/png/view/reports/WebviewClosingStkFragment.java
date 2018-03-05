/*
package com.ivy.sd.png.view.reports;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import java.util.HashMap;
import java.util.Locale;

public class WebviewClosingStkFragment extends IvyBaseFragment {

    private BusinessModel bmodel;
    private View view;
    private WebView webView;



    private AlertDialog alertDialog;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


        view = inflater.inflate(R.layout.fragment_archival_report, container, false);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        try {
            int theme = bmodel.configurationMasterHelper.getMVPTheme();
            super.getActivity().setTheme(theme);

            String font = bmodel.configurationMasterHelper.getFontSize();
            setFontStyle(font);

        } catch (Exception e) {
            Commons.printException(e);
        }

        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        Configuration config = new Configuration();
        Locale locale = config.locale;
        if (!Locale.getDefault().equals(
                sharedPrefs.getString("languagePref", LANGUAGE))) {
            locale = new Locale(sharedPrefs.getString("languagePref", LANGUAGE).substring(0, 2));
            Locale.setDefault(locale);
            config.locale = locale;
            getActivity().getBaseContext().getResources().updateConfiguration(config,
                    getActivity().getBaseContext().getResources().getDisplayMetrics());
        }

        webView = (WebView) view.findViewById(R.id.webview);

        bmodel.reportHelper.downloadWebViewClosingStkUrl();
        if (!bmodel.reportHelper.getWebViewClosingStkUrl().equals("")) {
            new DownloadToken().execute();
        }else
            Toast.makeText(getActivity(), getResources().getString(R.string.error_message_bad_url), Toast.LENGTH_LONG).show();

        return view;
    }

    class DownloadToken extends AsyncTask<String, Void, String> {

        AlertDialog.Builder builder;


        protected void onPreExecute() {

            builder = new AlertDialog.Builder(getActivity());

            customProgressDialog(builder,  getResources().getString(R.string.Authenticating));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            return bmodel.synchronizationHelper.downloadSOClosingStockToken(bmodel.reportHelper.getWebViewClosingStkUrl());

        }


        @Override
        protected void onPostExecute(String token) {
            super.onPostExecute(token);
            if ((alertDialog != null) && alertDialog.isShowing()) {
                alertDialog.dismiss();
            }

            if (!token.equals("")) {

            } else {
                Toast.makeText(getActivity(), R.string.Error_Authentication, Toast.LENGTH_LONG).show();
            }

        }


    }

    public void setFontStyle(String font) {
        if (font.equalsIgnoreCase("Small")) {
            getActivity().getTheme().applyStyle(R.style.FontStyle_Small, true);
        } else if (font.equalsIgnoreCase("Medium")) {
            getActivity().getTheme().applyStyle(R.style.FontStyle_Medium, true);
        } else if (font.equalsIgnoreCase("Large")) {
            getActivity().getTheme().applyStyle(R.style.FontStyle_Large, true);
        } else {
            getActivity().getTheme().applyStyle(R.style.FontStyle_Small, true);
        }
    }
}
*/
