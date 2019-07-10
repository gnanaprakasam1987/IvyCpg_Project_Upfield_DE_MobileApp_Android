package com.ivy.cpg.view.collection;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.PaymentBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.UpdatePaymentByDateInterface;
import com.ivy.sd.png.model.UpdatePaymentsInterface;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;

public class CashModeFragment extends IvyBaseFragment implements UpdatePaymentsInterface, View.OnClickListener {
    private static ArrayList<PaymentBO> mPaymentList;
    private PaymentBO mPaymentBO;
    private TextView mTotalTV, mEnteredAmountTitleTV, mRefNoTitleTV;
    private EditText mCollectAmtET;
    private LinearLayout mPaymentNoLL;
    private BusinessModel bmodel;
    private UpdatePaymentByDateInterface mUpdatePaymentInterface;
    private EditText mPaymentNoET;
    private boolean isAdvancePaymentAvailable;
    private int mCashModePos;
    private EditText QUANTITY;
    private String append = "";
    private InputMethodManager inputManager;
    private Button applyBtn, cancelBTn;
    private double tempPaidAmt = 0.0;
    private CollectionHelper collectionHelper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        collectionHelper = CollectionHelper.getInstance(getActivity());

        mUpdatePaymentInterface = (UpdatePaymentByDateInterface) getActivity();

        mCashModePos = getArguments().getInt("position", 0);
        isAdvancePaymentAvailable = getArguments().getBoolean("IsAdvancePaymentAvailable", false);

