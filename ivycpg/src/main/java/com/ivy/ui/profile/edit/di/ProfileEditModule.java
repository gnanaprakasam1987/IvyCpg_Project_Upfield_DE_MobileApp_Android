package com.ivy.ui.profile.edit.di;


import com.ivy.core.data.sync.SynchronizationDataManager;
import com.ivy.core.data.sync.SynchronizationDataManagerImpl;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.profile.data.ProfileDataManager;
import com.ivy.ui.profile.attribute.data.IProfileAttributeDataManager;
import com.ivy.ui.profile.attribute.data.ProfileAttributeDataManagerImpl;
import com.ivy.ui.profile.attribute.presenter.ProfileAttributePresenterImpl;
import com.ivy.ui.profile.data.IProfileDataManager;
import com.ivy.ui.profile.data.ProfileDataManagerImpl;
import com.ivy.ui.profile.edit.IProfileEditContract;
import com.ivy.ui.profile.edit.presenter.ProfileEditPresenterImp;
import com.ivy.utils.rx.AppSchedulerProvider;
import com.ivy.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;
import io.reactivex.disposables.CompositeDisposable;

@Module
public class ProfileEditModule {

    private IProfileEditContract.ProfileEditView mView;

    public ProfileEditModule(IProfileEditContract.ProfileEditView mView) {
        this.mView=mView;
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
    public IProfileEditContract.ProfileEditView profileEditView(){
        return mView;
    }

    @Provides
    @PerActivity
    IProfileEditContract.ProfileEditPresenter<IProfileEditContract.ProfileEditView> providesProfileEditPresenter(ProfileEditPresenterImp<IProfileEditContract.ProfileEditView> profileEditPresenter) {
        return profileEditPresenter;
    }

    @Provides
    ProfileDataManager provideProfileDataManager(ProfileDataManagerImpl profileDataManager){
        return profileDataManager;
    }

    @Provides
    SynchronizationDataManager providesSynchronizationDataManager(SynchronizationDataManagerImpl synchronizationDataManager){
        return synchronizationDataManager;
    }

    @Provides
    IProfileAttributeDataManager profileAttributeDataManager(ProfileAttributeDataManagerImpl profileAttributeDataManager){
        return profileAttributeDataManager;
    }

}
