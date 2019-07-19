package com.ivy.cpg.view.sync;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.ivy.core.IvyConstants;
import com.ivy.cpg.view.sfdc.MyjsonarrayPostRequest;
import com.ivy.cpg.view.van.LoadManagementHelper;
import com.ivy.lib.Utils;
import com.ivy.lib.existing.DBUtil;
import com.ivy.lib.rest.JSONFormatter;
import com.ivy.sd.png.asean.view.BuildConfig;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.DeviceUtils;
import com.ivy.utils.network.TLSSocketFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

import static com.ivy.lib.Utils.QT;
import static com.ivy.sd.png.provider.SynchronizationHelper.JSON_DATA_KEY;
import static com.ivy.sd.png.provider.SynchronizationHelper.SYNC_STATUS_COMPLETED;
import static com.ivy.sd.png.provider.SynchronizationHelper.SYNC_STATUS_FAILED;
import static com.ivy.sd.png.provider.SynchronizationHelper.access_token;
import static com.ivy.sd.png.provider.SynchronizationHelper.instance_url;

/**
 * Created by rajkumar on 19/3/18.
 * Upload helper
 */

public class UploadHelper {


    private BusinessModel businessModel;
    private static UploadHelper instance;

    private Handler handler;
    private StringBuilder mVisitedRetailerIds;
    private Context mContext;
    private RequestQueue mRequestQueue;
    private static final String TAG = "UploadHelper";

    public enum UPLOAD_STATUS {

        SUCCESS(1),
        TOKEN_ERROR(-1),
        URL_NOTFOUND(3),
        FAILED(0);

        private int value;

        UPLOAD_STATUS(int value) {
            this.value = value;
        }
    }

    // URL Type Code for synchronous upload
    public static final String UPLOAD_REALLOCATION_URL_CODE = "UPLDALLOC";
    public static final String UPLOAD_STOCK_APPLY_URL_CODE = "UPLDSTOK";
    public static final String UPLOAD_SIH_URL_CODE = "UPLDSIH";
    public static final String UPLOAD_TRIP_URL_CODE = "UPLOADTRIP";
    public static final String UPLOAD_PICKLIST_URL_CODE = "UPLDDELIVERYSTS";
    public static final String UPLOAD_LOYALTY_URL_CODE = "UPLDLOYALTY";
    public static final String UPLOAD_ORDR_DEL_URL_CODE = "UPLDORDDELSTS";
    public static final String UPLOAD_SEQUENCE_URL_CODE = "UPLDSEQ";

    // Regular transaction upload
    public static final String UPLOAD_TRANSACTION_URL_CODE = "UPLDTRAN";

    // Other spl direct connect URL's
    private static final String UPLOAD_NEW_RETAILER_URL_CODE = "UPLDRET";
    public static final String UPLOAD_USER_REPLACEMENT_URL_CODE = "USRREPLACEUPLD";
    public static final String UPLOAD_TERMSACCEPT_URL_CODE = "UPDATEUSER";


    private UploadHelper(Context context) {
        this.mContext = context;
        this.businessModel = (BusinessModel) context.getApplicationContext();
    }

    public static UploadHelper getInstance(Context context) {
        if (instance == null) {
            instance = new UploadHelper(context);
        }
        return instance;
    }

