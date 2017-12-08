package com.ivy.cpg.view.login;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by dharmapriya.k on 4/12/17.
 *
 */

public interface LoginContractor {
    interface LoginPresenter {
        void setView(LoginView loginView);

        void loadInitialData();

        void getSupportNo();

        void checkDB();

        void assignServerUrl();

        void checkLogin();

        void onLoginClick();

        void callAuthentication(boolean isDeviceChanged);

        void deleteTables(boolean isDownloaded);

        void applyLastSyncPref();

        void applyPasswordLockCountPref();

        int getPasswordLockCount();

        void callUpdateFinish();
    }

    interface LoginView {
        void showForgotPassword();

        void reload();

        void setSupportNoTV(String supportNo);

        void retrieveDBData();

        void showAlert(String msg, boolean isFinish);

        void showProgressDialog(String msg);

        void dismissAlertDialog();

        void showGPSDialog();

        void requestLocation();

        void goToChangePwd();

        void goToHomeScreen();

        void goToAttendance();

        void setAlertDialogMessage(String msg);

        void sendUserNotExistToHandler();

        void threadActions();

        void showDialog();

        void resetPassword();

        void showAppUpdateAlert(String msg);

        void goToDistributorSelection();

        void downloadImagesThreadStart(HashMap<String, String> imgUrls, TransferUtility transferUtility);

        void finishActivity();

        void callCatalogImageDownload(ArrayList<S3ObjectSummary> imgUrls, TransferUtility transferUtility);

        void callResetPassword();

    }
}
