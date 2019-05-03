package com.ivy.ui.retailer;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.ui.offlineplan.addplan.DateWisePlanBo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface RetailerContract {

     interface RetailerView extends BaseIvyView{

         void populateRetailers(List<RetailerMasterBO> retailerList);

         void populateTodayPlannedRetailers(RetailerMasterBO retailerList);

         void populatePlannedRetailers(List<RetailerMasterBO> plannedRetailers);

         void populateUnPlannedRetailers(List<RetailerMasterBO> unPlannedRetailers);

         void populateCompletedRetailers(List<RetailerMasterBO> unPlannedRetailers);

         void drawRoutePath(String path);

         void focusMarker();

    }

     interface RetailerPresenter<V extends RetailerView> extends BaseIvyPresenter<V> {

         List<RetailerMasterBO> loadRetailerList();

         void fetchRetailerList();

         void fetchTodayPlannedRetailers();

         void setRetailerMasterBo(RetailerMasterBO retailerMasterBO);

         void fetchLinkRetailer();

         void fetchRoutePath(String url);

         void fetchAllDateRetailerPlan();

         void fetchSelectedDateRetailerPlan(String date);

         HashMap<String, ArrayList<DateWisePlanBo>> getAllDateRetailerPlanList();

         ArrayList<DateWisePlanBo> getSelectedDateRetailerPlanList();

         DateWisePlanBo getSelectedRetailerPlan(String retailerId);

         void prepareFilteredRetailerList(ArrayList<String> retailerIds);

    }
}
