package com.ivy.ui.profile.create.di;

import com.ivy.core.data.beat.BeatDataManager;
import com.ivy.core.data.beat.BeatDataManagerImpl;
import com.ivy.core.data.sync.SynchronizationDataManager;
import com.ivy.core.data.sync.SynchronizationDataManagerImpl;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.profile.create.INewRetailerContract;
import com.ivy.ui.profile.create.presenter.NewOutletPresenterImpl;
import com.ivy.ui.profile.data.ProfileDataManager;
import com.ivy.ui.profile.data.ProfileDataManagerImpl;
import com.ivy.ui.survey.data.SurveyDataManager;
import com.ivy.ui.survey.data.SurveyDataManagerImpl;
import com.ivy.utils.rx.AppSchedulerProvider;
import com.ivy.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;
import io.reactivex.disposables.CompositeDisposable;

@Module
public class NewRetailerCreationModule {

    private INewRetailerContract.INewRetailerView view;

    public NewRetailerCreationModule(INewRetailerContract.INewRetailerView view) {
        this.view = view;
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
    INewRetailerContract.INewRetailerView provideRetailerView() {
        return view;
    }

    @Provides
    @PerActivity
    INewRetailerContract.INewRetailerPresenter<INewRetailerContract.INewRetailerView> provideNewRetailerPresenter(NewOutletPresenterImpl<INewRetailerContract.INewRetailerView> newRetailerPresenter) {
        return newRetailerPresenter;
    }

    @Provides
    ProfileDataManager provideProfileDataManager(ProfileDataManagerImpl profileDataManager) {
        return profileDataManager;
    }

    @Provides
    BeatDataManager provideBeatDataManager(BeatDataManagerImpl beatDataManager){
        return beatDataManager;
    }

    @Provides
    SurveyDataManager providesSurveyDataManager(SurveyDataManagerImpl surveyDataManager){
        return surveyDataManager;
    }

    @Provides
    SynchronizationDataManager providesSynchronizationDataManager(SynchronizationDataManagerImpl synchronizationDataManager){
        return synchronizationDataManager;
    }


}
