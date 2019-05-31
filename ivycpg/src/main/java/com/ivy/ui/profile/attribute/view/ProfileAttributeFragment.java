package com.ivy.ui.profile.attribute.view;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.ui.profile.attribute.IProfileAttributeContract;
import com.ivy.ui.profile.attribute.di.DaggerProfileAttributeComponent;
import com.ivy.ui.profile.attribute.di.ProfileAttributeModule;

import javax.inject.Inject;

public class ProfileAttributeFragment extends BaseFragment
        implements IProfileAttributeContract.IProfileAttributeView {

    @Inject
    IProfileAttributeContract.IProfileAttributePresenter<IProfileAttributeContract.IProfileAttributeView> profileAttributePresenter;

    @Override
    public void initializeDi() {
        DaggerProfileAttributeComponent.builder()
                .ivyAppComponent(((BusinessModel) ((Activity)getContext()).getApplication()).getComponent())
                .profileAttributeModule(new ProfileAttributeModule(this))
                .build().inject(this);
        setBasePresenter((BasePresenter) profileAttributePresenter);
    }

    @Override
    protected int setContentViewLayout() {
        return R.layout.profile_attribute_layout;
    }

    @Override
    public void init(View view) {

    }

    @Override
    protected void getMessageFromAliens() {

    }

    @Override
    protected void setUpViews() {

    }

    @Override
    public void displayAttributeSpinner() {

    }
}
