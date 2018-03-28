package com.ivy.sd.png.view;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ExpenseSheetBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.DateUtil;

import java.util.ArrayList;


public class CurrentMonthExpenseFragment extends IvyBaseFragment {

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
        list.setAdapter(new MyAdapter(bmodel.expenseSheetHelper.getCurrentMonthExpense()));
        tvTotalAmount.setText(sumExpenses(bmodel.expenseSheetHelper.getCurrentMonthExpense()));

        ((TextView) view.findViewById(R.id.titleTotalamt)).setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        tvTotalAmount.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        return view;
    }

    public String sumExpenses(ArrayList<ExpenseSheetBO> expenseList) {
        Double sum = 0.0;
        for (ExpenseSheetBO expobj : expenseList)
            sum = sum + Double.parseDouble(expobj.getAmount());
        return String.format("%.2f", sum);
    }

    class ViewHolder {
        TextView tvDate, tvExpType, tvAmount, tvProof;
        ImageView tvStatus;
        ExpenseSheetBO expenseSheetBO;
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
                holder.tvStatus = convertView.findViewById(R.id.tv_status);
                holder.tvProof.setVisibility(View.GONE);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.expenseSheetBO = items.get(position);
            holder.tvDate.setText(DateUtil.convertFromServerDateToRequestedFormat(holder.expenseSheetBO.getDate(),
                    ConfigurationMasterHelper.outDateFormat));
            holder.tvExpType.setText(holder.expenseSheetBO.getTypeName());
            holder.tvAmount.setText(bmodel.formatValue(Double.parseDouble("" + holder.expenseSheetBO.getAmount())));

            if (holder.expenseSheetBO.getStatus().equalsIgnoreCase("S"))
                holder.tvStatus.setImageResource(R.drawable.ok_tick);
            if (holder.expenseSheetBO.getStatus().equalsIgnoreCase("D"))
                holder.tvStatus.setImageResource(R.drawable.ic_cross_enable);
            if (holder.expenseSheetBO.getStatus().equalsIgnoreCase("R"))
                holder.tvStatus.setImageResource(R.drawable.ic_image_camera_alt_blk);

            return convertView;
        }
    }
}
