package com.ivy.sd.png.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.intermecprint.BtPrint4Ivy;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.CollectionBO;
import com.ivy.sd.png.bo.OrderHeader;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.SchemeBO;
import com.ivy.sd.png.bo.SchemeProductBO;
import com.ivy.sd.png.bo.TaxBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.MyThread;
import com.ivy.sd.png.model.ScreenReceiver;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.SalesReturnHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.DateUtil;
import com.ivy.sd.png.util.MyDatePickerDialog;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.sd.print.BtService;
import com.ivy.sd.print.CommonPrintPreviewActivity;
import com.ivy.sd.print.DemoSleeper;
import com.ivy.sd.print.GhanaPrintPreviewActivity;
import com.ivy.sd.print.PrintPreviewScreen;
import com.ivy.sd.print.PrintPreviewScreenDiageo;
import com.ivy.sd.print.PrintPreviewScreenTitan;
import com.ivy.sd.print.SettingsHelper;
import com.tremol.zfplibj.ZFPLib;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

public class OrderSummary extends IvyBaseActivityNoActionBar implements OnClickListener, StorewiseDiscountDialogFragment.OnMyDialogResult, DataPickerDialogFragment.UpdateDateInterface {

    /**
     * views *
     */
    private Button btnsave;
    private Button btnsaveAndGoInvoice;
    private TextView lpc;
    private TextView totalval;
    private TextView totalQtyTV;
    private Button delievery_date;
    private DiscountDialog initiativedialog;
    private ExpandableListView mExpListView;
    private AlertDialog.Builder build;
    private AlertDialog alertDialog;
    private ImageView icAmountSpilitup;
    AmountSplitupDialog dialogFragment;
    /**
     * Objects *
     */
    private boolean fromorder = false, isExpanded = false;
    private BusinessModel bmodel;
    private LinkedList<ProductMasterBO> mOrderedProductList;
    private Vector<ProductMasterBO> shortListOrder;

    private boolean hidealert = false;
    private double enteredDiscAmtOrPercent;

    private String nextDate;
    private double totalOrderValue, cmyDiscount, distDiscount;
    private boolean isClick = false;
    private boolean isDiscountDialog;
    private static final int DATE_DIALOG_ID = 0;

    private static final String discountresult = "0";
    private boolean isClicked;
    private ReturnProductDialog returnProductDialog;
    private String screenCode = "MENU_STK_ORD";
    private CollectionBO collectionbo;
    private CollectionBeforeInvoiceDialog collectionBeforeInvoiceDialog;

    private static final String TAG = "OrderSummary";
    private static final boolean D = true;
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    private static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    private double vatAmount = 0.0;

    // messages
    private SharedPreferences msettings;

    private BluetoothAdapter mBluetoothAdapter = null;
    private BtService mChatService = null;

    private String PHOTO_PATH = "";

    private static final String ZEBRA_3INCH = "3";
    private Connection zebraPrinterConnection;
    private ZebraPrinter printer;
    private AlertDialog.Builder builder10;
    private double productEntryLevelDis = 0.0;

    private int mSelectedPrintCount = 0;
    private BroadcastReceiver mReceiver;

    private int focusProductCount = 0;
    private int totalFocusProductCount = 0;
    private StorewiseDiscountDialogFragment mStoreWiseDiscountDialogFragment;

    private Toolbar toolbar;

    public static String mActivityCode;

    public boolean isDiscountDialog() {
        return isDiscountDialog;
    }

    public void setDiscountDialog(boolean discountDialog) {
        isDiscountDialog = discountDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ordersummary);
        PHOTO_PATH = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + DataMembers.photoFolderName;

