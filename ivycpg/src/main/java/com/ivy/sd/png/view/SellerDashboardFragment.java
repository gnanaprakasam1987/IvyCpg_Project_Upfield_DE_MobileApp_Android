package com.ivy.sd.png.view;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.ivy.cpg.primarysale.bo.DistributorMasterBO;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.DailyReportBO;
import com.ivy.sd.png.bo.DashBoardBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.KeyPairBoolData;
import com.ivy.sd.png.commons.MultiSpinner;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.commons.SpinnerListener;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.List;

import me.relex.circleindicator.CircleIndicator;

public class SellerDashboardFragment extends IvyBaseFragment implements AdapterView.OnItemSelectedListener {

    private BusinessModel bmodel;
    private final int beatPosition = 0;
    private RecyclerView dashBoardList;
    private FragmentManager fm;
    public DashBoardListViewAdapter dashBoardListViewAdapter;
    private int mSelectedUserId = 0;
    private boolean show_trend_chart = false;
    private CollapsingToolbarLayout collapsing;

    private String selectedInterval = MONTH;

    private AlertDialog alertDialog;
    private Boolean showinitiavite = true;

    /*new spinner configuration*/
    private Spinner dashSpinner;
    private Spinner userSpinner;
    private Spinner monthSpinner;

    private static final String MONTH = "MONTH";
    private static final String DAY = "DAY";
    private static final String P3M = "P3M";
    private static final String CODE1 = "VAL";
    private static final String CODE2 = "VIP";
    private static final String CODE3 = "TLS";
    private static final String CODE4 = "PDC";
    private static final String CODE5 = "MSP";
    private static final String CODE6 = "COV";
    private static final String CODE7 = "PRM";
    private static final String CODE8 = "MSL";
    private static final String CODE9 = "TRN";
    private static final String CODE10 = "AUB";
    private static final String CODE11 = "ASP";
    private static final String CODE12 = "ABV";
    private static final String CODE13 = "INV";

    private int NUM_ITEMS = 1;
    private double incentive = 0.0;
    private int chartpositionSMP = 0;
    private ArrayList<Fragment> fragmentList = new ArrayList<>();
    /**************************/
    private ArrayAdapter<UserMasterBO> userMasterBOArrayAdapter;
    private ArrayAdapter<DistributorMasterBO> distributorMasterBOArrayAdapter;
    private ArrayList<DashBoardBO> mDashboardList;
    private View view;
    ViewPager vpPager;
    CircleIndicator indicator;

    MyPagerAdapter adapterViewPager;
    private LinearLayout ll_users;
    private LinearLayout ll_distributor;
    private MultiSpinner distrSpinner1;
    private MultiSpinner userSpinner1;
    private String mSelectedDistributorId = "";
    private String mFilterUser = "";
    private TextView tvDistributorName;
    private TextView tvUserName;
    int transactionPerDay;
    int avgUnitsPerBill;
    int avgSellingPrice;
    int avgBillValue;
    boolean isFromHomeScreenTwo = false;
    //boolean isSemiCircleChartRequired = false;
    Bundle bundle;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_seller_dashboard, container, false);


        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        fm = getActivity().getSupportFragmentManager();
