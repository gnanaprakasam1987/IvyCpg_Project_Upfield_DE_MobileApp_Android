package com.ivy.sd.png.view;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.utils.FontUtils;

public class LicenseActivity extends IvyBaseActivityNoActionBar {

    private BusinessModel bmodel;
    private LinearLayout lnrLicense;

    private Toolbar toolbar;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_licensing);
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Set title to toolbar
        getSupportActionBar().setTitle(
                getResources().getString(R.string.software_licensing));
        // Used to on / off the back arrow icon
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Used to remove the app logo actionbar icon and set title as home
        // (title support click)
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        // Used to hide the app logo icon from actionbar
        // getSupportActionBar().setDisplayUseLogoEnabled(false);


        lnrLicense = findViewById(R.id.licenseLayout);
        TextView txtTitle = new CustomTextView(this);
        txtTitle.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM, this));
        txtTitle.setText(Html.fromHtml(getString(R.string.license_header)));

        TextView txtVersion = new CustomTextView(this);
        txtVersion.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM, this));
        txtVersion.setText(Html.fromHtml(getString(R.string.license_version)));

        TextView txtLink = new CustomTextView(this);
        txtLink.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM, this));
        txtLink.setMovementMethod(LinkMovementMethod.getInstance());
        txtLink.setText(Html.fromHtml(getString(R.string.license_link)));

        TextView txt = new CustomTextView(this);
        //txt.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR, this));
        txt.setText(Html.fromHtml(getString(R.string.apache_license)));

        lnrLicense.addView(txtTitle, 0);
        lnrLicense.addView(txtVersion, 1);
        lnrLicense.addView(txtLink, 2);
        lnrLicense.addView(txt, 3);

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
