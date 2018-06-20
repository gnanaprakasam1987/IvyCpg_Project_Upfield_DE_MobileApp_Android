package com.ivy.ui.activation;


import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.utils.rx.SchedulerProvider;

import io.reactivex.disposables.CompositeDisposable;

public class IActivationPresenterImpl extends BasePresenter {

    public IActivationPresenterImpl(DataManager dataManager,
                                    SchedulerProvider schedulerProvider,
                                    CompositeDisposable compositeDisposable) {
        super(dataManager,schedulerProvider,compositeDisposable);
    }
}
