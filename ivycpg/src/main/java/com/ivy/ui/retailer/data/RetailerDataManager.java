package com.ivy.ui.retailer.data;

import com.ivy.core.data.AppDataManagerContract;
import com.ivy.cpg.view.offlineplanning.OfflineDateWisePlanBO;
import com.ivy.ui.offlineplan.addplan.DateWisePlanBo;

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.Single;

public interface RetailerDataManager extends AppDataManagerContract {

    Single<String> getRoutePath(String url);

    Single<HashMap<String, ArrayList<DateWisePlanBo>>> getAllDateRetailerPlanList();

    Single<HashMap<String,DateWisePlanBo>> getRetailerPlanList(String date);
}
