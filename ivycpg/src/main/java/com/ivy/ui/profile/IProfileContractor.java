package com.ivy.ui.profile;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;

public interface IProfileContractor {

    interface IProfileView extends BaseIvyView {

    }

    interface IProfilePresenter<V extends IProfileView> extends BaseIvyPresenter<V> {

    }
}
