package com.ivy.cpg.view.dashboard.sellerdashboard;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
import com.ivy.cpg.view.dashboard.DashBoardBO;
import com.ivy.cpg.view.dashboard.DashBoardHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.BeatMasterBO;
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
import com.ivy.cpg.view.homescreen.HomeScreenActivity;
import com.ivy.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import me.relex.circleindicator.CircleIndicator;

public class SellerDashboardFragment extends IvyBaseFragment implements AdapterView.OnItemSelectedListener, SellerDashboardContractor.SellerDashView {

    private BusinessModel bmodel;
    private final int beatPosition = 0;
    private RecyclerView dashBoardList;
    private FragmentManager fm;
    public DashBoardListViewAdapter dashBoardListViewAdapter;
    private int mSelectedUserId = 0;
    private boolean show_trend_chart = false;
    private CollapsingToolbarLayout collapsing;

    private String selectedInterval = MONTH;

    /*new spinner configuration*/
    private Spinner dashSpinner;
    private Spinner userSpinner;
    private Spinner monthSpinner;
    private Spinner weekSpinner;
    private Spinner routeSpinner;

    private static final String MONTH = "MONTH";
    private static final String DAY = "DAY";
    private static final String P3M = "P3M";
    private static final String WEEK = "WEEK";
    private static final String ROUTE = "ROUTE";

    private int NUM_ITEMS = 1;
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
    boolean isFromHomeScreenTwo = false;
    private String menuCode = "";
    private String type = "";
    Bundle bundle;
    private boolean _hasLoadedOnce = false;
    private ArrayList<String> categories;

    private SellerDashboardContractor.SellerDashPresenter dashboardPresenter;
    private DashBoardHelper dashBoardHelper;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        dashBoardHelper = DashBoardHelper.getInstance(getActivity());
        dashboardPresenter = new SellerDashPresenterImpl(getContext());
        dashboardPresenter.setView(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_seller_dashboard, container, false);

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        fm = getActivity().getSupportFragmentManager();

        bundle = getArguments();
        if (bundle == null)
        bundle = getActivity().getIntent().getExtras();
        boolean isFromTab = false;

        if (bundle != null) {
            isFromHomeScreenTwo = bundle.getBoolean("isFromHomeScreenTwo", false);
            menuCode = bundle.getString("menuCode");
            isFromTab = bundle.getBoolean("isFromTab", false);
            type = bundle.getString("type");
        }

        if (getActionBar() != null)
            setUpActionBar();

