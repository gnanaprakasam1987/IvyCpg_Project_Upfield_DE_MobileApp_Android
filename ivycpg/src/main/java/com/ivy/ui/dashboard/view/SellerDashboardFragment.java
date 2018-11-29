package com.ivy.ui.dashboard.view;

import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseFragment;
import com.ivy.cpg.primarysale.bo.DistributorMasterBO;
import com.ivy.cpg.view.dashboard.DashBoardBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.BeatMasterBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.commons.KeyPairBoolData;
import com.ivy.sd.png.commons.MultiSpinner;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.commons.SpinnerListener;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.HomeScreenActivity;
import com.ivy.ui.dashboard.DashboardClickListener;
import com.ivy.ui.dashboard.SellerDashboardConstants;
import com.ivy.ui.dashboard.SellerDashboardContract;
import com.ivy.ui.dashboard.adapter.DashboardListAdapter;
import com.ivy.ui.dashboard.adapter.FragmentPagerAdapter;
import com.ivy.ui.dashboard.chart.kpi.KPIBarChartFragment;
import com.ivy.ui.dashboard.chart.p3m.P3MChartFragment;
import com.ivy.ui.dashboard.chart.smp.SMPChartFragment;
import com.ivy.ui.dashboard.di.DaggerSellerDashboardComponent;
import com.ivy.ui.dashboard.di.SellerDashboardModule;
import com.ivy.utils.FontUtils;
import com.ivy.utils.event.DashBoardEventData;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import me.relex.circleindicator.CircleIndicator;

import static com.ivy.ui.dashboard.SellerDashboardConstants.DAY;
import static com.ivy.ui.dashboard.SellerDashboardConstants.P3M;
import static com.ivy.ui.dashboard.SellerDashboardConstants.ROUTE;
import static com.ivy.ui.dashboard.SellerDashboardConstants.WEEK;
import static com.ivy.utils.AppUtils.QT;
import static com.ivy.utils.AppUtils.isNullOrEmpty;

public class SellerDashboardFragment extends BaseFragment implements SellerDashboardContract.SellerDashboardView, DashboardClickListener {

    public static final String DASHBOARD = "DASHBOARD";
    @Inject
    SellerDashboardContract.SellerDashboardPresenter<SellerDashboardContract.SellerDashboardView> presenter;

    @BindView(R.id.dashSpinnerLayout)
    ConstraintLayout dashSpinnerLayout;

    @BindView(R.id.distributorSpinner)
    Spinner distributorSpinnerStub;

    @BindView(R.id.userSpinner)
    Spinner userSpinnerStub;

    @BindView(R.id.dashSpinner)
    Spinner dashSpinner;

    @BindView(R.id.monthSpinner)
    Spinner monthSpinnerStub;

    @BindView(R.id.weekSpinner)
    Spinner weekSpinnerStub;

    @BindView(R.id.multiSelectStub)
    ViewStub multiSelectStub;

    @BindView(R.id.routeSpinner)
    Spinner routeSpinner;

    @BindView(R.id.viewpager)
    ViewPager pager;

    @BindView(R.id.collapsing)
    CollapsingToolbarLayout collapsingToolbarLayout;

    @BindView(R.id.indicator)
    CircleIndicator circleIndicatorView;

    @BindView(R.id.dashboardLv)
    RecyclerView dashboardRecyclerView;

    @BindView(R.id.resultsHeaderTxt)
    TextView spinnerHeaderTxt;

    private ArrayList<Fragment> fragments;

    private Spinner distributorSpinner;

    private Spinner userSpinner;

    private MultiSpinner distributorMultiSpinner;

    private MultiSpinner userMultiSpinner;

    private String menuCode;

    private boolean isFromRetailer; //isFromHomeScreenTwo
    private String type;

    private String mSelectedDistributorId = "";

    private DashboardListAdapter dashboardListAdapter;

