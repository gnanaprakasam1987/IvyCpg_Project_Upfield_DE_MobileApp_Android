package com.ivy.ui.retailerplan.addplan.data;

import com.ivy.core.data.AppDataManagerContract;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.ui.retailerplan.addplan.DateWisePlanBo;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;

public interface AddPlanDataManager extends AppDataManagerContract {

    Observable<DateWisePlanBo> savePlan(DateWisePlanBo dateWisePlanBo);

    Observable<List<DateWisePlanBo>> savePlan(List<DateWisePlanBo> dateWisePlanBo);

    Observable<DateWisePlanBo> updatePlan(DateWisePlanBo dateWisePlanBo,String reasonId,long planId);

    Observable<List<DateWisePlanBo>> updatePlan(List<DateWisePlanBo> dateWisePlanBo, DateWisePlanBo planBo,String reasonId);

    Observable<DateWisePlanBo> cancelPlan(DateWisePlanBo dateWisePlanBo, String reasonId);

    Observable<DateWisePlanBo> deletePlan(DateWisePlanBo dateWisePlanBo, String reasonId);

    Single<Boolean> copyPlan(List<DateWisePlanBo> planList, String toDate, int userId);

    Single<List<DateWisePlanBo>> copyPlan(List<DateWisePlanBo> planList, int userId);

    Single<Boolean> cancelPlan(List<DateWisePlanBo> planList,String reasonId);

    Single<Boolean> movePlan(List<DateWisePlanBo> planList, String toDate, String reasonId,int userId);

    Single<List<DateWisePlanBo>> movePlan(List<DateWisePlanBo> fromPlanList,List<DateWisePlanBo> toplanList, String reasonId,int userId);
}
