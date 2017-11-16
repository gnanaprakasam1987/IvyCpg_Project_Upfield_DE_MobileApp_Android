package com.ivy.sd.png.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.SalesReturnReasonBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.SalesReturnHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.DateUtil;
import com.ivy.sd.print.CommonPrintPreviewActivity;

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
    private Toolbar toolbar;
    private Button mBtnSave;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_salesreturn_summary);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        /** Initilize the ActionBar and set title to it **/
        if (getSupportActionBar() != null)
            setScreenTitle("Sales Return Summary");
        // Used to on / off the back arrow icon
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        salesReturnHelper = SalesReturnHelper.getInstance(this);


        outPutDateFormat = ConfigurationMasterHelper.outDateFormat;
        lvwplist = (ListView) findViewById(R.id.list);
        lvwplist.setCacheColorHint(0);


        TextView totalValue = (TextView) findViewById(R.id.totalValue);
        totalValue.setText(bmodel.formatValue(salesReturnHelper.getReturnValue()));
        TextView lineValue = (TextView) findViewById(R.id.lcpValue);
        String strLpcValue = salesReturnHelper.getLpcValue() + "";
        lineValue.setText(strLpcValue);

        mBtnSave = (Button) findViewById(R.id.btn_save);

        lineValue.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        totalValue.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        mBtnSave.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        ((TextView) findViewById(R.id.totalText)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) findViewById(R.id.lpc_title)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ;

        mBtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onNextButtonClick();
            }
        });
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

        for (ProductMasterBO product : bmodel.productHelper.getProductMaster()) {
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

        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            SalesReturnReasonBO product1 = items.get(position);
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(R.layout.row_salesreturn_summery,
                        parent, false);
                holder = new ViewHolder();

                holder.caseQty = (TextView) row
                        .findViewById(R.id.productqtyCases);
                holder.pieceQty = (TextView) row
                        .findViewById(R.id.productqtyPieces);
                holder.outerQty = (TextView) row
                        .findViewById(R.id.outerproductqtyCases);
                holder.srPieceQty = (TextView) row.findViewById(R.id.srQtyPcs);
                holder.srCaseQty = (TextView) row.findViewById(R.id.srQtyCases);
                holder.srOuterQty = (TextView) row.findViewById(R.id.srQtyouter);

                holder.psname = (TextView) row.findViewById(R.id.productName);
                holder.tvReason = (TextView) row.findViewById(R.id.tv_reason);

                holder.mfgDate = (TextView) row.findViewById(R.id.mfgDate);
                holder.expDate = (TextView) row.findViewById(R.id.expDate);
                holder.oldMrp = (TextView) row.findViewById(R.id.oldMrp);
                holder.invoiceno = (TextView) row.findViewById(R.id.invoiceno);
                holder.srpedit = (TextView) row.findViewById(R.id.srpedit);
                holder.lotnumber = (TextView) row.findViewById(R.id.lotnumber);

                //typefaces
                holder.psname.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.tvReason.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.mfgDate.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.oldMrp.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.expDate.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.invoiceno.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.srpedit.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.lotnumber.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.caseQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.pieceQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.outerQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.srPieceQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.srCaseQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.srOuterQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

                ((TextView) row.findViewById(R.id.tv_prodname_title)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                ((TextView) row.findViewById(R.id.reasonTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                ((TextView) row.findViewById(R.id.mfgDateTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                ((TextView) row.findViewById(R.id.expDateTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                ((TextView) row.findViewById(R.id.invoicenoTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                ((TextView) row.findViewById(R.id.lotnumberTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                ((TextView) row.findViewById(R.id.oldMrpTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                ((TextView) row.findViewById(R.id.srpeditTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                ((TextView) row.findViewById(R.id.pcsTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                ((TextView) row.findViewById(R.id.caseTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                ((TextView) row.findViewById(R.id.outercaseTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                ((TextView) row.findViewById(R.id.srPcsTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                ((TextView) row.findViewById(R.id.srCaseTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                ((TextView) row.findViewById(R.id.srOutercaseTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

                if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE) {
                    ((LinearLayout) row.findViewById(R.id.ll_case)).setVisibility(View.GONE);
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
                if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
                    ((LinearLayout) row.findViewById(R.id.ll_pc)).setVisibility(View.GONE);
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
                if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
                    ((LinearLayout) row.findViewById(R.id.ll_outer)).setVisibility(View.GONE);
                if (!salesReturnHelper.SHOW_STOCK_REPLACE_PCS)
                    ((LinearLayout) row.findViewById(R.id.ll_srpc)).setVisibility(View.GONE);
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
                    ((LinearLayout) row.findViewById(R.id.ll_srcase)).setVisibility(View.GONE);
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
                    ((LinearLayout) row.findViewById(R.id.ll_sroo)).setVisibility(View.GONE);
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
                    ((LinearLayout) row.findViewById(R.id.ll_oldmrp)).setVisibility(View.GONE);
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
                    ((LinearLayout) row.findViewById(R.id.ll_mfd)).setVisibility(View.GONE);
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
                    ((LinearLayout) row.findViewById(R.id.ll_expd)).setVisibility(View.GONE);
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
                if (!salesReturnHelper.SHOW_SRP_EDIT) {
                    ((LinearLayout) row.findViewById(R.id.ll_srpEdit)).setVisibility(View.GONE);
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
                    ((LinearLayout) row.findViewById(R.id.ll_lotno)).setVisibility(View.GONE);
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
                    ((LinearLayout) row.findViewById(R.id.ll_invoiceno)).setVisibility(View.GONE);
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
                    .setText((holder.productBO.getMfgDate() == null) ? DateUtil
                            .convertFromServerDateToRequestedFormat(
                                    SDUtil.now(SDUtil.DATE_GLOBAL),
                                    outPutDateFormat) : holder.productBO
                            .getMfgDate());
            holder.expDate
                    .setText((holder.productBO.getExpDate() == null) ? DateUtil
                            .convertFromServerDateToRequestedFormat(
                                    SDUtil.now(SDUtil.DATE_GLOBAL),
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

        if (!bmodel.configurationMasterHelper.SHOW_REMARKS_SAL_RET) {
            menu.findItem(R.id.menu_reviews).setVisible(false);
        } else
            menu.findItem(R.id.menu_reviews).setVisible(true);

        if (!bmodel.configurationMasterHelper.IS_SIH_VALIDATION)
            menu.findItem(R.id.menu_clear_tran).setVisible(true);

        menu.findItem(R.id.menu_next).setVisible(false);


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
        }
        return super.onOptionsItemSelected(item);
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
                                salesReturnHelper.clearTransaction();
                                Toast.makeText(SalesReturnSummery.this, getResources().getString(R.string.tran_deleted_successfully), Toast.LENGTH_LONG).show();
                                BusinessModel.loadActivity(SalesReturnSummery.this,
                                        DataMembers.actHomeScreenTwo);
                                finish();
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
            new SaveAsyncTask().execute();
        } else {
            bmodel.showAlert(
                    getResources().getString(R.string.no_products_exists), 0);
        }
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

    private class SaveAsyncTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            AlertDialog.Builder builder = new AlertDialog.Builder(SalesReturnSummery.this);
            bmodel.customProgressDialog(alertDialog, builder, SalesReturnSummery.this, getResources().getString(R.string.saving_sales_return));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            if (!bmodel.configurationMasterHelper.IS_APPLY_DISCOUNT_IN_SR) {
                bmodel.invoiceDisount = "0";
                bmodel.productHelper.getBillWiseDiscountList().clear();
            }

            if (!bmodel.configurationMasterHelper.IS_APPLY_TAX_IN_SR) {
                bmodel.productHelper.getTaxList().clear();
            }

            bmodel.saveModuleCompletion("MENU_SALES_RET");

            if (bmodel.configurationMasterHelper.IS_CREDIT_NOTE_CREATION)
                updateCreditNoteprintList();

            try {
                bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                        .now(SDUtil.TIME));
                salesReturnHelper.saveSalesReturn();
                // Update isVisited Flag
                bmodel.updateIsVisitedFlag();
                salesReturnHelper.clearSalesReturnTable();
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
                if (bmodel.configurationMasterHelper.SHOW_PRINT_CREDIT_NOTE && salesReturnHelper.getTotalValue() > 0) {
                    HashMap<String, String> keyValues = new HashMap<>();
                    keyValues.put("key1", "Tax CreditNote No : " + salesReturnHelper.getCreditNoteId().replaceAll("'", ""));
                    keyValues.put("key2", salesReturnHelper.getSalesReturnID().replaceAll("'", ""));

                    salesReturnHelper.setCreditNoteId("");
                    salesReturnHelper.setSalesReturnID("");

                    if ("1".equalsIgnoreCase(bmodel.getRetailerMasterBO().getRField4())) {
                        bmodel.productHelper.updateDistributorDetails();
                    }
                    bmodel.mCommonPrintHelper.xmlRead("credit_note", false, mPrintList, keyValues);
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
                            DataMembers.NOTIFY_SALES_RETURN_SAVED,true);
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

        for (ProductMasterBO product : bmodel.productHelper.getProductMaster()) {
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
                productMasterBO.setDiscount_order_value(totalBalanceAmount);
                mPrintList.add(productMasterBO);
            }

        }
    }
}