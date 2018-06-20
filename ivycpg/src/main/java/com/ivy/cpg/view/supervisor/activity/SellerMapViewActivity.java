package com.ivy.cpg.view.supervisor.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.ivy.cpg.view.supervisor.fragments.OutletPagerDialogFragment;
import com.ivy.cpg.view.supervisor.Seller;
import com.ivy.cpg.view.supervisor.helper.SupervisorActivityHelper;
import com.ivy.cpg.view.supervisor.helper.DataParser;
import com.ivy.cpg.view.supervisor.helper.DetailsBo;
import com.ivy.cpg.view.supervisor.helper.LatLngInterpolator;
import com.ivy.lib.DialogFragment;
import com.ivy.maplib.MapWrapperLayout;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class SellerMapViewActivity extends IvyBaseActivityNoActionBar implements OnMapReadyCallback,Seller,GoogleMap.OnMarkerClickListener,GoogleMap.OnInfoWindowClickListener  {

    private GoogleMap mMap;
    private LinkedHashMap<String,DetailsBo> sellerDetailHashmap = new LinkedHashMap<>();
    private String id;
    private int count =0;
    private int sellerDetailPos = 0,sellerLatLngPos = 0;
    ArrayList<LatLng> latLngArrayList = new ArrayList<>();
    private Marker marker;
    private int trackingType;
    private MapWrapperLayout mapWrapperLayout;
    private ViewGroup mymarkerview;
    private TextView tvMapInfoUserName,tvSellerName,tvSellerStartTime,tvSellerLastVisit,tvSellerPerformanceBtn,tvTarget,tvCovered;
    private ImageView imgMessage;
    private BusinessModel bmodel;
    private BottomSheetBehavior bottomSheetBehavior;


    private LinearLayout infoWindowLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.seller_map_view_activity);

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        bmodel = (BusinessModel) getApplicationContext();

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {

            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

        Bundle extras = getIntent().getExtras();
        try {
            if (extras == null)
                setScreenTitle("Seller");
            else {
                id = extras.getString("SellerId");
                setScreenTitle(extras.getString("screentitle"));
                trackingType = extras.getInt("TrackingType");
            }
        } catch (Exception e) {
            setScreenTitle("Seller");
            Commons.printException(e);
        }

        initViews();
        setViewValues();

        String date = SDUtil.now(8);
        String nodePath;
        if(trackingType == 1)
            nodePath = "/movement_tracking_history/"+id+"_"+date;
        else
            nodePath = "/activity_tracking_history/"+id+"_"+date;
        SupervisorActivityHelper.getInstance().subscribeSellerDetails(this,this,nodePath);
    }

    private void initViews() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mymarkerview = (ViewGroup)getLayoutInflater().inflate(R.layout.map_custom_outlet_info_window, null);

        infoWindowLayout = findViewById(R.id.user_info_layout);
        tvMapInfoUserName = mymarkerview.findViewById(R.id.tv_usr_name);

        tvSellerName = findViewById(R.id.tv_user_name);
        tvSellerStartTime = findViewById(R.id.tv_start_time);
        tvSellerLastVisit = findViewById(R.id.tv_address);
        tvSellerPerformanceBtn = findViewById(R.id.seller_performance_btn);
        tvTarget = findViewById(R.id.tv_target_outlet);
        tvCovered = findViewById(R.id.tv_outlet_covered);

        imgMessage = findViewById(R.id.message_img);

        tvSellerName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        tvSellerStartTime.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        tvSellerLastVisit.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        tvSellerPerformanceBtn.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        tvTarget.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        tvCovered.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));

        ((TextView)findViewById(R.id.number_text)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        ((TextView)findViewById(R.id.store_text)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        ((TextView)findViewById(R.id.time_in_text)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        ((TextView)findViewById(R.id.time_out_text)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));

        mapWrapperLayout = findViewById(R.id.map_wrap_layout);
        mapWrapperLayout.init(mMap, getPixelsFromDp(this, getPixelsFromDp(this, 39 + 20)));

        RecyclerView recyclerView = findViewById(R.id.outlet_list);;

        MyAdapter myAdapter = new MyAdapter();
        recyclerView.setAdapter(myAdapter);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        imgMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        tvSellerPerformanceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SellerMapViewActivity.this,SellerPerformanceListActivity.class);
                startActivity(intent);
            }
        });

        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.user_info_layout));
    }

    private void setViewValues(){
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        findViewById(R.id.cardview).setVisibility(View.VISIBLE);
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        findViewById(R.id.cardview).setVisibility(View.GONE);
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        findViewById(R.id.cardview).setVisibility(View.GONE);
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMaxZoomPreference(24);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowClickListener(this);
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {
//                if(infoWindowLayout.getVisibility() == View.VISIBLE)
//                    infoWindowLayout.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public void setSellerMarker(DataSnapshot dataSnapshot) {
        String key = dataSnapshot.getKey();

        if(dataSnapshot.getValue() != null) {
            HashMap<String, Object> value = (HashMap<String, Object>) dataSnapshot.getValue();

            String userId = value.get("userId")!=null?value.get("userId").toString():"";
            String userName = value.get("userName")!=null?value.get("userName").toString():"";
            String status = value.get("status")!=null?value.get("status").toString():"";
            String activityStatus = value.get("activityType")!=null?value.get("activityType").toString():"";
            String inTime = value.get("inTime")!=null?value.get("inTime").toString():"0";
            String outTime = value.get("outTime")!=null?value.get("outTime").toString():"0";
            int batteryStatus = Integer.valueOf(value.get("batterStatus")!=null?value.get("batterStatus").toString():"0");
            double lat = SDUtil.convertToDouble(value.get("latitude")!= null?value.get("latitude").toString():"0");
            double lng = SDUtil.convertToDouble(value.get("latitude")!= null?value.get("longitude").toString():"0");

            String retailerName = value.get("RetailerName")!=null?value.get("RetailerName").toString():"";
            String orderValue = value.get("orderValue")!=null?value.get("orderValue").toString():"";
            boolean isDeviated = Boolean.valueOf(value.get("isDeviated")!=null?value.get("isDeviated").toString():"false");

            LatLng destLatLng = new LatLng(lat, lng);

//            count = count+1;
//            BitmapDescriptor icon= BitmapDescriptorFactory.fromBitmap(SupervisorActivityHelper.getInstance().setMarkerDrawable(count,this));

            BitmapDescriptor icon;
            if(orderValue.equalsIgnoreCase("1"))
                icon= BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
            else if(orderValue.equalsIgnoreCase("2"))
                icon= BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
            else if(orderValue.equalsIgnoreCase("10"))
                icon= BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
            else
                icon= BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);

            DetailsBo detailsBo = new DetailsBo();
            detailsBo.setUserName(userName);
            detailsBo.setActivityName(activityStatus);
            detailsBo.setStatus(status);
            detailsBo.setBatterStatus(batteryStatus);
            detailsBo.setInTime(inTime);
            detailsBo.setOutTime(outTime);
            detailsBo.setUserId(userId.length()>0?Integer.valueOf(userId) : 0);
            detailsBo.setTime(key);
            detailsBo.setMarker(mMap.addMarker(new MarkerOptions().flat(true).
                    title(userName).position(destLatLng).snippet(key).icon(icon)));
            detailsBo.setRetailerName(retailerName);
            detailsBo.setOrderValue(orderValue);
            detailsBo.setDeviated(isDeviated);

            sellerDetailHashmap.put(key,detailsBo);

            final LatLngBounds.Builder builder = new LatLngBounds.Builder();

            for (DetailsBo detailBo : sellerDetailHashmap.values()) {
                builder.include(detailBo.getMarker().getPosition());
            }

            mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 200));
                }
            });
        }
    }

    @Override
    public void setMarker(DataSnapshot dataSnapshot) {

    }

    @Override
    public void updateSellerInfo(DataSnapshot dataSnapshot) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Commons.print("on Marker Click called");

        double angle = 130.0;
        double x = Math.sin(-angle * Math.PI / 180) * 0.5 + 3.9;
        double y = -(Math.cos(-angle * Math.PI / 180) * 0.5 - 1.1);
        marker.setInfoWindowAnchor((float)x, (float)y);

        mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));

        marker.showInfoWindow();
