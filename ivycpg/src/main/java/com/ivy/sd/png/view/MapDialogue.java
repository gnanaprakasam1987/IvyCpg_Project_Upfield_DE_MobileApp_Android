package com.ivy.sd.png.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.CustomMapFragment;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.MapWrapperLayout.OnDragListener;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

@SuppressLint("NewApi")
public class MapDialogue extends IvyBaseActivityNoActionBar implements OnDragListener, LocationListener, OnMapReadyCallback {

    private GoogleMap mMap;
    double lattitude = 0;
    double longitude = 0;


    String retailer = "";
    private BusinessModel bmodel;

    private boolean clickable = false;
    private CustomMapFragment mCustomMapFragment;

    private View mMarkerParentView;
    private ImageView mMarkerImageView;

    private int imageParentWidth = -1;
    private int imageParentHeight = -1;
    private int imageHeight = -1;
    private int centerX = -1;
    private int centerY = -1;


    AutoCompleteTextView edt_search;
    CardView currentLocView, selectedLocview;
    ImageView searchImgView;
    TextView locationTxt;
    LinearLayout layout_titleBar, layout_searchView;


    private static final String LOG_TAG = "IVY Retail";
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";
    private static final String API_KEY = "AIzaSyDkFsCSXvFYfRWHJZG84Fd9A0f-2pieVhQ";
    HashMap<Integer, String> lstPlaceID = new HashMap<Integer, String>();

    private boolean isChanged = false;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.map);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);


        // this dialog will never close when outside touch
        // this function is for devices of api above 11... for lower versions no need
        this.setFinishOnTouchOutside(false);
        toolbar = findViewById(R.id.toolbar);
        searchImgView =  findViewById(R.id.search_img_view);
        edt_search =  findViewById(R.id.search_txt_view);
        layout_titleBar =  findViewById(R.id.txt_layout);
        layout_searchView =  findViewById(R.id.search_view_lty);
        locationTxt =  findViewById(R.id.locTxtView);