        mPaymentList = collectionHelper.getCollectionPaymentList();
        mPaymentBO = mPaymentList.get(mCashModePos);
        tempPaidAmt = mPaymentBO.getAmount();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_cash_mode, container, false);

        setHasOptionsMenu(true);
        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayShowTitleEnabled(false);
        }
        if (mPaymentBO != null) {
            setScreenTitle(mPaymentBO.getListName());
        }

        collectionHelper.clearPaymentObjects(mPaymentBO);
        inputManager = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);

        mTotalTV = rootView.findViewById(R.id.tv_total_amount);

        mEnteredAmountTitleTV = rootView.findViewById(R.id.tv_enteramount_title);
        mCollectAmtET = rootView.findViewById(R.id.edit_amount);

        mCollectAmtET.requestFocus();
        QUANTITY = mCollectAmtET;

        applyBtn = rootView.findViewById(R.id.applybtn);
        applyBtn.setTypeface(FontUtils.getFontBalooHai(getActivity(), FontUtils.FontType.REGULAR));
        applyBtn.setOnClickListener(this);

        cancelBTn = rootView.findViewById(R.id.cancelbtn);
        cancelBTn.setTypeface(FontUtils.getFontBalooHai(getActivity(), FontUtils.FontType.REGULAR));
        cancelBTn.setOnClickListener(this);

        mPaymentNoLL = rootView.findViewById(R.id.ll3);
        mRefNoTitleTV = rootView.findViewById(R.id.tv_refno_title);
        mPaymentNoET = rootView.findViewById(R.id.edit_refno);

        if (StandardListMasterConstants.CASH.equals(mPaymentBO.getCashMode())) {
            mPaymentNoLL.setVisibility(View.GONE);
        } else if (StandardListMasterConstants.MOBILE_PAYMENT.equals(mPaymentBO.getCashMode())) {
            mPaymentNoLL.setVisibility(View.VISIBLE);
            mPaymentNoET.setText(mPaymentBO.getChequeNumber());
        }

        mUpdatePaymentInterface.updatePaymentDetails(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL));
        mPaymentNoET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mPaymentBO.setChequeNumber(s.toString());

            }
        });

        mCollectAmtET.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                QUANTITY = mCollectAmtET;

                int inType = mCollectAmtET
                        .getInputType();
                mCollectAmtET
                        .setInputType(InputType.TYPE_NULL);
                mCollectAmtET.onTouchEvent(event);
                mCollectAmtET.setInputType(inType);
                //mCollectAmtET.selectAll();
                mCollectAmtET.requestFocus();
                inputManager.hideSoftInputFromWindow(
                        mCollectAmtET
                                .getWindowToken(), 0);
                return true;
            }
        });
        mCollectAmtET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String qty = s.toString();
                if (SDUtil.isValidDecimal(qty, 16, 2)) {
                    double value = 0;
                    if (!"".equals(qty)) {
                        value = SDUtil.convertToDouble(qty);
                    }
                    mPaymentBO.setAmount(value);
                    mPaymentBO.setUpdatePayableamt(value);
                    if (value > 0 && isAdvancePaymentAvailable && !collectionHelper.isUseAllAdvancePaymentAmt()) {
                        if (!qty.contains("."))
                            qty = qty.length() > 1 ? qty.substring(0,
                                    qty.length() - 1) : "0";
                        else
                            qty = "";

                        mCollectAmtET.setText(SDUtil.getWithoutExponential(SDUtil.convertToDouble(qty)));
                        Toast.makeText(getActivity(), getResources().getString(R.string.please_user_advancepayment),
                                Toast.LENGTH_SHORT).show();
                    } else if (!collectionHelper.isEnterAmountExceed(mPaymentList, StandardListMasterConstants.CASH)) {
                        updateTotalAmountEntered();
                    } else {
                        if (value > 0) {
                            if (!qty.contains("."))
                                qty = qty.length() > 1 ? qty.substring(0,
                                        qty.length() - 1) : "0";
                            else
                                qty = "";

                            mCollectAmtET.setText(SDUtil.getWithoutExponential(SDUtil.convertToDouble(qty)));
                            Toast.makeText(
                                    getActivity(),
                                    getResources()
                                            .getString(R.string.amount_exeeds_the_balance_please_check),
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                    mCollectAmtET.setSelection(mCollectAmtET.getText().length());
                } else {
                    qty = qty.length() > 1 ? qty
                            .substring(0, qty.length() - 1) : "0";
                    mCollectAmtET.setText(SDUtil.getWithoutExponential(SDUtil.convertToDouble(qty)));
                }
            }
        });

        rootView.findViewById(R.id.calcdot).setVisibility(View.VISIBLE);
        updateFragments(mCashModePos);
        return rootView;
    }


    private void updateTotalAmountEntered() {
        double totalCollectepayment = 0;
        if (mPaymentList != null && mPaymentList.size() > 0) {
            for (PaymentBO paymentBO : mPaymentList) {
                totalCollectepayment = totalCollectepayment + paymentBO.getAmount();
            }
        }

        mTotalTV.setText(bmodel.formatValue(totalCollectepayment));
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void updatePaymentDetails() {
        updateTotalAmountEntered();
    }

    public void updateUI(PaymentBO paymentBO) {
        mPaymentBO = paymentBO;
        String strAmt = mPaymentBO.getAmount() + "";
        if ("0.0".equals(strAmt))
            strAmt = "0";
        mCollectAmtET.setText(SDUtil.getWithoutExponential(SDUtil.convertToDouble(strAmt)));
        mCollectAmtET.setSelection(mCollectAmtET.getText().length());
        CollectionFragmentNew.CaseMODE caseMODE = CollectionFragmentNew.CaseMODE.valueOf(mPaymentBO.getCashMode());
        switch (caseMODE) {
            case CA:
                mPaymentNoLL.setVisibility(View.GONE);
                break;
            case CM:
                mPaymentNoLL.setVisibility(View.VISIBLE);
                mPaymentNoET.setText(mPaymentBO.getChequeNumber());
                break;
            case CD:
                mPaymentNoLL.setVisibility(View.GONE);
                mEnteredAmountTitleTV.setText(getResources().getString(R.string.discount));
                break;
            case CP:
                mPaymentNoLL.setVisibility(View.VISIBLE);
                mEnteredAmountTitleTV.setText(getResources().getString(R.string.coupon_amount));
                mRefNoTitleTV.setText(getResources().getString(R.string.no_of_coupon));
                mPaymentNoET.setInputType(InputType.TYPE_CLASS_NUMBER);
                break;
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
            mPaymentBO.setAmount(tempPaidAmt);
            mPaymentBO.setUpdatePayableamt(tempPaidAmt);
            getActivity().finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateFragments(int position) {
        PaymentBO paymentBO = mPaymentList.get(position);
        CollectionFragmentNew.CaseMODE mode = CollectionFragmentNew.CaseMODE.valueOf(paymentBO.getCashMode());
        switch (mode) {
            case CA:
                updatePaymentDetails();
                updateUI(paymentBO);
                break;

            case CM:

                updatePaymentDetails();
                updateUI(paymentBO);

                break;

            case CP:

                updatePaymentDetails();
                updateUI(paymentBO);

                break;
            case CD:

                updatePaymentDetails();
                updateUI(paymentBO);

                break;

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
                        QUANTITY.setText(s + ".");// QUANTITY.append(".");
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
        if (!"0".equals(s)) {
            String strQuantity = QUANTITY.getText() + append;
            QUANTITY.setText(strQuantity);
        } else
            QUANTITY.setText(append);
    }

    @Override
    public void onClick(View v) {
        Button vw = (Button) v;

        if (vw == applyBtn) {
            double value = 0;
            String qty = mCollectAmtET.getText().toString();
            if (!"".equals(qty)) {
                value = SDUtil.convertToDouble(qty);
            }
            mPaymentBO.setAmount(value);
            mPaymentBO.setUpdatePayableamt(value);
            updateTotalAmountEntered();
            getActivity().finish();
        } else if (vw == cancelBTn) {
            mPaymentBO.setAmount(tempPaidAmt);
            mPaymentBO.setUpdatePayableamt(tempPaidAmt);
            getActivity().finish();

        }
    }


}