    private ArrayList<DashBoardBO> dashboardListData;

    private ArrayList<DashBoardBO> kpiChartData;

    private String mSelectedUser = "0";

    private String selectedInterval;

    private String screenTitle;


    @Override
    public void initializeDi() {

        DaggerSellerDashboardComponent.builder()
                .ivyAppComponent(((BusinessModel) getActivity().getApplication()).getComponent())
                .sellerDashboardModule(new SellerDashboardModule(this))
                .build()
                .inject(SellerDashboardFragment.this);


        setBasePresenter((BasePresenter) presenter);


    }

    @Override
    protected int setContentViewLayout() {
        return R.layout.fragment_seller_dashboard_new;
    }

    @Override
    public void initVariables(View view) {
        dashboardListData = new ArrayList<>();
    }

    @Override
    protected void getMessageFromAliens() {
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            menuCode = bundle.getString("menuCode");
            isFromRetailer = bundle.getBoolean("isFromHomeScreenTwo", false);
            type = bundle.getString("type");
            screenTitle = bundle.getString("screentitle");
        }


    }

    @Override
    protected void setUpViews() {

        setUpActionBar();

        dashboardListAdapter = new DashboardListAdapter(getActivity(), new ArrayList<DashBoardBO>(), presenter.getLabelsMap(), this);

        if (isFromRetailer) {
            getDashSpinnerData();
            hidePager();
        } else {
            mSelectedUser = String.valueOf(presenter.getCurrentUser().getUserid());
            handleSellerDashboard();

        }

        if (!presenter.shouldShowTrendChart()) {
            hidePager();
        }

        if (type != null
                && type.equals(ROUTE)) {
            presenter.fetchBeats();
        }


        setHasOptionsMenu(true);
        getActivity().invalidateOptionsMenu();

        spinnerHeaderTxt.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));

        dashboardRecyclerView.setHasFixedSize(false);
        dashboardRecyclerView.setNestedScrollingEnabled(false);
        dashboardRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        dashboardRecyclerView.setAdapter(dashboardListAdapter);

    }

    private void handleSellerDashboard() {
        if (presenter.isUserBasedDash() && presenter.isDistributorBasedDash()) {
            if (presenter.isNiveaBasedDash()) {
                dashSpinnerLayout.setVisibility(View.GONE);
                multiSelectStub.setVisibility(View.VISIBLE);

                CoordinatorLayout multiSelectLayout = (CoordinatorLayout) multiSelectStub.inflate();

                distributorMultiSpinner = multiSelectLayout.findViewById(R.id.distributorSpinner1);
                userMultiSpinner = multiSelectLayout.findViewById(R.id.userSpinner1);

                presenter.fetchDistributorList(true);

                if (!presenter.shouldShowTrendChart()) {
                    hidePager();
                }
            } else {
                dashSpinnerLayout.setVisibility(View.VISIBLE);
                multiSelectStub.setVisibility(View.GONE);
                distributorSpinnerStub.setVisibility(View.VISIBLE);
                distributorSpinner = (Spinner) distributorSpinnerStub;

                userSpinnerStub.setVisibility(View.VISIBLE);
                userSpinner = (Spinner) userSpinnerStub;

                presenter.fetchDistributorList(false);

                userSpinner.setOnItemSelectedListener(userSpinnerListener);
            }
        } else if (presenter.isUserBasedDash()) {
            dashSpinnerLayout.setVisibility(View.VISIBLE);
            multiSelectStub.setVisibility(View.GONE);

            userSpinnerStub.setVisibility(View.VISIBLE);
            userSpinner = (Spinner) userSpinnerStub;
        } else
            getDashSpinnerData();


    }

    @Override
    public void setupMultiSelectDistributorSpinner(ArrayList<DistributorMasterBO> distributors) {

        List<KeyPairBoolData> distArray = new ArrayList<>();
        distArray.add(new KeyPairBoolData(0, getString(R.string.all), true));

        int count = 0;

        for (int j = 0; j < distributors.size(); j++) {
            KeyPairBoolData h = new KeyPairBoolData();
            h.setId(SDUtil.convertToInt(distributors.get(j).getDId()));
            h.setName(distributors.get(j).getDName());
            h.setSelected(true);
            distArray.add(h);
            count++;
            mSelectedDistributorId += QT(distributors.get(j).getDId() + "");
            if (count != distributors.size())
                mSelectedDistributorId += ",";
        }

        distributorMultiSpinner.setItems(distArray, -1, new SpinnerListener() {
            @Override
            public void onItemsSelected(List<KeyPairBoolData> items) {
                int count = 0;
                mSelectedDistributorId = "";
                if (items.size() > 0) {
                    for (int i = 0; i < items.size(); i++) {
                        count++;
                        mSelectedDistributorId += QT(items.get(i).getId() + "");
                        if (count != items.size())
                            mSelectedDistributorId += ",";
                    }
                } else
                    mSelectedDistributorId = "0";

                presenter.fetchUserList(mSelectedDistributorId, true);


            }
        });

    }

    @Override
    public void setUpDistributorSpinner(ArrayList<DistributorMasterBO> distributorMasterBOS) {
        ArrayAdapter<DistributorMasterBO> distributorMasterBOArrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.dashboard_spinner_layout);
        distributorMasterBOArrayAdapter.add(new DistributorMasterBO("0", getResources().getString(R.string.select)));

        if (distributorMasterBOS.size() != 0) {
            distributorMasterBOArrayAdapter.addAll(distributorMasterBOS);
            distributorMasterBOArrayAdapter.setDropDownViewResource(R.layout.dashboard_spinner_list);
            distributorSpinner.setAdapter(distributorMasterBOArrayAdapter);
            distributorSpinner.setOnItemSelectedListener(distributorSpinnerListener);
        }
    }

    @Override
    public void setUpUserSpinner(ArrayList<UserMasterBO> userMasterBOS) {

        ArrayAdapter<UserMasterBO> userMasterBOArrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.dashboard_spinner_layout);
        userMasterBOArrayAdapter.add(new UserMasterBO(0, getResources().getString(R.string.all)));

        if (userMasterBOS.size() != 0) {
            userMasterBOArrayAdapter.addAll(userMasterBOS);
            userMasterBOArrayAdapter.setDropDownViewResource(R.layout.dashboard_spinner_list);
            userSpinner.setAdapter(userMasterBOArrayAdapter);
            userSpinner.setOnItemSelectedListener(userSpinnerListener);
        }
    }

    @Override
    public void setUpMultiSelectUserSpinner(ArrayList<UserMasterBO> users) {

        List<KeyPairBoolData> userArray = new ArrayList<>();

        userArray.add(new KeyPairBoolData(0, getString(R.string.all), true));
        int count = 0;
        for (int i = 0; i < users.size(); i++) {
            KeyPairBoolData h = new KeyPairBoolData();
            h.setId(users.get(i).getUserid());
            h.setName(users.get(i).getUserName());
            h.setSelected(true);
            userArray.add(h);
            count++;
            mSelectedUser += QT(users.get(i).getUserid() + "");
            if (count != users.size())
                mSelectedUser += ",";
        }

        userMultiSpinner.setItems(userArray, -1, new SpinnerListener() {
            @Override
            public void onItemsSelected(List<KeyPairBoolData> items) {
                Commons.print("Multi" + items.size());
                int count = 0;
                mSelectedUser = "";
                for (int i = 0; i < items.size(); i++) {
                    count++;
                    mSelectedUser += QT(items.get(i).getId() + "");
                    if (count != items.size())
                        mSelectedUser += ",";
                }
                loadMultiSelectData();
            }
        });


    }

    @Override
    public void setDashboardListAdapter(ArrayList<DashBoardBO> dashBoardBOS) {

        dashboardListAdapter = new DashboardListAdapter(getActivity(), dashBoardBOS, presenter.getLabelsMap(), this);
        dashboardRecyclerView.setAdapter(dashboardListAdapter);

    }

    @Override
    public void setupRouteSpinner(ArrayList<BeatMasterBO> beatMasterBOS) {
        routeSpinner.setVisibility(View.VISIBLE);
        ArrayAdapter<BeatMasterBO> routeAdapter = new ArrayAdapter<>(getActivity(), R.layout.dashboard_spinner_layout, beatMasterBOS);
        routeAdapter.setDropDownViewResource(R.layout.dashboard_spinner_list);
        routeSpinner.setAdapter(routeAdapter);
        routeSpinner.setOnItemSelectedListener(routeSpinnerSelectedListener);

    }


    private void loadMultiSelectData() {

        presenter.fetchKPIDashboardData(mSelectedUser, mSelectedDistributorId);
        if (presenter.shouldShowTrendChart()) {
            pager.setVisibility(View.VISIBLE);
            circleIndicatorView.setViewPager(pager);
        }

    }


    private void getDashSpinnerData() {
        if (!isNullOrEmpty(type) && type.equalsIgnoreCase(SellerDashboardConstants.ROUTE)) {
            presenter.fetchSellerDashList(SellerDashboardConstants.DashBoardType.ROUTE);
        } else if (!isFromRetailer) {
            presenter.fetchSellerDashList(SellerDashboardConstants.DashBoardType.SELLER);
        } else
            presenter.fetchSellerDashList(SellerDashboardConstants.DashBoardType.RETAILER);

    }


    private void hidePager() {
        pager.setVisibility(View.GONE);
        collapsingToolbarLayout.setVisibility(View.GONE);
    }


    private ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    private void setUpActionBar() {
        getActionBar().setDisplayShowTitleEnabled(false);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActionBar().setElevation(0);
        }

        if (screenTitle != null)
            setScreenTitle(screenTitle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_target_plan, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_skutgt).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            if (isFromRetailer) {
                //update time stamp if previous screen is homescreentwo
                presenter.updateTimeStampModuleWise();
                presenter.saveModuleCompletion(menuCode);
            }
            getActivity().finish();
            return true;
        } else if (i == R.id.menu_next) {
            startActivityAndFinish(HomeScreenActivity.class);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void updateDashSpinner(ArrayList<String> dashList) {
        // Creating adapter for spinner

        if (!dashList.isEmpty()) {
            dashSpinner.setVisibility(View.VISIBLE);
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(), R.layout.dashboard_spinner_layout, dashList);

            dataAdapter.setDropDownViewResource(R.layout.dashboard_spinner_list);

            dashSpinner.setAdapter(dataAdapter);
            dashSpinner.setOnItemSelectedListener(dashSpinnerSelectedListener);
        }

    }

    private AdapterView.OnItemSelectedListener routeSpinnerSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            String filterName = adapterView.getSelectedItem().toString();

            dashboardListData.clear();

            for (DashBoardBO dashBoardBO : presenter.getDashboardListData()) {
                if (dashBoardBO.getMonthName().equalsIgnoreCase(filterName)) {
                    dashboardListData.add(dashBoardBO);
                }
            }

            dashboardListAdapter = new DashboardListAdapter(getActivity(), dashboardListData, presenter.getLabelsMap(), SellerDashboardFragment.this);
            dashboardRecyclerView.setAdapter(dashboardListAdapter);

            //TODO Handle p3m chart
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };


    private AdapterView.OnItemSelectedListener dashSpinnerSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            selectedInterval = adapterView.getSelectedItem().toString();
            if (!isFromRetailer) {
                if (selectedInterval.equalsIgnoreCase(P3M))
                    presenter.fetchSellerDashboardDataForUser(mSelectedUser);
                else if (selectedInterval.equals(WEEK))
                    presenter.fetchSellerDashboardDataForWeek(mSelectedUser);
                else {
                    if (type.equals(ROUTE))
                        presenter.fetchRouteDashboardData(selectedInterval);
                    else
                        presenter.fetchSellerDashboardForUserAndInterval(mSelectedUser, selectedInterval);

                }
            } else
                presenter.fetchRetailerDashboard(selectedInterval);

            monthSpinnerStub.setVisibility(View.GONE);

            if (selectedInterval.equalsIgnoreCase(DAY) && mSelectedUser.equals(String.valueOf(presenter.getCurrentUser().getUserid()))) {
                presenter.computeDayAchievements();
                dashboardListAdapter.notifyDataSetChanged();
            } else if (selectedInterval.equals(P3M))
                presenter.fetchKpiMonths(isFromRetailer);
            else if (selectedInterval.equals(WEEK))
                presenter.fetchWeeks();
            else {
                weekSpinnerStub.setVisibility(View.GONE);
                dashboardListAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    @Override
    public void setUpMonthSpinner(ArrayList<String> monthList) {
        monthSpinnerStub.setVisibility(View.VISIBLE);
        weekSpinnerStub.setVisibility(View.GONE);
        Spinner monthSpinner = (Spinner) monthSpinnerStub;
        ArrayAdapter<String> monthdapter = new ArrayAdapter<>(getActivity(), R.layout.dashboard_spinner_layout, monthList);
        monthdapter.setDropDownViewResource(R.layout.dashboard_spinner_list);
        monthSpinner.setAdapter(monthdapter);
        monthSpinner.setOnItemSelectedListener(monthSelectedListener);
        monthSpinner.setSelection(0);
    }

    @Override
    public void setWeekSpinner(ArrayList<String> weekList, int currentWeek) {
        weekSpinnerStub.setVisibility(View.VISIBLE);
        Spinner weekSpinner = weekSpinnerStub;
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(getActivity(), R.layout.dashboard_spinner_layout, weekList);
        monthAdapter.setDropDownViewResource(R.layout.dashboard_spinner_list);
        weekSpinner.setAdapter(monthAdapter);
        weekSpinner.setOnItemSelectedListener(weekSelectedListener);
        weekSpinner.setSelection(currentWeek);



    }

    @Override
    public void onFactorNameClick(int position) {
        updateChartData(dashboardListData.get(position));
    }

    private void updateChartData(DashBoardBO currentItem){
        updateChartData(currentItem,false);
    }

    private void updateChartData(DashBoardBO currentItem, boolean isFromWeekSpinner) {
        if(presenter.shouldShowTrendChart()){

            DashBoardEventData dashBoardEventData = new DashBoardEventData();

            if((((selectedInterval.equalsIgnoreCase(WEEK)||isFromWeekSpinner) && !currentItem.getMonthName().equals(""))
                    || selectedInterval.equals(P3M)) &&  presenter.shouldShowKPIBarChart()){
                kpiChartData.clear();
                for (DashBoardBO dashBoardBO : presenter.getDashboardListData()) {
                    if (dashBoardBO.getCode().equalsIgnoreCase(currentItem.getCode())) {
                        kpiChartData.add(dashBoardBO);
                    }
                }
                dashBoardEventData.setEventDataList(kpiChartData);
                dashBoardEventData.setSource(DASHBOARD);
            }

            if (presenter.shouldShowSMPDash())
                dashBoardEventData.setSmpDashBoardData(currentItem);



            if(presenter.shouldShowP3MDash())
                dashBoardEventData.setKpiLovId(currentItem.getKpiTypeLovID());

            EventBus.getDefault().post(dashBoardEventData);

        }
    }

    @Override
    public void onSkuWiseClick(int position) {

    }

    private AdapterView.OnItemSelectedListener weekSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            String weekName = adapterView.getSelectedItem().toString();
            for (DashBoardBO dashBoardBO : presenter.getDashboardListData()) {
                if (dashBoardBO.getMonthName().equalsIgnoreCase(weekName) || weekName.equals("")) {
                    dashboardListData.add(dashBoardBO);
                }
            }

            updateChartData(dashboardListData.get(0),true);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    private AdapterView.OnItemSelectedListener monthSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            String filterName = adapterView.getSelectedItem().toString();

            dashboardListData.clear();

            for (DashBoardBO dashBoardBO : presenter.getDashboardListData()) {
                if (dashBoardBO.getMonthName().equalsIgnoreCase(filterName)) {
                    dashboardListData.add(dashBoardBO);
                }
            }

            dashboardListAdapter = new DashboardListAdapter(getActivity(), dashboardListData, presenter.getLabelsMap(), SellerDashboardFragment.this);
            dashboardRecyclerView.setAdapter(dashboardListAdapter);
            
            if(presenter.shouldShowTrendChart()){

                generatePagerFragments();

                updateChartData(dashboardListData.get(0));

            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    private AdapterView.OnItemSelectedListener userSpinnerListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            mSelectedUser = String.valueOf(((UserMasterBO) adapterView.getSelectedItem()).getUserid());

            if (!isFromRetailer) {
                if (selectedInterval.equalsIgnoreCase(P3M))
                    presenter.fetchSellerDashboardDataForUser(mSelectedUser);
                else
                    presenter.fetchSellerDashboardForUserAndInterval(mSelectedUser, selectedInterval);
            }

            if (presenter.shouldShowTrendChart()) {
                //TODO Add chart fragments
                presenter.fetchP3MSellerDashboardData();
            }

        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };


    private AdapterView.OnItemSelectedListener distributorSpinnerListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            int mSelectedDistributorId = SDUtil.convertToInt(((DistributorMasterBO) adapterView.getSelectedItem()).getDId());

            presenter.fetchUserList(Integer.toString(mSelectedDistributorId), false);

        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    private void generatePagerFragments() {

        fragments = new ArrayList<>();

        if (presenter.isSMPBasedDash()) {
            if (!selectedInterval.matches("WEEK|ROUTE")) {
                if (presenter.shouldShowP3MDash()) {
                    presenter.fetchP3mTrendChartData(mSelectedUser);
                }
                if (presenter.shouldShowSMPDash()) {
                    SMPChartFragment smpChartFragment = new SMPChartFragment();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("dashboardData", dashboardListData.get(0));
                    smpChartFragment.setArguments(bundle);
                    fragments.add(smpChartFragment);

                }
            }
        }

        if (presenter.shouldShowKPIBarChart()) {
            kpiChartData = new ArrayList<>();
            KPIBarChartFragment kpiBarChartFragment = new KPIBarChartFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("dashChartList", dashboardListData);
            bundle.putString("selectedInterval", selectedInterval);
            kpiBarChartFragment.setArguments(bundle);
            fragments.add(kpiBarChartFragment);
        }

        FragmentPagerAdapter viewPagerAdapter = new FragmentPagerAdapter(getActivity().getSupportFragmentManager(), fragments);

        pager.setAdapter(viewPagerAdapter);
        circleIndicatorView.setViewPager(pager);
    }

    @Override
    public void createP3MChartFragment(ArrayList<DashBoardBO> dashBoardBOS) {
        P3MChartFragment p3MChartFragment = new P3MChartFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("dashChartList", dashBoardBOS);
        bundle.putInt("paramLovId", dashBoardBOS.size() > 0 ? dashBoardBOS.get(0).getKpiTypeLovID() : 0);
        p3MChartFragment.setArguments(bundle);
        fragments.add(0, p3MChartFragment);

        if (pager.getAdapter() != null)
            pager.getAdapter().notifyDataSetChanged();
    }

}
