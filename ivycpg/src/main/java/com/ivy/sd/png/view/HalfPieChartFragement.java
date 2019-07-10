package com.ivy.sd.png.view;

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
import com.github.mikephil.charting.utils.ColorTemplate;
import com.ivy.cpg.view.dashboard.DashBoardHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.SKUWiseTargetBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class HalfPieChartFragement extends Fragment implements OnChartValueSelectedListener, OnChartGestureListener {

    private BusinessModel bmodel;
    private PieChart mChart;

    private View view;
    private ArrayList<SKUWiseTargetBO> skuList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_pie_chart, container, false);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        skuList = DashBoardHelper.getInstance(getActivity()).getSkuwiseGraphData();
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
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setTextColor(Color.WHITE);
        l.setXEntrySpace(10f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);
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
        double total = 0;
        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();
        for (SKUWiseTargetBO skuWiseTargetBO : skuList)
            total = total + skuWiseTargetBO.getAchieved();

        if (skuList.size() > 5) {
            double restPercent = 0;
            Collections.sort(skuList, achvimentComparator);
            for (int i = 0; i < 5; i++) {
                double achPercent = (skuList.get(i).getAchieved() / total) * 100;
                entries.add(new PieEntry((float) achPercent, (skuList.get(i).getProductShortName().length() > 0
                        ? skuList.get(i).getProductShortName()
                        : skuList.get(i).getProductName())));
            }
            for (int i = 5; i < skuList.size(); i++) {
                restPercent = restPercent + (skuList.get(i).getAchieved() / total) * 100;
            }
            if (restPercent > 0) {
                entries.add(new PieEntry((float) restPercent, "Rest Sku"));
            }
        } else {
            for (int i = 0; i < skuList.size(); i++) {
                double achPercent = (skuList.get(i).getAchieved() / total) * 100;
                entries.add(new PieEntry((float) achPercent, (skuList.get(i).getProductShortName().length() > 0
                        ? skuList.get(i).getProductShortName()
                        : skuList.get(i).getProductName())));
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(0f);
        dataSet.setSelectionShift(5f);
        // add a lot of colors

        ArrayList<Integer> colors = new ArrayList<Integer>();

        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);

        colors.add(ColorTemplate.getHoloBlue());

        dataSet.setColors(colors);
        dataSet.setDrawValues(false);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(0f);
        mChart.setData(data);

        // undo all highlights
        mChart.highlightValues(null);

        mChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);


    }


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