package com.ivy.ui.activation.view;

import android.widget.EditText;

import com.ivy.core.base.view.BaseActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.ui.activation.ActivationContract;
import com.ivy.ui.activation.di.ActivationModule;
import com.ivy.ui.activation.di.DaggerActivationComponent;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ActivationActivity extends BaseActivity implements ActivationContract.ActivationView {

    @Inject
    ActivationContract.ActivationPresenter<ActivationContract.ActivationView> mActivationPresenter;

    @BindView(R.id.activationKey)
    EditText mActivationKeyEdt;

    @Override
    public int getLayoutId() {
        return R.layout.fragment_screen_activation;
    }

    @Override
    protected void initVariables() {
    }

    @Override
    public void initializeDi() {
        DaggerActivationComponent.builder()
                .activationModule(new ActivationModule())
                .ivyAppComponent(((BusinessModel) getApplication()).getComponent())
                .build()
                .inject(this);

        mActivationPresenter.onAttach(this);

        setUnBinder(ButterKnife.bind(this));


    }

    @Override
    protected void getMessageFromAliens() {

    }

    @Override
    protected void setUpViews() {

    }

    @Override
    public void showActivationEmptyError() {

    }

    @Override
    public void showInvalidActivationError() {

    }

    @Override
    public void navigateToLoginScreen() {

    }
}
