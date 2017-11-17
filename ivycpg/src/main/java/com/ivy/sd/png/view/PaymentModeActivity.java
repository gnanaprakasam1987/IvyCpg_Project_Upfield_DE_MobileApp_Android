package com.ivy.sd.png.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.InvoiceHeaderBO;
import com.ivy.sd.png.bo.PaymentBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.UpdatePaymentByDateInterface;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by nagaganesh.n on 19-04-2017.
 */
public class PaymentModeActivity extends IvyBaseActivityNoActionBar implements UpdatePaymentByDateInterface, DataPickerDialogFragment.UpdateDateInterface,
        ReceiptNoDialogFragment.UpdateReceiptNoInterface, PrintCountDialogFragment.PrintInterface {

    private ArrayList<InvoiceHeaderBO> mInvioceList;
    private ArrayList<PaymentBO> mPaymentList;
    ArrayList<Fragment> mFragmentList;
    private Toolbar toolbar;
    private Bundle bundle;
    private int selectedPosition;
    private String mSelectedCashMode = "";
    private boolean isAdvancePaymentAvailable;
    private int count;
    private BusinessModel bmodel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_mode);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        bundle = getIntent().getExtras();
        if (bundle != null) {
            selectedPosition = bundle.getInt("position");
            isAdvancePaymentAvailable = bundle.getBoolean("IsAdvancePaymentAvailable");
            mSelectedCashMode = bundle.getString("paymode");
        }

        mInvioceList = bmodel.getInvoiceHeaderBO();
        mPaymentList = bmodel.collectionHelper.getCollectionPaymentList();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_payment_mode, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * CA - Cash mode
     * CD - Discount mode
     * CQ - Cheque
     * CN - Credit Note
     * CM - Mobile Payment
     * RTGS - RTGS
     * DD - Demand Draft
     * CP - Coupon Discount
     * AP - Advance Payment
     */
    public enum CaseMODE {
        CA, CD, CQ, CN, CM, RTGS, DD, CP, AP
    }

    @Override
    protected void onResume() {
        count = -1;
        Bundle bundle;
        mFragmentList = new ArrayList<>();
        for (PaymentBO paymentBO : mPaymentList) {
            count++;
            bundle = new Bundle();
            bundle.putInt("position", count);
            bundle.putBoolean("IsAdvancePaymentAvailable", isAdvancePaymentAvailable);
            CaseMODE mode = CaseMODE.valueOf(paymentBO.getCashMode());
            switch (mode) {
                case CA:
                    CashModeFragment cashModeFragment = new CashModeFragment();
                    cashModeFragment.setArguments(bundle);
                    mFragmentList.add(cashModeFragment);
                    break;
                case CQ:
                    CheckModeFragment checkModeFragment = new CheckModeFragment();
                    checkModeFragment.setArguments(bundle);
                    mFragmentList.add(checkModeFragment);
                    break;
                case CN:
                    CreditNoteFragment creditNoteFragment = new CreditNoteFragment();
                    creditNoteFragment.setArguments(bundle);
                    mFragmentList.add(creditNoteFragment);
                    break;
                case CM:
                    CashModeFragment mobilePaymentFragment = new CashModeFragment();
                    mobilePaymentFragment.setArguments(bundle);
                    mFragmentList.add(mobilePaymentFragment);
                    break;
                case CD:
                    CashModeFragment discountFragment = new CashModeFragment();
                    discountFragment.setArguments(bundle);
                    mFragmentList.add(discountFragment);
                    break;
                case CP:
                    CashModeFragment couponFragment = new CashModeFragment();
                    couponFragment.setArguments(bundle);
                    mFragmentList.add(couponFragment);
                    break;
                case DD:
                    CheckModeFragment ddFragment = new CheckModeFragment();
                    ddFragment.setArguments(bundle);
                    mFragmentList.add(ddFragment);
                    break;
                case RTGS:
                    CheckModeFragment rtgsFragment = new CheckModeFragment();
                    rtgsFragment.setArguments(bundle);
                    mFragmentList.add(rtgsFragment);
                    break;
                case AP:
                    AdvancePaymentFragment advancePaymentFragment = new AdvancePaymentFragment();
                    advancePaymentFragment.setArguments(bundle);
                    mFragmentList.add(advancePaymentFragment);
                    break;
            }
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment, mFragmentList.get(selectedPosition));
        ft.commit();
        super.onResume();
    }


    @Override
    public void updatePaymentDetails(String date) {
        updateSelectedList();
        PaymentBO paymentBO = mPaymentList.get(selectedPosition);
        bmodel.collectionHelper.updateCollectionList(mSelecteInvoiceList, paymentBO.getCashMode());
        //updateBottomValuesForSelectedInvoice();
    }

    ArrayList<InvoiceHeaderBO> mSelecteInvoiceList;

    private void updateSelectedList() {
        mSelecteInvoiceList = new ArrayList<>();
        for (InvoiceHeaderBO invoiceHeaderBO : mInvioceList) {
            if (invoiceHeaderBO.isChkBoxChecked()) {
                mSelecteInvoiceList.add(invoiceHeaderBO);
            }
        }
    }

    @Override
    public void updateDate(Date date, String tag) {
        CheckModeFragment checkModeFragment = (CheckModeFragment) mFragmentList.get(selectedPosition);
        if (checkModeFragment != null)
            checkModeFragment.updateDate(date, "");
    }

    @Override
    public void print(int printCount) {

    }

    @Override
    public void dismiss() {

    }

    @Override
    public void updateReceiptNo(String receiptno) {

    }

    /**
     * Callback method used to call number pressed.
     *
     * @param vw
     */
    public void numberPressed(View vw) {
        CaseMODE mode = CaseMODE.valueOf(mSelectedCashMode);
        switch (mode) {
            case CA:
                CashModeFragment cashModeFragment = (CashModeFragment) mFragmentList.get(selectedPosition);
                cashModeFragment.numberPressed(vw);
                break;
            case CQ:
                CheckModeFragment checkModeFragment = (CheckModeFragment) mFragmentList.get(selectedPosition);
                checkModeFragment.numberPressed(vw);
                break;
            case CN:
                CreditNoteFragment creditNoteFragment = (CreditNoteFragment) mFragmentList.get(selectedPosition);
                creditNoteFragment.numberPressed(vw);
                break;
            case CM:
                CashModeFragment cashModeFragment1 = (CashModeFragment) mFragmentList.get(selectedPosition);
                cashModeFragment1.numberPressed(vw);
                break;
            case CD:
                CashModeFragment cashModeFragment2 = (CashModeFragment) mFragmentList.get(selectedPosition);
                cashModeFragment2.numberPressed(vw);
                break;
            case CP:
                CashModeFragment cashModeFragment3 = (CashModeFragment) mFragmentList.get(selectedPosition);
                cashModeFragment3.numberPressed(vw);
                break;
            case DD:
                CheckModeFragment checkModeFragment1 = (CheckModeFragment) mFragmentList.get(selectedPosition);
                checkModeFragment1.numberPressed(vw);
                break;
            case RTGS:
                CheckModeFragment checkModeFragment2 = (CheckModeFragment) mFragmentList.get(selectedPosition);
                checkModeFragment2.numberPressed(vw);
                break;
            case AP:
                AdvancePaymentFragment advancePaymentFragment = (AdvancePaymentFragment) mFragmentList.get(selectedPosition);
                break;
        }
    }
}
