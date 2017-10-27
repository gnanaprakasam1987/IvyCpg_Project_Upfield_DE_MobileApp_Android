package com.ivy.sd.png.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.CreditNoteListBO;
import com.ivy.sd.png.bo.InvoiceHeaderBO;
import com.ivy.sd.png.bo.PaymentBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.sd.print.CommonPrintPreviewActivity;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by nagaganesh.n on 19-04-2017.
 */
public class BillPaymentActivityFragment extends IvyBaseFragment implements View.OnClickListener {

    private BusinessModel bmodel;
    private ArrayList<InvoiceHeaderBO> mInvioceList;
    private ArrayList<PaymentBO> mPaymentList;
    private ArrayList<CreditNoteListBO> mCreditNoteList;
    private TextView mPayableAmtTV, mDiscTV, mOSAmtTV, mCollectionAmtTV, mBalaceAmtTV;
    private RecyclerView recyclerView_paytype;
    private boolean isAdvancePaymentAvailable;
    private Button payBtn;
    private HashMap<String, PaymentBO> mPaymentBOByMode;
    private String mErrorMsg = "";
    private ArrayList<InvoiceHeaderBO> mSelecteInvoiceList;

    private AlertDialog.Builder build;
    private AlertDialog alertDialog;

    public BillPaymentActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_bill_payment, container, false);
        setHasOptionsMenu(true);
        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayShowTitleEnabled(false);
        }
        setScreenTitle(bmodel.mSelectedActivityName);

        recyclerView_paytype = (RecyclerView) view.findViewById(R.id.paymentmode_recycview);
        mPayableAmtTV = (TextView) view.findViewById(R.id.tv_paidamt);
        mDiscTV = (TextView) view.findViewById(R.id.tv_disc_amt);
        //mOSAmtTV = (TextView)view.findViewById(R.id.tv_osamount);
        mBalaceAmtTV = (TextView) view.findViewById(R.id.tv_balanceamt);
        mCollectionAmtTV = (TextView) view.findViewById(R.id.tv_collectionamt);
        payBtn = (Button) view.findViewById(R.id.paybtn);
        payBtn.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        payBtn.setOnClickListener(this);

        mPayableAmtTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        mDiscTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        mBalaceAmtTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));

        mInvioceList = bmodel.getInvoiceHeaderBO();
        mPaymentList = bmodel.collectionHelper.getCollectionPaymentList();
        mPaymentBOByMode = bmodel.collectionHelper.getPaymentBoByMode();
        updateIsAdvancePaymentAvailabe();

        if (!isAdvancePaymentAvailable) {
            updateBottomValuesForSelectedInvoice();
            if (mPaymentList != null)
                loadPayTypeList();
        }

        return view;
    }

    /**
     * Method used to load payment type list.
     */
    private void loadPayTypeList() {
        PayTypeRecyclerAdapter adapter = new PayTypeRecyclerAdapter(getActivity());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView_paytype.setLayoutManager(mLayoutManager);
        recyclerView_paytype.setItemAnimator(new DefaultItemAnimator());
        recyclerView_paytype.setAdapter(adapter);
    }

    /**
     * Method used to update payment amount.
     */
    private void updateBottomValuesForSelectedInvoice() {
        double mTotalOSAmt = 0;
        double mTotalPayableAmt = 0;
        double mTotalDiscAmt = 0;
        double paidAmt = 0;
        double givenDiscAmt = 0;
        double mBalanceAmt = 0;

        if (mInvioceList != null && mInvioceList.size() > 0) {
            for (InvoiceHeaderBO invoiceHeaderBO : mInvioceList) {
                if (invoiceHeaderBO.isChkBoxChecked()) {
                    mTotalOSAmt = mTotalOSAmt + (invoiceHeaderBO.getBalance() + invoiceHeaderBO.getRemainingDiscountAmt());
                    mTotalPayableAmt = mTotalPayableAmt + invoiceHeaderBO.getBalance();
                    mTotalDiscAmt = mTotalDiscAmt + invoiceHeaderBO.getRemainingDiscountAmt();
                }
            }
        }

        if (mPaymentList != null && mPaymentList.size() > 0) {
            for (PaymentBO paymentBO : mPaymentList) {
                if (paymentBO.getAmount() > 0) {
                    paidAmt = paidAmt + paymentBO.getAmount();
                    givenDiscAmt = givenDiscAmt + paymentBO.getDiscountedAmount();
                }
            }
        }

        //mTotalPayableAmt = mTotalPayableAmt - paidAmt;
        mTotalDiscAmt = mTotalDiscAmt - givenDiscAmt;
        if (mTotalPayableAmt > 0) {
            mPayableAmtTV.setText(bmodel.formatValueBasedOnConfig(mTotalPayableAmt));
        } else {
            mPayableAmtTV.setText(bmodel.formatValue(0));
        }
        if (mTotalDiscAmt > 0) {
            mDiscTV.setText(bmodel.formatValueBasedOnConfig(mTotalDiscAmt) + "");
        } else {
            mDiscTV.setText(bmodel.formatValue(0));
        }

        //mOSAmtTV.setText(bmodel.formatValue(mTotalOSAmt));
        if (mTotalPayableAmt > 0) {
            mCollectionAmtTV.setText(bmodel.formatValue(paidAmt));
        } else {
            mCollectionAmtTV.setText(bmodel.formatValue(0));
        }

        mBalanceAmt = mTotalPayableAmt - paidAmt;
        if (mBalanceAmt > 0) {
            mBalaceAmtTV.setText(bmodel.formatValueBasedOnConfig(mBalanceAmt));
        } else {
            mBalaceAmtTV.setText(bmodel.formatValue(0));
        }

    }

    /**
     * Custom Recyclerview adapter for Billpayment.
     */
    public class PayTypeRecyclerAdapter extends RecyclerView.Adapter<PayTypeRecyclerAdapter.MyViewHolder> {

        private Context context;

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private TextView nameTV;
            private ImageView imgNext;
            private LinearLayout linearLayout;
            private TextView paidAmtLabel;
            private StringBuilder paidLabel = new StringBuilder();

            public MyViewHolder(View view) {
                super(view);

                nameTV = (TextView) view
                        .findViewById(R.id.tv_name);
                imgNext = (ImageView) view
                        .findViewById(R.id.btn_inandout);
                linearLayout = (LinearLayout) view
                        .findViewById(R.id.childLayout);
                paidAmtLabel = (TextView) view
                        .findViewById(R.id.tv_paidamtlabel);
                view.setOnClickListener(this);

                nameTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                paidAmtLabel.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

                try {
                    if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                            R.id.tv_paidamtlabel).getTag()) != null)
                        ((TextView) view.findViewById(R.id.tv_paidamtlabel))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(view.findViewById(
                                                R.id.tv_paidamtlabel)
                                                .getTag()));

                } catch (Exception e) {
                    Commons.printException(e + "");
                }
            }

            @Override
            public void onClick(View v) {
                if (mPaymentList.get(getAdapterPosition()).getCashMode().equals("CN")) {
                    if (isCreditNoteAvailable()) {
                        Intent intent = new Intent(getActivity(), PaymentModeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("position", getAdapterPosition());
                        intent.putExtra("IsAdvancePaymentAvailable", isAdvancePaymentAvailable);
                        intent.putExtra("paymode", "" + mPaymentList.get(getAdapterPosition()).getCashMode());
                        startActivity(intent);
                    } else {
                        Toast.makeText(context, getResources().getString(R.string.credit_note_not_available), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Intent intent = new Intent(getActivity(), PaymentModeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("position", getAdapterPosition());
                    intent.putExtra("IsAdvancePaymentAvailable", isAdvancePaymentAvailable);
                    intent.putExtra("paymode", "" + mPaymentList.get(getAdapterPosition()).getCashMode());
                    startActivity(intent);
                }
            }
        }

        public PayTypeRecyclerAdapter(Context context) {
            this.context = context;
        }


        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_bill_payment, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final PayTypeRecyclerAdapter.MyViewHolder holder, int position) {

            holder.nameTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
            holder.nameTV.setText(mPaymentList.get(position).getListName());

            if (mPaymentList.get(position).getAmount() > 0) {
                holder.paidLabel.append(holder.paidAmtLabel.getText().toString());
                holder.paidLabel.append(" " + bmodel.formatValueBasedOnConfig(mPaymentList.get(position).getAmount()) + " paid");
                holder.paidAmtLabel.setText(holder.paidLabel.toString());
            } else {
                holder.paidAmtLabel.setText("");
            }


            if (position % 2 == 0) {
                holder.linearLayout.setBackgroundColor(getResources().getColor(R.color.list_even_item_bg));
            } else {
                holder.linearLayout.setBackgroundColor(getResources().getColor(R.color.list_odd_item_bg));
            }

        }

        @Override
        public int getItemCount() {
            return mPaymentList.size();
        }

    }

    private boolean isCreditNoteAvailable() {
        boolean isAvaiable = false;
        String modeID = bmodel.getStandardListIdAndType(
                "CNAP",
                StandardListMasterConstants.CREDIT_NOTE_TYPE);
        if (bmodel.collectionHelper.getCreditNoteList() != null) {
            mCreditNoteList = new ArrayList<>();
            for (CreditNoteListBO bo : bmodel.collectionHelper
                    .getCreditNoteList()) {
                if (bo.getRetailerId().equals(
                        bmodel.getRetailerMasterBO().getRetailerID())
                        && !bo.isUsed() && (!modeID.equals(bo.getTypeId() + "")))
                    mCreditNoteList.add(bo);
            }
            if (mCreditNoteList != null && mCreditNoteList.size() > 0)
                isAvaiable = true;

        }


        return isAvaiable;
    }

    //Method used to save collection.
    private void saveCollection() {
        if (isExceedCollectAmount()) {
            Toast.makeText(
                    getActivity(),
                    getResources()
                            .getString(
                                    R.string.amount_exeeds_the_balance_please_check),
                    Toast.LENGTH_SHORT).show();
        } else if (!isValidate()) {
            Toast.makeText(getActivity(), "Please " + mErrorMsg, Toast.LENGTH_SHORT).show();
        } else if (!isAmountEntered()) {
            Toast.makeText(getActivity(), getResources()
                    .getString(
                            R.string.alert_amount), Toast.LENGTH_SHORT).show();
        } else if (isAdvancePaymentAvailable
                && !bmodel.collectionHelper.isPaidAmountwithoutAdvanePayment()
                && !bmodel.collectionHelper.isUseAllAdvancePaymentAmt()) {
            Toast.makeText(getActivity(), getResources().getString(R.string.please_user_advancepayment), Toast.LENGTH_SHORT).show();
        } else if (bmodel.configurationMasterHelper.IS_PAYMENT_RECEIPTNO_GET) {
            ReceiptNoDialogFragment fragment = new ReceiptNoDialogFragment();
            fragment.setCancelable(false);
            fragment.setTargetFragment(BillPaymentActivityFragment.this, 1);//dialog dismiss call back to save collection after getting receipt number
            fragment.show(getFragmentManager(), getResources().getString(R.string.receipt_fragment));

        } else {
            new SaveCollection().execute();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (requestCode == 1) {
                new SaveCollection().execute();
            }
        }
    }

    //Validate if totalamount exceed invoice amount
    private boolean isExceedCollectAmount() {
        double totalCollected = 0;
        double totalPayableAmt = 0;
        boolean flag = false;
        if (mPaymentList != null && mPaymentList.size() > 0) {
            for (PaymentBO paymentBO : mPaymentList) {
                totalCollected = totalCollected + paymentBO.getAmount();
            }
        }

        if (mInvioceList != null && mInvioceList.size() > 0) {
            for (InvoiceHeaderBO invoiceHeaderBO : mInvioceList) {
                if (invoiceHeaderBO.isChkBoxChecked()) {
                    totalPayableAmt = totalPayableAmt + invoiceHeaderBO.getBalance();
                }
            }
        }
        totalPayableAmt = Double.parseDouble(bmodel.formatValueBasedOnConfig(totalPayableAmt));
        if (totalCollected > totalPayableAmt) {
            flag = true;
        }
        return flag;
    }

    //Validate Cheque,RTGS and DemandDraft field data.
    private boolean isValidate() {
        PaymentBO paymentBO = mPaymentBOByMode.get(StandardListMasterConstants.CHEQUE);
        if (paymentBO != null && paymentBO.getAmount() > 0) {
            if ("0".equals(paymentBO.getBankID())) {
                mErrorMsg = getResources().getString(R.string.sel_bank) + " in cheque";
                return false;
            }
            if ("0".equals(paymentBO.getBranchId())) {
                mErrorMsg = getResources().getString(R.string.sel_branch) + " in cheque";
                return false;
            }
            if ("".equals(paymentBO.getChequeNumber())) {
                mErrorMsg = getResources().getString(R.string.pls_select_chequeno) + " in cheque";
                return false;
            }
        }
        paymentBO = mPaymentBOByMode.get(StandardListMasterConstants.DEMAND_DRAFT);
        if (paymentBO != null && paymentBO.getAmount() > 0) {
            if ("0".equals(paymentBO.getBankID())) {
                mErrorMsg = getResources().getString(R.string.sel_bank) + " in Demand Draft";
                return false;
            }
            if ("0".equals(paymentBO.getBranchId())) {
                mErrorMsg = getResources().getString(R.string.sel_branch) + " in Demand Draft";
                return false;
            }
            if ("".equals(paymentBO.getChequeNumber())) {
                mErrorMsg = getResources().getString(R.string.pls_select_chequeno) + " in Demand Draft";
                return false;
            }
        }
        paymentBO = mPaymentBOByMode.get(StandardListMasterConstants.RTGS);
        if (paymentBO != null && paymentBO.getAmount() > 0) {
            if ("0".equals(paymentBO.getBankID())) {
                mErrorMsg = getResources().getString(R.string.sel_bank) + " in RTGS";
                return false;
            }
            if ("0".equals(paymentBO.getBranchId())) {
                mErrorMsg = getResources().getString(R.string.sel_branch) + " in RTGS";
                return false;
            }
            if ("".equals(paymentBO.getChequeNumber())) {
                mErrorMsg = getResources().getString(R.string.pls_select_chequeno) + " in RTGS";
                return false;
            }
        }
        paymentBO = mPaymentBOByMode.get(StandardListMasterConstants.MOBILE_PAYMENT);
        if (paymentBO != null && paymentBO.getAmount() > 0) {

            if ("".equals(paymentBO.getChequeNumber())) {
                mErrorMsg = getResources().getString(R.string.please_enter_referenceno) + " in Mobile Payment";
                return false;
            }
        }
        return true;
    }

    //Validate if total payment amount is 0
    private boolean isAmountEntered() {
        for (PaymentBO paymentBO : mPaymentList) {
            if (paymentBO.getAmount() > 0) {
                return true;
            }
        }
        return false;
    }

    //Assign selected invoice list.
    private void updateSelectedList() {
        mSelecteInvoiceList = new ArrayList<>();
        for (InvoiceHeaderBO invoiceHeaderBO : mInvioceList) {
            if (invoiceHeaderBO.isChkBoxChecked()) {
                mSelecteInvoiceList.add(invoiceHeaderBO);
            }
        }
    }

    //Update Payable amount
    private void updatePayableAmt() {
        for (PaymentBO paymentBO : mPaymentList) {
            paymentBO.setUpdatePayableamt(paymentBO.getAmount());
        }
    }

    /**
     * Class used to save collection using AsycTask
     */
    private class SaveCollection extends AsyncTask<String, String, String> {
        String appendString = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            build = new AlertDialog.Builder(getActivity());
            bmodel.customProgressDialog(alertDialog, build, getActivity(), getResources().getString(R.string.saving));
            alertDialog = build.create();
            alertDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            updateSelectedList();
            updatePayableAmt();

            bmodel.collectionHelper.saveCollection(mSelecteInvoiceList, mPaymentList);
            bmodel.saveModuleCompletion("MENU_COLLECTION");
            appendString = printDataforCollectionReport();
            bmodel.mCommonPrintHelper.setInvoiceData(new StringBuilder(appendString));
            if (bmodel.configurationMasterHelper.IS_PRINT_FILE_SAVE) {
                bmodel.writeToFile(appendString,
                        StandardListMasterConstants.PRINT_FILE_COLLECTION + bmodel.collectionHelper.collectionGroupId.replaceAll("\'", ""),"/IvyDist/");
            }
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            alertDialog.dismiss();
            if (bmodel.configurationMasterHelper.SHOW_COLLECTION_PRINT) {
                /*FragmentManager fm = getFragmentManager();
                PrintCountDialogFragment dialogFragment = new PrintCountDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putString("title", getResources().getString(R.string.collection_print_title));
                bundle.putString("textviewTitle", getActivity().getResources().getString(R.string.collection_saved_do_u_print));
                bundle.putInt("isfrom", 0);
                dialogFragment.setArguments(bundle);
                dialogFragment.show(fm, "Sample Fragment");*/
                Intent i = new Intent(getActivity(),
                        CommonPrintPreviewActivity.class);
                i.putExtra("isHomeBtnEnable", true);
                i.putExtra("isFromCollection", true);
                startActivity(i);
                getActivity().finish();
            } else {


                bmodel.collectionHelper.downloadCollectionMethods();

                bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                        SDUtil.now(SDUtil.DATE_GLOBAL),
                        SDUtil.now(SDUtil.TIME), "MENU_COLLECTION");

               /* if (menu.getConfigCode().equals(
                        StandardListMasterConstants.MENU_COLLECTION_VIEW)) {
                    bmodel.collectionHelper.setCollectionView(true);
                    bmodel.getRetailerMasterBO().setIsCollectionView("Y");
                    bmodel.isModuleCompleted("MENU_COLLECTION_VIEW");
                }*/

                Intent intent = new Intent(getActivity(),
                        CollectionScreen.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("screentitle", "MENU_COLLECTION");
                startActivity(intent);
                getActivity().finish();
            }
        }
    }


    /**
     * Method  used to Auto update the Advance Payment Amount.
     */
    private void updateIsAdvancePaymentAvailabe() {
        for (PaymentBO paymentBO : mPaymentList) {
            if (paymentBO.getCashMode().equals(StandardListMasterConstants.ADVANCE_PAYMENT)) {
                isAdvancePaymentAvailable = true;
                mCreditNoteList = bmodel.collectionHelper.getAdvancePaymentList();
                String modeID = bmodel.getStandardListIdAndType(
                        "CNAP",
                        StandardListMasterConstants.COLLECTION_PAY_TYPE);
                if (mCreditNoteList != null) {

                    for (CreditNoteListBO bo : bmodel.collectionHelper
                            .getCreditNoteList()) {
                        if (bo.getRetailerId().equals(
                                bmodel.getRetailerMasterBO().getRetailerID())
                                && !bo.isUsed() && (modeID.equals(bo.getTypeId())))
                            mCreditNoteList.add(bo);
                    }
                }
                applyAdvancePayment();
                updateCreditNotePayment(paymentBO);
                updateBottomValuesForSelectedInvoice();
                loadPayTypeList();

                break;
            }
        }
    }

    /**
     * Method used to apply Advance Payment
     */
    private void applyAdvancePayment() {
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        final ArrayList<InvoiceHeaderBO> invoiceList = new ArrayList<>(bmodel.getInvoiceHeaderBO());
        double totalInvoiceAmout = 0;

        for (InvoiceHeaderBO invoiceHeaderBO : invoiceList) {
            if (invoiceHeaderBO.isChkBoxChecked()) {
                totalInvoiceAmout = totalInvoiceAmout + invoiceHeaderBO.getBalance();
            }
        }
        for (CreditNoteListBO creditNoteListBO : mCreditNoteList) {
            if (totalInvoiceAmout > 0) {
                creditNoteListBO.setChecked(true);
                totalInvoiceAmout = totalInvoiceAmout - creditNoteListBO.getAmount();

            } else {
                creditNoteListBO.setChecked(false);
            }
        }

    }

    /**
     * Apply Creditnote payment.
     *
     * @param mPaymentBO
     */
    private void updateCreditNotePayment(PaymentBO mPaymentBO) {
        double mTotalCreditNoteValue = 0;
        if (mCreditNoteList != null && mCreditNoteList.size() > 0) {
            for (CreditNoteListBO creditNoteListBO : mCreditNoteList) {
                if (creditNoteListBO.isChecked() && creditNoteListBO.getTypeId() != 0) {
                    mTotalCreditNoteValue = mTotalCreditNoteValue + creditNoteListBO.getAmount();
                }
            }
        }

        mPaymentBO.setAmount(mTotalCreditNoteValue);

        if (!bmodel.collectionHelper.isEnterAmountExceed(mPaymentList,StandardListMasterConstants.CASH)) {
            mPaymentBO.setAmount(mTotalCreditNoteValue);
        } else {
            mTotalCreditNoteValue = bmodel.collectionHelper.getBalanceAmountWithOutCreditNote(mPaymentList, false);
            mPaymentBO.setAmount(mTotalCreditNoteValue);
        }

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            getActivity().finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        Button btn = (Button) v;
        if (btn == payBtn) {
            saveCollection();
        }
    }

    private String doPrintFormatingLeft(String str, int maxlength) {
        StringBuilder sb = new StringBuilder();
        if (str.length() >= maxlength) {
            sb.append(str.substring(0, maxlength));
        } else {
            sb.append(str);
            for (int i = 1; i < (maxlength - str.length()); i++)
                sb.append(" ");
        }
        return sb.toString();
    }

    private String doPrintFormatingRight(String str, int maxlength) {
        StringBuilder sb = new StringBuilder();
        if (str.length() > maxlength) {
            sb.append(str.substring(0, maxlength));
        } else {
            sb.append(str);
        }
        return sb.toString();
    }

    private String doPrintAddSpace(int space, int maxlenght) {
        StringBuilder sb = new StringBuilder();
        if (space < maxlenght) {
            for (int i = 0; i < maxlenght - space; i++) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    private String LineFeed(int line) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < line; i++) {
            sb.append("\r\n");
        }
        return sb.toString();
    }

    public String printDataforCollectionReport() {
        StringBuilder sb = new StringBuilder();
        ArrayList<PaymentBO> paymentList = bmodel.collectionHelper.getPaymentData(bmodel.collectionHelper.collectionGroupId);
        try {
            if (paymentList.size() > 0) {
                int center = 0;
                String tempStr;
                if (bmodel.configurationMasterHelper.SHOW_ZEBRA_UNIPAL) {
                    tempStr = "Unipal General Trading Company";
                    if (tempStr.length() < 48) {
                        center = (48 - tempStr.length()) / 2;
                    }

                    sb.append(doPrintAddSpace(0, center));
                    sb.append(doPrintFormatingRight(tempStr, 48));
                    sb.append(LineFeed(1));

                    center = 0;
                    tempStr = "VAT No : 562414227";
                    if (tempStr.length() < 48) {
                        center = (48 - tempStr.length()) / 2;
                    }

                    sb.append(doPrintAddSpace(0, center));
                    sb.append(doPrintFormatingRight(tempStr, 48));
                    sb.append(LineFeed(1));

                    center = 0;
                    tempStr = "Ramallah - Industrial zone, Tel: +972 2 2981060";
                    if (tempStr.length() < 48) {
                        center = (48 - tempStr.length()) / 2;
                    }

                    sb.append(doPrintAddSpace(0, center));
                    sb.append(doPrintFormatingRight(tempStr, 48));
                    sb.append(LineFeed(1));

                    center = 0;
                    tempStr = "Gaza - lndus. Zone - Carny, Tel: +972 7 2830324";
                    if (tempStr.length() < 48) {
                        center = (48 - tempStr.length()) / 2;
                    }

                    sb.append(doPrintAddSpace(0, center));
                    sb.append(doPrintFormatingRight(tempStr, 48));
                    sb.append(LineFeed(1));
                } else {
                    if (bmodel.userMasterHelper.getUserMasterBO().getDistributorName().length() < 48) {
                        center = (48 - bmodel.userMasterHelper.getUserMasterBO().getDistributorName().length()) / 2;
                    }

                    sb.append(doPrintAddSpace(0, center));
                    sb.append(doPrintFormatingRight(bmodel.userMasterHelper.getUserMasterBO().getDistributorName(), 48));
                    sb.append(LineFeed(1));

                    center = 0;
                    if (bmodel.userMasterHelper.getUserMasterBO().getDistributorTinNumber().length() < 48) {
                        center = (48 - bmodel.userMasterHelper.getUserMasterBO().getDistributorTinNumber().length()) / 2;
                    }

                    sb.append(doPrintAddSpace(0, center));
                    sb.append(doPrintFormatingRight(bmodel.userMasterHelper.getUserMasterBO().getDistributorTinNumber(), 48));
                    sb.append(LineFeed(1));

                    center = 0;
                    if (bmodel.userMasterHelper.getUserMasterBO().getDistributorAddress1().length() < 48) {
                        center = (48 - bmodel.userMasterHelper.getUserMasterBO().getDistributorAddress1().length()) / 2;
                    }

                    sb.append(doPrintAddSpace(0, center));
                    sb.append(doPrintFormatingRight(bmodel.userMasterHelper.getUserMasterBO().getDistributorAddress1(), 48));
                    sb.append(LineFeed(1));

                    center = 0;
                    if (bmodel.userMasterHelper.getUserMasterBO().getDistributorAddress2().length() < 48) {
                        center = (48 - bmodel.userMasterHelper.getUserMasterBO().getDistributorAddress2().length()) / 2;
                    }

                    sb.append(doPrintAddSpace(0, center));
                    sb.append(doPrintFormatingRight(bmodel.userMasterHelper.getUserMasterBO().getDistributorAddress2(), 48));
                    sb.append(LineFeed(1));
                }

                for (int i = 0; i < 47; i++) {
                    sb.append("-");
                }
                sb.append(LineFeed(1));

                double total;
                PaymentBO payHeaderBO = paymentList.get(0);
                total = 0;

                if (payHeaderBO.getAdvancePaymentId() != null) {
                    tempStr = "Rcpt Date:" + payHeaderBO.getAdvancePaymentDate();
                } else {
                    tempStr = "Rcpt Date:" + payHeaderBO.getCollectionDateTime();
                }
                sb.append(doPrintFormatingLeft(tempStr, 40));
                sb.append(" ");
                sb.append(LineFeed(1));

                tempStr = "Rcpt NO:" + bmodel.collectionHelper.collectionGroupId.replaceAll("\'", "");
                sb.append(doPrintFormatingLeft(tempStr, 40));
                sb.append(" ");
                sb.append(LineFeed(1));

                tempStr = "AgentCode:" + bmodel.userMasterHelper.getUserMasterBO().getUserCode();
                sb.append(doPrintFormatingLeft(tempStr, 25));
                sb.append(" ");

                tempStr = "AgentName:" + bmodel.userMasterHelper.getUserMasterBO().getUserName();
                sb.append(doPrintFormatingLeft(tempStr, 23));
                sb.append(" ");
                sb.append(LineFeed(1));

                if (payHeaderBO.getRetailerName().length() > 30) {
                    tempStr = payHeaderBO.getRetailerName().substring(0, 30);
                } else {
                    tempStr = payHeaderBO.getRetailerName();
                }

                sb.append(doPrintFormatingLeft("CustName:" + tempStr, 30));
                sb.append(" ");
                sb.append(LineFeed(1));

                tempStr = "CustCode:" + payHeaderBO.getRetailerCode();
                sb.append(doPrintFormatingLeft(tempStr, 30));
                sb.append(" ");
                sb.append(LineFeed(1));

                for (int i = 0; i < 47; i++) {
                    sb.append("-");
                }
                sb.append(LineFeed(1));

                sb.append(doPrintFormatingLeft("Inv No", 10));
                sb.append(LineFeed(1));

                sb.append(doPrintAddSpace(0, 9));
                sb.append(doPrintFormatingLeft("Type", 10));
                sb.append(doPrintFormatingLeft("Date", 12));
                sb.append(doPrintFormatingLeft("Chq Num", 9));
                sb.append(doPrintFormatingLeft(String.format("%10s", "Total"), 13));
                sb.append(LineFeed(1));

                for (int i = 0; i < 47; i++) {
                    sb.append("-");
                }
                sb.append(LineFeed(1));

                double totalDiscount = 0;
                for (PaymentBO payBO : paymentList) {

                    tempStr = payBO.getBillNumber() != null ? payBO.getBillNumber() : getResources().getString(R.string.advance_payment);
                    sb.append(doPrintFormatingLeft(tempStr, 48));
                    sb.append(LineFeed(1));

                    if (payBO.getCashMode().equals(StandardListMasterConstants.CREDIT_NOTE)) {
                        if (payBO.getReferenceNumber().startsWith("AP")) {
                            tempStr = getResources().getString(R.string.advance_payment);
                        } else {
                            tempStr = getResources().getString(R.string.credit_note);
                        }
                    } else {
                        if (payBO.getCashMode().equals(StandardListMasterConstants.CASH)) {
                            tempStr = getResources().getString(R.string.cash);
                        } else if (payBO.getCashMode().equals(StandardListMasterConstants.CHEQUE)) {
                            tempStr = getResources().getString(R.string.cheque);
                        } else if (payBO.getCashMode().equals(StandardListMasterConstants.DEMAND_DRAFT)) {
                            tempStr = "DD";
                        } else if (payBO.getCashMode().equals(StandardListMasterConstants.RTGS)) {
                            tempStr = getResources().getString(R.string.rtgs);
                        } else if (payBO.getCashMode().equals(StandardListMasterConstants.MOBILE_PAYMENT)) {
                            tempStr = "Mob.Pay";
                        }
                    }

                    sb.append(doPrintAddSpace(0, 9));
                    sb.append(doPrintFormatingLeft(tempStr, 10));
                    sb.append(doPrintFormatingLeft(payBO.getChequeDate() + "", 12));

                    if (!payBO.getCashMode().equals(StandardListMasterConstants.CREDIT_NOTE) && !payBO.getCashMode().equals(StandardListMasterConstants.ADVANCE_PAYMENT))
                        tempStr = "" + payBO.getChequeNumber();
                    else {
                        tempStr = "" + payBO.getReferenceNumber();
                    }
                    sb.append(doPrintFormatingLeft(tempStr, 9));
                    sb.append(doPrintFormatingLeft(String.format("%10s", bmodel.formatValueBasedOnConfig(payBO.getAmount())), 12));
                    sb.append(LineFeed(1));

                    if (!payBO.getCashMode().equals(StandardListMasterConstants.CREDIT_NOTE))
                        total += payBO.getAmount();
                    totalDiscount += payBO.getAppliedDiscountAmount();

                }
                for (int i = 0; i < 47; i++) {
                    sb.append("-");
                }
                sb.append(LineFeed(1));

                sb.append(doPrintFormatingLeft("Discount ", 12));
                sb.append(doPrintFormatingLeft(bmodel.formatValueBasedOnConfig(totalDiscount), 10));
                sb.append(doPrintAddSpace(0, 7));
                sb.append(doPrintFormatingLeft("Total ", 7));
                sb.append(String.format("%14s", bmodel.formatValueBasedOnConfig(total)));
                sb.append(LineFeed(1));

                for (int i = 0; i < 47; i++) {
                    sb.append("-");
                }
                sb.append(LineFeed(1));

                sb.append(LineFeed(1));
                sb.append(doPrintFormatingLeft("Comments: ----------------------------------------------", 47));
                sb.append(LineFeed(2));
                sb.append(doPrintFormatingLeft("Signature: ---------------------------------------------", 47));
                sb.append(LineFeed(2));

                return sb.toString();
            }

        } catch (Exception e) {
            Commons.printException(e);
        }
        return sb.toString();
    }


}
