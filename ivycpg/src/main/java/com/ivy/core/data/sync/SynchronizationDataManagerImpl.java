package com.ivy.core.data.sync;

import android.database.Cursor;
import android.os.Build;
import android.util.Base64;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.google.gson.JsonObject;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.core.model.UrlMaster;
import com.ivy.core.network.IvyJsonObjectRequest;
import com.ivy.core.network.IvyNetworkException;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.model.ApplicationConfigs;
import com.ivy.sd.png.model.MyHttpConnectionNew;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DeviceUtils;
import com.ivy.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Callable;

import javax.crypto.Cipher;
import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;

import static com.ivy.core.IvyConstants.AUTHENTICATION_SUCCESS_CODE;
import static com.ivy.core.IvyConstants.DATA_NOT_AVAILABLE_ERROR;
import static com.ivy.core.IvyConstants.IS_MANDATORY;
import static com.ivy.core.IvyConstants.USER_IDENTITY;
import static com.ivy.core.network.IvyJsonObjectRequest.TAG_JSON_OBJ;
import static com.ivy.sd.png.provider.SynchronizationHelper.ERROR_CODE;
import static com.ivy.sd.png.provider.SynchronizationHelper.JSON_KEY;
import static com.ivy.utils.StringUtils.getStringQueryParam;

public class SynchronizationDataManagerImpl implements SynchronizationDataManager {


    private final RequestQueue requestQueue;
    private DBUtil mDbUtil;

    private DataManager mDataManager;

    private static final String SECURITY_HEADER = "SECURITY_TOKEN_KEY";

    private static final String JSON_MASTER_KEY = "Master";
    private static final String JSON_FIELD_KEY = "Field";
    private static final String JSON_DATA_KEY = "Data";

    @Inject
    public SynchronizationDataManagerImpl(@DataBaseInfo DBUtil dbUtil, DataManager dataManager, RequestQueue requestQueue) {
        this.mDbUtil = dbUtil;
        this.mDataManager = dataManager;
        this.requestQueue = requestQueue;

    }

    private void initDb() {
        mDbUtil.createDataBase();
        if (mDbUtil.isDbNullOrClosed())
            mDbUtil.openDataBase();
    }

    private void shutDownDb() {
        mDbUtil.closeDB();
    }


