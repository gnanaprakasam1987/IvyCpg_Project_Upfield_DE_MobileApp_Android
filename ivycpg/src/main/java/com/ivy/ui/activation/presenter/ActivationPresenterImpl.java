package com.ivy.ui.activation.presenter;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.utils.rx.SchedulerProvider;
import com.ivy.ui.activation.ActivationContract;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;

public class ActivationPresenterImpl<V extends ActivationContract.ActivationView> extends BasePresenter<V> implements ActivationContract.ActivationPresenter<V> {

    @Inject
    public ActivationPresenterImpl(DataManager dataManager,
                                   SchedulerProvider schedulerProvider,
                                   CompositeDisposable compositeDisposable) {
        super(dataManager, schedulerProvider, compositeDisposable);
    }


    @Override
    public void validateActivationKey(String activationKey) {
        if(activationKey.length()<=0){
            getIvyView().showActivationEmptyError();
        }else if(activationKey.length()!=16){
            getIvyView().showInvalidActivationError();
        }
    }

    @Override
    public void triggerIMEIActivation(String imei) {

    }


}
