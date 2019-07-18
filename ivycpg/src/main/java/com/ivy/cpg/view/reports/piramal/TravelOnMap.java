package com.ivy.cpg.view.reports.piramal;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.ivy.lib.existing.DBUtil;
import com.ivy.maplib.OnInfoWindowElemTouchListener;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * Created by maheswaran.m on 16-10-2015.
 */
public class TravelOnMap extends IvyBaseFragment implements OnMapReadyCallback{

    private SupportMapFragment fragment;
    private GoogleMap map;
    private BusinessModel bmodel;
    private ArrayList<LatLng> geocoordinateList = null;
    private ArrayList<LatLng> mRoutecoordinateList = null;
    private ArrayList<String> mRetailerNameList;
    ArrayList<String> mRouteNameList;
    private LatLng latLng1;
    private LatLng[] markerLatLng = new LatLng[2];
    private Vector<Polyline> line;
    private String url;
    private ImageView badge;
    private OnInfoWindowElemTouchListener infoButtonListener;
    private ViewGroup infoWindow;
    private LayoutInflater layInflater;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View view = inflater
                .inflate(R.layout.fragment_travelonmap, container, false);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FragmentManager fm = getChildFragmentManager();
        fragment = (SupportMapFragment) fm.findFragmentById(R.id.map);
        if (fragment == null) {
            fragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.map, fragment).commit();
        }

        try {
            MapsInitializer.initialize(getActivity());
        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
    }

    /* @Override
    public void onStart() {

        drawLine();
    }*/

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        if (checkInternetConnection()) {
            int resultCode = GooglePlayServicesUtil
                    .isGooglePlayServicesAvailable(getActivity()
                            .getApplicationContext());
            if (resultCode == ConnectionResult.SUCCESS) {
                if (map == null) {
                    fragment.getMapAsync(this);
                }else{
                    mapFunction();
                }

            } else {
                GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(), 1);
            }

        } else {
            Toast.makeText(getActivity(), "Internet Connection Not Present", Toast.LENGTH_SHORT).show();
        }

    }

    private void addMarkers() {
        if (map != null) {

            for (int k = 0; k < geocoordinateList.size(); k++)
                map.addMarker(new MarkerOptions().position(geocoordinateList.get(k))
                        .title(mRetailerNameList.get(k))).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

        }

    }

    private String getMapsApiDirectionsUrl(String srclat, String srclon, String destlat, String destlon) {

        String OriDest = "origin=" + SDUtil.convertToDouble(srclat) + "," + SDUtil.convertToDouble(srclon) + "&destination=" + SDUtil.convertToDouble(destlat) + "," + SDUtil.convertToDouble(destlon);

        String mapKey = "key="+getString(R.string.google_maps_api_key);

        String sensor = "sensor=false&mode=walking";   //&mode=walking
        String params = OriDest + "&" + sensor+"&"+mapKey;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + params;

//        Log.i("PATH", url);
        return url;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mapFunction();
    }

    private void mapFunction(){
        try {
            MapsInitializer.initialize(getActivity());
            downloadRouteMapReport();
            downloadTravelMapReport();
            addMarkers();
           /* String url = "https://maps.googleapis.com/maps/api/directions/json?origin=12.965379,80.247531&destination=12.984606,80.174903"
                    +"&waypoints=optimize:false"
                    +"|12.972105,80.250039|"
                    +"12.979771,80.252885|"
                    +"12.981423,80.242950|"
                    +"12.981924,80.232725|"
                    +"12.976241,80.221505|"
                    +"12.988464,80.223309|"
                    +"12.990429,80.220599|"
                    +"12.997491,80.216423|&sensor=false";*/

            for (int r = 0; r < geocoordinateList.size() - 1; r++) {
                String srclat = String.valueOf(geocoordinateList.get(r).latitude);
                String srclon = String.valueOf(geocoordinateList.get(r).longitude);
                String destlat = String.valueOf(geocoordinateList.get(r + 1).latitude);
                String destlon = String.valueOf(geocoordinateList.get(r + 1).longitude);
                String url = getMapsApiDirectionsUrl(srclat, srclon, destlat, destlon);
//                        Log.i("LOOP " + r + " ", url);
                ReadTask downloadTask = new ReadTask(Color.BLUE);
                downloadTask.execute(url);
            }

            if (mRoutecoordinateList != null) {
                for (int s = 0; s < mRoutecoordinateList.size() - 1; s++) {
                    String srclat1 = String.valueOf(mRoutecoordinateList.get(s).latitude);
                    String srclon1 = String.valueOf(mRoutecoordinateList.get(s).longitude);
                    String destlat1 = String.valueOf(mRoutecoordinateList.get(s + 1).latitude);
                    String destlon1 = String.valueOf(mRoutecoordinateList.get(s + 1).longitude);

                    String url = getMapsApiDirectionsUrl(srclat1, srclon1, destlat1, destlon1);
//                            Log.i("LOOP " + s + " ", url);
                    ReadTask downloadTask = new ReadTask(Color.GREEN);
                    downloadTask.execute(url);
                }
            } else
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.no_visit_today),
                        Toast.LENGTH_SHORT).show();


            map.moveCamera(CameraUpdateFactory.newLatLngZoom(geocoordinateList.get(0), 13));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Commons.printException(e);
        }
    }


    private class ReadTask extends AsyncTask<String, Void, String> {
        int color;
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        public ReadTask(int color) {
            this.color = color;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            builder = new AlertDialog.Builder(getActivity());

            customProgressDialog(builder, "Fetching route, Please wait...");
            alertDialog = builder.create();
            alertDialog.show();

        }

        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                HttpConnection http = new HttpConnection();
                data = http.readUrl(url[0]);
            } catch (Exception e) {
                Commons.print("Background Task,"+ e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (alertDialog != null && alertDialog.isShowing()) {
                alertDialog.dismiss();
                alertDialog = null;
            }
            new ParserTask(color).execute(result);
        }
    }

    private class ParserTask extends
            AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        int color;

        ParserTask(int color) {
            this.color = color;
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                PathJSONParser parser = new PathJSONParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                Commons.printException(e);
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> routes) {

            try {
                ArrayList<LatLng> points = null;
                PolylineOptions polyLineOptions = null;

                // traversing through routes
                for (int i = 0; i < routes.size(); i++) {
                    points = new ArrayList<LatLng>();
                    polyLineOptions = new PolylineOptions();
                    List<HashMap<String, String>> path = routes.get(i);

                    for (int j = 0; j < path.size(); j++) {
                        HashMap<String, String> point = path.get(j);

                        double lat = SDUtil.convertToDouble(point.get("lat"));
                        double lng = SDUtil.convertToDouble(point.get("lng"));
                        LatLng position = new LatLng(lat, lng);

                        points.add(position);
                    }

                    polyLineOptions.addAll(points);
                    polyLineOptions.width(5);
                    polyLineOptions.color(color);
                }

                map.addPolyline(polyLineOptions);
            } catch (Exception e) {
                Commons.printException(e);
            }

        }
    }


    public class HttpConnection {
        public String readUrl(String mapsApiDirectionsUrl) throws IOException {
            String data = "";
            InputStream iStream = null;
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(mapsApiDirectionsUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                iStream = urlConnection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        iStream));
                StringBuffer sb = new StringBuffer();
                String line = "";
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                data = sb.toString();
                br.close();
            } catch (Exception e) {
                Commons.print("Exn while reading url,"+ e.toString());
            } finally {
                iStream.close();
                urlConnection.disconnect();
            }
            return data;
        }

    }


    public class PathJSONParser {

        public List<List<HashMap<String, String>>> parse(JSONObject jObject) {
            List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String, String>>>();
            JSONArray jRoutes = null;
            JSONArray jLegs = null;
            JSONArray jSteps = null;
            try {
                jRoutes = jObject.getJSONArray("routes");
                /** Traversing all routes */
                for (int i = 0; i < jRoutes.length(); i++) {
                    jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                    List<HashMap<String, String>> path = new ArrayList<HashMap<String, String>>();

                    /** Traversing all legs */
                    for (int j = 0; j < jLegs.length(); j++) {
                        jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");

                        /** Traversing all steps */
                        for (int k = 0; k < jSteps.length(); k++) {
                            String polyline = "";
                            polyline = (String) ((JSONObject) ((JSONObject) jSteps
                                    .get(k)).get("polyline")).get("points");
                            List<LatLng> list = decodePoly(polyline);

                            /** Traversing all points */
                            for (int l = 0; l < list.size(); l++) {
                                HashMap<String, String> hm = new HashMap<String, String>();
                                hm.put("lat",
                                        Double.toString(((LatLng) list.get(l)).latitude));
                                hm.put("lng",
                                        Double.toString(((LatLng) list.get(l)).longitude));
                                path.add(hm);
                            }
                        }
                        routes.add(path);
                    }
                }

            } catch (JSONException e) {
                Commons.printException(e);
            } catch (Exception e) {
            }
            return routes;
        }


        private List<LatLng> decodePoly(String encoded) {

            List<LatLng> poly = new ArrayList<LatLng>();
            int index = 0, len = encoded.length();
            int lat = 0, lng = 0;

            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;

                shift = 0;
                result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;

                LatLng p = new LatLng((((double) lat / 1E5)),
                        (((double) lng / 1E5)));
                poly.add(p);
            }
            return poly;
        }
    }

    public void downloadRouteMapReport() {

        try {
            String today = DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL);
            DBUtil db = new DBUtil(getActivity(), DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT DISTINCT RetailerID,latitude,longitude FROM OutletTimestamp WHERE latitude > 0.0 AND longitude > 0.0 AND VisitDate = '" + today + "' ORDER BY rowid");

            int i = 0;
            if (c != null) {
                mRoutecoordinateList = new ArrayList<>();
                mRouteNameList = new ArrayList<>();
                while (c.moveToNext()) {
                    {
                        mRoutecoordinateList.add(new LatLng(SDUtil.convertToDouble(c.getString(1)), SDUtil.convertToDouble(c.getString(2))));
                        mRouteNameList.add(c.getString(0));
                    }
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            // TODO: handle exception
            Commons.printException(e);
        }
    }

    public void downloadTravelMapReport() {

        try {
            DBUtil db = new DBUtil(getActivity(), DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT RM.RetailerID, RM.RetailerName, RA.latitude, RA.longitude FROM RetailerMasterInfo RMI "
                            + " INNER JOIN RetailerMaster RM ON RM.RetailerID = RMI.RetailerID"
                            + " LEFT JOIN RetailerAddress RA ON RA.RetailerId = RM.RetailerID AND (RA.latitude > 0.0 AND RA.longitude > 0.0)"
                            + " WHERE RMI.istoday = 1 ORDER BY RM.RetailerID");

            int i = 0;
            if (c != null) {
                geocoordinateList = new ArrayList<>();
                mRetailerNameList = new ArrayList<>();
                while (c.moveToNext()) {
                    {
                        geocoordinateList.add(new LatLng(SDUtil.convertToDouble(c.getString(2)), SDUtil.convertToDouble(c.getString(3))));
                        mRetailerNameList.add(c.getString(0));
                    }
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            // TODO: handle exception
            Commons.printException(e);
        }
    }

    /**
     * Check Network Connection
     */
    private boolean checkInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) getActivity()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        // test for connection
        if (cm.getActiveNetworkInfo() != null
                && cm.getActiveNetworkInfo().isAvailable()
                && cm.getActiveNetworkInfo().isConnected()) {
            return true;
        } else {
            Commons.print("Internet Check,"+ "Internet Connection Not Present");
            return false;
        }
    }
}


