package com.ivy.sd.png.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.aem.api.AEMPrinter;
import com.aem.api.AEMScrybeDevice;
import com.bixolon.printer.BixolonPrinter;
import com.ivy.cpg.view.collection.NoCollectionReasonActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.CreditNoteListBO;
import com.ivy.sd.png.bo.InvoiceHeaderBO;
import com.ivy.sd.png.bo.PaymentBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.DateUtil;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.sd.print.DemoSleeper;
import com.ivy.sd.print.ScribePrinter;
import com.ivy.sd.print.SettingsHelper;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

public class CollectionFragmentNew extends IvyBaseFragment
        implements DataPickerDialogFragment.UpdateDateInterface,
        ReceiptNoDialogFragment.UpdateReceiptNoInterface,
        PrintCountDialogFragment.PrintInterface {

    private static final String TAG = "CollectionFragmentNew";
    private BusinessModel bmodel;
    private ListView mCollectionLV;
    private ArrayList<PaymentBO> mPaymentList;

    private ArrayList<Fragment> mFragmentList;
    private ArrayList<CharSequence> mFragmentTitle;
    private ArrayList<InvoiceHeaderBO> mInvioceList;
    private TextView mOSAmtTV;
    private TextView mPayableAmtTV;
    private TextView mDiscTV;
    private TextView mTosTV;
    private TextView mPendingBillsTV;

    private ArrayList<InvoiceHeaderBO> mSelecteInvoiceList;

    private int mSelectedPagePos = 0;
    private String mErrorMsg = "";
    private HashMap<String, PaymentBO> mPaymentBOByMode;
    private AlertDialog.Builder build;
    private AlertDialog alertDialog;

    // printer
    private Connection zebraPrinterConnection;
    private int mSelectedPrintCount = 0;
    private LinearLayout mRootLL;

    private boolean isPageSeleced = false;
    private View rootView;
    private boolean isAdvancePaymentAvailable;

    private double mTotalInvoiceAmt = 0;
    private Button paybtn;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_collection_new, container,
                false);
        mRootLL = (LinearLayout) rootView.findViewById(R.id.ll_parent);
        setHasOptionsMenu(true);
        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayShowTitleEnabled(false);
        }
        setScreenTitle("" + bmodel.mSelectedActivityName);

        paybtn = (Button) rootView.findViewById(R.id.paybtn);
        paybtn.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        paybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!hasInvoice())
                    Toast.makeText(
                            getActivity(),
                            getResources().getString(R.string.please_select_anyone_invoice_from_list),
                            Toast.LENGTH_SHORT).show();
                else {

                    if (bmodel.configurationMasterHelper.IS_NAVIGATE_CREDIT_NOTE_SCREEN &&
                            mPaymentList != null && mPaymentList.size() > 0) {

                        int pos = 0;
                        for (PaymentBO paymentBO : mPaymentList) {

                            if (paymentBO.getCashMode().equals("CN")
                                    && isCreditNoteAvailable()) {
                                Intent intent = new Intent(getActivity(), PaymentModeActivity.class);
                                intent.putExtra("position", pos);
                                intent.putExtra("IsAdvancePaymentAvailable", false);
                                intent.putExtra("paymode", "" + paymentBO.getCashMode());
                                intent.putExtra("FromCollection", true);
                                startActivity(intent);
                                getActivity().overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

                                break;
                            } else if (pos == mPaymentList.size() - 1) {
                                Intent intent = new Intent(getActivity(), BillPaymentActivity.class);
                                bmodel.mSelectedActivityName = "Bill Payment";
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                getActivity().overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

                            }

                            pos = pos + 1;
                        }
                    }else{
                        Intent intent = new Intent(getActivity(), BillPaymentActivity.class);
                        bmodel.mSelectedActivityName = "Bill Payment";
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                    }

                }

            }
        });

        TextView tosAmtTitle = (TextView) rootView.findViewById(R.id.tv_title_tos_amount);
        tosAmtTitle.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        TextView pendingBillTitle = (TextView) rootView.findViewById(R.id.tv_title_pending_bills);
        pendingBillTitle.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        TextView dueBillTitle = (TextView) rootView.findViewById(R.id.tv_title_due_bill);
        dueBillTitle.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        //As of now we hiding this because functionality not yet completed
        rootView.findViewById(R.id.ll_due_bills).setVisibility(View.GONE);

        if (bmodel.configurationMasterHelper.MOVE_NEXT_ACTIVITY) {
            Button btnClose = (Button) rootView.findViewById(R.id.btn_close);
            btnClose.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            btnClose.setVisibility(View.VISIBLE);
            btnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new CommonDialog(getActivity().getApplicationContext(), getActivity(),
                            "", getActivity().getResources().getString(R.string.move_next_activity),
                            false, getActivity().getResources().getString(R.string.ok),
                            getActivity().getResources().getString(R.string.cancel), new CommonDialog.PositiveClickListener() {
                        @Override
                        public void onPositiveButtonClick() {
                            Intent intent = new Intent(getActivity(), HomeScreenTwo.class);

                            Bundle extras = getActivity().getIntent().getExtras();
                            if (extras != null) {
                                intent.putExtra("IsMoveNextActivity", true);
                                intent.putExtra("CurrentActivityCode", "MENU_COLLECTION");
                            }

                            startActivity(intent);
                            getActivity().finish();
                        }
                    }, new CommonDialog.negativeOnClickListener() {
                        @Override
                        public void onNegativeButtonClick() {

                        }
                    }).show();

                }
            });
        }

        if (getArguments().getBoolean("IS_NO_COLL_REASON",false) &&
                !bmodel.collectionHelper.checkInvoiceWithReason(bmodel.getRetailerMasterBO().getRetailerID(),getContext()))
            showDialog();

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPaymentList = bmodel.collectionHelper.getCollectionPaymentList();
        mPaymentBOByMode = bmodel.collectionHelper.getPaymentBoByMode();
        updateIsAdvancePaymentAvailabe();

        /**
         * Header Value
         */
        mTosTV = (TextView) rootView.findViewById(R.id.tv_tos_amount);
        mPendingBillsTV = (TextView) rootView.findViewById(R.id.tv_pending_bills);

        /**
         * Pending Invoice list
         */
        mCollectionLV = (ListView) rootView.findViewById(R.id.lv_collection);
        if (bmodel.collectionHelper.isCollectionView()) {
            mCollectionLV.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        }
        View padding = new View(getActivity());
        padding.setMinimumHeight(3);
        mCollectionLV.addHeaderView(padding);

        /**
         * Bottom Value for selected invoice
         */
        mOSAmtTV = (TextView) rootView.findViewById(R.id.tv_osamount);
        mPayableAmtTV = (TextView) rootView.findViewById(R.id.tv_paidamt);
        mDiscTV = (TextView) rootView.findViewById(R.id.tv_disc_amt);

        if (!bmodel.configurationMasterHelper.SHOW_DISC_AMOUNT_ALLOW) {
            LinearLayout discLL = (LinearLayout) rootView.findViewById(R.id.ll_disc);
            discLL.setVisibility(View.GONE);
        } else {
            LinearLayout discLL = (LinearLayout) rootView.findViewById(R.id.ll_disc);
            discLL.setVisibility(View.VISIBLE);
        }

        if (bmodel.collectionHelper.isCollectionView()) {
            rootView.findViewById(R.id.bottom_value_layout).setVisibility(View.GONE);
        }

        /**
         * Load the values
         */
        mInvioceList = bmodel.getInvoiceHeaderBO();
        updateHeaderValuesForPendingInvoice();
        updateInvoiceListAdapter();
        updateBottomValuesForSelectedInvoice();

        //

        if (bmodel.getRetailerMasterBO().getHasPaymentIssue() == 1) {
            Snackbar snackbar = Snackbar
                    .make(mRootLL,
                            getResources().getString(R.string.cheque_bounced), Snackbar.LENGTH_LONG);
            TypedValue typedValue = new TypedValue();
            TypedArray a = getActivity().obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorPrimary});
            a.recycle();
            View view = snackbar.getView();

            view.setMinimumWidth(1400);
            view.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.darker_gray));
            TextView snackbarTV = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
            snackbarTV.setTextColor(ContextCompat.getColor(getActivity(), R.color.dark_red));
            snackbar.show();
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {


        if (bmodel.collectionHelper.isCollectionView()) {
            menu.findItem(R.id.menu_next).setVisible(false);
            menu.findItem(R.id.menu_advance_payment).setVisible(false);
        }
        menu.findItem(R.id.menu_next).setVisible(false);

        if (bmodel.configurationMasterHelper.SHOW_NO_COLLECTION_REASON)
            menu.findItem(R.id.menu_collection_reason).setVisible(true);

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            bmodel.collectionHelper.setCollectionView(false);
            bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                    .now(SDUtil.TIME));
            getActivity().finish();
           /* BusinessModel.loadActivity(getActivity(),
                    DataMembers.actHomeScreenTwo);*/

            Intent myIntent = new Intent(getActivity(), HomeScreenTwo.class);
            startActivityForResult(myIntent, 0);

            getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            return true;
        } else if (i == R.id.menu_advance_payment) {
            if (mTotalInvoiceAmt == 0) {
                /*AdvancePaymentDialogFragment dialogFragment = new AdvancePaymentDialogFragment();
                dialogFragment.setCancelable(false);
               dialogFragment.show(getFragmentManager(), getResources().getString(R.string.advance_payment));*/
            } else {
                Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.advance_payment_cannot_be_reveived), Toast.LENGTH_SHORT).show();
            }
        }else if(i == R.id.menu_collection_reason){
            Intent intent = new Intent(getActivity(), NoCollectionReasonActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            getActivity().overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

        }
        return super.onOptionsItemSelected(item);
    }


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
        CheckModeFragment checkModeFragment = (CheckModeFragment) mFragmentList.get(mSelectedPagePos);
        if (checkModeFragment != null)
            checkModeFragment.updateDate(date, "");
    }

    @Override
    public void updateReceiptNo(String receiptno) {
        bmodel.collectionHelper.receiptno = receiptno;
        // new SaveCollection().execute();
    }

    @Override
    public void print(int printCount) {
        mSelectedPrintCount = printCount;
        build = new AlertDialog.Builder(getActivity());
        customProgressDialog(build, "Printing....");
        alertDialog = build.create();

        alertDialog.show();

        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                if (bmodel.configurationMasterHelper.SHOW_BIXOLON_TITAN || bmodel.configurationMasterHelper.COMMON_PRINT_BIXOLON) {
                    doConnectBixolon();
                } else if (bmodel.configurationMasterHelper.SHOW_SCRIBE_TITAN || bmodel.configurationMasterHelper.COMMON_PRINT_SCRYBE) {
                    doConnectionScribe();
                } else {
                    doConnection();
                }
                Looper.loop();
                Looper myLooper = Looper.myLooper();
                if (myLooper != null)
                    myLooper.quit();
            }
        }).start();
    }

    @Override
    public void dismiss() {
        Intent i = new Intent(getActivity(), HomeScreenTwo.class);
        startActivity(i);
        getActivity().finish();
    }


    private class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mInvioceList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.list_collection,
                        parent, false);
                holder = new ViewHolder();
                holder.tvInvoiceNo = (TextView) row.findViewById(R.id.tv_invoice_no);
                holder.tvInvAmt = (TextView) row.findViewById(R.id.tv_invamt);
                holder.tvOSAmt = (TextView) row.findViewById(R.id.tv_osamt);
                holder.tvReceivedAmt = (TextView) row.findViewById(R.id.tv_received);
                holder.tvPayableAmtTitle = (TextView) row.findViewById(R.id.tv_payableamt_title);
                holder.tvPayableAmt = (TextView) row.findViewById(R.id.tv_payableamt);
                holder.tvDiscAmtTitle = (TextView) row.findViewById(R.id.tv_disc_title);
                holder.tvDiscAmt = (TextView) row.findViewById(R.id.tv_disc);
                holder.tvInvDate = (TextView) row.findViewById(R.id.tv_invoice_date);
                holder.tvAge = (TextView) row.findViewById(R.id.tv_age);
                holder.tvDueDate = (TextView) row.findViewById(R.id.tv_duedate);
                holder.tvDueDateTitle = (TextView) row.findViewById(R.id.tv_duedate_title);
                holder.tvDocRef = (TextView) row.findViewById(R.id.tv_docRef);
                holder.tvDocRefTitle = (TextView) row.findViewById(R.id.tv_docRef_title);

                if (bmodel.configurationMasterHelper.SHOW_DISC_AMOUNT_ALLOW) {
                    holder.tvPayableAmtTitle.setVisibility(View.VISIBLE);
                    holder.tvPayableAmt.setVisibility(View.VISIBLE);
                    holder.tvDiscAmt.setVisibility(View.VISIBLE);
                    holder.tvDiscAmtTitle.setVisibility(View.VISIBLE);
                } else {
                    holder.tvPayableAmtTitle.setVisibility(View.GONE);
                    holder.tvPayableAmt.setVisibility(View.GONE);
                    holder.tvDiscAmt.setVisibility(View.GONE);
                    holder.tvDiscAmtTitle.setVisibility(View.GONE);
                }

                if (bmodel.getRetailerMasterBO().getCreditDays() > 0) {
                    holder.tvDueDateTitle.setVisibility(View.VISIBLE);
                    holder.tvDueDate.setVisibility(View.VISIBLE);
                } else {
                    holder.tvDueDateTitle.setVisibility(View.GONE);
                    holder.tvDueDate.setVisibility(View.GONE);
                }

                if (bmodel.configurationMasterHelper.SHOW_DOC_REF_NO) {
                    holder.tvDocRefTitle.setVisibility(View.VISIBLE);
                    holder.tvDocRef.setVisibility(View.VISIBLE);
                } else {
                    holder.tvDocRefTitle.setVisibility(View.GONE);
                    holder.tvDocRef.setVisibility(View.GONE);
                }

                holder.imgInvSelected = (ImageView) row.findViewById(R.id.img_check);

                row.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!bmodel.collectionHelper.isCollectionView()) {

                            if (bmodel.configurationMasterHelper.IS_COLLECTION_ORDER
                                    && checkPreviousInvoice(holder.invoiceHeaderBO)) {

                                Toast.makeText(
                                        getActivity(),
                                        getResources()
                                                .getString(
                                                        R.string.you_have_pending_previous_collection),
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                if (holder.invoiceHeaderBO.getBalance() > 0) {
                                    if (holder.invoiceHeaderBO.isChkBoxChecked()) {
                                        holder.invoiceHeaderBO.setChkBoxChecked(false);
                                        holder.imgInvSelected.setVisibility(View.INVISIBLE);

                                    } else {
                                        holder.invoiceHeaderBO.setChkBoxChecked(true);
                                        holder.imgInvSelected.setVisibility(View.VISIBLE);
                                        holder.imgInvSelected.setBackgroundResource(R.drawable.coll_tick);
                                    }
                                    PaymentBO paymentBO = mPaymentList.get(mSelectedPagePos);
                                    if (paymentBO.getCashMode().equals(StandardListMasterConstants.ADVANCE_PAYMENT)) {
                                        if (mFragmentList != null && mFragmentList.size() > 0) {
                                            AdvancePaymentFragment advancePaymentFragment = (AdvancePaymentFragment) mFragmentList.get(mSelectedPagePos);
                                            if (advancePaymentFragment != null) {
                                                updateSelectedList();
                                                advancePaymentFragment.updatePaymentDetails();
                                                bmodel.collectionHelper.updateCollectionList(mSelecteInvoiceList, paymentBO.getCashMode());
                                            }
                                        }
                                    }
                                    updateBottomValuesForSelectedInvoice();
                                    updateListView(holder);
                                } else {
                                    holder.invoiceHeaderBO.setChkBoxChecked(false);
                                    holder.imgInvSelected.setBackgroundResource(R.drawable.paid);
                                }
                            }
                        }
                    }
                });

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }
            holder.invoiceHeaderBO = mInvioceList.get(position);

            holder.tvInvoiceNo.setText(holder.invoiceHeaderBO.getInvoiceNo());
            holder.tvInvAmt.setText(bmodel.formatValue(holder.invoiceHeaderBO
                    .getInvoiceAmount()));

            holder.tvInvDate.setText(DateUtil.convertFromServerDateToRequestedFormat(
                    holder.invoiceHeaderBO.getInvoiceDate(),
                    ConfigurationMasterHelper.outDateFormat));
