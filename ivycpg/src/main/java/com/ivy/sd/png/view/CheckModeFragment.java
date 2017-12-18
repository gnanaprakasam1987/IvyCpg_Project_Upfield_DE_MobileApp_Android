package com.ivy.sd.png.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.BankMasterBO;
import com.ivy.sd.png.bo.BranchMasterBO;
import com.ivy.sd.png.bo.PaymentBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.UpdatePaymentByDateInterface;
import com.ivy.sd.png.model.UpdatePaymentsInterface;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DateUtil;
import com.ivy.sd.png.util.StandardListMasterConstants;

import java.util.ArrayList;
import java.util.Date;

public class CheckModeFragment extends IvyBaseFragment
        implements DataPickerDialogFragment.UpdateDateInterface,
        UpdatePaymentsInterface, View.OnClickListener {

    private BusinessModel bmodel;
    private static ArrayList<PaymentBO> mPaymentList;
    private PaymentBO mPaymentBO;
    private TextView mTotalAmountTV;
    private TextView mChequeNoTitleTV;
    private TextView mChequeDateTitleTV;
    private EditText mCollectAmountET;
    private EditText mChequeNoET;
    private Spinner mBankSpin;
    private Spinner mBranchSpin;
    private ArrayList<BankMasterBO> mBankDetailList;
    private EditText mBankET;
    private EditText mBranchET;

    private ArrayList<BranchMasterBO> mBranchDetailsList;
    private Button mChequeDateBTN;
    private UpdatePaymentByDateInterface mUpdatePaymentDateInterface;
    private CustomKeyBoard dialogCustomKeyBoard;

    private String mImageName;
    private String mImagePath;
    private final int mImageCount = 1;
    private View rootView;
    private boolean isAdvancePaymentAvailabe;

    private EditText QUANTITY;
    private String append = "";
    private InputMethodManager inputManager;
    private Button applyBtn, cancelBTn;
    private boolean isNumberPressed = false;
    private double tempPaidAmt = 0.0;
    private String mErrorMsg = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        inputManager = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);

        mUpdatePaymentDateInterface = (UpdatePaymentByDateInterface) getActivity();

        final int checkModePos = getArguments().getInt("position", 0);
        isAdvancePaymentAvailabe = getArguments().getBoolean("IsAdvancePaymentAvailable", false);

        mPaymentList = bmodel.collectionHelper.getCollectionPaymentList();
        mPaymentBO = mPaymentList.get(checkModePos);
        tempPaidAmt = mPaymentBO.getAmount();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_cheque_mode, container, false);
        setHasOptionsMenu(true);

        rootView.findViewById(R.id.calcdot).setVisibility(View.VISIBLE);
        if (getActivity().getActionBar() != null) {
            // getActionBar().setIcon(R.drawable.icon_stock);
            getActivity().getActionBar().setDisplayShowTitleEnabled(false);
        }
        if (mPaymentBO != null) {
            setScreenTitle(mPaymentBO.getListName());
        }

        applyBtn = (Button) rootView.findViewById(R.id.applybtn);
        applyBtn.setOnClickListener(this);
        applyBtn.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        cancelBTn = (Button) rootView.findViewById(R.id.cancelbtn);
        cancelBTn.setOnClickListener(this);
        applyBtn.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        bmodel.collectionHelper.clearPaymentObjects(mPaymentBO);
        mTotalAmountTV = (TextView) rootView.findViewById(R.id.tv_total_amount);
        mCollectAmountET = (EditText) rootView.findViewById(R.id.edit_collectamt);
        mChequeNoTitleTV = (TextView) rootView.findViewById(R.id.tv_chequeno_title);
        mChequeDateTitleTV = (TextView) rootView.findViewById(R.id.tv_date_title);
        mChequeNoET = (EditText) rootView.findViewById(R.id.edit_chequeno);
        mBankET = (EditText) rootView.findViewById(R.id.edit_bankname);
        mBranchET = (EditText) rootView.findViewById(R.id.edit_branchname);
        if (mPaymentBO.getAmount() > 0) {
            mCollectAmountET.setText(mPaymentBO.getAmount() + "");
            mCollectAmountET.setSelection(mCollectAmountET.getText().length());
        }

        mChequeNoET.setText(mPaymentBO.getChequeNumber());
        if (bmodel.configurationMasterHelper.COLL_CHEQUE_MODE){
            mChequeNoET.setInputType(InputType.TYPE_CLASS_TEXT);
        }

        mBankSpin = (Spinner) rootView.findViewById(R.id.spin_bank);
        ArrayAdapter<BankMasterBO> bankSpinnerAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_bluetext_layout);
        bankSpinnerAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
        BankMasterBO bankMasterBO = new BankMasterBO();
        bankMasterBO.setBankId(-1);
        bankMasterBO.setBankName(getResources().getString(R.string.sel_bank));
        bankSpinnerAdapter.add(bankMasterBO);
        mBankDetailList = bmodel.collectionHelper.getBankMasterBO();
        int size = mBankDetailList.size();
        for (int i = 0; i < size; ++i) {
            BankMasterBO ret = mBankDetailList.get(i);
            bankSpinnerAdapter.add(ret);
        }
        BankMasterBO otherMasterBO = new BankMasterBO();
        otherMasterBO.setBankId(0);
        otherMasterBO.setBankName(getResources().getString(R.string.tab_text_others));
        bankSpinnerAdapter.add(otherMasterBO);
        mBankSpin.setAdapter(bankSpinnerAdapter);

        mBranchSpin = (Spinner) rootView.findViewById(R.id.spin_branch);

        mBranchDetailsList = bmodel.collectionHelper.getBranchMasterBO();

        mBankSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                BankMasterBO bankBO = (BankMasterBO) mBankSpin.getSelectedItem();
                if (bankBO.getBankId() == 0) {
                    ((LinearLayout) rootView.findViewById(R.id.llBranch)).setVisibility(View.GONE);
                    ((LinearLayout) rootView.findViewById(R.id.llbankbranch)).setVisibility(View.VISIBLE);
                    mPaymentBO.setBankID(bankBO.getBankId() + "");
                    mPaymentBO.setBranchId("0");
                    mBankET.setText(mPaymentBO.getBankName());
                    mBranchET.setText(mPaymentBO.getBranchName());

                } else {
                    ((LinearLayout) rootView.findViewById(R.id.llBranch)).setVisibility(View.VISIBLE);
                    ((LinearLayout) rootView.findViewById(R.id.llbankbranch)).setVisibility(View.GONE);
                    mPaymentBO.setBankID(bankBO.getBankId() + "");
                    updateBranchSpiner(bankBO.getBankId() + "");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mBranchSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                BranchMasterBO branchMasterBO = (BranchMasterBO) mBranchSpin.getSelectedItem();
                mPaymentBO.setBranchId(branchMasterBO.getBranchID());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ImageView cameraBTN = (ImageView) rootView.findViewById(R.id.btn_camera);
        mChequeDateBTN = (Button) rootView.findViewById(R.id.btn_datepicker);
        if (mPaymentBO.getChequeDate() != null && !"".equals(mPaymentBO.getChequeDate()))
            mChequeDateBTN.setText(DateUtil.convertFromServerDateToRequestedFormat(
                    mPaymentBO.getChequeDate(), ConfigurationMasterHelper.outDateFormat));
        else {
            String todayDate = SDUtil.now(SDUtil.DATE_GLOBAL);
            mChequeDateBTN.setText(DateUtil.convertFromServerDateToRequestedFormat(
                    todayDate, ConfigurationMasterHelper.outDateFormat));
            mPaymentBO.setChequeDate(todayDate);
        }
        mChequeDateBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataPickerDialogFragment newFragment = new DataPickerDialogFragment();
                newFragment.show(getFragmentManager(), "datePicker1");
                //AssetTrackingFragment.DatePickerFragment newFragment = new AssetTrackingFragment.DatePickerFragment();
                //newFragment.show(getFragmentManager(),"datePicker1");
            }
        });

        updateTotalAmountEntered();
        updateBankAndBranchSelectedItem();

       /* mCollectAmountET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialogCustomKeyBoard == null || !dialogCustomKeyBoard.isDialogCreated()) {
                    dialogCustomKeyBoard = new CustomKeyBoard(getActivity(), mCollectAmountET, true, 12);
                    dialogCustomKeyBoard.show();
                    dialogCustomKeyBoard.setCancelable(false);

                    //Grab the window of the dialog, and change the width
                    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                    Window window = dialogCustomKeyBoard.getWindow();
                    lp.copyFrom(window.getAttributes());
                    lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                    lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                    window.setAttributes(lp);
                }
            }
        });*/

        cameraBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!"".equals(mChequeNoET.getText().toString()) && mPaymentBO.getAmount() > 0) {
                    String fnameStarts;
                    boolean nfiles_there;
                    if (bmodel.isExternalStorageAvailable()) {

                        mImageName = "COL_CHQ_"
                                + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                                + "_" + Commons.now(Commons.DATE_TIME)
                                + "_img.jpg";


                        Intent intent = new Intent(getActivity(), CameraActivity.class);
                        intent.putExtra("quality", 40);
                        mImagePath = "Collection" + "/" + bmodel.userMasterHelper.getUserMasterBO
                                ().getDownloadDate().replace("/", "")
                                + "/"
                                + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                                + "/";
                        mImagePath = mImagePath + mImageName;
                        String path = HomeScreenFragment.photoPath + "/" + mImageName;

                        mPaymentBO.setImageName(mImagePath);

                        intent.putExtra("path", path);
                        startActivityForResult(intent,
                                bmodel.CAMERA_REQUEST_CODE);

                    }
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.pls_select_chequeno), Toast.LENGTH_SHORT).show();
                }
            }
        });

        mCollectAmountET.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                QUANTITY = mCollectAmountET;

                int inType = mCollectAmountET
                        .getInputType();
                mCollectAmountET
                        .setInputType(InputType.TYPE_NULL);
                mCollectAmountET.onTouchEvent(event);
                mCollectAmountET.setInputType(inType);
                mCollectAmountET.selectAll();
                mCollectAmountET.requestFocus();
                inputManager.hideSoftInputFromWindow(
                        mCollectAmountET
                                .getWindowToken(), 0);
                return false;
            }
        });
        mCollectAmountET.addTextChangedListener(new TextWatcher() {
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
                if (!"".equals(qty)) {
                    value = SDUtil.convertToDouble(qty);
                }
                mPaymentBO.setAmount(value);
                mPaymentBO.setUpdatePayableamt(value);

                if (value > 0 && isAdvancePaymentAvailabe && !bmodel.collectionHelper.isUseAllAdvancePaymentAmt()) {
                    if (!qty.contains("."))
                        qty = qty.length() > 1 ? qty.substring(0,
                                qty.length() - 1) : "0";
                    else
                        qty = "";

                    mCollectAmountET.setText(qty);
                    Toast.makeText(getActivity(), getResources().getString(R.string.please_user_advancepayment),
                            Toast.LENGTH_SHORT).show();
                } else if (!bmodel.collectionHelper.isEnterAmountExceed(mPaymentList, StandardListMasterConstants.CHEQUE)) {
                    //updateTotalAmountEntered();
                } else {
                    if (!qty.contains("."))
                        qty = qty.length() > 1 ? qty.substring(0,
                                qty.length() - 1) : "0";
                    else
                        qty = "";

                    mCollectAmountET.setText(qty);
                    Toast.makeText(
                            getActivity(),
                            getResources()
                                    .getString(
                                            R.string.amount_exeeds_the_balance_please_check),
                            Toast.LENGTH_SHORT).show();
                }

                mCollectAmountET.setSelection(mCollectAmountET.getText().length());
            }
        });
        mChequeNoET.addTextChangedListener(new TextWatcher() {
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
        mBankET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mPaymentBO.setBankName(s.toString());
            }
        });

        mBranchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mPaymentBO.setBranchName(s.toString());
            }
        });

        Drawable mDrawable = ContextCompat.getDrawable(getActivity(), R.drawable.ic_camera);
        mDrawable.setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);

        updateView(mPaymentBO);
        return rootView;
    }


    private void updateTotalAmountEntered() {
        double totalCollectepayment = 0;
        if (mPaymentList != null && mPaymentList.size() > 0) {
            for (PaymentBO paymentBO : mPaymentList) {
                totalCollectepayment = totalCollectepayment + paymentBO.getAmount();
            }
        }

        mTotalAmountTV.setText(bmodel.formatValue(totalCollectepayment));
    }

    private void updateBranchSpiner(String bankId) {
        ArrayAdapter<BranchMasterBO> branchSpinnerAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_bluetext_layout);
        branchSpinnerAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
        BranchMasterBO branchMasterBO = new BranchMasterBO();
        branchMasterBO.setBankID(-1 + "");
        branchMasterBO.setBranchID(-1 + "");
        branchMasterBO.setBranchName(getResources().getString(R.string.sel_branch));
        branchSpinnerAdapter.add(branchMasterBO);
        int count = 0;
        int selectedBranchPos = 0;

        if (mBranchDetailsList != null && mBranchDetailsList.size() > 0) {

            for (BranchMasterBO branchBO : mBranchDetailsList) {

                if (branchBO.getBankID().equals(bankId)) {
                    count++;
                    if (branchBO.getBranchID().equals(mPaymentBO.getBranchId())) {
                        selectedBranchPos = count;
                    }

                    branchSpinnerAdapter.add(branchBO);
                }
            }
        }
        mBranchSpin.setAdapter(branchSpinnerAdapter);
        mBranchSpin.setSelection(selectedBranchPos);
    }

    @Override
    public void updateDate(Date date, String tag) {
        String paidDate = DateUtil.convertDateObjectToRequestedFormat(
                date, "yyyy/MM/dd");
        if (!bmodel.configurationMasterHelper.IS_POST_DATE_ALLOW) {
            if (!SDUtil.now(SDUtil.DATE_GLOBAL).equals(paidDate)) {
                Toast.makeText(getActivity(), getResources().getString(
                        R.string.post_dated_cheque_notallow),
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }
        mChequeDateBTN.setText(DateUtil.convertDateObjectToRequestedFormat(
                date, ConfigurationMasterHelper.outDateFormat));
        mPaymentBO.setChequeDate(paidDate);
        mUpdatePaymentDateInterface.updatePaymentDetails(paidDate);
    }

    private void updateBankAndBranchSelectedItem() {
        if ("".equals(mPaymentBO.getBankID()) || "-1".equals(mPaymentBO.getBankID())) {
            mBankSpin.setSelection(0);
        } else {
            if (mBankDetailList != null && mBankDetailList.size() > 0) {
                int count = 0;
                for (BankMasterBO bankBO : mBankDetailList) {
                    count = count + 1;
                    if (bankBO.getBankId() == Integer.parseInt(mPaymentBO.getBankID())) {
                        break;
                    }
                }
                if (!mPaymentBO.getBankID().equalsIgnoreCase("0"))
                    mBankSpin.setSelection(count);
                else
                    mBankSpin.setSelection(count + 1);
            } else {
                if (mPaymentBO.getBankID().equalsIgnoreCase("0"))
                    mBankSpin.setSelection(1);
            }
        }
    }

    @Override
    public void updatePaymentDetails() {
        updateTotalAmountEntered();
    }

    private void showFileDeleteAlert(final String imageNameStarts) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("");
        builder.setMessage(getResources().getString(R.string.word_already)
                + mImageCount
                + getResources().getString(
                R.string.word_photocaptured_delete_retake));

        builder.setPositiveButton(getResources().getString(R.string.yes),
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        bmodel.deleteFiles(HomeScreenFragment.photoPath,
                                imageNameStarts);
                        dialog.dismiss();
                        Intent intent = new Intent(getActivity(),
                                CameraActivity.class);
                        intent.putExtra("quality", 40);
                        String _path = HomeScreenFragment.photoPath + "/" + mImageName;
                        mPaymentBO.setImageName(mImageName);
                        Commons.print("PhotoPAth:  -      " + _path);
                        intent.putExtra("path", _path);
                        startActivityForResult(intent,
                                bmodel.CAMERA_REQUEST_CODE);
                    }
                });

        builder.setNegativeButton(getResources().getString(R.string.no),
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which)


                    {
                        dialog.dismiss();
                    }
                });

        builder.setCancelable(false);
        bmodel.applyAlertDialogTheme(builder);
    }

    public void updateView(PaymentBO paymentBO) {
        mPaymentBO = paymentBO;
        String strAmt = mPaymentBO.getAmount() + "";
        mCollectAmountET.setText(strAmt);
        mCollectAmountET.setSelection(mCollectAmountET.getText().length());

        mChequeDateBTN.setText(DateUtil.convertFromServerDateToRequestedFormat(
                mPaymentBO.getChequeDate(), ConfigurationMasterHelper.outDateFormat));
        mChequeNoET.setText(mPaymentBO.getChequeNumber());
        CollectionFragmentNew.CaseMODE caseMODE = CollectionFragmentNew.CaseMODE.valueOf(mPaymentBO.getCashMode());
        switch (caseMODE) {
            case CQ:
                mChequeNoTitleTV.setText(getResources().getString(R.string.cheque_no));
                mChequeDateTitleTV.setText(getResources().getString(R.string.cheque_date));
                break;
            case RTGS:
                mChequeNoTitleTV.setText(getResources().getString(R.string.rtgs_no));
                mChequeDateTitleTV.setText(getResources().getString(R.string.rtgs_date));
                break;
            case DD:
                mChequeNoTitleTV.setText(getResources().getString(R.string.dd_no));
                mChequeDateTitleTV.setText(getResources().getString(R.string.dd_no_date));
                break;
        }

        updateBankAndBranchSelectedItem();
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


    public void numberPressed(View vw) {
        if (QUANTITY == null) {
            bmodel.showAlert(
                    getActivity().getResources().getString(
                            R.string.please_select_item), 0);
        } else {
            int id = vw.getId();
            if (id == R.id.calcdel) {
                int s = SDUtil.convertToInt((String) QUANTITY.getText()
                        .toString());
                s = s / 10;
                QUANTITY.setText(s + "");
            } else if (id == R.id.calcdot) {
                String s = QUANTITY.getText().toString();

                if (s != null) {
                    if (!s.contains(".")) {
                        QUANTITY.setText(s + ".");// QUANTITY.append(".");
                    }
                }
            } else {
                Button ed = (Button) getView().findViewById(vw.getId());
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

    @Override
    public void onClick(View v) {
        Button vw = (Button) v;
        if (vw == applyBtn) {

            if (!isValidate(mPaymentBO)) {
                Toast.makeText(getActivity(), "Please " + mErrorMsg, Toast.LENGTH_SHORT).show();
            } else {
                double value = 0;
                String qty = mCollectAmountET.getText().toString();
                if (!"".equals(qty)) {
                    value = SDUtil.convertToDouble(qty);
                }
                mPaymentBO.setAmount(value);
                mPaymentBO.setUpdatePayableamt(value);
                updateTotalAmountEntered();
                getActivity().finish();
            }
        } else if (vw == cancelBTn) {
            mPaymentBO.setAmount(tempPaidAmt);
            mPaymentBO.setUpdatePayableamt(tempPaidAmt);
            getActivity().finish();

        }

    }

    //Validate Cheque,RTGS and DemandDraft field data.
    private boolean isValidate(PaymentBO paymentBO) {

        if (paymentBO.getCashMode().equalsIgnoreCase(StandardListMasterConstants.CHEQUE)) {
            if (paymentBO != null) {

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
                if (!(paymentBO.getAmount() > 0)) {
                    mErrorMsg = getResources().getString(R.string.enter_amount) + " in cheque";
                    return false;
                }

            }
        } else if (paymentBO.getCashMode().equalsIgnoreCase(StandardListMasterConstants.DEMAND_DRAFT)) {

            if (paymentBO != null) {

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
                if (!(paymentBO.getAmount() > 0)) {
                    mErrorMsg = getResources().getString(R.string.enter_amount) + " in Demand Draft";
                    return false;
                }
            }
        } else if (paymentBO.getCashMode().equalsIgnoreCase(StandardListMasterConstants.RTGS)) {

            if (paymentBO != null) {

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
                if (!(paymentBO.getAmount() > 0)) {
                    mErrorMsg = getResources().getString(R.string.enter_amount) + " in RTGS";
                    return false;
                }

            }
        }

        return true;
    }

}
