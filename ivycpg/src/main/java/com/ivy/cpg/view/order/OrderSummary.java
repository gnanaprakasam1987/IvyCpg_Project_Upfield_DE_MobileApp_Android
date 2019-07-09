package com.ivy.cpg.view.order;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.collection.AdvancePaymentDialogFragment;
import com.ivy.cpg.view.collection.CollectionBO;
import com.ivy.cpg.view.collection.CollectionHelper;
import com.ivy.cpg.view.nonfield.NonFieldHelper;
import com.ivy.cpg.view.order.catalog.CatalogOrder;
import com.ivy.cpg.view.order.discount.DiscountHelper;
import com.ivy.cpg.view.order.indicativeOrderReason.IndicativeOrderReasonActivity;
import com.ivy.cpg.view.order.scheme.SchemeDetailsMasterHelper;
import com.ivy.cpg.view.salesreturn.SalesReturnHelper;
import com.ivy.cpg.view.salesreturn.SalesReturnReasonBO;
import com.ivy.cpg.view.stockcheck.StockCheckHelper;
import com.ivy.cpg.view.van.LoadManagementHelper;
import com.ivy.lib.Utils;
import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.OrderHeader;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.cpg.view.order.scheme.SchemeProductBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.MyThread;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.MyDatePickerDialog;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.sd.png.view.CaptureSignatureActivity;
import com.ivy.sd.png.view.DataPickerDialogFragment;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.sd.png.view.OrderRemarkDialog;
import com.ivy.sd.png.view.OrderSummaryDialogFragment;
import com.ivy.sd.png.view.SerialNoEntryScreen;
import com.ivy.sd.print.BtService;
import com.ivy.sd.print.CommonPrintPreviewActivity;
import com.ivy.sd.print.DemoSleeper;
import com.ivy.sd.print.PrintPreviewScreenTitan;
import com.ivy.sd.print.SettingsHelper;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FileUtils;
import com.ivy.utils.FontUtils;
import com.tremol.zfplibj.ZFPLib;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

