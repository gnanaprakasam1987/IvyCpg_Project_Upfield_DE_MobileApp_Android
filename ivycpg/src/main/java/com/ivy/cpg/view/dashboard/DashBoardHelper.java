package com.ivy.cpg.view.dashboard;

import android.content.Context;
import android.database.Cursor;
import android.util.SparseArray;

import com.ivy.core.data.user.UserDataManagerImpl;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.DailyReportBO;
import com.ivy.sd.png.bo.PriorityBo;
import com.ivy.sd.png.bo.RetailerKPIBO;
import com.ivy.sd.png.bo.SKUWiseTargetBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.ProductTaggingHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.ui.dashboard.data.SellerDashboardDataManagerImpl;
import com.ivy.utils.DateTimeUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class DashBoardHelper {

    private Context mContext;
    private BusinessModel bmodel;
    private static DashBoardHelper instance = null;
    private ArrayList<IncentiveDashboardBO> incentiveList;
    private ArrayList<String> incentiveType;
    private ArrayList<String> incentiveGroups;
    ArrayList<Integer> mslProdIDs = null;

    public ArrayList<IncentiveDashboardBO> getIncentiveList() {
        return incentiveList;
    }

    public void setIncentiveList(ArrayList<IncentiveDashboardBO> incentiveList) {
        this.incentiveList = incentiveList;
    }

    private List<DashBoardBO> dashMasterData;
    private ArrayList<DashBoardBO> dashChartData;
    private ArrayList<DashBoardBO> p3mChartList;
    private List<CharSequence> beatList;

    private Vector<DashBoardBO> dashBoardReportList;

    public int mMinLevel;
    public int mMaxLevel;
    public int mSellerKpiMinLevel, mSellerKpiMaxLevel, mSellerKpiMinSeqLevel, mSellerKpiMaxSeqLevel;
    public int mSelectedSkuIndex = 0;

    public int mParamLovId = 0;

    public DashBoardBO dashboardBO = new DashBoardBO();

    private ArrayList<SKUWiseTargetBO> skuWiseTarget;
    private Vector<SKUWiseTargetBO> sellerKpiSku;
    private Map<Integer, Integer> kpiSkuMasterById;

    //for viewpager pie graph used in both kpi and day dashboard
    private ArrayList<SKUWiseTargetBO> skuwiseGraphData;

    private Vector<DashBoardBO> monthList;

    private ArrayList<DashBoardBO> kpiList;
    public static final String MONTH_NAME[] = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
    private SparseArray<ArrayList<RetailerKPIBO>> mRetailerKpiListBymonth;
    private LinkedHashSet<Integer> mRetailerKpiMonthList;
    private int showDayAndP3MSpinner = 0;

    //for P3M data loading for latest UI and chart
    private static final String P3M = "P3M";
    private static final String WEEK = "WEEK";
    ArrayList<String> weekList = new ArrayList<>();

    public boolean PRD_FOR_ORDER = false;
    public boolean PRD_FOR_SKT = false;
    private static final String PRODUCTVIE_CALLS = "PRODUCTIVECALL";
    private static final String PRD_ORD = "ORD";
    private static final String PRD_STK = "STK";

    private DashBoardHelper(Context context) {
        this.mContext = context;
        dashMasterData = new ArrayList<>();
        dashChartData = new ArrayList<>();
        dashBoardReportList = new Vector<>();
        p3mChartList = new ArrayList<>();
        monthList = new Vector<>();
        this.bmodel = (BusinessModel) context.getApplicationContext();
    }

    public static DashBoardHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DashBoardHelper(context);
        }
        return instance;
    }

    public ArrayList<String> getDashBoardFilter() {
        ArrayList<String> dashBoardFilterList = new ArrayList<>();
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        try {
            db.createDataBase();
            db.openDataBase();
            String sb = "select distinct  type as type1 from dashboardmaster where (type='MONTH' OR type='YEAR' OR type='DAY' OR type='QUARTER') " +
                    "union select distinct freqtype as type1  from skuwisetarget where(freqtype='MONTH' OR freqtype='YEAR' OR freqtype='DAY' OR freqtype='QUARTER')";
            Cursor c = db.selectSQL(sb);
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    dashBoardFilterList.add(c.getString(0));
                }
            }
            c.close();


        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }
        return dashBoardFilterList;
    }

    int currentmonthindex = 0;

    public int getCurrentMonthIndex() {
        return currentmonthindex;
    }

    public ArrayList<String> getMonthNameList() {
        ArrayList<String> monthNoList = new ArrayList<>();
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        try {
            db.createDataBase();
            db.openDataBase();
            String sb = "SELECT distinct strftime('%m', replace(date,'/','-')) AS Month FROM dashboardmaster where Month Not null and type='MONTH' " +
                    " union select distinct strftime('%m', replace(date,'/','-')) AS Month from skuwisetarget where Month Not null" +
                    " order by Month desc";
            Cursor c = db.selectSQL(sb);
            int index = 0;
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    int monthValue = SDUtil.convertToInt(c.getString(0));
                    if (monthValue > 0 && monthValue <= 12) {
                        monthNoList.add(MONTH_NAME[monthValue - 1]);
                        index++;
                        if ((Calendar.getInstance().get(Calendar.MONTH) + 1) == monthValue)
                            currentmonthindex = index - 1;
                    }
                }
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }
        return monthNoList;


    }

    /**
     * @param isFromHomeScreenTwo
     * @return isFromHomeScreenTwo - false  Load Month List for Seller P3M interval
     * isFromHomeScreenTwo - true   Load Month List for Retailer P3M interval
     * @See {@link SellerDashboardDataManagerImpl#getKpiMonths(boolean)}
     * @deprecated
     */
    @Deprecated
    public ArrayList<String> getKpiMonthNameList(boolean isFromHomeScreenTwo) {
        ArrayList<String> monthNoList = new ArrayList<>();
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        try {
            db.createDataBase();
            db.openDataBase();

            String monthText = "";
            if (bmodel.configurationMasterHelper.IS_KPI_CALENDAR) {
                monthText = "IntervalDesc";
            } else {
                monthText = "strftime('%m', replace(fromdate,'/','-'))";
            }

            String sb;
            if (!isFromHomeScreenTwo) {
                sb = "SELECT distinct " + monthText + " AS Month FROM SellerKPI " +
                        "WHERE Interval=" + bmodel.QT(P3M) +
                        " order by fromdate desc";
            } else {
                sb = "SELECT distinct " + monthText + " AS Month FROM RetailerKPI " +
                        "WHERE RetailerId= " + bmodel.getRetailerMasterBO().getRetailerID() + " AND Interval=" + bmodel.QT(P3M) +
                        " order by fromdate desc";
            }

            Cursor c = db.selectSQL(sb);
            int index = 0;
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    if (bmodel.configurationMasterHelper.IS_KPI_CALENDAR) {
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
        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }
        return monthNoList;


    }

    public ArrayList<String> getWeekList() {
        return weekList;
    }

    public void setWeekList(ArrayList<String> weekList) {
        this.weekList = weekList;
    }

    /**
     * @See {@link SellerDashboardDataManagerImpl#getKpiWeekList()}
     * @deprecated
     */
    @Deprecated
    public void getSellerKpiWeekList() {
        ArrayList<String> weekList = new ArrayList<>();
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        try {
            db.createDataBase();
            db.openDataBase();
            String sb = "SELECT distinct IntervalDesc AS Week FROM sellerkpi " +
                    "WHERE Interval=" + bmodel.QT(WEEK) +
                    " order by Week desc";
            Cursor c = db.selectSQL(sb);
            int index = 0;
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    //WEEKTYPE.valueOf(c.getString(0)).toString()
//                    String weekName = (c.getString(0).contains("wk"))?"Week " + c.getString(0).substring(c.getString(0).length()-1,
//                            c.getString(0).length()) : "";
//                    if(weekName.trim().length()>0) {
                    weekList.add(c.getString(0));
//                    }
                }
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }
        setWeekList(weekList);
    }

    public int getCurrentWeek() {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        try {
            db.createDataBase();
            db.openDataBase();
            String sb = "Select IntervalDesc from SellerKPI where " + bmodel.QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) + " between fromdate and todate and Interval = " + bmodel.QT(WEEK);
            Cursor c = db.selectSQL(sb);
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    //WEEKTYPE.valueOf(c.getString(0)).toString()
//                    int index = getKpiWeekList().indexOf((c.getString(0).contains("wk"))?"Week " + c.getString(0).substring(c.getString(0).length()-1,
//                            c.getString(0).length()) : "");
                    int index = getWeekList().indexOf(c.getString(0));
                    return index;
                }
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }
        return 0;
    }

    public ArrayList<String> getQuarterNameList() {
        ArrayList<String> quarterList = new ArrayList<>();
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        try {
            db.createDataBase();
            db.openDataBase();
            String sb = "SELECT distinct strftime('%m', replace(date,'/','-')) AS Month FROM dashboardmaster where Month Not null and type='QUARTER'" +
                    " union select distinct strftime('%m', replace(date,'/','-')) AS Month from skuwisetarget where Month Not null and freqtype='QUARTER'" +
                    " order by Month asc";
            Cursor c = db.selectSQL(sb);
            int lastMonth = -1;
            int monthInterval = 0;
            if (c.getCount() > 0) {
                int count = 0;
                while (c.moveToNext()) {
                    int month = SDUtil.convertToInt(c.getString(0)) - 1;
                    count++;

                    if (c.getCount() > 1) {
                        if (lastMonth >= 0) {
                            monthInterval = month - lastMonth;
                            quarterList.add(MONTH_NAME[lastMonth] + "-" + MONTH_NAME[month - 1]);
                        }
                        lastMonth = month;
                        if (count == c.getCount() && ((month + monthInterval) - 1) <= 12)
                            quarterList.add(MONTH_NAME[month] + "-" + MONTH_NAME[((month + monthInterval) - 1)]);
                    } else {
                        // single date- so assuming 3 month interval
                        if (month >= 0 && month <= 2)
                            quarterList.add(MONTH_NAME[0] + "-" + MONTH_NAME[2]);
                        else if (month >= 3 && month <= 5)
                            quarterList.add(MONTH_NAME[3] + "-" + MONTH_NAME[5]);
                        else if (month >= 6 && month <= 8)
                            quarterList.add(MONTH_NAME[6] + "-" + MONTH_NAME[8]);
                        else if (month >= 9 && month <= 11)
                            quarterList.add(MONTH_NAME[9] + "-" + MONTH_NAME[11]);
                    }


                }
            }

        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }
        return quarterList;


    }


    public void loadDashBoard(String retailerID) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            getDashboardMasterData().clear();
            Set<CharSequence> tempBeatSet = new LinkedHashSet<>();
            try {
                if (bmodel.labelsMasterHelper
                        .applyLabels(StandardListMasterConstants.MONTH) != null) {
                    tempBeatSet.add(bmodel.labelsMasterHelper
                            .applyLabels(StandardListMasterConstants.MONTH));
                } else {
                    tempBeatSet.add(StandardListMasterConstants.MONTH);
                }
            } catch (Exception e) {
                tempBeatSet.add(StandardListMasterConstants.MONTH);
                Commons.printException("" + e);
            }

            String sql = "SELECT text, tgt, ach,"
                    + " ROUND(CASE WHEN (100-((ach*100)/((tgt)*1.0))) < 0"
                    + " THEN 100 ELSE ((ach*100)/((tgt)*1.0)) END ,2) AS conv_ach_perc,"
                    + " code, type, routeid, BeatDescription, incentive,isFlip,IFNULL(strftime('%m', replace(date,'/','-')),0) AS Month,slm.flex1 FROM DashboardMaster"
                    + " INNER JOIN StandardListmaster slm on slm.listcode=code"
                    + " LEFT JOIN BeatMaster ON routeid = BeatID WHERE (type LIKE "
                    + "'MONTH' OR type like 'YEAR' OR type like 'DAY' OR type like 'QUARTER') AND retailerid=" + retailerID
                    + " AND isflip = 0 AND listType='TARGET_PARAMETERS' ORDER BY routeid, seq";
            Cursor c = db.selectSQL(sql);
            if (c != null) {
                while (c.moveToNext()) {
                    DashBoardBO sbo = new DashBoardBO();
                    sbo.setPId(0);
                    sbo.setText(c.getString(0));
                    sbo.setTarget(c.getDouble(1));
                    sbo.setAcheived(c.getDouble(2));

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
                    sbo.setCode(c.getString(4));
                    sbo.setType(c.getString(5));
                    sbo.setRouteID(c.getInt(6));
                    sbo.setBeatDescription(c.getString(7));
                    sbo.setIncentive(c.getDouble(8));
                    sbo.setIsFlip(c.getInt(9));
                    int value = SDUtil.convertToInt(c.getString(10));
                    if (value > 0 && value <= 12)
                        sbo.setMonthName(MONTH_NAME[value - 1]);

                    if (sbo.getBeatDescription() == null) {
                        sbo.setBeatDescription("");
                    } else {
                        tempBeatSet.add(c.getString(7));
                    }
                    sbo.setFlex1(c.getInt(11));

                    getDashboardMasterData().add(sbo);
                }
            }

            String sb = "SELECT B.pid, B.pname,IFNULL(A.tgt,0), IFNULL(A.ach,0)," +
                    " ROUND(CASE WHEN (100-((A.ach*100)/((A.tgt)*1.0))) < 0 THEN 100" +
                    " ELSE ((A.ach*100)/((A.tgt)*1.0)) END ,2) AS acheived," +
                    " IFNULL(D.ListCode,0), A.FreqType, A.routeid, C.BeatDescription, D.ListName,IFNULL(strftime('%m', replace(date,'/','-')),0) AS Month,D.flex1,IFNULL(A.RField,0) FROM SkuWiseTarget A" +
                    " INNER JOIN ProductMaster B ON A.pid = B.pid LEFT JOIN BeatMaster C ON A.routeid = C.BeatID" +
                    " LEFT JOIN StandardListMaster D on D.listid = A.Type WHERE (A.FreqType LIKE " +
                    "'MONTH' OR A.FreqType like 'YEAR' OR A.FreqType like 'QUARTER' OR A.FreqType like 'DAY') AND A.rid= " + retailerID + " AND A.Level = " + mMinLevel +
                    " ORDER BY A.routeid, B.pid";

            c = db.selectSQL(sb);

            if (c != null) {
                while (c.moveToNext()) {
                    DashBoardBO sbo = new DashBoardBO();
                    sbo.setPId(c.getInt(0));
                    sbo.setText(c.getString(1) + " " + c.getString(9));
                    sbo.setTarget(c.getDouble(2));
                    sbo.setAcheived(c.getDouble(3));

                    sbo.setCalculatedPercentage(c.getFloat(4));
                    if (sbo.getCalculatedPercentage() >= 100) {
                        sbo.setConvTargetPercentage(0);
                        sbo.setConvAcheivedPercentage(100);
                    } else {
                        sbo.setConvTargetPercentage(100 - sbo
                                .getCalculatedPercentage());
                        sbo.setConvAcheivedPercentage(sbo
                                .getCalculatedPercentage());
                    }
                    sbo.setCode(c.getString(5));
                    sbo.setType(c.getString(6));
                    sbo.setRouteID(c.getInt(7));
                    sbo.setBeatDescription(c.getString(8));
                    int value = SDUtil.convertToInt(c.getString(10));
                    if (value > 0 && value <= 12)
                        sbo.setMonthName(MONTH_NAME[value - 1]);

                    sbo.setIncentive(0);
                    sbo.setIsFlip(0);
                    if (sbo.getBeatDescription() == null) {
                        sbo.setBeatDescription("");
                    } else {
                        tempBeatSet.add(c.getString(8));
                    }
                    sbo.setFlex1(c.getInt(11));
                    sbo.setIncentive(c.getDouble(12));
                    getDashboardMasterData().add(sbo);
                }
                c.close();
            }

            beatList = new ArrayList<>(tempBeatSet);
            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    /**
     * @param data
     * @return
     * @See {@link SDUtil#getWholeNumber(String)}
     * @deprecated
     */
    @Deprecated
    public String getWhole(String data) {
        String wholeNumber;
        try {
            wholeNumber = data.substring(0, data.lastIndexOf("."));

        } catch (Exception e) {
            Commons.printException("" + e);
            return data;
        }
        return wholeNumber;
    }

    /**
     * @param retailerID
     * @param interval
     * @See {@link SellerDashboardDataManagerImpl#getRetailerDashboardForInterval(String, String)}
     * @deprecated
     */
    @Deprecated
    public void loadRetailerDashBoard(String retailerID, String interval) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            getDashboardMasterData().clear();

            String monthText = "";
            if (bmodel.configurationMasterHelper.IS_KPI_CALENDAR) {
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
                    + " where retailerid =" + retailerID + " and interval= '" + interval + "' "
                    + " order by DisplaySeq asc";
            Cursor c = db.selectSQL(sql);
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
                    sbo.setSubDataCount(getRetailerSubDataCount(sbo.getKpiTypeLovID()));
                    sbo.setCode(c.getString(9));
                    if (bmodel.configurationMasterHelper.IS_KPI_CALENDAR) {
                        sbo.setMonthName(c.getString(10));
                    } else {
                        int value = SDUtil.convertToInt(c.getString(10));
                        if (value > 0 && value <= 12)
                            sbo.setMonthName(MONTH_NAME[value - 1]);
                    }

                    getDashboardMasterData().add(sbo);
                }
                c.close();
            }
            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }


    @Deprecated
    /**
     * @deprecated
     * @see {@link SellerDashboardDataManagerImpl#getRouteDashboardForInterval(String)}
     */
    public void loadRouteDashBoard(String interval) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            getDashboardMasterData().clear();


            String sql = "SELECT SLM.ListName,RKD.Target,RKD.Achievement,"
                    + " ROUND(CASE WHEN (100-((RKD.Achievement*100)/((RKD.Target)*1.0))) < 0"
                    + " THEN 100 ELSE ((RKD.Achievement*100)/((RKD.Target)*1.0)) END ,2) AS conv_ach_perc"
                    + ",IFNULL(RKS.Score,0),IFNULL(RKS.Incentive,0),RK.KPIID,RKD.KPIParamLovId,SLM.Flex1,count(rkdd.KPIParamLovId),BeatDescription,SLM.ListCode as Code FROM RouteKPI RK"
                    + " inner join RouteKPIDetail RKD on RKD.KPIID= RK.KPIID"
                    + " LEFT join RouteKPIScore RKS on RKD.KPIID= RKS.KPIID and RKD.KPIParamLovId = RKS.KPIParamLovId"
                    + " inner join StandardListMaster SLM on SLM.Listid=RKD.KPIParamLovId"
                    + " LEFT join RouteKPISKUDetail rkdd on rkdd.KPIParamLovId =RKD.KPIParamLovId "
                    + " inner join BeatMaster on RouteID = BeatID "
                    + " where interval= '" + interval + "' "
                    + " AND "
                    + bmodel.QT(bmodel.userMasterHelper.getUserMasterBO().getDownloadDate())
                    + " between RK.fromdate and RK.todate group by SLM.Listid,RKD.KPiid order by DisplaySeq,RKD.KPiid asc";
            Cursor c = db.selectSQL(sql);
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
                    sbo.setCode(c.getString(c.getColumnIndex("Code")));
                    getDashboardMasterData().add(sbo);
                }
                c.close();
            }
            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public void findMinMaxProductLevelRetailerKPI(int kpiID, int kpiTypeLovID, String interval) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
        try {
            db.openDataBase();
            String sb = "select distinct (rksd.levelid),pl.sequence from RetailerKPISKUDetail rksd" +
                    " inner join Productlevel pl on pl.levelid =rksd.levelid where kpiid=" +
                    kpiID;

            Cursor c = db.selectSQL(sb);
            if (c != null) {
                if (c.getCount() > 0) {
                    while (c.moveToNext()) {
                        mSellerKpiMaxLevel = c.getInt(0);
                        mSellerKpiMaxSeqLevel = c.getInt(1);
                    }
                }
                c.close();
            }


            String sql = "select distinct (levelid),sequence from Productlevel where parentid=0";
            c = db.selectSQL(sql);
            if (c != null) {
                if (c.getCount() > 0) {
                    while (c.moveToNext()) {
                        mSellerKpiMinLevel = c.getInt(0);
                        mSellerKpiMinSeqLevel = c.getInt(1);
                    }
                }
                c.close();
            }


            downloadRetailerKpiSkuDetail(kpiID, kpiTypeLovID, mSellerKpiMinSeqLevel, mSellerKpiMaxSeqLevel, interval);

        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }

    }


    public void downloadRetailerKpiSkuDetail(int kpiID, int kpiTypeLovID, int parentLevel, int childLevel, String interval) {
        SKUWiseTargetBO temp;
        try {
            int loopEnd = childLevel - parentLevel + 1;

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            if (loopEnd == 0) {
                sb.append("SELECT P.PID, P.pname, P.psname, P.pcode, P.parentId,p.plid ,IFNULL(RKSD .kpiid,0), IFNULL(RKSD .Target,0)");
                sb.append(", IFNULL(RKSD .Achievement,0), ROUND(CASE WHEN (100-((RKSD .Achievement*100)/((RKSD ");
                sb.append(".Target)*1.0))) < 0 THEN 100 ELSE ((RKSD .Achievement*100)/((RKSD .Target)*1.0)) END ,2) AS acheived");
                sb.append(",pl.sequence  from ProductMaster p INNER JOIN RetailerKPISKUDetail RKSD ON RKSD.pid = p.pid and RKSD.kpiid=");
                sb.append(kpiID);
                sb.append(" and RKSD.KPIParamLovId=");
                sb.append(kpiTypeLovID);
                sb.append(" inner join  ProductLevel pl on pl.levelid = p.plid ");

                Cursor c = db.selectSQL(sb.toString());

                if (c != null) {
                    sellerKpiSku = new Vector<>();
                    kpiSkuMasterById = new HashMap<>();
                    while (c.moveToNext()) {
                        temp = new SKUWiseTargetBO();
                        temp.setPid(c.getInt(0));
                        temp.setProductName(c.getString(1));
                        temp.setProductShortName(c.getString(2));
                        temp.setProductCode(c.getString(3));
                        temp.setParentID(c.getInt(4));
                        temp.setLevelID(c.getInt(5));
                        temp.setKpiID(c.getInt(6));
                        temp.setTarget(c.getDouble(7));
                        temp.setAchieved(c.getDouble(8));
                        temp.setCalculatedPercentage(c.getFloat(9));
                        temp.setSequence(c.getInt(10));
                        if (temp.getCalculatedPercentage() >= 100) {
                            temp.setConvTargetPercentage(0);
                            temp.setConvAcheivedPercentage(100);
                        } else {
                            temp.setConvTargetPercentage(100 - temp
                                    .getCalculatedPercentage());
                            temp.setConvAcheivedPercentage(temp
                                    .getCalculatedPercentage());
                        }

                        sellerKpiSku.add(temp);
                        kpiSkuMasterById.put(new Integer(temp.getPid()), new Integer(temp.getParentID()));
                    }
                    c.close();
                }
                if (interval.equalsIgnoreCase("DAY")) {
                    calculateDayAcheivement(true);
                }
            } else {

                Commons.print("Loop ENd," + "" + loopEnd);
                if (interval.equalsIgnoreCase("DAY")) {
                    sb.append("SELECT P1.PID, P1.pname, P1.psname, P1.pcode, P1.parentId,p1.plid ,IFNULL(RKSD .kpiid,0), IFNULL(RKSD .Target,0)");
                    sb.append(", IFNULL(RKSD .Achievement,0), ROUND(CASE WHEN (100-((RKSD .Achievement*100)/((RKSD ");
                    sb.append(".Target)*1.0))) < 0 THEN 100 ELSE ((RKSD .Achievement*100)/((RKSD .Target)*1.0)) END ,2) AS acheived");
                    sb.append(",pl.sequence  from ProductMaster p1 INNER JOIN RetailerKPISKUDetail RKSD ON RKSD.pid = p1.pid and RKSD.kpiid=");
                    sb.append(kpiID);
                    sb.append(" and RKSD.KPIParamLovId=");
                    sb.append(kpiTypeLovID);

                    sb.append(" left join OrderDetail OD on OD.ProductID = RKSD.PiD inner join  ProductLevel pl on pl.levelid = p1.plid " +
                            "GROUP BY P1.PID, P1.pname, P1.psname, P1.pcode, P1.parentId, p1.plid ");
                    //sb.append(" inner join  ProductLevel pl on pl.levelid = p1.plid ");

                    for (int i = 2; i <= loopEnd; i++) {
                        Commons.print("Loop I," + "" + i);
                        sb.append("UNION ");
                        sb.append("SELECT P");
                        sb.append(i);
                        sb.append(".PID, P");
                        sb.append(i);
                        sb.append(".pname, P");
                        sb.append(i);
                        sb.append(".psname, P");
                        sb.append(i);
                        sb.append(".pcode, P");
                        sb.append(i);
                        sb.append(".parentId, p");
                        sb.append(i);
                        sb.append(".plid");
                        sb.append(",IFNULL(RKSD .kpiid,0),SUM(IFNULL(RKSD .Target,0)),SUM(IFNULL(RKSD .Achievement,0)),");
                        sb.append("ROUND(CASE WHEN (100-((SUM(RKSD .Achievement)*100)/((SUM(RKSD .Target))*1.0))) < ");
                        sb.append("0 THEN 100 ELSE ((SUM(RKSD .Achievement*100))/((SUM(RKSD .Target))*1.0)) END ,2) AS acheived,pl.sequence from RetailerKPISKUDetail RKSD  ");
                        sb.append("INNER JOIN ProductMaster p1 ON RKSD.pid = p1.pid and RKSD.kpiid=");
                        sb.append(kpiID);
                        sb.append(" and RKSD.KPIParamLovId=");
                        sb.append(kpiTypeLovID);
                        for (int j = 2; j <= i; j++) {
                            sb.append(" INNER JOIN  ProductMaster p");
                            sb.append(j);
                            sb.append(" on p");
                            sb.append((j - 1));
                            sb.append(".parentid=p");
                            sb.append(j);
                            sb.append(".pid ");
                        }

                        sb.append(" inner join  ProductLevel pl on pl.levelid = p");
                        sb.append(i);
                        sb.append(".plid");
                        sb.append(" GROUP BY P");
                        sb.append(i);
                        sb.append(".PID, P");
                        sb.append(i);
                        sb.append(".pname, P");
                        sb.append(i);
                        sb.append(".psname, P");
                        sb.append(i);
                        sb.append(".pcode, P");
                        sb.append(i);
                        sb.append(".parentId, p");
                        sb.append(i);
                        sb.append(".plid ");

                    }
                } else {
                    sb.append("SELECT P1.PID, P1.pname, P1.psname, P1.pcode, P1.parentId,p1.plid ,IFNULL(RKSD .kpiid,0), IFNULL(RKSD .Target,0)");
                    sb.append(", IFNULL(RKSD .Achievement,0), ROUND(CASE WHEN (100-((RKSD .Achievement*100)/((RKSD ");
                    sb.append(".Target)*1.0))) < 0 THEN 100 ELSE ((RKSD .Achievement*100)/((RKSD .Target)*1.0)) END ,2) AS acheived");
                    sb.append(",pl.sequence  from ProductMaster p1 INNER JOIN RetailerKPISKUDetail RKSD ON RKSD.pid = p1.pid and RKSD.kpiid=");
                    sb.append(kpiID);
                    sb.append(" and RKSD.KPIParamLovId=");
                    sb.append(kpiTypeLovID);
                    sb.append(" inner join  ProductLevel pl on pl.levelid = p1.plid GROUP BY P1.PID, P1.pname, P1.psname, P1.pcode, P1.parentId, p1.plid");

                    for (int i = 2; i <= loopEnd; i++) {
                        Commons.print("Loop I," + "" + i);
                        sb.append("UNION ");
                        sb.append("SELECT P");
                        sb.append(i);
                        sb.append(".PID, P");
                        sb.append(i);
                        sb.append(".pname, P");
                        sb.append(i);
                        sb.append(".psname, P");
                        sb.append(i);
                        sb.append(".pcode, P");
                        sb.append(i);
                        sb.append(".parentId, p");
                        sb.append(i);
                        sb.append(".plid");
                        sb.append(",IFNULL(RKSD .kpiid,0),SUM(IFNULL(RKSD .Target,0)),SUM(IFNULL(RKSD .Achievement,0)),");
                        sb.append("ROUND(CASE WHEN (100-((SUM(RKSD .Achievement)*100)/((SUM(RKSD .Target))*1.0))) < ");
                        sb.append("0 THEN 100 ELSE ((SUM(RKSD .Achievement*100))/((SUM(RKSD .Target))*1.0)) END ,2) AS acheived,pl.sequence from RetailerKPISKUDetail RKSD  ");
                        sb.append("INNER JOIN ProductMaster p1 ON RKSD.pid = p1.pid and RKSD.kpiid=");
                        sb.append(kpiID);
                        sb.append(" and RKSD.KPIParamLovId=");
                        sb.append(kpiTypeLovID);
                        for (int j = 2; j <= i; j++) {
                            sb.append(" INNER JOIN  ProductMaster p");
                            sb.append(j);
                            sb.append(" on p");
                            sb.append((j - 1));
                            sb.append(".parentid=p");
                            sb.append(j);
                            sb.append(".pid ");
                        }

                        sb.append(" inner join  ProductLevel pl on pl.levelid = p");
                        sb.append(i);
                        sb.append(".plid");
                        sb.append(" GROUP BY P");
                        sb.append(i);
                        sb.append(".PID, P");
                        sb.append(i);
                        sb.append(".pname, P");
                        sb.append(i);
                        sb.append(".psname, P");
                        sb.append(i);
                        sb.append(".pcode, P");
                        sb.append(i);
                        sb.append(".parentId, p");
                        sb.append(i);
                        sb.append(".plid ");

                    }

                }

                Cursor c = db.selectSQL(sb.toString());

                if (c != null) {
                    sellerKpiSku = new Vector<>();
                    kpiSkuMasterById = new HashMap<>();
                    while (c.moveToNext()) {
                        temp = new SKUWiseTargetBO();
                        temp.setPid(c.getInt(0));
                        temp.setProductName(c.getString(1));
                        temp.setProductShortName(c.getString(2));
                        temp.setProductCode(c.getString(3));
                        temp.setParentID(c.getInt(4));
                        temp.setLevelID(c.getInt(5));
                        temp.setKpiID(c.getInt(6));
                        temp.setTarget(c.getDouble(7));
                        temp.setAchieved(c.getDouble(8));
                        temp.setCalculatedPercentage(c.getFloat(9));
                        temp.setSequence(c.getInt(10));
                        if (temp.getCalculatedPercentage() >= 100) {
                            temp.setConvTargetPercentage(0);
                            temp.setConvAcheivedPercentage(100);
                        } else {
                            temp.setConvTargetPercentage(100 - temp
                                    .getCalculatedPercentage());
                            temp.setConvAcheivedPercentage(temp
                                    .getCalculatedPercentage());
                        }

                        sellerKpiSku.add(temp);
                        kpiSkuMasterById.put(new Integer(temp.getPid()), new Integer(temp.getParentID()));
                    }
                    c.close();
                }
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }


    // get a interval from DB
    public ArrayList<String> getDashList(boolean isRetailer) {

        ArrayList<String> dashList = new ArrayList<>();
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            String sql;
            if (!isRetailer)
                sql = "select distinct interval from SellerKPI";
            else
                sql = "select distinct interval from RetailerKPI where RetailerId=" + bmodel.getRetailerMasterBO().getRetailerID();
            Cursor c = db.selectSQL(sql);
            if (c != null) {
                while (c.moveToNext()) {
                    dashList.add(c.getString(0));
                }

                c.close();
            }
            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);
        }
        return dashList;
    }

    @Deprecated
    /**
     * @deprecated
     * @See {@link SellerDashboardDataManagerImpl#getRouteDashList()}
     */
    public ArrayList<String> getRouteDashList() {

        ArrayList<String> dashList = null;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            String sql;
            sql = "select distinct interval from RouteKPI";
            Cursor c = db.selectSQL(sql);
            if (c != null) {
                dashList = new ArrayList<>();
                while (c.moveToNext()) {
                    dashList.add(c.getString(0));
                }

                c.close();
            }
            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return dashList;
    }

    private void getMonths(String month) {
        String tempMonth;
        int mon;

        if (month.startsWith("0"))
            tempMonth = month.replace("0", "");
        else
            tempMonth = month;

        mon = SDUtil.convertToInt(tempMonth) - 1;

        DashBoardBO temp;

        temp = new DashBoardBO();
        temp.setMonthID(month);
        temp.setMonthName(MONTH_NAME[mon].substring(0, 3));

        Commons.print("Month No>>>>," + "" + temp.getMonthID() + " Month Name>>>>" + temp.getMonthName());

        monthList.add(temp);


    }

    public void loadSellerDashBoardReport(String userid) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            getDashboardMasterData().clear();


            String sql = "select listname,listid,listcode from StandardListMaster where listtype= 'TARGET_PARAMETERS' and listcode in ('AC' ,'CM' ,'COV' ,'JPA' ,'TV', 'VC')";

            kpiList = new ArrayList<>();

            Cursor c = db.selectSQL(sql);
            if (c != null) {
                while (c.moveToNext()) {
                    DashBoardBO temp = new DashBoardBO();
                    temp.setText(c.getString(0));
                    temp.setKpiTypeLovID(c.getInt(1));
                    temp.setKpiCode(c.getString(2));
                    kpiList.add(temp);
                }
                c.close();
            }

            sql = "select distinct substr(fromDate,6,2) as month from SellerKPI order by month desc";

            monthList = new Vector<>();
            c = db.selectSQL(sql);
            if (c != null) {
                while (c.moveToNext()) {
                    getMonths(c.getString(0));
                }
                c.close();
            }

            String tempMonthId = "-1";
            int monthIndex = -1;

            setDashBoardReportList(new Vector<DashBoardBO>());

            sql = "SELECT SLM.ListName,Target,Achievement,"
                    + " ROUND(CASE WHEN (100-((Achievement*100)/((Target)*1.0))) < 0"
                    + " THEN 100 ELSE ((Achievement*100)/((Target)*1.0)) END ,2) AS conv_ach_perc"
                    + ",SK.KPIID,SKD.KPIParamLovId,substr (FromDate, 6,2) as Month FROM SellerKPI SK"
                    + " inner join SellerKPIDetail SKD on SKD.KPIID= SK.KPIID"
                    + " LEFT join SellerKPIScore SKS on SKD.KPIID= SKS.KPIID and SKD.KPIParamLovId = SKS.KPIParamLovId"
                    + " inner join StandardListMaster SLM on SLM.Listid=SKD.KPIParamLovId"
                    + " where userid =" + userid + " and interval= 'MONTH' "
                    + " order by month  asc";

            c = db.selectSQL(sql);
            if (c != null) {
                while (c.moveToNext()) {
                    if (!tempMonthId.equals(c.getString(6))) {

                        monthIndex = monthIndex + 1;

                        tempMonthId = c.getString(6);

                        DashBoardBO sbo = new DashBoardBO();
                        sbo.setPId(0);
                        sbo.setText(c.getString(0));
                        sbo.setTarget(c.getDouble(1));
                        sbo.setAcheived(c.getDouble(2));


                        sbo.setCalculatedPercentage(c.getFloat(3));

                        sbo.setKpiID(c.getInt(4));
                        sbo.setKpiTypeLovID(c.getInt(5));
                        sbo.setMonthID(c.getString(6));
                        ArrayList<String> arr = new ArrayList<>();
                        arr.add(c.getDouble(1) + "");
                        arr.add(c.getDouble(2) + "");
                        arr.add(c.getDouble(3) + "");
                        sbo.getMonthKpiList().put(c.getString(5), arr);
                        getDashBoardReportList().add(sbo);
                    } else {
                        ArrayList<String> arr = new ArrayList<>();
                        arr.add(c.getDouble(1) + "");
                        arr.add(c.getDouble(2) + "");
                        arr.add(c.getDouble(3) + "");

                        getDashBoardReportList().get(monthIndex).getMonthKpiList().put(c.getString(5), arr);
                    }
                }
                c.close();
            }

            // For putting 0 in values for the non exsisting KPIS

            Set kpiListwithValue;
            for (DashBoardBO kpiResultList : getDashBoardReportList()) {
                for (DashBoardBO monthList : getMonthList()) {

                    Vector<String> tempKpiList = new Vector<>();
                    if (monthList.getMonthID().equals(kpiResultList.getMonthID())) {
                        kpiListwithValue = kpiResultList.getMonthKpiList().entrySet();


                        Iterator mapiterator = kpiListwithValue.iterator();

                        while (mapiterator.hasNext()) {

                            Map.Entry mapEntry = (Map.Entry) mapiterator.next();
                            String keyValue = (String) mapEntry.getKey();
                            tempKpiList.add(keyValue);

                        }
                        for (DashBoardBO kpiList : getKpiList()) {
                            if (!tempKpiList.contains(kpiList.getKpiTypeLovID() + "")) {
                                ArrayList<String> arr = new ArrayList<>();
                                arr.add("0");
                                arr.add("0");
                                arr.add("0");
                                kpiResultList.getMonthKpiList().put(kpiList.getKpiTypeLovID() + "", arr);
                            }
                        }
                    }
                }
            }


            // For adding footer values for KPIS

            for (DashBoardBO kpiList : getKpiList()) {

                float target = 0, achieved = 0, percentage = 0;
                int avg = 0;
                String finalTarget, finalAchieved;

                if (kpiList.getKpiCode().equals("CM")) {
                    for (DashBoardBO kpiResultList : getDashBoardReportList()) {


                        kpiListwithValue = kpiResultList.getMonthKpiList().entrySet();


                        Iterator mapiterator = kpiListwithValue.iterator();

                        while (mapiterator.hasNext()) {
                            Map.Entry mapEntry = (Map.Entry) mapiterator.next();
                            String keyValue = (String) mapEntry.getKey();
                            ArrayList<String> value = (ArrayList<String>) mapEntry.getValue();
                            if (kpiList.getKpiTypeLovID() == SDUtil.convertToInt(keyValue)) {

                                target = SDUtil.convertToFloat(value.get(0));
                                achieved = SDUtil.convertToFloat(value.get(1));
                                percentage = SDUtil.convertToFloat(value.get(2));
                            }
                        }
                        finalTarget = SDUtil.roundIt((target), 2) + "";
                        finalAchieved = SDUtil.roundIt((achieved), 2) + "";
                        kpiList.setKpiTarget(finalTarget);
                        kpiList.setKpiAcheived(finalAchieved);
                        kpiList.setCalculatedPercentage(percentage);

                    }
                } else {

                    for (DashBoardBO kpiResultList : getDashBoardReportList()) {


                        kpiListwithValue = kpiResultList.getMonthKpiList().entrySet();


                        Iterator mapiterator = kpiListwithValue.iterator();

                        while (mapiterator.hasNext()) {

                            Map.Entry mapEntry = (Map.Entry) mapiterator.next();
                            String keyValue = (String) mapEntry.getKey();
                            ArrayList<String> value = (ArrayList<String>) mapEntry.getValue();
                            if (kpiList.getKpiTypeLovID() == SDUtil.convertToInt(keyValue)) {

                                if (!value.get(0).equals("0")) {

                                    target = target + SDUtil.convertToFloat(value.get(0));
                                    achieved = achieved + SDUtil.convertToFloat(value.get(1));

                                    avg++;

                                }
                            }


                        }

                        if (kpiList.getKpiCode().equals("AC") || kpiList.getKpiCode().equals("VC")) {
                            finalTarget = SDUtil.roundIt((target / avg), 2) + "";
                            finalAchieved = SDUtil.roundIt((achieved / avg), 2) + "";
                            kpiList.setKpiTarget(finalTarget);
                            kpiList.setKpiAcheived(finalAchieved);
                            kpiList.setCalculatedPercentage((achieved / avg) / (target / avg) * 100);

                        } else {
                            finalTarget = SDUtil.roundIt((target), 2) + "";
                            finalAchieved = SDUtil.roundIt((achieved), 2) + "";
                            kpiList.setKpiTarget(finalTarget);
                            kpiList.setKpiAcheived(finalAchieved);
                            kpiList.setCalculatedPercentage((achieved) / (target) * 100);
                        }

                    }
                }
            }

            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }


    public Vector<DashBoardBO> getMonthList() {
        return monthList;
    }

    public ArrayList<DashBoardBO> getKpiList() {
        return kpiList;
    }

    /////////SELLER DAHBOARD///////////


    @Deprecated
    /**
     *
     * @deprecated
     * @See {@link SellerDashboardDataManagerImpl#getSellerDashboardForInterval(String, String)}
     * @param userid
     * @param interval
     */
    public void loadSellerDashBoard(String userid, String interval) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            getDashboardMasterData().clear();
            //mParamAchieved = 0;

            String sql = "SELECT SLM.ListName,SKD.Target,ifnull(SKD.Achievement,0),"
                    + " ROUND(CASE WHEN (100-((SKD.Achievement*100)/((SKD.Target)*1.0))) < 0"
                    + " THEN 100 ELSE ((SKD.Achievement*100)/((SKD.Target)*1.0)) END ,2) AS conv_ach_perc"
                    + ",IFNULL(SKS.Score,0),IFNULL(SKS.Incentive,0),SK.KPIID,SKD.KPIParamLovId,SLM.Flex1,count(SKDD.KPIParamLovId),SLM.ListCode,IFNULL(strftime('%m', replace(fromdate,'/','-')),0),SKD.Flex1 AS kpiFlex1 FROM SellerKPI SK"
                    + " inner join SellerKPIDetail SKD on SKD.KPIID= SK.KPIID"
                    + " LEFT join SellerKPIScore SKS on SKD.KPIID= SKS.KPIID and SKD.KPIParamLovId = SKS.KPIParamLovId"
                    + " inner join StandardListMaster SLM on SLM.Listid=SKD.KPIParamLovId"
                    + " LEFT join SellerKPISKUDetail skdd on skdd.KPIParamLovId =SKD.KPIParamLovId "
                    + " where userid = "
                    + bmodel.QT(userid)
                    + " and interval= "
                    + bmodel.QT(interval)
                    + " AND "
                    + bmodel.QT(bmodel.userMasterHelper.getUserMasterBO().getDownloadDate())
                    + " between SK.fromdate and SK.todate "
                    + (userid.equals("0") ? " and SK.isSummary=1" : "")
                    + " group by SLM.Listid order by DisplaySeq asc";
            Cursor c = db.selectSQL(sql);
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
//                    if (!c.getString(10).equals("INV")) {
                    getDashboardMasterData().add(sbo);