    @Override
    public Single<String> getSyncUrl(String code) {
        return Single.fromCallable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                String url = "";
                if (!code.equals("")) {

                    String query = "select URL from UrlDownloadMaster where typecode="
                            + getStringQueryParam(code);
                    try {
                        initDb();
                        Cursor c = mDbUtil.selectSQL(query);
                        if (c.getCount() > 0) {
                            if (c.moveToNext()) {
                                url = c.getString(0);
                                c.close();
                                shutDownDb();
                                return url;

                            }
                        }
                        c.close();
                        shutDownDb();
                        return url;
                    } catch (Exception ignored) {
                        shutDownDb();
                        return url;
                    }

                }

                return null;
            }
        });
    }

    @Override
    public Single<ArrayList<UrlMaster>> getSyncUrlList(String code) {
        return Single.fromCallable(new Callable<ArrayList<UrlMaster>>() {
            @Override
            public ArrayList<UrlMaster> call() throws Exception {
                ArrayList<UrlMaster> urlList = new ArrayList<>();
                UrlMaster urlMaster;
                initDb();
                try {
                    String query = "select URL,IsMandatory,IsOnDemand from UrlDownloadMaster where typecode=" + getStringQueryParam(code);
                    Cursor c = mDbUtil.selectSQL(query);
                    if (c.getCount() > 0) {
                        urlList = new ArrayList<>();
                        boolean random = true;
                        while (c.moveToNext()) {
                            urlMaster = new UrlMaster();
                            urlMaster.setIsMandatory(c.getInt(1));
                            urlMaster.setUrl(c.getString(0));
                            urlMaster.setIsOnDemand(c.getInt(2));
                            urlList.add(urlMaster);
                            random = !random;

                        }
                    }
                    c.close();

                } catch (Exception e) {
                    Commons.printException("" + e);
                } finally {
                    shutDownDb();
                }

                return urlList;
            }
        });
    }

    @Override
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

    private String bytesToHexString(byte[] bytes) {
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

    @Override
    public Single<Vector<String>> uploadDataToServer(String headerInfo, String data,
                                                     String appendUrl) {
        return getAuthToken().flatMap(strings -> {
            if (!strings[0].equalsIgnoreCase(AUTHENTICATION_SUCCESS_CODE))
                return Single.fromCallable(() -> new Vector<>());
            else {
                return Single.fromCallable(new Callable<Vector<String>>() {
                    @Override
                    public Vector<String> call() throws Exception {
                        StringBuilder url = new StringBuilder();
                        url.append(DataMembers.SERVER_URL);
                        url.append(appendUrl);
                        try {
                            MyHttpConnectionNew http = new MyHttpConnectionNew();
                            http.create(MyHttpConnectionNew.POST, url.toString(), null);
                            http.addHeader(SECURITY_HEADER, strings[1]);
                            http.addParam("userInfo", headerInfo);
                            if (data != null) {
                                http.addParam("Data", data);
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
                });
            }
        });
    }

    @Override
    public Single<String[]> getAuthToken() {
        return Single.fromCallable(new Callable<String[]>() {
            @Override
            public String[] call() throws Exception {

                String[] authResults = new String[2];

                try {
                    String downloadUrl = DataMembers.SERVER_URL + DataMembers.AUTHENTICATE;
                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("LoginId", mDataManager.getUserName());
                    jsonObj.put("Password", mDataManager.getUserPassword());
                    jsonObj.put("Platform", "Android");
                    jsonObj.put("OSVersion", android.os.Build.VERSION.RELEASE);
                    jsonObj.put("FirmWare", "");
                    jsonObj.put("Model", Build.MODEL);
                    jsonObj.put("VersionCode",
                            mDataManager.getAppVersionNumber());
                    jsonObj.put(SynchronizationHelper.VERSION_NAME, mDataManager.getAppVersionName());
                    jsonObj.put("DeviceId",
                            mDataManager.getIMEINumber());
                    jsonObj.put("RegistrationId", mDataManager.getFcmRegistrationToken());
                    jsonObj.put("DeviceUniqueId", mDataManager.getDeviceId());
                    addDeviceValidationParameters(false, jsonObj);

                    MyHttpConnectionNew http = new MyHttpConnectionNew();
                    http.create(MyHttpConnectionNew.POST, downloadUrl, null);
                    http.addParam(USER_IDENTITY, RSAEncrypt(jsonObj.toString()));//passing encrypted jsonObj
                    http.connectMe();

                    Vector<String> result = http.getResult();

                    if (!result.isEmpty()) {
                        for (String s : result) {
                            JSONObject jsonObject = new JSONObject(s);
                            Iterator itr = jsonObject.keys();
                            while (itr.hasNext()) {
                                String key = (String) itr.next();
                                if (key.equals("ErrorCode")) {
                                    authResults[0] = jsonObject.get("ErrorCode").toString();
                                    authResults[0] = authResults[0].replaceAll("[\\[\\],\"]", "");
                                    break;
                                }
                            }
                        }
                    }

                    Map<String, List<String>> headerFields = http.getResponseHeaderField();
                    if (headerFields != null) {
                        for (Map.Entry<String, List<String>> entry : headerFields.entrySet()) {
                            System.out.println(entry.getKey() + "/" + entry.getValue());
                            if (entry.getKey() != null && entry.getKey().equals(SECURITY_HEADER)) {
                                if (entry.getValue() != null && entry.getValue().size() > 0) {
                                    authResults[1] = entry.getValue().get(0);
                                    break;
                                }
                            }
                        }
                    }

                } catch (Exception ignored) {

                }

                return authResults;
            }
        });
    }

    @Override
    public Single<Boolean> parseAndInsertJSON(JSONObject jsonObject, boolean isDeleteTable) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
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

                        firstValue = firstValue.substring(1, firstValue.length() - 1);

                        firstValue = firstValue.replace("\\/", "/");

                        valuesList.add(firstValue);

                    }
                    insertRecordsToDB(tablename, columns, valuesList, isDeleteTable);
                } catch (Exception ignored) {
                    return false;
                }

                return true;
            }
        });
    }

    private void insertRecordsToDB(String tableName, String columns,
                                   ArrayList<String> valueList, boolean isDeleteTable) {
        try {
            initDb();

            if (isDeleteTable)
                mDbUtil.deleteSQL(tableName, null, true);


            int recCount = 0;
            if (valueList != null) {
                StringBuffer queryString = new StringBuffer();
                for (String values : valueList) {

                    recCount = recCount + 1;
                    if (queryString.length() == 0) {
                        queryString.append("INSERT INTO ").append(tableName)
                                .append(" ( ").append(columns).append(" ) ")
                                .append("SELECT ").append(values);
                    } else {
                        queryString.append(" UNION ALL SELECT ").append(
                                values);
                    }

                    if (recCount == 400) {
                        mDbUtil.multiInsert(queryString.toString());
                        queryString = new StringBuffer();
                        recCount = 0;
                    }

                }
                if (queryString.length() > 0) {
                    mDbUtil.multiInsert(queryString.toString());
                }

                insertSyncTableDetails(tableName, valueList.size());
            }

        } catch (Exception ignored) {

        }
    }

    /**
     * Method to insert downloaded table details
     *
     * @param tableName - Name of the table
     * @param lineCount - Total number of records inserted into the table
     */
    private void insertSyncTableDetails(String tableName, int lineCount) {

        try {
            if (!StringUtils.isNullOrEmpty(mDataManager.getSyncLogId())) {


                String columns = "Tid,tablename,linecount";

                String values = StringUtils.getStringQueryParam(mDataManager.getSyncLogId()) + "," + StringUtils.getStringQueryParam(tableName)
                        + "," + lineCount;
                mDbUtil.insertSQL("SyncDownloadTableStatus", columns,
                        values);

            }

        } catch (Exception e) {
            Commons.printException(e);
        }
    }


    /*Methods used add deviceID's json Validation params
     *  - isDeviceChanged - false - validate -1 and Update -0
     *  - isDeviceChaned - True - validate -0 and Update - 1
     *  if activation false or is in internal activation ie uses ivy apis both values set to 0*/
    private void addDeviceValidationParameters(boolean isDeviceChanged, JSONObject jsonObject) {
        int mDeviceIdValidate, mDeviceIdChange;
        try {
            if (mDataManager.getApplicationName().toLowerCase().contains("ivy") || !ApplicationConfigs.withActivation) {
                mDeviceIdValidate = 0;
                mDeviceIdChange = 0;
            } else if (isDeviceChanged) {
                //if device changed then stop validations and update new device ID
                mDeviceIdValidate = 0;
                mDeviceIdChange = 1;
            } else {
                mDeviceIdValidate = 1;
                mDeviceIdChange = 0;
            }

            jsonObject.put("ValidateDeviceId", mDeviceIdValidate);
            jsonObject.put("UpdateDeviceId", mDeviceIdChange);
        } catch (JSONException jsonExpection) {
            Commons.print(jsonExpection.getMessage());
        }
    }

    private String RSAEncrypt(String inputString) {

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

    @Override
    public Single<JSONObject> downloadDataFromServer(String url, JSONObject request, boolean isMandatory) {

        return getAuthToken().flatMap(strings -> Single.create(new SingleOnSubscribe<JSONObject>() {
            @Override
            public void subscribe(SingleEmitter<JSONObject> emitter) throws Exception {
                IvyJsonObjectRequest jsonObjectRequest = new IvyJsonObjectRequest(DataMembers.SERVER_URL + url, request, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (response != null) {
                            try {
                                response.put(IS_MANDATORY, isMandatory);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            emitter.onSuccess(response);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        emitter.onError(error);
                    }
                });

                requestQueue.add(jsonObjectRequest);
            }
        }));
    }

    @Override
    public void stopAllRequest() {
        requestQueue.cancelAll(TAG_JSON_OBJ);
    }

    @Override
    public String getErrorMessage(VolleyError volleyError) {

        if (volleyError instanceof TimeoutError) {
            return "E32";
        } else if (volleyError instanceof NoConnectionError) {
            return "E06";
            // syncStatus = SYNC_TYPE_NO_INTERNET;
        } else if (volleyError instanceof ServerError) {
            return "E01";
        } else if (volleyError instanceof NetworkError) {
            return "E01";
            // syncStatus = SYNC_TYPE_CONN_LOST;
        } else if (volleyError instanceof ParseError) {
            return "E31";
        } else {
            return "E01";
            //syncStatus = SYNC_STATUS_FAILED;
        }

    }

    @Override
    public ArrayList<JSONObject> parseResponseJson(JSONObject jsonObject) throws IvyNetworkException {
        ArrayList<JSONObject> jsonObjects = new ArrayList<>();

        if (jsonObject.has("Tables")) {
            try {
                JSONArray jsonArray = jsonObject.getJSONArray(JSON_KEY);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject value = (JSONObject) jsonArray.get(i);
                    if (value.getString(ERROR_CODE).equals("0")) {
                        jsonObjects.add(value);
                    } else if (!value.getString(ERROR_CODE).equalsIgnoreCase(DATA_NOT_AVAILABLE_ERROR) && value.getBoolean(IS_MANDATORY)) {
                        throw new IvyNetworkException(value.getString(ERROR_CODE));
                    }
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (jsonObject.has("Master")) {
            try {

                if (jsonObject.getString(ERROR_CODE).equals("0")) {
                    jsonObjects.add(jsonObject);
                } else if (!jsonObject.getString(ERROR_CODE).equalsIgnoreCase(DATA_NOT_AVAILABLE_ERROR) && jsonObject.getBoolean(IS_MANDATORY)) {
                    throw new IvyNetworkException(jsonObject.getString(ERROR_CODE));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return jsonObjects;
    }

}