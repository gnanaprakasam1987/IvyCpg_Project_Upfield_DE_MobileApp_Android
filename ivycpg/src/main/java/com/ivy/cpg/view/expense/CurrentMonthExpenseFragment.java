package com.ivy.cpg.view.expense;


import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;

/**
 * @author mansoor
 * To Show Current Expense for the month
 * data from ExpenseMTDDetail table date occurs this month
 */

public class CurrentMonthExpenseFragment extends IvyBaseFragment {

    BusinessModel bmodel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_current_month_expense,
                container, false);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        ExpenseSheetHelper expenseSheetHelper = ExpenseSheetHelper.getInstance(getActivity());

        ExpandedListView list = view.findViewById(R.id.expenses_list);
        TextView tvTotalAmount = view.findViewById(R.id.tvTotalAmount);
        list.setAdapter(new MyAdapter(expenseSheetHelper.getCurrentMonthExpense()));
        tvTotalAmount.setText(sumExpenses(expenseSheetHelper.getCurrentMonthExpense()));

        ((TextView) view.findViewById(R.id.titleTotalamt)).setTypeface(FontUtils.getFontBalooHai(getActivity(), FontUtils.FontType.REGULAR));
        tvTotalAmount.setTypeface(FontUtils.getFontBalooHai(getActivity(), FontUtils.FontType.REGULAR));

        return view;
    }

    public String sumExpenses(ArrayList<ExpenseSheetBO> expenseList) {
        Double sum = 0.0;
        for (ExpenseSheetBO expobj : expenseList)
            sum = sum + SDUtil.convertToDouble(expobj.getAmount());
        return bmodel.formatValue(sum);
    }

    class ViewHolder {
        TextView tvDate, tvExpType, tvAmount, tvProof;
        ImageView ivStatus;
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
        public @NonNull
        View getView(int position, View convertView, @NonNull ViewGroup parent) {
            final ViewHolder holder;

            String VALUE_PENDING = "R";
            String VALUE_ACCEPTED = "S";
            String VALUE_REJECTED = "D";

            if (convertView == null) {

                holder = new ViewHolder();

                LayoutInflater inflater = LayoutInflater.from(getActivity().getBaseContext());

                convertView = inflater.inflate(R.layout.row_expense_sheet, parent, false);

                holder.tvDate = convertView.findViewById(R.id.tv_datevalue);
                holder.tvExpType = convertView.findViewById(R.id.tv_expTypeValue);
                holder.tvAmount = convertView.findViewById(R.id.tv_amountvalue);
                holder.tvProof = convertView.findViewById(R.id.tv_imageproof);
                holder.ivStatus = convertView.findViewById(R.id.iv_status);
                holder.tvProof.setVisibility(View.GONE);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.expenseSheetBO = items.get(position);
            holder.tvDate.setText(DateTimeUtils.convertFromServerDateToRequestedFormat(holder.expenseSheetBO.getDate(),
                    ConfigurationMasterHelper.outDateFormat));
            holder.tvExpType.setText(holder.expenseSheetBO.getTypeName());
            holder.tvAmount.setText(bmodel.formatValue(SDUtil.convertToDouble("" + holder.expenseSheetBO.getAmount())));

            if (holder.expenseSheetBO.getStatus().equalsIgnoreCase(VALUE_ACCEPTED))
                holder.ivStatus.setImageResource(R.drawable.ok_tick);
            if (holder.expenseSheetBO.getStatus().equalsIgnoreCase(VALUE_REJECTED))
                holder.ivStatus.setImageResource(R.drawable.ic_cross_enable);
            if (holder.expenseSheetBO.getStatus().equalsIgnoreCase(VALUE_PENDING))
                holder.ivStatus.setImageResource(R.drawable.ic_pending);

            return convertView;
        }
    }
}
