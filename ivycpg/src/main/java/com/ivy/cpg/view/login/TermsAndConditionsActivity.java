package com.ivy.cpg.view.login;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.ivy.cpg.view.homescreen.HomeScreenActivity;
import com.ivy.cpg.view.sync.UploadHelper;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.NetworkUtils;

/**
 * Created by ramkumard on 8/2/19.
 * To display Terms and Conditions, in which user needs to accept it.
 */

public class TermsAndConditionsActivity extends IvyBaseActivityNoActionBar {

    private AlertDialog alertDialog;
    private BusinessModel bmodel;
    private String fromScreen = "";

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        setContentView(R.layout.activity_terms_conditions);

        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            // Set title to toolbar
            getSupportActionBar().setTitle(
                    getResources().getString(R.string.terms_conditions));
            // Used to on / off the back arrow icon
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            // Used to remove the app logo actionbar icon and set title as home
            // (title support click)
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        fromScreen = getIntent().getStringExtra("fromScreen");
        TextView tv_content = findViewById(R.id.tv_terms_cond);
        String htmlContent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            htmlContent = Html.fromHtml(LoginHelper.getInstance(this).getTermsContent(), Html.FROM_HTML_MODE_COMPACT).toString();
            tv_content.setText(Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_COMPACT));
        } else {
            htmlContent = Html.fromHtml(LoginHelper.getInstance(this).getTermsContent()).toString();
            tv_content.setText(Html.fromHtml(htmlContent));
        }
        tv_content.setMovementMethod(LinkMovementMethod.getInstance());//For auto-link clickable

        Button btn_cancel = findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAffinity();
            }
        });
        Button btn_accept = findViewById(R.id.btn_accept);
        btn_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkUtils.isNetworkConnected(TermsAndConditionsActivity.this))
                    new UploadTermsAccepted().execute();
                 else
                    bmodel.showAlert(getResources().getString(
                            R.string.please_connect_to_internet), 0);
            }
        });
    }


    class UploadTermsAccepted extends AsyncTask<String, Void, Boolean> {
        protected void onPreExecute() {

            AlertDialog.Builder builder = new AlertDialog.Builder(TermsAndConditionsActivity.this);

            customProgressDialog(builder,
                    getResources().
                            getString(R.string.uploading_data));
            alertDialog = builder.create();
            alertDialog.show();

        }

        @Override
        protected Boolean doInBackground(String... params) {

                UploadHelper mUploadHelper = UploadHelper.getInstance(TermsAndConditionsActivity.this);
                String res = mUploadHelper.updateTermsAccepted();

                return "1".equals(res);
            }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            alertDialog.dismiss();
            if (result) {
                updateTermsAccepted();
                showAlert(getResources().getString(
                        R.string.successfully_uploaded));
            }
            else
                bmodel.showAlert(getResources().getString(
                        R.string.error_e10), 0);

        }

    }

    private void updateTermsAccepted() {
        try {
            DBUtil db = new DBUtil(this, DataMembers.DB_NAME
            );
            db.openDataBase();
            db.updateSQL("update AppVariables set isTermsAccepted = 1");
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private void showAlert(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg);
        builder.setPositiveButton(getResources().getString(R.string.ok),
                new android.content.DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        if ("login".equals(fromScreen)) {
                            Intent myIntent = new Intent(TermsAndConditionsActivity.this, HomeScreenActivity.class);
                            myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(myIntent);
                        }
                        overridePendingTransition(0, R.anim.zoom_exit);
                        finish();
                    }

                });
        bmodel.applyAlertDialogTheme(builder);
    }
}
