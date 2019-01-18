package com.ivy.sd.png.view;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.ivy.cpg.view.tradeCoverage.VisitFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;

public class PlanningVisitActivity extends IvyBaseActivityNoActionBar {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_planning_visit);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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

        Bundle bundle = new Bundle();
        if (getIntent().getBooleanExtra("isPlanning", false)) {
            bundle.putBoolean("isPlanning", true);
            bundle.putString("From", "Day Planning");
        } else if (getIntent().getBooleanExtra("isPlanningSub", false)) {
            bundle.putBoolean("isPlanningSub", true);
            bundle.putString("Newplanningsub", "Planningsub");
        }
        VisitFragment fragmentObject = new VisitFragment();
        fragmentObject.setArguments(bundle);
        if (fragmentObject != null) {
            android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().add(R.id.content_fragemnt, fragmentObject).commit();
        }
    }

}
