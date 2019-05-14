package com.ivy.ui.retailerplan.addplan.data;

import com.ivy.core.data.AppDataManagerContract;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.ui.retailerplan.addplan.DateWisePlanBo;

import io.reactivex.Observable;
import io.reactivex.Single;

public interface AddPlanDataManager extends AppDataManagerContract {

    Observable<DateWisePlanBo> savePlan(DateWisePlanBo dateWisePlanBo);

    Observable<DateWisePlanBo> updatePlan(DateWisePlanBo dateWisePlanBo);

    Observable<DateWisePlanBo> cancelPlan(DateWisePlanBo dateWisePlanBo);

    Observable<DateWisePlanBo> DeletePlan(DateWisePlanBo dateWisePlanBo);
}