        setHasOptionsMenu(true);
        if (isFromTab == false) {
            initializeViews();
        }
        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isFragmentVisible_) {
        super.setUserVisibleHint(isFragmentVisible_);

        if (this.isVisible()) {
            // we check that the fragment is becoming visible
            isFragmentVisible_ = false;
            if (!isFragmentVisible_ && !_hasLoadedOnce) {
                //run your async task here since the user has just focused on your fragment
                initializeViews();
                _hasLoadedOnce = true;
            }
        }
    }


    private void initializeViews() {
        init();
    }

    private ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    private void init() {
        vpPager = view.findViewById(R.id.viewpager);
        collapsing = view.findViewById(R.id.collapsing);
        indicator = view.findViewById(R.id.indicator);

        dashBoardList = view.findViewById(R.id.dashboardLv);
        ((TextView) view.findViewById(R.id.textView)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        categories = new ArrayList<>();
        if(type != null && type.equals(ROUTE)){
            categories = dashBoardHelper.getRouteDashList();
        } else {
            categories = dashBoardHelper.getDashList(isFromHomeScreenTwo);
        }
        if (categories != null) {
            setpUpSpinner(categories);

        }


        try {

            String download = dashBoardHelper.getLastDownloadDate() ;

            TextView last_sync = view.findViewById(R.id.text_last_sync);

            last_sync.setText(download);
        }catch (Exception e){
            Commons.printException(e);
        }



        if (bmodel.configurationMasterHelper.IS_SMP_BASED_DASH) {
            show_trend_chart = true;
        }

        if (!show_trend_chart) {
            vpPager.setVisibility(View.GONE);
            collapsing.setVisibility(View.GONE);

        }
        tvDistributorName = view.findViewById(R.id.tv_distributor_title);
        tvUserName = view.findViewById(R.id.tv_username_title);

        tvDistributorName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        tvUserName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

        LinearLayout spinner_layout1 = view.findViewById(R.id.spinner_layout1);
        LinearLayout spinner_layout2 = view.findViewById(R.id.spinner_layout2);
        userSpinner = view.findViewById(R.id.userSpinner);

        if (!isFromHomeScreenTwo) {
            if (bmodel.configurationMasterHelper.IS_USER_BASED_DASH && bmodel.configurationMasterHelper.IS_DISTRIBUTOR_BASED_DASH) {
                if (bmodel.configurationMasterHelper.IS_NIVEA_BASED_DASH) {
                    spinner_layout1.setVisibility(View.GONE);
                    spinner_layout2.setVisibility(View.VISIBLE);
                    ll_distributor = view.findViewById(R.id.ll_distributor);
                    distrSpinner1 = view.findViewById(R.id.distributorSpinner1);
                    ll_distributor.setVisibility(View.VISIBLE);

                    ll_users = view.findViewById(R.id.ll_users);
                    userSpinner1 = view.findViewById(R.id.userSpinner1);
                    ll_users.setVisibility(View.VISIBLE);

                    final List<KeyPairBoolData> distArray = new ArrayList<>();
                    ArrayList<DistributorMasterBO> distributors = bmodel.distributorMasterHelper.getDistributors();
                    distArray.add(new KeyPairBoolData(0, getResources().getString(R.string.all), true));
                    int count = 0;

                    for (int j = 0; j < distributors.size(); j++) {
                        KeyPairBoolData h = new KeyPairBoolData();
                        h.setId(SDUtil.convertToInt(distributors.get(j).getDId()));
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
                            dashboardPresenter.updateUserData(mSelectedDistributorId);
                        }
                    });
                    dashboardPresenter.updateUserData(mSelectedDistributorId);
                    if (!show_trend_chart) {
                        vpPager.setVisibility(View.GONE);
                        collapsing.setVisibility(View.GONE);
                    }
                } else {
                    spinner_layout1.setVisibility(View.VISIBLE);
                    spinner_layout2.setVisibility(View.GONE);
                    Spinner distrSpinner = view.findViewById(R.id.distributorSpinner);
                    distrSpinner.setVisibility(View.VISIBLE);

                    userSpinner = view.findViewById(R.id.userSpinner);
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
                        dashBoardHelper.loadSellerDashBoard(Integer.toString(mSelectedUserId), MONTH);
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
                    dashBoardHelper.loadSellerDashBoard(Integer.toString(mSelectedUserId), MONTH);
                }
            }
        } else {
            spinner_layout1.setVisibility(View.VISIBLE);
            view.findViewById(R.id.distributorSpinner).setVisibility(View.GONE);
            userSpinner.setVisibility(View.GONE);
        }

        if (type != null
                && type.equals(ROUTE)) {
            bmodel.beatMasterHealper.downloadBeats();
            Vector<BeatMasterBO> monthNameList = bmodel.beatMasterHealper.getBeatMaster();
            routeSpinner.setVisibility(View.VISIBLE);
            ArrayAdapter<BeatMasterBO> monthdapter = new ArrayAdapter<>(getActivity(), R.layout.dashboard_spinner_layout, monthNameList);
            monthdapter.setDropDownViewResource(R.layout.dashboard_spinner_list);
            routeSpinner.setAdapter(monthdapter);
            routeSpinner.setOnItemSelectedListener(this);
        }

        if (!isFromHomeScreenTwo) {
            mSelectedUserId = bmodel.userMasterHelper.getUserMasterBO().getUserid();
            dashBoardHelper.loadSellerDashBoard(Integer.toString(mSelectedUserId), MONTH);
        } else {
            vpPager.setVisibility(View.GONE);
            collapsing.setVisibility(View.GONE);
        }

        dashBoardList.setHasFixedSize(false);
        dashBoardList.setNestedScrollingEnabled(false);
        dashBoardList.setLayoutManager(new LinearLayoutManager(getActivity()));
        dashBoardHelper.setDashboardBO(new DashBoardBO());
        dashboardPresenter.gridListDataLoad(beatPosition);
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

            View v;

            String[] codeArray = bmodel.configurationMasterHelper.SELLER_KPI_CODES.split(",");
            boolean isCode = false;
            if (codeArray != null && codeArray.length > 0) {
                for (int i = 0; i < codeArray.length; i++) {
                    if (codeArray[i].equalsIgnoreCase(dashboardList.get(viewType).getCode())) {
                        isCode = true;
                        break;
                    }
                }
            }

            if (bmodel.configurationMasterHelper.IS_SWITCH_WITH_OUT_TGT && isCode) {
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.seller_dashboard_without_target_row_layout, parent, false);
            } else {
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.seller_dashboard_row_layout, parent, false);
            }

            return new SellerDashboardFragment.DashBoardListViewAdapter.ViewHolder(v);
        }


        @Override
        public void onBindViewHolder(final SellerDashboardFragment.DashBoardListViewAdapter.ViewHolder holder, final int position) {
            final DashBoardBO dashboardData = dashboardList.get(position);
            if (bmodel.configurationMasterHelper.SHOW_SCORE_DASH
                    || !bmodel.configurationMasterHelper.SHOW_INCENTIVE_DASH) {
                holder.incentive.setVisibility(View.GONE);
                holder.incentiveTitle.setVisibility(View.GONE);
            }

            holder.dashboardDataObj = dashboardData;
            //typefaces
            holder.factorName.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
            holder.target.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.acheived.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.balance.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.index.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            if (holder.mChart.getVisibility() != View.VISIBLE) {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.index.getLayoutParams();
                params.setMargins(0, 0, 10, 0); //substitute parameters for left, top, right, bottom
                holder.index.setLayoutParams(params);
            }
            holder.incentive.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.score.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.incentiveTitle.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.kpiFlex1.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.scoreTitle.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
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

                        if (selectedInterval.equalsIgnoreCase(WEEK)) {
                            if (!mDashboardList.get(position).getMonthName().equals("")) {
                                //Weekly chart Specific Change
                                dashBoardHelper.getDashChartData().clear();
                                for (DashBoardBO dashBoardBO : dashBoardHelper.getDashboardMasterData()) {
                                    if (dashBoardBO.getCode().equalsIgnoreCase(mDashboardList.get(position).getCode())) {
                                        dashBoardHelper.getDashChartData().add(dashBoardBO);
                                    }
                                }
                            }
                        }
                        //P3M chart Specific Change
                        if (selectedInterval.equals(P3M)) {
                            dashBoardHelper.getDashChartData().clear();
                            for (DashBoardBO dashBoardBO : dashBoardHelper.getDashboardMasterData()) {
                                if (dashBoardBO.getCode().equalsIgnoreCase(mDashboardList.get(position).getCode())) {
                                    dashBoardHelper.getDashChartData().add(dashBoardBO);
                                }
                            }
                        }

                        if (mDashboardList != null && mDashboardList.size() > 0) {
                            dashBoardHelper.setDashboardBO(holder.dashboardDataObj);
                        }

                        dashBoardHelper.mParamLovId = dashboardData.getKpiTypeLovID();
                        adapterViewPager = new MyPagerAdapter(getActivity().getSupportFragmentManager(), fragmentList);
                        new setAdapterTask().execute();
                        vpPager.setCurrentItem(chartpositionSMP);
                    }
