package com.ivy.ui.retailer.view.list;

import android.view.View;

import com.google.android.gms.maps.model.MarkerOptions;
import com.ivy.core.base.view.BaseFragment;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.ui.retailer.RetailerContract;

import java.util.List;

public class RetailerListFragment extends BaseFragment implements RetailerContract.RetailerView {
    @Override
    public void initializeDi() {

    }

    @Override
    protected int setContentViewLayout() {
        return 0;
    }

    @Override
    public void initVariables(View view) {

    }

    @Override
    protected void getMessageFromAliens() {

    }

    @Override
    protected void setUpViews() {

    }

    @Override
    public void populateRetailers(List<RetailerMasterBO> retailerList) {

    }

    @Override
    public void populateTodayPlannedRetailers(RetailerMasterBO retailerList) {

    }

    @Override
    public void populatePlannedRetailers(List<RetailerMasterBO> plannedRetailers) {

    }

    @Override
    public void populateUnPlannedRetailers(List<RetailerMasterBO> unPlannedRetailers) {

    }

    @Override
    public void populateCompletedRetailers(List<RetailerMasterBO> unPlannedRetailers) {

    }

    @Override
    public void drawRoutePath(String path) {

    }

    @Override
    public void focusMarker() {

    }
}
