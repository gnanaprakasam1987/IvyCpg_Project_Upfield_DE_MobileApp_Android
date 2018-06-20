package com.ivy.ui.activation;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.core.di.scope.PerActivity;

public interface ActivationContract {

    interface ActivationView extends BaseIvyView{

        void showActivationEmptyError();

        void showInvalidActivationError();

        void navigateToLoginScreen();

    }

    @PerActivity
    interface ActivationPresenter<V extends ActivationView> extends BaseIvyPresenter<V>{

         void validateActivationKey(String activationKey);

         void triggerIMEIActivation(String imei);
    }


}
