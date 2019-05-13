package com.ivy.ui.retailer.data;

import com.ivy.calendarlibrary.weekview.WeekViewEvent;
import com.ivy.core.data.AppDataManagerContract;
import com.ivy.cpg.view.offlineplanning.OfflineDateWisePlanBO;
import com.ivy.ui.offlineplan.addplan.DateWisePlanBo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;

public interface RetailerDataManager extends AppDataManagerContract {

    Single<String> getRoutePath(String url);

    Observable<HashMap<String, List<DateWisePlanBo>>> getAllDateRetailerPlanList();

    Single<HashMap<String,DateWisePlanBo>> getRetailerPlanList(String date);
}
