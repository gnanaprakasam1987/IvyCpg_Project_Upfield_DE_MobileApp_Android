package com.ivy.sd.png.model;

import android.net.Uri;

import com.ivy.sd.png.util.Commons;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Created by rajesh.k on 17-07-2015.
 */
public class MyHttpConnectionNew {

    public static final int POST = 1;
    private String url;
    private int method;
    private String data;
    private Vector<String> result;
    HashMap<String, String> params = new HashMap<>();
    private String headerKey;
    private String headervalue;
    private boolean isFromWebActivity = false;
    Map<String, List<String>> headerFields;

    public JSONObject getParamsJsonObject() {
        return paramsJsonObject;
    }

    public void setParamsJsonObject(JSONObject paramsJsonObject) {
        this.paramsJsonObject = paramsJsonObject;
    }

    private JSONObject paramsJsonObject;

    public MyHttpConnectionNew() {
    }

    public void create(int method, String url, String data) {
        this.method = method;
        this.url = url;
        this.data = data;
    }

    public void addHeader(String var, String val) {
        this.headerKey = var;
        this.headervalue = val;

    }

    public void addParam(String var, String val) {
        params.put(var, val);
    }

    public void addParam(String var, int val) {
        params.put(var, String.valueOf(val));
    }

    public Vector<String> getResult() {
        return this.result;
    }

    public void setResult(Vector<String> result) {
        this.result = result;
    }

    public void get(String url) {
        this.create(0, url, (String) null);
    }

    public void post(String url, String data) {
        this.create(1, url, data);
    }

    public void put(String url, String data) {
        this.create(2, url, data);
    }

    public void delete(String url) {
        this.create(3, url, (String) null);
    }

    public void bitmap(String url) {
        this.create(4, url, (String) null);
    }


    public void connectMe() {
        URL uri = null;
        HttpURLConnection con = null;
        OutputStream os = null;
        try {
            switch (this.method) {
                case 1:
                    try {
                        if (paramsJsonObject != null) {
                            uri = new URL(url.trim());
                            con = (HttpURLConnection) uri.openConnection();
                            con.setRequestMethod("POST");
                            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                            if (getIsFromWebActivity())//body of response would be blank for web url call, hence default gzip compression throws eof exception on blank response. to avoid compression accept encoding is given blank since there is no body,only header is being used
                                con.setRequestProperty("Accept-Encoding", "");
                            // For POST only - START
                            con.setDoOutput(true);
                            if (headervalue != null) {
                                con.setRequestProperty(headerKey, headervalue);
                            }
                            os = con.getOutputStream();
                            os.write(paramsJsonObject.toString().getBytes("UTF-8"));
                            os.flush();
                            os.close();
                            // For POST only - END
                        } else if (params != null && params.size() > 0) {
                            URL serverUrl = new URL(url);
                            con = (HttpURLConnection) serverUrl.openConnection();
                            con.setDoInput(true);
                            con.setRequestMethod("POST");
                            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                            con.setDoOutput(true);
                            if (headervalue != null) {
                                con.setRequestProperty(headerKey, headervalue);
                            }
                            //didn't remove comment for future reference if needed
//                            StringBuilder resultbuilder = new StringBuilder();
//                            for (Map.Entry<String, String> entry : params.entrySet()) {
//                                resultbuilder.append((resultbuilder.length() > 0 ? "&" : "") + entry.getKey() + ":" + entry.getOutletData());//appends: key=value (for first param) OR &key=value(second and more)
//                            }
//                            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
//                            writer.write(resultbuilder.toString());
//                            writer.close();

                            // For POST only - START
                            Uri.Builder builder = new Uri.Builder();
                            for (Map.Entry<String, String> entry : params.entrySet()) {
                                builder.appendQueryParameter(entry.getKey(), entry.getValue());
                            }
                            String query = builder.build().getEncodedQuery();
                            os = con.getOutputStream();

                            os.write(query.getBytes());
                            os.flush();
                            os.close();

//                            con.connect();

                        }
                        this.processEntity(con.getInputStream());
                        this.setResponseHeader(con.getHeaderFields());

                    } catch (Exception ex) {
                        Commons.printException("MyHttpConnectionNew" + ",Error while posting data:: " + ex);
                        this.setResult((Vector) null);
                        os.flush();
                        os.close();
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception var5) {
            Commons.printException(this.getClass().getName() + ",Exception occured while connecting to server");
            this.setResult((Vector) null);
        }

    }

    private void setResponseHeader(Map<String, List<String>> headerFields) {
        this.headerFields = headerFields;
    }

    public Map<String, List<String>> getResponseHeaderField() {
        return headerFields;
    }

    public void setIsFromWebActivity(boolean bool) {
        isFromWebActivity = bool;
    }

    public boolean getIsFromWebActivity() {
        return isFromWebActivity;
    }

    private void processEntity(InputStream in) throws IllegalStateException, IOException {
        BufferedReader rd = new BufferedReader(new InputStreamReader(in));
        String line = "";
        this.result = new Vector();

        while ((line = rd.readLine()) != null) {
            this.result.addElement(line);
        }

    }

}


