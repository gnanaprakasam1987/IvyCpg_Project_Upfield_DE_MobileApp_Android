package com.ivy.ui.announcement.di;

import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.announcement.AnnouncementContract;
import com.ivy.ui.announcement.data.AnnouncementDataManager;
import com.ivy.ui.announcement.data.AnnouncementDataManagerImpl;
import com.ivy.ui.announcement.presenter.AnnouncementPresenterImpl;
import com.ivy.utils.rx.AppSchedulerProvider;
import com.ivy.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;
import io.reactivex.disposables.CompositeDisposable;

@Module
public class AnnouncementModel {

    private AnnouncementContract.AnnouncementView announcementView;

    public AnnouncementModel(AnnouncementContract.AnnouncementView announcementView) {
        this.announcementView = announcementView;
    }

    @Provides
    AnnouncementContract.AnnouncementView provideView() {
        return announcementView;
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
    AnnouncementDataManager announcementDataManager(AnnouncementDataManagerImpl announcementDataManagerImpl) {
        return announcementDataManagerImpl;
    }

    @Provides
    @PerActivity
    AnnouncementContract.AnnouncementPresenter<AnnouncementContract.AnnouncementView> provideNotePresenter(AnnouncementPresenterImpl<AnnouncementContract.AnnouncementView> announcementPresenter) {
        return announcementPresenter;
    }
}
