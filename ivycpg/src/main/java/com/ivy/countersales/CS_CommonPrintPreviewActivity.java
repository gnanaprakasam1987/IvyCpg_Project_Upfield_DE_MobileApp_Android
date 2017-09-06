package com.ivy.countersales;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.aem.api.AEMPrinter;
import com.aem.api.AEMScrybeDevice;
import com.aem.api.IAemScrybe;
import com.bixolon.android.library.BxlService;
import com.bixolon.printer.BixolonPrinter;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.HomeScreenActivity;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.sd.png.view.OrderSummary;
import com.ivy.sd.print.DemoSleeper;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.graphics.ZebraImageFactory;
import com.zebra.sdk.graphics.ZebraImageI;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Set;

/**
 * Created by rajkumar.s on 6/23/2017.
 */

public class CS_CommonPrintPreviewActivity extends IvyBaseActivityNoActionBar {

    private TextView mPrinterStatusTV;
    private Spinner mPrintCountSpinner;
    private TextView mPreviewTV;
    private ImageView mDistLogoIV;
    private ArrayAdapter<CharSequence> mSpinnerAdapter;

    private BluetoothAdapter mBluetoothAdapter;

    private BxlService mBxlService = null;
    private BixolonPrinter bixolonPrinterApi = null;

    private Connection zebraPrinterConnection;

    private BusinessModel bmodel;

    private int mPrintCount;
    private int mPrintCountInput = 1;

    private boolean zebraPrinter = false;
    private boolean bixPrinter = true;

    private int mImagePrintCount = 0;
    private int mDataPrintCount = 0;

