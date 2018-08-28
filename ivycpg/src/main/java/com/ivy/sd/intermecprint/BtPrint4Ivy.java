package com.ivy.sd.intermecprint;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.intermec.arabic.CUnicode;
import com.ivy.cpg.view.order.OrderHelper;
import com.ivy.cpg.view.salesreturn.SalesReturnHelper;
import com.ivy.lib.Utils;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.SchemeProductBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.DateUtil;
import com.ivy.sd.png.view.HomeScreenTwo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class BtPrint4Ivy extends IvyBaseActivityNoActionBar {
    btPrintFile btPrintService = null;
    private EditText mRemoteDevice;
    Button mConnectButton;
    private static final String TAG = "btprint";
    private static final boolean D = true;
    Button mBtnSelectFile;
    TextView mTxtFilename;
    Button mBtnPrint;
    PrintFileXML printFileXML = null;
    ArrayList<PrintFileDetails> printFileDetailses;
    public Spinner printcount;
    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Array adapter for the conversation thread
    private ArrayAdapter<String> mConversationArrayAdapter;
    private double mTotalValue = 0;

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Intent request codes for files list
    private static final int REQUEST_SELECT_FILE = 3;
    public static final String EXTRA_FILE_NAME = "fp4searslabel.prn";
    public static final String EXTRA_DEVICE_ADDRESS = "device_address";

    BluetoothAdapter mBluetoothAdapter = null;

    View _view;

    private TextView invoiceno, customerid, phcontact, salesdate, salesmanid;
    private TextView routeid, time, invoicedate, totalamount;
    private TextView total, sales, goodsreturn, expiryreturn, freegoods,
            netsales, netdueinvoice, cashpaid, tccharged, custommsg,
            retailername, accountno, empid, totalqty;
    private String mInvoiceno, mCustomerid, mPhcontact, mSalesdate,
            mSalesmanid, mRouteid, mTime, mInvoicedate, mSalesmanName, mAccno,
            mEmpid;
    private double mTotal, mSales, mNetSales, mNetDueInvoice, mCashPaid,
            mTcCharged;
    private int mGoodsreturn, mExpiryreturn;
    private SchemeProductBO schemebo;
    private int totcase = 0, totpcs = 0, totouter = 0;

    private ListView lvwplist;
    private HashMap<String, ArrayList<ProductMasterBO>> batchList;
    public BusinessModel bmodel;
    private Vector<ProductMasterBO> mProducts = new Vector<ProductMasterBO>();
    private ArrayList<ProductMasterBO> mProductsForAdapter = new ArrayList<ProductMasterBO>();
    public List<ProductMasterBO> mTempProducts = new ArrayList<ProductMasterBO>();
    private ImageView imagevw;
    boolean IsFromOrder, IsFromReport;
    public String mSelectedPrinterName;
    public String count;
    public TextView statusField;
    public ImageView mStatusIV;
    public LinearLayout mProductContainerLL;
    public Button reprint;
    public ArrayAdapter<CharSequence> spinadapter;
    private ArrayList<ProductMasterBO> batchproducts;
    private double saleablevalue, nonsaleablevalue, tot;
    private float mfreegoods;
    private String mystring;
    CUnicode ara = null;
    private String storediscount = "0";
    private Map<String, Double> lineWiseDiscount;
    SalesReturnHelper salesReturnHelper;
    private Toolbar toolbar;
    private OrderHelper orderHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // show
        super.onCreate(savedInstanceState);
        setContentView(R.layout.btprint_main);
        orderHelper = OrderHelper.getInstance(this);

        mRemoteDevice = (EditText) findViewById(R.id.remote_device);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        // mRemoteDevice.setText(R.string.bt_default_address);
        if (toolbar != null) {

            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(getResources().getString(R.string.print_preview));
//            // Used to on / off the back arrow icon
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//           // Used to remove the app logo actionbar icon and set title as home
//          // (title support click)
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        // connect button
        mConnectButton = (Button) findViewById(R.id.buttonConnect);
        mConnectButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                connectToDevice();
            }
        });

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(
                    this,
                    getResources().getString(
                            R.string.bluetooth_connection_not_available),
                    Toast.LENGTH_LONG).show();
            // finish();
            return;
        }
        mTxtFilename = (TextView) findViewById(R.id.txtFileName);
        mTxtFilename.setText(EXTRA_FILE_NAME);

        // mBtnPrint = (Button) findViewById(R.id.btnPrintFile);

        salesReturnHelper = SalesReturnHelper.getInstance(this);

        try {
            bmodel = (BusinessModel) getApplicationContext();
            bmodel.setContext(this);
            ara = new CUnicode();
            Bundle extras = getIntent().getExtras();
            if (extras != null) {

                if (extras.containsKey("IsFromOrder")) {
                    IsFromOrder = extras.getBoolean("IsFromOrder");
                }
                if (extras.containsKey("IsFromReport"))
                    IsFromReport = extras.containsKey("IsFromReport");
                // if (extras.containsKey("storediscount"))
                // storediscount = extras.getString("storediscount");
            }
            invoiceno = (TextView) findViewById(R.id.invoiceno);
            customerid = (TextView) findViewById(R.id.custid);
            phcontact = (TextView) findViewById(R.id.telno);
            salesdate = (TextView) findViewById(R.id.salesdate);
            salesmanid = (TextView) findViewById(R.id.salesmanid);
            routeid = (TextView) findViewById(R.id.routeid);
            time = (TextView) findViewById(R.id.time);
            invoicedate = (TextView) findViewById(R.id.invoicedate);
            sales = (TextView) findViewById(R.id.salesamt);
            goodsreturn = (TextView) findViewById(R.id.goodsreturn);
            expiryreturn = (TextView) findViewById(R.id.expiryreturn);
            freegoods = (TextView) findViewById(R.id.freegoods);
            netsales = (TextView) findViewById(R.id.netdue);
            netdueinvoice = (TextView) findViewById(R.id.netdue);
            cashpaid = (TextView) findViewById(R.id.cashpaid);
            tccharged = (TextView) findViewById(R.id.tccharged);
            lvwplist = (ListView) findViewById(R.id.product_list_lv);
            lvwplist.setCacheColorHint(0);
            imagevw = (ImageView) findViewById(R.id.imgvw);
            imagevw.setImageBitmap(setIcon());
            retailername = (TextView) findViewById(R.id.retailername);
            statusField = (TextView) findViewById(R.id.status_bar);
            mStatusIV = (ImageView) findViewById(R.id.status_iv);
            totalamount = (TextView) findViewById(R.id.totalamount);
            custommsg = (TextView) findViewById(R.id.custommsg);
            mProductContainerLL = (LinearLayout) findViewById(R.id.product_container_ll);
            accountno = (TextView) findViewById(R.id.acc_no);
            empid = (TextView) findViewById(R.id.emp_id);
            totalqty = (TextView) findViewById(R.id.totalqty);
            reprint = (Button) findViewById(R.id.reconnect_btn);


            if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE) {
                findViewById(R.id.outerprice).setVisibility(View.GONE);
                findViewById(R.id.outerpricearabic).setVisibility(View.GONE);
            }
            storediscount = orderHelper.invoiceDiscount;
        } catch (Exception e1) {
            Commons.printException("" + e1);
        }

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
            mRemoteDevice.setText(pref.getString("MAC", ""));
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        batchList = bmodel.batchAllocationHelper.getBatchlistByProductID();
        AssetFiles assetFiles = new AssetFiles(this);
        readPrintFileDescriptions();

    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            if (D)
                Commons.printException(TAG + ",++ ON START ++");

            if (mBluetoothAdapter != null) {
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableIntent = new Intent(
                            BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                } else {
                    if (btPrintService == null)
                        setupComm();
                }
            }
            mPhcontact = bmodel.userMasterHelper.getUserMasterBO()
                    .getDistributorContactNumber();

            mRouteid = bmodel.getRetailerMasterBO().getBeatID() + "";
            mCustomerid = bmodel.getRetailerMasterBO().getRetailerCode();
            mSalesdate = DateUtil.convertFromServerDateToRequestedFormat(
                    SDUtil.now(SDUtil.DATE_GLOBAL),
                    bmodel.configurationMasterHelper.outDateFormat);
            mTime = SDUtil.now(SDUtil.TIME);
            mInvoiceno = bmodel.invoiceNumber;
            mSalesmanid = bmodel.userMasterHelper.getUserMasterBO().getUserid()
                    + " "
                    + bmodel.userMasterHelper.getUserMasterBO().getUserName();
            mSalesmanName = bmodel.userMasterHelper.getUserMasterBO()
                    .getUserName();

            mAccno = bmodel.userMasterHelper.getUserMasterBO().getUserCode();
            mEmpid = bmodel.userMasterHelper.getUserMasterBO().getUserid() + "";

            /** set values in textview **/

            phcontact.setText(mPhcontact);
            routeid.setText(mRouteid + "");
            customerid.setText(mCustomerid);
            salesdate.setText(mSalesdate);
            time.setText(mTime);
            invoiceno.setText(getResources()
                    .getString(R.string.cash_tc_invoice) + mInvoiceno);
            if (null == bmodel.productHelper.getProductMaster()) {
                bmodel.showAlert(
                        getResources().getString(R.string.no_products_exists),
                        0);
                return;
            }
            // instead of sorting main product master BO.. making copy of it
            mTempProducts.clear();
            for (int i = 0; i < bmodel.productHelper.getProductMaster().size(); ++i) {
                ProductMasterBO product = (ProductMasterBO) bmodel.productHelper
                        .getProductMaster().get(i);
                if (product.getOrderedCaseQty() > 0
                        || product.getOrderedPcsQty() > 0
                        || product.getOrderedOuterQty() > 0)
                    mTempProducts.add(product);
            }
            mProducts = new Vector<ProductMasterBO>(mTempProducts.size());
            for (int j = 0; j < mTempProducts.size(); j++) {
                mProducts.add(new ProductMasterBO(mTempProducts.get(j)));
            }
            //
            salesReturnHelper.getNonSaleableReturnGoods(getApplicationContext());
            // saleablevalue = bmodel.salesReturnHelper.saleablevalue;
            nonsaleablevalue = salesReturnHelper.getNonsaleablevalue();
            updateproducts();

            totalamount.setText(bmodel.formatValue(tot) + "");
            sales.setText(bmodel.formatValue(mTotalValue) + "");
            netsales.setText(bmodel.formatValue(mNetSales) + "");
            netdueinvoice.setText(bmodel.formatValue(mNetSales) + "");
            tccharged.setText(bmodel.formatValue(mNetSales) + "");
            invoicedate.setText(mSalesdate + "");
            salesmanid.setText(mSalesmanid);
            custommsg.setText(bmodel.userMasterHelper.getUserMasterBO()
                    .getCustommsg());
            retailername
                    .setText(bmodel.getRetailerMasterBO().getRetailerName());
            empid.setText(bmodel.userMasterHelper.getUserMasterBO()
                    .getUserCode());
            accountno.setText(bmodel.userMasterHelper.getUserMasterBO()
                    .getAccountno() + "");
            if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
                totalqty.setText(totcase + "/" + totpcs);
            else
                totalqty.setText(totcase + "/" + totouter + "/" + totpcs);
            if (IsFromReport) {
                if (salesReturnHelper.isInvoiceCreated(getApplicationContext(), bmodel
                        .getInvoiceNumber()))
                    goodsreturn.setText(bmodel
                            .formatValue(salesReturnHelper.getReturn_amt())
                            + "");
                else
                    goodsreturn.setText(bmodel.formatValue(0) + "");
            } else {
                if (salesReturnHelper.isInvoiceCreated(getApplicationContext()))
                    goodsreturn.setText(bmodel
                            .formatValue(salesReturnHelper.getReturn_amt())
                            + "");
                else
                    goodsreturn.setText(bmodel.formatValue(0) + "");
            }
            expiryreturn.setText(bmodel.formatValue(nonsaleablevalue) + "");
            mfreegoods = bmodel.getOrderHeaderBO().getTotalFreeProductsAmount();
            freegoods.setText(mfreegoods + "");
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if (D)
            Commons.printException(TAG + ",+ ON RESUME +");

    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if (D)
            Commons.printException(TAG + ",- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        if (D)
            Commons.printException(TAG + ",-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (btPrintService != null)
            btPrintService.stop();
        if (D)
            Commons.printException(TAG + ",--- ON DESTROY ---");
    }

    void readPrintFileDescriptions() {
        InputStream inputStream = null;
        try {
            inputStream = this.getAssets().open("demofiles.xml");
            printFileXML = new PrintFileXML(inputStream);
            // now assign the array of known print files and there details
            printFileDetailses = printFileXML.printFileDetails;
        } catch (IOException e) {
            Commons.printException(TAG +
                    ",Exception in readPrintFileDescriptions: " + e.getMessage());
        }
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
            }
        }
    }

    byte[] escpQuery() {
        byte[] buf;
        String sBuf = "?{QST:HW}";
        ByteBuffer buf2;
        Charset charset = Charset.forName("UTF-8");
        buf2 = charset.encode(sBuf);
        buf2.put(0, (byte) 0x1B);
        return buf2.array();
    }

    private void updateproducts() {

        try {

            mProductContainerLL.removeAllViews();
            LayoutInflater inflater = getLayoutInflater();
            Collections.sort(mProducts, ProductMasterBO.SKUWiseAscending);
            lineWiseDiscount = new HashMap<String, Double>();
            for (ProductMasterBO productBO : mProducts) {
                if ((productBO.getOrderedPcsQty() > 0
                        || productBO.getOrderedCaseQty() > 0 || productBO
                        .getOrderedOuterQty() > 0)) {
                    mProductsForAdapter.add(productBO);

                    double tempAmount = 0;
                    double total = (productBO.getOrderedOuterQty() * productBO
                            .getOsrp())
                            + (productBO.getOrderedCaseQty() * productBO
                            .getCsrp())
                            + (productBO.getOrderedPcsQty() * productBO
                            .getSrp());

                    /** Calculate discounted line wise order value **/
                    if (bmodel.configurationMasterHelper.IS_ENTRY_LEVEL_DISCOUNT) {

                        double line_discount_sum = productBO.getD1()
                                + productBO.getD2() + productBO.getD3();
                        if (line_discount_sum > 0) {
                            tempAmount += (total * line_discount_sum / 100);
                            total = total - (total * line_discount_sum / 100);

                        } else if (productBO.getDA() > 0) {
                            total = total - productBO.getDA();
                            tempAmount += productBO.getDA();
                        }
                    }

                    mTotalValue = mTotalValue + total;
                    totcase = totcase + productBO.getOrderedCaseQty();
                    totpcs = totpcs + productBO.getOrderedPcsQty();
                    totouter = totouter + productBO.getOrderedOuterQty();
                    View v = inflater.inflate(R.layout.row_print_preview, null);
                    if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
                        ((TextView) v.findViewById(R.id.outerprice))
                                .setVisibility(View.GONE);
                    LinearLayout batchlist = (LinearLayout) v
                            .findViewById(R.id.batchlist);
                    LinearLayout schemelist = (LinearLayout) v
                            .findViewById(R.id.schemelist);
                    ((TextView) v.findViewById(R.id.productcode))
                            .setText(productBO.getProductCode() + "");
                    ((TextView) v.findViewById(R.id.product_name_tv))
                            .setText(productBO.getProductName() + "");
                    ((TextView) v.findViewById(R.id.upc)).setText(productBO
                            .getCaseSize() + "");
                    if (bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
                        ((TextView) v.findViewById(R.id.qty))
                                .setText(+productBO.getOrderedCaseQty() + "/"
                                        + productBO.getOrderedOuterQty() + "/"
                                        + productBO.getOrderedPcsQty());
                    else
                        ((TextView) v.findViewById(R.id.qty)).setText(productBO
                                .getOrderedCaseQty()
                                + "/"
                                + productBO.getOrderedPcsQty());

                    ((TextView) v.findViewById(R.id.price)).setText(bmodel
                            .formatValue(productBO.getCsrp()) + "");
                    if (bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
                        ((TextView) v.findViewById(R.id.outerprice))
                                .setText(bmodel.formatValue(productBO.getOsrp())
                                        + "");
                    lineWiseDiscount.put(productBO.getProductID(), tempAmount);
                    ((TextView) v.findViewById(R.id.unitprice)).setText(bmodel
                            .formatValue(productBO.getSrp()) + "");
                    ((TextView) v.findViewById(R.id.discount)).setText(bmodel
                            .formatValue(tempAmount) + "");
                    ((TextView) v.findViewById(R.id.amount)).setText(bmodel
                            .formatValue(total) + "");
                    mProductContainerLL.addView(v);
                    batchproducts = batchList.get(productBO.getProductID());
                    if (batchproducts != null)
                        for (ProductMasterBO batchbo : batchproducts) {
                            // TextView productcode = new TextView(this);
                            // productcode.setText(batchbo.getProductCode() +
                            // "");
                            TextView productname = new TextView(this);
                            productname.setText(getResources().getString(
                                    R.string.BATCH_NO)
                                    + "# " + batchbo.getBatchNo() + "");
                            TextView date = new TextView(this);
                            date.setText("("
                                    + DateUtil.convertFromServerDateToRequestedFormat(
                                    batchbo.getMfgDate(),
                                    bmodel.configurationMasterHelper.outDateFormat)
                                    + ",");
                            TextView caseqty = new TextView(this);
                            caseqty.setText(batchbo.getOrderedCaseQty() + "/");
                            TextView outerqty = new TextView(this);
                            outerqty.setText(batchbo.getOrderedOuterQty() + "/");
                            TextView pcsqty = new TextView(this);
                            pcsqty.setText(batchbo.getOrderedPcsQty() + "),");
                            // batchlist.addView(productcode);
                            batchlist.addView(productname);
                            batchlist.addView(date);
                            batchlist.addView(caseqty);
                            batchlist.addView(outerqty);
                            batchlist.addView(pcsqty);
                        }
                    if (productBO.isPromo()
                            && productBO.getSchemeProducts() != null) {
                        int size = productBO.getSchemeProducts().size();
                        for (int i = 0; i < size; i++) {
                            schemebo = productBO.getSchemeProducts().get(i);
                            // TextView productcode = new TextView(this);
                            // productcode.setText(schemebo.getpCode() + "");
                            TextView productname = new TextView(this);
                            productname.setText(schemebo.getProductName() + "");
                            TextView date = new TextView(this);
                            date.setText("("
                                    + DateUtil.convertFromServerDateToRequestedFormat(
                                    SDUtil.now(SDUtil.DATE_GLOBAL),
                                    bmodel.configurationMasterHelper.outDateFormat)
                                    + ",");

                            TextView caseqty = new TextView(this);
                            caseqty.setText(0 + "/");
                            TextView outerqty = new TextView(this);
                            outerqty.setText(0 + "/");
                            TextView pcsqty = new TextView(this);
                            pcsqty.setText(schemebo.getQuantitySelected()
                                    + "),");
                            // schemelist.addView(productcode);
                            schemelist.addView(productname);
                            schemelist.addView(date);
                            schemelist.addView(caseqty);
                            schemelist.addView(outerqty);
                            schemelist.addView(pcsqty);

                        }
                    }
                }
            }
            tot = mTotalValue;

            /** Apply store wise discount to totalvalue **/
            if (!storediscount.equals("0"))
                mTotalValue = getDiscountAppliedValue(
                        SDUtil.convertToDouble(storediscount), mTotalValue);

            if (bmodel.configurationMasterHelper.SHOW_SALES_RETURN_IN_INVOICE
                    && salesReturnHelper.isCreditNoteCreated(getApplicationContext()) != 1) {
                Commons.print("flag"
                        + salesReturnHelper.isInvoiceCreated(getApplicationContext()));
                if (IsFromReport) {
                    if (salesReturnHelper.isInvoiceCreated(getApplicationContext(), bmodel
                            .getInvoiceNumber()))
                        mNetSales = mTotalValue
                                - salesReturnHelper.getReturn_amt();
                    else
                        mNetSales = mTotalValue;
                } else {
                    if (salesReturnHelper.isInvoiceCreated(getApplicationContext()))
                        mNetSales = mTotalValue
                                - salesReturnHelper.getReturn_amt();
                    else
                        mNetSales = mTotalValue;

                }
            } else
                mNetSales = mTotalValue;
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    /**
     * this will print a file to the printer
     *
     * @throws IOException
     * @throws FileNotFoundException
     */
    void printFile() throws FileNotFoundException, IOException {
        try {
            String fileName = mTxtFilename.getText().toString();

            if (btPrintService.getState() != btPrintFile.STATE_CONNECTED) {
                return;
            }

            Integer totalWrite = 0;
            StringBuffer sb = new StringBuffer();
            int height = 0;
            int x = 1200;
            height = x + mProductsForAdapter.size() * 40 + 430;

            File file = new File(
                    getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                            + "/"
                            + bmodel.userMasterHelper.getUserMasterBO()
                            .getUserid()
                            + DataMembers.APP_DIGITAL_CONTENT + "/"
                            + "IMAGEN.PCX");

            System.out.println("file: " + file.getAbsolutePath());

            sb.append("INPUT ON\n");
            sb.append("CLIP ON\n");
            sb.append("IMAGE LOAD \"" + file.getName() + "\"," + file.length()
                    + ",\" \"\n");
            btPrintService.write(sb.toString().getBytes());
            btPrintService.write(getByteArrayFromImage(file.getAbsolutePath()));
            sb = new StringBuffer();
            sb.append("PP300," + x + ":AN1\n");
            sb.append("PM \"IMAGEN.PCX\"\n");
            x -= 30; // 1210
            sb.append("PP400," + x + ":AN1\n");

            sb.append("PP400," + x + ":FT \"Swiss 721 Bold BT\"\n");
            sb.append("FONTSIZE 10\n");
            if (SDUtil.convertToInt(count) == 1)
                sb.append("PP300,"
                        + x
                        + ":PT \""
                        + getResources().getString(
                        R.string.arabian_trading_supplies) + "\"\n");
            else
                sb.append("PP300,"
                        + x
                        + ":PT \""
                        + getResources().getString(
                        R.string.arabian_trading_supplies) + "("
                        + getResources().getString(R.string.duplicate) + ")"
                        + "\"\n");

            x -= 20;
            mystring = ara.Convert(
                    getResources().getString(
                            R.string.arabian_trading_supplies_arabic), false);
            sb.append("NASC \"UTF-8\"\r\n"); // NASC command to put the printer
            // in UTF-8
            sb.append("FONT \"Arial\"\r\n"); // Set the font to the downloaded
            // one
            sb.append("ALIGN 6\r\n");
            sb.append("PP500," + x + "\r\n");
            sb.append("PT \"" + mystring + "\"\r\n");
            x -= 40; // 1160
            sb.append("PP700," + x + ":FT \"Swiss 721 Bold BT\"\n");
            sb.append("FONTSIZE 10\n");
            sb.append("PP700," + x + ":PT \""
                    + getResources().getString(R.string.cash_tc_invoice)
                    + mInvoiceno + "\"\n");
            x -= 40; // 1120;
            sb.append("PP300," + x + ":FT \"Swiss 721 BT\"\n");
            sb.append("FONTSIZE 8\n");
            sb.append("PP300," + x + ":PT \""
                    + getResources().getString(R.string.cust) + ": "
                    + mCustomerid.trim() + "\"\n");
            sb.append("PP500," + x + ":PT \""
                    + getResources().getString(R.string.tel) + ":" + mPhcontact
                    + "\"\n");
            sb.append("PP780," + x + ":PT \""
                    + getResources().getString(R.string.sales_date) + ":"
                    + mSalesdate + "\"\n");
            x -= 40;
            sb.append("PP300," + x + ":PT \""
                    + bmodel.getRetailerMasterBO().getRetailerName() + "\"\n");
            mystring = ara.Convert(
                    getResources().getString(R.string.netduearabic), false);
            sb.append("NASC \"UTF-8\"\r\n"); // NASC command to put the
            // printer
            // in UTF-8
            sb.append("FONT \"Arial\"\r\n"); // Set the font to the
            // downloaded
            // one
            sb.append("ALIGN 6\r\n");
            sb.append("PP800," + x + "\r\n");
            sb.append("PT \"" + mystring + "\"\r\n");

            sb.append("PP320," + x + ":FT \"Swiss 721 BT\"\n");
            sb.append("FONTSIZE 8\n");
            x -= 40; // 1080;
            sb.append("PP320," + x + ":PT \""
                    + getResources().getString(R.string.salesman) + ":"
                    + mSalesmanid + "\"\n");
            sb.append("PP500," + x + ":PT \"" + mSalesmanName + "\"\n");
            sb.append("PP780," + x + ":PT \""
                    + getResources().getString(R.string.invoice_date) + ":"
                    + mSalesdate + "\"\n");

            x -= 40; // 1040;
            sb.append("PP240," + x + ":PT \""
                    + getResources().getString(R.string.route) + ":" + mRouteid
                    + "\"\n");
            sb.append("PP700," + x + ":PT \""
                    + getResources().getString(R.string.time) + ":" + mTime
                    + "\"\n");
            x -= 40; // 1000;
            sb.append("PP240," + x + ":PT \""
                    + getResources().getString(R.string.acc_no) + ":" + mAccno
                    + "\"\n");
            sb.append("PP700," + x + ":PT \""
                    + getResources().getString(R.string.emp_id) + ":" + mEmpid
                    + "\"\n");
            x -= 50; // 950;

            sb.append("PP400," + x + ":FT \"Swiss 721 Bold BT\"\n");
            sb.append("FONTSIZE 12\n");
            sb.append("PP400," + x + ":PT \""
                    + getResources().getString(R.string.sales) + "\"\n");

            mystring = ara.Convert(
                    getResources().getString(R.string.netsalesarabic), false);
            sb.append("NASC \"UTF-8\"\r\n"); // NASC command to put the printer
            // in UTF-8
            sb.append("FONT \"Arial\"\r\n"); // Set the font to the downloaded
            // one
            sb.append("ALIGN 6\r\n");
            sb.append("PP500," + x + "\r\n");
            sb.append("PT \"" + mystring + "\"\r\n");

            x -= 40; // 910;
            sb.append("PP40," + x + ":AN1\n");
            sb.append("PP40," + x + ":FT \"Swiss 721 Bold BT\"\n");
            sb.append("FONTSIZE 7\n");

            sb.append("PP30," + x + ":PT \""
                    + getResources().getString(R.string.itemno) + "\"\n");
            sb.append("PP120," + x + ":PT \""
                    + getResources().getString(R.string.description) + "\"\n");
            sb.append("PP250," + x + ":PT \""
                    + getResources().getString(R.string.upc) + "\"\n");
            sb.append("PP310," + x + ":PT \""
                    + getResources().getString(R.string.case_unit) + "\"\n");
            sb.append("PP400," + x + ":PT \""
                    + getResources().getString(R.string.outer_price) + "\"\n");
            sb.append("PP490," + x + ":PT \""
                    + getResources().getString(R.string.case_price) + "\"\n");
            sb.append("PP580," + x + ":PT \""
                    + getResources().getString(R.string.unit_price) + "\"\n");
            sb.append("PP660," + x + ":PT \""
                    + getResources().getString(R.string.discount) + "\"\n");
            sb.append("PP760," + x + ":PT \""
                    + getResources().getString(R.string.amount) + "\"\n");
            sb.append("FONTSLANT 0\n");
            sb.append("PL0,4\n");

            x -= 40;
            mystring = ara.Convert(
                    getResources().getString(R.string.itemno_arabic), false);
            sb.append("NASC \"UTF-8\"\r\n"); // NASC command to put the
            // printer
            // in UTF-8
            sb.append("FONT \"Arial\"\r\n"); // Set the font to the
            // downloaded
            // one
            sb.append("ALIGN 6\r\n");
            sb.append("PP30," + x + "\r\n");
            sb.append("PT \"" + mystring + "\"\r\n");

            mystring = ara.Convert(
                    getResources().getString(R.string.description_arabic),
                    false);
            sb.append("NASC \"UTF-8\"\r\n"); // NASC command to put the
            // printer
            // in UTF-8
            sb.append("FONT \"Arial\"\r\n"); // Set the font to the
            // downloaded
            // one
            sb.append("ALIGN 6\r\n");
            sb.append("PP80," + x + "\r\n");
            sb.append("PT \"" + mystring + "\"\r\n");

            mystring = ara.Convert(getResources()
                    .getString(R.string.upc_arabic), false);
            sb.append("NASC \"UTF-8\"\r\n"); // NASC command to put the
            // printer
            // in UTF-8
            sb.append("FONT \"Arial\"\r\n"); // Set the font to the
            // downloaded
            // one
            sb.append("ALIGN 6\r\n");
            sb.append("PP200," + x + "\r\n");
            sb.append("PT \"" + mystring + "\"\r\n");

            mystring = ara.Convert(
                    getResources().getString(R.string.case_unit_arabic), false);
            sb.append("NASC \"UTF-8\"\r\n"); // NASC command to put the
            // printer
            // in UTF-8
            sb.append("FONT \"Arial\"\r\n"); // Set the font to the
            // downloaded
            // one
            sb.append("ALIGN 6\r\n");
            sb.append("PP350," + x + "\r\n");
            sb.append("PT \"" + mystring + "\"\r\n");

            mystring = ara.Convert(
                    getResources().getString(R.string.outer_price_arabic),
                    false);
            sb.append("NASC \"UTF-8\"\r\n"); // NASC command to put the
            // printer
            // in UTF-8
            sb.append("FONT \"Arial\"\r\n"); // Set the font to the
            // downloaded
            // one
            sb.append("ALIGN 6\r\n");
            sb.append("PP450," + x + "\r\n");
            sb.append("PT \"" + mystring + "\"\r\n");

            mystring = ara
                    .Convert(
                            getResources()
                                    .getString(R.string.case_price_arabic),
                            false);
            sb.append("NASC \"UTF-8\"\r\n"); // NASC command to put the
            // printer
            // in UTF-8
            sb.append("FONT \"Arial\"\r\n"); // Set the font to the
            // downloaded
            // one
            sb.append("ALIGN 6\r\n");
            sb.append("PP550," + x + "\r\n");
            sb.append("PT \"" + mystring + "\"\r\n");

            mystring = ara
                    .Convert(
                            getResources()
                                    .getString(R.string.unit_price_arabic),
                            false);
            sb.append("NASC \"UTF-8\"\r\n"); // NASC command to put the
            // printer
            // in UTF-8
            sb.append("FONT \"Arial\"\r\n"); // Set the font to the
            // downloaded
            // one
            sb.append("ALIGN 6\r\n");
            sb.append("PP650," + x + "\r\n");
            sb.append("PT \"" + mystring + "\"\r\n");

            mystring = ara.Convert(
                    getResources().getString(R.string.discount_arabic), false);
            sb.append("NASC \"UTF-8\"\r\n"); // NASC command to put the
            // printer
            // in UTF-8
            sb.append("FONT \"Arial\"\r\n"); // Set the font to the
            // downloaded
            // one
            sb.append("ALIGN 6\r\n");
            sb.append("PP750," + x + "\r\n");
            sb.append("PT \"" + mystring + "\"\r\n");

            mystring = ara.Convert(
                    getResources().getString(R.string.amount_arabic), false);
            sb.append("NASC \"UTF-8\"\r\n"); // NASC command to put the
            // printer
            // in UTF-8
            sb.append("FONT \"Arial\"\r\n"); // Set the font to the
            // downloaded
            // one
            sb.append("ALIGN 6\r\n");
            sb.append("PP830," + x + "\r\n");
            sb.append("PT \"" + mystring + "\"\r\n");
            sb.append("FONTSLANT 0\n");
            sb.append("PL870,4\n");
            x -= 30;

            for (ProductMasterBO productBO : mProductsForAdapter) {

                System.out.println("PRoduct size: "
                        + mProductsForAdapter.size());

                double total = (productBO.getOrderedOuterQty() * productBO
                        .getOsrp())
                        + (productBO.getCsrp() * productBO.getOrderedCaseQty())
                        + (productBO.getOrderedPcsQty() * productBO.getSrp());
                x -= 30;
                sb.append("PP30," + x + ":FT \"Swiss 721 BT\"\n");
                sb.append("FONTSIZE 7\n");
                sb.append("PP30," + x + ":AN1:PT \""
                        + productBO.getProductCode().toLowerCase() + "\"\n");
                sb.append("PP120," + x + ":AN1:PT \""
                        + productBO.getProductName().toLowerCase() + "\"\n");

                x -= 30;
                sb.append("PP280," + x + ":AN3:PT \"" + productBO.getCaseSize()
                        + "\"\n");
                if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
                    sb.append("PP370," + x + ":AN3:PT \""
                            + productBO.getOrderedCaseQty() + "/"
                            + productBO.getOrderedPcsQty() + "\"\n");
                else
                    sb.append("PP370," + x + ":AN3:PT \""
                            + productBO.getOrderedCaseQty() + "/"
                            + productBO.getOrderedOuterQty() + "/"
                            + productBO.getOrderedPcsQty() + "\"\n");

                sb.append("PP460," + x + ":AN3:PT \"" + productBO.getCsrp()
                        + "\"\n");
                if (bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
                    sb.append("PP550," + x + ":AN3:PT \"" + productBO.getOsrp()
                            + "\"\n");
                sb.append("PP640," + x + ":AN3:PT \"" + productBO.getSrp()
                        + "\"\n");
                sb.append("PP720," + x + ":AN3:PT \""
                        + lineWiseDiscount.get(productBO.getProductID()) == null ? 0.0
                        : bmodel.formatValue(lineWiseDiscount.get(productBO
                        .getProductID())) + "\"\n");
                sb.append("PP820," + x + ":AN3:PT \"" + total + "\"\n");
                batchproducts = batchList.get(productBO.getProductID());
                if (batchproducts != null) {
                    for (ProductMasterBO batchbo : batchproducts) {
                        System.out.println("batchproducts size: "
                                + batchproducts.size());
                        x -= 30;
                        sb.append("PP30," + x + ":FT \"Swiss 721 BT\"\n");
                        sb.append("FONTSIZE 7\n");
                        // sb.append("PP30," + x + ":AN1:PT \""
                        // + batchbo.getProductCode() + "\"\n");
                        if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
                            sb.append("PP120,"
                                    + x
                                    + ":AN1:PT \""
                                    + " "
                                    + getResources().getString(
                                    R.string.BATCH_NO)
                                    + "# "
                                    + batchbo.getBatchNo().toLowerCase()
                                    + "("
                                    + DateUtil.convertFromServerDateToRequestedFormat(
                                    batchbo.getMfgDate(),
                                    bmodel.configurationMasterHelper.outDateFormat)
                                    + "," + batchbo.getOrderedCaseQty() + "/"
                                    + batchbo.getOrderedPcsQty() + "),"
                                    + "\"\n");
                        else
                            sb.append("PP120,"
                                    + x
                                    + ":AN1:PT \""
                                    + " "
                                    + getResources().getString(
                                    R.string.BATCH_NO)
                                    + "# "
                                    + batchbo.getBatchNo().toLowerCase()
                                    + "("
                                    + DateUtil.convertFromServerDateToRequestedFormat(
                                    SDUtil.now(SDUtil.DATE_GLOBAL),
                                    bmodel.configurationMasterHelper.outDateFormat)
                                    + "," + batchbo.getOrderedCaseQty() + "/"
                                    + batchbo.getOrderedOuterQty() + "/"
                                    + batchbo.getOrderedPcsQty() + "),"
                                    + "\"\n");

                    }

                }
                if (productBO.isPromo()
                        && productBO.getSchemeProducts() != null) {

                    int size = productBO.getSchemeProducts().size();
                    for (int i = 0; i < size; i++) {
                        System.out.println("getSchemeProducts size: " + size);
                        schemebo = productBO.getSchemeProducts().get(i);
                        x -= 30;
                        // sb.append("PP30," + x + ":AN1:PT \"" + " "
                        // + schemebo.getpCode() + "\n");
                        if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
                            sb.append("PP120,"
                                    + x
                                    + ":AN1:PT \""
                                    + " "
                                    + schemebo.getProductName().toLowerCase()
                                    + "("
                                    + DateUtil.convertFromServerDateToRequestedFormat(
                                    SDUtil.now(SDUtil.DATE_GLOBAL),
                                    bmodel.configurationMasterHelper.outDateFormat)
                                    + "," + 0 + "/"
                                    + schemebo.getQuantitySelected() + "),"
                                    + "\"\n");
                        else
                            sb.append("PP120,"
                                    + x
                                    + ":AN1:PT \""
                                    + " "
                                    + schemebo.getProductName().toLowerCase()
                                    + "("
                                    + DateUtil.convertFromServerDateToRequestedFormat(
                                    SDUtil.now(SDUtil.DATE_GLOBAL),
                                    bmodel.configurationMasterHelper.outDateFormat)
                                    + "," + 0 + "/" + 0 + "/"
                                    + schemebo.getQuantitySelected() + "),"
                                    + "\"\n");
                    }
                }
            }

            sb.append("FONTSLANT 0\n");
            sb.append("PL0,4\n");
            x -= 30; // 15*20 === 300
            sb.append("PP30," + x + ":FT \"Swiss 721 BT\"\n");
            sb.append("FONTSIZE 8\n");
            sb.append("PP440," + x + ":AN1:PT \""
                    + getResources().getString(R.string.total) + "\"\n");
            if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
                sb.append("PP600," + x + ":AN3:PT \"" + " " + totcase + "/"
                        + totouter + "/" + totpcs + "\"\n");
            else
                sb.append("PP600," + x + ":AN3:PT \"" + " " + totcase + "/"
                        + totpcs + "\"\n");

            sb.append("PP820," + x + ":AN3:PT \"" + " "
                    + Utils.round(mTotalValue, 2) + "\"\n");
            x -= 40;
            sb.append("PP40," + x + ":AN1:PT \""
                    + getResources().getString(R.string.sales) + "\"\n");
            sb.append("PP530," + x + ":AN3:PT \"" + " "
                    + Utils.round(mTotalValue, 2) + "\"\n");

            mystring = ara.Convert(
                    getResources().getString(R.string.netsalesarabic), false);
            sb.append("NASC \"UTF-8\"\r\n"); // NASC command to put the
            // printer
            // in UTF-8
            sb.append("FONT \"Arial\"\r\n"); // Set the font to the
            // downloaded
            // one
            sb.append("ALIGN 6\r\n");
            sb.append("PP820," + x + "\r\n");
            sb.append("PT \"" + mystring + "\"\r\n");

            x -= 30;
            sb.append("PP40," + x + ":FT \"Swiss 721 BT\"\n");
            sb.append("FONTSIZE 8\n");
            sb.append("PP40," + x + ":AN1:PT \""
                    + getResources().getString(R.string.good_returns) + "\"\n");
            if (IsFromReport) {
                if (salesReturnHelper.isInvoiceCreated(getApplicationContext(), bmodel
                        .getInvoiceNumber())) {
                    sb.append("PP530,"
                            + x
                            + ":AN3:PT \""
                            + " "
                            + bmodel.formatValue(salesReturnHelper.getReturn_amt())
                            + "\n");
                } else
                    sb.append("PP530," + x + ":AN3:PT \"" + " "
                            + bmodel.formatValue(0) + "\n");
            } else {
                if (salesReturnHelper.isInvoiceCreated(getApplicationContext()))
                    sb.append("PP530,"
                            + x
                            + ":AN3:PT \""
                            + " "
                            + bmodel.formatValue(salesReturnHelper.getReturn_amt())
                            + "\n");
                else
                    sb.append("PP530," + x + ":AN3:PT \"" + " "
                            + bmodel.formatValue(0) + "\n");
            }
            mystring = ara
                    .Convert(
                            getResources()
                                    .getString(R.string.goodsreturnarabic),
                            false);
            sb.append("NASC \"UTF-8\"\r\n"); // NASC command to put the
            // printer
            // in UTF-8
            sb.append("FONT \"Arial\"\r\n"); // Set the font to the
            // downloaded
            // one
            sb.append("ALIGN 6\r\n");
            sb.append("PP820," + x + "\r\n");
            sb.append("PT \"" + mystring + "\"\r\n");

            x -= 30;
            sb.append("PP40," + x + ":FT \"Swiss 721 BT\"\n");
            sb.append("FONTSIZE 8\n");
            sb.append("PP40," + x + ":AN1:PT \""
                    + getResources().getString(R.string.expiry_return) + "\"\n");
            sb.append("PP530," + x + ":AN3:PT \"" + " "
                    + bmodel.formatValue(nonsaleablevalue) + "\"\n");
            mystring = ara.Convert(
                    getResources().getString(R.string.expirysreturnarabic),
                    false);
            sb.append("NASC \"UTF-8\"\r\n"); // NASC command to put the
            // printer
            // in UTF-8
            sb.append("FONT \"Arial\"\r\n"); // Set the font to the
            // downloaded
            // one
            sb.append("ALIGN 6\r\n");

            sb.append("PP820," + x + "\r\n");
            sb.append("PT \"" + mystring + "\"\r\n");

            x -= 30;
            sb.append("PP40," + x + ":FT \"Swiss 721 BT\"\n");
            sb.append("FONTSIZE 8\n");
            sb.append("PP40," + x + ":AN1:AN1:PT \""
                    + getResources().getString(R.string.free_goods) + "\"\n");
            sb.append("PP530," + x + ":AN3:PT \"" + " "
                    + bmodel.formatValue(mfreegoods) + "\n");
            mystring = ara.Convert(
                    getResources().getString(R.string.freegoodsarabic), false);
            sb.append("NASC \"UTF-8\"\r\n"); // NASC command to put the
            // printer
            // in UTF-8
            sb.append("FONT \"Arial\"\r\n"); // Set the font to the
            // downloaded
            // one
            sb.append("ALIGN 6\r\n");
            sb.append("PP820," + x + "\r\n");
            sb.append("PT \"" + mystring + "\"\r\n");

            x -= 30;
            sb.append("PP80," + x + ":FT \"Swiss 721 BT\"\n");
            sb.append("FONTSIZE 8\n");
            sb.append("PP80," + x + ":AN1:PT \""
                    + getResources().getString(R.string.net_sales) + "\"\n");
            sb.append("PP580," + x + ":AN3:PT \"" + " "
                    + Utils.round(mNetSales, 2) + "\"\n");
            mystring = ara.Convert(
                    getResources().getString(R.string.netsalesarabic), false);
            sb.append("NASC \"UTF-8\"\r\n"); // NASC command to put the
            // printer
            // in UTF-8
            sb.append("FONT \"Arial\"\r\n"); // Set the font to the
            // downloaded
            // one
            sb.append("ALIGN 6\r\n");
            sb.append("PP820," + x + "\r\n");
            sb.append("PT \"" + mystring + "\"\r\n");

            x -= 30;
            sb.append("PP80," + x + ":FT \"Swiss 721 BT\"\n");
            sb.append("FONTSIZE 8\n");
            sb.append("PP80," + x + ":AN1:PT \""
                    + getResources().getString(R.string.net_due_invoice)
                    + "\"\n");
            sb.append("PP600," + x + ":AN3:PT \"" + " "
                    + Utils.round(mNetSales, 2) + "\"\n");
            mystring = ara.Convert(
                    getResources().getString(R.string.netduearabic), false);
            sb.append("NASC \"UTF-8\"\r\n"); // NASC command to put the
            // printer
            // in UTF-8
            sb.append("FONT \"Arial\"\r\n"); // Set the font to the
            // downloaded
            // one
            sb.append("ALIGN 6\r\n");

            sb.append("PP820," + x + "\r\n");
            sb.append("PT \"" + mystring + "\"\r\n");

            x -= 20;
            sb.append("PP520," + x + ":AN1:PT \"---------------\"\n");

            x -= 30;
            sb.append("PP140," + x + ":FT \"Swiss 721 BT\"\n");
            sb.append("FONTSIZE 8\n");
            sb.append("PP140," + x + ":AN1:PT \""
                    + getResources().getString(R.string.cash_paid) + "\"\n");
            sb.append("PP600," + x + ":AN3:PT \"" + " "
                    + Utils.round(mCashPaid, 2) + "\"\n");
            mystring = ara.Convert(
                    getResources().getString(R.string.cashpaidarabic), false);
            sb.append("NASC \"UTF-8\"\r\n"); // NASC command to put the
            // printer
            // in UTF-8
            sb.append("FONT \"Arial\"\r\n"); // Set the font to the
            // downloaded
            // one
            sb.append("ALIGN 6\r\n");

            sb.append("PP820," + x + "\r\n");
            sb.append("PT \"" + mystring + "\"\r\n");

            x -= 30;
            sb.append("PP140," + x + ":FT \"Swiss 721 BT\"\n");
            sb.append("FONTSIZE 8\n");
            sb.append("PP140," + x + ":AN1:PT \""
                    + getResources().getString(R.string.tc_charged) + "\"\n");
            sb.append("PP600," + x + ":AN3:PT \"" + " "
                    + Utils.round(mNetSales, 2) + "\"\n");

            sb.append("PP40," + x + ":FT \"Swiss 721 Bold BT\"\n");
            sb.append("FONTSIZE 10\n");
            x -= 60;
            sb.append("PP40," + x + ":AN1:PT \""
                    + getResources().getString(R.string.salesman_signature)
                    + "\"\n");

            sb.append("PP500,"
                    + x
                    + ":AN1:PT \"-------------------------------------------------\"\n");
            x -= 40;
            sb.append("PP40," + x + ":AN1:PT \""
                    + getResources().getString(R.string.customer_signature)
                    + "\"\n");

            sb.append("PP500,"
                    + x
                    + ":AN1:PT \"-------------------------------------------------\"\n");
            x -= 40;

            sb.append("PP140," + x + ":AN1:PT \""
                    + getResources().getString(R.string.orginal) + "\"\n");
            mystring = ara.Convert(
                    getResources().getString(R.string.orginalarabic), false);
            sb.append("NASC \"UTF-8\"\r\n"); // NASC command to put the printer
            // in UTF-8
            sb.append("FONT \"Arial\"\r\n"); // Set the font to the downloaded
            // one
            sb.append("ALIGN 6\r\n");
            sb.append("PP500," + x + "\r\n");
            sb.append("PT \"" + mystring + "\"\r\n");

            sb.append("PF\n");
            System.out.println("PRINT: " + sb.toString());
            btPrintService.write(sb.toString().getBytes());
        } catch (Exception e) {
            Commons.printException("" + e);
        }

    }

    private byte[] getByteArrayFromImage(String filePath)
            throws FileNotFoundException, IOException {

        File file = new File(filePath);
        System.out.println(file.exists() + "!!");

        FileInputStream fis = new FileInputStream(file);

        // InputStream in = resource.openStream();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        try {
            for (int readNum; (readNum = fis.read(buf)) != -1; ) {
                bos.write(buf, 0, readNum);
                // no doubt here is 0
                /*
                 * Writes len bytes from the specified byte array starting at
				 * offset off to this byte array output stream.
				 */
                System.out.println("read " + readNum + " bytes,");
            }
        } catch (IOException ex) {
            Commons.print("error," + "error");
        }
        byte[] bytes = bos.toByteArray();
        return bytes;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_print_preview, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i1 = item.getItemId();
        if (i1 == android.R.id.home) {
            try {

                if (IsFromReport)
                    finish();
                else {
                    bmodel.outletTimeStampHelper
                            .updateTimeStampModuleWise(SDUtil.now(SDUtil.TIME));
                    finish();
                    bmodel.productHelper.clearOrderTable();
                   /* BusinessModel.loadActivity(this,
                            DataMembers.actHomeScreenTwo);*/

                    Intent  myIntent = new Intent(this, HomeScreenTwo.class);
                    startActivityForResult(myIntent, 0);
                }
                overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            } catch (Exception e1) {
                Commons.printException("" + e1);
            }
            return true;
        } else if (i1 == R.id.menu_print) {// showDialog(SELECTED_PRINTER_DIALOG);
            try {
                for (int i = 0; i < SDUtil.convertToInt(count); i++) {
                    printFile();
                }

                if (IsFromReport) {
                    bmodel.showAlert(
                            getResources().getString(
                                    R.string.printed_successfully), 121);
                } else {
                    bmodel.showAlert(
                            getResources().getString(
                                    R.string.printed_successfully), 1234);
                }
            } catch (FileNotFoundException e) {
                Commons.printException("" + e);
            } catch (Exception e) {
                Commons.printException("" + e);
            }


        }
        return false;
    }

    void printESCP() {
        if (btPrintService != null) {
            if (btPrintService.getState() == btPrintFile.STATE_CONNECTED) {
                String message = btPrintService.printESCP();
                byte[] buf = message.getBytes();
                btPrintService.write(buf);
            }
        }
    }

    @SuppressLint("ResourceType")
    private void setupComm() {
        mConversationArrayAdapter = new ArrayAdapter<String>(this,
                R.id.remote_device);
        Commons.print(TAG + ",setupComm()");
        btPrintService = new btPrintFile(this, mHandler);
        if (btPrintService == null)
            Commons.printException(TAG + ",btPrintService init() failed");

    }

    public Handler getHandler() {
        // TODO Auto-generated method stub
        return mHandler;

    }

    // The Handler that gets information back from the btPrintService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case msgTypes.MESSAGE_STATE_CHANGE:
                    Bundle bundle = msg.getData();
                    int status = bundle.getInt("state");
                    if (D)
                        Commons.print(TAG + ",handleMessage: MESSAGE_STATE_CHANGE: "
                                + msg.arg1);
                    switch (msg.arg1) {
                        case btPrintFile.STATE_CONNECTED:
                            mConversationArrayAdapter.clear();
                            Commons.print(TAG + ",handleMessage: STATE_CONNECTED: "
                                    + mConnectedDeviceName);
                            break;
                        case btPrintFile.STATE_CONNECTING:
                            Commons.print(TAG + ",handleMessage: STATE_CONNECTING: "
                                    + mConnectedDeviceName);
                            break;
                        case btPrintFile.STATE_LISTEN:
                            Commons.print(TAG + ",handleMessage: STATE_LISTEN");
                            break;
                        case btPrintFile.STATE_IDLE:
                            Commons.print(TAG + ",handleMessage: STATE_NONE: not connected");
                            break;
                        case btPrintFile.STATE_DISCONNECTED:
                            Commons.print(TAG + ",handleMessage: STATE_DISCONNECTED");
                            break;
                    }
                    break;
                case msgTypes.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    String writeMessage = new String(writeBuf);
                    mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case msgTypes.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    mConversationArrayAdapter.add(mConnectedDeviceName + ":  "
                            + readMessage);
                    break;
                case msgTypes.MESSAGE_DEVICE_NAME:
                    mConnectedDeviceName = msg.getData().getString(
                            msgTypes.DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected",
                            Toast.LENGTH_SHORT).show();
                    Commons.print(TAG + ",handleMessage: CONNECTED TO: "
                            + msg.getData().getString(msgTypes.DEVICE_NAME));
                    updateConnectButton(false);

                    break;
                case msgTypes.MESSAGE_TOAST:

                    Commons.print(TAG +
                            ",handleMessage: TOAST: "
                            + msg.getData().getString(msgTypes.TOAST));
                    break;
                case msgTypes.MESSAGE_INFO:

                    String s = msg.getData().getString(msgTypes.INFO);
                    if (s.length() == 0)
                        s = String.format("int: %i"
                                + msg.getData().getInt(msgTypes.INFO));
                    Commons.print(TAG + ",handleMessage: INFO: " + s);
                    break;

                case DataMembers.NOTIFY_INVOICE_SAVED:
                    try {

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
                    break;
                case DataMembers.NOTIFY_PRINT:
                    try {
                        new Thread(new Runnable() {
                            public void run() {
                                connectToDevice();
                                // printFile();
                            }
                        }).start();
                    } catch (Exception e) {
                        Commons.printException("" + e);
                    }
                    break;
            }
        }
    };

    void connectToDevice() {
        String remote = getMacAddressFieldText();
        if (remote.length() == 0)
            return;
        if (btPrintService.getState() == btPrintFile.STATE_CONNECTED) {
            btPrintService.stop();
            // setConnectState(btPrintFile.STATE_DISCONNECTED);
            return;
        }

        String sMacAddr = remote;
        if (sMacAddr.contains(":") == false && sMacAddr.length() == 12) {

            char[] cAddr = new char[17];

            for (int i = 0, j = 0; i < 12; i += 2) {
                sMacAddr.getChars(i, i + 2, cAddr, j);
                j += 2;
                if (j < 17) {
                    cAddr[j++] = ':';
                }
            }

            sMacAddr = new String(cAddr);
        }

        BluetoothDevice device;
        try {
            device = mBluetoothAdapter.getRemoteDevice(sMacAddr);
        } catch (Exception e) {
            device = null;
        }

        if (device != null) {
            btPrintService.connect(device);
        } else {
        }
    }

    void connectToDevice(BluetoothDevice _device) {
        if (_device != null) {
            btPrintService.connect(_device);
        } else {
        }
    }

    // handles the scan devices and file list activity (dialog)
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (D)
            Commons.print(TAG + ",onActivityResult result code  " + resultCode);
        Commons.print(TAG + ",request code :" + requestCode);
        switch (requestCode) {
            case REQUEST_SELECT_FILE:
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String file = data.getExtras().getString(EXTRA_FILE_NAME);
                    if (printFileXML != null) {
                        PrintFileDetails details = printFileXML
                                .getPrintFileDetails(file);
                    }

                    mTxtFilename.setText(file);
                    // mRemoteDevice.setText(device.getAddress());
                    // Attempt to connect to the device
                }

                break;
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras().getString(
                            EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter
                            .getRemoteDevice(address);
                    mRemoteDevice.setText(device.getAddress());
                    // Attempt to connect to the device
                    // btPrintService.connect(device);
                    connectToDevice(device);
                }

                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Commons.print(TAG + ",onActivityResult: resultCode==OK");
                    // Bluetooth is now enabled, so set up a chat session
                    Commons.print(TAG + ",onActivityResult: starting setupComm()...");
                    setupComm();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Commons.print(TAG + ",onActivityResult: BT not enabled");

                    Toast.makeText(this, R.string.bt_not_enabled_turn_on_bluetooth,
                            Toast.LENGTH_SHORT).show();

                    // finish();
                }
                break;
        }
    }

    void updateConnectButton(boolean bConnected) {
        if (bConnected) {
            mConnectButton.setText(R.string.button_connect_text);
            mConnectButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        } else {
            mConnectButton.setText(R.string.button_disconnect_text);
            mConnectButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
    }

    private Bitmap setIcon() {
        Bitmap bit = null;
        try {

            File file = new File(
                    getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                            + "/"
                            + bmodel.userMasterHelper.getUserMasterBO()
                            .getUserid() + "/"
                            + DataMembers.DIGITAL_CONTENT + "/"
                            + "receiptImg.pcx");
            Commons.print("file" + file.getAbsolutePath());
            if (file.exists()) {

                bit = BitmapFactory.decodeFile(file.getAbsolutePath());
            }

        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return bit;
    }

    public String getMacAddressFieldText() {
        String macAddress = null;
        try {
            macAddress = mRemoteDevice.getText().toString().trim();
            // String macAddress = "00:22:58:08:1E:37";

            SharedPreferences pref = this.getSharedPreferences("PRINT",
                    MODE_PRIVATE);
            Editor editor = pref.edit();
            editor.putString("MAC", macAddress);
            editor.commit();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return macAddress;
    }

    private double getDiscountAppliedValue(double discnt, double totalvalue) {
        double total;
        total = mTotalValue;
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
}