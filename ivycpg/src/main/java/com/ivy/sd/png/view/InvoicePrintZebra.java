package com.ivy.sd.png.view;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bixolon.android.library.BxlService;
import com.ivy.lib.Logs;
import com.ivy.lib.Utils;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.print.DemoSleeper;
import com.ivy.sd.print.SettingsHelper;
import com.zebra.android.comm.BluetoothPrinterConnection;
import com.zebra.android.comm.ZebraPrinterConnection;
import com.zebra.android.comm.ZebraPrinterConnectionException;
import com.zebra.android.printer.PrinterLanguage;
import com.zebra.android.printer.ZebraPrinter;
import com.zebra.android.printer.ZebraPrinterFactory;
import com.zebra.android.printer.ZebraPrinterLanguageUnknownException;

import java.util.Formatter;
import java.util.Vector;

public class InvoicePrintZebra extends IvyBaseActivityNoActionBar implements OnClickListener {
    private static final String TAG = "InvoicePrint";
    private final String MSG_BLUETOOTH_NOT_ENABLED = "Bluetooth not enabled.";
    private final String MSG_PRINTER_NOT_CONNECTED = "Printer not connected";
    private final String MSG_PRINTER_CONNECTED = "Printer connected";
    private final String MSG_PRINTED_SUCCESSFULLY = "Printer successfully";
    private TextView mStatusTV, mDateTimeTV, mDistributorNameTV, mOutletNameTV,
            mTINNumberTV, mInvoiceNumberTV, mTotalQuantityTV, mTotalValueTV,
            mDiscoutnValueTV, mBillValueTV, mVATValueTV, mDistContactTV,
            mSellerName;
    private Button mReconnectBTN, mSkipBTN, mPrintBTN;
    private ListView mProductList;
    private LinearLayout mProductContainerLL;
    private ImageView mStatusIV;
    private ProgressBar mProgressBar;
    private BluetoothAdapter mBluetoothAdapter;
    private BxlService mBxlService = null;
    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;
    private boolean isBluetoothEnabled = false;
    private boolean isConnected = false;
    private Vector<ProductMasterBO> mProducts = new Vector<ProductMasterBO>();
    private Vector<ProductMasterBO> mProductsForAdapter = new Vector<ProductMasterBO>();
    private int mTotalQuantity;
    private double mTotalValue, mVATValue, mDiscountValue, mBillValue;
    private String mDate, mTINNumber, mInvoiceNumber, mDistributorName,
            mDistributorContact, mOutletName;
    private String mPrintProducts, mPrintTotal, mPrintVat, mPrintDiscount,
            mPrintBillValue;
    private BusinessModel bmodel;
    private ProgressDialog pd;
    boolean isDayClosed = true;
    boolean isClick, isFromReport, isFromOpenMarket;

    // Zebra Codes
    private StringBuilder sb = new StringBuilder();
    private Formatter f = new Formatter(sb);
    private ZebraPrinterConnection zebraPrinterConnection;
    private TextView statusField;
    private ZebraPrinter printer;

    // philippines

