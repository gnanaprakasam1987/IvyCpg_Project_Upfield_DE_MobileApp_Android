package com.ivy.cpg.view.supervisor.mvp.sellerperformance.sellerperformancelist;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;

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
import com.ivy.cpg.view.supervisor.mvp.sellerperformance.sellerperformancedetail.SellerPerformanceDetailActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.HideShowScrollListener;

import java.util.ArrayList;

import static android.graphics.Color.rgb;

public class SellerPerformanceListActivity extends IvyBaseActivityNoActionBar implements SellerPerformanceContractor.SellerPerformanceView
        , SellerPerformanceListAdapter.ItemClickedListener{

    private RecyclerView sellerPerformanceRecycler;
    private SellerPerformanceListAdapter sellerPerformanceListAdapter;

    private LinearLayout bottomLayout;
    private Animation slide_down, slide_up;
    private ArrayList<SellerBo> sellerPerformanceList = new ArrayList<>();
    private SellerPerformancePresenter sellerPerformancePresenter;

    private String selectedDate;
    private int sellerId;
    private FloatingActionButton sortView;
    private BottomSheetBehavior bottomSheetBehavior;
    private RadioGroup sortRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_performance_list);

        sellerPerformanceRecycler = findViewById(R.id.seller_list_recycler);
        bottomLayout = findViewById(R.id.bottom_layout);

        sortView = findViewById(R.id.fab);

        slide_down = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.out_to_bottom);
        slide_up = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.bottom_layout_slideup);

        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

        sortRadioGroup = findViewById(R.id.sort_radio_group);

        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottomSheetLayout));

        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        bottomSheetBehavior.setHideable(true);
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });

        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        Toolbar toolbar = findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        setScreenTitle("Seller Performance");

        prepareScreenData();

        sellerId = getIntent().getExtras()!=null?getIntent().getExtras().getInt("Sellerid"):0;
        selectedDate = getIntent().getExtras()!=null?getIntent().getExtras().getString("Date"):"";

        sellerPerformancePresenter = new SellerPerformancePresenter();
        sellerPerformancePresenter.setView(this,SellerPerformanceListActivity.this);
        sellerPerformancePresenter.getSellerListAWS();
        sellerPerformancePresenter.sellerActivityInfoListener(sellerId,selectedDate);

        sortView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        sortRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

                int radioButtonID = sortRadioGroup.getCheckedRadioButtonId();
                View radioButton = sortRadioGroup.findViewById(radioButtonID);
                int idx = sortRadioGroup.indexOfChild(radioButton);

                sellerPerformancePresenter.sortList(idx,sellerPerformanceList);
            }
        });

    }


    private void prepareScreenData(){

        sellerPerformanceListAdapter = new SellerPerformanceListAdapter(SellerPerformanceListActivity.this, sellerPerformanceList,this);
        sellerPerformanceRecycler.setAdapter(sellerPerformanceListAdapter);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(SellerPerformanceListActivity.this);
        sellerPerformanceRecycler.setLayoutManager(mLayoutManager);
        sellerPerformanceRecycler.setItemAnimator(new DefaultItemAnimator());


        sellerPerformanceRecycler.addOnScrollListener(new HideShowScrollListener() {
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
        menu.findItem(R.id.menu_sort).setVisible(true);
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
        else if(i == R.id.menu_sort){
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED){
                bottomSheetBehavior.setHideable(true);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
            else
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void updateSellerPerformanceList(ArrayList<SellerBo> sellerList) {
        sellerPerformanceList.clear();
        sellerPerformanceList.addAll(sellerList);
        sellerPerformanceListAdapter.notifyDataSetChanged();
    }

    @Override
    public void notifyListChange(){
        sellerPerformanceListAdapter.notifyDataSetChanged();
    }

    @Override
    public void updateChartInfo(){
        combinedChart();
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
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setAxisMinimum(0f);
        rightAxis.setDrawAxisLine(false);
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
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);

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

//    private BarData generateBarData() {
//
//        BarDataSet set1 = new BarDataSet(sellerPerformanceHelper.getBarEntries(), "Bar 1");
//        set1.setColor(ContextCompat.getColor(this,R.color.white_trans));
//        set1.setValueTextColor((ContextCompat.getColor(this,R.color.white_trans)));
//        set1.setValueTextSize(10f);
//        set1.setAxisDependency(YAxis.AxisDependency.LEFT);
//
//
//        float barWidth = 0.45f; // x2 dataset
//        // (0.45 + 0.02) * 2 + 0.06 = 1.00 -> interval per "group"
//
//        BarData d = new BarData(set1);
//        d.setBarWidth(barWidth);
//
//        // make this BarData object grouped
////        d.groupBars(0, groupSpace, barSpace); // start at x = 0
//
//        return d;
//    }

    private float getRandom(float range, float startsfrom) {
        return (float) (Math.random() * range) + startsfrom;
    }

    @Override
    public void itemclicked(SellerBo sellerBo) {
        Intent intent = new Intent(SellerPerformanceListActivity.this,SellerPerformanceDetailActivity.class);
        intent.putExtra("SellerId",sellerBo.getUserId());
        intent.putExtra("Date",selectedDate);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
