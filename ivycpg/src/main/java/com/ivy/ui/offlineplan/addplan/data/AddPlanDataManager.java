package com.ivy.ui.offlineplan.addplan.data;

import com.ivy.core.data.AppDataManagerContract;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.ui.offlineplan.addplan.DateWisePlanBo;

import io.reactivex.Single;

public interface AddPlanDataManager extends AppDataManagerContract {

    Single<Boolean> savePlan(DateWisePlanBo dateWisePlanBo);

    Single<Boolean> updatePlan(DateWisePlanBo dateWisePlanBo);

    Single<Boolean> cancelPlan(DateWisePlanBo dateWisePlanBo);

    Single<Boolean> DeletePlan(DateWisePlanBo dateWisePlanBo);
}
