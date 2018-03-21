package com.ivy.cpg.view.order;

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
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.salesreturn.SalesReturnHelper;
import com.ivy.sd.intermecprint.BtPrint4Ivy;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.CollectionBO;
import com.ivy.sd.png.bo.OrderHeader;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.SchemeProductBO;
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
import com.ivy.sd.png.view.AdvancePaymentDialogFragment;
import com.ivy.sd.png.view.AmountSplitUpDialog;
import com.ivy.sd.png.view.BixolonIIPrint;
import com.ivy.sd.png.view.BixolonIPrint;
import com.ivy.sd.png.view.CaptureSignatureActivity;
import com.ivy.sd.png.view.CatalogOrder;
import com.ivy.sd.png.view.DataPickerDialogFragment;
import com.ivy.sd.png.view.EmailDialog;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.sd.png.view.IndicativeOrderReasonDialog;
import com.ivy.sd.png.view.InvoicePrintZebraNew;
import com.ivy.sd.png.view.OrderConfirmationDialog;
import com.ivy.sd.png.view.OrderRemarkDialog;
import com.ivy.sd.png.view.OrderSummaryDialogFragment;
import com.ivy.sd.png.view.SerialNoEntryScreen;
import com.ivy.sd.png.view.StoreWiseDiscountDialog;
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
        StoreWiseDiscountDialog.OnMyDialogResult, DataPickerDialogFragment.UpdateDateInterface,
        EmailDialog.onSendButtonClickListnor, OrderConfirmationDialog.OnConfirmationResult {

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


    private Button button_order;
    private Button button_invoice;
    private TextView text_LPC;
    private TextView text_totalOrderValue;
    private TextView text_totalOrderedQuantity;
    private Button button_deliveryDate;
    private ExpandableListView listView;
    private ImageView imageView_amountSplitUp;

    private DiscountDialog discountDialog;
    private AlertDialog.Builder build;
    private AlertDialog alertDialog;
    private AmountSplitUpDialog amountSplitUpDialog;
    private OrderConfirmationDialog orderConfirmationDialog;
    private ReturnProductDialog returnProductDialog;
    private CollectionBeforeInvoiceDialog collectionBeforeInvoiceDialog;
    private StoreWiseDiscountDialog mStoreWiseDiscountDialogFragment;
    private BusinessModel BModel;
    private CollectionBO collectionbo;
    private DiscountHelper discountHelper;
    private OrderHelper orderHelper;

    private LinkedList<ProductMasterBO> mOrderedProductList;
    private Vector<ProductMasterBO> mSortedList;

    private String sendMailAndLoadClass;
    private boolean isFromOrder;
    private double enteredDiscAmtOrPercent = 0;

    private double totalOrderValue;
    private boolean isClick = false;
    private boolean isDiscountDialog;
    private double entryLevelDiscount = 0.0;

    private double totalSchemeDiscValue;
    private int mSelectedPrintCount = 0;

    private boolean isClicked;
    private String screenCode = "MENU_STK_ORD";
    private String PHOTO_PATH = "";
    private SharedPreferences sharedPreferences;

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


        BModel = (BusinessModel) getApplicationContext();
        BModel.setContext(this);
        discountHelper = DiscountHelper.getInstance(this);
        orderHelper = OrderHelper.getInstance(this);

        // Close the screen if user id becomes 0 **/
        if (BModel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
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
        if (BModel.mSelectedModule == 1 || BModel.mSelectedModule == 2) {
            BModel.configurationMasterHelper
                    .downloadNewActivityMenu(ConfigurationMasterHelper.MENU_ACTIVITY);
        }

        String screenTitle = BModel.configurationMasterHelper
                .getHomescreentwomenutitle("MENU_CLOSING");
        if ("MENU_CLOSING".equals(screenTitle))
            screenTitle = getResources().getString(R.string.summary);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
                setScreenTitle(screenTitle);
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
        }
        BModel.saveModuleCompletion("MENU_CLOSING");


        PHOTO_PATH = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + DataMembers.photoFolderName;

        // Focus product alert
        if (BModel.configurationMasterHelper.SHOW_ORDER_FOCUS_COUNT) {
            showFocusProductAlert();

        }

        initializeViews();
        hideAndSeek();
        updateLabels();
        setDeliveryDate();


        sharedPreferences = getSharedPreferences(BusinessModel.PREFS_NAME, MODE_PRIVATE);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter);


        if (BModel.getOrderHeaderBO() == null) {
            BModel.setOrderHeaderBO(new OrderHeader());
        }


        // update empty bottle returns
        if (!BModel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG
                || BModel.configurationMasterHelper.IS_SIH_VALIDATION) {

            if (!BModel.isEdit() || !BModel.isDoubleEdit_temp()) {
                BModel.schemeDetailsMasterHelper
                        .updataFreeproductBottleReturn();
            }
            // update empty bottle return group wise
            if (BModel.configurationMasterHelper.SHOW_GROUPPRODUCTRETURN) {
                BModel.productHelper.setGroupWiseReturnQty();
                BModel.productHelper.calculateOrderReturnTypeWiseValue();
            }
        }

        // IF Collection Before Invoice Module Enable
        if (BModel.configurationMasterHelper.SHOW_COLLECTION_BEFORE_INVOICE) {
            collectionbo = new CollectionBO();
            BModel.collectionHelper.getPaymentList().clear();
        }


    }

    private void initializeViews() {
        text_LPC = (TextView) findViewById(R.id.lcp);
        text_totalOrderValue = (TextView) findViewById(R.id.totalValue);
        button_deliveryDate = (Button) findViewById(R.id.deliveryDate);
        button_order = (Button) findViewById(R.id.orderSummarySave);
        listView = (ExpandableListView) findViewById(R.id.elv);
        button_invoice = (Button) findViewById(R.id.saveAndGoInvoice);
        text_totalOrderedQuantity = (TextView) findViewById(R.id.tv_totalqty);
        imageView_amountSplitUp = (ImageView) findViewById(R.id.icAmountSpilitup);

        //typefaces
        ((TextView) findViewById(R.id.tv_deliveryDate)).setTypeface(BModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) findViewById(R.id.lpcLabel)).setTypeface(BModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) findViewById(R.id.totalValuelbl)).setTypeface(BModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) findViewById(R.id.title_totalqty)).setTypeface(BModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        button_deliveryDate.setTypeface(BModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        text_LPC.setTypeface(BModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        text_totalOrderValue.setTypeface(BModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        text_totalOrderedQuantity.setTypeface(BModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        button_order.setTypeface(BModel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        button_invoice.setTypeface(BModel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        button_deliveryDate.setOnClickListener(this);
        button_order.setOnClickListener(this);
        button_invoice.setOnClickListener(this);
        imageView_amountSplitUp.setOnClickListener(this);
    }

    private void updateLabels() {

        try {
            if (BModel.labelsMasterHelper.applyLabels(findViewById(
                    R.id.totalValuelbl).getTag()) != null)
                ((TextView) findViewById(R.id.totalValuelbl))
                        .setText(BModel.labelsMasterHelper
                                .applyLabels(findViewById(
                                        R.id.totalValuelbl)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException(" " + e);
        }


        try {
            if (BModel.labelsMasterHelper.applyLabels(findViewById(
                    R.id.orderSummarySave).getTag()) != null)
                ((TextView) findViewById(R.id.orderSummarySave))
                        .setText(BModel.labelsMasterHelper
                                .applyLabels(findViewById(
                                        R.id.orderSummarySave)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException(" " + e);
        }
        try {
            if (BModel.labelsMasterHelper.applyLabels(findViewById(
                    R.id.saveAndGoInvoice).getTag()) != null)
                ((TextView) findViewById(R.id.saveAndGoInvoice))
                        .setText(BModel.labelsMasterHelper
                                .applyLabels(findViewById(
                                        R.id.saveAndGoInvoice)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException(" " + e);
        }
        try {
            if (BModel.labelsMasterHelper.applyLabels(findViewById(
                    R.id.lbl_comy_disc).getTag()) != null)
                ((TextView) findViewById(R.id.lbl_comy_disc))
                        .setText(BModel.labelsMasterHelper
                                .applyLabels(findViewById(
                                        R.id.lbl_comy_disc)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException(" " + e);
        }
        try {
            if (BModel.labelsMasterHelper.applyLabels(findViewById(
                    R.id.lbl_dist_disc).getTag()) != null)
                ((TextView) findViewById(R.id.lbl_dist_disc))
                        .setText(BModel.labelsMasterHelper
                                .applyLabels(findViewById(
                                        R.id.lbl_dist_disc)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException(" " + e);
        }

        try {
            if (BModel.labelsMasterHelper.applyLabels(findViewById(
                    R.id.totaltitle).getTag()) != null)
                ((TextView) findViewById(R.id.totaltitle))
                        .setText(BModel.labelsMasterHelper
                                .applyLabels(findViewById(R.id.totaltitle)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException(" " + e);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        BModel = (BusinessModel) getApplicationContext();
        BModel.setContext(this);

        isClicked = false;

        //session out if user id becomes 0
        if (BModel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        prepareScreenData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.order_summary_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        menu.findItem(R.id.menu_review).setVisible(BModel.configurationMasterHelper.SHOW_REVIEW_AND_PO);
        menu.findItem(R.id.menu_discount).setVisible(BModel.configurationMasterHelper.IS_ENTRY_LEVEL_DISCOUNT);

        if (BModel.configurationMasterHelper.IS_SIH_VALIDATION
                && BModel.configurationMasterHelper.SHOW_PRODUCTRETURN) {
            menu.findItem(R.id.menu_product_return).setVisible(true);
        } else {
            menu.findItem(R.id.menu_product_return).setVisible(false);
        }

        if (BModel.configurationMasterHelper.IS_SIH_VALIDATION
                && BModel.configurationMasterHelper.SHOW_COLLECTION_BEFORE_INVOICE
                && BModel.configurationMasterHelper.IS_INVOICE)
            menu.findItem(R.id.menu_collection).setVisible(true);
        else
            menu.findItem(R.id.menu_collection).setVisible(false);

        if (BModel.configurationMasterHelper.SHOW_SIGNATURE_SCREEN)
            menu.findItem(R.id.menu_signature).setVisible(true);


        if (BModel.configurationMasterHelper.SHOW_CALC)
            menu.findItem(R.id.menu_calculator).setVisible(true);
        else
            menu.findItem(R.id.menu_calculator).setVisible(false);

        if ((BModel.configurationMasterHelper.SHOW_STORE_WISE_DISCOUNT_DLG && BModel.configurationMasterHelper.BILL_WISE_DISCOUNT == 0)
                || BModel.configurationMasterHelper.SHOW_TOTAL_DISCOUNT_EDITTEXT)
            menu.findItem(R.id.menu_store_wise_discount).setVisible(true);
        else
            menu.findItem(R.id.menu_store_wise_discount).setVisible(false);

        if (BModel.configurationMasterHelper.SHOW_ORDER_SUMMARY_DETAIL_DIALOG) {
            menu.findItem(R.id.menu_summary_dialog).setVisible(true);
        }

        if (BModel.configurationMasterHelper.SHOW_SERIAL_NO_SCREEN)
            menu.findItem(R.id.menu_serialno).setVisible(true);
        else
            menu.findItem(R.id.menu_serialno).setVisible(false);

        if (BModel.mSelectedModule == 1)
            menu.findItem(R.id.menu_delete).setVisible(false);
        else if (!BModel.isEdit())
            menu.findItem(R.id.menu_delete).setVisible(false);
        else {
            menu.findItem(R.id.menu_delete).setVisible(true);
        }

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
        } else if (i1 == R.id.menu_discount) {

            discountDialog = new DiscountDialog(OrderSummary.this, null,
                    discountDismissListener);
            discountDialog.show();
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
                if (BModel.configurationMasterHelper.SHOW_GROUPPRODUCTRETURN) {
                    productSize = BModel.productHelper
                            .getBomReturnTypeProducts().size();
                } else {
                    productSize = BModel.productHelper.getBomReturnProducts()
                            .size();
                }
                if (productSize > 0) {
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
            CollectionBeforeInvoiceCall();
            return true;
        } else if (i1 == R.id.menu_signature) {
            if (BModel.getOrderHeaderBO().isSignCaptured()) {
                showDialog(DIALOG_SIGNATURE_AVAILABLE);
                return true;
            }
            Intent i = new Intent(OrderSummary.this,
                    CaptureSignatureActivity.class);
            i.putExtra("fromModule", "ORDER");
            startActivity(i);
            BModel.configurationMasterHelper.setSignatureTitle("Signature");
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            finish();
            return true;
        } else if (i1 == R.id.menu_summary_dialog) {
            BModel.configurationMasterHelper.loadOrderSummaryDetailConfig();
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
            editOrder();
        } else if (i1 == R.id.menu_delete) {
            if (orderHelper.isStockCheckMenuEnabled())
                showDialog(DIALOG_DELETE_STOCK_AND_ORDER);
            else
                showDialog(DIALOG_DELETE_ONLY_ORDER);
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
        BModel.downloadBankDetails();
        BModel.downloadBranchDetails();
        BModel.collectionHelper.loadPaymentModes();

        double minimumAmount;
        double creditBalance;

        if (BModel.getRetailerMasterBO().getCreditLimit() != 0) {
            creditBalance = BModel.getRetailerMasterBO().getCreditLimit() - BModel.collectionHelper.calculatePendingOSTAmount();
        } else
            creditBalance = 0;

        if (totalOrderValue < creditBalance)
            minimumAmount = 0;
        else
            minimumAmount = totalOrderValue - creditBalance;

        minimumAmount = Double.parseDouble(BModel.formatValue(minimumAmount));

        if (!isClicked) {
            isClicked = true;

            int paymentModeSize = BModel.collectionHelper.getPaymentModes()
                    .size();
            if (paymentModeSize > 0) {

                collectionBeforeInvoiceDialog = new CollectionBeforeInvoiceDialog(
                        this, this, collectionbo, totalOrderValue,
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
    }

    private boolean isEditMode = false;

    private void editOrder() {
        isEditMode = true;
        discountHelper.clearSchemeFreeProduct(mOrderedProductList);

        if (BModel.configurationMasterHelper.IS_ENTRY_LEVEL_DISCOUNT)
            discountHelper.clearDiscountQuantity();

        if (BModel.remarksHelper.getRemarksBO().getModuleCode() == null
                || BModel.remarksHelper.getRemarksBO().getModuleCode().length() == 0)
            BModel.remarksHelper.getRemarksBO().setModuleCode(StandardListMasterConstants.MENU_STK_ORD);

        BModel.setDoubleEdit_temp(false);


        BModel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                .now(SDUtil.TIME));

        Intent i;
        if (screenCode.equals(HomeScreenTwo.MENU_CATALOG_ORDER)) {
            i = new Intent(OrderSummary.this, CatalogOrder.class);
        } else {
            i = new Intent(OrderSummary.this, StockAndOrder.class);
        }
        i.putExtra("OrderFlag", "FromSummary");
        i.putExtra("ScreenCode", (screenCode == null ? "" : screenCode));

        i.putExtra("tempPo",
                (BModel.getOrderHeaderBO().getPO() == null ? "" : BModel
                        .getOrderHeaderBO().getPO()));
        i.putExtra("tempRemark",
                (BModel.getOrderHeaderBO().getRemark() == null ? ""
                        : BModel.getOrderHeaderBO().getRemark()));
        i.putExtra("tempRField1",
                (BModel.getOrderHeaderBO().getRField1() == null ? ""
                        : BModel.getOrderHeaderBO().getRField1()));
        i.putExtra("tempRField2",
                (BModel.getOrderHeaderBO().getRField2() == null ? ""
                        : BModel.getOrderHeaderBO().getRField2()));

        BModel.setOrderHeaderBO(null);
        startActivity(i);
        finish();
    }

    /**
     * This method will on/off the items based in the configuration.
     */
    private void hideAndSeek() {
        try {
            // if pre-sales disable the following two component
            if (BModel.configurationMasterHelper.IS_INVOICE_AS_MOD)
                button_invoice.setVisibility(View.GONE);

            if (BModel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG && !BModel.configurationMasterHelper.IS_SIH_VALIDATION) {
                button_invoice.setVisibility(View.GONE);
            }

            if (!BModel.configurationMasterHelper.IS_INVOICE)
                button_invoice.setVisibility(View.GONE);

            if (BModel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG) {
                if (BModel.configurationMasterHelper.IS_SIH_VALIDATION) {
                    findViewById(R.id.layoutDelivery).setVisibility(View.GONE);
                }
            } else if (BModel.configurationMasterHelper.IS_INVOICE || !BModel.configurationMasterHelper.SHOW_DELIVERY_DATE) {
                findViewById(R.id.layoutDelivery).setVisibility(View.GONE);
            }

            if (!BModel.configurationMasterHelper.SHOW_LPC_ORDER) {
                findViewById(R.id.ll_lines).setVisibility(View.GONE);
            }

            if (BModel.configurationMasterHelper.SHOW_TOTAL_QTY_ORDER) {
                findViewById(R.id.ll_totqty).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.ll_totqty).setVisibility(View.GONE);
            }

            if (BModel.configurationMasterHelper.SHOW_TOTAL_VALUE_ORDER) {
                findViewById(R.id.ll_values).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.ll_values).setVisibility(View.GONE);
            }


            if (BModel.configurationMasterHelper.IS_SHOW_DISCOUNTS_ORDER_SUMMARY) {
                imageView_amountSplitUp.setVisibility(View.VISIBLE);

            } else {
                imageView_amountSplitUp.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    private void setDeliveryDate() {
        try {
            if (BModel.isEdit()) {

                button_deliveryDate.setText(DateUtil.convertFromServerDateToRequestedFormat(
                        BModel.getDeliveryDate(BModel.getRetailerMasterBO()
                                .getRetailerID()),
                        ConfigurationMasterHelper.outDateFormat));
            } else {
                Calendar origDay = Calendar.getInstance();
                origDay.add(Calendar.DAY_OF_YEAR, (BModel.configurationMasterHelper.DEFAULT_NUMBER_OF_DAYS_TO_DELIVER_ORDER == 0 ? 1 : BModel.configurationMasterHelper.DEFAULT_NUMBER_OF_DAYS_TO_DELIVER_ORDER));

                button_deliveryDate.setText(DateUtil.convertDateObjectToRequestedFormat(origDay.getTime(), ConfigurationMasterHelper.outDateFormat));

            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }


    private void prepareScreenData() {

        int totalQuantityOrdered = 0;
        float totalWeight = 0;

        BModel.getRetailerMasterBO().setBillWiseCompanyDiscount(0);
        BModel.getRetailerMasterBO().setBillWiseDistributorDiscount(0);

        Vector<ProductMasterBO> productList = BModel.productHelper
                .getProductMaster();

        if (productList == null) {
            BModel.showAlert(
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

                if (BModel.configurationMasterHelper.IS_SIH_VALIDATION
                        && BModel.configurationMasterHelper.IS_INVOICE
                        && BModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION) {
                    if (productBO.getBatchwiseProductCount() > 0) {
                        // Apply batch wise price apply
                        lineValue = BModel.schemeDetailsMasterHelper
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

                // Set the calculated values in productBO **/
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


        if (BModel.configurationMasterHelper.IS_SHOW_ORDERING_SEQUENCE) {
            mSortedList = new Vector<>();
            mOrderedProductList = orderHelper.organizeProductsByUserEntry();
            mSortedList.addAll(mOrderedProductList);
        }


        if (BModel.getOrderHeaderBO() != null)
            BModel.getOrderHeaderBO().setTotalWeight(totalWeight);

        // Empties Management is Enabled, then we have to add totalOrderValue with remaining value(Order return value).
        if (BModel.configurationMasterHelper.SHOW_PRODUCTRETURN
                && !BModel.configurationMasterHelper.SHOW_BOTTLE_CREDITLIMIT
                && BModel.configurationMasterHelper.IS_SIH_VALIDATION && BModel.getOrderHeaderBO() != null) {
            totalOrderValue = totalOrderValue
                    + BModel.getOrderHeaderBO().getRemainigValue();
        }

        discountHelper.clearProductDiscountAndTaxValue(mOrderedProductList);


        // Scheme calculations
        if (!BModel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG
                || BModel.configurationMasterHelper.IS_SIH_VALIDATION) {
            totalSchemeDiscValue = discountHelper.calculateSchemeDiscounts(mOrderedProductList);
            totalOrderValue -= totalSchemeDiscValue;
        }

        if (BModel.configurationMasterHelper.IS_REMOVE_TAX_ON_SRP) {
            //applying removed tax..
            BModel.productHelper.taxHelper.applyRemovedTax(mOrderedProductList);
        }


        //  Apply product entry level discount
        if (BModel.configurationMasterHelper.IS_ENTRY_LEVEL_DISCOUNT) {
            entryLevelDiscount = discountHelper.calculateUserEntryLevelDiscount(mOrderedProductList);
            totalOrderValue = totalOrderValue - entryLevelDiscount;
        }

        // Apply Item  level discount
        if (BModel.configurationMasterHelper.SHOW_DISCOUNT) {
            double itemLevelDiscount = discountHelper.calculateItemLevelDiscount();
            totalOrderValue = totalOrderValue - itemLevelDiscount;
        }

        // Apply Exclude Item level Tax  in Product
        if (BModel.configurationMasterHelper.SHOW_TAX) {
            BModel.productHelper.taxHelper.updateProductWiseTax();
        }


        listView.setAdapter(new ProductExpandableAdapter());
        for (int i = 0; i < mOrderedProductList.size(); i++) {
            listView.expandGroup(i);
        }


        if (BModel.configurationMasterHelper.SHOW_STORE_WISE_DISCOUNT_DLG && BModel.configurationMasterHelper.BILL_WISE_DISCOUNT == 0) {
            //find the  range of discount by using total value
            final double billWiseRangeDiscount = discountHelper.calculateBillWiseRangeDiscount(totalOrderValue);
            totalOrderValue = totalOrderValue - billWiseRangeDiscount;

        } else if (BModel.configurationMasterHelper.SHOW_STORE_WISE_DISCOUNT_DLG && BModel.configurationMasterHelper.BILL_WISE_DISCOUNT == 1) {
            // Automatically apply bill wise discount
            final double billWiseDiscount = discountHelper.calculateBillWiseDiscount(totalOrderValue);
            if (BModel.getOrderHeaderBO() != null) {
                BModel.getOrderHeaderBO().setDiscountValue(billWiseDiscount);
            }
            totalOrderValue = totalOrderValue - billWiseDiscount;

        } else {
            // user manually enter bill wise discount
            double discount = BModel.orderAndInvoiceHelper.restoreDiscountAmount(BModel
                    .getRetailerMasterBO().getRetailerID());
            double billWiseDiscount = applyDiscountMaxValidation(discount);
            totalOrderValue = totalOrderValue - billWiseDiscount;
        }

        // Apply bill wise pay term discount
        // Apply bill wise payterm discount
        if (discountHelper.getBillWisePayternDiscountList() != null
                && discountHelper.getBillWisePayternDiscountList().size() > 0) {
            final double billWisePayTermDiscount = discountHelper.calculateBillWisePayTermDiscount(totalOrderValue);
            totalOrderValue = totalOrderValue - billWisePayTermDiscount;
        }

        // To open the dialog back while resuming
        if (!isDiscountDialog() && BModel.configurationMasterHelper.IS_ENTRY_LEVEL_DISCOUNT && discountDialog != null && discountDialog.isShowing()) {
            setDiscountDialog(true);
            discountDialog.dismiss();
            discountDialog = null;
            discountDialog = new DiscountDialog(OrderSummary.this, null,
                    discountDismissListener);
            discountDialog.show();
        }

        //updating footer labels
        text_totalOrderValue.setText(BModel.formatValue(totalOrderValue));
        text_LPC.setText(String.valueOf(mOrderedProductList.size()));
        text_totalOrderedQuantity.setText(String.valueOf(totalQuantityOrdered));

    }

    private void callAmountSplitUpScreen() {

        double cmy_disc = 0, dist_disc = 0;
        for (ProductMasterBO productMasterBO : mOrderedProductList) {
            cmy_disc = cmy_disc + productMasterBO.getCompanyTypeDiscount();
            dist_disc = dist_disc + productMasterBO.getDistributorTypeDiscount();
        }
        double cmyDiscount = cmy_disc + BModel.getRetailerMasterBO().getBillWiseCompanyDiscount();
        double distDiscount = dist_disc + BModel.getRetailerMasterBO().getBillWiseDistributorDiscount();

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
            amountSplitUpDialog.setArguments(args);
            amountSplitUpDialog.show(getSupportFragmentManager(), "AmtSplitupDialog");
        }

    }


    @Override
    public void onDiscountDismiss(String result, int result1, int result2, int result3) {
        if (BModel.configurationMasterHelper.SHOW_STORE_WISE_DISCOUNT_DLG && BModel.configurationMasterHelper.BILL_WISE_DISCOUNT == 0) {

            final double totalValue = discountHelper.calculateBillWiseRangeDiscount(totalOrderValue);
            text_totalOrderValue.setText(BModel.formatValue(totalValue));

        } else if (BModel.configurationMasterHelper.SHOW_TOTAL_DISCOUNT_EDITTEXT) {
            try {
                int f1 = 0;
                String qty = result;
                if ("".equals(qty)) {
                    qty = "0";
                }

                enteredDiscAmtOrPercent = SDUtil.convertToDouble(qty);

                if (enteredDiscAmtOrPercent != 0 && BModel.configurationMasterHelper.discountType == 1 && enteredDiscAmtOrPercent > 100) {
                    f1 = (int) (enteredDiscAmtOrPercent / 10);
                }

                String strDiscountAppliedValue = BModel.formatValue(getDiscountAppliedValue(SDUtil.convertToDouble(f1 + "")));

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
                if (BModel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG) {
                    if (BModel.configurationMasterHelper.IS_SIH_VALIDATION) {
                        delivery_date_txt = "";
                    }
                } else if (BModel.configurationMasterHelper.IS_INVOICE || !BModel.configurationMasterHelper.SHOW_DELIVERY_DATE) {
                    delivery_date_txt = "";
                }

                AlertDialog.Builder builder2 = new AlertDialog.Builder(OrderSummary.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(getResources().getString(R.string.order_saved_locally_order_id_is) + orderHelper.getOrderId())
                        .setMessage((delivery_date_txt.equals("") ? "" : getResources().getString(R.string.delivery_date_is) + " " + delivery_date_txt))
                        .setPositiveButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {

                                        printOrder();

                                    }
                                });


                BModel.applyAlertDialogTheme(builder2);

                break;
            }

            case DIALOG_ORDER_SAVED_WITH_PRINT_OPTION: {

                delivery_date_txt = button_deliveryDate.getText().toString();
                if (BModel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG) {
                    if (BModel.configurationMasterHelper.IS_SIH_VALIDATION) {
                        delivery_date_txt = "";
                    }
                } else if (BModel.configurationMasterHelper.IS_INVOICE || !BModel.configurationMasterHelper.SHOW_DELIVERY_DATE) {
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

                                        Intent i = new Intent(OrderSummary.this,
                                                HomeScreenTwo.class);
                                        Bundle extras = getIntent().getExtras();
                                        if (extras != null) {
                                            i.putExtra("IsMoveNextActivity", BModel.configurationMasterHelper.MOVE_NEXT_ACTIVITY);
                                            i.putExtra("CurrentActivityCode", mCurrentActivityCode);
                                        }
                                        startActivity(i);
                                        finish();
                                    }
                                })
                        .setPositiveButton(
                                getResources().getString(R.string.print_order),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

                                        if (BModel.configurationMasterHelper.SHOW_ZEBRA_TITAN
                                                || BModel.configurationMasterHelper.SHOW_ZEBRA_UNIPAL) {

                                            showDialog(DIALOG_NUMBER_OF_PRINTS_ORDER);
                                        } else {
                                            printOrder();
                                        }
                                    }
                                });


                BModel.applyAlertDialogTheme(builder1);
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

                                        BModel.getOrderHeaderBO().setIsSignCaptured(false);
                                        if (BModel.getOrderHeaderBO().getSignatureName() != null)
                                            BModel.synchronizationHelper.deleteFiles(
                                                    PHOTO_PATH, BModel.getOrderHeaderBO().getSignatureName());

                                        if (!BModel.hasStockInOrder())
                                            BModel.deleteModuleCompletion("MENU_STK_ORD");
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
                                        BModel.getOrderHeaderBO().setIsSignCaptured(false);
                                        if (BModel.getOrderHeaderBO().getSignatureName() != null)
                                            BModel.synchronizationHelper.deleteFiles(
                                                    PHOTO_PATH, BModel.getOrderHeaderBO().getSignatureName());

                                        discountHelper.clearSchemeFreeProduct(mOrderedProductList);
                                        BModel.deleteModuleCompletion("MENU_STK_ORD");
                                        new MyThread(OrderSummary.this,
                                                DataMembers.DELETE_STOCK_AND_ORDER).start();
                                    }
                                });
                BModel.applyAlertDialogTheme(builder);
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
                                        BModel.getOrderHeaderBO().setIsSignCaptured(false);
                                        if (BModel.getOrderHeaderBO().getSignatureName() != null)
                                            BModel.synchronizationHelper.deleteFiles(
                                                    PHOTO_PATH, BModel.getOrderHeaderBO().getSignatureName());

                                        customProgressDialog(build, getResources().getString(R.string.deleting_order));
                                        alertDialog = build.create();
                                        alertDialog.show();
                                        BModel.deleteModuleCompletion("MENU_STK_ORD");
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
                BModel.applyAlertDialogTheme(builder4);
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

                                        if (BModel.configurationMasterHelper.printCount > 0) {
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

                                        BModel.productHelper.clearOrderTableChecked();
                                        Intent i = new Intent(OrderSummary.this, HomeScreenTwo.class);
                                        startActivity(i);
                                        finish();
                                    }
                                });
                BModel.applyAlertDialogTheme(builder9);
                break;
            }

            case DIALOG_NUMBER_OF_PRINTS_ORDER: {

                AlertDialog.Builder builder11 = new AlertDialog.Builder(OrderSummary.this)
                        .setTitle("Print Count")
                        .setSingleChoiceItems(BModel.printHelper.getPrintCountArray(), 0, null)
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
                BModel.applyAlertDialogTheme(builder11);

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
                BModel.applyAlertDialogTheme(builder5);
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
                                        BModel.getOrderHeaderBO().setIsSignCaptured(false);
                                        if (BModel.getOrderHeaderBO().getSignatureName() != null)
                                            BModel.synchronizationHelper.deleteFiles(
                                                    PHOTO_PATH, BModel.getOrderHeaderBO().getSignatureName());
                                        Intent i = new Intent(OrderSummary.this,
                                                CaptureSignatureActivity.class);
                                        i.putExtra("fromModule", "ORDER");
                                        startActivity(i);
                                        BModel.configurationMasterHelper.setSignatureTitle("Signature");
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
                BModel.applyAlertDialogTheme(builder7);
                break;
            }

            case DIALOG_DELIVERY_DATE_PICKER: {

                Calendar c = Calendar.getInstance();
                c.add(Calendar.DAY_OF_YEAR, (BModel.configurationMasterHelper.DEFAULT_NUMBER_OF_DAYS_TO_DELIVER_ORDER == 0 ? 1 : BModel.configurationMasterHelper.DEFAULT_NUMBER_OF_DAYS_TO_DELIVER_ORDER));

                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                MyDatePickerDialog dialog = new MyDatePickerDialog(this, R.style.DatePickerDialogStyle,
                        mDeliverDatePickerListener, year, month, day);
                dialog.setPermanentTitle(getResources().getString(R.string.choose_date));
                dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

                if (BModel.configurationMasterHelper.MAX_NUMBER_OF_DAYS_ALLOWED_TO_DELIVER > 0) {
                    Calendar maxCalendar = Calendar.getInstance();
                    maxCalendar.add(Calendar.DAY_OF_YEAR, BModel.configurationMasterHelper.MAX_NUMBER_OF_DAYS_ALLOWED_TO_DELIVER);
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

        BModel = (BusinessModel) getApplicationContext();
        BModel.setContext(this);

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

        IndicativeOrderReasonDialog indicativeReasonDialog;
        isFromOrder = true;

        if (BModel.configurationMasterHelper.IS_SHOW_ORDERING_SEQUENCE && mSortedList != null)
            orderHelper.setSortedOrderedProducts(mSortedList);


        if (!isClick) {

            isClick = true;

            if (mOrderedProductList.size() > 0) {

                if ((BModel.configurationMasterHelper.IS_GST || BModel.configurationMasterHelper.IS_GST_HSN) && !orderHelper.isTaxAvailableForAllOrderedProduct(mOrderedProductList)) {
                    // If GST enabled then, every ordered product should have tax
                    BModel.showAlert(
                            getResources()
                                    .getString(
                                            R.string.tax_not_availble_for_some_product),
                            0);
                    isClick = false;
                    return;
                }

                if ((BModel.configurationMasterHelper.IS_SHOW_ONLY_INDICATIVE_ORDER || BModel.configurationMasterHelper.IS_SHOW_ORDER_REASON) && !orderHelper.isReasonProvided(mOrderedProductList)) {

                    indicativeReasonDialog = new IndicativeOrderReasonDialog(this, BModel);
                    indicativeReasonDialog.show();
                    isClick = false;

                } else {

                    BModel.getOrderHeaderBO().setOrderValue(getDiscountAppliedValue(enteredDiscAmtOrPercent));
                    BModel.getOrderHeaderBO().setDiscount(enteredDiscAmtOrPercent);
                    BModel.getOrderHeaderBO().setDiscountId(0);
                    BModel.getOrderHeaderBO().setIsCompanyGiven(0);
                    BModel.getOrderHeaderBO().setLinesPerCall(SDUtil.convertToInt((String) text_LPC.getText()));
                    BModel.getOrderHeaderBO().setDeliveryDate(DateUtil.convertToServerDateFormat(button_deliveryDate.getText().toString(),
                            ConfigurationMasterHelper.outDateFormat));


                    // Don't write any code  after this dialog.. because it is just a confirmation dialog
                    orderConfirmationDialog = new OrderConfirmationDialog(this, false, mOrderedProductList, totalOrderValue);
                    orderConfirmationDialog.show();
                    Window window = orderConfirmationDialog.getWindow();
                    window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    orderConfirmationDialog.setCancelable(false);

                }

            } else {
                Toast.makeText(
                        this,
                        getResources().getString(
                                R.string.no_products_exists),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveInvoice() {

        isFromOrder = false;
        IndicativeOrderReasonDialog indicativeReasonDialog;

        if (BModel.configurationMasterHelper.IS_SHOW_ORDERING_SEQUENCE && mSortedList != null)
            orderHelper.setSortedOrderedProducts(mSortedList);


        if ((BModel.configurationMasterHelper.IS_GST || BModel.configurationMasterHelper.IS_GST_HSN) && !orderHelper.isTaxAvailableForAllOrderedProduct(mOrderedProductList)) {
            // If GST enabled then, every ordered product should have tax
            BModel.showAlert(
                    getResources()
                            .getString(
                                    R.string.tax_not_availble_for_some_product),
                    0);
            isClick = false;
            return;
        }

        if (!isClick) {
            isClick = true;

            if (BModel.configurationMasterHelper.IS_SIH_VALIDATION && !orderHelper.isStockAvailableToDeliver(mOrderedProductList)) {
                Toast.makeText(
                        this,
                        getResources()
                                .getString(
                                        R.string.stock_not_available_to_deliver),
                        Toast.LENGTH_SHORT).show();
                isClick = false;
                return;
            }

            if (BModel.configurationMasterHelper.IS_VALIDATE_NEGATIVE_INVOICE) {
                if (totalOrderValue < 0) {
                    showDialog(DIALOG_NEGATIVE_INVOICE_CHECK);
                    return;
                }
            }

            if (BModel.configurationMasterHelper.IS_TAX_APPLIED_VALIDATION) {
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

            if (BModel.configurationMasterHelper.SHOW_COLLECTION_BEFORE_INVOICE) {

                double pendingAmount;
                double collectedAmount = 0;

                pendingAmount = BModel.getRetailerMasterBO().getCreditLimit() - BModel.collectionHelper.calculatePendingOSTAmount();

                if (collectionbo.getCashamt() > 0
                        || collectionbo.getChequeamt() > 0 || collectionbo.getCreditamt() > 0) {
                    collectedAmount = collectionbo.getCashamt()
                            + collectionbo.getChequeamt() + collectionbo.getCreditamt();
                }

                collectedAmount = Double.parseDouble(BModel.formatValue(collectedAmount));
                pendingAmount = collectedAmount + pendingAmount;
                pendingAmount = Double.parseDouble(BModel.formatValue(pendingAmount));

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


            BModel.getOrderHeaderBO().setOrderValue(getDiscountAppliedValue(enteredDiscAmtOrPercent));
            BModel.getOrderHeaderBO().setDiscount(enteredDiscAmtOrPercent);
            BModel.getOrderHeaderBO().setDiscountId(0);
            BModel.getOrderHeaderBO().setIsCompanyGiven(0);
            BModel.getOrderHeaderBO().setLinesPerCall(SDUtil.convertToInt((String) text_LPC.getText()));
            BModel.getOrderHeaderBO().setDeliveryDate(DateUtil.convertToServerDateFormat(button_deliveryDate.getText().toString(), ConfigurationMasterHelper.outDateFormat));

            if (!mOrderedProductList.isEmpty()) {

                if (orderHelper.isAllScanned() || !BModel.configurationMasterHelper.SHOW_SERIAL_NO_SCREEN) {

                    if (orderHelper.hasOrder(mOrderedProductList)) {

                        if ((BModel.configurationMasterHelper.IS_SHOW_ONLY_INDICATIVE_ORDER || BModel.configurationMasterHelper.IS_SHOW_ORDER_REASON) && !orderHelper.isReasonProvided(mOrderedProductList)) {

                            indicativeReasonDialog = new IndicativeOrderReasonDialog(this, BModel);
                            indicativeReasonDialog.show();
                            isClick = false;

                        } else {

                            if (BModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION
                                    && BModel.configurationMasterHelper.IS_SIH_VALIDATION
                                    && BModel.configurationMasterHelper.IS_INVOICE) {
                                BModel.batchAllocationHelper
                                        .loadFreeProductBatchList();
                            }

                            orderHelper.invoiceDiscount = Double.toString(enteredDiscAmtOrPercent);


                            // Don't write any code  after this dialog.. because it is just a confirmation dialog
                            orderConfirmationDialog = new OrderConfirmationDialog(this, true, mOrderedProductList, totalOrderValue);
                            orderConfirmationDialog.show();
                            Window window = orderConfirmationDialog.getWindow();
                            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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
                BModel.showAlert(
                        getResources().getString(R.string.no_products_exists),
                        0);
            }
        }
    }


    public Handler getHandler() {
        return handler;
    }

    private double getDiscountAppliedValue(double discount) {
        double total;
        total = totalOrderValue;

        double discountValue = 0;
        try {
            if (BModel.configurationMasterHelper.discountType == 1) {
                if (discount > 100)
                    discount = 100;
                discountValue = (total / 100) * discount;

            } else if (BModel.configurationMasterHelper.discountType == 2) {
                discountValue = discount;

            }

            total = total - discountValue;
            BModel.getOrderHeaderBO().setDiscountValue(discountValue);
        } catch (Exception e) {
            Commons.printException(e);
        }
        return total;
    }


    private double applyDiscountMaxValidation(double discount) {
        try {
            if (BModel.configurationMasterHelper.discountType == 1) {
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
            for (ProductMasterBO sku : BModel.productHelper.getProductMaster()) {
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
                    if (sku.getIsscheme() == 1) {
                        percent = sku.getMschemeper();
                    }

                    float goldenStore = 0;
                    if (BModel.configurationMasterHelper.SHOW_GOLD_STORE_DISCOUNT
                            && BModel.productHelper
                            .isGoldenStoreInCurrentandLastVisit()) {

                        goldenStore = (float) BModel.productHelper
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

        for (ProductMasterBO productBO : BModel.productHelper.getProductMaster()) {
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

        BModel.showAlert(msg, 0);
    }


    @Override
    public void save(boolean isInvoice) {
        try {
            if (orderConfirmationDialog != null)
                orderConfirmationDialog.dismiss();

            if (isInvoice) {

                if (BModel.configurationMasterHelper.IS_INVOICE) {
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
                if (BModel.configurationMasterHelper.IS_FOCUSBRAND_COUNT_IN_REPORT || BModel.configurationMasterHelper.IS_MUSTSELL_COUNT_IN_REPORT)
                    orderHelper.getFocusAndMustSellOrderedProducts(mOrderedProductList);

                //Adding accumulation scheme free products to the last ordered product list, so that it will listed on print
                orderHelper.updateOffInvoiceSchemeInProductOBJ(mOrderedProductList, totalOrderValue);

                new MyThread(this, DataMembers.SAVEINVOICE).start();
            } else {

                build = new AlertDialog.Builder(OrderSummary.this);

                customProgressDialog(build, getResources().getString(R.string.saving_new_order));
                alertDialog = build.create();
                alertDialog.show();
                if (BModel.configurationMasterHelper.IS_FOCUSBRAND_COUNT_IN_REPORT || BModel.configurationMasterHelper.IS_MUSTSELL_COUNT_IN_REPORT)
                    orderHelper.getFocusAndMustSellOrderedProducts(mOrderedProductList);

                if (orderHelper.hasOrder(mOrderedProductList)) {

                    if (BModel.configurationMasterHelper.SHOW_BATCH_ALLOCATION
                            && BModel.configurationMasterHelper.IS_SIH_VALIDATION
                            && BModel.configurationMasterHelper.IS_INVOICE) {
                        BModel.batchAllocationHelper
                                .loadFreeProductBatchList();
                    }


                    orderHelper.invoiceDiscount = Double.toString(enteredDiscAmtOrPercent);

                    new MyThread(OrderSummary.this,
                            DataMembers.SAVEORDERANDSTOCK).start();
                    BModel.saveModuleCompletion("MENU_STK_ORD");


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

    public boolean isDiscountDialog() {
        return isDiscountDialog;
    }

    public void setDiscountDialog(boolean discountDialog) {
        isDiscountDialog = discountDialog;
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
                holder.text_productName = (TextView) row
                        .findViewById(R.id.PRODUCTNAME);
                holder.pcsQty = (TextView) row.findViewById(R.id.P_QUANTITY);
                holder.caseQty = (TextView) row.findViewById(R.id.C_QUANTITY);
                holder.tw_srp = (TextView) row.findViewById(R.id.MRP);
                holder.text_total = (TextView) row.findViewById(R.id.TOTAL);
                holder.outerQty = (TextView) row.findViewById(R.id.OC_QUANTITY);
                holder.weight = (TextView) row.findViewById(R.id.tv_weight);

                holder.shelfCaseQty = (TextView) row.findViewById(R.id.sc_quantity);
                holder.shelfOuterQty = (TextView) row.findViewById(R.id.sho_quantity);
                holder.shelfPieceQty = (TextView) row.findViewById(R.id.sp_quantity);

                holder.text_productName.setTypeface(BModel.configurationMasterHelper.getProductNameFont());
                holder.text_total.setTypeface(BModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

                (row.findViewById(R.id.view_dotted_line)).setLayerType(View.LAYER_TYPE_SOFTWARE, null);


                if (!"MENU_ORDER".equals(screenCode) && BModel.configurationMasterHelper.SHOW_STOCK_IN_SUMMARY) {

                    if (BModel.configurationMasterHelper.SHOW_STOCK_SC) {
                        (row.findViewById(R.id.llShelfCase)).setVisibility(View.VISIBLE);
                        ((TextView) row.findViewById(R.id.shelfCaseTitle)).setTypeface(BModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                        holder.shelfCaseQty.setTypeface(BModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                        try {
                            if (BModel.labelsMasterHelper.applyLabels(row.findViewById(
                                    R.id.shelfCaseTitle).getTag()) != null)
                                ((TextView) row.findViewById(R.id.shelfCaseTitle))
                                        .setText(BModel.labelsMasterHelper
                                                .applyLabels(row.findViewById(
                                                        R.id.shelfCaseTitle)
                                                        .getTag()));
                        } catch (Exception e) {
                            Commons.printException(e);
                        }

                    }
                    if (BModel.configurationMasterHelper.SHOW_SHELF_OUTER) {
                        (row.findViewById(R.id.llShelfPiece)).setVisibility(View.VISIBLE);
                        ((TextView) row.findViewById(R.id.shelfOuterTitle)).setTypeface(BModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                        holder.shelfOuterQty.setTypeface(BModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                        try {
                            if (BModel.labelsMasterHelper.applyLabels(row.findViewById(
                                    R.id.shelfOuterTitle).getTag()) != null)
                                ((TextView) row.findViewById(R.id.shelfOuterTitle))
                                        .setText(BModel.labelsMasterHelper
                                                .applyLabels(row.findViewById(
                                                        R.id.shelfOuterTitle)
                                                        .getTag()));
                        } catch (Exception e) {
                            Commons.printException(e);
                        }

                    }

                    if (BModel.configurationMasterHelper.SHOW_STOCK_SP) {
                        (row.findViewById(R.id.llShelfPiece)).setVisibility(View.VISIBLE);
                        ((TextView) row.findViewById(R.id.shelfPcsTitle)).setTypeface(BModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                        holder.shelfPieceQty.setTypeface(BModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                        try {
                            if (BModel.labelsMasterHelper.applyLabels(row.findViewById(
                                    R.id.shelfPcsTitle).getTag()) != null)
                                ((TextView) row.findViewById(R.id.shelfPcsTitle))
                                        .setText(BModel.labelsMasterHelper
                                                .applyLabels(row.findViewById(
                                                        R.id.shelfPcsTitle)
                                                        .getTag()));
                        } catch (Exception e) {
                            Commons.printException(e);
                        }
                    }
                }
                // On/Off order case and pce
                if (!BModel.configurationMasterHelper.SHOW_ORDER_CASE) {
                    (row.findViewById(R.id.llCase)).setVisibility(View.GONE);
                } else {
                    ((TextView) row.findViewById(R.id.caseTitle)).setTypeface(BModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                    holder.caseQty.setTypeface(BModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    try {
                        if (BModel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.caseTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.caseTitle))
                                    .setText(BModel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.caseTitle).getTag()));
                    } catch (Exception e) {
                        Commons.printException(" " + e);
                    }
                }


                if (!BModel.configurationMasterHelper.SHOW_ORDER_PCS) {
                    (row.findViewById(R.id.llPiece)).setVisibility(View.GONE);
                } else {
                    ((TextView) row.findViewById(R.id.pcsTitle)).setTypeface(BModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                    holder.pcsQty.setTypeface(BModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    try {
                        if (BModel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.pcsTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.pcsTitle))
                                    .setText(BModel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.pcsTitle).getTag()));
                    } catch (Exception e) {
                        Commons.printException(" " + e);
                    }
                }
                if (!BModel.configurationMasterHelper.SHOW_OUTER_CASE) {
                    (row.findViewById(R.id.llOuter)).setVisibility(View.GONE);
                } else {
                    ((TextView) row.findViewById(R.id.outercaseTitle)).setTypeface(BModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                    holder.outerQty.setTypeface(BModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    try {
                        if (BModel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.outercaseTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.outercaseTitle))
                                    .setText(BModel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.outercaseTitle).getTag()));
                    } catch (Exception e) {
                        Commons.printException(" " + e);
                    }
                }
                if (!BModel.configurationMasterHelper.SHOW_ORDER_WEIGHT) {
                    (row.findViewById(R.id.llShelfWeight)).setVisibility(View.GONE);
                } else {
                    ((TextView) row.findViewById(R.id.weighttitle)).setTypeface(BModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                    holder.caseQty.setTypeface(BModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    try {
                        if (BModel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.weighttitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.weighttitle))
                                    .setText(BModel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.weighttitle).getTag()));
                    } catch (Exception e) {
                        Commons.printException(" " + e);
                    }
                }
                if (!BModel.configurationMasterHelper.SHOW_STK_ORD_SRP) {
                    (row.findViewById(R.id.llSrp)).setVisibility(View.GONE);
                } else {
                    ((TextView) row.findViewById(R.id.srpTitle)).setTypeface(BModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                    holder.tw_srp.setTypeface(BModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    try {
                        if (BModel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.srpTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.srpTitle))
                                    .setText(BModel.labelsMasterHelper
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

            holder.text_productName.setText(productBO.getProductName());
            holder.productBO = BModel.productHelper
                    .getProductMasterBOById(productBO.getProductId());

            holder.shelfCaseQty.setText(String.valueOf(holder.productBO.getLocations().get(BModel.productHelper.getmSelectedLocationIndex()).getShelfCase()));
            holder.shelfOuterQty.setText(String.valueOf(holder.productBO.getLocations().get(BModel.productHelper.getmSelectedLocationIndex()).getShelfOuter()));
            holder.shelfPieceQty.setText(String.valueOf(holder.productBO.getLocations().get(BModel.productHelper.getmSelectedLocationIndex()).getShelfPiece()));

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

                } else {
                    holder.pcsQty.setText(String.valueOf(productBO.getQuantitySelected()));
                    holder.caseQty.setText(String.valueOf(0));
                    holder.outerQty.setText(String.valueOf(0));
                }
            }

            holder.tw_srp.setText(SDUtil.roundIt(0, 2));

            if (!BModel.configurationMasterHelper.SHOW_STK_ORD_SRP) {
                holder.text_total.setVisibility(View.GONE);
            } else {
                holder.text_total.setText("0");
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
                holder.text_productName = (TextView) row
                        .findViewById(R.id.PRODUCTNAME);
                holder.pcsQty = (TextView) row.findViewById(R.id.P_QUANTITY);
                holder.caseQty = (TextView) row.findViewById(R.id.C_QUANTITY);
                holder.tw_srp = (TextView) row.findViewById(R.id.MRP);
                holder.text_total = (TextView) row.findViewById(R.id.TOTAL);
                holder.outerQty = (TextView) row.findViewById(R.id.OC_QUANTITY);
                holder.weight = (TextView) row.findViewById(R.id.tv_weight);

                holder.shelfCaseQty = (TextView) row.findViewById(R.id.sc_quantity);
                holder.shelfOuterQty = (TextView) row.findViewById(R.id.sho_quantity);
                holder.shelfPieceQty = (TextView) row.findViewById(R.id.sp_quantity);

                holder.text_productName.setMaxLines(BModel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);
                holder.text_productName.setTypeface(BModel.configurationMasterHelper.getProductNameFont());
                (row.findViewById(R.id.view_dotted_line)).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                holder.text_total.setTypeface(BModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

                if (!"MENU_ORDER".equals(screenCode) && BModel.configurationMasterHelper.SHOW_STOCK_IN_SUMMARY) {


                    if (BModel.configurationMasterHelper.SHOW_STOCK_SC) {
                        (row.findViewById(R.id.llShelfCase)).setVisibility(View.VISIBLE);
                        ((TextView) row.findViewById(R.id.shelfCaseTitle)).setTypeface(BModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                        holder.shelfCaseQty.setTypeface(BModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                        try {
                            if (BModel.labelsMasterHelper.applyLabels(row.findViewById(
                                    R.id.shelfCaseTitle).getTag()) != null)
                                ((TextView) row.findViewById(R.id.shelfCaseTitle))
                                        .setText(BModel.labelsMasterHelper
                                                .applyLabels(row.findViewById(
                                                        R.id.shelfCaseTitle)
                                                        .getTag()));
                        } catch (Exception e) {
                            Commons.printException(e);
                        }

                    }
                    if (BModel.configurationMasterHelper.SHOW_SHELF_OUTER) {
                        (row.findViewById(R.id.llShelfPiece)).setVisibility(View.VISIBLE);
                        ((TextView) row.findViewById(R.id.shelfOuterTitle)).setTypeface(BModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                        holder.shelfOuterQty.setTypeface(BModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                        try {
                            if (BModel.labelsMasterHelper.applyLabels(row.findViewById(
                                    R.id.shelfOuterTitle).getTag()) != null)
                                ((TextView) row.findViewById(R.id.shelfOuterTitle))
                                        .setText(BModel.labelsMasterHelper
                                                .applyLabels(row.findViewById(
                                                        R.id.shelfOuterTitle)
                                                        .getTag()));
                        } catch (Exception e) {
                            Commons.printException(e);
                        }

                    }

                    if (BModel.configurationMasterHelper.SHOW_STOCK_SP) {
                        (row.findViewById(R.id.llShelfPiece)).setVisibility(View.VISIBLE);
                        ((TextView) row.findViewById(R.id.shelfPcsTitle)).setTypeface(BModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                        holder.shelfPieceQty.setTypeface(BModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                        try {
                            if (BModel.labelsMasterHelper.applyLabels(row.findViewById(
                                    R.id.shelfPcsTitle).getTag()) != null)
                                ((TextView) row.findViewById(R.id.shelfPcsTitle))
                                        .setText(BModel.labelsMasterHelper
                                                .applyLabels(row.findViewById(
                                                        R.id.shelfPcsTitle)
                                                        .getTag()));
                        } catch (Exception e) {
                            Commons.printException(e);
                        }
                    }
                }

                // On/Off order case and pce
                if (!BModel.configurationMasterHelper.SHOW_ORDER_CASE) {
                    (row.findViewById(R.id.llCase)).setVisibility(View.GONE);
                } else {
                    ((TextView) row.findViewById(R.id.caseTitle)).setTypeface(BModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    holder.caseQty.setTypeface(BModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    try {
                        if (BModel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.caseTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.caseTitle))
                                    .setText(BModel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.caseTitle).getTag()));
                    } catch (Exception e) {
                        Commons.printException(" " + e);
                    }
                }


                if (!BModel.configurationMasterHelper.SHOW_ORDER_PCS) {
                    (row.findViewById(R.id.llPiece)).setVisibility(View.GONE);
                } else {
                    ((TextView) row.findViewById(R.id.pcsTitle)).setTypeface(BModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    holder.pcsQty.setTypeface(BModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    try {
                        if (BModel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.pcsTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.pcsTitle))
                                    .setText(BModel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.pcsTitle).getTag()));
                    } catch (Exception e) {
                        Commons.printException(" " + e);
                    }
                }
                if (!BModel.configurationMasterHelper.SHOW_OUTER_CASE) {
                    (row.findViewById(R.id.llOuter)).setVisibility(View.GONE);
                } else {
                    ((TextView) row.findViewById(R.id.outercaseTitle)).setTypeface(BModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    holder.outerQty.setTypeface(BModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    try {
                        if (BModel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.outercaseTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.outercaseTitle))
                                    .setText(BModel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.outercaseTitle).getTag()));
                    } catch (Exception e) {
                        Commons.printException(" " + e);
                    }
                }
                if (!BModel.configurationMasterHelper.SHOW_ORDER_WEIGHT) {
                    (row.findViewById(R.id.llShelfWeight)).setVisibility(View.GONE);
                } else {
                    ((TextView) row.findViewById(R.id.weighttitle)).setTypeface(BModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    holder.caseQty.setTypeface(BModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    try {
                        if (BModel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.weighttitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.weighttitle))
                                    .setText(BModel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.weighttitle).getTag()));
                    } catch (Exception e) {
                        Commons.printException(" " + e);
                    }
                }
                if (!BModel.configurationMasterHelper.SHOW_STK_ORD_SRP) {
                    (row.findViewById(R.id.llSrp)).setVisibility(View.GONE);
                } else {
                    ((TextView) row.findViewById(R.id.srpTitle)).setTypeface(BModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    holder.tw_srp.setTypeface(BModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    try {
                        if (BModel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.srpTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.srpTitle))
                                    .setText(BModel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.srpTitle).getTag()));
                    } catch (Exception e) {
                        Commons.printException(" " + e);
                    }
                }

                if (!BModel.configurationMasterHelper.SHOW_STK_ORD_SRP) {
                    holder.text_total.setVisibility(View.GONE);
                }

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.productBO = mOrderedProductList.get(groupPosition);
            holder.text_productName.setText(holder.productBO.getProductShortName());
            holder.pcsQty.setText(String.valueOf(holder.productBO.getOrderedPcsQty()));
            holder.caseQty.setText(String.valueOf(holder.productBO.getOrderedCaseQty()));
            holder.tw_srp.setText(BModel.formatValue(holder.productBO.getSrp()));
            holder.outerQty.setText(String.valueOf(holder.productBO.getOrderedOuterQty()));

            holder.shelfCaseQty.setText(String.valueOf(((holder.productBO.getLocations().get(BModel.productHelper.getmSelectedLocationIndex()).getShelfCase() == -1) ? 0 : holder.productBO.getLocations().get(BModel.productHelper.getmSelectedLocationIndex()).getShelfCase())));
            holder.shelfOuterQty.setText(String.valueOf(((holder.productBO.getLocations().get(BModel.productHelper.getmSelectedLocationIndex()).getShelfOuter() == -1) ? 0 : holder.productBO.getLocations().get(BModel.productHelper.getmSelectedLocationIndex()).getShelfOuter())));
            holder.shelfPieceQty.setText(String.valueOf(((holder.productBO.getLocations().get(BModel.productHelper.getmSelectedLocationIndex()).getShelfPiece() == -1) ? 0 : holder.productBO.getLocations().get(BModel.productHelper.getmSelectedLocationIndex()).getShelfPiece())));

            holder.text_total.setText(String.valueOf(BModel.formatValue(holder.productBO
                    .getDiscount_order_value())));
            int weight = holder.productBO.getOrderedPcsQty() + (holder.productBO.getOrderedCaseQty() * holder.productBO.getCaseSize()) + (holder.productBO.getOrderedOuterQty() * holder.productBO.getOutersize());
            holder.weight.setText(String.valueOf(weight * holder.productBO.getWeight()));

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
    }


    private final DatePickerDialog.OnDateSetListener mDeliverDatePickerListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            Calendar selectedDate = new GregorianCalendar(year, monthOfYear,
                    dayOfMonth);
            button_deliveryDate.setText(DateUtil.convertDateObjectToRequestedFormat(
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
                defaultCalendar.add(Calendar.DAY_OF_YEAR, (BModel.configurationMasterHelper.DEFAULT_NUMBER_OF_DAYS_TO_DELIVER_ORDER == 0 ? 1 : BModel.configurationMasterHelper.DEFAULT_NUMBER_OF_DAYS_TO_DELIVER_ORDER));
                button_deliveryDate.setText(DateUtil.convertDateObjectToRequestedFormat(defaultCalendar.getTime(), ConfigurationMasterHelper.outDateFormat));
            }
        }
    };


    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            isClick = false;


            if (msg.what == DataMembers.NOTIFY_ORDER_SAVED) {
                try {

                    alertDialog.dismiss();
                    BModel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil.now(SDUtil.TIME));

                    if ((BModel.configurationMasterHelper.SHOW_ZEBRA_GHANA
                            || BModel.configurationMasterHelper.SHOW_ZEBRA_DIAGEO
                            || BModel.configurationMasterHelper.SHOW_ZEBRA_TITAN
                            || BModel.configurationMasterHelper.SHOW_ZEBRA_UNIPAL)) {
                        showDialog(DIALOG_ORDER_SAVED_WITH_PRINT_OPTION);
                    } else {
                        showDialog(DIALOG_ORDER_SAVED);
                    }


                } catch (Exception e) {
                    Commons.printException("" + e);
                }
            } else if (msg.what == DataMembers.NOTIFY_ORDER_SAVED) {
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


                    if (BModel.configurationMasterHelper.IS_INVOICE) {
                        if (BModel.configurationMasterHelper.SHOW_ZEBRA_UNIPAL) {
                            showDialog(DIALOG_INVOICE_SAVED);
                        } else {
                            printInvoice();
                        }
                    } else {
                        BModel.showAlert(
                                getResources()
                                        .getString(
                                                R.string.order_saved_and_print_preview_created_successfully),
                                DataMembers.NOTIFY_INVOICE_SAVED);
                    }

                } catch (Exception e) {
                    Commons.printException(e);
                }
            } else if (msg.what == DataMembers.NOTIFY_ORDER_DELETED) {
                try {
                    alertDialog.dismiss();
                    BModel = (BusinessModel) getApplicationContext();

                    BModel.showAlert(
                            getResources().getString(
                                    R.string.order_deleted_sucessfully)
                                    + orderHelper.getOrderId(),
                            DataMembers.NOTIFY_ORDER_SAVED);
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
                BModel.productHelper.clearOrderTableChecked();
                Intent i = new Intent(OrderSummary.this, HomeScreenTwo.class);
                startActivity(i);
                finish();
            } else if (msg.what == DataMembers.NOTIFY_DATABASE_NOT_SAVED) {
                Toast.makeText(OrderSummary.this, "DataBase Restore failed.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    };


    private final android.content.DialogInterface.OnDismissListener discountDismissListener = new android.content.DialogInterface.OnDismissListener() {

        @Override
        public void onDismiss(DialogInterface dialog) {
            dialog.dismiss();
        }
    };


    private void printOrder() {

        Intent i;
        if (BModel.configurationMasterHelper.SHOW_ZEBRA_TITAN
                || BModel.configurationMasterHelper.SHOW_ZEBRA_UNIPAL
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
        } else if (BModel.configurationMasterHelper.SHOW_BIXOLONII) {
            i = new Intent(OrderSummary.this,
                    BixolonIIPrint.class);
            i.putExtra("IsFromOrder", true);
            startActivity(i);
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            finish();
        } else if (BModel.configurationMasterHelper.SHOW_BIXOLONI) {
            i = new Intent(OrderSummary.this,
                    BixolonIPrint.class);
            i.putExtra("IsFromOrder", true);
            startActivity(i);
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            finish();
        } else if (BModel.configurationMasterHelper.SHOW_ZEBRA) {
            i = new Intent(OrderSummary.this,
                    InvoicePrintZebraNew.class);
            i.putExtra("IsFromOrder", true);
            startActivity(i);
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            finish();
        } else if (BModel.configurationMasterHelper.SHOW_ZEBRA_ATS) {
            i = new Intent(OrderSummary.this,
                    PrintPreviewScreen.class);
            i.putExtra("IsFromOrder", true);

            startActivity(i);
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            finish();
        } else if (BModel.configurationMasterHelper.SHOW_INTERMEC_ATS) {
            i = new Intent(OrderSummary.this,
                    BtPrint4Ivy.class);
            i.putExtra("IsFromOrder", true);
            startActivity(i);
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            finish();
        } else if (BModel.configurationMasterHelper.SHOW_ZEBRA_DIAGEO) {
            i = new Intent(OrderSummary.this,
                    PrintPreviewScreenDiageo.class);
            i.putExtra("IsFromOrder", true);
            startActivity(i);
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            finish();
        } else if (BModel.configurationMasterHelper.SHOW_ZEBRA_GHANA) {
            i = new Intent(OrderSummary.this,
                    GhanaPrintPreviewActivity.class);
            i.putExtra("IsFromOrder", true);
            startActivity(i);
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        } else if (BModel.configurationMasterHelper.COMMON_PRINT_BIXOLON
                || BModel.configurationMasterHelper.COMMON_PRINT_ZEBRA || BModel.configurationMasterHelper.COMMON_PRINT_SCRYBE || BModel.configurationMasterHelper.COMMON_PRINT_LOGON) {

            if ("1".equalsIgnoreCase(BModel.retailerMasterBO.getRField4()))
                BModel.productHelper.updateDistributorDetails();

            SalesReturnHelper salesReturnHelper = SalesReturnHelper.getInstance(OrderSummary.this);

            final List<ProductMasterBO> orderListWithReplace = salesReturnHelper.updateReplaceQtyWithOutTakingOrder(mOrderedProductList);
            Vector<ProductMasterBO> orderList = new Vector<>(orderListWithReplace);

            BModel.mCommonPrintHelper.xmlRead("order", false, orderList, null);
            if (BModel.configurationMasterHelper.IS_PRINT_FILE_SAVE) {
                BModel.writeToFile(String.valueOf(BModel.mCommonPrintHelper.getInvoiceData()),
                        StandardListMasterConstants.PRINT_FILE_ORDER + BModel.invoiceNumber, "/" + DataMembers.IVYDIST_PATH);

                i = new Intent(OrderSummary.this,
                        CommonPrintPreviewActivity.class);
                i.putExtra("IsFromOrder", true);
                i.putExtra("IsUpdatePrintCount", true);
                i.putExtra("isHomeBtnEnable", true);
                i.putExtra("sendMailAndLoadClass", "PRINT_FILE_ORDER");
                startActivity(i);
                overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                finish();

            } else {
                i = new Intent(OrderSummary.this,
                        CommonPrintPreviewActivity.class);
                i.putExtra("IsFromOrder", true);
                i.putExtra("IsUpdatePrintCount", true);
                i.putExtra("isHomeBtnEnable", true);
                i.putExtra("sendMailAndLoadClass", "PRINT_FILE_ORDER");
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

    private void printInvoice() {

        if (BModel.configurationMasterHelper.SHOW_ZEBRA_UNIPAL) {
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

        } else if (BModel.configurationMasterHelper.SHOW_ZEBRA_TITAN) {

            Intent i = new Intent(OrderSummary.this,
                    PrintPreviewScreenTitan.class);
            i.putExtra("IsFromOrder", true);
            i.putExtra("entryLevelDis", entryLevelDiscount);
            startActivity(i);
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            finish();

        } else if (BModel.configurationMasterHelper.COMMON_PRINT_BIXOLON
                || BModel.configurationMasterHelper.COMMON_PRINT_ZEBRA
                || BModel.configurationMasterHelper.COMMON_PRINT_SCRYBE
                || BModel.configurationMasterHelper.COMMON_PRINT_LOGON) {

            if ("1".equalsIgnoreCase(BModel.getRetailerMasterBO().getRField4())) {
                BModel.productHelper.updateDistributorDetails();
            }

            SalesReturnHelper salesReturnHelper = SalesReturnHelper.getInstance(OrderSummary.this);

            final List<ProductMasterBO> orderListWithReplace = salesReturnHelper.updateReplaceQtyWithOutTakingOrder(mOrderedProductList);
            Vector<ProductMasterBO> orderList = new Vector<>(orderListWithReplace);
            BModel.mCommonPrintHelper.xmlRead("invoice", false, orderList, null);


            BModel.writeToFile(String.valueOf(BModel.mCommonPrintHelper.getInvoiceData()),
                    StandardListMasterConstants.PRINT_FILE_INVOICE + BModel.invoiceNumber, "/" + DataMembers.PRINT_FILE_PATH);

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
            BModel.showAlert(
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
                BModel.vanmodulehelper.downloadSubDepots();
                projectSpecificPrinterCall(printerName);
            } else {
                BModel.productHelper.clearOrderTable();
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
                if (BModel.configurationMasterHelper.SHOW_ZEBRA_UNIPAL) {
                    BModel.printHelper.setPrintCnt(0);
                    for (int i = 0; i < mSelectedPrintCount + 1; i++) {
                        zebraPrinterConnection.write(BModel.printHelper.printDatafor3inchprinterForUnipal(mOrderedProductList, isFromOrder, 1));
                        if (!isFromOrder) {
                            BModel.updatePrintCount(1);
                            BModel.printHelper.setPrintCnt(orderHelper.getPrintedCountForCurrentInvoice(this));
                        }
                    }
                } else if (BModel.configurationMasterHelper.SHOW_ZEBRA_TITAN) {
                    for (int i = 0; i < mSelectedPrintCount + 1; i++) {
                        zebraPrinterConnection.write(BModel.printHelper.printDataforTitan3inchOrderprinter(mOrderedProductList, 0));
                        if (!isFromOrder) {
                            BModel.updatePrintCount(1);
                            BModel.printHelper.setPrintCnt(orderHelper.getPrintedCountForCurrentInvoice(this));
                        }
                    }
                } else if (BModel.configurationMasterHelper.SHOW_ZEBRA_GHANA
                        || BModel.configurationMasterHelper.SHOW_ZEBRA_DIAGEO) {
                    zebraPrinterConnection.write(BModel.printHelper.printDatafor3inchPrinterDiageoNG(button_deliveryDate.getText().toString()));
                }

                alertDialog.dismiss();
                BModel.productHelper.clearOrderTable();

                BModel.showAlert(
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
            BModel.productHelper.clearOrderTableChecked();
            Intent i = new Intent(OrderSummary.this, HomeScreenTwo.class);
            startActivity(i);
            finish();
        }

    }


    private void prepareEmailData() {

        if (mOrderedProductList.size() > 0) {

            if (BModel.configurationMasterHelper.IS_ORDER_SUMMERY_EXPORT_AND_EMAIL) {
                android.support.v4.app.FragmentManager ft = getSupportFragmentManager();
                EmailDialog dialog = new EmailDialog(OrderSummary.this, BModel.getRetailerMasterBO().getEmail());
                dialog.setCancelable(false);
                dialog.show(ft, "MENU_STK_ORD");
            }
        } else {
            Toast.makeText(BModel, "No data to store", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void setEmailAddress(String value) {
        new SendMail(this, "Read", "Test", value).execute();

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

            //Configuring properties for GMAIL
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.socketFactory.port", "587");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.starttls.enable", "true");

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
                if (!TextUtils.isEmpty(BModel.getRetailerMasterBO().getEmail()))
                    message.setRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(BModel.getRetailerMasterBO().getEmail(), email));
                else
                    message.setRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(email));
                message.setSubject(subject);
                message.setText(body);
                //  mm.setContent(message,"text/html; charset=utf-8");

                BodyPart bodyPart = new MimeBodyPart();
                bodyPart.setText(body);
                //Attachment
                DataSource source;
                if (sendMailAndLoadClass.equalsIgnoreCase("CommonPrintPreviewActivityPRINT_FILE_ORDER") ||
                        sendMailAndLoadClass.equalsIgnoreCase("HomeScreenTwoPRINT_FILE_ORDER")) {
                    source = new FileDataSource(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + DataMembers.IVYDIST_PATH + "/" +
                            StandardListMasterConstants.PRINT_FILE_ORDER + orderHelper.getOrderId() + ".txt");
                    bodyPart.setDataHandler(new DataHandler(source));
                    bodyPart.setFileName("OrderDetails" + ".txt");
                }
                if (sendMailAndLoadClass.equalsIgnoreCase("CommonPrintPreviewActivityPRINT_FILE_INVOICE")) {
                    source = new FileDataSource(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + DataMembers.IVYDIST_PATH + "/" +
                            StandardListMasterConstants.PRINT_FILE_INVOICE + BModel.invoiceNumber + ".txt");
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


    //this method will be called after SendMail Async task is completed
    void loadClass() {
        Intent i;
        if (sendMailAndLoadClass.equals("CommonPrintPreviewActivityPRINT_FILE_INVOICE")) {
            i = new Intent(OrderSummary.this,
                    CommonPrintPreviewActivity.class);
            i.putExtra("IsFromOrder", true);
            i.putExtra("IsUpdatePrintCount", true);
            i.putExtra("isHomeBtnEnable", true);
            startActivity(i);
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            finish();

        } else if (sendMailAndLoadClass.equals("HomeScreenTwoPRINT_FILE_ORDER")) {
            i = new Intent(
                    OrderSummary.this,
                    HomeScreenTwo.class);
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                i.putExtra("IsMoveNextActivity", BModel.configurationMasterHelper.MOVE_NEXT_ACTIVITY);
                i.putExtra("CurrentActivityCode", mCurrentActivityCode);
            }
            startActivity(i);

        } else if (sendMailAndLoadClass.equals("CommonPrintPreviewActivityPRINT_FILE_ORDER")) {
            i = new Intent(
                    OrderSummary.this,
                    HomeScreenTwo.class);
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                i.putExtra("IsMoveNextActivity", BModel.configurationMasterHelper.MOVE_NEXT_ACTIVITY);
                i.putExtra("CurrentActivityCode", mCurrentActivityCode);
            }
            startActivity(i);

        }
    }


    public void numberPressed(View vw) {
        if (returnProductDialog != null && returnProductDialog.isShowing()) {
            returnProductDialog.numberPressed(vw);
        }
        if (collectionBeforeInvoiceDialog != null && collectionBeforeInvoiceDialog.isShowing()) {
            collectionBeforeInvoiceDialog.numberPressed(vw);
        }
        if (discountDialog != null && discountDialog.isShowing()) {
            discountDialog.numberPressed(vw);
        }
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
        if (!isEditMode) {
            BModel.productHelper.clearOrderTable();
            discountHelper.clearSchemeFreeProduct(mOrderedProductList);
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

}
