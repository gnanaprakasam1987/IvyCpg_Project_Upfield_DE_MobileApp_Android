package com.ivy.cpg.view.delivery.invoice;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.InvoiceHeaderBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.utils.DateTimeUtils;

import java.util.ArrayList;

/**
 * Created by rajesh.k on 22-02-2016.
 */
public class DeliveryManagement extends IvyBaseActivityNoActionBar {
    private BusinessModel bmodel;
    private ArrayList<InvoiceHeaderBO> mInvoiceList;
    private ListView mInvoiceLV;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_management);
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        mInvoiceLV = findViewById(R.id.lv_invoicelist);
        Toolbar toolbar = findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            setScreenTitle(bundle.getString("screentitle"));
        }


        updateData();
    }

    private void updateData() {
        DeliveryManagementHelper deliveryManagementHelper = DeliveryManagementHelper.getInstance(this);
        deliveryManagementHelper.downloadInvoiceDetails();
        mInvoiceList = deliveryManagementHelper.getInvoiceList();

        if (mInvoiceList != null) {
            if (mInvoiceList.size() > 0) {
                if (mInvoiceList.size() == 1) {

                    Intent i = new Intent(DeliveryManagement.this, DeliveryManagementDetail.class);
                    i.putExtra("invoiceno",
                            mInvoiceList.get(0).getInvoiceRefNo());
                    i.putExtra("invoiceId",
                            mInvoiceList.get(0).getInvoiceNo());
                    i.putExtra("screentitle", getIntent().getStringExtra("screentitle"));
                    if (getIntent().getStringExtra("From") != null) {
                        i.putExtra("From", getIntent().getStringExtra("From"));
                    }
                    startActivity(i);
                    finish();
                } else {
                    mInvoiceLV.setAdapter(new MyAdapter());

                }
            } else {
                Toast.makeText(DeliveryManagement.this, getResources().getString(R.string.data_not_mapped), Toast.LENGTH_SHORT).show();
            }

        }
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_only_next, menu);
        menu.findItem(R.id.menu_next).setVisible(false);
        menu.findItem(R.id.menu_fivefilter).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i1 = item.getItemId();
        if (i1 == android.R.id.home) {
            if (getIntent().getStringExtra("From") == null) {
                bmodel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
                        .now(DateTimeUtils.TIME));

                Intent myIntent = new Intent(this, HomeScreenTwo.class);
                startActivityForResult(myIntent, 0);
            }
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            finish();
        }
        return false;
    }

    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mInvoiceList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Holder holder;
            if (convertView == null) {
                holder = new Holder();
                LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(
                        R.layout.list_delivery_management, parent,
                        false);
                holder.invoicenoTV = convertView.findViewById(R.id.tv_invoice_no);
                holder.invoiceDateTV = convertView.findViewById(R.id.tv_invoice_date);
                holder.totalLinesTV = convertView.findViewById(R.id.tv_total_lines);
                holder.totalAmountTV = convertView.findViewById(R.id.tv_amount);

                try {
                    if (bmodel.labelsMasterHelper.applyLabels(convertView.findViewById(
                            R.id.tv_date_title).getTag()) != null)
                        ((TextView) convertView.findViewById(R.id.tv_date_title))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(convertView.findViewById(
                                                R.id.tv_date_title)
                                                .getTag()));

                    if (bmodel.labelsMasterHelper.applyLabels(convertView.findViewById(
                            R.id.tv_invoiceno_title).getTag()) != null)
                        ((TextView) convertView.findViewById(R.id.tv_invoiceno_title))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(convertView.findViewById(
                                                R.id.tv_invoiceno_title)
                                                .getTag()));
                } catch (Exception e) {
                    Commons.printException(e + "");
                }


                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(DeliveryManagement.this, DeliveryManagementDetail.class);
                        i.putExtra("invoiceno",
                                holder.invoiceHeaderBO.getInvoiceRefNo());
                        i.putExtra("invoiceId",
                                holder.invoiceHeaderBO.getInvoiceNo());
                        i.putExtra("screentitle", getIntent().getStringExtra("screentitle"));
                        if (getIntent().getStringExtra("From") != null) {
                            i.putExtra("From", getIntent().getStringExtra("From"));
                        }
                        startActivity(i);
                        finish();
                    }
                });
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }
            holder.invoiceHeaderBO = mInvoiceList.get(position);
            holder.invoicenoTV.setText(String.valueOf(holder.invoiceHeaderBO.getInvoiceRefNo()));
            holder.invoiceDateTV.setText(String.valueOf(holder.invoiceHeaderBO.getInvoiceDate()));
            holder.totalAmountTV.setText(String.valueOf(holder.invoiceHeaderBO.getInvoiceAmount()));
            holder.totalLinesTV.setText(String.valueOf(holder.invoiceHeaderBO.getLinesPerCall()));
            return convertView;
        }
    }

    class Holder {
        InvoiceHeaderBO invoiceHeaderBO;
        TextView invoicenoTV, invoiceDateTV, totalAmountTV, totalLinesTV;
    }
}
