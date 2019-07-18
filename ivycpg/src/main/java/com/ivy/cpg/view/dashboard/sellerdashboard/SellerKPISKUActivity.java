package com.ivy.cpg.view.dashboard.sellerdashboard;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;

public class SellerKPISKUActivity extends IvyBaseActivityNoActionBar {
    private Toolbar toolbar;
    private String screenTitle = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.sellerkpisku_activity);

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
                screenTitle = "SKU Target";
            }
            setScreenTitle(screenTitle);
        }
    }
}
