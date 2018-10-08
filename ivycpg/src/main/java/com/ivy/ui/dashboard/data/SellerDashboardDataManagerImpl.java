package com.ivy.ui.dashboard.data;

import android.database.Cursor;

import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.cpg.view.dashboard.DashBoardBO;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.ui.dashboard.SellerDashboardConstants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.functions.Function;

import static com.ivy.cpg.view.dashboard.DashBoardHelper.MONTH_NAME;
import static com.ivy.ui.dashboard.SellerDashboardConstants.P3M;
import static com.ivy.ui.dashboard.SellerDashboardConstants.WEEK;
import static com.ivy.utils.AppUtils.QT;

public class SellerDashboardDataManagerImpl implements SellerDashboardDataManager {

    private DBUtil mDbUtil;

    private AppDataProvider appDataProvider;

    private ConfigurationMasterHelper configurationMasterHelper;

    private int currentmonthindex = 0;

    @Inject
    public SellerDashboardDataManagerImpl(@DataBaseInfo DBUtil dbUtil, AppDataProvider appDataProvider, ConfigurationMasterHelper configurationMasterHelper) {
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
    public Observable<ArrayList<String>> getDashList(final SellerDashboardConstants.DashBoardType dashBoardType) {
        return Observable.fromCallable(new Callable<ArrayList<String>>() {
            @Override
            public ArrayList<String> call() throws Exception {
                ArrayList<String> dashList = new ArrayList<>();

                try {
                    initDb();

                    String sql = "";

                    switch (dashBoardType) {
                        case ROUTE:
                            sql = "select distinct interval from RouteKPI";
                            break;
                        case RETAILER:
                            sql = "select distinct interval from RetailerKPI where RetailerId=" + appDataProvider.getRetailMaster().getRetailerID();
                            break;
                        case SELLER:
                            sql = "select distinct interval from SellerKPI";
                            break;
                    }


                    Cursor c = mDbUtil.selectSQL(sql);
                    if (c != null) {
                        while (c.moveToNext()) {
                            dashList.add(c.getString(0));
                        }

                        c.close();
                    }
                    mDbUtil.closeDB();

                } catch (Exception ignored) {

                }

                shutDownDb();

                return dashList;
            }
        });
    }

    @Override
    public Observable<ArrayList<DashBoardBO>> getP3MSellerDashboardData(final String userId) {
        return Observable.fromCallable(new Callable<ArrayList<DashBoardBO>>() {
            @Override
            public ArrayList<DashBoardBO> call() throws Exception {

                ArrayList<DashBoardBO> dashBoardBOS = new ArrayList<>();
                try {
                    initDb();

                    String monthText = "";
                    if (configurationMasterHelper.IS_KPI_CALENDAR) {
                        monthText = "SK.IntervalDesc";
                    } else {
                        monthText = "IFNULL(strftime('%m', replace(fromdate,'/','-')),0)";
                    }

                    String sql = "SELECT SLM.ListName,SKD.Target,SKD.Achievement,"
                            + " ROUND(CASE WHEN (100-((SKD.Achievement*100)/((SKD.Target)*1.0))) < 0"
                            + " THEN 100 ELSE ((SKD.Achievement*100)/((SKD.Target)*1.0)) END ,2) AS conv_ach_perc"
                            + ",IFNULL(SKS.Score,0),IFNULL(SKS.Incentive,0),SK.KPIID,SKD.KPIParamLovId,SLM.Flex1,SLM.ListCode," + monthText + ",SKD.Flex1 AS kpiFlex1 FROM SellerKPI SK"
                            + " inner join SellerKPIDetail SKD on SKD.KPIID= SK.KPIID"
                            + " LEFT join SellerKPIScore SKS on SKD.KPIID= SKS.KPIID and SKD.KPIParamLovId = SKS.KPIParamLovId"
                            + " inner join StandardListMaster SLM on SLM.Listid=SKD.KPIParamLovId"
                            + " where userid = "
                            + QT(userId)
                            + " and interval= 'P3M' "
                            + (userId.equals("0") ? " and SK.isSummary=1" : "")
                            + " order by DisplaySeq asc";
                    Cursor c = mDbUtil.selectSQL(sql);
                    if (c != null) {
                        while (c.moveToNext()) {
                            DashBoardBO sbo = new DashBoardBO();
                            sbo.setPId(0);
                            sbo.setText(c.getString(0));
                            sbo.setKpiTarget(c.getString(1));
                            sbo.setKpiAcheived(c.getString(2));


                            sbo.setCalculatedPercentage(c.getFloat(3));
                            if (sbo.getCalculatedPercentage() >= 100) {
                                sbo.setConvTargetPercentage(0);
                                sbo.setConvAcheivedPercentage(100);
                            } else {
                                sbo.setConvTargetPercentage(100 - sbo
                                        .getCalculatedPercentage());
                                sbo.setConvAcheivedPercentage(sbo
                                        .getCalculatedPercentage());
                            }
                            sbo.setKpiScore(c.getString(4));
                            sbo.setKpiIncentive(c.getString(5));
                            sbo.setKpiID(c.getInt(6));
                            sbo.setKpiTypeLovID(c.getInt(7));
                            sbo.setFlex1(c.getInt(8));
                            sbo.setCode(c.getString(9));
                            sbo.setKpiFlex(c.getString(c.getColumnIndex("kpiFlex1")));
                            if (configurationMasterHelper.IS_KPI_CALENDAR) {
                                sbo.setMonthName(c.getString(10));
                            } else {
                                int value = SDUtil.convertToInt(c.getString(10));
                                if (value > 0 && value <= 12)
                                    sbo.setMonthName(MONTH_NAME[value - 1]);
                            }


                            String lovIdSql = "select count(*) from SellerKPISKUDetail where KPIParamLovId = " + sbo.getKpiTypeLovID();
                            Cursor lovIdSqlCursor = mDbUtil.selectSQL(lovIdSql);
                            if (lovIdSqlCursor != null) {
                                while (lovIdSqlCursor.moveToNext()) {
                                    sbo.setSubDataCount(c.getInt(0));
                                }
                                lovIdSqlCursor.close();
                            }
                            dashBoardBOS.add(sbo);
                        }
                        c.close();
                    }

                } catch (Exception ignored) {

                }
                shutDownDb();
                return dashBoardBOS;
            }
        });
    }

    @Override
    public Observable<ArrayList<DashBoardBO>> getSellerDashboardForWeek(final String userId) {
        return Observable.fromCallable(new Callable<ArrayList<DashBoardBO>>() {
            @Override
            public ArrayList<DashBoardBO> call() throws Exception {

                ArrayList<DashBoardBO> dashBoardBOS = new ArrayList<>();
                try {
                    initDb();
                    String sql = "SELECT SLM.ListName,SKD.Target,ifnull(SKD.Achievement,0),"
                            + " ROUND(CASE WHEN (100-((SKD.Achievement*100)/((SKD.Target)*1.0))) < 0"
                            + " THEN 100 ELSE ((SKD.Achievement*100)/((SKD.Target)*1.0)) END ,2) AS conv_ach_perc"
                            + ",IFNULL(SKS.Score,0),IFNULL(SKS.Incentive,0),SK.KPIID,SKD.KPIParamLovId,SLM.Flex1,count(SKDD.KPIParamLovId),SLM.ListCode,SK.IntervalDesc,SKD.Flex1 AS kpiFlex1 FROM SellerKPI SK"
                            + " inner join SellerKPIDetail SKD on SKD.KPIID= SK.KPIID"
                            + " LEFT join SellerKPIScore SKS on SKD.KPIID= SKS.KPIID and SKD.KPIParamLovId = SKS.KPIParamLovId"
                            + " inner join StandardListMaster SLM on SLM.Listid=SKD.KPIParamLovId"
                            + " LEFT join SellerKPISKUDetail skdd on skdd.KPIParamLovId =SKD.KPIParamLovId "
                            + " where userid = "
                            + QT(userId)
                            + " and interval= 'WEEK'"
                            + " group by SLM.Listid,SK.IntervalDesc order by DisplaySeq asc";
                    Cursor c = mDbUtil.selectSQL(sql);
                    if (c != null) {
                        while (c.moveToNext()) {
                            DashBoardBO sbo = new DashBoardBO();
                            sbo.setPId(0);
                            sbo.setText(c.getString(0));
                            sbo.setKpiTarget(c.getString(1));
                            sbo.setKpiAcheived(c.getString(2));


                            sbo.setCalculatedPercentage(c.getFloat(3));
                            if (sbo.getCalculatedPercentage() >= 100) {
                                sbo.setConvTargetPercentage(0);
                                sbo.setConvAcheivedPercentage(100);
                            } else {
                                sbo.setConvTargetPercentage(100 - sbo
                                        .getCalculatedPercentage());
                                sbo.setConvAcheivedPercentage(sbo
                                        .getCalculatedPercentage());
                            }
                            sbo.setKpiScore(c.getString(4));
                            sbo.setKpiIncentive(c.getString(5));
                            sbo.setKpiID(c.getInt(6));
                            sbo.setKpiTypeLovID(c.getInt(7));
                            sbo.setFlex1(c.getInt(8));
                            sbo.setSubDataCount(c.getInt(9));
                            sbo.setCode(c.getString(10));
                            sbo.setKpiFlex(c.getString(c.getColumnIndex("kpiFlex1")));
                            sbo.setMonthName(c.getString(11));
                            dashBoardBOS.add(sbo);
                        }
                        c.close();
                    }

                } catch (Exception ignored) {

                }
                shutDownDb();
                return dashBoardBOS;
            }
        });
    }

    @Override
    public Observable<ArrayList<DashBoardBO>> getSellerDashboardForInterval(final String userId, final String interval) {
        return Observable.fromCallable(new Callable<ArrayList<DashBoardBO>>() {
            @Override
            public ArrayList<DashBoardBO> call() throws Exception {
                ArrayList<DashBoardBO> dashBoardBOS = new ArrayList<>();
                try {

                    initDb();

                    String sql = "SELECT SLM.ListName,SKD.Target,ifnull(SKD.Achievement,0),"
                            + " ROUND(CASE WHEN (100-((SKD.Achievement*100)/((SKD.Target)*1.0))) < 0"
                            + " THEN 100 ELSE ((SKD.Achievement*100)/((SKD.Target)*1.0)) END ,2) AS conv_ach_perc"
                            + ",IFNULL(SKS.Score,0),IFNULL(SKS.Incentive,0),SK.KPIID,SKD.KPIParamLovId,SLM.Flex1,count(SKDD.KPIParamLovId),SLM.ListCode,IFNULL(strftime('%m', replace(fromdate,'/','-')),0),SKD.Flex1 AS kpiFlex1 FROM SellerKPI SK"
                            + " inner join SellerKPIDetail SKD on SKD.KPIID= SK.KPIID"
                            + " LEFT join SellerKPIScore SKS on SKD.KPIID= SKS.KPIID and SKD.KPIParamLovId = SKS.KPIParamLovId"
                            + " inner join StandardListMaster SLM on SLM.Listid=SKD.KPIParamLovId"
                            + " LEFT join SellerKPISKUDetail skdd on skdd.KPIParamLovId =SKD.KPIParamLovId "
                            + " where userid = "
                            + QT(userId)
                            + " and interval= "
                            + QT(interval)
                            + " AND "
                            + QT(SDUtil.now(SDUtil.DATE_GLOBAL))
                            + " between SK.fromdate and SK.todate "
                            + (userId.equals("0") ? " and SK.isSummary=1" : "")
                            + " group by SLM.Listid order by DisplaySeq asc";
                    Cursor c = mDbUtil.selectSQL(sql);
                    if (c != null) {
                        while (c.moveToNext()) {
                            DashBoardBO sbo = new DashBoardBO();
                            sbo.setPId(0);
                            sbo.setText(c.getString(0));
                            sbo.setKpiTarget(c.getString(1));
                            sbo.setKpiAcheived(c.getString(2));


                            sbo.setCalculatedPercentage(c.getFloat(3));
                            if (sbo.getCalculatedPercentage() >= 100) {
                                sbo.setConvTargetPercentage(0);
                                sbo.setConvAcheivedPercentage(100);
                            } else {
                                sbo.setConvTargetPercentage(100 - sbo
                                        .getCalculatedPercentage());
                                sbo.setConvAcheivedPercentage(sbo
                                        .getCalculatedPercentage());
                            }
                            sbo.setKpiScore(c.getString(4));
                            sbo.setKpiIncentive(c.getString(5));
                            sbo.setKpiID(c.getInt(6));
                            sbo.setKpiTypeLovID(c.getInt(7));
                            sbo.setFlex1(c.getInt(8));
                            sbo.setSubDataCount(c.getInt(9));
                            sbo.setCode(c.getString(10));
                            sbo.setKpiFlex(c.getString(c.getColumnIndex("kpiFlex1")));
                            int value = SDUtil.convertToInt(c.getString(11));
                            if (value > 0 && value <= 12)
                                sbo.setMonthName(MONTH_NAME[value - 1]);
                            dashBoardBOS.add(sbo);
                        }
                        c.close();
                    }

                } catch (Exception ignored) {
                }

                shutDownDb();
                return dashBoardBOS;
            }
        });
    }

    @Override
    public Observable<ArrayList<DashBoardBO>> getRetailerDashboardForInterval(final String retailerId, final String interval) {
        return Observable.fromCallable(new Callable<ArrayList<DashBoardBO>>() {
            @Override
            public ArrayList<DashBoardBO> call() throws Exception {
                ArrayList<DashBoardBO> dashBoardBOS = new ArrayList<>();
                try {
                    initDb();

                    String monthText = "";
                    if (configurationMasterHelper.IS_KPI_CALENDAR) {
                        monthText = "RK.IntervalDesc";
                    } else {
                        monthText = "IFNULL(strftime('%m', replace(fromdate,'/','-')),0)";
                    }

                    String sql = "SELECT SLM.ListName,RKD.Target,RKD.Achievement,"
                            + " ROUND(CASE WHEN (100-((RKD.Achievement*100)/((RKD.Target)*1.0))) < 0"
                            + " THEN 100 ELSE ((RKD.Achievement*100)/((RKD.Target)*1.0)) END ,2) AS conv_ach_perc"
                            + ",IFNULL(RKS.Score,0),IFNULL(RKS.Incentive,0),RK.KPIID,RKD.KPIParamLovId,SLM.Flex1,SLM.ListCode," + monthText + " FROM RetailerKPI RK"
                            + " inner join RetailerKPIDetail RKD on RKD.KPIID= RK.KPIID"
                            + " LEFT join RetailerKPIScore RKS on RKD.KPIID= RKS.KPIID and RKD.KPIParamLovId = RKS.KPIParamLovId"
                            + " inner join StandardListMaster SLM on SLM.Listid=RKD.KPIParamLovId"
                            + " where retailerid =" + retailerId + " and interval= '" + interval + "' "
                            + " order by DisplaySeq asc";
                    Cursor c = mDbUtil.selectSQL(sql);
                    if (c != null) {
                        while (c.moveToNext()) {
                            DashBoardBO sbo = new DashBoardBO();
                            sbo.setPId(0);
                            sbo.setText(c.getString(0));
                            sbo.setKpiTarget(c.getString(1));
                            sbo.setKpiAcheived(c.getString(2));

                            sbo.setCalculatedPercentage(c.getFloat(3));
                            if (sbo.getCalculatedPercentage() >= 100) {
                                sbo.setConvTargetPercentage(0);
                                sbo.setConvAcheivedPercentage(100);
                            } else {
                                sbo.setConvTargetPercentage(100 - sbo
                                        .getCalculatedPercentage());
                                sbo.setConvAcheivedPercentage(sbo
                                        .getCalculatedPercentage());
                            }
                            sbo.setKpiScore(c.getString(4));
                            sbo.setKpiIncentive(c.getString(5));
                            sbo.setKpiID(c.getInt(6));
                            sbo.setKpiTypeLovID(c.getInt(7));
                            sbo.setFlex1(c.getInt(8));
                            sbo.setCode(c.getString(9));
                            if (configurationMasterHelper.IS_KPI_CALENDAR) {
                                sbo.setMonthName(c.getString(10));
                            } else {
                                int value = SDUtil.convertToInt(c.getString(10));
                                if (value > 0 && value <= 12)
                                    sbo.setMonthName(MONTH_NAME[value - 1]);
                            }

                            String lovSql = "select count(*) from RetailerKPISKUDetail where KPIParamLovId = " + sbo.getKpiTypeLovID();
                            Cursor lovCursor = mDbUtil.selectSQL(sql);
                            if (lovCursor != null) {
                                while (lovCursor.moveToNext()) {
                                    sbo.setSubDataCount(c.getInt(0));

                                }
                                lovCursor.close();
                            }

                            dashBoardBOS.add(sbo);
                        }
                        c.close();
                    }

                } catch (Exception ignored) {

                }

                shutDownDb();
                return dashBoardBOS;
            }
        });
    }

    @Override
    public Observable<ArrayList<DashBoardBO>> getRouteDashboardForInterval(final String interval) {
        return Observable.fromCallable(new Callable<ArrayList<DashBoardBO>>() {
            @Override
            public ArrayList<DashBoardBO> call() throws Exception {
                ArrayList<DashBoardBO> dashBoardBOS = new ArrayList<>();
                try {
                    initDb();

                    String sql = "SELECT SLM.ListName,RKD.Target,RKD.Achievement,"
                            + " ROUND(CASE WHEN (100-((RKD.Achievement*100)/((RKD.Target)*1.0))) < 0"
                            + " THEN 100 ELSE ((RKD.Achievement*100)/((RKD.Target)*1.0)) END ,2) AS conv_ach_perc"
                            + ",IFNULL(RKS.Score,0),IFNULL(RKS.Incentive,0),RK.KPIID,RKD.KPIParamLovId,SLM.Flex1,count(rkdd.KPIParamLovId),BeatDescription FROM RouteKPI RK"
                            + " inner join RouteKPIDetail RKD on RKD.KPIID= RK.KPIID"
                            + " LEFT join RouteKPIScore RKS on RKD.KPIID= RKS.KPIID and RKD.KPIParamLovId = RKS.KPIParamLovId"
                            + " inner join StandardListMaster SLM on SLM.Listid=RKD.KPIParamLovId"
                            + " LEFT join RouteKPISKUDetail rkdd on rkdd.KPIParamLovId =RKD.KPIParamLovId "
                            + " inner join BeatMaster on RouteID = BeatID "
                            + " where interval= '" + interval + "' "
                            + " AND "
                            + QT(SDUtil.now(SDUtil.DATE_GLOBAL))
                            + " between RK.fromdate and RK.todate group by SLM.Listid,RKD.KPiid order by DisplaySeq,RKD.KPiid asc";
                    Cursor c = mDbUtil.selectSQL(sql);
                    if (c != null) {
                        while (c.moveToNext()) {
                            DashBoardBO sbo = new DashBoardBO();
                            sbo.setPId(0);
                            sbo.setText(c.getString(0));
                            sbo.setKpiTarget(c.getString(1));
                            sbo.setKpiAcheived(c.getString(2));

                            sbo.setCalculatedPercentage(c.getFloat(3));
                            if (sbo.getCalculatedPercentage() >= 100) {
                                sbo.setConvTargetPercentage(0);
                                sbo.setConvAcheivedPercentage(100);
                            } else {
                                sbo.setConvTargetPercentage(100 - sbo
                                        .getCalculatedPercentage());
                                sbo.setConvAcheivedPercentage(sbo
                                        .getCalculatedPercentage());
                            }
                            sbo.setKpiScore(c.getString(4));
                            sbo.setKpiIncentive(c.getString(5));
                            sbo.setKpiID(c.getInt(6));
                            sbo.setKpiTypeLovID(c.getInt(7));
                            sbo.setFlex1(c.getInt(8));
                            sbo.setSubDataCount(c.getInt(9));
                            sbo.setMonthName(c.getString(10));
                            dashBoardBOS.add(sbo);
                        }
                        c.close();
                    }
                } catch (Exception ignored) {

                }
                shutDownDb();

                return dashBoardBOS;
            }
        });
    }

    @Override
    public Observable<ArrayList<DashBoardBO>> getKPIDashboard(final String userId, final String interval) {
        return Observable.fromCallable(new Callable<ArrayList<DashBoardBO>>() {
            @Override
            public ArrayList<DashBoardBO> call() throws Exception {
                ArrayList<DashBoardBO> dashBoardBOS = new ArrayList<>();
                try {
                    initDb();
                    String sql =
                            "SELECT SLM.ListName,SUM(SKD.Target),SUM(SKD.Achievement),"
                                    + " ROUND(CASE WHEN (100-((SUM(SKD.Achievement)*100)/((SUM(SKD.Target))*1.0))) < 0"
                                    + " THEN 100 ELSE ((SUM(SKD.Achievement)*100)/((SUM(SKD.Target))*1.0)) END ,2) AS conv_ach_perc"
                                    + ",SUM(SKS.Score),SUM(SKS.Incentive),SK.KPIID,SKD.KPIParamLovId,SLM.Flex1,SLM.ListCode FROM SellerKPI SK"
                                    + " inner join SellerKPIDetail SKD on SKD.KPIID= SK.KPIID"
                                    + " LEFT join SellerKPIScore SKS on SKD.KPIID= SKS.KPIID and SKD.KPIParamLovId = SKS.KPIParamLovId"
                                    + " inner join StandardListMaster SLM on SLM.Listid=SKD.KPIParamLovId"
                                    + " where userid in ( "
                                    + userId + ")"
                                    + " and interval= "
                                    + QT(interval)
                                    + " AND "
                                    + QT(SDUtil.now(SDUtil.DATE_GLOBAL))
                                    + " between SK.fromdate and SK.todate "
                                    + (userId.equals("0") ? " and SK.isSummary=1" : "")
                                    + " group by SLM.Listid order by DisplaySeq asc";
                    Cursor c = mDbUtil.selectSQL(sql);
                    sql = null;
                    if (c != null) {
                        while (c.moveToNext()) {
                            DashBoardBO sbo = new DashBoardBO();
                            sbo.setPId(0);
                            sbo.setText(c.getString(0));
                            sbo.setKpiTarget(c.getString(1));
                            sbo.setKpiAcheived(c.getString(2));


                            sbo.setCalculatedPercentage(c.getFloat(3));
                            if (sbo.getCalculatedPercentage() >= 100) {
                                sbo.setConvTargetPercentage(0);
                                sbo.setConvAcheivedPercentage(100);
                            } else {
                                sbo.setConvTargetPercentage(100 - sbo
                                        .getCalculatedPercentage());
                                sbo.setConvAcheivedPercentage(sbo
                                        .getCalculatedPercentage());
                            }
                            sbo.setKpiScore(c.getString(4));
                            sbo.setKpiIncentive(c.getString(5));
                            sbo.setKpiID(c.getInt(6));
                            sbo.setKpiTypeLovID(c.getInt(7));
                            sbo.setFlex1(c.getInt(8));
                            sbo.setCode(c.getString(9));
                            if (sbo.getCode().equalsIgnoreCase("VAL")) {
                                String subDataCountSql = "select count(kpiid) from SellerKPISKUDetail"
                                        + " where KPIParamLovId =" + sbo.getKpiTypeLovID();
                                Cursor subDataCountCursor = mDbUtil.selectSQL(subDataCountSql);
                                if (subDataCountCursor != null) {
                                    while (subDataCountCursor.moveToNext()) {
                                        sbo.setSubDataCount(subDataCountCursor.getInt(0));
                                    }
                                }
                                assert subDataCountCursor != null;
                                subDataCountCursor.close();
                            } else
                                sbo.setSubDataCount(0);
                            dashBoardBOS.add(sbo);
                        }
                        c.close();
                    }


                } catch (Exception ignored) {

                }

                shutDownDb();
                return dashBoardBOS;
            }
        });
    }

    @Override
    public Observable<ArrayList<DashBoardBO>> getP3MTrendChart(final String userId) {
        return Observable.fromCallable(new Callable<ArrayList<DashBoardBO>>() {
            @Override
            public ArrayList<DashBoardBO> call() throws Exception {
                ArrayList<DashBoardBO> dashBoardBOS = new ArrayList<>();
                try {
                    initDb();

                    String monthText = "";
                    if (configurationMasterHelper.IS_KPI_CALENDAR) {
                        monthText = "SK.IntervalDesc ";
                    } else {
                        monthText = "IFNULL(strftime('%m', replace(fromdate,'/','-')),0) ";
                    }

                    String sql = "SELECT SLM.ListName,SKD.Target,SKD.Achievement,"
                            + " ROUND(CASE WHEN (100-((SKD.Achievement*100)/((SKD.Target)*1.0))) < 0"
                            + " THEN 100 ELSE ((SKD.Achievement*100)/((SKD.Target)*1.0)) END ,2) AS conv_ach_perc"
                            + ",IFNULL(SKS.Score,0),IFNULL(SKS.Incentive,0),SK.KPIID,SKD.KPIParamLovId,SLM.Flex1,SLM.ListCode," + monthText + "FROM SellerKPI SK"
                            + " inner join SellerKPIDetail SKD on SKD.KPIID= SK.KPIID"
                            + " LEFT join SellerKPIScore SKS on SKD.KPIID= SKS.KPIID and SKD.KPIParamLovId = SKS.KPIParamLovId"
                            + " inner join StandardListMaster SLM on SLM.Listid=SKD.KPIParamLovId"
                            + " where userid = "
                            + QT(userId)
                            + " and interval= 'P3M' "
                            + (userId.equals("0") ? " and SK.isSummary=1" : "")
                            + " order by DisplaySeq asc";
                    Cursor c = mDbUtil.selectSQL(sql);
                    if (c != null) {
                        while (c.moveToNext()) {
                            DashBoardBO sbo = new DashBoardBO();
                            sbo.setPId(0);
                            sbo.setText(c.getString(0));
                            sbo.setKpiTarget(c.getString(1));
                            sbo.setKpiAcheived(c.getString(2));


                            sbo.setCalculatedPercentage(c.getFloat(3));
                            if (sbo.getCalculatedPercentage() >= 100) {
                                sbo.setConvTargetPercentage(0);
                                sbo.setConvAcheivedPercentage(100);
                            } else {
                                sbo.setConvTargetPercentage(100 - sbo
                                        .getCalculatedPercentage());
                                sbo.setConvAcheivedPercentage(sbo
                                        .getCalculatedPercentage());
                            }
                            sbo.setKpiScore(c.getString(4));
                            sbo.setKpiIncentive(c.getString(5));
                            sbo.setKpiID(c.getInt(6));
                            sbo.setKpiTypeLovID(c.getInt(7));
                            sbo.setFlex1(c.getInt(8));
                            sbo.setCode(c.getString(9));
                            if (configurationMasterHelper.IS_KPI_CALENDAR) {
                                sbo.setMonthName(c.getString(10));
                            } else {
                                int value = SDUtil.convertToInt(c.getString(10));
                                if (value > 0 && value <= 12)
                                    sbo.setMonthName(MONTH_NAME[value - 1]);
                            }

                            String lovIdSql = "select count(*) from SellerKPISKUDetail where KPIParamLovId = " + sbo.getKpiTypeLovID();
                            Cursor lovIdSqlCursor = mDbUtil.selectSQL(lovIdSql);
                            if (lovIdSqlCursor != null) {
                                while (lovIdSqlCursor.moveToNext()) {
                                    sbo.setSubDataCount(c.getInt(0));
                                }
                                lovIdSqlCursor.close();
                            }

                            dashBoardBOS.add(sbo);
                        }

                        c.close();
                    }

                } catch (Exception ignored) {

                }
                shutDownDb();

                return dashBoardBOS;
            }
        });
    }

    @Override
    public Observable<ArrayList<Double>> getCollectedValue() {
        return Observable.fromCallable(new Callable<ArrayList<Double>>() {
            @Override
            public ArrayList<Double> call() throws Exception {

                ArrayList<Double> collectedList = new ArrayList<>();
                double osAmt = 0, paidAmt = 0;
                try {
                    initDb();

                    String sb = "SELECT Round(IFNULL((select sum(payment.Amount) from payment where payment.BillNumber=Inv.InvoiceNo),0)+Inv.paidAmount,2) as RcvdAmt," +
                            " Round(inv.discountedAmount- IFNULL((select sum(payment.Amount) from payment where payment.BillNumber=Inv.InvoiceNo),0),2) as os " +
                            " FROM InvoiceMaster Inv LEFT OUTER JOIN payment ON payment.BillNumber = Inv.InvoiceNo" +
                            " Where Inv.InvoiceDate = " + QT(SDUtil.now(SDUtil.DATE_GLOBAL));

                    Cursor c = mDbUtil
                            .selectSQL(sb);

                    if (c != null) {
                        if (c.getCount() > 0) {
                            while (c.moveToNext()) {
                                paidAmt = paidAmt + c.getDouble(c.getColumnIndex("RcvdAmt"));
                                osAmt = osAmt + c.getDouble(c.getColumnIndex("os"));
                            }

                        }
                        c.close();
                    }

                    collectedList.add(osAmt);
                    collectedList.add(paidAmt);
                } catch (Exception ignored) {

                }
                shutDownDb();
                return collectedList;
            }
        });
    }

    @Override
    public Observable<ArrayList<String>> getKpiMonths(final boolean isFromRetailer) {
        return Observable.fromCallable(new Callable<ArrayList<String>>() {
            @Override
            public ArrayList<String> call() throws Exception {
                ArrayList<String> monthNoList = new ArrayList<>();
                try {
                    initDb();

                    String monthText = "";
                    if (configurationMasterHelper.IS_KPI_CALENDAR) {
                        monthText = "IntervalDesc";
                    } else {
                        monthText = "strftime('%m', replace(fromdate,'/','-'))";
                    }

                    String sb;
                    if (!isFromRetailer) {
                        sb = "SELECT distinct " + monthText + " AS Month FROM SellerKPI " +
                                "WHERE Interval=" + QT(P3M) +
                                " order by fromdate desc";
                    } else {
                        sb = "SELECT distinct " + monthText + " AS Month FROM RetailerKPI " +
                                "WHERE RetailerId= " + appDataProvider.getRetailMaster().getRetailerID() + " AND Interval=" + QT(P3M) +
                                " order by fromdate desc";
                    }

                    Cursor c = mDbUtil.selectSQL(sb);
                    int index = 0;
                    if (c.getCount() > 0) {
                        while (c.moveToNext()) {
                            if (configurationMasterHelper.IS_KPI_CALENDAR) {
                                monthNoList.add(c.getString(0));
                                Date date = new SimpleDateFormat("MMMM", Locale.ENGLISH).parse(c.getString(0));
                                Calendar cal = Calendar.getInstance();
                                cal.setTime(date);
                                index++;
                                if ((Calendar.getInstance().get(Calendar.MONTH) + 1) == cal.get(Calendar.MONTH))
                                    currentmonthindex = index - 1;
                            } else {
                                int monthValue = SDUtil.convertToInt(c.getString(0));
                                if (monthValue > 0 && monthValue <= 12) {
                                    monthNoList.add(MONTH_NAME[monthValue - 1]);
                                    index++;
                                    if ((Calendar.getInstance().get(Calendar.MONTH) + 1) == monthValue)
                                        currentmonthindex = index - 1;
                                }
                            }
                        }
                    }

                } catch (Exception ignored) {

                }
                shutDownDb();

                return monthNoList;
            }
        });
    }

    @Override
    public Observable<ArrayList<String>> getKpiWeekList() {
        return Observable.fromCallable(new Callable<ArrayList<String>>() {
            @Override
            public ArrayList<String> call() throws Exception {
                ArrayList<String> weekList = new ArrayList<>();
                try {
                    initDb();

                    String sb = "SELECT distinct IntervalDesc AS Week FROM sellerkpi " +
                            "WHERE Interval=" + QT(WEEK) +
                            " order by Week desc";
                    Cursor c = mDbUtil.selectSQL(sb);
                    if (c.getCount() > 0) {
                        while (c.moveToNext()) {
                            weekList.add(c.getString(0));
                        }
                    }

                } catch (Exception ignored) {

                }
                shutDownDb();
                return weekList;
            }
        });
    }

    @Override
    public Single<Integer> getCurrentWeek(final ArrayList<String> weekList) {
        return Single.fromCallable(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                int week =0;
                try{
                    initDb();

                    String sb = "Select IntervalDesc from SellerKPI where " + QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + " between fromdate and todate and Interval = " + QT(WEEK);
                    Cursor c = mDbUtil.selectSQL(sb);
                    if (c.getCount() > 0) {
                        while (c.moveToNext()) {
                            return weekList.indexOf(c.getString(0));
                        }
                    }

                }catch (Exception ignored){

                }

                shutDownDb();
                return week;
            }
        });
    }
}
