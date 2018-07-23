package com.ivy.ui.photocapture.presenter;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
import android.support.annotation.NonNull;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.core.data.outlettime.OutletTimeStampDataManager;
import com.ivy.core.di.scope.LabelMasterInfo;
import com.ivy.core.di.scope.OutletTimeStampInfo;
import com.ivy.cpg.view.photocapture.PhotoCaptureLocationBO;
import com.ivy.cpg.view.photocapture.PhotoCaptureProductBO;
import com.ivy.cpg.view.photocapture.PhotoTypeMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.LabelsMasterHelper;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.ui.photocapture.PhotoCaptureContract;
import com.ivy.ui.photocapture.data.PhotoCaptureDataManager;
import com.ivy.utils.rx.SchedulerProvider;

import java.util.ArrayList;
import java.util.HashMap;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function3;
import io.reactivex.observers.DisposableObserver;

public class PhotoCapturePresenterImpl<V extends PhotoCaptureContract.PhotoCaptureView> extends BasePresenter<V> implements PhotoCaptureContract.PhotoCapturePresenter<V>, LifecycleObserver {


    private OutletTimeStampDataManager mOutletTimeStampDataManager;

    private LabelsMasterHelper labelsMasterHelper;

    private PhotoCaptureDataManager photoCaptureDataManager;

    private ConfigurationMasterHelper mConfigurationMasterHelper;

    private ArrayList<PhotoCaptureProductBO> mProductBOS = new ArrayList<>();

    private ArrayList<PhotoTypeMasterBO> mTypeMasterBOS = new ArrayList<>();

    private ArrayList<PhotoCaptureLocationBO> mLocationBOS = new ArrayList<>();


    private HashMap<String, PhotoCaptureLocationBO> editedData;

