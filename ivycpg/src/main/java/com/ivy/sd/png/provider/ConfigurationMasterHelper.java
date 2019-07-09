package com.ivy.sd.png.provider;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.preference.PreferenceManager;

import com.ivy.core.data.user.UserDataManagerImpl;
import com.ivy.cpg.view.order.catalog.CatalogOrder;
import com.ivy.cpg.view.order.scheme.SchemeDetailsMasterHelper;
import com.ivy.lib.existing.DBUtil;
import com.ivy.location.LocationUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.PasswordPolicyBO;
import com.ivy.sd.png.bo.VisitConfiguration;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.ApplicationConfigs;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.FontUtils;
import com.ivy.utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

public class ConfigurationMasterHelper {

    /**
     * Menu code List *
     */
    public static final String MENU_ORDER = "MENU_ORDER";
    public static final String MENU_STOCK = "MENU_STOCK";
    public static final String MENU_ACTIVITY = "ACT_MENU";
    public static final String MENU_SUBD = "SUBD_MENU";
    public static final String MENU_PRIMARY_SALES = "MENU_PRIMARY_SALES";
    public static final String MENU_STORECHECK = "MENU_STORECHECK";
    public int VALUE_SYNC_PROGRESS_TIME = 10000;

    public static int vanDistance = 0;
    public static String ACCESS_KEY_ID = "AKIAI5OG2UQYXDPYQNNQ";
    public static String SECRET_KEY = "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";
    public static final String CODE_SHOW_DATE_BTN = "PHOTOCAP02";
    public static final String CODE_DIGITAL_CONTENT = "DIGCON";
    public static final String CODE_NEW_OUTLET_UPLOAD = "FUN17";
    public static final String CODE_MRP_LEVEL_TAX = "TAX01";
    public static final String CODE_TAX_APPLY = "FUN19";
    public static final String CODE_DISCOUNT_APPLY = "FUN18";
    // Added for five level filter
    public static final String CODE_GLOBAL_LOCATION = "FUN23";
    public static final String CODE_GLOBAL_CATEGORY = "FUN24";
    public static final String CODE_SHOW_OUTLET_PLANNING_TAB = "PRO11";
    public static final String CODE_PROFILE_LOC1 = "PROFILE13";
    public static final String CODE_PROFILE_LOC2 = "PROFILE14";
    public static final String CODE_PROFILE_LOC3 = "PROFILE15";
    public static final String CODE_SHOW_RETAILER_LOCATION = "RTRS26";
    public static final String CODE_SHOW_ALL_ROUTE_FILTER = "RTRS27";
    public static final String CODE_SHOW_MISSED_RETAILER = "RTRS28";
    public static final String CODE_VALIDATE_TRADE_COVERAGE = "RTRS29";
    public static final String CODE_SUBD_RETIALER_SELECTION = "RTRS30";
    public static final String CODE_SIMPLE_RETAIER_ROW = "RTRS31";
    public static final String CODE_RETAILER_CONTACT_NAME = "RTRS32";
    public static final String CODE_CONTRACT_TYPE = "PROFILE17";
    public static final String CODE_CONTRACT_EXPIRYDATE = "PROFILE18";
    public static final String CODE_VISIT_FREQUENCY = "PROFILE19";

    public static final String CODE_PRODUCT_FILTER_IN_SURVEY = "SURVEY08";
    public static final String CODE_QDVP3_SCORE_CARD_TAB = "PRO18";
    public static final String CODE_OUTLET_PROGRAM = "PROFILE28";
    public static final String CODE_RE_CLASSIFICATION = "PROFILE29";
    public static final String CODE_CONTACT_TITLE_LOVID1 = "PROFILE41";
    public static final String CODE_CONTACT_TITLE_LOVID2 = "PROFILE42";
    public static final String CODE_UPDATE_SIH = "SR05";
    public static final String CODE_RETAILER_FREQUENCY = "RTRS06";
    public static final String CODE_DAY_REPORT_PRINT = "RPT02";
    public static final String CODE_CAMERA_PICTURE_SIZE = "PHOTOCAP04";
    // Password policy
    public static final String CODE_PSWD_MIN_LEN = "MIN_LEN";
    public static final String CODE_PSWD_MAX_LEN = "MAX_LEN";
    public static final String CODE_PSWD_EXPIRY = "EXPIRY";
    public static final String CODE_SAME_LOGIN = "SAME_LOGIN";
    public static final String CODE_PSWD_CHARACTERS = "CHARS";
    // manuval vanload
    public static final String CODE_IS_BATCHWISE_VANLOAD = "BATCHVAN";
    public static final String CODE_VANLOAD_STOCK_PRINT = "STKPRO21";
    // version 90
    public static final String CODE_LOCAITON_WISE_TAX_APPLIED = "TAX02";

    public static final String CODE_REMOVE_TAX_ON_PRICE_FOR_ALL_PRODUCTS = "TAX03";
    public boolean IS_REMOVE_TAX_ON_PRICE_FOR_ALL_PRODUCTS = false;

    private static final String MENU_CALL_ANALYSIS = "MENU_CALL_ANLYS";
    private static final String MENU_LOAD_MANAGEMENT = "MENU_LOAD_MANAGEMENT";
    private static final String MENU_HOME = "HOME_MENU";
    private static final String MENU_PLANNING_SUB = "MENU_PLANNING_SUB";
    // Added in 43 version
    // Image upload through Amazon or on Premises
    private static final String CODE_CLOUD_STORAGE = "CLOUD_STORAGE";
    public boolean IS_CLOUD_STORAGE_AVAILABLE;
    public boolean IS_SFDC_CLOUD_STORAGE;
    public boolean IS_S3_CLOUD_STORAGE = true;// default storage.
    public boolean IS_AZURE_CLOUD_STORAGE;

    // Show Tax in Invoice
    private static final String SHOW_TAX_INVOICE = "TAXINPRINT";
    //Is Survey Global Save
    private static final String SURVEY_GLOBAL_SAVE = "SURVEY01";
    /**
     * Configuration Code List *
     */
    private static final String CODE_NEW_TASK = "NTSK";
    private static final String CODE_SUGGESTED_ORDER = "ORDB04";
    private static final String CODE_STOCK_IN_HAND = "ORDB05";
    private static final String CODE_INVOICE = "ORDB08";
    private static final String CODE_RETAILER_DEVIATION = "RTRS01";
    private static final String CODE_MAP = "RTRS05";
    private static final String CODE_BAIDU_MAP = "FUN26";
    private static final String CODE_DELIVERY_REPORT = "ORDB09";

    private static final String CODE_PHOTO_CAPTURE = "PHOTOCAP";
    private static final String CODE_PHOTO_CAPTURE_IMG_PATH = "PHOTOCAP05";
    private static final String CODE_PHOTO_COMPETITOR = "PHOTOCOMP";
    private static final String CODE_TASK = "TSK";
    private static final String CODE_JUMP = "JUMPING";

    private static final String CODE_VOLUME_COMMA_COUNT = "VolComma";
    private static final String CODE_VOLUME_PRECISION_COUNT = "VolDecimal";
    private static final String CODE_PERCENT_PRECISION_COUNT = "PerDecimal";
    private static final String CODE_CALCULATION_PRECISION_COUNT = "CalcDecimal";
    public int PRECISION_COUNT_FOR_CALCULATION = 3;
    public int VALUE_PRECISION_COUNT = 2;
    public int VALUE_COMMA_COUNT = 0;
    public int PERCENT_PRECISION_COUNT = 0;


    private static final String CODE_VISITSCREEN_DEV_ALLOW = "RTRS02";
    private static final String CODE_DAY_MISMATCH = "RTRS03";
    private static final String CODE_INITIATIVE = "ORDB01";
    private static final String CODE_PRASENTATION_INORDER = "ORDB02";
    private static final String CODE_SHOW_ALL_ROUTES = "RTRS07";
    private static final String CODE_RETAILER_VISIT_CONFIRMATION = "RTRS08";
    private static final String CODE_HAS_NO_VISIT_REASON_VALIDATION = "VLD01";
    private static final String CODE_HAS_PROFILE_BUTTON_IN_RETAILER_LIST = "PRO01";
    private static final String CODE_SHOW_MONTH_OBJ_PROFILE = "PROFILE16";
    private static final String CODE_SHOW_CREDIT_LIMIT_PROFILE = "PROFILE19";
    private static final String CODE_SHOW_NO_VISIT_REASON = "PROFILE21";
    private static final String CODE_SHOW_ORDER_HISTORY = "PRO05";
    private static final String CODE_SHOW_TASK = "PRO08";
    public static final String CODE_SHOW_AVG_SALES_PER_LEVEL = "PRO09";
    //

    public static final String CODE_SHOW_SALES_VALUE_DR = "PRO30";

    private static final String CODE_SHOW_ASSET_HISTORY = "PRO07";
    private static final String CODE_SHOW_EDIT_PRO = "PRO21";
    private static final String COBE_DB_BACKUP = "SQLBACKUP";// code for DB backup
    private static final String CODE_GPS_ENABLE = "GPSENABLE"; // Code for GPS enabled
    private static final String CODE_SHOW_LPC_ORDER = "ORDB11";


    private static final String CODE_SHOW_INIT_FOOTER = "ORDB13";
    private static final String CODE_SHOW_REVIEW_PO = "ORDB14";
    private static final String CODE_IS_WSIH = "ORDB16"; // ORDB16
    private static final String CODE_SHOW_HIGHLIGHT_FOR_OOS = "ORDB15"; // ORDB15
    private static final String CODE_SHOW_PRODUCT_DISCOUNT_DIALOG = "FUN02"; // FUN02
    private static final String CODE_SHOW_DISCOUNT_ACTIVITY = "FUN03";
    // code added in v 33
    private static final String CODE_SHOW_CREDIT_BALANCE = "PROFILE20";//
    private static final String CODE_SHOW_CREDIT_DAYS = "PROFILE21";//
    private static final String CODE_SHOW_MAX_OUTSTANDING = "PROFILE22";//

    //private static final String CODE_INITIATIVE_MERCHANDISING = "VLD02";
    private static final String CODE_SUGGESTED_ORDER_LOGIC = "ORDB18";
    // code added in v 35

    private static final String CODE_CALCULATOR = "FUN04";

    private static final String CODE_BIXOLONI = "PRINT01";
    private static final String CODE_BIXOLONII = "PRINT02";
    private static final String CODE_ZEBRA = "PRINT03";
    private static final String CODE_PRINT_COUNT = "VLD03";
    private static final String CODE_DATE_FORMAT = "DATEFOR";
    // code added in v 37
    private static final String CODE_MUST_SELL = "MSL";
    private static final String CODE_MUST_SELL_REASON = "MSL_REASON";
    private static final String CODE_MUST_SELL_SKIP = "MSL_SKIP";
    private static final String CODE_PRODUCT_SCHEME_DIALOG = "ORDB21";
    private static final String CODE_DISCOUNT_EDITVIEW = "ORDB25";
    private static final String CODE_SHOW_STK_ORD_SRP = "ORDB26";
    private static final String CODE_SHOW_SPL_FILTER = "ORDB27";
    private static final String CODE_SHOW_COMPETITOR_FILTER = "FUN62";
    public static String COMPETITOR_FILTER_LEVELS;
    private static final String CODE_SHOW_MVP_DRAWER = "MVP01";
    private static final String CODE_LAT = "PROFILE08";
    private static final String CODE_LONG = "PROFILE31";
    // code added in v 38
    private static final String CODE_SHOW_REMARKS_STK_ORD = "REM1";
    private static final String CODE_SHOW_REMARKS_STK_CHK = "REM2";

    private static final String CODE_SHOW_DASH_HOME = "HASDASH";
    private static final String CODE_SHOW_LOCATION_PWD_DIALOG = "GPSVAL";
    private static final String CODE_SHOW_CHART_DASH = "DASH02";
    private static final String CODE_IS_MUST_STOCK = "STKPRO1";
    private static final String CODE_DISABLE_MANUAL_ENTRY = "STKPRO2";
    private static final String CODE_MUST_STOCK_ONLY = "STKPRO3";
    private static final String CODE_STOCK_SO_APPLY = "STKPRO5";
    private static final String CODE_STOCK_STDQTY_APPLY = "STKPRO6";
    private static final String CODE_STOCK_MAX_LIMIT = "STKPRO7";
    // code added in v 39
    private static final String CODE_SAL_RET_REASON_DLG = "SR02";
    private static final String CODE_STOCK_APPROVAL = "STKPRO8";
    private static final String CODE_END_JOURNEY = "STKPRO9";
    private static final String CODE_RETAILER_SELECTION_VALID = "STKPRO10";


    // Added in 39 versing
    private static final String CODE_CLOSE_DAY_VALID = "STKPRO11";
    private static final String CODE_BATCH_ALLOCATION = "ORDB29";
    private static final String CODE_VANGPS_VALIDATION = "VGPSVAL";
    private static final String CODE_RET_SKIP_VALIDATION = "RSEQVAL";
    private static final String CODE_INV_CREDIT_BALANCE = "CREDIT01";
    public boolean IS_SUPPLIER_CREDIT_LIMIT = false;
    public boolean IS_CREDIT_LIMIT_WITH_SOFT_ALERT = false;
    private static final String CODE_POST_DATE_ALLOW = "COLL01";
    private static final String CODE_DELIVERY_DATE = "ORDB30";
    //private static final String CODE_ALLOW_DECIMAL = "ORDB31";
    // NewOutlet
    private static final String CODE_NEWOUTLET_LOCATION = "OUTLOC";
    private static final String CODE_NEWOUTLET_IMAGETYPE = "OUTIMGTYPE";
    private static final String CODE_NEWOUTLET_MODULES = "OUTMOD";

    private static final String CODE_ZEBRA_ATS = "PRINT04";
    private static final String CODE_INTERMEC_ATS = "PRINT05";
    private static final String CODE_ZEBRA_DIAGEO = "PRINT06";
    private static final String CODE_ZEBRA_GHANA = "PRINT08";
    private static final String CODE_ZEBRA_UNIPAL = "PRINT09";
    private static final String CODE_ZEBRA_TITAN = "PRINT10";
    private static final String CODE_DISC_AMOUNT_ALLOW = "COLL02";


    private static final String CODE_PRIMARY_CONTACT_NAME = "PROFILE09";
    private static final String CODE_PRIMARY_CONTACT_NUMBER = "PROFILE10";
    private static final String CODE_SECONDARY_CONTACT_NAME = "PROFILE11";
    private static final String CODE_SECONDARY_CONTACT_NUMBER = "PROFILE12";
    private static final String CODE_BATCH_WISE_PRODUCT = "BWP01";
    private static final String CODE_SIGNATURE_SCREEN = "ORDB03";
    private static final String CODE_FULL_PAYMENT = "COLL03";
    private static final String CODE_COLLECTION_ORDER = "COLL04";
    private static final String CODE_COLLECTION_REASON = "COLL05";
    private static final String CODE_DEVICE_STATUS = "FUN05";
    private static final String CODE_STKPRO_SPL_FILTER = "STKPRO12";
    private static final String CODE_TOTAL_LINES = "ORDB32";
    // Added in V40
    private static final String CODE_SO_SPLIT = "SOSPLIT";
    private static final String CODE_SO_COPY = "SOCOPY";


    // Added in V41
    private static final String CODE_SHOW_MULTIPAYMENT = "COLL06";
    private static final String CODE_PHOTO_CAPTURE_COUNT = "PHOTOCAP01";
    private static final String CODE_ROAD_ACTIVITY_PHOTO_COUNT = "PHOTOCAP03";
    private static final String CODE_DOWNLOAD_ALERT = "SYNC01";
    private static final String CODE_DELETE_TABLE_ADHOC = "SYNC10";
    private static final String CODE_SHOW_SIH_IN_FNAME = "ORDB34";
    // Added in 42 version
    private static final String CODE_SHOW_SYNC_RETAILER_SELECT = "SYNC02";
    private static final String CODE_CURRENT_STDQTY_APPLY = "STKPRO14";
    private static final String CODE_VALIDATE_DIST_INV = "STKPRO15";
    private static final String CODE_SHOW_SYNC_EXPORT_TXT = "SYNC03";
    private static final String CODE_SHOW_SYNC_DAYCLOSE = "SYNC07";

    private static final String CODE_SHOW_OBJECTIVE = "ORDB35";
    private static final String CODE_REMOVE_INVOICE = "ORDB36";
    // Added in 43 version
    private static final String CODE_SHOW_DEVIATION = "RTRS24";
    private static final String CODE_SHOW_COLLECTION_SLAB = "COLL09";
    private static final String CODE_CREDIT_NOTE_CREATION = "SR03";
    private static final String CODE_MULTIPLE_JOINCALL = "JOINTCALL01";
    private static final String CODE_ALLOW_SURVEY = "JOINTCALL02";
    private static final String CODE_JOINT_CALL_LEVELS = "JOINTCALL03";
    private static final String CODE_SIH_VALIDATION = "ORDB42";
    private static final String CODE_SHOW_SELLER_DIALOG = "PREVAN01";
    private static final String CODE_CHNAGE_SELLER_CONFIG_LEVEL = "PREVAN02";
    private static final String CODE_SHOW_VALIDATE_CREDIT_DAYS = "CREDITDAY01";
    private static final String CODE_SHOW_LINK_DASH_SKUTGT = "DASH03";

    private static final String CODE_ADD_NEW_BATCH = "NEWBATCH01";
    private static final String CODE_SHOW_PRODUCT_CODE = "PRDCODE"; // enable product
    private static final String CODE_HIDE_ORDER_DIST = "ORDB22";  // Hide Dist Value in StockAndOrder Screen
    private static final String CODE_IS_TAX_APPLIED_VALIDATION = "ORDB23";  // Allow save Invoice only when Tax is applied
    private static final String CODE_STORE_WISE_DISCOUNT_DIALOG = "FUN07";
    private static final String CODE_STOCK_PRO_CREDIT_VALIDATION = "STKPRO16";
    private static final String CODE_DEVIATE_STORE_SCHEME_NOT_APPLY = "ORDB37";
    private static final String CODE_SALES_RETURN_IN_INVOICE = "SR04";
    //private static final String CODE_VANBARCODE_VALIDATION = "VBARCODEVAL";
    private static final String CODE_CREDIT_INVOICE_COUNT = "PROFILE23";

    private static final String CODE_HIDE_STOCK_APPLY_BUTTON = "STKPRO17";
    private static final String CODE_SHOW_PRODUCT_RETRUN = "ORDB41";
    private static final String CODE_SHOW_GROUPPRODUCT_RETRUN = "ORDB43";
    private static final String CODE_SHOW_CROWN = "ORDB44";
    private static final String CODE_SHOW_FREE_PRODUCT_GIVEN = "ORDB45";
    private static final String CODE_SHOW_BOTTLE_CREDITLIMIT = "CREDIT02";// CREDIT02
    private static final String CODE_APPLY_GOLD_STORE_DISCOUNT = "FUN10";
    private static final String CODE_SHOW_UNIT_PRICE = "STKPRO18";


    private static final String CODE_ADVANCE_PAYMENT = "COLL07";
    private static final String CODE_SHOW_PRINT = "COLL08";
    private static final String CODE_SHOW_PRINTRPT01 = "PRINTRPT01";
    private static final String CODE_SHOW_SUB_DEPOT = "STKPRO19";
    private static final String CODE_CALCULATE_UNLOAD = "STKPRO20";
    private static final String CODE_SHOW_SUPPLIER_SELECTION = "FUN11";
    private static final String CODE_SHOW_USER_TASK = "FUN12";
    private static final String CODE_SHOW_JOINT_CALL = "FUN13";
    private static final String CODE_NEAREXPIRY_IN_STOCKCHECK = "ORDB46";
    private static final String CODE_VANLOAD_LABELS = "VANLOAD01";
    private static final String CODE_SHOW_COLLECTION_BEFOREINVOICE = "COLL10";
    private static final String CODE_CAPTURE_LOCATION = "FUN14"; // Global GPS config
    private static final String CODE_SHOW_DGTC = "FUN16";
    private static final String CODE_INVOICE_SEQUENCE_NUMBER = "ORDB48";
    private static final String CODE_ORDER_SEQUENCE_NUMBER = "ORDB78";
    private static final String CODE_LOAD_NON_SALABLE_PRODUCTS = "ORDB79";
    private static final String CODE_SHOW_STOCK_IN_SUMMARY = "ORDB49";
    private static final String CODE_IS_TEAMLEAD = "ISTEAMLEAD"; // Code to validRegex whether the user is teamlead or not
    private static final String CODE_UPLOADUSERLOC = "UPLOADUSERLOC";
    // Time for alarm wake up
    private static final String CODE_ALARM_TIME = "ALARMTIME";
    // Start Time
    private static final String CODE_START_TIME = "STARTTIME";
    // End Time
    private static final String CODE_END_TIME = "ENDTIME";
    private static final String CODE_DROPSIZE = "DROPSIZE";
    private static final String CODE_STOCK_COMPETITOR = "STOCK_COMPETITOR";

    private static final String CODE_SIH_SPLIT = "SIHSPLIT";

    private static final String CODE_SHOW_REJECT_BTN = "STK_REJECT";
    private static final String CODE_VALIDATE_NEGATIVE_INVOICE = "ORDB47";
    private static final String CODE_SHOW_GCM_NOTIFICATION = "FUN15";
    private static final String CODE_SHOW_PREV_ORDER_REPORT = "RPT01";
    private static final String CODE_SHOW_INDICATIVE_ORDER_ICON = "RTRS25";
    private static final String CODE_SHOW_MAX_NO_PRODUCT_LINES = "FUN25";
    private static final String CODE_SHOW_ORDER_TYPE_DIALOG = "ORDB39";
    private static final String CODE_SHOW_LAST_3MONTHS_SALES = "PRO22";
    private static final String CODE_MSL_NOT_SOLD = "PRO24";
    private static final String CODE_RETAILER_CONTACT = "PRO28";
    private static final String CODE_NORMAL_DASHBOARD = "DASH13";
    private static final String CODE_SHOW_NEARBY_RETAILER_MAX = "NEARBYMAX";

    private static final String CODE_MAX_MIN_DATE_CHEQUE = "MIN_MAX_CHQ_DATE";
    public boolean IS_ENABLE_MIN_MAX_DATE_CHQ = false;
    private static final String CODE_ACC_NO_CHEQUE = "IS_ACC_NO_CHQ";
    public boolean IS_ENABLE_ACC_NO_CHQ = false;
    public int CHQ_MIN_DATE = 30;
    public int CHQ_MAX_DATE = 0;

    private static final String CODE_ENABLE_PRODUCT_TAGGING_VALIDATION = "TAGG01";
    public boolean IS_ENABLE_PRODUCT_TAGGING_VALIDATION = false;

    private static final String CODE_ENABLE_LAST_VISIT_HISTORY = "CSSTK06";
    public boolean IS_ENABLE_LAST_VISIT_HISTORY = false;

    public boolean IS_NEARBY_RETAILER = false;
    public int VALUE_NEARBY_RETAILER_MAX = 1;
    private static final String CODE_IS_AUDIT_USER = "ISAUDITUSER";
    public boolean IS_AUDIT_USER = false;
    private static final String CODE_IN_OUT_MANDATE = "Attendance01";
    public boolean IS_IN_OUT_MANDATE = false;
    private static final String CODE_IS_ADHOC = "RTRS11";
    public boolean IS_ADHOC = false;

    private static final String CODE_IS_SYNC_FROM_CALL_ANALYSIS = "FUN68";
    public boolean IS_SYNC_FROM_CALL_ANALYSIS = false;

    private static final String CODE_SHOW_FOCUSBRAND_COUNT_IN_REPORT = "ORDB52";
    public boolean IS_FOCUSBRAND_COUNT_IN_REPORT = false;
    private static final String CODE_SHOW_MUSTSELL_COUNT_IN_REPORT = "ORDB53";
    public boolean IS_MUSTSELL_COUNT_IN_REPORT = false;

    private static final String CODE_SHOW_START_TIME = "FUN32";
    public boolean IS_SHOW_START_TIME = false;
    private static final String CODE_SHOW_PPQ = "ORDB56";
    public boolean IS_SHOW_PSQ = false;
    private static final String CODE_SHOW_PSQ = "ORDB57";
    public boolean IS_SHOW_PPQ = false;
    private static final String CODE_COMMON_PRINT_ZEBRA = "PRINT101";
    public boolean COMMON_PRINT_ZEBRA;
    private static final String CODE_COMMON_PRINT_BIXOLON = "PRINT102";
    public boolean COMMON_PRINT_BIXOLON;
    private static final String CODE_COMMON_PRINT_SCRYBE = "PRINT103";
    public boolean COMMON_PRINT_SCRYBE;
    private static final String CODE_COMMON_PRINT_LOGON = "PRINT104";
    public boolean COMMON_PRINT_LOGON;
    private static final String CODE_COMMON_PRINT_MAESTROS = "PRINT105";
    public boolean COMMON_PRINT_MAESTROS;
    private static final String CODE_COMMON_PRINT_INTERMEC = "PRINT106";
    public boolean COMMON_PRINT_INTERMEC;

    private static final String CODE_FIT_SCORE = "FITDASH";
    public boolean IS_FITSCORE_NEEDED;

    public boolean SHOW_LAST_3MONTHS_BILLS, SHOW_MSL_NOT_SOLD, SHOW_NOR_DASHBOARD, SHOW_RETAILER_CONTACT;
    private static final String CODE_SHOW_COLLECTION_PRINT = "COLL12";
    public boolean SHOW_COLLECTION_PRINT;
    public int MAX_NO_OF_PRODUCT_LINES = 1;
    private static ConfigurationMasterHelper instance = null;

    private static final String CODE_DISCOUNT_FOR_UNPRICED_PRODUCTS = "FUN31";
    public boolean IS_DISCOUNT_FOR_UNPRICED_PRODUCTS;

    private static final String CODE_SHOW_ORDER_FOCUS_COUNT = "ORDB54"; //Focus count display
    private static final String CODE_SHOW_MENU_COUNTER_ALERT = "FUN28"; //Attendance - Show Counter

    private static final String CODE_SHOW_CHANNEL_SELECTION_NEW_RETAILER = "FUN36";
    public boolean IS_CHANNEL_SELECTION_NEW_RETAILER;

    private static final String CODE_SHOW_NO_ORDER_REASON = "FUN38";
    public boolean SHOW_NO_ORDER_REASON;


    private static final String CODE_CHAT = "CHAT01";
    public boolean IS_CHAT_ENABLED;

    private static final String CODE_SHOW_DISCOUNTS_ORDER_SUMMMARY = "ORDB58";
    public boolean IS_SHOW_DISCOUNTS_ORDER_SUMMARY;

    //Loyalty Points
    public static final String CODE_LOYALTY_AUTO_PAYOUT = "LOYALTY_AUTO_PAYOUT";
    public boolean IS_LOYALTY_AUTO_PAYOUT;

    private static final String CODE_PRINT_LANGUAGE_THAI = "FUN08";
    public boolean IS_SHOW_PRINT_LANGUAGE_THAI;

    public static final String CODE_LOAD_PRICE_GROUP_PRD_OLY = "FUN33";
    public boolean IS_LOAD_PRICE_GROUP_PRD_OLY;

    public static final String CODE_SIH_VALIDATION_ON_DELIVERY = "FUN44";
    public boolean IS_SIH_VALIDATION_ON_DELIVERY;

    public static final String CODE_SHOW_ONLY_INDICATIVE_ORDER = "FUN45";
    public boolean IS_SHOW_ONLY_INDICATIVE_ORDER;
    public boolean IS_SHOW_ORDER_REASON;

    private static final String CODE_SEND_EMAIL_STATEMENT_FOR_DELIVERY = "FUN09";
    public boolean IS_SEND_EMAIL_STATEMENT_FOR_DELIVERY;

    private static final String CODE_ATTRIBUTE_MENU = "HHT_CRITERIA_TYPE";
    public boolean IS_ATTRIBUTE_MENU;
    private static final String CODE_BACKDATE_DAYS = "MAX_BACKDATE_PERIOD";
    public int MAXIMUM_BACKDATE_DAYS = 0;

    private static final String CODE_USER_CAN_SELECT_BILL_WISE_DISCOUNT = "ORDB60";
    public boolean IS_USER_CAN_SELECT_BILL_WISE_DISCOUNT;

    private static final String CODE_PRODCUT_SEQ_UNIPAL = "ORDB63";
    public boolean IS_PRODUCT_SEQUENCE_UNIPAL;

    private static final String CODE_PROFILE_IMAGE = "PROFILE60";
    public boolean IS_PROFILE_IMAGE;

    public static final String CODE_MULTI_STOCKORDER = "FUN59";//replace later
    public boolean IS_MULTI_STOCKORDER;

    private static final String CODE_CATALOG_PRD_IMAGES = "AMAZONPRDIMG";
    public boolean IS_CATALOG_IMG_DOWNLOAD;

    private static final String CODE_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = "FUN57";
    public boolean IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE;


    private static final String CODE_TEMP_ORDER_DETAILS = "FUN60";
    public boolean IS_TEMP_ORDER_SAVE;

    private static final String CODE_ORDER_FILTER_TOP = "FUN61";
    public boolean IS_TOP_ORDER_FILTER;

    private static final String CODE_SHOW_ONLY_SERVER_TASK = "FUN63";
    public boolean IS_SHOW_ONLY_SERVER_TASK;

    private static final String CODE_ORDER_RPT_CONFIG = "ORDRPT02";
    public boolean SHOW_DELIVERY_DATE_IN_ORDER_RPT;

    private static final String CODE_SHOW_RID_CONCEDER_AS_DSTID = "FUN64";
    public boolean IS_SHOW_RID_CONCEDER_AS_DSTID;

    public static final String CODE_COMPETITOR = "COMP01";//change Code value
    private boolean LOAD_COMP_CONFIGS;
    public boolean SHOW_TIME_VIEW;
    public boolean SHOW_SPINNER;
    public boolean SHOW_COMP_QTY;
    public boolean SHOW_COMP_FEEDBACK;

    private static final String CODE_SHOW_JOINT_CALL_REMARKS = "JC_REMARK";
    public boolean IS_SHOW_JOINT_CALL_REMARKS;

    private static final String CODE_MOQ_ENABLED = "FUN66";//change config code
    public boolean IS_MOQ_ENABLED;

    private static final String CODE_ALLOW_CONTINUOUS_PRINT = "FUN67";
    public boolean IS_ALLOW_CONTINUOUS_PRINT;

    private static final String CODE_PRINT_DELIVERY = "DLRYPRINT";
    public boolean IS_DELIVERY_PRINT;

    private static final String CODE_FOCUS_PACK_NOT_DONE = "ORDB71";
    public boolean IS_FOCUS_PACK_NOT_DONE;

    private static final String CODE_DOWNLAOD_WAREHOUSE_STOCK = "ORDB72";
    public boolean IS_DOWNLOAD_WAREHOUSE_STOCK;

    private static final String CODE_ORDER_FROM_EXCESS_STOCK = "FUN69";
    public boolean IS_ORDER_FROM_EXCESS_STOCK;

    private static final String CODE_LOAD_SUBD_ONLY = "OFPLAN01";
    public boolean IS_LOAD_ONLY_SUBD;
    private static final String CODE_LOAD_NON_FIELD = "OFPLAN02";
    public boolean IS_LOAD_NON_FIELD;
    private static final String CODE_PLAN_RETAILER_ON_NONFILED = "OFPLAN03";
    public boolean IS_PLAN_RETIALER_NON_FIELD;

    private static final String CODE_EXPENSE_DAYS = "EXP01";
    public int expenseDays = 30;

    private static final String CODE_UPPERCASE_LETTER = "UPRCASE";
    public boolean IS_UPPERCASE_LETTER;

    private static final String CODE_PLANO_IMG_COUNT = "PLANO_IMG_COUNT";
    public int PLANO_IMG_COUNT;

    private static final String CODE_RETAILER_CONTACT_COUNT = "MAXCONTACT";
    public int RETAILER_CONTACT_COUNT = 2; // default two contact

    private static final String CODE_RETAILER_CONTACT_TAB = "CONTACT_TAB";
    public boolean IS_CONTACT_TAB;

    public static final String CODE_ENABLE_USER_FILTER_DASHBOARD = "DASH_USER_FILTER";
    public boolean IS_ENABLE_USER_FILTER_DASHBOARD;

    private static final String CODE_LICENSE_VALIDATION = "ORDB73";
    public boolean IS_ENABLE_LICENSE_VALIDATION;
    public boolean IS_SOFT_LICENSE_VALIDATION;

    private static final String CODE_ORD_DIGIT = "ORDB74";
    public boolean IS_ORD_DIGIT;
    public int ORD_DIGIT;

    private static final String CODE_STK_DIGIT = "ORDB80";
    public boolean IS_STK_DIGIT;
    public int STK_DIGIT;

    private static final String CODE_SWITCH_WITH_OUT_TGT_SELLER_DASHBOARD = "DASH15";
    public boolean IS_SWITCH_WITH_OUT_TGT;
    public String SELLER_KPI_CODES;

    private static final String CODE_SWITCH_WITH_OUT_TGT_SKU_WISE_DASHBOARD = "DASH16";
    public boolean IS_SWITCH_WITH_OUT_SKU_WISE_TGT;
    public String SELLER_SKU_WISE_KPI_CODES;

    private static final String CODE_NON_SALABLE_UNLOAD = "NS_UNLOAD";
    public boolean SHOW_NON_SALABLE_UNLOAD;

    private static final String CODE_TO_ENABLE_TRIP = "FUN80";
    public boolean IS_ENABLE_TRIP = false;
    public boolean IS_ALLOW_USER_TO_CONTINUE_FOR_MULTIPLE_DAYS_WITH_SAME_TRIP = false;


    /**
     * RoadActivity config *
     */
    public boolean IS_ORDER_STOCK;
    /**
     * Configuration not set in DB *
     */
    public String userLevel = "";
    public boolean IS_CUMULATIVE_AND;
    public boolean IS_NEARBY = false;
    public boolean SHOW_DEVICE_STATUS;
    public boolean floating_Survey = false;
    public boolean floating_np_reason_photo = false;
    public boolean IS_NEW_TASK;
    public boolean IS_SUGGESTED_ORDER; // used order screen to hid SO colom
    public boolean IS_SUGGESTED_ORDER_LOGIC;//used order screen to calculate so column
    public boolean IS_STOCK_IN_HAND; // used order screen to hide SIH colom
    public boolean IS_INVOICE; // decide seller is van seller or preseller
    public boolean IS_INVOICE_MASTER;
    public boolean IS_JUMP; // used to avoid jumping in activity menu
    public boolean IS_RETAILER_DEVIATION; // used to stop retailer deviation
    public boolean IS_MAP; // Not used yet
    public boolean IS_BAIDU_MAP;
    public boolean IS_DELIVERY_REPORT;
    public boolean IS_PHOTO_CAPTURE; // Activity Menu
    public boolean IS_PHOTO_CAPTURE_IMG_PATH_CHANGE;//to change image path for kelog's specific
    public boolean IS_PHOTO_COMPETITOR;
    public boolean IS_TASK; // Activity Menu

    public boolean IS_VISITSCREEN_DEV_ALLOW;
    public boolean IS_DATE_VALIDATION_REQUIRED;
    public boolean IS_INITIATIVE;

    public boolean SHOW_HST_DELDATE;
    public boolean SHOW_HST_INVDATE;
    public boolean SHOW_HST_INVQTY;
    public boolean SHOW_HST_REPCODE;
    public boolean SHOW_HST_TOTAL;
    public boolean SHOW_HST_VOLUM;
    public boolean SHOW_HST_DELSTATUS;
    public boolean SHOW_ORDER_HISTORY_DETAILS;
    public boolean SHOW_HST_STARTDATE;
    public boolean SHOW_HST_DUETDATE;
    public boolean SHOW_HST_PAID_AMOUNT;
    public boolean SHOW_HST_BAL_AMOUNT;
    public boolean SHOW_HST_DRIVER_NAME;
    public boolean SHOW_HST_PO_NUM;
    public boolean SHOW_HST_DOC_NO;

    public boolean SHOW_INV_HST_ORDERID;
    public boolean SHOW_INV_HST_INVOICEDATE;
    public boolean SHOW_INV_HST_INVOICEAMOUNT;
    public boolean SHOW_INV_HST_TOT_LINES;
    public boolean SHOW_INV_HST_DUEDATE;
    public boolean SHOW_INV_HST_OVERDUE_DAYS;
    public boolean SHOW_INV_HST_OS_AMOUNT;
    public boolean SHOW_INV_HST_STATUS;
    public boolean SHOW_INV_HST_VOLUME;
    public boolean SHOW_INV_HST_MARGIN_PRICE;
    public boolean SHOW_INV_HST_MARGIN_PER;
    public boolean SHOW_INV_ONDEMAND;


    public boolean IS_PRESENTATION_INORDER;
    public boolean SHOW_ALL_ROUTES; // RTRS07
    public boolean SHOW_RETAILER_VISIT_CONFIRMATION; // RTRS08
    public boolean HAS_NO_VISIT_REASON_VALIDATION; // VLD01
    public boolean HAS_PROFILE_BUTTON_IN_RETAILER_LIST; // PRO01
    public boolean SHOW_MONTH_OBJ_PROFILE; //
    public boolean SHOW_CREDIT_LIMIT_PROFILE; //
    public boolean SHOW_NO_VISIT_REASON; //
    public boolean SHOW_ORDER_HISTORY; // PRO05
    public boolean SHOW_ASSET_HISTORY; //PRO07
    public boolean SHOW_TASK;  //PRO08
    public boolean SHOW_AVG_SALES_PER_LEVEL;  //PRO09
    //
    public boolean SHOW_SALES_VALUE_DR;
    public boolean SHOW_PROFILE_EDIT;
    public boolean SHOW_LPC_ORDER;
    public boolean SHOW_TOTAL_QTY_ORDER_SUMMARY;
    public boolean SHOW_PRIMARY_CONTACT_NAME; //
    public boolean SHOW_PRIMARY_CONTACT_NUMBER; //
    public boolean SHOW_SECONDARY_CONTACT_NAME; //
    public boolean SHOW_SECONDARY_CONTACT_NUMBER; //
    public boolean IS_DIST_PRE_POST_ORDER;
    public boolean IS_DB_BACKUP;
    public boolean SHOW_GPS_ENABLE_DIALOG; // GPSENABLE


    public boolean SHOW_INIT_FOOTER;
    public boolean SHOW_REVIEW_AND_PO;
    // Order & stock flags
    public boolean SHOW_STOCK_SC;
    public boolean SHOW_STOCK_SP;
    public boolean SHOW_ORDER_PCS;
    public boolean SHOW_FOC;
    public boolean SHOW_ORDER_CASE;
    public boolean SHOW_ORDER_TOTAL;
    public boolean SHOW_INDICATIVE_ORDER;
    public boolean SHOW_CLEANED_ORDER;
    public boolean SHOW_ORDER_WEIGHT;
    public boolean SHOW_BARCODE;
    public boolean SHOW_VANLOAD_OC;
    public boolean SHOW_VANLOAD_OO;
    public boolean SHOW_VANLOAD_OP;
    public boolean SHOW_STOCK_RSN;// available reason
    public boolean SHOW_STOCK_CB;// available checkbox
    public boolean CHANGE_AVAL_FLOW;// check box tristate flow
    public boolean SHOW_LASTVISIT_GRAPH;

