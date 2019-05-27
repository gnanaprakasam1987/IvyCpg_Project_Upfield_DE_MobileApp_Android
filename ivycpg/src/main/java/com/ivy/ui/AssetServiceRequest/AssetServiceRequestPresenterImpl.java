package com.ivy.ui.AssetServiceRequest;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.core.data.reason.ReasonDataManager;
import com.ivy.core.di.scope.ReasonInfo;
import com.ivy.cpg.view.serializedAsset.SerializedAssetBO;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.ui.AssetServiceRequest.data.AssetServiceRequestDataManager;
import com.ivy.ui.AssetServiceRequest.data.AssetServiceRequestHelper;
import com.ivy.utils.rx.SchedulerProvider;

import java.util.ArrayList;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.observers.DisposableObserver;

public class AssetServiceRequestPresenterImpl<V extends AssetServiceRequestContractor.AssetServiceView> extends BasePresenter<V> implements AssetServiceRequestContractor.Presenter<V>{

    private AssetServiceRequestDataManager assetDataManager;
    private ReasonDataManager reasonDataManager;

    @Inject
    public AssetServiceRequestPresenterImpl(DataManager dataManager, SchedulerProvider schedulerProvider,
                                            CompositeDisposable compositeDisposable,
                                            ConfigurationMasterHelper configurationMasterHelper,
                                            V view, AssetServiceRequestDataManager assetDataManager,@ReasonInfo ReasonDataManager reasonDataManager){

        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, view);

        this.assetDataManager =assetDataManager;
        this.reasonDataManager=reasonDataManager;
    }


    @Override
    public void loadServiceRequests(String retailerId, boolean isFromReport) {

        getIvyView().showLoading();
        ArrayList<SerializedAssetBO> requestList = new ArrayList<>();
        getCompositeDisposable().add(assetDataManager.
                fetchAssetServiceRequests(retailerId, isFromReport)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<ArrayList<SerializedAssetBO>>() {

                    @Override
                    public void onNext(ArrayList<SerializedAssetBO> notesBoArrayList) {
                        requestList.clear();
                        requestList.addAll(notesBoArrayList);

                    }

                    @Override
                    public void onError(Throwable e) {
                        getIvyView().hideLoading();
                        getIvyView().showErrorMessage(1);
                    }

                    @Override
                    public void onComplete() {
                        ((AssetServiceRequestContractor.AssetServiceListView) getIvyView()).listServiceRequests(requestList);
                        getIvyView().hideLoading();
                    }
                }));


    }

    @Override
    public void fetchLists(String retailerId) {

        getIvyView().showLoading();

        ArrayList<ReasonMaster> issueTypeList=new ArrayList<>();
        ArrayList<SerializedAssetBO> assetsList=new ArrayList<>();
        getCompositeDisposable().add(Observable.zip(reasonDataManager.fetchReasonFromStdListMasterByListCode("ASSET_SER_REQ"),
                assetDataManager.fetchAssets(retailerId),
                new BiFunction<ArrayList<ReasonMaster>, ArrayList<SerializedAssetBO>, Object>() {
                    @Override
                    public Boolean apply(ArrayList<ReasonMaster> issueTypes, ArrayList<SerializedAssetBO> assetList) {
                        issueTypeList.clear();
                        issueTypeList.addAll(issueTypes);

                        assetsList.clear();
                        assetsList.addAll(assetList);

                        return true;
                    }
                }).subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<Object>() {
                    @Override
                    public void onNext(Object o) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        getIvyView().onError("Something went wrong");
                        getIvyView().hideLoading();
                    }

                    @Override
                    public void onComplete() {

                        ((AssetServiceRequestContractor.AssetNewServiceView)getIvyView()).populateViews(assetsList,issueTypeList);
                        getIvyView().hideLoading();

                    }
                }));

    }

    @Override
    public void saveNewRequest(SerializedAssetBO assetBO) {

        getIvyView().showLoading();

        getCompositeDisposable().add(assetDataManager.saveNewServiceRequest(assetBO)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(isAdded -> {
                    getIvyView().hideLoading();
                    if(isAdded)
                        ((AssetServiceRequestContractor.AssetNewServiceView)getIvyView()).onSavedSuccessfully();

                }));
    }
}
