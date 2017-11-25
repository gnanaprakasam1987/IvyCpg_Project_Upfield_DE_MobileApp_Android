package com.ivy.sd.png.view;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.StoreWsieDiscountBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class StoreWiseDiscountDialog extends Dialog implements OnClickListener {
    private Context context;

    private ListView lvwplist;
    private BusinessModel bmodel;
    private OrderSummary initAct;
    private ArrayList<StoreWsieDiscountBO> discountlist;
    private LinearLayout discountll;
    private Button ok, cancel;
    private OnMyDialogResult mDialogResult;
    private int type, discountId, isCompanygiven;
    private MyAdapter adapter;
    private int numberOfCheckboxesChecked;
    private double value,discount;
    private int dtype;
    private DecimalFormat df;

    public StoreWiseDiscountDialog(Context context,
                                   OnMyDialogResult onmydailogresult, double value, int dtype) {
        super(context);
        this.context = context;
        initAct = (OrderSummary) context;

        mDialogResult = onmydailogresult;
        this.value = value;
        this.dtype = dtype;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout ll = (LinearLayout) LayoutInflater.from(context).inflate(
                R.layout.dialog_store_wise_discount, null);
        setContentView(ll);
        setCancelable(true);
        lvwplist = (ListView) findViewById(R.id.list);
        bmodel = (BusinessModel) context.getApplicationContext();
        discountll = (LinearLayout) findViewById(R.id.discountlayout);
        ok = (Button) findViewById(R.id.btn_ok);
        ok.setOnClickListener(this);
        cancel = (Button) findViewById(R.id.btn_cancel);
        cancel.setOnClickListener(this);
        bmodel.productHelper.downloadBillwiseDiscount();
        discountlist = bmodel.productHelper.getBillWiseDiscountList();

        Commons.print("list size" + discountlist.size());
        // UpdateDialog();
        adapter = new MyAdapter(discountlist);
        lvwplist.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        lvwplist.setAdapter(adapter);
        lvwplist.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                StoreWsieDiscountBO disbo = (StoreWsieDiscountBO) parent
                        .getSelectedItem();
                discount = disbo.getDiscount();
                type = disbo.getIsPercentage();
                discountId = disbo.getDiscountId();
                isCompanygiven = disbo.getIsCompanyGiven();
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        df = new DecimalFormat("###.#");
    }


    StoreWsieDiscountBO discountbo;

    class MyAdapter extends ArrayAdapter<StoreWsieDiscountBO> {
        ArrayList<StoreWsieDiscountBO> items;

        MyAdapter(ArrayList<StoreWsieDiscountBO> items) {
            super(context, R.layout.dialog_store_wise_discount_row, items);
            this.items = items;
        }

        public StoreWsieDiscountBO getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            discountbo = (StoreWsieDiscountBO) items.get(position);
            final ViewHolder holder;
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(R.layout.dialog_store_wise_discount_row,
                        parent, false);
                holder = new ViewHolder();
                holder.discountck = (CheckBox) row
                        .findViewById(R.id.list_item_check_box);

                holder.discountck.setPadding(holder.discountck.getPaddingLeft()
                                + (int) (10.0f * 5 + 0.5f),
                        holder.discountck.getPaddingTop(),
                        holder.discountck.getPaddingRight(),
                        holder.discountck.getPaddingBottom());
                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.discountck
                    .setOnCheckedChangeListener(new OnCheckedChangeListener() {

                        public void onCheckedChanged(CompoundButton buttonView,
                                                     boolean isChecked) {
                            if (isChecked && numberOfCheckboxesChecked >= 1) {
                                holder.discountck.setChecked(false);
                            } else {
                                if (isChecked) {
                                    discount = holder.discountBO.getDiscount();
                                    if (holder.discountBO.getIsPercentage() == 1)
                                        type = 1;
                                    else
                                        type = 2;
                                    discountId = holder.discountBO.getDiscountId();
                                    isCompanygiven = holder.discountBO.getIsCompanyGiven();
                                    numberOfCheckboxesChecked++;

                                    bmodel.setDiscountlist(holder.discountBO);
                                } else {
                                    numberOfCheckboxesChecked--;

                                }
                            }
                        }
                    });

            holder.position = position;
            holder.discountBO = discountbo;
            Commons.print("position" + position);
            if (holder.discountBO != null)
                if (String.valueOf(holder.discountBO.getDiscount())
                        .equalsIgnoreCase(df.format(value))) {
                    holder.discountck.setChecked(true);
                    Commons.print("checked");
                } else {
                    Commons.print("not checked"
                            + holder.discountBO.getDiscount() + " " + value);
                    holder.discountck.setChecked(false);
                }
            holder.discountck.setText(discountbo.getDiscount() + "");

            return row;
        }

    }

    class ViewHolder {
        int position;
        StoreWsieDiscountBO discountBO;

        CheckBox discountck;
    }

    @Override
    public void onClick(View v) {
        int b = v.getId();
        if (b == R.id.btn_ok) {
            this.initAct.onResume();
            // lvwplist.getSelectedItemPosition()
            mDialogResult.finish(String.valueOf(discount), type,discountId,isCompanygiven);
            mDialogResult.cancel();
        } else if (b == R.id.btn_cancel)
            mDialogResult.cancel();

    }

    public interface OnMyDialogResult {
        void finish(String result, int result1,int result3,int result4);

        void cancel();
    }

    public void setDialogResult(OnMyDialogResult onMyDialogResult) {
        mDialogResult = onMyDialogResult;

    }
}
