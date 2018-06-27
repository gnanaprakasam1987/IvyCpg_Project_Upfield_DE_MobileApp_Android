package com.ivy.cpg.view.order;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.BankMasterBO;
import com.ivy.sd.png.bo.BranchMasterBO;
import com.ivy.sd.png.bo.CollectionBO;
import com.ivy.sd.png.bo.CreditNoteListBO;
import com.ivy.sd.png.bo.InvoiceHeaderBO;
import com.ivy.sd.png.bo.PaymentBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DateUtil;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.sd.png.view.HomeScreenFragment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class CollectionBeforeInvoiceDialog extends Dialog implements
        android.view.View.OnClickListener {

    // Declare Businness Model Class
    private BusinessModel bmodel;
    // Declare Context
    private Context context;
    // Vairalbes
    private String append = "";
    // Views
    private EditText QUANTITY;
    private OrderSummary orderSummaryActivity;
    private Button btnSubmit, btnDot;
    // Views
    private LinearLayout layoutBankMode, layoutChequeDate, layoutKeypad;
    private RadioGroup rbPaymentType;
    private boolean isClicked, setRadioBtnChecked;
    private EditText collectionamount, chequenumber;
    private TextView img_max_amount, img_min_amount;
    private TextView tvAmount;
    private String paymentmode = "";
    private CollectionBO collectionbo;

    private TextView payTotal, tvMinimumAmount;
    private Spinner Bank, Branch;
    private static Button chequedate;
    private String todayDate;
    private static String chequeDate = "";
    private ArrayList<BranchMasterBO> branchDetails;
    private ArrayList<BankMasterBO> bankDetails;
    private ArrayAdapter<BankMasterBO> bankSpinnerAdapter;
    private ArrayAdapter<BranchMasterBO> branchSpinnerAdapter;
    private String bankID = "0", branchID = "0";
    private static int bankIndex = 0;
    private static int branchIndex = 0;
    private boolean flagOnrestore = false;
    private static String outPutDateFormat;
    private InputMethodManager inputManager;
    private double mTotalInvoiceAmount;
    private double invoiceamount = 0d;
    private double tempAmtCollected = 0d;
    private double amountcollected = 0d;
    private double osamount = 0d;
    private double creditBalance = 0d;
    private String mTransactionPaymentMode = "";
    private PaymentBO pay;

    TextView tv_header_title;
    private RelativeLayout ll;
    private LinearLayout ll_keyboard;
    private Button close_btn;

    ImageView capturecheque;
    private String mImageName;
    private String mImagePath;
    private int mImageCount = 1;

    private String ChequeImgname;
    private ListView mCreditNoteLV;
    private ArrayList<CreditNoteListBO> mCreditNoteList;

    public CollectionBeforeInvoiceDialog(Context context,
                                         OrderSummary orderSummary, CollectionBO collection,
                                         double invoiceAmount, double minimumAmount, double creditBalance) {
        super(context);
        this.context = context;
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        bmodel = (BusinessModel) context.getApplicationContext();
        orderSummaryActivity = orderSummary;
        mTotalInvoiceAmount = invoiceAmount;
        invoiceamount = mTotalInvoiceAmount;
        collectionbo = collection;
        osamount = minimumAmount;
        this.creditBalance = creditBalance;
        ll = (RelativeLayout) LayoutInflater.from(context)
                .inflate(R.layout.dialog_collectionbeforeinvoice, null);
        RelativeLayout ll = (RelativeLayout) LayoutInflater.from(context)
                .inflate(R.layout.dialog_collectionbeforeinvoice, null);
        setContentView(ll);

        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        setCancelable(true);
        // Initialize Views in the Screen
        initializeView();
        inputManager = (InputMethodManager) orderSummaryActivity
                .getSystemService(Context.INPUT_METHOD_SERVICE);


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        todayDate = DateUtil.convertFromServerDateToRequestedFormat(
                SDUtil.now(SDUtil.DATE_GLOBAL),
                bmodel.configurationMasterHelper.outDateFormat);

    }

    @Override
    public void onStart() {

        tvAmount.setText(bmodel.formatValue(mTotalInvoiceAmount) + "");
        tvMinimumAmount.setText((bmodel.formatValue(osamount)) + "");

        if (rbPaymentType != null && rbPaymentType.getChildCount() > 0
                && flagOnrestore == false) {
            modegone();
            ((RadioButton) rbPaymentType.getChildAt(0)).setChecked(true);
        }
        if (!flagOnrestore)
            collectionamount.setText("0");
        else
            flagOnrestore = false;
        if (creditBalance <= 0 || creditBalance < mTotalInvoiceAmount)
            rbPaymentType.removeView(ll.findViewById(R.id.chequeRadioButton));

        fillRadioButton(rbPaymentType);
        viewTouchListener();
        payementTypeListener();
        loadData();
        QUANTITY = collectionamount;
        super.onStart();
    }

    private void updateBranchSpinner() {
        branchSpinnerAdapter = new ArrayAdapter<BranchMasterBO>(
                orderSummaryActivity, android.R.layout.simple_spinner_item);
        branchSpinnerAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        BranchMasterBO mm = new BranchMasterBO();
        mm.setBankID("0");
        mm.setBranchID("0");
        mm.setBranchName(orderSummaryActivity.getResources().getString(
                R.string.sel_branch));
        branchSpinnerAdapter.add(mm);
        int branchSize = branchDetails.size();
        for (int j = 0; j < branchSize; ++j) {
            BranchMasterBO ret = (BranchMasterBO) branchDetails.get(j);
            branchSpinnerAdapter.add(ret);
        }
        Branch.setAdapter(branchSpinnerAdapter);

    }

    private void payementTypeListener() {
        try {
            if (bmodel.collectionHelper.getPaymentModes() == null
                    || bmodel.collectionHelper.getPaymentModes().size() == 0) {
                modeInvisible();
            }
            rbPaymentType
                    .setOnCheckedChangeListener(new OnCheckedChangeListener() {

                        @Override
                        public void onCheckedChanged(RadioGroup group,
                                                     int checkedId) {
                            if (checkedId == R.id.chequeRadioButton) {
                                ll_keyboard.setVisibility(View.VISIBLE);
                                collectionamount.setEnabled(true);
                                if (collectionbo.getCashamt() != 0) {
                                    collectionbo.setCashamt(0);
                                }
                                modegone();
                                modevisibility();
                                mCreditNoteLV.setVisibility(View.GONE);
                                collectionamount.requestFocus();
                                paymentmode = StandardListMasterConstants.CHEQUE;

                                if (collectionbo.getChequeamt() != 0)
                                    collectionamount.setText(BigDecimal
                                            .valueOf(collectionbo
                                                    .getChequeamt())
                                            + "");
                                else
                                    collectionamount.setText("0");

                                QUANTITY = collectionamount;
                                bmodel.collectionHelper.setMradioGroupIndex(rbPaymentType
                                        .indexOfChild(findViewById(checkedId)));

                            } else if (checkedId == R.id.cashRadioButton) {
                                ll_keyboard.setVisibility(View.VISIBLE);
                                collectionamount.setEnabled(true);
                                if (collectionbo.getChequeamt() != 0) {
                                    collectionbo.setChequeamt(0);
                                    Bank.setSelection(0);
                                    Branch.setSelection(0);
                                    chequenumber.setText("");
                                    chequedate.setText(todayDate);
                                }
                                modegone();
                                mCreditNoteLV.setVisibility(View.GONE);
                                collectionamount.requestFocus();
                                paymentmode = StandardListMasterConstants.CASH;

                                if (collectionbo.getCashamt() != 0)
                                    collectionamount.setText(BigDecimal
                                            .valueOf(collectionbo.getCashamt())
                                            + "");
                                else
                                    collectionamount.setText("0");
                                QUANTITY = collectionamount;
                                bmodel.collectionHelper.setMradioGroupIndex(rbPaymentType
                                        .indexOfChild(findViewById(checkedId)));

                            } else if (checkedId == R.id.creditNoteRadioButton) {
                                clearCreditNoteList();
                                modegone();
                                if (mCreditNoteList.size() == 0) {
                                    collectionamount.setEnabled(false);
                                    collectionamount.clearFocus();
                                    if (ll_keyboard != null)
                                        ll_keyboard.setVisibility(View.GONE);
                                    mCreditNoteLV.setVisibility(View.GONE);
                                } else {
                                    mCreditNoteLV.setVisibility(View.VISIBLE);
                                    collectionamount.requestFocus();
                                    ll_keyboard.setVisibility(View.VISIBLE);
                                }

                                if (!bmodel.configurationMasterHelper.IS_PARTIAL_CREDIT_NOTE_ALLOW) {
                                    if (ll_keyboard != null)
                                        ll_keyboard.setVisibility(View.GONE);
                                }

                                collectionamount.requestFocus();
                                paymentmode = StandardListMasterConstants.CREDIT_NOTE;
                                chequenumber.setText("");
                                chequedate.setText(todayDate);
                                if (collectionbo.getCreditamt() != 0)
                                    collectionamount.setText(BigDecimal
                                            .valueOf(collectionbo.getCreditamt())
                                            + "");
                                else
                                    collectionamount.setText("0");
                                QUANTITY = collectionamount;
                                bmodel.collectionHelper.setMradioGroupIndex(rbPaymentType
                                        .indexOfChild(findViewById(checkedId)));
                                CreditNoteAdapter creditNoteAdapter = new CreditNoteAdapter();
                                mCreditNoteLV.setAdapter(creditNoteAdapter);
                                updateCreditNoteTotal();

                            }
                        }
                    });
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private void viewTouchListener() {
        try {
            chequenumber.setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    ll_keyboard.setVisibility(View.GONE);
                    QUANTITY = chequenumber;
                    int inType = chequenumber.getInputType();
                    chequenumber.setInputType(InputType.TYPE_NULL);
                    chequenumber.onTouchEvent(event);
                    chequenumber.setInputType(inType);

                    inputManager.showSoftInput(
                            chequenumber, InputMethodManager.SHOW_FORCED);
                    chequenumber.requestFocus();
                    return true;
                }
            });

            chequenumber.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    QUANTITY = chequenumber;

                }
            });

            collectionamount.setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    QUANTITY = collectionamount;
                    int inType = collectionamount.getInputType();
                    collectionamount.setInputType(InputType.TYPE_NULL);
                    collectionamount.onTouchEvent(event);
                    collectionamount.setInputType(inType);
                    collectionamount.selectAll();
                    collectionamount.requestFocus();
                    inputManager.hideSoftInputFromWindow(
                            collectionamount.getWindowToken(), 0);
                    ll_keyboard.setVisibility(View.VISIBLE);
                    return true;
                }
            });

            collectionamount.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    QUANTITY = collectionamount;
                }
            });
            collectionamount.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    try {
                        String qty = s.toString();
                        if (!qty.equals("")) {
                            if (paymentmode
                                    .equals(StandardListMasterConstants.CHEQUE))
                                collectionbo.setChequeamt(SDUtil
                                        .convertToDouble(qty));
                            else if (paymentmode
                                    .equals(StandardListMasterConstants.DISCOUNT))
                                collectionbo.setDiscountamt(SDUtil
                                        .convertToDouble(qty));

                            else if (paymentmode
                                    .equals(StandardListMasterConstants.MOBILE_PAYMENT))
                                collectionbo.setMobilePaymentamt(SDUtil
                                        .convertToDouble(qty));
                            else if (paymentmode.equals(StandardListMasterConstants.CREDIT_NOTE)) {
                                double enteredValue = SDUtil.convertToDouble(qty);
                                if (!isCreditAmountExceed(enteredValue) && enteredValue <= mTotalInvoiceAmount) {
                                    collectionbo.setCreditamt(enteredValue);
                                } else {
                                    qty = qty.length() > 1 ? qty.substring(0,
                                            qty.length() - 1) : "0";
                                    collectionamount.setText(qty);
                                    Toast.makeText(context, "Enter Amount Exceed ", Toast.LENGTH_SHORT).show();
                                }


                            } else
                                collectionbo.setCashamt(SDUtil
                                        .convertToDouble(qty));

                            payTotal.setText(bmodel.formatValue((collectionbo
                                    .getCashamt()
                                    + collectionbo.getChequeamt()
                                    + collectionbo.getCreditamt()
                                    + collectionbo.getDiscountamt() + collectionbo
                                    .getMobilePaymentamt())));
                        }
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                }
            });

            chequedate.setText(todayDate);
            chequeDate = todayDate;
            chequedate.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (!isClicked) {
                        Button btn = (Button) v;
                        if (btn == chequedate) {
                            DialogFragment newFragment = new DatePickerFragment();
                            newFragment.show(orderSummaryActivity
                                    .getSupportFragmentManager(), "datePicker");
                        }
                    }
                }
            });
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    /**
     * load data/view Bank, Branch, Invoice List View
     */
    private void loadData() {
        bmodel.downloadInvoice(bmodel.getRetailerMasterBO().getRetailerID(), "COL");
        ArrayList<InvoiceHeaderBO> items = bmodel.getInvoiceHeaderBO();
        if (items == null) {
            bmodel.showAlert(
                    orderSummaryActivity.getResources().getString(
                            R.string.no_products_exists), 0);
            return;
        }

        // constructTable();
        bankDetails = bmodel.collectionHelper.getBankMasterBO();
        branchDetails = bmodel.collectionHelper.getBranchMasterBO();

        bankSpinnerAdapter = new ArrayAdapter<BankMasterBO>(
                orderSummaryActivity, android.R.layout.simple_spinner_item);
        bankSpinnerAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        BankMasterBO mm = new BankMasterBO();
        mm.setBankId(0);
        mm.setBankName(orderSummaryActivity.getResources().getString(
                R.string.sel_bank));
        bankSpinnerAdapter.add(mm);
        int size = bankDetails.size();
        for (int i = 0; i < size; ++i) {
            BankMasterBO ret = (BankMasterBO) bankDetails.get(i);
            bankSpinnerAdapter.add(ret);
        }
        Bank.setAdapter(bankSpinnerAdapter);
        Bank.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

                BankMasterBO bank = (BankMasterBO) Bank.getSelectedItem();
                Branch.setEnabled(false);
                if (bank.getBankId() == 0) {
                    Branch.setEnabled(false);
                    bankIndex = position;
                    Branch.setSelection(0);
                    collectionbo.setBankId("0");
                    collectionbo.setBranchId("0");
                } else {
                    bankIndex = position;
                    bankID = String.valueOf(bank.getBankId());
                    Branch.setEnabled(true);
                    collectionbo.setBankId(String.valueOf(bank.getBankId()));
                    updateBranchSpinner(bankID + "");
                }

            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Branch.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

                BranchMasterBO branch = (BranchMasterBO) Branch
                        .getSelectedItem();
                if (branch.getBranchID().equals("0")) {
                    branchIndex = position;
                    collectionbo.setBranchId("0");
                } else {
                    branchIndex = position;
                    branchID = branch.getBranchID();
                    collectionbo.setBranchId(branchID);
                }

            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        String modeID = bmodel.getStandardListIdAndType(
                "CNAP",
                StandardListMasterConstants.CREDIT_NOTE_TYPE);
        if (bmodel.collectionHelper.getCreditNoteList() != null) {
            mCreditNoteList = new ArrayList<CreditNoteListBO>();
            for (CreditNoteListBO bo : bmodel.collectionHelper
                    .getCreditNoteList()) {
                if (bo.getRetailerId().equals(
                        bmodel.getRetailerMasterBO().getRetailerID())
                        && !bo.isUsed() && (!modeID.equals(bo.getTypeId() + "")))
                    mCreditNoteList.add(bo);
            }
            if (mCreditNoteList != null && mCreditNoteList.size() > 0) {
                CreditNoteAdapter creditNoteAdapter = new CreditNoteAdapter();
                mCreditNoteLV.setAdapter(creditNoteAdapter);


            }


        }
        List<PaymentBO> payment = bmodel.collectionHelper.getPaymentList();
        if (payment != null && payment.size() > 0) {
            ((RadioButton) rbPaymentType
                    .getChildAt(bmodel.collectionHelper.getMradioGroupIndex()))
                    .setChecked(true);
            if (collectionbo.getChequeamt() > 0) {
                collectionamount.setText(payment.get(0).getAmount() + "");
                chequenumber.setText(payment.get(0).getChequeNumber());
                chequedate.setText(DateUtil.convertFromServerDateToRequestedFormat(payment
                        .get(0).getChequeDate(), outPutDateFormat));
                // Bank.setSelection(getBankIndex(payment.get(0).getBankID()));
                Bank.setSelection(bankIndex);
                updateBranchSpinner();
                Commons.print("Branch Index:"
                        + getBranchIndex(payment.get(0).getBranchId()));
                Branch.setSelection(1);
            } else if (collectionbo.getCashamt() > 0) {
                collectionamount.setText(payment.get(0).getAmount() + "");
            } else if (collectionbo.getCreditamt() > 0) {
                collectionamount.setText(payment.get(0).getAmount() + "");
            }

        }


        CreditNoteAdapter adapter = new CreditNoteAdapter();
        mCreditNoteLV.setAdapter(adapter);

    }


    private int getBranchIndex(String bankId) {
        if (branchSpinnerAdapter.getCount() == 0)
            return 0;
        int len = branchSpinnerAdapter.getCount();
        if (len == 0)
            return 0;
        for (int i = 0; i < len; ++i) {
            BranchMasterBO s = (BranchMasterBO) branchSpinnerAdapter.getItem(i);
            if (s.getBranchID().equals(bankId)) {
                return i;
            }
        }
        return -1;
    }

    @SuppressLint("ValidFragment")
    public class DatePickerFragment extends DialogFragment implements
            DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), R.style.DatePickerDialogStyle, this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            Calendar selectedDate = new GregorianCalendar(year, month, day);
            chequedate.setText(DateUtil.convertDateObjectToRequestedFormat(
                    selectedDate.getTime(), outPutDateFormat));
            chequeDate = chequedate.getText().toString();
            Calendar currentcal = Calendar.getInstance();
            if (!bmodel.configurationMasterHelper.IS_POST_DATE_ALLOW) {
                if (selectedDate.after(currentcal)) {
                    Toast.makeText(
                            getActivity().getApplicationContext(),
                            getResources().getString(
                                    R.string.post_dated_cheque_notallow),
                            Toast.LENGTH_SHORT).show();
                    chequedate.setText(DateUtil.convertDateObjectToRequestedFormat(
                            currentcal.getTime(), outPutDateFormat));
                    chequeDate = chequedate.getText().toString();

                }
            }
        }
    }

    /**
     * Update branch for corresponding bank id
     *
     * @param bankID
     */
    private void updateBranchSpinner(String bankID) {
        branchSpinnerAdapter = new ArrayAdapter<BranchMasterBO>(
                orderSummaryActivity, android.R.layout.simple_spinner_item);
        branchSpinnerAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        BranchMasterBO mm = new BranchMasterBO();
        mm.setBranchID("0");
        mm.setBranchName(orderSummaryActivity.getResources().getString(
                R.string.sel_bank));
        branchSpinnerAdapter.add(mm);
        int branchSize = branchDetails.size();
        for (int j = 0; j < branchSize; ++j) {
            BranchMasterBO ret = (BranchMasterBO) branchDetails.get(j);
            if (ret.getBankID().equals(bankID)) {
                branchSpinnerAdapter.add(ret);
            }
        }
        Branch.setAdapter(branchSpinnerAdapter);
    }

    /**
     * Fill Payment Mode Dynamically
     *
     * @param rb
     */
    private void fillRadioButton(RadioGroup rb) {
        for (int i = 0; i < rb.getChildCount(); i++) {
            for (StandardListBO sbo : bmodel.collectionHelper
                    .getPaymentModes()) {
                RadioButton radioBut = (RadioButton) rb.getChildAt(i);
                if (radioBut.getTag().equals(sbo.getListCode())) {
                    radioBut.setVisibility(View.VISIBLE);
                    radioBut.setText(sbo.getListName());
                    if (setRadioBtnChecked) {
                        radioBut.setChecked(true);
                        setRadioBtnChecked = false;
                    }
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        Button b = (Button) v;
        if (b == btnSubmit) {
            amountcollected = collectionbo.getCashamt()
                    + collectionbo.getChequeamt()
                    + collectionbo.getDiscountamt()
                    + collectionbo.getCreditamt()
                    + collectionbo.getMobilePaymentamt();
            tempAmtCollected = amountcollected;

            if (validate() == false)
                return;

            else {
                amountcollected = SDUtil.truncateDecimal(amountcollected,
                        bmodel.configurationMasterHelper.VALUE_PRECISION_COUNT)
                        .doubleValue();
                if (osamount != 0) {

                    if (osamount > amountcollected) {
                        Toast.makeText(
                                orderSummaryActivity,
                                orderSummaryActivity.getResources().getString(
                                        R.string.enter_minimum_amount),

                                Toast.LENGTH_SHORT).show();
                    } else if (amountcollected > mTotalInvoiceAmount) {
                        Toast.makeText(
                                orderSummaryActivity,
                                orderSummaryActivity
                                        .getResources()
                                        .getString(
                                                R.string.amount_exeeds_the_balance_please_check),

                                Toast.LENGTH_SHORT).show();
                    } else {
                        doPaymentObjectList();
                        this.orderSummaryActivity.onResume();
                        dismiss();
                    }
                } else if (amountcollected > mTotalInvoiceAmount) {
                    Toast.makeText(
                            orderSummaryActivity,
                            orderSummaryActivity
                                    .getResources()
                                    .getString(
                                            R.string.amount_exeeds_the_balance_please_check),

                            Toast.LENGTH_SHORT).show();
                } else {
                    doPaymentObjectList();
                    this.orderSummaryActivity.onResume();
                    dismiss();

                }
            }

        }

    }

    /**
     * After Collection Save hit, call for payment object list
     */
    private void doPaymentObjectList() {
        bmodel.collectionHelper.getPaymentList().clear();
        mTransactionPaymentMode = StandardListMasterConstants.COLLECTION_NORMAL_PAYMENT;
        if (collectionbo.getCashamt() > 0) {

            setPaymentObject(invoiceamount, collectionbo.getCashamt(), "", "",
                    "", "", StandardListMasterConstants.CASH,
                    mTransactionPaymentMode, "");
        }

        if (collectionbo.getChequeamt() > 0) {

            setPaymentObject(
                    invoiceamount,
                    collectionbo.getChequeamt(),
                    collectionbo.getBankId(),
                    collectionbo.getBranchId(),
                    DateUtil.convertToServerDateFormat(chequedate.getText()
                                    + "",
                            bmodel.configurationMasterHelper.outDateFormat)
                            + "", chequenumber.getText().toString(),
                    StandardListMasterConstants.CHEQUE, mTransactionPaymentMode, ChequeImgname);

        }
        if (collectionbo.getCreditamt() > 0) {
            setPaymentObject(invoiceamount, collectionbo.getCreditamt(), "", "",
                    "", "", StandardListMasterConstants.CREDIT_NOTE,
                    mTransactionPaymentMode, "");

        }
    }

    private void setPaymentObject(double invoiceAmt, double paidAmt,
                                  String bankId, String branchId, String chequeDate, String chequeNo,
                                  String mode, String paymentMode, String mImageName) {
        try {
            boolean isCreditBalanceCheck = bmodel.collectionHelper.isCreditBalancebalance(mode);
            pay = new PaymentBO();
            pay.setInvoiceAmount(invoiceAmt);
            pay.setBankID(bankId);
            pay.setBranchId(branchId);
            pay.setBeatID(bmodel.getRetailerMasterBO().getBeatID() + "");
            pay.setUserId(bmodel.userMasterHelper.getUserMasterBO().getUserid()
                    + "");
            pay.setChequeDate(chequeDate);
            pay.setChequeNumber(chequeNo);
            pay.setAmount(paidAmt);

            pay.setCashMode(mode);
            pay.setPaymentTransactioMode(paymentMode);
            pay.setCreditBalancePayment(isCreditBalanceCheck);
            pay.setImageName(mImageName);
            pay.setUpdatePayableamt(paidAmt);
            bmodel.collectionHelper.getPaymentList().add(pay);
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    /**
     * Validation Checks
     *
     * @return true/false
     */
    private boolean validate() {
        boolean ok = true;

        if (tempAmtCollected == 0) {
            Toast.makeText(
                    orderSummaryActivity,
                    orderSummaryActivity.getResources().getString(
                            R.string.enter_amount), Toast.LENGTH_SHORT).show();
            ok = false;
        } else if (collectionbo.getChequeamt() > 0
                && TextUtils.isEmpty(chequenumber.getText().toString())) {
            Toast.makeText(
                    orderSummaryActivity,
                    orderSummaryActivity.getResources().getString(
                            R.string.enter_cheque_no), Toast.LENGTH_SHORT)
                    .show();
            ok = false;
        } else if (collectionbo.getChequeamt() > 0
                && collectionbo.getBankId().equals("0")) {
            Toast.makeText(
                    orderSummaryActivity,
                    orderSummaryActivity.getResources().getString(
                            R.string.sel_bank), Toast.LENGTH_SHORT).show();
            ok = false;
        } else if (collectionbo.getChequeamt() > 0
                && collectionbo.getBranchId().equals("0")) {
            Toast.makeText(
                    orderSummaryActivity,
                    orderSummaryActivity.getResources().getString(
                            R.string.sel_branch), Toast.LENGTH_SHORT).show();
            ok = false;
        } else if (collectionbo.getCreditamt() > 0 && isCreditAmountExceed(amountcollected)) {
            Toast.makeText(orderSummaryActivity, "Enter Amount exceed CreditNote Amount", Toast.LENGTH_SHORT).show();
            ok = false;

        }
        return ok;
    }

    private void modegone() {
        layoutBankMode.setVisibility(View.GONE);
        layoutChequeDate.setVisibility(View.GONE);
        capturecheque.setVisibility(View.GONE);
        layoutKeypad.setVisibility(View.VISIBLE);
    }

    private void modevisibility() {
        layoutBankMode.setVisibility(View.VISIBLE);
        layoutChequeDate.setVisibility(View.VISIBLE);
        capturecheque.setVisibility(View.VISIBLE);
    }

    private void modeInvisible() {
        chequenumber.setVisibility(View.GONE);
        collectionamount.setVisibility(View.GONE);
        chequedate.setVisibility(View.GONE);
        Bank.setVisibility(View.GONE);
        Branch.setVisibility(View.GONE);
        layoutBankMode.setVisibility(View.GONE);
        layoutChequeDate.setVisibility(View.GONE);
        payTotal.setVisibility(View.GONE);
        capturecheque.setVisibility(View.GONE);
    }

    private void initializeView() {
        try {
            btnSubmit = (Button) findViewById(R.id.btnsubmit);
            tvAmount = (TextView) findViewById(R.id.tv_amount);
            rbPaymentType = (RadioGroup) findViewById(R.id.chequeorcash);
            collectionamount = (EditText) findViewById(R.id.collectionAmount);
            layoutBankMode = (LinearLayout) findViewById(R.id.mode);
            layoutChequeDate = (LinearLayout) findViewById(R.id.ccdate);
            layoutKeypad = (LinearLayout) findViewById(R.id.keypad);
            chequenumber = (EditText) findViewById(R.id.collectionchequeNo);
            payTotal = (TextView) findViewById(R.id.payTotal);
            img_max_amount = (TextView) findViewById(R.id.img_max_amount);
            chequedate = (Button) findViewById(R.id.collectionDate);
            Bank = (Spinner) findViewById(R.id.bankName);
            Branch = (Spinner) findViewById(R.id.bankArea);
            btnDot = (Button) findViewById(R.id.calcdot);
            btnDot.setVisibility(View.VISIBLE);
            btnSubmit.setOnClickListener(this);
            tvMinimumAmount = (TextView) findViewById(R.id.tv_minimum_amount);
            img_min_amount = (TextView) findViewById(R.id.img_min_amount);
            mCreditNoteLV = (ListView) findViewById(R.id.lv_creditnote);
            ll_keyboard = (LinearLayout) findViewById(R.id.footer);


            tv_header_title = findViewById(R.id.titlebar);
            close_btn = (Button) findViewById(R.id.closeButton);

            tv_header_title.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
            ((TextView) findViewById(R.id.productName2)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            ((TextView) findViewById(R.id.minimumamount)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            ((TextView) findViewById(R.id.mode_title)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            ((TextView) findViewById(R.id.tv_branch_title)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
            ((TextView) findViewById(R.id.tv_bank_title)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
            ((TextView) findViewById(R.id.totalLabel)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
            ((TextView) findViewById(R.id.payTotal)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
            ((RadioButton) findViewById(R.id.cashRadioButton)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
            ((RadioButton) findViewById(R.id.chequeRadioButton)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
            ((RadioButton) findViewById(R.id.creditNoteRadioButton)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
            tvMinimumAmount.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
            tvAmount.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));

            btnSubmit.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
            if (bmodel.configurationMasterHelper.COLL_CHEQUE_MODE) {
                chequenumber.setInputType(InputType.TYPE_CLASS_TEXT);
            }

            close_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        if (orderSummaryActivity != null)
                            CollectionBeforeInvoiceDialog.this.orderSummaryActivity
                                    .onResume();
                        dismiss();
                }
            });

            /*
             * collectionamount.setText(BigDecimal.valueOf(collectionbo
			 * .getCashamt()) + "");
			 */

            img_max_amount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    collectionamount.setText(mTotalInvoiceAmount + "");
                }
            });

            img_min_amount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    collectionamount.setText(osamount + "");
                }
            });

            capturecheque = (ImageView) findViewById(R.id.capturecheque);
            capturecheque.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String fnameStarts = null;
                    boolean nfiles_there;
                    if (bmodel.isExternalStorageAvailable()) {

                        if (paymentmode
                                .equals(StandardListMasterConstants.CHEQUE)) {
                            mImageName = "COL_CHQ_"
                                    + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                                    + "_" + Commons.now(Commons.DATE_TIME)
                                    + "_img.jpg";

                            fnameStarts = "COL_CHQ_"
                                    + bmodel.getRetailerMasterBO().getRetailerID()
                                    + "_" + Commons.now(Commons.DATE);
                        } else if (paymentmode
                                .equals(StandardListMasterConstants.DEMAND_DRAFT)) {
                            mImageName = "COL_DD_"
                                    + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                                    + "_" + Commons.now(Commons.DATE_TIME)
                                    + "_img.jpg";

                            fnameStarts = "COL_DD_"
                                    + bmodel.getRetailerMasterBO().getRetailerID()
                                    + "_" + Commons.now(Commons.DATE);
                        }


                        nfiles_there = bmodel.checkForNFilesInFolder(
                                HomeScreenFragment.photoPath, mImageCount, fnameStarts);
                        if (nfiles_there) {
                            showFileDeleteAlert(fnameStarts);
                            return;
                        } else {
                            if (paymentmode
                                    .equals(StandardListMasterConstants.CHEQUE)) {
                                ChequeImgname = mImageName;
                            }
                            Intent intent = new Intent(context, CameraActivity.class);
                            intent.putExtra("quality", 40);
                            mImagePath = "Collection" + "/" + bmodel.userMasterHelper.getUserMasterBO
                                    ().getDownloadDate().replace("/", "")
                                    + "/"
                                    + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                                    + "/";
                            mImageName = mImagePath + mImageName;
                            String path = HomeScreenFragment.photoPath + "/" + mImageName;

                            Commons.print("photoPath : " + mImagePath);
                            intent.putExtra("path", path);
                            orderSummaryActivity.startActivityForResult(intent,
                                    bmodel.CAMERA_REQUEST_CODE);
                            return;
                        }

                    } else {
                        Toast.makeText(context,
                                R.string.sdcard_is_not_ready_to_capture_img,
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public void showFileDeleteAlert(final String imageNameStarts) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("");
        builder.setMessage(orderSummaryActivity.getResources().getString(R.string.word_already)
                + mImageCount
                + orderSummaryActivity.getResources().getString(
                R.string.word_photocaptured_delete_retake));

        builder.setPositiveButton(orderSummaryActivity.getResources().getString(R.string.yes),
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        bmodel.deleteFiles(HomeScreenFragment.photoPath,
                                imageNameStarts);
                        dialog.dismiss();
                        Intent intent = new Intent(context,
                                CameraActivity.class);
                        intent.putExtra("quality", 40);
                        String _path = HomeScreenFragment.photoPath + "/" + mImageName;
                        Commons.print("PhotoPAth:  -      " + _path);
                        intent.putExtra("path", _path);
                        orderSummaryActivity.startActivityForResult(intent,
                                bmodel.CAMERA_REQUEST_CODE);
                        return;
                    }
                });

        builder.setNegativeButton(orderSummaryActivity.getResources().getString(R.string.no),
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which)


                    {
                        dialog.dismiss();
                        return;
                    }
                });

        builder.setCancelable(false);
        bmodel.applyAlertDialogTheme(builder);
    }

    public void eff() {
        String s = (String) QUANTITY.getText().toString();
        if (!s.equals(orderSummaryActivity.getResources().getString(
                R.string.zero))) {
            if (s.indexOf(".") == -1 ? false : (s.substring(s.indexOf("."),
                    s.length()).length()) > 2 ? true : false) {
                Toast.makeText(
                        orderSummaryActivity,
                        orderSummaryActivity.getResources().getString(
                                R.string.only_upto_two_digit),
                        Toast.LENGTH_SHORT).show();
                return;
            } else
                QUANTITY.setText(QUANTITY.getText() + append);
        } else {
            QUANTITY.setText(append);
        }
        QUANTITY.setSelection(QUANTITY.getText().toString().length());
    }

    public void numberPressed(View v) {
        if (QUANTITY == null) {
            bmodel.showAlert(
                    orderSummaryActivity.getResources().getString(
                            R.string.please_select_item), 0);
        } else if (QUANTITY.equals(chequenumber)) {
            textPressed(v);
        } else {
            amountPressed(v);
        }
    }

    public void amountPressed(View vw) {
        if (QUANTITY == null) {
            bmodel.showAlert(
                    orderSummaryActivity.getResources().getString(
                            R.string.please_select_item), 0);
        } else {
            int id = vw.getId();
            if (id == R.id.calcone) {
                append = "1";
                eff();
            } else if (id == R.id.calctwo) {
                append = "2";
                eff();
            } else if (id == R.id.calcthree) {
                append = "3";
                eff();
            } else if (id == R.id.calcfour) {
                append = "4";
                eff();
            } else if (id == R.id.calcfive) {
                append = "5";
                eff();
            } else if (id == R.id.calcsix) {
                append = "6";
                eff();
            } else if (id == R.id.calcseven) {
                append = "7";
                eff();
            } else if (id == R.id.calceight) {
                append = "8";
                eff();
            } else if (id == R.id.calcnine) {
                append = "9";
                eff();
            } else if (id == R.id.calczero) {
                append = "0";
                eff();
            } else if (id == R.id.calcdel) {

                long s = SDUtil.convertToLong(QUANTITY.getText()
                        .toString());
                if (s == -1) {
                    String subStr = QUANTITY
                            .getText()
                            .toString()
                            .substring(
                                    0,
                                    (QUANTITY.getText().toString().length() - 1));
                    if ((subStr.charAt(subStr.length() - 1)) == '.')
                        subStr = subStr.substring(0, (subStr.length() - 1));
                    if (subStr.equals("-"))
                        subStr = "0";
                    if (subStr.equals("."))
                        subStr = "0";
                    if (subStr.equals(""))
                        subStr = "0";
                    QUANTITY.setText(subStr);
                } else {
                    s = s / 10;
                    QUANTITY.setText(s + "");
                    QUANTITY.setSelection(QUANTITY.getText().toString()
                            .length());
                }
            } else if (id == R.id.calcdot) {
                String s = QUANTITY.getText().toString();
                if (s != null) {
                    if (!s.contains(".")) {
                        QUANTITY.setText(s + ".");// QUANTITY.append(".");
                    }
                }
            }
        }

    }

    public void eff1() {
        QUANTITY.setText(QUANTITY.getText() + append);
        QUANTITY.setSelection(QUANTITY.getText().toString().length());
    }

    public void textPressed(View vw) {
        if (QUANTITY == null) {
            bmodel.showAlert(
                    orderSummaryActivity.getResources().getString(
                            R.string.please_select_item), 0);
        } else {
            int id = vw.getId();
            if (id == R.id.calcone) {
                append = "1";
                eff1();
            } else if (id == R.id.calctwo) {
                append = "2";
                eff1();
            } else if (id == R.id.calcthree) {
                append = "3";
                eff1();
            } else if (id == R.id.calcfour) {
                append = "4";
                eff1();
            } else if (id == R.id.calcfive) {
                append = "5";
                eff1();
            } else if (id == R.id.calcsix) {
                append = "6";
                eff1();
            } else if (id == R.id.calcseven) {
                append = "7";
                eff1();
            } else if (id == R.id.calceight) {
                append = "8";
                eff1();
            } else if (id == R.id.calcnine) {
                append = "9";
                eff1();
            } else if (id == R.id.calczero) {
                append = "0";
                eff1();
            } else if (id == R.id.calcdel) {
                // int s = Integer
                // .parseInt((String) QUANTITY.getText().toString());
                // s = s / 10;
                String text = (String) QUANTITY.getText().toString();
                if (text.length() > 0)
                    QUANTITY.setText(text.substring(0, text.length() - 1));
                QUANTITY.setSelection(QUANTITY.getText().toString().length());
            }
        }

    }

    /**
     * Method to check haspayment status
     * and allow collection by using CB only or not
     */
    private boolean checkHasPaymentIssue(String mode) {
        if (bmodel.getRetailerMasterBO().getHasPaymentIssue() == 1) {
            boolean isCBType = bmodel.collectionHelper.isCreditBalancebalance(mode);
            if (!isCBType) {

                return false;
            }
        }
        return true;
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
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(R.layout.row_credit_note, parent, false);
                holder = new ViewHolder();

                holder.refNoTxt = (TextView) row.findViewById(R.id.refNoTxt);
                holder.crdNoteAmtTxt = (TextView) row
                        .findViewById(R.id.crdNoteAmtTxt);
                holder.creditNoteCheckBox = (CheckBox) row
                        .findViewById(R.id.creditNoteCheckBox);


                holder.creditNoteCheckBox
                        .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                            @Override
                            public void onCheckedChanged(
                                    CompoundButton buttonView, boolean isChecked) {
                                holder.creditNoteListBO.setChecked(isChecked);
                                if (isChecked) {
                                    final int amountExceed = isAmoutnExceed();

                                    if (!bmodel.configurationMasterHelper.IS_PARTIAL_CREDIT_NOTE_ALLOW && amountExceed == 1) {
                                        holder.creditNoteCheckBox.setChecked(false);
                                        holder.creditNoteListBO.setChecked(false);
                                        Toast.makeText(
                                                context,
                                                context.getResources()
                                                        .getString(
                                                                R.string.amount_exeeds_the_balance_please_check),
                                                Toast.LENGTH_SHORT).show();
                                    }


                                }
                                updateCreditNoteTotal();

                            }

                        });

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.creditNoteListBO = mCreditNoteList.get(position);
            holder.refNoTxt.setText(holder.creditNoteListBO.getRefno() + "");
            holder.crdNoteAmtTxt.setText(bmodel.formatValue(holder.creditNoteListBO
                    .getAmount()) + "");
            holder.creditNoteCheckBox.setChecked(holder.creditNoteListBO.isChecked());
            String totCrdeitAmt = bmodel.formatValue(holder.creditNoteListBO.getAmount() + holder.creditNoteListBO.getAppliedAmount());
            holder.totalCreditNoteAmountTxt.setText(totCrdeitAmt);
            return (row);
        }
    }

    class ViewHolder {
        private TextView refNoTxt, crdNoteAmtTxt, totalCreditNoteAmountTxt;
        private CheckBox creditNoteCheckBox;
        private CreditNoteListBO creditNoteListBO;
    }

    private int isAmoutnExceed() {
        double sum = 0;
        if (mCreditNoteList != null) {
            for (CreditNoteListBO creditNoteListBO : mCreditNoteList) {
                if (creditNoteListBO.isChecked())
                    sum = sum + creditNoteListBO.getAmount();
            }
        }
        if (sum > mTotalInvoiceAmount) {
            return 1;
        } else if (sum < mTotalInvoiceAmount) {
            return -1;
        } else return 0;


    }

    private void updateCreditNoteTotal() {
        double sum = 0;
        if (mCreditNoteList != null) {
            for (CreditNoteListBO creditNoteListBO : mCreditNoteList) {
                if (creditNoteListBO.isChecked())
                    sum = sum + creditNoteListBO.getAmount();
            }
        }
        final int result = isAmoutnExceed();
        if (result == 0 || result == 1) {
            collectionamount.setText(mTotalInvoiceAmount + "");
        } else if (result == -1) {
            collectionamount.setText(sum + "");
        }


    }

    private boolean isCreditAmountExceed(double amountcollected) {

        double sum = 0;
        if (mCreditNoteList != null) {
            for (CreditNoteListBO creditNoteListBO : mCreditNoteList) {
                if (creditNoteListBO.isChecked())
                    sum = sum + creditNoteListBO.getAmount();
            }
        }
        if (amountcollected > sum) {
            return true;
        }
        return false;

    }

    private void clearCreditNoteList() {
        if (mCreditNoteList != null) {
            for (CreditNoteListBO creditNoteListBO : mCreditNoteList) {
                creditNoteListBO.setChecked(false);
            }
        }
    }

}
