package com.ivy.ui.retailer.presenter;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.ui.retailer.RetailerContract;

public class RetailerPresenterImpl<V extends RetailerContract.RetailerView> extends BasePresenter<V> implements RetailerContract.RetailerPresenter<V> {
    @Override
    public void fetchRetailerList() {

    }

    @Override
    public void addRetailerToPlan(RetailerMasterBO retailerMasterBO, String startDate, String endDate) {

    }

    @Override
    public void updateRetailerToPlan(RetailerMasterBO retailerMasterBO, String startDate, String endDate) {

    }

    @Override
    public void deleteRetailerFromPlan(RetailerMasterBO retailerMasterBO) {

    }
}
