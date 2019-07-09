package com.ivy.cpg.view.van.vanunload;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LoadManagementBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;

import java.util.ArrayList;

public class VanUnloadAdaptor extends BaseAdapter {

    private ArrayList<LoadManagementBO> items;
    private LoadManagementBO product;
    private Context mContext;
    private BusinessModel businessModel;
    private VanUnloadInterface vanUnloadInterface;
    private String append = "";
    private EditText QUANTITY;
    private String tv;

    public VanUnloadAdaptor(ArrayList<LoadManagementBO> items, Context mContext, VanUnloadInterface vanUnloadInterface) {
        this.items = items;
        this.mContext = mContext;
        this.businessModel = (BusinessModel) mContext.getApplicationContext();
        this.vanUnloadInterface = vanUnloadInterface;
    }

    public void setListData(ArrayList<LoadManagementBO> items) {
        this.items.clear();
        this.items = items;
    }


    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        final ViewHolder holder;

        product = items.get(position);
        View row = convertView;
        if (row == null) {
            LayoutInflater inflater = ((AppCompatActivity) mContext).getLayoutInflater();
            row = inflater.inflate(R.layout.van_unload, parent, false);
            holder = new ViewHolder();

            holder.listheaderLty = row.findViewById(R.id.van_unload_list_header);
            holder.caseQty = row
                    .findViewById(R.id.productqtyCases);
            holder.pieceQty = row
                    .findViewById(R.id.productqtyPieces);
            holder.psname = row.findViewById(R.id.productName);
            holder.psname.setMaxLines(businessModel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);
            holder.outerQty = row
                    .findViewById(R.id.productqtyouter);

            holder.sih = row
                    .findViewById(R.id.stock_and_order_listview_sih);
            holder.batchno = row
                    .findViewById(R.id.stock_and_order_listview_batchno);
            holder.nonSalableQty_pc = row.findViewById(R.id.tv_nonsalable_pc);

            // Nonsalable unload view
            if (!businessModel.configurationMasterHelper.SHOW_NON_SALABLE_UNLOAD)
                holder.nonSalableQty_pc.setVisibility(View.GONE);
            if (!businessModel.configurationMasterHelper.SHOW_ORDER_CASE)
                holder.caseQty.setVisibility(View.GONE);
            if (!businessModel.configurationMasterHelper.SHOW_ORDER_PCS)
                holder.pieceQty.setVisibility(View.GONE);
            if (!businessModel.configurationMasterHelper.SHOW_OUTER_CASE)
                holder.outerQty.setVisibility(View.GONE);


            holder.outerQty.addTextChangedListener(new TextWatcher() {

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
                    if (holder.productBO.getOuterSize() == 0) {
                        holder.outerQty.removeTextChangedListener(this);
                        holder.outerQty.setText("0");
                        holder.outerQty.addTextChangedListener(this);
                        return;
                    }
                    String qty = s.toString();

                    if (qty.length() > 0)
                        holder.outerQty.setSelection(qty.length());

                    if (!"".equals(qty)) {
                        holder.productBO.setOuterQty(SDUtil
                                .convertToInt(qty));
                        if (!"0".equals(qty)) {
                            int sum = (holder.productBO.getOuterQty() * holder.productBO
                                    .getOuterSize())
                                    + (holder.productBO.getPieceqty())
                                    + (holder.productBO.getCaseqty() * holder.productBO
                                    .getCaseSize());
                            if (sum > holder.productBO.getSih()) {

                                ((IvyBaseActivityNoActionBar) mContext).showMessage(String.format(mContext.getString(
                                        R.string.exceed),
                                        holder.productBO.getSih()));

                                qty = qty.length() > 1 ? qty
                                        .substring(0, qty.length() - 1) : "0";
                                holder.outerQty.setText(qty);
                                holder.productBO.setOuterQty(SDUtil
                                        .convertToInt(qty));
                            }
                        }
                        vanUnloadInterface.updateTotalQtyDetails(items);
                    }

                }
            });

