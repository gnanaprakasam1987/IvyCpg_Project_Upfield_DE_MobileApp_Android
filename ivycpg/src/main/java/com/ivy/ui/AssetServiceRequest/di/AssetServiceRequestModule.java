package com.ivy.ui.AssetServiceRequest.di;

import com.ivy.core.di.scope.PerActivity;
import com.ivy.cpg.view.asset.AssetServicePresenterImpl;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.ui.AssetServiceRequest.AssetServiceRequestContractor;
import com.ivy.ui.AssetServiceRequest.AssetServiceRequestPresenterImpl;
import com.ivy.ui.AssetServiceRequest.data.AssetServiceRequestDataManager;
import com.ivy.ui.AssetServiceRequest.data.AssetServiceRequestHelper;
import com.ivy.ui.notes.NotesContract;
import com.ivy.utils.rx.AppSchedulerProvider;
import com.ivy.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;
import io.reactivex.disposables.CompositeDisposable;

@Module
public class AssetServiceRequestModule {


    private AssetServiceRequestContractor.AssetServiceView assetServiceView;

    public AssetServiceRequestModule(AssetServiceRequestContractor.AssetServiceView assetServiceView) {
        this.assetServiceView = assetServiceView;
    }

    @PerActivity
    @Provides
    AssetServiceRequestContractor.AssetServiceView provideView() {
        return assetServiceView;
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
    AssetServiceRequestDataManager provideDataManager(AssetServiceRequestHelper dataManagerImpl) {
        return dataManagerImpl;
    }

    @Provides
    @PerActivity
    AssetServiceRequestContractor.Presenter<AssetServiceRequestContractor.AssetServiceView> providePresenter(AssetServiceRequestPresenterImpl<AssetServiceRequestContractor.AssetServiceView> presenter) {
        return presenter;
    }

}
