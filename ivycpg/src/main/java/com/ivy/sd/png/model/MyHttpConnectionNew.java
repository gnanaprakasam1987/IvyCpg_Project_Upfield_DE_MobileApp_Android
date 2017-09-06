package com.ivy.sd.png.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.ivy.sd.png.util.Commons;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by rajesh.k on 17-07-2015.
 */
public class MyHttpConnectionNew {

    public static final int GET = 0;
    public static final int POST = 1;
    public static final int PUT = 2;
    public static final int DELETE = 3;
    public static final int BITMAP = 4;
    private String url;
    private int method;
    private String data;
    private Vector<String> result;
    private List<NameValuePair> params = new ArrayList();
    private Bitmap bitmap;
    private HttpClient httpClient;
    private String headerKey;
    private String headervalue;

    public JSONObject getParamsJsonObject() {
        return paramsJsonObject;
    }

    public void setParamsJsonObject(JSONObject paramsJsonObject) {
        this.paramsJsonObject = paramsJsonObject;
    }

    private JSONObject paramsJsonObject;



    public Header[] getResponseHeader() {
        return responseHeader;
    }

    public void setResponseHeader(Header[] responseHeader) {
        this.responseHeader = responseHeader;
    }

    private Header[] responseHeader;

    public MyHttpConnectionNew() {
    }

    public void create(int method, String url, String data) {
        this.method = method;
        this.url = url;
        this.data = data;
    }

    public Bitmap getBitmap() {
        return this.bitmap;
    }

    public void addHeader(String var,String val){
        this.headerKey=var;
        this.headervalue=val;

    }
    public void addParam(String var, String val) {
        this.params.add(new BasicNameValuePair(var, val));
    }

    public void addParam(String var, int val) {
        this.params.add(new BasicNameValuePair(var, String.valueOf(val)));
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Vector<String> getResult() {
        return this.result;
    }

    public void setResult(Vector<String> result) {
        this.result = result;
    }

    public void get(String url) {
        this.create(0, url, (String)null);
    }

    public void post(String url, String data) {
        this.create(1, url, data);
    }

    public void put(String url, String data) {
        this.create(2, url, data);
    }

    public void delete(String url) {
        this.create(3, url, (String)null);
    }

    public void bitmap(String url) {
        this.create(4, url, (String)null);
    }



    public void connectMe() {
        this.httpClient = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(this.httpClient.getParams(), 90000);
        HttpConnectionParams.setSoTimeout(this.httpClient.getParams(), 90000);

        try {
            HttpResponse e = null;
            switch(this.method) {
                case 0:
                    HttpGet httpGet = new HttpGet(this.url);
                    if (headervalue != null) {
                        httpGet.addHeader(headerKey, headervalue);
                    }

                    e = this.httpClient.execute(httpGet);
                    break;
                case 1:
                    try {
                        HttpPost httpPut1 = new HttpPost(this.url);
                        if (paramsJsonObject != null) {

                            StringEntity entity = new StringEntity(paramsJsonObject.toString(), HTTP.UTF_8);
                            entity.setContentType("application/json");
                            httpPut1.setEntity(entity);
                        } else {
                          /*  httpPut1.setHeader(HTTP.CONTENT_TYPE,
                                    "application/x-www-form-urlencoded;charset=UTF-8");*/

                            UrlEncodedFormEntity p_entity = new UrlEncodedFormEntity(this.params, HTTP.UTF_8);

                            httpPut1.setEntity(p_entity);
                        }
                        if(headervalue!=null) {
                            httpPut1.addHeader(headerKey, headervalue);
                        }

                        e = this.httpClient.execute(httpPut1);


                    } catch (Exception var4) {
                        Commons.printException(""+var4);
                        this.setResult((Vector)null);
                    }
                    break;
                case 2:
                    HttpPut httpPut = new HttpPut(this.url);
                    httpPut.setEntity(new StringEntity(this.data));
                    httpPut.addHeader(headerKey, headervalue);
                    e = this.httpClient.execute(httpPut);

                    break;
                case 3:
                    e = this.httpClient.execute(new HttpDelete(this.url));

                    break;
                case 4:
                    e = this.httpClient.execute(new HttpGet(this.url));
                    this.processBitmapEntity(e.getEntity());
            }

            if(this.method < 4) {
                this.processEntity(e.getEntity());
                this.setResponseHeader(e.getAllHeaders());
            }
        } catch (Exception var5) {
            Commons.printException(this.getClass().getName()+ ",Exception occured while connecting to server");
            this.setResult((Vector)null);
        }

    }

    private void processEntity(HttpEntity entity) throws IllegalStateException, IOException {
        BufferedReader rd = new BufferedReader(new InputStreamReader(entity.getContent()));
        String line = "";
        this.result = new Vector();

        while((line = rd.readLine()) != null) {
            this.result.addElement(line);
        }

    }

    private void processBitmapEntity(HttpEntity entity) throws IOException {
        BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
        Bitmap bm = BitmapFactory.decodeStream(bufHttpEntity.getContent());
        this.setBitmap(bm);
    }

}


