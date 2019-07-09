package com.ivy.cpg.view.delivery.kellogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.salesreturn.SalesReturnReasonBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.FontUtils;

import java.util.Vector;


public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private Vector<ProductMasterBO> productList;
    private BusinessModel bmodel;
    private Context context;
    private EditText QUANTITY;

    private InputMethodManager inputManager;

    MyAdapter(Vector<ProductMasterBO> productList, BusinessModel bmodel, Context context) {
        this.context = context;
        this.productList = productList;
        this.bmodel = bmodel;

        inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView productName, srQty, sihQty, srpQty;
        private EditText pieceQty, caseQty, outerQty;

        public MyViewHolder(View view) {
            super(view);

            productName = view.findViewById(R.id.prod_name);
            pieceQty = view.findViewById(R.id.piece_qty);
            caseQty = view.findViewById(R.id.case_qty);
            outerQty = view.findViewById(R.id.outer_qty);
            srQty = view.findViewById(R.id.sales_return_qty);
            sihQty = view.findViewById(R.id.sih_qty);
            srpQty = view.findViewById(R.id.sales_replace_qty);

            productName.setTypeface(FontUtils.getProductNameFont(context));

            if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS)
                pieceQty.setVisibility(View.GONE);
            else {
                try {
                    pieceQty.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.LIGHT));
                    if (bmodel.labelsMasterHelper.applyLabels(pieceQty.getTag()) != null)
                        pieceQty.setText(bmodel.labelsMasterHelper
                                .applyLabels(pieceQty.getTag()));
                } catch (Exception e) {
                    Commons.printException(e + "");
                }
            }

            if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE)
                caseQty.setVisibility(View.GONE);
            else {
                try {
                    caseQty.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.LIGHT));
                    if (bmodel.labelsMasterHelper.applyLabels(caseQty.getTag()) != null)
                        caseQty.setText(bmodel.labelsMasterHelper
                                .applyLabels(caseQty.getTag()));
                } catch (Exception e) {
                    Commons.printException(e + "");
                }
            }

            if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
                outerQty.setVisibility(View.GONE);
            else {
                try {
                    outerQty.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.LIGHT));
                    if (bmodel.labelsMasterHelper.applyLabels(outerQty.getTag()) != null)
                        outerQty.setText(bmodel.labelsMasterHelper
                                .applyLabels(outerQty.getTag()));
                } catch (Exception e) {
                    Commons.printException(e + "");
                }
            }

            if (!bmodel.configurationMasterHelper.IS_STOCK_IN_HAND)
                sihQty.setVisibility(View.GONE);
            else {
                try {
                    sihQty.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.LIGHT));
                    if (bmodel.labelsMasterHelper.applyLabels(sihQty.getTag()) != null)
                        sihQty.setText(bmodel.labelsMasterHelper
                                .applyLabels(sihQty.getTag()));
                } catch (Exception e) {
                    Commons.printException(e + "");
                }
            }

            if (!bmodel.configurationMasterHelper.SHOW_SALES_RETURN_IN_ORDER)
                srpQty.setVisibility(View.GONE);
            else {
                try {
                    srpQty.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.LIGHT));
                    if (bmodel.labelsMasterHelper.applyLabels(srpQty.getTag()) != null)
                        srpQty.setText(bmodel.labelsMasterHelper
                                .applyLabels(srpQty.getTag()));
                } catch (Exception e) {
                    Commons.printException(e + "");
                }
            }

            if (!bmodel.configurationMasterHelper.SHOW_SALES_RETURN_IN_ORDER)
                srQty.setVisibility(View.GONE);
            else {
                try {
                    srQty.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.LIGHT));
                    if (bmodel.labelsMasterHelper.applyLabels(srQty.getTag()) != null)
                        srQty.setText(bmodel.labelsMasterHelper
                                .applyLabels(srQty.getTag()));
                } catch (Exception e) {
                    Commons.printException(e + "");
                }
            }

        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.order_delivery_edit_detail_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        holder.productName.setTextColor(Color.parseColor("#000000"));
        holder.pieceQty.setTextColor(Color.parseColor("#000000"));
        holder.caseQty.setTextColor(Color.parseColor("#000000"));
        holder.outerQty.setTextColor(Color.parseColor("#000000"));
        holder.sihQty.setTextColor(Color.parseColor("#000000"));
        holder.srpQty.setTextColor(Color.parseColor("#000000"));
        holder.srQty.setTextColor(Color.parseColor("#000000"));

        holder.productName.setText(String.valueOf(productList.get(position).getProductName()));
        holder.pieceQty.setText(String.valueOf(productList.get(position).getOrderedPcsQty()));
        holder.caseQty.setText(String.valueOf(productList.get(position).getOrderedCaseQty()));
        holder.outerQty.setText(String.valueOf(productList.get(position).getOrderedOuterQty()));

        holder.pieceQty.setTag(String.valueOf(productList.get(position).getOrderedPcsQty()));
        holder.caseQty.setTag(String.valueOf(productList.get(position).getOrderedCaseQty() * productList.get(position).getCaseSize()));
        holder.outerQty.setTag(String.valueOf(productList.get(position).getOrderedOuterQty() * productList.get(position).getOutersize()));

        holder.sihQty.setText(String.valueOf(productList.get(position).getSIH()));

        if (bmodel.configurationMasterHelper.SHOW_SALES_RETURN_IN_ORDER) {
            int total = 0;
            if (productList.get(position).getSalesReturnReasonList() != null) {
                for (SalesReturnReasonBO obj : productList.get(position).getSalesReturnReasonList())
                    total = total + obj.getPieceQty() + (obj.getCaseQty() * obj.getCaseSize()) + (obj.getOuterQty() * obj.getOuterSize());
            }
            String strTotal = Integer.toString(total);
            holder.srQty.setText(strTotal);
        }

        int totalReplaceQty = (productList.get(position).getRepCaseQty() * productList.get(position).getCaseSize())
                + productList.get(position).getRepPieceQty()
                + (productList.get(position).getRepOuterQty() * productList.get(position).getOutersize());

        holder.srpQty.setText(String.valueOf(totalReplaceQty));

        if (productList.get(position).getOuUomid() == 0 || !productList.get(position).isOuterMapped()) {
            holder.outerQty.setEnabled(false);
        } else {
            holder.outerQty.setEnabled(true);
        }
        if (productList.get(position).getCaseUomId() == 0 || !productList.get(position).isCaseMapped()) {
            holder.caseQty.setEnabled(false);
        } else {
            holder.caseQty.setEnabled(true);
        }
        if (productList.get(position).getPcUomid() == 0 || !productList.get(position).isPieceMapped()) {
            holder.pieceQty.setEnabled(false);
        } else {
            holder.pieceQty.setEnabled(true);
        }


        holder.pieceQty.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (productList.get(position).getPcUomid() == 0) {
                    holder.pieceQty.removeTextChangedListener(this);
                    holder.pieceQty.setText("0");
                    holder.pieceQty.addTextChangedListener(this);
                    return;
                }

                String qty = s.toString();
                if (qty.length() > 0)
                    holder.pieceQty.setSelection(qty.length());



                float totalQty = (productList.get(position).getOrderedCaseQty() * productList.get(position).getCaseSize())
                        + (SDUtil.convertToInt(qty))
                        + (productList.get(position).getOrderedOuterQty() * productList.get(position).getOutersize())
                        + productList.get(position).getRepPieceQty()
                        + (productList.get(position).getRepCaseQty() * productList.get(position).getCaseSize())
                        + (productList.get(position).getRepOuterQty() * productList.get(position).getOutersize());

                int storedPieceQty = 0;
                if (holder.pieceQty.getTag() != null && !holder.pieceQty.getTag().toString().equals(""))
                    storedPieceQty = Integer.valueOf(holder.pieceQty.getTag().toString());

                if (totalQty <= productList.get(position).getSIH() &&
                        SDUtil.convertToInt(qty) <= storedPieceQty) {
                    if (!"".equals(qty)) {
                        productList.get(position).setOrderedPcsQty(SDUtil
                                .convertToInt(qty));
                    }
                    double tot = (productList.get(position).getOrderedCaseQty() * productList.get(position).getCsrp())
                            + (productList.get(position).getOrderedPcsQty() * productList.get(position).getSrp())
                            + (productList.get(position).getOrderedOuterQty() * productList.get(position).getOsrp());

                    productList.get(position).setTotalamount(tot);
                } else {
                    if (!"0".equals(qty)) {
                        if (totalQty > productList.get(position).getSIH()) {
                            Toast.makeText(
                                    context,
                                    String.format(
                                            context.getResources().getString(
                                                    R.string.exceed),
                                            productList.get(position).getSIH()),
                                    Toast.LENGTH_SHORT).show();
                        } else if (SDUtil.convertToInt(qty) > storedPieceQty) {
                            Toast.makeText(
                                    context,
                                    context.getResources().getString(
                                            R.string.exceed_ordered_value),
                                    Toast.LENGTH_SHORT).show();
                        }

                        //Delete the last entered number and reset the qty
                        qty = qty.length() > 1 ? qty.substring(0,
                                qty.length() - 1) : "0";
                        productList.get(position).setOrderedPcsQty(SDUtil
                                .convertToInt(qty));
                        holder.pieceQty.setText(qty);
                    }
                }

            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
            }
        });

        holder.pieceQty.setFocusable(true);
        holder.pieceQty.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                QUANTITY = holder.pieceQty;
                int inType = holder.pieceQty.getInputType();
                holder.pieceQty.setInputType(InputType.TYPE_NULL);
                holder.pieceQty.onTouchEvent(event);
                holder.pieceQty.setInputType(inType);
                holder.pieceQty.requestFocus();
                if (holder.pieceQty.getText().length() > 0)
                    holder.pieceQty.setSelection(holder.pieceQty.getText().length());
                inputManager.hideSoftInputFromWindow(
                        holder.pieceQty.getWindowToken(), 0);
                return true;
            }
        });


        holder.caseQty.addTextChangedListener(new TextWatcher() {
            @SuppressLint("StringFormatInvalid")
            public void afterTextChanged(Editable s) {
                if (productList.get(position).getCaseSize() == 0) {
                    holder.caseQty.removeTextChangedListener(this);
                    holder.caseQty.setText("0");
                    holder.caseQty.addTextChangedListener(this);
                    return;
                }

                String qty = s.toString();
                if (qty.length() > 0)
                    holder.caseQty.setSelection(qty.length());

                float totalQty = (SDUtil.convertToInt(qty) * productList.get(position).getCaseSize())
                        + (productList.get(position).getOrderedPcsQty())
                        + (productList.get(position).getOrderedOuterQty() * productList.get(position).getOutersize())
                        + productList.get(position).getRepPieceQty()
                        + (productList.get(position).getRepCaseQty() * productList.get(position).getCaseSize())
                        + (productList.get(position).getRepOuterQty() * productList.get(position).getOutersize());

                int storedcaseQty = 0;
                if (holder.caseQty.getTag() != null && !holder.caseQty.getTag().toString().equals(""))
                    storedcaseQty = Integer.valueOf(holder.caseQty.getTag().toString());

                if (totalQty <= productList.get(position).getSIH() &&
                        (SDUtil.convertToInt(qty) * productList.get(position).getCaseSize()) <= storedcaseQty) {
                    if (!"".equals(qty)) {
                        productList.get(position).setOrderedCaseQty(SDUtil
                                .convertToInt(qty));
                    }

                    double tot = (productList.get(position).getOrderedCaseQty() * productList.get(position).getCsrp())
                            + (productList.get(position).getOrderedPcsQty() * productList.get(position).getSrp())
                            + (productList.get(position).getOrderedOuterQty() * productList.get(position).getOsrp());
                    productList.get(position).setTotalamount(tot);
                } else {
                    if (!"0".equals(qty)) {
                        if (totalQty > productList.get(position).getSIH()) {
                            Toast.makeText(
                                    context,
                                    String.format(
                                            context.getResources().getString(
                                                    R.string.exceed),
                                            productList.get(position).getSIH()),
                                    Toast.LENGTH_SHORT).show();
                        } else if ((SDUtil.convertToInt(qty) * productList.get(position).getCaseSize()) > storedcaseQty) {
                            Toast.makeText(
                                    context,
                                    String.format(
                                            context.getResources().getString(
                                                    R.string.exceed_ordered_value),
                                            storedcaseQty / productList.get(position).getCaseSize()),
                                    Toast.LENGTH_SHORT).show();
                        }

                        //Delete the last entered number and reset the qty
                        qty = qty.length() > 1 ? qty.substring(0,
                                qty.length() - 1) : "0";

                        holder.caseQty.setText(qty);
                        productList.get(position).setOrderedCaseQty(SDUtil
                                .convertToInt(qty));
                    }
                }
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
            }
        });


        holder.caseQty.setFocusable(true);
        holder.caseQty.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                QUANTITY = holder.caseQty;
                int inType = holder.caseQty.getInputType();
                holder.caseQty.setInputType(InputType.TYPE_NULL);
                holder.caseQty.onTouchEvent(event);
                holder.caseQty.setInputType(inType);
                holder.caseQty.requestFocus();
                if (holder.caseQty.getText().length() > 0)
                    holder.caseQty.setSelection(holder.caseQty.getText().length());
                inputManager.hideSoftInputFromWindow(
                        holder.caseQty.getWindowToken(), 0);
                return true;
            }
        });

        holder.outerQty.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if (productList.get(position).getOuUomid() == 0) {
                    holder.outerQty.removeTextChangedListener(this);
                    holder.outerQty.setText("0");
                    holder.outerQty.addTextChangedListener(this);
                    return;
                }
                String qty = s.toString();
                if (qty.length() > 0)
                    holder.outerQty.setSelection(qty.length());


                float totalQty = (SDUtil.convertToInt(qty) * productList.get(position).getOutersize())
                        + (productList.get(position).getOrderedCaseQty() * productList.get(position).getCaseSize())
                        + (productList.get(position).getOrderedPcsQty())
                        + productList.get(position).getRepPieceQty()
                        + (productList.get(position).getRepCaseQty() * productList.get(position).getCaseSize())
                        + (productList.get(position).getRepOuterQty() * productList.get(position).getOutersize());


                int storedouterQty = 0;
                if (holder.outerQty.getTag() != null && !holder.outerQty.getTag().toString().equals(""))
                    storedouterQty = Integer.valueOf(holder.outerQty.getTag().toString());

                if (totalQty <= productList.get(position).getSIH() &&
                        (SDUtil.convertToInt(qty) * productList.get(position).getOutersize()) <= storedouterQty) {
                    if (!"".equals(qty)) {
                        productList.get(position).setOrderedOuterQty(SDUtil
                                .convertToInt(qty));
                    }

                    double tot = (productList.get(position).getOrderedCaseQty() * productList.get(position).getCsrp())
                            + (productList.get(position).getOrderedPcsQty() * productList.get(position).getSrp())
                            + (productList.get(position).getOrderedOuterQty() * productList.get(position).getOsrp());
                    productList.get(position).setTotalamount(tot);
                } else {
                    if (!"0".equals(qty)) {
                        if (totalQty > productList.get(position).getSIH()) {
                            Toast.makeText(
                                    context,
                                    String.format(
                                            context.getResources().getString(
                                                    R.string.exceed),
                                            productList.get(position).getSIH()),
                                    Toast.LENGTH_SHORT).show();
                        } else if ((SDUtil.convertToInt(qty) * productList.get(position).getOutersize()) > storedouterQty) {
                            Toast.makeText(
                                    context,
                                    context.getResources().getString(
                                            R.string.exceed_ordered_value),
                                    Toast.LENGTH_SHORT).show();
                        }

                        qty = qty.length() > 1 ? qty.substring(0,
                                qty.length() - 1) : "0";

                        productList.get(position).setOrderedOuterQty(SDUtil
                                .convertToInt(qty));

                        holder.outerQty.setText(qty);
                    }
                }

            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

        });

        holder.outerQty.setFocusable(true);
        holder.outerQty.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                QUANTITY = holder.outerQty;
                int inType = holder.outerQty.getInputType();
                holder.outerQty.setInputType(InputType.TYPE_NULL);
                holder.outerQty.onTouchEvent(event);
                holder.outerQty.setInputType(inType);
                holder.outerQty.requestFocus();
                if (holder.outerQty.getText().length() > 0)
                    holder.outerQty.setSelection(holder.outerQty.getText().length());
                inputManager.hideSoftInputFromWindow(
                        holder.outerQty.getWindowToken(), 0);
                return true;
            }
        });
    }

    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }
}