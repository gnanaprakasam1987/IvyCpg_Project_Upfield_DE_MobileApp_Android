package com.ivy.ui.AssetServiceRequest.data;

import com.ivy.core.data.AppDataManagerContract;
import com.ivy.cpg.view.serializedAsset.SerializedAssetBO;
import com.ivy.sd.png.bo.ReasonMaster;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.Single;

public interface AssetServiceRequestDataManager extends AppDataManagerContract {

    Observable<ArrayList<String>> loadConfigs();

    Observable<ArrayList<SerializedAssetBO>> fetchAssetServiceRequests(boolean isFromReport);
    Single<Boolean> cancelServiceRequest(String requestId);

    Observable<ArrayList<SerializedAssetBO>> fetchAssets(boolean isFromReport);
    Observable<ArrayList<ReasonMaster>> fetchServiceProvider();

    Single<Boolean> saveNewServiceRequest(SerializedAssetBO assetBO);

    Single<Boolean> updateServiceRequest(SerializedAssetBO assetBO);

}
