package com.ivy.cpg.view.reports.soho;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
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

public class SalesReturnValidationReportSOHO extends IvyBaseActivityNoActionBar {

    private BusinessModel bmodel;
    private ProgressDialog alertDialog;
    private ProductMasterBO productMasterBO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_return_invalidated);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        String retailerId = getIntent().getExtras().getString("RetailerId");
        String productId = getIntent().getExtras().getString("ProductId");
        String productCode = getIntent().getExtras().getString("ProductCode");


        Toolbar toolbar = findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle("Sales Report");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayUseLogoEnabled(false);
        }

        ListView mReportLv = findViewById(R.id.lvwplist);
        SalesReturnReportHelperSOHO salesReturnReportHelperSOHO=new SalesReturnReportHelperSOHO(getApplicationContext());
        ArrayList<SalesReturnReasonBO> salesReturnList = salesReturnReportHelperSOHO.getSalesReturnList(productId, retailerId);
        productMasterBO = new ProductMasterBO();
        productMasterBO.setProductID(productId);
        productMasterBO.setProductCode(productCode);
        productMasterBO.setSalesReturnReasonList(salesReturnList);
        MyAdapter adapter = new MyAdapter(salesReturnList);
        mReportLv.setAdapter(adapter);

        Button btnValidate = findViewById(R.id.btn_validate);
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
            super(SalesReturnValidationReportSOHO.this, R.layout.row_sales_return_report, items);
            this.items = items;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            SalesReturnReasonBO salesReport = items.get(position);
            View row = convertView;

            if (row == null) {
                LayoutInflater inflater = SalesReturnValidationReportSOHO.this.getLayoutInflater();
                row = inflater.inflate(R.layout.row_sales_return_report, parent, false);

                holder = new ViewHolder();
                holder.invoiceNo = row.findViewById(R.id.invNoTv);
                holder.lotNumber = row.findViewById(R.id.invLotNumberTv);
                holder.srp = row.findViewById(R.id.invSRPTv);
                holder.quantity = row.findViewById(R.id.invQuantityTv);
                holder.totalAmount = row.findViewById(R.id.outAmtTv);
                holder.returnReason = row.findViewById(R.id.reportReasonTv);
                holder.dividerLine = row.findViewById(R.id.line);

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
            alertDialog = new ProgressDialog(SalesReturnValidationReportSOHO.this);
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
                Toast.makeText(SalesReturnValidationReportSOHO.this,
                        "Invalid Sales Information. Please provide correct info",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
