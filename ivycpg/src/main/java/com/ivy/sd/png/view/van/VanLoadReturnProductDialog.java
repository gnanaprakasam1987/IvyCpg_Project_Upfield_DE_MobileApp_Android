package com.ivy.sd.png.view.van;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.BomRetunBo;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;

public class VanLoadReturnProductDialog extends Dialog implements
        android.view.View.OnClickListener {

    // Declare Businness Model Class
    private BusinessModel bmodel;
    // Declare Context
    private Context context;
    // List to add values and Show in ListView
    private ArrayList<BomRetunBo> mylist;
    // Vairalbes
    private String append = "";
    // Views
    private ListView lvwplist;
    private EditText QUANTITY;
    private ManualVanLoadActivity manualVanLoadActivity;
    private Button  btnSave;
    private Toolbar toolbar;

    public VanLoadReturnProductDialog(Context context,
                                      ManualVanLoadActivity vanloadActivity) {
        super(context);
        this.context = context;
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        bmodel = (BusinessModel) context.getApplicationContext();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = getWindow();
        lp.copyFrom(window.getAttributes());
        // This makes the dialog take up the full width
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);

        manualVanLoadActivity = vanloadActivity;
        RelativeLayout ll = (RelativeLayout) LayoutInflater.from(context)
                .inflate(R.layout.dialog_returnproduct, null);
        setContentView(ll);
        this.getWindow().setLayout(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.Product_details);
        setCancelable(true);
        // Initialize Views in the Screen
        initializeView();
        // Iterate valus and show Values in the Listivew

        // Always Load SKU in Manual Van Load Module Bottle Return
        if (manualVanLoadActivity != null)
            showListValues();

    }

    private void initializeView() {
        try {
            lvwplist = (ListView) findViewById(R.id.lvwplist);
            lvwplist.setCacheColorHint(0);
            btnSave = (Button) findViewById(R.id.save_btn);
            btnSave.setOnClickListener(this);
            btnSave.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    /**
     * Iterate Array List and Add the values in the Listview
     */
    private void showListValues() {
        try {
            ArrayList<BomRetunBo> totalSize = bmodel.productHelper
                    .getBomReturnProducts();
            // If Total Size is null,Show alert in the Screen
            if (totalSize == null) {
                bmodel.showAlert(
                        context.getResources().getString(
                                R.string.no_products_exists), 0);
                return;
            }

            int size = totalSize.size();
            mylist = new ArrayList<>();
            // Add the products into list
            for (int i = 0; i < size; ++i) {
                BomRetunBo productBo = totalSize.get(i);
                mylist.add(productBo);

            }
            ProductAdapter mSchedule = new ProductAdapter(mylist);
            lvwplist.setAdapter(mSchedule);
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private class ProductAdapter extends ArrayAdapter<BomRetunBo> {
        private ArrayList<BomRetunBo> items;

        private ProductAdapter(ArrayList<BomRetunBo> mylist) {
            super(context, R.layout.dialog_initiative_listrow, mylist);
            this.items = mylist;
        }

        public BomRetunBo getItem(int position) {
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

                LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(
                        R.layout.dialog_returnproduct_row, parent, false);
                holder = new ViewHolder();

                holder.tvBarcode = (TextView) convertView
                        .findViewById(R.id.stock_and_order_listview_productbarcode);

                holder.tvSKUName = (TextView) convertView
                        .findViewById(R.id.orderPRODNAME);

                holder.tvLiableQty = (TextView) convertView
                        .findViewById(R.id.tv_liableqty);

                holder.etReturnQty = (EditText) convertView
                        .findViewById(R.id.et_returnqty);

                holder.etReturnQty.setOnTouchListener(new OnTouchListener() {

                    @Override
                    public boolean onTouch(View arg0, MotionEvent arg1) {
                        QUANTITY = holder.etReturnQty;
                        QUANTITY.setTag(holder.etReturnQty);
                        int inType = holder.etReturnQty.getInputType();
                        holder.etReturnQty.setInputType(InputType.TYPE_NULL);
                        holder.etReturnQty.onTouchEvent(arg1);
                        holder.etReturnQty.setInputType(inType);
                        holder.etReturnQty.selectAll();
                        holder.etReturnQty.requestFocus();
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

                        if (!qty.equals("")) {

                            if (SDUtil.convertToInt(qty) <= holder.mSKUBO
                                    .getTotalReturnQty()) {
                                holder.mSKUBO.setReturnQty(SDUtil
                                        .convertToInt(qty));
                            } else {

                                Toast.makeText(
                                        context,
                                        context.getResources().getString(
                                                R.string.exceed_allocation),
                                        Toast.LENGTH_SHORT).show();
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
        BomRetunBo mSKUBO;
        TextView tvBarcode, tvSKUName, tvLiableQty;
        EditText etReturnQty;

    }

    public void eff() {
        String s = QUANTITY.getText().toString();
        if (!s.equals("0") && !s.equals("0.0")) {
            QUANTITY.setText(QUANTITY.getText() + append);
        } else
            QUANTITY.setText(append);
    }

    public void numberPressed(View vw) {
        if (QUANTITY == null) {
            bmodel.showAlert(
                    context.getResources().getString(
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
                append = ed.getText().toString();
                eff();
            }
        }

    }

    @Override
    public void onClick(View v) {
        Button b = (Button) v;
       /* if (b == btnBack) {
            bmodel.productHelper.calculateOrderReturnValue();
            if (manualVanLoadActivity != null) {
                this.manualVanLoadActivity.onResume();
                dismiss();
            }
        }*/
        if (b == btnSave) {
            bmodel.productHelper.calculateOrderReturnValue();
            if (manualVanLoadActivity != null) {
                VanLoadReturnProductDialog.this.manualVanLoadActivity
                        .onResume();
                dismiss();
            }
        }
    }

}
