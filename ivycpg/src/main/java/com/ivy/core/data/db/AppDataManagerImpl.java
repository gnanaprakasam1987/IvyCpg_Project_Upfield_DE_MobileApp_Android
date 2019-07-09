package com.ivy.core.data.db;

import android.database.Cursor;

import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.data.sharedpreferences.SharedPreferenceHelper;
import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.functions.Function;

import static com.ivy.utils.StringUtils.getStringQueryParam;

public class AppDataManagerImpl implements AppDataManager {

    private DBUtil mDbUtil;

    private AppDataProvider appDataProvider;

    private ConfigurationMasterHelper configurationMasterHelper;

    private SharedPreferenceHelper mSharedPreferenceHelper;

    @Inject
    public AppDataManagerImpl(@DataBaseInfo DBUtil dbUtil, AppDataProvider appDataProvider, ConfigurationMasterHelper configurationMasterHelper, SharedPreferenceHelper sharedPreferenceHelper) {
        mDbUtil = dbUtil;
        this.appDataProvider = appDataProvider;
        this.configurationMasterHelper = configurationMasterHelper;
        this.mSharedPreferenceHelper = sharedPreferenceHelper;

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
                            + getStringQueryParam(appDataProvider.getRetailMaster().getRetailerID()) +
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
                                    + appDataProvider.getRetailMaster().getRetailerID() + " AND MENU_CODE = " + getStringQueryParam(moduleName));

                    if (c.getCount() == 0) {
                        String columns = "Retailerid,MENU_CODE";

                        String values = appDataProvider.getRetailMaster().getRetailerID() + ","
                                + getStringQueryParam(moduleName);

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
                                    + appDataProvider.getRetailMaster().getRetailerID() + " AND MENU_CODE = " + getStringQueryParam(menuName));

