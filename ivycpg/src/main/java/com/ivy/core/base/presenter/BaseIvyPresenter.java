package com.ivy.core.base.presenter;

import com.ivy.core.base.view.BaseIvyView;

public interface BaseIvyPresenter<V extends BaseIvyView> {

    void onAttach(V mvpView);

    void clearDisposable();

    void handleLayoutDirections();

    void onDetach();

    void getAppTheme();

    void getAppFontSize();

    boolean isNFCConfigurationEnabled();

    boolean isLocationConfigurationEnabled();

}
