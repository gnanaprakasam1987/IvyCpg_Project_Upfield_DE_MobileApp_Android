package com.ivy.ui.profile.edit;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.core.di.scope.PerActivity;

public class ProfileEditContract {

    interface ProfileEditView extends BaseIvyView{
        void showFoodLicenceExpiryDateError();
        void showDrugLicenceExpiryDateError();
        void showProfileEditUpdateFailedError();
        void showInvalidEmailActivationError();
        void showSuccessfullyProfileUpdatedAlert();
        void navigateToProfileScreen();
    }

    @PerActivity
    interface ProfileEditPresenter<V extends ProfileEditContract.ProfileEditView> extends BaseIvyPresenter<V>{

        void validateOTP(String type, String value);
        void updateProfile();

    }
}
