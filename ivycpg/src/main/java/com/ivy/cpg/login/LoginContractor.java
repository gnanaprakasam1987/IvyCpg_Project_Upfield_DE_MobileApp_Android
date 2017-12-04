package com.ivy.cpg.login;

/**
 * Created by ivyuser on 4/12/17.
 */

public interface LoginContractor {
    interface LoginPresenter {
        void setView(LoginView loginView);

        void loadInitialData();

        void getSupportNo();

        void checkDB();

        void copyAssetsProfile();

        void assignServerUrl();

        void checkLogin();

        void onLoginClick();

        void callAuthentication(boolean isDeviceChanged);
    }

    interface LoginView {
        void showForgotPassword();

        void reload();

        void setSupportNoTV(String supportNo);

        void retrieveDBData();

        void showAlert(String msg, boolean isFinish);

        void showProgressDialog(String msg);

        void dismissProgressDialog();

        void onCreateDialog();

        void requestLocation();

        void goToChangePwd();

        void goToHomeScreen();

        void goToAttendance();

        void setAlertDialogMessage(String msg);

        void enableGPSDialog();

        void sendMessageToHandler(int msg);

        void threadActions(int action);

        void showDialog();
    }
}
