package com.ivy.ui.profile.edit.presenter;


import android.content.Context;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.ui.profile.data.ProfileDataManager;
import com.ivy.ui.profile.data.ProfileDataManagerImpl;
import com.ivy.ui.profile.edit.IProfileEditContract;
import com.ivy.utils.rx.SchedulerProvider;


import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;

public class ProfileEditPresenterImp<V extends IProfileEditContract.ProfileEditView>
        extends BasePresenter<V> implements IProfileEditContract.ProfileEditPresenter<V> {

    ProfileDataManager profileDataManager;

    @Inject
    public ProfileEditPresenterImp(DataManager dataManager, SchedulerProvider schedulerProvider,
                                   CompositeDisposable compositeDisposable,
                                   ConfigurationMasterHelper configurationMasterHelper,
                                   ProfileDataManagerImpl profileDataManager,
                                   V view) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, view);
        this.profileDataManager=profileDataManager;
    }

    @Override
    public void downLoadDataFromDataBase(Context context) {

        profileDataManager.loadContactTitle(context);
    }

    @Override
    public void validateOTP(String type, String value) {

    }

    @Override
    public void updateProfile() {

    }
}
