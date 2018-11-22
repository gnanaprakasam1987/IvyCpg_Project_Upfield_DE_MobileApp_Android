package com.ivy.ui.dashboard.chart.kpi;

import android.graphics.Color;
import android.view.View;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.ivy.core.base.view.BaseFragment;
import com.ivy.cpg.view.dashboard.DashBoardBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;

import butterknife.BindView;

public class KPIBarChartFragment extends BaseFragment {

    private String selectedInterval;

    private ArrayList<DashBoardBO> dashboardListData;

    @BindView(R.id.bar_chart)
    BarChart mBarChart;

    @Override
    public void initializeDi() {

    }

    @Override
    protected int setContentViewLayout() {
        return R.layout.fragment_kpi_bar_chart;
    }

    @Override
    public void initVariables(View view) {

    }

    @Override
    protected void getMessageFromAliens() {
        if(getArguments() != null) {
            selectedInterval = getArguments().getString("selectedInterval");
            dashboardListData = (ArrayList<DashBoardBO>) getArguments().getSerializable("dashChartList");
            setChartData();
        }
    }

    @Override
    protected void setUpViews() {

        mBarChart.getAxisLeft().setDrawGridLines(false);
        mBarChart.getXAxis().setDrawGridLines(false);
        mBarChart.getDescription().setEnabled(false);

    }

    private void setChartData() {
        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
        for (int i = 0; i < dashboardListData.size(); i++) {
            yVals1.add(new BarEntry(i, dashboardListData.get(i).getCalculatedPercentage(), dashboardListData.get(i).getText()));
        }
        BarDataSet set1;
        set1 = new BarDataSet(yVals1, "");

        set1.setDrawIcons(false);
        set1.setValueTextColor(Color.WHITE);
        set1.setValueTypeface(FontUtils.getFontRoboto(getActivity(),FontUtils.FontType.LIGHT));

        set1.setColors(ColorTemplate.MATERIAL_COLORS);
        ArrayList<String> mStirngList = new ArrayList<>();
        for (int i = 0; i < dashboardListData.size(); i++) {
            String text = dashboardListData.get(i).getText().length() > 12 ? dashboardListData.get(i).getText().substring(0, 11) + ".." : dashboardListData.get(i).getText();
            if(selectedInterval != null && (selectedInterval.matches("WEEK|P3M"))){
                mStirngList.add((dashboardListData.get(i).getMonthName() != null && dashboardListData.get(i).getMonthName().length() == 0) ?
                        text : "(" + dashboardListData.get(i).getMonthName() + ")" + text);
            } else{
                mStirngList.add(text);
            }
        }


        ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
        dataSets.add(set1);

        BarData data = new BarData(dataSets);
        data.setValueTextSize(10f);
        data.setBarWidth(0.6f);

        mBarChart.setData(data);
        mBarChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(mStirngList));
        mBarChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        XAxis xLabels = mBarChart.getXAxis();
        xLabels.setTextColor(Color.WHITE);
        xLabels.setTextSize(10f);
        xLabels.setTypeface(FontUtils.getFontRoboto(getActivity(),FontUtils.FontType.LIGHT));
        xLabels.setGranularity(1f);

        YAxis yAxisRight = mBarChart.getAxisRight();
        yAxisRight.setEnabled(false);

        YAxis yAxis = mBarChart.getAxisLeft();
        yAxis.setTypeface(FontUtils.getFontRoboto(getActivity(),FontUtils.FontType.LIGHT));
        yAxis.setTextColor(Color.WHITE);
        yAxis.setTextSize(10f);
        yAxis.setAxisMinimum(0f);

        mBarChart.getLegend().setEnabled(false);
        mBarChart.animateY(500, Easing.EasingOption.EaseInOutQuad);
    }
}
