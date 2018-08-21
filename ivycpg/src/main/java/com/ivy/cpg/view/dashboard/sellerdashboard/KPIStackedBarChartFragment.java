package com.ivy.cpg.view.dashboard.sellerdashboard;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.ivy.cpg.view.dashboard.DashBoardHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.cpg.view.dashboard.DashBoardBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

import java.util.ArrayList;
import java.util.Collection;

public class KPIStackedBarChartFragment extends IvyBaseFragment {

    ArrayList<DashBoardBO> dashBoardList;
    BarChart mbarChart;
    private BusinessModel bmodel;
    private String selectedInterval;
    public static final int[] MATERIAL_COLORS = {
            ColorTemplate.rgb("#2ecc71"), ColorTemplate.rgb("#e74c3c")
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_kpi_stackedbar_chart, container, false);

        if (this.getArguments() != null) {
            selectedInterval = this.getArguments().getString("selectedInterval");
        }
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        dashBoardList = DashBoardHelper.getInstance(getActivity()).getDashListViewList();

        mbarChart = view.findViewById(R.id.stackedbar_chart);
        mbarChart.getAxisLeft().setDrawGridLines(false);
        mbarChart.getXAxis().setDrawGridLines(false);
        mbarChart.getDescription().setEnabled(false);

        setData();
        return view;
    }

    private void setData() {

        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
        for (int i = 0; i < dashBoardList.size(); i++) {
            float val1 = dashBoardList.get(i).getCalculatedPercentage();
            float val2 = (100 - dashBoardList.get(i).getCalculatedPercentage());

            yVals1.add(new BarEntry(i, new float[]{val1, val2}));
        }

        BarDataSet set1;
        set1 = new BarDataSet(yVals1, "");
        set1.setStackLabels(new String[]{"Achieved", "Target"});
        set1.setValueFormatter(new PercentageValueFormatter());
        set1.setDrawIcons(false);
        set1.setValueTextColor(Color.WHITE);
        set1.setValueTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        set1.setColors(MATERIAL_COLORS);
        ArrayList<String> mStirngList = new ArrayList<>();
        for (int i = 0; i < dashBoardList.size(); i++) {
            String text = dashBoardList.get(i).getText().length() > 12 ? dashBoardList.get(i).getText().substring(0, 11) + ".." : dashBoardList.get(i).getText();
            if (selectedInterval != null && (selectedInterval.matches("WEEK|P3M"))) {
                mStirngList.add((dashBoardList.get(i).getMonthName() != null && dashBoardList.get(i).getMonthName().length() == 0) ?
                        text : "(" + dashBoardList.get(i).getMonthName() + ")" + text);
            } else {
                mStirngList.add(text);
            }
        }
        ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
        dataSets.add(set1);

        BarData data = new BarData(dataSets);
        data.setValueTextSize(10f);
        data.setBarWidth(0.6f);

        mbarChart.setData(data);

        XAxis xLabels = mbarChart.getXAxis();
        xLabels.setTextColor(Color.WHITE);
        xLabels.setTextSize(10f);
        xLabels.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        xLabels.setGranularity(1f);
        xLabels.setValueFormatter(new IndexAxisValueFormatter(mStirngList));
        xLabels.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis yAxisRight = mbarChart.getAxisRight();
        yAxisRight.setCenterAxisLabels(true);
        yAxisRight.setDrawLabels(true);
        yAxisRight.setEnabled(false);

        YAxis yAxis = mbarChart.getAxisLeft();
        yAxis.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        yAxis.setTextColor(Color.WHITE);
        yAxis.setTextSize(10f);
        yAxis.setAxisMinimum(0f);

        mbarChart.getLegend().setEnabled(true);
        mbarChart.getLegend().setTextColor(Color.WHITE);

        mbarChart.animateY(500, Easing.EasingOption.EaseInOutQuad);
    }

    public class PercentageValueFormatter implements IValueFormatter {

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return String.valueOf(SDUtil.format(value, 0, 2) + "%");
        }
    }
}
