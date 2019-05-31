package com.ivy.ui.retailer.filter;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.sd.png.bo.AttributeBO;

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.Single;

public interface RetailerPlanFilterContract {

    interface RetailerPlanFilterView extends BaseIvyView{

        void showNotVisitedRow();

        void showTaskDueDateRow();

        void showLastVisitRow();

        void showAttributeSpinner();

        void filterValidationSuccess();

        void clearFilter();

        void noFilterRecord();

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
