package com.ivy.ui.retailerplanfilter.presenter;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.ui.retailerplanfilter.RetailerPlanFilterContract;
import com.ivy.ui.retailerplanfilter.data.RetailerPlanFilterDataManager;
import com.ivy.utils.rx.SchedulerProvider;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;

public class RetailerPlanFilterPresenterImpl<V extends RetailerPlanFilterContract.RetailerPlanFilterView>
        extends BasePresenter<V> implements RetailerPlanFilterContract.RetailerPlanFilterPresenter<V> {

    @Inject
    RetailerPlanFilterPresenterImpl(DataManager dataManager, SchedulerProvider schedulerProvider,
                                    CompositeDisposable compositeDisposable,
                                    ConfigurationMasterHelper configurationMasterHelper, V view
            ,RetailerPlanFilterDataManager retailerPlanFilterDataManager) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, view);
    }
}
