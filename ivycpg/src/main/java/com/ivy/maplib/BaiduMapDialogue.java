package com.ivy.maplib;


import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ViewFlipper;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class BaiduMapDialogue extends FragmentActivity implements View.OnClickListener,BaiduMap.OnMarkerDragListener,BaiduMap.OnMarkerClickListener{


    private Marker currLocMarker;
    private MarkerOptions currLocMarkerOption;
    private GoogleMap mMap;
    private List<MarkerOptions> markerList = new ArrayList<MarkerOptions>();
    double lattitude = 0;
    double longitude = 0;
    private boolean isChanged=false;
    String retailer = "";
    private BusinessModel bmodel;
    private ViewFlipper viewFlipper;
    private Button mBtn_Search, mBtn_clear, mBtn_close;
    AutoCompleteTextView edt_search;
    //
    private static final String LOG_TAG = "IVY Retail";
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";
    HashMap<Integer, String> lstPlaceID = new HashMap<Integer, String>();

    BaiduMap baiduMap;
    MapView mapView;
    public BaiduMapDialogue() {
        // Required empty public constructor
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(this.getApplication());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(0));
        setContentView(R.layout.baidu_map_dialogue);


        viewFlipper = (ViewFlipper) findViewById(R.id.view_flipper);
        mBtn_Search = (Button) findViewById(R.id.btn_search);
        mBtn_clear = (Button) findViewById(R.id.btn_clear);
        mBtn_close = (Button) findViewById(R.id.closeButton);
        edt_search = (AutoCompleteTextView) findViewById(R.id.edt_searchproductName);
        mBtn_Search.setOnClickListener(this);
        mBtn_clear.setOnClickListener(this);

        Intent i = getIntent();
        if (i.hasExtra("lat")) {
            lattitude = i.getDoubleExtra("lat", 0);
            longitude = i.getDoubleExtra("lon", 0);
        }

        mapView=(MapView)findViewById(R.id.map_View);
        baiduMap=mapView.getMap();
      //  mapView.getOverlay().ad

    }

    @Override
    protected void onResume() {
        super.onResume();
        LatLng location = new LatLng(lattitude, longitude);


        BitmapDescriptor bdA = BitmapDescriptorFactory
                .fromResource(R.drawable.markergreen);
        OverlayOptions ooA = new com.baidu.mapapi.map.MarkerOptions().position(location).title("Current Location").icon(bdA).draggable(true);

        baiduMap.addOverlay(ooA);
        updateMapStatus(location);
    }
    public void updateMapStatus(LatLng location){
        MapStatusUpdate statusUpdate= MapStatusUpdateFactory.newLatLng(location);
        baiduMap.setMapStatus(statusUpdate);
    }
    @Override
    public void onClick(View v) {

        Button vw = (Button) v;

        if (vw == mBtn_Search) {

            viewFlipper.showNext();

        } else if (vw == mBtn_clear) {

            edt_search.setText("");
            viewFlipper.showPrevious();

        }
    }
    public void dialogClose(View v) {
        Intent intent = new Intent();
        intent.putExtra("lat", lattitude);
        intent.putExtra("lon", longitude);
        intent.putExtra("isChanged", isChanged);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onMarkerDrag(com.baidu.mapapi.map.Marker marker) {

    }

    @Override
    public void onMarkerDragStart(com.baidu.mapapi.map.Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(com.baidu.mapapi.map.Marker marker) {
        lattitude = marker.getPosition().latitude;
        longitude = marker.getPosition().longitude;
        isChanged=true;
    }

    @Override
    public boolean onMarkerClick(com.baidu.mapapi.map.Marker marker) {
        return false;
    }
}
