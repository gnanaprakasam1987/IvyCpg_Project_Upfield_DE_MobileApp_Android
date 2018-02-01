package com.ivy.sd.print;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.ivy.cpg.view.order.OrderHelper;
import com.ivy.lib.Utils;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.BeatMasterBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.DateUtil;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.ZebraPrinter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

public class Zebra extends IvyBaseActivityNoActionBar {

    public static final String TAG = "InvoicePrint";
    public static final String ZEBRA_2INCH = "2";
    public static final String ZEBRA_4INCH = "4";
    public static final int SELECTED_PRINTER_DIALOG = 1;
    public TextView mDateTimeTV, mDistributorNameTV, mOutletNameTV,
            mTINNumberTV, mInvoiceNumberTV, mTotalQuantityTV, mTotalValueTV,
            mDiscoutnValueTV, mDistContactTV, mSellerName, mTtcTv, mHtTv,
            mTvaTv, mTotalttcTv, mDistributor_tin_numberTV;
    public TextView mRouteNameTV, mRetailerCodeTV, mRetailerNameTV,
            mRetailerAddressTV;
    public LinearLayout mProductContainerLL;
    public Vector<ProductMasterBO> mProducts = new Vector<ProductMasterBO>();
    public Vector<ProductMasterBO> mProductsForAdapter = new Vector<ProductMasterBO>();
    private List<ProductMasterBO> products = new ArrayList<ProductMasterBO>();
    public List<ProductMasterBO> mTempProducts = new ArrayList<ProductMasterBO>();

