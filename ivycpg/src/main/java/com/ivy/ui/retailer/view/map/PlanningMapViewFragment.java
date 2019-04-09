package com.ivy.ui.retailer.view.map;

import android.view.View;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.ui.offlineplan.OfflineBasePresenterImpl;
import com.ivy.ui.retailer.RetailerContract;
import com.ivy.ui.retailer.presenter.RetailerPresenterImpl;
import com.ivy.ui.retailer.view.map.di.DaggerMapViewComponent;
import com.ivy.ui.retailer.view.map.di.MapViewModule;

import java.util.ArrayList;
import java.util.Objects;

import javax.inject.Inject;

public class PlanningMapViewFragment extends BaseFragment implements RetailerContract.RetailerView {

    private String screenTitle;

    @Inject
    RetailerPresenterImpl<RetailerContract.RetailerView> presenter;

    @Override
    public void initializeDi() {
       /* DaggerMapViewComponent.builder()
                .ivyAppComponent(((BusinessModel) Objects.requireNonNull(getActivity()).getApplication()).getComponent())
                .mapViewModule(new MapViewModule(this, getActivity()))
                .build()
                .inject(PlanningMapViewFragment.this);*/

        setBasePresenter((BasePresenter) presenter);
    }

    @Override
    protected int setContentViewLayout() {
        return R.layout.fragment_map_view_planning;
    }

    @Override
    public void initVariables(View view) {

    }

    @Override
    protected void getMessageFromAliens() {
        if (getArguments() != null)
            screenTitle = getArguments().getString("screentitle");
    }

    @Override
    protected void setUpViews() {
        setUpToolbar(screenTitle);
        presenter.loadAllStoresData();
    }


}
