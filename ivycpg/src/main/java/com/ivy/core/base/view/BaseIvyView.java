package com.ivy.core.base.view;

import android.support.annotation.StringRes;

public interface BaseIvyView {

    void showLoading();

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
}
