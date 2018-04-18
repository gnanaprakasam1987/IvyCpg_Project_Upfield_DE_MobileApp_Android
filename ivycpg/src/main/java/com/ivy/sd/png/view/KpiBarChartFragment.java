package com.ivy.sd.png.view;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.DashBoardBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

import java.util.ArrayList;

public class KpiBarChartFragment extends IvyBaseFragment {

    ArrayList<DashBoardBO> dashBoardBOS;
    BarChart mbarChart;
    private BusinessModel bmodel;

    public KpiBarChartFragment(ArrayList<DashBoardBO> mDashboardList) {
        super();
        this.dashBoardBOS = mDashboardList;
    }

    public KpiBarChartFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_kpi_bar_chart, container, false);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        mbarChart = view.findViewById(R.id.bar_chart);
        mbarChart.getAxisLeft().setDrawGridLines(false);
        mbarChart.getXAxis().setDrawGridLines(false);
        mbarChart.getDescription().setEnabled(false);

        setData();
        return view;
    }

    private void setData() {

        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
        for (int i = 0; i < dashBoardBOS.size(); i++) {
            yVals1.add(new BarEntry(i, dashBoardBOS.get(i).getCalculatedPercentage(), dashBoardBOS.get(i).getText()));
        }
        BarDataSet set1;
        set1 = new BarDataSet(yVals1, "");

        set1.setDrawIcons(false);
        set1.setValueTextColor(Color.WHITE);
        set1.setValueTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

        set1.setColors(ColorTemplate.MATERIAL_COLORS);
        ArrayList<String> mStirngList = new ArrayList<>();
        for (int i = 0; i < dashBoardBOS.size(); i++) {
            mStirngList.add(dashBoardBOS.get(i).getText().length() > 12 ? dashBoardBOS.get(i).getText().substring(0, 11) + ".." : dashBoardBOS.get(i).getText());
        }


        ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
        dataSets.add(set1);

        BarData data = new BarData(dataSets);
        data.setValueTextSize(10f);
        data.setBarWidth(0.6f);

        mbarChart.setData(data);
        mbarChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(mStirngList));
        mbarChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        XAxis xLabels = mbarChart.getXAxis();
        xLabels.setTextColor(Color.WHITE);
        xLabels.setTextSize(10f);
        xLabels.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

        YAxis yAxisRight = mbarChart.getAxisRight();
        yAxisRight.setEnabled(false);

        YAxis yAxis = mbarChart.getAxisLeft();
        yAxis.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        yAxis.setTextColor(Color.WHITE);
        yAxis.setTextSize(10f);
        yAxis.setAxisMinimum(0f);

        mbarChart.getLegend().setEnabled(false);
    }

}