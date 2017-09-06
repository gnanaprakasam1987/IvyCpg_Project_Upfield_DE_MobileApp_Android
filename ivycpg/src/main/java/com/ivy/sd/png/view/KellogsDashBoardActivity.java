package com.ivy.sd.png.view;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;

public class KellogsDashBoardActivity extends IvyBaseActivityNoActionBar {
    private Toolbar toolbar;
    private KellogsDashboardFragment fragmentObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dashboard_viewpager_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        String title = getIntent().getStringExtra("screenTitle");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Used to remove the app logo actionbar icon and set title as home
            // (title support click)
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getSupportActionBar().setElevation(0);
            }

            setScreenTitle(title);
        }
        fragmentObject = new KellogsDashboardFragment();
        if (fragmentObject != null) {
            android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().add(R.id.content_fragemnt, fragmentObject).commit();
        }


    }
}





