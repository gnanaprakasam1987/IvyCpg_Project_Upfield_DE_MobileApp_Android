package com.ivy.sd.png.view;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.ivy.cpg.view.order.OrderHelper;
import com.ivy.lib.Utils;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.SchemeProductBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.DateUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Formatter;
import java.util.List;
import java.util.Vector;

public class Bixolon extends IvyBaseActivityNoActionBar {

    public static final String TAG = "InvoicePrint";

    public TextView mStatusTV, mDateTimeTV, mDistributorNameTV, mOutletNameTV,
            mTINNumberTV, mTotalQuantityTV, mTotalValueTV, mDiscoutnValueTV,
            mBillValueTV, mVATValueTV, mDistContactTV, mSellerName, mBeatName,
            mretailercode, mRetailerAddressTV, mNetSalesTV, mTotalAmountDueTV,
            mOrderIdTv, mInvoiceNoTv, qtytv;
    public LinearLayout mProductContainerLL;
    public ImageView mStatusIV;
    public Vector<ProductMasterBO> mProducts = new Vector<ProductMasterBO>();
    public Vector<ProductMasterBO> mProductsForAdapter = new Vector<ProductMasterBO>();
    public List<ProductMasterBO> mTempProducts = new ArrayList<ProductMasterBO>();

    public int mTotalQuantity;
    public double mTotalValue, mVATValue, mBillValue, mTotalAmountDue;
    public String mDate, mTINNumber, mInvoiceNumber, mDistributorName,
            mDistributorContact, mOutletName, mRetailerAddress, mNetSales,
            mOrderId;
    public String mPrintProducts, mPrintTotal, mPrintVat, mPrintDiscount,
            mPrintBillValue;
    public BusinessModel bmodel;
    public String count;
    public Spinner printcount;
    public ArrayAdapter<CharSequence> spinadapter;
    public StringBuilder sb = new StringBuilder();
    public Formatter f = new Formatter(sb);

    // Title
    public int QTY_SIZE = 12; // Used to display the Qty - title
    public int ITEM = 4; // Used to display item - Title
    public int PRICE_HEADER = 10;
    public int TOTAL_SIZE_TITLE = 20; // Used to display total - title

    // Value
    public int PROD_QTY_SIZE = 20; // used to display product qty - value
    public int PRICE_SIZE = 12; // Used to display product Price - value 8
    public int PRODUCT_SIZE = -54; // used to display product name - value
    public int TOTAL_SIZE = 28; // Used to display total - value & title

    // Others
    public int ITEM_SIZE = -32;
    public int OTHER_VALUE_SIZE = 26;
    public int LINE_SIZE = -64;
    public int AMOUNTDUE_SIZE = 24;

