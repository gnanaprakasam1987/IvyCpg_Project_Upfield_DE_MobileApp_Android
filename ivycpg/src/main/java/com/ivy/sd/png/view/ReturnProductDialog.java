package com.ivy.sd.png.view;

import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
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

public class ReturnProductDialog extends Dialog {

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
    private Button btnSave;
    private OrderSummary orderSummaryActivity;
    private Toolbar toolbar;

    public ReturnProductDialog(Context context, OrderSummary summaryActivity) {
        super(context);
        this.context = context;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        bmodel = (BusinessModel) context.getApplicationContext();
        orderSummaryActivity = summaryActivity;

        RelativeLayout ll = (RelativeLayout) LayoutInflater.from(context)
                .inflate(R.layout.dialog_returnproduct, null);
        setContentView(ll);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.Product_details);
        toolbar.setTitleTextColor(Color.WHITE);
        TextView mTitle = (TextView) toolbar.findViewById(R.id.tv_toolbar_title);
        mTitle.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        setCancelable(true);
        initializeView();

        if (bmodel.configurationMasterHelper.SHOW_GROUPPRODUCTRETURN)
            showGroupWiseListValues();
        else
            showListValues();
    }

    private void initializeView() {
        try {
            btnSave = (Button) findViewById(R.id.save_btn);
            lvwplist = (ListView) findViewById(R.id.lvwplist);
            lvwplist.setCacheColorHint(0);

            ((TextView) findViewById(R.id.productBarcodetitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            ((TextView) findViewById(R.id.tvProductNameTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            ((TextView) findViewById(R.id.caseTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            ((TextView) findViewById(R.id.outercaseTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            btnSave.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (bmodel.configurationMasterHelper.SHOW_GROUPPRODUCTRETURN)
                        bmodel.productHelper
                                .calculateOrderReturnTypeWiseValue();
                    else
                        bmodel.productHelper.calculateOrderReturnValue();
                    if (orderSummaryActivity != null)
                        ReturnProductDialog.this.orderSummaryActivity
                                .onResume();

                    dismiss();
                }
            });

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
                if (bmodel.configurationMasterHelper.CHECK_LIABLE_PRODUCTS) {
                    if (productBo.getLiableQty() > 0)
                        mylist.add(productBo);
                } else
                    mylist.add(productBo);

            }
            ProductAdapter mSchedule = new ProductAdapter(context, mylist);
            lvwplist.setAdapter(mSchedule);
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private void showGroupWiseListValues() {
        try {
            ArrayList<BomRetunBo> totalSize = bmodel.productHelper
                    .getBomReturnTypeProducts();

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
                if (bmodel.configurationMasterHelper.CHECK_LIABLE_PRODUCTS) {
                    if (productBo.getLiableQty() > 0)
                        mylist.add(productBo);
                } else
                    mylist.add(productBo);

            }
            ProductAdapter mSchedule = new ProductAdapter(context, mylist);
            lvwplist.setAdapter(mSchedule);
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private class ProductAdapter extends ArrayAdapter<BomRetunBo> {
        private ArrayList<BomRetunBo> items;
        private Context context;

        public ProductAdapter(Context context, ArrayList<BomRetunBo> mylist) {
            super(context, R.layout.dialog_returnproduct_row, mylist);
            this.items = mylist;
            this.context = context;
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

                holder.tvBarcode.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
                holder.tvSKUName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tvLiableQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
                holder.etReturnQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));

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

                holder.etReturnQty
                        .setOnFocusChangeListener(new View.OnFocusChangeListener() {
                            @Override
                            public void onFocusChange(View v, boolean hasFocus) {
                                if (hasFocus) {
                                    getWindow()
                                            .setSoftInputMode(
                                                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                                }
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
                            if (bmodel.configurationMasterHelper.CHECK_LIABLE_PRODUCTS) {
                                if (SDUtil.convertToInt(qty) <= holder.mSKUBO.getLiableQty())
                                    holder.mSKUBO.setReturnQty(SDUtil.convertToInt(qty));

                                else {
                                    if (!qty.equals("0")) {
                                        Toast.makeText(
                                                context, context.getResources().getString(R.string.return_qty_cannot_be_higher_than_liable_qty),
                                                Toast.LENGTH_SHORT).show();
                                        qty = qty.length() > 1 ? qty.substring(0,
                                                qty.length() - 1) : "0";
                                        holder.mSKUBO.setReturnQty(SDUtil.convertToInt(qty));
                                        holder.etReturnQty.setText(qty);
                                    }
                                }
                            } else
                                holder.mSKUBO.setReturnQty(SDUtil.convertToInt(qty));
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
            TypedArray typearr = context.getTheme().obtainStyledAttributes(R.styleable.MyTextView);
            if (position % 2 == 0) {
                convertView.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor_alt, 0));
            } else {
                convertView.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor, 0));
            }


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
                int s = SDUtil.convertToInt((String) QUANTITY.getText()
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


}
