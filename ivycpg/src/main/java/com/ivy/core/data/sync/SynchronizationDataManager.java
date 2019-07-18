package com.ivy.core.data.sync;

import com.android.volley.VolleyError;
import com.google.gson.JsonObject;
import com.ivy.core.model.UrlMaster;
import com.ivy.core.network.IvyNetworkException;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Vector;

import io.reactivex.Single;

public interface SynchronizationDataManager {

    Single<String> getSyncUrl(String code);

    Single<ArrayList<UrlMaster>> getSyncUrlList(String code);

    String generateChecksum(String input);

    Single<Vector<String>> uploadDataToServer(String headerInfo, String data, String appendUrl);

    Single<String[]> getAuthToken();

    Single<Boolean> parseAndInsertJSON(JSONObject jsonObject, boolean isDeleteTable);

    Single<JSONObject> downloadDataFromServer(String url, JSONObject requestBody, boolean isMandatory);

    void stopAllRequest();

    String getErrorMessage(VolleyError volleyError);

    ArrayList<JSONObject> parseResponseJson(JSONObject jsonObject) throws IvyNetworkException;

}
