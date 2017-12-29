package com.ivy.sd.print;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.ivy.cpg.view.salesreturn.SalesReturnHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.BeatMasterBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.SchemeBO;
import com.ivy.sd.png.bo.SchemeProductBO;
import com.ivy.sd.png.bo.TempSchemeBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.DateUtil;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class PrintPreviewScreen extends IvyBaseActivityNoActionBar {

    private TextView invoiceno, customerid, phcontact, salesdate, salesmanid;
    private TextView routeid, time, invoicedate, totalamount, totalqty;
    private TextView sales, netsales, netdueinvoice, tccharged, custommsg,
            accountno, empid, retailername, goodsreturn, expiryreturn,
            freegoods;
    private String mInvoiceno, mCustomerid, mPhcontact, mSalesdate,
            mSalesmanid, mRouteid, mTime;

    private ListView lvwplist;
    private HashMap<String, ArrayList<ProductMasterBO>> batchList;
    private BusinessModel bmodel;
    private Vector<ProductMasterBO> mProducts = new Vector<ProductMasterBO>();
    private ArrayList<ProductMasterBO> mProductsForAdapter = new ArrayList<ProductMasterBO>();
    public List<ProductMasterBO> mTempProducts = new ArrayList<ProductMasterBO>();
    private ImageView imagevw;
    private boolean IsFromOrder, IsFromReport;
    private ProgressDialog pd;
    private static final int SELECTED_PRINTER_DIALOG = 1;
    private static final String ZEBRA_2INCH = "2";
    private static final String ZEBRA_4INCH = "4";
    private Connection zebraPrinterConnection;
    private final String[] mPrinterSelectionArray = {ZEBRA_2INCH, ZEBRA_4INCH};
    private String mSelectedPrinterName;
    private static final String TAG = "InvoicePrint";
    private String count;
    private ZebraPrinter printer;
    private EditText mMacAddressET;
    private TextView statusField;
    private ImageView mStatusIV;
    private double mTotalValue = 0, mNetSales, total;
    private LinearLayout mProductContainerLL;
    private Spinner printcount;
    private ArrayAdapter<CharSequence> spinadapter;
    private ArrayList<ProductMasterBO> batchproducts;
    private String m_data;
    private Bitmap m_bmp;
    private int totcase = 0, totpcs = 0, totouter = 0;
    private SchemeProductBO schemebo;
    private double saleablevalue, nonsaleablevalue;
    private float mfreegoods;
    private String storediscount = "0";
    private Map<String, Double> lineWiseDiscount;
    private SalesReturnHelper salesReturnHelper;

    private Toolbar toolbar;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_preview_zebra);

        try {
            bmodel = (BusinessModel) getApplicationContext();
            bmodel.setContext(this);
            salesReturnHelper = SalesReturnHelper.getInstance(this);
            Bundle extras = getIntent().getExtras();
            if (extras != null) {

                if (extras.containsKey("IsFromOrder")) {
                    IsFromOrder = extras.getBoolean("IsFromOrder");
                }
                if (extras.containsKey("IsFromReport"))
                    IsFromReport = extras.getBoolean("IsFromReport");
                // if (extras.containsKey("storediscount"))
                // storediscount = extras.getString("storediscount");
                Commons.print("setSpinnerPosition" + storediscount);
            }
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            invoiceno = (TextView) findViewById(R.id.invoiceno);
            customerid = (TextView) findViewById(R.id.custid);
            phcontact = (TextView) findViewById(R.id.telno);
            salesdate = (TextView) findViewById(R.id.salesdate);
            salesmanid = (TextView) findViewById(R.id.salesmanid);
            routeid = (TextView) findViewById(R.id.routeid);
            time = (TextView) findViewById(R.id.time);
            invoicedate = (TextView) findViewById(R.id.invoicedate);
            sales = (TextView) findViewById(R.id.salesamt);
            netsales = (TextView) findViewById(R.id.netsales);
            netdueinvoice = (TextView) findViewById(R.id.netdue);
            tccharged = (TextView) findViewById(R.id.tccharged);
            lvwplist = (ListView) findViewById(R.id.product_list_lv);
            lvwplist.setCacheColorHint(0);
            imagevw = (ImageView) findViewById(R.id.imgvw);
            imagevw.setImageBitmap(setIcon());
            mMacAddressET = (EditText) findViewById(R.id.et_mac);
            statusField = (TextView) findViewById(R.id.status_bar);
            mStatusIV = (ImageView) findViewById(R.id.status_iv);
            totalamount = (TextView) findViewById(R.id.totalamount);
            custommsg = (TextView) findViewById(R.id.custommsg);
            accountno = (TextView) findViewById(R.id.acc_no);
            empid = (TextView) findViewById(R.id.emp_id);
            totalqty = (TextView) findViewById(R.id.totalqty);
            retailername = (TextView) findViewById(R.id.retailername);
            goodsreturn = (TextView) findViewById(R.id.goodsreturn);
            expiryreturn = (TextView) findViewById(R.id.expiryreturn);
            freegoods = (TextView) findViewById(R.id.freegoods);
            mProductContainerLL = (LinearLayout) findViewById(R.id.product_container_ll);

            if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE) {
                findViewById(R.id.outerprice).setVisibility(View.GONE);
                findViewById(R.id.outerpricearabic).setVisibility(View.GONE);
            }
            // Set title to toolbar
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                getSupportActionBar().setTitle(
                        getResources().getString(R.string.print_preview));
                getSupportActionBar().setIcon(R.drawable.icon_stock);
                // Used to on / off the back arrow icon
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                // Used to remove the app logo actionbar icon and set title as home
                // (title support click)
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
            // Used to hide the app logo icon from actionbar
            // getSupportActionBar().setDisplayUseLogoEnabled(false);

            storediscount = bmodel.invoiceDisount;
            Commons.print("discount" + bmodel.invoiceDisount + " "
                    + bmodel.configurationMasterHelper.discountType);
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
            mMacAddressET.setText(pref.getString("MAC", ""));
        } catch (Exception e) {
            Commons.printException(e);
        }
        /** get batch products list **/
        batchList = bmodel.batchAllocationHelper.getBatchlistByProductID();
        doInitialize();
    }

    private void doInitialize() {
        try {
            mPhcontact = bmodel.userMasterHelper.getUserMasterBO()
                    .getDistributorContactNumber();
            mRouteid = getTodayBeat().getBeatId() + "";
            mCustomerid = bmodel.getRetailerMasterBO().getRetailerCode();
            mSalesdate = DateUtil.convertFromServerDateToRequestedFormat(
                    SDUtil.now(SDUtil.DATE_GLOBAL),
                    bmodel.configurationMasterHelper.outDateFormat);
            mTime = SDUtil.now(SDUtil.TIME);
            mInvoiceno = bmodel.invoiceNumber;
            mSalesmanid = bmodel.userMasterHelper.getUserMasterBO().getUserid()
                    + " "
                    + bmodel.userMasterHelper.getUserMasterBO().getUserName();

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

            totalamount.setText(bmodel.formatValue(total) + "");
            sales.setText(bmodel.formatValue(mTotalValue) + "");
            netsales.setText(bmodel.formatValue(mNetSales) + "");
            netdueinvoice.setText(bmodel.formatValue(mNetSales) + "");
            tccharged.setText(bmodel.formatValue(mNetSales) + "");
            invoicedate.setText(mSalesdate + "");
            salesmanid.setText(mSalesmanid);
            custommsg.setText(bmodel.userMasterHelper.getUserMasterBO()
                    .getCustommsg());
            empid.setText(bmodel.userMasterHelper.getUserMasterBO()
                    .getUserCode());
            accountno.setText(bmodel.userMasterHelper.getUserMasterBO()
                    .getAccountno() + "");
            if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
                totalqty.setText(totcase + "/" + totpcs);
            else
                totalqty.setText(totcase + "/" + totouter + "/" + totpcs);
            retailername
                    .setText(bmodel.getRetailerMasterBO().getRetailerName());
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
            // TODO Auto-generated catch block
            Commons.printException(e);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

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
            return true;
        } else if (i == R.id.menu_print) {
            new Thread(new Runnable() {
                public void run() {
                    Looper.prepare();
                    doConnection(ZEBRA_4INCH);
                    Looper.loop();
                    Looper.myLooper().quit();
                }
            }).start();

        }
        return false;
    }

    /**
     * get Today's beatId
     **/
    private BeatMasterBO getTodayBeat() {
        try {
            int size = bmodel.beatMasterHealper.getBeatMaster().size();
            for (int i = 0; i < size; i++) {
                BeatMasterBO b = bmodel.beatMasterHealper.getBeatMaster()
                        .get(i);
                if (b.getToday() == 1)
                    return b;
            }

        } catch (Exception e) {

        }
        return null;
    }

    /**
     * set values in product list
     **/
    private void updateproducts() {

        try {
            List<TempSchemeBO> schemeApplyList = null;

            schemeApplyList = bmodel.loadOrderDetail(bmodel
                            .getRetailerMasterBO().getRetailerID(), 1,
                    bmodel.invoiceNumber);
            mProductContainerLL.removeAllViews();
            LayoutInflater inflater = getLayoutInflater();
            Collections.sort(mProducts, ProductMasterBO.SKUWiseAscending);
            double schemeDiscountAmount = 0;
            ArrayList<String> applySchemeIdList = new ArrayList<String>();
            lineWiseDiscount = new HashMap<String, Double>();
            for (ProductMasterBO productBO : mProducts) {
                if ((productBO.getOrderedPcsQty() > 0
                        || productBO.getOrderedCaseQty() > 0 || productBO
                        .getOrderedOuterQty() > 0)) {
                    mProductsForAdapter.add(productBO);
                    int totalQty = productBO.getOrderedPcsQty()
                            + productBO.getOrderedCaseQty()
                            * productBO.getCaseSize()
                            + productBO.getOrderedOuterQty()
                            * productBO.getOutersize();
                    double total;
                    double tempAmount = 0;
                    double tempPrice = 0;
                    boolean isHit = false;
                    if (schemeApplyList != null && schemeApplyList.size() > 0) {
                        for (TempSchemeBO bo : schemeApplyList) {
                            if (bo.getProductID().equals(
                                    productBO.getProductID()))
                                if (bo.getSchemePrice() > 0) {
                                    tempPrice = bo.getSchemePrice();
                                    isHit = true;
                                    break;
                                }
                        }
                        if (isHit)
                            total = totalQty * tempPrice;
                        else
                            total = (productBO.getOrderedOuterQty() * productBO
                                    .getOsrp())
                                    + (productBO.getOrderedCaseQty() * productBO
                                    .getCsrp())
                                    + (productBO.getOrderedPcsQty() * productBO
                                    .getSrp());
                    } else {
                        total = (productBO.getOrderedOuterQty() * productBO
                                .getOsrp())
                                + (productBO.getOrderedCaseQty() * productBO
                                .getCsrp())
                                + (productBO.getOrderedPcsQty() * productBO
                                .getSrp());
                    }

                    /** Calculate discounted line wise order value **/
                    if (bmodel.configurationMasterHelper.SHOW_DISCOUNT_DIALOG) {

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

                    if (schemeApplyList != null && schemeApplyList.size() > 0) {
                        try {
                            for (TempSchemeBO bo : schemeApplyList) {
                                if (bo.getProductID().equals(
                                        productBO.getProductID())) {
                                    if (bo.getSchemePercentage() > 0) {

                                        if (total
                                                - (total
                                                * bo.getSchemePercentage() / 100) < 0) {
                                            tempAmount += (total
                                                    * bo.getSchemePercentage() / 100)
                                                    - total;
                                            total = 0;
                                        } else {
                                            tempAmount += (total
                                                    * bo.getSchemePercentage() / 100);
                                            total -= (total
                                                    * bo.getSchemePercentage() / 100);
                                        }

                                    } else if (bo.getSchemePrice() > 0) {
                                        if (total < 0) {
                                            total = 0;
                                        } else {
                                            if (isHit)
                                                tempAmount += ((totalQty
                                                        * (productBO
                                                        .getOrderedOuterQty() * productBO
                                                        .getOsrp())
                                                        + (productBO
                                                        .getOrderedCaseQty() * productBO
                                                        .getCsrp()) + (productBO
                                                        .getOrderedPcsQty() * productBO
                                                        .getSrp())) - (totalQty * tempPrice));
                                        }
                                    } else if (bo.getSchemeAmount() > 0
                                            && bo.getSchemeID() != null) {
                                        if (!checkSchemeApplied(
                                                bo.getSchemeID(),
                                                schemeApplyList)) {

                                            if (total - bo.getSchemeAmount() < 0) {
                                                tempAmount += (bo
                                                        .getSchemeAmount() - total);
                                                total = 0;
                                            } else {
                                                tempAmount += bo
                                                        .getSchemeAmount();
                                                total -= bo.getSchemeAmount();
                                            }
                                            bo.setSchemeApplied(true);
                                        }
                                    }

                                }
                            }
                        } catch (Exception e) {
                            Commons.printException(e);
                        }
                    } else if (productBO.getIsscheme() == 1) {
                        SchemeBO schemeBO = productBO.getSchemeBO();
                        if (schemeBO != null) {

                            if (schemeBO.isAmountTypeSelected()) {
                                if (!applySchemeIdList.contains(schemeBO
                                        .getSchemeId())) {
                                    tempAmount += schemeBO.getSelectedAmount();
                                    schemeDiscountAmount = schemeDiscountAmount
                                            + schemeBO.getSelectedAmount();
                                    applySchemeIdList.add(schemeBO
                                            .getSchemeId());
                                }
                            } else if (schemeBO.isDiscountPrecentSelected()) {

                                if (schemeBO.getSelectedPrecent() > 0) {
                                    tempAmount += (total
                                            * schemeBO.getSelectedPrecent() / 100);
                                    total = total
                                            - (total
                                            * schemeBO
                                            .getSelectedPrecent() / 100);
                                }

                            } else if (schemeBO.isPriceTypeSeleted()) {
                                if (schemeBO.getSelectedPrice() > 0) {
                                    tempAmount += (totalQty
                                            * (productBO.getOrderedOuterQty() * productBO
                                            .getOsrp())
                                            + (productBO.getOrderedCaseQty() * productBO
                                            .getCsrp()) + (productBO
                                            .getOrderedPcsQty() * productBO
                                            .getSrp()))
                                            - (totalQty * (productBO.getSrp() - schemeBO
                                            .getSelectedPrice()));
                                    total = totalQty
                                            * (productBO.getSrp() - schemeBO.getSelectedPrice());
                                }
                            }

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
                        ((TextView) v.findViewById(R.id.qty)).setText(productBO
                                .getOrderedCaseQty()
                                + "/"
                                + productBO.getOrderedOuterQty()
                                + "/"
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
                            productname.setText(batchbo.getBatchNo() + "");
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
                    if (productBO.getIsscheme() == 1
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
                            ProductMasterBO product = bmodel.productHelper
                                    .getProductMasterBOById(schemebo
                                            .getProductId());

                            TextView caseqty = new TextView(this);

                            TextView outerqty = new TextView(this);

                            TextView pcsqty = new TextView(this);

                            if (product != null) {
                                if (schemebo.getUomID() != 0) {
                                    if (product.getCaseUomId() == schemebo
                                            .getUomID()) {
                                        caseqty.setText(schemebo
                                                .getQuantitySelected() + "/");
                                        outerqty.setText(0 + "/");
                                        pcsqty.setText("0),");
                                    } else if (product.getOuUomid() == schemebo
                                            .getUomID()) {
                                        caseqty.setText(0 + "/");
                                        outerqty.setText(schemebo
                                                .getQuantitySelected() + "/");
                                        pcsqty.setText("0),");
                                    } else {
                                        caseqty.setText(0 + "/");
                                        outerqty.setText(0 + "/");
                                        pcsqty.setText(schemebo
                                                .getQuantitySelected() + "),");
                                    }
                                } else {
                                    caseqty.setText(0 + "/");
                                    outerqty.setText(0 + "/");
                                    pcsqty.setText(schemebo
                                            .getQuantitySelected() + "),");
                                }
                            }
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
            mTotalValue = mTotalValue - schemeDiscountAmount; // reduce Amount
            // based SCHEME
            // discount
            total = mTotalValue;

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
            // TODO Auto-generated catch block
            Commons.printException(e);
        }

    }

    private boolean checkSchemeApplied(String schemeID,
                                       List<TempSchemeBO> schemeApplyList) {
        for (TempSchemeBO bo : schemeApplyList) {
            if (bo.getSchemeID() != null && bo.getSchemeID().equals(schemeID)
                    && bo.isSchemeApplied())
                return true;
        }
        return false;
    }

    /**
     * set image in the preview screen
     **/
    private Bitmap setIcon() {
        Bitmap bit = null;
        try {

            File file = new File(
                    getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                            + "/"
                            + bmodel.userMasterHelper.getUserMasterBO()
                            .getUserid() + "/"
                            + DataMembers.DIGITAL_CONTENT + "/"
                            + "receiptImg.png");
            Commons.print("file" + file.getAbsolutePath());
            if (file.exists()) {

                bit = BitmapFactory.decodeFile(file.getAbsolutePath());
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            Commons.printException(e);
        }
        return bit;
    }

    public Handler getHandler() {
        // TODO Auto-generated method stub
        return mHandler;

    }

    // connectivity
    boolean isPrinterLanguageDetected = false;

    /**
     * printing invoice
     **/
    public void printInvoice(String printername) {

        try {
            if (printername.equals(ZEBRA_2INCH)) {
                for (int i = 0; i < SDUtil.convertToInt(count); i++)
                    zebraPrinterConnection.write(printDatafor2inchprinter());
                if (!IsFromReport) {
                    bmodel.showAlert(
                            getResources().getString(
                                    R.string.printed_successfully), 1234);
                } else {
                    bmodel.showAlert(
                            getResources().getString(
                                    R.string.printed_successfully), 121);
                }

            } else if (printername.equals(ZEBRA_4INCH)) {
                for (int i = 0; i < SDUtil.convertToInt(count); i++)
                    zebraPrinterConnection.write(printDatafor4inchprinter());
                if (!IsFromReport) {
                    bmodel.showAlert(
                            getResources().getString(
                                    R.string.printed_successfully), 1234);
                } else {
                    bmodel.showAlert(
                            getResources().getString(
                                    R.string.printed_successfully), 121);
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
            setStatus(getResources().getString(R.string.printer_not_connected),
                    Color.RED);
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
        try {
            zebraPrinterConnection.open();
            mStatusIV.setImageResource(R.drawable.greenball);
            setStatus("Connected", Color.GREEN);
        } catch (ConnectionException e) {
            setStatus(getResources().getString(R.string.printer_not_connected),
                    Color.RED);
            Commons.printException(e);

            DemoSleeper.sleep(1000);
            disconnect();
        } catch (Exception e) {

            setStatus(getResources().getString(R.string.printer_not_connected),
                    Color.RED);
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
                                        + bmodel.getOrderid(),
                                DataMembers.NOTIFY_ORDER_SAVED);
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                    return true;

                case DataMembers.NOTIFY_INVOICE_SAVED:
                    try {
                        if (pd != null)
                            pd.dismiss();
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
                    try {
                        new Thread(new Runnable() {
                            public void run() {
                                Looper.prepare();
                                doConnection(ZEBRA_4INCH);
                                Looper.loop();
                                Looper.myLooper().quit();
                            }
                        }).start();
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        Commons.printException(e);
                    }
                    return true;
            }
            return false;
        }
    });

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

    public byte[] printDatafor2inchprinter() {
        return null;
    }


    public byte[] printDatafor4inchprinter() {
        byte[] PrintDataBytes = null;
        try {
            PrinterLanguage printerLanguage = printer
                    .getPrinterControlLanguage();

            byte[] configLabel = null;
            if (printerLanguage == PrinterLanguage.ZPL) {
                configLabel = "^XA^FO17,16^GB379,371,8^FS^FT65,255^A0N,135,134^FDTEST^FS^XZ"
                        .getBytes();
            } else if (printerLanguage == PrinterLanguage.CPCL) {
                File file = new File(
                        getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                                + "/"
                                + bmodel.userMasterHelper.getUserMasterBO()
                                .getUserid()
                                + DataMembers.APP_DIGITAL_CONTENT + "/"
                                + "receiptImg.png");

                m_bmp = BitmapFactory.decodeFile(file.getAbsolutePath());

                int height = 0;
                int x = 370;
                height = x + mProductsForAdapter.size() * 350 + 500;
                Commons.print(TAG + "Heigt:" + height);
                String Printitem = "! 0 200 200 " + height + " 1\r\n"
                        + "CENTER\r\n";
                Printitem += ExtractGraphicsDataForCPCL(0, 0);
                if (SDUtil.convertToInt(count) == 1)
                    Printitem += "T 5 1 10 120 "
                            + getResources().getString(
                            R.string.arabian_trading_supplies) + "\r\n";
                else
                    Printitem += "T 5 1 10 120 "
                            + getResources().getString(
                            R.string.arabian_trading_supplies) + "("
                            + getResources().getString(R.string.duplicate)
                            + ")" + "\r\n";

                // Printitem += "T 5 0 10 80  "; /*
                // * "print distributor name and distributor address"
                // */
                //
                // Printitem += "\r\n" + "T 5 0 10 100  \r\n";
                Printitem += "\r\n";
                Printitem += "T 5 0 10 170 "
                        + getResources().getString(R.string.cash_tc_invoice)
                        + mInvoiceno + "\r\n";

                Printitem += "LEFT \r\n";
                Printitem += "T 5 0 20 230 "
                        + getResources().getString(R.string.cust) + ":"
                        + mCustomerid + "\r\n";
                Printitem += "T 5 0 300 230 "
                        + getResources().getString(R.string.tel) + ":"
                        + mPhcontact + "\r\n";
                Printitem += "T 5 0 520 230 "
                        + getResources().getString(R.string.sales_date) + ":"
                        + mSalesdate + "\r\n";
                Printitem += "T 5 0 280 260 "
                        + bmodel.getRetailerMasterBO().getRetailerName()
                        + "\r\n";
                Printitem += "T 5 0 20 290 "
                        + getResources().getString(R.string.salesman) + ":"
                        + mSalesmanid + "\r\n";
                Printitem += "T 5 0 510 290 "
                        + getResources().getString(R.string.invoice_date) + ":"
                        + mSalesdate + "\r\n";
                Printitem += "T 5 0 20 310 "
                        + getResources().getString(R.string.route) + ":"
                        + mRouteid + "\r\n";
                Printitem += "T 5 0 510 310 "
                        + getResources().getString(R.string.time) + ":" + mTime
                        + "\r\n";
                Printitem += "T 5 0 20 330 "
                        + getResources().getString(R.string.acc_no)
                        + ":"
                        + bmodel.userMasterHelper.getUserMasterBO()
                        .getAccountno() + "\r\n";
                Printitem += "T 5 0 510 330 "
                        + getResources().getString(R.string.emp_id)
                        + ":"
                        + bmodel.userMasterHelper.getUserMasterBO()
                        .getUserCode() + "\r\n";
                Printitem += "\r\n";
                Printitem += "CENTER \r\n";
                Printitem += "T 5 1 320 370 "
                        + getResources().getString(R.string.sales)
                        .toUpperCase() + "\r\n";
                Printitem += "\r\n";
                Printitem += "LEFT \r\n";
                Printitem += "T 5 0 20 420 "
                        + getResources().getString(R.string.itemno) + "\r\n";
                Printitem += "T 5 0 100 420 "
                        + getResources().getString(R.string.description)
                        + "\r\n";
                Printitem += "T 5 0 230 420 "
                        + getResources().getString(R.string.upc) + "\r\n";
                Printitem += "T 5 0 290 420 "
                        + getResources().getString(R.string.case_unit) + "\r\n";
                if (bmodel.configurationMasterHelper.SHOW_OUTER_CASE) {
                    Printitem += "T 5 0 370 420 "
                            + getResources().getString(R.string.case_price)
                            + "\r\n";
                    Printitem += "T 5 0 460 420 "
                            + getResources().getString(R.string.outer_price)
                            + "\r\n";
                    Printitem += "T 5 0 540 420 "
                            + getResources().getString(R.string.unit_price)
                            + "\r\n";
                    Printitem += "T 5 0 620 420 "
                            + getResources().getString(R.string.discount)
                            + "\r\n";
                    Printitem += "T 5 0 690 420 "
                            + getResources().getString(R.string.amount)
                            + "\r\n";
                } else {
                    Printitem += "T 5 0 370 420 "
                            + getResources().getString(R.string.case_price)
                            + "\r\n";
                    Printitem += "T 5 0 460 420 "
                            + getResources().getString(R.string.unit_price)
                            + "\r\n";
                    Printitem += "T 5 0 540 420 "
                            + getResources().getString(R.string.discount)
                            + "\r\n";
                    Printitem += "T 5 0 620 420 "
                            + getResources().getString(R.string.amount)
                            + "\r\n";
                }
                Printitem += "T 5 0 10 440 --------------------------------------------------\r\n";
                x += 70;
                for (ProductMasterBO productBO : mProductsForAdapter) {

                    double total = (productBO.getOrderedOuterQty()
                            * productBO.getOutersize() * productBO.getOsrp())
                            + (productBO.getCsrp()
                            * productBO.getOrderedCaseQty() * productBO
                            .getCaseSize())
                            + (productBO.getOrderedPcsQty() * productBO
                            .getSrp());

                    x += 20;
                    Printitem += "T 5 0 20 " + x + " "
                            + productBO.getProductCode() + "\r\n";
                    Printitem += "T 5 0 220 " + x + " "
                            + productBO.getProductName().toLowerCase() + "\r\n";
                    Printitem += "\r\n";
                    x += 40;
                    Printitem += "T 5 0 240 " + x + " "
                            + productBO.getCaseSize() + "\r\n";
                    if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
                        Printitem += "T 5 0 300 " + x + " "
                                + productBO.getOrderedCaseQty() + "/"
                                + productBO.getOrderedPcsQty() + "\r\n";
                    else
                        Printitem += "T 5 0 300 " + x + " "
                                + productBO.getOrderedCaseQty() + "/"
                                + productBO.getOrderedOuterQty() + "/"
                                + productBO.getOrderedPcsQty() + "\r\n";

                    Printitem += "T 5 0 380 " + x + " "
                            + bmodel.formatValue(productBO.getCsrp()) + "\r\n";
                    if (bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
                        Printitem += "T 5 0 460 " + x + " "
                                + bmodel.formatValue(productBO.getOsrp())
                                + "\r\n";
                    Printitem += "T 5 0 520 " + x + " "
                            + bmodel.formatValue(productBO.getSrp()) + "\r\n";
                    Printitem += "T 5 0 630 " + x + " "
                            + lineWiseDiscount.get(productBO.getProductID()) == null ? 0
                            : bmodel.formatValue(lineWiseDiscount.get(productBO
                            .getProductID())) + "\r\n";
                    Printitem += "T 5 0 670 " + x + " "
                            + bmodel.formatValue(total) + "\r\n";
                    batchproducts = batchList.get(productBO.getProductID());
                    if (batchproducts != null)
                        for (ProductMasterBO batchbo : batchproducts) {
                            x += 30;
                            // Printitem += "T 5 0 20 " + x + " "
                            // + batchbo.getProductCode() + "\r\n";
                            if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
                                Printitem += "T 5 0 220 "
                                        + x
                                        + " "
                                        + batchbo.getBatchNo().toLowerCase()
                                        + "("
                                        + DateUtil
                                        .convertFromServerDateToRequestedFormat(
                                                batchbo.getMfgDate(),
                                                bmodel.configurationMasterHelper.outDateFormat)
                                        + "," + batchbo.getOrderedCaseQty()
                                        + "/" + batchbo.getOrderedPcsQty()
                                        + ")," + "\r\n";
                            else
                                Printitem += "T 5 0 220 "
                                        + x
                                        + " "
                                        + batchbo.getBatchNo().toLowerCase()
                                        + "("
                                        + DateUtil
                                        .convertFromServerDateToRequestedFormat(
                                                SDUtil.now(SDUtil.DATE_GLOBAL),
                                                bmodel.configurationMasterHelper.outDateFormat)
                                        + "," + batchbo.getOrderedCaseQty()
                                        + "/" + batchbo.getOrderedOuterQty()
                                        + "/" + batchbo.getOrderedPcsQty()
                                        + ")," + "\r\n";

                        }
                    if (productBO.getIsscheme() == 1
                            && productBO.getSchemeProducts() != null) {
                        int size = productBO.getSchemeProducts().size();
                        for (int i = 0; i < size; i++) {
                            schemebo = productBO.getSchemeProducts().get(i);
                            x += 30;
                            // Printitem += "T 5 0 20 " + x + " "
                            // + schemebo.getpCode() + "\r\n";
                            if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
                                Printitem += "T 5 0 220 "
                                        + x
                                        + " "
                                        + schemebo.getProductName()
                                        .toLowerCase()
                                        + "("
                                        + DateUtil
                                        .convertFromServerDateToRequestedFormat(
                                                SDUtil.now(SDUtil.DATE_GLOBAL),
                                                bmodel.configurationMasterHelper.outDateFormat)
                                        + "," + 0 + "/"
                                        + schemebo.getQuantitySelected() + "),"
                                        + "\r\n";
                            else
                                Printitem += "T 5 0 220 "
                                        + x
                                        + " "
                                        + schemebo.getProductName()
                                        .toLowerCase()
                                        + "("
                                        + DateUtil
                                        .convertFromServerDateToRequestedFormat(
                                                SDUtil.now(SDUtil.DATE_GLOBAL),
                                                bmodel.configurationMasterHelper.outDateFormat)
                                        + "," + 0 + "/" + 0 + "/"
                                        + schemebo.getQuantitySelected() + "),"
                                        + "\r\n";
                        }
                    }

                }
                x += 40;
                Printitem += "T 5 0 40 "
                        + x
                        + " --------------------------------------------------\r\n";


                x += 30;
                Printitem += "T 5 0 20 " + x
                        + getResources().getString(R.string.total) + "\r\n";
                if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
                    Printitem += "T 5 0 300 " + x + " " + totcase + "/"
                            + totpcs + "\r\n";
                else
                    Printitem += "T 5 0 300 " + x + " " + totcase + "/"
                            + totouter + "/" + totpcs + "\r\n";
                Printitem += "T 5 0 670 " + x + " "
                        + bmodel.formatValue(mTotalValue) + "\r\n";
                x += 40;
                Printitem += "T 5 0 20 " + x
                        + getResources().getString(R.string.sales) + "\r\n";
                Printitem += "T 5 0 410 " + x + " "
                        + bmodel.formatValue(mTotalValue) + "\r\n";
                x += 40;

                Printitem += "T 5 0 20 " + x
                        + getResources().getString(R.string.good_returns)
                        + "\r\n";
                if (IsFromReport) {
                    if (salesReturnHelper.isInvoiceCreated(getApplicationContext(), bmodel
                            .getInvoiceNumber())) {
                        Printitem += "T 5 0 410 "
                                + x
                                + " "
                                + bmodel.formatValue(salesReturnHelper.getReturn_amt())
                                + "\r\n";
                    } else
                        Printitem += "T 5 0 410 " + x + " "
                                + bmodel.formatValue(0) + "\r\n";
                } else {
                    if (salesReturnHelper.isInvoiceCreated(getApplicationContext()))
                        Printitem += "T 5 0 410 "
                                + x
                                + " "
                                + bmodel.formatValue(salesReturnHelper.getReturn_amt())
                                + "\r\n";
                    else
                        Printitem += "T 5 0 410 " + x + " "
                                + bmodel.formatValue(0) + "\r\n";
                }
                x += 40;
                Printitem += "T 5 0 20 " + x
                        + getResources().getString(R.string.expiry_return)
                        + "\r\n";
                Printitem += "T 5 0 410 " + x + " "
                        + bmodel.formatValue(nonsaleablevalue) + "\r\n";
                x += 40;
                Printitem += "T 5 0 20 " + x
                        + getResources().getString(R.string.free_goods)
                        + "\r\n";
                Printitem += "T 5 0 410 " + x + " "
                        + bmodel.formatValue(mfreegoods) + "\r\n";
                x += 40;
                Printitem += "T 5 0 20 " + x
                        + getResources().getString(R.string.net_sales) + "\r\n";
                Printitem += "T 5 0 410 " + x + " "
                        + bmodel.formatValue(mNetSales) + "\r\n";
                x += 40;
                Printitem += "T 5 0 20 " + x
                        + getResources().getString(R.string.net_due_invoice)
                        + "\r\n";
                Printitem += "T 5 0 410 " + x + " "
                        + bmodel.formatValue(mNetSales) + "\r\n";
                x += 40;
                Printitem += "T 5 0 20 " + x
                        + getResources().getString(R.string.cash_paid) + "\r\n";
                Printitem += "T 5 0 410 " + x + " " + 0 + "\r\n";
                x += 40;
                Printitem += "T 5 0 20 " + x
                        + getResources().getString(R.string.tc_charged)
                        + "\r\n";
                Printitem += "T 5 0 410 " + x + " "
                        + bmodel.formatValue(mNetSales) + "\r\n";
                x += 40;
                Printitem += "T 5 0 40 "
                        + x
                        + " --------------------------------------------------\r\n";
                x += 40;
                Printitem += "T 5 0 20 " + x
                        + getResources().getString(R.string.salesman_signature)
                        + "\r\n";
                Printitem += "T 5 0 350 " + x
                        + " -----------------------------\r\n";
                x += 40;
                Printitem += "T 5 0 20 " + x
                        + getResources().getString(R.string.customer_signature)
                        + "\r\n";
                Printitem += "T 5 0 350 " + x
                        + " ------------------------------\r\n";
                x += 40;
                Printitem += "T 5 0 140 " + x
                        + getResources().getString(R.string.orginal);
                Printitem += "T 5 0 350 " + x
                        + getResources().getString(R.string.orginalarabic);
                x += 40;
                Printitem += "T 5 0 20 " + x
                        + bmodel.userMasterHelper.getUserMasterBO()

                        .getCustommsg();
                Printitem += "\r\n";
                Printitem += "\r\n";
                Printitem += "\r\n";
                Printitem += "\r\n";
                Printitem += "\r\n";
                Printitem += "\r\n";
                Printitem += "PRINT\r\n";

                PrintDataBytes = Printitem.getBytes();
            }
        } catch (Exception e) {
        }
        return PrintDataBytes;
    }

    public String getMacAddressFieldText() {
        String macAddress = null;
        try {
            macAddress = mMacAddressET.getText().toString().trim();
            // String macAddress = "00:22:58:08:1E:37";

            SharedPreferences pref = this.getSharedPreferences("PRINT",
                    MODE_PRIVATE);
            Editor editor = pref.edit();
            editor.putString("MAC", macAddress);
            editor.commit();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return macAddress;
    }

    @Override
    public Dialog onCreateDialog(int id) {
        switch (id) {

            case SELECTED_PRINTER_DIALOG:

                AlertDialog.Builder builder2 = new AlertDialog.Builder(
                        PrintPreviewScreen.this).setTitle("Choose Printer")
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
                break;
        }
        return null;

    }

    public String ExtractGraphicsDataForCPCL(int _xpos, int _ypos) {
        m_data = "";
        int color = 0, bit = 0, currentValue = 0, redValue = 0, blueValue = 0, greenValue = 0;

        try {
            // Make sure the width is divisible by 8
            int loopWidth = 8 - (m_bmp.getWidth() % 8);
            if (loopWidth == 8)
                loopWidth = m_bmp.getWidth();
            else
                loopWidth += m_bmp.getWidth();

            m_data = "EG" + " " + Integer.toString((loopWidth / 8)) + " "
                    + Integer.toString(m_bmp.getHeight()) + " "
                    + Integer.toString(_xpos) + " " + Integer.toString(_ypos)
                    + " ";

            for (int y = 0; y < m_bmp.getHeight(); y++) {
                bit = 128;
                currentValue = 0;
                for (int x = 0; x < loopWidth; x++) {
                    int intensity = 0;

                    if (x < m_bmp.getWidth()) {
                        color = m_bmp.getPixel(x, y);

                        redValue = Color.red(color);
                        blueValue = Color.blue(color);
                        greenValue = Color.green(color);

                        intensity = 255 - ((redValue + greenValue + blueValue) / 3);
                    } else
                        intensity = 0;

                    if (intensity >= 128)
                        currentValue |= bit;
                    bit = bit >> 1;
                    if (bit == 0) {
                        String hex = Integer.toHexString(currentValue);
                        hex = LeftPad(hex);
                        m_data = m_data + hex.toUpperCase();

                        bit = 128;
                        currentValue = 0;

                        /****
                         * String dbg = "x,y" + "-"+ Integer.toString(x) + "," +
                         * Integer.toString(y) + "-" + "Col:" +
                         * Integer.toString(color) + "-" + "Red: " +
                         * Integer.toString(redValue) + "-" + "Blue: " +
                         * Integer.toString(blueValue) + "-" + "Green: " +
                         * Integer.toString(greenValue) + "-" + "Hex: " + hex;
                         *
                         * Log.d(TAG,dbg);
                         *****/

                    }
                }// x
            }// y
            m_data = m_data + "\r\n";

        } catch (Exception e) {
            m_data = e.getMessage();
            return m_data;
        }

        return m_data;
    }

    private String LeftPad(String _num) {

        String str = _num;

        if (_num.length() == 1) {
            str = "0" + _num;
        }

        return str;
    }

    private double getDiscountAppliedValue(double discnt, double totalvalue) {
        double total;
        total = totalvalue;
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