package com.ivy.ui.retailer;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.sd.png.bo.RetailerMasterBO;

import java.util.List;

public interface RetailerContract {

     interface RetailerView extends BaseIvyView{

         void populateRetailers(List<RetailerMasterBO> retailerList);

         void populateRetailersMarker(List<MarkerOptions> retailerList);

         void populatePlannedRetailers(List<RetailerMasterBO> plannedRetailers);

         void populateUnPlannedRetailers(List<RetailerMasterBO> unPlannedRetailers);

         void populateCompletedRetailers(List<RetailerMasterBO> unPlannedRetailers);

    }

     interface RetailerPresenter<V extends RetailerView> extends BaseIvyPresenter<V> {

         void fetchRetailerList();

         void prepareRetailerMarker(GoogleMap map, List<RetailerMasterBO> retailerList);

         void addRetailerToPlan(RetailerMasterBO retailerMasterBO, String startDate, String endDate);

         void updateRetailerToPlan(RetailerMasterBO retailerMasterBO, String startDate, String endDate);

         void deleteRetailerFromPlan(RetailerMasterBO retailerMasterBO);

         int getPixelsFromDpInt(Context context);

    }
}
