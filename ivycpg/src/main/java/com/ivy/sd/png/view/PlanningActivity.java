package com.ivy.sd.png.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ivy.maplib.BaiduMapFragment;
import com.ivy.maplib.PlanningMapFragment.DataPulling;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.BeatMasterBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.Collections;
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
    private List<MarkerOptions> markerList;
    private LatLng latLng;
    private TabLayout.Tab allTab;
    private TabLayout.Tab missedRetailerTab;
    private int mSelectedpostion = -1;
    private Toolbar toolbar;

    private List<com.baidu.mapapi.map.MarkerOptions> baiduMarkerList;
    com.baidu.mapapi.model.LatLng baidulatLng;


    private StandardListBO mSelectedMenuBO;
    private TabLayout tabLayout;

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
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setOnTabSelectedListener(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (bmodel.configurationMasterHelper.SHOW_CAPTURED_LOCATION) {
            checkAndRequestPermissionAtRunTime(3);
        }
        // reset the selected retailer location
        bmodel.mSelectedRetailerLatitude = 0;
        bmodel.mSelectedRetailerLongitude = 0;

        /** Initialising map view **/
        markerList = new ArrayList<>();
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
        allTab.setText(getResources().getString(R.string.all));
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
            BusinessModel.loadActivity(PlanningActivity.this,
                    DataMembers.actPlanning);
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

    private void setSearchTextColour(SearchView searchView) {
        LinearLayout searchPlate = (LinearLayout) searchView
                .findViewById(R.id.search_plate);
        searchPlate

                .setBackgroundResource(R.drawable.abc_ab_share_pack_holo_light);
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
                if (("Y").equals(bmodel.getRetailerMaster().get(i).getIsDeviated())) {
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
                                .title(retailer.get(i).getRetailerName())
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
    public List<MarkerOptions> getData() {
        return markerList;
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

        MissedVisitFragment mMissedVisitFragment;
        AdhocPlanningFragment mAdhocPlanningFragment;
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        mNonVisitFragment = (NonVisitFragment) fm.findFragmentByTag("all");
        mMissedVisitFragment = (MissedVisitFragment) fm.findFragmentByTag(mMissedretailer);
        mAdhocPlanningFragment = (AdhocPlanningFragment) fm.findFragmentByTag(mAdhoc);


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
                ft.add(R.id.realtabcontent, new NonVisitFragment(), "all");
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


    @SuppressLint("ValidFragment")
    public class CustomFragment extends DialogFragment {
        private String mTitle = "";


        private TextView mTitleTV;
        private Button mOkBtn;
        private Button mDismisBtn;
        private ListView mCountLV;


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mTitle = getArguments().getString("title");


        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.custom_dialog_fragment, container, false);
        }

        @Override
        public void onStart() {
            super.onStart();
            getDialog().setTitle(mTitle);
            if (getView() != null) {
                mTitleTV = (TextView) getView().findViewById(R.id.title);
                mOkBtn = (Button) getView().findViewById(R.id.btn_ok);
                mDismisBtn = (Button) getView().findViewById(R.id.btn_dismiss);
                mCountLV = (ListView) getView().findViewById(R.id.lv_colletion_print);
            }
            mTitleTV.setVisibility(View.GONE);

            ArrayAdapter<StandardListBO> adapter = new ArrayAdapter<>(PlanningActivity.this, android.R.layout.simple_list_item_single_choice, mRetailerSelectionList);
            mCountLV.setAdapter(adapter);
            mCountLV.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            if (mSelectedpostion != -1)
                mCountLV.setItemChecked(mSelectedpostion, true);
            mCountLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    dismiss();
                    mSelectedMenuBO = mRetailerSelectionList.get(position);
                    mSelectedpostion = position;

                    if (getSupportFragmentManager().findFragmentById(
                            R.id.realtabcontent) instanceof NonVisitFragment) {
                        mNonVisitFragment = (NonVisitFragment) getSupportFragmentManager()
                                .findFragmentById(R.id.realtabcontent);

                    }

                }
            });


            mCountLV.setAdapter(adapter);
            mOkBtn.setVisibility(View.GONE);
            mDismisBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });

        }

    }

}