    private boolean isFromOrder;
    private boolean isUpdatePrintCount;
    private boolean isHomeBtnEnable;
    private boolean isPrintClicked;
    private int widthImage, heightImage;
    private String PRINT_STATE = "";
    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_common_print_preview);


        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        mPrinterStatusTV = (TextView) findViewById(R.id.printer_status);
        mPrintCountSpinner = (Spinner) findViewById(R.id.print_count);
        mDistLogoIV = (ImageView) findViewById(R.id.dist_logo);
        mPreviewTV = (TextView) findViewById(R.id.preView);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        isFromOrder = getIntent().getExtras().getBoolean("IsFromOrder", false);
        isUpdatePrintCount = getIntent().getExtras().getBoolean("IsUpdatePrintCount", false);
        isHomeBtnEnable = getIntent().getExtras().getBoolean("isHomeBtnEnable", false);
        if (isHomeBtnEnable) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(null);
            setScreenTitle(getResources().getString(R.string.print_preview));
        }

        widthImage = bmodel.mCS_commonPrintHelper.width_image;
        heightImage = bmodel.mCS_commonPrintHelper.height_image;

        onScreenPreparation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_print_preview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (isFromOrder) {
                    bmodel.productHelper.clearOrderTable();

                    Intent i = new Intent(
                            CS_CommonPrintPreviewActivity.this,
                            HomeScreenTwo.class);
                    Bundle extras = getIntent().getExtras();
                    if (extras != null) {
                        i.putExtra("IsMoveNextActivity", bmodel.configurationMasterHelper.MOVE_NEXT_ACTIVITY);
                        i.putExtra("CurrentActivityCode", OrderSummary.mActivityCode);
                    }
                    startActivity(i);
                } else {
                    Intent i = new Intent(
                            CS_CommonPrintPreviewActivity.this,
                            HomeScreenActivity.class).putExtra("menuCode", ConfigurationMasterHelper.MENU_COUNTER);
                    startActivity(i);
                }
                overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
                finish();
                return true;
            case R.id.menu_print:
                if (!isPrintClicked) {
                    isPrintClicked = true;
                    if (isHomeBtnEnable)
                        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    if (bmodel.configurationMasterHelper.COMMON_PRINT_ZEBRA || bmodel.configurationMasterHelper.SHOW_ZEBRA_TITAN) {
                        new CS_CommonPrintPreviewActivity.Print().execute("1");
                    } else if (bmodel.configurationMasterHelper.COMMON_PRINT_BIXOLON) {
                        doConnectionBixolon();
                    } else if (bmodel.configurationMasterHelper.COMMON_PRINT_SCRYBE) {
                        doConnectionScrybe();
                    }
                }
                break;
        }
        return false;
    }

    private void onScreenPreparation() {
        try {

            mPrintCount = bmodel.getPrint_count();

            mSpinnerAdapter = new ArrayAdapter<CharSequence>(this,
                    android.R.layout.simple_spinner_item);
            mSpinnerAdapter
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            if (bmodel.configurationMasterHelper.printCount > 1) {
                for (int i = 1; i <= bmodel.configurationMasterHelper.printCount; ++i)
                    mSpinnerAdapter.add(i + "");
                mPrintCountSpinner.setAdapter(mSpinnerAdapter);
            } else {
                mPrintCountSpinner.setVisibility(View.GONE);
            }

            mPrintCountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int pos, long id) {
                    mPrintCountInput = Integer.parseInt((String) parent.getSelectedItem());
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {

                }

            });

            if (bmodel.mCS_commonPrintHelper.isLogoEnabled) {
                //InputStream inputStream = getAssets().open("logo.9.png");
                Bitmap bmp = BitmapFactory.decodeStream(getLogoIS());
                mDistLogoIV.setImageBitmap(bmp);
            }

            mPreviewTV.setText(bmodel.mCS_commonPrintHelper.getInvoiceData().toString().replace("#B#", "").replace("print_type", "").replace("print_no", ""));

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public void updateStatus(final String statusMessage) {
        try {
            runOnUiThread(new Runnable() {
                public void run() {
                    mPrinterStatusTV.setText(statusMessage);
                }
            });
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private InputStream getLogoIS() {
        InputStream xmlFile = null;
        try {
            File f = new File(
                    getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                            + "/"
                            + bmodel.userMasterHelper.getUserMasterBO()
                            .getUserid() + "APP");
            if (f.isDirectory()) {
                File files[] = f.listFiles(new FilenameFilter() {
                    public boolean accept(File directory, String fileName) {
                        return fileName.startsWith("dist_logo");
                    }
                });

                for (File temp : files) {
                    xmlFile = new FileInputStream(temp);
                    break;
                }
            }
        } catch (Exception e) {
            Commons.printException(e);
        }

        return xmlFile;
    }

    class Print extends AsyncTask<String, Void, Boolean> {
        protected void onPreExecute() {
            updateStatus("Connecting...");
        }

        @Override
        protected Boolean doInBackground(String... params) {
            if (params[0].equals("1"))
                doZebraPrintNew(getMacAddressFieldText());

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            showAlert();
        }

    }


    public String getMacAddressFieldText() {
        String macAddress = null;
        try {
            // String macAddress = "00:22:58:3A:CD:46";
            SharedPreferences pref = this.getSharedPreferences("PRINT",
                    MODE_PRIVATE);
            macAddress = pref.getString("MAC", "");
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("MAC", macAddress);
            editor.commit();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return macAddress;
    }

    private void doZebraPrintNew(String macAddress) {
        ZebraPrinter zebraPrinter = null;
        //InputStream inputStream;
        ZebraImageI zebraImageI = null;
        try {

            if (macAddress.equals(""))
                updateStatus("Mac address is empty...");

            zebraPrinterConnection = new BluetoothConnection(macAddress);
            zebraPrinterConnection.open();

            if (bmodel.mCS_commonPrintHelper.isLogoEnabled) {
                zebraPrinter = ZebraPrinterFactory.getInstance(zebraPrinterConnection);
                Bitmap bitmap = BitmapFactory.decodeStream(getLogoIS());
                if (bitmap != null) {
                    Bitmap resizeBitamp = Bitmap.createScaledBitmap(bitmap, widthImage, heightImage, false);
                    zebraImageI = ZebraImageFactory.getImage(resizeBitamp);
                }
            }

            if (zebraPrinterConnection.isConnected())
                updateStatus("Printing...");

            for (int i = 0; i < mPrintCountInput; i++) {
                if (zebraPrinterConnection.isConnected()) {
                    if (bmodel.mCS_commonPrintHelper.isLogoEnabled) {
                        zebraPrinterConnection.write("! UTILITIES\r\nIN-MILLIMETERS\r\nSETFF 1 0\r\nPRINT\r\n".getBytes());
                        //arg : image,x,y,width,height
                        // - default height and width will be taken if it mentioned as "-1"
                        if (zebraImageI != null) {
                            zebraPrinter.printImage(zebraImageI, 230, 0, -1, -1, false);
                        }
                        mImagePrintCount++;
                    }
                    zebraPrinterConnection.write(getDataZebra());
                    mDataPrintCount++;
                    mPrintCount++;

                }
            }

            if (zebraPrinterConnection != null) {
                zebraPrinterConnection.close();
            }

        } catch (ConnectionException e) {
            Commons.printException(e);
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private byte[] getDataZebra() {

        try {

            StringBuilder tempsb = new StringBuilder();

            if (bmodel.configurationMasterHelper.IS_SHOW_PRINT_LANGUAGE_THAI) {
                tempsb.append("! U1 SETLP ANG12PT.CPF 0 34 \n");
            }

            String[] lines = bmodel.mCS_commonPrintHelper.getInvoiceData().toString().split("\\r?\\n");
            for (String s : lines) {

                if (s.contains("print_type")) {
                    if (mPrintCount == 0) {
                        s = s.replace("print_type", "Original");
                    } else {
                        s = s.replace("print_type", "Duplicate");
                    }
                }

                if (s.contains("print_no")) {
                    s = s.replace("print_no", (mPrintCount + 1) + " of " + mPrintCountInput);
                }

                if (s.contains("#B#")) {
                    String str = s.replace("#B#", "");
                    int spaceCount = 0;
                    for (char c : str.toCharArray()) {
                        if (c == ' ') {
                            spaceCount++;
                        } else {
                            break;
                        }
                    }

                    if (bmodel.configurationMasterHelper.IS_SHOW_PRINT_LANGUAGE_THAI) {
                        tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                        tempsb.append("SETBOLD 1 \n");
                        tempsb.append("TEXT ANG12PT.CPF 0 " + (spaceCount * 12) + " 1 " + str.substring(spaceCount, str.length()) + "\n");
                        tempsb.append("SETBOLD 0 \n");
                        tempsb.append("PRINT\r\n");
                    } else {
                        tempsb.append("! 0 200 200 " + 25 + " 1\r\n" + "LEFT\r\n");
                        tempsb.append("T 5 0 " + (spaceCount * 12) + " 1 " + str.substring(spaceCount, str.length()) + "\r\n");
                        tempsb.append("PRINT\r\n");
                    }

                } else {
                    if (bmodel.configurationMasterHelper.IS_SHOW_PRINT_LANGUAGE_THAI) {
                        tempsb.append(s.replaceAll(" ", "  ").replaceAll("-", "--"));
                        tempsb.append("\n\r");
                    } else {
                        tempsb.append(s);
                        tempsb.append("\n\r");
                    }
                }
            }

            byte[] result;
            if (bmodel.configurationMasterHelper.IS_SHOW_PRINT_LANGUAGE_THAI) {
                result = String.valueOf(tempsb).getBytes("ISO-8859-11");
            } else {
                result = String.valueOf(tempsb).getBytes();
            }


            return result;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            Commons.printException(e);
        }

        return new byte[0];

    }

    private void showAlert() {

        if (isUpdatePrintCount)
            bmodel.updatePrintCount(mPrintCount);

        boolean isPrintSuccess = false;
        if (mPrintCountInput == mDataPrintCount)
            isPrintSuccess = true;

        if (bmodel.mCS_commonPrintHelper.isLogoEnabled) {
            if (mPrintCountInput != mImagePrintCount)
                isPrintSuccess = false;
        }

        String msg;
        if (isPrintSuccess) {
            updateStatus("Print completed.");
            msg = getResources().getString(
                    R.string.printed_successfully);
        } else {
            updateStatus("Printer error.");
            msg = "Error";
        }

        new CommonDialog(getApplicationContext(), this,
                "", msg,
                false, getResources().getString(R.string.ok),
                null, new CommonDialog.positiveOnClickListener() {
            @Override
            public void onPositiveButtonClick() {
                if (isFromOrder) {
                    bmodel.productHelper.clearOrderTable();

                    Intent i = new Intent(
                            CS_CommonPrintPreviewActivity.this,
                            HomeScreenActivity.class).putExtra("menuCode", ConfigurationMasterHelper.MENU_COUNTER);
                    Bundle extras = getIntent().getExtras();
                    if (extras != null) {
                        i.putExtra("IsMoveNextActivity", bmodel.configurationMasterHelper.MOVE_NEXT_ACTIVITY);
                        i.putExtra("CurrentActivityCode", OrderSummary.mActivityCode);
                    }
                    startActivity(i);
                }else{
                    Intent i = new Intent(
                            CS_CommonPrintPreviewActivity.this,
                            HomeScreenActivity.class).putExtra("menuCode", ConfigurationMasterHelper.MENU_COUNTER);
                    startActivity(i);
                }
                bmodel.userMasterHelper.downloadDistributionDetails();
                finish();
            }
        }, new CommonDialog.negativeOnClickListener() {
            @Override
            public void onNegativeButtonClick() {
            }
        }).show();

    }


    /**
     * Bixolon Printer Connection.
     */
    private void doConnectionBixolon() {
        disconnectBixolon();
        bixolonPrinterApi = new BixolonPrinter(this, handler, null);
        bixolonPrinterApi.findBluetoothPrinters();
    }

    /**
     * Bixolon printer call back function.
     */
    private final Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what) {
                case BixolonPrinter.MESSAGE_BLUETOOTH_DEVICE_SET:
                    Commons.print("Handler," + "BixolonPrinter.MESSAGE_BLUETOOTH_DEVICE_SET");
                    if (msg.obj == null) {
                        //updateScreenStatus(layoutThereArentPairedPrinters);
                    } else {
                        String addr = "";
                        Set<BluetoothDevice> pairedDevices = (Set<BluetoothDevice>) msg.obj;
                        for (BluetoothDevice device : pairedDevices) {
                            //bixolonPrinterApi.connect(device.getAddress());
                            addr = device.getAddress();
                            break;
                        }
                        try {
                            bixolonPrinterApi.connect(addr);

                        } catch (Exception e) {
                            Commons.printException(e);
                        } finally {
                        }
                    }
                    return true;

                case BixolonPrinter.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BixolonPrinter.STATE_CONNECTED:

                            PRINT_STATE = "TRUE";
                            String[] lines = bmodel.mCS_commonPrintHelper.getInvoiceData().toString().split("\\n");
                            updateStatus("Printing...");
                            for (int i = 0; i < mPrintCountInput; i++) {
                                mDataPrintCount++;
                                mPrintCount++;
                                doBixDataPrint(lines);
                            }

                            DemoSleeper.sleep(2000 * mDataPrintCount);
                            disconnectBixolon();
                            showAlert();
                            break;

                        case BixolonPrinter.STATE_NONE:
                            PRINT_STATE = "NO_PRINTER";
                            break;

                    }
                    return true;


                case BixolonPrinter.MESSAGE_TOAST:
                    if (!PRINT_STATE.equalsIgnoreCase("TRUE") && !PRINT_STATE.equalsIgnoreCase("NO_PRINTER")) {
                        showAlert();
                    }
                    return true;


                case BixolonPrinter.MESSAGE_PRINT_COMPLETE:
                    return true;

                case BixolonPrinter.MESSAGE_ERROR_OUT_OF_MEMORY:
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.out_of_memory),
                            Toast.LENGTH_SHORT).show();
                    return true;
            }
            return true;
        }
    });

    /**
     * Disconnect Bixolon printer.
     */
    private void disconnectBixolon() {
        if (bixolonPrinterApi != null) {
            bixolonPrinterApi.disconnect();
        }
    }


    /**
     * Memory clear cache.
     */
    void CheckGC() {
        CheckGC("");
    }

    void CheckGC(String FunctionName) {
        long VmfreeMemory = Runtime.getRuntime().freeMemory();
        long VmmaxMemory = Runtime.getRuntime().maxMemory();
        long VmtotalMemory = Runtime.getRuntime().totalMemory();
        long Memorypercentage = ((VmtotalMemory - VmfreeMemory) * 100)
                / VmtotalMemory;

        Commons.print(FunctionName + "Before Memorypercentage"
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
        Commons.print(FunctionName + "_After Memorypercentage"
                + Memorypercentage + "% VmtotalMemory[" + VmtotalMemory + "] "
                + "VmfreeMemory[" + VmfreeMemory + "] " + "VmmaxMemory["
                + VmmaxMemory + "] ");
    }

    /**
     * Bixolon Image print.
     */
    private void doBixImgPrint() {

        try {
            if (bixolonPrinterApi != null) {
                bixolonPrinterApi.setSingleByteFont(BixolonPrinter.CODE_PAGE_858_EURO); //It fixes an issue printing special values like �, �����...

                bixolonPrinterApi.lineFeed(2, false); //It's like printing \n\n
                Bitmap fewlapsBitmap = BitmapFactory.decodeStream(getLogoIS());

                Bitmap resizedBitmap = Bitmap.createScaledBitmap(
                        fewlapsBitmap, widthImage, heightImage, false);
                if (resizedBitmap != null) {
                    bixolonPrinterApi.printBitmap(resizedBitmap, BixolonPrinter.ALIGNMENT_RIGHT, 0, 50, false);
                }

                Thread.sleep(100);
                bixolonPrinterApi.lineFeed(1, false);
                mImagePrintCount++;
            }

        } catch (Exception e) {
            Commons.printException("ERROR," + "Printing", e);
        }

    }

    /**
     * Bixolon Data print.
     *
     * @param lines
     */
    private void doBixDataPrint(String[] lines) {
        CheckGC();
        if (bmodel.mCS_commonPrintHelper.isLogoEnabled) {
            doBixImgPrint();
        }

        if (bmodel.configurationMasterHelper.IS_SHOW_PRINT_LANGUAGE_THAI) {
            bixolonPrinterApi.setSingleByteFont(BixolonPrinter.CODE_PAGE_THAI11);
        } else {
            bixolonPrinterApi.setSingleByteFont(BixolonPrinter.CODE_PAGE_858_EURO);
        }

        for (String s : lines) {
            if (s.contains("print_type")) {
                if (mPrintCount == 1) {
                    s = s.replace("print_type", "Original");
                } else {
                    s = s.replace("print_type", "Duplicate");
                }
            }

            if (s.contains("print_no")) {
                s = s.replace("print_no", (mPrintCount + 1) + " of " + mPrintCountInput);
            }

            if (s.contains("#B#")) {

                String str = s.replace("#B#", "");
                int spaceCount = 0;
                for (char c : str.toCharArray()) {
                    if (c == ' ') {
                        spaceCount++;
                    } else {
                        break;
                    }
                }

                StringBuilder sbs = new StringBuilder();
                for (int j = 0; j < spaceCount; j++) {
                    sbs.append(" ");
                }
                bixolonPrinterApi.printText(sbs.toString() + str.substring(spaceCount, str.length()) + "\n", BixolonPrinter.ALIGNMENT_LEFT,
                        BixolonPrinter.TEXT_ATTRIBUTE_FONT_A,
                        BixolonPrinter.TEXT_SIZE_HORIZONTAL1
                                | BixolonPrinter.TEXT_SIZE_VERTICAL1, true);

            } else {
                bixolonPrinterApi.printText(s + "\n", BixolonPrinter.ALIGNMENT_LEFT,
                        BixolonPrinter.TEXT_ATTRIBUTE_FONT_A,
                        BixolonPrinter.TEXT_SIZE_HORIZONTAL1
                                | BixolonPrinter.TEXT_SIZE_VERTICAL1, true);
            }
        }
    }

    /**
     * Scribe Printer Connections
     */
    private void doConnectionScrybe() {
        try {
            new CS_CommonPrintPreviewActivity.ScrybePrinter(new CS_CommonPrintPreviewActivity.ScrybeListener() {
                @Override
                public void isScrybeResponse(AEMPrinter aemPrinter, AEMScrybeDevice aemScrybeDevice, boolean isConnected) {
                    updateStatus("Printing...");
                    printScrybeData(aemPrinter, aemScrybeDevice, isConnected);
                }
            }).execute();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    /**
     * Method used for Scribe Printing
     *
     * @param aemPrinter
     * @param aemScrybeDevice
     * @param isconnected
     */

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private void printScrybeData(AEMPrinter aemPrinter, AEMScrybeDevice aemScrybeDevice, boolean isconnected) {
        byte fontSize = 26;
        if (isconnected) {
            updateStatus("Connected");
            if (aemPrinter != null) {
                try {
                    for (int i = 0; i < mPrintCountInput; i++) {
                        aemPrinter.setFontType(AEMPrinter.FONT_NORMAL);
                        aemPrinter.setFontSize(fontSize);
                        aemPrinter.print(getDataScrybe(aemPrinter));
                        aemPrinter.setCarriageReturn();
                        mDataPrintCount++;
                        mPrintCount++;
                    }

                    DemoSleeper.sleep(2000 * mDataPrintCount);

                } catch (Exception e) {
                    Commons.print("Print Error :" + e.toString());
                } finally {
                    showAlert();
                    disconnectScrybe(aemScrybeDevice);
                }
            }

        } else {
            updateStatus("Printer not connected.");
            showAlert();
            Toast.makeText(CS_CommonPrintPreviewActivity.this, "Printer not connected ..", Toast.LENGTH_SHORT).show();
            disconnectScrybe(aemScrybeDevice);
        }
    }

    /**
     * Disconnect Scrybe Printer
     *
     * @param aemScrybeDevice
     */
    private void disconnectScrybe(AEMScrybeDevice aemScrybeDevice) {
        if (aemScrybeDevice != null) {
            try {
                aemScrybeDevice.disConnectPrinter();
            } catch (IOException e) {
                Commons.printException(e);
            }
        }
    }


    private String doPrintAddSpace(int space, int maxlenght) {
        StringBuffer sb = new StringBuffer();
        if (space < maxlenght) {
            for (int i = 0; i < maxlenght - space; i++) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    /**
     * Scrybe Print data Alignment
     *
     * @param aemPrinter
     * @return
     */
    private String getDataScrybe(AEMPrinter aemPrinter) {

        try {
            StringBuilder tempsb = new StringBuilder();
            String[] lines = bmodel.mCS_commonPrintHelper.getInvoiceData().toString().split("\\n");
            for (String s : lines) {

                if (s.contains("print_type")) {
                    if (mPrintCount == 0) {
                        s = s.replace("print_type", "Original");
                    } else {
                        s = s.replace("print_type", "Duplicate");
                    }
                }

                if (s.contains("print_no")) {
                    s = s.replace("print_no", (mPrintCount + 1) + " of " + mPrintCountInput);
                }

                if (s.contains("#B#")) {
                    String str = s.replace("#B#", "");
                    int spaceCount = 0;
                    for (char c : str.toCharArray()) {
                        if (c == ' ') {
                            spaceCount++;
                        } else {
                            break;
                        }
                    }

                    String distName = str.substring(spaceCount, str.length());
                    int centerLength = 48 - distName.length();
                    tempsb.append(doPrintAddSpace(0, centerLength / 2));
                    tempsb.append(str.substring(spaceCount, str.length()));

                    aemPrinter.setFontType(AEMPrinter.DOUBLE_HEIGHT);
                    aemPrinter.setFontSize(AEMPrinter.FONT_001);
                    aemPrinter.print(tempsb.toString());

                    tempsb.delete(0, tempsb.length());
                    aemPrinter.setFontType(AEMPrinter.FONT_NORMAL);
                    aemPrinter.setFontSize((byte) 26);

                } else {
                    tempsb.append(s);
                    aemPrinter.print(tempsb.toString());
                    tempsb.delete(0, tempsb.length());
                }
            }

            return tempsb.toString();

        } catch (Exception e) {
            Commons.printException(e);
        }

        return "";
    }


    /**
     * Scrybe printer connection call back
     */
    public class ScrybePrinter extends AsyncTask<Void, Void, Boolean> implements IAemScrybe {

        private CS_CommonPrintPreviewActivity.ScrybeListener onScrybeListener;
        private AEMScrybeDevice m_AemScrybeDevice;
        private AEMPrinter m_AemPrinter = null;

        public ScrybePrinter(CS_CommonPrintPreviewActivity.ScrybeListener onScrybeListener) {
            this.onScrybeListener = onScrybeListener;
            this.m_AemScrybeDevice = new AEMScrybeDevice(CS_CommonPrintPreviewActivity.ScrybePrinter.this);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            ArrayList<String> aemPrinterList = m_AemScrybeDevice.getPairedPrinters();
            if (aemPrinterList != null) {
                for (int i = 0; i < aemPrinterList.size(); i++) {
                    Log.v("Check", aemPrinterList.get(i));
                }
                if (aemPrinterList.size() > 0)
                    m_AemPrinter = connect(aemPrinterList.get(0).toString());
            }

            if (m_AemPrinter != null) {
                return true;
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean printerStatus) {
            super.onPostExecute(printerStatus);
            onScrybeListener.isScrybeResponse(m_AemPrinter, m_AemScrybeDevice, printerStatus);
        }

        @Override
        public void onDiscoveryComplete(ArrayList<String> arrayList) {

        }

        private AEMPrinter connect(String printerName) {
            try {
                Log.v("Check", "connecting");
                m_AemScrybeDevice.connectToPrinter(printerName);
                m_AemPrinter = m_AemScrybeDevice.getAemPrinter();
                Log.v("Check", "connected");

            } catch (IOException e) {
                Commons.printException(e);
                disConnectScrybe();
                m_AemPrinter = null;
                return m_AemPrinter;
            }

            return m_AemPrinter;
        }

        private void disConnectScrybe() {
            if (m_AemScrybeDevice != null) {
                try {
                    m_AemScrybeDevice.disConnectPrinter();
                } catch (IOException e) {
                    Commons.printException(e);
                }
            }
        }

    }

    public interface ScrybeListener {
        void isScrybeResponse(AEMPrinter aemPrinter, AEMScrybeDevice aemScrybeDevice, boolean isConnected);
    }
}
