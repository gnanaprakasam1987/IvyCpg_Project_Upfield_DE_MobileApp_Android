package com.ivy.sd.png.view;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ExpenseSheetBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.DateUtil;

import java.util.ArrayList;


public class PastMonthExpenseFragment extends IvyBaseFragment {

    BusinessModel bmodel;
    private ExpandedListView list;
    private TextView tvTotalAmount;

    private String VALUE_PENDING = "Pending"; //R
    private String VALUE_ACCEPTED = "Accepted"; //S
    private String VALUE_REJECTED = "Rejected"; //D

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_current_month_expense,
                container, false);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        list = view.findViewById(R.id.expenses_list);
        tvTotalAmount = view.findViewById(R.id.tvTotalAmount);
        list.setAdapter(new MyAdapter(bmodel.expenseSheetHelper.getPastMonthExpense()));
        tvTotalAmount.setText(sumExpenses(bmodel.expenseSheetHelper.getPastMonthExpense()));

        ((TextView) view.findViewById(R.id.titleTotalamt)).setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        tvTotalAmount.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

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

    class ViewHolder {
        TextView tvDate, tvExpType, tvAmount, tvProof, tvMonthName;
        ImageView ivStatus;
        ExpenseSheetBO expenseSheetBO;
        LinearLayout llView;
    }

    private class MyAdapter extends ArrayAdapter<ExpenseSheetBO> {
        private ArrayList<ExpenseSheetBO> items;

        public MyAdapter(ArrayList<ExpenseSheetBO> items) {
            super(getActivity(), R.layout.row_expense_sheet);
            this.items = items;
        }

        public ExpenseSheetBO getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            if (convertView == null) {

                holder = new ViewHolder();

                LayoutInflater inflater = LayoutInflater.from(getActivity().getBaseContext());

                convertView = inflater.inflate(R.layout.row_expense_sheet, null);

                holder.tvDate = convertView.findViewById(R.id.tv_datevalue);
                holder.tvExpType = convertView.findViewById(R.id.tv_expTypeValue);
                holder.tvAmount = convertView.findViewById(R.id.tv_amountvalue);
                holder.tvProof = convertView.findViewById(R.id.tv_imageproof);
                holder.ivStatus = convertView.findViewById(R.id.iv_status);
                holder.tvProof.setVisibility(View.GONE);

                holder.tvMonthName = convertView.findViewById(R.id.tvMonthName);
                holder.llView = convertView.findViewById(R.id.llview);
                holder.tvMonthName.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.expenseSheetBO = items.get(position);


            if (holder.expenseSheetBO.isMonthName()) {
                holder.llView.setVisibility(View.GONE);
                holder.tvMonthName.setVisibility(View.VISIBLE);

                holder.tvMonthName.setText(holder.expenseSheetBO.getTypeName());
            } else {
                holder.llView.setVisibility(View.VISIBLE);
                holder.tvMonthName.setVisibility(View.GONE);
                holder.tvDate.setText(DateUtil.convertFromServerDateToRequestedFormat(holder.expenseSheetBO.getDate(),
                        ConfigurationMasterHelper.outDateFormat));
                holder.tvExpType.setText(holder.expenseSheetBO.getTypeName());
                holder.tvAmount.setText(bmodel.formatValue(Double.parseDouble("" + holder.expenseSheetBO.getAmount())));

                if (holder.expenseSheetBO.getStatus().equalsIgnoreCase("S"))
                    holder.ivStatus.setImageResource(R.drawable.ok_tick);
                if (holder.expenseSheetBO.getStatus().equalsIgnoreCase("D"))
                    holder.ivStatus.setImageResource(R.drawable.ic_cross_enable);
                if (holder.expenseSheetBO.getStatus().equalsIgnoreCase("R"))
                    holder.ivStatus.setImageResource(R.drawable.ic_pending);

            }

            return convertView;
        }
    }
}
