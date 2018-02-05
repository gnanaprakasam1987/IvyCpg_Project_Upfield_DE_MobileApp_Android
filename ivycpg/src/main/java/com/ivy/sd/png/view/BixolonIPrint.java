package com.ivy.sd.png.view;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.bixolon.android.library.BxlService;
import com.ivy.cpg.view.order.OrderHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.OrderHeader;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.MyThread;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BixolonIPrint extends Bixolon {

    private final String MSG_BLUETOOTH_NOT_ENABLED = "Bluetooth not enabled.";
    private final String MSG_PRINTER_NOT_CONNECTED = "Printer not connected";
    private final String MSG_PRINTER_CONNECTED = "Printer connected";

    private BluetoothAdapter mBluetoothAdapter;
    private BxlService mBxlService = null;
    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;
    private boolean isBluetoothEnabled = false;
    private boolean isConnected = false;
    private boolean IsFromOrder, IsFromReport;
    //private ProgressDialog pd;
    private AlertDialog.Builder builder;
    private AlertDialog alertDialog;
    String state;
    TextView printtitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            if (extras.containsKey("IsFromOrder")) {
                IsFromOrder = extras.getBoolean("IsFromOrder");
            }
            if (extras.containsKey("IsFromReport")) {
                IsFromReport = extras.getBoolean("IsFromReport");
            }
        }

        printtitle = (TextView) findViewById(R.id.printtitle);
        if (IsFromOrder) {
            printtitle.setText(getResources().getString(
                    R.string.order_print_preview));
            title = getResources().getString(R.string.order_print_preview);
        } else {
            printtitle.setText(getResources().getString(
                    R.string.invoice_print_preview));
            title = getResources().getString(R.string.invoice_print_preview);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK,
                "My Tag");
        mWakeLock.acquire();

        mBxlService = new BxlService();

        updateConnectionState();

        if (!IsFromOrder) {
            TextView tv = (TextView) findViewById(R.id.invoice_number_tv);
            tv.setVisibility(View.VISIBLE);
            mInvoiceNoTv.setText(getResources().getString(R.string.invno) + ":"
                    + mInvoiceNumber);
        } else
            mInvoiceNoTv.setVisibility(View.GONE);

    }

    public Handler getHandler() {
        return mHandler;
    }

    private String updateConnectionState() {

        state = "FALSE";

        if (mBluetoothAdapter != null) {
            if (mBluetoothAdapter.isEnabled()) {
                isBluetoothEnabled = true;
            }
        }

        if (isBluetoothEnabled) {
            Commons.print("connect" + mBxlService.Connect() + "");
            if (mBxlService.Connect() == 0) {
                updateStatus(MSG_PRINTER_CONNECTED, true);
                isConnected = true;

                // mReconnectBTN.setVisibility(View.GONE);
                // mPrintBTN.setVisibility(View.VISIBLE);
                state = "TRUE";
            } else {
                updateStatus(MSG_PRINTER_NOT_CONNECTED, false);
                isConnected = false;
                mStatusIV.setImageResource(R.drawable.redball);
                // mReconnectBTN.setVisibility(View.VISIBLE);
                // mPrintBTN.setVisibility(View.GONE);

                state = "NO_PRINTER";
            }
        } else {
            updateStatus(MSG_BLUETOOTH_NOT_ENABLED, false);

            // mReconnectBTN.setVisibility(View.VISIBLE);
            // mPrintBTN.setVisibility(View.GONE);
            state = "NO_BLUETOOTH";
        }
        return state;
    }

    public boolean isConnected() {
        if (mBxlService.GetStatus() == BxlService.BXL_SUCCESS)
            return true;

        return false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isConnected) {
            mBxlService.Disconnect();
            mBxlService = null;
            isConnected = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isConnected) {
            mBxlService.Disconnect();
            mBxlService = null;
            isConnected = false;
        }
    }

    private final Handler mHandler = new Handler(new Handler.Callback() {

        @SuppressWarnings("unchecked")
        @Override
        public boolean handleMessage(Message msg) {
            Commons.print(TAG + "mHandler.handleMessage(" + msg + ")");

            switch (msg.what) {

                case DataMembers.NOTIFY_ORDER_SAVED:
                   /* if (pd != null)
                        pd.dismiss();*/
                    if (alertDialog != null)
                        alertDialog.dismiss();
                    return true;
                case DataMembers.NOTIFY_ORDER_DELETED:
                    try {
                      /*  if (pd != null)
                            pd.dismiss();*/
                        if (alertDialog != null)
                            alertDialog.dismiss();
                        bmodel = (BusinessModel) getApplicationContext();
                        bmodel.showAlert(
                                getResources().getString(
                                        R.string.order_deleted_sucessfully)
                                        + OrderHelper.getInstance(BixolonIPrint.this).getOrderId(),
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
                    try {
                        if (state != null && state.equals("TRUE")) {
                            if (!mPrintProducts.equals("")) {
                                for (int i = 0; i < SDUtil.convertToInt(count); i++) {
                                    printHeader();
                                    printOrder();
                                }

                                if (!IsFromReport) {
                                    bmodel.productHelper.clearOrderTableChecked();
                                    bmodel.showAlert(
                                            getResources().getString(
                                                    R.string.printed_successfully),
                                            1234);
                                } else
                                    bmodel.showAlert(
                                            getResources().getString(
                                                    R.string.printed_successfully),
                                            121);
                            }
                        } else {
                            // if (PRINT_STATE.equals("NO_PRINTER")) {
                            bmodel.productHelper.clearOrderTableChecked();
                            bmodel.showAlert(
                                    getResources().getString(
                                            R.string.printer_not_connected), 1234);
                            // }
                        }
                    } catch (Exception e) {
                        Commons.print("Print Invoice Error :" + e.toString());
                    }
                    return true;
            }
            return false;
        }
    });

    @Override
    public void printTextRight(String date, int size) {
        CheckGC();
        if (size == 1) {
            mBxlService.PrintText(date, BxlService.BXL_ALIGNMENT_RIGHT,
                    BxlService.BXL_FT_FONTB, BxlService.BXL_TS_0WIDTH
                            | BxlService.BXL_TS_0HEIGHT);

        }
        if (size == 2) {
            mBxlService.PrintText(date, BxlService.BXL_ALIGNMENT_RIGHT,
                    BxlService.BXL_FT_FONTB, BxlService.BXL_TS_1WIDTH
                            | BxlService.BXL_TS_1HEIGHT);
        }
    }

    @Override
    public void printTextLeft(String text, int size) {
        CheckGC();
        if (size == 1) {
            mBxlService.PrintText(text, BxlService.BXL_ALIGNMENT_LEFT,
                    BxlService.BXL_FT_DEFAULT, BxlService.BXL_TS_0WIDTH
                            | BxlService.BXL_TS_0HEIGHT);
        }
        if (size == 2) {
            mBxlService.PrintText(text, BxlService.BXL_ALIGNMENT_LEFT,
                    BxlService.BXL_FT_DEFAULT, BxlService.BXL_TS_1WIDTH
                            | BxlService.BXL_TS_1HEIGHT);
        }
    }

    @Override
    public void printTextCenter(String text, int size) {
        CheckGC();
        if (size == 1) {
            mBxlService.PrintText(text, BxlService.BXL_ALIGNMENT_CENTER,
                    BxlService.BXL_FT_DEFAULT, BxlService.BXL_TS_0WIDTH
                            | BxlService.BXL_TS_0HEIGHT);
        }
        if (size == 2) {
            mBxlService.PrintText(text, BxlService.BXL_ALIGNMENT_CENTER,
                    BxlService.BXL_FT_DEFAULT, BxlService.BXL_TS_1WIDTH
                            | BxlService.BXL_TS_1HEIGHT);
        }
    }

    @Override
    public void printLineFeed(int lines) {
        CheckGC();
        mBxlService.LineFeed(lines);

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
        if (view != null) {
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
    }

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
                finish();
            } else {
                finish();
                BusinessModel.loadActivity(this, DataMembers.actHomeScreenTwo);
            }
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        } else if (i1 == R.id.menu_print) {
            String PRINT_STATE = updateConnectionState();

            if (PRINT_STATE.equals("TRUE")) {

                if (!isConnected) {
                    if (IsFromReport)
                        bmodel.showAlert(
                                getResources().getString(
                                        R.string.printer_not_connected), 121);
                    else
                        bmodel.showAlert(
                                getResources().getString(
                                        R.string.printer_not_connected), 1234);
                    return true;
                }

                if (!mPrintProducts.equals("")) {
                    if (isConnected()) {
                        for (int i = 0; i < SDUtil.convertToInt(count); i++) {
                            printHeader();
                            printOrder();
                        }
                        if (isConnected()) {
                            bmodel.productHelper.clearOrderTableChecked();
                            if (IsFromReport) {
                                bmodel.showAlert(
                                        getResources().getString(
                                                R.string.printed_successfully),
                                        121);
                            } else {
                                bmodel.showAlert(
                                        getResources().getString(
                                                R.string.printed_successfully),
                                        1234);
                            }
                        }
                    }
                }
            } else if (PRINT_STATE == null
                    || PRINT_STATE.equals("NO_BLUETOOTH")
                    && PRINT_STATE.equals("NO_PRINTER")) {
                if (IsFromReport)
                    bmodel.showAlert(
                            getResources()
                                    .getString(
                                            R.string.bluetooth_connection_not_available)
                                    + " & \n"
                                    + getResources().getString(
                                    R.string.printer_not_connected),
                            121);
                else
                    bmodel.showAlert(
                            getResources()
                                    .getString(
                                            R.string.bluetooth_connection_not_available)
                                    + " & \n"
                                    + getResources().getString(
                                    R.string.printer_not_connected),
                            1234);
            } else if (PRINT_STATE.equals("NO_BLUETOOTH")) {
                if (IsFromReport)
                    bmodel.showAlert(
                            getResources()
                                    .getString(
                                            R.string.bluetooth_connection_not_available),
                            121);
                else
                    bmodel.showAlert(
                            getResources()
                                    .getString(
                                            R.string.bluetooth_connection_not_available),
                            1234);
            } else if (PRINT_STATE.equals("NO_PRINTER")) {
                if (IsFromReport)
                    bmodel.showAlert(
                            getResources().getString(
                                    R.string.printer_not_connected), 121);
                else
                    bmodel.showAlert(
                            getResources().getString(
                                    R.string.printer_not_connected), 1234);

            }

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
                    // bmodel.invoiceDisount = "0";
                    if (bmodel.configurationMasterHelper.IS_INVOICE) {
                        builder = new AlertDialog.Builder(BixolonIPrint.this);

                        customProgressDialog(builder, getResources().getString(
                                R.string.saving_invoice));
                        alertDialog = builder.create();
                        alertDialog.show();
                           /* pd = ProgressDialog.show(
                                    BixolonIPrint.this,
                                    DataMembers.SD,
                                    getResources().getString(
                                            R.string.saving_invoice), true, false);*/
                    } else {
                        builder = new AlertDialog.Builder(BixolonIPrint.this);

                        customProgressDialog(builder, getResources().getString(
                                R.string.saving_new_order));
                        alertDialog = builder.create();
                        alertDialog.show();
                         /*   pd = ProgressDialog
                                    .show(BixolonIPrint.this,
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

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!bmodel.configurationMasterHelper.SHOW_CALC)
            menu.findItem(R.id.menu_calculator).setVisible(false);

        if (!bmodel.configurationMasterHelper.SHOW_CALC)
            menu.findItem(R.id.menu_calculator).setVisible(false);

        if (IsFromReport)
            bmodel.configurationMasterHelper.IS_INVOICE_AS_MOD = false;
        if (IsFromOrder)
            bmodel.configurationMasterHelper.IS_INVOICE_AS_MOD = false;

        if (bmodel.configurationMasterHelper.IS_INVOICE_AS_MOD) {
            menu.findItem(R.id.menu_print).setVisible(false);
            menu.findItem(R.id.menu_preview_save).setVisible(true);
        } else {
            menu.findItem(R.id.menu_print).setVisible(true);
            menu.findItem(R.id.menu_preview_save).setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }
}
