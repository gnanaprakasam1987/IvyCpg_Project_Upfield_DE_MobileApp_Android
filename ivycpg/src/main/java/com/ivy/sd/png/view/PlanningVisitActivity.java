package com.ivy.sd.png.view;

import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import androidx.fragment.app.FragmentManager;

import com.ivy.cpg.view.tradeCoverage.VisitFragment;
import com.ivy.maplib.PlanningMapFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;


public class PlanningVisitActivity extends IvyBaseActivityNoActionBar implements VisitFragment.MapViewListener,PlanningMapFragment.DataPulling {

    private static final String MENU_PLANNING_CONSTANT = "Day Planning";
    private static final String MENU_VISIT = "MENU_VISIT";
    BusinessModel bmodel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_planning_visit);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

        bmodel=(BusinessModel)getApplicationContext();

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
            bundle.putString("From", "Planningsub");
        }

        if(bundle.getBoolean("isPlanningSub")){
            if (getIntent().getStringExtra("menuCode")!=null) {
                // It is just a view screen, so updating once the screen is visited once
                bmodel.saveModuleCompletion(getIntent().getStringExtra("menuCode"), false);
            }
        }

        VisitFragment fragmentObject = new VisitFragment();
        fragmentObject.setArguments(bundle);
            fragmentObject.setMapViewListener(this);
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().add(R.id.content_fragemnt, fragmentObject).commit();

    }

    @Override
    public void switchMapView() {
//        displayTodayRoute(null);
//        Bundle bndl = new Bundle();
//            bndl.putString("From", MENU_PLANNING_CONSTANT);
//        PlanningMapFragment fragment = new PlanningMapFragment();
//        fragment.setArguments(bndl);
//        ((PlanningMapFragment) fragment).setDataPull(this);
//        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
//        fm.beginTransaction().add(R.id.fragment_content, fragment,
//                "MENU_PLANE_MAP");
    }

    @Override
    public void switchVisitView() {
        // Load the HHTMenuTable
        bmodel.configurationMasterHelper.downloadMainMenu();
        for (ConfigureBO con : bmodel.configurationMasterHelper.getConfig()) {

            if (con.getConfigCode().equals(MENU_VISIT)) {

                bmodel.distributorMasterHelper.downloadDistributorsList();
                bmodel.configurationMasterHelper
                        .setTradecoveragetitle(con.getMenuName());

                Bundle bndl = new Bundle();
                bndl.putString("From", MENU_PLANNING_CONSTANT);
                bndl.putString("Newplanningsub", "");
                VisitFragment fragment = new VisitFragment();
                fragment.setArguments(bndl);
                fragment.setMapViewListener(this);
                FragmentManager fm = getSupportFragmentManager();
                fm.beginTransaction().add(R.id.fragment_content, fragment,
                        MENU_VISIT);
            }
            break;
        }
    }
}
