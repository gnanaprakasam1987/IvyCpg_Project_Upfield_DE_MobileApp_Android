package com.ivy.sd.png.view;


import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;
import com.itextpdf.text.List;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ExpenseSheetBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class PastMonthExpenseFragment extends IvyBaseFragment {

    BusinessModel bmodel;
    private TextView tvTotalAmount;
    BarChart mChart;
    PieChart pieChart;
    ImageView ivClosePieChart;
    RelativeLayout rlPieChart;

    private ArrayList<ExpMonthWiseBo> wiseBosmonth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_pastmonth_layout,
                container, false);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        mChart = view.findViewById(R.id.barchart);
        pieChart = view.findViewById(R.id.pieChart);
        ivClosePieChart = view.findViewById(R.id.iv_close_piechart);
        rlPieChart = view.findViewById(R.id.rl_piechart);
        setData();


        tvTotalAmount = view.findViewById(R.id.tvTotalAmount);
        tvTotalAmount.setText(sumExpenses(bmodel.expenseSheetHelper.getPastMonthExpense()));

        tvTotalAmount.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        ivClosePieChart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                rlPieChart.setVisibility(View.GONE);
                mChart.setVisibility(View.VISIBLE);
                tvTotalAmount.setText(sumExpenses(bmodel.expenseSheetHelper.getPastMonthExpense()));
            }
        });

        mChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {

                rlPieChart.setVisibility(View.VISIBLE);
                mChart.setVisibility(View.GONE);

                setDataPieChart(e.getData().toString());
            }

            @Override
            public void onNothingSelected() {

            }
        });

        return view;
    }

    public String sumExpenses(ArrayList<ExpenseSheetBO> expenseList) {
        Double sum = 0.0;
        for (ExpenseSheetBO expobj : expenseList) {
            sum = sum + SDUtil.convertToDouble(expobj.getAmount());
        }
        return bmodel.formatValue(sum);
    }


    public ArrayList<ExpMonthWiseBo> sumExpensesMonthWise(ArrayList<ExpenseSheetBO> expenseList) {

        ArrayList<ExpMonthWiseBo> expMonthWiseBos = new ArrayList<>();

        Double sum = 0.0;
        String monthName = "";

        for (int i = 0; i < expenseList.size(); i++) {
            ExpenseSheetBO expobj = expenseList.get(i);

            if (monthName.length() == 0)
                sum = sum + SDUtil.convertToDouble(expobj.getAmount());

            else if (monthName.equals(expobj.getMonth()))
                sum = sum + SDUtil.convertToDouble(expobj.getAmount());

            else if (!monthName.equals(expobj.getMonth())) {
                expMonthWiseBos.add(new ExpMonthWiseBo(monthName, bmodel.formatValue(sum)));
                sum = 0.0;
                sum = sum + SDUtil.convertToDouble(expobj.getAmount());
            }
            monthName = expobj.getMonth();

            if (i == expenseList.size() - 1) {
                expMonthWiseBos.add(new ExpMonthWiseBo(monthName, bmodel.formatValue(sum)));
            }


        }
        return expMonthWiseBos;
    }


    public ArrayList<ExpMonthWiseBo> sumExpensesMonthWiseExpence(ArrayList<ExpenseSheetBO> expenseList) {

        ArrayList<ExpMonthWiseBo> expMonthWiseBos = new ArrayList<>();
        HashMap<String, Double> expTypeMap = new HashMap<>();
        for (int i = 0; i < expenseList.size(); i++) {
            ExpenseSheetBO expobj = expenseList.get(i);

            if (expTypeMap.get(expobj.getTypeName()) == null)
                expTypeMap.put(expobj.getTypeName(), SDUtil.convertToDouble(expobj.getAmount()));

            else if (expTypeMap.get(expobj.getTypeName()) != null) {
                double amt = expTypeMap.get(expobj.getTypeName());
                amt = amt + SDUtil.convertToDouble(expobj.getAmount());
                expTypeMap.put(expobj.getTypeName(), amt);
            }
        }
        for (Map.Entry<String, Double> entry : expTypeMap.entrySet()) {
            expMonthWiseBos.add(new ExpMonthWiseBo(entry.getKey(), bmodel.formatValue(entry.getValue())));
        }


        return expMonthWiseBos;
    }

    private class ExpMonthWiseBo {

        private String monthName, amount;

        public ExpMonthWiseBo(String monthName, String amount) {
            this.monthName = monthName;
            this.amount = amount;
        }

        public String getMonthName() {
            return monthName;
        }

        public void setMonthName(String monthName) {
            this.monthName = monthName;
        }

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }


    }

    private void setData() {

        try {
            ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
            wiseBosmonth = sumExpensesMonthWise(bmodel.expenseSheetHelper.getPastMonthExpense());
            int i = 0;
            String[] monthsName = new String[12];
            for (ExpMonthWiseBo expMonthWiseBo : wiseBosmonth) {
                yVals1.add(new BarEntry(i, SDUtil.convertToFloat(expMonthWiseBo.getAmount()), expMonthWiseBo.getMonthName()));
                monthsName[i] = expMonthWiseBo.getMonthName();
                monthsName[i] = expMonthWiseBo.getMonthName();
                i++;

            }

            BarDataSet set1;
            set1 = new BarDataSet(yVals1, "");
            set1.setDrawIcons(false);
            set1.setColors(ColorTemplate.MATERIAL_COLORS);

            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);

            BarData data = new BarData(dataSets);
            data.setValueTextSize(11f);
            data.setBarWidth(0.4f);

            mChart.getDescription().setEnabled(false);


            YAxis leftAxis = mChart.getAxisLeft();
            leftAxis.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)


            mChart.setData(data);
            mChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(monthsName));
            mChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            mChart.getXAxis().setGranularity(1f);
            mChart.getXAxis().setGranularityEnabled(true);
            mChart.setDrawGridBackground(false);
            mChart.setDrawBarShadow(false);
            mChart.getAxisRight().setEnabled(false);

            mChart.getLegend().setEnabled(false);
            mChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setDataPieChart(String month) {

        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();

        double total = 0.0;

        for (ExpMonthWiseBo wiseBo : wiseBosmonth) {
            if (wiseBo.getMonthName().equals(month + "")) {
                total = SDUtil.convertToDouble(wiseBo.getAmount());
            }
        }

        try {

            ArrayList<ExpenseSheetBO> monthExpenses = new ArrayList<>();
            for (ExpenseSheetBO expBp : bmodel.expenseSheetHelper.getPastMonthExpense()) {
                if (month.equals(expBp.getMonth())) {
                    monthExpenses.add(expBp);
                }
            }
            ArrayList<ExpMonthWiseBo> wiseBos = sumExpensesMonthWiseExpence(monthExpenses);
            for (ExpMonthWiseBo expMonthWiseBo : wiseBos) {
                if (SDUtil.convertToDouble(expMonthWiseBo.getAmount()) > 0) {
                    double percentage = (SDUtil.convertToDouble(expMonthWiseBo.amount) / total) * 100;
                    entries.add(new PieEntry((float) percentage, expMonthWiseBo.getMonthName() + "\n" + expMonthWiseBo.amount));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        PieDataSet dataSet = new PieDataSet(entries, "");

        dataSet.setDrawIcons(false);
        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSelectionShift(5f);

        // add a lot of colors

        ArrayList<Integer> colors = new ArrayList<Integer>();


        for (int c : ColorTemplate.MATERIAL_COLORS)
            colors.add(c);


        dataSet.setColors(colors);

        pieChart.getDescription().setEnabled(false);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setEntryLabelTextSize(11f);
        pieChart.setEntryLabelTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(12f);
        data.setValueTextColor(Color.BLACK);
        data.setValueTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        pieChart.setData(data);

        // undo all highlights
        pieChart.highlightValues(null);
        pieChart.invalidate();

        pieChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);

        pieChart.setCenterText(month);
        pieChart.setCenterTextTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        pieChart.getLegend().setEnabled(false);

        tvTotalAmount.setText(bmodel.formatValue(total));
    }


}
