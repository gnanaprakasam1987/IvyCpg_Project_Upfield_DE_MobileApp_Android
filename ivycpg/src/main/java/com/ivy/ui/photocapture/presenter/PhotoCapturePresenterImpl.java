package com.ivy.ui.photocapture.presenter;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.core.data.outlettime.OutletTimeStampDataManager;
import com.ivy.core.data.user.UserDataManager;
import com.ivy.core.di.scope.OutletTimeStampInfo;
import com.ivy.core.di.scope.UserInfo;
import com.ivy.cpg.view.photocapture.PhotoCaptureLocationBO;
import com.ivy.cpg.view.photocapture.PhotoCaptureProductBO;
import com.ivy.cpg.view.photocapture.PhotoTypeMasterBO;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.ui.photocapture.PhotoCaptureContract;
import com.ivy.ui.photocapture.data.PhotoCaptureDataManager;
import com.ivy.utils.rx.SchedulerProvider;

import java.util.ArrayList;
import java.util.HashMap;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

public class PhotoCapturePresenterImpl<V extends PhotoCaptureContract.PhotoCaptureView> extends BasePresenter<V> implements PhotoCaptureContract.PhotoCapturePresenter<V>, LifecycleObserver {

    private UserDataManager mUserDataManager;

    private OutletTimeStampDataManager mOutletTimeStampDataManager;


    private PhotoCaptureDataManager photoCaptureDataManager;

    private ConfigurationMasterHelper mConfigurationMasterHelper;

    private ArrayList<PhotoCaptureProductBO> mProductBOS = new ArrayList<>();

    private ArrayList<PhotoTypeMasterBO> mTypeMasterBOS = new ArrayList<>();

    private ArrayList<PhotoCaptureLocationBO> mLocationBOS = new ArrayList<>();


    private HashMap<String, PhotoCaptureLocationBO> editedData;

    @Inject
    public PhotoCapturePresenterImpl(DataManager dataManager, SchedulerProvider schedulerProvider, CompositeDisposable compositeDisposable,
                                     ConfigurationMasterHelper configurationMasterHelper, V view, @UserInfo UserDataManager userDataManager, @OutletTimeStampInfo OutletTimeStampDataManager outletTimeStampDataManager, PhotoCaptureDataManager photoCaptureDataManager) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, view);
        this.mOutletTimeStampDataManager = outletTimeStampDataManager;
        this.mUserDataManager = userDataManager;
        this.photoCaptureDataManager = photoCaptureDataManager;
        this.mConfigurationMasterHelper = configurationMasterHelper;

        if (view instanceof LifecycleOwner) {
            ((LifecycleOwner) view).getLifecycle().addObserver(this);
        }
    }

    @Override
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void fetchData() {
        fetchPhotoCaptureProducts();
        fetchPhotoCaptureTypes();
        if (!mConfigurationMasterHelper.IS_GLOBAL_LOCATION)
            fetchLocations();
    }

    @Override
    public HashMap<String, PhotoCaptureLocationBO> getEditedPhotoListData() {
        return editedData;
    }

    @Override
    public int getGlobalLocationId() {
        return getDataManager().getGlobalLocationIndex();
    }

    @Override
    public boolean isMaxPhotoLimitReached() {
        if (getDataManager().getSavedImageCount() >= mConfigurationMasterHelper.photocount) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void fetchPhotoCaptureProducts() {
        getCompositeDisposable().add(photoCaptureDataManager.fetchPhotoCaptureProducts()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<ArrayList<PhotoCaptureProductBO>>() {
                    @Override
                    public void onNext(ArrayList<PhotoCaptureProductBO> photoCaptureProductBOS) {
                        if (mProductBOS == null) {
                            mProductBOS = new ArrayList<>();
                        }
                        mProductBOS.clear();
                        mProductBOS.addAll(photoCaptureProductBOS);

                        if (photoCaptureProductBOS.size() != 0)
                            getIvyView().setProductListData(photoCaptureProductBOS);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                }));
    }

    @Override
    public void fetchPhotoCaptureTypes() {
        getCompositeDisposable().add(photoCaptureDataManager.fetchPhotoCaptureTypes()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<ArrayList<PhotoTypeMasterBO>>() {
                    @Override
                    public void onNext(ArrayList<PhotoTypeMasterBO> photoCaptureProductBOS) {
                        mTypeMasterBOS.clear();
                        mTypeMasterBOS.addAll(photoCaptureProductBOS);


                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                }));
    }

    @Override
    public void fetchLocations() {
        getCompositeDisposable().add(photoCaptureDataManager.fetchLocations()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<ArrayList<PhotoCaptureLocationBO>>() {
                    @Override
                    public void onNext(ArrayList<PhotoCaptureLocationBO> photoCaptureProductBOS) {
                        mLocationBOS.clear();
                        mLocationBOS.addAll(photoCaptureProductBOS);


                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                }));
    }

    @Override
    public void fetchEditedPhotoTypes() {
        getCompositeDisposable().add(photoCaptureDataManager.fetchEditedLocations(getDataManager().getRetailMaster().getRetailerID(), getDataManager().getRetailMaster().getDistributorId())
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<ArrayList<PhotoCaptureLocationBO>>() {
                    @Override
                    public void onNext(ArrayList<PhotoCaptureLocationBO> photoCaptureProductBOS) {

                        editedData = new HashMap<>();
                        for (PhotoCaptureLocationBO photoCaptureLocationBO : photoCaptureProductBOS) {
                            editedData.put(photoCaptureLocationBO.getProductID() + "_" + photoCaptureLocationBO.getPhotoTypeId() + "_" + photoCaptureLocationBO.getLocationId(), photoCaptureLocationBO);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                }));
    }

    @Override
    public boolean isGlobalLocation() {
        return mConfigurationMasterHelper.IS_GLOBAL_LOCATION;
    }

    @Override
    public boolean isDateEnabled() {
        return mConfigurationMasterHelper.SHOW_DATE_BTN;
    }


}
