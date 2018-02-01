package com.ivy.sd.png.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bixolon.printer.BixolonPrinter;
import com.ivy.cpg.view.order.OrderHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.MyThread;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@SuppressLint("NewApi")
public class BixolonIIPrint extends Bixolon {
    String PRINT_STATE = "";

    static BixolonPrinter mBixolonPrinter;
    private boolean isconnected;
    boolean IsFromOrder, IsFromReport;
    //	private ProgressDialog pd;
    TextView printtitle;
    //	private ProgressDialog progressDialogue;
    private AlertDialog.Builder builder, builder1;
    private AlertDialog alertDialog, alertDialog1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        mStatusTV.setText(getResources().getString(
                R.string.printer_not_connected));

        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            if (extras.containsKey("IsFromOrder")) {
                IsFromOrder = extras.getBoolean("IsFromOrder");
            }
            if (extras.containsKey("IsFromReport"))
                IsFromReport = extras.containsKey("IsFromReport");
        }
        printtitle = (TextView) findViewById(R.id.printtitle);
        if (IsFromOrder) {
            printtitle.setText(getResources().getString(
                    R.string.order_print_preview));
            title = getResources().getString(R.string.order_print_preview);
            receipttitle = getResources().getString(R.string.order_slip);
            mDistContactTV.setText(getResources()
                    .getString(R.string.order_slip));
        } else {
            printtitle.setText(getResources().getString(
                    R.string.invoice_print_preview));
            title = getResources().getString(R.string.invoice_print_preview);
            receipttitle = getResources().getString(R.string.invoice_slip);
            mDistContactTV.setText(getResources().getString(
                    R.string.invoice_slip));
        }
        mBixolonPrinter = new BixolonPrinter(this, mHandler, null);

        mBixolonPrinter.findBluetoothPrinters();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!IsFromOrder) {

            mInvoiceNoTv.setText(getResources().getString(R.string.invno) + ":"
                    + mInvoiceNumber);
        } else
            mInvoiceNoTv.setVisibility(View.GONE);
    }

    public Handler getHandler() {
        return mHandler;
    }

    private final Handler mHandler = new Handler(new Handler.Callback() {

        @SuppressWarnings("unchecked")
        @Override
        public boolean handleMessage(Message msg) {
            Commons.print(TAG + "mHandler.handleMessage(" + msg + ")");

            switch (msg.what) {
                case BixolonPrinter.MESSAGE_STATE_CHANGE:
                    mBixolonPrinter.getStatus();
                    switch (msg.arg1) {
                        case BixolonPrinter.STATE_CONNECTED:
                            updateStatus(
                                    getResources()
                                            .getString(R.string.printer_connected),
                                    true);

                            alertDialog.dismiss();
                            //progressDialogue.dismiss();
                            Toast.makeText(getApplicationContext(),
                                    getResources().getString(R.string.connected),
                                    Toast.LENGTH_SHORT).show();
                            PRINT_STATE = "TRUE";
                            isconnected = true;
                            supportInvalidateOptionsMenu();
                            break;

                        case BixolonPrinter.STATE_CONNECTING:
                            updateStatus(getResources().getString(R.string.connecting),
                                    false);
                            // Toast.makeText(getApplicationContext(),
                            // getResources().getString(R.string.connecting),
                            // Toast.LENGTH_SHORT).show();
                            connectingPrint();
                            break;

                        case BixolonPrinter.STATE_NONE:
                            updateStatus(
                                    getResources().getString(
                                            R.string.printer_not_connected), false);

                            alertDialog.dismiss();
                            //	progressDialogue.dismiss();
                            Toast.makeText(
                                    getApplicationContext(),
                                    getResources().getString(
                                            R.string.printer_not_connected),
                                    Toast.LENGTH_SHORT).show();
                            PRINT_STATE = "NO_PRINTER";
                            isconnected = false;
                            break;
                    }
                    return true;

                case BixolonPrinter.MESSAGE_READ:
                    BixolonIIPrint.this.dispatchMessage(msg);
                    return true;

                case BixolonPrinter.MESSAGE_TOAST:
                    Toast.makeText(
                            getApplicationContext(),
                            msg.getData()
                                    .getString(BixolonPrinter.KEY_STRING_TOAST),
                            Toast.LENGTH_SHORT).show();
                    return true;

                case BixolonPrinter.MESSAGE_BLUETOOTH_DEVICE_SET:
                    if (msg.obj == null) {
                        Toast.makeText(
                                getApplicationContext(),
                                getResources().getString(R.string.no_paired_device),
                                Toast.LENGTH_SHORT).show();
                    } else {

                        showBluetoothDialog(BixolonIIPrint.this,
                                (Set<BluetoothDevice>) msg.obj);
                    }
                    return true;

                case BixolonPrinter.MESSAGE_PRINT_COMPLETE:
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.complete_to_print),
                            Toast.LENGTH_SHORT).show();
                    return true;

                case BixolonPrinter.MESSAGE_ERROR_OUT_OF_MEMORY:
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.out_of_memory),
                            Toast.LENGTH_SHORT).show();
                    return true;
                case DataMembers.NOTIFY_ORDER_SAVED:
                   /* if (pd != null)
                        pd.dismiss();*/
                    if (alertDialog1 != null)
                        alertDialog1.dismiss();
                    return true;
                case DataMembers.NOTIFY_ORDER_DELETED:
                    try {
                       /* if (pd != null)
                            pd.dismiss();*/
                        if (alertDialog1 != null)
                            alertDialog1.dismiss();
                        bmodel = (BusinessModel) getApplicationContext();
                        bmodel.showAlert(
                                getResources().getString(
                                        R.string.order_deleted_sucessfully)
                                        + OrderHelper.getInstance(BixolonIIPrint.this).getOrderid(),
                                DataMembers.NOTIFY_ORDER_SAVED);
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                    return true;

                case DataMembers.NOTIFY_INVOICE_SAVED:
                    try {
                      /*  if (pd != null)
                            pd.dismiss();*/
                        if (alertDialog1 != null)
                            alertDialog1.dismiss();
                        bmodel = (BusinessModel) getApplicationContext();
                        // bmodel.productHelper.clearOrderTable();
                        if (bmodel.configurationMasterHelper.IS_INVOICE) {
                            if (IsFromOrder)
                                bmodel.showAlert(
                                        getResources()
                                                .getString(
                                                        R.string.order_created_successfully),
                                        DataMembers.NOTIFY_INVOICE_SAVED);
                            else
                                bmodel.showAlert(
                                        getResources()
                                                .getString(
                                                        R.string.invoice_created_successfully),
                                        DataMembers.NOTIFY_INVOICE_SAVED);
                        } else {
                            if (IsFromOrder)
                                bmodel.showAlert(
                                        getResources()
                                                .getString(
                                                        R.string.order_saved_and_print_preview_created_successfully),
                                        DataMembers.NOTIFY_INVOICE_SAVED);
                            else
                                bmodel.showAlert(
                                        getResources()
                                                .getString(
                                                        R.string.invoice_saved_and_print_preview_created_successfully),
                                        DataMembers.NOTIFY_INVOICE_SAVED);
                        }
                    } catch (Exception e) {
                        Commons.print("Save Invoice Error :" + e.toString());
                    }
                    return true;
                case DataMembers.NOTIFY_PRINT:
                    printData();
                    return true;
            }
            return false;
        }

    });

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
                            BixolonIIPrint.mBixolonPrinter
                                    .connect(items[which]);

                        }
                    });
            bmodel.applyAlertDialogTheme(builder);
        } else
            BixolonIIPrint.mBixolonPrinter.connect(items[0]);
    }

    protected void printData() {
        try {
            if (PRINT_STATE != null && PRINT_STATE.equals("TRUE")) {
                if (!mPrintProducts.equals("")) {
                    for (int i = 0; i < SDUtil.convertToInt(count); i++) {
                        printHeader();
                        printOrder();
                    }

                    if (!IsFromReport) {
                        bmodel.productHelper.clearOrderTableChecked();
                        bmodel.showAlert(
                                getResources().getString(
                                        R.string.printed_successfully), 1234);

                    } else
                        bmodel.showAlert(
                                getResources().getString(
                                        R.string.printed_successfully), 121);

                }
            } else {
                // if (PRINT_STATE.equals("NO_PRINTER")) {
                bmodel.productHelper.clearOrderTableChecked();
                bmodel.showAlert(
                        getResources()
                                .getString(R.string.printer_not_connected),
                        1234);
                // }
            }
        } catch (Exception e) {
            Commons.print("Print Invoice Error :" + e.toString());
        }
    }

    private void dispatchMessage(Message msg) {
        switch (msg.arg1) {
            case BixolonPrinter.PROCESS_GET_STATUS:
                if (msg.arg2 == BixolonPrinter.STATUS_NORMAL) {
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.no_error),
                            Toast.LENGTH_SHORT).show();
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

                    Toast.makeText(getApplicationContext(), buffer.toString(),
                            Toast.LENGTH_SHORT).show();
                }
                break;

            case BixolonPrinter.PROCESS_GET_BATTERY_VOLTAGE_STATUS:
                if (msg.arg2 == BixolonPrinter.STATUS_BATTERY_LOW_VOLTAGE) {
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.low_voltage),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.normal_voltage),
                            Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }

    @Override
    public void printTextCenter(String text, int size) {
        CheckGC();
        if (size == 1)
            mBixolonPrinter.printText(text, BixolonPrinter.ALIGNMENT_CENTER,
                    BixolonPrinter.TEXT_ATTRIBUTE_FONT_A,
                    BixolonPrinter.TEXT_SIZE_HORIZONTAL1
                            | BixolonPrinter.TEXT_SIZE_VERTICAL1, true);
        else if (size == 2)
            mBixolonPrinter.printText(text, BixolonPrinter.ALIGNMENT_CENTER,
                    BixolonPrinter.TEXT_ATTRIBUTE_FONT_A,
                    BixolonPrinter.TEXT_SIZE_HORIZONTAL2
                            | BixolonPrinter.TEXT_SIZE_VERTICAL2, true);
    }

    @Override
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

    @Override
    public void printTextRight(String text, int size) {
        CheckGC();
        if (size == 1)
            mBixolonPrinter.printText(text, BixolonPrinter.ALIGNMENT_RIGHT,
                    BixolonPrinter.TEXT_ATTRIBUTE_FONT_A,
                    BixolonPrinter.TEXT_SIZE_HORIZONTAL1
                            | BixolonPrinter.TEXT_SIZE_VERTICAL1, true);
        else if (size == 2)
            mBixolonPrinter.printText(text, BixolonPrinter.ALIGNMENT_RIGHT,
                    BixolonPrinter.TEXT_ATTRIBUTE_FONT_A,
                    BixolonPrinter.TEXT_SIZE_HORIZONTAL2
                            | BixolonPrinter.TEXT_SIZE_VERTICAL2, true);

    }

    public void printLineFeed(int lines) {
        CheckGC();
        mBixolonPrinter.lineFeed(lines, true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindDrawables(findViewById(R.id.root));
        mBixolonPrinter.disconnect();
        // bmodel.productHelper.clearOrderTable();
        // force the garbage collector to run
        System.gc();
    }

    /**
     * this would clear all the resources used of the layout.
     *
     * @param view
     */
    private void unbindDrawables(View view) {
        if (view != null) {
            if (view.getBackground() != null) {
                view.getBackground().setCallback(null);
            }
            if (view instanceof ViewGroup) {
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                    unbindDrawables(((ViewGroup) view).getChildAt(i));
                }
                try {
                    ((ViewGroup) view).removeAllViews();
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_invoice_print, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (IsFromReport)
            bmodel.configurationMasterHelper.IS_INVOICE_AS_MOD = false;
        if (IsFromOrder)
            bmodel.configurationMasterHelper.IS_INVOICE_AS_MOD = false;

        if (!isconnected)
            menu.findItem(R.id.menu_print).setEnabled(false);
        else
            menu.findItem(R.id.menu_print).setEnabled(true);

        if (!bmodel.configurationMasterHelper.SHOW_CALC)
            menu.findItem(R.id.menu_calculator).setVisible(false);
        if (bmodel.configurationMasterHelper.IS_INVOICE_AS_MOD) {
            menu.findItem(R.id.menu_print).setVisible(false);
            menu.findItem(R.id.menu_preview_save).setVisible(true);
        } else {
            menu.findItem(R.id.menu_print).setVisible(true);
            menu.findItem(R.id.menu_preview_save).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);

    }

    public void connectingPrint() {
        /*progressDialogue = ProgressDialog.show(this, DataMembers.SD,
                getResources().getString(R.string.connecting), true, false);*/

        builder = new AlertDialog.Builder(BixolonIIPrint.this);

        customProgressDialog(builder, getResources().getString(R.string.connecting));
        alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i1 = item.getItemId();
        if (i1 == android.R.id.home) {
            if (!IsFromReport)
                bmodel.productHelper.clearOrderTable();

            if (IsFromReport) {
                finish();
            } else {
                bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                        .now(SDUtil.TIME));
                finish();
                BusinessModel.loadActivity(this, DataMembers.actHomeScreenTwo);
            }
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        } else if (i1 == R.id.menu_print) {
            printData();

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

                // OrderHeader ord = new OrderHeader();
                // ord.setOrderValue(mTotalValue);
                // ord.setLinesPerCall(lines);
                // ord.setDiscount(0);
                // ord.setDeliveryDate(SDUtil.now(SDUtil.DATE_GLOBAL));
                //
                // bmodel.setOrderHeaderBO(ord);

                if (bmodel.hasOrder()) {
                    // bmodel.invoiceDisount = "0";
                    if (bmodel.configurationMasterHelper.IS_INVOICE) {
                        builder1 = new AlertDialog.Builder(BixolonIIPrint.this);

                        customProgressDialog(builder1, getResources().getString(
                                R.string.saving_invoice));
                        alertDialog1 = builder1.create();
                        alertDialog1.show();
                          /*  pd = ProgressDialog.show(
                                    BixolonIIPrint.this,
                                    DataMembers.SD,
                                    getResources().getString(
                                            R.string.saving_invoice), true, false);*/
                    } else {
                        builder1 = new AlertDialog.Builder(BixolonIIPrint.this);

                        customProgressDialog(builder1, getResources().getString(
                                R.string.saving_new_order));
                        alertDialog1 = builder1.create();
                        alertDialog1.show();

                           /* pd = ProgressDialog
                                    .show(BixolonIIPrint.this,
                                            DataMembers.SD,
                                            getResources().getString(
                                                    R.string.saving_new_order),
                                            true, false);*/
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

        } else {
        }
        return false;
    }

}
