package com.ivy.sd.png.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
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
import com.ivy.sd.png.bo.InitiativeDetailBO;
import com.ivy.sd.png.bo.InitiativeHeaderBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.InitiativeHelper;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class InitiativeDialog extends Dialog implements
        OnClickListener {

    private BusinessModel bmodel;
    private Context context;
    private TextView totalValueText, totalQty, initvalue, initQty, productName,
            txt_initValue, txt_init_value_suffix;
    private ListView lvwplist;
    private ArrayList<ProductMasterBO> mylist;
    private EditText QUANTITY;
    private String append = "";
    private Vector<String> initiativeProductIds;
    private InitiativeActivity initAct;
    Toolbar toolbar;

    public InitiativeDialog(Context context, InitiativeHeaderBO initHeaderBO,
                            InitiativeActivity init) {
        super(context);
        this.context = context;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = getWindow();
        lp.copyFrom(window.getAttributes());
        // This makes the dialog take up the full width
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        initAct = init;

        RelativeLayout ll = (RelativeLayout) LayoutInflater.from(context)
                .inflate(R.layout.dialog_initiative, null);
        setContentView(ll);
        this.getWindow().setLayout(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);

        // setContentView(R.layout.initiativedialog);
        setCancelable(true);

        bmodel = (BusinessModel) context.getApplicationContext();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView mScreenTitleTV = (TextView) findViewById(R.id.tv_toolbar_title);
        mScreenTitleTV.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        mScreenTitleTV.setText(initHeaderBO.getDescription());
        toolbar.setTitle("");
        Button btn_done = (Button) findViewById(R.id.btn_done);
        btn_done.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        btn_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateBatchItem();
                InitiativeDialog.this.initAct.onResume();
                dismiss();
            }
        });

        //to hide the default keyboard enable
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        txt_initValue = (TextView) findViewById(R.id.txt_init_value);
        txt_init_value_suffix = (TextView) findViewById(R.id.txt_init_value_suffix);
        totalValueText = (TextView) findViewById(R.id.totalValue);
        totalQty = (TextView) findViewById(R.id.totalQty);

        initvalue = (TextView) findViewById(R.id.initValue);
        initQty = (TextView) findViewById(R.id.initQty);

        productName = (TextView) findViewById(R.id.productName2);
        productName.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                int inType = productName.getInputType();
                productName.setInputType(InputType.TYPE_NULL);
                productName.onTouchEvent(event);
                productName.setInputType(inType);
                return true;
            }
        });
        initQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        totalQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        initvalue.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        totalValueText.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        txt_initValue.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        txt_init_value_suffix.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        ((TextView) findViewById(R.id.txt_init_value_lable)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        ((TextView) findViewById(R.id.widget63)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        ((TextView) findViewById(R.id.widget65)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        ((TextView) findViewById(R.id.initvalue_title)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        ((TextView) findViewById(R.id.totalVolume_title)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        productName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) findViewById(R.id.productnametitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.caseTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.outercaseTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.pcsTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.sihtitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.totaltitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        Vector<InitiativeDetailBO> initiativeDetailBO = new Vector<InitiativeDetailBO>();

        if (initHeaderBO.getIsCombination() == 1)
            initiativeDetailBO = bmodel.initiativeHelper
                    .downloadCombinationInitiativeDetail(initHeaderBO
                            .getInitiativeId());
        else
            initiativeDetailBO = bmodel.initiativeHelper
                    .downloadInitiativeDetail(initHeaderBO, bmodel
                            .getRetailerMasterBO().getRetailerID(), bmodel
                            .getRetailerMasterBO().getSubchannelid());

        float sumForAnd = 0;
        float totalSumForAnd = 0;

        initiativeProductIds = new Vector<String>();
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
        if (initHeaderBO.getKeyword().equals("AND")
                && bmodel.configurationMasterHelper.IS_CUMULATIVE_AND) {
            if (initHeaderBO.getType().equals("QTY")) {
                txt_initValue.setText((int) sumForAnd + "");// + "/"
//						+ (int) totalSumForAnd);
                txt_init_value_suffix.setText(((int) totalSumForAnd) + "");
            } else {
                txt_initValue.setText(bmodel.formatValue(sumForAnd) + "");// + "/"
//						+ bmodel.formatValue(totalSumForAnd));
                txt_init_value_suffix.setText((bmodel.formatValue(totalSumForAnd)) + "");
            }
        } else {
            if (initHeaderBO.getType().equals("QTY")) {
                txt_initValue.setText((int) sumForAnd + "");// + "/"
//						+ (int) initiativeDetailBO.get(0).getInitiativeValue());
                txt_init_value_suffix.setText(((int) initiativeDetailBO.get(0).getInitiativeValue()) + "");
            } else {
                txt_initValue.setText(bmodel.formatValue(sumForAnd));
//                        + "/"
//                        + bmodel.formatValue(initiativeDetailBO.get(0)
//                        .getInitiativeValue()));
                txt_init_value_suffix.setText((bmodel.formatValue(initiativeDetailBO.get(0)
                        .getInitiativeValue())) + "");
            }
        }
        // End

        if (initHeaderBO.getType().equals(InitiativeHelper.VALUE_TYPE)) {
            if (initHeaderBO.getKeyword().equals("AND")
                    && bmodel.configurationMasterHelper.IS_CUMULATIVE_AND) {
                float k = (totalSumForAnd - sumForAnd) > 0 ? (totalSumForAnd - sumForAnd)
                        : 0;
                initvalue.setText(bmodel.formatValue(k) + "");
            } else {
                float k = (initiativeDetailBO.get(0)
                        .getInitiativeBalanceValue() - sumForAnd) > 0 ? (initiativeDetailBO
                        .get(0).getInitiativeBalanceValue() - sumForAnd) : 0;
                initvalue.setText(bmodel.formatValue(k) + "");

            }
        } else if (initHeaderBO.getType()
                .equals(InitiativeHelper.QUANTITY_TYPE)) {
            if (initHeaderBO.getKeyword().equals("AND")
                    && bmodel.configurationMasterHelper.IS_CUMULATIVE_AND) {
                float k = (totalSumForAnd - sumForAnd) > 0 ? (totalSumForAnd - sumForAnd)
                        : 0;
                initQty.setText(Math.round(k) + "");
            } else {
                float k = (initiativeDetailBO.get(0).getInitiativeValue() - sumForAnd) > 0 ? (initiativeDetailBO
                        .get(0).getInitiativeValue() - sumForAnd) : 0;
                initQty.setText(Math.round(k) + "");
            }

        }

        lvwplist = (ListView) findViewById(R.id.lvwplist);
        lvwplist.setCacheColorHint(0);

        if (initHeaderBO.getType().equals("VAL") || initHeaderBO.getType().equals(InitiativeHelper.VALUE_TYPE)) {
            findViewById(R.id.initQty_LL).setVisibility(View.GONE);
        } else {
            findViewById(R.id.initValue_LL).setVisibility(View.GONE);
        }

        if (!bmodel.configurationMasterHelper.SHOW_INIT_FOOTER) {
            findViewById(R.id.initQty_LL).setVisibility(View.GONE);
            findViewById(R.id.initvalue_title).setVisibility(View.GONE);
            findViewById(R.id.initValue_LL).setVisibility(View.VISIBLE);
            initvalue.setVisibility(View.GONE);
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

    public void onBackPressed() {
        // do something on back.
        return;
    }


    private void updateOrderTable() {

        Vector<ProductMasterBO> items = bmodel.productHelper.getProductMaster();
        if (items == null) {
            bmodel.showAlert(
                    context.getResources().getString(
                            R.string.no_products_exists), 0);
            return;
        }
        int siz = items.size();
        mylist = new ArrayList<ProductMasterBO>();
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

    private ProductMasterBO product;

    private boolean numPressed;

    private class MyAdapter extends ArrayAdapter {
        private ArrayList<ProductMasterBO> items;

        public MyAdapter(ArrayList<ProductMasterBO> items) {
            super(context, R.layout.dialog_initiative_listrow, items);
            this.items = items;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            final ViewHolder holder;
            product = items.get(position);
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(R.layout.dialog_initiative_listrow,
                        parent, false);
                holder = new ViewHolder();
                holder.psname = (TextView) row.findViewById(R.id.orderPRODNAME);
                holder.caseqtyEditText = (EditText) row
                        .findViewById(R.id.orderQTYinCase);
                holder.pieceqty = (EditText) row
                        .findViewById(R.id.orderQTYinpiece);

                holder.mrp = (TextView) row.findViewById(R.id.mrp);
                holder.p4qty = (TextView) row.findViewById(R.id.ppq);
                holder.msq = (TextView) row.findViewById(R.id.msq);
                holder.total = (TextView) row.findViewById(R.id.total);
                holder.ou_type = (TextView) row.findViewById(R.id.OU_Type);
                holder.sih = (TextView) row.findViewById(R.id.sih);
                holder.outerQty = (EditText) row
                        .findViewById(R.id.outerorderQTYinCase);
                holder.psname.setTypeface(bmodel.configurationMasterHelper.getProductNameFont());
                holder.mrp.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
                holder.sih.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
                holder.total.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
                holder.caseqtyEditText.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
                holder.pieceqty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
                holder.p4qty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
                holder.msq.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
                holder.ou_type.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
                holder.outerQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));

                holder.pieceqty.addTextChangedListener(new TextWatcher() {
                    public void afterTextChanged(Editable s) {

                        String qty = s.toString();
                        /** Calculate the total pcs qty **/
                        float totalQty = (holder.productObj.getOrderedCaseQty() * holder.productObj
                                .getCaseSize())
                                + (SDUtil.convertToInt(qty))
                                + holder.productObj.getOrderedOuterQty()
                                * holder.productObj.getOutersize();

//						bmodel.batchAllocationHelper.updateBatchlist(holder.productObj);

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
                                /** Show Toast **/
                                int reamining = holder.productObj.getSIH()
                                        - ((holder.productObj
                                        .getOrderedCaseQty() * holder.productObj
                                        .getCaseSize()) + (holder.productObj
                                        .getOrderedPcsQty()));
                                Toast.makeText(
                                        context,
                                        String.format(context.getResources()
                                                        .getString(R.string.exceed),
                                                holder.productObj.getSIH()),
                                        Toast.LENGTH_SHORT).show();
                                /**
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
                            holder.total.setText(bmodel.formatValue(tot) + "");
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
                                        /** Show Toast **/
                                        int reamining = holder.productObj
                                                .getSIH()
                                                - ((holder.productObj
                                                .getOrderedCaseQty() * holder.productObj
                                                .getCaseSize()) + (holder.productObj
                                                .getOrderedPcsQty()));
                                        Toast.makeText(
                                                context,
                                                String.format(
                                                        context.getResources()
                                                                .getString(
                                                                        R.string.exceed),
                                                        holder.productObj
                                                                .getSIH()),
                                                Toast.LENGTH_SHORT).show();

                                        /**
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
                                    holder.total.setText(bmodel
                                            .formatValue(tot) + "");
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
                                /** Show Toast **/
                                int reamining = holder.productObj.getSIH()
                                        - ((holder.productObj
                                        .getOrderedCaseQty() * holder.productObj
                                        .getCaseSize()) + (holder.productObj
                                        .getOrderedPcsQty()));
                                Toast.makeText(
                                        context,
                                        String.format(context.getResources()
                                                        .getString(R.string.exceed),
                                                holder.productObj.getSIH()),
                                        Toast.LENGTH_SHORT).show();

                                /**
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
                            holder.total.setText(bmodel.formatValue(tot) + "");
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
                        .setOnTouchListener(new OnTouchListener() {
                            public boolean onTouch(View v, MotionEvent event) {
                                productName.setText(holder.pname);
                                QUANTITY = holder.caseqtyEditText;
                                int inType = holder.caseqtyEditText
                                        .getInputType();
                                holder.caseqtyEditText
                                        .setInputType(InputType.TYPE_NULL);
                                holder.caseqtyEditText.onTouchEvent(event);
                                holder.caseqtyEditText.setInputType(inType);
                                return true;
                            }
                        });

                holder.pieceqty.setOnTouchListener(new OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        productName.setText(holder.pname);
                        QUANTITY = holder.pieceqty;
                        int inType = holder.pieceqty.getInputType();
                        holder.pieceqty.setInputType(InputType.TYPE_NULL);
                        holder.pieceqty.onTouchEvent(event);
                        holder.pieceqty.setInputType(inType);
                        return true;
                    }
                });
                holder.outerQty.setOnTouchListener(new OnTouchListener() {

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        productName.setText(holder.pname);
                        QUANTITY = holder.outerQty;
                        int inType = holder.outerQty.getInputType();
                        holder.outerQty.setInputType(InputType.TYPE_NULL);
                        holder.outerQty.onTouchEvent(event);
                        holder.outerQty.setInputType(inType);
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

            holder.mrp.setText("Price: "
                    + bmodel.formatValue(holder.productObj.getSrp()) + "");
            holder.p4qty.setText("P.PQ: "
                    + holder.productObj.getRetailerWiseProductWiseP4Qty());

            holder.caseqtyEditText.setText(holder.productObj
                    .getOrderedCaseQty() + "");
            holder.pieceqty.setText(holder.productObj.getOrderedPcsQty() + "");
            holder.outerQty.setText(holder.productObj
                    .getOrderedOuterQty() + "");
            holder.msq.setText("MSQ: " + holder.productObj.getMSQty() + "");

            holder.ou_type.setText("OU: " + holder.productObj.getOU() + "");

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
                    holder.sih.setText(holder.productObj.getSIH() + "");
                } else if (bmodel.configurationMasterHelper.IS_INVOICE) {
                    holder.sih.setText(product.getSIH() + "");
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
        double temp = 0;

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

        totalValueText.setText(bmodel.formatValue(total_value) + "");
        totalQty.setText(count + "");
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
        updateValue();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        // TODO Auto-generated method stub

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