//                    }

                }
            });

            if (dashboardData.getSubDataCount() > 0&&bmodel.configurationMasterHelper.SHOW_NOR_DASHBOARD) {
                SpannableString str = new SpannableString(holder.tvSkuWise
                        .getText().toString());
                str.setSpan(new UnderlineSpan(), 0, str.length(),
                        Spanned.SPAN_PARAGRAPH);
                str.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.half_Black)), 0,
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

                        new LoadAsyncTask(holder.dashboardDataObj.getKpiID(), holder.dashboardDataObj.getKpiTypeLovID(), holder.dashboardDataObj.getFlex1(), holder.dashboardDataObj.getPId(), holder.dashboardDataObj.getCode()).execute();


                    } catch (Exception e) {
                        Commons.printException(e + "");
                    }
                }
            });

            if (dashboardData.getFlex1() == 1) {
                holder.acheived.setText(dashBoardHelper.getWhole(dashboardData.getKpiAcheived()));
                holder.target.setText(dashBoardHelper.getWhole(dashboardData.getKpiTarget()));
                double balanceValue = SDUtil.convertToInt(dashboardData.getKpiTarget()) - SDUtil.convertToInt(dashboardData.getKpiAcheived());
                holder.balance.setText(balanceValue > 0 ? dashBoardHelper.getWhole(bmodel.formatValue(balanceValue)) : "0");
                String strCalcPercentage = dashboardData.getCalculatedPercentage() + "%";
                float temp_ach = 0;
                if (SDUtil.convertToFloat(dashboardData.getKpiTarget()) > 0)
                    temp_ach = SDUtil.convertToFloat(dashboardData.getKpiAcheived()) - SDUtil.convertToFloat(dashboardData.getKpiTarget());
                if (temp_ach > 0) {
                    int bonus = Math.round(SDUtil.convertToFloat(dashboardData.getKpiAcheived()) /
                            (SDUtil.convertToFloat(dashboardData.getKpiTarget())) * 100);
                    holder.index.setText(SDUtil.roundIt(bonus, 1) + "%");
                } else {
                    holder.index.setText(strCalcPercentage);
                }
                holder.kpiFlex1.setText(dashBoardHelper.getWhole(dashboardData.getKpiFlex()));
                holder.incentive.setText(dashBoardHelper.getWhole(dashboardData.getKpiIncentive()));
                holder.score.setText(dashBoardHelper.getWhole(dashboardData.getKpiScore()));
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
                float temp_ach = 0;
                if (SDUtil.convertToFloat(dashboardData.getKpiTarget()) > 0)
                    temp_ach = SDUtil.convertToFloat(dashboardData.getKpiAcheived()) - SDUtil.convertToFloat(dashboardData.getKpiTarget());
                if (temp_ach > 0) {
                    int bonus = Math.round(SDUtil.convertToFloat(dashboardData.getKpiAcheived()) /
                            (SDUtil.convertToFloat(dashboardData.getKpiTarget())) * 100);
                    holder.index.setText(SDUtil.roundIt(bonus, 1) + "%");
                } else {
                    holder.index.setText(strCalcPercentage);
                }
                double balanceValue = SDUtil.convertToDouble(dashboardData.getKpiTarget()) - SDUtil.convertToDouble(dashboardData.getKpiAcheived());
                holder.balance.setText(balanceValue > 0 ? bmodel.formatValue(balanceValue) : "0");
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

                double balanceValue = SDUtil.convertToDouble(dashboardData.getKpiTarget()) - SDUtil.convertToDouble(dashboardData.getKpiAcheived());
                entries.add(new PieEntry(SDUtil.convertToFloat(dashboardData.getKpiAcheived())));
                entries.add(new PieEntry(balanceValue >= 0 ? SDUtil.convertToFloat(balanceValue + "") : 0));

                PieDataSet dataSet = new PieDataSet(entries, "");

                dataSet.setSliceSpace(0f);
                dataSet.setSelectionShift(5f);

                // add a lot of colors

                ArrayList<Integer> colors = new ArrayList<>();

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
            if (!bundle.containsKey("screentitle"))
                setScreenTitle(bmodel.getMenuName("MENU_DASH"));
            else
                setScreenTitle(bundle.getString("screentitle"));
        } else {
            if (!bundle.containsKey("screentitle")) {
                bmodel.configurationMasterHelper
                        .downloadNewActivityMenu(ConfigurationMasterHelper.MENU_ACTIVITY);
                setScreenTitle(bmodel.getMenuName("MENU_RTR_KPI"));
            } else
                setScreenTitle(bundle.getString("screentitle"));
        }
        //if (!BusinessModel.dashHomeStatic) {
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowHomeEnabled(true);
        //}
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
            if (isFromHomeScreenTwo) {//update time stamp if previous screen is homescreentwo
                bmodel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
                        .now(DateTimeUtils.TIME));
                bmodel.saveModuleCompletion(menuCode, true);
            }
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


    private void setpUpSpinner(ArrayList<String> categoriesList) {
        try {
            dashSpinner = view.findViewById(R.id.dashSpinner);
            dashSpinner.setVisibility(View.VISIBLE);
            dashSpinner.setOnItemSelectedListener(this);

            if (categoriesList.contains(P3M))
                show_trend_chart = true;

            // Creating adapter for spinner
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(), R.layout.dashboard_spinner_layout, categoriesList);

            dataAdapter.setDropDownViewResource(R.layout.dashboard_spinner_list);

            // attaching data adapter to spinner
            dashSpinner.setAdapter(dataAdapter);
            monthSpinner = view.findViewById(R.id.monthSpinner);
            weekSpinner = view.findViewById(R.id.weekSpinner);
            routeSpinner = view.findViewById(R.id.routeSpinner);
        } catch (Exception e) {

        }


    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        try {
            int menuid = parent.getId();
            if (menuid == R.id.dashSpinner) {
                selectedInterval = dashSpinner.getSelectedItem().toString();
                if (!isFromHomeScreenTwo) {
                    if (selectedInterval.equals(P3M))
                        dashBoardHelper.loadSellerDashBoard(Integer.toString(mSelectedUserId));
                    else if (selectedInterval.equals(WEEK))
                        dashBoardHelper.loadSellerDashBoardforWeek(Integer.toString(mSelectedUserId));
                    else {
                        if (type.equals(ROUTE))
                            dashBoardHelper.loadRouteDashBoard(selectedInterval);
                        else
                            dashBoardHelper.loadSellerDashBoard(Integer.toString(mSelectedUserId), selectedInterval);
                    }

                } else {
                    dashBoardHelper.loadRetailerDashBoard(bmodel.getRetailerMasterBO().getRetailerID() + "", selectedInterval);
                }
                dashboardPresenter.gridListDataLoad(beatPosition);
                updateAll();
                monthSpinner.setVisibility(View.GONE);

                if (selectedInterval.equals(DAY) && mSelectedUserId == bmodel.userMasterHelper.getUserMasterBO().getUserid()) {
                    dashboardPresenter.computeDayAchivements();
                    dashBoardListViewAdapter.notifyDataSetChanged();
                } else if (selectedInterval.equals(P3M)) {
                    monthSpinner.setVisibility(View.VISIBLE);
                    weekSpinner.setVisibility(View.GONE);
                    final ArrayList<String> monthNameList = dashBoardHelper.getKpiMonthNameList(isFromHomeScreenTwo);
                    ArrayAdapter<String> monthdapter = new ArrayAdapter<>(getActivity(), R.layout.dashboard_spinner_layout, monthNameList);
                    monthdapter.setDropDownViewResource(R.layout.dashboard_spinner_list);
                    monthSpinner.setAdapter(monthdapter);
                    monthSpinner.setOnItemSelectedListener(this);
                    monthSpinner.setSelection(0);
                } else if (selectedInterval.equals(WEEK)) {
                    dashBoardHelper.getSellerKpiWeekList();
                    final ArrayList<String> weekList = dashBoardHelper.getWeekList();
                    if (weekList != null && weekList.size() > 0) {
                        weekSpinner.setVisibility(View.VISIBLE);
                        ArrayAdapter<String> monthdapter = new ArrayAdapter<>(getActivity(), R.layout.dashboard_spinner_layout, weekList);
                        monthdapter.setDropDownViewResource(R.layout.dashboard_spinner_list);
                        weekSpinner.setAdapter(monthdapter);
                        weekSpinner.setOnItemSelectedListener(this);
                        weekSpinner.setSelection(dashBoardHelper.getCurrentWeek());
                    } else {
                        weekSpinner.setVisibility(View.GONE);
                        //dashBoardHelper.loadSellerDashBoardforWeek(Integer.toString(mSelectedUserId));
                        updateWeek("");
                    }
                } else {
                    weekSpinner.setVisibility(View.GONE);
                    dashBoardListViewAdapter.notifyDataSetChanged();
                }

            } else if (menuid == R.id.userSpinner) {
                mSelectedUserId = userMasterBOArrayAdapter.getItem(position).getUserid();
                if (!isFromHomeScreenTwo) {
                    if (selectedInterval.equals(P3M))
                        dashBoardHelper.loadSellerDashBoard(Integer.toString(mSelectedUserId));
                    else
                        dashBoardHelper.loadSellerDashBoard(Integer.toString(mSelectedUserId), selectedInterval);
                }

                dashboardPresenter.gridListDataLoad(beatPosition);
                mDashboardList = dashBoardHelper.getDashChartData();
                dashBoardListViewAdapter = new DashBoardListViewAdapter(mDashboardList);
                dashBoardList.setAdapter(dashBoardListViewAdapter);
                if (show_trend_chart) {
                    if (mDashboardList != null && mDashboardList.size() > 0) {
                        dashBoardHelper.setDashboardBO(mDashboardList.get(0));
                    }
                    dashBoardHelper.loadP3MTrendChaart(Integer.toString(mSelectedUserId));
                    adapterViewPager = new MyPagerAdapter(getActivity().getSupportFragmentManager(), fragmentList);
                    new setAdapterTask().execute();
                }


            } else if (menuid == R.id.distributorSpinner) {
                int mSelectedDistributorId;
                mSelectedDistributorId = SDUtil.convertToInt(distributorMasterBOArrayAdapter.getItem(position).getDId());
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
            } else if (menuid == R.id.weekSpinner) {
                //final String filterName = dashBoardHelper.getEnumNamefromValue(weekSpinner.getSelectedItem().toString());
                final String filterName = weekSpinner.getSelectedItem().toString();
                updateWeek(filterName);
            } else if (menuid == R.id.routeSpinner) {
                final String filterName = routeSpinner.getSelectedItem().toString();
                updateRoute(filterName);
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

        for (DashBoardBO dashBoardBO : dashBoardHelper.getDashboardMasterData()) {
            if (dashBoardBO.getMonthName().equalsIgnoreCase(monthName)) {
                mDashboardList.add(dashBoardBO);
            }
        }
        dashBoardList.setAdapter(new DashBoardListViewAdapter(mDashboardList));
        if (show_trend_chart) {
            //P3M chart Specific Change
            if (selectedInterval.equals(P3M)) {
                dashBoardHelper.getDashChartData().clear();
                for (DashBoardBO dashBoardBO : dashBoardHelper.getDashboardMasterData()) {
                    if (dashBoardBO.getCode().equalsIgnoreCase(mDashboardList.get(0).getCode())) {
                        dashBoardHelper.getDashChartData().add(dashBoardBO);
                    }
                }
            }
            checkandaddScreens();
            if (mDashboardList != null && mDashboardList.size() > 0) {
                dashBoardHelper.setDashboardBO(mDashboardList.get(0));
            }
            dashBoardHelper.loadP3MTrendChaart(Integer.toString(mSelectedUserId));
            adapterViewPager = new MyPagerAdapter(getActivity().getSupportFragmentManager(), fragmentList);
            new setAdapterTask().execute();
        }
    }

    private void updateWeek(String weekName) {
        mDashboardList = new ArrayList<>();

        for (DashBoardBO dashBoardBO : dashBoardHelper.getDashboardMasterData()) {
            if (dashBoardBO.getMonthName().equalsIgnoreCase(weekName) || weekName.equals("")) {
                mDashboardList.add(dashBoardBO);
            }
        }

        dashBoardList.setAdapter(new DashBoardListViewAdapter(mDashboardList));
        if (show_trend_chart) {

            //Weekly chart Specific Change
            if (!weekName.equals("")) {
                dashBoardHelper.getDashChartData().clear();
                for (DashBoardBO dashBoardBO : dashBoardHelper.getDashboardMasterData()) {
                    if (dashBoardBO.getCode().equalsIgnoreCase(mDashboardList.get(0).getCode())) {
                        dashBoardHelper.getDashChartData().add(dashBoardBO);
                    }
                }
            }

            checkandaddScreens();
            if (mDashboardList != null && mDashboardList.size() > 0) {
                dashBoardHelper.setDashboardBO(mDashboardList.get(0));
            }
            dashBoardHelper.loadP3MTrendChaart(Integer.toString(mSelectedUserId));
            adapterViewPager = new MyPagerAdapter(getActivity().getSupportFragmentManager(), fragmentList);
            new setAdapterTask().execute();
        }
    }

    private void updateRoute(String routeName) {
        mDashboardList = new ArrayList<>();

        for (DashBoardBO dashBoardBO : dashBoardHelper.getDashboardMasterData()) {
            if (dashBoardBO.getMonthName().equalsIgnoreCase(routeName)) {
                mDashboardList.add(dashBoardBO);
            }
        }

        dashBoardList.setAdapter(new DashBoardListViewAdapter(mDashboardList));
        if (show_trend_chart) {

            dashBoardHelper.getDashChartData().clear();
            for (DashBoardBO dashBoardBO : dashBoardHelper.getDashboardMasterData()) {
                if (dashBoardBO.getMonthName().equalsIgnoreCase(routeName)) {
                    dashBoardHelper.getDashChartData().add(dashBoardBO);
                }
            }

            checkandaddScreens();
            if (mDashboardList != null && mDashboardList.size() > 0) {
                dashBoardHelper.setDashboardBO(mDashboardList.get(0));
            }
            dashBoardHelper.loadP3MTrendChaart(Integer.toString(mSelectedUserId));
            adapterViewPager = new MyPagerAdapter(getActivity().getSupportFragmentManager(), fragmentList);
            new setAdapterTask().execute();
        }
    }

    private void updateAll() {
        mDashboardList = dashBoardHelper.getDashChartData();
        dashBoardList.setAdapter(new DashBoardListViewAdapter(mDashboardList));
        if (show_trend_chart) {
            checkandaddScreens();
            if (mDashboardList != null && mDashboardList.size() > 0) {
                dashBoardHelper.setDashboardBO(mDashboardList.get(0));
            }
            dashBoardHelper.loadP3MTrendChaart(Integer.toString(mSelectedUserId));
            adapterViewPager = new MyPagerAdapter(getActivity().getSupportFragmentManager(), fragmentList);
            new setAdapterTask().execute();
        }
    }


    public class MyPagerAdapter extends FragmentStatePagerAdapter {
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

    @Override
    public void loadUserSpinner(String filterUser, List<KeyPairBoolData> userArray) {

        mFilterUser = filterUser;

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
        dashBoardHelper.loadKpiDashBoard(mFilterUser + "", selectedInterval);
        dashboardPresenter.gridListDataLoad(beatPosition);
        mDashboardList = dashBoardHelper.getDashChartData();
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
        fragmentList = new ArrayList<>();

        if (bmodel.configurationMasterHelper.IS_SMP_BASED_DASH) {
            if (!selectedInterval.matches("WEEK|ROUTE")) {
                if (bmodel.configurationMasterHelper.SHOW_P3M_DASH) {
                    NUM_ITEMS++;
                    dashBoardHelper.loadP3MTrendChaart(mFilterUser);
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
            if (bmodel.configurationMasterHelper.SHOW_KPIBARCHART_DASH) {
                NUM_ITEMS++;
                KPIStackedBarChartFragment barchartFragment = new KPIStackedBarChartFragment();
                Bundle bundle = new Bundle();
                bundle.putString("selectedInterval", selectedInterval);
                barchartFragment.setArguments(bundle);
                fragmentList.add(barchartFragment);
            }

        }
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

    class LoadAsyncTask extends AsyncTask<String, Integer, Boolean> {
        private ProgressDialog progressDialogue;
        private int kpiId, kpiTypeLovId;
        private int flex1, pId;
        private String dashCode;


        public LoadAsyncTask(int kpiId, int kpiTypeLovId, int flex1, int pId, String dashCode) {
            super();
            this.kpiId = kpiId;
            this.kpiTypeLovId = kpiTypeLovId;
            this.flex1 = flex1;
            this.pId = pId;
            this.dashCode = dashCode;
        }

        @Override
        protected Boolean doInBackground(String... arg0) {
            try {
                if (bmodel.configurationMasterHelper.SHOW_NOR_DASHBOARD) {
                    if (!isFromHomeScreenTwo)
                        dashBoardHelper.findMinMaxProductLevelSellerKPI(kpiId, kpiTypeLovId, selectedInterval);
                    else
                        dashBoardHelper.findMinMaxProductLevelRetailerKPI(kpiId, kpiTypeLovId, selectedInterval);
                }
                return Boolean.TRUE;
            } catch (Exception e) {
                Commons.printException(e);
                return Boolean.FALSE;
            }

        }

        protected void onPreExecute() {
            progressDialogue = ProgressDialog.show(getActivity(),
                    DataMembers.SD, getResources().getString(R.string.loading_data),
                    true, false);
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground

            progressDialogue.dismiss();

            if (dashBoardHelper.getSellerKpiSku().size() > 0) {
                Intent i = new Intent(getActivity(),
                        SellerKPISKUActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                i.putExtra("screentitle",
                        bmodel.getMenuName("MENU_SKUWISESTGT"));
                i.putExtra("screentitlebk",
                        ((AppCompatActivity) getActivity()).getSupportActionBar().getTitle());
                i.putExtra("from", "4");
                i.putExtra("flex1", flex1);
                i.putExtra("pid", pId);
                i.putExtra("isFromDash", true);
                i.putExtra("dashCode", dashCode);
                startActivity(i);
            } else {
                bmodel.showAlert(
                        getResources().getString(R.string.no_products_exists), 0);
            }


        }

    }
}
