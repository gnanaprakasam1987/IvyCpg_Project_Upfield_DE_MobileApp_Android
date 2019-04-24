package com.ivy.ui.offlineplan;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;

public interface OfflinePlanBaseContract {

    interface OfflinePlanBaseView  extends BaseIvyView{

    }

    interface OfflinePlanBasePresenter<V extends OfflinePlanBaseView> extends BaseIvyPresenter<V>{
        void loadRetailersData();
        void loadAllStoresData();
        void loadTodayVisitData();
    }
}
