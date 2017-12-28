package com.ivy.sd.png.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.asset.AssetTrackingActivity;
import com.ivy.cpg.view.asset.AssetTrackingHelper;
import com.ivy.cpg.view.asset.PosmTrackingActivity;
import com.ivy.cpg.view.competitor.CompetitorTrackingActivity;
import com.ivy.cpg.view.digitalcontent.DigitalContentActivity;
import com.ivy.cpg.view.digitalcontent.DigitalContentHelper;
import com.ivy.cpg.view.digitalcontent.StoreWiseGallery;
import com.ivy.cpg.view.nearexpiry.NearExpiryTrackingActivity;
import com.ivy.cpg.view.nearexpiry.NearExpiryTrackingHelper;
import com.ivy.cpg.view.photocapture.Gallery;
import com.ivy.cpg.view.photocapture.PhotoCaptureActivity;
import com.ivy.cpg.view.photocapture.PhotoCaptureHelper;
import com.ivy.cpg.view.planogram.PlanoGramActivity;
import com.ivy.cpg.view.planogram.PlanoGramHelper;
import com.ivy.cpg.view.price.PriceTrackActivity;
import com.ivy.cpg.view.price.PriceTrackCompActivity;
import com.ivy.cpg.view.price.PriceTrackingHelper;
import com.ivy.cpg.view.promotion.PromotionHelper;
import com.ivy.cpg.view.promotion.PromotionTrackingActivity;
import com.ivy.cpg.view.salesreturn.SalesReturnActivity;
import com.ivy.cpg.view.salesreturn.SalesReturnHelper;
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
import com.ivy.cpg.view.survey.SurveyActivityNew;
import com.ivy.cpg.view.survey.SurveyHelperNew;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.intermecprint.BtPrint4Ivy;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.bo.SupplierMasterBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.sd.png.view.merch.MerchandisingActivity;
import com.ivy.sd.png.view.profile.ProfileActivity;
import com.ivy.sd.print.PrintPreviewScreen;
import com.ivy.sd.print.PrintPreviewScreenDiageo;

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
    private static final String MENU_WITS = "MENU_WITS";
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
    private static final String MENU_TASK = "MENU_TASK";
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
    // Used to map icons
    private static final HashMap<String, Integer> menuIcons = new HashMap<String, Integer>();
    private static final String PRE_SALES = "PreSales";
    private static final String VAN_SALES = "VanSales";

    private ArrayAdapter<Integer> indicativeOrderAdapter;

    private BusinessModel bmodel;
    private String title;
    private Vector<ConfigureBO> menuDB = new Vector<>();
    private String[] mSalesTypeArray = {PRE_SALES, VAN_SALES};
    private String[] mOrderTypeArray;
    private ArrayList<StandardListBO> mOrderTypeList;
    private ArrayList<SupplierMasterBO> mSupplierList;
    private ArrayAdapter<SupplierMasterBO> mSupplierAdapter;
    private int mDefaultSupplierSelection = 0;
    private IconicAdapter mSchedule;
    private boolean isClick = false;
    private boolean isCreated;
    private TypedArray typearr;
    private int mOrderTypeCheckedItem = 0;
    private static final String ORDER_TYPE = "ORDER_TYPE";

    private Toolbar toolbar;
    private ActivityAdapter mActivityAdapter;
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

    private Vector<ConfigureBO> mTempMenuList = new Vector<>();
    private Vector<ConfigureBO> mTempMenuStoreList = new Vector<>();
    private Vector<ConfigureBO> menuWithSequence;
    private ImageView retProfileImage;
    private ArrayAdapter<StandardListBO> mLocationAdapter;
    private ArrayAdapter<LevelBO> mCategoryAdapter;
    private int mSelectedLocationIndex = 0;
    private int mSelectedCategoryIndex = 0;
    private boolean isStoreCheckMenu = false;
    private boolean isLocDialogShow = false;
    private HashMap<String, String> menuCodeList = new HashMap<>();
    String menuCode = "";

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


        typearr = getTheme().obtainStyledAttributes(R.styleable.MyTextView);

        activityView = (RecyclerView) findViewById(R.id.activity_list);
        activityView.setHasFixedSize(true);
        activityView.setNestedScrollingEnabled(false);
        isLocDialogShow = getIntent().getBooleanExtra("isLocDialog", false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        activityView.setLayoutManager(linearLayoutManager);

        try {
            int length = bmodel.retailerMasterBO.getRetailerName().indexOf("/");
            if (length == -1)
                length = bmodel.retailerMasterBO.getRetailerName().length();
            title = bmodel.retailerMasterBO.getRetailerName().substring(0,
                    length);
        } catch (Exception e) {
            Commons.printException(e);
        }
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        MyAppbar = (AppBarLayout) findViewById(R.id.MyAppbar);
        retailerNameTxt = (TextView) findViewById(R.id.retailer_name);
        retailerCodeTxt = (TextView) findViewById(R.id.retailer_code);
        iconLinearLayout = (LinearLayout) findViewById(R.id.img_layout);
        callAnalysisBtn = (Button) findViewById(R.id.btn_call_analysis);
        retProfileImage = (ImageView) findViewById(R.id.retProfileImage);
        retailer_name_header = (LinearLayout) findViewById(R.id.retailer_name_header);
        setSupportActionBar(toolbar);
        retailer_name_header.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent prof = new Intent(HomeScreenTwo.this, ProfileActivity.class);
                prof.putExtra("hometwo", true);
                startActivity(prof);
            }
        });
//        getSupportActionBar().setHomeButtonEnabled(true);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setRetailerProfileImage();
        retProfileImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
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
        retailerNameTxt.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        retailerCodeTxt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        callAnalysisBtn.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        retailerNameTxt.setText(title);
        if (bmodel.retailerMasterBO.getAddress3() != null && !bmodel.retailerMasterBO.getAddress3().isEmpty()) {
            retailerCodeTxt.setVisibility(View.VISIBLE);
            retailerCodeTxt.setText(bmodel.retailerMasterBO.getAddress3());
        } else {
            retailerCodeTxt.setVisibility(View.GONE);
        }

        collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapse_toolbar);


        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        collapsingToolbar.setTitleEnabled(false);

        collapsingToolbar.setCollapsedTitleTextColor(Color.WHITE);
        collapsingToolbar.setCollapsedTitleTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
