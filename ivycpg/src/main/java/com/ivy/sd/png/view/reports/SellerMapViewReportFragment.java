package com.ivy.sd.png.view.reports;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatDrawableManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.ui.IconGenerator;
import com.ivy.maplib.MapWrapperLayout;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.OutletReportBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.HomeScreenActivity;
import com.ivy.sd.png.view.SellerListFragment;
import com.ivy.sd.png.view.profile.DirectionsJSONParser;

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

/**
 * Created by rajkumar.s on 10/27/2017.
 */

public class SellerMapViewReportFragment extends SupportMapFragment implements SellerListFragment.SellerSelectionInterface{

    View view;
    BusinessModel bmodel;

    private DrawerLayout mDrawerLayout;
    FrameLayout drawer;

    private ArrayList<OutletReportBO>lstReports;

    private GoogleMap mMap;
    View mapView;
    MapWrapperLayout mainLayout;
    private LayoutInflater layInflater;

    ViewGroup infoWindow;
    TextView infoTitle,infoLocName,infoAddress,infoTimeIn,infoTimeOut,infoSalesValue,infoSequence,infoSeller;
    ImageView iv_planned,iv_deviated;
    private List<MarkerOptions> markerList = null;
    private LatLngBounds bounds = null;
    private LatLngBounds.Builder builder = null;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mapView = super.onCreateView(inflater, container, savedInstanceState);

