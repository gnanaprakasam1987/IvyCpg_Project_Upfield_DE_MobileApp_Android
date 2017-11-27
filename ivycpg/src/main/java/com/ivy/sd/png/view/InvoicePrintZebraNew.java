package com.ivy.sd.png.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.OrderHeader;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.MyThread;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.print.DemoSleeper;
import com.ivy.sd.print.SettingsHelper;
import com.ivy.sd.print.Zebra;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class InvoicePrintZebraNew extends Zebra {
    private AlertDialog.Builder builder;
    private AlertDialog alertDialog;
    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;

    boolean IsFromOrder, IsFromReport;
    //private ProgressDialog pd;
    private Connection zebraPrinterConnection;
    TextView printtitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Bundle extras = getIntent().getExtras();

            if (extras != null) {
                if (extras.containsKey("IsFromOrder")) {
                    IsFromOrder = extras.getBoolean("IsFromOrder");
                }
                if (extras.containsKey("IsFromReport"))
                    IsFromReport = extras.containsKey("IsFromReport");
            }
            printtitle = (TextView) findViewById(R.id.printtitle);
            if (IsFromOrder)
                printtitle.setText(getResources().getString(
                        R.string.order_print_preview));
            else
                printtitle.setText(getResources().getString(
                        R.string.invoice_print_preview));
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {

            mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = mPowerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK,
                    "My Tag");
            mWakeLock.acquire();
            if (!IsFromOrder) {
                findViewById(R.id.invoice_number_tv_lable).setVisibility(
                        View.VISIBLE);
                mInvoiceNumberTV.setText(mInvoiceNumber);
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public Handler getHandler() {
        return mHandler;
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindDrawables(findViewById(R.id.root));
        // force the garbage collector to run
        System.gc();
    }

    /**
     * this would clear all the resources used of the layout.
     *
     * @param view
     */
    private void unbindDrawables(View view) {
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            try {
                if (!(view instanceof AdapterView<?>))
                    ((ViewGroup) view).removeAllViews();
            } catch (Exception e) {
                Commons.printException(e);
            }
        }
    }

    boolean isPrinterLanguageDetected = false;

    public void printInvoice(String printername) {

        // byte[] formatedInvoice = null;
        // if (isPrinterLanguageDetected) {
        // PrinterLanguage printerLanguage = printer
        // .getPrinterControlLanguage();
        // if (printerLanguage == PrinterLanguage.ZPL) {
        // formatedInvoice = getZPLFormatedInvoice();
        //
        // // configLabel =
        // //
        // "^XA^FO17,16^GB379,371,8^FS^FT65,255^A0N,135,134^FDTEST^FS^XZ".getBytes();
        // } else if (printerLanguage == PrinterLanguage.CPCL) {
        // formatedInvoice = getCPCLFormatedInvoice();
        //
        // // String cpclConfigLabel = "! 0 200 200 406 1\r\n" +
        // // "ON-FEED IGNORE\r\n" + "BOX 20 20 380 380 8\r\n" +
        // // "T 0 6 137 177 TEST\r\n" + "PRINT\r\n";
        // // configLabel = cpclConfigLabel.getBytes();
        // }
        // } else {
        // formatedInvoice = getCPCLFormatedInvoice();
        // }

        // Logs.exception(TAG, "INVOICE : " + String.valueOf(getTestPrint()));

        // Logs.exception(TAG, "INVOICE : " + String.valueOf(formatedInvoice));

        try {
            // byte[] configLabel = getConfigLabel();
            // zebraPrinterConnection.write(getTestPrint());
            // zebraPrinterConnection.write(formatedInvoice);
            if (printername.equals(ZEBRA_2INCH)) {
                for (int i = 0; i < SDUtil.convertToInt(count); i++)
                    zebraPrinterConnection.write(printDatafor2inchprinter());
                if (!IsFromReport) {
                    bmodel.showAlert(
                            getResources().getString(R.string.printed_successfully),
                            1234);
                } else {
                    bmodel.showAlert(
                            getResources().getString(R.string.printed_successfully),
                            121);
                }

            } else if (printername.equals(ZEBRA_4INCH)) {
                for (int i = 0; i < SDUtil.convertToInt(count); i++)
                    zebraPrinterConnection.write(printDatafor4inchprinter());
                if (!IsFromReport) {
                    bmodel.showAlert(
                            getResources().getString(R.string.printed_successfully),
                            1234);
                } else {
                    bmodel.showAlert(
                            getResources().getString(R.string.printed_successfully),
                            121);
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
            setStatus(getResources().getString(
                    R.string.printer_not_connected), Color.RED);
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

        // } else {
        // try {
        // int port = Integer.parseInt(getTcpPortNumber());
        // zebraPrinterConnection = new TcpPrinterConnection(getTcpAddress(),
        // port);
        // SettingsHelper.saveIp(this, getTcpAddress());
        // SettingsHelper.savePort(this, getTcpPortNumber());
        // } catch (NumberFormatException e) {
        // setStatus("Port Number Is Invalid", Color.RED);
        // return null;
        // }
        // }

        try {
            zebraPrinterConnection.open();
            mStatusIV.setImageResource(R.drawable.greenball);
            setStatus("Connected", Color.GREEN);
        } catch (ConnectionException e) {
            setStatus(getResources().getString(
                    R.string.printer_not_connected), Color.RED);
            Commons.printException(e);

            DemoSleeper.sleep(1000);
            disconnect();
        } catch (Exception e) {

            setStatus(getResources().getString(
                    R.string.printer_not_connected), Color.RED);
            Commons.printException(e);
        }

        ZebraPrinter printer = null;

        isPrinterLanguageDetected = false;

        if (zebraPrinterConnection.isConnected()) {
            try {
                printer = ZebraPrinterFactory
                        .getInstance(zebraPrinterConnection);
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
            } catch (ZebraPrinterLanguageUnknownException e) {
                setStatus("Unknown Printer Language", Color.RED);
                Commons.printException(e);

                isPrinterLanguageDetected = false;

                // printer = null;
                // DemoSleeper.sleep(1000);
                // disconnect();
            }
        }

        return printer;
    }

	/*
     * public ZebraPrinter connect() { Logs.exception(TAG,
	 * "PRINT : CONNECTING..."); setStatus("Connecting...", Color.YELLOW);
	 * zebraPrinterConnection = null; // if (isBluetoothSelected()) {
	 * zebraPrinterConnection = new BluetoothPrinterConnection(
	 * getMacAddressFieldText()); // SettingsHelper.saveBluetoothAddress(this,
	 * getMacAddressFieldText());
	 * 
	 * Logs.exception(TAG, "PRINT MAC : " + getMacAddressFieldText());
	 * 
	 * // } else { // try { // int port = Integer.parseInt(getTcpPortNumber());
	 * // zebraPrinterConnection = new TcpPrinterConnection(getTcpAddress(), //
	 * port); // SettingsHelper.saveIp(this, getTcpAddress()); //
	 * SettingsHelper.savePort(this, getTcpPortNumber()); // } catch
	 * (NumberFormatException e) { // setStatus("Port Number Is Invalid",
	 * Color.RED); // return null; // } // }
	 * 
	 * try { zebraPrinterConnection.open(); Logs.exception(TAG,
	 * "PRINT CONNECTED"); setStatus("Connected", Color.GREEN); } catch
	 * (ZebraPrinterConnectionException e) { Logs.exception(TAG,
	 * "PRINT CANT CONNECT"); Commons.printException(e);
	 * setStatus("Comm Error! Disconnecting", Color.RED);
	 * DemoSleeper.sleep(1000); disconnect(); } catch (Exception e) {
	 * Logs.exception(TAG, "PRINT CANT CONNECT : GENERAL Exception");
	 * Commons.printException(e); }
	 * 
	 * ZebraPrinter printer = null;
	 * 
	 * isPrinterLanguageDetected = false;
	 * 
	 * if (zebraPrinterConnection.isConnected()) { try { printer =
	 * ZebraPrinterFactory .getInstance(zebraPrinterConnection);
	 * setStatus("Determining Printer Language", Color.YELLOW);
	 * Logs.exception(TAG, "PRINT DETEMINING LANUAGE"); PrinterLanguage pl =
	 * printer.getPrinterControlLanguage(); setStatus("Printer Language " + pl,
	 * Color.BLUE); Logs.exception(TAG, "PRINT LANGUAGE : " + pl);
	 * isPrinterLanguageDetected = true; } catch
	 * (ZebraPrinterConnectionException e) {
	 * setStatus("PrinterConnectionException", Color.RED); Logs.exception(TAG,
	 * "PRINT LANGUAGE : UNKNOWN : PrinterConnectionException");
	 * Commons.printException(e); // printer = null; // DemoSleeper.sleep(1000); //
	 * disconnect(); isPrinterLanguageDetected = false; } catch
	 * (ZebraPrinterLanguageUnknownException e) {
	 * setStatus("Unknown Printer Language", Color.RED); Logs.exception(TAG,
	 * "PRINT LANGUAGE : UNKNOWN"); Commons.printException(e);
	 * 
	 * isPrinterLanguageDetected = false;
	 * 
	 * // printer = null; // DemoSleeper.sleep(1000); // disconnect(); } }
	 * 
	 * return printer; }
	 */

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
                  /*  if (pd != null)
                        pd.dismiss();*/
                    if (alertDialog != null)
                        alertDialog.dismiss();
                    return true;
                case DataMembers.NOTIFY_ORDER_DELETED:
                    try {
                       /* if (pd != null)
                            pd.dismiss();*/
                        if (alertDialog != null)
                            alertDialog.dismiss();
                        bmodel = (BusinessModel) getApplicationContext();
                        bmodel.showAlert(
                                getResources().getString(
                                        R.string.order_deleted_sucessfully)
                                        + bmodel.getOrderid(),
                                DataMembers.NOTIFY_ORDER_SAVED);
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                    return true;

                case DataMembers.NOTIFY_INVOICE_SAVED:
                    try {
                       /* if (pd != null)
                            pd.dismiss();*/
                        if (alertDialog != null)
                            alertDialog.dismiss();
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
                    new Thread(new Runnable() {
                        public void run() {
                            Looper.prepare();
                            doConnection(bmodel.configurationMasterHelper.PRINTER_SIZE
                                    + "");
                            Looper.loop();
                            Looper.myLooper().quit();
                        }
                    }).start();
                    return true;
            }
            return false;
        }
    });

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_invoice_print, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i1 = item.getItemId();
        if (i1 == android.R.id.home) {
            bmodel.productHelper.clearOrderTable();
            if (IsFromReport) {
                Intent returnIntent = new Intent();
                setResult(RESULT_CANCELED, returnIntent);
                finish();
            } else {
                bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil.now(SDUtil.TIME));
                finish();
                BusinessModel.loadActivity(this, DataMembers.actHomeScreenTwo);
            }
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        } else if (i1 == R.id.menu_print) {// showDialog(SELECTED_PRINTER_DIALOG);

            new Thread(new Runnable() {
                public void run() {
                    Looper.prepare();
                    doConnection(bmodel.configurationMasterHelper.PRINTER_SIZE
                            + "");
                    Looper.loop();
                    Looper.myLooper().quit();
                }
            }).start();

        } else if (i1 == R.id.menu_calculator) {
            try {
                ArrayList<HashMap<String, Object>> items = new ArrayList<HashMap<String, Object>>();
                final PackageManager pm = getPackageManager();
                List<PackageInfo> packs = pm.getInstalledPackages(0);
                for (PackageInfo pi : packs) {
                    if (pi.packageName.toString().toLowerCase()
                            .contains("calcul")) {
                        HashMap<String, Object> map = new HashMap<String, Object>();
                        map.put("appName", pi.applicationInfo.loadLabel(pm));
                        map.put("packageName", pi.packageName);
                        items.add(map);
                    }
                }
                if (items.size() >= 1) {
                    String packageName = (String) items.get(0).get(
                            "packageName");
                    Intent i = pm.getLaunchIntentForPackage(packageName);
                    if (i != null)
                        startActivity(i);
                } else {
                    Toast.makeText(this, "Calculator application not found.",
                            Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Commons.printException("", e);
                // TODO: handle exception
            }
            return true;
        } else if (i1 == R.id.menu_preview_save) {
            try {

                OrderHeader ord = new OrderHeader();
                ord.setOrderValue(mTotalValue);
                ord.setLinesPerCall(lines);
                ord.setDiscount(0);
                ord.setDeliveryDate(SDUtil.now(SDUtil.DATE_GLOBAL));
                bmodel.setOrderHeaderBO(ord);

                if (bmodel.hasOrder()) {
                    bmodel.invoiceDisount = "0";
                    if (bmodel.configurationMasterHelper.IS_INVOICE) {
                        /*pd = ProgressDialog.show(
                                InvoicePrintZebraNew.this,
								DataMembers.SD,
								getResources().getString(
										R.string.saving_invoice), true, false);*/
                        builder = new AlertDialog.Builder(InvoicePrintZebraNew.this);

                        customProgressDialog(builder, getResources().getString(R.string.saving_invoice));
                        alertDialog = builder.create();
                        alertDialog.show();
                    } else {
                        /*pd = ProgressDialog
                                .show(InvoicePrintZebraNew.this,
										DataMembers.SD,
										getResources().getString(
												R.string.saving_new_order),
										true, false);*/
                        builder = new AlertDialog.Builder(InvoicePrintZebraNew.this);

                        customProgressDialog(builder, getResources().getString(R.string.saving_new_order));
                        alertDialog = builder.create();
                        alertDialog.show();
                    }
                    new MyThread(this, DataMembers.SAVEINVOICE, getHandler())
                            .start();

                } else {
                    Toast.makeText(
                            this,
                            getResources().getString(
                                    R.string.no_products_exists),
                            Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                Commons.printException(e);
            }

        }
        return false;
    }

    @Override
    public Dialog onCreateDialog(int id) {
        switch (id) {

            case SELECTED_PRINTER_DIALOG:

                AlertDialog.Builder builder2 = new AlertDialog.Builder(
                        InvoicePrintZebraNew.this).setTitle("Choose Printer")
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
        }
        return null;

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (IsFromReport)
            bmodel.configurationMasterHelper.IS_INVOICE_AS_MOD = false;
        if (IsFromOrder)
            bmodel.configurationMasterHelper.IS_INVOICE_AS_MOD = false;

        if (!bmodel.configurationMasterHelper.SHOW_CALC)
            menu.findItem(R.id.menu_calculator).setVisible(false);
        if (bmodel.configurationMasterHelper.IS_INVOICE_AS_MOD) {
            menu.findItem(R.id.menu_print).setVisible(false);
            menu.findItem(R.id.menu_preview_save).setVisible(true);
        } else {
            menu.findItem(R.id.menu_print).setVisible(true);
            menu.findItem(R.id.menu_preview_save).setVisible(false);
        }
        return true;
    }
}