//        currentLocView = (CardView)findViewById(R.id.current_loc);
        selectedLocview =  findViewById(R.id.set_loc_cardview);
        mMarkerImageView =  findViewById(R.id.marker_icon_view);
        mMarkerParentView =  findViewById(R.id.map_view);
        if (toolbar != null) {

            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle("Search Location");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        timeToView(selectedLocview);
        layout_searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout_titleBar.setVisibility(View.GONE);
                searchImgView.setVisibility(View.GONE);
                selectedLocview.setVisibility(View.GONE);
                edt_search.setVisibility(View.VISIBLE);
            }
        });


        edt_search.setAdapter(new PlacesAutoCompleteAdapter(this,
                android.R.layout.simple_dropdown_item_1line));
        edt_search.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int index,
                                    long id) {
                /*
                 * ListView lv = (ListView) arg0; ArrayAdapter<String> adapter =
				 * (ArrayAdapter<String>) arg0 .getAdapter();
				 */
                if (!edt_search.getText().toString().isEmpty())
                    edt_search.setText("");

                new downloadPlaceDetail().execute(lstPlaceID.get(index));
                edt_search.setVisibility(View.GONE);
                searchImgView.setVisibility(View.VISIBLE);
                layout_titleBar.setVisibility(View.VISIBLE);
                timeToView(selectedLocview);


            }
        });

        Intent i = getIntent();
        if (i.hasExtra("lat")) {
            lattitude = i.getDoubleExtra("lat", 0);
            longitude = i.getDoubleExtra("lon", 0);
        }
        try {

//            FragmentManager myFragmentManager = getSupportFragmentManager();
//            SupportMapFragment mySupportMapFragment = (SupportMapFragment) myFragmentManager
//                    .findFragmentById(R.id.map);

            mCustomMapFragment = ((CustomMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map));
            mCustomMapFragment.setOnDragListener(MapDialogue.this);
            mCustomMapFragment.getMapAsync(this);


        } catch (Exception e) {
            Commons.printException(e);
        }

        selectedLocview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("lat", lattitude);
                intent.putExtra("lon", longitude);
                intent.putExtra("isChanged", isChanged);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

    }

    private void setMap(){
        if (mMap != null) {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            int permissionStatus = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            }

            if (mMarkerParentView != null &&
                    mMarkerParentView.findViewById(SDUtil.convertToInt("1")) != null) {
                // Get the button view
                View locationButton = ((View) mMarkerParentView.findViewById(SDUtil.convertToInt("1")).getParent()).findViewById(SDUtil.convertToInt("2"));
                // and next place it, on bottom right (as Google Maps app)
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                        locationButton.getLayoutParams();
                // position on right bottom
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                layoutParams.setMargins(0, 0, 30, 30);

            }
            showMyLocation(truncateLocation(lattitude), truncateLocation(longitude));


            mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    clickable = true;
                    Location mylLocation = mMap.getMyLocation();
                    if (mylLocation != null) {
                        LatLng latLng = new LatLng(truncateLocation(mylLocation.getLatitude()), truncateLocation(mylLocation.getLongitude()));
                        showMeOnMap(latLng);
                        updateLocation(latLng);
                    }
                    return true;
                }
            });

        }
    }


    private void timeToView(final CardView selectCardView) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                selectCardView.setVisibility(View.VISIBLE);
            }
        }, 10 * 300); // For 3 seconds
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        imageParentWidth = mMarkerParentView.getWidth();
        imageParentHeight = mMarkerParentView.getHeight();
        imageHeight = mMarkerImageView.getHeight();

        centerX = imageParentWidth / 2;
        centerY = (imageParentHeight / 2) + (imageHeight / 2);
    }

    private ArrayList<String> autocomplete(String input) {
        ArrayList<String> resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();

        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE
                    + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=" + API_KEY);
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));
            sb.append("&sensor=false");
            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }

        } catch (MalformedURLException e) {
            Commons.printException(LOG_TAG + ",Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Commons.printException(LOG_TAG + ",Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");
            lstPlaceID.clear();
            // Extract the Place descriptions from the results
            resultList = new ArrayList<String>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                resultList.add(predsJsonArray.getJSONObject(i).getString(
                        "description"));

                lstPlaceID.put(i,
                        predsJsonArray.getJSONObject(i).getString("place_id"));
            }
        } catch (JSONException e) {
            Commons.printException(LOG_TAG + ",Cannot process JSON results", e);
        }

        return resultList;
    }


    @Override
    public void onDrag(MotionEvent motionEvent) {

        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            selectedLocview.setVisibility(View.GONE);
            if (edt_search.getVisibility() == View.VISIBLE) {
                layout_titleBar.setVisibility(View.VISIBLE);
                searchImgView.setVisibility(View.VISIBLE);
            }

        }

//           currentLocView.setVisibility(View.VISIBLE);
        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
