package com.ivy.ui.profile.di;


import com.ivy.core.data.sync.SynchronizationDataManager;
import com.ivy.core.data.sync.SynchronizationDataManagerImpl;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.profile.IProfileContractor;
import com.ivy.ui.profile.presenter.ProfilePresenterImpl;
import com.ivy.ui.profile.data.ProfileDataManager;
import com.ivy.ui.profile.data.ProfileDataManagerImpl;
import com.ivy.utils.rx.AppSchedulerProvider;
import com.ivy.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;
import io.reactivex.disposables.CompositeDisposable;

@Module
public class ProfileModule {

    private IProfileContractor.IProfileView profileView;

    public ProfileModule(IProfileContractor.IProfileView mView) {
        this.profileView = mView;
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
    public IProfileContractor.IProfileView profileView(){
        return profileView;
    }

    @Provides
    @PerActivity
    IProfileContractor.IProfilePresenter<IProfileContractor.IProfileView> profilePresenter(ProfilePresenterImpl<IProfileContractor.IProfileView> profilePresenter) {
        return profilePresenter;
    }

    @Provides
    ProfileDataManager profileDataManager(ProfileDataManagerImpl profileDataManager){
        return profileDataManager;
    }

    @Provides
    SynchronizationDataManager providesSynchronizationDataManager(SynchronizationDataManagerImpl synchronizationDataManager){
        return synchronizationDataManager;
    }

}
