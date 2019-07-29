package com.ivy.sd.png.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.Planorama.PlanoramaActivity;
import com.ivy.cpg.view.asset.AssetTrackingActivity;
import com.ivy.cpg.view.asset.AssetTrackingHelper;
import com.ivy.cpg.view.asset.PosmTrackingActivity;
import com.ivy.cpg.view.callanalysis.CallAnalysisActivity;
import com.ivy.cpg.view.callanalysis.CallAnalysisActivityKlgs;
import com.ivy.cpg.view.callanalysis.CloseCallActivity;
import com.ivy.cpg.view.collection.CollectionHelper;
import com.ivy.cpg.view.collection.CollectionReference;
import com.ivy.cpg.view.collection.CollectionScreen;
import com.ivy.cpg.view.competitor.CompetitorTrackingActivity;
import com.ivy.cpg.view.dashboard.DashBoardHelper;
import com.ivy.cpg.view.dashboard.FitScoreDashboardActivity;
import com.ivy.cpg.view.dashboard.KellogsDashBoardActivity;
import com.ivy.cpg.view.dashboard.olddashboard.DashBoardActivity;
import com.ivy.cpg.view.dashboard.olddashboard.SKUWiseTargetActivity;
import com.ivy.cpg.view.dashboard.sellerdashboard.SellerDashBoardActivity;
import com.ivy.cpg.view.delivery.foodempire.DeliveryOrderActivity;
import com.ivy.cpg.view.delivery.invoice.DeliveryManagement;
import com.ivy.cpg.view.delivery.invoice.DeliveryManagementHelper;
import com.ivy.cpg.view.delivery.kellogs.OrderDeliveryActivity;
import com.ivy.cpg.view.delivery.kellogs.OrderDeliveryHelper;
import com.ivy.cpg.view.delivery.salesreturn.SalesReturnDeliveryActivity;
import com.ivy.cpg.view.digitalcontent.DigitalContentActivity;
import com.ivy.cpg.view.digitalcontent.DigitalContentHelper;
import com.ivy.cpg.view.digitalcontent.StoreWiseGallery;
import com.ivy.cpg.view.displayscheme.DisplaySchemeActivity;
import com.ivy.cpg.view.displayscheme.DisplaySchemeTrackingActivity;
import com.ivy.cpg.view.emptyreturn.EmptyReturnActivity;
import com.ivy.cpg.view.emptyreturn.EmptyReturnHelper;
import com.ivy.cpg.view.loyality.LoyalityHelper;
import com.ivy.cpg.view.loyality.LoyaltyPointsFragmentActivity;
import com.ivy.cpg.view.nearexpiry.NearExpiryTrackingActivity;
import com.ivy.cpg.view.nearexpiry.NearExpiryTrackingHelper;
import com.ivy.cpg.view.order.OrderHelper;
import com.ivy.cpg.view.order.OrderSummary;
import com.ivy.cpg.view.order.StockAndOrder;
import com.ivy.cpg.view.order.catalog.CatalogOrder;
import com.ivy.cpg.view.order.discount.DiscountHelper;
import com.ivy.cpg.view.order.scheme.SchemeDetailsMasterHelper;
import com.ivy.cpg.view.photocapture.Gallery;
import com.ivy.cpg.view.photocapture.PhotoCaptureHelper;
import com.ivy.cpg.view.planogram.PlanoGramActivity;
import com.ivy.cpg.view.planogram.PlanoGramHelper;
import com.ivy.cpg.view.price.PriceTrackActivity;
import com.ivy.cpg.view.price.PriceTrackCompActivity;
import com.ivy.cpg.view.price.PriceTrackingHelper;
import com.ivy.cpg.view.profile.ProfileActivity;
import com.ivy.cpg.view.promotion.PromotionHelper;
import com.ivy.cpg.view.promotion.PromotionTrackingActivity;
import com.ivy.cpg.view.retailercontract.RetailerContractActivity;
import com.ivy.cpg.view.retailercontract.RetailerContractHelper;
import com.ivy.cpg.view.salesreturn.SalesReturnActivity;
import com.ivy.cpg.view.salesreturn.SalesReturnHelper;
import com.ivy.cpg.view.serializedAsset.SerializedAssetActivity;
import com.ivy.cpg.view.serializedAsset.SerializedAssetHelper;
import com.ivy.cpg.view.sf.SODActivity;
import com.ivy.cpg.view.sf.SODAssetActivity;
import com.ivy.cpg.view.sf.SODAssetHelper;
import com.ivy.cpg.view.sf.SOSActivity;
import com.ivy.cpg.view.sf.SOSActivity_PRJSpecific;
import com.ivy.cpg.view.sf.SOSKUActivity;
import com.ivy.cpg.view.sf.SalesFundamentalHelper;
import com.ivy.cpg.view.sf.ShelfShareHelper;
import com.ivy.cpg.view.stockcheck.CombinedStockFragmentActivity;
import com.ivy.cpg.view.stockcheck.StockCheckActivity;
import com.ivy.cpg.view.stockcheck.StockCheckHelper;
import com.ivy.cpg.view.survey.SurveyActivityNew;
import com.ivy.cpg.view.survey.SurveyHelperNew;
import com.ivy.cpg.view.task.TaskHelper;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.bo.SupplierMasterBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.CompetitorTrackingHelper;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.DownloadProductsAndPrice;
import com.ivy.sd.png.provider.ProductTaggingHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.ui.AssetServiceRequest.AssetServiceRequestActivity;
import com.ivy.ui.DisplayAsset.DisplayAssetActivity;
import com.ivy.ui.DisplayAsset.DisplayAssetHelper;
import com.ivy.ui.announcement.AnnouncementConstant;
import com.ivy.ui.announcement.view.AnnouncementActivity;
import com.ivy.ui.notes.NoteConstant;
import com.ivy.ui.notes.view.NotesActivity;
import com.ivy.ui.photocapture.view.PhotoCaptureActivity;
import com.ivy.ui.reports.dynamicreport.view.DynamicReportActivity;
import com.ivy.ui.task.TaskConstant;
import com.ivy.ui.task.view.TaskActivity;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FileUtils;
import com.ivy.utils.StringUtils;
import com.ivy.utils.view.OnSingleClickListener;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;


public class HomeScreenTwo extends IvyBaseActivityNoActionBar implements SupplierSelectionDialog.UpdateSupplierName {

    public static final String MENU_STOCK = "MENU_STOCK";
    public static final String MENU_COMBINED_STOCK = "MENU_COMBINE_STKCHK";
    private static final String MENU_ORDER = "MENU_ORDER";
    private static final String MENU_COLLECTION = "MENU_COLLECTION";
    private static final String MENU_COLLECTION_REF = "MENU_COLLECTION_REF";
    private static final String MENU_CALL_ANLYS = "MENU_CALL_ANLYS";
    private static final String MENU_INVOICE = "MENU_INVOICE";
    private static final String MENU_STK_ORD = "MENU_STK_ORD";
    private static final String MENU_DGT = "MENU_DGT";
    private static final String MENU_REV = "MENU_REV";
    private static final String MENU_CLOSING = "MENU_CLOSING";
    private static final String MENU_ECALL = "MENU_ECALL";
    private static final String MENU_RECORD = "MENU_RECORD";
    private static final String MENU_LCALL = "MENU_LCALL";
    private static final String MENU_SALES_RET = "MENU_SALES_RET";
    private static final String MENU_SURVEY = "MENU_SURVEY";
    public static final String MENU_TASK = "MENU_TASK";
    public static final String MENU_PHOTO = "MENU_PHOTO";
    public static final String MENU_ASSET = "MENU_ASSET";
    private static final String MENU_STORECHECK = "MENU_STORECHECK";
    private static final String MENU_PRESENTATION = "MENU_PRESENTATION";
    public static final String MENU_PLANOGRAM = "MENU_PLANOGRAM";
    public static final String MENU_NEAREXPIRY = "MENU_NEAREXPIRY";
    private static final String MENU_SKUWISERTGT = "MENU_SKUWISERTGT";
    public static final String MENU_PRICE = "MENU_PRICE";
    public static final String MENU_PRICE_COMP = "MENU_PRICE_COMP";
    private static final String MENU_EMPTY_RETURN = "MENU_EMPTY_RETURN";
    private static final String MENU_SURVEY_QDVP3 = "MENU_SURVEY_QDVP3";
    private static final String MENU_SURVEY01 = "MENU_SURVEY01";
    private static final String MENU_QUALITY = "MENU_QUALITY";
    private static final String MENU_PERSUATION = "MENU_PERSUATION";
    private static final String MENU_CLOSE_KLGS = "MENU_CALL_ANALYS_KELGS";
    private static final String MENU_CLOSE_CALL = "MENU_CLOSE_CALL";
    private static final String MENU_POSM = "MENU_POSM";
    private static final String MENU_DELIVERY = "MENU_DELIVERY_MGMT";
    private static final String MENU_CONTRACT_VIEW = "MENU_CONTRACT_VIEW";
    private String MENU_LOYALTY_POINTS = "MENU_LOYALTY_POINTS";
    public static final String MENU_CATALOG_ORDER = "MENU_CATALOG_ORDER";
    public static final String MENU_KELLGS_DASH = "MENU_DASH_KELGS_ACT";
    public static final String MENU_RTR_KPI = "MENU_RTR_KPI";
    public static final String MENU_DASH_ACT = "MENU_DASH_ACT";
    public static final String MENU_SOD = "MENU_SOD";
    public static final String MENU_SOD_ASSET = "MENU_SOD_ASSET";
    public static final String MENU_SOS = "MENU_SOS";
    public static final String MENU_SOSKU = "MENU_SOSKU";
    public static final String MENU_COMPETITOR = "MENU_COMPETITOR";
    public static final String MENU_PROMO = "MENU_PROMO";
    public static final String MENU_SOS_PROJ = "MENU_SOS_PROJ";
    public static final String MENU_DELIVERY_ORDER = "MENU_DELIVERY_ORDER";
    public static final String MENU_FIT_DASH = "MENU_FIT_DASH";
    public static final String MENU_DISPLAY_SCH = "MENU_DISPLAY_SCH";
    public static final String MENU_DISPLAY_SCH_TRACK = "MENU_DISPLAY_SCH_TRACK";
    public static final String MENU_ORD_DELIVERY = "MENU_DELIVERY_MGMT_ORD";
    public static final String MENU_SALES_RET_DELIVERY = "MENU_SALES_RET_DELIVERY";
    public static final String MENU_SERIALIZED_ASSET = "MENU_SERIALIZED_ASSET";
    public static final String MENU_PLANORMA = "MENU_PLANORAMA";
    public static final String MENU_DISPLAY_ASSET = "MENU_DISPLAY_ASSET";
    public static final String MENU_RTR_NOTES = "MENU_NOTES";
    public static final String MENU_ASSET_SERVICE_REQUEST = "MENU_ASSET_SERVICE";
    public static final String MENU_DYNAMIC_RETAILER_DASHBOARD01 = "MENU_DYN_RET_DASH01";
    public static final String MENU_DYNAMIC_RETAILER_DASHBOARD02 = "MENU_DYN_RET_DASH02";

    private final int INVOICE_CREDIT_BALANCE = 1;// Order Not Allowed when credit balance is 0
    private final int SALES_TYPES = 2;// show preVan seller dialog
    private final int ORDER_TYPES = 3;// show Order Type Dialog
    private final int MANDATORY_MODULE_CLOSE_CALL = 4;// show Mandatory dialog while try close call when mandatory module is not completed
    private final int MANDATORY_MODULE = 5;//show Mandatory dialog with initial time(one time) in home screen Two

    // Used to map icons
    private static final HashMap<String, Integer> menuIcons = new HashMap<String, Integer>();
    private String PRE_SALES = "PreSales";
    private String VAN_SALES = "VanSales";

    private ArrayAdapter<Integer> indicativeOrderAdapter;

    private BusinessModel bmodel;
    private String title;
    private Vector<ConfigureBO> menuDB = new Vector<>();
    private String[] mSalesTypeArray = {PRE_SALES, VAN_SALES};
    private ArrayAdapter mOrderTypeAadapter;
    private ArrayList<StandardListBO> mOrderTypeList;
    private ArrayList<SupplierMasterBO> mSupplierList;
    private IconicAdapter mSchedule;
    private boolean isClick = false;
    private boolean isCreated;
    private int mOrderTypeCheckedItem = 0;
    private static final String ORDER_TYPE = "ORDER_TYPE";

    private RecyclerView activityView;
    private Vector<ConfigureBO> mInStoreMenu = new Vector<>();
    private boolean isInstoreMenuVisible;
    private AppBarLayout MyAppbar;
    private int scrollRange = -1;
    TextView retailerNameTxt, retailerCodeTxt;
    LinearLayout iconLinearLayout, retailer_name_header;
    boolean isVisible = false;
    CollapsingToolbarLayout collapsingToolbar;
    private int selecteditem = 0;
    Button callAnalysisBtn;

    private TextView mActivityDoneCount, mActivityTotalCount;

    private Vector<ConfigureBO> menuWithSequence;
    private ImageView retProfileImage;
    private ArrayAdapter<StandardListBO> mLocationAdapter;
    private ArrayAdapter<LevelBO> mCategoryAdapter;
    private int mSelectedLocationIndex = 0;
    private int mSelectedCategoryIndex = 0;
    private boolean isStoreCheckMenu = false;
    private boolean isLocDialogShow = false;
    private boolean isMandatoryDialogShow = false;
    private HashMap<String, String> menuCodeList = new HashMap<>();
    String menuCode = "";
    private SchemeDetailsMasterHelper schemeHelper;
    private CollectionHelper collectionHelper;

    private boolean isPreVisit = false;

