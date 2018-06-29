package com.ivy.core.base.presenter;

import com.ivy.core.base.view.BaseIvyView;

public interface BaseIvyPresenter<V extends BaseIvyView> {

    public void onCreate();

    void onPause();

    void onResume();

    void onDetach();

    void getAppTheme();

    void getAppFontSize();

    boolean isNFCConfigurationEnabled();

    boolean isLocationConfigurationEnabled();

}
