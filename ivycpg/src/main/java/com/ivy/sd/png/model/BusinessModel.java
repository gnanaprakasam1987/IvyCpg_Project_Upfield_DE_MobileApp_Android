package com.ivy.sd.png.model;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.support.multidex.MultiDex;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.ivy.core.CodeCleanUpUtil;
import com.ivy.core.IvyConstants;
import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.data.app.AppDataProviderImpl;
import com.ivy.core.data.channel.ChannelDataManagerImpl;
import com.ivy.core.data.db.AppDataManagerImpl;
import com.ivy.core.data.retailer.RetailerDataManagerImpl;
import com.ivy.core.di.component.DaggerIvyAppComponent;
import com.ivy.core.di.component.IvyAppComponent;
import com.ivy.core.di.module.IvyAppModule;
import com.ivy.cpg.primarysale.provider.DisInvoiceDetailsHelper;
import com.ivy.cpg.primarysale.provider.DistTimeStampHeaderHelper;
import com.ivy.cpg.primarysale.provider.DistributorMasterHelper;
import com.ivy.cpg.view.acknowledgement.AcknowledgementActivity;
import com.ivy.cpg.view.callanalysis.CallAnalysisActivity;
import com.ivy.cpg.view.collection.CollectionHelper;
import com.ivy.cpg.view.collection.CollectionScreen;
import com.ivy.cpg.view.digitalcontent.DigitalContentActivity;
import com.ivy.cpg.view.homescreen.HomeScreenActivity;
import com.ivy.cpg.view.homescreen.HomeScreenFragment;
import com.ivy.cpg.view.initiative.InitiativeHelper;
import com.ivy.cpg.view.login.LoginScreen;
import com.ivy.cpg.view.order.OrderHelper;
import com.ivy.cpg.view.order.OrderSummary;
import com.ivy.cpg.view.order.StockAndOrder;
import com.ivy.cpg.view.order.tax.TaxBO;
import com.ivy.cpg.view.photocapture.Gallery;
import com.ivy.cpg.view.reports.invoicereport.InvoiceReportDetail;
import com.ivy.cpg.view.salesreturn.SalesReturnSummery;
import com.ivy.cpg.view.stockcheck.StockCheckActivity;
import com.ivy.cpg.view.stockcheck.StockCheckHelper;
import com.ivy.cpg.view.supervisor.chat.BaseInterfaceAdapter;
import com.ivy.cpg.view.supervisor.mvp.SupervisorActivityHelper;
import com.ivy.cpg.view.sync.AWSConnectionHelper;
import com.ivy.cpg.view.sync.AzureConnectionHelper;
import com.ivy.cpg.view.sync.largefiledownload.DigitalContentModel;
import com.ivy.cpg.view.sync.largefiledownload.FileDownloadProvider;
import com.ivy.cpg.view.van.odameter.OdameterHelper;
import com.ivy.cpg.view.van.vanstockapply.VanLoadStockApplyHelper;
import com.ivy.lib.Utils;
import com.ivy.lib.existing.DBUtil;
import com.ivy.location.LocationUtil;
import com.ivy.sd.png.asean.view.BuildConfig;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.BankMasterBO;
import com.ivy.sd.png.bo.BranchMasterBO;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.GuidedSellingBO;
import com.ivy.sd.png.bo.IndicativeBO;
import com.ivy.sd.png.bo.InvoiceHeaderBO;
import com.ivy.sd.png.bo.LoadManagementBO;
import com.ivy.sd.png.bo.LocationBO;
import com.ivy.sd.png.bo.NewOutletAttributeBO;
import com.ivy.sd.png.bo.NonproductivereasonBO;
import com.ivy.sd.png.bo.OrderFullfillmentBO;
import com.ivy.sd.png.bo.OrderHeader;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.SchemeProductBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.bo.SupplierMasterBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.provider.BatchAllocationHelper;
import com.ivy.sd.png.provider.BeatMasterHelper;
import com.ivy.sd.png.provider.ChannelMasterHelper;
import com.ivy.sd.png.provider.CloseCallHelper;
import com.ivy.sd.png.provider.CommonPrintHelper;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.FitScoreHelper;
import com.ivy.sd.png.provider.LabelsMasterHelper;
import com.ivy.sd.png.provider.ModuleTimeStampHelper;
import com.ivy.sd.png.provider.NewOutletAttributeHelper;
import com.ivy.sd.png.provider.NewOutletHelper;
import com.ivy.sd.png.provider.OrderAndInvoiceHelper;
import com.ivy.sd.png.provider.OutletTimeStampHelper;
import com.ivy.sd.png.provider.PrintHelper;
import com.ivy.sd.png.provider.ProductHelper;
import com.ivy.sd.png.provider.ProductTaggingHelper;
import com.ivy.sd.png.provider.ProfileHelper;
import com.ivy.sd.png.provider.ReasonHelper;
import com.ivy.sd.png.provider.RemarksHelper;
import com.ivy.sd.png.provider.RetailerHelper;
import com.ivy.sd.png.provider.SubChannelMasterHelper;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.sd.png.provider.TeamLeaderMasterHelper;
import com.ivy.sd.png.provider.UserMasterHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.TimerCount;
import com.ivy.sd.png.view.BatchAllocation;
import com.ivy.sd.png.view.CircleTransform;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.sd.png.view.NewOutlet;
import com.ivy.sd.png.view.ReAllocationActivity;
import com.ivy.sd.print.CollectionPreviewScreen;
import com.ivy.sd.print.CreditNotePrintPreviewScreen;
import com.ivy.sd.print.EODStockReportPreviewScreen;
import com.ivy.sd.print.PrintPreviewScreenTitan;
import com.ivy.ui.activation.view.ActivationActivity;
import com.ivy.ui.dashboard.data.SellerDashboardDataManagerImpl;
import com.ivy.ui.photocapture.view.PhotoCaptureActivity;
import com.ivy.ui.profile.data.ProfileDataManagerImpl;
import com.ivy.ui.retailer.RetailerConstants;
import com.ivy.utils.AppUtils;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FileUtils;
import com.ivy.utils.StringUtils;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Vector;

import javax.inject.Inject;

import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.Configuration;
import co.chatsdk.firebase.FirebaseNetworkAdapter;
import co.chatsdk.firebase.file_storage.FirebaseFileStorageModule;
import co.chatsdk.firebase.push.FirebasePushModule;

import co.chatsdk.firebase.push.DefaultBroadcastReceiver;

import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.FB_API_KEY;
import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.FB_APPLICATION_ID;
import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.FB_DATABSE_URL;
import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.FB_PROJECT_ID;
import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.FB_STORAGE_BUCKET;
import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.FIREBASE_ROOT_PATH;

public class BusinessModel extends Application {

    @Inject
    AppDataProvider appDataProvider;

    private IvyAppComponent mApplicationComponent;

    // to show the time taken on call analysis

    public static final String PREFS_NAME = "PRINT";
    public static String selectedDownloadRetailerID = "";
    public static int selectedDownloadUserID = 0;

    public final int CAMERA_REQUEST_CODE = 1;
    public TimerCount timer;
    private String remarkType = "0";

    public String userNameTemp = "", passwordTemp = "";
    public RetailerMasterBO retailerMasterBO;
    public Vector<RetailerMasterBO> retailerMaster;
    public Vector<RetailerMasterBO> subDMaster;
    public ArrayList<RetailerMasterBO> visitretailerMaster;

    public HashMap<String, String> mModuleCompletionResult;

    public boolean startjourneyclicked;
    public boolean endjourneyclicked;

    public String mSelectedActivityName = new String();
    public String mSelectedActivityConfigCode = new String();


    public String regid;


    public InitiativeHelper initiativeHelper;
    public BeatMasterHelper beatMasterHealper;
    public ChannelMasterHelper channelMasterHelper;
    public SubChannelMasterHelper subChannelMasterHelper;
    public ConfigurationMasterHelper configurationMasterHelper;
    public ProductHelper productHelper;
    public UserMasterHelper userMasterHelper;

    public SynchronizationHelper synchronizationHelper;
    public VanLoadStockApplyHelper stockreportmasterhelper;
    public LabelsMasterHelper labelsMasterHelper;
    public LocationUtil locationUtil;
    public OutletTimeStampHelper outletTimeStampHelper;
    public RemarksHelper remarksHelper;
    public ReasonHelper reasonHelper;
    public BatchAllocationHelper batchAllocationHelper;
    public NewOutletHelper newOutletHelper;
    public OrderAndInvoiceHelper orderAndInvoiceHelper;
    public CloseCallHelper closecallhelper;
    public RetailerHelper mRetailerHelper;
    public DistributorMasterHelper distributorMasterHelper;
    public DisInvoiceDetailsHelper disInvoiceDetailsHelper;
    public DistTimeStampHeaderHelper distTimeStampHeaderHelper;
    public PrintHelper printHelper;
    public ProfileHelper profilehelper;
    public CommonPrintHelper mCommonPrintHelper;
    public TeamLeaderMasterHelper teamLeadermasterHelper;
    private static BusinessModel mInstance;
    public NewOutletAttributeHelper newOutletAttributeHelper;
    public ModuleTimeStampHelper moduleTimeStampHelper;
    public FitScoreHelper fitscoreHelper;
    //Glide - Circle Image Transform
    @Deprecated
    public CircleTransform circleTransform;
    /* ******* Invoice Number To Print ******* */
    public String invoiceNumber;
    public String invoiceDate;
    //

    private Vector<StandardListBO> slist;
    private List<IndicativeBO> indicativeRtrList = null;
    private OrderHeader orderHeaderBO;
    private Activity ctx;

    private ArrayList<InvoiceHeaderBO> invoiceHeader;

    //private Vector payment;

    private Vector<LocationBO> locvect;
    // private String deviateResonText = "Select";
    // Used to maintain edit mode value
    private boolean isEditOrder;
    // Used to maintain edit stockcheck value
    private boolean isEditStockCheck;
    // Maintained to display the order ID in Save and Submit Dialogue.
    private String orderid;
    private String stockCheckRemark = "";
    private String orderHeaderNote = "";
    private String rField1 = "";
    private String deliveryDate = "";
    private String rField2 = "";
    private String rField3 = "";
    private String saleReturnNote = "";
    private String saleReturnRfValue = "";
    private String assetRemark = "";
    private String note = "";
    private String orderSplitScreenTitle = null;

    private HashMap<String, ArrayList<UserMasterBO>> mUserByRetailerID = new HashMap<String, ArrayList<UserMasterBO>>();
    private boolean isDoubleEdit_temp;
    private HashMap<String, String> digitalContentURLS, digitalContentSFDCURLS;
    private Handler handler;
    private Message mMessage;
    private File folder;
    private AWSCredentials myCredentials;
    private OrderFullfillmentBO orderfullfillmentbo;
    public int photocount = 0;
    public int mSelectedSubId = -1;


    private HashMap<String, RetailerMasterBO> mRetailerBOByRetailerid;

    //

    private int uploadFileCount = -1;
    private int successCount = 0;
    private boolean isErrorOccured = false;
    private File sfFiles[] = null;
    ArrayList<String> mExportFileNames;
    String mExportFileLocation;
    public int daySpinnerPositon = 0;


    private Vector<RetailerMasterBO> nearByRetailers = new Vector<>();

    private String retailerAttributeList;

    //used to save the location when retailer is selected
    public double mSelectedRetailerLatitude;
    public double mSelectedRetailerLongitude;
    public ProductMasterBO selectedPdt;

    private ArrayList<NewOutletAttributeBO> attributeList;

    /**
     * @See {@link  com.ivy.utils.AppUtils;}
     * @since CPG131 replaced by {@link com.ivy.utils.AppUtils#latlongImageFileName}
     * Will be removed from @version CPG133 Release
     * @deprecated This has been Migrated to MVP pattern
     */
    public String latlongImageFileName;
    private ArrayList<String> orderIdList = new ArrayList<>();

    // used for ProductiveCall
    public boolean PRD_FOR_ORDER = false;
    public boolean PRD_FOR_SKT = false;
    private static final String PRODUCTVIE_CALLS = "PRODUCTIVECALL";
    private static final String PRD_ORD = "ORD";
    private static final String PRD_STK = "STK";

    private String availablilityShare = "0.0";
    private int printSequenceLevelID;
    private String dashboardUserFilterString;


    private final String mFocusBrand = "Filt11";
    private final String mFocusBrand2 = "Filt12";
    private final String mFocusBrand3 = "Filt20";
    private final String mFocusBrand4 = "Filt21";

    private ArrayList<String> orderedBrands = new ArrayList<>();
    private ArrayList<String> totalFocusBrandList = new ArrayList<>();


    private HashMap<Integer, DigitalContentModel> digitalContentLargeFileURLS;

    public BusinessModel() {

        /** Create objects for Helpers **/
        initiativeHelper = InitiativeHelper.getInstance(this);

        beatMasterHealper = BeatMasterHelper.getInstance(this);
        channelMasterHelper = ChannelMasterHelper.getInstance(this);
        subChannelMasterHelper = SubChannelMasterHelper.getInstance(this);
        configurationMasterHelper = ConfigurationMasterHelper.getInstance(this);
        productHelper = ProductHelper.getInstance(this);
        userMasterHelper = UserMasterHelper.getInstance(this);
        synchronizationHelper = SynchronizationHelper.getInstance(this);
        stockreportmasterhelper = VanLoadStockApplyHelper.getInstance(this);
        labelsMasterHelper = LabelsMasterHelper.getInstance(this);
        locationUtil = LocationUtil.getInstance(this);
        outletTimeStampHelper = OutletTimeStampHelper.getInstance(this);
        remarksHelper = RemarksHelper.getInstance();
        reasonHelper = ReasonHelper.getInstance(this);

        batchAllocationHelper = BatchAllocationHelper.getInstance(this);
        orderAndInvoiceHelper = OrderAndInvoiceHelper.getInstance(this);
        closecallhelper = CloseCallHelper.getInstance(this);
        printHelper = PrintHelper.getInstance(this);

        retailerMasterBO = new RetailerMasterBO();

        invoiceHeader = new ArrayList<>();
        setRetailerMaster(new Vector<RetailerMasterBO>());

        newOutletHelper = NewOutletHelper.getInstance(this);

        // Shelf Share Helper
        mRetailerHelper = RetailerHelper.getInstance(this);
        distributorMasterHelper = DistributorMasterHelper.getInstance(this);
        disInvoiceDetailsHelper = DisInvoiceDetailsHelper.getInstance(this);
        distTimeStampHeaderHelper = DistTimeStampHeaderHelper.getInstance(this);
        profilehelper = ProfileHelper.getInstance(this);
        mCommonPrintHelper = CommonPrintHelper.getInstance(this);
        teamLeadermasterHelper = TeamLeaderMasterHelper.getInstance(this);

        newOutletAttributeHelper = NewOutletAttributeHelper.getInstance(this);

        moduleTimeStampHelper = ModuleTimeStampHelper.getInstance(this);
        fitscoreHelper = FitScoreHelper.getInstance(this);
    }


