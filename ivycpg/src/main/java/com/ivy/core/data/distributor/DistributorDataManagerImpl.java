package com.ivy.core.data.distributor;

import android.database.Cursor;

import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.cpg.primarysale.bo.DistributorMasterBO;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Observable;

public class DistributorDataManagerImpl implements DistributorDataManager {


    private DBUtil mDbUtil;

    private AppDataProvider appDataProvider;

    @Inject
    public DistributorDataManagerImpl(@DataBaseInfo DBUtil dbUtil, AppDataProvider appDataProvider) {
        this.mDbUtil = dbUtil;
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
    public Observable<ArrayList<DistributorMasterBO>> fetchDistributorList() {
        return Observable.fromCallable(new Callable<ArrayList<DistributorMasterBO>>() {
            @Override
            public ArrayList<DistributorMasterBO> call() throws Exception {
                ArrayList<DistributorMasterBO> distributorMasterBOS = new ArrayList<>();
                try {
                    initDb();
                    String sql = "select DISTINCT DId,DName, ParentID, IFNULL(GroupId,'') as GroupId from "
                            + DataMembers.tbl_DistributorMaster + " LEFT JOIN DistributorPriceMapping ON Did = DistId";

                    Cursor c = mDbUtil.selectSQL(sql);

                    DistributorMasterBO con;
                    if (c != null) {
                        while (c.moveToNext()) {
                            con = new DistributorMasterBO();//DId,DName,CNumber,Address1,Address2,Address3,Type,TinNo
                            con.setDId(c.getString(c.getColumnIndex("DId")));
                            con.setDName(c.getString(c.getColumnIndex("DName")));
                            con.setParentID(c.getString(c.getColumnIndex("ParentID")));

                            con.setGroupId(c.getString(c.getColumnIndex("GroupId")));
                            distributorMasterBOS.add(con);

                        }
                        c.close();
                    }


                } catch (Exception ignored) {

                }

                shutDownDb();
                return distributorMasterBOS;
            }
        });
    }


    @Override
    public void tearDown() {

    }

}