    public boolean SHOW_DISCOUNT_ACTIVITY;// FUN03
    public boolean SHOW_REPLACED_QTY_PC;
    public boolean SHOW_REPLACED_QTY_CS;
    public boolean SHOW_REPLACED_QTY_OU;

    // Outer & case conversion
    public boolean CONVERT_STOCK_SIH_PS;
    public boolean CONVERT_STOCK_SIH_CS;
    public boolean CONVERT_STOCK_SIH_OU;
    public boolean CONVERT_EOD_SIH_PS;
    public boolean CONVERT_EOD_SIH_CS;
    public boolean CONVERT_EOD_SIH_OU;

    public boolean SHOW_VAN_STK_PS;
    public boolean SHOW_VAN_STK_CS;
    public boolean SHOW_VAN_STK_OU;

    // Added in 32 version
    public boolean IS_WSIH; // ORDB16
    public boolean SHOW_HIGHLIGHT_FOR_OOS; // ORDB15
    public boolean IS_PRODUCT_DISCOUNT_BY_USER_ENTRY; // FUN02
    // Added in 33 version
    public boolean SHOW_CREDIT_BALANCE;//
    public boolean SHOW_CREDIT_DAYS;//
    public boolean SHOW_MAX_OUTSTANDING;//

    //public boolean SHOW_INITIATIVE_MERCHANDISING;
    // Added in 35 version
    public boolean SHOW_CALC;

    public boolean SHOW_BIXOLONI;
    public boolean SHOW_BIXOLONII;
    // this value specify how much value show after decimal
    public boolean SHOW_ZEBRA;
    public boolean IS_INVOICE_AS_MOD;
    // Added in 37 version
    public boolean IS_MUST_SELL;
    public boolean IS_MUST_SELL_REASON;
    public boolean IS_MUST_SELL_SKIP;
    public boolean IS_SCHEME_DIALOG;
    public boolean IS_PRODUCT_DIALOG;
    public boolean IS_PRODUCT_SCHEME_DIALOG;

    public boolean SHOW_STK_ORD_SRP = true;

    public boolean SHOW_STK_ORD_SRP_EDT;
    public boolean SHOW_STK_QTY_IN_ORDER;
    public boolean SHOW_D1;
    public boolean SHOW_D2;
    public boolean SHOW_D3;
    public boolean SHOW_DA;
    public boolean SHOW_DISCOUNTED_PRICE;
    public boolean SHOW_SPL_FILTER;
    public boolean SHOW_COMPETITOR_FILTER;
    public boolean SHOW_SPL_FLIER_NOT_NEEDED = false;
    public boolean SHOW_MVP_DRAWER;
    public boolean SHOW_LAT;
    public boolean SHOW_LONG;
    // Added in 38 version
    public boolean SHOW_REMARKS_STK_ORD;
    public boolean SHOW_REMARKS_STK_CHK;
    // Added in 45 version
    public boolean SHOW_LOCATION_PASSWORD_DIALOG;
    public boolean IS_SURVEY_ONCE;

    public boolean SHOW_SCORE_DASH;
    public boolean SHOW_INDEX_DASH;
    public boolean SHOW_TARGET_DASH;
    public boolean SHOW_ACHIEVED_DASH;
    public boolean SHOW_INCENTIVE_DASH;
    public boolean SHOW_BALANCE_DASH;
    public boolean SHOW_FLEX_DASH;
    public boolean SHOW_P3M_DASH;
    public boolean SHOW_YAI_DASH;
    public boolean SHOW_INV_DASH;
    public boolean SHOW_KPIBARCHART_DASH;
    public boolean SHOW_SMP_DASH;

    public boolean SHOW_CHART_DASH;

    public boolean SHOW_SAL_RET_REASON_DLG;
    public boolean SHOW_BATCH_ALLOCATION;// ORDB29
    public boolean IS_ORD_BY_BATCH_EXPIRY_DATE_WISE;
    public boolean SHOW_VANGPS_VALIDATION;
    public boolean SHOW_RET_SKIP_VALIDATION;
    public boolean SHOW_SIGNATURE_SCREEN;// SIGN01
    public boolean SHOW_INVOICE_CREDIT_BALANCE;// CREDIT01
    public boolean IS_POST_DATE_ALLOW;
    public boolean SHOW_DELIVERY_DATE;


    // NewOutlet
    public boolean IS_NEWOUTLET_IMAGETYPE;
    public boolean IS_NEWOUTLET_LOCATION;
    public boolean SHOW_DISC_AMOUNT_ALLOW;

    public boolean IS_FULL_PAYMENT;
    public boolean IS_COLLECTION_ORDER;
    public boolean SHOW_COLLECTION_REASON;
    public boolean IS_COLLECTION_MANDATE; // for Kellogs cash customer to do collection on the same day
    // Added in 40 version
    public boolean SHOW_ICO;
    public boolean SHOW_SO_SPLIT;
    public boolean ALLOW_SO_COPY;
    // in filter fragment]
    // Added in 41 version
    public boolean SHOW_MULTIPAYMENT = true;
    // Added in 42 version
    public boolean SHOW_SYNC_RETAILER_SELECT;
    public boolean SHOW_SYNC_EXPORT_TXT;
    public boolean SHOW_SYNC_DAYCLOSE;

    public boolean SHOW_OBJECTIVE;
    public boolean REMOVE_INVOICE;
    // Added in 43 version
    public boolean SHEME_NOT_APPLY_DEVIATEDSTORE;

    public boolean SHOW_ADVANCE_PAYMENT;
    public boolean SHOW_COLLECTION_SLAB;
    // Added in 45 version
    public boolean SHOW_PRINT_BUTTON;
    // Added in 45 version
    public boolean SHOW_BUTTON_PRINT01;
    // Added in 45 version
    public boolean IS_MULTIPLE_JOINCALL; // JOINTCALL01
    public boolean IS_ALLOW_SURVEY_WITHOUT_JOINTCALL; // JOINTCALL02
    public boolean IS_SIH_VALIDATION = false; // ORDB42
    public boolean HAS_SELLER_TYPE_SELECTION_ENABLED;// PREVAN01
    public boolean IS_SWITCH_SELLER_CONFIG_LEVEL;// PREVAN02
    public int switchConfigLevel = 0;
    public boolean IS_VALIDATE_CREDIT_DAYS;// CREDITDAY01
    public boolean SHOW_LINK_DASH_SKUTGT;// DASH03

    public boolean IS_ADD_NEW_BATCH;
    public boolean SHOW_PRODUCT_CODE;
    // Hide Review Plan Fields

    public boolean SHOW_ATTENDANCE;
    public boolean TAX_SHOW_INVOICE;
    //Is Hanging Order
    public boolean IS_HANGINGORDER;
    //Calculate QDVP3
    public boolean CALC_QDVP3;
    public boolean IS_SURVEY_GLOBAL_SAVE;
    public boolean HIDE_ORDER_DIST;
    public boolean IS_TAX_APPLIED_VALIDATION;
    public int ret_skip_flag = 0; // 1 Show OTP Dialog Skip Retailer Sequence, 0
    public boolean SHOW_PRODUCTRETURN; // ORDB41
    public boolean SHOW_GROUPPRODUCTRETURN; // ORDB43
    public boolean SHOW_CROWN_MANAGMENT; // ORDB44
    public boolean SHOW_FREE_PRODUCT_GIVEN; // ORDB45
    public boolean SHOW_BOTTLE_CREDITLIMIT;
    public boolean SHOW_SUBDEPOT;
    public boolean SHOW_SKUWISE_INCENTIVE;
    public boolean CALC_OUTSTANDING;
    public int VANLOAD_TYPE = 0;
    public boolean CALCULATE_UNLOAD;
    public boolean SHOW_SUPPLIER_SELECTION;
    public boolean SHOW_USER_TASK;
    public boolean SHOW_JOINT_CALL;
    public boolean SHOW_NEAREXPIRY_IN_STOCKCHECK;
    public boolean SHOW_VANLOAD_LABELS;
    public boolean SHOW_COLLECTION_BEFORE_INVOICE;
    public boolean SHOW_CAPTURED_LOCATION; // FUN14 -> Global GPS config.
    public boolean SHOW_DGTC;
    public boolean SHOW_INVOICE_SEQUENCE_NO;
    public boolean SHOW_ORDER_SEQUENCE_NO;
    public boolean SHOW_NON_SALABLE_PRODUCT;
    public boolean SHOW_SR_SEQUENCE_NO;
    public boolean SHOW_CN_SEQUENCE_NO;
    public boolean SHOW_STOCK_IN_SUMMARY;
    public boolean IS_TEAMLEAD;
    public int photocount;
    public int raPhotoCount;
    public int photopercent = 80;
    public int globalSeqId = 0;

    public int refreshMin = 5;
    public int tempOrderInterval = 10;
    public static String outDateFormat = "MM/dd/yyyy";//default date format
    public int printCount;
    public int PRINTER_SIZE;
    public int discountType = 0;
    public boolean SHOW_OUTER_CASE;
    public boolean IS_MUST_STOCK;
    public boolean DISABLE_MANUAL_ORDER;
    public boolean MUST_STOCK_ONLY;
    public boolean SHOW_STOCK_SO;
    public boolean SHOW_SO_APPLY;
    public boolean SHOW_STD_QTY_APPLY;
    public boolean STOCK_MAX_VALID;
    public boolean STOCK_DIST_INV;
    public boolean STOCK_APPROVAL;
    public boolean SHOW_END_JOURNEY;
    public boolean SHOW_RETAILER_SELECTION_VALID;
    public boolean SHOW_CLOSE_DAY_VALID;
    public boolean SHOW_ZEBRA_ATS;
    public boolean SHOW_INTERMEC_ATS;
    //public boolean SHOW_SRP_EDIT;
    public boolean SHOW_ZEBRA_DIAGEO;
    public boolean SHOW_ZEBRA_GHANA;
    public boolean SHOW_ZEBRA_UNIPAL;
    public boolean SHOW_ZEBRA_TITAN;
    public boolean SHOW_STKPRO_SPL_FILTER;
    public boolean SHOW_TOTAL_LINES;
    public boolean SHOW_TOTAL_QTY_IN_ORDER_REPORT;
    public boolean SHOW_DOWNLOAD_ALERT;
    public boolean SHOW_SIH_IN_PNAME;
    public boolean SHOW_CURRENT_STDQTY;
    public boolean SHOW_VALIDATION_DIST_INV;
    //public boolean SHOW_LOTNUMBER;

    // Added in 43 version
    public boolean SHOW_DEVIATION;
    public boolean SHOW_STORE_WISE_DISCOUNT_DLG;
    public boolean SHOW_STORE_WISE_DISCOUNT_DLG_MASTER;
    public int BILL_WISE_DISCOUNT = 0; // if 0 show discount in dialog /1 apply all bill discount / 3 with hold tax optional
    public boolean SHOW_STOCK_PRO_CREDIT_VALIDATION;
    public boolean SHOW_SALES_RETURN_IN_INVOICE;
    public boolean SHOW_CREDIT_INVOICE_COUNT;
    public boolean SHOW_TOTAL_DISCOUNT_EDITTEXT;
    public boolean SHOW_TOTAL_DISCOUNT_EDITTEXT_MASTER;

    public boolean HIDE_STOCK_APPLY_BUTTON;
    public boolean SHOW_UNIT_PRICE;
    // Added in 45 version
    public boolean SHOW_GOLD_STORE_DISCOUNT;
    public boolean ISUPLOADUSERLOC = false;
    public boolean IS_SHOW_DROPSIZE = false;
    public int DROPSIZE_ORDER_TYPE;

    public int LOAD_STOCK_COMPETITOR = 0;

    public int DEFAULT_NUMBER_OF_DAYS_TO_DELIVER_ORDER = 0;
    public int MIN_NUMBER_OF_DAYS_ALLOWED_TO_DELIVER = 0;
    public int MAX_NUMBER_OF_DAYS_ALLOWED_TO_DELIVER = 0;

    private static final String CODE_LOCATION_TIMER_PERIOD = "LOCTIMER";
    public int LOCATION_TIMER_PERIOD = 20;
    public boolean IS_LOC_TIMER_ON;

    public String LOAD_REMARKS_FIELD_STRING = "";
    public String LOAD_ORDER_SUMMARY_REMARKS_FIELD_STRING = "";
    public boolean IS_LOAD_STOCK_COMPETITOR = false;

    public boolean SHOW_SIH_SPLIT;

    public boolean IS_SHOW_REJECT_BTN;
    public boolean IS_VALIDATE_NEGATIVE_INVOICE;
    public boolean SHOW_GCM_NOTIFICATION;
    public boolean SHOW_PREV_ORDER_REPORT;
    public boolean IS_DELETE_TABLE;
    public boolean SHOW_INDICATIVE_ORDER_ICON;
    public boolean SHOW_DATE_BTN;
    public boolean IS_DIGITAL_CONTENT; // Activity Menu
    public boolean SHOW_NEW_OUTLET_UPLOAD;
    public boolean SHOW_MRP_LEVEL_TAX;
    public boolean SHOW_TAX;
    public boolean SHOW_DISCOUNT;
    public boolean IS_GLOBAL_LOCATION;
    public boolean IS_GLOBAL_CATEGORY;
    public boolean SHOW_OUTLET_PLANNING_TAB;
    public boolean SHOW_PROFILE_LOC1;
    public boolean SHOW_PROFILE_LOC2;
    public boolean SHOW_PROFILE_LOC3;
    public boolean SHOW_DATE_ROUTE;
    public boolean SHOW_BEAT_ROUTE;
    public boolean SHOW_WEEK_ROUTE;
    public boolean SHOW_DATE_PLAN_ROUTE;
    public boolean SHOW_RFIELD4;//RTRS26
    public boolean SHOW_MISSED_RETAILER;//RTRS28
    public boolean VALIDATE_TRADE_COVERAGE;//RTRS29
    public boolean SUBD_RETAILER_SELECTION;//RTRS30
    public boolean IS_SIMPLE_RETIALER;//RTRS31
    public boolean SHOW_RETIALER_CONTACTS;//RTRS32
    public boolean SHOW_CONTRACT_TYPE;
    public boolean SHOW_CONTRACT_EXPIRYDATE;
    public boolean SHOW_VISIT_FREQUENCY;
    public boolean SHOW_PRODUCT_FILTER_IN_SURVEY;
    public boolean SHOW__QDVP3_SCORE_CARD_TAB;
    public boolean SHOW__OUTLET_PROGRAM;
    public boolean SHOW__RE_CLASSIFICATION;
    public boolean SHOW_UPDATE_SIH;
    public boolean SHOW_RETAILER_FREQUENCY;
    public boolean IS_DAY_REPORT_PRINT;

    public boolean IS_ENABLE_CAMERA_PICTURE_SIZE = false;
    public int CAMERA_PICTURE_WIDTH = 640;
    public int CAMERA_PICTURE_HEIGHT = 480;
    public int CAMERA_PICTURE_QUALITY = 40;

    public boolean IS_SIH_VALIDATION_MASTER;
    public boolean IS_WSIH_MASTER;


    public boolean SHOW_TAX_MASTER;
    public boolean IS_EXCLUDE_TAX;

    public boolean IS_STOCK_IN_HAND_MASTER;
    public int PSWD_MIN_LEN = 0;
    public int PSWD_MAX_LEN = 0;
    public int PSWD_EXPIRY = 0;
    public boolean IS_SAME_LOGIN;
    public boolean IS_CHARACTER;
    public boolean IS_NUMERIC;
    public boolean IS_SPECIAL_CASE;
    public boolean IS_UPPER_CASE;
    public boolean IS_LOWER_CASE;
    public boolean IS_BATCHWISE_VANLOAD;
    public boolean SHOW_VANLOAD_STOCK_PRINT;
    public boolean IS_LOCATION_WISE_TAX_APPLIED;
    public String STRING_LOCATION_WISE_TAX_APPLIED = "";
    public String CODE_ORD_SUMMARY_DETAIL_DIALOG = "ORDB06";
    public String CODE_GLOBAL_DISOCUNT_DIALOG = "ORDB07";
    public boolean SHOW_ORDER_SUMMARY_DETAIL_DIALOG;
    public boolean SHOW_GLOBAL_DISOCUNT_DIALOG;
    public float discount_max = 100;
    public boolean SHOW_ORD_SUMMARY_PRICEOFF;
    public boolean SHOW_ORD_SUMMARY_DISC1;
    public boolean SHOW_ORD_SUMMARY_DISC2;
    public boolean SHOW_ORD_SUMMARY_DISC3;
    public boolean SHOW_ORD_SUMMARY_DISC4;
    public boolean SHOW_ORD_SUMMARY_DISC5;
    public boolean SHOW_ORD_SUMMARY_CASH_DISCOUNT;
    public boolean SHOW_ORD_SUMMARY_TAX;
    public boolean SHOW_NO_ORDER_CAPTURE_PHOTO;
    public boolean SHOW_NO_ORDER_EDITTEXT;
    public boolean SHOW_DEFAULT_LOCATION_POPUP;
    public boolean SHOW_NEW_OUTLET_ORDER;
    public boolean SHOW_NEW_OUTLET_OPPR;

    boolean SHOW_BATCH_WISE_PRICE;// BWP01
    public boolean SHOW_ORDER_TYPE_DIALOG;//ORDB39
    public boolean SHOW_ORDER_FOCUS_COUNT; //ORDB54
    public boolean SHOW_MENU_COUNTER_ALERT; //FUN28
    public boolean isRetailerBOMEnabled = false;


    //To show volume qty in order header report
    private static final String CODE_ORDER_RPT_VOLUME = "ORDRPT03";
    public boolean SHOW_VOLUME_QTY;

    // To hide sales value in sales performance
    private static final String CODE_OUTLET_SALES_VALUE = "SALES_VAL";
    public boolean HIDE_SALES_VALUE_FIELD;

    // TO show sync status report in Sync screen.
    private static final String CODE_DATA_UPLOAD_STATUS = "SYNC11";
    public boolean SHOW_DATA_UPLOAD_STATUS;


    //TO Show both salable and non salable products
    private static final String CODE_SALABLE_AND_NON_SALABLE_SKU = "CSSTK07";
    public boolean SHOW_SALABLE_AND_NON_SALABLE_SKU;

    //To show Product Code
    private static final String CODE_SHOW_SKU_CODE = "FUN06";
    public boolean IS_SHOW_SKU_CODE;

    private static final String CODE_SR_VALIDATE_BY_RETAILER_TYPE = "SR20";
    public boolean IS_SR_VALIDATE_BY_RETAILER_TYPE;

    private static final String CODE_SR_RETURN_OR_REPLACE_AT_ANY_LEVEL = "SR21";
    public boolean IS_SR_RETURN_OR_REPLACE_AT_ANY_LEVEL, IS_INDICATIVE_MASTER;

    private static final String CODE_SALES_RETURN_DELIVERY_SKU_LEVEL = "SR23";
    public boolean IS_SR_DELIVERY_SKU_LEVEL;

    private static final String CODE_REJECT_SALES_RETURN_DELIVERY = "DEL01";
    public boolean IS_SR_DELIVERY_REJECT;

    //int ROUND_DECIMAL_COUNT = 0;
    public boolean IS_CREDIT_NOTE_CREATION;
    private Context context;
    private BusinessModel bmodel;
    private SharedPreferences sharedPrefs;
    private String loadmanagementtitle;
    private String loadplanningsubttitle;
    private String tradecoveragetitle;


    private String expansetitle;
    private String subdtitle;


    private String batchAllocationtitle;
    private String signatureTitle;
    private String jointCallTitle;

    private String primarysaleTitle;
    private Vector<ConfigureBO> config;
    private Vector<ConfigureBO> activitymenuconfig;
    private Vector<ConfigureBO> primarymenus;

    public int alarmTime = 3;
    public int startTime = 8;
    public int endTime = 20;
    private Vector<ConfigureBO> genFilter, productdetails;
    private Vector<String> SIHApplyById = null;
    private ArrayList<ConfigureBO> mRetailerProperty;
    private Vector<ConfigureBO> profileConfig;
    private Vector<PasswordPolicyBO> passwordConfig;
    private Vector<ConfigureBO> storeCheckMenu;

    public boolean SHOW_SC;
    public boolean SHOW_SHO;
    public boolean SHOW_SP;
    public boolean SHOW_DIST_ORDER_CASE;
    public boolean SHOW_DIST_ORDER_OUTER;
    public boolean SHOW_DIST_ORDER_PIECE;
    public boolean SHOW_DIST_STOCK;
    private static final String CODE_SHOW_DIST_STOCK = "PSORD02";

    private static final String CODE_RTR_WISE_DOWNLOAD = "SYNC04";
    public boolean IS_RTR_WISE_DOWNLOAD;

    public boolean IS_USER_WISE_RETAILER_DOWNLOAD = false;
    public boolean IS_RET_NAME_RETAILER_DOWNLOAD = false;
    public boolean IS_BEAT_WISE_RETAILER_DOWNLOAD = false;
    public static final String CODE_USER_WISE_RETAILER_DOWNLOAD = "SYNC05";
    public static final String CODE_RETNAME_WISE_RETAILER_DOWNLOAD = "SYNC09";

    public boolean IS_SYNC_WITH_IMAGES = false;
    public static final String CODE_SYNC_WITH_IMAGES = "SYNC06";

    public boolean IS_INDICATIVE_ORDER = true;
    public static final String CODE_SHOW_STK_ORD_MRP = "ORDB38";
    public boolean SHOW_STK_ORD_MRP;

    public boolean IS_DEFAULT_PRESALE = false;

    private static final String CODE_ENABLE_GCM_REGISTRATION = "FUN21";
    public boolean IS_ENABLE_GCM_REGISTRATION;


    private static final String CODE_SHOW_FEEDBACK = "FUN22";
    public boolean SHOW_FEEDBACK;

    private static final String CODE_DOT_FOR_GROUP = "FUN29";
    public boolean IS_DOT_FOR_GROUP;


    public static final String CODE_RETAILER_PHOTO = "RTRS04";
    public boolean IS_RETAILER_PHOTO_NEEDED;
    public int RETAILER_PHOTO_COUNT = 1;
    private static final String CODE_NETAMOUNT_IN_REPORT = "ORDB50";
    public boolean SHOW_NETAMOUNT_IN_REPORT;
    public boolean IS_SUPPLIER_NOT_AVAILABLE;
    public static final String CODE_COLLECTION_SEQ_NO = "COLL11";
    public boolean SHOW_COLLECTION_SEQ_NO;
    public static final String CODE_SHOW_SERIAL_NO = "ORDB51";
    public boolean SHOW_SERIAL_NO_SCREEN;
    public static final String CODE_SHOW_RETAILER_SELECTION_FILTER = "RTRS09";
    public boolean SHOW_RETAILER_SELECTION_FILTER = false;
    public static final String CODE_FEEDBACK_IN_CLOSE_CALL = "FUN27";
    public boolean SHOW_FEEDBACK_IN_CLOSE_CALL = false;
    public static final String CODE_PRINT_DELIVERY_MANAGEMENT = "";
    public boolean SHOW_PRINT_DELIVERY_MANAGEMENT;

    public boolean SHOW_INCLUDE_BILL_TAX;
    public boolean CHECK_LIABLE_PRODUCTS;


    public static final String CODE_CREDIT_NOTE_PRINT = "SR06";
    public boolean SHOW_PRINT_CREDIT_NOTE;

    public static final String CODE_CUSTOM_KEYBOARD_NEW = "FUN30";
    public boolean SHOW_CUSTOM_KEYBOARD_NEW;

    public static final String CODE_NFC_VALIDATION_FOR_RETAILER = "RTRS10";
    public boolean SHOW_NFC_VALIDATION_FOR_RETAILER;

    private static final String CODE_NFC_SEARCH_IN_ASSET = "AT07";
    public boolean SHOW_NFC_SEARCH_IN_ASSET;

    private static final String CODE_ASSET_PHOTO_VALIDATION = "AT10";
    public boolean ASSET_PHOTO_VALIDATION;

    public static final String CODE_IS_UNLINK_FILTERS = "ORDB55";
    public boolean IS_UNLINK_FILTERS;

    public static final String CODE_IS_USER_BASED_DASH = "DASH08";
    public boolean IS_USER_BASED_DASH;

    public static final String CODE_IS_DISTRIBUTOR_BASED_DASH = "DASH09";
    public boolean IS_DISTRIBUTOR_BASED_DASH;

    public static final String CODE_IS_NIVEA_DASH = "DASH11";
    public boolean IS_NIVEA_BASED_DASH;

    public static final String CODE_IS_SMP_DASH = "DASH12";
    public boolean IS_SMP_BASED_DASH;

    public static final String CODE_ATTENDANCE_SYNCUPLOAD = "FUN34";
    public boolean IS_ATTENDANCE_SYNCUPLOAD;

    public static final String CODE_SHOW_DISTRIBUTOR_AVAILABLE = "FUN35";
    public boolean IS_DISTRIBUTOR_AVAILABLE;

    public static final String CODE_IS_DEACTIVATE_RETAILER = "PRO23";
    public boolean IS_DEACTIVATE_RETAILER;

    public static final String CODE_CLEAR_DATA = "SETTING01";
    public boolean IS_CLEAR_DATA;

    private static final String CODE_DIST_SELECT_BY_SUPPLIER = "FUN37";
    public boolean IS_DIST_SELECT_BY_SUPPLIER;
    public boolean IS_EOD_COLUMNS_AVALIABLE;
    private static final String CODE_EOD_COLUMNS = "EOD01";
    private static final String CODE_SR_SEQUENCE_NUMBER = "SR08";
    private static final String CODE_CN_SEQUENCE_NUMBER = "SR09";

    public boolean SHOW_PRICECHECK_IN_STOCKCHECK;
    public String CODE_IS_PRICECHECK_IN_STOCKCHECK = "CSSTK02";
    public boolean IS_PRICE_CHECK_RETAIN_LAST_VISIT_TRAN;
    public static final String CODE_PRICE_CHECK_RETAIN_LAST_VISIT_TRAN = "PRICE_RETAINLV";

    public boolean IS_STOCK_CHECK_RETAIN_LAST_VISIT_TRAN;
    public static final String CODE_STOCK_CHECK_RETAIN_LAST_VISIT_TRAN = "CSSTK03";

    public boolean IS_NEAR_EXPIRY_RETAIN_LAST_VISIT_TRAN;
    public static final String CODE_NEAR_EXPIRY_RETAIN_LAST_VISIT_TRAN = "NEXP01";

    public boolean IS_PROMOTION_RETAIN_LAST_VISIT_TRAN;
    public static final String CODE_PROMOTION_RETAIN_LAST_VISIT_TRAN = "PROMO02";

    public boolean IS_SURVEY_RETAIN_LAST_VISIT_TRAN;
    public static final String CODE_SURVEY_RETAIN_LAST_VISIT_TRAN = "SURVEY11";

    public boolean IS_SOS_RETAIN_LAST_VISIT_TRAN;
    public static final String CODE_SOS_RETAIN_LAST_VISIT_TRAN = "SOS02";

    public boolean IS_PLANOGRAM_RETAIN_LAST_VISIT_TRAN;
    public static final String CODE_PLANOGRAM_RETAIN_LAST_VISIT_TRAN = "PLANO01";

    public boolean IS_DISPLAY_ASSET_RETAIN_LAST_VISIT_TRAN;
    public static final String CODE_DISPLAY_ASSET_RETAIN_LAST_VISIT_TRAN = "DASSET01";

    public static final String CODE_PERRPT_REFRESH = "PERFRPTSYNC";

    public boolean IS_SF_NORM_CHECK;
    public static final String CODE_CHECK_NORM = "SFCHECK";

    public boolean SHOW_STOCK_REPLACE, SHOW_STOCK_EMPTY, SHOW_STOCK_FREE_ISSUED, SHOW_STOCK_RETURN, SHOW_STOCK_NON_SALABLE, SHOW_STOCK_VAN_UNLOAD, SHOW_FREE_STOCK_LOADED, SHOW_FREE_STOCK_IN_HAND;

    public boolean IS_PRINT_CREDIT_NOTE_REPORT;
    public static final String CODE_PRINT_CREDIT_NOTE_REPORT = "CDN01";

    public boolean IS_PRICE_CHECK_RETAIN_LAST_VISIT_IN_EDIT_MODE;
    public static final String CODE_PRICE_CHECK_RETAIN_LAST_VISIT_IN_EDIT_MODE = "PRICE_RETAINVW";

    private static final String CODE_DAY_WISE_RETAILER_WALKINGSEQ = "FUN40";
    public boolean IS_DAY_WISE_RETAILER_WALKINGSEQ;


    private static final String CODE_PARTIAL_CREDITNOTE = "COLL13";
    public boolean IS_PARTIAL_CREDIT_NOTE_ALLOW;

    private static final String CODE_PAYMENT_RECEIPT_NO = "COLL14";
    public boolean IS_PAYMENT_RECEIPTNO_GET;

    public boolean COLL_CHEQUE_MODE;
    private static final String CODE_COLL_CHEQUE_MODE = "COLL15";

    private static final String CODE_DOC_REF_NO = "COLL16";
    public boolean SHOW_DOC_REF_NO;

    private static final String CODE_COLLECTION_MANDATE = "COLL17";

    private static final String CODE_IS_NEW_RETAILER_EDIT = "NEWRET01";
    public boolean IS_NEW_RETAILER_EDIT;

    public static final String CODE_EOD_STOCK_SPLIT = "EODRPT_SPLIT";
    public boolean IS_EOD_STOCK_SPLIT = false;

    public boolean SHOW_EOD_OP;
    public boolean SHOW_EOD_OC;
    public boolean SHOW_EOD_OO;

    private static final String CODE_SHOW_STOCK_AVAILABLE_PRODUCTS_ONLY = "ORDB28";
    public boolean IS_STOCK_AVAILABLE_PRODUCTS_ONLY;
    public boolean IS_STOCK_AVAILABLE_PRODUCTS_ONLY_MASTER;

    private static final String CODE_BAR_CODE = "ORDB09";
    private static final String CODE_BAR_CODE_STOCK_CHECK = "CSSTK05";
    private static final String CODE_BAR_CODE_PRICE_CHECK = "PRICE_BARCODE";
    private static final String CODE_BAR_CODE_VAN_UNLOAD = "VAN_BARCODE";

    public boolean IS_BAR_CODE;
    public boolean IS_BAR_CODE_STOCK_CHECK;
    public boolean IS_BAR_CODE_PRICE_CHECK;
    public boolean IS_BAR_CODE_VAN_UNLOAD;

    public boolean IS_QTY_INCREASE;

    private static final String CODE_APLLY_BATCH_PRICE_FROM_PRODCUT = "FUN43";
    public boolean IS_APPLY_BATCH_PRICE_FROM_PRODUCT;


    private static final String CODE_SHOW_TAX_DISCOUNT_IN_REPORT = "INVRPT01";
    public boolean IS_SHOW_TAX_IN_REPORT;


    public boolean IS_SHOW_DISCOUNT_IN_REPORT;

    private static final String CODE_MAX_CREDIT_DAYS = "MAX_CREDIT_DAYS";
    public int MAX_CREDIT_DAYS = 90;

    private static final String CODE_ALLOW_BACK_DATE = "FUN47";
    public boolean ALLOW_BACK_DATE;

    private static final String CODE_SHOW_DELETE_OPTION = "FUN48";
    public boolean IS_SHOW_DELETE_OPTION;

    private static final String CODE_SHOW_ORDERING_SEQUENCE = "FUN49";
    public boolean IS_SHOW_ORDERING_SEQUENCE;

    private static final String CODE_GUIDED_SELLING = "FUN51";
    public boolean IS_GUIDED_SELLING;

    public boolean SHOW_BIXOLON_TITAN;
    public boolean SHOW_SCRIBE_TITAN;
    private static String CODE_BIXOLON_TITAN = "PRINT11";
    private static String CODE_SCRIBE_TITAN = "PRINT12";

    public boolean SHOW_DELIVERY_PC;
    public boolean SHOW_DELIVERY_CA;
    public boolean SHOW_DELIVERY_OU;

    private static final String CODE_RETAIN_NEAREXPIRY_CURRENT_TRAN_IN_STOCKCHECK = "ORDB59";
    private static final String CODE_SPL_FILTER_TAB = "ORDB61";
    public boolean IS_RETAIN_NEAREXPIRY_CURRENT_TRAN_IN_STOCKCHECK;
    public boolean IS_SPL_FILTER_TAB;

    private static final String CODE_ENABLE_BACKDATE_REPORTING = "SYNC08";
    public boolean IS_ENABLE_BACKDATE_REPORTING;

    private static final String CODE_MOVE_NEXT_ACTIVITY = "FUN50";
    public boolean MOVE_NEXT_ACTIVITY;


    private static final String CODE_PRINT_FILE_SAVE = "FUN52";
    public boolean IS_PRINT_FILE_SAVE;

    private static final String CODE_APPLY_DISTRIBUTOR_WISE_PRICE = "FUN53";
    public boolean IS_APPLY_DISTRIBUTOR_WISE_PRICE;

    private static final String CODE_ORDER_PRINT = "ORDB19";
    public boolean SHOW_PRINT_ORDER;

    private static final String CODE_ORD_CALC = "ORDB62";
    public boolean SHOW_ORD_CALC;

    public boolean ROUND_OF_CONFIG_ENABLED = false;

    private static final String CODE_CURRENCY_VALUE = "CURVAL";
    public boolean IS_FORMAT_USING_CURRENCY_VALUE;
    public boolean IS_APPLY_CURRENCY_CONFIG;

    private static final String CODE_GROUP_PRODUCTS_IN_COUNTER_SALES = "CNT02";
    public boolean IS_GROUP_PRODUCTS_IN_COUNTER_SALES;

    private HashMap<String, Boolean> hashMapHHTModuleConfig;
    private HashMap<String, Integer> hashMapHHTModuleOrder;


    public boolean SHOW_INVOICE_HISTORY_DETAIL = false;


    private static final String CODE_SHOW_VALUE_ORDER = "ORDB64";
    public boolean SHOW_TOTAL_VALUE_ORDER;

    public boolean SHOW_TOTAL_QTY_ORDER;
    private static final String CODE_SHOW_QTY_ORDER = "ORDB65";



    //cpg132-task 13
    public boolean SHOW_TOTAL_ACHIEVED_VOLUME;
    public boolean SHOW_TOTAL_ACHIEVED_VOLUME_WGT;
    public boolean SHOW_TOTAL_TIME_SPEND;
    public boolean SHOW_STORE_VISITED_COUNT;
    private static final String CODE_TOTAL_ACHIEVEDVOLUME = "RTRS34";

    public static final String CODE_TAX_MODEL = "TAX_MODEL";
    public boolean IS_GST;
    public boolean IS_GST_MASTER;
    public boolean IS_GST_HSN;
    public boolean IS_GST_HSN_MASTER;

    public String CODE_ORDER_REPORT_EXPORT_METHOD = "ORDRPT01";
    public boolean IS_EXPORT_ORDER_REPORT;
    public boolean IS_ORDER_REPORT_EXPORT_ONLY;
    public boolean IS_ORDER_REPORT_EXPORT_AND_EMAIL;
    public boolean IS_ORDER_REPORT_EXPORT_AND_SHARE;

    public String CODE_PRODUCT_DISPLAY_FOR_PIRAMAL = "ORDB66";
    public boolean IS_PRODUCT_DISPLAY_FOR_PIRAMAL;

    private static final String CODE_REASON_FOR_ALL_NON_STOCK_PRODUCTS = "FUN56";
    public boolean IS_REASON_FOR_ALL_NON_STOCK_PRODUCTS;

    private static final String CODE_LOAD_WAREHOUSE_PRD_ONLY = "FUN58";
    public boolean IS_LOAD_WAREHOUSE_PRD_ONLY;

    private static final String CODE_PRINT_SEQUENCE = "PRINT_SEQUENCE";
    public boolean IS_PRINT_SEQUENCE_REQUIRED;
    public boolean IS_PRINT_SEQUENCE_LEVELWISE;

    private static final String CODE_SHOW_INVOICE_HISTORY = "PRO06";
    public boolean SHOW_INVOICE_HISTORY; // PRO06

    private static final String CODE_SALES_DISTRIBUTION = "SALES_DISTRIBUTION_TAGGING";
    public boolean IS_PRODUCT_DISTRIBUTION;
    public String PRD_DISTRIBUTION_TYPE = "";

    private static final String CODE_REMOVE_TAX_ON_SRP = "ORDB67";
    public boolean IS_REMOVE_TAX_ON_SRP;

    private static final String CODE_SHARE_INVOICE = "ORDB68";
    public boolean IS_SHARE_INVOICE;

    private static final String CODE_STK_ORD_ROW = "ORDB69";
    private static final String CODE_STK_ORD_ROW_BS = "BS";
    private static final String CODE_STK_ORD_ROW_PROJECT = "PROJECT";
    public boolean IS_STK_ORD_BS;
    public boolean IS_STK_ORD_PROJECT;

    private static final String CODE_RESTRICT_ORDER_TAKING = "ORDB70";
    public boolean IS_RESTRICT_ORDER_TAKING;

    public boolean IS_COMBINED_STOCK_CHECK_FROM_ORDER;

    private static final String CODE_ORDER_SUMMERY_EXPORT_AND_EMAIL = "FUN65";
    public boolean IS_ORDER_SUMMERY_EXPORT_AND_EMAIL;
    public boolean IS_ATTACH_PDF;

    public int MVPTheme = 0;

    private static final String CODE_DOC_REF = "DOCREF";
    public boolean IS_DOC_SIGN;
    public boolean IS_DOC_REFNO;

    private static final String CODE_SALES_RETURN_VALIDATE = "SR13";
    public boolean IS_SALES_RETURN_VALIDATE;
    private static final String CODE_SALES_RETURN_SIGN = "SR14";
    public boolean IS_SALES_RETURN_SIGN;
    private static final String CODE_COMPUTE_DUE_DATE = "DDATE";
    public boolean COMPUTE_DUE_DATE;
    private static final String CODE_COMPUTE_DUE_DAYS = "DDAYS";
    public boolean COMPUTE_DUE_DAYS;

    public boolean SHOW_SALES_RETURN_IN_ORDER;
    public boolean SHOW_SALES_RETURN_TV_IN_ORDER;
    public boolean SHOW_SALES_RETURN_IN_DELIVERY;


    public int retailerLocAccuracyLvl;

    private static final String CODE_MUST_SELL_STK = "MSLSTK";
    public boolean IS_MUST_SELL_STK;

    //unipal specific
    public boolean SHOW_PRINT_HEADERS;
    private static final String CODE_SHOW_PRINT_HEADERS = "PRINT_HEADER";

    private static final String CODE_ORD_SR_VALUE_VALIDATE = "SR15";
    public boolean IS_ORD_SR_VALUE_VALIDATE;

    private static final String CODE_SR_INDICATIVE = "SR16";
    public boolean IS_INDICATIVE_SR;

    private static final String CODE_SR_INVOICE = "SR18";
    public boolean IS_INVOICE_SR;

    private static final String CODE_GENERATE_SR_IN_DELIVERY = "SR19";
    public boolean IS_GENERATE_SR_IN_DELIVERY;


    private static final String CODE_REALTIME_LOCATION_CAPTURE = "REALTIME01";
    public boolean IS_REALTIME_LOCATION_CAPTURE;

    private static final String CODE_UPLOAD_ATTENDANCE = "UPLOADATTENDANCE";
    public boolean IS_UPLOAD_ATTENDANCE;

    private static final String CODE_REMARK_ATTENDANCE = "ATTREMARK";
    public boolean IS_ATTENDANCE_REMARK;

