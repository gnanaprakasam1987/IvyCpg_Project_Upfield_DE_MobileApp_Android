package com.ivy.ui.dashboard.data;

import android.database.Cursor;

import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.cpg.view.dashboard.DashBoardBO;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.ui.dashboard.SellerDashboardConstants;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Observable;

import static com.ivy.cpg.view.dashboard.DashBoardHelper.MONTH_NAME;
import static com.ivy.utils.AppUtils.QT;

public class SellerDashboardDataManagerImpl implements SellerDashboardDataManager {

    private DBUtil mDbUtil;

    private AppDataProvider appDataProvider;

    private ConfigurationMasterHelper configurationMasterHelper;

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
                try{

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

                }catch (Exception ignored){
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



}
