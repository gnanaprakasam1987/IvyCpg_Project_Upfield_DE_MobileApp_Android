package com.ivy.cpg.view.supervisor.mvp.outletmapview;


import android.content.Context;

import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ivy.cpg.view.supervisor.helper.DetailsBo;
import com.ivy.cpg.view.supervisor.mvp.RetailerBo;

import java.util.ArrayList;

public interface OutletMapViewContractor {

    interface OutletMapView{

        void setRetailerMarker(RetailerBo retailerMarker,MarkerOptions markerOptions);

        void setOutletListAdapter(ArrayList<RetailerBo> retailerBosList);

        void focusMarker(LatLngBounds.Builder builder);

        void clearMap();

    }

    interface OutletMapPresenter {

        void setView(OutletMapView outletMapView,Context context);

        void downloadOutletListAws();

        void setOutletActivityDetail(int userId, String date);

        void getMarkerForFocus();

        void setTotalOutlet();

        void setCoveredOutlet();

        void setUnbilledOutlet();

        String convertMillisToTime(Long time);

        String calculateDuration(long startTime,long endTime);
    }
}
