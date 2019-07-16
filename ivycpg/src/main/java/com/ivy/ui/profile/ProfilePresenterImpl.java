package com.ivy.ui.profile;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.ui.profile.data.ProfileDataManager;
import com.ivy.ui.profile.view.ProfileBaseBo;
import com.ivy.utils.AppUtils;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.StringUtils;
import com.ivy.utils.rx.SchedulerProvider;

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.internal.operators.observable.ObservableAllSingle;
import io.reactivex.internal.operators.observable.ObservableAny;
import io.reactivex.internal.operators.observable.ObservableAnySingle;
import io.reactivex.observers.DisposableObserver;

public class ProfilePresenterImpl <V extends IProfileContractor.IProfileView>
        extends BasePresenter<V> implements IProfileContractor.IProfilePresenter<V>{

    private DataManager dataManager;
    private ProfileDataManager profileDataManager;


    public ProfilePresenterImpl(DataManager dataManager, SchedulerProvider schedulerProvider,
                                CompositeDisposable compositeDisposable, ConfigurationMasterHelper configurationMasterHelper,
                                V view, ProfileDataManager profileDataManager) {
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


        updateHeaderList();
    }

    private void saveProfileEditValues(ProfileBaseBo retailerProfileField) {

        /*getCompositeDisposable().add(Observable.zip(profileDataManager.saveEditProfileField(retailerProfileField.getProfileFields(), tid),
                profileDataManager.updateRetailer(tid, dataManager.getRetailMaster().getRetailerID(), currentDate),
                new BiFunction<Single<Boolean>, Single<Boolean>, Boolean>() {
                    @Override
                    public Boolean apply(Single<Boolean> t1, Single<Boolean> t2) throws Exception {


                        return true;
                    }
                }));*/

        getCompositeDisposable().add(Observable.zip(,
                new BiFunction<ArrayList<String>, ArrayList<String>, Boolean>() {
                    @Override
                    public Boolean apply(ArrayList<String> priorityProdList, ArrayList<String> nearbyRetailersList) throws Exception {

//                        Set Priority Product List

                        return true;
                    }
                }).subscribeOn(getSchedulerProvider().io())
                        .observeOn(getSchedulerProvider().ui())
                        .subscribeWith(new DisposableObserver<Boolean>() {
                            @Override
                            public void onNext(Boolean aBoolean) {

                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onComplete() {
                            }
                        })
        );

    }

    private void updateHeaderList() {

        getCompositeDisposable().add(profileDataManager.updateRetailer(tid, dataManager.getRetailMaster().getRetailerID(), currentDate)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<Boolean>() {
                               @Override
                               public void accept(Boolean response) throws Exception {
                                   if (response) {
                                       AppUtils.latlongImageFileName = "";

                                   }
                                   getIvyView().hideLoading();
//                                   getIvyView().showAlert();
                               }
                           }, new Consumer<Throwable>() {
                               @Override
                               public void accept(Throwable throwable) throws Exception {
                                   getIvyView().hideLoading();
                                   Commons.print(throwable.getMessage());
                               }
                           }
                ));

    }

}
