package com.ivy.core.base.presenter;

import com.ivy.core.base.view.BaseIvyView;

public interface BaseIvyPresenter<V extends BaseIvyView> {

    void onAttach(V mvpView);

    void onDetach();

    void getAppTheme();

    void getAppFontSize();

    boolean getShowNFCValidation();

    boolean getLocationConfiguration();

}
