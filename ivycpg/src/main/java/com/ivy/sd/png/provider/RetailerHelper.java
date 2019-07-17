package com.ivy.sd.png.provider;

import android.content.Context;
import android.database.Cursor;

import com.ivy.core.data.retailer.RetailerDataManagerImpl;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.DateWisePlanBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.RetailerMissedVisitBO;
import com.ivy.sd.png.bo.RtrWiseDeadProductsBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.bo.VisitConfiguration;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Vector;


public class RetailerHelper {
    private Context mContext;
    private BusinessModel bmodel;
    private static RetailerHelper instance = null;
    private ArrayList<RetailerMissedVisitBO> mMissedRetailerList;
    private ArrayList<RetailerMissedVisitBO> mMissedRetailerDetails;
    private List<VisitConfiguration> visitPlanning;
    private List<VisitConfiguration> visitCoverage;
    private String contractType;
    private String contractExpiryDate;
    private String contractID;
    private String contractStartDate;
    public ArrayList<RetailerMasterBO> deviateRetailerList;
    private ArrayList<StandardListBO> mRetailerSelectionFilter;
    public ArrayList<RetailerMasterBO> retailerTargetList;

    protected RetailerHelper(Context context) {
        this.mContext = context;
        this.bmodel = (BusinessModel) context;
    }

    public static RetailerHelper getInstance(Context context) {
        if (instance == null) {
            instance = new RetailerHelper(context);
        }
        return instance;
    }


    public String getContractType() {
        return contractType;
    }

    public String getContractExpiryDate() {
        return contractExpiryDate;
    }

    public String getContractID() {
        return contractID;
    }

    public String getContractStartDate() {
        return contractStartDate;
    }


