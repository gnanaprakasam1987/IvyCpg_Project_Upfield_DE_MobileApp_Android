package com.ivy.cpg.view.salesreturn;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.order.OrderHelper;
import com.ivy.cpg.view.order.discount.DiscountHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.CaptureSignatureActivity;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.sd.png.view.RemarksDialog;
import com.ivy.sd.print.CommonPrintPreviewActivity;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.view.OnSingleClickListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class SalesReturnSummery extends IvyBaseActivityNoActionBar {

    private String outPutDateFormat;
    private AlertDialog alertDialog;
    private Vector<ProductMasterBO> mPrintList;
    private BusinessModel bmodel;
    private ListView lvwplist;
    private SalesReturnHelper salesReturnHelper;
    private String PHOTO_PATH = "";
    private ArrayAdapter<String> mInvoiceListAdapter;
    private int mSelectedIndex;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_salesreturn_summary);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //Initilize the ActionBar and set title to it
        if (getSupportActionBar() != null)
            setScreenTitle("Sales Return Summary");
        // Used to on / off the back arrow icon
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        salesReturnHelper = SalesReturnHelper.getInstance(this);

        PHOTO_PATH = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + DataMembers.photoFolderName;
        outPutDateFormat = ConfigurationMasterHelper.outDateFormat;
        lvwplist = findViewById(R.id.list);
        lvwplist.setCacheColorHint(0);


        TextView totalValue = findViewById(R.id.totalValue);
        totalValue.setText(bmodel.formatValue(salesReturnHelper.getReturnValue()));
        TextView lineValue = findViewById(R.id.lcpValue);
        String strLpcValue = salesReturnHelper.getLpcValue() + "";
        lineValue.setText(strLpcValue);

        Button mBtnSave = findViewById(R.id.btn_save);

        mBtnSave.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                onNextButtonClick();
            }
        });

        mInvoiceListAdapter = new ArrayAdapter<>(this,
                android.R.layout.select_dialog_singlechoice);

        for (String temp : salesReturnHelper.getInvoiceNo(this))
            mInvoiceListAdapter.add(temp);
    }

    @Override
    protected void onStart() {
        super.onStart();
        refreshList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        supportInvalidateOptionsMenu();
    }

    private void refreshList() {
        ArrayList<SalesReturnReasonBO> list = new ArrayList<>();

        for (ProductMasterBO product : salesReturnHelper.getSalesReturnProducts()) {
            for (SalesReturnReasonBO bo : product.getSalesReturnReasonList()) {
                if ((bo.getPieceQty() + bo.getCaseQty() + bo
                        .getOuterQty()) > 0) {
                    bo.setSrPieceQty(product.getRepPieceQty());
                    bo.setSrCaseQty(product.getRepCaseQty());
                    bo.setSrOuterQty(product.getRepOuterQty());
                    list.add(bo);

                }
            }
        }
        MyAdapter mSchedule = new MyAdapter(list);
        lvwplist.setAdapter(mSchedule);
    }

    class MyAdapter extends ArrayAdapter<SalesReturnReasonBO> {
        final ArrayList<SalesReturnReasonBO> items;

        MyAdapter(ArrayList<SalesReturnReasonBO> items) {
            super(SalesReturnSummery.this, R.layout.row_salesreturn_summery,
                    items);
            this.items = items;
        }

        public SalesReturnReasonBO getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            final ViewHolder holder;
            SalesReturnReasonBO product1 = items.get(position);
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(R.layout.row_salesreturn_summery,
                        parent, false);
                holder = new ViewHolder();

                holder.caseQty = row
                        .findViewById(R.id.productqtyCases);
                holder.pieceQty = row
                        .findViewById(R.id.productqtyPieces);
                holder.outerQty = row
                        .findViewById(R.id.outerproductqtyCases);
                holder.srPieceQty = row.findViewById(R.id.srQtyPcs);
                holder.srCaseQty = row.findViewById(R.id.srQtyCases);
                holder.srOuterQty = row.findViewById(R.id.srQtyouter);

                holder.psname = row.findViewById(R.id.productName);
                holder.tvReason = row.findViewById(R.id.tv_reason);

                holder.mfgDate = row.findViewById(R.id.mfgDate);
                holder.expDate = row.findViewById(R.id.expDate);
                holder.oldMrp = row.findViewById(R.id.oldMrp);
                holder.invoiceno = row.findViewById(R.id.invoiceno);
                holder.srpedit = row.findViewById(R.id.srpedit);
                holder.lotnumber = row.findViewById(R.id.lotnumber);

                if (!salesReturnHelper.SHOW_SALES_RET_CASE) {
                    (row.findViewById(R.id.ll_case)).setVisibility(View.GONE);
                } else {
                    try {
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.caseTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.caseTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(R.id.caseTitle)
                                                    .getTag()));
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                }
                if (!salesReturnHelper.SHOW_SALES_RET_PCS) {
                    (row.findViewById(R.id.ll_pc)).setVisibility(View.GONE);
                } else {
                    try {
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.pcsTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.pcsTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(R.id.pcsTitle)
                                                    .getTag()));
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                }
                if (!salesReturnHelper.SHOW_SALES_RET_OUTER_CASE)
                    (row.findViewById(R.id.ll_outer)).setVisibility(View.GONE);
                if (!salesReturnHelper.SHOW_STOCK_REPLACE_PCS)
                    (row.findViewById(R.id.ll_srpc)).setVisibility(View.GONE);
                else {
                    try {
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.srPcsTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.srPcsTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(R.id.srPcsTitle)
                                                    .getTag()));
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                }
                if (!salesReturnHelper.SHOW_STOCK_REPLACE_CASE)
                    (row.findViewById(R.id.ll_srcase)).setVisibility(View.GONE);
                else {
                    try {
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.srCaseTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.srCaseTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(R.id.srCaseTitle)
                                                    .getTag()));
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                }
                if (!salesReturnHelper.SHOW_STOCK_REPLACE_OUTER)
                    (row.findViewById(R.id.ll_sroo)).setVisibility(View.GONE);
                else {
                    try {
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.srOutercaseTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.srOutercaseTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(R.id.srOutercaseTitle)
                                                    .getTag()));
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                }
                if (!salesReturnHelper.SHOW_SAL_RET_OLD_MRP) {
                    (row.findViewById(R.id.ll_oldmrp)).setVisibility(View.GONE);
                } else {
                    try {
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.oldMrpTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.oldMrpTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(R.id.oldMrpTitle)
                                                    .getTag()));
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                }
                if (!salesReturnHelper.SHOW_SAL_RET_MFG_DATE) {
                    (row.findViewById(R.id.ll_mfd)).setVisibility(View.GONE);
                } else {
                    try {
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.mfgDateTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.mfgDateTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(R.id.mfgDateTitle)
                                                    .getTag()));
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                }
                if (!salesReturnHelper.SHOW_SAL_RET_EXP_DATE) {
                    (row.findViewById(R.id.ll_expd)).setVisibility(View.GONE);
                } else {
                    try {
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.expDateTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.expDateTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(R.id.expDateTitle)
                                                    .getTag()));
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                }
                if (!salesReturnHelper.SHOW_SRP_EDIT && !salesReturnHelper.SHOW_SAL_RET_SRP) {
                    (row.findViewById(R.id.ll_srpEdit)).setVisibility(View.GONE);
                } else {
                    try {
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.srpeditTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.srpeditTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(R.id.srpeditTitle)
                                                    .getTag()));
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                }
                if (!salesReturnHelper.SHOW_LOTNUMBER) {
                    (row.findViewById(R.id.ll_lotno)).setVisibility(View.GONE);
                } else {
                    try {
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.lotnumberTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.lotnumberTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(R.id.lotnumberTitle)
                                                    .getTag()));
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                }
                if (!salesReturnHelper.SHOW_SR_INVOICE_NUMBER) {
                    (row.findViewById(R.id.ll_invoiceno)).setVisibility(View.GONE);
                } else {
                    try {
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.invoicenoTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.invoicenoTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(R.id.invoicenoTitle)
                                                    .getTag()));
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                }
                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }


            holder.productBO = product1;
            holder.psname.setText(holder.productBO.getProductShortName());
            holder.psname.setMaxLines(bmodel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);

            holder.lotnumber.setText(holder.productBO.getLotNumber());

            String strCaseQty = holder.productBO.getCaseQty() + "";
            holder.caseQty.setText(strCaseQty);
            String strPieceQty = holder.productBO.getPieceQty() + "";
            holder.pieceQty.setText(strPieceQty);
            String strOuterQty = holder.productBO.getOuterQty() + "";
            holder.outerQty.setText(strOuterQty);
            String strSrPieceQty = holder.productBO.getSrPieceQty() + "";
            holder.srPieceQty.setText(strSrPieceQty);
            String strSrCaseQty = holder.productBO.getSrCaseQty() + "";
            holder.srCaseQty.setText(strSrCaseQty);
            String strSrOuterQty = holder.productBO.getSrOuterQty() + "";
            holder.srOuterQty.setText(strSrOuterQty);
            holder.tvReason.setText(holder.productBO.getReasonDesc());

            if (holder.productBO.getInvoiceno() != null) {
                String strInvoiceno = holder.productBO.getInvoiceno() + "";
                holder.invoiceno.setText(strInvoiceno);
            } else
                holder.invoiceno.setText("0");
            if (holder.productBO.getSrpedit() > 0
                    && holder.productBO.getSrpedit() != holder.productBO.getSrp()) {
                String strSrpEdit = holder.productBO.getSrpedit() + "";
                holder.srpedit.setText(strSrpEdit);
            } else {
                holder.srpedit.setText("0");
            }

            holder.mfgDate
                    .setText((holder.productBO.getMfgDate() == null) ? DateTimeUtils
                            .convertFromServerDateToRequestedFormat(
                                    DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                                    outPutDateFormat) : holder.productBO
                            .getMfgDate());
            holder.expDate
                    .setText((holder.productBO.getExpDate() == null) ? DateTimeUtils
                            .convertFromServerDateToRequestedFormat(
                                    DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                                    outPutDateFormat) : holder.productBO.getExpDate());
            String strOldMrp = bmodel.formatValue(holder.productBO
                    .getOldMrp()) + "";
            holder.oldMrp.setText(strOldMrp);
            if (position % 2 == 0) {
                row.setBackgroundColor(ContextCompat.getColor(SalesReturnSummery.this, R.color.list_even_item_bg));
            } else {
                row.setBackgroundColor(ContextCompat.getColor(SalesReturnSummery.this, R.color.list_odd_item_bg));
            }
            return row;
        }
    }

    class ViewHolder {
        private SalesReturnReasonBO productBO;
        private TextView psname;
        private TextView pieceQty;
        private TextView caseQty;
        private TextView oldMrp;
        private TextView outerQty;
        private TextView invoiceno;
        private TextView srpedit;
        private TextView lotnumber;
        private TextView srPieceQty;
        private TextView srCaseQty;
        private TextView srOuterQty;
        private TextView tvReason;
        private TextView mfgDate;
        private TextView expDate;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (!salesReturnHelper.SHOW_REMARKS_SAL_RET) {
            menu.findItem(R.id.menu_reviews).setVisible(false);
        } else
            menu.findItem(R.id.menu_reviews).setVisible(true);

        if (!bmodel.configurationMasterHelper.IS_SIH_VALIDATION)
            menu.findItem(R.id.menu_clear_tran).setVisible(true);

        menu.findItem(R.id.menu_next).setVisible(false);

        if (bmodel.configurationMasterHelper.IS_SALES_RETURN_SIGN)
            menu.findItem(R.id.menu_signature).setVisible(true);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sales_return_summary, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            onBackButtonClick();
            return true;
        } else if (i == R.id.menu_reviews) {
            onNoteButtonClick();
            return true;
        } else if (i == R.id.menu_next) {
            onNextButtonClick();
            return true;
        } else if (i == R.id.menu_clear_tran) {
            showCustomDialog();
            return true;
        } else if (i == R.id.menu_signature) {
            if (salesReturnHelper.isSignCaptured()) {
                showDialog(8);
                return true;
            }
            Intent intent = new Intent(SalesReturnSummery.this,
                    CaptureSignatureActivity.class);
            intent.putExtra("fromModule", "SALES_RETURN");
            startActivity(intent);
            bmodel.configurationMasterHelper.setSignatureTitle("Signature");
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
//            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 8:
                AlertDialog.Builder builder7 = new AlertDialog.Builder(SalesReturnSummery.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(
                                "Signature Already taken.Do you want to delete and retake?")
                        .setPositiveButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        salesReturnHelper.setIsSignCaptured(false);
                                        if (salesReturnHelper.getSignatureName() != null)
                                            bmodel.synchronizationHelper.deleteFiles(
                                                    PHOTO_PATH, salesReturnHelper.getSignatureName());
                                        Intent i = new Intent(SalesReturnSummery.this,
                                                CaptureSignatureActivity.class);
                                        i.putExtra("fromModule", "SALES_RETURN");
                                        startActivity(i);
                                        bmodel.configurationMasterHelper.setSignatureTitle("Signature");
                                        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
//                                        finish();
                                    }
                                })
                        .setNegativeButton(
                                getResources().getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                    }
                                });
                bmodel.applyAlertDialogTheme(builder7);
                break;


            default:
                break;
        }
        return null;
    }

    private void showCustomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SalesReturnSummery.this)
                .setIcon(null)
                .setCancelable(false)
                .setTitle(
                        getResources().getString(
                                R.string.do_u_want_to_delete_tran))
                .setPositiveButton(getResources().getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                salesReturnHelper.setIsSignCaptured(false);
                                if (salesReturnHelper.getSignatureName() != null)
                                    bmodel.synchronizationHelper.deleteFiles(
                                            PHOTO_PATH, salesReturnHelper.getSignatureName());
                                salesReturnHelper.clearTransaction(getApplicationContext());
                                Toast.makeText(SalesReturnSummery.this, getResources().getString(R.string.tran_deleted_successfully), Toast.LENGTH_LONG).show();
                               /* BusinessModel.loadActivity(SalesReturnSummery.this,
                                        DataMembers.actHomeScreenTwo);*/

                                //  DataMembers.actHomeScreenTwo);
                                setResult(RESULT_OK);
                                finish();
                                Intent myIntent = new Intent(SalesReturnSummery.this, HomeScreenTwo.class);
                                startActivityForResult(myIntent, 0);
                            }
                        })
                .setNegativeButton(
                        getResources().getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                // no operation
                            }
                        });
        bmodel.applyAlertDialogTheme(builder);
    }

    private void onNextButtonClick() {
        if (salesReturnHelper.hasSalesReturn()) {
            if (bmodel.configurationMasterHelper.IS_INVOICE_SR && !mInvoiceListAdapter.isEmpty()) // SR18 Config Code
                showInvoiceNoDialog();
            else
                new SaveAsyncTask().execute();
        } else {
            bmodel.showAlert(
                    getResources().getString(R.string.no_products_exists), 0);
        }
    }

    private void showInvoiceNoDialog() {
        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(this);
        builder.setTitle(null);
        builder.setSingleChoiceItems(mInvoiceListAdapter, mSelectedIndex,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        mSelectedIndex = item;
                        dialog.dismiss();
                        salesReturnHelper.setInvoiceId(mInvoiceListAdapter.getItem(item));
                        new SaveAsyncTask().execute();
                    }
                });

        bmodel.applyAlertDialogTheme(builder);
    }

    private void onNoteButtonClick() {
        FragmentTransaction ft = (this)
                .getSupportFragmentManager().beginTransaction();
        RemarksDialog dialog = new RemarksDialog("MENU_SALES_RET");
        dialog.setCancelable(false);
        dialog.show(ft, "sl_ret_remark");
    }

    private void onBackButtonClick() {
        finish();
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
    }

    @SuppressLint("StaticFieldLeak")
    private class SaveAsyncTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            AlertDialog.Builder builder = new AlertDialog.Builder(SalesReturnSummery.this);
            customProgressDialog(builder, getResources().getString(R.string.saving_sales_return));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            if (!salesReturnHelper.IS_APPLY_DISCOUNT_IN_SR) {
                OrderHelper.getInstance(SalesReturnSummery.this).invoiceDiscount = "0";
                DiscountHelper.getInstance(SalesReturnSummery.this).getBillWiseDiscountList().clear();
            }

            if (!salesReturnHelper.IS_APPLY_TAX_IN_SR) {
                bmodel.productHelper.taxHelper.getBillTaxList().clear();
            }

            bmodel.saveModuleCompletion("MENU_SALES_RET", true);

            if (bmodel.configurationMasterHelper.IS_CREDIT_NOTE_CREATION)
                updateCreditNoteprintList();

            try {
                bmodel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
                        .now(DateTimeUtils.TIME));
                salesReturnHelper.saveSalesReturn(getApplicationContext(), "", "", false, false);
                salesReturnHelper.clearSalesReturnTable(false);
                return Boolean.TRUE;
            } catch (Exception e) {
                Commons.printException(e);
                return Boolean.FALSE;
            }
        }

        protected void onPostExecute(Boolean result) {
            try {
                if (alertDialog != null)
                    alertDialog.dismiss();
            } catch (Exception e) {
                Commons.printException(e);
            }

            if (result) {
                //this activity is not finished while going to CaptureSignatureActivity
                //in CaptureSignatureActivity bmodel context is set
                //so on returning it has to be reset here again to SalesReturnSummary context
                bmodel = (BusinessModel) getApplicationContext();
                bmodel.setContext(SalesReturnSummery.this);
                //clear sign details once sales return is exited to start fresh on next visit
                salesReturnHelper.setIsSignCaptured(false);
                salesReturnHelper.setSignatureName("");
                salesReturnHelper.setSignaturePath("");
                salesReturnHelper.setInvoiceId("");
                if (bmodel.configurationMasterHelper.SHOW_PRINT_CREDIT_NOTE && salesReturnHelper.getTotalValue() > 0) {
                    HashMap<String, String> keyValues = new HashMap<>();
                    keyValues.put("key1", "Tax CreditNote No : " + salesReturnHelper.getCreditNoteId().replaceAll("'", ""));
                    keyValues.put("key2", salesReturnHelper.getSalesReturnID().replaceAll("'", ""));

                    salesReturnHelper.setCreditNoteId("");
                    salesReturnHelper.setSalesReturnID("");

                    if ("1".equalsIgnoreCase(bmodel.getRetailerMasterBO().getRField4())) {
                        bmodel.productHelper.updateDistributorDetails();
                    }
                    bmodel.mCommonPrintHelper.xmlRead("credit_note", false, mPrintList, keyValues, null, null, null);
                    Intent i = new Intent(SalesReturnSummery.this, CommonPrintPreviewActivity.class);
                    i.putExtra("IsFromOrder", true);
                    i.putExtra("isHomeBtnEnable", true);
                    startActivity(i);
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    bmodel = (BusinessModel) getApplicationContext();
                    bmodel.showAlertWithImage("",
                            getResources().getString(R.string.saved_successfully),
                            DataMembers.NOTIFY_SALES_RETURN_SAVED, true);
                }
            } else {
                Toast.makeText(SalesReturnSummery.this, getResources().getString(R.string.saved_Failed), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * This method will setup value to print credit note.
     */
    private void updateCreditNoteprintList() {
        mPrintList = new Vector<>();

        for (ProductMasterBO product : salesReturnHelper.getSalesReturnProducts()) {
            List<SalesReturnReasonBO> reasonList = product.getSalesReturnReasonList();

            int totalSalesReturnQty = 0;
            float totalSalesReturnAmt = 0;
            float replacementPrice = 0;
            if (reasonList != null) {

                for (SalesReturnReasonBO reasonBO : reasonList) {
                    if (reasonBO.getPieceQty() > 0 || reasonBO.getCaseQty() > 0 || reasonBO.getOuterQty() > 0) {
                        //Calculate sales return total qty and price.
                        int totalQty = reasonBO.getPieceQty() + (reasonBO.getCaseQty() * product.getCaseSize()) + (reasonBO.getOuterQty() * product.getOutersize());
                        totalSalesReturnQty = totalSalesReturnQty + totalQty;
                        totalSalesReturnAmt = totalSalesReturnAmt + (totalQty * reasonBO.getSrpedit());
                        // Higher SRP edit price will be considered for replacement product price.
                        if (replacementPrice < reasonBO.getSrpedit())
                            replacementPrice = reasonBO.getSrpedit();
                    }
                }
            }

            // Calculate replacement qty price.
            int totalReplaceQty = product.getRepPieceQty() + (product.getRepCaseQty() * product.getCaseSize()) + (product.getRepOuterQty() * product.getOutersize());
            float totalReplacementPrice = totalReplaceQty * replacementPrice;

            int totalBalanceQty = totalSalesReturnQty - totalReplaceQty;
            float totalBalanceAmount = totalSalesReturnAmt - totalReplacementPrice;

            // set the total qty and value in ProductBO to enable print.
            if (totalBalanceQty > 0) {
                ProductMasterBO productMasterBO = new ProductMasterBO(product);
                productMasterBO.setOrderedPcsQty(totalBalanceQty);
                productMasterBO.setTotalOrderedQtyInPieces(totalBalanceQty);
                productMasterBO.setNetValue(SDUtil.formatAsPerCalculationConfig(totalBalanceAmount));
                mPrintList.add(productMasterBO);
            }

        }
    }
}