package com.ivy.ui.retailer.viewretailers;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.ui.retailerplan.addplan.DateWisePlanBo;
import com.ivy.ui.retailer.filter.RetailerPlanFilterBo;

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

         void updateView();

         void drawRoutePath(String path);

         void focusMarker();

    }

     interface RetailerPresenter<V extends RetailerView> extends BaseIvyPresenter<V> {

         List<RetailerMasterBO> loadRetailerList();

         void fetchRetailerList();

         void fetchUnPlannedRetailerList();

         void fetchTodayPlannedRetailers();

         void setRetailerMasterBo(RetailerMasterBO retailerMasterBO);

         void fetchLinkRetailer();

         void fetchRoutePath(String url);

         void fetchAllDateRetailerPlan();

         void fetchSelectedDateRetailerPlan(String date, boolean isRetailerUpdate);

         HashMap<String, List<DateWisePlanBo>> getAllDateRetailerPlanList();

         ArrayList<DateWisePlanBo> getSelectedDateRetailerPlanList();

         DateWisePlanBo getSelectedRetailerPlan(String retailerId);

         void prepareFilteredRetailerList(RetailerPlanFilterBo planFilterBo, String filter, boolean isFromRetailerlist);

    }
}
