package com.ivy.ui.gallery.di;

import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.gallery.GalleryContract;
import com.ivy.ui.gallery.data.GalleryDataManager;
import com.ivy.ui.gallery.data.GalleryDataManagerImpl;
import com.ivy.ui.gallery.presenter.GalleryPresenterImpl;
import com.ivy.utils.rx.AppSchedulerProvider;
import com.ivy.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;
import io.reactivex.disposables.CompositeDisposable;

@Module
public class GalleryModule {

    private GalleryContract.GalleryView galleryView;

    public GalleryModule(GalleryContract.GalleryView galleryView) {
        this.galleryView = galleryView;
    }

    @Provides
    GalleryContract.GalleryView provideGalleryView() {
        return galleryView;
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
    GalleryContract.GalleryPresenter<GalleryContract.GalleryView> provideGalleryPresenter(GalleryPresenterImpl<GalleryContract.GalleryView> galleryPresenter) {
        return galleryPresenter;
    }

    @Provides
    GalleryDataManager provideGalleryDataManager(GalleryDataManagerImpl galleryDataManagerImpl) {
        return galleryDataManagerImpl;
    }
}
