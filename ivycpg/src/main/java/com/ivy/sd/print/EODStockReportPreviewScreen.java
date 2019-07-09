package com.ivy.sd.print;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.ivy.cpg.view.order.OrderHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.StockReportBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ReportHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;

import java.util.ArrayList;

public class EODStockReportPreviewScreen extends IvyBaseActivityNoActionBar {

    private BusinessModel bmodel;
    private String count;
    private EditText mMacAddressET;
    private TextView statusField;
    private ImageView mStatusIV;
    private static final int SELECTED_PRINTER_DIALOG = 1;
    private static final String ZEBRA_2INCH = "2";
    private static final String ZEBRA_3INCH = "3";
    private static final String ZEBRA_4INCH = "4";
    private Connection zebraPrinterConnection;
    private TextView mLoadStkTV, mSoldStkTV, mFreeQtyTV, mSihTV, mEmptyQtyTV, mReturnQtyTV, mReplaceMentQtyTV;
    private static final String[] mPrinterSelectionArray = {ZEBRA_2INCH, ZEBRA_3INCH,
            ZEBRA_4INCH};
    private String mSelectedPrinterName;
    private LinearLayout mDetailsContainerLL;

    private ArrayList<StockReportBO> mDetails;
    private int mPrintDetailsCount = 0;
    private boolean isPrintClicked;

