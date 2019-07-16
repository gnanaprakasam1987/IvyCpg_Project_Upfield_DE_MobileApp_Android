package com.ivy.ui.profile;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.ui.profile.view.ProfileBaseBo;

public interface IProfileContractor {

    interface IProfileView extends BaseIvyView {

        void showSuccessMessage();

        void showFailureMessage();

        void showAlert();

    }

    interface IProfilePresenter<V extends IProfileView> extends BaseIvyPresenter<V> {

        void saveProfileData(ProfileBaseBo retailerProfileField);
    }
}
