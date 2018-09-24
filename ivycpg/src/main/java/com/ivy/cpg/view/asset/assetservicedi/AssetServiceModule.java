package com.ivy.cpg.view.asset.assetservicedi;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.cpg.view.asset.AssetServiceContract;
import com.ivy.cpg.view.asset.AssetServicePresenterImpl;
import com.ivy.ui.activation.data.ActivationDataManager;
import com.ivy.ui.activation.data.ActivationDataManagerImpl;
import com.ivy.utils.rx.AppSchedulerProvider;
import com.ivy.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;
import io.reactivex.disposables.CompositeDisposable;

@Module
public class AssetServiceModule {

    private BaseIvyView mView;


    public AssetServiceModule(BaseIvyView assetServiceView) {
        this.mView = assetServiceView;

    }

    @Provides
    BaseIvyView provideView() {
        return mView;
    }

    @Provides
    ActivationDataManager providesActivationDataManager() {
        return new ActivationDataManagerImpl();
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
    BaseIvyPresenter<BaseIvyView>
    providesAssetServicePresenter(BasePresenter<BaseIvyView> presenter) {
        return presenter;
    }

}
