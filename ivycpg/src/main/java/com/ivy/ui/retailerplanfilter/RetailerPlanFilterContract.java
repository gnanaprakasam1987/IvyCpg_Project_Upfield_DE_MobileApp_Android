package com.ivy.ui.retailerplanfilter;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;

public interface RetailerPlanFilterContract {

    interface RetailerPlanFilterView extends BaseIvyView{

        void showNotVisitedRow();

        void showTaskDueDateRow();

        void showLastVisitRow();

        void filterValidationSuccess(RetailerPlanFilterBo planFilterBo);

        void filterValidationFailure();
    }

    interface RetailerPlanFilterPresenter<V extends RetailerPlanFilterView> extends BaseIvyPresenter<V>{
        void prepareConfiguration();
        void validateFilterObject(RetailerPlanFilterBo planFilterBo);
    }
}
