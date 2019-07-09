package com.ivy.ui.dashboard.data;

import android.database.Cursor;

import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.data.channel.ChannelDataManager;
import com.ivy.core.di.scope.ChannelInfo;
import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.cpg.view.dashboard.DashBoardBO;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.DailyReportBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.ui.dashboard.SellerDashboardConstants;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.rx.Optional;

import org.reactivestreams.Publisher;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

import static com.ivy.cpg.view.dashboard.DashBoardHelper.MONTH_NAME;
import static com.ivy.ui.dashboard.SellerDashboardConstants.P3M;
import static com.ivy.ui.dashboard.SellerDashboardConstants.PRD_ORD;
import static com.ivy.ui.dashboard.SellerDashboardConstants.PRD_STK;
import static com.ivy.ui.dashboard.SellerDashboardConstants.PRODUCTVIE_CALLS;
import static com.ivy.ui.dashboard.SellerDashboardConstants.WEEK;
import static com.ivy.utils.StringUtils.getStringQueryParam;

public class SellerDashboardDataManagerImpl implements SellerDashboardDataManager {

    private DBUtil mDbUtil;

    private AppDataProvider appDataProvider;

    private ConfigurationMasterHelper configurationMasterHelper;

    private ChannelDataManager channelDataManager;

    private int currentmonthindex = 0;

