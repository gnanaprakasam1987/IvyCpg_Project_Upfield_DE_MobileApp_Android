package com.ivy.sd.png.view.reports;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.salesreturn.SalesReturnReasonBO;
import com.ivy.sd.png.asean.view.R;
//import com.ivy.sd.png.bo.SalesReturnReasonBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

import java.util.ArrayList;

public class SalesReturnValidationReport extends IvyBaseActivityNoActionBar {
    private static final String TAG = "SalesReturnValidationReport";
    private static BusinessModel bmodel;
    private Toolbar toolbar;
    private ListView mReportLv;
    private Button btnValidate;
    private String retailerId;
    private String productId;
    private ProgressDialog alertDialog;
    private String ProductCode;
    private ProductMasterBO productMasterBO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_return_invalidated);
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        retailerId = getIntent().getExtras().getString("RetailerId");
        productId = getIntent().getExtras().getString("ProductId");
        ProductCode = getIntent().getExtras().getString("ProductCode");

        initializeItems();
    }

    private void initializeItems() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle("Sales Report");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayUseLogoEnabled(false);
        }

        mReportLv = (ListView) findViewById(R.id.lvwplist);
        ArrayList<SalesReturnReasonBO> salesReturnList = bmodel.reportHelper.getSalesReturnList(productId, retailerId);
        productMasterBO = new ProductMasterBO();
        productMasterBO.setProductID(productId);
        productMasterBO.setProductCode(ProductCode);
        productMasterBO.setSalesReturnReasonList(salesReturnList);
        MyAdapter adapter = new MyAdapter(salesReturnList);
        mReportLv.setAdapter(adapter);

        btnValidate = (Button) findViewById(R.id.btn_validate);
        btnValidate.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        btnValidate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new validateSalesReturn().execute();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            onBackClicked();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onBackClicked() {
        finish();
    }

    class MyAdapter extends ArrayAdapter<SalesReturnReasonBO> {

        private final ArrayList<SalesReturnReasonBO> items;

        private MyAdapter(ArrayList<SalesReturnReasonBO> items) {
            super(SalesReturnValidationReport.this, R.layout.row_sales_return_report, items);
            this.items = items;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            SalesReturnReasonBO salesReport = items.get(position);
            View row = convertView;

            if (row == null) {
                LayoutInflater inflater = SalesReturnValidationReport.this.getLayoutInflater();
                row = inflater.inflate(R.layout.row_sales_return_report, parent, false);

                holder = new ViewHolder();
                holder.invoiceNo = (TextView) row.findViewById(R.id.invNoTv);
                holder.lotNumber = (TextView) row.findViewById(R.id.invLotNumberTv);
                holder.srp = (TextView) row.findViewById(R.id.invSRPTv);
                holder.quantity = (TextView) row.findViewById(R.id.invQuantityTv);
                holder.totalAmount = (TextView) row.findViewById(R.id.outAmtTv);
                holder.returnReason = (TextView) row.findViewById(R.id.reportReasonTv);
                holder.dividerLine = (LinearLayout) row.findViewById(R.id.line);

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.invoiceNo.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.lotNumber.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.srp.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.quantity.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.totalAmount.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.returnReason.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

            holder.invoiceNo.setText(salesReport.getInvoiceno());
            holder.lotNumber.setText(salesReport.getLotNumber());
            holder.srp.setText(salesReport.getSrpedit() + "");
            holder.quantity.setText(salesReport.getPieceQty() + "");
            holder.totalAmount.setText(salesReport.getOldMrp() + "");
            holder.returnReason.setText(salesReport.getReasonDesc());

            return (row);
        }
    }

    class ViewHolder {
        TextView invoiceNo, lotNumber, srp, quantity, totalAmount, returnReason;
        LinearLayout dividerLine;
    }

    class validateSalesReturn extends AsyncTask<Integer, Integer, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            alertDialog = new ProgressDialog(SalesReturnValidationReport.this);
            alertDialog.setMessage(getResources().getString(R.string.validating_sales));
            alertDialog.setCancelable(false);
            alertDialog.show();
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            return bmodel.synchronizationHelper.validateSalesReturn(productMasterBO);
        }

        @Override
        protected void onPostExecute(Integer s) {
            super.onPostExecute(s);
            alertDialog.dismiss();
            if (s == 1) {
                finish();
            } else {
                Toast.makeText(SalesReturnValidationReport.this,
                        "Invalid Sales Information. Please provide correct info",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