    @Inject
    public PhotoCapturePresenterImpl(DataManager dataManager, SchedulerProvider schedulerProvider, CompositeDisposable compositeDisposable,
                                     ConfigurationMasterHelper configurationMasterHelper, V view,
                                     @OutletTimeStampInfo OutletTimeStampDataManager outletTimeStampDataManager,
                                     PhotoCaptureDataManager photoCaptureDataManager, @LabelMasterInfo LabelsMasterHelper labelsMasterHelper) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, view);
        this.mOutletTimeStampDataManager = outletTimeStampDataManager;
        this.photoCaptureDataManager = photoCaptureDataManager;
        this.mConfigurationMasterHelper = configurationMasterHelper;
        this.labelsMasterHelper = labelsMasterHelper;

        if (view instanceof LifecycleOwner) {
            ((LifecycleOwner) view).getLifecycle().addObserver(this);
        }
    }

    @Override
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void fetchData() {
 /*       fetchPhotoCaptureProducts();
        fetchPhotoCaptureTypes();
        fetchLocations();*/
        getIvyView().showLoading();
        fetchEditedPhotoTypes();
        getCompositeDisposable().add(Observable.zip(photoCaptureDataManager.fetchPhotoCaptureProducts(),
                photoCaptureDataManager.fetchPhotoCaptureTypes(),
                photoCaptureDataManager.fetchLocations(),
                new Function3<ArrayList<PhotoCaptureProductBO>, ArrayList<PhotoTypeMasterBO>, ArrayList<PhotoCaptureLocationBO>, Object>() {
                    @Override
                    public Boolean apply(ArrayList<PhotoCaptureProductBO> photoCaptureProductBOS, ArrayList<PhotoTypeMasterBO> photoTypeMasterBOS, ArrayList<PhotoCaptureLocationBO> photoCaptureLocationBOS) throws Exception {
                        mProductBOS.clear();
                        mProductBOS.addAll(photoCaptureProductBOS);

                        if (photoCaptureProductBOS.size() != 0)
                            getIvyView().setProductListData(photoCaptureProductBOS);


                        mTypeMasterBOS.clear();
                        mTypeMasterBOS.addAll(photoTypeMasterBOS);

                        if (photoTypeMasterBOS.size() != 0) {
                            getIvyView().setPhotoTypeData(photoTypeMasterBOS);
                        }

                        mLocationBOS.clear();
                        mLocationBOS.addAll(photoCaptureLocationBOS);
                        if (photoCaptureLocationBOS.size() != 0) {
                            getIvyView().setLocationData(photoCaptureLocationBOS);
                        }


                        return true;
                    }
                }).subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<Object>() {
                    @Override
                    public void onNext(Object o) {
                        getIvyView().hideLoading();
                    }

                    @Override
                    public void onError(Throwable e) {
                        getIvyView().hideLoading();
                    }

                    @Override
                    public void onComplete() {

                    }
                }));
    }

    @Override
    public HashMap<String, PhotoCaptureLocationBO> getEditedPhotoListData() {
        return editedData;
    }

    @Override
    public void setEditedPhotosListData(HashMap<String, PhotoCaptureLocationBO> editedPhotosListData) {
        editedData.clear();
        editedData.putAll(editedPhotosListData);
        getIvyView().setSpinnerDefaults();
    }

    @Override
    public ArrayList<PhotoCaptureLocationBO> getLocationBOS() {
        return mLocationBOS;
    }

    @Override
    public int getGlobalLocationIndex() {
        return getDataManager().getGlobalLocationIndex();
    }

    @Override
    public String getTitleLabel() {
        return labelsMasterHelper
                .applyLabels((Object) "menu_photo");
    }

    @Override
    public void updateModuleTime() {

        getCompositeDisposable().add(mOutletTimeStampDataManager.updateTimeStampModuleWise(SDUtil
                .now(SDUtil.TIME))
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {

                    }
                }));
    }

    @Override
    public String getRetailerId() {
        return getDataManager().getRetailMaster().getRetailerID();
    }

    @Override
    public boolean isImagePathChanged() {
        return mConfigurationMasterHelper.IS_PHOTO_CAPTURE_IMG_PATH_CHANGE;
    }

    @Override
    public void updateLocalData(int productId, int typeId, int locationId, String imageName, String feedback, String productName, String typeName, String locationName) {
        updateLocalData(productId,typeId,locationId,imageName,feedback,"","","","",productName,typeName,locationName);
    }

    @Override
    public void updateLocalData(int productId, int typeId, int locationId, String imageName, String feedback, String skuName, String abv, String lotNumber, String seqNumber, String productName, String typeName, String locationName) {
        String imagePath = getImagePath(imageName);

        PhotoCaptureLocationBO photoCaptureLocationBO = new PhotoCaptureLocationBO();
        photoCaptureLocationBO.setPhotoTypeId(typeId);
        photoCaptureLocationBO.setProductID(productId);
        photoCaptureLocationBO.setLocationId(locationId);
        photoCaptureLocationBO.setImageName(imageName);
        photoCaptureLocationBO.setImagePath(imagePath);
        photoCaptureLocationBO.setFeedback(feedback);
        photoCaptureLocationBO.setSKUName(skuName);
        photoCaptureLocationBO.setAbv(abv);
        photoCaptureLocationBO.setLotCode(lotNumber);
        photoCaptureLocationBO.setSequenceNO(seqNumber);
        photoCaptureLocationBO.setProductName(productName);
        photoCaptureLocationBO.setmTypeName(typeName);
        photoCaptureLocationBO.setLocationName(locationName);

        if (isDateEnabled()) {
            photoCaptureLocationBO.setFromDate(getIvyView().getFromDate());
            photoCaptureLocationBO.setToDate(getIvyView().getToDate());
        }

        editedData.put(productId + "_" + typeId + "_" + locationId, photoCaptureLocationBO);

    }

    @Override
    public void onSaveButtonClick() {
        getIvyView().showLoading();
        getCompositeDisposable().add(Single.zip(photoCaptureDataManager.updatePhotoCaptureDetails(editedData),
                mOutletTimeStampDataManager.updateTimeStampModuleWise(SDUtil.now(SDUtil.TIME)),
                getDataManager().updateModuleTime(HomeScreenTwo.MENU_PHOTO),
                new Function3<Boolean, Boolean, Boolean, Boolean>() {
                    @Override
                    public Boolean apply(Boolean isDataUpdated, Boolean isTimeStampUpdated, Boolean isModuleTimeUpdated) throws Exception {
                        return isDataUpdated && isTimeStampUpdated && isModuleTimeUpdated;
                    }
                }).subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean isUpdated) throws Exception {

                        if (isUpdated) {
                            getIvyView().showUpdatedDialog();
                        }
                        getIvyView().hideLoading();

                    }
                }));


    }

    @Override
    public boolean shouldNavigateToNextActivity() {
        return mConfigurationMasterHelper.IS_PRINT_FILE_SAVE;
    }

    @NonNull
    private String getImagePath(String imageName) {
        String imagePath;
        if (mConfigurationMasterHelper.IS_PHOTO_CAPTURE_IMG_PATH_CHANGE)
            imagePath = "PhotoCapture/"
                    + getDataManager().getUser().getDownloadDate()
                    .replace("/", "") + "/"
                    + getDataManager().getUser().getUserid() + "/" + imageName;
        else
            imagePath = getDataManager().getUser().getDistributorid()
                    + "/"
                    + getDataManager().getUser().getUserid()
                    + "/"
                    + getDataManager().getUser().getDownloadDate()
                    .replace("/", "") + "/" + imageName;
        return imagePath;
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
                    public void onNext(ArrayList<PhotoTypeMasterBO> photoTypeMasterBOS) {
                        mTypeMasterBOS.clear();
                        mTypeMasterBOS.addAll(photoTypeMasterBOS);

                        if (photoTypeMasterBOS.size() != 0) {
                            getIvyView().setPhotoTypeData(photoTypeMasterBOS);
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
    public void fetchLocations() {
        getCompositeDisposable().add(photoCaptureDataManager.fetchLocations()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<ArrayList<PhotoCaptureLocationBO>>() {
                    @Override
                    public void onNext(ArrayList<PhotoCaptureLocationBO> photoCaptureLocationBOS) {
                        mLocationBOS.clear();
                        mLocationBOS.addAll(photoCaptureLocationBOS);
                        if (photoCaptureLocationBOS.size() != 0) {
                            getIvyView().setLocationData(photoCaptureLocationBOS);
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
