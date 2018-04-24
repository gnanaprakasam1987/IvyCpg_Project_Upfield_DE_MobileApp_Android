package com.ivy.sd.print;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.text.Html;
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
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.BomReturnBO;
import com.ivy.sd.png.bo.PaymentBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.SchemeProductBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.SchemeDetailsMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.DateUtil;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class GhanaPrintPreviewActivity extends IvyBaseActivityNoActionBar {

    private BusinessModel bmodel;
    // Views
    private TextView tvDistName, tvDistTinNo, tvInvoiceNo, tvCustomerName,
            tvCustomerCode, tvCustomerAddress, tvPhContact, tvSalesDate,
            tvSalesMan;
    private TextView tvTotalValue, tvExcludeTaxAmt, tvVatName, tvVatAmount,
            tvNhilName, tvNhilAmount, tvCash, tvCheque, tvTotalCredit;
    private TextView tvTotalEmptyAmount, tvTotalAmountDue;
    private EditText mMacAddressET;
    private TextView statusField;
    private ImageView mStatusIV;
    // Global Variables
    private String mDistName = "", mDistTinNo = "", mInvoiceNo,
            mCustomerName = "", mCustomerCode = "", mCustomerAddress,
            mCustomerPhContact;
    private String mSalesDate, mSalesMan, mSalesManCode;

    private ProgressDialog progressDialog;
    private boolean IsFromOrder, IsFromReport, IsOriginal;
    private LinearLayout mTaxLayout, mProductContainerLL,
            mEmpProductContainerLL;
    private Vector<ProductMasterBO> mProducts = new Vector<ProductMasterBO>();
    private ArrayList<BomReturnBO> mEmptyProducts = new ArrayList<BomReturnBO>();
    private ArrayList<ProductMasterBO> mProductsForAdapter = new ArrayList<ProductMasterBO>();
    private ArrayList<BomReturnBO> mEmptyLiaProductsForAdapter = new ArrayList<BomReturnBO>();
    private ArrayList<BomReturnBO> mEmptyRetProductsForAdapter = new ArrayList<BomReturnBO>();

    private double vatPercentage = 0, nhlPercentage = 0;
    private double mCaseTotalValue = 0, mPcTotalValue = 0, mEmpTotalValue = 0,
            mTotalColValue, totalProd, totalEmp, mVatValue = 0, mNhlValue = 0,
            mTotColDueValue, mCash, mCheque, mTotCredit;
    private double mTotalOrderValue = 0, mtotalExcludeTaxAmount = 0;
    private String mVatName = "", mNhilName = "";
    private static final String ZEBRA_3INCH = "3";
    private ZebraPrinter printer;
    private Connection zebraPrinterConnection;
    private String m_data;
    // connectivity
    boolean isPrinterLanguageDetected = false;
    private String count;
    private Spinner printcount;
    private ArrayAdapter<CharSequence> spinadapter;
    private Bitmap m_bmp;
    int space = 0;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_preview_ghana);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        try {
            bmodel = (BusinessModel) getApplicationContext();
            bmodel.setContext(this);
            Bundle extras = getIntent().getExtras();
            if (extras != null) {

                if (extras.containsKey("IsFromOrder")) {
                    IsFromOrder = extras.getBoolean("IsFromOrder");
                }
                if (extras.containsKey("IsFromReport"))
                    IsFromReport = extras.getBoolean("IsFromReport");

            }
        } catch (Exception e1) {
            Commons.printException("" + e1);
        }
        // Set title to toolbar

        setSupportActionBar(toolbar);

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
        viewInitialization();

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

                }

            });

            SharedPreferences pref = this.getSharedPreferences("PRINT",
                    MODE_PRIVATE);
            mMacAddressET.setText(pref.getString("MAC", ""));
            productsList();
            updateproducts();
            updateTaxDetails();
            calculateTaxDetails();
            updateEmptiesproducts();
            getValuesFromObjects();
            setValuesInViews();
            try {

                tvTotalValue.setText(bmodel.formatValue(mTotalOrderValue) + "");
                tvTotalEmptyAmount.setText(bmodel.formatValue(mEmpTotalValue)
                        + "");
                tvTotalAmountDue.setText(bmodel.formatValue(mTotalOrderValue
                        + mEmpTotalValue));
                tvTotalCredit.setText(bmodel
                        .formatValue(mTotCredit) + "");
            } catch (Exception e) {
                Commons.printException(e);
            }
        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void viewInitialization() {

        mMacAddressET = (EditText) findViewById(R.id.et_mac);
        statusField = (TextView) findViewById(R.id.status_bar);
        mStatusIV = (ImageView) findViewById(R.id.status_iv);
        ((TextView) findViewById(R.id.tv_invoice_no)).setText(Html
                .fromHtml("Invoice N<sup><small>o</small></sup>"));
        ((TextView) findViewById(R.id.tv_telephone_no)).setText(Html
                .fromHtml("Tel N<sup><small>o</small></sup>"));

        tvDistName = (TextView) findViewById(R.id.distname);
        tvDistTinNo = (TextView) findViewById(R.id.dist_tin_no);
        tvInvoiceNo = (TextView) findViewById(R.id.invoiceno);
        tvCustomerName = (TextView) findViewById(R.id.customer);
        tvCustomerAddress = (TextView) findViewById(R.id.address);
        tvPhContact = (TextView) findViewById(R.id.telno);
        tvSalesDate = (TextView) findViewById(R.id.salesdate);
        tvSalesMan = (TextView) findViewById(R.id.salesman);
        mProductContainerLL = (LinearLayout) findViewById(R.id.product_container_ll);
        mEmpProductContainerLL = (LinearLayout) findViewById(R.id.emp_product_container_ll);
        tvTotalValue = (TextView) findViewById(R.id.fullstockcstot);
        mTaxLayout = (LinearLayout) findViewById(R.id.ll_taxlayout);
        tvExcludeTaxAmt = (TextView) findViewById(R.id.tv_tax_amount);
        tvVatName = (TextView) findViewById(R.id.vatlabel);
        tvVatAmount = (TextView) findViewById(R.id.vat_amount);
        tvNhilName = (TextView) findViewById(R.id.nhllabel);
        tvNhilAmount = (TextView) findViewById(R.id.nhl_amount);
        if (bmodel.configurationMasterHelper.TAX_SHOW_INVOICE) {
            mTaxLayout.setVisibility(View.VISIBLE);
        }
        tvTotalEmptyAmount = (TextView) findViewById(R.id.totalempbtltot);
        tvTotalAmountDue = (TextView) findViewById(R.id.totalamountdue);
        tvCash = (TextView) findViewById(R.id.cash);
        tvCheque = (TextView) findViewById(R.id.check);
        tvTotalCredit = (TextView) findViewById(R.id.totcredit);
    }

    private void productsList() {
        try {
            mProducts = bmodel.productHelper.getProductMaster();
            if (bmodel.configurationMasterHelper.SHOW_GROUPPRODUCTRETURN)
                mEmptyProducts = bmodel.productHelper
                        .getBomReturnTypeProducts();
            else
                mEmptyProducts = bmodel.productHelper.getBomReturnProducts();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private void getValuesFromObjects() {
        try {
            mDistName = bmodel.userMasterHelper.getUserMasterBO()
                    .getDistributorName();
            mDistTinNo = bmodel.userMasterHelper.getUserMasterBO()
                    .getDistributorTinNumber();
            mInvoiceNo = bmodel.invoiceNumber;
            mCustomerName = bmodel.getRetailerMasterBO().getRetailerName();
            mCustomerCode = bmodel.getRetailerMasterBO().getRetailerCode();
            mCustomerAddress = bmodel.getRetailerMasterBO().getAddress1();
            mCustomerPhContact = bmodel.getRetailerMasterBO()
                    .getContactnumber();
            mSalesDate = DateUtil.convertFromServerDateToRequestedFormat(
                    SDUtil.now(SDUtil.DATE_GLOBAL),
                    bmodel.configurationMasterHelper.outDateFormat);
            mSalesMan = bmodel.userMasterHelper.getUserMasterBO().getUserName();
            mSalesManCode = bmodel.userMasterHelper.getUserMasterBO()
                    .getUserCode();

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private void setValuesInViews() {
        try {

            if (mDistName != null && !mDistName.equals(null)
                    && !mDistName.equals("null"))
                tvDistName.setText(mDistName);
            if (mDistTinNo != null && !mDistTinNo.equals(null)
                    && !mDistTinNo.equals("null"))
                tvDistTinNo.setText(getResources().getString(R.string.tin_no)
                        + mDistTinNo);
            else
                tvDistTinNo.setText(getResources().getString(R.string.tin_no));
            tvInvoiceNo.setText(mInvoiceNo);
            if (mCustomerName != null && !mCustomerName.equals(null)
                    && !mCustomerName.equals("null"))
                tvCustomerName.setText(mCustomerCode + mCustomerName);
            if (mCustomerAddress != null && !mCustomerAddress.equals(null)
                    && !mCustomerAddress.equals("null"))
                tvCustomerAddress.setText(mCustomerAddress);

            if (mCustomerPhContact != null && !mCustomerPhContact.equals(null)
                    && !mCustomerPhContact.equals("null"))
                tvPhContact.setText(mCustomerPhContact);
            tvSalesDate.setText(mSalesDate);
            if (mSalesMan != null && !mSalesMan.equals(null)
                    && !mSalesMan.equals("null"))
                tvSalesMan.setText(mSalesManCode + mSalesMan);

            mCash = 0;
            mCheque = 0;
            if (bmodel.configurationMasterHelper.SHOW_COLLECTION_BEFORE_INVOICE) {
                List<PaymentBO> payment = bmodel.collectionHelper
                        .getPaymentList();
                if (payment != null && payment.size() > 0) {

                    if (payment.get(0).getCashMode()
                            .equals(StandardListMasterConstants.CHEQUE)) {
                        mCheque = payment.get(0).getAmount();
                    } else if (payment.get(0).getCashMode()
                            .equals(StandardListMasterConstants.CASH)) {
                        mCash = payment.get(0).getAmount();
                    }

                }

                if (mCash > 0) {
                    tvCash.setText(bmodel
                            .formatValue(mCash) + "");
                    findViewById(R.id.ll_layout_cheque)
                            .setVisibility(View.GONE);
                    findViewById(R.id.ll_cheque_line).setVisibility(View.GONE);
                } else if (mCheque > 0) {
                    findViewById(R.id.ll_layout_cash).setVisibility(View.GONE);
                    tvCheque.setText(bmodel
                            .formatValue(mCheque) + "");
                    findViewById(R.id.ll_cash_line).setVisibility(View.GONE);
                }
                if (mCash == 0 && mCheque == 0) {
                    tvCash.setText(0 + "");
                    tvCheque.setText(0 + "");
                }
            } else {
                tvCash.setText(bmodel.formatValue(mCash)
                        + "");
                tvCheque.setText(bmodel
                        .formatValue(mCheque) + "");
            }

            mTotCredit = (mTotalOrderValue + mEmpTotalValue)
                    - (mCash + mCheque);

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private void calculateTaxDetails() {
        try {
            mtotalExcludeTaxAmount = mTotalOrderValue
                    / (1 + (vatPercentage / 100) + (nhlPercentage / 100));

            tvExcludeTaxAmt.setText(bmodel.formatValue(mtotalExcludeTaxAmount)
                    + "");

            mVatValue = (mtotalExcludeTaxAmount * vatPercentage) / 100;
            mNhlValue = (mtotalExcludeTaxAmount * nhlPercentage) / 100;

            if (mVatName != "") {
                tvVatName.setText(mVatName + "(" + vatPercentage + "%)");
                tvVatAmount.setText(bmodel
                        .formatValue(mVatValue) + "");
            } else {
                tvVatName.setVisibility(View.GONE);
                tvVatAmount.setVisibility(View.GONE);
            }
            if (mNhilName != "") {
                tvNhilName.setText(mNhilName + "(" + nhlPercentage + "%)");
                tvNhilAmount.setText(bmodel
                        .formatValue(mNhlValue) + "");
            } else {
                tvNhilName.setVisibility(View.GONE);
                tvNhilAmount.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    /**
     * set values in product list
     **/
    private void updateproducts() {

        try {
            mProductContainerLL.removeAllViews();
            LayoutInflater inflater = getLayoutInflater();
            //	Collections.sort(mProducts, ProductMasterBO.SKUWiseAscending);
            for (ProductMasterBO productBO : mProducts) {
                if ((productBO.getOrderedPcsQty() > 0
                        || productBO.getOrderedCaseQty() > 0 || productBO
                        .getOrderedOuterQty() > 0)) {
                    mProductsForAdapter.add(productBO);

                    mTotalOrderValue = mTotalOrderValue
                            + productBO.getDiscount_order_value();

                    View v = inflater.inflate(
                            R.layout.row_print_preview_diageo, null);

                    ((TextView) v.findViewById(R.id.productcode))
                            .setText(productBO.getProductCode() + "");
                    ((TextView) v.findViewById(R.id.product_name_tv))
                            .setText(productBO.getProductShortName() + "");
                    ((TextView) v.findViewById(R.id.caseqty)).setText(productBO
                            .getOrderedCaseQty() + "");
                    ((TextView) v.findViewById(R.id.pieceqty))
                            .setText(productBO.getOrderedPcsQty() + "");
                    if (productBO.getOrderedCaseQty() > 0)
                        ((TextView) v.findViewById(R.id.unitprice))
                                .setText(bmodel.formatValue(productBO.getCsrp())
                                        + "");
                    else
                        ((TextView) v.findViewById(R.id.unitprice))
                                .setText(bmodel.formatValue(productBO.getSrp())
                                        + "");
                    ((TextView) v.findViewById(R.id.amount)).setText(bmodel
                            .formatValue(productBO.getDiscount_order_value())
                            + "");
                    mProductContainerLL.addView(v);
                    // free products added to display
                    SchemeDetailsMasterHelper schemeHelper=SchemeDetailsMasterHelper.getInstance(getApplicationContext());
                    if (schemeHelper.IS_SCHEME_ON) {
                        if (productBO.getSchemeProducts() != null
                                && productBO.getSchemeProducts().size() > 0) {
                            updatFreeProduct(productBO);
                        }
                    }
                }

            }

        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    /**
     * show free product in print preview
     *
     * @param productBO
     */
    private void updatFreeProduct(ProductMasterBO productBO) {
        List<SchemeProductBO> freeProductList = productBO.getSchemeProducts();
        if (freeProductList != null) {
            for (SchemeProductBO schemeProductBo : freeProductList) {
                ProductMasterBO product = bmodel.productHelper
                        .getProductMasterBOById(schemeProductBo.getProductId());
                if (product != null) {
                    LayoutInflater inflater = getLayoutInflater();
                    View v = inflater.inflate(
                            R.layout.row_print_preview_diageo, null);

                    ((TextView) v.findViewById(R.id.productcode))
                            .setText(product.getProductCode() + "");
                    ((TextView) v.findViewById(R.id.product_name_tv))
                            .setText(product.getProductShortName() + "");

                    if (schemeProductBo.getUomID() == product.getCaseUomId()
                            && product.getCaseUomId() != 0) {

                        ((TextView) v.findViewById(R.id.caseqty))
                                .setText(schemeProductBo.getQuantitySelected()
                                        + "");
                        ((TextView) v.findViewById(R.id.pieceqty))
                                .setText(0 + "");
                    } else if (schemeProductBo.getUomID() == product
                            .getPcUomid() && schemeProductBo.getUomID() == 0) {
                        ((TextView) v.findViewById(R.id.pieceqty))
                                .setText(schemeProductBo.getQuantitySelected()
                                        + "");
                        ((TextView) v.findViewById(R.id.caseqty))
                                .setText(0 + "");
                    } else {
                        ((TextView) v.findViewById(R.id.pieceqty))
                                .setText(schemeProductBo.getQuantitySelected()
                                        + "");
                        ((TextView) v.findViewById(R.id.caseqty))
                                .setText(0 + "");
                    }

                    ((TextView) v.findViewById(R.id.unitprice)).setText("");
                    ((TextView) v.findViewById(R.id.amount)).setText("");
                    mProductContainerLL.addView(v);
                }
            }
        }

    }

    /**
     * set values in product list
     **/
    private void updateEmptiesproducts() {

        try {

            double mLiableTot = 0, mRetTot = 0;

            mEmpProductContainerLL.removeAllViews();
            LayoutInflater inflater = getLayoutInflater();
            if (mEmptyProducts != null) {
                //Collections.sort(mEmptyProducts, BomReturnBO.SKUWiseAscending);
                for (BomReturnBO productBO : mEmptyProducts) {
                    if ((productBO.getLiableQty() > 0)) {
                        mEmptyLiaProductsForAdapter.add(productBO);
                        totalEmp = (productBO.getLiableQty() * productBO
                                .getpSrp());

                        mLiableTot = mLiableTot
                                + (productBO.getLiableQty() * productBO
                                .getpSrp());

                        View v = inflater.inflate(
                                R.layout.row_print_preview_diageo, null);

                        ((TextView) v.findViewById(R.id.productcode))
                                .setText(productBO.getProdCode() + "");
                        ((TextView) v.findViewById(R.id.product_name_tv))
                                .setText(productBO.getProductShortName()
                                        + "-Liable");
                        ((TextView) v.findViewById(R.id.caseqty))
                                .setText(bmodel.formatValue(0) + "");
                        ((TextView) v.findViewById(R.id.pieceqty))
                                .setText(bmodel.formatValue(productBO
                                        .getLiableQty()) + "");
                        ((TextView) v.findViewById(R.id.unitprice))
                                .setText(bmodel.formatValue(productBO.getpSrp())
                                        + "");
                        ((TextView) v.findViewById(R.id.amount)).setText(bmodel
                                .formatValue(totalEmp) + "");
                        mEmpProductContainerLL.addView(v);
                    }

                }
            }

            for (BomReturnBO productBO2 : mEmptyProducts) {
                if ((productBO2.getReturnQty() > 0)) {
                    mEmptyRetProductsForAdapter.add(productBO2);
                    totalEmp = (productBO2.getReturnQty() * productBO2
                            .getpSrp());

                    mRetTot = mRetTot
                            + (productBO2.getReturnQty() * productBO2.getpSrp());

                    View v = inflater.inflate(
                            R.layout.row_print_preview_diageo, null);

                    ((TextView) v.findViewById(R.id.productcode))
                            .setText(productBO2.getProdCode() + "");
                    ((TextView) v.findViewById(R.id.product_name_tv))
                            .setText(productBO2.getProductShortName()
                                    + "-Returnable");
                    ((TextView) v.findViewById(R.id.caseqty)).setText(bmodel
                            .formatValue(0) + "");
                    ((TextView) v.findViewById(R.id.pieceqty)).setText(bmodel
                            .formatValue(productBO2.getReturnQty()) + "");
                    ((TextView) v.findViewById(R.id.unitprice)).setText(bmodel
                            .formatValue(productBO2.getpSrp()) + "");
                    ((TextView) v.findViewById(R.id.amount)).setText("-"
                            + bmodel.formatValue(totalEmp) + "");
                    mEmpProductContainerLL.addView(v);
                }

            }

            mEmpTotalValue = SDUtil.convertToDouble(bmodel
                    .formatValue(mLiableTot - mRetTot));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Commons.printException(e);
        }

    }

    public Handler getHandler() {
        return mHandler;

    }

    private final Handler mHandler = new Handler(new Handler.Callback() {

        @SuppressWarnings("unchecked")
        @Override
        public boolean handleMessage(Message msg) {
            Commons.print(this.getClass().getName() + "mHandler.handleMessage("
                    + msg + ")");

            switch (msg.what) {

                case DataMembers.NOTIFY_ORDER_SAVED:
                    if (progressDialog != null)
                        progressDialog.dismiss();
                    return true;
                case DataMembers.NOTIFY_ORDER_DELETED:
                    try {
                        if (progressDialog != null)
                            progressDialog.dismiss();
                        bmodel = (BusinessModel) getApplicationContext();
                        bmodel.showAlert(
                                getResources().getString(
                                        R.string.order_deleted_sucessfully)
                                        + OrderHelper.getInstance(GhanaPrintPreviewActivity.this).getOrderId(),
                                DataMembers.NOTIFY_ORDER_SAVED);
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                    return true;

                case DataMembers.NOTIFY_INVOICE_SAVED:
                    try {
                        if (progressDialog != null)
                            progressDialog.dismiss();
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
                                doConnection(ZEBRA_3INCH);
                                Looper.loop();
                                Looper.myLooper().quit();
                            }
                        }).start();
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                    return true;
            }
            return false;
        }
    });

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_print_preview, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            if (!IsFromReport) {
                // Clear the Values in Objects
                bmodel.productHelper.clearOrderTable();
                if (bmodel.configurationMasterHelper.SHOW_PRODUCTRETURN)
                    bmodel.productHelper.clearBomReturnProductsTable();
            }

            if (IsFromReport) {
                finish();
            } else {
                bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                        .now(SDUtil.TIME));
                finish();
                BusinessModel.loadActivity(this, DataMembers.actHomeScreenTwo);
            }
            return true;
        } else if (i == R.id.menu_print) {
            new Thread(new Runnable() {
                public void run() {
                    Looper.prepare();
                    doConnection(ZEBRA_3INCH);
                    Looper.loop();
                    Looper.myLooper().quit();
                }
            }).start();

            return true;
        }
        return false;
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

    public ZebraPrinter connect() {
        // setStatus("Connecting...", Color.YELLOW);
        zebraPrinterConnection = null;
        // if (isBluetoothSelected()) {
        zebraPrinterConnection = new BluetoothConnection(
                getMacAddressFieldText());
        SettingsHelper.saveBluetoothAddress(this, getMacAddressFieldText());

        Commons.print("PRINT MAC : " + getMacAddressFieldText());
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
                printer = ZebraPrinterFactory.getInstance(PrinterLanguage.CPCL,
                        zebraPrinterConnection);
                setStatus("Determining Printer Language", Color.YELLOW);
                PrinterLanguage pl = printer.getPrinterControlLanguage();
                setStatus("Printer Language " + pl, Color.BLUE);

                isPrinterLanguageDetected = true;
            } catch (ConnectionException e) {
                setStatus("PrinterConnectionException", Color.RED);

                Commons.printException(e);
                // printer = null;
                // DemoSleeper.sleep(1000);
                // disconnect();
                isPrinterLanguageDetected = false;
            }
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
            if (printername.equals(ZEBRA_3INCH)) {

                for (int i = 0; i < SDUtil.convertToInt(count); i++) {
                    if (i == 0)
                        IsOriginal = true;
                    else {
                        IsOriginal = false;
                    }
                    zebraPrinterConnection.write(printDatafor3inchprinter());
                }

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
                int x = 340;
                int schemeSize = 0;
                // update free product size
                SchemeDetailsMasterHelper schemeHelper=SchemeDetailsMasterHelper.getInstance(getApplicationContext());
                if (schemeHelper.IS_SCHEME_ON) {
                    for (ProductMasterBO product : mProductsForAdapter) {
                        if (product.isPromo()) {
                            if (product.getSchemeProducts() != null) {
                                schemeSize = schemeSize
                                        + product.getSchemeProducts().size();
                            }
                        }
                    }
                }
                height = x
                        + (mProductsForAdapter.size() + schemeSize
                        + mEmptyLiaProductsForAdapter.size() + mEmptyRetProductsForAdapter
                        .size()) * 50 + 800;
                if (bmodel.configurationMasterHelper.TAX_SHOW_INVOICE) {
                    height = height + 200;
                }

                String Printitem = "! 0 200 200 " + height + " 1\r\n"
                        + "CENTER\r\n";
                Printitem += ExtractGraphicsDataForCPCL(0, 0);

                Printitem += "T 5 1 10 140 " + "" + mDistName + "\r\n";

                Printitem += "T 5 0 10 190 " + ""
                        + getResources().getString(R.string.tin_no)
                        + mDistTinNo + "\r\n";

                // T- Text
                //
                // Font Size
                // Spacing
                // height between lines

                Printitem += "T 5 0 10 210 --------------------------------------------------\r\n";

                Printitem += "\r\n";
                Printitem += "LEFT \r\n";

                Printitem += "T 5 0 113 231 " + "o" + " \r\n";

                Printitem += "T 5 0 10 240 " + "Invoice N " + " :" + mInvoiceNo
                        + "\r\n";

                Printitem += "\r\n";
                Printitem += "LEFT \r\n";
                Printitem += "T 5 0 10 270 "
                        + getResources().getString(R.string.salesman) + ":"
                        + mSalesManCode + "-" + mSalesMan + "\r\n";

                Printitem += "\r\n";
                Printitem += "LEFT \r\n";
                Printitem += "T 5 0 10 300 "
                        + getResources().getString(R.string.customer) + ""
                        + mCustomerCode + "-" + mCustomerName + "\r\n";

                Printitem += "\r\n";
                Printitem += "LEFT \r\n";
                Printitem += "T 5 0 10 330 "
                        + getResources().getString(R.string.sales_date) + ":"
                        + mSalesDate + "\r\n";

                Printitem += "\r\n";
                Printitem += "LEFT \r\n";
                Printitem += "T 5 0 10 360 "
                        + getResources().getString(R.string.Address) + ":"
                        + bmodel.getRetailerMasterBO().getAddress1() + "\r\n";

                Printitem += "\r\n";
                Printitem += "LEFT \r\n";
                Printitem += "T 5 0 65 380 " + "o" + " \r\n";

                Printitem += "T 5 0 10 391 " + "Tel N " + " : "
                        + bmodel.getRetailerMasterBO().getContactnumber()
                        + "\r\n";

				/*
                 * Printitem += "T 5 0 10 390 " +
				 * getResources().getString(R.string.tel) + ":" +
				 * bmodel.getRetailerMasterBO().getContactnumber() + "\r\n";
				 */

                // Need To enable
                /*
				 * Printitem += "\r\n"; Printitem += "T 5 0 10 330 " +
				 * getResources().getString(R.string.tin) + ":" +
				 * bmodel.getRetailerMasterBO().getContactnumber() + "\r\n";
				 */

                if (IsOriginal) {
                    Printitem += "T 5 1 320 390 " + "" + "(Original Invoice)"
                            + "\r\n";
                } else {
                    Printitem += "T 5 1 300 390 " + "" + "(Duplicate Invoice)"
                            + "\r\n";
                }

                Printitem += "T 5 0 10 430 --------------------------------------------------\r\n";

                Printitem += "\r\n";
                Printitem += "LEFT \r\n";

                Printitem += "T 5 0 10 450 "
                        + getResources().getString(R.string.brand)
                        + getResources().getString(R.string.Name) + "\r\n";

                Printitem += "T 5 0 220 450 "
                        + getResources().getString(R.string.case_u) + "\r\n";
                Printitem += "T 5 0 280 450 "
                        + getResources().getString(R.string.bottle) + "\r\n";

                Printitem += "T 5 0 360 450 "
                        + getResources().getString(R.string.unit_price)
                        + "\r\n";
                Printitem += "T 5 0 450 450 "
                        + getResources().getString(R.string.Amount) + "\r\n";

                Printitem += "T 5 0 10 470 --------------------------------------------------\r\n";
                x += 150;
                for (ProductMasterBO productBO : mProductsForAdapter) {

                    x += 20;

                    String productname = "";
                    // For Printer Space issue , restriced to 10 character.
                    if (productBO.getProductShortName() != null
                            && !productBO.getProductShortName().equals("")
                            && !productBO.getProductShortName().equals("null")) {
                        if (productBO.getProductShortName().length() > 18)
                            productname = productBO.getProductShortName()
                                    .substring(0, 18);
                        else
                            productname = productBO.getProductShortName();
                    } else {
                        if (productBO.getProductName().length() > 18)
                            productname = productBO.getProductName().substring(
                                    0, 18);
                        else
                            productname = productBO.getProductName();
                    }

                    Printitem += "T 5 0 10 " + x + " "
                            + productname.toLowerCase() + "\r\n";
                    Printitem += "\r\n";

					/* x += 30; */
                    Printitem += "T 5 0 240 " + x + " "
                            + productBO.getOrderedCaseQty() + "\r\n";

                    Printitem += "T 5 0 290 " + x + " "
                            + productBO.getOrderedPcsQty() + "\r\n";

                    if (productBO.getOrderedCaseQty() > 0)
                        Printitem += "T 5 0 360 " + x + " "
                                + bmodel.formatValue(productBO.getCsrp())
                                + "\r\n";
                    else
                        Printitem += "T 5 0 360 " + x + " "
                                + bmodel.formatValue(productBO.getSrp())
                                + "\r\n";

                    double totalProdVal = (productBO.getOrderedOuterQty() * productBO
                            .getOsrp())
                            + (productBO.getOrderedCaseQty() * productBO
                            .getCsrp())
                            + (productBO.getOrderedPcsQty() * productBO
                            .getSrp());

                    Printitem += "RIGHT \r\n";

                    Printitem += "T 5 0 450 " + x + " "
                            + bmodel.formatValue(totalProdVal) + "\r\n";
                    x += 10;
                    // print scheme free product starts
                    if (productBO.isPromo()) {
                        if (productBO.getSchemeProducts() != null) {

                            List<SchemeProductBO> freeProductList = productBO
                                    .getSchemeProducts();
                            if (freeProductList != null) {
                                for (SchemeProductBO schemeProductBO : freeProductList) {
                                    ProductMasterBO product = bmodel.productHelper
                                            .getProductMasterBOById(schemeProductBO
                                                    .getProductId());

                                    if (product != null) {
                                        x += 20;
                                        String pname = "";
                                        // For Printer Space issue , restriced
                                        // to 10 character.
                                        if (product.getProductShortName() != null
                                                && !product
                                                .getProductShortName()
                                                .equals("")
                                                && !product
                                                .getProductShortName()
                                                .equals("null")) {
                                            if (product.getProductShortName()
                                                    .length() > 18)
                                                pname = product
                                                        .getProductShortName()
                                                        .substring(0, 18);
                                            else
                                                pname = product
                                                        .getProductShortName();
                                        } else {
                                            if (product.getProductName()
                                                    .length() > 18)
                                                pname = product
                                                        .getProductName()
                                                        .substring(0, 18);
                                            else
                                                pname = product
                                                        .getProductName();
                                        }
                                        Printitem += "T 5 0 10 " + x + " "
                                                + pname.toLowerCase() + "\r\n";
                                        Printitem += "\r\n";
                                        if (product.getCaseUomId() == schemeProductBO
                                                .getUomID()
                                                && product.getCaseUomId() != 0) {

                                            // x += 30;
                                            Printitem += "T 5 0 240 "
                                                    + x
                                                    + " "
                                                    + schemeProductBO
                                                    .getQuantitySelected()
                                                    + "\r\n";

                                            Printitem += "T 5 0 290 " + x + " "
                                                    + 0 + "\r\n";
                                            // case wise free quantity update

                                        } else if (product.getOuUomid() == schemeProductBO
                                                .getUomID()
                                                && product.getOuUomid() != 0) {
                                            // outer wise free quantity update

                                        } else {
                                            // x += 30;
                                            Printitem += "T 5 0 240 " + x + " "
                                                    + 0 + "\r\n";

                                            Printitem += "T 5 0 290 "
                                                    + x
                                                    + " "
                                                    + schemeProductBO
                                                    .getQuantitySelected()
                                                    + "\r\n";
                                        }
                                        x += 10;
                                    }
                                }
                            }

                        }
                    }
                    // print scheme free product ends

                }
                x += 30;
                Printitem += "T 5 0 10 "
                        + x
                        + " --------------------------------------------------\r\n";

                x += 30;
                Printitem += "T 5 0 10 " + x
                        + getResources().getString(R.string.total_stock_sold)
                        + "\r\n";

                Printitem += "RIGHT \r\n";
                Printitem += "T 5 0 450 " + x + " "
                        + bmodel.formatValue(mTotalOrderValue) + "\r\n";

                x += 20;
                Printitem += "T 5 0 10 "
                        + x
                        + " --------------------------------------------------\r\n";

                if (bmodel.configurationMasterHelper.TAX_SHOW_INVOICE) {

                    x += 30;
                    Printitem += "T 5 0 10 " + x
                            + getResources().getString(R.string.total_exec_tax)
                            + "\r\n";

                    Printitem += "RIGHT \r\n";
                    Printitem += "T 5 0 450 " + x + " "
                            + bmodel.formatValue(mtotalExcludeTaxAmount)
                            + "\r\n";
                    x += 20;
                    Printitem += "T 5 0 10 "
                            + x
                            + " --------------------------------------------------\r\n";
                    x += 30;
                    Printitem += "T 5 0 10 " + x + mVatName + "("
                            + vatPercentage + "%)" + "\r\n";

                    Printitem += "T 5 0 450 " + x + " "
                            + bmodel.formatValue(mVatValue) + "\r\n";

                    x += 20;
                    Printitem += "T 5 0 10 "
                            + x
                            + " --------------------------------------------------\r\n";
                    if (mNhilName != "") {
                        x += 30;
                        Printitem += "T 5 0 10 " + x + mNhilName + "("
                                + nhlPercentage + "%)" + "\r\n";

                        Printitem += "T 5 0 450 " + x + " "
                                + bmodel.formatValue(mNhlValue) + "\r\n";

                        x += 20;
                        Printitem += "T 5 0 10 "
                                + x
                                + " --------------------------------------------------\r\n";
                    }

                }
                x += 30;
                for (BomReturnBO productBO : mEmptyLiaProductsForAdapter) {

                    if ((productBO.getLiableQty() > 0)) {
                        x += 20;

                        String productname = "";
                        // For Printer Space issue , restriced to 10 character.
                        if (productBO.getProductShortName() != null
                                && !productBO.getProductShortName().equals("")
                                && !productBO.getProductShortName().equals(
                                "null")) {
                            if (productBO.getProductShortName().length() > 18)
                                productname = productBO.getProductShortName()
                                        .substring(0, 18);
                            else
                                productname = productBO.getProductShortName();
                        } else {
                            if (productBO.getProductName().length() > 18)
                                productname = productBO.getProductName()
                                        .substring(0, 18);
                            else
                                productname = productBO.getProductName();
                        }

                        Printitem += "T 5 0 10 " + x + " " + "ED-"
                                + productname.toLowerCase() + "\r\n";
                        Printitem += "\r\n";

						/* x += 30; */
						/* Printitem += "T 5 0 240 " + x + " " + "0" + "\r\n"; */

                        Printitem += "T 5 0 290 " + x + " "
                                + productBO.getLiableQty() + "\r\n";

                        Printitem += "T 5 0 360 " + x + " "
                                + bmodel.formatValue(productBO.getpSrp())
                                + "\r\n";

                        double totalEmpVal = (productBO.getLiableQty() * productBO
                                .getpSrp());

						/*
						 * Printitem += "T 5 0 450 " + x + " " +
						 * bmodel.formatValue(totalEmpVal) + "\r\n";
						 */

                        Printitem += "T 5 0 "
                                + (450 + 7 - (totalEmpVal + "").length()) + " "
                                + x + " " + bmodel.formatValue(totalEmpVal)
                                + "\r\n";

                        x += 10;
                    }

                }
                for (BomReturnBO productBO2 : mEmptyRetProductsForAdapter) {
                    if ((productBO2.getReturnQty() > 0)) {
                        x += 20;

                        String productname = "";
                        // For Printer Space issue , restriced to 10 character.
                        if (productBO2.getProductShortName() != null
                                && !productBO2.getProductShortName().equals("")
                                && !productBO2.getProductShortName().equals(
                                "null")) {
                            if (productBO2.getProductShortName().length() > 18)
                                productname = productBO2.getProductShortName()
                                        .substring(0, 18);
                            else
                                productname = productBO2.getProductShortName();
                        } else {
                            if (productBO2.getProductName().length() > 18) {
                                productname = productBO2.getProductName()
                                        .substring(0, 18);
                            } else {
                                productname = productBO2.getProductName();
                            }
                        }

                        Printitem += "T 5 0 10 " + x + " " + "ER-"
                                + productname.toLowerCase() + "\r\n";
                        Printitem += "\r\n";

						/* x += 30; */
						/* Printitem += "T 5 0 240 " + x + " " + "0" + "\r\n"; */

                        Printitem += "T 5 0 290 " + x + " "
                                + productBO2.getReturnQty() + "\r\n";

                        Printitem += "T 5 0 360 " + x + " "
                                + bmodel.formatValue(productBO2.getpSrp())
                                + "\r\n";

                        double totalEmpVal = (productBO2.getReturnQty() * productBO2
                                .getpSrp());
                        rightAlign(totalEmpVal + "");
                        Printitem += "T 5 0 430 " + x + "-"
                                + (bmodel.formatValue(totalEmpVal)) + "\r\n";
                        x += 10;
                    }

                }
                x += 20;
                Printitem += "T 5 0 10 "
                        + x
                        + " --------------------------------------------------\r\n";
                x += 30;
                Printitem += "T 5 0 10 " + x
                        + getResources().getString(R.string.totempties)
                        + " Charged" + "\r\n";

                Printitem += "T 5 0 450 " + x + " "
                        + bmodel.formatValue(mEmpTotalValue) + "\r\n";

                x += 20;
                Printitem += "T 5 0 10 "
                        + x
                        + " --------------------------------------------------\r\n";
                x += 30;
                Printitem += "T 5 0 10 " + x
                        + getResources().getString(R.string.totduecoll)
                        + "\r\n";

                Printitem += "T 5 0 450 " + x + " "
                        + bmodel.formatValue(mTotalOrderValue + mEmpTotalValue)
                        + "\r\n";

                x += 20;
                Printitem += "T 5 0 10 "
                        + x
                        + " --------------------------------------------------\r\n";

                if (bmodel.configurationMasterHelper.SHOW_COLLECTION_BEFORE_INVOICE) {
                    x += 30;
                    Printitem += "T 5 0 210 " + x + "Total Paid" + "\r\n";
                    if (mCash > 0) {
                        Printitem += "T 5 0 350 " + x + "(Cash)" + "\r\n";

                        Printitem += "T 5 0 450 " + x + " "
                                + bmodel.formatValue(mCash) + "\r\n";
                    }
					/*
					 * x += 30; Printitem += "T 5 0 10 " + x +
					 * " --------------------------------------------------\r\n"
					 * ;
					 */
                    if (mCheque > 0) {
                        x += 30;
                        Printitem += "T 5 0 350 " + x + "("
                                + getResources().getString(R.string.cheque)
                                + ")" + "\r\n";

                        Printitem += "T 5 0 450 " + x + " "
                                + bmodel.formatValue(mCheque) + "\r\n";
                    }
                } else {
                    x += 30;
                    Printitem += "T 5 0 10 " + x
                            + getResources().getString(R.string.total_paid)
                            + "\r\n";

                    Printitem += "T 5 0 350 " + x
                            + getResources().getString(R.string.cash) + "\r\n";

                    Printitem += "T 5 0 450 " + x + " "
                            + bmodel.formatValue(mCash) + "\r\n";

                    x += 20;
                    Printitem += "T 5 0 10 "
                            + x
                            + " --------------------------------------------------\r\n";

                    x += 30;
                    Printitem += "T 5 0 350 " + x
                            + getResources().getString(R.string.cheque)
                            + "\r\n";

                    Printitem += "T 5 0 450 " + x + " "
                            + bmodel.formatValue(mCheque) + "\r\n";
                }
                x += 20;
                Printitem += "T 5 0 10 "
                        + x
                        + " --------------------------------------------------\r\n";
                x += 30;
                Printitem += "T 5 0 350 " + x
                        + getResources().getString(R.string.credit) + "\r\n";

                Printitem += "T 5 0 450 " + x + " "
                        + bmodel.formatValue(mTotCredit) + "\r\n";

                x += 20;
                Printitem += "T 5 0 10 "
                        + x
                        + " --------------------------------------------------\r\n";

                x += 100;

                Printitem += "\r\n";
                Printitem += "\r\n";
                Printitem += "\r\n";

                Printitem += "T 5 0 10 " + x
                        + getResources().getString(R.string.customer_sign)
                        + "\r\n";
                Printitem += "T 5 0 180 " + x + " --------\r\n";

                Printitem += "T 5 0 330 " + x
                        + getResources().getString(R.string.rep_sign) + "\r\n";
                Printitem += "T 5 0 420 " + x + "--------\r\n";

                Printitem += "PRINT\r\n";

                PrintDataBytes = Printitem.getBytes();
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
        return PrintDataBytes;
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
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindDrawables(findViewById(R.id.root));
        // disconnect();
        // bmodel.productHelper.clearOrderTable();
        // force the garbage collector to run
        System.gc();
    }

    /**
     * set values in product list
     **/
    private void updateTaxDetails() {

        ArrayList<Float> taxRateList = new ArrayList<Float>();
        ArrayList<String> taxNameList = new ArrayList<String>();
        try {
            DBUtil db = new DBUtil(this, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            String sql = "Select TM.taxrate,SLM.ListName from TaxMaster TM INNER JOIN  standardlistmaster  SLM on TM.TaxType = SLM.ListID where applylevelid in (select listid from standardlistmaster where  listcode='BILL') limit 2";
            Cursor c = db.selectSQL(sql);
            if (c != null) {
                while (c.moveToNext()) {
                    taxRateList.add(c.getFloat(0));
                    taxNameList.add(c.getString(1));
                }
            }
            c.close();
            db.closeDB();
            int taxsize = taxRateList.size();
            if (taxsize > 0) {
                if (taxsize == 2) {
                    vatPercentage = taxRateList.get(0);
                    nhlPercentage = taxRateList.get(1);
                } else if (taxsize == 1) {
                    vatPercentage = taxRateList.get(0);
                    nhlPercentage = 0;
                }
            }
            int taxNamesize = taxNameList.size();
            if (taxNamesize > 0) {
                if (taxNamesize == 2) {
                    mVatName = taxNameList.get(0);
                    mNhilName = taxNameList.get(1);
                } else if (taxNamesize == 1) {
                    mVatName = taxNameList.get(0);
                    mNhilName = "";
                }
            }

        } catch (Exception e) {
            // TODO: handle exception
            Commons.printException(e);
        }

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
            m_data = "";
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

    private void rightAlign(String str) {

        if (str.length() < 6)
            space = (6 - str.length()) * 6 + 7;
        Commons.print("space" + " " + space);
    }
}
