package com.ivy.cpg.view.initiative;

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
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class InitiativeEditActivity extends IvyBaseActivityNoActionBar {

    private BusinessModel bmodel;
    private TextView productName;
    private ListView lvwplist;
    private ArrayList<ProductMasterBO> mylist;
    private EditText QUANTITY;
    private String append = "";
    private Vector<String> initiativeProductIds;
    private TextView initQty, initValue;
    private String strInitQty = "", strValue = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_initiative);

        bmodel = (BusinessModel) getApplicationContext();

        InitiativeHeaderBO initHeaderBO = null;

        if (getIntent().getExtras() != null)
            initHeaderBO = getIntent().getParcelableExtra("INITIATIVE_BO");

        if (initHeaderBO == null) {
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView mScreenTitleTV = findViewById(R.id.tv_toolbar_title);
        mScreenTitleTV.setTypeface(FontUtils.getFontBalooHai(this, FontUtils.FontType.REGULAR));
        mScreenTitleTV.setText(initHeaderBO.getDescription());
        toolbar.setTitle("");
        Button btn_done = findViewById(R.id.btn_done);
        btn_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateBatchItem();
                finish();
            }
        });

        //to hide the default keyboard enable
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        TextView txt_initValue = findViewById(R.id.txt_init_value);

        initValue = findViewById(R.id.initValue);
        initQty = findViewById(R.id.initQty);

        TextView initLabel = findViewById(R.id.widget63);
        String strLabel = getResources().getString(R.string.init_qty) + "/" +
                getResources().getString(R.string.tot_qty);
        initLabel.setText(strLabel);

        TextView initValueLabel = findViewById(R.id.initvalue_title);
        String strValueLabel = getResources().getString(R.string.init_value) + "/" +
                getResources().getString(R.string.total_value);
        initValueLabel.setText(strValueLabel);

        productName = findViewById(R.id.productName2);
        productName.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                int inType = productName.getInputType();
                productName.setInputType(InputType.TYPE_NULL);
                productName.onTouchEvent(event);
                productName.setInputType(inType);
                return true;
            }
        });
        Vector<InitiativeDetailBO> initiativeDetailBO;

        if (initHeaderBO.getIsCombination() == 1)
            initiativeDetailBO = bmodel.initiativeHelper
                    .downloadCombinationInitiativeDetail(initHeaderBO
                            .getInitiativeId());
        else
            initiativeDetailBO = bmodel.initiativeHelper
                    .downloadInitiativeDetail(initHeaderBO, bmodel
                            .getAppDataProvider().getRetailMaster().getRetailerID(), bmodel
                            .getAppDataProvider().getRetailMaster().getSubchannelid());

        float sumForAnd = 0;
        float totalSumForAnd = 0;

        initiativeProductIds = new Vector<>();
        for (int i = 0; i < initiativeDetailBO.size(); i++) {
            initiativeProductIds.add(initiativeDetailBO.get(i).getProductId()
                    + "");
            sumForAnd = sumForAnd
                    + (initiativeDetailBO.get(i).getInitiativeValue() - initiativeDetailBO
                    .get(i).getInitiativeBalanceValue());

            totalSumForAnd = totalSumForAnd
                    + initiativeDetailBO.get(i).getInitiativeValue();
        }

        // Following is used to shwo the Initial target of the Initaitive.
        String strInitValue;
        if (initHeaderBO.getKeyword().equals("AND")
                && bmodel.configurationMasterHelper.IS_CUMULATIVE_AND) {
            if (initHeaderBO.getType().equals("QTY")) {
                strInitValue = String.valueOf((int) sumForAnd) + "/" + String.valueOf((int) totalSumForAnd);
            } else {
                strInitValue = bmodel.formatValue(sumForAnd) + "/" + bmodel.formatValue(totalSumForAnd);
            }
        } else {
            if (initHeaderBO.getType().equals("QTY")) {
                strInitValue = String.valueOf((int) sumForAnd) + "/" + String.valueOf((int) initiativeDetailBO.get(0).getInitiativeValue());
            } else {
                strInitValue = bmodel.formatValue(sumForAnd) + "/" + bmodel.formatValue(initiativeDetailBO.get(0)
                        .getInitiativeValue());
            }
        }
        txt_initValue.setText(strInitValue);
        // End

        if (initHeaderBO.getType().equals(InitiativeHelper.VALUE_TYPE)) {
            if (initHeaderBO.getKeyword().equals("AND")
                    && bmodel.configurationMasterHelper.IS_CUMULATIVE_AND) {
                float k = (totalSumForAnd - sumForAnd) > 0 ? (totalSumForAnd - sumForAnd)
                        : 0;
                strValue = bmodel.formatValue(k);
            } else {
                float k = (initiativeDetailBO.get(0)
                        .getInitiativeBalanceValue() - sumForAnd) > 0 ? (initiativeDetailBO
                        .get(0).getInitiativeBalanceValue() - sumForAnd) : 0;
                strValue = bmodel.formatValue(k);
            }
        } else if (initHeaderBO.getType()
                .equals(InitiativeHelper.QUANTITY_TYPE)) {
            if (initHeaderBO.getKeyword().equals("AND")
                    && bmodel.configurationMasterHelper.IS_CUMULATIVE_AND) {
                float k = (totalSumForAnd - sumForAnd) > 0 ? (totalSumForAnd - sumForAnd)
                        : 0;
                strInitQty = String.valueOf(Math.round(k));
            } else {
                float k = (initiativeDetailBO.get(0).getInitiativeValue() - sumForAnd) > 0 ? (initiativeDetailBO
                        .get(0).getInitiativeValue() - sumForAnd) : 0;
                strInitQty = String.valueOf(Math.round(k));
            }

        }

        lvwplist = findViewById(R.id.list);
        lvwplist.setCacheColorHint(0);

        if (initHeaderBO.getType().equals("VAL") || initHeaderBO.getType().equals(InitiativeHelper.VALUE_TYPE)) {
            findViewById(R.id.initQty_LL).setVisibility(View.GONE);
        } else {
            findViewById(R.id.initValue_LL).setVisibility(View.GONE);
        }

        if (bmodel.configurationMasterHelper.SHOW_INIT_FOOTER) {
            findViewById(R.id.initQty_LL).setVisibility(View.GONE);
            initValueLabel.setVisibility(View.GONE);
            findViewById(R.id.initValue_LL).setVisibility(View.VISIBLE);
            initValue.setVisibility(View.GONE);
        }

        // On/Off order case and pcs
        if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE) {
            findViewById(R.id.caseTitle).setVisibility(View.GONE);
        } else {
            try {
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
        if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
            findViewById(R.id.pcsTitle).setVisibility(View.GONE);
        } else {
            try {
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
        if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
            findViewById(R.id.outercaseTitle).setVisibility(View.GONE);

        // hide sihtitle
        if (!bmodel.configurationMasterHelper.IS_STOCK_IN_HAND) {
            findViewById(R.id.sihtitle).setVisibility(View.GONE);
        } else {
            try {
                if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                        R.id.sihtitle).getTag()) != null)
                    ((TextView) findViewById(R.id.sihtitle))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(findViewById(R.id.sihtitle)
                                            .getTag()));
            } catch (Exception e) {
                Commons.printException(e);
            }
        }

        updateOrderTable();
    }

    private void updateOrderTable() {

        Vector<ProductMasterBO> items = bmodel.productHelper.getProductMaster();
        if (items == null) {
            bmodel.showAlert(
                    getResources().getString(
                            R.string.no_products_exists), 0);
            return;
        }
        int siz = items.size();
        mylist = new ArrayList<>();
        for (int i = 0; i < siz; ++i) {
            ProductMasterBO ret = items.elementAt(i);
            if (initiativeProductIds.contains(ret.getProductID())) {
                mylist.add(ret);
            }
        }
        MyAdapter mSchedule = new MyAdapter(mylist);
        lvwplist.setAdapter(mSchedule);
        updateValue();
    }

    private class MyAdapter extends ArrayAdapter<ProductMasterBO> {
        private ArrayList<ProductMasterBO> items;

        public MyAdapter(ArrayList<ProductMasterBO> items) {
            super(InitiativeEditActivity.this, R.layout.dialog_initiative_listrow, items);
            this.items = items;
        }

        public ProductMasterBO getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        @NonNull
        public View getView(final int position, View convertView,
                            @NonNull ViewGroup parent) {
            final ViewHolder holder;
            ProductMasterBO product = items.get(position);
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(R.layout.dialog_initiative_listrow,
                        parent, false);
                holder = new ViewHolder();
                holder.psname = row.findViewById(R.id.orderPRODNAME);
                holder.caseqtyEditText = row
                        .findViewById(R.id.orderQTYinCase);
                holder.pieceqty = row
                        .findViewById(R.id.orderQTYinpiece);

                holder.mrp = row.findViewById(R.id.mrp);
                holder.p4qty = row.findViewById(R.id.ppq);
                holder.msq = row.findViewById(R.id.msq);
                holder.total = row.findViewById(R.id.total);
                holder.ou_type = row.findViewById(R.id.OU_Type);
                holder.sih = row.findViewById(R.id.sih);
                holder.outerQty = row
                        .findViewById(R.id.outerorderQTYinCase);

                holder.pieceqty.addTextChangedListener(new TextWatcher() {
                    public void afterTextChanged(Editable s) {

                        String qty = s.toString();
                        if (qty.length() > 0)
                            holder.pieceqty.setSelection(qty.length());
                        /* Calculate the total pcs qty **/
                        float totalQty = (holder.productObj.getOrderedCaseQty() * holder.productObj
                                .getCaseSize())
                                + (SDUtil.convertToInt(qty))
                                + holder.productObj.getOrderedOuterQty()
                                * holder.productObj.getOutersize();


                        if (holder.productObj.isAllocation() == 1
                                && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
                            if (totalQty <= holder.productObj.getSIH()) {
                                if (!qty.equals("")) {
                                    holder.productObj.setOrderedPcsQty(SDUtil
                                            .convertToInt(qty));
                                }
                                double tot = (holder.productObj
                                        .getOrderedCaseQty()
                                        * holder.productObj
                                        .getCsrp())
                                        + (holder.productObj.getOrderedPcsQty() * holder.productObj
                                        .getSrp())
                                        + (holder.productObj
                                        .getOrderedOuterQty()
                                        * holder.productObj
                                        .getOsrp());
                                holder.total.setText(bmodel.formatValue(tot));
                            } else {
                                /* Show Toast **/
                                Toast.makeText(
                                        InitiativeEditActivity.this,
                                        String.format(getResources()
                                                        .getString(R.string.exceed),
                                                holder.productObj.getSIH()),
                                        Toast.LENGTH_SHORT).show();
                                /*
                                 * Delete the last entered number and reset the
                                 * qty
                                 **/
                                holder.pieceqty.setText(qty.length() > 1 ? qty
                                        .substring(0, qty.length() - 1) : "0");
                            }
                        } else {
                            if (!qty.equals("")) {
                                holder.productObj.setOrderedPcsQty(SDUtil
                                        .convertToInt(qty));
                            }
                            double tot = (holder.productObj.getOrderedCaseQty()
                                    * holder.productObj
                                    .getCsrp())
                                    + (holder.productObj.getOrderedPcsQty() * holder.productObj
                                    .getSrp())
                                    + (holder.productObj
                                    .getOrderedOuterQty()
                                    * holder.productObj
                                    .getOsrp());
                            holder.total.setText(bmodel.formatValue(tot));
                        }

                    }

                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {
                    }

                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {

                    }
                });

                if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE)
                    holder.caseqtyEditText.setVisibility(View.GONE);
                if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS)
                    holder.pieceqty.setVisibility(View.GONE);
                if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
                    holder.outerQty.setVisibility(View.GONE);

                holder.caseqtyEditText
                        .addTextChangedListener(new TextWatcher() {
                            public void afterTextChanged(Editable s) {
                                String qty = s.toString();
                                if (qty.length() > 0)
                                    holder.caseqtyEditText.setSelection(qty.length());
                                float totalQty = (SDUtil.convertToInt(qty) * holder.productObj
                                        .getCaseSize())
                                        + (holder.productObj.getOrderedPcsQty())
                                        + holder.productObj
                                        .getOrderedOuterQty()
                                        * holder.productObj.getOutersize();
                                if (holder.productObj.isAllocation() == 1
                                        && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
                                    if (totalQty <= holder.productObj.getSIH()) {
                                        if (!qty.equals("")) {
                                            holder.productObj
                                                    .setOrderedCaseQty(SDUtil
                                                            .convertToInt(qty));
                                        }

                                        double tot = (holder.productObj
                                                .getOrderedCaseQty()
                                                * holder.productObj
                                                .getCsrp())
                                                + (holder.productObj
                                                .getOrderedPcsQty() * holder.productObj
                                                .getSrp())
                                                + (holder.productObj
                                                .getOrderedOuterQty()
                                                * holder.productObj
                                                .getOsrp());
                                        holder.total.setText(bmodel
                                                .formatValue(tot));
                                    } else {
                                        /* Show Toast **/
                                        Toast.makeText(
                                                InitiativeEditActivity.this,
                                                String.format(
                                                        getResources()
                                                                .getString(
                                                                        R.string.exceed),
                                                        holder.productObj
                                                                .getSIH()),
                                                Toast.LENGTH_SHORT).show();

                                        /*
                                         * Delete the last entered number and
                                         * reset the qty
                                         **/
                                        holder.caseqtyEditText.setText(qty
                                                .length() > 1 ? qty.substring(
                                                0, qty.length() - 1) : "0");
                                    }
                                } else {
                                    if (!qty.equals("")) {
                                        holder.productObj
                                                .setOrderedCaseQty(SDUtil
                                                        .convertToInt(qty));
                                    }

                                    double tot = (holder.productObj
                                            .getOrderedCaseQty()
                                            * holder.productObj
                                            .getCsrp())
                                            + (holder.productObj
                                            .getOrderedPcsQty() * holder.productObj
                                            .getSrp())
                                            + (holder.productObj
                                            .getOrderedOuterQty()
                                            * holder.productObj
                                            .getOsrp());
                                    holder.total.setText(bmodel.formatValue(tot));
                                }

                            }

                            public void beforeTextChanged(CharSequence s,
                                                          int start, int count, int after) {
                            }

                            public void onTextChanged(CharSequence s,
                                                      int start, int before, int count) {

                            }
                        });
                holder.outerQty.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void afterTextChanged(Editable s) {
                        String qty = s.toString();
                        if (qty.length() > 0)
                            holder.outerQty.setSelection(qty.length());
                        float totalQty = (SDUtil.convertToInt(qty) * holder.productObj
                                .getOutersize())
                                + (holder.productObj.getOrderedPcsQty())
                                + (holder.productObj.getOrderedCaseQty() * holder.productObj
                                .getCaseSize());
                        if (holder.productObj.isAllocation() == 1
                                && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
                            if (totalQty <= holder.productObj.getSIH()) {
                                if (!qty.equals("")) {
                                    holder.productObj
                                            .setOrderedOuterQty(SDUtil
                                                    .convertToInt(qty));
                                }

                                double tot = (holder.productObj
                                        .getOrderedCaseQty()
                                        * holder.productObj
                                        .getCsrp())
                                        + (holder.productObj.getOrderedPcsQty() * holder.productObj
                                        .getSrp())
                                        + (holder.productObj
                                        .getOrderedOuterQty()
                                        * holder.productObj
                                        .getOsrp());
                                holder.total.setText(bmodel.formatValue(tot));
                            } else {
                                /* Show Toast **/
                                Toast.makeText(
                                        InitiativeEditActivity.this,
                                        String.format(getResources()
                                                        .getString(R.string.exceed),
                                                holder.productObj.getSIH()),
                                        Toast.LENGTH_SHORT).show();

                                /*
                                 * Delete the last entered number and reset the
                                 * qty
                                 **/
                                holder.outerQty.setText(qty.length() > 1 ? qty
                                        .substring(0, qty.length() - 1) : "0");
                            }
                        } else {
                            if (!qty.equals("")) {
                                holder.productObj.setOrderedOuterQty(SDUtil
                                        .convertToInt(qty));
                            }

                            double tot = (holder.productObj.getOrderedCaseQty()
                                    * holder.productObj
                                    .getCsrp())
                                    + (holder.productObj.getOrderedPcsQty() * holder.productObj
                                    .getSrp())
                                    + (holder.productObj
                                    .getOrderedOuterQty()
                                    * holder.productObj
                                    .getOsrp());
                            holder.total.setText(bmodel.formatValue(tot));
                        }

                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {
                        // TODO Auto-generated method stub

                    }
                });
                holder.caseqtyEditText
                        .setOnTouchListener(new View.OnTouchListener() {
                            public boolean onTouch(View v, MotionEvent event) {
                                productName.setText(holder.pname);
                                QUANTITY = holder.caseqtyEditText;
                                int inType = holder.caseqtyEditText
                                        .getInputType();
                                holder.caseqtyEditText
                                        .setInputType(InputType.TYPE_NULL);
                                holder.caseqtyEditText.onTouchEvent(event);
                                holder.caseqtyEditText.setInputType(inType);
                                if (holder.caseqtyEditText.getText().length() > 0)
                                    holder.caseqtyEditText.setSelection(holder.caseqtyEditText.getText().length());
                                return true;
                            }
                        });

                holder.pieceqty.setOnTouchListener(new View.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        productName.setText(holder.pname);
                        QUANTITY = holder.pieceqty;
                        int inType = holder.pieceqty.getInputType();
                        holder.pieceqty.setInputType(InputType.TYPE_NULL);
                        holder.pieceqty.onTouchEvent(event);
                        holder.pieceqty.setInputType(inType);
                        if (holder.pieceqty.getText().length() > 0)
                            holder.pieceqty.setSelection(holder.pieceqty.getText().length());
                        return true;
                    }
                });
                holder.outerQty.setOnTouchListener(new View.OnTouchListener() {

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        productName.setText(holder.pname);
                        QUANTITY = holder.outerQty;
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
                        productName.setText(holder.pname);
                    }
                });

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }
            holder.productObj = product;
            holder.ref = position;
            holder.productId = holder.productObj.getProductID();
            holder.productCode = holder.productObj.getProductCode();
            holder.psname.setText(holder.productObj.getProductShortName());
            holder.pname = holder.productObj.getProductName();

            holder.caseSize = holder.productObj.getCaseSize();
            holder.stockInHand = holder.productObj.getSIH();

            holder.mrp.setText("Price: " + bmodel.formatValue(holder.productObj.getSrp()));
            holder.p4qty.setText("P.PQ: " + holder.productObj.getRetailerWiseProductWiseP4Qty());

            holder.caseqtyEditText.setText(String.valueOf(holder.productObj.getOrderedCaseQty()));
            holder.pieceqty.setText(String.valueOf(holder.productObj.getOrderedPcsQty()));
            holder.outerQty.setText(String.valueOf(holder.productObj.getOrderedOuterQty()));
            holder.msq.setText("MSQ: " + holder.productObj.getMSQty());

            holder.ou_type.setText("OU: " + holder.productObj.getOU());

            if (holder.productObj.getOuUomid() == 0 || !holder.productObj.isOuterMapped()) {
                holder.outerQty.setEnabled(false);
            } else {
                holder.outerQty.setEnabled(true);
            }
            if (holder.productObj.getCaseUomId() == 0 || !holder.productObj.isCaseMapped()) {
                holder.caseqtyEditText.setEnabled(false);
            } else {
                holder.caseqtyEditText.setEnabled(true);
            }
            if (holder.productObj.getPcUomid() == 0 || !holder.productObj.isPieceMapped()) {
                holder.pieceqty.setEnabled(false);
            } else {
                holder.pieceqty.setEnabled(true);
            }

            // Hide SIH or set value
            if (bmodel.configurationMasterHelper.IS_STOCK_IN_HAND)
                if (holder.productObj.isAllocation() == 1) {
                    holder.sih.setText(String.valueOf(holder.productObj.getSIH()));
                } else if (bmodel.configurationMasterHelper.IS_INVOICE) {
                    holder.sih.setText(String.valueOf(product.getSIH()));
                } else {
                    holder.sih.setText("-");
                }
            else
                holder.sih.setVisibility(View.GONE);
            return (row);
        }
    }

    class ViewHolder {

        ProductMasterBO productObj;
        String productId, productCode, pname;
        int caseSize, stockInHand;// product id
        TextView psname, mrp, p4qty, sih;
        EditText caseqtyEditText, pieceqty, outerQty;
        int ref;
        TextView msq, total, ou_type;
    }

    public void updateValue() {
        double total_value = 0;
        int lpccount = 0;
        int count = 0;
        double temp;

        Vector items = bmodel.productHelper.getProductMaster();
        if (items == null) {
            return;
        }
        int siz = items.size();
        if (siz == 0)
            return;
        for (int i = 0; i < siz; i++) {
            ProductMasterBO ret = (ProductMasterBO) items.elementAt(i);
            if (initiativeProductIds.contains(ret.getProductID())) {
                lpccount = lpccount + 1;

                int j = (ret.getOrderedPcsQty())
                        + (ret.getOrderedCaseQty() * ret.getCaseSize())
                        + (ret.getOrderedOuterQty() * ret.getOutersize());

                temp = (ret.getOrderedPcsQty() * ret.getSrp())
                        + (ret.getOrderedCaseQty() * ret
                        .getCsrp())
                        + (ret.getOrderedOuterQty() * ret
                        .getOsrp());
                total_value = total_value + temp;

                count = count + j;

            }
        }

        strValue = strValue + "/" + bmodel.formatValue(total_value);
        initValue.setText(strValue);
        strInitQty = strInitQty + "/" + String.valueOf(count);
        initQty.setText(strInitQty);
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
                    getResources().getString(
                            R.string.please_select_item), 0);
        } else {
            int id = vw.getId();
            if (id == R.id.calcdel) {
                int s = SDUtil.convertToInt(QUANTITY.getText()
                        .toString());
                s = s / 10;
                QUANTITY.setText(String.valueOf(s));
            } else {
                Button ed = findViewById(vw.getId());
                append = ed.getText().toString();
                eff();
            }
        }
        updateValue();
    }

    private void updateBatchItem() {

        if (mylist != null && mylist.size() > 0) {
            for (ProductMasterBO product : mylist) {
                List<ProductMasterBO> batchList = bmodel.batchAllocationHelper.getBatchlistByProductID().get(product.getProductID());

                int totQty = product.getOrderedPcsQty() + (product.getOrderedCaseQty() * product.getCaseSize()) + (product.getOrderedOuterQty() * product.getOutersize());

                int totBatchQty = 0;
                if (batchList != null && batchList.size() > 0) {
                    for (ProductMasterBO batchProduct : batchList) {
                        int batchQty = batchProduct.getOrderedPcsQty() + (batchProduct.getOrderedCaseQty() * product.getCaseSize()) + (batchProduct.getOrderedOuterQty() * product.getOutersize());
                        totBatchQty += batchQty;
                    }
                }
                if (totQty != totBatchQty) {
                    bmodel.batchAllocationHelper.updateBatchlist(product);
                }

            }
        }
    }
}