        /** Get the application context **/
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        Bundle extras = getIntent().getExtras();
        setDiscountDialog(false);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (savedInstanceState == null) {
            if (extras != null && extras.getString("ScreenCode") != null) {
                screenCode = extras.getString("ScreenCode");
            }
        }
        /** Close the screen if userid becomes 0 **/
        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }
        /** Close the screen if userid becomes 0 **/
        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        if (bmodel.mSelectedModule == 1 || bmodel.mSelectedModule == 2
                || bmodel.mSelectedModule == 3) {
            bmodel.configurationMasterHelper
                    .downloadNewActivityMenu(ConfigurationMasterHelper.MENU_ACTIVITY);
        }

        /** Get Menu name **/
        String screentitle = bmodel.configurationMasterHelper
                .getHomescreentwomenutitle("MENU_CLOSING");

        /** If menu not available, set it as summary **/
        if ("MENU_CLOSING".equals(screentitle))
            screentitle = getResources().getString(R.string.summary);
        bmodel.saveModuleCompletion("MENU_CLOSING");


        /** Initilize the toolbar and set title to it **/
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(screentitle);
            // Used to on / off the back arrow icon
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            // Used to remove the app logo actionbar icon and set title as home
            // (title support click)
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        // Used to hide the app logo icon from actionbar

        // Initialize and show focus product alert
        if (bmodel.configurationMasterHelper.SHOW_ORDER_FOCUS_COUNT) {
            this.loadFocusProduct();
            String msg;

            if (totalFocusProductCount == 0) {
                msg = getResources().getString(R.string.no_focus_show);
            } else {
                msg = getResources().getString(R.string.focus_show_1)
                        + (totalFocusProductCount - focusProductCount)
                        + getResources().getString(R.string.focus_show_2)
                        + totalFocusProductCount
                        + getResources().getString(R.string.focus_show_3);
            }

            bmodel.showAlert(msg, 0);
        }

        /** Initialize the views **/
        lpc = (TextView) findViewById(R.id.lcp);
        totalval = (TextView) findViewById(R.id.totalValue);
        delievery_date = (Button) findViewById(R.id.deliveryDate);
        btnsave = (Button) findViewById(R.id.orderSummarySave);
        mExpListView = (ExpandableListView) findViewById(R.id.elv);
        btnsaveAndGoInvoice = (Button) findViewById(R.id.saveAndGoInvoice);
        totalQtyTV = (TextView) findViewById(R.id.tv_totalqty);


        icAmountSpilitup = (ImageView) findViewById(R.id.icAmountSpilitup);

        getNextDate();
        msettings = getSharedPreferences(BusinessModel.PREFS_NAME, MODE_PRIVATE);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter);

        //typefaces
        ((TextView) findViewById(R.id.tv_deliveryDate)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        ((TextView) findViewById(R.id.lpcLabel)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        ((TextView) findViewById(R.id.totalValuelbl)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        ((TextView) findViewById(R.id.title_totalqty)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        delievery_date.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        lpc.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        totalval.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        totalQtyTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        btnsave.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        btnsaveAndGoInvoice.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        if (bmodel.isEdit()) {

            if (bmodel.mSelectedModule == 3) {
                delievery_date.setText(DateUtil.convertFromServerDateToRequestedFormat(
                        bmodel.getDeliveryDate(bmodel.getOrderid()),
                        ConfigurationMasterHelper.outDateFormat));
            } else
                delievery_date.setText(DateUtil.convertFromServerDateToRequestedFormat(
                        bmodel.getDeliveryDate(bmodel.getRetailerMasterBO()
                                .getRetailerID()),
                        ConfigurationMasterHelper.outDateFormat));
        } else {
            delievery_date.setText(nextDate);

        }

        delievery_date.setOnClickListener(this);

        btnsave.setOnClickListener(this);
        btnsaveAndGoInvoice.setOnClickListener(this);

        if (bmodel.getOrderHeaderBO() == null) {
            bmodel.setOrderHeaderBO(new OrderHeader());
        }

        hideAndSeek();

        // update empty bottle returns
        if (!bmodel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG
                || bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {

            if (!bmodel.isEdit() || !bmodel.isDoubleEdit_temp()) {
                bmodel.schemeDetailsMasterHelper
                        .updataFreeproductBottleReturn();
            }
            // update empty bottle return groupwise
            if (bmodel.configurationMasterHelper.SHOW_GROUPPRODUCTRETURN) {
                bmodel.productHelper.setGroupWiseReturnQty();
                bmodel.productHelper.calculateOrderReturnTypeWiseValue();
            }
        }
        // IF Collection Before Invoice Module Enable
        if (bmodel.configurationMasterHelper.SHOW_COLLECTION_BEFORE_INVOICE) {
            collectionbo = new CollectionBO();
            bmodel.collectionHelper.getPaymentList().clear();
        }


        try {
            if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                    R.id.totalValuelbl).getTag()) != null)
                ((TextView) findViewById(R.id.totalValuelbl))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(findViewById(
                                        R.id.totalValuelbl)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException(" " + e);
        }


        try {
            if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                    R.id.orderSummarySave).getTag()) != null)
                ((TextView) findViewById(R.id.orderSummarySave))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(findViewById(
                                        R.id.orderSummarySave)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException(" " + e);
        }
        try {
            if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                    R.id.saveAndGoInvoice).getTag()) != null)
                ((TextView) findViewById(R.id.saveAndGoInvoice))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(findViewById(
                                        R.id.saveAndGoInvoice)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException(" " + e);
        }
        try {
            if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                    R.id.lbl_comy_disc).getTag()) != null)
                ((TextView) findViewById(R.id.lbl_comy_disc))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(findViewById(
                                        R.id.lbl_comy_disc)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException(" " + e);
        }
        try {
            if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                    R.id.lbl_dist_disc).getTag()) != null)
                ((TextView) findViewById(R.id.lbl_dist_disc))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(findViewById(
                                        R.id.lbl_dist_disc)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException(" " + e);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        returnProductDialog = null;
        hidealert = false;
        isClicked = false;

        /** session out if userid becomes 0 **/
        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        updateExpandableListView();
    }

    /**
     * This method will on/off the items based in the configuration.
     */
    private void hideAndSeek() {
        try {
            /** if pre-sales disable the following two component **/
            if (bmodel.configurationMasterHelper.IS_INVOICE_AS_MOD)
                btnsaveAndGoInvoice.setVisibility(View.GONE);
            if (bmodel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG && !bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
                btnsaveAndGoInvoice.setVisibility(View.GONE);
                // depends on seller
                // type dialog
                // selection
            }
            if (!bmodel.configurationMasterHelper.IS_INVOICE)
                btnsaveAndGoInvoice.setVisibility(View.GONE);
            if (bmodel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG) { // if
                // seller
                // dialog
                // enable
                if (bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
                    findViewById(R.id.layoutDelivery).setVisibility(View.GONE);
                }
            } else if (bmodel.configurationMasterHelper.IS_INVOICE || !bmodel.configurationMasterHelper.SHOW_DELIVERY_DATE) {
                findViewById(R.id.layoutDelivery).setVisibility(View.GONE);
            }

            if (!bmodel.configurationMasterHelper.SHOW_TOTAL_DISCOUNT_EDITTEXT)
                //findViewById(R.id.discountlayout).setVisibility(View.GONE);

                if (!bmodel.configurationMasterHelper.SHOW_LPC_ORDER) {
                    findViewById(R.id.ll_lines).setVisibility(View.GONE);
                }

            try {
                if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                        R.id.totaltitle).getTag()) != null)
                    ((TextView) findViewById(R.id.totaltitle))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(findViewById(R.id.totaltitle)
                                            .getTag()));
            } catch (Exception e) {
                Commons.printException(" " + e);
            }


            // On/Off order case and pcs

            if (bmodel.configurationMasterHelper.SHOW_TOTAL_QTY_ORDER) {
                findViewById(R.id.ll_totqty).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.ll_totqty).setVisibility(View.GONE);
            }

            if (bmodel.configurationMasterHelper.SHOW_TOTAL_VALUE_ORDER) {
                findViewById(R.id.ll_values).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.ll_values).setVisibility(View.GONE);
            }

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    private void getNextDate() {
        try {
            Calendar origDay = Calendar.getInstance();
            origDay.add(Calendar.DAY_OF_YEAR, (bmodel.configurationMasterHelper.LOAD_MAX_DELIVERY_DATE == 0 ? 1 : bmodel.configurationMasterHelper.LOAD_MAX_DELIVERY_DATE));
            nextDate = DateUtil.convertDateObjectToRequestedFormat(origDay.getTime(),
                    ConfigurationMasterHelper.outDateFormat);
        } catch (Exception e) {
            Commons.printException(" " + e);// TODO: handle exception
        }
    }

    private void updateExpandableListView() {
        int totalAllQty = 0;

        bmodel.getRetailerMasterBO().setBillWiseCompanyDiscount(0);
        bmodel.getRetailerMasterBO().setBillWiseDistributorDiscount(0);

        Vector<ProductMasterBO> productList = bmodel.productHelper
                .getProductMaster();

        if (productList == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.no_products_exists), 0);
            return;
        }

        int productsCount = productList.size();
        mOrderedProductList = new LinkedList<>();
        totalOrderValue = 0;

        ProductMasterBO productBO;
        float totalWeight = 0;

        for (int i = 0; i < productsCount; i++) {
            productBO = productList.elementAt(i);
            if (productBO.getOrderedCaseQty() > 0
                    || productBO.getOrderedPcsQty() > 0
                    || productBO.getOrderedOuterQty() > 0) {
                int totalQty = productBO.getOrderedPcsQty() + productBO.getOrderedCaseQty() * productBO.getCaseSize() + productBO.getOrderedOuterQty() * productBO.getOutersize();
                totalAllQty = totalAllQty + totalQty;
                totalWeight = totalWeight + (totalQty * productBO.getWeight());
                mOrderedProductList.add(productBO);

                double line_total_price;

                if (bmodel.configurationMasterHelper.IS_SIH_VALIDATION
                        && bmodel.configurationMasterHelper.IS_INVOICE
                        && bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION) {
                    if (productBO.getBatchwiseProductCount() > 0) {
                        // Apply batch wise price apply
                        line_total_price = bmodel.schemeDetailsMasterHelper
                                .getbatchWiseTotalValue(productBO);
                    } else {
                        line_total_price = (productBO.getOrderedCaseQty() * productBO
                                .getCsrp())
                                + (productBO.getOrderedPcsQty() * productBO
                                .getSrp())
                                + (productBO.getOrderedOuterQty() * productBO
                                .getOsrp());
                    }
                } else {
                    line_total_price = (productBO.getOrderedCaseQty() * productBO
                            .getCsrp())
                            + (productBO.getOrderedPcsQty() * productBO
                            .getSrp())
                            + (productBO.getOrderedOuterQty() * productBO
                            .getOsrp());
                }

                if (bmodel.configurationMasterHelper.IS_APPLY_PRODUCT_TAX) {
                    if (productBO.getTaxes() != null && productBO.getTaxes().size() > 0) {
                        for (TaxBO taxBO : productBO.getTaxes()) {
                            vatAmount = vatAmount + line_total_price
                                    * taxBO.getTaxRate() / 100;
                        }
                    }

                    line_total_price = line_total_price + vatAmount;
                }

                /** Set the calculated values in productBO **/
                productBO.setDiscount_order_value(line_total_price);
                productBO.setSchemeAppliedValue(line_total_price);

                productBO.setOrderPricePiece(productBO.getSrp());
                productBO.setCompanyTypeDiscount(0);
                productBO.setDistributorTypeDiscount(0);
                Commons.print("line value" + line_total_price);
                totalOrderValue += line_total_price;

                // clear scheme free products stored in product obj
                productBO.setSchemeProducts(new ArrayList<SchemeProductBO>());
            }
        } // End of products loop
        if (bmodel.configurationMasterHelper.IS_SHOW_IRDERING_SEQUENCE) {
            shortListOrder = new Vector<>();
            updateOrderListByEntry();
            shortListOrder.addAll(mOrderedProductList);
        }
        String mTotalAllQty = totalAllQty + "";
        totalQtyTV.setText(mTotalAllQty);

        if (bmodel.getOrderHeaderBO() != null)
            bmodel.getOrderHeaderBO().setTotalWeight(totalWeight);

        // Added by GP
        // Empties Management is Enabled, then we have to calculate the
        // TotalOrderValue(TotalValue - returnable prodcut value
        // Add the Returned products from Total Value
        if (bmodel.configurationMasterHelper.SHOW_PRODUCTRETURN
                && !bmodel.configurationMasterHelper.SHOW_BOTTLE_CREDITLIMIT
                && bmodel.configurationMasterHelper.IS_SIH_VALIDATION && bmodel.getOrderHeaderBO() != null) {
            totalOrderValue = totalOrderValue
                    + bmodel.getOrderHeaderBO().getRemainigValue();
        }

        bmodel.productHelper.clearProductDiscAndTaxValue(mOrderedProductList);
        // Apply scheme
        if (!bmodel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG
                || bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {

            updateSchemeDetails();
        }

        if(bmodel.configurationMasterHelper.IS_REMOVE_TAX_ON_SRP) {
            //applying removed tax..
            updateTaxOnProduct();
        }

        //  Apply product entry level discount
        if (bmodel.configurationMasterHelper.SHOW_DISCOUNT_DIALOG) {
            productEntryLevelDis = bmodel.productHelper.updateProductDiscountUsingEntry(mOrderedProductList);
            totalOrderValue = totalOrderValue - productEntryLevelDis;
        }

        // Apply Item  level discount
        if (bmodel.configurationMasterHelper.SHOW_DISCOUNT) {
            double productLevelDiscount = bmodel.productHelper.updateItemLevelDiscount();
            totalOrderValue = totalOrderValue - productLevelDiscount;
        }

        if (bmodel.configurationMasterHelper.SHOW_TAX) {
            // Apply Exclude Item level Tax  in Product
            bmodel.productHelper.updateProductWiseTax();
        }

        totalval.setText(bmodel.formatValue(totalOrderValue));


        mExpListView.setAdapter(new ProductExpandableAdapter());
        int orderedProductCount = mOrderedProductList.size();
        String strOrderedPrdtCount = orderedProductCount + "";
        lpc.setText(strOrderedPrdtCount);
        for (int i = 0; i < orderedProductCount; i++) {
            mExpListView.expandGroup(i);
        }


        if (bmodel.configurationMasterHelper.SHOW_STORE_WISE_DISCOUNT_DLG && bmodel.configurationMasterHelper.BILL_WISE_DISCOUNT == 0) {
            //find the  range of discount by using totalvalue
            final double billwiseRangeDiscount = bmodel.productHelper.updateBillwiseRangeDiscount(totalOrderValue);
            totalOrderValue = totalOrderValue - billwiseRangeDiscount;
            totalval.setText(bmodel.formatValue(totalOrderValue));
        } else if (bmodel.configurationMasterHelper.SHOW_STORE_WISE_DISCOUNT_DLG && bmodel.configurationMasterHelper.BILL_WISE_DISCOUNT == 1) {
            // Automatically apply bill wise discount
            final double billWiseDiscount = bmodel.productHelper.updateBillwiseDiscount(totalOrderValue);
            if (bmodel.getOrderHeaderBO() != null) {
                bmodel.getOrderHeaderBO().setDiscountValue(billWiseDiscount);
            }
            totalOrderValue = totalOrderValue - billWiseDiscount;
            totalval.setText(bmodel.formatValue(totalOrderValue));
        } else {
            // user manually enter bill wise discount
            double discnt = bmodel.orderAndInvoiceHelper.restoreDiscountAmount(bmodel
                    .getRetailerMasterBO().getRetailerID());

            String strAppliedDiscount = bmodel
                    .formatValue(getDiscountAppliedValue(discnt)) + "";
            totalval.setText(strAppliedDiscount);
        }

        // Apply bill wise payterm discount
        final double billwisePaytermDisc = bmodel.productHelper.updateBillwisePaytermDiscount(totalOrderValue);
        totalOrderValue = totalOrderValue - billwisePaytermDisc;
        totalval.setText(bmodel.formatValue(totalOrderValue));

        if (!isDiscountDialog() && bmodel.configurationMasterHelper.SHOW_DISCOUNT_DIALOG && initiativedialog != null && initiativedialog.isShowing()) {
            setDiscountDialog(true);
            initiativedialog.dismiss();
            initiativedialog = null;
            initiativedialog = new DiscountDialog(OrderSummary.this, null,
                    discountDismissListener);
            initiativedialog.show();
        }

        if (bmodel.configurationMasterHelper.IS_SHOW_DISCOUNTS_ORDER_SUMMARY) {
            icAmountSpilitup.setVisibility(View.VISIBLE);
            double cmy_disc = 0, dist_disc = 0;
            for (ProductMasterBO productMasterBO : mOrderedProductList) {
                cmy_disc = cmy_disc + productMasterBO.getCompanyTypeDiscount();
                dist_disc = dist_disc + productMasterBO.getDistributorTypeDiscount();

            }

            cmyDiscount = cmy_disc + bmodel.getRetailerMasterBO().getBillWiseCompanyDiscount();
            distDiscount = dist_disc + bmodel.getRetailerMasterBO().getBillWiseDistributorDiscount();

            icAmountSpilitup.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (dialogFragment == null) {
                        dialogFragment = new AmountSplitupDialog();
                        dialogFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {

                                dialogFragment = null;
                            }
                        });
                        Bundle args = new Bundle();
                        args.putDouble("totalOrderValue", totalOrderValue);
                        args.putDouble("cmy_disc", cmyDiscount);
                        args.putDouble("dist_disc", distDiscount);
                        dialogFragment.setArguments(args);
                        dialogFragment.show(getSupportFragmentManager(), "AmtSplitupDialog");
                    }
                }
            });

        } else {
            icAmountSpilitup.setVisibility(View.GONE);
        }
    }

    private void updateTaxOnProduct(){
        for(ProductMasterBO bo:mOrderedProductList){
            float finalAmount=0;

            if(bmodel.productHelper.getmTaxListByProductId().get(bo.getProductID())!=null) {
                for (TaxBO taxBO : bmodel.productHelper.getmTaxListByProductId().get(bo.getProductID())) {
                    if (taxBO.getParentType().equals("0")) {
                        finalAmount += SDUtil.truncateDecimal(bo.getDiscount_order_value() * (taxBO.getTaxRate() / 100), 2).floatValue();
                    }
                }
            }

            bo.setDiscount_order_value((bo.getDiscount_order_value()+finalAmount));
        }
    }
    @Override
    public void onDiscountDismiss(String result, int result1, int result2, int result3) {
        if (bmodel.configurationMasterHelper.SHOW_STORE_WISE_DISCOUNT_DLG && bmodel.configurationMasterHelper.BILL_WISE_DISCOUNT == 0) {
            final double totalValue = bmodel.productHelper.updateBillwiseRangeDiscount(totalOrderValue);
            totalval.setText(bmodel.formatValue(totalValue));
        } else if (bmodel.configurationMasterHelper.SHOW_TOTAL_DISCOUNT_EDITTEXT) {
            try {
                int f1 = 0;
                String qty = result.toString();

                if ("".equals(qty)) { // && numPressed
                    qty = "0";
                }

                enteredDiscAmtOrPercent = SDUtil.convertToDouble(qty);
                if (enteredDiscAmtOrPercent != 0 && bmodel.configurationMasterHelper.discountType == 1 && enteredDiscAmtOrPercent > 100) {
                    f1 = (int) (enteredDiscAmtOrPercent / 10);
                }

                String strDiscountAppliedvalue = bmodel
                        .formatValue(getDiscountAppliedValue(SDUtil
                                .convertToDouble(f1 + ""))) + "";

                totalval.setText(strDiscountAppliedvalue);

            } catch (Exception e) {
                Commons.printException(e);
            }
        }
    }

    @Override
    public void cancel() {

    }

    @Override
    public void updateDate(Date date) {

        AdvancePaymentDialogFragment paymentDialogFragment = (AdvancePaymentDialogFragment) getSupportFragmentManager().findFragmentByTag("Advance Payment");
        paymentDialogFragment.updateDate(date);

    }

    private class ProductExpandableAdapter extends BaseExpandableListAdapter {

        @Override
        public Object getChild(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater
                        .inflate(R.layout.row_ordersummary, parent, false);
                holder = new ViewHolder();
                holder.tvwspname = (TextView) row
                        .findViewById(R.id.PRODUCTNAME);
                holder.pcsQty = (TextView) row.findViewById(R.id.P_QUANTITY);
                holder.caseqty = (TextView) row.findViewById(R.id.C_QUANTITY);
                holder.tw_srp = (TextView) row.findViewById(R.id.MRP);
                holder.tvwtot = (TextView) row.findViewById(R.id.TOTAL);
                holder.outerQty = (TextView) row.findViewById(R.id.OC_QUANTITY);
                holder.weight = (TextView) row.findViewById(R.id.tv_weight);

                holder.scqty = (TextView) row.findViewById(R.id.sc_quantity);
                holder.shoqty = (TextView) row.findViewById(R.id.sho_quantity);
                holder.spqty = (TextView) row.findViewById(R.id.sp_quantity);

                holder.tvwspname.setTypeface(bmodel.configurationMasterHelper.getProductNameFont());
                holder.tvwtot.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));

                ((View) row.findViewById(R.id.view_dotted_line)).setLayerType(View.LAYER_TYPE_SOFTWARE, null);


                if (!"MENU_ORDER".equals(screenCode) && bmodel.configurationMasterHelper.SHOW_STOCK_IN_SUMMARY) {

                    if (bmodel.configurationMasterHelper.SHOW_STOCK_SC) {
                        ((LinearLayout) row.findViewById(R.id.llShelfCase)).setVisibility(View.VISIBLE);
                        ((TextView) row.findViewById(R.id.shelfCaseTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
                        holder.scqty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                        try {
                            if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                    R.id.shelfCaseTitle).getTag()) != null)
                                ((TextView) row.findViewById(R.id.shelfCaseTitle))
                                        .setText(bmodel.labelsMasterHelper
                                                .applyLabels(row.findViewById(
                                                        R.id.shelfCaseTitle)
                                                        .getTag()));
                        } catch (Exception e) {
                            Commons.printException(e);
                        }

                    }
                    if (bmodel.configurationMasterHelper.SHOW_SHELF_OUTER) {
                        ((LinearLayout) row.findViewById(R.id.llShelfPiece)).setVisibility(View.VISIBLE);
                        ((TextView) row.findViewById(R.id.shelfOuterTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
                        holder.shoqty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                        try {
                            if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                    R.id.shelfOuterTitle).getTag()) != null)
                                ((TextView) row.findViewById(R.id.shelfOuterTitle))
                                        .setText(bmodel.labelsMasterHelper
                                                .applyLabels(row.findViewById(
                                                        R.id.shelfOuterTitle)
                                                        .getTag()));
                        } catch (Exception e) {
                            Commons.printException(e);
                        }

                    }

                    if (bmodel.configurationMasterHelper.SHOW_STOCK_SP) {
                        ((LinearLayout) row.findViewById(R.id.llShelfPiece)).setVisibility(View.VISIBLE);
                        ((TextView) row.findViewById(R.id.shelfPcsTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
                        holder.spqty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                        try {
                            if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                    R.id.shelfPcsTitle).getTag()) != null)
                                ((TextView) row.findViewById(R.id.shelfPcsTitle))
                                        .setText(bmodel.labelsMasterHelper
                                                .applyLabels(row.findViewById(
                                                        R.id.shelfPcsTitle)
                                                        .getTag()));
                        } catch (Exception e) {
                            Commons.printException(e);
                        }
                    }
                }
                // On/Off order case and pce
                if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE) {
                    ((LinearLayout) row.findViewById(R.id.llCase)).setVisibility(View.GONE);
                } else {
                    ((TextView) row.findViewById(R.id.caseTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
                    holder.caseqty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                    try {
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.caseTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.caseTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.caseTitle).getTag()));
                    } catch (Exception e) {
                        Commons.printException(" " + e);
                    }
                }


                if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
                    ((LinearLayout) row.findViewById(R.id.llPiece)).setVisibility(View.GONE);
                } else {
                    ((TextView) row.findViewById(R.id.pcsTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
                    holder.pcsQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                    try {
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.pcsTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.pcsTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.pcsTitle).getTag()));
                    } catch (Exception e) {
                        Commons.printException(" " + e);
                    }
                }
                if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE) {
                    ((LinearLayout) row.findViewById(R.id.llOuter)).setVisibility(View.GONE);
                } else {
                    ((TextView) row.findViewById(R.id.outercaseTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
                    holder.outerQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                    try {
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.outercaseTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.outercaseTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.outercaseTitle).getTag()));
                    } catch (Exception e) {
                        Commons.printException(" " + e);
                    }
                }
                if (!bmodel.configurationMasterHelper.SHOW_ORDER_WEIGHT) {
                    ((LinearLayout) row.findViewById(R.id.llShelfWeight)).setVisibility(View.GONE);
                } else {
                    ((TextView) row.findViewById(R.id.weighttitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
                    holder.caseqty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                    try {
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.weighttitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.weighttitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.weighttitle).getTag()));
                    } catch (Exception e) {
                        Commons.printException(" " + e);
                    }
                }
                if (!bmodel.configurationMasterHelper.SHOW_STK_ORD_SRP) {
                    ((LinearLayout) row.findViewById(R.id.llSrp)).setVisibility(View.GONE);
                } else {
                    ((TextView) row.findViewById(R.id.srpTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
                    holder.tw_srp.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                    try {
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.srpTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.srpTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.srpTitle).getTag()));
                    } catch (Exception e) {
                        Commons.printException(" " + e);
                    }
                }

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            SchemeProductBO productBO = mOrderedProductList.get(groupPosition)
                    .getSchemeProducts().get(childPosition);

            holder.tvwspname.setText(productBO.getProductName());
            holder.productName = productBO.getProductFullName();
            holder.productBO = bmodel.productHelper
                    .getProductMasterBOById(productBO.getProductId());

            holder.scqty.setText(holder.productBO.getLocations().get(bmodel.productHelper.getmSelectedLocationIndex()).getShelfCase() + "");
            holder.shoqty.setText(holder.productBO.getLocations().get(bmodel.productHelper.getmSelectedLocationIndex()).getShelfOuter() + "");
            holder.spqty.setText(holder.productBO.getLocations().get(bmodel.productHelper.getmSelectedLocationIndex()).getShelfPiece() + "");

            if (holder.productBO != null) {
                if (holder.productBO.getCaseUomId() == productBO.getUomID()
                        && holder.productBO.getCaseUomId() != 0) {
                    // case wise free quantity update

                    holder.caseqty
                            .setText(productBO.getQuantitySelected() + "");
                    holder.pcsQty.setText(0 + "");
                    holder.outerQty.setText(0 + "");
                } else if (holder.productBO.getOuUomid() == productBO
                        .getUomID() && holder.productBO.getOuUomid() != 0) {
                    // outer wise free quantity update
                    holder.outerQty.setText(productBO.getQuantitySelected() + "");
                    holder.pcsQty.setText(0 + "");
                    holder.caseqty.setText(0 + "");

                } else {
                    holder.pcsQty.setText(productBO.getQuantitySelected() + "");
                    holder.caseqty.setText(0 + "");
                    holder.outerQty.setText(0 + "");
                }
            }

            holder.tw_srp.setText(SDUtil.roundIt(0, 2) + "");

            if (!bmodel.configurationMasterHelper.SHOW_STK_ORD_SRP) {
                holder.tvwtot.setVisibility(View.GONE);
            } else {
                holder.tvwtot.setText("0");
            }


            return row;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            if (mOrderedProductList.get(groupPosition).getIsscheme() == 1 && mOrderedProductList.get(groupPosition).getSchemeProducts() != null) {
                return mOrderedProductList.get(groupPosition)
                        .getSchemeProducts().size();
            }
            return 0;
        }

        @Override
        public Object getGroup(int groupPosition) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int getGroupCount() {
            if (mOrderedProductList == null)
                return 0;

            return mOrderedProductList.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            final ViewHolder holder;

            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater
                        .inflate(R.layout.row_ordersummary, parent, false);
                holder = new ViewHolder();
                holder.tvwspname = (TextView) row
                        .findViewById(R.id.PRODUCTNAME);
                holder.pcsQty = (TextView) row.findViewById(R.id.P_QUANTITY);
                holder.caseqty = (TextView) row.findViewById(R.id.C_QUANTITY);
                holder.tw_srp = (TextView) row.findViewById(R.id.MRP);
                holder.tvwtot = (TextView) row.findViewById(R.id.TOTAL);
                holder.outerQty = (TextView) row.findViewById(R.id.OC_QUANTITY);
                holder.weight = (TextView) row.findViewById(R.id.tv_weight);

                holder.scqty = (TextView) row.findViewById(R.id.sc_quantity);
                holder.shoqty = (TextView) row.findViewById(R.id.sho_quantity);
                holder.spqty = (TextView) row.findViewById(R.id.sp_quantity);

                holder.tvwspname.setMaxLines(bmodel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);
                holder.tvwspname.setTypeface(bmodel.configurationMasterHelper.getProductNameFont());
                ((View) row.findViewById(R.id.view_dotted_line)).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                holder.tvwtot.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));

                if (!"MENU_ORDER".equals(screenCode) && bmodel.configurationMasterHelper.SHOW_STOCK_IN_SUMMARY) {


                    if (bmodel.configurationMasterHelper.SHOW_STOCK_SC) {
                        ((LinearLayout) row.findViewById(R.id.llShelfCase)).setVisibility(View.VISIBLE);
                        ((TextView) row.findViewById(R.id.shelfCaseTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                        holder.scqty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                        try {
                            if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                    R.id.shelfCaseTitle).getTag()) != null)
                                ((TextView) row.findViewById(R.id.shelfCaseTitle))
                                        .setText(bmodel.labelsMasterHelper
                                                .applyLabels(row.findViewById(
                                                        R.id.shelfCaseTitle)
                                                        .getTag()));
                        } catch (Exception e) {
                            Commons.printException(e);
                        }

                    }
                    if (bmodel.configurationMasterHelper.SHOW_SHELF_OUTER) {
                        ((LinearLayout) row.findViewById(R.id.llShelfPiece)).setVisibility(View.VISIBLE);
                        ((TextView) row.findViewById(R.id.shelfOuterTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                        holder.shoqty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                        try {
                            if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                    R.id.shelfOuterTitle).getTag()) != null)
                                ((TextView) row.findViewById(R.id.shelfOuterTitle))
                                        .setText(bmodel.labelsMasterHelper
                                                .applyLabels(row.findViewById(
                                                        R.id.shelfOuterTitle)
                                                        .getTag()));
                        } catch (Exception e) {
                            Commons.printException(e);
                        }

                    }

                    if (bmodel.configurationMasterHelper.SHOW_STOCK_SP) {
                        ((LinearLayout) row.findViewById(R.id.llShelfPiece)).setVisibility(View.VISIBLE);
                        ((TextView) row.findViewById(R.id.shelfPcsTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                        holder.spqty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                        try {
                            if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                    R.id.shelfPcsTitle).getTag()) != null)
                                ((TextView) row.findViewById(R.id.shelfPcsTitle))
                                        .setText(bmodel.labelsMasterHelper
                                                .applyLabels(row.findViewById(
                                                        R.id.shelfPcsTitle)
                                                        .getTag()));
                        } catch (Exception e) {
                            Commons.printException(e);
                        }
                    }
                }

                // On/Off order case and pce
                if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE) {
                    ((LinearLayout) row.findViewById(R.id.llCase)).setVisibility(View.GONE);
                } else {
                    ((TextView) row.findViewById(R.id.caseTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                    holder.caseqty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                    try {
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.caseTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.caseTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.caseTitle).getTag()));
                    } catch (Exception e) {
                        Commons.printException(" " + e);
                    }
                }


                if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
                    ((LinearLayout) row.findViewById(R.id.llPiece)).setVisibility(View.GONE);
                } else {
                    ((TextView) row.findViewById(R.id.pcsTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                    holder.pcsQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                    try {
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.pcsTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.pcsTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.pcsTitle).getTag()));
                    } catch (Exception e) {
                        Commons.printException(" " + e);
                    }
                }
                if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE) {
                    ((LinearLayout) row.findViewById(R.id.llOuter)).setVisibility(View.GONE);
                } else {
                    ((TextView) row.findViewById(R.id.outercaseTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                    holder.outerQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                    try {
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.outercaseTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.outercaseTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.outercaseTitle).getTag()));
                    } catch (Exception e) {
                        Commons.printException(" " + e);
                    }
                }
                if (!bmodel.configurationMasterHelper.SHOW_ORDER_WEIGHT) {
                    ((LinearLayout) row.findViewById(R.id.llShelfWeight)).setVisibility(View.GONE);
                } else {
                    ((TextView) row.findViewById(R.id.weighttitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                    holder.caseqty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                    try {
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.weighttitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.weighttitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.weighttitle).getTag()));
                    } catch (Exception e) {
                        Commons.printException(" " + e);
                    }
                }
                if (!bmodel.configurationMasterHelper.SHOW_STK_ORD_SRP) {
                    ((LinearLayout) row.findViewById(R.id.llSrp)).setVisibility(View.GONE);
                } else {
                    ((TextView) row.findViewById(R.id.srpTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                    holder.tw_srp.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                    try {
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.srpTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.srpTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.srpTitle).getTag()));
                    } catch (Exception e) {
                        Commons.printException(" " + e);
                    }
                }

                if (!bmodel.configurationMasterHelper.SHOW_STK_ORD_SRP) {
                    holder.tvwtot.setVisibility(View.GONE);
                }

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.productBO = mOrderedProductList.get(groupPosition);
            holder.tvwspname.setText(holder.productBO.getProductShortName());
            holder.productName = holder.productBO.getProductName();
            holder.pcsQty.setText(holder.productBO.getOrderedPcsQty() + "");
            holder.caseqty.setText(holder.productBO.getOrderedCaseQty() + "");
            holder.tw_srp.setText(bmodel.formatValue(holder.productBO.getSrp())
                    + "");
            holder.outerQty.setText(holder.productBO.getOrderedOuterQty() + "");

            holder.scqty.setText(((holder.productBO.getLocations().get(bmodel.productHelper.getmSelectedLocationIndex()).getShelfCase() == -1) ? 0 : holder.productBO.getLocations().get(bmodel.productHelper.getmSelectedLocationIndex()).getShelfCase()) + "");
            holder.shoqty.setText(((holder.productBO.getLocations().get(bmodel.productHelper.getmSelectedLocationIndex()).getShelfOuter() == -1) ? 0 : holder.productBO.getLocations().get(bmodel.productHelper.getmSelectedLocationIndex()).getShelfOuter()) + "");
            holder.spqty.setText(((holder.productBO.getLocations().get(bmodel.productHelper.getmSelectedLocationIndex()).getShelfPiece() == -1) ? 0 : holder.productBO.getLocations().get(bmodel.productHelper.getmSelectedLocationIndex()).getShelfPiece()) + "");

            /**
             * This line wise total may be wrong is amount discount appied via
             * scheme
             **/
            holder.tvwtot.setText(bmodel.formatValue(holder.productBO
                    .getDiscount_order_value()) + "");
            int weight = holder.productBO.getOrderedPcsQty() + (holder.productBO.getOrderedCaseQty() * holder.productBO.getCaseSize()) + (holder.productBO.getOrderedOuterQty() * holder.productBO.getOutersize());
            holder.weight.setText(weight * holder.productBO.getWeight() + "");

            return row;
        }

        @Override
        public boolean hasStableIds() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            // TODO Auto-generated method stub
            return false;
        }

    }

    class ViewHolder {
        private ProductMasterBO productBO;
        private String productName;// product id
        private TextView tvwspname;
        private TextView pcsQty;
        private TextView caseqty;
        private TextView outerQty;
        private TextView weight;
        private TextView scqty;
        private TextView shoqty;
        private TextView spqty;
        private TextView tw_srp;
        private TextView tvwtot;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        String msg = "";
        String delivery_date_txt = "";
        switch (id) {
            case DATE_DIALOG_ID:
                Calendar c = Calendar.getInstance();
                c.add(Calendar.DAY_OF_YEAR, 1);
                int cyear = c.get(Calendar.YEAR);
                int cmonth = c.get(Calendar.MONTH);
                int cday = c.get(Calendar.DAY_OF_MONTH);

                nextDate = DateUtil.convertDateObjectToRequestedFormat(c.getTime(),
                        ConfigurationMasterHelper.outDateFormat);
                MyDatePickerDialog d = new MyDatePickerDialog(this,
                        mDateSetListener, cyear, cmonth, cday);
                int maxDeliverydate = bmodel.configurationMasterHelper.LOAD_MAX_DELIVERY_DATE;
                if (maxDeliverydate > 0) {
                    d.getDatePicker().setMaxDate(DateUtil.addDaystoDate(new Date(), maxDeliverydate).getTime());
                }
                d.setPermanentTitle(getResources().getString(R.string.choose_date));
                return d;

            case 1:
                AlertDialog.Builder builder = new AlertDialog.Builder(OrderSummary.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(
                                getResources().getString(
                                        R.string.do_you_want_delete_order))
                        .setPositiveButton(getResources().getString(R.string.yes),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        build = new AlertDialog.Builder(OrderSummary.this);
                                        bmodel.getOrderHeaderBO().setIsSignCaptured(false);
                                        bmodel.synchronizationHelper.deleteFiles(
                                                PHOTO_PATH, bmodel.getOrderHeaderBO().getSignatureName());

                                        bmodel.customProgressDialog(alertDialog, build, OrderSummary.this, getResources().getString(R.string.deleting_order));
                                        alertDialog = build.create();
                                        alertDialog.show();
                                        // clear scheme free products
                                        clearSchemeFreeProduct();

                                        new MyThread(OrderSummary.this,
                                                DataMembers.DELETE_ORDER).start();
                                    }
                                })
                        .setNeutralButton(
                                getResources().getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        isClick = false;
                                    }
                                })
                        .setNegativeButton(
                                getResources().getString(
                                        R.string.delete_stock_and_order),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        build = new AlertDialog.Builder(OrderSummary.this);

                                        bmodel.customProgressDialog(alertDialog, build, OrderSummary.this, getResources().getString(R.string.deleting_order));
                                        alertDialog = build.create();
                                        alertDialog.show();
                                        // clear scheme free products
                                        bmodel.getOrderHeaderBO().setIsSignCaptured(false);
                                        bmodel.synchronizationHelper.deleteFiles(
                                                PHOTO_PATH, bmodel.getOrderHeaderBO().getSignatureName());
                                        clearSchemeFreeProduct();
                                        new deleteStockAndOrder().execute();
                                        new MyThread(OrderSummary.this,
                                                DataMembers.DELETE_ORDER).start();
                                    }
                                });
                bmodel.applyAlertDialogTheme(builder);
                break;

            case 2:
                delivery_date_txt = delievery_date.getText().toString();
                if (bmodel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG) { // if
                    // seller
                    // dialog
                    // enable
                    if (bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
                        delivery_date_txt = "";
                    }
                } else if (bmodel.configurationMasterHelper.IS_INVOICE || !bmodel.configurationMasterHelper.SHOW_DELIVERY_DATE) {
                    delivery_date_txt = "";
                }
                AlertDialog.Builder builder1 = new AlertDialog.Builder(OrderSummary.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(
                                getResources().getString(
                                        R.string.order_saved_locally_order_id_is)
                                        + bmodel.getOrderid())
                        .setNegativeButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        bmodel.outletTimeStampHelper
                                                .updateTimeStampModuleWise(SDUtil
                                                        .now(SDUtil.TIME));
                                        // clear scheme free products
                                        clearSchemeFreeProduct();
                                        finish();
                                        Intent i = new Intent(OrderSummary.this,
                                                HomeScreenTwo.class);
                                        Bundle extras = getIntent().getExtras();
                                        if (extras != null) {
                                            i.putExtra("IsMoveNextActivity", bmodel.configurationMasterHelper.MOVE_NEXT_ACTIVITY);
                                            i.putExtra("CurrentActivityCode", mActivityCode);
                                        }
                                        startActivity(i);

                                    }
                                })
                        .setPositiveButton(
                                getResources().getString(R.string.print_order),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        bmodel.outletTimeStampHelper
                                                .updateTimeStampModuleWise(SDUtil
                                                        .now(SDUtil.TIME));
                                        Intent i;
                                        if (bmodel.configurationMasterHelper.SHOW_ZEBRA_UNIPAL) {
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

                                            builder10 = new AlertDialog.Builder(OrderSummary.this);

                                            bmodel.customProgressDialog(alertDialog, builder10, OrderSummary.this, "Printing....");
                                            alertDialog = builder10.create();
                                            alertDialog.show();
                                        } else if (bmodel.configurationMasterHelper.SHOW_BIXOLONII) {
//                                            finish();
                                            i = new Intent(OrderSummary.this,
                                                    BixolonIIPrint.class);
                                            i.putExtra("IsFromOrder", true);
                                            startActivity(i);
                                            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                                            finish();
                                        } else if (bmodel.configurationMasterHelper.SHOW_BIXOLONI) {
//                                            finish();
                                            i = new Intent(OrderSummary.this,
                                                    BixolonIPrint.class);
                                            i.putExtra("IsFromOrder", true);
                                            startActivity(i);
                                            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                                            finish();
                                        } else if (bmodel.configurationMasterHelper.SHOW_ZEBRA) {
//                                            finish();
                                            i = new Intent(OrderSummary.this,
                                                    InvoicePrintZebraNew.class);
                                            i.putExtra("IsFromOrder", true);
                                            startActivity(i);
                                            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                                            finish();
                                        } else if (bmodel.configurationMasterHelper.SHOW_ZEBRA_ATS) {
//                                            finish();
                                            i = new Intent(OrderSummary.this,
                                                    PrintPreviewScreen.class);
                                            i.putExtra("IsFromOrder", true);
                                            i.putExtra("storediscount",
                                                    discountresult);

                                            startActivity(i);
                                            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                                            finish();
                                        } else if (bmodel.configurationMasterHelper.SHOW_INTERMEC_ATS) {
//                                            finish();
                                            i = new Intent(OrderSummary.this,
                                                    BtPrint4Ivy.class);
                                            i.putExtra("IsFromOrder", true);
                                            i.putExtra("storediscount",
                                                    discountresult);
                                            startActivity(i);
                                            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                                            finish();
                                        } else if (bmodel.configurationMasterHelper.SHOW_ZEBRA_DIAGEO) {
//                                            finish();
                                            i = new Intent(OrderSummary.this,
                                                    PrintPreviewScreenDiageo.class);
                                            i.putExtra("IsFromOrder", true);
                                            i.putExtra("storediscount",
                                                    discountresult);

                                            startActivity(i);
                                            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                                            finish();
                                        } else if (bmodel.configurationMasterHelper.SHOW_ZEBRA_GHANA) {
                                            i = new Intent(OrderSummary.this,
                                                    GhanaPrintPreviewActivity.class);
                                            i.putExtra("IsFromOrder", true);
                                            startActivity(i);
                                            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                                        } else if (bmodel.configurationMasterHelper.COMMON_PRINT_BIXOLON
                                                || bmodel.configurationMasterHelper.COMMON_PRINT_ZEBRA || bmodel.configurationMasterHelper.COMMON_PRINT_SCRYBE) {
                                            if ("1".equalsIgnoreCase(bmodel.retailerMasterBO.getRField4()))
                                                bmodel.productHelper.updateDistributorDetails();

                                            SalesReturnHelper salesReturnHelper = SalesReturnHelper.getInstance(OrderSummary.this);

                                            final List<ProductMasterBO> orderListWithReplace = salesReturnHelper.updateReplaceQtyWithOutTakingOrder(mOrderedProductList);
                                            Vector<ProductMasterBO> orderList = new Vector<>(orderListWithReplace);

                                            bmodel.mCommonPrintHelper.xmlRead("order", false, orderList, null);

                                            if (bmodel.configurationMasterHelper.IS_PRINT_FILE_SAVE)
                                                bmodel.writeToFile(String.valueOf(bmodel.mCommonPrintHelper.getInvoiceData()),
                                                        StandardListMasterConstants.PRINT_FILE_ORDER + bmodel.invoiceNumber);



                                            i = new Intent(OrderSummary.this,
                                                    CommonPrintPreviewActivity.class);
                                            i.putExtra("IsFromOrder", true);
                                            i.putExtra("IsUpdatePrintCount", true);
                                            i.putExtra("isHomeBtnEnable", true);
                                            startActivity(i);
                                            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                                            finish();
                                        } else {
//                                            finish();
                                            i = new Intent(OrderSummary.this,
                                                    BixolonIIPrint.class);
                                            i.putExtra("IsFromOrder", true);
                                            startActivity(i);
                                            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                                            finish();
                                        }
                                    }
                                });
                if (!delivery_date_txt.equals("")) {
                    builder1.setMessage(getResources().getString(R.string.delivery_date_is) + " " + delivery_date_txt);
                }
                bmodel.applyAlertDialogTheme(builder1);
                break;

            case 3:
                delivery_date_txt = delievery_date.getText().toString();
                if (bmodel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG) { // if
                    // seller
                    // dialog
                    // enable
                    if (bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
                        delivery_date_txt = "";
                    }
                } else if (bmodel.configurationMasterHelper.IS_INVOICE || !bmodel.configurationMasterHelper.SHOW_DELIVERY_DATE) {
                    delivery_date_txt = "";
                }
                AlertDialog.Builder builder2 = new AlertDialog.Builder(OrderSummary.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(
                                getResources().getString(
                                        R.string.order_saved_locally_order_id_is)
                                        + bmodel.getOrderid())
                        .setPositiveButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        if (bmodel.mSelectedModule != 3) {
                                            bmodel.outletTimeStampHelper
                                                    .updateTimeStampModuleWise(SDUtil
                                                            .now(SDUtil.TIME));
                                        }
                                        bmodel.productHelper.clearOrderTable();
                                        finish();
                                        if (bmodel.mSelectedModule == 1) {
                                            Intent i = new Intent(
                                                    OrderSummary.this,
                                                    HomeScreenActivity.class);
                                            startActivity(i);
                                        } else if (bmodel.mSelectedModule == 3) {
                                            Intent i = new Intent(
                                                    OrderSummary.this,
                                                    OrderSplitMasterScreen.class);
                                            startActivity(i);

                                        } else {
                                            Intent i = new Intent(
                                                    OrderSummary.this,
                                                    HomeScreenTwo.class);
                                            Bundle extras = getIntent().getExtras();
                                            if (extras != null) {
                                                i.putExtra("IsMoveNextActivity", bmodel.configurationMasterHelper.MOVE_NEXT_ACTIVITY);
                                                i.putExtra("CurrentActivityCode", mActivityCode);
                                            }
                                            startActivity(i);
                                        }
                                    }
                                });
                if (!delivery_date_txt.equals("")) {
                    builder2.setMessage(getResources().getString(R.string.delivery_date_is) + " " + delivery_date_txt);
                }
                bmodel.applyAlertDialogTheme(builder2);
                break;

            case 4:
                AlertDialog.Builder builder3 = new AlertDialog.Builder(OrderSummary.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(
                                getResources().getString(
                                        R.string.please_save_first_before_printing))
                        .setPositiveButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

                                    }
                                });
                bmodel.applyAlertDialogTheme(builder3);
                break;

            case 5:
                AlertDialog.Builder builder4 = new AlertDialog.Builder(OrderSummary.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(
                                getResources().getString(
                                        R.string.do_you_want_delete_order))
                        .setPositiveButton(
                                getResources().getString(R.string.only_order),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        build = new AlertDialog.Builder(OrderSummary.this);
                                        bmodel.getOrderHeaderBO().setIsSignCaptured(false);
                                        bmodel.synchronizationHelper.deleteFiles(
                                                PHOTO_PATH, bmodel.getOrderHeaderBO().getSignatureName());

                                        bmodel.customProgressDialog(alertDialog, build, OrderSummary.this, getResources().getString(R.string.deleting_order));
                                        alertDialog = build.create();
                                        alertDialog.show();
                                        new MyThread(OrderSummary.this,
                                                DataMembers.DELETE_ORDER).start();
                                    }
                                })

                        .setNegativeButton(
                                getResources().getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

                                    }
                                });
                bmodel.applyAlertDialogTheme(builder4);
                break;

            case 6:
                AlertDialog.Builder builder5 = new AlertDialog.Builder(OrderSummary.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle("OrderValue is negative,Please check order")
                        .setPositiveButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

                                    }
                                });
                bmodel.applyAlertDialogTheme(builder5);
                break;

            case 7:
                AlertDialog.Builder builder6 = new AlertDialog.Builder(OrderSummary.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(
                                getResources().getString(
                                        R.string.invoice_created_dou_wnt_print))
                        .setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        if (mBluetoothAdapter == null) {
                                            Toast.makeText(getApplicationContext(),
                                                    "Bluetooth not enabled ",
                                                    Toast.LENGTH_LONG).show();
                                            // !!!!!
                                            finish();
                                            return;
                                        }

                                        Checkbluetoothenable();
                                    }
                                })

                        .setNegativeButton(
                                getResources().getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        bmodel.productHelper.clearOrderTableChecked();
                                        Intent i = new Intent(
                                                OrderSummary.this,
                                                HomeScreenTwo.class);
                                        startActivity(i);
                                        finish();
                                    }
                                });
                bmodel.applyAlertDialogTheme(builder6);
                break;

            case 8:
                AlertDialog.Builder builder7 = new AlertDialog.Builder(OrderSummary.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(
                                "Signature Already taken.Do you want to delete and retake?")
                        .setPositiveButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        bmodel.getOrderHeaderBO().setIsSignCaptured(false);
                                        bmodel.synchronizationHelper.deleteFiles(
                                                PHOTO_PATH, bmodel.getOrderHeaderBO().getSignatureName());
                                        Intent i = new Intent(OrderSummary.this,
                                                CaptureSignatureActivity.class);
                                        i.putExtra("fromModule", "ORDER");
                                        startActivity(i);
                                        bmodel.configurationMasterHelper.setSignatureTitle("Signature");
                                        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                                        finish();
                                    }
                                })
                        .setNegativeButton(
                                getResources().getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                    }
                                });
                bmodel.applyAlertDialogTheme(builder7);
                break;

            case 9:
                AlertDialog.Builder builder9 = new AlertDialog.Builder(OrderSummary.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(
                                getResources().getString(
                                        R.string.invoice_created_dou_wnt_print))
                        .setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        isClick = true;
                                        if (bmodel.configurationMasterHelper.printCount > 0) {
                                            showDialog(10);
                                        } else {

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

                                            builder10 = new AlertDialog.Builder(OrderSummary.this);

                                            bmodel.customProgressDialog(alertDialog, builder10, OrderSummary.this, "Printing....");
                                            alertDialog = builder10.create();
                                            alertDialog.show();
                                        }
                                    }
                                })

                        .setNegativeButton(
                                getResources().getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        bmodel.productHelper.clearOrderTableChecked();
                                        Intent i = new Intent(OrderSummary.this, HomeScreenTwo.class);
                                        startActivity(i);
                                        finish();
                                    }
                                });
                bmodel.applyAlertDialogTheme(builder9);
                break;

            case 10:
                AlertDialog.Builder builder11 = new AlertDialog.Builder(OrderSummary.this)
                        .setTitle("Print Count")
                        .setSingleChoiceItems(bmodel.printHelper.getPrintCountArray(), 0, null)
                        .setCancelable(false)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                                mSelectedPrintCount = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
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

                                builder10 = new AlertDialog.Builder(OrderSummary.this);

                                bmodel.customProgressDialog(alertDialog, builder10, OrderSummary.this, "Printing....");
                                alertDialog = builder10.create();
                                alertDialog.show();
                                // Do something useful withe the position of the selected radio button
                            }
                        });
                bmodel.applyAlertDialogTheme(builder11);
                break;

            case 11:
                AlertDialog.Builder builder12 = new AlertDialog.Builder(OrderSummary.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(
                                getResources().getString(
                                        R.string.order_created_dou_wnt_print))
                        .setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

                                        isClick = true;
                                        if (bmodel.configurationMasterHelper.SHOW_ZEBRA_TITAN || bmodel.configurationMasterHelper.SHOW_ZEBRA_UNIPAL) {
                                            showDialog(10);
                                        } else {
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

                                            builder10 = new AlertDialog.Builder(OrderSummary.this);

                                            bmodel.customProgressDialog(alertDialog, builder10, OrderSummary.this, "Printing....");
                                            alertDialog = builder10.create();
                                            alertDialog.show();
                                        }
                                    }
                                })

                        .setNegativeButton(
                                getResources().getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        Intent i = new Intent(OrderSummary.this, HomeScreenTwo.class);
                                        startActivity(i);
                                        finish();
                                    }
                                });
                bmodel.applyAlertDialogTheme(builder12);
                break;
            default:
                break;
        }
        return null;
    }

    private final DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            Calendar selectedDate = new GregorianCalendar(year, monthOfYear,
                    dayOfMonth);
            delievery_date.setText(DateUtil.convertDateObjectToRequestedFormat(
                    selectedDate.getTime(),
                    ConfigurationMasterHelper.outDateFormat));

            Calendar currentcal = Calendar.getInstance();
            currentcal.add(Calendar.DAY_OF_YEAR, -1);
            if (currentcal.after(selectedDate)) {
                Toast.makeText(
                        getApplicationContext(),
                        getResources().getString(
                                R.string.Please_select_next_day),
                        Toast.LENGTH_SHORT).show();
                delievery_date.setText(nextDate);
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.order_summary_menu, menu);

        /** on/off the items based on the configuration **/
        MenuItem reviewAndPo = menu.findItem(R.id.menu_review);
        reviewAndPo
                .setVisible(bmodel.configurationMasterHelper.SHOW_REVIEW_AND_PO);

        MenuItem discount = menu.findItem(R.id.menu_discount);
        discount.setVisible(bmodel.configurationMasterHelper.SHOW_DISCOUNT_DIALOG);

        MenuItem productReturn = menu.findItem(R.id.menu_product_return);
        if (bmodel.configurationMasterHelper.IS_SIH_VALIDATION
                && bmodel.configurationMasterHelper.SHOW_PRODUCTRETURN) {
            productReturn
                    .setVisible(true);
        } else {
            productReturn.setVisible(false);
        }
        if (bmodel.configurationMasterHelper.IS_SIH_VALIDATION
                && bmodel.configurationMasterHelper.SHOW_COLLECTION_BEFORE_INVOICE
                && bmodel.configurationMasterHelper.IS_INVOICE)
            menu.findItem(R.id.menu_collection).setVisible(true);
        else
            menu.findItem(R.id.menu_collection).setVisible(false);

        if (bmodel.configurationMasterHelper.SHOW_SIGNATURE_SCREEN)
            menu.findItem(R.id.menu_signature).setVisible(true);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i1 = item.getItemId();
        if (i1 == R.id.menu_review) {
            OrderRemarkDialog ordRemarkDialog = new OrderRemarkDialog(
                    OrderSummary.this, null);
            ordRemarkDialog.show();
            return true;
        } else if (i1 == R.id.menu_discount) {

            initiativedialog = new DiscountDialog(OrderSummary.this, null,
                    discountDismissListener);
            initiativedialog.show();
            return true;
        } else if (i1 == R.id.menu_calculator) {
            try {
                ArrayList<HashMap<String, Object>> items = new ArrayList<>();
                final PackageManager pm = getPackageManager();
                List<PackageInfo> packs = pm.getInstalledPackages(0);
                for (PackageInfo pi : packs) {
                    if ("calcul".contains(pi.packageName.toLowerCase().toString())) {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("appName", pi.applicationInfo.loadLabel(pm));
                        map.put("packageName", pi.packageName);
                        items.add(map);
                    }
                }
                if (!items.isEmpty()) {
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
                Commons.printException("" + e);
            }
            return true;
        } else if (i1 == R.id.menu_print) {
            hidealert = true;
            if (bmodel.isEdit()) {
                if (!mOrderedProductList.isEmpty()) {
                    bmodel.getOrderHeaderBO().setOrderValue(
                            getDiscountAppliedValue(enteredDiscAmtOrPercent));
                    bmodel.getOrderHeaderBO().setLinesPerCall(
                            SDUtil.convertToInt((String) lpc.getText()));
                    bmodel.getOrderHeaderBO().setDiscount(
                            enteredDiscAmtOrPercent);
                    bmodel.getOrderHeaderBO()
                            .setDeliveryDate(
                                    DateUtil.convertToServerDateFormat(
                                            delievery_date.getText().toString(),
                                            ConfigurationMasterHelper.outDateFormat));

                    build = new AlertDialog.Builder(OrderSummary.this);

                    bmodel.customProgressDialog(alertDialog, build, OrderSummary.this, getResources().getString(R.string.saving_new_order));
                    alertDialog = build.create();
                    alertDialog.show();

                    if (bmodel.hasOrder()) {
                        new MyThread(OrderSummary.this,
                                DataMembers.SAVEORDERANDSTOCK).start();
                    } else {
                        isClick = false;
                    }
                } else {
                    isClick = false;
                    Toast.makeText(
                            this,
                            getResources().getString(
                                    R.string.no_products_exists),
                            Toast.LENGTH_SHORT);
                }
            } else
                showDialog(4);
        } else if (i1 == R.id.menu_store_wise_discount) {
            FragmentManager fm = getSupportFragmentManager();
            mStoreWiseDiscountDialogFragment = new StorewiseDiscountDialogFragment();
            Bundle bundle = new Bundle();
            bundle.putDouble("totalValue", totalOrderValue);
            bundle.putDouble("enteredDiscAmtOrPercent", enteredDiscAmtOrPercent);
            mStoreWiseDiscountDialogFragment.setArguments(bundle);
            mStoreWiseDiscountDialogFragment.show(fm, "Sample Fragment");
            mStoreWiseDiscountDialogFragment.setCancelable(false);
            return true;
        } else if (i1 == R.id.menu_product_return) {
            if (!isClicked) {
                isClicked = true;
                int productsize;
                if (bmodel.configurationMasterHelper.SHOW_GROUPPRODUCTRETURN) {
                    productsize = bmodel.productHelper
                            .getBomReturnTypeProducts().size();
                } else {
                    productsize = bmodel.productHelper.getBomReturnProducts()
                            .size();
                }
                if (productsize > 0) {
                    returnProductDialog = new ReturnProductDialog(this, this);
                    returnProductDialog.show();
                    returnProductDialog.setCancelable(false);
                } else {
                    Toast.makeText(OrderSummary.this,
                            getResources().getString(R.string.data_not_mapped),
                            Toast.LENGTH_SHORT).show();
                    isClicked = false;
                }
            }
            return true;
        } else if (i1 == R.id.menu_collection) {
            bmodel.downloadBankDetails();
            bmodel.downloadBranchDetails();
            bmodel.collectionHelper.loadPaymentModes();
            double minimumAmount;

            double invoiceAmount = Double.parseDouble(totalval
                    .getText().toString());
            double creditBalance;
            if (bmodel.getRetailerMasterBO()
                    .getCreditLimit() != 0)
                creditBalance = bmodel.getRetailerMasterBO()
                        .getCreditLimit()
                        - bmodel.collectionHelper.calculatePendingOSTAmount();
            else
                creditBalance = 0;
            if (invoiceAmount < creditBalance)
                minimumAmount = 0;
            else
                minimumAmount = invoiceAmount - creditBalance;

            minimumAmount = Double.parseDouble(bmodel
                    .formatValue(minimumAmount));

            if (!isClicked) {
                isClicked = true;
                int productsize = bmodel.collectionHelper.getPaymentModes()
                        .size();
                if (productsize > 0) {
                    collectionBeforeInvoiceDialog = new CollectionBeforeInvoiceDialog(
                            this, this, collectionbo, invoiceAmount,
                            minimumAmount, creditBalance);
                    collectionBeforeInvoiceDialog.show();
                    collectionBeforeInvoiceDialog.setCancelable(false);
                } else {
                    Toast.makeText(OrderSummary.this,
                            getResources().getString(R.string.data_not_mapped),
                            Toast.LENGTH_SHORT).show();
                    isClicked = false;
                }
            }
            return true;
        } else if (i1 == R.id.menu_signature) {
            if (bmodel.getOrderHeaderBO().isSignCaptured()) {
                showDialog(8);
                return true;
            }
            Intent i = new Intent(OrderSummary.this,
                    CaptureSignatureActivity.class);
            i.putExtra("fromModule", "ORDER");
            startActivity(i);
            bmodel.configurationMasterHelper.setSignatureTitle("Signature");
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            finish();
            return true;
        } else if (i1 == R.id.menu_summary_dialog) {
            FragmentManager fm = getSupportFragmentManager();
            OrderSummaryDialogFragment dialogFragment = new OrderSummaryDialogFragment();
            Bundle bundle = new Bundle();

            bundle.putSerializable("OrderList", (Serializable) mOrderedProductList);
            dialogFragment.setArguments(bundle);
            dialogFragment.show(fm, "Dialog Fragment");
        } else if (i1 == R.id.menu_serialno) {
            if (isOrderedSerialNoProducts()) {
                Intent i = new Intent(OrderSummary.this, SerialNoEntryScreen.class);
                startActivity(i);
                overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            } else {
                Toast.makeText(OrderSummary.this, "No Scanned products ", Toast.LENGTH_SHORT).show();
            }
        } else if (i1 == R.id.menu_edit) {
            clearSchemeFreeProduct();
            bmodel.productHelper.clearDiscountQuantity();
            if (bmodel.remarksHelper.getRemarksBO().getModuleCode() == null
                    || bmodel.remarksHelper.getRemarksBO().getModuleCode()
                    .length() == 0)
                bmodel.remarksHelper.getRemarksBO().setModuleCode(
                        StandardListMasterConstants.MENU_STK_ORD);

            bmodel.setDoubleEdit_temp(false);

            Intent i;
            if (screenCode.equals(HomeScreenTwo.MENU_CATALOG_ORDER)) {
                i = new Intent(OrderSummary.this, CatalogOrder.class);
            } else {
                i = new Intent(OrderSummary.this, StockAndOrder.class);
            }
            i.putExtra("OrderFlag", "FromSummary");
            i.putExtra("ScreenCode", (screenCode == null ? "" : screenCode));
            if (bmodel.mSelectedModule != 3)
                bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                        .now(SDUtil.TIME));

            i.putExtra("tempPo",
                    (bmodel.getOrderHeaderBO().getPO() == null ? "" : bmodel
                            .getOrderHeaderBO().getPO()));
            i.putExtra("tempRemark",
                    (bmodel.getOrderHeaderBO().getRemark() == null ? ""
                            : bmodel.getOrderHeaderBO().getRemark()));
            i.putExtra("tempRField1",
                    (bmodel.getOrderHeaderBO().getRField1() == null ? ""
                            : bmodel.getOrderHeaderBO().getRField1()));
            i.putExtra("tempRField2",
                    (bmodel.getOrderHeaderBO().getRField2() == null ? ""
                            : bmodel.getOrderHeaderBO().getRField2()));

            bmodel.setOrderHeaderBO(null); // Clear Object other wise Data
            // Retain in Dialog
            startActivity(i);

            finish();
        } else if (i1 == R.id.menu_delete) {
            if (bmodel.configurationMasterHelper.isStockAvailable())
                showDialog(1);
            else
                showDialog(5);
        }
        return super.onOptionsItemSelected(item);
    }

    public void onClick(View comp) {
        Button vw = (Button) comp;
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        IndicativeOrderReasonDialog indicativeReasonDialog;
        int discountId = 0;
        int isCompanygiven = 0;

        if (vw == delievery_date)
            showDialog(DATE_DIALOG_ID);
        else if (vw == btnsave) {
            if (bmodel.configurationMasterHelper.IS_SHOW_IRDERING_SEQUENCE && shortListOrder != null)
                bmodel.productHelper.setShortProductMaster(shortListOrder);
            fromorder = true;
            if (!isClick) {
                isClick = true;
                if (mOrderedProductList.size() > 0) {

                    if (bmodel.configurationMasterHelper.IS_GST && !isTaxAvailableForAllOrderedProduct()) {
                        // If GST enabled then, every ordered product should have tax
                        bmodel.showAlert(
                                getResources()
                                        .getString(
                                                R.string.tax_not_availble_for_some_product),
                                0);
                        isClick = false;
                        return;
                    }

                    if (bmodel.configurationMasterHelper.IS_SHOW_ONLY_INDICATIVE_ORDER && !bmodel.isReasonProvided()) {
                        indicativeReasonDialog = new IndicativeOrderReasonDialog(this, bmodel);
                        indicativeReasonDialog.show();
                        isClick = false;
                    } else {

                        bmodel.getOrderHeaderBO()
                                .setOrderValue(
                                        getDiscountAppliedValue(enteredDiscAmtOrPercent));
                        bmodel.getOrderHeaderBO().setDiscount(
                                enteredDiscAmtOrPercent);
                        bmodel.getOrderHeaderBO().setDiscountId(discountId);
                        bmodel.getOrderHeaderBO().setIsCompanyGiven(isCompanygiven);


                        bmodel.getOrderHeaderBO().setLinesPerCall(
                                SDUtil.convertToInt((String) lpc.getText()));

                        bmodel.getOrderHeaderBO()
                                .setDeliveryDate(
                                        DateUtil.convertToServerDateFormat(
                                                delievery_date.getText().toString(),
                                                ConfigurationMasterHelper.outDateFormat));

                        build = new AlertDialog.Builder(OrderSummary.this);

                        bmodel.customProgressDialog(alertDialog, build, OrderSummary.this, getResources().getString(R.string.saving_new_order));
                        alertDialog = build.create();
                        alertDialog.show();
                        if (bmodel.configurationMasterHelper.IS_FOCUSBRAND_COUNT_IN_REPORT || bmodel.configurationMasterHelper.IS_MUSTSELL_COUNT_IN_REPORT)
                            getFocusandAndMustSellOrderedProducts();

                        if (bmodel.hasOrder()) {

                            if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION
                                    && bmodel.configurationMasterHelper.IS_SIH_VALIDATION
                                    && bmodel.configurationMasterHelper.IS_INVOICE) {
                                bmodel.batchAllocationHelper
                                        .loadFreeProductBatchList();
                            }

                            if (bmodel.mSelectedModule == 3) {
                                bmodel.invoiceDisount = Double.toString(enteredDiscAmtOrPercent);

                                new MyThread(OrderSummary.this,
                                        DataMembers.SAVEORDERANDSTOCK).start();
                            } else {
                                bmodel.invoiceDisount = Double.toString(enteredDiscAmtOrPercent);

                                new MyThread(OrderSummary.this,
                                        DataMembers.SAVEORDERANDSTOCK).start();
                                bmodel.saveModuleCompletion("MENU_STK_ORD");
                            }
                        } else {
                            isClick = false;
                        }
                    }
                } else {
                    isClick = false;
                    Toast.makeText(
                            this,
                            getResources().getString(
                                    R.string.no_products_exists),
                            Toast.LENGTH_SHORT);
                }
            }
        } else if (vw == btnsaveAndGoInvoice) {
            if (bmodel.configurationMasterHelper.IS_SHOW_IRDERING_SEQUENCE && shortListOrder != null)
                bmodel.productHelper.setShortProductMaster(shortListOrder);
            fromorder = false;
            if (!isClick) {
                isClick = true;

                if (bmodel.configurationMasterHelper.IS_SIH_VALIDATION && !bmodel.isStockAvailableToDeliver(mOrderedProductList)) {
                    Toast.makeText(
                            this,
                            getResources()
                                    .getString(
                                            R.string.stock_not_available_to_deliver),
                            Toast.LENGTH_SHORT).show();
                    isClick = false;
                    return;
                }
                if (bmodel.configurationMasterHelper.IS_VALIDATE_NEGATIVE_INVOICE) {
                    if (totalOrderValue < 0) {
                        showDialog(6);
                        return;
                    }
                }
                if (bmodel.configurationMasterHelper.IS_TAX_APPLIED_VALIDATION) {
                    if (getTaxAppliedTotal() == 0) {
                        Toast.makeText(
                                this,
                                getResources()
                                        .getString(
                                                R.string.cant_save_inovice_zero_tax_applied),
                                Toast.LENGTH_SHORT).show();
                        isClick = false;
                        return;
                    }
                }

                if (bmodel.configurationMasterHelper.SHOW_COLLECTION_BEFORE_INVOICE) {
                    double pendingAmount;
                    double collectedAmount = 0;
                    if (totalval.getText() != null
                            && totalval.getText().length() > 0)
                        Commons.print("Retailer Credit Limit"
                                + bmodel.getRetailerMasterBO().getCreditLimit());

                    pendingAmount = bmodel.getRetailerMasterBO().getCreditLimit()
                            - bmodel.collectionHelper.calculatePendingOSTAmount();
                    Commons.print("Invoice Pending Amount" + pendingAmount);
                    if (collectionbo.getCashamt() > 0
                            || collectionbo.getChequeamt() > 0 || collectionbo.getCreditamt() > 0)
                        collectedAmount = collectionbo.getCashamt()
                                + collectionbo.getChequeamt() + collectionbo.getCreditamt();

                    collectedAmount = Double.parseDouble(bmodel
                            .formatValue(collectedAmount));

                    Commons.print("Invoice Collected Amount" + collectedAmount);
                    pendingAmount = collectedAmount + pendingAmount;
                    Commons.print("Total Invoice Pending Amount" + pendingAmount);

                    pendingAmount = Double.parseDouble(bmodel
                            .formatValue(pendingAmount));

                    if (Double.parseDouble(totalval.getText().toString()) > pendingAmount) {
                        isClick = false;
                        Toast.makeText(
                                this,
                                getResources()
                                        .getString(
                                                R.string.credit_limit_exceed_do_you_wish_to_apply_partially),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                }


                bmodel.getOrderHeaderBO()
                        .setOrderValue(
                                getDiscountAppliedValue(enteredDiscAmtOrPercent));
                bmodel.getOrderHeaderBO().setDiscount(
                        enteredDiscAmtOrPercent);
                bmodel.getOrderHeaderBO().setDiscountId(discountId);
                bmodel.getOrderHeaderBO().setIsCompanyGiven(isCompanygiven);


                bmodel.getOrderHeaderBO().setLinesPerCall(
                        SDUtil.convertToInt((String) lpc.getText()));
                bmodel.getOrderHeaderBO().setDeliveryDate(
                        DateUtil.convertToServerDateFormat(delievery_date.getText()
                                        .toString(),
                                ConfigurationMasterHelper.outDateFormat));

                if (!mOrderedProductList.isEmpty()) {
                    if (bmodel.productHelper.isAllScanned() || !bmodel.configurationMasterHelper.SHOW_SERIAL_NO_SCREEN) {
                        if (bmodel.configurationMasterHelper.IS_SIH_VALIDATION && !bmodel.productHelper.isSihAvailableForOrderProducts(mOrderedProductList)) {
                            isClick = false;
                            Toast.makeText(this, "Ordered value exceeds SIH value.Please edit the order", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (bmodel.hasOrder()) {
                            if (bmodel.configurationMasterHelper.IS_SHOW_ONLY_INDICATIVE_ORDER && !bmodel.isReasonProvided()) {
                                indicativeReasonDialog = new IndicativeOrderReasonDialog(this, bmodel);
                                indicativeReasonDialog.show();
                                isClick = false;
                            } else {
                                if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION
                                        && bmodel.configurationMasterHelper.IS_SIH_VALIDATION
                                        && bmodel.configurationMasterHelper.IS_INVOICE) {
                                    bmodel.batchAllocationHelper
                                            .loadFreeProductBatchList();
                                }

                                bmodel.invoiceDisount = Double.toString(enteredDiscAmtOrPercent);
                                if (bmodel.configurationMasterHelper.IS_INVOICE) {
                                    build = new AlertDialog.Builder(OrderSummary.this);

                                    bmodel.customProgressDialog(alertDialog, build, OrderSummary.this, getResources().getString(R.string.saving_invoice));
                                    alertDialog = build.create();
                                    alertDialog.show();
                                } else {
                                    build = new AlertDialog.Builder(OrderSummary.this);

                                    bmodel.customProgressDialog(alertDialog, build, OrderSummary.this, getResources().getString(R.string.saving_new_order));
                                    alertDialog = build.create();
                                    alertDialog.show();
                                }
                                if (bmodel.configurationMasterHelper.IS_FOCUSBRAND_COUNT_IN_REPORT || bmodel.configurationMasterHelper.IS_MUSTSELL_COUNT_IN_REPORT)
                                    getFocusandAndMustSellOrderedProducts();


                                //Adding accumulation scheme free products to the last ordered product list, so that it will listed on print
                                updateOffInvoiceSchemeInProductOBJ();


                                new MyThread(this, DataMembers.SAVEINVOICE).start();
                            }
                        } else {
                            isClick = false;
                            Toast.makeText(
                                    this,
                                    getResources().getString(
                                            R.string.no_products_exists),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        isClick = false;
                        Toast.makeText(
                                this,
                                getResources().getString(R.string.please_scan_all_products),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    isClick = false;
                    bmodel.showAlert(
                            getResources().getString(R.string.no_products_exists),
                            0);
                }
            }
        }

    }

    private boolean isTaxAvailableForAllOrderedProduct() {
        for (ProductMasterBO bo : mOrderedProductList) {
            if (bmodel.productHelper.getmTaxListByProductId().get(bo.getProductID()) == null
                    || bmodel.productHelper.getmTaxListByProductId().get(bo.getProductID()).size() == 0) {
                return false;
            }
        }
        return true;
    }

    private int getTaxAppliedTotal() {
        Vector<ProductMasterBO> productList = bmodel.productHelper
                .getProductMaster();
        if (productList == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.no_products_exists), 0);
            return 0;
        }

        int productsCount = productList.size();

        for (int i = 0; i < productsCount; i++) {

            ProductMasterBO productBO = productList.elementAt(i);
            if (productBO.getOrderedCaseQty() > 0
                    || productBO.getOrderedPcsQty() > 0
                    || productBO.getOrderedOuterQty() > 0) {
                if (productBO.getTaxValue() > 0)
                    return 1;
            }
        }
        return 0;
    }

    public Handler getHandler() {
        return handler;
    }

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            isClick = false;

            if (msg.what == DataMembers.NOTIFY_DATABASE_NOT_SAVED) {
                Toast.makeText(OrderSummary.this, "DataBase Restore failed.",
                        Toast.LENGTH_SHORT).show();
            }

            if (msg.what == DataMembers.NOTIFY_ORDER_SAVED) {
                try {
                    alertDialog.dismiss();
                    Commons.print("test," + bmodel.mSelectedModule + ">>"
                            + bmodel.configurationMasterHelper.SHOW_PRINT_ORDER
                            + ">>>" + hidealert);
                    if (bmodel.mSelectedModule == 1
                            || bmodel.mSelectedModule == 3) {
                        showDialog(3);
                    } else {
                        if (bmodel.configurationMasterHelper.SHOW_PRINT_ORDER
                                && (bmodel.configurationMasterHelper.SHOW_ZEBRA_GHANA || bmodel.configurationMasterHelper.SHOW_ZEBRA_DIAGEO || bmodel.configurationMasterHelper.SHOW_ZEBRA_TITAN || bmodel.configurationMasterHelper.SHOW_ZEBRA_UNIPAL))
                            showDialog(11);

                        else if (bmodel.configurationMasterHelper.SHOW_PRINT_ORDER
                                && (!hidealert))

                            showDialog(2);
                        else {
                            if (hidealert) {
                                bmodel.outletTimeStampHelper
                                        .updateTimeStampModuleWise(SDUtil
                                                .now(SDUtil.TIME));
//                                finish();
                                Intent i;
                                if (bmodel.configurationMasterHelper.SHOW_BIXOLONII) {
                                    Commons.print("SHOW_BIXOLONII>>>>>>>>>>>>>," + "handle");
                                    i = new Intent(OrderSummary.this,
                                            BixolonIIPrint.class);
                                    i.putExtra("IsFromOrder", true);
                                } else if (bmodel.configurationMasterHelper.SHOW_BIXOLONI) {
                                    Commons.print("SHOW_BIXOLONI>>>>>>>>>>>>>," +
                                            "handle");
                                    i = new Intent(OrderSummary.this,
                                            BixolonIPrint.class);
                                    i.putExtra("IsFromOrder", true);
                                } else if (bmodel.configurationMasterHelper.SHOW_ZEBRA) {
                                    Commons.print("SHOW_ZEBRA>>>>>>>>>>>>>," + "handle");
                                    i = new Intent(OrderSummary.this,
                                            InvoicePrintZebraNew.class);
                                } else if (bmodel.configurationMasterHelper.SHOW_ZEBRA_ATS) {
                                    Commons.print("SHOW_ZEBRA_ATS>>>>>>>>>>>>>," +
                                            "handle");
                                    i = new Intent(OrderSummary.this,
                                            PrintPreviewScreen.class);
                                    i.putExtra("storediscount", discountresult);

                                } else if (bmodel.configurationMasterHelper.SHOW_INTERMEC_ATS) {
                                    Commons.print("SHOW_INTERMEC_ATS>>>>>>>>>>>>>," +
                                            "handle");
                                    i = new Intent(OrderSummary.this,
                                            BtPrint4Ivy.class);
                                    i.putExtra("storediscount", discountresult);
                                } else if (bmodel.configurationMasterHelper.SHOW_ZEBRA_DIAGEO) {
                                    Commons.print("SHOW_ZEBRA_DIAGEO>>>>>>>>>>>>>," +
                                            "handle");
                                    i = new Intent(OrderSummary.this,
                                            PrintPreviewScreenDiageo.class);
                                    i.putExtra("storediscount", discountresult);

                                } else if (bmodel.configurationMasterHelper.SHOW_ZEBRA_GHANA) {
                                    i = new Intent(OrderSummary.this,
                                            GhanaPrintPreviewActivity.class);
                                    i.putExtra("IsFromOrder", true);
                                    startActivity(i);
                                } else if (bmodel.configurationMasterHelper.COMMON_PRINT_BIXOLON
                                        || bmodel.configurationMasterHelper.COMMON_PRINT_ZEBRA || bmodel.configurationMasterHelper.COMMON_PRINT_SCRYBE) {
                                    if ("1".equalsIgnoreCase(bmodel.retailerMasterBO.getRField4()))
                                        bmodel.productHelper.updateDistributorDetails();

                                    SalesReturnHelper salesReturnHelper = SalesReturnHelper.getInstance(OrderSummary.this);
                                    final List<ProductMasterBO> orderListWithReplace = salesReturnHelper.updateReplaceQtyWithOutTakingOrder(mOrderedProductList);
                                    Vector<ProductMasterBO> orderList = new Vector<>(orderListWithReplace);
                                    bmodel.mCommonPrintHelper.xmlRead("order", false, orderList, null);

                                    if (bmodel.configurationMasterHelper.IS_PRINT_FILE_SAVE)
                                        bmodel.writeToFile(String.valueOf(bmodel.mCommonPrintHelper.getInvoiceData()),
                                                StandardListMasterConstants.PRINT_FILE_ORDER + bmodel.invoiceNumber);

                                    i = new Intent(OrderSummary.this,
                                            CommonPrintPreviewActivity.class);
                                    i.putExtra("IsFromOrder", true);
                                    i.putExtra("IsUpdatePrintCount", true);
                                    i.putExtra("isHomeBtnEnable", true);
                                    startActivity(i);
//                                    finish();
                                } else {
                                    Commons.print("ELSE>>>>>>>>>>>>>," + "handle");
                                    i = new Intent(OrderSummary.this,
                                            BixolonIIPrint.class);
                                    i.putExtra("IsFromOrder", true);

                                }
                                startActivity(i);
                                overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                                finish();

                            } else
                                showDialog(3);
                        }
                    }
                } catch (Exception e) {
                    Commons.printException("" + e);
                }
            }

            if (msg.what == DataMembers.NOTIFY_ORDER_DELETED) {
                try {
                    alertDialog.dismiss();
                    bmodel = (BusinessModel) getApplicationContext();
                    if (bmodel.mSelectedModule == 3) {
                        bmodel.showAlert(
                                getResources().getString(
                                        R.string.order_deleted_sucessfully)
                                        + bmodel.getOrderid(),
                                DataMembers.NOTIFY_ORDER_DELETED_FOR_ORDERSPLIT);
                    } else
                        bmodel.showAlert(
                                getResources().getString(
                                        R.string.order_deleted_sucessfully)
                                        + bmodel.getOrderid(),
                                DataMembers.NOTIFY_ORDER_SAVED);
                } catch (Exception e) {
                    Commons.printException("" + e);
                }
            }

            if (msg.what == DataMembers.NOTIFY_INVOICE_SAVED) {
                try {
                    bmodel.getPrintCount();
                    alertDialog.dismiss();
                    bmodel = (BusinessModel) getApplicationContext();

                    if (bmodel.configurationMasterHelper.IS_INVOICE) {
                        if (bmodel.configurationMasterHelper.SHOW_ZEBRA_UNIPAL) {

                            showDialog(9);
                        } else if (bmodel.configurationMasterHelper.SHOW_ZEBRA_TITAN) {

                            Intent i = new Intent(OrderSummary.this,
                                    PrintPreviewScreenTitan.class);
                            i.putExtra("IsFromOrder", true);
                            i.putExtra("entryLevelDis", productEntryLevelDis);
                            startActivity(i);
                            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                            finish();
                        } else if (bmodel.configurationMasterHelper.COMMON_PRINT_BIXOLON
                                || bmodel.configurationMasterHelper.COMMON_PRINT_ZEBRA || bmodel.configurationMasterHelper.COMMON_PRINT_SCRYBE) {
                            if ("1".equalsIgnoreCase(bmodel.getRetailerMasterBO().getRField4())) {
                                bmodel.productHelper.updateDistributorDetails();
                            }

                            SalesReturnHelper salesReturnHelper = SalesReturnHelper.getInstance(OrderSummary.this);

                            final List<ProductMasterBO> orderListWithReplace = salesReturnHelper.updateReplaceQtyWithOutTakingOrder(mOrderedProductList);
                            Vector<ProductMasterBO> orderList = new Vector<>(orderListWithReplace);
                            bmodel.mCommonPrintHelper.xmlRead("invoice_print.xml", true, orderList, null);

                            if (bmodel.configurationMasterHelper.IS_PRINT_FILE_SAVE)
                                bmodel.writeToFile(String.valueOf(bmodel.mCommonPrintHelper.getInvoiceData()),
                                        StandardListMasterConstants.PRINT_FILE_INVOICE + bmodel.invoiceNumber);

                            Intent i = new Intent(OrderSummary.this,
                                    CommonPrintPreviewActivity.class);
                            i.putExtra("IsFromOrder", true);
                            i.putExtra("IsUpdatePrintCount", true);
                            i.putExtra("isHomeBtnEnable", true);
                            startActivity(i);
                            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                            finish();
                        } else {
                            bmodel.showAlert(
                                    getResources()
                                            .getString(
                                                    R.string.invoice_created_successfully),
                                    DataMembers.NOTIFY_INVOICE_SAVED);
                        }
                    } else {
                        bmodel.showAlert(
                                getResources()
                                        .getString(
                                                R.string.order_saved_and_print_preview_created_successfully),
                                DataMembers.NOTIFY_INVOICE_SAVED);
                    }
                } catch (Exception e) {
                    Commons.printException("EXCEPTION<><><><>," + "" + e);
                }
            }
            if (msg.what == BtService.STATE_CONNECTED) {
                Toast.makeText(getApplicationContext(), "Connected",
                        Toast.LENGTH_SHORT).show();
                new Checkandprint().execute();

            }
            if (msg.what == BtService.STATE_CONNECTING) {
                Toast.makeText(getApplicationContext(), "Connecting",
                        Toast.LENGTH_SHORT).show();
            }
            if (msg.what == BtService.STATE_LISTEN)
                if (msg.what == BtService.STATE_NONE) {
                    Toast.makeText(getApplicationContext(), "None",
                            Toast.LENGTH_SHORT).show();

                }
            if (msg.what == MESSAGE_DEVICE_NAME) {
                String mConnectedDeviceName;
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(),
                        "Device Name " + mConnectedDeviceName,
                        Toast.LENGTH_SHORT).show();

            }
            if (msg.what == MESSAGE_TOAST) {
                Toast.makeText(getApplicationContext(),
                        msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
                        .show();
                bmodel.productHelper.clearOrderTableChecked();
                Intent i = new Intent(OrderSummary.this, HomeScreenTwo.class);
                startActivity(i);
                finish();
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (D)
            Commons.print(TAG + ",onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:// When DeviceListActivity returns with a
                // device to connect
                if (resultCode == Activity.RESULT_OK) {
                    String address = msettings.getString("MAC", "");
                    BluetoothDevice device = mBluetoothAdapter
                            .getRemoteDevice(address);
                    mChatService.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT: // When the request to enable Bluetooth returns
                Commons.print(TAG + ",BT not enabled");
                Toast.makeText(this, "Blue tooth not enable",
                        Toast.LENGTH_SHORT).show();
                finish();
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiver != null)
            unregisterReceiver(mReceiver);
        if (mChatService != null) {
            mChatService.stop();
        }
        unbindDrawables(findViewById(R.id.root));
    }

    /**
     * this would clear all the resources used of the layout.
     *
     * @param view
     */
    private void unbindDrawables(View view) {
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

    public interface OrderRemarksClickListener {

    }

    public void numberPressed(View vw) {
        if (returnProductDialog != null && returnProductDialog.isShowing()) {
            returnProductDialog.numberPressed(vw);
        }
        if (collectionBeforeInvoiceDialog != null && collectionBeforeInvoiceDialog.isShowing()) {
            collectionBeforeInvoiceDialog.numberPressed(vw);
        }
        if (initiativedialog != null && initiativedialog.isShowing()) {
            initiativedialog.numberPressed(vw);
        }
        if (mStoreWiseDiscountDialogFragment != null && mStoreWiseDiscountDialogFragment.isVisible()) {
            mStoreWiseDiscountDialogFragment.numberPressed(vw);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!bmodel.configurationMasterHelper.SHOW_PRINT_ORDER
                || (bmodel.configurationMasterHelper.SHOW_ZEBRA_GHANA || bmodel.configurationMasterHelper.SHOW_ZEBRA_DIAGEO || bmodel.configurationMasterHelper.SHOW_ZEBRA_TITAN || bmodel.configurationMasterHelper.SHOW_ZEBRA_UNIPAL || bmodel.configurationMasterHelper.COMMON_PRINT_ZEBRA || !bmodel.isEdit())) {
            menu.findItem(R.id.menu_print).setVisible(false);
        } else
            menu.findItem(R.id.menu_print).setVisible(true);

        if (bmodel.configurationMasterHelper.SHOW_CALC)
            menu.findItem(R.id.menu_calculator).setVisible(true);
        else
            menu.findItem(R.id.menu_calculator).setVisible(false);
        if ((bmodel.configurationMasterHelper.SHOW_STORE_WISE_DISCOUNT_DLG && bmodel.configurationMasterHelper.BILL_WISE_DISCOUNT == 0)
                || bmodel.configurationMasterHelper.SHOW_TOTAL_DISCOUNT_EDITTEXT)
            menu.findItem(R.id.menu_store_wise_discount).setVisible(true);
        else
            menu.findItem(R.id.menu_store_wise_discount).setVisible(false);
        if (bmodel.configurationMasterHelper.SHOW_ORDER_SUMMARY_DETAIL_DIALOG) {
            menu.findItem(R.id.menu_summary_dialog).setVisible(true);
        }

        if (bmodel.configurationMasterHelper.SHOW_SERIAL_NO_SCREEN)
            menu.findItem(R.id.menu_serialno).setVisible(true);
        else
            menu.findItem(R.id.menu_serialno).setVisible(false);

        if (bmodel.mSelectedModule == 1)
            menu.findItem(R.id.menu_delete).setVisible(false);
        else if (!bmodel.isEdit())
            menu.findItem(R.id.menu_delete).setVisible(false);
        else {
            menu.findItem(R.id.menu_delete).setVisible(true);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    private class deleteStockAndOrder extends AsyncTask<Integer, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(Integer... params) {
            bmodel.orderAndInvoiceHelper.deleteStockAndOrder();
            return null;
        }
    }

    private double getDiscountAppliedValue(double discnt) {
        double total;
        total = totalOrderValue;
        Commons.print("discounttype"
                + bmodel.configurationMasterHelper.discountType);
        double discountValue = 0;
        try {
            if (bmodel.configurationMasterHelper.discountType == 1) {
                if (discnt > 100)
                    discnt = 100;
                discountValue = (total / 100) * discnt;

            } else if (bmodel.configurationMasterHelper.discountType == 2) {
                discountValue = discnt;

            }

            total = total - discountValue;
            bmodel.getOrderHeaderBO().setDiscountValue(discountValue);
        } catch (Exception e) {
            Commons.printException(e);
        }
        return total;
    }

    private final android.content.DialogInterface.OnDismissListener discountDismissListener = new android.content.DialogInterface.OnDismissListener() {

        @Override
        public void onDismiss(DialogInterface dialog) {
            dialog.dismiss();
        }
    };

    /**
     * //@param produBo
     *
     * @author rajesh.k After applied scheme update scheme details in product
     * wise total and added scheme free product in any one of same
     * scheme Buy product.
     */
    private void updateSchemeDetails() {
        ArrayList<SchemeBO> appliedSchemeList = bmodel.schemeDetailsMasterHelper
                .getAppliedSchemeList();
        if (appliedSchemeList != null) {
            for (SchemeBO schemeBO : appliedSchemeList) {
                if (schemeBO != null) {
                    if (schemeBO.isAmountTypeSelected()) {
                        totalOrderValue = totalOrderValue
                                - schemeBO.getSelectedAmount();
                    }

                    List<SchemeProductBO> schemeproductList = schemeBO
                            .getBuyingProducts();
                    int i = 0;
                    boolean isBuyProductAvailable = false;
                    if (schemeproductList != null) {
                        ArrayList<String> productidList = new ArrayList<>();
                        for (SchemeProductBO schemeProductBo : schemeproductList) {
                            ProductMasterBO productBO = bmodel.productHelper
                                    .getProductMasterBOById(schemeProductBo
                                            .getProductId());
                            if (productBO != null) {
                                if (!productidList.contains(productBO.getProductID())) {
                                    productidList.add(productBO.getProductID());
                                    i = i++;
                                    if (productBO != null) {
                                        if (productBO.getOrderedPcsQty() > 0
                                                || productBO.getOrderedCaseQty() > 0
                                                || productBO.getOrderedOuterQty() > 0) {
                                            isBuyProductAvailable = true;
                                            if (schemeBO.isAmountTypeSelected()) {
                                                schemeProductBo.setDiscountValue(schemeBO.getSelectedAmount());
                                                if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION
                                                        && bmodel.configurationMasterHelper.IS_SIH_VALIDATION
                                                        && bmodel.configurationMasterHelper.IS_INVOICE) {
                                                    if (productBO
                                                            .getBatchwiseProductCount() > 0) {
                                                        ArrayList<ProductMasterBO> batchList = bmodel.batchAllocationHelper.getBatchlistByProductID().get(productBO.getProductID());
                                                        if (batchList != null && !batchList.isEmpty()) {
                                                            for (ProductMasterBO batchProduct : batchList) {
                                                                int totalQty = batchProduct.getOrderedPcsQty() + (batchProduct.getOrderedCaseQty() * productBO.getCaseSize())
                                                                        + (batchProduct.getOrderedOuterQty() * productBO.getOutersize());
                                                                if (totalQty > 0) {

                                                                    double discProd = schemeBO.getSelectedAmount() / schemeBO.getOrderedProductCount();
                                                                    batchProduct.setSchemeDiscAmount(batchProduct.getSchemeDiscAmount() + (discProd / productBO.getOrderedBatchCount()));
                                                                }
                                                            }
                                                        }
                                                    } else {
                                                        productBO.setSchemeDiscAmount(productBO.getSchemeDiscAmount() + (schemeBO.getSelectedAmount() / schemeBO.getOrderedProductCount()));
                                                    }
                                                } else {
                                                    productBO.setSchemeDiscAmount(productBO.getSchemeDiscAmount() + (schemeBO.getSelectedAmount() / schemeBO.getOrderedProductCount()));
                                                }
                                            } else if (schemeBO.isPriceTypeSeleted()) {
                                                double totalpriceDiscount;

                                                if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION
                                                        && bmodel.configurationMasterHelper.IS_SIH_VALIDATION
                                                        && bmodel.configurationMasterHelper.IS_INVOICE) {
                                                    if (productBO
                                                            .getBatchwiseProductCount() > 0) {
                                                        totalpriceDiscount = bmodel.schemeDetailsMasterHelper
                                                                .updateSchemeProducts(
                                                                        productBO,
                                                                        schemeBO.getSelectedPrice(),
                                                                        "SCH_PR", true);
                                                    } else {
                                                        totalpriceDiscount = bmodel.schemeDetailsMasterHelper
                                                                .updateSchemeProducts(
                                                                        productBO,
                                                                        schemeBO.getSelectedPrice(),
                                                                        "SCH_PR", false);
                                                    }

                                                } else {
                                                    totalpriceDiscount = bmodel.schemeDetailsMasterHelper
                                                            .updateSchemeProducts(
                                                                    productBO,
                                                                    schemeBO.getSelectedPrice(),
                                                                    "SCH_PR", false);
                                                }

                                                if (productBO.getDiscount_order_value() > 0) {
                                                    productBO
                                                            .setDiscount_order_value(productBO
                                                                    .getDiscount_order_value()
                                                                    - totalpriceDiscount);

                                                }
                                                if (productBO.getSchemeAppliedValue() > 0) {
                                                    productBO.setSchemeAppliedValue(productBO.getSchemeAppliedValue() - totalpriceDiscount);
                                                }

                                                schemeProductBo.setDiscountValue(totalpriceDiscount);

                                                totalOrderValue = totalOrderValue
                                                        - totalpriceDiscount;

                                            } else if (schemeBO
                                                    .isDiscountPrecentSelected()) {
                                                double totalPercentageDiscount;
                                                if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION
                                                        && bmodel.configurationMasterHelper.IS_SIH_VALIDATION
                                                        && bmodel.configurationMasterHelper.IS_INVOICE) {
                                                    if (productBO
                                                            .getBatchwiseProductCount() > 0) {
                                                        totalPercentageDiscount = bmodel.schemeDetailsMasterHelper
                                                                .updateSchemeProducts(
                                                                        productBO,
                                                                        schemeBO.getSelectedPrecent(),
                                                                        "SCH_PER", true);
                                                    } else {
                                                        totalPercentageDiscount = bmodel.schemeDetailsMasterHelper
                                                                .updateSchemeProducts(
                                                                        productBO,
                                                                        schemeBO.getSelectedPrecent(),
                                                                        "SCH_PER",
                                                                        false);
                                                    }
                                                } else {
                                                    totalPercentageDiscount = bmodel.schemeDetailsMasterHelper
                                                            .updateSchemeProducts(
                                                                    productBO,
                                                                    schemeBO.getSelectedPrecent(),
                                                                    "SCH_PER", false);
                                                }

                                                if (productBO.getDiscount_order_value() > 0) {
                                                    productBO
                                                            .setDiscount_order_value(productBO
                                                                    .getDiscount_order_value()
                                                                    - totalPercentageDiscount);
                                                }

                                                if (productBO.getSchemeAppliedValue() > 0) {
                                                    productBO.setSchemeAppliedValue(productBO.getSchemeAppliedValue() - totalPercentageDiscount);
                                                }
                                                schemeProductBo.setDiscountValue(totalPercentageDiscount);
                                                totalOrderValue = totalOrderValue
                                                        - totalPercentageDiscount;
                                            } else if (schemeBO
                                                    .isQuantityTypeSelected()) {
                                                updateSchemeFreeproduct(schemeBO,
                                                        productBO);
                                                break;
                                            }
                                        } else {
                                            if (schemeBO.isQuantityTypeSelected()) {
                                                // if  Accumulation scheme's buy product not avaliable, free product set in First order product object
                                                if (i == schemeproductList.size() && !isBuyProductAvailable) {
                                                    ProductMasterBO firstProductBO = mOrderedProductList.get(0);
                                                    updateSchemeFreeproduct(schemeBO,
                                                            firstProductBO);
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    /**
     * Method to add free product list into any one of scheme buy product
     *
     * @param schemeBO
     * @param productBO
     */
    private void updateSchemeFreeproduct(SchemeBO schemeBO,
                                         ProductMasterBO productBO) {
        List<SchemeProductBO> freeProductList = schemeBO.getFreeProducts();
        if (productBO.getSchemeProducts() == null) {
            productBO.setSchemeProducts(new ArrayList<SchemeProductBO>());
        }

        if (freeProductList != null) {
            for (SchemeProductBO freeProductBo : freeProductList) {
                if (freeProductBo.getQuantitySelected() > 0) {
                    ProductMasterBO product = bmodel.productHelper
                            .getProductMasterBOById(freeProductBo
                                    .getProductId());
                    if (product != null) {
                        productBO.getSchemeProducts().add(freeProductBo);
                    }
                }
            }
        }

    }

    /**
     * @author rajesh.k method to use clear free product object from Ordered
     * productmasterBO
     */
    private void clearSchemeFreeProduct() {
        for (ProductMasterBO productB0 : mOrderedProductList) {
            if (productB0.getSchemeProducts() != null) {
                productB0.getSchemeProducts().clear();
            }
            productB0.setCompanyTypeDiscount(0);
            productB0.setDistributorTypeDiscount(0);

        }

    }

    private void Checkbluetoothenable() {
        try {
            if (!mBluetoothAdapter.isEnabled()) // If BT is not on, request that
            // it
            // be enabled. setup will then
            // be
            // called during
            // onActivityResult
            {
                Toast.makeText(this, " Bluetooth Not Enabled",
                        Toast.LENGTH_SHORT).show();
                bmodel.productHelper.clearOrderTableChecked();
                Intent i = new Intent(OrderSummary.this, HomeScreenTwo.class);
                startActivity(i);
                finish();

            } else { // Otherwise, setup the chat session
                if (mChatService == null) {
                    mChatService = new BtService(getApplicationContext(),
                            handler);
                    String address = msettings.getString("MAC", "");
                    BluetoothDevice device = mBluetoothAdapter
                            .getRemoteDevice(address);
                    mChatService.connect(device);
                    checkBTConn();
                }
            }
        } catch (Exception e) {
            checkmacadd();
            // TODO Auto-generated catch block
            Commons.printException("" + e);
        }
    }

    private void checkmacadd() {
        Toast.makeText(this, "Please check mac address ", Toast.LENGTH_SHORT)
                .show();
        bmodel.productHelper.clearOrderTableChecked();
        Intent i = new Intent(OrderSummary.this, HomeScreenTwo.class);
        startActivity(i);
        finish();
    }

    private void Printdata() {
        try {
            ZFPLib zfp = mChatService.zfplib;
            zfp.openFiscalBon(1, "0000", false, false, false);

            boolean mgoldenstore = false;
            for (ProductMasterBO sku : bmodel.productHelper.getProductMaster()) {
                double vatAmount = 0.0;
                int taxSize = sku.getTaxes().size();
                for (int ii = 0; ii < taxSize; ii++) {
                    vatAmount = sku.getTaxes().get(ii).getTaxRate();
                    break;
                }

                if (sku.getOrderedPcsQty() > 0 || sku.getOrderedCaseQty() > 0
                        || sku.getOrderedOuterQty() > 0) {
                    float pieceCount = (sku.getOrderedCaseQty() * sku
                            .getCaseSize())
                            + (sku.getOrderedPcsQty())
                            + (sku.getOrderedOuterQty() * sku.getOutersize());

                    float taxdisc = (float) vatAmount;
                    double percent = 0;
                    if (sku.getIsscheme() == 1) {
                        percent = sku.getMschemeper();
                    }

                    float Goldenstore = 0;
                    if (bmodel.configurationMasterHelper.SHOW_GOLD_STORE_DISCOUNT
                            && bmodel.productHelper
                            .isGoldenStoreInCurrentandLastVisit()) {

                        Goldenstore = (float) bmodel.productHelper
                                .applyGoldStoreLineDiscount();
                        mgoldenstore = true;
                    }

                    float discount = (float) percent + Goldenstore;

                    char taxgrp;
                    if (Math.round(taxdisc) == 16) {
                        taxgrp = '1';
                    } else if (Math.round(taxdisc) == 18) {
                        taxgrp = '2';
                    } else {
                        taxgrp = '0';
                    }
                    System.out.println("taxdisc=" + Math.round(taxdisc)
                            + "taxgrp=" + taxgrp + " percent=" + -discount
                            + "sku.getIsscheme()=" + sku.getIsscheme());

                    zfp.sellFree(sku.getProductShortName(), taxgrp,
                            sku.getSrp(), pieceCount, -discount);
                }
            }
            double sum = zfp
                    .calcIntermediateSum(false, false, false, 0.0f, '0');
            zfp.payment(sum, 0, false);

            if (mgoldenstore) {
                zfp.printText("**Golden store applied**", 2);
            }
            zfp.closeFiscalBon();

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    private void checkBTConn() {
        if (mChatService.getState() != BtService.STATE_CONNECTED)
            Toast.makeText(this, "Not connected", Toast.LENGTH_SHORT).show();
    }

    private class Checkandprint extends AsyncTask<Integer, Integer, Boolean> {
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            builder = new AlertDialog.Builder(OrderSummary.this);

            bmodel.customProgressDialog(alertDialog, builder, OrderSummary.this, "Printing");
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            Commons.print(TAG + ", Asynchrous STATE :" + mChatService.getState());
            if (mChatService.getState() != BtService.STATE_CONNECTED) {
                return false;
            } else {
                Printdata();
                return true;
            }
        }

        @Override
        protected void onPostExecute(Boolean connect) {
            alertDialog.dismiss();
            if (!connect) {
                Toast.makeText(getApplicationContext(), "Not connected",
                        Toast.LENGTH_SHORT).show();
            }
            bmodel.productHelper.clearOrderTableChecked();
            Intent i = new Intent(OrderSummary.this, HomeScreenTwo.class);
            startActivity(i);
            finish();
        }

    }

    private void doConnection(String printername) {
        try {
            printer = connect();
            if (printer != null) {
                bmodel.vanmodulehelper.downloadSubDepots();
                printInvoice(printername);
            } else {
                bmodel.productHelper.clearOrderTable();
                disconnect();
                alertDialog.dismiss();
                Toast.makeText(this, "Printer not connected .Please check  Mac Address..", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(this, HomeScreenTwo.class);
                startActivity(i);
                finish();
            }
        } catch (Exception e) {
            Commons.printException("" + e);
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
        } catch (ConnectionException e) {
            Commons.printException("" + e);
            DemoSleeper.sleep(1000);
            disconnect();
        } catch (Exception e) {
            Commons.printException("" + e);
        }

        printer = null;

        if (zebraPrinterConnection.isConnected()) {
            try {
                printer = ZebraPrinterFactory.getInstance(PrinterLanguage.CPCL,
                        zebraPrinterConnection);

                PrinterLanguage pl = printer.getPrinterControlLanguage();

                Commons.print(TAG + "PRINT LANGUAGE : " + pl);
            } catch (ConnectionException e) {
                Commons.print(TAG
                        + "PRINT LANGUAGE : UNKNOWN : PrinterConnectionException");
                Commons.printException("" + e);
            }
        }
        return printer;
    }

    private void printInvoice(String printername) {
        try {
            if (printername.equals(ZEBRA_3INCH)) {
                if (bmodel.configurationMasterHelper.SHOW_ZEBRA_UNIPAL) {
                    bmodel.print_count = 0;
                    bmodel.printHelper.setPrintCnt(0);
                    for (int i = 0; i < mSelectedPrintCount + 1; i++) {
                        zebraPrinterConnection.write(bmodel.printHelper.printDatafor3inchprinterForUnipal(mOrderedProductList, fromorder, 1));
                        if (!fromorder) {
                            bmodel.updatePrintCount(1);
                            bmodel.getPrintCount();
                            bmodel.printHelper.setPrintCnt(bmodel.print_count);
                        }
                    }
                } else if (bmodel.configurationMasterHelper.SHOW_ZEBRA_TITAN) {
                    for (int i = 0; i < mSelectedPrintCount + 1; i++) {
                        zebraPrinterConnection.write(bmodel.printHelper.printDataforTitan3inchOrderprinter(mOrderedProductList, 0));
                        if (!fromorder) {
                            bmodel.updatePrintCount(1);
                            bmodel.getPrintCount();
                            bmodel.printHelper.setPrintCnt(bmodel.print_count);
                        }
                    }
                } else if (bmodel.configurationMasterHelper.SHOW_ZEBRA_GHANA || bmodel.configurationMasterHelper.SHOW_ZEBRA_DIAGEO) {
                    zebraPrinterConnection.write(bmodel.printHelper.printDatafor3inchPrinterDiageoNG(delievery_date.getText().toString()));
                }

                alertDialog.dismiss();
                bmodel.productHelper.clearOrderTable();

                bmodel.showAlert(
                        getResources().getString(
                                R.string.printed_successfully), DataMembers.NOTIFY_ORDER_SAVED);
            }

            DemoSleeper.sleep(1500);
            if (zebraPrinterConnection instanceof BluetoothConnection) {
                String friendlyName = ((BluetoothConnection) zebraPrinterConnection)
                        .getFriendlyName();

                Commons.print(TAG + "friendlyName : " + friendlyName);
                DemoSleeper.sleep(500);
            }
        } catch (ConnectionException e) {
            Commons.printException(e + "");
        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            disconnect();
        }
    }

    private String getMacAddressFieldText() {
        String macAddress = null;
        try {
            SharedPreferences pref = getSharedPreferences("PRINT",
                    MODE_PRIVATE);
            macAddress = pref.getString("MAC", "");
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return macAddress;
    }

    private void disconnect() {
        try {
            if (zebraPrinterConnection != null) {
                zebraPrinterConnection.close();
            }
        } catch (ConnectionException e) {
            Commons.printException("" + e);
        }
    }

    private boolean isOrderedSerialNoProducts() {
        for (ProductMasterBO productBO : mOrderedProductList) {
            if (productBO.getScannedProduct() == 1) {
                return true;
            }
        }
        return false;
    }

    private void getFocusandAndMustSellOrderedProducts() {
        int focusBrandProducts = 0;
        int focusBrandProducts1 = 0;
        int focusBrandProducts2 = 0;
        int focusBrandProducts3 = 0;
        int focusBrandProducts4 = 0;
        int mustSellProducts = 0;
        double mustSellProdValues = 0;
        double focusBrandProdValues = 0;

        for (ProductMasterBO bo : mOrderedProductList) {
            if (bo.getIsFocusBrand() == 1 || bo.getIsFocusBrand2() == 1 || bo.getIsFocusBrand3() == 1 || bo.getIsFocusBrand4() == 1) {
                focusBrandProdValues += bo.getDiscount_order_value();
            }
            if (bo.getIsFocusBrand() == 1) {
                focusBrandProducts1 = 1;
            }
            if (bo.getIsFocusBrand2() == 1) {
                focusBrandProducts2 = 1;
            }
            if (bo.getIsFocusBrand3() == 1) {
                focusBrandProducts3 = 1;
            }
            if (bo.getIsFocusBrand4() == 1) {
                focusBrandProducts4 = 1;
            }


            if (bo.getIsMustSell() == 1) {
                mustSellProdValues += bo.getDiscount_order_value();
                mustSellProducts += 1;
            }
        }
        focusBrandProducts = focusBrandProducts1 + focusBrandProducts2 + focusBrandProducts3 + focusBrandProducts4;

        if (bmodel.getOrderHeaderBO() != null) {
            bmodel.getOrderHeaderBO().setOrderedFocusBrands(focusBrandProducts);
            bmodel.getOrderHeaderBO().setOrderedMustSellCount(mustSellProducts);
            bmodel.getOrderHeaderBO().setTotalMustSellValue(mustSellProdValues);
            bmodel.getOrderHeaderBO().setTotalFocusProdValues(focusBrandProdValues);
        }
    }

    private void loadFocusProduct() {
        focusProductCount = 0;
        totalFocusProductCount = 0;

        for (ProductMasterBO productBO : bmodel.productHelper.getProductMaster()) {
            if (productBO.getIsFocusBrand() == 1 || productBO.getIsFocusBrand2() == 1 || productBO.getIsFocusBrand3() == 1 || productBO.getIsFocusBrand4() == 1) {
                totalFocusProductCount++;
            }
            if (productBO.getOrderedCaseQty() > 0
                    || productBO.getOrderedPcsQty() > 0
                    || productBO.getOrderedOuterQty() > 0) {
                if (productBO.getIsFocusBrand() == 1 || productBO.getIsFocusBrand2() == 1 || productBO.getIsFocusBrand3() == 1 || productBO.getIsFocusBrand4() == 1) {
                    focusProductCount++;
                }
            }
        }
    }

    /**
     * @AUTHOR Rajesh.K
     * <p>
     * Method used to add Off invoice scheme  free product in Last ordered  product (schemeproduct object).So that
     * we can show in Print
     */
    private void updateOffInvoiceSchemeInProductOBJ() {
        ProductMasterBO productBO = mOrderedProductList.get(mOrderedProductList.size() - 1);
        if (productBO != null) {
            ArrayList<SchemeBO> offInvoiceSchemeList = bmodel.schemeDetailsMasterHelper.getmOffInvoiceAppliedSchemeList();
            if (offInvoiceSchemeList != null) {
                for (SchemeBO schemeBO : offInvoiceSchemeList) {
                    if (schemeBO.isQuantityTypeSelected()) {
                        updateSchemeFreeproduct(schemeBO, productBO);
                    }
                }
            }
        }

    }

    private void updateOrderListByEntry() {

        LinkedList<String> productIdList = bmodel.productHelper.getmProductidOrderByEntry();
        if (productIdList != null) {
            mOrderedProductList = new LinkedList<>();
            for (String productid : productIdList) {
                ProductMasterBO productBO = bmodel.productHelper.getProductMasterBOById(productid);
                if (productBO != null) {
                    if (productBO.getOrderedCaseQty() > 0 || productBO.getOrderedPcsQty() > 0 || productBO.getOrderedOuterQty() > 0) {
                        mOrderedProductList.add(productBO);
                    }
                }
            }
        }
    }

}
