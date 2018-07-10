package com.ivy.core.base.presenter;

import com.ivy.core.base.view.BaseIvyView;

public interface BaseIvyPresenter<V extends BaseIvyView> {

    void onCreate();

    void onPause();

    void onResume();

    void onDetach();

    boolean isNFCConfigurationEnabled();

    boolean isLocationConfigurationEnabled();

}
