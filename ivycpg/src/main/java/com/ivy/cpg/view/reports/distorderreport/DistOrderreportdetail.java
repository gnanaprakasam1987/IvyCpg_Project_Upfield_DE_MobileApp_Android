package com.ivy.cpg.view.reports.distorderreport;

import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class DistOrderreportdetail extends IvyBaseActivityNoActionBar implements
        OnClickListener {
    /**
     * Called when the activity is first created.
     */
    private Button back;
    private ListView lvwplist;
    private TextView outletname, txttotal, productName, totalLines;
    private BusinessModel bmodel;
    private boolean isFromOrderReport;
    private Bundle extras;
    private Toolbar toolbar;
    private CompositeDisposable compositeDisposable;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_order_report_detail);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        totalLines = (TextView) findViewById(R.id.txttotallines);

        setSupportActionBar(toolbar);
        // Set title to toolbar
        if (getSupportActionBar() != null)

            getSupportActionBar().setTitle(null);
        // Used to on / off the back arrow icon
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Used to remove the app logo actionbar icon and set title as home
        // (title support click)
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        // Used to hide the app logo icon from actionbar
        // getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        setScreenTitle(getResources().getString(R.string.order_report_details));

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
            outletname = (TextView) findViewById(R.id.BtnBrandPrev);
            productName = (TextView) findViewById(R.id.productName);

            extras = getIntent().getExtras();
            DistOrderReportBo obj = null;
            if (extras != null) {
                if (extras.containsKey("OBJ")) {
                    obj = (DistOrderReportBo) extras
                            .getParcelable("OBJ");
                }
            }

            txttotal = (TextView) findViewById(R.id.txttotal);
            outletname.setText(getResources().getString(R.string.order_report)
                    + obj.getRetailerName());
            ExpandableListView lv = (ExpandableListView) findViewById(R.id.elv);
            lv.setVisibility(View.GONE);

            lvwplist = (ListView) findViewById(R.id.lvwplistorddet);
            lvwplist.setVisibility(View.VISIBLE);
            lvwplist.setCacheColorHint(0);


            /** on off case,piece,outer Title **/
            if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE)
                findViewById(R.id.cqty).setVisibility(View.GONE);
            if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS)
                findViewById(R.id.outid).setVisibility(View.GONE);
            if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
                findViewById(R.id.outercqty).setVisibility(View.GONE);


            getDistOrdDeetails(obj.getOrderId());


        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    private void getDistOrdDeetails(String orderID) {
        compositeDisposable = new CompositeDisposable();
        compositeDisposable.add((Disposable) DistOrderReportHelper.getInstance().distOrderReportDetail(this, orderID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<ArrayList<DistOrderReportBo>>() {
                    @Override
                    public void onNext(ArrayList<DistOrderReportBo> distOrderReportList) {
                        updateOrderDetailsGrid(distOrderReportList);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                }));
    }

    private void updateOrderDetailsGrid(ArrayList<DistOrderReportBo> mylist) {
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


        /**
         * set total lines value in textview
         * **/

        // not used any where in this class
      /*  double avglinesorderbooking;

        if (isFromOrderReport)
            avglinesorderbooking = bmodel.reportHelper
                    .getavglinesfororderbooking("OrderHeader");*/


        String totLines = "";
        double totalValue = 0;
        if (extras != null) {

            if (extras.containsKey("TotalValue")) {
                totalValue = extras.getDouble("TotalValue");
            }
            if (extras.containsKey("TotalLines")) {
                totLines = extras.getString("TotalLines");
            }
        }


        totalLines.setText(totLines + "");
        // Format and set on the lable
        txttotal.setText(bmodel.formatValue(totalValue) + "");

        // Load listview.
        MyAdapter mSchedule = new MyAdapter(mylist);
        lvwplist.setAdapter(mSchedule);
    }

    public void onNothingSelected(AdapterView<?> arg0) {

    }

    int pos;

    class MyAdapter extends ArrayAdapter<DistOrderReportBo> {
        ArrayList<DistOrderReportBo> items;

        MyAdapter(ArrayList<DistOrderReportBo> items) {
            super(DistOrderreportdetail.this, R.layout.row_orderdetail_report,
                    items);
            this.items = items;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            pos = position;
            bmodel = (BusinessModel) getApplicationContext();

            bmodel.setContext(DistOrderreportdetail.this);
            DistOrderReportBo orderreport = (DistOrderReportBo) items
                    .get(pos);
            View row = convertView;
            bmodel = (BusinessModel) getApplicationContext();

            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(R.layout.row_orderdetail_report, parent,
                        false);
                holder = new ViewHolder();
                holder.tvwpsname = (TextView) row.findViewById(R.id.PRDNAME1);

                holder.tvwval = (TextView) row.findViewById(R.id.PRDVAL);
                holder.tvwqty = (TextView) row.findViewById(R.id.PRDQTY);
                holder.tvcaseqty = (TextView) row.findViewById(R.id.PRDCASEQTY);
                holder.outerQty = (TextView) row
                        .findViewById(R.id.PRDOUTERQTY);
                holder.tvbatchid = (TextView) row.findViewById(R.id.prdbatchid);

                /** hide Pcs,case,outer **/
                if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE)
                    holder.tvcaseqty.setVisibility(View.GONE);
                if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS)
                    holder.tvwqty.setVisibility(View.GONE);
                if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
                    holder.outerQty.setVisibility(View.GONE);

                row.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        productName.setText(holder.productName);

                    }
                });

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }


            holder.tvwpsname.setText(orderreport.getProductShortName());
            holder.productName = orderreport.getProductName();
            holder.tvcaseqty.setText(orderreport.getCQty() + "");
            holder.tvwqty.setText(orderreport.getPQty() + "");
            holder.tvwval.setText(bmodel.formatValue((orderreport.getTot()))
                    + "");
            holder.outerQty.setText(orderreport.getOuterOrderedCaseQty() + "");
            if (orderreport.getBatchNo() != null) {
                holder.tvbatchid.setText(orderreport.getBatchNo() + "");
            } else {
                holder.tvbatchid.setText("");
            }


            holder.tvwpsname.setTextColor(holder.outerQty.getTextColors());
            if (orderreport.getIsCrown() == 1)
                holder.tvwpsname.setTextColor(Color.BLUE);
            else if (orderreport.getIsBottleReturn() == 1)
                holder.tvwpsname.setTextColor(Color.GREEN);


            return (row);
        }
    }

    class ViewHolder {
        String ref;// product id
        String productName;
        TextView tvwpsname;
        TextView tvwval, tvwqty, tvcaseqty, outerQty, tvbatchid;
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