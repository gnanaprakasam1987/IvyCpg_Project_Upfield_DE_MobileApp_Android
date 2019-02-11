package com.ivy.ui.activation;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.activation.bo.ActivationBO;

import java.util.List;

public interface ActivationContract {

    interface ActivationView extends BaseIvyView {

        // Error alert
        void showActivationEmptyError();

        // Error alert
        void showInvalidActivationError();

        //Error alert
        void showTryValidKeyError();

        //Error alert
        void showActivationFailedError();

        //Error toast
        void showConfigureUrlMessage();

        //Error toast
        void showAppUrlIsEmptyError();

        //Error toast
        void showServerError();

        // Error toast
        void showPreviousActivationError();

        //Error toast
        void showContactAdminMessage();

        // This method should be used for all errors
        void showActivationError(String activationError);

        // Do we need this?
        void navigateToLoginScreen();


        void showActivationDialog();

        void doValidationSuccess();

        void showSuccessfullyActivatedAlert();


    }

    @PerActivity
    interface ActivationPresenter<V extends ActivationView> extends BaseIvyPresenter<V> {

        void validateActivationKey(String key);

        void triggerIMEIActivation(String imei, String versionName, String versionNumber);

        void checkServerStatus(String url);

        void doActionForActivationDismiss();

        void doActivation(String activationKey, String imei, String versionName, String versionNumber);

        public List<ActivationBO> getAppUrls();
    }


}
