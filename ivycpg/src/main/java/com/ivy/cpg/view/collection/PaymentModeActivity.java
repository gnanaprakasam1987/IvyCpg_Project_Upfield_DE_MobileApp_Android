package com.ivy.cpg.view.collection;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.InvoiceHeaderBO;
import com.ivy.sd.png.bo.PaymentBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.UpdatePaymentByDateInterface;
import com.ivy.sd.png.view.DataPickerDialogFragment;

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
    private int selectedPosition;
    private String mSelectedCashMode = "";
    private boolean isAdvancePaymentAvailable;
    private boolean isFromColletion = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_mode);

        Toolbar toolbar = findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        BusinessModel bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            selectedPosition = bundle.getInt("position");
            isAdvancePaymentAvailable = bundle.getBoolean("IsAdvancePaymentAvailable");
            mSelectedCashMode = bundle.getString("paymode");
            isFromColletion = bundle.getBoolean("FromCollection",false);
        }

        mInvioceList = bmodel.getInvoiceHeaderBO();
        mPaymentList = CollectionHelper.getInstance(this).getCollectionPaymentList();
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
        int count = -1;
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
                    ChequeModeFragment chequeModeFragment = new ChequeModeFragment();
                    chequeModeFragment.setArguments(bundle);
                    mFragmentList.add(chequeModeFragment);
                    break;
                case CN:
                    CreditNoteFragment creditNoteFragment = new CreditNoteFragment();
                    bundle.putBoolean("FromCollection",isFromColletion);
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
                    ChequeModeFragment ddFragment = new ChequeModeFragment();
                    ddFragment.setArguments(bundle);
                    mFragmentList.add(ddFragment);
                    break;
                case RTGS:
                    ChequeModeFragment rtgsFragment = new ChequeModeFragment();
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

        if (!isFinishing()) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment, mFragmentList.get(selectedPosition));
            ft.commitAllowingStateLoss();
        }
        super.onResume();
    }


    @Override
    public void updatePaymentDetails(String date) {
        updateSelectedList();
        PaymentBO paymentBO = mPaymentList.get(selectedPosition);
        CollectionHelper.getInstance(this).updateCollectionList(mSelecteInvoiceList, paymentBO.getCashMode());
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
        ChequeModeFragment chequeModeFragment = (ChequeModeFragment) mFragmentList.get(selectedPosition);
        if (chequeModeFragment != null)
            chequeModeFragment.updateDate(date, "");
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
                ChequeModeFragment chequeModeFragment = (ChequeModeFragment) mFragmentList.get(selectedPosition);
                chequeModeFragment.numberPressed(vw);
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
                ChequeModeFragment chequeModeFragment1 = (ChequeModeFragment) mFragmentList.get(selectedPosition);
                chequeModeFragment1.numberPressed(vw);
                break;
            case RTGS:
                ChequeModeFragment chequeModeFragment2 = (ChequeModeFragment) mFragmentList.get(selectedPosition);
                chequeModeFragment2.numberPressed(vw);
                break;
            case AP:
                AdvancePaymentFragment advancePaymentFragment = (AdvancePaymentFragment) mFragmentList.get(selectedPosition);
                break;
        }
    }
}
