package com.ivy.cpg.view.sync;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.amazonaws.util.StringInputStream;
import com.ivy.lib.Utils;
import com.ivy.lib.existing.DBUtil;
import com.ivy.lib.rest.JSONFormatter;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;

import static com.ivy.lib.Utils.QT;

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


    private UploadHelper(Context context) {
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
            db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME, DataMembers.DB_PATH);
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


    /**
     * upload starts
     */
    public int uploadUsingHttp(Handler handlerr, final int flag, Context context) {
        responseMessage = 0;
        handler = handlerr;
        try {

            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME, DataMembers.DB_PATH);
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


            if (businessModel.configurationMasterHelper.SHOW_SYNC_INTERNAL_REPORT) {
                String id = SDUtil.now(SDUtil.DATE_TIME);
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
                        businessModel.activationHelper.getIMEINumber());
                jsonFormatter.addParameter("VersionCode",
                        businessModel.getApplicationVersionNumber());
                jsonFormatter.addParameter(SynchronizationHelper.VERSION_NAME, businessModel.getApplicationVersionName());
                jsonFormatter.addParameter("OrganisationId", businessModel.userMasterHelper
                        .getUserMasterBO().getOrganizationId());
                if (businessModel.synchronizationHelper.isDayClosed()) {
                    int varianceDwnDate = SDUtil.compareDate(SDUtil.now(SDUtil.DATE_GLOBAL),
                            businessModel.userMasterHelper.getUserMasterBO().getDownloadDate(),
                            "yyyy/MM/dd");
                    if (varianceDwnDate == 0) {
                        jsonFormatter.addParameter("MobileDate",
                                Utils.getDate("yyyy/MM/dd HH:mm:ss"));
                    }
                    if (varianceDwnDate > 0) {
                        jsonFormatter.addParameter("MobileDate",
                                businessModel.synchronizationHelper.getLastTransactedDate());
                    }
                } else
                    jsonFormatter.addParameter("MobileDate",
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
            } else if (flag == DataMembers.COUNTER_STOCK_APPLY_UPLOAD) {
                url = businessModel.synchronizationHelper.getUploadUrl("UPLDCSSTKRCPT");
                if (url.length() == 0) {
                    responseMessage = 2;
                    return responseMessage;
                }
            } else if (flag == DataMembers.COUNTER_SIH_UPLOAD) {
                url = businessModel.synchronizationHelper.getUploadUrl("UPLDCSSIH");
                if (url.length() == 0) {
                    responseMessage = 2;
                    return responseMessage;
                }
            } else if (flag == DataMembers.CS_REJECTED_VARIANCE_UPLOAD) {
                url = businessModel.synchronizationHelper.getUploadUrl("UPLDCSSTKVR");
                if (url.length() == 0) {
                    responseMessage = 2;
                    return responseMessage;
                }
            } else
                url = businessModel.synchronizationHelper.getUploadUrl("UPLDTRAN");

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

                if (flag == DataMembers.SYNCUPLOADRETAILERWISE) {
                    updateUploadFlagRetailerWise(context.getApplicationContext());
                    getVisitedRetailerIds().delete(0,
                            getVisitedRetailerIds().length());
                    responseMessage = 1;
                } else if (flag == DataMembers.SYNCSIHUPLOAD) {
                    updateUploadFlag(DataMembers.uploadSIHTable, context.getApplicationContext());

                    responseMessage = 2;
                } else if (flag == DataMembers.SYNCLYTYPTUPLOAD) {
                    updateUploadFlag(DataMembers.uploadLPTable, context.getApplicationContext());

                    responseMessage = 2;
                } else if (flag == DataMembers.SYNCSTKAPPLYUPLOAD) {
                    updateUploadFlag(DataMembers.uploadStockApplyTable, context.getApplicationContext());
                    responseMessage = 2;
                } else if (flag == DataMembers.SYNC_REALLOC_UPLOAD) {
                    updateUploadFlag(DataMembers.uploadReallocTable, context.getApplicationContext());
                    responseMessage = 1;
                } else if (flag == DataMembers.ATTENDANCE_UPLOAD) {
                    updateUploadFlag(DataMembers.uploadAttendanceColumn, context.getApplicationContext());
                    responseMessage = 1;
                } else if (flag == DataMembers.COUNTER_STOCK_APPLY_UPLOAD) {
                    updateUploadFlag(DataMembers.uploadCSStockApplyTable, context.getApplicationContext());
                    responseMessage = 2;
                } else if (flag == DataMembers.COUNTER_SIH_UPLOAD) {
                    updateUploadFlag(DataMembers.uploadCounterSIHTable, context.getApplicationContext());
                    responseMessage = 2;
                } else if (flag == DataMembers.CS_REJECTED_VARIANCE_UPLOAD) {
                    updateUploadFlag(DataMembers.uploadCSRejectedVarianceStatus, context.getApplicationContext());
                    responseMessage = 2;
                } else {
                    updateUploadFlag(DataMembers.uploadColumn, context.getApplicationContext());
                    responseMessage = 1;
                }
            } else if (response == 0) {
                if (DataMembers.SYNCUPLOADRETAILERWISE == 1) {
                    getVisitedRetailerIds().delete(0,
                            getVisitedRetailerIds().length());
                    responseMessage = 0;
                }
            }
            Commons.print("After Responce");
            // Upload Transaction Sequence Table Separate , the above method
            // successfully upload. This Method also doing same work, but server
            // need the data while replicate this data while download instantly.
            if ((businessModel.configurationMasterHelper.SHOW_INVOICE_SEQUENCE_NO || businessModel.configurationMasterHelper.SHOW_COLLECTION_SEQ_NO)
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


    public StringBuilder getVisitedRetailerIds() {
        return mVisitedRetailerIds;
    }

    public void setVisitedRetailerIds(StringBuilder mVisitedRetailerIds) {
        this.mVisitedRetailerIds = mVisitedRetailerIds;
    }

    private void updateUploadFlagRetailerWise(Context context) {
        try {
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
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
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME,
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
    private int uploadInvoiceSequenceNo(Handler handler, Context context) {

        responseMessage = 0;
        JSONObject jsonObjData;
        this.handler = handler;
        try {
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME,
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
                        businessModel.activationHelper.getIMEINumber());
                jsonFormatter.addParameter("VersionCode",
                        businessModel.getApplicationVersionNumber());
                jsonFormatter.addParameter(SynchronizationHelper.VERSION_NAME, businessModel.getApplicationVersionName());
                jsonFormatter.addParameter("OrganisationId", businessModel.userMasterHelper
                        .getUserMasterBO().getOrganizationId());
                jsonFormatter.addParameter("MobileDate",
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

            String url = businessModel.synchronizationHelper.generateChecksum("UPLDSEQ");
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

    public String uploadNewOutlet(Handler handler, Context context) {
        String rid = "";
        try {
            this.handler = handler;
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME,
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
                    businessModel.activationHelper.getIMEINumber());
            jsonFormatter.addParameter("LoginId", businessModel.userMasterHelper
                    .getUserMasterBO().getLoginName());
            jsonFormatter.addParameter("VersionCode",
                    businessModel.getApplicationVersionNumber());
            jsonFormatter.addParameter(SynchronizationHelper.VERSION_NAME, businessModel.getApplicationVersionName());
            jsonFormatter.addParameter("DistributorId", businessModel.userMasterHelper
                    .getUserMasterBO().getDistributorid());
            jsonFormatter.addParameter("OrganisationId", businessModel.userMasterHelper
                    .getUserMasterBO().getOrganizationId());
            jsonFormatter.addParameter("MobileDate",
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
                    businessModel.activationHelper.getIMEINumber());
            jsonFormatter.addParameter("LoginId", businessModel.userMasterHelper
                    .getUserMasterBO().getLoginName());
            jsonFormatter.addParameter("VersionCode",
                    businessModel.getApplicationVersionNumber());
            jsonFormatter.addParameter(SynchronizationHelper.VERSION_NAME, businessModel.getApplicationVersionName());
            jsonFormatter.addParameter("DistributorId", businessModel.userMasterHelper
                    .getUserMasterBO().getDistributorid());
            jsonFormatter.addParameter("OrganisationId", businessModel.userMasterHelper
                    .getUserMasterBO().getOrganizationId());
            jsonFormatter.addParameter("MobileDate",
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

}