    public int itemsize, pricesize, totalsize, linesize, prodqtysize,
            totalsizetitle, item, productnamesize, priceheader;
    ;
    public double qtysize, othervalueprice, amountduesize;
    int printersize;
    public int lines = 0;
    private double grossvalue, NetSales;
    public String title;
    public String receipttitle;
    public double discount = 0;
    private Toolbar toolbar;
    private OrderHelper orderHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.invoice_print_preview_bixolon);
        title = getResources().getString(R.string.inv_print_prieview);
        receipttitle = getResources().getString(R.string.invoice_slip);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        orderHelper = OrderHelper.getInstance(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        mStatusTV = (TextView) findViewById(R.id.status_tv);

        mDateTimeTV = (TextView) findViewById(R.id.date_time_tv);
        mDistContactTV = (TextView) findViewById(R.id.dist_contact_tv);
        mDistributorNameTV = (TextView) findViewById(R.id.distributor_name_tv);
        mOutletNameTV = (TextView) findViewById(R.id.outlet_name_tv);
        mTINNumberTV = (TextView) findViewById(R.id.tin_number_tv);
        mTotalQuantityTV = (TextView) findViewById(R.id.total_qty_tv);
        mTotalValueTV = (TextView) findViewById(R.id.total_price_tv);
        mVATValueTV = (TextView) findViewById(R.id.vat_value_tv);
        mDiscoutnValueTV = (TextView) findViewById(R.id.discount_value_tv);

        mBillValueTV = (TextView) findViewById(R.id.bill_value_tv);
        mSellerName = (TextView) findViewById(R.id.userNameTv);

        mBeatName = (TextView) findViewById(R.id.beat_name_tv);
        mretailercode = (TextView) findViewById(R.id.retailer_code_tv);

        mRetailerAddressTV = (TextView) findViewById(R.id.retailer_address_tv);
        mNetSalesTV = (TextView) findViewById(R.id.netdales_value);
        mTotalAmountDueTV = (TextView) findViewById(R.id.totalamountdue_value);
        mStatusIV = (ImageView) findViewById(R.id.status_iv);
        mOrderIdTv = (TextView) findViewById(R.id.order_id);
        mInvoiceNoTv = (TextView) findViewById(R.id.inv_no);
        qtytv = (TextView) findViewById(R.id.qtytv);
        if (bmodel.configurationMasterHelper.SHOW_ORDER_PCS && bmodel.configurationMasterHelper.SHOW_ORDER_CASE && bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
            qtytv.setText(getResources().getString(R.string.qty) + "   ("
                    + getResources().getString(R.string.item_piece) + ","
                    + getResources().getString(R.string.item_case) + "," + getResources().getString(R.string.item_outer) + ")");
        else if (bmodel.configurationMasterHelper.SHOW_ORDER_PCS && bmodel.configurationMasterHelper.SHOW_ORDER_CASE)
            qtytv.setText(getResources().getString(R.string.qty) + "   ("
                    + getResources().getString(R.string.item_piece) + ","
                    + getResources().getString(R.string.item_case) + ")");
        else
            qtytv.setText(getResources().getString(R.string.qty) + "   ("
                    + getResources().getString(R.string.item_piece) + ")");
        mProductContainerLL = (LinearLayout) findViewById(R.id.product_container_ll);
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

        if (bmodel.configurationMasterHelper.PRINTER_SIZE == 2)
            printersize = 2;
        else if (bmodel.configurationMasterHelper.PRINTER_SIZE == 4)
            printersize = 1;
        else if (bmodel.configurationMasterHelper.PRINTER_SIZE == 3)
            printersize = (3 / 4);
        else
            printersize = 1;
        itemsize = ITEM_SIZE / printersize;
        qtysize = Math.ceil(QTY_SIZE / printersize);
        pricesize = PRICE_SIZE / printersize;
        totalsize = TOTAL_SIZE / printersize;
        totalsizetitle = TOTAL_SIZE_TITLE / printersize;
        othervalueprice = Math.ceil(OTHER_VALUE_SIZE / printersize);
        linesize = LINE_SIZE / printersize;
        prodqtysize = PROD_QTY_SIZE / printersize;
        amountduesize = AMOUNTDUE_SIZE / printersize;

        item = ITEM / printersize; // Item title size
        productnamesize = PRODUCT_SIZE / printersize;
        priceheader = PRICE_HEADER / printersize;
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            // mDiscoutnValueTV.setText(bmodel.invoiceDisount + "%");

            if (bmodel.configurationMasterHelper.discountType == 0)
                discount = 0;
            else
                discount = SDUtil.convertToDouble(orderHelper.invoiceDiscount);
            mDiscoutnValueTV
                    .setText(bmodel.formatValue(discount)
                            + " "
                            + (bmodel.configurationMasterHelper.discountType == 0 ? ""
                            : (bmodel.configurationMasterHelper.discountType == 1 ? "%"
                            : "Amt")));

            if (null == mProducts) {
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

                mProducts.add(mTempProducts.get(j));
                Commons.print("b n=" + mProducts.get(j).getBrandname());
            }
            //

            mDate = DateUtil.convertFromServerDateToRequestedFormat(
                    SDUtil.now(SDUtil.DATE_GLOBAL),
                    bmodel.configurationMasterHelper.outDateFormat)
                    + " "
                    + SDUtil.now(SDUtil.TIME);

            mDistributorName = bmodel.userMasterHelper.getUserMasterBO()
                    .getDistributorName();
            mDistributorContact = bmodel.userMasterHelper.getUserMasterBO()
                    .getDistributorContactNumber();

            mTINNumber = bmodel.getRetailerMasterBO().getTinnumber();
            mOutletName = bmodel.getRetailerMasterBO().getRetailerName();
            mRetailerAddress = bmodel.getRetailerMasterBO().getAddress1();

            mOrderId = OrderHelper.getInstance(this).getOrderId();
            mInvoiceNumber = getInvoiceNumber(mOrderId);
            Commons.print("orderid" + mOrderId + " invoicenum" + mInvoiceNumber);
            mSellerName.setText(bmodel.userMasterHelper.getUserMasterBO()
                    .getUserName());
            mDateTimeTV.setText(getResources().getString(R.string.date) + ":"
                    + mDate.trim());
            mDistributorNameTV.setText(mDistributorName);

            mOutletNameTV.setText(getResources().getString(R.string.customer)
                    + mOutletName.trim());
            mRetailerAddressTV.setText(getResources().getString(
                    R.string.Address)
                    + ":" + mRetailerAddress.trim());
            if (!mTINNumber.equals("null")) {
                mTINNumberTV.setText(mTINNumber);
            }
            mInvoiceNoTv.setText(getResources().getString(R.string.invno) + ":"
                    + mInvoiceNumber);
            mOrderIdTv.setText(getResources().getString(R.string.ord_id) + ":"
                    + mOrderId);

            updatePreview();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private void updatePreview() {
        // clear previous data

        mProductContainerLL.removeAllViews();

        sb.delete(0, sb.length());

        f.format("%" + linesize + "s\n",
                "----------------------------------------------------------------");
        int QtySize = (int) qtysize;
        f.format("%" + item + "s %" + QtySize + "s %" + priceheader + "s %"
                        + totalsizetitle + "s\n",
                getResources().getString(R.string.item), getResources()
                        .getString(R.string.qty),
                getResources().getString(R.string.price), getResources()
                        .getString(R.string.total));
        f.format("%12s\n", "(" + getResources().getString(R.string.item_piece)
                + "," + getResources().getString(R.string.item_case) + ")");
        f.format("%" + linesize + "s\n",
                "----------------------------------------------------------------");

        mTotalQuantity = 0;
        mTotalValue = 0;
        mVATValue = 0;
        mBillValue = 0;
        int quantity = 0;
        float price = 0;
        String productName = "";
        LayoutInflater inflater = getLayoutInflater();
        int count = 1;
        SchemeProductBO schemebo;
        // Collections.sort(mProducts, ProductMasterBO.BrandiseAscending);
        Collections.sort(mProducts, new Comparator<ProductMasterBO>() {
            @Override
            public int compare(final ProductMasterBO object1,
                               final ProductMasterBO object2) {
                int result = 0;
                try {
                    result = object1.getBrandname().compareTo(
                            object2.getBrandname());
                    if (result == 0) {
                        result = Float.compare(object1.getSrp(),
                                object2.getSrp());
                        return result;
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    Commons.printException(e);
                }
                return result;

            }
        });
        for (ProductMasterBO productBO : mProducts) {
            if ((productBO.getOrderedPcsQty() > 0 || productBO
                    .getOrderedCaseQty() > 0)
                    || productBO.getOrderedOuterQty() > 0) {
                mProductsForAdapter.add(productBO);

                lines++;
                String casePcsQty;
                if (bmodel.configurationMasterHelper.SHOW_ORDER_PCS && bmodel.configurationMasterHelper.SHOW_ORDER_CASE && bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
                    casePcsQty = productBO.getOrderedPcsQty() + ","
                            + productBO.getOrderedCaseQty() + ","
                            + productBO.getOrderedOuterQty();
                else if (bmodel.configurationMasterHelper.SHOW_ORDER_PCS && bmodel.configurationMasterHelper.SHOW_ORDER_CASE)
                    casePcsQty = productBO.getOrderedPcsQty() + ","
                            + productBO.getOrderedCaseQty();
                else
                    casePcsQty = productBO.getOrderedPcsQty() + "";
                quantity = (productBO.getOrderedPcsQty())
                        + (productBO.getOrderedCaseQty() * productBO
                        .getCaseSize());
                price = productBO.getSrp();
                double total = quantity * productBO.getSrp();

                mTotalQuantity = mTotalQuantity + quantity;
                mTotalValue = mTotalValue + (total);

                productName = productBO.getProductName();
                f.format("%" + productnamesize + "s\n", count + "."
                        + productName);

                f.format("%" + prodqtysize + "s %" + pricesize + "s %"
                                + totalsize + "s\n", casePcsQty,
                        bmodel.formatValue(price), bmodel.formatValue(total));
                View v = inflater.inflate(
                        R.layout.row_invoice_print_preview_bixolon, null);
                ((TextView) v.findViewById(R.id.productno))
                        .setText(count + ".");
                count = count + 1;
                ((TextView) v.findViewById(R.id.product_name_tv))
                        .setText(productName);
                ((TextView) v.findViewById(R.id.ou_qty)).setText(casePcsQty
                        + "");
                ((TextView) v.findViewById(R.id.msq_qty)).setText(productBO
                        .getOrderedPcsQty() + "");
                ((TextView) v.findViewById(R.id.price_tv)).setText(bmodel
                        .formatValue(productBO.getSrp()) + "");
                ((TextView) v.findViewById(R.id.total)).setText(bmodel
                        .formatValue(total));
                ((TextView) v.findViewById(R.id.vat_tv)).setText(Utils
                        .formatAsTwoDecimal((double) productBO.getVat()));
                mProductContainerLL.addView(v);
                if (productBO.getIsscheme() == 1
                        && productBO.getSchemeProducts() != null) {
                    int cnt = 1;
                    int size = productBO.getSchemeProducts().size();
                    for (int i = 0; i < size; i++) {
                        schemebo = productBO.getSchemeProducts().get(i);
                        View view = inflater.inflate(
                                R.layout.row_scheme_print_preview, null);
                        f.format("%" + productnamesize + "s\n", cnt + "."
                                + schemebo.getProductFullName());
                        ProductMasterBO schemeProduct = bmodel.productHelper
                                .getProductMasterBOById(schemebo.getProductId());
                        int totalQty = 0;

                        if (schemebo.getUomID() != 0) {
                            if (schemeProduct != null) {
                                if (schemebo.getUomID() == schemeProduct
                                        .getCaseUomId()) { // case
                                    f.format(
                                            "%" + prodqtysize + "s %"
                                                    + pricesize + "s %"
                                                    + totalsize + "s\n",
                                            schemebo.getQuantitySelected()
                                                    * schemeProduct
                                                    .getCaseSize(), 0,
                                            0);
                                    totalQty = schemebo.getQuantitySelected()
                                            * schemeProduct.getCaseSize();

                                } else if (schemebo.getUomID() == schemeProduct
                                        .getOuUomid()) { // outer
                                    f.format(
                                            "%" + prodqtysize + "s %"
                                                    + pricesize + "s %"
                                                    + totalsize + "s\n",
                                            schemebo.getQuantitySelected()
                                                    * schemeProduct
                                                    .getOutersize(), 0,
                                            0);
                                    totalQty = schemebo.getQuantitySelected()
                                            * schemeProduct.getOutersize();
                                } else {
                                    f.format("%" + prodqtysize + "s %"
                                                    + pricesize + "s %" + totalsize
                                                    + "s\n",
                                            schemebo.getQuantitySelected(), 0,
                                            0);
                                    totalQty = schemebo.getQuantitySelected();

                                }
                            }
                        } else {
                            f.format("%" + prodqtysize + "s %" + pricesize
                                            + "s %" + totalsize + "s\n",
                                    schemebo.getQuantitySelected(), 0, 0);
                            totalQty = schemebo.getQuantitySelected();

                        }

                        ((TextView) view.findViewById(R.id.productno))
                                .setText(cnt + ".");
                        cnt = cnt + 1;
                        ((TextView) view.findViewById(R.id.product_name_tv))
                                .setText(schemebo.getProductFullName());

                        ((TextView) view.findViewById(R.id.ou_qty))
                                .setText(totalQty + "");

                        mProductContainerLL.addView(view);

                    }
                }

            }
        }

        mVATValue = mVATValue + (mTotalValue - (mTotalValue * 100 / 112));

        // double discount = (mTotalValue / 100)
        // * SDUtil.convertToFloat(bmodel.invoiceDisount);
        //
        // mBillValue = mTotalValue - discount;
        grossvalue = ((mTotalValue * 100 / 112));
        if (null == mProductsForAdapter) {
            bmodel.showAlert("No Products exists", 0);
            return;
        }

        f.format("%" + linesize + "s\n",
                "----------------------------------------------------------------");
        mPrintProducts = sb.toString();
        sb.delete(0, sb.length());
        int OtherValuePrice = (int) othervalueprice;
        f.format("%" + itemsize + "s %" + OtherValuePrice + "s\n",
                getResources().getString(R.string.gross_sales),
                bmodel.formatValue(grossvalue));

        mPrintTotal = sb.toString();

        sb.delete(0, sb.length());

        f.format("%" + itemsize + "s %" + OtherValuePrice + "s\n",
                getResources().getString(R.string.vatt),
                Utils.trimRight(Utils.formatAsTwoDecimal(mVATValue), ".00"));
        mPrintVat = sb.toString();

        sb.delete(0, sb.length());
        // f.format("%" + itemsize + "s %" + OtherValuePrice + "s%%\n",
        // getResources().getString(R.string.discount),
        // SDUtil.convertToFloat(bmodel.invoiceDisount));
        f.format("%" + itemsize + "s %" + OtherValuePrice + "s%%\n",
                getResources().getString(R.string.discount),
                bmodel.formatValue(discount));
        mPrintDiscount = sb.toString();

        sb.delete(0, sb.length());
        f.format("%" + itemsize + "s %" + OtherValuePrice + "s\n",
                getResources().getString(R.string.bill_value),
                bmodel.formatValue(mBillValue));
        mPrintBillValue = sb.toString();

        mTotalQuantityTV.setText(Utils.trimRight(
                String.valueOf(mTotalQuantity), ".00"));
        // mTotalValueTV.setText(Utils.trimRight(
        // Utils.formatAsTwoDecimal(mTotalValue), ".00"));

        mTotalValueTV.setText(bmodel.formatValue(grossvalue));
        NetSales = getDiscountAppliedValue(discount, grossvalue);
        mNetSalesTV.setText(bmodel.formatValue(NetSales));
        mTotalAmountDue = getDiscountAppliedValue(discount, mTotalValue);
        mTotalAmountDueTV.setText(bmodel.formatValue(mTotalAmountDue));
        mVATValueTV.setText(bmodel.formatValue(mVATValue));
        // mBillValueTV.setText(Utils.trimRight(
        // Utils.formatAsTwoDecimal(mBillValue), ".00"));
        mBillValueTV.setText(bmodel.formatValue(mBillValue));

        mBeatName.setText(SDUtil.today());
        mretailercode.setText(bmodel.getRetailerMasterBO().getRetailerCode());

    }

    void CheckGC() {
        CheckGC("");
    }

    void CheckGC(String FunctionName) {
        long VmfreeMemory = Runtime.getRuntime().freeMemory();
        long VmmaxMemory = Runtime.getRuntime().maxMemory();
        long VmtotalMemory = Runtime.getRuntime().totalMemory();
        long Memorypercentage = ((VmtotalMemory - VmfreeMemory) * 100)
                / VmtotalMemory;

        Commons.print(TAG + FunctionName + "Before Memorypercentage"
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
        Commons.print(TAG + FunctionName + "_After Memorypercentage"
                + Memorypercentage + "% VmtotalMemory[" + VmtotalMemory + "] "
                + "VmfreeMemory[" + VmfreeMemory + "] " + "VmmaxMemory["
                + VmmaxMemory + "] ");
    }

    public void printHeader() {

        sb.delete(0, sb.length());
        f.format("%s \n", title);
        printTextCenter(sb.toString(), DataMembers.PRINT_TEXT_SIZE);

        printLineFeed(1);

        sb.delete(0, sb.length());
        f.format("%s \n", mDistributorName);
        printTextCenter(sb.toString(), DataMembers.PRINT_TEXT_SIZE);

        sb.delete(0, sb.length());
        f.format("%s\n", receipttitle);
        printTextCenter(sb.toString(), DataMembers.PRINT_TEXT_SIZE);

        sb.delete(0, sb.length());
        f.format("%s\n", mDistributorContact);
        printTextCenter(sb.toString(), DataMembers.PRINT_TEXT_SIZE);

        sb.delete(0, sb.length());
        f.format("%s\n", mTINNumber);
        printTextCenter(sb.toString(), DataMembers.PRINT_TEXT_SIZE);

        sb.delete(0, sb.length());
        f.format("%s\n", getResources().getString(R.string.ord_id) + ":"
                + mOrderId);
        printTextCenter(sb.toString(), DataMembers.PRINT_TEXT_SIZE);

        sb.delete(0, sb.length());
        f.format("%s\n", getResources().getString(R.string.invno) + ":"
                + getInvoiceNumber(mOrderId));
        printTextCenter(sb.toString(), DataMembers.PRINT_TEXT_SIZE);

        printLineFeed(1);

        // BeatMasterBO b = getTodayBeat();
        // sb.delete(0, sb.length());
        // f.format("%" + linesize + "s\n", b.getBeatDescription());
        // printTextLeft(sb.toString(), DataMembers.PRINT_TEXT_SIZE);

        // sb.delete(0, sb.length());
        // f.format("%" + linesize + "s\n", bmodel.getRetailerMasterBO()
        // .getRetailerCode());
        // printTextLeft(sb.toString(), DataMembers.PRINT_TEXT_SIZE);

        // printLineFeed(1);
        // printTextCenter(getResources().getString(R.string.ph_no)
        // + mDistributorContact, DataMembers.PRINT_TEXT_SIZE);

        // printLineFeed(1);

        sb.delete(0, sb.length());
        f.format("%s\n", getResources().getString(R.string.customer)
                + mOutletName);
        printTextLeft(sb.toString(), DataMembers.PRINT_TEXT_SIZE);

        sb.delete(0, sb.length());
        f.format("%s\n", getResources().getString(R.string.Address) + ":"
                + mRetailerAddress);
        printTextLeft(sb.toString(), DataMembers.PRINT_TEXT_SIZE);

        sb.delete(0, sb.length());
        f.format("%s\n", mDate);
        printTextCenter(sb.toString(), DataMembers.PRINT_TEXT_SIZE);

        // sb.delete(0, sb.length());
        // f.format("%" + linesize + "s\n", mInvoiceNumber);
        // printTextLeft(sb.toString(), DataMembers.PRINT_TEXT_SIZE);
    }

    public void printOrder() {
        printTextLeft(mPrintProducts, DataMembers.PRINT_TEXT_SIZE);
        printTextLeft(mPrintTotal, DataMembers.PRINT_TEXT_SIZE);

        printTextLeft(mPrintDiscount, DataMembers.PRINT_TEXT_SIZE);
        // printTextLeft(mPrintBillValue, DataMembers.PRINT_TEXT_SIZE);
        int otherprice = (int) othervalueprice;
        sb.delete(0, sb.length());
        f.format("%" + itemsize + "s %" + otherprice + "s\n", getResources()
                .getString(R.string.net_sales), bmodel.formatValue(grossvalue));
        printTextLeft(sb.toString(), DataMembers.PRINT_TEXT_SIZE);
        printTextLeft(mPrintVat, DataMembers.PRINT_TEXT_SIZE);
        printLineFeed(2);

        // sb.delete(0, sb.length());
        // f.format("%s", getResources().getString(R.string.so_signature));
        // printTextRight(sb.toString(), DataMembers.PRINT_TEXT_SIZE);

        // printLineFeed(1);
        // sb.delete(0, sb.length());
        // f.format("%s",
        // bmodel.userMasterHelper.getUserMasterBO().getUserName());
        // printTextRight(sb.toString(), DataMembers.PRINT_TEXT_SIZE);
        int amountsize = (int) amountduesize;
        sb.delete(0, sb.length());
        f.format("%" + itemsize + "s %" + amountsize + "s\n", getResources()
                .getString(R.string.total_amount_due), bmodel
                .formatValue(mTotalAmountDue));
        printTextLeft(sb.toString(), DataMembers.PRINT_TEXT_SIZE);
        sb.delete(0, sb.length());
        f.format("%s", getResources().getString(R.string.received_by)
                + "_____________________________________________");// 45
        printTextRight(sb.toString(), DataMembers.PRINT_TEXT_SIZE);
        printLineFeed(3);
    }

    public void printTextRight(String date, int size) {

    }

    public void printTextLeft(String text, int size) {

    }

    public void printTextCenter(String text, int size) {

    }

    public void printLineFeed(int lines) {

    }

    public void updateStatus(String statusMessage, boolean isPositiveStatus) {
        mStatusTV.setText(statusMessage);
        if (isPositiveStatus) {
            mStatusTV.setTextColor(Color.GREEN);
            mStatusIV.setImageResource(R.drawable.greenball);
        } else {
            mStatusTV.setTextColor(Color.RED);
            mStatusIV.setImageResource(R.drawable.redball);
        }
    }


    public double getDiscountAppliedValue(double f, double total) {
        double discnt = f;
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

    private String getInvoiceNumber(String orderid) {
        String invno = null;
        DBUtil db = new DBUtil(this, DataMembers.DB_NAME, DataMembers.DB_PATH);
        db.openDataBase();
        Cursor c = db
                .selectSQL("select  InvoiceNo  from InvoiceMaster where orderid="
                        + orderid);
        if (c != null) {
            if (c.moveToNext()) {
                invno = c.getString(0);
            }
            c.close();
        }
        db.closeDB();
        return invno;
    }
}
