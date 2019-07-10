package com.ivy.sd.png.model;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import com.ivy.core.IvyConstants;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.DateTimeUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DownloadService extends IntentService {
    private BusinessModel bmodel;
    private boolean isAlreadyLastVisitConfigLoaded;

    public DownloadService() {
        super(DownloadService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        bmodel = (BusinessModel) getApplicationContext();
        updateServiceNew(intent);

    }

    private void updateServiceNew(Intent intent) {
        int method = intent.getIntExtra(SynchronizationHelper.SYNXC_STATUS, 0);
        SynchronizationHelper.FROM_SCREEN isFromWhere = (SynchronizationHelper.FROM_SCREEN) intent.getSerializableExtra("isFromWhere");
        int response = intent.getIntExtra(
                SynchronizationHelper.VOLLEY_RESPONSE, 0);

        int totalListCount = intent.getIntExtra("TotalCount", 0);
        int updateCount = intent.getIntExtra("UpdateCount", 0);
        switch (method) {
            case SynchronizationHelper.VOLLEY_LOGIN:

                Intent broadCastLoginIntent = new Intent(this, com.ivy.sd.png.model.DownloadReceiver.class);
                //broadCastLoginIntent.setAction("android.intent.action.DOWNLOAD");
                Bundle loginBundle = new Bundle();
                loginBundle.putInt(SynchronizationHelper.SYNXC_STATUS,
                        SynchronizationHelper.VOLLEY_LOGIN);
                loginBundle.putSerializable("isFromWhere", isFromWhere);

                if (response == SynchronizationHelper.VOLLEY_SUCCESS_RESPONSE) {

                    String jsonLoginString = intent
                            .getStringExtra(SynchronizationHelper.JSON_OBJECT);

                    try {
                        JSONObject jsonLoginObject = new JSONObject(jsonLoginString);
                        String errorLoginCode = jsonLoginObject
                                .getString(SynchronizationHelper.ERROR_CODE);
                        if (errorLoginCode
                                .equals(IvyConstants.AUTHENTICATION_SUCCESS_CODE)) {

                            bmodel.synchronizationHelper
                                    .parseJSONAndInsert(jsonLoginObject, false);
                            bmodel.userMasterHelper.downloadUserDetails();
                            bmodel.userMasterHelper.downloadDistributionDetails();

                        }
                        loginBundle.putString(SynchronizationHelper.ERROR_CODE,
                                errorLoginCode);
                        broadCastLoginIntent.putExtras(loginBundle);
                        sendBroadcast(broadCastLoginIntent);

                    } catch (JSONException e) {
                        loginBundle.putString(SynchronizationHelper.ERROR_CODE,
                                "E31");
                        broadCastLoginIntent.putExtras(loginBundle);
                        sendBroadcast(broadCastLoginIntent);

                    }

                } else if (response == SynchronizationHelper.VOLLEY_FAILURE_RESPONSE) {

                    String mobileErrorCode = intent
                            .getStringExtra(SynchronizationHelper.ERROR_CODE);
                    loginBundle.putString(SynchronizationHelper.ERROR_CODE,
                            mobileErrorCode);
                    broadCastLoginIntent.putExtras(loginBundle);
                    sendBroadcast(broadCastLoginIntent);

                }

                break;

            case SynchronizationHelper.URL_DOWNLOAD:
                Intent broadCastUrlIntent = new Intent(this, com.ivy.sd.png.model.DownloadReceiver.class);
                //broadCastUrlIntent.setAction("android.intent.action.DOWNLOAD");
                Bundle urlBundle = new Bundle();
                urlBundle.putInt(SynchronizationHelper.SYNXC_STATUS,
                        SynchronizationHelper.URL_DOWNLOAD);
                urlBundle.putSerializable("isFromWhere", isFromWhere);

                if (response == SynchronizationHelper.VOLLEY_SUCCESS_RESPONSE) {

                    ArrayList<String> tablelist = intent.getStringArrayListExtra(SynchronizationHelper.JSON_OBJECT_TABLE_LIST);

                    try {
                        for (int i = 0; i < tablelist.size(); i++) {
                            JSONObject jsonUrlObject = bmodel.synchronizationHelper.getmJsonObjectResponseByTableName().get(tablelist.get(i));
                            String errorUrlCode = jsonUrlObject.getString(SynchronizationHelper.ERROR_CODE);
                            if (errorUrlCode
                                    .equals(IvyConstants.AUTHENTICATION_SUCCESS_CODE)) {

                                bmodel.synchronizationHelper
                                        .parseJSONAndInsert(jsonUrlObject, true);

                                bmodel.synchronizationHelper.loadMasterUrlFromDB(true);
                            }
                            urlBundle.putString(SynchronizationHelper.ERROR_CODE,
                                    errorUrlCode);
                            broadCastUrlIntent.putExtras(urlBundle);
                            sendBroadcast(broadCastUrlIntent);
                        }

                    } catch (Exception e) {
                        urlBundle = new Bundle();
                        urlBundle
                                .putString(SynchronizationHelper.ERROR_CODE, "E31");
                        broadCastUrlIntent.putExtras(urlBundle);
                        sendBroadcast(broadCastUrlIntent);
                    }

                } else if (response == SynchronizationHelper.VOLLEY_FAILURE_RESPONSE) {

                    String mobileErrorCode = intent
                            .getStringExtra(SynchronizationHelper.ERROR_CODE);
                    urlBundle.putString(SynchronizationHelper.ERROR_CODE,
                            mobileErrorCode);
                    broadCastUrlIntent.putExtras(urlBundle);
                    sendBroadcast(broadCastUrlIntent);

                }

                break;
            case SynchronizationHelper.VOLLEY_DOWNLOAD_INSERT:

                Intent downloadInsertIntent;

                if (response == SynchronizationHelper.VOLLEY_SUCCESS_RESPONSE) {
                    ArrayList<String> tablelist = intent.getStringArrayListExtra(SynchronizationHelper.JSON_OBJECT_TABLE_LIST);
                    try {
                        String tablename;

                        for (int i = 0; i < tablelist.size(); i++) {
                            tablename = tablelist.get(i);
                            JSONObject jsonLoginObject = bmodel.synchronizationHelper.getmJsonObjectResponseByTableName().get(tablename);
                            String errorLoginCode;
                            try {
                                errorLoginCode = jsonLoginObject
                                        .getString(SynchronizationHelper.ERROR_CODE);
                            } catch (Exception e) {
                                errorLoginCode = null;
                                Commons.printException(e);
                            }

                            final long startTime = System.nanoTime();
                            if (errorLoginCode != null) {
                                if (errorLoginCode.equals(IvyConstants.AUTHENTICATION_SUCCESS_CODE)) {
                                    if (isFromWhere == SynchronizationHelper.FROM_SCREEN.RETAILER_SELECTION) {
                                        if (bmodel.configurationMasterHelper.IS_DELETE_TABLE) {
                                            bmodel.synchronizationHelper
                                                    .parseJSONAndInsert(jsonLoginObject, true);
                                        } else {
                                            bmodel.synchronizationHelper
                                                    .parseJSONAndInsert(jsonLoginObject, false);
                                        }
                                    } else {
                                        bmodel.synchronizationHelper
                                                .parseJSONAndInsert(jsonLoginObject, false);
                                    }
                                }
                            }
                            long endTime = (System.nanoTime() - startTime) / 1000000;
                            bmodel.synchronizationHelper.mTableList.put(tablename, endTime + "");

                            Commons.print("Download service," + "total Count: " + totalListCount + " update Count: " + updateCount);
                        }
                        if (totalListCount == updateCount) {
                            bmodel.synchronizationHelper.getURLResponse();

                            Bundle insertBundle = new Bundle();
                            insertBundle.putInt(SynchronizationHelper.SYNXC_STATUS,
                                    SynchronizationHelper.VOLLEY_DOWNLOAD_INSERT);
                            insertBundle
                                    .putString(
                                            SynchronizationHelper.ERROR_CODE,
                                            IvyConstants.AUTHENTICATION_SUCCESS_CODE);
                            insertBundle.putSerializable("isFromWhere", isFromWhere);
                            insertBundle.putInt("updateCount", updateCount);
                            insertBundle.putInt("totalCount", totalListCount);

                            downloadInsertIntent = new Intent(this, com.ivy.sd.png.model.DownloadReceiver.class);
                            //downloadInsertIntent.setAction("android.intent.action.DOWNLOAD");
                            downloadInsertIntent.putExtras(insertBundle);
                            sendBroadcast(downloadInsertIntent);
                        } else {
                            Bundle insertBundle = new Bundle();
                            insertBundle.putInt(SynchronizationHelper.SYNXC_STATUS,
                                    SynchronizationHelper.VOLLEY_DOWNLOAD_INSERT);
                            insertBundle.putString(SynchronizationHelper.ERROR_CODE,
                                    IvyConstants.UPDATE_TABLE_SUCCESS_CODE);
                            insertBundle.putSerializable("isFromWhere", isFromWhere);
                            insertBundle.putInt("updateCount", updateCount);
                            insertBundle.putInt("totalCount", totalListCount);

                            downloadInsertIntent = new Intent(this, com.ivy.sd.png.model.DownloadReceiver.class);
                            //downloadInsertIntent.setAction("android.intent.action.DOWNLOAD");
                            downloadInsertIntent.putExtras(insertBundle);
                            sendBroadcast(downloadInsertIntent);
                        }
                    } catch (Exception e) {
                        downloadInsertIntent = new Intent();
                        Bundle insertBundle = new Bundle();
                        insertBundle.putString(SynchronizationHelper.ERROR_CODE,
                                "E31");
                        insertBundle.putInt(SynchronizationHelper.SYNXC_STATUS,
                                SynchronizationHelper.VOLLEY_DOWNLOAD_INSERT);
                        insertBundle.putSerializable("FromLogin", isFromWhere);

                        downloadInsertIntent.putExtras(insertBundle);
                        sendBroadcast(downloadInsertIntent);
                    }
                } else if (response == SynchronizationHelper.VOLLEY_FAILURE_RESPONSE) {
                    downloadInsertIntent = new Intent(this, com.ivy.sd.png.model.DownloadReceiver.class);
                    //downloadInsertIntent.setAction("android.intent.action.DOWNLOAD");
                    Bundle insertBundle = new Bundle();
                    String mobileErrorCode = intent
                            .getStringExtra(SynchronizationHelper.ERROR_CODE);
                    insertBundle.putString(SynchronizationHelper.ERROR_CODE,
                            mobileErrorCode);
                    insertBundle.putInt(SynchronizationHelper.SYNXC_STATUS,
                            SynchronizationHelper.VOLLEY_DOWNLOAD_INSERT);
                    insertBundle.putSerializable("isFromWhere", isFromWhere);
                    downloadInsertIntent.putExtras(insertBundle);
                    sendBroadcast(downloadInsertIntent);


                }
                break;

            case SynchronizationHelper.DISTRIBUTOR_WISE_DOWNLOAD_INSERT:
                if (response == SynchronizationHelper.VOLLEY_SUCCESS_RESPONSE) {
                    ArrayList<String> tablelist = intent.getStringArrayListExtra(SynchronizationHelper.JSON_OBJECT_TABLE_LIST);

                    try {
                        String tablename = "";

                        for (int i = 0; i < tablelist.size(); i++) {
                            tablename = tablelist.get(i);
                            JSONObject jsonLoginObject = bmodel.synchronizationHelper.getmJsonObjectResponseByTableName().get(tablename);
                            String errorLoginCode = jsonLoginObject
                                    .getString(SynchronizationHelper.ERROR_CODE);
                            final long startTime = System.nanoTime();
                            if (errorLoginCode != null) {
                                if (errorLoginCode.equals(IvyConstants.AUTHENTICATION_SUCCESS_CODE)) {

                                    bmodel.synchronizationHelper
                                            .parseJSONAndInsert(jsonLoginObject, false);

                                }
                            }
                            long endTime = (System.nanoTime() - startTime) / 1000000;
                            bmodel.synchronizationHelper.mTableList.put(tablename, endTime + "");

                            Commons.print("Download service, " + "total Count :" + totalListCount);
                            Commons.print("Download Service, " + "update Count :" + updateCount);
                        }
                        if (totalListCount == updateCount) {
                            bmodel.synchronizationHelper.getURLResponse();
                            //						bmodel.synchronizationHelper.readEmptyTableDetails();


                            downloadInsertIntent = new Intent(this, com.ivy.sd.png.model.DownloadReceiver.class);
                            //downloadInsertIntent.setAction("android.intent.action.DOWNLOAD");
                            Bundle insertBundle = new Bundle();
                            insertBundle.putInt(SynchronizationHelper.SYNXC_STATUS,
                                    SynchronizationHelper.DISTRIBUTOR_WISE_DOWNLOAD_INSERT);
                            insertBundle
                                    .putString(
                                            SynchronizationHelper.ERROR_CODE,
                                            IvyConstants.AUTHENTICATION_SUCCESS_CODE);
                            insertBundle.putSerializable("isFromWhere", isFromWhere);
                            insertBundle.putInt("updateCount", updateCount);
                            insertBundle.putInt("totalCount", totalListCount);
                            downloadInsertIntent.putExtras(insertBundle);
                            sendBroadcast(downloadInsertIntent);
                        } else {
                            downloadInsertIntent = new Intent(this, com.ivy.sd.png.model.DownloadReceiver.class);
                            //downloadInsertIntent.setAction("android.intent.action.DOWNLOAD");
                            Bundle insertBundle = new Bundle();
                            insertBundle.putInt(SynchronizationHelper.SYNXC_STATUS,
                                    SynchronizationHelper.DISTRIBUTOR_WISE_DOWNLOAD_INSERT);
                            insertBundle
                                    .putString(
                                            SynchronizationHelper.ERROR_CODE,
                                            IvyConstants.UPDATE_TABLE_SUCCESS_CODE);
                            insertBundle.putSerializable("isFromWhere", isFromWhere);
                            insertBundle.putInt("updateCount", updateCount);
                            insertBundle.putInt("totalCount", totalListCount);


                            downloadInsertIntent.putExtras(insertBundle);
                            sendBroadcast(downloadInsertIntent);
                        }


                    } catch (JSONException e) {
                        downloadInsertIntent = new Intent();
                        Bundle insertBundle = new Bundle();
                        insertBundle.putString(SynchronizationHelper.ERROR_CODE,
                                "E31");
                        insertBundle.putInt(SynchronizationHelper.SYNXC_STATUS,
                                SynchronizationHelper.DISTRIBUTOR_WISE_DOWNLOAD_INSERT);
                        insertBundle.putSerializable("FromLogin", isFromWhere);

                        downloadInsertIntent.putExtras(insertBundle);
                        sendBroadcast(downloadInsertIntent);

                    }

                } else if (response == SynchronizationHelper.VOLLEY_FAILURE_RESPONSE) {
                    downloadInsertIntent = new Intent(this, com.ivy.sd.png.model.DownloadReceiver.class);
                    //downloadInsertIntent.setAction("android.intent.action.DOWNLOAD");
                    Bundle insertBundle = new Bundle();
                    String mobileErrorCode = intent
                            .getStringExtra(SynchronizationHelper.ERROR_CODE);
                    insertBundle.putString(SynchronizationHelper.ERROR_CODE,
                            mobileErrorCode);
                    insertBundle.putInt(SynchronizationHelper.SYNXC_STATUS,
                            SynchronizationHelper.DISTRIBUTOR_WISE_DOWNLOAD_INSERT);
                    insertBundle.putSerializable("isFromWhere", isFromWhere);
                    downloadInsertIntent.putExtras(insertBundle);
                    sendBroadcast(downloadInsertIntent);


                }

                break;
            case SynchronizationHelper.LAST_VISIT_TRAN_DOWNLOAD_INSERT:
                if (response == SynchronizationHelper.VOLLEY_SUCCESS_RESPONSE) {
                    ArrayList<String> tablelist = intent.getStringArrayListExtra(SynchronizationHelper.JSON_OBJECT_TABLE_LIST);

                    try {
                        String tablename = "";

                        for (int i = 0; i < tablelist.size(); i++) {
                            tablename = tablelist.get(i);
                            JSONObject jsonLoginObject = bmodel.synchronizationHelper.getmJsonObjectResponseByTableName().get(tablename);
                            String errorLoginCode = jsonLoginObject
                                    .getString(SynchronizationHelper.ERROR_CODE);
                            final long startTime = System.nanoTime();
                            if (errorLoginCode != null) {
                                if (errorLoginCode.equals(IvyConstants.AUTHENTICATION_SUCCESS_CODE)) {
                                    bmodel.synchronizationHelper
                                            .parseJSONAndInsert(jsonLoginObject, false);

                                }
                            }
                            long endTime = (System.nanoTime() - startTime) / 1000000;
                            bmodel.synchronizationHelper.mTableList.put(tablename, endTime + "");

                            Commons.print("Download service, " + "total Count :" + totalListCount);
                            Commons.print("Download Service, " + "update Count :" + updateCount);
                        }
                        if (totalListCount == updateCount) {
                            bmodel.synchronizationHelper.getURLResponse();
                            //						bmodel.synchronizationHelper.readEmptyTableDetails();


                            downloadInsertIntent = new Intent(this, com.ivy.sd.png.model.DownloadReceiver.class);
                            //downloadInsertIntent.setAction("android.intent.action.DOWNLOAD");
                            Bundle insertBundle = new Bundle();
                            insertBundle.putInt(SynchronizationHelper.SYNXC_STATUS,
                                    SynchronizationHelper.LAST_VISIT_TRAN_DOWNLOAD_INSERT);
                            insertBundle
                                    .putString(
                                            SynchronizationHelper.ERROR_CODE,
                                            IvyConstants.AUTHENTICATION_SUCCESS_CODE);
                            insertBundle.putSerializable("isFromWhere", isFromWhere);
                            insertBundle.putInt("updateCount", updateCount);
                            insertBundle.putInt("totalCount", totalListCount);
                            downloadInsertIntent.putExtras(insertBundle);
                            sendBroadcast(downloadInsertIntent);
                        } else {
                            downloadInsertIntent = new Intent(this, com.ivy.sd.png.model.DownloadReceiver.class);
                            //downloadInsertIntent.setAction("android.intent.action.DOWNLOAD");
                            Bundle insertBundle = new Bundle();
                            insertBundle.putInt(SynchronizationHelper.SYNXC_STATUS,
                                    SynchronizationHelper.LAST_VISIT_TRAN_DOWNLOAD_INSERT);
                            insertBundle
                                    .putString(
                                            SynchronizationHelper.ERROR_CODE,
                                            IvyConstants.UPDATE_TABLE_SUCCESS_CODE);
                            insertBundle.putSerializable("isFromWhere", isFromWhere);
                            insertBundle.putInt("updateCount", updateCount);
                            insertBundle.putInt("totalCount", totalListCount);


                            downloadInsertIntent.putExtras(insertBundle);
                            sendBroadcast(downloadInsertIntent);
                        }


                    } catch (JSONException e) {
                        downloadInsertIntent = new Intent();
                        Bundle insertBundle = new Bundle();
                        insertBundle.putString(SynchronizationHelper.ERROR_CODE,
                                "E31");
                        insertBundle.putInt(SynchronizationHelper.SYNXC_STATUS,
                                SynchronizationHelper.LAST_VISIT_TRAN_DOWNLOAD_INSERT);
                        insertBundle.putSerializable("FromLogin", isFromWhere);

                        downloadInsertIntent.putExtras(insertBundle);
                        sendBroadcast(downloadInsertIntent);

                    }

                } else if (response == SynchronizationHelper.VOLLEY_FAILURE_RESPONSE) {
                    downloadInsertIntent = new Intent(this, com.ivy.sd.png.model.DownloadReceiver.class);
                    //downloadInsertIntent.setAction("android.intent.action.DOWNLOAD");
                    Bundle insertBundle = new Bundle();
                    String mobileErrorCode = intent
                            .getStringExtra(SynchronizationHelper.ERROR_CODE);
                    insertBundle.putString(SynchronizationHelper.ERROR_CODE,
                            mobileErrorCode);
                    insertBundle.putInt(SynchronizationHelper.SYNXC_STATUS,
                            SynchronizationHelper.DISTRIBUTOR_WISE_DOWNLOAD_INSERT);
                    insertBundle.putSerializable("isFromWhere", isFromWhere);
                    downloadInsertIntent.putExtras(insertBundle);
                    sendBroadcast(downloadInsertIntent);
                }

                break;


            case SynchronizationHelper.NEW_RETAILER_DOWNLOAD_INSERT:

                Intent downloadRetailerInsertIntent = null;

                if (response == SynchronizationHelper.VOLLEY_SUCCESS_RESPONSE) {
                    ArrayList<String> tablelist = intent.getStringArrayListExtra(SynchronizationHelper.JSON_OBJECT_TABLE_LIST);
//					String jsoninsertString=bmodel.synchronizationHelper.getJsonResponseFromTableName(tableName);
                    /*String jsoninsertString = intent
                            .getStringExtra(SynchronizationHelper.JSON_OBJECT);*/
                    try {
                        for (int i = 0; i < tablelist.size(); i++) {
                            JSONObject jsonLoginObject = bmodel.synchronizationHelper.getmJsonObjectResponseByTableName().get(tablelist.get(i));
                            String errorLoginCode = jsonLoginObject
                                    .getString(SynchronizationHelper.ERROR_CODE);
                            if (errorLoginCode
                                    .equals(IvyConstants.AUTHENTICATION_SUCCESS_CODE)) {
                                bmodel.synchronizationHelper
                                        .parseJSONAndInsert(jsonLoginObject, false);

                            }

                            if (totalListCount == updateCount) {

                               /* bmodel.synchronizationHelper
                                        .updatetempTablesWithRetailerMaster();*/
                                //bmodel.configurationMasterHelper.downloadRetailerProperty();
                                bmodel.downloadRetailerMaster();
                                if (bmodel.configurationMasterHelper.CALC_QDVP3)
                                    bmodel.updateSurveyScoreHistoryRetailerWise();
                                bmodel.updateIsTodayAndIsVanSalesInRetailerMasterInfo();

                                downloadRetailerInsertIntent = new Intent(this, com.ivy.sd.png.model.DownloadReceiver.class);
                                //downloadRetailerInsertIntent.setAction("android.intent.action.DOWNLOAD");
                                Bundle insertBundle = new Bundle();
                                insertBundle
                                        .putInt(SynchronizationHelper.SYNXC_STATUS,
                                                SynchronizationHelper.NEW_RETAILER_DOWNLOAD_INSERT);
                                insertBundle
                                        .putString(
                                                SynchronizationHelper.ERROR_CODE,
                                                IvyConstants.AUTHENTICATION_SUCCESS_CODE);
                                insertBundle.putSerializable("isFromWhere", isFromWhere);

                                downloadRetailerInsertIntent.putExtras(insertBundle);
                                sendBroadcast(downloadRetailerInsertIntent);
                            }
                        }

                    } catch (JSONException e) {
                        downloadRetailerInsertIntent = new Intent();
                        Bundle insertBundle = new Bundle();
                        insertBundle.putString(SynchronizationHelper.ERROR_CODE,
                                "E31");
                        insertBundle.putInt(SynchronizationHelper.SYNXC_STATUS,
                                SynchronizationHelper.NEW_RETAILER_DOWNLOAD_INSERT);
                        insertBundle.putSerializable("isFromWhere", isFromWhere);

                        downloadRetailerInsertIntent.putExtras(insertBundle);
                        sendBroadcast(downloadRetailerInsertIntent);

                    }

                } else if (response == SynchronizationHelper.VOLLEY_FAILURE_RESPONSE) {
                    downloadRetailerInsertIntent = new Intent(this, com.ivy.sd.png.model.DownloadReceiver.class);
                    //downloadRetailerInsertIntent.setAction("android.intent.action.DOWNLOAD");
                    Bundle insertBundle = new Bundle();
                    String mobileErrorCode = intent
                            .getStringExtra(SynchronizationHelper.ERROR_CODE);
                    insertBundle.putString(SynchronizationHelper.ERROR_CODE,
                            mobileErrorCode);
                    insertBundle.putInt(SynchronizationHelper.SYNXC_STATUS,
                            SynchronizationHelper.NEW_RETAILER_DOWNLOAD_INSERT);
                    insertBundle.putSerializable("isFromWhere", isFromWhere);
                    downloadRetailerInsertIntent.putExtras(insertBundle);
                    sendBroadcast(downloadRetailerInsertIntent);

                }
                break;


            case SynchronizationHelper.USER_RETAILER_TRAN_DOWNLOAD_INSERT:

                Intent downloadUserRetailerInsertIntent = null;

                if (response == SynchronizationHelper.VOLLEY_SUCCESS_RESPONSE) {
                    ArrayList<String> tablelist = intent.getStringArrayListExtra(SynchronizationHelper.JSON_OBJECT_TABLE_LIST);
//					String jsoninsertString=bmodel.synchronizationHelper.getJsonResponseFromTableName(tableName);
                    /*String jsoninsertString = intent
                            .getStringExtra(SynchronizationHelper.JSON_OBJECT);*/
                    try {
                        for (int i = 0; i < tablelist.size(); i++) {
                            JSONObject jsonLoginObject = bmodel.synchronizationHelper.getmJsonObjectResponseByTableName().get(tablelist.get(i));
                            String errorLoginCode = jsonLoginObject
                                    .getString(SynchronizationHelper.ERROR_CODE);
                            if (errorLoginCode
                                    .equals(IvyConstants.AUTHENTICATION_SUCCESS_CODE)) {

                                bmodel.synchronizationHelper
                                        .parseJSONAndInsert(jsonLoginObject, false);


                            }
                            Commons.print("Download service, " + "total Count :" + totalListCount);
                            Commons.print("Download Service, " + "update Count :" + updateCount);
                            if (totalListCount == updateCount) {
                                Commons.print("Stop time, " + DateTimeUtils.now(DateTimeUtils.TIME));


                                downloadUserRetailerInsertIntent = new Intent(this, com.ivy.sd.png.model.DownloadReceiver.class);
                                //downloadUserRetailerInsertIntent.setAction("android.intent.action.DOWNLOAD");
                                Bundle insertBundle = new Bundle();
                                insertBundle
                                        .putInt(SynchronizationHelper.SYNXC_STATUS,
                                                SynchronizationHelper.USER_RETAILER_TRAN_DOWNLOAD_INSERT);
                                insertBundle
                                        .putString(
                                                SynchronizationHelper.ERROR_CODE,
                                                IvyConstants.AUTHENTICATION_SUCCESS_CODE);
                                insertBundle.putSerializable("isFromWhere", isFromWhere);

                                downloadUserRetailerInsertIntent.putExtras(insertBundle);
                                sendBroadcast(downloadUserRetailerInsertIntent);
                            }
                        }

                    } catch (JSONException e) {
                        downloadUserRetailerInsertIntent = new Intent();
                        Bundle insertBundle = new Bundle();
                        insertBundle.putString(SynchronizationHelper.ERROR_CODE,
                                "E31");
                        insertBundle.putInt(SynchronizationHelper.SYNXC_STATUS,
                                SynchronizationHelper.USER_RETAILER_TRAN_DOWNLOAD_INSERT);
                        insertBundle.putSerializable("isFromWhere", isFromWhere);

                        downloadUserRetailerInsertIntent.putExtras(insertBundle);
                        sendBroadcast(downloadUserRetailerInsertIntent);

                    }catch (NullPointerException e){

                        Commons.printException(e);
                    }

                } else if (response == SynchronizationHelper.VOLLEY_FAILURE_RESPONSE) {
                    downloadUserRetailerInsertIntent = new Intent(this, com.ivy.sd.png.model.DownloadReceiver.class);
                    //downloadUserRetailerInsertIntent.setAction("android.intent.action.DOWNLOAD");
                    Bundle insertBundle = new Bundle();
                    String mobileErrorCode = intent
                            .getStringExtra(SynchronizationHelper.ERROR_CODE);
                    insertBundle.putString(SynchronizationHelper.ERROR_CODE,
                            mobileErrorCode);
                    insertBundle.putInt(SynchronizationHelper.SYNXC_STATUS,
                            SynchronizationHelper.USER_RETAILER_TRAN_DOWNLOAD_INSERT);
                    insertBundle.putSerializable("isFromWhere", isFromWhere);
                    downloadUserRetailerInsertIntent.putExtras(insertBundle);
                    sendBroadcast(downloadUserRetailerInsertIntent);

                }
                break;
            case SynchronizationHelper.SIH_DOWNLOAD:
                Intent sihIntent = null;
                if (response == SynchronizationHelper.VOLLEY_SUCCESS_RESPONSE) {
                    ArrayList<String> tablelist = intent.getStringArrayListExtra(SynchronizationHelper.JSON_OBJECT_TABLE_LIST);
                    try {

                        for (int i = 0; i < tablelist.size(); i++) {
                            JSONObject jsonLoginObject = bmodel.synchronizationHelper.getmJsonObjectResponseByTableName().get(tablelist.get(i));
                            String errorLoginCode = jsonLoginObject
                                    .getString(SynchronizationHelper.ERROR_CODE);
                            if (errorLoginCode.equals(IvyConstants.AUTHENTICATION_SUCCESS_CODE)) {
                                bmodel.synchronizationHelper
                                        .parseJSONAndInsert(jsonLoginObject, false);
                            }
                        }

                    } catch (Exception e) {
                        Commons.printException(e);
                    }


                }
                sihIntent = new Intent(this, com.ivy.sd.png.model.DownloadReceiver.class);
                //sihIntent.setAction("android.intent.action.DOWNLOAD");
                Bundle insertBundle = new Bundle();

                insertBundle.putInt(SynchronizationHelper.SYNXC_STATUS,
                        SynchronizationHelper.SIH_DOWNLOAD);
                insertBundle.putSerializable("isFromWhere", isFromWhere);
                sihIntent.putExtras(insertBundle);
                sendBroadcast(sihIntent);
                break;

            case SynchronizationHelper.WAREHOUSE_STOCK_DOWNLOAD:
                Intent warehouseStockIntent;
                if (response == SynchronizationHelper.VOLLEY_SUCCESS_RESPONSE) {

                    ArrayList<String> tableList = intent.getStringArrayListExtra(SynchronizationHelper.JSON_OBJECT_TABLE_LIST);
                    String errorLoginCode = "";

                    try {

                        for (int i = 0; i < tableList.size(); i++) {
                            JSONObject jsonObject = bmodel.synchronizationHelper.getmJsonObjectResponseByTableName().get(tableList.get(i));
                            errorLoginCode = jsonObject.getString(SynchronizationHelper.ERROR_CODE);
                            if (errorLoginCode.equals(IvyConstants.AUTHENTICATION_SUCCESS_CODE)) {
                                bmodel.synchronizationHelper.parseJSONAndInsert(jsonObject, true);
                            }
                        }

                    } catch (Exception e) {
                        Commons.printException(e);
                    }


                    warehouseStockIntent = new Intent();
                    warehouseStockIntent
                            .setAction("com.ivy.intent.action.WareHouseStock");

                    insertBundle = new Bundle();
                    insertBundle.putInt(SynchronizationHelper.SYNXC_STATUS, SynchronizationHelper.WAREHOUSE_STOCK_DOWNLOAD);
                    insertBundle.putSerializable("isFromWhere", isFromWhere);
                    insertBundle.putString(SynchronizationHelper.ERROR_CODE, errorLoginCode);
                    warehouseStockIntent.putExtras(insertBundle);
                    sendBroadcast(warehouseStockIntent);
                } else {
                    warehouseStockIntent = new Intent();
                    warehouseStockIntent.setAction("com.ivy.intent.action.WareHouseStock");
                    insertBundle = new Bundle();
                    String mobileErrorCode = intent.getStringExtra(SynchronizationHelper.ERROR_CODE);
                    insertBundle.putString(SynchronizationHelper.ERROR_CODE, mobileErrorCode);
                    insertBundle.putInt(SynchronizationHelper.SYNXC_STATUS, SynchronizationHelper.WAREHOUSE_STOCK_DOWNLOAD);
                    insertBundle.putSerializable("isFromWhere", isFromWhere);
                    warehouseStockIntent.putExtras(insertBundle);
                    sendBroadcast(warehouseStockIntent);
                }
                break;
            case SynchronizationHelper.RETAILER_DOWNLOAD_BY_LOCATION:
                Intent retailerDownloadIntent = null;
                if (response == SynchronizationHelper.VOLLEY_SUCCESS_RESPONSE) {
                    ArrayList<String> tablelist = intent.getStringArrayListExtra(SynchronizationHelper.JSON_OBJECT_TABLE_LIST);
                    String errorLoginCode = "";
                    try {

                        for (int i = 0; i < tablelist.size(); i++) {
                            JSONObject jsonObject = bmodel.synchronizationHelper.getmJsonObjectResponseByTableName().get(tablelist.get(i));
                            errorLoginCode = jsonObject
                                    .getString(SynchronizationHelper.ERROR_CODE);
                            if (errorLoginCode.equals(IvyConstants.AUTHENTICATION_SUCCESS_CODE)) {
                                if (tablelist.get(i).equalsIgnoreCase("RetailerMaster"))
                                    bmodel.synchronizationHelper.downloadRetailerByLocOrUser(jsonObject);
                                if (tablelist.get(i).equalsIgnoreCase("RetailerBeatMapping"))
                                    bmodel.synchronizationHelper.downloadRetailerBeats(jsonObject);

//
                            }
                        }

                    } catch (Exception e) {
                        Commons.printException(e);
                    }


                    retailerDownloadIntent = new Intent(this, com.ivy.sd.png.model.DownloadReceiver.class);
                    //retailerDownloadIntent.setAction("android.intent.action.DOWNLOAD");
                    insertBundle = null;
                    insertBundle = new Bundle();

                    insertBundle.putInt(SynchronizationHelper.SYNXC_STATUS,
                            SynchronizationHelper.RETAILER_DOWNLOAD_BY_LOCATION);
                    insertBundle.putSerializable("isFromWhere", isFromWhere);

                    insertBundle.putString(SynchronizationHelper.ERROR_CODE,
                            errorLoginCode);
                    retailerDownloadIntent.putExtras(insertBundle);
                    sendBroadcast(retailerDownloadIntent);
                } else {
                    retailerDownloadIntent = new Intent(this, com.ivy.sd.png.model.DownloadReceiver.class);
                    //retailerDownloadIntent.setAction("android.intent.action.DOWNLOAD");
                    insertBundle = null;
                    insertBundle = new Bundle();
                    String mobileErrorCode = intent
                            .getStringExtra(SynchronizationHelper.ERROR_CODE);
                    insertBundle.putString(SynchronizationHelper.ERROR_CODE,
                            mobileErrorCode);
                    insertBundle.putInt(SynchronizationHelper.SYNXC_STATUS,
                            SynchronizationHelper.RETAILER_DOWNLOAD_BY_LOCATION);
                    insertBundle.putSerializable("isFromWhere", isFromWhere);
                    retailerDownloadIntent.putExtras(insertBundle);
                    sendBroadcast(retailerDownloadIntent);
                }
                break;
            case SynchronizationHelper.DATA_DOWNLOAD_BY_RETAILER:
                Intent dataDownloadIntent = null;
                if (response == SynchronizationHelper.VOLLEY_SUCCESS_RESPONSE) {
                    ArrayList<String> tablelist = intent.getStringArrayListExtra(SynchronizationHelper.JSON_OBJECT_TABLE_LIST);

                    String errorLoginCode = "";
                    try {
                        for (int i = 0; i < tablelist.size(); i++) {
                            JSONObject jsonLoginObject = bmodel.synchronizationHelper.getmJsonObjectResponseByTableName().get(tablelist.get(i));
                            errorLoginCode = jsonLoginObject
                                    .getString("Response");
                        }
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                    if (SynchronizationHelper.FROM_SCREEN.RETAILER_SELECTION == isFromWhere) {
                        bmodel.synchronizationHelper.loadMasterUrlFromDB(false);
                    } else if (SynchronizationHelper.FROM_SCREEN.LOGIN == isFromWhere) {
                        if (bmodel.synchronizationHelper.getmRetailerWiseIterateCount() == bmodel.synchronizationHelper.getRetailerwiseTotalIterateCount())
                            bmodel.synchronizationHelper.downloadMasterUrlFromDBRetailerWise();
                    }
                    final ArrayList<String> urlList = bmodel.synchronizationHelper.getUrlList();
                    if (urlList.size() == 0) {
                        bmodel.synchronizationHelper
                                .updateProductAndRetailerMaster();
                        bmodel.synchronizationHelper.loadMethodsNew();
                    }

                    dataDownloadIntent = new Intent(this, com.ivy.sd.png.model.DownloadReceiver.class);
                    //dataDownloadIntent.setAction("android.intent.action.DOWNLOAD");
                    insertBundle = null;
                    insertBundle = new Bundle();

                    insertBundle.putInt(SynchronizationHelper.SYNXC_STATUS,
                            SynchronizationHelper.DATA_DOWNLOAD_BY_RETAILER);
                    insertBundle.putSerializable("isFromWhere", isFromWhere);

                    insertBundle.putString(SynchronizationHelper.ERROR_CODE,
                            errorLoginCode);
                    dataDownloadIntent.putExtras(insertBundle);
                    sendBroadcast(dataDownloadIntent);
                } else {
                    dataDownloadIntent = new Intent(this, com.ivy.sd.png.model.DownloadReceiver.class);
                    //dataDownloadIntent.setAction("android.intent.action.DOWNLOAD");
                    insertBundle = null;
                    insertBundle = new Bundle();
                    String mobileErrorCode = intent
                            .getStringExtra(SynchronizationHelper.ERROR_CODE);
                    insertBundle.putString(SynchronizationHelper.ERROR_CODE,
                            mobileErrorCode);
                    insertBundle.putInt(SynchronizationHelper.SYNXC_STATUS,
                            SynchronizationHelper.DATA_DOWNLOAD_BY_RETAILER);
                    insertBundle.putSerializable("isFromWhere", isFromWhere);
                    dataDownloadIntent.putExtras(insertBundle);
                    sendBroadcast(dataDownloadIntent);
                }
                break;
            case SynchronizationHelper.RETAILER_DOWNLOAD_FINISH_UPDATE:
                Intent finishDownloadIntent = null;
                if (response == SynchronizationHelper.VOLLEY_SUCCESS_RESPONSE) {
                    ArrayList<String> tablelist = intent.getStringArrayListExtra(SynchronizationHelper.JSON_OBJECT_TABLE_LIST);

                    String errorLoginCode = "";
                    try {
                        for (int i = 0; i < tablelist.size(); i++) {
                            JSONObject jsonLoginObject = bmodel.synchronizationHelper.getmJsonObjectResponseByTableName().get(tablelist.get(i));
                            errorLoginCode = jsonLoginObject
                                    .getString("Response");
                        }
                    } catch (Exception e) {
                        Commons.printException(e);
                    }

                    if (bmodel.synchronizationHelper.getmRetailerWiseIterateCount() == 0) {
                        bmodel.synchronizationHelper
                                .updateProductAndRetailerMaster();
                        bmodel.synchronizationHelper.loadMethodsNew();
                    }
                    //bmodel.synchronizationHelper.loadMasterUrlFromDB(false);
                    finishDownloadIntent = new Intent(this, com.ivy.sd.png.model.DownloadReceiver.class);
                    //finishDownloadIntent.setAction("android.intent.action.DOWNLOAD");
                    insertBundle = null;
                    insertBundle = new Bundle();

                    insertBundle.putInt(SynchronizationHelper.SYNXC_STATUS,
                            SynchronizationHelper.RETAILER_DOWNLOAD_FINISH_UPDATE);
                    insertBundle.putSerializable("isFromWhere", isFromWhere);

                    insertBundle.putString(SynchronizationHelper.ERROR_CODE,
                            errorLoginCode);
                    finishDownloadIntent.putExtras(insertBundle);
                    sendBroadcast(finishDownloadIntent);
                } else {
                    finishDownloadIntent = new Intent(this, com.ivy.sd.png.model.DownloadReceiver.class);
                    //finishDownloadIntent.setAction("android.intent.action.DOWNLOAD");
                    insertBundle = null;
                    insertBundle = new Bundle();
                    String mobileErrorCode = intent
                            .getStringExtra(SynchronizationHelper.ERROR_CODE);
                    insertBundle.putString(SynchronizationHelper.ERROR_CODE,
                            mobileErrorCode);
                    insertBundle.putInt(SynchronizationHelper.SYNXC_STATUS,
                            SynchronizationHelper.RETAILER_DOWNLOAD_FINISH_UPDATE);
                    insertBundle.putSerializable("isFromWhere", isFromWhere);
                    finishDownloadIntent.putExtras(insertBundle);
                    sendBroadcast(finishDownloadIntent);
                }
                break;
            case SynchronizationHelper.VOLLEY_CUSTOMER_SEARCH:

                if (response == SynchronizationHelper.VOLLEY_SUCCESS_RESPONSE) {

                    String errorLoginCode = "";
                    try {
                        ArrayList<String> tablelist = intent.getStringArrayListExtra(SynchronizationHelper.JSON_OBJECT_TABLE_LIST);
                        for (int i = 0; i < tablelist.size(); i++) {
                            JSONObject jsonLoginObject = bmodel.synchronizationHelper.getmJsonObjectResponseByTableName().get(tablelist.get(i));
                            errorLoginCode = jsonLoginObject
                                    .getString(SynchronizationHelper.ERROR_CODE);
                        }
                    } catch (Exception e) {
                        Commons.printException(e);
                    }

                    Intent broadCastIntent = new Intent(this, com.ivy.sd.png.model.DownloadReceiver.class);
                    //broadCastIntent.setAction("android.intent.action.DOWNLOAD");

                    Bundle CSBundle = new Bundle();
                    CSBundle.putInt(SynchronizationHelper.SYNXC_STATUS,
                            SynchronizationHelper.VOLLEY_CUSTOMER_SEARCH);
                    CSBundle.putString(SynchronizationHelper.ERROR_CODE,
                            errorLoginCode);
                    CSBundle.putSerializable("isFromWhere", isFromWhere);

                    broadCastIntent.putExtras(CSBundle);
                    sendBroadcast(broadCastIntent);
                } else {
                    Intent broadCastIntent = new Intent(this, com.ivy.sd.png.model.DownloadReceiver.class);
                    //broadCastIntent.setAction("android.intent.action.DOWNLOAD");

                    Bundle CSBundle = new Bundle();
                    CSBundle.putInt(SynchronizationHelper.SYNXC_STATUS,
                            SynchronizationHelper.VOLLEY_CUSTOMER_SEARCH);
                    CSBundle.putSerializable("isFromWhere", isFromWhere);
                    String mobileErrorCode = intent
                            .getStringExtra(SynchronizationHelper.ERROR_CODE);
                    CSBundle.putString(SynchronizationHelper.ERROR_CODE,
                            mobileErrorCode);

                    broadCastIntent.putExtras(CSBundle);
                    sendBroadcast(broadCastIntent);
                }
                break;
            case SynchronizationHelper.VOLLEY_TL_ABSENTEES_RETAILER_DOWNLOAD:

                if (response == SynchronizationHelper.VOLLEY_SUCCESS_RESPONSE) {
                    ArrayList<String> tablelist = intent.getStringArrayListExtra(SynchronizationHelper.JSON_OBJECT_TABLE_LIST);
                    String errorLoginCode = "";
                    try {

                        for (int i = 0; i < tablelist.size(); i++) {
                            JSONObject jsonLoginObject = bmodel.synchronizationHelper.getmJsonObjectResponseByTableName().get(tablelist.get(i));
                            errorLoginCode = jsonLoginObject
                                    .getString(SynchronizationHelper.ERROR_CODE);
                            if (errorLoginCode.equals(IvyConstants.AUTHENTICATION_SUCCESS_CODE)) {
                                bmodel.synchronizationHelper
                                        .parseJSONAndInsert(jsonLoginObject, true);
                            }

                        }

                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                    Intent absenteesIntent = new Intent(this, com.ivy.sd.png.model.DownloadReceiver.class);
                    //absenteesIntent.setAction("android.intent.action.DOWNLOAD");
                    Bundle absenteesBundle = new Bundle();

                    absenteesBundle.putInt(SynchronizationHelper.SYNXC_STATUS,
                            SynchronizationHelper.VOLLEY_TL_ABSENTEES_RETAILER_DOWNLOAD);
                    absenteesBundle.putString(SynchronizationHelper.ERROR_CODE,
                            errorLoginCode);
                    absenteesBundle.putSerializable("isFromWhere", isFromWhere);
                    absenteesIntent.putExtras(absenteesBundle);
                    sendBroadcast(absenteesIntent);


                } else {
                    Intent absenteesIntent = new Intent();
                    Bundle absenteesBundle = new Bundle();

                    String mobileErrorCode = intent
                            .getStringExtra(SynchronizationHelper.ERROR_CODE);
                    absenteesBundle.putString(SynchronizationHelper.ERROR_CODE,
                            mobileErrorCode);
                    absenteesBundle.putInt(SynchronizationHelper.SYNXC_STATUS,
                            SynchronizationHelper.VOLLEY_TL_ABSENTEES_RETAILER_DOWNLOAD);
                    absenteesBundle.putSerializable("isFromWhere", isFromWhere);
                    absenteesIntent.putExtras(absenteesBundle);
                    sendBroadcast(absenteesIntent);
                }

                break;
            case SynchronizationHelper.DATA_DOWNLOAD_BY_DISTRIBUTOR:
                Intent distributorIntent = null;
                if (response == SynchronizationHelper.VOLLEY_SUCCESS_RESPONSE) {
                    ArrayList<String> tablelist = intent.getStringArrayListExtra(SynchronizationHelper.JSON_OBJECT_TABLE_LIST);

                    String errorLoginCode = "";
                    try {
                        for (int i = 0; i < tablelist.size(); i++) {
                            JSONObject jsonLoginObject = bmodel.synchronizationHelper.getmJsonObjectResponseByTableName().get(tablelist.get(i));
                            errorLoginCode = jsonLoginObject
                                    .getString("Response");
                        }
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                    bmodel.synchronizationHelper.loadMasterUrlFromDB(false);
                    distributorIntent = new Intent(this, com.ivy.sd.png.model.DownloadReceiver.class);
                    //distributorIntent.setAction("android.intent.action.DOWNLOAD");
                    insertBundle = null;
                    insertBundle = new Bundle();

                    insertBundle.putInt(SynchronizationHelper.SYNXC_STATUS,
                            SynchronizationHelper.DATA_DOWNLOAD_BY_DISTRIBUTOR);
                    insertBundle.putSerializable("isFromWhere", isFromWhere);

                    insertBundle.putString(SynchronizationHelper.ERROR_CODE,
                            errorLoginCode);
                    distributorIntent.putExtras(insertBundle);
                    sendBroadcast(distributorIntent);
                } else {
                    distributorIntent = new Intent(this, com.ivy.sd.png.model.DownloadReceiver.class);
                    //distributorIntent.setAction("android.intent.action.DOWNLOAD");
                    insertBundle = null;
                    insertBundle = new Bundle();
                    String mobileErrorCode = intent
                            .getStringExtra(SynchronizationHelper.ERROR_CODE);
                    insertBundle.putString(SynchronizationHelper.ERROR_CODE,
                            mobileErrorCode);
                    insertBundle.putInt(SynchronizationHelper.SYNXC_STATUS,
                            SynchronizationHelper.DATA_DOWNLOAD_BY_DISTRIBUTOR);
                    insertBundle.putSerializable("isFromWhere", isFromWhere);
                    distributorIntent.putExtras(insertBundle);
                    sendBroadcast(distributorIntent);
                }
                break;

            case SynchronizationHelper.DISTRIBUTOR_WISE_DOWNLOAD_FINISH_UPDATE:
                Intent distFinishDownloadIntent = null;
                if (response == SynchronizationHelper.VOLLEY_SUCCESS_RESPONSE) {
                    ArrayList<String> tablelist = intent.getStringArrayListExtra(SynchronizationHelper.JSON_OBJECT_TABLE_LIST);

                    String errorLoginCode = "";
                    try {
                        for (int i = 0; i < tablelist.size(); i++) {
                            JSONObject jsonLoginObject = bmodel.synchronizationHelper.getmJsonObjectResponseByTableName().get(tablelist.get(i));
                            errorLoginCode = jsonLoginObject
                                    .getString("Response");
                        }
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                    if (!bmodel.configurationMasterHelper.IS_PRICE_CHECK_RETAIN_LAST_VISIT_TRAN && !bmodel.configurationMasterHelper.IS_NEAR_EXPIRY_RETAIN_LAST_VISIT_TRAN && !bmodel.configurationMasterHelper.IS_STOCK_CHECK_RETAIN_LAST_VISIT_TRAN) {
                        bmodel.synchronizationHelper
                                .updateProductAndRetailerMaster();
                        bmodel.synchronizationHelper.loadMethodsNew();
                    }

                    distFinishDownloadIntent = new Intent(this, com.ivy.sd.png.model.DownloadReceiver.class);
                    //distFinishDownloadIntent.setAction("android.intent.action.DOWNLOAD");
                    insertBundle = null;
                    insertBundle = new Bundle();

                    insertBundle.putInt(SynchronizationHelper.SYNXC_STATUS,
                            SynchronizationHelper.DISTRIBUTOR_WISE_DOWNLOAD_FINISH_UPDATE);
                    insertBundle.putSerializable("isFromWhere", isFromWhere);

                    insertBundle.putString(SynchronizationHelper.ERROR_CODE,
                            errorLoginCode);
                    distFinishDownloadIntent.putExtras(insertBundle);
                    sendBroadcast(distFinishDownloadIntent);
                } else {
                    distFinishDownloadIntent = new Intent(this, com.ivy.sd.png.model.DownloadReceiver.class);
                    //distFinishDownloadIntent.setAction("android.intent.action.DOWNLOAD");
                    insertBundle = null;
                    insertBundle = new Bundle();
                    String mobileErrorCode = intent
                            .getStringExtra(SynchronizationHelper.ERROR_CODE);
                    insertBundle.putString(SynchronizationHelper.ERROR_CODE,
                            mobileErrorCode);
                    insertBundle.putInt(SynchronizationHelper.SYNXC_STATUS,
                            SynchronizationHelper.DISTRIBUTOR_WISE_DOWNLOAD_FINISH_UPDATE);
                    insertBundle.putSerializable("isFromWhere", isFromWhere);
                    distFinishDownloadIntent.putExtras(insertBundle);
                    sendBroadcast(distFinishDownloadIntent);
                }
                break;

            case SynchronizationHelper.DOWNLOAD_FINISH_UPDATE:
                Intent updateFinishedIntent = null;
                if (response == SynchronizationHelper.VOLLEY_SUCCESS_RESPONSE) {
                    ArrayList<String> tablelist = intent.getStringArrayListExtra(SynchronizationHelper.JSON_OBJECT_TABLE_LIST);

                    String errorLoginCode = "0";
                    try {
                        for (int i = 0; i < tablelist.size(); i++) {
                            JSONObject jsonLoginObject = bmodel.synchronizationHelper.getmJsonObjectResponseByTableName().get(tablelist.get(i));
                            errorLoginCode = jsonLoginObject
                                    .getString("Response");
                        }
                    } catch (Exception e) {
                        Commons.printException(e);
                    }


                    if (isFromWhere == SynchronizationHelper.FROM_SCREEN.RETAILER_SELECTION) {
                        bmodel.synchronizationHelper
                                .updatetempTablesWithRetailerMaster();
                        loadRetailerDependentMethod();
                    } else if (isFromWhere == SynchronizationHelper.FROM_SCREEN.NEW_RETAILER) {
                        bmodel.synchronizationHelper.updatetempTablesWithRetailerMaster();
                        bmodel.downloadRetailerMaster();
                    } else {
                        if (!bmodel.configurationMasterHelper.IS_DISTRIBUTOR_AVAILABLE)
                            bmodel.configurationMasterHelper.isDistributorWiseDownload();

                        if (bmodel.configurationMasterHelper.isLastVisitTransactionDownloadConfigEnabled()) {
                            isAlreadyLastVisitConfigLoaded = true;
                        } else {
                            isAlreadyLastVisitConfigLoaded = bmodel.configurationMasterHelper.downloadConfigForLoadLastVisit();
                        }

                        if (!bmodel.configurationMasterHelper.IS_DISTRIBUTOR_AVAILABLE && ((isFromWhere == SynchronizationHelper.FROM_SCREEN.LOGIN && !isAlreadyLastVisitConfigLoaded) || isFromWhere == SynchronizationHelper.FROM_SCREEN.SYNC)) {
                            final long startTime = System.nanoTime();
                            bmodel.synchronizationHelper
                                    .updateProductAndRetailerMaster();
                            bmodel.synchronizationHelper.loadMethodsNew();
                            long endTime = (System.nanoTime() - startTime) / 1000000;
                            bmodel.synchronizationHelper.mTableList.put("temp table update**", endTime + "");
                        }


                    }

                    updateFinishedIntent = new Intent(this, com.ivy.sd.png.model.DownloadReceiver.class);
                    //updateFinishedIntent.setAction("android.intent.action.DOWNLOAD");
                    insertBundle = null;
                    insertBundle = new Bundle();

                    insertBundle.putInt(SynchronizationHelper.SYNXC_STATUS,
                            SynchronizationHelper.DOWNLOAD_FINISH_UPDATE);
                    insertBundle.putSerializable("isFromWhere", isFromWhere);

                    insertBundle.putString(SynchronizationHelper.ERROR_CODE,
                            errorLoginCode);
                    updateFinishedIntent.putExtras(insertBundle);
                    sendBroadcast(updateFinishedIntent);
                } else {
                    updateFinishedIntent = new Intent(this, com.ivy.sd.png.model.DownloadReceiver.class);
                    //updateFinishedIntent.setAction("android.intent.action.DOWNLOAD");
                    insertBundle = null;
                    insertBundle = new Bundle();
                    String mobileErrorCode = intent
                            .getStringExtra(SynchronizationHelper.ERROR_CODE);
                    insertBundle.putString(SynchronizationHelper.ERROR_CODE,
                            mobileErrorCode);
                    insertBundle.putInt(SynchronizationHelper.SYNXC_STATUS,
                            SynchronizationHelper.DOWNLOAD_FINISH_UPDATE);
                    insertBundle.putSerializable("isFromWhere", isFromWhere);
                    updateFinishedIntent.putExtras(insertBundle);
                    sendBroadcast(updateFinishedIntent);
                }
                break;
            case SynchronizationHelper.MOBILE_EMAIL_VERIFICATION:
                Intent otpDownloadIntent = new Intent(this, com.ivy.sd.png.model.DownloadReceiver.class);
                //otpDownloadIntent.setAction("android.intent.action.DOWNLOAD");
                insertBundle = new Bundle();
                String mobileErrorCode = intent
                        .getStringExtra(SynchronizationHelper.ERROR_CODE);
                insertBundle.putString(SynchronizationHelper.ERROR_CODE,
                        mobileErrorCode);
                insertBundle.putInt(SynchronizationHelper.SYNXC_STATUS,
                        SynchronizationHelper.MOBILE_EMAIL_VERIFICATION);
                insertBundle.putSerializable("isFromWhere", isFromWhere);
                otpDownloadIntent.putExtras(insertBundle);
                sendBroadcast(otpDownloadIntent);
                break;

            default:
                break;

        }

    }


    private void loadRetailerDependentMethod() {

        bmodel.downloadRetailerwiseMerchandiser();
        bmodel.downloadRetailerMaster();
        bmodel.updateSurveyScoreHistoryRetailerWise();
        bmodel.initiativeHelper.generateInitiativeCoverageReport();
        bmodel.getRetailerMasterBO().setOtpActivatedDate("");
        bmodel.updateIsTodayAndIsVanSalesInRetailerMasterInfo();


    }

}
