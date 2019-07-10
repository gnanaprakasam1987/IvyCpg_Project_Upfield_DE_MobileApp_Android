package com.ivy.cpg.view.dashboard.sellerdashboard;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.ivy.cpg.view.dashboard.DashBoardHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.SKUWiseTargetBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.HalfPieChartFragement;
import com.ivy.sd.png.view.PieChartFragement;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import me.relex.circleindicator.CircleIndicator;

public class SellerKpiSkuFragment extends IvyBaseFragment {

    private FragmentManager fm;
    private View view;
    private Vector<SKUWiseTargetBO> mylist;
    private BusinessModel bmodel;
    private int flex1;
    private LinearLayout ll;
    private RecyclerView rvwplist;
    private boolean isFromDash;
    private Button previous;
    private TextView textview[] = null;
    private int curSeq = 0;
    private int mSelectedProductId = 0;
    ViewPager vpPager;
    CircleIndicator indicator;
    MyPagerAdapter adapterViewPager;
    HorizontalScrollView scr_View;
    private DashBoardHelper dashBoardHelper;
    private String dashCode;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_sellers_dash_sku, container, false);


        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        fm = getActivity().getSupportFragmentManager();

        Intent i = getActivity().getIntent();
        flex1 = i.getIntExtra("flex1", 0);
        isFromDash = i.getExtras().getBoolean("isFromDash");
        dashCode = i.getExtras().getString("dashCode", "");

        setHasOptionsMenu(true);

        vpPager = (ViewPager) view.findViewById(R.id.viewpager);
        indicator = (CircleIndicator) view.findViewById(R.id.indicator);
        previous = (Button) view.findViewById(R.id.previousBTN);
        ll = (LinearLayout) view.findViewById(R.id.ll);
        rvwplist = (RecyclerView) view.findViewById(R.id.rvwplist);
        rvwplist.setHasFixedSize(false);
        rvwplist.setNestedScrollingEnabled(false);
        rvwplist.setLayoutManager(new LinearLayoutManager(getActivity()));

        scr_View = (HorizontalScrollView) view.findViewById(R.id.scr_View);
        LinearLayout.LayoutParams weight1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        weight1.weight = 1;
        weight1.gravity = Gravity.CENTER;

        LinearLayout.LayoutParams weight2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        weight2.weight = 2;
        weight2.gravity = Gravity.CENTER;

        textview = new TextView[100];
        mylist = dashBoardHelper.getSellerKpiSku();
        updateList(dashBoardHelper.mSellerKpiMinSeqLevel);

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateList(curSeq - 1);
            }
        });


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        dashBoardHelper = DashBoardHelper.getInstance(getActivity());
    }


    private void updateList(int bid) {
        if (mylist == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.no_products_exists), 0);
            return;
        }
        int siz = mylist.size();
        ArrayList<SKUWiseTargetBO> temp = new ArrayList<>();
        if (dashBoardHelper.getKpiSkuMasterBoById(new Integer(mSelectedProductId)) != 0)
            mSelectedProductId = dashBoardHelper.getKpiSkuMasterBoById(new Integer(mSelectedProductId));
        for (int i = 0; i < siz; ++i) {
            SKUWiseTargetBO ret = mylist.get(i);
            if (bid != dashBoardHelper.mSellerKpiMinSeqLevel && bmodel.configurationMasterHelper.SHOW_NOR_DASHBOARD) {
                if (ret.getSequence() == bid && ret.getParentID() == mSelectedProductId) {
                    temp.add(ret);
                }
            } else {
                if (ret.getSequence() == bid) {
                    temp.add(ret);
                }
            }
        }

        if (bid == dashBoardHelper.mSellerKpiMinSeqLevel)
            previous.setVisibility(View.GONE);

        if (curSeq != dashBoardHelper.mSellerKpiMinSeqLevel && textview[curSeq] != null) {

            textview[curSeq].setText("");
            ll.removeView(textview[curSeq]);
        }

        curSeq = bid;

        MyAdapter mSchedule = new MyAdapter(temp);
        rvwplist.setAdapter(mSchedule);
        dashBoardHelper.setSkuwiseGraphData(temp);
        adapterViewPager = new MyPagerAdapter(getChildFragmentManager());
        vpPager.setAdapter(adapterViewPager);
        indicator.setViewPager(vpPager);

    }

    private void updateNextList(int parentID, String pname) {
        // Close the drawer
        if (mylist == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.no_products_exists), 0);
            return;
        }
        int siz = mylist.size();
        ArrayList<SKUWiseTargetBO> temp = new ArrayList<>();
        for (int i = 0; i < siz; ++i) {

            SKUWiseTargetBO ret = mylist.get(i);
            if (ret.getParentID() == parentID) {
                temp.add(ret);
                curSeq = ret.getSequence();
            }
        }
        if (!temp.isEmpty()) {
            MyAdapter mSchedule = new MyAdapter(temp);
            rvwplist.setAdapter(mSchedule);
            dashBoardHelper.setSkuwiseGraphData(temp);
            adapterViewPager = new MyPagerAdapter(getChildFragmentManager());
            vpPager.setAdapter(adapterViewPager);
            indicator.setViewPager(vpPager);
            if (curSeq != dashBoardHelper.mSellerKpiMinSeqLevel)
                previous.setVisibility(View.VISIBLE);
            ll.addView(getTextView(curSeq, parentID, pname));
            //Anand Asir
            //For scrolling to the Right
            scr_View.post(new Runnable() {
                public void run() {
                    scr_View.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
                }
            });
        } else {
            Toast.makeText(getActivity(),
                    "No  data to Show",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private TextView getTextView(final int mNumber
            , int pid, String textname) {
        textview[mNumber] = new TextView(getActivity());
        textview[mNumber].setClickable(true);
        textview[mNumber].setId(pid);
        String strText = textname + "  >  ";
        textview[mNumber].setText(strText);
        textview[mNumber].setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.font_small));
        textview[mNumber].setPadding(0, 20, 0, 20);
        return textview[mNumber];
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private final List<SKUWiseTargetBO> items;

        public MyAdapter(List<SKUWiseTargetBO> items) {
            this.items = items;
        }

        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v;
            if (bmodel.configurationMasterHelper.IS_SWITCH_WITH_OUT_SKU_WISE_TGT
                    && dashCode.length() > 0
                    && (bmodel.configurationMasterHelper.SELLER_SKU_WISE_KPI_CODES.contains(dashCode)
                    || items.get(viewType).getTarget() == 0)) {
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_sellerskuwise_without_tgt, parent, false);
            } else {
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_sellerskuwisetgt, parent, false);
            }

            return new MyAdapter.ViewHolder(v);
        }


        @Override
        public void onBindViewHolder(final MyAdapter.ViewHolder holder, int position) {
            SKUWiseTargetBO product = items.get(position);

            holder.productbo = product;
            mSelectedProductId = holder.productbo.getParentID();

            //typefaces
            holder.psname.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
            holder.target.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.acheived.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.index.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.targetTitle.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.acheivedTitle.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

            if (holder.productbo.getProductShortName().equals(""))
                holder.psname.setText(holder.productbo.getProductName());
            else
                holder.psname.setText(holder.productbo.getProductShortName());

            holder.psname.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    updateNextList(holder.productbo.getPid(), holder.productbo.getProductShortName());
                }
            });

            if (flex1 == 1) {
                holder.target.setText(dashBoardHelper.getWhole(product.getTarget() + ""));
                String strCaluPercentage = bmodel.formatPercent(product.getCalculatedPercentage()) + "%";
                holder.index.setText(strCaluPercentage);
                holder.acheived.setText(dashBoardHelper.getWhole(product.getAchieved() + ""));
            } else {
                holder.target.setText(bmodel.formatValue(product.getTarget()));
                String strCalcPercentage = bmodel.formatPercent(product.getCalculatedPercentage()) + "%";
                holder.index.setText(strCalcPercentage);
                holder.acheived.setText(bmodel.formatValue(product.getAchieved()));
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

                index = (TextView) row
                        .findViewById(R.id.index_dashboard_tv);


                mChart = (PieChart) row
                        .findViewById(R.id.pieChart);

                targetTitle = (TextView) row
                        .findViewById(R.id.target_title);
                acheivedTitle = (TextView) row
                        .findViewById(R.id.achived_title);

                rowDotBlue = (View) row
                        .findViewById(R.id.row_dot_blue);

                rowDotGreen = (View) row
                        .findViewById(R.id.row_dot_green);
                verticalSeparatorTarget = (View) row
                        .findViewById(R.id.verticalSeparatorTarget);
                try {
                    if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                            R.id.target_title).getTag()) != null)
                        ((TextView) row.findViewById(R.id.target_title))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(row.findViewById(R.id.target_title)
                                                .getTag()));


                    if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                            R.id.achived_title).getTag()) != null)
                        ((TextView) row.findViewById(R.id.achived_title))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(row.findViewById(R.id.achived_title)
                                                .getTag()));

                } catch (Exception e) {
                    e.printStackTrace();
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

        getActivity().finish();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        updateList(curSeq - 1);
        Toast.makeText(getActivity(),
                "" + textview[curSeq].getText() + " Id>>" + textview[curSeq].getId(),
                Toast.LENGTH_SHORT).show();
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
