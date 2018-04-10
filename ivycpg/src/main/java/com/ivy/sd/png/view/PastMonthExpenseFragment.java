package com.ivy.sd.png.view;


import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
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
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;


public class PastMonthExpenseFragment extends IvyBaseFragment {

    BusinessModel bmodel;
    private ExpandedListView list;
    private TextView tvTotalAmount, tvMonthNamePieChart;
    BarChart mChart;
    PieChart pieChart;
    ImageView ivClosePieChart;
    RelativeLayout rlPieChart;
    private String VALUE_PENDING = "Pending"; //R
    private String VALUE_ACCEPTED = "Accepted"; //S
    private String VALUE_REJECTED = "Rejected"; //D

    private ArrayList<ExpMonthWiseBo> wiseBosmonth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_pastmonth_layout,
                container, false);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        mChart = (BarChart) view.findViewById(R.id.barchart);
        pieChart = (PieChart) view.findViewById(R.id.pieChart);
        ivClosePieChart = (ImageView) view.findViewById(R.id.iv_close_piechart);
        tvMonthNamePieChart = (TextView) view.findViewById(R.id.tv_month_name);
        rlPieChart = (RelativeLayout) view.findViewById(R.id.rl_piechart);
        setData(10, 20.2f);


        tvTotalAmount = view.findViewById(R.id.tvTotalAmount);
        tvTotalAmount.setText(sumExpenses(bmodel.expenseSheetHelper.getPastMonthExpense()));

        tvTotalAmount.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        ivClosePieChart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                rlPieChart.setVisibility(View.GONE);
                mChart.setVisibility(View.VISIBLE);
            }
        });

        mChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {

                rlPieChart.setVisibility(View.VISIBLE);
                mChart.setVisibility(View.GONE);

                setDataPieChart(Integer.parseInt(e.getData().toString()));
                tvMonthNamePieChart.setText(bmodel.expenseSheetHelper.MONTH_NAME[Integer.parseInt(e.getData().toString())]);
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
            if (!expobj.isMonthName())
                sum = sum + Double.parseDouble(expobj.getAmount());
        }
        return bmodel.formatValue(sum);
    }


    public ArrayList<ExpMonthWiseBo> sumExpensesMonthWise(ArrayList<ExpenseSheetBO> expenseList) throws ParseException {

        ArrayList<ExpMonthWiseBo> expMonthWiseBos = new ArrayList<>();
        ArrayList<Integer> monthArray = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();

        for (int i = 0; i < 3; i++) {
            calendar.add(Calendar.MONTH, -1);
            monthArray.add(calendar.get(Calendar.MONTH));
        }
        for (Integer integer : monthArray) {
            Double sum = 0.0;
            for (ExpenseSheetBO expobj : expenseList) {

                String date1 = expobj.getDate() == null ? "2018/04/09" : expobj.getDate();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                Date date = dateFormat.parse(date1);
                Calendar calendar1 = Calendar.getInstance();
                calendar1.setTime(date);

                int month = calendar1.get(Calendar.MONTH);
                if (!expobj.isMonthName() && integer == month)
                    sum = sum + Double.parseDouble(expobj.getAmount());

            }
            expMonthWiseBos.add(new ExpMonthWiseBo("" + integer, sum));

        }
        return expMonthWiseBos;
    }


    public ArrayList<ExpMonthWiseBo> sumExpensesMonthWiseExpence(ArrayList<ExpenseSheetBO> expenseList, int month) throws ParseException {

        ArrayList<ExpMonthWiseBo> expMonthWiseBos = new ArrayList<>();
        ArrayList<String> expTypeArray = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();

        for (ExpenseSheetBO sheetBO : expenseList) {
            String name = (sheetBO.getTypeName() == null || sheetBO.getTypeName().isEmpty()) ? "" : sheetBO.getTypeName();
            expTypeArray.add(name);
        }
        for (String expType : new HashSet<String>(expTypeArray)) {
            Double sum = 0.0;
            for (ExpenseSheetBO expobj : expenseList) {

                String date1 = expobj.getDate() == null ? "2018/04/09" : expobj.getDate();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                Date date = dateFormat.parse(date1);
                Calendar calendar1 = Calendar.getInstance();
                calendar1.setTime(date);

                if (expType.equals(expobj.getTypeName()) && calendar1.get(Calendar.MONTH) == month) {
                    sum = sum + Double.parseDouble(expobj.getAmount());
                }
            }
            expMonthWiseBos.add(new ExpMonthWiseBo("" + expType, sum));

        }
        return expMonthWiseBos;
    }

    private class ExpMonthWiseBo {

        private String monthName;
        private String expType;

        public ExpMonthWiseBo(String monthName, double amount) {
            this.monthName = monthName;
            this.amount = amount;
        }

        public String getMonthName() {
            return monthName;
        }

        public void setMonthName(String monthName) {
            this.monthName = monthName;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        private double amount;

    }

    private void setData(int count, float range) {

        float start = 1f;
        try {
            ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
            DecimalFormat decimalFormat = new DecimalFormat("#");
            wiseBosmonth = sumExpensesMonthWise(bmodel.expenseSheetHelper.getPastMonthExpense());
            int i = 0;

            for (ExpMonthWiseBo expMonthWiseBo : wiseBosmonth) {
                yVals1.add(new BarEntry(i, Integer.parseInt(decimalFormat.format(expMonthWiseBo.getAmount()) + ""), expMonthWiseBo.getMonthName()));
                i++;
            }

            BarDataSet set1;
            set1 = new BarDataSet(yVals1, "");
            set1.setDrawIcons(false);
            ArrayList<String> monthNameList = new ArrayList<>();
            for (ExpMonthWiseBo expMonthWiseBo : wiseBosmonth) {
                monthNameList.add(bmodel.expenseSheetHelper.MONTH_NAME[Integer.parseInt(expMonthWiseBo.getMonthName())]);
            }

            set1.setColors(ColorTemplate.MATERIAL_COLORS);

//            set1.setStackLabels(strings);
            ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
            dataSets.add(set1);

            BarData data = new BarData(dataSets);
            data.setValueTextSize(10f);
            data.setBarWidth(0.5f);

            mChart.setData(data);
            mChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(monthNameList.toArray(new String[monthNameList.size()])));
            mChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void setDataPieChart(int month) {

        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();

        double total = 0.0;

        for (ExpMonthWiseBo wiseBo : wiseBosmonth) {
            if (wiseBo.getMonthName().equals(month + "")) {
                total = wiseBo.getAmount();
            }
        }

        try {

            ArrayList<ExpMonthWiseBo> wiseBos = sumExpensesMonthWiseExpence(bmodel.expenseSheetHelper.getPastMonthExpense(), month);
            for (ExpMonthWiseBo expMonthWiseBo : wiseBos) {
                if (expMonthWiseBo.getAmount() > 0) {
                    double percentage = (expMonthWiseBo.amount / total) * 100;
                    entries.add(new PieEntry((float) percentage, expMonthWiseBo.getMonthName() + "\n" + expMonthWiseBo.amount));
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        PieDataSet dataSet = new PieDataSet(entries, "");

        dataSet.setDrawIcons(false);
        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSelectionShift(5f);

        // add a lot of colors

        ArrayList<Integer> colors = new ArrayList<Integer>();


        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);


        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);


        colors.add(ColorTemplate.getHoloBlue());

        dataSet.setColors(colors);
        //dataSet.setSelectionShift(0f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);
//        data.setValueTypeface(mTfLight);
        pieChart.setData(data);

        // undo all highlights
        pieChart.highlightValues(null);

        pieChart.invalidate();
    }


}