//            final int count = DateUtil.getDateCount(holder.invoiceHeaderBO.getInvoiceDate(),
//                    SDUtil.now(SDUtil.DATE_GLOBAL), "yyyy/MM/dd");
            if(bmodel.configurationMasterHelper.COMPUTE_DUE_DAYS) {
                int count = 0;
                if (bmodel.retailerMasterBO.getCreditDays() != 0) {
                    if (holder.invoiceHeaderBO.getDueDate() != null)
                        count = DateUtil.getDateCount(SDUtil.now(SDUtil.DATE_GLOBAL),
                                holder.invoiceHeaderBO.getDueDate(), "yyyy/MM/dd");
                } else {
                    if (holder.invoiceHeaderBO.getInvoiceDate() != null)
                        count = DateUtil.getDateCount(SDUtil.now(SDUtil.DATE_GLOBAL),
                                holder.invoiceHeaderBO.getInvoiceDate(), "yyyy/MM/dd");
                }
                if (count < 0)
                    count = 0;
                String strCount = "(" + (count + 1) + ")";
                holder.tvAge.setText(strCount);
            }else{
                String strDays = (holder.invoiceHeaderBO.getDueDays() != null && !"0".equals(holder.invoiceHeaderBO.getDueDays())
                        && !holder.invoiceHeaderBO.getDueDays().isEmpty()) ? "(" + holder.invoiceHeaderBO.getDueDays() + ")" : "";
                holder.tvAge.setText(strDays);
            }

            String strPayment = bmodel.formatValueBasedOnConfig(holder.invoiceHeaderBO.getBalance()) + "";
            holder.tvPayableAmt.setText(strPayment);

            holder.tvDiscAmt.setText(bmodel
                    .formatValueBasedOnConfig(holder.invoiceHeaderBO.getRemainingDiscountAmt()));

            holder.tvReceivedAmt.setText(bmodel.formatValue(holder.invoiceHeaderBO
                    .getPaidAmount() + holder.invoiceHeaderBO.getAppliedDiscountAmount()));

            holder.tvDueDate.setText(holder.invoiceHeaderBO.getDueDate());

            double osamount = holder.invoiceHeaderBO.getBalance() + SDUtil.convertToDouble(bmodel.formatValueBasedOnConfig(holder.invoiceHeaderBO.getRemainingDiscountAmt()));
            if (osamount <= 0)
                holder.tvOSAmt.setText("0");
            else
                holder.tvOSAmt.setText(bmodel.formatValueBasedOnConfig(osamount));

            if (holder.invoiceHeaderBO.isChkBoxChecked()) {
                holder.imgInvSelected.setVisibility(View.VISIBLE);
                holder.imgInvSelected.setBackgroundResource(R.drawable.coll_tick);
            } else {
                if (holder.invoiceHeaderBO.getBalance() > 0) {
                    holder.imgInvSelected.setVisibility(View.INVISIBLE);
                } else {
                    holder.imgInvSelected.setVisibility(View.VISIBLE);
                    holder.imgInvSelected.setBackgroundResource(R.drawable.paid);
                }
            }

            holder.tvDocRef.setText((holder.invoiceHeaderBO.getDocRefNo() != null) ? holder.invoiceHeaderBO.getDocRefNo() : "-");

            return row;
        }
    }

    class ViewHolder {
        TextView tvInvoiceNo;
        TextView tvInvAmt;
        TextView tvOSAmt;
        TextView tvReceivedAmt;
        TextView tvPayableAmt;
        TextView tvDiscAmt;
        TextView tvPayableAmtTitle;
        TextView tvDiscAmtTitle;
        TextView tvInvDate;
        TextView tvAge;
        TextView tvDueDate;
        TextView tvDueDateTitle;
        TextView tvDocRef;
        TextView tvDocRefTitle;

        ImageView imgInvSelected;
        InvoiceHeaderBO invoiceHeaderBO;
    }

    private boolean checkPreviousInvoice(InvoiceHeaderBO bo) {
        try {
            for (int k = 0; k < mInvioceList.size(); k++) {
                if (bo == mInvioceList.get(k)) {
                    InvoiceHeaderBO inv = mInvioceList.get(k - 1);
                    return inv.getBalance() > 0;
                }
            }
        } catch (Exception e) {
            Commons.printException(e + "");
            return false;
        }
        return false;
    }

    private void updateBottomValuesForSelectedInvoice() {
        double mTotalOSAmt = 0;
        double mTotalPayableAmt = 0;
        double mTotalDiscAmt = 0;
        double paidAmt = 0;
        double givenDiscAmt = 0;

        if (mInvioceList != null && mInvioceList.size() > 0) {
            for (InvoiceHeaderBO invoiceHeaderBO : mInvioceList) {
                if (invoiceHeaderBO.isChkBoxChecked()) {
                    mTotalOSAmt = mTotalOSAmt + (invoiceHeaderBO.getBalance() + invoiceHeaderBO.getRemainingDiscountAmt());
                    mTotalPayableAmt = mTotalPayableAmt + invoiceHeaderBO.getBalance();
                    mTotalDiscAmt = mTotalDiscAmt + invoiceHeaderBO.getRemainingDiscountAmt();
                }
            }
        }

        if (mPaymentList != null && mPaymentList.size() > 0) {
            for (PaymentBO paymentBO : mPaymentList) {
                if (paymentBO.getAmount() > 0) {
                    paidAmt = paidAmt + paymentBO.getAmount();
                    givenDiscAmt = givenDiscAmt + paymentBO.getDiscountedAmount();
                }
            }
        }

        mTotalPayableAmt = mTotalPayableAmt - paidAmt;
        mTotalDiscAmt = mTotalDiscAmt - givenDiscAmt;
        if (mTotalPayableAmt > 0) {
            mPayableAmtTV.setText(bmodel.formatValue(mTotalPayableAmt));
        } else {
            mPayableAmtTV.setText(bmodel.formatValue(0));
        }
        if (mTotalDiscAmt > 0) {
            mDiscTV.setText(bmodel.formatValue(mTotalDiscAmt) + "");
        } else {
            mDiscTV.setText(bmodel.formatValue(0));
        }

        mOSAmtTV.setText(bmodel.formatValue(mTotalOSAmt));
    }

    private boolean hasInvoice() {
        if (mInvioceList != null && mInvioceList.size() > 0) {
            for (InvoiceHeaderBO invoiceHeaderBO : mInvioceList) {
                if (invoiceHeaderBO.isChkBoxChecked()) {
                    return true;
                }
            }
        }
        return false;
    }


    private void updateInvoiceListAdapter() {
        if (mInvioceList != null && mInvioceList.size() > 0) {
            MyAdapter mCollectionAdapter = new MyAdapter();
            mCollectionLV.setAdapter(mCollectionAdapter);
        }
    }

    private void updateListView(ViewHolder holder) {
        holder.tvPayableAmt.setText(bmodel.formatValueBasedOnConfig(holder.invoiceHeaderBO.getBalance()));
        holder.tvDiscAmt.setText(bmodel.formatValueBasedOnConfig(holder.invoiceHeaderBO.getRemainingDiscountAmt()));
    }

    private void updateHeaderValuesForPendingInvoice() {
        mTotalInvoiceAmt = 0;
        int count = 0;
        for (InvoiceHeaderBO invoiceHeaderBO : mInvioceList) {
            if (invoiceHeaderBO.getBalance() > 0) {
                count = count + 1;
                mTotalInvoiceAmt = mTotalInvoiceAmt + invoiceHeaderBO.getBalance() + invoiceHeaderBO.getRemainingDiscountAmt();
            }
        }

        mTosTV.setText(bmodel.formatValue(mTotalInvoiceAmt));
        String strCount = count + "";
        mPendingBillsTV.setText(strCount);
    }

    /*public void viewRestore(Bundle bundle) {
        mSelectedPagePos = bundle.getInt("viewPagerPos", 0);
        mViewPager.setCurrentItem(mSelectedPagePos);
    }*/

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("viewPagerPos", mSelectedPagePos);
        ((CollectionScreen) getActivity()).passData(outState);
    }

    /**
     * Method to check haspayment status
     * and allow collection by using CB only or not
     */
   /* private boolean checkHasPaymentIssue(String mode) {
        if (bmodel.getRetailerMasterBO().getHasPaymentIssue() == 1) {
            boolean isCBType = bmodel.collectionHelper.isCreditBalancebalance(mode);
            if (!isCBType) {
                return true;
            }
        }
        return false;
    }*/

    /// print
    private void doConnection() {
        try {
            ZebraPrinter printer = connect();
            if (printer != null) {
                // sendTestLabel();
                printInvoice();
            } else {
                disconnect();
                alertDialog.dismiss();
                Toast.makeText(getActivity(), getResources().getString(R.string.printer_not_connected_check_macaddress), Toast.LENGTH_SHORT).show();
                Intent i = new Intent(getActivity(), HomeScreenTwo.class);
                startActivity(i);
                getActivity().finish();
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    private void printInvoice() {
        try {
            int printDoneCount = bmodel.reportHelper.getPaymentPrintCount(bmodel.collectionHelper.collectionGroupId.replace("'", ""));
            for (int i = 0; i <= mSelectedPrintCount; i++) {
                if (i == 0 && printDoneCount == 0)
                    zebraPrinterConnection.write(bmodel.printHelper.printCollection(true));
                else
                    zebraPrinterConnection.write(bmodel.printHelper.printCollection(false));
            }

            bmodel.reportHelper.updatePaymentPrintCount(bmodel.collectionHelper.collectionGroupId.replace("'", ""), ((mSelectedPrintCount + 1) + printDoneCount));

            DemoSleeper.sleep(1500);
            if (zebraPrinterConnection instanceof BluetoothConnection) {
                DemoSleeper.sleep(500);
            }

            bmodel.showAlert(
                    getResources().getString(
                            R.string.printed_successfully), DataMembers.SAVECOLLECTION);
        } catch (ConnectionException e) {
            Commons.printException("" + e);

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
                Commons.printException("" + e);
            }
        }
        return printer;
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

    private void updateIsAdvancePaymentAvailabe() {
        if (mPaymentList != null) {
            for (PaymentBO paymentBO : mPaymentList) {
                if (paymentBO.getCashMode().equals(StandardListMasterConstants.ADVANCE_PAYMENT)) {
                    isAdvancePaymentAvailable = true;
                    break;
                }
            }
        }
    }
    //Bixolon Print Module

    static BixolonPrinter mBixolonPrinter = null;
    String PRINT_STATE = "";
    boolean isconnected;

    private void doConnectBixolon() {
        disconnectBixolon();
        mBixolonPrinter = new BixolonPrinter(getActivity(), mHandlerBixolon, null);
        mBixolonPrinter.findBluetoothPrinters();
    }

    private void disconnectBixolon() {
        if (mBixolonPrinter != null) {
            mBixolonPrinter.disconnect();
        }
    }

    private final Handler mHandlerBixolon = new Handler(new Handler.Callback() {

        @SuppressWarnings("unchecked")
        @Override
        public boolean handleMessage(Message msg) {
            Commons.print(TAG + "mHandler.handleMessage(" + msg + ")");

            switch (msg.what) {
                case BixolonPrinter.MESSAGE_STATE_CHANGE:
                    mBixolonPrinter.getStatus();

                    switch (msg.arg1) {
                        case BixolonPrinter.STATE_CONNECTED:
                            PRINT_STATE = "TRUE";
                            isconnected = true;
                            getActivity().supportInvalidateOptionsMenu();
                            printBixolonData();
                            break;

                        case BixolonPrinter.STATE_CONNECTING:

                            break;

                        case BixolonPrinter.STATE_NONE:

                            PRINT_STATE = "NO_PRINTER";
                            isconnected = false;
                            break;
                    }
                    return true;

                case BixolonPrinter.MESSAGE_READ:
                    CollectionFragmentNew.this.dispatchMessage(msg);
                    return true;

                case BixolonPrinter.MESSAGE_TOAST:
                    alertDialog.dismiss();
                    if (!PRINT_STATE.equalsIgnoreCase("TRUE") && !PRINT_STATE.equalsIgnoreCase("NO_PRINTER")) {
                        Toast.makeText(getActivity(), getResources().getString(R.string.printer_not_connected), Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(getActivity(), HomeScreenTwo.class);
                        startActivity(i);
                        getActivity().finish();
                    }
                    return true;

                case BixolonPrinter.MESSAGE_BLUETOOTH_DEVICE_SET:
                    if (msg.obj == null) {
                        Toast.makeText(
                                getActivity(),
                                getResources().getString(R.string.no_paired_device),
                                Toast.LENGTH_SHORT).show();
                        alertDialog.dismiss();
                        Intent i = new Intent(getActivity(), HomeScreenTwo.class);
                        startActivity(i);
                        getActivity().finish();

                    } else {

                        showBluetoothDialog(getActivity(),
                                (Set<BluetoothDevice>) msg.obj);
                    }
                    return true;

                case BixolonPrinter.MESSAGE_PRINT_COMPLETE:
                    /*Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.complete_to_print),
                            Toast.LENGTH_SHORT).show();*/
                    return true;

                case BixolonPrinter.MESSAGE_ERROR_OUT_OF_MEMORY:
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.out_of_memory),
                            Toast.LENGTH_SHORT).show();
                    return true;

            }
            return false;
        }

    });

    protected void printBixolonData() {
        try {
            boolean isOriginal;

            for (int i = 0; i <= mSelectedPrintCount; i++) {
                if (i == 0)
                    isOriginal = true;
                else isOriginal = false;
                printTextLeft(bmodel.printHelper.printDataforBixolon3inchCollectionprinter(true, bmodel.collectionHelper.collectionGroupId, isOriginal, true), DataMembers.PRINT_TEXT_SIZE);
            }
            bmodel.reportHelper.updatePaymentPrintCount(bmodel.collectionHelper.collectionGroupId.replace("'", ""), mSelectedPrintCount + 1);

            DemoSleeper.sleep(1500);

            bmodel.showAlert(
                    getResources().getString(
                            R.string.printed_successfully), DataMembers.SAVECOLLECTION);
        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            disconnectBixolon();
        }
    }

    public void printTextLeft(String text, int size) {
        CheckGC();
        if (size == 1)
            mBixolonPrinter.printText(text, BixolonPrinter.ALIGNMENT_LEFT,
                    BixolonPrinter.TEXT_ATTRIBUTE_FONT_A,
                    BixolonPrinter.TEXT_SIZE_HORIZONTAL1
                            | BixolonPrinter.TEXT_SIZE_VERTICAL1, true);
        else if (size == 2)
            mBixolonPrinter.printText(text, BixolonPrinter.ALIGNMENT_LEFT,
                    BixolonPrinter.TEXT_ATTRIBUTE_FONT_A,
                    BixolonPrinter.TEXT_SIZE_HORIZONTAL2
                            | BixolonPrinter.TEXT_SIZE_VERTICAL2, true);
    }

    void CheckGC() {
        CheckGC("");
    }

    void CheckGC(String FunctionName) {
        long VmfreeMemory = Runtime.getRuntime().freeMemory();
        long VmmaxMemory = Runtime.getRuntime().maxMemory();
        long VmtotalMemory = Runtime.getRuntime().totalMemory();
        long Memorypercentage = ((VmtotalMemory - VmfreeMemory) * 100)
                / VmtotalMemory;

        Commons.print(TAG + FunctionName + "Before Memorypercentage"
                + Memorypercentage + "% VmtotalMemory[" + VmtotalMemory + "] "
                + "VmfreeMemory[" + VmfreeMemory + "] " + "VmmaxMemory["
                + VmmaxMemory + "] ");

        // Runtime.getRuntime().gc();
        System.runFinalization();
        System.gc();
        VmfreeMemory = Runtime.getRuntime().freeMemory();
        VmmaxMemory = Runtime.getRuntime().maxMemory();
        VmtotalMemory = Runtime.getRuntime().totalMemory();
        Memorypercentage = ((VmtotalMemory - VmfreeMemory) * 100)
                / VmtotalMemory;
        Commons.print(TAG + FunctionName + "_After Memorypercentage"
                + Memorypercentage + "% VmtotalMemory[" + VmtotalMemory + "] "
                + "VmfreeMemory[" + VmfreeMemory + "] " + "VmmaxMemory["
                + VmmaxMemory + "] ");
    }

    private void dispatchMessage(Message msg) {
        switch (msg.arg1) {
            case BixolonPrinter.PROCESS_GET_STATUS:
                if (msg.arg2 == BixolonPrinter.STATUS_NORMAL) {
                    /*Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.no_error),
                            Toast.LENGTH_SHORT).show();*/
                } else {
                    StringBuffer buffer = new StringBuffer();
                    if ((msg.arg2 & BixolonPrinter.STATUS_COVER_OPEN) == BixolonPrinter.STATUS_COVER_OPEN) {
                        buffer.append(getResources().getString(
                                R.string.cover_is_open)
                                + ".\n");
                    }
                    if ((msg.arg2 & BixolonPrinter.STATUS_PAPER_NOT_PRESENT) == BixolonPrinter.STATUS_PAPER_NOT_PRESENT) {
                        buffer.append(getResources().getString(
                                R.string.paper_not_present)
                                + ".\n");
                    }

                    Toast.makeText(getActivity(), buffer.toString(),
                            Toast.LENGTH_SHORT).show();
                }
                break;

            case BixolonPrinter.PROCESS_GET_BATTERY_VOLTAGE_STATUS:
                if (msg.arg2 == BixolonPrinter.STATUS_BATTERY_LOW_VOLTAGE) {
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.low_voltage),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.normal_voltage),
                            Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }

    public void showBluetoothDialog(Context context,
                                    final Set<BluetoothDevice> pairedDevices) {

        final String[] items = new String[pairedDevices.size()];
        int index = 0;
        for (BluetoothDevice device : pairedDevices) {
            items[index++] = device.getAddress();
        }
        if (pairedDevices.size() > 1) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                    .setTitle(
                            getResources().getString(
                                    R.string.paired_bluetooth_printers))
                    .setItems(items, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            mBixolonPrinter
                                    .connect(items[which]);

                        }
                    });
            bmodel.applyAlertDialogTheme(builder);
        } else
            mBixolonPrinter.connect(items[0]);
    }

    // scribe

    private void doConnectionScribe() {
        try {
            new ScribePrinter(new ScribePrinter.ScribeListener() {
                @Override
                public void isScribeResponse(AEMPrinter aemPrinter, AEMScrybeDevice aemScrybeDevice, boolean isConnected) {
                    printScribeData(aemPrinter, aemScrybeDevice, isConnected);
                }
            }).execute();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    /**
     * Method used for Scribe Printing
     *
     * @param aemPrinter
     * @param aemScrybeDevice
     * @param isconnected
     */

    private void printScribeData(AEMPrinter aemPrinter, AEMScrybeDevice aemScrybeDevice, boolean isconnected) {
        byte fontSize = 26;
        if (isconnected) {
            if (aemPrinter != null) {
                try {
                    boolean isOriginal = false;

                    for (int i = 0; i <= mSelectedPrintCount; i++) {
                        if (i == 0) isOriginal = true;
                        else isOriginal = false;
                        aemPrinter.setFontType(AEMPrinter.FONT_NORMAL);
                        aemPrinter.setFontSize(fontSize);
                        aemPrinter.print((bmodel.printHelper.printDataforBixolon3inchCollectionprinter(false, bmodel.collectionHelper.collectionGroupId, isOriginal, true)));
                        aemPrinter.setCarriageReturn();


                    }
                    bmodel.reportHelper.updatePaymentPrintCount(bmodel.collectionHelper.collectionGroupId.replace("'", ""), (mSelectedPrintCount + 1));

                    DemoSleeper.sleep(1600 * (mSelectedPrintCount + 1));
                    bmodel.showAlert(
                            getResources().getString(
                                    R.string.printed_successfully), DataMembers.SAVECOLLECTION);
                } catch (Exception e) {
                    Commons.printException("" + e);
                } finally {
                    disconnectScribe(aemScrybeDevice);
                }

            }
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.printer_not_connected), Toast.LENGTH_SHORT).show();
            Intent i = new Intent(getActivity(), HomeScreenTwo.class);
            startActivity(i);
            getActivity().finish();
        }
    }

    private void disconnectScribe(AEMScrybeDevice aemScrybeDevice) {
        if (aemScrybeDevice != null) {
            try {
                aemScrybeDevice.disConnectPrinter();
            } catch (IOException e) {
                Commons.printException("" + e);
            }
        }
    }

    protected void showDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setIcon(null)
                .setCancelable(false)
                .setTitle(
                        getResources().getString(
                                R.string.no_collection_reason))
                .setMessage(getResources().getString(
                        R.string.invoice_with_no_collection))
                .setPositiveButton(getResources().getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {

                                Intent intent = new Intent(getActivity(), NoCollectionReasonActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                getActivity().overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

                            }

                        })
                .setNegativeButton(
                        getResources().getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {

                            }
                        });

        bmodel.applyAlertDialogTheme(builder);


    }

    private boolean isCreditNoteAvailable() {
        boolean isAvaiable = false;
        String modeID = bmodel.getStandardListIdAndType(
                "CNAP",
                StandardListMasterConstants.CREDIT_NOTE_TYPE);
        if (bmodel.collectionHelper.getCreditNoteList() != null) {
            ArrayList<CreditNoteListBO> mCreditNoteList = new ArrayList<>();
            for (CreditNoteListBO bo : bmodel.collectionHelper
                    .getCreditNoteList()) {
                if (bo.getRetailerId().equals(
                        bmodel.getRetailerMasterBO().getRetailerID())
                        && !bo.isUsed() && (!modeID.equals(bo.getTypeId() + "")))
                    mCreditNoteList.add(bo);
            }
            if (mCreditNoteList.size() > 0)
                isAvaiable = true;

        }


        return isAvaiable;
    }

}

