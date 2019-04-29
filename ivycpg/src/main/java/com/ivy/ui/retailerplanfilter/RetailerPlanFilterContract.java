package com.ivy.ui.retailerplanfilter;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;

public interface RetailerPlanFilterContract {

    interface RetailerPlanFilterView extends BaseIvyView{

    }

    interface RetailerPlanFilterPresenter<V extends RetailerPlanFilterView> extends BaseIvyPresenter<V>{

    }
}
