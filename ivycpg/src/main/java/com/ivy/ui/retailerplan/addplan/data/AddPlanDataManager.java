package com.ivy.ui.retailerplan.addplan.data;

import com.ivy.core.data.AppDataManagerContract;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.ui.retailerplan.addplan.DateWisePlanBo;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;

public interface AddPlanDataManager extends AppDataManagerContract {

    Observable<DateWisePlanBo> savePlan(DateWisePlanBo dateWisePlanBo);

    Observable<DateWisePlanBo> updatePlan(DateWisePlanBo dateWisePlanBo,String reasonId,long planId);

    Observable<DateWisePlanBo> cancelPlan(DateWisePlanBo dateWisePlanBo, String reasonId);

    Observable<DateWisePlanBo> DeletePlan(DateWisePlanBo dateWisePlanBo,String reasonId);

    Single<Boolean> deletePlan(List<DateWisePlanBo> planList);

    Single<Boolean> copyPlan(List<DateWisePlanBo> planList, String toDate);

    Single<Boolean> copyPlan(List<DateWisePlanBo> planList);
}
