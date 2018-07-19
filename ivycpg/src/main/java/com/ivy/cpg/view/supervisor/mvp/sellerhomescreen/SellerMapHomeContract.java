package com.ivy.cpg.view.supervisor.mvp.sellerhomescreen;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.ivy.cpg.view.supervisor.mvp.SupervisorModelBo;

import java.util.ArrayList;

public interface SellerMapHomeContract {

    interface SellerMapHomeView {
        void firebaseLoginFailure();
        void createMarker(SupervisorModelBo supervisorModelBo);
        void updateMaker(SupervisorModelBo supervisorModelBo);
        void focusMarker(LatLngBounds.Builder builder);
        void setSellerListAdapter(ArrayList<SupervisorModelBo> modelBoArrayList);
        void displayTotalSellerCount(int totalSellerCount);
        void updateSellerAttendance(int absentSellerCount,int marketSellerCount);
        void updateOrderValue(int totalOrderValue);
        void displayTotalOutletCount(int totalOutlet);
        void updateCoveredCount(int coveredOutlet);
        void updateUnbilledCount(int unBilledOutlet);
        void sellerProductivity(int productivityPercent);

    }

    interface SellerMapHomePresenter {
        void loginToFirebase(Context context,int userId);
        void getSellerMarkerInfo(String userId);
        void getSellerActivityInfoListener(int userId);
        void getMarkerForFocus();
        void setView(SellerMapHomeView supervisorHomeView, Context context);
        void computeSellerInfo();
        void removeFirestoreListener();
        void realtimeLocationInfoListener(int userId);
        void getSellerAttendanceInfoListener(int userId);
        void getSellerListAWS();
        void getSellerWiseRetailerAWS();
        void isRealtimeLocation();
        void animateSellerMarker(final LatLng destination, final Marker marker);
    }

}
