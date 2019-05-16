package com.ivy.core.data.retailer;

import com.ivy.sd.png.bo.IndicativeBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.RetailerMissedVisitBO;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.Single;

public interface RetailerDataManager {


    Observable<ArrayList<RetailerMasterBO>> fetchRetailers();

    Observable<Boolean> updateRouteConfig();

    Observable<ArrayList<IndicativeBO>> fetchIndicativeRetailers();

    Observable<ArrayList<RetailerMissedVisitBO>> fetchMissedRetailers();

    Single<Boolean> updatePlanAndVisitCount(RetailerMasterBO retailerMasterBO);

}
