package com.ivy.cpg.view.collection;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.sd.png.view.DataPickerDialogFragment;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FileUtils;

import java.util.ArrayList;
import java.util.Date;

public class ChequeModeFragment extends IvyBaseFragment
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
    private EditText mAccountnoET;

    private ArrayList<BranchMasterBO> mBranchDetailsList;
    private Button mChequeDateBTN;
    private UpdatePaymentByDateInterface mUpdatePaymentDateInterface;

    private String mImageName;
    private String mImagePath;
    private View rootView;
    private boolean isAdvancePaymentAvailabe;

    private EditText QUANTITY;
    private String append = "";
    private InputMethodManager inputManager;
    private Button applyBtn, cancelBTn;
    private double tempPaidAmt = 0.0;
    private String mErrorMsg = "";
    private int chqMinDate = 0, chqMaxDate = 0;
    private LinearLayout llAccountNo;
    String mName = "";
    private CollectionHelper collectionHelper;
    private final int CAMERA_REQUEST_CODE = 1;
    private final int CLEAR_OBJECTS = 0;
    private final int PHOTO_DELETE = 1;
    private static String folderPath;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        collectionHelper = CollectionHelper.getInstance(getActivity());
        inputManager = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);

        mUpdatePaymentDateInterface = (UpdatePaymentByDateInterface) getActivity();

        final int checkModePos = getArguments().getInt("position", 0);
        isAdvancePaymentAvailabe = getArguments().getBoolean("IsAdvancePaymentAvailable", false);

        mPaymentList = collectionHelper.getCollectionPaymentList();
        mPaymentBO = mPaymentList.get(checkModePos);
        tempPaidAmt = mPaymentBO.getAmount();

        chqMinDate = bmodel.configurationMasterHelper.CHQ_MIN_DATE;
        chqMaxDate = bmodel.configurationMasterHelper.CHQ_MAX_DATE;
        folderPath = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/"
                + DataMembers.photoFolderName;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_cheque_mode, container, false);
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
        cancelBTn = rootView.findViewById(R.id.cancelbtn);
        cancelBTn.setOnClickListener(this);

        collectionHelper.clearPaymentObjects(mPaymentBO);
        mTotalAmountTV = rootView.findViewById(R.id.tv_total_amount);
        mCollectAmountET = rootView.findViewById(R.id.edit_collectamt);
        mChequeNoTitleTV = rootView.findViewById(R.id.tv_chequeno_title);
        mChequeDateTitleTV = rootView.findViewById(R.id.tv_date_title);
        mChequeNoET = rootView.findViewById(R.id.edit_chequeno);
        mBankET = rootView.findViewById(R.id.edit_bankname);
        mBranchET = rootView.findViewById(R.id.edit_branchname);
        mAccountnoET = rootView.findViewById(R.id.edit_accountno);
        llAccountNo = rootView.findViewById(R.id.llAccountNo);

        if (mPaymentBO.getAmount() > 0) {
            mCollectAmountET.setText(bmodel.formatValue(SDUtil.convertToDouble(SDUtil.getWithoutExponential(mPaymentBO.getAmount()))));
            mCollectAmountET.setSelection(mCollectAmountET.getText().length());
        }

        if (mPaymentBO.getAccountNumber().length() > 0) {
            mAccountnoET.setText(mPaymentBO.getAccountNumber());
            mAccountnoET.setSelection(mAccountnoET.getText().length());
        }

        mChequeNoET.setText(mPaymentBO.getChequeNumber());
        if (bmodel.configurationMasterHelper.COLL_CHEQUE_MODE) {
            mChequeNoET.setInputType(InputType.TYPE_CLASS_TEXT);
        }

        mBankSpin = rootView.findViewById(R.id.spin_bank);
        ArrayAdapter<BankMasterBO> bankSpinnerAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_bluetext_layout);
        bankSpinnerAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
        BankMasterBO bankMasterBO = new BankMasterBO();
        bankMasterBO.setBankId(-1);
        bankMasterBO.setBankName(getResources().getString(R.string.sel_bank));
        bankSpinnerAdapter.add(bankMasterBO);
        mBankDetailList = collectionHelper.getBankMasterBO();
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

        mBranchSpin = rootView.findViewById(R.id.spin_branch);

        mBranchDetailsList = collectionHelper.getBranchMasterBO();

        mBankSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                BankMasterBO bankBO = (BankMasterBO) mBankSpin.getSelectedItem();
                if (bankBO.getBankId() == 0) {
                    (rootView.findViewById(R.id.llBranch)).setVisibility(View.GONE);
                    (rootView.findViewById(R.id.llbankbranch)).setVisibility(View.VISIBLE);
                    mPaymentBO.setBankID(bankBO.getBankId() + "");
                    mPaymentBO.setBranchId("0");
                    mBankET.setText(mPaymentBO.getBankName());
                    mBranchET.setText(mPaymentBO.getBranchName());

                } else {
                    (rootView.findViewById(R.id.llBranch)).setVisibility(View.VISIBLE);
                    (rootView.findViewById(R.id.llbankbranch)).setVisibility(View.GONE);
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
        ImageView cameraBTN = rootView.findViewById(R.id.btn_camera);
        mChequeDateBTN = rootView.findViewById(R.id.btn_datepicker);
        if (mPaymentBO.getChequeDate() != null && !"".equals(mPaymentBO.getChequeDate()))
            mChequeDateBTN.setText(DateTimeUtils.convertFromServerDateToRequestedFormat(
                    mPaymentBO.getChequeDate(), ConfigurationMasterHelper.outDateFormat));
        else {
            String todayDate = DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL);
            mChequeDateBTN.setText(DateTimeUtils.convertFromServerDateToRequestedFormat(
                    todayDate, ConfigurationMasterHelper.outDateFormat));
            mPaymentBO.setChequeDate(todayDate);
        }
        mChequeDateBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataPickerDialogFragment newFragment = new DataPickerDialogFragment();

                Bundle args = new Bundle();
                args.putString("MODULE", mName);
                args.putInt("CHQMINDATE", chqMinDate * -1);
                args.putInt("CHQMAXDATE", chqMaxDate);
                newFragment.setArguments(args);

                newFragment.show(getFragmentManager(), "datePicker1");
            }
        });

        updateTotalAmountEntered();
        updateBankAndBranchSelectedItem();

        cameraBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!"".equals(mChequeNoET.getText().toString()) && mPaymentBO.getAmount() > 0) {
                    if (FileUtils.isExternalStorageAvailable(10)) {

                        mImageName = "COL_CHQ_"
                                + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                                + "_" + Commons.now(Commons.DATE_TIME)
                                + "_img.jpg";

                        boolean mIsFileAvailable = false;
                        if (mPaymentBO.getImageName() != null
                                && mPaymentBO.getImageName().length() > 0) {
                            mIsFileAvailable = FileUtils
                                    .checkForNFilesInFolder(
                                            folderPath, 1,
                                            mPaymentBO.getImageName().
                                                    split(bmodel.userMasterHelper.
                                                            getUserMasterBO().getUserid() + "/")[1]);
                        }

                        if (mIsFileAvailable) {
                            showChequeAlertDialog(PHOTO_DELETE);
                        } else {


                            Intent intent = new Intent(getActivity(), CameraActivity.class);
                            intent.putExtra(CameraActivity.QUALITY, 40);
                            mImagePath = "Collection" + "/" + bmodel.userMasterHelper.getUserMasterBO
                                    ().getDownloadDate().replace("/", "")
                                    + "/"
                                    + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                                    + "/";
                            mImagePath = mImagePath + mImageName;
                            String path = FileUtils.photoFolderPath + "/" + mImageName;

                            mPaymentBO.setImageName(mImagePath);

                            intent.putExtra(CameraActivity.PATH, path);
                            startActivityForResult(intent,
                                    bmodel.CAMERA_REQUEST_CODE);
                        }

                    }
                } else {
                    if ("".equals(mChequeNoET.getText().toString()))
                        Toast.makeText(getActivity(), getResources().getString(R.string.pls_select_chequeno), Toast.LENGTH_SHORT).show();
                    else if (mPaymentBO.getAmount() == 0)
                        Toast.makeText(getActivity(), getResources().getString(R.string.alert_amount), Toast.LENGTH_SHORT).show();
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
                if (SDUtil.isValidDecimal(qty, 16, 2)) {
                    double value = 0;
                    if (!"".equals(qty)) {
                        value = SDUtil.convertToDouble(qty);
                    }
                    mPaymentBO.setAmount(value);
                    mPaymentBO.setUpdatePayableamt(value);

                    if (value > 0 && isAdvancePaymentAvailabe && !collectionHelper.isUseAllAdvancePaymentAmt()) {
                        if (!qty.contains("."))
                            qty = qty.length() > 1 ? qty.substring(0,
                                    qty.length() - 1) : "0";
                        else
                            qty = "";

                        mCollectAmountET.setText(SDUtil.getWithoutExponential(qty));
                        Toast.makeText(getActivity(), getResources().getString(R.string.please_user_advancepayment),
                                Toast.LENGTH_SHORT).show();
                    } else if (!collectionHelper.isEnterAmountExceed(mPaymentList, StandardListMasterConstants.CHEQUE)) {
                        updateTotalAmountEntered();
                    } else {
                        if (!qty.contains("."))
                            qty = qty.length() > 1 ? qty.substring(0,
                                    qty.length() - 1) : "0";
                        else
                            qty = "";

                        mCollectAmountET.setText(SDUtil.getWithoutExponential(qty));
                        Toast.makeText(
                                getActivity(),
                                getResources()
                                        .getString(
                                                R.string.amount_exeeds_the_balance_please_check),
                                Toast.LENGTH_SHORT).show();
                    }

                    mCollectAmountET.setSelection(mCollectAmountET.getText().length());
                } else {
                    qty = qty.length() > 1 ? qty
                            .substring(0, qty.length() - 1) : "0";
                    mCollectAmountET.setText(SDUtil.getWithoutExponential(qty));
                }
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

        mAccountnoET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mPaymentBO.setAccountNumber(s.toString());
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
        String paidDate = DateTimeUtils.convertDateObjectToRequestedFormat(
                date, "yyyy/MM/dd");
        if (!bmodel.configurationMasterHelper.IS_POST_DATE_ALLOW) {
            if (!bmodel.configurationMasterHelper.IS_ENABLE_MIN_MAX_DATE_CHQ) {
                if (!DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL).equals(paidDate)) {
                    Toast.makeText(getActivity(), getResources().getString(
                            R.string.post_dated_cheque_notallow),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            if (mPaymentBO.getCashMode().equalsIgnoreCase(StandardListMasterConstants.DEMAND_DRAFT)) {
                if (!DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL).equals(paidDate)) {
                    Toast.makeText(getActivity(), getResources().getString(
                            R.string.post_dated_demand_draft_notallow),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            if (mPaymentBO.getCashMode().equalsIgnoreCase(StandardListMasterConstants.RTGS)) {
                if (!DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL).equals(paidDate)) {
                    Toast.makeText(getActivity(), getResources().getString(
                            R.string.post_dated_rtgs_notallow),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }
        mChequeDateBTN.setText(DateTimeUtils.convertDateObjectToRequestedFormat(
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
                    if (bankBO.getBankId() == SDUtil.convertToInt(mPaymentBO.getBankID())) {
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


    public void showChequeAlertDialog(int flag) {
        int mImageCount = 1;
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setIcon(null);
        alertDialogBuilder.setCancelable(false);

        switch (flag) {
            case CLEAR_OBJECTS:
                alertDialogBuilder
                        .setTitle(
                                getResources().getString(
                                        R.string.data_cleared_do_u_want_go_back))
                        .setPositiveButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        clearObjects();
                                    }
                                })
                        .setNegativeButton(getResources().getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                    }
                                });
                break;
            case PHOTO_DELETE:
                alertDialogBuilder.setMessage(getResources().getString(R.string.word_already)
                        + mImageCount
                        + getResources().getString(
                        R.string.word_photocaptured_delete_retake));

                alertDialogBuilder.setPositiveButton(getResources().getString(R.string.yes),
                        new android.content.DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                FileUtils.deleteFiles(folderPath,
                                        mPaymentBO.getImageName().
                                                split(bmodel.userMasterHelper.
                                                        getUserMasterBO().getUserid() + "/")[1]);
                                dialog.dismiss();
                                Intent intent = new Intent(getActivity(),
                                        CameraActivity.class);
                                intent.putExtra("quality", 40);
                                String _path = folderPath + "/" + mImageName;
                                mPaymentBO.setImageName(mImageName);
                                Commons.print("PhotoPAth:  -      " + _path);
                                intent.putExtra("path", _path);
                                startActivityForResult(intent,
                                        bmodel.CAMERA_REQUEST_CODE);
                            }
                        });

                alertDialogBuilder.setNegativeButton(getResources().getString(R.string.no),
                        new android.content.DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                break;

        }
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        bmodel.applyAlertDialogTheme(alertDialogBuilder);
    }


    public void updateView(PaymentBO paymentBO) {
        mPaymentBO = paymentBO;
        String strAmt = mPaymentBO.getAmount() + "";
        mCollectAmountET.setText(bmodel.formatValue(SDUtil.convertToDouble(SDUtil.getWithoutExponential(strAmt))));
        mCollectAmountET.setSelection(mCollectAmountET.getText().length());

        mChequeDateBTN.setText(DateTimeUtils.convertFromServerDateToRequestedFormat(
                mPaymentBO.getChequeDate(), ConfigurationMasterHelper.outDateFormat));
        mChequeNoET.setText(mPaymentBO.getChequeNumber());

        mAccountnoET.setText(mPaymentBO.getAccountNumber() + "");
        mAccountnoET.setSelection(mAccountnoET.getText().length());

        CollectionFragmentNew.CaseMODE caseMODE = CollectionFragmentNew.CaseMODE.valueOf(mPaymentBO.getCashMode());
        switch (caseMODE) {
            case CQ:
                mChequeNoTitleTV.setText(getResources().getString(R.string.cheque_no));
                mChequeDateTitleTV.setText(getResources().getString(R.string.cheque_date));
                if (bmodel.configurationMasterHelper.IS_ENABLE_ACC_NO_CHQ) {
                    llAccountNo.setVisibility(View.VISIBLE);
                } else {
                    llAccountNo.setVisibility(View.GONE);
                }
                mName = "CHEQUE";
                break;
            case RTGS:
                mChequeNoTitleTV.setText(getResources().getString(R.string.rtgs_no));
                mChequeDateTitleTV.setText(getResources().getString(R.string.rtgs_date));
                llAccountNo.setVisibility(View.GONE);
                mName = "RTGS";
                break;
            case DD:
                mChequeNoTitleTV.setText(getResources().getString(R.string.dd_no));
                mChequeDateTitleTV.setText(getResources().getString(R.string.dd_no_date));
                llAccountNo.setVisibility(View.GONE);
                mName = "DD";
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
            if (isValidate(mPaymentBO)) {
                showChequeAlertDialog(CLEAR_OBJECTS);
            } else {
                getActivity().finish();
            }
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

    @Override
    public void onClick(View v) {
        Button vw = (Button) v;
        if (vw == applyBtn) {

            if (!isValidate(mPaymentBO)) {
                Toast.makeText(getActivity(), mErrorMsg, Toast.LENGTH_SHORT).show();
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

            if (mPaymentBO.getCashMode().equalsIgnoreCase(StandardListMasterConstants.RTGS)) {
                mPaymentBO.setAmount(0);
                mPaymentBO.setUpdatePayableamt(0);
            }
        }

    }

    private void clearObjects() {
        mPaymentBO.setAmount(0);
        mPaymentBO.setUpdatePayableamt(0);

        if (mPaymentBO.getImageName() != null
                && mPaymentBO.getImageName().length() > 0) {
            FileUtils.deleteFiles(folderPath,
                    mPaymentBO.getImageName().
                            split(bmodel.userMasterHelper.
                                    getUserMasterBO().getUserid() + "/")[1]);
            mPaymentBO.setImageName(null);
        }

        getActivity().finish();

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

                if (bmodel.configurationMasterHelper.IS_ENABLE_ACC_NO_CHQ) {
                    if (!(paymentBO.getAccountNumber().length() > 0)) {
                        mErrorMsg = getResources().getString(R.string.enter_account) + " in cheque";
                        return false;
                    }
                    if (!collectionHelper.checkRetailerWiseAccountMatched(paymentBO.getAccountNumber())) {
                        mErrorMsg = "Check the Retailer Account No. It is inCorrect in cheque";
                        return false;
                    }
                }
            }
        } else if (paymentBO.getCashMode().equalsIgnoreCase(StandardListMasterConstants.DEMAND_DRAFT)) {

            if (paymentBO != null) {

                if ("".equals(paymentBO.getChequeNumber())) {
                    mErrorMsg = getResources().getString(R.string.pls_enter_ddno) + " in Demand Draft";
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
                    mErrorMsg = getResources().getString(R.string.pls_enter_rtgsno) + " in RTGS";
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == 1) {
            mPaymentBO.setImageName(mImagePath);
        } else {
            mPaymentBO.setImageName(null);
        }
    }

}
