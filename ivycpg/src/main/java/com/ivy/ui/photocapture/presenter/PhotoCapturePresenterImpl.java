package com.ivy.ui.photocapture.presenter;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.annotation.NonNull;

import com.ivy.core.IvyConstants;
import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.core.data.label.LabelsDataManager;
import com.ivy.core.data.outlettime.OutletTimeStampDataManager;
import com.ivy.core.di.scope.LabelMasterInfo;
import com.ivy.core.di.scope.OutletTimeStampInfo;
import com.ivy.ui.photocapture.model.PhotoCaptureLocationBO;
import com.ivy.ui.photocapture.model.PhotoCaptureProductBO;
import com.ivy.ui.photocapture.model.PhotoTypeMasterBO;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.ui.photocapture.PhotoCaptureContract;
import com.ivy.ui.photocapture.data.PhotoCaptureDataManager;
import com.ivy.utils.DateTimeUtils;
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

import static com.ivy.utils.DateTimeUtils.DATE_GLOBAL;

public class PhotoCapturePresenterImpl<V extends PhotoCaptureContract.PhotoCaptureView> extends BasePresenter<V> implements PhotoCaptureContract.PhotoCapturePresenter<V>, LifecycleObserver {


    private OutletTimeStampDataManager mOutletTimeStampDataManager;

    private LabelsDataManager labelsDataManager;

    private PhotoCaptureDataManager photoCaptureDataManager;

    private ConfigurationMasterHelper mConfigurationMasterHelper;

    private ArrayList<PhotoCaptureProductBO> mProductBOS = new ArrayList<>();

    private ArrayList<PhotoTypeMasterBO> mTypeMasterBOS = new ArrayList<>();

    private ArrayList<PhotoCaptureLocationBO> mLocationBOS = new ArrayList<>();


    private HashMap<String, PhotoCaptureLocationBO> editedData = new HashMap<>();

    @Inject
    public PhotoCapturePresenterImpl(DataManager dataManager, SchedulerProvider schedulerProvider, CompositeDisposable compositeDisposable,
                                     ConfigurationMasterHelper configurationMasterHelper, V view,
                                     @OutletTimeStampInfo OutletTimeStampDataManager outletTimeStampDataManager,
                                     PhotoCaptureDataManager photoCaptureDataManager, @LabelMasterInfo LabelsDataManager labelsDataManager) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, view);
        this.mOutletTimeStampDataManager = outletTimeStampDataManager;
        this.photoCaptureDataManager = photoCaptureDataManager;
        this.mConfigurationMasterHelper = configurationMasterHelper;
        this.labelsDataManager = labelsDataManager;

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
                    public Boolean apply(ArrayList<PhotoCaptureProductBO> photoCaptureProductBOS, ArrayList<PhotoTypeMasterBO> photoTypeMasterBOS, ArrayList<PhotoCaptureLocationBO> photoCaptureLocationBOS) {
                        mProductBOS.clear();
                        mProductBOS.addAll(photoCaptureProductBOS);

                        mTypeMasterBOS.clear();
                        mTypeMasterBOS.addAll(photoTypeMasterBOS);

                        mLocationBOS.clear();
                        mLocationBOS.addAll(photoCaptureLocationBOS);


                        return true;
                    }
                }).subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<Object>() {
                    @Override
                    public void onNext(Object o) {
                        if (mProductBOS.size() != 0)
                            getIvyView().setProductListData(mProductBOS);

                        if (mTypeMasterBOS.size() != 0) {
                            getIvyView().setPhotoTypeData(mTypeMasterBOS);
                        }

                        if (mLocationBOS.size() != 0) {
                            getIvyView().setLocationData(mLocationBOS);
                        }


                        getIvyView().hideLoading();
                    }

                    @Override
                    public void onError(Throwable e) {
                        getIvyView().onError("Something went wrong");
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
    public void getTitleLabel() {

        getCompositeDisposable().add(labelsDataManager.getLabel("menu_photo")
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui()).subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String value) {
                        getIvyView().setToolBarTitle(value);
                    }
                }));

    }

    @Override
    public void updateModuleTime() {

        String date = DateTimeUtils.now(DATE_GLOBAL) + " " + DateTimeUtils
                .now(DateTimeUtils.TIME);
        if (mConfigurationMasterHelper.IS_DISABLE_CALL_ANALYSIS_TIMER)
            date = IvyConstants.DEFAULT_TIME_CONSTANT;
        getCompositeDisposable().add(mOutletTimeStampDataManager.updateTimeStampModuleWise(date)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) {

                    }
                }));
    }

    @Override
    public String getRetailerId() {
        return getDataManager().getRetailMaster().getRetailerID();
    }

    /**
     *
     * @return <code>true</code> if PHOTOCAP05 is true
     */
    @Override
    public boolean isImagePathChanged() {
        return mConfigurationMasterHelper.IS_PHOTO_CAPTURE_IMG_PATH_CHANGE;
    }

    @Override
    public void updateLocalData(int productId, int typeId, int locationId, String imageName, String feedback, String productName, String typeName, String locationName) {
        updateLocalData(productId, typeId, locationId, imageName, feedback, "", "", "", "", productName, typeName, locationName);
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

            if(getIvyView().getFromDate().length()!=0)
                photoCaptureLocationBO.setFromDate(getIvyView().getFromDate());

            if(getIvyView().getToDate().length()!=0)
                photoCaptureLocationBO.setToDate(getIvyView().getToDate());
        }

        editedData.put(productId + "_" + typeId + "_" + locationId, photoCaptureLocationBO);

    }

    @Override
    public void onSaveButtonClick() {
        getIvyView().showLoading();
        String date = DateTimeUtils.now(DATE_GLOBAL) + " " + DateTimeUtils
                .now(DateTimeUtils.TIME);
        if (mConfigurationMasterHelper.IS_DISABLE_CALL_ANALYSIS_TIMER)
            date = IvyConstants.DEFAULT_TIME_CONSTANT;
        getCompositeDisposable().add(Single.zip(photoCaptureDataManager.updatePhotoCaptureDetails(editedData),
                mOutletTimeStampDataManager.updateTimeStampModuleWise(date),
                getDataManager().updateModuleTime(HomeScreenTwo.MENU_PHOTO),
                new Function3<Boolean, Boolean, Boolean, Boolean>() {
                    @Override
                    public Boolean apply(Boolean isDataUpdated, Boolean isTimeStampUpdated, Boolean isModuleTimeUpdated) {
                        return isDataUpdated && isTimeStampUpdated && isModuleTimeUpdated;
                    }
                }).subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean isUpdated) {

                        if (isUpdated) {
                            getIvyView().showUpdatedDialog();
                        }
                        getIvyView().hideLoading();

                    }
                }));


    }

    /**
     * @return <code>true<code/> if FUN52 is true
     */
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
        return getDataManager().getSavedImageCount() >= mConfigurationMasterHelper.photocount;
    }

/*
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
*/

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

    /**
     * @return <code>true</code> if FUN23 is true
     */
    @Override
    public boolean isGlobalLocation() {
        return mConfigurationMasterHelper.IS_GLOBAL_LOCATION;
    }

    /**
     * @return <code>true</code> if PHOTOCAP02 is true
     */
    @Override
    public boolean isDateEnabled() {
        return mConfigurationMasterHelper.SHOW_DATE_BTN;
    }


    @Override
    public void onDetach() {
        photoCaptureDataManager.tearDown();
        mOutletTimeStampDataManager.tearDown();
        labelsDataManager.tearDown();
        super.onDetach();
    }
}
