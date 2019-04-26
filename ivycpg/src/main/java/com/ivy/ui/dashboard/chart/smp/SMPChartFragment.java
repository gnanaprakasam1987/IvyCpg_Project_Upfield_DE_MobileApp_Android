package com.ivy.ui.dashboard.chart.smp;

import android.graphics.Color;
import android.graphics.Point;
import android.view.Display;
import android.view.View;
import android.widget.LinearLayout;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.ivy.core.base.view.BaseFragment;
import com.ivy.cpg.view.dashboard.DashBoardBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.utils.FontUtils;
import com.ivy.utils.event.DashBoardEventData;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import butterknife.BindView;

import static com.ivy.ui.dashboard.view.SellerDashboardFragment.DASHBOARD;

public class SMPChartFragment extends BaseFragment {

    @BindView(R.id.chart1)
    PieChart mChart;

    private DashBoardBO dashBoardBO;

    private boolean isSemiCircleChartRequired;

    @Override
    public void initializeDi() {

    }

    @Override
    protected int setContentViewLayout() {
        return R.layout.fragment_viewpager_piechart;
    }

    @Override
    public void init(View view) {

    }

    @Override
    protected void getMessageFromAliens() {

        if (getArguments() != null) {
            dashBoardBO = (DashBoardBO) getArguments().getSerializable("dashboardData");
        }

    }


    @Subscribe
    public void onMessageEvent(DashBoardEventData event) {
        if (event.getSource().equalsIgnoreCase(DASHBOARD))
            dashBoardBO = event.getSmpDashBoardData();

        setData();

    }


    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);

    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    protected void setUpViews() {

        if (dashBoardBO != null && dashBoardBO.getKpiAcheived() != null && dashBoardBO.getKpiTarget() != null) {
            isSemiCircleChartRequired = (dashBoardBO.getFlex1() == 0);

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
            mChart.setEntryLabelTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
            mChart.setEntryLabelTextSize(0f);
            setData();
        }

    }


    private void setData() {
        if (dashBoardBO.getKpiTarget() != null && !dashBoardBO.getKpiTarget().equals("0")) {
            mChart.clear();
            mChart.clearValues();
            ArrayList<PieEntry> entries = new ArrayList<PieEntry>();
            float temp_ach = SDUtil.convertToFloat(dashBoardBO.getKpiAcheived()) - SDUtil.convertToFloat(dashBoardBO.getKpiTarget());
            if (temp_ach > 0) {
                int round = Math.round(SDUtil.convertToFloat(dashBoardBO.getKpiAcheived()) /
                        (SDUtil.convertToFloat(dashBoardBO.getKpiTarget())) * 100);
                if (round % 100 == 0) {
                    round = round + 1;
                }
                int rounded = ((round + 99) / 100) * 100;
                entries.add(new PieEntry(SDUtil.convertToFloat(dashBoardBO.getKpiAcheived()), "100%"));

                int bonus = Math.round(SDUtil.convertToFloat(dashBoardBO.getKpiAcheived()) /
                        (SDUtil.convertToFloat(dashBoardBO.getKpiTarget())) * 100);
                entries.add(new PieEntry(temp_ach, bonus + "%"));

                entries.add(new PieEntry(SDUtil.convertToFloat(dashBoardBO.getKpiTarget()), (rounded) + "%"));
            } else {
                int bonus = Math.round(SDUtil.convertToFloat(dashBoardBO.getKpiAcheived()) /
                        (SDUtil.convertToFloat(dashBoardBO.getKpiTarget())) * 100);
                entries.add(new PieEntry(SDUtil.convertToFloat(dashBoardBO.getKpiAcheived()), bonus + "%"));
                entries.add(new PieEntry(SDUtil.convertToFloat(dashBoardBO.getKpiTarget()), "100%"));
            }


            PieDataSet dataSet = new PieDataSet(entries, "");

            dataSet.setSliceSpace(isSemiCircleChartRequired ? 0f : 3f);
            dataSet.setSelectionShift(5f);

            // add a lot of colors

            ArrayList<Integer> colors = new ArrayList<Integer>();

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
            data.setValueTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
            mChart.setData(data);
            mChart.invalidate();
        }
    }

    private void setOffset(PieChart mChart) {
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
