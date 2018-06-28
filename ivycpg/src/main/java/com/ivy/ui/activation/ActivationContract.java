package com.ivy.ui.activation;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.sd.png.bo.ActivationBO;

import java.util.List;

public interface ActivationContract {

    interface ActivationView extends BaseIvyView{

        void showActivationEmptyError();

        void showInvalidActivationError();

        void navigateToLoginScreen();

        void showActivationError(String activationError);

        void showAppUrlIsEmptyError();


        void showServerError();

        void showPreviousActivationError();

        void showActivationDialog();

        void showTryValidKeyError();

        void doValidationSuccess();

        void showActivationFailedError();

        void showSuccessfullyActivatedAlert();


        void showConfigureUrlMessage();

        void showContactAdminMessage();

    }

    @PerActivity
    interface ActivationPresenter<V extends ActivationView> extends BaseIvyPresenter<V>{

         void validateActivationKey(String key);

         void triggerIMEIActivation(String imei, String versionName,String versionNumber);

         void checkServerStatus(String url);

        void doActionForActivationDismiss();

        void doActivation(String activationKey, String imei, String versionName,String versionNumber);

        public List<ActivationBO> getAppUrls();
    }


}
