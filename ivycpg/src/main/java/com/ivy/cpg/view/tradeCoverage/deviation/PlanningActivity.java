package com.ivy.cpg.view.tradeCoverage.deviation;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.google.android.gms.maps.MapsInitializer;
import com.ivy.maplib.BaiduMapFragment;
import com.ivy.maplib.PlanningMapFragment.DataPulling;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.BeatMasterBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.cpg.view.tradeCoverage.adhocPlanning.AdhocPlanningFragment;
import com.ivy.cpg.view.tradeCoverage.missedOutlets.MissedVisitFragment;

import java.util.ArrayList;
import java.util.List;

public class PlanningActivity extends IvyBaseActivityNoActionBar implements
        SearchView.OnQueryTextListener, TabLayout.OnTabSelectedListener, DataPulling, BaiduMapFragment.BaiduDataPulling {

    private BusinessModel bmodel;
    private static final String menuVisit = "Trade Coverage";
    private static final String RETAILER_FILTER_MENU_TYPE = "MENU_VISIT";
    private static final String mAdhoc = "adhoc";
    private static final String mMissedretailer = "missedretailer";
    private String fromWhere;
    private NonVisitFragment mNonVisitFragment;

    private TabLayout.Tab allTab;
    private TabLayout.Tab missedRetailerTab;

    private List<com.baidu.mapapi.map.MarkerOptions> baiduMarkerList;

    ArrayList<StandardListBO> mRetailerSelectionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.planning_tab);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        //Tablayout
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setOnTabSelectedListener(this);

        Toolbar toolbar = findViewById(R.id.toolbar);

        if (bmodel.configurationMasterHelper.SHOW_CAPTURED_LOCATION) {
            checkAndRequestPermissionAtRunTime(3);
        }
        // reset the selected retailer location
        bmodel.mSelectedRetailerLatitude = 0;
        bmodel.mSelectedRetailerLongitude = 0;

        baiduMarkerList = new ArrayList<>();
        try {
            if (bmodel.configurationMasterHelper.IS_MAP) {
                MapsInitializer.initialize(this);
                if (bmodel.configurationMasterHelper.IS_BAIDU_MAP)
                    SDKInitializer.initialize(getApplication());
            }
        } catch (Exception e) {
            Commons.printException(e);
        }

        /** This intent value is to identify calling Activity. **/
        fromWhere = getIntent().getStringExtra("From");
        if (fromWhere == null) {
            fromWhere = menuVisit;
        }

        if (bmodel.beatMasterHealper.getBeatMaster() == null) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        BeatMasterBO b = getTodayBeat();
        if (b != null) {
            bmodel.beatMasterHealper.setTodayBeatMasterBO(b);
        } else {
            BeatMasterBO tempBeat = new BeatMasterBO();
            tempBeat.setBeatId(0);
            if (bmodel.configurationMasterHelper.SHOW_ALL_ROUTES) {
                tempBeat.setBeatDescription(getResources().getString(
                        R.string.all));
            } else {
                tempBeat.setBeatDescription("No Plan");
            }
            tempBeat.setToday(0);
            bmodel.beatMasterHealper.setTodayBeatMasterBO(tempBeat);

        }

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("Deviation");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(bmodel.configurationMasterHelper.getTradecoveragetitle());
//            // Used to on / off the back arrow icon
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//           // Used to remove the app logo actionbar icon and set title as home
//          // (title support click)
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }



        if (bmodel.configurationMasterHelper.SHOW_RETAILER_SELECTION_FILTER) {
            bmodel.mRetailerHelper.downloadRetailerFilterSelection(RETAILER_FILTER_MENU_TYPE);
            mRetailerSelectionList = bmodel.mRetailerHelper.getRetailerSelectionFilter();
            StandardListBO standardListBO = new StandardListBO();
            standardListBO.setListCode("ALL");
            standardListBO.setListName("All");
            mRetailerSelectionList.add(0, standardListBO);

        }


        allTab = tabLayout.newTab();
        allTab.setText(getResources().getString(R.string.total_outlet_count));
        allTab.setTag("all");

        TabLayout.Tab nearByTab = tabLayout.newTab();
        nearByTab.setText("NearBy");
        nearByTab.setTag("near");

        TabLayout.Tab adhocTab = tabLayout.newTab();
        adhocTab.setText(getResources().getString(R.string.adhoc_planning));
        adhocTab.setTag(mAdhoc);

        missedRetailerTab = tabLayout.newTab();
        missedRetailerTab.setText(getResources().getString(R.string.missed_retailer));
        missedRetailerTab.setTag("MissedRetailer");

        if (!bmodel.configurationMasterHelper.SHOW_ALL_ROUTES)
            tabLayout.addTab(allTab);

        if (bmodel.configurationMasterHelper.IS_NEARBY)
            tabLayout.addTab(nearByTab);


        if (bmodel.configurationMasterHelper.SHOW_MISSED_RETAILER)
            tabLayout.addTab(missedRetailerTab);

        if (bmodel.configurationMasterHelper.IS_ADHOC)
            tabLayout.addTab(adhocTab);
    }

    /**
     * Get today beat object by searching the beatmaster vector.
     *
     * @return -Today beat
     */
    private BeatMasterBO getTodayBeat() {
        try {
            if(bmodel.beatMasterHealper.getBeatMaster()!=null) {
                int size = bmodel.beatMasterHealper.getBeatMaster().size();
                for (int i = 0; i < size; i++) {
                    BeatMasterBO b = bmodel.beatMasterHealper.getBeatMaster()
                            .get(i);
                    if (b.getToday() == 1)
                        return b;
                }
            }

        } catch (Exception e) {
            Commons.printException(e);
        }
        return null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    @Override
    protected void onPause() {
        Commons.print("Planning act on pause");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i1 = item.getItemId();
        if (i1 == android.R.id.home) {
            finish();
//            BusinessModel.loadActivity(PlanningActivity.this,
//                    DataMembers.actPlanning);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        if (getSupportFragmentManager().findFragmentById(
                R.id.realtabcontent) instanceof NonVisitFragment) {
            mNonVisitFragment = (NonVisitFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.realtabcontent);
            mNonVisitFragment.loadData(s);
        }
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        if (getSupportFragmentManager().findFragmentById(R.id.realtabcontent) instanceof NonVisitFragment) {
            if (s.isEmpty()) {
                mNonVisitFragment = (NonVisitFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.realtabcontent);
                mNonVisitFragment.loadData(null);
            } else if (s.length() >= 3) {
                mNonVisitFragment = (NonVisitFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.realtabcontent);
                mNonVisitFragment.loadData(s);
            }
        }
        return false;
    }

    public void updateRetailerCount(int retailerCount, int isFrom) {
        try {
            String str;
            if (isFrom == 2) {
                str = getTabText((PlanningActivity.this.allTab.getText() != null && PlanningActivity.this.allTab.getText().length() > 0) ? PlanningActivity.this.allTab.getText().toString() : "");
                PlanningActivity.this.allTab.setText(str + " : " + retailerCount);

            } else if (isFrom == 3) {
                str = getTabText((PlanningActivity.this.missedRetailerTab.getText() != null && PlanningActivity.this.missedRetailerTab.getText().length() > 0) ? PlanningActivity.this.missedRetailerTab.getText().toString() : "");
                PlanningActivity.this.missedRetailerTab.setText(str + " : " + retailerCount);


            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private String getTabText(String tabText) {
        try {
            if (tabText.contains(":")) {
                return tabText.substring(0, tabText.lastIndexOf(':') - 1);
            } else
                return tabText;
        } catch (Exception e) {
            Commons.printException(e);
        }
        return tabText;
    }

    @Override
    public void switchVisitView() {

    }

    @Override
    public List<com.baidu.mapapi.map.MarkerOptions> getBaiduData() {
        return baiduMarkerList;
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {

        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();

        mNonVisitFragment = (NonVisitFragment) fm.findFragmentByTag("all");
        MissedVisitFragment mMissedVisitFragment = (MissedVisitFragment) fm.findFragmentByTag(mMissedretailer);
        AdhocPlanningFragment mAdhocPlanningFragment = (AdhocPlanningFragment) fm.findFragmentByTag(mAdhoc);


        android.support.v4.app.FragmentTransaction ft = fm.beginTransaction();

        /** Detaches the fragment if exists */

        if (mNonVisitFragment != null)
            ft.detach(mNonVisitFragment);


        if (mMissedVisitFragment != null) {
            ft.detach(mMissedVisitFragment);
        }
        if (mAdhocPlanningFragment != null) {
            ft.detach(mAdhocPlanningFragment);
        }

        /** If current tab is android */
        if (("all").equalsIgnoreCase(tab.getTag() + "")) {
            if (mNonVisitFragment == null) {

                Bundle bndl = new Bundle();
                bndl.putString("From", fromWhere);
                NonVisitFragment fragment = new NonVisitFragment();
                fragment.setArguments(bndl);
                ft.add(R.id.realtabcontent, fragment, "all");
            } else {
                ft.attach(mNonVisitFragment);
            }
        } else if (mMissedretailer.equalsIgnoreCase(tab.getTag() + "")) {
            if (mMissedVisitFragment == null) {
                ft.add(R.id.realtabcontent, new MissedVisitFragment(),
                        mMissedretailer);
            } else {
                ft.attach(mMissedVisitFragment);
            }
        } else if (mAdhoc.equalsIgnoreCase(tab.getTag() + "")) {
            if (mAdhocPlanningFragment == null) {
                ft.add(R.id.realtabcontent, new AdhocPlanningFragment(),
                        mAdhoc);
            } else {
                ft.attach(mAdhocPlanningFragment);
            }
        }
        ft.commit();
        fm.executePendingTransactions();

    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        if (("Visit").equalsIgnoreCase(tab.getTag() + ""))
            tab.setText(bmodel.getDay(bmodel.userMasterHelper.getUserMasterBO().getDownloadDate()));
        else if (("all").equalsIgnoreCase(tab.getTag() + ""))
            tab.setText(getResources().getString(R.string.all));
        else if (mMissedretailer.equalsIgnoreCase(tab.getTag() + ""))
            tab.setText(getResources().getString(R.string.missed_retailer));

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }
}
