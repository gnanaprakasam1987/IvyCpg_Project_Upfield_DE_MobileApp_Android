package com.ivy.cpg.view.dashboard.olddashboard;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.ivy.cpg.view.dashboard.DashBoardHelper;
import com.ivy.cpg.view.dashboard.sellerdashboard.TotalAchivedFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.SKUWiseTargetBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.HalfPieChartFragement;
import com.ivy.cpg.view.homescreen.HomeScreenActivity;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.sd.png.view.PieChartFragement;

import java.util.ArrayList;
import java.util.List;

import me.relex.circleindicator.CircleIndicator;

public class SkuWiseTargetFragment extends IvyBaseFragment {

    private FragmentManager fm;
    private View view;
    private ArrayList<SKUWiseTargetBO> mylist;
    private BusinessModel bmodel;
    private String calledBy = "0";
    private String rid = "0";
    private String type = "";
    private String code = "0";
    private String prodName = "";
    private String from = "";
    private String monthName = "All";
    private int pid, flex1;
    private float acheivedPer = 0;
    private float targetPer = 0;
    private RecyclerView dashboardRv;
    private AlertDialog alertDialog;
    private boolean isFromDash;

    private static final String MONTH_TYPE = "MONTH";
    private static final String YEAR_TYPE = "YEAR";
    private static final String DAY_TYPE = "DAY";

    private static final String MONTH_TAG = "incentive_month";
    private static final String YEAR_TAG = "incentive_year";

    private Toolbar toolbar;
    ViewPager vpPager;
    CircleIndicator indicator;
    MyPagerAdapter adapterViewPager;

