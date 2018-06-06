package com.ivy.sd.png.view.reports.refactor;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.print.DemoSleeper;
import com.ivy.sd.print.SettingsHelper;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;


public class DayReportPrintHelper {
    private BusinessModel bModel;
    private Context mContext;
    private IDayReportModelPresenter iDayReportModelPresenter;

    private Connection zebraPrinterConnection;


//    private static final String ZEBRA_3INCH = "3";
    private static final String TAG = "DailyReportFragmentNew";

    private AlertDialog alertDialog;


    public DayReportPrintHelper(BusinessModel businessModel, Context context, AlertDialog alertDialog, IDayReportModelPresenter dayReportModelPresenter) {
        this.bModel = businessModel;
        this.mContext = context;
        this.alertDialog = alertDialog;
        this.iDayReportModelPresenter = dayReportModelPresenter;
    }

    public void doConnection(String printerName) {
        try {
            bModel.vanmodulehelper.downloadSubDepots();
            ZebraPrinter printer = connect();
            if (printer != null) {
                printInvoice(printerName);
            } else {
                disconnect();
                alertDialog.dismiss();
                Toast.makeText(mContext, "Printer not connected ..", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public String getMacAddressFieldText() {
        String macAddress = null;
        try {
            SharedPreferences pref = mContext.getSharedPreferences("PRINT",
                    Context.MODE_PRIVATE);
            macAddress = pref.getString("MAC", "");

        } catch (Exception e) {
            Commons.printException(e);
        }
        return macAddress;
    }

    private void disconnect() {
        try {
            if (zebraPrinterConnection != null) {
                zebraPrinterConnection.close();
                zebraPrinterConnection = null;
            }
        } catch (ConnectionException e) {
            Commons.printException(e);
        }
    }

    public void printInvoice(String printerName) {
        try {
            if (printerName.equals(DayReportFragment.ZEBRA_3INCH)) {
                zebraPrinterConnection.write(iDayReportModelPresenter.printDataFor3InchPrinter());
                alertDialog.dismiss();
                bModel.showAlert(
                        mContext.getResources().getString(
                                R.string.printed_successfully), 0);
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
            disconnect();
        }
    }


    public ZebraPrinter connect() {

        zebraPrinterConnection = new BluetoothConnection(getMacAddressFieldText());
        SettingsHelper.saveBluetoothAddress(mContext, getMacAddressFieldText());

        Commons.print(TAG + "PRINT MAC : " + getMacAddressFieldText());

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

        //  isPrinterLanguageDetected = false;

        if (zebraPrinterConnection.isConnected()) {
            try {
                printer = ZebraPrinterFactory.getInstance(PrinterLanguage.CPCL,
                        zebraPrinterConnection);

                PrinterLanguage pl = printer.getPrinterControlLanguage();

                Commons.print(TAG + "PRINT LANGUAGE : " + pl);
                // isPrinterLanguageDetected = true;
            } catch (ConnectionException e) {

                Commons.print(TAG + "PRINT LANGUAGE : UNKNOWN : PrinterConnectionException");
                Commons.printException(e);
                // isPrinterLanguageDetected = false;
            }
        }

        return printer;
    }
}