//            currentLocView.setVisibility(View.GONE);
            Projection projection = (mMap != null && mMap
                    .getProjection() != null) ? mMap.getProjection()
                    : null;
            //
            if (projection != null) {
                LatLng centerLatLng = projection.fromScreenLocation(new Point(
                        centerX, centerY));
                updateLocation(centerLatLng);


            }
            if (clickable == true) {
                timeToView(selectedLocview);
            } else {
                selectedLocview.setVisibility(View.VISIBLE);
            }
            clickable = false;

        }

    }


    private void updateLocation(LatLng centerLatlng) {

        if (centerLatlng != null) {
            Geocoder geocoder = new Geocoder(MapDialogue.this,
                    Locale.getDefault());

            List<Address> addresses = new ArrayList<Address>();
            try {
                addresses = geocoder.getFromLocation(centerLatlng.latitude,
                        centerLatlng.longitude, 1);
            } catch (IOException e) {
                Commons.printException(e);
            }

            if (addresses != null && addresses.size() > 0) {

                String addressIndex0 = (addresses.get(0).getAddressLine(0) != null) ? addresses
                        .get(0).getAddressLine(0) : null;
                String addressIndex1 = (addresses.get(0).getAddressLine(1) != null) ? addresses
                        .get(0).getAddressLine(1) : null;
                String addressIndex2 = (addresses.get(0).getAddressLine(2) != null) ? addresses
                        .get(0).getAddressLine(2) : null;
                String addressIndex3 = (addresses.get(0).getAddressLine(3) != null) ? addresses
                        .get(0).getAddressLine(3) : null;

                String completeAddress = addressIndex0 + "," + addressIndex1;

                if (addressIndex2 != null) {
                    completeAddress += "," + addressIndex2;
                }
                if (addressIndex3 != null) {
                    completeAddress += "," + addressIndex3;
                }
                if (completeAddress != null) {
                    Commons.print("address details:" + completeAddress);
                    locationTxt.setText(completeAddress);
                }
            }
        }
        lattitude = centerLatlng.latitude;
        longitude = centerLatlng.longitude;
        isChanged = true;
    }



    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setMap();
    }


    private class PlacesAutoCompleteAdapter extends ArrayAdapter<String>
            implements Filterable {
        private ArrayList<String> resultList;

        public PlacesAutoCompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public String getItem(int index) {
            return resultList.get(index);
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        // Retrieve the autocomplete results.
                        resultList = autocomplete(constraint.toString());

                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint,
                                              FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
            return filter;
        }
    }

    private class downloadPlaceDetail extends
            AsyncTask<String, Integer, JSONObject> {

        String url = "https://maps.googleapis.com/maps/api/place/details/json";
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();

        @Override
        protected JSONObject doInBackground(String... placeid) {

            HashMap<String, Double> list = null;
            JSONObject resultObj = null;
            try {
                StringBuilder sb = new StringBuilder(url);
                sb.append("?key=" + API_KEY);
                sb.append("&placeid=" + placeid[0]);
                sb.append("&sensor=false");
                URL url = new URL(sb.toString());
                conn = (HttpURLConnection) url.openConnection();
                InputStreamReader in = new InputStreamReader(
                        conn.getInputStream());

                int read;
                char[] buff = new char[1024];
                while ((read = in.read(buff)) != -1) {
                    jsonResults.append(buff, 0, read);
                }

            } catch (MalformedURLException e) {
                Commons.printException(LOG_TAG + ",Error processing Places API URL", e);
                return resultObj;
            } catch (IOException e) {
                Commons.printException(LOG_TAG + ",Error connecting to Places API", e);
                return resultObj;
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }

            try {
                JSONObject jsonObj = new JSONObject(jsonResults.toString());

                resultObj = jsonObj.getJSONObject("result");

            } catch (JSONException e) {
                Commons.printException(LOG_TAG + ",Cannot process JSON results", e);
            }

            return resultObj;
        }

        @Override
        protected void onPostExecute(JSONObject result) {

            double lat = 0, lon = 0;
            try {

                lat = (double) result.getJSONObject("geometry")
                        .getJSONObject("location").getDouble("lat");
                lon = (double) result.getJSONObject("geometry")
                        .getJSONObject("location").getDouble("lng");

            } catch (JSONException e) {
                Commons.printException(LOG_TAG + ",Error at parsing", e);
            }


            showMyLocation(truncateLocation(lat), truncateLocation(lon));

        }
    }


    public void showMyLocation(double lat, double lon) {


        try {
            LatLng latLng = new LatLng(lat, lon);
            showMeOnMap(latLng);
            updateLocation(latLng);
            lattitude = latLng.latitude;
            longitude = latLng.longitude;
            isChanged = true;
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    @Override
    public void onStart() {
        // TODO Auto-generated method stub
        super.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();

    }


    public void showMeOnMap(LatLng lt) {


//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
//                lt.latitude, lt.longitude), 15));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(lt.latitude, lt.longitude)).zoom(15f).build();

        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }

    @Override
    public void onPause() {
        super.onPause();


    }

    @Override
    public void onBackPressed() {

        return;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i = item.getItemId();
        if (i == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * To avoid exponential value and truncate decimal digits to 10
     * @param originalLocation Original Location
     * @return Truncated location
     */
    private static double truncateLocation(Double originalLocation) {

        Double val = 0.0;
        try {

            DecimalFormat decimalFormat = new DecimalFormat("0.0000000000");
            decimalFormat.setMinimumFractionDigits(2);
            decimalFormat.setMaximumFractionDigits(10);

            val = Double.valueOf(decimalFormat.format(originalLocation));
        }catch(Exception e){
            Commons.printException(e);
        }

        return val;
    }
}
