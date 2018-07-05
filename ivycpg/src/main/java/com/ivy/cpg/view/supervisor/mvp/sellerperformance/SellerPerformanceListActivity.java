package com.ivy.cpg.view.supervisor.mvp.sellerperformance;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

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
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.HideShowScrollListener;

import java.util.ArrayList;

import static android.graphics.Color.rgb;

public class SellerPerformanceListActivity extends IvyBaseActivityNoActionBar {

    private RecyclerView recyclerView;

    private LinearLayout bottomLayout;
    private Animation slide_down, slide_up;
    SellerPerformanceHelper sellerPerformanceHelper;
    private String[] mMonths;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_performance_list);

        recyclerView = findViewById(R.id.seller_list_recycler);
        bottomLayout = findViewById(R.id.bottom_layout);

        slide_down = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.out_to_bottom);
        slide_up = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.bottom_layout_slideup);

        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

        Toolbar toolbar = findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        setScreenTitle("Seller Performance");

        prepareScreenData();

        sellerPerformanceHelper = new SellerPerformanceHelper();
        mMonths = sellerPerformanceHelper.getSellerPerformanceList();

        combinedChart();

    }

    private void prepareScreenData(){

        SellerPerformanceListAdapter myAdapter = new SellerPerformanceListAdapter(getApplicationContext());
        recyclerView.setAdapter(myAdapter);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());


        recyclerView.addOnScrollListener(new HideShowScrollListener() {
            @Override
            public void onHide() {
                if (bottomLayout.getVisibility() == View.VISIBLE) {
                    bottomLayout.startAnimation(slide_down);
                    bottomLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onShow() {
                if (bottomLayout.getVisibility() == View.GONE) {
                    bottomLayout.setVisibility(View.VISIBLE);
                    bottomLayout.startAnimation(slide_up);
                }
            }

            @Override
            public void onScrolled() {
                // To load more data
            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_supervisor_screen, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        ImageView searchClose = searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
        searchClose.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        searchView.setSearchableInfo(searchManager != null ? searchManager.getSearchableInfo(getComponentName()) : null);
        SearchView.OnQueryTextListener textChangeListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
//                displaySearchItem(newText);
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }
        };
        searchView.setOnQueryTextListener(textChangeListener);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_dashboard).setVisible(false);
        menu.findItem(R.id.menu_date).setVisible(false);
        return true;
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

    private float getRandom(float range, float startsfrom) {
        return (float) (Math.random() * range) + startsfrom;
    }

}
