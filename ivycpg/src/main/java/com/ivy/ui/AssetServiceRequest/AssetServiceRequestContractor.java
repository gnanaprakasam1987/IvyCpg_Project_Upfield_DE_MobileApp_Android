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

        void loadServiceRequests(boolean isFromReport);
        SerializedAssetBO getServiceRequestDetails(String requestId);
        void cancelServiceRequest(String requestId);

        void fetchLists(boolean isFromReport);

        void validateRequests(SerializedAssetBO assetBO);
        void saveNewRequest(SerializedAssetBO assetBO);
        void updateRequest(SerializedAssetBO assetBO);
        void loadConfigs();

    }

    interface AssetServiceView  extends BaseIvyView {


        void showErrorMessage(int type);

    }

    interface AssetServiceListView extends AssetServiceView{
        void listServiceRequests(ArrayList<SerializedAssetBO> requestList);
        void onCancelledSuccessfully();


    }

    interface AssetNewServiceView extends AssetServiceView{

        void populateViews(ArrayList<SerializedAssetBO> assetList, ArrayList<ReasonMaster> issueTypes,ArrayList<ReasonMaster> serviceProviders);

        void showServiceProvider();

        void showEmptyAssetMessage();
        void showEmptyIssueTypeMessage();
        void showEmptySerialNumberMessage();
        void saveRequest();
        void onSavedSuccessfully();
        void onUpdatedSuccessfully();

    }

    interface AssetServiceFullDetailView extends AssetServiceView{
        void listServiceRequests(ArrayList<SerializedAssetBO> requestList);


    }
}