    private DashBoardHelper dashBoardHelper;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        dashBoardHelper = DashBoardHelper.getInstance(context);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_skuwise_target, container, false);


        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        fm = getActivity().getSupportFragmentManager();

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(null);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        Bundle extras;
        if (getActivity().getIntent().getExtras() != null)
            extras = getActivity().getIntent().getExtras();
        else
            extras = getArguments();

        //Set Screen Title
        try {
            if (extras.getString("screentitle") == null)
                setScreenTitle(bmodel.getMenuName("MENU_SKUWISESTGT"));
            else
                setScreenTitle(extras.getString("screentitle"));

            from = extras.getString("from");
            rid = extras.getString("rid");
            rid = rid != null ? rid : "0";
            type = extras.getString("type");
            type = type != null ? type : "MONTH";
            code = extras.getString("code");
            code = code != null ? code : "0";
            monthName = extras.getString("month_name");
            monthName = monthName != null ? monthName : "";
            pid = extras.getInt("pid", 0);
            flex1 = extras.getInt("flex1", 0);
            isFromDash = extras.getBoolean("isFromDash");
        } catch (Exception e) {
            Commons.printException(e);
        }

        setHasOptionsMenu(true);


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        dashboardRv = (RecyclerView) view.findViewById(R.id.dashboardRv);
        dashboardRv.setHasFixedSize(false);
        dashboardRv.setNestedScrollingEnabled(false);
        dashboardRv.setLayoutManager(new LinearLayoutManager(getActivity()));

        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        vpPager = (ViewPager) view.findViewById(R.id.viewpager);
        indicator = (CircleIndicator) view.findViewById(R.id.indicator);
        calledBy = from;

        new DownloadSKUWiseTarget().execute();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        customProgressDialog(builder, getResources().getString(R.string.loading_data));
        alertDialog = builder.create();

    }

    class DownloadSKUWiseTarget extends AsyncTask<Integer, Integer, Boolean> {
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                dashBoardHelper.findMinMaxProductLevel(rid);
                dashBoardHelper.downloadSKUWiseTarget(rid, type, code);
                dashBoardHelper.downloadDashboardLevelSkip(1);

                if ("DAY".equalsIgnoreCase(type))
                    dashBoardHelper.LoadSKUWiseTarget(rid);

            } catch (Exception e) {
                Commons.printException(e);
            }
            return Boolean.TRUE; // Return your real result here
        }

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(getActivity());

            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        protected void onPostExecute(Boolean result) {
            alertDialog.dismiss();
            mylist = dashBoardHelper.getSkuWiseTarget();
            dashBoardHelper.setSkuwiseGraphData(mylist);
            MyAdapter adapter = new MyAdapter(mylist);
            dashboardRv.setAdapter(adapter);
            adapterViewPager = new MyPagerAdapter(getActivity().getSupportFragmentManager());
            vpPager.setAdapter(adapterViewPager);
            indicator.setViewPager(vpPager);

        }
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private final List<SKUWiseTargetBO> items;

        public MyAdapter(List<SKUWiseTargetBO> items) {
            this.items = items;
        }

        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_skuwisetgt, parent, false);
            return new MyAdapter.ViewHolder(v);
        }


        @Override
        public void onBindViewHolder(final MyAdapter.ViewHolder holder, int position) {
            SKUWiseTargetBO product = items.get(position);

            holder.productbo = product;

            //typefaces
            holder.psname.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
            holder.target.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.acheived.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.index.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.incentiveTitle.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.incentive.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.targetTitle.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.acheivedTitle.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

            holder.psname.setText(holder.productbo.getProductShortName());


            if (flex1 == 1) {
                holder.target.setText(dashBoardHelper.getWhole(product.getTarget() + ""));
                String strCaluPercentage = bmodel.formatPercent(product.getCalculatedPercentage()) + "%";
                holder.index.setText(strCaluPercentage);
                holder.acheived.setText(dashBoardHelper.getWhole(product.getAchieved() + ""));
                holder.incentive.setText(dashBoardHelper.getWhole(product.getrField() + ""));
            } else {
                holder.target.setText(bmodel.formatValue(product.getTarget()));
                String strCalcPercentage = bmodel.formatPercent(product.getCalculatedPercentage()) + "%";
                holder.index.setText(strCalcPercentage);
                holder.acheived.setText(bmodel.formatValue(product.getAchieved()));
                holder.incentive.setText(bmodel.formatValue(product.getrField()));
            }

            ArrayList<PieEntry> entries = new ArrayList<PieEntry>();

            entries.add(new PieEntry((float) product.getConvAcheivedPercentage()));
            entries.add(new PieEntry((float) product.getConvTargetPercentage()));

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
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView psname;
            TextView target, targetTitle;
            TextView acheived, acheivedTitle;
            TextView incentive, incentiveTitle;
            TextView index;
            PieChart mChart;
            SKUWiseTargetBO productbo;
            View rowDotBlue, rowDotGreen, verticalSeparatorTarget;

            public ViewHolder(View row) {
                super(row);
                psname = (TextView) row
                        .findViewById(R.id.factorName_dashboard_tv);
                target = (TextView) row
                        .findViewById(R.id.target_dashboard_tv);
                acheived = (TextView) row
                        .findViewById(R.id.acheived_dashboard_tv);
                incentive = (TextView) row
                        .findViewById(R.id.initiative_dashboard_tv);

                index = (TextView) row
                        .findViewById(R.id.index_dashboard_tv);


                mChart = (PieChart) row
                        .findViewById(R.id.pieChart);

                targetTitle = (TextView) row
                        .findViewById(R.id.target_title);
                acheivedTitle = (TextView) row
                        .findViewById(R.id.achived_title);
                incentiveTitle = (TextView) row
                        .findViewById(R.id.incentive_title);

                rowDotBlue = (View) row
                        .findViewById(R.id.row_dot_blue);

                rowDotGreen = (View) row
                        .findViewById(R.id.row_dot_green);
                verticalSeparatorTarget = (View) row
                        .findViewById(R.id.verticalSeparatorTarget);

                if (!bmodel.configurationMasterHelper.SHOW_SKUWISE_INCENTIVE) {
                    incentive.setVisibility(View.GONE);
                    incentiveTitle.setVisibility(View.GONE);
                } else {
                    switch (type) {
                        case DAY_TYPE:
                            try {
                                if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                        R.id.incentive_title).getTag()) != null)
                                    ((TextView) row.findViewById(R.id.incentive_title))
                                            .setText(bmodel.labelsMasterHelper
                                                    .applyLabels(row.findViewById(R.id.incentive_title)
                                                            .getTag()));
                            } catch (Exception e) {
                                Commons.printException(e);
                            }
                            break;
                        case MONTH_TYPE:
                            try {
                                if (bmodel.labelsMasterHelper.applyLabels(MONTH_TAG) != null)
                                    ((TextView) row.findViewById(R.id.incentive_title))
                                            .setText(bmodel.labelsMasterHelper
                                                    .applyLabels(MONTH_TAG));
                                else
                                    ((TextView) row.findViewById(R.id.incentive_title))
                                            .setText(getResources().getString(R.string.incentive));
                            } catch (Exception e) {
                                Commons.printException(e);
                            }
                            break;
                        case YEAR_TYPE:
                            try {
                                if (bmodel.labelsMasterHelper.applyLabels(YEAR_TAG) != null)
                                    ((TextView) row.findViewById(R.id.incentive_title))
                                            .setText(bmodel.labelsMasterHelper
                                                    .applyLabels(YEAR_TAG));
                                else
                                    ((TextView) row.findViewById(R.id.incentive_title))
                                            .setText(getResources().getString(R.string.incentive));
                            } catch (Exception e) {
                                Commons.printException(e);
                            }
                            break;
                        default:
                            ((TextView) row.findViewById(R.id.incentive_title))
                                    .setText(getResources().getString(R.string.incentive));
                            break;
                    }
                }
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


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_inventory, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_location_filter).setVisible(false);
        menu.findItem(R.id.menu_remarks).setVisible(false);
        menu.findItem(R.id.menu_spl_filter).setVisible(false);
        menu.findItem(R.id.menu_next).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackButtonClick();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onBackButtonClick() {

        if ("1".equals(calledBy)) {
            startActivity(new Intent(getActivity(),
                    HomeScreenActivity.class));
            getActivity().finish();
        }

        // HOME SCREEN TWO
        if ("2".equals(calledBy)) {
            bmodel.getRetailerMasterBO().setIsSKUTGT("Y");
            startActivity(new Intent(getActivity(),
                    HomeScreenTwo.class));
            getActivity().finish();
        }
        // DashBoardActivity
        if ("4".equals(calledBy)) {
         /*   Intent i = new Intent(SKUWiseTargetActivity.this,
                    DashBoardActivity.class);
            i.putExtra("from", "1");
            i.putExtra("retid", rid);
            i.putExtra("screentitle", screentitlebk);
            i.putExtra("type", type);
            startActivity(i);*/
            getActivity().finish();
        }
        getActivity().finish();
        getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        try {


            if (fm != null)
                fm = null;
        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }


    public class MyPagerAdapter extends FragmentPagerAdapter {
        private int NUM_ITEMS = 3;

        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: // Fragment # 0 - This will show FirstFragment
                    return new PieChartFragement();
                case 1: // Fragment # 0 - This will show FirstFragment different title
                    Fragment fragment = new TotalAchivedFragment();
                    Bundle args = new Bundle();
                    args.putInt("flex1", flex1);
                    fragment.setArguments(args);
                    return fragment;
                case 2: // Fragment # 0 - This will show FirstFragment different title
                    return new HalfPieChartFragement();
                default:
                    return null;
            }
        }

    }


}
