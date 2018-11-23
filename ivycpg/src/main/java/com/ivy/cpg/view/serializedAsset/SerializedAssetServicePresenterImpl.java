package com.ivy.cpg.view.serializedAsset;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.cpg.view.asset.AssetServiceContract;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.utils.rx.SchedulerProvider;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;

public class SerializedAssetServicePresenterImpl<V extends AssetServiceContract.AssetServiceView> extends BasePresenter<V> implements
        AssetServiceContract.AssetServicePresenter<V> {

    @Inject
    public SerializedAssetServicePresenterImpl(DataManager dataManager, SchedulerProvider schedulerProvider, CompositeDisposable compositeDisposable, ConfigurationMasterHelper configurationMasterHelper, V view) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, view);
    }
}
