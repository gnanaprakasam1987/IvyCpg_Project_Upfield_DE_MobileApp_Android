package com.ivy.cpg.view.dashboard.sellerdashboard;

import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.DefaultFillFormatter;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.ivy.cpg.view.dashboard.DashBoardHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.cpg.view.dashboard.DashBoardBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;

public class P3MChartFragment extends Fragment implements OnChartValueSelectedListener, OnChartGestureListener {

    private BusinessModel bmodel;
    private LineChart mChart;

    private View view;
    private ArrayList<DashBoardBO> dashList,computedList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_viewpager_chart, container, false);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        dashList = DashBoardHelper.getInstance(getActivity()).getP3mChartList();
        computedList = new ArrayList<>();
        if (dashList.size() > 0) {
            for (DashBoardBO dashBoardBO : dashList) {
                if (dashBoardBO.getKpiTypeLovID() == DashBoardHelper.getInstance(getActivity()).mParamLovId) {
                    computedList.add(dashBoardBO);
                }
            }

        }
        mChart = (LineChart) view.findViewById(R.id.chart1);
        mChart.setOnChartValueSelectedListener(this);

        mChart.setDrawGridBackground(false);
        mChart.getDescription().setEnabled(false);
        mChart.setDrawBorders(false);

        mChart.getAxisRight().setEnabled(false);

        mChart.getAxisLeft().setDrawAxisLine(false);
        mChart.getAxisLeft().setDrawGridLines(true);
        mChart.getAxisLeft().setGridColor(Color.WHITE);
        mChart.getAxisLeft().setTextColor(Color.WHITE);
        mChart.getAxisLeft().setTextSize(10f);
        mChart.getAxisLeft().setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));


        mChart.getXAxis().setDrawAxisLine(false);
        mChart.getXAxis().setDrawGridLines(false);
        mChart.getXAxis().setGranularity(1f);
        mChart.getXAxis().setTextColor(Color.WHITE);
        mChart.getXAxis().setTextSize(10f);
        mChart.getXAxis().setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        mChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        mChart.getLegend().setEnabled(false);

        if (computedList.isEmpty()) {
            mChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    return computedList.get((int) value).getMonthName();
                }
            });

            mChart.getAxisLeft().setValueFormatter(new PercentFormatter());
        }


        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(false);
        mChart.setScaleEnabled(false);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(false);

        if (computedList.size() > 0)
            setData();
        else
            mChart.setNoDataText("No data");

        // mChart.animateXY(1000, 1000, Easing.EasingOption.EaseOutCubic, Easing.EasingOption.EaseOutCubic);
        mChart.animateY(1400);

    }

    private void setData() {

        ArrayList<Entry> achivedValues = new ArrayList<Entry>();

        for (int i = 0; i < computedList.size(); i++) {
            try {
                achivedValues.add(new Entry(i, SDUtil.convertToFloat("" + computedList.get(i).getConvAcheivedPercentage()), computedList.get(i).getText()));
            } catch (Exception e) {
                Commons.printException(e);
            }
        }

        LineDataSet set1;

        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) mChart.getData().getDataSetByIndex(0);
            set1.setValues(achivedValues);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set1 = new LineDataSet(achivedValues, computedList.get(0).getText() + " - Achieved");
            set1.setLineWidth(1f);
            set1.setCircleRadius(4f);
            set1.setDrawCircleHole(false);
            set1.setValueTextSize(9f);
            set1.setValueTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            set1.setDrawFilled(true);
            set1.setFillFormatter(new DefaultFillFormatter());
            set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            set1.setColor(Color.WHITE);
            set1.setCircleColor(Color.WHITE);

            // create a data object with the datasets
            LineData data = new LineData(set1);
            data.setValueTextColor(Color.WHITE);
            data.setValueTextSize(9f);

            // set data
            mChart.setData(data);
        }
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
}