            holder.pieceQty.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {

                    String qty = s.toString();
                    if (qty.length() > 0)
                        holder.pieceQty.setSelection(qty.length());


                    if (!"".equals(qty)) {
                        holder.productBO.setPieceqty(SDUtil
                                .convertToInt(qty));
                        if (!"0".equals(qty)) {
                            int sum = (holder.productBO.getOuterQty() * holder.productBO
                                    .getOuterSize())
                                    + (holder.productBO.getPieceqty())
                                    + (holder.productBO.getCaseqty() * holder.productBO
                                    .getCaseSize());
                            if (sum > holder.productBO.getSih()) {

                                ((IvyBaseActivityNoActionBar) mContext).showMessage(String.format(mContext.getString(
                                        R.string.exceed),
                                        holder.productBO.getSih()));

                                qty = qty.length() > 1 ? qty
                                        .substring(0, qty.length() - 1) : "0";
                                holder.pieceQty.setText(qty);
                                holder.productBO.setPieceqty(SDUtil
                                        .convertToInt(qty));

                            }
                        }
                        vanUnloadInterface.updateTotalQtyDetails(items);
                    }
                }

                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                }
            });

            holder.caseQty.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    if (holder.productBO.getCaseSize() == 0) {
                        holder.caseQty.removeTextChangedListener(this);
                        holder.caseQty.setText("0");
                        holder.caseQty.addTextChangedListener(this);
                        return;
                    }

                    String qty = s.toString();

                    if (qty.length() > 0)
                        holder.caseQty.setSelection(qty.length());

                    if (!"".equals(qty)) {
                        holder.productBO.setCaseqty(SDUtil
                                .convertToInt(qty));

                        if (!"0".equals(qty)) {
                            int sum = (holder.productBO.getOuterQty() * holder.productBO
                                    .getOuterSize())
                                    + (holder.productBO.getPieceqty())
                                    + (holder.productBO.getCaseqty() * holder.productBO
                                    .getCaseSize());
                            if (sum > holder.productBO.getSih()) {
                                ((IvyBaseActivityNoActionBar) mContext).showMessage(String.format(mContext.getString(
                                        R.string.exceed),
                                        holder.productBO.getSih()));

                                qty = qty.length() > 1 ? qty
                                        .substring(0, qty.length() - 1) : "0";
                                holder.caseQty.setText(qty);
                                holder.productBO.setCaseqty(SDUtil
                                        .convertToInt(qty));
                            }
                        }

                        vanUnloadInterface.updateTotalQtyDetails(items);
                    }
                }

                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                }
            });

            holder.caseQty.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    vanUnloadInterface.setProductName(holder.pname);

                    QUANTITY = holder.caseQty;
                    int inType = holder.caseQty.getInputType();
                    holder.caseQty.setInputType(InputType.TYPE_NULL);
                    holder.caseQty.onTouchEvent(event);
                    holder.caseQty.setInputType(inType);
                    holder.caseQty.requestFocus();
                    if (holder.caseQty.getText().length() > 0)
                        holder.caseQty.setSelection(holder.caseQty.getText().length());
                    vanUnloadInterface.hideKeyboard();

                    return true;
                }
            });
            holder.pieceQty.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    vanUnloadInterface.setProductName(holder.pname);
                    QUANTITY = holder.pieceQty;
                    int inType = holder.pieceQty.getInputType();
                    holder.pieceQty.setInputType(InputType.TYPE_NULL);
                    holder.pieceQty.onTouchEvent(event);
                    holder.pieceQty.setInputType(inType);
                    holder.pieceQty.requestFocus();
                    if (holder.pieceQty.getText().length() > 0)
                        holder.pieceQty.setSelection(holder.pieceQty.getText().length());
                    vanUnloadInterface.hideKeyboard();
                    return true;
                }
            });
            holder.outerQty.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    vanUnloadInterface.setProductName(holder.pname);
                    QUANTITY = holder.outerQty;
                    int inType = holder.outerQty.getInputType();
                    holder.outerQty.setInputType(InputType.TYPE_NULL);
                    holder.outerQty.onTouchEvent(event);
                    holder.outerQty.setInputType(inType);
                    holder.outerQty.requestFocus();
                    if (holder.outerQty.getText().length() > 0)
                        holder.outerQty.setSelection(holder.outerQty.getText().length());
                    vanUnloadInterface.hideKeyboard();
                    return true;
                }
            });
            row.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    vanUnloadInterface.setProductName(holder.pname);
                    QUANTITY = holder.caseQty;
                    holder.caseQty.selectAll();
                    holder.caseQty.requestFocus();

                    vanUnloadInterface.hideViewFlipper();

                }
            });


            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        holder.position = position;
        holder.productBO = product;
        holder.pname = product.getProductname();
        holder.psname.setText(product.getProductshortname());

        if (product.getIsFree() == 1)
            holder.psname.setTextColor(ContextCompat.getColor(mContext,
                    R.color.colorAccent));
        else
            holder.psname.setTextColor(ContextCompat.getColor(mContext,
                    android.R.color.black));

        tv = product.getStocksih() + "";
        holder.sih.setText(tv);
        tv = product.getCaseqty() + "";
        holder.caseQty.setText(tv);
        tv = product.getPieceqty() + "";
        holder.pieceQty.setText(tv);
        tv = product.getOuterQty() + "";
        holder.outerQty.setText(tv);
        if (product.getBatchNo() != null && !product.getBatchNo().trim().equals("")) {
            tv = "Batch No: " + product.getBatchNo() + "";
            holder.batchno.setText(tv);
        } else {
            holder.batchno.setText("");
        }

        if (holder.productBO.getdUomid() == 0 || !holder.productBO.isCaseMapped()) {
            holder.caseQty.setEnabled(false);
        } else {
            holder.caseQty.setEnabled(true);
        }
        if (holder.productBO.getdOuonid() == 0 || !holder.productBO.isOuterMapped()) {
            holder.outerQty.setEnabled(false);
        } else {
            holder.outerQty.setEnabled(true);
        }
        if (holder.productBO.getPiece_uomid() == 0 || !holder.productBO.isPieceMapped()) {
            holder.pieceQty.setEnabled(false);
        } else {
            holder.pieceQty.setEnabled(true);
        }
        if (holder.productBO.getNonSalableQty() > 0) {
            holder.nonSalableQty_pc.setText(holder.productBO.getNonSalableQty() + "");
        } else {
            holder.nonSalableQty_pc.setText(mContext.getString(R.string.zero));
        }

        return row;
    }

    class ViewHolder {
        LoadManagementBO productBO;

        int position;
        String pname;
        TextView psname;
        TextView sih;
        TextView batchno;
        EditText pieceQty;
        EditText caseQty;
        EditText outerQty;
        LinearLayout listheaderLty;
        TextView nonSalableQty_pc;
    }


    public void numberPressed(View vw) {
        if (QUANTITY == null) {
            businessModel.showAlert(
                    mContext.getString(R.string.please_select_item), 0);
        } else {
            int id = vw.getId();
            if (id == R.id.calcdel) {
                String s = QUANTITY.getText().toString();
                if (!(s.length() == 0)) {
                    s = s.substring(0, s.length() - 1);
                    if (s.length() == 0) {
                        s = "";
                    }
                }
                QUANTITY.setText(s);
            } else {
                Button ed = ((VanUnloadActivity) mContext).findViewById(vw.getId());
                append = ed.getText().toString();
                eff();
            }
        }
    }

    private void eff() {
        String s = QUANTITY.getText().toString();
        if (!"0".equals(s) && !"0.0".equals(s)) {
            String strQuantity = QUANTITY.getText() + append;
            QUANTITY.setText(strQuantity);
        } else
            QUANTITY.setText(append);
    }

}
