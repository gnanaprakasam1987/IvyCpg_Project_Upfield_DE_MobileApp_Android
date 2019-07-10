package com.ivy.core.base.view;

import androidx.annotation.StringRes;

import com.ivy.sd.png.util.CommonDialog;

public interface BaseIvyView {

    void showLoading();

    void showLoading(String message);

    void showLoading(int strinRes);

    void hideLoading();

    void onError(@StringRes int resId);

    void onError(String message);

    void showMessage(String message);

    void showMessage(@StringRes int resId);

    boolean isNetworkConnected();

    void hideKeyboard();

    void setLayoutDirection(int direction);

    void handleLayoutDirection(String language);

    void setBlueTheme();

    void setRedTheme();

    void setOrangeTheme();

    void setGreenTheme();

    void setPinkTheme();

    void setNavyBlueTheme();

    void setFontSize(String fontSize);

    void showAlert(String title,String msg);

    void showAlert(String title, String msg, CommonDialog.PositiveClickListener positiveClickListener);

    void showAlert(String title, String msg, CommonDialog.PositiveClickListener positiveClickListener, CommonDialog.negativeOnClickListener negativeOnClickListener);

    void showAlert(String title, String msg, CommonDialog.PositiveClickListener positiveClickListener,boolean isCancelable);

    void createNFCManager();

    void resumeNFCManager();

    void pauseNFCManager();

    void setScreenTitle(String title);

    void setUpToolbar(String title);

    void getLocationPermission();

    void getPhoneStatePermission();

    void getCameraPermission();
}
