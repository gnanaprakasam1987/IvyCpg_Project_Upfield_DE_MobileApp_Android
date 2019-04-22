package com.ivy.ui.retailer.data;

import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.util.Commons;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Single;

public class RetailerDataManagerImpl implements RetailerDataManager {
    private DBUtil mDbUtil;

    private AppDataProvider appDataProvider;

    @Inject
    RetailerDataManagerImpl(@DataBaseInfo DBUtil dbUtil, AppDataProvider appDataProvider) {
        mDbUtil = dbUtil;
        this.appDataProvider = appDataProvider;
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
    public void tearDown() {
        shutDownDb();
    }

    @Override
    public Single<String> getRoutePath(String url) {

        return Single.fromCallable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                JSONParser jParser = new JSONParser();
                return jParser.getJSONFromUrl(url);
            }
        });
    }

    public class JSONParser {

        InputStream is = null;
        String json = "";

        JSONParser() {
        }

        public String getJSONFromUrl(String url) {
            try {
                URL urlobj = new URL(url);
                HttpURLConnection urlConnection = (HttpURLConnection) urlobj.openConnection();
                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    is = urlConnection.getInputStream();
                }

            } catch (Exception e) {
                Commons.printException(e);
            }
            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(is, "iso-8859-1"), 8);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                    sb.append("\n");
                }

                json = sb.toString();
                is.close();
            } catch (Exception e) {
                Commons.printException("Buffer Error," + e);
            }
            return json;

        }
    }


}
