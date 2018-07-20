package com.ivy.cpg.view.supervisor.mvp.sellerperformance;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.ivy.cpg.view.supervisor.fragments.OutletPagerDialogFragment;
import com.ivy.lib.DialogFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;

import static android.graphics.Color.rgb;

public class SellerPerformanceDetailActivity extends IvyBaseActivityNoActionBar {

    SellerPerformanceHelper sellerPerformanceHelper;
    private String[] mMonths;
    private TextView sellerNameTv,sellerPositionTv,sellerPerformPercentTv,valueTargetTv,valueActualTv,valuePercentTv,
            coverageTargetTv,coverageActualtv,coveragePercenttv,linesTargetTv,linesActualTv,linesPercentTv,
            plannedValueTv,deviatedTv,durationTv,productiveTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_performance_detail);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

        Toolbar toolbar = findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        setScreenTitle("Seller Performance");
        initViews();

        sellerPerformanceHelper = new SellerPerformanceHelper();
        mMonths = sellerPerformanceHelper.getSellerPerformanceList();

        combinedChart();

        findViewById(R.id.bottom_outlet_btn_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                OutletPagerDialogFragment outletPagerDialogFragment = new OutletPagerDialogFragment();
//                outletPagerDialogFragment.setStyle(DialogFragment.STYLE_NO_FRAME, 0);
//                outletPagerDialogFragment.setCancelable(false);
//                outletPagerDialogFragment.show(getSupportFragmentManager(),"OutletPager");
            }
        });
    }

    private void initViews(){

        ((TextView)findViewById(R.id.number_text)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM,this));
        ((TextView)findViewById(R.id.target_text)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,this));
        ((TextView)findViewById(R.id.actual_text)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,this));
        ((TextView)findViewById(R.id.percent_text)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM,this));
        ((TextView)findViewById(R.id.value_text)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,this));
        ((TextView)findViewById(R.id.coverage_text)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,this));
        ((TextView)findViewById(R.id.lines_text)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,this));
        ((TextView)findViewById(R.id.calls_status_text)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM,this));
        ((TextView)findViewById(R.id.planned_text)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,this));
        ((TextView)findViewById(R.id.deviated_text)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,this));
        ((TextView)findViewById(R.id.duration_text)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,this));
        ((TextView)findViewById(R.id.productive_txt)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,this));
        ((TextView)findViewById(R.id.seller_performance_btn)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,this));

        sellerNameTv = findViewById(R.id.seller_name);
        sellerPositionTv = findViewById(R.id.seller_position);
        sellerPerformPercentTv = findViewById(R.id.seller_perform_percent);

        valueTargetTv = findViewById(R.id.value_target);
        valueActualTv = findViewById(R.id.value_actual);
        valuePercentTv = findViewById(R.id.value_percent);
        coverageTargetTv = findViewById(R.id.coverage_target);
        coverageActualtv = findViewById(R.id.coverage_actual);
        coveragePercenttv = findViewById(R.id.coverage_percent);
        linesTargetTv = findViewById(R.id.lines_target);
        linesActualTv = findViewById(R.id.lines_actual);
        linesPercentTv = findViewById(R.id.lines_percent);
        plannedValueTv = findViewById(R.id.planned_value);
        deviatedTv = findViewById(R.id.deviated_value);
        durationTv = findViewById(R.id.duration_value);
        productiveTv = findViewById(R.id.productive_value);

        sellerNameTv.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,this));
        sellerPositionTv.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,this));
        sellerPerformPercentTv.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,this));
        valueTargetTv.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,this));
        valueActualTv.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,this));
        valuePercentTv.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,this));
        coverageTargetTv.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,this));
        coverageActualtv.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,this));
        coveragePercenttv.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,this));
        linesTargetTv.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,this));
        linesActualTv.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,this));
        linesPercentTv.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,this));
        plannedValueTv.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM,this));
        deviatedTv.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM,this));
        durationTv.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM,this));
        productiveTv.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM,this));

        TabLayout tabLayout = findViewById(R.id.tab_layout);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                switch (tab.getPosition()) {
                    case 0:
                        break;
                    case 1:
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        changeTabsFont(tabLayout);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void combinedChart(){
        CombinedChart mChart = findViewById(R.id.combined_chart);
        mChart.getDescription().setEnabled(false);
        mChart.setDrawGridBackground(false);
        mChart.setDrawBarShadow(false);
        mChart.setHighlightFullBarEnabled(false);

        // draw bars behind lines
        mChart.setDrawOrder(new CombinedChart.DrawOrder[]{
                CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.BUBBLE, CombinedChart.DrawOrder.CANDLE, CombinedChart.DrawOrder.LINE, CombinedChart.DrawOrder.SCATTER
        });

        Legend l = mChart.getLegend();
        l.setWordWrapEnabled(true);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)
//        rightAxis.setPosition(YAxis.YAxisLabelPosition.);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMinimum(0f);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(ContextCompat.getColor(this,R.color.WHITE));
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return mMonths[(int) value % mMonths.length];
            }
        });

        CombinedData data = new CombinedData();

        data.setData(generateLineData());
        data.setData(generateBarData());

        xAxis.setAxisMaximum(data.getXMax() + 0.25f);

        mChart.getAxisRight().setDrawLabels(false);
        mChart.getAxisLeft().setTextColor(ContextCompat.getColor(this,R.color.WHITE));

        mChart.setData(data);
        mChart.invalidate();
    }

    private LineData generateLineData() {

        LineData d = new LineData();

        ArrayList<Entry> entries1 = new ArrayList<>();
        ArrayList<Entry> entries2 = new ArrayList<>();

        entries1.add(new Entry(2f,10f));
        entries1.add(new Entry(4f,8f));
        entries1.add(new Entry(6f,2f));
        entries1.add(new Entry(8f,6f));

        entries2.add(new Entry(1f,20f));
        entries2.add(new Entry(3f,32f));
        entries2.add(new Entry(8f,2f));
        entries2.add(new Entry(10f,6f));


        LineDataSet set = new LineDataSet(entries1, "Covered");
        set.setColor((ContextCompat.getColor(this,R.color.colorPrimary)));
        set.setLineWidth(2.5f);
        set.setCircleColor(rgb(240, 238, 70));
        set.setCircleRadius(5f);
        set.setFillColor(rgb(240, 238, 70));
        set.setMode(LineDataSet.Mode.LINEAR);
        set.setDrawValues(true);
        set.setValueTextSize(10f);
        set.setValueTextColor((ContextCompat.getColor(this,R.color.WHITE)));

        LineDataSet set1 = new LineDataSet(entries2, "Productivity");
        set1.setColor((ContextCompat.getColor(this,R.color.GREEN)));
        set1.setLineWidth(2.5f);
        set1.setCircleColor(rgb(240, 238, 70));
        set1.setCircleRadius(5f);
        set1.setFillColor(rgb(240, 238, 70));
        set1.setMode(LineDataSet.Mode.LINEAR);
        set1.setDrawValues(true);
        set1.setValueTextSize(10f);
        set1.setValueTextColor((ContextCompat.getColor(this,R.color.WHITE)));

        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);

        d.addDataSet(set);
        d.addDataSet(set1);

        return d;
    }

    private BarData generateBarData() {

        BarDataSet set1 = new BarDataSet(sellerPerformanceHelper.getBarEntries(), "Bar 1");
        set1.setColor(ContextCompat.getColor(this,R.color.white_trans));
        set1.setValueTextColor((ContextCompat.getColor(this,R.color.white_trans)));
        set1.setValueTextSize(10f);
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);


        float barWidth = 0.45f; // x2 dataset
        // (0.45 + 0.02) * 2 + 0.06 = 1.00 -> interval per "group"

        BarData d = new BarData(set1);
        d.setBarWidth(barWidth);

        // make this BarData object grouped
//        d.groupBars(0, groupSpace, barSpace); // start at x = 0

        return d;
    }

    private void changeTabsFont(TabLayout tabLayout) {

        ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
        int tabsCount = vg.getChildCount();
        for (int j = 0; j < tabsCount; j++) {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
            int tabChildCount = vgTab.getChildCount();
            for (int i = 0; i < tabChildCount; i++) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    ((TextView) tabViewChild).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM,this));
                }
            }
        }
    }
}