//        showInfoWindow(marker);

//        if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
//            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        return true;
    }

    private void showInfoWindow(final Marker marker){


    }

    class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }

        @Override
        public View getInfoWindow(final Marker marker) {

            tvMapInfoUserName.setText("Big Text message for the info window to test size of info window");

            mapWrapperLayout.setMarkerWithInfoWindow(marker, mymarkerview);

            return mymarkerview;
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        OutletPagerDialogFragment outletPagerDialogFragment = new OutletPagerDialogFragment();
        outletPagerDialogFragment.setStyle(DialogFragment.STYLE_NO_FRAME, 0);
        outletPagerDialogFragment.setCancelable(false);
        outletPagerDialogFragment.show(getSupportFragmentManager(),"OutletPager");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_supervisor_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i = item.getItemId();
        if (i == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
//        }else if(i == R.id.menu_route){
//            drawRoute();
//        }else if(i == R.id.menu_navigate){
//            moveMarkerInPath();
        }
        return super.onOptionsItemSelected(item);
    }

    private void drawRoute(){
        if(sellerDetailPos < sellerDetailHashmap.size()-1){
            DetailsBo startLocDetailBo= (new ArrayList<>(sellerDetailHashmap.values())).get(sellerDetailPos);
            DetailsBo endLocDetailBo= (new ArrayList<>(sellerDetailHashmap.values())).get(sellerDetailPos+1);
            String url = getUrl(startLocDetailBo.getMarker().getPosition(),endLocDetailBo.getMarker().getPosition());
            Commons.print("drawRoute "+ url);
            FetchUrl fetchUrl = new FetchUrl(this);
            fetchUrl.execute(url);
        }
    }

    private String getUrl(LatLng origin, LatLng dest) {

        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
//        String sensor = "sensor=false&units=metric";
        String alternatives = "alternatives=false&mode=driving";
        String mapKey = "key="+getString(R.string.google_maps_api_key);
        String parameters = str_origin + "&" + str_dest + "&" + alternatives+"&"+mapKey;
        String output = "json";

        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
    }

    private class FetchUrl extends AsyncTask<String, Void, String> {
        Context context;

        private FetchUrl (Context context){
            this.context = context;
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

            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);
        }
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

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

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
            PolylineOptions lineOptions = null;
            Polyline mapPolyLine = null;
            ArrayList<LatLng> points;

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

                        latLngArrayList.add(position);

