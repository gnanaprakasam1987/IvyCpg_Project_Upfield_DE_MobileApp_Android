package com.ivy.ui.retailerplan.addplan;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.sd.png.bo.RetailerMasterBO;

import java.util.Date;
import java.util.List;

public interface AddPlanContract {

    interface AddPlanView extends BaseIvyView {

        void showError(String message);

        void updateDatePlan(DateWisePlanBo planBo);

        void updateNewRecursivePlanList(List<DateWisePlanBo> planList);

        void updateEditedRecursivePlanList(List<DateWisePlanBo> planList,DateWisePlanBo planBo,String reasonID);
    }

    interface AddPlanPresenter<V extends AddPlanView> extends BaseIvyPresenter<V> {

        void addNewPlan(String date, String startTime, String endTime, RetailerMasterBO retailerMasterBO, boolean isAdhoc);

        void updatePlan(String startTime, String endTime, DateWisePlanBo planBo, String reasonId);

        void cancelPlan(DateWisePlanBo dateWisePlanBo, RetailerMasterBO retailerMasterBO, String reasonId);

        void deletePlan(DateWisePlanBo dateWisePlanBo, RetailerMasterBO retailerMasterBO, String reasonId);

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

        String getWeekDay(String date);

        String getMonthWeekNo(String date);

        void loadPlanEndDate();

        void allowedToAddRecursivePlan(int mode, String date, String startTime, String endTime, RetailerMasterBO retailerMasterBO);

        void addRecursivePlans(List<DateWisePlanBo> planList,RetailerMasterBO retailerMasterBO);

        void updateRecursivePlan(String startTime, String endTime, DateWisePlanBo planBo, String reasonId, boolean allFuturePlan);

        void saveEditedRecursiveList(List<DateWisePlanBo> planList,DateWisePlanBo planBo,String reasonID,RetailerMasterBO retailerMasterBO);
    }
}
