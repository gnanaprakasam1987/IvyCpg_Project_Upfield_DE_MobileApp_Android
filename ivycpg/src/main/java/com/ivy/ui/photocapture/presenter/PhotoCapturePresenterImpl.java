package com.ivy.ui.photocapture.presenter;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.core.data.outlettime.OutletTimeStampDataManager;
import com.ivy.core.data.user.UserDataManager;
import com.ivy.core.data.user.UserDataManagerImpl;
import com.ivy.core.di.scope.OutletTimeStampInfo;
import com.ivy.core.di.scope.RetailerInfo;
import com.ivy.core.di.scope.UserInfo;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.ui.photocapture.PhotoCaptureContract;
import com.ivy.utils.rx.SchedulerProvider;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;

public class PhotoCapturePresenterImpl<V extends PhotoCaptureContract.PhotoCaptureView> extends BasePresenter<V> implements PhotoCaptureContract.PhotoCapturePresenter<V> {

    private UserDataManager mUserDataManager;

    private OutletTimeStampDataManager mOutletTimeStampDataManager;


    @Inject
    public PhotoCapturePresenterImpl(DataManager dataManager, SchedulerProvider schedulerProvider, CompositeDisposable compositeDisposable,
                                     ConfigurationMasterHelper configurationMasterHelper, V view, @UserInfo UserDataManager userDataManager, @OutletTimeStampInfo OutletTimeStampDataManager outletTimeStampDataManager) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, view);
        this.mOutletTimeStampDataManager = outletTimeStampDataManager;
        this.mUserDataManager = userDataManager;
    }

    @Override
    public boolean isMaxPhotoLimitReached() {
        return false;
    }
}
