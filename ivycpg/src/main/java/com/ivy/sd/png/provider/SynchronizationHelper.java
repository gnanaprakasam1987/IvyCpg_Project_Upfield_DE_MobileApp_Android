package com.ivy.sd.png.provider;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Base64;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.StringInputStream;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.ivy.cpg.view.login.LoginHelper;
import com.ivy.lib.Utils;
import com.ivy.lib.existing.DBUtil;
import com.ivy.lib.rest.JSONFormatter;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.SyncRetailerBO;
import com.ivy.sd.png.bo.TeamLeadBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.bo.VanLoadMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.ApplicationConfigs;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.DownloadService;
import com.ivy.sd.png.model.MyHttpConnectionNew;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.sd.png.view.HomeScreenFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mindrot.BCrypt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;


public class
SynchronizationHelper {
    public static final String JSON_MASTER_KEY = "Master";
    public static final String JSON_FIELD_KEY = "Field";
    public static final String JSON_DATA_KEY = "Data";
    public static final String ERROR_CODE = "ErrorCode";
    public static final String JSON_KEY = "Tables";
    public static final String JSON_OBJECT = "JsonObject";
    public static final String JSON_OBJECT_TABLE_LIST = "JsonObject_TableList";
    public static final String VOLLEY_RESPONSE = "Response";
    public static final String SYNXC_STATUS = "Volley_Sync";
    public static final int VOLLEY_LOGIN = 1;
    public static final int VOLLEY_DOWNLOAD_INSERT = 2;
    public static final int URL_DOWNLOAD = 3;
    public static final int NEW_RETAILER_DOWNLOAD_INSERT = 4;
    public static final int USER_RETAILER_TRAN_DOWNLOAD_INSERT = 5;
    public static final int SIH_DOWNLOAD = 6;
    public static final int VANLOAD_DOWNLOAD = 7;
    public static final int RETAILER_DOWNLOAD_BY_LOCATION = 8;
    public static final int DATA_DOWNLOAD_BY_RETAILER = 9;
    public static final int RETAILER_DOWNLOAD_FINISH_UPDATE = 10;
    public static final int VOLLEY_SUCCESS_RESPONSE = 11;
    public static final int VOLLEY_FAILURE_RESPONSE = 12;
    public static final int VOLLEY_CUSTOMER_SEARCH = 13;
    public static final int VOLLEY_TL_ABSENTEES_RETAILER_DOWNLOAD = 14;
    public static final int DATA_DOWNLOAD_BY_DISTRIBUTOR = 15;
    public static final int DISTRIBUTOR_WISE_DOWNLOAD_FINISH_UPDATE = 16;
    public static final int DOWNLOAD_FINISH_UPDATE = 17;
    public static final int DISTRIBUTOR_WISE_DOWNLOAD_INSERT = 18;
    public static final int LAST_VISIT_TRAN_DOWNLOAD_INSERT = 19;

    public static final String AUTHENTICATION_SUCCESS_CODE = "0";
    public static final String UPDATE_TABLE_SUCCESS_CODE = "-1";


    public static final String USER_ID = "UserId";
    public static final String VERSION_CODE = "VersionCode";
    public static final String MOBILE_DATE_TIME = "MobileDateTime";
    public static final String MOBILE_UTC_DATE_TIME = "MobileUTCDateTime";
    public static final String REQUEST_MOBILE_DATE_TIME = "RequestDate";
    public boolean isDistributorDownloadDone;
    public boolean isLastVisitTranDownloadDone;
    public boolean isSihDownloadDone;

    public static final int DISTRIBUTOR_SELECTION_REQUEST_CODE = 51;
    public String dataMissedTable = "";
    public String passwordType;

    public enum FROM_SCREEN {
        LOGIN(0),
        SYNC(1),
        NEW_RETAILER(2),
        VISIT_SCREEN(3),
        LOAD_MANAGEMENT(4),
        RETAILER_SELECTION(5),
        COUNTER_SALES_SELECTION(6),
        TL_ALLOCATION(7);

        private int value;

        FROM_SCREEN(int value) {
            this.value = value;
        }

    }

    /**
     * This enum function is using  while download from Login and Sync screen
     * More data will be downloaded using distributore wise or retailer wise depends on configuration.
     * So we have defined enum value for different type of download
     */
    public enum NEXT_METHOD {
        DISTRIBUTOR_DOWNLOAD(1),
        LAST_VISIT_TRAN_DOWNLOAD(2),
        SIH_DOWNLOAD(3),
        DIGITAL_CONTENT_AVALILABLE(4), DEFAULT(5),
        NON_DISTRIBUTOR_DOWNLOAD(6);
        private int value;

        NEXT_METHOD(int value) {
            this.value = value;
        }
    }


    // Authentication token
    public static final String TOKEN_MISSINIG = "E21";
    public static final String EXPIRY_TOKEN_CODE = "E22";
    public static final String INVALID_TOKEN = "E23";
    private static final String TAG = "SynchronizationHelper";
    private static final String TAG_JSON_OBJ = "json_obj_req";
    public static final String CASE_TYPE = "CASE";
    public static final String PIECE_TYPE = "PIECE";
    public static final String OUTER_TYPE = "MSQ";
    private static final String SECURITY_HEADER = "SECURITY_TOKEN_KEY";
    public static final String SPF_PSWD_ENCRYPT_TYPE_MD5 = "MD5";

    public static final String REQUEST_INFO = "REQUEST_INFO";
    private static final String VALIDATE_DEVICE_ID = "ValidateDeviceId";
    private static final String UPDATE_DEVICE_ID = "UpdateDeviceId";
    public boolean isInternalActivation;

    private static SynchronizationHelper instance = null;

    // URL
    private static final String AUTOUPDATE_APPEND_URL = "/HHTVersionUpgrade/Masters?userinfo=";
    public static final String URLDOWNLOAD_MASTER_APPEND_URL = "/v2/UrldownloadMaster/Masters";
    public static final String UPDATE_FINISH_URL = "/IncrementalSync/Finish";
    public static final String INCREMENTAL_SYNC_INITIATE_URL = "/IncrementalSync/Initiate";


    private static final String DATA_NOT_AVAILABLE_ERROR = "E19";

    public static final int LAST_VISIT_TRAN_SPLIT_RETAILER_COUNT = 100;

    private Context context;
    private BusinessModel bmodel;
    private StringBuilder retailerIds;
    private RequestQueue mRequestQueue;
    private int mDownloadUrlCount;
    private ArrayList<String> mDownloadUrlList;
    private HashMap<String, Integer> mMandatoryByUrl;
    private ArrayList<String> mNewRetailerDownloadUrlList;

    private ArrayList<String> mUserRetailerTranDownloadUrlList;
    private HashMap<String, String> mErrorMessageByErrorCode;
    private String mSecurityKey = "";
    private HashMap<String, JSONObject> mJsonObjectResponseByTableName = new HashMap<String, JSONObject>();

    private ArrayList<RetailerMasterBO> mRetailerListByLocOrUserWise;

    private HashMap<String, URLListBO> mURLList;
    public HashMap<String, String> mTableList;

    public String getSecurityKey() {
        return mSecurityKey;
    }

    public int getmRetailerWiseIterateCount() {
        return retailerWiseUpdateIterateCount;
    }

    public void setmRetailerWiseIterateCount(int mRetailerWiseIterateCount) {
        this.retailerWiseUpdateIterateCount = mRetailerWiseIterateCount;
    }

    // object used for batchwise retailer download
    private int retailerWiseUpdateIterateCount = 0;

    public int getRetailerwiseTotalIterateCount() {
        return retailerwiseTotalIterateCount;
    }

    public void setRetailerwiseTotalIterateCount(int retailerwiseTotalIterateCount) {
        this.retailerwiseTotalIterateCount = retailerwiseTotalIterateCount;
    }

    private int retailerwiseTotalIterateCount = 0;

    /**
     * upload initialition starts
     */
    private int responceMessage;
    private Handler handler;


    protected SynchronizationHelper(Context context) {
        this.context = context;
        this.bmodel = (BusinessModel) context;
    }

    public static SynchronizationHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SynchronizationHelper(context);
        }
        return instance;
    }

    /**
     * This will return number of images left in mobile to upload by checking DB
     * data. This method will check the Photocapture table with upload flag as
     * 'N'
     *
     * @return imageCount
     */
    public int getImagesCount() {

        int imageCount = 0;
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select count(uid) from Photocapture where upload='N'");
            if (c != null) {
                if (c.moveToNext()) {
                    imageCount = c.getInt(0);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("Error ocured in getImageCountMaethod", e);
        }
        return imageCount;
    }


    /**
     * This will return number of images left in mobile SDCard.
     *
     * @return imageCount
     */
    public int countImageFiles() {
        int imageSize = 0;
        try {
            File f = new File(
                    context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                            + "/" + DataMembers.photoFolderName + "/");
            if (f.exists()) {
                File files[] = f.listFiles(new FilenameFilter() {
                    public boolean accept(File directory, String fileName) {

                        return fileName.endsWith(".jpg");
                    }
                });

                File printfiles[] = f.listFiles(new FilenameFilter() {
                    public boolean accept(File directory, String fileName) {

                        return fileName.startsWith("PF");
                    }
                });

                if (bmodel.configurationMasterHelper.IS_PRINT_FILE_SAVE)
                    imageSize = files.length + printfiles.length;
                else
                    imageSize = files.length;
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return imageSize;
    }

    /**
     * this will update status flag in DayClose Table
     *
     * @param status
     */
    public void closeDay(int status) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            db.deleteSQL(DataMembers.tbl_DayClose, null, true);
            db.insertSQL(DataMembers.tbl_DayClose,
                    DataMembers.tbl_DayClose_cols, status + "," + bmodel.QT(SDUtil.now(SDUtil.DATE_TIME)));

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    /**
     * This method is used to check if the Day is closed or not. Day will be
     * closed from sync Screen. By default false. If the day is closed then its
     * not possible to preform any operation.
     *
     * @return true is day closed or false
     */
    public boolean isDayClosed() {
        int i = 0;
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db.selectSQL("select  status  from DayClose");
            if (c != null) {
                if (c.moveToNext()) {
                    i = c.getInt(0);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }

        if (i == 1)
            return true;
        else
            return false;
    }

    /**
     * this method will read the sqlite DB from the applicaiton and past a copy
     * in application download folder. This file will be later used to restore.
     *
     * @return true - if saved sucessfully and false - save failed
     */
    public boolean backUpDB() {
        String currentDBPath = "data/com.ivy.sd.png.asean.view/databases/"
                + DataMembers.DB_NAME;
        File data = Environment.getDataDirectory();

        if (isExternalStorageAvailable()) {
            File folder;
            folder = new File(
                    context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                            + "/pandg/");
            if (!folder.exists()) {
                folder.mkdir();
            }

            String path = folder + "";

            File SDPath = new File(path);
            if (!SDPath.exists()) {
                SDPath.mkdir();
            }
            try {
                File currentDB = new File(data, currentDBPath);
                InputStream input = new FileInputStream(currentDB);
                byte dataa[] = new byte[input.available()];
                input.read(dataa);

                OutputStream out = new FileOutputStream(path + "/"
                        + DataMembers.DB_NAME);
                out.write(dataa);
                out.flush();
                out.close();
                input.close();
            } catch (Exception e) {
                Commons.printException("exception," + e + "");
                return false;
            }
            return true;
        } else {
            return false;
        }

    }

    /**
     * Delete SDCard Database backup.
     */
    public void deleteDBFromSD() {
        try {
            File backupDB = new File(
                    context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                            + "/pandg/" + DataMembers.DB_NAME);
            backupDB.delete();
        } catch (Exception e) {
            Commons.printException("" + e);
        }

    }

    /**
     * Used to check whether external storage is available or not. It will also
     * check the space. If storage has less than 5MB space then it will be
     * considered as no storage
     *
     * @return true - Available , false - not available
     */
    public boolean isExternalStorageAvailable() {

        StatFs stat = new StatFs(Environment.getExternalStorageDirectory()
                .getPath());
        double sdAvailSize = (double) stat.getAvailableBlocks()
                * (double) stat.getBlockSize();
        // One binary gigabyte equals 1,073,741,824 bytes.
        double mbAvailable = sdAvailSize / 1048576;
        Commons.print("AvailSize," + mbAvailable + "");

        String state = Environment.getExternalStorageState();
        boolean mExternalStorageAvailable;
        boolean mExternalStorageWriteable;

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

        if (mExternalStorageAvailable
                && mExternalStorageWriteable && mbAvailable > 5) {
            return true;
        } else {
            return false;
        }
    }

//    public boolean checkForNFilesInFolder(String folderPath, int n,
//                                          String fNameStarts) {
//
//		/*
//         * It returns true if the folder contains the n or more than n files
//		 * which starts name fnameStarts otherwiese returns false;
//		 */
//        if (n < 1)
//            return true;
//
//        File folder = new File(folderPath);
//        if (!folder.exists()) {
//            return false;
//        } else {
//            String fnames[] = folder.list();
//            if ((fnames == null) || (fnames.length < n)) {
//                return false;
//            } else {
//                int count = 0;
//
//                for (String str : fnames) {
//
//                    if ((str != null) && (str.length() > 0)) {
//                        if (str.startsWith(fNameStarts)) {
//                            count++;
//                        }
//                    }
//
//                    if (count == n) {
//                        return true;
//                    }
//                }
//            }
//
//        }
//        return false;
//    }

    public int getImageCountFromPath(String folderPath,
                                     String fNameStarts) {
        int count = 0;
        File folder = new File(folderPath);
        String fnames[] = folder.list();
        if (fnames == null) {
            return count;
        } else {
            for (String str : fnames) {

                if ((str != null) && (str.length() > 0)) {
                    if (str.startsWith(fNameStarts)) {
                        count++;
                    }
                }

            }
            return count;
        }
    }

    /**
     * This will delete all the records from OrderHeader and OrderDetail.
     */
    public void deleteOrderHistory() {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            db.executeQ("delete from OrderHeader");
            db.executeQ("delete from OrderDetail");
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    private void updateOrderStatus() {
        //Update RetailerMaster set isVisited = 'Y', isOrdered = 'Y' where RetailerID in(Select RetailerID from OrderHeader)
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            db.updateSQL("Update RetailerMaster set isOrdered = 'Y' " +
                    "where RetailerID in(Select RetailerID from OrderHeader)");

            db.updateSQL("Update RetailerBeatMapping set isVisited = 'Y' " +
                    "where RetailerID in(Select RetailerID from OrderHeader)");
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public boolean checkSIHTable() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
        boolean hasData = false;
        try {
            db.openDataBase();
            String sql;
            Cursor c;

            sql = "select count(*) from stockinhandmaster where upload='N'";

            c = db.selectSQL(sql);
            if (c != null) {
                while (c.moveToNext()) {
                    if (c.getInt(0) > 0)
                        hasData = true;
                }
                c.close();
            }

            db.closeDB();
            return hasData;
        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
            return hasData;
        }

    }

    public boolean checkStockTable() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
        boolean hasData = false;
        try {
            db.openDataBase();
            String sql;
            Cursor c;

            sql = "select count(*) from StockApply where upload='N'";

            c = db.selectSQL(sql);
            if (c != null) {
                while (c.moveToNext()) {
                    if (c.getInt(0) > 0)
                        hasData = true;
                }
                c.close();
            }

            db.closeDB();
            return hasData;
        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
            return hasData;
        }

    }

    public boolean checkLoyaltyPoints() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
        boolean hasData = false;
        try {
            db.openDataBase();
            String sql;
            Cursor c;

            sql = "select count(*) from LoyaltyPoints where upload='N'";

            c = db.selectSQL(sql);
            if (c != null) {
                while (c.moveToNext()) {
                    if (c.getInt(0) > 0)
                        hasData = true;
                }
                c.close();
            }

            db.closeDB();
            return hasData;
        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
            return hasData;
        }

    }


    /**
     * Delete all the tables. reset the closeDay. Delete Images.
     *
     * @param isDayClosed -  true - day close
     */

    public void deleteTables(boolean isDayClosed) {

        ArrayList<String> exceptionTableList = new ArrayList<>();
        if (!isDayClosed) {
            exceptionTableList.add(DataMembers.tbl_EmptyReconciliationHeader);
            exceptionTableList.add(DataMembers.tbl_EmptyReconciliationDetail);
        }
        exceptionTableList.add("android_metadata");
        exceptionTableList.add(DataMembers.tbl_orderHeader);
        exceptionTableList.add(DataMembers.tbl_orderDetails);
        exceptionTableList.add(DataMembers.tbl_SIH);
        exceptionTableList.add("UrlDownloadMaster");


        // updating last visit data from transaction table before delete
        if (bmodel.configurationMasterHelper.IS_PRICE_CHECK_RETAIN_LAST_VISIT_TRAN) {
            updateLastVisitPrice();
            exceptionTableList.add("LastVisitPrice");
        }
        if (bmodel.configurationMasterHelper.IS_STOCK_CHECK_RETAIN_LAST_VISIT_TRAN) {
            updateLastVisitStock();
            exceptionTableList.add("LastVisitStock");
        }

        if (bmodel.configurationMasterHelper.IS_NEAR_EXPIRY_RETAIN_LAST_VISIT_TRAN) {
            updateLastVisitNearExpiry();
            exceptionTableList.add("LastVisitNearExpiry");
        }

        if (bmodel.configurationMasterHelper.IS_PROMOTION_RETAIN_LAST_VISIT_TRAN) {

            updateLastVisitPromotion();
            exceptionTableList.add("LastVisitPromotion");
        }

        if (bmodel.configurationMasterHelper.IS_SURVEY_RETAIN_LAST_VISIT_TRAN) {
            updateLastVisitSurvey();
            exceptionTableList.add("LastVisitSurvey");
        }

        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            closeDay(0);

            Cursor allTableName = db.selectSQL("SELECT tbl_name FROM sqlite_master where type='table'");
            if (allTableName != null) {
                while (allTableName.moveToNext()) {
                    String tableName = allTableName.getString(0);
                    if (!exceptionTableList.contains(tableName)) {
                        db.deleteSQL(tableName, null, true);
                    }
                }
                allTableName.close();
            }

            Cursor allIndexName = db.selectSQL("SELECT name FROM sqlite_master WHERE type == 'index'  and name LIKE 'index%' ");
            if (allIndexName != null) {
                while (allIndexName.moveToNext()) {
                    String indexName = allIndexName.getString(0);
                    db.executeQ("DROP INDEX IF EXISTS " + indexName);
                }
            }

            db.closeDB();

            /** Added following line to fix the Order split download issues. **/
            if (!bmodel.configurationMasterHelper.SHOW_PREV_ORDER_REPORT) {
                deleteOrderHistory();
            }

            deleteDBFromSD();

            try {

                File f = new File(
                        context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                                + "/" + DataMembers.photoFolderName + "/");

                File[] files = f.listFiles();

                if (files != null && files.length > 0)
                    for (File file : files) {
                        file.delete();
                    }
            } catch (Exception e) {
                Commons.printException("" + e);
            }

        } catch (Exception e) {
            Commons.printException("" + e);
        }

    }


    /**
     * This function will fetch the orders from OrderHeader and Details and move
     * it to PVSOrderHeader and Detail. Along with orders are storing the
     * reailer infomation and Productnames.
     */
    public void backUpPreviousDayOrder() {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            // Copy OrderHeader and past in PVSOrderHeader
            String mappingCoulmn = "OrderID,OrderDate,OrderValue,LinesPerCall,RetailerId,RouteId,upload,invoicestatus,discount,deliveryDate,isToday,RetailerName,RetailerCode,downloadDate,distribution";
            String mappingOrder = "distinct OrderID,OrderDate,OrderValue,LinesPerCall,o.RetailerId,RouteId,o.upload,invoicestatus,discount,deliveryDate,o.isToday,o.RetailerName,o.RetailerCode,downloadDate,r.sbd_dist_stock||'/'||r.sbd_dist_achieve";
            db.executeQ("INSERT INTO PVSOrderHeader "
                    + "("
                    + mappingCoulmn
                    + ") "
                    + "SELECT "
                    + mappingOrder
                    + " FROM OrderHeader o,retailerMaster r where o.retailerid=r.retailerid");

            // Copy OrderDetail and paste in PVSOrderDetail
            mappingCoulmn = "OrderID,ProductId,Qty,Rate,uomcount,PieceQty,CaseQty,uomid,retailerid,msqqty,totalamount,upload,ProductName,ProductShortName,Pcode,outerQty,dOuomQty,dOuomid";
            db.executeQ("INSERT INTO PVSOrderDetail " + "(" + mappingCoulmn
                    + ") " + "SELECT " + mappingCoulmn + " FROM OrderDetail");

            if (!hasOrderExistInHistoryForDownloadedDate())
                deleteOldBackUpOrders();

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    private boolean hasOrderExistInHistoryForDownloadedDate() {
        int i = 0;
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select  count(OrderId)  from PVSOrderHeader where downloadDate="
                            + bmodel.QT(bmodel.userMasterHelper
                            .getUserMasterBO().getDownloadDate()));
            if (c != null) {
                if (c.moveToNext()) {
                    i = c.getInt(0);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        if (i > 0)
            return true;
        else
            return false;
    }

    public void deleteOldBackUpOrders() {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            // Collect All the distinct dates
            Cursor c = db
                    .selectSQL("select  distinct(downloadDate)  from PVSOrderHeader");
            ArrayList<String> oldDates = new ArrayList<>();
            if (c != null) {
                while (c.moveToNext()) {
                    oldDates.add(c.getString(0));
                }
                c.close();
            }
            // If size is 0 , then do nothing
            if (oldDates.size() == 0)
                return;
            // Get the Max date
            String previousDay = getMaxDate(oldDates);
            // Delete PVSOrders
            db.executeQ("delete from PVSOrderDetail where OrderId=(select orderid from PVSOrderHeader where downloadDate !="
                    + bmodel.QT(previousDay) + ")");
            db.executeQ("delete from PVSOrderHeader where downloadDate !="
                    + bmodel.QT(previousDay));
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    private String getMaxDate(ArrayList<String> date) {
        String maxDate = date.get(0);
        for (String temp : date) {
            int i = SDUtil.compareDate(maxDate, temp, "yyyy/MM/dd");
            if (i < 0)
                maxDate = temp;
        }
        return maxDate;
    }

    public boolean checkForImageToUpload() {
        try {
            File f = new File(HomeScreenFragment.photoPath);
            if (f.listFiles() != null) {
                String fnames[] = f.list();
                for (String str : fnames) {
                    if ((str != null) && (str.length() > 0)) {
                        if (str.endsWith(".jpg") || str.endsWith(".jpeg")) {
                            return true;
                        }
                    }

                }
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return false;
    }

    /**
     * isDataAvailable is used to test weather data downloaded in the mandatory
     * masters or not. Following Master will be checked 1) RetailerMaster 2)
     * HHTModuleMaster 3) BeatMaster 4) StandardListMaster
     *
     * @return true if data download or false.
     */
    public boolean isDataAvailable() {

        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.openDataBase();
        Cursor c;
        String tableName = "";
        dataMissedTable = "";
        int hhtCount = 0, standList = 0;
        try {
            c = db.selectSQL("select  count(hhtCode) from "
                    + DataMembers.tbl_HhtModuleMaster);
            if (c != null) {
                if (c.moveToNext()) {
                    hhtCount = c.getInt(0);
                }
                c.close();
            }

            c = db.selectSQL("select  count(listid) from "
                    + DataMembers.tbl_StandardListMaster);
            if (c != null) {
                if (c.moveToNext()) {
                    standList = c.getInt(0);
                }
                c.close();
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }

        db.closeDB();

        if (standList > 0 && hhtCount > 0) {
            return true;
        } else {
            if (standList == 0)
                tableName = tableName + " LovMaster";
            if (hhtCount == 0)
                tableName = tableName + " Configuration";

            dataMissedTable = tableName;
        }

        return false;
    }

    /**
     * checkDataForSync is used to check wheather DB has any unsubmitted data or
     * not.
     *
     * @return true if data exist or false
     * @params withPhoto
     */

    public boolean checkDataForSync() {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            int count;

            String sb = "select  count(orderid) from orderheader where upload='N'" +
                    "union select  count(billnumber) from payment where upload='N'" +
                    "union select  count(VisitId) from OutletTimeStamp where upload='N'" +
                    "union select  count(uid) from SalesReturnHeader where upload='N'" +
                    "union select  count(stockid) from ClosingStockHeader where upload='N'" +
                    "union select  count(RetailerID) from retailerMaster where upload='N'" +
                    "union select  count(taskid) from TaskConfigurationMaster where (((isDone='1' and usercreated='0') or (isDone='0' or isDone='1'and  usercreated='1')) and upload='N')" +
                    "union select  count(surveyid) from AnswerHeader where upload='N'" +
                    "union select  count(uid) from deviateReasontable where upload='N'" +
                    "union select  count(uid) from Photocapture where upload='N'" +
                    "union select  count(uid) from SbdMerchandisingHeader where upload='N'" +
                    "union select  count(uid) from Nonproductivereasonmaster where upload='N'" +
                    "union select  count(TargetId) from DailyTargetPlanned where upload='N'" +
                    "union select count(uid) from StockProposalMaster where upload='N'" +
                    "union select count(uid) from VanUnloadDetails where upload='N'" +
                    "union select count(uid) from AssetHeader where upload='N'" +
                    "union select count(InvoiceNo) from InvoiceMaster where upload='N'" +
                    "union select count(uid) from Odameter where upload='N'" +
                    "union select count(uid) from StockApply where upload='N'" +
                    "union select  count(rowid) from DayClose where upload='N' and status='1'" +
                    "union select count(uid) from PromotionHeader where upload='N'" +
                    "union select  count(uid) from SOD_Tracking_Header where upload='N'" +
                    "union select  count(uid) from SOSKU_Tracking_Header where upload='N'" +
                    "union select  count(uid) from SOS_Tracking_Header where upload='N'" +
                    "union select  count(RetailerID) from MonthlyPlanHeaderMaster where upload='N'" +
                    "union select  count(RetailerID) from MonthlyPlanDetail where upload='N'" +
                    "union select  count(RetailerID) from NearExpiry_Tracking_Header where upload='N'" +
                    "union select  count(RetailerId) from PriceCheckHeader where upload='N'" +
                    "union select  count(RetailerId) from CompetitorHeader where upload='N'" +
                    "union select  count(RetailerId) from PriceCheckHeader where upload='N'" +
                    "union select count(retailerid) from AttendanceDetail where upload='N'" +
                    "union select count(retailerid) from EmptyReconciliationHeader where upload='N'" +
                    "union select count(timestampid) from OutletJoinCall where upload='N'" +
                    "union select count(uid) from AssetAddDelete where upload='N'" +
                    "union select count(invoiceid) from InvoiceReturnDetail where upload='N'" +
                    "union select count(Tid) from PlanogramHeader where upload='N'" +
                    "union select count(uid) from RoadActivityTransaction where upload='N'" +
                    "union select count(OrderId) from DeliveryHeader where upload='N'" +
                    "union select count(OrderId) from DeliveryDetail where upload='N'" +
                    "union select count(DistId) from DistInvoiceDetails where upload='N'" +
                    "union select count(DistId) from DistTimeStampHeader where upload='N'" +
                    "union select count(MenuCode) from DistTimeStampDetails where upload='N'" +
                    "union select count(UId) from DistStockCheckHeader where upload='N'" +
                    "union select count(UId) from DistStockCheckDetails where upload='N'" +
                    "union select count(UId) from DistOrderHeader where upload='N'" +
                    "union select count(UId) from DistOrderDetails where upload='N'" +
                    "union select count(UId) from TaskExecutionDetails where upload='N'" +
                    "union select count(uid) from SOD_Tracking_Parent_Detail where upload='N'" +
                    "union select count(uid) from SOD_Tracking_Block_Detail where upload='N'" +
                    "union select count(uid) from UserFeedBack where upload='N'" +
                    "union select count(uid) from VanDeliveryHeader where upload='N'" +
                    "union select count(uid) from VanDeliveryDetail where upload='N'" +
                    "union select count(uid) from SalesReturnReplacementDetails where upload='N'" +
                    "union select count(uid) from SalesReturnTaxDetails where upload='N'" +
                    "union select count(uid) from CS_StockEntryDetails where upload='N'" +
                    "union select count(uid) from CS_StockEntryHeader where upload='N'" +
                    "union select count(uid) from CS_CustomerConcernDetails where upload='N'" +
                    "union select count(uid) from CS_CustomerSaleDetails where upload='N'" +
                    "union select count(uid) from CS_CustomerSampleGivenDetails where upload='N'" +
                    "union select count(uid) from CS_CustomerTrialDetails where upload='N'" +
                    "union select count(uid) from CS_CustomerVisitHeader where upload='N'" +
                    "union select count(Tid) from ExpenseHeader where upload='N'" +
                    "union select count(Tid) from RetailerContractRenewalDetails where upload='N'" +
                    "union select count(uid) from LeaveApprovalDetails where upload='N'" +
                    "union select count(uid) from NewRetailerSurveyResultHeader where upload='N'" +
                    "union select count(uid) from NewRetailerSurveyResultDetail where upload='N'" +
                    "union select count(uid) from RetailerEntryDetails where upload='N'" +
                    "union select count(Tid) from LocationTracking where upload='N'" +
                    "union select count(Tid) from RetailerEditHeader where upload='N'" +
                    "union select count(Tid) from RetailerEditDetail where upload='N'" +
                    "union select count(Tid) from AttendanceDetail where upload='N'" +
                    "union select count(Tid) from LeaveDetail where upload='N'" +
                    "union select count(RetailerId) from RetailerPriorityProducts where upload='N'" +
                    "union select count(UID) from LoyaltyRedemptionDetail where upload='N'" +
                    "union select count(UID) from LoyaltyRedemptionHeader where upload='N'" +
                    "union select count(pid) from CS_SIHDetails where upload='N'" +
                    "union select count(receipt_id) from CS_StockReceiptHeader where upload='N'" +
                    "union select count(uid) from CS_RejectedVarianceStatus where upload='N'" +
                    "union select count(uid) from CS_GroomingHeader where upload='N'" +
                    "union select count(uid) from CS_StockEntryVarianceHeader where upload='N'" +
                    "union select count(Tid) from ModuleActivityDetails where upload='N'" +
                    "union select count(uid) from AttendanceTimeDetails where upload='N'" +
                    "union select count(UID) from NonFieldActivity where upload='N'";
            Cursor c = db.selectSQL(sb);
            if (c != null) {
                while (c.moveToNext()) {
                    count = c.getInt(0);
                    if (count > 0) {
                        c.close();
                        db.closeDB();
                        return true;
                    }
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return false;
    }

    /**
     * method to use delete files from sdcard
     *
     * @param folderPath
     * @param fnamesStarts
     */

    public void deleteFiles(String folderPath, String fnamesStarts) {
        File folder = new File(folderPath);

        File files[] = folder.listFiles();

        for (File tempFile : files) {
            if (tempFile != null) {
                if (tempFile.getName().startsWith(fnamesStarts))
                    tempFile.delete();
            }
        }
    }

    public List<SyncRetailerBO> getRetailerIsVisited() {
        List<SyncRetailerBO> isVisitedRetailerList = null;
        try {
            isVisitedRetailerList = new ArrayList<>();
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT DISTINCT OT.RetailerID, RM.RetailerName FROM OutletTimestamp OT INNER JOIN RetailerMaster RM ON OT.RetailerID = RM.RetailerID Where OT.upload = 'N' ORDER BY RM.RetailerName");
            if (c != null) {
                while (c.moveToNext()) {
                    SyncRetailerBO retBO = new SyncRetailerBO();
                    retBO.setRetailerId(c.getString(0));
                    retBO.setRetailerName(c.getString(1));
                    retBO.setChecked(true);
                    isVisitedRetailerList.add(retBO);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return isVisitedRetailerList;
    }

    public StringBuilder getRetailerIds() {
        return retailerIds;
    }

    public void setRetailerIds(StringBuilder retailerIds) {
        this.retailerIds = retailerIds;
    }

    public RequestQueue getRequestQueue() {

        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(context);
            mRequestQueue.getCache().clear();

        }
        return mRequestQueue;

    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    /**
     * @param jsonObject - response json object
     *                   Method to split table name and columns from json object
     *                   response
     */
    public void parseJSONAndInsert(JSONObject jsonObject, boolean isDeleteTable) {

        try {

            String tablename = jsonObject.getString(JSON_MASTER_KEY);
            JSONArray jsonArray = jsonObject.getJSONArray(JSON_FIELD_KEY);

            String columns = jsonArray.toString();
            columns = columns.replaceAll("\\[", "").replaceAll("\\]", "");

            ArrayList<String> valuesList = new ArrayList<String>();

            JSONArray first = jsonObject.getJSONArray(JSON_DATA_KEY);

            for (int j = 0; j < first.length(); j++) {
                JSONArray value = (JSONArray) first.get(j);

                String firstValue = value.toString();
                firstValue = firstValue.replaceAll("\\[", "").replaceAll("\\]",
                        "");

                firstValue = firstValue.replace("\\/", "/");

                valuesList.add(firstValue);

            }
            inserRecords(tablename, columns, valuesList, isDeleteTable);

        } catch (JSONException e) {
            Commons.printException("" + e);
        }

    }


    public void inserVanloadRecodrs(JSONObject jsonObject) {

        try {

            String tablename = jsonObject.getString(JSON_MASTER_KEY);
            JSONArray jsonArray = jsonObject.getJSONArray(JSON_FIELD_KEY);

            String columns = jsonArray.toString();
            columns = columns.replaceAll("\\[", "").replaceAll("\\]", "");

            String withoutQoutesColumns = columns.replace("\"", "");
            String[] fieldArray = withoutQoutesColumns.split(",");
            int uidPos = -1;
            for (String field : fieldArray) {
                uidPos = uidPos + 1;
                if (field.equalsIgnoreCase("uid")) {
                    break;
                }
            }

            ArrayList<String> valuesList = new ArrayList<>();

            JSONArray first = jsonObject.getJSONArray(JSON_DATA_KEY);
            ArrayList<VanLoadMasterBO> uidList = bmodel.vanmodulehelper.downloadExistingUid();

            for (int j = 0; j < first.length(); j++) {
                boolean flag = false;
                JSONArray value = (JSONArray) first.get(j);
                for (VanLoadMasterBO uid : uidList) {

                    if (value.getString(uidPos).equals(uid.getRfield1())) {
                        flag = true;
                        break;

                    }
                }
                if (!flag) {
                    String firstValue = value.toString();
                    firstValue = firstValue.replaceAll("\\[", "").replaceAll("\\]",
                            "");

                    firstValue = firstValue.replace("\\/", "/");
                    valuesList.add(firstValue);
                }


            }

            if (valuesList.size() > 0) {
                inserRecords(tablename, columns, valuesList, false);

                DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                        DataMembers.DB_PATH);
                db.openDataBase();
                updateTable("temp_vanload", db);
                db.closeDB();
            }

        } catch (JSONException e) {
            Commons.printException("" + e);
        }

    }

    public void deleteUrlDownloadMaster() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.openDataBase();
        db.deleteSQL("UrlDownloadMaster", null, true);
        db.closeDB();
    }

    /**
     * @param tablename
     * @param columns
     * @param valueList After getting tableName and columns from json object
     *                  inserted into database
     */
    private void inserRecords(String tablename, String columns,
                              ArrayList<String> valueList, boolean isDeleteTable) {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
        try {
            db.createDataBase();
            db.openDataBase();


            if (isDeleteTable)
                db.deleteSQL(tablename, null, true);


            int recCount = 0;
            if (valueList != null) {
                StringBuffer queryString = new StringBuffer();
                for (String values : valueList) {

                    recCount = recCount + 1;
                    if (queryString.length() == 0) {
                        queryString.append("INSERT INTO ").append(tablename)
                                .append(" ( ").append(columns).append(" ) ")
                                .append("SELECT ").append(values);
                    } else {
                        queryString.append(" UNION ALL SELECT ").append(
                                values);
                    }

                    if (recCount == 400) {
                        db.multiInsert(queryString.toString());
                        queryString = new StringBuffer();
                        recCount = 0;
                    }

                }
                if (queryString.length() > 0) {
                    db.multiInsert(queryString.toString());
                }
                db.closeDB();
                tablename = null;
            }

        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
            mJsonObjectResponseByTableName.remove(tablename);
        }


    }

    public void downloadMasterListBySelectedRetailer(ArrayList<RetailerMasterBO> retailerList, FROM_SCREEN fromWhere) {
        mJsonObjectResponseByTableName = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        sb.append(DataMembers.SERVER_URL);
        sb.append("/IncrementalSync/Initiate");
        try {
            JSONObject json = new JSONObject();
            json.put("UserId", bmodel.userMasterHelper.getUserMasterBO()
                    .getUserid());
            json.put("VersionCode", bmodel.getApplicationVersionNumber());
            JSONArray jsonArray = new JSONArray();
            for (RetailerMasterBO retailerMasterBO : retailerList) {
                jsonArray.put(retailerMasterBO.getRetailerID());
            }
            json.put("RetailerIds", jsonArray);
            callVolley(sb.toString(), fromWhere, 0, DATA_DOWNLOAD_BY_RETAILER, json);
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public enum DownloadType {
        NORMAL_DOWNLOAD(0),
        RETAILER_WISE_DOWNLOAD(1),
        DISTRIBUTOR_WISE_DOWNLOAD(2);
        private int value;

        DownloadType(int value) {
            this.value = value;
        }
    }

    /**
     * @param fromLogin     0 -from login,1-from synchronization,2 - NewOutlet
     * @param whichDownload 0 - normal download,1 - retailer wise download,2 - distributor wise download
     *                      download master from server
     */
    public void downloadMasterAtVolley(final FROM_SCREEN fromLogin, DownloadType whichDownload) {
        mDownloadUrlCount = 0;
        if (mDownloadUrlList != null) {
            int size = mDownloadUrlList.size();
            try {
                JSONObject json = new JSONObject();
                json.put("UserId", bmodel.userMasterHelper.getUserMasterBO()
                        .getUserid());
                json.put("VersionCode", bmodel.getApplicationVersionNumber());

                int insert = VOLLEY_DOWNLOAD_INSERT;
                if (whichDownload == DownloadType.RETAILER_WISE_DOWNLOAD) {
                    json.put("IsRetailer", 1);
                } else if (whichDownload == DownloadType.DISTRIBUTOR_WISE_DOWNLOAD) {
                    json.put("IsDistributor", 1);
                    insert = DISTRIBUTOR_WISE_DOWNLOAD_INSERT;

                }

                mURLList = new HashMap<>();
                mTableList = new HashMap<>();

                for (String url : mDownloadUrlList) {
                    String downloadUrl = DataMembers.SERVER_URL + url;
                    callVolley(downloadUrl, fromLogin, size,
                            insert, json);
                }
            } catch (JSONException e) {
                Commons.printException("" + e);
            }
        }
    }

    public void downloadLastVisitTranAtVolley(final FROM_SCREEN fromLogin, int whichDownload) {
        mDownloadUrlCount = 0;
        if (mDownloadUrlList != null) {
            int size = mDownloadUrlList.size();
            try {
                JSONObject json = new JSONObject();
                json.put("UserId", bmodel.userMasterHelper.getUserMasterBO()
                        .getUserid());
                json.put("VersionCode", bmodel.getApplicationVersionNumber());
                if (whichDownload == 1)
                    json.put("IsRetailer", 1);
                else if (whichDownload == 2)
                    json.put("IsDistributor", 1);

                mURLList = new HashMap<>();
                mTableList = new HashMap<>();

                for (String url : mDownloadUrlList) {
                    String downloadUrl = DataMembers.SERVER_URL + url;
                    callVolley(downloadUrl, fromLogin, size,
                            LAST_VISIT_TRAN_DOWNLOAD_INSERT, json);
                }
            } catch (JSONException e) {
                Commons.printException(e);
            }
        }
    }

    public void getURLResponse() {
        ArrayList<JExcelHelper.ExcelBO> mExcelBOList = new ArrayList<>();
        JExcelHelper.ExcelBO excel;

        ArrayList<String> columnNames = new ArrayList<>();
        columnNames.add("URL");
        columnNames.add("Response");
        columnNames.add("Error");
        columnNames.add("Time(ms)");
        columnNames.add("Data Size(KB)");

        ArrayList<ArrayList<String>> columnValues = new ArrayList<>();
        for (Map.Entry<String, URLListBO> entry : mURLList.entrySet()) {
            String key = entry.getKey();
            URLListBO value = entry.getValue();
            ArrayList<String> row = new ArrayList<>();
            row.add(key);
            row.add(value.getServerResponse());
            row.add(value.getError());
            row.add(value.getTime());
            row.add(value.getDataSize());
            columnValues.add(row);
        }

        excel = bmodel.mJExcelHelper.new ExcelBO();
        excel.setSheetName("Server Response");
        excel.setColumnNames(columnNames);
        excel.setColumnValues(columnValues);

        mExcelBOList.add(excel);

        columnNames = new ArrayList<>();
        columnNames.add("Table Name");
        columnNames.add("Time(ms)");

        columnValues = new ArrayList<>();
        for (Map.Entry<String, String> entry : mTableList.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            ArrayList<String> row = new ArrayList<>();
            row.add(key);
            row.add(value);
            columnValues.add(row);
        }

        excel = bmodel.mJExcelHelper.new ExcelBO();
        excel.setSheetName("Data Save");
        excel.setColumnNames(columnNames);
        excel.setColumnValues(columnValues);

        mExcelBOList.add(excel);

        bmodel.mJExcelHelper.createExcel("Data_Download_Save.xls", mExcelBOList);
    }

    private class URLListBO {
        String mServerResponse = "";
        String mError = "";
        String mTime = "";
        String mDataSize = "0";

        public String getServerResponse() {
            return mServerResponse;
        }

        public void setServerResponse(String mServerResponse) {
            this.mServerResponse = mServerResponse;
        }

        public String getError() {
            return mError;
        }

        public String getTime() {
            return mTime;
        }

        public void setTime(String mTime) {
            this.mTime = mTime;
        }

        public String getDataSize() {
            return mDataSize;
        }

        public void setDataSize(String mDataSize) {
            this.mDataSize = mDataSize;
        }
    }


    private void callVolley(final String url, final FROM_SCREEN isFromWhere,
                            final int totalListCount, final int which, JSONObject json) {
        JsonObjectRequest jsonObjectRequest;
        try {

            json.put("MobileDate", Utils.getDate("yyyy/MM/dd HH:mm:ss"));
            json.put("MobileUTCDateTime",
                    Utils.getGMTDateTime("yyyy/MM/dd HH:mm:ss"));
            if (!DataMembers.backDate.isEmpty())
                json.put("RequestDate",
                        SDUtil.now(SDUtil.DATE_TIME_NEW));
        } catch (Exception e) {
            Commons.printException("" + e);
        }

        final long startTime = System.nanoTime();
        if (mURLList != null)
            mURLList.put(url, new URLListBO());

        jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST, url, json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Commons.printException("Volley Error", error);
            }
        }

        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put(SECURITY_HEADER, mSecurityKey);
                headers.put("Content-Type", "application/json; charset=utf-8");

                return headers;
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                if (response.data.length > 10000)
                    setShouldCache(false);

                return super.parseNetworkResponse(response);
            }

            @Override
            protected VolleyError parseNetworkError(VolleyError volleyError) {
                return super.parseNetworkError(volleyError);
            }

            @Override
            protected void deliverResponse(JSONObject jsonObject) {
                super.deliverResponse(jsonObject);
                String successUrl = getUrl();

                increaseCount();

                Intent i = new Intent(context, DownloadService.class);
                i.putExtra(SYNXC_STATUS, which);
                i.putExtra(VOLLEY_RESPONSE, VOLLEY_SUCCESS_RESPONSE);
                i.putExtra("TotalCount", totalListCount);
                i.putExtra("UpdateCount", mDownloadUrlCount);
                i.putExtra("isFromWhere", isFromWhere);
                Iterator itr = jsonObject.keys();
                String tableName = "";
                ArrayList<String> tableList = new ArrayList<>();
                int mandatory = 0;
                if (mMandatoryByUrl != null && mMandatoryByUrl.get(successUrl) != null) {
                    mandatory = mMandatoryByUrl.get(successUrl);
                }
                try {
                    while (itr.hasNext()) {
                        String key = (String) itr.next();

                        if (key.equals("Tables")) {
                            JSONArray jsonArray = jsonObject.getJSONArray(JSON_KEY);

                            long endTime = (System.nanoTime() - startTime) / 1000000;
                            if (mURLList != null) {
                                mURLList.get(url).setTime(endTime + "");
                                mURLList.get(url).setServerResponse("Tables");
                                mURLList.get(url).setDataSize((jsonObject.toString().getBytes().length / 1024.0) + "");
                            }

                            for (int k = 0; k < jsonArray.length(); k++) {

                                JSONObject value = (JSONObject) jsonArray.get(k);

                                Iterator tableItr = value.keys();
                                while (tableItr.hasNext()) {
                                    String innerKey = (String) tableItr.next();
                                    if (innerKey.equals("Master")) {
                                        if (value != null) {
                                            tableName = value.getString("Master");
                                        }

                                        mJsonObjectResponseByTableName.put(tableName, value);
                                        tableList.add(tableName);
                                        break;
                                    } else if (innerKey.equals(ERROR_CODE)) {
                                        if (value != null) {
                                            String errorCode = value.getString(innerKey);
                                            if (!errorCode.equals("0") && !errorCode.equals(DATA_NOT_AVAILABLE_ERROR)) {
                                                if (mandatory == 1) {
                                                    i.putExtra(VOLLEY_RESPONSE, VOLLEY_FAILURE_RESPONSE);
                                                    i.putExtra(ERROR_CODE, jsonObject.getString(key));
                                                    context.startService(i);
                                                    deleteAllRequestQueue();
                                                    return;

                                                } else {
                                                    i.putExtra(VOLLEY_RESPONSE, VOLLEY_SUCCESS_RESPONSE);
                                                    tableName = value.getString(ERROR_CODE);
                                                    mJsonObjectResponseByTableName.put(tableName, value);
                                                    tableList.add(tableName);
                                                    value = null;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            i.putStringArrayListExtra(JSON_OBJECT_TABLE_LIST, tableList);
                            context.startService(i);
                            break;
                        } else {
                            if (key.equals("Master")) {
                                tableName = jsonObject.getString("Master");

                                long endTime = (System.nanoTime() - startTime) / 1000000;
                                if (mURLList != null) {
                                    mURLList.get(url).setTime(endTime + "");
                                    mURLList.get(url).setServerResponse("Data - " + tableName);
                                    mURLList.get(url).setDataSize((jsonObject.toString().getBytes().length / 1024.0) + "");
                                }

                                mJsonObjectResponseByTableName.put(tableName, jsonObject);
                                tableList.add(tableName);
                                i.putStringArrayListExtra(JSON_OBJECT_TABLE_LIST, tableList);
                                context.startService(i);
                                break;
                            } else if (key.equals(ERROR_CODE)) {
                                String errorCode = jsonObject.getString(key);
                                if (!errorCode.equals("0") && !errorCode.equals(DATA_NOT_AVAILABLE_ERROR)) {
                                    if (mandatory == 1) {
                                        i.putExtra(VOLLEY_RESPONSE, VOLLEY_FAILURE_RESPONSE);
                                        i.putExtra(ERROR_CODE, jsonObject.getString(key));
                                        context.startService(i);
                                        deleteAllRequestQueue();
                                        return;

                                    } else {
                                        tableName = jsonObject.getString(ERROR_CODE);

                                        long endTime = (System.nanoTime() - startTime) / 1000000;
                                        if (mURLList != null) {
                                            mURLList.get(url).setTime(endTime + "");
                                            mURLList.get(url).setServerResponse("ErrorCode - " + tableName);
                                            mURLList.get(url).setDataSize((jsonObject.toString().getBytes().length / 1024.0) + "");
                                        }

                                        mJsonObjectResponseByTableName.put(tableName, jsonObject);
                                        tableList.add(tableName);
                                        i.putStringArrayListExtra(JSON_OBJECT_TABLE_LIST, tableList);
                                        context.startService(i);
                                        break;
                                    }
                                }

                            } else if (key.equals("Response")) {

                                mJsonObjectResponseByTableName.put(SynchronizationHelper.ERROR_CODE, jsonObject);
                                tableList.add(SynchronizationHelper.ERROR_CODE);
                                i.putStringArrayListExtra(JSON_OBJECT_TABLE_LIST, tableList);
                                context.startService(i);

                            }
                        }


                    }

                } catch (Exception e) {
                    Commons.printException("" + e);
                }
            }

            @Override
            public void deliverError(VolleyError error) {
                super.deliverError(error);
                String url = getUrl();
                int mantatory = 0;
                if (mMandatoryByUrl != null && mMandatoryByUrl.get(url) != null)
                    mantatory = mMandatoryByUrl.get(url);
                increaseCount();
                if (mantatory == 1) {
                    Intent i = new Intent(context, DownloadService.class);
                    i.putExtra(SYNXC_STATUS, which);
                    i.putExtra(VOLLEY_RESPONSE, VOLLEY_FAILURE_RESPONSE);
                    i.putExtra("isFromWhere", isFromWhere);

                    if (error instanceof TimeoutError) {
                        i.putExtra(ERROR_CODE, "E32");
                    } else if (error instanceof NoConnectionError) {
                        i.putExtra(ERROR_CODE, "E06");
                    } else if (error instanceof ServerError) {
                        i.putExtra(ERROR_CODE, "E01");
                    } else if (error instanceof NetworkError) {
                        i.putExtra(ERROR_CODE, "E01");
                    } else if (error instanceof ParseError) {
                        i.putExtra(ERROR_CODE, "E31");
                    } else {
                        i.putExtra(ERROR_CODE, "E01");
                    }

                    context.startService(i);
                    deleteAllRequestQueue();
                } else {
                    //mansoor.k for Volley Time out response is updated at last
                    if (totalListCount == mDownloadUrlCount) {
                        Intent i = new Intent(context, DownloadService.class);
                        i.putExtra(SYNXC_STATUS, which);
                        i.putExtra(VOLLEY_RESPONSE, VOLLEY_SUCCESS_RESPONSE);
                        i.putExtra("TotalCount", totalListCount);
                        i.putExtra("UpdateCount", mDownloadUrlCount);
                        i.putExtra("isFromWhere", isFromWhere);
                        i.putStringArrayListExtra(JSON_OBJECT_TABLE_LIST, new ArrayList<String>());
                        context.startService(i);
                    }
                }
            }
        };

        RetryPolicy policy = new DefaultRetryPolicy(
                (int) TimeUnit.SECONDS.toMillis(30),
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        jsonObjectRequest.setShouldCache(false);
        addToRequestQueue(jsonObjectRequest,
                TAG_JSON_OBJ);
    }

    public HashMap<String, JSONObject> getmJsonObjectResponseByTableName() {
        return mJsonObjectResponseByTableName;
    }

    public void setmJsonObjectResponseBytableName(HashMap<String, JSONObject> jsonObjectResponseBytableName) {
        this.mJsonObjectResponseByTableName = jsonObjectResponseBytableName;
    }

    public HashMap<String, JSONObject> downloadFromDb(JSONObject jsonObject) {

        mJsonObjectResponseByTableName = new HashMap<>();
//            callVolley(sb.toString(), FROM_SCREEN.COUNTER_SALES_SELECTION, 1, VOLLEY_CUSTOMER_SEARCH, json);
        Iterator itr = jsonObject.keys();
        String tableName = "";
        ArrayList<String> tableList = new ArrayList<>();
        int mandatory = 0;
        try {
            while (itr.hasNext()) {
                String key = (String) itr.next();

                if (key.equals("Tables")) {
                    JSONArray jsonArray = jsonObject.getJSONArray(JSON_KEY);


                    for (int k = 0; k < jsonArray.length(); k++) {

                        JSONObject value = (JSONObject) jsonArray.get(k);

                        Iterator tableItr = value.keys();
                        while (tableItr.hasNext()) {
                            String innerKey = (String) tableItr.next();
                            if (innerKey.equals("Master")) {
                                if (value != null) {
                                    tableName = value.getString("Master");
                                }

                                mJsonObjectResponseByTableName.put(tableName, value);
                                tableList.add(tableName);
                                break;
                            } else if (innerKey.equals(ERROR_CODE)) {
                                if (value != null) {
                                    String errorCode = value.getString(innerKey);
                                    if (!errorCode.equals("0") && !errorCode.equals(DATA_NOT_AVAILABLE_ERROR)) {
                                        if (mandatory == 1) {
                                            return null;

                                        } else {
                                            tableName = value.getString(ERROR_CODE);
                                            mJsonObjectResponseByTableName.put(tableName, value);
                                            tableList.add(tableName);
                                            value = null;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    break;
                } else {
                    if (key.equals("Master")) {
                        tableName = jsonObject.getString("Master");


                        mJsonObjectResponseByTableName.put(tableName, jsonObject);
                        tableList.add(tableName);
                        break;
                    } else if (key.equals(ERROR_CODE)) {
                        String errorCode = jsonObject.getString(key);
                        if (!errorCode.equals("0") && !errorCode.equals(DATA_NOT_AVAILABLE_ERROR)) {
                            tableName = jsonObject.getString(ERROR_CODE);


                            mJsonObjectResponseByTableName.put(tableName, jsonObject);
                            tableList.add(tableName);
                            break;
                        }

                    } else if (key.equals("Response")) {

                        mJsonObjectResponseByTableName.put(SynchronizationHelper.ERROR_CODE, jsonObject);
                        tableList.add(SynchronizationHelper.ERROR_CODE);

                    }
                }


            }
            return mJsonObjectResponseByTableName;
        } catch (Exception e) {
            Commons.printException("" + e);
            return null;
        }


    }

    public Boolean checkForAutoUpdate() {
        try {
            String autoUpdateApkUrl = "0";
            JSONObject json = new JSONObject();
            StringBuilder url = new StringBuilder();

            url.append(DataMembers.SERVER_URL);
            url.append(AUTOUPDATE_APPEND_URL);

            json.put("UserId", bmodel.userMasterHelper.getUserMasterBO()
                    .getUserid());
            json.put("VersionCode", bmodel.getApplicationVersionNumber());
            json.put("MobileDate", Utils.getDate("yyyy/MM/dd HH:mm:ss"));
            json.put("MobileUTCDateTime",
                    Utils.getGMTDateTime("yyyy/MM/dd HH:mm:ss"));

            MyHttpConnectionNew http = new MyHttpConnectionNew();
            http.create(MyHttpConnectionNew.POST, url.toString(), null);
            http.setParamsJsonObject(json);
            http.addHeader(SECURITY_HEADER, mSecurityKey);

            http.connectMe();

            Vector<String> responseVector = http.getResult();

            if (responseVector == null) {
                responseVector = new Vector<>();
            }

            if (responseVector.size() > 0) {
                for (String s : responseVector) {
                    JSONObject responseObject = new JSONObject(s);
                    autoUpdateApkUrl = responseObject.getString("Data");
                    Commons.print("Auto Update Response URL " + autoUpdateApkUrl);
                }
            }

            if (autoUpdateApkUrl.startsWith("http")) {
                SharedPreferences myPrefs1 = bmodel.getSharedPreferences(
                        "autoupdate", Activity.MODE_PRIVATE);
                SharedPreferences.Editor prefsEditor = myPrefs1.edit();
                prefsEditor.putString("URL", autoUpdateApkUrl);
                prefsEditor.putString("isUpdateExist", "True");
                prefsEditor.apply();
                return true;
            } else {
                Commons.print("Auto Update URL is not a valid URL : " + autoUpdateApkUrl);
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return false;
    }

    /**
     * Delete all volley request
     */
    private void deleteAllRequestQueue() {
        mRequestQueue.cancelAll(TAG_JSON_OBJ);

    }

    public void loadMasterUrlFromDB(boolean isCommonTableDownload) {
        mDownloadUrlList = new ArrayList<>();
        mMandatoryByUrl = new HashMap<>();
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
        try {
            db.createDataBase();
            db.openDataBase();

            StringBuilder sb = new StringBuilder();
            sb.append("select url,IsMandatory from UrlDownloadMaster where mastername!='STOCKINHANDMASTER'");

            if (!isCommonTableDownload)
                sb.append(" and typecode='SYNMAS' and IsOnDemand=1");
            else
                sb.append(" and ((typecode='SYNMAS' and IsOnDemand=0) OR typecode ='SYNCCONST') ");

            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    mDownloadUrlList.add(c.getString(0));
                    mMandatoryByUrl.put(DataMembers.SERVER_URL + c.getString(0), c.getInt(1));
                }
            }
            c.close();
            db.closeDB();

        } catch (Exception e) {
            db.closeDB();
            Commons.printException("" + e);
        }

    }

    public void downloadMasterUrlFromDBRetailerWise() {
        mDownloadUrlList = new ArrayList<>();
        mMandatoryByUrl = new HashMap<>();
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
        try {
            db.createDataBase();
            db.openDataBase();
            String sb = "select url,IsMandatory from UrlDownloadMaster where TypeCode='SYNAU'" +
                    " and IsOnDemand=1";

            Cursor c = db.selectSQL(sb);
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    mDownloadUrlList.add(c.getString(0));
                    mMandatoryByUrl.put(DataMembers.SERVER_URL + c.getString(0), c.getInt(1));
                }
            }
            c.close();
            db.closeDB();

        } catch (Exception e) {
            db.closeDB();
            Commons.printException("" + e);
        }

    }

    public void downloadTransactionUrl() {
        mDownloadUrlList = new ArrayList<>();
        mMandatoryByUrl = new HashMap<>();
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
        try {
            db.createDataBase();
            db.openDataBase();
            String sb = "select url,IsMandatory from UrlDownloadMaster where TypeCode='SYNAU'";

            Cursor c = db.selectSQL(sb);
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    mDownloadUrlList.add(c.getString(0));
                    mMandatoryByUrl.put(DataMembers.SERVER_URL + c.getString(0), c.getInt(1));
                }
            }
            c.close();
            db.closeDB();

        } catch (Exception e) {
            db.closeDB();
            Commons.printException("" + e);
        }

    }


    public ArrayList<String> getUrlList() {
        if (mDownloadUrlList != null) {
            return mDownloadUrlList;
        }
        return new ArrayList<>();
    }

    private synchronized void increaseCount() {
        mDownloadUrlCount++;
    }

    private synchronized void decreaseCount() {
        mDownloadUrlCount--;
    }

    public void updateTable(String tableName, DBUtil db) {


        StringBuffer sb = new StringBuffer();
        if (tableName.equalsIgnoreCase("temp_productmaster")) {

            Cursor c = db.selectSQL("select pid from temp_productmaster");

            if (c.getCount() > 0) {
                sb.append("INSERT INTO ProductMaster (PID, PName, sih, pCode, psname, barcode, vat, isfocus, dUomId, ");
                sb.append("msqQty, dUomQty, mrp, RField1, RField2, RField3, wsih, IsAlloc,  dOuomQty, dOuomid,  CaseBarcode, ");
                sb.append("OuterBarcode, isReturnable, suggestqty, isMust, maxQty, stdpcs, stdcase, stdouter, issalable, baseprice, ");
                sb.append("piece_uomid, isBom, TypeID, PLid, ParentId, PtypeId, sequence,weight,HasSerial,tagDescription) ");
                sb.append("SELECT P.PID, P.PName, P.sih, P.pCode, P.psname, (CASE WHEN IFNULL(A.piecebarcode,'') = '' THEN P.barcode ELSE A.piecebarcode END) bar code update , P.vat, P.isfocus, ");
                sb.append("IFNULL(A.caseUomId,0), P.msqQty, IFNULL(A.caseqty,0), P.mrp, P.RField1, P.RField2, P.RField3, P.wsih, P.IsAlloc,  ");
                sb.append("IFNULL(A.boxqty,0), IFNULL(A.boxUomId,0),  IFNULL(A.casebarcode,0), IFNULL(A.boxbarcode,0), P.isReturnable, ");
                sb.append("P.suggestqty, P.isMust, P.maxQty, P.stdpcs, P.stdcase, P.stdouter, P.issalable,P.baseprice, IFNULL(A.pieceUomId,0), ");
                sb.append("P.isBom, P.TypeID, P.PLid, P.ParentId,P.PtypeId, P.sequence,P.weight,P.Hasserial,P.tagDescription FROM temp_ProductMaster P");
                sb.append(" LEFT JOIN(select * from (");
                sb.append("SELECT t.PID, t1.uomqty as caseqty,t2.uomqty as pieceqty,t3.uomqty as boxqty,");
                sb.append("t1.uombarcode as casebarcode,t2.uombarcode as piecebarcode,t3.uombarcode as boxbarcode,");
                sb.append("t1.uomid as caseUomId,t2.uomid as pieceUomId,t3.uomid as boxUomId FROM temp_productuommaster t  ");
                sb.append("left join temp_productuommaster t1 on t1.pid=t.pid and upper(t1.uomname) ='CASE'");
                sb.append("left join temp_productuommaster t2 on t2.pid=t.pid and upper(t2.uomname) ='PIECE'");
                sb.append("left join temp_productuommaster t3 on t3.pid=t.pid and upper(t3.uomname) ='MSQ' group by t.pid)) A ");
                sb.append(" ON P.PID = A.PID");
                db.executeQ(sb.toString());
                db.deleteSQL("temp_productmaster", null, true);
                db.deleteSQL("temp_productuommaster", null, true);
            }
            try {
                db.executeQ("CREATE INDEX index_productmaster ON ProductMaster(pid,PLid,ParentId)");
                db.executeQ("CREATE INDEX index_productlevel ON ProductLevel(LevelId)");
                db.executeQ("CREATE INDEX index_producttagmaster ON ProductTaggingMaster(TaggingTypelovID)");
                db.executeQ("CREATE INDEX index_producttaggrpmaster ON ProductTaggingGroupMapping(Groupid)");
                db.executeQ("CREATE INDEX index_producttaggingmap ON ProductTaggingCriteriaMapping(locid)");
                db.executeQ("CREATE INDEX index_productmasterpid ON ProductMaster(ParentId)");
                db.executeQ("CREATE INDEX index_schememaster ON SchemeMaster(SchemeID)");
                db.executeQ("CREATE INDEX index_schemecritmap ON SchemeCriteriaMapping(SchemeID)");
                db.executeQ("CREATE INDEX index_schemecountmaster ON SchemeApplyCountMaster(SchemeID)");
                db.executeQ("CREATE INDEX index_schemebuymaster ON SchemeBuyMaster(SchemeID)");
                db.executeQ("CREATE INDEX index_schemeattrmap ON SchemeAttributeMapping(GroupID)");
                db.executeQ("CREATE INDEX index_schemefreeproduct ON SchemeFreeProducts(SchemeID)");
                db.executeQ("CREATE INDEX index_schemefreemaster ON SchemeFreeMaster(SchemeID)");
                db.executeQ("CREATE INDEX index_schemefreemaster ON SchemeFreeMaster(SchemeID)");
                db.executeQ("CREATE INDEX index_discountprdmap ON DiscountProductMapping(DiscountId)");
                db.executeQ("CREATE INDEX index_standardlistmaster ON StandardListMaster(ListId)");
                db.executeQ("CREATE INDEX index_entityattributemaster ON EntityAttributeMaster(AttributeId)");
            } catch (Exception e) {
                Commons.printException(e);
            }


        } else if (tableName.equalsIgnoreCase("temp_priceMaster")) {

            Cursor c = db.selectSQL("select pid from temp_pricemaster");
            if (c.getCount() > 0) {
                sb.append("insert into pricemaster (pid,scid,srp1,srp2,csrp1,csrp2,osrp1,osrp2,batchid,priceoffvalue,PriceOffId,cp)");
                sb.append("select pm.pid,ifnull(a.scid,ifnull(b.scid,ifnull(c.scid,0))) as scid1, a.srp1,a.srp2,b.srp1,b.srp2,");
                sb.append("c.srp1,c.srp2,ifnull(a.batchid,ifnull(b.batchid,ifnull(c.batchid,0))) as batchid1,");
                sb.append("ifnull(a.priceoffvalue,ifnull(b.priceoffvalue,ifnull(c.priceoffvalue,0))) ,ifnull(a.PriceOffId,");
                sb.append("ifnull(b.PriceOffId,ifnull(c.PriceOffId,0))),ifnull(a.cp,ifnull(b.cp,ifnull(c.cp,0))) from productmaster pm  ");
                sb.append("inner join temp_pricemaster tm on pm.pid=tm.pid ");
                sb.append("left join temp_pricemaster a on a.uom='PIECE' and tm.pid=a.pid and tm.scid=a.scid and a.batchid= tm.batchid ");
                sb.append("left join temp_pricemaster b on b.uom='CASE' and tm.pid=b.pid and tm.scid=b.scid and b.batchid= tm.batchid ");
                sb.append("left join temp_pricemaster c on c.uom='MSQ' and tm.pid=c.pid and tm.scid=c.scid and c.batchid= tm.batchid ");
                sb.append("group by pm.pid ,scid1,batchid1 order by pm.pid");
                db.executeQ(sb.toString());
                db.deleteSQL("temp_pricemaster", null, true);
            }
            try {
                db.executeQ("CREATE INDEX index_pricemaster ON pricemaster(pid,scid)");
            } catch (Exception e) {
                Commons.printException(e);
            }

        } else if (tableName.equalsIgnoreCase("temp_product_priceMaster")) {
            if (IsDataAvailableInTable("temp_product_priceMaster")) {
                sb = new StringBuffer();
                sb.append("update ProductMaster set  Mrp=(select TPP.Mrp from temp_product_priceMaster TPP where ProductMaster.Pid=TPP.Pid),");
                sb.append("baseprice =(select TPP.baseprice from temp_product_priceMaster TPP where ProductMaster.Pid=TPP.Pid)");
                db.executeQ(sb.toString());
                db.deleteSQL("temp_product_priceMaster", null, true);
            }
        } else if (tableName.equalsIgnoreCase("temp_productopeningstock")) {
            if (IsDataAvailableInTable("temp_productopeningstock")) {
                sb = new StringBuffer();
                sb.append("update ProductMaster set  SIH =ifnull((select TPS.SIH from temp_productopeningstock TPS where ProductMaster.Pid=TPS.Pid),0)");
                db.executeQ(sb.toString());
                db.deleteSQL("temp_productopeningstock", null, true);
            }
        } else if (tableName.equalsIgnoreCase("temp_productstandardstockmaster")) {
            if (IsDataAvailableInTable("temp_productstandardstockmaster")) {
                sb = new StringBuffer();
                sb.append("update ProductMaster set  stdcase=(select TPS.Qty from temp_productstandardstockmaster TPS where ProductMaster.Pid=TPS.Pid and duomid=TPS.uomid),");
                sb.append("stdouter=(select TPS.Qty from temp_productstandardstockmaster TPS where ProductMaster.Pid=TPS.Pid and dOuomid=TPS.uomid),");
                sb.append("stdpcs=(select TPS.Qty from temp_productstandardstockmaster TPS where ProductMaster.Pid=TPS.Pid and piece_uomid=TPS.uomid)");
                db.executeQ(sb.toString());
                db.deleteSQL("temp_productstandardstockmaster", null, true);
            }
        } else if (tableName.equalsIgnoreCase("temp_retailerprogramtarget")) {
            if (IsDataAvailableInTable("temp_retailerprogramtarget")) {
                sb = new StringBuffer();
                sb.append("update Retailermaster set Rfield4=IFNULL((select trp.Rfield4 from temp_retailerprogramtarget trp where Retailermaster.Retailerid=trp.retailerid),Rfield4)");
                db.executeQ(sb.toString());
                db.deleteSQL("temp_retailerprogramtarget", null, true);
            }
        } else if (tableName.equalsIgnoreCase("temp_indicativeorder")) {
            if (IsDataAvailableInTable("temp_indicativeorder")) {
                sb = new StringBuffer();
                sb.append("insert into indicativeorder(pid,op,oc,oo,rid,uid)");
                sb.append("select  distinct pm.pid,ti1.qty as piece,ti2.qty as cas,ti3.qty as outer,ifnull(ti1.rid,ifnull(ti2.rid,ifnull(ti3.rid,0))) as rid,");
                sb.append("ifnull(ti1.uid,ifnull(ti2.uid,ifnull(ti3.uid,0))) as uid from ProductMaster  pm inner join temp_indicativeorder ti on pm.pid =ti.pid ");
                sb.append(" left join temp_indicativeorder ti1 on pm.pid=ti1.pid and pm.piece_uomid=ti1.uomid and ti.uid=ti1.uid");
                sb.append(" left join temp_indicativeorder ti2 on pm.pid=ti2.pid and pm.dUomid=ti2.uomid and ti.uid=ti2.uid");
                sb.append(" left join temp_indicativeorder ti3 on pm.pid=ti3.pid and pm.douomid=ti3.uomid and ti.uid=ti3.uid");
                db.executeQ(sb.toString());
                db.deleteSQL("temp_indicativeorder", null, true);
            }
        } else if (tableName.equalsIgnoreCase("temp_vanload")) {
            if (IsDataAvailableInTable("temp_vanload")) {
                sb = new StringBuffer();
                sb.append("insert into vanload(uid,pid,pcsqty,caseqty,outerqty,date,duomqty,duomid,douomqty,douomid,batchid,loadNo) ");
                sb.append("select pm.uid,pm.pid,a.qty as pieceqty,b.qty as caseqty,c.qty as outerqty,");
                sb.append("ifnull(a.date,ifnull(b.date,ifnull(c.date,0))) as date,b.uomcount as duomqty,b.uomid as duomid,");
                sb.append("c.uomcount as douomqty,c.uomid as douomid,ifnull(a.batchid,ifnull(b.batchid,ifnull(c.batchid,0))) as batchid,pm.loadRefNo from temp_vanload pm");
                sb.append(" left join temp_vanload as a on pm.pid=a.pid and pm.uid=a.uid and a.uomCode="
                        + bmodel.QT(PIECE_TYPE) + "and a.batchid = pm.batchid");
                sb.append(" left join temp_vanload as b on pm.pid=b.pid and pm.uid =b.uid and b.uomcode="
                        + bmodel.QT(CASE_TYPE) + "and b.batchid = pm.batchid");
                sb.append(" left join temp_vanload as c on pm.pid=c.pid  and pm.uid=c.uid and c.uomcode="
                        + bmodel.QT(OUTER_TYPE) + "and c.batchid = pm.batchid");
                sb.append(" group by pm.uid,pm.pid,a.batchid,b.batchid,c.batchid order by pm.uid,pm.pid ");
                db.executeQ(sb.toString());
                db.deleteSQL("temp_vanload", null, true);
            }
        } else if (tableName.equalsIgnoreCase("temp_product_warehousestockmaster")) {
            if (IsDataAvailableInTable("temp_product_warehousestockmaster")) {
                sb = new StringBuffer();
                sb.append("insert into ProductWareHouseStockMaster(PID,Uomid,Qty,DistributorId) ");
                sb.append("select tw.pid,pm.piece_uomid,tw.wsih,0 from temp_product_warehousestockmaster tw ");
                sb.append(" left join ProductMaster as pm on pm.pid=tw.pid ");
                db.executeQ(sb.toString());
                db.deleteSQL("temp_product_warehousestockmaster", null, true);
                sb = null;
            }
        }
    }

    /**
     * Method to use update Product and retailer related values
     */

    public void updateProductAndRetailerMaster() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
        try {
            db.createDataBase();
            db.openDataBase();

            updateTable("temp_productmaster", db);
            updateTable("temp_pricemaster", db);
            updateTable("temp_vanload", db);

            updateTable("temp_productopeningstock", db);
            updateTable("temp_product_priceMaster", db);
            updateTable("temp_productstandardstockmaster", db);

            updateTable("temp_indicativeorder", db);
            updateTable("temp_retailerprogramtarget", db);
            updateTable("temp_product_warehousestockmaster", db);

        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }


    }

    public void updatetempTablesWithRetailerMaster() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
        try {
            db.createDataBase();
            db.openDataBase();
            updateTable("temp_retailerprogramtarget", db);
        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }
    }

    public void loadErrorCode() {
        mErrorMessageByErrorCode = new HashMap<>();
        mErrorMessageByErrorCode.put("E01", context.getResources().getString(R.string.error_e01));
        mErrorMessageByErrorCode.put("E02", context.getResources().getString(R.string.error_e02));
        mErrorMessageByErrorCode.put("E03", context.getResources().getString(R.string.error_e03));
        mErrorMessageByErrorCode.put("E04", context.getResources().getString(R.string.error_e04));
        mErrorMessageByErrorCode.put("E05", context.getResources().getString(R.string.error_e05));
        mErrorMessageByErrorCode.put("E06", context.getResources().getString(R.string.error_e06));
        mErrorMessageByErrorCode.put("E07", context.getResources().getString(R.string.error_e07));
        mErrorMessageByErrorCode.put("E08", context.getResources().getString(R.string.error_e08));
        mErrorMessageByErrorCode.put("E09", context.getResources().getString(R.string.error_e09));
        mErrorMessageByErrorCode.put("E10", context.getResources().getString(R.string.error_e10));
        mErrorMessageByErrorCode.put("E11", context.getResources().getString(R.string.error_e11));
        mErrorMessageByErrorCode.put("E12", context.getResources().getString(R.string.error_e12));
        mErrorMessageByErrorCode.put("E13", context.getResources().getString(R.string.error_e13));
        mErrorMessageByErrorCode.put("E14", context.getResources().getString(R.string.error_e14));
        mErrorMessageByErrorCode.put("E15", context.getResources().getString(R.string.error_e15));
        mErrorMessageByErrorCode.put("E18", context.getResources().getString(R.string.error_e18));
        mErrorMessageByErrorCode.put("E19", context.getResources().getString(R.string.error_e19));
        mErrorMessageByErrorCode.put("E20", context.getResources().getString(R.string.error_e20));
        mErrorMessageByErrorCode.put("E21", context.getResources().getString(R.string.error_e21));
        mErrorMessageByErrorCode.put("E23", context.getResources().getString(R.string.error_e23));
        mErrorMessageByErrorCode.put("E24", context.getResources().getString(R.string.error_e24));
        mErrorMessageByErrorCode.put("E25", context.getResources().getString(R.string.user_account_locked));
        mErrorMessageByErrorCode.put("E31", context.getResources().getString(R.string.error_e31));
        mErrorMessageByErrorCode.put("E32", context.getResources().getString(R.string.error_e32));
    }

    public HashMap<String, String> getErrormessageByErrorCode() {
        if (mErrorMessageByErrorCode != null) {
            return mErrorMessageByErrorCode;
        }

        return new HashMap<>();
    }

    /**
     * Method to use upload data from mobile. If exception throws it return
     * empty Vector
     *
     * @param headerinfo - construct header information
     * @param data       - construct all table records to string
     * @return
     */

    public Vector<String> getUploadResponse(String headerinfo, String data,
                                            String appendurl) {
        // Update Security key
        updateAuthenticateToken();
        StringBuilder url = new StringBuilder();
        url.append(DataMembers.SERVER_URL);
        url.append(appendurl);
        try {
            MyHttpConnectionNew http = new MyHttpConnectionNew();
            http.create(MyHttpConnectionNew.POST, url.toString(), null);
            http.addHeader(SECURITY_HEADER, mSecurityKey);
            http.addParam("userinfo", headerinfo);
            if (data != null) {
                http.addParam("data", data);
            }
            http.connectMe();
            Vector<String> result = http.getResult();
            if (result == null) {
                return new Vector<>();
            }
            return result;
        } catch (Exception e) {
            Commons.printException("" + e);
            return new Vector<>();
        }

    }

    public static final String USER_IDENTITY = "UserIdentity";

    public Vector<String> getUploadResponseForgotPassword(JSONObject jsonObject,
                                                          String appendurl, boolean isChangePassword) {
        // Update Security key
        // updateAuthenticateToken();
        if (isChangePassword)
            updateAuthenticateToken();
        else
            updateAuthenticateTokenWithoutPassword();

        StringBuffer url = new StringBuffer();
        url.append(DataMembers.SERVER_URL + appendurl);
        try {

            MyHttpConnectionNew http = new MyHttpConnectionNew();
            http.create(MyHttpConnectionNew.POST, url.toString(), null);
            http.addHeader(SECURITY_HEADER, mSecurityKey);
            http.addParam(USER_IDENTITY, RSAEncrypt(jsonObject.toString()));

            http.connectMe();
            Vector<String> result = http.getResult();
            if (result == null) {
                return new Vector<String>();
            }
            return result;
        } catch (Exception e) {
            Commons.printException(e);
            return new Vector<String>();
        }

    }

    public String updateAuthenticateTokenWithoutPassword() {
        mSecurityKey = "";

        try {
            StringBuffer downloadUrl = new StringBuffer();
            downloadUrl.append(DataMembers.SERVER_URL
                    + "/UserMaster/SecureAuthorizeUser");
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("LoginId", bmodel.userNameTemp);
            //jsonObj.put("Password", getPlainPwd());
            jsonObj.put("Platform", "Android");
            jsonObj.put("OSVersion", android.os.Build.VERSION.RELEASE);
            jsonObj.put("FirmWare", "");
            jsonObj.put("Model", Build.MODEL);
            jsonObj.put("VersionCode",
                    bmodel.getApplicationVersionNumber());
            jsonObj.put("DeviceId",
                    bmodel.activationHelper.getIMEINumber());
            jsonObj.put("RegistrationId", bmodel.regid);
            jsonObj.put("MobileDate",
                    Utils.getDate("yyyy/MM/dd HH:mm:ss"));
            jsonObj.put("MobileUTCDateTime",
                    Utils.getGMTDateTime("yyyy/MM/dd HH:mm:ss"));
            //downloadUrl.append(URLEncoder.encode(jsonObj.toString()));
            Commons.print("Update Authentication Token - Forgot password" + jsonObj.toString());
            MyHttpConnectionNew http = new MyHttpConnectionNew();
            http.create(MyHttpConnectionNew.POST, downloadUrl.toString(), null);
            http.addParam(USER_IDENTITY, RSAEncrypt(jsonObj.toString()));
            http.connectMe();
            Map<String, List<String>> headerFields = http.getResponseHeaderField();
            if (headerFields != null) {
                for (Map.Entry<String, List<String>> entry : headerFields.entrySet()) {
                    System.out.println(entry.getKey() + "/" + entry.getValue());
                    if (entry.getKey() != null && entry.getKey().equals(SECURITY_HEADER)) {
                        if (entry.getValue() != null && entry.getValue().size() > 0) {
                            mSecurityKey = entry.getValue().get(0);
                            return mSecurityKey;
                        }
                    }
                }
            }


        } catch (Exception e) {
            Commons.printException(e);
            mSecurityKey = "";

        }
        return mSecurityKey;


    }


    public void updateAuthenticateToken() {

        try {
            mSecurityKey = "";
            String downloadUrl = DataMembers.SERVER_URL + DataMembers.AUTHENTICATE;
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("LoginId", bmodel.userNameTemp);
            jsonObj.put("Password", bmodel.passwordTemp);
            jsonObj.put("Platform", "Android");
            jsonObj.put("OSVersion", android.os.Build.VERSION.RELEASE);
            jsonObj.put("FirmWare", "");
            jsonObj.put("Model", Build.MODEL);
            jsonObj.put("VersionCode",
                    bmodel.getApplicationVersionNumber());
            jsonObj.put("DeviceId",
                    bmodel.activationHelper.getIMEINumber());
            jsonObj.put("RegistrationId", bmodel.regid);
            jsonObj.put("DeviceUniqueId", bmodel.activationHelper.getDeviceId());
            Commons.print("Update Authentication Token " + jsonObj.toString());
            // adding additional two parameters
            addDeviceValidationParameters(false, jsonObj);
            MyHttpConnectionNew http = new MyHttpConnectionNew();
            http.create(MyHttpConnectionNew.POST, downloadUrl, null);
            //http.addHeader(REQUEST_INFO, getHeaderInfo());
            http.addParam(USER_IDENTITY, RSAEncrypt(jsonObj.toString()));//passing encrypted jsonObj
            http.connectMe();
            Map<String, List<String>> headerFields = http.getResponseHeaderField();
            if (headerFields != null) {
                for (Map.Entry<String, List<String>> entry : headerFields.entrySet()) {
                    System.out.println(entry.getKey() + "/" + entry.getValue());
                    if (entry.getKey() != null && entry.getKey().equals(SECURITY_HEADER)) {
                        if (entry.getValue() != null && entry.getValue().size() > 0) {
                            mSecurityKey = entry.getValue().get(0);
                            return;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Commons.printException("" + e);
            mSecurityKey = "";
        }
    }

    public String userAuthenticate(JSONObject jsonObject, boolean isDeviceChanged) {
        if (!bmodel.isOnline()) {
            return "E06";
        }
        mSecurityKey = "";
        String url = DataMembers.SERVER_URL + DataMembers.AUTHENTICATE;
        try {

            addDeviceValidationParameters(isDeviceChanged, jsonObject);

            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            //con.setRequestProperty(REQUEST_INFO, getHeaderInfo());
            // For POST only - START
            con.setDoOutput(true);
            con.connect();

            // For POST only - START
            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter(USER_IDENTITY, RSAEncrypt(jsonObject.toString()));
            String query = builder.build().getEncodedQuery();

            OutputStream os = con.getOutputStream();
            os.write(query.getBytes());
            os.flush();
            os.close();
            // For POST only - END

            Commons.print("User Authentication Token " + jsonObject.toString());
            int responseCode = con.getResponseCode();
            Commons.print("POST Response Code :: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) { //success
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                Commons.print(response.toString());
                // security token key updated
                if (con.getHeaderField(SECURITY_HEADER) != null)
                    mSecurityKey = con.getHeaderField(SECURITY_HEADER);

                return response.toString();
            } else {
                Commons.print("POST request not worked");
            }
        } catch (IOException e) {
            Commons.print("Reading file error");
            return context.getResources().getString(R.string.connection_exception);
        } catch (Exception ex) {
            Commons.printException(ex);
        }
        return "E01";
    }

    public void addDeviceValidationParameters(boolean isDeviceChanged, JSONObject jsonObject) {
        int mDeviceIdValidate, mDeviceIdChange;
        try {

            if (isInternalActivation || !ApplicationConfigs.withActivation) {
                mDeviceIdValidate = 0;
                mDeviceIdChange = 0;
            } else if (isDeviceChanged) {
                mDeviceIdValidate = 0;
                mDeviceIdChange = 1;
            } else {
                mDeviceIdValidate = 1;
                mDeviceIdChange = 0;
            }

            jsonObject.put(SynchronizationHelper.VALIDATE_DEVICE_ID, mDeviceIdValidate);
            jsonObject.put(SynchronizationHelper.UPDATE_DEVICE_ID, mDeviceIdChange);
        } catch (JSONException jsonExpection) {
            Commons.print(jsonExpection.getMessage());
        }
    }

    public String RSAEncrypt(String inputString) {

        Commons.print("Input String : " + inputString);

        StringBuilder sb = new StringBuilder();
        try {

            byte[] bytes = inputString.getBytes();

            PublicKey publicKey = getPublicKey();

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING");

            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            //128 character

            int keySize = 1024 / 8;

            int maxLength = keySize - 42;

            int dataLength = bytes.length;

            int iterationCount = dataLength / maxLength;

            for (int i = 0; i <= iterationCount; i++) {

                int tempLength = (dataLength - maxLength * i > maxLength) ? maxLength : dataLength - maxLength * i;

                byte[] tempBytes = new byte[tempLength];

                System.arraycopy(bytes, maxLength * i, tempBytes, 0, tempLength);

                String s = Base64.encodeToString(cipher.doFinal(tempBytes), Base64.NO_WRAP);

                sb.append(s);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        Commons.print("Encrypted String : " + sb.toString());

        return sb.toString();
    }

    private PublicKey getPublicKey() {
        PublicKey pubKey = null;
        try {
            byte[] modulusBytes = Base64.decode("qt73Blu3iiXTu0yjFoEyX/nOhZnMpgP8pKyUZqHUwCOHifAU+eQsqlr99VZtTNqJitQ742pkcQRdpifunqQzbL5H1AOyJtM4KszVv2Xjx8E/dZojwCxFUA48n9RP05wsPBUYdRm3FrTJdeGZO4DkHGXZ+ou/OJbtJdIdJB+9nQ8=", Base64.DEFAULT);
            byte[] exponentBytes = Base64.decode("AQAB", Base64.DEFAULT);
            BigInteger modulus = new BigInteger(1, modulusBytes);
            BigInteger exponent = new BigInteger(1, exponentBytes);

            RSAPublicKeySpec rsaPubKey = new RSAPublicKeySpec(modulus, exponent);

            KeyFactory fact = KeyFactory.getInstance("RSA");

            pubKey = fact.generatePublic(rsaPubKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pubKey;
    }


    /**
     * Method to use get upload url for the followning code
     *
     * @param code - mention upload seqence or new retailer or normal upload
     * @return
     */
    public String getUploadUrl(String code) {
        String url = "";
        if (!code.equals("")) {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            try {
                db.createDataBase();
                db.openDataBase();
                String query = "select URL from UrlDownloadMaster where typecode="
                        + bmodel.QT(code);
                Cursor c = db.selectSQL(query);
                if (c.getCount() > 0) {
                    if (c.moveToNext()) {
                        url = c.getString(0);
                        c.close();
                        db.closeDB();
                        return url;

                    }
                }
                c.close();

            } catch (Exception e) {
                Commons.printException("" + e);
            } finally {
                db.closeDB();
            }
        }

        return url;
    }

    public void downloadNewRetailerUrl() {

        DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
        try {
            db.createDataBase();
            db.openDataBase();
            String query = "select URL from UrlDownloadMaster where typecode='SYNRET'";
            Cursor c = db.selectSQL(query);
            if (c.getCount() > 0) {
                mNewRetailerDownloadUrlList = new ArrayList<>();
                while (c.moveToNext()) {
                    mNewRetailerDownloadUrlList.add(c.getString(0));

                }
            }
            c.close();

        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }

    }

    public ArrayList<String> getNewRetailerDownloadurlList() {
        if (mNewRetailerDownloadUrlList != null) {
            return mNewRetailerDownloadUrlList;
        }
        return new ArrayList<>();
    }

    public void downloadNewRetailerFromUrl(String retailerid) {
        mJsonObjectResponseByTableName = new HashMap<>();
        mDownloadUrlCount = 0;
        if (mNewRetailerDownloadUrlList != null) {
            try {
                JSONObject json = new JSONObject();
                json.put("UserId", bmodel.userMasterHelper.getUserMasterBO()
                        .getUserid());
                json.put("VersionCode", bmodel.getApplicationVersionNumber());
                json.put("RetailerId", retailerid);
                int size = mNewRetailerDownloadUrlList.size();
                for (String url : mNewRetailerDownloadUrlList) {
                    String downloadUrl = DataMembers.SERVER_URL + url;

                    callVolley(downloadUrl, FROM_SCREEN.NEW_RETAILER, size,
                            NEW_RETAILER_DOWNLOAD_INSERT, json);
                }
            } catch (JSONException e) {
                Commons.printException("" + e);
            }
        }
    }


    public void downloadUserRetailerTranUrl() {

        DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
        try {
            db.createDataBase();
            db.openDataBase();
            String query = "select URL from UrlDownloadMaster where typecode='SYNAU'";
            Cursor c = db.selectSQL(query);
            if (c.getCount() > 0) {
                mUserRetailerTranDownloadUrlList = new ArrayList<>();
                while (c.moveToNext()) {
                    mUserRetailerTranDownloadUrlList.add(c.getString(0));

                }
            }
            c.close();

        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }

    }

    public ArrayList<String> getUserRetailerTranDownloadurlList() {
        if (mUserRetailerTranDownloadUrlList != null) {
            return mUserRetailerTranDownloadUrlList;
        }
        return new ArrayList<>();
    }

    public void downloadUserRetailerTranFromUrl(String retailerid) {

        mJsonObjectResponseByTableName = new HashMap<>();


        mDownloadUrlCount = 0;
        if (mUserRetailerTranDownloadUrlList != null) {
            try {
                JSONObject json = new JSONObject();
                json.put("SupervisorId", bmodel.userMasterHelper.getUserMasterBO()
                        .getUserid());
                json.put("UserId", bmodel.retailerMasterBO.getSelectedUserID());
                json.put("VersionCode", bmodel.getApplicationVersionNumber());
                json.put("RetailerId", retailerid);
                int size = mUserRetailerTranDownloadUrlList.size();
                for (String url : mUserRetailerTranDownloadUrlList) {
                    String downloadUrl = DataMembers.SERVER_URL + url;
                    Commons.print(TAG + ",download url :" + downloadUrl);
                    callVolley(downloadUrl, FROM_SCREEN.VISIT_SCREEN, size,
                            USER_RETAILER_TRAN_DOWNLOAD_INSERT, json);
                }
            } catch (JSONException e) {
                Commons.printException("" + e);
            }
        }
    }

    public boolean checkAlreadySIHAvailable() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
        try {
            db.createDataBase();
            db.openDataBase();
            Cursor c;
            String query = "select count(*) from StockinhandMaster";

            c = db.selectSQL(query);
            if (c != null) {
                if (c.getCount() > 0) {
                    if (c.moveToNext()) {
                        if (c.getInt(0) == 0) {
                            c.close();
                            db.closeDB();
                            return false;
                        }
                    }
                }
            }

            c = db.selectSQL(query);
            if (c != null) {
                if (c.getCount() > 0) {
                    if (c.moveToNext()) {
                        if (c.getInt(0) == 0) {
                            c.close();
                            db.closeDB();
                            return false;
                        }
                    }
                }
            }

        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }
        return true;
    }

    public String getSIHUrl() {
        String appendUrl = "";
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            String query = "select url from UrlDownloadMaster where TypeCode='SYNMAS' and mastername='STOCKINHANDMASTER'";
            Cursor c = db.selectSQL(query);
            if (c != null) {
                if (c.getCount() > 0) {
                    while (c.moveToNext()) {
                        appendUrl = c.getString(0);
                    }
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return appendUrl;
    }


    public void downloadSIH(FROM_SCREEN fromWhere, String appendSihUrl) {
        mJsonObjectResponseByTableName = new HashMap<>();

        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            db.deleteSQL(DataMembers.tbl_SIH, null, true);

            db.closeDB();

            JSONObject json = new JSONObject();
            json.put("UserId", bmodel.userMasterHelper.getUserMasterBO()
                    .getUserid());
            json.put("VersionCode", bmodel.getApplicationVersionNumber());

            callVolley(DataMembers.SERVER_URL + appendSihUrl, fromWhere, 1, SIH_DOWNLOAD, json);
        } catch (Exception e) {
            Commons.printException("" + e);
        }


    }

    public void downloadVanloadFromServer() {
        mJsonObjectResponseByTableName = new HashMap<>();
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
        String downloadurl = "";
        try {
            db.openDataBase();
            db.createDataBase();
            Cursor c = db.selectSQL("select url from urldownloadmaster where mastername='VANLOAD'");
            if (c != null) {
                if (c.getCount() > 0) {
                    while (c.moveToNext()) {
                        downloadurl = c.getString(0);
                    }
                }
            }

            JSONObject json = new JSONObject();
            json.put("UserId", bmodel.userMasterHelper.getUserMasterBO()
                    .getUserid());
            json.put("VersionCode", bmodel.getApplicationVersionNumber());


            downloadurl = DataMembers.SERVER_URL + downloadurl;
            callVolley(downloadurl, FROM_SCREEN.LOAD_MANAGEMENT, 1, VANLOAD_DOWNLOAD, json);
        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }


    }

    public void downloadRetailerByLocFromServer(int id, boolean islocwise) {

        mJsonObjectResponseByTableName = new HashMap<>();

        DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
        String downloadurl = "";
        try {
            db.openDataBase();
            db.createDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("select url from urldownloadmaster where ");

            if (!bmodel.configurationMasterHelper.IS_USER_WISE_RETAILER_DOWNLOAD) {
                sb.append("mastername='LOCATION' and typecode='GETRET'");
            } else {
                sb.append("mastername='USER' and typecode='GETRET'");
            }
            Cursor c = db.selectSQL(sb.toString());
            if (c != null) {
                if (c.getCount() > 0) {
                    while (c.moveToNext()) {

                        downloadurl = c.getString(0);
                    }
                }
            }

            JSONObject json = new JSONObject();
            if (islocwise) {
                json.put("UserId", bmodel.userMasterHelper.getUserMasterBO()
                        .getUserid());
                json.put("VersionCode", bmodel.getApplicationVersionNumber());
                JSONArray jsonArray = new JSONArray();
                jsonArray.put(id);
                json.put("LocationIds", jsonArray);
            } else {
                json.put("LoginUserId", bmodel.userMasterHelper.getUserMasterBO().getUserid());
                json.put("UserId", id);
                json.put("VersionCode", bmodel.getApplicationVersionNumber());
            }


            downloadurl = DataMembers.SERVER_URL + downloadurl;
            callVolley(downloadurl, FROM_SCREEN.RETAILER_SELECTION, 1, RETAILER_DOWNLOAD_BY_LOCATION, json);
        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }


    }

    public void downloadRetailerByRetailerName(String retailerName) {

        mJsonObjectResponseByTableName = new HashMap<>();

        DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
        String downloadurl = "";
        try {
            db.openDataBase();
            db.createDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("select url from urldownloadmaster where ");
            sb.append("mastername='RETAILERMASTER' and typecode='GETRET'");

            Cursor c = db.selectSQL(sb.toString());
            if (c != null) {
                if (c.getCount() > 0) {
                    while (c.moveToNext()) {
                        downloadurl = c.getString(0);
                    }
                }
            }

            JSONObject json = new JSONObject();

            json.put("UserId", bmodel.userMasterHelper.getUserMasterBO()
                    .getUserid());
            json.put("VersionCode", bmodel.getApplicationVersionNumber());
            json.put("Key", retailerName);


            downloadurl = DataMembers.SERVER_URL + downloadurl;
            callVolley(downloadurl, FROM_SCREEN.RETAILER_SELECTION, 1, RETAILER_DOWNLOAD_BY_LOCATION, json);
        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }


    }

    public void downloadFinishUpdate(FROM_SCREEN fromWhere, int updateWhere) {
        mJsonObjectResponseByTableName = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        sb.append(DataMembers.SERVER_URL);
        sb.append("/IncrementalSync/Finish");
        try {
            JSONObject json = new JSONObject();
            json.put("UserId", bmodel.userMasterHelper.getUserMasterBO()
                    .getUserid());
            json.put("VersionCode", bmodel.getApplicationVersionNumber());


            callVolley(sb.toString(), fromWhere, 0, updateWhere, json);
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public void downloadRetailerByLocOrUser(JSONObject jsonObject) {
        mRetailerListByLocOrUserWise = new ArrayList<>();


        try {
            JSONArray fieldList = jsonObject.getJSONArray(SynchronizationHelper.JSON_FIELD_KEY);
            JSONArray dataList = jsonObject.getJSONArray(SynchronizationHelper.JSON_DATA_KEY);
            int retailerIdPos = -1;
            int retailerNamePos = -1;
            for (int i = 0; i < fieldList.length(); i++) {
                if (fieldList.getString(i).equalsIgnoreCase("RetailerID")) {
                    retailerIdPos = i;

                } else if (fieldList.getString(i).equalsIgnoreCase("RetailerName")) {
                    retailerNamePos = i;
                }
            }
            RetailerMasterBO retailerMasterBO;

            for (int i = 0; i < dataList.length(); i++) {
                retailerMasterBO = new RetailerMasterBO();
                JSONArray recordList = (JSONArray) dataList.get(i);
                retailerMasterBO.setRetailerID(recordList.get(retailerIdPos) + "");
                retailerMasterBO.setRetailerName((String) recordList.get(retailerNamePos));
                mRetailerListByLocOrUserWise.add(retailerMasterBO);

            }


        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public void downloadRetailerBeats(JSONObject jsonObject) {
        HashMap<Integer, Integer> retailerBeatMap = new HashMap<>();


        try {
            JSONArray fieldList = jsonObject.getJSONArray(SynchronizationHelper.JSON_FIELD_KEY);
            JSONArray dataList = jsonObject.getJSONArray(SynchronizationHelper.JSON_DATA_KEY);
            int retailerIdPos = -1;
            int beatIdPos = -1;
            for (int i = 0; i < fieldList.length(); i++) {
                if (fieldList.getString(i).equalsIgnoreCase("RetailerId")) {
                    retailerIdPos = i;

                } else if (fieldList.getString(i).equalsIgnoreCase("BeatId")) {
                    beatIdPos = i;
                }
            }

            for (int i = 0; i < dataList.length(); i++) {
                JSONArray recordList = (JSONArray) dataList.get(i);
                retailerBeatMap.put((Integer) recordList.get(retailerIdPos), (Integer) recordList.get(beatIdPos));
            }

            if (retailerBeatMap != null && !retailerBeatMap.isEmpty()) {
                for (int i = 0; i < mRetailerListByLocOrUserWise.size(); i++) {
                    int retailerId = Integer.parseInt(mRetailerListByLocOrUserWise.get(i).getRetailerID());
                    mRetailerListByLocOrUserWise.get(i).setBeatID(retailerBeatMap.get(retailerId) != null ? retailerBeatMap.get(retailerId) : 0);
                }
            }

        } catch (Exception e) {
            Commons.printException("" + e);
        }


    }

    public ArrayList<RetailerMasterBO> getmRetailerListByLocOrUserWise() {
        return mRetailerListByLocOrUserWise;
    }

    /**
     * Fetch UTC date and time from server.
     *
     * @param ur
     * @return 0 - error , 1 - date and time is correct, 2 - Date time miss match.
     */
    public int getUTCDateTimeNew(String ur) {

        String url = DataMembers.SERVER_URL
                + ur;
        Date from, to;
        Date datefrom, dateto, datenow;
        int flag = 0;
        try {

            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");


            int responseCode = con.getResponseCode();
            Commons.print("POST Response Code :: " + responseCode);
            StringBuilder response = new StringBuilder();
            if (responseCode == HttpURLConnection.HTTP_OK) { //success
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        con.getInputStream()));
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();


            } else {
                Commons.print("POST request not worked");
            }


            JSONObject jsonObject = new JSONObject(response.toString());
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH:mm", Locale.getDefault());

            Calendar today = Calendar.getInstance();
            today.setTime(formatter.parse(Utils.getGMTDateTime("yyyy-MM-dd-HH:mm")));

            //subtract 30 min to current time
            today.add(Calendar.MINUTE, -30);
            from = today.getTime();

            //Add 30 min to current time
            today.add(Calendar.MINUTE, 60);
            to = today.getTime();

            datefrom = formatter.parse(formatter.format(from));
            dateto = formatter.parse(formatter.format(to));


            datenow = formatter.parse(jsonObject.getString("UTCDate").replace("T", "-").substring(0, 16));

            if ((datefrom.before(datenow) || datefrom.equals(datenow))
                    && ((dateto.after(datenow) || dateto.equals(datenow))))
                flag = 1;
            else
                flag = 2;

        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return flag;
    }

    private boolean IsDataAvailableInTable(String tablename) {
        DBUtil db;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            String query = "select * from " + tablename;
            Cursor c = db.selectSQL(query);
            if (c.getCount() > 0) {
                if (c.moveToNext())
                    return true;
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return false;

    }

    /**
     * upload starts
     */
    public int uploadUsingHttp(Handler handlerr, final int flag) {
        responceMessage = 0;
        handler = handlerr;
        try {

            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            JSONObject jsonObjData = new JSONObject();
            ;
            if (flag == DataMembers.SYNCUPLOAD || flag == DataMembers.SYNC_EXPORT) {
                Set<String> keys = DataMembers.uploadColumn.keySet();

                jsonObjData = new JSONObject();
                for (String tableName : keys) {
                    JSONArray jsonArray = prepareDataForUploadJSON(db,
                            handlerr, tableName,
                            DataMembers.uploadColumn.get(tableName));

                    if (jsonArray.length() > 0)
                        jsonObjData.put(tableName, jsonArray);
                }
                Commons.print("jsonObjData.toString():0:"
                        + jsonObjData.toString());
            } else if (flag == DataMembers.SYNCUPLOADRETAILERWISE) {
                Set<String> keys = DataMembers.uploadColumnWithOutRetailer
                        .keySet();

                jsonObjData = new JSONObject();
                for (String tableName : keys) {
                    JSONArray jsonArray = prepareDataForUploadJSONWithOutRetailer(
                            db, tableName,
                            DataMembers.uploadColumnWithOutRetailer
                                    .get(tableName));

                    if (jsonArray.length() > 0)
                        jsonObjData.put(tableName, jsonArray);

                }
                keys = DataMembers.uploadColumnWithRetailer.keySet();
                for (String tableName : keys) {
                    JSONArray jsonArray = prepareDataForUploadJSONWithRetailer(
                            db, tableName,
                            DataMembers.uploadColumnWithRetailer.get(tableName));

                    if (jsonArray.length() > 0)
                        jsonObjData.put(tableName, jsonArray);
                }
                Commons.print("jsonObjData.toString():1:"
                        + jsonObjData.toString());
            } else if (flag == 3) {
                Set<String> keys = DataMembers.uploadLocationTrackingColumn
                        .keySet();

                jsonObjData = new JSONObject();
                for (String tableName : keys) {
                    JSONArray jsonArray = prepareDataForUploadJSON(db,
                            handlerr, tableName,
                            DataMembers.uploadLocationTrackingColumn
                                    .get(tableName));

                    if (jsonArray.length() > 0)
                        jsonObjData.put(tableName, jsonArray);
                }
                Commons.print("jsonObjData.toString():3:"
                        + jsonObjData.toString());
            } else if (flag == DataMembers.ATTENDANCE_UPLOAD) {
                Set<String> keys = DataMembers.uploadAttendanceColumn.keySet();

                jsonObjData = new JSONObject();
                for (String tableName : keys) {
                    JSONArray jsonArray = prepareDataForUploadJSON(db,
                            handlerr, tableName,
                            DataMembers.uploadAttendanceColumn.get(tableName));

                    if (jsonArray.length() > 0)
                        jsonObjData.put(tableName, jsonArray);
                }
                Commons.print("jsonObjData.toString():3:"
                        + jsonObjData.toString());
            } else if (flag == DataMembers.SYNCLYTYPTUPLOAD) {
                Set<String> keys = DataMembers.uploadLPTable.keySet();

                jsonObjData = new JSONObject();
                for (String tableName : keys) {
                    JSONArray jsonArray = prepareDataForUploadJSON(db,
                            handlerr, tableName,
                            DataMembers.uploadLPTable.get(tableName));

                    if (jsonArray.length() > 0)
                        jsonObjData.put(tableName, jsonArray);
                }
                Commons.print("jsonObjData.toString():3:"
                        + jsonObjData.toString());
            } else if (flag == DataMembers.SYNCSIHUPLOAD) {
                Set<String> keys = DataMembers.uploadSIHTable.keySet();

                jsonObjData = new JSONObject();
                for (String tableName : keys) {
                    JSONArray jsonArray = prepareDataForUploadJSON(db,
                            handlerr, tableName,
                            DataMembers.uploadSIHTable.get(tableName));

                    if (jsonArray.length() > 0)
                        jsonObjData.put(tableName, jsonArray);
                }
                Commons.print("jsonObjData.toString():3:"
                        + jsonObjData.toString());
            } else if (flag == DataMembers.SYNCSTKAPPLYUPLOAD) {
                Set<String> keys = DataMembers.uploadStockApplyTable.keySet();

                jsonObjData = new JSONObject();
                for (String tableName : keys) {
                    JSONArray jsonArray = prepareDataForUploadJSON(db,
                            handlerr, tableName,
                            DataMembers.uploadStockApplyTable.get(tableName));

                    if (jsonArray.length() > 0)
                        jsonObjData.put(tableName, jsonArray);
                }
                Commons.print("jsonObjData.toString():3:"
                        + jsonObjData.toString());
            } else if (flag == DataMembers.SYNC_REALLOC_UPLOAD) {
                Set<String> keys = DataMembers.uploadReallocTable.keySet();

                jsonObjData = new JSONObject();
                for (String tableName : keys) {
                    JSONArray jsonArray = prepareDataForUploadJSON(db,
                            handlerr, tableName,
                            DataMembers.uploadReallocTable.get(tableName));

                    if (jsonArray.length() > 0)
                        jsonObjData.put(tableName, jsonArray);
                }
                Commons.print("jsonObjData.toString():3:"
                        + jsonObjData.toString());
            } else if (flag == DataMembers.COUNTER_SIH_UPLOAD) {
                Set<String> keys = DataMembers.uploadCounterSIHTable.keySet();

                jsonObjData = new JSONObject();
                for (String tableName : keys) {
                    JSONArray jsonArray = prepareDataForUploadJSON(db,
                            handlerr, tableName,
                            DataMembers.uploadCounterSIHTable.get(tableName));

                    if (jsonArray.length() > 0)
                        jsonObjData.put(tableName, jsonArray);
                }
            } else if (flag == DataMembers.COUNTER_STOCK_APPLY_UPLOAD) {
                Set<String> keys = DataMembers.uploadCSStockApplyTable.keySet();

                jsonObjData = new JSONObject();
                for (String tableName : keys) {
                    JSONArray jsonArray = prepareDataForUploadJSON(db,
                            handlerr, tableName,
                            DataMembers.uploadCSStockApplyTable.get(tableName));

                    if (jsonArray.length() > 0)
                        jsonObjData.put(tableName, jsonArray);
                }
            } else if (flag == DataMembers.CS_REJECTED_VARIANCE_UPLOAD) {
                Set<String> keys = DataMembers.uploadCSRejectedVarianceStatus.keySet();

                jsonObjData = new JSONObject();
                for (String tableName : keys) {
                    JSONArray jsonArray = prepareDataForUploadJSON(db,
                            handlerr, tableName,
                            DataMembers.uploadCSRejectedVarianceStatus.get(tableName));

                    if (jsonArray.length() > 0)
                        jsonObjData.put(tableName, jsonArray);
                }
            }


            // UserId,DistributorId,BranchId,WareHouseId,VanId ,
            // SellerType,DayEnd,loginId,password,deviceId,VersionCode
            JSONFormatter jsonFormatter = new JSONFormatter("HeaderInformation");

            Commons.print("jsonObjData.toString()" + jsonObjData);
            try {
                jsonFormatter.addParameter("UserId", bmodel.userMasterHelper
                        .getUserMasterBO().getUserid());
                jsonFormatter.addParameter("DistributorId", bmodel.userMasterHelper
                        .getUserMasterBO().getDistributorid());
                jsonFormatter.addParameter("BranchId", bmodel.userMasterHelper
                        .getUserMasterBO().getBranchId());
                jsonFormatter.addParameter("LoginId", bmodel.userMasterHelper
                        .getUserMasterBO().getLoginName());
                jsonFormatter.addParameter("DeviceId",
                        bmodel.activationHelper.getIMEINumber());
                jsonFormatter.addParameter("VersionCode",
                        bmodel.getApplicationVersionNumber());
                jsonFormatter.addParameter("OrganisationId", bmodel.userMasterHelper
                        .getUserMasterBO().getOrganizationId());
                if (isDayClosed()) {
                    int varianceDwnDate = SDUtil.compareDate(SDUtil.now(SDUtil.DATE_GLOBAL),
                            bmodel.userMasterHelper.getUserMasterBO().getDownloadDate(),
                            "yyyy/MM/dd");
                    if (varianceDwnDate == 0) {
                        jsonFormatter.addParameter("MobileDate",
                                Utils.getDate("yyyy/MM/dd HH:mm:ss"));
                    }
                    if (varianceDwnDate > 0) {
                        jsonFormatter.addParameter("MobileDate",
                                getLastTransactedDate());
                    }
                } else
                    jsonFormatter.addParameter("MobileDate",
                            Utils.getDate("yyyy/MM/dd HH:mm:ss"));

                jsonFormatter.addParameter("MobileUTCDateTime",
                        Utils.getGMTDateTime("yyyy/MM/dd HH:mm:ss"));
                jsonFormatter.addParameter("DownloadedDataDate",
                        bmodel.userMasterHelper.getUserMasterBO().getDownloadDate());
                jsonFormatter.addParameter("VanId", bmodel.userMasterHelper
                        .getUserMasterBO().getVanId());
                jsonFormatter.addParameter("platform", "Android");
                jsonFormatter.addParameter("osversion",
                        android.os.Build.VERSION.RELEASE);
                jsonFormatter.addParameter("firmware", "");
                jsonFormatter.addParameter("model", Build.MODEL);
                String LastDayClose = "";
                if (isDayClosed()) {
                    LastDayClose = bmodel.userMasterHelper.getUserMasterBO()
                            .getDownloadDate();
                }
                jsonFormatter.addParameter("LastDayClose", LastDayClose);
                jsonFormatter.addParameter("DataValidationKey", generateChecksum(jsonObjData.toString()));
                Commons.print(jsonFormatter.getDataInJson());
            } catch (Exception e) {
                Commons.printException("" + e);
            }
            if (flag == DataMembers.SYNC_EXPORT) {
                File folder;
                File file;
                OutputStream out = null;
                InputStream input = null;
                try {
                    if (isExternalStorageAvailable()) {
                        folder = new File(Environment
                                .getExternalStorageDirectory().getPath()
                                + "/IVYData/");
                        if (!folder.exists()) {
                            folder.mkdirs();
                        }
                        SimpleDateFormat sdf = new SimpleDateFormat(
                                "yyyyMMdd_HHmmss", Locale.ENGLISH);
                        file = new File(folder, "Orderdetails_"
                                + bmodel.userMasterHelper.getUserMasterBO()
                                .getUserCode() + "_"
                                + sdf.format(new Date()) + ".txt");
                        if (file.exists()) {
                            file.delete();
                        }
                        final char END_OF_TEXT = 0x1E;
                        input = new StringInputStream(
                                jsonFormatter.getDataInJson() + END_OF_TEXT
                                        + jsonObjData.toString());
                        byte dataa[] = new byte[input.available()];
                        input.read(dataa);
                        out = new FileOutputStream(file);
                        out.write(dataa);
                        out.flush();
                        out.close();
                        input.close();
                        updateUploadFlag(DataMembers.uploadColumn);
                        out = null;
                        input = null;
                    }
                    return 1;
                } catch (Exception e) {
                    Commons.printException("" + e);
                    if (out != null)
                        out.close();
                    if (input != null)
                        input.close();
                    return 0;
                }

            }
            String url;
            if (flag == DataMembers.SYNCSIHUPLOAD) {
                url = getUploadUrl("UPLDSIH");
                if (url.length() == 0) {
                    responceMessage = 2;
                    return responceMessage;
                }
            } else if (flag == DataMembers.SYNCSTKAPPLYUPLOAD) {
                url = getUploadUrl("UPLDSTOK");
                if (url.length() == 0) {
                    responceMessage = 2;
                    return responceMessage;
                }
            } else if (flag == DataMembers.SYNC_REALLOC_UPLOAD) {
                url = getUploadUrl("UPLDALLOC");
                if (url.length() == 0) {
                    responceMessage = 1;
                    return responceMessage;
                }
            } else if (flag == DataMembers.SYNCLYTYPTUPLOAD) {
                url = getUploadUrl("UPLDLOYALTY");
                if (url.length() == 0) {
                    responceMessage = 2;
                    return responceMessage;
                }
            } else if (flag == DataMembers.COUNTER_STOCK_APPLY_UPLOAD) {
                url = getUploadUrl("UPLDCSSTKRCPT");
                if (url.length() == 0) {
                    responceMessage = 2;
                    return responceMessage;
                }
            } else if (flag == DataMembers.COUNTER_SIH_UPLOAD) {
                url = getUploadUrl("UPLDCSSIH");
                if (url.length() == 0) {
                    responceMessage = 2;
                    return responceMessage;
                }
            } else if (flag == DataMembers.CS_REJECTED_VARIANCE_UPLOAD) {
                url = getUploadUrl("UPLDCSSTKVR");
                if (url.length() == 0) {
                    responceMessage = 2;
                    return responceMessage;
                }
            } else
                url = getUploadUrl("UPLDTRAN");

            Vector<String> responseVector = getUploadResponse(jsonFormatter.getDataInJson(),
                    jsonObjData.toString(), url);

            int response = 0;


            if (responseVector.size() > 0) {


                for (String s : responseVector) {
                    JSONObject jsonObject = new JSONObject(s);

                    Iterator itr = jsonObject.keys();
                    while (itr.hasNext()) {
                        String key = (String) itr.next();
                        if (key.equals("Response")) {
                            response = jsonObject.getInt("Response");

                        } else if (key.equals("ErrorCode")) {
                            String tokenResponse = jsonObject.getString("ErrorCode");
                            if (tokenResponse.equals(SynchronizationHelper.INVALID_TOKEN)
                                    || tokenResponse.equals(SynchronizationHelper.TOKEN_MISSINIG)
                                    || tokenResponse.equals(SynchronizationHelper.EXPIRY_TOKEN_CODE)) {

                                return -1;

                            }

                        }

                    }


                }
            }

            if (response == 1) {

                if (flag == DataMembers.SYNCUPLOADRETAILERWISE) {
                    updateUploadFlagRetailerWise();
                    getRetailerIds().delete(0,
                            getRetailerIds().length());
                    responceMessage = 1;
                } else if (flag == DataMembers.SYNCSIHUPLOAD) {
                    updateUploadFlag(DataMembers.uploadSIHTable);

                    responceMessage = 2;
                } else if (flag == DataMembers.SYNCLYTYPTUPLOAD) {
                    updateUploadFlag(DataMembers.uploadLPTable);

                    responceMessage = 2;
                } else if (flag == DataMembers.SYNCSTKAPPLYUPLOAD) {
                    updateUploadFlag(DataMembers.uploadStockApplyTable);
                    responceMessage = 2;
                } else if (flag == DataMembers.SYNC_REALLOC_UPLOAD) {
                    updateUploadFlag(DataMembers.uploadReallocTable);
                    responceMessage = 1;
                } else if (flag == DataMembers.ATTENDANCE_UPLOAD) {
                    updateUploadFlag(DataMembers.uploadAttendanceColumn);
                    responceMessage = 1;
                } else if (flag == DataMembers.COUNTER_STOCK_APPLY_UPLOAD) {
                    updateUploadFlag(DataMembers.uploadCSStockApplyTable);
                    responceMessage = 2;
                } else if (flag == DataMembers.COUNTER_SIH_UPLOAD) {
                    updateUploadFlag(DataMembers.uploadCounterSIHTable);
                    responceMessage = 2;
                } else if (flag == DataMembers.CS_REJECTED_VARIANCE_UPLOAD) {
                    updateUploadFlag(DataMembers.uploadCSRejectedVarianceStatus);
                    responceMessage = 2;
                } else {
                    updateUploadFlag(DataMembers.uploadColumn);
                    responceMessage = 1;
                }
            } else if (response == 0) {
                if (DataMembers.SYNCUPLOADRETAILERWISE == 1) {
                    getRetailerIds().delete(0,
                            getRetailerIds().length());
                    responceMessage = 0;
                }
            }
            Commons.print("After Responce");
            // Upload Transaction Sequence Table Separate , the above method
            // successfully upload. This Method also doing same work, but server
            // need the data while replicate this data while download instantly.
            if ((bmodel.configurationMasterHelper.SHOW_INVOICE_SEQUENCE_NO || bmodel.configurationMasterHelper.SHOW_COLLECTION_SEQ_NO)
                    && bmodel.orderAndInvoiceHelper.hasTransactionSequence()) {
                if (responceMessage == 1) {
                    responceMessage = uploadInvoiceSequenceNo(this.handler);
                }
            }

            Commons.print(jsonObjData.toString());

            Commons.print("After Responce");
            db.closeDB();
        } catch (JSONException jsonException) {
            Commons.printException("" + jsonException);


        } catch (Exception e) {
            Commons.printException("" + e);
            return 0;
        }
        return responceMessage;
    }

    private JSONArray prepareDataForUploadJSON(DBUtil db, Handler handler,
                                               String tableName, String columns) {
        Message msg;

        JSONArray ohRowsArray = new JSONArray();

        try {
            Cursor cursor;
            String columnArray[] = columns.split(",");
            String sql = "select " + columns + " from " + tableName
                    + " where upload='N'";
            cursor = db.selectSQL(sql);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        JSONObject jsonObjRow = new JSONObject();
                        int count = 0;
                        for (String col : columnArray) {
                            String value = cursor.getString(count);
                            jsonObjRow.put(col, value);
                            count++;
                        }
                        ohRowsArray.put(jsonObjRow);
                    }

                    msg = new Message();
                    msg.obj = tableName + " collected to post";
                    msg.what = DataMembers.NOTIFY_UPDATE;
                    handler.sendMessage(msg);
                }

                cursor.close();

            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return ohRowsArray;
    }

    private JSONArray prepareDataForUploadJSONWithOutRetailer(DBUtil db,
                                                              String tableName, String columns) {
        JSONArray ohRowsArray = new JSONArray();

        try {
            Cursor cursor;
            String columnArray[] = columns.split(",");
            String sql = "select " + columns + " from " + tableName
                    + " where upload='N'";
            cursor = db.selectSQL(sql);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        JSONObject jsonObjRow = new JSONObject();
                        int count = 0;
                        for (String col : columnArray) {
                            String value = cursor.getString(count);
                            jsonObjRow.put(col, value);
                            count++;
                        }
                        ohRowsArray.put(jsonObjRow);
                    }
                }

                cursor.close();

            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return ohRowsArray;
    }

    private JSONArray prepareDataForUploadJSONWithRetailer(DBUtil db,
                                                           String tableName, String columns) {
        Message msg;

        JSONArray ohRowsArray = new JSONArray();

        try {
            Cursor cursor;
            String columnArray[] = columns.split(",");
            String sql = "select " + columns + " from " + tableName
                    + " where upload='N' and RetailerID in ("
                    + getRetailerIds().toString() + ")";
            cursor = db.selectSQL(sql);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        JSONObject jsonObjRow = new JSONObject();
                        int count = 0;
                        for (String col : columnArray) {
                            String value = cursor.getString(count);
                            jsonObjRow.put(col, value);
                            count++;
                        }
                        ohRowsArray.put(jsonObjRow);
                    }

                    msg = new Message();
                    msg.obj = tableName + " collected to post";
                    msg.what = DataMembers.NOTIFY_UPDATE;
                    handler.sendMessage(msg);
                }

                cursor.close();

            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return ohRowsArray;
    }


    private void updateUploadFlagRetailerWise() {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Set<String> keys = DataMembers.uploadColumnWithOutRetailer.keySet();
            for (String tableName : keys) {
                String query = "update " + tableName
                        + " set upload='Y' where upload='N'";
                db.updateSQL(query);
            }
            keys = DataMembers.uploadColumnWithRetailer.keySet();
            for (String tableName : keys) {
                String query = "update "
                        + tableName
                        + " set upload='Y' where upload='N' and RetailerID in ("
                        + getRetailerIds().toString()
                        + ")";
                db.updateSQL(query);
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    /**
     * Method to set upload column to Y for corresponding table list
     *
     * @param updateTableMap
     */
    private void updateUploadFlag(HashMap<String, String> updateTableMap) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Set<String> keys = updateTableMap.keySet();
            Commons.print(keys.size() + "size");
            for (String tableName : keys) {
                String query = "update " + tableName
                        + " set upload='Y' where upload='N'";
                db.updateSQL(query);
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }


    /**
     * Upload Transaction Sequence Table after Data Upload through seperate
     * method name Returns the response Success/Failure
     *
     * @param handler
     * @return
     */
    private int uploadInvoiceSequenceNo(Handler handler) {

        responceMessage = 0;
        JSONObject jsonObjData;
        this.handler = handler;
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Set<String> keys = DataMembers.uploadInvoiceSequenceNo.keySet();
            jsonObjData = new JSONObject();
            for (String tableName : keys) {
                JSONArray jsonArray = prepareDataForUploadJSON(db, handler,
                        tableName,
                        DataMembers.uploadInvoiceSequenceNo.get(tableName));

                if (jsonArray.length() > 0)
                    jsonObjData.put(tableName, jsonArray);
            }

            Commons.print("uploadInvoiceSequenceNo:" + jsonObjData.toString());

            JSONFormatter jsonFormatter = new JSONFormatter("HeaderInformation");

            try {
                jsonFormatter.addParameter("UserId", bmodel.userMasterHelper
                        .getUserMasterBO().getUserid());
                jsonFormatter.addParameter("DistributorId", bmodel.userMasterHelper
                        .getUserMasterBO().getDistributorid());
                jsonFormatter.addParameter("BranchId", bmodel.userMasterHelper
                        .getUserMasterBO().getBranchId());
                jsonFormatter.addParameter("LoginId", bmodel.userMasterHelper
                        .getUserMasterBO().getLoginName());
                jsonFormatter.addParameter("DeviceId",
                        bmodel.activationHelper.getIMEINumber());
                jsonFormatter.addParameter("VersionCode",
                        bmodel.getApplicationVersionNumber());
                jsonFormatter.addParameter("OrganisationId", bmodel.userMasterHelper
                        .getUserMasterBO().getOrganizationId());
                jsonFormatter.addParameter("MobileDate",
                        Utils.getDate("yyyy/MM/dd HH:mm:ss"));
                jsonFormatter.addParameter("MobileUTCDateTime",
                        Utils.getGMTDateTime("yyyy/MM/dd HH:mm:ss"));
                jsonFormatter.addParameter("DownloadedDataDate",
                        bmodel.userMasterHelper.getUserMasterBO().getDownloadDate());
                jsonFormatter.addParameter("VanId", bmodel.userMasterHelper
                        .getUserMasterBO().getVanId());
                String LastDayClose = "";
                if (isDayClosed()) {
                    LastDayClose = bmodel.userMasterHelper.getUserMasterBO()
                            .getDownloadDate();
                }
                jsonFormatter.addParameter("LastDayClose", LastDayClose);
                jsonFormatter.addParameter("DataValidationKey", generateChecksum(jsonObjData.toString()));
                Commons.print(jsonFormatter.getDataInJson());
            } catch (Exception e) {
                Commons.printException("" + e);
            }

            String url = getUploadUrl("UPLDSEQ");
            if (url.length() == 0)
                return 1;
            Vector<String> responseVector = getUploadResponse(jsonFormatter.getDataInJson(),

                    jsonObjData.toString(), url);

            int response = 0;


            if (responseVector.size() > 0) {


                for (String s : responseVector) {
                    JSONObject jsonObject = new JSONObject(s);

                    Iterator itr = jsonObject.keys();
                    while (itr.hasNext()) {
                        String key = (String) itr.next();
                        if (key.equals("Response")) {
                            response = jsonObject.getInt("Response");

                        } else if (key.equals("ErrorCode")) {
                            String tokenResponse = jsonObject.getString("ErrorCode");
                            if (tokenResponse.equals(SynchronizationHelper.INVALID_TOKEN)
                                    || tokenResponse.equals(SynchronizationHelper.TOKEN_MISSINIG)
                                    || tokenResponse.equals(SynchronizationHelper.EXPIRY_TOKEN_CODE)) {

                                return -1;

                            }

                        }

                    }


                }
            }
            if (response == 1) {

                db.updateSQL("Update " + DataMembers.tbl_TransactionSequence
                        + " SET UPLOAD = 'Y' WHERE UPLOAD = 'N'");
                db.closeDB();
                responceMessage = 1;

            } else if (response == 0) {
                responceMessage = 0;
            }
            db.closeDB();
            Commons.print("uploadInvoiceSequenceNo:After Responce");
        } catch (Exception e) {
            Commons.printException("" + e);
            return 0;
        }
        return responceMessage;
    }


    /**
     * Upload Ends
     */


    public void downloadCustomerSearch(String phoneNum) {

        String url = "";
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
        StringBuffer sb;
        mJsonObjectResponseByTableName = new HashMap<>();
        try {
            db.createDataBase();
            db.openDataBase();
            String query = "select URL from UrlDownloadMaster where typecode='CUST_SEARCH_RT'";
            Cursor c = db.selectSQL(query);
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    url = c.getString(0);
                }
            }
            c.close();

            sb = new StringBuffer();
            sb.append(DataMembers.SERVER_URL);
            sb.append(url);

            JSONObject json = new JSONObject();
            json.put("UserId", bmodel.userMasterHelper.getUserMasterBO()
                    .getUserid());
            json.put("VersionCode", bmodel.getApplicationVersionNumber());
            json.put("PhoneNo", phoneNum);

            callVolley(sb.toString(), FROM_SCREEN.COUNTER_SALES_SELECTION, 1, VOLLEY_CUSTOMER_SEARCH, json);

        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }

    }

    public void downloadAbsenteesRetailer(ArrayList<TeamLeadBO> absenteesList) {
        mJsonObjectResponseByTableName = new HashMap<>();
        String appendurl = getUploadUrl("PLANNED_STORE");
        String sb = DataMembers.SERVER_URL + appendurl;
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        try {
            jsonObject.put("UserId", bmodel.userMasterHelper.getUserMasterBO()
                    .getUserid());
            jsonObject.put("VersionCode", bmodel.getApplicationVersionNumber());
            for (TeamLeadBO teamLeadBO : absenteesList) {
                if (teamLeadBO.getAttendance() == 0) {


                    jsonArray.put(teamLeadBO.getUserID());

                }
            }
            jsonObject.put("UserIds", jsonArray);
        } catch (JSONException e) {
            Commons.printException("" + e);
        }


        callVolley(sb, FROM_SCREEN.TL_ALLOCATION, 1, VOLLEY_TL_ABSENTEES_RETAILER_DOWNLOAD, jsonObject);

    }

    public String downloadSOVisitPlanToken(String url) {
        try {
            String token = "";
            StringBuilder downloadUrl = new StringBuilder();
            downloadUrl.append(url);
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("loginId", bmodel.userMasterHelper.getUserMasterBO().getLoginName());
            jsonObj.put("password", bmodel.passwordTemp);
            jsonObj.put("organisationId", bmodel.userMasterHelper
                    .getUserMasterBO().getOrganizationId());
            Commons.print("Url " + downloadUrl.toString());
            MyHttpConnectionNew http = new MyHttpConnectionNew();
            http.create(MyHttpConnectionNew.POST, downloadUrl.toString(), null);
            http.addParam(USER_IDENTITY, RSAEncrypt(jsonObj.toString()));
            http.setIsFromWebActivity(true);
            http.connectMe();
            Map<String, List<String>> headerFields = http.getResponseHeaderField();
            if (headerFields != null) {
                for (Map.Entry<String, List<String>> entry : headerFields.entrySet()) {
                    System.out.println(entry.getKey() + "/" + entry.getValue());
                    if (entry.getKey() != null && entry.getKey().equals(SECURITY_HEADER)) {
                        if (entry.getValue() != null && entry.getValue().size() > 0) {
                            token = entry.getValue().get(0);
                            return token;
                        }
                    }
                }
            }
            return token;
        } catch (Exception e) {
            Commons.printException("" + e);
            return "";
        }

    }

    public String downloadSessionId(String url) {
        updateAuthenticateToken();
        String sessionId = "";
        try {

            MyHttpConnectionNew http = new MyHttpConnectionNew();
            http.create(MyHttpConnectionNew.POST, url, null);
            http.addHeader(SECURITY_HEADER, mSecurityKey);
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("UserId", bmodel.userMasterHelper.getUserMasterBO()
                    .getUserid());
            jsonObj.put("LoginId", bmodel.userMasterHelper.getUserMasterBO().getLoginName());
            jsonObj.put("VersionCode", bmodel.getApplicationVersionNumber());
            http.setParamsJsonObject(jsonObj);

            http.connectMe();
            Vector<String> result = http.getResult();

            if (!result.isEmpty()) {
                for (String s : result) {
                    JSONObject jsonObject = new JSONObject(s);
                    Iterator itr = jsonObject.keys();
                    while (itr.hasNext()) {
                        String key = (String) itr.next();
                        if (key.equals("Data")) {
                            sessionId = jsonObject.getJSONArray("Data").get(0).toString();
                            sessionId = sessionId.replaceAll("[\\[\\],\"]", "");
                        }
                    }
                }

            }
            return sessionId;
        } catch (Exception e) {
            Commons.printException(e);
            return sessionId;

        }


    }

    /**
     * Method to update last visit price table
     * from transaction table
     */
    private void updateLastVisitPrice() {

        DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
        try {
            db.createDataBase();
            db.openDataBase();

            String sql = "select pid,price,mrp,PH.retailerid,uomid,own from PriceCheckDetail PD"
                    + " INNER JOIN PriceCheckHeader PH ON PD.tid=PH.tid where date=" + bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL));
            Cursor cur = db.selectSQL(sql);
            if (cur != null) {
                while (cur.moveToNext()) {
                    sql = "Select pid from LastVisitPrice where pid=" + cur.getString(0) + " and rid=" + cur.getString(3);
                    Cursor cur1 = db.selectSQL(sql);
                    if (cur1 != null && cur1.getCount() > 0) {
                        db.updateSQL("update LastVisitPrice set price=" + cur.getString(1) + ",mrp=" + cur.getString(2) + " where pid=" + cur.getString(0) + " and rid=" + cur.getString(3));
                        cur1.close();
                    } else {
                        db.insertSQL("LastVisitPrice", "rid,pid,uomid,price,mrp,isown", cur.getString(3) + "," + cur.getString(0) + "," + cur.getString(4) + "," + cur.getString(1) + "," + cur.getString(2) + "," + cur.getString(5));
                    }
                }
                cur.close();
            }
            db.closeDB();
        } catch (Exception ex) {
            Commons.printException("" + ex);
            db.closeDB();
        }
    }

    public void updateLastVisitStock() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
        try {
            db.createDataBase();
            db.openDataBase();

            String sql = "select SH.retailerid,productId,shelfpqty,shelfcqty,shelfoqty,whpqty,whcqty,whoqty,LocId,isDistributed,isListed,reasonID,IsOwn,Facing"
                    + " from ClosingStockDetail SD INNER JOIN ClosingStockHeader SH ON SD.stockId=SH.stockId where SH.date=" + bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL));
            Cursor cur = db.selectSQL(sql);
            if (cur != null) {
                while (cur.moveToNext()) {
                    sql = "Select productid from LastVisitStock where productid=" + cur.getString(1) + " and retailerid=" + cur.getString(0) + " and LocId=" + cur.getString(8);
                    Cursor cur1 = db.selectSQL(sql);
                    if (cur1 != null && cur1.getCount() > 0) {
                        db.updateSQL("update LastVisitStock set shelfpqty=" + cur.getString(2) + ",shelfcqty=" + cur.getString(3) + ",shelfoqty=" + cur.getString(4)
                                + ",whpqty=" + cur.getString(5) + ",whcqty=" + cur.getString(6) + ",whoqty=" + cur.getString(7) + ",LocId=" + cur.getString(8)
                                + ",isDistributed=" + cur.getString(9) + ",isListed=" + cur.getString(10) + ",reasonID=" + cur.getString(11)
                                + ",IsOwn=" + cur.getString(12) + ",Facing=" + cur.getString(13)

                                + " where productid=" + cur.getString(1) + " and retailerid=" + cur.getString(0) + " and LocId=" + cur.getString(8));
                        cur1.close();
                    } else {
                        db.insertSQL("LastVisitStock", "retailerid,productId,shelfpqty,shelfcqty,shelfoqty,whpqty,whcqty,whoqty,LocId,isDistributed,isListed,reasonID,IsOwn,facing", cur.getString(1) + "," + cur.getString(0) + "," + cur.getString(2) + "," + cur.getString(3) + "," + cur.getString(4) + "," + cur.getString(5)
                                + "," + cur.getString(6) + "," + cur.getString(7) + "," + cur.getString(8) + "," + cur.getString(9) + "," + cur.getString(10)
                                + "," + cur.getString(11) + "," + cur.getString(12) + "," + cur.getString(13));

                    }
                }
                cur.close();
            }


            db.closeDB();
        } catch (Exception ex) {
            Commons.printException("" + ex);
            db.closeDB();
        }
    }

    private void updateLastVisitNearExpiry() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
        try {
            db.createDataBase();
            db.openDataBase();

            String sql = "select NH.retailerId,pid,locId,expDate,uomId,uomQty,isOwn from NearExpiry_Tracking_Detail ND INNER JOIN NearExpiry_Tracking_Header NH ON ND.tid=NH.tid where NH.date=" + bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL));
            Cursor cur = db.selectSQL(sql);
            if (cur != null) {
                while (cur.moveToNext()) {
                    sql = "Select productId from LastVisitNearExpiry where productId=" + cur.getString(1) + " and retailerid=" + cur.getString(0) + " and locid=" + cur.getString(2) + " and uomid=" + cur.getString(4) + " and expDate=" + bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL));
                    Cursor cur1 = db.selectSQL(sql);
                    if (cur1 != null && cur1.getCount() > 0) {
                        db.updateSQL("update LastVisitNearExpiry set expDate='" + cur.getString(3) + "',uomId=" + cur.getString(4) + ",Qty=" + cur.getString(5) + " where productId=" + cur.getString(1) + " and retailerid=" + cur.getString(0));
                        cur1.close();
                    } else {
                        db.insertSQL("LastVisitNearExpiry", "retailerId,productId,locId,uomid,qty,expDate,isown", cur.getString(0) + "," + cur.getString(1) + "," + cur.getString(2) + "," + cur.getString(4) + "," + cur.getString(5) + ",'" + cur.getString(3) + "'," + cur.getString(6));

                    }
                }
                cur.close();
            }

            db.closeDB();
        } catch (Exception ex) {
            Commons.printException("" + ex);
            db.closeDB();
        }
    }

    public void updateLastVisitPromotion() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
        try {
            db.createDataBase();
            db.openDataBase();

            String sql = "select PH.retailerId,locId,promotionId,isExecuted,promoQty,ReasonId,ExecRatingLovId,Flag,BrandId from PromotionDetail PD INNER JOIN PromotionHeader PH ON PD.uid=PH.uid where PH.date=" + bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + " and PD.flag='S'";
            Cursor cur = db.selectSQL(sql);
            if (cur != null) {
                while (cur.moveToNext()) {
                    sql = "Select promotionId from LastVisitPromotion where retailerid=" + cur.getString(0) + " and locid=" + cur.getString(1) + " and promotionId=" + cur.getString(2) + " and brandId=" + cur.getString(8);
                    Cursor cur1 = db.selectSQL(sql);
                    if (cur1 != null && cur1.getCount() > 0) {
                        db.updateSQL("update LastVisitPromotion set isExecuted=" + cur.getString(3) + ",promoQty=" + cur.getString(4) + ",ReasonId=" + cur.getString(5) + ",ExecRatingLovId=" + cur.getString(6) + ",Flag='" + cur.getString(7) + "' where retailerid=" + cur.getString(0) + " and locid=" + cur.getString(1) + " and promotionId=" + cur.getString(2) + " and brandId=" + cur.getString(8));
                        cur1.close();
                    } else {
                        db.insertSQL("LastVisitPromotion", "retailerId,locId,promotionId,isExecuted,promoQty,ReasonId,ExecRatingLovId,Flag,BrandId", cur.getString(0) + "," + cur.getString(1) + "," + cur.getString(2) + "," + cur.getString(3) + "," + cur.getString(4) + "," + cur.getString(5) + "," + cur.getString(6) + ",'" + cur.getString(7) + "'," + cur.getString(8));

                    }
                }
                cur.close();
            }

            db.closeDB();
        } catch (Exception ex) {
            Commons.printException("" + ex);
            db.closeDB();
        }
    }

    private void updateLastVisitSurvey() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
        try {
            db.createDataBase();
            db.openDataBase();

            String sql = "select AH.retailerId,AH.surveyId,qid,answerId,answer,score,isExcluded from AnswerDetail AD INNER JOIN AnswerHeader AH ON AD.uid=AH.uid where AH.date=" + bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL));
            Cursor cur = db.selectSQL(sql);
            if (cur != null) {
                while (cur.moveToNext()) {
                    sql = "Select surveyId from LastVisitSurvey where retailerid=" + cur.getString(0) + " and surveyId=" + cur.getString(1) + " and qid=" + cur.getString(2);
                    Cursor cur1 = db.selectSQL(sql);
                    if (cur1 != null && cur1.getCount() > 0) {
                        db.updateSQL("update LastVisitSurvey set answerId=" + cur.getString(3) + ",answer=" + bmodel.QT(cur.getString(4)) + ",score=" + cur.getString(5) + ",isExcluded=" + cur.getString(6) + " where retailerid=" + cur.getString(0) + " and surveyId=" + cur.getString(1) + " and qid=" + cur.getString(2));
                        cur1.close();
                    } else {
                        db.insertSQL("LastVisitSurvey", "retailerId,surveyId,qid,answerId,answer,score,isExcluded", cur.getString(0) + "," + cur.getString(1) + "," + cur.getString(2) + "," + cur.getString(3) + ",'" + cur.getString(4) + "'," + cur.getString(5) + "," + cur.getString(6));

                    }
                }
                cur.close();
            }

            db.closeDB();
        } catch (Exception ex) {
            Commons.printException("" + ex);
            db.closeDB();
        }
    }


    public int getTotalRetailersCount() {
        DBUtil db;
        int count = 0;
        try {

            db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            String query = "select distinct count(retailerid) from retailermaster";
            Cursor c = db.selectSQL(query);
            if (c.getCount() > 0) {
                if (c.moveToFirst()) {
                    count = c.getInt(0);
                }
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return count;
    }

    public ArrayList<RetailerMasterBO> getRetailerIdsForDownloadTranSactionData(int value) {

        ArrayList<RetailerMasterBO> retailerIdList = new ArrayList<>();

        DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);

        try {
            db.createDataBase();
            db.openDataBase();
            value = value * LAST_VISIT_TRAN_SPLIT_RETAILER_COUNT;
            Cursor c = db.selectSQL("select distinct retailerid from retailermaster limit " + value + "," + LAST_VISIT_TRAN_SPLIT_RETAILER_COUNT);
            if (c.getCount() > 0) {
                RetailerMasterBO retailerMasterBO;
                while (c.moveToNext()) {
                    retailerMasterBO = new RetailerMasterBO();
                    retailerMasterBO.setRetailerID(c.getString(0));
                    retailerIdList.add(retailerMasterBO);
                }
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }
        return retailerIdList;


    }

    /*public String generateChecksum(String input)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        byte[] buffer = input.toString().getBytes("UTF-8");
        String hexStr = "";
        long checksum = 0;
        for (byte bytes : buffer) {
            checksum += bytes;
        }
        hexStr = Long.toHexString(checksum & 0xff);
        return hexStr;
    }*/

    public String generateChecksum(String input) {
        MessageDigest digest = null;
        String hash = "";
        try {
            digest = MessageDigest.getInstance("SHA-256");
            digest.update(input.getBytes());
            hash = bytesToHexString(digest.digest());
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }
        return hash;
    }

    private static String bytesToHexString(byte[] bytes) {
        // http://stackoverflow.com/questions/332079
        StringBuffer sb = new StringBuffer();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(0xFF & aByte);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public String uploadNewOutlet(Handler handler) {
        String rid = "";
        try {
            this.handler = handler;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            JSONObject jsonobj = new JSONObject();

            Set<String> keys = DataMembers.uploadNewRetailerColumn.keySet();

            for (String tableName : keys) {
                JSONArray jsonArray = prepareDataForUploadJSON(db,
                        handler, tableName,
                        DataMembers.uploadNewRetailerColumn.get(tableName));

                if (jsonArray.length() > 0)
                    jsonobj.put(tableName, jsonArray);
            }

            Commons.print("jsonObjData.toString():0:" + jsonobj.toString());


            JSONFormatter jsonFormatter = new JSONFormatter("HeaderInformation");

            jsonFormatter.addParameter("DeviceId",
                    bmodel.activationHelper.getIMEINumber());
            jsonFormatter.addParameter("LoginId", bmodel.userMasterHelper
                    .getUserMasterBO().getLoginName());
            jsonFormatter.addParameter("VersionCode",
                    bmodel.getApplicationVersionNumber());
            jsonFormatter.addParameter("DistributorId", bmodel.userMasterHelper
                    .getUserMasterBO().getDistributorid());
            jsonFormatter.addParameter("OrganisationId", bmodel.userMasterHelper
                    .getUserMasterBO().getOrganizationId());
            jsonFormatter.addParameter("MobileDate",
                    Utils.getDate("yyyy/MM/dd HH:mm:ss"));
            jsonFormatter.addParameter("MobileUTCDateTime",
                    Utils.getGMTDateTime("yyyy/MM/dd HH:mm:ss"));
            jsonFormatter.addParameter("UserId", bmodel.userMasterHelper
                    .getUserMasterBO().getUserid());
            jsonFormatter.addParameter("VanId", bmodel.userMasterHelper
                    .getUserMasterBO().getVanId());
            String LastDayClose = "";
            if (isDayClosed()) {
                LastDayClose = bmodel.userMasterHelper.getUserMasterBO()
                        .getDownloadDate();
            }
            jsonFormatter.addParameter("LastDayClose", LastDayClose);
            jsonFormatter.addParameter("BranchId", bmodel.userMasterHelper
                    .getUserMasterBO().getBranchId());
            jsonFormatter.addParameter("DownloadedDataDate", bmodel.userMasterHelper
                    .getUserMasterBO().getDownloadDate());
            jsonFormatter.addParameter("DataValidationKey", generateChecksum(jsonobj.toString()));
            Commons.print(jsonFormatter.getDataInJson());
            String appendurl = getUploadUrl("UPLDRET");
            if (appendurl.length() == 0)
                return 2 + "";
            Vector<String> responseVector = bmodel.synchronizationHelper
                    .getUploadResponse(jsonFormatter.getDataInJson(),
                            jsonobj.toString(), appendurl);

            if (responseVector.size() > 0) {


                for (String s : responseVector) {
                    JSONObject jsonObject = new JSONObject(s);

                    Iterator itr = jsonObject.keys();
                    while (itr.hasNext()) {
                        String key = (String) itr.next();
                        if (key.equals("RetailerId")) {
                            rid = jsonObject.getString("RetailerId");
                        } else if (key.equals("ErrorCode")) {
                            String tokenResponse = jsonObject.getString("ErrorCode");
                            if (tokenResponse.equals(SynchronizationHelper.INVALID_TOKEN)
                                    || tokenResponse.equals(SynchronizationHelper.TOKEN_MISSINIG)
                                    || tokenResponse.equals(SynchronizationHelper.EXPIRY_TOKEN_CODE)) {

                                return -1 + "";

                            }

                        }

                    }


                }
            }
        } catch (SQLException | JSONException e) {
            Commons.printException("" + e);
        }
        return rid;
    }

    public boolean validateUser(String username, String password) {
        boolean isUser = username.equalsIgnoreCase(bmodel.userMasterHelper.getUserMasterBO().getLoginName());
        boolean isPwd;
        if (LoginHelper.getInstance(context).IS_PASSWORD_ENCRYPTED) {
            if (passwordType.equalsIgnoreCase(SPF_PSWD_ENCRYPT_TYPE_MD5))
                isPwd = encryptPassword(password).equalsIgnoreCase(bmodel.userMasterHelper.getUserMasterBO().getPassword());
            else
                isPwd = BCrypt.checkpw(password, bmodel.userMasterHelper.getUserMasterBO().getPassword());
        } else {
            isPwd = password.equals(bmodel.userMasterHelper.getUserMasterBO().getPassword());
        }
        return (isUser && isPwd);
    }

    public boolean validateJointCallUser(int userId, String username, String password) {
        ArrayList<UserMasterBO> mjoinCallUserList = bmodel.userMasterHelper.getUserMasterBO()
                .getJoinCallUserList();
        UserMasterBO jointCallUser = new UserMasterBO();
        for (UserMasterBO user : mjoinCallUserList) {
            if (userId == user.getUserid())
                jointCallUser = user;
        }

        boolean isUser = username.equalsIgnoreCase(jointCallUser.getLoginName());
        boolean isPwd;
        if (LoginHelper.getInstance(context).IS_PASSWORD_ENCRYPTED) {
            if (passwordType.equalsIgnoreCase(SPF_PSWD_ENCRYPT_TYPE_MD5))
                isPwd = encryptPassword(password).equalsIgnoreCase(jointCallUser.getPassword());
            else
                isPwd = BCrypt.checkpw(password, jointCallUser.getPassword());
        } else {
            isPwd = password.equals(jointCallUser.getPassword());
        }

        return (isUser && isPwd);
    }

    public String encryptPassword(String pwd) {
        if (passwordType.equalsIgnoreCase(SPF_PSWD_ENCRYPT_TYPE_MD5))
            return SDUtil.convertIntoMD5hashAndBase64(pwd);
        else
            return BCrypt.hashpw(pwd, BCrypt.gensalt());
    }

    public void setEncryptType() {
        try {
            String type = SPF_PSWD_ENCRYPT_TYPE_MD5;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Cursor c = db.selectSQL("select PwdEncryptType from AppVariables");
            if (c.getCount() > 0) {
                if (c.moveToNext()) {
                    type = c.getString(0);
                    if (type.equals("")) {
                        type = SPF_PSWD_ENCRYPT_TYPE_MD5;
                    }
                }
                c.close();
            }
            db.closeDB();

            passwordType = type;
        } catch (SQLException e) {
            Commons.printException("" + e);
        }
    }

    public String sendPostMethod(String url, JSONObject jsonObject) {
        if (!bmodel.isOnline()) {
            return "E06";
        }

        url = DataMembers.SERVER_URL + url;
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setRequestProperty(SECURITY_HEADER, mSecurityKey);
            // For POST only - START
            con.setDoOutput(true);
            OutputStream os = con.getOutputStream();
            os.write(jsonObject.toString().getBytes("UTF-8"));
            os.flush();
            os.close();
            // For POST only - END

            int responseCode = con.getResponseCode();
            Commons.print("POST Response Code :: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) { //success
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                Commons.print(response.toString());
                // security token key updated
                if (con.getHeaderField(SECURITY_HEADER) != null)
                    mSecurityKey = con.getHeaderField(SECURITY_HEADER);

                return response.toString();
            } else {
                Commons.print("POST request not worked");
            }
        } catch (IOException e) {
            Commons.print("Reading file error");
            return context.getResources().getString(R.string.connection_exception);
        }
        return "E01";
    }

    public void loadMethodsNew() {
        setmJsonObjectResponseBytableName(null);

        // If usermaster get updated
        bmodel.userMasterHelper.downloadUserDetails();
        bmodel.userMasterHelper.downloadDistributionDetails();
        // Common Configuration download
        bmodel.configurationMasterHelper.downloadConfig();
        // Preseller or Van Seller Configuration Download
        bmodel.configurationMasterHelper.downloadIndicativeOrderConfig();
        bmodel.configurationMasterHelper.downloadQDVP3ScoreConfig(StandardListMasterConstants.VISITCONFIG_COVERAGE);

        //download retailer row view configution in Visit or planning screen
        bmodel.mRetailerHelper.setVisitPlanning(bmodel.configurationMasterHelper
                .downloadVisitFragDatas(StandardListMasterConstants.VISITCONFIG_PLANNING));
        bmodel.mRetailerHelper.setVisitCoverage(bmodel.configurationMasterHelper
                .downloadVisitFragDatas(StandardListMasterConstants.VISITCONFIG_COVERAGE));

        bmodel.configurationMasterHelper.getPrinterConfig();

        if (bmodel.configurationMasterHelper.SHOW_PREV_ORDER_REPORT) {
            backUpPreviousDayOrder();
            deleteOrderHistory();
        }
        if (bmodel.configurationMasterHelper.IS_DELETE_TABLE) {
            updateOrderStatus();
        }

        if (bmodel.configurationMasterHelper.IS_TEAMLEAD) {
            bmodel.downloadRetailerwiseMerchandiser();
        }

        bmodel.updateRetailerMasterBySBDAcheived(false);
        bmodel.updateRetailerMasterBySBDMerchAcheived(false);
        bmodel.UpdateRetailermasterIsGoldStore();

        bmodel.configurationMasterHelper.downloadRetailerProperty();
        bmodel.downloadRetailerMaster();

        if (bmodel.configurationMasterHelper.CALC_QDVP3)
            bmodel.updateSurveyScoreHistoryRetailerWise();

        // Update Initiative coverage Table
        if (bmodel.configurationMasterHelper.IS_INITIATIVE
                && !bmodel.configurationMasterHelper.SHOW_ALL_ROUTES)
            bmodel.initiativeHelper.generateInitiativeCoverageReport();

        // Code moved from DOWNLOAD
        bmodel.beatMasterHealper.downloadBeats();
        bmodel.channelMasterHelper.downloadChannel();

        bmodel.reasonHelper.downloadDeviatedReason();
        bmodel.reasonHelper.downloadNonVisitReasonMaster();
        bmodel.reasonHelper.downloadNonProductiveReasonMaster();
        bmodel.subChannelMasterHelper.downloadsubChannel();

        bmodel.productHelper
                .setBuffer((float) ((float) bmodel.configurationMasterHelper
                        .downloadSOBuffer() / (float) 100));
        bmodel.labelsMasterHelper.downloadLabelsMaster();

        //save sales return with Old batchid for the product
        bmodel.productHelper.loadOldBatchIDMap();

        //credintote updatation and loading
        bmodel.collectionHelper.updateCreditNoteACtualAmt();
        bmodel.collectionHelper.loadCreditNote();

        bmodel.reasonHelper.downloadReasons();
        bmodel.updateIsTodayAndIsVanSalesInRetailerMasterInfo();
        bmodel.productHelper.downloadOrdeType();

        bmodel.configurationMasterHelper.downloadPasswordPolicy();

        if (bmodel.configurationMasterHelper.IS_ENABLE_GCM_REGISTRATION && bmodel.isOnline())
            LoginHelper.getInstance(context).onGCMRegistration();

        if (bmodel.configurationMasterHelper.IS_CHAT_ENABLED)
            bmodel.downloadChatCredentials();
        if (LoginHelper.getInstance(context).IS_PASSWORD_ENCRYPTED)
            setEncryptType();

        bmodel.printHelper.deletePrintFileAfterDownload(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                + "/" + DataMembers.PRINT_FILE_PATH + "/");
    }

    /**
     * This method is used to check which is next method to call
     * using condition
     *
     * @return value
     */
    public NEXT_METHOD checkNextSyncMethod() {
        if (!isDistributorDownloadDone
                && bmodel.configurationMasterHelper.IS_DISTRIBUTOR_AVAILABLE) {
            isDistributorDownloadDone = true;
            return NEXT_METHOD.DISTRIBUTOR_DOWNLOAD;
        } else if (!isDistributorDownloadDone) {
            isDistributorDownloadDone = true;
            return NEXT_METHOD.NON_DISTRIBUTOR_DOWNLOAD;
        } else if (!isLastVisitTranDownloadDone
                && bmodel.configurationMasterHelper.isLastVisitTransactionDownloadConfigEnabled()) {
            if (getmRetailerWiseIterateCount() <= 0) {
                isLastVisitTranDownloadDone = true;
            }
            return NEXT_METHOD.LAST_VISIT_TRAN_DOWNLOAD;
        } else if (!isSihDownloadDone
                && !getSIHUrl().equals("")) {
            isSihDownloadDone = true;
            return NEXT_METHOD.SIH_DOWNLOAD;
        } else if (bmodel.isDigitalContentAvailable()) {
            return NEXT_METHOD.DIGITAL_CONTENT_AVALILABLE;
        } else {
            return NEXT_METHOD.DEFAULT;
        }
    }

    public JSONObject getCommonJsonObject() {
        JSONObject json = null;
        try {
            json = new JSONObject();
            json.put(SynchronizationHelper.USER_ID, bmodel.userMasterHelper.getUserMasterBO()
                    .getUserid());
            json.put(SynchronizationHelper.VERSION_CODE, bmodel.getApplicationVersionNumber());
            json.put(SynchronizationHelper.MOBILE_DATE_TIME,
                    Utils.getDate("yyyy/MM/dd HH:mm:ss"));
            json.put(SynchronizationHelper.MOBILE_UTC_DATE_TIME,
                    Utils.getGMTDateTime("yyyy/MM/dd HH:mm:ss"));
            if (!DataMembers.backDate.isEmpty())
                json.put(SynchronizationHelper.REQUEST_MOBILE_DATE_TIME,
                        SDUtil.now(SDUtil.DATE_TIME_NEW));

        } catch (Exception e) {
            Commons.printException(e);
        }
        return json;
    }

    private String getLastTransactedDate() {
        DBUtil db = null;
        String date = Utils.getDate("yyyy/MM/dd") + " 23:59:00";
        ;
        ArrayList<String> dateList = new ArrayList<String>();
        try {


            db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            String query = "SELECT Orderdate from OrderHeader ORDER BY Orderdate DESC";
            Cursor c = db.selectSQL(query);
            if (c.getCount() > 0) {
                if (c.moveToFirst()) {
                    dateList.add(c.getString(0));
                }
            }
            query = "SELECT InvoiceDate from InvoiceMaster ORDER BY InvoiceDate DESC";
            c = db.selectSQL(query);
            if (c.getCount() > 0) {
                if (c.moveToFirst()) {
                    dateList.add(c.getString(0));
                }
            }
            query = "SELECT date from SalesReturnHeader ORDER BY date DESC";
            c = db.selectSQL(query);
            if (c.getCount() > 0) {
                if (c.moveToFirst()) {
                    dateList.add(c.getString(0));
                }
            }
            query = "SELECT DeliveryDate from VanDeliveryHeader ORDER BY DeliveryDate DESC";
            c = db.selectSQL(query);
            if (c.getCount() > 0) {
                if (c.moveToFirst()) {
                    dateList.add(c.getString(0));
                }
            }

            if (dateList.size() > 0) {
                Collections.sort(dateList, dateCompartor);
                date = dateList.get(0) + " " + "23:59:00";
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);

        }
        return date;
    }

    private static final Comparator<String> dateCompartor = new Comparator<String>() {

        public int compare(String file1, String file2) {


            return file2.compareTo(file1);

        }

    };

    public boolean isAttendanceCompleted() {
        DBUtil db = null;
        boolean check = true;
        int flag = 0;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            int counts = 0;

            Cursor c = db
                    .selectSQL("SELECT HHTCode ,(select COUNT(upload) from AttendanceTimeDetails where upload='I') as count FROM " +
                            "HhtMenuMaster where HHTCode='MENU_IN_OUT' and Flag=1 and hasLink=1");
            if (c != null) {
                if (c.moveToFirst())
                    counts = c.getInt(1);
            }
            Commons.print("Count of Attendance not completed : ," + counts + "");
            c.close();
            db.close();

            if (counts > 0) {
                check = false;
            } else {
                check = true;
            }

        } catch (Exception e) {
            Commons.printException(e);
        }
        return check;
    }

    public boolean isSaleDrafted() {
        DBUtil db = null;
        boolean check = true;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            int counts = 0;

            Cursor c = db
                    .selectSQL("select COUNT(pid) from CS_CustomerSaleDetails where upload='I'");
            if (c != null) {
                if (c.moveToFirst())
                    counts = c.getInt(0);
            }
            Commons.print("Count of isSaleDrafter not completed : ," + counts + "");
            c.close();
            db.close();

            if (counts > 0) {
                check = false;
            } else {
                check = true;
            }

        } catch (Exception e) {
            Commons.printException(e);
        }
        return check;
    }

    public void insertImageDetails(List<S3ObjectSummary> filesList) {
        Commons.print("insert start time " + SDUtil.now(SDUtil.TIME));
        android.database.sqlite.SQLiteDatabase database;
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            database = db.getWritableDatabase();
            database.beginTransaction();
            String columns = "Key,lastModified";
            ContentValues values = new ContentValues();
            for (int i = 0; i < filesList.size(); i++) {
                values.put("Key", filesList.get(i).getKey());
                values.put("lastModified", filesList.get(i).getLastModified() + "");
                values.put("Flag", 0);

                /*String values = bmodel.QT(filesList.get(i).getKey())
                        + ","
                        + bmodel.QT(filesList.get(i).getLastModified() + "");*/

                database.insert("CatalogImagesDetails", columns, values);
            }
            database.setTransactionSuccessful();
            database.endTransaction();
            db.closeDB();
            Commons.print("insert end time " + SDUtil.now(SDUtil.TIME));
        } catch (Exception e) {
            Commons.printException("insertImageDetails" + e);
        }
    }

    private List<S3ObjectSummary> filesList;

    public List<S3ObjectSummary> getImageDetails() {
        filesList = new ArrayList<>();
        S3ObjectSummary fileData;
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            Cursor c = db.selectSQL("Select * from CatalogImagesDetails where Flag != 1");
            /*Cursor c = db.selectSQL("Select CID.Key, CID.lastModified,PM.pCode,CID.Flag from CatalogImagesDetails CID " +
                    "INNER JOIN ProductMaster PM ON CID.Key = '" + DataMembers.S3_ROOT_DIRECTORY + "/Product/ProductCatalog/'" +
                    "|| PM.pCode ||'.jpg' AND PM.PLid = (Select Max(LevelId) from ProductLevel) Where CID.Flag != 1");
*/
            if (c != null && c.getCount() > 0) {
                while (c.moveToNext()) {
                    fileData = new S3ObjectSummary();
                    fileData.setKey(c.getString(c.getColumnIndex("Key")));
                    fileData.setBucketName(DataMembers.S3_ROOT_DIRECTORY);
                    fileData.setETag(c.getString(c.getColumnIndex("lastModified")));
                    filesList.add(fileData);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("Error catalog images list", e);
        }
        Commons.print("File List size " + filesList.size());
        return filesList;
    }

    public void updateImageDetails(List<S3ObjectSummary> filesList) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            String columns = "Key,lastModified,Flag";
            android.database.sqlite.SQLiteDatabase database = db.getWritableDatabase();
            database.beginTransaction();
            for (S3ObjectSummary s3ObjectSummary : filesList) {
                database.delete("CatalogImagesDetails", "Key=?", new String[]{bmodel.QT(s3ObjectSummary.getKey())});
            }

            for (int i = 0; i < filesList.size(); i++) {

                /*String values = bmodel.QT(filesList.get(i).getKey())
                        + ","
                        + bmodel.QT(filesList.get(i).getLastModified() + "") + "," + 0;*/
                ContentValues values = new ContentValues();
                values.put("Key", filesList.get(i).getKey());
                values.put("lastModified", filesList.get(i).getLastModified() + "");
                values.put("Flag", 0);
                database.insert("CatalogImagesDetails", columns, values);
            }
            database.setTransactionSuccessful();
            database.endTransaction();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("insertImageDetails" + e);
        }
    }

    public void deleteImageDetails(List<S3ObjectSummary> filesList) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            for (S3ObjectSummary s3ObjectSummary : filesList) {
                db.deleteSQL("CatalogImagesDetails", "Key=" + s3ObjectSummary.getKey(), false);
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("deleteImageDetails" + e);
        }
    }

    public void clearCatalogImages() {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            db.executeQ("Delete from CatalogImagesDetails");
            deleteFiles(getStorageDir(context.getResources().getString(R.string.app_name)));
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("deleteImageDetails" + e);
        }
    }

    public void deleteFiles(File file) {

        if (file.exists()) {
            String deleteCmd = "rm -r " + file.getAbsolutePath();
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec(deleteCmd);
            } catch (IOException e) {
            }
        }
    }

    /* Flag - Download Status
    *  0 - Download Failed / Not yet downloaded
    *  1 - Download Success */
    //private static int update_count = 0;
    public void updateFlagInCatalogImage(HashMap<String, Integer> downloadImageStatus) {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
        try {
            //update_count++;
            db.createDataBase();
            db.openDataBase();
            android.database.sqlite.SQLiteDatabase database = db.getWritableDatabase();
            database.beginTransaction();
            for (String keys : downloadImageStatus.keySet()) {
                Commons.print("Key" + keys + ", " + downloadImageStatus.get(keys));
                ContentValues contentValues = new ContentValues();
                contentValues.put("Flag", downloadImageStatus.get(keys));
                database.update("CatalogImagesDetails", contentValues, "Key=?", new String[]{keys});
            }
            Commons.print("Catalog image update ");
            database.setTransactionSuccessful();
            database.endTransaction();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public int getCatalogImagesCount() {
        return totalCatalogImageCount;
    }

    public void setCatalogImageDownloadFinishTime(String count) {
        String filename = "log";
        String string = SDUtil.now(SDUtil.DATE_TIME) + "\n";
        FileOutputStream outputStream;
        Commons.print("time " + string);
        try {
            Commons.print("FilePath " + getStorageDir(context.getResources().getString(R.string.app_name)) + "/" + filename);
            outputStream = new FileOutputStream(getStorageDir(context.getResources().getString(R.string.app_name)) + "/" + filename);//context.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(string.getBytes());
            outputStream.write(count.getBytes());

            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    int totalCatalogImageCount = 0;

    public String getLastDownloadedDateTime() {
        //Find the directory for the SD Card using the API
        File sdcard = getStorageDir(context.getResources().getString(R.string.app_name));

//Get the text file
        File file = new File(sdcard, "log");
        if (file.exists()) {
//Read text from file
            StringBuilder text = new StringBuilder();

            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                int count = 0;
                while ((line = br.readLine()) != null) {
                    if (count == 0) {
                        text.append(line);
                    } else {
                        totalCatalogImageCount = Integer.parseInt(line);
                    }
                    Commons.print("read line" + line);
                    count++;
                }
                br.close();
                Commons.print("Last downloaded time " + text);
                return text.toString();
            } catch (IOException e) {
                Commons.print("error" + e.getMessage());
            }
        }
        return "";
    }

    public File getStorageDir(String folderName) {

        File docsFolder = new File(Environment.getExternalStorageDirectory(), folderName);
        if (!docsFolder.exists()) {
            docsFolder.mkdir();
        }
        return docsFolder;

    }
}
