package com.ivy.cpg.view.reports.invoicereport;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.order.OrderHelper;
import com.ivy.cpg.view.order.scheme.SchemeDetailsMasterHelper;
import com.ivy.cpg.view.van.LoadManagementHelper;
import com.ivy.lib.Utils;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.cpg.view.order.scheme.SchemeProductBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.ReportHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.sd.print.BtService;
import com.ivy.sd.print.CommonPrintPreviewActivity;
import com.ivy.sd.print.DemoSleeper;
import com.ivy.sd.print.SettingsHelper;
import com.tremol.zfplibj.ZFPLib;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;

import java.util.ArrayList;
import java.util.Vector;

public class InvoiceReportDetail extends IvyBaseActivityNoActionBar implements
        OnClickListener {

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    private static final String TAG = "InvoiceReportDetail";
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final String ZEBRA_3INCH = "3";
    public static Resources mRes = null;

    public Vector<ProductMasterBO> mProducts = new Vector<>();
    public ArrayList<ProductMasterBO> mProductsForAdapter = new ArrayList<>();

    boolean isPrinterLanguageDetected = false;
    private SharedPreferences sharedPreferences;
    private double tot = 0;
    private float totalWeight = 0;
    private String mInvoiceId;
    private int mSelectedPrintCount = 0;

    private BusinessModel businessModel;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BtService mChatService = null;
    private Connection zebraPrinterConnection;
    private AlertDialog.Builder build;
    private AlertDialog alertDialog;
    private OrderHelper orderHelper;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_invoice_report_detail);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

        businessModel = (BusinessModel) getApplicationContext();
        businessModel.setContext(this);
        orderHelper = OrderHelper.getInstance(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey("TotalAmount")) {
                tot = extras.getDouble("TotalAmount");
            }
            if (extras.containsKey("lineinvoice")) {
                mInvoiceId = extras.getString("lineinvoice");
            }
            if (extras.containsKey("TotalWeight")) {
                totalWeight = extras.getFloat("TotalWeight");
            }
        }
        try {
            Toolbar toolbar = findViewById(R.id.toolbar);
            ReportHelper reportHelper = ReportHelper.getInstance(this);

            TextView text_totalValue = findViewById(R.id.txttotal);
            TextView text_totalLines = findViewById(R.id.txttotalqty);
            TextView label_totalLines = findViewById(R.id.TextView52);

            ListView listView = findViewById(R.id.lvwplistorddet);
            listView.setCacheColorHint(0);
            ExpandableListView expandableListView = findViewById(R.id.elv);

            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null)
                setScreenTitle(getResources().getString(R.string.invoice_report_details));
            getSupportActionBar().setIcon(null);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);


            if (!businessModel.configurationMasterHelper.SHOW_ORDER_CASE) {
                findViewById(R.id.cqty).setVisibility(View.GONE);
            } else {
                try {
                    if (businessModel.labelsMasterHelper.applyLabels(findViewById(
                            R.id.cqty).getTag()) != null)
                        ((TextView) findViewById(R.id.cqty))
                                .setText(businessModel.labelsMasterHelper
                                        .applyLabels(findViewById(
                                                R.id.cqty).getTag()));
                } catch (Exception e) {
                    Commons.printException(e);
                }
            }
            if (!businessModel.configurationMasterHelper.SHOW_ORDER_PCS) {
                findViewById(R.id.outid).setVisibility(View.GONE);
            } else {
                try {
                    if (businessModel.labelsMasterHelper.applyLabels(findViewById(
                            R.id.outid).getTag()) != null)
                        ((TextView) findViewById(R.id.outid))
                                .setText(businessModel.labelsMasterHelper
                                        .applyLabels(findViewById(
                                                R.id.outid).getTag()));
                } catch (Exception e) {
                    Commons.printException(e);
                }
            }
            if (!businessModel.configurationMasterHelper.SHOW_OUTER_CASE) {
                findViewById(R.id.outercqty).setVisibility(View.GONE);
            } else {
                try {
                    if (businessModel.labelsMasterHelper.applyLabels(findViewById(
                            R.id.outercqty).getTag()) != null)
                        ((TextView) findViewById(R.id.outercqty))
                                .setText(businessModel.labelsMasterHelper
                                        .applyLabels(findViewById(
                                                R.id.outercqty)
                                                .getTag()));
                } catch (Exception e) {
                    Commons.printException(e);
                }
            }


            //total weight
            if (!businessModel.configurationMasterHelper.SHOW_ORDER_WEIGHT) {
                findViewById(R.id.lbl_totWgt).setVisibility(View.GONE);
                findViewById(R.id.txt_totwgt).setVisibility(View.GONE);
            } else {
                try {
                    if (businessModel.labelsMasterHelper.applyLabels(findViewById(
                            R.id.lbl_totWgt).getTag()) != null)
                        ((TextView) findViewById(R.id.lbl_totWgt))
                                .setText(businessModel.labelsMasterHelper
                                        .applyLabels(findViewById(
                                                R.id.lbl_totWgt)
                                                .getTag()));
                } catch (Exception e) {
                    Commons.printException(e);
                }
                ((TextView) findViewById(R.id.txt_totwgt)).setText(Utils.formatAsTwoDecimal((double) totalWeight));
            }

            mProducts = businessModel.productHelper.getProductMaster();

            sharedPreferences = getSharedPreferences(BusinessModel.PREFS_NAME, MODE_PRIVATE);
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            mRes = getResources();

            double totalLines = 0;
            int totalAllQty = 0;
            if (businessModel.configurationMasterHelper.COMMON_PRINT_ZEBRA
                    || businessModel.configurationMasterHelper.COMMON_PRINT_SCRYBE
                    || businessModel.configurationMasterHelper.COMMON_PRINT_BIXOLON
                    || businessModel.configurationMasterHelper.COMMON_PRINT_LOGON
                    || businessModel.configurationMasterHelper.COMMON_PRINT_INTERMEC) {
                // All products not need to load.only invoice products loaded from
                // sqLite and stored in object.Because invoice print file saved in sdcard
                // we can show other details using the text file
                mProductsForAdapter = reportHelper.getReportDetails(mInvoiceId);
            } else {
                for (ProductMasterBO productBO : mProducts) {
                    if ((productBO.getOrderedPcsQty() > 0
                            || productBO.getOrderedCaseQty() > 0 || productBO
                            .getOrderedOuterQty() > 0)) {
                        totalLines = totalLines + 1;

                        if (businessModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION && productBO.getBatchwiseProductCount() > 0) {
                            ArrayList<ProductMasterBO> batchList = businessModel.batchAllocationHelper.getBatchlistByProductID().get(productBO.getProductID());
                            if (batchList != null) {
                                for (ProductMasterBO batchProductBO : batchList) {
                                    int totalQty = batchProductBO.getOrderedPcsQty() + (batchProductBO.getOrderedCaseQty() * productBO.getCaseSize())
                                            + (batchProductBO.getOrderedOuterQty() * productBO.getOutersize());
                                    totalAllQty = totalAllQty + totalQty;
                                    if (totalQty > 0) {
                                        batchProductBO.setProductShortName(productBO.getProductShortName());
                                        batchProductBO.setTotalamount(batchProductBO.getNetValue());
                                        mProductsForAdapter.add(batchProductBO);
                                    }
                                }
                            }

                        } else {
                            productBO.setBatchNo("");
                            int totalQty = productBO.getOrderedPcsQty() + productBO.getOrderedCaseQty() * productBO.getCaseSize() + productBO.getOrderedOuterQty() * productBO.getOutersize();
                            totalAllQty = totalAllQty + totalQty;
                            mProductsForAdapter.add(productBO);
                        }

                    }

                }
            }

            ArrayList<SchemeProductBO> schemeProductList;
            if (businessModel.configurationMasterHelper.COMMON_PRINT_BIXOLON
                    || businessModel.configurationMasterHelper.COMMON_PRINT_SCRYBE
                    || businessModel.configurationMasterHelper.COMMON_PRINT_ZEBRA
                    || businessModel.configurationMasterHelper.COMMON_PRINT_LOGON
                    || businessModel.configurationMasterHelper.COMMON_PRINT_INTERMEC) {
                schemeProductList = reportHelper.getSchemeProductDetails(mInvoiceId, true);
            } else {
                //load accumulation scheme free products
                schemeProductList = SchemeDetailsMasterHelper.getInstance(getApplicationContext()).downLoadAccumulationSchemeDetailReport(getApplicationContext(), mInvoiceId, true);
            }
            if (schemeProductList != null &&
                    mProductsForAdapter != null) {
                if (mProductsForAdapter.get(mProductsForAdapter.size() - 1).getSchemeProducts() != null)
                    mProductsForAdapter.get(mProductsForAdapter.size() - 1).getSchemeProducts().addAll(schemeProductList);
                else
                    mProductsForAdapter.get(mProductsForAdapter.size() - 1).setSchemeProducts(schemeProductList);
            }


            text_totalValue.setText(businessModel.formatValue(tot));

            if (businessModel.configurationMasterHelper.SHOW_TOTAL_LINES) {
                if (businessModel.configurationMasterHelper.SHOW_TOTAL_QTY_IN_ORDER_REPORT) {

                    if (mProductsForAdapter != null) {
                        for (ProductMasterBO productMasterBO : mProductsForAdapter) {
                            totalAllQty = totalAllQty + productMasterBO.getTotalQty();
                        }
                    }
                    text_totalLines.setText(String.valueOf(totalAllQty));
                    label_totalLines.setText(getResources().getString(R.string.tot_qty));
                } else {
                    totalLines = mProductsForAdapter.size();
                    text_totalLines.setText(String.valueOf(totalLines));
                    label_totalLines.setText(getResources().getString(R.string.tot_line));
                }

            } else {
                label_totalLines.setVisibility(View.GONE);
                text_totalLines.setVisibility(View.GONE);
            }

            // Show alert if error loading data.
            if (mProductsForAdapter == null) {
                businessModel.showAlert(
                        getResources().getString(R.string.unable_to_load_data),
                        0);
                return;
            }
            // Show alert if no order exist.
            if (mProductsForAdapter.size() == 0) {
                businessModel.showAlert(
                        getResources().getString(R.string.no_orders_available),
                        0);
                return;
            }

            // Load list view.
            expandableListView.setAdapter(new MyAdapter());
            int orderedProductCount = mProductsForAdapter.size();
            for (int i = 0; i < orderedProductCount; i++) {
                (expandableListView).expandGroup(i);
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    @Override
    public void onClick(View comp) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_invoice_report, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            onBackButtonClick();

        } else if (i == R.id.menu_print) {
            businessModel.invoiceNumber = mInvoiceId;
            OrderHelper.getInstance(this).getPrintedCountForCurrentInvoice(getApplicationContext());

            Intent intent = new Intent();

            if (businessModel.configurationMasterHelper.SHOW_ZEBRA_UNIPAL || businessModel.configurationMasterHelper.SHOW_ZEBRA_TITAN) {
                showDialog(2);
            } else if (businessModel.configurationMasterHelper.COMMON_PRINT_ZEBRA
                    || businessModel.configurationMasterHelper.COMMON_PRINT_BIXOLON
                    || businessModel.configurationMasterHelper.COMMON_PRINT_SCRYBE
                    || businessModel.configurationMasterHelper.COMMON_PRINT_LOGON
                    || businessModel.configurationMasterHelper.COMMON_PRINT_INTERMEC) {
                // Print file already saved.so not need to reload the object.we can get the object from print text file
                businessModel.mCommonPrintHelper.readBuilder(StandardListMasterConstants.PRINT_FILE_INVOICE + businessModel.invoiceNumber + ".txt",
                        DataMembers.PRINT_FILE_PATH);
                intent.setClass(InvoiceReportDetail.this,
                        CommonPrintPreviewActivity.class);
                intent.putExtra("IsUpdatePrintCount", true);
                intent.putExtra("isHomeBtnEnable", true);
                intent.putExtra("isFromInvoice", true);
            }

            if (!businessModel.configurationMasterHelper.SHOW_ZEBRA_UNIPAL && !businessModel.configurationMasterHelper.SHOW_ZEBRA_TITAN) {
                intent.putExtra("IsFromReport", true);
                startActivityForResult(intent, 0);
            }


        }
        return super.onOptionsItemSelected(item);
    }

    private void onBackButtonClick() {
        finish();
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
    }

    public Handler getHandler() {
        return handler;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (REQUEST_CONNECT_DEVICE == requestCode) {// When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                String address = sharedPreferences.getString("MAC", "");
                //String address="00:12:6F:23:47:C8";
//			String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                mChatService.connect(device);
            }
        }
        if (REQUEST_ENABLE_BT == requestCode) {    // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                Commons.print(TAG + ",BT enabled");
            } else {
                Commons.print(TAG + ",BT not enabled");
                Toast.makeText(this, "Blue tooth not enable", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        if (requestCode == 0) {
            if (resultCode == RESULT_CANCELED) {
                finish();
            }
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {


        switch (id) {
            case 0:
                AlertDialog.Builder builder = new AlertDialog.Builder(InvoiceReportDetail.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(getResources().getString(R.string.are_you_sure_you_want_to_print))
                        .setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        if (mBluetoothAdapter == null) {
                                            Toast.makeText(getApplicationContext(), "Bluetooth not enabled ", Toast.LENGTH_LONG).show();
                                            //!!!!!
                                            finish();
                                            return;
                                        }
                                        SchemeDetailsMasterHelper.getInstance(getApplicationContext()).downloadSchemeReport(getApplicationContext(), mInvoiceId, true);
                                        checkBluetoothEnabled();


                                    }
                                })

                        .setNegativeButton(
                                getResources().getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

                                    }
                                });
                businessModel.applyAlertDialogTheme(builder);
                break;
            case 2:
                AlertDialog.Builder builder9 = new AlertDialog.Builder(InvoiceReportDetail.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle("Do you want to Print?")
                        .setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        if (businessModel.configurationMasterHelper.printCount > 0) {
                                            mSelectedPrintCount = businessModel.configurationMasterHelper.printCount;
                                            showDialog(3);
                                        } else {
                                            new Thread(new Runnable() {
                                                public void run() {


                                                    Looper.prepare();
                                                    doConnection(ZEBRA_3INCH);
                                                    Looper.loop();
                                                    Looper.myLooper().quit();
                                                }

                                            }).start();

                                            build = new AlertDialog.Builder(InvoiceReportDetail.this);
                                            customProgressDialog(build, "Printing....");
                                            alertDialog = build.create();
                                            alertDialog.show();
                                        }

                                    }
                                })

                        .setNegativeButton(
                                getResources().getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

                                    }
                                });
                businessModel.applyAlertDialogTheme(builder9);
                break;
            case 3:
                AlertDialog.Builder builder11 = new AlertDialog.Builder(InvoiceReportDetail.this)
                        .setTitle("Print Count")
                        .setSingleChoiceItems(businessModel.printHelper.getPrintCountArray(), 0, null)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                                mSelectedPrintCount = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                                new Thread(new Runnable() {
                                    public void run() {
                                        Looper.prepare();
                                        doConnection(ZEBRA_3INCH);
                                        Looper.loop();
                                        Looper.myLooper().quit();
                                    }
                                }).start();

                                build = new AlertDialog.Builder(InvoiceReportDetail.this);
                                customProgressDialog(build, "Printing....");
                                alertDialog = build.create();
                                alertDialog.show();
                            }
                        });
                businessModel.applyAlertDialogTheme(builder11);

                break;
        }
        return null;
    }

    public void checkBluetoothEnabled() {
        try {
            if (!mBluetoothAdapter.isEnabled()) // If BT is not on, request that it
            // be enabled. setup will then be
            // called during onActivityResult
            {
                Toast.makeText(this, " Bluetooth Not Enabled", Toast.LENGTH_SHORT).show();
                finish();
            } else { // Otherwise, setup the chat session
                if (mChatService == null) {
                    mChatService = new BtService(getApplicationContext(), handler);

                    String address = sharedPreferences.getString("MAC", "");
                    BluetoothDevice device = mBluetoothAdapter
                            .getRemoteDevice(address);
                    mChatService.connect(device);

                    if (mChatService.getState() != BtService.STATE_CONNECTED)
                        Toast.makeText(this, "Not connected", Toast.LENGTH_SHORT).show();


                }

            }
        } catch (Exception e) {
            Commons.printException(e);
            Toast.makeText(this, "Please check mac address ", Toast.LENGTH_SHORT).show();
            finish();

        }
    }

    public void printData() {
        try {

            ZFPLib zfp = mChatService.zfplib;
            zfp.openFiscalBon(1, "0000", false, false, false);
            boolean isGoldenStore = false;
            for (ProductMasterBO productBO : mProducts) {

                double vatAmount = 0.0;


                if ((productBO.getOrderedPcsQty() > 0
                        || productBO.getOrderedCaseQty() > 0 || productBO
                        .getOrderedOuterQty() > 0)) {

                    float pieceCount = (productBO.getOrderedCaseQty() * productBO
                            .getCaseSize())
                            + (productBO.getOrderedPcsQty())
                            + (productBO.getOrderedOuterQty() * productBO
                            .getOutersize());

                    float total = pieceCount * productBO.getSrp();


                    float mTaxDiscount = (((float) vatAmount * 100) / total);

                    double percent = 0;
                    if (productBO.isPromo()) {

                        percent = productBO.getMschemeper();

                    }

                    float mGoldenStore = 0;
                    if (businessModel.configurationMasterHelper.SHOW_GOLD_STORE_DISCOUNT
                            && businessModel.productHelper.isGoldenStoreInCurrentandLastVisit()
                            ) {

                        mGoldenStore = (float) businessModel.productHelper.applyGoldStoreLineDiscount();
                        isGoldenStore = true;
                    }

                    float discount = (float) percent + mGoldenStore;
                    char mTaxGroup;
                    if (Math.round(mTaxDiscount) == 16) {
                        mTaxGroup = '1';
                    } else if (Math.round(mTaxDiscount) == 18) {
                        mTaxGroup = '2';
                    } else {
                        mTaxGroup = '0';
                    }
                    Commons.printException("taxdisc=" + Math.round(mTaxDiscount) + "taxgrp=" + mTaxGroup + " percent=" + -discount + "sku.isPromo()=" + productBO.isPromo());


                    zfp.sellFree(productBO.getProductShortName(), mTaxGroup, productBO.getSrp(), pieceCount, -discount);

                }
            }
            double sum = zfp.calcIntermediateSum(false, false, false, 0.0f, '0');
            zfp.payment(sum, 0, false);
            if (isGoldenStore) {
                zfp.printText("**Golden store applied**", 2);
            }
            zfp.closeFiscalBon();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null)
            mChatService.stop();

    }

    public void disconnect() {
        try {

            if (zebraPrinterConnection != null) {
                zebraPrinterConnection.close();
            }

        } catch (ConnectionException e) {

            Commons.printException(e);
        }
    }

    public ZebraPrinter connect() {
        zebraPrinterConnection = null;
        String macAddress = getMacAddressFieldText();
        zebraPrinterConnection = new BluetoothConnection(
                macAddress);
        SettingsHelper.saveBluetoothAddress(this, macAddress);


        try {
            zebraPrinterConnection.open();


        } catch (ConnectionException e) {

            Commons.printException(e);

            DemoSleeper.sleep(1000);

            disconnect();
        } catch (Exception e) {


            Commons.printException(e);
        }

        ZebraPrinter printer = null;

        isPrinterLanguageDetected = false;

        if (zebraPrinterConnection.isConnected()) {
            try {
                printer = ZebraPrinterFactory.getInstance(PrinterLanguage.CPCL,
                        zebraPrinterConnection);

                PrinterLanguage pl = printer.getPrinterControlLanguage();

                Commons.print(TAG + "PRINT LANGUAGE : " + pl);
                isPrinterLanguageDetected = true;
            } catch (ConnectionException e) {

                Commons.print(TAG
                        + "PRINT LANGUAGE : UNKNOWN : PrinterConnectionException");
                Commons.printException(e);
                isPrinterLanguageDetected = false;
            }
        }

        return printer;
    }

    private void doConnection(String printerName) {
        try {
            ZebraPrinter printer = connect();
            if (printer != null) {
                LoadManagementHelper.getInstance(this).downloadSubDepots();
                printInvoice(printerName);
            } else {
                businessModel.productHelper.clearOrderTable();
                disconnect();
                alertDialog.dismiss();
                Toast.makeText(this, "Printer not connected .Please check  Mac Address..", Toast.LENGTH_SHORT).show();

            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public String getMacAddressFieldText() {
        String macAddress = null;
        try {
            SharedPreferences pref = getSharedPreferences("PRINT",
                    MODE_PRIVATE);
            macAddress = pref.getString("MAC", "");
        } catch (Exception e) {
            Commons.printException(e);
        }
        return macAddress;
    }

    public void printInvoice(String printername) {
        int count = 0;
        try {
            OrderHelper.getInstance(this).getPrintedCountForCurrentInvoice(getApplicationContext());
            businessModel.printHelper.setPrintCnt(orderHelper.getPrint_count());
            if (printername.equals(ZEBRA_3INCH)) {

                if (businessModel.configurationMasterHelper.SHOW_ZEBRA_UNIPAL) {

                    for (int i = 0; i < mSelectedPrintCount + 1; i++) {
                        count = count + 1;
                        zebraPrinterConnection.write(businessModel.printHelper.printDatafor3inchprinterForUnipal(mProductsForAdapter, false, 1));
                        businessModel.updatePrintCount(1);
                        businessModel.printHelper.setPrintCnt(OrderHelper.getInstance(this).getPrintedCountForCurrentInvoice(getApplicationContext()));

                    }

                    ////
                } else if (businessModel.configurationMasterHelper.SHOW_ZEBRA_TITAN) {
                    double entryLevelDiscountValue = 0;
                    if (businessModel.configurationMasterHelper.IS_PRODUCT_DISCOUNT_BY_USER_ENTRY) {
                        entryLevelDiscountValue = businessModel.printHelper.getEntryLevelDiscountValue(mProductsForAdapter);
                    }

                    for (int i = 0; i < mSelectedPrintCount + 1; i++) {
                        zebraPrinterConnection.write(businessModel.printHelper.printDataforTitan3inchprinter(mProductsForAdapter, entryLevelDiscountValue, 0, true));
                        count = count + 1;

                    }

                }


                alertDialog.dismiss();
                businessModel.productHelper.clearOrderTable();

                businessModel.showAlert(
                        getResources().getString(
                                R.string.printed_successfully), 5002);

            }

            DemoSleeper.sleep(1500);
            if (zebraPrinterConnection instanceof BluetoothConnection) {
                String friendlyName = ((BluetoothConnection) zebraPrinterConnection)
                        .getFriendlyName();

                Commons.print(TAG + "friendlyName : " + friendlyName);
                DemoSleeper.sleep(500);
            }
        } catch (ConnectionException e) {
            Commons.printException(e);

        } catch (Exception e) {
            Commons.printException(e);
        } finally {
            businessModel.updatePrintCount(orderHelper.getPrint_count() + count);
            disconnect();
        }
    }

    class MyAdapter extends BaseExpandableListAdapter {

        @Override
        public Object getChild(int arg0, int arg1) {
            return null;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(R.layout.row_invoice_report_detail,
                        parent, false);
                holder = new ViewHolder();
                holder.productShortName = (TextView) row.findViewById(R.id.prd_nameTv);
                holder.productShortName.setMaxLines(businessModel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);
                holder.productCode = (TextView) row.findViewById(R.id.product_code);
                holder.text_PcsQuantity = (TextView) row.findViewById(R.id.PRDPCSQTY);
                holder.text_caseQuantity = (TextView) row.findViewById(R.id.PRDQTY);
                holder.text_value = (TextView) row.findViewById(R.id.PRDVAL);
                holder.tvBatchNo = (TextView) row.findViewById(R.id.batch_no);
                holder.outerQty = (TextView) row
                        .findViewById(R.id.outerCaseQty);

                row.setOnClickListener(new OnClickListener() {

                    public void onClick(View v) {
                        //   productName.setText(holder.productName);
                    }
                });

                // On/Off order case and pce
                if (!businessModel.configurationMasterHelper.SHOW_ORDER_CASE)
                    holder.text_caseQuantity.setVisibility(View.VISIBLE);

                if (!businessModel.configurationMasterHelper.SHOW_ORDER_PCS)
                    holder.text_PcsQuantity.setVisibility(View.GONE);
                if (!businessModel.configurationMasterHelper.SHOW_OUTER_CASE)
                    holder.outerQty.setVisibility(View.VISIBLE);
                if (!businessModel.configurationMasterHelper.IS_SHOW_SKU_CODE)
                    holder.productCode.setVisibility(View.GONE);

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            SchemeProductBO productBO = mProductsForAdapter.get(groupPosition)
                    .getSchemeProducts().get(childPosition);

            holder.productShortName.setText(productBO.getProductName());

            if (businessModel.configurationMasterHelper.IS_SHOW_SKU_CODE) {
                String prodCode = getResources().getString(R.string.prod_code)
                        + ": " + productBO.getProductCode() + " ";
                if (businessModel.labelsMasterHelper.applyLabels(holder.productCode.getTag()) != null)
                    prodCode = businessModel.labelsMasterHelper
                            .applyLabels(holder.productCode.getTag()) + ": " +
                            productBO.getProductCode() + " ";
                holder.productCode.setText(prodCode);
            }

            holder.productName = productBO.getProductFullName();
            if (businessModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION) {
                if (productBO.getBatchId() != null && !productBO.getBatchId().equals("null"))
                    holder.tvBatchNo.setText(productBO.getBatchId());
            }

            if (businessModel.configurationMasterHelper.COMMON_PRINT_BIXOLON
                    || businessModel.configurationMasterHelper.COMMON_PRINT_SCRYBE
                    || businessModel.configurationMasterHelper.COMMON_PRINT_ZEBRA
                    || businessModel.configurationMasterHelper.COMMON_PRINT_LOGON
                    || businessModel.configurationMasterHelper.COMMON_PRINT_INTERMEC) {
                if (productBO.getUomDescription().equals("CASE")) {
                    holder.text_caseQuantity.setText(String.valueOf(productBO.getQuantitySelected()));
                    holder.text_PcsQuantity.setText("0");
                } else if (productBO.getUomDescription().equals("OUTER")) {
                    holder.outerQty.setText(String.valueOf(productBO.getQuantitySelected()));
                    holder.text_PcsQuantity.setText("0");
                    holder.text_caseQuantity.setText("0");
                } else {
                    holder.text_PcsQuantity.setText(String.valueOf(productBO.getQuantitySelected()));
                    holder.text_caseQuantity.setText("0");
                    holder.outerQty.setText("0");
                }
            } else {

                holder.productBO = businessModel.productHelper
                        .getProductMasterBOById(productBO.getProductId());
                if (holder.productBO != null) {
                    if (holder.productBO.getCaseUomId() == productBO.getUomID()
                            && holder.productBO.getCaseUomId() != 0) {
                        // case wise free quantity update

                        holder.text_caseQuantity.setText(String.valueOf(productBO.getQuantitySelected()));
                        holder.text_PcsQuantity.setText("0");

                    } else if (holder.productBO.getOuUomid() == productBO
                            .getUomID() && holder.productBO.getOuUomid() != 0) {
                        // outer wise free quantity update
                        holder.outerQty.setText(String.valueOf(productBO.getQuantitySelected()));
                        holder.text_PcsQuantity.setText("0");
                        holder.text_caseQuantity.setText("0");
                    } else {
                        holder.text_PcsQuantity.setText(String.valueOf(productBO.getQuantitySelected()));
                        holder.text_caseQuantity.setText("0");
                        holder.outerQty.setText("0");
                    }
                }
            }

            holder.text_value.setText("0");
            return row;
        }

        @Override
        public int getChildrenCount(int groupPosition) {

            if (mProductsForAdapter.get(groupPosition).getSchemeProducts() != null
                    && mProductsForAdapter.get(groupPosition).getSchemeProducts().size() > 0) {

                //if (SchemeDetailsMasterHelper.getInstance(getApplicationContext()).getSchemeById().get(mProductsForAdapter.get(groupPosition).getSchemeProducts().get(0).getSchemeId()).isOffScheme()) {
                    return mProductsForAdapter.get(groupPosition).getSchemeProducts().size();
                //}
            }

            return 0;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return null;
        }

        @Override
        public int getGroupCount() {
            if (mProductsForAdapter == null)
                return 0;

            return mProductsForAdapter.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return 0;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            final ViewHolder holder;

            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(R.layout.row_invoice_report_detail,
                        parent, false);
                holder = new ViewHolder();
                holder.productShortName = (TextView) row.findViewById(R.id.prd_nameTv);
                holder.productCode = (TextView) row.findViewById(R.id.product_code);
                holder.productShortName.setMaxLines(businessModel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);
                holder.tvBatchNo = (TextView) row.findViewById(R.id.batch_no);
                holder.text_PcsQuantity = (TextView) row.findViewById(R.id.PRDPCSQTY);
                holder.text_caseQuantity = (TextView) row.findViewById(R.id.PRDQTY);
                holder.text_value = (TextView) row.findViewById(R.id.PRDVAL);
                holder.outerQty = (TextView) row
                        .findViewById(R.id.outerCaseQty);
                holder.tvWeight = (TextView) row.findViewById(R.id.prdweight);
                holder.productShortName.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        //productName.setText(((TextView) view).getText().toString());
                        return false;
                    }
                });

                // On/Off order case and pce
                if (!businessModel.configurationMasterHelper.SHOW_ORDER_CASE)
                    holder.text_caseQuantity.setVisibility(View.GONE);

                if (!businessModel.configurationMasterHelper.SHOW_ORDER_PCS)
                    holder.text_PcsQuantity.setVisibility(View.GONE);

                if (!businessModel.configurationMasterHelper.SHOW_OUTER_CASE)
                    holder.outerQty.setVisibility(View.GONE);

                if (!businessModel.configurationMasterHelper.SHOW_ORDER_WEIGHT)
                    holder.tvWeight.setVisibility(View.GONE);

                if (!businessModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION)
                    holder.tvBatchNo.setVisibility(View.GONE);

                if (!businessModel.configurationMasterHelper.IS_SHOW_SKU_CODE)
                    holder.productCode.setVisibility(View.GONE);

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.productBO = mProductsForAdapter.get(groupPosition);
            if (businessModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION) {
                if (holder.productBO.getBatchNo() != null && !holder.productBO.getBatchNo().equals("null")) {
                    String value = "" + holder.productBO.getBatchNo() + " , ";
                    holder.tvBatchNo.setText(value);
                }
            }
            holder.tvBatchNo.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.productShortName.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.productCode.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.productShortName.setText(holder.productBO.getProductShortName());

            if (businessModel.configurationMasterHelper.IS_SHOW_SKU_CODE) {
                String prodCode = getResources().getString(R.string.prod_code)
                        + ": " + holder.productBO.getProductCode() + " ";
                if (businessModel.labelsMasterHelper.applyLabels(holder.productCode.getTag()) != null)
                    prodCode = businessModel.labelsMasterHelper
                            .applyLabels(holder.productCode.getTag()) + ": " +
                            holder.productBO.getProductCode() + " ";
                holder.productCode.setText(prodCode);
            }

            holder.productName = holder.productBO.getProductName();
            holder.text_PcsQuantity.setText(String.valueOf(holder.productBO.getOrderedPcsQty()));
            holder.text_caseQuantity.setText(String.valueOf(holder.productBO.getOrderedCaseQty()));
            holder.outerQty.setText(String.valueOf(holder.productBO.getOrderedOuterQty()));
            int totalQty = holder.productBO.getOrderedPcsQty() + (holder.productBO.getOrderedCaseQty() * holder.productBO.getCaseSize()) + (holder.productBO.getOrderedOuterQty() * holder.productBO.getOutersize());
            String weightValue = " WGT : " + Utils.formatAsTwoDecimal((double) totalQty * holder.productBO.getWeight());
            holder.tvWeight.setText(weightValue);
            holder.tvWeight.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

            holder.text_value.setText(businessModel.formatValue(holder.productBO
                    .getTotalamount()));

            return row;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }

    }

    class ViewHolder {
        String productName;
        TextView productShortName, productCode;
        TextView tvBatchNo;
        TextView text_value, text_PcsQuantity, text_caseQuantity, outerQty;
        TextView tvWeight;
        private ProductMasterBO productBO;
    }

    private class Checkandprint extends AsyncTask<Integer, Integer, Boolean> {
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            builder = new AlertDialog.Builder(InvoiceReportDetail.this);

            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Boolean doInBackground(Integer... params) {


            Commons.print(TAG + ", Asynchronous STATE :" + mChatService.getState());
            if (mChatService.getState() != BtService.STATE_CONNECTED)

            {
                return false;
            } else {

                printData();
                return true;
            }


        }

        protected void onProgressUpdate(Integer... progress) {


        }


        @Override
        protected void onPostExecute(Boolean connect) {
            //	progressDialogue.dismiss();
            alertDialog.dismiss();
            if (!connect) {
                Toast.makeText(getApplicationContext(), "Not connected", Toast.LENGTH_SHORT).show();

            }
            businessModel.productHelper.clearOrderTableChecked();

            finish();


        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (msg.what == BtService.STATE_CONNECTED) {
                Toast.makeText(getApplicationContext(), "Connected",
                        Toast.LENGTH_SHORT).show();
                new Checkandprint().execute();

            }
            if (msg.what == BtService.STATE_CONNECTING) {
                Toast.makeText(getApplicationContext(), "Connecting",
                        Toast.LENGTH_SHORT).show();
            }

            if (msg.what == BtService.STATE_LISTEN)
                if (msg.what == BtService.STATE_NONE) {
                    Toast.makeText(getApplicationContext(), "None",
                            Toast.LENGTH_SHORT).show();

                }
            if (msg.what == MESSAGE_WRITE) {


            }
            if (msg.what == MESSAGE_READ) {


            }
            if (msg.what == MESSAGE_DEVICE_NAME) {
                String mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Device Name " + mConnectedDeviceName,
                        Toast.LENGTH_SHORT).show();

            }
            if (msg.what == MESSAGE_TOAST) {
                Toast.makeText(getApplicationContext(),
                        msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
                        .show();
                businessModel.productHelper.clearOrderTableChecked();

                finish();
            }
        }

    };


}
