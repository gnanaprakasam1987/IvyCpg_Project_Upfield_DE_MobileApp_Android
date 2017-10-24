package com.ivy.sd.png.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.CreditNoteListBO;
import com.ivy.sd.png.bo.InvoiceHeaderBO;
import com.ivy.sd.png.bo.PaymentBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.UpdatePaymentByDateInterface;
import com.ivy.sd.png.model.UpdatePaymentsInterface;
import com.ivy.sd.png.util.StandardListMasterConstants;

import java.util.ArrayList;

public class AdvancePaymentFragment extends IvyBaseFragment implements UpdatePaymentsInterface{

    private static ArrayList<PaymentBO> mPaymentList;

    private  PaymentBO mPaymentBO;
    private BusinessModel bmodel;
    private UpdatePaymentByDateInterface mUpdatePaymentInterface;
    private ArrayList<CreditNoteListBO> mCreditNoteList;
    private TextView mTotalTV;
    private EditText mEnterCreditNoteAmtET;

    private View rootView;
    private double mTotalCreditNoteValue=0;
    private CustomKeyBoard dialogCustomKeyBoard;
    private double tempCreditNoteValue=0;
    private boolean isFragmentAlreadyCreated=false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_creditnote, container, false);
        setHasOptionsMenu(true);
        if (getActivity().getActionBar() != null) {
            // getActionBar().setIcon(R.drawable.icon_stock);
            getActivity().getActionBar().setDisplayShowTitleEnabled(false);
        }
        if (mPaymentBO != null) {
            setScreenTitle(mPaymentBO.getListName());
        }
        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        mUpdatePaymentInterface = (UpdatePaymentByDateInterface) getActivity();

        mPaymentList=bmodel.collectionHelper.getCollectionPaymentList();

        final int creditNotePos=getArguments().getInt("position",0);
        mPaymentBO=mPaymentList.get(creditNotePos);
    }

    @Override
    public void onStart() {
        super.onStart();
        isFragmentAlreadyCreated=false;
        ListView creditNoteLV = (ListView) rootView.findViewById(R.id.lv_creditnote);
        mTotalTV=(TextView)rootView.findViewById(R.id.tv_total_amount);

        LinearLayout llEnterCreditNote=(LinearLayout)rootView.findViewById(R.id.ll_enter_creditnote);
        CardView mCardview = (CardView) rootView.findViewById(R.id.ll_cardview);
        LinearLayout hideKeyBtn = (LinearLayout) rootView.findViewById(R.id.bottom_layout);
        hideKeyBtn.setVisibility(View.GONE);
        mEnterCreditNoteAmtET=(EditText)rootView.findViewById(R.id.edit_creditnoteamt);

        mEnterCreditNoteAmtET.setVisibility(View.GONE);
        mCardview.setVisibility(View.GONE);

        if (bmodel.configurationMasterHelper.IS_PARTIAL_CREDIT_NOTE_ALLOW) {
            llEnterCreditNote.setVisibility(View.VISIBLE);
        }

        mUpdatePaymentInterface.updatePaymentDetails(SDUtil.now(SDUtil.DATE_GLOBAL));

        String modeID = bmodel.getStandardListIdAndType(
                "CNAP",
                StandardListMasterConstants.COLLECTION_PAY_TYPE);
        mCreditNoteList = bmodel.collectionHelper.getAdvancePaymentList();
        if (mCreditNoteList != null) {

            for (CreditNoteListBO bo : bmodel.collectionHelper
                    .getCreditNoteList()) {
                if (bo.getRetailerId().equals(
                        bmodel.getRetailerMasterBO().getRetailerID())
                        && !bo.isUsed()&&(modeID.equals(bo.getTypeId())))
                    mCreditNoteList.add(bo);
            }
            if (mCreditNoteList != null && mCreditNoteList.size() > 0) {
                CreditNoteAdapter creditNoteAdapter=new CreditNoteAdapter();
                creditNoteLV.setAdapter(creditNoteAdapter);
            }
        }else{
            mEnterCreditNoteAmtET.setVisibility(View.GONE);
        }
        mEnterCreditNoteAmtET.setEnabled(false);
    }

    @Override
    public void updatePaymentDetails() {
        applyAdvancePayment();
        updateCreditNotePayment();
        updateTotal();
    }

    private class CreditNoteAdapter extends BaseAdapter {

        public CreditNoteListBO getItem(int position) {
            return mCreditNoteList.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return mCreditNoteList.size();
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.row_credit_note, parent, false);
                holder = new ViewHolder();
                holder.refNoTxt = (TextView) row.findViewById(R.id.refNoTxt);
                holder.crdNoteAmtTxt = (TextView) row
                        .findViewById(R.id.crdNoteAmtTxt);
                holder.creditNoteCheckBox = (CheckBox) row
                        .findViewById(R.id.creditNoteCheckBox);
                holder.totCrdNoteAmtTxt=(TextView)row.findViewById(R.id.totcrdNoteAmtTxt);
                holder.creditNoteCheckBox.setVisibility(View.GONE);


                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.creditNoteListBO = mCreditNoteList.get(position);
            holder.refNoTxt.setText(holder.creditNoteListBO.getRefno());
            String strCreditAmt = bmodel.formatValue(holder.creditNoteListBO
                    .getAmount()) + "";
            holder.crdNoteAmtTxt.setText(strCreditAmt);
            holder.creditNoteCheckBox.setChecked(holder.creditNoteListBO.isChecked());
            String totCrdeitAmt=bmodel.formatValue(holder.creditNoteListBO.getAmount()+holder.creditNoteListBO.getAppliedAmount());
            holder.totCrdNoteAmtTxt.setText(totCrdeitAmt);


            updatePaymentDetails();
            return (row);
        }
    }

    class ViewHolder {
        private TextView refNoTxt;
        private TextView crdNoteAmtTxt;
        private TextView totCrdNoteAmtTxt;
        private CheckBox creditNoteCheckBox;
        private CreditNoteListBO creditNoteListBO;
    }

    private void updateCreditNotePayment(){
        mTotalCreditNoteValue=0;
        if(mCreditNoteList!=null&&mCreditNoteList.size()>0) {
            for (CreditNoteListBO creditNoteListBO : mCreditNoteList) {
                if (creditNoteListBO.isChecked()&&creditNoteListBO.getTypeId()!=0) {
                    mTotalCreditNoteValue = mTotalCreditNoteValue + creditNoteListBO.getAmount();
                }
            }
        }

        mPaymentBO.setAmount(mTotalCreditNoteValue);

        if (!bmodel.collectionHelper.isEnterAmountExceed(mPaymentList,StandardListMasterConstants.ADVANCE_PAYMENT)) {
            mPaymentBO.setAmount(mTotalCreditNoteValue);
        } else {
            mTotalCreditNoteValue = bmodel.collectionHelper.getBalanceAmountWithOutCreditNote(mPaymentList,false);
            mPaymentBO.setAmount(mTotalCreditNoteValue);
        }

        String strCreditValue = mTotalCreditNoteValue+"";
        mEnterCreditNoteAmtET.setText(strCreditValue);
    }

    private void updateTotal(){
        double totalCollectepayment=0;
        if(mPaymentList!=null&&mPaymentList.size()>0){
            for(PaymentBO paymentBO:mPaymentList){
                totalCollectepayment=totalCollectepayment+paymentBO.getAmount();
            }
        }

        mTotalTV.setText(bmodel.formatValue(totalCollectepayment));
    }
    private void applyAdvancePayment(){
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        final ArrayList<InvoiceHeaderBO> invoiceList = new ArrayList<>(bmodel.getInvoiceHeaderBO());
        double totalInvoiceAmout=0;

        for(InvoiceHeaderBO invoiceHeaderBO:invoiceList){
            if(invoiceHeaderBO.isChkBoxChecked()){
                totalInvoiceAmout=totalInvoiceAmout+invoiceHeaderBO.getBalance();
            }
        }

        for(CreditNoteListBO creditNoteListBO:mCreditNoteList){
            if(totalInvoiceAmout>0){
                creditNoteListBO.setChecked(true);
                totalInvoiceAmout=totalInvoiceAmout-creditNoteListBO.getAmount();

            }else{
                creditNoteListBO.setChecked(false);
            }
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
}