//                    } else {
//                        mParamAchieved = Double.parseDouble(sbo.getKpiAcheived());
//                    }
                }
                c.close();
            }
            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    //for Week Dashboard
    @Deprecated
    /**
     * @deprecated
     * @See {@link SellerDashboardDataManagerImpl#getSellerDashboardForWeek(String)}
     */
    public void loadSellerDashBoardforWeek(String userid) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            getDashboardMasterData().clear();
            //mParamAchieved = 0;

            String sql = "SELECT SLM.ListName,SKD.Target,ifnull(SKD.Achievement,0),"
                    + " ROUND(CASE WHEN (100-((SKD.Achievement*100)/((SKD.Target)*1.0))) < 0"
                    + " THEN 100 ELSE ((SKD.Achievement*100)/((SKD.Target)*1.0)) END ,2) AS conv_ach_perc"
                    + ",IFNULL(SKS.Score,0),IFNULL(SKS.Incentive,0),SK.KPIID,SKD.KPIParamLovId,SLM.Flex1,count(SKDD.KPIParamLovId),SLM.ListCode,SK.IntervalDesc,SKD.Flex1 AS kpiFlex1 FROM SellerKPI SK"
                    + " inner join SellerKPIDetail SKD on SKD.KPIID= SK.KPIID"
                    + " LEFT join SellerKPIScore SKS on SKD.KPIID= SKS.KPIID and SKD.KPIParamLovId = SKS.KPIParamLovId"
                    + " inner join StandardListMaster SLM on SLM.Listid=SKD.KPIParamLovId"
                    + " LEFT join SellerKPISKUDetail skdd on skdd.KPIParamLovId =SKD.KPIParamLovId "
                    + " where userid = "
                    + bmodel.QT(userid)
                    + " and interval= 'WEEK'"
                    + " group by SLM.Listid,SK.IntervalDesc order by DisplaySeq asc";
            Cursor c = db.selectSQL(sql);
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
                    getDashboardMasterData().add(sbo);
                }
                c.close();
            }
            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    //for P3m Purpose
    @Deprecated
    /**
     * @deprecated
     * @See {@link com.ivy.ui.dashboard.data.SellerDashboardDataManager#getP3MSellerDashboardData(String)}
     */
    public void loadSellerDashBoard(String userid) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            getDashboardMasterData().clear();
            //mParamAchieved = 0;

            String monthText = "";
            if (bmodel.configurationMasterHelper.IS_KPI_CALENDAR) {
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
                    + bmodel.QT(userid)
                    + " and interval= 'P3M' "
                    + (userid.equals("0") ? " and SK.isSummary=1" : "")
                    + " order by DisplaySeq asc";
            Cursor c = db.selectSQL(sql);
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
                    sbo.setSubDataCount(getSubdataCount(sbo.getKpiTypeLovID()));
                    sbo.setCode(c.getString(9));
                    sbo.setKpiFlex(c.getString(c.getColumnIndex("kpiFlex1")));
                    if (bmodel.configurationMasterHelper.IS_KPI_CALENDAR) {
                        sbo.setMonthName(c.getString(10));
                    } else {
                        int value = SDUtil.convertToInt(c.getString(10));
                        if (value > 0 && value <= 12)
                            sbo.setMonthName(MONTH_NAME[value - 1]);
                    }
//                    if (!c.getString(9).equals("INV")) {
                    getDashboardMasterData().add(sbo);
//                    } else {
//                        mParamAchieved = Double.parseDouble(sbo.getKpiAcheived());
//                    }
                }
                c.close();
            }
            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public int getSubdataCount(int KPIParamLovId) {
        int count = 0;
        try {
            showDayAndP3MSpinner = 0;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            String sql = "select count(*) from SellerKPISKUDetail where KPIParamLovId = " + KPIParamLovId;
            Cursor c = db.selectSQL(sql);
            if (c != null) {
                while (c.moveToNext()) {
                    count = c.getInt(0);

                }
                c.close();
            }
            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
            return 0;
        }

        return count;
    }

    private int getRetailerSubDataCount(int KPIParamLovId) {
        int count = 0;
        try {
            showDayAndP3MSpinner = 0;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            String sql = "select count(*) from RetailerKPISKUDetail where KPIParamLovId = " + KPIParamLovId;
            Cursor c = db.selectSQL(sql);
            if (c != null) {
                while (c.moveToNext()) {
                    count = c.getInt(0);

                }
                c.close();
            }
            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
            return 0;
        }

        return count;
    }

    //Only for loading trend chart
    @Deprecated
    /**
     * @deprecated
     * @See {@link SellerDashboardDataManagerImpl#getP3MTrendChart(String)}
     */
    public void loadP3MTrendChaart(String userid) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            getP3mChartList().clear();

            String monthText = "";
            if (bmodel.configurationMasterHelper.IS_KPI_CALENDAR) {
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
                    + bmodel.QT(userid)
                    + " and interval= 'P3M' "
                    + (userid.equals("0") ? " and SK.isSummary=1" : "")
                    + " order by DisplaySeq asc";
            Cursor c = db.selectSQL(sql);
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
                    sbo.setSubDataCount(getSubdataCount(sbo.getKpiTypeLovID()));
                    sbo.setCode(c.getString(9));
                    if (bmodel.configurationMasterHelper.IS_KPI_CALENDAR) {
                        sbo.setMonthName(c.getString(10));
                    } else {
                        int value = SDUtil.convertToInt(c.getString(10));
                        if (value > 0 && value <= 12)
                            sbo.setMonthName(MONTH_NAME[value - 1]);
                    }
                    getP3mChartList().add(sbo);
                }
                if (getP3mChartList().size() > 0) {
                    Collections.sort(getP3mChartList(), paramsIDComparator);
                    mParamLovId = getP3mChartList().get(0).getKpiTypeLovID();
                }
                c.close();
            }
            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public void checkDayAndP3MSpinner(boolean isRetailerDashboard) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            boolean isDaySpinner = false;
            boolean isP3MSpinner = false;

            String tableName = "SellerKPI";
            if (isRetailerDashboard)
                tableName = "RetailerKPI";

            String sql = "select count(*) from " + tableName + " where interval = 'DAY'";
            Cursor c = db.selectSQL(sql);
            if (c != null) {
                while (c.moveToNext()) {
                    int count = c.getInt(0);
                    if (count > 0) {
                        isDaySpinner = true;
                    }
                }
            }
            sql = "select count(*) from " + tableName + " where interval = 'P3M'";
            c = db.selectSQL(sql);
            if (c != null) {
                while (c.moveToNext()) {
                    int count = c.getInt(0);
                    if (count > 0) {
                        isP3MSpinner = true;
                    }
                }
                c.close();
            }
            db.closeDB();

            if (isDaySpinner && isP3MSpinner)
                showDayAndP3MSpinner = 3;
            else if (isDaySpinner)
                showDayAndP3MSpinner = 2;
            else if (isP3MSpinner)
                showDayAndP3MSpinner = 1;


        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }


    public Vector<SKUWiseTargetBO> getSellerKpiSku() {
        return sellerKpiSku;
    }


    public void findMinMaxProductLevelSellerKPI(int kpiID, int kpiTypeLovID, String interval) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
        ;
        try {
            db.openDataBase();
            String sb = "select distinct (Sksd.levelid),pl.sequence from SellerKPISKUDetail sksd" +
                    " inner join Productlevel pl on pl.levelid =sksd.levelid where kpiid=" + kpiID;

            Cursor c = db.selectSQL(sb);
            if (c != null) {
                if (c.getCount() > 0) {
                    while (c.moveToNext()) {
                        mSellerKpiMaxLevel = c.getInt(0);
                        mSellerKpiMaxSeqLevel = c.getInt(1);
                    }
                }
                c.close();
            }


            String sql = "select distinct (levelid),sequence from Productlevel where parentid=0";
            c = db.selectSQL(sql);
            if (c != null) {
                if (c.getCount() > 0) {
                    while (c.moveToNext()) {
                        mSellerKpiMinLevel = c.getInt(0);
                        mSellerKpiMinSeqLevel = c.getInt(1);
                    }
                }
                c.close();
            }


            downloadSellerKpiSkuDetail(kpiID, kpiTypeLovID, mSellerKpiMinSeqLevel, mSellerKpiMaxSeqLevel, interval);

        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }

    }

    public void calculateDayAcheivement(boolean isRetailer) {
        int maxLevel = 0, minLevel = 0;
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
        db.openDataBase();
        StringBuilder sb = new StringBuilder();
        sb.append("select Distinct p.PLid,pl.sequence from productmaster p ");
        sb.append(" inner join Productlevel pl on pl.levelid =p.PLid where P.PID in ");

        if (isRetailer)
            sb.append("(select distinct(ProductID) from OrderDetail where retailerid=" + bmodel.getRetailerMasterBO().getRetailerID() + ")");
        else
            sb.append("(select distinct(ProductID) from OrderDetail)");

        Cursor c = db.selectSQL(sb.toString());
        if (c != null) {
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    maxLevel = c.getInt(1);
                }
            }
            c.close();
        }


        String sql = "select distinct (levelid),sequence from Productlevel where parentid=0";
        c = db.selectSQL(sql);
        if (c != null) {
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    minLevel = c.getInt(1);
                }
            }
            c.close();
        }


        int loopEnd = maxLevel - minLevel + 1;

        sb = new StringBuilder();
        sb.append(" SELECT PM");
        sb.append(loopEnd);
        sb.append(".PId, SUM (totalamount) FROM OrderDetail OD");
        sb.append(" INNER JOIN ProductMaster PM1 ON PM1.PId = OD.ProductId");

        for (int i = 2; i <= loopEnd; i++) {
            sb.append(" INNER JOIN ProductMaster PM");
            sb.append(i);
            sb.append(" ON PM");
            sb.append(i);
            sb.append(".PId = PM");
            sb.append((i - 1));
            sb.append(".ParentId");

        }

        if (isRetailer)
            sb.append(" where OD.retailerid=" + bmodel.getRetailerMasterBO().getRetailerID());

        sb.append(" GROUP BY PM");
        sb.append(loopEnd);
        sb.append(".PId");

        c = db.selectSQL(sb.toString());
        if (c != null) {
            while (c.moveToNext()) {

                for (SKUWiseTargetBO sBO : sellerKpiSku) {
                    if (c.getInt(0) == sBO.getPid()) {
                        sBO.setAchieved(c.getInt(1));


                        sBO.setCalculatedPercentage(SDUtil.convertToFloat(SDUtil.format(((sBO.getAchieved() / sBO.getTarget()) * 100),
                                bmodel.configurationMasterHelper.VALUE_PRECISION_COUNT,
                                0, bmodel.configurationMasterHelper.IS_DOT_FOR_GROUP)));
                        if (sBO.getCalculatedPercentage() >= 100) {
                            sBO.setConvTargetPercentage(0);
                            sBO.setConvAcheivedPercentage(100);
                        } else {
                            sBO.setConvTargetPercentage(100 - sBO
                                    .getCalculatedPercentage());
                            sBO.setConvAcheivedPercentage(sBO
                                    .getCalculatedPercentage());
                        }
                    }

                }


            }
            c.close();
        }


    }


    public void downloadSellerKpiSkuDetail(int kpiID, int kpiTypeLovID, int parentLevel, int childLevel, String interval) {
        SKUWiseTargetBO temp;
        try {
            int loopEnd = childLevel - parentLevel + 1;

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            if (loopEnd == 0) {
                sb.append("SELECT P.PID, P.pname, P.psname, P.pcode, P.parentId,p.plid ,IFNULL(SKSD .kpiid,0), IFNULL(SKSD .Target,0)");
                sb.append(", IFNULL(SKSD .Achievement,0), ROUND(CASE WHEN (100-((SKSD .Achievement*100)/((SKSD ");
                sb.append(".Target)*1.0))) < 0 THEN 100 ELSE ((SKSD .Achievement*100)/((SKSD .Target)*1.0)) END ,2) AS acheived,pl.sequence  from ProductMaster p");
                sb.append(" INNER JOIN SellerKPISKUDetail SKSD ON SKSD .pid = p.pid and SKSD.kpiid=");
                sb.append(kpiID);
                sb.append(" and SKSD.KPIParamLovId=");
                sb.append(kpiTypeLovID);
                sb.append(" inner join  ProductLevel pl on pl.levelid = p.plid ");

                Cursor c = db.selectSQL(sb.toString());

                if (c != null) {
                    sellerKpiSku = new Vector<>();
                    kpiSkuMasterById = new HashMap<>();
                    while (c.moveToNext()) {
                        temp = new SKUWiseTargetBO();
                        temp.setPid(c.getInt(0));
                        temp.setProductName(c.getString(1));
                        temp.setProductShortName(c.getString(2));
                        temp.setProductCode(c.getString(3));
                        temp.setParentID(c.getInt(4));
                        temp.setLevelID(c.getInt(5));
                        temp.setKpiID(c.getInt(6));
                        temp.setTarget(c.getDouble(7));
                        temp.setAchieved(c.getDouble(8));
                        temp.setCalculatedPercentage(c.getFloat(9));
                        temp.setSequence(c.getInt(10));
                        if (temp.getCalculatedPercentage() >= 100) {
                            temp.setConvTargetPercentage(0);
                            temp.setConvAcheivedPercentage(100);
                        } else {
                            temp.setConvTargetPercentage(100 - temp
                                    .getCalculatedPercentage());
                            temp.setConvAcheivedPercentage(temp
                                    .getCalculatedPercentage());
                        }

                        sellerKpiSku.add(temp);
                        kpiSkuMasterById.put(new Integer(temp.getPid()), new Integer(temp.getParentID()));
                    }
                    c.close();
                }
                if (interval.equals("DAY")) {
                    calculateDayAcheivement(false);
                }

            } else {
                //Anand Asir
                //For Calcualting Achievement for a day - Joined with Order Detail Table
                if (interval.equals("DAY")) {
                    sb.append("SELECT P1.PID, P1.pname, P1.psname, P1.pcode, P1.parentId,p1.plid ,IFNULL(SKSD .kpiid,0), IFNULL(SKSD .Target,0)");
                    sb.append(", IFNULL(sum(0+OD.totalamount),0), ROUND(CASE WHEN (100-((OD.totalamount*100)/((SKSD ");
                    sb.append(".Target)*1.0))) < 0 THEN 100 ELSE ((OD.totalamount*100)/((SKSD .Target)*1.0)) END ,2) AS acheived,pl.sequence  from ProductMaster p1");
                    sb.append(" INNER JOIN SellerKPISKUDetail SKSD ON SKSD .pid = p1.pid and SKSD.kpiid=");
                    sb.append(kpiID);
                    sb.append(" and SKSD.KPIParamLovId=");
                    sb.append(kpiTypeLovID);
                    sb.append(" left join OrderDetail OD on OD.ProductID = (Select ProductId  from OrderDetail inner join ProductMaster on Pid = ProductID and parenthierarchy like '%/' || SKSD .pid || '/%') inner join  ProductLevel pl on pl.levelid = p1.plid " +
                            "GROUP BY P1.PID, P1.pname, P1.psname, P1.pcode, P1.parentId, p1.plid ");
                    for (int i = 2; i <= loopEnd; i++) {
                        Commons.print("Loop I," + "" + i);
                        sb.append("UNION ");
                        sb.append("SELECT P");
                        sb.append(i);
                        sb.append(".PID, P");
                        sb.append(i);
                        sb.append(".pname, P");
                        sb.append(i);
                        sb.append(".psname, P");
                        sb.append(i);
                        sb.append(".pcode, P");
                        sb.append(i);
                        sb.append(".parentId, p");
                        sb.append(i);
                        sb.append(".plid,IFNULL(SKSD .kpiid,0),SUM(IFNULL(SKSD .Target,0))");
                        sb.append(",IFNULL(sum(0+OD.totalamount),0),ROUND(CASE WHEN (100-((SUM(OD.totalamount)*100)/((SUM(SKSD .Target))*1.0))) < ");
                        sb.append("0 THEN 100 ELSE ((SUM(OD.totalamount*100))/((SUM(SKSD .Target))*1.0)) END ,2) AS acheived,pl.sequence from SellerKPISKUDetail SKSD left join OrderDetail OD on OD.ProductID = (Select ProductId  from OrderDetail inner join ProductMaster on Pid = ProductID and parenthierarchy like '%/' || SKSD .pid || '/%') ");
                        sb.append("INNER JOIN ProductMaster p1 ON SKSD .pid = p1.pid and SKSD.kpiid=");
                        sb.append(kpiID);
                        sb.append(" and SKSD.KPIParamLovId=");
                        sb.append(kpiTypeLovID);
                        for (int j = 2; j <= i; j++) {
                            sb.append(" INNER JOIN  ProductMaster p");
                            sb.append(j);
                            sb.append(" on p");
                            sb.append((j - 1));
                            sb.append(".parentid=p");
                            sb.append(j);
                            sb.append(".pid ");
                        }
                        sb.append(" inner join  ProductLevel pl on pl.levelid = p");
                        sb.append(i);
                        sb.append(".plid");
                        sb.append(" GROUP BY P");
                        sb.append(i);
                        sb.append(".PID, P");
                        sb.append(i);
                        sb.append(".pname, P");
                        sb.append(i);
                        sb.append(".psname, P");
                        sb.append(i);
                        sb.append(".pcode, P");
                        sb.append(i);
                        sb.append(".parentId, p");
                        sb.append(i);
                        sb.append(".plid ");
                    }
                } else {
                    Commons.print("Loop ENd," + "" + loopEnd);
                    sb.append("SELECT P1.PID, P1.pname, P1.psname, P1.pcode, P1.parentId,p1.plid ,IFNULL(SKSD .kpiid,0), IFNULL(SKSD .Target,0)");
                    sb.append(", IFNULL(SKSD .Achievement,0), ROUND(CASE WHEN (100-((SKSD .Achievement*100)/((SKSD ");
                    sb.append(".Target)*1.0))) < 0 THEN 100 ELSE ((SKSD .Achievement*100)/((SKSD .Target)*1.0)) END ,2) AS acheived,pl.sequence  from ProductMaster p1");
                    sb.append(" INNER JOIN SellerKPISKUDetail SKSD ON SKSD .pid = p1.pid and SKSD.kpiid=");
                    sb.append(kpiID);
                    sb.append(" and SKSD.KPIParamLovId=");
                    sb.append(kpiTypeLovID);
                    sb.append(" inner join  ProductLevel pl on pl.levelid = p1.plid ");

                    for (int i = 2; i <= loopEnd; i++) {
                        Commons.print("Loop I," + "" + i);
                        sb.append("UNION ");
                        sb.append("SELECT P");
                        sb.append(i);
                        sb.append(".PID, P");
                        sb.append(i);
                        sb.append(".pname, P");
                        sb.append(i);
                        sb.append(".psname, P");
                        sb.append(i);
                        sb.append(".pcode, P");
                        sb.append(i);
                        sb.append(".parentId, p");
                        sb.append(i);
                        sb.append(".plid,IFNULL(SKSD .kpiid,0),SUM(IFNULL(SKSD .Target,0))");
                        sb.append(",SUM(IFNULL(SKSD .Achievement,0)),ROUND(CASE WHEN (100-((SUM(SKSD .Achievement)*100)/((SUM(SKSD .Target))*1.0))) < ");
                        sb.append("0 THEN 100 ELSE ((SUM(SKSD .Achievement*100))/((SUM(SKSD .Target))*1.0)) END ,2) AS acheived,pl.sequence from SellerKPISKUDetail SKSD  ");
                        sb.append("INNER JOIN ProductMaster p1 ON SKSD .pid = p1.pid and SKSD.kpiid=");
                        sb.append(kpiID);
                        sb.append(" and SKSD.KPIParamLovId=");
                        sb.append(kpiTypeLovID);

                        for (int j = 2; j <= i; j++) {
                            sb.append(" INNER JOIN  ProductMaster p");
                            sb.append(j);
                            sb.append(" on p");
                            sb.append((j - 1));
                            sb.append(".parentid=p");
                            sb.append(j);
                            sb.append(".pid ");
                        }


                        sb.append(" inner join  ProductLevel pl on pl.levelid = p");
                        sb.append(i);
                        sb.append(".plid");
                        sb.append(" GROUP BY P");
                        sb.append(i);
                        sb.append(".PID, P");
                        sb.append(i);
                        sb.append(".pname, P");
                        sb.append(i);
                        sb.append(".psname, P");
                        sb.append(i);
                        sb.append(".pcode, P");
                        sb.append(i);
                        sb.append(".parentId, p");
                        sb.append(i);
                        sb.append(".plid ");

                    }

                }
                Cursor c = db.selectSQL(sb.toString());

                if (c != null) {
                    sellerKpiSku = new Vector<>();
                    kpiSkuMasterById = new HashMap<>();
                    while (c.moveToNext()) {
                        temp = new SKUWiseTargetBO();
                        temp.setPid(c.getInt(0));
                        temp.setProductName(c.getString(1));
                        temp.setProductShortName(c.getString(2));
                        temp.setProductCode(c.getString(3));
                        temp.setParentID(c.getInt(4));
                        temp.setLevelID(c.getInt(5));
                        temp.setKpiID(c.getInt(6));
                        temp.setTarget(c.getDouble(7));
                        temp.setAchieved(c.getDouble(8));
                        temp.setCalculatedPercentage(c.getFloat(9));
                        temp.setSequence(c.getInt(10));
                        if (temp.getCalculatedPercentage() >= 100) {
                            temp.setConvTargetPercentage(0);
                            temp.setConvAcheivedPercentage(100);
                        } else {
                            temp.setConvTargetPercentage(100 - temp
                                    .getCalculatedPercentage());
                            temp.setConvAcheivedPercentage(temp
                                    .getCalculatedPercentage());
                        }

                        sellerKpiSku.add(temp);
                        kpiSkuMasterById.put(new Integer(temp.getPid()), new Integer(temp.getParentID()));
                    }
                    c.close();
                }
//                if (interval.equals("DAY")) {
//                    calculateDayAcheivement();
//                }
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public void getGridData(int routeId) {
        try {
            getDashChartData().clear();
            for (DashBoardBO dshObj : getDashboardMasterData()) {
                if (dshObj.getRouteID() == routeId) {
                    getDashChartData().add(dshObj);
                }
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public void getGridData(String beatDesc) {
        try {
            getDashChartData().clear();
            for (DashBoardBO dshObj : getDashboardMasterData()) {
                if (dshObj.getBeatDescription().equalsIgnoreCase(beatDesc)) {
                    getDashChartData().add(dshObj);
                }
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }

    }

    public String getColumnstackedPercentData() {
        JSONArray jsonArr;
        JSONObject jsonObj = null;
        try {
            jsonArr = new JSONArray();
            jsonObj = new JSONObject();
            for (DashBoardBO semichart : getDashChartData()) {
                JSONObject tempJson = new JSONObject();
                tempJson.accumulate("chartText", semichart.getText());
                tempJson.accumulate("chartTargetVal",
                        semichart.getConvTargetPercentage());
                tempJson.accumulate("chartAcheivedVal",
                        semichart.getConvAcheivedPercentage());
                jsonArr.put(tempJson);
            }
            jsonObj.accumulate("chartInputParameter1", "Remaining Target");
            jsonObj.accumulate("chartInputParameter2", "Achieved");
            jsonObj.accumulate("chartName", "");
            jsonObj.accumulate("chartYaxisText", "Target/Achieved %");
            jsonObj.accumulate("chartData", jsonArr);
        } catch (JSONException e) {
            Commons.printException("" + e);
        }
        WeakReference<JSONObject> wkJson = new WeakReference<>(
                jsonObj);
        Commons.print(wkJson.get().toString());
        return wkJson.get().toString();
    }

    public List<DashBoardBO> getDashboardMasterData() {
        return dashMasterData;
    }

    public Vector<DashBoardBO> getDashBoardReportList() {
        return dashBoardReportList;
    }

    public void setDashBoardReportList(Vector<DashBoardBO> dashBoardReportList) {
        this.dashBoardReportList = dashBoardReportList;
    }

    public ArrayList<DashBoardBO> getDashChartData() {
        if (dashChartData == null)
            return new ArrayList<>();
        return dashChartData;
    }

    public List<CharSequence> getBeatList() {
        return beatList;
    }

    public String QT(String data) // Quote
    {
        return "'" + data + "'";
    }


    public void findMinMaxProductLevel(String retailerid) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
        try {
            db.openDataBase();
            String sb = "SELECT PLMin .LevelId,PLMax .LevelId FROM " +
                    "(SELECT MAX(PL.Sequence) MaX_Seq,MIN(PL.Sequence) Min_Seq FROM ProductLevel PL" +
                    " INNER JOIN SKUWiseTarget SWT ON (PL.LevelId = SWT.Level) where SWT.rid=" +
                    bmodel.QT(retailerid) + ") Src" +
                    "INNER JOIN ProductLevel PLMin ON (PLMin .Sequence= Min_Seq )" +
                    "INNER JOIN ProductLevel PLMax ON (PLMax .Sequence= MaX_Seq )";

            Cursor c = db.selectSQL(sb);
            if (c != null) {
                if (c.getCount() > 0) {
                    while (c.moveToNext()) {
                        mMinLevel = c.getInt(0);
                        mMaxLevel = c.getInt(1);
                    }
                }
                c.close();
            }


        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }

    }

    public void downloadSKUWiseTarget(String retailerId, String type,
                                      String code) {
        SKUWiseTargetBO temp;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.openDataBase();

            StringBuilder sb = new StringBuilder();
            sb.append("SELECT P.PID, P.pname, P.psname, P.pcode, P.parentId,");
            sb.append(" P.barcode, IFNULL(swt.rid,0), IFNULL(swt.tgt,0), ");

            if (type.equals("DAY")) {
                sb.append("sum(qty),");

            } else {
                sb.append("IFNULL(swt.ach,0),");
            }
            sb.append(" ROUND(CASE WHEN (100-((swt.ach*100)/((swt.tgt)*1.0))) < 0 THEN 100");
            sb.append(" ELSE ((swt.ach*100)/((swt.tgt)*1.0)) END ,2) AS acheived,");
            sb.append(" IFNULL(slt.listcode,0), IFNULL(slt.ListName,''),strftime('%m', replace(date,'/','-')) AS Month,Freqtype, IFNULL(swt.RField,0)");
            sb.append(" FROM ProductMaster P INNER JOIN SkuWiseTarget swt ON swt.pid = p.pid AND swt.FreqType LIKE '");
            sb.append(type);
            sb.append("'  AND rid = ");
            sb.append(retailerId);
            sb.append(" AND Level = ");
            sb.append(mMaxLevel);
            sb.append(" LEFT JOIN StandardListMaster slt on slt.listid=swt.type ");
            if (type.equals("DAY")) {
                sb.append("left join invoicedetails id on id.productid=swt.pid group by id.productid");
            }

            Cursor c = db.selectSQL(sb.toString());
            if (c != null) {
                skuWiseTarget = new ArrayList<>();
                while (c.moveToNext()) {
                    temp = new SKUWiseTargetBO();
                    temp.setPid(c.getInt(0));
                    if (!code.equals("0")) {
                        temp.setProductName(c.getString(1));
                        temp.setProductShortName(c.getString(2));
                    } else {
                        temp.setProductName(c.getString(1) + " "
                                + c.getString(12));
                        temp.setProductShortName(c.getString(2) + " "
                                + c.getString(12));
                    }
                    temp.setProductCode(c.getString(3));
                    temp.setParentID(c.getInt(4));
                    temp.setBarcode(c.getString(5));
                    temp.setRetailerID(c.getString(6));
                    temp.setTarget(c.getDouble(7));
                    temp.setAchieved(c.getDouble(8));
                    temp.setCalculatedPercentage(c.getFloat(9));
                    if (temp.getCalculatedPercentage() >= 100) {
                        temp.setConvTargetPercentage(0);
                        temp.setConvAcheivedPercentage(100);
                    } else {
                        temp.setConvTargetPercentage(100 - temp
                                .getCalculatedPercentage());
                        temp.setConvAcheivedPercentage(temp
                                .getCalculatedPercentage());
                    }
                    temp.setType(c.getString(10));

                    int monthValue = c.getInt(12);
                    if (monthValue > 0 && monthValue <= 12)
                        temp.setMonthName(MONTH_NAME[monthValue - 1]);

                    temp.setFreqType(c.getString(13));
                    temp.setrField(c.getDouble(14));

                    skuWiseTarget.add(temp);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public ArrayList<SKUWiseTargetBO> getSkuWiseTarget() {
        return skuWiseTarget;
    }

    public Integer getKpiSkuMasterBoById(Integer productID) {
        if (kpiSkuMasterById == null || kpiSkuMasterById.get(productID) == null)
            return 0;

        return kpiSkuMasterById.get(productID);
    }


    public void LoadSKUWiseTarget(String retailerId) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c;

            String tblName;

            if (bmodel.configurationMasterHelper.IS_INVOICE)
                tblName = "InvoiceDetails";
            else
                tblName = "OrderDetail";

            String retailerCondition = "";

            if (!retailerId.equals("0"))
                retailerCondition = " WHERE A.retailerid = " + retailerId;

            c = db.selectSQL("SELECT ProductID, SUM(qty), SUM(totalAmount) FROM "
                    + tblName
                    + retailerCondition + " Group By ProductID");

            if (c != null) {
                while (c.moveToNext()) {

                    for (SKUWiseTargetBO target : getSkuWiseTarget()) {
                        if (target.getPid() == c.getInt(0)) {
                            if (target.getType().equalsIgnoreCase("VOL")) {
                                target.setAchieved(target.getAchieved()
                                        + c.getInt(1));
                                target.setCalculatedPercentage(SDUtil.convertToFloat(bmodel.formatValue(
                                        (target.getAchieved()
                                                / target.getTarget() * 100))));
                                if (target.getCalculatedPercentage() >= 100) {
                                    target.setConvTargetPercentage(0);
                                    target.setConvAcheivedPercentage(100);
                                } else {
                                    target.setConvTargetPercentage(100 - target
                                            .getCalculatedPercentage());
                                    target.setConvAcheivedPercentage(target
                                            .getCalculatedPercentage());
                                }
                            } else if (target.getType().equalsIgnoreCase("SV")) {
                                target.setAchieved(target.getAchieved()
                                        + c.getInt(2));
                                target.setCalculatedPercentage(SDUtil.convertToFloat(bmodel.formatValue(
                                        (target.getAchieved()
                                                / target.getTarget() * 100))));
                                if (target.getCalculatedPercentage() >= 100) {
                                    target.setConvTargetPercentage(0);
                                    target.setConvAcheivedPercentage(100);
                                } else {
                                    target.setConvTargetPercentage(100 - target
                                            .getCalculatedPercentage());
                                    target.setConvAcheivedPercentage(target
                                            .getCalculatedPercentage());
                                }
                            }
                        }
                    }
                }
                c.close();
            }
            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    // @Mansoor
    //  flag is key used to identify is from dashboard or SkuwiseTargetActivity
    // to calculate achived value for each sku of VOL type

    public void downloadDashboardLevelSkip(int flag) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
        int parentLevel = 0;
        int childLevel = 0;
        try {
            db.openDataBase();
            String sb = "select distinct PL1.sequence,PL2.sequence  from Skuwisetarget SKU" +
                    " LEFT JOIN ProductLevel PL1 ON PL1.LevelId =" + mMinLevel +
                    " LEFT JOIN ProductLevel PL2 ON PL2.LevelId =" + mMaxLevel;
            Cursor sequenceCursor = db.selectSQL(sb);
            if (sequenceCursor != null) {
                if (sequenceCursor.getCount() > 0) {
                    if (sequenceCursor.moveToNext()) {
                        parentLevel = sequenceCursor.getInt(0);
                        childLevel = sequenceCursor.getInt(1);
                    }

                }
                sequenceCursor.close();
            }
            dashBoardLevelSkip(parentLevel, childLevel, db, flag);

        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }
    }

    private void dashBoardLevelSkip(int parentlevel, int childLevel, DBUtil db, int flag) {
        String tblName;

        if (bmodel.configurationMasterHelper.IS_INVOICE)
            tblName = "InvoiceDetails";
        else
            tblName = "OrderDetail";

        int loopend = childLevel - parentlevel + 1;
        StringBuilder sb = new StringBuilder();
        if (flag == 1) {
            sb.append("select A");
            sb.append(loopend);
            sb.append(".pid,A1.pid");
        }
        if (flag == 0) {
            sb.append("select A");
            sb.append(loopend);
            sb.append(".pid,A1.pid,SUM(OD.qty),SUM(OD.totalAmount)");
        }
        sb.append(" from ProductMaster A1 ");

        for (int i = 2; i <= loopend; i++) {
            sb.append(" INNER JOIN ProductMaster A");
            sb.append(i);
            sb.append(" ON A");
            sb.append(i);
            sb.append(".ParentId = A");
            sb.append((i - 1));
            sb.append(".PID");
        }
        if (flag == 0) {
            sb.append(" INNER JOIN ");
            sb.append(tblName);
            sb.append(" od on od.productid = A");
            sb.append(loopend);
            sb.append(".pid ");
            sb.append(" INNER JOIN SkuWiseTarget sk on od.productid = sk.pid  and sk.freqtype='DAY' group by a1.pid");
        }
        Cursor c = db.selectSQL(sb.toString());
        if (c != null) {
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    int pid = c.getInt(0);
                    int parentid = c.getInt(1);

                    if (flag == 1)
                        for (SKUWiseTargetBO skuwiseTargetBO : skuWiseTarget) {
                            if (skuwiseTargetBO.getPid() == pid) {
                                skuwiseTargetBO.setParentID(parentid);
                            }
                        }
                    if (flag == 0) {
                        for (DashBoardBO dashBoardBO : getDashChartData()) {
                            if (dashBoardBO.getPId() == parentid && dashBoardBO.getType().equalsIgnoreCase("DAY")) {
                                if (dashBoardBO.getCode().equals("VOL")) {
                                    dashBoardBO.setAcheived(c.getDouble(2));

                                    if (dashBoardBO.getTarget() > 0) {
                                        dashBoardBO.setCalculatedPercentage(SDUtil.convertToFloat(SDUtil.roundIt(((dashBoardBO.getAcheived() / dashBoardBO.getTarget()) * 100), 2)));
                                        if (dashBoardBO.getCalculatedPercentage() >= 100) {
                                            dashBoardBO.setConvTargetPercentage(0);
                                            dashBoardBO.setConvAcheivedPercentage(100);
                                        } else {
                                            dashBoardBO.setConvTargetPercentage(100 - dashBoardBO
                                                    .getCalculatedPercentage());
                                            dashBoardBO.setConvAcheivedPercentage(dashBoardBO
                                                    .getCalculatedPercentage());
                                        }
                                    } else if (dashBoardBO.getTarget() <= 0 && dashBoardBO.getAcheived() > 0) {
                                        dashBoardBO.setConvTargetPercentage(0);
                                        dashBoardBO.setConvAcheivedPercentage(100);
                                    }

                                }
                            }
                        }
                    }

                }
            }
            c.close();
        }


    }

    // Get First Day of the month
    public static String getFirstDateOfCurrentMonth(String format) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH,
                Calendar.getInstance().getActualMinimum(Calendar.DAY_OF_MONTH));
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.ENGLISH);
        return sdf.format(cal.getTime());
    }

    public void downloadRetailerKpi() {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
        mRetailerKpiListBymonth = new SparseArray<>();
        mRetailerKpiMonthList = new LinkedHashSet<>();

        try {
            db.openDataBase();
            String sb = "select rkd.kpiid,rkd.target,rkd.Achievement,slm.listname,slm.listcode," +
                    "strftime('%m', replace(rk.fromdate,'/','-')) AS Month,strftime('%Y', replace(rk.fromdate,'/','-')) AS Year,rk.fromdate  from retailerkpidetail rkd" +
                    " inner join retailerkpi rk on rk.kpiid=rkd.kpiid" +
                    " left join standardlistmaster slm on rkd.kpitypelovid=slm.listid where retailerid=" +
                    bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()) +
                    " order by year,month asc";
            Cursor c = db.selectSQL(sb);
            int month = 0;
            if (c.getCount() > 0) {
                RetailerKPIBO retailerKPIBO;
                ArrayList<RetailerKPIBO> retailerKpiList = new ArrayList<>();
                while (c.moveToNext()) {
                    retailerKPIBO = new RetailerKPIBO();
                    retailerKPIBO.setTarget(c.getDouble(1));
                    retailerKPIBO.setAchievement(c.getDouble(2));
                    retailerKPIBO.setListName(c.getString(3));
                    retailerKPIBO.setListCode(c.getString(4));
                    retailerKPIBO.setFromDate(c.getString(7));

                    if (month != c.getInt(5)) {
                        if (month != 0) {
                            mRetailerKpiListBymonth.put(month, retailerKpiList);
                            retailerKpiList = new ArrayList<>();
                            retailerKpiList.add(retailerKPIBO);
                            month = c.getInt(5);
                            mRetailerKpiMonthList.add(month);


                        } else {
                            retailerKpiList.add(retailerKPIBO);
                            month = c.getInt(5);
                            mRetailerKpiMonthList.add(month);
                        }
                    } else {
                        retailerKpiList.add(retailerKPIBO);
                    }


                }
                if (retailerKpiList.size() > 0) {
                    mRetailerKpiListBymonth.put(month, retailerKpiList);
                    mRetailerKpiMonthList.add(month);
                }
                c.close();
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }
    }

    public SparseArray<ArrayList<RetailerKPIBO>> getRetailerKpiListByMonth() {
        return mRetailerKpiListBymonth;
    }

    public LinkedHashSet<Integer> getRetailerKpiMonthList() {
        if (mRetailerKpiMonthList != null) {
            return mRetailerKpiMonthList;
        }
        return new LinkedHashSet<>();
    }

    /**
     * 0 - Day and P3M not available
     * 1 - P3M available
     * 2 - Day available
     * 3 - Day and P3M available
     *
     * @return
     */
    public int getShowDayAndP3MSpinner() {
        return showDayAndP3MSpinner;
    }

    public ArrayList<String> loadMSLUnsold(String retailerID) {
        ArrayList<String> mslUnsoldList = new ArrayList<>();
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
        db.createDataBase();
        db.openDataBase();
        int mContentLevel = bmodel.productHelper.getContentLevel(db, "MENU_DASHBOARD");
        String productIds = ProductTaggingHelper.getInstance(mContext).getTaggingDetails(mContext,"MSL", mContentLevel);
        try {
            if(db.isDbNullOrClosed())
                db.openDataBase();
            String sb = "Select distinct PM.PName from RtrWiseDeadProducts RP " +
                    "inner join ProductMaster PM on RP.pid = PM.PID " +
                    " Where RP.rid = " + bmodel.QT(retailerID) +
                    " AND PM.pid IN (" + productIds + ")";
            Cursor c = db.selectSQL(sb);
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    mslUnsoldList.add(c.getString(0));
                }
            }
            c.close();


        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }
        return mslUnsoldList;

    }

    public void downloadTotalValuesAndQty() {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        db.openDataBase();
        Cursor c;

        String tblName;

        if (bmodel.configurationMasterHelper.IS_INVOICE)
            tblName = "InvoiceDetails";
        else
            tblName = "OrderDetail";


        c = db.selectSQL("SELECT SUM(totalAmount) FROM "
                + tblName);
        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                for (DashBoardBO dashBoardBO : getDashChartData()) {
                    if (dashBoardBO.getCode().equals("SV") && dashBoardBO.getType().equalsIgnoreCase("DAY")) {
                        dashBoardBO.setAcheived(c.getDouble(0));

                        if (dashBoardBO.getTarget() > 0) {
                            dashBoardBO.setCalculatedPercentage(SDUtil.convertToFloat(bmodel.formatValue(((dashBoardBO.getAcheived() / dashBoardBO.getTarget()) * 100))));
                            if (dashBoardBO.getCalculatedPercentage() >= 100) {
                                dashBoardBO.setConvTargetPercentage(0);
                                dashBoardBO.setConvAcheivedPercentage(100);
                            } else {
                                dashBoardBO.setConvTargetPercentage(100 - dashBoardBO
                                        .getCalculatedPercentage());
                                dashBoardBO.setConvAcheivedPercentage(dashBoardBO
                                        .getCalculatedPercentage());
                            }
                        } else if (dashBoardBO.getTarget() <= 0 && dashBoardBO.getAcheived() > 0) {
                            dashBoardBO.setConvTargetPercentage(0);
                            dashBoardBO.setConvAcheivedPercentage(100);
                        }
                    }
                    if (dashBoardBO.getCode().equals("SVLM") && dashBoardBO.getType().equalsIgnoreCase("DAY") && dashBoardBO.getPId() == 0) {
                        dashBoardBO.setAcheived(c.getInt(1));

                        if (dashBoardBO.getTarget() > 0) {
                            dashBoardBO.setCalculatedPercentage(SDUtil.convertToFloat(bmodel.formatValue(((dashBoardBO.getAcheived() / dashBoardBO.getTarget()) * 100))));
                            if (dashBoardBO.getCalculatedPercentage() >= 100) {
                                dashBoardBO.setConvTargetPercentage(0);
                                dashBoardBO.setConvAcheivedPercentage(100);
                            } else {
                                dashBoardBO.setConvTargetPercentage(100 - dashBoardBO
                                        .getCalculatedPercentage());
                                dashBoardBO.setConvAcheivedPercentage(dashBoardBO
                                        .getCalculatedPercentage());
                            }
                        } else if (dashBoardBO.getTarget() <= 0 && dashBoardBO.getAcheived() > 0) {
                            dashBoardBO.setConvTargetPercentage(0);
                            dashBoardBO.setConvAcheivedPercentage(100);
                        }
                    }
                }
            }
        }
    }

    private boolean showDaySpinner = false;

    public boolean isShowDaySpinner() {
        return showDaySpinner;
    }

    public void setShowDaySpinner(boolean showDaySpinner) {
        this.showDaySpinner = showDaySpinner;
    }

    public void checkDaySpinner() {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            setShowDaySpinner(false);

            String sql = "select count(*) from SellerKPI where interval = 'DAY'";
            Cursor c = db.selectSQL(sql);
            sql = null;
            if (c != null) {
                while (c.moveToNext()) {
                    int count = c.getInt(0);
                    if (count > 0)
                        setShowDaySpinner(true);
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getKPiIDS(String userids) {
        String KpiIds = "";
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            getDashboardMasterData().clear();

            String sql = new String(
                    "select KPIId from SellerKPI userids"
                            + " where UserId in (" + userids) + ")";
            Cursor c = db.selectSQL(sql);
            sql = null;
            if (c != null) {
                int count = 0;
                while (c.moveToNext()) {
                    count++;
                    KpiIds += bmodel.QT(c.getString(0));
                    if (count != c.getCount())
                        KpiIds += ",";
                }
            }
            c.close();
            c = null;
            db.closeDB();

        } catch (Exception e) {
            e.printStackTrace();
            KpiIds = "";
        }
        return KpiIds;
    }

    public void findMinMaxProductLevelSellerKPI(String kpiIDs, int kpiTypeLovID, String interval) {
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.openDataBase();
            StringBuilder sb = new StringBuilder();

            sb.append("select distinct (Sksd.levelid),pl.sequence from SellerKPISKUDetail sksd");
            sb.append(" inner join Productlevel pl on pl.levelid =sksd.levelid where kpiid in(" + kpiIDs + ")");

            Cursor c = db.selectSQL(sb.toString());
            if (c != null) {
                if (c.getCount() > 0) {
                    while (c.moveToNext()) {
                        mSellerKpiMaxLevel = c.getInt(0);
                        mSellerKpiMaxSeqLevel = c.getInt(1);
                    }
                }

            }
            c.close();


            String sql = "select distinct (levelid),sequence from Productlevel where parentid=0";
            c = db.selectSQL(sql);
            if (c != null) {
                if (c.getCount() > 0) {
                    while (c.moveToNext()) {
                        mSellerKpiMinLevel = c.getInt(0);
                        mSellerKpiMinSeqLevel = c.getInt(1);
                    }
                }

            }
            c.close();

            downloadSellerKpiSkuDetail(kpiIDs, kpiTypeLovID, mSellerKpiMinSeqLevel, mSellerKpiMaxSeqLevel, interval);

        } catch (Exception e) {
            Commons.print(e.getMessage());
        } finally {
            db.closeDB();
        }

    }

    public void downloadSellerKpiSkuDetail(String kpiID, int kpiTypeLovID, int parentLevel, int childLevel, String interval) {
        SKUWiseTargetBO temp;
        try {
            int loopEnd = childLevel - parentLevel + 1;

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            if (loopEnd == 0) {
                sb.append("SELECT P.PID, P.pname, P.psname, P.pcode, P.parentId,p.plid ,IFNULL(SKSD .kpiid,0), SUM(IFNULL(SKSD .Target,0))");
                sb.append(", SUM(IFNULL(SKSD .Achievement,0)), ROUND(CASE WHEN (100-((SUM(SKSD .Achievement)*100)/((SUM(SKSD ");
                sb.append(".Target))*1.0))) < 0 THEN 100 ELSE ((SUM(SKSD .Achievement)*100)/((SUM(SKSD .Target))*1.0)) END ,2) AS acheived,pl.sequence  from ProductMaster p");
                sb.append(" INNER JOIN SellerKPISKUDetail SKSD ON SKSD .pid = p.pid and SKSD.kpiid in (" + kpiID + ") and SKSD.KPIParamLovId=" + kpiTypeLovID);
                sb.append(" inner join  ProductLevel pl on pl.levelid = p.plid group by SKSD .pid");

                Cursor c = db.selectSQL(sb.toString());

                if (c != null) {
                    sellerKpiSku = new Vector<SKUWiseTargetBO>();
                    kpiSkuMasterById = new HashMap<>();
                    while (c.moveToNext()) {
                        temp = new SKUWiseTargetBO();
                        temp.setPid(c.getInt(0));
                        temp.setProductName(c.getString(1));
                        temp.setProductShortName(c.getString(2));
                        temp.setProductCode(c.getString(3));
                        temp.setParentID(c.getInt(4));
                        temp.setLevelID(c.getInt(5));
                        temp.setKpiID(c.getInt(6));
                        temp.setTarget(c.getDouble(7));
                        temp.setAchieved(c.getDouble(8));
                        temp.setCalculatedPercentage(c.getFloat(9));
                        temp.setSequence(c.getInt(10));
                        if (temp.getCalculatedPercentage() >= 100) {
                            temp.setConvTargetPercentage(0);
                            temp.setConvAcheivedPercentage(100);
                        } else {
                            temp.setConvTargetPercentage(100 - temp
                                    .getCalculatedPercentage());
                            temp.setConvAcheivedPercentage(temp
                                    .getCalculatedPercentage());
                        }

                        sellerKpiSku.add(temp);
                        kpiSkuMasterById.put(new Integer(temp.getPid()), new Integer(temp.getParentID()));
                    }
                    c.close();
                }
                if (interval.equals("DAY")) {
                    calculateDayAcheivement(false);
                }

            } else {

                Commons.print("Loop ENd," + "" + loopEnd);
                sb.append("SELECT P1.PID, P1.pname, P1.psname, P1.pcode, P1.parentId,p1.plid ,IFNULL(SKSD .kpiid,0), SUM(IFNULL(SKSD .Target,0))");
                sb.append(", SUM(IFNULL(SKSD .Achievement,0)), ROUND(CASE WHEN (100-((SUM(SKSD .Achievement)*100)/((SUM(SKSD ");
                sb.append(".Target))*1.0))) < 0 THEN 100 ELSE ((SUM(SKSD .Achievement)*100)/((SUM(SKSD .Target))*1.0)) END ,2) AS acheived,pl.sequence  from ProductMaster p1");
                sb.append(" INNER JOIN SellerKPISKUDetail SKSD ON SKSD .pid = p1.pid and SKSD.kpiid in (" + kpiID + ") and SKSD.KPIParamLovId=" + kpiTypeLovID);
                sb.append(" inner join  ProductLevel pl on pl.levelid = p1.plid group by SKSD .pid");

                for (int i = 2; i <= loopEnd; i++) {
                    Commons.print("Loop I," + "" + i);
                    sb.append("UNION ");
                    sb.append("SELECT P" + i + ".PID, P" + i + ".pname, P" + i + ".psname, P" + i + ".pcode, P" + i + ".parentId, p" + i + ".plid,IFNULL(SKSD .kpiid,0),SUM(IFNULL(SKSD .Target,0))");
                    sb.append(",SUM(IFNULL(SKSD .Achievement,0)),ROUND(CASE WHEN (100-((SUM(SKSD .Achievement)*100)/((SUM(SKSD .Target))*1.0))) < ");
                    sb.append("0 THEN 100 ELSE ((SUM(SKSD .Achievement*100))/((SUM(SKSD .Target))*1.0)) END ,2) AS acheived,pl.sequence from SellerKPISKUDetail SKSD  ");
                    sb.append("INNER JOIN ProductMaster p1 ON SKSD .pid = p1.pid and SKSD.kpiid in (" + kpiID + ")and SKSD.KPIParamLovId=" + kpiTypeLovID);

                    for (int j = 2; j <= i; j++) {
                        sb.append(" INNER JOIN  ProductMaster p" + j + " on p" + (j - 1) + ".parentid=p" + j + ".pid ");
                    }


                    sb.append(" inner join  ProductLevel pl on pl.levelid = p" + i + ".plid");
                    sb.append(" GROUP BY P" + i + ".PID, P" + i + ".pname, P" + i + ".psname, P" + i + ".pcode, P" + i + ".parentId, p" + i + ".plid ");

                }


                Cursor c = db.selectSQL(sb.toString());

                if (c != null) {
                    sellerKpiSku = new Vector<SKUWiseTargetBO>();
                    kpiSkuMasterById = new HashMap<>();
                    while (c.moveToNext()) {
                        temp = new SKUWiseTargetBO();
                        temp.setPid(c.getInt(0));
                        temp.setProductName(c.getString(1));
                        temp.setProductShortName(c.getString(2));
                        temp.setProductCode(c.getString(3));
                        temp.setParentID(c.getInt(4));
                        temp.setLevelID(c.getInt(5));
                        temp.setKpiID(c.getInt(6));
                        temp.setTarget(c.getDouble(7));
                        temp.setAchieved(c.getDouble(8));
                        temp.setCalculatedPercentage(c.getFloat(9));
                        temp.setSequence(c.getInt(10));
                        if (temp.getCalculatedPercentage() >= 100) {
                            temp.setConvTargetPercentage(0);
                            temp.setConvAcheivedPercentage(100);
                        } else {
                            temp.setConvTargetPercentage(100 - temp
                                    .getCalculatedPercentage());
                            temp.setConvAcheivedPercentage(temp
                                    .getCalculatedPercentage());
                        }

                        sellerKpiSku.add(temp);
                        kpiSkuMasterById.put(new Integer(temp.getPid()), new Integer(temp.getParentID()));
                    }
                    c.close();
                }
                if (interval.equals("DAY")) {
                    calculateDayAcheivement(false);
                }
            }
            db.closeDB();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadKpiDashBoard(String userid, String interval) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            getDashboardMasterData().clear();

            String sql = new String(
                    "SELECT SLM.ListName,SUM(SKD.Target),SUM(SKD.Achievement),"
                            + " ROUND(CASE WHEN (100-((SUM(SKD.Achievement)*100)/((SUM(SKD.Target))*1.0))) < 0"
                            + " THEN 100 ELSE ((SUM(SKD.Achievement)*100)/((SUM(SKD.Target))*1.0)) END ,2) AS conv_ach_perc"
                            + ",SUM(SKS.Score),SUM(SKS.Incentive),SK.KPIID,SKD.KPIParamLovId,SLM.Flex1,SLM.ListCode FROM SellerKPI SK"
                            + " inner join SellerKPIDetail SKD on SKD.KPIID= SK.KPIID"
                            + " LEFT join SellerKPIScore SKS on SKD.KPIID= SKS.KPIID and SKD.KPIParamLovId = SKS.KPIParamLovId"
                            + " inner join StandardListMaster SLM on SLM.Listid=SKD.KPIParamLovId"
                            + " where userid in ( "
                            + userid.toString() + ")"
                            + " and interval= "
                            + bmodel.QT(interval)
                            + " AND "
                            + bmodel.QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL))
                            + " between SK.fromdate and SK.todate "
                            + (userid.equals("0") ? " and SK.isSummary=1" : "")
                            + " group by SLM.Listid order by DisplaySeq asc");
            Cursor c = db.selectSQL(sql);
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
                    if (sbo.getCode().equalsIgnoreCase("VAL"))
                        sbo.setSubDataCount(getSKUCount(sbo.getKpiTypeLovID()));
                    else
                        sbo.setSubDataCount(0);
                    getDashboardMasterData().add(sbo);
                    sbo = null;
                }
            }
            c.close();
            c = null;
            db.closeDB();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getSKUCount(int KPIParamLovId) {
        int count = 0;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            String sql = new String(
                    "select count(kpiid) from SellerKPISKUDetail"
                            + " where KPIParamLovId =" + KPIParamLovId);
            Cursor c = db.selectSQL(sql);
            sql = null;
            if (c != null) {
                while (c.moveToNext()) {
                    count = c.getInt(0);
                }
            }
            c.close();
            c = null;
            db.closeDB();

        } catch (Exception e) {
            e.printStackTrace();
            count = 0;
        }
        return count;
    }

    @Deprecated
    /**
     * @deprecated
     * @See {@link UserDataManagerImpl#fetchDashboardUsers()}
     */
    public ArrayList<UserMasterBO> downloadUserList() {
        ArrayList<UserMasterBO> userList = null;
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        try {

            db.createDataBase();
            db.openDataBase();
            String query = "select userid,username from usermaster where isDeviceuser!=1 and distributorid!=0";
            Cursor c = db.selectSQL(query);
            if (c != null) {
                userList = new ArrayList<UserMasterBO>();
                UserMasterBO userMasterBO;
                while (c.moveToNext()) {
                    userMasterBO = new UserMasterBO();
                    userMasterBO.setUserid(c.getInt(0));
                    userMasterBO.setUserName(c.getString(1));
                    userList.add(userMasterBO);

                }
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.closeDB();
        }
        return userList;
    }

    public ArrayList<SKUWiseTargetBO> getSkuwiseGraphData() {
        return skuwiseGraphData;
    }

    public void setSkuwiseGraphData(ArrayList<SKUWiseTargetBO> skuwiseGraphData) {
        this.skuwiseGraphData = skuwiseGraphData;
    }

    public ArrayList<DashBoardBO> getP3mChartList() {
        return p3mChartList;
    }

    public void setP3mChartList(ArrayList<DashBoardBO> p3mChartList) {
        this.p3mChartList = p3mChartList;
    }

    private Comparator<DashBoardBO> paramsIDComparator = new Comparator<DashBoardBO>() {

        public int compare(DashBoardBO file1, DashBoardBO file2) {

            return (int) file1.getKpiTypeLovID() - (int) file2.getKpiTypeLovID();

        }

    };

    /*Created by Mansoor 18-07-2017*/
    /*Kellogs dsashboard methods*/

    public ArrayList<StandardListBO> getDashTabs() {
        ArrayList<StandardListBO> mTabs = new ArrayList<>();
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        try {

            db.createDataBase();
            db.openDataBase();
            String query = "SELECT ListId,ListName,ListCode FROM  StandardListMaster WHERE ListType = 'TARGET_PARAMETERS'";
            Cursor c = db.selectSQL(query);
            if (c != null) {
                mTabs = new ArrayList<>();
                StandardListBO standardListBO;
                while (c.moveToNext()) {
                    standardListBO = new StandardListBO();
                    standardListBO.setListID(c.getString(0));
                    standardListBO.setListName(c.getString(1));
                    standardListBO.setListCode(c.getString(2));
                    mTabs.add(standardListBO);

                }
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.closeDB();
        }
        return mTabs;
    }

    private ArrayList<SKUWiseTargetBO> skuWiseTargetList;

    public ArrayList<SKUWiseTargetBO> getSkuWiseTargetList() {
        return skuWiseTargetList;
    }

    public void setSkuWiseTargetList(ArrayList<SKUWiseTargetBO> skuWiseTargetList) {
        this.skuWiseTargetList = skuWiseTargetList;
    }

    public void loadKlgsDashboardData(String ListCode) {
        skuWiseTargetList = new ArrayList<>();
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        try {

            db.createDataBase();
            db.openDataBase();
            String query = "SELECT DISTINCT PM.PName, round(RKD.Target,2), round(RKD.Achievement,2), " +
                    "round(ifnull(CAST(RKD.Achievement AS FLOAT)/CAST(RKD.Target AS FLOAT)*100,'0'),2) AS sales " +
                    "FROM RetailerKPISKUDetail RKD  INNER JOIN RetailerKPI RK ON RK.KPIId = RKD.KPIId INNER JOIN ProductMaster PM ON PM.PID = RKD.PId " +
                    "WHERE RK.RetailerId = '" + bmodel.retailerMasterBO.getRetailerID() + "' AND RKD.KPIParamLovId = " +
                    "(SELECT ListId FROM StandardListMaster WHERE ListCode = '" + ListCode + "' AND " +
                    "ListType = 'TARGET_PARAMETERS')";
            Cursor c = db.selectSQL(query);
            if (c != null) {
                skuWiseTargetList = new ArrayList<>();
                SKUWiseTargetBO skuWiseTargetBO;
                while (c.moveToNext()) {
                    skuWiseTargetBO = new SKUWiseTargetBO();
                    skuWiseTargetBO.setProductName(c.getString(0));
                    skuWiseTargetBO.setTarget(c.getDouble(1));
                    skuWiseTargetBO.setAchieved(c.getDouble(2));
                    skuWiseTargetBO.setrField(c.getDouble(3));
                    skuWiseTargetList.add(skuWiseTargetBO);

                }
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.closeDB();
        }
    }


    public void loadPlatformDashboardData() {
        skuWiseTargetList = new ArrayList<>();
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        try {

            db.createDataBase();
            db.openDataBase();
            String query = "SELECT  SLM.ListName, round(RKD.Target,2), round(RKD.Achievement,2), round(ifnull(CAST(RKD.Achievement AS FLOAT)/CAST(RKD.Target AS FLOAT)*100,'0'),2) AS sales " +
                    "FROM RetailerKPISKUDetail RKD  INNER JOIN RetailerKPI RK ON RK.KPIId = RKD.KPIId " +
                    "INNER JOIN StandardListMaster SLM ON SLM.ListId = RKD.PId AND SLM.ListType = 'POSM_GROUP_TYPE' " +
                    "WHERE RK.RetailerId = '" + bmodel.retailerMasterBO.getRetailerID() + "' AND RKD.KPIParamLovId = " +
                    "(SELECT ListId FROM StandardListMaster WHERE ListCode = 'PLATFORM' AND ListType = 'TARGET_PARAMETERS')group by SLM.ListId ";
            Cursor c = db.selectSQL(query);
            if (c != null) {
                skuWiseTargetList = new ArrayList<>();
                SKUWiseTargetBO skuWiseTargetBO;
                while (c.moveToNext()) {
                    skuWiseTargetBO = new SKUWiseTargetBO();
                    skuWiseTargetBO.setProductName(c.getString(0));
                    skuWiseTargetBO.setTarget(c.getDouble(1));
                    skuWiseTargetBO.setAchieved(c.getDouble(2));
                    skuWiseTargetBO.setrField(c.getDouble(3));
                    skuWiseTargetList.add(skuWiseTargetBO);

                }
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.closeDB();
        }
    }

    public ArrayList<Integer> productsIds;
    public ArrayList<Integer> priorityIds;
    public ArrayList<PriorityBo> priorityList;

    public void loadPriorityDashboardData() {
        priorityList = new ArrayList<>();
        productsIds = new ArrayList<>();
        priorityIds = new ArrayList<>();

        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        try {

            db.createDataBase();
            db.openDataBase();
            String query = "Select * from (SELECT RKD.KPITypeLovId,RKD.pid,PM.PName, RKD.Achievement FROM RetailerKPISKUDetail RKD " +
                    "INNER JOIN RetailerKPI RK ON RK.KPIId = RKD.KPIId " +
                    "INNER JOIN ProductMaster PM ON PM.PID = RKD.PId WHERE RK.RetailerId = '" + bmodel.retailerMasterBO.getRetailerID() + "' AND " +
                    "RKD.KPIParamLovId = " +
                    "(SELECT ListId FROM StandardListMaster WHERE ListCode = 'PRIORITY' AND ListType = 'TARGET_PARAMETERS') AND RKD.KPITypeLovId in( SELECT RKD.KPITypeLovId from RetailerKPISKUDetail RKD " +
                    "INNER JOIN RetailerKPI RK ON RK.KPIId = RKD.KPIId " +
                    "WHERE RKD.KPIParamLovId = (SELECT ListId FROM StandardListMaster WHERE ListCode = 'PRIORITY' AND ListType = 'TARGET_PARAMETERS')) ) group by KPITypeLovId,pid";
            Cursor c = db.selectSQL(query);
            if (c != null) {
                PriorityBo priorityBo;
                while (c.moveToNext()) {
                    priorityBo = new PriorityBo();
                    priorityBo.setPriorityId(c.getInt(0));
                    priorityBo.setProductID(c.getInt(1));
                    priorityBo.setProductName(c.getString(2));
                    priorityBo.setAchievement(c.getDouble(3));
                    priorityList.add(priorityBo);
                    priorityIds.add(c.getInt(0));
                    productsIds.add(c.getInt(1));
                }
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.closeDB();
        }

        Set<Integer> hs = new HashSet<>();
        hs.addAll(priorityIds);
        priorityIds.clear();
        priorityIds.addAll(hs);

        hs.clear();
        hs.addAll(productsIds);
        productsIds.clear();
        productsIds.addAll(hs);
    }

    public String getProductName(int productId) {
        String productName = "";
        for (int i = 0; i < priorityList.size(); i++) {
            if (priorityList.get(i).getProductID() == productId) {
                productName = priorityList.get(i).getProductName();
                break;
            }
        }

        return productName;

    }

    public double getProductAch(int productId, int priorityID) {
        double achievement = 0;
        for (int i = 0; i < priorityList.size(); i++) {
            if (priorityList.get(i).getProductID() == productId && priorityList.get(i).getPriorityId() == priorityID) {
                achievement = priorityList.get(i).getAchievement();
                break;
            }
        }

        return achievement;

    }

    public static final Comparator<Integer> productIdsComparator = new Comparator<Integer>() {

        public int compare(Integer file1, Integer file2) {


            //acs
            return file1 - file2;

            // descending order
            // return file2.imgFlag - file1.imgFlag;
        }

    };

//    public void getAchieved(int kpiID, int kpiTypeLovID, String interval) {
//        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
//        try {
//            db.openDataBase();
//            String sb = "select ifnull(sum(0+A.Achievement),0) from SellerKPIDetail A " +
//                    "inner join SellerKpi B on A.KpiID = B.KpiID " +
//                    "where B.kpiid='" + kpiID + "' and A.KpiParamLovID ='" + kpiTypeLovID + "' and B.Interval ='" + interval + "'";
//
//            Cursor c = db.selectSQL(sb);
//            if (c != null) {
//                if (c.getCount() > 0) {
//                    while (c.moveToNext()) {
//                        mParamAchieved = c.getInt(0);
//                    }
//                }
//                c.close();
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public DashBoardBO getDashboardBO() {
        return dashboardBO;
    }

    public void setDashboardBO(DashBoardBO dashboardBO) {
        this.dashboardBO = dashboardBO;
    }


    public ArrayList<IncentiveDashboardBO> downloadIncentiveList(String type) {
        incentiveList = new ArrayList<>();
        incentiveType = new ArrayList<>();

        String groupName = "0";
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.openDataBase();

            String sql = "SELECT * FROM " + DataMembers.tbl_IncentiveDashboard + " Where inctype='" + type + "'";

            Cursor c = db.selectSQL(sql);

            IncentiveDashboardBO con;
            if (c != null) {
                while (c.moveToNext()) {
                    con = new IncentiveDashboardBO();//DId,DName,CNumber,Address1,Address2,Address3,Type,TinNo
                    con.setTgt(c.getString(c.getColumnIndex("tgt")));
                    con.setPayout(c.getString(c.getColumnIndex("payout")));
                    con.setMaxpayout(c.getString(c.getColumnIndex("maxpayout")));
                    con.setInctype(c.getString(c.getColumnIndex("inctype")));
                    con.setGroups(c.getString(c.getColumnIndex("groups")));
                    con.setFactor(c.getString(c.getColumnIndex("factor")));
                    con.setAchper(c.getString(c.getColumnIndex("achper")));
                    con.setAch(c.getString(c.getColumnIndex("ach")));
                    if (con.getGroups().equalsIgnoreCase("") || !groupName.equalsIgnoreCase(con.getGroups())) {
                        con.setIsNewGroup(true);
                        groupName = con.getGroups();
                    } else {
                        con.setIsNewGroup(false);
                    }

                    incentiveList.add(con);
                }
            }

            sql = "SELECT distinct inctype FROM " + DataMembers.tbl_IncentiveDashboard;

            c = db.selectSQL(sql);

            if (c != null) {
                while (c.moveToNext()) {
                    incentiveType.add(c.getString(0));
                }
            }

            c.close();
            db.closeDB();

            return incentiveList;
        } catch (Exception e) {
            Commons.print("" + e);
        }

        return new ArrayList<>();
    }


    public ArrayList<IncentiveDashboardDefinitionBO> downloadIncentiveDetails(String type) {
        try {

            String groupName = "0";
            String factorName = "0";
            String groupPackage = "0";

            ArrayList<IncentiveDashboardDefinitionBO> list = new ArrayList<>();

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.openDataBase();

            String sql = "SELECT * FROM IncentiveDashboardDefinition where inctype='" + type + "'";
            Cursor c = db.selectSQL(sql);

            while (c.moveToNext()) {
                IncentiveDashboardDefinitionBO incentiveDashboardDefinitionBO = new IncentiveDashboardDefinitionBO();

                incentiveDashboardDefinitionBO.setFactor(c.getString(0));
                incentiveDashboardDefinitionBO.setSalesParam(c.getString(1));
                incentiveDashboardDefinitionBO.setAchPercentage(c.getString(2));
                incentiveDashboardDefinitionBO.setMaxOpportunity(c.getString(3));
                if (c.getString(4) != null)
                    incentiveDashboardDefinitionBO.setGroups(c.getString(4));
                else
                    incentiveDashboardDefinitionBO.setGroups("");
                incentiveDashboardDefinitionBO.setIncentiveType(c.getString(5));

                if (incentiveDashboardDefinitionBO.getSalesParam().trim().equalsIgnoreCase("") || !groupName.equalsIgnoreCase(incentiveDashboardDefinitionBO.getSalesParam())) {
                    incentiveDashboardDefinitionBO.setIsNewGroup(true);
                    groupName = incentiveDashboardDefinitionBO.getSalesParam();
                } else {
                    incentiveDashboardDefinitionBO.setIsNewGroup(false);
                }

                if (!incentiveDashboardDefinitionBO.getFactor().equalsIgnoreCase(factorName)) {
                    incentiveDashboardDefinitionBO.setIsNewFactor(true);
                    factorName = incentiveDashboardDefinitionBO.getFactor();
                } else {
                    incentiveDashboardDefinitionBO.setIsNewFactor(false);
                }

                if (incentiveDashboardDefinitionBO.getGroups().trim().equalsIgnoreCase("") || !incentiveDashboardDefinitionBO.getGroups().equalsIgnoreCase(groupPackage)) {
                    incentiveDashboardDefinitionBO.setNewPackage(true);
                    groupPackage = incentiveDashboardDefinitionBO.getGroups();
                } else {
                    incentiveDashboardDefinitionBO.setNewPackage(false);
                }


                list.add(incentiveDashboardDefinitionBO);

            }
            c.close();
            db.closeDB();

            return list;

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return new ArrayList<>();
    }

    public ArrayList<String> getIncentiveType() {
        return incentiveType;
    }

    public void setIncentiveType(ArrayList<String> incentiveType) {
        this.incentiveType = incentiveType;
    }

    public ArrayList<String> getIncentiveGroups() {
        return incentiveGroups;
    }

    public void setIncentiveGroups(ArrayList<String> incentiveGroups) {
        this.incentiveGroups = incentiveGroups;
    }

    private int getParentId(int contentLevel, int parentLevel, int parentID) {
        int parentId = 0;
        int tempParentId = parentID;
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        db.openDataBase();

        int loopEnd = contentLevel - parentLevel;
        for (int i = 2; i <= loopEnd; i++) {
            String query = "SELECT DISTINCT PM1.ParentId FROM ProductMaster PM1 WHERE PM1.PID = " + tempParentId;
            Cursor c = db.selectSQL(query);
            if (c != null) {
                while (c.moveToNext()) {
                    tempParentId = c.getInt(0);
                }
            }
        }
        parentId = tempParentId;
        return parentId;
    }


    /**
     * @param flag
     * @return
     * @See {@link SellerDashboardDataManagerImpl#fetchPromotionCount()
     * @link SellerDashboardDataManagerImpl#fetchPromotionExecutedCount()}
     * @deprecated
     */
    public int getPromotionDetail(String flag) {
        DBUtil db = null;
        int size = bmodel.getRetailerMaster().size();
        int count = 0;
        String chIDs = "";

        if (flag.equals("P")) {
            for (int i = 0; i < size; i++) {
                if (bmodel.getRetailerMaster().get(i).getIsToday() == 1) {
                    chIDs = chIDs + "," + bmodel.getChannelids();
                }
            }

            if (chIDs.endsWith(","))
                chIDs = chIDs.substring(0, chIDs.length() - 1);

            try {
                db = new DBUtil(mContext, DataMembers.DB_NAME);
                db.createDataBase();
                db.openDataBase();
                StringBuffer sb = new StringBuffer();
                sb.append("SELECT count(PromoID) FROM PromotionMapping where chid in (" + chIDs + ")");

                Cursor c = db.selectSQL(sb.toString());
                if (c.getCount() > 0) {
                    while (c.moveToNext()) {
                        count = c.getInt(0);
                    }
                }
                c.close();
                db.closeDB();
            } catch (Exception e) {
                Commons.printException("" + e);
            }
        } else {
            for (int i = 0; i < size; i++) {
                if (bmodel.getRetailerMaster().get(i).getIsToday() == 1) {
                    count = count + getPromotionExecDetail(bmodel.getRetailerMaster().get(i).getRetailerID());
                }
            }
        }

        return count;

    }

    private int getPromotionExecDetail(String retailerID) {
        DBUtil db = null;
        int count = 0;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.createDataBase();
            db.openDataBase();
            StringBuffer sb = new StringBuffer();
            sb.append("SELECT count( distinct PromotionID) FROM PromotionDetail where RetailerID =" + bmodel.QT(retailerID));

            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    count = c.getInt(0);
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }

        return count;

    }


    /**
     * @param flag
     * @return
     * @See {@link SellerDashboardDataManagerImpl#fetchMslCount()}
     * @deprecated
     */
    public int getMSLDetail(String flag) {
        DBUtil db;
        int size = bmodel.getRetailerMaster().size();
        int count = 0;
        String chIDs = "";
        String rids = "";
        if (flag.equals("P")) {
            mslProdIDs = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                if (bmodel.getRetailerMaster().get(i).getIsToday() == 1) {
                    chIDs = chIDs + "," + bmodel.getChannelids();
                }
            }
            if (chIDs.startsWith(","))
                chIDs = chIDs.substring(1, chIDs.length());
            if (chIDs.endsWith(","))
                chIDs = chIDs.substring(0, chIDs.length() - 1);

            try {
                db = new DBUtil(mContext, DataMembers.DB_NAME);
                db.createDataBase();
                db.openDataBase();
                StringBuffer sb = new StringBuffer();
                sb.append("SELECT PTGM.pid FROM ProductTaggingMaster PTM ");
                sb.append("inner join ProductTaggingGroupMapping PTGM on PTGM.groupid = PTCM.groupid ");
                sb.append("inner join  ProductTaggingCriteriaMapping PTCM on PTM.groupid = PTCM.groupid ");
                sb.append("AND PTM.TaggingTypelovID in (select listid from standardlistmaster where listcode='MSL' and listtype='PRODUCT_TAGGING') ");
                sb.append("where PTCM.ChannelId in (" + chIDs + ")");

                Cursor c = db.selectSQL(sb.toString());
                if (c.getCount() > 0) {
                    while (c.moveToNext()) {
                        count++;
                        if (!mslProdIDs.contains(c.getInt(1)))
                            mslProdIDs.add(c.getInt(1));
                    }
                }
                c.close();
                db.closeDB();
            } catch (Exception e) {
                Commons.printException("" + e);
            }
        } else {
            for (int i = 0; i < size; i++) {
                if (bmodel.getRetailerMaster().get(i).getIsToday() == 1) {
                    rids = rids + "," + bmodel.getRetailerMaster().get(i).getRetailerID();
                }
            }
            if (rids.startsWith(","))
                rids = rids.substring(1, rids.length());
            if (rids.endsWith(","))
                rids = rids.substring(0, rids.length() - 1);

            count = count + getMslExecDetail(rids, mslProdIDs);
        }
        return count;

    }

    private int getMslExecDetail(String retailerID, ArrayList<Integer> mslProdIDs) {
        DBUtil db;
        int count = 0;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.createDataBase();
            db.openDataBase();
            StringBuffer sb = new StringBuffer();
            sb.append("select count(*) from OrderDetail where retailerid in (" + retailerID + ")");
            if (mslProdIDs != null && !mslProdIDs.isEmpty())
                sb.append("and ProductID in (" + mslProdIDs + ")");
            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    count = c.getInt(0);
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }

        return count;

    }

    @Deprecated
    /**
     * @See {@link SellerDashboardDataManagerImpl#fetchOutletDailyReport()}
     */
    public DailyReportBO downloadDailyReport() {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
        db.openDataBase();
        StringBuffer sb = new StringBuffer();
        DailyReportBO dailyRep = new DailyReportBO();
        Cursor c = null;

        if (!bmodel.configurationMasterHelper.IS_INVOICE) {
            sb.append("select  count(distinct retailerid),sum(linespercall),sum(ordervalue) from OrderHeader ");
            sb.append("where upload!='X' and OrderDate=" + QT(bmodel.userMasterHelper.getUserMasterBO().getDownloadDate()));
            c = db
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
            sb.append("where InvoiceDate=" + QT(bmodel.userMasterHelper.getUserMasterBO().getDownloadDate()));
            c = db
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
        sb.append("where upload!='X' and OrderDate=" + QT(bmodel.userMasterHelper.getUserMasterBO().getDownloadDate()));
        c = db
                .selectSQL(sb.toString());
        if (c != null) {
            if (c.moveToNext()) {

                dailyRep.setMspValues(c.getString(0));
                dailyRep.setNoofOrder(c.getString(1));

            }
            c.close();
        }


        db.closeDB();
        return dailyRep;
    }


    /**
     * @return
     * @See {@link SellerDashboardDataManagerImpl#fetchNoOfInvoiceAndValue()}
     * @deprecated
     */
    public DailyReportBO getNoOfInvoiceAndValue() {
        DailyReportBO dailyRp = new DailyReportBO();
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select count(distinct Inv.InvoiceNo),sum(Inv.totalamount) from Invoicemaster Inv" +
                            " INNER JOIN OrderHeader OH ON OH.orderId=Inv.OrderId where Inv.invoicedate = "
                            + QT(bmodel.userMasterHelper.getUserMasterBO().getDownloadDate()));
            if (c != null) {
                if (c.getCount() > 0) {
                    while (c.moveToNext()) {
                        dailyRp.setTotLines(c.getInt(0) + "");
                        dailyRp.setTotValues(c.getDouble(1) + "");
                    }
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("Error at getNoOfInvoiceAndValue", e);
        }
        return dailyRp;
    }

    /**
     * @return
     * @See {@link SellerDashboardDataManagerImpl#fetchNoOfOrderAndValue()}
     * @deprecated
     */
    public DailyReportBO getNoOfOrderAndValue() {
        DailyReportBO dailyRp = new DailyReportBO();
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            Cursor c = db
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

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("Error at getNoOfOrderAndValue", e);
        }
        return dailyRp;
    }

    @Deprecated
    /**
     * @See {@link SellerDashboardDataManagerImpl#fetchFocusBrandInvoiceAmt()}
     */
    public DailyReportBO getFocusBrandInvoiceAmt() {
        DailyReportBO dailyRp = new DailyReportBO();
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            StringBuffer sb = new StringBuffer();
            sb.append("select count(distinct OrderID),sum(FocusPackValues) from OrderHeader");
            sb.append(" where invoicestatus=1");
            Cursor c = db
                    .selectSQL(sb.toString());
            if (c != null) {
                if (c.getCount() > 0) {
                    while (c.moveToNext()) {
                        dailyRp.setEffCoverage(c.getInt(0) + "");
                        dailyRp.setTotValues(c.getDouble(1) + "");
                    }
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("Error at getFocusBrandInvoiceAmt", e);
        }
        return dailyRp;
    }

    /**
     * @return
     * @See {@link SellerDashboardDataManagerImpl#fetchSalesReturnValue()}
     * @deprecated
     */
    public double getSalesReturnValue() {
        double sale_return_value = 0;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            StringBuffer sb = new StringBuffer();
            sb.append("select count(distinct uid),sum(ReturnValue) from SalesReturnHeader where upload!='X'");
            Cursor c = db
                    .selectSQL(sb.toString());
            if (c != null) {
                if (c.getCount() > 0) {
                    while (c.moveToNext()) {
                        sale_return_value = c.getDouble(1);
                    }
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("Error at getFocusBrandInvoiceAmt", e);
        }
        return sale_return_value;
    }

    /**
     * @return
     * @See {@link SellerDashboardDataManagerImpl#fetchFulfilmentValue()}
     * @deprecated
     */
    public DailyReportBO getFullFillmentValue() {
        DailyReportBO dailyRp = new DailyReportBO();
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            String query = "select VL.pcsqty,VL.outerqty,VL.douomqty,VL.caseqty,VL.duomqty,"
                    + "(select qty from StockInHandMaster where pid = VL.pid) as SIHQTY,"
                    + "(select srp1 from PriceMaster where scid = 0 and pid = VL.pid) as price from VanLoad VL"
                    + " inner join stockapply sa on sa.uid = vl.uid";
            Cursor c = db
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

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("Error at getFullFillmentValue", e);
        }
        return dailyRp;
    }

    /**
     * @return
     * @See {@link SellerDashboardDataManagerImpl#fetchTotalCallsForTheDayExcludingDeviatedVisits()}
     * @deprecated
     */
    @Deprecated
    public int getTotalCallsForTheDayExcludingDeviatedVisits() {
        int total_calls = 0;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT COUNT(DISTINCT RM.RETAILERID) FROM RETAILERMASTER RM inner join RetailerMasterInfo RMI " +
                            "on RM.RetailerID = RMI.RetailerId where RMI.istoday = 1");
            if (c != null) {
                if (c.getCount() > 0) {
                    while (c.moveToNext())
                        total_calls = c.getInt(0);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("Error at getTotalCallsForTheDayExcludingDeviatedVisits", e);
        }
        return total_calls;
    }

    /**
     * @return
     * @See {@link SellerDashboardDataManagerImpl#getVisitedCallsForTheDayExcludingDeviatedVisits()}
     * @deprecated
     */
    public int getVisitedCallsForTheDayExcludingDeviatedVisits() {
        int visited_calls = 0;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            Cursor c = db.selectSQL("select count(distinct RM.retailerid) from retailermaster RM inner join " +
                    "RetailerBeatMapping RBM on RM.RetailerId = RBM.Retailerid where RBM.isdeviated='N' and RBM.isVisited = 'Y'");
            if (c != null) {
                if (c.getCount() > 0) {
                    if (c.moveToNext()) {
                        visited_calls = c.getInt(0);
                    }
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(
                    "Error at getVisitedCallsForTheDayExcludingDeviatedVisits", e);
        }
        return visited_calls;
    }

    public int getProductiveCallsForTheDayExcludingDeviatedVisits() {
        int productive_calls = 0;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = null;
            if (bmodel.configurationMasterHelper.IS_INVOICE && !bmodel.configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED) {

                if (bmodel.beatMasterHealper.getTodayBeatMasterBO() == null
                        || bmodel.beatMasterHealper.getTodayBeatMasterBO().getBeatId() == 0) {
                    c = db.selectSQL("select distinct(i.Retailerid) from InvoiceMaster i" +
                            " inner join retailermaster r on i.retailerid=r.retailerid inner join " +
                            "RetailerBeatMapping RBM on r.retailerid = RBM.Retailerid where RBM.isdeviated='N' and RBM.isVisited = 'Y'");
                } else {
                    c = db.selectSQL("select  distinct(i.Retailerid) from InvoiceMaster i inner join retailermaster r on "
                            + "i.retailerid=r.retailerid  inner join Retailermasterinfo RMI on RMI.retailerid= R.retailerid "
                            + "inner join RetailerBeatMapping RBM on r.retailerid = RBM.Retailerid"
                            + " where RBM.isdeviated='N' or RMI.isToday=1 and i.IsPreviousInvoice = 0 and RBM.isVisited = 'Y'");
                }
            } else {
                c = db.selectSQL("select  distinct(r.Retailerid) from OrderHeader o inner join retailermaster r " +
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

            db.closeDB();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return productive_calls;
    }

    /**
     * This method will return the total calls planned for today. On Week day
     * the Deviated calls will be considered. On week end it will return total
     * deviated retailers count.
     *
     * @return plannedCallsForTodayCount
     */
    public int getTotalCallsForTheDay() {
        int total_calls = 0;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            Cursor c = null;
            if (bmodel.beatMasterHealper == null
                    || bmodel.beatMasterHealper.getTodayBeatMasterBO() == null
                    || bmodel.beatMasterHealper.getTodayBeatMasterBO().getBeatId() == 0) {
                c = db.selectSQL("select  distinct RM.RetailerID from RetailerMaster RM"
                        + " inner join Retailermasterinfo RMI on RMI.retailerid= RM.retailerid"
                        + " LEFT JOIN RetailerBeatMapping RBM ON RBM.RetailerID = RM.RetailerID"
                        + " where RBM.isdeviated='Y' or RMI.isToday='1'");
            } else {
                c = db.selectSQL("select  distinct RM.RetailerID from RetailerMaster RM"
                        + " inner join Retailermasterinfo RMI on RMI.retailerid= RM.retailerid"
                        + " LEFT JOIN RetailerBeatMapping RBM ON RBM.RetailerID = RM.RetailerID"
                        + " where RBM.isdeviated='Y' or RMI.isToday='1'");
            }
            if (c != null) {
                if (c.getCount() > 0) {
                    total_calls = c.getCount();
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("Error at calculating planned call.", e);
        }
        return total_calls;
    }

    /**
     * This method will return the total retailers visited today. Atleast one
     * activity for Eg. stockcheck has to be executed to consider as visited. On
     * Week days the Deviated calls wont get counted.
     *
     * @return plannedCallsForTodayCount
     */
    public int getVisitedCallsForTheDay() {
        int visited_calls = 0;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            Cursor c = null;
            c = db.selectSQL("select count(distinct RM.retailerid) from retailermaster RM" +
                    " LEFT JOIN RetailerBeatMapping RBM ON RBM.RetailerID = RM.RetailerID" +
                    " where RBM.isvisited='Y'");// and
            // isdeviated='N'
            if (c != null) {
                if (c.getCount() > 0) {
                    if (c.moveToNext()) {
                        visited_calls = c.getInt(0);
                    }
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(
                    "Error at calculating visited calls for the day.", e);
        }
        return visited_calls;
    }


    /**
     * @return ProductiveCallsForTheDay
     * @See {@link SellerDashboardDataManagerImpl#getProductiveCallsForDay()}
     * This method will return the productive retailers count for the Day. For
     * Van Seller, this method will get distinct retailer count from
     * InvoiceTable and For Pre-seller from OrderHeader if PRD_FOR_ORDER is TRUE and from ClosingStockHeader if PRD_FOR_STK is true.
     * This config is computed through loadProductiveCallsConfig()
     * Deviated retailer productivity wont be considered for deviated retailers.
     * @deprecated
     */
    public int getProductiveCallsForTheDay() {
        int productive_calls = 0;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = null;
            if (bmodel.configurationMasterHelper.IS_INVOICE && !bmodel.configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED) {
                // c =
                // db.selectSQL("SELECT distinct(Retailerid) FROM InvoiceMaster");

                if (bmodel.beatMasterHealper.getTodayBeatMasterBO() == null
                        || bmodel.beatMasterHealper.getTodayBeatMasterBO().getBeatId() == 0) {
                    c = db.selectSQL("select  distinct(Retailerid) from InvoiceMaster where upload='N'");
                } else {
                    c = db.selectSQL("select  distinct(i.Retailerid) from InvoiceMaster i inner join retailermaster r on "
                            + "i.retailerid=r.retailerid  inner join Retailermasterinfo RMI on RMI.retailerid= R.retailerid "
                            + "LEFT JOIN RetailerBeatMapping RBM ON RBM.RetailerID = r.RetailerID"
                            + " where RBM.isdeviated='Y' or RMI.isToday=1 and i.IsPreviousInvoice = 0 ");
                }
            } else {
                // c =
                // db.selectSQL("select distinct(RetailerId) from OrderHeader");
                loadProductiveCallsConfig();
                if (PRD_FOR_ORDER) {
                    if (bmodel.beatMasterHealper.getTodayBeatMasterBO() == null
                            || bmodel.beatMasterHealper.getTodayBeatMasterBO().getBeatId() == 0) {
                        c = db.selectSQL("select  distinct(Retailerid) from OrderHeader where upload!='X'");
                    } else {
                        c = db.selectSQL("select  distinct(o.Retailerid) from OrderHeader o inner join retailermaster r on "
                                + "o.retailerid=r.retailerid where o.upload!='X' ");// where
                        // r.isdeviated='N'
                    }
                } else if (PRD_FOR_SKT) {
                    if (bmodel.beatMasterHealper.getTodayBeatMasterBO() == null
                            || bmodel.beatMasterHealper.getTodayBeatMasterBO().getBeatId() == 0) {
                        c = db.selectSQL("select  distinct(RetailerID) from ClosingStockHeader");
                    } else {
                        c = db.selectSQL("select  distinct(CSH.RetailerID) from ClosingStockHeader CSH INNER JOIN RetailerMaster RM on "
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

            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
        }

        return productive_calls;
    }

    public int getProductiveCallsForTheDayKlgs() {
        int productive_calls = 0;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = null;

            // c =
            // db.selectSQL("select distinct(RetailerId) from OrderHeader");
            loadProductiveCallsConfig();
            if (PRD_FOR_ORDER) {
                if (bmodel.beatMasterHealper.getTodayBeatMasterBO() == null
                        || bmodel.beatMasterHealper.getTodayBeatMasterBO().getBeatId() == 0) {
                    c = db.selectSQL("select  distinct(Retailerid) from OrderHeader where upload!='X'");
                } else {
                    c = db.selectSQL("select  distinct(o.Retailerid) from OrderHeader o inner join retailermaster r on "
                            + "o.retailerid=r.retailerid where o.upload!='X' ");// where
                    // r.isdeviated='N'
                }
            } else if (PRD_FOR_SKT) {
                if (bmodel.beatMasterHealper.getTodayBeatMasterBO() == null
                        || bmodel.beatMasterHealper.getTodayBeatMasterBO().getBeatId() == 0) {
                    c = db.selectSQL("select  distinct(RetailerID) from ClosingStockHeader");
                } else {
                    c = db.selectSQL("select  distinct(CSH.RetailerID) from ClosingStockHeader CSH INNER JOIN RetailerMaster RM on "
                            + "CSH.RetailerID=RM.RetailerID ");
                }
            }

            if (c != null) {
                if (c.getCount() > 0) {
                    productive_calls = c.getCount();
                }
                c.close();
            }

            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
        }

        return productive_calls;
    }

    /* This method will download the config for the productivecall. Based on the RField
     * value the productive config will turn ON and accordingly the productiveCalls values will be computed*/

    public void loadProductiveCallsConfig() {
        try {
            PRD_FOR_ORDER = false;
            PRD_FOR_SKT = false;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.openDataBase();

            String sql = "SELECT RField FROM "
                    + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode=" + QT(PRODUCTVIE_CALLS) + " AND flag='1' and ForSwitchSeller = 0";

            Cursor c = db.selectSQL(sql);

            if (c != null && c.getCount() != 0) {
                while (c.moveToNext()) {
                    if (c.getString(0).equalsIgnoreCase(PRD_ORD))
                        PRD_FOR_ORDER = true;
                    else if (c.getString(0).equalsIgnoreCase(PRD_STK))
                        PRD_FOR_SKT = true;

                }
                c.close();
            }
            db.closeDB();

        } catch (Exception e) {
            Commons.printException("loadProductiveCallsConfigs " + e);
        } finally {
            if (!PRD_FOR_ORDER && !PRD_FOR_SKT)
                PRD_FOR_ORDER = true;
        }
    }

    public float getTotalWeight(String retailerid) {
        DBUtil db;
        float totalWeight = 0;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME
            );

            db.openDataBase();
            StringBuffer sb = new StringBuffer();
            sb.append("select sum(totalweight) from orderheader where OrderDate=");
            sb.append(bmodel.QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)));
            if (!retailerid.equals("")) {
                sb.append("and upload!='X' and retailerid=" + bmodel.QT(retailerid));
            }
            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                if (c.moveToFirst()) {
                    totalWeight = c.getFloat(0);

                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.print(e.getMessage());
        }
        return totalWeight;
    }

    public String getLastDownloadDate() {
        String downloadDate = "";

        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.createDataBase();
            db.openDataBase();

            Cursor c = db.selectSQL("select timestamp from SellerKPIDetail order by TimeStamp desc limit 1");
            if (c != null && c.getCount() > 0 && c.moveToNext()) {
                downloadDate = c.getString(0);
                c.close();
            }

            db.closeDB();

            if (downloadDate != null && !downloadDate.equals("")) {

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss",
                        Locale.ENGLISH);
                Date date = sdf.parse(downloadDate);
                sdf = new SimpleDateFormat(ConfigurationMasterHelper.outDateFormat + " HH:mm:ss", Locale.ENGLISH);

                downloadDate = mContext.getResources().getString(R.string.last_download_on)
                        + " " + sdf.format(date);

            }

        } catch (Exception e) {
            Commons.printException(
                    "Error at getLastDownloadDate In Dashboard.", e);
        }

        return downloadDate;
    }

}