    @SuppressLint("NewApi")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.hometwo);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        PRE_SALES = getResources().getString(R.string.sales_type_presale);
        VAN_SALES = getResources().getString(R.string.sales_type_vansale);
        mSalesTypeArray[0] = PRE_SALES;
        mSalesTypeArray[1] = VAN_SALES;

        activityView = findViewById(R.id.activity_list);
        activityView.setHasFixedSize(true);
        activityView.setNestedScrollingEnabled(false);
        isLocDialogShow = getIntent().getBooleanExtra("isLocDialog", false);
        isMandatoryDialogShow = getIntent().getBooleanExtra("isMandatoryDialog", false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        activityView.setLayoutManager(linearLayoutManager);

        isPreVisit = getIntent().getBooleanExtra("PreVisit", false);

        try {
            int length = bmodel.retailerMasterBO.getRetailerName().indexOf("/");
            if (length == -1)
                length = bmodel.retailerMasterBO.getRetailerName().length();
            title = bmodel.retailerMasterBO.getRetailerName().substring(0,
                    length);
        } catch (Exception e) {
            Commons.printException(e);
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        MyAppbar = findViewById(R.id.MyAppbar);
        retailerNameTxt = findViewById(R.id.retailer_name);
        retailerCodeTxt = findViewById(R.id.retailer_code);
        iconLinearLayout = findViewById(R.id.img_layout);
        callAnalysisBtn = findViewById(R.id.btn_call_analysis);
        retProfileImage = findViewById(R.id.retProfileImage);
        retailer_name_header = findViewById(R.id.retailer_name_header);
        setSupportActionBar(toolbar);
        retailer_name_header.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Intent prof = new Intent(HomeScreenTwo.this, ProfileActivity.class);
                prof.putExtra("hometwo", true);
                startActivity(prof);
            }
        });

        setRetailerProfileImage();
        retProfileImage.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (bmodel.retailerMasterBO.getProfileImagePath() != null &&
                        !"".equals(bmodel.retailerMasterBO.getProfileImagePath())) {
                    File filePath = null;
                    if (bmodel.profilehelper.hasProfileImagePath(bmodel.retailerMasterBO) &&
                            bmodel.retailerMasterBO.getProfileImagePath() != null && !"".equals(bmodel.retailerMasterBO.getProfileImagePath())) {
                        String[] imgPaths = bmodel.retailerMasterBO.getProfileImagePath().split("/");
                        String path = imgPaths[imgPaths.length - 1];
                        filePath = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/"
                                + DataMembers.photoFolderName + "/" + path);
                    } else if (bmodel.retailerMasterBO.getProfileImagePath() != null &&
                            !"".equals(bmodel.retailerMasterBO.getProfileImagePath())) {
                        String[] imgPaths = bmodel.retailerMasterBO.getProfileImagePath().split("/");
                        String path = imgPaths[imgPaths.length - 1];
                        filePath = new File(getExternalFilesDir(
                                Environment.DIRECTORY_DOWNLOADS)
                                + "/"
                                + bmodel.userMasterHelper.getUserMasterBO()
                                .getUserid()
                                + DataMembers.DIGITAL_CONTENT
                                + "/"
                                + DataMembers.PROFILE + "/"
                                + path);
                    }

                    if (filePath != null && filePath.exists()) {
                        try {
                            openImage(filePath.getAbsolutePath());
                        } catch (Exception e) {
                            Commons.printException("" + e);
                        }
                    } else {
                        Toast.makeText(HomeScreenTwo.this,
                                getResources().getString(R.string.unloadimage),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        callAnalysisBtn.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {

                if (isPreVisit)
                    finish();
                else {

                    for (ConfigureBO menu : menuDB) {
                        if (menu.getConfigCode().equalsIgnoreCase(MENU_CLOSE_CALL)) {
                            gotoNextActivity(menu, menu.getHasLink(), false);
                            break;
                        } else if (menu.getConfigCode().equalsIgnoreCase(MENU_CLOSE_KLGS)) {
                            gotoNextActivity(menu, menu.getHasLink(), false);
                            break;
                        } else if (menu.getConfigCode().equalsIgnoreCase(MENU_CALL_ANLYS)) {
                            gotoNextActivity(menu, menu.getHasLink(), false);
                            break;
                        }
                    }
                }
            }
        });

        try {
            if (bmodel.labelsMasterHelper.applyLabels(callAnalysisBtn.getTag()) != null)
                callAnalysisBtn.setText(bmodel.labelsMasterHelper.applyLabels(callAnalysisBtn.getTag()));
        } catch (Exception e) {
            Commons.printException(e);
        }

        retailerNameTxt.setText(title);
        if ((bmodel.retailerMasterBO.getAddress3() != null && !bmodel.retailerMasterBO.getAddress3().isEmpty())
                || (bmodel.configurationMasterHelper.SHOW_SUPPLIER_SELECTION
                && !bmodel.configurationMasterHelper.IS_APPLY_DISTRIBUTOR_WISE_PRICE
                && !bmodel.configurationMasterHelper.IS_DISTRIBUTOR_AVAILABLE)) {
            retailerCodeTxt.setVisibility(View.VISIBLE);
            if (!bmodel.configurationMasterHelper.SHOW_SUPPLIER_SELECTION)
                retailerCodeTxt.setText(bmodel.retailerMasterBO.getAddress3());
        } else {
            retailerCodeTxt.setVisibility(View.GONE);
        }
        collapsingToolbar = findViewById(R.id.collapse_toolbar);


        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        collapsingToolbar.setTitleEnabled(false);

        collapsingToolbar.setCollapsedTitleTextColor(Color.WHITE);
        collapsingToolbar.setCollapsedTitleTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        MyAppbar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {


            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    retailerNameTxt.setVisibility(View.GONE);
                    retailerCodeTxt.setVisibility(View.GONE);
                    iconLinearLayout.setVisibility(View.GONE);
                    getSupportActionBar().setTitle(title);
                    collapsingToolbar.setTitleEnabled(true);
                    isVisible = true;
                } else if (isVisible) {
                    retailerNameTxt.setVisibility(View.VISIBLE);
                    if ((bmodel.retailerMasterBO.getAddress3() != null && !bmodel.retailerMasterBO.getAddress3().isEmpty())
                            || (bmodel.configurationMasterHelper.SHOW_SUPPLIER_SELECTION
                            && !bmodel.configurationMasterHelper.IS_APPLY_DISTRIBUTOR_WISE_PRICE
                            && !bmodel.configurationMasterHelper.IS_DISTRIBUTOR_AVAILABLE)) {
                        retailerCodeTxt.setVisibility(View.VISIBLE);
                        if (!bmodel.configurationMasterHelper.SHOW_SUPPLIER_SELECTION)
                            retailerCodeTxt.setText(bmodel.retailerMasterBO.getAddress3());
                    } else {
                        retailerCodeTxt.setVisibility(View.GONE);
                    }
                    iconLinearLayout.setVisibility(View.VISIBLE);
                    getSupportActionBar().setTitle("");
                    collapsingToolbar.setTitleEnabled(false);
                    isVisible = false;
                }
            }
        });


        prepareMenuIcons();

        // Load the HHTTable
        menuDB = bmodel.configurationMasterHelper
                .downloadNewActivityMenu(ConfigurationMasterHelper.MENU_ACTIVITY);

        mInStoreMenu = bmodel.configurationMasterHelper
                .downloadStoreCheckMenu(ConfigurationMasterHelper.MENU_STORECHECK);

        try {
            if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                    R.id.label_activity_count).getTag()) != null)
                ((TextView) findViewById(R.id.label_activity_count))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(findViewById(
                                        R.id.label_activity_count)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException(e);
        }

        mActivityDoneCount = findViewById(R.id.activity_done_count);
        mActivityTotalCount = findViewById(R.id.activity_total_count);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                boolean isMoveNext = extras.getBoolean("IsMoveNextActivity", false);
                String mCurrentMenuCode = extras.getString("CurrentActivityCode", "");
                if (isMoveNext) {
                    prepareMenusInOrder();
                    ConfigureBO mNextMenu = getNextActivity(mCurrentMenuCode);
                    if (mNextMenu != null) {
                        gotoNextActivity(mNextMenu, mNextMenu.getHasLink(), false);
                    }
                }
            }
        }

        if (bmodel.configurationMasterHelper.IS_GLOBAL_LOCATION) {
            bmodel.productHelper.downloadInStoreLocations();
        }

        if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY) {
            bmodel.productHelper.downloadGloabalCategory();
        }

        // load location filter
        mLocationAdapter = new ArrayAdapter<>(this,
                android.R.layout.select_dialog_singlechoice);

        for (StandardListBO temp : bmodel.productHelper.getInStoreLocation())
            mLocationAdapter.add(temp);

        mSelectedLocationIndex = bmodel.productHelper.getmSelectedGLobalLocationIndex();

        // load Category filter
        mCategoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.select_dialog_singlechoice);

        for (LevelBO temp : bmodel.productHelper.getGlobalCategory())
            mCategoryAdapter.add(temp);

        if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY) {
            mSelectedCategoryIndex = getCategoryIndex();
            bmodel.productHelper.setmSelectedGlobalProductId(bmodel.productHelper.getGlobalCategory().get(mSelectedCategoryIndex).getProductID());
            bmodel.productHelper.setmSelectedGLobalLevelID(bmodel.productHelper.getGlobalCategory().get(mSelectedCategoryIndex).getParentID());
        }

        if (bmodel.configurationMasterHelper.SHOW_DEFAULT_LOCATION_POPUP && isLocDialogShow) {
            showLocation();
        }


    }

    private int getCategoryIndex() {
        if (bmodel.productHelper.getmSelectedGLobalLevelID() == 0 && bmodel.productHelper.getmSelectedGlobalProductId() == 0)
            return 0;
        else {
            if (bmodel.productHelper.getGlobalCategory().size() > 0) {
                for (int i = 0; i < bmodel.productHelper.getGlobalCategory().size(); i++) {
                    if (bmodel.productHelper.getGlobalCategory().get(i).getParentID() == bmodel.productHelper.getmSelectedGLobalLevelID() &&
                            bmodel.productHelper.getGlobalCategory().get(i).getProductID() == bmodel.productHelper.getmSelectedGlobalProductId())
                        return i;
                }
            }
        }

        return 0;
    }

    private void prepareMenusInOrder() {
        menuWithSequence = new Vector<>();

        for (ConfigureBO menu : menuDB) {
            if (menu.getConfigCode().equals(MENU_STORECHECK)) {
                menuWithSequence.addAll(mInStoreMenu);
            } else {
                menuWithSequence.add(menu);
            }
        }
    }

    private ConfigureBO getNextActivity(String mCurrentMenuCode) {
        int size = menuWithSequence.size();

        for (int i = 0; i < size; i++) {
            if (menuWithSequence.get(i).getConfigCode().equals(mCurrentMenuCode)) {
                if (i != (size - 1)) {
                    return menuWithSequence.get(i + 1);
                }
            }
        }
        return null;
    }


    private void prepareMenuIcons() {
        menuIcons.put(MENU_STOCK, R.drawable.activity_icon_stock_check);
        menuIcons.put(MENU_COMBINED_STOCK, R.drawable.activity_icon_stock_check);
        menuIcons.put(MENU_ORDER, R.drawable.activity_icon_order_taking);
        menuIcons.put(MENU_STK_ORD, R.drawable.activity_icon_order_taking);
        menuIcons.put(MENU_NEAREXPIRY, R.drawable.activity_icon_stock_check);
        menuIcons.put(MENU_PLANOGRAM, R.drawable.activity_icon_stock_check);
        menuIcons.put(MENU_SKUWISERTGT, R.drawable.activity_icon_stock_check);
        menuIcons.put(MENU_COLLECTION, R.drawable.activity_icon_stock_check);
        menuIcons.put(MENU_COLLECTION_REF, R.drawable.activity_icon_stock_check);
        menuIcons.put(MENU_DGT, R.drawable.activity_icon_presentation);
        menuIcons.put(MENU_CLOSING, R.drawable.activity_icon_order_taking);
        menuIcons.put(MENU_REV, R.drawable.activity_icon_presentation);
        menuIcons.put(MENU_INVOICE, R.drawable.activity_icon_presentation);
        menuIcons.put(MENU_CALL_ANLYS, R.drawable.activity_icon_survey);
        menuIcons.put(MENU_ECALL, R.drawable.activity_icon_presentation);
        menuIcons.put(MENU_RECORD, R.drawable.activity_icon_survey);
        menuIcons.put(MENU_LCALL, R.drawable.activity_icon_survey);
        menuIcons.put(MENU_SALES_RET, R.drawable.activity_icon_stock_check);
        menuIcons.put(MENU_PHOTO, R.drawable.icon_photo);
        menuIcons.put(MENU_TASK, R.drawable.activity_icon_survey);
        menuIcons.put(MENU_SURVEY, R.drawable.activity_icon_survey);
        menuIcons.put(MENU_SURVEY_QDVP3, R.drawable.activity_icon_survey);
        menuIcons.put(MENU_SURVEY01, R.drawable.activity_icon_survey);
        menuIcons.put(MENU_QUALITY, R.drawable.activity_icon_survey);
        menuIcons.put(MENU_PERSUATION, R.drawable.activity_icon_survey);
        menuIcons.put(MENU_ASSET, R.drawable.activity_icon_presentation);
        menuIcons.put(MENU_SERIALIZED_ASSET, R.drawable.activity_icon_presentation);
        menuIcons.put(MENU_POSM, R.drawable.activity_icon_presentation);
        menuIcons.put(MENU_STORECHECK, R.drawable.activity_icon_order_taking);
        menuIcons.put(MENU_PRESENTATION, R.drawable.activity_icon_presentation);
        menuIcons.put(MENU_PROMO,
                R.drawable.activity_icon_order_taking);
        menuIcons.put(StandardListMasterConstants.MENU_COLLECTION_VIEW,
                R.drawable.activity_icon_order_taking);
        menuIcons.put(StandardListMasterConstants.MENU_STOCK_REPLACEMENT,
                R.drawable.activity_icon_survey);
        menuIcons.put(MENU_PRICE, R.drawable.activity_icon_survey);
        menuIcons.put(MENU_PRICE_COMP, R.drawable.activity_icon_survey);
        menuIcons.put(MENU_EMPTY_RETURN, R.drawable.activity_icon_order_taking);
        menuIcons.put(MENU_SOS, R.drawable.activity_icon_order_taking);
        menuIcons.put(MENU_SOD, R.drawable.activity_icon_order_taking);
        menuIcons.put(MENU_SOD_ASSET, R.drawable.activity_icon_survey);
        menuIcons
                .put(MENU_SOSKU, R.drawable.activity_icon_order_taking);
        menuIcons.put(MENU_COMPETITOR,
                R.drawable.activity_icon_order_taking);
        menuIcons.put(MENU_CLOSE_CALL,
                R.drawable.activity_icon_order_taking);
        menuIcons.put(MENU_CLOSE_KLGS, R.drawable.activity_icon_order_taking);
        menuIcons.put(MENU_DELIVERY, R.drawable.activity_icon_order_taking);
        menuIcons.put(MENU_CONTRACT_VIEW, R.drawable.activity_icon_survey);
        menuIcons.put(MENU_LOYALTY_POINTS, R.drawable.activity_icon_order_taking);

        menuIcons.put(MENU_CATALOG_ORDER, R.drawable.activity_icon_order_taking);
        menuIcons.put(MENU_KELLGS_DASH, R.drawable.icon_dash);
        menuIcons.put(MENU_RTR_KPI, R.drawable.icon_dash);
        menuIcons.put(MENU_DASH_ACT, R.drawable.icon_dash);
        menuIcons.put(MENU_FIT_DASH, R.drawable.icon_dash);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        if (bmodel.getRetailerMasterBO().getHasPaymentIssue() == 1) {
            Toast.makeText(this, "Collection check bounced ", Toast.LENGTH_SHORT).show();
        }

        bmodel.configurationMasterHelper.IS_INVOICE_AS_MOD = isInvoiceAsModule();
        // Create a boolean array with menus and set to true if the menu is
        // covered
        updateMenuVisitStatus(menuDB);
        updateMenuVisitStatus(mInStoreMenu);

        String rSalesType = bmodel.getStandardListCode(bmodel.getRetailerMasterBO().getSalesTypeId());
        if (bmodel.configurationMasterHelper.IS_SHOW_RID_CONCEDER_AS_DSTID && rSalesType.equalsIgnoreCase("INDIRECT")) {

            bmodel.retailerMasterBO.setDistributorId(SDUtil.convertToInt(bmodel.retailerMasterBO.getRetailerID()));
            bmodel.retailerMasterBO.setDistParentId(0);
            if (bmodel.retailerMasterBO.getAddress3() != null)
                retailerCodeTxt.setText(bmodel.retailerMasterBO.getAddress3());


        } else {
            if (!bmodel.configurationMasterHelper.IS_APPLY_DISTRIBUTOR_WISE_PRICE
                    && !bmodel.configurationMasterHelper.IS_DISTRIBUTOR_AVAILABLE) {
                mSupplierList = bmodel.downloadSupplierDetails();
                if (mSupplierList != null && mSupplierList.size() > 0)
                    updateDefaultSupplierSelection();
            }
        }
        /*if (bmodel.configurationMasterHelper.SHOW_CAPTURED_LOCATION) {
            int permissionStatus = ContextCompat.checkSelfPermission(HomeScreenTwo.this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionStatus == PackageManager.PERMISSION_GRANTED)
                bmodel.locationUtil.stopLocationListener();
        }*/

        //Clearing appllied scheme list to prevent it from listing on other common print type(Credit note,..).
        schemeHelper = SchemeDetailsMasterHelper.getInstance(getApplicationContext());
        schemeHelper.getAppliedSchemeList().clear();

        collectionHelper = CollectionHelper.getInstance(this);

        if (bmodel.configurationMasterHelper.SHOW_ORDER_TYPE_DIALOG) {
            mOrderTypeList = bmodel.productHelper.getTypeList(ORDER_TYPE);
            if (mOrderTypeList != null && mOrderTypeList.size() > 0) {
                mOrderTypeAadapter = new ArrayAdapter<>(this,
                        R.layout.supplier_selection_list_adapter, mOrderTypeList);
                if (bmodel.getRetailerMasterBO().getOrderTypeId() != null) {
                    for (int i = 0; i < mOrderTypeList.size(); i++) {

                        if (bmodel.getRetailerMasterBO().getOrderTypeId().equals(mOrderTypeList.get(i).getListID())) {
                            mOrderTypeCheckedItem = i;
                            break;

                        }

                    }
                } else {
                    bmodel.getRetailerMasterBO().setOrderTypeId(mOrderTypeList.get(0).getListID());
                    mOrderTypeCheckedItem = 0;
                }
            }
        } else {
            bmodel.getRetailerMasterBO().setOrderTypeId(0 + "");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isClick = false;
        isCreated = false;
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        updateMenuVisitStatus(menuDB);
        updateMenuVisitStatus(mInStoreMenu);


        mSchedule = new IconicAdapter(mInStoreMenu);

        Vector<ConfigureBO> mTempMenuList = new Vector<>(menuDB);


        for (ConfigureBO menu : menuDB) {
            if (menu.getConfigCode().equalsIgnoreCase(MENU_STORECHECK))
                isStoreCheckMenu = true;
            if (menu.getConfigCode().equalsIgnoreCase(MENU_CLOSE_CALL)
                    || menu.getConfigCode().equalsIgnoreCase(MENU_CALL_ANLYS)) {
                mTempMenuList.remove(menu);
            }
        }
        Vector<ConfigureBO> mTempMenuStoreList = new Vector<>(mInStoreMenu);
        for (ConfigureBO storeMenu : mInStoreMenu) {
            if (storeMenu.getConfigCode().equalsIgnoreCase("MENU_CLOSE")) {
                mTempMenuStoreList.remove(storeMenu);
            }
        }


        ActivityAdapter mActivityAdapter = new ActivityAdapter(mTempMenuList);
        activityView.setAdapter(mActivityAdapter);

        int totalVisitCount = getMenuVisitCount(mTempMenuList) + getStoreMenuVisitCount(mTempMenuStoreList);

        mActivityDoneCount.setText(new DecimalFormat("0").format((isStoreCheckMenu ? (totalVisitCount != 0 ? (getStoreMenuVisitCount(mTempMenuStoreList) > 0 ? totalVisitCount - 1 : totalVisitCount) : 0) : totalVisitCount)));

        mActivityTotalCount.setText(String.valueOf("/" + ((isStoreCheckMenu ? mTempMenuList.size() - 1 : mTempMenuList.size()) + mTempMenuStoreList.size())));

        // this dialog will return when mandatory module is not completed otherwise not show
        if (isMandatoryDialogShow && bmodel.configurationMasterHelper.IS_CHECK_MODULE_MANDATORY)
            onCreateDialog(MANDATORY_MODULE);

    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.supplier_new);
        drawable.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.white), PorterDuff.Mode.SRC_ATOP);
        menu.findItem(R.id.menu_supplier_selection).setIcon(drawable);

        if (!bmodel.configurationMasterHelper.SHOW_DGTC)
            menu.findItem(R.id.menu_dgtc).setVisible(false);
        if (!bmodel.configurationMasterHelper.IS_PHOTO_CAPTURE)
            menu.findItem(R.id.menu_photo).setVisible(false);
        if (!bmodel.configurationMasterHelper.IS_TASK)
            menu.findItem(R.id.menu_reminder).setVisible(false);
        if (!bmodel.configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED)
            menu.findItem(R.id.menu_sales_selection).setVisible(false);
        if (!bmodel.configurationMasterHelper.SHOW_SUPPLIER_SELECTION || bmodel.configurationMasterHelper.IS_APPLY_DISTRIBUTOR_WISE_PRICE)
            menu.findItem(R.id.menu_supplier_selection).setVisible(false);

        if (bmodel.configurationMasterHelper.IS_SHOW_RID_CONCEDER_AS_DSTID && (bmodel.getStandardListCode(bmodel.getRetailerMasterBO().getSalesTypeId()).equalsIgnoreCase("INDIRECT")))
            menu.findItem(R.id.menu_supplier_selection).setVisible(false);

        if (!bmodel.configurationMasterHelper.IS_DIGITAL_CONTENT)
            menu.findItem(R.id.menu_digital_content).setVisible(false);
        if (!bmodel.configurationMasterHelper.SHOW_ORDER_TYPE_DIALOG)
            menu.findItem(R.id.menu_order_type).setVisible(false);

        if (!bmodel.configurationMasterHelper.IS_GLOBAL_LOCATION)
            menu.findItem(R.id.menu_loc_filter).setVisible(false);
        else {
            if (bmodel.productHelper.getInStoreLocation().size() < 2)
                menu.findItem(R.id.menu_loc_filter).setVisible(false);
        }

        if (!bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY)
            menu.findItem(R.id.menu_category_filter).setVisible(false);
        else {
            if (bmodel.productHelper.getGlobalCategory().size() == 0)
                menu.findItem(R.id.menu_category_filter).setVisible(false);
        }

        if (bmodel.configurationMasterHelper.IS_SHOW_ANNOUNCEMENT) {
            MenuItem item = menu.findItem(R.id.menu_announcement);
            item.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
            item.setVisible(true);
        }

        Commons.print("icon" + bmodel.configurationMasterHelper.SHOW_DGTC);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_homescreen_two, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i1 = item.getItemId();
        if (i1 == android.R.id.home) {
            return true;
        } else if (i1 == R.id.menu_reminder) {
            if (!isClick) {
                isClick = true;
                if (TaskHelper.getInstance(this).getTaskData(bmodel.getRetailerMasterBO().getRetailerID()).size() > 0) {

                    if (!isPreVisit)
                        bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                                DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                                DateTimeUtils.now(DateTimeUtils.TIME), MENU_TASK);

                    Intent intent = new Intent(HomeScreenTwo.this, TaskActivity.class);

                    if (isPreVisit)
                        intent.putExtra("PreVisit", true);

                    intent.putExtra(TaskConstant.RETAILER_WISE_TASK, true);
                    intent.putExtra(TaskConstant.MENU_CODE, MENU_TASK);
                    startActivity(intent);

                    finish();
                }

            }
            return true;
        } else if (i1 == R.id.menu_loc_filter) {
            if (!isClick) {
                isClick = true;
                showLocation();
            }
            return true;
        } else if (i1 == R.id.menu_category_filter) {
            if (!isClick) {
                isClick = true;
                showCategory();
            }
            return true;
        } else if (i1 == R.id.menu_photo && !isPreVisit) {
            int count = bmodel.synchronizationHelper.getImagesCount();

            if (!isClick) {
                isClick = true;

                if (count >= 10
                        && count <= bmodel.configurationMasterHelper.photocount) {

                    Toast.makeText(
                            this,
                            getResources()
                                    .getString(
                                            R.string.its_highly_recommend_you_to_upload_the_images_before_capturing_new_image),
                            Toast.LENGTH_LONG).show();

                    bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                            DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                            DateTimeUtils.now(DateTimeUtils.TIME), MENU_PHOTO);
                    startActivity(new Intent(HomeScreenTwo.this,
                            com.ivy.ui.photocapture.view.PhotoCaptureActivity.class).putExtra("isFromMenuClick", true));
                    finish();

                } else if (count >= bmodel.configurationMasterHelper.photocount) {

                    showGalleryAlert(
                            getResources()
                                    .getString(
                                            R.string.maximum_number_of_images_has_been_captured_without_upload_Do_upload_or_delete_images),
                            0);
                    isClick = false;

                } else {

                    bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                            DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                            DateTimeUtils.now(DateTimeUtils.TIME), MENU_PHOTO);
                    startActivity(new Intent(HomeScreenTwo.this,
                            com.ivy.ui.photocapture.view.PhotoCaptureActivity.class).putExtra("isFromMenuClick", true));
                    finish();
                }
            }
            return true;
        } else if (i1 == R.id.menu_sales_selection) {
            showDialog(SALES_TYPES);
            return true;
        } else if (i1 == R.id.menu_supplier_selection) {

            SupplierSelectionDialog dialog = new SupplierSelectionDialog(HomeScreenTwo.this, mSupplierList);
            dialog.show();

            return true;
        } else if (i1 == R.id.menu_dgtc && !isPreVisit) {
            if (!isClick) {
                isClick = true;
                Intent intent = new Intent(HomeScreenTwo.this,
                        StoreWiseGallery.class);
                startActivity(intent);
                finish();
            }
            return true;
        } else if (i1 == R.id.menu_digital_content && !isPreVisit) {
            if (!isClick) {
                isClick = true;
                DigitalContentHelper mDigitalContentHelper = DigitalContentHelper.getInstance(this);
                mDigitalContentHelper.downloadDigitalContent(getApplicationContext(), "RETAILER");
                if (mDigitalContentHelper.getDigitalMaster() != null
                        && mDigitalContentHelper.getDigitalMaster()
                        .size() > 0) {
                    bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                            DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                            DateTimeUtils.now(DateTimeUtils.TIME), MENU_DGT);
                    Intent i = new Intent(HomeScreenTwo.this,
                            DigitalContentActivity.class);
                    i.putExtra("FromDigi", "Digi");
                    startActivity(i);
                    finish();
                } else {
                    dataNotMapped();
                    isClick = false;
                }
            }
            return true;
        } else if (i1 == R.id.menu_order_type) {
            if (mOrderTypeList != null && mOrderTypeList.size() > 0) {
                showDialog(ORDER_TYPES);
            } else {
                Toast.makeText(this, "No order type available ", Toast.LENGTH_SHORT).show();
            }

        } else if (i1 == R.id.menu_profile_view && !isPreVisit) {
            Intent prof = new Intent(HomeScreenTwo.this, ProfileActivity.class);
            prof.putExtra("hometwo", true);
            startActivity(prof);
        } else if (i1 == R.id.menu_announcement) {
            Intent i = new Intent(this,
                    AnnouncementActivity.class);
            i.putExtra(AnnouncementConstant.SCREEN_TITLE, getString(R.string.announcement));
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateDefaultSupplierSelection() {
        try {
            int mDefaultSupplierSelection = bmodel.getSupplierPosition(mSupplierList);
            if (mSupplierList != null && mSupplierList.size() > 0) {
                bmodel.getRetailerMasterBO().setSupplierBO(
                        mSupplierList.get(mDefaultSupplierSelection));
                bmodel.getRetailerMasterBO().setDistributorId(mSupplierList.get(mDefaultSupplierSelection).getSupplierID());
                bmodel.getRetailerMasterBO().setDistParentId(mSupplierList.get(mDefaultSupplierSelection).getDistParentID());
                bmodel.getRetailerMasterBO().setSupplierTaxLocId(mSupplierList.get(mDefaultSupplierSelection).getSupplierTaxLocId());
                retailerCodeTxt.setText(mSupplierList.get(mDefaultSupplierSelection).getSupplierName());
            }
        } catch (Exception ex) {
            Commons.printException(ex);
        }
    }

    private int getMenuVisitCount(Vector<ConfigureBO> menuDB) {
        int mMneuDoneCount = 0;
        for (ConfigureBO menu : menuDB) {
            if (menu.isDone())
                mMneuDoneCount++;
        }
        return mMneuDoneCount;
    }

    private int getStoreMenuVisitCount(Vector<ConfigureBO> storeMenuDB) {
        int mMneuDoneCount = 0;
        for (ConfigureBO menu : storeMenuDB) {
            if (menu.isDone())
                mMneuDoneCount++;
        }
        return mMneuDoneCount;
    }

    /**
     * This method will update isDone flag of the menu. IsDone flag is used to
     * fill the <b>color </b> if the activity is completed. <br>
     * If any new menu is introduced, then it has mapped here. Otherwise menu
     * will never get highlighted if its done.<br>
     * For the Disabled menus the isDone flag will be updated based on the
     * previous menu isDone flag.
     */
    private void updateMenuVisitStatus(Vector<ConfigureBO> menuDB) {
        int size = menuDB.size();

        bmodel.isModuleDone(true);

        try {
            for (int i = 0; i < size; i++) {
                menuDB.get(i).setDone(false);
            }

            if (!menuDB.isEmpty() && menuDB.get(0).getHasLink() == 0) {
                menuDB.get(0).setDone(true);
            }
            for (int i = 0; i < size; i++) {
                // menuDB.get(i).setDone(false);

                if (menuDB.get(i).getConfigCode().equals(MENU_COLLECTION)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.isModuleCompleted(menuDB.get(i).getConfigCode()))
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                } else if (menuDB.get(i).getConfigCode().equals(MENU_COLLECTION_REF)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.isModuleCompleted(menuDB.get(i).getConfigCode()))
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                } else if (menuDB
                        .get(i)
                        .getConfigCode()
                        .equals(StandardListMasterConstants.MENU_COLLECTION_VIEW)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.getRetailerMasterBO().getIsCollectionView()
                                .equals("Y"))
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                } else if (menuDB.get(i).getConfigCode().equals(MENU_SALES_RET)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.isModuleCompleted(menuDB.get(i).getConfigCode()))
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                } else if (menuDB
                        .get(i)
                        .getConfigCode()
                        .equals(StandardListMasterConstants.MENU_STOCK_REPLACEMENT)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (SalesReturnHelper.getInstance(this).isStockReplacementDone(getApplicationContext()))
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                } else if (menuDB.get(i).getConfigCode().equals(MENU_SURVEY)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.isModuleCompleted(menuDB.get(i).getConfigCode()))
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                } else if (menuDB.get(i).getConfigCode().equals(MENU_CONTRACT_VIEW)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.isContractRenewalDone())
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                } else if (menuDB.get(i).getConfigCode()
                        .equals(MENU_SURVEY_QDVP3)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.isSurveyDone(MENU_SURVEY_QDVP3) || bmodel.isModuleCompleted(menuDB.get(i).getConfigCode()))
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                } else if (menuDB.get(i).getConfigCode().equals(MENU_SURVEY01)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.isSurveyDone(MENU_SURVEY01) || bmodel.isModuleCompleted(menuDB.get(i).getConfigCode()))
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                } else if (menuDB.get(i).getConfigCode().equals(MENU_QUALITY)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.isSurveyDone(MENU_QUALITY) || bmodel.isModuleCompleted(menuDB.get(i).getConfigCode()))
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                } else if (menuDB.get(i).getConfigCode()
                        .equals(MENU_PERSUATION)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.isSurveyDone(MENU_PERSUATION) || bmodel.isModuleCompleted(menuDB.get(i).getConfigCode()))
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                } else if (menuDB.get(i).getConfigCode().equals(MENU_INVOICE)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.getRetailerMasterBO().getIsInvoiceDone()
                                .equals("Y"))
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                } else if (menuDB.get(i).getConfigCode().equals(MENU_ORDER)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.getRetailerMasterBO().getIsOrdered()
                                .equals("Y")
                                || bmodel.getRetailerMasterBO()
                                .getIsOrderMerch().equals("Y") || bmodel.isModuleCompleted(menuDB.get(i).getConfigCode()))
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                } else if (menuDB.get(i).getConfigCode().equals(MENU_CATALOG_ORDER)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.getRetailerMasterBO().getIsOrdered()
                                .equals("Y")
                                || bmodel.getRetailerMasterBO()
                                .getIsOrderMerch().equals("Y"))
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                } else if (menuDB.get(i).getConfigCode().equals(MENU_REV)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.getRetailerMasterBO().getIsReviewPlan()
                                .equals("Y"))
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }

                } else if (menuDB.get(i).getConfigCode().equals(MENU_CLOSING)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.getRetailerMasterBO().getIsOrdered()
                                .equals("Y")
                                || bmodel.getRetailerMasterBO()
                                .getIsOrderMerch().equals("Y"))
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                } else if (menuDB.get(i).getConfigCode().equals(MENU_STK_ORD)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.getRetailerMasterBO().getIsOrdered()
                                .equals("Y")
                                || bmodel.getRetailerMasterBO()
                                .getIsOrderMerch().equals("Y") || bmodel.isModuleCompleted(menuDB.get(i).getConfigCode()))
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                } else if (menuDB.get(i).getConfigCode().equals(MENU_STOCK)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.isModuleCompleted(menuDB.get(i).getConfigCode()))
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                } else if (menuDB.get(i).getConfigCode().equals(MENU_COMBINED_STOCK)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.isModuleCompleted(menuDB.get(i).getConfigCode()))
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                } else if (menuDB.get(i).getConfigCode().equals(MENU_DGT)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.getRetailerMasterBO().getIsDigitalContent()
                                .equals("Y")) {
                            menuDB.get(i).setDone(true);
                        }
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                } else if (menuDB.get(i).getConfigCode().equals(MENU_RECORD)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.getRetailerMasterBO().getIsOrderMerch()
                                .equals("Y"))
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                } else if (menuDB.get(i).getConfigCode().equals(MENU_ECALL)) {
                    if (menuDB.get(i).getHasLink() == 1) {

                        if ((bmodel.getRetailerMasterBO().getIsOrdered()
                                .equals("Y"))
                                || (bmodel.getRetailerMasterBO()
                                .getIsOrderMerch().equals("Y")))
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                } else if (menuDB.get(i).getConfigCode().equals(MENU_LCALL)) {

                    if (getPreviousMenuBO(menuDB.get(i)).isDone())
                        menuDB.get(i).setDone(true);

                } else if (menuDB.get(i).getConfigCode().equals(MENU_PHOTO)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.isModuleCompleted(menuDB.get(i).getConfigCode()))
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                } else if (menuDB.get(i).getConfigCode()
                        .equals(MENU_KELLGS_DASH)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.isModuleCompleted(menuDB.get(i).getConfigCode()))
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                } else if (menuDB.get(i).getConfigCode().equals(MENU_ASSET)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.isModuleCompleted(menuDB.get(i).getConfigCode()))
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                } else if (menuDB.get(i).getConfigCode().equals(MENU_SERIALIZED_ASSET)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.isModuleCompleted(menuDB.get(i).getConfigCode()))
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                } else if (menuDB.get(i).getConfigCode().equals(MENU_POSM)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.isModuleCompleted(menuDB.get(i).getConfigCode()))
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                } else if (menuDB.get(i).getConfigCode()
                        .equals(MENU_NEAREXPIRY)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.isModuleCompleted(menuDB.get(i).getConfigCode()))
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }

                } else if (menuDB.get(i).getConfigCode().equals(MENU_PLANOGRAM)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.isModuleCompleted(menuDB.get(i).getConfigCode()))
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }

                } else if (menuDB.get(i).getConfigCode()
                        .equals(MENU_SKUWISERTGT)) {

                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.getRetailerMasterBO().getIsSKUTGT()
                                .equals("Y"))
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                } else if (menuDB.get(i).getConfigCode()
                        .equals(MENU_PRESENTATION)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.isModuleCompleted(menuDB.get(i).getConfigCode()))
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                } else if (menuDB.get(i).getConfigCode()
                        .equals(MENU_STORECHECK)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (isStoreCheckMoudleCompleted()) {
                            menuDB.get(i).setDone(true);
                        }
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }

                } else if (menuDB.get(i).getConfigCode().equals(MENU_PRICE)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.isModuleCompleted(menuDB.get(i).getConfigCode()))
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                } else if (menuDB.get(i).getConfigCode().equals(MENU_PRICE_COMP)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.isModuleCompleted(menuDB.get(i).getConfigCode()))
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                } else if (menuDB.get(i).getConfigCode()
                        .equals(MENU_EMPTY_RETURN)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.isModuleCompleted(menuDB.get(i).getConfigCode()))
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                } else if (menuDB.get(i).getConfigCode()
                        .equals(MENU_PROMO)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.isModuleCompleted(menuDB.get(i).getConfigCode()))
                            menuDB.get(i).setDone(true);
                        //
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                } else if (menuDB.get(i).getConfigCode()
                        .equals(MENU_SOS)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.isModuleCompleted(menuDB.get(i).getConfigCode()))
                            menuDB.get(i).setDone(true);
                        //

                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                } else if (menuDB.get(i).getConfigCode()
                        .equals(MENU_SOD)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.isModuleCompleted(menuDB.get(i).getConfigCode()))
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                } else if (menuDB.get(i).getConfigCode()
                        .equals(MENU_SOD_ASSET)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.isModuleCompleted(menuDB.get(i).getConfigCode()))
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                } else if (menuDB.get(i).getConfigCode()
                        .equals(MENU_SOSKU)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.isModuleCompleted(menuDB.get(i).getConfigCode()))
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                } else if (menuDB.get(i).getConfigCode()
                        .equals(MENU_COMPETITOR)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.isModuleCompleted(menuDB.get(i).getConfigCode()))
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);

                    }
                } else if (menuDB.get(i).getConfigCode().equals(MENU_TASK)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.isModuleCompleted(menuDB.get(i).getConfigCode()))
                            menuDB.get(i).setDone(true);

                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }


                } else if (menuDB.get(i).getConfigCode().equals(MENU_DELIVERY)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.isModuleCompleted(menuDB.get(i).getConfigCode()))
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                } else if (menuDB.get(i).getConfigCode().equals(MENU_CONTRACT_VIEW)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (DeliveryManagementHelper.getInstance(this).isDeliveryMgtDone())
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                } else if (menuDB.get(i).getConfigCode().equals(MENU_LOYALTY_POINTS)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.isModuleCompleted(menuDB.get(i).getConfigCode()))
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                } else if (menuDB.get(i).getConfigCode().equals(MENU_SOS_PROJ)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.isModuleCompleted(menuDB.get(i).getConfigCode()))
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                } else if (menuDB.get(i).getConfigCode().equals(MENU_RTR_KPI)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.isModuleCompleted(menuDB.get(i).getConfigCode()))
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                } else if (menuDB.get(i).getConfigCode().equals(MENU_DASH_ACT)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.getRetailerMasterBO().getIsOrdered()
                                .equals("Y")
                                || bmodel.getRetailerMasterBO()
                                .getIsOrderMerch().equals("Y") || bmodel.isModuleCompleted(menuDB.get(i).getConfigCode()))
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                } else if (menuDB.get(i).getConfigCode()
                        .equals(MENU_DELIVERY_ORDER)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.isModuleCompleted(menuDB.get(i).getConfigCode()))
                            menuDB.get(i).setDone(true);
                        //
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                } else if (menuDB.get(i).getConfigCode()
                        .equals(MENU_FIT_DASH)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.isModuleCompleted(menuDB.get(i).getConfigCode()))
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                } else if (menuDB.get(i).getConfigCode().equals(MENU_DISPLAY_SCH)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.isModuleCompleted(menuDB.get(i).getConfigCode()))
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                } else if (menuDB.get(i).getConfigCode().equals(MENU_DISPLAY_SCH_TRACK)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.isModuleCompleted(menuDB.get(i).getConfigCode()))
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                } else if (menuDB.get(i).getConfigCode().equals(MENU_ORD_DELIVERY)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.isModuleCompleted(menuDB.get(i).getConfigCode()))
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                } else if (menuDB.get(i).getConfigCode().equals(MENU_SALES_RET_DELIVERY)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.isModuleCompleted(menuDB.get(i).getConfigCode()))
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                } else if (menuDB.get(i).getConfigCode().equals(MENU_DISPLAY_ASSET)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.isModuleCompleted(menuDB.get(i).getConfigCode()))
                            menuDB.get(i).setDone(true);
                    } else {
                        if (getPreviousMenuBO(menuDB.get(i)).isDone())
                            menuDB.get(i).setDone(true);
                    }
                }

            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private ConfigureBO getPreviousMenuBO(ConfigureBO config) {

        try {
            for (int i = 0; i < menuDB.size(); i++) {
                if (menuDB.get(i).getConfigCode()
                        .equals(config.getConfigCode())) {
                    if (menuDB.get(i - 1).getConfigCode().equals(MENU_INVOICE)) {
                        return menuDB.get(i - 2);
                    } else {
                        return menuDB.get(i - 1);
                    }
                }
            }
        } catch (Exception e) {
            return menuDB.get(0);
        }
        return menuDB.get(0);
    }

    /**
     * This method will have all the activity links. Based on the Menu code
     * activity will be called. Following Menu are configured
     * <p/>
     * STOCK <br>
     * ORDER <br>
     * STOCK_ORDER <br>
     * COLLECTION <br>
     * SALES RETURN <br>
     * INVOICE <br>
     * TASK <br>
     * SURVEY <br>
     * PHOTOCAPTURE <br>
     * CLOSING - ORDER SUMMARY <br>
     * REVIEW <br>
     * CALL ANALYSIS <br>
     * DIGITAL CONTENT <br>
     *
     * @param --menuCode
     */
    private void gotoNextActivity(ConfigureBO menu, int hasLink, boolean isFromChild) {


        //location dialog show from store click
        isLocDialogShow = false;

        // this conditon added to load download product
        // filter method once when GLOBAL CATEGORY SELECTION enabled
        if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY) {
            if (menu.getConfigCode().equals(MENU_STOCK)
                    || menu.getConfigCode().equals(MENU_COMBINED_STOCK)
                    || menu.getConfigCode().equals(MENU_ORDER)
                    || menu.getConfigCode().equals(MENU_STK_ORD)
                    || menu.getConfigCode().equals(MENU_CATALOG_ORDER)
                    || menu.getConfigCode().equals(MENU_SALES_RET)
                    || menu.getConfigCode().equals(MENU_NEAREXPIRY)
                    || menu.getConfigCode().equals(MENU_PRICE)
                    || menu.getConfigCode().equals(MENU_PRICE_COMP)
                    || menu.getConfigCode().equals(MENU_EMPTY_RETURN)
                    || menu.getConfigCode().equals(MENU_DELIVERY)
                    || menu.getConfigCode().equals(MENU_DGT)
                    && hasLink == 1) {
                if (bmodel.productHelper.getmLoadedGlobalProductId() != bmodel.productHelper.getmSelectedGlobalProductId()) {

                   /* GenericObjectPair<Vector<ProductMasterBO>, Map<String, ProductMasterBO>> genericObjectPair = bmodel.productHelper.downloadProducts(MENU_STK_ORD);
                    if (genericObjectPair != null) {
                        bmodel.productHelper.setProductMaster(genericObjectPair.object1);
                        bmodel.productHelper.setProductMasterById(genericObjectPair.object2);
                    }*/
                    bmodel.productHelper.setFilterProductLevels(bmodel.productHelper.downloadFilterLevel(MENU_STK_ORD));
                    bmodel.productHelper.setFilterProductsByLevelId(bmodel.productHelper.downloadFilterLevelProducts(
                            bmodel.productHelper.getFilterProductLevels(), true));
                }

            }
        }
        if ((menu.getConfigCode().equals(MENU_STOCK)
                || menu.getConfigCode().equals(MENU_COMBINED_STOCK)) && hasLink == 1) {

            load_MENU_STOCK(menu, isFromChild);

        } else if (menu.getConfigCode().equals(MENU_ORDER) && hasLink == 1 && !isPreVisit) {

            load_MENU_ORDER(menu);

        } else if ((menu.getConfigCode().equals(MENU_STK_ORD)
                || menu.getConfigCode().equals(MENU_CATALOG_ORDER) && hasLink == 1) && !isPreVisit) {
            StockCheckHelper.getInstance(HomeScreenTwo.this).loadStockCheckConfiguration(HomeScreenTwo.this, bmodel.retailerMasterBO.getSubchannelid());
            DigitalContentHelper.getInstance(HomeScreenTwo.this).loadFloatingDgtConfig(HomeScreenTwo.this);
            new StockAndOrderTask(menu, this).execute();
            // moveToStockAndOrder(menu);
        } else if (menu.getConfigCode().equals(MENU_CLOSING) && hasLink == 1 && !isPreVisit) {

            load_MENU_CLOSING(menu);

        } else if ((menu.getConfigCode().equals(MENU_SURVEY)
                || menu.getConfigCode().equals(MENU_SURVEY01)
                || menu.getConfigCode().equals(MENU_QUALITY)
                || menu.getConfigCode().equals(MENU_PERSUATION)
                || menu.getConfigCode().equals(MENU_SURVEY_QDVP3))
                && hasLink == 1) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP || isPreVisit) {

                SurveyHelperNew surveyHelperNew = SurveyHelperNew.getInstance(this);

                surveyHelperNew.setFromHomeScreen(false);
                surveyHelperNew.downloadModuleId("STANDARD");

                bmodel.configurationMasterHelper.downloadFloatingNPReasonWithPhoto(menu.getConfigCode());
                chooseFilterType(menu.getConfigCode());

                surveyHelperNew.downloadQuestionDetails(menu.getConfigCode());
                surveyHelperNew.loadSurveyAnswers(0);

                if (!isClick) {
                    isClick = true;
                    if (surveyHelperNew.getSurvey() != null
                            && surveyHelperNew.getSurvey().size() > 0) {
                        bmodel.mSelectedActivityName = menu.getMenuName();
                        bmodel.mSelectedActivityConfigCode = menu.getConfigCode();
                        surveyHelperNew.loadSurveyConfig(menu
                                .getConfigCode());

                        if (!isPreVisit)
                            bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                                    DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                                    DateTimeUtils.now(DateTimeUtils.TIME), menu.getConfigCode());

                        Intent intent = new Intent(HomeScreenTwo.this,
                                SurveyActivityNew.class);

                        if (isPreVisit)
                            intent.putExtra("PreVisit", true);

                        intent.putExtra("screentitle", menu.getMenuName());
                        intent.putExtra("SurveyType", 0);
                        intent.putExtra("menucode", menu.getConfigCode());
                        intent.putExtra("from", "HomeScreenTwo");
                        intent.putExtra("CurrentActivityCode", menu.getConfigCode());
                        if (isFromChild)
                            intent.putExtra("isFromChild", true);
                        startActivity(intent);
                        finish();
                        isCreated = true;
                    } else {
                        dataNotMapped();
                        isCreated = false;
                        isClick = false;
                        menuCode = (menuCodeList.get(menu.getConfigCode()) == null ? "" : menuCodeList.get(menu.getConfigCode()));
                        if (!menuCode.equals(menu.getConfigCode()))
                            menuCodeList.put(menu.getConfigCode(), menu.getConfigCode());
                    }
                }
            } else {
                Toast.makeText(
                        this,
                        getResources().getString(
                                R.string.please_complete_previous_activity),
                        Toast.LENGTH_SHORT).show();
                isCreated = false;
            }

        } else if (menu.getConfigCode().equals(MENU_TASK) && hasLink == 1) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP || isPreVisit) {
                if (!isClick) {
                    isClick = true;
                    // finish();
                    bmodel.configurationMasterHelper.downloadFloatingNPReasonWithPhoto(MENU_TASK);

                    if (!isPreVisit)
                        bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                                DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                                DateTimeUtils.now(DateTimeUtils.TIME), menu.getConfigCode());

                    Intent intent = new Intent(HomeScreenTwo.this,
                            TaskActivity.class);
                    intent.putExtra(TaskConstant.CURRENT_ACTIVITY_CODE, menu.getConfigCode());
                    intent.putExtra(TaskConstant.MENU_CODE, menu.getConfigCode());
                    intent.putExtra(TaskConstant.RETAILER_WISE_TASK, true);
                    intent.putExtra(TaskConstant.SCREEN_TITLE, menu.getMenuName());

                    if (isPreVisit)
                        intent.putExtra("PreVisit", true);

                    startActivity(intent);
                    isCreated = false;
                    finish();
                }
            } else {
                Toast.makeText(
                        this,
                        getResources().getString(
                                R.string.please_complete_previous_activity),
                        Toast.LENGTH_SHORT).show();
                isCreated = false;
            }

        } else if (menu.getConfigCode().equals(MENU_KELLGS_DASH) && hasLink == 1 && !isPreVisit) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP) {
                if (!isClick) {
                    isClick = true;
                    // finish();
                    bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                            DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                            DateTimeUtils.now(DateTimeUtils.TIME), menu.getConfigCode());
                    Intent intent = new Intent(getApplicationContext(),
                            KellogsDashBoardActivity.class);
                    intent.putExtra("screenTitle", menu.getMenuName());
                    startActivity(intent);
                    isCreated = false;
                    finish();
                }
            } else {
                Toast.makeText(
                        this,
                        getResources().getString(
                                R.string.please_complete_previous_activity),
                        Toast.LENGTH_SHORT).show();
                isCreated = false;
            }

        } else if (menu.getConfigCode().equals(MENU_PHOTO) && hasLink == 1 && !isPreVisit) {

            load_MENU_PHOTO(menu, isFromChild);

        } else if (menu.getConfigCode().equals(MENU_INVOICE) && hasLink == 1 && !isPreVisit) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP
            ) {

                OrderHelper orderHelper = OrderHelper.getInstance(this);

                if (bmodel.isOrderExistToCreateInvoice()) {

                    if (bmodel.isEdit()) {
                        bmodel.setEdit(true);
                        orderHelper.loadOrderedProducts(this, bmodel.getRetailerMasterBO()
                                .getRetailerID(), null);
                        orderHelper.loadSerialNo(this);
                        enableSchemeModule();
                    }

                    bmodel.productHelper.loadRetailerWiseProductWisePurchased();
                    bmodel.productHelper
                            .loadRetailerWiseProductWiseP4StockAndOrderQty();

                    if (bmodel.configurationMasterHelper.IS_INITIATIVE) {
                        /** Load Initiative **/
                        bmodel.productHelper.loadInitiativeProducts();
                        bmodel.initiativeHelper.downloadInitiativeHeader(bmodel
                                .getRetailerMasterBO().getSubchannelid());
                        /** Load Order History **/
                        bmodel.initiativeHelper.loadLocalOrdersQty(bmodel
                                .getRetailerMasterBO().getRetailerID());
                    }

                    /** Load SO Norm **/
                    if (bmodel.configurationMasterHelper.IS_SUGGESTED_ORDER) {
                        bmodel.productHelper
                                .loadRetailerWiseInventoryOrderQty();
                    }

                    if (bmodel.configurationMasterHelper.IS_PRODUCT_DISPLAY_FOR_PIRAMAL)
                        bmodel.productHelper.updateProductColorAndSequance();

                    orderHelper.loadOrderedProducts(this, bmodel.getRetailerMasterBO()
                            .getRetailerID(), null);
                    // Intent intent = new Intent(HomeScreenTwo.this,
                    // OrderSummary.class);
                    Intent intent = null;
                    bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                            DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                            DateTimeUtils.now(DateTimeUtils.TIME), menu.getConfigCode());
                    /*if (bmodel.configurationMasterHelper.SHOW_ZEBRA_DIAGEO) {
                        intent = new Intent(HomeScreenTwo.this,
                                PrintPreviewScreenDiageo.class);
                    }
                    intent.putExtra("IsFromOrder", false);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);*/
                } else {
                    Toast.makeText(
                            this,
                            getResources()
                                    .getString(
                                            R.string.please_take_order_to_create_invoice),
                            Toast.LENGTH_SHORT).show();
                    isCreated = false;
                }
            } else {
                Toast.makeText(
                        this,
                        getResources().getString(
                                R.string.please_complete_previous_activity),
                        Toast.LENGTH_SHORT).show();
                isCreated = false;
            }
        } else if ((menu.getConfigCode().equals(MENU_COLLECTION)
                || menu.getConfigCode().equals(StandardListMasterConstants.MENU_COLLECTION_VIEW))
                && hasLink == 1 && !isPreVisit) {
            if (!isClick) {
                isClick = true;
                if (bmodel.configurationMasterHelper.IS_JUMP
                        || isPreviousDone(menu)) {
                    if (bmodel.configurationMasterHelper.SHOW_DISC_AMOUNT_ALLOW) {
                        collectionHelper.downloadDiscountSlab();
                    }

                    collectionHelper.downloadBankDetails();
                    collectionHelper.downloadBranchDetails();
                    collectionHelper.downloadRetailerAccountDetails();
                    collectionHelper.updateInvoiceDiscountedAmount();


                    bmodel.downloadInvoice(bmodel.getRetailerMasterBO().getRetailerID(), "COL");
                    collectionHelper.loadPaymentMode();

                    if (bmodel.getInvoiceHeaderBO() != null
                            && bmodel.getInvoiceHeaderBO().size() > 0) {

                        //load currency data
                        if (bmodel.configurationMasterHelper.IS_FORMAT_USING_CURRENCY_VALUE) {
                            bmodel.downloadCurrencyConfig();
                        }

                        bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                                DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                                DateTimeUtils.now(DateTimeUtils.TIME), menu.getConfigCode());

                        if (menu.getConfigCode().equals(
                                StandardListMasterConstants.MENU_COLLECTION_VIEW)) {
                            collectionHelper.setCollectionView(true);
                            bmodel.getRetailerMasterBO().setIsCollectionView("Y");
                            bmodel.isModuleCompleted("MENU_COLLECTION_VIEW");
                        }

                        Intent intent = new Intent(HomeScreenTwo.this,
                                CollectionScreen.class);
                        bmodel.mSelectedActivityName = menu.getMenuName();
                        intent.putExtra("screentitle", menu.getMenuName());
                        intent.putExtra("CurrentActivityCode", menu.getConfigCode());
                        startActivity(intent);
                        finish();
                    } else {
                        if (!isFinishing()) {
                            Toast.makeText(
                                    this,
                                    getResources()
                                            .getString(
                                                    R.string.no_data_exists),
                                    Toast.LENGTH_SHORT).show();
                        }
                        isCreated = false;
                        isClick = false;
                        menuCode = (menuCodeList.get(menu.getConfigCode()) == null ? "" : menuCodeList.get(menu.getConfigCode()));
                        if (!menuCode.equals(menu.getConfigCode()))
                            menuCodeList.put(menu.getConfigCode(), menu.getConfigCode());
                    }


                } else {
                    if (!isFinishing()) {
                        Toast.makeText(
                                this,
                                getResources().getString(
                                        R.string.please_complete_previous_activity),
                                Toast.LENGTH_SHORT).show();
                    }
                    isCreated = false;
                    isClick = false;
                }
            }
        } else if (menu.getConfigCode().equals(MENU_COLLECTION_REF)
                && hasLink == 1 && !isPreVisit) {
            if (!isClick) {
                isClick = true;
                if (bmodel.configurationMasterHelper.IS_JUMP
                        || isPreviousDone(menu)) {

                    collectionHelper.updateInvoiceDiscountedAmount();
                    bmodel.downloadInvoice(bmodel.getRetailerMasterBO().getRetailerID(), "DOC");
                    collectionHelper.loadCollectionReference();

                    if (bmodel.getInvoiceHeaderBO() != null
                            && bmodel.getInvoiceHeaderBO().size() > 0) {

                        bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                                DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                                DateTimeUtils.now(DateTimeUtils.TIME), menu.getConfigCode());

                        Intent intent = new Intent(HomeScreenTwo.this,
                                CollectionReference.class);
                        bmodel.mSelectedActivityName = menu.getMenuName();
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(
                                this,
                                getResources()
                                        .getString(
                                                R.string.no_data_exists),
                                Toast.LENGTH_SHORT).show();
                        isCreated = false;
                        isClick = false;
                        menuCode = (menuCodeList.get(menu.getConfigCode()) == null ? "" : menuCodeList.get(menu.getConfigCode()));
                        if (!menuCode.equals(menu.getConfigCode()))
                            menuCodeList.put(menu.getConfigCode(), menu.getConfigCode());
                    }
                } else {
                    Toast.makeText(
                            this,
                            getResources().getString(
                                    R.string.please_complete_previous_activity),
                            Toast.LENGTH_SHORT).show();
                    isCreated = false;
                    isClick = false;
                }
            }
        } else if (((menu.getConfigCode().equals(MENU_SALES_RET) && hasLink == 1)
                || (menu.getConfigCode().equals(StandardListMasterConstants.MENU_STOCK_REPLACEMENT) && hasLink == 1)) && !isPreVisit) {
            if (!isClick) {
                isClick = true;
                if (bmodel.configurationMasterHelper.IS_ORD_SR_VALUE_VALIDATE &&
                        !bmodel.configurationMasterHelper.IS_INVOICE &&
                        bmodel.getOrderValue() == 0) {
                    Toast.makeText(this, getResources().getString(R.string.please_complete_order_taking_activity_to_perform_sales_return_activity), Toast.LENGTH_LONG).show();
                    isCreated = false;
                    isClick = false;
                } else if (isPreviousDone(menu) || bmodel.configurationMasterHelper.IS_JUMP) {

                    SalesReturnHelper salesReturnHelper = SalesReturnHelper.getInstance(this);
                    salesReturnHelper.loadSalesReturnConfigurations(getApplicationContext());

                    bmodel.reasonHelper.downloadSalesReturnReason();

                    if (bmodel.reasonHelper.getReasonSalesReturnMaster().size() > 0) {

                        new DownloadSalesReturnProducts(salesReturnHelper, menu.getConfigCode(), menu.getMenuName()).execute();

                    } else {
                        Toast.makeText(
                                HomeScreenTwo.this,
                                getResources()
                                        .getString(
                                                R.string.reasonmaster_not_downloaded),
                                Toast.LENGTH_SHORT).show();
                        isCreated = false;

                        isClick = false;
                        menuCode = (menuCodeList.get(menu.getConfigCode()) == null ? "" : menuCodeList.get(menu.getConfigCode()));
                        if (!menuCode.equals(menu.getConfigCode()))
                            menuCodeList.put(menu.getConfigCode(), menu.getConfigCode());
                    }

                } else {
                    Toast.makeText(
                            this,
                            getResources().getString(
                                    R.string.please_complete_previous_activity),
                            Toast.LENGTH_SHORT).show();
                    isCreated = false;

                    isClick = false;
                }
            }
        } else if (menu.getConfigCode().equals(MENU_DGT) && hasLink == 1 && !isPreVisit) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP
            ) {
                DigitalContentHelper mDigitalContentHelper = DigitalContentHelper.getInstance(this);
                mDigitalContentHelper.downloadDigitalContent(getApplicationContext(), "RETAILER");
                if (!isClick) {
                    isClick = true;
                    if (mDigitalContentHelper.getDigitalMaster() != null
                            && mDigitalContentHelper.getDigitalMaster()
                            .size() > 0) {
                        bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                                DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                                DateTimeUtils.now(DateTimeUtils.TIME), menu.getConfigCode());
                        Intent i = new Intent(HomeScreenTwo.this,
                                DigitalContentActivity.class);
                        i.putExtra("CurrentActivityCode", menu.getConfigCode());
                        i.putExtra("FromDigi", "Digi");
                        i.putExtra("screentitle", menu.getMenuName());
                        startActivity(i);
                        finish();
                    } else {
                        dataNotMapped();
                        isCreated = false;

                        isClick = false;
                        menuCode = (menuCodeList.get(menu.getConfigCode()) == null ? "" : menuCodeList.get(menu.getConfigCode()));
                        if (!menuCode.equals(menu.getConfigCode()))
                            menuCodeList.put(menu.getConfigCode(), menu.getConfigCode());
                    }
                }
            } else {
                Toast.makeText(
                        this,
                        getResources().getString(
                                R.string.please_complete_previous_activity),
                        Toast.LENGTH_SHORT).show();
                isCreated = false;

            }

        } else if (menu.getConfigCode().equals(MENU_CALL_ANLYS) && !isPreVisit) {
            if (bmodel.getAppDataProvider().getRetailMaster().isAdhoc())
                finish();
            else {
                if (bmodel.configurationMasterHelper.SHOW_NO_COLLECTION_REASON &&
                        !collectionHelper.checkInvoiceWithReason(bmodel.getRetailerMasterBO().getRetailerID(), this)) {

                    isCreated = false;
                    isClick = false;
                    Toast.makeText(this, getString(R.string.invoice_with_no_collection), Toast.LENGTH_SHORT).show();

                } else if ((!bmodel.configurationMasterHelper.IS_JUMP && isPreviousDone(menu))
                        || (bmodel.configurationMasterHelper.IS_JUMP && isAllMandatoryMenuDone())
                        || !canAllowCallAnalysis()) {
                    bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                            DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                            DateTimeUtils.now(DateTimeUtils.TIME), menu.getConfigCode());

                    // bmodel.productHelper.downloadIndicativeOrder();

                    if (!bmodel.configurationMasterHelper.IS_SKIP_CALL_ANALYSIS) {
                        if (bmodel.isEdit()) {
                            OrderHelper.getInstance(this).loadOrderedProducts(this, bmodel.getRetailerMasterBO()
                                    .getRetailerID(), null);
                            enableSchemeModule();
                        }
                        if (menuCodeList.size() > 0)
                            menuCodeList.clear();
                        Intent in = new Intent(HomeScreenTwo.this,
                                CallAnalysisActivity.class);
                        in.putExtra("screentitle", menu.getMenuName());
                        startActivity(in);
                    } else
                        doCloseCall();

                    finish();
                } else {
                    if (bmodel.configurationMasterHelper.IS_JUMP)
                        onCreateDialog(MANDATORY_MODULE_CLOSE_CALL);
                    else
                        Toast.makeText(
                                this,
                                getResources().getString(
                                        R.string.please_complete_previous_activity),
                                Toast.LENGTH_SHORT).show();
                    isCreated = false;

                    isClick = false;
                }
            }

        } else if (menu.getConfigCode().equals(MENU_ASSET) && hasLink == 1) {
            if (!isClick) {
                isClick = true;
                if (isPreviousDone(menu)
                        || bmodel.configurationMasterHelper.IS_JUMP || isPreVisit) {

                    AssetTrackingHelper assetTrackingHelper = AssetTrackingHelper.getInstance(this);
                    boolean isAssetTransactionExistForAudit = assetTrackingHelper.loadDataForAssetPOSM(getApplicationContext(), MENU_ASSET);

                    if (isAssetTransactionExistForAudit && assetTrackingHelper.getAssetTrackingList().size() > 0 ||
                            assetTrackingHelper.SHOW_ADD_NEW_ASSET) {
                        bmodel.configurationMasterHelper.downloadFloatingNPReasonWithPhoto(menu.getConfigCode());
                        assetTrackingHelper.mSelectedActivityName = menu.getMenuName();

                        if (!isPreVisit)
                            bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                                    DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                                    DateTimeUtils.now(DateTimeUtils.TIME), menu.getConfigCode());

                        Intent in = new Intent(HomeScreenTwo.this,
                                AssetTrackingActivity.class);

                        if (isPreVisit)
                            in.putExtra("PreVisit", true);

                        in.putExtra("CurrentActivityCode", menu.getConfigCode());
                        if (isFromChild)
                            in.putExtra("isFromChild", true);
                        startActivity(in);
                        finish();

                    } else {

                        dataNotMapped();
                        isCreated = false;

                        isClick = false;
                        menuCode = (menuCodeList.get(menu.getConfigCode()) == null ? "" : menuCodeList.get(menu.getConfigCode()));
                        if (!menuCode.equals(menu.getConfigCode()))
                            menuCodeList.put(menu.getConfigCode(), menu.getConfigCode());
                    }
                } else {
                    Toast.makeText(
                            this,
                            getResources().getString(
                                    R.string.please_complete_previous_activity),
                            Toast.LENGTH_SHORT).show();
                    isCreated = false;

                    isClick = false;
                }
            }
        } else if (menu.getConfigCode().equals(MENU_SERIALIZED_ASSET) && hasLink == 1) {
            if (!isClick) {
                isClick = true;
                if (isPreviousDone(menu)
                        || bmodel.configurationMasterHelper.IS_JUMP || isPreVisit) {

                    SerializedAssetHelper assetTrackingHelper = SerializedAssetHelper.getInstance(this);
                    assetTrackingHelper.loadDataForAssetPOSM(getApplicationContext(), MENU_SERIALIZED_ASSET);

                    if (assetTrackingHelper.getAssetTrackingList().size() > 0 ||
                            assetTrackingHelper.SHOW_ADD_NEW_ASSET) {

                        assetTrackingHelper.mSelectedActivityName = menu.getMenuName();

                        bmodel.configurationMasterHelper.downloadFloatingNPReasonWithPhoto(menu.getConfigCode());

                        if (!isPreVisit)
                            bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                                    DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                                    DateTimeUtils.now(DateTimeUtils.TIME), menu.getConfigCode());

                        Intent in = new Intent(HomeScreenTwo.this,
                                SerializedAssetActivity.class);

                        if (isPreVisit)
                            in.putExtra("PreVisit", true);

                        in.putExtra("CurrentActivityCode", menu.getConfigCode());
                        if (isFromChild)
                            in.putExtra("isFromChild", isFromChild);
                        startActivity(in);
                        finish();

                    } else {

                        dataNotMapped();
                        isCreated = false;
                        isClick = false;
                        menuCode = (menuCodeList.get(menu.getConfigCode()) == null ? "" : menuCodeList.get(menu.getConfigCode()));
                        if (!menuCode.equals(menu.getConfigCode()))
                            menuCodeList.put(menu.getConfigCode(), menu.getConfigCode());
                    }
                } else {
                    Toast.makeText(
                            this,
                            getResources().getString(
                                    R.string.please_complete_previous_activity),
                            Toast.LENGTH_SHORT).show();
                    isCreated = false;
                    isClick = false;
                }
            }
        } else if (menu.getConfigCode().equals(MENU_POSM) && hasLink == 1) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP || isPreVisit) {

                AssetTrackingHelper assetTrackingHelper = AssetTrackingHelper.getInstance(this);

                boolean isAssetTransactionExistForAudit = assetTrackingHelper.loadDataForAssetPOSM(getApplicationContext(), MENU_POSM);

                if (isAssetTransactionExistForAudit && assetTrackingHelper.getAssetTrackingList().size() > 0) {

                    assetTrackingHelper.mSelectedActivityName = menu.getMenuName();

                    if (!isPreVisit)
                        bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                                DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                                DateTimeUtils.now(DateTimeUtils.TIME), menu.getConfigCode());

                    Intent in = new Intent(HomeScreenTwo.this,
                            PosmTrackingActivity.class);

                    if (isPreVisit)
                        in.putExtra("PreVisit", true);

                    in.putExtra("CurrentActivityCode", menu.getConfigCode());
                    if (isFromChild)
                        in.putExtra("isFromChild", true);
                    startActivity(in);
                    finish();

                } else {
                    dataNotMapped();
                    isCreated = false;

                    menuCode = (menuCodeList.get(menu.getConfigCode()) == null ? "" : menuCodeList.get(menu.getConfigCode()));
                    if (!menuCode.equals(menu.getConfigCode()))
                        menuCodeList.put(menu.getConfigCode(), menu.getConfigCode());
                }
            } else {
                Toast.makeText(
                        this,
                        getResources().getString(
                                R.string.please_complete_previous_activity),
                        Toast.LENGTH_SHORT).show();
                isCreated = false;

            }

        } else if (menu.getConfigCode().equals(MENU_NEAREXPIRY) && hasLink == 1) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP || isPreVisit) {

                NearExpiryTrackingHelper mNearExpiryHelper = NearExpiryTrackingHelper.getInstance(this);

                boolean isNearExpiryDataExist = true;
                if (bmodel.configurationMasterHelper.isAuditEnabled() &&
                        !mNearExpiryHelper.hasAlreadySKUTrackingDone(getApplicationContext()))
                    isNearExpiryDataExist = false;

                if (isNearExpiryDataExist) {

                    mNearExpiryHelper.mSelectedActivityName = menu.getMenuName();

                    bmodel.productHelper.downloadInStoreLocations();
                    mNearExpiryHelper.loadSKUTracking(getApplicationContext(), false);
                    mNearExpiryHelper.loadNearExpiryConfig(getApplicationContext());
                    if (bmodel.configurationMasterHelper.IS_NEAR_EXPIRY_RETAIN_LAST_VISIT_TRAN && !mNearExpiryHelper.hasAlreadySKUTrackingDone(getApplicationContext())) {
                        mNearExpiryHelper.loadLastVisitSKUTracking(getApplicationContext());
                    }

                    bmodel.updateProductUOM(StandardListMasterConstants.mActivityCodeByMenuCode.get(MENU_NEAREXPIRY), 1);

                    if (!isPreVisit)
                        bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                                DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), DateTimeUtils.now(DateTimeUtils.TIME),
                                MENU_NEAREXPIRY);

                    Intent intent = new Intent(HomeScreenTwo.this,
                            NearExpiryTrackingActivity.class);

                    if (isPreVisit)
                        intent.putExtra("PreVisit", true);

                    intent.putExtra("CurrentActivityCode", menu.getConfigCode());
                    if (isFromChild)
                        intent.putExtra("isFromChild", isFromChild);
                    startActivity(intent);
                    finish();
                } else {
                    dataNotMapped();
                    isCreated = false;

                    menuCode = (menuCodeList.get(menu.getConfigCode()) == null ? "" : menuCodeList.get(menu.getConfigCode()));
                    if (!menuCode.equals(menu.getConfigCode()))
                        menuCodeList.put(menu.getConfigCode(), menu.getConfigCode());
                }

            } else {
                Toast.makeText(
                        this,
                        getResources().getString(
                                R.string.please_complete_previous_activity),
                        Toast.LENGTH_SHORT).show();
                isCreated = false;

            }
        } else if (menu.getConfigCode().equals(MENU_SKUWISERTGT)
                && hasLink == 1 && !isPreVisit) {

            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP
            ) {

                Intent i = new Intent(HomeScreenTwo.this,
                        SKUWiseTargetActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                i.putExtra("screentitle", menu.getMenuName());
                i.putExtra("from", "2");
                i.putExtra("rid", bmodel.retailerMasterBO.getRetailerID());
                i.putExtra("type", "MONTH");
                i.putExtra("code", "SV");
                startActivity(i);
                finish();

            } else {
                Toast.makeText(
                        this,
                        getResources().getString(
                                R.string.please_complete_previous_activity),
                        Toast.LENGTH_SHORT).show();
                isCreated = false;

            }

        } else if (menu.getConfigCode().equals(MENU_CONTRACT_VIEW)
                && hasLink == 1 && !isPreVisit) {

            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP
            ) {
                bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                        DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                        DateTimeUtils.now(DateTimeUtils.TIME), menu.getConfigCode());
                RetailerContractHelper retailerContractHelper = RetailerContractHelper.getInstance(this);
                retailerContractHelper.downloadRetailerContract(bmodel.getRetailerMasterBO().getRetailerID());
                retailerContractHelper.downloadRenewedContract(bmodel.getRetailerMasterBO().getRetailerID());
                Intent i = new Intent(HomeScreenTwo.this,
                        RetailerContractActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                i.putExtra("screentitle", menu.getMenuName());

                startActivity(i);
                finish();

            } else {
                Toast.makeText(
                        this,
                        getResources().getString(
                                R.string.please_complete_previous_activity),
                        Toast.LENGTH_SHORT).show();
                isCreated = false;

            }

        } else if (menu.getConfigCode().equals(MENU_PLANOGRAM) && hasLink == 1) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP || isPreVisit) {

                PlanoGramHelper mPlanoGramHelper = PlanoGramHelper.getInstance(this);

                mPlanoGramHelper.mSelectedActivityName = menu.getMenuName();
                mPlanoGramHelper.loadConfigurations(getApplicationContext());
                chooseFilterType(MENU_PLANOGRAM);
                mPlanoGramHelper.downloadLevels(getApplicationContext(), MENU_PLANOGRAM, bmodel.retailerMasterBO.getRetailerID());
                mPlanoGramHelper.downloadMaster(getApplicationContext(), MENU_PLANOGRAM);
                mPlanoGramHelper.loadPlanoGramInEditMode(getApplicationContext(), bmodel.retailerMasterBO.getRetailerID());
                bmodel.configurationMasterHelper.downloadFloatingNPReasonWithPhoto(MENU_PLANOGRAM);

                if ((mPlanoGramHelper.getPlanogramMaster() != null && mPlanoGramHelper.getPlanogramMaster().size() > 0)) {

                    if (!isPreVisit)
                        bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                                DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                                DateTimeUtils.now(DateTimeUtils.TIME), menu.getConfigCode());

                    Intent in = new Intent(HomeScreenTwo.this,
                            PlanoGramActivity.class);
                    in.putExtra("from", "2");

                    if (isPreVisit)
                        in.putExtra("PreVisit", true);

                    in.putExtra("CurrentActivityCode", menu.getConfigCode());
                    if (isFromChild)
                        in.putExtra("isFromChild", true);
                    startActivity(in);
                    finish();

                } else {
                    dataNotMapped();
                    isCreated = false;

                    menuCode = (menuCodeList.get(menu.getConfigCode()) == null ? "" : menuCodeList.get(menu.getConfigCode()));
                    if (!menuCode.equals(menu.getConfigCode()))
                        menuCodeList.put(menu.getConfigCode(), menu.getConfigCode());
                }
            } else {
                Toast.makeText(
                        this,
                        getResources().getString(
                                R.string.please_complete_previous_activity),
                        Toast.LENGTH_SHORT).show();
                isCreated = false;

            }

        } else if (menu.getConfigCode().equals(MENU_PRICE) && hasLink == 1) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP || isPreVisit) {
                PriceTrackingHelper priceTrackingHelper = PriceTrackingHelper.getInstance(this);

                // To set the screen name, we are taking the menu name storing in global obj.
                bmodel.mSelectedActivityName = menu.getMenuName();

                // Load survey if floating survey is enabled for the price check module.
                if (bmodel.configurationMasterHelper
                        .downloadFloatingSurveyConfig(MENU_PRICE)) {
                    SurveyHelperNew surveyHelperNew = SurveyHelperNew.getInstance(this);
                    surveyHelperNew.setFromHomeScreen(false);
                    surveyHelperNew.downloadModuleId("STANDARD");
                    surveyHelperNew.downloadQuestionDetails(MENU_PRICE);
                    surveyHelperNew.loadSurveyAnswers(0);
                }

                // Download Tagged products and update the product master obj
                ProductTaggingHelper.getInstance(this).downloadTaggedProducts(this, "PC");

                // Load Price related configurations.
                priceTrackingHelper.loadPriceCheckConfiguration(getApplicationContext(), bmodel.getRetailerMasterBO().getSubchannelid());

                if (priceTrackingHelper.IS_LOAD_PRICE_COMPETITOR) {
                    if (bmodel.configurationMasterHelper.SHOW_COMPETITOR_FILTER) {
                        bmodel.productHelper.downloadCompetitorFiveFilterLevels();
                    }
                    bmodel.productHelper.downloadCompetitorProducts(MENU_PRICE);
                    ProductTaggingHelper.getInstance(this).downloadCompetitorTaggedProducts(this, "PC");
                }

                priceTrackingHelper.clearPriceCheck();
                priceTrackingHelper.loadPriceTransaction(getApplicationContext());

                if (bmodel.configurationMasterHelper.IS_PRICE_CHECK_RETAIN_LAST_VISIT_IN_EDIT_MODE && !priceTrackingHelper.isPriceCheckDone(getApplicationContext())) {
                    priceTrackingHelper.updateLastVisitPriceAndMRP();
                }

                bmodel.updateProductUOM(StandardListMasterConstants.mActivityCodeByMenuCode.get(MENU_PRICE), 0);

                if (!isPreVisit)
                    bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                            DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                            DateTimeUtils.now(DateTimeUtils.TIME), menu.getConfigCode());

                Intent in = new Intent(HomeScreenTwo.this,
                        PriceTrackActivity.class);

                if (isPreVisit)
                    in.putExtra("PreVisit", true);

                in.putExtra("CurrentActivityCode", menu.getConfigCode());
                if (isFromChild)
                    in.putExtra("isFromChild", true);
                startActivity(in);
                finish();
            } else {
                Toast.makeText(
                        this,
                        getResources().getString(
                                R.string.please_complete_previous_activity),
                        Toast.LENGTH_SHORT).show();
                isCreated = false;

            }
        } else if (menu.getConfigCode().equals(MENU_PRICE_COMP) && hasLink == 1) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP || isPreVisit) {
                PriceTrackingHelper priceTrackingHelper = PriceTrackingHelper.getInstance(this);
                // To set the screen name, we are taking the menu name storing in global obj.
                bmodel.mSelectedActivityName = menu.getMenuName();

                // Load survey if floating survey is enabled for the price check module.
                if (bmodel.configurationMasterHelper
                        .downloadFloatingSurveyConfig(MENU_PRICE_COMP)) {
                    SurveyHelperNew surveyHelperNew = SurveyHelperNew.getInstance(this);
                    surveyHelperNew.setFromHomeScreen(false);
                    surveyHelperNew.downloadModuleId("STANDARD");
                    surveyHelperNew.downloadQuestionDetails(MENU_PRICE_COMP);
                    surveyHelperNew.loadSurveyAnswers(0);
                }

                // Download Tagged products and update the product master obj
                ProductTaggingHelper.getInstance(this).downloadTaggedProducts(this, "PC");

                // Load Price related configurations.
                priceTrackingHelper.loadPriceCheckConfiguration(getApplicationContext(), bmodel.getRetailerMasterBO().getSubchannelid());
                //its menu price comp
                bmodel.productHelper.downloadCompetitorProducts(MENU_PRICE_COMP);
                ProductTaggingHelper.getInstance(this).downloadCompetitorTaggedProducts(this, "PC");

                priceTrackingHelper.clearPriceCheck();
                priceTrackingHelper.loadPriceTransaction(getApplicationContext());
                CompetitorTrackingHelper.getInstance(this).downloadPriceCompanyMaster(MENU_PRICE_COMP);

                if (bmodel.configurationMasterHelper.IS_PRICE_CHECK_RETAIN_LAST_VISIT_IN_EDIT_MODE && !priceTrackingHelper.isPriceCheckDone(getApplicationContext())) {
                    priceTrackingHelper.updateLastVisitPriceAndMRP();
                }

                bmodel.updateProductUOM(StandardListMasterConstants.mActivityCodeByMenuCode.get(MENU_PRICE_COMP), 0);

                if (!isPreVisit)
                    bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                            DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                            DateTimeUtils.now(DateTimeUtils.TIME), menu.getConfigCode());


                Intent in = new Intent(HomeScreenTwo.this,
                        PriceTrackCompActivity.class);
                in.putExtra("CurrentActivityCode", menu.getConfigCode());
                if (isFromChild)
                    in.putExtra("isFromChild", true);

                if (isPreVisit)
                    in.putExtra("PreVisit", true);

                startActivity(in);
                finish();
            } else {
                Toast.makeText(
                        this,
                        getResources().getString(
                                R.string.please_complete_previous_activity),
                        Toast.LENGTH_SHORT).show();
                isCreated = false;

            }
        } else if (menu.getConfigCode().equals(MENU_EMPTY_RETURN)
                && hasLink == 1 && !isPreVisit) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP
            ) {
                if (bmodel.configurationMasterHelper.SHOW_GROUPPRODUCTRETURN)
                    EmptyReturnHelper.getInstance(this).downloadProductType();
                bmodel.mSelectedActivityName = menu.getMenuName();
                bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                        DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                        DateTimeUtils.now(DateTimeUtils.TIME), menu.getConfigCode());
                Intent in = new Intent(HomeScreenTwo.this,
                        EmptyReturnActivity.class);
                startActivity(in);
                finish();
            } else {
                Toast.makeText(
                        this,
                        getResources().getString(
                                R.string.please_complete_previous_activity),
                        Toast.LENGTH_SHORT).show();
                isCreated = false;

            }
        } else if (menu.getConfigCode().equals(
                MENU_PROMO)
                && hasLink == 1) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP || isPreVisit) {
                PromotionHelper promotionHelper = PromotionHelper.getInstance(this);
                promotionHelper.loadDataForPromotion(getApplicationContext(), menu.getConfigCode());
                if (promotionHelper.getPromotionList().size() > 0) {
                    bmodel.mSelectedActivityName = menu.getMenuName();

                    if (!isPreVisit)
                        bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                                DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                                DateTimeUtils.now(DateTimeUtils.TIME),
                                MENU_PROMO);

                    Intent intent = new Intent(HomeScreenTwo.this,
                            PromotionTrackingActivity.class);

                    if (isPreVisit)
                        intent.putExtra("PreVisit", true);

                    intent.putExtra("CurrentActivityCode", menu.getConfigCode());
                    if (isFromChild)
                        intent.putExtra("isFromChild", true);
                    startActivity(intent);
                    finish();
                } else {
                    dataNotMapped();
                    isCreated = false;

                    menuCode = (menuCodeList.get(menu.getConfigCode()) == null ? "" : menuCodeList.get(menu.getConfigCode()));
                    if (!menuCode.equals(menu.getConfigCode()))
                        menuCodeList.put(menu.getConfigCode(), menu.getConfigCode());
                }
            } else {
                Toast.makeText(
                        this,
                        getResources().getString(
                                R.string.please_complete_previous_activity),
                        Toast.LENGTH_SHORT).show();
                isCreated = false;

            }
        } else if (menu.getConfigCode()
                .equals(MENU_SOS) && hasLink == 1) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP || isPreVisit) {

                SalesFundamentalHelper mSFHelper = SalesFundamentalHelper.getInstance(this);
                ShelfShareHelper mShelfShareHelper = ShelfShareHelper.getInstance();

                //Load Configurations
                mSFHelper.updateSalesFundamentalConfigurations();
                mSFHelper.setTotalPopUpConfig();

                //Load the locations
                mSFHelper.downloadLocations();
                mShelfShareHelper.setLocations(mSFHelper.cloneLocationList(mSFHelper.getLocationList()));

                //Load filter
                //mSFHelper.downloadSFFiveLevelFilter(MENU_SOS);
                mSFHelper.setmSFModuleSequence(bmodel.productHelper.downloadFilterLevel(MENU_SOS));
                mSFHelper.setmFilterProductsByLevelId(bmodel.productHelper.downloadFilterLevelProducts(
                        mSFHelper.getSequenceValues(), false));

                //load content data
                mSFHelper.loadData(MENU_SOS);

                //load transaction data
                boolean isDataAvailforSOS = mSFHelper.loadSavedTracking(MENU_SOS);

                if (isDataAvailforSOS && (mSFHelper.getSOSList() != null
                        && mSFHelper.getSOSList().size() > 0)) {

                    if (!isPreVisit)
                        bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                                DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                                DateTimeUtils.now(DateTimeUtils.TIME),
                                MENU_SOS);

                    mSFHelper.mSelectedActivityName = menu.getMenuName();
                    Intent intent = new Intent(this, SOSActivity.class);
                    intent.putExtra("CurrentActivityCode", menu.getConfigCode());

                    if (isPreVisit)
                        intent.putExtra("PreVisit", true);

                    if (isFromChild)
                        intent.putExtra("isFromChild", true);
                    startActivity(intent);
                    finish();

                } else {

                    dataNotMapped();
                    isCreated = false;


                    menuCode = (menuCodeList.get(menu.getConfigCode()) == null ? "" : menuCodeList.get(menu.getConfigCode()));
                    if (!menuCode.equals(menu.getConfigCode()))
                        menuCodeList.put(menu.getConfigCode(), menu.getConfigCode());
                }

            } else {
                Toast.makeText(
                        this,
                        getResources().getString(
                                R.string.please_complete_previous_activity),
                        Toast.LENGTH_SHORT).show();
                isCreated = false;

            }
        } else if (menu.getConfigCode()
                .equals(MENU_SOS_PROJ) && hasLink == 1) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP && !isPreVisit) {

                bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                        DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                        DateTimeUtils.now(DateTimeUtils.TIME),
                        MENU_SOS_PROJ);

                Intent intent = new Intent(this, SOSActivity_PRJSpecific.class);
                if (isFromChild)
                    intent.putExtra("isFromChild", true);
                startActivity(intent);
                finish();

            } else {
                Toast.makeText(
                        this,
                        getResources().getString(
                                R.string.please_complete_previous_activity),
                        Toast.LENGTH_SHORT).show();
                isCreated = false;

            }
        } else if (menu.getConfigCode()
                .equals(MENU_SOD) && hasLink == 1) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP || isPreVisit) {

                SalesFundamentalHelper mSFHelper = SalesFundamentalHelper.getInstance(this);
                ShelfShareHelper mShelfShareHelper = ShelfShareHelper.getInstance();

                mSFHelper.updateSalesFundamentalConfigurations();
                mSFHelper.setTotalPopUpConfig();

                mSFHelper.downloadLocations();
                mShelfShareHelper.setLocations(mSFHelper.cloneLocationList(mSFHelper.getLocationList()));

                //Load filter
                //mSFHelper.downloadSFFiveLevelFilter(MENU_SOD);
                mSFHelper.setmSFModuleSequence(bmodel.productHelper.downloadFilterLevel(MENU_SOD));
                mSFHelper.setmFilterProductsByLevelId(bmodel.productHelper.downloadFilterLevelProducts(
                        mSFHelper.getSequenceValues(), false));


                mSFHelper.loadData(MENU_SOD);

                boolean isDataAvailforSOD = mSFHelper.loadSavedTracking(MENU_SOD);

                if (isDataAvailforSOD && (mSFHelper.getSODList() != null && mSFHelper.getSODList().size() > 0)) {

                    if (!isPreVisit)
                        bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                                DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                                DateTimeUtils.now(DateTimeUtils.TIME),
                                MENU_SOD);
                    mSFHelper.mSelectedActivityName = menu.getMenuName();
                    Intent intent = new Intent(this, SODActivity.class);

                    if (isPreVisit)
                        intent.putExtra("PreVisit", true);

                    if (isFromChild)
                        intent.putExtra("isFromChild", true);
                    startActivity(intent);
                    finish();

                } else {
                    dataNotMapped();
                    isCreated = false;

                    menuCode = (menuCodeList.get(menu.getConfigCode()) == null ? "" : menuCodeList.get(menu.getConfigCode()));
                    if (!menuCode.equals(menu.getConfigCode()))
                        menuCodeList.put(menu.getConfigCode(), menu.getConfigCode());
                }

            } else {
                Toast.makeText(
                        this,
                        getResources().getString(
                                R.string.please_complete_previous_activity),
                        Toast.LENGTH_SHORT).show();
                isCreated = false;

            }
        } else if (menu.getConfigCode()
                .equals(MENU_SOD_ASSET) && hasLink == 1) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP || isPreVisit) {

                AssetTrackingHelper assetTrackingHelper = AssetTrackingHelper.getInstance(this);
                SODAssetHelper mSODAssetHelper = SODAssetHelper.getInstance(this);
                SalesFundamentalHelper mSFHelper = SalesFundamentalHelper.getInstance(this);

                mSODAssetHelper.downloadLocations();
                assetTrackingHelper.loadDataForAssetPOSM(getApplicationContext(), MENU_ASSET);

                //Load filter
                //mSODAssetHelper.downloadSFFiveLevelFilter(MENU_SOD_ASSET);
                mSFHelper.setmSFModuleSequence(bmodel.productHelper.downloadFilterLevel(MENU_SOD_ASSET));
                mSFHelper.setmFilterProductsByLevelId(bmodel.productHelper.downloadFilterLevelProducts(
                        mSFHelper.getSequenceValues(), false));

                mSODAssetHelper.loadSODAssetData(MENU_SOD_ASSET);

                mSODAssetHelper.loadSavedTracking(MENU_SOD_ASSET);

                if (mSODAssetHelper.getSODList() != null && mSODAssetHelper.getSODList().size() > 0) {

                    if (!isPreVisit)
                        bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                                DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                                DateTimeUtils.now(DateTimeUtils.TIME),
                                MENU_SOD_ASSET);
                    mSODAssetHelper.mSelectedActivityName = menu.getMenuName();
                    Intent intent = new Intent(this, SODAssetActivity.class);

                    if (isPreVisit)
                        intent.putExtra("PreVisit", true);

                    if (isFromChild)
                        intent.putExtra("isFromChild", true);
                    startActivity(intent);
                    finish();
                } else {

                    dataNotMapped();
                    isCreated = false;


                    menuCode = (menuCodeList.get(menu.getConfigCode()) == null ? "" : menuCodeList.get(menu.getConfigCode()));
                    if (!menuCode.equals(menu.getConfigCode()))
                        menuCodeList.put(menu.getConfigCode(), menu.getConfigCode());
                }

            } else {
                Toast.makeText(
                        this,
                        getResources().getString(
                                R.string.please_complete_previous_activity),
                        Toast.LENGTH_SHORT).show();
                isCreated = false;

            }
        } else if (menu.getConfigCode().equals(
                MENU_SOSKU)
                && hasLink == 1) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP || isPreVisit) {

                SalesFundamentalHelper mSFHelper = SalesFundamentalHelper.getInstance(this);

                mSFHelper.updateSalesFundamentalConfigurations();

                //mSFHelper.downloadSFFiveLevelFilter(MENU_SOSKU);
                mSFHelper.setmSFModuleSequence(bmodel.productHelper.downloadFilterLevel(MENU_SOSKU));
                mSFHelper.setmFilterProductsByLevelId(bmodel.productHelper.downloadFilterLevelProducts(
                        mSFHelper.getSequenceValues(), false));

                mSFHelper.loadData(MENU_SOSKU);

                mSFHelper
                        .loadSavedTracking(MENU_SOSKU);

                if (mSFHelper.getSOSKUList() != null && mSFHelper.getSOSKUList().size() > 0) {

                    if (!isPreVisit)
                        bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                                DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                                DateTimeUtils.now(DateTimeUtils.TIME),
                                MENU_SOSKU);
                    mSFHelper.mSelectedActivityName = menu.getMenuName();
                    Intent intent = new Intent(this, SOSKUActivity.class);
                    intent.putExtra("CurrentActivityCode", menu.getConfigCode());

                    if (isPreVisit)
                        intent.putExtra("PreVisit", true);

                    if (isFromChild)
                        intent.putExtra("isFromChild", true);
                    startActivity(intent);
                    finish();

                } else {

                    dataNotMapped();
                    isCreated = false;

                    menuCode = (menuCodeList.get(menu.getConfigCode()) == null ? "" : menuCodeList.get(menu.getConfigCode()));
                    if (!menuCode.equals(menu.getConfigCode()))
                        menuCodeList.put(menu.getConfigCode(), menu.getConfigCode());
                }

            } else {
                Toast.makeText(
                        this,
                        getResources().getString(
                                R.string.please_complete_previous_activity),
                        Toast.LENGTH_SHORT).show();
                isCreated = false;

            }
        } else if (menu.getConfigCode().equals(
                MENU_COMPETITOR)
                && hasLink == 1) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP || isPreVisit) {
                CompetitorTrackingHelper competitorTrackingHelper = CompetitorTrackingHelper.getInstance(this);
                competitorTrackingHelper.downloadCompanyMaster(MENU_COMPETITOR);
                competitorTrackingHelper.downloadTrackingList();
                competitorTrackingHelper
                        .downloadCompetitors(MENU_COMPETITOR);
                competitorTrackingHelper.loadcompetitors();
                int companySize = competitorTrackingHelper.getCompanyList().size();
                if (companySize > 0) {
                    bmodel.mSelectedActivityName = menu.getMenuName();

                    if (!isPreVisit)
                        bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                                DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                                DateTimeUtils.now(DateTimeUtils.TIME),
                                MENU_COMPETITOR);

                    Intent intent = new Intent(this,
                            CompetitorTrackingActivity.class);

                    if (isPreVisit)
                        intent.putExtra("PreVisit", true);

                    intent.putExtra("CurrentActivityCode", menu.getConfigCode());
                    if (isFromChild)
                        intent.putExtra("isFromChild", true);
                    startActivity(intent);
                    finish();
                } else {
                    dataNotMapped();
                    isCreated = false;

                    menuCode = (menuCodeList.get(menu.getConfigCode()) == null ? "" : menuCodeList.get(menu.getConfigCode()));
                    if (!menuCode.equals(menu.getConfigCode()))
                        menuCodeList.put(menu.getConfigCode(), menu.getConfigCode());
                }
            } else {
                Toast.makeText(
                        this,
                        getResources().getString(
                                R.string.please_complete_previous_activity),
                        Toast.LENGTH_SHORT).show();
                isCreated = false;

            }
        } else if (menu.getConfigCode().equals(MENU_CLOSE_CALL)
                && hasLink == 1 && !isPreVisit) {
            if ((!bmodel.configurationMasterHelper.IS_JUMP && isPreviousDone(menu))
                    || (bmodel.configurationMasterHelper.IS_JUMP && isAllMandatoryMenuDone())
                    || !canAllowCallAnalysis()) {
                bmodel.reasonHelper.downloadClosecallReasonList();
                bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                        DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                        DateTimeUtils.now(DateTimeUtils.TIME), menu.getConfigCode());
                int reasonsize = bmodel.reasonHelper.getClosecallReasonList().size();

                if (reasonsize > 0) {
                    if (menuCodeList.size() > 0)
                        menuCodeList.clear();
                    Intent intent = new Intent(this, CloseCallActivity.class);
                    startActivity(intent);
                    finish();

                } else {
                    dataNotMapped();
                    isCreated = false;

                    isClick = false;
                }
            } else {
                if (bmodel.configurationMasterHelper.IS_JUMP)
                    onCreateDialog(MANDATORY_MODULE_CLOSE_CALL);
                else
                    Toast.makeText(
                            this,
                            getResources().getString(
                                    R.string.please_complete_previous_activity),
                            Toast.LENGTH_SHORT).show();
                isCreated = false;

                isClick = false;
            }
        } else if (menu.getConfigCode().equals(MENU_CLOSE_KLGS)
                && hasLink == 1 && !isPreVisit) {
            {
                if ((!bmodel.configurationMasterHelper.IS_JUMP && isPreviousDone(menu))
                        || (bmodel.configurationMasterHelper.IS_JUMP && isAllMandatoryMenuDone())
                        || !canAllowCallAnalysis()) {
                    bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                            DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                            DateTimeUtils.now(DateTimeUtils.TIME), menu.getConfigCode());

                    if (menuCodeList.size() > 0)
                        menuCodeList.clear();
                    Intent in = new Intent(HomeScreenTwo.this,
                            CallAnalysisActivityKlgs.class);
                    in.putExtra("screentitle", menu.getMenuName());
                    startActivity(in);
                    finish();
                } else {
                    if (bmodel.configurationMasterHelper.IS_JUMP)
                        onCreateDialog(MANDATORY_MODULE_CLOSE_CALL);
                    else
                        Toast.makeText(
                                this,
                                getResources().getString(
                                        R.string.please_complete_previous_activity),
                                Toast.LENGTH_SHORT).show();
                    isCreated = false;

                    isClick = false;
                }

            }
        } else if (menu.getConfigCode().equals(MENU_DELIVERY) && hasLink == 1 && !isPreVisit) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP
                    || menu.getModule_Order() == 1) {
                bmodel.configurationMasterHelper.loadDeliveryUOMConfiguration();
                bmodel.mSelectedActivityName = menu.getMenuName();
                Intent i = new Intent(this, DeliveryManagement.class);
                i.putExtra("screentitle", menu.getMenuName());
                startActivity(i);
                finish();
            }

        } else if (menu.getConfigCode().equals(MENU_LOYALTY_POINTS) && hasLink == 1 && !isPreVisit) {

            if (isPreviousDone(menu) || bmodel.configurationMasterHelper.IS_JUMP) {
                // bmodel.productHelper.downloadLoyaltyDescription();

                bmodel.productHelper.downloadloyaltyBenifits();
                bmodel.productHelper.downloadLoyaltyDescription(bmodel.getRetailerMasterBO().getRetailerID());

                if ((bmodel.productHelper.getProductloyalties() != null)
                        && (bmodel.productHelper.getProductloyalties().size() > 0)) {
                    LoyalityHelper loyalityHelper = LoyalityHelper.getInstance(this);
                    if (loyalityHelper.hasUpdatedLoyalties(bmodel.getRetailerMasterBO().getRetailerID()))
                        loyalityHelper.updatedLoyaltyPoints(bmodel.getRetailerMasterBO().getRetailerID());

                    Intent i = new Intent(HomeScreenTwo.this, LoyaltyPointsFragmentActivity.class);
                    i.putExtra("screentitle", menu.getMenuName());
                    bmodel.mSelectedActivityName = menu.getMenuName();
                    startActivity(i);
                    isCreated = true;
                    finish();
                } else {
                    dataNotMapped();
                    isCreated = false;

                }


            }
        } else if (menu.getConfigCode().equals(MENU_RTR_KPI) && hasLink == 1 && !isPreVisit) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP) {
                DashBoardHelper dashBoardHelper = DashBoardHelper.getInstance(this);
                ArrayList<String> dashList = dashBoardHelper.getDashList(true);
                if (!dashList.isEmpty())
                    dashBoardHelper.loadRetailerDashBoard(bmodel.getRetailerMasterBO().getRetailerID() + "", dashList.get(0));

                if (dashBoardHelper.getDashboardMasterData() != null && !dashBoardHelper.getDashboardMasterData().isEmpty()) {
                    Intent i = new Intent(this,
                            SellerDashBoardActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    Bundle bnd = new Bundle();
                    bnd.putString("screentitle", menu.getMenuName());
                    bnd.putString("retid", bmodel.getRetailerMasterBO().getRetailerID());
                    bnd.putBoolean("isFromHomeScreenTwo", true);
                    bnd.putString("menuCode", menu.getConfigCode());
                    i.putExtras(bnd);
                    bmodel.mSelectedActivityName = menu.getMenuName();
                    bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                            DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                            DateTimeUtils.now(DateTimeUtils.TIME), menu.getConfigCode());
                    startActivity(i);
                } else {
                    Toast.makeText(
                            this,
                            getResources()
                                    .getString(
                                            R.string.no_data_exists),
                            Toast.LENGTH_SHORT).show();
                    isCreated = false;

                    menuCode = (menuCodeList.get(menu.getConfigCode()) == null ? "" : menuCodeList.get(menu.getConfigCode()));
                    if (!menuCode.equals(menu.getConfigCode()))
                        menuCodeList.put(menu.getConfigCode(), menu.getConfigCode());
                }
            } else {
                Toast.makeText(
                        this,
                        getResources().getString(
                                R.string.please_complete_previous_activity),
                        Toast.LENGTH_SHORT).show();
                isCreated = false;

            }

        } else if (menu.getConfigCode().equals(MENU_DASH_ACT) && hasLink == 1 && !isPreVisit) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP) {
                Intent i = new Intent(this,
                        DashBoardActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                i.putExtra("screentitle", menu.getMenuName());
                i.putExtra("isFromHomeScreenTwo", true);
                i.putExtra("menuCode", menu.getConfigCode());
                i.putExtra("retid", bmodel.getRetailerMasterBO().getRetailerID());
                bmodel.mSelectedActivityName = menu.getMenuName();
                bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                        DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                        DateTimeUtils.now(DateTimeUtils.TIME), menu.getConfigCode());
                startActivity(i);
            } else {
                Toast.makeText(
                        this,
                        getResources().getString(
                                R.string.please_complete_previous_activity),
                        Toast.LENGTH_SHORT).show();
                isCreated = false;


            }

        } else if (menu.getConfigCode().equals(MENU_DELIVERY_ORDER) && hasLink == 1 && !isPreVisit) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP) {


                OrderHelper orderHelper = OrderHelper.getInstance(this);
                if (orderHelper.hasAlreadyOrdered(this, bmodel.getRetailerMasterBO()
                        .getRetailerID())) {

                    orderHelper.loadOrderedProducts(this, bmodel.getRetailerMasterBO()
                            .getRetailerID(), null);
                    enableSchemeModule();

                    bmodel.mSelectedActivityName = menu.getMenuName();
                    bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                            DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                            DateTimeUtils.now(DateTimeUtils.TIME), menu.getConfigCode());

                    Intent i = new Intent(this,
                            DeliveryOrderActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    i.putExtra("screentitle", menu.getMenuName());
                    startActivity(i);
                    finish();
                } else {
                    Toast.makeText(
                            this,
                            getResources().getString(
                                    R.string.no_orders_available),
                            Toast.LENGTH_SHORT).show();
                    isCreated = false;

                }


            } else {
                Toast.makeText(
                        this,
                        getResources().getString(
                                R.string.please_complete_previous_activity),
                        Toast.LENGTH_SHORT).show();
                isCreated = false;

            }

        } else if (menu.getConfigCode().equals(MENU_FIT_DASH) && hasLink == 1 && !isPreVisit) {
            Intent i = new Intent(this,
                    FitScoreDashboardActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            i.putExtra("screentitle", menu.getMenuName());
            i.putExtra("menuCode", menu.getConfigCode());
            startActivity(i);
            finish();
        } else if (menu.getConfigCode().equals(MENU_DISPLAY_SCH_TRACK) && hasLink == 1 && !isPreVisit) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP) {

                schemeHelper.downloadDisplaySchemeTracking(getApplicationContext());
                if (schemeHelper.getDisplaySchemeTrackingList().size() > 0) {
                    Intent i = new Intent(this,
                            DisplaySchemeTrackingActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    i.putExtra("menuName", menu.getMenuName());
                    startActivity(i);
                    finish();
                } else {
                    Toast.makeText(
                            this,
                            getResources().getString(
                                    R.string.data_not_mapped),
                            Toast.LENGTH_SHORT).show();
                    isCreated = false;

                }

            } else {
                Toast.makeText(
                        this,
                        getResources().getString(
                                R.string.please_complete_previous_activity),
                        Toast.LENGTH_SHORT).show();
                isCreated = false;

            }
        } else if (menu.getConfigCode().equals(MENU_DISPLAY_SCH) && hasLink == 1 && !isPreVisit) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP) {

                schemeHelper.downloadDisplayScheme(getApplicationContext());
                schemeHelper.downloadDisplaySchemeSlabs(getApplicationContext());
                if (schemeHelper.getDisplaySchemeMasterList().size() > 0) {
                    Intent i = new Intent(this,
                            DisplaySchemeActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    i.putExtra("menuName", menu.getMenuName());
                    startActivity(i);
                    finish();
                } else {
                    Toast.makeText(
                            this,
                            getResources().getString(
                                    R.string.data_not_mapped),
                            Toast.LENGTH_SHORT).show();
                    isCreated = false;

                }
            } else {
                Toast.makeText(
                        this,
                        getResources().getString(
                                R.string.please_complete_previous_activity),
                        Toast.LENGTH_SHORT).show();
                isCreated = false;

            }
        } else if (menu.getConfigCode().equals(MENU_ORD_DELIVERY) && hasLink == 1 && !isPreVisit) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP) {

                bmodel.configurationMasterHelper.loadDeliveryUOMConfiguration();
                OrderDeliveryHelper orderDeliveryHelper = OrderDeliveryHelper.getInstance(this);
                orderDeliveryHelper.downloadOrderDeliveryHeader(this);

                if (orderDeliveryHelper.getOrderHeaders().size() > 0) {
                    bmodel.outletTimeStampHelper
                            .saveTimeStampModuleWise(
                                    DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                                    DateTimeUtils.now(DateTimeUtils.TIME),
                                    MENU_ORD_DELIVERY);

                    Intent i = new Intent(this,
                            OrderDeliveryActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    i.putExtra("menuName", menu.getMenuName());
                    i.putExtra("menuCode", menu.getConfigCode());
                    startActivity(i);
                    finish();
                } else {
                    Toast.makeText(
                            this,
                            getResources().getString(
                                    R.string.data_not_mapped),
                            Toast.LENGTH_SHORT).show();
                    isCreated = false;

                }

            } else {
                Toast.makeText(
                        this,
                        getResources().getString(
                                R.string.please_complete_previous_activity),
                        Toast.LENGTH_SHORT).show();
                isCreated = false;

            }
        } else if (menu.getConfigCode().equals(MENU_SALES_RET_DELIVERY) && hasLink == 1 && !isPreVisit) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP) {
                if (DeliveryManagementHelper.getInstance(this).hasDeliveryReturn()) {
                    Intent i = new Intent(this,
                            SalesReturnDeliveryActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    i.putExtra("menuName", menu.getMenuName());
                    i.putExtra("menuCode", menu.getConfigCode());
                    startActivity(i);
                    finish();
                } else {
                    Toast.makeText(
                            this,
                            getResources().getString(
                                    R.string.data_not_mapped),
                            Toast.LENGTH_SHORT).show();
                    isCreated = false;
                }
            }
        } else if (menu.getConfigCode().equals(MENU_PLANORMA) && hasLink == 1 && !isPreVisit) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP) {
                Intent i = new Intent(this,
                        PlanoramaActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
                finish();
            }
        } else if (menu.getConfigCode().equals(MENU_RTR_NOTES) && hasLink == 1) {
            if (!isClick) {
                isClick = true;
                if (isPreviousDone(menu)
                        || bmodel.configurationMasterHelper.IS_JUMP || isPreVisit) {
                    Intent i = new Intent(this,
                            NotesActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    if (isPreVisit)
                        i.putExtra("PreVisit", true);

                    i.putExtra(NoteConstant.MENU_CODE, menu.getConfigCode());
                    i.putExtra(NoteConstant.SCREEN_TITLE, menu.getMenuName());
                    startActivity(i);
                    finish();
                } else {
                    Toast.makeText(
                            this,
                            getResources().getString(
                                    R.string.please_complete_previous_activity),
                            Toast.LENGTH_SHORT).show();
                    isCreated = false;
                    isClick = false;

                }
            }
        } else if (menu.getConfigCode().equals(MENU_DISPLAY_ASSET) && hasLink == 1) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP || isPreVisit) {

                DisplayAssetHelper assetHelper = DisplayAssetHelper.getInstance(this);
                assetHelper.downloadDisplayAssets(this);

                if (assetHelper.getDisplayAssetList().size() > 0) {

                    if (!isPreVisit)
                        bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                                DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                                DateTimeUtils.now(DateTimeUtils.TIME), menu.getConfigCode());

                    assetHelper.loadDisplayAssetInEditMode(this);
                    Intent i = new Intent(this,
                            DisplayAssetActivity.class);
                    i.putExtra("menuName", menu.getMenuName());

                    if (isPreVisit)
                        i.putExtra("PreVisit", true);

                    i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(i);
                    finish();

                } else {
                    Toast.makeText(
                            this,
                            getResources().getString(
                                    R.string.data_not_mapped),
                            Toast.LENGTH_SHORT).show();
                    isCreated = false;
                }
            } else {
                Toast.makeText(
                        this,
                        getResources().getString(
                                R.string.please_complete_previous_activity),
                        Toast.LENGTH_SHORT).show();
                isCreated = false;

            }
        } else if (menu.getConfigCode().equals(MENU_ASSET_SERVICE_REQUEST) && hasLink == 1) {

            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP) {

                bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                        DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                        DateTimeUtils.now(DateTimeUtils.TIME), menu.getConfigCode());

                Intent i = new Intent(this,
                        AssetServiceRequestActivity.class);
                i.putExtra("menuName", menu.getMenuName());
                i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
                finish();

            } else {
                Toast.makeText(
                        this,
                        getResources().getString(
                                R.string.please_complete_previous_activity),
                        Toast.LENGTH_SHORT).show();
                isCreated = false;

                }
            } else if ((menu.getConfigCode().equals(MENU_DYNAMIC_RETAILER_DASHBOARD01) || menu.getConfigCode().equals(MENU_DYNAMIC_RETAILER_DASHBOARD02)) && hasLink == 1) {

            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP) {

                bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                        DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                        DateTimeUtils.now(DateTimeUtils.TIME), menu.getConfigCode());

                Intent i = new Intent(this,
                        DynamicReportActivity.class);
                i.putExtra("menuName", menu.getMenuName());
                i.putExtra("screentitle", menu.getMenuName());
                i.putExtra("menucode", menu.getConfigCode());
                i.putExtra("rid", bmodel.getAppDataProvider().getRetailMaster().getRetailerID());
                i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
                finish();

            } else {
                Toast.makeText(
                        this,
                        getResources().getString(
                                R.string.please_complete_previous_activity),
                        Toast.LENGTH_SHORT).show();
                isCreated = false;

            }
        }

    }

    private void load_MENU_PHOTO(ConfigureBO menu, boolean isFromChild) {
        if (isPreviousDone(menu)
                || bmodel.configurationMasterHelper.IS_JUMP
        ) {

            int count = bmodel.synchronizationHelper.getImagesCount();
            bmodel.productHelper.downloadInStoreLocationsForStockCheck();
            bmodel.productHelper.downloadInStoreLocations();

            PhotoCaptureHelper mPhotoCaptureHelper = PhotoCaptureHelper.getInstance(this);
            mPhotoCaptureHelper.downloadLocations(getApplicationContext());
            mPhotoCaptureHelper.downloadPhotoCaptureProducts(getApplicationContext());
            mPhotoCaptureHelper.downloadPhotoTypeMaster(getApplicationContext());
            mPhotoCaptureHelper.loadPhotoCaptureDetailsInEditMode(getApplicationContext(), bmodel.getRetailerMasterBO().getRetailerID());

            if (!isClick) {
                isClick = true;

                if (mPhotoCaptureHelper.getPhotoCaptureProductList().size() > 0
                        && mPhotoCaptureHelper.getPhotoTypeMaster().size() > 0) {

                    if (count >= 10
                            && count <= bmodel.configurationMasterHelper.photocount) {

                        Toast.makeText(
                                this,
                                getResources()
                                        .getString(
                                                R.string.its_highly_recommend_you_to_upload_the_images_before_capturing_new_image),
                                Toast.LENGTH_LONG).show();
                        finish();

                        bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                                DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                                DateTimeUtils.now(DateTimeUtils.TIME), menu.getConfigCode());

                        Intent intent = new Intent(HomeScreenTwo.this,
                                PhotoCaptureActivity.class);
                        intent.putExtra("screen_title", menu.getMenuName());
                        intent.putExtra("CurrentActivityCode", menu.getConfigCode());
                        if (isFromChild)
                            intent.putExtra("isFromChild", isFromChild);
                        startActivity(intent);

                    } else if (count >= bmodel.configurationMasterHelper.photocount) {

                        showGalleryAlert(
                                getResources()
                                        .getString(
                                                R.string.maximum_number_of_images_has_been_captured_without_upload_Do_upload_or_delete_images),
                                0);
                        isClick = false;

                    } else {
                        finish();

                        bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                                DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                                DateTimeUtils.now(DateTimeUtils.TIME), menu.getConfigCode());

                        Intent intent = new Intent(HomeScreenTwo.this,
                                PhotoCaptureActivity.class);
                        intent.putExtra("screen_title", menu.getMenuName());
                        intent.putExtra("CurrentActivityCode", menu.getConfigCode());
                        startActivity(intent);
                    }
                } else {

                    dataNotMapped();

                    isClick = false;
                    isCreated = false;

                    menuCode = (menuCodeList.get(menu.getConfigCode()) == null ? "" : menuCodeList.get(menu.getConfigCode()));
                    if (!menuCode.equals(menu.getConfigCode()))
                        menuCodeList.put(menu.getConfigCode(), menu.getConfigCode());
                }
            }
        } else {
            Toast.makeText(
                    this,
                    getResources().getString(
                            R.string.please_complete_previous_activity),
                    Toast.LENGTH_SHORT).show();
            isCreated = false;
        }
    }

    private void load_MENU_CLOSING(ConfigureBO menu) {
        if (isPreviousDone(menu)
                || bmodel.configurationMasterHelper.IS_JUMP
        ) {

            OrderHelper orderHelper = OrderHelper.getInstance(this);
            StockCheckHelper.getInstance(HomeScreenTwo.this).loadStockCheckConfiguration(HomeScreenTwo.this, bmodel.retailerMasterBO.getSubchannelid());
            /** Load the stock check if opened in edit mode. **/
            bmodel.setEditStockCheck(false);
            if (bmodel.hasAlreadyStockChecked(bmodel.getRetailerMasterBO()
                    .getRetailerID())) {
                bmodel.setEditStockCheck(true);
                bmodel.loadStockCheckedProducts(bmodel
                        .getRetailerMasterBO().getRetailerID(), menu.getConfigCode());
            }

            bmodel.setEdit(false);

            if (orderHelper.hasAlreadyOrdered(this, bmodel.getRetailerMasterBO()
                    .getRetailerID())) {
                bmodel.setEdit(true);

                /*if (bmodel.isEdit()) {
                    orderHelper.loadOrderedProducts(this, bmodel.getRetailerMasterBO()
                            .getRetailerID(), null);
                    orderHelper.loadSerialNo(this);
                    enableSchemeModule();
                }*/
                bmodel.productHelper.loadRetailerWiseProductWisePurchased();
                bmodel.productHelper
                        .loadRetailerWiseProductWiseP4StockAndOrderQty();
                bmodel.configurationMasterHelper
                        .downloadProductDetailsList();
                if (bmodel.configurationMasterHelper.IS_INITIATIVE) {
                    /** Load Initiative **/
                    bmodel.productHelper.loadInitiativeProducts();
                    bmodel.initiativeHelper.downloadInitiativeHeader(bmodel
                            .getRetailerMasterBO().getSubchannelid());
                    /** Load Order History **/
                    bmodel.initiativeHelper.loadLocalOrdersQty(bmodel
                            .getRetailerMasterBO().getRetailerID());
                }

                /** Load SO Norm **/
                if (bmodel.configurationMasterHelper.IS_SUGGESTED_ORDER) {
                    bmodel.productHelper
                            .loadRetailerWiseInventoryOrderQty();
                }

                if (bmodel.configurationMasterHelper.IS_PRODUCT_DISPLAY_FOR_PIRAMAL)
                    bmodel.productHelper.updateProductColorAndSequance();

                /** Settign color **/
                bmodel.configurationMasterHelper.downloadFilterList();
                bmodel.productHelper.updateProductColor();
                bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                        DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                        DateTimeUtils.now(DateTimeUtils.TIME), menu.getConfigCode());

//                    OrderSummary.mCurrentActivityCode = menu.getConfigCode();
//
//                    Intent i = new Intent(HomeScreenTwo.this,
//                            OrderSummary.class);
//                    i.putExtra("FromClose", "Closing");
//                    startActivity(i);
//                    finish();

                bmodel.productHelper.downloadIndicativeOrderList();//moved here to check size of indicative order
                orderHelper.selectedOrderId = "";
                if (bmodel.productHelper.getIndicativeList() != null
                        && bmodel.productHelper.getIndicativeList().size() < 1
                        && bmodel.configurationMasterHelper.IS_MULTI_STOCKORDER) {
                    if (bmodel.isEdit()) {
                        orderHelper.selectedOrderId = "";//cleared to avoid reuse of id
                        final String menuConfigCode = menu.getConfigCode();
                        final String menuName = menu.getMenuName();
                        OrderTransactionListDialog obj = new OrderTransactionListDialog(getApplicationContext(), HomeScreenTwo.this, new OrderTransactionListDialog.newOrderOnClickListener() {
                            @Override
                            public void onNewOrderButtonClick() {
                                //the methods that were called during normal stock and order loading in non edit mode are called here
                                //loadOrderedProducts,loadSerialNo,enableSchemeModule are used in edit mode so avoided here as in this case screen should be loaded fresh
                                bmodel.setOrderHeaderBO(null);
                                loadRequiredMethodsForStockAndOrder(menuConfigCode, menuName);
                                loadstockorderscreen(menuConfigCode);
                            }
                        }, new OrderTransactionListDialog.oldOrderOnClickListener() {
                            @Override
                            public void onOldOrderButtonClick(String id) {
                                OrderHelper.getInstance(HomeScreenTwo.this).selectedOrderId = id;
                                //the methods that were called during normal stock and order loading in edit mode are called here
                                //selectedOrderId is passed to loadOrderedProducts method  to load ordered products for that id
                                //loadSerialNo,enableSchemeModule included as these were called in edit mode
                                OrderHelper.getInstance(HomeScreenTwo.this).loadOrderedProducts(HomeScreenTwo.this, bmodel.getRetailerMasterBO()
                                        .getRetailerID(), id);
                                OrderHelper.getInstance(HomeScreenTwo.this).loadSerialNo(HomeScreenTwo.this);
                                enableSchemeModule();
                                loadRequiredMethodsForStockAndOrder(menuConfigCode, menuName);
                                loadOrderSummaryScreen(menuConfigCode);
                            }
                        }, true, new OrderTransactionListDialog.OnDismissListener() {
                            @Override
                            public void onDismiss() {
                                isCreated = false;
                            }
                        });
                        obj.show();
                        obj.setCancelable(false);
                    } else {
                        OrderHelper.getInstance(this).isQuickCall = false;
                        OrderSummary.mCurrentActivityCode = menu.getConfigCode();

                        Intent i = new Intent(HomeScreenTwo.this,
                                OrderSummary.class);
                        i.putExtra("FromClose", "Closing");
                        i.putExtra("ScreenCode", menu.getConfigCode());
                        startActivity(i);
                        finish();
                    }
                } else {
                    if (bmodel.isEdit()) {
                        orderHelper.loadOrderedProducts(this, bmodel.getRetailerMasterBO()
                                .getRetailerID(), null);
                        OrderHelper.getInstance(this).selectedOrderId = orderHelper.getOrderId();
                        orderHelper.loadSerialNo(this);
                        enableSchemeModule();
                    }
                    loadRequiredMethodsForStockAndOrder(menu.getConfigCode(), menu.getMenuName());
                    if (bmodel.isEdit()) {
                        loadOrderSummaryScreen(menu.getConfigCode());

                    } else {
                        loadstockorderscreen(menu.getConfigCode());
                    }
                }

            } else {
                Toast.makeText(
                        this,
                        getResources()
                                .getString(R.string.no_order_to_close),
                        Toast.LENGTH_SHORT).show();
                isCreated = false;
            }
        } else {

            Toast.makeText(
                    this,
                    getResources().getString(
                            R.string.please_complete_previous_activity),
                    Toast.LENGTH_SHORT).show();
            isCreated = false;
        }
    }

    private void load_MENU_ORDER(ConfigureBO menu) {
        if (isPreviousDone(menu)
                || bmodel.configurationMasterHelper.IS_JUMP
        ) {
            if (!isClick) {
                isClick = true;
                StockCheckHelper.getInstance(HomeScreenTwo.this).loadStockCheckConfiguration(HomeScreenTwo.this, bmodel.retailerMasterBO.getSubchannelid());

                if (bmodel.configurationMasterHelper
                        .downloadFloatingSurveyConfig(MENU_ORDER)) {
                    SurveyHelperNew surveyHelperNew = SurveyHelperNew.getInstance(this);
                    surveyHelperNew.setFromHomeScreen(false);
                    surveyHelperNew.downloadModuleId("STANDARD");
                    surveyHelperNew.downloadQuestionDetails(MENU_ORDER);
                    surveyHelperNew.loadSurveyAnswers(0);
                }

                OrderHelper orderHelper = OrderHelper.getInstance(this);
                if (bmodel.productHelper.getProductMaster().size() > 0) {


                    if (bmodel.configurationMasterHelper.SHOW_SALES_RETURN_IN_ORDER
                            || bmodel.configurationMasterHelper.SHOW_SALES_RETURN_TV_IN_ORDER) {
                        SalesReturnHelper salesReturnHelper = SalesReturnHelper.getInstance(this);
                        salesReturnHelper.loadSalesReturnConfigurations(getApplicationContext());
                        bmodel.reasonHelper.downloadSalesReturnReason();
                        if (bmodel.reasonHelper.getReasonSalesReturnMaster().size() > 0) {
                            salesReturnHelper.getInstance(this).cloneReasonMaster(true);
//
                            salesReturnHelper.getInstance(this).clearSalesReturnTable(true);
//
////                        if (!bmodel.configurationMasterHelper.IS_INVOICE) {
                            salesReturnHelper.getInstance(this).removeSalesReturnTable(true);
////                        }
                            if (bmodel.configurationMasterHelper.SHOW_SALES_RETURN_TV_IN_ORDER) {
                                salesReturnHelper.getInstance(HomeScreenTwo.this).loadSalesReturnData(getApplicationContext(), "", "", bmodel.configurationMasterHelper.SHOW_SALES_RETURN_TV_IN_ORDER);
                            }
                        }
                    }

                    bmodel.configurationMasterHelper.downloadFloatingNPReasonWithPhoto(MENU_ORDER);

                    if ((!bmodel.configurationMasterHelper.IS_VALIDATE_DUE_DAYS || bmodel.productHelper.isDueDateExpired()) && (!bmodel.configurationMasterHelper.IS_VALIDATE_CREDIT_DAYS
                            || bmodel.getRetailerMasterBO().getCreditDays() == 0
                            || bmodel.productHelper.isCheckCreditPeriod())) {

                        if (bmodel.configurationMasterHelper.SHOW_STK_QTY_IN_ORDER) {
                            if (bmodel.hasAlreadyStockChecked(bmodel
                                    .getRetailerMasterBO().getRetailerID()) && !bmodel.configurationMasterHelper.IS_LOAD_STK_CHECK_LAST_VISIT) {
                                bmodel.loadStockCheckedProducts(bmodel
                                        .getRetailerMasterBO().getRetailerID(), menu.getConfigCode());
                            } else if (bmodel.configurationMasterHelper.IS_LOAD_STK_CHECK_LAST_VISIT) {
                                clearStockCheck();
                                bmodel.loadLastVisitStockCheckedProducts(bmodel
                                        .getRetailerMasterBO().getRetailerID(), menu.getConfigCode());
                            }
                        }

                        bmodel.setEdit(false);
                        if (orderHelper.hasAlreadyOrdered(this, bmodel.getRetailerMasterBO()
                                .getRetailerID())) {
                            bmodel.setEdit(true);
                        } else {
                            bmodel.setOrderHeaderBO(null);
                        }

                        if (bmodel.configurationMasterHelper.IS_SHOW_ORDERING_SEQUENCE) {
                            bmodel.productHelper.getmProductidOrderByEntry().clear();
                            bmodel.productHelper.getmProductidOrderByEntryMap().clear();
                        }

                        if (bmodel.configurationMasterHelper.SHOW_DISC_AMOUNT_ALLOW) {
                            collectionHelper.downloadDiscountSlab();
                        }
                        if (bmodel.configurationMasterHelper.SHOW_COLLECTION_BEFORE_INVOICE)
                            collectionHelper.loadCreditNote();
                        //   bmodel.productHelper.downloadProductFilter("MENU_STK_ORD"); /*03/09/2015*/
                        bmodel.productHelper.loadRetailerWiseProductWisePurchased();
                        bmodel.productHelper
                                .loadRetailerWiseProductWiseP4StockAndOrderQty();
                        bmodel.configurationMasterHelper
                                .downloadProductDetailsList();
                        collectionHelper.downloadBankDetails();
                        collectionHelper.downloadBranchDetails();
                        collectionHelper.downloadRetailerAccountDetails();
                        if (bmodel.configurationMasterHelper.IS_SUGGESTED_ORDER) {
                            bmodel.productHelper
                                    .loadRetailerWiseInventoryOrderQty();
                        }

                        if (bmodel.configurationMasterHelper.IS_PRODUCT_DISPLAY_FOR_PIRAMAL)
                            bmodel.productHelper.updateProductColorAndSequance();

                        if (bmodel.configurationMasterHelper.IS_INITIATIVE) {
                            bmodel.productHelper.loadInitiativeProducts();
                            bmodel.initiativeHelper.loadLocalOrdersQty(bmodel
                                    .getRetailerMasterBO().getRetailerID());
                            bmodel.initiativeHelper.downloadInitiativeHeader(bmodel
                                    .getRetailerMasterBO().getSubchannelid());
                        }

                        /** Settign color **/
                        bmodel.configurationMasterHelper.downloadFilterList();
                        bmodel.productHelper.updateProductColor();
                        bmodel.orderAndInvoiceHelper.restoreDiscountAmount(bmodel.getRetailerMasterBO().getRetailerID());

                        if (schemeHelper.IS_SCHEME_ON_MASTER)
                            schemeHelper.downloadSchemeHistoryDetails(getApplicationContext(), bmodel.getRetailerMasterBO().getRetailerID(), bmodel.isEdit(), orderHelper.selectedOrderId);


                        bmodel.productHelper.downloadInStoreLocations();

                        OrderSummary.mCurrentActivityCode = menu.getConfigCode();

                        //load currency data
                        if (bmodel.configurationMasterHelper.IS_FORMAT_USING_CURRENCY_VALUE) {
                            bmodel.downloadCurrencyConfig();
                        }


                        if (bmodel.isEdit()) {


                            bmodel.productHelper.downloadIndicativeOrderList();

                            if (bmodel.productHelper.getIndicativeList() != null
                                    && bmodel.productHelper.getIndicativeList().size() < 1
                                    && bmodel.configurationMasterHelper.IS_MULTI_STOCKORDER) {

                                orderHelper.selectedOrderId = "";//cleared to avoid reuse of id
                                final String menuConfigCode = menu.getConfigCode();
                                final String menuName = menu.getMenuName();
                                OrderTransactionListDialog obj = new OrderTransactionListDialog(getApplicationContext(), HomeScreenTwo.this, new OrderTransactionListDialog.newOrderOnClickListener() {
                                    @Override
                                    public void onNewOrderButtonClick() {
                                        //the methods that were called during normal stock and order loading in non edit mode are called here
                                        //loadOrderedProducts,loadSerialNo,enableSchemeModule are used in edit mode so avoided here as in this case screen should be loaded fresh
                                        bmodel.setOrderHeaderBO(null);
                                        bmodel.setEdit(false);
                                        OrderHelper.getInstance(HomeScreenTwo.this).selectedOrderId = "";
                                        loadRequiredMethodsforOrder(menuConfigCode);
                                    }
                                }, new OrderTransactionListDialog.oldOrderOnClickListener() {
                                    @Override
                                    public void onOldOrderButtonClick(String id) {
                                        OrderHelper.getInstance(HomeScreenTwo.this).selectedOrderId = id;
                                        //the methods that were called during normal stock and order loading in edit mode are called here
                                        //selectedOrderId is passed to loadOrderedProducts method  to load ordered products for that id
                                        //loadSerialNo,enableSchemeModule included as these were called in edit mode
                                        OrderHelper.getInstance(HomeScreenTwo.this).loadOrderedProducts(HomeScreenTwo.this, bmodel.getRetailerMasterBO()
                                                .getRetailerID(), id);
                                        OrderHelper.getInstance(HomeScreenTwo.this).loadSerialNo(HomeScreenTwo.this);
                                        enableSchemeModule();
                                        loadOrderSummaryScreen(menuConfigCode);
                                    }
                                }, false, new OrderTransactionListDialog.OnDismissListener() {
                                    @Override
                                    public void onDismiss() {
                                        isCreated = false;
                                        isClick = false;
                                    }
                                });
                                obj.show();
                                obj.setCancelable(false);
                            } else {
                                orderHelper.loadOrderedProducts(this, bmodel.getRetailerMasterBO()
                                        .getRetailerID(), null);
                                orderHelper.loadSerialNo(this);
                                enableSchemeModule();

                                Intent intent = new Intent(HomeScreenTwo.this,
                                        OrderSummary.class);
                                intent.putExtra("ScreenCode", "MENU_ORDER");
                                startActivity(intent);
                                finish();

                            }
                        } else {
                            loadRequiredMethodsforOrder(menu.getConfigCode());
                        }

                    } else {
                        Toast.makeText(
                                this,
                                getResources().getString(
                                        R.string.please_pay_old_invoice),
                                Toast.LENGTH_SHORT).show();
                        isCreated = false;
                        isClick = false;
                    }


                } else {
                    dataNotMapped();
                    isCreated = false;
                    isClick = false;
                    menuCode = (menuCodeList.get(menu.getConfigCode()) == null ? "" : menuCodeList.get(menu.getConfigCode()));
                    if (!menuCode.equals(menu.getConfigCode()))
                        menuCodeList.put(menu.getConfigCode(), menu.getConfigCode());
                }
            }
        } else {
            Toast.makeText(
                    this,
                    getResources().getString(
                            R.string.please_complete_previous_activity),
                    Toast.LENGTH_SHORT).show();
            isCreated = false;
            isClick = false;
        }
    }

    private void load_MENU_STOCK(ConfigureBO menu, boolean isFromChild) {
        if (isPreviousDone(menu)
                || bmodel.configurationMasterHelper.IS_JUMP || isPreVisit) {

            StockCheckHelper stockCheckHelper = StockCheckHelper.getInstance(this);
            // More than 15 characters not allowed in sync. So code shortened..
            if (menu.getConfigCode().equals(MENU_COMBINED_STOCK)) {
                stockCheckHelper.loadCmbStkChkConfiguration(this, bmodel.retailerMasterBO.getSubchannelid());
                ProductTaggingHelper.getInstance(this).downloadTaggedProducts(this, "MENU_COMB_STK");
            } else {
                stockCheckHelper.loadStockCheckConfiguration(this, bmodel.retailerMasterBO.getSubchannelid());
                ProductTaggingHelper.getInstance(this).downloadTaggedProducts(this, MENU_STOCK);
            }

            /** Download location to load in the filter. **/
            bmodel.productHelper.downloadInStoreLocations();


            if (bmodel.configurationMasterHelper.IS_LOAD_STOCK_COMPETITOR) {
                if (bmodel.configurationMasterHelper.SHOW_COMPETITOR_FILTER) {
                    bmodel.productHelper.downloadCompetitorFiveFilterLevels();
                }
                bmodel.productHelper.downloadCompetitorProducts(MENU_STOCK);
                if (menu.getConfigCode().equals(MENU_COMBINED_STOCK))
                    ProductTaggingHelper.getInstance(this).downloadCompetitorTaggedProducts(this, "MENU_COMB_STK");
                else
                    ProductTaggingHelper.getInstance(this).downloadCompetitorTaggedProducts(this, menu.getConfigCode());
            }

            if (ProductTaggingHelper.getInstance(this).getTaggedProducts().size() > 0) {
                if (stockCheckHelper.SHOW_STOCK_AVGDAYS && menu.getConfigCode().equals(MENU_COMBINED_STOCK))
                    bmodel.productHelper.loadRetailerWiseInventoryFlexQty();

                if (bmodel.configurationMasterHelper
                        .downloadFloatingSurveyConfig(MENU_STOCK)) {
                    SurveyHelperNew surveyHelperNew = SurveyHelperNew.getInstance(this);
                    surveyHelperNew.setFromHomeScreen(false);
                    surveyHelperNew.downloadModuleId("STANDARD");
                    surveyHelperNew.downloadQuestionDetails(MENU_STOCK);
                    surveyHelperNew.loadSurveyAnswers(0);
                }

                bmodel.configurationMasterHelper.downloadFloatingNPReasonWithPhoto(menu.getConfigCode());

                if (bmodel.hasAlreadyStockChecked(bmodel.getRetailerMasterBO()
                        .getRetailerID())) {
                    bmodel.setEditStockCheck(true);
                    bmodel.loadStockCheckedProducts(bmodel
                            .getRetailerMasterBO().getRetailerID(), menu.getConfigCode());

                    if (bmodel.configurationMasterHelper.SHOW_NEAREXPIRY_IN_STOCKCHECK
                            && bmodel.configurationMasterHelper.IS_RETAIN_NEAREXPIRY_CURRENT_TRAN_IN_STOCKCHECK) {
                        NearExpiryTrackingHelper mNearExpiryHelper = NearExpiryTrackingHelper.getInstance(this);
                        mNearExpiryHelper.loadSKUTracking(getApplicationContext(), true);
                    }

                    if (bmodel.configurationMasterHelper.SHOW_PRICECHECK_IN_STOCKCHECK) {
                        PriceTrackingHelper priceTrackingHelper = PriceTrackingHelper.getInstance(this);
                        priceTrackingHelper.loadPriceTransaction(getApplicationContext());
                        if (bmodel.configurationMasterHelper.IS_PRICE_CHECK_RETAIN_LAST_VISIT_IN_EDIT_MODE && !priceTrackingHelper.isPriceCheckDone(getApplicationContext())) {
                            priceTrackingHelper.updateLastVisitPriceAndMRP();
                        }
                    }
                } else {// to laod data from last vist transaction tables
                    boolean isDataAvailableforLastVisitHistory = false;
                    if (bmodel.configurationMasterHelper.IS_ENABLE_LAST_VISIT_HISTORY) {
                        // load last visit data
                        isDataAvailableforLastVisitHistory =
                                bmodel.loadLastVisitHistoryStockCheckedProducts(bmodel.getRetailerMasterBO().getRetailerID());
                    }

                    if ((!bmodel.configurationMasterHelper.IS_ENABLE_LAST_VISIT_HISTORY || !isDataAvailableforLastVisitHistory) &&
                            bmodel.configurationMasterHelper.IS_STOCK_CHECK_RETAIN_LAST_VISIT_TRAN) {
                        // load last visit data
                        bmodel.loadLastVisitStockCheckedProducts(bmodel.getRetailerMasterBO().getRetailerID(), "MENU_STOCK");
                    }


                    //load Last Vist Near Expir Data
                    if (bmodel.configurationMasterHelper.SHOW_NEAREXPIRY_IN_STOCKCHECK) {
                        NearExpiryTrackingHelper mNearExpiryHelper = NearExpiryTrackingHelper.getInstance(this);
                        mNearExpiryHelper.loadSKUTracking(getApplicationContext(), true);
                        if (bmodel.configurationMasterHelper.IS_NEAR_EXPIRY_RETAIN_LAST_VISIT_TRAN && !mNearExpiryHelper.hasAlreadySKUTrackingDone(getApplicationContext())) {
                            mNearExpiryHelper.loadLastVisitSKUTracking(getApplicationContext());
                        }
                    }

                    //Load Last Visit Price Check Data
                    if (bmodel.configurationMasterHelper.SHOW_PRICECHECK_IN_STOCKCHECK) {
                        PriceTrackingHelper priceTrackingHelper = PriceTrackingHelper.getInstance(this);
                        priceTrackingHelper.clearPriceCheck();
                        priceTrackingHelper.loadPriceTransaction(getApplicationContext());
                        if (bmodel.configurationMasterHelper.IS_PRICE_CHECK_RETAIN_LAST_VISIT_IN_EDIT_MODE && !priceTrackingHelper.isPriceCheckDone(getApplicationContext())) {
                            priceTrackingHelper.updateLastVisitPriceAndMRP();
                        }
                    }

                }

                if (bmodel.configurationMasterHelper.IS_INITIATIVE) {
                    /** Following should load module wise **/
                    bmodel.initiativeHelper.loadLocalOrdersQty(bmodel
                            .getRetailerMasterBO().getRetailerID());
                    /**
                     * loadInitiativeProducts is not required to be called on
                     * every module
                     **/
                    bmodel.productHelper.loadInitiativeProducts();
                }

                /** Following is not required to be called in every module **/
                bmodel.productHelper.loadRetailerWiseProductWisePurchased();
                bmodel.productHelper.loadRetailerWiseProductWiseP4StockAndOrderQty();

                if (!isPreVisit)
                    bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                            DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                            DateTimeUtils.now(DateTimeUtils.TIME), menu.getConfigCode());

                /**
                 * Download product long-press information dialog
                 * configurations.
                 **/
                bmodel.configurationMasterHelper.downloadProductDetailsList();

                // Load Data for Special Filter
                bmodel.configurationMasterHelper.downloadFilterList();
                bmodel.productHelper.updateProductColor();
                bmodel.productHelper.loadRetailerWiseProductWiseP4StockAndOrderQty();


                /** Load the screen **/
                Intent intent;
                if (menu.getConfigCode().equals(MENU_COMBINED_STOCK)) {
                    intent = new Intent(HomeScreenTwo.this,
                            CombinedStockFragmentActivity.class);
                    intent.putExtra("CurrentActivityCode", menu.getConfigCode());
                    if (isFromChild)
                        intent.putExtra("isFromChild", true);
                } else {
                    intent = new Intent(HomeScreenTwo.this,
                            StockCheckActivity.class);
                    intent.putExtra("CurrentActivityCode", menu.getConfigCode());
                    if (isFromChild)
                        intent.putExtra("isFromChild", true);
                }

                if (isPreVisit)
                    intent.putExtra("PreVisit", true);

                bmodel.mSelectedActivityName = menu.getMenuName();
                startActivity(intent);
                finish();

            } else {
                dataNotMapped();
                isCreated = false;
                menuCode = (menuCodeList.get(menu.getConfigCode()) == null ? "" : menuCodeList.get(menu.getConfigCode()));
                if (!menuCode.equals(menu.getConfigCode()))
                    menuCodeList.put(menu.getConfigCode(), menu.getConfigCode());
            }
        } else {
            Toast.makeText(
                    this,
                    getResources().getString(
                            R.string.please_complete_previous_activity),
                    Toast.LENGTH_SHORT).show();
            isCreated = false;
        }

    }

    private void loadOrderSummaryScreen(String menuConfigCode) {
        OrderHelper.getInstance(this).isQuickCall = false;
        Intent intent = new Intent(HomeScreenTwo.this,
                OrderSummary.class);
        if (menuConfigCode.equals(MENU_CATALOG_ORDER)) {
//                            bmodel.productHelper
//                                    .downloadFiveFilterLevels("MENU_STK_ORD");
            intent.putExtra("ScreenCode", MENU_CATALOG_ORDER);
        } else if (menuConfigCode.equals(MENU_ORDER)) {
            intent.putExtra("ScreenCode", MENU_ORDER);
        } else {
            intent.putExtra("ScreenCode", "MENU_STK_ORD");
        }
        //intent.putExtra("ScreenCode", "MENU_STK_ORD");
        startActivity(intent);
        finish();
    }

    private void loadRequiredMethodsForStockAndOrder(String configCode, String menuName) {
        try {

            if (bmodel.configurationMasterHelper.IS_GUIDED_SELLING) {
                bmodel.downloadGuidedSelling();
            }

            if (bmodel.configurationMasterHelper.SHOW_DISC_AMOUNT_ALLOW) {
                collectionHelper.downloadDiscountSlab();
            }

            //  bmodel.productHelper.downloadProductFilter("MENU_STK_ORD"); /*03/09/2015*/
            bmodel.productHelper.loadRetailerWiseProductWisePurchased();
            bmodel.productHelper
                    .loadRetailerWiseProductWiseP4StockAndOrderQty();

            bmodel.configurationMasterHelper
                    .downloadProductDetailsList();

            if (bmodel.configurationMasterHelper.IS_INITIATIVE) {
                /** Load Initiative **/
                bmodel.productHelper.loadInitiativeProducts();
                bmodel.initiativeHelper.downloadInitiativeHeader(bmodel
                        .getRetailerMasterBO().getSubchannelid());
                /** Load Order History **/
                bmodel.initiativeHelper.loadLocalOrdersQty(bmodel
                        .getRetailerMasterBO().getRetailerID());
            }

            /** Load SO Norm **/
            if (bmodel.configurationMasterHelper.IS_SUGGESTED_ORDER) {
                bmodel.productHelper
                        .loadRetailerWiseInventoryOrderQty();
            }

            if (bmodel.configurationMasterHelper.IS_PRODUCT_DISPLAY_FOR_PIRAMAL)
                bmodel.productHelper.updateProductColorAndSequance();

            /** Settign color **/
            bmodel.configurationMasterHelper.downloadFilterList();
            bmodel.productHelper.updateProductColor();

            DiscountHelper discountHelper = DiscountHelper.getInstance(this);
            if (bmodel.configurationMasterHelper.SHOW_STORE_WISE_DISCOUNT_DLG) {

                discountHelper.downloadBillWiseDiscount(this);
                discountHelper.loadExistingBillWiseRangeDiscount(this);
            }
            // apply bill wise pay term discount
            discountHelper.downloadBillWisePayTermDiscount(this);

            bmodel.productHelper.downloadInStoreLocations();

            OrderHelper orderHelper = OrderHelper.getInstance(this);
            if (schemeHelper.IS_SCHEME_ON_MASTER)
                schemeHelper.downloadSchemeHistoryDetails(getApplicationContext(), bmodel.getRetailerMasterBO().getRetailerID(), bmodel.isEdit(), orderHelper.selectedOrderId);
            schemeHelper.downloadOffInvoiceSchemeDetails(getApplicationContext(), bmodel.getRetailerMasterBO().getRetailerID());


            if (bmodel.configurationMasterHelper.SHOW_COLLECTION_BEFORE_INVOICE) {
                collectionHelper.downloadBankDetails();
                collectionHelper.downloadBranchDetails();
                collectionHelper.downloadRetailerAccountDetails();
                collectionHelper.loadCreditNote();
            }

            bmodel.updateProductUOM(StandardListMasterConstants.mActivityCodeByMenuCode.get(MENU_STK_ORD), 1);


            if (bmodel.configurationMasterHelper.IS_FORMAT_USING_CURRENCY_VALUE) {
                bmodel.downloadCurrencyConfig();
            }

        } catch (Exception e) {
            Commons.printException(e);
        }

        OrderSummary.mCurrentActivityCode = configCode;
        bmodel.mSelectedActivityName = menuName;
    }


    public void loadstockorderscreen(String menu) {
        {
            indicativeOrderAdapter = new ArrayAdapter<Integer>(this,
                    android.R.layout.select_dialog_singlechoice);

            for (Integer temp : bmodel.productHelper
                    .getIndicativeList())
                indicativeOrderAdapter.add(temp);
            if (bmodel.configurationMasterHelper.SHOW_INVOICE_CREDIT_BALANCE &&
                    "CREDIT".equals(bmodel.getRetailerMasterBO().getRpTypeCode())) {
                if (bmodel.getRetailerMasterBO()
                        .getCredit_balance() == -1
                        || bmodel.getRetailerMasterBO()
                        .getCredit_balance() > 0) {

                    if (bmodel.productHelper.getIndicativeList() != null) {
                        if (bmodel.productHelper.getIndicativeList().size() > 1) {
                            showIndicativeOrderFilterAlert(menu);
                            return;
                        }
                        if (bmodel.productHelper.getIndicativeList().size() > 0) {
                            if (!bmodel.productHelper.isAlreadyIndicativeOrderTaken(bmodel.productHelper.getIndicativeList().get(0))
                                    && !bmodel.isEdit()) {
                                bmodel.productHelper.downloadIndicativeOrder(bmodel.productHelper.getIndicativeList().get(0));
                                bmodel.productHelper.updateIndicateOrder();
                            }
                        }
                    }
                    bmodel.outletTimeStampHelper
                            .saveTimeStampModuleWise(
                                    DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                                    DateTimeUtils.now(DateTimeUtils.TIME),
                                    menu);
                    OrderHelper.getInstance(this).isQuickCall = false;
                    Intent intent = new Intent(HomeScreenTwo.this,
                            StockAndOrder.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                    finish();
                } else {
                    showDialog(INVOICE_CREDIT_BALANCE);
                }
            } else {
                bmodel.outletTimeStampHelper
                        .saveTimeStampModuleWise(
                                DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                                DateTimeUtils.now(DateTimeUtils.TIME),
                                menu);

                if (bmodel.productHelper.getIndicativeList() != null) {
                    if (bmodel.productHelper.getIndicativeList().size() > 1) {
                        showIndicativeOrderFilterAlert(menu);
                        return;
                    }
                    if (bmodel.productHelper.getIndicativeList().size() > 0) {
                        if (!bmodel.productHelper.isAlreadyIndicativeOrderTaken(bmodel.productHelper.getIndicativeList().get(0))
                                && !bmodel.isEdit()) {
                            bmodel.productHelper.downloadIndicativeOrder(bmodel.productHelper.getIndicativeList().get(0));
                            bmodel.productHelper.updateIndicateOrder();
                        }
                    }
                }
//                            Intent intent = new Intent(HomeScreenTwo.this,
//                                    StockAndOrder.class);
                OrderHelper.getInstance(this).isQuickCall = false;

                Intent intent;
                if (menu.equals(HomeScreenTwo.MENU_CATALOG_ORDER)) {
//                                bmodel.productHelper
//                                        .downloadFiveFilterLevels("MENU_STK_ORD");
                    intent = new Intent(HomeScreenTwo.this,
                            CatalogOrder.class);
                } else {
                    intent = new Intent(HomeScreenTwo.this,
                            StockAndOrder.class);
                }
                startActivity(intent);
                finish();
            }
        }
    }

    public boolean canAllowCallAnalysis() {
        for (ConfigureBO config : menuDB) {
            if (config.getHasLink() == 1 && config.isDone()
                    && !config.getConfigCode().equals(MENU_REV)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAllMandatoryMenuDone() {
        int count = 0;
        int tepmcount = 0;
        if (menuCodeList.size() == 0)
            menuCodeList.put("MENU_CODE", "");

        for (ConfigureBO config : menuDB) {
            if (config.getMandatory() == 1 && !config.isDone()
                    && !config.getConfigCode().equals(MENU_REV)) {
                count = count + 1;
                if ((menuCodeList.get(config.getConfigCode()) == null ? "" : menuCodeList.get(config.getConfigCode())).equals(config.getConfigCode()))
                    tepmcount = tepmcount + 1;
            }

        }

        if (isStoreCheckMenu) {
            for (ConfigureBO config : mInStoreMenu) {
                if (config.getMandatory() == 1 && !config.isDone()
                        && !config.getConfigCode().equals("MENU_CLOSE")) {
                    count = count + 1;
                    if ((menuCodeList.get(config.getConfigCode()) == null ? "" : menuCodeList.get(config.getConfigCode())).equals(config.getConfigCode()))
                        tepmcount = tepmcount + 1;

                }

            }
        }
        return (count == tepmcount) ? true : false;
    }

    /**
     * This will return true if MENU_INVOICE is enable.
     *
     * @return true or false
     */
    private boolean isInvoiceAsModule() {
        for (ConfigureBO config : menuDB) {
            if (config.getHasLink() == 1
                    && config.getConfigCode().equals(MENU_INVOICE)
                    && config.isFlag() == 1) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {

            case INVOICE_CREDIT_BALANCE:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(HomeScreenTwo.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(
                                getResources()
                                        .getString(
                                                R.string.order_not_allowed_credit_balance_zero))
                        .setPositiveButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        isClick = false;
                                        isCreated = false;

                                    }
                                });
                bmodel.applyAlertDialogTheme(builder1);
                break;

            case SALES_TYPES:
                AlertDialog.Builder builder2 = new AlertDialog.Builder(HomeScreenTwo.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(getString(R.string.plain_select))
                        .setSingleChoiceItems(mSalesTypeArray,
                                bmodel.getRetailerMasterBO().getIsVansales(),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        String selectedType = mSalesTypeArray[which];
                                        if (selectedType.equals(VAN_SALES)) {
                                            bmodel.configurationMasterHelper.
                                                    updateConfigurationSelectedSellerType(false);
                                            updateRetailerwiseSellertype(1); // Vansales
                                            bmodel.getAppDataProvider().getRetailMaster()
                                                    .setIsVansales(1);

                                        } else {
                                            bmodel.configurationMasterHelper.
                                                    updateConfigurationSelectedSellerType(true);
                                            updateRetailerwiseSellertype(0); // Presales
                                            bmodel.getAppDataProvider().getRetailMaster()
                                                    .setIsVansales(0);
                                        }
                                        if (bmodel.configurationMasterHelper.IS_SWITCH_SELLER_CONFIG_LEVEL) {
                                            new DownloadProductsAndPrice(HomeScreenTwo.this, "", "",
                                                    "", "", false).execute();
                                        }
                                        dialog.dismiss();

                                    }
                                });
                bmodel.applyAlertDialogTheme(builder2);
                break;

            case ORDER_TYPES:
                AlertDialog.Builder builder4 = new AlertDialog.Builder(HomeScreenTwo.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(getString(R.string.select_order_type))
                        .setSingleChoiceItems(mOrderTypeAadapter,
                                mOrderTypeCheckedItem,
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        final String typeId = mOrderTypeList.get(which).getListID();
                                        bmodel.getRetailerMasterBO().setOrderTypeId(typeId);
                                        mOrderTypeCheckedItem = which;
                                        dialog.dismiss();

                                    }
                                });
                bmodel.applyAlertDialogTheme(builder4);
                break;

            case MANDATORY_MODULE_CLOSE_CALL:
                AlertDialog.Builder builder5 = new AlertDialog.Builder(HomeScreenTwo.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(getResources().getString(
                                R.string.please_finish_mandatory_modules))
                        .setMessage(getMandatoryModules(1))
                        .setPositiveButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        isClick = false;

                                        dialog.dismiss();

                                    }

                                });

                bmodel.applyAlertDialogTheme(builder5);
                break;

            case MANDATORY_MODULE:
                String mandatoryStr = getMandatoryModules(2);
                if (mandatoryStr.length() > 0) {
                    AlertDialog.Builder builder6 = new AlertDialog.Builder(HomeScreenTwo.this)
                            .setIcon(null)
                            .setCancelable(false)
                            .setTitle(getResources().getString(
                                    R.string.please_finish_mandatory_modules))
                            .setMessage(mandatoryStr)
                            .setPositiveButton(getResources().getString(R.string.ok),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int whichButton) {
                                            dialog.dismiss();

                                        }

                                    });

                    bmodel.applyAlertDialogTheme(builder6);
                }
                break;


        }

        return null;
    }

    private String getMandatoryModules(int flag) {
        StringBuilder sb = new StringBuilder();

        for (ConfigureBO config : menuDB) {
            if (config.getMandatory() == 1 && !config.isDone()
                    && !config.getConfigCode().equals(MENU_CALL_ANLYS)
                    && !config.getConfigCode().equals(MENU_CLOSE_CALL) && !config.getConfigCode().equals(MENU_CLOSE_KLGS)) {

                if (flag == 1)
                    sb.append(config.getMenuName() + " "
                            + getResources().getString(R.string.is_not_done) + "\n");
                else if (flag == 2)
                    sb.append(getResources().getString(R.string.please_complete) + " " + config.getMenuName() +
                            "\n");
            }

        }

        if (isStoreCheckMenu) {
            for (ConfigureBO config : mInStoreMenu) {
                if (config.getMandatory() == 1 && !config.isDone()
                        && !config.getConfigCode().equals("MENU_CLOSE")) {

                    if (flag == 1)
                        sb.append(config.getMenuName() + " "
                                + getResources().getString(R.string.is_not_done) + "\n");
                    else if (flag == 2)
                        sb.append(getResources().getString(R.string.please_complete) + " " + config.getMenuName() +
                                "\n");
                }

            }
        }

        return sb.toString();

    }

    public void showGalleryAlert(String msg, int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                HomeScreenTwo.this);
        builder.setCancelable(false);
        builder.setMessage(msg);
        builder.setPositiveButton(getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        isCreated = false;

                    }

                });
        builder.setNegativeButton(getResources().getString(R.string.gallery),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(HomeScreenTwo.this, Gallery.class);
                        i.putExtra("IsFromHome", true);
                        startActivity(i);
                    }
                });

        bmodel.applyAlertDialogTheme(builder);

    }

    public void showsurveyAlert(String msg, int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                HomeScreenTwo.this);
        builder.setCancelable(false);
        builder.setMessage(msg);
        builder.setPositiveButton(getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        isCreated = false;
                    }

                });

        bmodel.applyAlertDialogTheme(builder);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // force the garbage collector to run
        System.gc();
    }


    private boolean isPreviousDone(ConfigureBO config) {

        try {
            for (int i = 0; i < menuDB.size(); i++) {
                if (menuDB.get(i).getConfigCode()
                        .equals(config.getConfigCode())) {
                    Commons.print("prev" + menuDB.get(i).getConfigCode() + "i="
                            + i);
                    for (int j = 0; j < i; j++) {
                        if (menuDB.get(j).getConfigCode().equals(MENU_INVOICE))
                            continue;
                        else {
                            if (menuDB.get(j).getMandatory() == 0
                                    && !menuDB.get(j).isDone()
                                    && menuDB.get(j).getHasLink() == 1) {

                                return false;
                            }
                        }
                    }
                    break;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    /**
     * Method to use updated selected seller type either vansales or presales
     *
     * @param flag - 1 vansales,0 - presales
     */
    private void updateRetailerwiseSellertype(int flag) {
        try {

            DBUtil db = new DBUtil(HomeScreenTwo.this, DataMembers.DB_NAME
            );
            db.openDataBase();
            String query = "update retailermasterinfo set is_vansales=" + flag
                    + " where retailerid="
                    + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID());
            db.updateSQL(query);
            db.close();

        } catch (Exception e) {
            Commons.printException(e);
        }

    }


    /**
     * Method to use if scheme module enable ,load ordered scheme details or if
     * seller type dialog configuration on and seller type selected
     * vansales,load ordered scheme details
     * vansales,load ordered scheme details
     */
    private void enableSchemeModule() {
        if (schemeHelper.IS_SCHEME_ON
                && schemeHelper.IS_SCHEME_SHOW_SCREEN) {
            SchemeDetailsMasterHelper.getInstance(getApplicationContext()).prepareNecessaryLists(bmodel.productHelper.getProductMaster());
            schemeHelper.loadSchemeDetails(getApplicationContext(), bmodel
                    .getRetailerMasterBO().getRetailerID());
        }
    }

    public void dataNotMapped() {
        bmodel.showAlert(
                getResources().getString(R.string.data_not_mapped),
                0);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed here it is 2
        if (requestCode == 997)
            if (resultCode == RESULT_OK)
                mSchedule.notifyDataSetChanged();
    }

    private boolean isSTK_ORD_DONE() {
        for (int i = 0; i < menuDB.size(); i++) {
            Commons.print("menuDB.get(i).getConfigCode()" + menuDB.get(i).getConfigCode());
            if (menuDB.get(i).getConfigCode().equals("MENU_STK_ORD") && menuDB.get(i).isDone()
                    && menuDB.get(i).getHasLink() == 1) {

                return true;
            }

        }
        return false;
    }

    private ViewGroup.LayoutParams getListLayoutParam(View v) {

        ViewGroup.LayoutParams lp = v.getLayoutParams();
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        int listHeightInDp = (int) (getResources().getDimension(R.dimen.sub_activity_list_height) / displayMetrics.density);
        int listHeightInPx = (int) ((listHeightInDp * displayMetrics.density) + 0.5);
        lp.height = mInStoreMenu.size() * listHeightInPx;

        return lp;
    }

    @Override
    public void updateSupplierName(String supplierName) {
        retailerCodeTxt.setText(supplierName);
    }


    public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.MyViewHolder> {

        private Vector<ConfigureBO> mActivityList;
        private ConfigureBO configTemp;
        private View itemView;

        public ActivityAdapter(Vector<ConfigureBO> mActivityList) {
            this.mActivityList = mActivityList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.homescreentwo_listitem, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            configTemp = mActivityList.get(position);

            holder.config = configTemp;

            if (configTemp.getMandatory() == 1) {
                String menuName = configTemp.getMenuName();
                String mandatory = " *";
                SpannableStringBuilder builder = new SpannableStringBuilder();

                builder.append(menuName);
                int start = builder.length();
                builder.append(mandatory);
                int middle = builder.length();

                builder.setSpan(new ForegroundColorSpan(Color.BLACK), start, middle
                        ,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.activityname.setText(builder.toString());
            } else {
                holder.activityname.setText(configTemp.getMenuName());
            }

            holder.menuCode = configTemp.getConfigCode();
            holder.hasLink = configTemp.getHasLink();

            Integer i = menuIcons.get(configTemp.getConfigCode());
            if (i != null)
                holder.iconIV.setImageResource(i);
            else
                holder.iconIV.setImageResource(menuIcons.get(MENU_ORDER));

            if (holder.config.isDone()) {
                holder.icon_ll.setBackgroundResource(R.drawable.activity_icon_bg_module_completed);
                holder.iconIV.setColorFilter(Color.WHITE);

            } else {
                holder.icon_ll.setBackgroundResource(R.drawable.activity_icon_bg_normal);
                holder.iconIV.setColorFilter(Color.argb(0, 0, 0, 0));
            }

            itemView.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    /*if (!isCreated) {
                        isCreated = true;*/
                    if (holder.config.getConfigCode().equals(MENU_STORECHECK)) {
                        isCreated = false;


                        if (holder.childListView.getVisibility() == View.GONE) {
                            holder.img_arrow.setImageResource(R.drawable.activity_icon_close);
                            isInstoreMenuVisible = true;

                            holder.childListView.setLayoutParams(getListLayoutParam(holder.childListView));
                            holder.childListView.setAdapter(mSchedule);
                            holder.childListView.setVisibility(View.VISIBLE);
                        } else {
                            holder.img_arrow.setImageResource(R.drawable.activity_icon_next);
                            isInstoreMenuVisible = false;

                            holder.childListView.setVisibility(View.GONE);
                        }
                    } else {
                        gotoNextActivity(holder.config, holder.hasLink, false);
                    }
                    // }
                }
            });

            if ((isInstoreMenuVisible || getIntent().getBooleanExtra("isStoreMenu", false))
                    && holder.config.getConfigCode().equals(MENU_STORECHECK)) {
                holder.img_arrow.setImageResource(R.drawable.activity_icon_close);
                holder.childListView.setLayoutParams(getListLayoutParam(holder.childListView));
                holder.childListView.setAdapter(mSchedule);
                holder.childListView.setVisibility(View.VISIBLE);
            } else {
                holder.img_arrow.setImageResource(R.drawable.activity_icon_next);
                holder.childListView.setVisibility(View.GONE);
            }

        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return mActivityList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView activityname;
            public ImageView iconIV, img_arrow;
            public String menuCode;
            public int hasLink;
            public ConfigureBO config;
            public ListView childListView;
            public LinearLayout icon_ll;

            public MyViewHolder(View view) {
                super(view);
                iconIV = view.findViewById(R.id.list_item_icon_iv);
                icon_ll = view.findViewById(R.id.icon_ll);
                img_arrow = view.findViewById(R.id.img_arrow);
                activityname = view.findViewById(R.id.activityName);
                childListView = view.findViewById(R.id.childList);
            }
        }
    }

    class IconicAdapter extends ArrayAdapter<ConfigureBO> {

        Vector<ConfigureBO> items;

        private IconicAdapter(Vector<ConfigureBO> menuDB) {
            super(HomeScreenTwo.this, R.layout.homescreentwo_child_list, menuDB);
            this.items = menuDB;
        }

        public ConfigureBO getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            ConfigureBO configTemp = items.get(position);

            if (convertView == null) {
                LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(R.layout.homescreentwo_child_list,
                        parent, false);
                holder = new ViewHolder();

                holder.activity_icon_circle = convertView
                        .findViewById(R.id.circle);

                holder.activityname = convertView
                        .findViewById(R.id.activityName);

                convertView.setOnClickListener(new OnSingleClickListener() {

                    @Override
                    public void onSingleClick(View v) {
                        /*if (!isCreated) {
                            isCreated = true;*/
                        gotoNextActivity(holder.config, holder.hasLink, true);
                        //  }
                    }
                });

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.position = position;
            holder.config = configTemp;
            holder.menuCode = configTemp.getConfigCode();
            holder.hasLink = configTemp.getHasLink();
            if (configTemp.getMandatory() == 1) {
                String childMenuName = configTemp.getMenuName();
                String mandatory = " *";
                SpannableStringBuilder builder = new SpannableStringBuilder();

                builder.append(childMenuName);
                int start = builder.length();
                builder.append(mandatory);
                int middle = builder.length();

                builder.setSpan(new ForegroundColorSpan(Color.argb(1, 131, 195, 65)), start, middle
                        ,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.activityname.setText(builder.toString());
            } else {
                holder.activityname.setText(configTemp.getMenuName());
            }

            if (holder.config.isDone()) {
                holder.activity_icon_circle.setColorFilter(ContextCompat.getColor(HomeScreenTwo.this, R.color.green_productivity));
            } else {
                holder.activity_icon_circle.setColorFilter(ContextCompat.getColor(getBaseContext(), R.color.black_bg1));
            }

            return convertView;
        }

        class ViewHolder {
            ConfigureBO config;
            String menuCode;
            int position;
            TextView activityname;
            int hasLink;
            ImageView activity_icon_circle;
        }

    }

    /*
     * Show Location wise Filter
     */
    private void showIndicativeOrderFilterAlert(final String menuCode) {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle(null);
        builder.setSingleChoiceItems(indicativeOrderAdapter, selecteditem,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        Integer selectedId = indicativeOrderAdapter
                                .getItem(item);
                        selecteditem = item;

                        if (!bmodel.productHelper.isAlreadyIndicativeOrderTaken(selectedId)
                                && !bmodel.isEdit()) {
                            bmodel.productHelper.downloadIndicativeOrder(selectedId);
                            bmodel.productHelper.updateIndicateOrder();
                        }
                        //      setImagefromCamera(mProductID, mTypeID);
                        OrderHelper.getInstance(HomeScreenTwo.this).isQuickCall = false;
                        if (menuCode.equals(ConfigurationMasterHelper.MENU_ORDER)) {
                            Intent i = new Intent(HomeScreenTwo.this,
                                    StockAndOrder.class);
                            i.putExtra("OrderFlag", "Nothing");
                            i.putExtra("ScreenCode",
                                    ConfigurationMasterHelper.MENU_ORDER);
                            startActivity(i);
                            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                            finish();
                        } else {

                            Intent intent = new Intent(HomeScreenTwo.this,
                                    StockAndOrder.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                            finish();
                        }
                        dialog.dismiss();


                    }
                });

        bmodel.applyAlertDialogTheme(builder);
    }

    private boolean isStoreCheckMoudleCompleted() {
        boolean isDoneMenus = false;
        for (ConfigureBO menuBo : mInStoreMenu) {
            if (menuBo.isDone()) {
                isDoneMenus = true;
                break;
            }
        }
        return isDoneMenus;
    }

    private void setRetailerProfileImage() {
        Vector<ConfigureBO> profileConfig = bmodel.configurationMasterHelper.getProfileModuleConfig();
        int size = profileConfig.size();
        boolean hasImageConfig = false;
        for (int i = 0; i < size; i++) {
            int flag = profileConfig.get(i).isFlag();
            String configCode = profileConfig.get(i).getConfigCode();
            if (configCode.equals("PROFILE60") && flag == 1) {

                hasImageConfig = true;

                if (bmodel.profilehelper.hasProfileImagePath(bmodel.retailerMasterBO) &&
                        bmodel.retailerMasterBO.getProfileImagePath() != null && !"".equals(bmodel.retailerMasterBO.getProfileImagePath())) {
                    retProfileImage.setVisibility(View.VISIBLE);
                    String[] imgPaths = bmodel.retailerMasterBO.getProfileImagePath().split("/");
                    String path = imgPaths[imgPaths.length - 1];
                    String filePath = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/"
                            + DataMembers.photoFolderName + "/" + path;
                    if (bmodel.profilehelper.isImagePresent(filePath)) {
                        setImageFromCamera(bmodel.retailerMasterBO);
                    } else {
                        retProfileImage.setImageResource(R.drawable.face);
                    }
                } else if (bmodel.retailerMasterBO.getProfileImagePath() != null &&
                        !"".equals(bmodel.retailerMasterBO.getProfileImagePath())) {
                    retProfileImage.setVisibility(View.VISIBLE);
                    String[] imgPaths = bmodel.retailerMasterBO.getProfileImagePath().split("/");
                    String path = imgPaths[imgPaths.length - 1];
                    File imgFile = new File(getExternalFilesDir(
                            Environment.DIRECTORY_DOWNLOADS)
                            + "/"
                            + bmodel.userMasterHelper.getUserMasterBO()
                            .getUserid()
                            + DataMembers.DIGITAL_CONTENT
                            + "/"
                            + DataMembers.PROFILE + "/"
                            + path);
                    if (imgFile.exists()) {
                        setImageFromServer();
                    } else {
                        retProfileImage.setImageResource(R.drawable.face);
                    }
                } else {
                    retProfileImage.setVisibility(View.GONE);
                    MyAppbar.setExpanded(false);
                }
                break;
            }
        }

        if (!hasImageConfig)
            MyAppbar.setExpanded(false);
    }

    private void setImageFromCamera(RetailerMasterBO retailerObj) {
        try {
            String[] imgPaths = retailerObj.getProfileImagePath().split("/");
            String path = imgPaths[imgPaths.length - 1];
            Uri uri = bmodel.profilehelper.getUriFromFile(FileUtils.photoFolderPath + "/" + path);
            retProfileImage.invalidate();
            retProfileImage.setImageURI(uri);
        } catch (Exception e) {
            retProfileImage.setImageResource(R.drawable.face);
            Commons.printException("" + e);
        }
    }

    private void setImageFromServer() {
        String[] imgPaths = bmodel.retailerMasterBO.getProfileImagePath().split("/");
        String path = imgPaths[imgPaths.length - 1];
        File imgFile = new File(getExternalFilesDir(
                Environment.DIRECTORY_DOWNLOADS)
                + "/"
                + bmodel.userMasterHelper.getUserMasterBO()
                .getUserid()
                + DataMembers.DIGITAL_CONTENT
                + "/"
                + DataMembers.PROFILE + "/"
                + path);
        Bitmap myBitmap = FileUtils.decodeFile(imgFile);
        retProfileImage.setImageBitmap(myBitmap);
    }

    /*
     * Open the Image in Photo Gallery while onClick
     */
    private void openImage(String fileName) {
        if (fileName.trim().length() > 0) {
            try {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file://" + fileName),
                        "image/*");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Commons.printException("" + e);
                Toast.makeText(
                        this,
                        getResources()
                                .getString(
                                        R.string.no_application_available_to_view_video),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this,
                    getResources().getString(R.string.unloadimage),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void showLocation() {
        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(this);
        builder.setTitle(null);
        builder.setCancelable(false);
        builder.setSingleChoiceItems(mLocationAdapter, mSelectedLocationIndex,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        mSelectedLocationIndex = item;
                        bmodel.productHelper.setmSelectedGLobalLocationIndex(item);
                        dialog.dismiss();
                        isClick = false;
                        showCategory();
                    }
                });

        bmodel.applyAlertDialogTheme(builder);
    }

    private void showCategory() {
        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(this);
        builder.setTitle(null);
        builder.setCancelable(false);
        builder.setSingleChoiceItems(mCategoryAdapter, mSelectedCategoryIndex,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        mSelectedCategoryIndex = item;
                        bmodel.productHelper.setmSelectedGLobalLevelID(bmodel.productHelper.getGlobalCategory().get(item).getParentID());
                        bmodel.productHelper.setmSelectedGlobalProductId(bmodel.productHelper.getGlobalCategory().get(item).getProductID());
                        dialog.dismiss();
                        isClick = false;
                    }
                });

        bmodel.applyAlertDialogTheme(builder);
    }

    //used for filter method loading non products content module
    private void chooseFilterType(String menuCode) {
      /*  bmodel.productHelper
                .downloadFiveLevelFilterNonProducts(menuCode);*/
        bmodel.productHelper.setFilterProductLevelsRex(bmodel.productHelper.downloadFilterLevel(menuCode));
        bmodel.productHelper.setFilterProductsByLevelIdRex(bmodel.productHelper.downloadFilterLevelProducts(
                bmodel.productHelper.getRetailerModuleSequenceValues(), false));
    }


    /**
     * Load Sales Download method in Async Task
     */
    class DownloadSalesReturnProducts extends AsyncTask<Integer, Integer, Boolean> {
        private AlertDialog.Builder builder;
        private AlertDialog salesAlertDialog;
        private SalesReturnHelper salesReturnHelper;
        private String menCode;
        private String menuName;

        public DownloadSalesReturnProducts(SalesReturnHelper salesReturnHelper, String configCode, String menuName) {
            this.salesReturnHelper = salesReturnHelper;
            this.menCode = configCode;
            this.menuName = menuName;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            builder = new AlertDialog.Builder(HomeScreenTwo.this);

            customProgressDialog(builder, getResources().getString(R.string.loading));
            salesAlertDialog = builder.create();
            salesAlertDialog.show();
        }

        @Override
        protected Boolean doInBackground(Integer... integers) {
            try {

                if (salesReturnHelper.IS_PRD_CNT_DIFF_SR)
                    salesReturnHelper.downloadSalesReturnSKUs(HomeScreenTwo.this);

                else
                    salesReturnHelper.downloadSalesReturnProducts(HomeScreenTwo.this);


                salesReturnHelper.cloneReasonMaster(false);

                Commons.print("Sales Return Prod Size<><><><<>" + salesReturnHelper.getSalesReturnProducts().size());

                salesReturnHelper.getInstance(HomeScreenTwo.this).clearSalesReturnTable(false);


                if (!bmodel.configurationMasterHelper.IS_INVOICE) {
                    salesReturnHelper.getInstance(HomeScreenTwo.this).removeSalesReturnTable(false);
                    salesReturnHelper.getInstance(HomeScreenTwo.this).loadSalesReturnData(getApplicationContext(), "", "", false);
                }

                bmodel.updateProductUOM(StandardListMasterConstants.mActivityCodeByMenuCode.get(MENU_SALES_RET), 1);

                bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                        DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                        DateTimeUtils.now(DateTimeUtils.TIME), menCode);
                return Boolean.TRUE;
            } catch (Exception e) {
                Commons.printException("" + e);
            }
            return Boolean.FALSE;
        }

        @Override
        protected void onPostExecute(Boolean isFlag) {
            super.onPostExecute(isFlag);
            salesAlertDialog.dismiss();
            if (isFlag) {
                Intent intent = new Intent(HomeScreenTwo.this,
                        SalesReturnActivity.class);
                intent.putExtra("CurrentActivityCode", menCode);
                intent.putExtra("screentitle", menuName);
                startActivity(intent);
                finish();
            } else {
                isCreated = false;

                isClick = false;
                Toast.makeText(
                        HomeScreenTwo.this,
                        getResources()
                                .getString(
                                        R.string.unable_to_load_data),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadRequiredMethodsforOrder(String configCode) {
        bmodel.productHelper.downloadIndicativeOrderList();
        for (Integer temp : bmodel.productHelper
                .getIndicativeList())
            indicativeOrderAdapter.add(temp);

        DiscountHelper discountHelper = DiscountHelper.getInstance(this);
        if (bmodel.configurationMasterHelper.SHOW_STORE_WISE_DISCOUNT_DLG) {

            discountHelper.downloadBillWiseDiscount(this);
            discountHelper.loadExistingBillWiseRangeDiscount(this);
        }
        // apply bill wise pay term discount
        discountHelper.downloadBillWisePayTermDiscount(this);

        if (bmodel.configurationMasterHelper.SHOW_INVOICE_CREDIT_BALANCE &&
                "CREDIT".equals(bmodel.getRetailerMasterBO().getRpTypeCode())) {
            if (bmodel.getRetailerMasterBO()
                    .getCredit_balance() == -1
                    || bmodel.getRetailerMasterBO()
                    .getCredit_balance() > 0) {

                if (bmodel.productHelper.getIndicativeList() != null) {
                    if (bmodel.productHelper.getIndicativeList().size() > 1) {
                        showIndicativeOrderFilterAlert(configCode);
                        return;
                    }
                    if (bmodel.productHelper.getIndicativeList().size() > 0) {
                        if (!bmodel.productHelper.isAlreadyIndicativeOrderTaken(bmodel.productHelper.getIndicativeList().get(0))
                                && !bmodel.isEdit()) {
                            bmodel.productHelper.downloadIndicativeOrder(bmodel.productHelper.getIndicativeList().get(0));
                            bmodel.productHelper.updateIndicateOrder();
                        }
                    }
                }
                bmodel.outletTimeStampHelper
                        .saveTimeStampModuleWise(
                                DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                                DateTimeUtils.now(DateTimeUtils.TIME),
                                configCode);
                OrderHelper.getInstance(this).isQuickCall = false;
                Intent i = new Intent(HomeScreenTwo.this,
                        StockAndOrder.class);
                i.putExtra("OrderFlag", "Nothing");
                i.putExtra("ScreenCode",
                        ConfigurationMasterHelper.MENU_ORDER);
                startActivity(i);
                overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                finish();
            } else {
                showDialog(INVOICE_CREDIT_BALANCE);
            }
        } else {

            if (bmodel.productHelper.getIndicativeList() != null) {
                if (bmodel.productHelper.getIndicativeList().size() > 1) {
                    showIndicativeOrderFilterAlert(configCode);
                    return;
                }
                if (bmodel.productHelper.getIndicativeList().size() > 0) {
                    if (!bmodel.productHelper.isAlreadyIndicativeOrderTaken(bmodel.productHelper.getIndicativeList().get(0))
                            && !bmodel.isEdit()) {
                        bmodel.productHelper.downloadIndicativeOrder(bmodel.productHelper.getIndicativeList().get(0));
                        bmodel.productHelper.updateIndicateOrder();
                    }
                }
            }
            bmodel.outletTimeStampHelper
                    .saveTimeStampModuleWise(
                            DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                            DateTimeUtils.now(DateTimeUtils.TIME),
                            configCode);
            OrderHelper.getInstance(this).isQuickCall = false;
            Intent i = new Intent(HomeScreenTwo.this,
                    StockAndOrder.class);
            i.putExtra("OrderFlag", "Nothing");
            i.putExtra("ScreenCode",
                    ConfigurationMasterHelper.MENU_ORDER);
            startActivity(i);
            finish();
        }
    }


    private void resetRemarksBO() {
        bmodel.setOrderHeaderNote("");
        bmodel.setRField1("");
        bmodel.setRField2("");
        bmodel.setSaleReturnNote("");
        bmodel.setSaleReturnRfValue("");
        bmodel.setStockCheckRemark("");
        bmodel.setAssetRemark("");
    }

    class StockAndOrderTask extends AsyncTask<Void, Void, Integer> {
        private ConfigureBO menu;
        private Context mContext;
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        public StockAndOrderTask(ConfigureBO configureBO, Context context) {
            this.menu = configureBO;
            this.mContext = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            builder = new AlertDialog.Builder(HomeScreenTwo.this);
            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            Integer num = moveToStockAndOrder(menu);
            return num;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            alertDialog.dismiss();


            if (integer != null && integer == 0) {
                Toast.makeText(
                        mContext,
                        getResources().getString(
                                R.string.please_complete_previous_activity),
                        Toast.LENGTH_SHORT).show();
            } else if (integer != null && integer == 1) {
                Toast.makeText(
                        mContext,
                        getResources().getString(
                                R.string.please_pay_old_invoice),
                        Toast.LENGTH_SHORT).show();
            } else if (integer != null && integer == 2) {
                loadstockorderscreen(menu.getConfigCode());
            } else if (integer != null && integer == 3) {
                loadOrderSummaryScreen(menu.getConfigCode());
            } else if (integer != null && integer == 4) {
                dataNotMapped();
            } else if (integer != null && integer == 5) {
                final String menuConfigCode = menu.getConfigCode();
                final String menuName = menu.getMenuName();
                OrderTransactionListDialog obj = new OrderTransactionListDialog(getApplicationContext(), HomeScreenTwo.this, new OrderTransactionListDialog.newOrderOnClickListener() {
                    @Override
                    public void onNewOrderButtonClick() {
                        //the methods that were called during normal stock and order loading in non edit mode are called here
                        //loadOrderedProducts,loadSerialNo,enableSchemeModule are used in edit mode so avoided here as in this case screen should be loaded fresh
                        bmodel.setOrderHeaderBO(null);
                        resetRemarksBO();
                        bmodel.setEdit(false);
                        loadRequiredMethodsForStockAndOrder(menuConfigCode, menuName);
                        loadstockorderscreen(menuConfigCode);
                    }
                }, new OrderTransactionListDialog.oldOrderOnClickListener() {
                    @Override
                    public void onOldOrderButtonClick(String id) {
                        OrderHelper.getInstance(HomeScreenTwo.this).selectedOrderId = id;
                        //the methods that were called during normal stock and order loading in edit mode are called here
                        //selectedOrderId is passed to loadOrderedProducts method  to load ordered products for that id
                        //loadSerialNo,enableSchemeModule included as these were called in edit mode
                        OrderHelper.getInstance(HomeScreenTwo.this).loadOrderedProducts(HomeScreenTwo.this, bmodel.getRetailerMasterBO()
                                .getRetailerID(), id);
                        OrderHelper.getInstance(HomeScreenTwo.this).loadSerialNo(HomeScreenTwo.this);
                        enableSchemeModule();
                        loadRequiredMethodsForStockAndOrder(menuConfigCode, menuName);
                        loadOrderSummaryScreen(menuConfigCode);

                    }
                }, false, new OrderTransactionListDialog.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        isCreated = false;
                        isClick = false;
                    }
                });
                obj.show();
                obj.setCancelable(false);
            }
        }
    }


    public Integer moveToStockAndOrder(ConfigureBO menu) {

        if (isPreviousDone(menu)
                || bmodel.configurationMasterHelper.IS_JUMP) {

            OrderHelper orderHelper = OrderHelper.getInstance(this);

            if (bmodel.configurationMasterHelper.IS_ORDER_FROM_EXCESS_STOCK) {
                bmodel.productHelper.clearOrderTable();
                OrderDeliveryHelper orderDeliveryHelper = OrderDeliveryHelper.getInstance(this);
                orderDeliveryHelper.updateProductWithExcessStock(this);

                if (bmodel.productHelper.getProductDiscountListByDiscountID() != null)
                    bmodel.productHelper.getProductDiscountListByDiscountID().clear();
            }

            // Tin Expiry validation
            if (bmodel.configurationMasterHelper.IS_RESTRICT_ORDER_TAKING
                    && (bmodel.getRetailerMasterBO().getRField4().equals("1")
                    || (bmodel.getRetailerMasterBO().getTinExpDate() != null
                    && !bmodel.getRetailerMasterBO().getTinExpDate().isEmpty() &&
                    DateTimeUtils.compareDate(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                            bmodel.getRetailerMasterBO().getTinExpDate(), "yyyy/MM/dd") > 0))) {
                bmodel.showAlert(getResources().getString(R.string.order_not_allowed_for_retailer), 0);
                isCreated = false;
                return null;
            }

            if (bmodel.configurationMasterHelper
                    .downloadFloatingSurveyConfig(MENU_STK_ORD)) {
                SurveyHelperNew surveyHelperNew = SurveyHelperNew.getInstance(this);
                surveyHelperNew.setFromHomeScreen(false);
                surveyHelperNew.downloadModuleId("STANDARD");
                surveyHelperNew.downloadQuestionDetails(MENU_STK_ORD);
                surveyHelperNew.loadSurveyAnswers(0);
            }


            if (bmodel.configurationMasterHelper.IS_SUPPLIER_CREDIT_LIMIT
                    && !bmodel.configurationMasterHelper.IS_SUPPLIER_NOT_AVAILABLE
                    && bmodel.getRetailerMasterBO().getSupplierBO() != null &&
                    bmodel.getRetailerMasterBO().getSupplierBO().getCreditLimit() > 0) {
                bmodel.getRetailerMasterBO().setCreditLimit(bmodel.getRetailerMasterBO().getSupplierBO().getCreditLimit());
            }

            if (bmodel.configurationMasterHelper.SHOW_SALES_RETURN_IN_ORDER
                    || bmodel.configurationMasterHelper.SHOW_SALES_RETURN_TV_IN_ORDER) {
                SalesReturnHelper salesReturnHelper = SalesReturnHelper.getInstance(this);
                salesReturnHelper.loadSalesReturnConfigurations(getApplicationContext());
                bmodel.reasonHelper.downloadSalesReturnReason();
                if (bmodel.reasonHelper.getReasonSalesReturnMaster().size() > 0) {
                    salesReturnHelper.cloneReasonMaster(true);//
                    salesReturnHelper.clearSalesReturnTable(true);//
                    salesReturnHelper.removeSalesReturnTable(true);

                    if (bmodel.configurationMasterHelper.SHOW_SALES_RETURN_TV_IN_ORDER) {
                        salesReturnHelper.getInstance(HomeScreenTwo.this).loadSalesReturnData(getApplicationContext(), "", "", bmodel.configurationMasterHelper.SHOW_SALES_RETURN_TV_IN_ORDER);
                    }
                }
            }
            if (!isClick) {
                isClick = true;
                if (bmodel.productHelper.getProductMaster().size() > 0) {
                    bmodel.configurationMasterHelper.downloadFloatingNPReasonWithPhoto(MENU_STK_ORD);
                    if ((!bmodel.configurationMasterHelper.IS_VALIDATE_DUE_DAYS || bmodel.productHelper.isDueDateExpired()) && (!bmodel.configurationMasterHelper.IS_VALIDATE_CREDIT_DAYS
                            || bmodel.getRetailerMasterBO().getCreditDays() == 0
                            || bmodel.productHelper.isCheckCreditPeriod())) {

                        /** Load the stock check if opened in edit mode. **/
                        bmodel.setEditStockCheck(false);
                        if (bmodel.hasAlreadyStockChecked(bmodel
                                .getRetailerMasterBO().getRetailerID())) {

                            bmodel.setEditStockCheck(true);
                            bmodel.loadStockCheckedProducts(bmodel
                                    .getRetailerMasterBO().getRetailerID(), menu.getConfigCode());


                            if (bmodel.configurationMasterHelper.IS_COMBINED_STOCK_CHECK_FROM_ORDER) {
                                if (bmodel.configurationMasterHelper.SHOW_NEAREXPIRY_IN_STOCKCHECK
                                        && bmodel.configurationMasterHelper.IS_RETAIN_NEAREXPIRY_CURRENT_TRAN_IN_STOCKCHECK) {
                                    NearExpiryTrackingHelper mNearExpiryHelper = NearExpiryTrackingHelper.getInstance(this);
                                    mNearExpiryHelper.loadSKUTracking(getApplicationContext(), false);
                                }

                                if (bmodel.configurationMasterHelper.SHOW_PRICECHECK_IN_STOCKCHECK) {
                                    PriceTrackingHelper priceTrackingHelper = PriceTrackingHelper.getInstance(this);
                                    priceTrackingHelper.loadPriceTransaction(getApplicationContext());
                                    if (bmodel.configurationMasterHelper.IS_PRICE_CHECK_RETAIN_LAST_VISIT_IN_EDIT_MODE && !priceTrackingHelper.isPriceCheckDone(getApplicationContext())) {
                                        priceTrackingHelper.updateLastVisitPriceAndMRP();
                                    }
                                }
                            } else if (bmodel.configurationMasterHelper.IS_LOAD_STK_CHECK_LAST_VISIT) {
                                clearStockCheck();
                                bmodel.loadLastVisitStockCheckedProducts(bmodel
                                        .getRetailerMasterBO().getRetailerID(), menu.getConfigCode());
                            }
                        }
                        bmodel.productHelper.setProductImageUrl();
                        bmodel.setEdit(false);
                        if (orderHelper.hasAlreadyOrdered(this, bmodel.getRetailerMasterBO()
                                .getRetailerID())) {
                            bmodel.setEdit(true);
                        }


                        if (bmodel.configurationMasterHelper.IS_SHOW_ORDERING_SEQUENCE) {
                            bmodel.productHelper.getmProductidOrderByEntry().clear();
                            bmodel.productHelper.getmProductidOrderByEntryMap().clear();
                        }

                        bmodel.productHelper.downloadIndicativeOrderList();//moved here to check size of indicative order
                        orderHelper.selectedOrderId = "";
                        if (bmodel.productHelper.getIndicativeList() != null
                                && bmodel.productHelper.getIndicativeList().size() < 1
                                && bmodel.configurationMasterHelper.IS_MULTI_STOCKORDER) {
                            if (bmodel.isEdit()) {
                                orderHelper.selectedOrderId = "";//cleared to avoid reuse of id
                                return 5;
                            } else {
                                //the methods that were called during normal stock and order loading in non edit mode are called here
                                //loadOrderedProducts,loadSerialNo,enableSchemeModule are used in edit mode so avoided here as in this case screen should be loaded fresh
                                bmodel.setOrderHeaderBO(null);
                                loadRequiredMethodsForStockAndOrder(menu.getConfigCode(), menu.getMenuName());
                                // loadstockorderscreen(menu.getConfigCode());
                                return 2;
                            }
                        } else {
                            if (bmodel.isEdit()) {//doubt
                                orderHelper.loadOrderedProducts(this, bmodel.getRetailerMasterBO()
                                        .getRetailerID(), null);
                                OrderHelper.getInstance(this).selectedOrderId = orderHelper.getOrderId();
                                orderHelper.loadSerialNo(this);
                                enableSchemeModule();
                            } else {
                                bmodel.setOrderHeaderBO(null);
                                if (bmodel.configurationMasterHelper.IS_TEMP_ORDER_SAVE && menu.getConfigCode().equals(MENU_CATALOG_ORDER)) {
                                    bmodel.loadTempOrderDetails();
                                }
                            }
                            loadRequiredMethodsForStockAndOrder(menu.getConfigCode(), menu.getMenuName());
                            if (bmodel.isEdit()) {
                                // loadOrderSummaryScreen(menu.getConfigCode());
                                return 3;

                            } else {
                                // loadstockorderscreen(menu.getConfigCode());
                                return 2;
                            }
                        }
                    } else {
                        /*Toast.makeText(
                                this,
                                getResources().getString(
                                        R.string.please_pay_old_invoice),
                                Toast.LENGTH_SHORT).show();*/
                        isCreated = false;
                        isClick = false;
                        return 1;
                    }

                } else {

                    isCreated = false;
                    isClick = false;
                    menuCode = (menuCodeList.get(menu.getConfigCode()) == null ? "" : menuCodeList.get(menu.getConfigCode()));
                    if (!menuCode.equals(menu.getConfigCode()))
                        menuCodeList.put(menu.getConfigCode(), menu.getConfigCode());

                    return 4;
                }
            }

        } else {
           /* Toast.makeText(
                    this,
                    getResources().getString(
                            R.string.please_complete_previous_activity),
                    Toast.LENGTH_SHORT).show();*/
            isCreated = false;

            return 0;
        }

        return null;
    }

    //Clear ProductMasterBO to load the data in lastvisitstockcheck
    private void clearStockCheck() {
        int siz = bmodel.productHelper.getProductMaster().size();
        if (siz == 0)
            return;
        for (int i = 0; i < siz; ++i) {
            ProductMasterBO product = bmodel.productHelper.getProductMaster().get(i);
            int siz1 = product.getLocations().size();
            for (int j = 0; j < siz1; j++) {
                product.getLocations().get(j).setShelfPiece(-1);
                product.getLocations().get(j).setShelfCase(-1);
                product.getLocations().get(j).setShelfOuter(-1);
            }
        }
    }

    private void doCloseCall() {
        bmodel.updateIsVisitedFlag("Y");

        // stop timer
        if (bmodel.timer != null) {
            bmodel.timer.stopTimer();
            bmodel.timer = null;
        }

        bmodel.outletTimeStampHelper.updateTimeStamp(DateTimeUtils
                .now(DateTimeUtils.TIME), "");

        if (!hasActivityDone() && !bmodel.configurationMasterHelper.SHOW_FEEDBACK_IN_CLOSE_CALL && !bmodel.configurationMasterHelper.SHOW_NO_ORDER_REASON) {
            bmodel.outletTimeStampHelper.deleteTimeStampAllModule();
            bmodel.outletTimeStampHelper.deleteTimeStamp();
            bmodel.outletTimeStampHelper.deleteTimeStampImages();
            bmodel.outletTimeStampHelper.deleteImagesFromFolder();
            bmodel.outletTimeStampHelper.deleteTimeStampRetailerDeviation();

        } else {
            bmodel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
                    .now(DateTimeUtils.TIME));
            bmodel.saveModuleCompletion("MENU_CALL_ANLYS", true);
        }
        resetRemarksBO();
        if (bmodel.configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED) {
            resetSellerConfiguration();
        }

        bmodel.productHelper.clearProductHelper();
    }

    private void resetSellerConfiguration() {
        bmodel.configurationMasterHelper.IS_SIH_VALIDATION = bmodel.configurationMasterHelper.IS_SIH_VALIDATION_MASTER;
        bmodel.configurationMasterHelper.IS_STOCK_IN_HAND = bmodel.configurationMasterHelper.IS_STOCK_IN_HAND_MASTER;
        bmodel.configurationMasterHelper.IS_WSIH = bmodel.configurationMasterHelper.IS_WSIH_MASTER;
        SchemeDetailsMasterHelper.getInstance(this).IS_SCHEME_ON = SchemeDetailsMasterHelper.getInstance(this).IS_SCHEME_ON_MASTER;
        SchemeDetailsMasterHelper.getInstance(this).IS_SCHEME_SHOW_SCREEN = SchemeDetailsMasterHelper.getInstance(this).IS_SCHEME_SHOW_SCREEN_MASTER;
        bmodel.configurationMasterHelper.SHOW_TAX = bmodel.configurationMasterHelper.SHOW_TAX_MASTER;
        bmodel.configurationMasterHelper.IS_INVOICE = bmodel.configurationMasterHelper.IS_INVOICE_MASTER;
    }

    /**
     * Check whether any activity is done on this call or not.
     *
     * @return boolean
     */
    private boolean hasActivityDone() {
        try {
            if (isClosingStockDone()) {
                return true;
            } else {
                menuDB = bmodel.configurationMasterHelper.getActivityMenu();

                for (ConfigureBO config : menuDB) {
                    if (!config.getConfigCode().equals(MENU_CALL_ANLYS)
                            && !config.getConfigCode().equals(StandardListMasterConstants.MENU_COLLECTION_VIEW)
                            && !config.getConfigCode().equals(StandardListMasterConstants.MENU_REV)) {
                        if (config.getHasLink() == 1 && config.isDone()) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
        return false;
    }

    public boolean isClosingStockDone() {
        boolean flag = false;
        try {
            DBUtil db = new DBUtil(HomeScreenTwo.this, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db.selectSQL("select stockid from "
                    + DataMembers.tbl_closingstockheader + " where retailerid="
                    + StringUtils.getStringQueryParam(bmodel.getAppDataProvider().getRetailMaster().getRetailerID())
                    + " AND DistributorID=" + bmodel.getAppDataProvider().getRetailMaster().getDistributorId());
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
}
