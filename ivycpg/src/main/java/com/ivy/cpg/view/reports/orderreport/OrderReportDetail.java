package com.ivy.cpg.view.reports.orderreport;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.ivy.lib.Utils;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.SchemeProductBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.ReportHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.sd.print.CommonPrintPreviewActivity;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;

/**
 * Order Report Detail Screen
 */
public class OrderReportDetail extends IvyBaseActivityNoActionBar implements
        OnClickListener {

    private Button back;
    private TextView label_total, text_total, totalLines, tv_lbl_total_lines, tv_lbl_total_wgt,txtWeight;

    private BusinessModel businessModel;
    private OrderReportBO obj;

    private boolean isFromOrderReport;
    private double TotalValue;
    private float TotalWeight;
    private String TotalLines;

    private ArrayList<OrderReportBO> list;
    private ArrayList<SchemeProductBO> schemeProductList = new ArrayList<SchemeProductBO>();
    private ExpandableListView elv;
    private InputMethodManager inputManager;
    private TextView productName;
    private EditText mEdt_searchProductName;

    private String orderID = "";
    private ReportHelper reportHelper;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_order_report_detail);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

        businessModel = (BusinessModel) getApplicationContext();
        businessModel.setContext(this);

        inputManager = (InputMethodManager) getSystemService(
                INPUT_METHOD_SERVICE);
        productName = findViewById(R.id.productTvName);

        reportHelper = ReportHelper.getInstance(this);

        Toolbar toolbar = findViewById(R.id.toolbar);

        //total lines
        totalLines = findViewById(R.id.txttotallines);
        tv_lbl_total_lines = findViewById(R.id.lbl_total_lines);

        label_total = findViewById(R.id.label_totalValue);
        text_total = findViewById(R.id.txttotal);

        tv_lbl_total_wgt = findViewById(R.id.label_totalweight);
        txtWeight = findViewById(R.id.txtWeight);


        TextView outletName = findViewById(R.id.BtnBrandPrev);

        label_total.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.LIGHT));
        tv_lbl_total_lines.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.LIGHT));
        tv_lbl_total_wgt.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.LIGHT));
        txtWeight.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.LIGHT));

        try {
            back = findViewById(R.id.btnPersBack);
            back.setOnClickListener(this);

            try {
                if (businessModel.labelsMasterHelper.applyLabels(findViewById(
                        R.id.cqty).getTag()) != null)
                    ((TextView) findViewById(R.id.cqty))
                            .setText(businessModel.labelsMasterHelper
                                    .applyLabels(findViewById(R.id.cqty)
                                            .getTag()));
                if (businessModel.labelsMasterHelper.applyLabels(findViewById(
                        R.id.outid).getTag()) != null)
                    ((TextView) findViewById(R.id.outid))
                            .setText(businessModel.labelsMasterHelper
                                    .applyLabels(findViewById(R.id.outid)
                                            .getTag()));

                if (businessModel.labelsMasterHelper.applyLabels(findViewById(
                        R.id.outercqty).getTag()) != null)
                    ((TextView) findViewById(R.id.outercqty))
                            .setText(businessModel.labelsMasterHelper
                                    .applyLabels(findViewById(R.id.outercqty)
                                            .getTag()));
            } catch (Exception e) {
                Commons.printException(e);
            }


            Bundle extras = getIntent().getExtras();

            if (extras != null) {
                if (extras.containsKey("OBJ")) {
                    obj = extras
                            .getParcelable("OBJ");

                }
                if (extras.containsKey("isFromOrder")) {
                    isFromOrderReport = extras.getBoolean("isFromOrder");

                }
                if (extras.containsKey("TotalValue")) {
                    TotalValue = extras.getDouble("TotalValue");
                }
                if (extras.containsKey("TotalLines")) {
                    TotalLines = extras.getString("TotalLines");
                }
                if (extras.containsKey("TotalWeight")) {
                    TotalWeight = extras.getFloat("TotalWeight");
                }
            }


            String value = getResources().getString(R.string.order_report)
                    + obj.getRetailerName();
            outletName.setText(value);


            elv = findViewById(R.id.elv);

            setSupportActionBar(toolbar);
            // Set title to toolbar
            if (getSupportActionBar() != null)
                setScreenTitle(
                        getResources().getString(R.string.order_report_details));
            //     getSupportActionBar().setIcon(R.drawable.icon_order);
            // Used to on / off the back arrow icon
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Used to remove the app logo actionbar icon and set title as home
            // (title support click)
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            // Used to hide the app logo icon from actionbar
            // getSupportActionBar().setDisplayUseLogoEnabled(false);
            getSupportActionBar().setDisplayShowTitleEnabled(false);

            if (!businessModel.configurationMasterHelper.SHOW_ORDER_CASE)
                findViewById(R.id.cqty).setVisibility(View.GONE);
            if (!businessModel.configurationMasterHelper.SHOW_ORDER_PCS)
                findViewById(R.id.outid).setVisibility(View.GONE);
            if (!businessModel.configurationMasterHelper.SHOW_OUTER_CASE)
                findViewById(R.id.outercqty).setVisibility(View.GONE);
            if (!businessModel.configurationMasterHelper.SHOW_ORDER_WEIGHT)
                findViewById(R.id.title_weight).setVisibility(View.GONE);

            if (!businessModel.configurationMasterHelper.SHOW_STK_ORD_SRP) {
                findViewById(R.id.lpc).setVisibility(View.GONE);
            }


            orderID = obj.getOrderID();
            list = reportHelper.downloadOrderreportdetail(orderID);


            //scheme products
            schemeProductList = reportHelper.getSchemeProductDetails(orderID, false);

            if (schemeProductList != null && list != null) {
                if (list.get(list.size() - 1).getSchemeProducts() != null)
                    list.get(list.size() - 1).getSchemeProducts().addAll(schemeProductList);
                else
                    list.get(list.size() - 1).setSchemeProducts(schemeProductList);
            }

            updateOrderDetailsGrid();
        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    private void updateOrderDetailsGrid() {
        // double total = 0;

        // Show alert if error loading data.
        if (list == null) {
            businessModel.showAlert(
                    getResources().getString(R.string.unable_to_load_data), 0);
            return;
        }
        // Show alert if no order exist.
        if (list.size() == 0) {
            businessModel.showAlert(
                    getResources().getString(R.string.no_orders_available), 0);
            return;
        }


        if (businessModel.configurationMasterHelper.SHOW_TOTAL_LINES) {
            if (businessModel.configurationMasterHelper.SHOW_TOTAL_QTY_IN_ORDER_REPORT) {
                totalLines.setText(String.valueOf(reportHelper.getTotalQtyfororder(obj.getOrderID())));
                tv_lbl_total_lines.setText(getResources().getString(R.string.tot_qty));
            } else {
                totalLines.setText(TotalLines);
                tv_lbl_total_lines.setText(getResources().getString(R.string.tot_line));
            }

        } else {
            totalLines.setVisibility(View.GONE);
            tv_lbl_total_lines.setVisibility(View.GONE);
            findViewById(R.id.view1).setVisibility(View.GONE);
        }

        if (businessModel.configurationMasterHelper.SHOW_TOTAL_VALUE_ORDER) {
            text_total.setText(SDUtil.format(TotalValue,
                    businessModel.configurationMasterHelper.VALUE_PRECISION_COUNT,
                    0, businessModel.configurationMasterHelper.IS_DOT_FOR_GROUP));
        } else {
            text_total.setVisibility(View.GONE);
            label_total.setVisibility(View.GONE);
            findViewById(R.id.view2).setVisibility(View.GONE);
        }

        if (businessModel.configurationMasterHelper.SHOW_ORDER_WEIGHT){
            txtWeight.setText(Utils.formatAsTwoDecimal((double) TotalWeight));
        }else{
            tv_lbl_total_wgt.setVisibility(View.GONE);
            txtWeight.setVisibility(View.GONE);
        }



        elv.setAdapter(new MyAdapter());
        int orderedProductCount = list.size();
        for (int i = 0; i < orderedProductCount; i++) {
            ((ExpandableListView) elv).expandGroup(i);
        }
    }


    class MyAdapter extends BaseExpandableListAdapter {

        @Override
        public Object getChild(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(R.layout.row_orderdetail_report,
                        parent, false);
                holder = new ViewHolder();
                holder.tvwpsname = (TextView) row.findViewById(R.id.PRDNAME1);
                holder.tvwpsname.setMaxLines(businessModel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);
                holder.tvProductCode = (TextView) row.findViewById(R.id.product_code);
                holder.tvpcsqty = (TextView) row.findViewById(R.id.PRDQTY);
                holder.tvcaseqty = (TextView) row.findViewById(R.id.PRDCASEQTY);
                holder.tvwval = (TextView) row.findViewById(R.id.PRDVAL);
                holder.tvBatchNo = (TextView) row.findViewById(R.id.prdbatchid);
                holder.outerQty = (TextView) row.findViewById(R.id.PRDOUTERQTY);

                row.setOnClickListener(new OnClickListener() {

                    public void onClick(View v) {
                        productName.setText(holder.productName);
                    }
                });

                // On/Off order case and pce
                if (!businessModel.configurationMasterHelper.SHOW_ORDER_CASE)
                    holder.tvcaseqty.setVisibility(View.GONE);

                if (!businessModel.configurationMasterHelper.SHOW_ORDER_PCS)
                    holder.tvpcsqty.setVisibility(View.GONE);

                if (!businessModel.configurationMasterHelper.SHOW_OUTER_CASE)
                    holder.outerQty.setVisibility(View.GONE);


                if (!businessModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION)
                    holder.tvBatchNo.setVisibility(View.GONE);

                if (!businessModel.configurationMasterHelper.SHOW_STK_ORD_SRP)
                    holder.tvwval.setVisibility(View.GONE);

                if (!businessModel.configurationMasterHelper.IS_SHOW_SKU_CODE)
                    holder.tvProductCode.setVisibility(View.GONE);


                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            SchemeProductBO productBO = list.get(groupPosition)
                    .getSchemeProducts().get(childPosition);

            holder.tvwpsname.setText(productBO.getProductName());

            holder.productName = productBO.getProductFullName();
            if (businessModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION) {
                if (productBO.getBatchId() != null && !productBO.getBatchId().equals("null"))
                    holder.tvBatchNo.setText(productBO.getBatchId());
            }

            if (businessModel.configurationMasterHelper.IS_SHOW_SKU_CODE) {
                String prodCode = getResources().getString(R.string.prod_code)
                        + ": " + productBO.getProductCode() + " ";
                holder.tvProductCode.setText(prodCode);
            }

            if (productBO.getUomDescription().equals("CASE")) {
                holder.tvcaseqty.setText(productBO.getQuantitySelected() + "");
                holder.tvpcsqty.setText(0 + "");
            } else if (productBO.getUomDescription().equals("OUTER")) {
                holder.outerQty.setText(productBO.getQuantitySelected() + "");
                holder.tvpcsqty.setText(0 + "");
                holder.tvcaseqty.setText(0 + "");
            } else {
                holder.tvpcsqty.setText(productBO.getQuantitySelected() + "");
                holder.tvcaseqty.setText(0 + "");
                holder.outerQty.setText(0 + "");
            }

            holder.tvwval.setText("0");
            return row;
        }

        @Override
        public int getChildrenCount(int groupPosition) {


            if (list.get(groupPosition).getSchemeProducts() != null) {
                return list.get(groupPosition)
                        .getSchemeProducts().size();

            }

            return 0;
        }

        @Override
        public Object getGroup(int groupPosition) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int getGroupCount() {
            if (list == null)
                return 0;

            return list.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            final ViewHolder holder;

            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(R.layout.row_orderdetail_report,
                        parent, false);
                holder = new ViewHolder();
                holder.tvwpsname = (TextView) row.findViewById(R.id.PRDNAME1);
                holder.tvwpsname.setMaxLines(businessModel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);
                holder.tvProductCode = (TextView) row.findViewById(R.id.product_code);
                holder.tvBatchNo = (TextView) row.findViewById(R.id.prdbatchid);
                holder.tvpcsqty = (TextView) row.findViewById(R.id.PRDQTY);
                holder.tvcaseqty = (TextView) row.findViewById(R.id.PRDCASEQTY);
                holder.tvwval = (TextView) row.findViewById(R.id.PRDVAL);
                holder.outerQty = (TextView) row.findViewById(R.id.PRDOUTERQTY);
                holder.tvWeight = (TextView) row.findViewById(R.id.prdweight);
                row.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        //productName.setText(holder.productName);
                    }
                });

                // On/Off order case and pce
                if (!businessModel.configurationMasterHelper.SHOW_ORDER_CASE)
                    holder.tvcaseqty.setVisibility(View.GONE);

                if (!businessModel.configurationMasterHelper.SHOW_ORDER_PCS)
                    holder.tvpcsqty.setVisibility(View.GONE);

                if (!businessModel.configurationMasterHelper.SHOW_OUTER_CASE)
                    holder.outerQty.setVisibility(View.GONE);

                if (!businessModel.configurationMasterHelper.SHOW_ORDER_WEIGHT)
                    holder.tvWeight.setVisibility(View.INVISIBLE);

                if (!businessModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION)
                    holder.tvBatchNo.setVisibility(View.INVISIBLE);

                if (!businessModel.configurationMasterHelper.SHOW_STK_ORD_SRP)
                    holder.tvwval.setVisibility(View.GONE);

                if (!businessModel.configurationMasterHelper.IS_SHOW_SKU_CODE)
                    holder.tvProductCode.setVisibility(View.GONE);

                row.setOnClickListener(new OnClickListener() {

                    public void onClick(View v) {
                        productName.setText(holder.productBO.getProductName());
                    }
                });

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.productBO = list.get(groupPosition);
            if (businessModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION) {
                if (holder.productBO.getBatchNo() != null && !holder.productBO.getBatchNo().equals("null"))
                    holder.tvBatchNo.setText("12345" + holder.productBO.getBatchNo() + " , ");
            }
            holder.tvBatchNo.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.tvwpsname.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.tvWeight.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.tvProductCode.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.tvwpsname.setText(holder.productBO.getProductShortName());

            if (businessModel.configurationMasterHelper.IS_SHOW_SKU_CODE) {
                String prodCode = getResources().getString(R.string.prod_code)
                        + ": " + holder.productBO.getProductCode() + " ";
                holder.tvProductCode.setText(prodCode);
            }

            holder.productName = holder.productBO.getProductName();
            holder.tvpcsqty.setText(holder.productBO.getPQty() + "");
            holder.tvcaseqty.setText(holder.productBO.getCQty() + "");
            holder.outerQty.setText(holder.productBO.getOuterOrderedCaseQty() + "");
            int totalQty = holder.productBO.getTotalQty();
            holder.tvWeight.setText(" WGT : " + Utils.formatAsTwoDecimal((double) totalQty * holder.productBO.getWeight()) + "");
            /**
             * This line wise total may be wrong is amount discount appied via
             * scheme
             **/
            holder.tvwval.setText(businessModel.formatValue(holder.productBO
                    .getTot()) + "");

            return row;
        }

        @Override
        public boolean hasStableIds() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            // TODO Auto-generated method stub
            return false;
        }

    }

    class ViewHolder {
        private OrderReportBO productBO;
        String ref;// product id
        String productName;
        TextView tvwpsname, tvProductCode;
        TextView tvBatchNo;
        TextView tvwval, tvpcsqty, tvcaseqty, outerQty;
        TextView tvWeight;
    }

    public void onClick(View comp) {
        Button btn = (Button) comp;
        if (btn == back) {
            super.onDestroy();
            finish();
        }

    }

    public void onBackPressed() {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_settings).setVisible(false);

        if (businessModel.configurationMasterHelper.SHOW_PRINT_ORDER)
            menu.findItem(R.id.menu_print).setVisible(true);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackButtonClick();
                break;
            case R.id.menu_print:
                preparePrintData();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void preparePrintData() {

        businessModel.invoiceNumber = orderID;

        Intent intent = new Intent();

        businessModel.mCommonPrintHelper.readBuilder(StandardListMasterConstants.PRINT_FILE_ORDER + businessModel.invoiceNumber + ".txt"
                , DataMembers.IVYDIST_PATH);
        intent.setClass(this, CommonPrintPreviewActivity.class);
        intent.putExtra("IsUpdatePrintCount", true);
        intent.putExtra("isHomeBtnEnable", true);
        intent.putExtra("isFromInvoice", false);
        intent.putExtra("IsFromReport", true);

        startActivity(intent);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
    }

    private void onBackButtonClick() {
        finish();
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
    }
}