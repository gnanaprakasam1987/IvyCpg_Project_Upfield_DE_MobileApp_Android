package com.ivy.cpg.view.dashboard.olddashboard;

import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import androidx.fragment.app.FragmentManager;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;

public class DashBoardActivity extends IvyBaseActivityNoActionBar {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dashboard_viewpager_main);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(null);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getSupportActionBar().setElevation(0);
            }
        }

        Bundle bundle = new Bundle();
        bundle.putString("screentitle", getIntent().getStringExtra("screentitle"));
        bundle.putString("retid", getIntent().getStringExtra("retid"));//retailer id is passed to load Retailer Dashboard by re-using DashboardFragment
        bundle.putString("type", "DAY");
        bundle.putBoolean("planning", true);
        DashboardFragment fragmentObject = new DashboardFragment();
        fragmentObject.setArguments(bundle);
        if (fragmentObject != null) {
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().add(R.id.content_fragemnt, fragmentObject).commit();
        }


    }
}





