package com.ivy.ui.profile;


import com.ivy.core.di.scope.PerActivity;
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

}
