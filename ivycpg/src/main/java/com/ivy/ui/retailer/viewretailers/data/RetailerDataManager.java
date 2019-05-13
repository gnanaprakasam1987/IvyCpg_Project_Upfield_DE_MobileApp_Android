package com.ivy.ui.retailer.viewretailers.data;

import com.ivy.core.data.AppDataManagerContract;
import com.ivy.ui.retailerplan.addplan.DateWisePlanBo;

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.Single;

public interface RetailerDataManager extends AppDataManagerContract {

    Single<String> getRoutePath(String url);

    Single<HashMap<String, ArrayList<DateWisePlanBo>>> getAllDateRetailerPlanList();

    Single<HashMap<String,DateWisePlanBo>> getRetailerPlanList(String date);
}
