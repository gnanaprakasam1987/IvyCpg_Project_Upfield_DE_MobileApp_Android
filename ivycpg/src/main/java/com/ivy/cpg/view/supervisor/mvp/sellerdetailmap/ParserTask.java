package com.ivy.cpg.view.supervisor.mvp.sellerdetailmap;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;
import com.ivy.cpg.view.supervisor.customviews.DataParser;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.util.Commons;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

    private SellerDetailMapPresenter sellerMapViewPresenter;


    ParserTask(SellerDetailMapPresenter sellerMapViewPresenter) {
        this.sellerMapViewPresenter =sellerMapViewPresenter;
    }

    // Parsing the data in non-ui thread
    @Override
    protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

        JSONObject jObject;
        List<List<HashMap<String, String>>> routes = null;

        try {
            jObject = new JSONObject(jsonData[0]);
            Commons.print("ParserTask"+ jsonData[0]);

            DataParser parser = new DataParser();
            Commons.print("ParserTask"+parser.toString());

            routes = parser.parse(jObject);
            Commons.print("ParserTask"+ "Executing routes");
            Commons.print("ParserTask"+ routes.toString());

        } catch (Exception e) {
            Commons.print("ParserTask"+e.toString());
            e.printStackTrace();
        }

        return routes;
    }

    @Override
    protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
        List<HashMap<String, String>> path;
        ArrayList<LatLng> points = null;

        if (routes != null) {

            for (int i = routes.size() - 1; i >= 0; i--) {

                points = new ArrayList<>();

                path = routes.get(i);

                for (int j = 0; j < path.size(); j++) {

                    HashMap<String, String> point = path.get(j);

                    double lat = SDUtil.convertToDouble(point.get("lat"));
                    double lng = SDUtil.convertToDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

//                sellerMapViewPresenter.drawRoute(points);
            }
            sellerMapViewPresenter.drawRoute(points);
        }

    }
}