    @Inject
    public SellerDashboardDataManagerImpl(@DataBaseInfo DBUtil dbUtil, AppDataProvider appDataProvider, ConfigurationMasterHelper configurationMasterHelper, @ChannelInfo ChannelDataManager channelDataManager) {
        mDbUtil = dbUtil;
        this.appDataProvider = appDataProvider;
        this.configurationMasterHelper = configurationMasterHelper;
        this.channelDataManager = channelDataManager;
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
    public Observable<List<String>> getDashList(final SellerDashboardConstants.DashBoardType dashBoardType) {
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
    public Observable<List<DashBoardBO>> getP3MSellerDashboardData(final String userId) {
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
                            + getStringQueryParam(userId)
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
    public Observable<List<DashBoardBO>> getSellerDashboardForWeek(final String userId) {
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
                            + getStringQueryParam(userId)
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
    public Observable<List<DashBoardBO>> getSellerDashboardForInterval(final String userId, final String interval) {
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
                            + getStringQueryParam(userId)
                            + " and interval= "
                            + getStringQueryParam(interval)
                            + " AND "
                            + getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL))
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
    public Observable<List<DashBoardBO>> getRetailerDashboardForInterval(final String retailerId, final String interval) {
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
    public Observable<List<DashBoardBO>> getRouteDashboardForInterval(final String interval) {
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
                            + getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL))
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
    public Observable<List<DashBoardBO>> getKPIDashboard(final String userId, final String interval) {
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
                                    + getStringQueryParam(interval)
                                    + " AND "
                                    + getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL))
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
    public Observable<List<DashBoardBO>> getP3MTrendChart(final String userId) {
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
                            + getStringQueryParam(userId)
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

                if (dashBoardBOS.size() > 0) {
                    Collections.sort(dashBoardBOS, paramsIDComparator);
                }

                return dashBoardBOS;
            }
        });
    }

    private Comparator<DashBoardBO> paramsIDComparator = new Comparator<DashBoardBO>() {

        public int compare(DashBoardBO file1, DashBoardBO file2) {

            return (int) file1.getKpiTypeLovID() - (int) file2.getKpiTypeLovID();

        }

    };


    @Override
    public Observable<List<Double>> getCollectedValue() {
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
                            " Where Inv.InvoiceDate = " + getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL));

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
    public Observable<List<String>> getKpiMonths(final boolean isFromRetailer) {
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
                                "WHERE Interval=" + getStringQueryParam(P3M) +
                                " order by fromdate desc";
                    } else {
                        sb = "SELECT distinct " + monthText + " AS Month FROM RetailerKPI " +
                                "WHERE RetailerId= " + appDataProvider.getRetailMaster().getRetailerID() + " AND Interval=" + getStringQueryParam(P3M) +
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
    public Observable<List<String>> getKpiWeekList() {
        return Observable.fromCallable(new Callable<ArrayList<String>>() {
            @Override
            public ArrayList<String> call() throws Exception {
                ArrayList<String> weekList = new ArrayList<>();
                try {
                    initDb();

                    String sb = "SELECT distinct IntervalDesc AS Week FROM sellerkpi " +
                            "WHERE Interval=" + getStringQueryParam(WEEK) +
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
    public Single<String> getCurrentWeekInterval() {
        return Single.fromCallable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                try {
                    initDb();

                    String sb = "Select IntervalDesc from SellerKPI where " + getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) + " between fromdate and todate and Interval = " + getStringQueryParam(WEEK);
                    Cursor c = mDbUtil.selectSQL(sb);
                    if (c.getCount() > 0) {
                        while (c.moveToNext()) {
                            return c.getString(0);
                        }
                    }

                } catch (Exception ignored) {

                }

                shutDownDb();
                return "";
            }
        });
    }

    @Override
    public Single<Optional<DailyReportBO>> fetchOutletDailyReport() {
        return Single.fromCallable(new Callable<Optional<DailyReportBO>>() {
            @Override
            public Optional<DailyReportBO> call() throws Exception {

                DailyReportBO dailyRep = null;

                StringBuffer sb = new StringBuffer();
                Cursor c = null;
                try {
                    initDb();

                    if (!configurationMasterHelper.IS_INVOICE) {
                        sb.append("select  count(distinct retailerid),sum(linespercall),sum(ordervalue) from OrderHeader ");
                        sb.append("where upload!='X' and OrderDate=").append(getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)));
                        c = mDbUtil
                                .selectSQL(sb.toString());
                        if (c != null) {
                            if (c.moveToNext()) {

                                dailyRep.setEffCoverage(c.getString(0));
                                dailyRep.setTotLines(c.getInt(1) + "");
                                dailyRep.setTotValues(c.getDouble(2) + "");
                            }
                            c.close();
                        }
                    } else {
                        sb.append("select  count(distinct retailerid),sum(linespercall),sum(invoiceAmount) from Invoicemaster ");
                        sb.append("where InvoiceDate=" + getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)));
                        c = mDbUtil
                                .selectSQL(sb.toString());
                        if (c != null) {
                            if (c.moveToNext()) {
                                dailyRep.setEffCoverage(c.getString(0));
                                dailyRep.setTotLines(c.getInt(1) + "");
                                dailyRep.setTotValues(c.getDouble(2) + "");
                            }
                            c.close();
                        }
                    }
                    sb = new StringBuffer();
                    sb.append("select  sum(mspvalues),count(distinct orderid) from OrderHeader ");
                    sb.append("where upload!='X' and OrderDate=" + getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)));
                    c = mDbUtil
                            .selectSQL(sb.toString());
                    if (c != null) {
                        if (c.moveToNext()) {

                            dailyRep.setMspValues(c.getString(0));
                            dailyRep.setNoofOrder(c.getString(1));

                        }
                        c.close();
                    }

                } catch (Exception ignored) {

                }

                shutDownDb();
                return new Optional<DailyReportBO>(dailyRep);
            }
        });
    }

    @Override
    public Single<Integer> fetchTotalCallsForTheDayExcludingDeviatedVisits() {
        return Single.fromCallable(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                int total_calls = 0;
                try {
                    initDb();

                    Cursor c = mDbUtil
                            .selectSQL("SELECT COUNT(DISTINCT RM.RETAILERID) FROM RETAILERMASTER RM inner join RetailerMasterInfo RMI " +
                                    "on RM.RetailerID = RMI.RetailerId where RMI.istoday = 1");
                    if (c != null) {
                        if (c.getCount() > 0) {
                            while (c.moveToNext())
                                total_calls = c.getInt(0);
                        }
                        c.close();
                    }

                } catch (Exception ignored) {

                }

                shutDownDb();
                return total_calls;
            }
        });
    }

    @Override
    public Single<Optional<DailyReportBO>> fetchNoOfInvoiceAndValue() {
        return Single.fromCallable(new Callable<Optional<DailyReportBO>>() {
            @Override
            public Optional<DailyReportBO> call() throws Exception {
                DailyReportBO dailyRp = new DailyReportBO();
                try {
                    initDb();

                    Cursor c = mDbUtil
                            .selectSQL("select count(distinct Inv.InvoiceNo),sum(Inv.totalamount) from Invoicemaster Inv" +
                                    " INNER JOIN OrderHeader OH ON OH.orderId=Inv.OrderId where Inv.invoicedate = "
                                    + getStringQueryParam(appDataProvider.getUser().getDownloadDate()));
                    if (c != null) {
                        if (c.getCount() > 0) {
                            while (c.moveToNext()) {
                                dailyRp.setTotLines(c.getInt(0) + "");
                                dailyRp.setTotValues(c.getDouble(1) + "");
                            }
                        }
                        c.close();
                    }

                } catch (Exception ignored) {

                }

                shutDownDb();
                return new Optional<>(dailyRp);
            }
        });
    }

    @Override
    public Single<Optional<DailyReportBO>> fetchNoOfOrderAndValue() {
        return Single.fromCallable(new Callable<Optional<DailyReportBO>>() {
            @Override
            public Optional<DailyReportBO> call() throws Exception {
                DailyReportBO dailyRp = new DailyReportBO();
                try {
                    initDb();

                    Cursor c = mDbUtil
                            .selectSQL("select count(distinct orderid),sum(totalamount) from OrderHeader where invoicestatus =0 ");
                    if (c != null) {
                        if (c.getCount() > 0) {
                            while (c.moveToNext()) {
                                dailyRp.setTotLines(c.getInt(0) + "");
                                dailyRp.setTotValues(c.getDouble(1) + "");
                            }
                        }
                        c.close();
                    }


                } catch (Exception ignored) {

                }

                shutDownDb();
                return new Optional<>(dailyRp);
            }
        });
    }


    /**
     * This method will return the productive retailers count for the Day. For
     * Van Seller, this method will get distinct retailer count from
     * InvoiceTable and For Pre-seller from OrderHeader if PRD_FOR_ORDER is TRUE and from ClosingStockHeader if PRD_FOR_STK is true.
     * This config is computed through loadProductiveCallsConfig()
     * Deviated retailer productivity wont be considered for deviated retailers.
     *
     * @return ProductiveCallsForTheDay
     */
    @Override
    public Single<Integer> getProductiveCallsForDay() {
        return Single.fromCallable(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                int productive_calls = 0;
                try {
                    initDb();

                    Cursor c = null;
                    if (configurationMasterHelper.IS_INVOICE && !configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED) {
                        // c =
                        // db.selectSQL("SELECT distinct(Retailerid) FROM InvoiceMaster");

                        if (appDataProvider.getBeatMasterBo() == null
                                || appDataProvider.getBeatMasterBo().getBeatId() == 0) {
                            c = mDbUtil.selectSQL("select  distinct(Retailerid) from InvoiceMaster where upload='N'");
                        } else {
                            c = mDbUtil.selectSQL("select  distinct(i.Retailerid) from InvoiceMaster i inner join retailermaster r on "
                                    + "i.retailerid=r.retailerid  inner join Retailermasterinfo RMI on RMI.retailerid= R.retailerid "
                                    + "LEFT JOIN RetailerBeatMapping RBM ON RBM.RetailerID = r.RetailerID"
                                    + " where RBM.isdeviated='Y' or RMI.isToday=1 and i.IsPreviousInvoice = 0 ");
                        }
                    } else {

                        String sql = "SELECT RField FROM "
                                + DataMembers.tbl_HhtModuleMaster
                                + " where hhtCode=" + getStringQueryParam(PRODUCTVIE_CALLS) + " AND flag='1' and ForSwitchSeller = 0";

                        Cursor prodCallsConfigCursor = mDbUtil.selectSQL(sql);

                        boolean isProdForOrder = false;
                        if (prodCallsConfigCursor != null && prodCallsConfigCursor.getCount() != 0) {
                            while (prodCallsConfigCursor.moveToNext()) {
                                if (prodCallsConfigCursor.getString(0).equalsIgnoreCase(PRD_ORD))
                                    isProdForOrder = true;
                                else if (prodCallsConfigCursor.getString(0).equalsIgnoreCase(PRD_STK))
                                    isProdForOrder = false;

                            }
                            prodCallsConfigCursor.close();
                        }

                        if (isProdForOrder) {
                            if (appDataProvider.getBeatMasterBo() == null
                                    || appDataProvider.getBeatMasterBo().getBeatId() == 0) {
                                c = mDbUtil.selectSQL("select  distinct(Retailerid) from OrderHeader where upload!='X'");
                            } else {
                                c = mDbUtil.selectSQL("select  distinct(o.Retailerid) from OrderHeader o inner join retailermaster r on "
                                        + "o.retailerid=r.retailerid where o.upload!='X' ");// where
                            }
                        } else {
                            if (appDataProvider.getBeatMasterBo() == null
                                    || appDataProvider.getBeatMasterBo().getBeatId() == 0) {
                                c = mDbUtil.selectSQL("select  distinct(RetailerID) from ClosingStockHeader");
                            } else {
                                c = mDbUtil.selectSQL("select  distinct(CSH.RetailerID) from ClosingStockHeader CSH INNER JOIN RetailerMaster RM on "
                                        + "CSH.RetailerID=RM.RetailerID ");
                            }
                        }
                    }
                    if (c != null) {
                        if (c.getCount() > 0) {
                            productive_calls = c.getCount();
                        }
                        c.close();
                    }


                } catch (Exception ignored) {

                }

                shutDownDb();
                return productive_calls;
            }
        });
    }

    @Override
    public Single<Integer> getVisitedCallsForTheDayExcludingDeviatedVisits() {
        return Single.fromCallable(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                int visited_calls = 0;
                try {
                    initDb();

                    Cursor c = mDbUtil.selectSQL("select count(distinct RM.retailerid) from retailermaster RM inner join " +
                            "RetailerBeatMapping RBM on RM.RetailerId = RBM.Retailerid where RBM.isdeviated='N' and RBM.isVisited = 'Y'");
                    if (c != null) {
                        if (c.getCount() > 0) {
                            if (c.moveToNext()) {
                                visited_calls = c.getInt(0);
                            }
                        }
                        c.close();
                    }

                } catch (Exception ignored) {

                }

                shutDownDb();
                return visited_calls;
            }
        });
    }

    @Override
    public Single<Integer> getProductiveCallsForTheDayExcludingDeviatedVisits() {
        return Single.fromCallable(() -> {
            int productive_calls = 0;
            try {
                initDb();

                Cursor c = null;
                if (configurationMasterHelper.IS_INVOICE && !configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED) {

                    if (appDataProvider.getBeatMasterBo() == null
                            || appDataProvider.getBeatMasterBo().getBeatId() == 0) {
                        c = mDbUtil.selectSQL("select distinct(i.Retailerid) from InvoiceMaster i" +
                                " inner join retailermaster r on i.retailerid=r.retailerid inner join " +
                                "RetailerBeatMapping RBM on r.retailerid = RBM.Retailerid where RBM.isdeviated='N' and RBM.isVisited = 'Y'");
                    } else {
                        c = mDbUtil.selectSQL("select  distinct(i.Retailerid) from InvoiceMaster i inner join retailermaster r on "
                                + "i.retailerid=r.retailerid  inner join Retailermasterinfo RMI on RMI.retailerid= R.retailerid "
                                + "inner join RetailerBeatMapping RBM on r.retailerid = RBM.Retailerid"
                                + " where RBM.isdeviated='N' or RMI.isToday=1 and i.IsPreviousInvoice = 0 and RBM.isVisited = 'Y'");
                    }
                } else {
                    c = mDbUtil.selectSQL("select  distinct(r.Retailerid) from OrderHeader o inner join retailermaster r " +
                            "on o.retailerid=r.retailerid inner join RetailerBeatMapping RBM on r.retailerid = RBM.Retailerid " +
                            "where o.upload!='X' and RBM.isdeviated='N' and RBM.isVisited = 'Y'");
                }
                if (c != null) {
                    if (c.getCount() > 0) {
                        while (c.moveToNext())
                            productive_calls = c.getCount();
                    }
                    c.close();
                }

            } catch (Exception ignored) {

            }

            shutDownDb();
            return productive_calls;
        });
    }

    @Override
    public Single<Double> fetchFocusBrandInvoiceAmt() {
        return Single.fromCallable(new Callable<Double>() {
            @Override
            public Double call() throws Exception {

                Double totalValue = 0.0;
                try {

                    initDb();

                    String sb = "select count(distinct OrderID),sum(FocusPackValues) from OrderHeader" +
                            " where invoicestatus=1";
                    Cursor c = mDbUtil
                            .selectSQL(sb);
                    if (c != null) {
                        if (c.getCount() > 0) {
                            while (c.moveToNext()) {
                                totalValue = c.getDouble(1);
                            }
                        }
                        c.close();
                    }


                } catch (Exception ignored) {

                }
                shutDownDb();

                return totalValue;
            }
        });
    }

    @Override
    public Single<Double> fetchSalesReturnValue() {
        return Single.fromCallable(new Callable<Double>() {
            @Override
            public Double call() throws Exception {
                double salesReturnValue = 0.0;
                initDb();
                try {
                    Cursor c = mDbUtil
                            .selectSQL("select count(distinct uid),sum(ReturnValue) from SalesReturnHeader where upload!='X'");
                    if (c != null) {
                        if (c.getCount() > 0) {
                            while (c.moveToNext()) {
                                salesReturnValue = c.getDouble(1);
                            }
                        }
                        c.close();
                    }
                } catch (Exception ignored) {

                }

                return salesReturnValue;
            }
        });
    }

    @Override
    public Single<Optional<DailyReportBO>> fetchFulfilmentValue() {
        return Single.fromCallable(new Callable<Optional<DailyReportBO>>() {
            @Override
            public Optional<DailyReportBO> call() throws Exception {
                DailyReportBO dailyRp = new DailyReportBO();

                try {
                    initDb();

                    String query = "select VL.pcsqty,VL.outerqty,VL.douomqty,VL.caseqty,VL.duomqty,"
                            + "(select qty from StockInHandMaster where pid = VL.pid) as SIHQTY,"
                            + "(select srp1 from PriceMaster where scid = 0 and pid = VL.pid) as price from VanLoad VL"
                            + " inner join stockapply sa on sa.uid = vl.uid";
                    Cursor c = mDbUtil
                            .selectSQL(query);
                    int loadQty;
                    int deliverQty;
                    double price;
                    double deliveredValue = 0;
                    double loadedValue = 0;

                    if (c != null) {
                        if (c.getCount() > 0) {
                            while (c.moveToNext()) {
                                loadQty = c.getInt(0) + (c.getInt(1) * c.getInt(2))
                                        + (c.getInt(3) * c.getInt(4));
                                deliverQty = loadQty - c.getInt(5);
                                deliverQty = deliverQty < 0 ? 0 : deliverQty;
                                price = c.getDouble(6);
                                deliveredValue += deliverQty * price;
                                loadedValue += loadQty * price;
                            }
                            dailyRp.setDelivered(deliveredValue);
                            dailyRp.setLoaded(loadedValue);
                        }
                        c.close();
                    }

                } catch (Exception ignored) {

                }

                shutDownDb();

                return new Optional<>(dailyRp);
            }
        });
    }

    @Override
    public Single<Integer> fetchPromotionCount() {

        return channelDataManager.fetchChannelIds().flatMap(new Function<String, SingleSource<Integer>>() {
            @Override
            public SingleSource<Integer> apply(final String channelIds) throws Exception {
                return Single.fromCallable(new Callable<Integer>() {
                    @Override
                    public Integer call() throws Exception {

                        String chIDs = "";
                        int count = 0;
                        try {

                            for (RetailerMasterBO retailerMasterBO : appDataProvider.getRetailerMasters()) {
                                if (retailerMasterBO.getIsToday() == 1) {
                                    chIDs = chIDs + "," + channelIds;
                                }
                            }
                            if (chIDs.endsWith(","))
                                chIDs = chIDs.substring(0, chIDs.length() - 1);

                            initDb();
                            Cursor c = mDbUtil.selectSQL(("SELECT count(PromoID) FROM PromotionMapping where chid in (" + chIDs + ")"));
                            if (c.getCount() > 0) {
                                while (c.moveToNext()) {
                                    count = c.getInt(0);
                                }
                            }
                            c.close();

                        } catch (Exception ignored) {

                        }
                        shutDownDb();
                        return count;
                    }
                });
            }
        });

    }

    private int promotionExecutedCount = 0;

    @Override
    public Single<Integer> fetchPromotionExecutedCount() {
        promotionExecutedCount = 0;

        return Flowable.just(appDataProvider.getRetailerMasters()).flatMap(new Function<ArrayList<RetailerMasterBO>, Publisher<RetailerMasterBO>>() {
            @Override
            public Publisher<RetailerMasterBO> apply(ArrayList<RetailerMasterBO> retailerMasterBOS) throws Exception {

                return Flowable.fromIterable(retailerMasterBOS);
            }
        }).map(new Function<RetailerMasterBO, RetailerMasterBO>() {
            @Override
            public RetailerMasterBO apply(RetailerMasterBO retailerMasterBO) throws Exception {
                return retailerMasterBO;
            }
        }).takeWhile(new Predicate<RetailerMasterBO>() {
            @Override
            public boolean test(RetailerMasterBO retailerMasterBO) throws Exception {
                return retailerMasterBO.getIsToday() == 1;
            }
        }).flatMapSingle(new Function<RetailerMasterBO, SingleSource<Integer>>() {
            @Override
            public SingleSource<Integer> apply(RetailerMasterBO retailerMasterBO) throws Exception {
                return fetchPromotionExecCount(retailerMasterBO.getRetailerID());
            }
        }).repeat().lastElement().toSingle();
    }


    private Single<Integer> fetchPromotionExecCount(final String retailerID) {
        return Single.fromCallable(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {


                try {
                    initDb();

                    Cursor c = mDbUtil.selectSQL("SELECT count( distinct PromotionID) FROM PromotionDetail where RetailerID =" + getStringQueryParam(retailerID));
                    if (c.getCount() > 0) {
                        while (c.moveToNext()) {
                            promotionExecutedCount += c.getInt(0);
                        }
                    }
                    c.close();

                } catch (Exception ignored) {

                }
                shutDownDb();

                return promotionExecutedCount;
            }
        });
    }

    @Override
    public Single<Optional<String>> fetchMslCount() {
        final int[] mslCount = {0};
        return channelDataManager.fetchChannelIds().flatMap((Function<String, SingleSource<Optional<String>>>) channelIds -> Single.fromCallable(() -> {

            String chIDs = "";
            String mslProdIDs = "";
            try {

                for (RetailerMasterBO retailerMasterBO : appDataProvider.getRetailerMasters()) {
                    if (retailerMasterBO.getIsToday() == 1) {
                        chIDs = chIDs + "," + channelIds;
                    }
                }
                if (chIDs.endsWith(","))
                    chIDs = chIDs.substring(0, chIDs.length() - 1);

                String sb = "SELECT PTGM.pid FROM ProductTaggingMaster PTM " +
                        "inner join ProductTaggingGroupMapping PTGM on PTGM.groupid = PTCM.groupid " +
                        "inner join  ProductTaggingCriteriaMapping PTCM on PTM.groupid = PTCM.groupid " +
                        "AND PTM.TaggingTypelovID in (select listid from standardlistmaster where listcode='MSL' and listtype='PRODUCT_TAGGING') " +
                        "where PTCM.ChannelId in (" + chIDs + ")";

                Cursor c = mDbUtil.selectSQL(sb);
                if (c.getCount() > 0) {
                    while (c.moveToNext()) {
                        mslCount[0]++;
                        mslProdIDs = mslProdIDs + "," + c.getInt(1);
                    }
                }
                c.close();
            } catch (Exception ignored) {

            }
            shutDownDb();
            return mslProdIDs;
        }).flatMap((Function<String, SingleSource<Optional<String>>>) mslProdIDs -> Single.fromCallable(new Callable<Optional<String>>() {
            @Override
            public Optional<String> call() throws Exception {

                String rids = "";
                int mslExecutedCount = 0;

                for (RetailerMasterBO retailerMasterBO : appDataProvider.getRetailerMasters())
                    if (retailerMasterBO.getIsToday() == 1) {
                        rids = rids + "," + retailerMasterBO.getRetailerID();
                    }

                if (rids.startsWith(","))
                    rids = rids.substring(1, rids.length());
                if (rids.endsWith(","))
                    rids = rids.substring(0, rids.length() - 1);

                try {
                    initDb();

                    StringBuilder sb = new StringBuilder();
                    sb.append("select count(*) from OrderDetail where retailerid in (").append(rids).append(")");
                    if (mslProdIDs != null && !mslProdIDs.isEmpty())
                        sb.append("and ProductID in (").append(mslProdIDs).append(")");
                    Cursor c = mDbUtil.selectSQL(sb.toString());
                    if (c.getCount() > 0) {
                        while (c.moveToNext()) {
                            mslExecutedCount = c.getInt(0);
                        }
                    }
                    c.close();

                } catch (Exception ignored) {

                }


                return new Optional<String>(mslCount[0] + "," + mslExecutedCount);
            }
        })));
    }
}
