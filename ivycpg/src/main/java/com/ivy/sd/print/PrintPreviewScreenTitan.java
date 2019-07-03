package com.ivy.sd.print;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.ivy.cpg.view.order.OrderHelper;
import com.ivy.cpg.view.van.LoadManagementHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.cpg.view.order.scheme.SchemeProductBO;
import com.ivy.sd.png.bo.StoreWiseDiscountBO;
import com.ivy.sd.png.bo.SubDepotBo;
import com.ivy.cpg.view.order.tax.TaxBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.NumberToWord;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.utils.DateTimeUtils;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

public class PrintPreviewScreenTitan extends IvyBaseActivityNoActionBar {

    private TextView distName, distName2, invoiceno, salesdate;
    private String mInvoiceno;

    private BusinessModel bmodel;
    private List<ProductMasterBO> mOrderedProductList;
    double productEntryLevelDis = 0.0;
    private Vector<ProductMasterBO> mProducts = new Vector<ProductMasterBO>();
    private ArrayList<ProductMasterBO> mProductsForAdapter = new ArrayList<ProductMasterBO>();

    private ImageView imagevw;
    private ProgressDialog pd;
    private static final int SELECTED_PRINTER_DIALOG = 1;
    private static final String ZEBRA_2INCH = "2";
    private static final String ZEBRA_3INCH = "3";
    private static final String ZEBRA_4INCH = "4";
    private static final String VAT_TAX = "VAT";
    private Connection zebraPrinterConnection;
    private final String[] mPrinterSelectionArray = {ZEBRA_2INCH, ZEBRA_3INCH,
            ZEBRA_4INCH};
    private String mSelectedPrinterName;
    private static final String TAG = "InvoicePrint";
    private String count;
    private ZebraPrinter printer;
    private TextView statusField, distaddress;
    private ImageView mStatusIV;
    private LinearLayout mProductContainerLL, mDiscountContainerLL, mTaxcontainerLL;

    private Spinner printcount;
    private ArrayAdapter<CharSequence> spinadapter;
    private String m_data;
    private String storediscount = "0";
    private boolean IsFromOrder, IsFromReport;
    private HashMap<StringBuffer, StringBuffer> mTaxDetailByDesc2;


