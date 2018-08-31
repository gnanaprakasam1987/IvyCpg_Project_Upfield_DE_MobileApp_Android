package com.ivy.ui.photocapture.di;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.utils.rx.AppSchedulerProvider;
import com.ivy.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;
import io.reactivex.disposables.CompositeDisposable;

@Module
public class PhotoGalleryModule {

    private BaseIvyView mView;

    public PhotoGalleryModule(BaseIvyView mView) {
        this.mView = mView;
    }

    @Provides
    public BaseIvyView provideView() {
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
    @PerActivity
    BaseIvyPresenter<BaseIvyView> providesPhotoCapturePresenter(BasePresenter<BaseIvyView> photoCapturePresenter) {
        return photoCapturePresenter;
    }

}
