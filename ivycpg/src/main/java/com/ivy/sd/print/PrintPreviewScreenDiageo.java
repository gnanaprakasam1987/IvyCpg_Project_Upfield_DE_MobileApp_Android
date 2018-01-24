package com.ivy.sd.print;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
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
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.BeatMasterBO;
import com.ivy.sd.png.bo.BomRetunBo;
import com.ivy.sd.png.bo.PaymentBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.SchemeProductBO;
import com.ivy.sd.png.bo.TaxBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.TaxInterface;
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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Vector;

public class PrintPreviewScreenDiageo extends IvyBaseActivityNoActionBar {

    private TextView distName, distadd, distTelno, invoiceno, customername,
            address, phcontact, salesdate, totFullStockCs, totFullStockPc,
            totEmpty, totCol, totVat, totNhl, totCollDue, cash, check,
            totCredit, totVatLabel, totNhlLabel;
    private String mInvoiceno, mCustomername, mPhcontact, mSalesdate;

    private ListView lvwplist;
    private BusinessModel bmodel;
    private Vector<ProductMasterBO> mProducts = new Vector<ProductMasterBO>();
    private ArrayList<ProductMasterBO> mProductsForAdapter = new ArrayList<ProductMasterBO>();
    private ArrayList<BomRetunBo> mEmptyProducts = new ArrayList<BomRetunBo>();
    private ArrayList<BomRetunBo> mEmptyLiaProductsForAdapter = new ArrayList<BomRetunBo>();
    private ArrayList<BomRetunBo> mEmptyRetProductsForAdapter = new ArrayList<BomRetunBo>();
    private ArrayList<TaxBO> mTax = new ArrayList<TaxBO>();
    private ImageView imagevw;
    private boolean IsFromOrder, IsFromReport, IsOriginal;
    private ProgressDialog pd;
    private static final int SELECTED_PRINTER_DIALOG = 1;
    private static final String ZEBRA_2INCH = "2";
    private static final String ZEBRA_3INCH = "3";
    private static final String ZEBRA_4INCH = "4";
    private Connection zebraPrinterConnection;
    private final String[] mPrinterSelectionArray = {ZEBRA_2INCH, ZEBRA_3INCH,
            ZEBRA_4INCH};
    private String mSelectedPrinterName;
    private static final String TAG = "InvoicePrint";
    private String count;
    private ZebraPrinter printer;
    private EditText mMacAddressET;
    private TextView statusField;
    private ImageView mStatusIV;
    private double mCaseTotalValue = 0, mPcTotalValue = 0, mEmpTotalValue = 0,
            mTotalColValue, totalProd, totalEmp, mVatValue = 0, mNhlValue = 0,
            mTotColDueValue, mCash, mCheque, mTotCredit,mCreditNoteValue,mSchemeDiscount;
    private LinearLayout mTaxLayout, mProductContainerLL,
            mEmpProductContainerLL;
    private Spinner printcount;
    private ArrayAdapter<CharSequence> spinadapter;
    private ArrayList<ProductMasterBO> batchproducts;
    private String m_data;
    private Bitmap m_bmp;
    private int totcase = 0, totpcs = 0, totouter = 0;
    private SchemeProductBO schemebo;
    private double saleablevalue;
    private float mfreegoods;
    private String storediscount = "0";
    private double vatPercentage = 0, nhlPercentage = 0;
    private String mVatName = "", mNhilName = "";
    private Toolbar toolbar;
    TextView tv_scheme_discount;