    private ArrayList<Integer> mTypeIdList;
    private SparseArray<ArrayList<Integer>> mDiscountIdListByTypeId;
    private SparseArray<Double> mDiscountValueByTypeId;
    private Toolbar toolbar;
    private OrderHelper orderHelper;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_preview_zebra_titan);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try {
            bmodel = (BusinessModel) getApplicationContext();
            bmodel.setContext(this);
            orderHelper = OrderHelper.getInstance(this);

            mInvoiceno = bmodel.invoiceNumber;
            Bundle extras = getIntent().getExtras();
            if (extras != null) {

                if (extras.containsKey("IsFromOrder"))
                    IsFromOrder = extras.getBoolean("IsFromOrder");

                if (extras.containsKey("entryLevelDis"))
                    productEntryLevelDis = extras.getDouble("entryLevelDis");
                // if (extras.containsKey("storediscount"))
                // storediscount = extras.getString("storediscount");
                Commons.print("setSpinnerPosition" + storediscount);
            }

            Commons.print("PRODUCT DISCOUNT<><><>," + "" + productEntryLevelDis);


            invoiceno = (TextView) findViewById(R.id.invoiceno);

            salesdate = (TextView) findViewById(R.id.salesdate);

            distName = (TextView) findViewById(R.id.distname);
            distName2 = (TextView) findViewById(R.id.distname2);
            distaddress = (TextView) findViewById(R.id.distadd);
            salesdate.setText(DateTimeUtils.convertFromServerDateToRequestedFormat(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), bmodel.configurationMasterHelper.outDateFormat));
            invoiceno.setText(mInvoiceno);
            if (bmodel.userMasterHelper.getUserMasterBO().getDistributorName().length() > 15) {
                distName.setText(bmodel.userMasterHelper.getUserMasterBO()
                        .getDistributorName().substring(0, 15));
                distName2.setText(bmodel.userMasterHelper.getUserMasterBO().getDistributorName().substring(16));
            } else {
                distName.setText(bmodel.userMasterHelper.getUserMasterBO()
                        .getDistributorName());
            }
            if (bmodel.getRetailerMasterBO().getTinnumber() != null && !bmodel.getRetailerMasterBO().getTinnumber().equals("")) {
                ((TextView) findViewById(R.id.tv_invoice_type)).setText("Tax Invoice");
            } else {
                ((TextView) findViewById(R.id.tv_invoice_type)).setText("Sales/Retail Invoice ");
            }

            if (bmodel.userMasterHelper.getUserMasterBO().getDistributorAddress3() != null) {
                if (bmodel.userMasterHelper.getUserMasterBO().getDistributorAddress3().length() > 15) {
                    distaddress.setText(bmodel.userMasterHelper.getUserMasterBO().getDistributorAddress3().substring(0, 15) + "");
                } else {
                    distaddress.setText(bmodel.userMasterHelper.getUserMasterBO().getDistributorAddress3() + "");
                }
            }
            ((TextView) findViewById(R.id.tinno)).setText(bmodel.userMasterHelper.getUserMasterBO().getDistributorTinNumber() + "");
            ((TextView) findViewById(R.id.cstno)).setText(bmodel.userMasterHelper.getUserMasterBO().getCstNo());

            if (bmodel.getRetailerMasterBO().getRetailerName().length() > 15) {
                ((TextView) findViewById(R.id.retname)).setText(bmodel.getRetailerMasterBO().getRetailerName().substring(0, 15) + "");
                ((TextView) findViewById(R.id.retname2)).setText(bmodel.getRetailerMasterBO().getRetailerName().substring(16) + "");
            } else {
                ((TextView) findViewById(R.id.retname)).setText(bmodel.getRetailerMasterBO().getRetailerName() + "");
            }

            ((TextView) findViewById(R.id.retadd)).setText(bmodel.getRetailerMasterBO().getAddress3() + "");
            ((TextView) findViewById(R.id.rettinno)).setText(bmodel.getRetailerMasterBO().getTinnumber() + "");
            ((TextView) findViewById(R.id.retcstno)).setText(bmodel.getRetailerMasterBO().getCredit_invoice_count() + "");

            statusField = (TextView) findViewById(R.id.status_bar);
            mStatusIV = (ImageView) findViewById(R.id.status_iv);
            mProductContainerLL = (LinearLayout) findViewById(R.id.product_container_ll);
            mDiscountContainerLL = (LinearLayout) findViewById(R.id.base_ll);
            mTaxcontainerLL = (LinearLayout) findViewById(R.id.tax_ll);


            Vector<ProductMasterBO> productList = bmodel.productHelper
                    .getProductMaster();

            int productsCount = productList.size();
            mOrderedProductList = new ArrayList<ProductMasterBO>();
            ProductMasterBO productBO;
            for (int i = 0; i < productsCount; i++) {

                productBO = productList.elementAt(i);
                if (productBO.getOrderedCaseQty() > 0
                        || productBO.getOrderedPcsQty() > 0
                        || productBO.getOrderedOuterQty() > 0) {

                    mOrderedProductList.add(productBO);
                }
            }


            mTypeIdList = bmodel.productHelper.getTypeIdList();
            mDiscountIdListByTypeId = bmodel.productHelper.getDiscountIdListByTypeId();


            /** set values in textview **/


            updateproducts();

            // Set title to toolbar
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

            storediscount = orderHelper.invoiceDiscount;
            Commons.print("discount" + orderHelper.invoiceDiscount + " "
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
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    /**
     * set values in product list *
     */
    private void updateproducts() {

        try {
            mProductContainerLL.removeAllViews();


            LayoutInflater inflater = getLayoutInflater();

            int totalOrderedBatchCount = 0;
            int totalFrreProudctCount = 0;
            int totaltaxCount = 0;
            if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION
                    && bmodel.configurationMasterHelper.IS_INVOICE
                    && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
                for (ProductMasterBO productBO : mOrderedProductList) {
                    if (productBO.getBatchwiseProductCount() > 0) {
                        ArrayList<ProductMasterBO> batchList = bmodel.batchAllocationHelper.getBatchlistByProductID().get(productBO.getProductID());
                        if (batchList != null) {
                            for (ProductMasterBO batchProductBO : batchList) {
                                if (batchProductBO.getOrderedPcsQty() > 0
                                        || batchProductBO.getOrderedCaseQty() > 0
                                        || batchProductBO.getOrderedOuterQty() > 0) {
                                    totalOrderedBatchCount = totalOrderedBatchCount + 1;
                                }
                            }
                        }

                    } else {
                        totalOrderedBatchCount = totalOrderedBatchCount + 1;
                    }
                }
            } else {
                totalOrderedBatchCount = mOrderedProductList.size();
            }


            for (ProductMasterBO productBO : mOrderedProductList) {
                if (productBO.isPromo()) {
                    List<SchemeProductBO> schemeFreeList = productBO.getSchemeProducts();
                    if (schemeFreeList != null) {
                        totalFrreProudctCount = totalFrreProudctCount + schemeFreeList.size();
                    }
                    schemeFreeList = null;
                }
            }

            bmodel.productHelper.taxHelper.loadTaxDetailsForPrint(bmodel.invoiceNumber);
            bmodel.productHelper.taxHelper.loadTaxProductDetailsForPrint(bmodel.invoiceNumber);

            ArrayList<TaxBO> groupIdList = bmodel.productHelper.taxHelper.getGroupIdList();

            if (groupIdList != null) {
                for (TaxBO taxBO : groupIdList) {
                    HashSet<Double> percentagerList = bmodel.productHelper.taxHelper.getTaxPercentagerListByGroupId().get(taxBO.getGroupId());
                    if (percentagerList != null) {
                        totaltaxCount = totaltaxCount + (percentagerList.size());
                    }
                    percentagerList = null;
                }
            }


            ArrayList<SubDepotBo> distributorList = LoadManagementHelper.getInstance(this).getSubDepotList();
            String distributorAddress1 = "";
            String distributorAddress2 = "";
            String distributorContactNo = "";
            if (distributorList != null) {
                for (SubDepotBo subDepotBo : distributorList) {
                    distributorAddress1 = subDepotBo.getAddress1();
                    distributorAddress2 = subDepotBo.getAddress2();
                    distributorContactNo = subDepotBo.getContactNumber();
                }

            }


            double total = 0;
            double totalExcludeTaxvalue = 0;
            boolean isBatchwise = false;
            double totalPriceOffValue = 0;
            double totalCashDiscountValue = 0;
            if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION
                    && bmodel.configurationMasterHelper.IS_INVOICE
                    && bmodel.configurationMasterHelper.IS_SIH_VALIDATION)
                isBatchwise = true;
            int totalQty = 0;
            for (ProductMasterBO productBO : mOrderedProductList) {


                if (isBatchwise && productBO.getBatchwiseProductCount() > 0) {

                    ArrayList<ProductMasterBO> batchList = bmodel.batchAllocationHelper.getBatchlistByProductID().get(productBO.getProductID());
                    if (batchList != null) {
                        for (ProductMasterBO batchProductBO : batchList) {
                            View v = inflater.inflate(
                                    R.layout.row_print_preview_titan, null);

                            int totalBatchQty = batchProductBO.getOrderedPcsQty() + batchProductBO.getOrderedCaseQty() * productBO.getCaseSize()
                                    + batchProductBO.getOrderedOuterQty() * productBO.getOutersize();

                            if (totalBatchQty > 0) {

                                totalPriceOffValue = totalPriceOffValue + (totalBatchQty * batchProductBO.getPriceoffvalue());
                                totalQty = totalQty + totalBatchQty;
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

                                ((TextView) v.findViewById(R.id.product_name_tv))
                                        .setText(productname + "");
                                if (batchProductBO.getBatchNo().equalsIgnoreCase("NA") || batchProductBO.getBatchNo().equalsIgnoreCase("none"))
                                    ((TextView) v.findViewById(R.id.product_batch_tv))
                                            .setText(batchProductBO.getBatchNo() + "");
                                else {
                                    ((TextView) v.findViewById(R.id.product_batch_tv))
                                            .setText("");
                                }

                                ((TextView) v.findViewById(R.id.product_batch_tv))
                                        .setText(batchProductBO.getBatchNo() + "");
                                Commons.print("Batch No <><>," + "" + batchProductBO.getBatchNo());
                                ((TextView) v.findViewById(R.id.qty)).setText(totalBatchQty + "");
                                ((TextView) v.findViewById(R.id.ucp)).setText(SDUtil.format((batchProductBO.getSrp() + batchProductBO.getPriceoffvalue()), 2, 0) + "");


                                totalExcludeTaxvalue = totalExcludeTaxvalue + (batchProductBO.getTaxableAmount() > 0 ? batchProductBO.getTaxableAmount() : batchProductBO.getNetValue());
                                total = total + batchProductBO.getNetValue();
                                ((TextView) v.findViewById(R.id.payable)).setText(SDUtil.format(batchProductBO.getNetValue(), 2, 0) + "");
                                mProductContainerLL.addView(v);

                            }
                        }

                    }


                } else {

                    View v = inflater.inflate(
                            R.layout.row_print_preview_titan, null);
                    int totalProductQty = productBO.getOrderedPcsQty() + productBO.getOrderedCaseQty() * productBO.getCaseSize()
                            + productBO.getOrderedOuterQty() * productBO.getOutersize();
                    totalPriceOffValue = totalPriceOffValue + productBO.getPriceoffvalue();

                    totalQty = totalQty + totalProductQty;
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


                    ((TextView) v.findViewById(R.id.product_name_tv))
                            .setText(productname + "");

                    ((TextView) v.findViewById(R.id.qty)).setText(totalProductQty + "");
                    ((TextView) v.findViewById(R.id.ucp)).setText(productBO.getSrp() + "");


                    totalExcludeTaxvalue = totalExcludeTaxvalue + (productBO.getTaxableAmount() > 0 ? productBO.getTaxableAmount() : productBO.getNetValue());
                    total = total + productBO.getNetValue();
                    ((TextView) v.findViewById(R.id.payable)).setText(SDUtil.format(productBO.getNetValue(), 2, 0) + "");

                    mProductContainerLL.addView(v);
                }


            }


            ((TextView) findViewById(R.id.totqty)).setText(totalQty + "");
            ((TextView) findViewById(R.id.totaltamt)).setText(SDUtil.format(total, 2, 0) + "");
            ((TextView) findViewById(R.id.schemediscountamt)).setText(SDUtil.format(totalPriceOffValue, 2, 0) + "");

            mDiscountContainerLL.removeAllViews();

            // Print Item Level Discount

            mTypeIdList = bmodel.productHelper.getTypeIdList();
            mDiscountIdListByTypeId = bmodel.productHelper.getDiscountIdListByTypeId();
            if (mTypeIdList != null && mDiscountIdListByTypeId != null) {
                for (Integer typeId : mTypeIdList) {
                    ArrayList<Integer> discountIdList = mDiscountIdListByTypeId.get(typeId);
                    if (discountIdList != null) {
                        View v = inflater.inflate(
                                R.layout.row_print_preview_titan_discount, null);
                        double totalDiscountValue = 0;
                        String discountDescription = "";
                        for (int discountid : discountIdList) {

                            ArrayList<StoreWiseDiscountBO> discountList = bmodel.productHelper.getProductDiscountListByDiscountID().get(discountid);


                            if (discountList != null) {

                                for (StoreWiseDiscountBO storeWiseDiscountBO : discountList) {

                                    discountDescription = storeWiseDiscountBO.getDescription();
                                    ProductMasterBO productMasterBO = bmodel.productHelper.getProductMasterBOById(storeWiseDiscountBO.getProductId() + "");
                                    if (productMasterBO != null) {
                                        int totalProductQty = 0;
                                        totalProductQty = productMasterBO.getOrderedPcsQty()
                                                + productMasterBO.getOrderedCaseQty() * productMasterBO.getCaseSize()
                                                + productMasterBO.getOrderedOuterQty() * productMasterBO.getOutersize();
                                        if (totalProductQty > 0) {
                                            if (productMasterBO.getBatchwiseProductCount() > 0) {

                                                ArrayList<ProductMasterBO> batchList = bmodel.batchAllocationHelper.getBatchlistByProductID().get(productMasterBO.getProductID());
                                                if (batchList != null) {
                                                    for (ProductMasterBO batchProductBO : batchList) {
                                                        double totalValue = 0;
                                                        double batchDiscountValue = 0;
                                                        int totalBatchQty = batchProductBO.getOrderedPcsQty() + batchProductBO.getOrderedCaseQty() * productMasterBO.getCaseSize()
                                                                + batchProductBO.getOrderedOuterQty() * productMasterBO.getOutersize();

                                                        if (batchProductBO.getLineValueAfterSchemeApplied() > 0) {
                                                            totalValue = batchProductBO.getLineValueAfterSchemeApplied();
                                                        } else {
                                                            totalValue = batchProductBO.getOrderedPcsQty()
                                                                    * batchProductBO.getSrp()
                                                                    + batchProductBO.getOrderedCaseQty()
                                                                    * batchProductBO.getCsrp()
                                                                    + batchProductBO.getOrderedOuterQty()
                                                                    * batchProductBO.getOsrp();
                                                        }

                                                        if (storeWiseDiscountBO.getIsPercentage() == 1) {
                                                            batchDiscountValue = totalValue * storeWiseDiscountBO.getDiscount() / 100;


                                                        } else if (storeWiseDiscountBO.getIsPercentage() == 0) {
                                                            batchDiscountValue = totalBatchQty * storeWiseDiscountBO.getDiscount();
                                                        }

                                                        totalDiscountValue = totalDiscountValue + batchDiscountValue;


                                                    }
                                                }


                                            } else {
                                                double totalValue = 0;
                                                double productDiscount = 0;

                                                if (productMasterBO.getLineValueAfterSchemeApplied() > 0) {
                                                    totalValue = productMasterBO.getLineValueAfterSchemeApplied();
                                                } else {
                                                    totalValue = productMasterBO.getOrderedPcsQty() * productMasterBO.getSrp()
                                                            + productMasterBO.getOrderedCaseQty() * productMasterBO.getCsrp()
                                                            + productMasterBO.getOrderedOuterQty() * productMasterBO.getOsrp();
                                                }

                                                if (storeWiseDiscountBO.getIsPercentage() == 1) {
                                                    productDiscount = totalValue * storeWiseDiscountBO.getDiscount() / 100;


                                                } else if (storeWiseDiscountBO.getIsPercentage() == 0) {
                                                    productDiscount = totalProductQty * storeWiseDiscountBO.getDiscount();
                                                }

                                                totalDiscountValue = totalDiscountValue + productDiscount;


                                            }
                                        }

                                    }

                                }


                            }


                        }

                        if (totalDiscountValue > 0) {
                            String discName = "";
                            if (discountDescription.length() < 10) {
                                discName = discountDescription;
                            } else {
                                discName = discountDescription.substring(0, 10);
                            }


                            ((TextView) v.findViewById(R.id.disc_name_tv))
                                    .setText(discName + "");
                            ((TextView) v.findViewById(R.id.discamt))
                                    .setText(SDUtil.format(totalDiscountValue, 2, 0) + "");
                            mDiscountContainerLL.addView(v);

                        }

                    }
                }
            }


// cash discount

            ((TextView) findViewById(R.id.cashdiscountamt)).setText(SDUtil.format(productEntryLevelDis, 2, 0) + "");

// apply item level tax

            HashMap<String, HashSet<String>> productListByGroupId = bmodel.productHelper.taxHelper.getProductIdByTaxGroupId();

            SparseArray<LinkedHashSet<TaxBO>> taxListByGroupId = bmodel.productHelper.taxHelper.getTaxBoByGroupId();

            mTaxcontainerLL.removeAllViews();
            if (groupIdList != null) {
                String taxDesc = "";

                String previousTaxDesc = "";
                mTaxDetailByDesc2 = new HashMap<StringBuffer, StringBuffer>();


                for (TaxBO taxBO : groupIdList) {


                    LinkedHashSet<TaxBO> taxList = taxListByGroupId.get(taxBO.getGroupId());
                    if (taxList != null) {

                        for (TaxBO taxchildBO : taxList) {

                            double taxpercentege = taxchildBO.getTaxRate();
                            HashSet<String> taxProductList = productListByGroupId.get(taxBO.getGroupId() + "" + taxpercentege);
                            taxDesc = taxchildBO.getTaxDesc2();


                            double totalTax = 0.0;
                            double totalExcludeValue = 0.0;
                            if (taxProductList != null) {

                                for (String productid : taxProductList) {
                                    ProductMasterBO prodcutBO = bmodel.productHelper.getProductMasterBOById(productid);
                                    if (prodcutBO != null) {


                                        totalExcludeValue = totalExcludeValue + prodcutBO.getTaxableAmount();
                                        totalTax = totalTax + (prodcutBO.getTaxableAmount() * taxpercentege) / 100;

                                    }

                                }

                                if (totalTax > 0) {
                                    View v = inflater.inflate(
                                            R.layout.row_print_preview_titan_tax, null);

                                    if (!taxDesc.equals(previousTaxDesc)) {
                                        if (taxDesc.length() > 10) {
                                            ((TextView) v.findViewById(R.id.tax_name_tv))
                                                    .setText(taxDesc.substring(0, 10) + "");
                                        } else {
                                            ((TextView) v.findViewById(R.id.tax_name_tv))
                                                    .setText(taxDesc + "");
                                        }
                                    }


                                    ((TextView) v.findViewById(R.id.tax_tv))
                                            .setText(taxpercentege + "% on Rs " + SDUtil.format(totalExcludeValue, 2, 0) + "");

                                    ((TextView) v.findViewById(R.id.taxamt))
                                            .setText(SDUtil.format(totalTax, 2, 0) + "");

                                    mTaxcontainerLL.addView(v);
                                }


                            }
                            previousTaxDesc = taxDesc;


                        }
                    }

                }
            }


            ((TextView) findViewById(R.id.netpayableamount)).setText(SDUtil.format(Math.round(total), 2, 0) + "");


            String formatTotal = SDUtil.format(Math.round(total), 2, 0);

            if (formatTotal.length() <= 12) {
                String[] splits = formatTotal.split(Pattern.quote("."));
                StringBuffer convertBuffer = new StringBuffer();
                NumberToWord numberToWord = new NumberToWord();
                for (int i = 0; i < splits.length; i++) {
                    long splitvalue = SDUtil.convertToLong(splits[i]);
                    if (i == 1 && splitvalue > 0) {
                        convertBuffer.append(" and ");
                    }
                    convertBuffer.append(numberToWord.convertNumberToWords(SDUtil.convertToLong(splits[i].toString())));
                    if (i == 0) {
                        convertBuffer.append(" Rupees ");
                    } else if (i == 1) {
                        if (!splits[i].toString().equals("00"))
                            convertBuffer.append(" Paise");
                    }

                }
                //if (convertBuffer.length() < 40) {

                ((TextView) findViewById(R.id.amountinwords)).setText(convertBuffer.toString() + "");

               /* } else {

                    ((TextView) findViewById(R.id.amountinwords)).setText(convertBuffer.substring(0, 40) + "");


                }*/
                convertBuffer = null;
                numberToWord = null;
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_print_preview, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!IsFromReport) {
                // Clear the Values in Objects
                bmodel.productHelper.clearOrderTable();
                if (bmodel.configurationMasterHelper.SHOW_PRODUCTRETURN)
                    bmodel.productHelper.clearBomReturnProductsTable();
            }

            if (IsFromReport) {
                finish();
            } else {
                bmodel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
                        .now(DateTimeUtils.TIME));
                finish();
                //BusinessModel.loadActivity(this, DataMembers.actHomeScreenTwo);
                //  DataMembers.actHomeScreenTwo);
                Intent myIntent = new Intent(this, HomeScreenTwo.class);
                startActivityForResult(myIntent, 0);
            }
            return true;
        } else if (id == R.id.menu_print) {

            new Thread(new Runnable() {
                public void run() {
                    Looper.prepare();
                    doConnection();
                    Looper.loop();
                    Looper.myLooper().quit();
                }
            }).start();
        }
        return false;
    }


    public byte[] printDataforTitan3inchprinter(List<ProductMasterBO> mOrderedProductList, double entryLevelDis, int printCount) {
        byte[] configLabel = null;
        byte[] printDataBytes = null;
        try {
            StringBuilder sb = new StringBuilder();
            PrinterLanguage printerLanguage = PrinterLanguage.CPCL;
            // 00:22:58:3D:7E:83 - RW420
            // AC:3F:A4:16:B9:AE - IMZ320

            if (printerLanguage == PrinterLanguage.CPCL) {
                int totalOrderedBatchCount = 0;
                int totalFrreProudctCount = 0;
                int totaltaxCount = 0;
                if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION
                        && bmodel.configurationMasterHelper.IS_INVOICE
                        && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
                    for (ProductMasterBO productBO : mOrderedProductList) {
                        if (productBO.getBatchwiseProductCount() > 0) {
                            ArrayList<ProductMasterBO> batchList = bmodel.batchAllocationHelper.getBatchlistByProductID().get(productBO.getProductID());
                            if (batchList != null) {
                                for (ProductMasterBO batchProductBO : batchList) {
                                    if (batchProductBO.getOrderedPcsQty() > 0
                                            || batchProductBO.getOrderedCaseQty() > 0
                                            || batchProductBO.getOrderedOuterQty() > 0) {
                                        totalOrderedBatchCount = totalOrderedBatchCount + 1;
                                    }
                                }
                            }

                        } else {
                            totalOrderedBatchCount = totalOrderedBatchCount + 1;
                        }
                    }
                } else {
                    totalOrderedBatchCount = mOrderedProductList.size();
                }


                for (ProductMasterBO productBO : mOrderedProductList) {
                    if (productBO.isPromo()) {
                        List<SchemeProductBO> schemeFreeList = productBO.getSchemeProducts();
                        if (schemeFreeList != null) {
                            totalFrreProudctCount = totalFrreProudctCount + schemeFreeList.size();
                        }
                        schemeFreeList = null;
                    }
                }

                ArrayList<TaxBO> groupIdList = bmodel.productHelper.taxHelper.getGroupIdList();

                if (groupIdList != null) {
                    for (TaxBO taxBO : groupIdList) {
                        LinkedHashSet<TaxBO> percentagerList = bmodel.productHelper.taxHelper.getTaxBoByGroupId().get(taxBO.getGroupId());
                        if (percentagerList != null) {
                            totaltaxCount = totaltaxCount + (percentagerList.size());
                        }
                        percentagerList = null;
                    }
                }


                ArrayList<SubDepotBo> distributorList = LoadManagementHelper.getInstance(this).getSubDepotList();
                String distributorAddress1 = "";
                String distributorAddress2 = "";
                String distributorContactNo = "";
                if (distributorList != null) {
                    for (SubDepotBo subDepotBo : distributorList) {
                        distributorAddress1 = subDepotBo.getAddress1();
                        distributorAddress2 = subDepotBo.getAddress2();
                        distributorContactNo = subDepotBo.getContactNumber();
                    }

                }


                int height = 0;

                height = 460
                        + ((totalOrderedBatchCount + totalFrreProudctCount + totaltaxCount) * 50) + 650;

                sb.append("! 0 200 200 " + (height * (printCount)) + " 1\r\n"
                        + "LEFT\r\n");
                boolean isOriginal = true;

                for (int j = 1; j <= printCount; j++) {
                    if (j > 1)
                        isOriginal = false;
                    int x = 100;
                    int totalLength = height * (j - 1);
                    sb.append("T 5 0 10 " + (5 + totalLength));
                    if (isOriginal)
                        sb.append("Original Copy" + "\r\n");
                    else
                        sb.append("Duplicate Copy" + "\r\n");
                    sb.append("T 5 0 200 " + (20 + totalLength));


                    if (bmodel.getRetailerMasterBO().getTinnumber() != null && !bmodel.getRetailerMasterBO().getTinnumber().equals("")) {
                        sb.append("Tax Invoice  \r\n");
                    } else {
                        sb.append("Sales/Retail Invoice  \r\n");
                    }

                    sb.append("T 5 0 200 " + (50 + totalLength));
                    sb.append("Invoice No:" + bmodel.invoiceNumber + "\r\n");

                    sb.append("T 5 0 200 " + (80 + totalLength));
                    sb.append("Date:" + DateTimeUtils.convertFromServerDateToRequestedFormat(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), bmodel.configurationMasterHelper.outDateFormat) + "\r\n");


                    sb.append("T 5 0 10 " + (130 + totalLength));
                    sb.append("From " + "\r\n");

                    if (bmodel.userMasterHelper.getUserMasterBO().getDistributorName() != null) {
                        sb.append("T 5 0 10 " + (160 + totalLength));
                        if (bmodel.userMasterHelper.getUserMasterBO().getDistributorName().length() > 15) {
                            sb.append(bmodel.userMasterHelper.getUserMasterBO().getDistributorName().substring(0, 15) + "\r\n");
                            sb.append("T 5 0 10 " + (190 + totalLength));
                            if (bmodel.userMasterHelper.getUserMasterBO().getDistributorName().substring(15).length() > 15)
                                sb.append(bmodel.userMasterHelper.getUserMasterBO().getDistributorName().substring(15, 30) + "\r\n");
                            else
                                sb.append(bmodel.userMasterHelper.getUserMasterBO().getDistributorName().substring(15) + "\r\n");
                        } else {
                            sb.append(bmodel.userMasterHelper.getUserMasterBO().getDistributorName() + "\r\n");
                        }
                    }


                    if (bmodel.userMasterHelper.getUserMasterBO().getDistributorAddress3() != null) {
                        sb.append("T 5 0 10 " + (220 + totalLength));
                        if (bmodel.userMasterHelper.getUserMasterBO().getDistributorAddress3().length() > 15) {
                            sb.append(bmodel.userMasterHelper.getUserMasterBO().getDistributorAddress3().substring(0, 15) + " \r\n");
                        } else {
                            sb.append(bmodel.userMasterHelper.getUserMasterBO().getDistributorAddress3() + " \r\n");
                        }
                    }


                    sb.append("T 5 0 10 " + (250 + totalLength));
                    if (bmodel.userMasterHelper.getUserMasterBO().getDistributorTinNumber() != null) {
                        sb.append("TIN:-" + bmodel.userMasterHelper.getUserMasterBO().getDistributorTinNumber() + "\r\n");
                    } else {
                        sb.append("TIN:-" + "\r\n");
                    }
                    sb.append("T 5 0 10 " + (280 + totalLength));
                    if (bmodel.userMasterHelper.getUserMasterBO().getCstNo() != null) {
                        sb.append("CST:-" + bmodel.userMasterHelper.getUserMasterBO().getCstNo() + "\r\n");
                    } else {
                        sb.append("CST:-" + "\r\n");
                    }

                    sb.append("T 5 0 300 " + (130 + totalLength));
                    sb.append("To" + "\r\n");
                    sb.append("T 5 0 300 " + (160 + totalLength));
                    if (bmodel.getRetailerMasterBO().getRetailerName().length() > 15) {
                        sb.append(bmodel.getRetailerMasterBO().getRetailerName().substring(0, 15) + "\r\n");
                        sb.append("T 5 0 300 " + (190 + totalLength));
                        if (bmodel.getRetailerMasterBO().getRetailerName().substring(15).length() > 15)
                            sb.append(bmodel.getRetailerMasterBO().getRetailerName().substring(15, 30) + "\r\n");
                        else
                            sb.append(bmodel.getRetailerMasterBO().getRetailerName().substring(15) + "\r\n");
                    } else {
                        sb.append(bmodel.getRetailerMasterBO().getRetailerName() + "\r\n");
                    }

                    sb.append("T 5 0 300 " + (220 + totalLength));
                    if (bmodel.getRetailerMasterBO().getAddress3() != null) {
                        if (bmodel.getRetailerMasterBO().getAddress3().length() > 18) {
                            sb.append(bmodel.getRetailerMasterBO().getAddress3().substring(0, 18) + "\r\n");
                        } else {
                            sb.append(bmodel.getRetailerMasterBO().getAddress3() + "\r\n");
                        }
                    }


                    sb.append("T 5 0 300 " + (250 + totalLength));
                    if (bmodel.getRetailerMasterBO().getTinnumber() != null) {
                        sb.append("TIN:-" + bmodel.getRetailerMasterBO().getTinnumber() + "\r\n");
                    } else {
                        sb.append("TIN:-" + "\r\n");
                    }

                    sb.append("T 5 0 300 " + (280 + totalLength));
                    if (bmodel.getRetailerMasterBO().getCredit_invoice_count() != null) {
                        sb.append("CST:-" + bmodel.getRetailerMasterBO().getCredit_invoice_count() + "\r\n");
                    } else {
                        sb.append("CST:-" + "\r\n");
                    }


                    sb.append("T 5 0 10 " + (330 + totalLength));
                    sb.append("Material" + "\r\n");
                    sb.append("T 5 0 190 " + (330 + totalLength));
                    sb.append("Qty" + "\r\n");
                    sb.append("T 5 0 280 " + (330 + totalLength));
                    sb.append("UCP" + "\r\n");
                    /*sb.append("T 5 0 330 "+(270+totalLength));
                    sb.append("Val" + "\r\n");
                    sb.append("T 5 0 330 "+(300+totalLength));
                    sb.append("(excl Tax)" + "\r\n");*/
                    sb.append("T 5 0 420 " + (330 + totalLength));
                    sb.append("Payable" + "\r\n");
                    sb.append("T 5 0 420 " + (360 + totalLength));
                    sb.append("(incl.Tax)" + "\r\n");
                    x = x + 240 + totalLength;
                    double total = 0;
                    double totalExcludeTaxvalue = 0;
                    boolean isBatchwise = false;
                    double totalPriceOffValue = 0;
                    double totalCashDiscountValue = 0;
                    if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION
                            && bmodel.configurationMasterHelper.IS_INVOICE
                            && bmodel.configurationMasterHelper.IS_SIH_VALIDATION)
                        isBatchwise = true;
                    int totalQty = 0;
                    for (ProductMasterBO productBO : mOrderedProductList) {


                        if (isBatchwise && productBO.getBatchwiseProductCount() > 0) {

                            ArrayList<ProductMasterBO> batchList = bmodel.batchAllocationHelper.getBatchlistByProductID().get(productBO.getProductID());
                            if (batchList != null) {
                                for (ProductMasterBO batchProductBO : batchList) {

                                    int totalBatchQty = batchProductBO.getOrderedPcsQty() + batchProductBO.getOrderedCaseQty() * productBO.getCaseSize()
                                            + batchProductBO.getOrderedOuterQty() * productBO.getOutersize();

                                    if (totalBatchQty > 0) {

                                        x = x + 45;
                                        totalPriceOffValue = totalPriceOffValue + (totalBatchQty * batchProductBO.getPriceoffvalue());
                                        totalQty = totalQty + totalBatchQty;
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
                                        sb.append("T 5 0 10 " + x + " ");
                                        sb.append(productname + "\r\n");

                                        sb.append("T 5 0 10 " + (x + 25) + " ");
                                        if (batchProductBO.getBatchNo().equalsIgnoreCase("NA") || batchProductBO.getBatchNo().equalsIgnoreCase("none"))
                                            sb.append("" + "\r\n");
                                        else {
                                            sb.append(batchProductBO.getBatchNo() + "\r\n");
                                        }

                                        sb.append("T 5 0 190 " + x + " ");
                                        sb.append(totalBatchQty + "\r\n");
                                        sb.append("T 5 0 280 " + x + " ");
                                        sb.append(SDUtil.format((batchProductBO.getSrp() + batchProductBO.getPriceoffvalue()), 2, 0) + "\r\n");
//                                        sb.append("T 5 0 330 " + x + " ");
                                        totalExcludeTaxvalue = totalExcludeTaxvalue + (batchProductBO.getTaxableAmount() > 0 ? batchProductBO.getTaxableAmount() : batchProductBO.getNetValue());
//                                        sb.append(SDUtil.format(batchProductBO.getTaxableAmount() > 0 ? batchProductBO.getTaxableAmount() : batchProductBO.getNetValue(), 2, 0) + "\r\n");
                                        sb.append("T 5 0 420 " + x + " ");
                                        total = total + batchProductBO.getNetValue();
                                        sb.append(SDUtil.format(batchProductBO.getNetValue(), 2, 0) + "\r\n");


                                    }
                                }

                            }


                        } else {
                            x = x + 45;


                            int totalProductQty = productBO.getOrderedPcsQty() + productBO.getOrderedCaseQty() * productBO.getCaseSize()
                                    + productBO.getOrderedOuterQty() * productBO.getOutersize();
                            totalPriceOffValue = totalPriceOffValue + productBO.getPriceoffvalue();

                            totalQty = totalQty + totalProductQty;
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
                            sb.append("T 5 0 10 " + x + " ");
                            sb.append(productname + "\r\n");


                            sb.append("T 5 0 190 " + x + " ");
                            sb.append(totalProductQty + "\r\n");
                            sb.append("T 5 0 280 " + x + " ");
                            sb.append(productBO.getSrp() + "\r\n");
//                            sb.append("T 5 0 330 " + x + " ");
                            totalExcludeTaxvalue = totalExcludeTaxvalue + (productBO.getTaxableAmount() > 0 ? productBO.getTaxableAmount() : productBO.getNetValue());
//                            sb.append(SDUtil.format(productBO.getTaxableAmount() > 0 ? productBO.getTaxableAmount() : productBO.getNetValue(), 2, 0) + "\r\n");
                            sb.append("T 5 0 420 " + x + " ");
                            total = total + productBO.getNetValue();
                            sb.append(SDUtil.format(productBO.getNetValue(), 2, 0) + "\r\n");


                        }


                    }
                    x = x + 70;
                    sb.append("T 5 0 10 " + x + " ");
                    sb.append("TOTAL" + "\r\n");

                    sb.append("T 5 0 190 " + x + " ");
                    sb.append(totalQty + "\r\n");


//                    sb.append("T 5 0 330 " + x + " ");
//                    sb.append(SDUtil.format(totalExcludeTaxvalue, 2, 0) + "\r\n");
                    sb.append("T 5 0 420 " + x + " ");
                    sb.append(SDUtil.format(total, 2, 0) + "\r\n");
                    x = x + 50;

// print price off discount
                    sb.append("T 5 0 10 " + x + " ");
                    sb.append("Scheme Discount" + "\r\n");
                    sb.append("T 5 0 450 " + x + " ");
                    sb.append(SDUtil.format(totalPriceOffValue, 2, 0) + "\r\n");

                    // Print Item Level Discount

                    mTypeIdList = bmodel.productHelper.getTypeIdList();
                    mDiscountIdListByTypeId = bmodel.productHelper.getDiscountIdListByTypeId();
                    if (mTypeIdList != null && mDiscountIdListByTypeId != null) {
                        x = x + 20;
                        for (Integer typeId : mTypeIdList) {
                            ArrayList<Integer> discountIdList = mDiscountIdListByTypeId.get(typeId);
                            if (discountIdList != null) {

                                String discountDescription = "";

                                double totalDiscountValue = 0;
                                for (int discountid : discountIdList) {


                                    ArrayList<StoreWiseDiscountBO> discountList = bmodel.productHelper.getProductDiscountListByDiscountID().get(discountid);

                                    if (discountList != null) {

                                        for (StoreWiseDiscountBO storeWiseDiscountBO : discountList) {

                                            discountDescription = storeWiseDiscountBO.getDescription();
                                            ProductMasterBO productMasterBO = bmodel.productHelper.getProductMasterBOById(storeWiseDiscountBO.getProductId() + "");
                                            if (productMasterBO != null) {
                                                int totalProductQty = 0;
                                                totalProductQty = productMasterBO.getOrderedPcsQty()
                                                        + productMasterBO.getOrderedCaseQty() * productMasterBO.getCaseSize()
                                                        + productMasterBO.getOrderedOuterQty() * productMasterBO.getOutersize();
                                                if (totalProductQty > 0) {
                                                    if (productMasterBO.getBatchwiseProductCount() > 0) {

                                                        ArrayList<ProductMasterBO> batchList = bmodel.batchAllocationHelper.getBatchlistByProductID().get(productMasterBO.getProductID());
                                                        if (batchList != null) {
                                                            for (ProductMasterBO batchProductBO : batchList) {
                                                                double totalValue = 0;
                                                                double batchDiscountValue = 0;
                                                                int totalBatchQty = batchProductBO.getOrderedPcsQty() + batchProductBO.getOrderedCaseQty() * productMasterBO.getCaseSize()
                                                                        + batchProductBO.getOrderedOuterQty() * productMasterBO.getOutersize();

                                                                if (batchProductBO.getLineValueAfterSchemeApplied() > 0) {
                                                                    totalValue = batchProductBO.getLineValueAfterSchemeApplied();
                                                                } else {
                                                                    totalValue = batchProductBO.getOrderedPcsQty()
                                                                            * batchProductBO.getSrp()
                                                                            + batchProductBO.getOrderedCaseQty()
                                                                            * batchProductBO.getCsrp()
                                                                            + batchProductBO.getOrderedOuterQty()
                                                                            * batchProductBO.getOsrp();
                                                                }

                                                                if (storeWiseDiscountBO.getIsPercentage() == 1) {
                                                                    batchDiscountValue = totalValue * storeWiseDiscountBO.getDiscount() / 100;


                                                                } else if (storeWiseDiscountBO.getIsPercentage() == 0) {
                                                                    batchDiscountValue = totalBatchQty * storeWiseDiscountBO.getDiscount();
                                                                }

                                                                totalDiscountValue = totalDiscountValue + batchDiscountValue;


                                                            }
                                                        }


                                                    } else {
                                                        double totalValue = 0;
                                                        double productDiscount = 0;

                                                        if (productMasterBO.getLineValueAfterSchemeApplied() > 0) {
                                                            totalValue = productMasterBO.getLineValueAfterSchemeApplied();
                                                        } else {
                                                            totalValue = productMasterBO.getOrderedPcsQty() * productMasterBO.getSrp()
                                                                    + productMasterBO.getOrderedCaseQty() * productMasterBO.getCsrp()
                                                                    + productMasterBO.getOrderedOuterQty() * productMasterBO.getOsrp();
                                                        }

                                                        if (storeWiseDiscountBO.getIsPercentage() == 1) {
                                                            productDiscount = totalValue * storeWiseDiscountBO.getDiscount() / 100;


                                                        } else if (storeWiseDiscountBO.getIsPercentage() == 0) {
                                                            productDiscount = totalProductQty * storeWiseDiscountBO.getDiscount();
                                                        }

                                                        totalDiscountValue = totalDiscountValue + productDiscount;


                                                    }
                                                }

                                            }

                                        }


                                    }
                                }
                                if (totalDiscountValue > 0) {
                                    x = x + 40;
                                    sb.append("T 5 0 10 " + x + " ");
                                    if (discountDescription.length() < 10) {
                                        sb.append(discountDescription + "\r\n");
                                    } else {
                                        sb.append(discountDescription.substring(0, 10) + "\r\n");
                                    }
                                    sb.append("T 5 0 450 " + x + " ");
                                    sb.append(SDUtil.format(totalDiscountValue, 2, 0) + "\r\n");
                                }
                            }


                        }
                    }

                    //print cash  discount value
                    x = x + 40;
                    sb.append("T 5 0 10 " + x + " ");
                    sb.append("CashDiscount" + "\r\n");
                    sb.append("T 5 0 450 " + x + " ");
                    sb.append(SDUtil.format(entryLevelDis, 2, 0) + "\r\n");

                    //print tax
                    x = x + 100;

                    HashMap<String, HashSet<String>> productListByGroupId = bmodel.productHelper.taxHelper.getProductIdByTaxGroupId();

                    SparseArray<LinkedHashSet<TaxBO>> taxListByGroupId = bmodel.productHelper.taxHelper.getTaxBoByGroupId();
                    if (groupIdList != null) {
                        String taxDesc = "";
                        String previousTaxDesc = "";

                        for (TaxBO taxBO : groupIdList) {


                            LinkedHashSet<TaxBO> totalTaxList = taxListByGroupId.get(taxBO.getGroupId());

                            if (totalTaxList != null) {


                                for (TaxBO totalTaxBO : totalTaxList) {
                                    taxDesc = totalTaxBO.getTaxDesc2();

                                    double taxpercentege = totalTaxBO.getTaxRate();
                                    HashSet<String> taxProductList = productListByGroupId.get(taxBO.getGroupId() + "" + taxpercentege);
                                    double totalTax = 0.0;
                                    double totalExcludeValue = 0.0;
                                    if (taxProductList != null) {
                                        for (String productid : taxProductList) {
                                            ProductMasterBO prodcutBO = bmodel.productHelper.getProductMasterBOById(productid);
                                            if (prodcutBO != null) {
                                                totalExcludeValue = totalExcludeValue + prodcutBO.getTaxableAmount();
                                                totalTax = totalTax + (prodcutBO.getTaxableAmount() * taxpercentege) / 100;

                                            }
                                        }
                                        if (totalTax > 0) {
                                            if (!taxDesc.equals(previousTaxDesc)) {
                                                sb.append("T 5 0 10 " + x + " ");
                                                if (taxDesc.length() > 10) {
                                                    sb.append(taxDesc.substring(0, 10) + "\r\n");
                                                } else {
                                                    sb.append(taxDesc + "\r\n");
                                                }
                                            }
                                            sb.append("T 5 0 200 " + x + " ");
                                            sb.append(taxpercentege + "% on Rs " + SDUtil.format(totalExcludeValue, 2, 0) + "\r\n");
                                            sb.append("T 5 0 450 " + x + " ");
                                            sb.append(SDUtil.format(totalTax, 2, 0) + "\r\n");
                                            x = x + 50;


                                        }
                                    }
                                    previousTaxDesc = taxDesc;


                                }

                            }

                        }
                    }
                    x = x + 40;
                    sb.append("T 5 0 10 " + x + " ");
                    sb.append("Net Payable" + "\r\n");

                    sb.append("T 5 0 450 " + x + " ");
                    String formatTotal = SDUtil.format(Math.round(total), 2, 0);
                    sb.append(formatTotal + "\r\n");

                    if (formatTotal.length() <= 12) {
                        String[] splits = formatTotal.split(Pattern.quote("."));
                        StringBuffer convertBuffer = new StringBuffer();
                        NumberToWord numberToWord = new NumberToWord();
                        for (int i = 0; i < splits.length; i++) {
                            long splitvalue = SDUtil.convertToLong(splits[i]);
                            if (i == 1 && splitvalue > 0) {
                                convertBuffer.append(" and ");
                            }
                            convertBuffer.append(numberToWord.convertNumberToWords(SDUtil.convertToLong(splits[i].toString())));
                            if (i == 0) {
                                convertBuffer.append(" Rupees ");
                            } else if (i == 1) {
                                if (!splits[i].toString().equals("00"))
                                    convertBuffer.append(" Paise");
                            }

                        }

                        sb.append("T 5 0 10 " + (x + 30) + " ");
                        sb.append("In Words" + "\r\n");

                        if (convertBuffer.length() < 40) {
                            sb.append("T 7 0 10 " + (x + 70) + " ");
                            sb.append(convertBuffer.toString() + "\r\n");
                        } else {
                            sb.append("T 7 0 10 " + (x + 70) + " ");
                            sb.append(convertBuffer.substring(0, 40) + "\r\n");
                        /*sb.append("T 7 0 25 " + (x + 70) + " ");
                        sb.append(convertBuffer.substring(40) + "\r\n");*/

                            try {
                                int startat = 100;
                                String str = convertBuffer.substring(40, convertBuffer.length());
                                while (str.length() > 0) {
                                    if (str.length() > 40) {
                                        sb.append("T 7 0 25 " + (x + startat) + " ");
                                        sb.append(str.substring(0, 40) + "\r\n");
                                        startat = startat + 30;
                                        str = str.substring(40, str.length());
                                    } else {
                                        sb.append("T 7 0 25 " + (x + startat) + " ");
                                        sb.append(str + "\r\n");
                                        str = "";
                                    }
                                }
                            } catch (Exception e) {
                                Commons.printException(e);
                            }

                        }
                        convertBuffer = null;
                        numberToWord = null;
                    }

                    sb.append("T 5 0 10 " + (x + 190) + " ");
                    sb.append("For (RS Name) " + "\r\n");

                    sb.append("T 5 0 10 " + (x + 270) + " ");
                    sb.append("Received " + "\r\n");

                    sb.append("T 5 0 10 " + (x + 300) + " ");
                    sb.append("------------------------------------------------------------ " + "\r\n");


                }

                sb.append("PRINT \r\n");
                printDataBytes = sb.toString().getBytes();


            }
        } catch (Exception e) {
            Commons.printException(e);
        }
        return printDataBytes;
    }


    /**
     * set image in the preview screen *
     */
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
     * printing invoice *
     */
    public void printInvoice() {

        try {

            zebraPrinterConnection.write(printDataforTitan3inchprinter(mOrderedProductList, productEntryLevelDis, SDUtil.convertToInt(count)));
            bmodel.updatePrintCount(1);

            if (!IsFromReport) {
                bmodel.showAlert(
                        getResources().getString(
                                R.string.printed_successfully), 1234);
            } else {
                bmodel.showAlert(
                        getResources().getString(
                                R.string.printed_successfully), 121);
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
        String macAddress = getMacAddressFieldText();
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
             * Commons.printException(e);
             *
             * isPrinterLanguageDetected = false;
             *
             * // printer = null; // DemoSleeper.sleep(1000); // disconnect(); }
             */
        }

        return printer;
    }

    private void doConnection() {
        try {
            printer = connect();
            if (printer != null) {
                // sendTestLabel();
                printInvoice();
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
                                        + orderHelper.getOrderId(),
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
                                doConnection();
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


    public String getMacAddressFieldText() {
        String macAddress = null;
        try {
            // String macAddress = "00:22:58:08:1E:37";

            SharedPreferences pref = this.getSharedPreferences("PRINT",
                    MODE_PRIVATE);
            macAddress = pref.getString("MAC", "");
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
                        PrintPreviewScreenTitan.this).setTitle("Choose Printer")
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

                                                doConnection();
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