    private static final String TAG = "EodStockPrint";
    private Toolbar toolbar;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eodstockreport_preview_zebra_diageo);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);


        mLoadStkTV = (TextView) findViewById(R.id.loadstk_uom);
        mSoldStkTV = (TextView) findViewById(R.id.soldstk_uom);
        mFreeQtyTV = (TextView) findViewById(R.id.free_uom);
        mSihTV = (TextView) findViewById(R.id.sih_uom);
        mEmptyQtyTV = (TextView) findViewById(R.id.empty_uom);
        mReturnQtyTV = (TextView) findViewById(R.id.return_uom);
        mReplaceMentQtyTV = (TextView) findViewById(R.id.replacement_uom);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        try {
            bmodel = (BusinessModel) getApplicationContext();
            bmodel.setContext(this);

            mMacAddressET = (EditText) findViewById(R.id.et_mac);
            statusField = (TextView) findViewById(R.id.status_bar);
            mStatusIV = (ImageView) findViewById(R.id.status_iv);
            mDetailsContainerLL = (LinearLayout) findViewById(R.id.details_container_ll);

            if (!bmodel.configurationMasterHelper.SHOW_STOCK_FREE_ISSUED)
                findViewById(R.id.ll_free_issued).setVisibility(View.GONE);
            if (!bmodel.configurationMasterHelper.SHOW_STOCK_EMPTY)
                findViewById(R.id.ll_emptyQty).setVisibility(View.GONE);
            if (!bmodel.configurationMasterHelper.SHOW_STOCK_REPLACE)
                findViewById(R.id.ll_replacementQty).setVisibility(View.GONE);

            if (bmodel.configurationMasterHelper.CONVERT_EOD_SIH_PS ||
                    bmodel.configurationMasterHelper.CONVERT_EOD_SIH_CS ||
                    bmodel.configurationMasterHelper.CONVERT_EOD_SIH_OU) {

                mLoadStkTV.setVisibility(View.VISIBLE);
                mSoldStkTV.setVisibility(View.VISIBLE);
                mSihTV.setVisibility(View.VISIBLE);
                mFreeQtyTV.setVisibility(View.VISIBLE);
                mEmptyQtyTV.setVisibility(View.VISIBLE);
                mReturnQtyTV.setVisibility(View.VISIBLE);
                mReplaceMentQtyTV.setVisibility(View.VISIBLE);

            } else if (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT) {
                mLoadStkTV.setVisibility(View.VISIBLE);
                mSoldStkTV.setVisibility(View.VISIBLE);
                mSihTV.setVisibility(View.VISIBLE);
                mFreeQtyTV.setVisibility(View.VISIBLE);
                mEmptyQtyTV.setVisibility(View.VISIBLE);
                mReturnQtyTV.setVisibility(View.VISIBLE);
                mReplaceMentQtyTV.setVisibility(View.VISIBLE);
            } else {
                mLoadStkTV.setVisibility(View.GONE);
                mSoldStkTV.setVisibility(View.GONE);
                mSihTV.setVisibility(View.GONE);
                mFreeQtyTV.setVisibility(View.GONE);
                mEmptyQtyTV.setVisibility(View.GONE);
                mReturnQtyTV.setVisibility(View.GONE);
                mReplaceMentQtyTV.setVisibility(View.GONE);
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
        } catch (Exception e1) {
            Commons.printException(e1 + " ");
        }
        /**
         * set values in print copy spinner
         **/
        try {
            Spinner printcount = (Spinner) findViewById(R.id.printcount);
            ArrayAdapter<CharSequence> spinadapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item);
            spinadapter
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            int prntcount = bmodel.configurationMasterHelper.printCount;
            if (prntcount == 0)
                prntcount = 1;
            for (int i = 1; i <= prntcount; ++i) {

                spinadapter.add(Integer.toString(i));
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

                }

            });

            SharedPreferences pref = this.getSharedPreferences("PRINT",
                    MODE_PRIVATE);
            mMacAddressET.setText(pref.getString("MAC", ""));
        } catch (Exception e) {
            Commons.printException(e + "");
        }
        doInitialize();
    }

    private void doInitialize() {
        try {

            mDetails = ReportHelper.getInstance(this).getEODStockReport();
            String caseOrPieceOrOuter = "";
            String slash = "";
            if (bmodel.configurationMasterHelper.CONVERT_EOD_SIH_PS ||
                    bmodel.configurationMasterHelper.CONVERT_EOD_SIH_CS ||
                    bmodel.configurationMasterHelper.CONVERT_EOD_SIH_OU) {


                if (bmodel.configurationMasterHelper.CONVERT_EOD_SIH_CS) {
                    caseOrPieceOrOuter = "c";
                    slash = "/";
                }
                if (bmodel.configurationMasterHelper.CONVERT_EOD_SIH_PS) {
                    caseOrPieceOrOuter = caseOrPieceOrOuter + slash + "p";
                    slash = "/";
                }
                if (bmodel.configurationMasterHelper.CONVERT_EOD_SIH_OU) {
                    caseOrPieceOrOuter = caseOrPieceOrOuter + slash + "o";
                }
                mLoadStkTV.setText(caseOrPieceOrOuter);
                mSoldStkTV.setText(caseOrPieceOrOuter);
                mFreeQtyTV.setText(caseOrPieceOrOuter);
                mSihTV.setText(caseOrPieceOrOuter);
                mEmptyQtyTV.setText(caseOrPieceOrOuter);
                mReturnQtyTV.setText(caseOrPieceOrOuter);
                mReplaceMentQtyTV.setText(caseOrPieceOrOuter);
            }


            else if (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT) {

                if (bmodel.configurationMasterHelper.SHOW_EOD_OC) {
                    caseOrPieceOrOuter = "c";
                    slash = "/";
                }
                if (bmodel.configurationMasterHelper.SHOW_EOD_OP) {
                    caseOrPieceOrOuter = caseOrPieceOrOuter + slash + "p";
                    slash = "/";
                }
                if (bmodel.configurationMasterHelper.SHOW_EOD_OO) {
                    caseOrPieceOrOuter = caseOrPieceOrOuter + slash + "o";
                }
                mLoadStkTV.setText(caseOrPieceOrOuter);
                mSoldStkTV.setText(caseOrPieceOrOuter);
                mFreeQtyTV.setText(caseOrPieceOrOuter);
                mSihTV.setText(caseOrPieceOrOuter);
                mEmptyQtyTV.setText(caseOrPieceOrOuter);
                mReturnQtyTV.setText(caseOrPieceOrOuter);
                mReplaceMentQtyTV.setText(caseOrPieceOrOuter);
            }

            updateDetails();

        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }

    private void updateDetails() {
        try {
            TextView pName;
            TextView loadStk;
            TextView soldStk;
            TextView free;
            TextView sih;
            TextView empty;
            TextView returnQty;
            TextView replacement;
            TextView batchno;

            mDetailsContainerLL.removeAllViews();
            LayoutInflater inflater = getLayoutInflater();
            mPrintDetailsCount = 0;
            for (StockReportBO stockBO : mDetails) {
                if (stockBO.getVanLoadQty() > 0 || stockBO.getEmptyBottleQty() > 0 || stockBO.getFreeIssuedQty() > 0
                        || stockBO.getSoldQty() > 0 || stockBO.getReplacementQty() > 0 || stockBO.getReturnQty() > 0) {

                    mPrintDetailsCount = mPrintDetailsCount + 1;
                    final ViewGroup nullParent = null;
                    View v = inflater.inflate(
                            R.layout.row_eodstock_print_preview_diageo, nullParent);

                    pName = (TextView) v.findViewById(R.id.productname);
                    loadStk = (TextView) v.findViewById(R.id.loadstk);
                    soldStk = (TextView) v.findViewById(R.id.soldstk);
                    free = (TextView) v.findViewById(R.id.free);
                    sih = (TextView) v.findViewById(R.id.sih);
                    empty = (TextView) v.findViewById(R.id.empty);
                    returnQty = (TextView) v.findViewById(R.id.returnQty);
                    replacement = (TextView) v.findViewById(R.id.replacementQty);
                    batchno = (TextView) v.findViewById(R.id.batchno);
                    if (stockBO.getBatchNo() != null && !stockBO.getBatchNo().equals("")) {
                        batchno.setVisibility(View.VISIBLE);
                        batchno.setText(stockBO.getBatchNo());
                    }

                    pName.setText(stockBO.getProductName() + "");
                    if (bmodel.configurationMasterHelper.CONVERT_EOD_SIH_PS ||
                            bmodel.configurationMasterHelper.CONVERT_EOD_SIH_CS ||
                            bmodel.configurationMasterHelper.CONVERT_EOD_SIH_OU) {

                        if (bmodel.configurationMasterHelper.CONVERT_EOD_SIH_PS) {
                            loadStk.setText(stockBO.getVanLoadQty() + "");
                            soldStk.setText(stockBO.getSoldQty() + "");
                            free.setText(stockBO.getFreeIssuedQty() + "");
                            sih.setText(stockBO.getSih() + "");
                            empty.setText(stockBO.getEmptyBottleQty() + "");
                            returnQty.setText(stockBO.getReturnQty() + "");
                            replacement.setText(stockBO.getReplacementQty() + "");
                        } else if (bmodel.configurationMasterHelper.CONVERT_EOD_SIH_OU) {
                            if (stockBO.getOuterSize() != 0) {
                                loadStk.setText(SDUtil.mathRoundoff((double) stockBO.getVanLoadQty() / stockBO.getOuterSize()) + "");
                                soldStk.setText(SDUtil.mathRoundoff((double) stockBO.getSoldQty() / stockBO.getOuterSize()) + "");
                                free.setText(SDUtil.mathRoundoff((double) stockBO.getFreeIssuedQty() / stockBO.getOuterSize()) + "");
                                sih.setText(SDUtil.mathRoundoff((double) stockBO.getSih() / stockBO.getOuterSize()) + "");
                                empty.setText(SDUtil.mathRoundoff((double) stockBO.getEmptyBottleQty() / stockBO.getOuterSize()) + "");
                                returnQty.setText(SDUtil.mathRoundoff((double) stockBO.getReturnQty() / stockBO.getOuterSize()) + "");
                                replacement.setText(SDUtil.mathRoundoff((double) stockBO.getReplacementQty() / stockBO.getOuterSize()) + "");
                            } else {
                                loadStk.setText(stockBO.getVanLoadQty() + "");
                                soldStk.setText(stockBO.getSoldQty() + "");
                                free.setText(stockBO.getFreeIssuedQty() + "");
                                sih.setText(stockBO.getSih() + "");
                                empty.setText(stockBO.getEmptyBottleQty() + "");
                                returnQty.setText(stockBO.getReturnQty() + "");
                                replacement.setText(stockBO.getReplacementQty() + "");
                            }
                        } else if (bmodel.configurationMasterHelper.CONVERT_EOD_SIH_CS) {
                            if (stockBO.getCaseSize() != 0) {
                                loadStk.setText(SDUtil.mathRoundoff((double) stockBO.getVanLoadQty() / stockBO.getCaseSize()) + "");
                                soldStk.setText(SDUtil.mathRoundoff((double) stockBO.getSoldQty() / stockBO.getCaseSize()) + "");
                                free.setText(SDUtil.mathRoundoff((double) stockBO.getFreeIssuedQty() / stockBO.getCaseSize()) + "");
                                sih.setText(SDUtil.mathRoundoff((double) stockBO.getSih() / stockBO.getCaseSize()) + "");
                                empty.setText(SDUtil.mathRoundoff((double) stockBO.getEmptyBottleQty() / stockBO.getCaseSize()) + "");
                                returnQty.setText(SDUtil.mathRoundoff((double) stockBO.getReturnQty() / stockBO.getCaseSize()) + "");
                                replacement.setText(SDUtil.mathRoundoff((double) stockBO.getReplacementQty() / stockBO.getCaseSize()) + "");
                            } else {
                                loadStk.setText(stockBO.getVanLoadQty() + "");
                                soldStk.setText(stockBO.getSoldQty() + "");
                                free.setText(stockBO.getFreeIssuedQty() + "");
                                sih.setText(stockBO.getSih() + "");
                                empty.setText(stockBO.getEmptyBottleQty() + "");
                                returnQty.setText(stockBO.getReturnQty() + "");
                                replacement.setText(stockBO.getReplacementQty() + "");
                            }
                        }
                    } else if (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT) {
                        StringBuffer loadStkSB = new StringBuffer();
                        StringBuffer soldStkSB = new StringBuffer();
                        StringBuffer freeSB = new StringBuffer();
                        StringBuffer emptySB = new StringBuffer();
                        StringBuffer returnSB = new StringBuffer();
                        StringBuffer replaceSB = new StringBuffer();
                        StringBuffer sihSB = new StringBuffer();
                        String slash = "";
                        if (bmodel.configurationMasterHelper.SHOW_EOD_OC) {
                            loadStkSB.append(stockBO.getVanLoadQty_cs());
                            soldStkSB.append(stockBO.getSoldQty_cs());
                            freeSB.append(stockBO.getFreeIssuedQty_cs());
                            emptySB.append(stockBO.getEmptyBottleQty_cs());
                            returnSB.append(stockBO.getReturnQty_cs());
                            replaceSB.append(stockBO.getReplacementQty_cs());
                            sihSB.append(stockBO.getSih_cs());
                            slash = "/";
                        }
                        if (bmodel.configurationMasterHelper.SHOW_EOD_OP) {
                            loadStkSB.append(slash + stockBO.getVanLoadQty_pc());
                            soldStkSB.append(slash + stockBO.getSoldQty_pc());
                            freeSB.append(slash + stockBO.getFreeIssuedQty_pc());
                            emptySB.append(slash + stockBO.getEmptyBottleQty_pc());
                            returnSB.append(slash + stockBO.getReturnQty_pc());
                            replaceSB.append(slash + stockBO.getReplacementQty_pc());
                            sihSB.append(slash + stockBO.getSih_pc());
                            slash = "/";
                        }
                        if (bmodel.configurationMasterHelper.SHOW_EOD_OO) {
                            loadStkSB.append(slash + stockBO.getVanLoadQty_ou());
                            soldStkSB.append(slash + stockBO.getSoldQty_ou());
                            freeSB.append(slash + stockBO.getFreeIssuedQty_ou());
                            emptySB.append(slash + stockBO.getEmptyBottleQty_ou());
                            returnSB.append(slash + stockBO.getReturnQty_ou());
                            replaceSB.append(slash + stockBO.getReplacemnetQty_ou());
                            sihSB.append(slash + stockBO.getSih_ou());
                        }

                        loadStk.setText(loadStkSB.toString());
                        soldStk.setText(soldStkSB.toString());
                        free.setText(freeSB.toString());
                        sih.setText(sihSB.toString());
                        empty.setText(emptySB.toString());
                        returnQty.setText(returnSB.toString());
                        replacement.setText(replaceSB.toString());
                    } else {
                        loadStk.setText(stockBO.getVanLoadQty() + "");
                        soldStk.setText(stockBO.getSoldQty() + "");
                        free.setText(stockBO.getFreeIssuedQty() + "");
                        sih.setText(stockBO.getSih() + "");
                        empty.setText(stockBO.getEmptyBottleQty() + "");
                        returnQty.setText(stockBO.getReturnQty() + "");
                        replacement.setText(stockBO.getReplacementQty() + "");
                    }

                    if (!bmodel.configurationMasterHelper.SHOW_STOCK_EMPTY)
                        empty.setVisibility(View.GONE);
                    if (!bmodel.configurationMasterHelper.SHOW_STOCK_FREE_ISSUED)
                        free.setVisibility(View.GONE);
                    if (!bmodel.configurationMasterHelper.SHOW_STOCK_REPLACE)
                        replacement.setVisibility(View.GONE);

                    mDetailsContainerLL.addView(v);
                }

            }

        } catch (Exception e) {
            Commons.printException(e + "");
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
            return true;
        } else if (i == R.id.menu_print) {
            if (!isPrintClicked) {
                isPrintClicked = true;
                if (getSupportActionBar() != null)
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                new Thread(new Runnable() {
                    public void run() {
                        Looper.prepare();
                        doConnection(ZEBRA_3INCH);
                        Looper.loop();
                        Looper myLooper = Looper.myLooper();
                        if (myLooper != null)
                            myLooper.quit();
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

    public Handler getHandler() {
        return mHandler;
    }

    /**
     * printing invoice
     **/
    private void printInvoice(String printername) {
        try {
            if (printername.equals(ZEBRA_2INCH)) {
                for (int i = 0; i < SDUtil.convertToInt(count); i++)
                    zebraPrinterConnection.write(printDatafor2inchprinter());

            } else if (printername.equals(ZEBRA_3INCH)) {

                for (int i = 0; i < SDUtil.convertToInt(count); i++) {
                    zebraPrinterConnection.write(printDatafor3inchprinter());
                }

                bmodel.showAlert(
                        getResources().getString(R.string.printed_successfully),
                        5001);
            } else if (printername.equals(ZEBRA_4INCH)) {

                for (int i = 0; i < SDUtil.convertToInt(count); i++) {
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
            Commons.printException(e + "");
            setStatus(e.getMessage(), Color.RED);
            bmodel.showAlert(
                    getResources().getString(
                            R.string.error), 5001);
        } catch (Exception e) {
            Commons.printException(e + "");
            bmodel.showAlert(
                    getResources().getString(
                            R.string.error), 5001);
        } finally {
            disconnect();
        }
    }

    private void disconnect() {
        try {
            setStatus("Disconnecting", Color.RED);

            if (zebraPrinterConnection != null) {
                zebraPrinterConnection.close();
            }
            setStatus("Not Connected", Color.RED);
        } catch (ConnectionException e) {
            setStatus(getResources().getString(R.string.printer_not_connected),
                    Color.RED);
            Commons.printException(e + "");
        }
    }

    private ZebraPrinter connect() {
        zebraPrinterConnection = null;
        zebraPrinterConnection = new BluetoothConnection(
                getMacAddressFieldText());
        SettingsHelper.saveBluetoothAddress(this, getMacAddressFieldText());

        Commons.print(TAG + "PRINT MAC : " + getMacAddressFieldText());
        try {
            zebraPrinterConnection.open();
            mStatusIV.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),
                    R.drawable.greenball));
            setStatus("Connected", Color.GREEN);
        } catch (ConnectionException e) {

            setStatus(getResources().getString(R.string.printer_not_connected),
                    Color.RED);
            Commons.printException(e + "");

            DemoSleeper.sleep(1000);
            disconnect();
        } catch (Exception e) {

            setStatus(getResources().getString(R.string.printer_not_connected),
                    Color.RED);
            Commons.printException(e + "");
        }

        ZebraPrinter printer = null;

        if (zebraPrinterConnection.isConnected()) {

            try {
                printer = ZebraPrinterFactory.getInstance(PrinterLanguage.CPCL,
                        zebraPrinterConnection);

                setStatus("Determining Printer Language", Color.YELLOW);
                PrinterLanguage pl = printer.getPrinterControlLanguage();
                setStatus("Printer Language " + pl, Color.BLUE);
                Commons.print(TAG + "PRINT LANGUAGE : " + pl);
            } catch (ConnectionException e) {
                setStatus("PrinterConnectionException", Color.RED);
                Commons.print(TAG
                        + "PRINT LANGUAGE : UNKNOWN : PrinterConnectionException");
                Commons.printException(e + "");
            }
        }

        return printer;
    }

    private void doConnection(String printername) {
        try {
            ZebraPrinter printer = connect();
            if (printer != null) {
                printInvoice(printername);
            } else {
                bmodel.showAlert(
                        getResources().getString(
                                R.string.error), 5001);
                disconnect();
            }
        } catch (Exception e) {
            bmodel.showAlert(
                    getResources().getString(
                            R.string.error), 5001);
            Commons.printException(e + "");
        }
    }

    private final Handler mHandler = new Handler(new Handler.Callback() {
        ProgressDialog pd;

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
                                        + OrderHelper.getInstance(EODStockReportPreviewScreen.this).getOrderId(),
                                DataMembers.NOTIFY_ORDER_SAVED);
                    } catch (Exception e) {
                        Commons.printException(e + "");
                    }
                    return true;

                case DataMembers.NOTIFY_INVOICE_SAVED:
                    try {
                        if (pd != null)
                            pd.dismiss();
                        bmodel = (BusinessModel) getApplicationContext();
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
                        Commons.printException("Save Invoice Error :" + e.toString());
                    }
                    return true;
                case DataMembers.NOTIFY_PRINT:
                    try {
                        new Thread(new Runnable() {
                            public void run() {
                                Looper.prepare();
                                doConnection(ZEBRA_3INCH);
                                Looper.loop();
                                Looper myLooper = Looper.myLooper();
                                if (myLooper != null)
                                    myLooper.quit();
                            }
                        }).start();
                    } catch (Exception e) {
                        Commons.printException(e + "");
                    }
                    return true;
                default:
                    break;
            }
            return false;
        }
    });

    private void setStatus(final String statusMessage, final int color) {
        try {
            Commons.print(TAG + statusMessage);
            runOnUiThread(new Runnable() {
                public void run() {
                    statusField.setBackgroundColor(color);
                    statusField.setText(statusMessage);
                }
            });
            DemoSleeper.sleep(1000);
        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }

    private byte[] printDatafor2inchprinter() {
        return new byte[0];
    }

    private byte[] printDatafor3inchprinter() {
        byte[] PrintDataBytes = null;
        try {

            PrinterLanguage printerLanguage = PrinterLanguage.CPCL;
            // 00:22:58:3D:7E:83 - RW420
            // AC:3F:A4:16:B9:AE - IMZ320
            if (printerLanguage == PrinterLanguage.CPCL) {

                if (bmodel.configurationMasterHelper.IS_SHOW_PRINT_LANGUAGE_THAI) {
                    return printThai();
                }

                int height;
                int mHeight = 0;
                if (bmodel.configurationMasterHelper.SHOW_PRINT_HEADERS) {
                    mHeight = 120;
                }
                int x = 250 + mHeight;
                if (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT)
                    x += 50;

                Commons.print(TAG + ",Print Details Count :" + mPrintDetailsCount);

                //height logic is changed for blank paper issue 16-03-2015 --chiru
                if (mPrintDetailsCount < 5) {
                    height = x + 100 + mPrintDetailsCount * 100;
                } else if (mPrintDetailsCount > 5 && mPrintDetailsCount < 10) {
                    height = x + mPrintDetailsCount * 100;
                } else {
                    height = x + mPrintDetailsCount * 75;
                }
                Commons.print(TAG + "Heigt:" + height);
                String Printitem = "! 0 200 200 " + height + " 1\r\n"
                        + "CENTER\r\n";
                int heightlenth;
                int heightspace = 30;
                if (bmodel.configurationMasterHelper.SHOW_PRINT_HEADERS) {
                    heightlenth = 90;
                    Printitem += ("T 7 0 30 " + heightlenth + "Unipal General Trading Company\n\r\n");
                    heightlenth = heightlenth + heightspace;
                    Printitem += ("T 7 0 60 " + heightlenth + "VAT No  : 562414227 \r\n");
                    heightlenth = heightlenth + heightspace;
//
                    Printitem += ("T 7 0 10 " + heightlenth + "Ramallah - Industrial zone, Tel: +972 2 2981060 \r\n");
                    heightlenth = heightlenth + heightspace;
                    Printitem += ("T 7 0 10 " + heightlenth + "Gaza - lndus. Zone - Carny, Tel: +972 7 2830324\n \r\n");

                } else {
                    heightlenth = 60;
                }

                heightlenth = heightlenth + heightspace;
                Printitem += "T 5 1 10 " + (heightlenth)
                        + " "
                        + bmodel.userMasterHelper.getUserMasterBO()
                        .getDistributorName() + "\r\n";
                heightlenth = heightlenth + 50;


                Printitem += "T 5 0 10 " + (heightlenth)
                        + " "
                        + bmodel.userMasterHelper.getUserMasterBO()
                        .getDistributorContactNumber() + "\r\n";

                heightlenth = heightlenth + heightspace;
                Printitem += "T 5 0 10 " + (heightlenth)
                        + getResources().getString(R.string.musername) + "" + bmodel.userMasterHelper.getUserMasterBO()
                        .getUserName() + "\r\n";
                heightlenth = heightlenth + heightspace;
                Printitem += "T 5 0 10 " + (heightlenth)
                        + " " + DateTimeUtils.now(DateTimeUtils.DATE_TIME)
                        + "\r\n";
                heightlenth = heightlenth + heightspace;
                // T- Text // // Font Size // Spacing // height between lines

                Printitem += "T 5 0 5 " + (heightlenth) + " --------------------------------------------------\r\n";
                heightlenth = heightlenth + heightspace;
                Printitem += "\r\n";
                Printitem += "LEFT \r\n";
                Printitem += "T 5 0 5 " + heightlenth
                        + getResources().getString(R.string.product_name)
                        + "\r\n";
                String caseOrPieceOrOuter = "";
                String slash = "";
                if (bmodel.configurationMasterHelper.SHOW_EOD_OC) {
                    caseOrPieceOrOuter = "c";
                    slash = "/";
                }
                if (bmodel.configurationMasterHelper.SHOW_EOD_OP) {
                    caseOrPieceOrOuter = caseOrPieceOrOuter + slash + "p";
                    slash = "/";
                }
                if (bmodel.configurationMasterHelper.SHOW_EOD_OO) {
                    caseOrPieceOrOuter = caseOrPieceOrOuter + slash + "o";
                }

                boolean isAdditionalColumns = false;
                int tempColumns;
                if (bmodel.configurationMasterHelper.SHOW_STOCK_FREE_ISSUED || bmodel.configurationMasterHelper.SHOW_STOCK_EMPTY || bmodel.configurationMasterHelper.SHOW_STOCK_REPLACE)
                    isAdditionalColumns = true;
                int widthLenth;
                if (isAdditionalColumns) {
                    widthLenth = 35;
                    heightlenth += 30;

                    //For alignment
                    if (!bmodel.configurationMasterHelper.SHOW_STOCK_FREE_ISSUED || !bmodel.configurationMasterHelper.SHOW_STOCK_EMPTY || !bmodel.configurationMasterHelper.SHOW_STOCK_REPLACE) {
                        tempColumns = 0;
                        if (bmodel.configurationMasterHelper.SHOW_STOCK_FREE_ISSUED)
                            tempColumns += 1;
                        if (bmodel.configurationMasterHelper.SHOW_STOCK_EMPTY)
                            tempColumns += 1;
                        if (bmodel.configurationMasterHelper.SHOW_STOCK_REPLACE)
                            tempColumns += 1;

                        if (tempColumns == 1)
                            widthLenth += 120;
                        else if (tempColumns == 2)
                            widthLenth += 50;
                    }

                    int tempHeight = 0;// temp height used to add new line for 'c/p/o'
                    if (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT) {
                        tempHeight = heightlenth + 20;
                    }

                    Printitem += "T 5 0 " + widthLenth + " " + heightlenth + "Load" + "\r\n";
                    if (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT) {
                        Printitem += "T 5 0 " + widthLenth + " " + tempHeight + " " + caseOrPieceOrOuter + "\r\n";
                    }
                    widthLenth += 115;

                    Printitem += "T 5 0 " + widthLenth + " " + heightlenth + "Sales" + "\r\n";
                    if (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT) {
                        Printitem += "T 5 0 " + widthLenth + " " + tempHeight + " " + caseOrPieceOrOuter + "\r\n";
                    }
                    widthLenth += 70;

                    if (bmodel.configurationMasterHelper.SHOW_STOCK_FREE_ISSUED) {
                        Printitem += "T 5 0 " + widthLenth + "  " + heightlenth + "FI" + "\r\n";
                        if (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT) {
                            Printitem += "T 5 0 " + widthLenth + " " + tempHeight + " " + caseOrPieceOrOuter + "\r\n";
                        }
                        widthLenth += 70;
                    }


                    Printitem += "T 5 0 " + widthLenth + " " + heightlenth
                            + getResources().getString(R.string.sih) + "\r\n";
                    if (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT) {
                        Printitem += "T 5 0 " + widthLenth + " " + tempHeight + " " + caseOrPieceOrOuter + "\r\n";
                    }
                    widthLenth += 100;

                    if (bmodel.configurationMasterHelper.SHOW_STOCK_EMPTY) {
                        Printitem += "T 5 0 " + widthLenth + " " + heightlenth
                                + "Emp" + "\r\n";
                        if (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT) {
                            Printitem += "T 5 0 " + widthLenth + " " + tempHeight + " " + caseOrPieceOrOuter + "\r\n";
                        }
                        widthLenth += 70;
                    }

                    Printitem += "T 5 0 " + widthLenth + " " + heightlenth
                            + "Ret" + "\r\n";
                    if (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT) {
                        Printitem += "T 5 0 " + widthLenth + " " + tempHeight + " " + caseOrPieceOrOuter + "\r\n";
                    }
                    widthLenth += 60;

                    if (bmodel.configurationMasterHelper.SHOW_STOCK_REPLACE) {
                        Printitem += "T 5 0 " + widthLenth + " " + heightlenth
                                + "Rep" + "\r\n";
                        if (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT) {
                            Printitem += "T 5 0 " + widthLenth + " " + tempHeight + " " + caseOrPieceOrOuter + "\r\n";
                        }
                    }

                    if (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT) {
                        heightlenth = tempHeight;
                    }


                } else {
                    Printitem += "T 5 0 200 " + heightlenth + "Load" + "\r\n";
                    Printitem += "T 5 0 300 " + heightlenth + "Sales" + "\r\n";

                    Printitem += "T 5 0 380 " + heightlenth
                            + getResources().getString(R.string.sih) + "\r\n";

                    Printitem += "T 5 0 480 " + heightlenth
                            + "Ret" + "\r\n";

                    if (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT) {
                        heightlenth += 20;

                        Printitem += "T 5 0 200 " + heightlenth + " " + caseOrPieceOrOuter + "\r\n";
                        Printitem += "T 5 0 300 " + heightlenth + " " + caseOrPieceOrOuter + "\r\n";
                        Printitem += "T 5 0 380 " + heightlenth + " " + caseOrPieceOrOuter + "\r\n";
                        Printitem += "T 5 0 480 " + heightlenth + " " + caseOrPieceOrOuter + "\r\n";
                    }
                }


                Printitem += "\r\n";

                heightlenth = heightlenth + 20;
                Printitem += "T 5 0 10 " + heightlenth + " --------------------------------------------------\r\n";

                Printitem += "\r\n";
                x += 50;

                for (StockReportBO stockBO : mDetails) {
                    if (stockBO.getVanLoadQty() > 0 || stockBO.getEmptyBottleQty() > 0 || stockBO.getFreeIssuedQty() > 0
                            || stockBO.getSoldQty() > 0 || stockBO.getReplacementQty() > 0 || stockBO.getReturnQty() > 0) {

                        x += 20;
                        Printitem += "T 5 0 5 " + x + " "
                                + stockBO.getProductName().toLowerCase().substring(0, 25)
                                + "\r\n";
                        if (stockBO.getBatchNo() != null && !stockBO.getBatchNo().equals("")) {

                            Printitem += "T 5 0 5 " + (x + 30) + " "
                                    + stockBO.getBatchNo().toLowerCase()
                                    + "\r\n";
                        }
                        x += 30;

                        StringBuffer loadStkSB = new StringBuffer();
                        StringBuffer soldStkSB = new StringBuffer();
                        StringBuffer freeSB = new StringBuffer();
                        StringBuffer emptySB = new StringBuffer();
                        StringBuffer returnSB = new StringBuffer();
                        StringBuffer replaceSB = new StringBuffer();
                        StringBuffer sihSB = new StringBuffer();
                        if (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT) {

                            slash = "";
                            if (bmodel.configurationMasterHelper.SHOW_EOD_OC) {
                                loadStkSB.append(stockBO.getVanLoadQty_cs());
                                soldStkSB.append(stockBO.getSoldQty_cs());
                                freeSB.append(stockBO.getFreeIssuedQty_cs());
                                emptySB.append(stockBO.getEmptyBottleQty_cs());
                                returnSB.append(stockBO.getReturnQty_cs());
                                replaceSB.append(stockBO.getReplacementQty_cs());
                                sihSB.append(stockBO.getSih_cs());
                                slash = "/";
                            }
                            if (bmodel.configurationMasterHelper.SHOW_EOD_OP) {
                                loadStkSB.append(slash + stockBO.getVanLoadQty_pc());
                                soldStkSB.append(slash + stockBO.getSoldQty_pc());
                                freeSB.append(slash + stockBO.getFreeIssuedQty_pc());
                                emptySB.append(slash + stockBO.getEmptyBottleQty_pc());
                                returnSB.append(slash + stockBO.getReturnQty_pc());
                                replaceSB.append(slash + stockBO.getReplacementQty_pc());
                                sihSB.append(slash + stockBO.getSih_pc());
                                slash = "/";
                            }
                            if (bmodel.configurationMasterHelper.SHOW_EOD_OO) {
                                loadStkSB.append(slash + stockBO.getVanLoadQty_ou());
                                soldStkSB.append(slash + stockBO.getSoldQty_ou());
                                freeSB.append(slash + stockBO.getFreeIssuedQty_ou());
                                emptySB.append(slash + stockBO.getEmptyBottleQty_ou());
                                returnSB.append(slash + stockBO.getReturnQty_ou());
                                replaceSB.append(slash + stockBO.getReplacemnetQty_ou());
                                sihSB.append(slash + stockBO.getSih_ou());
                            }
                        }


                        if (bmodel.configurationMasterHelper.SHOW_STOCK_FREE_ISSUED || bmodel.configurationMasterHelper.SHOW_STOCK_EMPTY || bmodel.configurationMasterHelper.SHOW_STOCK_REPLACE) {
                            widthLenth = 35;

                            //For alignment
                            if (!bmodel.configurationMasterHelper.SHOW_STOCK_FREE_ISSUED || !bmodel.configurationMasterHelper.SHOW_STOCK_EMPTY || !bmodel.configurationMasterHelper.SHOW_STOCK_REPLACE) {
                                tempColumns = 0;
                                if (bmodel.configurationMasterHelper.SHOW_STOCK_FREE_ISSUED)
                                    tempColumns += 1;
                                if (bmodel.configurationMasterHelper.SHOW_STOCK_EMPTY)
                                    tempColumns += 1;
                                if (bmodel.configurationMasterHelper.SHOW_STOCK_REPLACE)
                                    tempColumns += 1;

                                if (tempColumns == 1)
                                    widthLenth += 120;
                                else if (tempColumns == 2)
                                    widthLenth += 50;

                            }

                            Printitem += "T 5 0 " + widthLenth + " " + x + " "
                                    + (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT ? (loadStkSB.toString()) : stockBO.getVanLoadQty()) + "\r\n";
                            Printitem += "\r\n";
                            widthLenth += 115;

                            Printitem += "T 5 0 " + widthLenth + " " + x + " "
                                    + (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT ? soldStkSB.toString() : stockBO.getSoldQty()) + "\r\n";
                            widthLenth += 70;

                            if (bmodel.configurationMasterHelper.SHOW_STOCK_FREE_ISSUED) {
                                Printitem += "T 5 0 " + widthLenth + " " + x + " "
                                        + (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT ? freeSB.toString() : stockBO.getFreeIssuedQty()) + "\r\n";
                                widthLenth += 70;
                            }


                            Printitem += "T 5 0 " + widthLenth + " " + x + " "
                                    + (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT ? sihSB.toString() : stockBO.getSih()) + "\r\n";
                            widthLenth += 100;

                            if (bmodel.configurationMasterHelper.SHOW_STOCK_EMPTY) {
                                Printitem += "T 5 0 " + widthLenth + " " + x + " "
                                        + (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT ? emptySB.toString() : stockBO.getEmptyBottleQty()) + "\r\n";
                                widthLenth += 70;
                            }

                            Printitem += "T 5 0 " + widthLenth + " " + x + " "
                                    + (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT ? returnSB.toString() : stockBO.getReturnQty()) + "\r\n";
                            widthLenth += 60;

                            if (bmodel.configurationMasterHelper.SHOW_STOCK_REPLACE) {
                                Printitem += "T 5 0 " + widthLenth + " " + x + " "
                                        + (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT ? replaceSB : stockBO.getReplacementQty()) + "\r\n";
                            }
                        } else {
                            Printitem += "T 5 0 200 " + x + " "
                                    + (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT ? loadStkSB.toString() : stockBO.getVanLoadQty()) + "\r\n";
                            Printitem += "\r\n";

                            Printitem += "T 5 0 300 " + x + " "
                                    + (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT ? soldStkSB.toString() : stockBO.getSoldQty()) + "\r\n";

                            Printitem += "T 5 0 380 " + x + " "
                                    + (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT ? sihSB.toString() : stockBO.getSih()) + "\r\n";
                            Printitem += "RIGHT \r\n";
                            Printitem += "T 5 0 480 " + x + " "
                                    + (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT ? returnSB.toString() : stockBO.getReturnQty()) + "\r\n";

                        }
                        x += 10;
                    }

                }

                x += 30;
                Printitem += "T 5 0 10 "
                        + x
                        + " --------------------------------------------------\r\n";

                x += 50;

                Printitem += "\r\n";
                Printitem += "\r\n";
                Printitem += "\r\n";

                Printitem += "T 5 0 10 " + x + "Customer Sign" + "\r\n";
                Printitem += "T 5 0 180 " + x + " --------\r\n";

                Printitem += "T 5 0 330 " + x + "Rep. Sign" + "\r\n";
                Printitem += "T 5 0 440 " + x + "--------\r\n";

                Printitem += "PRINT\r\n";

                PrintDataBytes = Printitem.getBytes();
            }
        } catch (Exception e) {
            Commons.printException(e + " ");
        }
        return PrintDataBytes;
    }

    private byte[] printDatafor4inchprinter() {
        byte[] PrintDataBytes = null;
        try {

            PrinterLanguage printerLanguage = PrinterLanguage.CPCL;
            // 00:22:58:3D:7E:83
            // AC:3F:A4:16:B9:AE
            if (printerLanguage == PrinterLanguage.CPCL) {
                int height;
                int x = 280;
                height = x + mDetails.size() * 350 + 600;
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

                Printitem += "T 5 0 20 220 "
                        + getResources().getString(R.string.product_name)
                        + "\r\n";
                Printitem += "T 5 0 230 220 "
                        + getResources().getString(R.string.loading_stock)
                        + "\r\n";
                Printitem += "T 5 0 400 220 "
                        + getResources().getString(R.string.sold_stock)
                        + "\r\n";
                if (bmodel.configurationMasterHelper.SHOW_STOCK_FREE_ISSUED)
                    Printitem += "T 5 0 480 220 "
                            + getResources().getString(R.string.free_issued)
                            + "\r\n";
                if (bmodel.configurationMasterHelper.SHOW_STOCK_FREE_ISSUED)
                    Printitem += "T 5 0 580 220 "
                            + getResources().getString(R.string.sih) + "\r\n";
                else
                    Printitem += "T 5 0 480 220 "
                            + getResources().getString(R.string.sih) + "\r\n";
                if (bmodel.configurationMasterHelper.SHOW_STOCK_EMPTY)
                    Printitem += "T 5 0 700 220 "
                            + getResources().getString(R.string.empty) + "\r\n";

                Printitem += "T 5 0 10 260 --------------------------------------------------\r\n";

                for (StockReportBO productBO : mDetails) {

                    x += 20;
                    Printitem += "T 5 0 20 " + x + " "
                            + productBO.getProductCode() + "\r\n";
                    Printitem += "T 5 0 230 " + x + " "
                            + productBO.getVanLoadQty() + "\r\n";
                    Printitem += "\r\n";

                    x += 30;
                    Printitem += "T 5 0 430 " + x + " "
                            + productBO.getSoldQty() + "\r\n";
                    if (bmodel.configurationMasterHelper.SHOW_STOCK_FREE_ISSUED)
                        Printitem += "T 5 0 500 " + x + " "
                                + productBO.getFreeIssuedQty() + "\r\n";
                    if (bmodel.configurationMasterHelper.SHOW_STOCK_FREE_ISSUED)
                        Printitem += "T 5 0 580 " + x + " " + productBO.getSih()
                                + "\r\n";
                    else
                        Printitem += "T 5 0 500 " + x + " " + productBO.getSih()
                                + "\r\n";

                    Printitem += "RIGHT \r\n";
                    if (bmodel.configurationMasterHelper.SHOW_STOCK_EMPTY)
                        Printitem += "T 5 0 690 " + x + " "
                                + productBO.getEmptyBottleQty() + "\r\n";
                    x += 10;

                }
                x += 30;
                Printitem += "T 5 0 180 " + x + "Total Cash" + "\r\n";

                Printitem += "RIGHT \r\n";
                Printitem += "T 5 0 350 " + x + " " + bmodel.formatValue(0)
                        + "\r\n";
                x += 30;
                Printitem += "T 5 0 180 " + x + "Total Cheque" + "\r\n";

                Printitem += "RIGHT \r\n";
                Printitem += "T 5 0 350 " + x + " " + bmodel.formatValue(0)
                        + "\r\n";

                x += 30;
                Printitem += "T 5 0 180 " + x + "Total Collected" + "\r\n";

                Printitem += "RIGHT \r\n";
                Printitem += "T 5 0 350 " + x + " " + bmodel.formatValue(0)
                        + "\r\n";

                x += 30;
                Printitem += "T 5 0 10 "
                        + x
                        + " --------------------------------------------------\r\n";

                x += 50;

                Printitem += "\r\n";
                Printitem += "\r\n";
                Printitem += "\r\n";

                Printitem += "T 5 0 20 " + x + "Cusotmer Sign" + "\r\n";
                Printitem += "T 5 0 200 " + x + " -------------\r\n";

                Printitem += "T 5 0 480 " + x + "Rep. Sign" + "\r\n";
                Printitem += "T 5 0 600 " + x + "------------\r\n";

                Printitem += "PRINT\r\n";

                PrintDataBytes = Printitem.getBytes();
            }
        } catch (Exception e) {
            Commons.printException(e + " ");
        }
        return PrintDataBytes;
    }

    private String getMacAddressFieldText() {
        String macAddress = null;
        try {
            macAddress = mMacAddressET.getText().toString().trim();

            SharedPreferences pref = this.getSharedPreferences("PRINT",
                    MODE_PRIVATE);
            Editor editor = pref.edit();
            editor.putString("MAC", macAddress);
            editor.apply();
        } catch (Exception e) {
            Commons.printException(e + "");
        }
        return macAddress;
    }

    @Override
    public Dialog onCreateDialog(int id) {
        switch (id) {
            case SELECTED_PRINTER_DIALOG:
                AlertDialog.Builder builder2 = new AlertDialog.Builder(
                        EODStockReportPreviewScreen.this)
                        .setTitle("Choose Printer").setSingleChoiceItems(
                                mPrinterSelectionArray, -1,
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {

                                        // dismissing the dialog when the user makes
                                        // a selection.
                                        mSelectedPrinterName = mPrinterSelectionArray[which];
                                        dialog.dismiss();
                                        new Thread(new Runnable() {
                                            public void run() {
                                                Looper.prepare();

                                                doConnection(mSelectedPrinterName);
                                                Looper.loop();
                                                Looper myLooper = Looper.myLooper();
                                                if (myLooper != null)
                                                    myLooper.quit();
                                            }
                                        }).start();
                                    }
                                });
                bmodel.applyAlertDialogTheme(builder2);
                break;
            default:
                break;
        }
        return null;

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
                    Commons.printException(e + " ");
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindDrawables(findViewById(R.id.root));
    }

    private byte[] printThai(){
        byte[] printDataBytes = null;
        int x = 0;
        try {
            StringBuilder tempsb = new StringBuilder();
            tempsb.append("! U1 SETLP ANG12PT.CPF 0 34 \n");

            tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "CENTER\r\n");
            tempsb.append("SETBOLD 1 \r\n");
            if(bmodel.configurationMasterHelper.SHOW_PRINT_HEADERS) {
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
                        .getDistributorContactNumber() + "\r\n");
                tempsb.append("SETBOLD 0 \r\n");
                tempsb.append("PRINT\r\n");

                tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "CENTER\r\n");
                tempsb.append("SETBOLD 1 \r\n");
                tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 "
                        + getResources().getString(R.string.musername) + "" + bmodel.userMasterHelper.getUserMasterBO()
                        .getUserName() + "\r\n");
                tempsb.append("SETBOLD 0 \r\n");
                tempsb.append("PRINT\r\n");
            }

            tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
            tempsb.append("SETBOLD 1 \r\n");
            tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 "+ "---------------------------------------------------------------------------\r\n");
            tempsb.append("SETBOLD 0 \r\n");
            tempsb.append("PRINT\r\n");

            tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
            tempsb.append("SETBOLD 1 \r\n");
            tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 "
                    + getResources().getString(R.string.product_name)+ "\r\n");
            tempsb.append("SETBOLD 0 \r\n");
            tempsb.append("PRINT\r\n");

            String caseOrPieceOrOuter="";
            String slash="";
            if(bmodel.configurationMasterHelper.SHOW_EOD_OC){
                caseOrPieceOrOuter="c";
                slash="/";
            }
            if(bmodel.configurationMasterHelper.SHOW_EOD_OP){
                caseOrPieceOrOuter=caseOrPieceOrOuter+slash+"p";
                slash="/";
            }
            if(bmodel.configurationMasterHelper.SHOW_EOD_OO){
                caseOrPieceOrOuter=caseOrPieceOrOuter+slash+"o";
            }

            if (bmodel.configurationMasterHelper.SHOW_STOCK_FREE_ISSUED || bmodel.configurationMasterHelper.SHOW_STOCK_EMPTY || bmodel.configurationMasterHelper.SHOW_STOCK_REPLACE){

                x += 40;
                tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                tempsb.append("SETBOLD 1 \r\n");
                tempsb.append("TEXT ANG12PT.CPF 0 " + x + " 1 "
                        + "Load" + "\r\n");
                tempsb.append("SETBOLD 0 \r\n");
                tempsb.append("PRINT");

                x += 100;
                tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                tempsb.append("SETBOLD 1 \r\n");
                tempsb.append("TEXT ANG12PT.CPF 0 " + x + " 1 "
                        + "Sales" + "\r\n");
                tempsb.append("SETBOLD 0 \r\n");
                tempsb.append("PRINT");

                if (bmodel.configurationMasterHelper.SHOW_STOCK_FREE_ISSUED) {
                    x += 90;
                    tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                    tempsb.append("SETBOLD 1 \r\n");
                    tempsb.append("TEXT ANG12PT.CPF 0 " + x + " 1 "
                            + "FI" + "\r\n");
                    tempsb.append("SETBOLD 0 \r\n");
                    tempsb.append("PRINT");
                }


                x += 90;
                tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                tempsb.append("SETBOLD 1 \r\n");
                tempsb.append("TEXT ANG12PT.CPF 0 " + x + " 1 "
                        + getResources().getString(R.string.sih) + "\r\n");
                tempsb.append("SETBOLD 0 \r\n");
                tempsb.append("PRINT");

                if (bmodel.configurationMasterHelper.SHOW_STOCK_EMPTY) {
                    x += 60;
                    tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                    tempsb.append("SETBOLD 1 \r\n");
                    tempsb.append("TEXT ANG12PT.CPF 0 " + x + " 1 "
                            + "Emp" + "\r\n");
                    tempsb.append("SETBOLD 0 \r\n");
                    tempsb.append("PRINT");
                }

                x += 60;
                tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                tempsb.append("SETBOLD 1 \r\n");
                tempsb.append("TEXT ANG12PT.CPF 0 " + x + " 1 "
                        + "Ret" + "\r\n");
                tempsb.append("SETBOLD 0 \r\n");
                tempsb.append("PRINT");

                if (bmodel.configurationMasterHelper.SHOW_STOCK_REPLACE) {
                    x += 60;
                    tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                    tempsb.append("SETBOLD 1 \r\n");
                    tempsb.append("TEXT ANG12PT.CPF 0 " + x + " 1 "
                            + "Rep" + "\r\n");
                    tempsb.append("SETBOLD 0 \r\n");
                    tempsb.append("PRINT\r\n");
                }else {
                    tempsb.append("\r\n");
                }

                if (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT) {
                    x = 0;
                    x += 40;
                    tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                    tempsb.append("SETBOLD 1 \r\n");
                    tempsb.append("TEXT ANG12PT.CPF 0 " + x + " 1 "
                            + caseOrPieceOrOuter + "\r\n");
                    tempsb.append("SETBOLD 0 \r\n");
                    tempsb.append("PRINT");

                    x += 100;
                    tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                    tempsb.append("SETBOLD 1 \r\n");
                    tempsb.append("TEXT ANG12PT.CPF 0 " + x + " 1 "
                            + caseOrPieceOrOuter + "\r\n");
                    tempsb.append("SETBOLD 0 \r\n");
                    tempsb.append("PRINT");

                    if (bmodel.configurationMasterHelper.SHOW_STOCK_FREE_ISSUED) {
                        x += 90;
                        tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                        tempsb.append("SETBOLD 1 \r\n");
                        tempsb.append("TEXT ANG12PT.CPF 0 " + x + " 1 "
                                + caseOrPieceOrOuter + "\r\n");
                        tempsb.append("SETBOLD 0 \r\n");
                        tempsb.append("PRINT");
                    }

                    x += 90;
                    tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                    tempsb.append("SETBOLD 1 \r\n");
                    tempsb.append("TEXT ANG12PT.CPF 0 " + x + " 1 "
                            + caseOrPieceOrOuter + "\r\n");
                    tempsb.append("SETBOLD 0 \r\n");
                    tempsb.append("PRINT");

                    if (bmodel.configurationMasterHelper.SHOW_STOCK_EMPTY) {
                        x += 60;
                        tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                        tempsb.append("SETBOLD 1 \r\n");
                        tempsb.append("TEXT ANG12PT.CPF 0 " + x + " 1 "
                                + caseOrPieceOrOuter + "\r\n");
                        tempsb.append("SETBOLD 0 \r\n");
                        tempsb.append("PRINT");
                    }

                    x += 60;
                    tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                    tempsb.append("SETBOLD 1 \r\n");
                    tempsb.append("TEXT ANG12PT.CPF 0 " + x + " 1 "
                            + caseOrPieceOrOuter + "\r\n");
                    tempsb.append("SETBOLD 0 \r\n");
                    tempsb.append("PRINT");

                    if (bmodel.configurationMasterHelper.SHOW_STOCK_REPLACE) {
                        x += 60;
                        tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                        tempsb.append("SETBOLD 1 \r\n");
                        tempsb.append("TEXT ANG12PT.CPF 0 " + x + " 1 "
                                + caseOrPieceOrOuter + "\r\n");
                        tempsb.append("SETBOLD 0 \r\n");
                        tempsb.append("PRINT\r\n");
                    }else {
                        tempsb.append("\r\n");
                    }
                }

            } else {
                x += 260;
                tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                tempsb.append("SETBOLD 1 \r\n");
                tempsb.append("TEXT ANG12PT.CPF 0 " + x + " 1 "
                        + "Load" + "\r\n");
                tempsb.append("SETBOLD 0 \r\n");
                tempsb.append("PRINT");

                x += 80;
                tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                tempsb.append("SETBOLD 1 \r\n");
                tempsb.append("TEXT ANG12PT.CPF 0 " + x + " 1 "
                        + "Sales" + "\r\n");
                tempsb.append("SETBOLD 0 \r\n");
                tempsb.append("PRINT");

                x += 70;
                tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                tempsb.append("SETBOLD 1 \r\n");
                tempsb.append("TEXT ANG12PT.CPF 0 " + x + " 1 "
                        + getResources().getString(R.string.sih) + "\r\n");
                tempsb.append("SETBOLD 0 \r\n");
                tempsb.append("PRINT");

                x += 60;
                tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                tempsb.append("SETBOLD 1 \r\n");
                tempsb.append("TEXT ANG12PT.CPF 0 " + x + " 1 "
                        + "Ret" + "\r\n");
                tempsb.append("SETBOLD 0 \r\n");
                tempsb.append("PRINT\r\n");

                if (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT) {
                    x = 0;
                    x += 260;
                    tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                    tempsb.append("SETBOLD 1 \r\n");
                    tempsb.append("TEXT ANG12PT.CPF 0 " + x + " 1 "
                            + caseOrPieceOrOuter + "\r\n");
                    tempsb.append("SETBOLD 0 \r\n");
                    tempsb.append("PRINT");

                    x += 80;
                    tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                    tempsb.append("SETBOLD 1 \r\n");
                    tempsb.append("TEXT ANG12PT.CPF 0 " + x + " 1 "
                            + caseOrPieceOrOuter + "\r\n");
                    tempsb.append("SETBOLD 0 \r\n");
                    tempsb.append("PRINT");

                    x += 70;
                    tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                    tempsb.append("SETBOLD 1 \r\n");
                    tempsb.append("TEXT ANG12PT.CPF 0 " + x + " 1 "
                            + caseOrPieceOrOuter + "\r\n");
                    tempsb.append("SETBOLD 0 \r\n");
                    tempsb.append("PRINT");

                    x += 60;
                    tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                    tempsb.append("SETBOLD 1 \r\n");
                    tempsb.append("TEXT ANG12PT.CPF 0 " + x + " 1 "
                            + caseOrPieceOrOuter + "\r\n");
                    tempsb.append("SETBOLD 0 \r\n");
                    tempsb.append("PRINT\r\n");
                }
            }

            tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
            tempsb.append("SETBOLD 1 \r\n");
            tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 "+ "---------------------------------------------------------------------------\r\n");
            tempsb.append("SETBOLD 0 \r\n");
            tempsb.append("PRINT\r\n");

            for (StockReportBO stockBO : mDetails) {
                if (stockBO.getVanLoadQty() > 0 || stockBO.getEmptyBottleQty() > 0||stockBO.getFreeIssuedQty()>0
                        ||stockBO.getSoldQty()>0||stockBO.getReplacementQty()>0||stockBO.getReturnQty()>0) {

                    tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                    tempsb.append("SETBOLD 1 \r\n");
                    tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 "
                            + stockBO.getProductName().toLowerCase() + "\r\n");
                    tempsb.append("SETBOLD 0 \r\n");
                    tempsb.append("PRINT");

                    if(stockBO.getBatchNo()!=null&&!stockBO.getBatchNo().equals("")){
                        tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                        tempsb.append("SETBOLD 1 \r\n");
                        tempsb.append("TEXT ANG12PT.CPF 0 " + 400 + " 1 "
                                + stockBO.getBatchNo().toLowerCase() + "\r\n");
                        tempsb.append("SETBOLD 0 \r\n");
                        tempsb.append("PRINT\r\n");
                    }else{
                        tempsb.append("\r\n");
                    }

                    StringBuffer loadStkSB = new StringBuffer();
                    StringBuffer soldStkSB = new StringBuffer();
                    StringBuffer freeSB = new StringBuffer();
                    StringBuffer emptySB = new StringBuffer();
                    StringBuffer returnSB = new StringBuffer();
                    StringBuffer replaceSB = new StringBuffer();
                    StringBuffer sihSB = new StringBuffer();
                    if (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT) {

                        slash = "";
                        if (bmodel.configurationMasterHelper.SHOW_EOD_OC) {
                            loadStkSB.append(stockBO.getVanLoadQty_cs());
                            soldStkSB.append(stockBO.getSoldQty_cs());
                            freeSB.append(stockBO.getFreeIssuedQty_cs());
                            emptySB.append(stockBO.getEmptyBottleQty_cs());
                            returnSB.append(stockBO.getReturnQty_cs());
                            replaceSB.append(stockBO.getReplacementQty_cs());
                            sihSB.append(stockBO.getSih_cs());
                            slash = "/";
                        }
                        if (bmodel.configurationMasterHelper.SHOW_EOD_OP) {
                            loadStkSB.append(slash + stockBO.getVanLoadQty_pc());
                            soldStkSB.append(slash + stockBO.getSoldQty_pc());
                            freeSB.append(slash + stockBO.getFreeIssuedQty_pc());
                            emptySB.append(slash + stockBO.getEmptyBottleQty_pc());
                            returnSB.append(slash + stockBO.getReturnQty_pc());
                            replaceSB.append(slash + stockBO.getReplacementQty_pc());
                            sihSB.append(slash + stockBO.getSih_pc());
                            slash = "/";
                        }
                        if (bmodel.configurationMasterHelper.SHOW_EOD_OO) {
                            loadStkSB.append(slash + stockBO.getVanLoadQty_ou());
                            soldStkSB.append(slash + stockBO.getSoldQty_ou());
                            freeSB.append(slash + stockBO.getFreeIssuedQty_ou());
                            emptySB.append(slash + stockBO.getEmptyBottleQty_ou());
                            returnSB.append(slash + stockBO.getReturnQty_ou());
                            replaceSB.append(slash + stockBO.getReplacemnetQty_ou());
                            sihSB.append(slash + stockBO.getSih_ou());
                        }
                    }


                    if (bmodel.configurationMasterHelper.SHOW_STOCK_FREE_ISSUED || bmodel.configurationMasterHelper.SHOW_STOCK_EMPTY || bmodel.configurationMasterHelper.SHOW_STOCK_REPLACE) {

                        x = 0;
                        x += 40;
                        tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                        tempsb.append("SETBOLD 1 \r\n");
                        tempsb.append("TEXT ANG12PT.CPF 0 " + x + " 1 "
                                + (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT ? (loadStkSB.toString()) : stockBO.getVanLoadQty()) + "\r\n");
                        tempsb.append("SETBOLD 0 \r\n");
                        tempsb.append("PRINT");

                        x += 100;
                        tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                        tempsb.append("SETBOLD 1 \r\n");
                        tempsb.append("TEXT ANG12PT.CPF 0 " + x + " 1 "
                                + (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT ? soldStkSB.toString() : stockBO.getSoldQty()) + "\r\n");
                        tempsb.append("SETBOLD 0 \r\n");
                        tempsb.append("PRINT");

                        if (bmodel.configurationMasterHelper.SHOW_STOCK_FREE_ISSUED) {
                            x += 90;
                            tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                            tempsb.append("SETBOLD 1 \r\n");
                            tempsb.append("TEXT ANG12PT.CPF 0 " + x + " 1 "
                                    + (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT ? freeSB.toString() : stockBO.getFreeIssuedQty()) + "\r\n");
                            tempsb.append("SETBOLD 0 \r\n");
                            tempsb.append("PRINT");
                        }

                        x += 90;
                        tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                        tempsb.append("SETBOLD 1 \r\n");
                        tempsb.append("TEXT ANG12PT.CPF 0 " + x + " 1 "
                                + (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT ? sihSB.toString() : stockBO.getSih()) + "\r\n");
                        tempsb.append("SETBOLD 0 \r\n");
                        tempsb.append("PRINT");

                        if (bmodel.configurationMasterHelper.SHOW_STOCK_EMPTY) {
                            x += 60;
                            tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                            tempsb.append("SETBOLD 1 \r\n");
                            tempsb.append("TEXT ANG12PT.CPF 0 " + x + " 1 "
                                    + (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT ? emptySB.toString() : stockBO.getEmptyBottleQty()) + "\r\n");
                            tempsb.append("SETBOLD 0 \r\n");
                            tempsb.append("PRINT");
                        }

                        x += 60;
                        tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                        tempsb.append("SETBOLD 1 \r\n");
                        tempsb.append("TEXT ANG12PT.CPF 0 " + x + " 1 "
                                + (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT ? returnSB.toString() : stockBO.getReturnQty()) + "\r\n");
                        tempsb.append("SETBOLD 0 \r\n");
                        tempsb.append("PRINT");

                        if (bmodel.configurationMasterHelper.SHOW_STOCK_REPLACE) {
                            x += 60;
                            tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                            tempsb.append("SETBOLD 1 \r\n");
                            tempsb.append("TEXT ANG12PT.CPF 0 " + x + " 1 "
                                    + (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT ? replaceSB : stockBO.getReplacementQty()) + "\r\n");
                            tempsb.append("SETBOLD 0 \r\n");
                            tempsb.append("PRINT\r\n");
                        } else {
                            tempsb.append("\r\n");
                        }
                    } else {
                        x = 0;
                        x += 260;
                        tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                        tempsb.append("SETBOLD 1 \r\n");
                        tempsb.append("TEXT ANG12PT.CPF 0 " + x + " 1 "
                                + (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT ? loadStkSB.toString() : stockBO.getVanLoadQty()) + "\r\n");
                        tempsb.append("SETBOLD 0 \r\n");
                        tempsb.append("PRINT");

                        x += 80;
                        tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                        tempsb.append("SETBOLD 1 \r\n");
                        tempsb.append("TEXT ANG12PT.CPF 0 " + x + " 1 "
                                + (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT ? soldStkSB.toString() : stockBO.getSoldQty()) + "\r\n");
                        tempsb.append("SETBOLD 0 \r\n");
                        tempsb.append("PRINT");

                        x += 70;
                        tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                        tempsb.append("SETBOLD 1 \r\n");
                        tempsb.append("TEXT ANG12PT.CPF 0 " + x + " 1 "
                                + (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT ? sihSB.toString() : stockBO.getSih()) + "\r\n");
                        tempsb.append("SETBOLD 0 \r\n");
                        tempsb.append("PRINT");

                        x += 60;
                        tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                        tempsb.append("SETBOLD 1 \r\n");
                        tempsb.append("TEXT ANG12PT.CPF 0 " + x + " 1 "
                                + (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT ? returnSB.toString() : stockBO.getReturnQty()) + "\r\n");
                        tempsb.append("SETBOLD 0 \r\n");
                        tempsb.append("PRINT\r\n");
                    }
                }

            }

            tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
            tempsb.append("SETBOLD 1 \r\n");
            tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 "+ "---------------------------------------------------------------------------\r\n");
            tempsb.append("SETBOLD 0 \r\n");
            tempsb.append("PRINT\r\n");

            tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
            tempsb.append("SETBOLD 1 \r\n");
            tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 "
                    + "Customer Sign -----------------------" + "\r\n");
            tempsb.append("SETBOLD 0 \r\n");
            tempsb.append("PRINT");

            tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
            tempsb.append("SETBOLD 1 \r\n");
            tempsb.append("TEXT ANG12PT.CPF 0 " + 330 + " 1 "
                    + "Rep. Sign ----------------" + "\r\n");
            tempsb.append("SETBOLD 0 \r\n");
            tempsb.append("PRINT\r\n");
            tempsb.append("\r\n");

            printDataBytes = String.valueOf(tempsb).getBytes("ISO-8859-11");

        } catch (Exception e){
            Commons.printException(e);
        }
        return printDataBytes;
    }
}