    private static final String CODE_SHOW_DISTRIBUTOR_PROFILE = "PRO27";
    public boolean SHOW_DISTRIBUTOR_PROFILE;
    public int SHOW_DISTRIBUTOR_PROFILE_FROM;

    private static final String CODE_SBD_TARGET_PERCENT = "SBD_DIST_ACH";
    public static int SBD_TARGET_PERCENTAGE = 100;

    private static final String CODE_SBD_GAP_PROFILE = "SBD_PROFILE";
    public boolean SHOW_SBD_GAP_IN_PROFILE = true;

    private static final String CODE_SPLIT_ORDER = "SPLIT_ORDER";  //jnj project specific
    public boolean IS_ORDER_SPLIT;

    private static final String CODE_PHOTODAM = "PHOTODAM";
    public boolean SHOW_PHOTO_ODAMETER;

    public boolean IS_BEAT_WISE_RETAILER_MAPPING = true;
    private static final String CODE_BEAT_WISE_RETAILER = "FIELD_USER_PLAN";

    public boolean IS_FILTER_TAG_PRODUCTS = true;
    private static final String CODE_FILTER_TAGGED_PRODUCTS = "FILTER_TAG";

    private static final String CODE_ENABLE_SHARE_PERCENTAGE_STOCK_CHECK = "CSSTK04";  //jnj project specific
    public boolean IS_ENABLE_SHARE_PERCENTAGE_STOCK_CHECK;

    private static final String CODE_ENABLE_PROMOTION_SKUNAME = "PROMO03";  //jnj project specific
    public boolean IS_ENABLE_PROMOTION_SKUNAME;
    private static final String CODE_ENABLE_PROMOTION_DATES = "PROMO04";  //jnj project specific
    public boolean IS_ENABLE_PROMOTION_DATES;

    private static final String CODE_TASK_OPEN = "TASK_RPT_OPEN";
    public int TASK_OPEN;

    private static final String CODE_TASK_PLANNED = "TASK_RPT_PLANNED";
    public int TASK_PLANNED = -1;

    private static final String CODE_TASK_SELLER_RPT = "TASK_SELLER_RPT";
    public boolean IS_SELLER_TASK_RPT;

    public boolean IS_WITHHOLD_DISCOUNT;

    private static final String CODE_ORDER_STATUS_REPORT = "ORD_STAT_RPT";
    public boolean IS_ENABLE_ORDER_STATUS_REPORT;
    public boolean IS_ORDER_STATUS_REPORT;

    private static final String CODE_SHOW_DEFAULT_UOM = "ORDB24";
    public boolean IS_SHOW_DEFAULT_UOM;

    private static final String CODE_SHOW_EXPLIST_IN_PROMO = "PROMO06";
    public boolean IS_SHOW_EXPLIST_IN_PROMO;

    private static final String CODE_SHOW_ORDER_PHOTO_CAPTURE = "ORDB20";
    public boolean IS_SHOW_ORDER_PHOTO_CAPTURE;
    //132 --- task 45
    public boolean IS_SHOW_ORDER_ATTACH_FILE;

    private static final String CODE_SHOW_ALL_SKU_ON_EDIT = "ORDB75";
    public boolean IS_SHOW_ALL_SKU_ON_EDIT;

    //Provision to highlight 0 qty of warehouse stock in ordered products
    private static final String CODE_SHOW_OOS = "ORDB76";
    public boolean IS_SHOW_OOS;

    //Provision to load stock check whether from last visit or closed stock
    private static final String CODE_STK_CHECK_LAST_VISIT = "ORDB77";
    public boolean IS_LOAD_STK_CHECK_LAST_VISIT;

    private static final String CODE_KPI_CALENDAR = "KPI_CALENDER";
    public boolean IS_KPI_CALENDAR;

    private static final String CODE_GST_TAX_LOCATION_TYPE = "TAX_LOCATION_TYPE";
    public boolean IS_TAX_LOC;

    private static final String CODE_CHECK_PHOTO_MANDATORY = "FUN71";
    public boolean IS_CHECK_PHOTO_MANDATORY;

    private static final String CODE_SHOW_MODULE_MANDATORY = "FUN72";
    public boolean IS_CHECK_MODULE_MANDATORY;

    private static final String CODE_DISCOUNT_PRICE_PER = "FUN73";
    public boolean IS_DISCOUNT_PRICE_PER;
    public double DISCOUNT_PRICE_PER = 50;

    private static final String CODE_NAVIGATE_CREDIT_NOTE_SCREEN = "COLL19";
    public boolean IS_NAVIGATE_CREDIT_NOTE_SCREEN;

    private static final String CODE_NO_COLLECTION_REASON = "COLL18";
    public boolean SHOW_NO_COLLECTION_REASON;

    private static final String CODE_GLOBAL_SHOW_NO_ORDER_REASON = "FUN74";
    public boolean SHOW_GLOBAL_NO_ORDER_REASON;
    private static final String CODE_MENU_FIREBASE_CHAT = "CHAT02";
    public boolean IS_FIREBASE_CHAT_ENABLED;

    private static final String CODE_CHECK_DIGITAL_SIZE = "SYNC12";
    public long DIGITAL_CONTENT_SIZE = -1;

    private static final String CODE_FREE_SIH_AVAILABLE = "FUN75";
    public boolean IS_FREE_SIH_AVAILABLE;

    private static final String CODE_SKIP_SCHEME_APPLY = "SCH12";
    public boolean IS_SKIP_SCHEME_APPLY;

    private static final String CODE_SHOW_TERMS_COND = "FUN76";
    public boolean IS_SHOW_TERMS_COND;

    private static final String CODE_VOICE_TO_TEXT = "VOICETXT";
    public int IS_VOICE_TO_TEXT = -1;

    private static final String CODE_SKIP_CALL_ANALYSIS = "FUN78";
    public boolean IS_SKIP_CALL_ANALYSIS;

    private static final String CODE_COLLECTION_DELETE = "COLL20";
    public boolean IS_COLLECTION_DELETE;

    private static final String CODE_VALIDATE_DUE_DATE = "CREDITDUE01";
    public boolean IS_VALIDATE_DUE_DAYS;

    private static final String CODE_SHOW_TASK_PRODUCT_LEVEL = "TASK01";
    public boolean IS_SHOW_TASK_PRODUCT_LEVEL;
    public int TASK_PRODUCT_LEVEL_NO;

    private static final String CODE_TASK_DUDE_DATE_COUNT = "TASK02";
    public int IS_TASK_DUDE_DATE_COUNT;

    private static final String CODE_TASK_REMARKS_MANDATORY = "TASK03";
    public boolean IS_TASK_REMARKS_MANDATORY;

    private static final String CODE_SHOW_RETAILER_LAST_VISIT = "RTRS33";
    public boolean IS_SHOW_RETAILER_LAST_VISIT;
    public boolean IS_SHOW_RETAILER_LAST_VISITEDBY;
    public int ret_skip_otp_flag = 0; // 0 otp edittext, 1 Show reason spinner, 2 Soft Alert, 3 Hard Alert

    private static final String CODE_SHOW_PAUSE_CALL_ANALYSIS = "FUN81";
    public boolean IS_SHOW_PAUSE_CALL_ANALYSIS;

    private static final String CODE_DISABLE_CALL_ANALAYSIS_TIMER = "FUN82";
    public boolean IS_DISABLE_CALL_ANALYSIS_TIMER = true;

    private static final String CODE_SHOW_SORT_STKCHK = "CSSTK09";
    public boolean IS_SHOW_SORT_STKCHK = true;

    private static final String CODE_ENABLE_EDIT_OPTION_FOR_OTHER_USER = "NOTE01";
    public boolean IS_ENABLE_EDIT_OPTION_FOR_OTHER_USER;

    private static final String CODE_ENABLE_GLOBAL_GPS_DISTANCE = "GPSDISTANCE";
    public int GLOBAL_GPS_DISTANCE;

    public boolean IS_SURVEY_PDF_SHARE;
    public static final String CODE_SURVEY_PDF_SHARE = "SURVEY15";

    private final String CODE_PRE_VISIT = "PREVISIT";
    public boolean IS_PRE_VISIT = false;

    private static final String CODE_SHOW_ANNOUNCEMENT = "ANNCMNT01";
    public Boolean IS_SHOW_ANNOUNCEMENT;

    private static final String CODE_SHOW_NOTIFICATION = "NOTIFY01";
    public boolean IS_SHOW_NOTIFICATION;

    private ConfigurationMasterHelper(Context context) {
        this.context = context;
        this.bmodel = (BusinessModel) context;

    }

    public static ConfigurationMasterHelper getInstance(Context context) {
        if (instance == null) {
            instance = new ConfigurationMasterHelper(context);
        }
        return instance;
    }

    public String getLoadplanningsubttitle() {
        return loadplanningsubttitle;
    }

    public void setLoadplanningsubttitle(String loadplanningsubttitle) {
        this.loadplanningsubttitle = loadplanningsubttitle;
    }

    public String getPrimarysaleTitle() {
        return primarysaleTitle;
    }

    public void setPrimarysaleTitle(String primarysaleTitle) {
        this.primarysaleTitle = primarysaleTitle;
    }

    public Vector<ConfigureBO> getProfileModuleConfig() {
        if (profileConfig == null) {
            profileConfig = new Vector<>();
        }
        return profileConfig;
    }


