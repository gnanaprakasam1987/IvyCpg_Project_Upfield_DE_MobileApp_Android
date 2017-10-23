package com.ivy.sd.png.view.reports;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.ReportonorderbookingBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;

public class Orderreportdetail extends IvyBaseActivityNoActionBar implements
        OnClickListener {
    /**
     * Called when the activity is first created.
     */
    private Button back;
    private ArrayList<ReportonorderbookingBO> mylist;
    private ListView lvwplist;
    private TextView outletname, txttotal, productName, totalLines, tv_lbl_total_lines, TextView51;
    private BusinessModel bmodel;
    private ReportonorderbookingBO obj;
    private boolean isFromOrderReport;
    private double TotalValue;
    private String TotalLines;
    private Toolbar toolbar;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // requestWindowFeature(Window.FEATURE_NO_TITLE);
        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        // WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_order_report_detail);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);


        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        totalLines = (TextView) findViewById(R.id.txttotallines);
        tv_lbl_total_lines = (TextView) findViewById(R.id.lbl_total_lines);
        outletname = (TextView) findViewById(R.id.BtnBrandPrev);
        productName = (TextView) findViewById(R.id.productName);
        TextView51 = (TextView) findViewById(R.id.TextView51);
        TextView51.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        tv_lbl_total_lines.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));

        try {
            back = (Button) findViewById(R.id.btnPersBack);
            back.setOnClickListener(this);

            try {
                if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                        R.id.cqty).getTag()) != null)
                    ((TextView) findViewById(R.id.cqty))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(findViewById(R.id.cqty)
                                            .getTag()));
                if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                        R.id.outid).getTag()) != null)
                    ((TextView) findViewById(R.id.outid))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(findViewById(R.id.outid)
                                            .getTag()));
            } catch (Exception e) {
                Commons.printException(e);
            }


            Bundle extras = getIntent().getExtras();

            if (extras != null) {
                if (extras.containsKey("OBJ")) {
                    obj = (ReportonorderbookingBO) extras
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

            txttotal = (TextView) findViewById(R.id.txttotal);
            outletname.setText(getResources().getString(R.string.order_report)
                    + obj.getretailerName());

            lvwplist = (ListView) findViewById(R.id.lvwplistorddet);
            lvwplist.setCacheColorHint(0);

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

            /** on off case,piece,outer Title **/
            if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE)
                findViewById(R.id.cqty).setVisibility(View.GONE);
            if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS)
                findViewById(R.id.outid).setVisibility(View.GONE);
            if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
                findViewById(R.id.outercqty).setVisibility(View.GONE);
            if (!bmodel.configurationMasterHelper.SHOW_ORDER_WEIGHT)
                findViewById(R.id.title_weight).setVisibility(View.GONE);

            if (!bmodel.configurationMasterHelper.SHOW_TOTAL_VALUE_ORDER) {
                findViewById(R.id.TextView51).setVisibility(View.GONE);
                findViewById(R.id.txttotal).setVisibility(View.GONE);
            }
            if (!bmodel.configurationMasterHelper.SHOW_STK_ORD_SRP) {
                findViewById(R.id.lpc).setVisibility(View.GONE);
            }


            String orderID = obj.getorderID();
            if (isFromOrderReport)
                mylist = bmodel.reportHelper.downloadOrderreportdetail(orderID);

            else
                mylist = bmodel.reportHelper
                        .downloadPVSOrderreportdetail(orderID);
            updateOrderDetailsGrid();
        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    private void updateOrderDetailsGrid() {
        // double total = 0;

        // Show alert if error loading data.
        if (mylist == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.unable_to_load_data), 0);
            return;
        }
        // Show alert if no order exist.
        if (mylist.size() == 0) {
            bmodel.showAlert(
                    getResources().getString(R.string.no_orders_available), 0);
            return;
        }

        // Calculate the total order value.
        // for (ReportonorderbookingBO ret : mylist) {
        // total = total + ret.getTot();
        // }

        /**
         * set total lines value in textview
         * **/

        double avglinesorderbooking;

        if (isFromOrderReport)
            avglinesorderbooking = bmodel.reportHelper
                    .getavglinesfororderbooking("OrderHeader");
        else
            avglinesorderbooking = bmodel.reportHelper
                    .getavglinesfororderbooking("PVSOrderHeader");


        if (bmodel.configurationMasterHelper.SHOW_TOTAL_LINES) {
            if (bmodel.configurationMasterHelper.SHOW_TOTAL_QTY_IN_ORDER_REPORT) {
                totalLines.setText(bmodel.reportHelper.getTotalQtyfororder(obj.getorderID()) + "");
                tv_lbl_total_lines.setText(getResources().getString(R.string.tot_qty));
            } else {
                totalLines.setText(TotalLines + "");
                tv_lbl_total_lines.setText(getResources().getString(R.string.tot_line));
            }

        } else {
            totalLines.setVisibility(View.GONE);
            tv_lbl_total_lines.setVisibility(View.GONE);
            findViewById(R.id.view1).setVisibility(View.GONE);
        }


        // Format and set on the lable
        txttotal.setText(SDUtil.format(TotalValue,
                bmodel.configurationMasterHelper.VALUE_PRECISION_COUNT,
                0, bmodel.configurationMasterHelper.IS_DOT_FOR_GROUP));

        // Load listview.
        MyAdapter mSchedule = new MyAdapter(mylist);
        lvwplist.setAdapter(mSchedule);
    }

    public void onNothingSelected(AdapterView<?> arg0) {

    }

    int pos;

    class MyAdapter extends ArrayAdapter<ReportonorderbookingBO> {
        ArrayList<ReportonorderbookingBO> items;

        MyAdapter(ArrayList<ReportonorderbookingBO> items) {
            super(Orderreportdetail.this, R.layout.row_orderdetail_report,
                    items);
            this.items = items;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            pos = position;
            bmodel = (BusinessModel) getApplicationContext();

            bmodel.setContext(Orderreportdetail.this);
            ReportonorderbookingBO orderreport = (ReportonorderbookingBO) items
                    .get(pos);
            View row = convertView;
            bmodel = (BusinessModel) getApplicationContext();

            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(R.layout.row_orderdetail_report, parent,
                        false);
                holder = new ViewHolder();
                holder.tvwpsname = (TextView) row.findViewById(R.id.PRDNAME1);
                holder.tvwpsname.setMaxLines(bmodel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);

                holder.tvwval = (TextView) row.findViewById(R.id.PRDVAL);
                holder.tvwqty = (TextView) row.findViewById(R.id.PRDQTY);
                holder.tvcaseqty = (TextView) row.findViewById(R.id.PRDCASEQTY);
                holder.outerQty = (TextView) row
                        .findViewById(R.id.outerCaseQty);
                holder.tvbatchid = (TextView) row.findViewById(R.id.prdbatchid);
                holder.tvWeight = (TextView) row.findViewById(R.id.prdweight);

                /** hide Pcs,case,outer **/
                if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE)
                    holder.tvcaseqty.setVisibility(View.GONE);
                if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS)
                    holder.tvwqty.setVisibility(View.GONE);
                if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
                    holder.outerQty.setVisibility(View.GONE);
                if (!bmodel.configurationMasterHelper.SHOW_ORDER_WEIGHT)
                    holder.tvWeight.setVisibility(View.GONE);
                if (!bmodel.configurationMasterHelper.SHOW_STK_ORD_SRP) {
                    holder.tvwval.setVisibility(View.GONE);
                }

                row.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        //   productName.setText(holder.productName);

                    }
                });

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }


            holder.tvwpsname.setText(orderreport.getProductshortname());
            holder.tvwpsname.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
            holder.tvbatchid.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));

            holder.productName = orderreport.getProductname();
            holder.tvcaseqty.setText(orderreport.getCQty() + "");
            holder.tvwqty.setText(orderreport.getPQty() + "");
            holder.tvwval.setText(bmodel.formatValue((orderreport.getTot()))
                    + "");
            holder.outerQty.setText(orderreport.getOuterOrderedCaseQty() + "");
            if (orderreport.getBatchNo() != null && !orderreport.getBatchNo().equals("null"))
                holder.tvbatchid.setText("Batch No : " + orderreport.getBatchNo());
            else holder.tvbatchid.setText("" + " ");

            holder.tvwpsname.setTextColor(holder.outerQty.getTextColors());
            if (orderreport.getIsCrown() == 1)
                holder.tvwpsname.setTextColor(Color.BLUE);
            else if (orderreport.getIsBottleReturn() == 1)
                holder.tvwpsname.setTextColor(Color.GREEN);
            holder.tvWeight.setText("WGT :" + orderreport.getTotalQty() * orderreport.getWeight() + "");
            holder.tvWeight.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));


            return (row);
        }
    }

    class ViewHolder {
        String ref;// product id
        String productName;
        TextView tvwpsname;
        TextView tvwval, tvwqty, tvcaseqty, outerQty, tvbatchid, tvWeight;
        ProductMasterBO productBO;
    }

    public void onClick(View comp) {
        // TODO Auto-generated method stub
        Button btn = (Button) comp;
        if (btn == back) {
            super.onDestroy();
            finish();
        }

    }

    public void onBackPressed() {
        // do something on back.
        return;
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