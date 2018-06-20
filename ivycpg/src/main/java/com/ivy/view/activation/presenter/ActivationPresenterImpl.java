package com.ivy.view.activation.presenter;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.utils.rx.SchedulerProvider;
import com.ivy.view.activation.ActivationContract;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;

public class ActivationPresenterImpl<V extends ActivationContract.ActivationView> extends BasePresenter<V> implements ActivationContract.ActivationPresenter<V> {

    @Inject
    public ActivationPresenterImpl(DataManager dataManager,
                                   SchedulerProvider schedulerProvider,
                                   CompositeDisposable compositeDisposable) {
        super(dataManager, schedulerProvider, compositeDisposable);
    }


}
