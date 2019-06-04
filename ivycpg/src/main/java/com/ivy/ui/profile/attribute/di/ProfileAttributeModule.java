package com.ivy.ui.profile.attribute.di;

import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.profile.attribute.IProfileAttributeContract;
import com.ivy.ui.profile.attribute.data.IProfileAttributeDataManager;
import com.ivy.ui.profile.attribute.data.ProfileAttributeDataManagerImpl;
import com.ivy.ui.profile.attribute.presenter.ProfileAttributePresenterImpl;
import com.ivy.utils.rx.AppSchedulerProvider;
import com.ivy.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;
import io.reactivex.disposables.CompositeDisposable;

@Module
public class ProfileAttributeModule {

    private IProfileAttributeContract.IProfileAttributeView attributeView;

    public ProfileAttributeModule(IProfileAttributeContract.IProfileAttributeView mView) {
        this.attributeView=mView;
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
    public IProfileAttributeContract.IProfileAttributeView attributeView(){
        return attributeView;
    }

    @Provides
    @PerActivity
    IProfileAttributeContract.IProfileAttributePresenter<IProfileAttributeContract.IProfileAttributeView> attributePresenter(ProfileAttributePresenterImpl<IProfileAttributeContract.IProfileAttributeView> attributePresenter) {
        return attributePresenter;
    }

    @Provides
    IProfileAttributeDataManager provideProfileDataManager(ProfileAttributeDataManagerImpl attributeDataManager){
        return attributeDataManager;
    }

}
