package com.ivy.ui.retailerplan.addplan.data;

import com.ivy.core.data.AppDataManagerContract;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.ui.retailerplan.addplan.DateWisePlanBo;

import io.reactivex.Single;

public interface AddPlanDataManager extends AppDataManagerContract {

    Single<DateWisePlanBo> savePlan(DateWisePlanBo dateWisePlanBo);

    Single<DateWisePlanBo> updatePlan(DateWisePlanBo dateWisePlanBo);

    Single<DateWisePlanBo> cancelPlan(DateWisePlanBo dateWisePlanBo);

    Single<DateWisePlanBo> DeletePlan(DateWisePlanBo dateWisePlanBo);
}
