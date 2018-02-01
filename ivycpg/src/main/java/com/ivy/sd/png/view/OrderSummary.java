package com.ivy.sd.png.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.text.TextUtils;
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

import com.ivy.cpg.view.order.DiscountHelper;
import com.ivy.cpg.view.order.OrderHelper;
import com.ivy.cpg.view.salesreturn.SalesReturnHelper;
import com.ivy.sd.intermecprint.BtPrint4Ivy;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.CollectionBO;
import com.ivy.sd.png.bo.OrderHeader;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.SchemeProductBO;
import com.ivy.sd.png.bo.TaxBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.MyThread;
import com.ivy.sd.png.model.ScreenReceiver;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
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
import java.util.Properties;
import java.util.Vector;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.BodyPart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class OrderSummary extends IvyBaseActivityNoActionBar implements OnClickListener,
        StorewiseDiscountDialogFragment.OnMyDialogResult, DataPickerDialogFragment.UpdateDateInterface,
        EmailDialog.onSendButtonClickListnor, OrderConfirmationDialog.OnConfirmationResult {

    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_TOAST = 5;
    private static final int DIALOG_DELIVERY_DATE_PICKER = 0;
    private static final String discountresult = "0";
    private static final String TAG = "OrderSummary";
    private static final boolean D = true;
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int MESSAGE_DEVICE_NAME = 4;
    private static final String ZEBRA_3INCH = "3";
    public static String mActivityCode;

    private static final int DIALOG_NEGATIVE_INVOICE_CHECK = 6;
    private static final int DIALOG_DELETE_ORDER = 1;
    private static final int DIALOG_ORDER_SAVED_WITH_PRINT_OPTION = 2;
    private static final int DIALOG_ORDER_SAVED = 3;
    private static final int DIALOG_NUMBER_OF_PRINTS =10;
    private static final int DIALOG_INVOICE_SAVED =9;

    private Toolbar toolbar;
    private Button btnsave;
    private Button btnsaveAndGoInvoice;
    private TextView lpc;
    private TextView totalval;
    private TextView totalQtyTV;
    private Button delievery_date;
    private ExpandableListView mExpListView;
    private ImageView icAmountSpilitup;

    private DiscountDialog initiativedialog;
    private AlertDialog.Builder build;
    private AlertDialog alertDialog;
    private AmountSplitupDialog dialogFragment;
    private OrderConfirmationDialog orderConfirmationDialog;
    private ReturnProductDialog returnProductDialog;
    private CollectionBeforeInvoiceDialog collectionBeforeInvoiceDialog;
    private StorewiseDiscountDialogFragment mStoreWiseDiscountDialogFragment;
    private BusinessModel bmodel;
    private CollectionBO collectionbo;
    private DiscountHelper discountHelper;
    private OrderHelper orderHelper;

    private LinkedList<ProductMasterBO> mOrderedProductList;
    private Vector<ProductMasterBO> mSortedList;

    private String sendMailAndLoadClass;
    private boolean fromorder = false;
    private boolean isPrintMenuClicked = false;
    private double enteredDiscAmtOrPercent = 0;

    private double totalOrderValue, cmyDiscount, distDiscount;
    private boolean isClick = false;
    private boolean isDiscountDialog;
    private double entryLevelDiscount = 0.0;

    private double totalSchemeDiscValue;
    private int mSelectedPrintCount = 0;

    private boolean isClicked;
    private String screenCode = "MENU_STK_ORD";
    private String PHOTO_PATH = "";
    private SharedPreferences msettings;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BtService mChatService = null;
    private Connection zebraPrinterConnection;
    private ZebraPrinter printer;
    private AlertDialog.Builder builder10;
    private BroadcastReceiver mReceiver;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.ordersummary);

        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

        /** Close the screen if userid becomes 0 **/
        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        Bundle extras = getIntent().getExtras();
        if (savedInstanceState == null) {
            if (extras != null && extras.getString("ScreenCode") != null) {
                screenCode = extras.getString("ScreenCode");
            }
        }

        setDiscountDialog(false);


        // Setting screen title
        if (bmodel.mSelectedModule == 1 || bmodel.mSelectedModule == 2) {
            bmodel.configurationMasterHelper
                    .downloadNewActivityMenu(ConfigurationMasterHelper.MENU_ACTIVITY);
        }

        String screentitle = bmodel.configurationMasterHelper
                .getHomescreentwomenutitle("MENU_CLOSING");
        if ("MENU_CLOSING".equals(screentitle))
            screentitle = getResources().getString(R.string.summary);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if(getSupportActionBar()!=null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
                setScreenTitle(screentitle);
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
        }
        bmodel.saveModuleCompletion("MENU_CLOSING");


        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        discountHelper = DiscountHelper.getInstance(this);


        PHOTO_PATH = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + DataMembers.photoFolderName;

        // Focus product alert
        if (bmodel.configurationMasterHelper.SHOW_ORDER_FOCUS_COUNT) {
            showFocusProductAlert();

        }

        /** Initialize the views **/
        initializeViews();
        hideAndSeek();
        updateLabels();

        setDeliveryDate();


        msettings = getSharedPreferences(BusinessModel.PREFS_NAME, MODE_PRIVATE);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter);




        if (bmodel.getOrderHeaderBO() == null) {
            bmodel.setOrderHeaderBO(new OrderHeader());
        }


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


    }

    private void initializeViews() {
        lpc = (TextView) findViewById(R.id.lcp);
        totalval = (TextView) findViewById(R.id.totalValue);
        delievery_date = (Button) findViewById(R.id.deliveryDate);
        btnsave = (Button) findViewById(R.id.orderSummarySave);
        mExpListView = (ExpandableListView) findViewById(R.id.elv);
        btnsaveAndGoInvoice = (Button) findViewById(R.id.saveAndGoInvoice);
        totalQtyTV = (TextView) findViewById(R.id.tv_totalqty);
        icAmountSpilitup = (ImageView) findViewById(R.id.icAmountSpilitup);

        //typefaces
        ((TextView) findViewById(R.id.tv_deliveryDate)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) findViewById(R.id.lpcLabel)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) findViewById(R.id.totalValuelbl)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) findViewById(R.id.title_totalqty)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        delievery_date.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        lpc.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        totalval.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        totalQtyTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        btnsave.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        btnsaveAndGoInvoice.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        delievery_date.setOnClickListener(this);
        btnsave.setOnClickListener(this);
        btnsaveAndGoInvoice.setOnClickListener(this);
        icAmountSpilitup.setOnClickListener(this);
    }

    private void updateLabels() {

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

        isPrintMenuClicked = false;
        isClicked = false;

        /** session out if userid becomes 0 **/
        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        prepareScreenData();
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
            }
            if (!bmodel.configurationMasterHelper.IS_INVOICE)
                btnsaveAndGoInvoice.setVisibility(View.GONE);
            if (bmodel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG) { // if
                if (bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
                    findViewById(R.id.layoutDelivery).setVisibility(View.GONE);
                }
            } else if (bmodel.configurationMasterHelper.IS_INVOICE || !bmodel.configurationMasterHelper.SHOW_DELIVERY_DATE) {
                findViewById(R.id.layoutDelivery).setVisibility(View.GONE);
            }

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


            if (bmodel.configurationMasterHelper.IS_SHOW_DISCOUNTS_ORDER_SUMMARY) {
                icAmountSpilitup.setVisibility(View.VISIBLE);

            } else {
                icAmountSpilitup.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    private void setDeliveryDate() {
        try {
            if (bmodel.isEdit()) {

                delievery_date.setText(DateUtil.convertFromServerDateToRequestedFormat(
                        bmodel.getDeliveryDate(bmodel.getRetailerMasterBO()
                                .getRetailerID()),
                        ConfigurationMasterHelper.outDateFormat));
            } else {
                Calendar origDay = Calendar.getInstance();
                origDay.add(Calendar.DAY_OF_YEAR, (bmodel.configurationMasterHelper.DEFAULT_NUMBER_OF_DAYS_TO_DELIVER_ORDER == 0 ? 1 : bmodel.configurationMasterHelper.DEFAULT_NUMBER_OF_DAYS_TO_DELIVER_ORDER));

                delievery_date.setText(DateUtil.convertDateObjectToRequestedFormat(origDay.getTime(),ConfigurationMasterHelper.outDateFormat));

            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }


    private void prepareScreenData() {

        int totalQuantityOrdered = 0;
        float totalWeight = 0;

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


        for (int i = 0; i < productsCount; i++) {
            productBO = productList.elementAt(i);

            if (productBO.getOrderedCaseQty() > 0
                    || productBO.getOrderedPcsQty() > 0
                    || productBO.getOrderedOuterQty() > 0) {

                int totalQuantity = productBO.getOrderedPcsQty() + productBO.getOrderedCaseQty() * productBO.getCaseSize() + productBO.getOrderedOuterQty() * productBO.getOutersize();

                totalQuantityOrdered = totalQuantityOrdered + totalQuantity;
                totalWeight = totalWeight + (totalQuantity * productBO.getWeight());

                mOrderedProductList.add(productBO);

                double lineValue;

                if (bmodel.configurationMasterHelper.IS_SIH_VALIDATION
                        && bmodel.configurationMasterHelper.IS_INVOICE
                        && bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION) {
                    if (productBO.getBatchwiseProductCount() > 0) {
                        // Apply batch wise price apply
                        lineValue = bmodel.schemeDetailsMasterHelper
                                .getbatchWiseTotalValue(productBO);
                    } else {
                        lineValue = (productBO.getOrderedCaseQty() * productBO
                                .getCsrp())
                                + (productBO.getOrderedPcsQty() * productBO
                                .getSrp())
                                + (productBO.getOrderedOuterQty() * productBO
                                .getOsrp());
                    }
                } else {
                    lineValue = (productBO.getOrderedCaseQty() * productBO
                            .getCsrp())
                            + (productBO.getOrderedPcsQty() * productBO
                            .getSrp())
                            + (productBO.getOrderedOuterQty() * productBO
                            .getOsrp());
                }

                /** Set the calculated values in productBO **/
                productBO.setDiscount_order_value(lineValue);
                productBO.setSchemeAppliedValue(lineValue);
                productBO.setOrderPricePiece(productBO.getSrp());

                productBO.setCompanyTypeDiscount(0);
                productBO.setDistributorTypeDiscount(0);
                // clear scheme free products stored in product obj
                productBO.setSchemeProducts(new ArrayList<SchemeProductBO>());

                totalOrderValue += lineValue;

                Commons.print("line value" + lineValue);
            }
        }



        if (bmodel.configurationMasterHelper.IS_SHOW_ORDERING_SEQUENCE) {
            mSortedList = new Vector<>();
            updateOrderListByEntry();
            mSortedList.addAll(mOrderedProductList);
        }



        if (bmodel.getOrderHeaderBO() != null)
            bmodel.getOrderHeaderBO().setTotalWeight(totalWeight);

        // Empties Management is Enabled, then we have to add totalOrderValue with remaining value(Order return value).
        if (bmodel.configurationMasterHelper.SHOW_PRODUCTRETURN
                && !bmodel.configurationMasterHelper.SHOW_BOTTLE_CREDITLIMIT
                && bmodel.configurationMasterHelper.IS_SIH_VALIDATION && bmodel.getOrderHeaderBO() != null) {
            totalOrderValue = totalOrderValue
                    + bmodel.getOrderHeaderBO().getRemainigValue();
        }

        discountHelper.clearProductDiscountAndTaxValue(mOrderedProductList);


        // Scheme calculations
        if (!bmodel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG
                || bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
            totalSchemeDiscValue = discountHelper.calculateSchemeDiscounts(mOrderedProductList);
           totalOrderValue -=totalSchemeDiscValue;
        }

        if (bmodel.configurationMasterHelper.IS_REMOVE_TAX_ON_SRP) {
            //applying removed tax..
            applyRemovedTax();
        }


        //  Apply product entry level discount
        if (bmodel.configurationMasterHelper.IS_ENTRY_LEVEL_DISCOUNT) {
            entryLevelDiscount = discountHelper.calculateEntryLevelDiscount(mOrderedProductList);
            totalOrderValue = totalOrderValue - entryLevelDiscount;
        }

        // Apply Item  level discount
        if (bmodel.configurationMasterHelper.SHOW_DISCOUNT) {
            double itemLevelDiscount = discountHelper.calculateItemLevelDiscount();
            totalOrderValue = totalOrderValue - itemLevelDiscount;
        }

        // Apply Exclude Item level Tax  in Product
        if (bmodel.configurationMasterHelper.SHOW_TAX) {
            bmodel.productHelper.taxHelper.updateProductWiseTax();
        }




        mExpListView.setAdapter(new ProductExpandableAdapter());
        for (int i = 0; i < mOrderedProductList.size(); i++) {
            mExpListView.expandGroup(i);
        }


        if (bmodel.configurationMasterHelper.SHOW_STORE_WISE_DISCOUNT_DLG && bmodel.configurationMasterHelper.BILL_WISE_DISCOUNT == 0) {
            //find the  range of discount by using totalvalue
            final double billwiseRangeDiscount = discountHelper.updateBillwiseRangeDiscount(totalOrderValue);
            totalOrderValue = totalOrderValue - billwiseRangeDiscount;

        } else if (bmodel.configurationMasterHelper.SHOW_STORE_WISE_DISCOUNT_DLG && bmodel.configurationMasterHelper.BILL_WISE_DISCOUNT == 1) {
            // Automatically apply bill wise discount
            final double billWiseDiscount = discountHelper.updateBillwiseDiscount(totalOrderValue);
            if (bmodel.getOrderHeaderBO() != null) {
                bmodel.getOrderHeaderBO().setDiscountValue(billWiseDiscount);
            }
            totalOrderValue = totalOrderValue - billWiseDiscount;

        } else {
            // user manually enter bill wise discount
            double discnt = bmodel.orderAndInvoiceHelper.restoreDiscountAmount(bmodel
                    .getRetailerMasterBO().getRetailerID());
            double billWiseDiscount = applyDiscountMaxValidation(discnt);
            totalOrderValue = totalOrderValue - billWiseDiscount;
        }

        // Apply bill wise payterm discount
        final double billWisePayTermDiscount = discountHelper.calculateBillWisePayTermDiscount(totalOrderValue);
        totalOrderValue = totalOrderValue - billWisePayTermDiscount;

        // To open the dialog back while resuming
        if (!isDiscountDialog() && bmodel.configurationMasterHelper.IS_ENTRY_LEVEL_DISCOUNT && initiativedialog != null && initiativedialog.isShowing()) {
            setDiscountDialog(true);
            initiativedialog.dismiss();
            initiativedialog = null;
            initiativedialog = new DiscountDialog(OrderSummary.this, null,
                    discountDismissListener);
            initiativedialog.show();
        }

        //updating footer labels
        totalval.setText(bmodel.formatValue(totalOrderValue));
        lpc.setText(mOrderedProductList.size());
        totalQtyTV.setText(String.valueOf(totalQuantityOrdered));

    }

    private void callAmountSplitUpScreen(){

        double cmy_disc = 0, dist_disc = 0;
        for (ProductMasterBO productMasterBO : mOrderedProductList) {
            cmy_disc = cmy_disc + productMasterBO.getCompanyTypeDiscount();
            dist_disc = dist_disc + productMasterBO.getDistributorTypeDiscount();
        }
        cmyDiscount = cmy_disc + bmodel.getRetailerMasterBO().getBillWiseCompanyDiscount();
        distDiscount = dist_disc + bmodel.getRetailerMasterBO().getBillWiseDistributorDiscount();

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
            args.putDouble("scheme_disc", totalSchemeDiscValue);
            dialogFragment.setArguments(args);
            dialogFragment.show(getSupportFragmentManager(), "AmtSplitupDialog");
        }

    }


    /**
     * Project specific: Tax should be removed for scheme calculation.
     * So, the removed tax is applied back to it after scheme calculation finished.
     */
    private void applyRemovedTax() {
        for (ProductMasterBO bo : mOrderedProductList) {
            float finalAmount = 0;

            if (bmodel.productHelper.taxHelper.getmTaxListByProductId() != null) {
                if (bmodel.productHelper.taxHelper.getmTaxListByProductId().get(bo.getProductID()) != null) {
                    for (TaxBO taxBO : bmodel.productHelper.taxHelper.getmTaxListByProductId().get(bo.getProductID())) {
                        if (taxBO.getParentType().equals("0")) {
                            finalAmount += SDUtil.truncateDecimal(bo.getDiscount_order_value() * (taxBO.getTaxRate() / 100), 2).floatValue();
                        }
                    }
                }
            }

            bo.setDiscount_order_value((bo.getDiscount_order_value() + finalAmount));
        }
    }



    @Override
    public void onDiscountDismiss(String result, int result1, int result2, int result3) {
        if (bmodel.configurationMasterHelper.SHOW_STORE_WISE_DISCOUNT_DLG && bmodel.configurationMasterHelper.BILL_WISE_DISCOUNT == 0) {

            final double totalValue = discountHelper.updateBillwiseRangeDiscount(totalOrderValue);
            totalval.setText(bmodel.formatValue(totalValue));

        } else if (bmodel.configurationMasterHelper.SHOW_TOTAL_DISCOUNT_EDITTEXT) {
            try {
                int f1 = 0;
                String qty = result;
                if ("".equals(qty)) {
                    qty = "0";
                }

                enteredDiscAmtOrPercent = SDUtil.convertToDouble(qty);

                if (enteredDiscAmtOrPercent != 0 && bmodel.configurationMasterHelper.discountType == 1 && enteredDiscAmtOrPercent > 100) {
                    f1 = (int) (enteredDiscAmtOrPercent / 10);
                }

                String strDiscountAppliedvalue = bmodel.formatValue(getDiscountAppliedValue(SDUtil.convertToDouble(f1 + "")));

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
    public void updateDate(Date date, String tag) {

        AdvancePaymentDialogFragment paymentDialogFragment = (AdvancePaymentDialogFragment) getSupportFragmentManager().findFragmentByTag("Advance Payment");
        paymentDialogFragment.updateDate(date, "");

    }

    @Override
    public void setEmailAddress(String value) {
        new SendMail(this, "Read", "Test", value).execute();

    }

    @Override
    protected Dialog onCreateDialog(int id) {
        String delivery_date_txt = "";
        switch (id) {
            case DIALOG_DELIVERY_DATE_PICKER: {

                Calendar c = Calendar.getInstance();
                c.add(Calendar.DAY_OF_YEAR, (bmodel.configurationMasterHelper.DEFAULT_NUMBER_OF_DAYS_TO_DELIVER_ORDER == 0 ? 1 : bmodel.configurationMasterHelper.DEFAULT_NUMBER_OF_DAYS_TO_DELIVER_ORDER));

                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                MyDatePickerDialog dialog = new MyDatePickerDialog(this,
                        mDeliverDatePickerListener, year, month, day);
                dialog.setPermanentTitle(getResources().getString(R.string.choose_date));
                dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

                if (bmodel.configurationMasterHelper.MAX_NUMBER_OF_DAYS_ALLOWED_TO_DELIVER > 0) {
                    Calendar maxCalendar = Calendar.getInstance();
                    maxCalendar.add(Calendar.DAY_OF_YEAR, bmodel.configurationMasterHelper.MAX_NUMBER_OF_DAYS_ALLOWED_TO_DELIVER);
                    dialog.getDatePicker().setMaxDate(maxCalendar.getTimeInMillis());
                }



                return dialog;
            }

            case DIALOG_DELETE_ORDER: {

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
                                        customProgressDialog(build, getResources().getString(R.string.deleting_order));
                                        alertDialog = build.create();
                                        alertDialog.show();

                                        bmodel.getOrderHeaderBO().setIsSignCaptured(false);
                                        if (bmodel.getOrderHeaderBO().getSignatureName() != null)
                                            bmodel.synchronizationHelper.deleteFiles(
                                                    PHOTO_PATH, bmodel.getOrderHeaderBO().getSignatureName());

                                        // clear scheme free products
                                        discountHelper.clearSchemeFreeProduct(mOrderedProductList);

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
                                        customProgressDialog(build, getResources().getString(R.string.deleting_order));
                                        alertDialog = build.create();
                                        alertDialog.show();

                                        // clear scheme free products
                                        bmodel.getOrderHeaderBO().setIsSignCaptured(false);
                                        if (bmodel.getOrderHeaderBO().getSignatureName() != null)
                                            bmodel.synchronizationHelper.deleteFiles(
                                                    PHOTO_PATH, bmodel.getOrderHeaderBO().getSignatureName());
                                        discountHelper.clearSchemeFreeProduct(mOrderedProductList);

                                        new deleteStockAndOrder().execute();

                                        new MyThread(OrderSummary.this,
                                                DataMembers.DELETE_ORDER).start();
                                    }
                                });
                bmodel.applyAlertDialogTheme(builder);
                break;
            }



            case DIALOG_ORDER_SAVED: {

                delivery_date_txt = delievery_date.getText().toString();
                if (bmodel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG) {
                    if (bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
                        delivery_date_txt = "";
                    }
                } else if (bmodel.configurationMasterHelper.IS_INVOICE || !bmodel.configurationMasterHelper.SHOW_DELIVERY_DATE) {
                    delivery_date_txt = "";
                }

                AlertDialog.Builder builder2 = new AlertDialog.Builder(OrderSummary.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(getResources().getString(R.string.order_saved_locally_order_id_is) + orderHelper.getOrderid())

                        .setPositiveButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {

                                            bmodel.outletTimeStampHelper
                                                    .updateTimeStampModuleWise(SDUtil
                                                            .now(SDUtil.TIME));
                                        bmodel.productHelper.clearOrderTable();

                                            SalesReturnHelper salesReturnHelper = SalesReturnHelper.getInstance(OrderSummary.this);
                                            final List<ProductMasterBO> orderListWithReplace = salesReturnHelper.updateReplaceQtyWithOutTakingOrder(mOrderedProductList);
                                            Vector<ProductMasterBO> orderList = new Vector<>(orderListWithReplace);
                                            bmodel.mCommonPrintHelper.xmlRead("order", false, orderList, null);

                                            bmodel.writeToFile(String.valueOf(bmodel.mCommonPrintHelper.getInvoiceData()),
                                                    StandardListMasterConstants.PRINT_FILE_ORDER + orderHelper.getOrderid(), "/" + DataMembers.IVYDIST_PATH);

                                            sendMailAndLoadClass = "HomeScreenTwoPRINT_FILE_ORDER";
                                            if (bmodel.configurationMasterHelper.IS_ORDER_SUMMERY_EXPORT_AND_EMAIL) {
                                                new ShowEmailDialog().execute();
                                                //isClick = false;
                                                //return;
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
            }

            case DIALOG_ORDER_SAVED_WITH_PRINT_OPTION: {

                delivery_date_txt = delievery_date.getText().toString();
                if (bmodel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG) {
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
                                        + orderHelper.getOrderid())
                        .setNegativeButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

                                        bmodel.outletTimeStampHelper
                                                .updateTimeStampModuleWise(SDUtil
                                                        .now(SDUtil.TIME));

                                        // clear scheme free products
                                        discountHelper.clearSchemeFreeProduct(mOrderedProductList);

                                        Intent i = new Intent(OrderSummary.this,
                                                HomeScreenTwo.class);
                                        Bundle extras = getIntent().getExtras();
                                        if (extras != null) {
                                            i.putExtra("IsMoveNextActivity", bmodel.configurationMasterHelper.MOVE_NEXT_ACTIVITY);
                                            i.putExtra("CurrentActivityCode", mActivityCode);
                                        }
                                        startActivity(i);   finish();
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
                                        if (bmodel.configurationMasterHelper.SHOW_ZEBRA_TITAN || bmodel.configurationMasterHelper.SHOW_ZEBRA_UNIPAL) {
                                            showDialog(DIALOG_NUMBER_OF_PRINTS);
                                        }
                                        else {
                                            printOrder();
                                        }
                                    }
                                });

                if (!delivery_date_txt.equals("")) {
                    builder1.setMessage(getResources().getString(R.string.delivery_date_is) + " " + delivery_date_txt);
                }


                bmodel.applyAlertDialogTheme(builder1);
                break;
            }

            case DIALOG_INVOICE_SAVED:
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
                                            showDialog(DIALOG_NUMBER_OF_PRINTS);
                                        } else {
                                           printOrder();
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

            case DIALOG_NUMBER_OF_PRINTS: {

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
                                        printOrder();
                                    }
                                }).start();

                                builder10 = new AlertDialog.Builder(OrderSummary.this);

                                customProgressDialog(builder10, "Printing....");
                                alertDialog = builder10.create();
                                alertDialog.show();
                            }
                        });
                bmodel.applyAlertDialogTheme(builder11);

                break;

            }


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
                                        if (bmodel.getOrderHeaderBO().getSignatureName() != null)
                                            bmodel.synchronizationHelper.deleteFiles(
                                                    PHOTO_PATH, bmodel.getOrderHeaderBO().getSignatureName());

                                        customProgressDialog(build, getResources().getString(R.string.deleting_order));
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

            case DIALOG_NEGATIVE_INVOICE_CHECK:
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
                                        if (bmodel.getOrderHeaderBO().getSignatureName() != null)
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



            default:
                break;
        }
        return null;
    }

    private void printOrder(){

        Intent i;
        if (bmodel.configurationMasterHelper.SHOW_ZEBRA_TITAN||bmodel.configurationMasterHelper.SHOW_ZEBRA_UNIPAL) {
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

            customProgressDialog(builder10, "Printing....");
            alertDialog = builder10.create();
            alertDialog.show();
        } else if (bmodel.configurationMasterHelper.SHOW_BIXOLONII) {
            i = new Intent(OrderSummary.this,
                    BixolonIIPrint.class);
            i.putExtra("IsFromOrder", true);
            startActivity(i);
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            finish();
        } else if (bmodel.configurationMasterHelper.SHOW_BIXOLONI) {
            i = new Intent(OrderSummary.this,
                    BixolonIPrint.class);
            i.putExtra("IsFromOrder", true);
            startActivity(i);
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            finish();
        } else if (bmodel.configurationMasterHelper.SHOW_ZEBRA) {
            i = new Intent(OrderSummary.this,
                    InvoicePrintZebraNew.class);
            i.putExtra("IsFromOrder", true);
            startActivity(i);
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            finish();
        } else if (bmodel.configurationMasterHelper.SHOW_ZEBRA_ATS) {
            i = new Intent(OrderSummary.this,
                    PrintPreviewScreen.class);
            i.putExtra("IsFromOrder", true);
            i.putExtra("storediscount",
                    discountresult);

            startActivity(i);
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            finish();
        } else if (bmodel.configurationMasterHelper.SHOW_INTERMEC_ATS) {
            i = new Intent(OrderSummary.this,
                    BtPrint4Ivy.class);
            i.putExtra("IsFromOrder", true);
            i.putExtra("storediscount",
                    discountresult);
            startActivity(i);
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            finish();
        } else if (bmodel.configurationMasterHelper.SHOW_ZEBRA_DIAGEO) {
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
                || bmodel.configurationMasterHelper.COMMON_PRINT_ZEBRA || bmodel.configurationMasterHelper.COMMON_PRINT_SCRYBE || bmodel.configurationMasterHelper.COMMON_PRINT_LOGON) {

            if ("1".equalsIgnoreCase(bmodel.retailerMasterBO.getRField4()))
                bmodel.productHelper.updateDistributorDetails();

            SalesReturnHelper salesReturnHelper = SalesReturnHelper.getInstance(OrderSummary.this);

            final List<ProductMasterBO> orderListWithReplace = salesReturnHelper.updateReplaceQtyWithOutTakingOrder(mOrderedProductList);
            Vector<ProductMasterBO> orderList = new Vector<>(orderListWithReplace);

            bmodel.mCommonPrintHelper.xmlRead("order", false, orderList, null);

            if (bmodel.configurationMasterHelper.IS_PRINT_FILE_SAVE) {
                bmodel.writeToFile(String.valueOf(bmodel.mCommonPrintHelper.getInvoiceData()),
                        StandardListMasterConstants.PRINT_FILE_ORDER + bmodel.invoiceNumber, "/" + DataMembers.IVYDIST_PATH);
                sendMailAndLoadClass = "CommonPrintPreviewActivityPRINT_FILE_ORDER";

                if (bmodel.configurationMasterHelper.IS_ORDER_SUMMERY_EXPORT_AND_EMAIL) {
                    new ShowEmailDialog().execute();
                    //isClick = false;
                    //return;
                } else {
                    i = new Intent(OrderSummary.this,
                            CommonPrintPreviewActivity.class);
                    i.putExtra("IsFromOrder", true);
                    i.putExtra("IsUpdatePrintCount", true);
                    i.putExtra("isHomeBtnEnable", true);
                    startActivity(i);
                    overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                    finish();
                }
            } else {
                i = new Intent(OrderSummary.this,
                        CommonPrintPreviewActivity.class);
                i.putExtra("IsFromOrder", true);
                i.putExtra("IsUpdatePrintCount", true);
                i.putExtra("isHomeBtnEnable", true);
                startActivity(i);
                overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                finish();
            }

        } else {
            i = new Intent(OrderSummary.this,
                    BixolonIIPrint.class);
            i.putExtra("IsFromOrder", true);
            startActivity(i);
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            finish();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.order_summary_menu, menu);

        /** on/off the items based on the configuration **/
        MenuItem reviewAndPo = menu.findItem(R.id.menu_review);
        reviewAndPo
                .setVisible(bmodel.configurationMasterHelper.SHOW_REVIEW_AND_PO);

        MenuItem discount = menu.findItem(R.id.menu_discount);
        discount.setVisible(bmodel.configurationMasterHelper.IS_ENTRY_LEVEL_DISCOUNT);

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
                    OrderSummary.this, null, false);
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
        }  else if (i1 == R.id.menu_store_wise_discount) {
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
            bmodel.configurationMasterHelper.loadOrderSummaryDetailConfig();
            FragmentManager fm = getSupportFragmentManager();
            OrderSummaryDialogFragment dialogFragment = new OrderSummaryDialogFragment();
            Bundle bundle = new Bundle();

            bundle.putSerializable("OrderList", (Serializable) mOrderedProductList);
            dialogFragment.setArguments(bundle);
            dialogFragment.show(fm, "Dialog Fragment");
            dialogFragment.setCancelable(false);
        } else if (i1 == R.id.menu_serialno) {
            if (isOrderedSerialNoProducts()) {
                Intent i = new Intent(OrderSummary.this, SerialNoEntryScreen.class);
                startActivity(i);
                overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            } else {
                Toast.makeText(OrderSummary.this, "No Scanned products ", Toast.LENGTH_SHORT).show();
            }
        } else if (i1 == R.id.menu_edit) {
            discountHelper.clearSchemeFreeProduct(mOrderedProductList);
            if (bmodel.configurationMasterHelper.IS_ENTRY_LEVEL_DISCOUNT)
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
                showDialog(DIALOG_DELETE_ORDER);
            else
                showDialog(5);
        }
        return super.onOptionsItemSelected(item);
    }

    public void onClick(View viewClicked) {

        Button vw = (Button) viewClicked;
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        if (vw == delievery_date)
            showDialog(DIALOG_DELIVERY_DATE_PICKER);
        else if (vw == btnsave) {
            saveOrder();
        } else if (vw == btnsaveAndGoInvoice) {

            saveInvoice();
        }
        else if(viewClicked==icAmountSpilitup){
            callAmountSplitUpScreen();
        }

    }

    private void saveOrder(){

        IndicativeOrderReasonDialog indicativeReasonDialog;
        fromorder = true;

        if (bmodel.configurationMasterHelper.IS_SHOW_ORDERING_SEQUENCE && mSortedList != null)
            orderHelper.setSortedOrderedProducts(mSortedList);


        if (!isClick) {

            isClick = true;

            if (bmodel.configurationMasterHelper.IS_TEMP_ORDER_SAVE && screenCode.equals(HomeScreenTwo.MENU_CATALOG_ORDER))
                bmodel.orderTimer.cancel();

            if (mOrderedProductList.size() > 0) {

                if ((bmodel.configurationMasterHelper.IS_GST || bmodel.configurationMasterHelper.IS_GST_HSN) && !orderHelper.isTaxAvailableForAllOrderedProduct(mOrderedProductList)) {
                    // If GST enabled then, every ordered product should have tax
                    bmodel.showAlert(
                            getResources()
                                    .getString(
                                            R.string.tax_not_availble_for_some_product),
                            0);
                    isClick = false;
                    return;
                }

                if ((bmodel.configurationMasterHelper.IS_SHOW_ONLY_INDICATIVE_ORDER || bmodel.configurationMasterHelper.IS_SHOW_ORDER_REASON) && !orderHelper.isReasonProvided(mOrderedProductList)) {

                    indicativeReasonDialog = new IndicativeOrderReasonDialog(this, bmodel);
                    indicativeReasonDialog.show();
                    isClick = false;

                } else {

                    bmodel.getOrderHeaderBO().setOrderValue(getDiscountAppliedValue(enteredDiscAmtOrPercent));
                    bmodel.getOrderHeaderBO().setDiscount(enteredDiscAmtOrPercent);
                    bmodel.getOrderHeaderBO().setDiscountId(0);
                    bmodel.getOrderHeaderBO().setIsCompanyGiven(0);
                    bmodel.getOrderHeaderBO().setLinesPerCall(SDUtil.convertToInt((String) lpc.getText()));
                    bmodel.getOrderHeaderBO().setDeliveryDate(DateUtil.convertToServerDateFormat(delievery_date.getText().toString(),
                            ConfigurationMasterHelper.outDateFormat));


                    orderConfirmationDialog = new OrderConfirmationDialog(this, false, mOrderedProductList, totalOrderValue);
                    orderConfirmationDialog.show();
                    orderConfirmationDialog.setCancelable(false);

                    return;
                }

            } else {
                Toast.makeText(
                        this,
                        getResources().getString(
                                R.string.no_products_exists),
                        Toast.LENGTH_SHORT);
            }
        }
    }

    private void saveInvoice(){

        fromorder = false;
        IndicativeOrderReasonDialog indicativeReasonDialog;

        if (bmodel.configurationMasterHelper.IS_SHOW_ORDERING_SEQUENCE && mSortedList != null)
            orderHelper.setSortedOrderedProducts(mSortedList);


        if ((bmodel.configurationMasterHelper.IS_GST || bmodel.configurationMasterHelper.IS_GST_HSN) && !orderHelper.isTaxAvailableForAllOrderedProduct(mOrderedProductList)) {
            // If GST enabled then, every ordered product should have tax
            bmodel.showAlert(
                    getResources()
                            .getString(
                                    R.string.tax_not_availble_for_some_product),
                    0);
            isClick = false;
            return;
        }

        if (!isClick) {
            isClick = true;

            if (bmodel.configurationMasterHelper.IS_SIH_VALIDATION && !orderHelper.isStockAvailableToDeliver(mOrderedProductList)) {
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
                    showDialog(DIALOG_NEGATIVE_INVOICE_CHECK);
                    return;
                }
            }

            if (bmodel.configurationMasterHelper.IS_TAX_APPLIED_VALIDATION) {
                if (!orderHelper.isTaxAppliedForAnyProduct(mOrderedProductList)) {
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

                pendingAmount = bmodel.getRetailerMasterBO().getCreditLimit()- bmodel.collectionHelper.calculatePendingOSTAmount();

                if (collectionbo.getCashamt() > 0
                        || collectionbo.getChequeamt() > 0 || collectionbo.getCreditamt() > 0) {
                    collectedAmount = collectionbo.getCashamt()
                            + collectionbo.getChequeamt() + collectionbo.getCreditamt();
                }

                collectedAmount = Double.parseDouble(bmodel.formatValue(collectedAmount));
                pendingAmount = collectedAmount + pendingAmount;
                pendingAmount = Double.parseDouble(bmodel.formatValue(pendingAmount));

                if (totalOrderValue > pendingAmount) {
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


            bmodel.getOrderHeaderBO().setOrderValue(getDiscountAppliedValue(enteredDiscAmtOrPercent));
            bmodel.getOrderHeaderBO().setDiscount(enteredDiscAmtOrPercent);
            bmodel.getOrderHeaderBO().setDiscountId(0);
            bmodel.getOrderHeaderBO().setIsCompanyGiven(0);
            bmodel.getOrderHeaderBO().setLinesPerCall(SDUtil.convertToInt((String) lpc.getText()));
            bmodel.getOrderHeaderBO().setDeliveryDate(DateUtil.convertToServerDateFormat(delievery_date.getText().toString(),ConfigurationMasterHelper.outDateFormat));

            if (!mOrderedProductList.isEmpty()) {

                if (orderHelper.isAllScanned() || !bmodel.configurationMasterHelper.SHOW_SERIAL_NO_SCREEN) {

                    if (orderHelper.hasOrder(mOrderedProductList)) {

                        if ((bmodel.configurationMasterHelper.IS_SHOW_ONLY_INDICATIVE_ORDER || bmodel.configurationMasterHelper.IS_SHOW_ORDER_REASON) && !orderHelper.isReasonProvided(mOrderedProductList)) {

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

                            orderHelper.invoiceDisount = Double.toString(enteredDiscAmtOrPercent);

                            if (bmodel.configurationMasterHelper.IS_INVOICE) {
                                build = new AlertDialog.Builder(OrderSummary.this);

                                customProgressDialog(build, getResources().getString(R.string.saving_invoice));
                                alertDialog = build.create();
                                alertDialog.show();

                                orderConfirmationDialog = new OrderConfirmationDialog(this, true, mOrderedProductList, totalOrderValue);
                                orderConfirmationDialog.show();
                                orderConfirmationDialog.setCancelable(false);
                                return;
                            } else {
                                build = new AlertDialog.Builder(OrderSummary.this);

                                customProgressDialog(build, getResources().getString(R.string.saving_new_order));
                                alertDialog = build.create();
                                alertDialog.show();
                            }
                            if (bmodel.configurationMasterHelper.IS_FOCUSBRAND_COUNT_IN_REPORT || bmodel.configurationMasterHelper.IS_MUSTSELL_COUNT_IN_REPORT)
                                getFocusandAndMustSellOrderedProducts();


                            //Adding accumulation scheme free products to the last ordered product list, so that it will listed on print
                            orderHelper.updateOffInvoiceSchemeInProductOBJ(mOrderedProductList);


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





    public Handler getHandler() {
        return handler;
    }

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


    private double getDiscountAppliedValue(double discnt) {
        double total;
        total = totalOrderValue;

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


    private double applyDiscountMaxValidation(double discount){
        try {
            if (bmodel.configurationMasterHelper.discountType == 1) {
                if (discount > 100)
                    discount = 100;

            }
        } catch (Exception e) {
            Commons.printException(e);
        }

        return discount;
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
                    bmodel.printHelper.setPrintCnt(0);
                    for (int i = 0; i < mSelectedPrintCount + 1; i++) {
                        zebraPrinterConnection.write(bmodel.printHelper.printDatafor3inchprinterForUnipal(mOrderedProductList, fromorder, 1));
                        if (!fromorder) {
                            bmodel.updatePrintCount(1);
                            bmodel.printHelper.setPrintCnt(orderHelper.getPrintCount(this));
                        }
                    }
                } else if (bmodel.configurationMasterHelper.SHOW_ZEBRA_TITAN) {
                    for (int i = 0; i < mSelectedPrintCount + 1; i++) {
                        zebraPrinterConnection.write(bmodel.printHelper.printDataforTitan3inchOrderprinter(mOrderedProductList, 0));
                        if (!fromorder) {
                            bmodel.updatePrintCount(1);
                            bmodel.printHelper.setPrintCnt(orderHelper.getPrintCount(this));
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

    private void showFocusProductAlert() {
        int focusProductCount = 0;
        int totalFocusProductCount = 0;

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

    @Override
    public void save(boolean isInvoice) {
        try {
            if (orderConfirmationDialog != null)
                orderConfirmationDialog.dismiss();

            if (isInvoice) {

                if (bmodel.configurationMasterHelper.IS_INVOICE) {
                    build = new AlertDialog.Builder(OrderSummary.this);

                    customProgressDialog(build, getResources().getString(R.string.saving_invoice));
                    alertDialog = build.create();
                    alertDialog.show();
                } else {
                    build = new AlertDialog.Builder(OrderSummary.this);

                    customProgressDialog(build, getResources().getString(R.string.saving_new_order));
                    alertDialog = build.create();
                    alertDialog.show();
                }
                if (bmodel.configurationMasterHelper.IS_FOCUSBRAND_COUNT_IN_REPORT || bmodel.configurationMasterHelper.IS_MUSTSELL_COUNT_IN_REPORT)
                    getFocusandAndMustSellOrderedProducts();

                //Adding accumulation scheme free products to the last ordered product list, so that it will listed on print
                orderHelper.updateOffInvoiceSchemeInProductOBJ(mOrderedProductList);

                new MyThread(this, DataMembers.SAVEINVOICE).start();
            } else {

                build = new AlertDialog.Builder(OrderSummary.this);

                customProgressDialog(build, getResources().getString(R.string.saving_new_order));
                alertDialog = build.create();
                alertDialog.show();
                if (bmodel.configurationMasterHelper.IS_FOCUSBRAND_COUNT_IN_REPORT || bmodel.configurationMasterHelper.IS_MUSTSELL_COUNT_IN_REPORT)
                    getFocusandAndMustSellOrderedProducts();

                if (orderHelper.hasOrder(mOrderedProductList)) {

                    if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION
                            && bmodel.configurationMasterHelper.IS_SIH_VALIDATION
                            && bmodel.configurationMasterHelper.IS_INVOICE) {
                        bmodel.batchAllocationHelper
                                .loadFreeProductBatchList();
                    }


                    orderHelper.invoiceDisount = Double.toString(enteredDiscAmtOrPercent);

                    new MyThread(OrderSummary.this,
                            DataMembers.SAVEORDERANDSTOCK).start();
                    bmodel.saveModuleCompletion("MENU_STK_ORD");


                } else {
                    isClick = false;
                }


            }
        } catch (Exception ex) {
            Commons.printException(ex);
        }
    }

    @Override
    public void dismiss() {
        isClick = false;
    }

    //this method will be called after SendMail Asytask is completed
    void loadClass() {
        Intent i;
        switch (sendMailAndLoadClass) {
            case "CommonPrintPreviewActivityPRINT_FILE_INVOICE":
                i = new Intent(OrderSummary.this,
                        CommonPrintPreviewActivity.class);
                i.putExtra("IsFromOrder", true);
                i.putExtra("IsUpdatePrintCount", true);
                i.putExtra("isHomeBtnEnable", true);
                startActivity(i);
                overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                finish();
                break;

            case "HomeScreenTwoPRINT_FILE_ORDER":
                i = new Intent(
                        OrderSummary.this,
                        HomeScreenTwo.class);
                Bundle extras = getIntent().getExtras();
                if (extras != null) {
                    i.putExtra("IsMoveNextActivity", bmodel.configurationMasterHelper.MOVE_NEXT_ACTIVITY);
                    i.putExtra("CurrentActivityCode", mActivityCode);
                }
                startActivity(i);
                break;

        }
    }

    public boolean isDiscountDialog() {
        return isDiscountDialog;
    }

    public void setDiscountDialog(boolean discountDialog) {
        isDiscountDialog = discountDialog;
    }

    public interface OrderRemarksClickListener {

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
                holder.tvwtot.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

                ((View) row.findViewById(R.id.view_dotted_line)).setLayerType(View.LAYER_TYPE_SOFTWARE, null);


                if (!"MENU_ORDER".equals(screenCode) && bmodel.configurationMasterHelper.SHOW_STOCK_IN_SUMMARY) {

                    if (bmodel.configurationMasterHelper.SHOW_STOCK_SC) {
                        ((LinearLayout) row.findViewById(R.id.llShelfCase)).setVisibility(View.VISIBLE);
                        ((TextView) row.findViewById(R.id.shelfCaseTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                        holder.scqty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
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
                        ((TextView) row.findViewById(R.id.shelfOuterTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                        holder.shoqty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
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
                        ((TextView) row.findViewById(R.id.shelfPcsTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                        holder.spqty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
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
                    ((TextView) row.findViewById(R.id.caseTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                    holder.caseqty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
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
                    ((TextView) row.findViewById(R.id.pcsTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                    holder.pcsQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
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
                    ((TextView) row.findViewById(R.id.outercaseTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                    holder.outerQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
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
                    ((TextView) row.findViewById(R.id.weighttitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                    holder.caseqty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
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
                    ((TextView) row.findViewById(R.id.srpTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                    holder.tw_srp.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
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
                holder.tvwtot.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

                if (!"MENU_ORDER".equals(screenCode) && bmodel.configurationMasterHelper.SHOW_STOCK_IN_SUMMARY) {


                    if (bmodel.configurationMasterHelper.SHOW_STOCK_SC) {
                        ((LinearLayout) row.findViewById(R.id.llShelfCase)).setVisibility(View.VISIBLE);
                        ((TextView) row.findViewById(R.id.shelfCaseTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                        holder.scqty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
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
                        ((TextView) row.findViewById(R.id.shelfOuterTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                        holder.shoqty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
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
                        ((TextView) row.findViewById(R.id.shelfPcsTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                        holder.spqty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
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
                    ((TextView) row.findViewById(R.id.caseTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    holder.caseqty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
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
                    ((TextView) row.findViewById(R.id.pcsTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    holder.pcsQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
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
                    ((TextView) row.findViewById(R.id.outercaseTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    holder.outerQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
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
                    ((TextView) row.findViewById(R.id.weighttitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    holder.caseqty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
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
                    ((TextView) row.findViewById(R.id.srpTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    holder.tw_srp.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
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

    private class deleteStockAndOrder extends AsyncTask<Integer, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(Integer... params) {
            bmodel.orderAndInvoiceHelper.deleteStockAndOrder();
            return null;
        }
    }

    private class Checkandprint extends AsyncTask<Integer, Integer, Boolean> {
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            builder = new AlertDialog.Builder(OrderSummary.this);

            customProgressDialog(builder, "Printing");
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
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



    private final DatePickerDialog.OnDateSetListener mDeliverDatePickerListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            Calendar selectedDate = new GregorianCalendar(year, monthOfYear,
                    dayOfMonth);
            delievery_date.setText(DateUtil.convertDateObjectToRequestedFormat(
                    selectedDate.getTime(),
                    ConfigurationMasterHelper.outDateFormat));

            Calendar currentCalendar = Calendar.getInstance();
            currentCalendar.add(Calendar.DAY_OF_YEAR, -1);

            if (currentCalendar.after(selectedDate)) {

                Toast.makeText(
                        getApplicationContext(),
                        getResources().getString(
                                R.string.Please_select_next_day),
                        Toast.LENGTH_SHORT).show();

                Calendar defaultCalendar = Calendar.getInstance();
                defaultCalendar.add(Calendar.DAY_OF_YEAR, (bmodel.configurationMasterHelper.DEFAULT_NUMBER_OF_DAYS_TO_DELIVER_ORDER == 0 ? 1 : bmodel.configurationMasterHelper.DEFAULT_NUMBER_OF_DAYS_TO_DELIVER_ORDER));
                delievery_date.setText(DateUtil.convertDateObjectToRequestedFormat(defaultCalendar.getTime(), ConfigurationMasterHelper.outDateFormat));
            }
        }
    };



    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            isClick = false;

            if (msg.what == DataMembers.NOTIFY_DATABASE_NOT_SAVED) {
                Toast.makeText(OrderSummary.this, "DataBase Restore failed.",
                        Toast.LENGTH_SHORT).show();
            }
            else if (msg.what == DataMembers.NOTIFY_ORDER_SAVED) {
                try {

                    alertDialog.dismiss();

                        if((bmodel.configurationMasterHelper.SHOW_ZEBRA_GHANA
                                || bmodel.configurationMasterHelper.SHOW_ZEBRA_DIAGEO
                                || bmodel.configurationMasterHelper.SHOW_ZEBRA_TITAN
                                || bmodel.configurationMasterHelper.SHOW_ZEBRA_UNIPAL)){
                            showDialog(DIALOG_ORDER_SAVED_WITH_PRINT_OPTION);
                        }
                        else {
                            showDialog(DIALOG_ORDER_SAVED);
                        }


                } catch (Exception e) {
                    Commons.printException("" + e);
                }
            }
            else if (msg.what == DataMembers.NOTIFY_ORDER_DELETED) {
                try {
                    alertDialog.dismiss();
                    bmodel = (BusinessModel) getApplicationContext();

                    bmodel.showAlert(
                            getResources().getString(
                                    R.string.order_deleted_sucessfully)
                                    + orderHelper.getOrderid(),
                            DataMembers.NOTIFY_ORDER_SAVED);
                } catch (Exception e) {
                    Commons.printException("" + e);
                }
            }
            else if (msg.what == DataMembers.NOTIFY_INVOICE_SAVED) {
                try {

                    orderHelper.getPrintCount(OrderSummary.this);
                    alertDialog.dismiss();


                    if (bmodel.configurationMasterHelper.IS_INVOICE) {
                        if (bmodel.configurationMasterHelper.SHOW_ZEBRA_UNIPAL) {
                            showDialog(DIALOG_INVOICE_SAVED);
                        } else if (bmodel.configurationMasterHelper.SHOW_ZEBRA_TITAN) {

                            Intent i = new Intent(OrderSummary.this,
                                    PrintPreviewScreenTitan.class);
                            i.putExtra("IsFromOrder", true);
                            i.putExtra("entryLevelDis", entryLevelDiscount);
                            startActivity(i);
                            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                            finish();

                        } else if (bmodel.configurationMasterHelper.COMMON_PRINT_BIXOLON
                                || bmodel.configurationMasterHelper.COMMON_PRINT_ZEBRA
                                || bmodel.configurationMasterHelper.COMMON_PRINT_SCRYBE
                                || bmodel.configurationMasterHelper.COMMON_PRINT_LOGON) {

                            if ("1".equalsIgnoreCase(bmodel.getRetailerMasterBO().getRField4())) {
                                bmodel.productHelper.updateDistributorDetails();
                            }

                            SalesReturnHelper salesReturnHelper = SalesReturnHelper.getInstance(OrderSummary.this);

                            final List<ProductMasterBO> orderListWithReplace = salesReturnHelper.updateReplaceQtyWithOutTakingOrder(mOrderedProductList);
                            Vector<ProductMasterBO> orderList = new Vector<>(orderListWithReplace);
                            bmodel.mCommonPrintHelper.xmlRead("invoice", false, orderList, null);


                            bmodel.writeToFile(String.valueOf(bmodel.mCommonPrintHelper.getInvoiceData()),
                                    StandardListMasterConstants.PRINT_FILE_INVOICE + bmodel.invoiceNumber, "/" + DataMembers.PRINT_FILE_PATH);

                            sendMailAndLoadClass = "CommonPrintPreviewActivityPRINT_FILE_INVOICE";
                            if (bmodel.configurationMasterHelper.IS_ORDER_SUMMERY_EXPORT_AND_EMAIL) {
                                new ShowEmailDialog().execute();
                            } else {
                                Intent i = new Intent(OrderSummary.this,
                                        CommonPrintPreviewActivity.class);
                                i.putExtra("IsFromOrder", true);
                                i.putExtra("IsUpdatePrintCount", true);
                                i.putExtra("isHomeBtnEnable", true);
                                startActivity(i);
                                overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                                finish();
                            }
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
                    Commons.printException(e);
                }
            }
            else if (msg.what == BtService.STATE_CONNECTED) {
                Toast.makeText(getApplicationContext(), "Connected",
                        Toast.LENGTH_SHORT).show();
                new Checkandprint().execute();

            }
            else if (msg.what == BtService.STATE_CONNECTING) {
                Toast.makeText(getApplicationContext(), "Connecting",
                        Toast.LENGTH_SHORT).show();
            }
            else if (msg.what == BtService.STATE_LISTEN) {
                if (msg.what == BtService.STATE_NONE) {
                    Toast.makeText(getApplicationContext(), "None",
                            Toast.LENGTH_SHORT).show();

                }
            }
            else if (msg.what == MESSAGE_DEVICE_NAME) {
                String mConnectedDeviceName;
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(),
                        "Device Name " + mConnectedDeviceName,
                        Toast.LENGTH_SHORT).show();

            }
            else if (msg.what == MESSAGE_TOAST) {
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



    private final android.content.DialogInterface.OnDismissListener discountDismissListener = new android.content.DialogInterface.OnDismissListener() {

        @Override
        public void onDismiss(DialogInterface dialog) {
            dialog.dismiss();
        }
    };



    //if IS_ORDER_SUMMERY_EXPORT_AND_EMAIL config
    //enabled ShowEmail dialog will be called
    private class ShowEmailDialog extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {

            if (mOrderedProductList.size() > 0) {

                return true;
            } else {
                Toast.makeText(bmodel, "No data to store", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (bmodel.configurationMasterHelper.IS_ORDER_SUMMERY_EXPORT_AND_EMAIL && aBoolean) {
                android.support.v4.app.FragmentManager ft = getSupportFragmentManager();
                EmailDialog dialog = new EmailDialog(
                        "MENU_STK_ORD", OrderSummary.this, bmodel.getRetailerMasterBO().getEmail());
                dialog.setCancelable(false);
                dialog.show(ft, "MENU_STK_ORD");
            }
        }
    }

    public class SendMail extends AsyncTask<Void, Void, Boolean> {

        private final String emailId = "";//Change this field value
        private final String password = "";//Change this field value
        Session session;
        Context mContext;
        ProgressDialog progressDialog;
        private String subject;
        private String body;
        private String email;

        public SendMail(Context ctx, String subject, String message, String email) {
            this.mContext = ctx;

            this.subject = subject;
            this.body = message;
            this.email = email;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(mContext, getResources().getString(R.string.sending_email), getResources().getString(R.string.please_wait_some_time), false);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            Properties props = System.getProperties();// new Properties();

            //Configuring properties for gmail
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.socketFactory.port", "587");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.starttls.enable", "true");
            //  props.put("mail.debug",true);

            //Creating a new session
            session = Session.getDefaultInstance(props,
                    new javax.mail.Authenticator() {
                        //Authenticating the password
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(emailId, password);
                        }
                    });

            try {


                javax.mail.Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(emailId));
                if (!TextUtils.isEmpty(bmodel.getRetailerMasterBO().getEmail()))
                    message.setRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(bmodel.getRetailerMasterBO().getEmail(), email));
                else
                    message.setRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(email));
                message.setSubject(subject);
                message.setText(body);
                //  mm.setContent(message,"text/html; charset=utf-8");

                BodyPart bodyPart = new MimeBodyPart();
                bodyPart.setText(body);//Content(message,"text/html");
                //Attachment
                DataSource source = null;
                if (sendMailAndLoadClass.equalsIgnoreCase("CommonPrintPreviewActivityPRINT_FILE_ORDER") ||
                        sendMailAndLoadClass.equalsIgnoreCase("HomeScreenTwoPRINT_FILE_ORDER")) {
                    source = new FileDataSource(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + DataMembers.IVYDIST_PATH + "/" +
                            StandardListMasterConstants.PRINT_FILE_ORDER + orderHelper.getOrderid() + ".txt");
                    bodyPart.setDataHandler(new DataHandler(source));
                    bodyPart.setFileName("OrderDetails" + ".txt");
                }
                if (sendMailAndLoadClass.equalsIgnoreCase("CommonPrintPreviewActivityPRINT_FILE_INVOICE")) {
                    source = new FileDataSource(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + DataMembers.IVYDIST_PATH + "/" +
                            StandardListMasterConstants.PRINT_FILE_INVOICE + bmodel.invoiceNumber + ".txt");
                    bodyPart.setDataHandler(new DataHandler(source));
                    bodyPart.setFileName("InvoiceDetails" + ".txt");
                }


                MimeMultipart multiPart = new MimeMultipart();
                multiPart.addBodyPart(bodyPart);
                message.setContent(multiPart);

                Thread.currentThread().setContextClassLoader(getClassLoader());

                MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
                mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
                mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
                mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
                mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
                mc.addMailcap("message/rfc822;; x-java-content- handler=com.sun.mail.handlers.message_rfc822");

                //sending mail
                Transport.send(message);
                //}
                //}
            } catch (Exception ex) {
                Commons.printException(ex);
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean isSent) {
            super.onPostExecute(isSent);

            progressDialog.dismiss();

            if (isSent) {
                Toast.makeText(OrderSummary.this, getResources().getString(R.string.email_sent),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(OrderSummary.this, getResources().getString(R.string.error_in_sending_email),
                        Toast.LENGTH_SHORT).show();
            }
            loadClass();
        }
    }

}
