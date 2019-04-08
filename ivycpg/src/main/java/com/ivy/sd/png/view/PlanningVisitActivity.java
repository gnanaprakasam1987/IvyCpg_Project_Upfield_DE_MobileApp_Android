package com.ivy.sd.png.view;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ivy.cpg.view.tradeCoverage.VisitFragment;
import com.ivy.maplib.PlanningMapFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlanningVisitActivity extends IvyBaseActivityNoActionBar implements VisitFragment.MapViewListener,PlanningMapFragment.DataPulling {

    private static final String MENU_PLANNING_CONSTANT = "Day Planning";
    private static final String MENU_VISIT = "MENU_VISIT";
    BusinessModel bmodel;
    private List<MarkerOptions> markerList;
    private List<com.baidu.mapapi.map.MarkerOptions> baiduMarkerList;
    com.baidu.mapapi.model.LatLng baidulatLng;

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
        if (fragmentObject != null) {
            ((VisitFragment) fragmentObject).setMapViewListener(this);
            android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().add(R.id.content_fragemnt, fragmentObject).commit();
        }
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
    public List<MarkerOptions> getData() {
        return markerList;
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
                ((VisitFragment) fragment).setMapViewListener(this);
                android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
                fm.beginTransaction().add(R.id.fragment_content, fragment,
                        MENU_VISIT);
            }
            break;
        }
    }

    private void displayTodayRoute(String filter) {
        LatLng latLng;

        List<RetailerMasterBO> retailer = new ArrayList<>();
        try {
            int siz = bmodel.getRetailerMaster().size();
            retailer.clear();
            // Add today's retailers.

            if (!bmodel.configurationMasterHelper.SHOW_ALL_ROUTES) {
                for (int i = 0; i < siz; i++) {
                    if (bmodel.getRetailerMaster().get(i).getIsToday() == 1) {
                        retailer.add(bmodel.getRetailerMaster().get(i));
                    }
                }
            } else {
                for (int i = 0; i < siz; i++) {

                    retailer.add(bmodel.getRetailerMaster().get(i));

                }
            }

            Collections.sort(retailer,
                    RetailerMasterBO.WalkingSequenceComparator);


            // Add today'sdeviated retailers.
            for (int i = 0; i < siz; i++) {
                if (bmodel.getRetailerMaster().get(i).getIsDeviated() != null && ("Y").equals(bmodel.getRetailerMaster().get(i).getIsDeviated())) {
                    if (filter != null) {
                        if ((bmodel.getRetailerMaster().get(i)
                                .getRetailerName().toLowerCase())
                                .contains(filter.toLowerCase())) {
                            retailer.add(bmodel.getRetailerMaster().get(i));
                        }
                    } else {
                        retailer.add(bmodel.getRetailerMaster().get(i));
                    }
                }
            }
        } catch (Exception e) {
            Commons.printException(e);
        }

        try {

            if (bmodel.configurationMasterHelper.IS_MAP) {
                markerList.clear();
                baiduMarkerList.clear();
                for (int i = 0; i < retailer.size(); i++) {
                    if (bmodel.configurationMasterHelper.IS_BAIDU_MAP) {
                        Bundle bndl = new Bundle();
                        bndl.putCharSequence("addr", retailer.get(i).getAddress1());
                        baidulatLng = new com.baidu.mapapi.model.LatLng(retailer.get(i).getLatitude(), retailer
                                .get(i).getLongitude());
                        com.baidu.mapapi.map.MarkerOptions mBMarker = new com.baidu.mapapi.map.MarkerOptions().position(baidulatLng)
                                .title(retailer.get(i).getRetailerName())
                                .extraInfo(bndl)
                                .icon(com.baidu.mapapi.map.BitmapDescriptorFactory.fromResource(R.drawable.markergreen)
                                ).animateType(com.baidu.mapapi.map.MarkerOptions.MarkerAnimateType.drop);
                        baiduMarkerList.add(mBMarker);
                    } else {
                        latLng = new LatLng(retailer.get(i).getLatitude(), retailer
                                .get(i).getLongitude());
                        MarkerOptions mMarkerOptions = new MarkerOptions()
                                .position(latLng)
                                .title(retailer.get(i).getRetailerName() + "," + retailer.get(i).getRetailerID())
                                .snippet(retailer.get(i).getAddress1())
                                .icon(BitmapDescriptorFactory
                                        .defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                        markerList.add(mMarkerOptions);
                    }
                }
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }
}
