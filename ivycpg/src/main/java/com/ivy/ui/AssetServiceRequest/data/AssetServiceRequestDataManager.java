package com.ivy.ui.AssetServiceRequest.data;

import com.ivy.core.data.AppDataManagerContract;
import com.ivy.cpg.view.serializedAsset.SerializedAssetBO;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.Single;

public interface AssetServiceRequestDataManager extends AppDataManagerContract {


    Observable<ArrayList<SerializedAssetBO>> fetchAssetServiceRequests(String retailerId, boolean isFromReport);

    Observable<ArrayList<SerializedAssetBO>> fetchAssets(String retailerId);

    Single<Boolean> saveNewServiceRequest(SerializedAssetBO assetBO);

}
