package com.ivy.cpg.view.serializedAsset;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.core.di.scope.PerActivity;

public interface SerializedAssetServiceContract {

    interface AssetServiceView extends BaseIvyView {

    }

    @PerActivity
    interface AssetServicePresenter<V extends SerializedAssetServiceContract.AssetServiceView> extends BaseIvyPresenter<V> {

    }
}
