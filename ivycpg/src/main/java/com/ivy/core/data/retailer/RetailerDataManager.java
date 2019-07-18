package com.ivy.core.data.retailer;

import com.ivy.sd.png.bo.IndicativeBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.RetailerMissedVisitBO;
import com.ivy.ui.retailerplan.addplan.DateWisePlanBo;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;

public interface RetailerDataManager {


    Observable<ArrayList<RetailerMasterBO>> fetchRetailers();

    Observable<Boolean> updateRouteConfig();

    Observable<ArrayList<IndicativeBO>> fetchIndicativeRetailers();

    Observable<ArrayList<RetailerMissedVisitBO>> fetchMissedRetailers();

    Single<Boolean> updatePriceGroupId(boolean isRetailer);
    Single<DateWisePlanBo> updatePlanAndVisitCount(RetailerMasterBO retailerMasterBO,DateWisePlanBo planBo);

    Single<Boolean> updateSurveyScoreHistoryRetailerWise();
    Single<Boolean> updatePlanVisitCount(List<DateWisePlanBo> planList);

    Single<Boolean> updateIsToday();

}