//
//        collapsingToolbar.setExpandedTitleColor(Color.WHITE);
//        collapsingToolbar.setExpandedTitleTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));


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

        //  bmodel.configurationMasterHelper.downloadNewActivityMenu(ConfigurationMasterHelper.MENU_ACTIVITY);

        ((TextView) findViewById(R.id.label_activity_count)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

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

        mActivityDoneCount = (TextView) findViewById(R.id.activity_done_count);
        mActivityTotalCount = (TextView) findViewById(R.id.activity_total_count);

        mActivityDoneCount.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        mActivityTotalCount.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));

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

    public void CallAnalysisClick(View v) {

        for (ConfigureBO menu : menuDB) {
            if (menu.getConfigCode().equalsIgnoreCase(MENU_CLOSE_CALL)) {
                gotoNextActivity(menu, menu.getHasLink(), false);
                return;
            }
        }

        for (ConfigureBO menu : menuDB) {
            if (menu.getConfigCode().equalsIgnoreCase(MENU_CLOSE_KLGS)) {
                gotoNextActivity(menu, menu.getHasLink(), false);
            }
        }

        for (ConfigureBO menu : menuDB) {
            if (menu.getConfigCode().equalsIgnoreCase(MENU_CALL_ANLYS)) {
                gotoNextActivity(menu, menu.getHasLink(), false);
            }
        }
    }

    private void prepareMenuIcons() {
        menuIcons.put(MENU_STOCK, R.drawable.activity_icon_stock_check);
        menuIcons.put(MENU_COMBINED_STOCK, R.drawable.activity_icon_stock_check);
        menuIcons.put(MENU_ORDER, R.drawable.activity_icon_order_taking);
        menuIcons.put(MENU_STK_ORD, R.drawable.activity_icon_order_taking);
        menuIcons.put(MENU_NEAREXPIRY, R.drawable.icon_order);
        menuIcons.put(MENU_PLANOGRAM, R.drawable.icon_order);
        menuIcons.put(MENU_SKUWISERTGT, R.drawable.icon_order);
        menuIcons.put(MENU_COLLECTION, R.drawable.icon_collection);
        menuIcons.put(MENU_COLLECTION_REF, R.drawable.icon_collection);
        menuIcons.put(MENU_WITS, R.drawable.icon_sbd);
        menuIcons.put(MENU_DGT, R.drawable.icon_photo);
        menuIcons.put(MENU_CLOSING, R.drawable.icon_order);
        menuIcons.put(MENU_REV, R.drawable.icon_visit);
        menuIcons.put(MENU_INVOICE, R.drawable.icon_invoice);
        menuIcons.put(MENU_CALL_ANLYS, R.drawable.icon_call);
        menuIcons.put(MENU_ECALL, R.drawable.icon_call);
        menuIcons.put(MENU_RECORD, R.drawable.icon_reports);
        menuIcons.put(MENU_LCALL, R.drawable.icon_call);
        menuIcons.put(MENU_SALES_RET, R.drawable.icon_sales_return);
        menuIcons.put(MENU_PHOTO, R.drawable.icon_photo);
        menuIcons.put(MENU_TASK, R.drawable.icon_new_retailer);
        menuIcons.put(MENU_SURVEY, R.drawable.activity_icon_survey);
        menuIcons.put(MENU_SURVEY_QDVP3, R.drawable.activity_icon_survey);
        menuIcons.put(MENU_SURVEY01, R.drawable.activity_icon_survey);
        menuIcons.put(MENU_QUALITY, R.drawable.activity_icon_survey);
        menuIcons.put(MENU_PERSUATION, R.drawable.activity_icon_survey);
        menuIcons.put(MENU_ASSET, R.drawable.icon_survey);
        menuIcons.put(MENU_POSM, R.drawable.icon_survey);
        menuIcons.put(MENU_STORECHECK, R.drawable.icon_stock_check);
        menuIcons.put(MENU_PRESENTATION, R.drawable.activity_icon_presentation);
        menuIcons.put(MENU_PROMO,
                R.drawable.icon_promo_track);
        menuIcons.put(StandardListMasterConstants.MENU_COLLECTION_VIEW,
                R.drawable.icon_visit);
        menuIcons.put(StandardListMasterConstants.MENU_STOCK_REPLACEMENT,
                R.drawable.icon_sales_return);
        menuIcons.put(MENU_PRICE, R.drawable.icon_pricecheck);
        menuIcons.put(MENU_PRICE_COMP, R.drawable.icon_pricecheck);
        menuIcons.put(MENU_EMPTY_RETURN, R.drawable.icon_pricecheck);
        menuIcons.put(MENU_SOS, R.drawable.icon_sos);
        menuIcons.put(MENU_SOD, R.drawable.icon_sod);
        menuIcons.put(MENU_SOD_ASSET, R.drawable.icon_sod);
        menuIcons
                .put(MENU_SOSKU, R.drawable.icon_sosku);
        menuIcons.put(MENU_COMPETITOR,
                R.drawable.icon_competitor);
        menuIcons.put(MENU_CLOSE_CALL,
                R.drawable.icon_competitor);
        menuIcons.put(MENU_CLOSE_KLGS, R.drawable.icon_survey);
        menuIcons.put(MENU_DELIVERY, R.drawable.icon_survey);
        menuIcons.put(MENU_CONTRACT_VIEW, R.drawable.icon_call);
        menuIcons.put(MENU_LOYALTY_POINTS, R.drawable.icon_collection);

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

        if (bmodel.configurationMasterHelper.IS_SHOW_RID_CONCEDER_AS_DSTID) {
            String rSalesType = bmodel.getStandardListCode(bmodel.getRetailerMasterBO().getSalesTypeId());

            if (rSalesType.equalsIgnoreCase("INDIRECT")) {
                bmodel.retailerMasterBO.setDistributorId(Integer.parseInt(bmodel.retailerMasterBO.getRetailerID()));
                bmodel.retailerMasterBO.setDistParentId(0);
                if (bmodel.retailerMasterBO.getAddress3() != null)
                    retailerCodeTxt.setText(bmodel.retailerMasterBO.getAddress3());

            } else if (rSalesType.equalsIgnoreCase("DIRECT")) {

                if (!bmodel.configurationMasterHelper.IS_APPLY_DISTRIBUTOR_WISE_PRICE
                        && !bmodel.configurationMasterHelper.IS_DISTRIBUTOR_AVAILABLE) {
                    mSupplierList = bmodel.downloadSupplierDetails();
                    mSupplierAdapter = new ArrayAdapter<>(this,
                            R.layout.supplier_selection_list_adapter, mSupplierList);

                    updateDefaultSupplierSelection();
                }


            }

        } else {
            if (!bmodel.configurationMasterHelper.IS_APPLY_DISTRIBUTOR_WISE_PRICE
                    && !bmodel.configurationMasterHelper.IS_DISTRIBUTOR_AVAILABLE) {
                mSupplierList = bmodel.downloadSupplierDetails();
                mSupplierAdapter = new ArrayAdapter<>(this,
                        android.R.layout.select_dialog_singlechoice, mSupplierList);

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
        bmodel.schemeDetailsMasterHelper.getAppliedSchemeList().clear();

        if (bmodel.configurationMasterHelper.SHOW_ORDER_TYPE_DIALOG) {
            mOrderTypeList = bmodel.productHelper.getTypeList(ORDER_TYPE);
            if (mOrderTypeList != null && mOrderTypeList.size() > 0) {
                mOrderTypeArray = new String[mOrderTypeList.size()];
                if (bmodel.getRetailerMasterBO().getOrderTypeId() != null) {
                    for (int i = 0; i < mOrderTypeList.size(); i++) {
                        mOrderTypeArray[i] = mOrderTypeList.get(i).getListName();

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

        mTempMenuList = new Vector<>(menuDB);


        for (ConfigureBO menu : menuDB) {
            if (menu.getConfigCode().equalsIgnoreCase(MENU_STORECHECK))
                isStoreCheckMenu = true;
            if (menu.getConfigCode().equalsIgnoreCase(MENU_CLOSE_CALL)
                    || menu.getConfigCode().equalsIgnoreCase(MENU_CALL_ANLYS)) {
                mTempMenuList.remove(menu);
            }
        }
        mTempMenuStoreList = new Vector<>(mInStoreMenu);
        for (ConfigureBO storeMenu : mInStoreMenu) {
            if (storeMenu.getConfigCode().equalsIgnoreCase("MENU_CLOSE")) {
                mTempMenuStoreList.remove(storeMenu);
            }
        }

        mActivityAdapter = new ActivityAdapter(mTempMenuList);
        activityView.setAdapter(mActivityAdapter);

        int totalVisitCount = getMenuVisitCount(mTempMenuList) + getStoreMenuVisitCount(mTempMenuStoreList);

        mActivityDoneCount.setText(new DecimalFormat("0").format((isStoreCheckMenu ? (totalVisitCount != 0 ? (getStoreMenuVisitCount(mTempMenuStoreList) > 0 ? totalVisitCount - 1 : totalVisitCount) : 0) : totalVisitCount)));

        mActivityTotalCount.setText(String.valueOf("/" + ((isStoreCheckMenu ? mTempMenuList.size() - 1 : mTempMenuList.size()) + mTempMenuStoreList.size())));
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (!bmodel.configurationMasterHelper.SHOW_DGTC)
            menu.findItem(R.id.menu_dgtc).setVisible(false);
        if (!bmodel.configurationMasterHelper.IS_PHOTO_CAPTURE)
            menu.findItem(R.id.menu_photo).setVisible(false);
        if (!bmodel.configurationMasterHelper.IS_TASK)
            menu.findItem(R.id.menu_reminder).setVisible(false);
        if (!bmodel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG)
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
                bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                        SDUtil.now(SDUtil.DATE_GLOBAL),
                        SDUtil.now(SDUtil.TIME), MENU_TASK);
                Intent intent = new Intent(getApplicationContext(), Task.class);
                intent.putExtra("IsRetailerwisetask", true);
                startActivity(intent);

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
        } else if (i1 == R.id.menu_photo) {
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
                            SDUtil.now(SDUtil.DATE_GLOBAL),
                            SDUtil.now(SDUtil.TIME), MENU_PHOTO);
                    startActivity(new Intent(HomeScreenTwo.this,
                            PhotoCaptureActivity.class).putExtra("isFromMenuClick", true));
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
                            SDUtil.now(SDUtil.DATE_GLOBAL),
                            SDUtil.now(SDUtil.TIME), MENU_PHOTO);
                    startActivity(new Intent(HomeScreenTwo.this,
                            PhotoCaptureActivity.class).putExtra("isFromMenuClick", true));
                    finish();
                }
            }
            return true;
        } else if (i1 == R.id.menu_sales_selection) {
            showDialog(2);
            return true;
        } else if (i1 == R.id.menu_supplier_selection) {

            if (!bmodel.configurationMasterHelper.IS_SUPPLIER_NOT_AVAILABLE && mSupplierList.size() > 0 && mSupplierList.get(0).getIsPrimary() == 1) {// checking first position- because if primary available then there is a need to show seggregated view
                SupplierSelectionDialog dialog = new SupplierSelectionDialog();
                dialog.show(getSupportFragmentManager(), "supplier");
                //Bundle bndl=new Bundle();
                dialog.setCancelable(false);
            } else {
                if (mSupplierList != null && mSupplierList.size() > 0) {
                    updateDefaultSupplierSelection();
                    showDialog(3);
                }
            }

            return true;
        } else if (i1 == R.id.menu_dgtc) {
            if (!isClick) {
                isClick = true;
                Intent intent = new Intent(HomeScreenTwo.this,
                        StoreWiseGallery.class);
                startActivity(intent);
                finish();
            }
            return true;
        } else if (i1 == R.id.menu_digital_content) {
            if (!isClick) {
                isClick = true;
                DigitalContentHelper mDigitalContentHelper = DigitalContentHelper.getInstance(this);
                mDigitalContentHelper.downloadDigitalContent(getApplicationContext(), "RETAILER");
                if (mDigitalContentHelper.getDigitalMaster() != null
                        && mDigitalContentHelper.getDigitalMaster()
                        .size() > 0) {
                    bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                            SDUtil.now(SDUtil.DATE_GLOBAL),
                            SDUtil.now(SDUtil.TIME), MENU_DGT);
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
                showDialog(4);
            } else {
                Toast.makeText(this, "No order type available ", Toast.LENGTH_SHORT).show();
            }

        } else if (i1 == R.id.menu_profile_view) {
            Intent prof = new Intent(HomeScreenTwo.this, ProfileActivity.class);
            prof.putExtra("hometwo", true);
            startActivity(prof);
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateDefaultSupplierSelection() {
        try {
            mDefaultSupplierSelection = bmodel.getSupplierPosition(mSupplierList);
            if (mSupplierList != null && mSupplierList.size() > 0) {
                bmodel.getRetailerMasterBO().setSupplierBO(
                        mSupplierList.get(mDefaultSupplierSelection));
                bmodel.getRetailerMasterBO().setDistributorId(mSupplierList.get(mDefaultSupplierSelection).getSupplierID());
                bmodel.getRetailerMasterBO().setDistParentId(mSupplierList.get(mDefaultSupplierSelection).getDistParentID());
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

        bmodel.isModuleDone();

        try {
            for (int i = 0; i < size; i++) {
                menuDB.get(i).setDone(false);
            }

            if (menuDB.get(0).getHasLink() == 0) {
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
                        if (SalesReturnHelper.getInstance(this).isStockReplacementDone())
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
                } else if (menuDB.get(i).getConfigCode().equals(MENU_WITS)) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (bmodel.getRetailerMasterBO()
                                .getIsMerchandisingDone().equals("Y"))
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
                        if (bmodel.getRetailerMasterBO().getIsPresentation()
                                .equals("Y")) {
                            menuDB.get(i).setDone(true);
                        }
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
                        if (bmodel.deliveryManagementHelper.isDeliveryMgtDone())
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
                        if (bmodel.getRetailerMasterBO().getIsOrdered()
                                .equals("Y")
                                || bmodel.getRetailerMasterBO()
                                .getIsOrderMerch().equals("Y") || bmodel.isModuleCompleted(menuDB.get(i).getConfigCode()))
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

        boolean isDeviatedStore = bmodel.getRetailerMasterBO().getIsDeviated()
                .equals("Y");// true deviated store,false not deviated store

        //location dialog show from store click
        isLocDialogShow = false;

        // this conditon added to load download product
        // filter method once when GLOBAL CATEGORY SELECTION enabled
        if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY && bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER) {
            if (menu.getConfigCode().equals(MENU_STOCK)
                    || menu.getConfigCode().equals(MENU_COMBINED_STOCK)
                    || menu.getConfigCode().equals(MENU_ORDER)
                    || menu.getConfigCode().equals(MENU_STK_ORD)
                    || menu.getConfigCode().equals(MENU_SALES_RET)
                    || menu.getConfigCode().equals(MENU_NEAREXPIRY)
                    || menu.getConfigCode().equals(MENU_PRICE)
                    || menu.getConfigCode().equals(MENU_PRICE_COMP)
                    || menu.getConfigCode().equals(MENU_EMPTY_RETURN)
                    || menu.getConfigCode().equals(MENU_DELIVERY)
                    || menu.getConfigCode().equals(MENU_DGT)
                    && hasLink == 1) {
                if (bmodel.productHelper.getmLoadedGlobalProductId() != bmodel.productHelper.getmSelectedGlobalProductId()) {
                    bmodel.productHelper
                            .downloadFiveFilterLevels(MENU_STK_ORD);
                    bmodel.productHelper
                            .downloadProductsWithFiveLevelFilter(MENU_STK_ORD);
                }

            }
        }
        if (menu.getConfigCode().equals(MENU_STOCK)
                || menu.getConfigCode().equals(MENU_COMBINED_STOCK) && hasLink == 1) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP) {


                bmodel.productHelper.downloadTaggedProducts(MENU_STOCK);

                /** Download location to load in the filter. **/
                bmodel.productHelper.downloadInStoreLocations();


                if (bmodel.configurationMasterHelper.IS_LOAD_STOCK_COMPETITOR) {
                    bmodel.productHelper.downloadCompetitorProducts(MENU_STOCK);
                    if (menu.getConfigCode().equals(MENU_COMBINED_STOCK))
                        bmodel.productHelper.downloadCompetitorTaggedProducts("MENU_COMB_STK");
                    else
                        bmodel.productHelper.downloadCompetitorTaggedProducts(menu.getConfigCode());
                }

                if (bmodel.productHelper.getTaggedProducts().size() > 0) {
                    if (bmodel.configurationMasterHelper.SHOW_STOCK_AVGDAYS && menu.getConfigCode().equals(MENU_COMBINED_STOCK))
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
                    } else if (bmodel.configurationMasterHelper.IS_STOCK_CHECK_RETAIN_LAST_VISIT_TRAN) {
                        // load last visit data
                        bmodel.loadLastVisitStockCheckedProducts(bmodel.getRetailerMasterBO().getRetailerID());
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

                    bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                            SDUtil.now(SDUtil.DATE_GLOBAL),
                            SDUtil.now(SDUtil.TIME), menu.getConfigCode());

                    /**
                     * Download product long-press information dialog
                     * configurations.
                     **/
                    bmodel.configurationMasterHelper.downloadProductDetailsList();

                    // Load Data for Special Filter
                    bmodel.configurationMasterHelper.downloadFilterList();
                    bmodel.productHelper.updateProductColor();


                    /** Load the screen **/
                    Intent intent;
                    if (menu.getConfigCode().equals(MENU_COMBINED_STOCK)) {
                        intent = new Intent(HomeScreenTwo.this,
                                CombinedStockFragmentActivity.class);
                        intent.putExtra("CurrentActivityCode", menu.getConfigCode());
                        if (isFromChild)
                            intent.putExtra("isFromChild", isFromChild);
                    } else {
                        intent = new Intent(HomeScreenTwo.this,
                                StockCheckActivity.class);
                        intent.putExtra("CurrentActivityCode", menu.getConfigCode());
                        if (isFromChild)
                            intent.putExtra("isFromChild", isFromChild);
                    }
                    //intent.putExtra("screentitle", menu.getMenuName());
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

        } else if (menu.getConfigCode().equals(MENU_ORDER) && hasLink == 1) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP
                    ) {
                if (bmodel.configurationMasterHelper
                        .downloadFloatingSurveyConfig(MENU_ORDER)) {
                    SurveyHelperNew surveyHelperNew = SurveyHelperNew.getInstance(this);
                    surveyHelperNew.setFromHomeScreen(false);
                    surveyHelperNew.downloadModuleId("STANDARD");
                    surveyHelperNew.downloadQuestionDetails(MENU_ORDER);
                    surveyHelperNew.loadSurveyAnswers(0);
                }

                if (bmodel.productHelper.getProductMaster().size() > 0) {
                    bmodel.configurationMasterHelper.downloadFloatingNPReasonWithPhoto(MENU_ORDER);

                    if (!bmodel.configurationMasterHelper.IS_VALIDATE_CREDIT_DAYS
                            || bmodel.getRetailerMasterBO().getCreditDays() == 0
                            || bmodel.productHelper.isCheckCreditPeriod()) {

                        if (bmodel.hasAlreadyStockChecked(bmodel
                                .getRetailerMasterBO().getRetailerID())) {
                            bmodel.loadStockCheckedProducts(bmodel
                                    .getRetailerMasterBO().getRetailerID(), menu.getConfigCode());
                        }

                        bmodel.setEdit(false);
                        if (bmodel.hasAlreadyOrdered(bmodel.getRetailerMasterBO()
                                .getRetailerID())) {
                            bmodel.setEdit(true);
                        }

                        if (bmodel.configurationMasterHelper.IS_SHOW_IRDERING_SEQUENCE) {
                            bmodel.productHelper.getmProductidOrderByEntry().clear();
                            bmodel.productHelper.getmProductidOrderByEntryMap().clear();
                        }


                        if (bmodel.isEdit()) {
                            bmodel.loadOrderedProducts(bmodel.getRetailerMasterBO()
                                    .getRetailerID(), null);
                            bmodel.productHelper.loadSerialNo();
                            enableSchemeModule();
                        }
                        if (bmodel.configurationMasterHelper.SHOW_DISC_AMOUNT_ALLOW) {
                            bmodel.collectionHelper.downloadDiscountSlab();
                        }
                        if (bmodel.configurationMasterHelper.SHOW_COLLECTION_BEFORE_INVOICE)
                            bmodel.collectionHelper.loadCreditNote();
                        //   bmodel.productHelper.downloadProductFilter("MENU_STK_ORD"); /*03/09/2015*/
                        bmodel.productHelper.loadRetailerWiseProductWisePurchased();
                        bmodel.productHelper
                                .loadRetailerWiseProductWiseP4StockAndOrderQty();
                        bmodel.configurationMasterHelper
                                .downloadProductDetailsList();
                        bmodel.collectionHelper.downloadBankDetails();
                        bmodel.collectionHelper.downloadBranchDetails();
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

                        if (bmodel.configurationMasterHelper.IS_SCHEME_ON_MASTER)
                            bmodel.schemeDetailsMasterHelper.loadSchemeHistoryDetails();


                        // Reset the Configuration if Directly goes from
                        // HomeScreenTwo
                        bmodel.mSelectedModule = -1;

                        bmodel.productHelper.downloadInStoreLocations();

                        OrderSummary.mActivityCode = menu.getConfigCode();

                        //load currency data
                        if (bmodel.configurationMasterHelper.IS_FORMAT_USING_CURRENCY_VALUE) {
                            bmodel.downloadCurrencyConfig();
                        }


                        if (bmodel.isEdit()) {
                            Intent intent = new Intent(HomeScreenTwo.this,
                                    OrderSummary.class);
                            intent.putExtra("ScreenCode", "MENU_ORDER");
                            startActivity(intent);
                            finish();

                        } else {
                            bmodel.productHelper.downloadIndicativeOrderList();

                            for (Integer temp : bmodel.productHelper
                                    .getIndicativeList())
                                indicativeOrderAdapter.add(temp);

                            if (bmodel.configurationMasterHelper.SHOW_INVOICE_CREDIT_BALANCE) {
                                if (bmodel.getRetailerMasterBO()
                                        .getCredit_balance() == -1
                                        || bmodel.getRetailerMasterBO()
                                        .getCredit_balance() > 0) {

                                    if (bmodel.productHelper.getIndicativeList() != null) {
                                        if (bmodel.productHelper.getIndicativeList().size() > 1) {
                                            showIndicativeOrderFilterAlert(menu.getConfigCode());
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
                                                    SDUtil.now(SDUtil.DATE_GLOBAL),
                                                    SDUtil.now(SDUtil.TIME),
                                                    menu.getConfigCode());
                                    Intent i = new Intent(HomeScreenTwo.this,
                                            StockAndOrder.class);
                                    i.putExtra("OrderFlag", "Nothing");
                                    i.putExtra("ScreenCode",
                                            ConfigurationMasterHelper.MENU_ORDER);
                                    startActivity(i);
                                    overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                                    finish();
                                } else {
                                    showDialog(1);
                                    isCreated = false;
                                }
                            } else {

                                if (bmodel.productHelper.getIndicativeList() != null) {
                                    if (bmodel.productHelper.getIndicativeList().size() > 1) {
                                        showIndicativeOrderFilterAlert(menu.getConfigCode());
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
                                                SDUtil.now(SDUtil.DATE_GLOBAL),
                                                SDUtil.now(SDUtil.TIME),
                                                menu.getConfigCode());
                                Intent i = new Intent(HomeScreenTwo.this,
                                        StockAndOrder.class);
                                i.putExtra("OrderFlag", "Nothing");
                                i.putExtra("ScreenCode",
                                        ConfigurationMasterHelper.MENU_ORDER);
                                startActivity(i);
                                finish();
                            }
                        }
                    } else {
                        Toast.makeText(
                                this,
                                getResources().getString(
                                        R.string.please_pay_old_invoice),
                                Toast.LENGTH_SHORT).show();
                        isCreated = false;
                    }


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

        } else if (menu.getConfigCode().equals(MENU_STK_ORD)
                || menu.getConfigCode().equals(MENU_CATALOG_ORDER) && hasLink == 1) {

            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP) {

                if (bmodel.configurationMasterHelper.IS_RESTRICT_ORDER_TAKING
                        && (bmodel.getRetailerMasterBO().getRField4().equals("1")
                        || (bmodel.getRetailerMasterBO().getTinExpDate() != null && !bmodel.getRetailerMasterBO().getTinExpDate().isEmpty() && SDUtil.compareDate(SDUtil.now(SDUtil.DATE_GLOBAL), bmodel.getRetailerMasterBO().getTinExpDate(), "yyyy/MM/dd") > 0))) {
                    bmodel.showAlert(getResources().getString(R.string.order_not_allowed_for_retailer), 0);
                    isCreated = false;
                    return;
                }

                if (bmodel.configurationMasterHelper
                        .downloadFloatingSurveyConfig(MENU_STK_ORD)) {
                    SurveyHelperNew surveyHelperNew = SurveyHelperNew.getInstance(this);
                    surveyHelperNew.setFromHomeScreen(false);
                    surveyHelperNew.downloadModuleId("STANDARD");
                    surveyHelperNew.downloadQuestionDetails(MENU_STK_ORD);
                    surveyHelperNew.loadSurveyAnswers(0);
                }

                if (bmodel.productHelper.getProductMaster().size() > 0) {
                    bmodel.configurationMasterHelper.downloadFloatingNPReasonWithPhoto(MENU_STK_ORD);
                    if (!bmodel.configurationMasterHelper.IS_VALIDATE_CREDIT_DAYS
                            || bmodel.getRetailerMasterBO().getCreditDays() == 0
                            || bmodel.productHelper.isCheckCreditPeriod()) {

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
                            }
                        }
                        bmodel.productHelper.setProductImageUrl();
                        bmodel.setEdit(false);
                        if (bmodel.hasAlreadyOrdered(bmodel.getRetailerMasterBO()
                                .getRetailerID())) {
                            bmodel.setEdit(true);
                        }


                        if (bmodel.configurationMasterHelper.IS_SHOW_IRDERING_SEQUENCE) {
                            bmodel.productHelper.getmProductidOrderByEntry().clear();
                            bmodel.productHelper.getmProductidOrderByEntryMap().clear();
                        }

                        bmodel.productHelper.downloadIndicativeOrderList();//moved here to check size of indicative order
                        bmodel.selectedOrderId = "";
                        if (bmodel.productHelper.getIndicativeList() != null
                                && bmodel.productHelper.getIndicativeList().size() < 1
                                && bmodel.configurationMasterHelper.IS_MULTI_STOCKORDER) {
                            if (bmodel.isEdit()) {
                                bmodel.selectedOrderId = "";//cleared to avoid reuse of id
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
                                        bmodel.selectedOrderId = id;
                                        //the methods that were called during normal stock and order loading in edit mode are called here
                                        //selectedOrderId is passed to loadOrderedProducts method  to load ordered products for that id
                                        //loadSerialNo,enableSchemeModule included as these were called in edit mode
                                        bmodel.loadOrderedProducts(bmodel.getRetailerMasterBO()
                                                .getRetailerID(), id);
                                        bmodel.productHelper.loadSerialNo();
                                        enableSchemeModule();
                                        loadRequiredMethodsForStockAndOrder(menuConfigCode, menuName);
                                        loadOrderSummaryScreen(menuConfigCode);
                                    }
                                });
                                obj.show();
                            } else {
                                //the methods that were called during normal stock and order loading in non edit mode are called here
                                //loadOrderedProducts,loadSerialNo,enableSchemeModule are used in edit mode so avoided here as in this case screen should be loaded fresh
                                bmodel.setOrderHeaderBO(null);
                                loadRequiredMethodsForStockAndOrder(menu.getConfigCode(), menu.getMenuName());
                                loadstockorderscreen(menu.getConfigCode());
                            }
                        } else {
                            if (bmodel.isEdit()) {//doubt
                                bmodel.loadOrderedProducts(bmodel.getRetailerMasterBO()
                                        .getRetailerID(), null);
                                bmodel.productHelper.loadSerialNo();
                                enableSchemeModule();
                            } else {
                                bmodel.setOrderHeaderBO(null);
                                if (bmodel.configurationMasterHelper.IS_TEMP_ORDER_SAVE && menu.getConfigCode().equals(MENU_CATALOG_ORDER)) {
                                    bmodel.loadTempOrderDetails();
                                }
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
                                getResources().getString(
                                        R.string.please_pay_old_invoice),
                                Toast.LENGTH_SHORT).show();
                        isCreated = false;
                    }

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

        } else if (menu.getConfigCode().equals(MENU_CLOSING) && hasLink == 1) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP
                    ) {

                /** Load the stock check if opened in edit mode. **/
                bmodel.setEditStockCheck(false);
                if (bmodel.hasAlreadyStockChecked(bmodel.getRetailerMasterBO()
                        .getRetailerID())) {
                    bmodel.setEditStockCheck(true);
                    bmodel.loadStockCheckedProducts(bmodel
                            .getRetailerMasterBO().getRetailerID(), menu.getConfigCode());
                }

                bmodel.setEdit(false);

                if (bmodel.hasAlreadyOrdered(bmodel.getRetailerMasterBO()
                        .getRetailerID())) {
                    bmodel.setEdit(true);

                    if (bmodel.isEdit()) {
                        bmodel.loadOrderedProducts(bmodel.getRetailerMasterBO()
                                .getRetailerID(), null);
                        bmodel.productHelper.loadSerialNo();
                        enableSchemeModule();
                    }
                    bmodel.productHelper.downloadProductFilter("MENU_STK_ORD");
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
                            SDUtil.now(SDUtil.DATE_GLOBAL),
                            SDUtil.now(SDUtil.TIME), menu.getConfigCode());

                    OrderSummary.mActivityCode = menu.getConfigCode();

                    Intent i = new Intent(HomeScreenTwo.this,
                            OrderSummary.class);
                    i.putExtra("FromClose", "Closing");
                    startActivity(i);
                    finish();

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
        } else if ((menu.getConfigCode().equals(MENU_SURVEY)
                || menu.getConfigCode().equals(MENU_SURVEY01)
                || menu.getConfigCode().equals(MENU_QUALITY)
                || menu.getConfigCode().equals(MENU_PERSUATION)
                || menu.getConfigCode().equals(MENU_SURVEY_QDVP3))
                && hasLink == 1) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP) {

                SurveyHelperNew surveyHelperNew = SurveyHelperNew.getInstance(this);

                surveyHelperNew.setFromHomeScreen(false);
                surveyHelperNew.downloadModuleId("STANDARD");

                bmodel.configurationMasterHelper.downloadFloatingNPReasonWithPhoto(menu.getConfigCode());
                chooseFilterType(menu.getConfigCode());

                surveyHelperNew.downloadQuestionDetails(menu.getConfigCode());
                surveyHelperNew.loadSurveyAnswers(0);

                if (surveyHelperNew.getSurvey() != null
                        && surveyHelperNew.getSurvey().size() > 0) {
                    bmodel.mSelectedActivityName = menu.getMenuName();
                    bmodel.mSelectedActivityConfigCode = menu.getConfigCode();
                    surveyHelperNew.loadSurveyConfig(menu
                            .getConfigCode());

                    bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                            SDUtil.now(SDUtil.DATE_GLOBAL),
                            SDUtil.now(SDUtil.TIME), menu.getConfigCode());

                    Intent intent = new Intent(HomeScreenTwo.this,
                            SurveyActivityNew.class);
                    intent.putExtra("screentitle", menu.getMenuName());
                    intent.putExtra("SurveyType", 0);
                    intent.putExtra("menucode", menu.getConfigCode());
                    intent.putExtra("from", "HomeScreenTwo");
                    intent.putExtra("CurrentActivityCode", menu.getConfigCode());
                    if (isFromChild)
                        intent.putExtra("isFromChild", isFromChild);
                    startActivity(intent);
                    finish();
                    isCreated = true;
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

        } else if (menu.getConfigCode().equals(MENU_TASK) && hasLink == 1) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP
                    ) {
                if (!isClick) {
                    isClick = true;
                    // finish();
                    if (bmodel.taskHelper.getTaskData(bmodel.getRetailerMasterBO().getRetailerID()).size() > 0) {
                        bmodel.configurationMasterHelper.downloadFloatingNPReasonWithPhoto(MENU_TASK);
                        bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                                SDUtil.now(SDUtil.DATE_GLOBAL),
                                SDUtil.now(SDUtil.TIME), menu.getConfigCode());
                        Intent intent = new Intent(getApplicationContext(),
                                Task.class);
                        intent.putExtra("CurrentActivityCode", menu.getConfigCode());
                        intent.putExtra("IsRetailerwisetask", true);

                        startActivity(intent);
                        isCreated = false;
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

        } else if (menu.getConfigCode().equals(MENU_KELLGS_DASH) && hasLink == 1) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP
                    ) {
                if (!isClick) {
                    isClick = true;
                    // finish();
                    bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                            SDUtil.now(SDUtil.DATE_GLOBAL),
                            SDUtil.now(SDUtil.TIME), menu.getConfigCode());
                    Intent intent = new Intent(getApplicationContext(),
                            KellogsDashBoardActivity.class);
                    intent.putExtra("screenTitle", menu.getMenuName());
                    startActivity(intent);
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

        } else if (menu.getConfigCode().equals(MENU_PHOTO) && hasLink == 1) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP
                    ) {

                int count = bmodel.synchronizationHelper.getImagesCount();
                bmodel.productHelper.getLocations();
                bmodel.productHelper.downloadInStoreLocations();

                PhotoCaptureHelper mPhotoCaptureHelper = PhotoCaptureHelper.getInstance(this);
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
                                    SDUtil.now(SDUtil.DATE_GLOBAL),
                                    SDUtil.now(SDUtil.TIME), menu.getConfigCode());

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
                                    SDUtil.now(SDUtil.DATE_GLOBAL),
                                    SDUtil.now(SDUtil.TIME), menu.getConfigCode());

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
        } else if (menu.getConfigCode().equals(MENU_INVOICE) && hasLink == 1) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP
                    ) {

                if (bmodel.isOrderExistToCreateInvoice()) {

                    if (bmodel.isEdit()) {
                        bmodel.setEdit(true);
                        bmodel.loadOrderedProducts(bmodel.getRetailerMasterBO()
                                .getRetailerID(), null);
                        bmodel.productHelper.loadSerialNo();
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

                    bmodel.loadOrderedProducts(bmodel.getRetailerMasterBO()
                            .getRetailerID(), null);
                    // Intent intent = new Intent(HomeScreenTwo.this,
                    // OrderSummary.class);
                    Intent intent = null;
                    bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                            SDUtil.now(SDUtil.DATE_GLOBAL),
                            SDUtil.now(SDUtil.TIME), menu.getConfigCode());
                    if (bmodel.configurationMasterHelper.SHOW_BIXOLONII) {

                        intent = new Intent(HomeScreenTwo.this,
                                BixolonIIPrint.class);
                    } else if (bmodel.configurationMasterHelper.SHOW_BIXOLONI) {
                        intent = new Intent(HomeScreenTwo.this,
                                BixolonIPrint.class);
                    } else if (bmodel.configurationMasterHelper.SHOW_ZEBRA) {
                        intent = new Intent(HomeScreenTwo.this,
                                InvoicePrintZebraNew.class);
                    } else if (bmodel.configurationMasterHelper.SHOW_ZEBRA_ATS) {
                        intent = new Intent(HomeScreenTwo.this,
                                PrintPreviewScreen.class);
                    } else if (bmodel.configurationMasterHelper.SHOW_INTERMEC_ATS) {
                        intent = new Intent(HomeScreenTwo.this,
                                BtPrint4Ivy.class);
                    } else if (bmodel.configurationMasterHelper.SHOW_ZEBRA_DIAGEO) {
                        intent = new Intent(HomeScreenTwo.this,
                                PrintPreviewScreenDiageo.class);
                    } else {
                        intent = new Intent(HomeScreenTwo.this,
                                BixolonIIPrint.class);
                    }
                    intent.putExtra("IsFromOrder", false);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
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
                && hasLink == 1) {
            if (bmodel.configurationMasterHelper.IS_JUMP
                    || isPreviousDone(menu)) {

                if (bmodel.configurationMasterHelper.SHOW_DISC_AMOUNT_ALLOW) {
                    bmodel.collectionHelper.downloadDiscountSlab();
                }

                bmodel.collectionHelper.downloadBankDetails();
                bmodel.collectionHelper.downloadBranchDetails();
                bmodel.collectionHelper.updateInvoiceDiscountedAmount();


                bmodel.downloadInvoice(bmodel.getRetailerMasterBO().getRetailerID(), "COL");
                bmodel.collectionHelper.loadPaymentMode();

                //load currency data
                if (bmodel.configurationMasterHelper.IS_FORMAT_USING_CURRENCY_VALUE) {
                    bmodel.downloadCurrencyConfig();
                }

                bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                        SDUtil.now(SDUtil.DATE_GLOBAL),
                        SDUtil.now(SDUtil.TIME), menu.getConfigCode());

                if (menu.getConfigCode().equals(
                        StandardListMasterConstants.MENU_COLLECTION_VIEW)) {
                    bmodel.collectionHelper.setCollectionView(true);
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
                Toast.makeText(
                        this,
                        getResources().getString(
                                R.string.please_complete_previous_activity),
                        Toast.LENGTH_SHORT).show();
                isCreated = false;
            }
        } else if (menu.getConfigCode().equals(MENU_COLLECTION_REF)
                && hasLink == 1) {
            if (bmodel.configurationMasterHelper.IS_JUMP
                    || isPreviousDone(menu)) {

                bmodel.collectionHelper.updateInvoiceDiscountedAmount();
                bmodel.downloadInvoice(bmodel.getRetailerMasterBO().getRetailerID(), "DOC");
                bmodel.collectionHelper.loadCollectionReference();

                bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                        SDUtil.now(SDUtil.DATE_GLOBAL),
                        SDUtil.now(SDUtil.TIME), menu.getConfigCode());

                Intent intent = new Intent(HomeScreenTwo.this,
                        CollectionReference.class);
                bmodel.mSelectedActivityName = menu.getMenuName();
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
        } else if ((menu.getConfigCode().equals(MENU_SALES_RET) && hasLink == 1)
                || (menu.getConfigCode().equals(StandardListMasterConstants.MENU_STOCK_REPLACEMENT) && hasLink == 1)) {

            if (isPreviousDone(menu) || bmodel.configurationMasterHelper.IS_JUMP) {

                SalesReturnHelper salesReturnHelper = SalesReturnHelper.getInstance(this);
                salesReturnHelper.loadSalesReturnConfigurations();

                bmodel.reasonHelper.downloadSalesReturnReason();

                if (bmodel.reasonHelper.getReasonSalesReturnMaster().size() > 0) {

                    bmodel.productHelper.downloadSalesReturnProducts();
                    if (salesReturnHelper.IS_PRD_CNT_DIFF_SR)
                        bmodel.productHelper.downloadSalesReturnSKUs();


                    bmodel.productHelper.cloneReasonMaster();

                    Commons.print("Sales Return Prod Size<><><><<>" + bmodel.productHelper.getSalesReturnProducts().size());

                    salesReturnHelper.getInstance(this).clearSalesReturnTable();

                    if (!bmodel.configurationMasterHelper.IS_INVOICE) {
                        salesReturnHelper.getInstance(this).removeSalesReturnTable();
                        salesReturnHelper.getInstance(this).loadSalesReturnData();
                    }

                    bmodel.updateProductUOM(StandardListMasterConstants.mActivityCodeByMenuCode.get(MENU_SALES_RET), 1);

                    //bmodel.salesReturnHelper.setSalesEdit(false);
                    bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                            SDUtil.now(SDUtil.DATE_GLOBAL),
                            SDUtil.now(SDUtil.TIME), menu.getConfigCode());

                    Intent intent = new Intent(HomeScreenTwo.this,
                            SalesReturnActivity.class);
                    intent.putExtra("CurrentActivityCode", menu.getConfigCode());
                    intent.putExtra("screentitle", menu.getMenuName());
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(
                            HomeScreenTwo.this,
                            getResources()
                                    .getString(
                                            R.string.reasonmaster_not_downloaded),
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
        } else if (menu.getConfigCode().equals(MENU_WITS) && hasLink == 1) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP
                    ) {

                if (bmodel.getRetailerMasterBO().getIsMerchandisingDone()
                        .equals("Y")) {
                    bmodel.mSelectedActivityName = menu.getMenuName();
                    showDialog(0);
                } else {
                    bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                            SDUtil.now(SDUtil.DATE_GLOBAL),
                            SDUtil.now(SDUtil.TIME), menu.getConfigCode());
                    Intent sbd = new Intent(HomeScreenTwo.this,
                            MerchandisingActivity.class);
                    Commons.print("menu name" + menu.getMenuName());
                    sbd.putExtra("screentitle", menu.getMenuName());
                    sbd.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(sbd);
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

        } else if (menu.getConfigCode().equals(MENU_DGT) && hasLink == 1) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP
                    ) {
                DigitalContentHelper mDigitalContentHelper = DigitalContentHelper.getInstance(this);
                mDigitalContentHelper.downloadDigitalContent(getApplicationContext(), "RETAILER");
                if (mDigitalContentHelper.getDigitalMaster() != null
                        && mDigitalContentHelper.getDigitalMaster()
                        .size() > 0) {
                    bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                            SDUtil.now(SDUtil.DATE_GLOBAL),
                            SDUtil.now(SDUtil.TIME), menu.getConfigCode());
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

        } else if (menu.getConfigCode().equals(MENU_REV) && hasLink == 1) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP
                    ) {
                if (bmodel.configurationMasterHelper.IS_TARGET_SCREEN_PH) {
                    bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                            SDUtil.now(SDUtil.DATE_GLOBAL),
                            SDUtil.now(SDUtil.TIME), menu.getConfigCode());
                    Intent i = new Intent(HomeScreenTwo.this,
                            TargetPlanActivity_PH.class);
                    i.putExtra("From", "Review");
                    startActivity(i);
                    finish();
                } else {
                    bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                            SDUtil.now(SDUtil.DATE_GLOBAL),
                            SDUtil.now(SDUtil.TIME), menu.getConfigCode());
                    Intent i = new Intent(HomeScreenTwo.this,
                            TargetPlanActivity.class);
                    i.putExtra("From", "Review");
                    i.putExtra("screentitle", menu.getMenuName());
                    startActivity(i);
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

        } else if (menu.getConfigCode().equals(MENU_CALL_ANLYS)) {
            if ((bmodel.configurationMasterHelper.IS_JUMP ? false : isPreviousDone(menu))
                    || (bmodel.configurationMasterHelper.IS_JUMP && isAllMandatoryMenuDone())
                    || !canAllowCallAnalysis()) {
                bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                        SDUtil.now(SDUtil.DATE_GLOBAL),
                        SDUtil.now(SDUtil.TIME), menu.getConfigCode());

                // bmodel.productHelper.downloadIndicativeOrder();

                if (bmodel.isEdit()) {
                    bmodel.loadOrderedProducts(bmodel.getRetailerMasterBO()
                            .getRetailerID(), null);
                    enableSchemeModule();
                }
                if (menuCodeList.size() > 0)
                    menuCodeList.clear();
                Intent in = new Intent(HomeScreenTwo.this,
                        CallAnalysisActivity.class);
                in.putExtra("screentitle", menu.getMenuName());
                startActivity(in);
                finish();
            } else {
                if (bmodel.configurationMasterHelper.IS_JUMP)
                    onCreateDialog(5);
                else
                    Toast.makeText(
                            this,
                            getResources().getString(
                                    R.string.please_complete_previous_activity),
                            Toast.LENGTH_SHORT).show();
                isCreated = false;
            }

        } else if (menu.getConfigCode().equals(MENU_ASSET) && hasLink == 1) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP) {

                AssetTrackingHelper assetTrackingHelper = AssetTrackingHelper.getInstance(this);
                assetTrackingHelper.loadDataForAssetPOSM(getApplicationContext(), MENU_ASSET);

                if (assetTrackingHelper.getAssetTrackingList().size() > 0) {

                    assetTrackingHelper.mSelectedActivityName = menu.getMenuName();

                    bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                            SDUtil.now(SDUtil.DATE_GLOBAL),
                            SDUtil.now(SDUtil.TIME), menu.getConfigCode());

                    Intent in = new Intent(HomeScreenTwo.this,
                            AssetTrackingActivity.class);
                    in.putExtra("CurrentActivityCode", menu.getConfigCode());
                    if (isFromChild)
                        in.putExtra("isFromChild", isFromChild);
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

        } else if (menu.getConfigCode().equals(MENU_POSM) && hasLink == 1) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP
                    ) {

                AssetTrackingHelper assetTrackingHelper = AssetTrackingHelper.getInstance(this);

                assetTrackingHelper.loadDataForAssetPOSM(getApplicationContext(), MENU_POSM);

                if (assetTrackingHelper.getAssetTrackingList().size() > 0) {

                    bmodel.mSelectedActivityName = menu.getMenuName();

                    bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                            SDUtil.now(SDUtil.DATE_GLOBAL),
                            SDUtil.now(SDUtil.TIME), menu.getConfigCode());

                    Intent in = new Intent(HomeScreenTwo.this,
                            PosmTrackingActivity.class);
                    in.putExtra("CurrentActivityCode", menu.getConfigCode());
                    if (isFromChild)
                        in.putExtra("isFromChild", isFromChild);
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
                    || bmodel.configurationMasterHelper.IS_JUMP
                    ) {

                NearExpiryTrackingHelper mNearExpiryHelper = NearExpiryTrackingHelper.getInstance(this);

                bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                        SDUtil.now(SDUtil.DATE_GLOBAL), SDUtil.now(SDUtil.TIME),
                        MENU_NEAREXPIRY);
                mNearExpiryHelper.mSelectedActivityName = menu.getMenuName();

                bmodel.productHelper.downloadInStoreLocations();
                mNearExpiryHelper.loadSKUTracking(getApplicationContext(), false);

                if (bmodel.configurationMasterHelper.IS_NEAR_EXPIRY_RETAIN_LAST_VISIT_TRAN && !mNearExpiryHelper.hasAlreadySKUTrackingDone(getApplicationContext())) {
                    mNearExpiryHelper.loadLastVisitSKUTracking(getApplicationContext());
                }

                bmodel.updateProductUOM(StandardListMasterConstants.mActivityCodeByMenuCode.get(MENU_NEAREXPIRY), 1);

                Intent intent = new Intent(HomeScreenTwo.this,
                        NearExpiryTrackingActivity.class);
                intent.putExtra("CurrentActivityCode", menu.getConfigCode());
                if (isFromChild)
                    intent.putExtra("isFromChild", isFromChild);
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
        } else if (menu.getConfigCode().equals(MENU_SKUWISERTGT)
                && hasLink == 1) {

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
                && hasLink == 1) {

            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP
                    ) {
                bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                        SDUtil.now(SDUtil.DATE_GLOBAL),
                        SDUtil.now(SDUtil.TIME), menu.getConfigCode());
                bmodel.retailerContractHelper.downloadRetailerContract(bmodel.getRetailerMasterBO().getRetailerID());
                bmodel.retailerContractHelper.downloadRenewedContract(bmodel.getRetailerMasterBO().getRetailerID());
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
                    || bmodel.configurationMasterHelper.IS_JUMP
                    ) {

                PlanoGramHelper mPlanoGramHelper = PlanoGramHelper.getInstance(this);

                mPlanoGramHelper.mSelectedActivityName = menu.getMenuName();
                mPlanoGramHelper.loadConfigurations(getApplicationContext());
                chooseFilterType(MENU_PLANOGRAM);
                mPlanoGramHelper.downloadLevels(getApplicationContext(), MENU_PLANOGRAM, bmodel.retailerMasterBO.getRetailerID());
                mPlanoGramHelper.downloadMaster(getApplicationContext(), MENU_PLANOGRAM);
                mPlanoGramHelper.loadPlanoGramInEditMode(getApplicationContext(), bmodel.retailerMasterBO.getRetailerID());
                bmodel.configurationMasterHelper.downloadFloatingNPReasonWithPhoto(MENU_PLANOGRAM);

                if (mPlanoGramHelper.getPlanogramMaster() != null && mPlanoGramHelper.getPlanogramMaster().size() > 0) {
                    bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                            SDUtil.now(SDUtil.DATE_GLOBAL),
                            SDUtil.now(SDUtil.TIME), menu.getConfigCode());

                    Intent in = new Intent(HomeScreenTwo.this,
                            PlanoGramActivity.class);
                    in.putExtra("from", "2");
                    in.putExtra("CurrentActivityCode", menu.getConfigCode());
                    if (isFromChild)
                        in.putExtra("isFromChild", isFromChild);
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
                    || bmodel.configurationMasterHelper.IS_JUMP
                    ) {
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
                bmodel.productHelper.downloadTaggedProducts("PC");

                // Load Price related configurations.
                priceTrackingHelper.loadPriceCheckConfiguration(getApplicationContext(), bmodel.getRetailerMasterBO().getSubchannelid());

                if (priceTrackingHelper.IS_LOAD_PRICE_COMPETITOR) {
                    bmodel.productHelper.downloadCompetitorProducts(MENU_PRICE);
                    bmodel.productHelper.downloadCompetitorTaggedProducts("PC");
                }

                priceTrackingHelper.clearPriceCheck();
                priceTrackingHelper.loadPriceTransaction(getApplicationContext());

                if (bmodel.configurationMasterHelper.IS_PRICE_CHECK_RETAIN_LAST_VISIT_IN_EDIT_MODE && !priceTrackingHelper.isPriceCheckDone(getApplicationContext())) {
                    priceTrackingHelper.updateLastVisitPriceAndMRP();
                }

                bmodel.updateProductUOM(StandardListMasterConstants.mActivityCodeByMenuCode.get(MENU_PRICE), 0);

                bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                        SDUtil.now(SDUtil.DATE_GLOBAL),
                        SDUtil.now(SDUtil.TIME), menu.getConfigCode());


                Intent in = new Intent(HomeScreenTwo.this,
                        PriceTrackActivity.class);
                in.putExtra("CurrentActivityCode", menu.getConfigCode());
                if (isFromChild)
                    in.putExtra("isFromChild", isFromChild);
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
                    || bmodel.configurationMasterHelper.IS_JUMP
                    ) {
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
                bmodel.productHelper.downloadTaggedProducts("PC");

                // Load Price related configurations.
                priceTrackingHelper.loadPriceCheckConfiguration(getApplicationContext(), bmodel.getRetailerMasterBO().getSubchannelid());
                //its menu price comp
                bmodel.productHelper.downloadCompetitorProducts(MENU_PRICE_COMP);
                bmodel.productHelper.downloadCompetitorTaggedProducts("PC");

                priceTrackingHelper.clearPriceCheck();
                priceTrackingHelper.loadPriceTransaction(getApplicationContext());
                bmodel.competitorTrackingHelper.downloadPriceCompanyMaster(MENU_PRICE_COMP);

                if (bmodel.configurationMasterHelper.IS_PRICE_CHECK_RETAIN_LAST_VISIT_IN_EDIT_MODE && !priceTrackingHelper.isPriceCheckDone(getApplicationContext())) {
                    priceTrackingHelper.updateLastVisitPriceAndMRP();
                }

                bmodel.updateProductUOM(StandardListMasterConstants.mActivityCodeByMenuCode.get(MENU_PRICE_COMP), 0);

                bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                        SDUtil.now(SDUtil.DATE_GLOBAL),
                        SDUtil.now(SDUtil.TIME), menu.getConfigCode());


                Intent in = new Intent(HomeScreenTwo.this,
                        PriceTrackCompActivity.class);
                in.putExtra("CurrentActivityCode", menu.getConfigCode());
                if (isFromChild)
                    in.putExtra("isFromChild", isFromChild);
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
                && hasLink == 1) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP
                    ) {
                if (bmodel.configurationMasterHelper.SHOW_GROUPPRODUCTRETURN)
                    bmodel.mEmptyReturnHelper.downloadProductType();
                bmodel.mSelectedActivityName = menu.getMenuName();
                bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                        SDUtil.now(SDUtil.DATE_GLOBAL),
                        SDUtil.now(SDUtil.TIME), menu.getConfigCode());
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
                    || bmodel.configurationMasterHelper.IS_JUMP
                    ) {
                PromotionHelper promotionHelper = PromotionHelper.getInstance(this);
                promotionHelper.loadDataForPromotion(menu.getConfigCode());
                if (promotionHelper.getPromotionList().size() > 0) {
                    bmodel.mSelectedActivityName = menu.getMenuName();
                    bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                            SDUtil.now(SDUtil.DATE_GLOBAL),
                            SDUtil.now(SDUtil.TIME),
                            MENU_PROMO);
                    Intent intent = new Intent(HomeScreenTwo.this,
                            PromotionTrackingActivity.class);
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
        } else if (menu.getConfigCode()
                .equals(MENU_SOS) && hasLink == 1) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP) {

                SalesFundamentalHelper mSFHelper = SalesFundamentalHelper.getInstance(this);
                ShelfShareHelper mShelfShareHelper = ShelfShareHelper.getInstance();

                //Load Configurations
                mSFHelper.updateSalesFundamentalConfigurations();
                mSFHelper.setTotalPopUpConfig();

                //Load the locations
                mSFHelper.downloadLocations();
                mShelfShareHelper.setLocations(mSFHelper.cloneLocationList(mSFHelper.getLocationList()));

                //Load filter
                if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER)
                    mSFHelper.downloadSFFiveLevelFilter(MENU_SOS);
                else
                    bmodel.productHelper.downloadProductFilter(MENU_SOS);

                //load content data
                mSFHelper.loadData(MENU_SOS);

                //load transaction data
                mSFHelper.loadSavedTracking(MENU_SOS);

                if (mSFHelper.getSOSList() != null
                        && mSFHelper.getSOSList().size() > 0) {

                    bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                            SDUtil.now(SDUtil.DATE_GLOBAL),
                            SDUtil.now(SDUtil.TIME),
                            MENU_SOS);

                    mSFHelper.mSelectedActivityName = menu.getMenuName();
                    Intent intent = new Intent(this, SOSActivity.class);
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
        } else if (menu.getConfigCode()
                .equals(MENU_SOS_PROJ) && hasLink == 1) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP) {

                bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                        SDUtil.now(SDUtil.DATE_GLOBAL),
                        SDUtil.now(SDUtil.TIME),
                        MENU_SOS_PROJ);

                Intent intent = new Intent(this, SOSActivity_PRJSpecific.class);
                if (isFromChild)
                    intent.putExtra("isFromChild", isFromChild);
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
        }
        if (menu.getConfigCode()
                .equals(MENU_SOD) && hasLink == 1) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP
                    ) {

                SalesFundamentalHelper mSFHelper = SalesFundamentalHelper.getInstance(this);
                ShelfShareHelper mShelfShareHelper = ShelfShareHelper.getInstance();

                mSFHelper.updateSalesFundamentalConfigurations();
                mSFHelper.setTotalPopUpConfig();

                mSFHelper.downloadLocations();
                mShelfShareHelper.setLocations(mSFHelper.cloneLocationList(mSFHelper.getLocationList()));

                //Load filter
                if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER)
                    mSFHelper.downloadSFFiveLevelFilter(MENU_SOD);
                else
                    bmodel.productHelper.downloadProductFilter(MENU_SOD);

                mSFHelper.loadData(MENU_SOD);

                mSFHelper.loadSavedTracking(MENU_SOD);

                if (mSFHelper.getSODList() != null && mSFHelper.getSODList().size() > 0) {

                    bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                            SDUtil.now(SDUtil.DATE_GLOBAL),
                            SDUtil.now(SDUtil.TIME),
                            MENU_SOD);

                    Intent intent = new Intent(this, SODActivity.class);
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
        } else if (menu.getConfigCode()
                .equals(MENU_SOD_ASSET) && hasLink == 1) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP
                    ) {

                AssetTrackingHelper assetTrackingHelper = AssetTrackingHelper.getInstance(this);
                SODAssetHelper mSODAssetHelper = SODAssetHelper.getInstance(this);

                mSODAssetHelper.downloadLocations();
                assetTrackingHelper.loadDataForAssetPOSM(getApplicationContext(), MENU_ASSET);

                //Load filter
                if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER)
                    mSODAssetHelper.downloadSFFiveLevelFilter(MENU_SOD_ASSET);

                mSODAssetHelper.loadSODAssetData(MENU_SOD_ASSET);

                mSODAssetHelper.loadSavedTracking(MENU_SOD_ASSET);

                if (mSODAssetHelper.getSODList() != null && mSODAssetHelper.getSODList().size() > 0) {

                    bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                            SDUtil.now(SDUtil.DATE_GLOBAL),
                            SDUtil.now(SDUtil.TIME),
                            MENU_SOD_ASSET);

                    Intent intent = new Intent(this, SODAssetActivity.class);
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
        } else if (menu.getConfigCode().equals(
                MENU_SOSKU)
                && hasLink == 1) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP
                    ) {

                SalesFundamentalHelper mSFHelper = SalesFundamentalHelper.getInstance(this);

                mSFHelper.updateSalesFundamentalConfigurations();

                if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER)
                    mSFHelper.downloadSFFiveLevelFilter(MENU_SOSKU);
                else
                    bmodel.productHelper.downloadProductFilter(MENU_SOSKU);

                mSFHelper.loadData(MENU_SOSKU);

                mSFHelper
                        .loadSavedTracking(MENU_SOSKU);

                if (mSFHelper.getSOSKUList() != null && mSFHelper.getSOSKUList().size() > 0) {
                    bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                            SDUtil.now(SDUtil.DATE_GLOBAL),
                            SDUtil.now(SDUtil.TIME),
                            MENU_SOSKU);

                    Intent intent = new Intent(this, SOSKUActivity.class);
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
        } else if (menu.getConfigCode().equals(
                MENU_COMPETITOR)
                && hasLink == 1) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP
                    ) {
                bmodel.competitorTrackingHelper.downloadCompanyMaster(MENU_COMPETITOR);
                bmodel.competitorTrackingHelper.downloadTrackingList();
                bmodel.competitorTrackingHelper
                        .downloadCompetitors(MENU_COMPETITOR);
                bmodel.competitorTrackingHelper.loadcompetitors();
                int companySize = bmodel.competitorTrackingHelper
                        .getCompanyList().size();
                if (companySize > 0) {
                    bmodel.mSelectedActivityName = menu.getMenuName();
                    bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                            SDUtil.now(SDUtil.DATE_GLOBAL),
                            SDUtil.now(SDUtil.TIME),
                            MENU_COMPETITOR);
                    Intent intent = new Intent(this,
                            CompetitorTrackingActivity.class);
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
        } else if (menu.getConfigCode().equals(MENU_CLOSE_CALL)
                && hasLink == 1) {
            if ((bmodel.configurationMasterHelper.IS_JUMP ? false : isPreviousDone(menu))
                    || (bmodel.configurationMasterHelper.IS_JUMP && isAllMandatoryMenuDone())
                    || !canAllowCallAnalysis()) {
                bmodel.reasonHelper.downloadClosecallReasonList();
                bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                        SDUtil.now(SDUtil.DATE_GLOBAL),
                        SDUtil.now(SDUtil.TIME), menu.getConfigCode());
                int reasonsize = bmodel.reasonHelper.getClosecallReasonList().size();

                if (reasonsize > 0) {
                    if (menuCodeList.size() > 0)
                        menuCodeList.clear();
                    Intent intent = new Intent(this, CloseCallActivity.class);
                    startActivity(intent);

                } else {
                    dataNotMapped();
                    isCreated = false;
                }
            } else {
                if (bmodel.configurationMasterHelper.IS_JUMP)
                    onCreateDialog(5);
                else
                    Toast.makeText(
                            this,
                            getResources().getString(
                                    R.string.please_complete_previous_activity),
                            Toast.LENGTH_SHORT).show();
                isCreated = false;
            }
        } else if (menu.getConfigCode().equals(MENU_CLOSE_KLGS)
                && hasLink == 1) {
            {
                if ((bmodel.configurationMasterHelper.IS_JUMP ? false : isPreviousDone(menu))
                        || (bmodel.configurationMasterHelper.IS_JUMP && isAllMandatoryMenuDone())
                        || !canAllowCallAnalysis()) {
                    bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                            SDUtil.now(SDUtil.DATE_GLOBAL),
                            SDUtil.now(SDUtil.TIME), menu.getConfigCode());

                    if (menuCodeList.size() > 0)
                        menuCodeList.clear();
                    Intent in = new Intent(HomeScreenTwo.this,
                            CallAnalysisActivityKlgs.class);
                    in.putExtra("screentitle", menu.getMenuName());
                    startActivity(in);
                    finish();
                } else {
                    if (bmodel.configurationMasterHelper.IS_JUMP)
                        onCreateDialog(5);
                    else
                        Toast.makeText(
                                this,
                                getResources().getString(
                                        R.string.please_complete_previous_activity),
                                Toast.LENGTH_SHORT).show();
                    isCreated = false;
                }

            }
        } else if (menu.getConfigCode().equals(MENU_DELIVERY) && hasLink == 1) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP
                    || menu.getModule_Order() == 1) {
                bmodel.configurationMasterHelper.loadDeliveryUOMConfiguration();
                bmodel.mSelectedActivityName = menu.getMenuName();
                Intent i = new Intent(this, DeliveryManagement.class);
                i.putExtra("screentitle", menu.getMenuName());
                startActivity(i);
            }

        } else if (menu.getConfigCode().equals(MENU_LOYALTY_POINTS) && hasLink == 1) {

            if (isPreviousDone(menu) || bmodel.configurationMasterHelper.IS_JUMP) {
                // bmodel.productHelper.downloadLoyaltyDescription();

                bmodel.productHelper.downloadloyaltyBenifits();
                bmodel.productHelper.downloadLoyaltyDescription(bmodel.getRetailerMasterBO().getRetailerID());

                if ((bmodel.productHelper.getProductloyalties() != null)
                        && (bmodel.productHelper.getProductloyalties().size() > 0)) {

                    if (bmodel.mLoyalityHelper.hasUpdatedLoyalties(bmodel.getRetailerMasterBO().getRetailerID()))
                        bmodel.mLoyalityHelper.updatedLoyaltyPoints(bmodel.getRetailerMasterBO().getRetailerID());

                    Intent i = new Intent(HomeScreenTwo.this, LoyaltyPointsFragmentActivity.class);
                    i.putExtra("screentitle", menu.getMenuName());
                    bmodel.mSelectedActivityName = menu.getMenuName();
                    startActivity(i);
                    isCreated = true;
                } else {
                    dataNotMapped();
                    isCreated = false;
                }


            }
        } else if (menu.getConfigCode().equals(MENU_RTR_KPI) && hasLink == 1) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP) {
                bmodel.dashBoardHelper.loadRetailerDashBoard(bmodel.getRetailerMasterBO().getRetailerID() + "", "MONTH");

                if (bmodel.dashBoardHelper.getDashChartDataList().size() > 0) {
                    Intent i = new Intent(this,
                            SellerDashBoardActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    Bundle bnd = new Bundle();
                    bnd.putString("screentitle", menu.getMenuName());
                    bnd.putString("retid", bmodel.getRetailerMasterBO().getRetailerID());
                    bnd.putBoolean("isFromHomeScreenTwo", true);
                    bnd.putString("menuCode", menu.getConfigCode());
                    i.putExtras(bnd);
//                    i.putExtra("screentitle", menu.getMenuName());
//                    i.putExtra("retid", bmodel.getRetailerMasterBO().getRetailerID());
//                    i.putExtra("isFromHomeScreenTwo", true);
                    bmodel.mSelectedActivityName = menu.getMenuName();
                    bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                            SDUtil.now(SDUtil.DATE_GLOBAL),
                            SDUtil.now(SDUtil.TIME), menu.getConfigCode());
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

        } else if (menu.getConfigCode().equals(MENU_DASH_ACT) && hasLink == 1) {
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
                        SDUtil.now(SDUtil.DATE_GLOBAL),
                        SDUtil.now(SDUtil.TIME), menu.getConfigCode());
                startActivity(i);
            } else {
                Toast.makeText(
                        this,
                        getResources().getString(
                                R.string.please_complete_previous_activity),
                        Toast.LENGTH_SHORT).show();
                isCreated = false;

            }

        } else if (menu.getConfigCode().equals(MENU_DELIVERY_ORDER) && hasLink == 1) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP) {


                if (bmodel.hasAlreadyOrdered(bmodel.getRetailerMasterBO()
                        .getRetailerID())) {

                    bmodel.loadOrderedProducts(bmodel.getRetailerMasterBO()
                            .getRetailerID(), null);
                    enableSchemeModule();

                    bmodel.mSelectedActivityName = menu.getMenuName();
                    bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                            SDUtil.now(SDUtil.DATE_GLOBAL),
                            SDUtil.now(SDUtil.TIME), menu.getConfigCode());

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

        } else if (menu.getConfigCode().equals(MENU_FIT_DASH) && hasLink == 1) {
            Intent i = new Intent(this,
                    FitScoreDashboardActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            i.putExtra("screentitle", menu.getMenuName());
            i.putExtra("menuCode", menu.getConfigCode());
            startActivity(i);
            finish();
        } else {
            isCreated = false;
        }

    }

    private void loadOrderSummaryScreen(String menuConfigCode) {
        Intent intent = new Intent(HomeScreenTwo.this,
                OrderSummary.class);
        if (menuConfigCode.equals(MENU_CATALOG_ORDER)) {
//                            bmodel.productHelper
//                                    .downloadFiveFilterLevels("MENU_STK_ORD");
            intent.putExtra("ScreenCode", MENU_CATALOG_ORDER);
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
                bmodel.collectionHelper.downloadDiscountSlab();
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
            if (bmodel.configurationMasterHelper.SHOW_STORE_WISE_DISCOUNT_DLG) {
                bmodel.productHelper.downloadBillwiseDiscount();
                bmodel.productHelper.updateRangeWiseBillDiscountFromDB();
            }
            // apply bill wise payterm discount
            bmodel.productHelper.downloadBillwisePaytermDiscount();

            bmodel.productHelper.downloadInStoreLocations();

            if (bmodel.configurationMasterHelper.IS_SCHEME_ON_MASTER)
                bmodel.schemeDetailsMasterHelper.loadSchemeHistoryDetails();

            //  if (bmodel.configurationMasterHelper.IS_SCHEME_ON) {
            bmodel.schemeDetailsMasterHelper.downloadOffInvoiceSchemeDetails();
            // }

            if (bmodel.configurationMasterHelper.SHOW_COLLECTION_BEFORE_INVOICE) {
                bmodel.collectionHelper.downloadBankDetails();
                bmodel.collectionHelper.downloadBranchDetails();
                bmodel.collectionHelper.loadCreditNote();
            }

            bmodel.updateProductUOM(StandardListMasterConstants.mActivityCodeByMenuCode.get(MENU_STK_ORD), 1);


            if (bmodel.configurationMasterHelper.IS_FORMAT_USING_CURRENCY_VALUE) {
                bmodel.downloadCurrencyConfig();
            }

        } catch (Exception e) {
            Commons.printException(e);
        }
        // Reset the Configuration if Directly goes from
        // HomeScreenTwo
        bmodel.mSelectedModule = -1;
        OrderSummary.mActivityCode = configCode;
        bmodel.mSelectedActivityName = menuName;
    }


    public void loadstockorderscreen(String menu) {
        {
            indicativeOrderAdapter = new ArrayAdapter<Integer>(this,
                    android.R.layout.select_dialog_singlechoice);

            for (Integer temp : bmodel.productHelper
                    .getIndicativeList())
                indicativeOrderAdapter.add(temp);
            if (bmodel.configurationMasterHelper.SHOW_INVOICE_CREDIT_BALANCE) {
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
                                    SDUtil.now(SDUtil.DATE_GLOBAL),
                                    SDUtil.now(SDUtil.TIME),
                                    menu);

                    Intent intent = new Intent(HomeScreenTwo.this,
                            StockAndOrder.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                    finish();
                } else {
                    showDialog(1);
                    isCreated = false;
                }
            } else {
                bmodel.outletTimeStampHelper
                        .saveTimeStampModuleWise(
                                SDUtil.now(SDUtil.DATE_GLOBAL),
                                SDUtil.now(SDUtil.TIME),
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
            case 0:
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeScreenTwo.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(
                                getResources()
                                        .getString(
                                                R.string.wits_merchandising_already_done_Do_you_want_do_again))
                        .setPositiveButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        finish();
                                        Intent sbd = new Intent(HomeScreenTwo.this,
                                                MerchandisingActivity.class);
                                        sbd.putExtra("screentitle", bmodel.mSelectedActivityName);
                                        startActivity(sbd);
                                    /* User clicked OK so do some stuff */
                                    }
                                })
                        .setNegativeButton(
                                getResources().getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        isCreated = false;
                                    /* User clicked Cancel so do some stuff */
                                    }
                                });
                bmodel.applyAlertDialogTheme(builder);

                break;
            case 1:
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

                                    }
                                });
                bmodel.applyAlertDialogTheme(builder1);
                break;
            case 2:
                AlertDialog.Builder builder2 = new AlertDialog.Builder(HomeScreenTwo.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle("Select")
                        .setSingleChoiceItems(mSalesTypeArray,
                                bmodel.getRetailerMasterBO().getIsVansales(),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        String selectedType = mSalesTypeArray[which];
                                        if (selectedType.equals(VAN_SALES)) {
                                            updateConfigurationSelectedSellerType(true);
                                            bmodel.configurationMasterHelper.IS_WSIH = false;

                                            updateRetailerwiseSellertype(1); // Vansales
                                            bmodel.getRetailerMasterBO()
                                                    .setIsVansales(1);

                                        } else {
                                            updateConfigurationSelectedSellerType(false);
                                            bmodel.configurationMasterHelper.IS_WSIH = bmodel.configurationMasterHelper.IS_WSIH_MASTER;

                                            updateRetailerwiseSellertype(0); // Presales
                                            bmodel.getRetailerMasterBO()
                                                    .setIsVansales(0);
                                        }
                                        dialog.dismiss();

                                    }
                                });
                bmodel.applyAlertDialogTheme(builder2);
                break;
            case 3:
                AlertDialog.Builder builder3 = new AlertDialog.Builder(HomeScreenTwo.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(getResources().getString(R.string.select_supplier))

                        .setSingleChoiceItems(mSupplierAdapter,
                                mDefaultSupplierSelection,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {

                                        SupplierMasterBO supplierBo = mSupplierAdapter
                                                .getItem(which);

                                        bmodel.getRetailerMasterBO().setSupplierBO(
                                                supplierBo);
                                        bmodel.getRetailerMasterBO().setDistributorId(supplierBo.getSupplierID());
                                        bmodel.getRetailerMasterBO().setDistParentId(supplierBo.getDistParentID());
                                        bmodel.updateRetailerWiseSupplierType(supplierBo
                                                .getSupplierID());
                                        retailerCodeTxt.setText(supplierBo.getSupplierName());
                                        dialog.dismiss();

                                    }
                                });
                bmodel.applyAlertDialogTheme(builder3);
                break;
            case 4:
                AlertDialog.Builder builder4 = new AlertDialog.Builder(HomeScreenTwo.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle("Select")
                        .setSingleChoiceItems(mOrderTypeArray,
                                mOrderTypeCheckedItem,
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        final String typeId = mOrderTypeList.get(which).getListID();
                                        bmodel.getRetailerMasterBO().setOrderTypeId(typeId);
                                        dialog.dismiss();

                                    }
                                });
                bmodel.applyAlertDialogTheme(builder4);
                break;
            case 5:
                AlertDialog.Builder builder5 = new AlertDialog.Builder(HomeScreenTwo.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(getResources().getString(
                                R.string.please_finish_mandatory_modules))
                        .setMessage(getMandatoryModules())
                        .setPositiveButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        dialog.dismiss();

                                    }

                                });

                bmodel.applyAlertDialogTheme(builder5);
                break;


        }

        return null;
    }

    private String getMandatoryModules() {
        StringBuilder sb = new StringBuilder();

        for (ConfigureBO config : menuDB) {
            if (config.getMandatory() == 1 && !config.isDone()
                    && !config.getConfigCode().equals(MENU_CALL_ANLYS)
                    && !config.getConfigCode().equals(MENU_CLOSE_CALL) && !config.getConfigCode().equals(MENU_CLOSE_KLGS)) {

                sb.append(config.getMenuName() + " "
                        + getResources().getString(R.string.is_not_done) + "\n");
            }

        }

        if (isStoreCheckMenu) {
            for (ConfigureBO config : mInStoreMenu) {
                if (config.getMandatory() == 1 && !config.isDone()
                        && !config.getConfigCode().equals("MENU_CLOSE")) {

                    sb.append(config.getMenuName() + " "
                            + getResources().getString(R.string.is_not_done) + "\n");
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

            DBUtil db = new DBUtil(HomeScreenTwo.this, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
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
     */
    private void enableSchemeModule() {
        if (bmodel.configurationMasterHelper.IS_SCHEME_ON) {
            if (bmodel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG) {
                if (bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {

                    bmodel.schemeDetailsMasterHelper.loadSchemeDetails(bmodel
                            .getRetailerMasterBO().getRetailerID());
                }
            } else {
                bmodel.schemeDetailsMasterHelper.loadSchemeDetails(bmodel
                        .getRetailerMasterBO().getRetailerID());
            }
        }
    }

    public void dataNotMapped() {
        bmodel.showAlert(
                getResources().getString(R.string.data_not_mapped),
                0);
    }

    /**
     * Method to use change some specify configuration flag depends on selected
     * seller type
     *
     * @param flag
     */
    private void updateConfigurationSelectedSellerType(boolean flag) {
        if (!flag) {
            bmodel.configurationMasterHelper.IS_SIH_VALIDATION = flag;
            bmodel.configurationMasterHelper.IS_STOCK_IN_HAND = flag;
            bmodel.configurationMasterHelper.IS_SCHEME_ON = flag;
            bmodel.configurationMasterHelper.IS_SCHEME_SHOW_SCREEN = flag;
            bmodel.configurationMasterHelper.SHOW_TAX = flag;
            bmodel.configurationMasterHelper.IS_GST = flag;
            bmodel.configurationMasterHelper.SHOW_STORE_WISE_DISCOUNT_DLG = flag;
            bmodel.configurationMasterHelper.SHOW_TOTAL_DISCOUNT_EDITTEXT = flag;
//            bmodel.configurationMasterHelper.SHOW_DISCOUNT = flag;
        } else {
            bmodel.configurationMasterHelper.IS_SIH_VALIDATION = bmodel.configurationMasterHelper.IS_SIH_VALIDATION_MASTER;
            bmodel.configurationMasterHelper.IS_STOCK_IN_HAND = bmodel.configurationMasterHelper.IS_STOCK_IN_HAND_MASTER;
            bmodel.configurationMasterHelper.IS_SCHEME_ON = bmodel.configurationMasterHelper.IS_SCHEME_ON_MASTER;
            bmodel.configurationMasterHelper.IS_SCHEME_SHOW_SCREEN = bmodel.configurationMasterHelper.IS_SCHEME_SHOW_SCREEN_MASTER;

            bmodel.configurationMasterHelper.SHOW_TAX = bmodel.configurationMasterHelper.SHOW_TAX_MASTER;
        }

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
                holder.icon_ll.setBackgroundResource(R.drawable.activity_icon_bg_completed);
                holder.iconIV.setColorFilter(Color.argb(255, 255, 255, 255));

            } else {
                holder.icon_ll.setBackgroundResource(R.drawable.activity_icon_bg_normal);
                holder.iconIV.setColorFilter(Color.argb(0, 0, 0, 0));
            }

            itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(getApplicationContext(),position+"",Toast.LENGTH_SHORT).show();
                    if (!isCreated) {
                        isCreated = true;
                        if (holder.config.getConfigCode().equals(MENU_STORECHECK)) {
                            isCreated = false;

                            if (holder.childListView.getVisibility() == View.GONE) {
                                holder.img_arrow.setImageResource(R.drawable.activity_icon_close);
                                isInstoreMenuVisible = true;

                                holder.childListView.setLayoutParams(getListLayoutParam(holder.childListView));
                                holder.childListView.setAdapter(mSchedule);
                                holder.childListView.setVisibility(View.VISIBLE);

                                //Animation anim = AnimationUtils.loadAnimation(HomeScreenTwo.this, R.anim.view_show);
                                //holder.childListView.startAnimation(anim);
                            } else {
                                holder.img_arrow.setImageResource(R.drawable.activity_icon_next);
                                isInstoreMenuVisible = false;

                                holder.childListView.setVisibility(View.GONE);
                            }
                        } else {
                            gotoNextActivity(holder.config, holder.hasLink, false);
                        }
                    }
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
                iconIV = (ImageView) view.findViewById(R.id.list_item_icon_iv);
                icon_ll = (LinearLayout) view.findViewById(R.id.icon_ll);
                img_arrow = (ImageView) view.findViewById(R.id.img_arrow);
                activityname = (TextView) view.findViewById(R.id.activityName);
                childListView = (ListView) view.findViewById(R.id.childList);
                activityname.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
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

                holder.activity_icon_circle = (ImageView) convertView
                        .findViewById(R.id.circle);

                holder.activity_icon_top_line = (ImageView) convertView
                        .findViewById(R.id.top_line);

                holder.activity_icon_bottom_line = (ImageView) convertView
                        .findViewById(R.id.bottom_line);

                holder.activityname = (TextView) convertView
                        .findViewById(R.id.activityName);

                holder.activityname.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

                convertView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (!isCreated) {
                            isCreated = true;
                            gotoNextActivity(holder.config, holder.hasLink, true);
                        }
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

                builder.setSpan(new ForegroundColorSpan(Color.WHITE), start, middle
                        ,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.activityname.setText(builder.toString());
            } else {
                holder.activityname.setText(configTemp.getMenuName());
            }

            if (holder.config.isDone()) {
                holder.activity_icon_circle.setColorFilter(ContextCompat.getColor(getBaseContext(), R.color.white));
            } else {
                holder.activity_icon_circle.setColorFilter(ContextCompat.getColor(getBaseContext(), R.color.black_bg1));
            }

            if (position == 0) {
                holder.activity_icon_top_line.setVisibility(View.INVISIBLE);
            } else if (position == getCount() - 1) {
                holder.activity_icon_bottom_line.setVisibility(View.INVISIBLE);
            } else {
                holder.activity_icon_top_line.setVisibility(View.VISIBLE);
                holder.activity_icon_bottom_line.setVisibility(View.VISIBLE);
            }

            return convertView;
        }

        class ViewHolder {
            ConfigureBO config;
            String menuCode;
            int position;
            TextView activityname;
            int hasLink;
            ImageView activity_icon_circle, activity_icon_top_line, activity_icon_bottom_line;
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
        for (int i = 0; i < size; i++) {
            int flag = profileConfig.get(i).isFlag();
            String configCode = profileConfig.get(i).getConfigCode();
            if (configCode.equals("PROFILE60") && flag == 1) {
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
                }
                break;
            }
        }
    }

    private void setImageFromCamera(RetailerMasterBO retailerObj) {
        try {
            String[] imgPaths = retailerObj.getProfileImagePath().split("/");
            String path = imgPaths[imgPaths.length - 1];
            Uri uri = bmodel.profilehelper.getUriFromFile(HomeScreenFragment.photoPath + "/" + path);
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
        Bitmap myBitmap = bmodel.decodeFile(imgFile);
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
        if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER)
            bmodel.productHelper
                    .downloadFiveLevelFilterNonProducts(menuCode);
        else
            bmodel.productHelper
                    .downloadProductFilter(menuCode);
    }


}
