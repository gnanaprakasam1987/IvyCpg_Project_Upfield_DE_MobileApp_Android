package com.ivy.ui.dashboard.data;

import android.database.Cursor;

import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.lib.existing.DBUtil;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Observable;

public class SellerDashboardDataManagerImpl implements SellerDashboardDataManager{

    private DBUtil mDbUtil;

    private AppDataProvider appDataProvider;

    @Inject
    public SellerDashboardDataManagerImpl(@DataBaseInfo DBUtil dbUtil, AppDataProvider appDataProvider) {
        mDbUtil = dbUtil;
        this.appDataProvider = appDataProvider;
    }

    private void initDb() {
        mDbUtil.createDataBase();
        if(mDbUtil.isDbNullOrClosed())
            mDbUtil.openDataBase();
    }

    private void shutDownDb(){
        mDbUtil.closeDB();
    }


    @Override
    public Observable<ArrayList<String>> getRouteDashList() {
        return Observable.fromCallable(new Callable<ArrayList<String>>() {
            @Override
            public ArrayList<String> call() throws Exception {
                ArrayList<String> routeList = new ArrayList<>();

                try{
                    initDb();

                    String sql;
                    sql = "select distinct interval from RouteKPI";
                    Cursor c = mDbUtil.selectSQL(sql);
                    if (c != null) {
                        while (c.moveToNext()) {
                            routeList.add(c.getString(0));
                        }

                        c.close();
                    }
                    mDbUtil.closeDB();

                }catch (Exception ignored){

                }

                shutDownDb();

                return routeList;
            }
        });
    }

    @Override
    public Observable<ArrayList<String>> getSellerDashList() {
        return Observable.fromCallable(new Callable<ArrayList<String>>() {
            @Override
            public ArrayList<String> call() throws Exception {
                ArrayList<String> dashList = new ArrayList<>();

                try{
                    initDb();

                    String sql;
                    sql = "select distinct interval from SellerKPI";
                    Cursor c = mDbUtil.selectSQL(sql);
                    if (c != null) {
                        while (c.moveToNext()) {
                            dashList.add(c.getString(0));
                        }

                        c.close();
                    }
                    mDbUtil.closeDB();

                }catch (Exception ignored){

                }

                shutDownDb();

                return dashList;
            }
        });
    }

    @Override
    public Observable<ArrayList<String>> getRetailerDashList() {
        return Observable.fromCallable(new Callable<ArrayList<String>>() {
            @Override
            public ArrayList<String> call() throws Exception {
                ArrayList<String> dashList = new ArrayList<>();

                try{
                    initDb();

                    String sql;
                    sql = "select distinct interval from RetailerKPI where RetailerId=" + appDataProvider.getRetailMaster().getRetailerID();
                    Cursor c = mDbUtil.selectSQL(sql);
                    if (c != null) {
                        while (c.moveToNext()) {
                            dashList.add(c.getString(0));
                        }

                        c.close();
                    }
                    mDbUtil.closeDB();

                }catch (Exception ignored){

                }

                shutDownDb();

                return dashList;
            }
        });
    }
}
