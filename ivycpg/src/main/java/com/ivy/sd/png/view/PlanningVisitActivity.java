package com.ivy.sd.png.view;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;

public class PlanningVisitActivity extends IvyBaseActivityNoActionBar {
    private Toolbar toolbar;
    private VisitFragment fragmentObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planning_visit);

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

//        bundle.putString("Newplanningsub", "Planningsub");
        Bundle bundle = new Bundle();
        bundle.putBoolean("isPlanning",true);
        bundle.putString("From", "Day Planning");
        bundle.putString("Newplanningsub", "Planningsub");
        fragmentObject = new VisitFragment();
        fragmentObject .setArguments(bundle);
        if(fragmentObject!=null){
            android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().add(R.id.content_fragemnt, fragmentObject).commit();
        }
    }

}
