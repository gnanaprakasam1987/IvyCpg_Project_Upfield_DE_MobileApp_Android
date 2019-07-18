package com.ivy.sd.print;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.aem.api.AEMPrinter;
import com.aem.api.AEMScrybeDevice;
import com.bixolon.printer.BixolonPrinter;
import com.ivy.cpg.view.order.OrderHelper;
import com.ivy.cpg.view.reports.collectionreport.CollectionReportHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.PaymentBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.utils.DateTimeUtils;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

public class CollectionPreviewScreen extends IvyBaseActivityNoActionBar {

    private TextView totcash, totcheque, total_dd, total_rtgs, tot_mob_payment, total, totCn;
    private TextView tv_distributor, tv_vatNo, tv_adr1, tv_adr2, tv_printType;
    private BusinessModel bmodel;
    private Spinner printcount;
    private ArrayAdapter<CharSequence> spinadapter;
    private String count;
    private Double totalCash = 0.0, totalCheque = 0.0, totalColl = 0.0, totalDD = 0.0, totalRTGS = 0.0, total_mob_payment = 0.0, totalCn = 0.0;
    private EditText mMacAddressET;
    private TextView statusField;
    private ImageView mStatusIV;
    private ProgressDialog pd;
    private static final int SELECTED_PRINTER_DIALOG = 1;
    private static final String ZEBRA_2INCH = "2";
    private static final String ZEBRA_3INCH = "3";
    private static final String ZEBRA_4INCH = "4";
    private Connection zebraPrinterConnection;
    private final String[] mPrinterSelectionArray = {ZEBRA_2INCH, ZEBRA_3INCH,
            ZEBRA_4INCH};
    private boolean IsOriginal;
    private ZebraPrinter printer;
    private String mSelectedPrinterName;
    private LinearLayout mDetailsContainerLL;

    private ArrayList<PaymentBO> mDetails;

    private static final String TAG = "ColectionPrint";

    String mSelectedRetailer, mSelectedGroupId;
    int no_of_print_done = 0;

