package com.ivy.sd.png.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;

/**
 * Created by rajesh.k on 22-02-2016.
 */
public class DeliveryManagement extends IvyBaseActivityNoActionBar {
    private static final String TAG = "DeliveryManageMent";
    private BusinessModel bmodel;
    private ArrayList<InvoiceHeaderBO> mInvoiceList;
    private ListView mInvoiceLV;
    private InvoiceHeaderBO mSelectedInvoiceHeaderBO;
    private Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_management);
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        mInvoiceLV = (ListView) findViewById(R.id.lv_invoicelist);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        setScreenTitle("" + bmodel.mSelectedActivityName);


        updateData();
    }

    private void updateData() {

        bmodel.deliveryManagementHelper.downloadInvoiceDetails();
        mInvoiceList = bmodel.deliveryManagementHelper.getInvoiceList();

        if (mInvoiceList != null) {
            if (mInvoiceList.size() > 0) {
                if (mInvoiceList.size() == 1) {

                    Intent i = new Intent(DeliveryManagement.this, DeliveryManagementDetail.class);
                    i.putExtra("invoiceno",
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
                bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                        .now(SDUtil.TIME));

               /* BusinessModel.loadActivity(DeliveryManagement.this,
                        DataMembers.actHomeScreenTwo);*/

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
                holder.invoicenoTV = (TextView) convertView.findViewById(R.id.tv_invoice_no);
                holder.invoiceDateTV = (TextView) convertView.findViewById(R.id.tv_invoice_date);
                holder.totalLinesTV = (TextView) convertView.findViewById(R.id.tv_total_lines);
                holder.totalAmountTV = (TextView) convertView.findViewById(R.id.tv_amount);

                holder.invoicenoTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.invoiceDateTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.totalLinesTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.totalAmountTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

                ((TextView) convertView.findViewById(R.id.tv_invoice_no)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                ((TextView) convertView.findViewById(R.id.tv_amount)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                ((TextView) convertView.findViewById(R.id.tv_invoice_date)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                ((TextView) convertView.findViewById(R.id.tv_total_lines)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

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
                        mSelectedInvoiceHeaderBO = holder.invoiceHeaderBO;
                        Intent i = new Intent(DeliveryManagement.this, DeliveryManagementDetail.class);
                        i.putExtra("invoiceno",
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
            holder.invoicenoTV.setText("" + holder.invoiceHeaderBO.getInvoiceNo() + "");
            holder.invoiceDateTV.setText("" + holder.invoiceHeaderBO.getInvoiceDate() + "");
            holder.totalAmountTV.setText("" + holder.invoiceHeaderBO.getInvoiceAmount() + "");
            holder.totalLinesTV.setText("" + holder.invoiceHeaderBO.getLinesPerCall() + "");
            return convertView;
        }
    }

    class Holder {
        InvoiceHeaderBO invoiceHeaderBO;
        TextView invoicenoTV, invoiceDateTV, totalAmountTV, totalLinesTV;
    }
}
