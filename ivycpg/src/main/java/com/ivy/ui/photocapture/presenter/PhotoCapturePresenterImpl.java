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
import com.ivy.cpg.view.photocapture.PhotoCaptureProductBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.ui.photocapture.PhotoCaptureContract;
import com.ivy.ui.photocapture.data.PhotoCaptureDataManager;
import com.ivy.utils.rx.SchedulerProvider;

import java.util.ArrayList;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

public class PhotoCapturePresenterImpl<V extends PhotoCaptureContract.PhotoCaptureView> extends BasePresenter<V> implements PhotoCaptureContract.PhotoCapturePresenter<V> {

    private UserDataManager mUserDataManager;

    private OutletTimeStampDataManager mOutletTimeStampDataManager;

    private PhotoCaptureDataManager photoCaptureDataManager;

    private ConfigurationMasterHelper mConfigurationMasterHelper;


    @Inject
    public PhotoCapturePresenterImpl(DataManager dataManager, SchedulerProvider schedulerProvider, CompositeDisposable compositeDisposable,
                                     ConfigurationMasterHelper configurationMasterHelper, V view, @UserInfo UserDataManager userDataManager, @OutletTimeStampInfo OutletTimeStampDataManager outletTimeStampDataManager, PhotoCaptureDataManager photoCaptureDataManager) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, view);
        this.mOutletTimeStampDataManager = outletTimeStampDataManager;
        this.mUserDataManager = userDataManager;
        this.photoCaptureDataManager = photoCaptureDataManager;
        this.mConfigurationMasterHelper = configurationMasterHelper;
    }

    @Override
    public boolean isMaxPhotoLimitReached() {
        return false;
    }

    @Override
    public void fetchPhotoCaptureProducts() {
        getCompositeDisposable().add(photoCaptureDataManager.fetchPhotoCaptureProducts()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<ArrayList<PhotoCaptureProductBO>>() {
                    @Override
                    public void onNext(ArrayList<PhotoCaptureProductBO> photoCaptureProductBOS) {
                        if (photoCaptureProductBOS.size() != 0)
                            getIvyView().setProductListData(photoCaptureProductBOS);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                }));
    }

    @Override
    public boolean isGlobalLocation() {
        return mConfigurationMasterHelper.IS_GLOBAL_LOCATION;
    }
}
