package com.ivy.ui.offlineplan.addplan.data;

import com.ivy.core.data.AppDataManagerContract;
import com.ivy.sd.png.bo.RetailerMasterBO;

public interface AddPlanDataManager extends AppDataManagerContract {

    void savePlan(RetailerMasterBO retailerMasterBO);

    void updatePlan(RetailerMasterBO retailerMasterBO);

    void cancelPlan(RetailerMasterBO retailerMasterBO);

    void DeletePlan(RetailerMasterBO retailerMasterBO);
}