    public TextView statusField;
    public EditText mMacAddressET;
    public int mTotalQuantity;
    public double mTotalValue;
    public ImageView mStatusIV;
    public String mDate, mTINNumber, mInvoiceNumber, mDistributorName,
            mDistributorContact, mOutletName;
    public BusinessModel bmodel;
    public ZebraPrinter printer;
    public String mRouteName, mRetailerName, mRetailerCode, mRetailerAddress;
    public final String[] mPrinterSelectionArray = {ZEBRA_2INCH, ZEBRA_4INCH};
    public String mSelectedPrinterName;
    public String count;
    public Spinner printcount;
    public ArrayAdapter<CharSequence> spinadapter;
    public int lines = 0;
    int space = 0;
    private Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.invoice_print_preview_new);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mDateTimeTV = (TextView) findViewById(R.id.date_time_tv);
        mDistContactTV = (TextView) findViewById(R.id.dist_contact_tv);
        mDistributorNameTV = (TextView) findViewById(R.id.distributor_name_tv);
        mOutletNameTV = (TextView) findViewById(R.id.outlet_name_tv);
        mTINNumberTV = (TextView) findViewById(R.id.tin_number_tv);
        mInvoiceNumberTV = (TextView) findViewById(R.id.invoice_number_tv);
        mTotalQuantityTV = (TextView) findViewById(R.id.total_qty_tv);
        mTotalValueTV = (TextView) findViewById(R.id.total_price_tv);
        mDiscoutnValueTV = (TextView) findViewById(R.id.discount_value_tv);
        mRouteNameTV = (TextView) findViewById(R.id.route_name_tv);
        mRetailerCodeTV = (TextView) findViewById(R.id.retailer_code_tv);
        mRetailerAddressTV = (TextView) findViewById(R.id.retailer_address_tv);
        mRetailerNameTV = (TextView) findViewById(R.id.retailer_name_tv);
        mDistributor_tin_numberTV = (TextView) findViewById(R.id.distributor_address_tv);

        mSellerName = (TextView) findViewById(R.id.userNameTv);

        mMacAddressET = (EditText) findViewById(R.id.et_mac);
        statusField = (TextView) findViewById(R.id.status_bar);

        mStatusIV = (ImageView) findViewById(R.id.status_iv);

        mProductContainerLL = (LinearLayout) findViewById(R.id.product_container_ll);

        mTtcTv = (TextView) findViewById(R.id.footer1);
        mHtTv = (TextView) findViewById(R.id.footer2);
        mTvaTv = (TextView) findViewById(R.id.footer3);
        mTotalttcTv = (TextView) findViewById(R.id.footer4);
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

        try {
            printcount = (Spinner) findViewById(R.id.printcount);
            spinadapter = new ArrayAdapter<CharSequence>(this,
                    android.R.layout.simple_spinner_item);
            spinadapter
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            for (int i = 1; i <= DataMembers.PRINT_COUNT; ++i) {

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
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            SharedPreferences sharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(this);

            Configuration config = new Configuration();
            Locale locale = config.locale;
            if (!Locale.getDefault().equals(
                    sharedPrefs.getString("languagePref", "en"))) {
                locale = new Locale(sharedPrefs.getString("languagePref", "en").substring(0, 2));
                Locale.setDefault(locale);
                config.locale = locale;
                getBaseContext().getResources().updateConfiguration(config,
                        getBaseContext().getResources().getDisplayMetrics());
            }

            mDiscoutnValueTV.setText(OrderHelper.getInstance(this).invoiceDisount + "%");

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
                mProducts.add(new ProductMasterBO(mTempProducts.get(j)));
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
            mInvoiceNumber = bmodel.getInvoiceNumber();

            mTINNumber = bmodel.getRetailerMasterBO().getTinnumber();
            mOutletName = bmodel.getRetailerMasterBO().getRetailerName();

            // if (mDistributorName.length() > 10) {
            // mDistributorName = mDistributorName.subSequence(0, 10) + "..";
            // }

            mSellerName.setText(bmodel.userMasterHelper.getUserMasterBO()
                    .getUserName());

            mDateTimeTV.setText(mDate);
            mDistributorNameTV.setText(mDistributorName);

            mDistContactTV.setText("Ph No: " + mDistributorContact);
            mOutletNameTV.setText(mOutletName);

            if (!mTINNumber.equals("null") && mTINNumber != null) {
                mTINNumberTV.setText(mTINNumber);
            }

            if (bmodel.userMasterHelper.getUserMasterBO()
                    .getDistributorTinNumber() != null
                    && !bmodel.userMasterHelper.getUserMasterBO()
                    .getDistributorTinNumber().equals("null")) {
                mDistributor_tin_numberTV.setText(bmodel.userMasterHelper
                        .getUserMasterBO().getDistributorTinNumber());

            } else {
                mDistributor_tin_numberTV.setText("");
            }

            updatePreview();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private boolean updatePreview() {

        try {
            mRetailerCode = bmodel.getRetailerMasterBO().getRetailerCode();
            mRetailerName = bmodel.getRetailerMasterBO().getContactname();
            mRetailerAddress = bmodel.getRetailerMasterBO().getAddress1();
            mRouteNameTV.setText(SDUtil.today());

            if (mRetailerCode != null) {
                mRetailerCodeTV.setText(mRetailerCode);
            } else {
                mRetailerCodeTV.setText("");
            }

            if (mRetailerName != null) {
                mRetailerNameTV.setText(mRetailerName);
            } else {
                mRetailerNameTV.setText("");
            }
            if (mRetailerAddress != null) {
                mRetailerAddressTV.setText(mRetailerAddress);

            } else {
                mRetailerAddressTV.setText("");

            }

            // clear previous data

            mProductContainerLL.removeAllViews();

            mTotalQuantity = 0;
            mTotalValue = 0;

            int quantity = 0;
            double price = 0.0;

            String productName = "";
            LayoutInflater inflater = getLayoutInflater();
            for (int i = 0; i < mProducts.size(); i++)
                products.add(mProducts.get(i));
            Collections.sort(products, new Comparator<ProductMasterBO>() {
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
            for (ProductMasterBO productBO : products) {
                // if (productBO.isCheked()
                // && (productBO.getCaseQty() > 0 || productBO.getPieceQty() >
                // 0)) {
                if ((productBO.getOrderedPcsQty() > 0 || productBO
                        .getOrderedCaseQty() > 0)) {
                    mProductsForAdapter.add(productBO);
                    lines++;
                    // quantity = productBO.getPieceQty()
                    // + (productBO.getCaseQty() * productBO.getUomqty());
                    // price = Math.round(quantity * productBO.getTrmrp());
                    // vat = price * productBO.getVat() / 100;

                    quantity = (productBO.getOrderedPcsQty())
                            + (productBO.getOrderedCaseQty() * productBO
                            .getCaseSize());
                    String showquantity = productBO.getOrderedCaseQty() + "/"
                            + productBO.getOrderedPcsQty();
                    price = productBO.getSrp();
                    float total = quantity * productBO.getSrp();

                    mTotalQuantity = mTotalQuantity + quantity;
                    mTotalValue = mTotalValue + (total);

                    productName = productBO.getProductName();
                    if (productName.length() > 12) {

                        // productName = productName.substring(0, end)

                        productName = productName.subSequence(0, 10) + ".";

                    } else {

                    }

                    View v = inflater.inflate(
                            R.layout.row_invoice_print_preview, null);

                    int qty = (productBO.getCaseSize() * productBO
                            .getOrderedCaseQty())
                            + productBO.getOrderedPcsQty();

                    ((TextView) v.findViewById(R.id.product_name_tv))
                            .setText(productName);
                    // ((TextView)
                    // v.findViewById(R.id.ou_qty)).setText(productBO
                    // .getOrder_ou_qty() + "");
                    ((TextView) v.findViewById(R.id.ou_qty))
                            .setText(showquantity + "");
                    ((TextView) v.findViewById(R.id.msq_qty)).setText(productBO
                            .getOrderedPcsQty() + "");
                    // ((TextView) v.findViewById(R.id.price_tv))
                    // .setText(Utils.trimRight(
                    // String.valueOf(Utils.formatAsTwoDecimal(price)),
                    // ".00"));
                    ((TextView) v.findViewById(R.id.price_tv)).setText(bmodel
                            .formatValue(productBO.getSrp()) + "");

                    // float tot = Math.round((productBO.getOrder_ou_qty() *
                    // productBO
                    // .getOuPriceValue())
                    // + (productBO.getOrder_msq_qty() * productBO
                    // .getPrice_Value()));

                    float tot = (productBO.getOrderedCaseQty() * productBO
                            .getCaseSize()) + productBO.getOrderedPcsQty();
                    float tot_price = tot * productBO.getSrp();

                    ((TextView) v.findViewById(R.id.total)).setText(bmodel
                            .formatValue(tot_price) + "");
                    ((TextView) v.findViewById(R.id.vat_tv)).setText(Utils
                            .formatAsTwoDecimal((double) productBO.getVat()));
                    mProductContainerLL.addView(v);
                }
            }

            if (null == mProductsForAdapter) {
                bmodel.showAlert("No Products exists", 0);
                return false;
            }

            mTotalQuantityTV.setText(Utils.trimRight(
                    String.valueOf(mTotalQuantity), ".00"));
            // mTotalValueTV.setText(Utils.trimRight(
            // Utils.formatAsTwoDecimal(mTotalValue), ".00"));

            mTotalValueTV.setText(bmodel.formatValue(mTotalValue));

            double ttc = (mTotalValue * 0.4) / 100;
            double tva = (mTotalValue * 20) / 100;
            double ht = mTotalValue - (ttc + tva);
            mTtcTv.setText(bmodel.formatValue(ttc) + "");
            mHtTv.setText(bmodel.formatValue(ht) + "");
            mTvaTv.setText(bmodel.formatValue(tva) + "");
            mTotalttcTv.setText(bmodel.formatValue(mTotalValue) + "");
            rightAlign(ht + "");
            Commons.print("HT" + String.valueOf(ht).length());
        } catch (Exception e) {
            Commons.printException(e);
        }
        return true;
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

    public byte[] printDatafor2inchprinter() {
        byte[] PrintDataBytes = null;

        try {
            PrinterLanguage printerLanguage = printer
                    .getPrinterControlLanguage();

            byte[] configLabel = null;
            if (printerLanguage == PrinterLanguage.ZPL) {
                configLabel = "^XA^FO17,16^GB379,371,8^FS^FT65,255^A0N,135,134^FDTEST^FS^XZ"
                        .getBytes();
            } else if (printerLanguage == PrinterLanguage.CPCL) {
                space = 0;
                int y = 0;
                int height = 0;
                int x = 370;
                height = x + mProductsForAdapter.size() * 40 + 430;
                Commons.print(TAG + "Heigt:" + height);
                String Printitem = "! 0 200 200 " + height + " 1\r\n"
                        + "CENTER\r\n";
                Printitem += "T 5 0 10 40"
                        + getResources()
                        .getString(R.string.print_invoice_print)
                        + "\r\n";
                Printitem += "T 5 0 10 60  "; /*
                                             * "print distributor name and distributor address"
											 */

                Printitem += "\r\n" + "T 5 0 10 80  \r\n";
                Printitem += "T 5 0 10 120 "
                        + bmodel.userMasterHelper.getUserMasterBO()
                        .getDistributorName() + "\r\n";

                Printitem += "\r\n" + "T 5 0 10 130  \r\n";
                Printitem += "T 5 0 10 150 "
                        + bmodel.userMasterHelper.getUserMasterBO()
                        .getDistributorTinNumber() + "\r\n";

                Printitem += "\r\n" + "T 5 0 10 160  \r\n";
                Printitem += "T 5 0 10 180 " + mDate + "\r\n";

                Printitem += "LEFT \r\n";
                BeatMasterBO b = getTodayBeat();
                Printitem += "T 5 0 20 200 ";
                Printitem += SDUtil.today() + "\r\n";

                // Printitem += "T 5 0 20 250 ";
                // Printitem += mRetailerCode;
                // Printitem += "\r\n";
                Printitem += "T 5 0 20 250 ";
                if (mOutletName.length() < 23) {
                    Printitem += mOutletName + "\r\n";
                } else {
                    Printitem += mOutletName.substring(0, 23) + "\r\n";
                }

                Printitem += "\r\n";

                Printitem += "CENTER\r\n";
                Printitem += "T 5 0 20 290 " + mInvoiceNumber + "\r\n";
                Printitem += "LEFT \r\n";
                Printitem += "T 5 0 10 310 --------------------------------------------------\r\n";
                Printitem += "T 5 0 20 330"
                        + getResources().getString(R.string.product_name_print)
                        + "\r\n";
                Printitem += "T 5 0 190 330"
                        + getResources().getString(R.string.qty_print) + "\r\n";
                Printitem += "T 5 0 250 330"
                        + getResources().getString(R.string.price_print)
                        + "\r\n";
                Printitem += "T 5 0 330 330"
                        + getResources().getString(R.string.total_print)
                        + "\r\n";
                Printitem += "T 5 0 10 350 --------------------------------------------------\r\n";
                x += 10;
                for (ProductMasterBO productBO : mProductsForAdapter) {
                    int qty = productBO.getOrderedPcsQty()
                            + productBO.getCaseSize()
                            * productBO.getOrderedCaseQty();
                    String printqty = productBO.getOrderedCaseQty() + "/"
                            + productBO.getOrderedPcsQty();
                    Commons.print(TAG + "qty:" + qty);
                    double total = qty * productBO.getSrp();

                    x += 40;
                    if (productBO.getProductName().length() > 13) {
                        Printitem += "T 5 0 20 "
                                + x
                                + " "
                                + productBO.getProductName().substring(0, 13)
                                .toLowerCase() + "\r\n";
                    } else {
                        Printitem += "T 5 0 20 " + x + " "
                                + productBO.getProductName().toLowerCase()
                                + "\r\n";
                    }
                    Printitem += "T 5 0 190 " + x + " " + printqty + "\r\n";
                    rightAlign(bmodel.formatValue(productBO.getSrp()));
                    y = 210 + space;
                    Printitem += "T 5 0 " + y + " " + x + " "
                            + bmodel.formatValue(productBO.getSrp()) + "\r\n";
                    rightAlign(bmodel.formatValue(total));
                    y = 320 + space;
                    Printitem += "T 5 0 " + y + " " + x + " "
                            + bmodel.formatValue(total) + "\r\n";
                }

                x += 40;
                Printitem += "T 5 0 10 "
                        + x
                        + " --------------------------------------------------\r\n";
                x += 31;
                Printitem += "T 5 0 20 " + x
                        + getResources().getString(R.string.total_print)
                        + "\r\n";
                rightAlign(bmodel.formatValue(mTotalValue));
                y = 320 + space;
                Printitem += "T 5 0 " + y + " " + x + " "
                        + bmodel.formatValue(mTotalValue) + "\r\n";

                double ttc = (mTotalValue * 0.4) / 100;
                double tva = (mTotalValue * 20) / 100;
                double ht = mTotalValue - (ttc + tva);

                x += 40;
                Printitem += "T 5 0 20 " + x
                        + getResources().getString(R.string.print_footer1)
                        + "\r\n";
                rightAlign(ttc + "");
                y = 320 + space;
                Printitem += "T 5 0 " + y + " " + x + " "
                        + bmodel.formatValue(ttc) + "\r\n";

                x += 30;
                Printitem += "T 5 0 20 " + x
                        + getResources().getString(R.string.print_footer2)
                        + "\r\n";
                rightAlign(ht + "");
                y = 320 + space;
                Printitem += "T 5 0 " + y + " " + x + " "
                        + bmodel.formatValue(ht) + "\r\n";

                x += 30;
                Printitem += "T 5 0 20 " + x
                        + getResources().getString(R.string.print_footer3)
                        + "\r\n";
                rightAlign(tva + "");
                y = 320 + space;
                Printitem += "T 5 0 " + y + " " + x + " "
                        + bmodel.formatValue(tva) + "\r\n";

                x += 60;
                Printitem += "T 5 0 20 " + x
                        + getResources().getString(R.string.print_footer4)
                        + "\r\n";
                rightAlign(bmodel.formatValue(mTotalValue));
                y = 320 + space;
                Printitem += "T 5 0 " + y + " " + x + " "
                        + bmodel.formatValue(mTotalValue) + "\r\n";
                Printitem += "T 5 0 400 " + x + " MAD\r\n";

                // x += 60;
                // Printitem += "T 5 0 260 " + x
                // + getResources().getString(R.string.so_signature)
                // + "\r\n";
                x += 60;
                Printitem += "T 5 0 200 "
                        + x
                        + " "
                        + bmodel.userMasterHelper.getUserMasterBO()
                        .getUserName() + "\r\n";

                Printitem += "\r\n";
                Printitem += "\r\n";
                Printitem += "\r\n";
                Printitem += "PRINT\r\n";

                PrintDataBytes = Printitem.getBytes();
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
        return PrintDataBytes;

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
                space = 0;
                int y = 0;
                int height = 0;
                int x = 370;
                height = x + mProductsForAdapter.size() * 40 + 430;
                Commons.print(TAG + "Heigt:" + height);
                String Printitem = "! 0 200 200 " + height + " 1\r\n"
                        + "CENTER\r\n";

                Printitem += "T 5 0 10 40"
                        + getResources()
                        .getString(R.string.print_invoice_print)
                        + "\r\n";
                Printitem += "T 5 0 10 60  "; /*
                                             * "print distributor name and distributor address"
											 */

                Printitem += "\r\n" + "T 5 0 10 80  \r\n";
                Printitem += "T 5 0 10 120 "
                        + bmodel.userMasterHelper.getUserMasterBO()
                        .getDistributorName() + "\r\n";

                Printitem += "\r\n" + "T 5 0 10 130  \r\n";
                Printitem += "T 5 0 10 150 "
                        + bmodel.userMasterHelper.getUserMasterBO()
                        .getDistributorTinNumber() + "\r\n";

                Printitem += "\r\n" + "T 5 0 10 160  \r\n";
                Printitem += "T 5 0 10 180 " + mDate + "\r\n";

                Printitem += "LEFT \r\n";
                BeatMasterBO b = getTodayBeat();
                Printitem += "T 5 0 140 200 ";
                Printitem += SDUtil.today() + "\r\n";
                // Printitem += "T 5 0 140 250 ";
                // Printitem += mRetailerCode;
                // Printitem += "\r\n";
                Printitem += "T 5 0 140 250 ";
                if (mOutletName.length() < 23) {
                    Printitem += mOutletName + "\r\n";
                } else {
                    Printitem += mOutletName.substring(0, 23) + "\r\n";
                }

                Printitem += "\r\n";
                Printitem += "CENTER\r\n";
                Printitem += "T 5 0 20 280 " + mInvoiceNumber + "\r\n";
                Printitem += "LEFT \r\n";
                Printitem += "T 5 0 10 300 --------------------------------------------------\r\n";
                Printitem += "T 5 0 140 320"
                        + getResources().getString(R.string.product_name_print)
                        + "\r\n";
                Printitem += "T 5 0 460 320"
                        + getResources().getString(R.string.qty_print) + "\r\n";
                Printitem += "T 5 0 570 320"
                        + getResources().getString(R.string.price_print)
                        + "\r\n";
                Printitem += "T 5 0 670 320"
                        + getResources().getString(R.string.total_print)
                        + "\r\n";
                Printitem += "T 5 0 10 340 --------------------------------------------------\r\n";
                x += 10;

                for (ProductMasterBO productBO : mProductsForAdapter) {
                    int qty = productBO.getOrderedPcsQty()
                            + productBO.getCaseSize()
                            * productBO.getOrderedCaseQty();
                    String printqty = productBO.getOrderedCaseQty() + "/"
                            + productBO.getOrderedPcsQty();

                    double total = qty * productBO.getSrp();

                    x += 40;
                    if (productBO.getProductName().length() > 14) {
                        Printitem += "T 5 0 140 "
                                + x
                                + " "
                                + productBO.getProductName().substring(0, 14)
                                .toLowerCase() + "\r\n";
                    } else {
                        Printitem += "T 5 0 140 " + x + " "
                                + productBO.getProductName().toLowerCase()
                                + "\r\n";
                    }
                    Printitem += "T 5 0 460 " + x + " " + printqty + "\r\n";
                    rightAlign(bmodel.formatValue(productBO.getSrp()));
                    y = 510 + space;
                    Printitem += "T 5 0 " + y + " " + x + " "
                            + bmodel.formatValue(productBO.getSrp()) + "\r\n";
                    rightAlign(bmodel.formatValue(total));
                    y = 630 + space;
                    Printitem += "T 5 0 " + y + " " + x + " "
                            + bmodel.formatValue(total) + "\r\n";
                }

                x += 40;
                Printitem += "T 5 0 40 "
                        + x
                        + " --------------------------------------------------\r\n";
                x += 30;
                Printitem += "T 5 0 140 " + x
                        + getResources().getString(R.string.total_print)
                        + "\r\n";
                rightAlign(bmodel.formatValue(mTotalValue));
                y = 630 + space;
                Printitem += "T 5 0 " + y + " " + x + " "
                        + bmodel.formatValue(mTotalValue) + "\r\n";

                double ttc = (mTotalValue * 0.4) / 100;
                double tva = (mTotalValue * 20) / 100;
                double ht = mTotalValue - (ttc + tva);

                x += 40;
                Printitem += "T 5 0 140 " + x
                        + getResources().getString(R.string.print_footer1)
                        + "\r\n";
                rightAlign(bmodel.formatValue(ttc));
                y = 530 + space;
                Printitem += "T 5 0 " + y + " " + x + " "
                        + bmodel.formatValue(ttc) + "\r\n";

                x += 30;
                Printitem += "T 5 0 140 " + x
                        + getResources().getString(R.string.print_footer2)
                        + "\r\n";
                rightAlign(bmodel.formatValue(ht));
                y = 530 + space;
                Printitem += "T 5 0 " + y + " " + x + " "
                        + bmodel.formatValue(ht) + "\r\n";
                Commons.print("HT" + String.valueOf(ht).length());
                x += 30;
                Printitem += "T 5 0 140 " + x
                        + getResources().getString(R.string.print_footer3)
                        + "\r\n";
                rightAlign(bmodel.formatValue(tva));
                y = 530 + space;
                Printitem += "T 5 0 " + y + " " + x + " "
                        + bmodel.formatValue(tva) + "\r\n";

                x += 60;
                Printitem += "T 5 0 140 " + x
                        + getResources().getString(R.string.print_footer4)
                        + "\r\n";
                rightAlign(bmodel.formatValue(mTotalValue));
                y = 530 + space;

                Printitem += "T 5 0 " + y + " " + x + " "
                        + bmodel.formatValue(mTotalValue) + "\r\n";
                Printitem += "T 5 0 680 " + x + " MAD\r\n";

                // x += 60;
                // Printitem += "T 5 0 560 " + x
                // + getResources().getString(R.string.so_signature)
                // + "\r\n";
                x += 60;
                Printitem += "T 5 0 480 "
                        + x
                        + " "
                        + bmodel.userMasterHelper.getUserMasterBO()
                        .getUserName() + "\r\n";

                Printitem += "\r\n";
                Printitem += "\r\n";
                Printitem += "\r\n";
                Printitem += "PRINT\r\n";

                PrintDataBytes = Printitem.getBytes();
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
        return PrintDataBytes;

    }

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

    public void rightAlign(String str) {
        if (str.length() < 12)
            space = (12 - str.length()) * 10 + 15;
        Commons.print("space" + " " + space);
    }
}
