package com.ivy.core.network;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ivy.lib.Utils;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class IvyJsonObjectRequest extends JsonObjectRequest {

    private static final String SECURITY_HEADER = "SECURITY_TOKEN_KEY";

    public static final String TAG_JSON_OBJ = "json_obj_req";

    private static final String MOBILE_DATE_TIME = "MobileDateTime";

    private String securityKey;


    private RetryPolicy policy = new DefaultRetryPolicy(
            (int) TimeUnit.SECONDS.toMillis(30),
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

    public IvyJsonObjectRequest(int method, String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener, String securityKey) {
        super(method, url, jsonRequest, listener, errorListener);
        setShouldCache(false);
        this.securityKey =securityKey;
        appendGenericHeaderInfo(jsonRequest);
    }


    public IvyJsonObjectRequest(int method, String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
        setShouldCache(false);
        appendGenericHeaderInfo(jsonRequest);
    }


    public IvyJsonObjectRequest(String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(url, jsonRequest, listener, errorListener);
        setShouldCache(false);
        appendGenericHeaderInfo(jsonRequest);
    }

    private void appendGenericHeaderInfo(JSONObject jsonRequest){

        try {
            jsonRequest.put(MOBILE_DATE_TIME, Utils.getDate("yyyy/MM/dd HH:mm:ss"));
            jsonRequest.put("MobileUTCDateTime",
                    Utils.getGMTDateTime("yyyy/MM/dd HH:mm:ss"));
            if (!DataMembers.backDate.isEmpty())
                jsonRequest.put("RequestDate",
                        DateTimeUtils.now(DateTimeUtils.DATE_TIME_NEW));
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }


    @Override
    public Request<?> setTag(Object tag) {
        return super.setTag(TAG_JSON_OBJ);
    }

    @Override
    public RetryPolicy getRetryPolicy() {
        return policy;
    }

    public void setSecurityKey(String securityKey) {
        this.securityKey = securityKey;
    }

    @Override
    public int getMethod() {
        return Request.Method.POST;
    }

    @Override
    public String getUrl() {
        return super.getUrl();
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>();
        headers.put(SECURITY_HEADER, securityKey);
        headers.put("Content-Type", "application/json; charset=utf-8");

        return headers;
    }

    @Override
    protected void deliverResponse(JSONObject response) {
        super.deliverResponse(response);
    }

    @Override
    public void deliverError(VolleyError error) {
        super.deliverError(error);
    }

}