    private Toolbar toolbar;
    private CollectionReportHelper mCollectionReportHelper;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_preview_zebra_diageo);

        try {
            bmodel = (BusinessModel) getApplicationContext();
            bmodel.setContext(this);
            mCollectionReportHelper = new CollectionReportHelper(this);
            Bundle extras = getIntent().getExtras();

            toolbar = (Toolbar) findViewById(R.id.toolbar);
            mMacAddressET = (EditText) findViewById(R.id.et_mac);
            statusField = (TextView) findViewById(R.id.status_bar);
            mStatusIV = (ImageView) findViewById(R.id.status_iv);
            mDetailsContainerLL = (LinearLayout) findViewById(R.id.details_container_ll);
            totcash = (TextView) findViewById(R.id.totcash);
            totcheque = (TextView) findViewById(R.id.tocheque);
            totCn = (TextView) findViewById(R.id.totCn);
            total_dd = (TextView) findViewById(R.id.total_dd);
            total_rtgs = (TextView) findViewById(R.id.total_rtgs);
            tot_mob_payment = (TextView) findViewById(R.id.total_mob_payment);
            total = (TextView) findViewById(R.id.tot);

            tv_distributor = (TextView) findViewById(R.id.tv_distributor_name);
            tv_vatNo = (TextView) findViewById(R.id.tv_vat_no);
            tv_adr1 = (TextView) findViewById(R.id.tv_distadd1);
            tv_adr2 = (TextView) findViewById(R.id.tv_distadd2);
            if (bmodel.configurationMasterHelper.SHOW_PRINT_HEADERS) {
                tv_distributor.setText("Unipal General Trading Company");
                tv_vatNo.setText("VAT No  : 562414227");
                tv_adr1.setText("Ramallah - Industrial zone, Tel: +972 2 2981060");
                tv_adr2.setText("Gaza - lndus. Zone - Carny, Tel: +972 7 2830324");
            } else {
                tv_distributor.setText(bmodel.userMasterHelper.getUserMasterBO().getDistributorName());
                tv_vatNo.setText(bmodel.userMasterHelper.getUserMasterBO().getDistributorTinNumber());
                tv_adr1.setText(bmodel.userMasterHelper.getUserMasterBO().getDistributorAddress1());
                tv_adr2.setText(bmodel.userMasterHelper.getUserMasterBO().getDistributorAddress2());
            }

            setSupportActionBar(toolbar);

            // Set title to toolbar
            if (getSupportActionBar() != null)
                getSupportActionBar().setTitle(
                        getResources().getString(R.string.print_preview));
            getSupportActionBar().setIcon(R.drawable.icon_stock);
            // Used to on / off the back arrow icon
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Used to remove the app logo actionbar icon and set title as home
            // (title support click)
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            // Used to hide the app logo icon from actionbar
            // getSupportActionBar().setDisplayUseLogoEnabled(false);
            mCollectionReportHelper.loadCollectionReport();

        } catch (Exception e1) {
            Commons.printException("" + e1);
        }
        /**
         * set values in print copy spinner
         **/
        try {
            printcount = (Spinner) findViewById(R.id.printcount);
            spinadapter = new ArrayAdapter<CharSequence>(this,
                    android.R.layout.simple_spinner_item);
            spinadapter
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            int prntcount = bmodel.configurationMasterHelper.printCount;
            if (prntcount == 0)
                prntcount = 1;
            for (int i = 1; i <= prntcount; ++i) {

                spinadapter.add(i + "");
            }
            printcount.setAdapter(spinadapter);
            printcount.setOnItemSelectedListener(new OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int pos, long id) {
                    count = (String) parent.getSelectedItem();

                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                    // TODO Auto-generated method stub

                }

            });

            SharedPreferences pref = this.getSharedPreferences("PRINT",
                    MODE_PRIVATE);
            mMacAddressET.setText(pref.getString("MAC", ""));
        } catch (Exception e) {
            Commons.printException(e);
        }

        if (getIntent().hasExtra("Retailer"))
            mSelectedRetailer = getIntent().getStringExtra("Retailer");
        if (getIntent().hasExtra("GroupId"))
            mSelectedGroupId = getIntent().getStringExtra("GroupId");

        if (!mSelectedRetailer.equals("ALL"))
            no_of_print_done = mCollectionReportHelper.getPaymentPrintCount(mSelectedGroupId);

        doInitialize();
    }


    private void doInitialize() {
        try {

            mDetails = mCollectionReportHelper.getPaymentList();
            updatePreviewDetails();

            if (totalCash != null)
                totcash.setText(bmodel.formatValue(totalCash) + "");
            if (totalCheque != null)
                totcheque.setText(bmodel.formatValue(totalCheque) + "");
            if (total_dd != null)
                total_dd.setText(bmodel.formatValue(totalDD) + "");
            if (total_rtgs != null)
                total_rtgs.setText(bmodel.formatValue(totalRTGS) + "");
            if (tot_mob_payment != null)
                tot_mob_payment.setText(bmodel.formatValue(total_mob_payment) + "");
            if (totalCn != null)
                totCn.setText(bmodel.formatValue(totalCn) + "");
            total.setText(bmodel.formatValue(totalColl) + "");
            total.setText(bmodel.formatValue(totalColl) + "");

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private void updatePreviewDetails() {

        try {
            TextView tv_reciptDate, tv_reciptNum, tv_userCode, tv_userName, tv_retailerCode, tv_retailerName;
            TextView tv_cashType, tv_date, tv_cheq_num, tv_inv_num, tv_total;
            LinearLayout ll_cashType;
            LayoutInflater inflater = getLayoutInflater();

            for (String groupid : mCollectionReportHelper.getLstPaymentBObyGroupId().keySet()) {

                if (mSelectedRetailer.equals("ALL") || (groupid.equals(mSelectedGroupId) && mCollectionReportHelper.getLstPaymentBObyGroupId().get(groupid).get(0).getRetailerName().equals(mSelectedRetailer))) {

                    View v = inflater.inflate(
                            R.layout.row_collection_print_preview_diageo, null);
                    tv_reciptDate = ((TextView) v.findViewById(R.id.tv_recipt_date));
                    tv_reciptNum = ((TextView) v.findViewById(R.id.tv_recipt_no));
                    tv_userCode = ((TextView) v.findViewById(R.id.tv_user_code));
                    tv_userName = ((TextView) v.findViewById(R.id.tv_user_name));
                    tv_retailerCode = ((TextView) v.findViewById(R.id.tv_retailer_code));
                    tv_retailerName = ((TextView) v.findViewById(R.id.tv_retailername));

                    ll_cashType = (LinearLayout) v.findViewById(R.id.product_container_ll);

                    tv_reciptDate.setText(mCollectionReportHelper.getLstPaymentBObyGroupId().get(groupid).get(0).getCollectionDate());
                    tv_reciptNum.setText(mCollectionReportHelper.getLstPaymentBObyGroupId().get(groupid).get(0).getGroupId());
                    tv_userCode.setText(bmodel.userMasterHelper.getUserMasterBO().getUserCode());
                    tv_userName.setText(bmodel.userMasterHelper.getUserMasterBO().getUserName());
                    tv_retailerCode.setText(mCollectionReportHelper.getLstPaymentBObyGroupId().get(groupid).get(0).getRetailerCode());
                    tv_retailerName.setText(mCollectionReportHelper.getLstPaymentBObyGroupId().get(groupid).get(0).getRetailerName());

                    for (int i = 0; i < mCollectionReportHelper.getLstPaymentBObyGroupId().get(groupid).size(); i++) {
                        PaymentBO payBO = mCollectionReportHelper.getLstPaymentBObyGroupId().get(groupid).get(i);
                        View view = inflater.inflate(
                                R.layout.row_collection_print_preview_cash_type, null);
                        tv_cashType = ((TextView) view.findViewById(R.id.tv_type));
                        tv_date = ((TextView) view.findViewById(R.id.tv_date));
                        tv_cheq_num = ((TextView) view.findViewById(R.id.tv_chq_num));
                        tv_inv_num = ((TextView) view.findViewById(R.id.tv_inv_num));
                        tv_total = ((TextView) view.findViewById(R.id.tv_total));

                        if (payBO.getCashMode().equals(StandardListMasterConstants.CASH)) {
                            tv_cashType.setText(getResources().getString(R.string.cash));
                            totalCash = totalCash + payBO.getAmount();
                        } else if (payBO.getCashMode().equals(StandardListMasterConstants.CHEQUE)) {
                            tv_cashType.setText(getResources().getString(R.string.cheque));
                            totalCheque = totalCheque + payBO.getAmount();
                        } else if (payBO.getCashMode().equals(StandardListMasterConstants.DEMAND_DRAFT)) {
                            tv_cashType.setText(getResources().getString(R.string.demanddraft));
                            totalDD += payBO.getAmount();
                        } else if (payBO.getCashMode().equals(StandardListMasterConstants.RTGS)) {
                            tv_cashType.setText(getResources().getString(R.string.rtgs));
                            totalRTGS += payBO.getAmount();
                        } else if (payBO.getCashMode().equals(StandardListMasterConstants.MOBILE_PAYMENT)) {
                            tv_cashType.setText(getResources().getString(R.string.mobile_payment));
                            total_mob_payment += payBO.getAmount();
                        } else if (payBO.getCashMode().equals(StandardListMasterConstants.CREDIT_NOTE)) {
                            if (payBO.getReferenceNumber().startsWith("AP")) {
                                tv_cashType.setText(getResources().getString(R.string.advance_payment));
                            } else {
                                tv_cashType.setText(getResources().getString(R.string.credit_note));
                            }
                            totalCn += payBO.getAmount();
                        }

                        tv_date.setText(mCollectionReportHelper.getLstPaymentBObyGroupId().get(groupid).get(i).getChequeDate());

                        if (!payBO.getCashMode().equals(StandardListMasterConstants.CREDIT_NOTE) && !payBO.getCashMode().equals(StandardListMasterConstants.ADVANCE_PAYMENT))
                            tv_cheq_num.setText(mCollectionReportHelper.getLstPaymentBObyGroupId().get(groupid).get(i).getChequeNumber());
                        else
                            tv_cheq_num.setText("");

                        tv_inv_num.setText(mCollectionReportHelper.getLstPaymentBObyGroupId().get(groupid).get(i).getBillNumber());
                        tv_total.setText(mCollectionReportHelper.getLstPaymentBObyGroupId().get(groupid).get(i).getAmount() + "");

                        ll_cashType.addView(view);
                    }


                    mDetailsContainerLL.addView(v);
                }


            }

            totalColl = totalCash + totalCheque + totalDD + totalRTGS + total_mob_payment + totalCn;
        } catch (Exception e) {
            Commons.printException(e);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_print_preview, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            finish();
            /*Intent i = new Intent(this, ReportMenu.class);
            startActivity(i);*/

            return true;
        } else if (i == R.id.menu_print) {
            new Thread(new Runnable() {
                public void run() {
                    Looper.prepare();
                    if (bmodel.configurationMasterHelper.COMMON_PRINT_BIXOLON || bmodel.configurationMasterHelper.SHOW_BIXOLON_TITAN)
                        doConnectBixolon();
                    else if (bmodel.configurationMasterHelper.COMMON_PRINT_SCRYBE || bmodel.configurationMasterHelper.SHOW_SCRIBE_TITAN)
                        doConnectionScribe();
                    else
                        doConnection(ZEBRA_3INCH);

                    Looper.loop();
                    Looper.myLooper().quit();
                }
            }).start();

        }
        return false;
    }

    public Handler getHandler() {
        // TODO Auto-generated method stub
        return mHandler;

    }

    // connectivity
    boolean isPrinterLanguageDetected = false;

    /**
     * printing invoice
     **/
    public void printInvoice(String printername) {

        try {
            if (printername.equals(ZEBRA_2INCH)) {
                for (int i = 0; i < SDUtil.convertToInt(count); i++)
                    zebraPrinterConnection.write(printDatafor2inchprinter());

            } else if (printername.equals(ZEBRA_3INCH)) {

                for (int i = 0; i < SDUtil.convertToInt(count); i++) {
                    if (i == 0 && no_of_print_done == 0)
                        IsOriginal = true;
                    else {
                        IsOriginal = false;
                    }
                    zebraPrinterConnection.write(printDatafor3inchprinter());
                }

                if (!mSelectedRetailer.equals("ALL"))
                    mCollectionReportHelper.updatePaymentPrintCount(mSelectedGroupId, (SDUtil.convertToInt(count) + no_of_print_done));

                bmodel.showAlert(
                        getResources().getString(
                                R.string.printed_successfully), 5000);
            } else if (printername.equals(ZEBRA_4INCH)) {

                for (int i = 0; i < SDUtil.convertToInt(count); i++) {
                    if (i == 0)
                        IsOriginal = true;
                    else {
                        IsOriginal = false;
                    }
                    zebraPrinterConnection.write(printDatafor4inchprinter());
                }

            }
            setStatus("Sending Data", Color.BLUE);
            DemoSleeper.sleep(1500);

            if (zebraPrinterConnection instanceof BluetoothConnection) {
                String friendlyName = ((BluetoothConnection) zebraPrinterConnection)
                        .getFriendlyName();
                setStatus(friendlyName, Color.MAGENTA);
                Commons.print(TAG + "friendlyName : " + friendlyName);
                DemoSleeper.sleep(500);
            }
        } catch (ConnectionException e) {
            Commons.printException(e);
            setStatus(e.getMessage(), Color.RED);
        } catch (Exception e) {
            Commons.printException(e);
        } finally {
            disconnect();
        }
    }

    public void disconnect() {
        try {
            setStatus("Disconnecting", Color.RED);

            if (zebraPrinterConnection != null) {
                zebraPrinterConnection.close();
            }
            setStatus("Not Connected", Color.RED);
        } catch (ConnectionException e) {
            setStatus(getResources().getString(R.string.printer_not_connected),
                    Color.RED);
            Commons.printException(e);
        } finally {
        }
    }

    public ZebraPrinter connect() {
        // setStatus("Connecting...", Color.YELLOW);
        zebraPrinterConnection = null;
        // if (isBluetoothSelected()) {
        zebraPrinterConnection = new BluetoothConnection(
                getMacAddressFieldText());
        SettingsHelper.saveBluetoothAddress(this, getMacAddressFieldText());

        Commons.print(TAG + "PRINT MAC : " + getMacAddressFieldText());
        try {
            zebraPrinterConnection.open();
            mStatusIV.setImageDrawable(getResources().getDrawable(R.drawable.greenball));
            setStatus("Connected", Color.GREEN);
        } catch (ConnectionException e) {
            setStatus(getResources().getString(R.string.printer_not_connected),
                    Color.RED);
            Commons.printException(e);

            DemoSleeper.sleep(1000);
            disconnect();
        } catch (Exception e) {

            setStatus(getResources().getString(R.string.printer_not_connected),
                    Color.RED);
            Commons.printException(e);
        }

        isPrinterLanguageDetected = false;

        ZebraPrinter printer = null;

        if (zebraPrinterConnection.isConnected()) {

            try {
                printer = ZebraPrinterFactory.getInstance(PrinterLanguage.CPCL,
                        zebraPrinterConnection);

                setStatus("Determining Printer Language", Color.YELLOW);
                PrinterLanguage pl = printer.getPrinterControlLanguage();
                setStatus("Printer Language " + pl, Color.BLUE);
                Commons.print(TAG + "PRINT LANGUAGE : " + pl);
                isPrinterLanguageDetected = true;
            } catch (ConnectionException e) {
                setStatus("PrinterConnectionException", Color.RED);
                Commons.print(TAG
                        + "PRINT LANGUAGE : UNKNOWN : PrinterConnectionException");
                Commons.printException(e);
                // printer = null;
                // DemoSleeper.sleep(1000);
                // disconnect();
                isPrinterLanguageDetected = false;
            } /*
             * catch (ZebraPrinterLanguageUnknownException e) {
             * setStatus("Unknown Printer Language", Color.RED);
             * Commons.printException(e);
             *
             * isPrinterLanguageDetected = false;
             *
             * // printer = null; // DemoSleeper.sleep(1000); // disconnect(); }
             */
        }

        return printer;
    }

    private void doConnection(String printername) {
        try {
            printer = connect();
            if (printer != null) {
                // sendTestLabel();
                printInvoice(printername);
            } else {
                disconnect();
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private final Handler mHandler = new Handler(new Handler.Callback() {

        @SuppressWarnings("unchecked")
        @Override
        public boolean handleMessage(Message msg) {
            Commons.print(TAG + "mHandler.handleMessage(" + msg + ")");

            switch (msg.what) {

                case DataMembers.NOTIFY_ORDER_SAVED:
                    if (pd != null)
                        pd.dismiss();
                    return true;
                case DataMembers.NOTIFY_ORDER_DELETED:
                    try {
                        if (pd != null)
                            pd.dismiss();
                        bmodel = (BusinessModel) getApplicationContext();
                        bmodel.showAlert(
                                getResources().getString(
                                        R.string.order_deleted_sucessfully)
                                        + OrderHelper.getInstance(CollectionPreviewScreen.this).getOrderId(),
                                DataMembers.NOTIFY_ORDER_SAVED);
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                    return true;

                case DataMembers.NOTIFY_INVOICE_SAVED:
                    try {
                        if (pd != null)
                            pd.dismiss();
                        bmodel = (BusinessModel) getApplicationContext();
                        // bmodel.productHelper.clearOrderTable();
                        if (bmodel.configurationMasterHelper.IS_INVOICE) {
                            bmodel.showAlert(
                                    getResources().getString(
                                            R.string.invoice_created_successfully),
                                    DataMembers.NOTIFY_INVOICE_SAVED);
                        } else {
                            bmodel.showAlert(
                                    getResources()
                                            .getString(
                                                    R.string.order_saved_and_print_preview_created_successfully),
                                    DataMembers.NOTIFY_INVOICE_SAVED);
                        }
                    } catch (Exception e) {
                        Commons.print("Save Invoice Error :" + e.toString());
                    }
                    return true;
                case DataMembers.NOTIFY_PRINT:
                    try {
                        new Thread(new Runnable() {
                            public void run() {
                                Looper.prepare();
                                doConnection(ZEBRA_3INCH);
                                Looper.loop();
                                Looper.myLooper().quit();
                            }
                        }).start();
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        Commons.printException(e);
                    }
                    return true;
            }
            return false;
        }
    });

    public void setStatus(final String statusMessage, final int color) {
        try {
            Commons.print(TAG + statusMessage);
            runOnUiThread(new Runnable() {
                public void run() {
                    statusField.setBackgroundColor(color);
                    statusField.setText(statusMessage);

                    // Logs.debug(TAG, statusMessage);
                }
            });
            DemoSleeper.sleep(1000);
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public byte[] printDatafor2inchprinter() {
        return null;
    }

    public byte[] printDatafor3inchprinter() {
        byte[] PrintDataBytes = null;
        try {

            PrinterLanguage printerLanguage = PrinterLanguage.CPCL;
            // 00:22:58:3D:7E:83 - RW420
            // AC:3F:A4:16:B9:AE - IMZ320
            byte[] configLabel = null;
            if (printerLanguage == PrinterLanguage.ZPL) {
                configLabel = "^XA^FO17,16^GB379,371,8^FS^FT65,255^A0N,135,134^FDTEST^FS^XZ"
                        .getBytes();
            } else if (printerLanguage == PrinterLanguage.CPCL) {

                if (bmodel.configurationMasterHelper.IS_SHOW_PRINT_LANGUAGE_THAI) {
                    return printThai();
                }

                int height = 0;
                int x = 190;

                int rowItemSize = 0;
                int size = 0;
                for (String groupid : mCollectionReportHelper.getLstPaymentBObyGroupId().keySet()) {
                    if (mSelectedRetailer.equals("ALL") || (groupid.equals(mSelectedGroupId) && mCollectionReportHelper.getLstPaymentBObyGroupId().get(groupid).get(0).getRetailerName().equals(mSelectedRetailer))) {
                        size += 1;

                        for (int j = 0; j < mCollectionReportHelper.getLstPaymentBObyGroupId().get(groupid).size(); j++) {
                            rowItemSize += 1;
                        }
                    }
                }

                height = x + (size * 330) + (rowItemSize * 95) + 460;
                height = SDUtil.convertToInt(count) * height;
                Commons.print(TAG + "Heigt:" + height);
                String Printitem = "! 0 200 200 " + height + " 1\r\n"
                        + "CENTER\r\n";

                if (bmodel.configurationMasterHelper.SHOW_PRINT_HEADERS) {
                    Printitem += "T 5 1 10 60 "
                            + ""
                            + "Unipal General Trading Company" + "\r\n";

                    Printitem += "T 5 0 10 110 "
                            + ""
                            + "VAT No : 562414227" + "\r\n";

                    Printitem += "T 5 0 10 130 "
                            + ""
                            + "Ramallah - Industrial zone, Tel: +972 2 2981060" + "\r\n";
                    Printitem += "T 5 0 10 150 "
                            + ""
                            + "Gaza - lndus. Zone - Carny, Tel: +972 7 2830324" + "\r\n";
                } else {
                    Printitem += "T 5 1 10 60 "
                            + ""
                            + bmodel.userMasterHelper.getUserMasterBO()
                            .getDistributorName() + "\r\n";

                    Printitem += "T 5 0 10 110 "
                            + ""
                            + bmodel.userMasterHelper.getUserMasterBO()
                            .getDistributorTinNumber() + "\r\n";

                    Printitem += "T 5 0 10 130 "
                            + ""
                            + bmodel.userMasterHelper.getUserMasterBO()
                            .getDistributorAddress1() + "\r\n";
                    Printitem += "T 5 0 10 150 "
                            + ""
                            + bmodel.userMasterHelper.getUserMasterBO()
                            .getDistributorAddress2() + "\r\n";
                }

                if (!mSelectedRetailer.equals("ALL")) {
                    if (IsOriginal)
                        Printitem += "T 5 0 10 170 "
                                + ""
                                + "Original Print" + "\r\n";
                    else
                        Printitem += "T 5 0 10 170 "
                                + ""
                                + "Duplicate print" + "\r\n";
                }

                // T- Text // // Font Size // Spacing // height between lines

                double total = 0;

                for (String groupid : mCollectionReportHelper.getLstPaymentBObyGroupId().keySet()) {
                    PaymentBO payHeaderBO = mCollectionReportHelper.getLstPaymentBObyGroupId().get(groupid).get(0);
                    if (mSelectedRetailer.equals("ALL") || (groupid.equals(mSelectedGroupId) && payHeaderBO.getRetailerName().equals(mSelectedRetailer))) {
                        total = 0;
                        x += 10;
                        Printitem += "T 5 0 10 " + x + " " + "--------------------------------------------------\r\n";

                        x += 20;
                        Printitem += "LEFT \r\n";
                        if (payHeaderBO.getAdvancePaymentId() != null) {
                            Printitem += "T 5 0 10 " + x + " "
                                    + "Rcpt Date:"
                                    + ""
                                    + payHeaderBO.getAdvancePaymentDate() + "\r\n";
                        } else {
                            Printitem += "T 5 0 10 " + x + " "
                                    + "Rcpt Date:"
                                    + ""
                                    + payHeaderBO.getCollectionDateTime() + "\r\n";
                        }
                        x += 40;
                        Printitem += "T 5 0 10 " + x + " "
                                + "Rcpt NO"
                                + ":"
                                + payHeaderBO.getGroupId()
                                + "\r\n";

                        x += 40;
                        Printitem += "T 5 0 260 " + x + " "
                                + "AgentName"
                                + ":"
                                + bmodel.userMasterHelper.getUserMasterBO().getUserName()
                                + "\r\n";

                        Printitem += "LEFT \r\n";
                        Printitem += "T 5 0 10 " + x + " "
                                + "AgentCode:"
                                + ""
                                + bmodel.userMasterHelper.getUserMasterBO()
                                .getUserCode() + "\r\n";
                        String retailername = "";
                        if (payHeaderBO.getRetailerName().length() > 30) {
                            retailername = payHeaderBO.getRetailerName().substring(0, 30);
                        } else {
                            retailername = payHeaderBO.getRetailerName();
                        }

                        x += 40;
                        Printitem += "T 5 0 10 " + x + " "
                                + "CustName"
                                + ":"
                                + retailername
                                + "\r\n";


                        x += 40;
                        Printitem += "LEFT \r\n";
                        Printitem += "T 5 0 10 " + x + " "
                                + "CustCode:"
                                + ""
                                + payHeaderBO.getRetailerCode()
                                + "\r\n";

                        Printitem += "\r\n";

                        x += 40;
                        Printitem += "T 5 0 10 " + x + " " + "--------------------------------------------------\r\n";

                        x += 20;
                        Printitem += "T 5 0 10 " + x + " Inv No" + "\r\n";

                        x += 20;
                        double totalDiscount = 0;
                        Printitem += "T 5 0 80 " + x + " Type" + "\r\n";
                        Printitem += "T 5 0 180 " + x + " Date" + "\r\n";
                        Printitem += "T 5 0 310 " + x + " Chq Num" + "\r\n";
                        Printitem += "T 5 0 450 " + x + " Total" + "\r\n";

                        x += 20;
                        Printitem += "T 5 0 10 " + x + " --------------------------------------------------\r\n";
                        for (int i = 0; i < mCollectionReportHelper.getLstPaymentBObyGroupId().get(groupid).size(); i++) {
                            PaymentBO payBO = mCollectionReportHelper.getLstPaymentBObyGroupId().get(groupid).get(i);

                            x += 40;

                            Printitem += "T 5 0 10 " + x + " "
                                    + (payBO.getBillNumber() != null ? payBO.getBillNumber() : getResources().getString(R.string.advance_payment))
                                    + "\r\n";

                            x += 30;

                            if (payBO.getCashMode().equals(StandardListMasterConstants.CREDIT_NOTE)) {
                                if (payBO.getReferenceNumber().startsWith("AP")) {
                                    Printitem += "T 5 0 80 " + x + " "
                                            + getResources().getString(R.string.advance_payment) + "\r\n";
                                } else {
                                    Printitem += "T 5 0 80 " + x + " "
                                            + getResources().getString(R.string.credit_note) + "\r\n";
                                }
                            } else {
                                if (payBO.getCashMode().equals(StandardListMasterConstants.CASH)) {
                                    Printitem += "T 5 0 80 " + x + " "
                                            + getResources().getString(R.string.cash) + "\r\n";
                                } else if (payBO.getCashMode().equals(StandardListMasterConstants.CHEQUE)) {
                                    Printitem += "T 5 0 80 " + x + " "
                                            + getResources().getString(R.string.cheque) + "\r\n";
                                } else if (payBO.getCashMode().equals(StandardListMasterConstants.DEMAND_DRAFT)) {
                                    Printitem += "T 5 0 80 " + x + " "
                                            + "DD" + "\r\n";
                                } else if (payBO.getCashMode().equals(StandardListMasterConstants.RTGS)) {
                                    Printitem += "T 5 0 80 " + x + " "
                                            + getResources().getString(R.string.rtgs) + "\r\n";
                                } else if (payBO.getCashMode().equals(StandardListMasterConstants.MOBILE_PAYMENT)) {
                                    Printitem += "T 5 0 80 " + x + " "
                                            + "Mob.Pay" + "\r\n";
                                }
                            }


                            Printitem += "T 5 0 180 " + x + " "
                                    + payBO.getChequeDate()
                                    + "\r\n";
                            x = x - 30;
                            if (!payBO.getCashMode().equals(StandardListMasterConstants.CREDIT_NOTE) && !payBO.getCashMode().equals(StandardListMasterConstants.ADVANCE_PAYMENT))
                                Printitem += "T 5 0 310 " + x + " "
                                        + payBO.getChequeNumber() + "\r\n";
                            else {
                                Printitem += "T 5 0 310 " + x + " "
                                        + payBO.getReferenceNumber() + "\r\n";
                            }
                            x = x + 30;

                            Printitem += "T 5 0 450 " + x + " "
                                    + bmodel.formatValue(payBO.getAmount())
                                    + "\r\n";
                            //if pay via credit note than  amt not calculated in net total
                            if (!payBO.getCashMode().equals(StandardListMasterConstants.CREDIT_NOTE))
                                total += payBO.getAmount();
                            totalDiscount += payBO.getAppliedDiscountAmount();

                        }

                        x += 30;
                        Printitem += "T 5 0 10 "
                                + x
                                + " --------------------------------------------------\r\n";
                        x += 30;

                        Printitem += "T 5 0 20 " + x + "Discount " + "\r\n";

                        Printitem += "T 5 0 140 " + x + " "
                                + bmodel.formatValue(totalDiscount) + "\r\n";

                        Printitem += "T 5 0 390 " + x + "Total " + "\r\n";

                        Printitem += "RIGHT \r\n";
                        Printitem += "T 5 0 460 " + x + " "
                                + bmodel.formatValue(total) + "\r\n";

                        x += 30;
                        Printitem += "T 5 0 0 "
                                + x
                                + " --------------------------------------------------\r\n";


                    }
                }


                x += 50;

                Printitem += "\r\n";
                Printitem += "\r\n";
                Printitem += "\r\n";

                Printitem += "T 5 0 10 " + x + "Comments:" + "\r\n";
                Printitem += "T 5 0 150 " + x + " --------------------------\r\n";

                x += 50;
                Printitem += "T 5 0 10 " + x + "Signature:" + "\r\n";
                Printitem += "T 5 0 150 " + x + " --------------------------\r\n";

                Printitem += "PRINT\r\n";

                PrintDataBytes = Printitem.getBytes();
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
        return PrintDataBytes;
    }

    public byte[] printDatafor4inchprinter() {
        byte[] PrintDataBytes = null;
        try {

            PrinterLanguage printerLanguage = PrinterLanguage.CPCL;
            // 00:22:58:3D:7E:83
            // AC:3F:A4:16:B9:AE
            byte[] configLabel = null;
            if (printerLanguage == PrinterLanguage.ZPL) {
                configLabel = "^XA^FO17,16^GB379,371,8^FS^FT65,255^A0N,135,134^FDTEST^FS^XZ"
                        .getBytes();
            } else if (printerLanguage == PrinterLanguage.CPCL) {

                int height = 0;
                int x = 280;
                height = x + mDetails.size() * 350 + 600;
                height = SDUtil.convertToInt(count) * height;
                Commons.print(TAG + "Heigt:" + height);
                String Printitem = "! 0 200 200 " + height + " 1\r\n"
                        + "CENTER\r\n";

                Printitem += "T 5 1 10 140 "
                        + ""
                        + bmodel.userMasterHelper.getUserMasterBO()
                        .getDistributorName() + "\r\n";

                Printitem += "T 5 0 10 180 "
                        + ""
                        + bmodel.userMasterHelper.getUserMasterBO()
                        .getDistributorContactNumber() + "\r\n";

                // T- Text // // Font Size // Spacing // height between lines

                Printitem += "T 5 0 10 200 --------------------------------------------------\r\n";

                Printitem += "T 5 0 520 230 "
                        + getResources().getString(R.string.date)
                        + ":"
                        + DateTimeUtils.convertFromServerDateToRequestedFormat(
                        DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                        bmodel.configurationMasterHelper.outDateFormat)
                        + "\r\n";

                Printitem += "LEFT \r\n";
                Printitem += "T 5 0 20 230 "
                        + "Rep.Name:"
                        + ""
                        + bmodel.userMasterHelper.getUserMasterBO()
                        .getUserName() + "\r\n";

                Printitem += "\r\n";

                Printitem += "T 5 0 10 260 --------------------------------------------------\r\n";

                for (PaymentBO productBO : mDetails) {

                    x += 10;
                    Printitem += "T 5 0 20 " + x + " " + "Outlet Name" + "\r\n";
                    Printitem += "T 5 0 180 " + x + " "
                            + productBO.getRetailerName() + "\r\n";

                    Printitem += "T 5 0 450 " + x + " " + "Inv Amount" + "\r\n";
                    Printitem += "T 5 0 600 " + x + " "
                            + bmodel.formatValue(productBO.getInvoiceAmount())
                            + "\r\n";

                    x += 30;
                    Printitem += "T 5 0 20 " + x + " " + "Inv Date" + "\r\n";
                    Printitem += "T 5 0 180 " + x + " "
                            + productBO.getInvoiceDate() + "\r\n";

                    Printitem += "T 5 0 450 " + x + " " + "OS Amount" + "\r\n";
                    Printitem += "T 5 0 600 " + x + " "
                            + bmodel.formatValue(productBO.getBalance())
                            + "\r\n";

                    x += 40;
                    Printitem += "T 5 0 180 " + x + " " + "Cash" + "\r\n";
                    if (productBO.getCashMode().equals("CA")) {
                        Printitem += "T 5 0 400 " + x + " "
                                + bmodel.formatValue(productBO.getAmount())
                                + "\r\n";
                    } else {
                        Printitem += "T 5 0 400 " + x + " " + "0" + "\r\n";
                    }

                    x += 30;
                    Printitem += "T 5 0 180 " + x + " " + "Cheque" + "\r\n";
                    if (productBO.getCashMode().equals("CQ")) {
                        Printitem += "T 5 0 400 " + x + " "
                                + bmodel.formatValue(productBO.getAmount())
                                + "\r\n";
                    } else {
                        Printitem += "T 5 0 400 " + x + " " + "0" + "\r\n";
                    }

                    x += 30;
                    Printitem += "T 5 0 180 " + x + " " + "Total" + "\r\n";

                    Printitem += "T 5 0 400 " + x + " "
                            + bmodel.formatValue(productBO.getAmount())
                            + "\r\n";
                    x += 20;

                    Printitem += "T 5 0 10 "
                            + x
                            + " --------------------------------------------------\r\n";
                    x += 20;

                }

                x += 30;
                Printitem += "T 5 0 180 " + x + "Total Cash" + "\r\n";

                Printitem += "RIGHT \r\n";
                Printitem += "T 5 0 350 " + x + " "
                        + bmodel.formatValue(totalCash) + "\r\n";
                x += 30;
                Printitem += "T 5 0 180 " + x + "Total Cheque" + "\r\n";

                Printitem += "RIGHT \r\n";
                Printitem += "T 5 0 350 " + x + " "
                        + bmodel.formatValue(totalCheque) + "\r\n";

                x += 30;
                Printitem += "T 5 0 180 " + x + "Total Collected" + "\r\n";

                Printitem += "RIGHT \r\n";
                Printitem += "T 5 0 350 " + x + " "
                        + bmodel.formatValue(totalColl) + "\r\n";

                x += 30;
                Printitem += "T 5 0 10 "
                        + x
                        + " --------------------------------------------------\r\n";

                x += 50;

                Printitem += "\r\n";
                Printitem += "\r\n";
                Printitem += "\r\n";

                Printitem += "T 5 0 20 " + x + "Customer Sign" + "\r\n";
                Printitem += "T 5 0 200 " + x + " -------------\r\n";

                Printitem += "T 5 0 480 " + x + "Rep. Sign" + "\r\n";
                Printitem += "T 5 0 600 " + x + "------------\r\n";

                Printitem += "PRINT\r\n";

                PrintDataBytes = Printitem.getBytes();
            }
        } catch (Exception e) {
        }
        return PrintDataBytes;
    }

    public String getMacAddressFieldText() {
        String macAddress = null;
        try {
            macAddress = mMacAddressET.getText().toString().trim();
            // String macAddress = "00:22:58:08:1E:37";

            SharedPreferences pref = this.getSharedPreferences("PRINT",
                    MODE_PRIVATE);
            Editor editor = pref.edit();
            editor.putString("MAC", macAddress);
            editor.commit();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return macAddress;
    }

    @Override
    public Dialog onCreateDialog(int id) {
        switch (id) {

            case SELECTED_PRINTER_DIALOG:

                AlertDialog.Builder builder2 = new AlertDialog.Builder(
                        CollectionPreviewScreen.this).setTitle("Choose Printer")
                        .setSingleChoiceItems(mPrinterSelectionArray, -1,
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        // TODO Auto-generated method stub

                                        // dismissing the dialog when the user makes
                                        // a selection.
                                        mSelectedPrinterName = mPrinterSelectionArray[which];
                                        dialog.dismiss();
                                        new Thread(new Runnable() {
                                            public void run() {
                                                Looper.prepare();

                                                doConnection(mSelectedPrinterName);
                                                Looper.loop();
                                                Looper.myLooper().quit();
                                            }
                                        }).start();
                                    }
                                });
                bmodel.applyAlertDialogTheme(builder2);
                break;
        }
        return null;

    }

    private String LeftPad(String _num) {

        String str = _num;

        if (_num.length() == 1) {
            str = "0" + _num;
        }

        return str;
    }

    private double getDiscountAppliedValue(double discnt, double totalvalue) {
        double total;
        total = totalvalue;
        Commons.print("discounttype"
                + bmodel.configurationMasterHelper.discountType);
        try {
            if (bmodel.configurationMasterHelper.discountType == 1) {
                if (discnt > 100)
                    discnt = 100;
                total = total - ((total / 100) * discnt);
            } else if (bmodel.configurationMasterHelper.discountType == 2) {
                total = total - discnt;
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
        return total;
    }

    //Bixolon Print Module

    static BixolonPrinter mBixolonPrinter = null;
    String PRINT_STATE = "";
    boolean isconnected;

    private void doConnectBixolon() {
        PRINT_STATE = "";
        disconnectBixolon();
        mBixolonPrinter = new BixolonPrinter(this, mHandlerBixolon, null);
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
                            /*Toast.makeText(getApplicationContext(),
                                    getResources().getString(R.string.connected),
                                    Toast.LENGTH_SHORT).show();*/
                            PRINT_STATE = "TRUE";
                            isconnected = true;
                            supportInvalidateOptionsMenu();
                            mStatusIV.setImageDrawable(getResources().getDrawable(R.drawable.greenball));
                            setStatus("Connected", Color.GREEN);
                            printBixolonData();
                            break;

                        case BixolonPrinter.STATE_CONNECTING:
                            setStatus("Connecting...", Color.BLUE);
                            break;

                        case BixolonPrinter.STATE_NONE:

                            PRINT_STATE = "NO_PRINTER";
                            isconnected = false;
                            break;
                    }
                    return true;

                case BixolonPrinter.MESSAGE_READ:
                    CollectionPreviewScreen.this.dispatchMessage(msg);
                    return true;

                case BixolonPrinter.MESSAGE_TOAST:

                    if (!PRINT_STATE.equalsIgnoreCase("TRUE") && !PRINT_STATE.equalsIgnoreCase("NO_PRINTER")) {
                        mStatusIV.setImageDrawable(getResources().getDrawable(R.drawable.redball));
                        setStatus("Not Connected", Color.RED);
                        Toast.makeText(CollectionPreviewScreen.this.getApplicationContext(), "Printer not connected ..", Toast.LENGTH_SHORT).show();
                    }
                    return true;

                case BixolonPrinter.MESSAGE_BLUETOOTH_DEVICE_SET:
                    if (msg.obj == null) {
                        setStatus("No Paired Device", Color.RED);
                        Toast.makeText(
                                CollectionPreviewScreen.this,
                                getResources().getString(R.string.no_paired_device),
                                Toast.LENGTH_SHORT).show();
                    } else {

                        showBluetoothDialog(getApplicationContext(),
                                (Set<BluetoothDevice>) msg.obj);
                    }
                    return true;

                case BixolonPrinter.MESSAGE_PRINT_COMPLETE:
                    /*Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.complete_to_print),
                            Toast.LENGTH_SHORT).show();*/
                    setStatus("Print Completed", Color.GREEN);
                    return true;

                case BixolonPrinter.MESSAGE_ERROR_OUT_OF_MEMORY:
                    Toast.makeText(CollectionPreviewScreen.this,
                            getResources().getString(R.string.out_of_memory),
                            Toast.LENGTH_SHORT).show();
                    return true;

            }
            return false;
        }

    });

    public Handler getHandlerBixolon() {
        return mHandlerBixolon;
    }

    protected void printBixolonData() {

        try {
            if (PRINT_STATE != null && PRINT_STATE.equals("TRUE")) {


                printTextLeft(printDataforBixolon3inchCollectionReport(SDUtil.convertToInt(count)), DataMembers.PRINT_TEXT_SIZE);


                DemoSleeper.sleep(1500);
                bmodel.showAlert(
                        getResources().getString(
                                R.string.printed_successfully), 5000);

            }


        } catch (Exception e) {
            Commons.print("Print DailyReport Error :" + e.toString());
        } finally {
            disconnectBixolon();
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

                    Toast.makeText(this, buffer.toString(),
                            Toast.LENGTH_SHORT).show();
                }
                break;

            case BixolonPrinter.PROCESS_GET_BATTERY_VOLTAGE_STATUS:
                if (msg.arg2 == BixolonPrinter.STATUS_BATTERY_LOW_VOLTAGE) {
                    Toast.makeText(this,
                            getResources().getString(R.string.low_voltage),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this,
                            getResources().getString(R.string.normal_voltage),
                            Toast.LENGTH_SHORT).show();
                }
                break;

        }
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


    public String printDataforBixolon3inchCollectionReport(int printCount) {
        StringBuffer sb = new StringBuffer();

        for (int i = 1; i <= printCount; i++) {
            if (i == 1 && no_of_print_done == 0)
                IsOriginal = true;
            else {
                IsOriginal = false;
            }
            if (mSelectedRetailer.equals("ALL")) {
                IsOriginal = false;

                int count = 0;
                for (String groupid : mCollectionReportHelper.getLstPaymentBObyGroupId().keySet()) {
                    count = count + 1;
                    if (count == mCollectionReportHelper.getLstPaymentBObyGroupId().size()) {
                        if (count == 1)
                            sb.append(bmodel.printHelper.printDataforBixolon3inchCollectionprinter(true, bmodel.QT(groupid), IsOriginal, true));
                        else
                            sb.append(bmodel.printHelper.printDataforBixolon3inchCollectionprinter(false, bmodel.QT(groupid), IsOriginal, true));
                    } else {
                        if (count == 1)
                            sb.append(bmodel.printHelper.printDataforBixolon3inchCollectionprinter(true, bmodel.QT(groupid), IsOriginal, false));
                        else
                            sb.append(bmodel.printHelper.printDataforBixolon3inchCollectionprinter(false, bmodel.QT(groupid), IsOriginal, false));
                    }
                }
            } else {
                sb.append(bmodel.printHelper.printDataforBixolon3inchCollectionprinter(true, bmodel.QT(mSelectedGroupId), IsOriginal, true));
            }

            if (!mSelectedRetailer.equals("ALL"))
                mCollectionReportHelper.updatePaymentPrintCount(mSelectedGroupId, (SDUtil.convertToInt(count)));
        }
        return sb.toString();
    }


    /**
     * Scribe Printer Connections
     */
    private void doConnectionScribe() {
        try {
            setStatus("Determining Printer Language", Color.YELLOW);
            setStatus("Printer Language ", Color.BLUE);
            setStatus("Connecting...", Color.BLUE);
            new ScribePrinter(new ScribePrinter.ScribeListener() {
                @Override
                public void isScribeResponse(AEMPrinter aemPrinter, AEMScrybeDevice aemScrybeDevice, boolean isConnected) {
                    printScribeData(aemPrinter, aemScrybeDevice, isConnected);
                }
            }).execute();
        } catch (Exception e) {
            e.printStackTrace();
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
                    setStatus("Connected", Color.GREEN);
                    mStatusIV.setImageResource(R.drawable.greenball);

                    aemPrinter.setFontType(AEMPrinter.FONT_NORMAL);
                    aemPrinter.setFontSize(fontSize);
                    aemPrinter.print(printDataforBixolon3inchCollectionReport(SDUtil.convertToInt(count)));
                    aemPrinter.setCarriageReturn();


                    DemoSleeper.sleep(1600 * SDUtil.convertToInt(count));
                    bmodel.showAlert(
                            getResources().getString(
                                    R.string.printed_successfully), 5000);
                    disconnectScribe(aemScrybeDevice);


                } catch (Exception e) {
                    Commons.print("Print CollectionReport Error :" + e.toString());
                } finally {
                    disconnectScribe(aemScrybeDevice);
                }

            }

        } else {
            mStatusIV.setImageDrawable(getResources().getDrawable(R.drawable.redball));
            setStatus("Not Connected", Color.RED);
            Toast.makeText(CollectionPreviewScreen.this.getApplicationContext(), "Printer not connected ..", Toast.LENGTH_SHORT).show();
            disconnectScribe(aemScrybeDevice);
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

    public byte[] printThai() {
        byte[] PrintDataBytes = null;
        try {
            StringBuilder tempsb = new StringBuilder();

            tempsb.append("! U1 SETLP ANG12PT.CPF 0 34 \n");


            tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "CENTER\r\n");
            tempsb.append("SETBOLD 1 \r\n");

            if (bmodel.configurationMasterHelper.SHOW_PRINT_HEADERS) {
                tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 "
                        + "Unipal General Trading Company" + "\r\n");
                tempsb.append("SETBOLD 0 \r\n");
                tempsb.append("PRINT\r\n");

                tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "CENTER\r\n");
                tempsb.append("SETBOLD 1 \r\n");
                tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 " + "VAT No : 562414227" + "\r\n");
                tempsb.append("SETBOLD 0 \r\n");
                tempsb.append("PRINT\r\n");

                tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "CENTER\r\n");
                tempsb.append("SETBOLD 1 \r\n");
                tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 "
                        + "Ramallah - Industrial zone, Tel: +972 2 2981060" + "\r\n");
                tempsb.append("SETBOLD 0 \r\n");
                tempsb.append("PRINT\r\n");

                tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "CENTER\r\n");
                tempsb.append("SETBOLD 1 \r\n");
                tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 "
                        + "Gaza - lndus. Zone - Carny, Tel: +972 7 2830324" + "\r\n");
                tempsb.append("SETBOLD 0 \r\n");
                tempsb.append("PRINT\r\n");
            } else {
                tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 "
                        + bmodel.userMasterHelper.getUserMasterBO()
                        .getDistributorName() + "\r\n");
                tempsb.append("SETBOLD 0 \r\n");
                tempsb.append("PRINT\r\n");

                tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "CENTER\r\n");
                tempsb.append("SETBOLD 1 \r\n");
                tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 "
                        + bmodel.userMasterHelper.getUserMasterBO()
                        .getDistributorTinNumber() + "\r\n");
                tempsb.append("SETBOLD 0 \r\n");
                tempsb.append("PRINT\r\n");

                tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "CENTER\r\n");
                tempsb.append("SETBOLD 1 \r\n");
                tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 "
                        + bmodel.userMasterHelper.getUserMasterBO()
                        .getDistributorAddress1() + "\r\n");
                tempsb.append("SETBOLD 0 \r\n");
                tempsb.append("PRINT\r\n");

                tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "CENTER\r\n");
                tempsb.append("SETBOLD 1 \r\n");
                tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 "
                        + bmodel.userMasterHelper.getUserMasterBO()
                        .getDistributorAddress2() + "\r\n");
                tempsb.append("SETBOLD 0 \r\n");
                tempsb.append("PRINT\r\n");
            }

            if (!mSelectedRetailer.equals("ALL")) {
                tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "CENTER\r\n");
                tempsb.append("SETBOLD 1 \r\n");
                if (IsOriginal) {
                    tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 "
                            + "Original Print" + "\r\n");
                } else {
                    tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 "
                            + "Duplicate print" + "\r\n");
                }
                tempsb.append("SETBOLD 0 \r\n");
                tempsb.append("PRINT\r\n");
            }


            double total;
            double totalDiscount = 0;
            for (String groupid : mCollectionReportHelper.getLstPaymentBObyGroupId().keySet()) {
                PaymentBO payHeaderBO = mCollectionReportHelper.getLstPaymentBObyGroupId().get(groupid).get(0);
                if (mSelectedRetailer.equals("ALL") || (groupid.equals(mSelectedGroupId) && payHeaderBO.getRetailerName().equals(mSelectedRetailer))) {
                    total = 0;
                    tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                    tempsb.append("SETBOLD 1 \r\n");
                    tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 " + "---------------------------------------------------------------------------\r\n");
                    tempsb.append("SETBOLD 0 \r\n");
                    tempsb.append("PRINT\r\n");

                    tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                    tempsb.append("SETBOLD 1 \r\n");
                    if (payHeaderBO.getAdvancePaymentId() != null) {
                        tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 "
                                + getString(R.string.recipt_date)
                                + ":"
                                + payHeaderBO.getAdvancePaymentDate() + "\r\n");
                    } else {
                        tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 "
                                + getString(R.string.recipt_date)
                                + ":"
                                + payHeaderBO.getCollectionDateTime() + "\r\n");
                    }
                    tempsb.append("SETBOLD 0 \r\n");
                    tempsb.append("PRINT\r\n");

                    tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                    tempsb.append("SETBOLD 1 \r\n");
                    tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 "
                            + getString(R.string.recipt_no)
                            + ":"
                            + payHeaderBO.getGroupId()
                            + "\r\n");
                    tempsb.append("SETBOLD 0 \r\n");
                    tempsb.append("PRINT\r\n");

                    tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                    tempsb.append("SETBOLD 1 \r\n");
                    tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 "
                            + getString(R.string.agent_code)
                            + ":"
                            + bmodel.userMasterHelper.getUserMasterBO()
                            .getUserCode() + "\r\n");
                    tempsb.append("SETBOLD 0 \r\n");
                    tempsb.append("PRINT\r\n");

                    tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                    tempsb.append("SETBOLD 1 \r\n");
                    tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 "
                            + getString(R.string.agent_name)
                            + ":"
                            + bmodel.userMasterHelper.getUserMasterBO().getUserName()
                            + "\r\n");
                    tempsb.append("SETBOLD 0 \r\n");
                    tempsb.append("PRINT\r\n");

                    String retailername = "";
                    if (payHeaderBO.getRetailerName().length() > 30) {
                        retailername = payHeaderBO.getRetailerName().substring(0, 30);
                    } else {
                        retailername = payHeaderBO.getRetailerName();
                    }

                    tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                    tempsb.append("SETBOLD 1 \r\n");
                    tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 "
                            + getString(R.string.cust_name)
                            + ":"
                            + retailername
                            + "\r\n");
                    tempsb.append("SETBOLD 0 \r\n");
                    tempsb.append("PRINT\r\n");

                    tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                    tempsb.append("SETBOLD 1 \r\n");
                    tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 "
                            + getString(R.string.cust_code)
                            + ":"
                            + payHeaderBO.getRetailerCode()
                            + "\r\n");
                    tempsb.append("SETBOLD 0 \r\n");
                    tempsb.append("PRINT\r\n");

                    tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                    tempsb.append("SETBOLD 1 \r\n");
                    tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 " + "---------------------------------------------------------------------------\r\n");
                    tempsb.append("SETBOLD 0 \r\n");
                    tempsb.append("PRINT\r\n");


                    tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                    tempsb.append("SETBOLD 1 \r\n");
                    tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 " + getString(R.string.inv_no) + "\r\n");
                    tempsb.append("SETBOLD 0 \r\n");
                    tempsb.append("PRINT\r\n");

                    tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                    tempsb.append("SETBOLD 1 \r\n");
                    tempsb.append("TEXT ANG12PT.CPF 0 " + 40 + " 1 " + getString(R.string.type) + "\r\n");
                    tempsb.append("SETBOLD 0 \r\n");
                    tempsb.append("PRINT");

                    tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                    tempsb.append("SETBOLD 1 \r\n");
                    tempsb.append("TEXT ANG12PT.CPF 0 " + 120 + " 1 " + getString(R.string.date) + "\r\n");
                    tempsb.append("SETBOLD 0 \r\n");
                    tempsb.append("PRINT");

                    tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                    tempsb.append("SETBOLD 1 \r\n");
                    tempsb.append("TEXT ANG12PT.CPF 0 " + 240 + " 1 " + "Chq Num" + "\r\n");
                    tempsb.append("SETBOLD 0 \r\n");
                    tempsb.append("PRINT");

                    tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                    tempsb.append("SETBOLD 1 \r\n");
                    tempsb.append("TEXT ANG12PT.CPF 0 " + 440 + " 1 " + getString(R.string.total) + "\r\n");
                    tempsb.append("SETBOLD 0 \r\n");
                    tempsb.append("PRINT\r\n");

                    tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                    tempsb.append("SETBOLD 1 \r\n");
                    tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 " + " ---------------------------------------------------------------------------\r\n");
                    tempsb.append("SETBOLD 0 \r\n");
                    tempsb.append("PRINT\r\n");
                    for (int i = 0; i < mCollectionReportHelper.getLstPaymentBObyGroupId().get(groupid).size(); i++) {
                        PaymentBO payBO = mCollectionReportHelper.getLstPaymentBObyGroupId().get(groupid).get(i);


                        tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                        tempsb.append("SETBOLD 1 \r\n");
                        tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 "
                                + (payBO.getBillNumber() != null ? payBO.getBillNumber() : getResources().getString(R.string.advance_payment))
                                + "\r\n");
                        tempsb.append("SETBOLD 0 \r\n");
                        tempsb.append("PRINT\r\n");

                        tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                        tempsb.append("SETBOLD 1 \r\n");
                        if (payBO.getCashMode().equals(StandardListMasterConstants.CREDIT_NOTE)) {
                            if (payBO.getReferenceNumber().startsWith("AP")) {
                                tempsb.append("TEXT ANG12PT.CPF 0 " + 40 + " 1 "
                                        + getResources().getString(R.string.advance_payment) + "\r\n");
                            } else {
                                tempsb.append("TEXT ANG12PT.CPF 0 " + 40 + " 1 "
                                        + getResources().getString(R.string.credit_note) + "\r\n");
                            }
                        } else {
                            if (payBO.getCashMode().equals(StandardListMasterConstants.CASH)) {
                                tempsb.append("TEXT ANG12PT.CPF 0 " + 40 + " 1 "
                                        + getResources().getString(R.string.cash) + "\r\n");
                            } else if (payBO.getCashMode().equals(StandardListMasterConstants.CHEQUE)) {
                                tempsb.append("TEXT ANG12PT.CPF 0 " + 40 + " 1 "
                                        + getResources().getString(R.string.cheque) + "\r\n");
                            } else if (payBO.getCashMode().equals(StandardListMasterConstants.DEMAND_DRAFT)) {
                                tempsb.append("TEXT ANG12PT.CPF 0 " + 40 + " 1 "
                                        + "DD" + "\r\n");
                            } else if (payBO.getCashMode().equals(StandardListMasterConstants.RTGS)) {
                                tempsb.append("TEXT ANG12PT.CPF 0 " + 40 + " 1 "
                                        + getResources().getString(R.string.rtgs) + "\r\n");
                            } else if (payBO.getCashMode().equals(StandardListMasterConstants.MOBILE_PAYMENT)) {
                                tempsb.append("TEXT ANG12PT.CPF 0 " + 40 + " 1 "
                                        + "Mob.Pay" + "\r\n");
                            }
                        }
                        tempsb.append("SETBOLD 0 \r\n");
                        tempsb.append("PRINT");


                        tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                        tempsb.append("SETBOLD 1 \r\n");
                        tempsb.append("TEXT ANG12PT.CPF 0 " + 120 + " 1 "
                                + payBO.getChequeDate()
                                + "\r\n");
                        tempsb.append("SETBOLD 0 \r\n");
                        tempsb.append("PRINT");

                        tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                        tempsb.append("SETBOLD 1 \r\n");
                        if (!payBO.getCashMode().equals(StandardListMasterConstants.CREDIT_NOTE) && !payBO.getCashMode().equals(StandardListMasterConstants.ADVANCE_PAYMENT))
                            tempsb.append("TEXT ANG12PT.CPF 0 " + 240 + " 1 "
                                    + payBO.getChequeNumber() + "\r\n");
                        else {
                            tempsb.append("TEXT ANG12PT.CPF 0 " + 240 + " 1 "
                                    + payBO.getReferenceNumber() + "\r\n");
                        }
                        tempsb.append("SETBOLD 0 \r\n");
                        tempsb.append("PRINT");


                        tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                        tempsb.append("SETBOLD 1 \r\n");
                        tempsb.append("TEXT ANG12PT.CPF 0 " + 440 + " 1 "
                                + bmodel.formatValue(payBO.getAmount())
                                + "\r\n");
                        tempsb.append("SETBOLD 0 \r\n");
                        tempsb.append("PRINT\r\n");

                        total += payBO.getAmount();
                        totalDiscount += payBO.getAppliedDiscountAmount();

                    }

                    tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                    tempsb.append("SETBOLD 1 \r\n");
                    tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 " + "---------------------------------------------------------------------------\r\n");
                    tempsb.append("SETBOLD 0 \r\n");
                    tempsb.append("PRINT\r\n");

                    tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                    tempsb.append("SETBOLD 1 \r\n");
                    tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 " + getString(R.string.discount) + "\r\n");
                    tempsb.append("SETBOLD 0 \r\n");
                    tempsb.append("PRINT");

                    tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                    tempsb.append("SETBOLD 1 \r\n");
                    tempsb.append("TEXT ANG12PT.CPF 0 " + 170 + " 1 "
                            + bmodel.formatValue(totalDiscount) + "\r\n");
                    tempsb.append("SETBOLD 0 \r\n");
                    tempsb.append("PRINT");

                    tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                    tempsb.append("SETBOLD 1 \r\n");
                    tempsb.append("TEXT ANG12PT.CPF 0 " + 360 + " 1 " + getString(R.string.total) + "\r\n");
                    tempsb.append("SETBOLD 0 \r\n");
                    tempsb.append("PRINT");

                    tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                    tempsb.append("SETBOLD 1 \r\n");
                    tempsb.append("TEXT ANG12PT.CPF 0 " + 440 + " 1 "
                            + bmodel.formatValue(total) + "\r\n");
                    tempsb.append("SETBOLD 0 \r\n");
                    tempsb.append("PRINT\r\n");

                    tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                    tempsb.append("SETBOLD 1 \r\n");
                    tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 " + "---------------------------------------------------------------------------\r\n");
                    tempsb.append("SETBOLD 0 \r\n");
                    tempsb.append("PRINT\r\n");


                }
            }

            tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
            tempsb.append("SETBOLD 1 \r\n");
            tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 " + getString(R.string.comments)+":------------------------------------------------------------\r\n");
            tempsb.append("SETBOLD 0 \r\n");
            tempsb.append("PRINT\r\n");

            tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
            tempsb.append("SETBOLD 1 \r\n");
            tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 " + "Signature:--------------------------------------------------------------\r\n");
            tempsb.append("SETBOLD 0 \r\n");
            tempsb.append("PRINT\r\n");
            tempsb.append("\r\n");

            PrintDataBytes = String.valueOf(tempsb).getBytes("ISO-8859-11");

        } catch (Exception e) {
            Commons.printException(e);
        }
        return PrintDataBytes;
    }

}