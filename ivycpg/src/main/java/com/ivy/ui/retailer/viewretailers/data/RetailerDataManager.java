package com.ivy.ui.retailer.viewretailers.data;

import com.ivy.calendarlibrary.weekview.WeekViewEvent;
import com.ivy.core.data.AppDataManagerContract;
import com.ivy.ui.retailerplan.addplan.DateWisePlanBo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;

public interface RetailerDataManager extends AppDataManagerContract {

    Single<String> getRoutePath(String url);

    Observable<HashMap<String, List<DateWisePlanBo>>> getAllDateRetailerPlanList();

    Observable<HashMap<String,DateWisePlanBo>> getRetailerPlanList(String date);
}
