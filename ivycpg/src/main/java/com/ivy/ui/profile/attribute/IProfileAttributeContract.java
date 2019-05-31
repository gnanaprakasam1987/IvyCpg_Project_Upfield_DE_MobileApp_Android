package com.ivy.ui.profile.attribute;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;

public interface IProfileAttributeContract {

    interface IProfileAttributeView extends BaseIvyView{

        void displayAttributeSpinner();
    }

    interface IProfileAttributePresenter<V extends IProfileAttributeView> extends BaseIvyPresenter<V>{

    }
}
