package com.ivy.cpg.view.supervisor.mvp.sellermapview;

import android.content.Context;

import com.google.android.gms.maps.model.LatLngBounds;
import com.ivy.cpg.view.supervisor.mvp.SupervisorModelBo;

public interface SellerMapViewContractor {

    interface SellerMapView {
        void displaySellerList();
        void setRetailerMarker(SupervisorModelBo retailerMarker);
        void focusMarker(LatLngBounds.Builder builder);
        void setOutletListAdapter(SupervisorModelBo supervisorModelBo);
        void updateSellerInfo(SupervisorModelBo supervisorModelBo);
    }

    interface SellerViewPresenter{
        void setView(SellerMapView sellerMapView, Context context);
        void getSellerInfoAWS(int userId);
        void getSellerActivityListener(int userId);
        void getSellerActivityDetailListener(int userId);
        void isRealtimeLocation();
        void realtimeLocationInfoListener();
        void getMarkerForFocus();
        void getSellerList(int sellerInfoType);
    }
}