//                        MarkerOptions markerOptions = new MarkerOptions();
//                        markerOptions.position(position);
//                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.location_icon));
                    }

                    int colorNormalId = R.color.Black;

                    if (lineOptions == null) {

                        lineOptions = new PolylineOptions();

                        lineOptions.addAll(points);
                        lineOptions.width(10);
                        lineOptions.color(getResources().getColor(colorNormalId));
                        lineOptions.zIndex(10000000);
                        lineOptions.geodesic(true);

                        mapPolyLine = mMap.addPolyline(lineOptions);
                        mapPolyLine.setClickable(true);

                    } else {
                        mapPolyLine.setPoints(points);
                    }
                }
            }

//            latLngArrayList.add(points);

            sellerDetailPos = sellerDetailPos+1;
            drawRoute();

        }
    }

    /**
     * Animate the seller marker in map path
     */
    void moveMarkerInPath() {

        if(sellerLatLngPos < latLngArrayList.size()-1) {

            final LatLng start = latLngArrayList.get(sellerLatLngPos);
            final LatLng destination = latLngArrayList.get(sellerLatLngPos+1);

            if (marker == null) {
                BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.location_icon);
                marker = mMap.addMarker(new MarkerOptions()
                        .position(start).icon(icon));

                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                        .target(start)
                        .zoom(18f)
                        .build()));
            } else
                marker.setPosition(start);

            final LatLng startPosition = marker.getPosition();
            final LatLng endPosition = new LatLng(destination.latitude, destination.longitude);

            final LatLngInterpolator latLngInterpolator = new LatLngInterpolator.LinearFixed();

            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.setDuration(800); // duration 800ms
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    try {
                        float v = animation.getAnimatedFraction();
                        LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, endPosition);
                        marker.setPosition(newPosition);
//                        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
//                                .target(newPosition)
//                                .zoom(18f)
//                                .build()));

//                        float bearing = getBearing(startPosition, destination);
//                        if (bearing >= 0)
//                            marker.setRotation(getBearing(startPosition, destination));
                    } catch (Exception ex) {
                        Commons.printException(ex);
                    }
                }
            });
            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    sellerLatLngPos= sellerLatLngPos+1;
                    moveMarkerInPath();
                    // if (mMarker != null) {
                    // mMarker.remove();
                    // }
                    // mMarker = googleMap.addMarker(new MarkerOptions().position(endPosition).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_car)));

                }
            });
            valueAnimator.start();
        }

    }

    //Method for finding bearing between two points
    private float getBearing(LatLng begin, LatLng end) {
        double lat = Math.abs(begin.latitude - end.latitude);
        double lng = Math.abs(begin.longitude - end.longitude);

        if (begin.latitude < end.latitude && begin.longitude < end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)));
        else if (begin.latitude >= end.latitude && begin.longitude < end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 90);
        else if (begin.latitude >= end.latitude && begin.longitude >= end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)) + 180);
        else if (begin.latitude < end.latitude && begin.longitude >= end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 270);
        return -1;
    }

    private int getPixelsFromDp(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

        public class MyViewHolder extends RecyclerView.ViewHolder {
            private LinearLayout linearLayout;
            private TextView tvSerialNumber,tvStoreName,tvTimeIn,tvTimeOut;

            public MyViewHolder(View view) {
                super(view);
                linearLayout = view.findViewById(R.id.outlet_item_layout);

                tvSerialNumber = view.findViewById(R.id.number_text);
                tvStoreName = view.findViewById(R.id.store_text);
                tvTimeIn = view.findViewById(R.id.time_in_text);
                tvTimeOut = view.findViewById(R.id.time_out_text);

                tvSerialNumber.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                tvStoreName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                tvTimeIn.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                tvTimeOut.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));

            }
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.outlet_list_item, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {

            if(position%2 == 0)
                holder.linearLayout.setBackgroundColor(getResources().getColor(R.color.white));
            else
                holder.linearLayout.setBackgroundColor(getResources().getColor(R.color.outlet_item_bg));

        }

        @Override
        public int getItemCount() {
            return 20;
        }
    }

}