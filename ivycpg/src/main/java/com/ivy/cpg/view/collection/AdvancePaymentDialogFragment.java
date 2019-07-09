package com.ivy.cpg.view.collection;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.BankMasterBO;
import com.ivy.sd.png.bo.BranchMasterBO;
import com.ivy.sd.png.bo.InvoiceHeaderBO;
import com.ivy.sd.png.bo.PaymentBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.ReportHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.sd.png.view.CustomKeyBoard;
import com.ivy.sd.png.view.DataPickerDialogFragment;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.sd.print.DemoSleeper;
import com.ivy.sd.print.SettingsHelper;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FileUtils;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by rajesh.k on 11/8/2016.
 */

public class AdvancePaymentDialogFragment extends IvyBaseFragment
        implements DataPickerDialogFragment.UpdateDateInterface,
        PrintCountDialogFragment.PrintInterface {

    private BusinessModel bmodel;
    ArrayList<PaymentBO> mPaymentList;
    private RadioGroup mPaymentTypeRB;
    private TextView mPayTotalTV;
    private EditText mCollectAmtET, mChequeNoET;
    private Button mChequeDateBTN, mSubmitBTN, mCloseBTN;
    private ImageView mCameraImg;
    private Spinner mBankSpin, mBranchSpin;
    private LinearLayout mBankModeLL, mChequeDateLL;
    private ArrayList<BankMasterBO> mBankDetailsList;
    private ArrayList<BranchMasterBO> mBranchDetailsList;
    private PaymentBO mSelectedPaymentBO;
    private CustomKeyBoard dialogCustomKeyBoard;
    private String mImageName;
    private final int mImageCount = 1;
    private int mSelectedPrintCount = 0;
    private AlertDialog alertDialog;
    private Connection zebraPrinterConnection;
    private double mTotalInvoiceAmt = 0;
    private int rcheckedId = 0;
    private EditText mBankET;
    private EditText mBranchET;

    private CollectionHelper collectionHelper;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        collectionHelper = CollectionHelper.getInstance(getActivity());

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootview = inflater.inflate(R.layout.dialog_fragment_advance_payment, container, false);
        setHasOptionsMenu(true);

        return rootview;
    }


    @Override
    public void onStart() {
        super.onStart();
        //getDialog().setTitle("Advance Payment");
        initializeobj();
        loadInvoiceList();
        loadPaymentObj();
        allViewTypeListener();
        loadBankDetails();
    }

    public void viewRestore(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (mSelectedPaymentBO == null)
                mSelectedPaymentBO = new PaymentBO();

            mSelectedPaymentBO.setAmount(savedInstanceState.getDouble("amount"));
            mSelectedPaymentBO.setCashMode(savedInstanceState.getString("mode"));
            mSelectedPaymentBO.setChequeNumber(savedInstanceState.getString("chequeno"));
            mSelectedPaymentBO.setChequeDate(savedInstanceState.getString("chequedate"));
            mSelectedPaymentBO.setBankID(savedInstanceState.getString("bankid"));
            mSelectedPaymentBO.setBranchId(savedInstanceState.getString("branchid"));
            mSelectedPaymentBO.setImageName(savedInstanceState.getString("imagename"));

            if (mSelectedPaymentBO.getCashMode().equals(StandardListMasterConstants.CASH)) {
                modegone();
            } else {
                modevisibility();
                updateBankAndBranchSelectedItem();
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putDouble("amount", mSelectedPaymentBO.getAmount());
        outState.putString("mode", mSelectedPaymentBO.getCashMode());
        outState.putString("chequeno", mSelectedPaymentBO.getChequeNumber());
        outState.putString("chequedate", mSelectedPaymentBO.getChequeDate());
        outState.putString("bankid", mSelectedPaymentBO.getBankID());
        outState.putString("branchid", mSelectedPaymentBO.getBranchId());
        outState.putString("imagename", mSelectedPaymentBO.getImageName());
        ((CollectionScreen) getActivity()).passData(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            if (mSelectedPaymentBO == null)
                mSelectedPaymentBO = new PaymentBO();

            mSelectedPaymentBO.setAmount(savedInstanceState.getDouble("amount"));
            mSelectedPaymentBO.setCashMode(savedInstanceState.getString("mode"));
            mSelectedPaymentBO.setChequeNumber(savedInstanceState.getString("chequeno"));
            mSelectedPaymentBO.setChequeDate(savedInstanceState.getString("chequedate"));
            mSelectedPaymentBO.setBankID(savedInstanceState.getString("bankid"));
            mSelectedPaymentBO.setBranchId(savedInstanceState.getString("branchid"));
            mSelectedPaymentBO.setImageName(savedInstanceState.getString("imagename"));
        }
    }

    // check pending invoice is available or not available
    private void loadInvoiceList() {
        ArrayList<InvoiceHeaderBO> mInvioceList = bmodel.getInvoiceHeaderBO();

        for (InvoiceHeaderBO invoiceHeaderBO : mInvioceList) {
            if (invoiceHeaderBO.getBalance() > 0) {
                mTotalInvoiceAmt = mTotalInvoiceAmt + invoiceHeaderBO.getBalance() + invoiceHeaderBO.getRemainingDiscountAmt();
            }
        }
    }


    private void loadPaymentObj() {
        mPaymentList = new ArrayList<>();
        PaymentBO cashBO = new PaymentBO();
        cashBO.setCashMode(StandardListMasterConstants.CASH);
        cashBO.setListName("Cash");
        if (mSelectedPaymentBO == null) {
            mSelectedPaymentBO = cashBO;
        }
        mPaymentList.add(cashBO);
        PaymentBO chequeBO = new PaymentBO();
        chequeBO.setCashMode(StandardListMasterConstants.CHEQUE);
        chequeBO.setListName("Cheque");
        mPaymentList.add(chequeBO);

        if (mSelectedPaymentBO != null && !"".equals(mSelectedPaymentBO.getChequeDate())) {
            mChequeDateBTN.setText(mSelectedPaymentBO.getChequeDate());
            mSelectedPaymentBO.setChequeDate(mSelectedPaymentBO.getChequeDate());
        } else {
            String todayDate = DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL);
            updateDate(new Date(todayDate), "");
        }

    }

    private void initializeobj() {
        mPaymentTypeRB =  getView().findViewById(R.id.chequeorcash);
        mCollectAmtET =  getView().findViewById(R.id.collectionAmount);
        mChequeNoET =  getView().findViewById(R.id.collectionchequeNo);
        mChequeDateBTN =  getView().findViewById(R.id.collectionDate);
        mCameraImg =  getView().findViewById(R.id.capturecheque);
        mBankModeLL =  getView().findViewById(R.id.mode);
        mChequeDateLL =  getView().findViewById(R.id.ccdate);
        mPayTotalTV =  getView().findViewById(R.id.payTotal);
        mSubmitBTN =  getView().findViewById(R.id.btnsubmit);
        mCloseBTN =  getView().findViewById(R.id.btnclose);
    }

    private void loadBankDetails() {
        mBankSpin =  getView().findViewById(R.id.bankName);
        mBranchSpin =  getView().findViewById(R.id.bankArea);
        mBankET =  getView().findViewById(R.id.edit_bankname);
        mBranchET =  getView().findViewById(R.id.edit_branchname);
        mBankDetailsList = collectionHelper.getBankMasterBO();
        mBranchDetailsList = collectionHelper.getBranchMasterBO();
        ArrayAdapter<BankMasterBO> bankSpinnerAdapter = new ArrayAdapter<>(
                getActivity(), R.layout.spinner_bluetext_layout);
        bankSpinnerAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
        BankMasterBO mm = new BankMasterBO();
        mm.setBankId(-1);
        mm.setBankName(getActivity().getResources().getString(
                R.string.sel_bank));
        bankSpinnerAdapter.add(mm);
        final int size = mBankDetailsList.size();
        for (int i = 0; i < size; ++i) {
            BankMasterBO ret = mBankDetailsList.get(i);
            bankSpinnerAdapter.add(ret);
        }
        BankMasterBO otherMasterBO = new BankMasterBO();
        otherMasterBO.setBankId(0);
        bankSpinnerAdapter.add(otherMasterBO);
        otherMasterBO.setBankName(getResources().getString(R.string.tab_text_others));

        mBankSpin.setAdapter(bankSpinnerAdapter);

        mBankSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                BankMasterBO bank = (BankMasterBO) mBankSpin.getSelectedItem();

                if (bank.getBankId() == 0) {
                    ( getView().findViewById(R.id.llBranch)).setVisibility(View.GONE);
                    ( getView().findViewById(R.id.llbankbranch)).setVisibility(View.VISIBLE);
                    mBranchSpin.setSelection(0);
                    if (mSelectedPaymentBO != null) {
                        mSelectedPaymentBO.setBankID("0");
                        mSelectedPaymentBO.setBranchId("0");
                        mBankET.setText(mSelectedPaymentBO.getBankName());
                        mBranchET.setText(mSelectedPaymentBO.getBranchName());
                    }

                } else {
                    ( getView().findViewById(R.id.llBranch)).setVisibility(View.VISIBLE);
                    ( getView().findViewById(R.id.llbankbranch)).setVisibility(View.GONE);
                    String bankID = String.valueOf(bank.getBankId());
                    if (mSelectedPaymentBO != null) {
                        mSelectedPaymentBO.setBankID(bankID);
                    }
                    updateBranchSpinner(bankID + "");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mBranchSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

                BranchMasterBO branch = (BranchMasterBO) mBranchSpin
                        .getSelectedItem();
                if (branch.getBranchID().equals("-1")) {

                    if (mSelectedPaymentBO != null)
                        mSelectedPaymentBO.setBranchId("-1");
                } else {

                    if (mSelectedPaymentBO != null)
                        mSelectedPaymentBO.setBranchId(branch.getBranchID());
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
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
                mSelectedPaymentBO.setBankName(s.toString());
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
                mSelectedPaymentBO.setBranchName(s.toString());
            }
        });

    }

    private void updateBranchSpinner(String bankID) {
        ArrayAdapter<BranchMasterBO> branchSpinnerAdapter = new ArrayAdapter<>(
                getActivity(), R.layout.spinner_bluetext_layout);
        branchSpinnerAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
        BranchMasterBO mm = new BranchMasterBO();
        mm.setBranchID("-1");
        mm.setBranchName(getActivity().getResources().getString(
                R.string.sel_branch));
        branchSpinnerAdapter.add(mm);
        int branchSize = mBranchDetailsList.size();
        for (int j = 0; j < branchSize; ++j) {
            BranchMasterBO ret = mBranchDetailsList.get(j);
            if (ret.getBankID().equals(bankID)) {
                branchSpinnerAdapter.add(ret);
            }
        }
        mBranchSpin.setAdapter(branchSpinnerAdapter);
        if (!mSelectedPaymentBO.getBranchId().equals("-1")) {
            for (int k = 1; k < branchSpinnerAdapter.getCount(); k++) {
                BranchMasterBO branchMasterBO = branchSpinnerAdapter.getItem(k);
                if (branchMasterBO.getBranchID().equals(mSelectedPaymentBO.getBranchId())) {
                    mBranchSpin.setSelection(k);
                    break;
                }
            }
        }
    }

    private void allViewTypeListener() {
        if (mSelectedPaymentBO.getCashMode().equals(StandardListMasterConstants.CASH)) {
            modegone();
        } else if (mSelectedPaymentBO.getCashMode().equals(StandardListMasterConstants.CHEQUE)) {
            modevisibility();
        }

        try {
            if (collectionHelper.getCollectionPaymentList() == null
                    || collectionHelper.getCollectionPaymentList().size() == 0) {
                modeInvisible();
            }
            mPaymentTypeRB
                    .setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

                        @Override
                        public void onCheckedChanged(RadioGroup group,
                                                     int checkedId) {
                            if (checkedId == R.id.cashRadioButton) {
                                mSelectedPaymentBO = mPaymentList.get(0);
                                rcheckedId = 0;
                                modegone();
                                clearPaymentObject(StandardListMasterConstants.CHEQUE);
                            } else if (checkedId == R.id.chequeRadioButton) {
                                mSelectedPaymentBO = mPaymentList.get(1);
                                rcheckedId = 1;
                                modevisibility();
                                clearPaymentObject(StandardListMasterConstants.CASH);
                            }
                        }
                    });
        } catch (Exception e) {
            Commons.printException(e);
        }
        mCameraImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!"".equals(mChequeNoET.getText().toString())) {
                    String fnameStarts;
                    boolean nfiles_there;
                    if (bmodel.isExternalStorageAvailable()) {

                        mImageName = "COL_CHQ_"
                                + bmodel.getRetailerMasterBO().getRetailerID()
                                + "_" + mChequeNoET.getText().toString()
                                + "_" + Commons.now(Commons.DATE)
                                + "_img.jpg";

                        fnameStarts = "COL_CHQ_"
                                + bmodel.getRetailerMasterBO().getRetailerID()
                                + "_" + mChequeNoET.getText().toString()
                                + "_" + Commons.now(Commons.DATE);


                        nfiles_there = bmodel.checkForNFilesInFolder(
                                FileUtils.photoFolderPath, mImageCount, fnameStarts);
                        if (nfiles_there) {
                            showFileDeleteAlert(fnameStarts);
                        } else {
                            Intent intent = new Intent(getActivity(), CameraActivity.class);
                            intent.putExtra(CameraActivity.QUALITY, 40);
                            String path = FileUtils.photoFolderPath + "/" + mImageName;
                            intent.putExtra(CameraActivity.PATH, path);
                            startActivityForResult(intent,
                                    bmodel.CAMERA_REQUEST_CODE);
                        }
                    }
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.pls_select_chequeno), Toast.LENGTH_SHORT).show();
                }
            }
        });

        mChequeDateBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataPickerDialogFragment newFragment = new DataPickerDialogFragment();
                newFragment.show(getFragmentManager(), "datePicker1");
            }
        });

        mCollectAmtET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialogCustomKeyBoard == null || !dialogCustomKeyBoard.isDialogCreated()) {
                    dialogCustomKeyBoard = new CustomKeyBoard(getActivity(), mCollectAmtET, true, 12);
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
                double value = 0;
                if (!"".equals(qty)) {
                    value = SDUtil.convertToDouble(qty);
                }
                mSelectedPaymentBO.setAmount(value);
                mPayTotalTV.setText("Amount Paid - " + qty);

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
                mSelectedPaymentBO.setChequeNumber(s.toString());
            }
        });

        mSubmitBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTotalInvoiceAmt == 0) {//check pending invoice is available or not available
                    if (!isValidate())
                        return;

                    displayAlertDialog("Do you want to save Advance Payment?", true);
                } else {
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.advance_payment_cannot_be_reveived), Toast.LENGTH_SHORT).show();
                    if (rcheckedId == 0)
                        clearPaymentObject(StandardListMasterConstants.CASH);
                    else if (rcheckedId == 1)
                        clearPaymentObject(StandardListMasterConstants.CHEQUE);

                }


            }
        });
        mCloseBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayAlertDialog("Do you want to close Advance Payment?", false);
            }
        });

    }

    private void modegone() {
        mBankModeLL.setVisibility(View.GONE);
        mChequeDateLL.setVisibility(View.GONE);
    }

    private void modevisibility() {
        mBankModeLL.setVisibility(View.VISIBLE);
        mChequeDateLL.setVisibility(View.VISIBLE);
    }

    private void modeInvisible() {
        mChequeNoET.setVisibility(View.GONE);
        mCollectAmtET.setVisibility(View.GONE);
        mChequeDateBTN.setVisibility(View.GONE);
        mBranchSpin.setVisibility(View.GONE);
        mBankSpin.setVisibility(View.GONE);
        mBankModeLL.setVisibility(View.GONE);
        mChequeDateLL.setVisibility(View.GONE);
        mPayTotalTV.setVisibility(View.GONE);

    }


    @Override
    public void updateDate(Date date, String tag) {
        String paidDate = DateTimeUtils.convertDateObjectToRequestedFormat(
                date, "yyyy/MM/dd");
        try {
            if (!DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL).equals(paidDate))//this for checking today date since before method not woking for today date
                if (date.before(new Date())) {

                    if (mSelectedPaymentBO.getCashMode().equals(StandardListMasterConstants.DEMAND_DRAFT))
                        Toast.makeText(getActivity(), getResources().getString(
                                R.string.prev_dated_demand_draft_notallow),
                                Toast.LENGTH_SHORT).show();
                    else if (mSelectedPaymentBO.getCashMode().equals(StandardListMasterConstants.RTGS))
                        Toast.makeText(getActivity(), getResources().getString(
                                R.string.prev_dated_rtgs_notallow),
                                Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getActivity(), getResources().getString(
                                R.string.prev_dated_cheque_notallow),
                                Toast.LENGTH_SHORT).show();

                    return;
                }
        } catch (Exception e) {
            Commons.printException(e);
            return;
        }

        mChequeDateBTN.setText(DateTimeUtils.convertDateObjectToRequestedFormat(
                date, ConfigurationMasterHelper.outDateFormat));
        mSelectedPaymentBO.setChequeDate(paidDate);
    }

    private void clearPaymentObject(String mode) {
        mCollectAmtET.setText("0");
        if (mode.equals(StandardListMasterConstants.CASH)) {

            PaymentBO paymentBO = mPaymentList.get(0);
            paymentBO.setAmount(0);
            mCollectAmtET.setText("");
            mCollectAmtET.setHint("Enter Amount");
        } else if (mode.equals(StandardListMasterConstants.CHEQUE)) {
            PaymentBO paymentBO = mPaymentList.get(1);
            paymentBO.setAmount(0);
            paymentBO.setChequeDate("");
            paymentBO.setChequeNumber("");
            paymentBO.setBankID("-1");
            paymentBO.setBranchId("-1");

            mCollectAmtET.setText("");
            mCollectAmtET.setHint("Enter Amount");
            mChequeNoET.setText("");
            mBankSpin.setSelection(0);
            mBankSpin.setSelection(0);

        }
        if (mSelectedPaymentBO != null && !"".equals(mSelectedPaymentBO.getChequeDate())) {
            mChequeDateBTN.setText(mSelectedPaymentBO.getChequeDate());
            mSelectedPaymentBO.setChequeDate(mSelectedPaymentBO.getChequeDate());
        } else {
            String todayDate = DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL);
            updateDate(new Date(todayDate), "");
        }

    }


    private boolean isValidate() {
        if (mSelectedPaymentBO.getAmount() > 0) {
            if (mSelectedPaymentBO.getCashMode().equals(StandardListMasterConstants.CHEQUE)) {
                if (mSelectedPaymentBO.getChequeNumber() == null || mSelectedPaymentBO.getChequeNumber().equals("")) {
                    Toast.makeText(
                            getActivity(),
                            getActivity().getResources().getString(
                                    R.string.enter_cheque_no), Toast.LENGTH_SHORT)
                            .show();
                    return false;
                } else if (mSelectedPaymentBO.getChequeDate() == null || mSelectedPaymentBO.getChequeDate().equals("")) {

                    Toast.makeText(
                            getActivity(),
                            getActivity().getResources().getString(
                                    R.string.please_select_cheque_date), Toast.LENGTH_SHORT)
                            .show();
                    return false;
                } else if (mSelectedPaymentBO.getBankID() == null || mSelectedPaymentBO.getBankID().equals("-1")) {
                    Toast.makeText(
                            getActivity(),
                            getActivity().getResources().getString(
                                    R.string.sel_bank), Toast.LENGTH_SHORT).show();
                    return false;
                } else if (mSelectedPaymentBO.getBranchId() == null || mSelectedPaymentBO.getBranchId().equals("-1")) {
                    Toast.makeText(
                            getActivity(),
                            getActivity().getResources().getString(
                                    R.string.sel_branch), Toast.LENGTH_SHORT).show();
                    return false;
                }

            }
        } else {
            Toast.makeText(
                    getActivity(), "Please Enter Amount", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void displayAlertDialog(String message, final boolean isSave) {

        Context context = getActivity();
        String title = "Save Advance Payment";

        String button1String = "Ok";
        String button2String = "Cancel";

        AlertDialog.Builder ad = new AlertDialog.Builder(context);
        if (isSave)
            ad.setTitle(title);

        ad.setMessage(message);

        ad.setPositiveButton(
                button1String,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        if (isSave)
                            new SaveAdvancePayment().execute();
                        else
                            dismiss();
                    }
                }
        );

        ad.setNegativeButton(
                button2String,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        // do nothing

                    }
                }
        );

        //
        bmodel.applyAlertDialogTheme(ad);
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

                        bmodel.deleteFiles(FileUtils.photoFolderPath,
                                imageNameStarts);
                        dialog.dismiss();
                        Intent intent = new Intent(getActivity(),
                                CameraActivity.class);
                        intent.putExtra(CameraActivity.QUALITY, 40);
                        String _path = FileUtils.photoFolderPath + "/" + mImageName;
                        Commons.print("PhotoPAth:  -      " + _path);
                        intent.putExtra(CameraActivity.PATH, _path);
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

    @Override
    public void print(int printCount) {
        mSelectedPrintCount = printCount;
        AlertDialog.Builder build = new AlertDialog.Builder(getActivity());
        customProgressDialog(build, "Printing....");
        alertDialog = build.create();

        alertDialog.show();

        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                doConnection();
                Looper.loop();
                Looper myLooper = Looper.myLooper();
                if (myLooper != null)
                    myLooper.quit();
            }
        }).start();
    }

    @Override
    public void dismiss() {

    }

    class SaveAdvancePayment extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            collectionHelper.saveAdvancePayment(mSelectedPaymentBO);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.save_advance_payment), Toast.LENGTH_SHORT).show();
            bmodel.saveModuleCompletion("MENU_COLLECTION", true);
            // clearing the object's after saved
            if (rcheckedId == 0)
                clearPaymentObject(StandardListMasterConstants.CASH);
            else if (rcheckedId == 1)
                clearPaymentObject(StandardListMasterConstants.CHEQUE);

            if (bmodel.configurationMasterHelper.SHOW_ZEBRA_UNIPAL) {
                FragmentManager fm = getFragmentManager();
                PrintCountDialogFragment dialogFragment = new PrintCountDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putString("title", getResources().getString(R.string.advance_payment_title));
                bundle.putString("textviewTitle", getActivity().getResources().getString(R.string.advancepayment_saved_do_u_print));
                bundle.putInt("isfrom", 1);
                dialogFragment.setArguments(bundle);
                dialogFragment.show(fm, "Sample Fragment");
            } else {
                dismiss();
            }

        }
    }

    private void updateBankAndBranchSelectedItem() {
        if ("".equals(mSelectedPaymentBO.getBankID()) || (-1 + "").equals(mSelectedPaymentBO.getBranchId())) {
            mBankSpin.setSelection(0);
        } else {
            if (mBankDetailsList != null && mBankDetailsList.size() > 0) {
                int count = 0;
                for (BankMasterBO bankBO : mBankDetailsList) {
                    count = count + 1;
                    if (bankBO.getBankId() == SDUtil.convertToInt(mSelectedPaymentBO.getBankID())) {
                        break;
                    }
                }
                mBankSpin.setSelection(count);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == bmodel.CAMERA_REQUEST_CODE && resultCode == 1) {
            mSelectedPaymentBO.setImageName(mImageName);
        }
    }

    private void doConnection() {
        try {
            ZebraPrinter printer = connect();
            if (printer != null) {
                // sendTestLabel();
                printInvoice();
            } else {
                disconnect();
                alertDialog.dismiss();
                Toast.makeText(getActivity(), "Printer not connected .Please check  Mac Address..", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private ZebraPrinter connect() {
        zebraPrinterConnection = null;
        zebraPrinterConnection = new BluetoothConnection(getMacAddressFieldText());
        SettingsHelper.saveBluetoothAddress(getActivity(), getMacAddressFieldText());

        try {
            zebraPrinterConnection.open();
        } catch (ConnectionException e) {
            Commons.printException(e + "");
            DemoSleeper.sleep(1000);
            disconnect();
        } catch (Exception e) {
            Commons.printException(e + "");
        }

        ZebraPrinter printer = null;

        if (zebraPrinterConnection.isConnected()) {
            try {
                printer = ZebraPrinterFactory.getInstance(PrinterLanguage.CPCL,
                        zebraPrinterConnection);

            } catch (ConnectionException e) {
                Commons.printException(e);
            }
        }
        return printer;
    }

    private void printInvoice() {
        try {
            int printDoneCount = ReportHelper.getInstance(getActivity()).getPaymentPrintCount(collectionHelper.collectionGroupId.replace("'", ""));
            for (int i = 0; i <= mSelectedPrintCount; i++) {
                if (i == 0 && printDoneCount == 0)
                    zebraPrinterConnection.write(bmodel.printHelper.printAdvancePayment(true));
                else
                    zebraPrinterConnection.write(bmodel.printHelper.printAdvancePayment(false));
            }

            ReportHelper.getInstance(getActivity()).updatePaymentPrintCount(collectionHelper.collectionGroupId.replace("'", ""), ((mSelectedPrintCount + 1) + printDoneCount));

            DemoSleeper.sleep(1500);
            if (zebraPrinterConnection instanceof BluetoothConnection) {
                DemoSleeper.sleep(500);
            }

            alertDialog.dismiss();
            bmodel.showAlert(getResources().getString(R.string.printed_successfully), 0);
            dismiss();
        } catch (ConnectionException e) {
            Commons.printException(e);

        } catch (Exception e) {
            Commons.printException(e + "");
        } finally {
            disconnect();
        }
    }

    private void disconnect() {
        try {
            if (zebraPrinterConnection != null) {
                zebraPrinterConnection.close();
            }
        } catch (ConnectionException e) {
            Commons.printException(e + "");
        }
    }

    private String getMacAddressFieldText() {
        String macAddress = null;
        try {
            SharedPreferences pref = getActivity().getSharedPreferences("PRINT",
                    Context.MODE_PRIVATE);
            macAddress = pref.getString("MAC", "");
        } catch (Exception e) {
            Commons.printException(e + "");
        }
        return macAddress;
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_advance_payment).setVisible(false);
        menu.findItem(R.id.menu_next).setVisible(false);

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            collectionHelper.setCollectionView(false);
            bmodel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
                    .now(DateTimeUtils.TIME));
            getActivity().finish();
            Intent  myIntent = new Intent(getActivity(), HomeScreenTwo.class);
            startActivityForResult(myIntent, 0);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
