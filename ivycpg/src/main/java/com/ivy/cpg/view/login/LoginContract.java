package com.ivy.cpg.view.login;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.microsoft.azure.storage.blob.CloudBlobContainer;

import java.util.HashMap;

/**
 * Created by dharmapriya.k on 4/12/17.
 *
 */

public interface LoginContract {
    interface LoginPresenter {
        void setView(LoginContract.LoginBaseView loginView);

        void loadInitialData();

        void getSupportNo();

        void checkDB();

        void assignServerUrl();

        void checkLogin();

        void onLoginClick();

        void callInitialAuthentication(boolean isDeviceChanged);

        void applyLastSyncPref();

        void updateDownloadedTime();

        void applyPasswordLockCountPref();

        int getPasswordLockCount();

        void callUpdateFinish();

    }

    interface LoginBaseView {
        void showAlert(String msg, boolean isFinish);

        void showProgressDialog(String msg);

        void dismissAlertDialog();

        void showGPSDialog();

        void setAlertDialogMessage(String msg);

        void goToHomeScreen();

        void goToAttendance();

        void finishActivity();

        void reload();

        void requestLocation();

        void doLocalLogin();

        void goToDistributorSelection();

        void downloadImagesThreadStart(HashMap<String, String> imgUrls, TransferUtility transferUtility,HashMap<String, String> sfdcImgUrls);

        void downloadImagesThreadStartFromAzure(HashMap<String, String> imgUrls, CloudBlobContainer cloudBlobContainer,HashMap<String, String> sfdcImgUrls);
    }


    interface LoginView extends LoginBaseView{
        void showForgotPassword();

        void reload();

        void setSupportNoTV(String supportNo);

        void retrieveDBData();

        void goToChangePwd();

        void sendUserNotExistToHandler();

        void showDeviceLockedDialog();

        void resetPassword();

        void showAppUpdateAlert(String msg);



        void callResetPassword();

    }
}
