package com.ivy.ui.retailerplanfilter.data;

import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.lib.existing.DBUtil;

import javax.inject.Inject;

public class RetailerPlanFilterDataManagerImpl implements RetailerPlanFilterDataManager {

    private DBUtil mDbUtil;

    @Inject
    RetailerPlanFilterDataManagerImpl(@DataBaseInfo DBUtil dbUtil){
        this.mDbUtil = dbUtil;
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
}
