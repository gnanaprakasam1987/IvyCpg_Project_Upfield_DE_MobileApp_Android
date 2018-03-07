package com.ivy.cpg.view.reports;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;

/**
 * Order Report Detail Screen
 */
public class OrderReportDetail extends IvyBaseActivityNoActionBar implements
        OnClickListener {

    private Button back;
    private ListView listView;
    private TextView text_total, totalLines, tv_lbl_total_lines;

    private BusinessModel businessModel;
    private OrderReportBO obj;

    private boolean isFromOrderReport;
    private double TotalValue;
    private String TotalLines;

    private ArrayList<OrderReportBO> list;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_order_report_detail);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

        businessModel = (BusinessModel) getApplicationContext();
        businessModel.setContext(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        totalLines = (TextView) findViewById(R.id.txttotallines);
        tv_lbl_total_lines = (TextView) findViewById(R.id.lbl_total_lines);
        TextView outletName = (TextView) findViewById(R.id.BtnBrandPrev);
        TextView label_total = (TextView) findViewById(R.id.label_totalValue);
        label_total.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        tv_lbl_total_lines.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

        try {
            back = (Button) findViewById(R.id.btnPersBack);
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
            }

            text_total = (TextView) findViewById(R.id.txttotal);
            String value = getResources().getString(R.string.order_report)
                    + obj.getRetailerName();
            outletName.setText(value);

            listView = (ListView) findViewById(R.id.lvwplistorddet);
            listView.setCacheColorHint(0);

            setSupportActionBar(toolbar);
            // Set title to toolbar
            if (getSupportActionBar() != null)
                getSupportActionBar().setTitle(
                        getResources().getString(R.string.order_report_details));
            //     getSupportActionBar().setIcon(R.drawable.icon_order);
            // Used to on / off the back arrow icon
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Used to remove the app logo actionbar icon and set title as home
            // (title support click)
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            // Used to hide the app logo icon from actionbar
            // getSupportActionBar().setDisplayUseLogoEnabled(false);

            if (!businessModel.configurationMasterHelper.SHOW_ORDER_CASE)
                findViewById(R.id.cqty).setVisibility(View.GONE);
            if (!businessModel.configurationMasterHelper.SHOW_ORDER_PCS)
                findViewById(R.id.outid).setVisibility(View.GONE);
            if (!businessModel.configurationMasterHelper.SHOW_OUTER_CASE)
                findViewById(R.id.outercqty).setVisibility(View.GONE);
            if (!businessModel.configurationMasterHelper.SHOW_ORDER_WEIGHT)
                findViewById(R.id.title_weight).setVisibility(View.GONE);

            if (!businessModel.configurationMasterHelper.SHOW_TOTAL_VALUE_ORDER) {
                findViewById(R.id.label_totalValue).setVisibility(View.GONE);
                findViewById(R.id.txttotal).setVisibility(View.GONE);
            }
            if (!businessModel.configurationMasterHelper.SHOW_STK_ORD_SRP) {
                findViewById(R.id.lpc).setVisibility(View.GONE);
            }


            String orderID = obj.getOrderID();
            if (isFromOrderReport)
                list = businessModel.reportHelper.downloadOrderreportdetail(orderID);

            else
                list = businessModel.reportHelper
                        .downloadPVSOrderreportdetail(orderID);
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
                totalLines.setText(String.valueOf(businessModel.reportHelper.getTotalQtyfororder(obj.getOrderID())));
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


        text_total.setText(SDUtil.format(TotalValue,
                businessModel.configurationMasterHelper.VALUE_PRECISION_COUNT,
                0, businessModel.configurationMasterHelper.IS_DOT_FOR_GROUP));

        MyAdapter mSchedule = new MyAdapter(list);
        listView.setAdapter(mSchedule);
    }


    int pos;

    class MyAdapter extends ArrayAdapter<OrderReportBO> {
        ArrayList<OrderReportBO> items;

        MyAdapter(ArrayList<OrderReportBO> items) {
            super(OrderReportDetail.this, R.layout.row_orderdetail_report,
                    items);
            this.items = items;
        }

        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            final ViewHolder holder;
            pos = position;
            businessModel = (BusinessModel) getApplicationContext();

            businessModel.setContext(OrderReportDetail.this);
            OrderReportBO orderReportBO = items
                    .get(pos);
            View row = convertView;
            businessModel = (BusinessModel) getApplicationContext();

            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(R.layout.row_orderdetail_report, parent,
                        false);
                holder = new ViewHolder();
                holder.productShortName = (TextView) row.findViewById(R.id.PRDNAME1);
                holder.productShortName.setMaxLines(businessModel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);

                holder.text_value = (TextView) row.findViewById(R.id.PRDVAL);
                holder.text_quantity = (TextView) row.findViewById(R.id.PRDQTY);
                holder.text_caseQuantity = (TextView) row.findViewById(R.id.PRDCASEQTY);
                holder.outerQty = (TextView) row
                        .findViewById(R.id.outerCaseQty);
                holder.text_batchId = (TextView) row.findViewById(R.id.prdbatchid);
                holder.tvWeight = (TextView) row.findViewById(R.id.prdweight);

                if (!businessModel.configurationMasterHelper.SHOW_ORDER_CASE)
                    holder.text_caseQuantity.setVisibility(View.GONE);
                if (!businessModel.configurationMasterHelper.SHOW_ORDER_PCS)
                    holder.text_quantity.setVisibility(View.GONE);
                if (!businessModel.configurationMasterHelper.SHOW_OUTER_CASE)
                    holder.outerQty.setVisibility(View.GONE);
                if (!businessModel.configurationMasterHelper.SHOW_ORDER_WEIGHT)
                    holder.tvWeight.setVisibility(View.GONE);
                if (!businessModel.configurationMasterHelper.SHOW_STK_ORD_SRP) {
                    holder.text_value.setVisibility(View.GONE);
                }
                if (!businessModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION)
                    holder.text_batchId.setVisibility(View.GONE);

                row.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        //   productName.setText(holder.productName);

                    }
                });

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }


            holder.productShortName.setText(orderReportBO.getProductShortName());
            holder.productShortName.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.text_batchId.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

            holder.productName = orderReportBO.getProductName();
            holder.text_caseQuantity.setText(String.valueOf(orderReportBO.getCQty()));
            holder.text_quantity.setText(String.valueOf(orderReportBO.getPQty()));
            holder.text_value.setText(businessModel.formatValue((orderReportBO.getTot())));
            holder.outerQty.setText(String.valueOf(orderReportBO.getOuterOrderedCaseQty()));
            if (businessModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION) {
                if (orderReportBO.getBatchNo() != null && !orderReportBO.getBatchNo().equals("null")) {
                    String value = "Batch No : " + orderReportBO.getBatchNo();
                    holder.text_batchId.setText(value);
                } else holder.text_batchId.setText("");
            }

            holder.productShortName.setTextColor(holder.outerQty.getTextColors());
            if (orderReportBO.getIsCrown() == 1)
                holder.productShortName.setTextColor(Color.BLUE);
            else if (orderReportBO.getIsBottleReturn() == 1)
                holder.productShortName.setTextColor(Color.GREEN);
            String weight = "WGT :" + orderReportBO.getTotalQty() * orderReportBO.getWeight();
            holder.tvWeight.setText(weight);
            holder.tvWeight.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));


            return (row);
        }
    }

    class ViewHolder {
        String productName;
        TextView productShortName;
        TextView text_value, text_quantity, text_caseQuantity, outerQty, text_batchId, tvWeight;
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
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackButtonClick();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onBackButtonClick() {
        finish();
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
    }
}