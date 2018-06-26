package com.ivy.cpg.view.supervisor.mvp.outletmapview;


import android.content.Context;

import com.ivy.cpg.view.supervisor.helper.DetailsBo;

public interface OutletMapViewContractor {

    interface OutletMapView{
        void displayOutlet();
        void displayOutletMarkerInfo();
    }

    interface OutletMapViewPresenter{
        void getOutletList(Context context,int outletType);
        void getOutletMarkerInfo(DetailsBo detailsBo);
    }
}