    /**
     * Has attendance completed or not
     * @param context application context
     * @return true or false
     */
    public boolean isAttendanceCompleted(Context context) {
        DBUtil db;
        boolean check = true;
        try {
            db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME);
            db.createDataBase();
            db.openDataBase();

            int counts = 0;

            Cursor c = db
                    .selectSQL("SELECT HHTCode ,(select COUNT(upload) from AttendanceTimeDetails where outtime IS NULL) as count FROM " +
                            "HhtMenuMaster where HHTCode='MENU_IN_OUT' and Flag=1 and hasLink=1");
            if (c != null) {
                if (c.moveToFirst())
                    counts = c.getInt(1);

                c.close();
            }
            db.close();

            check = counts <= 0;

        } catch (Exception e) {
            Commons.printException(e);
        }
        return check;
    }


    /**
     * upload starts
     */
    public UPLOAD_STATUS uploadTransactionDataByType(final Handler handler, final int syncType, Context context) {

        UPLOAD_STATUS uploadStatus = UPLOAD_STATUS.FAILED;

        this.handler = handler;
        try {

            // Open Database connection to read data
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME);
            db.createDataBase();
            db.openDataBase();

            // Generate data to upload based on Sync Type
            JSONObject jsonObjData = prepareTransactionDataToUploadByType(syncType, db, handler);

            // Generate Header Information
            JSONFormatter uploadHeaderInformation = generateUploadHeaderInformation(context, jsonObjData);

            // Store Log
            if (businessModel.configurationMasterHelper.SHOW_DATA_UPLOAD_STATUS) {
                storeSyncLog(jsonObjData, db);
            }
            String uploadStartTime = DateTimeUtils.now(DateTimeUtils.DATE_TIME_NEW);


            if (BuildConfig.FLAVOR.equalsIgnoreCase("aws")) {

                //Get Sync URL by Sync Type
                String uploadURL = getUploadURLByType(syncType);
                if (uploadURL.length() == 0) {
                    return UPLOAD_STATUS.URL_NOTFOUND;
                }

                // Connect server and get upload
                uploadStatus = getUploadResponseStatus(context, jsonObjData, uploadHeaderInformation, uploadURL);

                // Update transaction
                updateTransactionUploadStatusByType(syncType, context, uploadStatus);

                // Update Sync Log
                businessModel.synchronizationHelper.insertSyncHeader(uploadStartTime, DateTimeUtils.now(DateTimeUtils.DATE_TIME_NEW), SynchronizationHelper.SYNC_TYPE_DGT_UPLOAD,
                        0, uploadStatus == UPLOAD_STATUS.SUCCESS ? SynchronizationHelper.SYNC_STATUS_COMPLETED : SYNC_STATUS_FAILED, 1);
            } else {
                final JSONObject finalJsonObjData = jsonObjData;
                businessModel.synchronizationHelper.getAuthToken(new SynchronizationHelper.VolleyResponseCallbackInterface() {
                    @Override
                    public String onSuccess(String result) {
                        getUploadResponseSFDC(finalJsonObjData, syncType, handler);
                        return "";
                    }

                    @Override
                    public String onFailure(String errorresult) {
                        return "";
                    }
                });

            }

            db.closeDB();
        } catch (JSONException jsonException) {
            Commons.printException("" + jsonException);
        } catch (Exception e) {
            Commons.printException("" + e);
            return UPLOAD_STATUS.FAILED;
        }
        return uploadStatus;
    }

    private JSONObject prepareTransactionDataToUploadByType(int flag, DBUtil db, Handler handlerr) throws Exception {

        JSONObject jsonObjData = new JSONObject();

        if (flag == UploadThread.SYNC_UPLOAD) {
            Set<String> keys = DataMembers.uploadColumn.keySet();

            jsonObjData = new JSONObject();
            for (String tableName : keys) {
                if (!DataMembers.tbl_SyncLogDetails.equalsIgnoreCase(tableName)) {
                    JSONArray jsonArray = prepareDataForUploadJSON(db,
                            handlerr, tableName,
                            DataMembers.uploadColumn.get(tableName));

                    if (jsonArray.length() > 0)
                        jsonObjData.put(tableName, jsonArray);
                }
            }

        } else if (flag == UploadThread.SYNC_UPLOAD_RETAILER_WISE) {
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

        } else if (flag == UploadThread.ATTENDANCE_UPLOAD) {
            Set<String> keys = DataMembers.uploadAttendanceColumn.keySet();

            jsonObjData = new JSONObject();
            for (String tableName : keys) {
                JSONArray jsonArray = prepareDataForUploadJSON(db,
                        handlerr, tableName,
                        DataMembers.uploadAttendanceColumn.get(tableName));

                if (jsonArray.length() > 0)
                    jsonObjData.put(tableName, jsonArray);
            }

        } else if (flag == UploadThread.SYNC_SEQ_NUMBER_UPLOAD) {

            Set<String> keys = DataMembers.uploadInvoiceSequenceNo.keySet();
            jsonObjData = new JSONObject();
            for (String tableName : keys) {
                JSONArray jsonArray = prepareDataForUploadJSON(db, handler,
                        tableName,
                        DataMembers.uploadInvoiceSequenceNo.get(tableName));

                if (jsonArray.length() > 0)
                    jsonObjData.put(tableName, jsonArray);
            }

        }else if (flag == UploadThread.SYNC_LYTY_PT_UPLOAD) {
            Set<String> keys = DataMembers.uploadLPTable.keySet();

            jsonObjData = new JSONObject();
            for (String tableName : keys) {
                JSONArray jsonArray = prepareDataForUploadJSON(db,
                        handlerr, tableName,
                        DataMembers.uploadLPTable.get(tableName));

                if (jsonArray.length() > 0)
                    jsonObjData.put(tableName, jsonArray);
            }

        } else if (flag == UploadThread.SYNC_SIH_UPLOAD) {
            Set<String> keys = DataMembers.uploadSIHTable.keySet();

            jsonObjData = new JSONObject();
            for (String tableName : keys) {
                JSONArray jsonArray = prepareDataForUploadJSON(db,
                        handlerr, tableName,
                        DataMembers.uploadSIHTable.get(tableName));

                if (jsonArray.length() > 0)
                    jsonObjData.put(tableName, jsonArray);
            }

        } else if (flag == UploadThread.SYNC_STK_APPLY_UPLOAD) {
            Set<String> keys = DataMembers.uploadStockApplyTable.keySet();

            jsonObjData = new JSONObject();
            for (String tableName : keys) {
                JSONArray jsonArray = prepareDataForUploadJSON(db,
                        handlerr, tableName,
                        DataMembers.uploadStockApplyTable.get(tableName));

                if (jsonArray.length() > 0)
                    jsonObjData.put(tableName, jsonArray);
            }

        } else if (flag == UploadThread.SYNC_REALLOC_UPLOAD) {
            Set<String> keys = DataMembers.uploadReallocTable.keySet();

            jsonObjData = new JSONObject();
            for (String tableName : keys) {
                JSONArray jsonArray = prepareDataForUploadJSON(db,
                        handlerr, tableName,
                        DataMembers.uploadReallocTable.get(tableName));

                if (jsonArray.length() > 0)
                    jsonObjData.put(tableName, jsonArray);
            }

        } else if (flag == UploadThread.SYNC_ORDER_DELIVERY_STATUS_UPLOAD) { // Delivered order realtime sync
            Set<String> keys = DataMembers.uploadOrderDeliveryStatusTable.keySet();

            jsonObjData = new JSONObject();
            for (String tableName : keys) {
                JSONArray jsonArray = prepareDataForUploadJSON(db,
                        handlerr, tableName,
                        DataMembers.uploadOrderDeliveryStatusTable.get(tableName));

                if (jsonArray.length() > 0)
                    jsonObjData.put(tableName, jsonArray);
            }

        } else if (flag == UploadThread.SYNC_PICK_LIST_UPLOAD) { // Pick List

            Set<String> keys = DataMembers.uploadPickListStatusTable.keySet();

            jsonObjData = new JSONObject();
            for (String tableName : keys) {
                JSONArray jsonArray = prepareDataForUploadJSON(db,
                        handlerr, tableName,
                        DataMembers.uploadPickListStatusTable.get(tableName));

                if (jsonArray.length() > 0)
                    jsonObjData.put(tableName, jsonArray);
            }

        } else if (flag == UploadThread.SYNC_TRIP) { // Pick List

            Set<String> keys = DataMembers.uploadTripTable.keySet();

            jsonObjData = new JSONObject();
            for (String tableName : keys) {
                JSONArray jsonArray = prepareDataForUploadJSON(db,
                        handlerr, tableName,
                        DataMembers.uploadTripTable.get(tableName));

                if (jsonArray.length() > 0)
                    jsonObjData.put(tableName, jsonArray);
            }

        }

        return jsonObjData;
    }

    private void storeSyncLog(JSONObject jsonObjData, DBUtil db) {
        String id = DateTimeUtils.now(DateTimeUtils.DATE_TIME);
        Iterator<String> keyItr = jsonObjData.keys();
        while (keyItr.hasNext()) {
            String key = keyItr.next();
            if (DataMembers.statusReportTables.keySet().contains(key)) {
                try {
                    String name = DataMembers.statusReportTables.get(key);
                    JSONArray jsonArray = jsonObjData.getJSONArray(key);
                    db.insertSQL("SyncStatus_Internal", "id,TableName,LineCount", QT(id) + "," + QT(name) + "," + jsonArray.length());
                } catch (Exception ex) {
                    Commons.printException("" + ex);
                }
            }
        }
    }

    private String getUploadURLByType(int flag) {
        String url = "";

        switch (flag) {
            case UploadThread.SYNC_SIH_UPLOAD:
                url = businessModel.synchronizationHelper.getUploadUrl(UPLOAD_SIH_URL_CODE);
                break;
            case UploadThread.SYNC_STK_APPLY_UPLOAD:
                url = businessModel.synchronizationHelper.getUploadUrl(UPLOAD_STOCK_APPLY_URL_CODE);
                break;
            case UploadThread.SYNC_SEQ_NUMBER_UPLOAD:
                url = businessModel.synchronizationHelper.getUploadUrl(UPLOAD_SEQUENCE_URL_CODE);
                break;
            case UploadThread.SYNC_REALLOC_UPLOAD:
                url = businessModel.synchronizationHelper.getUploadUrl(UPLOAD_REALLOCATION_URL_CODE);
                break;
            case UploadThread.SYNC_LYTY_PT_UPLOAD:
                url = businessModel.synchronizationHelper.getUploadUrl(UPLOAD_LOYALTY_URL_CODE);
                break;
            case UploadThread.SYNC_ORDER_DELIVERY_STATUS_UPLOAD:
                url = businessModel.synchronizationHelper.getUploadUrl(UPLOAD_ORDR_DEL_URL_CODE);
                break;
            case UploadThread.SYNC_PICK_LIST_UPLOAD:
                url = businessModel.synchronizationHelper.getUploadUrl(UPLOAD_PICKLIST_URL_CODE);
                break;
            case UploadThread.SYNC_TRIP:
                url = businessModel.synchronizationHelper.getUploadUrl(UPLOAD_TRIP_URL_CODE);
                break;
            default:
                url = businessModel.synchronizationHelper.getUploadUrl(UPLOAD_TRANSACTION_URL_CODE);
                break;
        }

        return url;

    }

    /**
     * This method will return header information as json
     *
     * @param context    application context
     * @param uploadData to calcuate hash
     * @return JSON - header Information
     */
    private JSONFormatter generateUploadHeaderInformation(Context context, JSONObject uploadData) {

        JSONFormatter jsonFormatter = new JSONFormatter("HeaderInformation");


        try {
            if (!"0".equals(businessModel.userMasterHelper.getUserMasterBO().getBackupSellerID())) {
                jsonFormatter.addParameter("UserId", businessModel.userMasterHelper
                        .getUserMasterBO().getBackupSellerID());
                jsonFormatter.addParameter("WorkingFor", businessModel.userMasterHelper.getUserMasterBO().getUserid());
            } else {
                jsonFormatter.addParameter("UserId", businessModel.userMasterHelper
                        .getUserMasterBO().getUserid());
            }

            jsonFormatter.addParameter("DistributorId", businessModel.userMasterHelper
                    .getUserMasterBO().getDistributorid());
            jsonFormatter.addParameter("BranchId", businessModel.userMasterHelper
                    .getUserMasterBO().getBranchId());
            jsonFormatter.addParameter("LoginId", businessModel.userMasterHelper
                    .getUserMasterBO().getLoginName());
            jsonFormatter.addParameter("DeviceId",
                    DeviceUtils.getIMEINumber(context));
            jsonFormatter.addParameter("VersionCode",
                    businessModel.getApplicationVersionNumber());
            jsonFormatter.addParameter(SynchronizationHelper.VERSION_NAME, businessModel.getApplicationVersionName());
            jsonFormatter.addParameter("OrganisationId", businessModel.userMasterHelper
                    .getUserMasterBO().getOrganizationId());
            jsonFormatter.addParameter("ParentPositionIds", businessModel.getUserParentPosition());
            if (businessModel.synchronizationHelper.isDayClosed()) {
                int varianceDwnDate = DateTimeUtils.compareDate(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                        businessModel.userMasterHelper.getUserMasterBO().getDownloadDate(),
                        "yyyy/MM/dd");
                if (varianceDwnDate == 0) {
                    jsonFormatter.addParameter("MobileDateTime",
                            Utils.getDate("yyyy/MM/dd HH:mm:ss"));
                }
                if (varianceDwnDate > 0) {
                    jsonFormatter.addParameter("MobileDateTime",
                            businessModel.synchronizationHelper.getLastTransactedDate());
                }
            } else
                jsonFormatter.addParameter("MobileDateTime",
                        Utils.getDate("yyyy/MM/dd HH:mm:ss"));

            jsonFormatter.addParameter("MobileUTCDateTime",
                    Utils.getGMTDateTime("yyyy/MM/dd HH:mm:ss"));
            jsonFormatter.addParameter("DownloadedDataDate",
                    businessModel.userMasterHelper.getUserMasterBO().getDownloadDate());
            jsonFormatter.addParameter("VanId", businessModel.userMasterHelper
                    .getUserMasterBO().getVanId());
            jsonFormatter.addParameter("platform", "Android");
            jsonFormatter.addParameter("osversion",
                    android.os.Build.VERSION.RELEASE);
            jsonFormatter.addParameter("firmware", "");
            jsonFormatter.addParameter("model", Build.MODEL);
            String LastDayClose = "";
            if (businessModel.synchronizationHelper.isDayClosed()) {
                LastDayClose = businessModel.userMasterHelper.getUserMasterBO()
                        .getDownloadDate();
            }
            jsonFormatter.addParameter("LastDayClose", LastDayClose);
            jsonFormatter.addParameter("DataValidationKey", businessModel.synchronizationHelper.generateChecksum(uploadData.toString()));
            Commons.print(jsonFormatter.getDataInJson());
        } catch (Exception e) {
            Commons.printException("" + e);
        }

        return jsonFormatter;
    }


    private void getUploadResponseSFDC(final JSONObject data, final int flag, final Handler handler) {

        String initTime = DateTimeUtils.now(DateTimeUtils.DATE_TIME_NEW);

        MyjsonarrayPostRequest jsonObjectRequest;

        String uniqueTransactionID = businessModel.userMasterHelper.getUserMasterBO().getUserid()
                + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);

        // This has not value, sent this for just downwards compatiblity.
        try {
            data.put("MessageId", uniqueTransactionID);
            HttpsURLConnection.setDefaultSSLSocketFactory(new TLSSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }

        StringBuilder sb = new StringBuilder();
        sb.append(instance_url);
        sb.append(businessModel.synchronizationHelper.getUploadUrl("UPLDTRAN"));

        try {
            jsonObjectRequest = new MyjsonarrayPostRequest(
                    Request.Method.POST, sb.toString(), data,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray jsonObject) {
                            Commons.print(" RES: " + jsonObject);
                            System.gc();
                            try {

                                int response = 0;
                                if (jsonObject.toString().contains("Response")) {
                                    response = jsonObject.getJSONObject(0).getInt("Response");
                                }

                                int responseMsg = 0;
                                if (response == 1) {

                                    if (flag == UploadThread.SYNC_UPLOAD_RETAILER_WISE) {
                                        updateUploadFlagRetailerWise(mContext);
                                        getVisitedRetailerIds().delete(0,
                                                getVisitedRetailerIds().length());
                                        responseMsg = 1;
                                    } else if (flag == UploadThread.SYNC_SIH_UPLOAD) {
                                        updateUploadFlag(DataMembers.uploadSIHTable, mContext);
                                        responseMsg = 2;
                                    } else if (flag == UploadThread.SYNC_LYTY_PT_UPLOAD) {
                                        updateUploadFlag(DataMembers.uploadLPTable, mContext);
                                        responseMsg = 2;
                                    } else if (flag == UploadThread.SYNC_STK_APPLY_UPLOAD) {

                                        updateUploadFlag(DataMembers.uploadStockApplyTable, mContext);
                                        responseMsg = 2;
                                    } else if (flag == UploadThread.SYNC_REALLOC_UPLOAD) {
                                        updateUploadFlag(DataMembers.uploadReallocTable, mContext);
                                        responseMsg = 1;
                                    } else if (flag == UploadThread.ATTENDANCE_UPLOAD) {
                                        updateUploadFlag(DataMembers.uploadAttendanceColumn, mContext);
                                        responseMsg = 1;
                                    } else {
                                        updateUploadFlag(DataMembers.uploadColumn, mContext);
                                        responseMsg = 1;
                                    }

                                } else if (response == 0) {
                                    if (UploadThread.SYNC_UPLOAD_RETAILER_WISE == 1) {
                                        getVisitedRetailerIds().delete(0,
                                                getVisitedRetailerIds().length());
                                        responseMsg = 0;
                                    }
                                }
                                // Upload Transaction Sequence Table Separate , the above method
                                // successfully upload. This Method also doing same work, but server
                                // need the data while replicate this data while download instantly.
                                if ((businessModel.configurationMasterHelper.SHOW_ORDER_SEQUENCE_NO || businessModel.configurationMasterHelper.SHOW_INVOICE_SEQUENCE_NO
                                        || businessModel.configurationMasterHelper.SHOW_COLLECTION_SEQ_NO)
                                        && businessModel.orderAndInvoiceHelper.hasTransactionSequence()) {
                                    if (responseMsg == 1) {
                                        responseMsg = uploadInvoiceSequenceNo(handler, mContext).value;
                                    }
                                }
                                Commons.print("After Responce");

                                String syncStatus;
                                if (responseMsg == 1) {
                                    syncStatus = SYNC_STATUS_COMPLETED;
                                } else {
                                    syncStatus = SYNC_STATUS_FAILED;
                                }
                                businessModel.synchronizationHelper.insertSyncHeader(initTime, DateTimeUtils.now(DateTimeUtils.DATE_TIME_NEW), SynchronizationHelper.SYNC_TYPE_DGT_UPLOAD,
                                        0, syncStatus, 1);

                                if (responseMsg == 1) {
                                    handler.sendEmptyMessage(
                                            DataMembers.NOTIFY_UPLOADED);
                                } else if (responseMsg == -1) {
                                    handler.sendEmptyMessage(
                                            DataMembers.NOTIFY_TOKENT_AUTHENTICATION_FAIL);


                                } else if (responseMsg == 0) {
                                    handler.sendEmptyMessage(
                                            DataMembers.NOTIFY_UPLOAD_ERROR);
                                }
                            } catch (JSONException e) {
                                //error.getMessage();
                                handler.sendEmptyMessage(
                                        DataMembers.NOTIFY_UPLOAD_ERROR);
                            }

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    System.gc();
                    handler.sendEmptyMessage(
                            DataMembers.NOTIFY_UPLOAD_ERROR);
                    businessModel.synchronizationHelper.insertSyncHeader(initTime, DateTimeUtils.now(DateTimeUtils.DATE_TIME_NEW), SynchronizationHelper.SYNC_TYPE_DGT_UPLOAD,
                            0, SYNC_STATUS_FAILED, 1);
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "OAuth " + access_token);
                    headers.put("Content_Type", "application/json");
                    headers.put("HeaderInformation", getJsonObjectforLog().toString());
                    if (businessModel.synchronizationHelper.isDayClosed()) {
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(businessModel.getApplicationContext());
                        String tripExtendedDate = sharedPreferences.getString("tripExtendedDate", "");
                        if (!tripExtendedDate.equals("")) {
                            headers.put("MobileDate", DateTimeUtils.convertFromServerDateToRequestedFormat(tripExtendedDate, "yyyy/MM/dd HH:mm:ss"));
                        } else {
                            LoadManagementHelper loadManagementHelper = LoadManagementHelper.getInstance(mContext);
                            String startedDate = loadManagementHelper.getTripStartedDate(mContext);

                            String date = !startedDate.equals("") ? DateTimeUtils.convertFromServerDateToRequestedFormat(startedDate, "yyyy/MM/dd HH:mm:ss") : Utils.getDate("yyyy/MM/dd HH:mm:ss");
                            headers.put("MobileDate", date);
                        }
                    }

                    return headers;
                }

                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    //Map<String, String> params = new HashMap<String, String>();
                    return new HashMap<>();
                }

            };

            RetryPolicy policy = new DefaultRetryPolicy(
                    (int) TimeUnit.SECONDS.toMillis(30),
                    0,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            jsonObjectRequest.setRetryPolicy(policy);
            jsonObjectRequest.setShouldCache(false);

            addToRequestQueue(jsonObjectRequest, "");

        } catch (Exception e) {
            Commons.printException("" + e);
        }

    }

    private JSONObject getJsonObjectforLog() {
        JSONObject jsonLogObject = new JSONObject();
        try {
            jsonLogObject.put("DeviceId", DeviceUtils.getIMEINumber(mContext));
            jsonLogObject.put("LoginId", businessModel.userNameTemp);
            jsonLogObject.put(SynchronizationHelper.MOBILE_DATE_TIME, Utils.getDate("yyyy/MM/dd HH:mm:ss"));
            jsonLogObject.put(SynchronizationHelper.MOBILE_UTC_DATE_TIME, Utils.getGMTDateTime("yyyy/MM/dd HH:mm:ss"));
            jsonLogObject.put(SynchronizationHelper.VERSION_NAME, businessModel.getApplicationVersionName());
            jsonLogObject.put(SynchronizationHelper.VERSION_CODE, businessModel.getApplicationVersionNumber());
            jsonLogObject.put("Platform", "Android");
            jsonLogObject.put("OSVersion", android.os.Build.VERSION.RELEASE);
            jsonLogObject.put("Model", Build.MODEL);
            jsonLogObject.put("UserId", businessModel.getAppDataProvider().getUser().getUserid());
            jsonLogObject.put("BackupUserId", businessModel.getAppDataProvider().getUser().getBackupSellerID());
        } catch (JSONException jsonException) {
            Commons.print(jsonException.getMessage());
        }
        return jsonLogObject;
    }

    private RequestQueue getRequestQueue() {

        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mContext);
            mRequestQueue.getCache().clear();

        }
        return mRequestQueue;

    }

    private <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }


    /**
     * @param context
     * @param jsonObjData
     * @param jsonFormatter
     * @param url
     * @return UPLOAD_STATUS
     * @throws JSONException
     */
    private UPLOAD_STATUS getUploadResponseStatus(Context context, JSONObject jsonObjData, JSONFormatter jsonFormatter, String url) throws JSONException {

        int response = 0;
        Vector<String> responseVector = businessModel.synchronizationHelper.getUploadResponse(jsonFormatter.getDataInJson(),
                jsonObjData.toString(), url);

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
                            return UPLOAD_STATUS.TOKEN_ERROR;

                        }

                    }

                }
            }
            return response == 0 ? UPLOAD_STATUS.FAILED : UPLOAD_STATUS.SUCCESS;
        } else {
            if (!businessModel.synchronizationHelper.getAuthErroCode().equals(IvyConstants.AUTHENTICATION_SUCCESS_CODE)) {
                String errorMsg = businessModel.synchronizationHelper.getErrormessageByErrorCode().get(businessModel.synchronizationHelper.getAuthErroCode());
                if (errorMsg != null) {
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, context.getResources().getString(R.string.data_not_downloaded), Toast.LENGTH_SHORT).show();
                }
            }
            return UPLOAD_STATUS.FAILED;
        }
    }

    /**
     * This method will update transaction based on upload type
     * @param uploadType SIH or Stock or Transaction
     * @param context app context
     * @param response success or failure
     */
    private void updateTransactionUploadStatusByType(int uploadType, Context context, UPLOAD_STATUS response) {

        if (response == UPLOAD_STATUS.SUCCESS) {

            if (uploadType == UploadThread.SYNC_UPLOAD_RETAILER_WISE) {
                updateUploadFlagRetailerWise(context.getApplicationContext());
                getVisitedRetailerIds().delete(0,
                        getVisitedRetailerIds().length());
            } else if (uploadType == UploadThread.SYNC_SIH_UPLOAD) {
                updateUploadFlag(DataMembers.uploadSIHTable, context.getApplicationContext());
            } else if (uploadType == UploadThread.SYNC_LYTY_PT_UPLOAD) {
                updateUploadFlag(DataMembers.uploadLPTable, context.getApplicationContext());
            } else if (uploadType == UploadThread.SYNC_STK_APPLY_UPLOAD) {
                updateUploadFlag(DataMembers.uploadStockApplyTable, context.getApplicationContext());
            } else if (uploadType == UploadThread.SYNC_REALLOC_UPLOAD) {
                updateUploadFlag(DataMembers.uploadReallocTable, context.getApplicationContext());
            } else if (uploadType == UploadThread.ATTENDANCE_UPLOAD) {
                updateUploadFlag(DataMembers.uploadAttendanceColumn, context.getApplicationContext());
            } else if (uploadType == UploadThread.SYNC_ORDER_DELIVERY_STATUS_UPLOAD) {
                updateUploadFlag(DataMembers.uploadOrderDeliveryStatusTable, context.getApplicationContext());
            } else if (uploadType == UploadThread.SYNC_PICK_LIST_UPLOAD) {
                updateUploadFlag(DataMembers.uploadPickListStatusTable, context.getApplicationContext());
            } else if (uploadType == UploadThread.SYNC_TRIP) {
                updateUploadFlag(DataMembers.uploadTripTable, context.getApplicationContext());
            } else if (uploadType == UploadThread.SYNC_SEQ_NUMBER_UPLOAD) {
                updateUploadFlag(DataMembers.uploadInvoiceSequenceNo, context.getApplicationContext());
            } else {
                updateUploadFlag(DataMembers.uploadColumn, context.getApplicationContext());
            }
        } else if (response == UPLOAD_STATUS.FAILED) {
            if (UploadThread.SYNC_UPLOAD_RETAILER_WISE == 1) {
                getVisitedRetailerIds().delete(0,
                        getVisitedRetailerIds().length());

            }
        }

    }


    public StringBuilder getVisitedRetailerIds() {
        return mVisitedRetailerIds;
    }

    public void setVisitedRetailerIds(StringBuilder mVisitedRetailerIds) {
        this.mVisitedRetailerIds = mVisitedRetailerIds;
    }

    private void updateUploadFlagRetailerWise(Context context) {
        try {
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME
            );
            db.openDataBase();
            String query = "";
            Set<String> keys = DataMembers.uploadColumnWithOutRetailer.keySet();
            for (String tableName : keys) {
                query = "update " + tableName
                        + " set upload='Y' where upload='N'";
                db.updateSQL(query);
            }
            keys = DataMembers.uploadColumnWithRetailer.keySet();
            for (String tableName : keys) {
                if (tableName.equalsIgnoreCase("NearByRetailers") || tableName.equalsIgnoreCase("RetailerPotential")
                        || tableName.equalsIgnoreCase("RrtNearByEditRequest")) {
                    query = "update "
                            + tableName
                            + " set upload='Y' where upload='N' and rid in ("
                            + getVisitedRetailerIds().toString()
                            + ")";
                } else {
                    if (!tableName.equalsIgnoreCase("OrderDetailRequest")
                            || !tableName.equalsIgnoreCase("SOSDetail_Proj")
                            || !tableName.equalsIgnoreCase("RetailerPotential"))
                        query = "update "
                                + tableName
                                + " set upload='Y' where upload='N' and RetailerID in ("
                                + getVisitedRetailerIds().toString()
                                + ")";
                }
                db.updateSQL(query);
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
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

    private JSONArray prepareDataForNewRetailerJSONUpload(DBUtil db, Handler handler,
                                                          String tableName, String columns, String retailerID) {
        Message msg;

        JSONArray ohRowsArray = new JSONArray();

        try {
            Cursor cursor;
            String columnArray[] = columns.split(",");

            String retailerColumn = "RetailerID = " + QT(retailerID);
            if (tableName.equals(DataMembers.tbl_nearbyRetailer) || tableName.equals(DataMembers.tbl_retailerPotential)) {
                retailerColumn = "rid = " + QT(retailerID);
            }

            String sql = "select " + columns + " from " + tableName
                    + " where upload='N' and " + retailerColumn;
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
                    + getVisitedRetailerIds().toString() + ")";
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

    /**
     * Method to set upload column to Y for corresponding table list
     *
     * @param updateTableMap
     */
    private void updateUploadFlag(HashMap<String, String> updateTableMap, Context context) {
        try {
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME
            );
            db.openDataBase();
            Set<String> keys = updateTableMap.keySet();
            Commons.print(keys.size() + "size");
            for (String tableName : keys) {
                if (!DataMembers.tbl_SyncLogDetails.equalsIgnoreCase(tableName)) {
                    String query = "update " + tableName
                            + " set upload='Y' where upload='N'";
                    db.updateSQL(query);
                }
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
    private UPLOAD_STATUS uploadInvoiceSequenceNo(Handler handler, Context context) {

        UPLOAD_STATUS responseMessage = UPLOAD_STATUS.FAILED;

        JSONObject jsonObjData;
        this.handler = handler;
        try {
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME);
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


            // Generate header Information
            JSONFormatter jsonFormatter = generateUploadHeaderInformation(context, jsonObjData);

            // Get server URL
            String url = businessModel.synchronizationHelper.getUploadUrl(UPLOAD_SEQUENCE_URL_CODE);
            if (url.length() == 0)
                return UPLOAD_STATUS.URL_NOTFOUND;

            //Connect server
            Vector<String> responseVector = businessModel.synchronizationHelper.getUploadResponse(jsonFormatter.getDataInJson(), jsonObjData.toString(), url);

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

                                return UPLOAD_STATUS.TOKEN_ERROR;

                            }

                        }

                    }


                }
            } else {
                if (!businessModel.synchronizationHelper.getAuthErroCode().equals(IvyConstants.AUTHENTICATION_SUCCESS_CODE)) {
                    String errorMsg = businessModel.synchronizationHelper.getErrormessageByErrorCode().get(businessModel.synchronizationHelper.getAuthErroCode());
                    if (errorMsg != null) {
                        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, context.getResources().getString(R.string.data_not_downloaded), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            if (response == 1) {
                db.updateSQL("Update " + DataMembers.tbl_TransactionSequence
                        + " SET UPLOAD = 'Y' WHERE UPLOAD = 'N'");
                db.closeDB();
                responseMessage = UPLOAD_STATUS.SUCCESS;

            } else if (response == 0) {
                responseMessage = UPLOAD_STATUS.FAILED;
            }
            db.closeDB();
            Commons.print("uploadInvoiceSequenceNo:After Responce");
        } catch (Exception e) {
            Commons.printException("" + e);
            return UPLOAD_STATUS.FAILED;
        }
        return responseMessage;
    }

    /**
     * @See {@link  com.ivy.ui.profile.data.ProfileDataManagerImpl;}
     * Will be removed from @version CPG134 Release
     * @deprecated This has been Migrated to MVP pattern
     */
    public String uploadNewOutlet(Handler handler, Context context, String retailerID) {
        String rid = "";
        try {
            this.handler = handler;
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME);
            db.createDataBase();
            db.openDataBase();
            JSONObject jsonobj = new JSONObject();
            Set<String> keys = DataMembers.uploadNewRetailerColumn.keySet();

            for (String tableName : keys) {
                JSONArray jsonArray = prepareDataForNewRetailerJSONUpload(db, handler, tableName, DataMembers.uploadNewRetailerColumn.get(tableName), retailerID);
                if (jsonArray.length() > 0)
                    jsonobj.put(tableName, jsonArray);
            }

            Commons.print("jsonObjData.toString():0:" + jsonobj.toString());

            JSONFormatter jsonFormatter = generateUploadHeaderInformation(context, jsonobj);

            String appendurl = businessModel.synchronizationHelper.getUploadUrl(UPLOAD_NEW_RETAILER_URL_CODE);
            if (appendurl.length() == 0)
                return 2 + "";

            Vector<String> responseVector = businessModel.synchronizationHelper
                    .getUploadResponse(jsonFormatter.getDataInJson(), jsonobj.toString(), appendurl);

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


    public String uploadBackupSeller(String backupSellerId, Handler handler) {
        String res = "";
        try {
            this.handler = handler;

            // Prepare data to upload
            JSONObject jObject = new JSONObject();
            jObject.put("UserId", backupSellerId);
            jObject.put("ReplacementUser", businessModel.userMasterHelper.getUserMasterBO().getUserid());
            jObject.put("Date", Utils.getDate("yyyy/MM/dd"));

            JSONObject jsonobj = new JSONObject();
            jsonobj.put("UserReplacement", jObject);

            //Prepare Header Info
            JSONFormatter jsonFormatter = generateUploadHeaderInformation(mContext, jsonobj);


            String appendurl = businessModel.synchronizationHelper.getUploadUrl(UPLOAD_USER_REPLACEMENT_URL_CODE);
            if (appendurl.length() == 0)
                return 2 + "";

            Vector<String> responseVector = businessModel.synchronizationHelper
                    .getUploadResponse(jsonFormatter.getDataInJson(),
                            jsonobj.toString(), appendurl);

            if (responseVector.size() > 0) {

                for (String s : responseVector) {
                    JSONObject jsonObject = new JSONObject(s);

                    Iterator itr = jsonObject.keys();
                    while (itr.hasNext()) {
                        String key = (String) itr.next();
                        if (key.equals("ErrorCode")) {
                            res = jsonObject.getString("ErrorCode");
                        }
                    }


                }
            } else {
                if (!businessModel.synchronizationHelper.getAuthErroCode().equals(IvyConstants.AUTHENTICATION_SUCCESS_CODE)) {
                    res = businessModel.synchronizationHelper.getErrormessageByErrorCode().get(businessModel.synchronizationHelper.getAuthErroCode());
                }
            }
        } catch (SQLException | JSONException e) {
            Commons.printException(e);
        }
        return res;
    }

    /**
     * Method to upload synclogdetails. This method will get hit after normal upload/download.
     */
    public void uploadSyncLogDetails() {

        try {
            JSONObject jsonObjData = new JSONObject();
            JSONArray jsonArray = prepareDataForSyncLogUpload();
            if (jsonArray.length() > 0)
                jsonObjData.put(DataMembers.tbl_SyncLogDetails, jsonArray);

            JSONFormatter jsonFormatter = generateUploadHeaderInformation(mContext, jsonObjData);

            String url = businessModel.synchronizationHelper.getUploadUrl(UPLOAD_TRANSACTION_URL_CODE);

            if (BuildConfig.FLAVOR.equalsIgnoreCase("aws")) {
                int response = getUploadResponseStatus(mContext, jsonObjData, jsonFormatter, url).value;
                if (response == 1)
                    updateFlagSyncLogDetails();
            } else {
                final JSONObject finalJsonObjData = jsonObjData;
                businessModel.synchronizationHelper.getAuthToken(new SynchronizationHelper.VolleyResponseCallbackInterface() {
                    @Override
                    public String onSuccess(String result) {
                        uploadSyncLogSFDC(finalJsonObjData);
                        return "";
                    }

                    @Override
                    public String onFailure(String errorresult) {
                        return "";
                    }
                });

            }
        } catch (Exception e) {
            Commons.printException(e);
        }


    }

    /**
     * Method to upload synclogdetails for SFDC
     *
     * @param data - Prepared Json data contains synclogdetails column and values
     */
    private void uploadSyncLogSFDC(final JSONObject data) {

        MyjsonarrayPostRequest jsonObjectRequest;

        String uniqueTransactionID = businessModel.getAppDataProvider().getUser().getUserid()
                + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);


        try {
            data.put("MessageId", uniqueTransactionID);
            HttpsURLConnection.setDefaultSSLSocketFactory(new TLSSocketFactory());

        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();
        sb.append(instance_url);

        sb.append(businessModel.synchronizationHelper.getUploadUrl("UPLDTRAN"));
        try {

            jsonObjectRequest = new MyjsonarrayPostRequest(
                    Request.Method.POST, sb.toString(), data,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray jsonObject) {
                            Commons.print(" RES: " + jsonObject);
                            System.gc();

                            try {

                                int response = 0;
                                if (jsonObject.toString().contains("Response")) {
                                    response = jsonObject.getJSONObject(0).getInt("Response");
                                }

                                if (response == 1) {
                                    updateFlagSyncLogDetails();
                                }

                            } catch (JSONException e) {
                                handler.sendEmptyMessage(
                                        DataMembers.NOTIFY_UPLOAD_ERROR);
                            }

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    System.gc();

                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "OAuth " + access_token);
                    headers.put("Content_Type", "application/json");
                    if (businessModel.synchronizationHelper.isDayClosed()) {
                        int varianceDwnDate = DateTimeUtils.compareDate(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                                businessModel.getAppDataProvider().getUser().getDownloadDate(),
                                "yyyy/MM/dd");
                        if (varianceDwnDate == 0) {
                            headers.put("MobileDate",
                                    DateTimeUtils.now(DateTimeUtils.DATE_TIME_NEW));
                        }
                        if (varianceDwnDate > 0) {
                            headers.put("MobileDate",
                                    businessModel.synchronizationHelper.getLastTransactedDate());
                        }
                    }

                    return headers;
                }

                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    //Map<String, String> params = new HashMap<String, String>();
                    return new HashMap<>();
                }

            };

            RetryPolicy policy = new DefaultRetryPolicy(
                    (int) TimeUnit.SECONDS.toMillis(30),
                    0,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            jsonObjectRequest.setRetryPolicy(policy);
            jsonObjectRequest.setShouldCache(false);

            addToRequestQueue(jsonObjectRequest, "");

        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    /**
     * Method to prepare Json data for SyncLog upload
     *
     * @return Json array contains SyncLogDetails table columns and values
     */
    private JSONArray prepareDataForSyncLogUpload() {

        JSONArray jsonArray = new JSONArray();

        try {
            DBUtil db = new DBUtil(mContext.getApplicationContext(), DataMembers.DB_NAME);
            db.createDataBase();
            db.openDataBase();
            Cursor cursor;
            String columnArray[] = DataMembers.tbl_SyncLogDetails_cols.split(",");
            String sql = "select " + DataMembers.tbl_SyncLogDetails_cols + " from " + DataMembers.tbl_SyncLogDetails
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
                        jsonArray.put(jsonObjRow);
                    }
                }

                cursor.close();

            }
        } catch (Exception e) {
            Commons.printException(e);
        }
        return jsonArray;
    }

    /**
     * Method to update upload flag for synclogdetails table
     */
    private void updateFlagSyncLogDetails() {
        try {
            DBUtil db = new DBUtil(mContext.getApplicationContext(), DataMembers.DB_NAME);
            db.createDataBase();
            db.openDataBase();
            String query = "update synclogdetails set upload='Y' where upload='N'";
            db.updateSQL(query);
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }


    /**
     * Check data is in Transaction sequence Table
     *
     * @return true /false;
     */
    public boolean hasTransactionSequenceDataExist() {
        DBUtil db = null;
        boolean istransaction = false;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.openDataBase();

            Cursor c = db.selectSQL("SELECT TypeID FROM TransactionSequence WHERE Upload = 'N'");

            if (c.getCount() > 0) {
                if (c.moveToNext()) {
                    istransaction = true;
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            db.closeDB();
            Commons.printException(e);
        }

        return istransaction;
    }

    /**
     * Check for data in Salable and Non-salable Stock in Hand
     * @return true or false
     */
    public boolean hasStockInHandDataExist() {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
        boolean hasData = false;
        try {
            db.openDataBase();
            String sql;
            Cursor c;

            sql = "select count(*) from stockinhandmaster where upload='N' " +
                    "union select count(*) from NonSalableSIHMaster where upload='N'";

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
     * Check for data in Stock Apply Table.
     * @return true or false
     */
    public boolean hasStockApplyDateExisit() {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
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


    /**
     * checkDataForSync is used to check wheather DB has any unsubmitted data or
     * not.
     *
     * @return true if data exist or false
     */
    public boolean checkDataForSync() {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
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
                    "union select count(Tid) from ModuleActivityDetails where upload='N'" +
                    "union select count(uid) from AttendanceTimeDetails where upload='N'" +
                    "union select count(UID) from NonFieldActivity where upload='N'" +
                    "union select count(Tid) from DisplaySchemeEnrollmentHeader where upload='N'" +
                    "union select count(Tid) from DisplaySchemeTrackingHeader where upload='N'" +
                    "union select count(PlanId) from DatewisePlan where upload='N'" +
                    "union select count(KPIId) from RetailerKPIModifiedDetail where upload='N'" +
                    "union select count(uid) from VanLoad where upload='N'" +
                    "union select count(Uid) from JointCallDetail where upload='N'" +
                    "union select count(Tid) from RetailerScoreHeader where upload='N'" +
                    "union select count(Tid) from RetailerScoreDetails where upload='N'" +
                    "union select count(uid) from DenominationHeader where upload='N'" +
                    "union select count(uid) from DenominationDetails where upload='N'" +
                    "union select count(Tid) from RetailerLocationDeviation where upload='N'" +
                    "union select count(uid) from DisplayAssetTrackingHeader where upload='N'" +
                    "union select count(uid) from DisplayAssetTrackingDetails where upload='N'" +
                    "union select count(RetailerId) from RetailerNotes where upload='N'" +
                    "union select count(RetailerId) from SerializedAssetServiceRequest where upload='N'";
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

}
