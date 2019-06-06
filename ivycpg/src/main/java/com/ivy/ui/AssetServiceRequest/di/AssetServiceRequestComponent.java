package com.ivy.ui.AssetServiceRequest.di;

import com.ivy.core.di.component.IvyAppComponent;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.AssetServiceRequest.AssetServiceReqFragment;
import com.ivy.ui.AssetServiceRequest.AssetServiceRequestActivity;
import com.ivy.ui.AssetServiceRequest.AssetServiceRequestContractor;
import com.ivy.ui.AssetServiceRequest.AssetServiceRequestViewActivity;
import com.ivy.ui.AssetServiceRequest.NewAssetServiceRequest;
import com.ivy.ui.notes.di.NotesModule;

import dagger.Component;

@PerActivity
@Component(dependencies = IvyAppComponent.class, modules = {AssetServiceRequestModule.class})
public interface AssetServiceRequestComponent {

    void inject(AssetServiceReqFragment assetServiceRequestFragment);

    void inject(NewAssetServiceRequest newAssetServiceRequest);

    void inject(AssetServiceRequestViewActivity fullDetailView);
}
