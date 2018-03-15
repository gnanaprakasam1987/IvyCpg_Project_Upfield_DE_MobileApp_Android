package com.ivy.sd.png.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
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
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.DashBoardBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends IvyBaseFragment {

    private BusinessModel bmodel;
    private final int beatPosition = 0;
    private RecyclerView dashBoardListRV;
    private Spinner mDashBoardFilterSpin;
    private Spinner spn_sub_filter;
    private ArrayAdapter<String> mMonthNameAdapter;
    private ArrayAdapter<String> mQuarterNameAdapter;
    private FragmentManager fm;

    private AlertDialog alertDialog;
    private Boolean showinitiavite = true;
    private String retid = "0";
    private String type;
    private List<DashBoardBO> mDashboardList;
    private int mSelectedMonthPos = 0;
    private static final String MONTH_TYPE = "MONTH";
    private static final String YEAR_TYPE = "YEAR";
    private static final String DAY_TYPE = "DAY";
    private static final String QUARTER_TYPE = "QUARTER";
    private static final String ALL_TYPE = "ALL";
    private static final String JOURNEY_PLAN_CALL = "JPC";
    private static final String PRODUCTIVE_CALL = "PDC";
    private static final String MONTH_TAG = "incentive_month";
    private static final String YEAR_TAG = "incentive_year";

    private static final String ACH_MONTH_TAG = "achieved_month";
    private static final String ACH_YEAR_TAG = "achieved_year";

    private static final String TGT_MONTH_TAG = "target_month";
    private static final String TGT_YEAR_TAG = "target_year";
    private boolean isFromPlanning = false;
    private String menuCode = "";

    private View view;
    boolean isFromHomeScreenTwo = false;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
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


        bmodel.dashBoardHelper.findMinMaxProductLevel(retid);
        bmodel.dashBoardHelper.loadDashBoard(retid);

        if (type.equalsIgnoreCase(DAY_TYPE)) {
            bmodel.dashBoardHelper.downloadDashboardLevelSkip(0);
            bmodel.dashBoardHelper.downloadTotalValuesAndQty();
        }

        dashBoardListRV = (RecyclerView) view.findViewById(R.id.dashboardRv);
        dashBoardListRV.setHasFixedSize(false);
        dashBoardListRV.setNestedScrollingEnabled(false);
        dashBoardListRV.setLayoutManager(new LinearLayoutManager(getActivity()));

        gridListDataLoad(beatPosition);
        mDashboardList = bmodel.dashBoardHelper.getDashListViewList();
        updateProductiveAndPlanedCall();
        bmodel.dashBoardHelper.downloadTotalValuesAndQty();

        mDashBoardFilterSpin = (Spinner) view.findViewById(R.id.spin_dashboard);
        spn_sub_filter = (Spinner) view.findViewById(R.id.spin_month);


        final ArrayList<String> dashBoardFilterList = bmodel.dashBoardHelper.getDashBoardFilter();

        final ArrayList<String> monthNameList = bmodel.dashBoardHelper.getMonthNameList();
        mMonthNameAdapter = new ArrayAdapter<>(getActivity(), R.layout.dashboard_spinner_layout, monthNameList);
        mMonthNameAdapter.setDropDownViewResource(R.layout.dashboard_spinner_list);

        final ArrayList<String> quarterNameList = bmodel.dashBoardHelper.getQuarterNameList();
        mQuarterNameAdapter = new ArrayAdapter<>(getActivity(), R.layout.dashboard_spinner_layout, quarterNameList);
        mQuarterNameAdapter.setDropDownViewResource(R.layout.dashboard_spinner_list);

        if (dashBoardFilterList != null && !dashBoardFilterList.isEmpty()) {
            ArrayAdapter<String> mDashBoardFilterAdapter = new ArrayAdapter<>(getActivity(), R.layout.dashboard_spinner_layout, dashBoardFilterList);
            mDashBoardFilterAdapter.setDropDownViewResource(R.layout.dashboard_spinner_list);
            mDashBoardFilterSpin.setAdapter(mDashBoardFilterAdapter);

        } else {
            mDashBoardFilterSpin.setVisibility(View.GONE);
            spn_sub_filter.setVisibility(View.GONE);
            updateDashboardList(ALL_TYPE, "");
        }

        mDashBoardFilterSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String filterName = mDashBoardFilterSpin.getSelectedItem().toString();
                switch (filterName) {

                    case YEAR_TYPE:
                        type = YEAR_TYPE;
                        spn_sub_filter.setVisibility(View.GONE);
                        updateDashboardList(YEAR_TYPE, "");
                        break;

                    case MONTH_TYPE:
                        type = MONTH_TYPE;
                        if (!monthNameList.isEmpty()) {
                            spn_sub_filter.setVisibility(View.VISIBLE);
                            spn_sub_filter.setAdapter(mMonthNameAdapter);
                            spn_sub_filter.setSelection(bmodel.dashBoardHelper.getCurrentMonthIndex());
                        } else {
                            spn_sub_filter.setVisibility(View.GONE);
                            updateDashboardList(MONTH_TYPE, "");
                        }
                        break;

                    case DAY_TYPE:
                        type = DAY_TYPE;
                        spn_sub_filter.setVisibility(View.GONE);
                        updateDashboardList(DAY_TYPE, "");
                        break;

                    case QUARTER_TYPE:
                        type = QUARTER_TYPE;
                        if (!quarterNameList.isEmpty()) {
                            spn_sub_filter.setVisibility(View.VISIBLE);
                            spn_sub_filter.setAdapter(mQuarterNameAdapter);
                        } else {
                            spn_sub_filter.setVisibility(View.GONE);
                            updateDashboardList(QUARTER_TYPE, "");
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
                    updateDashboardList(MONTH_TYPE, filterName);
                    mSelectedMonthPos = position;
                } else if (type.equals(QUARTER_TYPE)) {
                    updateDashboardList(QUARTER_TYPE, filterName);
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
                bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                        .now(SDUtil.TIME));
                bmodel.saveModuleCompletion(menuCode);
                getActivity().finish();
            } else {
                Intent j = new Intent(getActivity(), HomeScreenActivity.class);
                if (isFromPlanning)
                    j.putExtra("menuCode", "MENU_PLANNING_SUB");
                else
                    j.putExtra("menuCode", "MENU_LOAD_MANAGEMENT");
                startActivity(j);
                getActivity().finish();
            }
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

    private void gridListDataLoad(int position) {
        if (position == 0) {
            bmodel.dashBoardHelper.getGridData(0);
            if (bmodel.configurationMasterHelper.SHOW_INCENTIVE_DASH) {
                showinitiavite = true;
            }
        } else {
            bmodel.dashBoardHelper.getGridData((String) bmodel.dashBoardHelper
                    .getBeatList().get(position));
            showinitiavite = false;
        }
    }


    private void updateDashboardList(String type, String subFilter) {
        if (mDashboardList != null) {
            List<DashBoardBO> dashBoardList;
            dashBoardList = new ArrayList<>();
            if (type.equalsIgnoreCase(MONTH_TYPE)) {

                for (DashBoardBO dashBoardBO : mDashboardList) {
                    if (dashBoardBO.getType().equals(MONTH_TYPE)) {
                        if (dashBoardBO.getMonthName() != null) {
                            if (dashBoardBO.getMonthName().equalsIgnoreCase(subFilter)) {
                                dashBoardList.add(dashBoardBO);

                            }
                        } else {
                            dashBoardList.add(dashBoardBO);

                        }
                    }

                }
            } else if (type.equalsIgnoreCase(YEAR_TYPE)) {
                for (DashBoardBO dashBoardBO : mDashboardList) {

                    if (dashBoardBO.getType().equalsIgnoreCase(YEAR_TYPE)) {
                        dashBoardList.add(dashBoardBO);

                    }
                }

            } else if (type.equals(ALL_TYPE)) {
                for (DashBoardBO dashBoardBO : mDashboardList) {
                    dashBoardList.add(dashBoardBO);
                }

            } else if (type.equals(DAY_TYPE)) {
                for (DashBoardBO dashBoardBO : mDashboardList) {

                    if (dashBoardBO.getType().equalsIgnoreCase(DAY_TYPE)) {
                        dashBoardList.add(dashBoardBO);

                    }
                }

            } else if (type.equalsIgnoreCase(QUARTER_TYPE)) {
                String[] monthLimit = subFilter.split("-");
                int startingMonth = getMonthCount(monthLimit[0]);
                int endingMonth = getMonthCount(monthLimit[1]);

                for (DashBoardBO dashBoardBO : mDashboardList) {
                    if (dashBoardBO.getType().equals(QUARTER_TYPE)) {
                        if (dashBoardBO.getMonthName() != null) {
                            if (getMonthCount(dashBoardBO.getMonthName()) >= startingMonth && getMonthCount(dashBoardBO.getMonthName()) <= endingMonth) {
                                dashBoardList.add(dashBoardBO);

                            }
                        } else {
                            dashBoardList.add(dashBoardBO);

                        }
                    }
                }

            }

            DashBoardListViewAdapter adapter = new DashBoardListViewAdapter(dashBoardList);
            dashBoardListRV.setAdapter(adapter);
        }
    }

    private void updateProductiveAndPlanedCall() {
        for (DashBoardBO dashBoardBO : mDashboardList) {
            if (dashBoardBO.getType().equalsIgnoreCase(DAY_TYPE)) {
                if (dashBoardBO.getCode().equals(JOURNEY_PLAN_CALL)) {
                    final int totalcalls = bmodel.getTotalCallsForTheDay();
                    final int visitedcalls = bmodel.getVisitedCallsForTheDay();
                    dashBoardBO.setTarget(totalcalls);
                    dashBoardBO.setAcheived(visitedcalls);

                    if (dashBoardBO.getTarget() > 0) {
                        dashBoardBO.setCalculatedPercentage(SDUtil.convertToFloat(SDUtil.roundIt((dashBoardBO.getAcheived() / dashBoardBO.getTarget() * 100), 2)));
                        if (dashBoardBO.getCalculatedPercentage() >= 100) {
                            dashBoardBO.setConvTargetPercentage(0);
                            dashBoardBO.setConvAcheivedPercentage(100);
                        } else {
                            dashBoardBO.setConvTargetPercentage(100 - dashBoardBO
                                    .getCalculatedPercentage());
                            dashBoardBO.setConvAcheivedPercentage(dashBoardBO
                                    .getCalculatedPercentage());
                        }
                    } else if (dashBoardBO.getTarget() <= 0 && dashBoardBO.getAcheived() > 0) {
                        dashBoardBO.setConvTargetPercentage(0);
                        dashBoardBO.setConvAcheivedPercentage(100);
                    }


                } else if (dashBoardBO.getCode().equals(PRODUCTIVE_CALL)) {
                    final int totalcalls = bmodel.getTotalCallsForTheDay();
                    double targetProductiveCalls = totalcalls * 0.25;
                    final int productivecalls = bmodel.getProductiveCallsForTheDay();
                    dashBoardBO.setTarget(targetProductiveCalls);
                    dashBoardBO.setAcheived(productivecalls);

                    if (dashBoardBO.getTarget() > 0) {
                        dashBoardBO.setCalculatedPercentage(SDUtil.convertToFloat(SDUtil.roundIt(((dashBoardBO.getAcheived() / dashBoardBO.getTarget()) * 100), 2)));
                        if (dashBoardBO.getCalculatedPercentage() >= 100) {
                            dashBoardBO.setConvTargetPercentage(0);
                            dashBoardBO.setConvAcheivedPercentage(100);
                        } else {
                            dashBoardBO.setConvTargetPercentage(100 - dashBoardBO
                                    .getCalculatedPercentage());
                            dashBoardBO.setConvAcheivedPercentage(dashBoardBO
                                    .getCalculatedPercentage());
                        }
                    } else if (dashBoardBO.getTarget() <= 0 && dashBoardBO.getAcheived() > 0) {
                        dashBoardBO.setConvTargetPercentage(0);
                        dashBoardBO.setConvAcheivedPercentage(100);
                    }
                }
            }
        }
    }

    private String appendZero(String value) {
        if (value.length() == 1)
            value = value + "0";
        return value;
    }

    private static int getMonthCount(String monthName) {
        switch (monthName) {
            case "January":
                return 1;
            case "February":
                return 2;
            case "March":
                return 3;
            case "April":
                return 4;
            case "May":
                return 5;
            case "June":
                return 6;
            case "July":
                return 7;
            case "August":
                return 8;
            case "September":
                return 9;
            case "October":
                return 10;
            case "November":
                return 11;
            case "December":
                return 12;
            default:
                return 1;
        }
    }

    public class DashBoardListViewAdapter extends RecyclerView.Adapter<DashBoardListViewAdapter.ViewHolder> {
        private final List<DashBoardBO> dashboardList;

        public DashBoardListViewAdapter(List<DashBoardBO> dashboardList) {
            this.dashboardList = dashboardList;
        }

        @Override
        public DashBoardListViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.seller_dashboard_row_layout, parent, false);
            return new DashBoardListViewAdapter.ViewHolder(v);
        }


        @Override
        public void onBindViewHolder(final DashBoardListViewAdapter.ViewHolder holder, int position) {
            final DashBoardBO dashboardData = dashboardList.get(position);
            if (!showinitiavite
                    || !bmodel.configurationMasterHelper.SHOW_INCENTIVE_DASH) {
                holder.incentive.setVisibility(View.GONE);
                holder.incentiveTitle.setVisibility(View.GONE);
            } else {
                holder.incentive.setVisibility(View.VISIBLE);
                holder.incentiveTitle.setVisibility(View.VISIBLE);
            }

            holder.dashboardDataObj = dashboardData;
            //typefaces

            holder.factorName.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
            holder.target.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.acheived.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.balance.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.index.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.incentive.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.score.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.incentiveTitle.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.scoreTitle.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.targetTitle.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.acheivedTitle.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.balanceTitle.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.tvSkuWise.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));


            holder.factorName.setText(dashboardData.getText());

            if (bmodel.configurationMasterHelper.SHOW_LINK_DASH_SKUTGT
                    && ("MONTH".equalsIgnoreCase(dashboardData.getType()) || "DAY"
                    .equalsIgnoreCase(dashboardData.getType()))
                    && dashboardData.getRouteID() == 0
                    && dashboardData.getIsFlip() == 0
                    && ("SV".equalsIgnoreCase(dashboardData.getCode()) || "VOL".equalsIgnoreCase(dashboardData
                    .getCode()) || "SVSKU".equalsIgnoreCase(dashboardData.getCode()))) {
                holder.tvSkuWise.setVisibility(View.VISIBLE);
                holder.verticalSkuWise.setVisibility(View.VISIBLE);
                SpannableString str = new SpannableString(holder.tvSkuWise
                        .getText().toString());
                str.setSpan(new UnderlineSpan(), 0, str.length(),
                        Spanned.SPAN_PARAGRAPH);
                str.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.Black)), 0,
                        str.length(), 0);
                holder.tvSkuWise.setText(str);
            } else {
                holder.tvSkuWise.setVisibility(View.GONE);
                holder.verticalSkuWise.setVisibility(View.GONE);
            }


            holder.tvSkuWise.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    try {
                        if (bmodel.configurationMasterHelper.SHOW_LINK_DASH_SKUTGT
                                && (holder.dashboardDataObj.getType()
                                .equalsIgnoreCase(MONTH_TYPE) || holder.dashboardDataObj
                                .getType().equalsIgnoreCase(DAY_TYPE) || holder.dashboardDataObj.getType().equalsIgnoreCase(YEAR_TYPE))
                                && holder.dashboardDataObj.getRouteID() == 0
                                && holder.dashboardDataObj.getIsFlip() == 0
                                && ("SV".equalsIgnoreCase(holder.dashboardDataObj.getCode()) || "VOL".equalsIgnoreCase(holder.dashboardDataObj
                                .getCode()) || "SVSKU".equalsIgnoreCase(holder.dashboardDataObj.getCode()))) {
                            Intent i = new Intent(getActivity(),
                                    SKUWiseTargetActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            i.putExtra("screentitle",
                                    bmodel.getMenuName("MENU_SKUWISESTGT"));
                            i.putExtra("screentitlebk",
                                    ((AppCompatActivity) getActivity()).getSupportActionBar().getTitle());
                            i.putExtra("from", "4");
                            i.putExtra("flex1", holder.dashboardDataObj.getFlex1());
                            i.putExtra("rid", retid);
                            i.putExtra("type", holder.dashboardDataObj.getType());
                            i.putExtra("code",
                                    holder.dashboardDataObj.getCode());
                            i.putExtra("pid",
                                    holder.dashboardDataObj.getPId());
                            i.putExtra("isFromDash", true);
                            if (mMonthNameAdapter != null)
                                i.putExtra("month_name", mMonthNameAdapter.getItem(mSelectedMonthPos));

                            startActivity(i);
                        } else {
                            holder.factorName.setClickable(false);
                        }
                    } catch (Exception e) {
                        Commons.printException(e + "");
                    }
                }
            });

            if (dashboardData.getFlex1() == 1) {
                if ("PDC".equalsIgnoreCase(dashboardData.getCode())) {
                    //after decimal point value
                    String dec_target;
                    String dec_ach;
                    String dec_balance;
                    String dec_inc;
                    //before decimal point value
                    String target;
                    String ach;
                    String balance;
                    String inc;

                    dec_target = appendZero(String.valueOf(dashboardData.getTarget()).substring(String.valueOf(dashboardData.getTarget()).indexOf(".")).substring(1));
                    target = bmodel.dashBoardHelper.getWhole(dashboardData.getTarget() + "");

                    dec_ach = appendZero(String.valueOf(dashboardData.getAcheived()).substring(String.valueOf(dashboardData.getAcheived()).indexOf(".")).substring(1));
                    ach = bmodel.dashBoardHelper.getWhole(dashboardData.getAcheived() + "");

                    dec_balance = appendZero(String.valueOf(dashboardData.getTarget() - dashboardData.getAcheived()).substring
                            (String.valueOf(dashboardData.getTarget() - dashboardData.getAcheived()).indexOf(".")).substring(1));
                    balance = bmodel.dashBoardHelper.getWhole((dashboardData.getTarget() - dashboardData.getAcheived()) + "");

                    dec_inc = appendZero(String.valueOf(dashboardData.getIncentive()).substring(String.valueOf(dashboardData.getIncentive()).indexOf(".")).substring(1));
                    inc = bmodel.dashBoardHelper.getWhole(dashboardData.getIncentive() + "");

                    if (Integer.parseInt(dec_target) >= 25)
                        holder.target.setText(bmodel.dashBoardHelper.getWhole(Math.ceil(dashboardData.getTarget()) + ""));
                    else
                        holder.target.setText(target);
                    if (Integer.parseInt(dec_ach) >= 25)
                        holder.acheived.setText(bmodel.dashBoardHelper.getWhole(Math.ceil(dashboardData.getAcheived()) + ""));
                    else
                        holder.acheived.setText(ach);
                    if (Integer.parseInt(dec_balance) >= 25)
                        holder.balance.setText(bmodel.dashBoardHelper.getWhole(Math.ceil(dashboardData.getTarget() - dashboardData.getAcheived()) + ""));
                    else
                        holder.balance.setText(balance);
                    if (Integer.parseInt(dec_inc) >= 25)
                        holder.incentive.setText(bmodel.dashBoardHelper.getWhole(Math.ceil(dashboardData.getIncentive()) + ""));
                    else
                        holder.incentive.setText(inc);
                } else {
                    holder.target.setText(SDUtil.format(dashboardData.getTarget(), 0, bmodel.configurationMasterHelper.VALUE_COMMA_COUNT));
                    holder.acheived.setText(SDUtil.format(dashboardData.getAcheived(), 0, bmodel.configurationMasterHelper.VALUE_COMMA_COUNT));
                    holder.balance.setText(SDUtil.format(dashboardData.getTarget() - dashboardData.getAcheived(), 0, bmodel.configurationMasterHelper.VALUE_COMMA_COUNT));
                    holder.incentive.setText(SDUtil.format(dashboardData.getIncentive(), 0, bmodel.configurationMasterHelper.VALUE_COMMA_COUNT));
                }

                String strCalcPercentage = bmodel.formatPercent(dashboardData.getCalculatedPercentage()) + "%";
                holder.index.setText(strCalcPercentage);

            } else {
                if ("PDC".equalsIgnoreCase(dashboardData.getCode())) {
                    //after decimal point value
                    String dec_target;
                    String dec_ach;
                    String dec_balance;
                    String dec_inc;
                    //  before decimal point value
                    String target;
                    String ach;
                    String balance;
                    String inc;

                    dec_target = appendZero(String.valueOf(dashboardData.getTarget()).substring(String.valueOf(dashboardData.getTarget()).indexOf(".")).substring(1));
                    target = bmodel.dashBoardHelper.getWhole(dashboardData.getTarget() + "");

                    dec_ach = appendZero(String.valueOf(dashboardData.getAcheived()).substring(String.valueOf(dashboardData.getAcheived()).indexOf(".")).substring(1));
                    ach = bmodel.dashBoardHelper.getWhole(dashboardData.getAcheived() + "");

                    dec_balance = appendZero(String.valueOf(dashboardData.getTarget() - dashboardData.getAcheived()).substring
                            (String.valueOf(dashboardData.getTarget() - dashboardData.getAcheived()).indexOf(".")).substring(1));
                    balance = bmodel.dashBoardHelper.getWhole((dashboardData.getTarget() - dashboardData.getAcheived()) + "");

                    dec_inc = appendZero(String.valueOf(dashboardData.getIncentive()).substring(String.valueOf(dashboardData.getIncentive()).indexOf(".")).substring(1));
                    inc = bmodel.dashBoardHelper.getWhole(dashboardData.getIncentive() + "");

                    if (Integer.parseInt(dec_target) >= 25)
                        holder.target.setText(bmodel.dashBoardHelper.getWhole(Math.ceil(dashboardData.getTarget()) + ""));
                    else
                        holder.target.setText(target);
                    if (Integer.parseInt(dec_ach) >= 25)
                        holder.acheived.setText(bmodel.dashBoardHelper.getWhole(Math.ceil(dashboardData.getAcheived()) + ""));
                    else
                        holder.acheived.setText(ach);
                    if (Integer.parseInt(dec_balance) >= 25)
                        holder.balance.setText(bmodel.dashBoardHelper.getWhole(Math.ceil(dashboardData.getTarget() - dashboardData.getAcheived()) + ""));
                    else
                        holder.balance.setText(balance);
                    if (Integer.parseInt(dec_inc) >= 25)
                        holder.incentive.setText(bmodel.dashBoardHelper.getWhole(Math.ceil(dashboardData.getIncentive()) + ""));
                    else
                        holder.incentive.setText(inc);
                } else {
                    holder.target.setText(bmodel.formatValue(dashboardData.getTarget()));
                    holder.acheived.setText(bmodel.formatValue(dashboardData.getAcheived()));
                    holder.balance.setText(bmodel.formatValue(dashboardData.getTarget() - dashboardData.getAcheived()));
                    String strIncentive = bmodel.formatValue(dashboardData.getIncentive()) + "";
                    holder.incentive.setText(strIncentive);
                }

                String strCalcPercentage = bmodel.formatPercent(dashboardData.getCalculatedPercentage()) + "%";
                holder.index.setText(strCalcPercentage);
            }

            ArrayList<PieEntry> entries = new ArrayList<PieEntry>();

            entries.add(new PieEntry((float) dashboardData.getConvAcheivedPercentage()));
            entries.add(new PieEntry((float) dashboardData.getConvTargetPercentage()));

            PieDataSet dataSet = new PieDataSet(entries, "");

            dataSet.setSliceSpace(3f);
            dataSet.setSelectionShift(5f);

            // add a lot of colors

            ArrayList<Integer> colors = new ArrayList<Integer>();

            colors.add(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
            colors.add(ContextCompat.getColor(getActivity(), R.color.Orange));

            dataSet.setColors(colors);

            PieData data = new PieData(dataSet);
            data.setValueFormatter(new PercentFormatter());
            data.setValueTextSize(11f);
            data.setValueTextColor(Color.WHITE);
            data.setValueTextSize(0f);
            holder.mChart.setData(data);

        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return dashboardList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView factorName;
            TextView target, targetTitle;
            TextView acheived, acheivedTitle;
            TextView index;
            TextView incentive, incentiveTitle;
            TextView score, scoreTitle;
            TextView balance, balanceTitle;
            TextView tvSkuWise;
            PieChart mChart;
            DashBoardBO dashboardDataObj;
            View rowDotBlue, rowDotOrange, rowDotGreen, verticalSeparatorTarget, verticalSeparatorBalance, verticalSkuWise;

            public ViewHolder(View row) {
                super(row);
                factorName = (TextView) row
                        .findViewById(R.id.factorName_dashboard_tv);
                target = (TextView) row
                        .findViewById(R.id.target_dashboard_tv);
                acheived = (TextView) row
                        .findViewById(R.id.acheived_dashboard_tv);
                balance = (TextView) row
                        .findViewById(R.id.balance_dashboard_tv);
                index = (TextView) row
                        .findViewById(R.id.index_dashboard_tv);
                score = (TextView) row
                        .findViewById(R.id.score_dashboard_tv);
                incentive = (TextView) row
                        .findViewById(R.id.initiative_dashboard_tv);

                mChart = (PieChart) row
                        .findViewById(R.id.pieChart);

                targetTitle = (TextView) row
                        .findViewById(R.id.target_title);
                acheivedTitle = (TextView) row
                        .findViewById(R.id.achived_title);
                incentiveTitle = (TextView) row
                        .findViewById(R.id.incentive_title);

                balanceTitle = (TextView) row
                        .findViewById(R.id.balance_title);
                scoreTitle = (TextView) row
                        .findViewById(R.id.score_title);

                rowDotBlue = (View) row
                        .findViewById(R.id.row_dot_blue);
                rowDotOrange = (View) row
                        .findViewById(R.id.row_dot_orange);
                rowDotGreen = (View) row
                        .findViewById(R.id.row_dot_green);
                verticalSeparatorTarget = (View) row
                        .findViewById(R.id.verticalSeparatorTarget);
                verticalSeparatorBalance = (View) row
                        .findViewById(R.id.verticalSeparatorBalance);
                tvSkuWise = (TextView) row
                        .findViewById(R.id.tv_skuwise);
                verticalSkuWise = (View) row
                        .findViewById(R.id.verticalSeparatorSkuWise);

                if (!bmodel.configurationMasterHelper.SHOW_INDEX_DASH) {
                    index.setVisibility(View.GONE);
                }
                if (!bmodel.configurationMasterHelper.SHOW_TARGET_DASH) {
                    target.setVisibility(View.GONE);
                    targetTitle.setVisibility(View.GONE);
                    verticalSeparatorTarget.setVisibility(View.GONE);
                    rowDotGreen.setVisibility(View.GONE);
                } else {
                    if (type.equals(DAY_TYPE)) {
                        try {
                            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                                    R.id.target_title).getTag()) != null)
                                ((TextView) view.findViewById(R.id.target_title))
                                        .setText(bmodel.labelsMasterHelper.
                                                applyLabels(view.findViewById(R.id.target_title)
                                                        .getTag()));
                            else
                                ((TextView) view.findViewById(R.id.target_title))
                                        .setText(getResources().getString(R.string.target));
                        } catch (Exception e) {
                            Commons.printException(e + "");
                        }
                    } else if (type.equals(MONTH_TYPE)) {
                        try {
                            if (bmodel.labelsMasterHelper.applyLabels(TGT_MONTH_TAG) != null)
                                ((TextView) view.findViewById(R.id.target_title))
                                        .setText(bmodel.labelsMasterHelper
                                                .applyLabels(TGT_MONTH_TAG));
                            else
                                ((TextView) view.findViewById(R.id.target_title))
                                        .setText(getResources().getString(R.string.target));
                        } catch (Exception e) {
                            Commons.printException(e + "");
                        }
                    } else if (type.equals(YEAR_TYPE)) {
                        try {
                            if (bmodel.labelsMasterHelper.applyLabels(TGT_YEAR_TAG) != null)
                                ((TextView) view.findViewById(R.id.target_title))
                                        .setText(bmodel.labelsMasterHelper
                                                .applyLabels(TGT_YEAR_TAG));
                            else
                                ((TextView) view.findViewById(R.id.target_title))
                                        .setText(getResources().getString(R.string.target));
                        } catch (Exception e) {
                            Commons.printException(e + "");
                        }
                    } else {
                        try {
                            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                                    R.id.target_title).getTag()) != null)
                                ((TextView) view.findViewById(R.id.target_title))
                                        .setText(bmodel.labelsMasterHelper.
                                                applyLabels(view.findViewById(R.id.target_title)
                                                        .getTag()));
                            else
                                ((TextView) view.findViewById(R.id.target_title))
                                        .setText(getResources().getString(R.string.target));
                        } catch (Exception e) {
                            Commons.printException(e + "");
                        }
                    }

                }
                if (!bmodel.configurationMasterHelper.SHOW_ACHIEVED_DASH) {
                    acheived.setVisibility(View.GONE);
                    acheivedTitle.setVisibility(View.GONE);
                    rowDotBlue.setVisibility(View.GONE);
                } else {
                    if (type.equals(DAY_TYPE)) {
                        try {
                            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                                    R.id.achived_title).getTag()) != null)
                                ((TextView) view.findViewById(R.id.achived_title))
                                        .setText(bmodel.labelsMasterHelper.
                                                applyLabels(view.findViewById(R.id.achived_title)
                                                        .getTag()));
                            else
                                ((TextView) view.findViewById(R.id.achived_title))
                                        .setText(getResources().getString(R.string.achieved));
                        } catch (Exception e) {
                            Commons.printException(e + "");
                        }
                    } else if (type.equals(MONTH_TYPE)) {
                        try {
                            if (bmodel.labelsMasterHelper.applyLabels(ACH_MONTH_TAG) != null)
                                ((TextView) view.findViewById(R.id.achived_title))
                                        .setText(bmodel.labelsMasterHelper
                                                .applyLabels(ACH_MONTH_TAG));
                            else
                                ((TextView) view.findViewById(R.id.achived_title))
                                        .setText(getResources().getString(R.string.achieved));
                        } catch (Exception e) {
                            Commons.printException(e + "");
                        }
                    } else if (type.equals(YEAR_TYPE)) {
                        try {
                            if (bmodel.labelsMasterHelper.applyLabels(ACH_YEAR_TAG) != null)
                                ((TextView) view.findViewById(R.id.achived_title))
                                        .setText(bmodel.labelsMasterHelper
                                                .applyLabels(ACH_YEAR_TAG));
                            else
                                ((TextView) view.findViewById(R.id.achived_title))
                                        .setText(getResources().getString(R.string.achieved));
                        } catch (Exception e) {
                            Commons.printException(e + "");
                        }
                    } else {
                        try {
                            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                                    R.id.achived_title).getTag()) != null)
                                ((TextView) view.findViewById(R.id.achived_title))
                                        .setText(bmodel.labelsMasterHelper.
                                                applyLabels(view.findViewById(R.id.achived_title)
                                                        .getTag()));
                            else
                                ((TextView) view.findViewById(R.id.achived_title))
                                        .setText(getResources().getString(R.string.achieved));
                        } catch (Exception e) {
                            Commons.printException(e + "");
                        }
                    }

                }
                if (!bmodel.configurationMasterHelper.SHOW_BALANCE_DASH) {
                    balance.setVisibility(View.GONE);
                    balanceTitle.setVisibility(View.GONE);
                    verticalSeparatorBalance.setVisibility(View.GONE);
                    rowDotOrange.setVisibility(View.GONE);
                } else {
                    try {
                        if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                                R.id.balancetv).getTag()) != null)
                            ((TextView) view.findViewById(R.id.balancetv))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(view.findViewById(R.id.balancetv)
                                                    .getTag()));
                    } catch (Exception e) {
                        Commons.printException(e + "");
                    }

                }
                if (!bmodel.configurationMasterHelper.SHOW_INCENTIVE_DASH) {
                    incentive.setVisibility(View.GONE);
                    incentiveTitle.setVisibility(View.GONE);
                } else {
                    if (type.equals(DAY_TYPE)) {
                        try {
                            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                                    R.id.incentive_title).getTag()) != null)
                                ((TextView) view.findViewById(R.id.incentive_title))
                                        .setText(bmodel.labelsMasterHelper
                                                .applyLabels(view.findViewById(R.id.incentive_title)
                                                        .getTag()));
                            else
                                ((TextView) view.findViewById(R.id.incentive_title))
                                        .setText(getResources().getString(R.string.incentive));
                        } catch (Exception e) {
                            Commons.printException(e + "");
                        }
                    } else if (type.equals(MONTH_TYPE)) {
                        try {
                            if (bmodel.labelsMasterHelper.applyLabels(MONTH_TAG) != null)
                                ((TextView) view.findViewById(R.id.incentive_title))
                                        .setText(bmodel.labelsMasterHelper
                                                .applyLabels(MONTH_TAG));
                            else
                                ((TextView) view.findViewById(R.id.incentive_title))
                                        .setText(getResources().getString(R.string.incentive));
                        } catch (Exception e) {
                            Commons.printException(e + "");
                        }
                    } else if (type.equals(YEAR_TYPE)) {
                        try {
                            if (bmodel.labelsMasterHelper.applyLabels(YEAR_TAG) != null)
                                ((TextView) view.findViewById(R.id.incentive_title))
                                        .setText(bmodel.labelsMasterHelper
                                                .applyLabels(YEAR_TAG));
                            else
                                ((TextView) view.findViewById(R.id.incentive_title))
                                        .setText(getResources().getString(R.string.incentive));
                        } catch (Exception e) {
                            Commons.printException(e + "");
                        }
                    } else {
                        try {
                            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                                    R.id.incentive_title).getTag()) != null)
                                ((TextView) view.findViewById(R.id.incentive_title))
                                        .setText(bmodel.labelsMasterHelper
                                                .applyLabels(view.findViewById(R.id.incentive_title)
                                                        .getTag()));
                            else
                                ((TextView) view.findViewById(R.id.incentive_title))
                                        .setText(getResources().getString(R.string.incentive));
                        } catch (Exception e) {
                            Commons.printException(e + "");
                        }
                    }


                }

                //common row layout used - old dashboard score not available
                score.setVisibility(View.GONE);
                scoreTitle.setVisibility(View.GONE);

                mChart.setUsePercentValues(true);
                mChart.getDescription().setEnabled(false);
                mChart.setExtraOffsets(0, 0, 0, 0);

                mChart.setDragDecelerationFrictionCoef(0.95f);

                mChart.setDrawHoleEnabled(false);

                mChart.setTransparentCircleColor(Color.WHITE);
                mChart.setTransparentCircleAlpha(110);

                mChart.setDrawCenterText(false);

                // enable rotation of the chart by touch
                mChart.setRotationEnabled(false);
                mChart.setHighlightPerTapEnabled(true);

                mChart.animateXY(1400, 1400, Easing.EasingOption.EaseInOutQuad, Easing.EasingOption.EaseInOutQuad);


                Legend l = mChart.getLegend();
                l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
                l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
                l.setOrientation(Legend.LegendOrientation.VERTICAL);
                l.setDrawInside(false);
                l.setEnabled(false);

            }
        }
    }

}
