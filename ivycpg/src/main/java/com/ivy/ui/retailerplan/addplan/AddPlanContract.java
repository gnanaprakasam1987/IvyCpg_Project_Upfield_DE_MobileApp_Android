package com.ivy.ui.retailerplan.addplan;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.sd.png.bo.RetailerMasterBO;

public interface AddPlanContract {

    interface AddPlanView extends BaseIvyView{

        void showUpdatedSuccessfullyMessage();

        void showUpdateFailureMessage();
    }

    interface AddPlanPresenter<V extends AddPlanView> extends BaseIvyPresenter<V>{
        void addNewPlan(String date, String startTime, String endTime,RetailerMasterBO retailerMasterBO);
        void updatePlan(String date, String startTime, String endTime,RetailerMasterBO retailerMasterBO);
        void cancelPlan(DateWisePlanBo dateWisePlanBo);
        void deletePlan(DateWisePlanBo dateWisePlanBo);
    }
}
