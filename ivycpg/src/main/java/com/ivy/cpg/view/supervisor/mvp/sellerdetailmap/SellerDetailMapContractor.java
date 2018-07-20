package com.ivy.cpg.view.supervisor.mvp.sellerdetailmap;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ivy.cpg.view.supervisor.mvp.RetailerBo;

import java.util.ArrayList;

public interface SellerDetailMapContractor {

    interface SellerDetailMapView {

        void setRetailerMarker(RetailerBo retailerMarker, MarkerOptions markerOptions);

        void focusMarker(LatLngBounds.Builder builder);

        void setOutletListAdapter(ArrayList<RetailerBo> retailerMasterList);

        void updateSellerInfo(String timeIn,String retailerName,String target,String covered,LatLng sellerCurrentLocation);

        void drawRoute(ArrayList<LatLng> points);
    }

    interface SellerDetailMapPresenter {

        void setView(SellerDetailMapView sellerMapView, Context context);

        void downloadSellerOutletAWS(int userId);

        void isRealtimeLocation();

        void setSellerActivityListener(int userId);

        void setSellerActivityDetailListener(int userId);

        void getMarkerForFocus();

        String convertMillisToTime(Long millis);

        void drawRoute(ArrayList<LatLng> points);

        void removeFirestoreListener();
    }
}
