package com.ivy.cpg.view.dashboard.sellerdashboard;

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
import com.ivy.cpg.view.dashboard.DashBoardHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.cpg.view.dashboard.DashBoardBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

import java.util.ArrayList;

public class SMPChartFragment extends Fragment {

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
        mDashboardList = DashBoardHelper.getInstance(getActivity()).getDashboardBO();
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

            mChart.setDrawCenterText(true);

            // enable rotation of the chart by touch
            mChart.setRotationEnabled(false);
            mChart.setHighlightPerTapEnabled(true);

            mChart.animateXY(1400, 1400, Easing.EasingOption.EaseInOutQuad, Easing.EasingOption.EaseInOutQuad);

            Legend l = mChart.getLegend();
            l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
            l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
            l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
            l.setDrawInside(false);
            l.setXEntrySpace(7f);
            l.setYEntrySpace(0f);
            l.setTextColor(Color.WHITE);
            l.setYOffset(0f);


            //if (isSemiCircleChartRequired) {
                setOffset(mChart);
                mChart.setHoleColor(Color.TRANSPARENT);
                mChart.setHoleRadius(50f);
                mChart.setTransparentCircleRadius(28f);
                mChart.setMaxAngle(180f); // HALF CHART
                mChart.setRotationAngle(180f);
                // entry label styling
                mChart.setEntryLabelColor(Color.WHITE);
                mChart.setEntryLabelTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                mChart.setEntryLabelTextSize(0f);
            //}
            setData();
        }
        DashBoardHelper.getInstance(getActivity()).setDashboardBO(new DashBoardBO());
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    private void setData() {
        if (mDashboardList.getKpiTarget() != null && !mDashboardList.getKpiTarget().equals("0")) {
            ArrayList<PieEntry> entries = new ArrayList<PieEntry>();
            float temp_ach = SDUtil.convertToFloat(mDashboardList.getKpiAcheived()) - SDUtil.convertToFloat(mDashboardList.getKpiTarget());
            if (temp_ach > 0) {
                int round = Math.round(SDUtil.convertToFloat(mDashboardList.getKpiAcheived()) /
                        (SDUtil.convertToFloat(mDashboardList.getKpiTarget())) * 100);
                if (round % 100 == 0) {
                    round = round + 1;
                }
                int rounded = ((round + 99) / 100) * 100;
                entries.add(new PieEntry(SDUtil.convertToFloat(mDashboardList.getKpiAcheived()), "100%"));

                int bonus = Math.round(SDUtil.convertToFloat(mDashboardList.getKpiAcheived()) /
                        (SDUtil.convertToFloat(mDashboardList.getKpiTarget())) * 100);
                entries.add(new PieEntry(temp_ach, bonus + "%"));

                entries.add(new PieEntry(SDUtil.convertToFloat(mDashboardList.getKpiTarget()), (rounded) + "%"));
            } else {
                int bonus = Math.round(SDUtil.convertToFloat(mDashboardList.getKpiAcheived()) /
                        (SDUtil.convertToFloat(mDashboardList.getKpiTarget())) * 100);
                entries.add(new PieEntry(SDUtil.convertToFloat(mDashboardList.getKpiAcheived()), bonus + "%"));
                entries.add(new PieEntry(SDUtil.convertToFloat(mDashboardList.getKpiTarget()), "100%"));
            }


            PieDataSet dataSet = new PieDataSet(entries, "");

            dataSet.setSliceSpace(isSemiCircleChartRequired ? 0f : 3f);
            dataSet.setSelectionShift(5f);

            // add a lot of colors

            ArrayList<Integer> colors = new ArrayList<Integer>();
/*
        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);*/
            colors.add(Color.rgb(119, 147, 60));
            colors.add(Color.rgb(255, 192, 0));
            colors.add(Color.rgb(192, 80, 78));

            dataSet.setColors(colors);
            dataSet.setDrawValues(false);

            dataSet.setValueLinePart1OffsetPercentage(80.f);
            dataSet.setValueLinePart1Length(0.2f);
            dataSet.setValueLinePart2Length(0.4f);
            //dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
            dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

            PieData data = new PieData(dataSet);
            data.setValueFormatter(new PercentFormatter());
            data.setValueTextSize(12f);
            data.setValueTextColor(Color.BLACK);
            data.setValueTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            mChart.setData(data);
            mChart.invalidate();
        }
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