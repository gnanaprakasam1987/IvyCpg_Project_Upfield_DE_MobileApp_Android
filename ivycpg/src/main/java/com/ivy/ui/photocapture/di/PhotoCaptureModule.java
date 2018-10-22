package com.ivy.ui.photocapture.di;

import com.ivy.core.data.outlettime.OutletTimeStampDataManager;
import com.ivy.core.data.outlettime.OutletTimeStampDataManagerImpl;
import com.ivy.core.data.user.UserDataManager;
import com.ivy.core.data.user.UserDataManagerImpl;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.ui.photocapture.PhotoCaptureContract;
import com.ivy.ui.photocapture.data.PhotoCaptureDataManager;
import com.ivy.ui.photocapture.data.PhotoCaptureDataManagerImpl;
import com.ivy.ui.photocapture.presenter.PhotoCapturePresenterImpl;
import com.ivy.utils.rx.AppSchedulerProvider;
import com.ivy.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;
import io.reactivex.disposables.CompositeDisposable;

@Module
public class PhotoCaptureModule {

    private PhotoCaptureContract.PhotoCaptureView mView;

    public PhotoCaptureModule(PhotoCaptureContract.PhotoCaptureView mView) {
        this.mView = mView;
    }

    @Provides
    public PhotoCaptureContract.PhotoCaptureView provideView() {
        return mView;
    }

    @Provides
    CompositeDisposable provideCompositeDisposable() {
        return new CompositeDisposable();
    }

    @Provides
    SchedulerProvider provideSchedulerProvider() {
        return new AppSchedulerProvider();
    }

    @Provides
    PhotoCaptureDataManager providesPhotoCaptureDataManager(PhotoCaptureDataManagerImpl photoCaptureDataManager){
        return photoCaptureDataManager;
    }

    @Provides
    @PerActivity
    PhotoCaptureContract.PhotoCapturePresenter<PhotoCaptureContract.PhotoCaptureView> providesPhotoCapturePresenter(PhotoCapturePresenterImpl<PhotoCaptureContract.PhotoCaptureView> photoCapturePresenter) {
        return photoCapturePresenter;
    }

}
