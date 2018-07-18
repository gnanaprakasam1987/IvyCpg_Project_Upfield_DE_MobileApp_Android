package com.ivy.cpg.view.supervisor.mvp.sellermapview;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.ivy.cpg.view.supervisor.customviews.DataParser;
import com.ivy.cpg.view.supervisor.fragments.OutletPagerDialogFragment;
import com.ivy.cpg.view.supervisor.helper.DetailsBo;
import com.ivy.cpg.view.supervisor.mvp.SupervisorModelBo;
import com.ivy.cpg.view.supervisor.mvp.sellerperformance.SellerPerformanceListActivity;
import com.ivy.lib.DialogFragment;
import com.ivy.maplib.MapWrapperLayout;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.FontUtils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

public class SellerMapViewActivity extends IvyBaseActivityNoActionBar implements SellerMapViewContractor.SellerMapView,
        OnMapReadyCallback,GoogleMap.OnMarkerClickListener,GoogleMap.OnInfoWindowClickListener  {

    private GoogleMap mMap;
    private LinkedHashMap<String,DetailsBo> sellerDetailHashmap = new LinkedHashMap<>();
    private int userId;
    private int sellerDetailPos = 0;
    ArrayList<LatLng> latLngArrayList = new ArrayList<>();
    private MapWrapperLayout mapWrapperLayout;
    private ViewGroup mymarkerview;
    private TextView tvMapInfoUserName,tvSellerName,tvSellerStartTime,tvSellerLastVisit,tvSellerPerformanceBtn,tvTarget,tvCovered;
    private ImageView imgMessage;
    private BottomSheetBehavior bottomSheetBehavior;

    private SellerMapViewPresenter sellerMapViewPresenter;
    private OutletListAdapter outletListAdapter;
    private ArrayList<SupervisorModelBo> outletListBos = new ArrayList<>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.seller_map_view_activity);

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

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
                userId = extras.getInt("SellerId");
                setScreenTitle(extras.getString("screentitle"));
            }
        } catch (Exception e) {
            setScreenTitle("Seller");
            Commons.printException(e);
        }

        sellerMapViewPresenter = new SellerMapViewPresenter();
        sellerMapViewPresenter.setView(this,SellerMapViewActivity.this);

        initViews();
        setViewValues();
    }

    private void initViews() {

        mymarkerview = (ViewGroup)getLayoutInflater().inflate(R.layout.map_custom_outlet_info_window, null);

        tvMapInfoUserName = mymarkerview.findViewById(R.id.tv_usr_name);

        tvSellerName = findViewById(R.id.tv_user_name);
        tvSellerStartTime = findViewById(R.id.tv_start_time);
        tvSellerLastVisit = findViewById(R.id.tv_address);
        tvSellerPerformanceBtn = findViewById(R.id.seller_performance_btn);
        tvTarget = findViewById(R.id.tv_target_outlet);
        tvCovered = findViewById(R.id.tv_outlet_covered);

        imgMessage = findViewById(R.id.message_img);

        tvSellerName.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,this));
        tvSellerStartTime.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,this));
        tvSellerLastVisit.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,this));
        tvSellerPerformanceBtn.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,this));
        tvTarget.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,this));
        tvCovered.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,this));

        ((TextView)findViewById(R.id.number_text)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,this));
        ((TextView)findViewById(R.id.store_text)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,this));
        ((TextView)findViewById(R.id.time_in_text)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,this));
        ((TextView)findViewById(R.id.time_out_text)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,this));

        mapWrapperLayout = findViewById(R.id.map_wrap_layout);
        mapWrapperLayout.init(mMap, getPixelsFromDp(this, getPixelsFromDp(this, 39 + 20)));

        RecyclerView recyclerView = findViewById(R.id.outlet_list);

        outletListAdapter = new OutletListAdapter(this, outletListBos);
        recyclerView.setAdapter(outletListAdapter);
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

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this.getApplicationContext(), "Enable location permission from App Settings", Toast.LENGTH_SHORT).show();

        }else{
            mMap.setMyLocationEnabled(true);
        }

        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        //map style restricting landmarks
//        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.map_style));

        mMap.setOnMarkerClickListener(this);
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
//        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {



            }
        });

        sellerMapViewPresenter.getSellerInfoAWS(userId);
        sellerMapViewPresenter.isRealtimeLocation();

        sellerMapViewPresenter.getSellerActivityListener(userId);
        sellerMapViewPresenter.getSellerActivityDetailListener(userId);

    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        Commons.print("on Marker Click called");

        double angle = 130.0;
        double x = Math.sin(-angle * Math.PI / 180) * 0.5 + getResources().getDimension(R.dimen.outlet_map_info_x);
        double y = -(Math.cos(-angle * Math.PI / 180) * 0.5 - getResources().getDimension(R.dimen.outlet_map_info_y));
        marker.setInfoWindowAnchor((float)x, (float)y);

        mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));

        marker.showInfoWindow();
