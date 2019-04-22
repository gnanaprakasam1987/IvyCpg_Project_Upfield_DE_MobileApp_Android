package com.ivy.ui.offlineplan.addplan.data;

import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.RetailerMasterBO;

import javax.inject.Inject;

public class AddPlanDataManagerImpl implements AddPlanDataManager {

    private DBUtil mDbUtil;

    @Inject
    AddPlanDataManagerImpl(@DataBaseInfo DBUtil dbUtil){
        mDbUtil = dbUtil;
    }


    private void initDb() {
        mDbUtil.createDataBase();
        if (mDbUtil.isDbNullOrClosed())
            mDbUtil.openDataBase();
    }

    private void shutDownDb() {
        mDbUtil.closeDB();
    }


    @Override
    public void tearDown() {
        shutDownDb();
    }

    @Override
    public void savePlan(RetailerMasterBO retailerMasterBO) {

    }

    @Override
    public void updatePlan(RetailerMasterBO retailerMasterBO) {

    }

    @Override
    public void cancelPlan(RetailerMasterBO retailerMasterBO) {

    }

    @Override
    public void DeletePlan(RetailerMasterBO retailerMasterBO) {

    }
}
