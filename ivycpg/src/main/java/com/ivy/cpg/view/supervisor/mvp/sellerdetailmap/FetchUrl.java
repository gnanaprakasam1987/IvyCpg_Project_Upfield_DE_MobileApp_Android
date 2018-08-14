package com.ivy.cpg.view.supervisor.mvp.sellerdetailmap;

import android.os.AsyncTask;

import com.ivy.sd.png.util.Commons;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

class FetchUrl extends AsyncTask<String, Void, String> {

    private SellerDetailMapPresenter sellerMapViewPresenter;


    FetchUrl(SellerDetailMapPresenter sellerMapViewPresenter) {
        this.sellerMapViewPresenter = sellerMapViewPresenter;
    }

    @Override
    protected String doInBackground(String... url) {

        String data = "";

        try {

            data = downloadUrl(url[0]);
            Commons.print("Background Task data"+ data);
        } catch (Exception e) {
            Commons.print("Background Task"+ e.toString());
        }
        return data;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        ParserTask parserTask = new ParserTask(sellerMapViewPresenter);
        parserTask.execute(result);
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuilder sb = new StringBuilder();

            String line ;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            Commons.print("downloadUrl"+ data);
            br.close();

        } catch (Exception e) {
            Commons.print("Exception"+ e.toString());
        } finally {
            if(iStream!=null)
                iStream.close();
            if(urlConnection!=null)
                urlConnection.disconnect();
        }
        return data;
    }
}