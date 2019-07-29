package com.ivy.cpg.view.supervisor.mvp.sellerdetailmap;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ivy.cpg.view.supervisor.mvp.models.RetailerBo;

import java.util.ArrayList;

public interface SellerDetailMapContractor {

    interface SellerDetailMapView {

        void setRetailerMarker(RetailerBo retailerMarker, MarkerOptions markerOptions);

        void focusMarker(LatLngBounds.Builder builder);

        void setOutletListAdapter(ArrayList<RetailerBo> retailerMasterList, int lastVisitRetailSeq);

        void updateSellerInfo(String timeIn,String retailerName,String target,String covered,LatLng sellerCurrentLocation);

        void updateSellerLocation(LatLng sellerCurrentLocation);

        void drawRoute(ArrayList<LatLng> points);
    }

    interface SellerDetailMapPresenter {

        void setView(SellerDetailMapView sellerMapView, Context context);

        void downloadSellerOutletAWS(int userId,String date);

        void isRealtimeLocation();

        void setSellerActivityListener(int userId,String date);

        void setSellerMovementListener(int userId, String date);

        void setSellerActivityDetailListener(int userId,String date);

        void getMarkerForFocus();

        String calculateDuration(long startTime,long endTime);

        void animateSellerMarker(final LatLng destination, final Marker marker);

        void drawRoute(ArrayList<LatLng> points);

        void downloadSellerRoute(String userId,String date);

        void removeFirestoreListener();
    }
}
