package com.ivy.cpg.view.collection;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.CreditNoteListBO;
import com.ivy.sd.png.bo.PaymentBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.UpdatePaymentByDateInterface;
import com.ivy.sd.png.model.UpdatePaymentsInterface;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.utils.DateTimeUtils;

import java.util.ArrayList;

public class CreditNoteFragment extends IvyBaseFragment implements UpdatePaymentsInterface, View.OnClickListener {

    private static ArrayList<PaymentBO> mPaymentList;

    private PaymentBO mPaymentBO;
    private BusinessModel bmodel;
    private UpdatePaymentByDateInterface mUpdatePaymentInterface;
    private ArrayList<CreditNoteListBO> mCreditNoteList;
    private TextView mTotalTV;
    private EditText mEnterCreditNoteAmtET;
    private double mTotalCreditNoteValue = 0;
    private double tempCreditNoteValue = 0;
    private boolean isFragmentAlreadyCreated = false;
    private boolean isAdvancePaymentAvailable;
    private Button applyBtn, cancelBtn;
    private EditText QUANTITY;
    private String append = "";
    private InputMethodManager inputManager;
    private double preCollectionValue, currentCollectionValue;
    private boolean isFromCollection = false;
    private CollectionHelper collectionHelper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        collectionHelper = CollectionHelper.getInstance(getActivity());
        inputManager = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);

        mUpdatePaymentInterface = (UpdatePaymentByDateInterface) getActivity();


        final int creditNotePos = getArguments().getInt("position", 0);
        isAdvancePaymentAvailable = getArguments().getBoolean("IsAdvancePaymentAvailable", false);
        isFromCollection = getArguments().getBoolean("FromCollection", false);

        mPaymentList = collectionHelper.getCollectionPaymentList();
        mPaymentBO = mPaymentList.get(creditNotePos);
        preCollectionValue = mPaymentBO.getAmount();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_creditnote, container, false);
        setHasOptionsMenu(true);

        rootView.findViewById(R.id.calcdot).setVisibility(View.VISIBLE);
        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayShowTitleEnabled(false);
        }
        if (mPaymentBO != null) {
            setScreenTitle(mPaymentBO.getListName());
        }

        applyBtn = rootView.findViewById(R.id.applybtn);
        applyBtn.setOnClickListener(this);
        cancelBtn = rootView.findViewById(R.id.cancelbtn);
        cancelBtn.setOnClickListener(this);

        if (isFromCollection)
            cancelBtn.setText(getString(R.string.skip));
        else
            cancelBtn.setText(getString(R.string.cancel));

        isFragmentAlreadyCreated = false;
        ListView creditNoteLV = rootView.findViewById(R.id.lv_creditnote);
        mTotalTV = rootView.findViewById(R.id.tv_total_amount);
        LinearLayout llEnterCreditNote = rootView.findViewById(R.id.ll_enter_creditnote);
        mEnterCreditNoteAmtET = rootView.findViewById(R.id.edit_creditnoteamt);
        LinearLayout numKeyLayout = rootView.findViewById(R.id.ll_keypad);
        CardView cardViewLayout = rootView.findViewById(R.id.ll_cardview);

        if (bmodel.configurationMasterHelper.IS_PARTIAL_CREDIT_NOTE_ALLOW) {
            llEnterCreditNote.setVisibility(View.VISIBLE);
            numKeyLayout.setVisibility(View.VISIBLE);
            cardViewLayout.setVisibility(View.VISIBLE);
        } else {
            llEnterCreditNote.setVisibility(View.GONE);
            numKeyLayout.setVisibility(View.GONE);
            cardViewLayout.setVisibility(View.GONE);
        }
        mUpdatePaymentInterface.updatePaymentDetails(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL));
        tempCreditNoteValue = mPaymentBO.getAmount();
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
            if (mCreditNoteList != null && mCreditNoteList.size() > 0) {
                CreditNoteAdapter creditNoteAdapter = new CreditNoteAdapter();
                creditNoteLV.setAdapter(creditNoteAdapter);
            }
        } else {
            mEnterCreditNoteAmtET.setVisibility(View.GONE);
            numKeyLayout.setVisibility(View.GONE);

        }

        mEnterCreditNoteAmtET.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                QUANTITY = mEnterCreditNoteAmtET;

                int inType = mEnterCreditNoteAmtET
                        .getInputType();
                mEnterCreditNoteAmtET
                        .setInputType(InputType.TYPE_NULL);
                mEnterCreditNoteAmtET.onTouchEvent(event);
                mEnterCreditNoteAmtET.setInputType(inType);
                mEnterCreditNoteAmtET.selectAll();
                mEnterCreditNoteAmtET.requestFocus();
                inputManager.hideSoftInputFromWindow(
                        mEnterCreditNoteAmtET
                                .getWindowToken(), 0);
                return false;
            }
        });
        mEnterCreditNoteAmtET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String qty = s.toString();
                double value = 0;
                if (SDUtil.isValidDecimal(qty, 16, 2)) {
                    if (!qty.equals("")) {
                        value = SDUtil.convertToDouble(qty);
                    }

                    if (mTotalCreditNoteValue >= value) {
                        if (tempCreditNoteValue > 0 && tempCreditNoteValue != value) {
                            mPaymentBO.setAmount(tempCreditNoteValue);
                            currentCollectionValue = tempCreditNoteValue;

                            String strCreditNote = tempCreditNoteValue + "";
                            mEnterCreditNoteAmtET.setText(strCreditNote);
                            if (isFragmentAlreadyCreated)
                                tempCreditNoteValue = 0;
                        } else {
                            mPaymentBO.setAmount(value);
                            currentCollectionValue = value;
                        }
                    } else {
                        qty = qty.length() > 1 ? qty.substring(0,
                                qty.length() - 1) : "0";

                        mEnterCreditNoteAmtET.setText(qty);
                        Toast.makeText(
                                getActivity(),
                                getResources()
                                        .getString(
                                                R.string.amount_exeeds_the_balance_please_check),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    mEnterCreditNoteAmtET.setText(qty.length() > 1 ? qty
                            .substring(0, qty.length() - 1) : "0");
                }
            }
        });

        return rootView;
    }


    @Override
    public void updatePaymentDetails() {
        if (isFragmentAlreadyCreated) {
            tempCreditNoteValue = mPaymentBO.getAmount();
        }
        updateCreditNotePayment();
        updateTotal();
    }

    @Override
    public void onClick(View v) {
        Button btn = (Button) v;
        if (btn == applyBtn) {
            mUpdatePaymentInterface.updatePaymentDetails(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL));
            mPaymentBO.setAmount(currentCollectionValue);

            checkFromCollectionScreen();
            getActivity().finish();

        } else if (btn == cancelBtn) {
            mPaymentBO.setAmount(preCollectionValue);
            checkFromCollectionScreen();
            getActivity().finish();
        }

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

                holder.creditNoteIDTxt = (TextView) row.findViewById(R.id.creditNoteIDTxt);
                holder.refNoTxt = row.findViewById(R.id.refNoTxt);
                holder.crdNoteAmtTxt = row
                        .findViewById(R.id.crdNoteAmtTxt);
                holder.creditNoteCheckBox = row
                        .findViewById(R.id.creditNoteCheckBox);
                holder.totCrdNoteAmtTxt = row.findViewById(R.id.totcrdNoteAmtTxt);
                holder.parentLayout = row.findViewById(R.id.parentLayout);

                holder.creditNoteCheckBox
                        .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                            @Override
                            public void onCheckedChanged(
                                    CompoundButton buttonView, boolean isChecked) {
                                if (isFragmentAlreadyCreated)
                                    tempCreditNoteValue = 0;

                                holder.creditNoteListBO.setChecked(isChecked);
                                updateCreditNotePayment();
                                if (isChecked && isAdvancePaymentAvailable && !collectionHelper.isUseAllAdvancePaymentAmt()) {
                                    holder.creditNoteCheckBox.setChecked(false);
                                    holder.creditNoteListBO.setChecked(false);
                                    updateCreditNotePayment();
                                    updateTotal();
                                    Toast.makeText(getActivity(), getResources().getString(R.string.please_user_advancepayment),
                                            Toast.LENGTH_SHORT).show();
                                } else if (bmodel.configurationMasterHelper.IS_PARTIAL_CREDIT_NOTE_ALLOW || !collectionHelper.isEnterAmountExceed(mPaymentList, StandardListMasterConstants.CREDIT_NOTE))
                                    updateTotal();
                                else {

                                    holder.creditNoteCheckBox.setChecked(false);
                                    holder.creditNoteListBO.setChecked(false);
                                    updateCreditNotePayment();
                                    updateTotal();
                                    Toast.makeText(
                                            getActivity(),
                                            getResources()
                                                    .getString(
                                                            R.string.amount_exeeds_the_balance_please_check),
                                            Toast.LENGTH_SHORT).show();
                                }
                                tempCreditNoteValue = 0;
                                isFragmentAlreadyCreated = true;
                            }

                        });

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.creditNoteListBO = mCreditNoteList.get(position);
            holder.creditNoteIDTxt.setText(holder.creditNoteListBO.getId());
            holder.refNoTxt.setText(holder.creditNoteListBO.getRefno());
            String strCreditAmt = bmodel.formatValue(holder.creditNoteListBO
                    .getAmount()) + "";
            holder.crdNoteAmtTxt.setText(strCreditAmt);
            holder.creditNoteCheckBox.setChecked(holder.creditNoteListBO.isChecked());
            String totCrdeitAmt = bmodel.formatValue(holder.creditNoteListBO.getAmount() + holder.creditNoteListBO.getAppliedAmount());
            holder.totCrdNoteAmtTxt.setText(totCrdeitAmt);

            if (position % 2 == 0) {
                holder.parentLayout.setBackgroundColor(getResources().getColor(R.color.list_even_item_bg));
            } else {
                holder.parentLayout.setBackgroundColor(getResources().getColor(R.color.list_odd_item_bg));
            }

            return (row);
        }
    }

    class ViewHolder {
        private TextView creditNoteIDTxt;
        private TextView refNoTxt;
        private TextView crdNoteAmtTxt;
        private TextView totCrdNoteAmtTxt;
        private CheckBox creditNoteCheckBox;
        private CreditNoteListBO creditNoteListBO;
        private LinearLayout parentLayout;
    }

    private void updateCreditNotePayment() {
        mTotalCreditNoteValue = 0;
        if (mCreditNoteList != null && mCreditNoteList.size() > 0) {

            for (CreditNoteListBO creditNoteListBO : mCreditNoteList) {
                if (creditNoteListBO.isChecked()) {
                    mTotalCreditNoteValue = mTotalCreditNoteValue + creditNoteListBO.getAmount();
                }
            }
            if (mTotalCreditNoteValue > 0) {
                mEnterCreditNoteAmtET.setEnabled(true);
            } else {
                mEnterCreditNoteAmtET.setEnabled(false);
            }
        }

        mPaymentBO.setAmount(mTotalCreditNoteValue);
        currentCollectionValue = mTotalCreditNoteValue;

        if (bmodel.configurationMasterHelper.IS_PARTIAL_CREDIT_NOTE_ALLOW) {
            if (tempCreditNoteValue > 0) {
                mPaymentBO.setAmount(tempCreditNoteValue);
                currentCollectionValue = tempCreditNoteValue;
            } else {

                if (!collectionHelper.isEnterAmountExceed(mPaymentList, StandardListMasterConstants.CREDIT_NOTE)) {
                    mPaymentBO.setAmount(mTotalCreditNoteValue);
                    currentCollectionValue = mTotalCreditNoteValue;
                } else {
                    mTotalCreditNoteValue = collectionHelper.getBalanceAmountWithOutCreditNote(mPaymentList, true);
                    mPaymentBO.setAmount(mTotalCreditNoteValue);
                    currentCollectionValue = mTotalCreditNoteValue;
                }
            }
        }
        mTotalCreditNoteValue = SDUtil.convertToDouble(bmodel.formatValue(mTotalCreditNoteValue));

        String strCreditValue = mTotalCreditNoteValue + "";
        mEnterCreditNoteAmtET.setText(strCreditValue);
    }

    private void updateTotal() {
        double totalCollectepayment = 0;
        if (mPaymentList != null && mPaymentList.size() > 0) {
            for (PaymentBO paymentBO : mPaymentList) {
                totalCollectepayment = totalCollectepayment + paymentBO.getAmount();
            }
        }

        mTotalTV.setText(bmodel.formatValue(totalCollectepayment));
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            mPaymentBO.setAmount(preCollectionValue);
            getActivity().finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkFromCollectionScreen() {
        if (isFromCollection) {
            Intent intent = new Intent(getActivity(), BillPaymentActivity.class);
            bmodel.mSelectedActivityName = "Bill Payment";
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            getActivity().overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

        }
    }

    public void numberPressed(View vw) {
        if (QUANTITY == null) {
            bmodel.showAlert(
                    getActivity().getResources().getString(
                            R.string.please_select_item), 0);
        } else {
            int id = vw.getId();
            if (id == R.id.calcdel) {
                String val = QUANTITY.getText().toString();

                if (!val.isEmpty()) {

                    val = val.substring(0, val.length() - 1);

                    if (val.length() == 0) {
                        val = "0";
                    }

                } else {
                    val = "0";
                }

                QUANTITY.setText(val);
            } else if (id == R.id.calcdot) {
                String s = QUANTITY.getText().toString();

                if (s != null) {
                    if (!s.contains(".")) {
                        QUANTITY.setText(s + ".");
                    }
                }
            } else {
                Button ed = getView().findViewById(vw.getId());
                append = ed.getText().toString();
                eff();
            }
        }
    }

    private void eff() {
        String s = QUANTITY.getText().toString();
        if (!"0".equals(s) && !"0.0".equals(s)) {
            String strQuantity = QUANTITY.getText() + append;
            QUANTITY.setText(strQuantity);
        } else
            QUANTITY.setText(append);
    }
}