//        if (getActivity().getIntent().getExtras() != null) {
//            if (getActivity().getIntent().getBooleanExtra("isFromHomeScreenTwo", false)) {
//                isFromHomeScreenTwo = true;
//            }
//        }
        bundle = getActivity().getIntent().getExtras();
        if (bundle != null) {
            isFromHomeScreenTwo = bundle.getBoolean("isFromHomeScreenTwo", false);
        }

        if (getActionBar() != null)
            setUpActionBar();

        setHasOptionsMenu(true);
        init();
        return view;
    }

    private ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    private void init() {
        vpPager = (ViewPager) view.findViewById(R.id.viewpager);
        collapsing = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing);
        indicator = (CircleIndicator) view.findViewById(R.id.indicator);

        dashBoardList = (RecyclerView) view.findViewById(R.id.dashboardLv);
        ((TextView) view.findViewById(R.id.textView)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));

        if (bmodel.dashBoardHelper.getShowDayAndP3MSpinner() != 0) {
            setpUpSpinner();
            bmodel.downloadDailyReport();
        }

        if (bmodel.configurationMasterHelper.IS_SMP_BASED_DASH) {
            show_trend_chart = true;
        }

        if (!show_trend_chart) {
            vpPager.setVisibility(View.GONE);
            collapsing.setVisibility(View.GONE);

        }
        tvDistributorName = (TextView) view.findViewById(R.id.tv_distributor_title);
        tvUserName = (TextView) view.findViewById(R.id.tv_username_title);

        tvDistributorName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        tvUserName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));

        LinearLayout spinner_layout1 = (LinearLayout) view.findViewById(R.id.spinner_layout1);
        LinearLayout spinner_layout2 = (LinearLayout) view.findViewById(R.id.spinner_layout2);
        userSpinner = (Spinner) view.findViewById(R.id.userSpinner);

        if (!isFromHomeScreenTwo) {
            if (bmodel.configurationMasterHelper.IS_USER_BASED_DASH && bmodel.configurationMasterHelper.IS_DISTRIBUTOR_BASED_DASH) {
                if (bmodel.configurationMasterHelper.IS_NIVEA_BASED_DASH) {
                    spinner_layout1.setVisibility(View.GONE);
                    spinner_layout2.setVisibility(View.VISIBLE);
                    ll_distributor = (LinearLayout) view.findViewById(R.id.ll_distributor);
                    distrSpinner1 = (MultiSpinner) view.findViewById(R.id.distributorSpinner1);
                    ll_distributor.setVisibility(View.VISIBLE);

                    ll_users = (LinearLayout) view.findViewById(R.id.ll_users);
                    userSpinner1 = (MultiSpinner) view.findViewById(R.id.userSpinner1);
                    ll_users.setVisibility(View.VISIBLE);

                    final List<KeyPairBoolData> distArray = new ArrayList<>();
                    ArrayList<DistributorMasterBO> distributors = bmodel.distributorMasterHelper.getDistributors();
                    distArray.add(new KeyPairBoolData(0, getResources().getString(R.string.all), true));
                    int count = 0;

                    for (int j = 0; j < distributors.size(); j++) {
                        KeyPairBoolData h = new KeyPairBoolData();
                        h.setId(Integer.parseInt(distributors.get(j).getDId()));
                        h.setName(distributors.get(j).getDName());
                        h.setSelected(true);
                        distArray.add(h);
                        count++;
                        mSelectedDistributorId += bmodel.QT(distributors.get(j).getDId() + "");
                        if (count != distributors.size())
                            mSelectedDistributorId += ",";
                    }

                    distrSpinner1.setItems(distArray, -1, new SpinnerListener() {
                        @Override
                        public void onItemsSelected(List<KeyPairBoolData> items) {
                            Commons.print("Multi" + items.size());
                            int count = 0;
                            mSelectedDistributorId = "";
                            if (items.size() > 0) {
                                for (int i = 0; i < items.size(); i++) {
                                    count++;
                                    mSelectedDistributorId += bmodel.QT(items.get(i).getId() + "");
                                    if (count != items.size())
                                        mSelectedDistributorId += ",";
                                }
                            } else
                                mSelectedDistributorId = "0";
                            loadUserSpinner(mSelectedDistributorId);
                        }
                    });
                    loadUserSpinner("0");
                    if (!show_trend_chart) {
                        vpPager.setVisibility(View.GONE);
                        collapsing.setVisibility(View.GONE);
                    }
                } else {
                    spinner_layout1.setVisibility(View.VISIBLE);
                    spinner_layout2.setVisibility(View.GONE);
                    Spinner distrSpinner = (Spinner) view.findViewById(R.id.distributorSpinner);
                    distrSpinner.setVisibility(View.VISIBLE);

                    userSpinner = (Spinner) view.findViewById(R.id.userSpinner);
                    userSpinner.setVisibility(View.VISIBLE);

                    distrSpinner.setOnItemSelectedListener(this);
                    userSpinner.setOnItemSelectedListener(this);

                    //To load all distributor
                    distributorMasterBOArrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.dashboard_spinner_layout);
                    distributorMasterBOArrayAdapter.add(new DistributorMasterBO("0", getResources().getString(R.string.select)));
                    if (bmodel.distributorMasterHelper.getDistributors() != null &&
                            bmodel.distributorMasterHelper.getDistributors().size() != 0) {
                        for (DistributorMasterBO bo : bmodel.distributorMasterHelper.getDistributors()) {
                            distributorMasterBOArrayAdapter.add(bo);
                        }
                        distributorMasterBOArrayAdapter.setDropDownViewResource(R.layout.dashboard_spinner_list);
                        distrSpinner.setAdapter(distributorMasterBOArrayAdapter);
                    }

                    //User Spinner only with select to have distributor to get selected first
                    userMasterBOArrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.dashboard_spinner_layout);
                    userMasterBOArrayAdapter.add(new UserMasterBO(0, getResources().getString(R.string.all)));
                    userMasterBOArrayAdapter.setDropDownViewResource(R.layout.dashboard_spinner_list);
                    userSpinner.setAdapter(userMasterBOArrayAdapter);

                    if (!isFromHomeScreenTwo) {
                        mSelectedUserId = bmodel.userMasterHelper.getUserMasterBO().getUserid();
                        bmodel.dashBoardHelper.loadSellerDashBoard(Integer.toString(mSelectedUserId), MONTH);
                    }
                }
            } else if (bmodel.configurationMasterHelper.IS_USER_BASED_DASH) {
                spinner_layout1.setVisibility(View.VISIBLE);
                spinner_layout2.setVisibility(View.GONE);

                userSpinner.setVisibility(View.VISIBLE);
                userSpinner.setOnItemSelectedListener(this);
                userMasterBOArrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.dashboard_spinner_layout);
                userMasterBOArrayAdapter.add(bmodel.userMasterHelper.getUserMasterBO());
                for (UserMasterBO bo : bmodel.userMasterHelper.downloadUserList()) {
                    userMasterBOArrayAdapter.add(bo);
                }
                userMasterBOArrayAdapter.setDropDownViewResource(R.layout.dashboard_spinner_list);
                userSpinner.setAdapter(userMasterBOArrayAdapter);
                if (!isFromHomeScreenTwo) {
                    mSelectedUserId = bmodel.userMasterHelper.getUserMasterBO().getUserid();
                    bmodel.dashBoardHelper.loadSellerDashBoard(Integer.toString(mSelectedUserId), MONTH);
                }
            }
        } else {
            spinner_layout1.setVisibility(View.VISIBLE);
            view.findViewById(R.id.distributorSpinner).setVisibility(View.GONE);
            userSpinner.setVisibility(View.GONE);
        }

        if (!isFromHomeScreenTwo) {
            mSelectedUserId = bmodel.userMasterHelper.getUserMasterBO().getUserid();
            bmodel.dashBoardHelper.loadSellerDashBoard(Integer.toString(mSelectedUserId), MONTH);
        } else {
            vpPager.setVisibility(View.GONE);
            collapsing.setVisibility(View.GONE);
        }

        dashBoardList.setHasFixedSize(false);
        dashBoardList.setNestedScrollingEnabled(false);
        dashBoardList.setLayoutManager(new LinearLayoutManager(getActivity()));
        bmodel.dashBoardHelper.setDashboardBO(new DashBoardBO());
        gridListDataLoad(beatPosition);
        updateAll();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public class DashBoardListViewAdapter extends RecyclerView.Adapter<DashBoardListViewAdapter.ViewHolder> {
        private final List<DashBoardBO> dashboardList;

        public DashBoardListViewAdapter(List<DashBoardBO> dashboardList) {
            this.dashboardList = dashboardList;
        }

        @Override
        public SellerDashboardFragment.DashBoardListViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.seller_dashboard_row_layout, parent, false);
            return new SellerDashboardFragment.DashBoardListViewAdapter.ViewHolder(v);
        }


        @Override
        public void onBindViewHolder(final SellerDashboardFragment.DashBoardListViewAdapter.ViewHolder holder, int position) {
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
            holder.index.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
            if (holder.mChart.getVisibility() != View.VISIBLE) {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.index.getLayoutParams();
                params.setMargins(0, 0, 10, 0); //substitute parameters for left, top, right, bottom
                holder.index.setLayoutParams(params);
            }
            holder.incentive.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
            holder.score.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
            holder.incentiveTitle.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
            holder.kpiFlex1.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.scoreTitle.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
            holder.targetTitle.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.acheivedTitle.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.balanceTitle.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.tvSkuWise.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.flex1Title.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));


            holder.factorName.setText(dashboardData.getText());
            //for P3M trend Chart loading
            holder.factorName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    if (selectedInterval.equals(P3M)) {
                    if (show_trend_chart) {
                        if (mDashboardList != null && mDashboardList.size() > 0) {
                            bmodel.dashBoardHelper.setDashboardBO(holder.dashboardDataObj);
                        }

                        bmodel.dashBoardHelper.mParamLovId = dashboardData.getKpiTypeLovID();
                        adapterViewPager = new MyPagerAdapter(getActivity().getSupportFragmentManager(), fragmentList);
                        new setAdapterTask().execute();
                        vpPager.setCurrentItem(chartpositionSMP);
                    }
//                    }

                }
            });

            if (dashboardData.getSubDataCount() > 0) {
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
                        if (bmodel.configurationMasterHelper.SHOW_NOR_DASHBOARD)
                            bmodel.dashBoardHelper.findMinMaxProductLevelSellerKPI(holder.dashboardDataObj.getKpiID(), holder.dashboardDataObj.getKpiTypeLovID(), selectedInterval);
                            //for loaeral
                        else
                            bmodel.dashBoardHelper.downloadLorealSkuDetails(holder.dashboardDataObj.getKpiID(), holder.dashboardDataObj.getKpiTypeLovID(), selectedInterval);

                        if (bmodel.dashBoardHelper.getSellerKpiSku().size() > 0) {
                            Intent i = new Intent(getActivity(),
                                    SellerKPISKUActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            i.putExtra("screentitle",
                                    bmodel.getMenuName("MENU_SKUWISESTGT"));
                            i.putExtra("screentitlebk",
                                    ((AppCompatActivity) getActivity()).getSupportActionBar().getTitle());
                            i.putExtra("from", "4");
                            i.putExtra("flex1", holder.dashboardDataObj.getFlex1());
                            i.putExtra("pid",
                                    holder.dashboardDataObj.getPId());
                            i.putExtra("isFromDash", true);
                            startActivity(i);
                        } else {
                            bmodel.showAlert(
                                    getResources().getString(R.string.no_products_exists), 0);
                        }
                    } catch (Exception e) {
                        Commons.printException(e + "");
                    }
                }
            });

            if (dashboardData.getFlex1() == 1) {
                holder.acheived.setText(bmodel.dashBoardHelper.getWhole(dashboardData.getKpiAcheived()));
                holder.target.setText(bmodel.dashBoardHelper.getWhole(dashboardData.getKpiTarget()));
                holder.balance.setText(bmodel.dashBoardHelper.getWhole(bmodel.formatValue(SDUtil.convertToInt(dashboardData.getKpiTarget()) - SDUtil.convertToInt(dashboardData.getKpiAcheived()))));
                String strCalcPercentage = dashboardData.getCalculatedPercentage() + "%";
                float temp_ach = Float.parseFloat(dashboardData.getKpiAcheived()) - Float.parseFloat(dashboardData.getKpiTarget());
                if (temp_ach > 0) {
                    int bonus = Math.round(Float.parseFloat(dashboardData.getKpiAcheived()) /
                            (Float.parseFloat(dashboardData.getKpiTarget())) * 100);
                    holder.index.setText(SDUtil.roundIt(bonus, 1) + "%");
                } else {
                    holder.index.setText(strCalcPercentage);
                }
                holder.kpiFlex1.setText(bmodel.dashBoardHelper.getWhole(dashboardData.getKpiFlex()));
                holder.incentive.setText(bmodel.dashBoardHelper.getWhole(dashboardData.getKpiIncentive()));
                holder.score.setText(bmodel.dashBoardHelper.getWhole(dashboardData.getKpiScore()));
            } else {
                try {
                    String strKpiAchieved = bmodel.formatValue(SDUtil.convertToDouble(dashboardData.getKpiAcheived())) + "";
                    holder.acheived.setText(strKpiAchieved);
                    String strKpiTarget = bmodel.formatValue(SDUtil.convertToDouble(dashboardData.getKpiTarget())) + "";
                    holder.target.setText(strKpiTarget);
                } catch (Exception e) {
                    Commons.printException(e + "");
                }
                String strCalcPercentage = dashboardData.getCalculatedPercentage() + "%";
                float temp_ach = Float.parseFloat(dashboardData.getKpiAcheived()) - Float.parseFloat(dashboardData.getKpiTarget());
                if (temp_ach > 0) {
                    int bonus = Math.round(Float.parseFloat(dashboardData.getKpiAcheived()) /
                            (Float.parseFloat(dashboardData.getKpiTarget())) * 100);
                    holder.index.setText(SDUtil.roundIt(bonus, 1) + "%");
                } else {
                    holder.index.setText(strCalcPercentage);
                }
                holder.balance.setText(bmodel.formatValue((SDUtil.convertToInt(dashboardData.getKpiTarget()) - SDUtil.convertToInt(dashboardData.getKpiAcheived()))));
                holder.kpiFlex1.setText(dashboardData.getKpiFlex());
                holder.incentive.setText(bmodel.formatValue(SDUtil.convertToDouble(dashboardData.getKpiIncentive() + "")));
                String strKpiScore = dashboardData.getKpiScore() + "";
                holder.score.setText(strKpiScore);
                //isSemiCircleChartRequired = true;
            }

            if (!bmodel.configurationMasterHelper.IS_SMP_BASED_DASH) {
                holder.mChart.setUsePercentValues(true);
                holder.mChart.getDescription().setEnabled(false);
                holder.mChart.setExtraOffsets(0, 0, 0, 0);

                holder.mChart.setDragDecelerationFrictionCoef(0.95f);

                holder.mChart.setDrawHoleEnabled(true);

                holder.mChart.setTransparentCircleColor(Color.TRANSPARENT);
                holder.mChart.setTransparentCircleAlpha(110);

                holder.mChart.setDrawCenterText(false);

                // enable rotation of the chart by touch
                holder.mChart.setRotationEnabled(false);
                holder.mChart.setHighlightPerTapEnabled(true);

                holder.mChart.animateXY(1400, 1400, Easing.EasingOption.EaseInOutQuad, Easing.EasingOption.EaseInOutQuad);


                Legend l = holder.mChart.getLegend();
                l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
                l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
                l.setOrientation(Legend.LegendOrientation.VERTICAL);
                l.setDrawInside(false);
                l.setEnabled(false);

                //if (isSemiCircleChartRequired) {
                setOffset(holder.mChart);
                holder.mChart.setHoleColor(Color.TRANSPARENT);
                holder.mChart.setHoleRadius(50f);
                holder.mChart.setTransparentCircleRadius(28f);
                holder.mChart.setMaxAngle(180f); // HALF CHART
                holder.mChart.setRotationAngle(180f);
                // entry label styling
                holder.mChart.setEntryLabelColor(Color.TRANSPARENT);
                holder.mChart.setEntryLabelTextSize(0f);
                //}

                ArrayList<PieEntry> entries = new ArrayList<PieEntry>();

                entries.add(new PieEntry(Float.parseFloat(dashboardData.getKpiAcheived())));
                entries.add(new PieEntry(Float.parseFloat(dashboardData.getKpiTarget())));

                PieDataSet dataSet = new PieDataSet(entries, "");

                dataSet.setSliceSpace(0f);
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
            TextView kpiFlex1, flex1Title;
            TextView tvSkuWise;
            PieChart mChart;
            DashBoardBO dashboardDataObj;
            View rowDotBlue, rowDotOrange, rowDotOrange1, rowDotGreen, verticalSeparatorTarget, verticalSeparatorBalance, verticalSeparatorFlex1, verticalSkuWise;

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
                kpiFlex1 = (TextView) row
                        .findViewById(R.id.flex_dashboard_tv);
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
                flex1Title = (TextView) row
                        .findViewById(R.id.flex_title);

                rowDotBlue = (View) row
                        .findViewById(R.id.row_dot_blue);
                rowDotOrange = (View) row
                        .findViewById(R.id.row_dot_orange);
                rowDotOrange1 = (View) row
                        .findViewById(R.id.row_dot_orange1);
                rowDotGreen = (View) row
                        .findViewById(R.id.row_dot_green);
                verticalSeparatorTarget = (View) row
                        .findViewById(R.id.verticalSeparatorTarget);
                verticalSeparatorBalance = (View) row
                        .findViewById(R.id.verticalSeparatorBalance);
                verticalSeparatorFlex1 = (View) row
                        .findViewById(R.id.verticalSeparatorFlex);
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
                    try {
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.target_title).getTag()) != null)
                            ((TextView) row.findViewById(R.id.target_title))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(R.id.target_title)
                                                    .getTag()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (!bmodel.configurationMasterHelper.SHOW_ACHIEVED_DASH) {
                    acheived.setVisibility(View.GONE);
                    acheivedTitle.setVisibility(View.GONE);
                    rowDotBlue.setVisibility(View.GONE);
                } else {
                    try {
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.achived_title).getTag()) != null)
                            ((TextView) row.findViewById(R.id.achived_title))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(R.id.achived_title)
                                                    .getTag()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (!bmodel.configurationMasterHelper.SHOW_FLEX_DASH) {
                    kpiFlex1.setVisibility(View.GONE);
                    flex1Title.setVisibility(View.GONE);
                    verticalSeparatorFlex1.setVisibility(View.GONE);
                    rowDotOrange1.setVisibility(View.GONE);
                } else {

                    try {
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.flex_title).getTag()) != null)
                            ((TextView) row.findViewById(R.id.flex_title))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(R.id.flex_title)
                                                    .getTag()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (!bmodel.configurationMasterHelper.SHOW_BALANCE_DASH) {
                    balance.setVisibility(View.GONE);
                    balanceTitle.setVisibility(View.GONE);
                    verticalSeparatorBalance.setVisibility(View.GONE);
                    rowDotOrange.setVisibility(View.GONE);
                } else {

                    try {
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.balance_title).getTag()) != null)
                            ((TextView) row.findViewById(R.id.balance_title))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(R.id.balance_title)
                                                    .getTag()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (!bmodel.configurationMasterHelper.SHOW_INCENTIVE_DASH) {
                    incentive.setVisibility(View.GONE);
                    incentiveTitle.setVisibility(View.GONE);
                } else {

                    try {
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.incentive_title).getTag()) != null)
                            ((TextView) row.findViewById(R.id.incentive_title))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(R.id.incentive_title)
                                                    .getTag()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (!bmodel.configurationMasterHelper.SHOW_SCORE_DASH) {
                    score.setVisibility(View.GONE);
                    scoreTitle.setVisibility(View.GONE);
                } else {

                    try {
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.score_title).getTag()) != null)
                            ((TextView) row.findViewById(R.id.score_title))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(R.id.score_title)
                                                    .getTag()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (bmodel.configurationMasterHelper.SHOW_SCORE_DASH && bmodel.configurationMasterHelper.SHOW_INCENTIVE_DASH) {
                    score.setVisibility(View.GONE);
                    scoreTitle.setVisibility(View.GONE);
                }
                if (bmodel.configurationMasterHelper.IS_SMP_BASED_DASH) {
                    mChart.setVisibility(View.GONE);
                }
            }
        }
    }

    public void setOffset(PieChart mChart) {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;

        int offset = (int) (height * 0.20); /* percent to move */

        RelativeLayout.LayoutParams rlParams =
                (RelativeLayout.LayoutParams) mChart.getLayoutParams();
        rlParams.setMargins(0, 10, 0, -offset);
        mChart.setLayoutParams(rlParams);
    }

    private void setUpActionBar() {
        getActionBar().setDisplayShowTitleEnabled(false);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActionBar().setElevation(0);
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
                setScreenTitle(bmodel.getMenuName("MENU_RTR_KPI"));
            } else
                setScreenTitle(getActivity().getIntent().getStringExtra("screentitle"));
        }
        if (!BusinessModel.dashHomeStatic) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_target_plan, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_skutgt).setVisible(false);
        if (BusinessModel.dashHomeStatic)
            menu.findItem(R.id.menu_next).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            if (isFromHomeScreenTwo)//update time stamp if previous screen is homescreentwo
                bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                        .now(SDUtil.TIME));
            getActivity().finish();
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


            if (dashBoardList != null)
                dashBoardList = null;
            if (fm != null)
                fm = null;
        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }

    private void gridListDataLoad(int position) {
        if (position == 0) {
            bmodel.dashBoardHelper.getGridData(0);
            if (bmodel.configurationMasterHelper.SHOW_INCENTIVE_DASH && !bmodel.configurationMasterHelper.SHOW_SCORE_DASH) {
                // incentivetv.setVisibility(View.VISIBLE);
                showinitiavite = true;
            }
        } else {
            bmodel.dashBoardHelper.getGridData((String) bmodel.dashBoardHelper
                    .getBeatList().get(position));
            showinitiavite = false;
        }
    }


    private void setpUpSpinner() {
        dashSpinner = (Spinner) view.findViewById(R.id.dashSpinner);
        dashSpinner.setVisibility(View.VISIBLE);
        dashSpinner.setOnItemSelectedListener(this);
        final int dayAndP3MSpinner = bmodel.dashBoardHelper.getShowDayAndP3MSpinner();

        List<String> categories = new ArrayList<>();
        categories.add(MONTH);
        if (dayAndP3MSpinner == 3) {
            categories.add(DAY);
            categories.add(P3M);
            show_trend_chart = true;
        } else if (dayAndP3MSpinner == 2) {
            categories.add(DAY);
        } else if (dayAndP3MSpinner == 1) {
            categories.add(P3M);
            show_trend_chart = true;
        }

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(), R.layout.dashboard_spinner_layout, categories);

        dataAdapter.setDropDownViewResource(R.layout.dashboard_spinner_list);

        // attaching data adapter to spinner
        dashSpinner.setAdapter(dataAdapter);
        monthSpinner = (Spinner) view.findViewById(R.id.monthSpinner);


    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        try {
            int menuid = parent.getId();
            if (menuid == R.id.dashSpinner) {
                selectedInterval = dashSpinner.getSelectedItem().toString();
                if (!isFromHomeScreenTwo) {
                    if (selectedInterval.equals(P3M))
                        bmodel.dashBoardHelper.loadSellerDashBoard(Integer.toString(mSelectedUserId));
                    else
                        bmodel.dashBoardHelper.loadSellerDashBoard(Integer.toString(mSelectedUserId), selectedInterval);
                } else {
                    bmodel.dashBoardHelper.loadRetailerDashBoard(bmodel.getRetailerMasterBO().getRetailerID() + "", selectedInterval);
                }
                gridListDataLoad(beatPosition);
                updateAll();
                monthSpinner.setVisibility(View.GONE);

                if (selectedInterval.equals(DAY) && mSelectedUserId == bmodel.userMasterHelper.getUserMasterBO().getUserid()) {
                    DailyReportBO outlet = bmodel.getDailyRep();
                    for (DashBoardBO dashBoardBO : bmodel.dashBoardHelper.getDashListViewList()) {
                        if (dashBoardBO.getCode().equalsIgnoreCase(CODE9) | dashBoardBO.getCode().equalsIgnoreCase(CODE10) || dashBoardBO.getCode().equalsIgnoreCase(CODE11) ||
                                dashBoardBO.getCode().equalsIgnoreCase(CODE12)) {
                            getCounterSalesDetail();
                        }
                    }
                    for (DashBoardBO dashBoardBO : bmodel.dashBoardHelper.getDashListViewList()) {
                        if (dashBoardBO.getCode().equalsIgnoreCase(CODE1)) {
                            dashBoardBO.setKpiAcheived(outlet.getTotValues());

                            int kpiAcheived = (int) SDUtil.convertToDouble(outlet.getTotValues());
                            int kpiTarget;

                            try {
                                kpiTarget = (int) Double.parseDouble(dashBoardBO.getKpiTarget());
                            } catch (Exception e) {
                                kpiTarget = 0;
                                Commons.printException(e + "");
                            }

                            if (kpiTarget == 0) {
                                dashBoardBO.setCalculatedPercentage(0);
                            } else {
                                dashBoardBO.setCalculatedPercentage((kpiAcheived * 100) / kpiTarget);
                            }
                            if (dashBoardBO.getCalculatedPercentage() >= 100) {
                                dashBoardBO.setConvTargetPercentage(0);
                                dashBoardBO.setConvAcheivedPercentage(100);
                            } else {
                                dashBoardBO.setConvTargetPercentage(100 - dashBoardBO
                                        .getCalculatedPercentage());
                                dashBoardBO.setConvAcheivedPercentage(dashBoardBO
                                        .getCalculatedPercentage());
                            }
                        } else if (dashBoardBO.getCode().equalsIgnoreCase(CODE2)) {
                            dashBoardBO.setKpiAcheived(outlet.getEffCoverage());
                            int kpiAcheived = (int) SDUtil.convertToDouble(outlet.getEffCoverage());
                            int kpiTarget;

                            try {
                                kpiTarget = (int) Double.parseDouble(dashBoardBO.getKpiTarget());
                            } catch (Exception e) {
                                kpiTarget = 0;
                                Commons.printException(e + "");
                            }

                            if (kpiTarget == 0) {
                                dashBoardBO.setCalculatedPercentage(0);
                            } else {
                                dashBoardBO.setCalculatedPercentage((kpiAcheived * 100) / kpiTarget);
                            }
                            if (dashBoardBO.getCalculatedPercentage() >= 100) {
                                dashBoardBO.setConvTargetPercentage(0);
                                dashBoardBO.setConvAcheivedPercentage(100);
                            } else {
                                dashBoardBO.setConvTargetPercentage(100 - dashBoardBO
                                        .getCalculatedPercentage());
                                dashBoardBO.setConvAcheivedPercentage(dashBoardBO
                                        .getCalculatedPercentage());
                            }

                        } else if (dashBoardBO.getCode().equalsIgnoreCase(CODE3)) {
                            dashBoardBO.setKpiAcheived(outlet.getTotLines());
                            int kpiAcheived = (int) SDUtil.convertToDouble(outlet.getTotLines());
                            int kpiTarget;

                            try {
                                kpiTarget = (int) Double.parseDouble(dashBoardBO.getKpiTarget());
                            } catch (Exception e) {
                                kpiTarget = 0;
                                Commons.printException(e + "");
                            }

                            if (kpiTarget == 0) {
                                dashBoardBO.setCalculatedPercentage(0);
                            } else {
                                dashBoardBO.setCalculatedPercentage((kpiAcheived * 100) / kpiTarget);
                            }
                            if (dashBoardBO.getCalculatedPercentage() >= 100) {
                                dashBoardBO.setConvTargetPercentage(0);
                                dashBoardBO.setConvAcheivedPercentage(100);
                            } else {
                                dashBoardBO.setConvTargetPercentage(100 - dashBoardBO
                                        .getCalculatedPercentage());
                                dashBoardBO.setConvAcheivedPercentage(dashBoardBO
                                        .getCalculatedPercentage());
                            }
                        } else if (dashBoardBO.getCode().equalsIgnoreCase(CODE4)) {
                            int productivecalls = bmodel.getProductiveCallsForTheDay();

                            dashBoardBO.setKpiAcheived(Integer.toString(productivecalls));
                            int kpiTarget;

                            try {
                                kpiTarget = (int) Double.parseDouble(dashBoardBO.getKpiTarget());
                            } catch (Exception e) {
                                kpiTarget = 0;
                                Commons.printException(e + "");
                            }

                            if (kpiTarget == 0) {
                                dashBoardBO.setCalculatedPercentage(0);
                            } else {
                                dashBoardBO.setCalculatedPercentage((productivecalls * 100) / kpiTarget);
                            }
                            if (dashBoardBO.getCalculatedPercentage() >= 100) {
                                dashBoardBO.setConvTargetPercentage(0);
                                dashBoardBO.setConvAcheivedPercentage(100);
                            } else {
                                dashBoardBO.setConvTargetPercentage(100 - dashBoardBO
                                        .getCalculatedPercentage());
                                dashBoardBO.setConvAcheivedPercentage(dashBoardBO
                                        .getCalculatedPercentage());
                            }

                        } else if (dashBoardBO.getCode().equalsIgnoreCase(CODE5)) {
                            dashBoardBO.setKpiAcheived(outlet.getMspValues());
                            int kpiAcheived = (int) SDUtil.convertToDouble(outlet.getMspValues());
                            int kpiTarget;

                            try {
                                kpiTarget = (int) Double.parseDouble(dashBoardBO.getKpiTarget());
                            } catch (Exception e) {
                                kpiTarget = 0;
                                Commons.printException(e + "");
                            }

                            if (kpiTarget == 0) {
                                dashBoardBO.setCalculatedPercentage(0);
                            } else {
                                dashBoardBO.setCalculatedPercentage((kpiAcheived * 100) / kpiTarget);
                            }

                            if (dashBoardBO.getCalculatedPercentage() >= 100) {
                                dashBoardBO.setConvTargetPercentage(0);
                                dashBoardBO.setConvAcheivedPercentage(100);
                            } else {
                                dashBoardBO.setConvTargetPercentage(100 - dashBoardBO
                                        .getCalculatedPercentage());
                                dashBoardBO.setConvAcheivedPercentage(dashBoardBO
                                        .getCalculatedPercentage());
                            }
                        } else if (dashBoardBO.getCode().equalsIgnoreCase(CODE6)) {
                            int plannedRetailerCount = getRetailerDetail("P");
                            int plannedRetailerVisitCount = getRetailerDetail("V");

                            dashBoardBO.setKpiAcheived(plannedRetailerVisitCount + "");
                            int kpiAcheived = plannedRetailerVisitCount;
                            int kpiTarget;

                            try {
                                kpiTarget = (plannedRetailerCount);
                            } catch (Exception e) {
                                kpiTarget = 0;
                                Commons.printException(e + "");
                            }

                            if (kpiTarget == 0) {
                                dashBoardBO.setCalculatedPercentage(0);
                            } else {
                                dashBoardBO.setCalculatedPercentage((kpiAcheived * 100) / kpiTarget);
                            }

                            if (dashBoardBO.getCalculatedPercentage() >= 100) {
                                dashBoardBO.setConvTargetPercentage(0);
                                dashBoardBO.setConvAcheivedPercentage(100);
                            } else {
                                dashBoardBO.setConvTargetPercentage(100 - dashBoardBO
                                        .getCalculatedPercentage());
                                dashBoardBO.setConvAcheivedPercentage(dashBoardBO
                                        .getCalculatedPercentage());
                            }


                        } else if (dashBoardBO.getCode().equalsIgnoreCase(CODE7)) {
                            int promotionCount = getPromotionDetail("P");
                            int promotionAchievedCount = getPromotionDetail("V");

                            dashBoardBO.setKpiAcheived(promotionAchievedCount + "");
                            int kpiAcheived = promotionAchievedCount;
                            int kpiTarget;

                            try {
                                kpiTarget = (promotionCount);
                            } catch (Exception e) {
                                kpiTarget = 0;
                                Commons.printException(e + "");
                            }

                            if (kpiTarget == 0) {
                                dashBoardBO.setCalculatedPercentage(0);
                            } else {
                                dashBoardBO.setCalculatedPercentage((kpiAcheived * 100) / kpiTarget);
                            }

                            if (dashBoardBO.getCalculatedPercentage() >= 100) {
                                dashBoardBO.setConvTargetPercentage(0);
                                dashBoardBO.setConvAcheivedPercentage(100);
                            } else {
                                dashBoardBO.setConvTargetPercentage(100 - dashBoardBO
                                        .getCalculatedPercentage());
                                dashBoardBO.setConvAcheivedPercentage(dashBoardBO
                                        .getCalculatedPercentage());
                            }

                        } else if (dashBoardBO.getCode().equalsIgnoreCase(CODE8)) {
                            int mslCount = getMSLDetail("P");
                            int mslAchievedCount = getMSLDetail("V");

                            dashBoardBO.setKpiAcheived(mslAchievedCount + "");
                            int kpiAcheived = mslAchievedCount;
                            int kpiTarget;

                            try {
                                kpiTarget = (mslCount);
                            } catch (Exception e) {
                                kpiTarget = 0;
                                Commons.printException(e + "");
                            }

                            if (kpiTarget == 0) {
                                dashBoardBO.setCalculatedPercentage(0);
                            } else {
                                dashBoardBO.setCalculatedPercentage((kpiAcheived * 100) / kpiTarget);
                            }

                            if (dashBoardBO.getCalculatedPercentage() >= 100) {
                                dashBoardBO.setConvTargetPercentage(0);
                                dashBoardBO.setConvAcheivedPercentage(100);
                            } else {
                                dashBoardBO.setConvTargetPercentage(100 - dashBoardBO
                                        .getCalculatedPercentage());
                                dashBoardBO.setConvAcheivedPercentage(dashBoardBO
                                        .getCalculatedPercentage());
                            }

                        } else if (dashBoardBO.getCode().equalsIgnoreCase(CODE9)) {


                            dashBoardBO.setKpiAcheived(Integer.toString(transactionPerDay));
                            int kpiTarget;

                            try {
                                kpiTarget = (int) Double.parseDouble(dashBoardBO.getKpiTarget());
                            } catch (Exception e) {
                                kpiTarget = 0;
                                Commons.printException(e + "");
                            }

                            if (kpiTarget == 0) {
                                dashBoardBO.setCalculatedPercentage(0);
                            } else {
                                dashBoardBO.setCalculatedPercentage((transactionPerDay * 100) / kpiTarget);
                            }
                            if (dashBoardBO.getCalculatedPercentage() >= 100) {
                                dashBoardBO.setConvTargetPercentage(0);
                                dashBoardBO.setConvAcheivedPercentage(100);
                            } else {
                                dashBoardBO.setConvTargetPercentage(100 - dashBoardBO
                                        .getCalculatedPercentage());
                                dashBoardBO.setConvAcheivedPercentage(dashBoardBO
                                        .getCalculatedPercentage());
                            }

                        } else if (dashBoardBO.getCode().equalsIgnoreCase(CODE10)) {


                            dashBoardBO.setKpiAcheived(Integer.toString(avgUnitsPerBill));
                            int kpiTarget;

                            try {
                                kpiTarget = (int) Double.parseDouble(dashBoardBO.getKpiTarget());
                            } catch (Exception e) {
                                kpiTarget = 0;
                                Commons.printException(e + "");
                            }

                            if (kpiTarget == 0) {
                                dashBoardBO.setCalculatedPercentage(0);
                            } else {
                                dashBoardBO.setCalculatedPercentage((avgUnitsPerBill * 100) / kpiTarget);
                            }
                            if (dashBoardBO.getCalculatedPercentage() >= 100) {
                                dashBoardBO.setConvTargetPercentage(0);
                                dashBoardBO.setConvAcheivedPercentage(100);
                            } else {
                                dashBoardBO.setConvTargetPercentage(100 - dashBoardBO
                                        .getCalculatedPercentage());
                                dashBoardBO.setConvAcheivedPercentage(dashBoardBO
                                        .getCalculatedPercentage());
                            }

                        } else if (dashBoardBO.getCode().equalsIgnoreCase(CODE11)) {


                            dashBoardBO.setKpiAcheived(Integer.toString(avgSellingPrice));
                            int kpiTarget;

                            try {
                                kpiTarget = (int) Double.parseDouble(dashBoardBO.getKpiTarget());
                            } catch (Exception e) {
                                kpiTarget = 0;
                                Commons.printException(e + "");
                            }

                            if (kpiTarget == 0) {
                                dashBoardBO.setCalculatedPercentage(0);
                            } else {
                                dashBoardBO.setCalculatedPercentage((avgSellingPrice * 100) / kpiTarget);
                            }
                            if (dashBoardBO.getCalculatedPercentage() >= 100) {
                                dashBoardBO.setConvTargetPercentage(0);
                                dashBoardBO.setConvAcheivedPercentage(100);
                            } else {
                                dashBoardBO.setConvTargetPercentage(100 - dashBoardBO
                                        .getCalculatedPercentage());
                                dashBoardBO.setConvAcheivedPercentage(dashBoardBO
                                        .getCalculatedPercentage());
                            }

                        } else if (dashBoardBO.getCode().equalsIgnoreCase(CODE12)) {


                            dashBoardBO.setKpiAcheived(Integer.toString(avgBillValue));
                            int kpiTarget;

                            try {
                                kpiTarget = (int) Double.parseDouble(dashBoardBO.getKpiTarget());
                            } catch (Exception e) {
                                kpiTarget = 0;
                                Commons.printException(e + "");
                            }

                            if (kpiTarget == 0) {
                                dashBoardBO.setCalculatedPercentage(0);
                            } else {
                                dashBoardBO.setCalculatedPercentage((avgBillValue * 100) / kpiTarget);
                            }
                            if (dashBoardBO.getCalculatedPercentage() >= 100) {
                                dashBoardBO.setConvTargetPercentage(0);
                                dashBoardBO.setConvAcheivedPercentage(100);
                            } else {
                                dashBoardBO.setConvTargetPercentage(100 - dashBoardBO
                                        .getCalculatedPercentage());
                                dashBoardBO.setConvAcheivedPercentage(dashBoardBO
                                        .getCalculatedPercentage());
                            }

                        }
                    }
                    dashBoardListViewAdapter.notifyDataSetChanged();
                } else if (selectedInterval.equals(P3M)) {
                    monthSpinner.setVisibility(View.VISIBLE);

                    // Creating adapter for spinner
                    final ArrayList<String> monthNameList = bmodel.dashBoardHelper.getSellerKpiMonthNameList();
                    ArrayAdapter<String> monthdapter = new ArrayAdapter<>(getActivity(), R.layout.dashboard_spinner_layout, monthNameList);
                    monthdapter.setDropDownViewResource(R.layout.dashboard_spinner_list);
                    monthSpinner.setAdapter(monthdapter);
                    monthSpinner.setOnItemSelectedListener(this);
                    monthSpinner.setSelection(0);
                } else {
                    dashBoardListViewAdapter.notifyDataSetChanged();
                }

            } else if (menuid == R.id.userSpinner) {
                mSelectedUserId = userMasterBOArrayAdapter.getItem(position).getUserid();
                if (!isFromHomeScreenTwo) {
                    if (selectedInterval.equals(P3M))
                        bmodel.dashBoardHelper.loadSellerDashBoard(Integer.toString(mSelectedUserId));
                    else
                        bmodel.dashBoardHelper.loadSellerDashBoard(Integer.toString(mSelectedUserId), selectedInterval);
                }

                gridListDataLoad(beatPosition);
                mDashboardList = bmodel.dashBoardHelper.getDashListViewList();
                dashBoardListViewAdapter = new DashBoardListViewAdapter(mDashboardList);
                dashBoardList.setAdapter(dashBoardListViewAdapter);
                if (show_trend_chart) {
                    if (mDashboardList != null && mDashboardList.size() > 0) {
                        bmodel.dashBoardHelper.setDashboardBO(mDashboardList.get(0));
                    }
                    bmodel.dashBoardHelper.loadP3MTrendChaart(Integer.toString(mSelectedUserId));
                    adapterViewPager = new MyPagerAdapter(getActivity().getSupportFragmentManager(), fragmentList);
                    new setAdapterTask().execute();
                }


            } else if (menuid == R.id.distributorSpinner) {
                int mSelectedDistributorId;
                mSelectedDistributorId = Integer.parseInt(distributorMasterBOArrayAdapter.getItem(position).getDId());
                userMasterBOArrayAdapter.clear();
                userMasterBOArrayAdapter.add(new UserMasterBO(0, getResources().getString(R.string.all)));
                userSpinner.setAdapter(userMasterBOArrayAdapter);
                if (mSelectedDistributorId != 0)
                    for (UserMasterBO bo : bmodel.userMasterHelper.downloadUserList(mSelectedDistributorId)) {
                        userMasterBOArrayAdapter.add(bo);
                    }
                userMasterBOArrayAdapter.notifyDataSetChanged();

            } else if (menuid == R.id.monthSpinner) {

                final String filterName = monthSpinner.getSelectedItem().toString();
                updateMonth(filterName);
            }
        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //no operation
    }

    private void updateMonth(String monthName) {
        mDashboardList = new ArrayList<>();

        for (DashBoardBO dashBoardBO : bmodel.dashBoardHelper.getDashListViewList()) {
            if (dashBoardBO.getMonthName().equals(monthName)) {
                mDashboardList.add(dashBoardBO);
            }
        }
        dashBoardList.setAdapter(new DashBoardListViewAdapter(mDashboardList));
        if (show_trend_chart) {
            checkandaddScreens();
            //calculateIncentive();
            if (mDashboardList != null && mDashboardList.size() > 0) {
                bmodel.dashBoardHelper.setDashboardBO(mDashboardList.get(0));
            }
            bmodel.dashBoardHelper.loadP3MTrendChaart(Integer.toString(mSelectedUserId));
            adapterViewPager = new MyPagerAdapter(getActivity().getSupportFragmentManager(), fragmentList);
            new setAdapterTask().execute();
        }
    }

    private void updateAll() {
        mDashboardList = bmodel.dashBoardHelper.getDashListViewList();
        dashBoardList.setAdapter(new DashBoardListViewAdapter(mDashboardList));
        if (show_trend_chart) {
            checkandaddScreens();
            calculateIncentive();
            if (mDashboardList != null && mDashboardList.size() > 0) {
                bmodel.dashBoardHelper.setDashboardBO(mDashboardList.get(0));
            }
            bmodel.dashBoardHelper.loadP3MTrendChaart(Integer.toString(mSelectedUserId));
            adapterViewPager = new MyPagerAdapter(getActivity().getSupportFragmentManager(), fragmentList);
            new setAdapterTask().execute();
        }
    }

    private void calculateIncentive() {
        incentive = 0;
        for (DashBoardBO dash : mDashboardList) {
            incentive = incentive + Double.parseDouble(dash.getKpiIncentive());
        }
        bmodel.dashBoardHelper.mParamAchieved = incentive;
    }

    private int getRetailerDetail(String flag) {
        int size = bmodel.getRetailerMaster().size();
        int count = 0;
        /** Add today's retailers. **/
        if (flag.equals("P")) {
            for (int i = 0; i < size; i++) {
                if (bmodel.getRetailerMaster().get(i).getIsToday() == 1) {
                    count++;
                }

            }
        } else {
            for (int i = 0; i < size; i++) {
                if (bmodel.getRetailerMaster().get(i).getIsVisited().equals("Y")) {
                    count++;
                }
            }
        }

        return count;

    }

    private int getPromotionDetail(String flag) {
        DBUtil db = null;
        int size = bmodel.getRetailerMaster().size();
        int count = 0;
        String chIDs = "";

        if (flag.equals("P")) {
            for (int i = 0; i < size; i++) {
                if (bmodel.getRetailerMaster().get(i).getIsToday() == 1) {
                    chIDs = chIDs + "," + bmodel.schemeDetailsMasterHelper.getChannelidForScheme(bmodel.getRetailerMaster().get(i).getSubchannelid());
                }
            }

            if (chIDs.endsWith(","))
                chIDs = chIDs.substring(0, chIDs.length() - 1);

            try {
                db = new DBUtil(getContext(), DataMembers.DB_NAME, DataMembers.DB_PATH);
                db.createDataBase();
                db.openDataBase();
                StringBuffer sb = new StringBuffer();
                sb.append("SELECT count(PromoID) FROM PromotionMapping where chid in (" + chIDs + ")");

                Cursor c = db.selectSQL(sb.toString());
                if (c.getCount() > 0) {
                    while (c.moveToNext()) {
                        count = c.getInt(0);
                    }
                }
                c.close();
                db.closeDB();
            } catch (Exception e) {
                Commons.printException("" + e);
            }
        } else {
            for (int i = 0; i < size; i++) {
                if (bmodel.getRetailerMaster().get(i).getIsToday() == 1) {
                    count = count + getPromotionExecDetail(bmodel.getRetailerMaster().get(i).getRetailerID());
                }
            }
        }

        return count;

    }

    private int getPromotionExecDetail(String retailerID) {
        DBUtil db = null;
        int count = 0;
        try {
            db = new DBUtil(getContext(), DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            StringBuffer sb = new StringBuffer();
            sb.append("SELECT count( distinct PromotionID) FROM PromotionDetail where RetailerID =" + bmodel.QT(retailerID));

            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    count = c.getInt(0);
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }

        return count;

    }

    ArrayList<Integer> mslProdIDs = new ArrayList<>();

    private int getMSLDetail(String flag) {
        DBUtil db = null;
        int size = bmodel.getRetailerMaster().size();
        int count = 0;
        String chIDs = "";
        mslProdIDs = new ArrayList<>();
        if (flag.equals("P")) {
            for (int i = 0; i < size; i++) {
                if (bmodel.getRetailerMaster().get(i).getIsToday() == 1) {
                    chIDs = chIDs + "," + bmodel.schemeDetailsMasterHelper.getChannelidForScheme(bmodel.getRetailerMaster().get(i).getSubchannelid());
                }
            }
            if (chIDs.endsWith(","))
                chIDs = chIDs.substring(0, chIDs.length() - 1);

            try {
                db = new DBUtil(getContext(), DataMembers.DB_NAME, DataMembers.DB_PATH);
                db.createDataBase();
                db.openDataBase();
                StringBuffer sb = new StringBuffer();
                sb.append("SELECT PTGM.pid FROM ProductTaggingMaster PTM ");
                sb.append("inner join ProductTaggingGroupMapping PTGM on PTGM.groupid = PTCM.groupid ");
                sb.append("inner join  ProductTaggingCriteriaMapping PTCM on PTM.groupid = PTCM.groupid ");
                sb.append("AND PTM.TaggingTypelovID in (select listid from standardlistmaster where listcode='MSL' and listtype='PRODUCT_TAGGING') ");
                sb.append("where criteriatype = 'CHANNEL' and Criteriaid in (" + chIDs + ")");

                Cursor c = db.selectSQL(sb.toString());
                if (c.getCount() > 0) {
                    while (c.moveToNext()) {
                        count++;
                        if (!mslProdIDs.contains(c.getInt(1)))
                            mslProdIDs.add(c.getInt(1));
                    }
                }
                c.close();
                db.closeDB();
            } catch (Exception e) {
                Commons.printException("" + e);
            }
        } else {
            for (int i = 0; i < size; i++) {
                if (bmodel.getRetailerMaster().get(i).getIsToday() == 1) {
                    count = count + getMslExecDetail(bmodel.getRetailerMaster().get(i).getRetailerID());
                }
            }
        }
        return count;

    }

    private int getMslExecDetail(String retailerID) {
        DBUtil db = null;
        int count = 0;
        try {
            db = new DBUtil(getContext(), DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            StringBuffer sb = new StringBuffer();
            sb.append("select count(*) from OrderDetail where retailerid = " + bmodel.QT(retailerID));
            sb.append("and ProductID in (" + mslProdIDs + ")");
            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    count = c.getInt(0);
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }

        return count;

    }

    private void getCounterSalesDetail() {
        DBUtil db = null;

        try {
            db = new DBUtil(getContext(), DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            StringBuffer sb = new StringBuffer();
            sb.append("SELECT count(distinct uid)as TRN,(count(pid)/count(distinct uid)) as avgUnitsBill,");
            sb.append("(sum(price)/count(pid)) as avgSellBill ,(sum(value)/count(distinct uid)) as avgBill ");
            sb.append("FROM CS_CustomerSaleDetails");
            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    transactionPerDay = c.getInt(0);
                    avgUnitsPerBill = c.getInt(1);
                    avgSellingPrice = c.getInt(2);
                    avgBillValue = c.getInt(3);
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }


    }

    public class MyPagerAdapter extends FragmentPagerAdapter {
        private ArrayList<Fragment> fragmentList = new ArrayList<>();

        public MyPagerAdapter(FragmentManager fragmentManager, ArrayList<Fragment> fragmentList) {
            super(fragmentManager);
            this.fragmentList = fragmentList;
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return this.fragmentList.size();
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            return this.fragmentList.get(position);
        }

    }

    private void loadUserSpinner(String distrubutorIds) {
        ArrayList<UserMasterBO> users;
        final List<KeyPairBoolData> userArray = new ArrayList<>();
        mFilterUser = "";
        if (distrubutorIds.equals("0"))
            users = bmodel.dashBoardHelper.downloadUserList();

        else
            users = bmodel.userMasterHelper.downloadUserList(distrubutorIds);

        userArray.add(new KeyPairBoolData(0, getResources().getString(R.string.all), true));
        int count = 0;
        for (int i = 0; i < users.size(); i++) {
            KeyPairBoolData h = new KeyPairBoolData();
            h.setId(users.get(i).getUserid());
            h.setName(users.get(i).getUserName());
            h.setSelected(true);
            userArray.add(h);
            count++;
            mFilterUser += bmodel.QT(users.get(i).getUserid() + "");
            if (count != users.size())
                mFilterUser += ",";
        }
        userSpinner1.setItems(userArray, -1, new SpinnerListener() {
            @Override
            public void onItemsSelected(List<KeyPairBoolData> items) {
                Commons.print("Multi" + items.size());
                int count = 0;
                mFilterUser = "";
                for (int i = 0; i < items.size(); i++) {
                    count++;
                    mFilterUser += bmodel.QT(items.get(i).getId() + "");
                    if (count != items.size())
                        mFilterUser += ",";
                }
                loadListChart();
            }
        });
        loadListChart();
    }

    private void loadListChart() {
        bmodel.dashBoardHelper.loadKpiDashBoard(mFilterUser + "", selectedInterval);
        gridListDataLoad(beatPosition);
        mDashboardList = bmodel.dashBoardHelper.getDashListViewList();
        dashBoardListViewAdapter = new DashBoardListViewAdapter(mDashboardList);
        dashBoardList.setAdapter(dashBoardListViewAdapter);
        if (show_trend_chart) {
            checkandaddScreens();
            adapterViewPager = new MyPagerAdapter(getActivity().getSupportFragmentManager(), fragmentList);
            vpPager.setAdapter(adapterViewPager);
            indicator.setViewPager(vpPager);
        }

    }

    private void checkandaddScreens() {
        NUM_ITEMS = 1;
        //bmodel.dashBoardHelper.setDashboardBO(null);
        fragmentList = new ArrayList<>();

        if (bmodel.configurationMasterHelper.IS_SMP_BASED_DASH) {
            if (bmodel.configurationMasterHelper.SHOW_P3M_DASH) {
                NUM_ITEMS++;
                bmodel.dashBoardHelper.loadP3MTrendChaart(mFilterUser);
                fragmentList.add(new P3MChartFragment());
            }
            if (bmodel.configurationMasterHelper.SHOW_SMP_DASH) {
                NUM_ITEMS++;
                fragmentList.add(new SMPChartFragment());
                chartpositionSMP = NUM_ITEMS;
            }
            if (bmodel.configurationMasterHelper.SHOW_INV_DASH) {
                NUM_ITEMS++;
                Fragment fragment = new TotalAchivedFragment();
                Bundle args = new Bundle();
                if (mDashboardList != null && mDashboardList.size() > 0) {
                    args.putInt("flex1", mDashboardList.get(0).getFlex1());
                } else {
                    args.putInt("flex1", 0);
                }
                fragment.setArguments(args);
                fragmentList.add(fragment);
            }
        }
//        else if (show_trend_chart) {
//            bmodel.dashBoardHelper.loadP3MTrendChaart(mFilterUser);
//            fragmentList.add(new P3MChartFragment());
//        }
    }

    private class setAdapterTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            vpPager.setAdapter(adapterViewPager);
            indicator.setViewPager(vpPager);
        }
    }

}
