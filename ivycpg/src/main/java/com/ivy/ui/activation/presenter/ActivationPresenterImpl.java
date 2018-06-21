package com.ivy.ui.activation.presenter;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.ui.activation.data.ActivationDataManager;
import com.ivy.utils.rx.SchedulerProvider;
import com.ivy.ui.activation.ActivationContract;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

public class ActivationPresenterImpl<V extends ActivationContract.ActivationView> extends BasePresenter<V> implements ActivationContract.ActivationPresenter<V> {

    private ActivationDataManager activationDataManager;

    @Inject
    public ActivationPresenterImpl(DataManager dataManager,
                                   SchedulerProvider schedulerProvider,
                                   CompositeDisposable compositeDisposable, ActivationDataManager activationDataManager) {
        super(dataManager, schedulerProvider, compositeDisposable);
        this.activationDataManager = activationDataManager;
    }


    @Override
    public void validateActivationKey(String activationKey) {
        if (activationKey.length() <= 0) {
            getIvyView().showActivationEmptyError();
        } else if (activationKey.length() != 16) {
            getIvyView().showInvalidActivationError();
        }
    }

    @Override
    public void triggerIMEIActivation(String imei, String versionName, String versionNumber) {
        throw new UnsupportedOperationException("Unsupported operation");
    }

    @Override
    public void checkServerStatus(String url) {
        getCompositeDisposable().add(activationDataManager.isServerOnline(url)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean response) throws Exception {
                        if (response)
                            getIvyView().navigateToLoginScreen();
                        else
                            getIvyView().showInvalidUrlError();
                    }
                }));
    }


}
