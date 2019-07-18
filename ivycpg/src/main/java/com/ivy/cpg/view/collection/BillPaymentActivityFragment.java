package com.ivy.cpg.view.collection;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.sd.print.CommonPrintPreviewActivity;
import com.ivy.utils.DateTimeUtils;

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
    private TextView mPayableAmtTV, mDiscTV, mCollectionAmtTV, mBalaceAmtTV;
    private RecyclerView recyclerView_paytype;
    private boolean isAdvancePaymentAvailable;
    private Button payBtn;
    private HashMap<String, PaymentBO> mPaymentBOByMode;
    private String mErrorMsg = "";
    private ArrayList<InvoiceHeaderBO> mSelecteInvoiceList;
    private AlertDialog alertDialog;

    private CollectionHelper collectionHelper;

    public BillPaymentActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        collectionHelper = CollectionHelper.getInstance(getActivity());
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

        recyclerView_paytype = view.findViewById(R.id.paymentmode_recycview);
        mPayableAmtTV = view.findViewById(R.id.tv_paidamt);
        mDiscTV = view.findViewById(R.id.tv_disc_amt);
        mBalaceAmtTV = view.findViewById(R.id.tv_balanceamt);
        mCollectionAmtTV = view.findViewById(R.id.tv_collectionamt);
        payBtn = view.findViewById(R.id.paybtn);
        payBtn.setOnClickListener(this);

        mInvioceList = bmodel.getInvoiceHeaderBO();
        mPaymentList = collectionHelper.getCollectionPaymentList();
        mPaymentBOByMode = collectionHelper.getPaymentBoByMode();
        updateIsAdvancePaymentAvailabe();

        if (!isAdvancePaymentAvailable) {
            updateBottomValuesForSelectedInvoice();
            if (mPaymentList != null)
                loadPayTypeList();
        }

        if (!bmodel.configurationMasterHelper.SHOW_DISC_AMOUNT_ALLOW) {
            view.findViewById(R.id.ll_disc).setVisibility(View.GONE);
            view.findViewById(R.id.layout_vertical_line).setVisibility(View.GONE);
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

        mTotalDiscAmt = mTotalDiscAmt - givenDiscAmt;
        if (mTotalPayableAmt > 0) {
            mPayableAmtTV.setText(bmodel.formatBasedOnCurrency(mTotalPayableAmt));
        } else {
            mPayableAmtTV.setText(bmodel.formatValue(0));
        }
        if (mTotalDiscAmt > 0) {
            mDiscTV.setText(bmodel.formatBasedOnCurrency(mTotalDiscAmt) + "");
        } else {
            mDiscTV.setText(bmodel.formatValue(0));
        }

        if (mTotalPayableAmt > 0) {
            mCollectionAmtTV.setText(bmodel.formatValue(paidAmt));
        } else {
            mCollectionAmtTV.setText(bmodel.formatValue(0));
        }

        mBalanceAmt = mTotalPayableAmt - paidAmt;
        mBalanceAmt = Double.parseDouble(bmodel.formatBasedOnCurrency(mBalanceAmt));
        if (mBalanceAmt > 0) {
            mBalaceAmtTV.setText(bmodel.formatBasedOnCurrency(mBalanceAmt));
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
            private LinearLayout linearLayout;
            private TextView paidAmtLabel;

            public MyViewHolder(View view) {
                super(view);

                nameTV = view
                        .findViewById(R.id.tv_name);
                linearLayout = view
                        .findViewById(R.id.childLayout);
                paidAmtLabel = view
                        .findViewById(R.id.tv_paidamtlabel);
                view.setOnClickListener(this);

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

        private PayTypeRecyclerAdapter(Context context) {
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
            holder.nameTV.setText(mPaymentList.get(position).getListName());

            if (mPaymentList.get(position).getAmount() > 0) {

                String paidLabelVal = " " + bmodel.formatValue(mPaymentList.get(position).getAmount()) + " " + getString(R.string.paid);

                holder.paidAmtLabel.setText(paidLabelVal);
            } else {
                holder.paidAmtLabel.setText("");
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
        if (collectionHelper.getCreditNoteList() != null) {
            mCreditNoteList = new ArrayList<>();
            for (CreditNoteListBO bo : collectionHelper
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
                && !collectionHelper.isPaidAmountwithoutAdvanePayment()
                && !collectionHelper.isUseAllAdvancePaymentAmt()) {
            Toast.makeText(getActivity(), getResources().getString(R.string.please_user_advancepayment), Toast.LENGTH_SHORT).show();
        } else if (bmodel.configurationMasterHelper.IS_PAYMENT_RECEIPTNO_GET) {
            ReceiptNoDialogFragment fragment = new ReceiptNoDialogFragment();
            fragment.setCancelable(false);
            fragment.setTargetFragment(BillPaymentActivityFragment.this, 1);//dialog dismiss call back to save collection after getting receipt number
            fragment.show(getFragmentManager(), getResources().getString(R.string.receipt_fragment));

        } else if (bmodel.configurationMasterHelper.IS_FULL_PAYMENT && !isFullAmountCollected()) {
            Toast.makeText(getActivity(), getResources()
                    .getString(
                            R.string.Partial_payment_not_allowed), Toast.LENGTH_SHORT).show();
        } else {
            new SaveCollection().execute();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1)
            new SaveCollection().execute();

    }

    private boolean isFullAmountCollected() {
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
        totalCollected = Double.parseDouble(bmodel.formatBasedOnCurrency(totalCollected));
        totalPayableAmt = Double.parseDouble(bmodel.formatBasedOnCurrency(totalPayableAmt));
        if (totalCollected == totalPayableAmt) {
            flag = true;
        }
        return flag;
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
        totalPayableAmt = SDUtil.convertToDouble(bmodel.formatBasedOnCurrency(totalPayableAmt));
        totalCollected = SDUtil.convertToDouble(bmodel.formatBasedOnCurrency(totalCollected));
        if (totalCollected > totalPayableAmt) {
            flag = true;
        }
        return flag;
    }

    //Validate Cheque,RTGS and DemandDraft field data.
    private boolean isValidate() {
        PaymentBO paymentBO = mPaymentBOByMode.get(StandardListMasterConstants.CHEQUE);
        if (paymentBO != null && paymentBO.getAmount() > 0) {
            if ("".equals(paymentBO.getChequeNumber())) {
                mErrorMsg = getResources().getString(R.string.pls_select_chequeno) + " in cheque";
                return false;
            }
            if ("-1".equals(paymentBO.getBankID())) {
                mErrorMsg = getResources().getString(R.string.sel_bank) + " in cheque";
                return false;
            }
            if ("-1".equals(paymentBO.getBranchId())) {
                mErrorMsg = getResources().getString(R.string.sel_branch) + " in cheque";
                return false;
            }
            if ("0".equals(paymentBO.getBankID()) && "0".equals(paymentBO.getBranchId())) {
                if ("".equals(paymentBO.getBankName())) {
                    mErrorMsg = getResources().getString(R.string.pls_etr_bnk_name) + " in cheque";
                    return false;
                }
                if ("".equals(paymentBO.getBranchName())) {
                    mErrorMsg = getResources().getString(R.string.pls_etr_brc_name) + " in cheque";
                    return false;
                }
            }
        }
        paymentBO = mPaymentBOByMode.get(StandardListMasterConstants.DEMAND_DRAFT);
        if (paymentBO != null && paymentBO.getAmount() > 0) {
            if ("".equals(paymentBO.getChequeNumber())) {
                mErrorMsg = getResources().getString(R.string.pls_select_chequeno) + " in Demand Draft";
                return false;
            }
            if ("-1".equals(paymentBO.getBankID())) {
                mErrorMsg = getResources().getString(R.string.sel_bank) + " in Demand Draft";
                return false;
            }
            if ("-1".equals(paymentBO.getBranchId())) {
                mErrorMsg = getResources().getString(R.string.sel_branch) + " in Demand Draft";
                return false;
            }
            if ("0".equals(paymentBO.getBankID()) && "0".equals(paymentBO.getBranchId())) {
                if ("".equals(paymentBO.getBankName())) {
                    mErrorMsg = getResources().getString(R.string.pls_etr_bnk_name) + " in Demand Draft";
                    return false;
                }
                if ("".equals(paymentBO.getBranchName())) {
                    mErrorMsg = getResources().getString(R.string.pls_etr_brc_name) + " in Demand Draft";
                    return false;
                }
            }
        }
        paymentBO = mPaymentBOByMode.get(StandardListMasterConstants.RTGS);
        if (paymentBO != null && paymentBO.getAmount() > 0) {
            if ("".equals(paymentBO.getChequeNumber())) {
                mErrorMsg = getResources().getString(R.string.pls_select_chequeno) + " in RTGS";
                return false;
            }
            if ("-1".equals(paymentBO.getBankID())) {
                mErrorMsg = getResources().getString(R.string.sel_bank) + " in RTGS";
                return false;
            }
            if ("-1".equals(paymentBO.getBranchId())) {
                mErrorMsg = getResources().getString(R.string.sel_branch) + " in RTGS";
                return false;
            }
            if ("0".equals(paymentBO.getBankID()) && "0".equals(paymentBO.getBranchId())) {
                if ("".equals(paymentBO.getBankName())) {
                    mErrorMsg = getResources().getString(R.string.pls_etr_bnk_name) + " in RTGS";
                    return false;
                }
                if ("".equals(paymentBO.getBranchName())) {
                    mErrorMsg = getResources().getString(R.string.pls_etr_brc_name) + " in RTGS";
                    return false;
                }
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
            showProgressDialog();
        }

        @Override
        protected String doInBackground(String... params) {
            updateSelectedList();
            updatePayableAmt();

            collectionHelper.saveCollection(mSelecteInvoiceList, mPaymentList);
            bmodel.saveModuleCompletion("MENU_COLLECTION", true);
            bmodel.saveModuleCompletion("MENU_COLLECTION", true);
            if (bmodel.configurationMasterHelper.COMMON_PRINT_INTERMEC)
                appendString = print2inchDataforCollectionReport();
            else
                appendString = printDataforCollectionReport();
            bmodel.mCommonPrintHelper.setInvoiceData(new StringBuilder(appendString));
            if (bmodel.configurationMasterHelper.IS_PRINT_FILE_SAVE) {
                bmodel.writeToFile(appendString,
                        StandardListMasterConstants.PRINT_FILE_COLLECTION + collectionHelper.collectionGroupId.replaceAll("\'", ""), "/IvyDist/", "");
            }
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if (getActivity().isDestroyed()) { // or call isFinishing() if min sdk version < 17
                    return;
                }
            } else if (getActivity().isFinishing()) { // or call isFinishing() if min sdk version < 17
                return;
            }
            dismissProgressDialog();
            if (bmodel.configurationMasterHelper.SHOW_COLLECTION_PRINT) {
                Intent i = new Intent(getActivity(),
                        CommonPrintPreviewActivity.class);
                i.putExtra("isHomeBtnEnable", true);
                i.putExtra("isFromCollection", true);
                startActivity(i);
                getActivity().finish();
            } else {

                collectionHelper.downloadCollectionMethods();
                bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                        DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                        DateTimeUtils.now(DateTimeUtils.TIME), "MENU_COLLECTION");

                Intent intent = new Intent(getActivity(),
                        CollectionScreen.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("screentitle", "MENU_COLLECTION");

                if (bmodel.configurationMasterHelper.SHOW_NO_COLLECTION_REASON)
                    intent.putExtra("IS_NO_COLL_REASON", true);

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
                mCreditNoteList = collectionHelper.getAdvancePaymentList();
                String modeID = bmodel.getStandardListIdAndType(
                        "CNAP",
                        StandardListMasterConstants.COLLECTION_PAY_TYPE);
                if (mCreditNoteList != null) {

                    for (CreditNoteListBO bo : collectionHelper
                            .getCreditNoteList()) {
                        if (bo.getRetailerId().equals(
                                bmodel.getRetailerMasterBO().getRetailerID())
                                && !bo.isUsed() && (modeID.equals(bo.getTypeId() + "")))
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
     * @param mPaymentBO payment
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

        if (!collectionHelper.isEnterAmountExceed(mPaymentList, StandardListMasterConstants.CASH)) {
            mPaymentBO.setAmount(mTotalCreditNoteValue);
        } else {
            mTotalCreditNoteValue = collectionHelper.getBalanceAmountWithOutCreditNote(mPaymentList, false);
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
            try {
                saveCollection();
            } catch (Exception ex) {
                Commons.printException(ex);
            }
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
        ArrayList<PaymentBO> paymentList = collectionHelper.getPaymentData(collectionHelper.collectionGroupId);
        try {
            if (paymentList.size() > 0) {
                int center = 0;
                String tempStr;
                if (bmodel.configurationMasterHelper.SHOW_PRINT_HEADERS) {
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

                for (int i = 0; i < 40; i++) {
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

                tempStr = "Rcpt NO:" + collectionHelper.collectionGroupId.replaceAll("\'", "");
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

                for (int i = 0; i < 40; i++) {
                    sb.append("-");
                }
                sb.append(LineFeed(1));

                sb.append(doPrintFormatingLeft("Inv No", 10));
                sb.append(LineFeed(1));

                sb.append(doPrintAddSpace(0, 6));
                sb.append(doPrintFormatingLeft("Type", 10));
                sb.append(doPrintFormatingLeft("Date", 14));
                sb.append(doPrintFormatingLeft("Ref Num", 8));
                sb.append(doPrintFormatingLeft(String.format("%10s", "Total"), 13));
                sb.append(LineFeed(1));

                for (int i = 0; i < 40; i++) {
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
                        switch (payBO.getCashMode()) {
                            case StandardListMasterConstants.CASH:
                                tempStr = getResources().getString(R.string.cash);
                                break;
                            case StandardListMasterConstants.CHEQUE:
                                tempStr = getResources().getString(R.string.cheque);
                                break;
                            case StandardListMasterConstants.DEMAND_DRAFT:
                                tempStr = "DD";
                                break;
                            case StandardListMasterConstants.RTGS:
                                tempStr = getResources().getString(R.string.rtgs);
                                break;
                            case StandardListMasterConstants.MOBILE_PAYMENT:
                                tempStr = "Mob.Pay";
                                break;
                        }
                    }

                    sb.append(doPrintAddSpace(0, 6));
                    sb.append(doPrintFormatingLeft(tempStr, 10));
                    sb.append(doPrintFormatingLeft(payBO.getChequeDate() + "", 14));


                    if (!payBO.getCashMode().equals(StandardListMasterConstants.CREDIT_NOTE) && !payBO.getCashMode().equals(StandardListMasterConstants.ADVANCE_PAYMENT))
                        tempStr = "" + payBO.getChequeNumber();
                    else {
                        if (payBO.getReferenceNumber().contains("AP"))
                            tempStr = "" + (payBO.getReferenceNumber().replace(" ", ""));
                        else
                            tempStr = "" + payBO.getReferenceNumber();
                    }
                    sb.append(doPrintFormatingLeft(tempStr, 9));
                    sb.append(doPrintFormatingLeft(String.format("%10s", bmodel.formatBasedOnCurrency(payBO.getAmount())), 12));
                    sb.append(LineFeed(1));

                    if (!payBO.getCashMode().equals(StandardListMasterConstants.CREDIT_NOTE))
                        total += payBO.getAmount();
                    totalDiscount += payBO.getAppliedDiscountAmount();

                }
                for (int i = 0; i < 40; i++) {
                    sb.append("-");
                }
                sb.append(LineFeed(1));

                sb.append(doPrintFormatingLeft("Discount ", 12));
                sb.append(doPrintFormatingLeft(bmodel.formatBasedOnCurrency(totalDiscount), 10));
                sb.append(doPrintAddSpace(0, 7));
                sb.append(doPrintFormatingLeft("Total ", 7));
                sb.append(String.format("%14s", bmodel.formatBasedOnCurrency(total)));
                sb.append(LineFeed(1));

                for (int i = 0; i < 40; i++) {
                    sb.append("-");
                }
                sb.append(LineFeed(1));

                sb.append(LineFeed(1));
                sb.append(doPrintFormatingLeft("Comments: --------------------------------------------", 44));
                sb.append(LineFeed(2));
                sb.append(doPrintFormatingLeft("Signature: ---------------------------------------------", 44));
                sb.append(LineFeed(2));

                return sb.toString();
            }

        } catch (Exception e) {
            Commons.printException(e);
        }
        return sb.toString();
    }


    public String print2inchDataforCollectionReport() {
        StringBuilder sb = new StringBuilder();
        ArrayList<PaymentBO> paymentList = collectionHelper.getPaymentData(collectionHelper.collectionGroupId);
        try {
            if (paymentList.size() > 0) {
                int center = 0;
                String tempStr;
                if (bmodel.configurationMasterHelper.SHOW_PRINT_HEADERS) {
                    tempStr = "Unipal General Trading Company";
                    if (tempStr.length() < 37) {
                        center = (37 - tempStr.length()) / 2;
                    }

                    sb.append(doPrintAddSpace(0, center));
                    sb.append(doPrintFormatingRight(tempStr, 37));
                    sb.append(LineFeed(1));

                    center = 0;
                    tempStr = "VAT No : 562414227";
                    if (tempStr.length() < 37) {
                        center = (37 - tempStr.length()) / 2;
                    }

                    sb.append(doPrintAddSpace(0, center));
                    sb.append(doPrintFormatingRight(tempStr, 37));
                    sb.append(LineFeed(1));

                    center = 0;
                    tempStr = "Ramallah - Industrial zone, Tel: +972 2 2981060";
                    if (tempStr.length() < 37) {
                        center = (37 - tempStr.length()) / 2;
                    }

                    sb.append(doPrintAddSpace(0, center));
                    sb.append(doPrintFormatingRight(tempStr, 37));
                    sb.append(LineFeed(1));

                    center = 0;
                    tempStr = "Gaza - lndus. Zone - Carny, Tel: +972 7 2830324";
                    if (tempStr.length() < 37) {
                        center = (37 - tempStr.length()) / 2;
                    }

                    sb.append(doPrintAddSpace(0, center));
                    sb.append(doPrintFormatingRight(tempStr, 37));
                    sb.append(LineFeed(1));
                } else {
                    if (bmodel.userMasterHelper.getUserMasterBO().getDistributorName() != null) {
                        if (bmodel.userMasterHelper.getUserMasterBO().getDistributorName().length() < 37) {
                            center = (37 - bmodel.userMasterHelper.getUserMasterBO().getDistributorName().length()) / 2;
                        }

                        sb.append(doPrintAddSpace(0, center));
                        sb.append(doPrintFormatingRight(bmodel.userMasterHelper.getUserMasterBO().getDistributorName(), 37));
                        sb.append(LineFeed(1));

                        center = 0;
                    }
                    if (bmodel.userMasterHelper.getUserMasterBO().getDistributorTinNumber() != null) {
                        if (bmodel.userMasterHelper.getUserMasterBO().getDistributorTinNumber().length() < 37) {
                            center = (37 - bmodel.userMasterHelper.getUserMasterBO().getDistributorTinNumber().length()) / 2;
                        }

                        sb.append(doPrintAddSpace(0, center));
                        sb.append(doPrintFormatingRight(bmodel.userMasterHelper.getUserMasterBO().getDistributorTinNumber(), 37));
                        sb.append(LineFeed(1));

                        center = 0;
                    }
                    if (bmodel.userMasterHelper.getUserMasterBO().getDistributorAddress1() != null) {
                        if (bmodel.userMasterHelper.getUserMasterBO().getDistributorAddress1().length() < 37) {
                            center = (37 - bmodel.userMasterHelper.getUserMasterBO().getDistributorAddress1().length()) / 2;
                        }

                        sb.append(doPrintAddSpace(0, center));
                        sb.append(doPrintFormatingRight(bmodel.userMasterHelper.getUserMasterBO().getDistributorAddress1(), 37));
                        sb.append(LineFeed(1));

                        center = 0;
                    }
                    if (bmodel.userMasterHelper.getUserMasterBO().getDistributorAddress2() != null) {
                        if (bmodel.userMasterHelper.getUserMasterBO().getDistributorAddress2().length() < 37) {
                            center = (37 - bmodel.userMasterHelper.getUserMasterBO().getDistributorAddress2().length()) / 2;
                        }

                        sb.append(doPrintAddSpace(0, center));
                        sb.append(doPrintFormatingRight(bmodel.userMasterHelper.getUserMasterBO().getDistributorAddress2(), 37));
                        sb.append(LineFeed(1));
                    }
                }

                for (int i = 0; i < 36; i++) {
                    sb.append("-");
                }
                sb.append(LineFeed(1));

                double total;
                PaymentBO payHeaderBO = paymentList.get(0);
                total = 0;

                if (payHeaderBO.getAdvancePaymentId() != null) {
                    tempStr = getResources().getString(R.string.rcpt_date) + ":" + payHeaderBO.getAdvancePaymentDate();
                } else {
                    tempStr = getResources().getString(R.string.rcpt_date) + ":" + payHeaderBO.getCollectionDateTime();
                }
                sb.append(doPrintFormatingLeft(tempStr, 30));
                sb.append(" ");
                sb.append(LineFeed(1));

                tempStr = getResources().getString(R.string.rcpt_no) + "  :" + collectionHelper.collectionGroupId.replaceAll("\'", "");
                sb.append(doPrintFormatingLeft(tempStr, 30));
                sb.append(" ");
                sb.append(LineFeed(1));

                tempStr = getResources().getString(R.string.seller_code);
                sb.append(doPrintFormatingLeft(tempStr, 12));
                sb.append(" ");
                sb.append(LineFeed(1));
                tempStr = bmodel.userMasterHelper.getUserMasterBO().getUserCode();
                sb.append(doPrintFormatingLeft(tempStr, 10));
                sb.append(" ");
                sb.append(LineFeed(1));


                tempStr = getResources().getString(R.string.musername);
                sb.append(doPrintFormatingLeft(tempStr, 12));
                sb.append(" ");
                sb.append(LineFeed(1));
                tempStr = bmodel.userMasterHelper.getUserMasterBO().getUserName();
                sb.append(doPrintFormatingLeft(tempStr, 23));
                sb.append(" ");
                sb.append(LineFeed(1));

                if (payHeaderBO.getRetailerName().length() > 30) {
                    tempStr = payHeaderBO.getRetailerName().substring(0, 30);
                } else {
                    tempStr = payHeaderBO.getRetailerName();
                }

                sb.append(doPrintFormatingLeft(getResources().getString(R.string.cust_name) + tempStr, 30));
                sb.append(" ");
                sb.append(LineFeed(1));

                tempStr = getResources().getString(R.string.cust_code) + payHeaderBO.getRetailerCode();
                sb.append(doPrintFormatingLeft(tempStr, 30));
                sb.append(" ");
                sb.append(LineFeed(1));

                for (int i = 0; i < 37; i++) {
                    sb.append("-");
                }
                sb.append(LineFeed(1));

                sb.append(doPrintFormatingLeft(getResources().getString(R.string.invno), 10));
                sb.append(LineFeed(1));

                //sb.append(doPrintAddSpace(0, 3));
                sb.append(doPrintFormatingLeft(getResources().getString(R.string.type), 7));
                sb.append(doPrintAddSpace(0, 1));
                sb.append(doPrintFormatingLeft(getResources().getString(R.string.date_label), 10));
                sb.append(doPrintAddSpace(0, 1));
                sb.append(doPrintFormatingLeft(getResources().getString(R.string.ref_no), 8));
                sb.append(doPrintFormatingLeft(String.format("%10s", getResources().getString(R.string.total)), 10));
                sb.append(LineFeed(1));

                for (int i = 0; i < 36; i++) {
                    sb.append("-");
                }
                sb.append(LineFeed(1));

                double totalDiscount = 0;
                for (PaymentBO payBO : paymentList) {

                    tempStr = payBO.getBillNumber() != null ? payBO.getBillNumber() : getResources().getString(R.string.advance_payment);
                    sb.append(doPrintFormatingLeft(tempStr, 36));
                    sb.append(LineFeed(1));

                    if (payBO.getCashMode().equals(StandardListMasterConstants.CREDIT_NOTE)) {
                        if (payBO.getReferenceNumber().startsWith("AP")) {
                            tempStr = getResources().getString(R.string.advance_payment);
                        } else {
                            tempStr = getResources().getString(R.string.credit_note);
                        }
                    } else {
                        switch (payBO.getCashMode()) {
                            case StandardListMasterConstants.CASH:
                                tempStr = getResources().getString(R.string.cash);
                                break;
                            case StandardListMasterConstants.CHEQUE:
                                tempStr = getResources().getString(R.string.cheque);
                                break;
                            case StandardListMasterConstants.DEMAND_DRAFT:
                                tempStr = "DD";
                                break;
                            case StandardListMasterConstants.RTGS:
                                tempStr = getResources().getString(R.string.rtgs);
                                break;
                            case StandardListMasterConstants.MOBILE_PAYMENT:
                                tempStr = "Mob.Pay";
                                break;
                        }
                    }

                    //sb.append(doPrintAddSpace(0, 4));
                    sb.append(doPrintFormatingLeft(tempStr, 7));
                    sb.append(doPrintAddSpace(0, 1));
                    sb.append(doPrintFormatingLeft(payBO.getChequeDate() + "", 10));
                    sb.append(doPrintAddSpace(0, 1));

                    if (!payBO.getCashMode().equals(StandardListMasterConstants.CREDIT_NOTE) && !payBO.getCashMode().equals(StandardListMasterConstants.ADVANCE_PAYMENT))
                        tempStr = "" + payBO.getChequeNumber();
                    else {
                        if (payBO.getReferenceNumber().contains("AP"))
                            tempStr = "" + (payBO.getReferenceNumber().replace(" ", ""));
                        else
                            tempStr = "" + payBO.getReferenceNumber();
                    }
                    sb.append(doPrintFormatingLeft(tempStr, 9));
                    sb.append(doPrintFormatingLeft(String.format("%10s", bmodel.formatBasedOnCurrency(payBO.getAmount())), 10));
                    sb.append(LineFeed(1));

                    if (!payBO.getCashMode().equals(StandardListMasterConstants.CREDIT_NOTE))
                        total += payBO.getAmount();
                    totalDiscount += payBO.getAppliedDiscountAmount();

                }
                for (int i = 0; i < 36; i++) {
                    sb.append("-");
                }
                sb.append(LineFeed(1));

                if (totalDiscount > 0) {
                    sb.append(doPrintFormatingLeft(getResources().getString(R.string.discount) + " ", 10));
                    sb.append(doPrintAddSpace(0, 17));
                    sb.append(doPrintFormatingRight(bmodel.formatBasedOnCurrency(totalDiscount), 13));
                    sb.append(LineFeed(1));
                }
                sb.append(doPrintFormatingLeft(getResources().getString(R.string.total) + " ", 7));
                sb.append(doPrintAddSpace(0, 15));
                sb.append(doPrintFormatingRight(String.format("%13s", bmodel.formatBasedOnCurrency(total) + " MXN"), 13));
                sb.append(LineFeed(1));

                for (int i = 0; i < 36; i++) {
                    sb.append("-");
                }
                sb.append(LineFeed(1));

                sb.append(LineFeed(1));
                sb.append(doPrintFormatingLeft(getResources().getString(R.string.comments) + ": ----------------------------------------------", 36));
                sb.append(LineFeed(2));
                sb.append(doPrintFormatingLeft(getResources().getString(R.string.customer_sign) + ": ---------------------------------------------", 36));
                sb.append(LineFeed(5));

                return sb.toString();
            }

        } catch (Exception e) {
            Commons.printException(e);
        }
        return sb.toString();
    }

    @Override
    public void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }

    private void showProgressDialog() {
        if (alertDialog == null) {
            AlertDialog.Builder build = new AlertDialog.Builder(getActivity());
            customProgressDialog(build, getResources().getString(R.string.saving));
            alertDialog = build.create();
        }
        alertDialog.show();
    }

    private void dismissProgressDialog() {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }
}
