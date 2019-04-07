package com.ivy.cpg.view.dashboard.olddashboard;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.ivy.cpg.view.dashboard.DashBoardBO;
import com.ivy.cpg.view.dashboard.DashBoardHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.cpg.view.homescreen.HomeScreenActivity;
import com.ivy.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends IvyBaseFragment implements DashboardContractor.DashboardView {

    private BusinessModel bmodel;
    private RecyclerView dashBoardListRV;
    private Spinner mDashBoardFilterSpin;
    private Spinner spn_sub_filter;
    private ArrayAdapter<String> mMonthNameAdapter;
    private ArrayAdapter<String> mQuarterNameAdapter;
    private FragmentManager fm;

    private String retid = "0";
    private String type;
    private int mSelectedMonthPos = 0;
    private static final String MONTH_TYPE = "MONTH";
    private static final String YEAR_TYPE = "YEAR";
    private static final String DAY_TYPE = "DAY";
    private static final String QUARTER_TYPE = "QUARTER";
    private static final String ALL_TYPE = "ALL";
    private boolean isFromPlanning = false;
    private String menuCode = "";

    private View view;
    boolean isFromHomeScreenTwo = false;

    private DashboardContractor.DashboardPresenter dashboardPresenter;
    private DashBoardHelper dashBoardHelper;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        dashBoardHelper = DashBoardHelper.getInstance(getActivity());
        dashboardPresenter = new DashBoardPresenterImpl(getContext());
        dashboardPresenter.setView(this);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        if (getArguments().getString("retid") == null)
            retid = "0";
        else
            retid = getArguments().getString("retid");

        type = DAY_TYPE;

        isFromPlanning = getArguments().getBoolean("planning");

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        fm = getActivity().getSupportFragmentManager();
        //to check whether dashboard is called from homescreentwo class
        if (getActivity().getIntent().getExtras() != null) {
            if (getActivity().getIntent().getBooleanExtra("isFromHomeScreenTwo", false)) {
                isFromHomeScreenTwo = true;
            }
            menuCode = getActivity().getIntent().getStringExtra("menuCode");
        }

        setUpActionBar();
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        dashboardPresenter.loadDownloadMethods(retid, type);

        dashBoardListRV = view.findViewById(R.id.dashboardRv);
        dashBoardListRV.setHasFixedSize(false);
        dashBoardListRV.setNestedScrollingEnabled(false);
        dashBoardListRV.setLayoutManager(new LinearLayoutManager(getActivity()));

        dashboardPresenter.updateProductiveAndPlanedCall();
        dashBoardHelper.downloadTotalValuesAndQty();

        mDashBoardFilterSpin = view.findViewById(R.id.spin_dashboard);
        spn_sub_filter = view.findViewById(R.id.spin_month);


        final ArrayList<String> dashBoardFilterList = dashBoardHelper.getDashBoardFilter();

        final ArrayList<String> monthNameList = dashBoardHelper.getMonthNameList();
        if (monthNameList.size() > 0) {
            mMonthNameAdapter = new ArrayAdapter<>(getActivity(), R.layout.dashboard_spinner_layout, monthNameList);
            mMonthNameAdapter.setDropDownViewResource(R.layout.dashboard_spinner_list);

        }

        final ArrayList<String> quarterNameList = dashBoardHelper.getQuarterNameList();
        if (quarterNameList.size() > 0) {
            mQuarterNameAdapter = new ArrayAdapter<>(getActivity(), R.layout.dashboard_spinner_layout, quarterNameList);
            mQuarterNameAdapter.setDropDownViewResource(R.layout.dashboard_spinner_list);
        }

        if (dashBoardFilterList != null && !dashBoardFilterList.isEmpty()) {
            ArrayAdapter<String> mDashBoardFilterAdapter = new ArrayAdapter<>(getActivity(), R.layout.dashboard_spinner_layout, dashBoardFilterList);
            mDashBoardFilterAdapter.setDropDownViewResource(R.layout.dashboard_spinner_list);
            mDashBoardFilterSpin.setAdapter(mDashBoardFilterAdapter);

        } else {
            mDashBoardFilterSpin.setVisibility(View.GONE);
            spn_sub_filter.setVisibility(View.GONE);
            dashboardPresenter.computeDashboardList(ALL_TYPE, "");
        }

        mDashBoardFilterSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String filterName = mDashBoardFilterSpin.getSelectedItem().toString();
                switch (filterName) {

                    case YEAR_TYPE:
                        type = YEAR_TYPE;
                        spn_sub_filter.setVisibility(View.GONE);
                        dashboardPresenter.computeDashboardList(YEAR_TYPE, "");
                        break;

                    case MONTH_TYPE:
                        type = MONTH_TYPE;
                        if (!monthNameList.isEmpty()) {
                            spn_sub_filter.setVisibility(View.VISIBLE);
                            spn_sub_filter.setAdapter(mMonthNameAdapter);
                            spn_sub_filter.setSelection(dashBoardHelper.getCurrentMonthIndex());
                        } else {
                            spn_sub_filter.setVisibility(View.GONE);
                            dashboardPresenter.computeDashboardList(MONTH_TYPE, "");
                        }
                        break;

                    case DAY_TYPE:
                        type = DAY_TYPE;
                        spn_sub_filter.setVisibility(View.GONE);
                        dashboardPresenter.computeDashboardList(DAY_TYPE, "");
                        break;

                    case QUARTER_TYPE:
                        type = QUARTER_TYPE;
                        if (!quarterNameList.isEmpty()) {
                            spn_sub_filter.setVisibility(View.VISIBLE);
                            spn_sub_filter.setAdapter(mQuarterNameAdapter);
                        } else {
                            spn_sub_filter.setVisibility(View.GONE);
                            dashboardPresenter.computeDashboardList(QUARTER_TYPE, "");
                        }
                        break;

                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // no operation
            }
        });

        spn_sub_filter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String filterName = spn_sub_filter.getSelectedItem().toString();
                if (type.equals(MONTH_TYPE)) {
                    dashboardPresenter.computeDashboardList(MONTH_TYPE, filterName);
                    mSelectedMonthPos = position;
                } else if (type.equals(QUARTER_TYPE)) {
                    dashboardPresenter.computeDashboardList(QUARTER_TYPE, filterName);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //no operation
            }
        });
    }


    private void setUpActionBar() {
        ((AppCompatActivity) getActivity()).getSupportActionBar();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setElevation(0);
        }

