package com.ivy.view.activation;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.core.di.scope.PerActivity;

public interface ActivationContract {

    interface ActivationView extends BaseIvyView{

    }

    @PerActivity
    interface ActivationPresenter<V extends ActivationView> extends BaseIvyPresenter<V>{

    }


}