    private int printDoneCount = 0;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_preview_zebra_diageo);

        try {
            bmodel = (BusinessModel) getApplicationContext();
            bmodel.setContext(this);
            Bundle extras = getIntent().getExtras();
            if (extras != null) {

                if (extras.containsKey("print_count"))
                    printDoneCount = extras.getInt("print_count");
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
            distName = (TextView) findViewById(R.id.distname);
            distadd = (TextView) findViewById(R.id.distadd);
            distTelno = (TextView) findViewById(R.id.disttel);

            invoiceno = (TextView) findViewById(R.id.invoiceno);
            customername = (TextView) findViewById(R.id.customer);
            address = (TextView) findViewById(R.id.address);
            phcontact = (TextView) findViewById(R.id.telno);
            salesdate = (TextView) findViewById(R.id.salesdate);
            lvwplist = (ListView) findViewById(R.id.product_list_lv);
            lvwplist.setCacheColorHint(0);
            imagevw = (ImageView) findViewById(R.id.imgvw);
            imagevw.setImageBitmap(setIcon());
            mMacAddressET = (EditText) findViewById(R.id.et_mac);
            statusField = (TextView) findViewById(R.id.status_bar);
            mStatusIV = (ImageView) findViewById(R.id.status_iv);
            mTaxLayout = (LinearLayout) findViewById(R.id.ll_taxlayout);
            mProductContainerLL = (LinearLayout) findViewById(R.id.product_container_ll);
            mEmpProductContainerLL = (LinearLayout) findViewById(R.id.emp_product_container_ll);
            tv_scheme_discount=(TextView) findViewById(R.id.tv_scheme_discount);

            totFullStockCs = (TextView) findViewById(R.id.fullstockcstot);
            totFullStockPc = (TextView) findViewById(R.id.fullstockbtltot);
            totEmpty = (TextView) findViewById(R.id.totalempbtltot);
            totCol = (TextView) findViewById(R.id.totalemptot);
            totVat = (TextView) findViewById(R.id.vat);
            totNhl = (TextView) findViewById(R.id.nhl);
            totCollDue = (TextView) findViewById(R.id.collectionDue);
            cash = (TextView) findViewById(R.id.cash);
            check = (TextView) findViewById(R.id.check);
            totCredit = (TextView) findViewById(R.id.totcredit);
            totVatLabel = (TextView) findViewById(R.id.vatlabel);
            totNhlLabel = (TextView) findViewById(R.id.nhllabel);

            if (bmodel.configurationMasterHelper.TAX_SHOW_INVOICE) {
                mTaxLayout.setVisibility(View.VISIBLE);
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
        doInitialize();
    }

    private void doInitialize() {
        try {
            mPhcontact = bmodel.userMasterHelper.getUserMasterBO()
                    .getDistributorContactNumber();
            mCustomername = bmodel.getRetailerMasterBO().getRetailerName();
            mSalesdate = DateUtil.convertFromServerDateToRequestedFormat(
                    SDUtil.now(SDUtil.DATE_GLOBAL),
                    bmodel.configurationMasterHelper.outDateFormat);
            mInvoiceno = bmodel.invoiceNumber;

            /** set values in textview **/

            distName.setText(bmodel.userMasterHelper.getUserMasterBO()
                    .getDistributorName());
            /*
             * distadd.setText(bmodel.userMasterHelper.getUserMasterBO()
			 * .getDistributorName());
			 */
            distTelno.setText(mPhcontact);

            phcontact.setText(bmodel.getRetailerMasterBO().getContactnumber());
            customername.setText(mCustomername);
            address.setText(bmodel.getRetailerMasterBO().getAddress1());
            salesdate.setText(mSalesdate);
            invoiceno.setText(mInvoiceno);
            mProducts = bmodel.productHelper.getProductMaster();
            if (bmodel.configurationMasterHelper.SHOW_GROUPPRODUCTRETURN)
                mEmptyProducts = bmodel.productHelper
                        .getBomReturnTypeProducts();
            else
                mEmptyProducts = bmodel.productHelper.getBomReturnProducts();

            mTax = bmodel.productHelper.taxHelper.getBillTaxList();

            // saleablevalue = bmodel.salesReturnHelper.saleablevalue;
            updateproducts();
            updateEmptiesproducts();
            updateTaxDetails();

            mTotalColValue = mCaseTotalValue + mPcTotalValue + mEmpTotalValue - bmodel.productHelper.getSchemeAmount(mInvoiceno);

            if (bmodel.configurationMasterHelper.TAX_SHOW_INVOICE) {
                mVatValue = ((SDUtil.convertToDouble(bmodel
                        .formatValue(mTotalColValue)) * vatPercentage) / 100);
                mNhlValue = ((SDUtil.convertToDouble(bmodel
                        .formatValue(mTotalColValue)) * nhlPercentage) / 100);
            }

            mTotColDueValue = SDUtil.convertToDouble(bmodel
                    .formatValue(mTotalColValue))
                    + SDUtil.convertToDouble(bmodel.formatValue(mVatValue))
                    + SDUtil.convertToDouble(bmodel.formatValue(mNhlValue));

            mCash = 0;
            mCheque = 0;
            mCreditNoteValue=0;
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
                    }else if(payment.get(0).getCashMode().equals(StandardListMasterConstants.CREDIT_NOTE)){
                        mCreditNoteValue=payment.get(0).getAmount();
                    }

                }
            }
            mTotCredit = mTotColDueValue - mCash - mCheque-mCreditNoteValue;

            // ,,mTotColDueValue,mCash,mCheque,mtotCredit

            totFullStockCs.setText(bmodel
                    .formatValue(mCaseTotalValue) + "");
            totFullStockPc.setText(bmodel
                    .formatValue(mPcTotalValue) + "");
            totEmpty.setText(mEmpTotalValue + "");
            totCol.setText(bmodel
                    .formatValue(mTotalColValue) + "");
            tv_scheme_discount.setText(bmodel
                    .formatValue(mSchemeDiscount)+"");

            if (mVatName != "") {
                totVatLabel.setText(mVatName + "(" + vatPercentage + "%)");
                totVat.setText(bmodel
                        .formatValue(mVatValue) + "");
            } else {
                totVatLabel.setVisibility(View.GONE);
                totVat.setVisibility(View.GONE);
            }
            if (mNhilName != "") {
                totNhlLabel.setText(mNhilName + "(" + nhlPercentage + "%)");
                totNhl.setText(bmodel
                        .formatValue(mNhlValue) + "");
            } else {
                totNhlLabel.setVisibility(View.GONE);
                totNhl.setVisibility(View.GONE);
            }

            totCollDue.setText(bmodel
                    .formatValue(mTotColDueValue) + "");
            if (bmodel.configurationMasterHelper.SHOW_COLLECTION_BEFORE_INVOICE) {
                if (mCash > 0) {
                    cash.setText(bmodel.formatValue(mCash) + "");
                    findViewById(R.id.ll_layout_cheque).setVisibility(View.GONE);
                    findViewById(R.id.ll_cheque_line).setVisibility(View.GONE);
                } else if (mCheque > 0) {
                    findViewById(R.id.ll_layout_cash).setVisibility(View.GONE);
                    check.setText(bmodel.formatValue(mCheque)
                            + "");
                    findViewById(R.id.ll_cash_line).setVisibility(View.GONE);
                }
            } else {
                cash.setText(bmodel.formatValue(mCash) + "");
                check.setText(bmodel.formatValue(mCheque)
                        + "");
            }
            totCredit.setText(bmodel
                    .formatValue(mTotCredit) + "");

        } catch (Exception e) {
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
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
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
    private void updateTaxDetails() {

        ArrayList<Float> taxRateList = new ArrayList<Float>();
        ArrayList<String> taxNameList = new ArrayList<String>();
        try {
            DBUtil db = new DBUtil(this, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            // String sql =
            // "Select taxrate from TaxMaster where applylevelid in (select listid from standardlistmaster where  listcode='BILL') limit 2";
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

					/*
                     * totalProd = (productBO.getOrderedOuterQty() * productBO
					 * .getOsrp()) + (productBO.getOrderedCaseQty() * productBO
					 * .getCsrp()) + (productBO.getOrderedPcsQty() * productBO
					 * .getSrp());
					 */


                    View v = null;

                    v = inflater.inflate(
                            R.layout.row_print_preview_diageo_batchwise_header, null);

                    ((TextView) v.findViewById(R.id.productcode))
                            .setText(productBO.getProductCode() + "");
                    ((TextView) v.findViewById(R.id.product_name_tv))
                            .setText(productBO.getProductShortName() + "");
                    LinearLayout batchLayout = (LinearLayout) v.findViewById(R.id.batchlayout);
                    if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION
                            && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
                        ArrayList<ProductMasterBO> batchList = bmodel.batchAllocationHelper.getBatchlistByProductID().get(productBO.getProductID());
                        if (batchList != null && batchList.size() > 0) {
                            batchLayout.removeAllViews();
                            for (ProductMasterBO batchBO : batchList) {
                                if (batchBO.getOrderedPcsQty() > 0
                                        || batchBO
                                        .getOrderedCaseQty() > 0
                                        || batchBO
                                        .getOrderedOuterQty() > 0) {

                                    batchLayout.addView(getProductsDetailView(batchBO, true));


                                }
                            }
                        } else {
                            batchLayout.addView(getProductsDetailView(productBO, false));
                        }
                    } else {

                        batchLayout.addView(getProductsDetailView(productBO, false));
                    }
                    mProductContainerLL.addView(v);
                    // free products added to display
                    if (bmodel.configurationMasterHelper.IS_SCHEME_ON) {
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

    private View getProductsDetailView(ProductMasterBO productBO, boolean isBatch) {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(
                R.layout.row_print_preview_diageo_batchwise, null);
        try {
            if (isBatch)
                ((TextView) view.findViewById(R.id.batch_name_tv)).setText("Batch: " + productBO.getBatchNo());

            ((TextView) view.findViewById(R.id.caseqty)).setText(bmodel
                    .formatValue(productBO.getOrderedCaseQty()) + "");
            ((TextView) view.findViewById(R.id.pieceqty)).setText(bmodel
                    .formatValue(productBO.getOrderedPcsQty()) + "");
            if (productBO.getOrderedCaseQty() > 0)
                ((TextView) view.findViewById(R.id.unitprice))
                        .setText(bmodel.formatValue(productBO.getCsrp())
                                + "");
            else
                ((TextView) view.findViewById(R.id.unitprice))
                        .setText(bmodel.formatValue(productBO.getSrp())
                                + "");
            ((TextView) view.findViewById(R.id.amount)).setText(bmodel
                    .formatValue(productBO.getDiscount_order_value())
                    + "");


            //
            if (productBO.getOrderedCaseQty() > 0) {

                mCaseTotalValue = mCaseTotalValue
                        + (productBO.getOrderedCaseQty() * productBO.getCsrp());
            }

            if (productBO.getOrderedPcsQty() > 0) {
                mPcTotalValue = mPcTotalValue
                        + (productBO.getOrderedPcsQty() * productBO.getSrp());
            }

            mSchemeDiscount+=productBO.getSchemeDiscAmount();

        } catch (Exception ex) {

        }
        return view;
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

    private void printFreeProduct(ProductMasterBO productBo, int x,
                                  String Printitem) {
        List<SchemeProductBO> freeProductList = productBo.getSchemeProducts();
        if (freeProductList != null) {
            for (SchemeProductBO schemeProductBO : freeProductList) {
                ProductMasterBO productBO = bmodel.productHelper
                        .getProductMasterBOById(schemeProductBO.getProductId());

                if (productBO != null) {
                    x += 20;
                    if (productBO.getCaseUomId() == schemeProductBO.getUomID()
                            && productBO.getCaseUomId() != 0) {

						/* x += 30; */
                        Printitem += "T 5 0 240 " + x + " "
                                + schemeProductBO.getQuantitySelected()
                                + "\r\n";

                        Printitem += "T 5 0 290 " + x + " " + 0 + "\r\n";
                        // case wise free quantity update

                    } else if (productBO.getOuUomid() == schemeProductBO
                            .getUomID() && productBO.getOuUomid() != 0) {
                        // outer wise free quantity update

                    } else {
						/* x += 30; */
                        Printitem += "T 5 0 240 " + x + " " + 0 + "\r\n";

                        Printitem += "T 5 0 290 " + x + " "
                                + schemeProductBO.getQuantitySelected()
                                + "\r\n";
                    }
                    x += 10;
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
                Collections.sort(mEmptyProducts, BomRetunBo.SKUWiseAscending);
                for (BomRetunBo productBO : mEmptyProducts) {
                    if ((productBO.getLiableQty() > 0)) {
                        mEmptyLiaProductsForAdapter.add(productBO);
                        totalEmp = (productBO.getLiableQty() * productBO
                                .getpSrp());

                        mLiableTot = mLiableTot
                                + (productBO.getLiableQty() * productBO
                                .getpSrp());

                        // mEmpTotalValue = mEmpTotalValue
                        // + (productBO.getReturnQty() * productBO.getpSrp());

                        View v = inflater.inflate(
                                R.layout.row_print_preview_diageo, null);

                        ((TextView) v.findViewById(R.id.productcode))
                                .setText(productBO.getProdCode() + "");
                        ((TextView) v.findViewById(R.id.product_name_tv))
                                .setText(productBO.getProductShortName() + "-Liable");
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

            for (BomRetunBo productBO2 : mEmptyProducts) {
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
                    ((TextView) v.findViewById(R.id.product_name_tv)).setText(productBO2.getProductShortName()
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
            Commons.printException(e);
        }

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
                            .getUserid() + DataMembers.DIGITAL_CONTENT
                            + "/" + "receiptImg.png");
            Commons.print("file" + file.getAbsolutePath());
            if (file.exists()) {

                bit = BitmapFactory.decodeFile(file.getAbsolutePath());
            }

        } catch (Exception e) {
            Commons.printException(e);
        }
        return bit;
    }

    public Handler getHandler() {

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


            } else if (printername.equals(ZEBRA_3INCH)) {

                for (int i = 0; i < SDUtil.convertToInt(count); i++) {
                    if (i == 0 && printDoneCount <= 0)
                        IsOriginal = true;
                    else {
                        IsOriginal = false;
                    }
                    zebraPrinterConnection.write(printDatafor3inchprinter());
                }

                printDoneCount += SDUtil.convertToInt(count);
                bmodel.updatePrintCount(printDoneCount);
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

                for (int i = 0; i < SDUtil.convertToInt(count); i++) {
                    if (i == 0 && printDoneCount <= 0)
                        IsOriginal = true;
                    else {
                        IsOriginal = false;
                    }
                    zebraPrinterConnection.write(printDatafor4inchprinter());
                }

                printDoneCount += SDUtil.convertToInt(count);
                bmodel.updatePrintCount(printDoneCount);
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
                printer = ZebraPrinterFactory.getInstance(PrinterLanguage.CPCL,
                        zebraPrinterConnection);
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
                File file = new File(
                        getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                                + "/"
                                + bmodel.userMasterHelper.getUserMasterBO()
                                .getUserid()
                                + DataMembers.APP_DIGITAL_CONTENT + "/"
                                + "receiptImg.png");

                m_bmp = BitmapFactory.decodeFile(file.getAbsolutePath());

                int height = 0;
                int x = 340;
                int schemeSize = 0;
                int batchSize = 0;
                // update free product size
                if (bmodel.configurationMasterHelper.IS_SCHEME_ON) {
                    for (ProductMasterBO product : mProductsForAdapter) {
                        if (product.getIsscheme() == 1) {
                            if (product.getSchemeProducts() != null) {
                                schemeSize = schemeSize
                                        + product.getSchemeProducts().size();
                            }
                        }

                        //
                        if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION
                                && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
                            if (bmodel.batchAllocationHelper.getBatchlistByProductID().get(product.getProductID()) != null) {
                                for (ProductMasterBO batchBO : bmodel.batchAllocationHelper.getBatchlistByProductID().get(product.getProductID())) {
                                    if (batchBO.getOrderedPcsQty() > 0
                                            || batchBO
                                            .getOrderedCaseQty() > 0
                                            || batchBO
                                            .getOrderedOuterQty() > 0) {
                                        batchSize += 1;
                                    }
                                }
                            }
                        }
                    }
                }
                height = x
                        + (mProductsForAdapter.size() + schemeSize
                        + mEmptyLiaProductsForAdapter.size() + mEmptyRetProductsForAdapter
                        .size()) * 50 + 800 + (batchSize * 30);
                if (bmodel.configurationMasterHelper.TAX_SHOW_INVOICE) {
                    height = height + 200;
                }
                Commons.print(TAG + "Heigt:" + height);
                String Printitem = "! 0 200 200 " + height + " 1\r\n"
                        + "CENTER\r\n";
                Printitem += ExtractGraphicsDataForCPCL(0, 0);

                Printitem += "T 5 1 10 140 "
                        + ""
                        + bmodel.userMasterHelper.getUserMasterBO()
                        .getDistributorName() + "\r\n";

                Printitem += "T 5 0 10 180 " + "" + mPhcontact + "\r\n";

                // T- Text
                //
                // Font Size
                // Spacing
                // height between lines

                Printitem += "T 5 0 10 210 --------------------------------------------------\r\n";
                // Printitem += "T 5 0 10 80  "; /*
                // * "print distributor name and distributor address"
                // */
                //
                // Printitem += "\r\n" + "T 5 0 10 100  \r\n";
                Printitem += "\r\n";
                Printitem += "LEFT \r\n";
                Printitem += "T 5 0 10 240 "
                        + getResources().getString(R.string.invoice_no) + ":"
                        + mInvoiceno + "\r\n";

                Printitem += "T 5 0 350 240 "
                        + getResources().getString(R.string.sales_date) + ":"
                        + mSalesdate + "\r\n";

                Printitem += "LEFT \r\n";
                Printitem += "T 5 0 10 270 "
                        + getResources().getString(R.string.customer) + ""
                        + mCustomername + "\r\n";

                Printitem += "\r\n";
                Printitem += "LEFT \r\n";

                Printitem += "T 5 0 10 300 "
                        + getResources().getString(R.string.Address) + ":"
                        + bmodel.getRetailerMasterBO().getAddress1() + "\r\n";
                Printitem += "\r\n";
                Printitem += "T 5 0 10 330 "
                        + getResources().getString(R.string.tel) + ":"
                        + bmodel.getRetailerMasterBO().getContactnumber() + "\r\n";

                if (IsOriginal) {
                    Printitem += "T 5 1 320 330 " + "" + "(Original Invoice)"
                            + "\r\n";
                } else {
                    Printitem += "T 5 1 300 330 " + "" + "(Duplicate Invoice)"
                            + "\r\n";
                }

                Printitem += "T 5 0 10 370 --------------------------------------------------\r\n";

                Printitem += "\r\n";
                Printitem += "LEFT \r\n";
				/*
				 * Printitem += "T 5 0 20 395 " +
				 * getResources().getString(R.string.code) + "\r\n";
				 */
                Printitem += "T 5 0 10 395 "
                        + getResources().getString(R.string.brand) + "\r\n";
                Printitem += "T 5 0 220 395 "
                        + getResources().getString(R.string.case_u) + "\r\n";
                Printitem += "T 5 0 280 395 "
                        + getResources().getString(R.string.bottle) + "\r\n";

                Printitem += "T 5 0 360 395 "
                        + getResources().getString(R.string.unit_price)
                        + "\r\n";
                Printitem += "T 5 0 450 395 "
                        + getResources().getString(R.string.Amount) + "\r\n";

                Printitem += "T 5 0 10 420 --------------------------------------------------\r\n";
                x += 110;
                for (ProductMasterBO productBO : mProductsForAdapter) {

                    x += 30;
					/*
					 * Printitem += "T 5 0 20 " + x + " " +
					 * productBO.getProductCode() + "\r\n";
					 */
                    String productname = "";
                    // For Printer Space issue , restriced to 10 character.
                    if (productBO.getProductShortName() != null && !productBO.getProductShortName().equals("")
                            && !productBO.getProductShortName().equals("null")) {
                        if (productBO.getProductShortName().length() > 18)
                            productname = productBO.getProductShortName().substring(
                                    0, 18);
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

                    if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION
                            && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
                        ArrayList<ProductMasterBO> batchList = bmodel.batchAllocationHelper.getBatchlistByProductID().get(productBO.getProductID());
                        if (batchList != null && batchList.size() > 0) {
                            for (ProductMasterBO batchBO : batchList) {
                                if (batchBO.getOrderedPcsQty() > 0
                                        || batchBO
                                        .getOrderedCaseQty() > 0
                                        || batchBO
                                        .getOrderedOuterQty() > 0) {
                                    x += 30;
                                    Printitem += "T 5 0 50 " + x + " "
                                            + "Batch: " + batchBO.getBatchNo() + "\r\n";
                                    Printitem += "T 5 0 240 " + x + " "
                                            + batchBO.getOrderedCaseQty() + "\r\n";

                                    Printitem += "T 5 0 290 " + x + " "
                                            + batchBO.getOrderedPcsQty() + "\r\n";

                                    if (batchBO.getOrderedCaseQty() > 0)
                                        Printitem += "T 5 0 360 " + x + " "
                                                + bmodel.formatValue(batchBO.getCsrp())
                                                + "\r\n";
                                    else
                                        Printitem += "T 5 0 360 " + x + " "
                                                + bmodel.formatValue(batchBO.getSrp())
                                                + "\r\n";

                                    double totalProdVal = (batchBO.getOrderedOuterQty() * batchBO
                                            .getOsrp())
                                            + (batchBO.getOrderedCaseQty() * batchBO
                                            .getCsrp())
                                            + (batchBO.getOrderedPcsQty() * batchBO
                                            .getSrp());

                                    Printitem += "RIGHT \r\n";

                                    Printitem += "T 5 0 450 " + x + " "
                                            + bmodel.formatValue(totalProdVal) + "\r\n";
                                }
                            }

                        } else {
                            // No batch-
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

                        }
                    } else {
                        // No batch-
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
                    }
                    x += 10;
                    // print scheme free product starts
                    if (productBO.getIsscheme() == 1) {
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
                                        // For Printer Space issue , restriced to 10 character.
                                        if (product.getProductShortName() != null && !product.getProductShortName().equals("")
                                                && !product.getProductShortName().equals("null")) {
                                            if (product.getProductShortName().length() > 18)
                                                pname = product.getProductShortName().substring(
                                                        0, 18);
                                            else
                                                pname = product.getProductShortName();
                                        } else {
                                            if (product.getProductName().length() > 18)
                                                pname = product.getProductName().substring(
                                                        0, 18);
                                            else
                                                pname = product.getProductName();
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
                Printitem += "T 5 0 210 " + x
                        + getResources().getString(R.string.totfullstockcs)
                        + "\r\n";

                Printitem += "RIGHT \r\n";
                Printitem += "T 5 0 450 " + x + " "
                        + bmodel.formatValue(mCaseTotalValue) + "\r\n";

                x += 20;
                Printitem += "T 5 0 10 "
                        + x
                        + " --------------------------------------------------\r\n";

                x += 30;
                Printitem += "T 5 0 210 " + x
                        + getResources().getString(R.string.totfullstockbtl)
                        + "\r\n";

                Printitem += "T 5 0 450 " + x + " "
                        + bmodel.formatValue(mPcTotalValue) + "\r\n";

                x += 30;
                Printitem += "T 5 0 10 "
                        + x
                        + " --------------------------------------------------\r\n";

                x += 40;
                for (BomRetunBo productBO : mEmptyLiaProductsForAdapter) {
                    if ((productBO.getLiableQty() > 0)) {
                        x += 20;
						/*
						 * Printitem += "T 5 0 20 " + x + " " +
						 * productBO.getProdCode() + "\r\n";
						 */
                        String productname = "";
                        // For Printer Space issue , restriced to 10 character.
                        if (productBO.getProductShortName() != null && !productBO.getProductShortName().equals("")
                                && !productBO.getProductShortName().equals("null")) {
                            if (productBO.getProductShortName().length() > 18)
                                productname = productBO.getProductShortName().substring(
                                        0, 18);
                            else
                                productname = productBO.getProductShortName();
                        } else {
                            if (productBO.getProductName().length() > 18)
                                productname = productBO.getProductName().substring(
                                        0, 18);
                            else
                                productname = productBO.getProductName();
                        }


                        Printitem += "T 5 0 10 " + x + " " + productname.toLowerCase()
                                + "\r\n";
                        Printitem += "\r\n";

						/* x += 30; */
                        Printitem += "T 5 0 240 " + x + " " + "0" + "\r\n";

                        Printitem += "T 5 0 290 " + x + " "
                                + productBO.getLiableQty() + "\r\n";

                        Printitem += "T 5 0 360 " + x + " "
                                + bmodel.formatValue(productBO.getpSrp())
                                + "\r\n";

                        double totalEmpVal = (productBO.getLiableQty() * productBO
                                .getpSrp());

                        Printitem += "T 5 0 450 " + x + " "
                                + bmodel.formatValue(totalEmpVal) + "\r\n";
                        x += 10;
                    }

                }
                for (BomRetunBo productBO2 : mEmptyRetProductsForAdapter) {
                    if ((productBO2.getReturnQty() > 0)) {
                        x += 20;
						/*
						 * Printitem += "T 5 0 20 " + x + " " +
						 * productBO2.getProdCode() + "\r\n";
						 */
                        String productname = "";
                        // For Printer Space issue , restriced to 10 character.
                        if (productBO2.getProductShortName() != null && !productBO2.getProductShortName().equals("")
                                && !productBO2.getProductShortName().equals("null")) {
                            if (productBO2.getProductShortName().length() > 18)
                                productname = productBO2.getProductShortName().substring(
                                        0, 18);
                            else
                                productname = productBO2.getProductShortName();
                        } else {
                            if (productBO2.getProductName().length() > 18) {
                                productname = productBO2.getProductName().substring(
                                        0, 18);
                            } else {
                                productname = productBO2.getProductName();
                            }
                        }

                        Printitem += "T 5 0 10 " + x + " " + productname.toLowerCase()
                                + "\r\n";
                        Printitem += "\r\n";

						/* x += 30; */
                        Printitem += "T 5 0 240 " + x + " " + "0" + "\r\n";

                        Printitem += "T 5 0 290 " + x + " "
                                + productBO2.getReturnQty() + "\r\n";

                        Printitem += "T 5 0 360 " + x + " "
                                + bmodel.formatValue(productBO2.getpSrp())
                                + "\r\n";

                        double totalEmpVal = (productBO2.getReturnQty() * productBO2
                                .getpSrp());

                        Printitem += "T 5 0 450 " + x + " -"
                                + bmodel.formatValue(totalEmpVal) + "\r\n";
                        x += 10;
                    }

                }

                x += 40;
                Printitem += "T 5 0 10 "
                        + x
                        + " --------------------------------------------------\r\n";

                x += 30;
                Printitem += "T 5 0 210 " + x
                        + getResources().getString(R.string.scheme_discount)
                        + "\r\n";

                Printitem += "T 5 0 450 " + x + " "
                        + bmodel.formatValue(mSchemeDiscount) + "\r\n";

                x += 30;
                Printitem += "T 5 0 10 "
                        + x
                        + " --------------------------------------------------\r\n";

                x += 30;
                Printitem += "T 5 0 210 " + x
                        + getResources().getString(R.string.totempties)
                        + "\r\n";

                Printitem += "T 5 0 450 " + x + " "
                        + bmodel.formatValue(mEmpTotalValue) + "\r\n";

                x += 20;
                Printitem += "T 5 0 10 "
                        + x
                        + " --------------------------------------------------\r\n";
                x += 30;
                Printitem += "T 5 0 210 " + x
                        + getResources().getString(R.string.totduecoll)
                        + "\r\n";

                Printitem += "T 5 0 450 " + x + " "
                        + bmodel.formatValue(mTotalColValue) + "\r\n";

                x += 30;
                Printitem += "T 5 0 10 "
                        + x
                        + " --------------------------------------------------\r\n";

                if (bmodel.configurationMasterHelper.TAX_SHOW_INVOICE) {
                    x += 30;
                    Printitem += "T 5 0 210 " + x + mVatName + "("
                            + vatPercentage + "%)" + "\r\n";

                    Printitem += "T 5 0 450 " + x + " "
                            + bmodel.formatValue(mVatValue) + "\r\n";

                    x += 30;
                    Printitem += "T 5 0 10 "
                            + x
                            + " --------------------------------------------------\r\n";
                    if (mNhilName != "") {
                        x += 30;
                        Printitem += "T 5 0 210 " + x + mNhilName + "("
                                + nhlPercentage + "%)" + "\r\n";

                        Printitem += "T 5 0 450 " + x + " "
                                + bmodel.formatValue(mNhlValue) + "\r\n";

                        x += 30;
                        Printitem += "T 5 0 10 "
                                + x
                                + " --------------------------------------------------\r\n";
                    }
                    x += 30;
                    Printitem += "T 5 0 210 " + x + "Total Collection Due"
                            + "\r\n";

                    Printitem += "T 5 0 450 " + x + " "
                            + bmodel.formatValue(mTotColDueValue) + "\r\n";

                    x += 30;
                    Printitem += "T 5 0 10 "
                            + x
                            + " --------------------------------------------------\r\n";
                }
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
                        Printitem += "T 5 0 350 " + x
                                + "(" + getResources().getString(R.string.cheque) + ")"
                                + "\r\n";

                        Printitem += "T 5 0 450 " + x + " "
                                + bmodel.formatValue(mCheque) + "\r\n";
                    }
                } else {
                    x += 30;
                    Printitem += "T 5 0 210 " + x + "Total Paid" + "\r\n";

                    Printitem += "T 5 0 350 " + x + "Cash" + "\r\n";

                    Printitem += "T 5 0 450 " + x + " "
                            + bmodel.formatValue(mCash) + "\r\n";

                    x += 30;
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
                x += 30;
                Printitem += "T 5 0 10 "
                        + x
                        + " --------------------------------------------------\r\n";
                x += 30;
                Printitem += "T 5 0 210 " + x + "Total Credit" + "\r\n";

                Printitem += "T 5 0 450 " + x + " "
                        + bmodel.formatValue(mTotCredit) + "\r\n";

                x += 30;
                Printitem += "T 5 0 10 "
                        + x
                        + " --------------------------------------------------\r\n";

                x += 100;

                Printitem += "\r\n";
                Printitem += "\r\n";
                Printitem += "\r\n";

                Printitem += "T 5 0 10 " + x + "Customer Sign" + "\r\n";
                Printitem += "T 5 0 180 " + x + " --------\r\n";

                Printitem += "T 5 0 330 " + x + "Rep. Sign" + "\r\n";
                Printitem += "T 5 0 420 " + x + "--------\r\n";

                Printitem += "PRINT\r\n";

                PrintDataBytes = Printitem.getBytes();
            }
        } catch (Exception e) {
        }
        return PrintDataBytes;
    }

    public byte[] printDatafor4inchprinter() {
        byte[] PrintDataBytes = null;
        try {
            PrinterLanguage printerLanguage = PrinterLanguage.CPCL;
            // 00:22:58:3D:7E:83
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
                height = x + mProductsForAdapter.size() * 350 + 600;
                Commons.print(TAG + "Heigt:" + height);
                String Printitem = "! 0 200 200 " + height + " 1\r\n"
                        + "CENTER\r\n";
                Printitem += ExtractGraphicsDataForCPCL(0, 0);

                Printitem += "T 5 1 10 140 "
                        + ""
                        + bmodel.userMasterHelper.getUserMasterBO()
                        .getDistributorName() + "\r\n";

                Printitem += "T 5 0 10 180 " + "" + mPhcontact + "\r\n";

                // T- Text
                //
                // Font Size
                // Spacing
                // height between lines

                Printitem += "T 5 0 10 200 --------------------------------------------------\r\n";
                // Printitem += "T 5 0 10 80  "; /*
                // * "print distributor name and distributor address"
                // */
                //
                // Printitem += "\r\n" + "T 5 0 10 100  \r\n";
                Printitem += "\r\n";
                Printitem += "LEFT \r\n";
                Printitem += "T 5 0 20 230 "
                        + getResources().getString(R.string.invoice_no) + ": "
                        + mInvoiceno + "\r\n";

                Printitem += "T 5 0 520 230 "
                        + getResources().getString(R.string.sales_date) + ":"
                        + mSalesdate + "\r\n";

                Printitem += "LEFT \r\n";
                Printitem += "T 5 0 20 260 "
                        + getResources().getString(R.string.customer) + ""
                        + mCustomername + "\r\n";

                Printitem += "\r\n";
                Printitem += "LEFT \r\n";

                Printitem += "T 5 0 20 290 "
                        + getResources().getString(R.string.Address) + ":"
                        + bmodel.getRetailerMasterBO().getAddress1() + "\r\n";
                Printitem += "\r\n";
                Printitem += "T 5 0 20 320 "
                        + getResources().getString(R.string.tel) + ":"
                        + bmodel.getRetailerMasterBO().getAddress1() + "\r\n";

                if (IsOriginal) {
                    Printitem += "T 5 1 480 320 " + "" + "(Original Invoice)"
                            + "\r\n";
                } else {
                    Printitem += "T 5 1 480 320 " + "" + "(Duplicate Invoice)"
                            + "\r\n";
                }

                Printitem += "T 5 0 10 350 --------------------------------------------------\r\n";

                Printitem += "\r\n";
                Printitem += "LEFT \r\n";
                Printitem += "T 5 0 20 390 "
                        + getResources().getString(R.string.code) + "\r\n";
                Printitem += "T 5 0 230 390 "
                        + getResources().getString(R.string.brand) + "\r\n";
                Printitem += "T 5 0 400 390 "
                        + getResources().getString(R.string.case_u) + "\r\n";
                Printitem += "T 5 0 480 390 "
                        + getResources().getString(R.string.bottle) + "\r\n";

                Printitem += "T 5 0 580 390 "
                        + getResources().getString(R.string.unit_price)
                        + "\r\n";
                Printitem += "T 5 0 700 390 "
                        + getResources().getString(R.string.Amount) + "\r\n";

                Printitem += "T 5 0 10 420 --------------------------------------------------\r\n";
                x += 110;
                for (ProductMasterBO productBO : mProductsForAdapter) {

                    x += 20;
                    Printitem += "T 5 0 20 " + x + " "
                            + productBO.getProductCode() + "\r\n";
                    Printitem += "T 5 0 230 " + x + " "
                            + productBO.getProductName().toLowerCase() + "\r\n";
                    Printitem += "\r\n";

                    x += 30;
                    Printitem += "T 5 0 430 " + x + " "
                            + productBO.getOrderedCaseQty() + "\r\n";

                    Printitem += "T 5 0 500 " + x + " "
                            + productBO.getOrderedPcsQty() + "\r\n";

                    if (productBO.getOrderedCaseQty() > 0)
                        Printitem += "T 5 0 580 " + x + " "
                                + bmodel.formatValue(productBO.getCsrp())
                                + "\r\n";
                    else
                        Printitem += "T 5 0 580 " + x + " "
                                + bmodel.formatValue(productBO.getSrp())
                                + "\r\n";
                    double totalProdVal = (productBO.getOrderedOuterQty() * productBO
                            .getOsrp())
                            + (productBO.getOrderedCaseQty() * productBO
                            .getCsrp())
                            + (productBO.getOrderedPcsQty() * productBO
                            .getSrp());

                    Printitem += "RIGHT \r\n";

                    Printitem += "T 5 0 690 " + x + " "
                            + bmodel.formatValue(totalProdVal) + "\r\n";
                    x += 10;

                }
                x += 30;
                Printitem += "T 5 0 10 "
                        + x
                        + " --------------------------------------------------\r\n";

                x += 30;
                Printitem += "T 5 0 230 " + x
                        + getResources().getString(R.string.totfullstockcs)
                        + "\r\n";

                Printitem += "RIGHT \r\n";
                Printitem += "T 5 0 690 " + x + " "
                        + bmodel.formatValue(mCaseTotalValue) + "\r\n";

                x += 20;
                Printitem += "T 5 0 10 "
                        + x
                        + " --------------------------------------------------\r\n";

                x += 30;
                Printitem += "T 5 0 230 " + x
                        + getResources().getString(R.string.totfullstockbtl)
                        + "\r\n";

                Printitem += "T 5 0 690 " + x + " "
                        + bmodel.formatValue(mPcTotalValue) + "\r\n";

                x += 30;
                Printitem += "T 5 0 10 "
                        + x
                        + " --------------------------------------------------\r\n";

                x += 40;
                for (BomRetunBo productBO : mEmptyLiaProductsForAdapter) {
                    if ((productBO.getLiableQty() > 0)) {
                        x += 20;
                        Printitem += "T 5 0 20 " + x + " "
                                + productBO.getProdCode() + "\r\n";
                        Printitem += "T 5 0 230 " + x + " "
                                + productBO.getProductName().toLowerCase()
                                + "\r\n";
                        Printitem += "\r\n";

                        x += 30;
                        Printitem += "T 5 0 430 " + x + " " + "0" + "\r\n";

                        Printitem += "T 5 0 500 " + x + " "
                                + productBO.getLiableQty() + "\r\n";

                        Printitem += "T 5 0 580 " + x + " "
                                + bmodel.formatValue(productBO.getpSrp())
                                + "\r\n";

                        double totalEmpVal = (productBO.getLiableQty() * productBO
                                .getpSrp());

                        Printitem += "T 5 0 690 " + x + " "
                                + bmodel.formatValue(totalEmpVal) + "\r\n";
                        x += 10;
                    }

                }
                for (BomRetunBo productBO2 : mEmptyRetProductsForAdapter) {
                    if ((productBO2.getReturnQty() > 0)) {
                        x += 20;
                        Printitem += "T 5 0 20 " + x + " "
                                + productBO2.getProdCode() + "\r\n";
                        Printitem += "T 5 0 230 " + x + " "
                                + productBO2.getProductName().toLowerCase()
                                + "\r\n";
                        Printitem += "\r\n";

                        x += 30;
                        Printitem += "T 5 0 430 " + x + " " + "0" + "\r\n";

                        Printitem += "T 5 0 500 " + x + " "
                                + productBO2.getReturnQty() + "\r\n";

                        Printitem += "T 5 0 580 " + x + " "
                                + bmodel.formatValue(productBO2.getpSrp())
                                + "\r\n";

                        double totalEmpVal = (productBO2.getReturnQty() * productBO2
                                .getpSrp());

                        Printitem += "T 5 0 690 " + x + " "
                                + bmodel.formatValue(totalEmpVal) + "\r\n";
                        x += 10;
                    }

                }

                x += 40;
                Printitem += "T 5 0 10 "
                        + x
                        + " --------------------------------------------------\r\n";

                x += 30;
                Printitem += "T 5 0 210 " + x
                        + getResources().getString(R.string.scheme_discount)
                        + "\r\n";

                Printitem += "T 5 0 450 " + x + " "
                        + bmodel.formatValue(mSchemeDiscount) + "\r\n";

                x += 30;
                Printitem += "T 5 0 10 "
                        + x
                        + " --------------------------------------------------\r\n";

                x += 30;
                Printitem += "T 5 0 230 " + x
                        + getResources().getString(R.string.totempties)
                        + "\r\n";

                Printitem += "T 5 0 690 " + x + " "
                        + bmodel.formatValue(mEmpTotalValue) + "\r\n";

                x += 20;
                Printitem += "T 5 0 10 "
                        + x
                        + " --------------------------------------------------\r\n";
                x += 20;
                Printitem += "T 5 0 230 " + x
                        + getResources().getString(R.string.totduecoll)
                        + "\r\n";

                Printitem += "T 5 0 690 " + x + " "
                        + bmodel.formatValue(mTotalColValue) + "\r\n";

                x += 30;
                Printitem += "T 5 0 10 "
                        + x
                        + " --------------------------------------------------\r\n";
                if (bmodel.configurationMasterHelper.TAX_SHOW_INVOICE) {
                    x += 20;
                    Printitem += "T 5 0 230 " + x + mVatName + "("
                            + vatPercentage + "%)" + "\r\n";

                    Printitem += "T 5 0 690 " + x + " "
                            + bmodel.formatValue(mVatValue) + "\r\n";

                    x += 30;
                    Printitem += "T 5 0 10 "
                            + x
                            + " --------------------------------------------------\r\n";
                    if (mNhilName != "") {
                        x += 20;
                        Printitem += "T 5 0 230 " + x + mNhilName + "("
                                + nhlPercentage + "%)" + "\r\n";

                        Printitem += "T 5 0 690 " + x + " "
                                + bmodel.formatValue(mNhlValue) + "\r\n";

                        x += 30;
                        Printitem += "T 5 0 10 "
                                + x
                                + " --------------------------------------------------\r\n";
                    }
                }
                x += 20;
                Printitem += "T 5 0 230 " + x + "Total Collection Due" + "\r\n";

                Printitem += "T 5 0 690 " + x + " "
                        + bmodel.formatValue(mTotColDueValue) + "\r\n";

                x += 30;
                Printitem += "T 5 0 10 "
                        + x
                        + " --------------------------------------------------\r\n";

                x += 20;
                Printitem += "T 5 0 230 " + x + "Total Paid" + "\r\n";

                Printitem += "T 5 0 380 " + x + "Cash" + "\r\n";

                Printitem += "T 5 0 690 " + x + " " + bmodel.formatValue(mCash)
                        + "\r\n";

                x += 30;
                Printitem += "T 5 0 10 "
                        + x
                        + " --------------------------------------------------\r\n";

                x += 20;
                Printitem += "T 5 0 380 " + x
                        + getResources().getString(R.string.cheque) + "\r\n";

                Printitem += "T 5 0 690 " + x + " "
                        + bmodel.formatValue(mCheque) + "\r\n";

                x += 30;
                Printitem += "T 5 0 10 "
                        + x
                        + " --------------------------------------------------\r\n";
                x += 20;
                Printitem += "T 5 0 230 " + x + "Total Credit" + "\r\n";

                Printitem += "T 5 0 690 " + x + " "
                        + bmodel.formatValue(mTotCredit) + "\r\n";

                x += 30;
                Printitem += "T 5 0 10 "
                        + x
                        + " --------------------------------------------------\r\n";

                x += 180;

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
                        PrintPreviewScreenDiageo.this).setTitle("Choose Printer")
                        .setSingleChoiceItems(mPrinterSelectionArray, -1,
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
            Commons.print(TAG + ",Bitmap height :" + m_bmp.getHeight());
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
    protected void onDestroy() {
        super.onDestroy();
        unbindDrawables(findViewById(R.id.root));
        disconnect();
        // bmodel.productHelper.clearOrderTable();
        // force the garbage collector to run
        System.gc();
    }
}