//        showInfoWindow(marker);

//        if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
//            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        return true;
    }

    @Override
    public void displaySellerList() {

    }

    @Override
    public void setRetailerMarker(SupervisorModelBo retailerMarker) {
        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.marker_grey);
//        BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
        Marker marker = mMap.addMarker(retailerMarker.getMarkerOptions());
        marker.setIcon(icon);
        retailerMarker.setMarker(marker);
    }

    @Override
    public void focusMarker(final LatLngBounds.Builder builder) {

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 200));
            }
        });
    }

    @Override
    public void setOutletListAdapter(SupervisorModelBo supervisorModelBo) {
        outletListBos.clear();
        outletListBos.addAll(supervisorModelBo.getSellerDetailArrayList());
        outletListAdapter.notifyDataSetChanged();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void updateSellerInfo(SupervisorModelBo supervisorModelBo) {
        tvSellerName.setText(supervisorModelBo.getUserName());
        tvSellerStartTime.setText(getResources().getString(R.string.visit_time)+" "+convertTime(supervisorModelBo.getTimeIn()));
        tvSellerLastVisit.setText(getResources().getString(R.string.last_vist)+" "+supervisorModelBo.getRetailerName());
        tvTarget.setText(getResources().getString(R.string.targeted)+" "+supervisorModelBo.getTarget());
        tvCovered.setText(getResources().getString(R.string.covered)+" "+supervisorModelBo.getCovered());
    }


    class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }

        @Override
        public View getInfoWindow(final Marker marker) {

            tvMapInfoUserName.setText(marker.getTitle());

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
        getMenuInflater().inflate(R.menu.menu_supervisor_screen, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        ImageView searchClose = searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
        searchClose.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        searchView.setSearchableInfo(searchManager != null ? searchManager.getSearchableInfo(getComponentName()) : null);
        SearchView.OnQueryTextListener textChangeListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
//                displaySearchItem(newText);
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }
        };
        searchView.setOnQueryTextListener(textChangeListener);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_dashboard).setVisible(false);
        menu.findItem(R.id.menu_date).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i = item.getItemId();
        if (i == android.R.id.home) {
            if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            else{
                finish();
                overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            }

//        }else if(i == R.userId.menu_route){
//            drawRoute();
//        }else if(i == R.userId.menu_navigate){
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

    private int getPixelsFromDp(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public class OutletListAdapter extends RecyclerView.Adapter<OutletListAdapter.MyViewHolder> {

        private Context context;
        private ArrayList<SupervisorModelBo> outletListBos;
        OutletListAdapter(Context context,ArrayList<SupervisorModelBo> outletListBos){
            this.context = context;
            this.outletListBos = outletListBos;
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            private LinearLayout linearLayout;
            private TextView tvSerialNumber,tvStoreName,tvTimeIn,tvTimeOut,tvSkipped;

            public MyViewHolder(View view) {
                super(view);
                linearLayout = view.findViewById(R.id.outlet_item_layout);

                tvSerialNumber = view.findViewById(R.id.number_text);
                tvStoreName = view.findViewById(R.id.store_text);
                tvTimeIn = view.findViewById(R.id.time_in_text);
                tvTimeOut = view.findViewById(R.id.time_out_text);
                tvSkipped = view.findViewById(R.id.skipped_text);

                tvSerialNumber.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,context));
                tvStoreName.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,context));
                tvTimeIn.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,context));
                tvTimeOut.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,context));
                tvSkipped.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,context));

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

            holder.tvSerialNumber.setText(String.valueOf(outletListBos.get(position).getSequence()));
            holder.tvStoreName.setText(outletListBos.get(position).getRetailerName());
            holder.tvTimeIn.setText(convertTime(outletListBos.get(position).getTimeIn()));
            holder.tvTimeOut.setText(convertTime(outletListBos.get(position).getTimeOut()));

            if(convertTime(outletListBos.get(position).getTimeIn()).isEmpty()){
                holder.tvTimeIn.setVisibility(View.GONE);
                holder.tvTimeOut.setVisibility(View.GONE);
                holder.tvSkipped.setVisibility(View.VISIBLE);
            }else
                holder.tvSkipped.setVisibility(View.GONE);

        }

        @Override
        public int getItemCount() {
            return outletListBos.size();
        }
    }

    private String convertTime(Long time){

        if(time != null && time != 0) {
            Date date = new Date(time);
            Format format = new SimpleDateFormat("hh:mm a", Locale.US);
            return format.format(date);
        }else
            return "";
    }

}