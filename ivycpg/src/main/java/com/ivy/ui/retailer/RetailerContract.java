package com.ivy.ui.retailer;

import android.content.Context;

import com.google.android.gms.maps.model.MarkerOptions;
import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.sd.png.bo.RetailerMasterBO;

import java.util.List;

public interface RetailerContract {

     interface RetailerView extends BaseIvyView{

         void populateRetailers(List<RetailerMasterBO> retailerList);

         void populateTodayPlannedRetailers(List<RetailerMasterBO> retailerList);

         void populatePlannedRetailers(List<RetailerMasterBO> plannedRetailers);

         void populateUnPlannedRetailers(List<RetailerMasterBO> unPlannedRetailers);

         void populateCompletedRetailers(List<RetailerMasterBO> unPlannedRetailers);

         void drawRoutePath(String path);

    }

     interface RetailerPresenter<V extends RetailerView> extends BaseIvyPresenter<V> {

         void fetchRetailerList();

         void fetchTodayPlannedRetailers();


         void setRetailerMasterBo(RetailerMasterBO retailerMasterBO);

         void fetchLinkRetailer();

         void fetchRoutePath(String url);

    }
}