    private Button Calculator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.invoice_print_preview);
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        mStatusTV = (TextView) findViewById(R.id.status_tv);
        statusField = (TextView) findViewById(R.id.status_bar);
        mDateTimeTV = (TextView) findViewById(R.id.date_time_tv);
        mDistContactTV = (TextView) findViewById(R.id.dist_contact_tv);
        mDistributorNameTV = (TextView) findViewById(R.id.distributor_name_tv);
        mOutletNameTV = (TextView) findViewById(R.id.outlet_name_tv);
        mTINNumberTV = (TextView) findViewById(R.id.tin_number_tv);
        mInvoiceNumberTV = (TextView) findViewById(R.id.invoice_number_tv);
        mTotalQuantityTV = (TextView) findViewById(R.id.total_qty_tv);
        mTotalValueTV = (TextView) findViewById(R.id.total_price_tv);
        mVATValueTV = (TextView) findViewById(R.id.vat_value_tv);
        mDiscoutnValueTV = (TextView) findViewById(R.id.discount_value_tv);
        mBillValueTV = (TextView) findViewById(R.id.bill_value_tv);
        mSellerName = (TextView) findViewById(R.id.userNameTv);
        mReconnectBTN = (Button) findViewById(R.id.reconnect_btn);
        mSkipBTN = (Button) findViewById(R.id.skip_btn);
        mPrintBTN = (Button) findViewById(R.id.print_btn);
        Calculator = (Button) findViewById(R.id.calc_btn);

        mReconnectBTN.setOnClickListener(this);
        mSkipBTN.setOnClickListener(this);
        mPrintBTN.setOnClickListener(this);
        Calculator.setOnClickListener(this);
        Calculator.setVisibility(View.GONE);

        mStatusIV = (ImageView) findViewById(R.id.status_iv);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        mProductList = (ListView) findViewById(R.id.product_list_lv);
        mProductContainerLL = (LinearLayout) findViewById(R.id.product_container_ll);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            if (extras.containsKey("IsFromReport")) {
                isFromReport = extras.getBoolean("IsFromReport");
            }
            if (extras.containsKey("IsFromOpenMarket")) {
                isFromOpenMarket = extras.getBoolean("IsFromOpenMarket");
            }
        }

        if (isFromOpenMarket) {
            TextView title = (TextView) findViewById(R.id.titlebar);
            title.setText("Delivery Receipt");
        }

        if (isFromOpenMarket) {
            TextView tv = (TextView) findViewById(R.id.invoice_number_tv_lable);
            tv.setVisibility(View.GONE);
            mInvoiceNumberTV.setVisibility(View.GONE);
            RelativeLayout r = (RelativeLayout) findViewById(R.id.discountLayout);
            r.setVisibility(View.GONE);
            RelativeLayout r1 = (RelativeLayout) findViewById(R.id.billLayout);
            r1.setVisibility(View.GONE);
        }

        if (!bmodel.configurationMasterHelper.IS_DELIVERY_REPORT) {
            RelativeLayout r = (RelativeLayout) findViewById(R.id.discountLayout);
            r.setVisibility(View.GONE);
            RelativeLayout r1 = (RelativeLayout) findViewById(R.id.billLayout);
            r1.setVisibility(View.GONE);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        Logs.debug(TAG, "onStart");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK,
                "My Tag");
        mWakeLock.acquire();
        // mBxlService = new BxlService();

        // updateConnectionState();

        mDiscoutnValueTV.setText(bmodel.invoiceDisount + "%");
        mProducts = bmodel.productHelper.getProductMaster();

        if (null == mProducts) {
            bmodel.showAlert("No Products exists", 0);
            return;
        }

        mDate = SDUtil.now(SDUtil.DATE_TIME);
        mDistributorName = bmodel.userMasterHelper.getUserMasterBO()
                .getDistributorName();
        mDistributorContact = bmodel.userMasterHelper.getUserMasterBO()
                .getDistributorContactNumber();
        mInvoiceNumber = bmodel.getInvoiceNumber();

        if (isFromReport) {
            String str[] = bmodel.getRetailerNameAndTin(mInvoiceNumber + "");
            mOutletName = str[0];
            mTINNumber = str[1];

        } else {
            mTINNumber = bmodel.getRetailerMasterBO().getTinnumber();
            mOutletName = bmodel.getRetailerMasterBO().getRetailerName();
        }

        // if(isFromOpenMarket){
        // mInvoiceNumber=bmodel.getOrderIDFormInvoice();
        // }

        if (mDistributorName.length() > 10) {
            mDistributorName = mDistributorName.subSequence(0, 10) + "..";
        }

        mSellerName.setText(bmodel.userMasterHelper.getUserMasterBO()
                .getUserName());
        mDateTimeTV.setText(mDate);
        mDistributorNameTV.setText(mDistributorName);
        mDistContactTV.setText("Ph No: " + mDistributorContact);
        mOutletNameTV.setText(mOutletName);
        mTINNumberTV.setText(mTINNumber);
        mInvoiceNumberTV.setText(mInvoiceNumber);

        updatePreview();
    }

    private String updateConnectionState() {

        Commons.print(TAG+ ",update connection state called");
        String state = "FALSE";
        if (mBluetoothAdapter != null) {
            Commons.print(TAG+ ",Bluetooth adapter not null");
            if (mBluetoothAdapter.isEnabled()) {
                isBluetoothEnabled = true;
            }
        }

        if (isBluetoothEnabled) {
            Commons.print(TAG+ ",bluetooth enabled");

            // if (mBxlService.Connect() == 0) {
            if (isConnected()) {
                Logs.debug(TAG, "Printer Connected");
                updateStatus(MSG_PRINTER_CONNECTED, true);
                isConnected = true;
                mStatusIV.setImageResource(R.drawable.greenball);
                // mReconnectBTN.setVisibility(View.GONE);
                // mPrintBTN.setVisibility(View.VISIBLE);
                state = "TRUE";
            } else {
                Logs.debug(TAG, "Printer Connection Failed");
                updateStatus(MSG_PRINTER_NOT_CONNECTED, false);
                isConnected = false;
                mStatusIV.setImageResource(R.drawable.redball);
                // mReconnectBTN.setVisibility(View.VISIBLE);
                // mPrintBTN.setVisibility(View.GONE);
                state = "NO_PRINTER";
            }
        } else {
            Logs.debug(TAG, MSG_BLUETOOTH_NOT_ENABLED);
            updateStatus(MSG_BLUETOOTH_NOT_ENABLED, false);
            // mReconnectBTN.setVisibility(View.VISIBLE);
            // mPrintBTN.setVisibility(View.GONE);
            state = "NO_BLUETOOTH";
        }

        Commons.print(TAG+ ",state" + state);

        return state;
    }

    private void updateStatus(String statusMessage, boolean isPositiveStatus) {
        mStatusTV.setText(statusMessage + "");
        if (isPositiveStatus) {
            mStatusTV.setTextColor(Color.GREEN);
        } else {
            mStatusTV.setTextColor(Color.RED);
        }
    }

    private boolean updatePreview() {
        // clear previous data

        mProductContainerLL.removeAllViews();
        sb.delete(0, sb.length());

        f.format("%-30s\n", "--------------------------------");
        f.format("%-19s %4s %7s\n", "Item", "Qty", "Price");
        f.format("%-30s\n", "--------------------------------");

        mTotalQuantity = 0;
        mTotalValue = 0;
        mVATValue = 0;
        mBillValue = 0;
        int quantity = 0;
        double price = 0.0;
        double vat = 0;
        String productName = "";
        LayoutInflater inflater = getLayoutInflater();

        for (ProductMasterBO productBO : mProducts) {
            // if (productBO.isCheked()
            // && (productBO.getCaseQty() > 0 || productBO.getPieceQty() > 0)) {
            if ((productBO.getOrderedPcsQty() > 0 || productBO
                    .getOrderedCaseQty() > 0)) {
                mProductsForAdapter.add(productBO);

                // quantity = productBO.getPieceQty()
                // + (productBO.getCaseQty() * productBO.getUomqty());
                // price = Math.round(quantity * productBO.getTrmrp());
                // vat = price * productBO.getVat() / 100;

                quantity = (productBO.getOrderedPcsQty())
                        + (productBO.getOrderedCaseQty() * productBO
                        .getCaseSize());

                price = Math.round(quantity * productBO.getSrp());

                mTotalQuantity = mTotalQuantity + quantity;
                mTotalValue = mTotalValue + (price);
                mVATValue = mVATValue + vat;

                productName = productBO.getProductName();
                if (productName.length() > 19) {
                    // productName = productName.substring(0, end)

                    productName = productName.subSequence(0, 17) + "..";

                    f.format("%-19s %4s %7d\n", productName, quantity,
                            (int) price);
                    // f.format("%-15s \n", productName.subSequence(15,
                    // productName.length()));
                } else {
                    f.format("%-19s %4s %7d\n", productName, quantity,
                            (int) price);
                }

                View v = inflater.inflate(R.layout.row_invoice_print_preview,
                        null);

                ((TextView) v.findViewById(R.id.product_name_tv))
                        .setText(productName);
                ((TextView) v.findViewById(R.id.ou_qty)).setText(productBO
                        .getOrderedCaseQty() + "");
                ((TextView) v.findViewById(R.id.msq_qty)).setText(productBO
                        .getOrderedPcsQty() + "");
                // ((TextView) v.findViewById(R.id.price_tv))
                // .setText(Utils.trimRight(
                // String.valueOf(Utils.formatAsTwoDecimal(price)),
                // ".00"));
                ((TextView) v.findViewById(R.id.price_tv)).setText(productBO
                        .getSrp() + "");

                float tot = Math.round((productBO.getOrderedCaseQty()
                        * productBO.getCaseSize() * productBO.getSrp())
                        + (productBO.getOrderedPcsQty() * productBO.getSrp()));

                ((TextView) v.findViewById(R.id.total)).setText(tot + "");
                ((TextView) v.findViewById(R.id.vat_tv)).setText(Utils
                        .formatAsTwoDecimal((double) productBO.getVat()));

                mProductContainerLL.addView(v);
            }
        }

        double discount = (mTotalValue / 100)
                * SDUtil.convertToFloat(bmodel.invoiceDisount);

        mTotalValue = Math.round(mTotalValue);

        mBillValue = Math.round(mTotalValue - discount);

        mVATValue = Math.round(mVATValue);

        if (null == mProductsForAdapter) {
            bmodel.showAlert("No Products exists", 0);
            return false;
        }

        f.format("%-30s\n", "--------------------------------");
        mPrintProducts = sb.toString();
        sb.delete(0, sb.length());

        f.format("%-17s %6s %7d\n", "Total", mTotalQuantity, (int) mTotalValue);
        mPrintTotal = sb.toString();

        sb.delete(0, sb.length());
        f.format("%-15s %15d\n", "(+) VAT", (int) mVATValue);
        mPrintVat = sb.toString();

        sb.delete(0, sb.length());
        f.format("%-15s %15d%%\n", "Discount",
                (int) SDUtil.convertToFloat(bmodel.invoiceDisount));
        mPrintDiscount = sb.toString();

        sb.delete(0, sb.length());
        f.format("%-15s %16d\n", "Bill Value", (int) mBillValue);
        mPrintBillValue = sb.toString();

        mTotalQuantityTV.setText(Utils.trimRight(
                String.valueOf(mTotalQuantity), ".00"));
        mTotalValueTV.setText(Utils.trimRight(
                Utils.formatAsTwoDecimal(mTotalValue), ".00"));
        mVATValueTV.setText(Utils.trimRight(
                Utils.formatAsTwoDecimal(mVATValue), ".00"));
        mBillValueTV.setText(Utils.trimRight(
                Utils.formatAsTwoDecimal(mBillValue), ".00"));

        return true;
    }

    public Handler getHandler() {
        return handler;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (msg.what == 9) {
                pd.dismiss();
                bmodel = (BusinessModel) getApplicationContext();
                bmodel.showAlert(
                        "Order Saved Locally. Order ID is "
                                + bmodel.getOrderid(),
                        DataMembers.NOTIFY_UPLOAD_ERROR);
            }
            if (msg.what == 10) {
                pd.dismiss();
                if (isDayClosed) {
                    bmodel = (BusinessModel) getApplicationContext();
                    bmodel.showAlert("Order Saved Locally. Order ID is "
                                    + bmodel.getOrderid(),
                            DataMembers.NOTIFY_UPLOAD_ERROR);
                } else {
                    bmodel = (BusinessModel) getApplicationContext();
                    bmodel.showAlert(
                            "You are not Closed the Previous day.Order Saved Locally. Order ID is "
                                    + bmodel.getOrderid(), 98);
                }

            }
            if (msg.what == 6) {
                pd.dismiss();
                bmodel = (BusinessModel) getApplicationContext();
                bmodel.showAlert(
                        "Order Saved Locally. Order ID is "
                                + bmodel.getOrderid(),
                        DataMembers.NOTIFY_UPLOAD_ERROR);
            }
            if (msg.what == 5) {
                pd.dismiss();
                bmodel = (BusinessModel) getApplicationContext();
                bmodel.showAlert(
                        "Order Submitted. Order ID is " + bmodel.getOrderid(),
                        DataMembers.NOTIFY_UPLOADED);
            }
        }
    };

    @Override
    public void onClick(View v) {
        int i1 = v.getId();
        if (i1 == R.id.reconnect_btn) {
            Logs.debug(TAG, "Reconnect Clicked");

            // updateConnectionState();


        } else if (i1 == R.id.calc_btn) {
            Intent i = new Intent();
            i.setClassName("com.android.calculator2",
                    "com.android.calculator2.Calculator");
            startActivity(i);

        } else if (i1 == R.id.skip_btn) {// bmodel.clearOrderTableChecked();
            bmodel.productHelper.clearOrderTable();
            disconnect();
            finish();
            if (!isFromReport)
                BusinessModel.loadActivity(this, DataMembers.actHomeScreenTwo);

        } else if (i1 == R.id.print_btn) {// doConnectionTest();
            new loadPrinter().execute();


        } else {
        }
    }

    public boolean isConnected() {
        // if (mBxlService.GetStatus() == mBxlService.BXL_SUCCESS)
        printer = connect();
        if (printer != null) {
            return true;
        }
        return false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (zebraPrinterConnection != null
                && zebraPrinterConnection.isConnected()) {
            disconnect();
        }
    }

    public ZebraPrinter connect() {
        setStatus("Connecting...", Color.YELLOW);

        zebraPrinterConnection = null;
        zebraPrinterConnection = new BluetoothPrinterConnection(
                getMacAddressFieldText());
        SettingsHelper.saveBluetoothAddress(this, getMacAddressFieldText());
        Commons.print(TAG+ ",getMacAddressFieldText() : " + getMacAddressFieldText());

        // if (!zebraPrinterConnection.isConnected()) {
        try {
            Commons.print(TAG+ ",trying to open printer");
            zebraPrinterConnection.open();
            setStatus("Connected", Color.GREEN);
        } catch (ZebraPrinterConnectionException e) {
            Commons.print(TAG+ ",trying to open printer exception");
            setStatus("Comm Error! Disconnecting", Color.RED);
            DemoSleeper.sleep(1000);
            disconnect();
        }
        // }

        ZebraPrinter printer = null;
        if (zebraPrinterConnection.isConnected()) {
            try {
                printer = ZebraPrinterFactory
                        .getInstance(zebraPrinterConnection);
                setStatus("Determining Printer Language", Color.YELLOW);
                PrinterLanguage pl = printer.getPrinterControlLanguage();
                setStatus("Printer Language " + pl, Color.BLUE);
            } catch (ZebraPrinterConnectionException e) {
                setStatus("Unknown Printer Language", Color.RED);
                printer = null;
                DemoSleeper.sleep(1000);
                disconnect();
            } catch (ZebraPrinterLanguageUnknownException e) {
                setStatus("Unknown Printer Language", Color.RED);
                printer = null;
                DemoSleeper.sleep(1000);
                disconnect();
            }
        } else {
            Commons.print(TAG+ ",printer connection fails here");
        }

        Commons.print(TAG+ ",printer" + printer);

        return printer;
    }

    public void disconnect() {
        try {
            setStatus("Disconnecting", Color.RED);
            if (zebraPrinterConnection != null) {
                zebraPrinterConnection.close();
            }
            setStatus("Not Connected", Color.RED);
        } catch (ZebraPrinterConnectionException e) {
            setStatus("COMM Error! Disconnected", Color.RED);
        } finally {
            // enableTestButton(true);
        }
    }

    private void setStatus(final String statusMessage, final int color) {
        runOnUiThread(new Runnable() {
            public void run() {
                statusField.setBackgroundColor(color);
                statusField.setText(statusMessage);
            }
        });
        DemoSleeper.sleep(1000);
    }

    private String getMacAddressFieldText() {

        String macAddress = "00:22:58:3C:B3:02";
        return macAddress;
    }

    private void sendTestLabel(String PrintableText) {
        try {
            byte[] configLabel = getConfigLabel(PrintableText);
            zebraPrinterConnection.write(configLabel);
            setStatus("Sending Data", Color.BLUE);
            DemoSleeper.sleep(1500);
            if (zebraPrinterConnection instanceof BluetoothPrinterConnection) {
                String friendlyName = ((BluetoothPrinterConnection) zebraPrinterConnection)
                        .getFriendlyName();

                setStatus(friendlyName, Color.MAGENTA);
                DemoSleeper.sleep(500);
            }
        } catch (ZebraPrinterConnectionException e) {
            setStatus(e.getMessage(), Color.RED);
        } finally {
            Commons.print(TAG+ ",not connected in send test label");
            disconnect();
        }
    }

    private byte[] getConfigLabel(String PrintableText) {

        Commons.print(TAG+ ",getconfiglabel called");

        PrinterLanguage printerLanguage = printer.getPrinterControlLanguage();

        byte[] configLabel = null;
        if (printerLanguage == PrinterLanguage.ZPL) {
            configLabel = "^XA^FO17,16^GB379,371,8^FS^FT65,255^A0N,135,134^FDTEST^FS^XZ"
                    .getBytes();
        } else if (printerLanguage == PrinterLanguage.CPCL) {
            // String cpclConfigLabel = "! 0 200 200 406 1\r\n"
            // + "ON-FEED IGNORE\r\n" + "BOX 20 20 380 380 8\r\n"
            // + "T 0 6 137 177 TESTPRINT123\r\n" + "PRINT\r\n";

            String cpclConfigLabel = PrintableText;

            configLabel = cpclConfigLabel.getBytes();
        }
        return configLabel;
    }

    public boolean printTitle(String title) {
        // CheckGC();
        // if (mBxlService.PrintText(title, mBxlService.BXL_ALIGNMENT_CENTER,
        // mBxlService.BXL_FT_DEFAULT | mBxlService.BXL_FT_BOLD,
        // mBxlService.BXL_TS_0WIDTH | mBxlService.BXL_TS_1HEIGHT) == 0)
        // return true;
        //
        // return false;
        sendTestLabel(title);

        return true;
    }

    public boolean printDate(String date) {
        // CheckGC();
        // if (mBxlService.PrintText(date, mBxlService.BXL_ALIGNMENT_RIGHT,
        // mBxlService.BXL_FT_FONTB, mBxlService.BXL_TS_0WIDTH
        // | mBxlService.BXL_TS_0HEIGHT) == 0)
        sendTestLabel(date);
        return true;
    }

    public boolean printText(String text) {
        // CheckGC();
        // if (mBxlService.PrintText(text, mBxlService.BXL_ALIGNMENT_LEFT,
        // mBxlService.BXL_FT_DEFAULT, mBxlService.BXL_TS_0WIDTH
        // | mBxlService.BXL_TS_0HEIGHT) == 0)
        sendTestLabel(text);
        return true;
    }

    public boolean printTextCenter(String text) {
        // CheckGC();
        // if (mBxlService.PrintText(text, mBxlService.BXL_ALIGNMENT_CENTER,
        // mBxlService.BXL_FT_DEFAULT, mBxlService.BXL_TS_0WIDTH
        // | mBxlService.BXL_TS_0HEIGHT) == 0)
        sendTestLabel(text);
        return true;
    }

    public boolean printLineFeed(int lines) {
        // CheckGC();
        // if (mBxlService.LineFeed(lines) == 0)
        sendTestLabel("\n\r");
        return true;
    }

    public boolean printTextBold(String text) {
        // CheckGC();
        // if (mBxlService.PrintText(text, mBxlService.BXL_ALIGNMENT_LEFT,
        // mBxlService.BXL_FT_DEFAULT | mBxlService.BXL_FT_BOLD,
        // mBxlService.BXL_TS_0WIDTH | mBxlService.BXL_TS_0HEIGHT) == 0)
        sendTestLabel(text);
        return true;
    }

    void CheckGC(String FunctionName) {
        long VmfreeMemory = Runtime.getRuntime().freeMemory();
        long VmmaxMemory = Runtime.getRuntime().maxMemory();
        long VmtotalMemory = Runtime.getRuntime().totalMemory();
        long waittime = 53;
        long Memorypercentage = ((VmtotalMemory - VmfreeMemory) * 100)
                / VmtotalMemory;

        Commons.print(TAG+","+ FunctionName + "Before Memorypercentage" + Memorypercentage
                + "% VmtotalMemory[" + VmtotalMemory + "] " + "VmfreeMemory["
                + VmfreeMemory + "] " + "VmmaxMemory[" + VmmaxMemory + "] ");

        // Runtime.getRuntime().gc();
        System.runFinalization();
        System.gc();
        VmfreeMemory = Runtime.getRuntime().freeMemory();
        VmmaxMemory = Runtime.getRuntime().maxMemory();
        VmtotalMemory = Runtime.getRuntime().totalMemory();
        Memorypercentage = ((VmtotalMemory - VmfreeMemory) * 100)
                / VmtotalMemory;
        Commons.print(TAG+","+ FunctionName + "_After Memorypercentage" + Memorypercentage
                + "% VmtotalMemory[" + VmtotalMemory + "] " + "VmfreeMemory["
                + VmfreeMemory + "] " + "VmmaxMemory[" + VmmaxMemory + "] ");
    }

    class loadPrinter extends AsyncTask<Integer, Integer, Boolean> {

        //	private ProgressDialog progressDialogue;
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                printInvoice();
            } catch (Exception e) {
                Commons.printException(e);
            }
            return Boolean.TRUE; // Return your real result here
        }

        protected void onPreExecute() {
            /*progressDialogue = ProgressDialog.show(InvoicePrintZebra.this,
					DataMembers.SD, "Printing...", true, false);*/

            builder = new AlertDialog.Builder(InvoicePrintZebra.this);

            customProgressDialog(builder, "Printing...");
            alertDialog = builder.create();
            alertDialog.show();
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground
            disconnect();
            //progressDialogue.dismiss();

            alertDialog.dismiss();
            if (isFromReport) {
                bmodel.showAlert("Printed successfully.", 1235);
            } else {
                bmodel.showAlert("Printed successfully.", 2222);
            }
        }
    }

    public void printInvoice() {

        Commons.print(TAG+ ",Print Invoice FN ");

        String PRINT_STATE = updateConnectionState();

        Commons.print(TAG+ ",print state in print invoice : " + PRINT_STATE);

        if (PRINT_STATE.equals("TRUE")) {

            Logs.debug(TAG, "Print clicked");
            if (!isConnected) {
                disconnect();
                bmodel.showAlert("Printer not connected. Click OK to Save.",
                        1234);
                return;
            }

            mPrintBTN.setOnClickListener(null);

            Commons.print(TAG+ ",mPrintProducts length : " + mPrintProducts.length());

            if (!mPrintProducts.equals("")) {
                Logs.debug(TAG, "Printer connected mprintproduct not null");

                if (isFromReport) {
                    printTextCenter("Duplicate");
                    printLineFeed(1);
                } else if (isFromOpenMarket) {
                    printTextCenter("Delivery Receipt");
                    printLineFeed(1);
                }

                printDate(mDate);
                printLineFeed(1);
                // printTitle("PROCTER & GAMBLE");
                // printLineFeed(1);
                printTextCenter("(P&G)" + mDistributorName + " ");
                printTextCenter("TIN: " + mTINNumber);

                printLineFeed(1);
                printTextCenter("Ph No: " + mDistributorContact);

                printLineFeed(1);
                printTextCenter(mOutletName);

                if (!isFromOpenMarket) {
                    printLineFeed(1);
                    printTextCenter("INV No: " + mInvoiceNumber);
                }

                printLineFeed(1);
                printText(mPrintProducts);
                printText(mPrintTotal);

                if (!isFromOpenMarket
                        && bmodel.configurationMasterHelper.IS_DELIVERY_REPORT) {
                    printText(mPrintDiscount);
                    printText(mPrintBillValue);
                }
                printLineFeed(2);

                sb.delete(0, sb.length());
                f.format("%30s", "S.O. Signature");
                printText(sb.toString());

                printLineFeed(1);
                sb.delete(0, sb.length());
                f.format("%30s", bmodel.userMasterHelper.getUserMasterBO()
                        .getUserName());
                printText(sb.toString());

                printLineFeed(3);

                // if (isConnected()) {
                // bmodel.clearOrderTableChecked();
                // disconnect();
                // if (isFromReport)
                // bmodel.showAlert("Printed successfully.", 1235);
                // else
                // bmodel.showAlert("Printed successfully.", 2222);
                // }
            }
        } else {
            if (PRINT_STATE.equals("NO_BLUETOOTH")) {

                bmodel.showAlert("Bluetooth connection not available!", 0);
            }
            if (PRINT_STATE.equals("NO_PRINTER")) {

                bmodel.showAlert("Printer not connected properly!", 0);

            } else {
            }
        }
    }

}
