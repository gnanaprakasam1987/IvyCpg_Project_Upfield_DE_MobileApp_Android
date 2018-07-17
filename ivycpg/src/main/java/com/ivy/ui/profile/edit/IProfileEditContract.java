package com.ivy.ui.profile.edit;

import android.content.Context;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.core.di.scope.PerActivity;

public interface IProfileEditContract {

    interface ProfileEditView extends BaseIvyView{

        void createImageView();
        void createEditTextView();
        void createTextView();
        void createCheckBoxView();
        void createButtonView();
        void createSpinnerView();
        void createEditTextWithButtonView();
        void createEditTextWithSpiinerView();
        void showSuccessfullyProfileUpdatedAlert();
        void navigateToProfileScreen();
    }

    @PerActivity
    interface ProfileEditPresenter<V extends ProfileEditView> extends BaseIvyPresenter<V>{
        void downLoadDataFromDataBase(Context context);
        void validateOTP(String type, String value);
        void updateProfile();
    }
}
