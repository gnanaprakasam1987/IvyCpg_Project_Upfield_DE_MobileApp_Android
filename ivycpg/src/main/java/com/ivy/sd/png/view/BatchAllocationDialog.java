package com.ivy.sd.png.view;

import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.appcompat.widget.Toolbar;
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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;

public class BatchAllocationDialog extends Dialog {
    private static final String TAG = "BatchAllocationDialog";
    private Context mContext;
    private BusinessModel bmodel;
    private BatchAllocation mBatchAllocation;
    private ProductMasterBO productBO;
    private ArrayList<ProductMasterBO> mBatctAllocationList;
    private ArrayList<ProductMasterBO> myList;
    private TextView mTotalTV;
    private ListView mBatchAllocationLV;

    private EditText QUANTITY;
    private String append = "";
    private int mOrderedQty = 0;
    private int mBalanceQty = 0;
    private int mTotalEnteredQty = 0;

    // add Header listview details
    private TextView headerProductNameTV;
    private TextView headerPieceQTY;
    private TextView headerCaseQty;
    private TextView headerOuterQty;
    private TextView headerTotal;


    private Button mBtnDone;
    private Toolbar toolbar;


    public BatchAllocationDialog(Context context, ProductMasterBO product,
                                 BatchAllocation batchAllocation) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        RelativeLayout ll = (RelativeLayout) LayoutInflater.from(context)
                .inflate(R.layout.dialog_batch_allocation, null);
        setContentView(ll);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        this.mContext = context;
        this.productBO = product;
        this.mBatchAllocation = batchAllocation;
        bmodel = (BusinessModel) mContext.getApplicationContext();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title_batch_allocation);
        toolbar.setTitleTextColor(Color.WHITE);
        TextView mTitle = (TextView) toolbar.findViewById(R.id.tv_toolbar_title);
        mTitle.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        mBatchAllocationLV = (ListView) findViewById(R.id.list);

        mTotalTV = (TextView) findViewById(R.id.totalValue);
        mBtnDone = (Button) findViewById(R.id.btn_done);

        mBtnDone.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        mBtnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBalanceQty = productBO.getOrderedPcsQty()
                        + (productBO.getOrderedCaseQty() * productBO
                        .getCaseSize())
                        + (productBO.getOrderedOuterQty() * productBO
                        .getOutersize()) - mTotalEnteredQty;
                if (mBalanceQty == 0) {
                    BatchAllocationDialog.this.mBatchAllocation.onResume();
                    BatchAllocationDialog.this.dismiss();
                } else {
                    Toast.makeText(mContext, mContext.getResources().getString(R.string.text_batch_allocated_mismatch),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });


        loadData();


    }

    private void loadData() {

        myList = new ArrayList<ProductMasterBO>();


        // entered qty total size
        mOrderedQty = productBO.getOrderedPcsQty()
                + (productBO.getOrderedCaseQty() * productBO.getCaseSize())
                + (productBO.getOrderedOuterQty() * productBO.getOutersize());
        mBatctAllocationList = bmodel.batchAllocationHelper
                .getBatchlistByProductID().get(productBO.getProductID());
        if (mBatctAllocationList == null) {
            Toast.makeText(
                    mContext,
                    mContext.getResources().getString(
                            R.string.no_products_exists), Toast.LENGTH_SHORT)
                    .show();

            return;
        } else {
            for (ProductMasterBO productBo : mBatctAllocationList) {
                if (productBo.getSIH() > 0) {
                    myList.add(productBo);
                }

            }

            addHeaderListView();

            MyAdapter adapter = new MyAdapter();
            mBatchAllocationLV.setAdapter(adapter);

        }

    }

    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return myList.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            final ViewHolder holder;

            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(R.layout.dialog_batch_allocation_row,
                        parent, false);
                holder = new ViewHolder();
                holder.psname = (TextView) row.findViewById(R.id.tvBatchNo);
                holder.batchno = (TextView) row.findViewById(R.id.tvBatchName);
                holder.caseqtyEditText = (EditText) row
                        .findViewById(R.id.orderQTYinCase);
                holder.pieceqty = (EditText) row
                        .findViewById(R.id.orderQTYinpiece);

                holder.total = (TextView) row.findViewById(R.id.total);
                holder.mfgDateTV = (TextView) row
                        .findViewById(R.id.tv_mfg_date);
                holder.expDateTV = (TextView) row
                        .findViewById(R.id.tv_exp_date);
                holder.outerQty = (EditText) row
                        .findViewById(R.id.outerorderQTYinCase);

                holder.psname.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.batchno.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.total.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.mfgDateTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.expDateTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                ((TextView) row.findViewById(R.id.mfdTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                ((TextView) row.findViewById(R.id.expTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                ((TextView) row.findViewById(R.id.totalTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

                if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE)
                    ((LinearLayout) row.findViewById(R.id.llCase)).setVisibility(View.GONE);
                else {
                    try {
                        holder.caseqtyEditText.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                        ((TextView) row.findViewById(R.id.caseTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.caseTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.caseTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(R.id.caseTitle)
                                                    .getTag()));
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                }
                if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS)
                    ((LinearLayout) row.findViewById(R.id.llPc)).setVisibility(View.GONE);
                else {
                    try {
                        holder.pieceqty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                        ((TextView) row.findViewById(R.id.pcsTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.pcsTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.pcsTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(R.id.pcsTitle)
                                                    .getTag()));
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                }
                if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
                    ((LinearLayout) row.findViewById(R.id.llOuter)).setVisibility(View.GONE);
                else {
                    try {
                        holder.outerQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                        ((TextView) row.findViewById(R.id.outercaseTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.outercaseTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.outercaseTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(R.id.outercaseTitle)
                                                    .getTag()));
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                }

                holder.pieceqty.setOnTouchListener(new OnTouchListener() {

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {

                        QUANTITY = holder.pieceqty;
                        QUANTITY.setTag(holder.productObj);
                        int inType = holder.pieceqty.getInputType();
                        holder.pieceqty.setInputType(InputType.TYPE_NULL);
                        holder.pieceqty.onTouchEvent(event);
                        holder.pieceqty.setInputType(inType);
                        holder.pieceqty.requestFocus();
                        if (holder.pieceqty.getText().length() > 0)
                            holder.pieceqty.setSelection(holder.pieceqty.getText().length());
                        return true;
                    }
                });

                holder.pieceqty.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        // TODO Auto-generated method stub

                        double total = 0.0;
                        if (s != null) {
                            String qty = s.toString();

                            if (qty.length() > 0)
                                holder.pieceqty.setSelection(qty.length());

                            if (!qty.equals("")) {
                                int pieceQty = SDUtil.convertToInt(qty);

                                if (pieceQty <= holder.productObj.getSIH()) {
                                    if (bmodel.batchAllocationHelper
                                            .checkAndValidateBatchEnteredValue(
                                                    productBO,
                                                    holder.productObj, 0,
                                                    pieceQty)) {
                                        holder.productObj
                                                .setOrderedPcsQty(pieceQty);
                                    } else {

                                        /**
                                         * Delete the last entered number and
                                         * reset the qty
                                         **/
                                        holder.pieceqty
                                                .setText(qty.length() > 1 ? qty.substring(
                                                        0, qty.length() - 1)
                                                        : "0");
                                        Toast.makeText(
                                                mContext,
                                                String.format(
                                                        mContext.getResources()
                                                                .getString(
                                                                        R.string.exceed),
                                                        mOrderedQty),
                                                Toast.LENGTH_SHORT).show();
                                    }

                                } else {
                                    /**
                                     * Delete the last entered number and reset
                                     * the qty
                                     **/
                                    holder.pieceqty.setText(qty.length() > 1 ? qty
                                            .substring(0, qty.length() - 1)
                                            : "0");
                                    Toast.makeText(
                                            mContext,
                                            String.format(
                                                    mContext.getResources()
                                                            .getString(
                                                                    R.string.exceed),
                                                    holder.productObj.getSIH()),
                                            Toast.LENGTH_SHORT).show();
                                }
                                total = holder.productObj.getOrderedPcsQty()
                                        * holder.productObj.getSrp()
                                        + (holder.productObj
                                        .getOrderedCaseQty() * holder.productObj
                                        .getCsrp())
                                        + (holder.productObj
                                        .getOrderedOuterQty() * holder.productObj
                                        .getOsrp());


                                holder.total.setText(bmodel.formatValue(total)
                                        + "");

                                updateValue(); // update over all total value
                            }

                        }

                    }
                });
                holder.caseqtyEditText
                        .addTextChangedListener(new TextWatcher() {

                            @Override
                            public void onTextChanged(CharSequence s,
                                                      int start, int before, int count) {
                                // TODO Auto-generated method stub

                            }

                            @Override
                            public void beforeTextChanged(CharSequence s,
                                                          int start, int count, int after) {
                                // TODO Auto-generated method stub

                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                                // TODO Auto-generated method stub
                                if (productBO.getCaseSize() == 0) { // if uomID
                                    // is
                                    // zero,not
                                    // allow to
                                    // enter
                                    // case
                                    // value
                                    holder.caseqtyEditText
                                            .removeTextChangedListener(this);
                                    holder.caseqtyEditText.setText(0 + "");
                                    holder.caseqtyEditText
                                            .addTextChangedListener(this);
                                    return;
                                }
                                if (s != null) {
                                    double total = 0.0;
                                    String qty = s.toString();
                                    if (qty.length() > 0)
                                        holder.caseqtyEditText.setSelection(qty.length());
                                    if (!qty.equals("")) {
                                        int caseQty = SDUtil.convertToInt(qty);
                                        if ((caseQty * productBO.getCaseSize()) <= holder.productObj
                                                .getSIH()) {
                                            if (bmodel.batchAllocationHelper
                                                    .checkAndValidateBatchEnteredValue(
                                                            productBO,
                                                            holder.productObj,
                                                            1, caseQty)) {
                                                holder.productObj
                                                        .setOrderedCaseQty(caseQty);

                                            } else {
                                                /**
                                                 * Delete the last entered
                                                 * number and reset the qty
                                                 **/
                                                holder.caseqtyEditText
                                                        .setText(qty.length() > 1 ? qty
                                                                .substring(
                                                                        0,
                                                                        qty.length() - 1)
                                                                : "0");
                                                Toast.makeText(
                                                        mContext,
                                                        String.format(
                                                                mContext.getResources()
                                                                        .getString(
                                                                                R.string.exceed),
                                                                mOrderedQty),
                                                        Toast.LENGTH_SHORT)
                                                        .show();
                                            }
                                        } else {
                                            /**
                                             * Delete the last entered number
                                             * and reset the qty
                                             **/
                                            holder.caseqtyEditText.setText(qty
                                                    .length() > 1 ? qty
                                                    .substring(0,
                                                            qty.length() - 1)
                                                    : "0");
                                            Toast.makeText(
                                                    mContext,
                                                    String.format(
                                                            mContext.getResources()
                                                                    .getString(
                                                                            R.string.exceed),
                                                            holder.productObj
                                                                    .getSIH()),
                                                    Toast.LENGTH_SHORT).show();
                                        }

                                        total = holder.productObj
                                                .getOrderedPcsQty()
                                                * holder.productObj.getSrp()
                                                + (holder.productObj
                                                .getOrderedCaseQty() * holder.productObj
                                                .getCsrp())
                                                + (holder.productObj
                                                .getOrderedOuterQty() * holder.productObj
                                                .getOsrp());


                                        holder.total.setText(bmodel
                                                .formatValue(total) + "");

                                        updateValue(); // update over all total
                                        // value
                                    }
                                }

                            }
                        });
                holder.caseqtyEditText
                        .setOnTouchListener(new OnTouchListener() {

                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                QUANTITY = holder.caseqtyEditText;
                                QUANTITY.setTag(holder.productObj);
                                int inType = holder.caseqtyEditText
                                        .getInputType();
                                holder.caseqtyEditText
                                        .setInputType(InputType.TYPE_NULL);
                                holder.caseqtyEditText.onTouchEvent(event);
                                holder.caseqtyEditText.setInputType(inType);
                                holder.caseqtyEditText.requestFocus();
                                if (holder.caseqtyEditText.getText().length() > 0)
                                    holder.caseqtyEditText.setSelection(holder.caseqtyEditText.getText().length());
                                return true;
                            }
                        });

                holder.outerQty.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                        // TODO Auto-generated method stub

                        if (productBO.getOutersize() == 0) {// if oUuomID is
                            // zero,not allow to
                            // enter outer value
                            holder.outerQty.removeTextChangedListener(this);
                            holder.outerQty.setText(0 + "");
                            holder.outerQty.addTextChangedListener(this);
                            return;
                        }
                        if (s != null) {

                            double total = 0.0;
                            String qty = s.toString();
                            if (qty.length() > 0)
                                holder.outerQty.setSelection(qty.length());
                            if (!qty.equals("")) {
                                int outerQty = SDUtil.convertToInt(qty);
                                if ((outerQty * productBO.getOutersize()) <= holder.productObj
                                        .getSIH()) {
                                    if (bmodel.batchAllocationHelper
                                            .checkAndValidateBatchEnteredValue(
                                                    productBO,
                                                    holder.productObj, 2,
                                                    outerQty)) {
                                        holder.productObj
                                                .setOrderedOuterQty(outerQty);

                                    } else {
                                        /**
                                         * Delete the last entered number and
                                         * reset the qty
                                         **/
                                        holder.outerQty
                                                .setText(qty.length() > 1 ? qty.substring(
                                                        0, qty.length() - 1)
                                                        : "0");
                                        Toast.makeText(
                                                mContext,
                                                String.format(
                                                        mContext.getResources()
                                                                .getString(
                                                                        R.string.exceed),
                                                        mOrderedQty),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    /**
                                     * Delete the last entered number and reset
                                     * the qty
                                     **/
                                    holder.outerQty.setText(qty.length() > 1 ? qty
                                            .substring(0, qty.length() - 1)
                                            : "0");
                                    Toast.makeText(
                                            mContext,
                                            String.format(
                                                    mContext.getResources()
                                                            .getString(
                                                                    R.string.exceed),
                                                    holder.productObj.getSIH()),
                                            Toast.LENGTH_SHORT).show();
                                }

                                total = holder.productObj.getOrderedPcsQty()
                                        * holder.productObj.getSrp()
                                        + (holder.productObj
                                        .getOrderedCaseQty() * holder.productObj
                                        .getCsrp())
                                        + (holder.productObj
                                        .getOrderedOuterQty() * holder.productObj
                                        .getOsrp());


                                holder.total.setText(total + "");

                                updateValue(); // update over all total value
                            }
                        }

                    }
                });
                holder.outerQty.setOnTouchListener(new OnTouchListener() {

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        QUANTITY = holder.outerQty;
                        QUANTITY.setTag(holder.productObj);
                        int inType = holder.outerQty.getInputType();
                        holder.outerQty.setInputType(InputType.TYPE_NULL);
                        holder.outerQty.onTouchEvent(event);
                        holder.outerQty.setInputType(inType);
                        if (holder.outerQty.getText().length() > 0)
                            holder.outerQty.setSelection(holder.outerQty.getText().length());
                        return true;
                    }
                });
                row.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }
            holder.productObj = myList.get(position);
            holder.ref = position;
            holder.productId = holder.productObj.getProductID();
            holder.productCode = holder.productObj.getProductCode();
            holder.psname.setText("Batch " + position + 1);
            holder.batchno.setText("BATCH NO: " + holder.productObj.getBatchNo());
            holder.pname = holder.productObj.getProductName();

            holder.caseSize = holder.productObj.getCaseSize();
            holder.stockInHand = holder.productObj.getSIH();

            holder.outerQty
                    .setText(holder.productObj.getOrderedOuterQty() + "");
            holder.caseqtyEditText.setText(holder.productObj
                    .getOrderedCaseQty() + "");
            holder.pieceqty.setText(holder.productObj.getOrderedPcsQty() + "");
            holder.mfgDateTV.setText(": " + holder.productObj.getMfgDate());
            holder.expDateTV.setText(": " + holder.productObj.getExpDate());

            if (productBO.getOuUomid() == 0 || !productBO.isOuterMapped()) {
                holder.outerQty.setEnabled(false);
            } else {
                holder.outerQty.setEnabled(true);
            }
            if (productBO.getCaseUomId() == 0 || !productBO.isCaseMapped()) {
                holder.caseqtyEditText.setEnabled(false);
            } else {
                holder.caseqtyEditText.setEnabled(true);
            }
            if (productBO.getPcUomid() == 0 || !productBO.isPieceMapped()) {
                holder.pieceqty.setEnabled(false);
            } else {
                holder.pieceqty.setEnabled(true);
            }
            TypedArray typearr = mContext.getTheme().obtainStyledAttributes(R.styleable.MyTextView);
            if (position % 2 == 0) {
                row.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor_alt, 0));
            } else {
                row.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor, 0));
            }

            return (row);

        }
    }

    class ViewHolder {

        ProductMasterBO productObj;
        String productId, productCode, pname;
        int caseSize, stockInHand;// product id
        TextView psname, batchno;
        TextView mfgDateTV, expDateTV;
        EditText caseqtyEditText, pieceqty, outerQty;
        int ref;
        TextView total;
    }

    public void numberPressed(View vw) {

        // ProductMasterBO temp = (ProductMasterBO) QUANTITY.getTag();
        if (QUANTITY == null) {
            bmodel.showAlert(
                    mContext.getResources().getString(
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

    private void eff() {
        String s = (String) QUANTITY.getText().toString();
        if (!s.equals("0") && !s.equals("0.0")) {
            QUANTITY.setText(QUANTITY.getText() + append);
        } else
            QUANTITY.setText(append);
    }

    /**
     * Method to use update listview values and total
     */

    private void updateValue() {


        double totalValue = 0.0;
        double batchwiseTotalValue = 0.0;
        mTotalEnteredQty = 0;
        for (ProductMasterBO batchProduct : mBatctAllocationList) {
            if (batchProduct.getOrderedPcsQty() > 0
                    || batchProduct.getOrderedCaseQty() > 0
                    || batchProduct.getOrderedOuterQty() > 0) {
                batchwiseTotalValue = (batchProduct.getOrderedPcsQty() * batchProduct
                        .getSrp())
                        + (batchProduct.getOrderedCaseQty() * batchProduct
                        .getCsrp())
                        + (batchProduct.getOrderedOuterQty() * batchProduct
                        .getOsrp());


                totalValue = totalValue + batchwiseTotalValue;

                mTotalEnteredQty = mTotalEnteredQty
                        + batchProduct.getOrderedPcsQty()
                        + (batchProduct.getOrderedCaseQty() * productBO
                        .getCaseSize())
                        + (batchProduct.getOrderedOuterQty() * productBO
                        .getOutersize());
            }
        }
        productBO.setBatchwiseTotal(totalValue);
        productBO.setTotalamount(totalValue);
        mTotalTV.setText(bmodel.formatValue(totalValue) + "");

    }

    /**
     * display order details to user,how much order taken
     */
    private void addHeaderListView() {

        headerProductNameTV = (TextView) findViewById(R.id.tv_product_name);
        headerPieceQTY = (TextView) findViewById(R.id.tv_pcs_qty);
        headerCaseQty = (TextView) findViewById(R.id.tv_case_qty);
        headerOuterQty = (TextView) findViewById(R.id.tv_outer_qty);

        headerProductNameTV.setText(productBO.getProductShortName());
        headerPieceQTY.setText(productBO.getOrderedPcsQty() + "");
        headerCaseQty.setText(productBO.getOrderedCaseQty() + "");
        headerOuterQty.setText(productBO.getOrderedOuterQty() + "");
        headerPieceQTY.setEnabled(false);
        headerCaseQty.setEnabled(false);
        headerOuterQty.setEnabled(false);

        //typeface
        ((TextView) findViewById(R.id.totalTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        headerProductNameTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        mTotalTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        ((View) findViewById(R.id.view_dotted_line)).setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE) {
            ((LinearLayout) findViewById(R.id.llCase)).setVisibility(View.GONE);
            ((TextView) findViewById(R.id.tvCaseSeparator)).setVisibility(View.GONE);
        } else {
            try {
                ((TextView) findViewById(R.id.caseTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                headerCaseQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                        R.id.caseTitle).getTag()) != null)
                    ((TextView) findViewById(R.id.caseTitle))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(findViewById(R.id.caseTitle)
                                            .getTag()));
            } catch (Exception e) {
                Commons.printException(e);
            }
        }
        if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS)
            ((LinearLayout) findViewById(R.id.llPc)).setVisibility(View.GONE);
        else {
            try {
                ((TextView) findViewById(R.id.pcsTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                headerPieceQTY.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                        R.id.pcsTitle).getTag()) != null)
                    ((TextView) findViewById(R.id.pcsTitle))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(findViewById(R.id.pcsTitle)
                                            .getTag()));
            } catch (Exception e) {
                Commons.printException(e);
            }
        }
        if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE) {
            ((LinearLayout) findViewById(R.id.llOuter)).setVisibility(View.GONE);
            ((TextView) findViewById(R.id.tvOuterSeparator)).setVisibility(View.GONE);
        } else {
            try {
                ((TextView) findViewById(R.id.outercaseTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                headerOuterQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                        R.id.outercaseTitle).getTag()) != null)
                    ((TextView) findViewById(R.id.outercaseTitle))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(findViewById(R.id.outercaseTitle)
                                            .getTag()));
            } catch (Exception e) {
                Commons.printException(e);
            }
        }
        try {
            if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                    R.id.totalTitle).getTag()) != null)
                ((TextView) findViewById(R.id.totalTitle))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(findViewById(R.id.totalTitle)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException(e);
        }


    }


}
