package com.ivy.cpg.view.dashboard.olddashboard;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;

public class SKUWiseTargetActivity extends IvyBaseActivityNoActionBar {
    private Toolbar toolbar;
    private String screenTitle = "";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.skuwisetarget_activity);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getSupportActionBar().setElevation(0);
            }
            Intent i = getIntent();
            screenTitle = i.getStringExtra("screentitle");
            if (screenTitle.equals("")) {
                screenTitle = getResources().getString(R.string.sku_target_title);
            }
            setScreenTitle(screenTitle);
        }
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
    }

}