public class OrderSummary extends IvyBaseActivityNoActionBar implements OnClickListener,
        StoreWiseDiscountDialog.OnMyDialogResult, DataPickerDialogFragment.UpdateDateInterface,
        OrderConfirmationDialog.OnConfirmationResult {

    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_TOAST = 5;
    private static final int DIALOG_DELIVERY_DATE_PICKER = 0;
    private static final String TAG = "OrderSummary";
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int MESSAGE_DEVICE_NAME = 4;
    private static final String ZEBRA_3INCH = "3";

    public static String mCurrentActivityCode;

    private static final int DIALOG_NEGATIVE_INVOICE_CHECK = 6;
    private static final int DIALOG_DELETE_STOCK_AND_ORDER = 1;
    private static final int DIALOG_DELETE_ONLY_ORDER = 5;
    private static final int DIALOG_ORDER_SAVED_WITH_PRINT_OPTION = 2;
    private static final int DIALOG_ORDER_SAVED = 3;
    private static final int DIALOG_NUMBER_OF_PRINTS_ORDER = 10;
    private static final int DIALOG_NUMBER_OF_PRINTS_INVOICE = 11;
    private static final int DIALOG_INVOICE_SAVED = 9;
    private static final int DIALOG_SIGNATURE_AVAILABLE = 8;
    private static final int CAMERA_REQUEST_CODE = 7;

    private static final int DISCOUNT_RESULT_CODE = 114;
    private static final int RETURN_PRODUCT_RESULT_CODE = 115;
    private static final int INDICATIVE_ORDER_REASON_RESULT_CODE = 116;
    private static final int COLLECTION_INVOICE_RESULT_CODE = 120;


    private static final int FILE_SELECTION = 12;

    private Button button_order;
    private Button button_invoice;
    private TextView text_LPC;
    private TextView text_totalOrderValue, textbill1, textbill2, linesBill1, linesBill2, text_totweigh;
    private TextView text_totalOrderedQuantity;
    private Button button_deliveryDate;
    private ExpandableListView listView;
    private ImageView imageView_amountSplitUp;

    private AlertDialog.Builder build;
    private AlertDialog alertDialog;
    private AmountSplitUpDialog amountSplitUpDialog;
    private OrderConfirmationDialog orderConfirmationDialog;
    private StoreWiseDiscountDialog mStoreWiseDiscountDialogFragment;

    private BusinessModel bModel;
    private CollectionBO collectionbo;

    private DiscountHelper discountHelper;
    private OrderHelper orderHelper;
    private StockCheckHelper stockCheckHelper;

    private LinkedList<ProductMasterBO> mOrderedProductList;
    private Vector<ProductMasterBO> mSortedList;

    private boolean isFromOrder;
    private double enteredDiscAmtOrPercent = 0;

    private double totalOrderValue;
    private boolean isClick = false;
    private double entryLevelDiscount = 0.0;

    private double totalSchemeDiscValue;
    private int mSelectedPrintCount = 0;

    private boolean isClicked;
    private String screenCode = "MENU_STK_ORD";
    private String PHOTO_PATH = "";
    private String signatureName;
    private SharedPreferences sharedPreferences;

    private BluetoothAdapter mBluetoothAdapter = null;
    private BtService mChatService = null;
    private Connection zebraPrinterConnection;
    private ZebraPrinter printer;
    private AlertDialog.Builder builder10;

    private TextView text_creditNote;
    public static final String CREDIT_TYPE = "CREDIT";

    private boolean isEditMode = false;
    private Calendar mCalendar = null;
    private String mImageName, attachedFilePath = "";
    private int linesPerCall = 0;
    private String schemeNames;
    private String discountNames;
    private double taxValue;

    private CollectionHelper collectionHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.ordersummary);

        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);


        bModel = (BusinessModel) getApplicationContext();
        bModel.setContext(this);

        discountHelper = DiscountHelper.getInstance(this);
        orderHelper = OrderHelper.getInstance(this);
        stockCheckHelper = StockCheckHelper.getInstance(this);
        mCalendar = Calendar.getInstance();
        collectionHelper = CollectionHelper.getInstance(this);

        // Close the screen if user id becomes 0 **/
        if (bModel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
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

        signatureName = "";

        String screenTitle = bModel.configurationMasterHelper
                .getHomescreentwomenutitle("MENU_CLOSING");

        if (!screenCode.equals("MENU_CLOSING"))
            screenTitle = getResources().getString(R.string.summary);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
                setScreenTitle(screenTitle);
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
        }

        //Purpose of saving this is not clear. So Commenting by Abbas
        //BModel.saveModuleCompletion("MENU_CLOSING");


        PHOTO_PATH = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + DataMembers.photoFolderName;

        // Focus product alert
        if (bModel.configurationMasterHelper.SHOW_ORDER_FOCUS_COUNT) {
            showFocusProductAlert();
        }

        initializeViews();
        hideAndSeek();
        updateLabels();

        if (!bModel.configurationMasterHelper.IS_INVOICE && bModel.configurationMasterHelper.SHOW_DELIVERY_DATE)
            setDeliveryDate();


        sharedPreferences = getSharedPreferences(BusinessModel.PREFS_NAME, MODE_PRIVATE);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        if (bModel.getOrderHeaderBO() == null) {
            bModel.setOrderHeaderBO(new OrderHeader());
        }


        // update empty bottle returns
        if (SchemeDetailsMasterHelper.getInstance(getApplicationContext()).IS_SCHEME_ON &&
                SchemeDetailsMasterHelper.getInstance(getApplicationContext()).IS_SCHEME_SHOW_SCREEN) {

            if (!bModel.isEdit() || !bModel.isDoubleEdit_temp()) {
                SchemeDetailsMasterHelper.getInstance(getApplicationContext())
                        .updateFreeProductBottleReturn();
            }
            // update empty bottle return group wise
            if (bModel.configurationMasterHelper.SHOW_GROUPPRODUCTRETURN) {
                bModel.productHelper.setGroupWiseReturnQty();
                bModel.productHelper.calculateOrderReturnTypeWiseValue();
            }
        }

        // If Collection before invoice module enable, clear object
        if (bModel.configurationMasterHelper.SHOW_COLLECTION_BEFORE_INVOICE) {
            collectionbo = new CollectionBO();
            collectionHelper.getPaymentList().clear();
        }
    }

    private void initializeViews() {
        text_LPC = findViewById(R.id.lcp);
        text_totalOrderValue = findViewById(R.id.totalValue);
        textbill1 = findViewById(R.id.tvBill1Value);
        textbill2 = findViewById(R.id.tvBill2Value);
        linesBill1 = findViewById(R.id.tvBill1Line);
        linesBill2 = findViewById(R.id.tvBill2Line);
        button_deliveryDate = findViewById(R.id.deliveryDate);
        button_order = findViewById(R.id.orderSummarySave);
        listView = findViewById(R.id.elv);
        button_invoice = findViewById(R.id.saveAndGoInvoice);
        text_totalOrderedQuantity = findViewById(R.id.tv_totalqty);
        imageView_amountSplitUp = findViewById(R.id.icAmountSpilitup);
        text_creditNote = findViewById(R.id.tvCreditNote);
        text_totweigh = findViewById(R.id.tvTotWeigh);

        //typefaces
        ((TextView) findViewById(R.id.tv_deliveryDate)).setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.LIGHT));
        ((TextView) findViewById(R.id.lpcLabel)).setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.LIGHT));
        ((TextView) findViewById(R.id.totalValuelbl)).setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.LIGHT));
        ((TextView) findViewById(R.id.title_totalqty)).setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.LIGHT));
        ((TextView) findViewById(R.id.lblbill1)).setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.LIGHT));
        ((TextView) findViewById(R.id.lblbill2)).setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.LIGHT));
        ((TextView) findViewById(R.id.lblbill1Line)).setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.LIGHT));
        ((TextView) findViewById(R.id.lblbill2Line)).setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.LIGHT));
        ((TextView) findViewById(R.id.lblweigh)).setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.LIGHT));

        button_deliveryDate.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.THIN));
        text_LPC.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.THIN));
        text_totalOrderValue.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.THIN));
        textbill1.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.THIN));
        textbill2.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.THIN));
        linesBill1.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.THIN));
        linesBill2.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.THIN));
        text_totalOrderedQuantity.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.THIN));
        text_totweigh.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.THIN));
        button_order.setTypeface(FontUtils.getFontBalooHai(this, FontUtils.FontType.REGULAR));
        button_invoice.setTypeface(FontUtils.getFontBalooHai(this, FontUtils.FontType.REGULAR));
        text_creditNote.setTypeface(FontUtils.getFontBalooHai(this, FontUtils.FontType.REGULAR));

        button_deliveryDate.setOnClickListener(this);
        button_order.setOnClickListener(this);
        button_invoice.setOnClickListener(this);
        imageView_amountSplitUp.setOnClickListener(this);

        if (bModel.configurationMasterHelper.IS_ORDER_SPLIT) {
            findViewById(R.id.ll_values).setVisibility(View.GONE);
            findViewById(R.id.ll_lines).setVisibility(View.GONE);
        } else {
            findViewById(R.id.ll_bill1).setVisibility(View.GONE);
            findViewById(R.id.ll_bill2).setVisibility(View.GONE);
            findViewById(R.id.ll_bill1Line).setVisibility(View.GONE);
            findViewById(R.id.ll_bill2Line).setVisibility(View.GONE);
        }
    }

    /**
     * Label configurations
     */
    private void updateLabels() {

        try {
            if (bModel.labelsMasterHelper.applyLabels(findViewById(
                    R.id.totalValuelbl).getTag()) != null)
                ((TextView) findViewById(R.id.totalValuelbl))
                        .setText(bModel.labelsMasterHelper
                                .applyLabels(findViewById(
                                        R.id.totalValuelbl)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException(" " + e);
        }


        try {
            if (bModel.labelsMasterHelper.applyLabels(findViewById(
                    R.id.title_totalqty).getTag()) != null)
                ((TextView) findViewById(R.id.title_totalqty))
                        .setText(bModel.labelsMasterHelper
                                .applyLabels(findViewById(
                                        R.id.title_totalqty)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException(" " + e);
        }

        try {
            if (bModel.labelsMasterHelper.applyLabels(findViewById(
                    R.id.orderSummarySave).getTag()) != null)
                ((TextView) findViewById(R.id.orderSummarySave))
                        .setText(bModel.labelsMasterHelper
                                .applyLabels(findViewById(
                                        R.id.orderSummarySave)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException(" " + e);
        }
        try {
            if (bModel.labelsMasterHelper.applyLabels(findViewById(
                    R.id.saveAndGoInvoice).getTag()) != null)
                ((TextView) findViewById(R.id.saveAndGoInvoice))
                        .setText(bModel.labelsMasterHelper
                                .applyLabels(findViewById(
                                        R.id.saveAndGoInvoice)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException(" " + e);
        }
        try {
            if (bModel.labelsMasterHelper.applyLabels(findViewById(
                    R.id.lbl_comy_disc).getTag()) != null)
                ((TextView) findViewById(R.id.lbl_comy_disc))
                        .setText(bModel.labelsMasterHelper
                                .applyLabels(findViewById(
                                        R.id.lbl_comy_disc)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException(" " + e);
        }
        try {
            if (bModel.labelsMasterHelper.applyLabels(findViewById(
                    R.id.lbl_dist_disc).getTag()) != null)
                ((TextView) findViewById(R.id.lbl_dist_disc))
                        .setText(bModel.labelsMasterHelper
                                .applyLabels(findViewById(
                                        R.id.lbl_dist_disc)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException(" " + e);
        }

        try {
            if (bModel.labelsMasterHelper.applyLabels(findViewById(
                    R.id.totaltitle).getTag()) != null)
                ((TextView) findViewById(R.id.totaltitle))
                        .setText(bModel.labelsMasterHelper
                                .applyLabels(findViewById(R.id.totaltitle)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException(" " + e);
        }
        if (bModel.configurationMasterHelper.SHOW_ORDER_WEIGHT) {
            try {
                if (bModel.labelsMasterHelper.applyLabels(findViewById(
                        R.id.lblweigh).getTag()) != null)
                    ((TextView) findViewById(R.id.lblweigh))
                            .setText(bModel.labelsMasterHelper
                                    .applyLabels(findViewById(R.id.lblweigh)
                                            .getTag()));
            } catch (Exception e) {
                Commons.printException(" " + e);
            }
        }

    }

    /**
     * This method will on/off the items based in the configuration.
     */
    private void hideAndSeek() {
        try {
            // if pre-sales disable the following two component
            if (bModel.configurationMasterHelper.IS_INVOICE_AS_MOD)
                button_invoice.setVisibility(View.GONE);

            if (bModel.configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED && !bModel.configurationMasterHelper.IS_SIH_VALIDATION) {
                button_invoice.setVisibility(View.GONE);
            }

            if (!bModel.configurationMasterHelper.IS_INVOICE)
                button_invoice.setVisibility(View.GONE);

            if (orderHelper.isQuickCall)
                button_invoice.setVisibility(View.GONE);

            if (bModel.configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED) {
                if (bModel.configurationMasterHelper.IS_SIH_VALIDATION) {
                    findViewById(R.id.layoutDelivery).setVisibility(View.GONE);
                }
            } else if (bModel.configurationMasterHelper.IS_INVOICE || !bModel.configurationMasterHelper.SHOW_DELIVERY_DATE) {
                findViewById(R.id.layoutDelivery).setVisibility(View.GONE);
            }

            if (!bModel.configurationMasterHelper.SHOW_LPC_ORDER || bModel.configurationMasterHelper.IS_ORDER_SPLIT) {
                findViewById(R.id.ll_lines).setVisibility(View.GONE);
            }

            if (bModel.configurationMasterHelper.SHOW_TOTAL_QTY_ORDER) {
                findViewById(R.id.ll_totqty).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.ll_totqty).setVisibility(View.GONE);
            }

            if (bModel.configurationMasterHelper.SHOW_TOTAL_VALUE_ORDER && !bModel.configurationMasterHelper.IS_ORDER_SPLIT) {
                findViewById(R.id.ll_values).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.ll_values).setVisibility(View.GONE);
            }

            if (bModel.configurationMasterHelper.IS_SHOW_DISCOUNTS_ORDER_SUMMARY) {
                imageView_amountSplitUp.setVisibility(View.VISIBLE);

            } else {
                imageView_amountSplitUp.setVisibility(View.GONE);
            }
            if (!bModel.configurationMasterHelper.SHOW_ORDER_WEIGHT)
                findViewById(R.id.ll_totweight).setVisibility(View.GONE);
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    /**
     * Default delivery date fetching and setting value.
     */
    private void setDeliveryDate() {
        try {
            if (bModel.isEdit()) {

                String delDate = DateTimeUtils.convertFromServerDateToRequestedFormat(bModel.getDeliveryDate(OrderHelper.getInstance(this).selectedOrderId, bModel.getRetailerMasterBO()
                                .getRetailerID()),
                        ConfigurationMasterHelper.outDateFormat);
                button_deliveryDate.setText(delDate);
                Date selected = DateTimeUtils.convertStringToDateObject(delDate, ConfigurationMasterHelper.outDateFormat);
                mCalendar.setTime(selected);
            } else {
                NonFieldHelper.getInstance(this).downWeekOffs(OrderSummary.this);
                mCalendar.add(Calendar.DAY_OF_YEAR, (bModel.configurationMasterHelper.DEFAULT_NUMBER_OF_DAYS_TO_DELIVER_ORDER == 0 ? 1 : bModel.configurationMasterHelper.DEFAULT_NUMBER_OF_DAYS_TO_DELIVER_ORDER));

                mCalendar = dateValidation(mCalendar);
                button_deliveryDate.setText(DateTimeUtils.convertDateObjectToRequestedFormat(mCalendar.getTime(), ConfigurationMasterHelper.outDateFormat));

            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        bModel = (BusinessModel) getApplicationContext();
        bModel.setContext(this);

        isClicked = false;

        //session out if user id becomes 0
        if (bModel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }
        if (bModel.configurationMasterHelper.IS_SHOW_ORDER_PHOTO_CAPTURE)
            invalidateOptionsMenu();

        prepareScreenData();

    }

    private void prepareScreenData() {

        int totalQuantityOrdered = 0;
        float totalWeight = 0;
        linesPerCall = 0;

        bModel.getRetailerMasterBO().setBillWiseCompanyDiscount(0);
        bModel.getRetailerMasterBO().setBillWiseDistributorDiscount(0);

        Vector<ProductMasterBO> productList = bModel.productHelper
                .getProductMaster();

        if (productList == null) {
            bModel.showAlert(
                    getResources().getString(R.string.no_products_exists), 0);
            return;
        }

        mOrderedProductList = new LinkedList<>();
        totalOrderValue = 0;

        if (!bModel.configurationMasterHelper.IS_ORDER_SPLIT) {

            for (ProductMasterBO productBO : productList) {

                if (productBO.getOrderedCaseQty() > 0
                        || productBO.getOrderedPcsQty() > 0
                        || productBO.getOrderedOuterQty() > 0
                        || (bModel.configurationMasterHelper.SHOW_SALES_RETURN_IN_ORDER && isReturnDoneForProduct(productBO))
                        || (bModel.configurationMasterHelper.SHOW_NON_SALABLE_PRODUCT && productBO.getFoc()>0)) {

                    int totalQuantity = productBO.getOrderedPcsQty() + productBO.getOrderedCaseQty() * productBO.getCaseSize() + productBO.getOrderedOuterQty() * productBO.getOutersize();

                    int totalOrderedQty = orderHelper.getTotalOrderedQty(productBO);
                    totalQuantityOrdered = (totalOrderedQty != -1) ? (totalQuantityOrdered + totalOrderedQty) : (totalQuantityOrdered + totalQuantity);

                    totalWeight = totalWeight + SDUtil.convertToFloat(String.valueOf(SDUtil.formatAsPerCalculationConfig(totalQuantity * productBO.getWeight())));

                    mOrderedProductList.add(productBO);

                    if (bModel.configurationMasterHelper.SHOW_SALES_RETURN_IN_ORDER && totalQuantity > 0)
                        linesPerCall++;

                    // Set the calculated flat line values in productBO
                    double lineValue = calculateLineValue(productBO);

                    productBO.setTotalOrderedQtyInPieces(totalQuantity);
                    productBO.setLineValue(lineValue);
                    productBO.setNetValue(lineValue);
                    productBO.setLineValueAfterSchemeApplied(lineValue);


                    totalOrderValue += lineValue;

                    // Purpose of setting this is not clear
                    productBO.setOrderPricePiece(productBO.getSrp());

                    // clear discounts.
                    productBO.setCompanyTypeDiscount(0);
                    productBO.setDistributorTypeDiscount(0);
                    discountHelper.clearProductDiscountAndTaxValue(productBO);
                    // clear scheme free products stored in product obj
                    productBO.setSchemeProducts(new ArrayList<>());

                }
            }


            if (linesPerCall == 0)
                linesPerCall = mOrderedProductList.size();

            // Developed for JnJ ID : Sequencing based on user entry.
            if (bModel.configurationMasterHelper.IS_SHOW_ORDERING_SEQUENCE) {
                mSortedList = new Vector<>();
                mOrderedProductList = orderHelper.organizeProductsByUserEntry();
                mSortedList.addAll(mOrderedProductList);
            }

            // Sequencing for print. Alphabetically or Brand wise Alphabetical.
            if (bModel.configurationMasterHelper.IS_PRINT_SEQUENCE_REQUIRED) {
                if (bModel.configurationMasterHelper.IS_PRINT_SEQUENCE_LEVELWISE) {
                    mOrderedProductList = bModel.orderAndInvoiceHelper.sortByLevel(mOrderedProductList);
                } else {
                    mOrderedProductList = bModel.orderAndInvoiceHelper.sort(mOrderedProductList);
                }
            }

            if (bModel.getOrderHeaderBO() != null)
                bModel.getOrderHeaderBO().setTotalWeight(totalWeight);

            // Empties Management is Enabled, then we have to add totalOrderValue with remaining value(Order return value).
            if (bModel.configurationMasterHelper.SHOW_PRODUCTRETURN
                    && !bModel.configurationMasterHelper.SHOW_BOTTLE_CREDITLIMIT
                    && bModel.configurationMasterHelper.IS_SIH_VALIDATION && bModel.getOrderHeaderBO() != null) {
                totalOrderValue = totalOrderValue
                        + bModel.getOrderHeaderBO().getRemainigValue();
            }

            // Scheme calculations
            if (SchemeDetailsMasterHelper.getInstance(getApplicationContext()).IS_SCHEME_ON &&
                    SchemeDetailsMasterHelper.getInstance(getApplicationContext()).IS_SCHEME_SHOW_SCREEN) {
                totalSchemeDiscValue = discountHelper.calculateSchemeDiscounts(mOrderedProductList, getApplicationContext());
                schemeNames = discountHelper.getSchemeData();
                totalOrderValue -= totalSchemeDiscValue;
            }

            if (bModel.configurationMasterHelper.IS_REMOVE_TAX_ON_SRP) {
                //applying removed tax..
                bModel.productHelper.taxHelper.applyRemovedTax(mOrderedProductList);
            }

            //  Apply product entry level discount
            if (bModel.configurationMasterHelper.IS_PRODUCT_DISCOUNT_BY_USER_ENTRY) {
                entryLevelDiscount = discountHelper.calculateUserEntryLevelDiscount(mOrderedProductList);
                totalOrderValue = totalOrderValue - entryLevelDiscount;
            }

            // Apply Item  level discount
            if (bModel.configurationMasterHelper.SHOW_DISCOUNT) {
                double itemLevelDiscount = discountHelper.calculateItemLevelDiscount(mOrderedProductList);
                totalOrderValue = totalOrderValue - itemLevelDiscount;
                discountNames = discountHelper.getDistDiscountData();
                if (!"".equals(schemeNames)) {
                    schemeNames = schemeNames + "\n" + discountHelper.getCompDiscountData();
                } else
                    schemeNames = discountHelper.getCompDiscountData();
            }

            // Apply Exclude Item level Tax  in Product
            if (bModel.configurationMasterHelper.SHOW_TAX) {

                boolean isGSTEnabled = bModel.configurationMasterHelper.IS_GST || bModel.configurationMasterHelper.IS_GST_HSN;

                if (!isGSTEnabled || bModel.getRetailerMasterBO().getSupplierBO() != null && !bModel.getRetailerMasterBO().getSupplierBO().isCompositeRetailer() ) {

                    if (!isGSTEnabled
                            || (!bModel.getRetailerMasterBO().getGSTNumber().equals("-") || totalOrderValue > 5000)) {

                        if (bModel.configurationMasterHelper.IS_EXCLUDE_TAX)
                            bModel.productHelper.taxHelper.updateProductWiseExcludeTax();
                        else {
                            double totalTaxVal = bModel.productHelper.taxHelper.updateProductWiseIncludeTax(mOrderedProductList);
                            totalOrderValue = totalOrderValue + totalTaxVal;
                            taxValue = totalTaxVal;
                        }
                    }
                }
            }

            listView.setAdapter(new ProductExpandableAdapter());
            for (int i = 0; i < mOrderedProductList.size(); i++) {
                listView.expandGroup(i);
            }

            applyBillWiseDiscAndTax();

            //updating footer labels
            text_totalOrderValue.setText(bModel.formatValue(totalOrderValue));
            text_LPC.setText(String.valueOf(linesPerCall));
            text_totalOrderedQuantity.setText(String.valueOf(totalQuantityOrdered));
            text_totweigh.setText(Utils.formatAsTwoDecimal((double) totalWeight));

            if (bModel.configurationMasterHelper.IS_CREDIT_NOTE_CREATION &&
                    bModel.retailerMasterBO.getRpTypeCode().equalsIgnoreCase(CREDIT_TYPE) &&
                    ((bModel.configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED && bModel.getRetailerMasterBO().getIsVansales() == 1)
                            || bModel.configurationMasterHelper.IS_INVOICE)) {

                double remReturnValue = orderHelper.getRemaingReturnAmt();
                if (remReturnValue > 0) {
                    double creditNoteAmt = orderHelper.getCreditNoteValue(OrderSummary.this, remReturnValue);
                    text_creditNote.setText(getResources().getString(R.string.credit_note) + " : " + bModel.formatValue(creditNoteAmt));
                    text_creditNote.setVisibility(View.VISIBLE);
                } else
                    text_creditNote.setVisibility(View.GONE);
            } else
                text_creditNote.setVisibility(View.GONE);

        }
        //jnj specific separate bill
        if (bModel.configurationMasterHelper.IS_ORDER_SPLIT) {
            Vector<ProductMasterBO> bill1Products = new Vector<>();
            Vector<ProductMasterBO> bill2Products = new Vector<>();
            double bill1Value = 0, bill2Value = 0;

            for (ProductMasterBO productBO : productList) {
                //productBO = productList.elementAt(i);
                if (productBO.getOrderedCaseQty() > 0
                        || productBO.getOrderedPcsQty() > 0
                        || productBO.getOrderedOuterQty() > 0) {

                    int totalQuantity = productBO.getOrderedPcsQty() + productBO.getOrderedCaseQty() * productBO.getCaseSize() + productBO.getOrderedOuterQty() * productBO.getOutersize();

                    int totalOrderedQty = orderHelper.getTotalOrderedQty(productBO);
                    totalQuantityOrdered = (totalOrderedQty != -1) ? (totalQuantityOrdered + totalOrderedQty) : (totalQuantityOrdered + totalQuantity);

                    mOrderedProductList.add(productBO);

                    double lineValue = calculateLineValue(productBO);

                    // Set the calculated flat line values in productBO
                    totalOrderValue += lineValue;

                    productBO.setNetValue(lineValue);
                    productBO.setOrderPricePiece(productBO.getSrp());
                    productBO.setLineValueAfterSchemeApplied(lineValue);

                    productBO.setCompanyTypeDiscount(0);
                    productBO.setDistributorTypeDiscount(0);
                    // clear scheme free products stored in product obj
                    productBO.setSchemeProducts(new ArrayList<>());


                    if (productBO.isSeparateBill()) {
                        bill2Products.add(productBO);
                        bill2Value += lineValue;
                    } else {
                        bill1Products.add(productBO);
                        bill1Value += lineValue;
                    }
                }
            }

            if (bModel.configurationMasterHelper.IS_SHOW_ORDERING_SEQUENCE) {
                mSortedList = new Vector<>();
                mOrderedProductList = orderHelper.organizeProductsByUserEntry();
                mSortedList.addAll(mOrderedProductList);
            }


            listView.setAdapter(new ProductExpandableAdapter());
            for (int i = 0; i < mOrderedProductList.size(); i++) {
                listView.expandGroup(i);
            }

            text_LPC.setText(String.valueOf(bill1Products.size() + bill2Products.size()));
            textbill1.setText(bModel.formatValue(bill1Value));
            textbill2.setText(bModel.formatValue(bill2Value));
            linesBill1.setText(String.valueOf(bill1Products.size()));
            linesBill2.setText(String.valueOf(bill2Products.size()));
            text_totalOrderedQuantity.setText(String.valueOf(totalQuantityOrdered));

            listView.setAdapter(new ProductExpandableAdapter());
            for (int i = 0; i < mOrderedProductList.size(); i++) {
                listView.expandGroup(i);
            }

        }

        if (hasSchemeApplied())
            imageView_amountSplitUp.setColorFilter(getResources().getColor(R.color.Orange));
    }

    private double calculateLineValue(ProductMasterBO productBO) {
        if (bModel.configurationMasterHelper.IS_SIH_VALIDATION
                && bModel.configurationMasterHelper.IS_INVOICE
                && bModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION
                && productBO.getBatchwiseProductCount() > 0) {
            // Calculate batch wise price.
            return orderHelper
                    .getTotalValueOfAllBatches(productBO);

        } else {

            double totalValue = (productBO.getOrderedCaseQty() * productBO
                    .getCsrp())
                    + (productBO.getOrderedPcsQty() * productBO
                    .getSrp())
                    + (productBO.getOrderedOuterQty() * productBO
                    .getOsrp());

            return SDUtil.formatAsPerCalculationConfig(totalValue);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.order_summary_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {


        if (bModel.configurationMasterHelper.IS_SHOW_ORDER_PHOTO_CAPTURE) {
            if (bModel.getOrderHeaderBO() != null && bModel.getOrderHeaderBO().getOrderImageName() != null && bModel.getOrderHeaderBO().getOrderImageName().length() > 0) {
                Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_photo_camera_grey_24dp);
                drawable.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.Orange), PorterDuff.Mode.SRC_ATOP);
                menu.findItem(R.id.menu_capture).setIcon(drawable);
            } else {
                Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_photo_camera_grey_24dp);
                drawable.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.white), PorterDuff.Mode.SRC_ATOP);
                menu.findItem(R.id.menu_capture).setIcon(drawable);
            }

        }

        menu.findItem(R.id.menu_review).setVisible(bModel.configurationMasterHelper.SHOW_REVIEW_AND_PO);
        menu.findItem(R.id.menu_product_discount_by_user_entry).setVisible(bModel.configurationMasterHelper.IS_PRODUCT_DISCOUNT_BY_USER_ENTRY);

        // Empty returns.
        if (bModel.configurationMasterHelper.IS_SIH_VALIDATION
                && bModel.configurationMasterHelper.SHOW_PRODUCTRETURN) {
            menu.findItem(R.id.menu_product_return).setVisible(true);
        } else {
            menu.findItem(R.id.menu_product_return).setVisible(false);
        }

        // Collection before Invoice
        if (bModel.configurationMasterHelper.IS_SIH_VALIDATION
                && bModel.configurationMasterHelper.SHOW_COLLECTION_BEFORE_INVOICE
                && bModel.configurationMasterHelper.IS_INVOICE)
            menu.findItem(R.id.menu_collection).setVisible(true);
        else
            menu.findItem(R.id.menu_collection).setVisible(false);

        // Signature capturing.
        if (bModel.configurationMasterHelper.SHOW_SIGNATURE_SCREEN)
            menu.findItem(R.id.menu_signature).setVisible(true);


        if (bModel.configurationMasterHelper.SHOW_CALC)
            menu.findItem(R.id.menu_calculator).setVisible(true);
        else
            menu.findItem(R.id.menu_calculator).setVisible(false);

        if ((bModel.configurationMasterHelper.SHOW_STORE_WISE_DISCOUNT_DLG && bModel.configurationMasterHelper.BILL_WISE_DISCOUNT == 0)
                || bModel.configurationMasterHelper.SHOW_TOTAL_DISCOUNT_EDITTEXT)
            menu.findItem(R.id.menu_store_wise_discount).setVisible(true);
        else
            menu.findItem(R.id.menu_store_wise_discount).setVisible(false);

        // Line wise Discount and Tax split.
        if (bModel.configurationMasterHelper.SHOW_ORDER_SUMMARY_DETAIL_DIALOG) {
            menu.findItem(R.id.menu_summary_dialog).setVisible(true);
        }

        // Unipal's serial number capturing.
        if (bModel.configurationMasterHelper.SHOW_SERIAL_NO_SCREEN)
            menu.findItem(R.id.menu_serialno).setVisible(true);
        else
            menu.findItem(R.id.menu_serialno).setVisible(false);

        // Show delete button only on Edit.
        if (bModel.isEdit())
            menu.findItem(R.id.menu_delete).setVisible(true);
        else {
            menu.findItem(R.id.menu_delete).setVisible(false);
        }

        // Config to collect reason against each ordered products.
        if ((bModel.configurationMasterHelper.IS_SHOW_ONLY_INDICATIVE_ORDER
                || bModel.configurationMasterHelper.IS_SHOW_ORDER_REASON)) {
            menu.findItem(R.id.menu_indicative_order_reason).setVisible(true);
        }
        // enable photo capture option
        menu.findItem(R.id.menu_capture).
                setVisible(bModel.configurationMasterHelper.IS_SHOW_ORDER_PHOTO_CAPTURE);


        /*
         * enable attach file option
         * */

        if (bModel.configurationMasterHelper.IS_SHOW_ORDER_ATTACH_FILE) {
            if (bModel.getOrderHeaderBO().getOrderImageName().length() > 0) {
                Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_attach_file_black_24dp);
                drawable.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.Orange), PorterDuff.Mode.SRC_ATOP);
                menu.findItem(R.id.menu_attach_file).setIcon(drawable);
            } else {
                Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_attach_file_black_24dp);
                drawable.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.white), PorterDuff.Mode.SRC_ATOP);
                menu.findItem(R.id.menu_attach_file).setIcon(drawable);
            }

        }

        menu.findItem(R.id.menu_attach_file).setVisible(bModel.configurationMasterHelper.IS_SHOW_ORDER_ATTACH_FILE);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i1 = item.getItemId();
        if (i1 == R.id.menu_review) {
            OrderRemarkDialog ordRemarkDialog = new OrderRemarkDialog(
                    OrderSummary.this, false);
            ordRemarkDialog.show();
            return true;
        } else if (i1 == R.id.menu_product_discount_by_user_entry) {

            Intent intent = new Intent(OrderSummary.this, DiscountEditActivity.class);

            ActivityOptionsCompat opts = ActivityOptionsCompat.makeCustomAnimation(this, R.anim.zoom_enter, R.anim.hold);
            ActivityCompat.startActivityForResult(this, intent, DISCOUNT_RESULT_CODE, opts.toBundle());

            return true;
        } else if (i1 == R.id.menu_calculator) {
            callCalculatorApplication();
            return true;
        } else if (i1 == R.id.menu_store_wise_discount) {

            FragmentManager fm = getSupportFragmentManager();
            mStoreWiseDiscountDialogFragment = new StoreWiseDiscountDialog();
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
                int productSize;
                if (bModel.configurationMasterHelper.SHOW_GROUPPRODUCTRETURN) {
                    productSize = bModel.productHelper
                            .getBomReturnTypeProducts().size();
                } else {
                    productSize = bModel.productHelper.getBomReturnProducts()
                            .size();
                }
                if (productSize > 0) {

                    Intent intent = new Intent(this, ReturnProductActivity.class);
                    ActivityOptionsCompat opts = ActivityOptionsCompat.makeCustomAnimation(this, R.anim.zoom_enter, R.anim.hold);
                    ActivityCompat.startActivityForResult(this, intent, RETURN_PRODUCT_RESULT_CODE, opts.toBundle());

                } else {
                    Toast.makeText(OrderSummary.this,
                            getResources().getString(R.string.data_not_mapped),
                            Toast.LENGTH_SHORT).show();
                    isClicked = false;
                }
            }
            return true;
        } else if (i1 == R.id.menu_collection) {
            CollectionBeforeInvoiceCall();
            return true;
        } else if (i1 == R.id.menu_signature) {
            if (bModel.getOrderHeaderBO() != null)
                if (bModel.getOrderHeaderBO().isSignCaptured()) {
                    showDialog(DIALOG_SIGNATURE_AVAILABLE);
                    return true;
                }
            Intent i = new Intent(OrderSummary.this,
                    CaptureSignatureActivity.class);
            i.putExtra("fromModule", "ORDER");
            startActivity(i);
            bModel.configurationMasterHelper.setSignatureTitle("Signature");
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            //  finish();
            return true;
        } else if (i1 == R.id.menu_summary_dialog) {
            bModel.configurationMasterHelper.loadOrderSummaryDetailConfig();
            FragmentManager fm = getSupportFragmentManager();
            OrderSummaryDialogFragment dialogFragment = new OrderSummaryDialogFragment();
            Bundle bundle = new Bundle();

            bundle.putSerializable("OrderList", mOrderedProductList);
            dialogFragment.setArguments(bundle);
            dialogFragment.show(fm, "Dialog Fragment");
            dialogFragment.setCancelable(false);
        } else if (i1 == R.id.menu_serialno) {
            if (orderHelper.isOrderedSerialNoProducts(mOrderedProductList)) {
                Intent i = new Intent(OrderSummary.this, SerialNoEntryScreen.class);
                startActivity(i);
                overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            } else {
                Toast.makeText(OrderSummary.this, "No Scanned products ", Toast.LENGTH_SHORT).show();
            }
        } else if (i1 == R.id.menu_edit) {
            if (!isClick) {
                isClick = true;
                editOrder();
            }
        } else if (i1 == R.id.menu_delete) {
            if (orderHelper.isStockCheckMenuEnabled(OrderSummary.this))
                showDialog(DIALOG_DELETE_STOCK_AND_ORDER);
            else
                showDialog(DIALOG_DELETE_ONLY_ORDER);
        } else if (i1 == R.id.menu_indicative_order_reason) {

            Intent intent = new Intent(OrderSummary.this, IndicativeOrderReasonActivity.class);
            ActivityOptionsCompat opts = ActivityOptionsCompat.makeCustomAnimation(this, R.anim.zoom_enter, R.anim.hold);
            ActivityCompat.startActivityForResult(this, intent, INDICATIVE_ORDER_REASON_RESULT_CODE, opts.toBundle());

        } else if (i1 == R.id.menu_capture) {
            if (bModel.isExternalStorageAvailable()) {
                mImageName = "ORD_"
                        + Commons.now(Commons.DATE_TIME) + "_"
                        + bModel.getRetailerMasterBO()
                        .getRetailerID() + "_"
                        + bModel.userMasterHelper.getUserMasterBO().getUserid()
                        + "_img.jpg";


                String mFirstName = bModel.getOrderHeaderBO().getOrderImageName();

                boolean nFilesThere = bModel
                        .checkForNFilesInFolder(
                                FileUtils.photoFolderPath,
                                1, mFirstName);
                if (nFilesThere) {

                    showFileDeleteAlertWithImage(mFirstName, bModel.getOrderHeaderBO().getOrderImageName()
                    );
                } else {
                    Intent intent = new Intent(OrderSummary.this,
                            CameraActivity.class);
                    String path = FileUtils.photoFolderPath + "/"
                            + mImageName;
                    intent.putExtra("path", path);
                    startActivityForResult(intent,
                            CAMERA_REQUEST_CODE);
                }

            } else {
                Toast.makeText(
                        OrderSummary.this,
                        R.string.sdcard_is_not_ready_to_capture_img,
                        Toast.LENGTH_SHORT).show();
            }
        } else if (i1 == R.id.menu_attach_file) {

            attachedFilePath = "ORD_"
                    + Commons.now(Commons.DATE_TIME) + "_"
                    + bModel.getRetailerMasterBO()
                    .getRetailerID() + "_"
                    + bModel.userMasterHelper.getUserMasterBO().getUserid()
                    + "_file.pdf";


            String mFirstName = bModel.getOrderHeaderBO().getOrderImageName();

            boolean nFilesThere = bModel
                    .checkForNFilesInFolder(
                            FileUtils.photoFolderPath,
                            1, mFirstName);
            if (nFilesThere) {
                openPdfDeleteDialog(mFirstName);
            } else {
                String path = FileUtils.photoFolderPath + "/"
                        + attachedFilePath;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("application/pdf");
                intent.putExtra("path", path);
                startActivityForResult(intent, FILE_SELECTION);

            }


        }
        return super.onOptionsItemSelected(item);
    }

    private void callCalculatorApplication() {
        try {
            ArrayList<HashMap<String, Object>> items = new ArrayList<>();
            final PackageManager pm = getPackageManager();
            List<PackageInfo> packs = pm.getInstalledPackages(0);
            for (PackageInfo pack : packs) {
                if ("calcul".contains(pack.packageName.toLowerCase())) {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("appName", pack.applicationInfo.loadLabel(pm));
                    map.put("packageName", pack.packageName);
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
    }

    private void CollectionBeforeInvoiceCall() {
        bModel.downloadBankDetails();
        bModel.downloadBranchDetails();
        collectionHelper.loadPaymentModes();

        double minimumAmount;
        double creditBalance;

        if (bModel.getRetailerMasterBO().getCreditLimit() != 0) {
            creditBalance = bModel.getRetailerMasterBO().getCreditLimit() - collectionHelper.calculatePendingOSTAmount();
        } else
            creditBalance = 0;

        if (totalOrderValue < creditBalance)
            minimumAmount = 0;
        else
            minimumAmount = totalOrderValue - creditBalance;

        minimumAmount = SDUtil.convertToDouble(bModel.formatValue(minimumAmount));

        if (!isClicked) {
            isClicked = true;

            int paymentModeSize = collectionHelper.getPaymentModes()
                    .size();
            if (paymentModeSize > 0) {

                Intent intent = new Intent(OrderSummary.this, CollectionBeforeInvoiceActivity.class);

                Bundle bundle = new Bundle();

                bundle.putDouble("TotalInvoiceAmt", totalOrderValue);
                bundle.putDouble("OsAmount", minimumAmount);
                bundle.putDouble("CreditDalance", creditBalance);
                bundle.putParcelable("Collection", collectionbo);

                intent.putExtras(bundle);

                ActivityOptionsCompat opts = ActivityOptionsCompat.makeCustomAnimation(this, R.anim.zoom_enter, R.anim.hold);
                ActivityCompat.startActivityForResult(this, intent, COLLECTION_INVOICE_RESULT_CODE, opts.toBundle());

            } else {
                Toast.makeText(OrderSummary.this,
                        getResources().getString(R.string.data_not_mapped),
                        Toast.LENGTH_SHORT).show();
                isClicked = false;
            }
        }
    }

    private void editOrder() {

        isEditMode = true;
        discountHelper.clearSchemeFreeProduct(OrderSummary.this, mOrderedProductList);

        if (bModel.configurationMasterHelper.SHOW_DISCOUNT || bModel.configurationMasterHelper.IS_PRODUCT_DISCOUNT_BY_USER_ENTRY)
            discountHelper.clearDiscountQuantity();

        if (bModel.remarksHelper.getRemarksBO().getModuleCode() == null
                || bModel.remarksHelper.getRemarksBO().getModuleCode().length() == 0)
            bModel.remarksHelper.getRemarksBO().setModuleCode(StandardListMasterConstants.MENU_STK_ORD);

        bModel.setDoubleEdit_temp(false);


        bModel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
                .now(DateTimeUtils.TIME));

        Intent i;
        if (screenCode.equals(HomeScreenTwo.MENU_CATALOG_ORDER)) {
            i = new Intent(OrderSummary.this, CatalogOrder.class);
        } else {
            i = new Intent(OrderSummary.this, StockAndOrder.class);
        }
        i.putExtra("OrderFlag", "FromSummary");
        i.putExtra("ScreenCode", (screenCode == null ? "" : screenCode));

        if (bModel.getOrderHeaderBO() != null) {
            i.putExtra("tempPo",
                    (bModel.getOrderHeaderBO().getPO() == null ? "" : bModel
                            .getOrderHeaderBO().getPO()));
            i.putExtra("tempRemark",
                    (bModel.getOrderHeaderBO().getRemark() == null ? ""
                            : bModel.getOrderHeaderBO().getRemark()));
            i.putExtra("tempRField1",
                    (bModel.getOrderHeaderBO().getRField1() == null ? ""
                            : bModel.getOrderHeaderBO().getRField1()));
            i.putExtra("tempRField2",
                    (bModel.getOrderHeaderBO().getRField2() == null ? ""
                            : bModel.getOrderHeaderBO().getRField2()));
            i.putExtra("tempOrdImg",
                    (bModel.getOrderHeaderBO().getOrderImageName() == null ? ""
                            : bModel.getOrderHeaderBO().getOrderImageName()));
            i.putExtra("tempAddressId",
                    bModel.getOrderHeaderBO().getAddressID());
        }

        bModel.setOrderHeaderBO(null);
        startActivity(i);
        finish();
    }

    private void callAmountSplitUpScreen() {

        double cmy_disc = 0, dist_disc = 0;
        for (ProductMasterBO productMasterBO : mOrderedProductList) {
            cmy_disc = cmy_disc + productMasterBO.getCompanyTypeDiscount();
            dist_disc = dist_disc + productMasterBO.getDistributorTypeDiscount();
        }
        double cmyDiscount = cmy_disc + bModel.getRetailerMasterBO().getBillWiseCompanyDiscount();
        double distDiscount = dist_disc + bModel.getRetailerMasterBO().getBillWiseDistributorDiscount();

        if (amountSplitUpDialog == null) {
            amountSplitUpDialog = new AmountSplitUpDialog();
            amountSplitUpDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {

                    amountSplitUpDialog = null;
                }
            });
            Bundle args = new Bundle();
            args.putDouble("totalOrderValue", totalOrderValue);
            args.putDouble("cmy_disc", cmyDiscount);
            args.putDouble("dist_disc", distDiscount);
            args.putDouble("scheme_disc", totalSchemeDiscValue);
            args.putString("scheme_name", schemeNames);
            args.putString("disc_name", discountNames);
            args.putDouble("tax_value", taxValue);
            amountSplitUpDialog.setArguments(args);
            amountSplitUpDialog.show(getSupportFragmentManager(), "AmtSplitupDialog");
        }

    }


    @Override
    public void onDiscountDismiss(String result, int result1, int result2, int result3) {
        if (bModel.configurationMasterHelper.SHOW_STORE_WISE_DISCOUNT_DLG && bModel.configurationMasterHelper.BILL_WISE_DISCOUNT == 0) {

            final double totalValue = discountHelper.calculateBillWiseRangeDiscount(totalOrderValue, -1);
            text_totalOrderValue.setText(bModel.formatValue(totalValue));

        } else if (bModel.configurationMasterHelper.SHOW_TOTAL_DISCOUNT_EDITTEXT) {
            try {
                int f1 = 0;
                String qty = result;
                if ("".equals(qty)) {
                    qty = "0";
                }

                enteredDiscAmtOrPercent = SDUtil.convertToDouble(qty);

                if (enteredDiscAmtOrPercent != 0 && bModel.configurationMasterHelper.discountType == 1 && enteredDiscAmtOrPercent > 100) {
                    f1 = (int) (enteredDiscAmtOrPercent / 10);
                }

                String strDiscountAppliedValue = bModel.formatValue(getDiscountValue(SDUtil.convertToDouble(f1 + ""), totalOrderValue));

                text_totalOrderValue.setText(strDiscountAppliedValue);

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
    protected Dialog onCreateDialog(int id) {
        String delivery_date_txt;
        switch (id) {

            case DIALOG_ORDER_SAVED: {

                delivery_date_txt = button_deliveryDate.getText().toString();
                if (bModel.configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED) {
                    if (bModel.configurationMasterHelper.IS_SIH_VALIDATION) {
                        delivery_date_txt = "";
                    }
                } else if (bModel.configurationMasterHelper.IS_INVOICE || !bModel.configurationMasterHelper.SHOW_DELIVERY_DATE) {
                    delivery_date_txt = "";
                }

                AlertDialog.Builder builder2 = new AlertDialog.Builder(OrderSummary.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(getResources().getString(R.string.Orde_Saved))
                        .setMessage(getResources().getString(R.string.Order_id) + orderHelper.getOrderId() + "\n" +
                                (delivery_date_txt.equals("") ? "" : getResources().getString(R.string.delivery_date_is) + " " + delivery_date_txt))
                        .setPositiveButton(bModel.configurationMasterHelper.MOVE_NEXT_ACTIVITY ? getResources().getString(R.string.next) : getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {

                                        if (!orderHelper.isQuickCall) {
                                            Intent i = new Intent(
                                                    OrderSummary.this,
                                                    HomeScreenTwo.class);
                                            Bundle extras = getIntent().getExtras();
                                            if (extras != null) {
                                                i.putExtra("IsMoveNextActivity", bModel.configurationMasterHelper.MOVE_NEXT_ACTIVITY);
                                                i.putExtra("CurrentActivityCode", mCurrentActivityCode);
                                            }
                                            startActivity(i);
                                        }
                                        finish();

                                    }
                                });
                if (bModel.configurationMasterHelper.MOVE_NEXT_ACTIVITY)
                    builder2.setNegativeButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int whichButton) {
                            Intent i = new Intent(
                                    OrderSummary.this,
                                    HomeScreenTwo.class);
                            startActivity(i);
                            finish();
                        }
                    });


                bModel.applyAlertDialogTheme(builder2);

                break;
            }

            case DIALOG_ORDER_SAVED_WITH_PRINT_OPTION: {

                delivery_date_txt = button_deliveryDate.getText().toString();
                if (bModel.configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED) {
                    if (bModel.configurationMasterHelper.IS_SIH_VALIDATION) {
                        delivery_date_txt = "";
                    }
                } else if (bModel.configurationMasterHelper.IS_INVOICE || !bModel.configurationMasterHelper.SHOW_DELIVERY_DATE) {
                    delivery_date_txt = "";
                }

                AlertDialog.Builder builder1 = new AlertDialog.Builder(OrderSummary.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(
                                getResources().getString(
                                        R.string.order_saved_locally_order_id_is)
                                        + orderHelper.getOrderId())
                        .setMessage(delivery_date_txt.equals("") ? "" : getResources().getString(R.string.delivery_date_is) + " " + delivery_date_txt)
                        .setNegativeButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

                                        createOrderPrintFile(true, orderHelper.getOrderId());

                                        Intent i = new Intent(OrderSummary.this,
                                                HomeScreenTwo.class);
                                        Bundle extras = getIntent().getExtras();
                                        if (extras != null) {
                                            i.putExtra("IsMoveNextActivity", bModel.configurationMasterHelper.MOVE_NEXT_ACTIVITY);
                                            i.putExtra("CurrentActivityCode", mCurrentActivityCode);
                                        }
                                        startActivity(i);
                                        finish();
                                    }
                                })
                        .setPositiveButton(bModel.labelsMasterHelper
                                        .applyLabels((Object) "Ord_Sum_Print_Order") != null ? bModel.labelsMasterHelper
                                        .applyLabels((Object) "Ord_Sum_Print_Order") :
                                        getResources().getString(R.string.print_order),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

                                        if (bModel.configurationMasterHelper.SHOW_ZEBRA_TITAN
                                                || bModel.configurationMasterHelper.SHOW_ZEBRA_UNIPAL) {

                                            showDialog(DIALOG_NUMBER_OF_PRINTS_ORDER);
                                        } else {
                                            printOrder();
                                        }
                                    }
                                });


                bModel.applyAlertDialogTheme(builder1);
                break;
            }

            case DIALOG_DELETE_STOCK_AND_ORDER: {

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
                                        if (bModel.getOrderHeaderBO() != null) {
                                            bModel.getOrderHeaderBO().setIsSignCaptured(false);
                                            if (bModel.getOrderHeaderBO().getSignatureName() != null)
                                                bModel.synchronizationHelper.deleteFiles(
                                                        PHOTO_PATH, bModel.getOrderHeaderBO().getSignatureName());
                                        }
                                        if (!bModel.hasStockInOrder())
                                            bModel.deleteModuleCompletion("MENU_STK_ORD");
                                        // clear scheme free products
                                        discountHelper.clearSchemeFreeProduct(OrderSummary.this, mOrderedProductList);

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
                                        if (bModel.getOrderHeaderBO() != null) {
                                            // clear scheme free products
                                            bModel.getOrderHeaderBO().setIsSignCaptured(false);

                                            if (bModel.getOrderHeaderBO().getSignatureName() != null)
                                                bModel.synchronizationHelper.deleteFiles(
                                                        PHOTO_PATH, bModel.getOrderHeaderBO().getSignatureName());
                                        }
                                        discountHelper.clearSchemeFreeProduct(OrderSummary.this, mOrderedProductList);
                                        bModel.deleteModuleCompletion("MENU_STK_ORD");
                                        new MyThread(OrderSummary.this,
                                                DataMembers.DELETE_STOCK_AND_ORDER).start();
                                    }
                                });
                bModel.applyAlertDialogTheme(builder);
                break;
            }


            case DIALOG_DELETE_ONLY_ORDER: {
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
                                        if (bModel.getOrderHeaderBO() != null) {
                                            bModel.getOrderHeaderBO().setIsSignCaptured(false);
                                            if (bModel.getOrderHeaderBO().getSignatureName() != null)
                                                bModel.synchronizationHelper.deleteFiles(
                                                        PHOTO_PATH, bModel.getOrderHeaderBO().getSignatureName());
                                        }
                                        customProgressDialog(build, getResources().getString(R.string.deleting_order));
                                        alertDialog = build.create();
                                        alertDialog.show();
                                        bModel.deleteModuleCompletion("MENU_STK_ORD");
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
                bModel.applyAlertDialogTheme(builder4);
                break;
            }


            case DIALOG_INVOICE_SAVED: {
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

                                        if (bModel.configurationMasterHelper.printCount > 0) {
                                            showDialog(DIALOG_NUMBER_OF_PRINTS_INVOICE);
                                        } else {
                                            printInvoice();
                                        }
                                    }
                                })

                        .setNegativeButton(
                                getResources().getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

                                        if (bModel.configurationMasterHelper.SHOW_PRINT_ORDER)
                                            createOrderPrintFile(true, orderHelper.getOrderId());

                                        bModel.productHelper.clearOrderTableChecked();
                                        Intent i = new Intent(OrderSummary.this, HomeScreenTwo.class);
                                        startActivity(i);
                                        finish();
                                    }
                                });
                bModel.applyAlertDialogTheme(builder9);
                break;
            }

            case DIALOG_NUMBER_OF_PRINTS_ORDER: {

                AlertDialog.Builder builder11 = new AlertDialog.Builder(OrderSummary.this)
                        .setTitle("Print Count")
                        .setSingleChoiceItems(bModel.printHelper.getPrintCountArray(), 0, null)
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
                bModel.applyAlertDialogTheme(builder11);

                break;

            }

            case DIALOG_NEGATIVE_INVOICE_CHECK: {
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
                bModel.applyAlertDialogTheme(builder5);
                break;
            }

            case DIALOG_SIGNATURE_AVAILABLE: {
                AlertDialog.Builder builder7 = new AlertDialog.Builder(OrderSummary.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(
                                "Signature Already taken.Do you want to delete and retake?")
                        .setPositiveButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        if (bModel.getOrderHeaderBO() != null) {
                                            bModel.getOrderHeaderBO().setIsSignCaptured(false);
                                            if (bModel.getOrderHeaderBO().getSignatureName() != null)
                                                bModel.synchronizationHelper.deleteFiles(
                                                        PHOTO_PATH, bModel.getOrderHeaderBO().getSignatureName());
                                        }
                                        Intent i = new Intent(OrderSummary.this,
                                                CaptureSignatureActivity.class);
                                        i.putExtra("fromModule", "ORDER");
                                        startActivity(i);
                                        bModel.configurationMasterHelper.setSignatureTitle("Signature");
                                        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                                        //finish();
                                    }
                                })
                        .setNegativeButton(
                                getResources().getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                    }
                                });
                bModel.applyAlertDialogTheme(builder7);
                break;
            }

            case DIALOG_DELIVERY_DATE_PICKER: {

                Calendar maxCalendar = Calendar.getInstance();
                if (bModel.configurationMasterHelper.DEFAULT_NUMBER_OF_DAYS_TO_DELIVER_ORDER != 0) {
                    mCalendar.setTimeInMillis(System.currentTimeMillis() - 1000);
                    if (bModel.configurationMasterHelper.MIN_NUMBER_OF_DAYS_ALLOWED_TO_DELIVER > 0) {
                        mCalendar.add(Calendar.DAY_OF_MONTH, bModel.configurationMasterHelper.MIN_NUMBER_OF_DAYS_ALLOWED_TO_DELIVER);
                    } else {
                        mCalendar.add(Calendar.DAY_OF_MONTH, 1);
                    }
                    if (bModel.configurationMasterHelper.MAX_NUMBER_OF_DAYS_ALLOWED_TO_DELIVER > 0) {
                        maxCalendar.add(Calendar.DAY_OF_YEAR, bModel.configurationMasterHelper.MAX_NUMBER_OF_DAYS_ALLOWED_TO_DELIVER);
                    }
                }

                int year = mCalendar.get(Calendar.YEAR);
                int month = mCalendar.get(Calendar.MONTH);
                int day = mCalendar.get(Calendar.DAY_OF_MONTH);

                MyDatePickerDialog dialog = new MyDatePickerDialog(this, R.style.DatePickerDialogStyle,
                        mDeliverDatePickerListener, year, month, day);
                dialog.setPermanentTitle(getResources().getString(R.string.choose_date));
                dialog.getDatePicker().setMinDate(mCalendar.getTimeInMillis());
                if (bModel.configurationMasterHelper.DEFAULT_NUMBER_OF_DAYS_TO_DELIVER_ORDER != 0 &&
                        bModel.configurationMasterHelper.MAX_NUMBER_OF_DAYS_ALLOWED_TO_DELIVER > 0) {
                    dialog.getDatePicker().setMaxDate(maxCalendar.getTimeInMillis());
                }
                return dialog;
            }


            default:
                break;
        }
        return null;
    }


    public void onClick(View viewClicked) {

        bModel = (BusinessModel) getApplicationContext();
        bModel.setContext(this);

        if (viewClicked == button_deliveryDate)
            showDialog(DIALOG_DELIVERY_DATE_PICKER);
        else if (viewClicked == button_order) {
            saveOrder();
        } else if (viewClicked == button_invoice) {
            saveInvoice();
        } else if (viewClicked == imageView_amountSplitUp) {
            callAmountSplitUpScreen();
        }

    }

    private void saveOrder() {

        isFromOrder = true;

        if (bModel.configurationMasterHelper.IS_SHOW_ORDERING_SEQUENCE && mSortedList != null)
            orderHelper.setSortedOrderedProducts(mSortedList);


        if (!isClick) {

            isClick = true;

            if (mOrderedProductList.size() > 0) {

                if (isTaxRequiredforAllProducts()) return;

                if ((bModel.configurationMasterHelper.IS_SHOW_ONLY_INDICATIVE_ORDER || bModel.configurationMasterHelper.IS_SHOW_ORDER_REASON) && !orderHelper.isReasonProvided(mOrderedProductList)) {

                    Intent intent = new Intent(OrderSummary.this, IndicativeOrderReasonActivity.class);
                    ActivityOptionsCompat opts = ActivityOptionsCompat.makeCustomAnimation(this, R.anim.zoom_enter, R.anim.hold);
                    ActivityCompat.startActivityForResult(this, intent, INDICATIVE_ORDER_REASON_RESULT_CODE, opts.toBundle());

                    isClick = false;

                } else {

                    totalOrderValue = totalOrderValue - getDiscountValue(enteredDiscAmtOrPercent, totalOrderValue);
                    if (bModel.getOrderHeaderBO() != null) {
                        bModel.getOrderHeaderBO().setOrderValue(totalOrderValue);
                        bModel.getOrderHeaderBO().setDiscount(enteredDiscAmtOrPercent);
                        bModel.getOrderHeaderBO().setDiscountId(0);
                        bModel.getOrderHeaderBO().setIsCompanyGiven(0);
                        bModel.getOrderHeaderBO().setLinesPerCall(SDUtil.convertToInt((String) text_LPC.getText()));
                        if (!button_deliveryDate.getText().toString().trim().equals("")) {
                            bModel.getOrderHeaderBO().setDeliveryDate(DateTimeUtils.convertToServerDateFormat(button_deliveryDate.getText().toString(),
                                    ConfigurationMasterHelper.outDateFormat));
                        } else {
                            bModel.getOrderHeaderBO().setDeliveryDate(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL));
                        }
                        signatureName = bModel.getOrderHeaderBO().getSignatureName();
                    }

                    //Removed as per the JIRA changes
//                    if (bModel.configurationMasterHelper.SHOW_SALES_RETURN_IN_ORDER
//                            && bModel.retailerMasterBO.getRpTypeCode() != null && "CASH".equals(bModel.retailerMasterBO.getRpTypeCode())
//                            && bModel.getOrderHeaderBO().getOrderValue() < orderHelper.getTotalReturnValue(mOrderedProductList)) {
//                        Toast.makeText(this, getResources().getString(R.string.sales_return_value_exceeds_order_value), Toast.LENGTH_LONG).show();
//                        isClick = false;
//                        return;
//                    }
                    if (bModel.configurationMasterHelper.SHOW_SALES_RETURN_IN_ORDER
                            && bModel.getOrderHeaderBO().getOrderValue() < orderHelper.getTotalReturnValue(mOrderedProductList)) {
                        Toast.makeText(this, getResources().getString(R.string.sales_return_value_exceeds_order_value), Toast.LENGTH_LONG).show();
                        isClick = false;
                        return;
                    }

                    // Don't write any code  after this dialog.. because it is just a confirmation dialog
                    orderConfirmationDialog = new OrderConfirmationDialog(this, false, mOrderedProductList, totalOrderValue);
                    orderConfirmationDialog.show();
                    orderConfirmationDialog.setCancelable(false);

                }

            } else {
                Toast.makeText(
                        this,
                        getResources().getString(
                                R.string.no_products_exists),
                        Toast.LENGTH_SHORT).show();
                isClick = false;
            }
        }
    }

    private boolean isTaxRequiredforAllProducts() {
        if (bModel.configurationMasterHelper.IS_INVOICE && ((bModel.configurationMasterHelper.IS_GST || bModel.configurationMasterHelper.IS_GST_HSN) && !orderHelper.isTaxAvailableForAllOrderedProduct(mOrderedProductList))) {
            // If GST enabled then, every ordered product should have tax
            bModel.showAlert(
                    getResources()
                            .getString(
                                    R.string.tax_not_availble_for_some_product),
                    0);
            isClick = false;
            return true;
        }
        return false;
    }

    private void saveInvoice() {

        isFromOrder = false;

        if (bModel.configurationMasterHelper.IS_SHOW_ORDERING_SEQUENCE && mSortedList != null)
            orderHelper.setSortedOrderedProducts(mSortedList);


        if (isTaxRequiredforAllProducts()) return;

        if (!isClick) {
            isClick = true;

            if (bModel.configurationMasterHelper.IS_SIH_VALIDATION && !orderHelper.isStockAvailableToDeliver(mOrderedProductList, getApplicationContext())) {
                Toast.makeText(
                        this,
                        getResources()
                                .getString(
                                        R.string.stock_not_available_to_deliver),
                        Toast.LENGTH_SHORT).show();
                isClick = false;
                return;
            }

            if (bModel.configurationMasterHelper.IS_VALIDATE_NEGATIVE_INVOICE) {
                if (totalOrderValue < 0) {
                    showDialog(DIALOG_NEGATIVE_INVOICE_CHECK);
                    return;
                }
            }

            if (bModel.configurationMasterHelper.IS_TAX_APPLIED_VALIDATION) {
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

            if (bModel.configurationMasterHelper.SHOW_COLLECTION_BEFORE_INVOICE) {

                double pendingAmount;
                double collectedAmount = 0;

                pendingAmount = bModel.getRetailerMasterBO().getCreditLimit() - collectionHelper.calculatePendingOSTAmount();

                if (collectionbo.getCashamt() > 0
                        || collectionbo.getChequeamt() > 0 || collectionbo.getCreditamt() > 0) {
                    collectedAmount = collectionbo.getCashamt()
                            + collectionbo.getChequeamt() + collectionbo.getCreditamt();
                }

                collectedAmount = SDUtil.convertToDouble(bModel.formatValue(collectedAmount));
                pendingAmount = collectedAmount + pendingAmount;
                pendingAmount = SDUtil.convertToDouble(bModel.formatValue(pendingAmount));

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

            totalOrderValue -= getDiscountValue(enteredDiscAmtOrPercent, totalOrderValue);
            if (bModel.getOrderHeaderBO() != null) {
                bModel.getOrderHeaderBO().setOrderValue(totalOrderValue);
                bModel.getOrderHeaderBO().setDiscount(enteredDiscAmtOrPercent);
                bModel.getOrderHeaderBO().setDiscountId(0);
                bModel.getOrderHeaderBO().setIsCompanyGiven(0);
                bModel.getOrderHeaderBO().setLinesPerCall(SDUtil.convertToInt((String) text_LPC.getText()));
                if (!button_deliveryDate.getText().toString().trim().equals("")) {
                    bModel.getOrderHeaderBO().setDeliveryDate(DateTimeUtils.convertToServerDateFormat(button_deliveryDate.getText().toString(),
                            ConfigurationMasterHelper.outDateFormat));
                } else {
                    bModel.getOrderHeaderBO().setDeliveryDate(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL));
                }
                signatureName = bModel.getOrderHeaderBO().getSignatureName();
            }

            if (!mOrderedProductList.isEmpty()) {

                if (orderHelper.isAllScanned() || !bModel.configurationMasterHelper.SHOW_SERIAL_NO_SCREEN) {

                    if (orderHelper.hasOrder(mOrderedProductList)) {

                        if ((bModel.configurationMasterHelper.IS_SHOW_ONLY_INDICATIVE_ORDER || bModel.configurationMasterHelper.IS_SHOW_ORDER_REASON) && !orderHelper.isReasonProvided(mOrderedProductList)) {

                            Intent intent = new Intent(OrderSummary.this, IndicativeOrderReasonActivity.class);
                            ActivityOptionsCompat opts = ActivityOptionsCompat.makeCustomAnimation(this, R.anim.zoom_enter, R.anim.hold);
                            ActivityCompat.startActivityForResult(this, intent, INDICATIVE_ORDER_REASON_RESULT_CODE, opts.toBundle());

                            isClick = false;

                        } else {

                            if (bModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION
                                    && bModel.configurationMasterHelper.IS_SIH_VALIDATION
                                    && bModel.configurationMasterHelper.IS_INVOICE) {
                                bModel.batchAllocationHelper
                                        .loadFreeProductBatchList();
                            }

                            orderHelper.invoiceDiscount = Double.toString(enteredDiscAmtOrPercent);

                            //Removed as per the JIRA changes
//                            if (bModel.configurationMasterHelper.SHOW_SALES_RETURN_IN_ORDER
//                                    && bModel.getOrderHeaderBO().getOrderValue() < orderHelper.getTotalReturnValue(mOrderedProductList)) {
//                                Toast.makeText(this, getResources().getString(R.string.sales_return_value_exceeds_order_value), Toast.LENGTH_LONG).show();
//                                isClick = false;
//                                return;
//                            }

                            // Don't write any code  after this dialog.. because it is just a confirmation dialog
                            orderConfirmationDialog = new OrderConfirmationDialog(this, true, mOrderedProductList, totalOrderValue);
                            orderConfirmationDialog.show();
                            orderConfirmationDialog.setCancelable(false);


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
                bModel.showAlert(
                        getResources().getString(R.string.no_products_exists),
                        0);
            }
        }
    }


    public Handler getHandler() {
        return handler;
    }

    private double getDiscountValue(double discount, double orderValue) {

        double discountValue = 0;
        try {
            if (bModel.configurationMasterHelper.discountType == 1) {
                if (discount > 100)
                    discount = 100;
                discountValue = (orderValue / 100) * discount;

            } else if (bModel.configurationMasterHelper.discountType == 2) {
                discountValue = discount;

            }
            if (bModel.getOrderHeaderBO() != null && !bModel.configurationMasterHelper.SHOW_STORE_WISE_DISCOUNT_DLG)
                bModel.getOrderHeaderBO().setBillLevelDiscountValue(discountValue);
        } catch (Exception e) {
            Commons.printException(e);
        }
        return discountValue;
    }


    private double applyDiscountMaxValidation(double discount) {
        try {
            if (bModel.configurationMasterHelper.discountType == 1) {
                if (discount > 100)
                    discount = 100;

            }
        } catch (Exception e) {
            Commons.printException(e);
        }

        return discount;
    }


    private void PrintData() {
        try {
            ZFPLib zfp = mChatService.zfplib;
            zfp.openFiscalBon(1, "0000", false, false, false);

            boolean mGoldenStore = false;
            for (ProductMasterBO sku : bModel.productHelper.getProductMaster()) {
                double vatAmount = 0.0;
                int taxSize = sku.getTaxes().size();
                for (int ii = 0; ii < taxSize; ii++) {
                    vatAmount = sku.getTaxes().get(ii).getTaxRate();
                }

                if (sku.getOrderedPcsQty() > 0 || sku.getOrderedCaseQty() > 0
                        || sku.getOrderedOuterQty() > 0) {
                    float pieceCount = (sku.getOrderedCaseQty() * sku
                            .getCaseSize())
                            + (sku.getOrderedPcsQty())
                            + (sku.getOrderedOuterQty() * sku.getOutersize());

                    float taxDiscount = (float) vatAmount;
                    double percent = 0;
                    if (sku.isPromo()) {
                        percent = sku.getMschemeper();
                    }

                    float goldenStore = 0;
                    if (bModel.configurationMasterHelper.SHOW_GOLD_STORE_DISCOUNT
                            && bModel.productHelper
                            .isGoldenStoreInCurrentandLastVisit()) {

                        goldenStore = (float) bModel.productHelper
                                .applyGoldStoreLineDiscount();
                        mGoldenStore = true;
                    }

                    float discount = (float) percent + goldenStore;

                    char taxGroup;
                    if (Math.round(taxDiscount) == 16) {
                        taxGroup = '1';
                    } else if (Math.round(taxDiscount) == 18) {
                        taxGroup = '2';
                    } else {
                        taxGroup = '0';
                    }

                    zfp.sellFree(sku.getProductShortName(), taxGroup,
                            sku.getSrp(), pieceCount, -discount);
                }
            }
            double sum = zfp
                    .calcIntermediateSum(false, false, false, 0.0f, '0');
            zfp.payment(sum, 0, false);

            if (mGoldenStore) {
                zfp.printText("**Golden store applied**", 2);
            }
            zfp.closeFiscalBon();

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }


    private void showFocusProductAlert() {
        int focusProductCount = 0;
        int totalFocusProductCount = 0;

        for (ProductMasterBO productBO : bModel.productHelper.getProductMaster()) {
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

        bModel.showAlert(msg, 0);
    }


    @Override
    public void save(boolean isInvoice) {
        try {
            if (orderConfirmationDialog != null)
                orderConfirmationDialog.dismiss();

            if (isInvoice) {

                if (bModel.configurationMasterHelper.IS_INVOICE) {
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
                if (bModel.configurationMasterHelper.IS_FOCUSBRAND_COUNT_IN_REPORT || bModel.configurationMasterHelper.IS_MUSTSELL_COUNT_IN_REPORT)
                    orderHelper.getFocusAndMustSellOrderedProducts(mOrderedProductList);

                //Adding accumulation scheme free products to the last ordered product list, so that it will listed on print
                orderHelper.updateOffInvoiceSchemeInProductOBJ(mOrderedProductList, totalOrderValue, getApplicationContext());


                new MyThread(this, DataMembers.SAVEINVOICE).start();
            } else {

                build = new AlertDialog.Builder(OrderSummary.this);

                customProgressDialog(build, getResources().getString(R.string.saving_new_order));
                alertDialog = build.create();
                alertDialog.show();
                if (bModel.configurationMasterHelper.IS_FOCUSBRAND_COUNT_IN_REPORT || bModel.configurationMasterHelper.IS_MUSTSELL_COUNT_IN_REPORT)
                    orderHelper.getFocusAndMustSellOrderedProducts(mOrderedProductList);

                if (orderHelper.hasOrder(mOrderedProductList)) {

                    if (bModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION
                            && bModel.configurationMasterHelper.IS_SIH_VALIDATION
                            && bModel.configurationMasterHelper.IS_INVOICE) {
                        bModel.batchAllocationHelper
                                .loadFreeProductBatchList();
                    }


                    orderHelper.invoiceDiscount = Double.toString(enteredDiscAmtOrPercent);

                    new MyThread(OrderSummary.this,
                            DataMembers.SAVEORDERANDSTOCK).start();
                    if (!orderHelper.isQuickCall)
                        bModel.saveModuleCompletion("MENU_STK_ORD", true);


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

    private class ProductExpandableAdapter extends BaseExpandableListAdapter {

        @Override
        public Object getChild(int arg0, int arg1) {
            return null;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
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
                holder.text_productName = row
                        .findViewById(R.id.PRODUCTNAME);
                holder.pcsQty = row.findViewById(R.id.P_QUANTITY);
                holder.caseQty = row.findViewById(R.id.C_QUANTITY);
                holder.tw_srp = row.findViewById(R.id.MRP);
                holder.text_total = row.findViewById(R.id.TOTAL);
                holder.outerQty = row.findViewById(R.id.OC_QUANTITY);
                holder.weight = row.findViewById(R.id.tv_weight);
                holder.foc = row.findViewById(R.id.FOC_QUANTITY);
                holder.shelfCaseQty = row.findViewById(R.id.sc_quantity);
                holder.shelfOuterQty = row.findViewById(R.id.sho_quantity);
                holder.shelfPieceQty = row.findViewById(R.id.sp_quantity);

                holder.salesReturn = row.findViewById(R.id.stock_and_order_listview_sales_return_qty);
                holder.rep_cs = row.findViewById(R.id.rep_case);
                holder.rep_ou = row.findViewById(R.id.rep_outer);
                holder.rep_pcs = row.findViewById(R.id.rep_pcs);

                holder.text_productName.setTypeface(FontUtils.getProductNameFont(OrderSummary.this));
                holder.text_total.setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.MEDIUM));

                (row.findViewById(R.id.view_dotted_line)).setLayerType(View.LAYER_TYPE_SOFTWARE, null);

                holder.cbSeparateBill = row.findViewById(R.id.cbSeparateBill);
                holder.cbSeparateBill.setVisibility(View.GONE);

                if (!"MENU_ORDER".equals(screenCode) && bModel.configurationMasterHelper.SHOW_STOCK_IN_SUMMARY) {

                    if (stockCheckHelper.SHOW_STOCK_SC) {
                        (row.findViewById(R.id.llShelfCase)).setVisibility(View.VISIBLE);
                        ((TextView) row.findViewById(R.id.shelfCaseTitle)).setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.MEDIUM));
                        holder.shelfCaseQty.setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.MEDIUM));
                        try {
                            if (bModel.labelsMasterHelper.applyLabels(row.findViewById(
                                    R.id.shelfCaseTitle).getTag()) != null)
                                ((TextView) row.findViewById(R.id.shelfCaseTitle))
                                        .setText(bModel.labelsMasterHelper
                                                .applyLabels(row.findViewById(
                                                        R.id.shelfCaseTitle)
                                                        .getTag()));
                        } catch (Exception e) {
                            Commons.printException(e);
                        }

                    }
                    if (stockCheckHelper.SHOW_SHELF_OUTER) {
                        (row.findViewById(R.id.llShelfOuter)).setVisibility(View.VISIBLE);
                        ((TextView) row.findViewById(R.id.shelfOuterTitle)).setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.MEDIUM));
                        holder.shelfOuterQty.setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.MEDIUM));
                        try {
                            if (bModel.labelsMasterHelper.applyLabels(row.findViewById(
                                    R.id.shelfOuterTitle).getTag()) != null)
                                ((TextView) row.findViewById(R.id.shelfOuterTitle))
                                        .setText(bModel.labelsMasterHelper
                                                .applyLabels(row.findViewById(
                                                        R.id.shelfOuterTitle)
                                                        .getTag()));
                        } catch (Exception e) {
                            Commons.printException(e);
                        }

                    }

                    if (stockCheckHelper.SHOW_STOCK_SP) {
                        (row.findViewById(R.id.llShelfPiece)).setVisibility(View.VISIBLE);
                        ((TextView) row.findViewById(R.id.shelfPcsTitle)).setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.MEDIUM));
                        holder.shelfPieceQty.setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.MEDIUM));
                        try {
                            if (bModel.labelsMasterHelper.applyLabels(row.findViewById(
                                    R.id.shelfPcsTitle).getTag()) != null)
                                ((TextView) row.findViewById(R.id.shelfPcsTitle))
                                        .setText(bModel.labelsMasterHelper
                                                .applyLabels(row.findViewById(
                                                        R.id.shelfPcsTitle)
                                                        .getTag()));
                        } catch (Exception e) {
                            Commons.printException(e);
                        }
                    }
                }
                // On/Off order case and pce
                if (!bModel.configurationMasterHelper.SHOW_ORDER_CASE) {
                    (row.findViewById(R.id.llCase)).setVisibility(View.GONE);
                } else {
                    ((TextView) row.findViewById(R.id.caseTitle)).setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.MEDIUM));
                    holder.caseQty.setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.MEDIUM));
                    try {
                        if (bModel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.caseTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.caseTitle))
                                    .setText(bModel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.caseTitle).getTag()));
                    } catch (Exception e) {
                        Commons.printException(" " + e);
                    }
                }


                if (!bModel.configurationMasterHelper.SHOW_ORDER_PCS) {
                    (row.findViewById(R.id.llPiece)).setVisibility(View.GONE);
                } else {
                    ((TextView) row.findViewById(R.id.pcsTitle)).setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.MEDIUM));
                    holder.pcsQty.setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.MEDIUM));
                    try {
                        if (bModel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.pcsTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.pcsTitle))
                                    .setText(bModel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.pcsTitle).getTag()));
                    } catch (Exception e) {
                        Commons.printException(" " + e);
                    }
                }
                if (!bModel.configurationMasterHelper.SHOW_OUTER_CASE) {
                    (row.findViewById(R.id.llOuter)).setVisibility(View.GONE);
                } else {
                    ((TextView) row.findViewById(R.id.outercaseTitle)).setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.MEDIUM));
                    holder.outerQty.setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.MEDIUM));
                    try {
                        if (bModel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.outercaseTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.outercaseTitle))
                                    .setText(bModel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.outercaseTitle).getTag()));
                    } catch (Exception e) {
                        Commons.printException(" " + e);
                    }
                }
                if (!bModel.configurationMasterHelper.SHOW_ORDER_WEIGHT) {
                    (row.findViewById(R.id.llShelfWeight)).setVisibility(View.GONE);
                } else {
                    ((TextView) row.findViewById(R.id.weighttitle)).setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.MEDIUM));
                    holder.caseQty.setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.MEDIUM));
                    try {
                        if (bModel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.weighttitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.weighttitle))
                                    .setText(bModel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.weighttitle).getTag()));
                    } catch (Exception e) {
                        Commons.printException(" " + e);
                    }
                }
                if (!bModel.configurationMasterHelper.SHOW_STK_ORD_SRP) {
                    (row.findViewById(R.id.llSrp)).setVisibility(View.GONE);
                } else {
                    ((TextView) row.findViewById(R.id.srpTitle)).setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.MEDIUM));
                    holder.tw_srp.setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.MEDIUM));
                    try {
                        if (bModel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.srpTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.srpTitle))
                                    .setText(bModel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.srpTitle).getTag()));
                    } catch (Exception e) {
                        Commons.printException(" " + e);
                    }
                }

                if (!bModel.configurationMasterHelper.SHOW_SALES_RETURN_IN_ORDER)
                    (row.findViewById(R.id.llStkRtEdit)).setVisibility(View.GONE);
                else {
                    try {
                        ((TextView) row.findViewById(R.id.stkRtTitle)).setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.LIGHT));
                        holder.salesReturn.setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.LIGHT));
                        if (bModel.labelsMasterHelper.applyLabels(row.findViewById(R.id.stkRtTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.stkRtTitle))
                                    .setText(bModel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.stkRtTitle).getTag()));
                    } catch (Exception e) {
                        Commons.printException(e + "");
                    }
                }
                if (!bModel.configurationMasterHelper.SHOW_REPLACED_QTY_CS)
                    (row.findViewById(R.id.llRepCase)).setVisibility(View.GONE);
                else {
                    ((TextView) row.findViewById(R.id.rep_caseTitle)).setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.LIGHT));
                    holder.rep_cs.setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.LIGHT));
                    try {
                        if (bModel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.rep_caseTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.rep_caseTitle))
                                    .setText(bModel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.rep_caseTitle)
                                                    .getTag()));
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                }
                if (!bModel.configurationMasterHelper.SHOW_REPLACED_QTY_OU)
                    (row.findViewById(R.id.llRepOu)).setVisibility(View.GONE);
                else {
                    ((TextView) row.findViewById(R.id.rep_outerTitle)).setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.LIGHT));
                    holder.rep_ou.setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.LIGHT));
                    try {
                        if (bModel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.rep_outerTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.rep_outerTitle))
                                    .setText(bModel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.rep_outerTitle)
                                                    .getTag()));
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                }
                if (!bModel.configurationMasterHelper.SHOW_REPLACED_QTY_PC)
                    (row.findViewById(R.id.llRepPc)).setVisibility(View.GONE);
                else {
                    ((TextView) row.findViewById(R.id.rep_pcsTitle)).setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.LIGHT));
                    holder.rep_pcs.setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.LIGHT));
                    try {
                        if (bModel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.rep_pcsTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.rep_pcsTitle))
                                    .setText(bModel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.rep_pcsTitle)
                                                    .getTag()));
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                }

                if (!bModel.configurationMasterHelper.SHOW_FOC) {
                    (row.findViewById(R.id.llfoc)).setVisibility(View.GONE);
                } else {
                    ((TextView) row.findViewById(R.id.focTitle)).setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.LIGHT));
                    holder.foc.setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.MEDIUM));
                    try {
                        if (bModel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.focTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.focTitle))
                                    .setText(bModel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.focTitle).getTag()));
                    } catch (Exception e) {
                        Commons.printException(" " + e);
                    }
                }
                holder.rl_oos = row.findViewById(R.id.rl_oos);
                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            SchemeProductBO productBO = mOrderedProductList.get(groupPosition)
                    .getSchemeProducts().get(childPosition);

            holder.text_productName.setText(productBO.getProductName());
            holder.productBO = bModel.productHelper
                    .getProductMasterBOById(productBO.getProductId());

            holder.shelfCaseQty.setText(String.valueOf(holder.productBO.getLocations().get(bModel.productHelper.getmSelectedLocationIndex()).getShelfCase()));
            holder.shelfOuterQty.setText(String.valueOf(holder.productBO.getLocations().get(bModel.productHelper.getmSelectedLocationIndex()).getShelfOuter()));
            holder.shelfPieceQty.setText(String.valueOf(holder.productBO.getLocations().get(bModel.productHelper.getmSelectedLocationIndex()).getShelfPiece()));

            if (holder.productBO != null) {
                if (holder.productBO.getCaseUomId() == productBO.getUomID()
                        && holder.productBO.getCaseUomId() != 0) {
                    // case wise free quantity update

                    holder.caseQty
                            .setText(String.valueOf(productBO.getQuantitySelected()));
                    holder.pcsQty.setText(String.valueOf(0));
                    holder.outerQty.setText(String.valueOf(0));
                } else if (holder.productBO.getOuUomid() == productBO
                        .getUomID() && holder.productBO.getOuUomid() != 0) {
                    // outer wise free quantity update
                    holder.outerQty.setText(String.valueOf(productBO.getQuantitySelected()));
                    holder.pcsQty.setText(String.valueOf(0));
                    holder.caseQty.setText(String.valueOf(0));

                    //If only other UOMs are enabled but free is in outer then showing it in screen without considering UOM config.
                    if (productBO.getQuantitySelected() > 0)
                        row.findViewById(R.id.llOuter).setVisibility(View.VISIBLE);
                    else row.findViewById(R.id.llOuter).setVisibility(View.GONE);

                } else {

                    //If only other UOMs are enabled but free is in pieces then showing it in screen without considering UOM config.
                    if (productBO.getQuantitySelected() > 0)
                        row.findViewById(R.id.llPiece).setVisibility(View.VISIBLE);
                    else row.findViewById(R.id.llPiece).setVisibility(View.GONE);

                    holder.pcsQty.setText(String.valueOf(productBO.getQuantitySelected()));
                    holder.caseQty.setText(String.valueOf(0));
                    holder.outerQty.setText(String.valueOf(0));
                }
            }

            holder.tw_srp.setText(SDUtil.roundIt(0, 2));
            holder.foc.setText(SDUtil.roundIt(0, 2));

            if (!bModel.configurationMasterHelper.SHOW_STK_ORD_SRP) {
                holder.text_total.setVisibility(View.GONE);
            } else {
                holder.text_total.setText("0");
            }
            if (bModel.configurationMasterHelper.IS_SHOW_OOS && holder.productBO.getWSIH() == 0)
                holder.rl_oos.setVisibility(View.VISIBLE);
            else
                holder.rl_oos.setVisibility(View.GONE);

            return row;
        }

        @Override
        public int getChildrenCount(int groupPosition) {

            if (mOrderedProductList.get(groupPosition).isPromo()
                    && (mOrderedProductList.get(groupPosition).getSchemeProducts() != null
                    && mOrderedProductList.get(groupPosition).getSchemeProducts().size() > 0)) {

                if (!SchemeDetailsMasterHelper.getInstance(getApplicationContext()).getSchemeById().get(mOrderedProductList.get(groupPosition).getSchemeProducts().get(0).getSchemeId()).isOffScheme()) {
                    return mOrderedProductList.get(groupPosition).getSchemeProducts().size();
                }
            }
            return 0;
        }

        @Override
        public Object getGroup(int groupPosition) {
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
                holder.text_productName = row
                        .findViewById(R.id.PRODUCTNAME);
                holder.pcsQty = row.findViewById(R.id.P_QUANTITY);
                holder.caseQty = row.findViewById(R.id.C_QUANTITY);
                holder.tw_srp = row.findViewById(R.id.MRP);
                holder.text_total = row.findViewById(R.id.TOTAL);
                holder.outerQty = row.findViewById(R.id.OC_QUANTITY);
                holder.weight = row.findViewById(R.id.tv_weight);

                holder.shelfCaseQty = row.findViewById(R.id.sc_quantity);
                holder.shelfOuterQty = row.findViewById(R.id.sho_quantity);
                holder.shelfPieceQty = row.findViewById(R.id.sp_quantity);
                holder.foc = row.findViewById(R.id.FOC_QUANTITY);
                holder.cbSeparateBill = row.findViewById(R.id.cbSeparateBill);

                holder.salesReturn = row.findViewById(R.id.stock_and_order_listview_sales_return_qty);
                holder.rep_cs = row.findViewById(R.id.rep_case);
                holder.rep_ou = row.findViewById(R.id.rep_outer);
                holder.rep_pcs = row.findViewById(R.id.rep_pcs);
                holder.rl_oos = row.findViewById(R.id.rl_oos);

                holder.text_productName.setMaxLines(bModel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);
                holder.text_productName.setTypeface(FontUtils.getProductNameFont(OrderSummary.this));
                (row.findViewById(R.id.view_dotted_line)).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                holder.text_total.setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.MEDIUM));

                if (!"MENU_ORDER".equals(screenCode) && bModel.configurationMasterHelper.SHOW_STOCK_IN_SUMMARY) {


                    if (stockCheckHelper.SHOW_STOCK_SC) {
                        (row.findViewById(R.id.llShelfCase)).setVisibility(View.VISIBLE);
                        ((TextView) row.findViewById(R.id.shelfCaseTitle)).setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.LIGHT));
                        holder.shelfCaseQty.setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.MEDIUM));
                        try {
                            if (bModel.labelsMasterHelper.applyLabels(row.findViewById(
                                    R.id.shelfCaseTitle).getTag()) != null)
                                ((TextView) row.findViewById(R.id.shelfCaseTitle))
                                        .setText(bModel.labelsMasterHelper
                                                .applyLabels(row.findViewById(
                                                        R.id.shelfCaseTitle)
                                                        .getTag()));
                        } catch (Exception e) {
                            Commons.printException(e);
                        }

                    }
                    if (stockCheckHelper.SHOW_SHELF_OUTER) {
                        (row.findViewById(R.id.llShelfOuter)).setVisibility(View.VISIBLE);
                        ((TextView) row.findViewById(R.id.shelfOuterTitle)).setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.LIGHT));
                        holder.shelfOuterQty.setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.MEDIUM));
                        try {
                            if (bModel.labelsMasterHelper.applyLabels(row.findViewById(
                                    R.id.shelfOuterTitle).getTag()) != null)
                                ((TextView) row.findViewById(R.id.shelfOuterTitle))
                                        .setText(bModel.labelsMasterHelper
                                                .applyLabels(row.findViewById(
                                                        R.id.shelfOuterTitle)
                                                        .getTag()));
                        } catch (Exception e) {
                            Commons.printException(e);
                        }

                    }

                    if (stockCheckHelper.SHOW_STOCK_SP) {
                        (row.findViewById(R.id.llShelfPiece)).setVisibility(View.VISIBLE);
                        ((TextView) row.findViewById(R.id.shelfPcsTitle)).setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.LIGHT));
                        holder.shelfPieceQty.setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.MEDIUM));
                        try {
                            if (bModel.labelsMasterHelper.applyLabels(row.findViewById(
                                    R.id.shelfPcsTitle).getTag()) != null)
                                ((TextView) row.findViewById(R.id.shelfPcsTitle))
                                        .setText(bModel.labelsMasterHelper
                                                .applyLabels(row.findViewById(
                                                        R.id.shelfPcsTitle)
                                                        .getTag()));
                        } catch (Exception e) {
                            Commons.printException(e);
                        }
                    }
                }

                if (!bModel.configurationMasterHelper.IS_ORDER_SPLIT) {
                    holder.cbSeparateBill.setVisibility(View.GONE);
                } else {
                    holder.cbSeparateBill.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            holder.productBO.setSeparateBill(isChecked);
                            updateFooter();
                        }
                    });
                }

                // On/Off order case and pce
                if (!bModel.configurationMasterHelper.SHOW_ORDER_CASE) {
                    (row.findViewById(R.id.llCase)).setVisibility(View.GONE);
                } else {
                    ((TextView) row.findViewById(R.id.caseTitle)).setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.LIGHT));
                    holder.caseQty.setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.MEDIUM));
                    try {
                        if (bModel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.caseTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.caseTitle))
                                    .setText(bModel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.caseTitle).getTag()));
                    } catch (Exception e) {
                        Commons.printException(" " + e);
                    }
                }


                if (!bModel.configurationMasterHelper.SHOW_ORDER_PCS) {
                    (row.findViewById(R.id.llPiece)).setVisibility(View.GONE);
                } else {
                    ((TextView) row.findViewById(R.id.pcsTitle)).setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.LIGHT));
                    holder.pcsQty.setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.MEDIUM));
                    try {
                        if (bModel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.pcsTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.pcsTitle))
                                    .setText(bModel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.pcsTitle).getTag()));
                    } catch (Exception e) {
                        Commons.printException(" " + e);
                    }
                }
                if (!bModel.configurationMasterHelper.SHOW_OUTER_CASE) {
                    (row.findViewById(R.id.llOuter)).setVisibility(View.GONE);
                } else {
                    ((TextView) row.findViewById(R.id.outercaseTitle)).setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.LIGHT));
                    holder.outerQty.setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.MEDIUM));
                    try {
                        if (bModel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.outercaseTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.outercaseTitle))
                                    .setText(bModel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.outercaseTitle).getTag()));
                    } catch (Exception e) {
                        Commons.printException(" " + e);
                    }
                }
                if (!bModel.configurationMasterHelper.SHOW_ORDER_WEIGHT) {
                    (row.findViewById(R.id.llShelfWeight)).setVisibility(View.GONE);
                } else {
                    ((TextView) row.findViewById(R.id.weighttitle)).setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.LIGHT));
                    holder.caseQty.setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.MEDIUM));
                    try {
                        if (bModel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.weighttitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.weighttitle))
                                    .setText(bModel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.weighttitle).getTag()));
                    } catch (Exception e) {
                        Commons.printException(" " + e);
                    }
                }
                if (!bModel.configurationMasterHelper.SHOW_SALES_RETURN_IN_ORDER)
                    (row.findViewById(R.id.llStkRtEdit)).setVisibility(View.GONE);
                else {
                    try {
                        ((TextView) row.findViewById(R.id.stkRtTitle)).setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.LIGHT));
                        holder.salesReturn.setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.LIGHT));
                        if (bModel.labelsMasterHelper.applyLabels(row.findViewById(R.id.stkRtTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.stkRtTitle))
                                    .setText(bModel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.stkRtTitle).getTag()));
                    } catch (Exception e) {
                        Commons.printException(e + "");
                    }
                }
                if (!bModel.configurationMasterHelper.SHOW_REPLACED_QTY_CS)
                    (row.findViewById(R.id.llRepCase)).setVisibility(View.GONE);
                else {
                    ((TextView) row.findViewById(R.id.rep_caseTitle)).setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.LIGHT));
                    holder.rep_cs.setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.LIGHT));
                    try {
                        if (bModel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.rep_caseTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.rep_caseTitle))
                                    .setText(bModel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.rep_caseTitle)
                                                    .getTag()));
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                }
                if (!bModel.configurationMasterHelper.SHOW_REPLACED_QTY_OU)
                    (row.findViewById(R.id.llRepOu)).setVisibility(View.GONE);
                else {
                    ((TextView) row.findViewById(R.id.rep_outerTitle)).setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.LIGHT));
                    holder.rep_ou.setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.LIGHT));
                    try {
                        if (bModel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.rep_outerTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.rep_outerTitle))
                                    .setText(bModel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.rep_outerTitle)
                                                    .getTag()));
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                }
                if (!bModel.configurationMasterHelper.SHOW_REPLACED_QTY_PC)
                    (row.findViewById(R.id.llRepPc)).setVisibility(View.GONE);
                else {
                    ((TextView) row.findViewById(R.id.rep_pcsTitle)).setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.LIGHT));
                    holder.rep_pcs.setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.LIGHT));
                    try {
                        if (bModel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.rep_pcsTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.rep_pcsTitle))
                                    .setText(bModel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.rep_pcsTitle)
                                                    .getTag()));
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                }
                if (!bModel.configurationMasterHelper.SHOW_STK_ORD_SRP) {
                    (row.findViewById(R.id.llSrp)).setVisibility(View.GONE);
                } else {
                    ((TextView) row.findViewById(R.id.srpTitle)).setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.LIGHT));
                    holder.tw_srp.setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.MEDIUM));
                    try {
                        if (bModel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.srpTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.srpTitle))
                                    .setText(bModel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.srpTitle).getTag()));
                    } catch (Exception e) {
                        Commons.printException(" " + e);
                    }
                }

                if (!bModel.configurationMasterHelper.SHOW_STK_ORD_SRP) {
                    holder.text_total.setVisibility(View.GONE);
                }

                if (!bModel.configurationMasterHelper.SHOW_FOC) {
                    (row.findViewById(R.id.llfoc)).setVisibility(View.GONE);
                } else {
                    ((TextView) row.findViewById(R.id.focTitle)).setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.LIGHT));
                    holder.foc.setTypeface(FontUtils.getFontRoboto(OrderSummary.this, FontUtils.FontType.LIGHT));
                    try {
                        if (bModel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.focTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.focTitle))
                                    .setText(bModel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.focTitle).getTag()));
                    } catch (Exception e) {
                        Commons.printException(" " + e);
                    }
                }


                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.productBO = mOrderedProductList.get(groupPosition);
            holder.text_productName.setText(holder.productBO.getProductShortName());
            holder.pcsQty.setText(String.valueOf(holder.productBO.getOrderedPcsQty()));
            holder.caseQty.setText(String.valueOf(holder.productBO.getOrderedCaseQty()));
            holder.tw_srp.setText(bModel.formatValue(holder.productBO.getSrp()));
            holder.outerQty.setText(String.valueOf(holder.productBO.getOrderedOuterQty()));

            holder.shelfCaseQty.setText(String.valueOf(((holder.productBO.getLocations().get(bModel.productHelper.getmSelectedLocationIndex()).getShelfCase() == -1) ? 0 : holder.productBO.getLocations().get(bModel.productHelper.getmSelectedLocationIndex()).getShelfCase())));
            holder.shelfOuterQty.setText(String.valueOf(((holder.productBO.getLocations().get(bModel.productHelper.getmSelectedLocationIndex()).getShelfOuter() == -1) ? 0 : holder.productBO.getLocations().get(bModel.productHelper.getmSelectedLocationIndex()).getShelfOuter())));
            holder.shelfPieceQty.setText(String.valueOf(((holder.productBO.getLocations().get(bModel.productHelper.getmSelectedLocationIndex()).getShelfPiece() == -1) ? 0 : holder.productBO.getLocations().get(bModel.productHelper.getmSelectedLocationIndex()).getShelfPiece())));
            holder.foc.setText(String.valueOf(holder.productBO.getFoc()));
            holder.text_total.setText(String.valueOf(bModel.formatValue(holder.productBO
                    .getNetValue())));
            int weight = holder.productBO.getOrderedPcsQty() + (holder.productBO.getOrderedCaseQty() * holder.productBO.getCaseSize()) + (holder.productBO.getOrderedOuterQty() * holder.productBO.getOutersize());
            holder.weight.setText(Utils.formatAsTwoDecimal((double) weight * holder.productBO.getWeight()));

            if (bModel.configurationMasterHelper.SHOW_SALES_RETURN_IN_ORDER) {
                int total = 0;
                if (holder.productBO.getSalesReturnReasonList() != null) {
                    for (SalesReturnReasonBO obj : holder.productBO.getSalesReturnReasonList())
                        total = total + obj.getPieceQty() + (obj.getCaseQty() * obj.getCaseSize()) + (obj.getOuterQty() * obj.getOuterSize());
                }
                String strTotal = Integer.toString(total);
                holder.salesReturn.setText(strTotal);
            }

            String strRepCaseQty = holder.productBO.getRepCaseQty() + "";
            holder.rep_cs.setText(strRepCaseQty);
            String strRepOuterQty = holder.productBO.getRepOuterQty() + "";
            holder.rep_ou.setText(strRepOuterQty);
            String strRepPcsQty = holder.productBO.getRepPieceQty() + "";
            holder.rep_pcs.setText(strRepPcsQty);

            holder.cbSeparateBill.setChecked(holder.productBO.isSeparateBill());

            if (bModel.configurationMasterHelper.IS_SHOW_OOS && holder.productBO.getWSIH() == 0)
                holder.rl_oos.setVisibility(View.VISIBLE);
            else
                holder.rl_oos.setVisibility(View.GONE);

            return row;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }

    }

    class ViewHolder {
        private ProductMasterBO productBO;
        private TextView text_productName;
        private TextView pcsQty;
        private TextView caseQty;
        private TextView outerQty;
        private TextView weight;
        private TextView shelfCaseQty;
        private TextView shelfOuterQty;
        private TextView shelfPieceQty;
        private TextView tw_srp;
        private TextView text_total;
        private TextView foc;
        private AppCompatCheckBox cbSeparateBill;
        private TextView rep_pcs;
        private TextView rep_cs;
        private TextView rep_ou;
        private TextView salesReturn;
        private RelativeLayout rl_oos;
    }


    private final DatePickerDialog.OnDateSetListener mDeliverDatePickerListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            Calendar selectedDate = new GregorianCalendar(year, monthOfYear,
                    dayOfMonth);

            String dbDateFormat = DateTimeUtils.convertDateObjectToRequestedFormat(
                    selectedDate.getTime(), "yyyy/MM/dd");
            if (NonFieldHelper.getInstance(OrderSummary.this).isHoliday(dbDateFormat, OrderSummary.this)
                    || NonFieldHelper.getInstance(OrderSummary.this).isWeekOff(dbDateFormat)) {
                Toast.makeText(OrderSummary.this, "The Selected day is a holiday", Toast.LENGTH_SHORT).show();
            }

            button_deliveryDate.setText(DateTimeUtils.convertDateObjectToRequestedFormat(
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
                defaultCalendar.add(Calendar.DAY_OF_YEAR, (bModel.configurationMasterHelper.DEFAULT_NUMBER_OF_DAYS_TO_DELIVER_ORDER == 0 ? 1 : bModel.configurationMasterHelper.DEFAULT_NUMBER_OF_DAYS_TO_DELIVER_ORDER));
                button_deliveryDate.setText(DateTimeUtils.convertDateObjectToRequestedFormat(defaultCalendar.getTime(), ConfigurationMasterHelper.outDateFormat));
            }
            view.updateDate(year, monthOfYear, dayOfMonth);
        }
    };

    private Calendar dateValidation(Calendar selectedDate) {
        String dbDateFormat = DateTimeUtils.convertDateObjectToRequestedFormat(
                selectedDate.getTime(), "yyyy/MM/dd");

        while (NonFieldHelper.getInstance(OrderSummary.this).isHoliday(dbDateFormat, OrderSummary.this)
                || NonFieldHelper.getInstance(OrderSummary.this).isWeekOff(dbDateFormat)) {
            selectedDate.add(Calendar.DAY_OF_MONTH, 1);
            dbDateFormat = DateTimeUtils.convertDateObjectToRequestedFormat(
                    selectedDate.getTime(), "yyyy/MM/dd");
        }
        return selectedDate;

//        String dbDateFormat = DateUtil.convertDateObjectToRequestedFormat(
//                selectedDate.getTime(), "yyyy/MM/dd");
//        if (NonFieldHelper.getInstance(OrderSummary.this).isHoliday(dbDateFormat, OrderSummary.this)
//                || NonFieldHelper.getInstance(OrderSummary.this).isWeekOff(dbDateFormat)) {
//            selectedDate.add(Calendar.DAY_OF_MONTH, 1);
//            return dateValidation(selectedDate);
//        } else {
//            return selectedDate;
//        }
    }


    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            isClick = false;


            if (msg.what == DataMembers.NOTIFY_ORDER_SAVED) {
                try {

                    alertDialog.dismiss();
                    bModel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils.now(DateTimeUtils.TIME));

                    if (bModel.configurationMasterHelper.SHOW_PRINT_ORDER && !orderHelper.isQuickCall) {
                        showDialog(DIALOG_ORDER_SAVED_WITH_PRINT_OPTION);
                    } else {
                        showDialog(DIALOG_ORDER_SAVED);
                    }


                } catch (Exception e) {
                    Commons.printException("" + e);
                }
            } else if (msg.what == DataMembers.NOTIFY_ORDER_NOT_SAVED) {
                try {
                    alertDialog.dismiss();
                    Toast.makeText(OrderSummary.this, getResources().getString(R.string.order_save_falied),
                            Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Commons.printException("" + e);
                }
            } else if (msg.what == DataMembers.NOTIFY_INVOICE_SAVED) {
                try {

                    orderHelper.getPrintedCountForCurrentInvoice(OrderSummary.this);
                    alertDialog.dismiss();


                    if (bModel.configurationMasterHelper.IS_INVOICE) {
                        if (bModel.configurationMasterHelper.SHOW_ZEBRA_UNIPAL) {
                            showDialog(DIALOG_INVOICE_SAVED);
                        } else {
                            printInvoice();
                        }
                    } else {
                        bModel.showAlert(
                                getResources()
                                        .getString(
                                                R.string.order_saved_and_print_preview_created_successfully),
                                DataMembers.NOTIFY_INVOICE_SAVED);
                    }

                } catch (Exception e) {
                    Commons.printException(e);
                }
            } else if (msg.what == DataMembers.NOTIFY_INVOICE_NOT_SAVED) {
                try {
                    alertDialog.dismiss();
                    Toast.makeText(OrderSummary.this, getResources().getString(R.string.not_able_to_generate_invoice),
                            Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Commons.printException("" + e);
                }
            } else if (msg.what == DataMembers.NOTIFY_ORDER_DELETED) {
                try {
                    alertDialog.dismiss();
                    bModel = (BusinessModel) getApplicationContext();

                    bModel.showAlert(
                            getResources().getString(
                                    R.string.order_deleted_sucessfully)
                                    + orderHelper.getOrderId(),
                            DataMembers.NOTIFY_ORDER_DELETED);
                } catch (Exception e) {
                    Commons.printException("" + e);
                }
            } else if (msg.what == BtService.STATE_CONNECTED) {
                Toast.makeText(getApplicationContext(), "Connected",
                        Toast.LENGTH_SHORT).show();
                new checkAndPrint().execute();

            } else if (msg.what == BtService.STATE_CONNECTING) {
                Toast.makeText(getApplicationContext(), "Connecting",
                        Toast.LENGTH_SHORT).show();
            } else if (msg.what == BtService.STATE_LISTEN) {
                if (msg.what == BtService.STATE_NONE) {
                    Toast.makeText(getApplicationContext(), "None",
                            Toast.LENGTH_SHORT).show();

                }
            } else if (msg.what == MESSAGE_DEVICE_NAME) {
                String mConnectedDeviceName;
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(),
                        "Device Name " + mConnectedDeviceName,
                        Toast.LENGTH_SHORT).show();

            } else if (msg.what == MESSAGE_TOAST) {
                Toast.makeText(getApplicationContext(),
                        msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
                        .show();
                bModel.productHelper.clearOrderTableChecked();
                if (!orderHelper.isQuickCall) {
                    Intent i = new Intent(OrderSummary.this, HomeScreenTwo.class);
                    startActivity(i);
                }
                finish();
            } else if (msg.what == DataMembers.NOTIFY_DATABASE_NOT_SAVED) {
                Toast.makeText(OrderSummary.this, "DataBase Restore failed.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void createOrderPrintFile(boolean isPrepareData, String orderId) {

        orderId = orderId.replace("\'", "");
        if (isPrepareData) {
            if ("1".equalsIgnoreCase(bModel.retailerMasterBO.getRField4()))
                bModel.productHelper.updateDistributorDetails();

            SalesReturnHelper salesReturnHelper = SalesReturnHelper.getInstance(OrderSummary.this);

            final List<ProductMasterBO> orderListWithReplace = salesReturnHelper.updateReplaceQtyWithOutTakingOrder(mOrderedProductList);
            Vector<ProductMasterBO> orderList = new Vector<>(orderListWithReplace);

            bModel.mCommonPrintHelper.xmlRead("order", false, orderList, null, signatureName, null, null);

        }
        bModel.writeToFile(String.valueOf(bModel.mCommonPrintHelper.getInvoiceData()),
                StandardListMasterConstants.PRINT_FILE_ORDER + orderId, "/" + DataMembers.IVYDIST_PATH + "/", "");

    }


    private void printOrder() {

        Intent i;
        if (bModel.configurationMasterHelper.SHOW_ZEBRA_TITAN
                || bModel.configurationMasterHelper.SHOW_ZEBRA_UNIPAL
        ) {
            new Thread(new Runnable() {
                public void run() {
                    Looper.prepare();
                    connectZebraPrinter(ZEBRA_3INCH);
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
        } else if (bModel.configurationMasterHelper.COMMON_PRINT_BIXOLON
                || bModel.configurationMasterHelper.COMMON_PRINT_ZEBRA
                || bModel.configurationMasterHelper.COMMON_PRINT_SCRYBE
                || bModel.configurationMasterHelper.COMMON_PRINT_LOGON
                || bModel.configurationMasterHelper.COMMON_PRINT_INTERMEC
                || bModel.configurationMasterHelper.COMMON_PRINT_MAESTROS
                || bModel.configurationMasterHelper.SHOW_PRINT_ORDER) {

            if ("1".equalsIgnoreCase(bModel.retailerMasterBO.getRField4()))
                bModel.productHelper.updateDistributorDetails();

            SalesReturnHelper salesReturnHelper = SalesReturnHelper.getInstance(OrderSummary.this);

            final List<ProductMasterBO> orderListWithReplace = salesReturnHelper.updateReplaceQtyWithOutTakingOrder(mOrderedProductList);
            Vector<ProductMasterBO> orderList = new Vector<>(orderListWithReplace);

            bModel.mCommonPrintHelper.xmlRead("order", false, orderList, null, signatureName, null, null);
            if (bModel.configurationMasterHelper.IS_PRINT_FILE_SAVE) {
                bModel.writeToFile(String.valueOf(bModel.mCommonPrintHelper.getInvoiceData()),
                        StandardListMasterConstants.PRINT_FILE_ORDER + bModel.invoiceNumber, "/" + DataMembers.IVYDIST_PATH, "");

                i = new Intent(OrderSummary.this,
                        CommonPrintPreviewActivity.class);
                i.putExtra("IsFromOrder", true);
                i.putExtra("IsUpdatePrintCount", true);
                i.putExtra("isFromInvoice", true);
                i.putExtra("isHomeBtnEnable", true);
                i.putExtra("sendMailAndLoadClass", "PRINT_FILE_ORDER");
                startActivity(i);
                overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                finish();

            } else {

                createOrderPrintFile(false, orderHelper.getOrderId());

                i = new Intent(OrderSummary.this,
                        CommonPrintPreviewActivity.class);
                i.putExtra("IsFromOrder", true);
                i.putExtra("IsUpdatePrintCount", true);
                i.putExtra("isHomeBtnEnable", true);
                i.putExtra("isFromInvoice", true);
                i.putExtra("sendMailAndLoadClass", "PRINT_FILE_ORDER");
                startActivity(i);
                overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                finish();
            }

        }

    }

    private void printInvoice() {

        if (bModel.configurationMasterHelper.SHOW_ZEBRA_UNIPAL) {
            new Thread(new Runnable() {
                public void run() {
                    Looper.prepare();
                    connectZebraPrinter(ZEBRA_3INCH);
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

        } else if (bModel.configurationMasterHelper.SHOW_ZEBRA_TITAN) {

            Intent i = new Intent(OrderSummary.this,
                    PrintPreviewScreenTitan.class);
            i.putExtra("IsFromOrder", true);
            i.putExtra("entryLevelDis", entryLevelDiscount);
            startActivity(i);
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            finish();

        } else if (bModel.configurationMasterHelper.COMMON_PRINT_BIXOLON
                || bModel.configurationMasterHelper.COMMON_PRINT_ZEBRA
                || bModel.configurationMasterHelper.COMMON_PRINT_SCRYBE
                || bModel.configurationMasterHelper.COMMON_PRINT_LOGON
                || bModel.configurationMasterHelper.COMMON_PRINT_INTERMEC
                || bModel.configurationMasterHelper.COMMON_PRINT_MAESTROS) {


            if ("1".equalsIgnoreCase(bModel.getRetailerMasterBO().getRField4())) {
                bModel.productHelper.updateDistributorDetails();
            }

            SalesReturnHelper salesReturnHelper = SalesReturnHelper.getInstance(OrderSummary.this);

            final List<ProductMasterBO> orderListWithReplace = salesReturnHelper.updateReplaceQtyWithOutTakingOrder(mOrderedProductList);
            Vector<ProductMasterBO> orderList = new Vector<>(orderListWithReplace);

            if (bModel.configurationMasterHelper.SHOW_PRINT_ORDER) {
                bModel.mCommonPrintHelper.xmlRead("order", false, orderList, null, signatureName, null, null);
                createOrderPrintFile(false, orderHelper.getOrderId());
            }

            bModel.mCommonPrintHelper.xmlRead("invoice", false, orderList, null, signatureName, null, null);


            bModel.writeToFile(String.valueOf(bModel.mCommonPrintHelper.getInvoiceData()),
                    StandardListMasterConstants.PRINT_FILE_INVOICE + bModel.invoiceNumber, "/" + DataMembers.PRINT_FILE_PATH, "");

            Intent i = new Intent(OrderSummary.this,
                    CommonPrintPreviewActivity.class);
            i.putExtra("IsFromOrder", true);
            i.putExtra("IsUpdatePrintCount", true);
            i.putExtra("isHomeBtnEnable", true);
            i.putExtra("sendMailAndLoadClass", "PRINT_FILE_INVOICE");
            startActivity(i);
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            finish();

        } else {
            bModel.showAlert(
                    getResources()
                            .getString(
                                    R.string.invoice_created_successfully),
                    DataMembers.NOTIFY_INVOICE_SAVED);
        }

    }


    private void connectZebraPrinter(String printerName) {
        try {
            printer = InitializeZebraPrinter();

            if (printer != null) {
                LoadManagementHelper.getInstance(this).downloadSubDepots();
                projectSpecificPrinterCall(printerName);
            } else {
                bModel.productHelper.clearOrderTable();
                disconnectZebraPrinter();
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

    private ZebraPrinter InitializeZebraPrinter() {
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
            disconnectZebraPrinter();
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

    private void projectSpecificPrinterCall(String printerName) {
        try {
            if (printerName.equals(ZEBRA_3INCH)) {
                if (bModel.configurationMasterHelper.SHOW_ZEBRA_UNIPAL) {
                    bModel.printHelper.setPrintCnt(0);
                    for (int i = 0; i < mSelectedPrintCount + 1; i++) {
                        zebraPrinterConnection.write(bModel.printHelper.printDatafor3inchprinterForUnipal(mOrderedProductList, isFromOrder, 1));
                        if (!isFromOrder) {
                            bModel.updatePrintCount(1);
                            bModel.printHelper.setPrintCnt(orderHelper.getPrintedCountForCurrentInvoice(this));
                        }
                    }
                } else if (bModel.configurationMasterHelper.SHOW_ZEBRA_TITAN) {
                    for (int i = 0; i < mSelectedPrintCount + 1; i++) {
                        zebraPrinterConnection.write(bModel.printHelper.printDataforTitan3inchOrderprinter(mOrderedProductList, 0));
                        if (!isFromOrder) {
                            bModel.updatePrintCount(1);
                            bModel.printHelper.setPrintCnt(orderHelper.getPrintedCountForCurrentInvoice(this));
                        }
                    }
                } else if (bModel.configurationMasterHelper.SHOW_ZEBRA_GHANA
                        || bModel.configurationMasterHelper.SHOW_ZEBRA_DIAGEO) {
                    zebraPrinterConnection.write(bModel.printHelper.printDatafor3inchPrinterDiageoNG(button_deliveryDate.getText().toString()));
                }

                alertDialog.dismiss();
                bModel.productHelper.clearOrderTable();

                bModel.showAlert(
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
            disconnectZebraPrinter();
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

    private void disconnectZebraPrinter() {
        try {
            if (zebraPrinterConnection != null) {
                zebraPrinterConnection.close();
            }
        } catch (ConnectionException e) {
            Commons.printException("" + e);
        }
    }

    private class checkAndPrint extends AsyncTask<Integer, Integer, Boolean> {
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
                PrintData();
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
            bModel.productHelper.clearOrderTableChecked();
            Intent i = new Intent(OrderSummary.this, HomeScreenTwo.class);
            startActivity(i);
            finish();
        }

    }

    public void numberPressed(View vw) {

        if (mStoreWiseDiscountDialogFragment != null && mStoreWiseDiscountDialogFragment.isVisible()) {
            mStoreWiseDiscountDialogFragment.numberPressed(vw);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:// When DeviceListActivity returns with a
                // device to connect
                if (resultCode == Activity.RESULT_OK) {
                    String address = sharedPreferences.getString("MAC", "");
                    BluetoothDevice device = mBluetoothAdapter
                            .getRemoteDevice(address);
                    mChatService.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT: // When the request to enable Bluetooth returns
                Toast.makeText(this, "Blue tooth not enable",
                        Toast.LENGTH_SHORT).show();
                finish();
            case CAMERA_REQUEST_CODE:
                if (resultCode == 1) {
                    if (bModel.getOrderHeaderBO() != null)
                        bModel.getOrderHeaderBO().setOrderImageName(mImageName);
                }
                break;
            case FILE_SELECTION:
                if (requestCode == 12 && data != null) {

                    invalidateOptionsMenu();
                    String realPath = FileUtils.getPath(this, data.getData());
                    FileUtils.copyFile(new File(realPath), FileUtils.photoFolderPath, attachedFilePath);
                    if (bModel.getOrderHeaderBO() != null)
                        bModel.getOrderHeaderBO().setOrderImageName(attachedFilePath);
                }
                break;
            case DISCOUNT_RESULT_CODE:
                overridePendingTransition(0, R.anim.zoom_exit);
                break;
            case RETURN_PRODUCT_RESULT_CODE:
                overridePendingTransition(0, R.anim.zoom_exit);
                break;
            case INDICATIVE_ORDER_REASON_RESULT_CODE:
                overridePendingTransition(0, R.anim.zoom_exit);
                break;
            case COLLECTION_INVOICE_RESULT_CODE:
                overridePendingTransition(0, R.anim.zoom_exit);
                collectionbo = Objects.requireNonNull(data.getExtras()).getParcelable("Collection");
                break;
            default:
                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mChatService != null) {
            mChatService.stop();
        }
        if (!isEditMode) {
            bModel.productHelper.clearOrderTable();
            discountHelper.clearSchemeFreeProduct(OrderSummary.this, mOrderedProductList);
        }
        unbindDrawables(findViewById(R.id.root));
    }

    /**
     * this would clear all the resources used of the layout.
     *
     * @param view root view
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

    private void updateFooter() {
        if (bModel.configurationMasterHelper.IS_ORDER_SPLIT) {
            Vector<ProductMasterBO> bill1Products = new Vector<>();
            Vector<ProductMasterBO> bill2Products = new Vector<>();
            double bill1Value = 0, bill2Value = 0;
            ProductMasterBO productBO;
            for (int i = 0; i < bModel.productHelper
                    .getProductMaster().size(); i++) {

                productBO = bModel.productHelper
                        .getProductMaster().elementAt(i);
                if (productBO.getOrderedCaseQty() > 0
                        || productBO.getOrderedPcsQty() > 0
                        || productBO.getOrderedOuterQty() > 0) {


                    double lineValue = (productBO.getOrderedCaseQty() * productBO
                            .getCsrp())
                            + (productBO.getOrderedPcsQty() * productBO
                            .getSrp())
                            + (productBO.getOrderedOuterQty() * productBO
                            .getOsrp());

                    if (productBO.isSeparateBill()) {
                        bill2Products.add(productBO);
                        bill2Value += lineValue;
                    } else {
                        bill1Products.add(productBO);
                        bill1Value += lineValue;
                    }
                }
            }

            textbill1.setText(bModel.formatValue(bill1Value));
            textbill2.setText(bModel.formatValue(bill2Value));
            linesBill1.setText(String.valueOf(bill1Products.size()));
            linesBill2.setText(String.valueOf(bill2Products.size()));


        }
    }


    /**
     * Showing alert dialog to denote image availability..
     *
     * @param imageNameStarts Image Name
     * @param imageSrc        Image Path
     */
    private void showFileDeleteAlertWithImage(final String imageNameStarts,
                                              final String imageSrc) {
        final CommonDialog commonDialog = new CommonDialog(getApplicationContext(), //Context
                this, //Context
                "", //Title
                getResources().getString(R.string.word_already) + " " + getResources().getString(R.string.word_photocaptured_delete_retake), //Message
                true, //ToDisplayImage
                getResources().getString(R.string.yes), //Positive Button
                getResources().getString(R.string.no), //Negative Button
                false, //MoveToNextActivity
                getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + DataMembers.photoFolderName + "/" + imageSrc, //LoadImage
                new CommonDialog.PositiveClickListener() {
                    @Override
                    public void onPositiveButtonClick() {

                        bModel.deleteFiles(FileUtils.photoFolderPath,
                                imageNameStarts);
                        bModel.getOrderHeaderBO().setOrderImageName("");

                        Intent intent = new Intent(OrderSummary.this,
                                CameraActivity.class);
                        String path = FileUtils.photoFolderPath + "/" + mImageName;
                        intent.putExtra("path", path);
                        startActivityForResult(intent,
                                CAMERA_REQUEST_CODE);

                    }
                }, new CommonDialog.negativeOnClickListener() {
            @Override
            public void onNegativeButtonClick() {
                dismiss();
            }
        });
        commonDialog.show();
        commonDialog.setCancelable(false);
    }

    private boolean isReturnDoneForProduct(ProductMasterBO productMasterBO) {
        try {
            for (SalesReturnReasonBO bo : productMasterBO.getSalesReturnReasonList()) {
                if (bo.getPieceQty() != 0 || bo.getCaseQty() != 0
                        || bo.getOuterQty() > 0)
                    return true;

            }

            if (productMasterBO.getRepPieceQty() > 0
                    || productMasterBO.getRepOuterQty() > 0 || productMasterBO.getRepCaseQty() > 0)
                return true;

        } catch (Exception ex) {
            Commons.printException(ex);
        }
        return false;
    }

    private boolean hasSchemeApplied() {
        for (ProductMasterBO productMasterBO : mOrderedProductList) {
            if (productMasterBO.getSchemeDiscAmount() > 0 || productMasterBO.getProductLevelDiscountValue() > 0) {
                return true;
            }
        }

        return false;
    }

    private void openPdfDeleteDialog(final String imageNameStarts) {

        final CommonDialog commonDialog = new CommonDialog(getApplicationContext(), //Context
                this, //Context
                "", //Title
                getResources().getString(R.string.word_already) + " " + getResources().getString(R.string.pdf_attached), //Message
                false, //ToDisplayImage
                getResources().getString(R.string.yes), //Positive Button
                getResources().getString(R.string.no), //Negative Button
                false, //MoveToNextActivity
                new CommonDialog.PositiveClickListener() {
                    @Override
                    public void onPositiveButtonClick() {
                        invalidateOptionsMenu();

                        bModel.deleteFiles(FileUtils.photoFolderPath,
                                imageNameStarts);
                        bModel.getOrderHeaderBO().setOrderImageName("");

                        String path = FileUtils.photoFolderPath + "/"
                                + attachedFilePath;
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("application/pdf");
                        intent.putExtra("path", path);
                        startActivityForResult(intent, FILE_SELECTION);

                    }
                }, new CommonDialog.negativeOnClickListener() {
            @Override
            public void onNegativeButtonClick() {
                dismiss();
            }
        });
        commonDialog.show();
        commonDialog.setCancelable(false);
    }

    /**
     * Bill wise discount apply based on isFlag
     * isFlag - 0 Discount Computation OR Apply in before tax apply
     * isFlag - 1 Discount Computation OR Apply in after tax apply
     */
    private void applyBillWiseDiscAndTax() {

        callBillWiseDiscount(0);
        applyBillWiseTax();
        callBillWiseDiscount(1);
    }


    private void callBillWiseDiscount(int isFlag) {
        //Applying Bill wise Discount
        if (bModel.configurationMasterHelper.SHOW_STORE_WISE_DISCOUNT_DLG && bModel.configurationMasterHelper.BILL_WISE_DISCOUNT == 0) {
            //find the  range of discount by using total value
            totalOrderValue = discountHelper.calculateBillWiseRangeDiscount(totalOrderValue, isFlag);

            if (bModel.getOrderHeaderBO() != null)
                enteredDiscAmtOrPercent = bModel.getOrderHeaderBO().getBillLevelDiscountValue();

        } else if (bModel.configurationMasterHelper.SHOW_STORE_WISE_DISCOUNT_DLG && bModel.configurationMasterHelper.BILL_WISE_DISCOUNT == 1) {
            // Automatically apply bill wise discount
            totalOrderValue = discountHelper.calculateBillWiseDiscount(totalOrderValue, isFlag);

            if (bModel.getOrderHeaderBO() != null)
                enteredDiscAmtOrPercent = bModel.getOrderHeaderBO().getBillLevelDiscountValue();

            // totalOrderValue = totalOrderValue - SDUtil.convertToDouble(SDUtil.format(billWiseDiscount, bModel.configurationMasterHelper.VALUE_PRECISION_COUNT, 0));

        } else if (bModel.configurationMasterHelper.SHOW_TOTAL_DISCOUNT_EDITTEXT) {
            // user manually enter bill wise discount
            double discount = bModel.orderAndInvoiceHelper.restoreDiscountAmount(bModel
                    .getRetailerMasterBO().getRetailerID());
            double billWiseDiscount = applyDiscountMaxValidation(discount);
            totalOrderValue = totalOrderValue - billWiseDiscount;
        }

        // Apply bill wise pay term discount
        if (discountHelper.getBillWisePayternDiscountList() != null
                && discountHelper.getBillWisePayternDiscountList().size() > 0) {
            totalOrderValue = discountHelper.calculateBillWisePayTermDiscount(totalOrderValue, isFlag);
        }
    }


    private void applyBillWiseTax() {
        //Applying bill wise tax
        if (bModel.configurationMasterHelper.TAX_SHOW_INVOICE) {
            bModel.productHelper.taxHelper.downloadBillWiseTaxDetails();
            double billLevelTax = bModel.productHelper.taxHelper.applyBillWiseTax(totalOrderValue);
            bModel.getOrderHeaderBO().setBillLevelTaxValue(billLevelTax);

            if (bModel.configurationMasterHelper.SHOW_INCLUDE_BILL_TAX) {
                totalOrderValue += billLevelTax;
                taxValue += billLevelTax;
            }
        }
    }
}