                    if (c.getCount() == 0) {
                        String columns = "Retailerid,MENU_CODE";

                        String values = appDataProvider.getRetailMaster().getRetailerID() + ","
                                + getStringQueryParam(menuName);

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


    /**
     * This method will download the Menu configured for this particular channel
     * type. This will also download the Menu Name,Number and hasLink attributes
     *
     * @return sd
     */
    @Override
    public Observable<ArrayList<ConfigureBO>> fetchNewActivityMenu(String menuName) {

        if (!configurationMasterHelper.IS_ATTRIBUTE_MENU) {
            return Observable.fromCallable(() -> {

                String language = mSharedPreferenceHelper.getPreferredLanguage();

                String sql = "";
                String sql1 = "";

                sql = "select hhtCode, flag, RField,MName,MNumber,hasLink,RField1 from "
                        + DataMembers.tbl_HhtMenuMaster
                        + " where hhtCode like 'MENU_%' and lang="
                        + getStringQueryParam(language)
                        + " and flag=1 and SubChannelId = "
                        + appDataProvider.getRetailMaster().getSubchannelid()
                        + " and AttributeId = 0 and MenuType="
                        + getStringQueryParam(menuName)
                        + " order by MNumber";

                sql1 = "select hhtCode, flag, RField,MName,MNumber,hasLink,RField1 from "
                        + DataMembers.tbl_HhtMenuMaster
                        + " where hhtCode like 'MENU_%' and lang="
                        + getStringQueryParam(language)
                        + " and flag=1 and SubChannelId =0 "
                        + " and AttributeId = 0 and MenuType="
                        + getStringQueryParam(menuName)
                        + " order by MNumber";

                ArrayList<ConfigureBO> activitymenuconfig = new ArrayList<>();
                getActivityMenuConfig(sql, sql1, activitymenuconfig);

                return activitymenuconfig;
            });
        }else {
            return Single.fromCallable(() -> {
                String retailerAttributeList = "0";
                try{
                    initDb();

                    StringBuilder sb = new StringBuilder();

                    sb.append("select EAM1.AttributeId,EAM2.AttributeId from EntityAttributeMaster EAM1 ");
                    sb.append("INNER JOIN EntityAttributeMaster EAM2 ON EAM1.ParentId = EAM2.AttributeId");
                    sb.append(" where EAM1.AttributeId in (select AttributeId from RetailerAttribute where RetailerId=" + appDataProvider.getRetailMaster().getRetailerID() + ")");
                    Cursor c = mDbUtil.selectSQL(sb.toString());

                    if (c != null) {
                        while (c.moveToNext()) {
                            for (int i = 0; i < c.getColumnCount(); i++) {
                                retailerAttributeList = retailerAttributeList + c.getString(i);
                                if (c.getColumnCount() > 1 && i != c.getColumnCount())
                                    retailerAttributeList = retailerAttributeList + ",";
                            }

                        }

                        c.close();
                    }

                }catch (Exception ignored){

                }

                if (retailerAttributeList.endsWith(","))
                    retailerAttributeList = retailerAttributeList.substring(0, retailerAttributeList.length() - 1);

                return retailerAttributeList;
            }).flatMapObservable((Function<String, ObservableSource<ArrayList<ConfigureBO>>>) retailerAttributeList -> Observable.fromCallable(new Callable<ArrayList<ConfigureBO>>() {
                @Override
                public ArrayList<ConfigureBO> call() throws Exception {
                    ArrayList<ConfigureBO> activitymenuconfig = new ArrayList<>();
                    String language = mSharedPreferenceHelper.getPreferredLanguage();

                    String sql = "";
                    String sql1 = "";
                    sql = "select hhtCode, flag, RField,MName,MNumber,hasLink,RField1 from "
                            + DataMembers.tbl_HhtMenuMaster
                            + " where hhtCode like 'MENU_%' and lang="
                            + getStringQueryParam(language)
                            + " and flag=1 and attributeId in (0, "
                            + retailerAttributeList
                            + ") and MenuType="
                            + getStringQueryParam(menuName)
                            + " order by MNumber";

                    getActivityMenuConfig(sql, sql1, activitymenuconfig);

                    return activitymenuconfig;
                }
            }));

        }



    }

    private void getActivityMenuConfig(String sql, String sql1, ArrayList<ConfigureBO> activitymenuconfig) {
        try{
            initDb();
            configurationMasterHelper.IS_ORDER_STOCK = false;

            Cursor c = mDbUtil.selectSQL(sql);
            if (c != null) {
                if (c.getCount() == 0) {
                    c = mDbUtil.selectSQL(sql1);
                }
            }

            ConfigureBO con;
            if (c != null) {
                while (c.moveToNext()) {
                    con = new ConfigureBO();
                    con.setConfigCode(c.getString(0));
                    con.setFlag(c.getInt(1));
                    con.setModule_Order(c.getInt(2));
                    con.setMenuName(c.getString(3));
                    con.setMenuNumber(c.getString(4));
                    con.setHasLink(c.getInt(5));
                    con.setMandatory(c.getInt(6));
                    activitymenuconfig.add(con);

                    if (c.getString(0).equals("MENU_STK_ORD"))
                        configurationMasterHelper.IS_ORDER_STOCK = true;

                }
                c.close();
            }


        }catch (Exception ignored){

        }

        shutDownDb();
    }


    /**
     * This method is used to check if the Day is closed or not. Day will be
     * closed from sync Screen. By default false. If the day is closed then its
     * not possible to preform any operation.
     *
     * @return true is day closed or false
     */
    @Override
    public Single<Boolean> isDayClosed() {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {

                int i = 0;
                try {
                    initDb();
                    Cursor c = mDbUtil.selectSQL("select  status  from DayClose");
                    if (c != null) {
                        if (c.moveToNext()) {
                            i = c.getInt(0);
                        }
                        c.close();
                    }

                } catch (Exception e) {
                    Commons.printException("" + e);
                }
                shutDownDb();
                return i == 1;

            }
        });
    }


    /**
     * This method will return RFiled6 column value from the HHTMenuMaster table.
     *
     * @return boolean true - survey is required.
     */
    @Override
    public Single<Boolean> isFloatingSurveyEnabled(String moduleCode) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try{
                    String sql = "select RField6 from " + DataMembers.tbl_HhtMenuMaster
                            + " where hhtCode=" + getStringQueryParam(moduleCode);

                    initDb();

                    Cursor c = mDbUtil.selectSQL(sql);
                    if (c != null && c.getCount() != 0) {
                        while (c.moveToNext()) {
                            if (c.getInt(0) == 1) {
                                return true;
                            }

                        }
                        c.close();
                    }
                    shutDownDb();

                }catch (Exception ignored){
                    return false;
                }
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
