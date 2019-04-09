package com.ivy.ui.offlineplan;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.sd.png.bo.RetailerMasterBO;

import java.util.ArrayList;

public interface OfflinePlanBaseContract {

    interface OfflinePlanBaseView  extends BaseIvyView{
        void showAllStores(ArrayList<RetailerMasterBO> retailersBo);
        void showTodayVisit(ArrayList<RetailerMasterBO> retailersBo);
    }

    interface OfflinePlanBasePresenter<V extends OfflinePlanBaseView> extends BaseIvyPresenter<V>{
        void loadAllStoresData();
        void loadTodayVisitData();
    }
}
