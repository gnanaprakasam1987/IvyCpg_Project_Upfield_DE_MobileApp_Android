package com.ivy.sd.png.view.reports;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferType;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.DownloaderThreadNew;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.sd.print.CommonPrintPreviewActivity;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by Rajkumar on 10/1/18.
 */

public class WebViewArchivalReportFragment extends IvyBaseFragment {

    private BusinessModel bmodel;
    private View view;
    private WebView webView;
    private HashMap<String, String> reqHeader;
    private DownloaderThreadNew downloaderThread;

    private TransferUtility transferUtility;
    private AmazonS3Client s3;

    private AlertDialog alertDialog;


    private String transactionId;

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

        bmodel.reportHelper.downloadWebViewArchAuthUrl();
        if (!bmodel.reportHelper.getWebViewAuthUrl().equals(""))
            new DownloadToken().execute();
        else
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
            return bmodel.synchronizationHelper.downloadSOVisitPlanToken(bmodel.reportHelper.getWebViewAuthUrl());

        }


        @Override
        protected void onPostExecute(String token) {
            super.onPostExecute(token);
            if ((alertDialog != null) && alertDialog.isShowing()) {
                alertDialog.dismiss();
            }

            if (!token.equals("")) {
                bmodel.reportHelper.downloadWebViewArchUrl();
                if (!bmodel.reportHelper.getWebViewArchUrl().equals("")) {

                    reqHeader = new HashMap<>();
                    reqHeader.put("SECURITY_TOKEN_KEY", token);

                    webView.getSettings().setJavaScriptEnabled(true);
                    webView.getSettings().setAllowFileAccess(false);
                    webView.getSettings().setDomStorageEnabled(true);
                    webView.addJavascriptInterface(new WebAppInterface(getActivity()), "Android");

                    webView.loadUrl(bmodel.reportHelper.getWebViewArchUrl(), reqHeader);
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.error_message_bad_url), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getActivity(), R.string.Error_Authentication, Toast.LENGTH_LONG).show();
            }

        }


    }

    public class WebAppInterface {
        Context mContext;

        WebAppInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void downloadPrintFile(String filePath,String id) {
            transactionId=id;
            bmodel.reportHelper.prepareArchiveFileDownload(filePath);
            if(bmodel.getDigitalContentURLS().size()>0) {
                bmodel.configurationMasterHelper.setAmazonS3Credentials();
                initializeTransferUtility();
                downloaderThread = new DownloaderThreadNew(getActivity(),
                        activityHandler, bmodel.getDigitalContentURLS(),
                        bmodel.userMasterHelper.getUserMasterBO()
                                .getUserid(), transferUtility);
                downloaderThread.start();
            }
            else
            {
                Toast.makeText(getActivity(), getResources().getString(R.string.error_message_bad_url), Toast.LENGTH_LONG).show();
            }
        }
    }

    private Handler activityHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case DataMembers.MESSAGE_DOWNLOAD_STARTED:
                    // obj will contain a String representing the file name
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    customProgressDialog(builder,  getActivity().getResources().getString(R.string.progress_dialog_title_downloading));
                    alertDialog = builder.create();

                    Message cancelMsg = Message.obtain(this,
                            DataMembers.MESSAGE_DOWNLOAD_CANCELED);
                    alertDialog.setCancelMessage(cancelMsg);
                    alertDialog.setCanceledOnTouchOutside(false);

                    alertDialog.setCancelable(true);
                    alertDialog.show();

                    break;


                case DataMembers.MESSAGE_UPDATE_PROGRESS_BAR:
                   /* if (alertDialog != null) {
                        int currentProgress = msg.arg1;
                        progressDialog.setProgress(currentProgress);
                    }*/
                    break;

                case DataMembers.MESSAGE_DOWNLOAD_COMPLETE_DC:
                    dismissCurrentProgressDialog();
                    Toast.makeText(getActivity(), getResources().getString(R.string.downloaded_successfully), Toast.LENGTH_LONG).show();

                    bmodel.invoiceNumber=transactionId;
                    bmodel.readBuilder(StandardListMasterConstants.PRINT_FILE_INVOICE+bmodel.invoiceNumber+".txt");
                    Intent intent = new Intent();
                    intent.setClass(getActivity(),
                            CommonPrintPreviewActivity.class);
                    intent.putExtra("IsUpdatePrintCount", true);
                    intent.putExtra("isHomeBtnEnable", true);
                    intent.putExtra("IsFromReport", true);
                    startActivity(intent);

                    break;


                case DataMembers.MESSAGE_DOWNLOAD_CANCELED:
                    clearAmazonDownload();
                    dismissCurrentProgressDialog();
                    Toast.makeText(getActivity(), getResources().getString(R.string.user_message_download_canceled), Toast.LENGTH_LONG).show();

                    break;
                case DataMembers.MESSAGE_ENCOUNTERED_ERROR_DC:
                    // obj will contain a string representing the error message
                    dismissCurrentProgressDialog();
                    Toast.makeText(getActivity(), getResources().getString(R.string.error_message_general), Toast.LENGTH_LONG).show();

                    break;
            }
        }
    };

    private void dismissCurrentProgressDialog() {
        if (alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
    }

    private void clearAmazonDownload() {
        if (transferUtility != null) {
            transferUtility.cancelAllWithType(TransferType.DOWNLOAD);
        }
    }

    private void initializeTransferUtility() {
        System.setProperty
                (SDKGlobalConfiguration.ENABLE_S3_SIGV4_SYSTEM_PROPERTY, "true");
        BasicAWSCredentials myCredentials = new BasicAWSCredentials(ConfigurationMasterHelper.ACCESS_KEY_ID,
                ConfigurationMasterHelper.SECRET_KEY);
        AmazonS3Client s3 = new AmazonS3Client(myCredentials);
        s3.setEndpoint(DataMembers.S3_BUCKET_REGION);
        transferUtility = new TransferUtility(s3, getActivity().getApplicationContext());
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

    @Override
    public void onPause() {
        super.onPause();

        if ((alertDialog != null) && alertDialog.isShowing())
            alertDialog.dismiss();
        alertDialog = null;
    }

}
