package com.ivy.sd.print;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.ivy.cpg.view.reports.creditNoteReport.CreditNoteHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.CreditNoteListBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.DateTimeUtils;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;

import java.util.ArrayList;

/**
 * Created by rajkumar.s on 14-07-2016.
 */
public class CreditNotePrintPreviewScreen extends IvyBaseActivityNoActionBar {

    private BusinessModel bmodel;
    private Spinner printcount;
    private ArrayAdapter<CharSequence> spinadapter;

    private String count;
    private Double totalColl = 0.0;
    private EditText mMacAddressET;
    ArrayList<CreditNoteListBO> mDetails;
    private LinearLayout mDetailsContainerLL;
    TextView total;
    private ZebraPrinter printer;
    private Connection zebraPrinterConnection;
    private ImageView mStatusIV;
    private static final String TAG = "CreditNoteReportPrint";
    private TextView statusField;

    private static final String ZEBRA_2INCH = "2";
    private static final String ZEBRA_3INCH = "3";
    private static final String ZEBRA_4INCH = "4";
    private boolean IsOriginal, isPrintClicked;
    private Toolbar toolbar;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creditnote_preview_zebra);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

        try {
            bmodel = (BusinessModel) getApplicationContext();
            bmodel.setContext(this);

            toolbar = (Toolbar) findViewById(R.id.toolbar);
            mDetailsContainerLL = (LinearLayout) findViewById(R.id.details_container_ll);
            total = (TextView) findViewById(R.id.tot);
            mMacAddressET = (EditText) findViewById(R.id.et_mac);
            mStatusIV = (ImageView) findViewById(R.id.status_iv);
            statusField = (TextView) findViewById(R.id.status_bar);

            Bundle extras = getIntent().getExtras();

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
            printcount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

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
        doInitialize();
    }

    public String getMacAddressFieldText() {
        String macAddress = null;
        try {
            macAddress = mMacAddressET.getText().toString().trim();

            SharedPreferences pref = this.getSharedPreferences("PRINT",
                    MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("MAC", macAddress);
            editor.commit();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return macAddress;
    }

    private void doInitialize() {
        try {

            mDetails = CreditNoteHelper.getInstance().loadCreditNote(this);
            updateDetails();

            total.setText(bmodel.formatValue(totalColl));

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private void updateDetails() {

        try {

            TextView outletName, credit_amount, is_issued;

            mDetailsContainerLL.removeAllViews();
            LayoutInflater inflater = getLayoutInflater();
            for (CreditNoteListBO creditBO : mDetails) {

                View v = inflater.inflate(
                        R.layout.row_credit_note_print_preview, null);

                outletName = ((TextView) v.findViewById(R.id.outletname));
                credit_amount = ((TextView) v.findViewById(R.id.credit_amount));
                is_issued = ((TextView) v.findViewById(R.id.is_issued));


                outletName.setText(creditBO.getRetailerName());
                credit_amount.setText(bmodel.formatValue(creditBO.getAmount()));
                if (creditBO.isUsed())
                    is_issued.setText("Y");
                else
                    is_issued.setText("N");

                totalColl += creditBO.getAmount();

                mDetailsContainerLL.addView(v);

            }

        } catch (Exception e) {
            Commons.printException(e);
        }

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
            onBackButtonClick();
            /*Intent i = new Intent(this, ReportMenu.class);
            startActivity(i);*/

            return true;
        } else if (i == R.id.menu_print) {
            if (!isPrintClicked) {
                isPrintClicked = true;
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                new Thread(new Runnable() {
                    public void run() {
                        Looper.prepare();
                        doConnection(ZEBRA_3INCH);
                        Looper.loop();
                        Looper.myLooper().quit();
                    }
                }).start();
            }
        }
        return false;
    }

    private void onBackButtonClick() {
        finish();
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
    }

    private void doConnection(String printername) {
        try {
            printer = connect();
            if (printer != null) {
                // sendTestLabel();
                printInvoice(printername);
            } else {
                bmodel.showAlert(
                        getResources().getString(
                                R.string.error), 5004);
                disconnect();
            }
        } catch (Exception e) {
            bmodel.showAlert(
                    getResources().getString(
                            R.string.error), 5004);
            Commons.printException(e);
        }
    }

    // connectivity
    boolean isPrinterLanguageDetected = false;

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
            bmodel.showAlert(
                    getResources().getString(
                            R.string.error), 5004);
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
                bmodel.showAlert(
                        getResources().getString(
                                R.string.error), 5004);
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

			 *
			 * isPrinterLanguageDetected = false;
			 *
			 * // printer = null; // DemoSleeper.sleep(1000); // disconnect(); }
			 */
        }

        return printer;
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

    /**
     * printing invoice
     **/
    public void printInvoice(String printername) {

        try {
            if (printername.equals(ZEBRA_2INCH)) {
               /* for (int i = 0; i < SDUtil.convertToInt(count); i++)
                    zebraPrinterConnection.write(printDatafor2inchprinter());*/

            } else if (printername.equals(ZEBRA_3INCH)) {

                for (int i = 0; i < SDUtil.convertToInt(count); i++) {
                    if (i == 0)
                        IsOriginal = true;
                    else {
                        IsOriginal = false;
                    }
                    zebraPrinterConnection.write(printDatafor3inchprinter());
                }

                bmodel.showAlert(
                        getResources().getString(
                                R.string.printed_successfully), 5004);
            } else if (printername.equals(ZEBRA_4INCH)) {

                /*for (int i = 0; i < SDUtil.convertToInt(count); i++) {
                    if (i == 0)
                        IsOriginal = true;
                    else {
                        IsOriginal = false;
                    }
                    zebraPrinterConnection.write(printDatafor4inchprinter());
                }*/

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
            bmodel.showAlert(
                    getResources().getString(
                            R.string.error), 5004);
        } catch (Exception e) {
            Commons.printException(e);
            bmodel.showAlert(
                    getResources().getString(
                            R.string.error), 5004);
        } finally {
            disconnect();
        }
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

                int height = 0;
                int x = 280;
                height = x + mDetails.size() * 150 + 250;
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

                Printitem += "T 5 0 350 230 "
                        + getResources().getString(R.string.date)
                        + ":"
                        + DateTimeUtils.convertFromServerDateToRequestedFormat(
                        DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                        bmodel.configurationMasterHelper.outDateFormat)
                        + "\r\n";

                Printitem += "LEFT \r\n";
                Printitem += "T 5 0 10 230 "
                        + "Rep.Name:"
                        + ""
                        + bmodel.userMasterHelper.getUserMasterBO()
                        .getUserName() + "\r\n";

                Printitem += "\r\n";

                Printitem += "T 5 0 10 260 --------------------------------------------------\r\n";

                for (CreditNoteListBO creditNoteBO : mDetails) {

                    x += 10;
                    Printitem += "T 5 0 20 " + x + " " + "Credit Note" + "\r\n";
                    Printitem += "T 5 0 160 " + x + " " + ":" + "\r\n";
                    Printitem += "T 5 0 180 " + x + " "
                            + creditNoteBO.getId()
                            + "\r\n";
                    x += 30;

                    Printitem += "T 5 0 20 " + x + " " + "Outlet Name" + "\r\n";
                    Printitem += "T 5 0 160 " + x + " " + ":" + "\r\n";
                    Printitem += "T 5 0 180 " + x + " "
                            + creditNoteBO.getRetailerName() + "\r\n";
                    x += 30;

                    Printitem += "T 5 0 20 " + x + " " + "Crd Amount" + "\r\n";
                    Printitem += "T 5 0 160 " + x + " " + ":" + "\r\n";
                    Printitem += "T 5 0 180 " + x + " "
                            + bmodel.formatValue(creditNoteBO.getAmount())
                            + "\r\n";

                    x += 30;

                    String temp = "N";
                    if (creditNoteBO.isUsed())
                        temp = "Y";

                    Printitem += "T 5 0 20 " + x + " " + "Is Issued" + "\r\n";
                    Printitem += "T 5 0 160 " + x + " " + ":" + "\r\n";
                    Printitem += "T 5 0 180 " + x + " "
                            + temp + "\r\n";
                  /*  x += 30;

                    Printitem += "T 5 0 20 " + x + " " + "OS Amount" + "\r\n";
                    Printitem += "T 5 0 180 " + x + " "
                            + bmodel.formatValue(productBO.getBalance())
                            + "\r\n";

                    x += 40;
                    Printitem += "T 5 0 180 " + x + " " + "Cash" + "\r\n";
                    if (productBO.getCashMode().equals("CA")) {
                        Printitem += "T 5 0 400 " + x + " "
                                + bmodel.formatValue(productBO.getPaidAmount())
                                + "\r\n";
                    } else {
                        Printitem += "T 5 0 400 " + x + " " + "0" + "\r\n";
                    }

                    x += 30;
                    Printitem += "T 5 0 180 " + x + " " + "Cheque" + "\r\n";
                    if (productBO.getCashMode().equals("CQ")) {
                        Printitem += "T 5 0 400 " + x + " "
                                + bmodel.formatValue(productBO.getPaidAmount())
                                + "\r\n";
                    } else {
                        Printitem += "T 5 0 400 " + x + " " + "0" + "\r\n";
                    }

                    x += 30;
                    Printitem += "T 5 0 180 " + x + " " + "Total" + "\r\n";

                    Printitem += "T 5 0 400 " + x + " "
                            + bmodel.formatValue(productBO.getPaidAmount())
                            + "\r\n";*/
                    x += 20;

                    Printitem += "T 5 0 10 "
                            + x
                            + " --------------------------------------------------\r\n";
                    x += 20;

                }

                x += 30;
                Printitem += "T 5 0 180 " + x + "Total Amount :" + "\r\n";

                Printitem += "RIGHT \r\n";
                Printitem += "T 5 0 350 " + x + " "
                        + bmodel.formatValue(totalColl) + "\r\n";
              /*  x += 30;
                Printitem += "T 5 0 180 " + x + "Total Cheque" + "\r\n";

                Printitem += "RIGHT \r\n";
                Printitem += "T 5 0 350 " + x + " "
                        + bmodel.formatValue(totalCheque) + "\r\n";

                x += 30;
                Printitem += "T 5 0 180 " + x + "Total Collected" + "\r\n";

                Printitem += "RIGHT \r\n";
                Printitem += "T 5 0 350 " + x + " "
                        + bmodel.formatValue(totalColl) + "\r\n";*/

                x += 30;
                Printitem += "T 5 0 10 "
                        + x
                        + " --------------------------------------------------\r\n";

                x += 50;

                Printitem += "\r\n";
                Printitem += "\r\n";
                Printitem += "\r\n";

                Printitem += "T 5 0 10 " + x + "Cusotmer Sign" + "\r\n";
                Printitem += "T 5 0 180 " + x + " --------\r\n";

                Printitem += "T 5 0 330 " + x + "Rep. Sign" + "\r\n";
                Printitem += "T 5 0 440 " + x + "--------\r\n";

                Printitem += "PRINT\r\n";

                PrintDataBytes = Printitem.getBytes();
            }
        } catch (Exception e) {
        }
        return PrintDataBytes;
    }

}
