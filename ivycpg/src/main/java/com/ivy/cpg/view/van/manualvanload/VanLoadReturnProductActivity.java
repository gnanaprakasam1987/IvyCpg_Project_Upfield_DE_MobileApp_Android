package com.ivy.cpg.view.van.manualvanload;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.BomReturnBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;

public class VanLoadReturnProductActivity extends IvyBaseActivityNoActionBar implements View.OnClickListener {

    // Declare Businness Model Class
    private BusinessModel bmodel;

    // Views
    private ListView lvwplist;
    private EditText QUANTITY;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_returnproduct);

        bmodel = (BusinessModel) getApplicationContext();

        initializeView();
        // Iterate valus and show Values in the Listivew

        // Always Load SKU in Manual Van Load Module Bottle Return
        showListValues();
    }

    private void initializeView() {
        try {

            Toolbar toolbar = findViewById(R.id.toolbar);
            toolbar.setVisibility(View.VISIBLE);

            setSupportActionBar(toolbar);

            if (getSupportActionBar() != null) {

                getSupportActionBar().setDisplayShowTitleEnabled(false);
//            // Used to on / off the back arrow icon
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//           // Used to remove the app logo actionbar icon and set title as home
//          // (title support click)
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }

            setScreenTitle(getResources().getString(R.string.Product_details));

            lvwplist = findViewById(R.id.list);
            lvwplist.setCacheColorHint(0);
            btnSave = findViewById(R.id.save_btn);
            btnSave.setOnClickListener(this);

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    /**
     * Iterate Array List and Add the values in the Listview
     */
    private void showListValues() {
        try {
            ArrayList<BomReturnBO> totalSize = bmodel.productHelper
                    .getBomReturnProducts();
            // If Total Size is null,Show alert in the Screen
            if (totalSize == null) {
                bmodel.showAlert(
                        getResources().getString(
                                R.string.no_products_exists), 0);
                return;
            }

            int size = totalSize.size();
            // List to add values and Show in ListView
            ArrayList<BomReturnBO> mylist = new ArrayList<>();
            // Add the products into list
            for (int i = 0; i < size; ++i) {
                BomReturnBO productBo = totalSize.get(i);
                mylist.add(productBo);

            }
            ProductAdapter mSchedule = new ProductAdapter(mylist);
            lvwplist.setAdapter(mSchedule);
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private class ProductAdapter extends ArrayAdapter<BomReturnBO> {
        private ArrayList<BomReturnBO> items;

        private ProductAdapter(ArrayList<BomReturnBO> mylist) {
            super(VanLoadReturnProductActivity.this, R.layout.dialog_initiative_listrow, mylist);
            this.items = mylist;
        }

        public BomReturnBO getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {

                LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(
                        R.layout.dialog_returnproduct_row, parent, false);
                holder = new ViewHolder();

                holder.tvBarcode = convertView
                        .findViewById(R.id.stock_and_order_listview_productbarcode);

                holder.tvSKUName = convertView
                        .findViewById(R.id.orderPRODNAME);

                holder.tvLiableQty = convertView
                        .findViewById(R.id.tv_liableqty);

                holder.etReturnQty = convertView
                        .findViewById(R.id.et_returnqty);

                holder.etReturnQty.setOnTouchListener(new View.OnTouchListener() {

                    @Override
                    public boolean onTouch(View arg0, MotionEvent arg1) {
                        QUANTITY = holder.etReturnQty;
                        QUANTITY.setTag(holder.etReturnQty);
                        int inType = holder.etReturnQty.getInputType();
                        holder.etReturnQty.setInputType(InputType.TYPE_NULL);
                        holder.etReturnQty.onTouchEvent(arg1);
                        holder.etReturnQty.setInputType(inType);
                        holder.etReturnQty.requestFocus();
                        if (holder.etReturnQty.getText().length() > 0)
                            holder.etReturnQty.setSelection(holder.etReturnQty.getText().length());
                        return true;
                    }
                });

                holder.etReturnQty.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {

                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        String qty = s.toString();
                        if (qty.length() > 0)
                            holder.etReturnQty.setSelection(qty.length());

                        if (!qty.equals("")) {

                            if (SDUtil.convertToInt(qty) <= holder.mSKUBO
                                    .getTotalReturnQty()) {
                                holder.mSKUBO.setReturnQty(SDUtil
                                        .convertToInt(qty));
                            } else {

                                ((IvyBaseActivityNoActionBar) VanLoadReturnProductActivity.this).showMessage(getString(
                                        R.string.exceed_allocation));
                                qty = qty.length() > 1 ? qty.substring(0,
                                        qty.length() - 1) : "0";

                                holder.mSKUBO.setReturnQty(SDUtil
                                        .convertToInt(qty));
                                holder.etReturnQty.setText(holder.mSKUBO
                                        .getReturnQty() + "");

                            }

                        }
                    }
                });

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.mSKUBO = items.get(position);

            holder.tvBarcode.setText(holder.mSKUBO.getBarcode());
            holder.tvSKUName.setText(holder.mSKUBO.getProductName());
            holder.tvLiableQty.setText(holder.mSKUBO.getLiableQty() + "");
            holder.etReturnQty.setText(holder.mSKUBO.getReturnQty() + "");

            return convertView;
        }
    }

    class ViewHolder {
        BomReturnBO mSKUBO;
        TextView tvBarcode, tvSKUName, tvLiableQty;
        EditText etReturnQty;

    }

    public void eff(String append) {
        String s = QUANTITY.getText().toString();
        if (!s.equals("0") && !s.equals("0.0")) {
            QUANTITY.setText(QUANTITY.getText() + append);
        } else
            QUANTITY.setText(append);
    }

    public void numberPressed(View vw) {
        if (QUANTITY == null) {
            bmodel.showAlert(
                    getResources().getString(
                            R.string.please_select_item), 0);
        } else {
            int id = vw.getId();
            if (id == R.id.calcdel) {
                int s = SDUtil.convertToInt(QUANTITY.getText()
                        .toString());
                s = s / 10;
                QUANTITY.setText(s + "");
            } else {
                Button ed = (Button) findViewById(vw.getId());
                String append = ed.getText().toString();
                eff(append);
            }
        }

    }

    @Override
    public void onClick(View v) {
        Button b = (Button) v;
        if (b == btnSave) {
            bmodel.productHelper.calculateOrderReturnValue();
            finish();
        }
    }
}
