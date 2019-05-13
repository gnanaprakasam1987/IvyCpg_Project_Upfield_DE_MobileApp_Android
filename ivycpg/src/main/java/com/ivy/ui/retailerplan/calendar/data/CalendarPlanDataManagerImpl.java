package com.ivy.ui.retailerplan.calendar.data;

import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.lib.existing.DBUtil;

import javax.inject.Inject;

/**
 * Created by mansoor on 27/03/2019
 */
public class CalendarPlanDataManagerImpl implements CalendarPlanDataManager {
    private DBUtil mDbUtil;

    private AppDataProvider appDataProvider;

    @Inject
    CalendarPlanDataManagerImpl(@DataBaseInfo DBUtil dbUtil, AppDataProvider appDataProvider) {
        mDbUtil = dbUtil;
        this.appDataProvider = appDataProvider;
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