        try {
            getActivity().getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

            view = inflater.inflate(R.layout.fragment_seller_mapview, container, false);

            layInflater = inflater;

            bmodel = (BusinessModel) getActivity().getApplicationContext();
            bmodel.setContext(getActivity());

            if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.sessionout_loginagain),
                        Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }


            if (lstReports == null)
                lstReports = bmodel.reportHelper.downloadOutletReports();


        }
        catch (Exception ex){
            Commons.printException(ex);
        }

        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mainLayout = (MapWrapperLayout) view
                .findViewById(R.id.planningmapnew);
        mainLayout.addView(mapView);

    }

    @Override
    public void onStart() {
        super.onStart();

        try {
            setHasOptionsMenu(true);
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setDisplayShowHomeEnabled(true);
            getActionBar().setHomeButtonEnabled(true);

            ActionBarDrawerToggle mDrawerToggle;
            drawer = (FrameLayout) getView().findViewById(R.id.right_drawer);

            mDrawerLayout = (DrawerLayout) getView().findViewById(
                    R.id.drawer_layout);

            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                    GravityCompat.START);
            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                    GravityCompat.END);
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

            mDrawerToggle = new ActionBarDrawerToggle(getActivity(),
                    mDrawerLayout,
                    R.string.ok,
                    R.string.close
            ) {
                public void onDrawerClosed(View view) {
                   /* if (getActionBar() != null) {
                        ((TextView)getActivity(). findViewById(R.id.tv_toolbar_title)).setText(bmodel.mSelectedActivityName);
                    }
*/
                    getActivity().supportInvalidateOptionsMenu();
                }

                public void onDrawerOpened(View drawerView) {
                    if (getActionBar() != null) {
                        ((TextView)getActivity(). findViewById(R.id.tv_toolbar_title)).setText(getResources().getString(R.string.filter));
                    }

                    getActivity().supportInvalidateOptionsMenu();
                }
            };

            mDrawerLayout.addDrawerListener(mDrawerToggle);
            mDrawerLayout.closeDrawer(GravityCompat.END);

            initializeMap();

        }
        catch (Exception ex){
            Commons.printException(ex);
        }


    }

    private void initializeMap(){

        mMap = this.getMap();
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);

        float pxlDp = 39 + 20;
        mainLayout.init(mMap, getPixelsFromDp(getActivity(), pxlDp));
        this.infoWindow = (ViewGroup) layInflater.inflate(
                R.layout.outlet_info_window, null);
        this.infoTitle = (TextView) infoWindow.findViewById(R.id.tv_storename);
        this.infoLocName = (TextView) infoWindow.findViewById(R.id.tv_loc_name);
        this.infoAddress = (TextView) infoWindow.findViewById(R.id.tv_address);
        this.infoTimeIn = (TextView) infoWindow.findViewById(R.id.tv_time_in);
        this.infoTimeOut = (TextView) infoWindow.findViewById(R.id.tv_time_out);
        this.infoSalesValue = (TextView) infoWindow.findViewById(R.id.tv_sales_value);
        this.iv_planned = (ImageView) infoWindow.findViewById(R.id.iv_planned);
        this.iv_deviated = (ImageView) infoWindow.findViewById(R.id.iv_deviate);
        this.infoSeller = (TextView) infoWindow.findViewById(R.id.tv_seller);

        this.infoTitle.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        this.infoLocName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        this.infoAddress.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        this.infoTimeIn.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        this.infoTimeOut.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        this.infoSalesValue.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        this.infoSeller.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        ((TextView) infoWindow.findViewById(R.id.lbl_time_in)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) infoWindow.findViewById(R.id.lbl_time_out)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) infoWindow.findViewById(R.id.lbl_sales_value)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setMyLocationEnabled(false);
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                updateUserSelection(null,true);
            }
        });

    }

    public static int getPixelsFromDp(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_seller_mapview, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        boolean drawerOpen = mDrawerLayout.isDrawerOpen(GravityCompat.END);
        menu.findItem(R.id.menu_users).setVisible(!drawerOpen);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.menu_users) {
            loadUsers();
        }
        else if(i==android.R.id.home){
            if (mDrawerLayout.isDrawerOpen(GravityCompat.END))
                mDrawerLayout.closeDrawers();
            else {
                onBackButtonClick();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void onBackButtonClick() {
        Intent i = new Intent(getActivity(), HomeScreenActivity.class);
        i.putExtra("menuCode", "MENU_REPORT");
        i.putExtra("title", "aaa");
        startActivity(i);
        getActivity().finish();
        getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);

    }

    private void loadUsers(){

        try {
            mDrawerLayout.openDrawer(GravityCompat.END);

            android.support.v4.app.FragmentManager fm = getActivity().getSupportFragmentManager();
            SellerListFragment frag = (SellerListFragment) fm.findFragmentByTag("filter");
            android.support.v4.app.FragmentTransaction ft = fm.beginTransaction();
            if (frag != null)
                ft.detach(frag);

            SellerListFragment fragment = new SellerListFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("users",bmodel.reportHelper.getLstUsers());
            fragment.setArguments(bundle);

            ft.replace(R.id.right_drawer, fragment, "filter");
            ft.commit();

            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
        }
        catch (Exception ex){
            Commons.printException(ex);
        }
    }

    @Override
    public void updateUserSelection(ArrayList<Integer> mSelectedUsers, boolean isAlluser) {
        try {

            if (lstReports != null) {

                LatLng storeLatLng;
                markerList = new ArrayList<>();

                if (mMap != null) {
                    mMap.clear();
                }
                mDrawerLayout.closeDrawers();

                //If nothing selected then showing default text
                if (getActionBar() != null) {
                    ((TextView) getActivity().findViewById(R.id.tv_toolbar_title)).setText(bmodel.mSelectedActivityName);
                }

                ArrayList<Integer> lstLastVisitedRetailerIds = null;
                if (isAlluser) {
                    lstLastVisitedRetailerIds = new ArrayList<>();
                        for (OutletReportBO userBo : bmodel.reportHelper.getLstUsers()) {
                            lstLastVisitedRetailerIds.add(bmodel.reportHelper.downloadlastVisitedRetailer(userBo.getUserId()));
                        }

                }

                // get total retailers for selected user to show end marker
                int totalRetailers = 0;
                if (!isAlluser) {
                    for (OutletReportBO bo : lstReports) {
                        if (mSelectedUsers != null && mSelectedUsers.contains(bo.getUserId())) {
                            totalRetailers += 1;
                        }
                    }
                }

                    int sequence = 0;
                    for (OutletReportBO bo : lstReports) {
                        if ((isAlluser && lstLastVisitedRetailerIds.contains(bo.getRetailerId()))
                                || (mSelectedUsers != null && mSelectedUsers.contains(bo.getUserId()))) {

                            if (isValidLatLng(bo.getLatitude(), bo.getLongitude())) {
                                if (bo.getLatitude() != 0 && bo.getLongitude() != 0) {

                                    if (!isAlluser) {
                                        sequence += 1;
                                        bo.setSequence(sequence);

                                        //update screen title
                                        if (getActionBar() != null) {
                                            ((TextView) getActivity().findViewById(R.id.tv_toolbar_title)).setText(bo.getUserName());
                                        }

                                    } else {
                                        bo.setSequence(0);

                                        //update screen title
                                        if (getActionBar() != null) {
                                            ((TextView) getActivity().findViewById(R.id.tv_toolbar_title)).setText(bmodel.mSelectedActivityName);
                                        }

                                    }

                                    storeLatLng = new LatLng(bo.getLatitude(), bo.getLongitude());

                                    IconGenerator iconFactory = new IconGenerator(getActivity());
                                    MarkerOptions markerOptions = new MarkerOptions().title(bo.getRetailerName()).
                                            position(storeLatLng).
                                            anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV())
                                            .snippet(bo.getRetailerId() + "");

                                    if (bo.getSequence() == 1) {
                                        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon("S")));
                                    } else if (bo.getSequence() != 0 && bo.getSequence() == totalRetailers) {
                                        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon("E")));
                                    } else {
                                        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(getBitmapFromVectorDrawable(getActivity(), R.drawable.ic_marker_person)));
                                    }
                                    markerList.add(markerOptions);

                                    getMap().addMarker(markerOptions);


                                }
                            }
                        }
                    }


                //fit all markers into the screen
                if (markerList.size() > 0) {
                    if (builder == null) {
                        builder = new LatLngBounds.Builder();
                    } else {
                        builder = null;
                        builder = new LatLngBounds.Builder();
                    }

                    for (int i = 0; i < markerList.size(); i++) {
                        LatLng latLng = markerList.get(i).getPosition();
                        double lat = latLng.latitude;
                        double lng = latLng.longitude;
                        if (lat != 0.0 && lng != 0.0) {
                            builder.include(markerList.get(i).getPosition());

                        }
                    }
                    if (bounds == null) {
                        bounds = builder.build();
                    } else {
                        bounds = null;
                        bounds = builder.build();
                    }

                    if (bounds != null)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,
                                100));
                }

                if(!isAlluser) {
                    drawMapRoute();
                }

            }


        } catch (Exception ex) {
            Commons.printException(ex);
        }


    }


    private class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {


        public CustomInfoWindowAdapter() {
            // CustomInfoWindowAdapter
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }

        @Override
        public View getInfoWindow(final Marker marker) {

            try {
                infoTitle.setText(marker.getTitle());
                int retailerd = Integer.parseInt(marker.getSnippet());
                for (OutletReportBO bo : lstReports) {
                    if (bo.getRetailerId() == retailerd) {
                        infoLocName.setText(bo.getLocationName());
                        infoAddress.setText(bo.getAddress());
                        infoTimeIn.setText(bo.getTimeIn());
                        infoTimeOut.setText(bo.getTimeOut());
                        infoSalesValue.setText(bo.getSalesValue());
                        infoSeller.setText("Seller: "+bo.getUserName());

                        if(bo.getIsPlanned()==1) {
                            iv_planned.setVisibility(View.VISIBLE);
                            iv_deviated.setVisibility(View.GONE);
                        }
                        else if(bo.getIsPlanned()==0&&bo.getIsVisited()==1) {
                            iv_deviated.setVisibility(View.VISIBLE);
                            iv_planned.setVisibility(View.GONE);
                        }
                        else{
                            iv_deviated.setVisibility(View.GONE);
                            iv_planned.setVisibility(View.GONE);
                        }

                        break;
                    }
                }
            }
            catch (Exception ex){
                Commons.printException(ex);
            }

            mainLayout.setMarkerWithInfoWindow(marker, infoWindow);
            return infoWindow;
        }

    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = AppCompatDrawableManager.get().getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    /**
     * Method used to validate lat long values.
     *
     * @param lat
     * @param lng
     * @return
     */
    public boolean isValidLatLng(double lat, double lng) {
        if (lat < -90 || lat > 90) {
            return false;
        } else if (lng < -180 || lng > 180) {
            return false;
        }
        return true;
    }

    @Override
    public void updateClose() {
        mDrawerLayout.closeDrawers();

        if (getActionBar() != null) {
            ((TextView)getActivity(). findViewById(R.id.tv_toolbar_title)).setText(bmodel.mSelectedActivityName);
        }
    }


    private void drawMapRoute() {
        try {
            if (markerList.size() >= 2) {
                LatLng origin = markerList.get(0).getPosition();
                LatLng dest = markerList.get(markerList.size() - 1).getPosition();

                // Getting URL to the Google Directions API
                String url = getDirectionsUrl(origin, dest);
                DownloadTask downloadTask = new DownloadTask();
                downloadTask.execute(url);


            }
        }
        catch (Exception ex){
            Commons.printException(ex);
        }
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude; // Destination of route
        String sensor = "sensor=false";
        String waypoints = "";

        for (int i = 2; i < markerList.size(); i++) {
            LatLng point = markerList.get(i).getPosition();
            if (i == 2)
                waypoints = "waypoints=";
            waypoints += point.latitude + "," + point.longitude + "|";
        }

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + waypoints;
        String output = "json";

        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            String data = "";
            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Commons.printException("Background Task", e);
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);
        }
    }

    /**
     * A method to download json data from url
     */
    @SuppressLint("LongLogTag")
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream;
        HttpURLConnection urlConnection = null;
        try {
            // Creating an http connection to communicate with url
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            br.close();
            iStream.close();
            urlConnection.disconnect();
        } catch (Exception e) {
            Commons.printException("Exception while downloading url", e);
        }
        return data;
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {

            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);
                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }
                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(5);
                lineOptions.color(Color.RED);
            }
            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null) {
                mMap.addPolyline(lineOptions);
            }
        }
    }
}
