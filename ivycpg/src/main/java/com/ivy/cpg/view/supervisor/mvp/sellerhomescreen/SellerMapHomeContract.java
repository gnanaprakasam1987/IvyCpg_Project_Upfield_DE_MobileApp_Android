package com.ivy.cpg.view.supervisor.mvp.sellerhomescreen;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ivy.cpg.view.supervisor.mvp.models.SellerBo;

import java.util.ArrayList;

public interface SellerMapHomeContract {

    interface SellerMapHomeView {

        void firebaseLoginSuccess();

        void firebaseLoginFailure();

        void createMarker(SellerBo sellerBo,MarkerOptions markerOptions);

        void updateMaker(LatLng destinationLatLng, Marker marker);

        void focusMarker(LatLngBounds.Builder builder);

        void setSellerListAdapter(ArrayList<SellerBo> modelBoArrayList);

        void displayTotalSellerCount(int totalSellerCount);

        void updateSellerAttendance(int absentSellerCount,int marketSellerCount);

        void updateOrderValue(double totalOrderValue);

        void displayTotalOutletCount(int totalOutlet);

        void updateCoveredCount(int coveredOutlet);

        void updateUnbilledCount(int unBilledOutlet);

        void sellerProductivity(int productivityPercent);

        void updateSellerInfoByDate(String selectedDate);
    }

    interface SellerMapHomePresenter {

        void setView(SellerMapHomeView supervisorHomeView, Context context);

        int getLoginUserId();

        void getSellerListAWS(String date);

        boolean isRealtimeLocation();

        void loginToFirebase(Context context,int userId);

        void sellerActivityInfoListener(int userId,String date);

        void realtimeLocationInfoListener(int userId,String date);

        void sellerAttendanceInfoListener(int userId,String date);

        void getSellerMarkerInfo(String userId);

        void getMarkerValuesToFocus();

        void computeSellerInfo();

        void animateSellerMarker(final LatLng destination, final Marker marker);

        void removeFirestoreListener();

        void downloadSupRetailerMaster(String selectedDate);
    }

}
