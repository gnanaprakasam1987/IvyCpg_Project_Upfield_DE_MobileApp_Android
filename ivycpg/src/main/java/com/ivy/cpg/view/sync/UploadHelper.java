package com.ivy.cpg.view.sync;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.amazonaws.util.StringInputStream;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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
import com.ivy.utils.AppUtils;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.DeviceUtils;
import com.ivy.utils.network.TLSSocketFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
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
    private int responseMessage;
    private Handler handler;
    private StringBuilder mVisitedRetailerIds;

    private Context mContext;

    private RequestQueue mRequestQueue;

    private static final String TAG = "UploadHelper";


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
    public int uploadUsingHttp(final Handler handlerr, final int flag, Context context) {
        responseMessage = 0;
        handler = handlerr;
        try {

            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME);
            db.createDataBase();
            db.openDataBase();

            JSONObject jsonObjData = new JSONObject();
            String initTime = DateTimeUtils.now(DateTimeUtils.DATE_TIME_NEW);
            if (flag == DataMembers.SYNCUPLOAD || flag == DataMembers.SYNC_EXPORT) {
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
            } else if (flag == DataMembers.SYNC_ORDER_DELIVERY_STATUS_UPLOAD) { // Delivered order realtime sync
                Set<String> keys = DataMembers.uploadOrderDeliveryStatusTable.keySet();

                jsonObjData = new JSONObject();
                for (String tableName : keys) {
                    JSONArray jsonArray = prepareDataForUploadJSON(db,
                            handlerr, tableName,
                            DataMembers.uploadOrderDeliveryStatusTable.get(tableName));

                    if (jsonArray.length() > 0)
                        jsonObjData.put(tableName, jsonArray);
                }
                Commons.print("jsonObjData.toString():3:"
                        + jsonObjData.toString());
            }
            else if (flag == DataMembers.SYNCPICKLISTUPLOAD) { // Pick List

                Set<String> keys = DataMembers.uploadPickListStatusTable.keySet();

                jsonObjData = new JSONObject();
                for (String tableName : keys) {
                    JSONArray jsonArray = prepareDataForUploadJSON(db,
                            handlerr, tableName,
                            DataMembers.uploadPickListStatusTable.get(tableName));

                    if (jsonArray.length() > 0)
                        jsonObjData.put(tableName, jsonArray);
                }
                Commons.print("jsonObjData.toString():3:"
                        + jsonObjData.toString());
            }
            else if (flag == DataMembers.SYNC_TRIP) { // Pick List

                Set<String> keys = DataMembers.uploadTripTable.keySet();

                jsonObjData = new JSONObject();
                for (String tableName : keys) {
                    JSONArray jsonArray = prepareDataForUploadJSON(db,
                            handlerr, tableName,
                            DataMembers.uploadTripTable.get(tableName));

                    if (jsonArray.length() > 0)
                        jsonObjData.put(tableName, jsonArray);
                }
                Commons.print("jsonObjData.toString():3:"
                        + jsonObjData.toString());
            }


            if (businessModel.configurationMasterHelper.SHOW_DATA_UPLOAD_STATUS) {
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

            JSONFormatter jsonFormatter = new JSONFormatter("HeaderInformation");

            Commons.print("jsonObjData.toString()" + jsonObjData);
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
                jsonFormatter.addParameter("DataValidationKey", businessModel.synchronizationHelper.generateChecksum(jsonObjData.toString()));
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
                    if (businessModel.synchronizationHelper.isExternalStorageAvailable()) {
                        folder = new File(Environment
                                .getExternalStorageDirectory().getPath()
                                + "/IVYData/");
                        if (!folder.exists()) {
                            folder.mkdirs();
                        }
                        SimpleDateFormat sdf = new SimpleDateFormat(
                                "yyyyMMdd_HHmmss", Locale.ENGLISH);
                        file = new File(folder, "Orderdetails_"
                                + businessModel.userMasterHelper.getUserMasterBO()
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
                        updateUploadFlag(DataMembers.uploadColumn, context.getApplicationContext());
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
                url = businessModel.synchronizationHelper.getUploadUrl("UPLDSIH");
                if (url.length() == 0) {
                    responseMessage = 2;
                    return responseMessage;
                }
            } else if (flag == DataMembers.SYNCSTKAPPLYUPLOAD) {
                url = businessModel.synchronizationHelper.getUploadUrl("UPLDSTOK");
                if (url.length() == 0) {
                    responseMessage = 2;
                    return responseMessage;
                }
            } else if (flag == DataMembers.SYNC_REALLOC_UPLOAD) {
                url = businessModel.synchronizationHelper.getUploadUrl("UPLDALLOC");
                if (url.length() == 0) {
                    responseMessage = 1;
                    return responseMessage;
                }
            } else if (flag == DataMembers.SYNCLYTYPTUPLOAD) {
                url = businessModel.synchronizationHelper.getUploadUrl("UPLDLOYALTY");
                if (url.length() == 0) {
                    responseMessage = 2;
                    return responseMessage;
                }
            } else if (flag == DataMembers.SYNC_ORDER_DELIVERY_STATUS_UPLOAD) {
                url = businessModel.synchronizationHelper.getUploadUrl("UPLDORDDELSTS");
                if (url.length() == 0) {
                    responseMessage = 2;
                    return responseMessage;
                }
            } else if (flag == DataMembers.SYNCPICKLISTUPLOAD) {
                url = businessModel.synchronizationHelper.getUploadUrl("UPLDDELIVERYSTS");
                if (url.length() == 0) {
                    responseMessage = 2;
                    return responseMessage;
                }
            }else if (flag == DataMembers.SYNC_TRIP) {
                url = businessModel.synchronizationHelper.getUploadUrl("UPLOADTRIP");
                if (url.length() == 0) {
                    responseMessage = 2;
                    return responseMessage;
                }
            } else
                url = businessModel.synchronizationHelper.getUploadUrl("UPLDTRAN");




            if(BuildConfig.FLAVOR.equalsIgnoreCase("aws")){
                int response = getResponseFlag(context, jsonObjData, jsonFormatter, url);

                String syncStatus;
                if (response == -1)
                    return response;

                responseMessage = handleUploadresponse(flag, context, response);
                if (responseMessage == 1) {
                    syncStatus = SynchronizationHelper.SYNC_STATUS_COMPLETED;
                } else {
                    syncStatus = SynchronizationHelper.SYNC_STATUS_FAILED;
                }
                businessModel.synchronizationHelper.insertSyncHeader(initTime, DateTimeUtils.now(DateTimeUtils.DATE_TIME_NEW), SynchronizationHelper.SYNC_TYPE_UPLOAD,
                        0, syncStatus,  1);
            }else {
                final JSONObject finalJsonObjData = jsonObjData;
                businessModel.synchronizationHelper.getAuthToken(new SynchronizationHelper.VolleyResponseCallbackInterface() {
                    @Override
                    public String onSuccess(String result) {
                        getUploadResponseSFDC(finalJsonObjData,flag,handlerr);
                        return "";
                    }

                    @Override
                    public String onFailure(String errorresult) {
                        return "";
                    }
                });

            }


            Commons.print("After Responce");
            // Upload Transaction Sequence Table Separate , the above method
            // successfully upload. This Method also doing same work, but server
            // need the data while replicate this data while download instantly.
            if ((businessModel.configurationMasterHelper.SHOW_ORDER_SEQUENCE_NO || businessModel.configurationMasterHelper.SHOW_INVOICE_SEQUENCE_NO
                    || businessModel.configurationMasterHelper.SHOW_COLLECTION_SEQ_NO)
                    && businessModel.orderAndInvoiceHelper.hasTransactionSequence()) {
                if (responseMessage == 1) {
                    responseMessage = uploadInvoiceSequenceNo(this.handler, context.getApplicationContext());
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
        return responseMessage;
    }

    private String uniqueTransactionID;

    private void getUploadResponseSFDC(final JSONObject data, final int flag, final Handler handler) {
        // Update Security key
        //updateAuthenticateToken();
        /*StringBuilder url = new StringBuilder();
        url.append(DataMembers.SERVER_URL);
        url.append(appendurl);*/
        String initTime = DateTimeUtils.now(DateTimeUtils.DATE_TIME_NEW);
        Commons.print("Json data " + data);
        MyjsonarrayPostRequest jsonObjectRequest;

        uniqueTransactionID = businessModel.userMasterHelper.getUserMasterBO().getUserid()
                + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);


        try {
            data.put("MessageId",uniqueTransactionID);
            HttpsURLConnection.setDefaultSSLSocketFactory(new TLSSocketFactory());

        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }catch (JSONException ex){
            ex.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();
        sb.append(instance_url);

        //sb.append("/");
        sb.append(businessModel.synchronizationHelper.getUploadUrl("UPLDTRAN"));
        try {

            jsonObjectRequest = new MyjsonarrayPostRequest(
                    Request.Method.POST, sb.toString(),data,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray jsonObject) {
                            Commons.print(" RES: " + jsonObject);
                            System.gc();

                            try {

                                int response = 0;
                                if (jsonObject.toString().contains("Response")) {
                                    response= jsonObject.getJSONObject(0).getInt("Response");
                                }

                                int responseMsg = 0;
                                if (response == 1) {

                                    if (flag == DataMembers.SYNCUPLOADRETAILERWISE) {
                                        updateUploadFlagRetailerWise(mContext);
                                        getVisitedRetailerIds().delete(0,
                                                getVisitedRetailerIds().length());
                                        responseMsg = 1;
                                    } else if (flag == DataMembers.SYNCSIHUPLOAD) {
                                        updateUploadFlag(DataMembers.uploadSIHTable,mContext);

                                        responseMsg = 2;
                                    } else if (flag == DataMembers.SYNCLYTYPTUPLOAD) {
                                        updateUploadFlag(DataMembers.uploadLPTable,mContext);

                                        responseMsg = 2;
                                    } else if (flag == DataMembers.SYNCSTKAPPLYUPLOAD) {

                                        updateUploadFlag(DataMembers.uploadStockApplyTable,mContext);
                                        responseMsg = 2;
                                    } else if (flag == DataMembers.SYNC_REALLOC_UPLOAD) {
                                        updateUploadFlag(DataMembers.uploadReallocTable,mContext);
                                        responseMsg = 1;
                                    } else if (flag == DataMembers.ATTENDANCE_UPLOAD) {
                                        updateUploadFlag(DataMembers.uploadAttendanceColumn,mContext);
                                        responseMsg = 1;
                                    } else {
                                        updateUploadFlag(DataMembers.uploadColumn,mContext);
                                        responseMsg = 1;
                                    }

                                } else if (response == 0) {
                                    if (DataMembers.SYNCUPLOADRETAILERWISE == 1) {
                                        getVisitedRetailerIds().delete(0,
                                                getVisitedRetailerIds().length());
                                        responseMsg = 0;
                                    }
                                }else if(response==2){
                                    updateProgress(flag,handler);
                                    responseMsg = 3;
                                }
                                // Upload Transaction Sequence Table Separate , the above method
                                // successfully upload. This Method also doing same work, but server
                                // need the data while replicate this data while download instantly.
                                if ((businessModel.configurationMasterHelper.SHOW_ORDER_SEQUENCE_NO || businessModel.configurationMasterHelper.SHOW_INVOICE_SEQUENCE_NO
                                        || businessModel.configurationMasterHelper.SHOW_COLLECTION_SEQ_NO)
                                        && businessModel.orderAndInvoiceHelper.hasTransactionSequence()) {
                                    if (responseMsg == 1) {
                                        responseMsg = uploadInvoiceSequenceNo(handler,mContext);
                                    }
                                }
                                Commons.print("After Responce");

                                String syncStatus;
                                if (responseMsg == 1) {
                                    syncStatus = SYNC_STATUS_COMPLETED;
                                } else {
                                    syncStatus = SYNC_STATUS_FAILED;
                                }
                                businessModel.synchronizationHelper.insertSyncHeader(initTime, DateTimeUtils.now(DateTimeUtils.DATE_TIME_NEW), SynchronizationHelper.SYNC_TYPE_UPLOAD,
                                        0, syncStatus,  1);

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

                    Commons.print("AuthFailureError 5");
                    System.gc();
                    handler.sendEmptyMessage(
                            DataMembers.NOTIFY_UPLOAD_ERROR);
                    businessModel.synchronizationHelper.insertSyncHeader(initTime, DateTimeUtils.now(DateTimeUtils.DATE_TIME_NEW), SynchronizationHelper.SYNC_TYPE_UPLOAD,
                            0, SYNC_STATUS_FAILED,  1);
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "OAuth " + access_token);
                    headers.put("Content_Type", "application/json");
                    if (businessModel.synchronizationHelper.isDayClosed()) {
                        /*int varianceDwnDate = DateTimeUtils.compareDate(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                                businessModel.userMasterHelper.getUserMasterBO().getDownloadDate(),
                                "yyyy/MM/dd");*/
                       /* if (varianceDwnDate == 0) {
                            headers.put("MobileDate",
                                    Utils.getDate("yyyy/MM/dd HH:mm:ss"));
                        }
                        if (varianceDwnDate > 0) {
                            headers.put("MobileDate",
                                    businessModel.synchronizationHelper.getLastTransactedDate());
                        }*/
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(businessModel.getApplicationContext());
                        String tripExtendedDate = sharedPreferences.getString("tripExtendedDate", "");
                        if(!tripExtendedDate.equals("")){
                            headers.put("MobileDate",DateTimeUtils.convertFromServerDateToRequestedFormat(tripExtendedDate,"yyyy/MM/dd HH:mm:ss"));
                        }
                        else {
                            LoadManagementHelper loadManagementHelper=LoadManagementHelper.getInstance(mContext);
                            String startedDate=loadManagementHelper.getTripStartedDate(mContext);

                            String date=!startedDate.equals("")?DateTimeUtils.convertFromServerDateToRequestedFormat(startedDate,"yyyy/MM/dd HH:mm:ss"):Utils.getDate("yyyy/MM/dd HH:mm:ss");
                            headers.put("MobileDate",date);
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



    private void updateProgress( final int flag, final Handler handler){
        try {

            final Handler handlerNew = new Handler();
            handlerNew.postDelayed(new Runnable() {
                public void run() {
                    getCheckUploadedResponseFromSFDC(flag,handler);
                }
            }, businessModel.configurationMasterHelper.VALUE_SYNC_PROGRESS_TIME);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getCheckUploadedResponseFromSFDC(final int flag, final Handler handler) {


        JsonObjectRequest jsonObjectRequest;



        try {
            HttpsURLConnection.setDefaultSSLSocketFactory(new TLSSocketFactory());

        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();
        sb.append(instance_url);

        //sb.append("/");
        sb.append(businessModel.synchronizationHelper.getUploadUrl("UPLDSTS"));
        try {

            jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.GET, sb.toString(),new JSONObject(),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {
                            Commons.print(" RES: " + jsonObject);
                            System.gc();
                            try {

                                int response = 0;
                                if (jsonObject.toString().contains("Data")) {
                                    JSONArray first = jsonObject.getJSONArray(JSON_DATA_KEY);
                                    for (int j = 0; j < first.length(); j++) {
                                        JSONArray value = (JSONArray) first.get(j);
                                        response=value.getInt(0);
                                    }

                                }

                                int responseMsg = 0;
                                if (response == 1) {

                                    if (flag == DataMembers.SYNCUPLOADRETAILERWISE) {
                                        updateUploadFlagRetailerWise(mContext);
                                        getVisitedRetailerIds().delete(0,
                                                getVisitedRetailerIds().length());
                                        responseMsg = 1;
                                    } else if (flag == DataMembers.SYNCSIHUPLOAD) {
                                        updateUploadFlag(DataMembers.uploadSIHTable,mContext);

                                        responseMsg = 2;
                                    } else if (flag == DataMembers.SYNCLYTYPTUPLOAD) {
                                        updateUploadFlag(DataMembers.uploadLPTable,mContext);

                                        responseMsg = 2;
                                    } else if (flag == DataMembers.SYNCSTKAPPLYUPLOAD) {

                                        updateUploadFlag(DataMembers.uploadStockApplyTable,mContext);
                                        responseMsg = 2;
                                    } else if (flag == DataMembers.SYNC_REALLOC_UPLOAD) {
                                        updateUploadFlag(DataMembers.uploadReallocTable,mContext);
                                        responseMsg = 1;
                                    } else if (flag == DataMembers.ATTENDANCE_UPLOAD) {
                                        updateUploadFlag(DataMembers.uploadAttendanceColumn,mContext);
                                        responseMsg = 1;
                                    } else {
                                        updateUploadFlag(DataMembers.uploadColumn,mContext);
                                        responseMsg = 1;
                                    }

                                } else if (response == 0) {
                                    if (DataMembers.SYNCUPLOADRETAILERWISE == 1) {
                                        getVisitedRetailerIds().delete(0,
                                                getVisitedRetailerIds().length());
                                        responseMsg = 0;
                                    }
                                }else if(response==2){
                                    updateProgress(flag,handler);
                                    responseMsg = 3;
                                }
                                // Upload Transaction Sequence Table Separate , the above method
                                // successfully upload. This Method also doing same work, but server
                                // need the data while replicate this data while download instantly.
                                if ((businessModel.configurationMasterHelper.SHOW_ORDER_SEQUENCE_NO || businessModel.configurationMasterHelper.SHOW_INVOICE_SEQUENCE_NO
                                        || businessModel.configurationMasterHelper.SHOW_COLLECTION_SEQ_NO)
                                        && businessModel.orderAndInvoiceHelper.hasTransactionSequence()) {
                                    if (responseMsg == 1) {
                                        responseMsg = uploadInvoiceSequenceNo(handler,mContext);
                                    }
                                }
                                Commons.print("After Responce");

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

                            }

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Commons.print("AuthFailureError 5");
                    System.gc();
                    handler.sendEmptyMessage(
                            DataMembers.NOTIFY_UPLOAD_ERROR);

                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "OAuth " + access_token);
                    headers.put("MessageId",uniqueTransactionID);
                    headers.put("Content_Type", "application/json");

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

    private int getResponseFlag(Context context, JSONObject jsonObjData, JSONFormatter jsonFormatter, String url) throws JSONException {

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
                            return -1;

                        }

                    }

                }
            }
            return response;
        } else {
            if (!businessModel.synchronizationHelper.getAuthErroCode().equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
                String errorMsg = businessModel.synchronizationHelper.getErrormessageByErrorCode().get(businessModel.synchronizationHelper.getAuthErroCode());
                if (errorMsg != null) {
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, context.getResources().getString(R.string.data_not_downloaded), Toast.LENGTH_SHORT).show();
                }
            }
            return response;
        }
    }

    private int handleUploadresponse(int flag, Context context, int response) {

        int responseMsg = 0;
        if (response == 1) {

            if (flag == DataMembers.SYNCUPLOADRETAILERWISE) {
                updateUploadFlagRetailerWise(context.getApplicationContext());
                getVisitedRetailerIds().delete(0,
                        getVisitedRetailerIds().length());
                responseMsg = 1;
            } else if (flag == DataMembers.SYNCSIHUPLOAD) {
                updateUploadFlag(DataMembers.uploadSIHTable, context.getApplicationContext());

                responseMsg = 2;
            } else if (flag == DataMembers.SYNCLYTYPTUPLOAD) {
                updateUploadFlag(DataMembers.uploadLPTable, context.getApplicationContext());

                responseMsg = 2;
            } else if (flag == DataMembers.SYNCSTKAPPLYUPLOAD) {
                updateUploadFlag(DataMembers.uploadStockApplyTable, context.getApplicationContext());
                responseMsg = 2;
            } else if (flag == DataMembers.SYNC_REALLOC_UPLOAD) {
                updateUploadFlag(DataMembers.uploadReallocTable, context.getApplicationContext());
                responseMsg = 1;
            } else if (flag == DataMembers.ATTENDANCE_UPLOAD) {
                updateUploadFlag(DataMembers.uploadAttendanceColumn, context.getApplicationContext());
                responseMsg = 1;
            } else if (flag == DataMembers.SYNC_ORDER_DELIVERY_STATUS_UPLOAD) {
                updateUploadFlag(DataMembers.uploadOrderDeliveryStatusTable, context.getApplicationContext());
                responseMsg = 2;
            } else if (flag == DataMembers.SYNCPICKLISTUPLOAD) {
                updateUploadFlag(DataMembers.uploadPickListStatusTable, context.getApplicationContext());
                responseMsg = 2;
            }else if (flag == DataMembers.SYNC_TRIP) {
                updateUploadFlag(DataMembers.uploadTripTable, context.getApplicationContext());
                responseMsg = 2;
            }else {
                updateUploadFlag(DataMembers.uploadColumn, context.getApplicationContext());
                responseMsg = 1;
            }
        } else if (response == 0) {
            if (DataMembers.SYNCUPLOADRETAILERWISE == 1) {
                getVisitedRetailerIds().delete(0,
                        getVisitedRetailerIds().length());
                responseMsg = 0;
            }
        }

        return responseMsg;
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
    private int uploadInvoiceSequenceNo(Handler handler, Context context) {

        responseMessage = 0;
        JSONObject jsonObjData;
        this.handler = handler;
        try {
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME
            );
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
                jsonFormatter.addParameter("MobileDateTime",
                        Utils.getDate("yyyy/MM/dd HH:mm:ss"));
                jsonFormatter.addParameter("MobileUTCDateTime",
                        Utils.getGMTDateTime("yyyy/MM/dd HH:mm:ss"));
                jsonFormatter.addParameter("DownloadedDataDate",
                        businessModel.userMasterHelper.getUserMasterBO().getDownloadDate());
                jsonFormatter.addParameter("VanId", businessModel.userMasterHelper
                        .getUserMasterBO().getVanId());
                String LastDayClose = "";
                if (businessModel.synchronizationHelper.isDayClosed()) {
                    LastDayClose = businessModel.userMasterHelper.getUserMasterBO()
                            .getDownloadDate();
                }
                jsonFormatter.addParameter("LastDayClose", LastDayClose);
                jsonFormatter.addParameter("DataValidationKey", businessModel.synchronizationHelper.generateChecksum(jsonObjData.toString()));
                Commons.print(jsonFormatter.getDataInJson());
            } catch (Exception e) {
                Commons.printException("" + e);
            }

            String url = businessModel.synchronizationHelper.getUploadUrl("UPLDSEQ");
            if (url.length() == 0)
                return 1;
            Vector<String> responseVector = businessModel.synchronizationHelper.getUploadResponse(jsonFormatter.getDataInJson(),

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
            } else {
                if (!businessModel.synchronizationHelper.getAuthErroCode().equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
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
                responseMessage = 1;

            } else if (response == 0) {
                responseMessage = 0;
            }
            db.closeDB();
            Commons.print("uploadInvoiceSequenceNo:After Responce");
        } catch (Exception e) {
            Commons.printException("" + e);
            return 0;
        }
        return responseMessage;
    }

    public String uploadNewOutlet(Handler handler, Context context, String retailerID) {
        String rid = "";
        try {
            this.handler = handler;
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            JSONObject jsonobj = new JSONObject();

            Set<String> keys = DataMembers.uploadNewRetailerColumn.keySet();

            for (String tableName : keys) {
                JSONArray jsonArray = prepareDataForNewRetailerJSONUpload(db,
                        handler, tableName,
                        DataMembers.uploadNewRetailerColumn.get(tableName), retailerID);

                if (jsonArray.length() > 0)
                    jsonobj.put(tableName, jsonArray);
            }

            Commons.print("jsonObjData.toString():0:" + jsonobj.toString());


            JSONFormatter jsonFormatter = new JSONFormatter("HeaderInformation");

            jsonFormatter.addParameter("DeviceId",
                    DeviceUtils.getIMEINumber(context));
            jsonFormatter.addParameter("LoginId", businessModel.userMasterHelper
                    .getUserMasterBO().getLoginName());
            jsonFormatter.addParameter("VersionCode",
                    businessModel.getApplicationVersionNumber());
            jsonFormatter.addParameter(SynchronizationHelper.VERSION_NAME, businessModel.getApplicationVersionName());
            jsonFormatter.addParameter("DistributorId", businessModel.userMasterHelper
                    .getUserMasterBO().getDistributorid());
            jsonFormatter.addParameter("OrganisationId", businessModel.userMasterHelper
                    .getUserMasterBO().getOrganizationId());
            jsonFormatter.addParameter("MobileDateTime",
                    Utils.getDate("yyyy/MM/dd HH:mm:ss"));
            jsonFormatter.addParameter("MobileUTCDateTime",
                    Utils.getGMTDateTime("yyyy/MM/dd HH:mm:ss"));
            if (!"0".equals(businessModel.userMasterHelper.getUserMasterBO().getBackupSellerID())) {
                jsonFormatter.addParameter("UserId", businessModel.userMasterHelper
                        .getUserMasterBO().getBackupSellerID());
                jsonFormatter.addParameter("WorkingFor", businessModel.userMasterHelper.getUserMasterBO().getUserid());
            } else {
                jsonFormatter.addParameter("UserId", businessModel.userMasterHelper
                        .getUserMasterBO().getUserid());
            }
            jsonFormatter.addParameter("VanId", businessModel.userMasterHelper
                    .getUserMasterBO().getVanId());
            String LastDayClose = "";
            if (businessModel.synchronizationHelper.isDayClosed()) {
                LastDayClose = businessModel.userMasterHelper.getUserMasterBO()
                        .getDownloadDate();
            }
            jsonFormatter.addParameter("LastDayClose", LastDayClose);
            jsonFormatter.addParameter("BranchId", businessModel.userMasterHelper
                    .getUserMasterBO().getBranchId());
            jsonFormatter.addParameter("DownloadedDataDate", businessModel.userMasterHelper
                    .getUserMasterBO().getDownloadDate());
            jsonFormatter.addParameter("DataValidationKey", businessModel.synchronizationHelper.generateChecksum(jsonobj.toString()));
            Commons.print(jsonFormatter.getDataInJson());
            String appendurl = businessModel.synchronizationHelper.getUploadUrl("UPLDRET");
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
            JSONObject jsonobj = new JSONObject();


            JSONObject jObject = new JSONObject();
            jObject.put("UserId", backupSellerId);
            jObject.put("ReplacementUser", businessModel.userMasterHelper.getUserMasterBO().getUserid());
            jObject.put("Date", Utils.getDate("yyyy/MM/dd"));
            jsonobj.put("UserReplacement", jObject);


            Commons.print("jsonObjData.toString():0:" + jsonobj.toString());


            JSONFormatter jsonFormatter = new JSONFormatter("HeaderInformation");

            jsonFormatter.addParameter("DeviceId",
                    DeviceUtils.getIMEINumber(mContext));
            jsonFormatter.addParameter("LoginId", businessModel.userMasterHelper
                    .getUserMasterBO().getLoginName());
            jsonFormatter.addParameter("VersionCode",
                    businessModel.getApplicationVersionNumber());
            jsonFormatter.addParameter(SynchronizationHelper.VERSION_NAME, businessModel.getApplicationVersionName());
            jsonFormatter.addParameter("DistributorId", businessModel.userMasterHelper
                    .getUserMasterBO().getDistributorid());
            jsonFormatter.addParameter("OrganisationId", businessModel.userMasterHelper
                    .getUserMasterBO().getOrganizationId());
            jsonFormatter.addParameter("MobileDateTime",
                    Utils.getDate("yyyy/MM/dd HH:mm:ss"));
            jsonFormatter.addParameter("MobileUTCDateTime",
                    Utils.getGMTDateTime("yyyy/MM/dd HH:mm:ss"));
            if (!"0".equals(businessModel.userMasterHelper.getUserMasterBO().getBackupSellerID())) {
                jsonFormatter.addParameter("UserId", businessModel.userMasterHelper
                        .getUserMasterBO().getBackupSellerID());
                jsonFormatter.addParameter("WorkingFor", businessModel.userMasterHelper.getUserMasterBO().getUserid());
            } else {
                jsonFormatter.addParameter("UserId", businessModel.userMasterHelper
                        .getUserMasterBO().getUserid());
            }
            jsonFormatter.addParameter("VanId", businessModel.userMasterHelper
                    .getUserMasterBO().getVanId());
            String LastDayClose = "";
            if (businessModel.synchronizationHelper.isDayClosed()) {
                LastDayClose = businessModel.userMasterHelper.getUserMasterBO()
                        .getDownloadDate();
            }
            jsonFormatter.addParameter("LastDayClose", LastDayClose);
            jsonFormatter.addParameter("BranchId", businessModel.userMasterHelper
                    .getUserMasterBO().getBranchId());
            jsonFormatter.addParameter("DownloadedDataDate", businessModel.userMasterHelper
                    .getUserMasterBO().getDownloadDate());
            jsonFormatter.addParameter("DataValidationKey", businessModel.synchronizationHelper.generateChecksum(jsonobj.toString()));
            jsonFormatter.addParameter("WorkingFor", businessModel.userMasterHelper.getUserMasterBO().getBackupSellerID());
            Commons.print(jsonFormatter.getDataInJson());
            String appendurl = businessModel.synchronizationHelper.getUploadUrl("USRREPLACEUPLD");
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
                if (!businessModel.synchronizationHelper.getAuthErroCode().equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
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

            JSONFormatter jsonFormatter = new JSONFormatter("HeaderInformation");

            try {
                if (!"0".equals(businessModel.getAppDataProvider().getUser().getBackupSellerID())) {
                    jsonFormatter.addParameter("UserId", businessModel.getAppDataProvider().getUser().getBackupSellerID());
                    jsonFormatter.addParameter("WorkingFor", businessModel.getAppDataProvider().getUser().getUserid());
                } else {
                    jsonFormatter.addParameter("UserId", businessModel.getAppDataProvider().getUser().getUserid());
                }

                jsonFormatter.addParameter("DistributorId", businessModel.getAppDataProvider().getUser().getDistributorid());
                jsonFormatter.addParameter("BranchId", businessModel.getAppDataProvider().getUser().getBranchId());
                jsonFormatter.addParameter("LoginId", businessModel.getAppDataProvider().getUser().getLoginName());
                jsonFormatter.addParameter("DeviceId",
                        DeviceUtils.getIMEINumber(mContext));
                jsonFormatter.addParameter("VersionCode",
                        AppUtils.getApplicationVersionNumber(mContext));
                jsonFormatter.addParameter(SynchronizationHelper.VERSION_NAME, AppUtils.getApplicationVersionName(mContext));
                jsonFormatter.addParameter("OrganisationId", businessModel.getAppDataProvider().getUser().getOrganizationId());
                jsonFormatter.addParameter("ParentPositionIds", businessModel.getUserParentPosition());
                if (businessModel.synchronizationHelper.isDayClosed()) {
                    int varianceDwnDate = DateTimeUtils.compareDate(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                            businessModel.getAppDataProvider().getUser().getDownloadDate(),
                            DateTimeUtils.DateFormats.SERVER_DATE_FORMAT);
                    if (varianceDwnDate == 0) {
                        jsonFormatter.addParameter("MobileDateTime",
                                DateTimeUtils.now(DateTimeUtils.DATE_TIME_NEW));
                    }
                    if (varianceDwnDate > 0) {
                        jsonFormatter.addParameter("MobileDateTime",
                                businessModel.synchronizationHelper.getLastTransactedDate());
                    }
                } else
                    jsonFormatter.addParameter("MobileDateTime",
                            DateTimeUtils.now(DateTimeUtils.DATE_TIME_NEW));

                jsonFormatter.addParameter("MobileUTCDateTime",
                        DateTimeUtils.now(DateTimeUtils.GMT_DATE_TIME));
                jsonFormatter.addParameter("DownloadedDataDate",
                        businessModel.getAppDataProvider().getUser().getDownloadDate());
                jsonFormatter.addParameter("VanId", businessModel.getAppDataProvider().getUser().getVanId());
                jsonFormatter.addParameter("platform", "Android");
                jsonFormatter.addParameter("osversion",
                        android.os.Build.VERSION.RELEASE);
                jsonFormatter.addParameter("firmware", "");
                jsonFormatter.addParameter("model", Build.MODEL);
                String LastDayClose = "";
                if (businessModel.synchronizationHelper.isDayClosed()) {
                    LastDayClose = businessModel.getAppDataProvider().getUser()
                            .getDownloadDate();
                }
                jsonFormatter.addParameter("LastDayClose", LastDayClose);
                jsonFormatter.addParameter("DataValidationKey", businessModel.synchronizationHelper.generateChecksum(jsonObjData.toString()));

                String url = businessModel.synchronizationHelper.getUploadUrl("UPLDTRAN");

                if(BuildConfig.FLAVOR.equalsIgnoreCase("aws")){
                    int response = getResponseFlag(mContext, jsonObjData, jsonFormatter, url);
                    if (response == 1)
                    updateFlagSyncLogDetails();
                }else {
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
        } catch (JSONException jsonException) {
            Commons.printException(jsonException);


        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    /**
     * Method to upload synclogdetails for SFDC
     * @param data - Prepared Json data contains synclogdetails column and values
     */
    private void uploadSyncLogSFDC(final JSONObject data) {

        MyjsonarrayPostRequest jsonObjectRequest;

        uniqueTransactionID = businessModel.getAppDataProvider().getUser().getUserid()
                + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);


        try {
            data.put("MessageId",uniqueTransactionID);
            HttpsURLConnection.setDefaultSSLSocketFactory(new TLSSocketFactory());

        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }catch (JSONException ex){
            ex.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();
        sb.append(instance_url);

        sb.append(businessModel.synchronizationHelper.getUploadUrl("UPLDTRAN"));
        try {

            jsonObjectRequest = new MyjsonarrayPostRequest(
                    Request.Method.POST, sb.toString(),data,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray jsonObject) {
                            Commons.print(" RES: " + jsonObject);
                            System.gc();

                            try {

                                int response = 0;
                                if (jsonObject.toString().contains("Response")) {
                                    response= jsonObject.getJSONObject(0).getInt("Response");
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

}