    /**
     * Method will download configuration related to retailer profile view.
     */
    public void downloadProfileModuleConfig() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
        try {
            SharedPreferences sharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(context);
            String locale = sharedPrefs.getString("languagePref",
                    ApplicationConfigs.LANGUAGE);
            db.openDataBase();
            profileConfig = new Vector<>();
            String query = "select HHTCode,MName,RField,hasLink,flag,RField6,MNumber,Regex,RField1 from "
                    + DataMembers.tbl_HhtMenuMaster
                    + " where flag=1" +
                    " and MenuType= 'RETAILER_PROFILE' and lang=" + bmodel.QT(locale)
                    + " order by MNumber";

            Cursor c = db.selectSQL(query);
            ConfigureBO con;
            if (c != null) {
                while (c.moveToNext()) {
                    con = new ConfigureBO();
                    con.setConfigCode(c.getString(0));
                    con.setMenuName(c.getString(1));
                    con.setModule_Order(c.getInt(2));
                    con.setHasLink(c.getInt(3));
                    con.setFlag(c.getInt(4));
//                    con.setMaxLengthNo(c.getInt(5));
                    con.setMenuNumber(c.getString(6));
                    String str = c.getString(7);
                    if (str != null && !str.isEmpty()) {
                        if (str.contains("<") && str.contains(">")) {

                            String minlen = str.substring(str.indexOf("<") + 1, str.indexOf(">"));
                            if (!minlen.isEmpty()) {
                                try {
                                    con.setMaxLengthNo(SDUtil.convertToInt(minlen));
                                } catch (Exception ex) {
                                    Commons.printException("min len in new outlet helper", ex);
                                }
                            }
                        }
                    }
                    con.setRegex(c.getString(7));
                    con.setMandatory(c.getInt(8));
                    profileConfig.add(con);

                }
                c.close();
            }
            db.closeDB();
            configProfileStatus();


        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
        }
    }

    private void configProfileStatus() {
        ConfigureBO con;
        int siz = profileConfig.size();
        if (siz == 0)
            return;

        for (int i = 0; i < siz; ++i) {
            con = profileConfig.get(i);

            if (CODE_LAT.equals(con.getConfigCode())
                    && con.getHasLink() == 1)
                this.SHOW_LAT = true;
            else if (CODE_LONG.equals(con.getConfigCode())
                    && con.getHasLink() == 1)
                this.SHOW_LONG = true;
            else if (CODE_PRIMARY_CONTACT_NAME.equals(con.getConfigCode())
                    && con.isFlag() == 1) {
                this.SHOW_PRIMARY_CONTACT_NAME = true;
                profileConfig.add(new ConfigureBO("LNAME", "Contact1_LName", "", 1, 1, 1));
            } else if (CODE_PRIMARY_CONTACT_NUMBER.equals(con.getConfigCode())
                    && con.getHasLink() == 1)
                this.SHOW_PRIMARY_CONTACT_NUMBER = true;
            else if (CODE_SECONDARY_CONTACT_NAME.equals(con.getConfigCode())
                    && con.isFlag() == 1) {
                this.SHOW_SECONDARY_CONTACT_NAME = true;
                profileConfig.add(new ConfigureBO("LNAME2", "Contact2_LName", "", 1, 1, 1));
            } else if (CODE_SECONDARY_CONTACT_NUMBER.equals(con.getConfigCode())
                    && con.getHasLink() == 1)
                this.SHOW_SECONDARY_CONTACT_NUMBER = true;
            else if (CODE_PROFILE_LOC1.equals(con.getConfigCode())
                    && con.getHasLink() == 1) {
                SHOW_PROFILE_LOC1 = true;
            } else if (CODE_PROFILE_LOC2.equals(con.getConfigCode())
                    && con.getHasLink() == 1) {
                SHOW_PROFILE_LOC2 = true;
            } else if (CODE_PROFILE_LOC3.equals(con.getConfigCode())
                    && con.getHasLink() == 1) {
                SHOW_PROFILE_LOC3 = true;
            } else if (CODE_SHOW_MONTH_OBJ_PROFILE.equals(con.getConfigCode())
                    && con.getHasLink() == 1)
                this.SHOW_MONTH_OBJ_PROFILE = true;
            else if (CODE_SHOW_CREDIT_LIMIT_PROFILE.equals(con.getConfigCode())
                    && con.getHasLink() == 1)
                this.SHOW_CREDIT_LIMIT_PROFILE = true;
            else if (CODE_SHOW_NO_VISIT_REASON.equals(con.getConfigCode())
                    && con.getHasLink() == 1)
                this.SHOW_NO_VISIT_REASON = true;
            else if (CODE_CONTRACT_TYPE.equals(con.getConfigCode())
                    && con.getHasLink() == 1) {
                SHOW_CONTRACT_TYPE = true;
            } else if (CODE_CONTRACT_EXPIRYDATE.equals(con.getConfigCode())
                    && con.getHasLink() == 1) {
                SHOW_CONTRACT_EXPIRYDATE = true;
            } else if (CODE_VISIT_FREQUENCY.equals(con.getConfigCode())
                    && con.getHasLink() == 1) {
                SHOW_VISIT_FREQUENCY = true;
            } else if (CODE_SHOW_CREDIT_BALANCE.equals(con.getConfigCode())
                    && con.getHasLink() == 1)
                this.SHOW_CREDIT_BALANCE = true;
            else if (CODE_SHOW_CREDIT_DAYS.equals(con.getConfigCode())
                    && con.getHasLink() == 1)
                this.SHOW_CREDIT_DAYS = true;
            else if (CODE_SHOW_MAX_OUTSTANDING.equals(con.getConfigCode())
                    && con.getHasLink() == 1)
                this.SHOW_MAX_OUTSTANDING = true;
            else if (CODE_CREDIT_INVOICE_COUNT.equals(con.getConfigCode())
                    && con.getHasLink() == 1)
                this.SHOW_CREDIT_INVOICE_COUNT = true;
            else if (CODE_OUTLET_PROGRAM.equals(con.getConfigCode()) && con.isFlag() == 1)
                SHOW__OUTLET_PROGRAM = true;
            else if (CODE_RE_CLASSIFICATION.equals(con.getConfigCode()) && con.isFlag() == 1)
                SHOW__RE_CLASSIFICATION = true;
            else if (CODE_CONTACT_TITLE_LOVID1.equals(con.getConfigCode()) && con.isFlag() == 1)
                profileConfig.add(new ConfigureBO("CT1TITLE", "Contact1_title", "", 1, 1, 1));
            else if (CODE_CONTACT_TITLE_LOVID2.equals(con.getConfigCode()) && con.isFlag() == 1)
                profileConfig.add(new ConfigureBO("CT2TITLE", "Contact2_title", "", 1, 1, 1));

        }
    }

    /**
     * Download the configurtion codes from the HHTModuleMaster, and update the
     * flags
     * <p>
     * It will also call OrderAndStockConfiguration and DateFormat
     */
    public void downloadConfig() {

        try {
            resetHHTModuleValue();

            ConfigureBO con;

            String sql = "select hhtCode, flag, RField,menu_type from "
                    + DataMembers.tbl_HhtModuleMaster + " Where ForSwitchSeller = 0";

            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            config = new Vector<>();

            Cursor c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {

                while (c.moveToNext()) {
                    con = new ConfigureBO();
                    con.setConfigCode(c.getString(0));
                    con.setFlag(c.getInt(1));
                    con.setModule_Order(c.getInt(2));
                    con.setMenu_type(c.getString(3));
                    config.add(con);
                }
                configStatusTest();
                c.close();
            }
            db.closeDB();
            loadOrderAndStockConfiguration(0);
            loadOrderReasonDialog();
            getDateFormat();
        } catch (Exception e) {
            Commons.printException("Unable to load the configurations " + e);
        }
    }

    public void downloadSwitchConfig() {

        try {
            SchemeDetailsMasterHelper schemeDetailsMasterHelper = SchemeDetailsMasterHelper.getInstance(context);
            this.IS_SIH_VALIDATION = false;
            this.IS_STOCK_IN_HAND = false;
            schemeDetailsMasterHelper.IS_SCHEME_ON = false;
            schemeDetailsMasterHelper.IS_SCHEME_SHOW_SCREEN = false;
            this.SHOW_TAX = false;
            this.IS_GST = false;
            this.SHOW_STORE_WISE_DISCOUNT_DLG = false;
            this.SHOW_TOTAL_DISCOUNT_EDITTEXT = false;
            this.IS_WSIH = false;
            this.IS_INVOICE = false;
            this.IS_STOCK_AVAILABLE_PRODUCTS_ONLY = false;

            ConfigureBO con;

            String sql = "select hhtCode, flag, RField,menu_type from "
                    + DataMembers.tbl_HhtModuleMaster + " Where ForSwitchSeller = 1";

            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();


            Vector<ConfigureBO> config = new Vector<>();

            Cursor c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                while (c.moveToNext()) {
                    con = new ConfigureBO();
                    con.setConfigCode(c.getString(0));
                    con.setFlag(c.getInt(1));
                    con.setModule_Order(c.getInt(2));
                    con.setMenu_type(c.getString(3));
                    config.add(con);
                }
                c.close();
            }

            for (ConfigureBO configureBO : config) {
                if (configureBO.isFlag() == 1) {
                    if (configureBO.getConfigCode().equals(CODE_SIH_VALIDATION))
                        this.IS_SIH_VALIDATION = true;
                    if (configureBO.getConfigCode().equals(CODE_STOCK_IN_HAND))
                        this.IS_STOCK_IN_HAND = true;
                    if (configureBO.getConfigCode().equals("SCH01"))
                        schemeDetailsMasterHelper.IS_SCHEME_ON = true;
                    if (configureBO.getConfigCode().equals("SCH03"))
                        schemeDetailsMasterHelper.IS_SCHEME_SHOW_SCREEN = true;
                    if (configureBO.getConfigCode().equals(CODE_TAX_APPLY))
                        this.SHOW_TAX = true;
                    if (configureBO.getConfigCode().equals(CODE_TAX_MODEL))
                        getTaxModelSwitchUser(CODE_TAX_MODEL);
                    if (configureBO.getConfigCode().equals(CODE_STORE_WISE_DISCOUNT_DIALOG))
                        this.SHOW_STORE_WISE_DISCOUNT_DLG = true;
                    if (configureBO.getConfigCode().equals(CODE_DISCOUNT_EDITVIEW))
                        this.SHOW_TOTAL_DISCOUNT_EDITTEXT = true;
                    if (configureBO.getConfigCode().equals(CODE_IS_WSIH))
                        this.IS_WSIH = true;
                    if (configureBO.getConfigCode().equals(CODE_INVOICE))
                        this.IS_INVOICE = true;
                    if (configureBO.getConfigCode().equals(CODE_SR_INDICATIVE))
                        this.IS_INDICATIVE_SR = true;
                    if (configureBO.getConfigCode().equals(CODE_SHOW_STOCK_AVAILABLE_PRODUCTS_ONLY))
                        this.IS_STOCK_AVAILABLE_PRODUCTS_ONLY = true;
                }

            }

            db.closeDB();

        } catch (Exception e) {
            Commons.printException("Unable to load the configurations " + e);
        }
    }

    /**
     * This method will the buffer value to calculate the SO. HttConfig will
     * have value in SOBUFFER.
     *
     * @return
     */
    public int downloadSOBuffer() {

        int buffer = 0;
        try {
            String sql = "select RField from "
                    + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode='SOBUFFER' and ForSwitchSeller = 0";
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                while (c.moveToNext()) {
                    buffer = c.getInt(0);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return buffer;

    }

    public String loadLocationWiseTaxApplied() {

        String code = "";
        try {
            String sql = "select RField from "
                    + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode='TAX02' and ForSwitchSeller = 0";
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                while (c.moveToNext()) {
                    code = c.getString(0);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return code;

    }

    public void downloadQDVP3ScoreConfig(String menutype) {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String language = sharedPrefs.getString("languagePref",
                ApplicationConfigs.LANGUAGE);
        try {
            String sql = "select hhtCode, MName from "
                    + DataMembers.tbl_HhtMenuMaster
                    + " where  flag=1 and MenuType=" + bmodel.QT(menutype)
                    + " and hhtCode=" + bmodel.QT("VST11")
                    + " and lang=" + bmodel.QT(language) + " order by RField";
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                while (c.moveToNext()) {
                    CALC_QDVP3 = true;
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }

    }

    /**
     * This method will return RFiled6 column value from the HHTMenuMaster table.
     *
     * @return boolean true - survey is required.
     */
    public boolean downloadFloatingSurveyConfig(String moduleCode) {

        try {
            String sql = "select RField6 from " + DataMembers.tbl_HhtMenuMaster
                    + " where hhtCode=" + bmodel.QT(moduleCode);
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                while (c.moveToNext()) {
                    if (c.getInt(0) == 1) {
                        floating_Survey = true;
                        return true;
                    }

                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        floating_Survey = false;
        return false;

    }

    /**
     * This method will return Regex column value from the hhtmenuMaster table.
     *
     * @return boolean false - npReasonwith photo is not required.
     * boolean true - npReasonwith photo is required.
     */
    public void downloadFloatingNPReasonWithPhoto(String moduleCode) {
        floating_np_reason_photo = false;
        try {
            String sql = "select Regex from " + DataMembers.tbl_HhtMenuMaster
                    + " where hhtCode=" + bmodel.QT(moduleCode);
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                while (c.moveToNext()) {
                    if (c.getString(0).equals("1")) {
                        floating_np_reason_photo = true;
                    }

                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }


    }


    private void configStatusTest() {
        int siz = config.size();
        if (siz == 0)
            return;

        ConfigureBO con;
        for (int i = 0; i < siz; ++i) {
            con = config.get(i);

            if (con.isFlag() == 1) {
                hashMapHHTModuleConfig.put(con.getConfigCode(), true);
                hashMapHHTModuleOrder.put(con.getConfigCode(), con.getModule_Order());
            }
        }

        if (hashMapHHTModuleConfig.size() > 0 && hashMapHHTModuleOrder.size() > 0)
            setHHTModuleValue();
    }

    private void resetHHTModuleValue() {
        hashMapHHTModuleConfig = new HashMap<>();
        hashMapHHTModuleOrder = new HashMap<>();
        setHHTModuleValue();
    }

    private void setHHTModuleValue() {
        String CODE_PHOTO_CAPTURE_PERCENT = "PHOTOCAP06";
        String CODE_SKUWISE_INCENTIVE = "FUN41";
        String CODE_CALCULATE_OUTSTANDING = "FUN42";
        // Show Attendance Module
        String SHOW_ATTENDANCE_MOD = "ATTENDANCE";

        this.SHOW_GPS_ENABLE_DIALOG = hashMapHHTModuleConfig.get(CODE_GPS_ENABLE) != null ? hashMapHHTModuleConfig.get(CODE_GPS_ENABLE) : false;
        this.IS_INVOICE = hashMapHHTModuleConfig.get(CODE_INVOICE) != null ? hashMapHHTModuleConfig.get(CODE_INVOICE) : false;
        this.IS_INVOICE_MASTER = hashMapHHTModuleConfig.get(CODE_INVOICE) != null ? hashMapHHTModuleConfig.get(CODE_INVOICE) : false;
        this.IS_MAP = hashMapHHTModuleConfig.get(CODE_MAP) != null ? hashMapHHTModuleConfig.get(CODE_MAP) : false;
        this.IS_BAIDU_MAP = hashMapHHTModuleConfig.get(CODE_BAIDU_MAP) != null ? hashMapHHTModuleConfig.get(CODE_BAIDU_MAP) : false;
        this.IS_NEW_TASK = hashMapHHTModuleConfig.get(CODE_NEW_TASK) != null ? hashMapHHTModuleConfig.get(CODE_NEW_TASK) : false;
        this.IS_RETAILER_DEVIATION = hashMapHHTModuleConfig.get(CODE_RETAILER_DEVIATION) != null ? hashMapHHTModuleConfig.get(CODE_RETAILER_DEVIATION) : false;
        this.IS_SUGGESTED_ORDER = hashMapHHTModuleConfig.get(CODE_SUGGESTED_ORDER) != null ? hashMapHHTModuleConfig.get(CODE_SUGGESTED_ORDER) : false;
        this.IS_SUGGESTED_ORDER_LOGIC = hashMapHHTModuleConfig.get(CODE_SUGGESTED_ORDER_LOGIC) != null ? hashMapHHTModuleConfig.get(CODE_SUGGESTED_ORDER_LOGIC) : false;
        this.IS_DELIVERY_REPORT = hashMapHHTModuleConfig.get(CODE_DELIVERY_REPORT) != null ? hashMapHHTModuleConfig.get(CODE_DELIVERY_REPORT) : false;
        this.IS_TASK = hashMapHHTModuleConfig.get(CODE_TASK) != null ? hashMapHHTModuleConfig.get(CODE_TASK) : false;
        this.IS_PHOTO_CAPTURE = hashMapHHTModuleConfig.get(CODE_PHOTO_CAPTURE) != null ? hashMapHHTModuleConfig.get(CODE_PHOTO_CAPTURE) : false;
        this.IS_PHOTO_CAPTURE_IMG_PATH_CHANGE = hashMapHHTModuleConfig.get(CODE_PHOTO_CAPTURE_IMG_PATH) != null ? hashMapHHTModuleConfig.get(CODE_PHOTO_CAPTURE_IMG_PATH) : false;
        this.IS_PHOTO_COMPETITOR = hashMapHHTModuleConfig.get(CODE_PHOTO_COMPETITOR) != null ? hashMapHHTModuleConfig.get(CODE_PHOTO_COMPETITOR) : false;
        this.IS_JUMP = hashMapHHTModuleConfig.get(CODE_JUMP) != null ? hashMapHHTModuleConfig.get(CODE_JUMP) : false;
        this.IS_VISITSCREEN_DEV_ALLOW = hashMapHHTModuleConfig.get(CODE_VISITSCREEN_DEV_ALLOW) != null ? hashMapHHTModuleConfig.get(CODE_VISITSCREEN_DEV_ALLOW) : false;
        this.IS_DATE_VALIDATION_REQUIRED = hashMapHHTModuleConfig.get(CODE_DAY_MISMATCH) != null ? hashMapHHTModuleConfig.get(CODE_DAY_MISMATCH) : false;
        this.IS_INITIATIVE = hashMapHHTModuleConfig.get(CODE_INITIATIVE) != null ? hashMapHHTModuleConfig.get(CODE_INITIATIVE) : false;
        this.IS_CHAT_ENABLED = hashMapHHTModuleConfig.get(CODE_CHAT) != null ? hashMapHHTModuleConfig.get(CODE_CHAT) : false;
        this.IS_PRESENTATION_INORDER = hashMapHHTModuleConfig.get(CODE_PRASENTATION_INORDER) != null ? hashMapHHTModuleConfig.get(CODE_PRASENTATION_INORDER) : false;
        this.HAS_PROFILE_BUTTON_IN_RETAILER_LIST = hashMapHHTModuleConfig.get(CODE_HAS_PROFILE_BUTTON_IN_RETAILER_LIST) != null ? hashMapHHTModuleConfig.get(CODE_HAS_PROFILE_BUTTON_IN_RETAILER_LIST) : false;
        this.SHOW_ALL_ROUTES = hashMapHHTModuleConfig.get(CODE_SHOW_ALL_ROUTES) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_ALL_ROUTES) : false;
        this.SHOW_RETAILER_VISIT_CONFIRMATION = hashMapHHTModuleConfig.get(CODE_RETAILER_VISIT_CONFIRMATION) != null ? hashMapHHTModuleConfig.get(CODE_RETAILER_VISIT_CONFIRMATION) : false;
        this.HAS_NO_VISIT_REASON_VALIDATION = hashMapHHTModuleConfig.get(CODE_HAS_NO_VISIT_REASON_VALIDATION) != null ? hashMapHHTModuleConfig.get(CODE_HAS_NO_VISIT_REASON_VALIDATION) : false;
        this.SHOW_MONTH_OBJ_PROFILE = hashMapHHTModuleConfig.get(CODE_SHOW_MONTH_OBJ_PROFILE) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_MONTH_OBJ_PROFILE) : false;
        this.SHOW_CREDIT_LIMIT_PROFILE = hashMapHHTModuleConfig.get(CODE_SHOW_CREDIT_LIMIT_PROFILE) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_CREDIT_LIMIT_PROFILE) : false;
        this.SHOW_CREDIT_BALANCE = hashMapHHTModuleConfig.get(CODE_SHOW_CREDIT_BALANCE) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_CREDIT_BALANCE) : false;
        this.SHOW_NO_VISIT_REASON = hashMapHHTModuleConfig.get(CODE_SHOW_NO_VISIT_REASON) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_NO_VISIT_REASON) : false;
        this.SHOW_ORDER_HISTORY = hashMapHHTModuleConfig.get(CODE_SHOW_ORDER_HISTORY) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_ORDER_HISTORY) : false;
        this.SHOW_TASK = hashMapHHTModuleConfig.get(CODE_SHOW_TASK) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_TASK) : false;
        this.SHOW_AVG_SALES_PER_LEVEL = hashMapHHTModuleConfig.get(CODE_SHOW_AVG_SALES_PER_LEVEL) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_AVG_SALES_PER_LEVEL) : false;

        this.SHOW_SALES_VALUE_DR = hashMapHHTModuleConfig.get(CODE_SHOW_SALES_VALUE_DR) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_SALES_VALUE_DR) : false;

        this.SHOW_ASSET_HISTORY = hashMapHHTModuleConfig.get(CODE_SHOW_ASSET_HISTORY) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_ASSET_HISTORY) : false;
        this.SHOW_PROFILE_EDIT = hashMapHHTModuleConfig.get(CODE_SHOW_EDIT_PRO) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_EDIT_PRO) : false;
        this.IS_DB_BACKUP = hashMapHHTModuleConfig.get(COBE_DB_BACKUP) != null ? hashMapHHTModuleConfig.get(COBE_DB_BACKUP) : false;
        this.SHOW_LPC_ORDER = hashMapHHTModuleConfig.get(CODE_SHOW_LPC_ORDER) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_LPC_ORDER) : false;
        this.SHOW_INIT_FOOTER = hashMapHHTModuleConfig.get(CODE_SHOW_INIT_FOOTER) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_INIT_FOOTER) : false;
        this.SHOW_REVIEW_AND_PO = hashMapHHTModuleConfig.get(CODE_SHOW_REVIEW_PO) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_REVIEW_PO) : false;
        this.SHOW_HIGHLIGHT_FOR_OOS = hashMapHHTModuleConfig.get(CODE_SHOW_HIGHLIGHT_FOR_OOS) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_HIGHLIGHT_FOR_OOS) : false;
        this.IS_PRODUCT_DISCOUNT_BY_USER_ENTRY = hashMapHHTModuleConfig.get(CODE_SHOW_PRODUCT_DISCOUNT_DIALOG) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_PRODUCT_DISCOUNT_DIALOG) : false;
        this.SHOW_DISCOUNT_ACTIVITY = hashMapHHTModuleConfig.get(CODE_SHOW_DISCOUNT_ACTIVITY) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_DISCOUNT_ACTIVITY) : false;

        //this.SHOW_INITIATIVE_MERCHANDISING = hashMapHHTModuleConfig.get(CODE_INITIATIVE_MERCHANDISING) != null ? hashMapHHTModuleConfig.get(CODE_INITIATIVE_MERCHANDISING) : false;
        this.SHOW_CALC = hashMapHHTModuleConfig.get(CODE_CALCULATOR) != null ? hashMapHHTModuleConfig.get(CODE_CALCULATOR) : false;
        this.IS_MUST_SELL = hashMapHHTModuleConfig.get(CODE_MUST_SELL) != null ? hashMapHHTModuleConfig.get(CODE_MUST_SELL) : false;
        this.SHOW_STK_ORD_SRP = hashMapHHTModuleConfig.get(CODE_SHOW_STK_ORD_SRP) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_STK_ORD_SRP) : false;
        this.SHOW_MVP_DRAWER = hashMapHHTModuleConfig.get(CODE_SHOW_MVP_DRAWER) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_MVP_DRAWER) : false;
        this.SHOW_REMARKS_STK_ORD = hashMapHHTModuleConfig.get(CODE_SHOW_REMARKS_STK_ORD) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_REMARKS_STK_ORD) : false;
        this.SHOW_REMARKS_STK_CHK = hashMapHHTModuleConfig.get(CODE_SHOW_REMARKS_STK_CHK) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_REMARKS_STK_CHK) : false;
        this.SHOW_LOCATION_PASSWORD_DIALOG = hashMapHHTModuleConfig.get(CODE_SHOW_LOCATION_PWD_DIALOG) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_LOCATION_PWD_DIALOG) : false;
        this.SHOW_CHART_DASH = hashMapHHTModuleConfig.get(CODE_SHOW_CHART_DASH) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_CHART_DASH) : false;
        this.SHOW_LINK_DASH_SKUTGT = hashMapHHTModuleConfig.get(CODE_SHOW_LINK_DASH_SKUTGT) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_LINK_DASH_SKUTGT) : false;
        this.IS_MUST_STOCK = hashMapHHTModuleConfig.get(CODE_IS_MUST_STOCK) != null ? hashMapHHTModuleConfig.get(CODE_IS_MUST_STOCK) : false;
        this.DISABLE_MANUAL_ORDER = hashMapHHTModuleConfig.get(CODE_DISABLE_MANUAL_ENTRY) != null ? hashMapHHTModuleConfig.get(CODE_DISABLE_MANUAL_ENTRY) : false;
        this.MUST_STOCK_ONLY = hashMapHHTModuleConfig.get(CODE_MUST_STOCK_ONLY) != null ? hashMapHHTModuleConfig.get(CODE_MUST_STOCK_ONLY) : false;
        this.SHOW_SO_APPLY = hashMapHHTModuleConfig.get(CODE_STOCK_SO_APPLY) != null ? hashMapHHTModuleConfig.get(CODE_STOCK_SO_APPLY) : false;
        this.STOCK_MAX_VALID = hashMapHHTModuleConfig.get(CODE_STOCK_MAX_LIMIT) != null ? hashMapHHTModuleConfig.get(CODE_STOCK_MAX_LIMIT) : false;
        this.SHOW_STD_QTY_APPLY = hashMapHHTModuleConfig.get(CODE_STOCK_STDQTY_APPLY) != null ? hashMapHHTModuleConfig.get(CODE_STOCK_STDQTY_APPLY) : false;
        this.SHOW_SAL_RET_REASON_DLG = hashMapHHTModuleConfig.get(CODE_SAL_RET_REASON_DLG) != null ? hashMapHHTModuleConfig.get(CODE_SAL_RET_REASON_DLG) : false;
        this.STOCK_APPROVAL = hashMapHHTModuleConfig.get(CODE_STOCK_APPROVAL) != null ? hashMapHHTModuleConfig.get(CODE_STOCK_APPROVAL) : false;
        this.SHOW_END_JOURNEY = hashMapHHTModuleConfig.get(CODE_END_JOURNEY) != null ? hashMapHHTModuleConfig.get(CODE_END_JOURNEY) : false;
        this.SHOW_RETAILER_SELECTION_VALID = hashMapHHTModuleConfig.get(CODE_RETAILER_SELECTION_VALID) != null ? hashMapHHTModuleConfig.get(CODE_RETAILER_SELECTION_VALID) : false;
        this.SHOW_CLOSE_DAY_VALID = hashMapHHTModuleConfig.get(CODE_CLOSE_DAY_VALID) != null ? hashMapHHTModuleConfig.get(CODE_CLOSE_DAY_VALID) : false;
        this.SHOW_BATCH_ALLOCATION = hashMapHHTModuleConfig.get(CODE_BATCH_ALLOCATION) != null ? hashMapHHTModuleConfig.get(CODE_BATCH_ALLOCATION) : false;
        this.SHOW_INVOICE_CREDIT_BALANCE = hashMapHHTModuleConfig.get(CODE_INV_CREDIT_BALANCE) != null ? hashMapHHTModuleConfig.get(CODE_INV_CREDIT_BALANCE) : false;
        //this.SHOW_VANBARCODE_VALIDATION = hashMapHHTModuleConfig.get(CODE_VANBARCODE_VALIDATION) != null ? hashMapHHTModuleConfig.get(CODE_VANBARCODE_VALIDATION) : false;
        this.IS_POST_DATE_ALLOW = hashMapHHTModuleConfig.get(CODE_POST_DATE_ALLOW) != null ? hashMapHHTModuleConfig.get(CODE_POST_DATE_ALLOW) : false;
        this.SHOW_DELIVERY_DATE = hashMapHHTModuleConfig.get(CODE_DELIVERY_DATE) != null ? hashMapHHTModuleConfig.get(CODE_DELIVERY_DATE) : false;
        this.IS_NEWOUTLET_IMAGETYPE = hashMapHHTModuleConfig.get(CODE_NEWOUTLET_IMAGETYPE) != null ? hashMapHHTModuleConfig.get(CODE_NEWOUTLET_IMAGETYPE) : false;
        this.IS_NEWOUTLET_LOCATION = hashMapHHTModuleConfig.get(CODE_NEWOUTLET_LOCATION) != null ? hashMapHHTModuleConfig.get(CODE_NEWOUTLET_LOCATION) : false;
        this.SHOW_BATCH_WISE_PRICE = hashMapHHTModuleConfig.get(CODE_BATCH_WISE_PRODUCT) != null ? hashMapHHTModuleConfig.get(CODE_BATCH_WISE_PRODUCT) : false;
        this.SHOW_SIGNATURE_SCREEN = hashMapHHTModuleConfig.get(CODE_SIGNATURE_SCREEN) != null ? hashMapHHTModuleConfig.get(CODE_SIGNATURE_SCREEN) : false;
        this.SHOW_DISC_AMOUNT_ALLOW = hashMapHHTModuleConfig.get(CODE_DISC_AMOUNT_ALLOW) != null ? hashMapHHTModuleConfig.get(CODE_DISC_AMOUNT_ALLOW) : false;
        this.IS_FULL_PAYMENT = hashMapHHTModuleConfig.get(CODE_FULL_PAYMENT) != null ? hashMapHHTModuleConfig.get(CODE_FULL_PAYMENT) : false;
        this.SHOW_SKUWISE_INCENTIVE = hashMapHHTModuleConfig.get(CODE_SKUWISE_INCENTIVE) != null ? hashMapHHTModuleConfig.get(CODE_SKUWISE_INCENTIVE) : false;
        this.CALC_OUTSTANDING = hashMapHHTModuleConfig.get(CODE_CALCULATE_OUTSTANDING) != null ? hashMapHHTModuleConfig.get(CODE_CALCULATE_OUTSTANDING) : false;
        this.SHOW_COLLECTION_SLAB = hashMapHHTModuleConfig.get(CODE_SHOW_COLLECTION_SLAB) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_COLLECTION_SLAB) : false;
        this.IS_COLLECTION_ORDER = hashMapHHTModuleConfig.get(CODE_COLLECTION_ORDER) != null ? hashMapHHTModuleConfig.get(CODE_COLLECTION_ORDER) : false;
        this.SHOW_COLLECTION_REASON = hashMapHHTModuleConfig.get(CODE_COLLECTION_REASON) != null ? hashMapHHTModuleConfig.get(CODE_COLLECTION_REASON) : false;
        this.SHOW_DEVICE_STATUS = hashMapHHTModuleConfig.get(CODE_DEVICE_STATUS) != null ? hashMapHHTModuleConfig.get(CODE_DEVICE_STATUS) : false;
        this.SHOW_STKPRO_SPL_FILTER = hashMapHHTModuleConfig.get(CODE_STKPRO_SPL_FILTER) != null ? hashMapHHTModuleConfig.get(CODE_STKPRO_SPL_FILTER) : false;
        this.SHOW_SO_SPLIT = hashMapHHTModuleConfig.get(CODE_SO_SPLIT) != null ? hashMapHHTModuleConfig.get(CODE_SO_SPLIT) : false;
        this.ALLOW_SO_COPY = hashMapHHTModuleConfig.get(CODE_SO_COPY) != null ? hashMapHHTModuleConfig.get(CODE_SO_COPY) : false;
        this.ALLOW_BACK_DATE = hashMapHHTModuleConfig.get(CODE_ALLOW_BACK_DATE) != null ? hashMapHHTModuleConfig.get(CODE_ALLOW_BACK_DATE) : false;
        this.SHOW_TOTAL_LINES = hashMapHHTModuleConfig.get(CODE_TOTAL_LINES) != null ? hashMapHHTModuleConfig.get(CODE_TOTAL_LINES) : false;
        if (hashMapHHTModuleOrder.get(CODE_TOTAL_LINES) != null) {
            if (hashMapHHTModuleOrder.get(CODE_TOTAL_LINES) == 1)
                SHOW_TOTAL_QTY_IN_ORDER_REPORT = true;
        }
        this.SHOW_MULTIPAYMENT = hashMapHHTModuleConfig.get(CODE_SHOW_MULTIPAYMENT) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_MULTIPAYMENT) : false;
        this.SHOW_DOWNLOAD_ALERT = hashMapHHTModuleConfig.get(CODE_DOWNLOAD_ALERT) != null ? hashMapHHTModuleConfig.get(CODE_DOWNLOAD_ALERT) : false;
        this.SHOW_SIH_IN_PNAME = hashMapHHTModuleConfig.get(CODE_SHOW_SIH_IN_FNAME) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_SIH_IN_FNAME) : false;
        this.SHOW_SYNC_RETAILER_SELECT = hashMapHHTModuleConfig.get(CODE_SHOW_SYNC_RETAILER_SELECT) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_SYNC_RETAILER_SELECT) : false;
        this.SHOW_CURRENT_STDQTY = hashMapHHTModuleConfig.get(CODE_CURRENT_STDQTY_APPLY) != null ? hashMapHHTModuleConfig.get(CODE_CURRENT_STDQTY_APPLY) : false;
        this.SHOW_VALIDATION_DIST_INV = hashMapHHTModuleConfig.get(CODE_VALIDATE_DIST_INV) != null ? hashMapHHTModuleConfig.get(CODE_VALIDATE_DIST_INV) : false;
        this.SHOW_SYNC_EXPORT_TXT = hashMapHHTModuleConfig.get(CODE_SHOW_SYNC_EXPORT_TXT) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_SYNC_EXPORT_TXT) : false;
        this.SHOW_SYNC_DAYCLOSE = hashMapHHTModuleConfig.get(CODE_SHOW_SYNC_DAYCLOSE) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_SYNC_DAYCLOSE) : false;
        this.SHOW_OBJECTIVE = hashMapHHTModuleConfig.get(CODE_SHOW_OBJECTIVE) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_OBJECTIVE) : false;
        this.SHOW_DEVIATION = hashMapHHTModuleConfig.get(CODE_SHOW_DEVIATION) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_DEVIATION) : false;
        this.IS_CREDIT_NOTE_CREATION = hashMapHHTModuleConfig.get(CODE_CREDIT_NOTE_CREATION) != null ? hashMapHHTModuleConfig.get(CODE_CREDIT_NOTE_CREATION) : false;
        this.SHOW_STORE_WISE_DISCOUNT_DLG = hashMapHHTModuleConfig.get(CODE_STORE_WISE_DISCOUNT_DIALOG) != null ? hashMapHHTModuleConfig.get(CODE_STORE_WISE_DISCOUNT_DIALOG) : false;
        this.SHOW_STORE_WISE_DISCOUNT_DLG_MASTER = hashMapHHTModuleConfig.get(CODE_STORE_WISE_DISCOUNT_DIALOG) != null ? hashMapHHTModuleConfig.get(CODE_STORE_WISE_DISCOUNT_DIALOG) : false;
        this.SHOW_STOCK_PRO_CREDIT_VALIDATION = hashMapHHTModuleConfig.get(CODE_STOCK_PRO_CREDIT_VALIDATION) != null ? hashMapHHTModuleConfig.get(CODE_STOCK_PRO_CREDIT_VALIDATION) : false;
        this.REMOVE_INVOICE = hashMapHHTModuleConfig.get(CODE_REMOVE_INVOICE) != null ? hashMapHHTModuleConfig.get(CODE_REMOVE_INVOICE) : false;
        this.SHEME_NOT_APPLY_DEVIATEDSTORE = hashMapHHTModuleConfig.get(CODE_DEVIATE_STORE_SCHEME_NOT_APPLY) != null ? hashMapHHTModuleConfig.get(CODE_DEVIATE_STORE_SCHEME_NOT_APPLY) : false;
        this.SHOW_SALES_RETURN_IN_INVOICE = hashMapHHTModuleConfig.get(CODE_SALES_RETURN_IN_INVOICE) != null ? hashMapHHTModuleConfig.get(CODE_SALES_RETURN_IN_INVOICE) : false;
        this.SHOW_CREDIT_INVOICE_COUNT = hashMapHHTModuleConfig.get(CODE_CREDIT_INVOICE_COUNT) != null ? hashMapHHTModuleConfig.get(CODE_CREDIT_INVOICE_COUNT) : false;

        IS_CLOUD_STORAGE_AVAILABLE = hashMapHHTModuleConfig.get(CODE_CLOUD_STORAGE) != null ? hashMapHHTModuleConfig.get(CODE_CLOUD_STORAGE) : false;
        if (IS_CLOUD_STORAGE_AVAILABLE) {
            int cloudStorageType = hashMapHHTModuleOrder.get(CODE_CLOUD_STORAGE);
            if (cloudStorageType == 0) {
                IS_S3_CLOUD_STORAGE = true;
            } else if (cloudStorageType == 1) {
                IS_SFDC_CLOUD_STORAGE = true;
            } else if (cloudStorageType == 2) {
                IS_AZURE_CLOUD_STORAGE = true;
            } else {
                // default flag
                IS_S3_CLOUD_STORAGE = true;
            }
        }


        this.IS_MULTIPLE_JOINCALL = hashMapHHTModuleConfig.get(CODE_MULTIPLE_JOINCALL) != null ? hashMapHHTModuleConfig.get(CODE_MULTIPLE_JOINCALL) : false;
        this.IS_ALLOW_SURVEY_WITHOUT_JOINTCALL = hashMapHHTModuleConfig.get(CODE_ALLOW_SURVEY) != null ? hashMapHHTModuleConfig.get(CODE_ALLOW_SURVEY) : false;
        if (hashMapHHTModuleConfig.get(CODE_JOINT_CALL_LEVELS) != null) {
            getUserLevel(CODE_JOINT_CALL_LEVELS);
        }
        this.SHOW_ATTENDANCE = hashMapHHTModuleConfig.get(SHOW_ATTENDANCE_MOD) != null ? hashMapHHTModuleConfig.get(SHOW_ATTENDANCE_MOD) : false;
        this.TAX_SHOW_INVOICE = hashMapHHTModuleConfig.get(SHOW_TAX_INVOICE) != null ? hashMapHHTModuleConfig.get(SHOW_TAX_INVOICE) : false;
        this.IS_SURVEY_GLOBAL_SAVE = hashMapHHTModuleConfig.get(SURVEY_GLOBAL_SAVE) != null ? hashMapHHTModuleConfig.get(SURVEY_GLOBAL_SAVE) : false;
        this.HIDE_STOCK_APPLY_BUTTON = hashMapHHTModuleConfig.get(CODE_HIDE_STOCK_APPLY_BUTTON) != null ? hashMapHHTModuleConfig.get(CODE_HIDE_STOCK_APPLY_BUTTON) : false;
        this.SHOW_PRODUCTRETURN = hashMapHHTModuleConfig.get(CODE_SHOW_PRODUCT_RETRUN) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_PRODUCT_RETRUN) : false;
        this.SHOW_GROUPPRODUCTRETURN = hashMapHHTModuleConfig.get(CODE_SHOW_GROUPPRODUCT_RETRUN) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_GROUPPRODUCT_RETRUN) : false;
        this.HAS_SELLER_TYPE_SELECTION_ENABLED = hashMapHHTModuleConfig.get(CODE_SHOW_SELLER_DIALOG) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_SELLER_DIALOG) : false;

        this.IS_SWITCH_SELLER_CONFIG_LEVEL = hashMapHHTModuleConfig.get(CODE_CHNAGE_SELLER_CONFIG_LEVEL) != null ? hashMapHHTModuleConfig.get(CODE_CHNAGE_SELLER_CONFIG_LEVEL) : false;
        if (IS_SWITCH_SELLER_CONFIG_LEVEL) {
            if (hashMapHHTModuleOrder.get(CODE_CHNAGE_SELLER_CONFIG_LEVEL) != null)
                this.switchConfigLevel = hashMapHHTModuleOrder.get(CODE_CHNAGE_SELLER_CONFIG_LEVEL);
        }

        this.IS_VALIDATE_CREDIT_DAYS = hashMapHHTModuleConfig.get(CODE_SHOW_VALIDATE_CREDIT_DAYS) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_VALIDATE_CREDIT_DAYS) : false;
        this.SHOW_UNIT_PRICE = hashMapHHTModuleConfig.get(CODE_SHOW_UNIT_PRICE) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_UNIT_PRICE) : false;

        this.SHOW_CROWN_MANAGMENT = hashMapHHTModuleConfig.get(CODE_SHOW_CROWN) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_CROWN) : false;
        this.SHOW_BOTTLE_CREDITLIMIT = hashMapHHTModuleConfig.get(CODE_SHOW_BOTTLE_CREDITLIMIT) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_BOTTLE_CREDITLIMIT) : false;
        this.SHOW_ADVANCE_PAYMENT = hashMapHHTModuleConfig.get(CODE_ADVANCE_PAYMENT) != null ? hashMapHHTModuleConfig.get(CODE_ADVANCE_PAYMENT) : false;
        this.SHOW_FREE_PRODUCT_GIVEN = hashMapHHTModuleConfig.get(CODE_SHOW_FREE_PRODUCT_GIVEN) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_FREE_PRODUCT_GIVEN) : false;
        this.IS_ADD_NEW_BATCH = hashMapHHTModuleConfig.get(CODE_ADD_NEW_BATCH) != null ? hashMapHHTModuleConfig.get(CODE_ADD_NEW_BATCH) : false;
        this.SHOW_PRINT_BUTTON = hashMapHHTModuleConfig.get(CODE_SHOW_PRINT) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_PRINT) : false;
        this.SHOW_BUTTON_PRINT01 = hashMapHHTModuleConfig.get(CODE_SHOW_PRINTRPT01) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_PRINTRPT01) : false;
        this.SHOW_SIH_SPLIT = hashMapHHTModuleConfig.get(CODE_SIH_SPLIT) != null ? hashMapHHTModuleConfig.get(CODE_SIH_SPLIT) : false;
        this.SHOW_GOLD_STORE_DISCOUNT = hashMapHHTModuleConfig.get(CODE_APPLY_GOLD_STORE_DISCOUNT) != null ? hashMapHHTModuleConfig.get(CODE_APPLY_GOLD_STORE_DISCOUNT) : false;

        this.IS_SHOW_REJECT_BTN = hashMapHHTModuleConfig.get(CODE_SHOW_REJECT_BTN) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_REJECT_BTN) : false;
        this.CALCULATE_UNLOAD = hashMapHHTModuleConfig.get(CODE_CALCULATE_UNLOAD) != null ? hashMapHHTModuleConfig.get(CODE_CALCULATE_UNLOAD) : false;
        this.SHOW_SUPPLIER_SELECTION = hashMapHHTModuleConfig.get(CODE_SHOW_SUPPLIER_SELECTION) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_SUPPLIER_SELECTION) : false;
        if (hashMapHHTModuleConfig.get(CODE_SHOW_SUPPLIER_SELECTION) != null) {
            if (hashMapHHTModuleOrder.get(CODE_SHOW_SUPPLIER_SELECTION) == 1)
                this.IS_SUPPLIER_NOT_AVAILABLE = true;
        }
        this.SHOW_USER_TASK = hashMapHHTModuleConfig.get(CODE_SHOW_USER_TASK) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_USER_TASK) : false;
        this.SHOW_JOINT_CALL = hashMapHHTModuleConfig.get(CODE_SHOW_JOINT_CALL) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_JOINT_CALL) : false;
        this.SHOW_NEAREXPIRY_IN_STOCKCHECK = hashMapHHTModuleConfig.get(CODE_NEAREXPIRY_IN_STOCKCHECK) != null ? hashMapHHTModuleConfig.get(CODE_NEAREXPIRY_IN_STOCKCHECK) : false;
        this.SHOW_VANLOAD_LABELS = hashMapHHTModuleConfig.get(CODE_VANLOAD_LABELS) != null ? hashMapHHTModuleConfig.get(CODE_VANLOAD_LABELS) : false;
        this.SHOW_COLLECTION_BEFORE_INVOICE = hashMapHHTModuleConfig.get(CODE_SHOW_COLLECTION_BEFOREINVOICE) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_COLLECTION_BEFOREINVOICE) : false;
        this.IS_VALIDATE_NEGATIVE_INVOICE = hashMapHHTModuleConfig.get(CODE_VALIDATE_NEGATIVE_INVOICE) != null ? hashMapHHTModuleConfig.get(CODE_VALIDATE_NEGATIVE_INVOICE) : false;
        this.SHOW_GCM_NOTIFICATION = hashMapHHTModuleConfig.get(CODE_SHOW_GCM_NOTIFICATION) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_GCM_NOTIFICATION) : false;
        this.SHOW_PREV_ORDER_REPORT = hashMapHHTModuleConfig.get(CODE_SHOW_PREV_ORDER_REPORT) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_PREV_ORDER_REPORT) : false;
        this.IS_DELETE_TABLE = hashMapHHTModuleConfig.get(CODE_DELETE_TABLE_ADHOC) != null ? hashMapHHTModuleConfig.get(CODE_DELETE_TABLE_ADHOC) : false;
        this.SHOW_INDICATIVE_ORDER_ICON = hashMapHHTModuleConfig.get(CODE_SHOW_INDICATIVE_ORDER_ICON) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_INDICATIVE_ORDER_ICON) : false;
        this.SHOW_DGTC = hashMapHHTModuleConfig.get(CODE_SHOW_DGTC) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_DGTC) : false;
        this.SHOW_INVOICE_SEQUENCE_NO = hashMapHHTModuleConfig.get(CODE_INVOICE_SEQUENCE_NUMBER) != null ? hashMapHHTModuleConfig.get(CODE_INVOICE_SEQUENCE_NUMBER) : false;

        this.SHOW_NON_SALABLE_PRODUCT = hashMapHHTModuleConfig.get(CODE_LOAD_NON_SALABLE_PRODUCTS) != null ? hashMapHHTModuleConfig.get(CODE_LOAD_NON_SALABLE_PRODUCTS) : false;

        this.SHOW_ORDER_SEQUENCE_NO = hashMapHHTModuleConfig.get(CODE_ORDER_SEQUENCE_NUMBER) != null ? hashMapHHTModuleConfig.get(CODE_ORDER_SEQUENCE_NUMBER) : false;
        this.SHOW_SR_SEQUENCE_NO = hashMapHHTModuleConfig.get(CODE_SR_SEQUENCE_NUMBER) != null ? hashMapHHTModuleConfig.get(CODE_SR_SEQUENCE_NUMBER) : false;
        this.SHOW_CN_SEQUENCE_NO = hashMapHHTModuleConfig.get(CODE_CN_SEQUENCE_NUMBER) != null ? hashMapHHTModuleConfig.get(CODE_CN_SEQUENCE_NUMBER) : false;
        this.SHOW_STOCK_IN_SUMMARY = hashMapHHTModuleConfig.get(CODE_SHOW_STOCK_IN_SUMMARY) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_STOCK_IN_SUMMARY) : false;
        this.IS_TEAMLEAD = hashMapHHTModuleConfig.get(CODE_IS_TEAMLEAD) != null ? hashMapHHTModuleConfig.get(CODE_IS_TEAMLEAD) : false;
        this.SHOW_DATE_BTN = hashMapHHTModuleConfig.get(CODE_SHOW_DATE_BTN) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_DATE_BTN) : false;
        this.IS_DIGITAL_CONTENT = hashMapHHTModuleConfig.get(CODE_DIGITAL_CONTENT) != null ? hashMapHHTModuleConfig.get(CODE_DIGITAL_CONTENT) : false;
        this.SHOW_NEW_OUTLET_UPLOAD = hashMapHHTModuleConfig.get(CODE_NEW_OUTLET_UPLOAD) != null ? hashMapHHTModuleConfig.get(CODE_NEW_OUTLET_UPLOAD) : false;
        this.SHOW_DISCOUNT = hashMapHHTModuleConfig.get(CODE_DISCOUNT_APPLY) != null ? hashMapHHTModuleConfig.get(CODE_DISCOUNT_APPLY) : false;
        this.IS_GLOBAL_LOCATION = hashMapHHTModuleConfig.get(CODE_GLOBAL_LOCATION) != null ? hashMapHHTModuleConfig.get(CODE_GLOBAL_LOCATION) : false;
        this.IS_GLOBAL_CATEGORY = hashMapHHTModuleConfig.get(CODE_GLOBAL_CATEGORY) != null ? hashMapHHTModuleConfig.get(CODE_GLOBAL_CATEGORY) : false;
        this.SHOW_MRP_LEVEL_TAX = hashMapHHTModuleConfig.get(CODE_MRP_LEVEL_TAX) != null ? hashMapHHTModuleConfig.get(CODE_MRP_LEVEL_TAX) : false;
        this.SHOW_OUTLET_PLANNING_TAB = hashMapHHTModuleConfig.get(CODE_SHOW_OUTLET_PLANNING_TAB) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_OUTLET_PLANNING_TAB) : false;
        this.SHOW_PROFILE_LOC1 = hashMapHHTModuleConfig.get(CODE_PROFILE_LOC1) != null ? hashMapHHTModuleConfig.get(CODE_PROFILE_LOC1) : false;
        this.SHOW_PROFILE_LOC3 = hashMapHHTModuleConfig.get(CODE_PROFILE_LOC3) != null ? hashMapHHTModuleConfig.get(CODE_PROFILE_LOC3) : false;
        this.SHOW_MISSED_RETAILER = hashMapHHTModuleConfig.get(CODE_SHOW_MISSED_RETAILER) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_MISSED_RETAILER) : false;
        this.VALIDATE_TRADE_COVERAGE = hashMapHHTModuleConfig.get(CODE_VALIDATE_TRADE_COVERAGE) != null ? hashMapHHTModuleConfig.get(CODE_VALIDATE_TRADE_COVERAGE) : false;
        this.SUBD_RETAILER_SELECTION = hashMapHHTModuleConfig.get(CODE_SUBD_RETIALER_SELECTION) != null ? hashMapHHTModuleConfig.get(CODE_SUBD_RETIALER_SELECTION) : false;
        this.IS_SIMPLE_RETIALER = hashMapHHTModuleConfig.get(CODE_SIMPLE_RETAIER_ROW) != null ? hashMapHHTModuleConfig.get(CODE_SIMPLE_RETAIER_ROW) : false;
        this.SHOW_RETIALER_CONTACTS = hashMapHHTModuleConfig.get(CODE_RETAILER_CONTACT_NAME) != null ? hashMapHHTModuleConfig.get(CODE_RETAILER_CONTACT_NAME) : false;
        this.SHOW_RFIELD4 = hashMapHHTModuleConfig.get(CODE_SHOW_RETAILER_LOCATION) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_RETAILER_LOCATION) : false;
        this.SHOW_PROFILE_LOC2 = hashMapHHTModuleConfig.get(CODE_PROFILE_LOC2) != null ? hashMapHHTModuleConfig.get(CODE_PROFILE_LOC2) : false;
        this.SHOW_CONTRACT_TYPE = hashMapHHTModuleConfig.get(CODE_CONTRACT_TYPE) != null ? hashMapHHTModuleConfig.get(CODE_CONTRACT_TYPE) : false;
        this.SHOW_CONTRACT_EXPIRYDATE = hashMapHHTModuleConfig.get(CODE_CONTRACT_EXPIRYDATE) != null ? hashMapHHTModuleConfig.get(CODE_CONTRACT_EXPIRYDATE) : false;
        this.SHOW_VISIT_FREQUENCY = hashMapHHTModuleConfig.get(CODE_VISIT_FREQUENCY) != null ? hashMapHHTModuleConfig.get(CODE_VISIT_FREQUENCY) : false;
        this.SHOW_PRODUCT_FILTER_IN_SURVEY = hashMapHHTModuleConfig.get(CODE_PRODUCT_FILTER_IN_SURVEY) != null ? hashMapHHTModuleConfig.get(CODE_PRODUCT_FILTER_IN_SURVEY) : false;
        this.SHOW__QDVP3_SCORE_CARD_TAB = hashMapHHTModuleConfig.get(CODE_QDVP3_SCORE_CARD_TAB) != null ? hashMapHHTModuleConfig.get(CODE_QDVP3_SCORE_CARD_TAB) : false;
        this.SHOW__OUTLET_PROGRAM = hashMapHHTModuleConfig.get(CODE_OUTLET_PROGRAM) != null ? hashMapHHTModuleConfig.get(CODE_OUTLET_PROGRAM) : false;
        this.SHOW__RE_CLASSIFICATION = hashMapHHTModuleConfig.get(CODE_RE_CLASSIFICATION) != null ? hashMapHHTModuleConfig.get(CODE_RE_CLASSIFICATION) : false;
        this.SHOW_UPDATE_SIH = hashMapHHTModuleConfig.get(CODE_UPDATE_SIH) != null ? hashMapHHTModuleConfig.get(CODE_UPDATE_SIH) : false;
        this.SHOW_RETAILER_FREQUENCY = hashMapHHTModuleConfig.get(CODE_RETAILER_FREQUENCY) != null ? hashMapHHTModuleConfig.get(CODE_RETAILER_FREQUENCY) : false;
        this.IS_DAY_REPORT_PRINT = hashMapHHTModuleConfig.get(CODE_DAY_REPORT_PRINT) != null ? hashMapHHTModuleConfig.get(CODE_DAY_REPORT_PRINT) : false;
        this.IS_BATCHWISE_VANLOAD = hashMapHHTModuleConfig.get(CODE_IS_BATCHWISE_VANLOAD) != null ? hashMapHHTModuleConfig.get(CODE_IS_BATCHWISE_VANLOAD) : false;
        this.SHOW_VANLOAD_STOCK_PRINT = hashMapHHTModuleConfig.get(CODE_VANLOAD_STOCK_PRINT) != null ? hashMapHHTModuleConfig.get(CODE_VANLOAD_STOCK_PRINT) : false;
        this.HIDE_ORDER_DIST = hashMapHHTModuleConfig.get(CODE_HIDE_ORDER_DIST) != null ? hashMapHHTModuleConfig.get(CODE_HIDE_ORDER_DIST) : false;
        this.IS_TAX_APPLIED_VALIDATION = hashMapHHTModuleConfig.get(CODE_IS_TAX_APPLIED_VALIDATION) != null ? hashMapHHTModuleConfig.get(CODE_IS_TAX_APPLIED_VALIDATION) : false;
        this.SHOW_ORDER_SUMMARY_DETAIL_DIALOG = hashMapHHTModuleConfig.get(CODE_ORD_SUMMARY_DETAIL_DIALOG) != null ? hashMapHHTModuleConfig.get(CODE_ORD_SUMMARY_DETAIL_DIALOG) : false;
        this.SHOW_DIST_STOCK = hashMapHHTModuleConfig.get(CODE_SHOW_DIST_STOCK) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_DIST_STOCK) : false;
        this.IS_RTR_WISE_DOWNLOAD = hashMapHHTModuleConfig.get(CODE_RTR_WISE_DOWNLOAD) != null ? hashMapHHTModuleConfig.get(CODE_RTR_WISE_DOWNLOAD) : false;
        this.IS_USER_WISE_RETAILER_DOWNLOAD = hashMapHHTModuleConfig.get(CODE_USER_WISE_RETAILER_DOWNLOAD) != null ? hashMapHHTModuleConfig.get(CODE_USER_WISE_RETAILER_DOWNLOAD) : false;
        if (hashMapHHTModuleOrder.get(CODE_USER_WISE_RETAILER_DOWNLOAD) != null) {
            if (hashMapHHTModuleOrder.get(CODE_USER_WISE_RETAILER_DOWNLOAD) == 1)
                IS_BEAT_WISE_RETAILER_DOWNLOAD = true;
        }
        this.IS_RET_NAME_RETAILER_DOWNLOAD = hashMapHHTModuleConfig.get(CODE_RETNAME_WISE_RETAILER_DOWNLOAD) != null ? hashMapHHTModuleConfig.get(CODE_RETNAME_WISE_RETAILER_DOWNLOAD) : false;
        this.IS_SYNC_WITH_IMAGES = hashMapHHTModuleConfig.get(CODE_SYNC_WITH_IMAGES) != null ? hashMapHHTModuleConfig.get(CODE_SYNC_WITH_IMAGES) : false;
        this.SHOW_STK_ORD_MRP = hashMapHHTModuleConfig.get(CODE_SHOW_STK_ORD_MRP) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_STK_ORD_MRP) : false;
        this.IS_ENABLE_GCM_REGISTRATION = hashMapHHTModuleConfig.get(CODE_ENABLE_GCM_REGISTRATION) != null ? hashMapHHTModuleConfig.get(CODE_ENABLE_GCM_REGISTRATION) : false;
        this.SHOW_FEEDBACK = hashMapHHTModuleConfig.get(CODE_SHOW_FEEDBACK) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_FEEDBACK) : false;
        this.SHOW_ORDER_TYPE_DIALOG = hashMapHHTModuleConfig.get(CODE_SHOW_ORDER_TYPE_DIALOG) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_ORDER_TYPE_DIALOG) : false;
        this.SHOW_ORDER_FOCUS_COUNT = hashMapHHTModuleConfig.get(CODE_SHOW_ORDER_FOCUS_COUNT) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_ORDER_FOCUS_COUNT) : false;
        this.SHOW_LAST_3MONTHS_BILLS = hashMapHHTModuleConfig.get(CODE_SHOW_LAST_3MONTHS_SALES) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_LAST_3MONTHS_SALES) : false;
        this.SHOW_MSL_NOT_SOLD = hashMapHHTModuleConfig.get(CODE_MSL_NOT_SOLD) != null ? hashMapHHTModuleConfig.get(CODE_MSL_NOT_SOLD) : false;
        this.SHOW_RETAILER_CONTACT = hashMapHHTModuleConfig.get(CODE_RETAILER_CONTACT) != null ? hashMapHHTModuleConfig.get(CODE_RETAILER_CONTACT) : false;
        this.SHOW_NOR_DASHBOARD = hashMapHHTModuleConfig.get(CODE_NORMAL_DASHBOARD) != null ? hashMapHHTModuleConfig.get(CODE_NORMAL_DASHBOARD) : false;
        this.SHOW_COLLECTION_PRINT = hashMapHHTModuleConfig.get(CODE_SHOW_COLLECTION_PRINT) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_COLLECTION_PRINT) : false;
        this.SHOW_NETAMOUNT_IN_REPORT = hashMapHHTModuleConfig.get(CODE_NETAMOUNT_IN_REPORT) != null ? hashMapHHTModuleConfig.get(CODE_NETAMOUNT_IN_REPORT) : false;
        this.SHOW_COLLECTION_SEQ_NO = hashMapHHTModuleConfig.get(CODE_COLLECTION_SEQ_NO) != null ? hashMapHHTModuleConfig.get(CODE_COLLECTION_SEQ_NO) : false;
        this.SHOW_SERIAL_NO_SCREEN = hashMapHHTModuleConfig.get(CODE_SHOW_SERIAL_NO) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_SERIAL_NO) : false;
        this.SHOW_RETAILER_SELECTION_FILTER = hashMapHHTModuleConfig.get(CODE_SHOW_RETAILER_SELECTION_FILTER) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_RETAILER_SELECTION_FILTER) : false;
        this.SHOW_FEEDBACK_IN_CLOSE_CALL = hashMapHHTModuleConfig.get(CODE_FEEDBACK_IN_CLOSE_CALL) != null ? hashMapHHTModuleConfig.get(CODE_FEEDBACK_IN_CLOSE_CALL) : false;
        this.SHOW_PRINT_DELIVERY_MANAGEMENT = hashMapHHTModuleConfig.get(CODE_PRINT_DELIVERY_MANAGEMENT) != null ? hashMapHHTModuleConfig.get(CODE_PRINT_DELIVERY_MANAGEMENT) : false;
        this.SHOW_MENU_COUNTER_ALERT = hashMapHHTModuleConfig.get(CODE_SHOW_MENU_COUNTER_ALERT) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_MENU_COUNTER_ALERT) : false;
        this.IS_CHANNEL_SELECTION_NEW_RETAILER = hashMapHHTModuleConfig.get(CODE_SHOW_CHANNEL_SELECTION_NEW_RETAILER) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_CHANNEL_SELECTION_NEW_RETAILER) : false;
        this.SHOW_PRINT_CREDIT_NOTE = hashMapHHTModuleConfig.get(CODE_CREDIT_NOTE_PRINT) != null ? hashMapHHTModuleConfig.get(CODE_CREDIT_NOTE_PRINT) : false;
        this.IS_FOCUSBRAND_COUNT_IN_REPORT = hashMapHHTModuleConfig.get(CODE_SHOW_FOCUSBRAND_COUNT_IN_REPORT) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_FOCUSBRAND_COUNT_IN_REPORT) : false;
        this.IS_MUSTSELL_COUNT_IN_REPORT = hashMapHHTModuleConfig.get(CODE_SHOW_MUSTSELL_COUNT_IN_REPORT) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_MUSTSELL_COUNT_IN_REPORT) : false;
        this.SHOW_CUSTOM_KEYBOARD_NEW = hashMapHHTModuleConfig.get(CODE_CUSTOM_KEYBOARD_NEW) != null ? hashMapHHTModuleConfig.get(CODE_CUSTOM_KEYBOARD_NEW) : false;
        this.SHOW_NFC_VALIDATION_FOR_RETAILER = hashMapHHTModuleConfig.get(CODE_NFC_VALIDATION_FOR_RETAILER) != null ? hashMapHHTModuleConfig.get(CODE_NFC_VALIDATION_FOR_RETAILER) : false;
        this.SHOW_NFC_SEARCH_IN_ASSET = hashMapHHTModuleConfig.get(CODE_NFC_SEARCH_IN_ASSET) != null ? hashMapHHTModuleConfig.get(CODE_NFC_SEARCH_IN_ASSET) : false;
        this.ASSET_PHOTO_VALIDATION = hashMapHHTModuleConfig.get(CODE_ASSET_PHOTO_VALIDATION) != null ? hashMapHHTModuleConfig.get(CODE_ASSET_PHOTO_VALIDATION) : false;
        this.IS_DOT_FOR_GROUP = hashMapHHTModuleConfig.get(CODE_DOT_FOR_GROUP) != null ? hashMapHHTModuleConfig.get(CODE_DOT_FOR_GROUP) : false;
        this.IS_UNLINK_FILTERS = hashMapHHTModuleConfig.get(CODE_IS_UNLINK_FILTERS) != null ? hashMapHHTModuleConfig.get(CODE_IS_UNLINK_FILTERS) : false;
        this.IS_SHOW_START_TIME = hashMapHHTModuleConfig.get(CODE_SHOW_START_TIME) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_START_TIME) : false;
        this.IS_SHOW_PPQ = hashMapHHTModuleConfig.get(CODE_SHOW_PPQ) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_PPQ) : false;
        this.IS_SHOW_PSQ = hashMapHHTModuleConfig.get(CODE_SHOW_PSQ) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_PSQ) : false;
        this.IS_AUDIT_USER = hashMapHHTModuleConfig.get(CODE_IS_AUDIT_USER) != null ? hashMapHHTModuleConfig.get(CODE_IS_AUDIT_USER) : false;
        this.IS_IN_OUT_MANDATE = hashMapHHTModuleConfig.get(CODE_IN_OUT_MANDATE) != null ? hashMapHHTModuleConfig.get(CODE_IN_OUT_MANDATE) : false;
        this.IS_ADHOC = hashMapHHTModuleConfig.get(CODE_IS_ADHOC) != null ? hashMapHHTModuleConfig.get(CODE_IS_ADHOC) : false;
        this.IS_ATTENDANCE_SYNCUPLOAD = hashMapHHTModuleConfig.get(CODE_ATTENDANCE_SYNCUPLOAD) != null ? hashMapHHTModuleConfig.get(CODE_ATTENDANCE_SYNCUPLOAD) : false;
        this.IS_DISCOUNT_FOR_UNPRICED_PRODUCTS = hashMapHHTModuleConfig.get(CODE_DISCOUNT_FOR_UNPRICED_PRODUCTS) != null ? hashMapHHTModuleConfig.get(CODE_DISCOUNT_FOR_UNPRICED_PRODUCTS) : false;
        this.IS_USER_BASED_DASH = hashMapHHTModuleConfig.get(CODE_IS_USER_BASED_DASH) != null ? hashMapHHTModuleConfig.get(CODE_IS_USER_BASED_DASH) : false;
        this.IS_DISTRIBUTOR_BASED_DASH = hashMapHHTModuleConfig.get(CODE_IS_DISTRIBUTOR_BASED_DASH) != null ? hashMapHHTModuleConfig.get(CODE_IS_DISTRIBUTOR_BASED_DASH) : false;
        this.SHOW_NO_ORDER_REASON = hashMapHHTModuleConfig.get(CODE_SHOW_NO_ORDER_REASON) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_NO_ORDER_REASON) : false;
        this.IS_CLEAR_DATA = hashMapHHTModuleConfig.get(CODE_CLEAR_DATA) != null ? hashMapHHTModuleConfig.get(CODE_CLEAR_DATA) : false;
        this.IS_NIVEA_BASED_DASH = hashMapHHTModuleConfig.get(CODE_IS_NIVEA_DASH) != null ? hashMapHHTModuleConfig.get(CODE_IS_NIVEA_DASH) : false;
        this.IS_SMP_BASED_DASH = hashMapHHTModuleConfig.get(CODE_IS_SMP_DASH) != null ? hashMapHHTModuleConfig.get(CODE_IS_SMP_DASH) : false;

        this.IS_MUST_SELL_REASON = hashMapHHTModuleConfig.get(CODE_MUST_SELL_REASON) != null ? hashMapHHTModuleConfig.get(CODE_MUST_SELL_REASON) : false;
        this.IS_MUST_SELL_SKIP = hashMapHHTModuleConfig.get(CODE_MUST_SELL_SKIP) != null ? hashMapHHTModuleConfig.get(CODE_MUST_SELL_SKIP) : false;
        this.IS_PRICE_CHECK_RETAIN_LAST_VISIT_TRAN = hashMapHHTModuleConfig.get(CODE_PRICE_CHECK_RETAIN_LAST_VISIT_TRAN) != null ? hashMapHHTModuleConfig.get(CODE_PRICE_CHECK_RETAIN_LAST_VISIT_TRAN) : false;
        this.IS_STOCK_CHECK_RETAIN_LAST_VISIT_TRAN = hashMapHHTModuleConfig.get(CODE_STOCK_CHECK_RETAIN_LAST_VISIT_TRAN) != null ? hashMapHHTModuleConfig.get(CODE_STOCK_CHECK_RETAIN_LAST_VISIT_TRAN) : false;
        this.IS_NEAR_EXPIRY_RETAIN_LAST_VISIT_TRAN = hashMapHHTModuleConfig.get(CODE_NEAR_EXPIRY_RETAIN_LAST_VISIT_TRAN) != null ? hashMapHHTModuleConfig.get(CODE_NEAR_EXPIRY_RETAIN_LAST_VISIT_TRAN) : false;
        this.IS_PROMOTION_RETAIN_LAST_VISIT_TRAN = hashMapHHTModuleConfig.get(CODE_PROMOTION_RETAIN_LAST_VISIT_TRAN) != null ? hashMapHHTModuleConfig.get(CODE_PROMOTION_RETAIN_LAST_VISIT_TRAN) : false;
        this.IS_SURVEY_RETAIN_LAST_VISIT_TRAN = hashMapHHTModuleConfig.get(CODE_SURVEY_RETAIN_LAST_VISIT_TRAN) != null ? hashMapHHTModuleConfig.get(CODE_SURVEY_RETAIN_LAST_VISIT_TRAN) : false;
        this.IS_SOS_RETAIN_LAST_VISIT_TRAN = hashMapHHTModuleConfig.get(CODE_SOS_RETAIN_LAST_VISIT_TRAN) != null ? hashMapHHTModuleConfig.get(CODE_SOS_RETAIN_LAST_VISIT_TRAN) : false;
        this.IS_PLANOGRAM_RETAIN_LAST_VISIT_TRAN = hashMapHHTModuleConfig.get(CODE_PLANOGRAM_RETAIN_LAST_VISIT_TRAN) != null ? hashMapHHTModuleConfig.get(CODE_PLANOGRAM_RETAIN_LAST_VISIT_TRAN) : false;
        this.IS_SF_NORM_CHECK = hashMapHHTModuleConfig.get(CODE_CHECK_NORM) != null ? hashMapHHTModuleConfig.get(CODE_CHECK_NORM) : false;
        this.IS_CATALOG_IMG_DOWNLOAD = hashMapHHTModuleConfig.get(CODE_CATALOG_PRD_IMAGES) != null ? hashMapHHTModuleConfig.get(CODE_CATALOG_PRD_IMAGES) : false;
        this.IS_MULTI_STOCKORDER = hashMapHHTModuleConfig.get(CODE_MULTI_STOCKORDER) != null ? hashMapHHTModuleConfig.get(CODE_MULTI_STOCKORDER) : false;
        if (IS_MUST_SELL_REASON && IS_MUST_SELL_SKIP) {
            this.IS_MUST_SELL_SKIP = true;
            this.IS_MUST_SELL_REASON = false;
        }
        this.IS_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE = hashMapHHTModuleConfig.get(CODE_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE) != null ? hashMapHHTModuleConfig.get(CODE_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE) : false;

        if (hashMapHHTModuleOrder.get(CODE_PRODUCT_SCHEME_DIALOG) != null) {
            if (hashMapHHTModuleOrder.get(CODE_PRODUCT_SCHEME_DIALOG) == 1)
                IS_PRODUCT_DIALOG = true;
            else if (hashMapHHTModuleOrder.get(CODE_PRODUCT_SCHEME_DIALOG) == 2)
                IS_SCHEME_DIALOG = true;
            else if (hashMapHHTModuleOrder.get(CODE_PRODUCT_SCHEME_DIALOG) == 3)
                IS_PRODUCT_SCHEME_DIALOG = true;
        }

        if (hashMapHHTModuleOrder.get(SURVEY_GLOBAL_SAVE) != null) {
            if (hashMapHHTModuleOrder.get(SURVEY_GLOBAL_SAVE) == 1)
                IS_SURVEY_ONCE = true;

        }

        if (hashMapHHTModuleOrder.get(CODE_COLLECTION_REASON) != null) {
            if (hashMapHHTModuleOrder.get(CODE_COLLECTION_REASON) == 1)
                IS_COLLECTION_MANDATE = true;

        }

        this.IS_STOCK_IN_HAND = hashMapHHTModuleConfig.get(CODE_STOCK_IN_HAND) != null ? hashMapHHTModuleConfig.get(CODE_STOCK_IN_HAND) : false;
        this.IS_STOCK_IN_HAND_MASTER = hashMapHHTModuleConfig.get(CODE_STOCK_IN_HAND) != null ? hashMapHHTModuleConfig.get(CODE_STOCK_IN_HAND) : false;

        this.IS_WSIH = hashMapHHTModuleConfig.get(CODE_IS_WSIH) != null ? hashMapHHTModuleConfig.get(CODE_IS_WSIH) : false;
        this.IS_WSIH_MASTER = hashMapHHTModuleConfig.get(CODE_IS_WSIH) != null ? hashMapHHTModuleConfig.get(CODE_IS_WSIH) : false;

        this.IS_SIH_VALIDATION = hashMapHHTModuleConfig.get(CODE_SIH_VALIDATION) != null ? hashMapHHTModuleConfig.get(CODE_SIH_VALIDATION) : false;
        this.IS_SIH_VALIDATION_MASTER = hashMapHHTModuleConfig.get(CODE_SIH_VALIDATION) != null ? hashMapHHTModuleConfig.get(CODE_SIH_VALIDATION) : false;


        this.SHOW_TAX = hashMapHHTModuleConfig.get(CODE_TAX_APPLY) != null ? hashMapHHTModuleConfig.get(CODE_TAX_APPLY) : false;
        this.SHOW_TAX_MASTER = hashMapHHTModuleConfig.get(CODE_TAX_APPLY) != null ? hashMapHHTModuleConfig.get(CODE_TAX_APPLY) : false;

        if (hashMapHHTModuleConfig.get(CODE_TAX_APPLY) != null) {
            this.IS_EXCLUDE_TAX = hashMapHHTModuleOrder.get(CODE_TAX_APPLY) == null || hashMapHHTModuleOrder.get(CODE_TAX_APPLY) != 1;
        }

        this.discountType = hashMapHHTModuleOrder.get(CODE_DISCOUNT_EDITVIEW) != null ? hashMapHHTModuleOrder.get(CODE_DISCOUNT_EDITVIEW) : 0;
        this.SHOW_TOTAL_DISCOUNT_EDITTEXT = hashMapHHTModuleConfig.get(CODE_DISCOUNT_EDITVIEW) != null ? hashMapHHTModuleConfig.get(CODE_DISCOUNT_EDITVIEW) : false;
        this.SHOW_TOTAL_DISCOUNT_EDITTEXT_MASTER = hashMapHHTModuleConfig.get(CODE_DISCOUNT_EDITVIEW) != null ? hashMapHHTModuleConfig.get(CODE_DISCOUNT_EDITVIEW) : false;

        this.SHOW_SPL_FILTER = hashMapHHTModuleConfig.get(CODE_SHOW_SPL_FILTER) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_SPL_FILTER) : false;

        this.SHOW_COMPETITOR_FILTER = hashMapHHTModuleConfig.get(CODE_SHOW_COMPETITOR_FILTER) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_COMPETITOR_FILTER) : false;
        if (SHOW_COMPETITOR_FILTER) {
            downloadCompetitorFilterLevels();
        }

        this.SHOW_VANGPS_VALIDATION = hashMapHHTModuleConfig.get(CODE_VANGPS_VALIDATION) != null ? hashMapHHTModuleConfig.get(CODE_VANGPS_VALIDATION) : false;
        ConfigurationMasterHelper.vanDistance = hashMapHHTModuleOrder.get(CODE_VANGPS_VALIDATION) != null ? hashMapHHTModuleOrder.get(CODE_VANGPS_VALIDATION) : 0;

        this.SHOW_RET_SKIP_VALIDATION = hashMapHHTModuleConfig.get(CODE_RET_SKIP_VALIDATION) != null ? hashMapHHTModuleConfig.get(CODE_RET_SKIP_VALIDATION) : false;
        this.ret_skip_flag = hashMapHHTModuleOrder.get(CODE_RET_SKIP_VALIDATION) != null ? hashMapHHTModuleOrder.get(CODE_RET_SKIP_VALIDATION) : 0;

        this.ISUPLOADUSERLOC = hashMapHHTModuleConfig.get(CODE_UPLOADUSERLOC) != null ? hashMapHHTModuleConfig.get(CODE_UPLOADUSERLOC) : false;
        this.alarmTime = hashMapHHTModuleOrder.get(CODE_ALARM_TIME) != null ? hashMapHHTModuleOrder.get(CODE_ALARM_TIME) : alarmTime;
        this.startTime = hashMapHHTModuleOrder.get(CODE_START_TIME) != null ? hashMapHHTModuleOrder.get(CODE_START_TIME) : startTime;
        this.endTime = hashMapHHTModuleOrder.get(CODE_END_TIME) != null ? hashMapHHTModuleOrder.get(CODE_END_TIME) : endTime;

        this.IS_SHOW_DROPSIZE = hashMapHHTModuleConfig.get(CODE_DROPSIZE) != null ? hashMapHHTModuleConfig.get(CODE_DROPSIZE) : false;
        this.DROPSIZE_ORDER_TYPE = hashMapHHTModuleOrder.get(CODE_DROPSIZE) != null ? hashMapHHTModuleOrder.get(CODE_DROPSIZE) : 0;

        this.SHOW_SUBDEPOT = hashMapHHTModuleConfig.get(CODE_SHOW_SUB_DEPOT) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_SUB_DEPOT) : false;
        this.VANLOAD_TYPE = hashMapHHTModuleOrder.get(CODE_SHOW_SUB_DEPOT) != null ? hashMapHHTModuleOrder.get(CODE_SHOW_SUB_DEPOT) : 0;

        this.SHOW_CAPTURED_LOCATION = hashMapHHTModuleConfig.get(CODE_CAPTURE_LOCATION) != null ? hashMapHHTModuleConfig.get(CODE_CAPTURE_LOCATION) : false;
        LocationUtil.gpsconfigcode = hashMapHHTModuleOrder.get(CODE_CAPTURE_LOCATION) != null ? hashMapHHTModuleOrder.get(CODE_CAPTURE_LOCATION) : 0;

        this.SHOW_GLOBAL_DISOCUNT_DIALOG = hashMapHHTModuleConfig.get(CODE_GLOBAL_DISOCUNT_DIALOG) != null ? hashMapHHTModuleConfig.get(CODE_GLOBAL_DISOCUNT_DIALOG) : false;
        this.discount_max = hashMapHHTModuleOrder.get(CODE_GLOBAL_DISOCUNT_DIALOG) != null ? hashMapHHTModuleOrder.get(CODE_GLOBAL_DISOCUNT_DIALOG) : 100;

        this.VALUE_COMMA_COUNT = hashMapHHTModuleOrder.get(CODE_VOLUME_COMMA_COUNT) != null ? hashMapHHTModuleOrder.get(CODE_VOLUME_COMMA_COUNT) : 0;
        this.VALUE_PRECISION_COUNT = hashMapHHTModuleOrder.get(CODE_VOLUME_PRECISION_COUNT) != null ? hashMapHHTModuleOrder.get(CODE_VOLUME_PRECISION_COUNT) : 2;
        this.PERCENT_PRECISION_COUNT = hashMapHHTModuleOrder.get(CODE_PERCENT_PRECISION_COUNT) != null ? hashMapHHTModuleOrder.get(CODE_PERCENT_PRECISION_COUNT) : 0;
        this.PRECISION_COUNT_FOR_CALCULATION = hashMapHHTModuleOrder.get(CODE_CALCULATION_PRECISION_COUNT) != null ? hashMapHHTModuleOrder.get(CODE_CALCULATION_PRECISION_COUNT) : 3;
        SDUtil.CALCULATION_PRECISION_COUNT = hashMapHHTModuleOrder.get(CODE_CALCULATION_PRECISION_COUNT) != null ? hashMapHHTModuleOrder.get(CODE_CALCULATION_PRECISION_COUNT) : 3;

        this.printCount = hashMapHHTModuleOrder.get(CODE_PRINT_COUNT) != null ? hashMapHHTModuleOrder.get(CODE_PRINT_COUNT) : 0;
        this.photocount = hashMapHHTModuleOrder.get(CODE_PHOTO_CAPTURE_COUNT) != null ? hashMapHHTModuleOrder.get(CODE_PHOTO_CAPTURE_COUNT) : 0;
        this.raPhotoCount = hashMapHHTModuleOrder.get(CODE_ROAD_ACTIVITY_PHOTO_COUNT) != null ? hashMapHHTModuleOrder.get(CODE_ROAD_ACTIVITY_PHOTO_COUNT) : 0;
        //this.ROUND_DECIMAL_COUNT = hashMapHHTModuleOrder.get(CODE_ALLOW_DECIMAL) != null ? hashMapHHTModuleOrder.get(CODE_ALLOW_DECIMAL) : 0;
        this.photopercent = hashMapHHTModuleOrder.get(CODE_PHOTO_CAPTURE_PERCENT) != null ? hashMapHHTModuleOrder.get(CODE_PHOTO_CAPTURE_PERCENT) : 80;
        this.globalSeqId = hashMapHHTModuleOrder.get(CODE_GLOBAL_CATEGORY) != null ? hashMapHHTModuleOrder.get(CODE_GLOBAL_CATEGORY) : 0;


        this.refreshMin = hashMapHHTModuleOrder.get(CODE_PERRPT_REFRESH) != null ? hashMapHHTModuleOrder.get(CODE_PERRPT_REFRESH) : 5;
        this.refreshMin = this.refreshMin > 5 ? this.refreshMin : 5;


        if (hashMapHHTModuleConfig.get(CODE_SHOW_MAX_NO_PRODUCT_LINES) != null)
            this.MAX_NO_OF_PRODUCT_LINES = 2;

        this.IS_ENABLE_CAMERA_PICTURE_SIZE = hashMapHHTModuleConfig.get(CODE_CAMERA_PICTURE_SIZE) != null ? hashMapHHTModuleConfig.get(CODE_CAMERA_PICTURE_SIZE) : false;
        if (this.IS_ENABLE_CAMERA_PICTURE_SIZE)
            loadCameraPictureSize();

        this.IS_ENABLE_MIN_MAX_DATE_CHQ = hashMapHHTModuleConfig.get(CODE_MAX_MIN_DATE_CHEQUE) != null ? hashMapHHTModuleConfig.get(CODE_MAX_MIN_DATE_CHEQUE) : false;
        if (this.IS_ENABLE_MIN_MAX_DATE_CHQ)
            loadMinMaxDateInChq();
        this.IS_ENABLE_ACC_NO_CHQ = hashMapHHTModuleConfig.get(CODE_ACC_NO_CHEQUE) != null ? hashMapHHTModuleConfig.get(CODE_ACC_NO_CHEQUE) : false;

        this.IS_LOCATION_WISE_TAX_APPLIED = hashMapHHTModuleConfig.get(CODE_LOCAITON_WISE_TAX_APPLIED) != null ? hashMapHHTModuleConfig.get(CODE_LOCAITON_WISE_TAX_APPLIED) : false;
        if (this.IS_LOCATION_WISE_TAX_APPLIED)
            this.STRING_LOCATION_WISE_TAX_APPLIED = loadLocationWiseTaxApplied();

        this.IS_REMOVE_TAX_ON_PRICE_FOR_ALL_PRODUCTS = hashMapHHTModuleConfig.get(CODE_REMOVE_TAX_ON_PRICE_FOR_ALL_PRODUCTS) != null ? hashMapHHTModuleConfig.get(CODE_REMOVE_TAX_ON_PRICE_FOR_ALL_PRODUCTS) : false;

        this.IS_RETAILER_PHOTO_NEEDED = hashMapHHTModuleConfig.get(CODE_RETAILER_PHOTO) != null ? hashMapHHTModuleConfig.get(CODE_RETAILER_PHOTO) : false;
        if (this.IS_RETAILER_PHOTO_NEEDED && hashMapHHTModuleOrder.get(CODE_RETAILER_PHOTO) > 1)
            this.RETAILER_PHOTO_COUNT = hashMapHHTModuleOrder.get(CODE_RETAILER_PHOTO);

        this.IS_NEARBY_RETAILER = hashMapHHTModuleConfig.get(CODE_SHOW_NEARBY_RETAILER_MAX) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_NEARBY_RETAILER_MAX) : false;
        if (this.IS_NEARBY_RETAILER && hashMapHHTModuleOrder.get(CODE_SHOW_NEARBY_RETAILER_MAX) > 1)
            this.VALUE_NEARBY_RETAILER_MAX = hashMapHHTModuleOrder.get(CODE_SHOW_NEARBY_RETAILER_MAX);

        this.IS_DEACTIVATE_RETAILER = hashMapHHTModuleConfig.get(CODE_IS_DEACTIVATE_RETAILER) != null ? hashMapHHTModuleConfig.get(CODE_IS_DEACTIVATE_RETAILER) : false;
        this.IS_DISTRIBUTOR_AVAILABLE = hashMapHHTModuleConfig.get(CODE_SHOW_DISTRIBUTOR_AVAILABLE) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_DISTRIBUTOR_AVAILABLE) : false;
        this.SHOW_PRICECHECK_IN_STOCKCHECK = hashMapHHTModuleConfig.get(CODE_IS_PRICECHECK_IN_STOCKCHECK) != null ? hashMapHHTModuleConfig.get(CODE_IS_PRICECHECK_IN_STOCKCHECK) : false;
        this.IS_DIST_SELECT_BY_SUPPLIER = hashMapHHTModuleConfig.get(CODE_DIST_SELECT_BY_SUPPLIER) != null ? hashMapHHTModuleConfig.get(CODE_DIST_SELECT_BY_SUPPLIER) : false;
        this.IS_PRINT_CREDIT_NOTE_REPORT = hashMapHHTModuleConfig.get(CODE_PRINT_CREDIT_NOTE_REPORT) != null ? hashMapHHTModuleConfig.get(CODE_PRINT_CREDIT_NOTE_REPORT) : false;
        this.IS_PRICE_CHECK_RETAIN_LAST_VISIT_IN_EDIT_MODE = hashMapHHTModuleConfig.get(CODE_PRICE_CHECK_RETAIN_LAST_VISIT_IN_EDIT_MODE) != null ? hashMapHHTModuleConfig.get(CODE_PRICE_CHECK_RETAIN_LAST_VISIT_IN_EDIT_MODE) : false;
        this.IS_EOD_COLUMNS_AVALIABLE = hashMapHHTModuleConfig.get(CODE_EOD_COLUMNS) != null ? hashMapHHTModuleConfig.get(CODE_EOD_COLUMNS) : false;
        this.IS_DAY_WISE_RETAILER_WALKINGSEQ = hashMapHHTModuleConfig.get(CODE_DAY_WISE_RETAILER_WALKINGSEQ) != null ? hashMapHHTModuleConfig.get(CODE_DAY_WISE_RETAILER_WALKINGSEQ) : false;
        this.IS_PARTIAL_CREDIT_NOTE_ALLOW = hashMapHHTModuleConfig.get(CODE_PARTIAL_CREDITNOTE) != null ? hashMapHHTModuleConfig.get(CODE_PARTIAL_CREDITNOTE) : false;
        this.IS_PAYMENT_RECEIPTNO_GET = hashMapHHTModuleConfig.get(CODE_PAYMENT_RECEIPT_NO) != null ? hashMapHHTModuleConfig.get(CODE_PAYMENT_RECEIPT_NO) : false;
        this.COLL_CHEQUE_MODE = hashMapHHTModuleConfig.get(CODE_COLL_CHEQUE_MODE) != null ? hashMapHHTModuleConfig.get(CODE_COLL_CHEQUE_MODE) : false;
        this.SHOW_DOC_REF_NO = hashMapHHTModuleConfig.get(CODE_DOC_REF_NO) != null ? hashMapHHTModuleConfig.get(CODE_DOC_REF_NO) : false;
        if (hashMapHHTModuleOrder.get(CODE_COLLECTION_REASON) != null) {
            if (hashMapHHTModuleOrder.get(CODE_COLLECTION_REASON) == 1)
                IS_COLLECTION_MANDATE = true;

        }

        this.IS_NEW_RETAILER_EDIT = hashMapHHTModuleConfig.get(CODE_IS_NEW_RETAILER_EDIT) != null ? hashMapHHTModuleConfig.get(CODE_IS_NEW_RETAILER_EDIT) : false;
        this.IS_EOD_STOCK_SPLIT = hashMapHHTModuleConfig.get(CODE_EOD_STOCK_SPLIT) != null ? hashMapHHTModuleConfig.get(CODE_EOD_STOCK_SPLIT) : false;
        this.IS_STOCK_AVAILABLE_PRODUCTS_ONLY = hashMapHHTModuleConfig.get(CODE_SHOW_STOCK_AVAILABLE_PRODUCTS_ONLY) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_STOCK_AVAILABLE_PRODUCTS_ONLY) : false;
        this.IS_STOCK_AVAILABLE_PRODUCTS_ONLY_MASTER = hashMapHHTModuleConfig.get(CODE_SHOW_STOCK_AVAILABLE_PRODUCTS_ONLY) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_STOCK_AVAILABLE_PRODUCTS_ONLY) : false;
        this.IS_LOYALTY_AUTO_PAYOUT = hashMapHHTModuleConfig.get(CODE_LOYALTY_AUTO_PAYOUT) != null ? hashMapHHTModuleConfig.get(CODE_LOYALTY_AUTO_PAYOUT) : false;
        this.IS_BAR_CODE = hashMapHHTModuleConfig.get(CODE_BAR_CODE) != null ? hashMapHHTModuleConfig.get(CODE_BAR_CODE) : false;
        if (IS_BAR_CODE && hashMapHHTModuleOrder.get(CODE_BAR_CODE) == 1) {
            IS_QTY_INCREASE = true;
        }
        this.IS_BAR_CODE_STOCK_CHECK = hashMapHHTModuleConfig.get(CODE_BAR_CODE_STOCK_CHECK) != null ? hashMapHHTModuleConfig.get(CODE_BAR_CODE_STOCK_CHECK) : false;
        this.IS_BAR_CODE_PRICE_CHECK = hashMapHHTModuleConfig.get(CODE_BAR_CODE_PRICE_CHECK) != null ? hashMapHHTModuleConfig.get(CODE_BAR_CODE_PRICE_CHECK) : false;
        this.IS_BAR_CODE_VAN_UNLOAD = hashMapHHTModuleConfig.get(CODE_BAR_CODE_VAN_UNLOAD) != null ? hashMapHHTModuleConfig.get(CODE_BAR_CODE_VAN_UNLOAD) : false;

        this.IS_SHOW_DISCOUNTS_ORDER_SUMMARY = hashMapHHTModuleConfig.get(CODE_SHOW_DISCOUNTS_ORDER_SUMMMARY) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_DISCOUNTS_ORDER_SUMMMARY) : false;
        this.IS_APPLY_BATCH_PRICE_FROM_PRODUCT = hashMapHHTModuleConfig.get(CODE_APLLY_BATCH_PRICE_FROM_PRODCUT) != null ? hashMapHHTModuleConfig.get(CODE_APLLY_BATCH_PRICE_FROM_PRODCUT) : false;
        this.IS_SHOW_PRINT_LANGUAGE_THAI = hashMapHHTModuleConfig.get(CODE_PRINT_LANGUAGE_THAI) != null ? hashMapHHTModuleConfig.get(CODE_PRINT_LANGUAGE_THAI) : false;
        this.IS_LOAD_PRICE_GROUP_PRD_OLY = hashMapHHTModuleConfig.get(CODE_LOAD_PRICE_GROUP_PRD_OLY) != null ? hashMapHHTModuleConfig.get(CODE_LOAD_PRICE_GROUP_PRD_OLY) : false;
        this.IS_SIH_VALIDATION_ON_DELIVERY = hashMapHHTModuleConfig.get(CODE_SIH_VALIDATION_ON_DELIVERY) != null ? hashMapHHTModuleConfig.get(CODE_SIH_VALIDATION_ON_DELIVERY) : false;
        this.IS_SHOW_ONLY_INDICATIVE_ORDER = hashMapHHTModuleConfig.get(CODE_SHOW_ONLY_INDICATIVE_ORDER) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_ONLY_INDICATIVE_ORDER) : false;
        this.IS_SEND_EMAIL_STATEMENT_FOR_DELIVERY = hashMapHHTModuleConfig.get(CODE_SEND_EMAIL_STATEMENT_FOR_DELIVERY) != null ? hashMapHHTModuleConfig.get(CODE_SEND_EMAIL_STATEMENT_FOR_DELIVERY) : false;
        this.IS_RETAIN_NEAREXPIRY_CURRENT_TRAN_IN_STOCKCHECK = hashMapHHTModuleConfig.get(CODE_RETAIN_NEAREXPIRY_CURRENT_TRAN_IN_STOCKCHECK) != null ? hashMapHHTModuleConfig.get(CODE_RETAIN_NEAREXPIRY_CURRENT_TRAN_IN_STOCKCHECK) : false;
        this.IS_SPL_FILTER_TAB = hashMapHHTModuleConfig.get(CODE_SPL_FILTER_TAB) != null ? hashMapHHTModuleConfig.get(CODE_SPL_FILTER_TAB) : false;
        this.IS_SHOW_DELETE_OPTION = hashMapHHTModuleConfig.get(CODE_SHOW_DELETE_OPTION) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_DELETE_OPTION) : false;
        this.IS_SHOW_ORDERING_SEQUENCE = hashMapHHTModuleConfig.get(CODE_SHOW_ORDERING_SEQUENCE) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_ORDERING_SEQUENCE) : false;
        this.MOVE_NEXT_ACTIVITY = hashMapHHTModuleConfig.get(CODE_MOVE_NEXT_ACTIVITY) != null ? hashMapHHTModuleConfig.get(CODE_MOVE_NEXT_ACTIVITY) : false;
        this.IS_GUIDED_SELLING = hashMapHHTModuleConfig.get(CODE_GUIDED_SELLING) != null ? hashMapHHTModuleConfig.get(CODE_GUIDED_SELLING) : false;
        this.IS_PRINT_FILE_SAVE = hashMapHHTModuleConfig.get(CODE_PRINT_FILE_SAVE) != null ? hashMapHHTModuleConfig.get(CODE_PRINT_FILE_SAVE) : false;
        this.IS_APPLY_DISTRIBUTOR_WISE_PRICE = hashMapHHTModuleConfig.get(CODE_APPLY_DISTRIBUTOR_WISE_PRICE) != null ? hashMapHHTModuleConfig.get(CODE_APPLY_DISTRIBUTOR_WISE_PRICE) : false;
        this.IS_FORMAT_USING_CURRENCY_VALUE = hashMapHHTModuleConfig.get(CODE_CURRENCY_VALUE) != null ? hashMapHHTModuleConfig.get(CODE_CURRENCY_VALUE) : false;
        this.IS_GROUP_PRODUCTS_IN_COUNTER_SALES = hashMapHHTModuleConfig.get(CODE_GROUP_PRODUCTS_IN_COUNTER_SALES) != null ? hashMapHHTModuleConfig.get(CODE_GROUP_PRODUCTS_IN_COUNTER_SALES) : false;
        if (hashMapHHTModuleConfig.get(CODE_CURRENCY_VALUE) != null && hashMapHHTModuleOrder.get(CODE_CURRENCY_VALUE) == 1) {
            IS_APPLY_CURRENCY_CONFIG = true;
        }


        this.IS_ENABLE_BACKDATE_REPORTING = hashMapHHTModuleConfig.get(CODE_ENABLE_BACKDATE_REPORTING) != null ? hashMapHHTModuleConfig.get(CODE_ENABLE_BACKDATE_REPORTING) : false;
        this.IS_USER_CAN_SELECT_BILL_WISE_DISCOUNT = hashMapHHTModuleConfig.get(CODE_USER_CAN_SELECT_BILL_WISE_DISCOUNT) != null ? hashMapHHTModuleConfig.get(CODE_USER_CAN_SELECT_BILL_WISE_DISCOUNT) : false;
        this.SHOW_ORD_CALC = hashMapHHTModuleConfig.get(CODE_ORD_CALC) != null ? hashMapHHTModuleConfig.get(CODE_ORD_CALC) : false;
        this.SHOW_PRINT_ORDER = hashMapHHTModuleConfig.get(CODE_ORDER_PRINT) != null ? hashMapHHTModuleConfig.get(CODE_ORDER_PRINT) : false;

        if (hashMapHHTModuleConfig.get(CODE_SHOW_LPC_ORDER) != null) {
            if (hashMapHHTModuleOrder.get(CODE_SHOW_LPC_ORDER) == 0)
                this.SHOW_LPC_ORDER = true;
            else if (hashMapHHTModuleOrder.get(CODE_SHOW_LPC_ORDER) == 1) {
                this.SHOW_TOTAL_QTY_ORDER_SUMMARY = true;
            }
        }

        if (hashMapHHTModuleConfig.get(CODE_MAX_CREDIT_DAYS) != null && hashMapHHTModuleConfig.get(CODE_MAX_CREDIT_DAYS)) {
            MAX_CREDIT_DAYS = hashMapHHTModuleOrder.get(CODE_MAX_CREDIT_DAYS);
        }
        if (hashMapHHTModuleConfig.get(CODE_ATTRIBUTE_MENU) != null) {
            isAttributeMenu("HHT_CRITERIA_TYPE");
        }

        if (hashMapHHTModuleConfig.get(CODE_BACKDATE_DAYS) != null) {
            MAXIMUM_BACKDATE_DAYS = hashMapHHTModuleOrder.get(CODE_BACKDATE_DAYS);
        }
        this.IS_PRODUCT_SEQUENCE_UNIPAL = hashMapHHTModuleConfig.get(CODE_PRODCUT_SEQ_UNIPAL) != null ? hashMapHHTModuleConfig.get(CODE_PRODCUT_SEQ_UNIPAL) : false;

        this.SHOW_TOTAL_VALUE_ORDER = hashMapHHTModuleConfig.get(CODE_SHOW_VALUE_ORDER) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_VALUE_ORDER) : false;
        this.SHOW_TOTAL_QTY_ORDER = hashMapHHTModuleConfig.get(CODE_SHOW_QTY_ORDER) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_QTY_ORDER) : false;

        this.SHOW_TOTAL_ACHIEVED_VOLUME = hashMapHHTModuleConfig.get(CODE_TOTAL_ACHIEVEDVOLUME) != null ? hashMapHHTModuleConfig.get(CODE_TOTAL_ACHIEVEDVOLUME) : false;

        if (hashMapHHTModuleOrder.get(CODE_TOTAL_ACHIEVEDVOLUME) != null) {
            if (hashMapHHTModuleOrder.get(CODE_TOTAL_ACHIEVEDVOLUME) == 1)
                this.SHOW_TOTAL_ACHIEVED_VOLUME_WGT = true;
            else if (hashMapHHTModuleOrder.get(CODE_TOTAL_ACHIEVEDVOLUME) == 2)
                this.SHOW_TOTAL_TIME_SPEND = true;
            else if (hashMapHHTModuleOrder.get(CODE_TOTAL_ACHIEVEDVOLUME) == 3)
                this.SHOW_STORE_VISITED_COUNT = true;
        }

        this.IS_PROFILE_IMAGE = hashMapHHTModuleConfig.get(CODE_PROFILE_IMAGE) != null ? hashMapHHTModuleConfig.get(CODE_PROFILE_IMAGE) : false;

        if (hashMapHHTModuleConfig.get(CODE_TAX_MODEL) != null) {
            getTaxModel(CODE_TAX_MODEL);
        }

        this.IS_EXPORT_ORDER_REPORT = hashMapHHTModuleConfig.get(CODE_ORDER_REPORT_EXPORT_METHOD) != null ? hashMapHHTModuleConfig.get(CODE_ORDER_REPORT_EXPORT_METHOD) : false;
        if (hashMapHHTModuleConfig.get(CODE_ORDER_REPORT_EXPORT_METHOD) != null) {

            if (hashMapHHTModuleOrder.get(CODE_ORDER_REPORT_EXPORT_METHOD) == 2)
                this.IS_ORDER_REPORT_EXPORT_AND_EMAIL = true;
            else if (hashMapHHTModuleOrder.get(CODE_ORDER_REPORT_EXPORT_METHOD) == 3) {
                this.IS_ORDER_REPORT_EXPORT_AND_SHARE = true;
            } else if (hashMapHHTModuleOrder.get(CODE_ORDER_REPORT_EXPORT_METHOD) == 1) {
                this.IS_ORDER_REPORT_EXPORT_ONLY = true;
            }
        }
        this.IS_PRODUCT_DISPLAY_FOR_PIRAMAL = hashMapHHTModuleConfig.get(CODE_PRODUCT_DISPLAY_FOR_PIRAMAL) != null ? hashMapHHTModuleConfig.get(CODE_PRODUCT_DISPLAY_FOR_PIRAMAL) : false;

        this.IS_REASON_FOR_ALL_NON_STOCK_PRODUCTS = hashMapHHTModuleConfig.get(CODE_REASON_FOR_ALL_NON_STOCK_PRODUCTS) != null ? hashMapHHTModuleConfig.get(CODE_REASON_FOR_ALL_NON_STOCK_PRODUCTS) : false;
        this.IS_LOAD_WAREHOUSE_PRD_ONLY = hashMapHHTModuleConfig.get(CODE_LOAD_WAREHOUSE_PRD_ONLY) != null ? hashMapHHTModuleConfig.get(CODE_LOAD_WAREHOUSE_PRD_ONLY) : false;
        this.IS_TOP_ORDER_FILTER = hashMapHHTModuleConfig.get(CODE_ORDER_FILTER_TOP) != null ? hashMapHHTModuleConfig.get(CODE_ORDER_FILTER_TOP) : false;

        this.IS_TEMP_ORDER_SAVE = hashMapHHTModuleConfig.get(CODE_TEMP_ORDER_DETAILS) != null ? hashMapHHTModuleConfig.get(CODE_TEMP_ORDER_DETAILS) : false;
        this.tempOrderInterval = hashMapHHTModuleOrder.get(CODE_TEMP_ORDER_DETAILS) != null ? hashMapHHTModuleOrder.get(CODE_TEMP_ORDER_DETAILS) : 10;
        this.tempOrderInterval = this.tempOrderInterval >= 10 ? this.tempOrderInterval : 10;

        this.expenseDays = hashMapHHTModuleOrder.get(CODE_EXPENSE_DAYS) != null ? hashMapHHTModuleOrder.get(CODE_EXPENSE_DAYS) : 30;
        this.expenseDays = this.expenseDays >= 30 ? this.expenseDays : 30;

        this.IS_FITSCORE_NEEDED = hashMapHHTModuleConfig.get(CODE_FIT_SCORE) != null ? hashMapHHTModuleConfig.get(CODE_FIT_SCORE) : false;

        this.IS_UPPERCASE_LETTER = hashMapHHTModuleConfig.get(CODE_UPPERCASE_LETTER) != null ? hashMapHHTModuleConfig.get(CODE_UPPERCASE_LETTER) : false;


        if (hashMapHHTModuleConfig.get(CODE_SHOW_SPL_FILTER) != null) {
            if (hashMapHHTModuleOrder.get(CODE_SHOW_SPL_FILTER) == 1)
                this.SHOW_SPL_FLIER_NOT_NEEDED = true;
            else if (hashMapHHTModuleOrder.get(CODE_SHOW_SPL_FILTER) == 0)
                this.SHOW_SPL_FLIER_NOT_NEEDED = false;
        }

        this.SHOW_INVOICE_HISTORY = hashMapHHTModuleConfig.get(CODE_SHOW_INVOICE_HISTORY) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_INVOICE_HISTORY) : false;

        if (hashMapHHTModuleConfig.get(CODE_SALES_DISTRIBUTION) != null) {
            if (hashMapHHTModuleConfig.get(CODE_SALES_DISTRIBUTION)) {
                IS_PRODUCT_DISTRIBUTION = true;
                loadProductDistributionConfig();

            }
        }

        this.IS_REMOVE_TAX_ON_SRP = hashMapHHTModuleConfig.get(CODE_REMOVE_TAX_ON_SRP) != null ? hashMapHHTModuleConfig.get(CODE_REMOVE_TAX_ON_SRP) : false;
        this.IS_SHARE_INVOICE = hashMapHHTModuleConfig.get(CODE_SHARE_INVOICE) != null ? hashMapHHTModuleConfig.get(CODE_SHARE_INVOICE) : false;
        this.IS_SHOW_ONLY_SERVER_TASK = hashMapHHTModuleConfig.get(CODE_SHOW_ONLY_SERVER_TASK) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_ONLY_SERVER_TASK) : false;
        this.IS_FOCUS_PACK_NOT_DONE = hashMapHHTModuleConfig.get(CODE_FOCUS_PACK_NOT_DONE) != null ? hashMapHHTModuleConfig.get(CODE_FOCUS_PACK_NOT_DONE) : false;
        this.IS_DOWNLOAD_WAREHOUSE_STOCK = hashMapHHTModuleConfig.get(CODE_DOWNLAOD_WAREHOUSE_STOCK) != null ? hashMapHHTModuleConfig.get(CODE_DOWNLAOD_WAREHOUSE_STOCK) : false;
        this.IS_LOAD_ONLY_SUBD = hashMapHHTModuleConfig.get(CODE_LOAD_SUBD_ONLY) != null ? hashMapHHTModuleConfig.get(CODE_LOAD_SUBD_ONLY) : false;
        this.IS_LOAD_NON_FIELD = hashMapHHTModuleConfig.get(CODE_LOAD_NON_FIELD) != null ? hashMapHHTModuleConfig.get(CODE_LOAD_NON_FIELD) : false;
        this.IS_PLAN_RETIALER_NON_FIELD = hashMapHHTModuleConfig.get(CODE_PLAN_RETAILER_ON_NONFILED) != null ? hashMapHHTModuleConfig.get(CODE_PLAN_RETAILER_ON_NONFILED) : false;

        this.IS_ORDER_FROM_EXCESS_STOCK = hashMapHHTModuleConfig.get(CODE_ORDER_FROM_EXCESS_STOCK) != null ? hashMapHHTModuleConfig.get(CODE_ORDER_FROM_EXCESS_STOCK) : false;

        if (hashMapHHTModuleConfig.get(CODE_ORDER_RPT_CONFIG) != null) {
            if (hashMapHHTModuleConfig.get(CODE_ORDER_RPT_CONFIG)) {
                loadOrderReportConfiguration();
            }
        }

        if (hashMapHHTModuleConfig.get(CODE_LOCATION_TIMER_PERIOD) != null
                && hashMapHHTModuleConfig.get(CODE_LOCATION_TIMER_PERIOD)) {
            this.IS_LOC_TIMER_ON = hashMapHHTModuleConfig.get(CODE_LOCATION_TIMER_PERIOD) != null ? hashMapHHTModuleConfig.get(CODE_LOCATION_TIMER_PERIOD) : false;
            if (hashMapHHTModuleOrder.get(CODE_LOCATION_TIMER_PERIOD) > 0) {
                LOCATION_TIMER_PERIOD = hashMapHHTModuleOrder.get(CODE_LOCATION_TIMER_PERIOD);
            }
        }

        this.IS_RESTRICT_ORDER_TAKING = hashMapHHTModuleConfig.get(CODE_RESTRICT_ORDER_TAKING) != null ? hashMapHHTModuleConfig.get(CODE_RESTRICT_ORDER_TAKING) : false;
        this.IS_SHOW_RID_CONCEDER_AS_DSTID = hashMapHHTModuleConfig.get(CODE_SHOW_RID_CONCEDER_AS_DSTID) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_RID_CONCEDER_AS_DSTID) : false;

        this.LOAD_COMP_CONFIGS = hashMapHHTModuleConfig.get(CODE_COMPETITOR) != null ? hashMapHHTModuleConfig.get(CODE_COMPETITOR) : false;
        if (LOAD_COMP_CONFIGS) {
            loadCompetitorConfig();
        }
        this.IS_ORDER_SUMMERY_EXPORT_AND_EMAIL = hashMapHHTModuleConfig.get(CODE_ORDER_SUMMERY_EXPORT_AND_EMAIL) != null ? hashMapHHTModuleConfig.get(CODE_ORDER_SUMMERY_EXPORT_AND_EMAIL) : false;
        if (hashMapHHTModuleOrder.get(CODE_ORDER_SUMMERY_EXPORT_AND_EMAIL) != null && hashMapHHTModuleOrder.get(CODE_ORDER_SUMMERY_EXPORT_AND_EMAIL) == 1) {
            IS_ATTACH_PDF = true;
        }
        this.IS_MOQ_ENABLED = hashMapHHTModuleConfig.get(CODE_MOQ_ENABLED) != null ? hashMapHHTModuleConfig.get(CODE_MOQ_ENABLED) : false;

        this.IS_ALLOW_CONTINUOUS_PRINT = hashMapHHTModuleOrder.get(CODE_ALLOW_CONTINUOUS_PRINT) != null ? hashMapHHTModuleConfig.get(CODE_ALLOW_CONTINUOUS_PRINT) : false;

        this.SHOW_VOLUME_QTY = hashMapHHTModuleConfig.get(CODE_ORDER_RPT_VOLUME) != null ? hashMapHHTModuleConfig.get(CODE_ORDER_RPT_VOLUME) : false;
        this.HIDE_SALES_VALUE_FIELD = hashMapHHTModuleConfig.get(CODE_OUTLET_SALES_VALUE) != null ? hashMapHHTModuleConfig.get(CODE_OUTLET_SALES_VALUE) : false;
        this.SHOW_DATA_UPLOAD_STATUS = hashMapHHTModuleConfig.get(CODE_DATA_UPLOAD_STATUS) != null ? hashMapHHTModuleConfig.get(CODE_DATA_UPLOAD_STATUS) : false;

        this.retailerLocAccuracyLvl = hashMapHHTModuleOrder.get(CODE_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE) != null ? hashMapHHTModuleOrder.get(CODE_LOCATION_WHILE_NEWOUTLET_IMAGE_CAPTURE) : 0;
        this.IS_DELIVERY_PRINT = hashMapHHTModuleConfig.get(CODE_PRINT_DELIVERY) != null ? hashMapHHTModuleConfig.get(CODE_PRINT_DELIVERY) : false;

        this.IS_MUST_SELL_STK = hashMapHHTModuleConfig.get(CODE_MUST_SELL_STK) != null ? hashMapHHTModuleConfig.get(CODE_MUST_SELL_STK) : false;

        this.SHOW_PRINT_HEADERS = hashMapHHTModuleConfig.get(CODE_SHOW_PRINT_HEADERS) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_PRINT_HEADERS) : false;

        this.IS_ORD_SR_VALUE_VALIDATE = hashMapHHTModuleConfig.get(CODE_ORD_SR_VALUE_VALIDATE) != null ? hashMapHHTModuleConfig.get(CODE_ORD_SR_VALUE_VALIDATE) : false;
        this.IS_SHOW_JOINT_CALL_REMARKS = hashMapHHTModuleConfig.get(CODE_SHOW_JOINT_CALL_REMARKS) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_JOINT_CALL_REMARKS) : false;

        this.IS_INDICATIVE_SR = hashMapHHTModuleConfig.get(CODE_SR_INDICATIVE) != null ? hashMapHHTModuleConfig.get(CODE_SR_INDICATIVE) : false;
        this.IS_INVOICE_SR = hashMapHHTModuleConfig.get(CODE_SR_INVOICE) != null ? hashMapHHTModuleConfig.get(CODE_SR_INVOICE) : false;
        this.IS_GENERATE_SR_IN_DELIVERY = hashMapHHTModuleConfig.get(CODE_GENERATE_SR_IN_DELIVERY) != null ? hashMapHHTModuleConfig.get(CODE_GENERATE_SR_IN_DELIVERY) : false;
        this.IS_SYNC_FROM_CALL_ANALYSIS = hashMapHHTModuleConfig.get(CODE_IS_SYNC_FROM_CALL_ANALYSIS) != null ? hashMapHHTModuleConfig.get(CODE_IS_SYNC_FROM_CALL_ANALYSIS) : false;

        this.IS_REALTIME_LOCATION_CAPTURE = hashMapHHTModuleConfig.get(CODE_REALTIME_LOCATION_CAPTURE) != null ? hashMapHHTModuleConfig.get(CODE_REALTIME_LOCATION_CAPTURE) : false;

        if (!isInOutModule() && this.IS_REALTIME_LOCATION_CAPTURE) {
            this.IS_REALTIME_LOCATION_CAPTURE = false;
        }

        this.IS_UPLOAD_ATTENDANCE = hashMapHHTModuleConfig.get(CODE_UPLOAD_ATTENDANCE) != null ? hashMapHHTModuleConfig.get(CODE_UPLOAD_ATTENDANCE) : false;
        this.IS_ATTENDANCE_REMARK = hashMapHHTModuleConfig.get(CODE_REMARK_ATTENDANCE) != null ? hashMapHHTModuleConfig.get(CODE_REMARK_ATTENDANCE) : false;

        this.SHOW_DISTRIBUTOR_PROFILE = hashMapHHTModuleConfig.get(CODE_SHOW_DISTRIBUTOR_PROFILE) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_DISTRIBUTOR_PROFILE) : false;
        if (hashMapHHTModuleConfig.get(CODE_SHOW_DISTRIBUTOR_PROFILE) != null
                && hashMapHHTModuleOrder.get(CODE_SHOW_DISTRIBUTOR_PROFILE) != null) {
            SHOW_DISTRIBUTOR_PROFILE_FROM = hashMapHHTModuleOrder.get(CODE_SHOW_DISTRIBUTOR_PROFILE);
        }
        this.SHOW_SBD_GAP_IN_PROFILE = hashMapHHTModuleConfig.get(CODE_SBD_GAP_PROFILE) != null ? hashMapHHTModuleConfig.get(CODE_SBD_GAP_PROFILE) : false;
        if (hashMapHHTModuleConfig.get(CODE_SBD_TARGET_PERCENT) != null && hashMapHHTModuleConfig.get(CODE_SBD_TARGET_PERCENT)) {
            SBD_TARGET_PERCENTAGE = hashMapHHTModuleOrder.get(CODE_SBD_TARGET_PERCENT);
        }

        this.IS_ORDER_SPLIT = hashMapHHTModuleConfig.get(CODE_SPLIT_ORDER) != null ? hashMapHHTModuleConfig.get(CODE_SPLIT_ORDER) : false;

        this.SHOW_PHOTO_ODAMETER = hashMapHHTModuleConfig.get(CODE_PHOTODAM) != null ? hashMapHHTModuleConfig.get(CODE_PHOTODAM) : false;


        this.IS_BEAT_WISE_RETAILER_MAPPING = hashMapHHTModuleConfig.get(CODE_BEAT_WISE_RETAILER) != null ? hashMapHHTModuleConfig.get(CODE_BEAT_WISE_RETAILER) : false;
        this.IS_FILTER_TAG_PRODUCTS = hashMapHHTModuleConfig.get(CODE_FILTER_TAGGED_PRODUCTS) != null ? hashMapHHTModuleConfig.get(CODE_FILTER_TAGGED_PRODUCTS) : false;
        this.IS_ENABLE_PRODUCT_TAGGING_VALIDATION = hashMapHHTModuleConfig.get(CODE_ENABLE_PRODUCT_TAGGING_VALIDATION) != null ? hashMapHHTModuleConfig.get(CODE_ENABLE_PRODUCT_TAGGING_VALIDATION) : false;
        this.IS_ENABLE_SHARE_PERCENTAGE_STOCK_CHECK = hashMapHHTModuleConfig.get(CODE_ENABLE_SHARE_PERCENTAGE_STOCK_CHECK) != null ? hashMapHHTModuleConfig.get(CODE_ENABLE_SHARE_PERCENTAGE_STOCK_CHECK) : false;

        this.PLANO_IMG_COUNT = hashMapHHTModuleOrder.get(CODE_PLANO_IMG_COUNT) != null ? hashMapHHTModuleOrder.get(CODE_PLANO_IMG_COUNT) : 1;
        if (hashMapHHTModuleOrder.get(CODE_RETAILER_CONTACT_COUNT) != null) {
            this.RETAILER_CONTACT_COUNT = hashMapHHTModuleOrder.get(CODE_RETAILER_CONTACT_COUNT) != null ? hashMapHHTModuleOrder.get(CODE_RETAILER_CONTACT_COUNT) : 1;
        }

        this.TASK_OPEN = hashMapHHTModuleOrder.get(CODE_TASK_OPEN) != null ? hashMapHHTModuleOrder.get(CODE_TASK_OPEN) : 0;
        this.TASK_PLANNED = hashMapHHTModuleOrder.get(CODE_TASK_PLANNED) != null ? hashMapHHTModuleOrder.get(CODE_TASK_PLANNED) : -1;
        this.IS_SELLER_TASK_RPT = hashMapHHTModuleConfig.get(CODE_TASK_SELLER_RPT) != null ? hashMapHHTModuleConfig.get(CODE_TASK_SELLER_RPT) : false;

        this.IS_ENABLE_PROMOTION_SKUNAME = hashMapHHTModuleConfig.get(CODE_ENABLE_PROMOTION_SKUNAME) != null ? hashMapHHTModuleConfig.get(CODE_ENABLE_PROMOTION_SKUNAME) : false;
        this.IS_ENABLE_PROMOTION_DATES = hashMapHHTModuleConfig.get(CODE_ENABLE_PROMOTION_DATES) != null ? hashMapHHTModuleConfig.get(CODE_ENABLE_PROMOTION_DATES) : false;
        this.IS_ENABLE_ORDER_STATUS_REPORT = hashMapHHTModuleConfig.get(CODE_ORDER_STATUS_REPORT) != null ? hashMapHHTModuleConfig.get(CODE_ORDER_STATUS_REPORT) : false;
        if (IS_ENABLE_ORDER_STATUS_REPORT) {
            loadOrderStatusReportConfiguration();
        }
        this.IS_ENABLE_LAST_VISIT_HISTORY = hashMapHHTModuleConfig.get(CODE_ENABLE_LAST_VISIT_HISTORY) != null ? hashMapHHTModuleConfig.get(CODE_ENABLE_LAST_VISIT_HISTORY) : false;

        this.IS_ENABLE_USER_FILTER_DASHBOARD = hashMapHHTModuleConfig.get(CODE_ENABLE_USER_FILTER_DASHBOARD) != null ? hashMapHHTModuleConfig.get(CODE_ENABLE_USER_FILTER_DASHBOARD) : false;
        if (IS_ENABLE_USER_FILTER_DASHBOARD) {
            loadDashboardUserFilter();
        }
        this.IS_ENABLE_LICENSE_VALIDATION = hashMapHHTModuleConfig.get(CODE_LICENSE_VALIDATION) != null ? hashMapHHTModuleConfig.get(CODE_LICENSE_VALIDATION) : false;
        if (IS_ENABLE_LICENSE_VALIDATION) {
            loadLicenseValidationConfig();
        }
        //CODE_SHOW_ALL_SKU_ON_EDIT
        this.IS_SHOW_ALL_SKU_ON_EDIT = hashMapHHTModuleConfig.get(CODE_SHOW_ALL_SKU_ON_EDIT) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_ALL_SKU_ON_EDIT) : false;
        this.IS_KPI_CALENDAR = hashMapHHTModuleConfig.get(CODE_KPI_CALENDAR) != null ? hashMapHHTModuleConfig.get(CODE_KPI_CALENDAR) : false;

        this.IS_SHOW_SKU_CODE = hashMapHHTModuleConfig.get(CODE_SHOW_SKU_CODE) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_SKU_CODE) : false;

        if (hashMapHHTModuleConfig.get(CODE_GST_TAX_LOCATION_TYPE) != null) {
            getLocationTaxGSTModel(CODE_GST_TAX_LOCATION_TYPE);
        }

        this.IS_CHECK_PHOTO_MANDATORY = hashMapHHTModuleConfig.get(CODE_CHECK_PHOTO_MANDATORY) != null ? hashMapHHTModuleConfig.get(CODE_CHECK_PHOTO_MANDATORY) : false;
        this.IS_CHECK_MODULE_MANDATORY = hashMapHHTModuleConfig.get(CODE_SHOW_MODULE_MANDATORY) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_MODULE_MANDATORY) : false;
        this.IS_CONTACT_TAB = hashMapHHTModuleConfig.get(CODE_RETAILER_CONTACT_TAB) != null ? hashMapHHTModuleConfig.get(CODE_RETAILER_CONTACT_TAB) : false;

        this.IS_DISCOUNT_PRICE_PER = hashMapHHTModuleConfig.get(CODE_DISCOUNT_PRICE_PER) != null ? hashMapHHTModuleConfig.get(CODE_DISCOUNT_PRICE_PER) : false;
        this.DISCOUNT_PRICE_PER = hashMapHHTModuleOrder.get(CODE_DISCOUNT_PRICE_PER) != null ? hashMapHHTModuleOrder.get(CODE_DISCOUNT_PRICE_PER) : 50;

        this.IS_SR_VALIDATE_BY_RETAILER_TYPE = hashMapHHTModuleConfig.get(CODE_SR_VALIDATE_BY_RETAILER_TYPE) != null ? hashMapHHTModuleConfig.get(CODE_SR_VALIDATE_BY_RETAILER_TYPE) : false;
        this.IS_SR_RETURN_OR_REPLACE_AT_ANY_LEVEL = hashMapHHTModuleConfig.get(CODE_SR_RETURN_OR_REPLACE_AT_ANY_LEVEL) != null ? hashMapHHTModuleConfig.get(CODE_SR_RETURN_OR_REPLACE_AT_ANY_LEVEL) : false;
        this.IS_INDICATIVE_MASTER = hashMapHHTModuleConfig.get(CODE_SR_INDICATIVE) != null ? hashMapHHTModuleConfig.get(CODE_SR_INDICATIVE) : false;


        this.IS_NAVIGATE_CREDIT_NOTE_SCREEN = hashMapHHTModuleConfig.get(CODE_NAVIGATE_CREDIT_NOTE_SCREEN) != null ? hashMapHHTModuleConfig.get(CODE_NAVIGATE_CREDIT_NOTE_SCREEN) : false;
        this.SHOW_NO_COLLECTION_REASON = hashMapHHTModuleConfig.get(CODE_NO_COLLECTION_REASON) != null ? hashMapHHTModuleConfig.get(CODE_NO_COLLECTION_REASON) : false;
        // Unload non salable product returns.
        this.SHOW_NON_SALABLE_UNLOAD = hashMapHHTModuleConfig.get(CODE_NON_SALABLE_UNLOAD) != null ? hashMapHHTModuleConfig.get(CODE_NON_SALABLE_UNLOAD) : false;
        this.SHOW_GLOBAL_NO_ORDER_REASON = hashMapHHTModuleConfig.get(CODE_GLOBAL_SHOW_NO_ORDER_REASON) != null ? hashMapHHTModuleConfig.get(CODE_GLOBAL_SHOW_NO_ORDER_REASON) : false;
        this.IS_FREE_SIH_AVAILABLE = hashMapHHTModuleConfig.get(CODE_FREE_SIH_AVAILABLE) != null ? hashMapHHTModuleConfig.get(CODE_FREE_SIH_AVAILABLE) : false;

        this.IS_FIREBASE_CHAT_ENABLED = hashMapHHTModuleConfig.get(CODE_MENU_FIREBASE_CHAT) != null ? hashMapHHTModuleConfig.get(CODE_MENU_FIREBASE_CHAT) : false;
        this.IS_SR_DELIVERY_SKU_LEVEL = hashMapHHTModuleConfig.get(CODE_SALES_RETURN_DELIVERY_SKU_LEVEL) != null ? hashMapHHTModuleConfig.get(CODE_SALES_RETURN_DELIVERY_SKU_LEVEL) : false;
        this.IS_SR_DELIVERY_REJECT = hashMapHHTModuleConfig.get(CODE_REJECT_SALES_RETURN_DELIVERY) != null ? hashMapHHTModuleConfig.get(CODE_REJECT_SALES_RETURN_DELIVERY) : false;
        this.IS_SHOW_OOS = hashMapHHTModuleConfig.get(CODE_SHOW_OOS) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_OOS) : false;
        this.IS_LOAD_STK_CHECK_LAST_VISIT = hashMapHHTModuleConfig.get(CODE_STK_CHECK_LAST_VISIT) != null ? hashMapHHTModuleConfig.get(CODE_STK_CHECK_LAST_VISIT) : false;

        this.IS_SKIP_SCHEME_APPLY = hashMapHHTModuleConfig.get(CODE_SKIP_SCHEME_APPLY) != null ? hashMapHHTModuleConfig.get(CODE_SKIP_SCHEME_APPLY) : false;
        if (hashMapHHTModuleConfig.get(CODE_BATCH_ALLOCATION) != null) {
            IS_ORD_BY_BATCH_EXPIRY_DATE_WISE = hashMapHHTModuleOrder.get(CODE_BATCH_ALLOCATION) == 1;
        }
        this.IS_SHOW_TERMS_COND = hashMapHHTModuleConfig.get(CODE_SHOW_TERMS_COND) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_TERMS_COND) : false;

        this.IS_VOICE_TO_TEXT = hashMapHHTModuleOrder.get(CODE_VOICE_TO_TEXT) != null ? hashMapHHTModuleOrder.get(CODE_VOICE_TO_TEXT) : -1;
        this.IS_SKIP_CALL_ANALYSIS = hashMapHHTModuleConfig.get(CODE_SKIP_CALL_ANALYSIS) != null ? hashMapHHTModuleConfig.get(CODE_SKIP_CALL_ANALYSIS) : false;
        this.IS_COLLECTION_DELETE = hashMapHHTModuleConfig.get(CODE_COLLECTION_DELETE) != null ? hashMapHHTModuleConfig.get(CODE_COLLECTION_DELETE) : false;
        this.IS_VALIDATE_DUE_DAYS = hashMapHHTModuleConfig.get(CODE_VALIDATE_DUE_DATE) != null ? hashMapHHTModuleConfig.get(CODE_VALIDATE_DUE_DATE) : false;
        this.IS_SHOW_TASK_PRODUCT_LEVEL = hashMapHHTModuleConfig.get(CODE_SHOW_TASK_PRODUCT_LEVEL) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_TASK_PRODUCT_LEVEL) : false;
        if (hashMapHHTModuleConfig.get(CODE_SHOW_TASK_PRODUCT_LEVEL) != null) {
            TASK_PRODUCT_LEVEL_NO = hashMapHHTModuleOrder.get(CODE_SHOW_TASK_PRODUCT_LEVEL);
        }

        if (hashMapHHTModuleConfig.get(CODE_TASK_DUDE_DATE_COUNT) != null) {
            IS_TASK_DUDE_DATE_COUNT = hashMapHHTModuleOrder.get(CODE_TASK_DUDE_DATE_COUNT);
        }
        this.IS_TASK_REMARKS_MANDATORY = hashMapHHTModuleConfig.get(CODE_TASK_REMARKS_MANDATORY) != null ? hashMapHHTModuleConfig.get(CODE_TASK_REMARKS_MANDATORY) : false;

        this.IS_SHOW_RETAILER_LAST_VISIT = hashMapHHTModuleConfig.get(CODE_SHOW_RETAILER_LAST_VISIT) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_RETAILER_LAST_VISIT) : false;
        this.IS_SHOW_RETAILER_LAST_VISITEDBY = isShowLastVisitedBy();

        this.IS_ENABLE_TRIP = hashMapHHTModuleConfig.get(CODE_TO_ENABLE_TRIP) != null ? hashMapHHTModuleConfig.get(CODE_TO_ENABLE_TRIP) : false;
        if (hashMapHHTModuleOrder.get(CODE_TO_ENABLE_TRIP) != null && hashMapHHTModuleOrder.get(CODE_TO_ENABLE_TRIP) == 1)
            this.IS_ALLOW_USER_TO_CONTINUE_FOR_MULTIPLE_DAYS_WITH_SAME_TRIP = true;
        else this.IS_ALLOW_USER_TO_CONTINUE_FOR_MULTIPLE_DAYS_WITH_SAME_TRIP = false;
        this.ret_skip_otp_flag = hashMapHHTModuleOrder.get(CODE_SHOW_LOCATION_PWD_DIALOG) != null ? hashMapHHTModuleOrder.get(CODE_SHOW_LOCATION_PWD_DIALOG) : 0;
        this.IS_DISABLE_CALL_ANALYSIS_TIMER = hashMapHHTModuleConfig.get(CODE_DISABLE_CALL_ANALAYSIS_TIMER) != null ? hashMapHHTModuleConfig.get(CODE_DISABLE_CALL_ANALAYSIS_TIMER) : false;
        this.IS_SHOW_PAUSE_CALL_ANALYSIS = hashMapHHTModuleConfig.get(CODE_SHOW_PAUSE_CALL_ANALYSIS) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_PAUSE_CALL_ANALYSIS) : false;

        this.IS_DISPLAY_ASSET_RETAIN_LAST_VISIT_TRAN = hashMapHHTModuleConfig.get(CODE_DISPLAY_ASSET_RETAIN_LAST_VISIT_TRAN) != null ? hashMapHHTModuleConfig.get(CODE_DISPLAY_ASSET_RETAIN_LAST_VISIT_TRAN) : false;

        this.IS_ENABLE_EDIT_OPTION_FOR_OTHER_USER = hashMapHHTModuleConfig.get(CODE_ENABLE_EDIT_OPTION_FOR_OTHER_USER) != null ? hashMapHHTModuleConfig.get(CODE_ENABLE_EDIT_OPTION_FOR_OTHER_USER) : false;
        this.GLOBAL_GPS_DISTANCE = hashMapHHTModuleOrder.get(CODE_ENABLE_GLOBAL_GPS_DISTANCE) != null ? hashMapHHTModuleOrder.get(CODE_ENABLE_GLOBAL_GPS_DISTANCE) : 0;
        this.IS_SURVEY_PDF_SHARE = hashMapHHTModuleConfig.get(CODE_SURVEY_PDF_SHARE) != null ? hashMapHHTModuleConfig.get(CODE_SURVEY_PDF_SHARE) : false;
        this.IS_PRE_VISIT = hashMapHHTModuleConfig.get(CODE_PRE_VISIT) != null ? hashMapHHTModuleConfig.get(CODE_PRE_VISIT) : false;
        this.IS_SHOW_SORT_STKCHK = hashMapHHTModuleConfig.get(CODE_SHOW_SORT_STKCHK) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_SORT_STKCHK) : false;
        this.IS_SHOW_EXPLIST_IN_PROMO = hashMapHHTModuleConfig.get(CODE_SHOW_EXPLIST_IN_PROMO) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_EXPLIST_IN_PROMO) : false;
        this.IS_SHOW_ANNOUNCEMENT = hashMapHHTModuleConfig.get(CODE_SHOW_ANNOUNCEMENT) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_ANNOUNCEMENT) : false;
        this.IS_SHOW_NOTIFICATION = hashMapHHTModuleConfig.get(CODE_SHOW_NOTIFICATION) != null ? hashMapHHTModuleConfig.get(CODE_SHOW_NOTIFICATION) : false;
    }

    private boolean isInOutModule() {
        boolean isInOutModule = false;
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        try {
            db.openDataBase();
            Cursor c = db.selectSQL("select RField from HhtMenuMaster where hhtCode='MENU_IN_OUT'");
            if (c.getCount() > 0 && c.moveToNext()) {
                isInOutModule = true;
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }
        return isInOutModule;
    }

    public void loadOrderReportConfiguration() {
        try {

            SHOW_DELIVERY_DATE_IN_ORDER_RPT = false;

            String codeValue = null;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            String sql = "select RField from "
                    + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode=" + bmodel.QT(CODE_ORDER_RPT_CONFIG) + " and Flag=1 and ForSwitchSeller = 0";
            Cursor c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    codeValue = c.getString(0);
                }
                c.close();
            }

            if (codeValue != null) {
                String codeSplit[] = codeValue.split(",");
                for (String temp : codeSplit) {
                    switch (temp) {
                        case "DELVDATE":
                            SHOW_DELIVERY_DATE_IN_ORDER_RPT = true;
                            break;

                    }

                }
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public void loadOrderStatusReportConfiguration() {
        try {

            IS_ORDER_STATUS_REPORT = false;

            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            String sql = "select RField from "
                    + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode=" + bmodel.QT(CODE_ORDER_STATUS_REPORT) + " and Flag=1 and ForSwitchSeller = 0";
            Cursor c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    if (c.getString(0).equals("0")) {
                        IS_ORDER_STATUS_REPORT = true;
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
     * @See {@link UserDataManagerImpl#fetchUsers()}
     * @deprecated
     */
    public void loadDashboardUserFilter() {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            String sql = "select RField from "
                    + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode=" + bmodel.QT(CODE_ENABLE_USER_FILTER_DASHBOARD) + " and Flag=1 and ForSwitchSeller = 0";
            Cursor c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    bmodel.setDashboardUserFilterString(c.getString(0).replaceAll("^|$", "'").replaceAll(",", "','"));
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public void loadLicenseValidationConfig() {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            String sql = "select RField from "
                    + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode=" + bmodel.QT(CODE_LICENSE_VALIDATION) + " and Flag=1 and ForSwitchSeller = 0";
            Cursor c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    IS_SOFT_LICENSE_VALIDATION = c.getString(0).equals("0");
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    /**
     * @param hhtCode for genral tax model
     */

    private void getTaxModel(String hhtCode) {


        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        try {
            db.openDataBase();
            Cursor c = db.selectSQL("select RField from HhtModuleMaster where hhtCode='" + hhtCode + "' and  ForSwitchSeller = 0");
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    String RField = c.getString(0);
                    if (RField.equals("GST_IN")) {
                        IS_GST = true;
                        IS_GST_MASTER = true;
                    }
                    if (RField.equals("GST_HSN")) {
                        IS_GST_HSN = true;
                        IS_GST_HSN_MASTER = true;
                    }

                }
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }

    }

    /**
     * @param hhtCode tax model for when switch user
     */

    private void getTaxModelSwitchUser(String hhtCode) {


        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        try {
            db.openDataBase();
            Cursor c = db.selectSQL("select RField from HhtModuleMaster where hhtCode='" + hhtCode + "' and  ForSwitchSeller = 1 ");
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    String RField = c.getString(0);
                    if (RField.equals("GST_IN"))
                        IS_GST = true;

                    if (RField.equals("GST_HSN"))
                        IS_GST_HSN = true;


                }
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }

    }

    /**
     * @param hhtCode for general Location wise GST Tax Model
     */
    private void getLocationTaxGSTModel(String hhtCode) {


        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        try {
            db.openDataBase();
            Cursor c = db.selectSQL("select RField from HhtModuleMaster where hhtCode='" + hhtCode + "' and  ForSwitchSeller = 0");
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    String RField = c.getString(0);
                    if (RField.equals("TAX_LOC")) {
                        IS_TAX_LOC = true;
                    }
                }
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }

    }




    /*get IS_ATTRIBUTE_MENU boolean

     */

    private boolean isAttributeMenu(String hhtCode) {


        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        try {
            db.openDataBase();
            Cursor c = db.selectSQL("select RField from HhtModuleMaster where hhtCode='" + hhtCode + "'  and  ForSwitchSeller = 0");
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    String RField = c.getString(0);
                    if (RField.equals("RTR_ATTRIBUTES")) {
                        IS_ATTRIBUTE_MENU = true;

                    }
                }
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }
        return IS_ATTRIBUTE_MENU;
    }

    /**
     * @return sd
     * @See {@link com.ivy.core.data.db.AppDataManagerImpl#fetchNewActivityMenu(String)}
     * This method will download the Menu configured for this particular channel
     * type. This will also download the Menu Name,Number and hasLink attributes
     * @deprecated
     */
    public Vector<ConfigureBO> downloadNewActivityMenu(String menuName) {
        activitymenuconfig = new Vector<>();
        try {
            sharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(context);
            String language = sharedPrefs.getString("languagePref",
                    ApplicationConfigs.LANGUAGE);
            String sql = "";
            String sql1 = "";
            if (!IS_ATTRIBUTE_MENU) {
                sql = "select hhtCode, flag, RField,MName,MNumber,hasLink,RField1 from "
                        + DataMembers.tbl_HhtMenuMaster
                        + " where hhtCode like 'MENU_%' and lang="
                        + bmodel.QT(language)
                        + " and flag=1 and SubChannelId = "
                        + bmodel.retailerMasterBO.getSubchannelid()
                        + " and AttributeId = 0 and MenuType="
                        + bmodel.QT(menuName)
                        + " order by MNumber";

                sql1 = "select hhtCode, flag, RField,MName,MNumber,hasLink,RField1 from "
                        + DataMembers.tbl_HhtMenuMaster
                        + " where hhtCode like 'MENU_%' and lang="
                        + bmodel.QT(language)
                        + " and flag=1 and SubChannelId =0 "
                        + " and AttributeId = 0 and MenuType="
                        + bmodel.QT(menuName)
                        + " order by MNumber";
            } else {
                sql = "select hhtCode, flag, RField,MName,MNumber,hasLink,RField1 from "
                        + DataMembers.tbl_HhtMenuMaster
                        + " where hhtCode like 'MENU_%' and lang="
                        + bmodel.QT(language)
                        + " and flag=1 and attributeId in (0, "
                        + bmodel.getRetailerAttributeList()
                        + ") and MenuType="
                        + bmodel.QT(menuName)
                        + " order by MNumber";
            }

            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();

            IS_ORDER_STOCK = false;

            Cursor c = db.selectSQL(sql);
            if (c != null) {
                if (c.getCount() == 0) {
                    c = db.selectSQL(sql1);
                }
            }

            ConfigureBO con;
            if (c != null) {
                while (c.moveToNext()) {
                    con = new ConfigureBO();
                    con.setConfigCode(c.getString(0));
                    con.setFlag(c.getInt(1));
                    con.setModule_Order(c.getInt(2));
                    con.setMenuName(c.getString(3));
                    con.setMenuNumber(c.getString(4));
                    con.setHasLink(c.getInt(5));
                    con.setMandatory(c.getInt(6));
                    activitymenuconfig.add(con);

                    if (c.getString(0).equals("MENU_STK_ORD"))
                        IS_ORDER_STOCK = true;

                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return activitymenuconfig;
    }

    public Vector<ConfigureBO> downloadStoreCheckMenu(String menuName) {
        storeCheckMenu = new Vector<>();
        try {
            sharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(context);
            String language = sharedPrefs.getString("languagePref",
                    ApplicationConfigs.LANGUAGE);
            String sql = "";
            String sql1 = "";
            if (!IS_ATTRIBUTE_MENU) {
                sql = "select hhtCode, flag, RField,MName,MNumber,hasLink,RField1 from "
                        + DataMembers.tbl_HhtMenuMaster
                        + " where hhtCode like 'MENU_%' and lang="
                        + bmodel.QT(language)
                        + " and flag=1 and AttributeId = 0 and SubChannelId = "
                        + bmodel.retailerMasterBO.getSubchannelid()
                        + " and MenuType="
                        + bmodel.QT(menuName)
                        + " order by MNumber";

                sql1 = "select hhtCode, flag, RField,MName,MNumber,hasLink,RField1 from "
                        + DataMembers.tbl_HhtMenuMaster
                        + " where hhtCode like 'MENU_%' and lang="
                        + bmodel.QT(language)
                        + " and flag=1 and AttributeId = 0 and SubChannelId =0 "
                        + " and MenuType="
                        + bmodel.QT(menuName)
                        + " order by MNumber";
            } else {
                sql = "select hhtCode, flag, RField,MName,MNumber,hasLink,RField1 from "
                        + DataMembers.tbl_HhtMenuMaster
                        + " where hhtCode like 'MENU_%' and lang="
                        + bmodel.QT(language)
                        + " and flag=1 and attributeId in (0, "
                        + bmodel.getRetailerAttributeList()
                        + ") and MenuType="
                        + bmodel.QT(menuName)
                        + " order by MNumber";
            }

            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();


            Cursor c = db.selectSQL(sql);
            if (c != null) {
                if (c.getCount() == 0) {
                    c = db.selectSQL(sql1);
                }
            }

            ConfigureBO con;
            if (c != null) {
                if (c.getCount() > 0) {
                    while (c.moveToNext()) {
                        con = new ConfigureBO();
                        con.setConfigCode(c.getString(0));
                        con.setFlag(c.getInt(1));
                        con.setModule_Order(c.getInt(2));
                        con.setMenuName(c.getString(3));
                        con.setMenuNumber(c.getString(4));
                        con.setHasLink(c.getInt(5));
                        con.setMandatory(c.getInt(6));
                        storeCheckMenu.add(con);

                    }
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return storeCheckMenu;
    }


    public Vector<ConfigureBO> getStoreCheckMenu() {
        return storeCheckMenu;
    }

    public Vector<ConfigureBO> getActivityMenu() {
        return activitymenuconfig;

    }

    /**
     * download the menus for HomeScreenFragment
     *
     * @return
     */
    public Vector<ConfigureBO> downloadMainMenu() {
        config = new Vector<>();

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String language = sharedPrefs.getString("languagePref",
                ApplicationConfigs.LANGUAGE);
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();

            String sql = "select distinct hhtCode, flag, RField,MName,RField1,hasLink,MNumber from "
                    + DataMembers.tbl_HhtMenuMaster
                    + " where  flag=1 and MenuType="
                    + bmodel.QT(MENU_HOME)
                    + " and lang="
                    + bmodel.QT(language)
                    + " order by RField";

            Cursor c = db.selectSQL(sql);

            ConfigureBO con;
            if (c != null) {
                while (c.moveToNext()) {
                    con = new ConfigureBO();
                    con.setConfigCode(c.getString(0));
                    con.setFlag(c.getInt(1));
                    con.setModule_Order(c.getInt(2));
                    con.setMenuName(c.getString(3));
                    con.setMandatory(c.getInt(4));
                    con.setHasLink(c.getInt(5));
                    con.setMenuNumber(c.getString(6));

                    config.add(con);

                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return config;
    }


    public Vector<ConfigureBO> getStockistMenu() {
        return primarymenus;
    }

    public void downloadStockistMenu() {
        primarymenus = new Vector<>();

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String language = sharedPrefs.getString("languagePref",
                ApplicationConfigs.LANGUAGE);

        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();

            String sql = "select hhtCode, flag, RField,MName, hasLink from "
                    + DataMembers.tbl_HhtMenuMaster
                    + " where  flag=1 and MenuType="
                    + bmodel.QT(MENU_PRIMARY_SALES) + " and lang="
                    + bmodel.QT(language) + " order by RField";

            Cursor c = db.selectSQL(sql);

            ConfigureBO con;
            if (c != null) {
                while (c.moveToNext()) {
                    con = new ConfigureBO();
                    con.setConfigCode(c.getString(0));
                    con.setFlag(c.getInt(1));
                    con.setModule_Order(c.getInt(2));
                    con.setMenuName(c.getString(3));
                    con.setHasLink(c.getInt(4));
                    primarymenus.add(con);

                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public Vector<ConfigureBO> downloadLoadManagementMenu() {
        Vector<ConfigureBO> loadmanagementmenuconfig = new Vector<>();

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String language = sharedPrefs.getString("languagePref",
                ApplicationConfigs.LANGUAGE);

        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();

            String sql = "select hhtCode, flag, RField,MName,RField1 from "
                    + DataMembers.tbl_HhtMenuMaster
                    + " where  flag=1 and MenuType="
                    + bmodel.QT(MENU_LOAD_MANAGEMENT) + " and lang="
                    + bmodel.QT(language) + " order by RField";

            Cursor c = db.selectSQL(sql);

            ConfigureBO con;
            if (c != null) {
                while (c.moveToNext()) {
                    con = new ConfigureBO();
                    con.setConfigCode(c.getString(0));
                    con.setFlag(c.getInt(1));
                    con.setModule_Order(c.getInt(2));
                    con.setMenuName(c.getString(3));
                    con.setMandatory(c.getInt(4));
                    loadmanagementmenuconfig.add(con);

                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }

        return loadmanagementmenuconfig;
    }

    public Vector<ConfigureBO> downloadPlanningSubMenu() {
        config = new Vector<>();

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String language = sharedPrefs.getString("languagePref",
                ApplicationConfigs.LANGUAGE);

        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();

            String sql = "select hhtCode, flag, RField,MName,RField1 from "
                    + DataMembers.tbl_HhtMenuMaster
                    + " where  flag=1 and MenuType="
                    + bmodel.QT(MENU_PLANNING_SUB) + " and lang="
                    + bmodel.QT(language) + " order by RField";

            Cursor c = db.selectSQL(sql);

            ConfigureBO con;
            if (c != null) {
                while (c.moveToNext()) {
                    con = new ConfigureBO();
                    con.setConfigCode(c.getString(0));
                    con.setFlag(c.getInt(1));
                    con.setModule_Order(c.getInt(2));
                    con.setMenuName(c.getString(3));
                    con.setMandatory(c.getInt(4));
                    config.add(con);

                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }

        return config;
    }

    public Vector<ConfigureBO> downloadCallAnalysisMenu() {
        config = new Vector<>();

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String language = sharedPrefs.getString("languagePref",
                ApplicationConfigs.LANGUAGE);
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();

            String sql = "select hhtCode,MName,MNumber from "
                    + DataMembers.tbl_HhtMenuMaster
                    + " where  flag=1 and MenuType="
                    + bmodel.QT(MENU_CALL_ANALYSIS) + " and lang="
                    + bmodel.QT(language) + " order by RField";

            Cursor c = db.selectSQL(sql);

            ConfigureBO con;
            if (c != null) {
                while (c.moveToNext()) {
                    con = new ConfigureBO();
                    con.setConfigCode(c.getString(0));
                    con.setMenuName(c.getString(1));
                    con.setMandatory(c.getInt(2));
                    config.add(con);

                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }

        return config;

    }

    public Vector<ConfigureBO> downloadDayReportList() {
        config = new Vector<>();

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String language = sharedPrefs.getString("languagePref",
                ApplicationConfigs.LANGUAGE);

        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();

            String sql = "select hhtCode,MName from "
                    + DataMembers.tbl_HhtMenuMaster
                    + " where  flag=1 and MenuType=" + bmodel.QT("REPORT01")
                    + " and lang=" + bmodel.QT(language) + " order by RField";

            Cursor c = db.selectSQL(sql);

            ConfigureBO con;
            if (c != null) {
                while (c.moveToNext()) {
                    con = new ConfigureBO();
                    con.setConfigCode(c.getString(0));
                    con.setMenuName(c.getString(1));
                    config.add(con);

                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }

        return config;

    }

    public ArrayList<VisitConfiguration> downloadVisitFragDatas(String menutype) {
        ArrayList<VisitConfiguration> config = new ArrayList<>();

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String language = sharedPrefs.getString("languagePref",
                ApplicationConfigs.LANGUAGE);

        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();

            String sql = "select hhtCode, MName from "
                    + DataMembers.tbl_HhtMenuMaster
                    + " where  flag=1 and MenuType=" + bmodel.QT(menutype)
                    + " and lang=" + bmodel.QT(language) + " and hhtcode like 'VST%' order by RField";

            Cursor c = db.selectSQL(sql);

            VisitConfiguration con;
            if (c != null) {
                while (c.moveToNext()) {
                    con = new VisitConfiguration(c.getString(0), c.getString(1));
                    config.add(con);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }

        return config;
    }

    public void downloadCompetitorFilterLevels() {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();

            String sql = "select RField from "
                    + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode='" + CODE_SHOW_COMPETITOR_FILTER + "' and Flag=1 and ForSwitchSeller = 0";
            Cursor c = db.selectSQL(sql);
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    COMPETITOR_FILTER_LEVELS = c.getString(0);
                }
                c.close();
            }

            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    public void loadCompetitorConfig() {
        try {
            SHOW_TIME_VIEW = false;
            SHOW_SPINNER = false;
            SHOW_COMP_QTY = false;
            SHOW_COMP_FEEDBACK = false;

            String codeValue = null;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            String sql = "select RField from "
                    + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode='" + CODE_COMPETITOR + "' and Flag=1 and ForSwitchSeller = 0";
            Cursor c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    codeValue = c.getString(0);
                }
                c.close();
            }

            if (codeValue != null) {
                String codeSplit[] = codeValue.split(",");
                for (String temp : codeSplit) {
                    switch (temp) {
                        case "DATE":
                            SHOW_TIME_VIEW = true;
                            break;
                        case "RSN":
                            SHOW_SPINNER = true;
                            break;
                        case "QTY":
                            SHOW_COMP_QTY = true;
                            break;
                        case "FEEDBACK":
                            SHOW_COMP_FEEDBACK = true;
                            break;
                    }

                }
            }

            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    public void loadDeliveryUOMConfiguration() {
        try {

            SHOW_DELIVERY_PC = false;
            SHOW_DELIVERY_CA = false;
            SHOW_DELIVERY_OU = false;
            SHOW_SALES_RETURN_IN_DELIVERY = false;

            String codeValue = null;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            String sql = "select RField from "
                    + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode='DELIVERY01' and Flag=1 and ForSwitchSeller = 0";
            Cursor c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    codeValue = c.getString(0);
                }
                c.close();
            }

            if (codeValue != null) {
                String codeSplit[] = codeValue.split(",");
                for (String temp : codeSplit) {
                    switch (temp) {
                        case "OP":
                            SHOW_DELIVERY_PC = true;
                            break;
                        case "OO":
                            SHOW_DELIVERY_OU = true;
                            break;
                        case "OC":
                            SHOW_DELIVERY_CA = true;
                            break;
                        case "SR":
                            SHOW_SALES_RETURN_IN_DELIVERY = true;
                            break;
                    }

                }
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public void loadProfileHistoryConfiguration() {
        try {

            SHOW_HST_DELDATE = false;
            SHOW_HST_DELSTATUS = false;
            SHOW_HST_INVDATE = false;
            SHOW_HST_INVQTY = false;
            SHOW_HST_REPCODE = false;
            SHOW_HST_TOTAL = false;
            SHOW_HST_VOLUM = false;
            SHOW_HST_STARTDATE = false;
            SHOW_HST_DUETDATE = false;
            SHOW_HST_PAID_AMOUNT = false;
            SHOW_HST_BAL_AMOUNT = false;
            SHOW_HST_DRIVER_NAME = false;
            SHOW_HST_PO_NUM = false;
            SHOW_HST_DOC_NO = false;

            SHOW_ORDER_HISTORY_DETAILS = false;

            String codeValue = null;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            String sql = "select RField from "
                    + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode='HST01' and Flag=1 and ForSwitchSeller = 0";
            Cursor c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    codeValue = c.getString(0);
                }
                c.close();
            }

            if (codeValue != null) {
                String codeSplit[] = codeValue.split(",");
                for (String temp : codeSplit) {
                    switch (temp) {
                        case "DEDT":
                            SHOW_HST_DELDATE = true;
                            break;
                        case "INDT":
                            SHOW_HST_INVDATE = true;
                            break;
                        case "INVQTY":
                            SHOW_HST_INVQTY = true;
                            break;
                        case "RPC":
                            SHOW_HST_REPCODE = true;
                            break;
                        case "TOT":
                            SHOW_HST_TOTAL = true;
                            break;
                        case "VOL":
                            SHOW_HST_VOLUM = true;
                            break;
                        case "INVDT":
                            SHOW_ORDER_HISTORY_DETAILS = true;
                            break;
                        case "ST":
                            SHOW_HST_DELSTATUS = true;
                            break;
                        case "STDT":
                            SHOW_HST_STARTDATE = true;
                            break;
                        case "DUDT":
                            SHOW_HST_DUETDATE = true;
                            break;
                        case "PAMT":
                            SHOW_HST_PAID_AMOUNT = true;
                            break;
                        case "BAMT":
                            SHOW_HST_BAL_AMOUNT = true;
                            break;
                        case "PONUM":
                            SHOW_HST_PO_NUM = true;
                            break;
                        case "DRIVER":
                            SHOW_HST_DRIVER_NAME = true;
                            break;
                        case "DOCNO":
                            SHOW_HST_DOC_NO = true;
                            break;
                    }

                }
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public void loadInvoiceHistoryConfiguration() {
        try {

            SHOW_INV_HST_ORDERID = false;
            SHOW_INV_HST_INVOICEDATE = false;
            SHOW_INV_HST_INVOICEAMOUNT = false;
            SHOW_INV_HST_TOT_LINES = false;
            SHOW_INV_HST_DUEDATE = false;
            SHOW_INV_HST_OVERDUE_DAYS = false;
            SHOW_INV_HST_OS_AMOUNT = false;
            SHOW_INV_HST_STATUS = false;
            SHOW_INV_HST_VOLUME = false;
            SHOW_INV_HST_MARGIN_PRICE = false;
            SHOW_INV_HST_MARGIN_PER = false;
            SHOW_INV_ONDEMAND = false;

            SHOW_INVOICE_HISTORY_DETAIL = false;

            String codeValue = null;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            String sql = "select RField from "
                    + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode='HST02' and Flag=1 and ForSwitchSeller = 0";
            Cursor c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    codeValue = c.getString(0);
                }
                c.close();
            }

            if (codeValue != null) {
                String codeSplit[] = codeValue.split(",");
                for (String temp : codeSplit) {
                    switch (temp) {
                        case "ORDID":
                            SHOW_INV_HST_ORDERID = true;
                            break;
                        case "INV_DETAIL":
                            SHOW_INVOICE_HISTORY_DETAIL = true;
                            break;
                        case "INVDATE":
                            SHOW_INV_HST_INVOICEDATE = true;
                            break;
                        case "INVAMT":
                            SHOW_INV_HST_INVOICEAMOUNT = true;
                            break;
                        case "TL":
                            SHOW_INV_HST_TOT_LINES = true;
                            break;
                        case "DUDATE":
                            SHOW_INV_HST_DUEDATE = true;
                            break;
                        case "ODDAYS":
                            SHOW_INV_HST_OVERDUE_DAYS = true;
                            break;
                        case "OSAMT":
                            SHOW_INV_HST_OS_AMOUNT = true;
                            break;
                        case "ST":
                            SHOW_INV_HST_STATUS = true;
                            break;
                        case "VOL":
                            SHOW_INV_HST_VOLUME = true;
                            break;
                        case "MGNPRC":
                            SHOW_INV_HST_MARGIN_PRICE = true;
                            break;
                        case "MGNPER":
                            SHOW_INV_HST_MARGIN_PER = true;
                            break;
                        case "ONDMD":
                            SHOW_INV_ONDEMAND = true;
                            break;

                    }

                }
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public void loadPrimarySaleStockCheckAndOrderConfiguration() {
        try {

            String codeValue = null;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            String sql = "select RField from "
                    + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode='PSSTK01' and Flag=1 and ForSwitchSeller = 0";
            Cursor c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    codeValue = c.getString(0);
                }
                c.close();
            }

            if (codeValue != null) {

                String codeSplit[] = codeValue.split(",");
                for (String temp : codeSplit) {
                    if (temp.equals("OC"))
                        SHOW_SC = true;
                    else if (temp.equals("OO"))
                        SHOW_SHO = true;
                    else if (temp.equals("OP"))
                        SHOW_SP = true;

                }
            }

            sql = "select RField from "
                    + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode='PSORD01' and Flag=1 and ForSwitchSeller = 0";
            c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    codeValue = c.getString(0);
                }
                c.close();
            }

            if (codeValue != null) {

                String codeSplit[] = codeValue.split(",");
                for (String temp : codeSplit) {
                    if (temp.equals("OC"))
                        SHOW_DIST_ORDER_CASE = true;
                    else if (temp.equals("OO"))
                        SHOW_DIST_ORDER_OUTER = true;
                    else if (temp.equals("OP"))
                        SHOW_DIST_ORDER_PIECE = true;

                }
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public void loadCameraPictureSize() {
        try {

            String codeValue;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            String sql = "select RField from "
                    + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode='PHOTOCAP04' and Flag=1 and ForSwitchSeller = 0";
            Cursor c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    codeValue = c.getString(0);
                    String[] camera_params = codeValue.split(",");
                    CAMERA_PICTURE_WIDTH = SDUtil.convertToInt(camera_params[0]);
                    CAMERA_PICTURE_HEIGHT = SDUtil.convertToInt(camera_params[1]);
                    CAMERA_PICTURE_QUALITY = SDUtil.convertToInt(camera_params[2]) >= 40 ? SDUtil.convertToInt(camera_params[2]) : 40;
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }

    }

    public void loadMinMaxDateInChq() {
        try {

            String codeValue;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            String sql = "select RField from "
                    + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode='MIN_MAX_CHQ_DATE' and Flag=1 and ForSwitchSeller = 0";
            Cursor c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    codeValue = c.getString(0);
                    String[] min_max_params = codeValue.split(",");
                    CHQ_MIN_DATE = SDUtil.convertToInt(min_max_params[0]);
                    CHQ_MAX_DATE = SDUtil.convertToInt(min_max_params[1]);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }

    }

    public void loadProductDistributionConfig() {
        try {

            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();

            String sql = "select RField from "
                    + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode=" + bmodel.QT(CODE_SALES_DISTRIBUTION) + " and Flag=1 and ForSwitchSeller = 0";
            Cursor c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    this.PRD_DISTRIBUTION_TYPE = c.getString(0);
                }
                c.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }

    }

    /**
     * This method will load the Order and Stock screen configurations. This is
     * mandatory, otherwise stock and order wont have any text entries.
     * WP,WC,SP,SC in Stock screen will be controlled. PCS and CASE in Order
     * screen will be controlled
     * <p>
     * Configuration will be loaded based on subchannel id only
     */

    public void loadOrderAndStockConfiguration(int subChannelID) {
        try {
            SHOW_STOCK_SP = false;
            SHOW_STOCK_SC = false;
            SHOW_STOCK_CB = false;
            CHANGE_AVAL_FLOW = false;
            SHOW_STOCK_RSN = false;
            SHOW_ORDER_CASE = false;
            SHOW_ORDER_PCS = false;
            SHOW_FOC = false;
            SHOW_OUTER_CASE = false;
            SHOW_ICO = false;
            SHOW_BARCODE = false;
            SHOW_ORDER_TOTAL = false;
            SHOW_PRODUCT_CODE = false;
            SHOW_ORDER_WEIGHT = false;
            SHOW_D1 = false;
            SHOW_D2 = false;
            SHOW_D3 = false;
            SHOW_DA = false;
            SHOW_DISCOUNTED_PRICE = false;

            SHOW_INDEX_DASH = false;
            SHOW_TARGET_DASH = false;
            SHOW_ACHIEVED_DASH = false;
            SHOW_BALANCE_DASH = false;
            SHOW_INCENTIVE_DASH = false;
            SHOW_FLEX_DASH = false;
            SHOW_SCORE_DASH = false;
            SHOW_P3M_DASH = false;
            SHOW_YAI_DASH = false;
            SHOW_STOCK_SO = false;
            STOCK_DIST_INV = false;
            SHOW_INV_DASH = false;
            SHOW_SMP_DASH = false;
            SHOW_KPIBARCHART_DASH = false;

            IS_LOAD_STOCK_COMPETITOR = false;
            LOAD_STOCK_COMPETITOR = 0;
            DEFAULT_NUMBER_OF_DAYS_TO_DELIVER_ORDER = 1;
            MIN_NUMBER_OF_DAYS_ALLOWED_TO_DELIVER = 1;
            MAX_NUMBER_OF_DAYS_ALLOWED_TO_DELIVER = 1;
            LOAD_REMARKS_FIELD_STRING = "";
            SHOW_INCLUDE_BILL_TAX = false;

            SHOW_REPLACED_QTY_PC = false;
            SHOW_REPLACED_QTY_CS = false;
            SHOW_REPLACED_QTY_OU = false;

            SHOW_VANLOAD_OC = false;
            SHOW_VANLOAD_OO = false;
            SHOW_VANLOAD_OP = false;
            SHOW_NO_ORDER_CAPTURE_PHOTO = false;
            SHOW_NO_ORDER_EDITTEXT = false;
            SHOW_DEFAULT_LOCATION_POPUP = false;
            SHOW_NEW_OUTLET_OPPR = false;
            SHOW_NEW_OUTLET_ORDER = false;
            IS_STK_ORD_BS = false;
            IS_STK_ORD_PROJECT = false;
            SHOW_SALES_RETURN_IN_ORDER = false;
            SHOW_SALES_RETURN_TV_IN_ORDER = false;


            IS_PRINT_SEQUENCE_REQUIRED = false;
            IS_PRINT_SEQUENCE_LEVELWISE = false;
            IS_SHOW_DEFAULT_UOM = false;
            SHOW_SALABLE_AND_NON_SALABLE_SKU = false;
            IS_SHOW_ORDER_PHOTO_CAPTURE = false;
            IS_SHOW_ORDER_ATTACH_FILE = false;
            IS_ORD_DIGIT = false;
            ORD_DIGIT = 0;
            IS_SWITCH_WITH_OUT_TGT = false;
            SELLER_KPI_CODES = "";
            IS_SWITCH_WITH_OUT_SKU_WISE_TGT = false;
            SELLER_SKU_WISE_KPI_CODES = "";

            SHOW_LASTVISIT_GRAPH = false;

            String codeValue = null;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            String sql = "select RField from "
                    + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode='CSSTK01' and SubchannelId="
                    + subChannelID;
            Cursor c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    codeValue = c.getString(0);
                }
                c.close();
            }

            // If there is no record for that subchannel we have to load the
            // query with 0

            else {
                sql = "select RField from " + DataMembers.tbl_HhtModuleMaster
                        + " where hhtCode='CSSTK01' and SubChannelId= 0 and ForSwitchSeller = 0";
                c = db.selectSQL(sql);
                if (c != null && c.getCount() != 0) {
                    if (c.moveToNext()) {
                        codeValue = c.getString(0);
                    }
                    c.close();
                }

            }

            if (codeValue != null) {

                String codeSplit[] = codeValue.split(",");
                for (String temp : codeSplit)
                    switch (temp) {
                        case "REPPC":
                            SHOW_REPLACED_QTY_PC = true;
                            break;
                        case "REPCS":
                            SHOW_REPLACED_QTY_CS = true;
                            break;
                        case "REPOO":
                            SHOW_REPLACED_QTY_OU = true;
                            break;
                        case "CSTK":
                            IS_COMBINED_STOCK_CHECK_FROM_ORDER = true;
                            break;
                        case "SR":
                            SHOW_SALES_RETURN_IN_ORDER = true;
                            break;
                        case "SRQTY":
                            SHOW_SALES_RETURN_TV_IN_ORDER = true;
                            break;
                    }
            }


            codeValue = null;

            sql = "select RField from " + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode='ORDB10' and ForSwitchSeller = 0";
            c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    codeValue = c.getString(0);
                }
                c.close();
            }
            if (codeValue != null) {
                String codeSplit[] = codeValue.split(",");
                for (String temp : codeSplit) {
                    if (temp.equals("CS"))
                        SHOW_ORDER_CASE = true;
                    else if (temp.equals("PS"))
                        SHOW_ORDER_PCS = true;
                    else if (temp.equals("RF"))
                        SHOW_FOC = true;
                    else if (temp.equals("OOC"))
                        SHOW_OUTER_CASE = true;
                    else if (temp.equals("ICO"))
                        SHOW_ICO = true;
                    else if (temp.equals("BARCODE"))
                        SHOW_BARCODE = true;
                    else if (temp.equals("TOTAL"))
                        SHOW_ORDER_TOTAL = true;
                    else if (temp.equals(CODE_SHOW_PRODUCT_CODE))
                        SHOW_PRODUCT_CODE = true;
                    else if (temp.equals("WGT"))
                        SHOW_ORDER_WEIGHT = true;
                    else if (temp.equals("IO"))
                        SHOW_INDICATIVE_ORDER = true;
                    else if (temp.equals("CO"))
                        SHOW_CLEANED_ORDER = true;
                    else if (temp.equals("SRPEDT"))
                        SHOW_STK_ORD_SRP_EDT = true;
                    else if (temp.equals("STKQTY"))
                        SHOW_STK_QTY_IN_ORDER = true;
                }
            }

            sql = "select RField from " + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode=" + bmodel.QT(CODE_SHOW_REMARKS_STK_ORD) + " and Flag=1 and ForSwitchSeller = 0";

            c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {

                    LOAD_REMARKS_FIELD_STRING = c.getString(0);
                }
                c.close();
            }

            if (IS_INITIATIVE) {
                codeValue = null;
                sql = "select RField from " + DataMembers.tbl_HhtModuleMaster
                        + " where hhtCode='ORDB01' and ForSwitchSeller = 0";
                c = db.selectSQL(sql);
                if (c != null && c.getCount() != 0) {
                    if (c.moveToNext()) {
                        codeValue = c.getString(0);
                    }
                }
                if (codeValue != null) {
                    String codeSplit[] = codeValue.split(",");
                    for (String temp : codeSplit) {
                        switch (temp) {
                            case "CA":
                                IS_CUMULATIVE_AND = true;
                                break;
                            default:
                                break;
                        }
                    }
                }
                c.close();

            }

            codeValue = null;

            sql = "select RField from " + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode='FUN02' and ForSwitchSeller = 0";
            c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    codeValue = c.getString(0);
                }
                c.close();
            }
            if (codeValue != null) {
                String codeSplit[] = codeValue.split(",");
                for (String temp : codeSplit) {
                    if (temp.equals("D1"))
                        SHOW_D1 = true;
                    else if (temp.equals("D2"))
                        SHOW_D2 = true;
                    else if (temp.equals("D3"))
                        SHOW_D3 = true;
                    else if (temp.equals("DA"))
                        SHOW_DA = true;
                    else if (temp.equals("DPRICE"))
                        SHOW_DISCOUNTED_PRICE = true;
                }
            }

            if (SHOW_VANLOAD_LABELS) {
                codeValue = null;

                sql = "select RField from " + DataMembers.tbl_HhtModuleMaster
                        + " where hhtCode= " + bmodel.QT(CODE_VANLOAD_LABELS) + " and ForSwitchSeller = 0";
                c = db.selectSQL(sql);
                if (c != null && c.getCount() != 0) {
                    if (c.moveToNext()) {
                        codeValue = c.getString(0);
                    }
                    c.close();
                }
                if (codeValue != null) {
                    String codeSplit[] = codeValue.split(",");
                    for (String temp : codeSplit) {
                        if (temp.equals("OC"))
                            SHOW_VANLOAD_OC = true;
                        else if (temp.equals("OO"))
                            SHOW_VANLOAD_OO = true;
                        else if (temp.equals("OP"))
                            SHOW_VANLOAD_OP = true;
                    }
                }
            }
            codeValue = null;
            //dashboard
            sql = "select RField from " + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode='DASH01' and ForSwitchSeller = 0";
            c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    codeValue = c.getString(0);
                }
                c.close();
            }
            if (codeValue != null) {
                String codeSplit[] = codeValue.split(",");
                for (String temp : codeSplit) {
                    if (temp.equals("IDX"))
                        SHOW_INDEX_DASH = true;
                    else if (temp.equals("TGT"))
                        SHOW_TARGET_DASH = true;
                    else if (temp.equals("ACH"))
                        SHOW_ACHIEVED_DASH = true;
                    else if (temp.equals("FLEX1"))
                        SHOW_FLEX_DASH = true;
                    else if (temp.equals("BAL"))
                        SHOW_BALANCE_DASH = true;
                    else if (temp.equals("INC"))
                        SHOW_INCENTIVE_DASH = true;
                    else if (temp.equals("SCR"))
                        SHOW_SCORE_DASH = true;
                    else if (temp.equals("P3M"))
                        SHOW_P3M_DASH = true;
                    else if (temp.equals("YAI"))
                        SHOW_YAI_DASH = true;
                }
            }

            codeValue = null;

            //stock proposal
            sql = "select RField from " + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode='STKPRO4' and ForSwitchSeller = 0";
            c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    codeValue = c.getString(0);
                }
            }
            if (codeValue != null) {
                String codeSplit[] = codeValue.split(",");
                for (String temp : codeSplit) {
                    if (temp.equals("SO"))
                        SHOW_STOCK_SO = true;
                    else if (temp.equals("DINV"))
                        STOCK_DIST_INV = true;

                }
            }

            sql = "select RField from " + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode=" + bmodel.QT(CODE_STOCK_COMPETITOR) + " and Flag=1 and ForSwitchSeller = 0";

            c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {

                    LOAD_STOCK_COMPETITOR = c.getInt(0);
                    IS_LOAD_STOCK_COMPETITOR = true;
                }
            }

            sql = "select RField from " + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode=" + bmodel.QT(CODE_DELIVERY_DATE) + " and Flag=1 and ForSwitchSeller = 0";
            codeValue = "";
            c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    codeValue = c.getString(0);
                }
            }
            if (codeValue != null && !codeValue.equals("")) {
                String codeSplit[] = codeValue.split(",");
                if (codeSplit.length == 3) {
                    if (codeSplit[0] != null && !codeSplit[0].equals(""))
                        DEFAULT_NUMBER_OF_DAYS_TO_DELIVER_ORDER = SDUtil.convertToInt(codeSplit[0]);
                    if (codeSplit[1] != null && !codeSplit[1].equals(""))
                        MIN_NUMBER_OF_DAYS_ALLOWED_TO_DELIVER = SDUtil.convertToInt(codeSplit[1]);
                    if (codeSplit[2] != null && !codeSplit[2].equals(""))
                        MAX_NUMBER_OF_DAYS_ALLOWED_TO_DELIVER = SDUtil.convertToInt(codeSplit[2]);
                    if (DEFAULT_NUMBER_OF_DAYS_TO_DELIVER_ORDER < MIN_NUMBER_OF_DAYS_ALLOWED_TO_DELIVER) {
                        MIN_NUMBER_OF_DAYS_ALLOWED_TO_DELIVER = DEFAULT_NUMBER_OF_DAYS_TO_DELIVER_ORDER;
                    }
                    if (DEFAULT_NUMBER_OF_DAYS_TO_DELIVER_ORDER > MAX_NUMBER_OF_DAYS_ALLOWED_TO_DELIVER) {
                        MAX_NUMBER_OF_DAYS_ALLOWED_TO_DELIVER = DEFAULT_NUMBER_OF_DAYS_TO_DELIVER_ORDER;
                    }
                } else {
                    DEFAULT_NUMBER_OF_DAYS_TO_DELIVER_ORDER = 1;
                    MIN_NUMBER_OF_DAYS_ALLOWED_TO_DELIVER = 1;
                    MAX_NUMBER_OF_DAYS_ALLOWED_TO_DELIVER = 1;
                }
            }

            sql = "select RField from " + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode=" + bmodel.QT(CODE_SHOW_REMARKS_STK_ORD) + " and Flag=1 and ForSwitchSeller = 0";

            c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {

                    LOAD_REMARKS_FIELD_STRING = c.getString(0);
                }
            }

            sql = "select RField from " + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode=" + bmodel.QT(CODE_SHOW_REVIEW_PO) + " and Flag=1 and ForSwitchSeller = 0";

            c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {

                    LOAD_ORDER_SUMMARY_REMARKS_FIELD_STRING = c.getString(0);
                }
            }

            sql = "select RField from " + DataMembers.tbl_HhtModuleMaster +
                    " where hhtcode=" + bmodel.QT(SHOW_TAX_INVOICE) + " and Flag=1 and ForSwitchSeller = 0";
            c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    int value = c.getInt(0);
                    if (value == 1) {
                        SHOW_INCLUDE_BILL_TAX = true;
                    }
                }
                c.close();
            }

            sql = "select RField from " + DataMembers.tbl_HhtModuleMaster +
                    " where hhtcode=" + bmodel.QT(CODE_SHOW_PRODUCT_RETRUN) + " and Flag=1 and ForSwitchSeller = 0";
            c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    int value = c.getInt(0);
                    if (value == 1) {
                        CHECK_LIABLE_PRODUCTS = true;

                    }
                }
                c.close();
            }

            sql = "select RField from " + DataMembers.tbl_HhtModuleMaster +
                    " where hhtcode=" + bmodel.QT(CODE_SHOW_TAX_DISCOUNT_IN_REPORT) + " and Flag=1 and ForSwitchSeller = 0";
            c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    codeValue = c.getString(0);
                }
                c.close();
            }
            if (codeValue != null) {
                String codeSplit[] = codeValue.split(",");
                for (String temp : codeSplit) {
                    if (temp.equals("SCHDISC"))
                        IS_SHOW_DISCOUNT_IN_REPORT = true;
                    else if (temp.equals("TAX"))
                        IS_SHOW_TAX_IN_REPORT = true;

                }
            }
            sql = "select RField from " + DataMembers.tbl_HhtModuleMaster +
                    " where hhtcode=" + bmodel.QT(CODE_SHOW_NO_ORDER_REASON) + " and Flag=1 and ForSwitchSeller = 0";
            c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    int value = c.getInt(0);
                    if (value == 1) {
                        SHOW_NO_ORDER_CAPTURE_PHOTO = true;

                    } else if (value == 2) {
                        SHOW_NO_ORDER_EDITTEXT = true;
                    }
                }
                c.close();
            }

            //dashboard

            sql = "select RField from " + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode='DASH12' and ForSwitchSeller = 0";
            c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    codeValue = c.getString(0);
                }
                c.close();
            }
            if (codeValue != null) {
                String codeSplit[] = codeValue.split(",");
                for (String temp : codeSplit) {
                    if (temp.equals("P3M"))
                        SHOW_P3M_DASH = true;
                    else if (temp.equals("INV"))
                        SHOW_INV_DASH = true;
                    else if (temp.equals("SMP"))
                        SHOW_SMP_DASH = true;
                    else if (temp.equals("BAR"))
                        SHOW_KPIBARCHART_DASH = true;
                }
            }

            if (IS_GLOBAL_LOCATION) {
                sql = "select RField from " + DataMembers.tbl_HhtModuleMaster +
                        " where hhtcode=" + bmodel.QT(CODE_GLOBAL_LOCATION) + " and Flag=1 and ForSwitchSeller = 0";
                c = db.selectSQL(sql);
                if (c != null && c.getCount() != 0) {
                    if (c.moveToNext()) {
                        int value = c.getInt(0);
                        if (value == 1) {
                            SHOW_DEFAULT_LOCATION_POPUP = true;
                        }
                    }
                    c.close();
                }
            }

            // new outlet modules
            sql = "select RField from " + DataMembers.tbl_HhtModuleMaster +
                    " where hhtcode=" + bmodel.QT(CODE_NEWOUTLET_MODULES) + " and Flag=1 and ForSwitchSeller = 0";

            c = db.selectSQL(sql);

            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    codeValue = c.getString(0);
                }
            }
            c.close();
            if (codeValue != null) {
                String codeSplit[] = codeValue.split(",");
                for (String temp : codeSplit) {
                    if (temp.equals("ORD"))
                        SHOW_NEW_OUTLET_ORDER = true;
                    else if (temp.equals("OPR"))
                        SHOW_NEW_OUTLET_OPPR = true;

                }
            }


            //STK_ORD row configuration
            sql = "select RField from " + DataMembers.tbl_HhtModuleMaster +
                    " where hhtcode=" + bmodel.QT(CODE_STK_ORD_ROW) + " and Flag=1 and ForSwitchSeller = 0";

            c = db.selectSQL(sql);

            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    codeValue = c.getString(0);
                }
                c.close();
            }

            if (codeValue != null) {
                if (codeValue.equalsIgnoreCase(CODE_STK_ORD_ROW_BS))
                    IS_STK_ORD_BS = true;
                else if (codeValue.equalsIgnoreCase(CODE_STK_ORD_ROW_PROJECT))
                    IS_STK_ORD_PROJECT = true;

            }

            //RField Check to get Credit Limit value from Supplier Master
            codeValue = null;
            sql = "select RField from " + DataMembers.tbl_HhtModuleMaster +
                    " where hhtcode=" + bmodel.QT(CODE_INV_CREDIT_BALANCE) + " and Flag=1 and ForSwitchSeller = 0";
            c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    int value = c.getInt(0);
                    if (value == 0 || value == 2) {
                        IS_SUPPLIER_CREDIT_LIMIT = false;
                        if (value == 2)
                            IS_CREDIT_LIMIT_WITH_SOFT_ALERT = true;
                    } else if (value == 1 || value == 3) {
                        IS_SUPPLIER_CREDIT_LIMIT = true;
                        if (value == 3)
                            IS_CREDIT_LIMIT_WITH_SOFT_ALERT = true;
                    }
                }
                c.close();
            }


            sql = "select RField from " + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode=" + bmodel.QT(CODE_STORE_WISE_DISCOUNT_DIALOG) + " and Flag=1 and ForSwitchSeller = 0";
            c = db.selectSQL(sql);
            if (c.getCount() > 0) {
                if (c.moveToFirst()) {
                    BILL_WISE_DISCOUNT = c.getInt(0);
                }
            }
            c.close();


            sql = "select RField from " + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode=" + bmodel.QT(CODE_PRINT_SEQUENCE) + " and Flag=1";

            c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    IS_PRINT_SEQUENCE_REQUIRED = true;
                    if (c.getInt(0) != 0) {
                        IS_PRINT_SEQUENCE_LEVELWISE = true;
                        bmodel.setPrintSequenceLevelID(c.getInt(0));
                    }
                }
                c.close();
            }

            sql = "select listid from " + DataMembers.tbl_StandardListMaster
                    + " where ListCode='WHT' and ListType='DISCOUNT_TYPE'";

            c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    IS_WITHHOLD_DISCOUNT = true;
                }
                c.close();
            }

            // default uom config
            sql = "select RField from " + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode=" + bmodel.QT(CODE_SHOW_DEFAULT_UOM) + " and Flag=1";

            c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    IS_SHOW_DEFAULT_UOM = true;
                }
                c.close();
            }

            //this config used in stock check and sales return screen
            sql = "select RField from " + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode=" + bmodel.QT(CODE_SALABLE_AND_NON_SALABLE_SKU) + " and Flag=1";

            c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    SHOW_SALABLE_AND_NON_SALABLE_SKU = true;
                }
                c.close();
            }


            sql = "select RField from " + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode=" + bmodel.QT(CODE_SHOW_ORDER_PHOTO_CAPTURE) + " and Flag=1";

            c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    int rField = c.getInt(c.getColumnIndex("RField"));
                    if (rField == 0) {
                        IS_SHOW_ORDER_PHOTO_CAPTURE = true;
                        IS_SHOW_ORDER_ATTACH_FILE = false;
                    } else if (rField == 1) {
                        IS_SHOW_ORDER_PHOTO_CAPTURE = false;
                        IS_SHOW_ORDER_ATTACH_FILE = true;
                    }
                }
                c.close();
            }


            //Order Digit config
            sql = "select RField from " + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode=" + bmodel.QT(CODE_ORD_DIGIT) + " and Flag=1 and  ForSwitchSeller = 0 ";
            c = db.selectSQL(sql);
            if (c != null && c.getCount() > 0) {
                if (c.moveToFirst()) {
                    ORD_DIGIT = (c.getInt(0) <= 5) ? 5 : c.getInt(0);
                    IS_ORD_DIGIT = true;
                }
                c.close();
            }

            sql = "select RField from " + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode=" + bmodel.QT(CODE_STK_DIGIT) + " and Flag=1 and  ForSwitchSeller = 0 ";
            c = db.selectSQL(sql);
            if (c != null && c.getCount() > 0) {
                if (c.moveToFirst()) {
                    STK_DIGIT = (c.getInt(0) <= 5) ? 5 : c.getInt(0);
                    IS_STK_DIGIT = true;
                }
                c.close();
            }


            //Seller KPI Dashboard
            sql = "select RField from " + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode=" + bmodel.QT(CODE_SWITCH_WITH_OUT_TGT_SELLER_DASHBOARD) + " and  ForSwitchSeller = 0 ";
            c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()
                        && c.getString(0).length() > 0) {
                    IS_SWITCH_WITH_OUT_TGT = true;
                    SELLER_KPI_CODES = c.getString(0);
                }
                c.close();
            }


            sql = "select RField from " + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode=" + bmodel.QT(CODE_SWITCH_WITH_OUT_TGT_SKU_WISE_DASHBOARD) + " and  ForSwitchSeller = 0 ";
            c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()
                        && c.getString(0).length() > 0) {
                    IS_SWITCH_WITH_OUT_SKU_WISE_TGT = true;
                    SELLER_SKU_WISE_KPI_CODES = c.getString(0);
                }
                c.close();
            }

            sql = "select RField from " + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode=" + bmodel.QT(CODE_SHOW_PSQ) + " and  ForSwitchSeller = 0 ";
            c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    int value = c.getInt(0);
                    if (value == 1) {
                        SHOW_LASTVISIT_GRAPH = true;
                    }
                }
                c.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public void loadEODColumnConfiguration() {
        String CODE_STOCK_REPLACE_PCS = "RPPC";
        String CODE_STOCK_REPLACE_CASE = "RPOC";
        String CODE_STOCK_REPLACE_OUTER = "RPOO";
        String CODE_STOCK_EMPTY = "EMP";
        String CODE_STOCK_FREE_ISSUED = "FI";
        String CODE_STOCK_RETURN = "RET";
        String CODE_STOCK_NON_SALABLE = "NS";
        String CODE_STOCK_VAN_UNLOAD = "UL";
        String CODE_STOCK_FREE_LOADED = "FSIHL";
        String CODE_STOCK_FREE_SIH = "FSIH";
        SHOW_STOCK_NON_SALABLE = false;
        SHOW_STOCK_VAN_UNLOAD = false;
        SHOW_STOCK_REPLACE = false;
        SHOW_STOCK_RETURN = false;
        SHOW_STOCK_EMPTY = false;
        SHOW_STOCK_FREE_ISSUED = false;
        SHOW_STOCK_NON_SALABLE = false;
        SHOW_STOCK_VAN_UNLOAD = false;
        SHOW_FREE_STOCK_LOADED = false;
        SHOW_FREE_STOCK_IN_HAND = false;

        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        try {
            db.openDataBase();
            if (IS_EOD_COLUMNS_AVALIABLE) {
                String codeValue = null;


                String sql = "select RField from " + DataMembers.tbl_HhtModuleMaster
                        + " where hhtCode=" + bmodel.QT(CODE_EOD_COLUMNS) + " and ForSwitchSeller = 0";
                Cursor c = db.selectSQL(sql);
                if (c != null && c.getCount() != 0) {
                    if (c.moveToNext()) {
                        codeValue = c.getString(0);
                    }
                    c.close();
                }
                if (codeValue != null) {
                    String codeSplit[] = codeValue.split(",");
                    for (String temp : codeSplit) {
                        if (temp.equals(CODE_STOCK_REPLACE_CASE) ||
                                temp.equals(CODE_STOCK_REPLACE_OUTER) ||
                                temp.equals(CODE_STOCK_REPLACE_PCS))
                            SHOW_STOCK_REPLACE = true;
                        else if (temp.equals(CODE_STOCK_EMPTY))
                            SHOW_STOCK_EMPTY = true;
                        else if (temp.equals(CODE_STOCK_FREE_ISSUED))
                            SHOW_STOCK_FREE_ISSUED = true;
                        else if (temp.equals(CODE_STOCK_RETURN))
                            SHOW_STOCK_RETURN = true;
                        else if (temp.equals(CODE_STOCK_NON_SALABLE))
                            SHOW_STOCK_NON_SALABLE = true;
                        else if (temp.equals(CODE_STOCK_VAN_UNLOAD))
                            SHOW_STOCK_VAN_UNLOAD = true;
                        else if (temp.equals(CODE_STOCK_FREE_LOADED))
                            SHOW_FREE_STOCK_LOADED = true;
                        else if (temp.equals(CODE_STOCK_FREE_SIH))
                            SHOW_FREE_STOCK_IN_HAND = true;
                    }
                }

            }

            SHOW_EOD_OC = false;
            SHOW_EOD_OO = false;
            SHOW_EOD_OP = false;

            String codeValue = null;

            String sql = "select RField from "
                    + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode=" + bmodel.QT(CODE_EOD_STOCK_SPLIT) + " and Flag=1 ";
            Cursor c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    codeValue = c.getString(0);
                }
            }

            if (codeValue != null) {
                String codeSplit[] = codeValue.split(",");
                for (String temp : codeSplit) {
                    switch (temp) {
                        case "OP":
                            SHOW_EOD_OP = true;
                            break;
                        case "OO":
                            SHOW_EOD_OO = true;
                            break;
                        case "OC":
                            SHOW_EOD_OC = true;
                            break;
                    }

                }
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);

        }


    }

    public void loadOrderSummaryDetailConfig() {
        SHOW_ORD_SUMMARY_PRICEOFF = false;
        SHOW_ORD_SUMMARY_DISC1 = false;
        SHOW_ORD_SUMMARY_DISC2 = false;
        SHOW_ORD_SUMMARY_DISC3 = false;
        SHOW_ORD_SUMMARY_DISC4 = false;
        SHOW_ORD_SUMMARY_DISC5 = false;
        SHOW_ORD_SUMMARY_CASH_DISCOUNT = false;
        SHOW_ORD_SUMMARY_TAX = false;

        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        db.openDataBase();
        String sql = "select RField from " + DataMembers.tbl_HhtModuleMaster
                + " where hhtCode='ORDB06' and Flag=1 and ForSwitchSeller = 0";
        String codeValue = "";
        Cursor c = db.selectSQL(sql);
        if (c != null && c.getCount() != 0) {
            if (c.moveToNext()) {
                codeValue = c.getString(0);
            }
        }
        if (codeValue != null && !codeValue.equals("")) {
            String codeSplit[] = codeValue.split(",");
            for (String temp : codeSplit) {
                if (temp.equals("PROFF"))
                    SHOW_ORD_SUMMARY_PRICEOFF = true;
                else if (temp.equals("D1"))
                    SHOW_ORD_SUMMARY_DISC1 = true;
                else if (temp.equals("D2"))
                    SHOW_ORD_SUMMARY_DISC2 = true;
                else if (temp.equals("D3"))
                    SHOW_ORD_SUMMARY_DISC3 = true;
                else if (temp.equals("D4"))
                    SHOW_ORD_SUMMARY_DISC4 = true;
                else if (temp.equals("D5"))
                    SHOW_ORD_SUMMARY_DISC5 = true;
                else if (temp.equals("CD"))
                    SHOW_ORD_SUMMARY_CASH_DISCOUNT = true;
                else if (temp.equals("TAX"))
                    SHOW_ORD_SUMMARY_TAX = true;


            }
        }

        c.close();
        db.closeDB();


    }

    public Vector<ConfigureBO> getConfig() {
        return config;
    }

    public Vector<ConfigureBO> downloadFilterList() {
        setGenFilter(new Vector<ConfigureBO>());

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String language = sharedPrefs.getString("languagePref",
                ApplicationConfigs.LANGUAGE);

        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();

            String sql = "select hhtCode,MName,MNumber,hasLink,RField1 from "
                    + DataMembers.tbl_HhtMenuMaster
                    + " where  flag=1 and lower(MenuType)="
                    + bmodel.QT("FILTER").toLowerCase()
                    + " and lang=" + bmodel.QT(language) + " order by RField";

            Cursor c = db.selectSQL(sql);

            ConfigureBO con;
            if (c != null) {
                while (c.moveToNext()) {
                    con = new ConfigureBO();
                    con.setConfigCode(c.getString(0));

                    con.setMenuName(c.getString(1));
                    con.setMenuNumber(c.getString(2));
                    con.setHasLink(c.getInt(3));
                    con.setMandatory(c.getInt(4));
                    getGenFilter().add(con);

                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return getGenFilter();
    }



    public int getSbdDistTargetPCent() {
        int targetPercent = 0;
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        db.openDataBase();
        String sql = "select MNumber from HhtMenuMaster where hhtCode='CallA13' or hhtCode='CallA14' and Flag=1";
        Cursor c = db.selectSQL(sql);

        if (c != null) {
            if (c.moveToNext()) {
                targetPercent = c.getInt(0);
                c.close();
                db.closeDB();
                return targetPercent;
            }
            c.close();
        }
        db.closeDB();
        return targetPercent;
    }

    public int getSbdMerchTargetPCent() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        db.openDataBase();
        String sql = "select MNumber from HhtMenuMaster where hhtCode='CallA6' and Flag=1";
        Cursor c = db.selectSQL(sql);

        if (c != null) {
            if (c.moveToNext()) {
                return c.getInt(0);
            }
            c.close();
        }
        db.closeDB();
        return 0;
    }

    public int getSOLogic() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        db.openDataBase();
        String sql = "select Rfield from HhtModuleMaster where hhtCode="
                + bmodel.QT(CODE_SUGGESTED_ORDER_LOGIC) + " and  ForSwitchSeller = 0";

        Cursor c = db.selectSQL(sql);

        if (c != null) {
            if (c.moveToNext()) {
                db.closeDB();
                return c.getInt(0);
            }
            c.close();
        }
        db.closeDB();
        return 0;
    }

    public ArrayList<ConfigureBO> getLanguageList() {

        ArrayList<ConfigureBO> lanList = new ArrayList<>();
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();

            String sql = "select * from standardListMaster where ListType='LANGUAGE_TYPE'";

            Cursor c = db.selectSQL(sql);

            ConfigureBO con;
            if (c != null) {
                while (c.moveToNext()) {
                    con = new ConfigureBO();
                    con.setConfigCode(c.getString(c.getColumnIndex("ListCode")));
                    con.setMenuName(c.getString(c.getColumnIndex("ListName")));
                    lanList.add(con);

                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return lanList;

    }

    /**
     * Get the menu name from menucode
     *
     * @param menucode
     * @return menuname
     */
    public String getHomescreentwomenutitle(String menucode) {
        String menuName = "";
        Vector<ConfigureBO> config = getActivityMenu();
        if (config != null) {
            for (int i = 0; i < config.size(); i++) {
                if (config.get(i).getConfigCode().equals(menucode))
                    menuName = config.get(i).getMenuName();
            }
        }
        return menuName;

    }

    public Vector<ConfigureBO> getGenFilter() {
        return genFilter;
    }

    public void setGenFilter(Vector<ConfigureBO> genFilter) {
        this.genFilter = genFilter;
    }



    public String getLoadmanagementtitle() {
        return loadmanagementtitle;
    }

    public void setLoadmanagementtitle(String loadmanagementtitle) {
        this.loadmanagementtitle = loadmanagementtitle;
    }

    public String getTradecoveragetitle() {
        return tradecoveragetitle;
    }

    public void setTradecoveragetitle(String tradecoveragetitle) {
        this.tradecoveragetitle = tradecoveragetitle;
    }

    public String getExpansetitle() {
        return expansetitle;
    }

    public void setExpansetitle(String expansetitle) {
        this.expansetitle = expansetitle;
    }


    public String getSubdtitle() {
        return subdtitle;
    }

    public void setSubdtitle(String subdtitle) {
        this.subdtitle = subdtitle;
    }

    public String getBatchAllocationtitle() {
        return batchAllocationtitle;
    }

    public void setBatchAllocationtitle(String batchAllocationtitle) {
        this.batchAllocationtitle = batchAllocationtitle;
    }

    public String getSignatureTitle() {
        return signatureTitle;
    }

    public void setSignatureTitle(String signatureTitle) {
        this.signatureTitle = signatureTitle;
    }


    /**
     * Get the Date format from HHTModuleMaster
     */
    private void getDateFormat() {

        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            String sql = "SELECT RField FROM HhtModuleMaster where hhtCode = '"
                    + CODE_DATE_FORMAT + "' and flag='1' and  ForSwitchSeller = 0";
            Cursor c = db.selectSQL(sql);
            if (c != null) {
                while (c.moveToNext()) {
                    ConfigurationMasterHelper.outDateFormat = c.getString(0);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }

    }

    public ArrayList<ConfigureBO> getPrinterList() {
        ArrayList<ConfigureBO> printerList = new ArrayList<>();
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();

            String sql = "select * from standardListMaster where ListType='PRINTER_TYPE'";

            Cursor c = db.selectSQL(sql);

            ConfigureBO con;
            if (c != null) {
                while (c.moveToNext()) {
                    con = new ConfigureBO();
                    con.setConfigCode(c.getString(c.getColumnIndex("ListCode")));
                    con.setMenuName(c.getString(c.getColumnIndex("ListName")));
                    printerList.add(con);

                }
                c.close();
            }
            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return printerList;

    }

    /**
     * If the the printer is already selected by user then load the printer
     * selection from the shared preference. Also load the print size.
     * <p>
     * By default, Bexilon II will be selected if nothing is selected by user.
     */
    public void getPrinterConfig() {
        try {
            sharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(context);

            String printer = sharedPrefs.getString("PrinterPref", null);
            try {
                ArrayList<ConfigureBO> lst = bmodel.configurationMasterHelper
                        .getPrinterList();

                if (printer == null) {
                    if (lst != null && lst.size() > 0)
                        printer = sharedPrefs.getString("PrinterPref",
                                lst.get(0).getConfigCode());
                    else
                        printer = sharedPrefs.getString("PrinterPref", "PRINT101");
                }
            } catch (Exception e) {
                printer = sharedPrefs.getString("PrinterPref", "PRINT101");
            }

            SHOW_BIXOLONI = false;
            SHOW_BIXOLONII = false;
            SHOW_ZEBRA = false;
            SHOW_ZEBRA_ATS = false;
            SHOW_INTERMEC_ATS = false;
            SHOW_ZEBRA_DIAGEO = false;
            SHOW_ZEBRA_GHANA = false;
            SHOW_ZEBRA_UNIPAL = false;
            SHOW_ZEBRA_TITAN = false;
            COMMON_PRINT_ZEBRA = false;
            COMMON_PRINT_BIXOLON = false;
            COMMON_PRINT_SCRYBE = false;
            SHOW_BIXOLON_TITAN = false;
            SHOW_SCRIBE_TITAN = false;
            COMMON_PRINT_LOGON = false;
            COMMON_PRINT_MAESTROS = false;


            if (CODE_BIXOLONI.equals(printer))
                SHOW_BIXOLONI = true;
            else if (CODE_BIXOLONII.equals(printer))
                SHOW_BIXOLONII = true;
            else if (CODE_ZEBRA.equals(printer))
                SHOW_ZEBRA = true;
            else if (CODE_ZEBRA_ATS.equals(printer))
                SHOW_ZEBRA_ATS = true;
            else if (CODE_INTERMEC_ATS.equals(printer))
                SHOW_INTERMEC_ATS = true;
            else if (CODE_ZEBRA_DIAGEO.equals(printer))
                SHOW_ZEBRA_DIAGEO = true;
            else if (CODE_ZEBRA_GHANA.equals(printer))
                SHOW_ZEBRA_GHANA = true;
            else if (CODE_ZEBRA_UNIPAL.equals(printer))
                SHOW_ZEBRA_UNIPAL = true;
            else if (CODE_ZEBRA_TITAN.equals(printer))
                SHOW_ZEBRA_TITAN = true;
            else if (CODE_COMMON_PRINT_ZEBRA.equals(printer))
                COMMON_PRINT_ZEBRA = true;
            else if (CODE_COMMON_PRINT_BIXOLON.equals(printer))
                COMMON_PRINT_BIXOLON = true;
            else if (CODE_COMMON_PRINT_SCRYBE.equals(printer))
                COMMON_PRINT_SCRYBE = true;
            else if (CODE_COMMON_PRINT_LOGON.equals(printer))
                COMMON_PRINT_LOGON = true;
            else if (CODE_BIXOLON_TITAN.equals(printer))
                SHOW_BIXOLON_TITAN = true;
            else if (CODE_SCRIBE_TITAN.equals(printer))
                SHOW_SCRIBE_TITAN = true;
            else if (CODE_COMMON_PRINT_MAESTROS.equals(printer))
                COMMON_PRINT_MAESTROS = true;
            else if (CODE_COMMON_PRINT_INTERMEC.equals(printer))
                COMMON_PRINT_INTERMEC = true;


            String printersize = sharedPrefs.getString("PrinterSizePref", "2");
            PRINTER_SIZE = SDUtil.convertToInt(printersize);
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public Vector<String> downloadSIHAppliedById() {
        SIHApplyById = new Vector<>();
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db.selectSQL("select uid from StockApply ");

            if (c != null) {
                while (c.moveToNext()) {

                    SIHApplyById.add(c.getString(0));
                }
                c.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return SIHApplyById;

    }

    public boolean isOdaMeterOn() {
        boolean flag = false;
        try {
            SharedPreferences sharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(context);
            String language = sharedPrefs.getString("languagePref",
                    ApplicationConfigs.LANGUAGE);
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            String sql = "select hht.flag,o.isended,o.isstarted from HhtMenuMaster hht  " +
                    "left join Odameter o where hht.HHTCode='MENU_ODAMETER' and hht.lang= "
                    + bmodel.QT(language);
            Cursor c = db.selectSQL(sql);
            if (c != null) {
                while (c.moveToNext()) {
                    if (c.getInt(0) == 1)
                        flag = true;
                    bmodel.endjourneyclicked = c.getInt(1) == 1;
                    bmodel.startjourneyclicked = c.getInt(2) == 1;
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }

        return flag;

    }

    public Vector<String> getSIHApplyById() {
        return SIHApplyById;
    }

    public Vector<ConfigureBO> getSpecialFilterList(String code) {
        Vector<String> Filter = new Vector<>();
        Vector<ConfigureBO> filterlist = new Vector<>();
        ConfigureBO con;

        String codeValue = null;
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            String sql = "select RField from "
                    + DataMembers.tbl_HhtModuleMaster + " where hhtCode="
                    + bmodel.QT(code) + " and flag=1 and ForSwitchSeller = 0";
            Cursor c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    codeValue = c.getString(0);
                }
                c.close();
            }

            if (codeValue != null) {
                Filter.addAll(Arrays.asList(codeValue.split(",")));

            }
            Vector<ConfigureBO> genfilter = bmodel.configurationMasterHelper
                    .downloadFilterList();

            for (int i = 0; i < genfilter.size(); i++) {

                if (Filter.contains(genfilter.get(i).getConfigCode())) {
                    con = new ConfigureBO();
                    con.setConfigCode(genfilter.get(i).getConfigCode());
                    con.setMenuName(genfilter.get(i).getMenuName());
                    filterlist.add(con);

                }

            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }

        return filterlist;
    }

    public String getJoincallTitile() {

        String code = "";
        try {
            String language = sharedPrefs.getString("languagePref",
                    ApplicationConfigs.LANGUAGE);

            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            String sql = "select MName from " + DataMembers.tbl_HhtMenuMaster
                    + " where hhtCode=" + bmodel.QT("MENU_JOINT_CALL")
                    + " and flag=1 and lang=" + bmodel.QT(language);
            Cursor c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    code = c.getString(0);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return code;
    }

    public String getJointCallTitle() {
        return jointCallTitle;
    }

    public void setJointCallTitle(String jointCallTitle) {
        this.jointCallTitle = jointCallTitle;
    }

    public int getsbddistpostwihtouthistory() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        db.createDataBase();
        db.openDataBase();
        Cursor c;

        int acheived = 0;

        String stockSql, sql;

        stockSql = "";

        sql = " union select gname from SbdDistributionAchievedMaster where rid="
                + bmodel.getRetailerMasterBO().getRetailerID();
        if (bmodel.configurationMasterHelper.IS_INVOICE) {
            c = db.selectSQL("select count(distinct GrpName) from SbdDistributionMaster where ChannelId="
                    + bmodel.getRetailerMasterBO().getChannelID()
                    + " and GrpName in (select distinct A.GrpName  from SbdDistributionMaster A inner join InvoiceDetails B on A.productid=B.productid where B.retailerid="
                    + bmodel.getRetailerMasterBO().getRetailerID()
                    + sql
                    + stockSql + ")");
        } else {
            c = db.selectSQL("select count(distinct GrpName) from SbdDistributionMaster where ChannelId="
                    + bmodel.getRetailerMasterBO().getChannelID()
                    + " and GrpName in (select distinct A.GrpName  from SbdDistributionMaster A inner join OrderDetail B on A.productid=B.productid  where B.retailerid="
                    + bmodel.getRetailerMasterBO().getRetailerID()
                    + sql
                    + stockSql + ")");
        }
        if (c != null) {
            if (c.moveToNext()) {
                acheived = c.getInt(0);
            }
            c.close();
        }
        db.closeDB();
        return acheived;

    }

    public Vector<ConfigureBO> downloadProductDetailsList() {

        setProductDetails(new Vector<ConfigureBO>());

        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            String language = sharedPrefs.getString("languagePref",
                    ApplicationConfigs.LANGUAGE);

            String sql = "select hhtCode,MName,MNumber,hasLink from "
                    + DataMembers.tbl_HhtMenuMaster
                    + " where  flag=1 and lower(MenuType)="
                    + bmodel.QT("PRODUCT_DETAILS").toLowerCase()
                    + " and lang=" + bmodel.QT(language) + " order by RField";

            Cursor c = db.selectSQL(sql);

            ConfigureBO con;
            if (c != null) {
                while (c.moveToNext()) {
                    con = new ConfigureBO();
                    con.setConfigCode(c.getString(0));
                    con.setMenuName(c.getString(1));
                    con.setMenuNumber(c.getString(2));
                    con.setHasLink(c.getInt(3));
                    getProductDetails().add(con);

                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }

        return getProductDetails();

    }

    public Vector<ConfigureBO> getProductDetails() {
        return productdetails;
    }

    public void setProductDetails(Vector<ConfigureBO> productdetails) {
        this.productdetails = productdetails;
    }

    public void downloadRetailerProperty() {
        mRetailerProperty = new ArrayList<>();
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
        try {
            db.openDataBase();
            String sb = "select  hhtCode,ifnull(Rfield,'') from hhtmodulemaster where menu_type='RETAILER_PROPERTY'" +
                    " and flag=1 and  ForSwitchSeller = 0 order by Rfield LIMIT 4";
            Cursor c = db.selectSQL(sb);
            if (c.getCount() > 0) {
                while (c.moveToNext()) {

                    ConfigureBO configureBO = new ConfigureBO();
                    configureBO.setConfigCode(c.getString(0));
                    configureBO.setRField(c.getString(1));
                    mRetailerProperty.add(configureBO);

                    if (c.getString(0).equals("RTPRTY07"))
                        isRetailerBOMEnabled = true;

                    if (c.getString(0).equals("RTPRTY03"))
                        IS_HANGINGORDER = true;

                }
            }
            c.close();

        } catch (Exception e) {
            Commons.printException("" + e);

        } finally {
            db.closeDB();
        }

    }

    public ArrayList<ConfigureBO> getRetailerPropertyList() {
        if (mRetailerProperty != null) {
            return mRetailerProperty;
        }
        return new ArrayList<>();
    }


    public void loadRouteConfig(DBUtil db) {
        try {
            String sb = "select Rfield from HhtModuleMaster where flag=1 and hhtcode=" +
                    bmodel.QT(CODE_SHOW_ALL_ROUTE_FILTER) + " and  ForSwitchSeller = 0";
            Cursor c = db.selectSQL(sb);
            if (c.getCount() > 0) {
                if (c.moveToNext()) {
                    int value = c.getInt(0);
                    if (value == 2) {
                        SHOW_DATE_ROUTE = true;
                    } else if (value == 3) {
                        SHOW_BEAT_ROUTE = true;
                    } else if (value == 4) {
                        SHOW_DATE_PLAN_ROUTE = true;
                    } else {
                        SHOW_WEEK_ROUTE = true;
                    }
                }
            }
            c.close();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public void checkCollectionDocConfig() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME);

        try {
            db.openDataBase();
            String sb = "select Rfield from HhtModuleMaster where flag=1 and hhtcode=" +
                    bmodel.QT(CODE_DOC_REF) + " and  ForSwitchSeller = 0";
            Cursor c = db.selectSQL(sb);
            String codeValue = "";
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    codeValue = c.getString(0);
                }
            }
            if (codeValue != null && !codeValue.equals("")) {
                String codeSplit[] = codeValue.split(",");
                for (String temp : codeSplit) {
                    if (temp.equals("SIGN"))
                        IS_DOC_SIGN = true;
                    else if (temp.equals("REFNO"))
                        IS_DOC_REFNO = true;
                }
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }
    }

    public void checkSalesReturnValidateConfig() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME);

        try {
            db.openDataBase();
            String sb = "select Rfield from HhtModuleMaster where flag=1 and hhtcode=" +
                    bmodel.QT(CODE_SALES_RETURN_VALIDATE) + " and  ForSwitchSeller = 0";
            Cursor c = db.selectSQL(sb);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    int value = c.getInt(0);
                    IS_SALES_RETURN_VALIDATE = value == 1;
                }
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }
    }

    public void checkSalesReturnSignConfig() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME);

        try {
            db.openDataBase();
            String sb = "select Rfield from HhtModuleMaster where flag=1 and hhtcode=" +
                    bmodel.QT(CODE_SALES_RETURN_SIGN) + " and  ForSwitchSeller = 0";
            Cursor c = db.selectSQL(sb);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    int value = c.getInt(0);
                    IS_SALES_RETURN_SIGN = value == 1;
                }
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }
    }

    /**
     * Method to use download password policy
     */
    public void downloadPasswordPolicy() {
        IS_SAME_LOGIN = false;
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );

        try {
            db.openDataBase();
            PasswordPolicyBO con;

            String sql = "select Rulekey, value from "
                    + DataMembers.tbl_password_rules;

            passwordConfig = new Vector<>();

            Cursor c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {

                while (c.moveToNext()) {
                    con = new PasswordPolicyBO();
                    con.setRuleKey(c.getString(0));
                    con.setValue(c.getString(1));
                    passwordConfig.add(con);
                }

                setPasswordRules();
                c.close();
            }
            db.closeDB();

        } catch (Exception e) {
            db.closeDB();
            Commons.printException("Unable to load the Password Rules " + e);
        }
    }

    /**
     * Method to set configuration for password rules
     */
    private void setPasswordRules() {
        try {
            sharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(context);
            PasswordPolicyBO con;
            int siz = passwordConfig.size();
            if (siz == 0)
                return;

            for (int i = 0; i < siz; ++i) {
                con = passwordConfig.get(i);

                if (CODE_PSWD_MIN_LEN.equals(con.getRuleKey())
                        && con.getValue() != null)
                    this.PSWD_MIN_LEN = SDUtil.convertToInt(con.getValue());
                else if (CODE_PSWD_MAX_LEN.equals(con.getRuleKey())
                        && con.getValue() != null)
                    this.PSWD_MAX_LEN = SDUtil.convertToInt(con.getValue());
                else if (CODE_PSWD_EXPIRY.equals(con.getRuleKey())
                        && con.getValue() != null)
                    this.PSWD_EXPIRY = SDUtil.convertToInt(con.getValue());
                else if (CODE_SAME_LOGIN.equals(con.getRuleKey())) {
                    IS_SAME_LOGIN = SDUtil.convertToInt(con.getValue()) == 1;
                } else if (CODE_PSWD_CHARACTERS.equals(con.getRuleKey())) {
                    if (con.getValue() != null) {
                        String codeSplit[] = con.getValue().split(",");
                        for (String temp : codeSplit) {
                            if (temp.equals("C"))
                                IS_CHARACTER = true;
                            else if (temp.equals("N"))
                                IS_NUMERIC = true;
                            else if (temp.equals("S"))
                                IS_SPECIAL_CASE = true;
                            else if (temp.equals("U"))
                                IS_UPPER_CASE = true;
                            else if (temp.equals("L"))
                                IS_LOWER_CASE = true;

                        }
                    }
                }
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public int getMVPTheme() {
        // MVPTheme = R.style.MVPTheme_Blue;
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );

        try {
            db.createDataBase();
            db.openDataBase();

            String query = "select RField from HhtModuleMaster where hhtcode='THEME01' and flag=1 and  ForSwitchSeller = 0";
            Cursor c = db.selectSQL(query);
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    if (c.getString(0).equalsIgnoreCase("blue"))
                        MVPTheme = R.style.MVPTheme_Blue;
                    else if (c.getString(0).equalsIgnoreCase("red"))
                        MVPTheme = R.style.MVPTheme_Red;
                    else if (c.getString(0).equalsIgnoreCase("orange"))
                        MVPTheme = R.style.MVPTheme_Orange;
                    else if (c.getString(0).equalsIgnoreCase("green"))
                        MVPTheme = R.style.MVPTheme_Green;
                    else if (c.getString(0).equalsIgnoreCase("nblue"))
                        MVPTheme = R.style.MVPTheme_NBlue;

                }
            }

            db.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return MVPTheme;
    }

    public enum FontType {
        LIGHT, MEDIUM, THIN, REGULAR
    }

    private Typeface mFontBaloobhaiRegular;

    /**
     * @param mFontType
     * @return
     * @See {@link com.ivy.utils.FontUtils#getFontBalooHai(Context, FontUtils.FontType)}
     * @deprecated
     */
    public Typeface getFontBaloobhai(FontType mFontType) {
        if (mFontType == FontType.REGULAR) {
            if (mFontBaloobhaiRegular == null)
                mFontBaloobhaiRegular = Typeface.createFromAsset(context.getAssets(), "font/baloobhai_regular.ttf");

            return mFontBaloobhaiRegular;
        }

        return Typeface.createFromAsset(context.getAssets(), "font/baloobhai_regular.ttf");
    }

    private Typeface mFontRobotoLight;
    private Typeface mFontRobotoMedium;
    private Typeface mFontRobotoThin;

    /**
     * @param mFontType
     * @return
     * @See {@link FontUtils#getFontRoboto(Context, FontUtils.FontType)}
     * @deprecated
     */
    public Typeface getFontRoboto(FontType mFontType) {
        if (mFontType == FontType.LIGHT) {
            if (mFontRobotoLight == null)
                mFontRobotoLight = Typeface.createFromAsset(context.getAssets(), "font/Roboto-Light.ttf");
            return mFontRobotoLight;
        } else if (mFontType == FontType.MEDIUM) {
            if (mFontRobotoMedium == null)
                mFontRobotoMedium = Typeface.createFromAsset(context.getAssets(), "font/Roboto-Medium.ttf");
            return mFontRobotoMedium;
        } else if (mFontType == FontType.THIN) {
            if (mFontRobotoThin == null)
                mFontRobotoThin = Typeface.createFromAsset(context.getAssets(), "font/Roboto-Thin.ttf");
            return mFontRobotoThin;
        }

        return Typeface.createFromAsset(context.getAssets(), "font/Roboto-Medium.ttf");
    }

    @Deprecated
    //this method moved into FontUtils class
    public Typeface getProductNameFont() {
        return Typeface.createFromAsset(context.getAssets(), "font/Roboto-Medium.ttf");
    }


    public void downloadIndicativeOrderConfig() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );

        try {
            db.createDataBase();
            db.openDataBase();
            String query = "select RField from HhtModuleMaster where hhtcode=" +
                    bmodel.QT(CODE_SHOW_SELLER_DIALOG) + " and flag=1 and  ForSwitchSeller = 0";
            Cursor c = db.selectSQL(query);
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    if (c.getInt(0) == 1) {
                        IS_INDICATIVE_ORDER = false;
                    }
                }
            }
            c.close();

            String sb = "select RField from HhtModuleMaster where hhtcode=" +
                    bmodel.QT(CODE_INVOICE) + " and flag=1 and  ForSwitchSeller = 0";
            Cursor c1 = db.selectSQL(sb);
            if (c1.getCount() > 0) {
                while (c1.moveToNext()) {
                    if (c1.getInt(0) == 1) {
                        IS_DEFAULT_PRESALE = true;
                    }
                }
            }
            c1.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);

        } finally {
            db.closeDB();
        }
    }

    public boolean checkLocationConfiguration() {

        return checkLocationInNewRetailerAndProfile()
                || bmodel.configurationMasterHelper.IS_MAP
                || bmodel.configurationMasterHelper.SHOW_LOCATION_PASSWORD_DIALOG
                || bmodel.configurationMasterHelper.SHOW_VANGPS_VALIDATION
                || bmodel.configurationMasterHelper.SHOW_CAPTURED_LOCATION;

    }

    private boolean checkLocationInNewRetailerAndProfile() {
        boolean returnValue = false;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String language = sharedPrefs.getString("languagePref",
                ApplicationConfigs.LANGUAGE);
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();

            String sql = "select hhtCode, flag, RField1, hasLink from "
                    + DataMembers.tbl_HhtMenuMaster
                    + " where HHTCode = 'MENU_NEW_RETAILER' AND flag = 1 AND hasLink = 1 AND MenuType="
                    + bmodel.QT(MENU_HOME)
                    + " and lang="
                    + bmodel.QT(language);

            Cursor c = db.selectSQL(sql);

            if (c != null) {
                if (c.moveToNext()) {
                    Cursor c1 = db.selectSQL("SELECT HHTCode FROM HhtMenuMaster WHERE MenuType= 'MENU_NEW_RET'"
                            + " AND HHTCode = 'LATLONG' AND Flag = 1 AND lang=" + bmodel.QT(language));
                    if (c1 != null) {
                        if (c1.moveToNext()) {
                            returnValue = true;
                        }
                        c1.close();
                    }
                }
                c.close();
            }

            if (!returnValue) {
                if (bmodel.configurationMasterHelper.HAS_PROFILE_BUTTON_IN_RETAILER_LIST) {
                    Cursor c1 = db.selectSQL("SELECT HHTCode FROM HhtMenuMaster WHERE MenuType= 'RETAILER_PROFILE'"
                            + " AND HHTCode = 'PROFILE08' AND Flag = 1 AND lang=" + bmodel.QT(language));
                    if (c1 != null) {
                        if (c1.moveToNext()) {
                            returnValue = true;
                        }
                        c1.close();
                    }
                }
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return returnValue;
    }

    public boolean isDistributorWiseDownload() {

        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        try {
            db.openDataBase();
            Cursor c = db.selectSQL("select Flag from HhtModuleMaster where hhtcode='FUN35' and  ForSwitchSeller = 0");
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    int value = c.getInt(0);
                    if (value == 1) {
                        IS_DISTRIBUTOR_AVAILABLE = true;

                    }
                }
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }
        return IS_DISTRIBUTOR_AVAILABLE;

    }

    public boolean downloadConfigForLoadLastVisit() {
        boolean flag = false;
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        StringBuffer sb;
        try {
            db.openDataBase();
            sb = new StringBuffer();
            sb.append("select flag from hhtmodulemaster where hhtcode =");
            sb.append(bmodel.QT(CODE_PRICE_CHECK_RETAIN_LAST_VISIT_TRAN) + " and  ForSwitchSeller = 0");
            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                if (c.moveToNext()) {
                    int value = c.getInt(0);
                    if (value == 1) {
                        IS_PRICE_CHECK_RETAIN_LAST_VISIT_TRAN = true;
                        flag = true;
                    }
                }
            }
            sb = new StringBuffer();
            sb.append("select flag from hhtmodulemaster where hhtcode =");
            sb.append(bmodel.QT(CODE_NEAR_EXPIRY_RETAIN_LAST_VISIT_TRAN) + " and  ForSwitchSeller = 0");
            c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                if (c.moveToNext()) {
                    int value = c.getInt(0);
                    if (value == 1) {
                        IS_NEAR_EXPIRY_RETAIN_LAST_VISIT_TRAN = true;
                        flag = true;
                    }
                }
            }
            sb = new StringBuffer();
            sb.append("select flag from hhtmodulemaster where hhtcode =");
            sb.append(bmodel.QT(CODE_STOCK_CHECK_RETAIN_LAST_VISIT_TRAN) + " and  ForSwitchSeller = 0");
            c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                if (c.moveToNext()) {
                    int value = c.getInt(0);
                    if (value == 1) {
                        IS_STOCK_CHECK_RETAIN_LAST_VISIT_TRAN = true;
                        flag = true;
                    }
                }
            }
            sb = new StringBuffer();
            sb.append("select flag from hhtmodulemaster where hhtcode =");
            sb.append(bmodel.QT(CODE_PROMOTION_RETAIN_LAST_VISIT_TRAN) + " and  ForSwitchSeller = 0");
            c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                if (c.moveToNext()) {
                    int value = c.getInt(0);
                    if (value == 1) {
                        IS_PROMOTION_RETAIN_LAST_VISIT_TRAN = true;
                        flag = true;
                    }
                }
            }
            sb = new StringBuffer();
            sb.append("select flag from hhtmodulemaster where hhtcode =");
            sb.append(bmodel.QT(CODE_SURVEY_RETAIN_LAST_VISIT_TRAN) + " and  ForSwitchSeller = 0");
            c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                if (c.moveToNext()) {
                    int value = c.getInt(0);
                    if (value == 1) {
                        IS_SURVEY_RETAIN_LAST_VISIT_TRAN = true;
                        flag = true;
                    }
                }
            }
            sb = new StringBuffer();
            sb.append("select flag from hhtmodulemaster where hhtcode =");
            sb.append(bmodel.QT(CODE_SOS_RETAIN_LAST_VISIT_TRAN) + " and  ForSwitchSeller = 0");
            c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                if (c.moveToNext()) {
                    int value = c.getInt(0);
                    if (value == 1) {
                        IS_SOS_RETAIN_LAST_VISIT_TRAN = true;
                        flag = true;
                    }
                }
            }

            c.close();
            db.closeDB();

        } catch (Exception e) {
            db.closeDB();
            Commons.printException("" + e);
        }
        return flag;
    }

    public int getActivtyType(String code) {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        db.openDataBase();
        String query = "select listid from standardlistmaster where listtype='OTP_ACTIVITY_CMN_TYPE' and ListCode=" + bmodel.QT(code);
        Cursor c = db.selectSQL(query);
        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                int listid = c.getInt(0);
                c.close();
                db.closeDB();
                return listid;
            }
        }
        return 0;
    }

    public boolean isLastVisitTransactionDownloadConfigEnabled() {
        return bmodel.configurationMasterHelper.IS_STOCK_CHECK_RETAIN_LAST_VISIT_TRAN
                || bmodel.configurationMasterHelper.IS_NEAR_EXPIRY_RETAIN_LAST_VISIT_TRAN
                || bmodel.configurationMasterHelper.IS_PROMOTION_RETAIN_LAST_VISIT_TRAN
                || bmodel.configurationMasterHelper.IS_SURVEY_RETAIN_LAST_VISIT_TRAN
                || bmodel.configurationMasterHelper.IS_SOS_RETAIN_LAST_VISIT_TRAN
                || bmodel.configurationMasterHelper.IS_PRICE_CHECK_RETAIN_LAST_VISIT_TRAN;
    }

    public void loadOrderReasonDialog() {

        try {
            String sql = "select RField from " + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode=" + bmodel.QT(CODE_SHOW_ONLY_INDICATIVE_ORDER)
                    + " and flag=1 and ForSwitchSeller = 0";
            IS_SHOW_ORDER_REASON = false;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();

            Cursor c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {

                while (c.moveToNext()) {
                    if (c.getInt(0) == 1)
                        IS_SHOW_ORDER_REASON = true;
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("Unable to load the configurations " + e);
        }
    }

    public void loadInvoiceMasterDueDateAndDateConfig() {

        try {
            COMPUTE_DUE_DATE = true;
            COMPUTE_DUE_DAYS = true;
            String sql = "select RField from " + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode='SR01' and ForSwitchSeller = 0";
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();

            Cursor c = db.selectSQL(sql);
            String rFieldValue = "";
            if (c != null && c.getCount() != 0) {

                while (c.moveToNext()) {
                    rFieldValue = c.getString(0);
                }
                c.close();
            }
            db.closeDB();
            if (rFieldValue != null && rFieldValue.length() > 0 && rFieldValue.contains(",")) {
                String rFieldSplit[] = rFieldValue.split(",");
                for (String temp : rFieldSplit) {
                    if (temp.equals(CODE_COMPUTE_DUE_DAYS))
                        COMPUTE_DUE_DATE = false;
                    else if (temp.equals(CODE_COMPUTE_DUE_DATE))
                        COMPUTE_DUE_DAYS = false;
                }
            }
        } catch (Exception e) {
            Commons.printException("Unable to load the configurations " + e);
        }
    }


    /**
     * This method will return spl filter code set as default.
     *
     * @return
     */
    public String getDefaultFilter() {
        String defaultfilter = CatalogOrder.GENERAL;
        try {
            Vector<ConfigureBO> genfilter = bmodel.configurationMasterHelper
                    .getGenFilter();
            for (int i = 0; i < genfilter.size(); i++) {
                if (genfilter.get(i).getHasLink() == 1) {
                    if (!bmodel.configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED) {
                        defaultfilter = genfilter.get(i).getConfigCode();
                        break;
                    } else {
                        if (bmodel.getRetailerMasterBO().getIsVansales() == 1) {
                            if (genfilter.get(i).getConfigCode().equals("Filt13")) {
                                defaultfilter = genfilter.get(i).getConfigCode();
                                break;
                            } else if (!genfilter.get(i).getConfigCode().equals("Filt08")) {
                                defaultfilter = genfilter.get(i).getConfigCode();
                                break;
                            }
                        } else {
                            if (genfilter.get(i).getConfigCode().equals("Filt08")) {
                                defaultfilter = genfilter.get(i).getConfigCode();
                                break;
                            } else if (!genfilter.get(i).getConfigCode().equals("Filt13")) {
                                defaultfilter = genfilter.get(i).getConfigCode();
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Commons.printException(e + "");
        }
        return defaultfilter;
    }

    private void getUserLevel(String hhtCode) {


        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        try {
            db.openDataBase();
            String codeValue = null;
            StringBuilder userLevels = new StringBuilder();

            Cursor c = db.selectSQL("select RField from HhtModuleMaster where hhtCode='" + hhtCode + "' and  ForSwitchSeller = 0");
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    codeValue = c.getString(0);
                }
                c.close();
            }

            if (codeValue != null) {
                String codeSplit[] = codeValue.split(",");
                for (String temp : codeSplit) {
                    if (!userLevels.toString().equals(""))
                        userLevels.append(",");
                    userLevels.append(bmodel.QT(temp));
                }
                userLevel = userLevels.toString();
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }

    }

    /* This method is used in End of The stock and StockView to calculate piece based
    on the Outer and case size*/

    public void loadStockUOMConfiguration() {

        CONVERT_STOCK_SIH_PS = false;
        CONVERT_STOCK_SIH_CS = false;
        CONVERT_STOCK_SIH_OU = false;

        try {
            String codeValue = null;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            String sql = "select RField from "
                    + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode='SIHINUOM' and ForSwitchSeller = 0 ";
            Cursor c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    codeValue = c.getString(0);
                }
                c.close();
            }
            if (codeValue != null) {

                if (codeValue.equals("PS"))
                    CONVERT_STOCK_SIH_PS = true;
                else if (codeValue.equals("CS"))
                    CONVERT_STOCK_SIH_CS = true;
                else if (codeValue.equals("OU"))
                    CONVERT_STOCK_SIH_OU = true;


            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    /* This method is used in End of The stock and StockView to calculate piece based
       on the Outer and case size*/
    public void loadEODUOMConfiguration() {

        CONVERT_EOD_SIH_PS = false;
        CONVERT_EOD_SIH_CS = false;
        CONVERT_EOD_SIH_OU = false;

        try {
            String codeValue = null;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            String sql = "select RField from "
                    + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode='EODSIHINUOM' and ForSwitchSeller = 0 ";
            Cursor c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    codeValue = c.getString(0);
                }
                c.close();
            }
            if (codeValue != null) {

                if (codeValue.equals("PS"))
                    CONVERT_EOD_SIH_PS = true;
                else if (codeValue.equals("CS"))
                    CONVERT_EOD_SIH_CS = true;
                else if (codeValue.equals("OU"))
                    CONVERT_EOD_SIH_OU = true;

            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    /*Van Accept and Reject UOM Configuration*/

    public void loadVanStockUOMConfiguration() {

        SHOW_VAN_STK_PS = false;
        SHOW_VAN_STK_CS = false;
        SHOW_VAN_STK_OU = false;

        try {
            String codeValue = null;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            String sql = "select RField from "
                    + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode='VANSTKUOM' ";
            Cursor c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    codeValue = c.getString(0);
                }
                c.close();
            }
            if (codeValue != null) {
                String codeSplit[] = codeValue.split(",");
                for (String temp : codeSplit) {
                    if (temp.equals("PS"))
                        SHOW_VAN_STK_PS = true;
                    else if (temp.equals("CS"))
                        SHOW_VAN_STK_CS = true;
                    else if (temp.equals("OU"))
                        SHOW_VAN_STK_OU = true;
                }
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    /**
     * Method to use change some specify configuration flag depends on selected
     * seller type
     *
     * @param switchToPreSeller
     */
    public void updateConfigurationSelectedSellerType(boolean switchToPreSeller) {
        if (switchToPreSeller) {
            downloadSwitchConfig();
            this.IS_INDICATIVE_SR = true;
            this.SHOW_UPDATE_SIH = false;
            this.IS_CREDIT_NOTE_CREATION = false;

        } else {
            SchemeDetailsMasterHelper schemeDetailsMasterHelper = SchemeDetailsMasterHelper.getInstance(context);
            this.IS_SIH_VALIDATION = this.IS_SIH_VALIDATION_MASTER;
            this.IS_STOCK_IN_HAND = this.IS_STOCK_IN_HAND_MASTER;
            schemeDetailsMasterHelper.IS_SCHEME_ON = schemeDetailsMasterHelper.IS_SCHEME_ON_MASTER;
            schemeDetailsMasterHelper.IS_SCHEME_SHOW_SCREEN = schemeDetailsMasterHelper.IS_SCHEME_SHOW_SCREEN_MASTER;
            this.SHOW_TAX = this.SHOW_TAX_MASTER;
            this.IS_GST = this.IS_GST_MASTER;
            this.IS_GST_HSN = this.IS_GST_HSN_MASTER;
            this.SHOW_STORE_WISE_DISCOUNT_DLG = this.SHOW_STORE_WISE_DISCOUNT_DLG_MASTER;
            this.SHOW_TOTAL_DISCOUNT_EDITTEXT = this.SHOW_TOTAL_DISCOUNT_EDITTEXT_MASTER;
            this.IS_WSIH = this.IS_WSIH_MASTER;
            this.IS_INVOICE = this.IS_INVOICE_MASTER;

            this.IS_INDICATIVE_SR = this.IS_INDICATIVE_MASTER;
            this.SHOW_UPDATE_SIH = true;
            this.IS_CREDIT_NOTE_CREATION = true;

            this.IS_STOCK_AVAILABLE_PRODUCTS_ONLY = this.IS_STOCK_AVAILABLE_PRODUCTS_ONLY_MASTER;
        }

    }


    public String getDynamicReportTitle() {

        String sql = "select RField from " + DataMembers.tbl_HhtModuleMaster
                + " where hhtCode=" + bmodel.QT(ConfigurationMasterHelper.CODE_SHOW_SALES_VALUE_DR) + " and Flag=1 and ForSwitchSeller = 0";

        DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
        db.openDataBase();

        String title = "";

        Cursor c = db.selectSQL(sql);
        if (c != null && c.getCount() != 0) {
            if (c.moveToNext()) {
                title = c.getString(c.getColumnIndex("RField")).equalsIgnoreCase("") ? "Report" : c.getString(c.getColumnIndex("RField"));
            }
            c.close();
            db.closeDB();
        }

        return title;
    }

    public void getDigitalContentSize() {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
            db.openDataBase();

            Cursor c = db.selectSQL("select RField from " + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode=" + StringUtils.QT(ConfigurationMasterHelper.CODE_CHECK_DIGITAL_SIZE));
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    this.DIGITAL_CONTENT_SIZE = c.getLong(0);
                }
                c.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public boolean isAuditEnabled() {

        return IS_TEAMLEAD && IS_AUDIT_USER;
    }

    private boolean isShowLastVisitedBy() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
        db.openDataBase();

        String sql = "SELECT hhtCode, RField FROM "
                + DataMembers.tbl_HhtModuleMaster
                + " WHERE flag='1' AND hhtCode='RTRS33' and ForSwitchSeller = 0";
        Cursor c = db.selectSQL(sql);
        if (c != null && c.getCount() != 0) {
            while (c.moveToNext()) {
                if (c.getString(1).equalsIgnoreCase("1")) {
                    return true;
                }
            }
            c.close();
        }
        db.closeDB();


        return false;
    }

}