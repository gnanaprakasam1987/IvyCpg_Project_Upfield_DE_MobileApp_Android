package com.ivy.cpg.view.supervisor.mvp.sellerperformance.sellerperformancedetail;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.ivy.cpg.view.supervisor.mvp.SellerBo;
import com.ivy.lib.DialogFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.FontUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.graphics.Color.rgb;

public class SellerPerformanceDetailActivity extends IvyBaseActivityNoActionBar implements
        SellerPerformanceDetailContractor.SellerPerformanceDetailView {

    private SellerPerformanceDetailPresenter sellerPerformancePresenter;
    private TextView sellerNameTv,sellerPerformPercentTv,valueTargetTv,valueActualTv,valuePercentTv,
            coverageTargetTv,coverageActualtv,coveragePercenttv,linesTargetTv,linesActualTv,linesPercentTv,
            plannedValueTv,deviatedTv,durationTv,productiveTv;

    private ProgressBar progressBar;

    private TabLayout tabLayout;

    private int sellerId=0;
    private String selectedDate="";

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


        if(getIntent().getExtras() != null) {
            sellerId = getIntent().getExtras().getInt("SellerId");
            selectedDate = getIntent().getExtras().getString("Date");
        }

        sellerPerformancePresenter = new SellerPerformanceDetailPresenter();

        sellerPerformancePresenter.setDetailView(this,SellerPerformanceDetailActivity.this);

        sellerPerformancePresenter.checkDownloadSelerKPIData(sellerId,convertPlaneDateToGlobal(selectedDate));

        findViewById(R.id.bottom_outlet_btn_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                OutletPagerDialogFragment outletPagerDialogFragment = new OutletPagerDialogFragment(sellerPerformancePresenter);
                outletPagerDialogFragment.setStyle(DialogFragment.STYLE_NO_FRAME, 0);
                outletPagerDialogFragment.setCancelable(false);
                outletPagerDialogFragment.show(getSupportFragmentManager(),"OutletPager");
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
        TextView sellerPositionTv = findViewById(R.id.seller_position);
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

        progressBar = findViewById(R.id.progressBar);

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

        tabLayout = findViewById(R.id.tab_layout);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                switch (tab.getPosition()) {
                    case 0:
                        sellerPerformancePresenter.downloadSellerKPI(sellerId,selectedDate,false);
                        break;
                    case 1:
                        sellerPerformancePresenter.downloadSellerKPI(sellerId,selectedDate,true);
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

    public void initializeMethods(){

        sellerPerformancePresenter.downloadSellerData(sellerId,convertPlaneDateToGlobal(selectedDate));

        sellerPerformancePresenter.setSellerActivityListener(sellerId,selectedDate);

        sellerPerformancePresenter.downloadSellerKPI(sellerId,convertPlaneDateToGlobal(selectedDate),false);

        sellerPerformancePresenter.downloadSellerOutletAWS(sellerId,convertPlaneDateToGlobal(selectedDate));

        sellerPerformancePresenter.setSellerActivityDetailListener(sellerId,selectedDate);
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

    @Override
    public void updateSellerPerformanceData(SellerBo sellerBo) {

        sellerNameTv.setText(sellerBo.getUserName());

        int target = sellerBo.getTarget();
        int billed = sellerBo.getBilled();
        int sellerProductive = 0;

        if (target != 0) {
            sellerProductive = (int)((float)billed / (float)target * 100);
        }

        progressBar.setProgress(sellerProductive);

        sellerPerformPercentTv.setText(sellerProductive+"%");
    }

    @Override
    public void updateChartInfo(){
        combinedChart();
    }

    @Override
    public void updateSellerTabViewInfo(SellerBo sellerBo) {
        coverageTargetTv.setText(String.valueOf(sellerBo.getTargetCoverage()));
        linesTargetTv.setText(String.valueOf(sellerBo.getTargetLines()));
        valueTargetTv.setText(String.valueOf(sellerBo.getTargetValue()));

        int covered,lines;
        long orderValue;

        if(tabLayout.getSelectedTabPosition() == 1){
            covered = sellerBo.getAchievedCoverage();
            orderValue = sellerBo.getAchievedValue();
            lines = sellerBo.getAchievedLines();
        }else{
            covered = sellerBo.getCovered();
            orderValue = sellerBo.getTotalOrderValue();
            lines = sellerBo.getLpc();
        }

        coverageActualtv.setText(String.valueOf(covered));
        valueActualTv.setText(String.valueOf(orderValue));
        linesActualTv.setText(String.valueOf(lines));

        if (sellerBo.getTargetCoverage() != 0) {
            int coverPercent = (int)((float)covered / (float)sellerBo.getTargetCoverage() * 100);
            coveragePercenttv.setText(coverPercent+"%");
        }

        if (sellerBo.getTargetValue() != 0) {
            int orderPercent = (int)((float)orderValue / (float)sellerBo.getTargetValue() * 100);
            valuePercentTv.setText(orderPercent+"%");
        }

        if (sellerBo.getTargetLines() != 0) {
            int linePercent = (int)((float)lines / (float)sellerBo.getTargetLines() * 100);
            linesPercentTv.setText(linePercent+"%");
        }


        plannedValueTv.setText(String.valueOf(sellerBo.getTarget()));
        deviatedTv.setText("0");
        durationTv.setText("0");
        productiveTv.setText(String.valueOf(sellerBo.getBilled()));
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
        rightAxis.setEnabled(false);


        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)
        leftAxis.setEnabled(false);
        leftAxis.setDrawAxisLine(false);


        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMinimum(0f);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(true);
        xAxis.setDrawAxisLine(true);
        xAxis.setTextColor(ContextCompat.getColor(this,R.color.WHITE));
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return sellerPerformancePresenter.getChartDaysStr().get((int)value % sellerPerformancePresenter.getChartDaysStr().size());
            }
        });


        CombinedData data = new CombinedData();

        data.setData(generateLineData());
//        data.setData(generateBarData());

        xAxis.setAxisMaximum(data.getXMax() + 0.25f);

        mChart.getAxisRight().setDrawLabels(false);
        mChart.getAxisLeft().setTextColor(ContextCompat.getColor(this,R.color.WHITE));

        mChart.setData(data);
        mChart.invalidate();
    }

    private LineData generateLineData() {

        LineData d = new LineData();

        LineDataSet set = new LineDataSet(sellerPerformancePresenter.getSellerCoveredEntry(), "Covered");
        set.setColor((ContextCompat.getColor(this,R.color.colorPrimary)));
        set.setLineWidth(2.5f);
        set.setCircleColor(rgb(240, 238, 70));
        set.setCircleRadius(5f);
        set.setFillColor(rgb(240, 238, 70));
        set.setMode(LineDataSet.Mode.LINEAR);
        set.setDrawValues(true);
        set.setValueTextSize(10f);
        set.setValueTextColor((ContextCompat.getColor(this,R.color.WHITE)));

        LineDataSet set1 = new LineDataSet(sellerPerformancePresenter.getSellerBilledEntry(), "Productivity");
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

    private String convertPlaneDateToGlobal(String planeDate){
        try {

            SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyy", Locale.ENGLISH);
            Date date = sdf.parse(planeDate);

            sdf = new SimpleDateFormat("yyyy/MM/dd",Locale.ENGLISH);
            planeDate =sdf.format(date);

            return planeDate;

        }catch(Exception e){
            Commons.printException(e);
        }

        return planeDate;
    }
}
