package com.ivy.cpg.view.supervisor.mvp.sellerdetailmap;

import android.content.Context;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.ivy.cpg.view.supervisor.mvp.SupervisorModelBo;

import java.util.ArrayList;

public interface SellerDetailMapContractor {

    interface SellerDetailMapView {
        void setRetailerMarker(SupervisorModelBo retailerMarker,BitmapDescriptor icon);
        void focusMarker(LatLngBounds.Builder builder);
        void setOutletListAdapter(ArrayList<SupervisorModelBo> retailerMasterList);
        void updateSellerInfo(SupervisorModelBo supervisorModelBo);
        void drawRoute(ArrayList<LatLng> points);
    }

    interface SellerDetailMapPresenter {
        void setView(SellerDetailMapView sellerMapView, Context context);
        void getSellerInfoAWS(int userId);
        void getSellerActivityListener(int userId);
        void getSellerActivityDetailListener(int userId);
        void isRealtimeLocation();
        void realtimeLocationInfoListener(int userId);
        void getMarkerForFocus();
        String convertMillisToTime(Long millis);
        void drawRoute(ArrayList<LatLng> points);
        void removeFirestoreListener();
    }
}
