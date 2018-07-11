package com.ivy.cpg.view.supervisor.mvp.supervisorhomepage;

import android.content.Context;

import com.google.android.gms.maps.model.LatLngBounds;
import com.ivy.cpg.view.supervisor.helper.DetailsBo;
import com.ivy.cpg.view.supervisor.mvp.SupervisorModelBo;

import java.util.ArrayList;

public interface SupervisorHomeContract {

    interface SupervisorHomeView{
        void firebaseLoginFailure();
        void updateSellerCount();
        void updateSellerMarkerInfo(SupervisorModelBo supervisorModelBo);
        void updateSellerFirebaseInfo(SupervisorModelBo supervisorModelBo);
        void createMarker(SupervisorModelBo supervisorModelBo);
        void updateMaker(SupervisorModelBo supervisorModelBo);
        void focusMarker(LatLngBounds.Builder builder);
        void setSellerListAdapter(ArrayList<SupervisorModelBo> modelBoArrayList);
        void updateSellerAttendance(int totalSellerCount,int absentSellerCount,int marketSellerCount);
        void updateOrderValue(int totalOrderValue);

    }

    interface SupervisorHomePresenter{
        void loginToFirebase(Context context);
        void getSellerMarkerInfo(String userId);
        void getSellerCount(Context context);
        void getSellerActivityInfoListener();
        void getMarkerForFocus();
        void setView(SupervisorHomeView supervisorHomeView,Context context);
        void computeSellerInfo();
        void removeFirestoreListener();
        void realtimeLocationInfoListener();
        void getSellerAttendanceInfoListener();
        void getSellerListAWS();
    }

}
