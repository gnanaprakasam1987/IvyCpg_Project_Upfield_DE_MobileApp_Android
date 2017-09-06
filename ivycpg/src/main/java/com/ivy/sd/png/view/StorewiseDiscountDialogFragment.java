package com.ivy.sd.png.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.lib.DialogFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.StoreWsieDiscountBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

import java.util.ArrayList;

/**
 * Created by rajesh.k on 9/27/2016.
 */
public class StorewiseDiscountDialogFragment extends DialogFragment {
    private BusinessModel bmodel;
    private ArrayList<StoreWsieDiscountBO> mDiscountList;

    private Button mDoneBTN;
    private OnMyDialogResult mDialogResult;
    private TextView mMinRangeTV, mMaxRangeTV, mTitleTv;
    private EditText mDiscountET, QUANTITY;
    private StoreWsieDiscountBO mStorewiseDiscountBO;
    public InputMethodManager inputManager;
    private double mTotalOrderValue,mEnteredDiscAmtOrPercent;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        mDialogResult = (OnMyDialogResult) getActivity();
        mTotalOrderValue = getArguments().getDouble("totalValue", 0);
        mEnteredDiscAmtOrPercent = getArguments().getDouble("enteredDiscAmtOrPercent", 0);


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (getDialog() != null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        getDialog().setCancelable(false);
        this.setCancelable(false);

        View rootView = inflater.inflate(R.layout.fragment_storewise_discount, container, false);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        inputManager = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getView().getWindowToken(), 0);

        if (bmodel.configurationMasterHelper.SHOW_STORE_WISE_DISCOUNT_DLG && bmodel.configurationMasterHelper.BILL_WISE_DISCOUNT == 0) {
            mDiscountList = bmodel.productHelper.getBillWiseDiscountList();
            findDiscout();
            if (mStorewiseDiscountBO == null) {
                getDialog().dismiss();
                return;
            }
        }

        // getDialog().setTitle(mStorewiseDiscountBO.getDescription());
        mMinRangeTV = (TextView) getView().findViewById(R.id.tv_min_range);
        mMaxRangeTV = (TextView) getView().findViewById(R.id.tv_max_range);
        mTitleTv = (TextView) getView().findViewById(R.id.tvTitle);
        mDiscountET = (EditText) getView().findViewById(R.id.edit_discount_value);

        mTitleTv.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
        mDiscountET.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        mMinRangeTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        mMaxRangeTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        if (mStorewiseDiscountBO != null) {

            mTitleTv.setText(mStorewiseDiscountBO.getDescription());
            mMinRangeTV.setText("Minimum Range  : " + mStorewiseDiscountBO.getDiscount() + "");
            mMaxRangeTV.setText("Maximum Range  : " + mStorewiseDiscountBO.getToDiscount() + "");
            mDiscountET.setText(mStorewiseDiscountBO.getAppliedDiscount() + "");
        } else if (bmodel.configurationMasterHelper.SHOW_TOTAL_DISCOUNT_EDITTEXT) {
            mTitleTv.setText(getResources().getString(R.string.title_entry_discount));
            mMinRangeTV.setVisibility(View.GONE);
            mMaxRangeTV.setText("Discount  :");
            String strDiscCnt = mEnteredDiscAmtOrPercent + "";
            mDiscountET.setText(strDiscCnt);
        }
        ((Button) getView().findViewById(R.id.calcdot)).setVisibility(View.VISIBLE);
        mDiscountET.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                QUANTITY = mDiscountET;
                int inType = mDiscountET.getInputType();
                mDiscountET.setInputType(InputType.TYPE_NULL);
                mDiscountET.onTouchEvent(event);
                mDiscountET.setInputType(inType);
                mDiscountET.selectAll();
                inputManager.hideSoftInputFromWindow(
                        QUANTITY.getWindowToken(), 0);
                mDiscountET.requestFocus();


                return true;
            }
        });
        mDiscountET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String qty = s.toString();
                if (!qty.equals("")) {
                    if (mStorewiseDiscountBO != null) {
                        double discValue = Double.parseDouble(qty);
                        mStorewiseDiscountBO.setAppliedDiscount(discValue);
                    }

                } else {
                    mDiscountET.setText(0 + "");
                }
            }


        });


        mDoneBTN = (Button) getView().findViewById(R.id.btn_done);
        mDoneBTN.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        mDoneBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mStorewiseDiscountBO != null) {
                    if (isValidate()) {
                        mDialogResult.onDiscountDismiss(String.valueOf(mStorewiseDiscountBO.getAppliedDiscount()), mStorewiseDiscountBO.getIsPercentage(), mStorewiseDiscountBO.getDiscountId(), mStorewiseDiscountBO.getIsCompanyGiven());
                        getDialog().dismiss();
                    } else {
                        Toast.makeText(
                                getActivity(),
                                "Please enter value between range",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    mDialogResult.onDiscountDismiss(mDiscountET.getText().toString(), 0, 0, 0);
                    getDialog().dismiss();
                }
            }
        });
    }


    public interface OnMyDialogResult {
        void onDiscountDismiss(String result, int result1, int result3, int result4);

        void cancel();
    }

    public void numberPressed(View vw) {


        if (QUANTITY == null) {
            Toast.makeText(getActivity(), getResources().getString(R.string.please_select_item), Toast.LENGTH_SHORT).show();
        } else {
            int id = vw.getId();
            if (id == R.id.calcdel) {

                String enterText = (String) QUANTITY.getText().toString();
                if (enterText.contains(".")) {
                    String[] splitValue = enterText.split("\\.");
                    try {

                        int s = SDUtil.convertToInt(splitValue[1]);
                        if (s == 0) {
                            s = SDUtil.convertToInt(splitValue[0]);
                            QUANTITY.setText(s + "");
                        } else {
                            s = s / 10;

                            QUANTITY.setText(splitValue[0] + "." + s);
                        }


                    } catch (ArrayIndexOutOfBoundsException e) {
                        QUANTITY.setText(SDUtil.convertToInt(enterText) + "");
                    }


                } else {

                    int s = SDUtil.convertToInt((String) QUANTITY.getText()
                            .toString());
                    s = s / 10;
                    QUANTITY.setText(s + "");

                }
            } else if (id == R.id.calcdot) {
                String s = QUANTITY.getText().toString();

                if (s != null) {
                    if (!s.contains(".")) {
                        QUANTITY.setText(s + ".");// QUANTITY.append(".");
                    }
                }

            } else {
                Button ed = (Button) getDialog().findViewById(vw.getId());
                String append = ed.getText().toString();
                eff(append);

            }

        }
    }

    public void eff(String append) {
        String s = (String) QUANTITY.getText().toString();
        if (!s.equals("0") && !s.equals("0.0")) {
            QUANTITY.setText(QUANTITY.getText() + append);
        } else
            QUANTITY.setText(append);
    }

    private void findDiscout() {
        if (mDiscountList != null) {
            for (StoreWsieDiscountBO storeWsieDiscountBO : mDiscountList) {
                if (mTotalOrderValue >= storeWsieDiscountBO.getMinAmount() && mTotalOrderValue <= storeWsieDiscountBO.getMaxAmount()) {
                    mStorewiseDiscountBO = storeWsieDiscountBO;
                    mStorewiseDiscountBO.setApplied(true);
                    break;
                }
            }
        }
    }

    private boolean isValidate() {
        double enteredeValue = Double.parseDouble(mDiscountET.getText().toString());
        if (enteredeValue < mStorewiseDiscountBO.getDiscount() || enteredeValue > mStorewiseDiscountBO.getToDiscount()) {
            return false;
        }
        return true;
    }


}
