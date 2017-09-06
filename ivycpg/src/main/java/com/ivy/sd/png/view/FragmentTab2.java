package com.ivy.sd.png.view;

import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.DashBoardBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

import java.util.ArrayList;

public class FragmentTab2 extends Fragment {

    private BusinessModel bmodel;
    private PieChart mChart;

    private View view;
    private DashBoardBO mDashboardList;
    boolean isSemiCircleChartRequired;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_viewpager_piechart, container, false);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        mChart = (PieChart) view.findViewById(R.id.chart1);
        mDashboardList = bmodel.dashBoardHelper.getDashboardBO();
        if (mDashboardList != null && mDashboardList.getKpiAcheived() != null && mDashboardList.getKpiTarget() != null) {
            isSemiCircleChartRequired = (mDashboardList.getFlex1() == 0);
            //mChart.setOnChartValueSelectedListener(this);

            mChart.setUsePercentValues(true);
            mChart.getDescription().setEnabled(false);
            mChart.setExtraOffsets(0, 0, 0, 0);

            mChart.setDragDecelerationFrictionCoef(0.95f);

            mChart.setDrawHoleEnabled(isSemiCircleChartRequired ? true : false);

            mChart.setTransparentCircleColor(isSemiCircleChartRequired ? Color.TRANSPARENT : Color.WHITE);
            mChart.setTransparentCircleAlpha(110);

            mChart.setDrawCenterText(false);

            // enable rotation of the chart by touch
            mChart.setRotationEnabled(false);
            mChart.setHighlightPerTapEnabled(true);

            mChart.animateXY(1400, 1400, Easing.EasingOption.EaseInOutQuad, Easing.EasingOption.EaseInOutQuad);


            Legend l = mChart.getLegend();
            l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
            l.setOrientation(Legend.LegendOrientation.VERTICAL);
            l.setDrawInside(false);
            l.setTextColor(Color.WHITE);
            l.setXEntrySpace(5f);
            l.setYEntrySpace(0f);
            l.setYOffset(0f);
            l.setXOffset(25f);
            l.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));

            if (isSemiCircleChartRequired) {
                setOffset(mChart);
                mChart.setHoleColor(Color.TRANSPARENT);
                mChart.setHoleRadius(50f);
                mChart.setTransparentCircleRadius(28f);
                mChart.setMaxAngle(180f); // HALF CHART
                mChart.setRotationAngle(180f);
                // entry label styling
                mChart.setEntryLabelColor(Color.TRANSPARENT);
                mChart.setEntryLabelTextSize(0f);
            }
            setData();
        }
        bmodel.dashBoardHelper.setDashboardBO(new DashBoardBO());
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    private void setData() {

        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();

        entries.add(new PieEntry(Float.parseFloat(mDashboardList.getKpiAcheived())));
        entries.add(new PieEntry(Float.parseFloat(mDashboardList.getKpiTarget())));

        PieDataSet dataSet = new PieDataSet(entries, "");

        dataSet.setSliceSpace(isSemiCircleChartRequired ? 0f : 3f);
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
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);
        data.setValueTextSize(0f);
        mChart.setData(data);
    }

    public void setOffset(PieChart mChart) {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;

        int offset = (int) (height * 0.20); /* percent to move */

        LinearLayout.LayoutParams rlParams =
                (LinearLayout.LayoutParams) mChart.getLayoutParams();
        rlParams.setMargins(0, 10, 0, -offset);
        mChart.setLayoutParams(rlParams);
    }
}