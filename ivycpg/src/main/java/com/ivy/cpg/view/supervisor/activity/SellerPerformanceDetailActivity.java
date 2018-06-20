package com.ivy.cpg.view.supervisor.activity;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.ivy.cpg.view.supervisor.fragments.OutletPagerDialogFragment;
import com.ivy.lib.DialogFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;

import java.util.ArrayList;

import static android.graphics.Color.rgb;

public class SellerPerformanceDetailActivity extends IvyBaseActivityNoActionBar {

    private CombinedChart mChart;
    private final int itemcount = 12;
    private String[] mMonths = new String[] {
            "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"
    };

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

        combinedChart();

        findViewById(R.id.bottom_outlet_btn_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OutletPagerDialogFragment outletPagerDialogFragment = new OutletPagerDialogFragment();
                outletPagerDialogFragment.setStyle(DialogFragment.STYLE_NO_FRAME, 0);
                outletPagerDialogFragment.setCancelable(false);
                outletPagerDialogFragment.show(getSupportFragmentManager(),"OutletPager");
            }
        });
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
        mChart = findViewById(R.id.combined_chart);
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

        ArrayList<Entry> entries1 = new ArrayList<Entry>();
        ArrayList<Entry> entries2 = new ArrayList<Entry>();

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

        ArrayList<BarEntry> entries1 = new ArrayList<BarEntry>();

        for (int index = 0; index < itemcount; index++) {
            entries1.add(new BarEntry(5,55));
        }

        BarDataSet set1 = new BarDataSet(entries1, "Bar 1");
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
}
