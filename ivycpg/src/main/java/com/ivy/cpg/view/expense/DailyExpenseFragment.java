package com.ivy.cpg.view.expense;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.DateUtil;
import com.ivy.sd.png.view.HomeScreenFragment;

import java.io.File;
import java.util.ArrayList;

public class DailyExpenseFragment extends IvyBaseFragment {


    BusinessModel bmodel;

    private String photoNamePath;
    private TextView tvTotalAmount;

    private ExpandedListView list;
    ExpenseProofDialog dialogFragment;

    private ExpenseSheetHelper expenseSheetHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_daily_expense,
                container, false);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        expenseSheetHelper = ExpenseSheetHelper.getInstance(getActivity());

        photoNamePath = HomeScreenFragment.photoPath + "/";


        tvTotalAmount = view.findViewById(R.id.tvTotalAmount);
        list = view.findViewById(R.id.expenses_list);

        ((TextView) view.findViewById(R.id.tvTitleTotal)).setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        tvTotalAmount.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        loadExpensesList();


    }

    private void loadExpensesList(String date) {
        String Tid = expenseSheetHelper.checkExpenseHeader(date);
        if (Tid.length() > 0) {
            ArrayList<ExpensesBO> selectedDateExpenses = expenseSheetHelper.getSelectedExpenses(Tid);
            if (selectedDateExpenses.size() == 0)
                expenseSheetHelper.deleteHeader(Tid);
            loadExpensesList();

        }
    }

    private void loadExpensesList() {
        int count;
        count = expenseSheetHelper.checkExpenseHeader();
        if (count > 0) {

            ArrayList<ExpensesBO> selectedDateExpenses = expenseSheetHelper.getAllExpenses();
            if (selectedDateExpenses.size() > 0) {
                list.setVisibility(View.VISIBLE);
                MyAdapter adapter = new MyAdapter(selectedDateExpenses);
                list.setAdapter(adapter);
                double total_amount = 0;
                for (ExpensesBO expAmount : selectedDateExpenses) {
                    total_amount = total_amount + SDUtil.convertToDouble(expAmount.getAmount());
                }
                //tvTotalAmount.setText(String.format("%.2f", total_amount));
                tvTotalAmount.setText(bmodel.formatValue(total_amount));
            }
        } else {
            list.setVisibility(View.GONE);
            tvTotalAmount.setText("0.0");
        }
    }


    class ViewHolder {
        TextView tvDate, tvExpType, tvAmount, tvProof;
        ExpensesBO expensesBO;
    }

    private class MyAdapter extends ArrayAdapter<ExpensesBO> {
        private ArrayList<ExpensesBO> items;

        public MyAdapter(ArrayList<ExpensesBO> items) {
            super(getActivity(), R.layout.row_expense_sheet);
            this.items = items;
        }

        public ExpensesBO getItem(int position) {
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

            if (convertView == null) {

                holder = new ViewHolder();

                LayoutInflater inflater = LayoutInflater.from(getActivity());

                convertView = inflater.inflate(R.layout.row_expense_sheet, parent, false);

                holder.tvDate = convertView.findViewById(R.id.tv_datevalue);
                holder.tvExpType = convertView.findViewById(R.id.tv_expTypeValue);
                holder.tvAmount = convertView.findViewById(R.id.tv_amountvalue);
                holder.tvProof = convertView.findViewById(R.id.tv_imageproof);

                holder.tvProof.setPaintFlags(holder.tvProof.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

                convertView.findViewById(R.id.iv_status).setVisibility(View.GONE);
                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    showAlert(getResources().getString(R.string.do_you_want_to_delete_this_expense),
                            holder.expensesBO);
                    return true;
                }
            });
            holder.expensesBO = items.get(position);
            holder.tvDate.setText(DateUtil.convertFromServerDateToRequestedFormat(holder.expensesBO.getDate(),
                    ConfigurationMasterHelper.outDateFormat));

            holder.tvExpType.setText(holder.expensesBO.getTypeName());
            holder.tvAmount.setText(bmodel.formatValue(SDUtil.convertToDouble("" + holder.expensesBO.getAmount())));

            holder.tvProof.setText("" + holder.expensesBO.getImageList().size());

            holder.tvProof.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (holder.expensesBO.getImageList().size() > 0) {


                        if (dialogFragment == null) {
                            dialogFragment = new ExpenseProofDialog();
                            dialogFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    loadExpensesList();
                                    dialogFragment = null;
                                }
                            });
                            Bundle args = new Bundle();
                            args.putString("refId", holder.expensesBO.getRefId());
                            dialogFragment.setArguments(args);
                            dialogFragment.show(getActivity().getSupportFragmentManager(), "ExpenseDilogFragement");
                        }
                    }
                }
            });

            return convertView;
        }

    }

    public void showAlert(String title, final ExpensesBO expensesBO) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setPositiveButton(getResources().getString(R.string.ok),
                new android.content.DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        new DeleteAsyncTask(expensesBO).execute();
                    }

                });
        builder.setNegativeButton(getResources().getString(R.string.cancel),
                new android.content.DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                    }

                });
        bmodel.applyAlertDialogTheme(builder);
    }

    public void refresh() {
        loadExpensesList();
    }

    class DeleteAsyncTask extends AsyncTask<String, Integer, Boolean> {
        private ProgressDialog progressDialogue;
        ExpensesBO expensesBO;

        DeleteAsyncTask(ExpensesBO expensesBO) {
            this.expensesBO = expensesBO;
        }

        @Override
        protected Boolean doInBackground(String... arg0) {
            try {
                if (expensesBO.getImageList().size() > 0) {
                    for (String imagename : expensesBO.getImageList()) {
                        String imgpath = new File(photoNamePath + "/" + imagename).getAbsolutePath();
                        new File(imgpath).delete();
                    }
                }
                expenseSheetHelper.deleteExpense(expensesBO.getRefId(), expensesBO.getTid(),
                        expensesBO.getDate(), expensesBO.getAmount());

                return Boolean.TRUE;
            } catch (Exception e) {
                Commons.printException(e);
                return Boolean.FALSE;
            }

        }

        protected void onPreExecute() {
            progressDialogue = ProgressDialog.show(getActivity(),
                    DataMembers.SD, getResources().getString(R.string.saving),
                    true, false);
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground

            progressDialogue.dismiss();
            loadExpensesList(expensesBO.getDate());

        }

    }

}