//screen title set based previous screen
        if (!isFromHomeScreenTwo) {
            if (bmodel.getMenuName("MENU_DASH").endsWith(""))
                bmodel.configurationMasterHelper.downloadMainMenu();
            if (getArguments().getString("screentitle") == null)
                setScreenTitle(bmodel.getMenuName("MENU_DASH"));
            else
                setScreenTitle(getArguments().getString("screentitle"));
        } else {

            if (getActivity().getIntent().getStringExtra("screentitle").toString().isEmpty()) {
                bmodel.configurationMasterHelper
                        .downloadNewActivityMenu(ConfigurationMasterHelper.MENU_ACTIVITY);
                setScreenTitle(bmodel.getMenuName("MENU_DASH_ACT"));
            } else
                setScreenTitle(getActivity().getIntent().getStringExtra("screentitle"));
        }
        //if (!BusinessModel.dashHomeStatic)
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_target_plan, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_skutgt).setVisible(false);
        /*if (BusinessModel.dashHomeStatic)
            menu.findItem(R.id.menu_next).setVisible(true);*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            //back navigation based on previous screen
            if (isFromHomeScreenTwo) {
                bmodel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
                        .now(DateTimeUtils.TIME));
                bmodel.saveModuleCompletion(menuCode, true);
                getActivity().finish();
            } else {
                getActivity().finish();
                bmodel.saveModuleCompletion(menuCode, false);
            }
            getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            return true;
        } else if (i == R.id.menu_next) {
            Intent intent = new Intent(getActivity(), HomeScreenActivity.class);
            startActivity(intent);
            getActivity().finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {

            if (dashBoardListRV != null)
                dashBoardListRV = null;
            if (fm != null)
                fm = null;
        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }

    @Override
    public void updateDashboardList(List<DashBoardBO>dashBoardList) {

            String monthName = null;
            if (mMonthNameAdapter != null)
                monthName = mMonthNameAdapter.getItem(mSelectedMonthPos);

            DashBoardListViewAdapter adapter = new DashBoardListViewAdapter(bmodel, dashBoardList, type, retid, monthName);
            dashBoardListRV.setAdapter(adapter);

    }

    @Override
    public void gridListDataLoad(int position) {
        if (position == 0) {
            dashBoardHelper.getGridData(0);

        } else {
            dashBoardHelper.getGridData((String) dashBoardHelper
                    .getBeatList().get(position));
        }
    }
}
