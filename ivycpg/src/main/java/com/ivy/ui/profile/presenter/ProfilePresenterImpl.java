package com.ivy.ui.profile.presenter;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.core.data.sync.SynchronizationDataManager;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.ui.profile.IProfileContractor;
import com.ivy.ui.profile.data.ProfileDataManager;
import com.ivy.ui.profile.view.ProfileBaseBo;
import com.ivy.utils.AppUtils;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.StringUtils;
import com.ivy.utils.rx.SchedulerProvider;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function4;

public class ProfilePresenterImpl <V extends IProfileContractor.IProfileView>
        extends BasePresenter<V> implements IProfileContractor.IProfilePresenter<V>{

    private DataManager dataManager;
    private ProfileDataManager profileDataManager;

    @Inject
    public ProfilePresenterImpl(DataManager dataManager, SchedulerProvider schedulerProvider,
                                CompositeDisposable compositeDisposable, ConfigurationMasterHelper configurationMasterHelper,
                                V view, ProfileDataManager profileDataManager, SynchronizationDataManager synchronizationDataManager) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, view);

        this.dataManager = dataManager;
        this.profileDataManager = profileDataManager;
    }

    private String tid="";
    private String currentDate="";

    @Override
    public void saveProfileData(final ProfileBaseBo retailerProfileField) {

        getIvyView().showLoading();

        currentDate = DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL);
        tid = dataManager.getUser().getUserid()
                + "" + dataManager.getRetailMaster().getRetailerID()
                + "" + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);

        getCompositeDisposable().add(profileDataManager.checkHeaderAvailablility(dataManager.getRetailMaster().getRetailerID(), currentDate)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<String>() {
                               @Override
                               public void accept(String response) throws Exception {
                                   if (!StringUtils.isNullOrEmpty(response))
                                       tid = response;
                                   saveProfileEditValues(retailerProfileField);
                               }
                           }, new Consumer<Throwable>() {
                               @Override
                               public void accept(Throwable throwable) throws Exception {
                                   Commons.print(throwable.getMessage());
                                   saveProfileEditValues(retailerProfileField);
                               }
                           }
                ));

    }

    private void saveProfileEditValues(ProfileBaseBo retailerProfileField) {

        getCompositeDisposable().add(Single.zip(profileDataManager.saveEditProfileField(retailerProfileField.getProfileFields(), tid),
                profileDataManager.saveEditContactData(retailerProfileField.getContactList(), tid),
                profileDataManager.saveEditAttributeData(retailerProfileField.getAttributeList(), tid),
                profileDataManager.updateRetailer(tid, dataManager.getRetailMaster().getRetailerID(), currentDate),
                new Function4<Boolean, Boolean, Boolean, Boolean, Boolean>() {
                    @Override
                    public Boolean apply(Boolean isProfileField, Boolean isContactData, Boolean isAttributeData, Boolean isRetailerHeader) {
                        return true;
                    }
                }).subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean isUpdated) {
                        if (isUpdated) {
                            AppUtils.latlongImageFileName = "";
                        }
                        getIvyView().hideLoading();
                        getIvyView().showAlert();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        getIvyView().hideLoading();
                        Commons.print(throwable.getMessage());
                    }
                }));

    }

}
