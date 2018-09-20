package com.ivy.cpg.view.asset.assetservicedi;

import com.ivy.core.di.component.IvyAppComponent;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.cpg.view.asset.AssetServiceActivity;

import dagger.Component;

@PerActivity
@Component(dependencies = IvyAppComponent.class, modules = {AssetServiceModule.class})
public interface AssetServiceComponent {
    void inject(AssetServiceActivity assetServiceActivity);
}
