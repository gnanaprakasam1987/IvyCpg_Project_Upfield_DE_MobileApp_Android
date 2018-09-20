package com.ivy.cpg.view.asset;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.core.di.scope.PerActivity;

public interface AssetServiceContract {

    interface AssetServiceView extends BaseIvyView {

    }

    @PerActivity
    interface AssetServicePresenter<V extends AssetServiceContract.AssetServiceView> extends BaseIvyPresenter<V> {

    }
}
