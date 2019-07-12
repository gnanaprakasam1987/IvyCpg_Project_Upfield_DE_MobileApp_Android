package com.ivy.ui.profile;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.utils.rx.SchedulerProvider;

import io.reactivex.disposables.CompositeDisposable;

public class ProfilePresenterImpl <V extends IProfileContractor.IProfileView>
        extends BasePresenter<V> implements IProfileContractor.IProfilePresenter<V>{


    public ProfilePresenterImpl(DataManager dataManager, SchedulerProvider schedulerProvider, CompositeDisposable compositeDisposable, ConfigurationMasterHelper configurationMasterHelper, V view) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, view);
    }
}
