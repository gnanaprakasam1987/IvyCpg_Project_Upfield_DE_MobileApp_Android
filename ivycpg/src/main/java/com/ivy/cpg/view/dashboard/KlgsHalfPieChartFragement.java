package com.ivy.cpg.view.dashboard;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.SKUWiseTargetBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

import java.util.ArrayList;
import java.util.Comparator;

import static com.github.mikephil.charting.utils.ColorTemplate.rgb;

public class KlgsHalfPieChartFragement extends Fragment implements OnChartValueSelectedListener, OnChartGestureListener {

    private BusinessModel bmodel;
    private PieChart mChart;

    private View view;
    private ArrayList<SKUWiseTargetBO> skuList;

    private DashBoardHelper dashBoardHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_pie_chart, container, false);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        dashBoardHelper = DashBoardHelper.getInstance(context);
    }

    @Override
    public void onStart() {
        super.onStart();
        skuList = dashBoardHelper.getSkuWiseTargetList();
        mChart = (PieChart) view.findViewById(R.id.chart1);
        mChart.setOnChartValueSelectedListener(this);
        moveOffScreen();
        mChart.setUsePercentValues(true);
        mChart.getDescription().setEnabled(false);
        mChart.setExtraOffsets(0, 0, 0, 0);
        mChart.setDragDecelerationFrictionCoef(0.95f);

        mChart.setDrawHoleEnabled(true);
        mChart.setHoleColor(Color.TRANSPARENT);

        mChart.setTransparentCircleColor(Color.TRANSPARENT);
        mChart.setTransparentCircleAlpha(110);

        mChart.setHoleRadius(25f);
        mChart.setTransparentCircleRadius(28f);

        mChart.setDrawCenterText(true);

        mChart.setMaxAngle(180f); // HALF CHART
        mChart.setRotationAngle(180f);
        // enable rotation of the chart by touch
        mChart.setRotationEnabled(false);
        mChart.setHighlightPerTapEnabled(true);

        Legend l = mChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setTextColor(Color.WHITE);
        l.setXEntrySpace(10f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);
        l.setTextSize(12f);
        l.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));


        if (skuList.size() > 0)
            setData();
        else
            mChart.setNoDataText("No data");

        // entry label styling
        mChart.setEntryLabelColor(Color.TRANSPARENT);
        mChart.setEntryLabelTextSize(0f);

    }

    private void setData() {
        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();


        double achPercent = skuList.get(dashBoardHelper.mSelectedSkuIndex).getAchieved();
        double targetPercent = skuList.get(dashBoardHelper.mSelectedSkuIndex).getTarget();
        entries.add(new PieEntry((float) targetPercent, getActivity().getResources().getString(R.string.target)));
        entries.add(new PieEntry((float) achPercent, getActivity().getResources().getString(R.string.achieved)));

        PieDataSet dataSet = new PieDataSet(entries, skuList.get(dashBoardHelper.mSelectedSkuIndex).getProductName());
        dataSet.setSliceSpace(0f);
        dataSet.setSelectionShift(5f);
        dataSet.setValueTextSize(getResources().getDimension(R.dimen.text_size_primary));

        dataSet.setColors(MATERIAL_COLORS);
        dataSet.setDrawValues(false);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(0f);
        mChart.setData(data);

        // undo all highlights
        mChart.highlightValues(null);

        mChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);


    }

    private int[] MATERIAL_COLORS = {
            rgb("#83c341"), rgb("#ed7c0d"), rgb("#e74c3c"), rgb("#3498db")
    };

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

    }

    @Override
    public void onChartLongPressed(MotionEvent me) {

    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {

    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {

    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {

    }

    private Comparator<SKUWiseTargetBO> achvimentComparator = new Comparator<SKUWiseTargetBO>() {

        public int compare(SKUWiseTargetBO file1, SKUWiseTargetBO file2) {


            return (int) file2.getAchieved() - (int) file1.getAchieved();

        }

    };

    private void moveOffScreen() {

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;

        int offset = (int) (height * 0.20); /* percent to move */

        RelativeLayout.LayoutParams rlParams =
                (RelativeLayout.LayoutParams) mChart.getLayoutParams();
        rlParams.setMargins(0, 0, 0, -offset);
        mChart.setLayoutParams(rlParams);
    }
}