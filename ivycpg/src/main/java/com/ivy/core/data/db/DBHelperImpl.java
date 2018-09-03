package com.ivy.core.data.db;

import android.database.Cursor;

import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Single;

import static com.ivy.utils.AppUtils.QT;

public class DBHelperImpl implements DbHelper {

    private DBUtil mDbUtil;

    private AppDataProvider appDataProvider;

    @Inject
    public DBHelperImpl(@DataBaseInfo DBUtil dbUtil, AppDataProvider appDataProvider) {
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
    public Single<String> getThemeColor() {
        return Single.fromCallable(new Callable<String>() {
            @Override
            public String call() {
                String theme = "blue";
                try {
                    initDb();


                    String query = "select RField from HhtModuleMaster where hhtcode='THEME01' and flag=1 and  ForSwitchSeller = 0";
                    Cursor c = mDbUtil.selectSQL(query);
                    if (c.getCount() > 0) {
                        while (c.moveToNext()) {
                            theme = c.getString(0);
                        }
                    }
                    mDbUtil.close();
                } catch (Exception e) {
                    Commons.printException("" + e);
                }

                shutDownDb();
                return theme;
            }
        });


    }

    @Override
    public Single<String> getFontSize() {
        return Single.fromCallable(new Callable<String>() {
            @Override
            public String call() {
                String fontSize = "Small";
                try {
                    initDb();

                    String query = "select RField from HhtModuleMaster where hhtcode='THEME02' and flag=1 and  ForSwitchSeller = 0";
                    Cursor c = mDbUtil.selectSQL(query);
                    if (c.getCount() > 0) {
                        while (c.moveToNext()) {
                            fontSize = c.getString(0);

                        }
                    }
                    c.close();
                } catch (Exception e) {
                    Commons.printException("" + e);
                }

                shutDownDb();
                return fontSize;
            }
        });

    }

    @Override
    public Single<Double> getOrderValue() {
        return Single.fromCallable(new Callable<Double>() {
            @Override
            public Double call() {
                try {
                    initDb();

                    Cursor c = mDbUtil.selectSQL("select sum(ordervalue)from "
                            + DataMembers.tbl_orderHeader + " where retailerid="
                            + QT(appDataProvider.getRetailMaster().getRetailerID()) +
                            " AND upload='N'");
                    if (c != null) {
                        if (c.moveToNext()) {
                            double i = c.getDouble(0);
                            c.close();
                            return i;
                        }
                    }

                } catch (Exception e) {
                    Commons.printException("" + e);
                }

                shutDownDb();
                return 0.0;
            }
        });
    }

    @Override
    public Single<Boolean> updateModuleTime(final String moduleName) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                try {
                    initDb();

                    Cursor c = mDbUtil
                            .selectSQL("SELECT * FROM ModuleCompletionReport WHERE RetailerId="
                                    + appDataProvider.getRetailMaster().getRetailerID() + " AND MENU_CODE = " + QT(moduleName));

                    if (c.getCount() == 0) {
                        String columns = "Retailerid,MENU_CODE";

                        String values = appDataProvider.getRetailMaster().getRetailerID() + ","
                                + QT(moduleName);

                        mDbUtil.insertSQL("ModuleCompletionReport", columns, values);

                    }
                    c.close();

                    shutDownDb();
                    return true;
                } catch (Exception e) {
                    Commons.printException("" + e);
                }
                shutDownDb();

                return false;
            }
        });
    }


    @Override
    public void tearDown() {
        if (mDbUtil != null)
            mDbUtil.closeDB();
    }
}
