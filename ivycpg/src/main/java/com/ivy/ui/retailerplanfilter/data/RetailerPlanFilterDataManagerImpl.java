package com.ivy.ui.retailerplanfilter.data;

import android.database.Cursor;

import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.ui.offlineplan.addplan.DateWisePlanBo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Single;

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

    @Override
    public Single<ArrayList<String>> prepareConfigurationMaster() {
        return Single.fromCallable(new Callable<ArrayList<String>>() {
            @Override
            public ArrayList<String> call() throws Exception {
                ArrayList<String> listValues = new ArrayList<>();

                shutDownDb();

                return listValues;
            }
        });
    }
}
