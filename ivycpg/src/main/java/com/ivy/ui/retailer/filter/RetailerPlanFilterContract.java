package com.ivy.ui.filter;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;

import java.util.ArrayList;

public interface RetailerPlanFilterContract {

    interface RetailerPlanFilterView extends BaseIvyView{

        void showNotVisitedRow();

        void showTaskDueDateRow();

        void showLastVisitRow();

        void filterValidationSuccess();

        void filterValidationFailure(String error);

        void filteredRetailerIds(ArrayList<String> retailerIds);
    }

    interface RetailerPlanFilterPresenter<V extends RetailerPlanFilterView> extends BaseIvyPresenter<V>{
        void prepareConfiguration();
        boolean isConfigureAvail(String configuration);
        void validateFilterObject(RetailerPlanFilterBo planFilterBo);
        void getRetailerFilterArray(RetailerPlanFilterBo planFilterBo);
    }
}
