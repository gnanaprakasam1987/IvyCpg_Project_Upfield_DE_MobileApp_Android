package com.ivy.ui.retailerplan.addplan;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.sd.png.bo.RetailerMasterBO;

public interface AddPlanContract {

    interface AddPlanView extends BaseIvyView{

        void showUpdatedSuccessfullyMessage();

        void showUpdateFailureMessage();

        void updateDatePlan(DateWisePlanBo planBo);

    }

    interface AddPlanPresenter<V extends AddPlanView> extends BaseIvyPresenter<V>{
        void addNewPlan(String date, String startTime, String endTime,RetailerMasterBO retailerMasterBO, boolean isAdhoc);
        void updatePlan(String startTime, String endTime,DateWisePlanBo planBo,String reasonId);
        void cancelPlan(DateWisePlanBo dateWisePlanBo,RetailerMasterBO retailerMasterBO,String reasonId);
        void deletePlan(DateWisePlanBo dateWisePlanBo,RetailerMasterBO retailerMasterBO, String reasonId);
        boolean showRescheduleToday();
        boolean showRescheduleReasonToday();
        boolean showRescheduleFuture();
        boolean showRescheduleReasonFuture();
        boolean showDeleteToday();
        boolean showDeleteReasonToday();
        boolean showDeleteFuture();
        boolean showDeleteReasonFuture();
        boolean showCancelToday();
        boolean showCancelFuture();

    }
}