    private void loadActivity(Activity ctxx, String act) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (ctxx.isDestroyed()) { // or call isFinishing() if min sdk version < 17
                return;
            }
        } else if (ctxx.isFinishing()) {
            return;
        }
        Intent myIntent;
        if (act.equals(DataMembers.actLoginScreen)) {
            myIntent = new Intent(ctxx, LoginScreen.class);
            ctxx.startActivityForResult(myIntent, 0);
        } else if (act.equals(DataMembers.actHomeScreen)) {
            /*if (dashHomeStatic) {
                myIntent = new Intent(ctxx, DashBoardActivity.class);
                myIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                myIntent.putExtra("screentitle", dasHomeTitle);
                ctxx.startActivityForResult(myIntent, 0);
            } else {*/
            myIntent = new Intent(ctxx, HomeScreenActivity.class);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            ctxx.startActivityForResult(myIntent, 0);
            //}
        } else if (act.equals(DataMembers.actPlanning)) {
            myIntent = new Intent(ctxx, HomeScreenActivity.class);
            myIntent.putExtra("menuCode", "MENU_VISIT");
            ctxx.startActivityForResult(myIntent, 0);
        } else if (act.equals(DataMembers.actHomeScreenTwo)) {
            myIntent = new Intent(ctxx, HomeScreenTwo.class);
            ctxx.startActivityForResult(myIntent, 0);
        } else if (act.equals(DataMembers.actNewRetailer)) {
            myIntent = new Intent(ctxx, NewOutlet.class);
            ctxx.startActivityForResult(myIntent, 0);
        } else if (act.equals(DataMembers.actDigitalContent)) {
            myIntent = new Intent(ctxx, DigitalContentActivity.class);
            ctxx.startActivityForResult(myIntent, 0);
        } else if (act.equals(DataMembers.actOrderAndStock)) {
            myIntent = new Intent(ctxx, StockAndOrder.class);
            ctxx.startActivityForResult(myIntent, 0);
        } else if (act.equals(DataMembers.actOrderSummary)) {
            myIntent = new Intent(ctxx, OrderSummary.class);
            ctxx.startActivityForResult(myIntent, 0);
        } else if (act.equals(DataMembers.actCollection)) {
            myIntent = new Intent(ctxx, CollectionScreen.class);
            ctxx.startActivityForResult(myIntent, 0);
        } else if (act.equals(DataMembers.actactivationscreen)) {
            myIntent = new Intent(ctxx, ActivationActivity.class);
            ctxx.startActivityForResult(myIntent, 0);
        } else if (act.equals(DataMembers.actclosingstock)) {
            myIntent = new Intent(ctxx, StockCheckActivity.class);
            ctxx.startActivityForResult(myIntent, 0);
        } else if (act.equals(DataMembers.actPhotocapture)) {
            myIntent = new Intent(ctxx, PhotoCaptureActivity.class);
            ctxx.startActivityForResult(myIntent, 0);
        } else if (act.equals("AcknowledgementActivity")) {
            myIntent = new Intent(ctxx, AcknowledgementActivity.class);
            ctxx.startActivityForResult(myIntent, 0);
        }
    }

    public OrderFullfillmentBO getOrderfullfillmentbo() {
        return orderfullfillmentbo;
    }

    public void setOrderfullfillmentbo(OrderFullfillmentBO orderfullfillmentbo) {
        this.orderfullfillmentbo = orderfullfillmentbo;
    }

    public Context getContext() {
        return ctx;
    }

    public void setContext(Activity ctx) {
        this.ctx = ctx;
    }

    /**
     * @return
     * @See {@link RetailerDataManagerImpl#getWeekText()}
     * @deprecated
     */
    public String getWeekText() {
        String weekText = "wk1";
        try {

            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select CurrentWeekNo from AppVariables");
            if (c != null) {
                if (c.moveToNext()) {
                    weekText = c.getString(0);
                }
                c.close();
            }
            db.closeDB();

        } catch (Exception e) {

            Commons.printException(e);
        }

        return weekText;

    }

    public Vector<StandardListBO> getWeekDay() {
        return slist;
    }

   /* public StoreWiseDiscountBO getDiscountlist() {
        return discountlist;
    }

    public void setDiscountlist(StoreWiseDiscountBO discountlist) {
        this.discountlist = discountlist;
    }*/

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getAssetRemark() {
        return assetRemark;
    }

    public void setAssetRemark(String assetRemark) {
        this.assetRemark = assetRemark;
    }

    public String getSaleReturnNote() {
        return saleReturnNote;
    }

    public void setSaleReturnNote(String saleReturnNote) {
        this.saleReturnNote = saleReturnNote;
    }

    public String getSaleReturnRfValue() {
        return saleReturnRfValue;
    }

    public void setSaleReturnRfValue(String saleReturnRfValue) {
        this.saleReturnRfValue = saleReturnRfValue;
    }

    public String getOrderHeaderNote() {
        return orderHeaderNote;
    }

    public void setOrderHeaderNote(String orderHeaderNote) {
        this.orderHeaderNote = orderHeaderNote;
    }

    public String getRField1() {
        return rField1;
    }

    public void setRField1(String rField1) {
        this.rField1 = rField1;
    }

    public String getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(String deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public String getRField2() {
        return rField2;
    }

    public void setRField2(String rField2) {
        this.rField2 = rField2;
    }

    public String getRField3() {
        return rField3;
    }

    public void setRField3(String rField3) {
        this.rField3 = rField3;
    }

    public String getRemarkType() {
        return remarkType;
    }

    public void setRemarkType(String remarkType) {
        this.remarkType = remarkType;
    }

    public String getStockCheckRemark() {
        return stockCheckRemark;
    }

    public void setStockCheckRemark(String stockCheckRemark) {
        this.stockCheckRemark = stockCheckRemark;
    }

    public boolean isEdit() {
        return isEditOrder;
    }

    public void setEdit(boolean isEdit) {
        this.isEditOrder = isEdit;
        setDoubleEdit_temp(isEdit);
    }

    public String getOrderid() {
        return orderid;
    }

    public String getAvailablilityShare() {
        return availablilityShare;
    }

    public void setAvailablilityShare(String availablilityShare) {
        this.availablilityShare = availablilityShare;
    }

    public boolean isEditStockCheck() {
        return isEditStockCheck;
    }

    public void setEditStockCheck(boolean isEditStockCheck) {
        this.isEditStockCheck = isEditStockCheck;
    }

    public int getPrintSequenceLevelID() {
        return printSequenceLevelID;
    }

    public void setPrintSequenceLevelID(int printSequenceLevelID) {
        this.printSequenceLevelID = printSequenceLevelID;
    }

    public String getDashboardUserFilterString() {
        return dashboardUserFilterString;
    }

    public void setDashboardUserFilterString(String dashboardUserFilterString) {
        this.dashboardUserFilterString = dashboardUserFilterString;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        try {

            mInstance = this;
            //Glide - Circle Image Transform
            circleTransform = CircleTransform.getInstance(this.getApplicationContext());
            // appComponent = DaggerAppComponent.builder().appModule(new AppModule(this)).build();
            // appComponent.inject(this);

            mApplicationComponent = DaggerIvyAppComponent.builder()
                    .ivyAppModule(new IvyAppModule(this))
                    .build();

            mApplicationComponent.inject(this);

            codeCleanUpUtil = CodeCleanUpUtil.getInstance(this, appDataProvider);

            initializeFirebase();
            initializeChatSdk();

        } catch (Exception ex) {
            Commons.printException(ex);
        }

    }

    private void enableDisableChatReceiver(boolean isenableReceiver){

        PackageManager pm = getPackageManager();
        ComponentName compName =
                new ComponentName(getApplicationContext(),
                        DefaultBroadcastReceiver.class);


        if (isenableReceiver)
            pm.setComponentEnabledSetting(
                    compName,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
        else
            pm.setComponentEnabledSetting(
                    compName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);

    }

    /***********************************************************************Code Refactoring Initiatives******************************************************************/

    public CodeCleanUpUtil codeCleanUpUtil;


    public IvyAppComponent getComponent() {
        return mApplicationComponent;
    }


    /*******************************************************************************************************************************************************************************/

    public void initializeChatSdk() {
        try {
            Context context = getApplicationContext();

            String rootPath = AppUtils.getSharedPreferences(context).getString(FIREBASE_ROOT_PATH, "");

            if (!rootPath.equals("")) {

                Commons.print("Check CHAT SDK INITIALIZATION");

// Create a new configuration
                Configuration.Builder builder = new Configuration.Builder(context);

                builder.firebaseRootPath(rootPath);
                builder.firebaseStorageURL(Objects.requireNonNull(FirebaseApp.getInstance()).getOptions().getStorageBucket()); // /files/new_folder_cpg/chat_img
                //builder.firebaseCloudMessagingServerKey(BuildConfig.FB_SERVER_KEY);
                builder.googleMaps(getResources().getString(R.string.google_maps_api_key));
                builder.locationMessagesEnabled(true);
                //builder.imageMessagesEnabled(false);
                builder.setInboundPushHandlingEnabled(true);
                builder.setShowLocalNotifications(true);

                builder.groupsEnabled(false);
                builder.threadDetailsEnabled(false);
                builder.publicRoomCreationEnabled(false);
                builder.setClientPushEnabled(true);
                builder.pushNotificationColor(R.attr.primarycolor);
                builder.pushNotificationImageDefaultResourceId(R.drawable.launchericon);


                ChatSDK.initialize(builder.build(), new BaseInterfaceAdapter(context), new FirebaseNetworkAdapter());

                FirebaseFileStorageModule.activate();
                FirebasePushModule.activateForFirebase();

            }

            if (!SupervisorActivityHelper.getInstance().isChatConfigAvail(this))
                enableDisableChatReceiver(false);
            else
                enableDisableChatReceiver(true);

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public FirebaseApp initializeFirebase() {

        String appId = AppUtils.getSharedPreferences(this).getString(FB_APPLICATION_ID, "");
        String apiKey = AppUtils.getSharedPreferences(this).getString(FB_API_KEY, "");
        String dbUrl = AppUtils.getSharedPreferences(this).getString(FB_DATABSE_URL, "");
        String storageBucket = AppUtils.getSharedPreferences(this).getString(FB_STORAGE_BUCKET, "");
        String proId = AppUtils.getSharedPreferences(this).getString(FB_PROJECT_ID, "");

        if (!appId.isEmpty() && FirebaseApp.getApps(this).isEmpty()) {

            Commons.print("No Firebase Instance Found");

            FirebaseOptions.Builder builder = new FirebaseOptions.Builder()
                    .setApplicationId(appId)
                    .setApiKey(apiKey)
                    .setDatabaseUrl(dbUrl)
                    .setStorageBucket(storageBucket)
                    .setProjectId(proId);

            return FirebaseApp.initializeApp(this, builder.build());

        } else {
            Commons.print("Firebase Instance Already Created");
            return null;
        }
    }

    @Override
    public void onTerminate() {

        super.onTerminate();
    }

    public void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception exc) {
        }
    }

    /**
     * This method will return the standard list code for the given listID.
     *
     * @param listCode
     * @return listId
     */
    public String getStandardListId(String listCode) {
        String listID = "";
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select ListId from StandardListMaster where ListCode='"
                            + listCode + "'");
            if (c != null) {
                if (c.moveToNext()) {
                    listID = c.getString(0);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {

            Commons.printException(e);
        }
        return listID;
    }


    /**
     * This method will return the standard list name for the given listID.
     *
     * @param listId
     * @return listName
     */

    public String getStandardListName(int listId) {
        String listName = "";
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select ListName from StandardListMaster where ListId="
                            + listId + "");
            if (c != null) {
                if (c.moveToNext()) {
                    listName = c.getString(0);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {

            Commons.printException(e);
        }
        return listName;
    }


    /**
     * This method will return the standard list name for the given listID.
     *
     * @param listCode
     * @return listName
     */
    public String getStandardListNameByCode(String listCode) {
        String listName = "";
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select ListName from StandardListMaster where ListType="
                            + QT(listCode));
            if (c != null) {
                if (c.moveToNext()) {
                    listName = c.getString(0);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {

            Commons.printException(e);
        }
        return listName;
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    /**
     * This method will return the standard list code for the given listID.
     *
     * @param listCode lc
     * @return listId id
     */
    public String getStandardListIdAndType(String listCode, String listType) {
        String listID = "";
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select ListId from StandardListMaster where ListCode='"
                            + listCode + "' AND ListType = '" + listType + "'");
            if (c != null) {
                if (c.moveToNext()) {
                    listID = c.getString(0);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return listID;
    }


    public String getMenuName(String menuCode) {
        String menuName = "";
        try {

            for (ConfigureBO configureBO : configurationMasterHelper.getConfig()) {
                if (configureBO.getConfigCode().equals(menuCode)) {
                    menuName = configureBO.getMenuName();
                }

            }
        } catch (Exception e) {
            Commons.printException(e);
        }
        return menuName;
    }


    /**
     * Download the Invoice of A particular Retailer Id and DocStatus, and stored in
     * invoiceHeader Vector.
     *
     * @param retailerId
     * @param docStatus
     */

    public void downloadInvoice(String retailerId, String docStatus) {
        try {
            configurationMasterHelper.loadInvoiceMasterDueDateAndDateConfig();
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.openDataBase();

            StringBuffer sb = new StringBuffer();

            sb.append("SELECT distinct Inv.InvoiceNo, Inv.InvoiceDate, Round(invNetamount,2) as Inv_amt,");
            sb.append(" Round(IFNULL((select sum(payment.Amount) from payment where payment.BillNumber=Inv.InvoiceNo),0)+IFNULL(Inv.paidAmount,0),2) as RcvdAmt,");
            sb.append(" Round(inv.discountedAmount- IFNULL((select sum(payment.Amount) from payment where payment.BillNumber=Inv.InvoiceNo),0),2) as os,");
            sb.append(" payment.ChequeNumber,payment.ChequeDate,Round(Inv.discountedAmount,2),sum(PD.discountvalue),inv.DocRefNo,inv.DueDays,inv.DueDate,payment.Date");
            sb.append(" FROM InvoiceMaster Inv LEFT OUTER JOIN payment ON payment.BillNumber = Inv.InvoiceNo");
            sb.append(" LEFT OUTER JOIN PaymentDiscountDetail PD ON payment.uid = PD.uid");
            sb.append(" WHERE inv.Retailerid = ");
            sb.append(QT(retailerId));
            sb.append(" AND inv.DocStatus = ");
            sb.append(QT(docStatus));
            sb.append(" GROUP BY Inv.InvoiceNo");
            sb.append(" ORDER BY Inv.InvoiceDate");

            Cursor c = db.selectSQL(sb.toString());
            if (c != null) {
                InvoiceHeaderBO invocieHeaderBO;
                invoiceHeader = new ArrayList<>();
                while (c.moveToNext()) {
                    invocieHeaderBO = new InvoiceHeaderBO();
                    invocieHeaderBO.setInvoiceNo(c.getString(0));
                    invocieHeaderBO.setInvoiceDate(c.getString(1));
                    invocieHeaderBO.setInvoiceAmount(c.getDouble(2));
                    invocieHeaderBO.setPaidAmount(c.getDouble(3));
                    invocieHeaderBO.setBalance(c.getDouble(4));
                    invocieHeaderBO.setAppliedDiscountAmount(c.getDouble(8));
                    invocieHeaderBO.setDocRefNo(c.getString(9));

                    int count = DateTimeUtils.getDateCount(invocieHeaderBO.getInvoiceDate(),
                            DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), "yyyy/MM/dd");
                    final double discountpercentage = CollectionHelper.getInstance(ctx).getDiscountSlabPercent(count + 1);

                    double remaingAmount = (invocieHeaderBO.getInvoiceAmount() - (invocieHeaderBO.getAppliedDiscountAmount() + invocieHeaderBO.getPaidAmount())) * discountpercentage / 100;
                    if (configurationMasterHelper.ROUND_OF_CONFIG_ENABLED) {
                        remaingAmount = SDUtil.convertToDouble(SDUtil.format(remaingAmount,
                                0,
                                0, configurationMasterHelper.IS_DOT_FOR_GROUP));
                    }

                    invocieHeaderBO.setRemainingDiscountAmt(remaingAmount);

                    if (configurationMasterHelper.COMPUTE_DUE_DATE) {

                        if (retailerMasterBO.getCreditDays() != 0) {

                            SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
                            Date date = format.parse(invocieHeaderBO.getInvoiceDate());
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(date);
                            calendar.add(Calendar.DAY_OF_YEAR, retailerMasterBO.getCreditDays());
                            Date dueDate = format.parse(format.format(calendar.getTime()));

                            invocieHeaderBO.setDueDate(DateTimeUtils.convertDateObjectToRequestedFormat(
                                    dueDate, configurationMasterHelper.outDateFormat));

                        }
                    } else {
                        invocieHeaderBO.setDueDate(c.getString(11));
                    }
                    if (!configurationMasterHelper.COMPUTE_DUE_DAYS)
                        invocieHeaderBO.setDueDays(c.getString(10));
                    invocieHeaderBO.setCollectionDate(c.getString(12));
                    invoiceHeader.add(invocieHeaderBO);
                }
                c.close();
            }
            downloadDebitNoteDetails(db);
            db.closeDB();
        } catch (Exception e) {

            Commons.printException(e);
        }
    }

    /*
     * This method will return total acheived value of the seller for the day.
     * OrderHeader if preseller or InvoiceMaster. Deviated retailer acheived
     * value will not be considered.
     * @See {@link AppUtils#QT}
     */

    /**
     * @param data
     * @return
     * @See {@link StringUtils#QT(String)}
     * @deprecated
     */
    public String QT(String data) {
        return "'" + data + "'";
    }

    /*
     * This method will return total acheived value of the seller for the day.
     * OrderHeader if preseller or InvoiceMaster. Deviated retailer acheived
     * value will be considered.
     */

    /**
     * Used to check order exist without invoice creation.
     *
     * @return
     */
    public boolean isOrderExistToCreateInvoice() {
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db.selectSQL("select orderid from "
                    + DataMembers.tbl_orderHeader + " where retailerid="
                    + QT(getRetailerMasterBO().getRetailerID())
                    + " and invoicestatus=0 and upload!='X'");
            if (c != null) {
                if (c.getCount() > 0) {
                    c.close();
                    db.closeDB();
                    return true;
                }
                c.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }

        return false;
    }

    /*
     * This method will return total acheived value of the retailwer for the
     * day. OrderHeader if preseller or InvoiceMaster. Deviated retailer
     * acheived value will be considered.
     */

    /**
     * This method will return whether there is any order exist in DB without
     * Invoice created.
     *
     * @return true | false
     */
    public boolean isOrderExistToCreateInvoiceAll() {
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.openDataBase();
            StringBuffer sb = new StringBuffer();
            sb.append("select orderid from " + DataMembers.tbl_orderHeader
                    + " where invoicestatus = 0 and upload!='X' and OFlag = 1 ");
            if (configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED) {
                sb.append(" and is_vansales=1");
            }
            Cursor c = db.selectSQL(sb.toString());
            if (c != null) {
                if (c.getCount() > 0) {
                    c.close();
                    db.closeDB();
                    return true;
                }
                c.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }

        return false;
    }

    public void updateIsOrderWithoutInvoice() {
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.openDataBase();
            String sql = "";

            sql = "select retailerid,invoicestatus from " + DataMembers.tbl_orderHeader;

            if (configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED) {
                sql += " where is_vansales=1 and upload != 'X'";
            }
            Cursor c = db.selectSQL(sql);
            if (c != null) {
                if (c.getCount() > 0) {
                    while (c.moveToNext()) {
                        int invoicestatus = c.getInt(1);
                        if (invoicestatus == 0) {
                            mRetailerBOByRetailerid.get(c.getString(0)).setOrderWithoutInvoice(true);
                        } else if (invoicestatus == 1) {
                            mRetailerBOByRetailerid.get(c.getString(0)).setOrderWithoutInvoice(false);
                        }
                    }
                }
                c.close();
            }
           /* sql = "select retailerid from " + DataMembers.tbl_orderHeader
                    + " where invoicestatus = 1";
            if (configurationMasterHelper.IS_SHOW_SELLER_DIALOG) {
                sql += " and is_vansales=1";
            }
           c = db.selectSQL(sql);
            if (c != null) {
                if (c.getCount() > 0) {
                    while (c.moveToNext()) {
                        mRetailerBOByRetailerid.get(c.getString(0)).setOrderWithoutInvoice(false);
                    }
                }
                c.close();
            }
*/
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }

    }


    public boolean isContractRenewalDone() {
        boolean flag = false;
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db.selectSQL("select tid from "
                    + DataMembers.tbl_retailercontractrenewal
                    + " where RetailerId="
                    + QT(getRetailerMasterBO().getRetailerID()));
            if (c != null) {
                if (c.getCount() > 0) {
                    flag = true;
                }
                c.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return flag;
    }

    public boolean isSurveyDone(String menucode) {
        boolean flag = false;
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db.selectSQL("select uid from "
                    + DataMembers.tbl_AnswerHeader + " where retailerid="
                    + QT(getRetailerMasterBO().getRetailerID())
                    + " and menucode=" + QT(menucode));
            if (c != null) {
                if (c.getCount() > 0) {
                    flag = true;
                    getRetailerMasterBO().setIsSurveyDone(true);
                }
                c.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return flag;
    }


    /**
     * @return Order Value
     * @See {@link AppDataManagerImpl#getOrderValue()}
     * @deprecated This has been Migrated to MVP pattern
     */
    public double getOrderValue() {
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db.selectSQL("select sum(ordervalue)from "
                    + DataMembers.tbl_orderHeader + " where retailerid="
                    + QT(retailerMasterBO.getRetailerID()) +
                    " AND upload='N' and invoicestatus=0");
            if (c != null) {
                if (c.moveToNext()) {
                    double i = c.getDouble(0);
                    c.close();
                    db.closeDB();
                    return i;
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }

        return 0;
    }


    //To update already Audit/Downloaded User

    public double getInvoiceAmount() {
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select sum(invNetamount) from InvoiceMaster where retailerid="
                            + QT(retailerMasterBO.getRetailerID()) + " and InvoiceDate = " + QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)));
            if (c != null) {
                if (c.moveToNext()) {
                    double i = c.getFloat(0);
                    c.close();
                    db.closeDB();
                    return i;
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }

        return 0;
    }

    public double getOutStandingInvoiceAmount() {
        double i = 0;
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.openDataBase();

            Cursor c = db.selectSQL("SELECT SUM( IFNULL(payment.amount,0))  FROM payment  WHERE  payment.retailerid = "
                    + QT(retailerMasterBO.getRetailerID()));
            if (c != null) {
                if (c.moveToNext()) {
                    i = c.getFloat(0);
                }
            }
            c = db.selectSQL("select sum(paidamount) from InvoiceMaster WHERE retailerid = "
                    + QT(retailerMasterBO.getRetailerID()));
            if (c != null) {
                if (c.moveToNext()) {
                    i += c.getFloat(0);
                }
                c.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }

        return i;
    }


    //Anand Asir V
    public double getLoyaltyPoints() {
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select sum(Points) from LoyaltyPoints where retailerid="
                            + QT(retailerMasterBO.getRetailerID()));
            if (c != null) {
                if (c.moveToNext()) {
                    double i = c.getFloat(0);
                    c.close();
                    db.closeDB();
                    return i;
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }

        return 0;
    }

    public double getLoyaltyBalancePoints() {
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select sum(BalancePoints) from LoyaltyPoints where retailerid="
                            + QT(retailerMasterBO.getRetailerID()));
            if (c != null) {
                if (c.moveToNext()) {
                    double i = c.getFloat(0);
                    c.close();
                    db.closeDB();
                    return i;
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }

        return 0;
    }

    public ArrayList<RetailerMasterBO> downloadRetailerMasterData() {
        DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
        );
        db.openDataBase();
        ArrayList<RetailerMasterBO> retailerMasterData = new ArrayList<>();
        Cursor c = db.selectSQL("SELECT DISTINCT RetailerId, RetailerCode, RetailerName from retailerMaster");
        if (c != null)
            while (c.moveToNext()) {
                RetailerMasterBO retailerMasterBO = new RetailerMasterBO();
                retailerMasterBO.setMovRetailerName(c.getString(2));
                //   retailerMasterBO.setMovRetailerCode(c.getString(1));
                retailerMasterBO.setMovRetailerId(c.getString(0));

                retailerMasterData.add(retailerMasterBO);
            }
        return retailerMasterData;
    }


    /**
     * @See {@link RetailerDataManagerImpl#fetchRetailers()}
     * @deprecated
     */
    public void downloadRetailerMaster() {
        try {
            mRetailerBOByRetailerid = new HashMap<>();
            RetailerMasterBO retailer;

            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME);
            db.openDataBase();

            configurationMasterHelper.loadRouteConfig(db);
            downloadIndicativeOrderedRetailer(db);

            Cursor c = db
                    .selectSQL("SELECT DISTINCT A.RetailerID, A.RetailerCode, A.RetailerName, RBM.BeatID as beatid, A.creditlimit, A.tinnumber, A.TinExpDate, A.channelID,"
                            + " A.classid, A.subchannelid, ifnull(A.daily_target_planned,0) as daily_target_planned, RBM.isDeviated,"
                            + " ifnull(A.sbdMerchpercent,0) as sbdMerchpercent, A.is_new,ifnull(A.initiativePercent,0) as initiativePercent,"
                            + " isOrdered, RBM.isProductive, isInvoiceCreated, isDigitalContent, isReviewPlan, RBM.isVisited,"
                            + " (select count (sbdid) from SbdMerchandisingMaster where ChannelId = A.ChannelId"
                            + " and TypeListId = (select ListId from StandardListMaster where ListCode='MERCH')) as rpstgt,"
                            + " ifnull(A.RPS_Merch_Achieved,0) as RPS_Merch_Achieved, ifnull(RC.weekNo,0) as weekNo,A.isDeadStore,A.isPlanned,"
                            + (configurationMasterHelper.IS_DIST_SELECT_BY_SUPPLIER ? " ifnull((select ListCode from StandardListMaster where ListID=SM.RpTypeId),'') as RpTypeCode," : " ifnull((select ListCode from StandardListMaster where ListID=A.RpTypeId),'') as RpTypeCode,")
                            + "A.sptgt, A.isOrderMerch,"
                            + " A.PastVisitStatus, A.isMerchandisingDone, A.isInitMerchandisingDone,"
                            + " case when RC.WalkingSeq='' then 9999 else RC.WalkingSeq end as WalkingSeq,"
                            + " A.sbd_dist_stock,A.RField1,"
                            + "(select count (sbdid) from SbdMerchandisingMaster where "
                            + "ChannelId = A.ChannelId and TypeListId=(select ListId from "
                            + "StandardListMaster where ListCode='MERCH_INIT')) as pricetgt,"
                            + "A.sbdMerchInitAcheived,A.sbdMerchInitPercent, A.initiative_achieved, "
                            + "(select  count(rowid) from InitiativeHeaderMaster B where isParent=1 and B.InitID in "
                            + "(select InitId from InitiativeDetailMaster where LocalChannelId=A.subchannelid))as init_target"
                            + " , IFNULL(A.RField2,0) as RField2, A.radius as GPS_DIST, " +
                            "StoreOTPActivated, SkipOTPActivated,RField3,A.RetCreditLimit," +
                            "TaxTypeId,RField4,locationid,LM.LocName,A.VisitDays,A.accountid,A.NfcTagId,A.contractstatuslovid,A.ProfileImagePath,"
                            + (configurationMasterHelper.IS_DIST_SELECT_BY_SUPPLIER ? "SM.sid as RetDistributorId," : +userMasterHelper.getUserMasterBO().getBranchId() + " as RetDistributorId,")
                            + (configurationMasterHelper.IS_DIST_SELECT_BY_SUPPLIER ? "SM.sid as RetDistParentId," : +userMasterHelper.getUserMasterBO().getDistributorid() + " as RetDistParentId,")
                            + "RA.address1, RA.address2, RA.address3, RA.City, RA.State, RA.pincode, RA.contactnumber, RA.email, IFNULL(RA.latitude,0) as latitude, IFNULL(RA.longitude,0) as longitude, RA.addressId"
                            + " , IFNULL(RC1.contactname,'') as pc_name, IFNULL(RC1.ContactName_LName,'') as pc_LName, RC1.ContactNumber as pc_Number,"
                            + " RC1.CPID as pc_CPID, IFNULL(RC1.DOB,'') as pc_DOB, RC1.contact_title as pc_title, RC1.contact_title_lovid as pc_title_lovid"
                            + " , IFNULL(RC2.contactname,'') as sc_name, IFNULL(RC2.ContactName_LName,'') as sc_LName, RC2.ContactNumber as sc_Number,"
                            + " RC2.CPID as sc_CPID, IFNULL(RC2.DOB,'') as sc_DOB, RC2.contact_title as sc_title, RC2.contact_title_lovid as sc_title_lovid,"

                            + "RV.PlannedVisitCount, RV.VisitDoneCount, RV.VisitFrequency, RV.lastVisitDate, RV.lastVisitedBy,"

                            + " IFNULL(RACH.monthly_acheived,0) as MonthlyAcheived, IFNULL(creditPeriod,'') as creditPeriod,RField5,RField6,RField7,RField8,RField9,RPP.ProductId as priorityBrand,SalesType,A.isSameZone, A.GSTNumber,A.InSEZ,A.DLNo,A.DLNoExpDate,IFNULL(A.SubDId,0) as SubDId,"
                            + " A.pan_number,A.food_licence_number,A.food_licence_exp_date,RA.Mobile,RA.FaxNo,RA.Region,RA.Country,RA.District,"
                            + "IFNULL((select EAM.AttributeCode from EntityAttributeMaster EAM where EAM.AttributeId = RAT.AttributeId and "
                            + "(select AttributeCode from EntityAttributeMaster where AttributeId = EAM.ParentId"
                            + " and IsSystemComputed = 1) = 'Golden_Type'),0) as AttributeCode,A.sbdDistPercent,A.retailerTaxLocId as RetailerTaxLocId,"
                            + (configurationMasterHelper.IS_DIST_SELECT_BY_SUPPLIER ? "SM.supplierTaxLocId as SupplierTaxLocId," : "0 as SupplierTaxLocId,")
                            + "ridSF FROM RetailerMaster A"

                            + " LEFT JOIN RetailerBeatMapping RBM ON RBM.RetailerID = A.RetailerID"

                            + " LEFT JOIN RetailerClientMappingMaster RC " + (configurationMasterHelper.IS_BEAT_WISE_RETAILER_MAPPING ? " on RC.beatID=RBM.beatId" : " on RC.Rid = A.RetailerId")

                            + (configurationMasterHelper.SHOW_DATE_ROUTE ? " AND RC.date = " + QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) : "")

                            + " LEFT JOIN RetailerAddress RA ON RA.RetailerId = A.RetailerID AND RA.IsPrimary=1"

                            + " LEFT JOIN RetailerContact RC1 ON RC1.RetailerId = A.RetailerID AND RC1.IsPrimary = 1"
                            + " LEFT JOIN (SELECT RetailerId,contactname,ContactName_LName,ContactNumber,CPID,DOB,contact_title,contact_title_lovid from RetailerContact WHERE IsPrimary=0 LIMIT 1) AS RC2 ON RC2.RetailerId=A.RetailerId"

                            + (configurationMasterHelper.IS_DIST_SELECT_BY_SUPPLIER ? " left join SupplierMaster SM ON SM.rid = A.RetailerID" : "")

                            + " LEFT JOIN RetailerVisit RV ON RV.RetailerID = A.RetailerID"

                            + " LEFT JOIN RetailerAchievement RACH ON RACH.RetailerID = A.RetailerID"

                            + " LEFT JOIN LocationMaster LM ON LM.LocId = A.locationid"

                            + " LEFT JOIN RetailerPriorityProducts RPP ON RPP.retailerid = A.RetailerID"

                            + " LEFT JOIN RetailerAttribute RAT ON A.RetailerID = RAT.RetailerId");

            // group by A.retailerid
            if (c != null) {
                setRetailerMaster(new Vector<RetailerMasterBO>());
                setSubDMaster(new Vector<RetailerMasterBO>());
                while (c.moveToNext()) {
                    retailer = new RetailerMasterBO();
                    String retID = c.getString(c.getColumnIndex("RetailerID"));
                    retailer.setRetailerID(retID);
                    retailer.setRetailerCode(c.getString(c.getColumnIndex("RetailerCode")));
                    retailer.setRetailerName(c.getString(c.getColumnIndex("RetailerName")));
                    retailer.setBeatID(c.getInt(c.getColumnIndex("beatid")));
                    retailer.setCreditLimit(c.getDouble(c.getColumnIndex("creditlimit")));
                    retailer.setTinnumber(c.getString(c.getColumnIndex("tinnumber")));
                    retailer.setTinExpDate(c.getString(c.getColumnIndex("TinExpDate")));
                    retailer.setChannelID(c.getInt(c.getColumnIndex("channelID")));
                    retailer.setClassid(c.getInt(c.getColumnIndex("classid")));
                    retailer.setSubchannelid(c.getInt(c.getColumnIndex("subchannelid")));
                    retailer.setDaily_target_planned(c.getDouble(c.getColumnIndex("daily_target_planned")));
                    retailer.setDaily_target_planned_temp(c.getDouble(c.getColumnIndex("daily_target_planned")));

                    retailer.setIsPlanned(c.getString(c.getColumnIndex("isPlanned")));
                    retailer.setIsDeviated(c.getString(c.getColumnIndex("isDeviated")));
                    retailer.setIsVisited(c.getString(c.getColumnIndex("isVisited")));
                    retailer.setOrdered(c.getString(c.getColumnIndex("isOrdered")));
                    retailer.setProductive(c.getString(c.getColumnIndex("isProductive")));
                    retailer.setIsNew(c.getString(c.getColumnIndex("is_new")));
                    retailer.setIsDeadStore(c.getString(c.getColumnIndex("isDeadStore")));
                    retailer.setIsGoldStore(c.getInt(c.getColumnIndex("AttributeCode"))); // To display golden store

                    // Dist and merch precent
                    retailer.setSbdMercPercent(c.getString(c.getColumnIndex("sbdMerchpercent")));

                    retailer.setInitiativePercent(c.getString(c.getColumnIndex("initiativePercent")));

                    retailer.setInvoiceDone(c.getString(c.getColumnIndex("isInvoiceCreated")));
                    retailer.setIsDigitalContent(c.getString(c.getColumnIndex("isDigitalContent")));
                    retailer.setIsReviewPlan(c.getString(c.getColumnIndex("isReviewPlan")));

                    // Dist and merch tgt & acheivement count
                    retailer.setSBDMerchTarget(c.getInt(c.getColumnIndex("rpstgt")));
                    retailer.setSBDMerchAchieved(c.getInt(c.getColumnIndex("RPS_Merch_Achieved")));

                    retailer.setWeekNo(c.getString(c.getColumnIndex("weekNo")));
                    retailer.setWalkingSequence(c.getInt(c.getColumnIndex("WalkingSeq")));

                    retailer.setRpTypeCode(c.getString(c.getColumnIndex("RpTypeCode")));
                    retailer.setSpTarget(c.getFloat(c.getColumnIndex("sptgt")));
                    retailer.setIsOrderMerch(c.getString(c.getColumnIndex("isOrderMerch")));
                    retailer.setLastVisitStatus(c.getString(c.getColumnIndex("PastVisitStatus")));
                    retailer.setIsMerchandisingDone(c.getString(c.getColumnIndex("isMerchandisingDone")));
                    retailer.setIsInitMerchandisingDone(c.getString(c.getColumnIndex("isInitMerchandisingDone")));

                    retailer.setSbdDistStock(c.getInt(c.getColumnIndex("sbd_dist_stock")));
                    retailer.setSbdMerchInitTarget(c.getInt(c.getColumnIndex("pricetgt")));
                    retailer.setSbdMerchInitAcheived(c.getInt(c.getColumnIndex("sbdMerchInitAcheived")));
                    retailer.setSbdMerchInitPrecent(c.getString(c.getColumnIndex("sbdMerchInitPercent")));
                    retailer.setInitiative_achieved(c.getInt(c.getColumnIndex("initiative_achieved")));
                    retailer.setInitiative_target(c.getInt(c.getColumnIndex("init_target")));
                    retailer.setRfield2(c.getString(c.getColumnIndex("RField2")));


                    retailer.setGpsDistance(c.getInt(c.getColumnIndex("GPS_DIST")));
                    retailer.setOtpActivatedDate(c.getString(c.getColumnIndex("StoreOTPActivated")));
                    retailer.setSkipActivatedDate(c.getString(c.getColumnIndex("SkipOTPActivated")));
                    try {
                        retailer.setBottle_creditLimit(c.getDouble(c.getColumnIndex("RetCreditLimit")));
                    } catch (Exception e) {
                        Commons.printException(e);
                        retailer.setBottle_creditLimit(0.0);
                    }

                    retailer.setProfile_creditLimit(c.getString(c.getColumnIndex("RetCreditLimit")));

                    retailer.setTaxTypeId(c.getInt(c.getColumnIndex("TaxTypeId")));
                    retailer.setLocationId(c.getInt(c.getColumnIndex("locationid")));
                    retailer.setLocName(c.getString(c.getColumnIndex("LocName")));
                    retailer.setVisitday(c.getString(c.getColumnIndex("VisitDays")));
                    retailer.setAccountid(c.getInt(c.getColumnIndex("accountid")));
                    retailer.setNFCTagId(c.getString(c.getColumnIndex("NfcTagId")));
                    retailer.setContractLovid(c.getInt(c.getColumnIndex("contractstatuslovid")));
                    retailer.setDistributorId(c.getInt(c.getColumnIndex("RetDistributorId")));
                    retailer.setDistParentId(c.getInt(c.getColumnIndex("RetDistParentId")));
                    try {
                        retailer.setCredit_balance(SDUtil.convertToDouble(c.getString(c.getColumnIndex("RField1"))));
                    } catch (Exception e) {
                        Commons.printException(e);
                        retailer.setCredit_balance(0.0);
                    }
                    retailer.setRField1(c.getString(c.getColumnIndex("RField1")));
                    retailer.setCredit_invoice_count(c.getString(c.getColumnIndex("RField3")));
                    retailer.setRField4(c.getString(c.getColumnIndex("RField4")));

                    //temp_retailer_address
                    retailer.setAddress1(c.getString(c.getColumnIndex("Address1")));
                    retailer.setAddress2(c.getString(c.getColumnIndex("Address2")));
                    retailer.setAddress3(c.getString(c.getColumnIndex("Address3")));
                    retailer.setCity(c.getString(c.getColumnIndex("City")));
                    retailer.setState(c.getString(c.getColumnIndex("State")));
                    retailer.setPincode(c.getString(c.getColumnIndex("pincode")));
                    retailer.setContactnumber(c.getString(c.getColumnIndex("ContactNumber")));
                    retailer.setEmail(c.getString(c.getColumnIndex("Email")));
                    retailer.setLatitude(c.getDouble(c.getColumnIndex("latitude")));
                    retailer.setLongitude(c.getDouble(c.getColumnIndex("longitude")));
                    retailer.setAddressid(c.getString(c.getColumnIndex("AddressId")));

                    //temp_retailerContact
                    retailer.setContactname(c.getString(c.getColumnIndex("pc_name")));
                    retailer.setContactLname(c.getString(c.getColumnIndex("pc_LName")));
                    retailer.setContactnumber1(c.getString(c.getColumnIndex("pc_Number")));
                    retailer.setCp1id(c.getString(c.getColumnIndex("pc_CPID")));
                    if (!c.getString(c.getColumnIndex("pc_DOB")).equalsIgnoreCase("null"))
                        retailer.setDob(c.getString(c.getColumnIndex("pc_DOB")));
                    else
                        retailer.setDob("");
                    retailer.setContact1_title(c.getString(c.getColumnIndex("pc_title")));
                    retailer.setContact1_titlelovid(c.getString(c.getColumnIndex("pc_title_lovid")));

                    retailer.setContactname2(c.getString(c.getColumnIndex("sc_name")));
                    retailer.setContactLname2(c.getString(c.getColumnIndex("sc_LName")));
                    retailer.setContactnumber2(c.getString(c.getColumnIndex("sc_Number")));
                    retailer.setCp2id(c.getString(c.getColumnIndex("sc_CPID")));
                    retailer.setContact2_title(c.getString(c.getColumnIndex("sc_title")));
                    retailer.setContact2_titlelovid(c.getString(c.getColumnIndex("sc_title_lovid")));

                    //temp_retailer_pricegroup
                    //retailer.setGroupId(c.getInt(c.getColumnIndex("retgroupID")));

                    //temp_retailervisit
                    retailer.setPlannedVisitCount(c.getInt(c
                            .getColumnIndex("PlannedVisitCount")));
                    retailer.setVisitDoneCount(c.getInt(c.getColumnIndex("VisitDoneCount")));
                    retailer.setVisit_frequencey(c.getInt(c.getColumnIndex("VisitFrequency")));

                    //temp_invoice_monthlyachievement
                    retailer.setMonthly_acheived(c.getDouble(c.getColumnIndex("MonthlyAcheived")));

                    retailer.setCreditDays(c.getInt(c.getColumnIndex("creditPeriod")));
                    retailer.setRField5(c.getString(c.getColumnIndex("RField5")));
                    retailer.setRField6(c.getString(c.getColumnIndex("RField6")));
                    retailer.setRField7(c.getString(c.getColumnIndex("RField7")));
                    retailer.setRField8(c.getString(c.getColumnIndex("RField8")));
                    retailer.setRField9(c.getString(c.getColumnIndex("RField9")));

                    retailer.setPrioriryProductId(c.getInt(c.getColumnIndex("priorityBrand")));
                    retailer.setSalesTypeId(c.getInt(c.getColumnIndex("SalesType")));
                    retailer.setProfileImagePath(c.getString(c.getColumnIndex("ProfileImagePath")));

                    retailer.setSameZone(c.getInt(c.getColumnIndex("isSameZone")));
                    retailer.setGSTNumber(c.getString(c.getColumnIndex("GSTNumber")));
                    retailer.setIsSEZzone(c.getInt(c.getColumnIndex("InSEZ")));
                    retailer.setDLNo(c.getString(c.getColumnIndex("DLNo")));
                    retailer.setDLNoExpDate(c.getString(c.getColumnIndex("DLNoExpDate")));
                    retailer.setSubdId(c.getInt(c.getColumnIndex("SubDId")));
                    retailer.setPanNumber(c.getString(c.getColumnIndex("pan_number")));
                    retailer.setFoodLicenceNo(c.getString(c.getColumnIndex("food_licence_number")));
                    retailer.setFoodLicenceExpDate(c.getString(c.getColumnIndex("food_licence_exp_date")));
                    retailer.setMobile(c.getString(c.getColumnIndex("Mobile")));
                    retailer.setFax(c.getString(c.getColumnIndex("FaxNo")));
                    retailer.setRegion(c.getString(c.getColumnIndex("Region")));
                    retailer.setCountry(c.getString(c.getColumnIndex("Country")));
                    retailer.setSbdPercent(c.getFloat(c.getColumnIndex("sbdDistPercent"))); // updated sbd percentage from history and ordered details
                    retailer.setRetailerTaxLocId(c.getInt(c.getColumnIndex("RetailerTaxLocId")));
                    retailer.setSupplierTaxLocId(c.getInt(c.getColumnIndex("SupplierTaxLocId")));
                    retailer.setRidSF(c.getString(c.getColumnIndex("ridSF")));
                    retailer.setDistrict(c.getString(c.getColumnIndex("District")));
                    retailer.setLastVisitDate(c.getString(c.getColumnIndex("lastVisitDate")));
                    retailer.setLastVisitedBy(c.getString(c.getColumnIndex("lastVisitedBy")));

                    retailer.setIsToday(0);
                    retailer.setHangingOrder(false);
                    retailer.setIndicateFlag(0);
                    retailer.setIsCollectionView("N");

                    //set global gps distance for retailer from the config:GPSDISTANCE
                    if (retailer.getGpsDistance() <= 0 && configurationMasterHelper.GLOBAL_GPS_DISTANCE > 0)
                        retailer.setGpsDistance(configurationMasterHelper.GLOBAL_GPS_DISTANCE);

                    updateRetailerPriceGRP(retailer, db);

                    if (configurationMasterHelper.IS_HANGINGORDER) {
                        OrderHelper.getInstance(getContext()).updateHangingOrder(getContext(), retailer, db);
                    }
                    updateIndicativeOrderedRetailer(retailer);

                    if (configurationMasterHelper.isRetailerBOMEnabled) {
                        setIsBOMAchieved(retailer, db);
                    }

                    updatePlanAndVisitCount(retailer, db);

                    getRetailerMaster().add(retailer);

                    mRetailerBOByRetailerid.put(retailer.getRetailerID(), retailer);


                }
                c.close();
            }

            if (getRetailerMaster() != null && getRetailerMaster().size() > 0)
                getMSLValues();


            if (configurationMasterHelper.SHOW_DATE_ROUTE) {
                mRetailerHelper.updatePlannedDatesInRetailerObj(db);
                mRetailerHelper.getPlannedRetailerFromDate();
            } else if (configurationMasterHelper.SHOW_DATE_PLAN_ROUTE)
                updateIsToday(db);
            else
                getPlannedRetailer();

            if (configurationMasterHelper.SHOW_MISSED_RETAILER) {
                mRetailerHelper.downloadMissedRetailer(db);
            }

            setWeeknoFoNewRetailer();

            CollectionHelper.getInstance(ctx).updateHasPaymentIssue(db);

            /********************************************/

            if (configurationMasterHelper.IS_DAY_WISE_RETAILER_WALKINGSEQ)
                mRetailerHelper.updateWalkingSequenceDayWise(db);

            updateCurrentFITscore(db);
            updateRetailersTotWgt(db);

            if (configurationMasterHelper.SUBD_RETAILER_SELECTION | configurationMasterHelper.IS_LOAD_ONLY_SUBD) {

                for (RetailerMasterBO retailerMasterBO : getRetailerMaster()) {
                    if (retailerMasterBO.getSubdId() != 0) {
                        getSubDMaster().add(retailerMasterBO);
                    }

                }

                codeCleanUpUtil.setSubDMaster(getSubDMaster());
            }

            for (RetailerMasterBO retailerMasterBO : getRetailerMaster()) {
                if ("P".equals(retailerMasterBO.getIsVisited())) {
                    appDataProvider.setPausedRetailer(retailerMasterBO);
                    break;
                }

            }

            mRetailerHelper.downloadRetailerTarget("SV", db);


            db.closeDB();

            codeCleanUpUtil.setRetailerMaster(getRetailerMaster());
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    /**
     * update retailer price group
     *
     * @param retObj
     * @param db
     */
    private void updateRetailerPriceGRP(RetailerMasterBO retObj, DBUtil db) {

        try {
            Cursor c;
            int distId = 0;
            c = db.selectSQL("select DistributorID From RetailerPriceGroup where DistributorID<>0 AND RetailerId=" + StringUtils.QT(retObj.getRetailerID()));
            if (c != null
                    && c.getCount() > 0) {
                if (c.moveToNext())
                    distId = c.getInt(0);

            }
            c.close();

            c = db.selectSQL("SELECT IFNULL(GroupId,0) From RetailerPriceGroup WHERE DistributorID=" + distId + " AND RetailerId=" + StringUtils.QT(retObj.getRetailerID()) + " LIMIT 1");
            if (c != null
                    && c.getCount() > 0) {
                if (c.moveToNext())
                    retObj.setGroupId(c.getInt(0));
            }
            c.close();
        } catch (Exception e) {
            Commons.printException("Exception ", e);
        }

    }

    /**
     * update retailer plan and visit count
     *
     * @param retObj
     */
    private void updatePlanAndVisitCount(RetailerMasterBO retObj, DBUtil db) {

        try {
            Cursor c;
            c = db.selectSQL("select PlanId From DatewisePlan where planStatus ='APPROVED'or 'PENDING' AND EntityId=" + StringUtils.QT(retObj.getRetailerID()));
            if (c != null
                    && c.getCount() > 0) {
                if (c.moveToNext())
                    retObj.setTotalPlanned(c.getCount());

                c.close();
            }


            c = db.selectSQL("SELECT PlanId From DatewisePlan WHERE VisitStatus= 'COMPLETED' AND EntityId=" + StringUtils.QT(retObj.getRetailerID()) + " LIMIT 1");
            if (c != null
                    && c.getCount() > 0) {
                if (c.moveToNext())
                    retObj.setTotalVisited(c.getCount());

                c.close();
            }
        } catch (Exception ignore) {

        }

    }

    private void updateIsToday(DBUtil db) {
        List<String> retailerIds = new ArrayList<>();
        List<String> vistedRetailerIds = new ArrayList<>();
        Cursor c = db.selectSQL("select EntityId,VisitStatus From DatewisePlan where planStatus ='APPROVED' AND (VisitStatus = 'PLANNED' or VisitStatus = 'COMPLETED')" +
                "AND Date = " + StringUtils.QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)));
        if (c != null
                && c.getCount() > 0) {
            while (c.moveToNext()) {
                retailerIds.add(c.getString(0));
                if (c.getString(1).equals("COMPLETED"))
                    vistedRetailerIds.add(c.getString(0));
            }

            c.close();
        }
        if (retailerIds.size() > 0)
            for (RetailerMasterBO retailerMasterBO : getRetailerMaster()) {
                retailerMasterBO.setIsToday(0);
                retailerMasterBO.setIsVisited("N");
                if (retailerIds.contains(retailerMasterBO.getRetailerID())) {
                    retailerMasterBO.setIsToday(1);
                    if (vistedRetailerIds.contains(retailerMasterBO.getRetailerID()))
                        retailerMasterBO.setIsVisited("Y");
                }
            }
    }

    @Deprecated
    /**
     * @See
     * {@link com.ivy.core.data.retailer.RetailerDataManagerImpl#setIsBOMAchieved(RetailerMasterBO)}
     */
    private void setIsBOMAchieved(RetailerMasterBO Retailer, DBUtil db) {
        try {
            String sql = "";
            Cursor c = null;

            sql = "select RDP.pid,CSD.Shelfpqty,CSD.Shelfcqty,CSD.shelfoqty,OD.pieceqty,OD.caseQty,OD.outerQty from RtrWiseDeadProducts RDP " +
                    "left join ClosingStockDetail CSD on CSD.ProductID = RDP.pid And CSD.retailerid = RDP.rid " +
                    "left join OrderDetail OD on OD.retailerid = RDP.rid And OD.ProductID = RDP.pid where RDP.rid = " + SDUtil.convertToInt(Retailer.getRetailerID());

            c = db.selectSQL(sql);
            if (c != null) {
                while (c.moveToNext()) {
                    Commons.print("qty " + c.getString(1) + ", " + c.getString(2) + "," + c.getString(3) + "," + c.getString(4) + "," + c.getString(5) + "," + c.getString(6));
                    if ((c.getString(1) != null && !c.getString(1).equals("0")) ||
                            (c.getString(2) != null && !c.getString(2).equals("0")) ||
                            (c.getString(3) != null && !c.getString(3).equals("0")) ||
                            (c.getString(4) != null && !c.getString(4).equals("0")) ||
                            (c.getString(5) != null && !c.getString(5).equals("0")) ||
                            (c.getString(6) != null && !c.getString(6).equals("0"))) {
                        Retailer.setBomAchieved(true);
                    } else {
                        Retailer.setBomAchieved(false);
                    }
                }
            }
            c.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean getStockAndOrderForRetailerPdts(int pdtId) {
        int siz = productHelper.getProductMaster().size();
        if (siz == 0)
            return false;
        for (int i = 0; i < siz; ++i) {
            ProductMasterBO product = productHelper
                    .getProductMaster().get(i);
            if (SDUtil.convertToInt(product.getProductID()) == pdtId) {
                for (int j = 0; j < product.getLocations().size(); j++) {
                    if ((product.getLocations().get(j).getShelfPiece() > -1 ||
                            product.getLocations().get(j).getShelfCase() > -1 ||
                            product.getLocations().get(j).getShelfOuter() > -1)
                            || product.getLocations().get(j).getAvailability() > -1) {
                        return true;
                    }
                }
                if (product.getOrderedCaseQty() > 0
                        || product.getOrderedPcsQty() > 0
                        || product.getOrderedOuterQty() > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public void isDeadGoldenAchieved() {
        int bomCount = 0;
        if (mRetailerHelper.getmRtrWiseDeadProductsList() != null && mRetailerHelper.getmRtrWiseDeadProductsList().size() > 0) {
            for (int i = 0; i < mRetailerHelper.getmRtrWiseDeadProductsList().size(); i++) {
                if (getStockAndOrderForRetailerPdts(mRetailerHelper.getmRtrWiseDeadProductsList().get(i).getPid())) {
                    bomCount++;
                }
            }
            if (bomCount == mRetailerHelper.getmRtrWiseDeadProductsList().size()) {
                getRetailerMasterBO().setBomAchieved(true);
            }
        }
    }

    /**
     * @See {@link  com.ivy.ui.profile.data.ProfileDataManagerImpl;}
     * @since CPG131 replaced by {@link com.ivy.ui.profile.data.ProfileDataManagerImpl#downloadAttributeListForRetailer(String)}
     * Will be removed from @version CPG133 Release
     * @deprecated This has been Migrated to MVP pattern
     */
    public void getAttributeListForRetailer() {

        DBUtil db = null;
        try {

            db = new DBUtil(ctx, DataMembers.DB_NAME);
            db.openDataBase();
            Cursor cursor = db.selectSQL("select RB.attributeid, RB.levelid from retailerattribute RB INNER JOIN " +
                    "EntityAttributeMaster  EAM  ON RB.attributeid=EAM.Attributeid  where RB.retailerid = " + getRetailerMasterBO().getRetailerID() +
                    " order by EAM.ParentId");
            if (cursor != null) {
                ArrayList<NewOutletAttributeBO> attributeBOArrayList = new ArrayList<>();
                NewOutletAttributeBO tempBO;
                while (cursor.moveToNext()) {
                    tempBO = new NewOutletAttributeBO();
                    tempBO.setAttrId(cursor.getInt(0));
                    tempBO.setLevelId(cursor.getInt(1));
                    attributeBOArrayList.add(tempBO);
                }

                getRetailerMasterBO().setAttributeBOArrayList(attributeBOArrayList);
                cursor.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public int getTotalFocusBrands() {
        try {

            int focusBrandCount = 0;

            int focusBrandProducts1 = 0;
            int focusBrandProducts2 = 0;
            int focusBrandProducts3 = 0;
            int focusBrandProducts4 = 0;

            Vector<ProductMasterBO> products = productHelper.getProductMaster();
            if (products != null) {
                for (int index = 0; index < products.size(); index++) {
                    if (products.get(index).getIsFocusBrand() == 1)
                        focusBrandProducts1 = 1;
                    else if (products.get(index).getIsFocusBrand2() == 1)
                        focusBrandProducts2 = 1;
                    else if (products.get(index).getIsFocusBrand3() == 1)
                        focusBrandProducts3 = 1;
                    else if (products.get(index).getIsFocusBrand4() == 1)
                        focusBrandProducts4 = 1;

                }
            }

            getTotalFocusBrandList().clear();
            if (focusBrandProducts1 == 1) {
                getTotalFocusBrandList().add(getFocusFilterName(mFocusBrand));
            }
            if (focusBrandProducts2 == 1) {
                getTotalFocusBrandList().add(getFocusFilterName(mFocusBrand2));
            }
            if (focusBrandProducts3 == 1) {
                getTotalFocusBrandList().add(getFocusFilterName(mFocusBrand3));
            }
            if (focusBrandProducts4 == 1) {
                getTotalFocusBrandList().add(getFocusFilterName(mFocusBrand4));
            }

            focusBrandCount = focusBrandProducts1 + focusBrandProducts2 + focusBrandProducts3 + focusBrandProducts4;

            return focusBrandCount;

        } catch (Exception ex) {
            Commons.printException(ex);
        }

        return 0;
    }

    public void getOrderedFocusBrandList() {

        try {

            ArrayList<String> mOrderedProductList = new ArrayList<>();

            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db.selectSQL("select distinct ProductID from "
                    + DataMembers.tbl_orderDetails + " where retailerid="
                    + QT(retailerMasterBO.getRetailerID()) + " and upload='N'");
            if (c != null) {
                while (c.moveToNext()) {
                    mOrderedProductList.add(c.getString(0));
                }
                c.close();
            }
            db.closeDB();

            int focusBrandProducts1 = 0;
            int focusBrandProducts2 = 0;
            int focusBrandProducts3 = 0;
            int focusBrandProducts4 = 0;

            for (String productID : mOrderedProductList) {

                ProductMasterBO bo = productHelper.getProductMasterBOById(productID);
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
            }

            getOrderedFocusBrands().clear();
            if (focusBrandProducts1 == 1) {
                getOrderedFocusBrands().add(getFocusFilterName(mFocusBrand));
            }
            if (focusBrandProducts2 == 1) {
                getOrderedFocusBrands().add(getFocusFilterName(mFocusBrand2));
            }
            if (focusBrandProducts3 == 1) {
                getOrderedFocusBrands().add(getFocusFilterName(mFocusBrand3));
            }
            if (focusBrandProducts4 == 1) {
                getOrderedFocusBrands().add(getFocusFilterName(mFocusBrand4));
            }
        } catch (Exception e) {
            Commons.printException(e);
        }

    }


    /**
     * @See {@link RetailerDataManagerImpl#getMSLValues(ArrayList)}
     * @deprecated
     */
    @Deprecated
    private void getMSLValues() {
        DBUtil db = null;
        try {

            db = new DBUtil(ctx, DataMembers.DB_NAME);
            db.openDataBase();
            String sql = "";
            Cursor c = null;

            sql = "Select DRD.ColumnId , Value , RowId,entityid,DRH.Columnname from DynamicReportDetail drd " +
                    "inner join DynamicReportHeader DRH on DRD.reportid=DRH.reportid  AND  DRD.columnid=DRH.columnid " +
                    "where drH.menucode = 'RETAILER_VALUE'  group by entityid,drd.columnid,rowid";

            c = db.selectSQL(sql);
            if (c != null) {
                while (c.moveToNext()) {

                    for (RetailerMasterBO retailer : getRetailerMaster()) {
                        if (retailer.getRetailerID().equals(c.getString(3))) {
                            if (c.getString(4).equalsIgnoreCase("MSL_ACH"))
                                retailer.setMslAch(c.getString(1));
                            if (c.getString(4).equalsIgnoreCase("MSL_TGT"))
                                retailer.setMslTaget(c.getString(1));
                            if (c.getString(4).equalsIgnoreCase("Sales"))
                                retailer.setSalesValue(c.getString(1));
                        }
                    }
                }
                c.close();
            }
            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public void updateSurveyScoreHistoryRetailerWise() {
        DBUtil db;
        try {

            db = new DBUtil(ctx, DataMembers.DB_NAME);
            db.openDataBase();
            String sql = "";
            Cursor c = null;

            sql = "SELECT sum(ifnull(score,0)),rm.retailerid FROM retailermaster rm "
                    + " left  join SurveyScoreHistory s on rm.retailerid =s.retailerid and  s.Date >="
                    + QT(DateTimeUtils.getFirstDayOfCurrentMonth())
                    + " group by rm.retailerid";

            c = db.selectSQL(sql);
            if (c != null) {
                while (c.moveToNext()) {

                    for (RetailerMasterBO retailer : getRetailerMaster()) {
                        if (retailer.getRetailerID().equals(c.getString(1))) {
                            retailer.setSurveyHistoryScore(c.getInt(0));
                        }
                    }
                }
                c.close();
            }

            sql = "SELECT sum(AH.achscore),rm.retailerid  FROM  retailermaster rm"
                    + " join answerheader AH on rm.retailerid = ah.retailerid "
                    + " Left Join SurveyScoreHistory SSH on SSH.SurveyId=AH.SurveyId and ah.retailerid=ssh.retailerid and AH.Date = "
                    + QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL))
                    + " group by rm.retailerid";
            c = db.selectSQL(sql);
            if (c != null) {
                while (c.moveToNext()) {
                    for (RetailerMasterBO retailer : getRetailerMaster()) {
                        if (retailer.getRetailerID().equals(c.getString(1))) {
                            retailer.setSurveyHistoryScore(c.getInt(0));
                        }
                    }
                }
                c.close();
            }


            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    /**
     * Method to use download retailer ,which mapped indicative order and not
     * order taken
     *
     * @See {@link RetailerDataManagerImpl#fetchIndicativeRetailers()}
     * @deprecated
     */
    public void downloadIndicativeOrderedRetailer(DBUtil db) {
        indicativeRtrList = new ArrayList<IndicativeBO>();
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("select distinct io.rid ,case when io.rid!=ifnull(oh.retailerid,0) then 1 else 0 end as flag ");
            sb.append("from indicativeorder io left join orderHeader oh on  io.rid=oh.retailerid");
            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    IndicativeBO indBo = new IndicativeBO();
                    indBo.setIsIndicative(c.getShort(1));
                    indBo.setRetailerID(c.getString(0));
                    indicativeRtrList.add(indBo);
                }
            }
            c.close();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public void downloadRetailerwiseMerchandiser() {
        UserMasterBO userBo;
        ArrayList<UserMasterBO> userList;
        DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
        );
        try {
            db.createDataBase();
            db.openDataBase();
            String query = "select  distinct RC.RID,RC.UserID, U.Username from retailerclientmappingmaster RC inner join  Usermaster U on RC.UserID = U.userid order by RID";
            Cursor c = db.selectSQL(query);
            if (c.getCount() > 0) {
                userList = new ArrayList<UserMasterBO>();
                String retailerID = "";
                while (c.moveToNext()) {
                    userBo = new UserMasterBO();
                    userBo.setUserid(c.getInt(1));
                    userBo.setUserName(c.getString(2));

                    if (!retailerID.equals(c.getString(0))) {
                        if (retailerID != "") {
                            mUserByRetailerID.put(retailerID,
                                    userList);
                            userList = new ArrayList<UserMasterBO>();
                            userList.add(userBo);
                            retailerID = c.getString(0);

                        } else {
                            userList.add(userBo);
                            retailerID = c.getString(0);

                        }
                    } else {
                        userList.add(userBo);
                    }

                }
                if (userList.size() > 0) {
                    mUserByRetailerID.put(retailerID, userList);
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("TL Exception ", e);
            db.closeDB();
        }
    }

    public HashMap<String, ArrayList<UserMasterBO>> getUserByRetailerID() {
        return mUserByRetailerID;
    }

    public boolean isAlreadyDownloadUser() {
        selectedDownloadRetailerID = "";
        selectedDownloadUserID = 0;
        DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
        );
        try {
            db.createDataBase();
            db.openDataBase();
            String query = "select RID,UserId from RetailerClientMappingMaster where isAudit=1";
            Cursor c = db.selectSQL(query);

            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    selectedDownloadRetailerID = c.getString(0);
                    selectedDownloadUserID = c.getInt(1);
                    c.close();
                    db.closeDB();
                    return true;
                }
            }
            c.close();
            db.closeDB();
            return false;
        } catch (Exception e) {
            db.closeDB();
            Commons.printException("TL Exception ", e);
            return false;
        }

    }

    public void updateUserAudit(int Auditvalue) {
        DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
        );
        try {
            db.createDataBase();
            db.openDataBase();
            db.executeQ("update RetailerClientMappingMaster set isAudit=0");
            if (Auditvalue != 0) {
                String query = "update RetailerClientMappingMaster set isAudit="
                        + Auditvalue
                        + " where RID="
                        + QT(getRetailerMasterBO().getRetailerID())
                        + " and userid="
                        + getRetailerMasterBO()
                        .getSelectedUserID();
                db.executeQ(query);
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("TL Exception ", e);
            db.closeDB();
        }
    }

    /**
     * @param --retailerList
     * @param --retailerid   - mapped indicative ordered retailer
     * @param --flag
     * @See {@link com.ivy.core.data.retailer.RetailerDataManagerImpl#updateIndicativeOrderedRetailer(RetailerMasterBO, ArrayList)}
     * update indicative orderflag is 1 in retailer master objet
     * @deprecated
     */
    public void updateIndicativeOrderedRetailer(RetailerMasterBO retObj) {
        retObj.setIndicateFlag(0);
        for (IndicativeBO indBo : indicativeRtrList) {
            if (indBo.getRetailerID().equals(retObj.getRetailerID())) {
                retObj.setIndicateFlag(indBo.getIsIndicative());
                break;
            }
        }
    }

    /**
     * Update isToday and is_vansales.
     * IS_DEFAULT_PRESALE - true than update is_vansales = 0 based on
     * ORDB08 config RField Value is 1
     * IS_DEFAULT_PRESALE - fales than update is_vansales = 1
     */
    public void updateIsTodayAndIsVanSalesInRetailerMasterInfo() {
        DBUtil db = null;
        try {

            db = new DBUtil(ctx, DataMembers.DB_NAME);
            db.openDataBase();

            String query = "";
            String mRetailerIds = "";

            query = "insert into RetailerMasterInfo (RetailerId)  select retailerid from RetailerMaster ";

            db.executeQ(query);

            for (RetailerMasterBO retailerMasterBO : retailerMaster) {

                if (retailerMasterBO.getIsToday() == 1) {

                    if (!mRetailerIds.equals("")) {
                        mRetailerIds = mRetailerIds + "," + retailerMasterBO.getRetailerID();
                    } else {
                        mRetailerIds = retailerMasterBO.getRetailerID();
                    }
                }
            }

            query = ("update RetailerMasterInfo set isToday=1 where RetailerId IN (" + mRetailerIds + ")");
            db.updateSQL(query);

            if (configurationMasterHelper.IS_DEFAULT_PRESALE) {
                query = ("update RetailerMasterInfo set is_vansales=0");
                db.updateSQL(query);
            } else {
                query = ("update RetailerMasterInfo set is_vansales=1");
                db.updateSQL(query);
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    /**
     * Check the retailer is planned for today by route plan(WeekNo) Update the
     * isToday flag as 1 if planned, Bydefault this flag is 0
     *
     * @See {@link RetailerDataManagerImpl#getPlannedRetailers(ArrayList)}
     * @deprecated
     */
    @Deprecated
    private void getPlannedRetailer() {

        try {

            DateFormat sdf;
            Date now = new Date();
            String downloadDate = userMasterHelper.getUserMasterBO()
                    .getDownloadDate();

            sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss 'EET' yyyy", Locale.ENGLISH);

            String mCurrentDay = sdf.format(now.parse(downloadDate))
                    .substring(0, 3).toUpperCase(Locale.ENGLISH);

            String mCurrentWeek = getWeekText();


            String mRetailerPlan;

            int start, end;
            String userLeaveSession = "";
            userLeaveSession = getUserSession();


            //Added by Rajkumar
            ArrayList<RetailerMasterBO> retailerWIthSequence = new ArrayList<RetailerMasterBO>();
            ArrayList<RetailerMasterBO> retailerWithoutSequence = new ArrayList<RetailerMasterBO>();
            for (RetailerMasterBO bo : retailerMaster) {
                if (bo.getWalkingSequence() != 0) {
                    retailerWIthSequence.add(bo);
                } else {
                    retailerWithoutSequence.add(bo);
                }
            }
            Collections.sort(retailerWIthSequence, RetailerMasterBO.WalkingSequenceComparator);
            Collections.sort(retailerWithoutSequence, RetailerMasterBO.RetailerNameComparator);
            getRetailerMaster().clear();
            getRetailerMaster().addAll(retailerWIthSequence);
            getRetailerMaster().addAll(retailerWithoutSequence);
            int size = 0;

            for (RetailerMasterBO retailer : getRetailerMaster()) {
                mRetailerPlan = retailer.getWeekNo();
                if (mRetailerPlan != null)
                    if (mRetailerPlan.contains(mCurrentDay)) {
                        start = mRetailerPlan.indexOf(mCurrentDay);
                        end = mRetailerPlan.indexOf(";", start);
                        if (mRetailerPlan.substring(start, end).contains(
                                mCurrentWeek)) {

                            retailer.setIsToday(1);
                            size += 1;


                        }

                    }
            }


            //setting isToday based on the leave session
            if ((userLeaveSession.equalsIgnoreCase("AN") || userLeaveSession.equalsIgnoreCase("FN") || userLeaveSession.equalsIgnoreCase("FD")) && size > 0) {
                int tempSize = 0;
                if (size % 2 == 0) {
                    tempSize = (size / 2);
                } else {
                    tempSize = ((size + 1) / 2);
                }

                int tempCount = 0;
                for (RetailerMasterBO retailer : getRetailerMaster()) {
                    if (retailer.getIsToday() == 1) {
                        tempCount += 1;
                        retailer.setIsToday(0);
                        if (userLeaveSession.equals("FN")) {
                            if (tempCount > (size - tempSize)) {
                                retailer.setIsToday(1);
                            }

                        } else if (userLeaveSession.equals("AN")) {
                            if (tempCount <= (size - tempSize)) {
                                retailer.setIsToday(1);
                            }
                        }

                    }
                }
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }


    @Override
    public void unregisterOnProvideAssistDataListener(OnProvideAssistDataListener callback) {
        super.unregisterOnProvideAssistDataListener(callback);
    }

    /**
     * This method is used to check weather the retailer trying to deviate is
     * already exist in current route or not.
     *
     * @param retailerId
     * @return true is already exist or false
     */
    public boolean isAlreadyExistInToday(String retailerId) {

        boolean bool = false;
        RetailerMasterBO retailer;
        int siz = 0;

        if (retailerMaster != null)
            siz = retailerMaster.size();

        if (siz == 0)
            return bool;
        for (int i = 0; i < siz; ++i) {
            retailer = retailerMaster.get(i);

            if (retailer.getRetailerID().equals(retailerId)
                    && (retailer.getIsToday() == 1 || (retailer.getIsDeviated() != null && retailer.getIsDeviated()
                    .equals("Y")))) {
                bool = true;
            }
        }
        return bool;
    }

    public ProductMasterBO getProductbyId(String productid) {
        ProductMasterBO product;
        int siz = productHelper.getProductMaster().size();
        if (siz == 0)
            return null;

        for (int i = 0; i < siz; ++i) {
            product = productHelper.getProductMaster().get(i);
            if (product.getProductID().equals(productid)) {
                return product;
            }
        }
        return null;
    }

    /**
     * Check weather product Master has any order.
     *
     * @return
     */
    public boolean hasOrder() {

        int siz = productHelper.getProductMaster().size();
        if (siz == 0)
            return false;
        for (int i = 0; i < siz; ++i) {
            ProductMasterBO product = productHelper
                    .getProductMaster().get(i);
            if (product.getOrderedCaseQty() > 0
                    || product.getOrderedPcsQty() > 0
                    || product.getOrderedOuterQty() > 0)
                return true;
        }
        return false;
    }

    public boolean hasCombinedStkChecked() {
        ProductTaggingHelper productTaggingHelper = ProductTaggingHelper.getInstance(getContext());
        int cSize = productTaggingHelper.getTaggedProducts().size();
        if (cSize == 0)
            return false;
        for (int j = 0; j < cSize; j++) {
            ProductMasterBO product = productTaggingHelper
                    .getTaggedProducts().get(j);

            if (product.getIsDistributed() == 1 || product.getIsListed() == 1
                    || product.getPriceChanged() == 1
                    || SDUtil.convertToInt(product.getMrp_pc()) > 0
                    || SDUtil.convertToInt(product.getMrp_ca()) > 0
                    || SDUtil.convertToInt(product.getMrp_ou()) > 0) {
                return true;
            }
            int cSize2 = product.getLocations().size();
            for (int f = 0; f < cSize2; f++) {
                if (product.getLocations().get(f).getFacingQty() > 0
                        || product.getLocations().get(f).getAvailability() != -1
                        || product.getLocations().get(f).getReasonId() != 0
                        || product.getLocations().get(f).getShelfPiece() != -1
                        || product.getLocations().get(f).getShelfCase() != -1
                        || product.getLocations().get(f).getShelfOuter() != -1) {
                    return true;
                }


                if (configurationMasterHelper.SHOW_NEAREXPIRY_IN_STOCKCHECK) {
                    int nearSize = product.getLocations().get(f).getNearexpiryDate().size();
                    for (int x = 0; x < nearSize; x++) {
                        if (!product.getLocations().get(f).getNearexpiryDate().get(x)
                                .getNearexpPC().equals("0")
                                || !product.getLocations().get(f).getNearexpiryDate().get(x)
                                .getNearexpCA().equals("0")
                                || !product.getLocations().get(f).getNearexpiryDate().get(x)
                                .getNearexpOU().equals("0")) {

                            return true;
                        }
                    }
                }
            }


        }

        return false;
    }


    public void resetSRPvalues() {
        try {
            for (ProductMasterBO productMasterBO : productHelper.getProductMaster()) {
                if (productMasterBO.getOriginalSrp() > 0) {
                    productMasterBO.setSrp(productMasterBO.getOriginalSrp());
                }
            }
        } catch (Exception ex) {
            Commons.printException(ex);
        }
    }

    public ArrayList<InvoiceHeaderBO> getInvoiceHeaderBO() {
        return invoiceHeader;
    }

    // // ****************** Daily Report


    public Vector<NonproductivereasonBO> getMissedCallRetailers() {

        Vector<NonproductivereasonBO> nonProductiveVector = new Vector<NonproductivereasonBO>();
        try {
            NonproductivereasonBO ret;
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.openDataBase();

            StringBuffer sb = new StringBuffer();
            sb.append(" SELECT RM.RetailerID as rid,RetailerName,beatid,");
            sb.append(configurationMasterHelper.IS_DIST_SELECT_BY_SUPPLIER ? "SM.sid as DistributorId" : +userMasterHelper.getUserMasterBO().getDistributorid() + " as DistributorId");
            sb.append(" FROM RetailerMaster RM INNER JOIN RetailerMasterInfo RMI on RM.RetailerID = RMI.RetailerID ");
            sb.append(" where RM.retailerid NOT IN (select oh.retailerid from orderheader oh where oh.upload!='X') and RM.retailerid not in ");
            sb.append("(select np.retailerid from nonproductivereasonmaster np) and RMI.isToday=1");

            Cursor c = db.selectSQL(sb.toString());
            if (c != null) {
                while (c.moveToNext()) {

                    ret = new NonproductivereasonBO();
                    ret.setRetailerid(c.getString(c
                            .getColumnIndex("rid")));
                    ret.setRetailerName(c.getString(c
                            .getColumnIndex("RetailerName")));
                    ret.setBeatId(c.getInt(c.getColumnIndex("beatid")));
                    ret.setDistributorID(c.getInt(c.getColumnIndex("DistributorId")));
                    ret.setReasonid("0");
                    nonProductiveVector.add(ret);

                }

                c.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return nonProductiveVector;

    }

    public boolean saveNonProductiveRetailers(
            Vector<NonproductivereasonBO> retailers) {

        // uid = distid+uid+hh:mm
        boolean bool = false;
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            String values;
            db.createDataBase();
            db.openDataBase();

            String id;

            String columns = "UID,RetailerID,RouteID,Date,ReasonID,ReasonTypes,upload,DistributorID,ridSF";

            for (int i = 0; i < retailers.size(); i++) {
                id = StringUtils.QT(getAppDataProvider().getUser().getDistributorid()
                        + "" + getAppDataProvider().getUser().getUserid()
                        + "" + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID) + i);

                NonproductivereasonBO outlet = retailers.get(i);

                if (!outlet.getReasonid().equals("0")) {
                    bool = true;
                    String ridSF = "";
                    if (getAppDataProvider().getRetailMaster() != null && getAppDataProvider().getRetailMaster().getRidSF() != null)
                        ridSF = getAppDataProvider().getRetailMaster().getRidSF();
                    values = id + "," + StringUtils.QT(outlet.getRetailerid()) + ","
                            + outlet.getBeatId() + "," + StringUtils.QT(outlet.getDate())
                            + "," + StringUtils.QT(outlet.getReasonid()) + ","
                            + StringUtils.QT(getStandardListId(outlet.getReasontype()))
                            + "," + StringUtils.QT("N") + "," + outlet.getDistributorID()
                            + "," + StringUtils.QT(ridSF);

                    db.insertSQL("Nonproductivereasonmaster", columns, values);
                }
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }

        return bool;

    }

    /**
     * This method will called to store both non-productive as well as Non-Visit
     * reason.
     */
    public void saveNonproductivereason(NonproductivereasonBO outlet, String remarks) {
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            String values;
            db.createDataBase();
            db.openDataBase();

            // uid = distid+uid+hh:mm

            String id = QT(userMasterHelper.getUserMasterBO()
                    .getDistributorid()
                    + ""
                    + userMasterHelper.getUserMasterBO().getUserid()
                    + ""
                    + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID_MILLIS));

            db.deleteSQL(
                    "Nonproductivereasonmaster",
                    "RetailerID=" + StringUtils.QT(getAppDataProvider().getRetailMaster().getRetailerID())
                            + " and ReasonTypes="
                            + StringUtils.QT(getStandardListId(outlet.getReasontype()))
                            + " and RouteID="
                            + getAppDataProvider().getRetailMaster().getBeatID(), false);
            db.deleteSQL(
                    "Nonproductivereasonmaster",
                    "RetailerID="
                            + StringUtils.QT(getAppDataProvider().getRetailMaster().getRetailerID())
                            + " and ReasonTypes="
                            + StringUtils.QT(getStandardListId(outlet
                            .getCollectionReasonType()))
                            + " and RouteID="
                            + getAppDataProvider().getRetailMaster().getBeatID(), false);

            String columns = "UID,RetailerID,RouteID,Date,ReasonID,ReasonTypes,upload,distributorID,imagepath,remarks,ridSF";

            values = id + "," + StringUtils.QT(getAppDataProvider().getRetailMaster().getRetailerID()) + ","
                    + getAppDataProvider().getRetailMaster().getBeatID() + ","
                    + StringUtils.QT(outlet.getDate()) + "," + StringUtils.QT(outlet.getReasonid())
                    + "," + StringUtils.QT(getStandardListId(outlet.getReasontype())) + ","
                    + StringUtils.QT("N") + "," + getAppDataProvider().getRetailMaster().getDistributorId() + "," + StringUtils.QT(outlet.getImagePath()) + "," + StringUtils.QT(remarks)
                    + "," + StringUtils.QT(getAppDataProvider().getRetailMaster().getRidSF());

            db.insertSQL("Nonproductivereasonmaster", columns, values);
            if (outlet.getCollectionReasonID() != null && !outlet.getCollectionReasonID().equals("0")) {
                String uid = StringUtils.QT(getAppDataProvider().getUser()
                        .getDistributorid()
                        + ""
                        + getAppDataProvider().getUser().getUserid()
                        + ""
                        + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID_MILLIS));
                values = uid
                        + ","
                        + StringUtils.QT(getAppDataProvider().getRetailMaster().getRetailerID())
                        + ","
                        + getAppDataProvider().getRetailMaster().getBeatID()
                        + ","
                        + StringUtils.QT(outlet.getDate())
                        + ","
                        + StringUtils.QT(outlet.getCollectionReasonID())
                        + ","
                        + StringUtils.QT(getStandardListId(outlet.getCollectionReasonType()))
                        + "," + StringUtils.QT("N") + "," + getAppDataProvider().getRetailMaster().getDistributorId() + "," + StringUtils.QT(outlet.getImagePath()) + "," + StringUtils.QT(remarks)
                        + "," + StringUtils.QT(getAppDataProvider().getRetailMaster().getRidSF());
                db.insertSQL("Nonproductivereasonmaster", columns, values);
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }


    public void saveCancelVistreason(String reasonId, String date) {
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            String query = "Update DatewisePlan set cancelReasonId=" + StringUtils.QT(reasonId) + ",VisitStatus = 'CANCELLED' , Status = 'D' "
                    + " where EntityId=" + StringUtils.QT(getAppDataProvider().getRetailMaster().getRetailerID()) + " and Date=" + StringUtils.QT(date);

            db.updateSQL(query);

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }


    /**
     * @See {@link  com.ivy.ui.profile.edit.presenter.ProfileEditPresenterImp;}
     * @since CPG131 replaced by {@link com.ivy.ui.profile.edit.presenter.ProfileEditPresenterImp#getRetailerAttribute}
     * Will be removed from @version CPG133 Release
     * @deprecated This has been Migrated to MVP pattern
     */
    public ArrayList<NewOutletAttributeBO> getRetailerAttribute() {
        return attributeList;
    }

    /**
     * @See {@link  com.ivy.ui.profile.edit.presenter.ProfileEditPresenterImp;}
     * @since CPG131 replaced by {@link com.ivy.ui.profile.edit.presenter.ProfileEditPresenterImp#setRetailerAttribute }
     * Will be removed from @version CPG133 Release
     * @deprecated This has been Migrated to MVP pattern
     */
    public void setRetailerAttribute(ArrayList<NewOutletAttributeBO> list) {
        this.attributeList = list;
    }


    /**
     * @See {@link  com.ivy.ui.profile.edit.presenter.ProfileEditPresenterImp;}
     * @since CPG131 replaced by {@link com.ivy.ui.profile.edit.presenter.ProfileEditPresenterImp#getNearByRetailers}
     * Will be removed from @version CPG133 Release
     * @deprecated This has been Migrated to MVP pattern
     */
    public Vector<RetailerMasterBO> getNearByRetailers() {
        return nearByRetailers;
    }

    /**
     * @See {@link  com.ivy.ui.profile.edit.presenter.ProfileEditPresenterImp;}
     * @since CPG131 replaced by {@link com.ivy.ui.profile.edit.presenter.ProfileEditPresenterImp#setNearByRetailers}
     * Will be removed from @version CPG133 Release
     * @deprecated This has been Migrated to MVP pattern
     */
    public void setNearByRetailers(Vector<RetailerMasterBO> nearByRetailers) {
        this.nearByRetailers = nearByRetailers;
    }

    /**
     * @See {@link  com.ivy.ui.profile.data.ProfileDataManagerImpl;}
     * @since CPG131 replaced by {@link ProfileDataManagerImpl#saveNearByRetailers}
     * Will be removed from @version CPG133 Release
     * @deprecated This has been Migrated to MVP pattern
     */
    public void saveNearByRetailers(String id) {
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            String columnsNew = "rid,nearbyrid,upload";
            String values;
            for (int j = 0; j < getNearByRetailers().size(); j++) {
                values = QT(id) + "," + getNearByRetailers().get(j).getRetailerID() + "," + QT("N");
                db.insertSQL("NearByRetailers", columnsNew, values);
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }


    //Applying currency value config or normal format(2)
    public String formatBasedOnCurrency(double value) {
        String formattedValue = "0";
        try {
            if (configurationMasterHelper.IS_FORMAT_USING_CURRENCY_VALUE) {
                if (configurationMasterHelper.IS_APPLY_CURRENCY_CONFIG) {
                    // getting currency config value for decimal value..
                    String tempVal;
                    String fractionalStr;

                    /* tempVal = formatValue(value) + "";*/
                    tempVal = SDUtil.format(value, configurationMasterHelper.VALUE_PRECISION_COUNT, 0);
                    fractionalStr = tempVal.substring(tempVal.indexOf('.') + 1);
                    fractionalStr = (fractionalStr.length() > 2 ? fractionalStr.substring(0, 2) : fractionalStr);

                    int integerValue = (int) SDUtil.convertToDouble(tempVal);
                    int fractionValue = SDUtil.convertToInt(fractionalStr);

                    formattedValue = (integerValue + getCurrencyActualValue(fractionValue) + "");


                } else {
                    formattedValue = SDUtil.format(value, 0, 0);

                }
            } else {
                formattedValue = String.valueOf(SDUtil.formatAsPerCalculationConfig(value));

            }
        } catch (Exception ex) {
            Commons.printException(ex);
        }
        return SDUtil.getWithoutExponential(formattedValue);
    }


    public ArrayList<String> getOrderIDList() {
        return orderIdList;
    }

    public boolean isOrderTaken() {
        boolean flag = false;
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db.selectSQL("select OrderID from "
                    + DataMembers.tbl_orderHeader + " where upload !='X' and retailerid="
                    + QT(getRetailerMasterBO().getRetailerID()));
            if (c != null) {
                if (c.getCount() > 0) {
                    flag = true;
                }
                c.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return flag;
    }

    /**
     * Check whether stock check done for the particular retailer or not.
     * <p>
     * For stockcheck we don't need to consider the upload flag
     *
     * @param retailerId
     * @return true|false
     */
    public boolean hasAlreadyStockChecked(String retailerId) {
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            String sql = "select StockID from "
                    + DataMembers.tbl_closingstockheader + " where RetailerID="
                    + QT(retailerId) + " and DistributorID=" + getRetailerMasterBO().getDistributorId();
            sql += " AND date = " + QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL));
            sql += " and upload= 'N'";
            Cursor orderHeaderCursor = db.selectSQL(sql);
            if (orderHeaderCursor.getCount() > 0) {
                orderHeaderCursor.close();
                db.closeDB();
                return true;
            } else {
                orderHeaderCursor.close();
                db.closeDB();
                return false;
            }
        } catch (Exception e) {

            Commons.printException("hasAlreadyStockChecked", e);
            return false;
        }
    }

    public String getDeliveryDate(String orderID, String retailerID) {
        DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME);
        db.createDataBase();
        db.openDataBase();
        String date = "";
        // Order Header
        String sql = null;

        sql = "select deliveryDate from " + DataMembers.tbl_orderHeader
                + " where upload !='X' and RetailerID=" + QT(retailerID) + " and OrderID = " + QT(orderID);

        Cursor orderHeaderCursor = db.selectSQL(sql);
        if (orderHeaderCursor != null) {
            if (orderHeaderCursor.moveToNext()) {
                date = orderHeaderCursor.getString(0);

            }
        }
        orderHeaderCursor.close();
        db.closeDB();
        return date;
    }


    public void loadLastVisitStockCheckedProducts(String retailerId, String menucode) {
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            String sql1 = "select productId,shelfpqty,shelfcqty,whpqty,whcqty,whoqty,shelfoqty,LocId,isDistributed,isListed,reasonID,IsOwn,facing,hasPriceTag from "
                    + DataMembers.tbl_LastVisitStock
                    + " where retailerid=" + retailerId;

            Cursor orderDetailCursor = db.selectSQL(sql1);
            if (orderDetailCursor != null) {
                while (orderDetailCursor.moveToNext()) {
                    String productId = orderDetailCursor.getString(0);
                    int shelfpqty = orderDetailCursor.getInt(1);
                    int shelfcqty = orderDetailCursor.getInt(2);
                    int whpqty = orderDetailCursor.getInt(3);
                    int whcqty = orderDetailCursor.getInt(4);
                    int whoqty = orderDetailCursor.getInt(5);
                    int shelfoqty = orderDetailCursor.getInt(6);
                    int locationId = orderDetailCursor.getInt(7);
                    int isDistributed = orderDetailCursor.getInt(8);
                    int isListed = orderDetailCursor.getInt(9);
                    int reasonID = orderDetailCursor.getInt(10);
                    int isOwn = orderDetailCursor.getInt(11);
                    int facing = orderDetailCursor.getInt(12);
                    int priceTag = orderDetailCursor.getInt(13);
                    int pouring = 0;
                    int cocktail = 0;

                    int availability = 0;
                    if (shelfpqty > 0 || shelfcqty > 0 || shelfoqty > 0)
                        availability = 1;

                    setStockCheckQtyDetails(productId, shelfpqty, shelfcqty,
                            whpqty, whcqty, whoqty, shelfoqty, locationId,
                            isDistributed, isListed, reasonID, 0, isOwn, facing, pouring, cocktail, menucode, availability, priceTag);

                }
                orderDetailCursor.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public boolean loadLastVisitHistoryStockCheckedProducts(String retailerId) {
        boolean isDataVailable = false;
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            String sql1 = "select productid, qty, StoreLocId from " + DataMembers.tbl_LastVisitStock_History + " where retailerid=" + QT(retailerId) + "";
            Cursor orderDetailCursor = db.selectSQL(sql1);
            if (orderDetailCursor != null) {
                while (orderDetailCursor.moveToNext()) {
                    String productId = orderDetailCursor.getString(0);
                    int shelfpqty = orderDetailCursor.getInt(1);
                    int locationid = orderDetailCursor.getInt(2);
                    int pouring = 0;
                    int cocktail = 0;
                    int availability = 0;
                    if (shelfpqty > 0)
                        availability = 1;

                    setStockCheckQtyHistoryDetails(productId, shelfpqty, 0,
                            0, 0, 0, 0, locationid,
                            0, 0, 0, 0, 1, 0, pouring, cocktail, "MENU_STOCK", availability);
                    isDataVailable = true;

                }
                orderDetailCursor.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return isDataVailable;
    }

    /**
     * Load the ClosingStock Details and ClosingStock Header datas into product
     * master to Edit Order.
     */
    public void loadStockCheckedProducts(String retailerId, String menuCode) {
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            String stockID = new String();
            // Order Header

            StringBuilder sb = new StringBuilder();
            sb.append("select StockID,ifnull(remark,'') from ");
            sb.append(DataMembers.tbl_closingstockheader + " where RetailerID=");
            sb.append(QT(retailerId));
            sb.append(" AND DistributorID=" + getRetailerMasterBO().getDistributorId());
            sb.append(" AND date = " + QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)));
            sb.append(" and upload= 'N'");


            Cursor stockCheckedHeaderCursor = db.selectSQL(sb.toString());
            if (stockCheckedHeaderCursor != null) {
                if (stockCheckedHeaderCursor.moveToNext()) {
                    stockID = stockCheckedHeaderCursor.getString(0);
                    setStockCheckRemark(stockCheckedHeaderCursor.getString(1));
                } else {
                    setStockCheckRemark("");
                }
            } else {
                setStockCheckRemark("");
            }
            stockCheckedHeaderCursor.close();
            // if (remarksHelper.getRemarksBO().getModuleCode()
            // .equals(StandardListMasterConstants.MENU_STOCK))
            // remarksHelper.getRemarksBO().setTid(stockID);
            String sql1 = "select productId,shelfpqty,shelfcqty,whpqty,whcqty,whoqty,shelfoqty,LocId,isDistributed,isListed,reasonID,isAuditDone,IsOwn,Facing,RField1,RField2,isAvailable,hasPriceTag from "
                    + DataMembers.tbl_closingstockdetail
                    + " where stockId="
                    + QT(stockID) + "";
            Cursor orderDetailCursor = db.selectSQL(sql1);
            if (orderDetailCursor != null) {
                while (orderDetailCursor.moveToNext()) {
                    String productId = orderDetailCursor.getString(0);
                    int shelfpqty = orderDetailCursor.getInt(1);
                    int shelfcqty = orderDetailCursor.getInt(2);
                    int whpqty = orderDetailCursor.getInt(3);
                    int whcqty = orderDetailCursor.getInt(4);
                    int whoqty = orderDetailCursor.getInt(5);
                    int shelfoqty = orderDetailCursor.getInt(6);
                    int locationId = orderDetailCursor.getInt(7);
                    int isDistributed = orderDetailCursor.getInt(8);
                    int isListed = orderDetailCursor.getInt(9);
                    int reasonID = orderDetailCursor.getInt(10);
                    int audit = orderDetailCursor.getInt(11);
                    int isOwn = orderDetailCursor.getInt(12);
                    int facing = orderDetailCursor.getInt(13);
                    int pouring = orderDetailCursor.getInt(14);
                    int cocktail = orderDetailCursor.getInt(15);
                    int availability = orderDetailCursor.getInt(16);
                    int priceTag = orderDetailCursor.getInt(17);

                    setStockCheckQtyDetails(productId, shelfpqty, shelfcqty,
                            whpqty, whcqty, whoqty, shelfoqty, locationId,
                            isDistributed, isListed, reasonID, audit, isOwn, facing, pouring, cocktail, menuCode, availability, priceTag);

                }
                orderDetailCursor.close();
            }
            db.closeDB();
        } catch (Exception e) {

            Commons.printException(e);
        }
    }

    /**
     * Update product Quantity for particular product Id.
     *
     * @param productid
     * @param --qty
     */
    private void setStockCheckQtyHistoryDetails(String productid, int shelfpqty,
                                                int shelfcqty, int whpqty, int whcqty, int whoqty, int shelfoqty,
                                                int locationId, int isDistributed, int isListed, int reasonID,
                                                int audit, int isOwn, int facing, int pouring, int cocktail,
                                                String menuCode, int availability) {

        //mTaggedProducts list only used in StockCheck screen. So updating only in mTaggedProducts
        ProductMasterBO product = null;
        if (menuCode.equals("MENU_STOCK") || menuCode.equals("MENU_COMBINE_STKCHK")) {
            product = ProductTaggingHelper.getInstance(getContext()).getTaggedProductBOById(productid);
        } else if (menuCode.equals("MENU_STK_ORD") || menuCode.equals("MENU_ORDER") || menuCode.equals("MENU_CATALOG_ORDER")) {
            product = productHelper.getProductMasterBOById(productid);
        }

        if (product != null && product.getOwn() == isOwn) {
            for (int j = 0; j < product.getLocations().size(); j++) {
                if (product.getLocations().get(j).getLocationId() == locationId) {
                    product.getLocations().get(j).setShelfPiece(shelfpqty);
                    product.getLocations().get(j).setShelfCase(shelfcqty);
                    product.getLocations().get(j).setShelfOuter(shelfoqty);
                    product.getLocations().get(j).setWHPiece(whpqty);
                    product.getLocations().get(j).setWHCase(whcqty);
                    product.getLocations().get(j).setWHOuter(whoqty);
                    product.setIsDistributed(isDistributed);
                    product.setIsListed(isListed);
                    product.getLocations().get(j).setReasonId(reasonID);
                    product.getLocations().get(j).setAudit(audit);
                    product.getLocations().get(j).setFacingQty(facing);
                    product.getLocations().get(j).setIsPouring(pouring);
                    product.getLocations().get(j).setCockTailQty(cocktail);
                    product.getLocations().get(j).setAvailability(availability);
                    return;
                }
            }
        }


    }

    /**
     * Update product Quantity for particular product Id.
     *
     * @param productid
     * @param --qty
     */
    private void setStockCheckQtyDetails(String productid, int shelfpqty,
                                         int shelfcqty, int whpqty, int whcqty, int whoqty, int shelfoqty,
                                         int locationId, int isDistributed, int isListed, int reasonID,
                                         int audit, int isOwn, int facing, int pouring, int cocktail,
                                         String menuCode, int availability, int priceTag) {

        //mTaggedProducts list only used in StockCheck screen. So updating only in mTaggedProducts
        ProductMasterBO product = null;
        StockCheckHelper stockCheckHelper = StockCheckHelper.getInstance(ctx);
        if (menuCode.equals("MENU_STOCK") || menuCode.equals("MENU_COMBINE_STKCHK")) {
            product = ProductTaggingHelper.getInstance(getContext()).getTaggedProductBOById(productid);
        } else if (menuCode.equals("MENU_STK_ORD") || menuCode.equals("MENU_ORDER") || menuCode.equals("MENU_CATALOG_ORDER")) {
            product = productHelper.getProductMasterBOById(productid);
        }

        if (menuCode.equals("MENU_COMBINE_STKCHK")) {
            if (!stockCheckHelper.SHOW_COMB_STOCK_SP)
                shelfpqty = -1;
            if (!stockCheckHelper.SHOW_COMB_STOCK_SC)
                shelfcqty = -1;
            if (!stockCheckHelper.SHOW_COMB_STOCK_SHELF_OUTER)
                shelfoqty = -1;
            if (!stockCheckHelper.SHOW_COMB_STOCK_CB)
                availability = -1;
        } else {
            if (!stockCheckHelper.SHOW_STOCK_SP)
                shelfpqty = -1;
            if (!stockCheckHelper.SHOW_STOCK_SC)
                shelfcqty = -1;
            if (!stockCheckHelper.SHOW_SHELF_OUTER)
                shelfoqty = -1;
            if (!stockCheckHelper.SHOW_STOCK_CB)
                availability = -1;
        }


        if (product != null && product.getOwn() == isOwn) {
            for (int j = 0; j < product.getLocations().size(); j++) {
                if (product.getLocations().get(j).getLocationId() == locationId) {
                    product.getLocations().get(j).setShelfPiece(shelfpqty);
                    product.getLocations().get(j).setShelfCase(shelfcqty);
                    product.getLocations().get(j).setShelfOuter(shelfoqty);
                    product.getLocations().get(j).setWHPiece(whpqty);
                    product.getLocations().get(j).setWHCase(whcqty);
                    product.getLocations().get(j).setWHOuter(whoqty);
                    product.setIsDistributed(isDistributed);
                    product.setIsListed(isListed);
                    product.getLocations().get(j).setReasonId(reasonID);
                    product.getLocations().get(j).setAudit(audit);
                    product.getLocations().get(j).setFacingQty(facing);
                    product.getLocations().get(j).setIsPouring(pouring);
                    product.getLocations().get(j).setCockTailQty(cocktail);
                    product.getLocations().get(j).setAvailability(availability);
                    product.getLocations().get(j).setPriceTagAvailability(priceTag);

                    int totalStockQty = (shelfpqty + (shelfcqty * product.getCaseSize()) + (shelfoqty * product.getOutersize()));
                    product.setTotalStockQty(product.getTotalStockQty() + totalStockQty);

                    return;
                }
            }
        }


    }


    private void setProductDetails(String productid, int pieceqty, int caseqty,
                                   int outerQty) {
        ProductMasterBO product;
        int siz = productHelper.getProductMaster().size();
        if (siz == 0)
            return;

        if (productid == null)
            return;

        for (int i = 0; i < siz; ++i) {
            product = productHelper.getProductMaster().get(i);

            if (product.getProductID().equals(productid)) {
                product.setOrderedPcsQty(pieceqty);
                product.setOrderedCaseQty(caseqty);
                product.setOrderedOuterQty(outerQty);

                productHelper.getProductMaster().setElementAt(product, i);

                return;
            }
        }
        return;
    }


    /**
     * Delete order Placed for the distid. Delete will only possible for
     * order which is not sync with server.
     *
     * @param distid
     */
    void deleteDistributorOrder(String distid) {
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            String orderId = "";

            StringBuffer sb = new StringBuffer();
            sb.append("select UId from DistOrderHeader where DistId="
                    + QT(distid));
            sb.append(" and upload='N'");

            Cursor orderDetailCursor = db.selectSQL(sb.toString());

            if (orderDetailCursor != null) {
                if (orderDetailCursor.moveToNext()) {
                    orderId = orderDetailCursor.getString(0);
                }
                orderDetailCursor.close();
            }
            db.deleteSQL(DataMembers.tbl_distributor_order_header, "UId=" + QT(orderId)
                    + " and upload='N'", false);
            db.deleteSQL(DataMembers.tbl_distributor_order_detail, "UId=" + QT(orderId)
                    + " and upload='N'", false);
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    /**
     * Delete order Placed for the distid. Delete will only possible for
     * order which is not sync with server.
     *
     * @param distid
     */
    void deleteDistributorStock(String distid) {
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            String orderId = "";

            StringBuffer sb = new StringBuffer();
            sb.append("select UId from DistStockCheckHeader where DistId="
                    + QT(distid));
            sb.append(" and upload='N'");

            Cursor orderDetailCursor = db.selectSQL(sb.toString());

            if (orderDetailCursor != null) {
                if (orderDetailCursor.moveToNext()) {
                    orderId = orderDetailCursor.getString(0);
                }
                orderDetailCursor.close();
            }

            db.deleteSQL(DataMembers.tbl_distributor_closingstock_header, "UId=" + QT(orderId)
                    + " and upload='N'", false);
            db.deleteSQL(DataMembers.tbl_distributor_closingstock_detail, "UId=" + QT(orderId)
                    + " and upload='N'", false);
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    @Deprecated
//this method moved into #NetWorkUitls class
    public boolean isOnline() {

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;

    }

    public void showAlertWithImage(String title, String msg, int id, boolean imgDisplay) {

        final int idd = id;
        if (getContext() == null) {
            return;
        }

        CommonDialog dialog = new CommonDialog(this, getContext(), title, msg, imgDisplay, getResources().getString(R.string.ok), new CommonDialog.PositiveClickListener() {
            @Override
            public void onPositiveButtonClick() {

                if (idd == DataMembers.NOTIFY_NEW_OUTLET_SAVED) {
//                    NewOutlet frm = (NewOutlet) ctx;
//                    frm.finish();
//                    BusinessModel.loadActivity(ctx, DataMembers.actHomeScreen);
                } else if (idd == DataMembers.NOTIFY_UPLOAD_CLOSINGSTOCK) {
                    StockCheckActivity frm = (StockCheckActivity) ctx;
                    frm.finish();
                    ctx.startActivity(new Intent(ctx, HomeScreenTwo.class));
                } else if (idd == 3333) {
                    ReAllocationActivity frm = (ReAllocationActivity) ctx;
                    frm.finish();
                    loadActivity(ctx, DataMembers.actHomeScreen);
                } else if (idd == 1234) {
                    productHelper.clearOrderTable();
                    if (configurationMasterHelper.SHOW_ZEBRA_TITAN) {
                        PrintPreviewScreenTitan frm = (PrintPreviewScreenTitan) ctx;
                        loadActivity(frm, DataMembers.actHomeScreenTwo);
                        frm.finish();

                    }
                } else if (idd == 121) {

                    if (configurationMasterHelper.SHOW_ZEBRA_TITAN) {
                        PrintPreviewScreenTitan frm = (PrintPreviewScreenTitan) ctx;

                        frm.finish();

                    }
                } else if (idd == DataMembers.SAVECOLLECTION) {
                    CollectionScreen frm = (CollectionScreen) ctx;
                    frm.finish();
                    loadActivity(ctx,
                            DataMembers.actHomeScreenTwo);
                } else if (idd == DataMembers.NOTIFY_ORDER_SAVED) {
                    OrderSummary frm = (OrderSummary) ctx;
                    Intent returnIntent = new Intent();
                    frm.setResult(Activity.RESULT_OK, returnIntent);
                    frm.finish();
                    loadActivity(ctx,
                            DataMembers.actHomeScreenTwo);
                } else if (idd == DataMembers.NOTIFY_CLOSE_HOME) {
                    HomeScreenFragment currentFragment = (HomeScreenFragment) ((FragmentActivity) ctx).getSupportFragmentManager().findFragmentById(R.id.homescreen_fragment);
                    if (currentFragment != null) {
                        currentFragment.loadHomeMenuConfiguration();
                        currentFragment.refreshList(false);
                    }
                } else if (idd == DataMembers.NOTIFY_SALES_RETURN_SAVED) {
                    SalesReturnSummery frm = (SalesReturnSummery) ctx;
                    Intent intent = new Intent();
                    frm.setResult(frm.RESULT_OK, intent);
                    frm.finish();
                    loadActivity(ctx, DataMembers.actHomeScreenTwo);

                } else if (idd == DataMembers.NOTIFY_INVOICE_SAVED) {
                    if (ctx.getClass().getSimpleName()
                            .equals("BatchAllocation")) {
                        BatchAllocation frm = (BatchAllocation) ctx;

                    } else {
                        OrderSummary frm = (OrderSummary) ctx;
                        Intent returnIntent = new Intent();
                        frm.setResult(Activity.RESULT_OK, returnIntent);
                        frm.finish();
                        loadActivity(ctx, DataMembers.actHomeScreenTwo);
                    }
                } else if (idd == 1502) {
                    Gallery frm = (Gallery) ctx;
                    frm.finish();
                    loadActivity(ctx, DataMembers.actPhotocapture);
                } else if (idd == DataMembers.NOTIFY_NEW_OUTLET_SAVED) {
                    NewOutlet frm = (NewOutlet) ctx;
                    frm.finish();
                    loadActivity(ctx, DataMembers.actHomeScreen);
                } else if (idd == -881) {
                    // do nothing
                } else if (idd == 5000) {
                    CollectionPreviewScreen frm = (CollectionPreviewScreen) ctx;
                    frm.finish();
                } else if (idd == 5001) {
                    EODStockReportPreviewScreen frm = (EODStockReportPreviewScreen) ctx;
                    frm.finish();
                } else if (idd == DataMembers.NEWOUTLET_UPLOAD) {
                    NewOutlet frm = (NewOutlet) ctx;
                    frm.finish();
                    loadActivity(ctx, DataMembers.actHomeScreen);
                } else if (idd == 5002) {
                    InvoiceReportDetail frm = (InvoiceReportDetail) ctx;
                    frm.finish();
                } else if (idd == 5003) {
                    Intent intent = new Intent(ctx,
                            LoginScreen.class);
                    HomeScreenActivity frm = (HomeScreenActivity) ctx;
                    frm.startActivity(intent);
                    frm.finish();
                } else if (idd == 5004) {
                    CreditNotePrintPreviewScreen frm = (CreditNotePrintPreviewScreen) ctx;
                    frm.finish();
                } else if (idd == 6004) {
                    CallAnalysisActivity callAnalysisActivity = (CallAnalysisActivity) ctx;
                    loadActivity(ctx, DataMembers.actPlanning);
                    callAnalysisActivity.finish();
                } else if (idd == DataMembers.NOTIFY_ORDER_DELETED) {
                    OrderSummary frm = (OrderSummary) ctx;
                    Intent returnIntent = new Intent();
                    frm.setResult(Activity.RESULT_OK, returnIntent);
                    frm.finish();
                    loadActivity(ctx, DataMembers.actHomeScreenTwo);
                }

            }
        });
        dialog.setCancelable(false);
        dialog.show();

    }

    /**
     * @See {@link com.ivy.core.base.view.BaseActivity#showAlert(String, String)}
     * @deprecated
     */
    public void showAlert(String msg, int id) {
        showAlertWithImage("", msg, id, false);
    }


    public OrderHeader getOrderHeaderBO() {
        return orderHeaderBO;
    }

    public void setOrderHeaderBO(OrderHeader orderHeaderBO) {
        this.orderHeaderBO = orderHeaderBO;
    }


    /**
     * @return
     * @See {@link AppDataProviderImpl#getRetailMaster()}
     * @deprecated
     */
    public RetailerMasterBO getRetailerMasterBO() {
        return retailerMasterBO;
    }

    /**
     * @return
     * @See {@link AppDataProviderImpl#setRetailerMaster(RetailerMasterBO)}
     * @deprecated
     */
    public void setRetailerMasterBO(RetailerMasterBO retailerMasterBO) {
        // Until all the code is refactored, Retail master is updated in the Appdataprovider and business model
        appDataProvider.setRetailerMaster(retailerMasterBO);

        //TODO remove business model retailer master
        this.retailerMasterBO = retailerMasterBO;
    }

    public HashMap<String, RetailerMasterBO> getRetailerBoByRetailerID() {
        return mRetailerBOByRetailerid;
    }

    @Deprecated
    /**
     * @deprecated
     * @see {@link AppUtils#getApplicationVersionName(Context)}
     */
    public String getApplicationVersionName() {
        String versionName = "";
        try {
            PackageInfo pinfo = getPackageManager().getPackageInfo(
                    getPackageName(), 0);
            versionName = pinfo.versionName;
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return versionName;
    }

    // *****************************************************

    @Deprecated
    /**
     * @deprecated
     * @see {@link AppUtils#getApplicationVersionNumber(Context)}
     */
    public String getApplicationVersionNumber() {
        int versionNumber = 0;
        try {
            PackageInfo pinfo = getPackageManager().getPackageInfo(
                    getPackageName(), 0);
            versionNumber = pinfo.versionCode;
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return versionNumber + "";
    }

    /**
     * Get Digital Content URL and Count From PlanogramMaster
     */
    public void getimageDownloadURL() {
        try {
            boolean isAmazonCloud = true;
            boolean isSFDCCloud = false;
            boolean isAzureCloud = false;

            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT Rfield FROM HHTModuleMaster where hhtCode = 'CLOUD_STORAGE' and flag = 1 and ForSwitchSeller = 0");
            if (c != null) {
                while (c.moveToNext()) {
                    if (c.getInt(0) == 0) {
                        isAmazonCloud = true;
                    } else if (c.getInt(0) == 1) {
                        isSFDCCloud = true;
                    } else if (c.getInt(0) == 2) {
                        isAzureCloud = true;
                    } else {
                        isAmazonCloud = true;
                    }
                }
                c.close();
            }

            c = null;

            if (!isAmazonCloud && !isAzureCloud) {
                c = db
                        .selectSQL("SELECT ListName FROM StandardListMaster Where ListCode = 'AS_HOST'");
                if (c != null) {
                    while (c.moveToNext()) {
                        DataMembers.IMG_DOWN_URL = c.getString(0);
                    }
                    c.close();
                }
            } else {
                c = db
                        .selectSQL("SELECT ListName FROM StandardListMaster Where ListCode = 'AS_ROOT_DIR'");
                if (c != null) {
                    while (c.moveToNext()) {
                        DataMembers.IMG_DOWN_URL = c.getString(0) + "/";
                    }
                    c.close();
                }
            }
            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    /**
     * Get Digital Content URL and Count From PlanogramMaster
     */
    public boolean isDigitalContentAvailable() {
        getimageDownloadURL();
        setDigitalContentURLS(new HashMap<>());
        setDigitalContentLargeFileURLS(new HashMap<>());
        setDigitalContentSFDCURLS(new HashMap<>());

        configurationMasterHelper.getDigitalContentSize();

        boolean isDigiContentAvail = false;
        try {

            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            Cursor c = db
                    .selectSQL("SELECT DISTINCT ImgURL,imgId FROM PlanogramImageInfo");
            if (c != null) {
                while (c.moveToNext()) {
                    if (configurationMasterHelper.IS_PLANOGRAM_RETAIN_LAST_VISIT_TRAN) {

                        DigitalContentModel digitalContentBO = new DigitalContentModel();

                        String downloadUrl = DataMembers.IMG_DOWN_URL + "" + c.getString(0);
                        digitalContentBO.setFileSize(String.valueOf(FileDownloadProvider.MB_IN_BYTES * 2));//approx 2mb
                        digitalContentBO.setImageID(c.getInt(1));
                        digitalContentBO.setImgUrl(downloadUrl);
                        digitalContentBO.setContentFrom(DataMembers.PLANOGRAM);
                        digitalContentBO.setUserId(userMasterHelper.getUserMasterBO().getUserid());

                        digitalContentLargeFileURLS.put(digitalContentBO.getImageID(), digitalContentBO);

                    } else {
                        getDigitalContentURLS().put(
                                DataMembers.IMG_DOWN_URL + "" + c.getString(0),
                                DataMembers.PLANOGRAM);
                    }

                }
                c.close();
            }

            if (configurationMasterHelper.IS_PLANOGRAM_RETAIN_LAST_VISIT_TRAN) {
                c = db
                        .selectSQL("SELECT DISTINCT ImagePath,imageId FROM LastVisitPlanogramImages");
                int count = 0;
                if (c != null) {
                    while (c.moveToNext()) {

                        count += 100;
                        DigitalContentModel digitalContentBO = new DigitalContentModel();

                        String downloadUrl = DataMembers.IMG_DOWN_URL + "" + c.getString(0);
                        digitalContentBO.setFileSize(String.valueOf(FileDownloadProvider.MB_IN_BYTES * 2));// approx  2 mb
                        digitalContentBO.setImageID(c.getInt(1));
                        digitalContentBO.setImgUrl(downloadUrl);
                        digitalContentBO.setContentFrom(DataMembers.PLANOGRAM);
                        digitalContentBO.setUserId(userMasterHelper.getUserMasterBO().getUserid());

                        digitalContentLargeFileURLS.put(digitalContentBO.getImageID() + count, digitalContentBO);


                    }
                    c.close();
                }
            }

            c = db.selectSQL("SELECT DISTINCT ImageURL,fileSize,imageid,imagename,ifnull(SM.listCode,'') FROM DigitalContentMaster left join standardListMaster SM ON SM.listId=storageType");
            if (c != null) {
                while (c.moveToNext()) {

                    if (c.getString(4).equalsIgnoreCase("SFDC")) {// SFDC type
                        getDigitalContentSFDCURLS().put(c.getString(0) + "%" + c.getString(3), DataMembers.DIGITALCONTENT);
                    } else {
                        if (configurationMasterHelper.DIGITAL_CONTENT_SIZE != -1 &&
                                configurationMasterHelper.DIGITAL_CONTENT_SIZE < c.getLong(1)) {
                            DigitalContentModel digitalContentBO = new DigitalContentModel();

                            String downloadUrl = DataMembers.IMG_DOWN_URL + "" + c.getString(0);
                            digitalContentBO.setFileSize(c.getString(1));
                            digitalContentBO.setImageID(c.getInt(2));
                            digitalContentBO.setImgUrl(downloadUrl);
                            digitalContentBO.setContentFrom(DataMembers.DIGITALCONTENT);
                            digitalContentBO.setUserId(userMasterHelper.getUserMasterBO().getUserid());

                            digitalContentLargeFileURLS.put(digitalContentBO.getImageID(), digitalContentBO);

                        } else {

                            getDigitalContentURLS().put(
                                    DataMembers.IMG_DOWN_URL + "" + c.getString(0),
                                    DataMembers.DIGITALCONTENT);

                        }
                    }

                }
                c.close();
            }

            c = db.selectSQL("SELECT DISTINCT ImageURL FROM App_ImageInfo");
            if (c != null) {
                while (c.moveToNext()) {
                    getDigitalContentURLS().put(
                            DataMembers.IMG_DOWN_URL + "" + c.getString(0),
                            DataMembers.APP_DIGITAL_CONTENT);
                }
                c.close();
            }

            c = db.selectSQL("SELECT DISTINCT ImageURL FROM MVPBadgeMaster");
            if (c != null) {
                while (c.moveToNext()) {
                    getDigitalContentURLS().put(
                            DataMembers.IMG_DOWN_URL + "" + c.getString(0),
                            DataMembers.MVP);
                }
                c.close();
            }

            c = db.selectSQL("SELECT DISTINCT ImagePath FROM LoyaltyBenefits");
            if (c != null) {
                while ((c.moveToNext())) {
                    getDigitalContentURLS().put(
                            DataMembers.IMG_DOWN_URL + "" + c.getString(0),
                            DataMembers.LOYALTY_POINTS);

                }
                c.close();
            }

            c = db.selectSQL("SELECT DISTINCT ProfileImagePath FROM UserMaster");
            if (c != null) {
                while ((c.moveToNext())) {
                    getDigitalContentURLS().put(
                            DataMembers.IMG_DOWN_URL + "" + c.getString(0),
                            DataMembers.USER);

                }
                c.close();
            }


            c = db.selectSQL("SELECT DISTINCT ProfileImagePath FROM RetailerMaster");
            if (c != null) {
                while ((c.moveToNext())) {
                    getDigitalContentURLS().put(
                            DataMembers.IMG_DOWN_URL + "" + c.getString(0),
                            DataMembers.PROFILE);

                }
                c.close();
            }

            c = db.selectSQL("SELECT DISTINCT TaskImageName FROM TaskImageDetails");
            if (c != null) {
                while ((c.moveToNext())) {
                    getDigitalContentURLS().put(
                            DataMembers.IMG_DOWN_URL + "" + c.getString(0),
                            DataMembers.TASK_DIGITAL_CONTENT);

                }
                c.close();
            }

            c = db.selectSQL("SELECT DISTINCT ImageName FROM SerializedAssetImageDetails");
            if (c != null) {
                while ((c.moveToNext())) {
                    getDigitalContentURLS().put(
                            DataMembers.IMG_DOWN_URL + "" + c.getString(0),
                            DataMembers.SERIALIZED_ASSET_DIG_CONTENT);

                }
                c.close();
            }

            db.closeDB();

            getDigitalContentURLS().put(
                    DataMembers.IMG_DOWN_URL + "PRINT/" + "order_print.xml",
                    DataMembers.PRINT);
            getDigitalContentURLS().put(
                    DataMembers.IMG_DOWN_URL + "PRINT/" + "invoice_print.xml",
                    DataMembers.PRINT);
            getDigitalContentURLS().put(
                    DataMembers.IMG_DOWN_URL + "PRINT/" + "credit_note_print.xml",
                    DataMembers.PRINT);
            getDigitalContentURLS().put(
                    DataMembers.IMG_DOWN_URL + "PRINT/" + "eod_print.xml",
                    DataMembers.PRINT);

            if (getDigitalContentURLS().size() > 0 || getDigitalContentLargeFileURLS().size() > 0)
                isDigiContentAvail = true;

            return isDigiContentAvail;

        } catch (Exception e) {
            Commons.printException(e);
            return isDigiContentAvail;
        }
    }

    public boolean isAutoUpdateAvailable() {
        SharedPreferences pref = this.getSharedPreferences("autoupdate",
                MODE_PRIVATE);
        String key = pref.getString("isUpdateExist", "False");
        return !key.equals("False");
    }

    public String getUpdateURL() {
        SharedPreferences pref = this.getSharedPreferences("autoupdate",
                MODE_PRIVATE);
        return pref.getString("URL", "");
    }


    public HashMap<String, String> getDigitalContentURLS() {
        return digitalContentURLS;
    }

    public void setDigitalContentURLS(HashMap<String, String> digitalContentURLS) {
        this.digitalContentURLS = digitalContentURLS;
    }

    public HashMap<String, String> getDigitalContentSFDCURLS() {
        return digitalContentSFDCURLS;
    }

    public void setDigitalContentSFDCURLS(HashMap<String, String> digitalContentSFDCURLS) {
        this.digitalContentSFDCURLS = digitalContentSFDCURLS;
    }

    public Vector<RetailerMasterBO> getRetailerMaster() {
        return retailerMaster;
    }

    public void setRetailerMaster(Vector<RetailerMasterBO> retailerMaster) {
        if (codeCleanUpUtil != null)
            codeCleanUpUtil.setRetailerMaster(retailerMaster);
        this.retailerMaster = retailerMaster;
    }


    public ArrayList<RetailerMasterBO> getVisitretailerMaster() {
        return visitretailerMaster;
    }

    public void setVisitretailerMaster(ArrayList<RetailerMasterBO> visitretailerMaster) {
        this.visitretailerMaster = visitretailerMaster;
    }

    public void setIsOrdered(String falg) {
        RetailerMasterBO retailer;
        int siz = retailerMaster.size();
        if (siz == 0)
            return;

        for (int i = 0; i < siz; ++i) {
            retailer = retailerMaster.get(i);
            if (retailer.getRetailerID().equals(
                    getRetailerMasterBO().getRetailerID())) {
                retailer.setOrdered(falg);
                retailerMaster.setElementAt(retailer, i);
                return;
            }
        }

    }


    public void setIsReviewPlan(String flag) {
        RetailerMasterBO retailer;
        int siz = retailerMaster.size();
        if (siz == 0)
            return;

        for (int i = 0; i < siz; ++i) {
            retailer = retailerMaster.get(i);
            if (retailer.getRetailerID().equals(
                    getRetailerMasterBO().getRetailerID())) {
                retailer.setIsReviewPlan(flag);
                retailerMaster.setElementAt(retailer, i);
                return;
            }
        }

    }

    void setIsOrderMerch() {
        RetailerMasterBO retailer;
        int siz = retailerMaster.size();
        if (siz == 0)
            return;

        for (int i = 0; i < siz; ++i) {
            retailer = retailerMaster.get(i);
            if (retailer.getRetailerID().equals(
                    getRetailerMasterBO().getRetailerID())) {
                retailer.setIsOrderMerch("Y");
                retailerMaster.setElementAt(retailer, i);
                return;
            }
        }

    }

    void setIsInvoiceDone() {
        RetailerMasterBO retailer;
        int siz = retailerMaster.size();
        if (siz == 0)
            return;

        for (int i = 0; i < siz; ++i) {
            retailer = retailerMaster.get(i);
            if (retailer.getRetailerID().equals(
                    getRetailerMasterBO().getRetailerID())) {
                retailer.setInvoiceDone("Y");
                retailerMaster.setElementAt(retailer, i);
                return;
            }
        }

    }

    public void setOrderedInDB(String flag) {
        DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME);
        db.createDataBase();
        db.openDataBase();
        db.executeQ("update " + DataMembers.tbl_retailerMaster
                + " set isOrdered=" + QT(flag) + " where retailerid="
                + QT(getRetailerMasterBO().getRetailerID()));
        db.closeDB();
    }

    void setOrderMerchInDB(String flag) {
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            db.executeQ("update " + DataMembers.tbl_retailerMaster
                    + " set isOrderMerch=" + QT(flag) + " where retailerid="
                    + QT(getRetailerMasterBO().getRetailerID()));
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    void setInvoiceDoneInDB() {
        DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME);
        db.createDataBase();
        db.openDataBase();
        db.executeQ("update " + DataMembers.tbl_retailerMaster
                + " set isInvoiceCreated=" + QT("Y") + " where retailerid="
                + QT(getRetailerMasterBO().getRetailerID()));
        db.closeDB();
    }

    /* ******* Invoice Number To Print End ******* */

    public float getCollectionValue() {

        DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME);
        db.createDataBase();
        db.openDataBase();
        float total = 0;
        Cursor c = db
                .selectSQL("select ifnull(sum(Amount),0) from Payment where retailerid="
                        + StringUtils.QT(getRetailerMasterBO().getRetailerID()) + " and Date = " + StringUtils.QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)));
        if (c != null) {
            if (c.getCount() > 0) {
                c.moveToNext();
                total = c.getFloat(0);
            }
        }
        c.close();
        db.closeDB();

        return total;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }


    public void deleteAdhocImageDetailsFormTable(String ImageName) {
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.openDataBase();
            db.deleteSQL(DataMembers.tbl_RoadActivityDetail, "imgname ="
                    + QT("RoadActivity" + "/" + DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL_PLAIN)
                    + "/" + userMasterHelper.getUserMasterBO().getUserid()
                    + "/" + ImageName), false); // QT(ImageName));

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }


    /* ******* Invoice Number To Print End ******* */




    /* ******* Invoice Number To Print End ******* */

    public int getAdhocimgCount() {
        int i = 0;
        DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME);
        db.openDataBase();

        Cursor c = db.selectSQL("select count(distinct imgname)from RoadActivityTransactiondetail where Upload = 'N'");
        if (c != null) {
            if (c.moveToNext()) {
                i = c.getInt(0);
            }
            c.close();
        }

        db.closeDB();
        return i;
    }

    /**
     * This method will download the acheived value of all the retailer and set
     * it in RetailerBO. setVisit_Actual will hold this value. If the seller is
     * Preseller , sum will be calculated from OrderHeader otherwise from
     * Invoice.
     */
    public void downloadVisit_Actual_Achieved() {
        try {
            RetailerMasterBO ret = new RetailerMasterBO();
            int siz = retailerMaster.size();
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.openDataBase();

            Cursor c = null;

            for (int i = 0; i < siz; i++) {
                ret = retailerMaster.get(i);
                ret.setVisit_Actual(0);
            }

           /* if (configurationMasterHelper.IS_INVOICE) {
                c = db.selectSQL("select Retailerid, sum(invNetamount) from InvoiceMaster where invoicedate = "
                        + QT(userMasterHelper.getUserMasterBO().getDownloadDate()) + " group by retailerid");
            } else {*/
            c = db.selectSQL("select RetailerID, sum(OrderValue) from OrderHeader where upload!='X' group by retailerid");
            //}
            if (c != null) {
                while (c.moveToNext()) {
                    for (int i = 0; i < siz; i++) {
                        ret = retailerMaster.get(i);
                        if (ret.getRetailerID().equals(c.getString(0))) {
                            ret.setVisit_Actual(c.getFloat(1));
                        }
                    }
                }

                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    /**
     * Update the visited status in DB as well as loaded objects. In
     * retailerMaster isVisited field will be set to 'Y'
     */
    public void updateIsVisitedFlag(String flag) {
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME);
            db.openDataBase();
            db.updateSQL("Update RetailerBeatMapping set isVisited=" + QT(flag) + " where RetailerID ="
                    + getRetailerMasterBO().getRetailerID()
                    + " AND BeatID=" + getRetailerMasterBO().getBeatID());

            if (configurationMasterHelper.SHOW_DATE_PLAN_ROUTE)
                db.updateSQL("Update DatewisePlan set VisitStatus=" + StringUtils.QT(RetailerConstants.COMPLETED) +
                        " where EntityId=" + StringUtils.QT(getAppDataProvider().getRetailMaster().getRetailerID()) + " and Date=" + StringUtils.QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)));

            db.closeDB();

            // update loaded retailerMaster flag.
            int siz = getRetailerMaster().size();
            for (int i = 0; i < siz; i++) {
                RetailerMasterBO ret = retailerMaster.get(i);
                if (ret.getRetailerID().equals(
                        getRetailerMasterBO().getRetailerID())) {
                    ret.setIsVisited(flag);
                }
            }

            // Updated selected object flag
            getRetailerMasterBO().setIsVisited(flag);

            if ("P".equals(flag))
                getAppDataProvider().setPausedRetailer(getRetailerMasterBO());
            else
                getAppDataProvider().setPausedRetailer(null);

        } catch (Exception e) {
            Commons.printException(e);
        }
    }


    public boolean hasStockInOrder() {
        int siz = productHelper.getProductMaster().size();
        if (siz == 0)
            return false;
        for (int i = 0; i < siz; ++i) {
            ProductMasterBO product = productHelper
                    .getProductMaster().get(i);

            int siz1 = product.getLocations().size();
            for (int j = 0; j < siz1; j++) {
                if (product.getLocations().get(j).getShelfPiece() > -1
                        || product.getLocations().get(j).getShelfCase() > -1
                        || product.getLocations().get(j).getShelfOuter() > -1
                        || product.getLocations().get(j).getWHPiece() > 0
                        || product.getLocations().get(j).getWHCase() > 0
                        || product.getLocations().get(j).getWHOuter() > 0
                        || product.getLocations().get(j).getAvailability() > -1)
                    return true;
            }
        }
        return false;
    }


    public void calculateFitscoreandInsert(DBUtil db, double sum, String module) {
        String headerID = "";
        double headerScore = 0;
        String fitscoreHeaderColumns = "Tid,RetailerID,Date,Score,Upload";
        String fitscoreHeaderValues = "";
        String fitscoreDetailColumns = "Tid, ModuleCode,Weightage,Score,Upload";
        String fitscoreDetailValues = "";

        try {
            Cursor closingStockCursor = db
                    .selectSQL("select Tid from RetailerScoreHeader where RetailerID=" + getRetailerMasterBO().getRetailerID() + " and Date = " + QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)));

            if (closingStockCursor.getCount() > 0) {
                closingStockCursor.moveToNext();
                if (closingStockCursor.getString(0) != null) {
                    headerID = QT(closingStockCursor.getString(0));
                    db.deleteSQL("RetailerScoreDetails", "Tid=" + headerID + " and ModuleCode = " + QT(module), false);
                }
            }
            closingStockCursor.close();

            String tid = (headerID.trim().length() == 0) ? QT(userMasterHelper.getUserMasterBO().getUserid() + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID)) : headerID;
            int moduleWeightage = fitscoreHelper.getModuleWeightage(module);
            double achieved = (((double) sum / (double) 100) * moduleWeightage);
            fitscoreDetailValues = (tid) + ", " + QT(module) + ", " + moduleWeightage + ", " + achieved + ", " + QT("N");
            db.insertSQL(DataMembers.tbl_retailerscoredetail, fitscoreDetailColumns, fitscoreDetailValues);

            if (headerID.trim().length() == 0) {
                String retailerID = getRetailerMasterBO().getRetailerID();
                String date = QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL));
                fitscoreHeaderValues = (tid) + ", " + QT(retailerID) + ", " + date + ", " + achieved + ", " + QT("N");
                db.insertSQL(DataMembers.tbl_retailerscoreheader, fitscoreHeaderColumns, fitscoreHeaderValues);
            } else {
                Cursor achievedCursor = db
                        .selectSQL("select sum(0+ifnull(B.Score,0)) from RetailerScoreHeader A inner join RetailerScoreDetails B on A.Tid = B.Tid where A.RetailerID="
                                + getRetailerMasterBO().getRetailerID() + " and A.Date = " + QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)));

                if (achievedCursor.getCount() > 0) {
                    achievedCursor.moveToNext();
                    headerScore = achievedCursor.getDouble(0);
                }
                achievedCursor.close();
                db.updateSQL("Update " + DataMembers.tbl_retailerscoreheader + " set Score = " + headerScore + " where " +
                        " Date = " + QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) + "" +
                        " and RetailerID = " + QT(getRetailerMasterBO().getRetailerID()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public HashMap<String, ArrayList<TaxBO>> getmFreeProductTaxListByProductId() {
        return mFreeProductTaxListByProductId;
    }

    private HashMap<String, ArrayList<TaxBO>> mFreeProductTaxListByProductId;


    //updating tax for scheme free products
    public void updateTaxForFreeProduct(Vector<ProductMasterBO> mOrderedProductList, String orderId, DBUtil db) {

        try {
            double largestTax = 0;
            String productIdHasLargetTax = null;

            // getting product which has more tax
            for (ProductMasterBO prod : mOrderedProductList) {
                for (TaxBO taxBO : productHelper.taxHelper.getmTaxListByProductId().get(prod.getProductID())) {
                    if (taxBO.getTaxRate() > largestTax) {
                        largestTax = taxBO.getTaxRate();
                        productIdHasLargetTax = prod.getProductID();
                    }
                }

            }

            if (productIdHasLargetTax != null) {

                ProductMasterBO productWithMaxTaxRate = productHelper.getProductMasterBOById(productIdHasLargetTax);


                for (ProductMasterBO prod : mOrderedProductList) {
                    for (SchemeProductBO schemeProductBO : prod.getSchemeProducts()) {

                        double lineValue = 0;

                        // calculating line value for scheme product
                        if (productWithMaxTaxRate.getCaseUomId() == schemeProductBO.getUomID()
                                && productWithMaxTaxRate.getCaseUomId() != 0) {
                            lineValue += (productWithMaxTaxRate.getCsrp() * schemeProductBO.getQuantitySelected());
                        }
                        if (productWithMaxTaxRate.getOuUomid() == schemeProductBO.getUomID()
                                && productWithMaxTaxRate.getOuUomid() != 0) {
                            lineValue += (productWithMaxTaxRate.getOsrp() * schemeProductBO.getQuantitySelected());
                        }
                        if (productWithMaxTaxRate.getPcUomid() == schemeProductBO.getUomID()
                                && productWithMaxTaxRate.getPcUomid() != 0) {
                            lineValue += (productWithMaxTaxRate.getSrp() * schemeProductBO.getQuantitySelected());
                        }

                        lineValue = SDUtil.formatAsPerCalculationConfig(lineValue);

                        schemeProductBO.setLineValue(lineValue);

                        //


                        if (mFreeProductTaxListByProductId == null)
                            mFreeProductTaxListByProductId = new HashMap<>();

                        // cloning highest tax list  for scheme free product
                        ArrayList<TaxBO> clonedTaxList = new ArrayList<>();
                        for (TaxBO bo : productHelper.taxHelper.getmTaxListByProductId().get(productWithMaxTaxRate.getProductID())) {
                            clonedTaxList.add(productHelper.taxHelper.cloneTaxBo(bo));

                        }

                        mFreeProductTaxListByProductId.put(schemeProductBO.getProductId(), clonedTaxList);

                        ProductMasterBO schemeProduct = new ProductMasterBO(productHelper.getProductMasterBOById(schemeProductBO.getProductId()));

                        double totalTaxAmount = 0;
                        for (TaxBO taxBO : mFreeProductTaxListByProductId.get(schemeProductBO.getProductId())) {
                            //Excluding tax for scheme free products

                            //setting line value to discount_order_value, because it is needed for excluding tax values..
                            schemeProduct.setNetValue(lineValue);

                            //just resetting values
                            schemeProduct.setOrderedPcsQty(0);
                            schemeProduct.setOrderedCaseQty(0);
                            schemeProduct.setOrderedOuterQty(0);
                            schemeProduct.setFoc(0);

                            // excluding tax values
                            productHelper.taxHelper.calculateTaxOnTax(schemeProduct, taxBO, true);

                            //inserting free product tax details to db
                            productHelper.taxHelper.insertProductLevelTaxForFreeProduct(orderId, db, schemeProductBO.getProductId(), taxBO);

                            totalTaxAmount += taxBO.getTotalTaxAmount();
                        }
                        schemeProductBO.setTaxAmount(totalTaxAmount);
                    }
                }
            }
        } catch (Exception ex) {
            Commons.printException(ex);
        }

    }


    // for amazon cloud image upload

    public String formatValue(double value) {

        return SDUtil.format(value,
                configurationMasterHelper.VALUE_PRECISION_COUNT,
                configurationMasterHelper.VALUE_COMMA_COUNT, configurationMasterHelper.IS_DOT_FOR_GROUP);
    }

    public String formatPercent(double value) {

        return SDUtil.format(value,
                configurationMasterHelper.PERCENT_PRECISION_COUNT, 0);
    }


    public String getProductFilter() {
        SharedPreferences pref = this.getSharedPreferences("ProductFilter",
                MODE_PRIVATE);

        return pref.getString("FilterType", getResources()
                .getString(R.string.product_name));

    }

    public void setProductFilter(String filterType) {

        SharedPreferences pref = this.getSharedPreferences("ProductFilter",
                MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = pref.edit();
        prefsEditor.putString("FilterType", filterType);
        prefsEditor.apply();
    }


    private void sentMessageToHandler(int messageID, String message,
                                      Handler handler) {
        if (handler != null) {
            mMessage = new Message();
            mMessage.what = messageID;
            mMessage.obj = message;

            handler.sendMessage(mMessage);
        }
    }

    // Amazon Image Upload
    void uploadImageToAmazonCloud(Handler handler) {
        try {
            AWSConnectionHelper.getInstance().setAmazonS3Credentials(getApplicationContext());
            TransferUtility tm = new TransferUtility(AWSConnectionHelper.getInstance().getS3Connection(), getApplicationContext
                    ());

            folder = new File(FileUtils.photoFolderPath + "/");

            sfFiles = folder.listFiles();

            uploadFileCount = sfFiles.length;
            successCount = 0;
            isErrorOccured = false;

            String tag = "Business Model";
            Commons.print(tag + ",ss : " + uploadFileCount);

            for (int i = 0; i < uploadFileCount; i++) {

                String filename = sfFiles[i].getName();
                //  print invoice file not upload to server

                getResponseForUploadImageToAmazonCloud(filename, tm, handler);

            }
            // success
            AWSConnectionHelper.getInstance().getS3Connection().shutdown();
            // tm.shutdownNow();

        } catch (Exception e) {

            Commons.printException(e);
        }
    }


    private void getResponseForUploadImageToAmazonCloud(String
                                                                imageName, TransferUtility tm, final Handler mHandler) {
        try {
            String start_time = DateTimeUtils.now(DateTimeUtils.DATE_TIME_NEW);
            final File image = new File(folder, "/" + imageName);
            String mBucketName;
            String mBucketDetails = DataMembers.S3_BUCKET + "/"
                    + DataMembers.S3_ROOT_DIRECTORY;

            String path = "/"
                    + userMasterHelper.getUserMasterBO().getDownloadDate()
                    .replace("/", "") + "/"
                    + userMasterHelper.getUserMasterBO().getUserid();


            if (imageName.startsWith("AT_") || imageName.startsWith("NAT_")) {
                mBucketName = mBucketDetails + "/" + "Asset" + path;
            } else if (imageName.startsWith("NO_")) {
                mBucketName = mBucketDetails + "/" + "RetailerImages" +
                        path;
            } else if (imageName.startsWith("SGN_")) {
                mBucketName = mBucketDetails + "/" + "Invoice" + path;
            } else if (imageName.startsWith("INIT_")) {
                mBucketName = mBucketDetails + "/" + "Initiative" + path;
            } else if (imageName.startsWith("PT_")) {
                mBucketName = mBucketDetails + "/" + "Promotion" + path;
            } else if (imageName.startsWith("SOD_")) {
                mBucketName = mBucketDetails + "/" + "SOD" + path;
            } else if (imageName.startsWith("SOS_")) {
                mBucketName = mBucketDetails + "/" + "SOS" + path;
            } else if (imageName.startsWith("SOSKU_")) {
                mBucketName = mBucketDetails + "/" + "SOSKU" + path;
            } else if (imageName.startsWith("PL_")) {
                mBucketName = mBucketDetails + "/" + "Planogram" + path;
            } else if (imageName.startsWith("VPL_")) {
                mBucketName = mBucketDetails + "/" + "VanPlanogram" +
                        path;
            } else if (imageName.startsWith("CPL_")) {
                mBucketName = mBucketDetails + "/" + "CounterPlanogram" +
                        path;
            } else if (imageName.startsWith("CT_")) {
                mBucketName = mBucketDetails + "/" + "Competitor" + path;
            } else if (imageName.startsWith("SVY_")) {
                mBucketName = mBucketDetails + "/" + "Survey" + path;
            } else if (imageName.startsWith("RA_")) {
                mBucketName = mBucketDetails + "/" + "RoadActivity" + path;
            } else if (imageName.startsWith("COL_")) {
                mBucketName = mBucketDetails + "/" + "Collection" + path;
            } else if (imageName.startsWith("RT_")) {
                mBucketName = mBucketDetails + "/" + "Retail" + path;
            } else if (imageName.startsWith("EXP_")) {
                mBucketName = mBucketDetails + "/" + "Expense" + path;
            } else if (imageName.startsWith("DV_")) {
                mBucketName = mBucketDetails + "/" + "Delivery" + path;
            } else if (imageName.startsWith("NP_")) {
                mBucketName = mBucketDetails + "/" + "NonProductive" + path;
            } else if (imageName.startsWith("PF_")) {
                mBucketName = mBucketDetails + "/" + "PrintFile" + path;
            } else if (imageName.startsWith("GROM_")) {
                mBucketName = mBucketDetails + "/" + "Grooming" + path;
            } else if (imageName.startsWith("PRO_")) {
                mBucketName = mBucketDetails + "/" + "Profile" + path;
            } else if (imageName.startsWith("USER_")) {
                mBucketName = mBucketDetails + "/" + "User" + path;
            } else if (imageName.startsWith("SR_SGN_")) {
                mBucketName = mBucketDetails + "/" + "SalesReturn" + path;
            } else if (imageName.startsWith("ORD_")) {
                mBucketName = mBucketDetails + "/" + "Order" + path;
            } else if (imageName.startsWith("TSK_")) {
                mBucketName = mBucketDetails + "/" + "Task" + path;
            } else if (imageName.startsWith("SUR_SGN_")) {
                mBucketName = mBucketDetails + "/" + "Survey" + path;
            } else if (imageName.startsWith("ASR_")) {
                mBucketName = mBucketDetails + "/" + "AssetServiceRequest" + path;
            } else {
                if (configurationMasterHelper.IS_PHOTO_CAPTURE_IMG_PATH_CHANGE) {
                    mBucketName = mBucketDetails + "/" + "PhotoCapture" + path;
                } else {

                    mBucketName = mBucketDetails
                            + "/"
                            + userMasterHelper.getUserMasterBO
                            ().getDistributorid()
                            + "/"
                            + userMasterHelper.getUserMasterBO().getUserid()
                            + "/"
                            + userMasterHelper.getUserMasterBO
                            ().getDownloadDate()
                            .replace("/", "");
                }
            }
            final TransferObserver myUpload = tm.upload(mBucketName,
                    imageName, image);

            myUpload.setTransferListener(new TransferListener() {
                @Override
                public void onStateChanged(int i, TransferState
                        transferState) {
                    if (transferState == TransferState.COMPLETED) {
                        successCount = successCount + 1;
                        if (successCount == uploadFileCount) {
                            fileDeleteAfterUpload();
                            myUpload.cleanTransferListener();
                            sentMessageToHandler
                                    (DataMembers.NOTIFY_WEB_UPLOAD_SUCCESS,
                                            "Images uploaded Successfully",
                                            mHandler);

                        }
                    } else if (transferState == TransferState.FAILED) {
                        myUpload.cleanTransferListener();
                        if (!isErrorOccured) {
                            isErrorOccured = true;
                            sentMessageToHandler
                                    (DataMembers.NOTIFY_WEB_UPLOAD_ERROR,
                                            "Image Upload Failed!", mHandler);
                        }
                    } else if (transferState == TransferState.CANCELED) {
                        myUpload.cleanTransferListener();
                        if (!isErrorOccured) {
                            isErrorOccured = true;
                            sentMessageToHandler
                                    (DataMembers.NOTIFY_WEB_UPLOAD_ERROR,
                                            "Image Upload Canceled!", mHandler);
                        }
                    }
                    if ((successCount == uploadFileCount) || isErrorOccured) {
                        String status = SynchronizationHelper.SYNC_STATUS_COMPLETED;
                        if (isErrorOccured) {
                            if (successCount == 0)
                                status = SynchronizationHelper.SYNC_STATUS_FAILED;
                            else if (successCount > 0)
                                status = SynchronizationHelper.SYNC_STATUS_PARTIAL;
                        } else
                            status = SynchronizationHelper.SYNC_STATUS_COMPLETED;
                        synchronizationHelper.insertSyncHeader(start_time, DateTimeUtils.now(DateTimeUtils.DATE_TIME_NEW), SynchronizationHelper.SYNC_TYPE_DGT_UPLOAD,
                                successCount, status, uploadFileCount);
                        if (successCount == uploadFileCount)
                            successCount = 0;
                    }
                }

                @Override
                public void onProgressChanged(int i, long l, long l1) {

                }

                @Override
                public void onError(int i, Exception e) {
                    myUpload.cleanTransferListener();
                    if (!isErrorOccured) {
                        isErrorOccured = true;
                        sentMessageToHandler
                                (DataMembers.NOTIFY_WEB_UPLOAD_ERROR,
                                        "Image Upload Failed!", mHandler);

                    }
                }
            });


        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private void fileDeleteAfterUpload() {
        if (sfFiles != null && sfFiles.length > 0) {
            for (int i = 0; i < sfFiles.length; i++) {
                File deleteFile = new File(folder, "/" + sfFiles[i].getName());
                deleteFile.delete();
            }
        }
    }

    public void uploadFileInAmazon(Handler mhandler) {
        try {
            successCount = 0;
            isErrorOccured = false;
            this.handler = mhandler;
            mExportFileNames = new ArrayList<String>();
            //List value order should not be changed
            mExportFileNames.add("app_log.log");
            mExportFileNames.add("all_log.log");
            mExportFileNames.add(DataMembers.DB_NAME);
            mExportFileNames.add("MemoryDetails.txt");
            mExportFileNames.add("Data_Download_Save.xls");
            // int mSucessCount=0;
            AWSConnectionHelper.getInstance().setAmazonS3Credentials(getApplicationContext());
            TransferUtility tm = new TransferUtility(AWSConnectionHelper.getInstance().getS3Connection(), getApplicationContext
                    ());

            if (synchronizationHelper.isExternalStorageAvailable()) {

                mExportFileLocation = getExternalFilesDir
                        (Environment.DIRECTORY_DOWNLOADS)
                        + "/cpg/";

                File folder;
                folder = new File(mExportFileLocation);
                if (!folder.exists()) {
                    folder.mkdir();
                }

                File SDPath = new File(mExportFileLocation);
                if (!SDPath.exists()) {
                    SDPath.mkdir();
                }

                AWSConnectionHelper.getInstance().setAmazonS3Credentials(getApplicationContext());

                for (int i = 0; i < mExportFileNames.size(); i++) {
                    Commons.print("UploadFileInAmazon," + mExportFileNames.get(i));
                    File mLogFile = new File(mExportFileLocation +
                            mExportFileNames.get(i));
                    mLogFile.createNewFile();

                    if (i == 0) {
                        String cmd = "logcat -d -v time -f" +
                                mLogFile.getAbsolutePath();
                        Runtime.getRuntime().exec(cmd).waitFor();
                    } else if (i == 1) {
                        String cmd = "logcat -d -f" + mLogFile.getAbsolutePath
                                ();
                        Runtime.getRuntime().exec(cmd).waitFor();
                    } else if (i == 2) {
                        File currentDB = new File(this.getDatabasePath(DataMembers.DB_NAME).getPath());
                        InputStream input = new FileInputStream(currentDB);
                        byte dataa[] = new byte[input.available()];
                        input.read(dataa);

                        OutputStream out = new FileOutputStream
                                (mExportFileLocation + "/"
                                        + mExportFileNames.get(i));
                        out.write(dataa);
                        out.flush();

                        out.close();
                        input.close();
                    } else if (i == 3) {

                        StringBuilder sb = new StringBuilder();

                        final Runtime runtime = Runtime.getRuntime();
                        final long usedMemInMB = (runtime.totalMemory() -
                                runtime.freeMemory()) / 1048576L;
                        final long maxHeapSizeInMB = runtime.maxMemory() /
                                1048576L;

                        sb.append("UsedMem: " + usedMemInMB + "MB" + "  MaxHeapSize:" + maxHeapSizeInMB + "MB");
                        sb.append('\n');
                        sb.append('\n');

                        PackageManager packageManager = getPackageManager();
                        List<ApplicationInfo> list =
                                packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
                        int listSize = list.size();

                        for (int j = 0; j < listSize; j++) {
                            sb.append(list.get(j).packageName);
                            sb.append('\n');
                        }

                        FileOutputStream outputStream = new FileOutputStream
                                (mExportFileLocation + "/" + mExportFileNames.get(i));
                        outputStream.write(sb.toString().getBytes());
                        outputStream.close();
                    } else if (i == 4) {
                        File currentFile = new File(mExportFileLocation + "/" + mExportFileNames.get(i));
                        InputStream input = new FileInputStream(currentFile);
                        byte dataa[] = new byte[input.available()];
                        input.read(dataa);
                        OutputStream out = new FileOutputStream
                                (mExportFileLocation + "/"
                                        + mExportFileNames.get(i));
                        out.write(dataa);
                        out.flush();
                        out.close();
                        input.close();
                    }

                    uploadFileAtAmazonCloud(mExportFileLocation,
                            mExportFileNames.get(i), tm);
                }
            }
            AWSConnectionHelper.getInstance().getS3Connection().shutdown();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public void uploadFileAtAmazonCloud(final String mFileLocation, String
            mFileName, TransferUtility tm) {
        try {

            final File mFile = new File(mFileLocation, "/" + mFileName);

            String mBucketName;
            String mBucketDetails = DataMembers.S3_BUCKET + "/"
                    + DataMembers.S3_ROOT_DIRECTORY;

            String path = "/"
                    + userMasterHelper.getUserMasterBO().getDownloadDate()
                    .replace("/", "") + "/"
                    + userMasterHelper.getUserMasterBO().getUserid();


            mBucketName = mBucketDetails + "/" + "BackUp" + path;
            final TransferObserver myUpload = tm.upload(mBucketName, mFileName,
                    mFile);
            myUpload.setTransferListener(new TransferListener() {
                @Override
                public void onStateChanged(int i, TransferState transferState) {
                    Commons.print("ammz," + transferState + "");
                    if (transferState == TransferState.COMPLETED) {
                        successCount++;
                        if (successCount >= mExportFileNames.size()) {
                            deleteExportedFiles(mFileLocation);
                            myUpload.cleanTransferListener();
                            sentMessageToHandler
                                    (DataMembers.NOTIFY_FILE_UPLOADED__COMPLETED_IN_AMAZON,
                                            "File uploaded Successfully", handler);
                        }

                    } else if (transferState == TransferState.FAILED) {
                        if (!isErrorOccured) {
                            isErrorOccured = true;
                            myUpload.cleanTransferListener();
                            sentMessageToHandler
                                    (DataMembers.NOTIFY_FILE_UPLOADED_FAILED_IN_AMAZON,
                                            "File uploaded Failed", handler);
                        }
                    } else if (transferState == TransferState.CANCELED) {
                        if (!isErrorOccured) {
                            isErrorOccured = true;
                            myUpload.cleanTransferListener();
                            sentMessageToHandler
                                    (DataMembers.NOTIFY_FILE_UPLOADED_FAILED_IN_AMAZON,
                                            "File uploaded Canceled", handler);
                        }
                    }
                }

                @Override
                public void onProgressChanged(int i, long l, long l1) {

                }

                @Override
                public void onError(int i, Exception e) {
                    if (!isErrorOccured) {
                        isErrorOccured = true;
                        myUpload.cleanTransferListener();
                        sentMessageToHandler
                                (DataMembers.NOTIFY_FILE_UPLOADED_FAILED_IN_AMAZON,
                                        "File uploaded Failed", handler);
                    }
                }
            });

            // success

            // tm.shutdownNow();

        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    private void deleteExportedFiles(String location) {
        File folder = new File(location);
        File[] files = folder.listFiles();
        if (files != null && files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                File deleteFile = new File(folder, "/" + files[i].getName());
                deleteFile.delete();
            }
        }
    }

    public String checkOTP(String mRetailerId, String mOTP, String activityType) {

        String downloadReponse = "";
        try {
            System.gc();
            downloadReponse = "0";

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("UserId", userMasterHelper.getUserMasterBO()
                    .getUserid());
            jsonObj.put("RetailerId", mRetailerId);
            jsonObj.put("MobileDateTime", Utils.getDate("yyyy/MM/dd HH:mm:ss"));
            jsonObj.put("MobileUTCDateTime",
                    Utils.getGMTDateTime("yyyy/MM/dd HH:mm:ss"));
            jsonObj.put("OTPValue", mOTP);
            jsonObj.put("ActivityType", activityType);
            jsonObj.put("VersionCode", getApplicationVersionNumber());
            jsonObj.put(SynchronizationHelper.VERSION_NAME, getApplicationVersionName());
            String appendUrl = synchronizationHelper.getUploadUrl("SYNOTP");
            appendUrl = appendUrl + "?userinfo=";
            Vector<String> responseVector = synchronizationHelper
                    .getUploadResponse(jsonObj.toString(), null, appendUrl);

            if (responseVector.size() > 0) {


                for (String s : responseVector) {
                    JSONObject jsonObject = new JSONObject(s);

                    Iterator itr = jsonObject.keys();
                    while (itr.hasNext()) {
                        String key = (String) itr.next();
                        if (key.equals("Response")) {
                            downloadReponse = jsonObject.getInt("Response") + "";

                        } else if (key.equals("ErrorCode")) {
                            String tokenResponse = jsonObject.getString("ErrorCode");
                            if (tokenResponse.equals(SynchronizationHelper.INVALID_TOKEN)
                                    || tokenResponse.equals(SynchronizationHelper.TOKEN_MISSINIG)
                                    || tokenResponse.equals(SynchronizationHelper.EXPIRY_TOKEN_CODE)) {

                                return -5 + "";

                            }

                        }

                    }


                }
            } else {
                if (!synchronizationHelper.getAuthErroCode().equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
                    String errorMsg = synchronizationHelper.getErrormessageByErrorCode().get(synchronizationHelper.getAuthErroCode());
                    if (errorMsg != null) {
                        Toast.makeText(ctx, errorMsg, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ctx, getResources().getString(R.string.data_not_downloaded), Toast.LENGTH_SHORT).show();
                    }
                }
            }



           /* for (String res : responseVector) {
                JSONObject responseObject = new JSONObject(res);
                downloadReponse = responseObject.getInt("Response") + "";

            }*/

        } catch (Exception e) {
            Commons.printException("Exception", e);
            return "0";
        }

        return downloadReponse;
    }


    /**
     * @See {@link  com.ivy.utils.AppUtils;}
     * @since CPG131 replaced by {@link FileUtils#isExternalStorageAvailable}
     * Will be removed from @version CPG133 Release
     * @deprecated This has been Migrated to MVP pattern
     */
    public boolean isExternalStorageAvailable() {

        StatFs stat = new StatFs(Environment.getExternalStorageDirectory()
                .getPath());
        double sdAvailSize = (double) stat.getAvailableBlocks()
                * (double) stat.getBlockSize();
        // One binary gigabyte equals 1,073,741,824 bytes.
        double mbAvailable = sdAvailSize / 1048576;

        String state = Environment.getExternalStorageState();
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Something else is wrong. It may be one of many other states, but
            // all we need
            // to know is we can neither read nor write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }

        if (mExternalStorageAvailable == true
                && mExternalStorageWriteable == true && mbAvailable > 10) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * It returns true if the folder contains the n or more than n files
     * which starts name fnameStarts otherwiese returns false;
     *
     * @param folderPath
     * @param n
     * @param fNameStarts
     * @return
     * @See {@link FileUtils#checkForNFilesInFolder(String, int, String)}
     * @deprecated
     */
    public boolean checkForNFilesInFolder(String folderPath, int n,
                                          String fNameStarts) {

        if (n < 1)
            return true;

        boolean b = false;

        File folder = new File(folderPath);
        if ((folder == null) || (!folder.exists())) {
            folder = null;
            return false;
        } else {
            String fnames[] = folder.list();
            if ((fnames == null) || (fnames.length < n)) {
                folder = null;
                fnames = null;
                return false;
            } else {
                int count = 0;
                int fnames_size = fnames.length;
                for (String str : fnames) {
                    if ((str != null) && !fNameStarts.equals("") && (str.length() > 0)) {
                        if (str.startsWith(fNameStarts)) {
                            count++;
                        }
                    }

                    if (count == n) {
                        fnames = null;
                        str = null;
                        return true;
                    }
                }
            }
            fnames = null;

        }
        folder = null;
        return false;
    }

    /**
     * @param folderPath
     * @param fnamesStarts
     * @See {@link FileUtils#deleteFiles(String, String)}
     * @deprecated
     */
    public void deleteFiles(String folderPath, String fnamesStarts) {
        File folder = new File(folderPath);

        File files[] = folder.listFiles();
        if ((files == null) || (files.length < 1)) {
            folder = null;
            files = null;
            return;
        } else {

            for (File tempFile : files) {
                if (tempFile != null) {
                    if (tempFile.getName().startsWith(fnamesStarts))
                        tempFile.delete();
                }
            }
        }
    }

    @Deprecated
    /**
     * This has been moved to  Dbhelper
     * @See {@link AppDataManagerImpl#saveModuleCompletion(String)}
     */
    public boolean saveModuleCompletion(String menuName, boolean isRetailerModule) {
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            String retailerId = "0";
            if (isRetailerModule)
                retailerId = getRetailerMasterBO().getRetailerID();

            Cursor c = db
                    .selectSQL("SELECT * FROM ModuleCompletionReport WHERE RetailerId="
                            + retailerId + " AND MENU_CODE = " + QT(menuName));

            if (c.getCount() == 0) {
                String columns = "Retailerid,MENU_CODE";

                String values = retailerId + ","
                        + QT(menuName);

                db.insertSQL("ModuleCompletionReport", columns, values);

            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("Saving" + menuName + "exception", e);
            return false;
        }
        return true;
    }


    public boolean deleteModuleCompletion(String menuName) {
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            Cursor c = db
                    .selectSQL("SELECT * FROM ModuleCompletionReport WHERE RetailerId="
                            + getRetailerMasterBO().getRetailerID() + " AND MENU_CODE = " + QT(menuName));

            if (c.getCount() > 0) {
                db.deleteSQL("ModuleCompletionReport", "MENU_CODE="
                        + QT(menuName), false);
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("deleting" + menuName + "exception", e);
            return false;
        }
        return true;

    }


    public void isModuleDone(boolean isRetailerBasedModule) {
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.openDataBase();
            String query = "Select MENU_CODE from ModuleCompletionReport "
                    + " where retailerid=";
            if (isRetailerBasedModule)
                query += getRetailerMasterBO().getRetailerID();
            else query += 0;


            Cursor c = db
                    .selectSQL(query);

            mModuleCompletionResult = new HashMap<String, String>();

            if (c != null) {
                while (c.moveToNext())
                    mModuleCompletionResult.put(c.getString(0), "1");
                c.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public boolean isDoubleEdit_temp() {
        return isDoubleEdit_temp;
    }

    public void setDoubleEdit_temp(boolean isDoubleEdit_temp) {
        this.isDoubleEdit_temp = isDoubleEdit_temp;
    }


    public int getTotalLines() {
        try {
            boolean isVansales;
            if (configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED) {
                if (getRetailerMasterBO().getIsVansales() == 1) {
                    isVansales = true;
                } else {
                    isVansales = false;
                }

            } else {
                if (configurationMasterHelper.IS_INVOICE) {
                    isVansales = true;
                } else {
                    isVansales = false;
                }
            }

            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c;
            if (isVansales) {
                c = db.selectSQL("select ifnull(sum(LinesPerCall),0) from invoicemaster where retailerid="
                        + StringUtils.QT(getRetailerMasterBO().getRetailerID()) + " and InvoiceDate = " + StringUtils.QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)));
            } else {
                c = db.selectSQL("select ifnull(sum(LinesPerCall),0) from orderHeader where retailerid="
                        + StringUtils.QT(getRetailerMasterBO().getRetailerID())
                        + " and upload='N' and is_vansales = 0");
            }
            if (c.getCount() > 0) {
                if (c.moveToNext()) {
                    return c.getInt(0);
                }
            }
            c.close();
            db.closeDB();
            return 0;
        } catch (Exception e) {
            Commons.printException(e);
            return 0;
        }
    }

    public ArrayList<ConfigureBO> getFITscore() {
        try {
            ArrayList<ConfigureBO> lst = new ArrayList<>();
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select  SMA.SurveyDesc,sum((AD.score*SM.weight)/100) Total from AnswerScoreDetail  AD  " +
                            "INNER JOIN AnswerHeader AH  ON AH.uid=AD.uid " +
                            "LEFT JOIN SurveyMapping SM  ON SM.surveyid=AD.surveyid " +
                            "INNER JOIN SurveyMaster SMA ON SMA.surveyid = SM.surveyid   " +
                            "and SM.qid=AD.qid where AH.retailerid="
                            + getRetailerMasterBO().getRetailerID() +
                            " and (SMA.menucode='MENU_SURVEY' OR SMA.menucode='MENU_SURVEY_SW' OR SMA.menucode='MENU_SURVEY_QDVP3')" +
                            " and AD.upload='N' group by AD.surveyId");
            if (c.getCount() > 0) {
                lst = new ArrayList<>();
                ConfigureBO bo;
                while (c.moveToNext()) {
                    bo = new ConfigureBO();
                    bo.setMenuName(c.getString(0));
                    bo.setMenuNumber(SDUtil.format(c.getDouble(1), 2, 0) + "");

                    lst.add(bo);
                }
            }
            c.close();
            db.closeDB();
            return lst;
        } catch (Exception e) {
            Commons.printException(e);
            return null;
        }
    }

    public ArrayList<ConfigureBO> getGroupWiseFITScore() {
        try {
            ArrayList<ConfigureBO> lst = new ArrayList<>();
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select SM.groupName,sum((AD.score*SM.weight)/100) Total from AnswerScoreDetail AD"
                            + " INNER JOIN AnswerHeader AH ON AH.uid=AD.uid"
                            + "  INNER JOIN SurveyMapping SM  ON SM.surveyid=AD.surveyid and SM.qid=AD.qid where AH.retailerid="
                            + getRetailerMasterBO().getRetailerID()
                            + " and AH.menuCode in('MENU_SURVEY','MENU_SURVEY_SW','MENU_SURVEY_QDVP3') and AD.upload='N' group by SM.groupName");
            if (c.getCount() > 0) {
                lst = new ArrayList<>();
                ConfigureBO bo;
                while (c.moveToNext()) {

                    bo = new ConfigureBO();
                    bo.setMenuName(c.getString(0));
                    bo.setMenuNumber(SDUtil.format(c.getDouble(1), 2, 0) + "");

                    if (bo.getMenuName() != null && bo.getMenuNumber() != null)
                        lst.add(bo);


                }
            }
            c.close();
            db.closeDB();
            return lst;
        } catch (Exception e) {
            Commons.printException(e);
            return null;
        }

    }

    public void updateCurrentFITscore(DBUtil db) {
        try {
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select  AH.retailerid,sum((AD.score*SM.weight)/100) Total from AnswerScoreDetail AD " +
                            "INNER JOIN AnswerHeader AH  ON AH.uid=AD.uid " +
                            "LEFT JOIN SurveyMapping SM  ON SM.surveyid=AD.surveyid " +
                            "INNER JOIN SurveyMaster SMA ON SMA.surveyid = SM.surveyid   and " +
                            "SM.qid=AD.qid where (SMA.menucode='MENU_SURVEY' OR SMA.menucode='MENU_SURVEY_SW') and AD.upload='N' group by AH.retailerid");
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    for (RetailerMasterBO bo : retailerMaster) {
                        if (bo.getRetailerID().equals(c.getString(0))) {
                            bo.setCurrentFitScore(c.getDouble(1));
                        }
                    }
                }
            }
            c.close();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public void updateCurrentFITscore(RetailerMasterBO bo) {
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.openDataBase();
            int count = 0;
            Cursor c = db
                    .selectSQL("select sum((AD.score*SM.weight)/100) Total from AnswerDetail AD " +
                            "LEFT JOIN SurveyMapping SM  ON SM.surveyid=AD.surveyid " +
                            "INNER JOIN SurveyMaster SMA ON SMA.surveyid = SM.surveyid  and SM.qid=AD.qid " +
                            "where AD.retailerid=" + bo.getRetailerID() +
                            " and (SMA.menucode='MENU_SURVEY' OR SMA.menucode='MENU_SURVEY_SW') and AD.upload='N' group by AD.retailerid");
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    bo.setCurrentFitScore(c.getDouble(0));
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }


    void updatePoRemarks() {
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            if (getOrderHeaderNote() != null
                    && getOrderHeaderNote().length() > 0) {
                db.updateSQL("UPDATE " + DataMembers.tbl_orderHeader
                        + " SET remark =" + QT(getOrderHeaderNote())
                        + " WHERE " + " OrderID = (SELECT OrderID FROM  "
                        + DataMembers.tbl_orderHeader + " where "
                        + " RetailerID = "
                        + getRetailerMasterBO().getRetailerID() + ")");
                setOrderHeaderNote("");
                getOrderHeaderBO().setRemark("");
            }
            if (getRField1() != null
                    && getRField1().length() > 0) {
                db.updateSQL("UPDATE " + DataMembers.tbl_orderHeader
                        + " SET RField1 =" + QT(getRField1())
                        + " WHERE " + " OrderID = (SELECT OrderID FROM  "
                        + DataMembers.tbl_orderHeader + " where "
                        + " RetailerID = "
                        + getRetailerMasterBO().getRetailerID() + ")");
                setRField1("");
                getOrderHeaderBO().setRField1("");
            }
            if (getRField2() != null
                    && getRField2().length() > 0) {
                db.updateSQL("UPDATE " + DataMembers.tbl_orderHeader
                        + " SET RField2 =" + QT(getRField2())
                        + " WHERE " + " OrderID = (SELECT OrderID FROM  "
                        + DataMembers.tbl_orderHeader + " where "
                        + " RetailerID = "
                        + getRetailerMasterBO().getRetailerID() + ")");
                setRField2("");
                getOrderHeaderBO().setRField2("");
            }
            if (orderHeaderBO.getPO() != null
                    && orderHeaderBO.getPO().length() > 0) {
                db.updateSQL("UPDATE " + DataMembers.tbl_orderHeader
                        + " SET po =" + QT(orderHeaderBO.getPO()) + " WHERE "
                        + " OrderID = (SELECT OrderID FROM  "
                        + DataMembers.tbl_orderHeader + " where "
                        + " RetailerID = "
                        + getRetailerMasterBO().getRetailerID() + ")");
                orderHeaderBO.setPO("");
            }
            if (orderHeaderBO.getDeliveryDate() != null
                    && orderHeaderBO.getDeliveryDate().length() > 0) {
                db.updateSQL("UPDATE " + DataMembers.tbl_orderHeader
                        + " SET deliveryDate ="
                        + QT(orderHeaderBO.getDeliveryDate()) + " WHERE "
                        + " OrderID = (SELECT OrderID FROM  "
                        + DataMembers.tbl_orderHeader + " where "
                        + " RetailerID = "
                        + getRetailerMasterBO().getRetailerID() + ")");
                orderHeaderBO.setDeliveryDate("");
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }


    /**
     * download retailer wise seller type
     */

    public void getRetailerWiseSellerType() {
        try {
            DBUtil db = new DBUtil(this, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            retailerMasterBO.setIsVansales(0);
            String query = "select is_vansales from retailermasterinfo where retailerid="
                    + QT(retailerMasterBO.getRetailerID());
            Cursor c = db.selectSQL(query);
            if (c.getCount() > 0) {
                if (c.moveToNext()) {
                    int flag = c.getInt(0);
                    retailerMasterBO.setIsVansales(flag);
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }


    /**
     * download supplier list for select primary or secondary
     *
     * @return
     */
    public ArrayList<SupplierMasterBO> downloadSupplierDetails() {
        ArrayList<SupplierMasterBO> mSupplierList = new ArrayList<SupplierMasterBO>();
        DBUtil db = null;
        SupplierMasterBO supplierMasterBO;
        try {
            db = new DBUtil(ctx, DataMembers.DB_NAME);
            db.createDataBase();
            db.openDataBase();
            StringBuffer sb = new StringBuffer();
            if (configurationMasterHelper.IS_SUPPLIER_NOT_AVAILABLE) {
                sb.append("select did,dname,type,0,parentid from DistributorMaster ");

            } else {
                sb.append("select SM.sid,SM.sname,SM.stype,SM.isPrimary,SM.parentid," +
                        "SM.creditlimit,SM.supplierTaxLocId,SM.IsCompositionScheme,IFNULL(SLM.ListCode,'') as RpTypeCode " +
                        "from Suppliermaster SM ");
                sb.append("LEFT JOIN StandardListMaster SLM ON SLM.ListId=SM.RpTypeId ");
                sb.append("where rid=");
                sb.append(StringUtils.QT(retailerMasterBO.getRetailerID()));
                sb.append(" or SM.rid= 0 order by SM.isPrimary desc");
            }
            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    supplierMasterBO = new SupplierMasterBO();
                    supplierMasterBO.setSupplierID(c.getInt(0));
                    supplierMasterBO.setSupplierName(c.getString(1));
                    try {
                        supplierMasterBO.setSupplierType(c.getInt(2));
                    } catch (Exception e) {
                        supplierMasterBO.setSupplierType(0);
                    }
                    supplierMasterBO.setIsPrimary(c.getInt(3));
                    supplierMasterBO.setDistParentID(c.getInt(4));

                    if (c.getColumnCount() == 8) {
                        supplierMasterBO.setCreditLimit(c.getFloat(5));
                        supplierMasterBO.setSupplierTaxLocId(c.getInt(6));

                        if (c.getInt(7) == 1)
                            supplierMasterBO.setCompositeRetailer(true);
                        else supplierMasterBO.setCompositeRetailer(false);

                        supplierMasterBO.setRpTypeCode(c.getString(8));
                    }

                    mSupplierList.add(supplierMasterBO);
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            db.closeDB();
            Commons.printException("" + e);
            return new ArrayList<SupplierMasterBO>();
        }

        return mSupplierList;
    }

    /**
     * Method to get select supplier position
     *
     * @return
     */
    public int getSupplierPosition(ArrayList<SupplierMasterBO> mSupplierList) {
        int count = 0;
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select supplierid from RetailerMasterInfo where retailerid="
                            + QT(retailerMasterBO.getRetailerID()));
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    int sid = c.getInt(0);

                    if (sid > 0) {
                        for (SupplierMasterBO supplierBO : mSupplierList) {
                            if (sid == supplierBO.getSupplierID()) {
                                c.close();
                                db.closeDB();
                                return count;
                            } else {
                                count++;
                            }
                        }

                    }
                }

            }
            c.close();
            db.closeDB();
            return 0;
        } catch (Exception e) {
            Commons.print(e.getMessage());
            return 0;
        }
    }

    /**
     * Update the Bottle Return amount from Retailer Master Bottle Return Credit
     * limit
     */
    void updateBottleCreditLimitAmount() {
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            db.updateSQL("UPDATE "
                    + DataMembers.tbl_retailerMaster
                    + " SET RetCreditLimit = "
                    + (getRetailerMasterBO().getBottle_creditLimit() - getOrderHeaderBO()
                    .getRemainigValue()) + " WHERE RetailerID = "
                    + getRetailerMasterBO().getRetailerID());
            // Update the lastest amount in RetailerMasterBo, then only reduce
            // the amount for next order within the Screen
            getRetailerMasterBO().setBottle_creditLimit(
                    getRetailerMasterBO().getBottle_creditLimit()
                            - getOrderHeaderBO().getRemainigValue());
            getRetailerMasterBO().setProfile_creditLimit("" + getRetailerMasterBO().getBottle_creditLimit());
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public int getPreSbdAchieved() {
        int achieved = 0;
        try {
            achieved = 0;
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            Cursor c = null;

            c = db.selectSQL("select count(distinct gName) from SbdDistributionAchievedMaster where rid ="
                    + retailerMasterBO.getRetailerID());
            if (c != null) {
                while (c.moveToNext()) {
                    achieved = c.getInt(0);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return achieved;
    }

    public Vector<LocationBO> downloadLocationMaster() {
        try {
            locvect = new Vector<LocationBO>();
            LocationBO locbo;
            DBUtil db = null;
            db = new DBUtil(this, DataMembers.DB_NAME);
            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("select LocId,LocName from LocationMaster ");
            sb.append("where LocLevelid=(SELECT  id FROM LocationLevel ORDER BY sequence DESC LIMIT 1)");
            Cursor c = db.selectSQL(sb.toString());
            if (c != null) {
                while (c.moveToNext()) {
                    locbo = new LocationBO();
                    locbo.setLocId(c.getInt(0));
                    locbo.setLocName(c.getString(1));
                    locvect.add(locbo);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return locvect;
    }

    public void parseJSONAndInsert(JSONObject json) throws JSONException,
            Exception {

        String TAG = "parseJSONAndInsert";

        // Open Database to store the records.
        DBUtil mDBAdapter = new DBUtil(ctx, DataMembers.DB_NAME
        );
        mDBAdapter.openDataBase();

        // Get all the Keys from json object, keys are considered as table
        // names.
        Iterator tableNamesIterator = json.keys();

        while (tableNamesIterator.hasNext()) {

            // Get First Table Name
            String tableName = (String) tableNamesIterator.next();
            Commons.printInformation("Table name: " + tableName);

            // Get First table rows array
            JSONArray tableRows = json.getJSONArray(tableName);
            Commons.printInformation("Table Size: " + tableRows.length());

            StringBuilder queryString = new StringBuilder();

            int recCount = 0;
            for (int i = 0; i < tableRows.length(); i++) {
                JSONObject aTableRowObject = tableRows.getJSONObject(i);
                Iterator columNamesIterator = aTableRowObject.keys();
                recCount = recCount + 1;
                int count = 0;

                StringBuilder colNames = new StringBuilder();
                StringBuilder colValues = new StringBuilder();

                while (columNamesIterator.hasNext()) {
                    String columnName = (String) columNamesIterator.next();
                    String value = aTableRowObject.getString(columnName);
                    Commons.print("value" + value);
                    if (count != 0) {
                        colNames.append(",").append(columnName);
                        colValues.append(",").append(
                                DatabaseUtils.sqlEscapeString(value));
                    } else {
                        colNames.append(columnName);
                        colValues.append(DatabaseUtils.sqlEscapeString(value));
                    }

                    count++;
                }

                // Form query and append in StringBuilder
                if (queryString.length() == 0) {
                    queryString.append("INSERT INTO ")
                            .append(tableName.toString())
                            .append(" ( " + colNames.toString() + " ) ")
                            .append("SELECT ").append(colValues.toString());
                } else {
                    queryString.append(" UNION ALL SELECT ").append(
                            colValues.toString());
                }

                // If record count reaches 400 , fire the query immediately.
                if (recCount == 400) {
                    if (colValues.toString() != null) {
                        mDBAdapter.multiInsert(queryString.toString());

                    }
                    queryString = new StringBuilder();
                    recCount = 0;
                }
            }// End Of Loop - Table records

            // Store the data once loop finished the execution.
            if (queryString.length() > 0)
                mDBAdapter.multiInsert(queryString.toString());

        }

        mDBAdapter.closeDB();
    }


    public void saveNotification(String msg, String url, String type) {
        try {
            String values;
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            String columns = "Message, Imageurl, TimeStamp, Type";

            values = QT(msg) + "," + (url) + "," + QT(DateTimeUtils.now(DateTimeUtils.DATE_TIME)) + "," + QT(type);

            db.insertSQL("Notification", columns, values);

            Commons.print("," + "");
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public void downloadWeekDay() {
        slist = new Vector<StandardListBO>();
        StandardListBO slbo;

        DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME);
        db.openDataBase();
        slbo = new StandardListBO();
        slbo.setListName(getResources()
                .getString(R.string.all));
        slist.add(slbo);
        Cursor c = db
                .selectSQL("select ListId,ListCode,ListName from StandardListMaster where ListType='WEEKDAY_TYPE'");
        if (c != null) {
            while (c.moveToNext()) {
                slbo = new StandardListBO();
                slbo.setListID(c.getString(0));
                slbo.setListCode(c.getString(1));
                slbo.setListName(c.getString(2));
                slist.add(slbo);
            }

        }

    }

    public int getTodaysVisitCount(RetailerMasterBO rid) {
        DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME);
        db.openDataBase();

        Cursor c = db
                .selectSQL("select RetailerID from OutletTimestamp where RetailerID="
                        + rid.getRetailerID()
                        + " and VisitDate="
                        + this.QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)));
        if (c.getCount() > 0)
            while (c.moveToNext()) {
                return 1;
            }
        c.close();
        db.closeDB();
        return 0;
    }

    public AlertDialog applyAlertDialogTheme(AlertDialog.Builder builder) {
        TypedArray typearr = getContext().getTheme().obtainStyledAttributes(R.styleable.MyTextView);

        AlertDialog dialog = builder.show();
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = (int) (metrics.widthPixels * 0.80);
        dialog.getWindow().setLayout(screenWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
        int alertTitleId = getResources().getIdentifier("alertTitle", "id", "android");
        TextView alertTitle = (TextView) dialog.getWindow().getDecorView().findViewById(alertTitleId);
        alertTitle.setTextColor(typearr.getColor(R.styleable.MyTextView_primarycolor, 0)); // change title text color
//        alertTitle.setTypeface(configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
//        TextViewCompat.setTextAppearance(alertTitle, typearr.getResourceId(R.styleable.MyTextView_textTitleStyle, 0));
//        alertTitle.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        Button negativeBtn = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        negativeBtn.setTypeface(configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        negativeBtn.setTextColor(typearr.getColor(R.styleable.MyTextView_accentcolor, 0)); // change button text color

        Button positiveBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positiveBtn.setTypeface(configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        positiveBtn.setTextColor(typearr.getColor(R.styleable.MyTextView_accentcolor, 0)); // change button text color

        Button neutralBtn = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
        neutralBtn.setTypeface(configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        neutralBtn.setTextColor(typearr.getColor(R.styleable.MyTextView_accentcolor, 0)); // change button text color

        // Set title divider color
        int titleDividerId = getResources().getIdentifier("titleDivider", "id", "android");
        View titleDivider = dialog.findViewById(titleDividerId);
        if (titleDivider != null)
            titleDivider.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_primarycolor, 0));

        return dialog;
       /* ((Button) dialog.getWindow().getDecorView().findViewById(android.R.id.button1)).setBackgroundResource(R.drawable.tab_selection);
        ((Button) dialog.getWindow().getDecorView().findViewById(android.R.id.button2)).setBackgroundResource(R.drawable.tab_selection);
        ((Button) dialog.getWindow().getDecorView().findViewById(android.R.id.button3)).setBackgroundResource(R.drawable.tab_selection);*/
    }

    /**
     * @See {@link RetailerDataManagerImpl#fetchRetailers()}
     * @deprecated Handled inside the fetchAllRetailers() method itself
     */
    public void setWeeknoFoNewRetailer() {
        for (RetailerMasterBO retailer : getRetailerMaster()) {
            if (retailer.getIsNew().equalsIgnoreCase("Y")) {
                retailer.setWeekNo(retailer.getVisitday() + "");
                Commons.print("visitdays" + retailer.getVisitday());
            }
        }
    }

    public String getStandardListCode(int listid) {
        String listCode = "";
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select ListCode from StandardListMaster where ListId="
                            + listid);
            if (c != null) {
                if (c.moveToNext()) {
                    listCode = c.getString(0);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {

            Commons.printException(e);
        }
        return listCode;
    }

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }


    //
    public String getUserSession() {
        String session = "";
        boolean fn = false, an = false, fd = false;
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.openDataBase();
            Calendar calendar = Calendar.getInstance();
            Cursor c = db
                    .selectSQL("select distinct SL.ListCode from AttendanceDetail AD INNER JOIN StandardListMaster SL ON SL.Listid=AD.session where AD.upload='X' and " + QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) + " between AD.fromdate and AD.todate");
            if (c != null) {
                while (c.moveToNext()) {
                    session = c.getString(0);
                    if (session.equalsIgnoreCase("AN")) {
                        an = true;

                    } else if (session.equalsIgnoreCase("FN")) {
                        fn = true;

                    } else if (session.equalsIgnoreCase("FD")) {
                        fd = true;

                    }

                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        if (fd) {
            return "FD";
        } else if (an && fn) {
            return "FD";
        }
        return session;
    }

    //
    protected static final int REQUEST_CHECK_SETTINGS = 1000;
    GoogleApiClient googleApiClient;
    private static final int UPDATE_INTERVAL = 1000 * 2;
    private static final int FASTEST_INTERVAL = 1000;

    /**
     * @see {@link com.ivy.core.base.view.BaseActivity#requestLocation(Activity)}
     * @deprecated
     */
    public void requestLocation(final Activity ctxt) {

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API).build();
        }
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {

                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            status.startResolutionForResult(
                                    ctxt, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }


    // Chat
    String chatRegId, chatUserName, ChatPassword;

    public void downloadChatCredentials() {
        try {

            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select chatregid,chatUserName,chatUserPwd from AppVariables");
            if (c != null) {
                if (c.moveToNext()) {
                    chatRegId = c.getString(0);
                    chatUserName = c.getString(1);
                    ChatPassword = c.getString(2);
                }
                c.close();
            }
            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
        }


    }

    public String getChatRegId() {
        return chatRegId;
    }


    public String getChatUserName() {
        return chatUserName;
    }


    public String getChatPassword() {
        return ChatPassword;
    }


    public void updatePrintCount(int count) {
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.openDataBase();
            db.updateSQL("update InvoiceMaster set print_count=" + count + " where invoiceNo = '" + this.invoiceNumber + "'");

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }


    private int counterId = 0;

    public int getCounterId() {
        return counterId;
    }

    public String getCounterRetailerId() {
        return counterRetailerId;
    }


    private String counterRetailerId = "0";


    public void useNetworkProvidedValues() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                int i = android.provider.Settings.Global.getInt(
                        getContentResolver(),
                        android.provider.Settings.Global.AUTO_TIME);
                if (i == 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ctx)
                            .setIcon(null)
                            .setTitle(getResources().getString(R.string.enable_auto_date_time))
                            .setCancelable(false)
                            .setPositiveButton(getResources().getString(R.string.ok),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int whichButton) {
                                            Intent intent = new Intent(android.provider.Settings.ACTION_DATE_SETTINGS);

                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);

                                        }
                                    });
                    applyAlertDialogTheme(builder);

                }
            } else {
                int i = android.provider.Settings.System.getInt(
                        getContentResolver(),
                        android.provider.Settings.System.AUTO_TIME);
                if (i == 0) {
                    android.provider.Settings.System.putInt(getContentResolver(),
                            android.provider.Settings.System.AUTO_TIME, 1);
                }
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    /**
     * @See {@link com.ivy.utils.AppUtils}
     * @since CPG131 replaced by {@link com.ivy.utils.AppUtils#validateInput}
     * Will be removed from @version CPG133 Release
     * @deprecated This has been Migrated to MVP pattern
     */
    public String validateInput(String input) {
        String str = "";
        if (input != null && input != "") {
            str = Html.fromHtml(input).toString();
        }
        return str;
    }

    public String getDay(String mdate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/M/dd");
            Date date = sdf.parse(mdate);
            SimpleDateFormat sdf1 = new SimpleDateFormat("EEEE");
            switch (sdf1.format(date)) {
                case "Monday":
                    return getResources().getString(R.string.Monday);
                case "Tuesday":
                    return getResources().getString(R.string.Tuesday);
                case "Wednesday":
                    return getResources().getString(R.string.Wednesday);
                case "Thursday":
                    return getResources().getString(R.string.Thursday);
                case "Friday":
                    return getResources().getString(R.string.Friday);
                case "Saturday":
                    return getResources().getString(R.string.Saturday);
                case "Sunday":
                    return getResources().getString(R.string.Sunday);
                default:
                    return sdf1.format(date);
            }
        } catch (Exception ex) {
            return "";
        }

    }

    public void insertSeqNumber(String type) {
        Cursor cursor = null;
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.openDataBase();
            cursor = db
                    .selectSQL("SELECT COUNT(SEQNO) FROM TransactionSequence WHERE TypeId IN (SELECT ListID FROM StandardListMaster WHERE LISTCODE =" + QT(type) + ")");
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    int count = cursor.getInt(0);
                    if (count == 0) {
                        insertSequenceNo(type);
                    }
                }
                cursor.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    /**
     * Insert the Record in Transaction Sequence Table
     *
     * @param type the count
     */
    private void insertSequenceNo(String type) {
        Cursor cursor = null;
        String columns = "TypeID,SeqNo";
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            cursor = db
                    .selectSQL("SELECT ListId FROM StandardListMaster WHERE LISTCODE =" + QT(type));
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String values = cursor.getInt(0) + "," + 0;
                    db.insertSQL(DataMembers.tbl_TransactionSequence, columns, values);
                }
                cursor.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    /**
     * Download and create SalesReturnId or CreditNoteId Number
     *
     * @return DistributorId+UserID+Downloaded Date+Transaction Sequence Number
     * DistributorID (Max.4 digits),UserID(Max.5 digits Downloaded
     * date(Max.8 digits, Transaction sequence number(Max.4 digits) If
     * number digits lesser means Zero will append
     */
    public String downloadSequenceNo(String type) {
        StringBuilder mComputeID = new StringBuilder();
        long seqNo = 0L;
        Cursor cursor = null;
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            // Download the Invoice ID Creating Rules, According to that Invoice
            // Id generated
            ArrayList<String> mRules = downloadTransactionRules(type);
            if (mRules != null && mRules.size() > 0) {
                for (int i = 0; i < mRules.size(); i++) {

                    // DistributorID
                    if (mRules.get(i).contains("{DIST_ID")) {
                        seqNo = 0L;
                        seqNo = userMasterHelper
                                .getUserMasterBO().getDistributorid();
                        if (mRules.get(i).contains("{DIST_ID,")) {
                            try {
                                String value = mRules.get(i).substring(mRules.get(i).lastIndexOf(",") + 1, mRules.get(i).lastIndexOf("}")).replace(" ", "");
                                mComputeID.append(appendZero(seqNo, value));
                            } catch (Exception e) {
                                Commons.printException(e);
                                mComputeID.append(appendZero(seqNo, "0000"));
                            }

                        } else {
                            mComputeID.append(appendZero(seqNo, "0000"));
                        }

                    }
                    // UserID
                    else if (mRules.get(i).contains("{USER_ID")) {
                        seqNo = 0L;
                        seqNo = userMasterHelper
                                .getUserMasterBO().getUserid();
                        if (mRules.get(i).contains("{USER_ID,")) {
                            try {
                                String value = mRules.get(i).substring(mRules.get(i).lastIndexOf(",") + 1, mRules.get(i).lastIndexOf("}")).replace(" ", "");
                                mComputeID.append(appendZero(seqNo, value));
                            } catch (Exception e) {
                                Commons.printException(e);
                                mComputeID.append(appendZero(seqNo, "00000"));
                            }

                        } else {
                            mComputeID.append(appendZero(seqNo, "00000"));
                        }
                    }
                    //DistributorCode
                    else if (mRules.get(i).contains("{DIST_CODE")) {
                        seqNo = 0L;
                        String distCode = userMasterHelper
                                .getUserMasterBO().getDistributorCode();
                        mComputeID.append(distCode);

                    }
                    //FYEAR
                    else if (mRules.get(i).contains("{FYEAR")) {
                        seqNo = 0L;
                        String[] mYear = new String[2];
                        cursor = db
                                .selectSQL("SELECT FYEAR from AppVariables");
                        if (cursor != null) {
                            if (cursor.moveToNext()) {
                                mYear = cursor.getString(0).split("-");
                            }
                            cursor.close();
                        }


                        if (mRules.get(i).contains("{FYEAR,")) {

                            try {
                                String value = mRules.get(i).substring(mRules.get(i).lastIndexOf(",") + 1, mRules.get(i).lastIndexOf("}")).replace(" ", "");
                                mComputeID.append(splitYear(SDUtil.convertToInt(mYear[0]), value));
                                mComputeID.append("-" + splitYear(SDUtil.convertToInt(mYear[1]), value));
                            } catch (Exception e) {
                                Commons.printException(e);
                                mComputeID.append(appendZero(seqNo, "0000"));
                                mComputeID.append(appendZero(seqNo, "0000"));
                            }

                        } else {

                            mComputeID.append(appendZero(seqNo, "0000"));
                        }
                    } //Retailer Type ID
                    else if (mRules.get(i).contains("{RETAILER_TYPE,")) {

                        String value = getTypeCodeByRPType(mRules.get(i).substring(mRules.get(i).lastIndexOf(",") + 1, mRules.get(i).lastIndexOf("}")).replace(" ", ""));
                        mComputeID.append(value);
                    } else if (mRules.get(i).contains("{SOURCE")) {

                        mComputeID.append("M");
                    }


                    // Download Date
                    if (mRules.get(i).contains("YYYY") || mRules.get(i).contains("yyyy")
                            || mRules.get(i).contains("YY") || mRules.get(i).contains("yy")) {
                        String str = mRules.get(i).replace("{", "").replace("}", "");
                        if (str.equalsIgnoreCase("YY"))
                            str = str.replace("YY", "yy");
                        else
                            str = str.replace("YYYY", "yyyy");
                        mComputeID.append(DateTimeUtils.convertFromServerDateToRequestedFormat(userMasterHelper.getUserMasterBO().getDownloadDate(), str));
                    }
                   /* else if (mRules.get(i).contains("yyyy")) {
                        mComputeID.append(DateUtil.convertFromServerDateToRequestedFormat(userMasterHelper.getUserMasterBO().getDownloadDate(),
                                mRules.get(i).replace("{", "").replace("}", "")));
                    }
*/
                    // Get Sequence ID
                    else if (mRules.get(i).contains("{SEQ")) {
                        seqNo = 0L;
                        cursor = db
                                .selectSQL("SELECT SeqNo FROM TransactionSequence WHERE TypeId IN (SELECT ListID FROM StandardListMaster WHERE LISTCODE =" + QT(type) + ")");
                        if (cursor != null) {
                            if (cursor.moveToNext()) {
                                seqNo = (cursor.getInt(0) + 1);
                            }
                            cursor.close();
                        }


                        if (mRules.get(i).contains("{SEQ,")) {

                            try {
                                String value = mRules.get(i).substring(mRules.get(i).lastIndexOf(",") + 1, mRules.get(i).lastIndexOf("}")).replace(" ", "");
                                mComputeID.append(appendZero(seqNo, value));
                            } catch (Exception e) {
                                Commons.printException(e);
                                mComputeID.append(appendZero(seqNo, "0000"));
                            }

                        } else {

                            mComputeID.append(appendZero(seqNo, "0000"));
                        }
                    } else if (!mRules.get(i).contains("{")) {
                        mComputeID.append(mRules.get(i).replaceAll("\\[", "").replaceAll("\\]", ""));
                    } else if (mRules.get(i).contains("{SELLERCODE")) {

                        String userCode = userMasterHelper
                                .getUserMasterBO().getUserCode();
                        mComputeID.append(userCode);
                    }
                }
            } else {
                //If Rules not available set Default Sales Return ID or Credit Note Id with SeqNo

                if (type.equals("SR"))
                    mComputeID.append("SR"
                            + userMasterHelper.getUserMasterBO().getUserid()
                            + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID));
                else if (type.equals("CN"))
                    mComputeID.append("CR"
                            + userMasterHelper.getUserMasterBO().getUserid()
                            + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID));

                if (type.equals("ORD") || type.equals("INV") || type.equals("COL")) {
                    seqNo = 0L;
                    // DistributorID
                    seqNo = userMasterHelper
                            .getUserMasterBO().getDistributorid();
                    mComputeID.append(appendZero(seqNo, "0000"));

                    seqNo = 0L;
                    // UserID
                    seqNo = userMasterHelper
                            .getUserMasterBO().getUserid();
                    mComputeID.append(appendZero(seqNo, "00000"));


                    // Download Date
                    mComputeID.append(DateTimeUtils.convertFromServerDateToRequestedFormat(userMasterHelper.getUserMasterBO().getDownloadDate(), "MMddyyyy"));
                }
                seqNo = 0L;

                // Get Sequence ID
                cursor = db
                        .selectSQL("SELECT SeqNo FROM TransactionSequence WHERE TypeId IN (SELECT ListID FROM StandardListMaster WHERE LISTCODE =" + QT(type) + ")");
                if (cursor != null) {
                    if (cursor.moveToNext()) {
                        seqNo = (cursor.getInt(0) + 1);
                    }
                    cursor.close();
                }
                mComputeID.append(appendZero(seqNo, "0000"));
            } // End of Else

            // Update Sequence number
            db.updateSQL("Update TransactionSequence SET SeqNo = SeqNo+1, Upload = 'N'"
                    + " WHERE TypeId IN (SELECT ListID FROM StandardListMaster WHERE LISTCODE = " + QT(type) + ")");


            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }

        return mComputeID.toString();
    }

    /**
     * @param ruleString
     * @return this method will return retailer type based Retailer Type Code
     */
    private String getTypeCodeByRPType(String ruleString) {
        final String CASH_TYPE = "CASH";
        final String CREDIT_TYPE = "CREDIT";
        final String READY_TO_SALES_TYPE = "READYSALE";
        String retType;

        if (getRetailerMasterBO().getRpTypeCode().equalsIgnoreCase(CASH_TYPE)) {
            retType = (ruleString.split("\\|")[0]).split("_")[1];
        } else if (getRetailerMasterBO().getRpTypeCode().equalsIgnoreCase(CREDIT_TYPE)) {
            retType = (ruleString.split("\\|")[1]).split("_")[1];
        } else if (getRetailerMasterBO().getRpTypeCode().equalsIgnoreCase(READY_TO_SALES_TYPE)) {
            retType = (ruleString.split("\\|")[1]).split("_")[1];
        } else {
            retType = "";
        }
        return retType;
    }

    /**
     * Download the Sales Return or Credit Note Rules
     *
     * @return Array List of Transaction Rules for Sales Return or Credit Note
     */
    private ArrayList<String> downloadTransactionRules(String type) {
        ArrayList<String> rules = new ArrayList<>();
        Cursor cursor = null;
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            // Get Sequence ID
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT Rule FROM TransactionRules WHERE TypeId ");
            sb.append("IN (SELECT ListID FROM StandardListMaster WHERE LISTCODE =");
            sb.append(QT(type) + ")");
            // Get Sequence ID
            cursor = db
                    .selectSQL(sb.toString());

            if (cursor != null) {
                if (cursor.moveToNext()) {
                    String[] mRule = cursor.getString(0).split(":");

                    for (String aMRule : mRule) {
                        rules.add(aMRule);
                    }
                }
                cursor.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return rules;
    }

    private String appendZero(long seqNo, String value) {
        DecimalFormat df = new DecimalFormat(value);
        return df.format(seqNo);
    }

    private String splitYear(int year, String value) {
        int divider = SDUtil.convertToInt("1" + value);
        return "" + year % divider;
    }

    public int getTotalFocusBrandLines() {
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db.selectSQL("select sum(FocusPackLines)from "
                    + DataMembers.tbl_orderHeader + " where retailerid="
                    + QT(retailerMasterBO.getRetailerID()) + " and upload='N'");
            if (c != null) {
                if (c.moveToNext()) {
                    int i = c.getInt(0);
                    c.close();
                    db.closeDB();
                    return i;
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }

        return 0;
    }

    public int getTotalMSLLines() {
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db.selectSQL("select sum(MSPLines)from "
                    + DataMembers.tbl_orderHeader + " where retailerid="
                    + QT(retailerMasterBO.getRetailerID()) + " and upload='N'");
            if (c != null) {
                if (c.moveToNext()) {
                    int i = c.getInt(0);
                    c.close();
                    db.closeDB();
                    return i;
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }

        return 0;
    }

    public boolean isModuleCompleted(String menuName) {

        try {
            if (mModuleCompletionResult != null &&
                    mModuleCompletionResult.get(menuName) != null &&
                    mModuleCompletionResult.get(menuName).equals("1"))
                return true;
            else
                return false;

        } catch (Exception e) {
            return false;
        }
    }


    public void updateProductUOM(String activity, int type) {
        //type-0 tagged product
        //type-1 product master

        DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
        );
        db.createDataBase();
        db.openDataBase();
        try {

            int contentLevel = 0, contentLevelId = 0;
            Cursor c = db.selectSQL("SELECT MAX(Sequence),levelId FROM ProductLevel");
            if (c != null) {
                if (c.moveToNext()) {
                    contentLevel = c.getInt(0);
                    contentLevelId = c.getInt(1);
                }
            }


            c = db.selectSQL("SELECT productId, productLevelId, uomid from ActivityGroupMapping AGM" +
                    " INNER JOIN ActivityGroupProductMapping APM ON APM.groupid=AGM.groupid" +
                    " where AGM.activity=" + QT(activity));
            if (c != null) {
                if (c.getCount() > 0) {
                    initializeUOMmapping(type);
                    while (c.moveToNext()) {
                        updateProductMapping(c.getString(0), c.getInt(1), c.getInt(2), contentLevel, contentLevelId, type);
                    }
                } else {
                    enableUOMForAllProducts(type);
                }
            }
            c.close();

            db.closeDB();
        } catch (Exception e) {
            if (db != null)
                db.closeDB();
            Commons.printException("" + e);
        }
    }

    private void initializeUOMmapping(int type) {
        if (type == 0) {
            ProductTaggingHelper productTaggingHelper = ProductTaggingHelper.getInstance(getContext());
            for (ProductMasterBO bo : productTaggingHelper.getTaggedProducts()) {
                bo.setOuterMapped(false);
                bo.setCaseMapped(false);
                bo.setPieceMapped(false);
            }
        } else if (type == 1) {
            for (ProductMasterBO bo : productHelper.getProductMaster()) {
                bo.setOuterMapped(false);
                bo.setCaseMapped(false);
                bo.setPieceMapped(false);
            }
        } else if (type == 2) {
            for (LoadManagementBO bo : productHelper.getLoadMgmtProducts()) {
                bo.setOuterMapped(false);
                bo.setCaseMapped(false);
                bo.setPieceMapped(false);
            }
        }
    }

    private void enableUOMForAllProducts(int type) {
        if (type == 0) {
            ProductTaggingHelper productTaggingHelper = ProductTaggingHelper.getInstance(getContext());
            for (ProductMasterBO bo : productTaggingHelper.getTaggedProducts()) {
                bo.setOuterMapped(true);
                bo.setCaseMapped(true);
                bo.setPieceMapped(true);
            }
        } else if (type == 1) {
            for (ProductMasterBO bo : productHelper.getProductMaster()) {
                bo.setOuterMapped(true);
                bo.setCaseMapped(true);
                bo.setPieceMapped(true);
            }
        } else if (type == 2) {
            for (LoadManagementBO bo : productHelper.getLoadMgmtProducts()) {
                bo.setOuterMapped(true);
                bo.setCaseMapped(true);
                bo.setPieceMapped(true);
            }
        }
    }

    private void updateProductMapping(String productId, int pLevelId, int uomId, int contentLevel, int contentLevelId, int type) {
        DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
        );
        db.createDataBase();
        db.openDataBase();

        try {
            ProductTaggingHelper productTaggingHelper = ProductTaggingHelper.getInstance(getContext());

            if (contentLevelId == pLevelId) {
                if (type == 0) {
                    if (productTaggingHelper.getTaggedProductBOById(productId) != null) {
                        if (productTaggingHelper.getTaggedProductBOById(productId).getPcUomid() == uomId)
                            productTaggingHelper.getTaggedProductBOById(productId).setPieceMapped(true);
                        else if (productTaggingHelper.getTaggedProductBOById(productId).getCaseUomId() == uomId)
                            productTaggingHelper.getTaggedProductBOById(productId).setCaseMapped(true);
                        else if (productTaggingHelper.getTaggedProductBOById(productId).getOuUomid() == uomId)
                            productTaggingHelper.getTaggedProductBOById(productId).setOuterMapped(true);
                    }
                } else if (type == 1) {
                    if (productHelper.getProductMasterBOById(productId) != null) {
                        if (productHelper.getProductMasterBOById(productId).getPcUomid() == uomId)
                            productHelper.getProductMasterBOById(productId).setPieceMapped(true);
                        else if (productHelper.getProductMasterBOById(productId).getCaseUomId() == uomId)
                            productHelper.getProductMasterBOById(productId).setCaseMapped(true);
                        else if (productHelper.getProductMasterBOById(productId).getOuUomid() == uomId)
                            productHelper.getProductMasterBOById(productId).setOuterMapped(true);
                    }
                } else if (type == 2) {
                    if (productHelper.getLoadManagementBOById(SDUtil.convertToInt(productId)) != null) {
                        if (productHelper.getLoadManagementBOById(SDUtil.convertToInt(productId)).getPiece_uomid() == uomId)
                            productHelper.getLoadManagementBOById(SDUtil.convertToInt(productId)).setPieceMapped(true);
                        else if (productHelper.getLoadManagementBOById(SDUtil.convertToInt(productId)).getdUomid() == uomId)
                            productHelper.getLoadManagementBOById(SDUtil.convertToInt(productId)).setCaseMapped(true);
                        else if (productHelper.getLoadManagementBOById(SDUtil.convertToInt(productId)).getdOuonid() == uomId)
                            productHelper.getLoadManagementBOById(SDUtil.convertToInt(productId)).setOuterMapped(true);
                    }
                }
            } else {

                int parentLevel = 0;

                Cursor c = db.selectSQL("SELECT Sequence FROM ProductLevel where levelId=" + pLevelId);
                if (c != null) {
                    if (c.moveToNext())
                        parentLevel = c.getInt(0);
                }

                int loopEnd = contentLevel - parentLevel + 1;

                StringBuilder sb = new StringBuilder();
                sb.append("select distinct PM" + loopEnd + ".pid from productmaster PM1 ");
                for (int i = 2; i <= loopEnd; i++) {
                    sb.append(" INNER JOIN ProductMaster PM" + i + " ON PM" + i
                            + ".ParentId = PM" + (i - 1) + ".PID");
                }
                sb.append(" where PM1.pid=" + productId);

                c = db.selectSQL(sb.toString());
                if (c.getCount() > 0) {
                    while (c.moveToNext()) {
                        if (type == 0) {
                            if (productTaggingHelper.getTaggedProductBOById(c.getString(0)) != null) {
                                if (productTaggingHelper.getTaggedProductBOById(c.getString(0)).getPcUomid() == uomId)
                                    productTaggingHelper.getTaggedProductBOById(c.getString(0)).setPieceMapped(true);
                                else if (productTaggingHelper.getTaggedProductBOById(c.getString(0)).getCaseUomId() == uomId)
                                    productTaggingHelper.getTaggedProductBOById(c.getString(0)).setCaseMapped(true);
                                else if (productTaggingHelper.getTaggedProductBOById(c.getString(0)).getOuUomid() == uomId)
                                    productTaggingHelper.getTaggedProductBOById(c.getString(0)).setOuterMapped(true);
                            }
                        } else if (type == 1) {
                            if (productHelper.getProductMasterBOById(c.getString(0)) != null) {
                                if (productHelper.getProductMasterBOById(c.getString(0)).getPcUomid() == uomId)
                                    productHelper.getProductMasterBOById(c.getString(0)).setPieceMapped(true);
                                else if (productHelper.getProductMasterBOById(c.getString(0)).getCaseUomId() == uomId)
                                    productHelper.getProductMasterBOById(c.getString(0)).setCaseMapped(true);
                                else if (productHelper.getProductMasterBOById(c.getString(0)).getOuUomid() == uomId)
                                    productHelper.getProductMasterBOById(c.getString(0)).setOuterMapped(true);
                            }
                        } else if (type == 2) {
                            if (productHelper.getLoadManagementBOById(SDUtil.convertToInt(c.getString(0))) != null) {
                                if (productHelper.getLoadManagementBOById(SDUtil.convertToInt(c.getString(0))).getPiece_uomid() == uomId)
                                    productHelper.getLoadManagementBOById(SDUtil.convertToInt(c.getString(0))).setPieceMapped(true);
                                else if (productHelper.getLoadManagementBOById(SDUtil.convertToInt(c.getString(0))).getdUomid() == uomId)
                                    productHelper.getLoadManagementBOById(SDUtil.convertToInt(c.getString(0))).setCaseMapped(true);
                                else if (productHelper.getLoadManagementBOById(SDUtil.convertToInt(c.getString(0))).getdOuonid() == uomId)
                                    productHelper.getLoadManagementBOById(SDUtil.convertToInt(c.getString(0))).setOuterMapped(true);
                            }
                        }
                    }

                }

                c.close();

            }


            // updating competitor products UOM based on mapping with own products.
            if (type == 0) {
                for (ProductMasterBO bo : productTaggingHelper.getTaggedProducts()) {
                    if (bo.getOwn() == 0) {
                        if (productTaggingHelper.getTaggedProductBOById(bo.getOwnPID()) != null) {
                            if (productTaggingHelper.getTaggedProductBOById(bo.getOwnPID()).isPieceMapped())
                                bo.setPieceMapped(true);
                            if (productTaggingHelper.getTaggedProductBOById(bo.getOwnPID()).isCaseMapped())
                                bo.setCaseMapped(true);
                            if (productTaggingHelper.getTaggedProductBOById(bo.getOwnPID()).isOuterMapped())
                                bo.setOuterMapped(true);
                        }
                    }
                }
            }

            db.closeDB();
        } catch (Exception e) {
            if (db != null)
                db.closeDB();
            Commons.printException("" + e);
        }

    }

    // Download bank details
    public void downloadBankDetails() {
        BankMasterBO inv;
        DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME);
        try {

            db.openDataBase();
            Cursor c = db.selectSQL("SELECT ListName, ListId FROM StandardListMaster WHERE ListType = 'BANK_TYPE'");
            if (c != null) {
                Vector<BankMasterBO> bankMaster = new Vector<BankMasterBO>();
                while (c.moveToNext()) {
                    inv = new BankMasterBO();
                    inv.setBankName(c.getString(0));
                    inv.setBankId(c.getInt(1));
                    bankMaster.add(inv);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            db.closeDB();
        }
    }

    public void downloadBranchDetails() {
        BranchMasterBO inv;
        DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME);
        db.openDataBase();
        Cursor c = db.selectSQL("SELECT ListId, Parentid, ListName, ListCode FROM StandardListMaster WHERE ListType = 'BANK_BRANCH_TYPE'");
        if (c != null) {
            Vector<BranchMasterBO> bankBranch = new Vector<BranchMasterBO>();
            while (c.moveToNext()) {
                inv = new BranchMasterBO();
                inv.setBranchID(c.getString(0));
                inv.setBankID(c.getString(1));
                inv.setBranchName(c.getString(2));
                inv.setBankbranchCode(c.getString(3));
                bankBranch.add(inv);
            }
            c.close();
        }
        db.closeDB();
    }

    /**
     * @param supplierid - selected supplierid
     * @author rajesh.k Method to use update supplier selectionin retailer
     * information table
     */
    public void updateRetailerWiseSupplierType(int supplierid) {
        try {

            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.openDataBase();
            String query = "update retailermasterinfo set supplierid="
                    + supplierid + " where retailerid="
                    + QT(getRetailerMasterBO().getRetailerID());
            db.updateSQL(query);
            db.close();

        } catch (Exception e) {
            Commons.printException(e);
        }

    }


//    public Bitmap getCircularBitmapFrom(Bitmap source) {
//        if (source == null || source.isRecycled()) {
//            return null;
//        }
//        float radius = source.getWidth() > source.getHeight() ? ((float) source
//                .getHeight()) / 2f : ((float) source.getWidth()) / 2f;
//        Bitmap bitmap = Bitmap.createBitmap(source.getWidth(),
//                source.getHeight(), Bitmap.Config.ARGB_8888);
//
//        Paint paint = new Paint();
//        BitmapShader shader = new BitmapShader(source, Shader.TileMode.CLAMP,
//                Shader.TileMode.CLAMP);
//        paint.setShader(shader);
//        paint.setAntiAlias(true);
//
//        Canvas canvas = new Canvas(bitmap);
//        canvas.drawCircle(source.getWidth() / 2, source.getHeight() / 2,
//                radius, paint);
//
//        return bitmap;
//    }

    public void downloadDebitNoteDetails(DBUtil db) {

        if (invoiceHeader == null) {
            invoiceHeader = new ArrayList<>();
        }
        StringBuffer sb = new StringBuffer();
        sb.append("select debitnoteno,inv.date,debitnoteamount,");

        sb.append(" Round(IFNULL((select sum(payment.Amount) from payment where payment.BillNumber=Inv.DebitNoteNo),0),2) as RcvdAmt,");
        sb.append(" Round(inv.BalanceAmount - IFNULL((select sum(payment.Amount) from payment where payment.BillNumber=Inv.DebitNoteNo),0),2) as os,");
        sb.append(" Inv.comments FROM DebitNoteMaster Inv LEFT OUTER JOIN payment ON payment.BillNumber = Inv.DebitNoteNo");
        sb.append(" LEFT OUTER JOIN PaymentDiscountDetail PD ON payment.uid = PD.uid");
        sb.append(" WHERE inv.Retailerid = ");
        sb.append(QT(getRetailerMasterBO().getRetailerID()));
        sb.append(" GROUP BY Inv.DebitNoteNo ");
        sb.append(" ORDER BY Inv.Date");

        Cursor c = db.selectSQL(sb.toString());
        if (c.getCount() > 0) {
            InvoiceHeaderBO invoiceHeaderBO;
            while (c.moveToNext()) {
                invoiceHeaderBO = new InvoiceHeaderBO();
                invoiceHeaderBO.setInvoiceNo(c.getString(0));
                invoiceHeaderBO.setInvoiceDate(c.getString(1));
                invoiceHeaderBO.setInvoiceAmount(c.getDouble(2));
                invoiceHeaderBO.setPaidAmount(c.getDouble(3));
                invoiceHeaderBO.setBalance(c.getDouble(4));
                invoiceHeaderBO.setComments(c.getString(5));
                invoiceHeaderBO.setDebitNote(true);
                invoiceHeader.add(invoiceHeaderBO);
            }
        }
        c.close();


    }

    public String getRetailerAttributeList() {
        return retailerAttributeList;
    }

    /**
     * @See {@link com.ivy.core.data.db.AppDataManagerImpl#fetchNewActivityMenu(String)}
     * @deprecated
     */
    public void getAttributeHierarchyForRetailer() {
        retailerAttributeList = "";
        String str = "0";
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );

            int mContentLevel = 0;
            db.openDataBase();

            StringBuilder sb = new StringBuilder();

            sb.append("select EAM1.AttributeId,EAM2.AttributeId from EntityAttributeMaster EAM1 ");
            sb.append("INNER JOIN EntityAttributeMaster EAM2 ON EAM1.ParentId = EAM2.AttributeId");
            sb.append(" where EAM1.AttributeId in (select AttributeId from RetailerAttribute where RetailerId=" + getRetailerMasterBO().getRetailerID() + ")");
            Cursor c = db.selectSQL(sb.toString());

            if (c != null) {
                while (c.moveToNext()) {
                    for (int i = 0; i < c.getColumnCount(); i++) {
                        str = str + c.getString(i);
                        if (c.getColumnCount() > 1 && i != c.getColumnCount())
                            str = str + ",";
                    }

                }

                c.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        if (str.endsWith(","))
            str = str.substring(0, str.length() - 1);

        retailerAttributeList = str;
    }


    public ArrayList<String> getAttributeParentListForCurrentRetailer(String retailerId) {
        ArrayList<String> lst = null;
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );

            db.openDataBase();
            String sql = "select distinct EA.parentid from RetailerAttribute RA" +
                    " inner join EntityAttributeMaster EA on EA.Attributeid = RA.Attributeid" +
                    " where retailerid =" + retailerId;
            Cursor c = db.selectSQL(sql);
            if (c != null && c.getCount() > 0) {
                lst = new ArrayList<>();
                while (c.moveToNext()) {
                    lst.add(c.getString(0));
                }
                c.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.print(e.getMessage());
            return null;
        }
        return lst;
    }


    public ArrayList<GuidedSellingBO> getmGuidedSelling() {
        if (mGuidedSelling == null)
            mGuidedSelling = new ArrayList<>();

        return mGuidedSelling;
    }

    ArrayList<GuidedSellingBO> mGuidedSelling;

    public void downloadGuidedSelling() {
        DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME);
        db.openDataBase();

        try {
            Cursor c = db
                    .selectSQL("SELECT subActivity,FilterCode,ApplyLevel,sequence,prodFilter FROM GuidedSellingConfig order by sequence");

            if (c != null) {
                if (c.getCount() > 0) {
                    mGuidedSelling = new ArrayList<>();
                    GuidedSellingBO bo;
                    while (c.moveToNext()) {
                        bo = new GuidedSellingBO();
                        bo.setSubActivity(c.getString(0));
                        bo.setFilterCode(c.getString(1));
                        bo.setApplyLevel(c.getString(2));
                        bo.setSequance(c.getInt(3));
                        bo.setProductFilter(c.getInt(4) == 1);
                        mGuidedSelling.add(bo);
                    }
                    c.close();
                }
            }
        } catch (Exception e) {
            Commons.print(e.getMessage());
        }

        db.closeDB();
    }

    public void writeToFile(String data, String filename, String foldername) {
        String path = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + foldername;

        File folder = new File(path);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File newFile = new File(path, filename + ".txt");
        try {
            newFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(newFile);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(data);
            myOutWriter.close();
            fOut.flush();
            fOut.close();
            if (configurationMasterHelper.IS_PRINT_FILE_SAVE && filename.startsWith(DataMembers.PRINT_FILE_START)) {
                String destpath = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + DataMembers.IVYDIST_PATH + "/";
                copyFile(newFile, destpath, filename);
            }
        } catch (IOException e) {
            Commons.printException(e);
        }
    }


    /**
     * read text from given file and convert to string object
     * and store in object
     *
     * @param fileName
     */
    public void readBuilder(String fileName) {
        String path = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                + "/" + userMasterHelper.getUserMasterBO().getUserid() + DataMembers.PRINTFILE + "/";
        File file = new File(path + fileName);
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));


            String st;
            while ((st = br.readLine()) != null) {
                sb.append(st);
                sb.append("\n");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mCommonPrintHelper.setInvoiceData(sb);

    }

    private void copyFile(File sourceFile, String path, String filename) {

        File folder = new File(path);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File destFile = new File(path, filename + ".txt");
        FileChannel source = null;
        FileChannel destination = null;
        try {

            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
            source.close();
            destination.close();
        } catch (FileNotFoundException e) {
            Commons.printException(e.getMessage());
        } catch (IOException e) {
            Commons.printException(e.getMessage());
        } finally {

        }
    }


    public void updatePriceGroupId(boolean isRetailer) {

        DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME);
        db.openDataBase();
        StringBuilder sb = new StringBuilder();

        try {
            sb.append("SELECT groupId FROM RetailerPriceGroup WHERE");

            if (isRetailer) {
                sb.append(" retailerId =" + getRetailerMasterBO().getRetailerID() + " AND");
            }

            sb.append(" distributorId =" + getRetailerMasterBO().getDistributorId());


            Cursor c = db.selectSQL(sb.toString());

            if (c != null) {
                if (c.getCount() > 0) {
                    while (c.moveToNext()) {
                        getRetailerMasterBO().setGroupId(c.getInt(0));
                    }
                    c.close();
                } else {
                    getRetailerMasterBO().setGroupId(0);
                }
            }
        } catch (Exception e) {
            Commons.print(e.getMessage());
        }

        db.closeDB();
    }

    private HashMap<Integer, Double> mCurrencyConfig;

    public void downloadCurrencyConfig() {

        DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME);
        db.openDataBase();

        try {
            Cursor c = db
                    .selectSQL("select fromValue,toValue,Value from CurrencyConfigRule order by fromValue");

            if (c != null) {
                if (c.getCount() > 0) {
                    mCurrencyConfig = new HashMap<>();
                    while (c.moveToNext()) {
                        mCurrencyConfig.put(c.getInt(0), c.getDouble(2));
                    }
                    c.close();
                }

            }
        } catch (Exception e) {
            Commons.print(e.getMessage());
        }

        db.closeDB();
    }

    //getting actual value for given decimal value
    public double getCurrencyActualValue(Integer decimalValue) {
        double result = 0;
        try {
            if (mCurrencyConfig != null) {
                for (Integer key : mCurrencyConfig.keySet()) {
                    if (decimalValue >= 51 && key == 51) {
                        result = mCurrencyConfig.get(key);
                        break;
                    } else if (decimalValue == 50) {
                        result = mCurrencyConfig.get(key);
                        break;
                    } else if (decimalValue <= 49 && key == 0) {
                        result = mCurrencyConfig.get(key);
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            Commons.printException(ex);
        }
        return result;

    }

    int selectedUserId;

    public int getSelectedUserId() {
        return selectedUserId;
    }

    public void setSelectedUserId(int selectedUserId) {
        this.selectedUserId = selectedUserId;
    }


    //Pending invoice report

    public void downloadInvoice() {
        try {
            configurationMasterHelper.loadInvoiceMasterDueDateAndDateConfig();
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.openDataBase();

            StringBuffer sb = new StringBuffer();

            sb.append("SELECT distinct Inv.InvoiceNo, Inv.InvoiceDate, Round(invNetamount,2) as Inv_amt,");
            sb.append(" Round(IFNULL((select sum(payment.Amount) from payment where payment.BillNumber=Inv.InvoiceNo),0)+Inv.paidAmount,2) as RcvdAmt,");
            sb.append(" Round(inv.discountedAmount- IFNULL((select sum(payment.Amount) from payment where payment.BillNumber=Inv.InvoiceNo),0),2) as os,");
            sb.append(" payment.ChequeNumber,payment.ChequeDate,Round(Inv.discountedAmount,2),sum(PD.discountvalue),RM.RetailerName as RetailerName,IFNULL(RM.creditPeriod,'') as creditPeriod,DueDays,DueDate");
            sb.append(" FROM InvoiceMaster Inv LEFT OUTER JOIN payment ON payment.BillNumber = Inv.InvoiceNo");
            sb.append(" LEFT OUTER JOIN PaymentDiscountDetail PD ON payment.uid = PD.uid");
            sb.append(" INNER JOIN RetailerMaster RM ON inv.Retailerid = RM.RetailerID");
            sb.append(" GROUP BY Inv.InvoiceNo,inv.Retailerid");
            sb.append(" ORDER BY Inv.InvoiceDate");

            Cursor c = db.selectSQL(sb.toString());
            if (c != null) {
                InvoiceHeaderBO invocieHeaderBO;
                invoiceHeader = new ArrayList<>();
                while (c.moveToNext()) {
                    invocieHeaderBO = new InvoiceHeaderBO();
                    invocieHeaderBO.setInvoiceNo(c.getString(0));
                    invocieHeaderBO.setInvoiceDate(c.getString(1));
                    invocieHeaderBO.setInvoiceAmount(c.getDouble(2));
                    invocieHeaderBO.setPaidAmount(c.getDouble(3));
                    invocieHeaderBO.setBalance(c.getDouble(4));
                    invocieHeaderBO.setAppliedDiscountAmount(c.getDouble(8));
                    invocieHeaderBO.setRetailerName(c.getString(c.getColumnIndex("RetailerName")));

                    int count = DateTimeUtils.getDateCount(invocieHeaderBO.getInvoiceDate(),
                            DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), "yyyy/MM/dd");
                    final double discountpercentage = CollectionHelper.getInstance(ctx).getDiscountSlabPercent(count + 1);

                    double remaingAmount = (invocieHeaderBO.getInvoiceAmount() - (invocieHeaderBO.getAppliedDiscountAmount() + invocieHeaderBO.getPaidAmount())) * discountpercentage / 100;
                    if (configurationMasterHelper.ROUND_OF_CONFIG_ENABLED) {
                        remaingAmount = SDUtil.convertToDouble(SDUtil.format(remaingAmount,
                                0,
                                0, configurationMasterHelper.IS_DOT_FOR_GROUP));
                    }

                    invocieHeaderBO.setRemainingDiscountAmt(remaingAmount);
                    if (configurationMasterHelper.COMPUTE_DUE_DATE) {
                        int crediiDays = c.getInt(c.getColumnIndex("creditPeriod"));

                        if (crediiDays != 0) {

                            SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
                            Date date = format.parse(invocieHeaderBO.getInvoiceDate());
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(date);
                            calendar.add(Calendar.DAY_OF_YEAR, crediiDays);
                            Date dueDate = format.parse(format.format(calendar.getTime()));

                            invocieHeaderBO.setDueDate(DateTimeUtils.convertDateObjectToRequestedFormat(
                                    dueDate, configurationMasterHelper.outDateFormat));

                        }
                    } else {
                        invocieHeaderBO.setDueDate(c.getString(c.getColumnIndex("DueDate")));
                    }
                    if (!configurationMasterHelper.COMPUTE_DUE_DAYS)
                        invocieHeaderBO.setDueDays(c.getString(c.getColumnIndex("DueDays")));
                    if (invocieHeaderBO.getBalance() > 0)
                        invoiceHeader.add(invocieHeaderBO);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {

            Commons.printException(e);
        }
    }


    public void insertTempOrder() {

        DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
        );
        try {
            db.createDataBase();
            db.openDataBase();
            int siz = productHelper.getProductMaster().size();

            db.deleteSQL("TempOrderDetail", "RetailerID=" + QT(getRetailerMasterBO().getRetailerID()),
                    false);

            String columns = "RetailerID,ProductID,pieceqty,caseQty,outerQty";

            for (int i = 0; i < siz; ++i) {
                ProductMasterBO product = productHelper
                        .getProductMaster().get(i);
                if (product.getOrderedCaseQty() > 0
                        || product.getOrderedPcsQty() > 0
                        || product.getOrderedOuterQty() > 0) {

                    String values = QT(getRetailerMasterBO().getRetailerID())
                            + ","
                            + QT(product.getProductID())
                            + ","
                            + product.getOrderedPcsQty()
                            + ","
                            + product.getOrderedCaseQty()
                            + ","
                            + product.getOrderedOuterQty();


                    db.insertSQL("TempOrderDetail", columns, values);
                }
            }
            db.closeDB();
        } catch (Exception ex) {
            db.closeDB();
            Commons.printException(ex);
        }
    }


    public void loadTempOrderDetails() {
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            String sql2 = "select productId,pieceqty,caseQty,outerQty from TempOrderDetail "
                    + " where RetailerID="
                    + QT(getRetailerMasterBO().getRetailerID()) + " order by rowid";

            Cursor tOrderDetailCursor = db.selectSQL(sql2);

            if (tOrderDetailCursor != null) {
                while (tOrderDetailCursor.moveToNext()) {

                    String productId = tOrderDetailCursor.getString(0);
                    int pieceqty = tOrderDetailCursor.getInt(1);
                    int caseqty = tOrderDetailCursor.getInt(2);
                    int outerQty = tOrderDetailCursor.getInt(3);

                    setProductDetails(productId, pieceqty, caseqty,
                            outerQty);

                }
            }
            tOrderDetailCursor.close();

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    /**
     * This method will called to planeDeviateReason
     * reason.
     */
    public void savePlaneDiveateReason(ArrayList<NonproductivereasonBO> reasonBoList, String remarks) {
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            String values;
            db.createDataBase();
            db.openDataBase();

            // uid = distid+uid+hh:mm
            String id = "";
            Cursor c = db.selectSQL("Select Uid from NonFieldActivity where UserId="
                    + userMasterHelper.getUserMasterBO().getUserid() + " AND Upload ='N'");

            if (c != null) {
                if (c.getCount() > 0)
                    while (c.moveToNext()) {
                        id = c.getString(0);
                        break;
                    }
            }
            c.close();

            if (!id.equals(""))
                db.deleteSQL(
                        "NonFieldActivity",
                        "Uid=" + QT(id), false);


            id = QT(userMasterHelper.getUserMasterBO()
                    .getDistributorid()
                    + ""
                    + userMasterHelper.getUserMasterBO().getUserid()
                    + ""
                    + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID_MILLIS));


            String columns = "UID,UserId,Date,ReasonID,Remarks,DistributorID";

            for (NonproductivereasonBO reasnBo : reasonBoList) {

                String remark = "";
                if (!reasnBo.getDeviatedReasonId().equalsIgnoreCase("0")) {
                    remark = reasnBo.getDeviationReason();
                } else {
                    remark = remarks;
                }

                values = id + "," + QT(userMasterHelper.getUserMasterBO().getUserid() + "") + ","
                        + QT(reasnBo.getDate()) + "," + QT(reasnBo.getReasonid())
                        + "," + QT(remark) +
                        "," + getRetailerMasterBO().getDistributorId();

                db.insertSQL("NonFieldActivity", columns, values);

            }


            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }


    /**
     * Returns email credentials given
     *
     * @return
     */
    public HashMap<String, String> downloadEmailAccountCredentials() {
        HashMap<String, String> mUserCredentials = new HashMap<>();
        mUserCredentials.put("EMAILID", "");
        mUserCredentials.put("PASSWORD", "");
        mUserCredentials.put("TYPE", "");
        try {
            DBUtil db = new DBUtil(getContext(), DataMembers.DB_NAME
            );
            db.openDataBase();
            String s = "SELECT ListName FROM StandardListMaster where listcode='DELIVERY_EMAIL' and listtype='DELIVERY_MAIL'";

            Cursor c = db.selectSQL(s);
            if (c != null) {
                if (c.moveToNext()) {
                    mUserCredentials.put("EMAILID", c.getString(0));
                }
                c.close();
            }

            s = "SELECT ListName FROM StandardListMaster where listcode='DELIVERY_PWD' and listtype='DELIVERY_MAIL'";

            c = db.selectSQL(s);
            if (c != null) {
                if (c.moveToNext()) {
                    mUserCredentials.put("PASSWORD", c.getString(0));
                }
                c.close();
            }

            s = "SELECT ListName FROM StandardListMaster where listcode='DELIVERY_TYPE' and listtype='DELIVERY_MAIL'";

            c = db.selectSQL(s);
            if (c != null) {
                if (c.moveToNext()) {
                    mUserCredentials.put("TYPE", c.getString(0));
                }
                c.close();
            }

            db.closeDB();
        } catch (SQLException e) {
            Commons.printException(e);
        }

        return mUserCredentials;
    }

    public Vector<RetailerMasterBO> getSubDMaster() {
        if (subDMaster == null)
            return new Vector<RetailerMasterBO>();
        return subDMaster;
    }

    public void setSubDMaster(Vector<RetailerMasterBO> subDMaster) {
        this.subDMaster = subDMaster;
    }

    /**
     * @return
     * @See {@link ChannelDataManagerImpl#fetchChannelIds()}
     * @deprecated
     */
    public String getChannelids() {
        String sql;
        String sql1 = "";
        String str = "";
        int channelid = 0;
        try {
            if (getRetailerMasterBO() != null)
                channelid = getRetailerMasterBO().getSubchannelid();


            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );

            int mChildLevel = 0;
            int mContentLevel = 0;
            db.openDataBase();
            Cursor c = db.selectSQL("select min(Sequence) as childlevel,(select Sequence from ChannelLevel cl inner join ChannelHierarchy ch on ch.LevelId=cl.LevelId where ch.ChId=" + channelid + ") as contentlevel  from ChannelLevel");
            if (c != null) {
                while (c.moveToNext()) {
                    mChildLevel = c.getInt(0);
                    mContentLevel = c.getInt(1);
                }
                c.close();
            }

            int loopEnd = mContentLevel - mChildLevel + 1;

            for (int i = 2; i <= loopEnd; i++) {
                sql1 = sql1 + " LM" + i + ".ChId";
                if (i != loopEnd)
                    sql1 = sql1 + ",";
            }
            sql = "select LM1.ChId," + sql1 + "  from ChannelHierarchy LM1";
            for (int i = 2; i <= loopEnd; i++)
                sql = sql + " INNER JOIN ChannelHierarchy LM" + i + " ON LM" + (i - 1)
                        + ".ParentId = LM" + i + ".ChId";
            sql = sql + " where LM1.ChId=" + channelid;
            c = db.selectSQL(sql);
            if (c != null) {
                while (c.moveToNext()) {
                    for (int i = 0; i < c.getColumnCount(); i++) {
                        str = str + c.getString(i);
                        if (c.getColumnCount() > 1 && i != c.getColumnCount())
                            str = str + ",";
                    }
                    if (str.endsWith(","))
                        str = str.substring(0, str.length() - 1);
                }
                c.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return str;
    }

    @Deprecated
    /**
     * @deprecated
     * @See {@link SellerDashboardDataManagerImpl#getCollectedValue()}
     */
    public ArrayList<Double> getCollectedValue() {
        ArrayList<Double> collectedList = new ArrayList<>();
        double osAmt = 0, paidAmt = 0;
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            StringBuffer sb = new StringBuffer();

            sb.append("SELECT Round(IFNULL((select sum(payment.Amount) from payment where payment.BillNumber=Inv.InvoiceNo),0)+Inv.paidAmount,2) as RcvdAmt,");
            sb.append(" Round(inv.discountedAmount- IFNULL((select sum(payment.Amount) from payment where payment.BillNumber=Inv.InvoiceNo),0),2) as os ");
            sb.append(" FROM InvoiceMaster Inv LEFT OUTER JOIN payment ON payment.BillNumber = Inv.InvoiceNo");
            sb.append(" Where Inv.InvoiceDate = " + QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)));
            Cursor c = db
                    .selectSQL(sb.toString());

            if (c != null) {
                if (c.getCount() > 0) {
                    while (c.moveToNext()) {
                        paidAmt = paidAmt + c.getDouble(c.getColumnIndex("RcvdAmt"));
                        osAmt = osAmt + c.getDouble(c.getColumnIndex("os"));
                    }

                }
                c.close();
            }

            collectedList.add(osAmt);
            collectedList.add(paidAmt);


            db.closeDB();
        } catch (Exception e) {
            Commons.printException("Error at getCollectedValue", e);
        }
        return collectedList;

    }

    public ArrayList<String> getOrderedFocusBrands() {
        return orderedBrands;
    }

    public ArrayList<String> getTotalFocusBrandList() {
        return totalFocusBrandList;
    }


    private String getFocusFilterName(String filtername) {
        Vector<ConfigureBO> genfilter = configurationMasterHelper
                .getGenFilter();
        for (int i = 0; i < genfilter.size(); i++) {
            if (genfilter.get(i).getConfigCode().equals(filtername))
                filtername = genfilter.get(i).getMenuName();
        }
        return filtername;
    }


    public double getRetailerInvoiceAmount() {
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select sum(invNetamount) from InvoiceMaster where retailerid="
                            + QT(retailerMasterBO.getRetailerID()));
            if (c != null) {
                if (c.moveToNext()) {
                    double i = c.getFloat(0);
                    c.close();
                    db.closeDB();
                    return i;
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }

        return 0;
    }

    public boolean hasPendingInvoice(String date, String retailerIds) {
        try {
            double balance = 0;
            DBUtil db = new DBUtil(this, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db.selectSQL("select Inv.InvoiceNo,Round(Inv.discountedAmount- IFNULL((select sum(payment.Amount) from payment where payment.BillNumber=Inv.InvoiceNo),0),2) as balance from "
                    + DataMembers.tbl_InvoiceMaster + " Inv LEFT OUTER JOIN payment ON payment.BillNumber = Inv.InvoiceNo where Inv.Retailerid in("
                    + retailerIds
                    + ") and Inv.InvoiceDate ='" + date + "'and Inv.upload = 'N'");
            if (c != null) {
                while (c.moveToNext()) {
                    balance = balance + c.getDouble(c.getColumnIndex("balance"));
                }
                c.close();
                if (balance > 0)
                    return true;
            }

            db.closeDB();
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public String getUserParentPosition() {
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select ParentPositionIds from UserMaster where userid="
                            + QT(String.valueOf(userMasterHelper.getUserMasterBO().getUserid())));
            if (c != null) {
                if (c.moveToNext()) {
                    String id = c.getString(0);
                    c.close();
                    db.closeDB();
                    return id == null ? "" : id;
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }

        return "";
    }

    public void updateRetailersTotWgt(DBUtil db) {
        try {
            Cursor c = db.selectSQL("select pieceqty,caseQty,outerQty,uomcount,dOuomQty,weight,retailerid from OrderDetail");
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    int qty = c.getInt(0) +
                            (c.getInt(1) * c.getInt(3) +
                                    (c.getInt(2) * c.getInt(4)));
                    for (RetailerMasterBO bo : retailerMaster) {
                        if (bo.getRetailerID().equals(c.getString(6))) {
                            bo.setmOrderedTotWgt(bo.getmOrderedTotWgt() + (qty * c.getDouble(5)));
                        }
                    }
                }

            }
            c.close();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public void updateRetailersTotWgt(RetailerMasterBO bo) {
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db.selectSQL("select pieceqty,caseQty,outerQty,uomcount,dOuomQty,weight from OrderDetail " +
                    "where retailerid =" + StringUtils.QT(bo.getRetailerID()));
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    int qty = c.getInt(0) +
                            (c.getInt(1) * c.getInt(3) +
                                    (c.getInt(2) * c.getInt(4)));
                    bo.setmOrderedTotWgt(bo.getmOrderedTotWgt() + (qty * c.getDouble(5)));
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    String newlyaddedRetailer = "";

    public String getNewlyaddedRetailer() {
        return newlyaddedRetailer;
    }

    public void setNewlyaddedRetailer(String newlyaddedRetailer) {
        this.newlyaddedRetailer = newlyaddedRetailer;
    }

    HashMap<String, String> photosTakeninCurrentCompetitorTracking = new HashMap<>();

    public HashMap<String, String> getPhotosTakeninCurrentCompetitorTracking() {
        return photosTakeninCurrentCompetitorTracking;
    }

    public void setPhotosTakeninCurrentCompetitorTracking(HashMap<String, String> photosTakeninCurrentCompetitorTracking) {
        this.photosTakeninCurrentCompetitorTracking = photosTakeninCurrentCompetitorTracking;
    }

    HashMap<String, String> photosTakeninCurrentAssetTracking = new HashMap<>();

    public HashMap<String, String> getPhotosTakeninCurrentAssetTracking() {
        return photosTakeninCurrentAssetTracking;
    }

    public void setPhotosTakeninCurrentAssetTracking(HashMap<String, String> photosTakeninCurrentAssetTracking) {
        this.photosTakeninCurrentAssetTracking = photosTakeninCurrentAssetTracking;
    }

    public ArrayList<DigitalContentModel> getDigitalContentLargeFileURLS() {
        if (digitalContentLargeFileURLS == null)
            return new ArrayList<>();
        else
            return new ArrayList<>(digitalContentLargeFileURLS.values());
    }

    public DigitalContentModel getDigitalContenLargeFileModel(int imageId) {
        if (digitalContentLargeFileURLS.get(imageId) == null)
            return null;
        else
            return digitalContentLargeFileURLS.get(imageId);
    }

    public void setDigitalContentLargeFileURLS(HashMap<Integer, DigitalContentModel> digitalContentLargeFileURLS) {
        this.digitalContentLargeFileURLS = digitalContentLargeFileURLS;
    }

    public AppDataProvider getAppDataProvider() {
        return appDataProvider;
    }

    //Azure ImageUpload
    void uploadImageToAzureCloud(Handler handler) {
        String start_time = DateTimeUtils.now(DateTimeUtils.DATE_TIME_NEW);
        AzureConnectionHelper.getInstance().setAzureCredentials(getApplicationContext());
        try {

            folder = new File(FileUtils.photoFolderPath + "/");

            sfFiles = folder.listFiles();

            uploadFileCount = sfFiles.length;
            isErrorOccured = false;
            for (int i = 0; i < uploadFileCount; i++) {

                String filename = sfFiles[i].getName();
                //  print invoice file not upload to server

                getResponseForUploadImageToAzureStorageCloud(filename, AzureConnectionHelper.getInstance().initializeAzureStorageConnection(), handler);

            }
            if (successCount == uploadFileCount) {
                fileDeleteAfterUpload();
                synchronizationHelper.insertSyncHeader(start_time, DateTimeUtils.now(DateTimeUtils.DATE_TIME_NEW), SynchronizationHelper.SYNC_TYPE_DGT_UPLOAD,
                        successCount, SynchronizationHelper.SYNC_STATUS_COMPLETED, uploadFileCount);
                successCount = 0;
                sentMessageToHandler
                        (DataMembers.NOTIFY_WEB_UPLOAD_SUCCESS,
                                "Images uploaded Successfully",
                                handler);

            } else {
                sentMessageToHandler
                        (DataMembers.NOTIFY_WEB_UPLOAD_ERROR,
                                "Image Upload Failed!", handler);
                synchronizationHelper.insertSyncHeader(start_time, DateTimeUtils.now(DateTimeUtils.DATE_TIME_NEW), SynchronizationHelper.SYNC_TYPE_DGT_UPLOAD,
                        successCount, SynchronizationHelper.SYNC_STATUS_FAILED, uploadFileCount);
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private void getResponseForUploadImageToAzureStorageCloud(String imageName, CloudBlobContainer cloudBlobContainer, final Handler mHandler) {
        try {
            final File image = new File(folder, "/" + imageName);
            InputStream fileInputStream = getContentResolver().openInputStream(Uri.fromFile(image));
            String mBucketName;

            String path = "/"
                    + userMasterHelper.getUserMasterBO().getDownloadDate()
                    .replace("/", "") + "/"
                    + userMasterHelper.getUserMasterBO().getUserid() + "/";


            if (imageName.startsWith("AT_") || imageName.startsWith("NAT_")) {
                mBucketName = "Asset" + path + imageName;
            } else if (imageName.startsWith("NO_")) {
                mBucketName = "RetailerImages" + path + imageName;
            } else if (imageName.startsWith("SGN_")) {
                mBucketName = "Invoice" + path + imageName;
            } else if (imageName.startsWith("INIT_")) {
                mBucketName = "Initiative" + path + imageName;
            } else if (imageName.startsWith("PT_")) {
                mBucketName = "Promotion" + path + imageName;
            } else if (imageName.startsWith("SOD_")) {
                mBucketName = "SOD" + path + imageName;
            } else if (imageName.startsWith("SOS_")) {
                mBucketName = "SOS" + path + imageName;
            } else if (imageName.startsWith("SOSKU_")) {
                mBucketName = "SOSKU" + path + imageName;
            } else if (imageName.startsWith("PL_")) {
                mBucketName = "Planogram" + path + imageName;
            } else if (imageName.startsWith("VPL_")) {
                mBucketName = "VanPlanogram" + path + imageName;
            } else if (imageName.startsWith("CPL_")) {
                mBucketName = "CounterPlanogram" + path + imageName;
            } else if (imageName.startsWith("CT_")) {
                mBucketName = "Competitor" + path + imageName;
            } else if (imageName.startsWith("SVY_")) {
                mBucketName = "Survey" + path + imageName;
            } else if (imageName.startsWith("RA_")) {
                mBucketName = "RoadActivity" + path + imageName;
            } else if (imageName.startsWith("COL_")) {
                mBucketName = "Collection" + path + imageName;
            } else if (imageName.startsWith("RT_")) {
                mBucketName = "Retail" + path + imageName;
            } else if (imageName.startsWith("EXP_")) {
                mBucketName = "Expense" + path + imageName;
            } else if (imageName.startsWith("DV_")) {
                mBucketName = "Delivery" + path + imageName;
            } else if (imageName.startsWith("NP_")) {
                mBucketName = "NonProductive" + path + imageName;
            } else if (imageName.startsWith("PF_")) {
                mBucketName = "PrintFile" + path + imageName;
            } else if (imageName.startsWith("GROM_")) {
                mBucketName = "Grooming" + path + imageName;
            } else if (imageName.startsWith("PRO_")) {
                mBucketName = "Profile" + path + imageName;
            } else if (imageName.startsWith("USER_")) {
                mBucketName = "User" + path + imageName;
            } else if (imageName.startsWith("SR_SGN_")) {
                mBucketName = "SalesReturn" + path + imageName;
            } else if (imageName.startsWith("ORD_")) {
                mBucketName = "Order" + path + imageName;
            } else if (imageName.startsWith("SUR_SGN_")) {
                mBucketName = "Survey" + path + imageName;
            } else if (imageName.startsWith("ASR_")) {
                mBucketName = "AssetServiceRequest" + path + imageName;
            } else {
                if (configurationMasterHelper.IS_PHOTO_CAPTURE_IMG_PATH_CHANGE) {
                    mBucketName = "PhotoCapture" + path + imageName;
                } else {

                    mBucketName =
                            +userMasterHelper.getUserMasterBO
                                    ().getDistributorid()
                                    + "/"
                                    + userMasterHelper.getUserMasterBO().getUserid()
                                    + "/"
                                    + userMasterHelper.getUserMasterBO
                                    ().getDownloadDate()
                                    .replace("/", "") + imageName;
                }
            }

            mBucketName = DataMembers.AZURE_ROOT_DIRECTORY + "/" + mBucketName;

            CloudBlockBlob cloudBlockBlob = null;
            if (ConfigurationMasterHelper.ACCESS_KEY_ID.equalsIgnoreCase(IvyConstants.SAS_KEY_TYPE)) {
                String downloadURL = AppUtils.buildAzureUrl(mBucketName);
                cloudBlockBlob = new CloudBlockBlob(new URI(downloadURL));
            } else {
                cloudBlobContainer.getBlockBlobReference(mBucketName);
            }

            if (fileInputStream != null && cloudBlockBlob != null) {
                cloudBlockBlob.upload(fileInputStream, fileInputStream.available());
                successCount = successCount + 1;
            }

        } catch (Exception e) {
            Commons.printException(e);
        }
    }
}



