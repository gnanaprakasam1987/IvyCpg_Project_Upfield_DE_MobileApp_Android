package com.ivy.ui.AssetServiceRequest;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.cpg.view.serializedAsset.SerializedAssetBO;
import com.ivy.sd.png.bo.ReasonMaster;

import java.util.ArrayList;

public interface AssetServiceRequestContractor {

    @PerActivity
    interface Presenter<V extends AssetServiceView> extends BaseIvyPresenter<V> {

        void loadServiceRequests(String retailerId, boolean isFromReport);

        void fetchLists(String retailerId);

        void saveNewRequest(SerializedAssetBO assetBO);

    }

    interface AssetServiceView  extends BaseIvyView {


        void showErrorMessage(int type);

    }

    interface AssetServiceListView extends AssetServiceView{
        void listServiceRequests(ArrayList<SerializedAssetBO> requestList);

    }

    interface AssetNewServiceView extends AssetServiceView{

        void populateViews(ArrayList<SerializedAssetBO> assetList, ArrayList<ReasonMaster> issueTypes);

        void onSavedSuccessfully();

    }
}
