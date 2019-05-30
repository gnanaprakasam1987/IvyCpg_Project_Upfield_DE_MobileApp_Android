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
import io.reactivex.functions.Function3;
import io.reactivex.observers.DisposableObserver;

public class AssetServiceRequestPresenterImpl<V extends AssetServiceRequestContractor.AssetServiceView> extends BasePresenter<V> implements AssetServiceRequestContractor.Presenter<V>{


    private AssetServiceRequestDataManager assetDataManager;
    private ReasonDataManager reasonDataManager;
    private ArrayList<SerializedAssetBO> requestList;
    private ArrayList<String> configList;

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
    public void loadServiceRequests(boolean isFromReport) {

        getIvyView().showLoading();
        requestList = new ArrayList<>();
        getCompositeDisposable().add(assetDataManager.
                fetchAssetServiceRequests( isFromReport)
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
    public void fetchLists(boolean isFromReport) {

        getIvyView().showLoading();

        ArrayList<ReasonMaster> issueTypeList=new ArrayList<>();
        ArrayList<SerializedAssetBO> assetsList=new ArrayList<>();
        ArrayList<ReasonMaster> serviceProviderList=new ArrayList<>();
        getCompositeDisposable().add(Observable.zip(reasonDataManager.fetchReasonFromStdListMasterByListCode("ASSET_SER_REQ"),
                assetDataManager.fetchAssets(isFromReport),assetDataManager.fetchServiceProvider(),
                new Function3<ArrayList<ReasonMaster>, ArrayList<SerializedAssetBO>,ArrayList<ReasonMaster>, Object>() {
                    @Override
                    public Boolean apply(ArrayList<ReasonMaster> issueTypes, ArrayList<SerializedAssetBO> assetList,ArrayList<ReasonMaster> providers) {
                        issueTypeList.clear();
                        issueTypeList.addAll(issueTypes);

                        assetsList.clear();
                        assetsList.addAll(assetList);

                        serviceProviderList.clear();
                        serviceProviderList.addAll(providers);
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

                        ((AssetServiceRequestContractor.AssetNewServiceView)getIvyView()).populateViews(assetsList,issueTypeList,serviceProviderList);
                        getIvyView().hideLoading();

                    }
                }));

    }

    @Override
    public void validateRequests(SerializedAssetBO assetBO) {
        if(assetBO.getSerialNo().equalsIgnoreCase("0")||
                assetBO.getSerialNo().equalsIgnoreCase("")){
            ((AssetServiceRequestContractor.AssetNewServiceView)getIvyView()).showEmptySerialNumberMessage();
        }
        else if(assetBO.getAssetID()==0){
            ((AssetServiceRequestContractor.AssetNewServiceView)getIvyView()).showEmptyAssetMessage();
        }
        else if(assetBO.getReasonID()==0){
            ((AssetServiceRequestContractor.AssetNewServiceView)getIvyView()).showEmptyIssueTypeMessage();
        }
        else {
            ((AssetServiceRequestContractor.AssetNewServiceView)getIvyView()).saveRequest();
        }

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
                    else ((AssetServiceRequestContractor.AssetNewServiceView)getIvyView()).showErrorMessage(0);

                }));
    }

    @Override
    public void updateRequest(SerializedAssetBO assetBO) {
        getIvyView().showLoading();

        getCompositeDisposable().add(assetDataManager.updateServiceRequest(assetBO)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(isAdded -> {
                    getIvyView().hideLoading();
                    if(isAdded)
                        ((AssetServiceRequestContractor.AssetNewServiceView)getIvyView()).onSavedSuccessfully();
                    else ((AssetServiceRequestContractor.AssetNewServiceView)getIvyView()).showErrorMessage(0);

                }));
    }

    @Override
    public SerializedAssetBO getServiceRequestDetails(String requestId) {

        if(requestList!=null) {
            for (SerializedAssetBO assetBO : requestList) {
                if (assetBO.getRField().equals(requestId)) {
                    return assetBO;
                }
            }
        }

        return new SerializedAssetBO();
    }

    @Override
    public void cancelServiceRequest(String requestId) {
        getIvyView().showLoading();

        getCompositeDisposable().add(assetDataManager.cancelServiceRequest(requestId)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(isCancelled -> {
                    getIvyView().hideLoading();
                    if(isCancelled) {
                        ((AssetServiceRequestContractor.AssetServiceListView) getIvyView()).onCancelledSuccessfully();
                    }
                    else ((AssetServiceRequestContractor.AssetNewServiceView)getIvyView()).showErrorMessage(0);

                }));
    }

    @Override
    public void loadConfigs() {

        getIvyView().showLoading();

        configList = new ArrayList<>();
        getCompositeDisposable().add(assetDataManager.
                loadConfigs()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<ArrayList<String>>() {

                    @Override
                    public void onNext(ArrayList<String> list) {
                        configList.clear();
                        configList.addAll(list);

                    }

                    @Override
                    public void onError(Throwable e) {
                        getIvyView().hideLoading();
                        getIvyView().showErrorMessage(1);
                    }

                    @Override
                    public void onComplete() {
                        if(configList.contains("SHOW_SERVICE_PROVIDER"))
                        ((AssetServiceRequestContractor.AssetServiceListView) getIvyView()).listServiceRequests(requestList);

                        getIvyView().hideLoading();
                    }
                }));

    }
}
