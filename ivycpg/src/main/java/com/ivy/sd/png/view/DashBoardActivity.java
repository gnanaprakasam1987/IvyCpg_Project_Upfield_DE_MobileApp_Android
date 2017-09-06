package com.ivy.sd.png.view;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;

public class DashBoardActivity extends IvyBaseActivityNoActionBar {
    private Toolbar toolbar;
    private DashboardFragment fragmentObject;
    private boolean isFormPlanning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dashboard_viewpager_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Used to remove the app logo actionbar icon and set title as home
            // (title support click)
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getSupportActionBar().setElevation(0);
            }
        }

        String screen = getIntent().getStringExtra("screentitle");

        Bundle bundle = new Bundle();
        bundle.putString("retid", getIntent().getStringExtra("retid"));//retailer id is passed to load Retailer Dashboard by re-using DashboardFragment
        bundle.putString("type", "DAY");
        bundle.putBoolean("planning", true);
        fragmentObject = new DashboardFragment();
        fragmentObject.setArguments(bundle);
        if (fragmentObject != null) {
            android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().add(R.id.content_fragemnt, fragmentObject).commit();
        }


    }
}