    public void loadContractData() {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor cr = db
                    .selectSQL("SELECT IFNULL(ContractType,''), IFNULL(EndDate,''), IFNULL(ContractId,''), IFNULL(StartDate,'') FROM RetailerContract where RetailerId="
                            + bmodel.getRetailerMasterBO().getRetailerID());
            contractType = "";
            contractExpiryDate = "";
            contractID = "";
            contractStartDate = "";
            if (cr != null) {
                while (cr.moveToNext()) {
                    contractType = cr.getString(0);
                    contractExpiryDate = cr.getString(1);
                    contractID = cr.getString(2);
                    contractStartDate = cr.getString(3);
                }
                cr.close();
            }

            db.close();
            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
        }

    }


    /**
     * @deprecated
     * @see {@link com.ivy.ui.profile.data.ProfileDataManagerImpl#getParentLevelName(int, boolean)}
     * @param locid
     * @param isParent
     * @return
     */
    @Deprecated
    public String[] getParentLevelName(int locid, boolean isParent) {
        String[] parentLevel = new String[3];
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
        try {
            db.createDataBase();
            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("select locid,locName, LocLevelId from locationmaster where locid=");
            if (!isParent) {
                sb.append(locid);

            } else {
                sb.append("(select locParentId from locationmaster  where locid=");
                sb.append(locid);
                sb.append(")");
            }

            Cursor c = db.selectSQL(sb.toString());
            if (c != null) {
                if (c.getCount() > 0) {
                    if (c.moveToNext()) {

                        parentLevel[0] = c.getInt(0) + "";
                        parentLevel[1] = c.getString(1);
                        parentLevel[2] = c.getString(2);
                        c.close();
                    }
                }
                c.close();
            }

            sb.delete(0, sb.length());
            sb.append("select Name from LocationLevel where id=");
            sb.append(bmodel.QT(parentLevel[2]));
            Cursor c1 = db.selectSQL(sb.toString());
            if (c1 != null) {
                if (c1.getCount() > 0) {
                    if (c1.moveToNext()) {
                        parentLevel[2] = c1.getString(0);
                        db.closeDB();
                        return parentLevel;
                    }
                }
                c1.close();
            }

        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }
        return parentLevel;
    }

    public String getPhysicalLcoation(int locid) {
        String locationName = "";
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
        try {
            db.createDataBase();
            db.openDataBase();
            String sb = "select locName from locationmaster where locid= " + locid;

            Cursor c = db.selectSQL(sb);
            if (c != null) {
                if (c.getCount() > 0) {
                    if (c.moveToNext()) {
                        locationName = c.getString(0);
                        c.close();
                    }
                }
                c.close();
            }

        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }
        return locationName;
    }

    public String getGSTType(int locid) {
        String gstType = "";
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
        try {
            db.createDataBase();
            db.openDataBase();
            String sb = "select ListName from StandardListMaster where ListId= " +
                    locid + " AND ListType = 'CERTIFICATE_TYPE'";


            Cursor c = db.selectSQL(sb);
            if (c != null) {
                if (c.getCount() > 0) {
                    if (c.moveToNext()) {
                        gstType = c.getString(0);
                        c.close();
                    }
                }
                c.close();
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }
        return gstType;
    }

    public List<VisitConfiguration> getVisitPlanning() {
        return visitPlanning;
    }


    /**
     * Method to update planned retailer from date
     *
     * @See {@link com.ivy.core.data.retailer.RetailerDataManagerImpl#updatePlannedDatesForRetailer(ArrayList)}
     * @deprecated
     */
    @Deprecated
    public void getPlannedRetailerFromDate() {

        //Added by Rajkumar
        ArrayList<RetailerMasterBO> retailerWIthSequence = new ArrayList<>();
        ArrayList<RetailerMasterBO> retailerWithoutSequence = new ArrayList<>();
        for (RetailerMasterBO bo : bmodel.getRetailerMaster()) {
            if (bo.getWalkingSequence() != 0) {
                retailerWIthSequence.add(bo);
            } else {
                retailerWithoutSequence.add(bo);
            }
        }
        Collections.sort(retailerWIthSequence, RetailerMasterBO.WalkingSequenceComparator);
        Collections.sort(retailerWithoutSequence, RetailerMasterBO.RetailerNameComparator);
        bmodel.getRetailerMaster().clear();
        bmodel.getRetailerMaster().addAll(retailerWIthSequence);
        bmodel.getRetailerMaster().addAll(retailerWithoutSequence);
        int size = 0;

        Vector<RetailerMasterBO> retailerList = bmodel.retailerMaster;
        for (RetailerMasterBO retailerBO : retailerList) {
            HashSet<DateWisePlanBO> plannedRetailerList = retailerBO.getPlannedDates();
            if (plannedRetailerList != null) {
                for (DateWisePlanBO dateWisePlanBO : plannedRetailerList) {
                    int isToday = DateTimeUtils.compareDate(dateWisePlanBO.getDate(),
                            bmodel.userMasterHelper.getUserMasterBO().getDownloadDate(), "yyyy/MM/dd");
                    if (isToday == 0) {
                        retailerBO.setWalkingSequence(dateWisePlanBO.getWalkingSequence());
                        retailerBO.setIsToday(1);
                        size += 1;
                        break;
                    } else {
                        retailerBO.setIsToday(0);
                    }
                }

            }

        }

        String userLeaveSession;
        userLeaveSession = bmodel.getUserSession();
        //setting isToday based on the leave session
        if ((userLeaveSession.equalsIgnoreCase("AN") || userLeaveSession.equalsIgnoreCase("FN") || userLeaveSession.equalsIgnoreCase("FD")) && size > 0) {
            int tempSize;
            if (size % 2 == 0) {
                tempSize = (size / 2);
            } else {
                tempSize = ((size + 1) / 2);
            }

            int tempCount = 0;
            for (RetailerMasterBO retailer : bmodel.getRetailerMaster()) {
                if (retailer.getIsToday() == 1) {
                    tempCount += 1;
                    retailer.setIsToday(0);
                    if (userLeaveSession.equals("FN")) {
                        if (tempCount > (size - tempSize)) {
                            retailer.setIsToday(1);
                        }

                    } else if (userLeaveSession.equals("AN")) {
                        if (tempCount <= (size - tempSize)) {
                            retailer.setIsToday(1);
                        }
                    }

                }
            }
        }
    }

    public void setVisitPlanning(List<VisitConfiguration> visitPlanning) {
        this.visitPlanning = visitPlanning;
    }


    public ArrayList<String> getMaxDaysInRouteSelection() {
        ArrayList<String> routeSelectionDateList = new ArrayList<>();
        int maxDaysCount = 7;
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
        try {
            db.createDataBase();
            db.openDataBase();

            String sb = "select distinct date from retailerclientmappingmaster where  date >" +
                    bmodel.QT(bmodel.userMasterHelper.getUserMasterBO().getDownloadDate()) +
                    " ORDER BY date" +
                    " limit " + maxDaysCount;
            Cursor c = db.selectSQL(sb);
            if (c.getCount() > 0) {

                while (c.moveToNext()) {
                    routeSelectionDateList.add(c.getString(0));

                }
            }
            c.close();

        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }

        return routeSelectionDateList;
    }

    /**
     * @See {@link com.ivy.core.data.retailer.RetailerDataManagerImpl#updatePlannedDatesForRetailer(ArrayList)}
     * @deprecated
     */
    public void updatePlannedDatesInRetailerObj(DBUtil db) {
        try {
            db.openDataBase();
            String sb = "Select Rid,Date,RCm.WalkingSeq from RetailerClientMappingMaster RCM " +
                    "inner join RetailerMaster RM on RCM.rid=RM.retailerid order by rid";
            Cursor c = db.selectSQL(sb);
            if (c.getCount() > 0) {
                DateWisePlanBO dateWisePlanBO;
                while (c.moveToNext()) {
                    dateWisePlanBO = new DateWisePlanBO();
                    String retailerId = c.getString(0);
                    dateWisePlanBO.setDate(c.getString(1));
                    dateWisePlanBO.setWalkingSequence(c.getInt(2));
                    setRetaierPlannedDate(retailerId, dateWisePlanBO);
                }
            }
            c.close();

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    private void setRetaierPlannedDate(String retailerid, DateWisePlanBO dateWisePlanBO) {
        Vector<RetailerMasterBO> retailermaster = bmodel.retailerMaster;
        for (RetailerMasterBO retailermasterBo : retailermaster) {

            if (retailerid.equals(retailermasterBo.getRetailerID())) {
                HashSet<DateWisePlanBO> plannedDateList = retailermasterBo.getPlannedDates();
                if (plannedDateList == null) {
                    plannedDateList = new HashSet<>();
                    plannedDateList.add(dateWisePlanBO);
                } else {
                    plannedDateList.add(dateWisePlanBO);
                }
                retailermasterBo.setPlannedDates(plannedDateList);
                break;
            }


        }
    }

    /**
     * Method to download missed  retailer list
     *
     * @See {@link RetailerDataManagerImpl#fetchMissedRetailers()}
     * @deprecated
     */
    public void downloadMissedRetailer(DBUtil db) {
        mMissedRetailerList = new ArrayList<>();
        RetailerMissedVisitBO missedRetailerBO;
        try {
            db.openDataBase();
            String sb = "select distinct RMV.Retailerid,RM.RetailerName,RV.PlannedVisitCount,RBM.beatid from RetailerMissedVisit RMV" +
                    " inner join RetailerMaster RM on RM.RetailerId=RMV.RetailerId " +
                    " inner join RetailerBeatMapping RBM on RBM.RetailerID = RM.RetailerID" +
                    " LEFT JOIN RetailerVisit RV ON RV.RetailerID = RMV.RetailerID";
                    //" Group by RMV.MissedDate";
            ;
            Cursor c = db.selectSQL(sb);
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    missedRetailerBO = new RetailerMissedVisitBO();
                    missedRetailerBO.setRetailerId(c.getString(0));
                    missedRetailerBO.setRetailerName(c.getString(1));
                    missedRetailerBO.setPlannedVisitCount(c.getInt(2));
                    missedRetailerBO.setBeatId(c.getInt(3));
                    missedRetailerBO.setMissedCount(getRetailerCount(c.getInt(0)));
                    mMissedRetailerList.add(missedRetailerBO);
                }
            }
            c.close();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    private int getRetailerCount(int RetailerId) {
        int count = 0;
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
        try {
            db.createDataBase();
            db.openDataBase();
            String sb = "select ReasonId from RetailerMissedVisit where RetailerId = " + RetailerId;
            Cursor c = db.selectSQL(sb);
            count = c.getCount();
            c.close();
        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }
        return count;
    }

    /**
     * @return
     * @See {@link RetailerDataManagerImpl#fetchMissedRetailers()}
     * @deprecated
     */
    public ArrayList<RetailerMissedVisitBO> getMissedRetailerlist() {
        if (mMissedRetailerList != null) {
            return mMissedRetailerList;
        }
        return new ArrayList<>();
    }

    public void downloadMissedRetailerDetails(String Retailerid) {
        mMissedRetailerDetails = new ArrayList<>();
        RetailerMissedVisitBO missedRetailerBO;
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
        try {
            db.createDataBase();
            db.openDataBase();
            String sb = "select RM.RetailerName,RMV.MissedDate,IFNULL(R.ListName,'-'),RM.beatid from RetailerMissedVisit RMV " +
                    "inner join RetailerMaster RM on RM.RetailerId = " + bmodel.QT(Retailerid) +
                    "Left join StandardListMaster R on R.ListId=RMV.reasonId" +
                    " Where RMV.Retailerid = " + bmodel.QT(Retailerid) +
                    " Group by RMV.MissedDate";
            Cursor c = db.selectSQL(sb);
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    missedRetailerBO = new RetailerMissedVisitBO();
                    missedRetailerBO.setRetailerName(c.getString(0));
                    missedRetailerBO.setMissedDate(c.getString(1));
                    missedRetailerBO.setReasonDes(c.getString(2));
                    missedRetailerBO.setBeatId(c.getInt(3));
                    mMissedRetailerDetails.add(missedRetailerBO);
                }
            }
            c.close();
        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }

    }

    public ArrayList<RetailerMissedVisitBO> getmMissedRetailerDetails() {
        if (mMissedRetailerDetails != null) {
            return mMissedRetailerDetails;
        }
        return new ArrayList<>();
    }

    public List<VisitConfiguration> getVisitCoverage() {
        return visitCoverage;
    }

    public void setVisitCoverage(List<VisitConfiguration> visitCoverage) {
        this.visitCoverage = visitCoverage;
    }

    public String getREclassification(int id) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
        String listname = null;

        try {
            db.createDataBase();
            db.openDataBase();

            Cursor c = db.selectSQL("Select ListName from StandardListMaster where ListId='" + id + "'");
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    listname = c.getString(0);
                }
            }

        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }
        return listname;
    }

    public boolean IsRetailerGivenNoVisitReason() {


        int siz = bmodel.getRetailerMaster().size();
        for (int i = 0; i < siz; i++) {
            bmodel.getRetailerMaster().get(i).setHasNoVisitReason(false);
        }

        boolean flag = false;
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);


        try {
            db.createDataBase();
            db.openDataBase();
            String query = "SELECT RM.RetailerId FROM RetailerMaster RM"
                    + " INNER JOIN Nonproductivereasonmaster NP ON NP.RetailerID = RM.RetailerID"
                    + " AND NP.ReasonTypes =(select ListId from StandardListMaster where ListCode='NV')"
                    + " LEFT JOIN RetailerBeatMapping RBM ON RBM.RetailerID = RM.RetailerID"
                    + " WHERE RBM.isVisited = 'N'";
            Cursor c = db.selectSQL(query);
            if (c != null) {

                if (c.getCount() > 0) {
                    while (c.moveToNext()) {
                        String retId = c.getString(0);
                        for (int i = 0; i < siz; i++) {
                            if (bmodel.getRetailerMaster().get(i).getRetailerID().equals(retId)) {
                                bmodel.getRetailerMaster().get(i).setHasNoVisitReason(true);
                                break;
                            }
                        }
                    }

                }

            }

        } catch (Exception e) {
            Commons.printException("" + e);
        }
        db.closeDB();
        return flag;
    }

    public void downloadRetailerFilterSelection(String menuType) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
        mRetailerSelectionFilter = new ArrayList<>();


        try {
            db.createDataBase();
            db.openDataBase();
            String sb = "select hhtcode,MName from Hhtmenumaster " +
                    "where MenuType=" + bmodel.QT(menuType) +
                    "  and hhtcode like 'Filt%'";
            Cursor c = db.selectSQL(sb);
            if (c.getCount() > 0) {
                StandardListBO standardListBO;
                while (c.moveToNext()) {
                    standardListBO = new StandardListBO();
                    standardListBO.setListCode(c.getString(0));
                    standardListBO.setListName(c.getString(1));
                    mRetailerSelectionFilter.add(standardListBO);
                }
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }
    }

    public ArrayList<StandardListBO> getRetailerSelectionFilter() {
        if (mRetailerSelectionFilter != null)
            return mRetailerSelectionFilter;

        return new ArrayList<>();
    }

    /**
     * @deprecated
     * @See {@link RetailerDataManagerImpl#updateWalkingSequenceDayWise(RetailerMasterBO, String)}
     * @param db
     */
    public void updateWalkingSequenceDayWise(DBUtil db) {

        DateFormat sdf;
        String downloadDate = bmodel.userMasterHelper.getUserMasterBO().getDownloadDate();

        sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss 'EET' yyyy", Locale.ENGLISH);

        String currentDay = sdf.format(Date.parse(downloadDate))
                .substring(0, 3).toUpperCase(Locale.ENGLISH);

        String currentWeek = bmodel.getWeekText();

        String sb = "select retailerid,sequenceno from RetailerVisitSequence where " +
                " weekno=" + bmodel.QT(currentWeek) + " and day=" + bmodel.QT(currentDay);
        Cursor c = db.selectSQL(sb);
        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                String retailerid = c.getString(0);
                int seq = c.getInt(1);
                RetailerMasterBO retailerMasterBO = bmodel.getRetailerBoByRetailerID().get(retailerid);
                if (retailerMasterBO != null) {
                    retailerMasterBO.setWalkingSequence(seq);
                }

            }
        }
        c.close();
    }

    public String getColorCode(String value) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
        String color = "";
        try {
            db.createDataBase();
            db.openDataBase();
            String sb = "Select  *  from " +
                    "(select ListName ,substr(listcode,0,3) as fromrange,substr(listcode,4,7) as torange" +
                    " from standardlistmaster where listtype ='FIT_TYPE') where " + bmodel.QT(value) + " between fromrange and torange";
            Cursor c = db.selectSQL(sb);
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    color = c.getString(0);
                }
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }
        return color;
    }

    public ArrayList<RtrWiseDeadProductsBO> getmRtrWiseDeadProductsList() {
        return mRtrWiseDeadProductsList;
    }

    public void setmRtrWiseDeadProductsList(ArrayList<RtrWiseDeadProductsBO> mRtrWiseDeadProductsList) {
        this.mRtrWiseDeadProductsList = mRtrWiseDeadProductsList;
    }

    private ArrayList<RtrWiseDeadProductsBO> mRtrWiseDeadProductsList;

    /* get retailerwise dead products */
    public void downloadRetailerWiseDeadPdts(int RetailerId) {
        mRtrWiseDeadProductsList = new ArrayList<RtrWiseDeadProductsBO>();
        RtrWiseDeadProductsBO rtrWiseDeadProductsBO;
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.createDataBase();
            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM RtrWiseDeadProducts where rid = " + RetailerId);
            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    rtrWiseDeadProductsBO = new RtrWiseDeadProductsBO();
                    rtrWiseDeadProductsBO.setRid(c.getInt(c.getColumnIndex("rid")));
                    rtrWiseDeadProductsBO.setPid(c.getInt(c.getColumnIndex("pid")));
                    rtrWiseDeadProductsBO.setFlag(c.getString(c.getColumnIndex("flag")));
                    mRtrWiseDeadProductsList.add(rtrWiseDeadProductsBO);
                }
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.closeDB();
        }
    }

    public String getValidateUrl() {
        String url = "";

        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("select URL from UrlDownloadMaster where MasterName = 'TRADE_COVERAGE_VALIDATE'");

            Cursor c = db.selectSQL(sb.toString());
            if (c != null) {
                while (c.moveToNext()) {
                    url = c.getString(0);
                }
            }
        } catch (Exception ex) {
            Commons.printException(ex);
        }

        return url;
    }

    private ArrayList<RetailerMasterBO> getRetailerTargetList() {
        return retailerTargetList;
    }

    private void setRetailerTargetList(ArrayList<RetailerMasterBO> retailerTargetList) {
        this.retailerTargetList = retailerTargetList;
    }

    public void downloadRetailerTarget(String code,DBUtil db) {

        try {

            String sb = "select rk.retailerid,rk.interval,rkd.target,rk.kpiid,rkd.kpiparamlovid,rkd.achievement from RetailerKPI rk" +
                    " inner join RetailerKPIDetail rkd on rk.kpiid = rkd.kpiid INNER JOIN StandardListMaster SM" +
                    " ON SM.listid = rkd.KPIParamLovId where SM.listcode=" + bmodel.QT(code);

            Cursor c = db.selectSQL(sb);
            setRetailerTargetList(new ArrayList<RetailerMasterBO>());
            if (c != null) {
                while (c.moveToNext()) {

                    RetailerMasterBO retailerMasterBO = new RetailerMasterBO();
                    retailerMasterBO.setRetailerID(c.getString(0));
                    retailerMasterBO.setInterval(c.getString(1));
                    retailerMasterBO.setDaily_target(c.getDouble(2));
                    retailerMasterBO.setKpiid_day(c.getInt(3));
                    retailerMasterBO.setKpi_param_day(c.getInt(4));
                    retailerMasterBO.setMonthly_acheived(c.getDouble(5));
                    getRetailerTargetList().add(retailerMasterBO);
                }
            }

            if (c != null) {
                c.close();
            }

            sb = "select rk.retailerid,rk.interval,rkmd.target,rk.kpiid,rkmd.kpiparamlovid from RetailerKPI rk" +
                    " inner join RetailerKPIModifiedDetail rkmd on rk.kpiid = rkmd.kpiid INNER JOIN StandardListMaster SM" +
                    " ON SM.listid = rkmd.KPIParamLovId where SM.listcode=" + bmodel.QT(code);

            c = db.selectSQL(sb);
            ArrayList<RetailerMasterBO> tempList = new ArrayList<>();
            if (c != null) {
                while (c.moveToNext()) {

                    RetailerMasterBO retailerMasterBO = new RetailerMasterBO();
                    retailerMasterBO.setRetailerID(c.getString(0));
                    retailerMasterBO.setInterval(c.getString(1));
                    retailerMasterBO.setDaily_target(c.getDouble(2));
                    retailerMasterBO.setKpiid_day(c.getInt(3));
                    retailerMasterBO.setKpi_param_day(c.getInt(4));
                    tempList.add(retailerMasterBO);
                }
            }

            if (c != null) {
                c.close();
            }

            for (RetailerMasterBO masterBO : getRetailerTargetList()) {

                for (RetailerMasterBO retailerMasterBO : tempList) {
                    if (masterBO.getRetailerID().equals(retailerMasterBO.getRetailerID())) {
                        masterBO.setDaily_target(retailerMasterBO.getDaily_target());
                        masterBO.setMonthly_acheived(retailerMasterBO.getMonthly_acheived());
                        break;
                    }

                }
            }

            setRetailerTarget();
        } catch (Exception ex) {
            Commons.printException(ex);
        }
    }

    private void setRetailerTarget() {
        double dailyTgt;
        double monthlyTgt;
        double monthlyAch;

        for (RetailerMasterBO masterBO : bmodel.getRetailerMaster()) {
            dailyTgt = 0;
            monthlyTgt = 0;
            monthlyAch = 0;
            masterBO.setKpiid_day(0);
            for (RetailerMasterBO retailerMasterBO : getRetailerTargetList()) {
                if (masterBO.getRetailerID().equals(retailerMasterBO.getRetailerID())) {
                    if ("DAY".equals(retailerMasterBO.getInterval())) {
                        dailyTgt = retailerMasterBO.getDaily_target();
                        masterBO.setKpiid_day(retailerMasterBO.getKpiid_day());
                        masterBO.setKpi_param_day(retailerMasterBO.getKpi_param_day());
                    } else if ("MONTH".equals(retailerMasterBO.getInterval())) {
                        monthlyTgt = retailerMasterBO.getDaily_target();
                        monthlyAch = retailerMasterBO.getMonthly_acheived();
                    }

                    if (dailyTgt > 0 && monthlyTgt > 0 && monthlyAch > 0)
                        break;
                }
            }

            masterBO.setDaily_target(dailyTgt);
            masterBO.setMonthly_target(monthlyTgt);
            masterBO.setMonthly_acheived(monthlyAch);//Overwrite using the value got from sellerkpidetail
        }
    }
}

