package com.ivy.core.data.db;

import android.database.Cursor;

import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.IndicativeBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;

import static com.ivy.sd.png.provider.ConfigurationMasterHelper.CODE_SHOW_ALL_ROUTE_FILTER;
import static com.ivy.utils.AppUtils.QT;

public class AppDataManagerImpl implements AppDataManager {

    private DBUtil mDbUtil;

    private AppDataProvider appDataProvider;

    private ConfigurationMasterHelper configurationMasterHelper;

    @Inject
    public AppDataManagerImpl(@DataBaseInfo DBUtil dbUtil, AppDataProvider appDataProvider, ConfigurationMasterHelper configurationMasterHelper) {
        mDbUtil = dbUtil;
        this.appDataProvider = appDataProvider;
        this.configurationMasterHelper = configurationMasterHelper;

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
    public Single<Boolean> saveModuleCompletion(final String menuName) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    initDb();
                    Cursor c = mDbUtil
                            .selectSQL("SELECT * FROM ModuleCompletionReport WHERE RetailerId="
                                    + appDataProvider.getRetailMaster().getRetailerID() + " AND MENU_CODE = " + QT(menuName));

                    if (c.getCount() == 0) {
                        String columns = "Retailerid,MENU_CODE";

                        String values = appDataProvider.getRetailMaster().getRetailerID() + ","
                                + QT(menuName);

                        mDbUtil.insertSQL("ModuleCompletionReport", columns, values);

                    }
                    c.close();
                    shutDownDb();
                    return true;
                } catch (Exception e) {
                    shutDownDb();
                    return false;
                }


            }
        });
    }



    @Override
    public void tearDown() {
        if (mDbUtil != null)
            mDbUtil.closeDB();
    }
}