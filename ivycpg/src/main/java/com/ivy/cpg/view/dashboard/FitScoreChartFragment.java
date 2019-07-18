package com.ivy.cpg.view.dashboard;

import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.FitScoreChartBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

import java.util.ArrayList;

public class FitScoreChartFragment extends Fragment {

    private BusinessModel bmodel;
    private PieChart mChart;

    private View view;
    private FitScoreChartBO mDashboardList;
    boolean isSemiCircleChartRequired;
    TextView target_tv, previous_score_TV, growth_tv, txtTitle;
    LinearLayout lnrPreviousScore;
    TextView textView11, textView12, textView13;

    public static FitScoreChartFragment newInstance(String retailerID, String Module, String title) {
        FitScoreChartFragment myFragment = new FitScoreChartFragment();
        Bundle args = new Bundle();
        args.putString("retailerID", retailerID);
        args.putString("Module", Module);
        args.putString("Title", title);
        myFragment.setArguments(args);
        return myFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fitscore_dashboard_toolbar, container, false);
        Bundle b = getArguments();
        String retailerID = b.getString("retailerID", "");
        String Module = b.getString("Module", "");
        String title = b.getString("Title", "");

        target_tv = (TextView) view.findViewById(R.id.target_tv);
        previous_score_TV = (TextView) view.findViewById(R.id.previous_score_TV);
        growth_tv = (TextView) view.findViewById(R.id.growth_tv);
        txtTitle = (TextView) view.findViewById(R.id.txtTitle);

        textView11 = (TextView) view.findViewById(R.id.textView11);
        textView12 = (TextView) view.findViewById(R.id.textView12);
        textView13 = (TextView) view.findViewById(R.id.textView13);

        view.findViewById(R.id.color_rep_black).setBackgroundColor(Color.rgb(255, 192, 0));
        view.findViewById(R.id.color_rep_orange).setBackgroundColor(Color.rgb(119, 147, 60));

        lnrPreviousScore = (LinearLayout) view.findViewById(R.id.lnrPreviousScore);
        //if (!Module.equalsIgnoreCase("ALL")) {
            lnrPreviousScore.setVisibility(View.GONE);
        //}

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        mChart = (PieChart) view.findViewById(R.id.chart1);

        bmodel.fitscoreHelper.getFitScoreChartforall(retailerID);
        for (FitScoreChartBO chart : bmodel.fitscoreHelper.getFitScoreChartList()) {
            if (chart.getModule().equals(Module)) {
                mDashboardList = chart;
            }
        }

        if (mDashboardList != null && mDashboardList.getAchieved() != null && mDashboardList.getTarget() != null) {
            txtTitle.setText(title);
            if (txtTitle != null) {
                txtTitle.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
                //txtTitle.setText(title);
            }
            mChart.setUsePercentValues(false);
            mChart.getDescription().setEnabled(false);
            mChart.setExtraOffsets(0, 0, 0, 0);

            mChart.setDragDecelerationFrictionCoef(0.95f);

            mChart.setDrawHoleEnabled(isSemiCircleChartRequired ? true : false);

            mChart.setTransparentCircleColor(Color.TRANSPARENT);
            mChart.setTransparentCircleAlpha(110);

            mChart.setDrawCenterText(false);

            // enable rotation of the chart by touch
            mChart.setRotationEnabled(false);
            mChart.setHighlightPerTapEnabled(false);

            mChart.animateXY(1400, 1400, Easing.EasingOption.EaseInOutQuad, Easing.EasingOption.EaseInOutQuad);

            Legend l = mChart.getLegend();
            l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
            l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
            l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
            l.setDrawInside(false);
            l.setXEntrySpace(7f);
            l.setYEntrySpace(0f);
            l.setTextColor(Color.TRANSPARENT);
            l.setYOffset(0f);
            mChart.getLegend().setEnabled(false);

            //if (isSemiCircleChartRequired) {
//            setOffset(mChart);
//            mChart.setHoleColor(Color.TRANSPARENT);
//            mChart.setHoleRadius(50f);
//            mChart.setTransparentCircleRadius(28f);
//            mChart.setMaxAngle(180f); // HALF CHART
//            mChart.setRotationAngle(180f);
            // entry label styling
//            mChart.setEntryLabelColor(Color.WHITE);
//            mChart.setEntryLabelTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
//            mChart.setEntryLabelTextSize(0f);
            //}
            setData();
            target_tv.setText(mDashboardList.getTarget() + "");
            previous_score_TV.setText((bmodel.getRetailerMasterBO().getRField5().isEmpty()
                    ? "0" : bmodel.getRetailerMasterBO().getRField5()));
            growth_tv.setText(mDashboardList.getAchieved() + "");
            target_tv.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.MEDIUM));
            previous_score_TV.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.MEDIUM));
            growth_tv.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.MEDIUM));
            textView11.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
            textView12.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
            textView13.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    private void setData() {

        if (mDashboardList.getTarget() != null && !mDashboardList.getTarget().equals("0")) {
            ArrayList<PieEntry> entries = new ArrayList<PieEntry>();
            float temp_ach = SDUtil.convertToFloat(mDashboardList.getAchieved()) - SDUtil.convertToFloat(mDashboardList.getTarget());
            if (temp_ach > 0) {
                int round = Math.round(SDUtil.convertToFloat(mDashboardList.getAchieved()) /
                        (SDUtil.convertToFloat(mDashboardList.getTarget())) * 100);
                if (round % 100 == 0) {
                    round = round + 1;
                }
                int rounded = ((round + 99) / 100) * 100;
                entries.add(new PieEntry(SDUtil.convertToFloat(mDashboardList.getTarget()), "100%"));

                int bonus = Math.round(SDUtil.convertToFloat(mDashboardList.getAchieved()) /
                        (SDUtil.convertToFloat(mDashboardList.getTarget())) * 100);
                entries.add(new PieEntry(temp_ach, bonus + "%"));

                if (temp_ach > SDUtil.convertToFloat(mDashboardList.getTarget())) {
                    entries.add(new PieEntry(SDUtil.convertToFloat(mDashboardList.getAchieved()) - temp_ach, (rounded) + "%"));
                } else {
                    entries.add(new PieEntry(SDUtil.convertToFloat(mDashboardList.getTarget()) - temp_ach, (rounded) + "%"));
                }
            } else {
                int bonus = Math.round(SDUtil.convertToFloat(mDashboardList.getAchieved()) /
                        (SDUtil.convertToFloat(mDashboardList.getTarget())) * 100);
                entries.add(new PieEntry(SDUtil.convertToFloat(mDashboardList.getAchieved()), bonus + "%"));
                entries.add(new PieEntry(SDUtil.convertToFloat(mDashboardList.getTarget()) -
                        SDUtil.convertToFloat(mDashboardList.getAchieved()), "100%"));
            }


            PieDataSet dataSet = new PieDataSet(entries, "");

            dataSet.setSliceSpace(3f);
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

//            if(Module.equalsIgnoreCase("ALL")) {
//                dataSet.setDrawValues(false);
//                dataSet.setValueLinePart1OffsetPercentage(80.f);
//                dataSet.setValueLinePart1Length(0.2f);
//                dataSet.setValueLinePart2Length(0.4f);
//                //dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
//                dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
//            } else{
            mChart.setDrawSliceText(false);
//            }

            PieData data = new PieData(dataSet);
//            if(Module.equalsIgnoreCase("ALL")) {
//                data.setValueFormatter(new PercentFormatter());
//            }
            data.setValueTextSize(12f);
            data.setValueTextColor(Color.WHITE);
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
