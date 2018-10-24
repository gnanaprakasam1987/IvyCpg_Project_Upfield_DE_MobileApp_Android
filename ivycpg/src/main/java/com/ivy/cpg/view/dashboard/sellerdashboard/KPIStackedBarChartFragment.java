package com.ivy.cpg.view.dashboard.sellerdashboard;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.buffer.BarBuffer;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.renderer.BarChartRenderer;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.ivy.cpg.view.dashboard.DashBoardBO;
import com.ivy.cpg.view.dashboard.DashBoardHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;
import java.util.List;

public class KPIStackedBarChartFragment extends IvyBaseFragment {

    ArrayList<DashBoardBO> dashBoardList;
    BarChart mbarChart;
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
        BusinessModel bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        dashBoardList = DashBoardHelper.getInstance(getActivity()).getDashListViewList();

        mbarChart = view.findViewById(R.id.stackedbar_chart);
        mbarChart.getAxisLeft().setDrawGridLines(false);
        mbarChart.getXAxis().setDrawGridLines(false);
        mbarChart.getDescription().setEnabled(false);
        mbarChart.setDoubleTapToZoomEnabled(false);
        mbarChart.setPinchZoom(false);
        mbarChart.setScaleEnabled(false);

        mbarChart.setRenderer(new MyBarChartRenderer(mbarChart, mbarChart.getAnimator(), mbarChart.getViewPortHandler()));
        setData();
        return view;
    }

    private void setData() {

        ArrayList<BarEntry> yVals1 = new ArrayList<>();
        for (int i = 0; i < dashBoardList.size(); i++) {
            if (selectedInterval != null && (selectedInterval.matches("WEEK|P3M"))) {
                float val1, val2;
                float target = SDUtil.convertToFloat(dashBoardList.get(i).getKpiTarget());
                float achieved = SDUtil.convertToFloat(dashBoardList.get(i).getKpiAcheived());
                if (achieved > target) {
                    val1 = achieved;
                    val2 = 0;
                } else {
                    val1 = achieved;
                    val2 = target - achieved;
                }
                yVals1.add(new BarEntry(i, new float[]{val1, val2}));
            } else {
                float val1 = dashBoardList.get(i).getCalculatedPercentage();
                float val2 = (100 - dashBoardList.get(i).getCalculatedPercentage());
                yVals1.add(new BarEntry(i, new float[]{val1, val2}));
            }
        }

        String text = "";
        ArrayList<String> mStringList = new ArrayList<>();
        for (int i = 0; i < dashBoardList.size(); i++) {
            text = dashBoardList.get(i).getText().length() > 12 ? dashBoardList.get(i).getText().substring(0, 11) + ".." : dashBoardList.get(i).getText();
            if (selectedInterval != null && (selectedInterval.matches("WEEK|P3M"))) {
                mStringList.add((dashBoardList.get(i).getMonthName() != null && dashBoardList.get(i).getMonthName().length() == 0) ?
                        text : dashBoardList.get(i).getMonthName());
            } else {
                mStringList.add(text);
            }
        }

        BarDataSet set1;
        set1 = new BarDataSet(yVals1, "");
        String kpiLabel = " " + text;
        if (selectedInterval != null && (selectedInterval.matches("WEEK|P3M"))) {
            set1.setStackLabels(new String[]{getResources().getString(R.string.achieved) + kpiLabel , getResources().getString(R.string.balance) + kpiLabel});
        } else {
            set1.setStackLabels(new String[]{"% " + getResources().getString(R.string.achieved), "% " + getResources().getString(R.string.balance)});
        }
        set1.setValueFormatter(new PercentageValueFormatter());
        set1.setDrawIcons(false);
        set1.setValueTextColor(Color.WHITE);
        set1.setValueTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
        set1.setColors(MATERIAL_COLORS);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        BarData data = new BarData(dataSets);
        data.setValueTextSize(10f);
        data.setBarWidth(0.6f);

        mbarChart.setData(data);

        XAxis xLabels = mbarChart.getXAxis();
        xLabels.setTextColor(Color.WHITE);
        xLabels.setTextSize(10f);
        xLabels.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
        xLabels.setGranularity(1f);
        xLabels.setValueFormatter(new IndexAxisValueFormatter(mStringList));
        xLabels.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis yAxisRight = mbarChart.getAxisRight();
        yAxisRight.setCenterAxisLabels(true);
        yAxisRight.setDrawLabels(true);
        yAxisRight.setEnabled(false);

        YAxis yAxis = mbarChart.getAxisLeft();
        yAxis.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
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
            String labelStr;
            if (selectedInterval != null && (!selectedInterval.matches("WEEK|P3M"))) {
                labelStr = String.valueOf(SDUtil.format(value, 0, 0)) + "%";
            } else {
                labelStr = String.valueOf(SDUtil.format(value, 0, 0));
            }
            return labelStr;
        }
    }

    public class MyBarChartRenderer extends BarChartRenderer {

        MyBarChartRenderer(BarDataProvider chart, ChartAnimator animator,
                           ViewPortHandler viewPortHandler) {
            super(chart, animator, viewPortHandler);
        }

        @Override
        public void drawValues(Canvas c) {
            // if values are drawn
            if (isDrawingValuesAllowed(mChart)) {

                List<IBarDataSet> dataSets = mChart.getBarData().getDataSets();

                final float valueOffsetPlus = Utils.convertDpToPixel(4.5f);
                float posOffset;
                float negOffset;
                boolean drawValueAboveBar = mChart.isDrawValueAboveBarEnabled();

                for (int i = 0; i < mChart.getBarData().getDataSetCount(); i++) {

                    IBarDataSet dataSet = dataSets.get(i);

                    if (!shouldDrawValues(dataSet))
                        continue;

                    // apply the text-styling defined by the DataSet
                    applyValueTextStyle(dataSet);

                    boolean isInverted = mChart.isInverted(dataSet.getAxisDependency());

                    // calculate the correct offset depending on the draw position of
                    // the value
                    float valueTextHeight = Utils.calcTextHeight(mValuePaint, "8");
                    posOffset = (drawValueAboveBar ? -valueOffsetPlus : valueTextHeight + valueOffsetPlus);
                    negOffset = (drawValueAboveBar ? valueTextHeight + valueOffsetPlus : -valueOffsetPlus);

                    if (isInverted) {
                        posOffset = -posOffset - valueTextHeight;
                        negOffset = -negOffset - valueTextHeight;
                    }

                    // get the buffer
                    BarBuffer buffer = mBarBuffers[i];

                    final float phaseY = mAnimator.getPhaseY();

                    MPPointF iconsOffset = MPPointF.getInstance(dataSet.getIconsOffset());
                    iconsOffset.x = Utils.convertDpToPixel(iconsOffset.x);
                    iconsOffset.y = Utils.convertDpToPixel(iconsOffset.y);

                    // if only single values are drawn (sum)
                    if (!dataSet.isStacked()) {

                        for (int j = 0; j < buffer.buffer.length * mAnimator.getPhaseX(); j += 4) {

                            float x = (buffer.buffer[j] + buffer.buffer[j + 2]) / 2f;

                            if (!mViewPortHandler.isInBoundsRight(x))
                                break;

                            if (!mViewPortHandler.isInBoundsY(buffer.buffer[j + 1])
                                    || !mViewPortHandler.isInBoundsLeft(x))
                                continue;

                            BarEntry entry = dataSet.getEntryForIndex(j / 4);
                            float val = entry.getY();

                            if (dataSet.isDrawValuesEnabled()) {
                                drawValue(c, dataSet.getValueFormatter(), val, entry, i, x,
                                        val >= 0 ?
                                                (buffer.buffer[j + 1] + posOffset) :
                                                (buffer.buffer[j + 3] + negOffset),
                                        dataSet.getValueTextColor(j / 4));
                            }

                            if (entry.getIcon() != null && dataSet.isDrawIconsEnabled()) {

                                Drawable icon = entry.getIcon();

                                float px = x;
                                float py = val >= 0 ?
                                        (buffer.buffer[j + 1] + posOffset) :
                                        (buffer.buffer[j + 3] + negOffset);

                                px += iconsOffset.x;
                                py += iconsOffset.y;

                                Utils.drawImage(
                                        c,
                                        icon,
                                        (int) px,
                                        (int) py,
                                        icon.getIntrinsicWidth(),
                                        icon.getIntrinsicHeight());
                            }
                        }

                        // if we have stacks
                    } else {

                        Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());

                        int bufferIndex = 0;
                        int index = 0;

                        while (index < dataSet.getEntryCount() * mAnimator.getPhaseX()) {

                            BarEntry entry = dataSet.getEntryForIndex(index);

                            float[] vals = entry.getYVals();
                            float x = (buffer.buffer[bufferIndex] + buffer.buffer[bufferIndex + 2]) / 2f;

                            int color = dataSet.getValueTextColor(index);

                            // we still draw stacked bars, but there is one
                            // non-stacked
                            // in between
                            if (vals == null) {

                                if (!mViewPortHandler.isInBoundsRight(x))
                                    break;

                                if (!mViewPortHandler.isInBoundsY(buffer.buffer[bufferIndex + 1])
                                        || !mViewPortHandler.isInBoundsLeft(x))
                                    continue;

                                if (dataSet.isDrawValuesEnabled()) {
                                    drawValue(c, dataSet.getValueFormatter(), entry.getY(), entry, i, x,
                                            buffer.buffer[bufferIndex + 1] +
                                                    (entry.getY() >= 0 ? posOffset : negOffset),
                                            color);
                                }

                                if (entry.getIcon() != null && dataSet.isDrawIconsEnabled()) {

                                    Drawable icon = entry.getIcon();

                                    float px = x;
                                    float py = buffer.buffer[bufferIndex + 1] +
                                            (entry.getY() >= 0 ? posOffset : negOffset);

                                    px += iconsOffset.x;
                                    py += iconsOffset.y;

                                    Utils.drawImage(
                                            c,
                                            icon,
                                            (int) px,
                                            (int) py,
                                            icon.getIntrinsicWidth(),
                                            icon.getIntrinsicHeight());
                                }

                                // draw stack values
                            } else {

                                float[] transformed = new float[vals.length * 2];

                                float posY = 0f;
                                float negY = -entry.getNegativeSum();

                                for (int k = 0, idx = 0; k < transformed.length; k += 2, idx++) {

                                    float value = vals[idx];
                                    float y;

                                    if (value == 0.0f && (posY == 0.0f || negY == 0.0f)) {
                                        // Take care of the situation of a 0.0 value, which overlaps a non-zero bar
                                        y = value;
                                    } else if (value >= 0.0f) {
                                        posY += value;
                                        y = posY;
                                    } else {
                                        y = negY;
                                        negY -= value;
                                    }

                                    transformed[k + 1] = y * phaseY;
                                }

                                trans.pointValuesToPixel(transformed);

                                for (int k = 0; k < transformed.length; k += 2) {

                                    final float val = vals[k / 2];
                                    final boolean drawBelow =
                                            (val == 0.0f && negY == 0.0f && posY > 0.0f) ||
                                                    val < 0.0f;
                                    float y = transformed[k + 1]
                                            + (drawBelow ? negOffset : posOffset);

                                    if (!mViewPortHandler.isInBoundsRight(x))
                                        break;

                                    if (!mViewPortHandler.isInBoundsY(y)
                                            || !mViewPortHandler.isInBoundsLeft(x))
                                        continue;

                                    if (dataSet.isDrawValuesEnabled()) {
                                        double percentageOccupied = (vals[0] / (vals[0] + vals[1])) * 100;
                                        if (vals[1] != 0 && percentageOccupied > 80 && vals[k / 2] == vals[1]) {
                                            y = y - 5;
                                        } else if (vals[1] != 0 && percentageOccupied > 80 && vals[k / 2] == vals[0]) {
                                            y = y + 30;
                                        }
                                        drawValue(c,
                                                dataSet.getValueFormatter(),
                                                vals[k / 2],
                                                entry,
                                                i,
                                                x,
                                                y,
                                                color);
                                    }

                                    if (entry.getIcon() != null && dataSet.isDrawIconsEnabled()) {

                                        Drawable icon = entry.getIcon();

                                        Utils.drawImage(
                                                c,
                                                icon,
                                                (int) (x + iconsOffset.x),
                                                (int) (y + iconsOffset.y),
                                                icon.getIntrinsicWidth(),
                                                icon.getIntrinsicHeight());
                                    }
                                }
                            }

                            bufferIndex = vals == null ? bufferIndex + 4 : bufferIndex + 4 * vals.length;
                            index++;
                        }
                    }

                    MPPointF.recycleInstance(iconsOffset);
                }
            }

        }
    }
}
