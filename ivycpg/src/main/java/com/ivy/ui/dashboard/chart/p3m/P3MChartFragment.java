package com.ivy.ui.dashboard.chart.p3m;

import android.graphics.Color;
import android.view.View;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.DefaultFillFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.ivy.core.base.view.BaseFragment;
import com.ivy.cpg.view.dashboard.DashBoardBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.FontUtils;
import com.ivy.utils.event.DashBoardEventData;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Iterator;

import butterknife.BindView;

import static com.ivy.ui.dashboard.view.SellerDashboardFragment.DASHBOARD;

public class P3MChartFragment extends BaseFragment  {

    @BindView(R.id.chart1)
    LineChart mChart;

    private int paramLovId;

    ArrayList<DashBoardBO> dashBoardBOArrayList;

    @Override
    public void initializeDi() {

    }

    @Override
    protected int setContentViewLayout() {
        return R.layout.fragment_viewpager_chart;
    }

    @Override
    public void init(View view) {

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
    protected void getMessageFromAliens() {
        if (getArguments() != null) {
            dashBoardBOArrayList = (ArrayList<DashBoardBO>) getArguments().getSerializable("dashChartList");
            paramLovId = getArguments().getInt("paramLovId");
        }

        filterData();
    }

    private void filterData() {
        Iterator<DashBoardBO> i = dashBoardBOArrayList.iterator();
        while (i.hasNext()) {
            DashBoardBO s = i.next(); // must be called before you can call i.remove()
            if (s.getKpiTypeLovID() != paramLovId)
                i.remove();
        }
    }

    @Subscribe
    public void onMessageEvent(DashBoardEventData event) {
        if(event.getSource().equalsIgnoreCase(DASHBOARD))
            paramLovId = event.getKpiLovId();

        filterData();
        setData();

    }

    @Override
    protected void setUpViews() {

        mChart.setDrawGridBackground(false);
        mChart.getDescription().setEnabled(false);
        mChart.setDrawBorders(false);

        mChart.getAxisRight().setEnabled(false);

        mChart.getAxisLeft().setDrawAxisLine(false);
        mChart.getAxisLeft().setDrawGridLines(true);
        mChart.getAxisLeft().setGridColor(Color.WHITE);
        mChart.getAxisLeft().setTextColor(Color.WHITE);
        mChart.getAxisLeft().setTextSize(10f);
        mChart.getAxisLeft().setTypeface(FontUtils.getFontRoboto(getActivity(),FontUtils.FontType.MEDIUM));


        mChart.getXAxis().setDrawAxisLine(false);
        mChart.getXAxis().setDrawGridLines(false);
        mChart.getXAxis().setGranularity(1f);
        mChart.getXAxis().setTextColor(Color.WHITE);
        mChart.getXAxis().setTextSize(10f);
        mChart.getXAxis().setTypeface(FontUtils.getFontRoboto(getActivity(),FontUtils.FontType.MEDIUM));
        mChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        mChart.getLegend().setEnabled(false);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(false);
        mChart.setScaleEnabled(false);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(false);

        setData();

    }

    private void setData(){
        mChart.clear();
        mChart.clearValues();
        if (dashBoardBOArrayList.isEmpty()) {
            mChart.getXAxis().setValueFormatter((value, axis) -> dashBoardBOArrayList.get((int) value).getMonthName());

            mChart.getAxisLeft().setValueFormatter(new PercentFormatter());
        }

        ArrayList<Entry> achivedValues = new ArrayList<>();
        for (int i = 0; i < dashBoardBOArrayList.size(); i++) {
            try {
                achivedValues.add(new Entry(i, SDUtil.convertToFloat("" + dashBoardBOArrayList.get(i).getConvAcheivedPercentage()), dashBoardBOArrayList.get(i).getText()));
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
            set1 = new LineDataSet(achivedValues, dashBoardBOArrayList.get(0).getText() + " - Achieved");
            set1.setLineWidth(1f);
            set1.setCircleRadius(4f);
            set1.setDrawCircleHole(false);
            set1.setValueTextSize(9f);
            set1.setValueTypeface(FontUtils.getFontRoboto(getActivity(),FontUtils.FontType.MEDIUM));
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

            mChart.animateY(1400);
        }
    }


}
