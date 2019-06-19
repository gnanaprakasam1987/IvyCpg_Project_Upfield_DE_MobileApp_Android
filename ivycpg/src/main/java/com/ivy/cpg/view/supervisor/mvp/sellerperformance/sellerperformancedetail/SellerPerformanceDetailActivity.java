package com.ivy.cpg.view.supervisor.mvp.sellerperformance.sellerperformancedetail;

import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.ivy.cpg.view.supervisor.mvp.models.SellerBo;
import com.ivy.lib.DialogFragment;
import com.ivy.lib.Utils;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.FontUtils;

import java.io.File;

import static android.graphics.Color.rgb;

public class SellerPerformanceDetailActivity extends IvyBaseActivityNoActionBar implements
        SellerPerformanceDetailContractor.SellerPerformanceDetailView {

    private SellerPerformanceDetailPresenter sellerPerformancePresenter;
    private TextView sellerNameTv,sellerPerformPercentTv,valueTargetTv,valueActualTv,valuePercentTv,
            coverageTargetTv,coverageActualtv,coveragePercenttv,linesTargetTv,linesActualTv,linesPercentTv,
            plannedValueTv,deviatedTv,durationTv,productiveTv;
    private ImageView userImage;

    private ProgressBar progressBar;

    private TabLayout tabLayout;

    private int sellerId=0;
    private String selectedDate="";
    private CombinedChart mChart;

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

        sellerPerformancePresenter.checkDownloadSelerKPIData(sellerId,sellerPerformancePresenter.convertPlaneDateToGlobal(selectedDate));

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

        ((TextView)findViewById(R.id.number_text)).setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.MEDIUM));
        ((TextView)findViewById(R.id.target_text)).setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.REGULAR));
        ((TextView)findViewById(R.id.actual_text)).setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.REGULAR));
        ((TextView)findViewById(R.id.percent_text)).setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.MEDIUM));
        ((TextView)findViewById(R.id.value_text)).setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.REGULAR));
        ((TextView)findViewById(R.id.coverage_text)).setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.REGULAR));
        ((TextView)findViewById(R.id.lines_text)).setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.REGULAR));
        ((TextView)findViewById(R.id.calls_status_text)).setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.MEDIUM));
        ((TextView)findViewById(R.id.planned_text)).setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.REGULAR));
        ((TextView)findViewById(R.id.deviated_text)).setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.REGULAR));
        ((TextView)findViewById(R.id.duration_text)).setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.REGULAR));
        ((TextView)findViewById(R.id.productive_txt)).setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.REGULAR));
        ((TextView)findViewById(R.id.seller_performance_btn)).setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.REGULAR));

        sellerNameTv = findViewById(R.id.seller_name);
        TextView sellerPositionTv = findViewById(R.id.seller_position);
        sellerPerformPercentTv = findViewById(R.id.seller_perform_percent);
        userImage = findViewById(R.id.user_image);

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

        sellerNameTv.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.REGULAR));
        sellerPositionTv.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.REGULAR));
        sellerPerformPercentTv.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.REGULAR));
        valueTargetTv.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.REGULAR));
        valueActualTv.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.REGULAR));
        valuePercentTv.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.REGULAR));
        coverageTargetTv.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.REGULAR));
        coverageActualtv.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.REGULAR));
        coveragePercenttv.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.REGULAR));
        linesTargetTv.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.REGULAR));
        linesActualTv.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.REGULAR));
        linesPercentTv.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.REGULAR));
        plannedValueTv.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.MEDIUM));
        deviatedTv.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.MEDIUM));
        durationTv.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.MEDIUM));
        productiveTv.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.MEDIUM));

        mChart = findViewById(R.id.combined_chart);
        mChart.setNoDataText("Loading...");
        mChart.setNoDataTextColor(ContextCompat.getColor(this,R.color.white));
        mChart.animateXY(2500,2500,
                Easing.EasingOption.EaseInBack, Easing.EasingOption.EaseOutBack);

        tabLayout = findViewById(R.id.tab_layout);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                coveragePercenttv.setText("0%");
                valuePercentTv.setText("0%");
                linesPercentTv.setText("0%");

                switch (tab.getPosition()) {
                    case 0:
                        sellerPerformancePresenter.downloadSellerKPI(sellerId,sellerPerformancePresenter.convertPlaneDateToGlobal(selectedDate),false);
                        break;
                    case 1:
                        sellerPerformancePresenter.downloadSellerKPI(sellerId,sellerPerformancePresenter.convertPlaneDateToGlobal(selectedDate),true);
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

        sellerPerformancePresenter.downloadSellerData(sellerId,sellerPerformancePresenter.convertPlaneDateToGlobal(selectedDate));

        sellerPerformancePresenter.setSellerActivityListener(sellerId,selectedDate);

        sellerPerformancePresenter.downloadSellerKPI(sellerId,sellerPerformancePresenter.convertPlaneDateToGlobal(selectedDate),false);

        sellerPerformancePresenter.downloadSellerOutletAWS(sellerId,sellerPerformancePresenter.convertPlaneDateToGlobal(selectedDate));

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
                    ((TextView) tabViewChild).setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.MEDIUM));
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

        if (sellerProductive > 100)
            sellerProductive = 100;

        progressBar.setProgress(sellerProductive);

        sellerPerformPercentTv.setText(sellerProductive+"%");

        setProfileImage(sellerBo.getImagePath(),sellerBo.getUserId());
    }

    @Override
    public void updateChartInfo(){
        combinedChart();
    }

    @Override
    public void updateSellerTabViewInfo(SellerBo sellerBo) {
        coverageTargetTv.setText(String.valueOf(sellerBo.getTargetCoverage()));
        linesTargetTv.setText(String.valueOf(sellerBo.getTargetLines()));
        valueTargetTv.setText(Utils.formatAsTwoDecimal((double) sellerBo.getTargetValue()));

        int covered,lines;
        double orderValue;

        if(tabLayout.getSelectedTabPosition() == 1){
            covered = sellerBo.getAchievedCoverage();
            orderValue = sellerBo.getAchievedValue();
            lines = sellerBo.getAchievedLines();
        }else{
            covered = sellerBo.getCovered();
            orderValue = sellerBo.getTotalOrderValue();
            lines = sellerBo.getTotallpc();
        }

        coverageActualtv.setText(String.valueOf(covered));
        valueActualTv.setText(Utils.formatAsTwoDecimal(orderValue));
        linesActualTv.setText(String.valueOf(lines));

        if (sellerBo.getTargetCoverage() != 0) {
            int coverPercent = (int)((float)covered / (float)sellerBo.getTargetCoverage() * 100);
            coveragePercenttv.setText((coverPercent>100?100:coverPercent)+"%");
        }

        if (sellerBo.getTargetValue() != 0) {
            int orderPercent = (int)((float)orderValue / (float)sellerBo.getTargetValue() * 100);
            valuePercentTv.setText((orderPercent>100?100:orderPercent)+"%");
        }

        if (sellerBo.getTargetLines() != 0) {
            int linePercent = (int)((float)lines / (float)sellerBo.getTargetLines() * 100);
            linesPercentTv.setText((linePercent>100?100:linePercent)+"%");
        }


        plannedValueTv.setText(String.valueOf(sellerBo.getTarget()));
        productiveTv.setText(String.valueOf(sellerBo.getBilled()));
    }

    @Override
    public void updateSellerCallInfo(SellerBo sellerBo){
        deviatedTv.setText(String.valueOf(sellerBo.getDeviationCount()));

        String hms = sellerPerformancePresenter.convertSecondsToHMmSs(sellerBo.getTotalCallDuration());

        durationTv.setText(hms);
    }

    private void combinedChart(){
        mChart.getDescription().setEnabled(false);
        mChart.setDrawGridBackground(false);
        mChart.setDrawBarShadow(false);
        mChart.setHighlightFullBarEnabled(false);

        if (sellerPerformancePresenter.getChartDaysStr().size() == 0)
            mChart.setNoDataText("No chart data available");

        mChart.setNoDataTextColor(ContextCompat.getColor(this,R.color.white));

        // draw bars behind lines
        mChart.setDrawOrder(new CombinedChart.DrawOrder[]{
                CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.BUBBLE, CombinedChart.DrawOrder.CANDLE, CombinedChart.DrawOrder.LINE, CombinedChart.DrawOrder.SCATTER
        });


        Legend l = mChart.getLegend();
        l.setWordWrapEnabled(true);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setTextColor(ContextCompat.getColor(this,R.color.white));
        l.setTextSize(14f);
        l.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.REGULAR));
        l.setDrawInside(false);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setAxisMinimum(0f);
        rightAxis.setDrawAxisLine(false);
        rightAxis.setEnabled(true);
        rightAxis.setDrawGridLines(true);
        rightAxis.setGridColor(ContextCompat.getColor(this,R.color.chart_horizontal_line_color));
        rightAxis.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.REGULAR));

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)
        leftAxis.setEnabled(true);
        leftAxis.setDrawAxisLine(true);
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisLineColor(ContextCompat.getColor(this,R.color.chart_horizontal_line_color));
        leftAxis.setGridColor(ContextCompat.getColor(this,R.color.chart_horizontal_line_color));
        leftAxis.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.REGULAR));

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMinimum(0f);
        xAxis.setGranularity(1f);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);
        xAxis.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.REGULAR));
        xAxis.setAxisLineColor(ContextCompat.getColor(this,R.color.chart_horizontal_line_color));

        xAxis.setTextColor(ContextCompat.getColor(this,R.color.white));
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
        mChart.getAxisLeft().setTextColor(ContextCompat.getColor(this,R.color.white));

        mChart.setData(data);
        mChart.invalidate();
    }

    private LineData generateLineData() {

        LineData d = new LineData();

        LineDataSet set = new LineDataSet(sellerPerformancePresenter.getSellerCoveredEntry(), "Covered");
        set.setColor((ContextCompat.getColor(this,R.color.chart_covered_line)));
        set.setLineWidth(4f);
        set.setCircleColor(ContextCompat.getColor(this,R.color.white));
        set.setCircleColorHole(ContextCompat.getColor(this,R.color.chart_point_circle));
        set.setCircleRadius(6f);
        set.setCircleHoleRadius(4f);
        set.setMode(LineDataSet.Mode.LINEAR);
        set.setDrawValues(true);
        set.setValueTextSize(12f);
        set.setValueTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.REGULAR));
        set.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {

                return String.valueOf((int)value);
            }
        });
        set.setValueTextColor((ContextCompat.getColor(this,R.color.white)));

        LineDataSet set1 = new LineDataSet(sellerPerformancePresenter.getSellerBilledEntry(), "Productivity");
        set1.setColor(ContextCompat.getColor(this,R.color.chart_productivity_line));
        set1.setLineWidth(4f);
        set1.setCircleColor(ContextCompat.getColor(this,R.color.white));
        set1.setCircleColorHole(ContextCompat.getColor(this,R.color.chart_point_circle));
        set1.setCircleRadius(6f);
        set1.setCircleHoleRadius(4f);
        set1.setMode(LineDataSet.Mode.LINEAR);
        set1.setDrawValues(true);
        set1.setValueTextSize(12f);
        set1.setValueTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.REGULAR));
        set1.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {

                return String.valueOf((int)value);
            }
        });
        set1.setValueTextColor((ContextCompat.getColor(this,R.color.white)));

        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);

        d.addDataSet(set);
        d.addDataSet(set1);

        return d;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sellerPerformancePresenter.removeFirestoreListener();
    }

    private void setProfileImage(String imagePath, int userId) {
        try {
            if (imagePath != null && !"".equals(imagePath)) {
                String[] imgPaths = imagePath.split("/");
                String path = imgPaths[imgPaths.length - 1];
                File imgFile = new File(SellerPerformanceDetailActivity.this.getExternalFilesDir(
                        Environment.DIRECTORY_DOWNLOADS)
                        + "/"
                        + userId
                        + DataMembers.DIGITAL_CONTENT
                        + "/"
                        + DataMembers.USER + "/"
                        + path);
                if (imgFile.exists()) {
                    try {
                        userImage.setScaleType(ImageView.ScaleType.FIT_XY);
                        userImage.setAdjustViewBounds(true);

                        Glide.with(SellerPerformanceDetailActivity.this)
                                .load(imgFile)
                                .centerCrop()
                                .placeholder(R.drawable.ic_default_user)
                                .error(R.drawable.ic_default_user)
                                .into(userImage);

                    } catch (Exception e) {
                        Commons.printException("" + e);
                    }
                } else {
                    userImage.setImageResource(R.drawable.ic_default_user);
                }
            }
        }catch(Exception e){
            Commons.printException(e);
        }
    }
}
