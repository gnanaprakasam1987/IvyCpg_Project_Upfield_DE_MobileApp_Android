package com.ivy.ui.photocapture.presenter;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.core.di.scope.RetailerInfo;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.ui.photocapture.PhotoCaptureContract;
import com.ivy.utils.rx.SchedulerProvider;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;

public class PhotoCapturePresenterImpl<V extends PhotoCaptureContract.PhotoCaptureView> extends BasePresenter<V> implements PhotoCaptureContract.PhotoCapturePresenter<V> {

    @Inject
    public PhotoCapturePresenterImpl(DataManager dataManager, SchedulerProvider schedulerProvider, CompositeDisposable compositeDisposable,
                                     ConfigurationMasterHelper configurationMasterHelper, V view) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, view);
    }
}
