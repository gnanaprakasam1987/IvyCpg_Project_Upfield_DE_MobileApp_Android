package com.ivy.cpg.view.login;

import android.content.Intent;
import android.database.SQLException;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import androidx.appcompat.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.ivy.cpg.view.homescreen.HomeScreenActivity;
import com.ivy.cpg.view.sync.UploadHelper;
import com.ivy.lib.Utils;
import com.ivy.lib.existing.DBUtil;
import com.ivy.lib.rest.JSONFormatter;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DeviceUtils;
import com.ivy.utils.NetworkUtils;
import com.ivy.utils.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ramkumard on 8/2/19.
 * To display Terms and Conditions, in which user needs to accept it.
 */

public class TermsAndConditionsActivity extends IvyBaseActivityNoActionBar {

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

        fromScreen = getIntent().getStringExtra("fromScreen");
        TextView tv_content = findViewById(R.id.tv_terms_cond);

        if (!StringUtils.isNullOrEmpty(LoginHelper.getInstance(this).getTermsContent())) {

            String htmlContent = LoginHelper.getInstance(this).getTermsContent();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                htmlContent = Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_COMPACT).toString();
                tv_content.setText(Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_COMPACT));
            } else {
                tv_content.setText(Html.fromHtml(htmlContent));
            }
            tv_content.setMovementMethod(LinkMovementMethod.getInstance());//For auto-link clickable
        }

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
                btn_accept.setEnabled(false);
                if (NetworkUtils.isNetworkConnected(TermsAndConditionsActivity.this)) {
                    new Thread(new Runnable() {
                        public void run() {
                            Looper.prepare();
                            uploadTermsAccepted();
                            Looper.loop();
                            Looper myLooper = Looper.myLooper();
                            if (myLooper != null)
                                myLooper.quit();
                        }
                    }).start();
                    updateTermsAccepted();
                    if ("login".equals(fromScreen)) {
                        Intent myIntent = new Intent(TermsAndConditionsActivity.this, HomeScreenActivity.class);
                        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(myIntent);
                    }
                    overridePendingTransition(0, R.anim.zoom_exit);
                    finish();
                } else {
                    bmodel.showAlert(getResources().getString(
                            R.string.please_connect_to_internet), 0);
                    btn_accept.setEnabled(true);
                }
            }
        });
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

    public void uploadTermsAccepted() {
        try {

            JSONObject jsonobj = new JSONObject();

            JSONObject jObject = new JSONObject();
            jObject.put("isTermsAccepted", "1");
            jsonobj.put("UserMaster", jObject);


            JSONFormatter jsonFormatter = new JSONFormatter("HeaderInformation");

            jsonFormatter.addParameter("DeviceId",
                    DeviceUtils.getIMEINumber(this));
            jsonFormatter.addParameter("LoginId", bmodel.getAppDataProvider()
                    .getUser().getLoginName());
            jsonFormatter.addParameter("VersionCode",
                    bmodel.getApplicationVersionNumber());
            jsonFormatter.addParameter(SynchronizationHelper.VERSION_NAME, bmodel.getApplicationVersionName());
            jsonFormatter.addParameter("DistributorId", bmodel.getAppDataProvider()
                    .getUser().getDistributorid());
            jsonFormatter.addParameter("OrganisationId", bmodel.getAppDataProvider()
                    .getUser().getOrganizationId());
            jsonFormatter.addParameter("MobileDateTime",
                    Utils.getDate("yyyy/MM/dd HH:mm:ss"));
            jsonFormatter.addParameter("MobileUTCDateTime",
                    Utils.getGMTDateTime("yyyy/MM/dd HH:mm:ss"));
            jsonFormatter.addParameter("UserId", bmodel.getAppDataProvider()
                    .getUser().getUserid());
            jsonFormatter.addParameter("VanId", bmodel.getAppDataProvider()
                    .getUser().getVanId());
            String LastDayClose = "";
            if (bmodel.synchronizationHelper.isDayClosed()) {
                LastDayClose = bmodel.getAppDataProvider().getUser()
                        .getDownloadDate();
            }
            jsonFormatter.addParameter("LastDayClose", LastDayClose);
            jsonFormatter.addParameter("BranchId", bmodel.getAppDataProvider()
                    .getUser().getBranchId());
            jsonFormatter.addParameter("DownloadedDataDate", bmodel.getAppDataProvider()
                    .getUser().getDownloadDate());
            jsonFormatter.addParameter("DataValidationKey", bmodel.synchronizationHelper.generateChecksum(jsonobj.toString()));
            Commons.print(jsonFormatter.getDataInJson());
            String appendurl = bmodel.synchronizationHelper.getUploadUrl(UploadHelper.UPLOAD_TERMSACCEPT_URL_CODE);
            bmodel.synchronizationHelper
                    .getUploadResponse(jsonFormatter.getDataInJson(),
                            jsonobj.toString(), appendurl);
        } catch (SQLException | JSONException e) {
            Commons.printException(e);
        }
    }
}
