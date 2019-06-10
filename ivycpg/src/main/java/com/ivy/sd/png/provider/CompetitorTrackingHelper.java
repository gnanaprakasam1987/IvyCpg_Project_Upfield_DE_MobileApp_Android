package com.ivy.sd.png.provider;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.Utils;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.CompanyBO;
import com.ivy.sd.png.bo.CompetetorPOSMBO;
import com.ivy.sd.png.bo.CompetitorBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;

public class CompetitorTrackingHelper {

    private Context mContext;
    private BusinessModel bmodel;
    private static CompetitorTrackingHelper instance = null;
    private ArrayList<CompetitorBO> competitorMaster;
    private ArrayList<CompanyBO> companyList;
    private ArrayList<CompetetorPOSMBO> trackingList;

    protected CompetitorTrackingHelper(Context context) {
        this.mContext = context;
        this.bmodel = (BusinessModel) context.getApplicationContext();
    }

    public static CompetitorTrackingHelper getInstance(Context context) {
        if (instance == null) {
            instance = new CompetitorTrackingHelper(context);
        }
        return instance;
    }

    /**
     * Download Companies which are competitor for the isOwn Company and set in
     * the objects
     */
    public void downloadCompanyMaster(String menucode) {
        DBUtil db = null;
        try {
            CompanyBO competitor;
            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT distinct CM.CompanyID,CM.CompanyName FROM CompanyMaster CM"
                            + " INNER JOIN CompetitorMappingMaster cpm on cpm.cpid=cp.cpid"
                            + " INNER JOIN competitorProductmaster cp on cp.CompanyID=cM.CompanyID"
                            + " where CP.plid in (select ProductFilter1 from configactivityfilter where ActivityCode =" + bmodel.QT(menucode) + ") and cm.isown != 1");

            if (c != null) {
                companyList = new ArrayList<CompanyBO>();

           /*     competitor = new CompanyBO();
                competitor.setCompetitorid(-1);
                competitor.setCompetitorName(mContext.getResources().getString(R.string.all));
                companyList.add(competitor);*/

                while (c.moveToNext()) {
                    competitor = new CompanyBO();
                    competitor.setCompetitorid(c.getInt(0));
                    competitor.setCompetitorName(c.getString(1));
                    companyList.add(competitor);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
            db.closeDB();
        }
    }

    public void downloadPriceCompanyMaster(String menucode) {
        DBUtil db = null;
        try {
            CompanyBO competitor;
            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT distinct CM.CompanyID,CM.CompanyName FROM CompanyMaster CM"
                            + " INNER JOIN CompetitorMappingMaster cpm on cpm.cpid=cp.cpid"
                            + " INNER JOIN competitorProductmaster cp on cp.CompanyID=cM.CompanyID"
                            + " where CP.plid in (select ProductContent from configactivityfilter where ActivityCode =" + bmodel.QT(menucode) + ") and cm.isown != 1");

            if (c != null) {
                companyList = new ArrayList<CompanyBO>();

                while (c.moveToNext()) {
                    competitor = new CompanyBO();
                    competitor.setCompetitorid(c.getInt(0));
                    competitor.setCompetitorName(c.getString(1));
                    companyList.add(competitor);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
            db.closeDB();
        }
    }


    /**
     * Downlaod Competitior Products which are related to Competitor Company
     */
    public void downloadCompetitors(String moduleCode) {
        DBUtil db = null;
        try {
            CompetitorBO competitorBo;
            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.openDataBase();
            Cursor c = null;

            c = db.selectSQL("Select levelname from productlevel"
                    + " where levelid= (select ProductFilter1 from configactivityfilter where ActivityCode ="
                    + bmodel.QT(moduleCode) + ")");
            if (c != null) {
                while (c.moveToNext()) {
                    DataMembers.COMP_PLEVELNAME = c.getString(0);
                }
            }

            c = db.selectSQL("Select CPM.CPCode,CM.CompanyName  ||'  '|| CPM.CPName as CPName ,CPM.CPLId,CPM.CPID,CPM.CompanyID from CompetitorProductMaster CPM INNER JOIN CompanyMaster CM on CM.CompanyID = CPM.CompanyID "
                    + " where plid in (select ProductFilter1 from configactivityfilter where ActivityCode ="
                    + bmodel.QT(moduleCode) + ")");
            if (c != null) {
                competitorMaster = new ArrayList<CompetitorBO>();
                while (c.moveToNext()) {
                    competitorBo = new CompetitorBO();
                    competitorBo.setProductcode(c.getString(0));
                    competitorBo.setProductname(c.getString(1));
                    competitorBo.setPlevelid(c.getInt(2));
                    competitorBo.setCompetitorpid(c.getInt(3));
                    competitorBo
                            .setCompetitoreason(cloneTrackingList(trackingList));
                    competitorBo.setCompanyID(c.getInt(4));
                    competitorMaster.add(competitorBo);

                }

                c.close();
            }
            // Competitor Tagging
            if (!competitorMaster.isEmpty()) {
                String productIDs =ProductTaggingHelper.getInstance(mContext).getCompetitorTaggingDetails(mContext,"COMPETITOR");
                ArrayList<CompetitorBO> tempList = new ArrayList<>();
                if (productIDs != null && !productIDs.trim().equals("")) {
                    for (CompetitorBO competitorBO : competitorMaster) {
                        if (productIDs.contains(competitorBO.getCompetitorpid() + ""))
                            tempList.add(competitorBO);
                    }

                    competitorMaster.clear();
                    competitorMaster.addAll(tempList);
                }
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
            db.closeDB();
        }
    }

    /**
     * Dwonload Tacking List for the Competitor Tracking, and this tacking list
     * will be same for the all Competitor Products
     */
    public void downloadTrackingList() {
        CompetetorPOSMBO reasonbo;
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("Select listid,ListName from StandardListMaster where ListType  ='COMPETITOR_TRACKING_TYPE'");
            if (c != null) {

                trackingList = new ArrayList<CompetetorPOSMBO>();
                while (c.moveToNext()) {
                    reasonbo = new CompetetorPOSMBO();
                    reasonbo.setId(c.getInt(0));
                    reasonbo.setName(c.getString(1));
                    trackingList.add(reasonbo);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
            db.closeDB();
        }

    }

    /**
     * Clone the Tacking list for Each competitors, this ArrayList Clone will
     * redcue the time to
     *
     * @param list
     * @return
     */
    public static ArrayList<CompetetorPOSMBO> cloneTrackingList(
            ArrayList<CompetetorPOSMBO> list) {
        ArrayList<CompetetorPOSMBO> clone = new ArrayList<CompetetorPOSMBO>(
                list.size());
        for (CompetetorPOSMBO item : list)
            clone.add(new CompetetorPOSMBO(item));
        return clone;
    }

    /**
     * Save the competitor details using Company wise Header and Details.
     */
    public void saveCompetitor() {
        CompetitorBO competitor;
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
        ;
        try {
            db.createDataBase();
            db.openDataBase();
            String headerColumns = "Tid,Date,RetailerID,CompetitorID,Feedback,ImageName,TimeZone,pid,Remark,CounterId,imgName,distributorid,ridSF,VisitId";
            String detailColumns = "TiD,TrackingListid,pid,RetailerID,FromDate,ToDate,Feedback,ImageName,imgName,qty,reasonID,RField1";

            String competitorReturnID = "CT"
                    + bmodel.getAppDataProvider().getUser().getUserid()
                    + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);

            Cursor orderDetailCursor = db.selectSQL("select tid from "
                    + DataMembers.tbl_CompetitorHeader + " where retailerid="
                    + QT(bmodel.retailerMasterBO.getRetailerID())
                    + " and date= " + QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL))
                    + " and upload!= 'Y'");

            if (orderDetailCursor.getCount() > 0) {
                while (orderDetailCursor.moveToNext()) {
                    db.deleteSQL(DataMembers.tbl_CompetitorHeader, "tid="
                            + Utils.QT(orderDetailCursor.getString(0)), false);
                    db.deleteSQL(DataMembers.tbl_CompetitorDetails, "tid="
                            + Utils.QT(orderDetailCursor.getString(0)), false);
                }
                // competitorReturnID = orderDetailCursor.getString(0);
            }

            orderDetailCursor.close();

            int siz = getCompetitorMaster()
                    .size();
            for (int i = 0; i < siz; ++i) {

                competitor = getCompetitorMaster().get(i);

                ArrayList<CompetetorPOSMBO> checktrackinglist = competitor
                        .getCompetitoreason();

                boolean checked = false;
                for (CompetetorPOSMBO temp : checktrackinglist) {
                    if (temp.isExecuted()) {
                        checked = true;
                        break;
                    }
                }

                if (!competitor.getFeedBack().isEmpty()
                        || competitor.getFeedBack().length() != 0 || checked) {

                    String values = QT(competitorReturnID + "-"
                            + competitor.getCompetitorpid())
                            + ","
                            + QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL))
                            + ","
                            + QT(bmodel.retailerMasterBO.getRetailerID())
                            + ","
                            + competitor.getCompanyID()
                            + ","
                            + QT(competitor.getFeedBack())
                            + ","
                            + QT(competitor.getImagePath())
                            + ","
                            + QT(DateTimeUtils.getTimeZone())
                            + ","
                            + competitor.getCompetitorpid()
                            + ","
                            + QT(bmodel.getNote())
                            + ","
                            + bmodel.getCounterId()
                            + ","
                            + QT(competitor.getImageName())
                            + ","
                            + bmodel.retailerMasterBO.getDistributorId()
                            + ","
                            + QT(bmodel.getAppDataProvider().getRetailMaster().getRidSF())
                            + ","
                            + bmodel.getAppDataProvider().getUniqueId();

                    db.insertSQL(DataMembers.tbl_CompetitorHeader,
                            headerColumns, values);
                }

                ArrayList<CompetetorPOSMBO> trackinglist = competitor
                        .getCompetitoreason();
                for (CompetetorPOSMBO temp : trackinglist) {
                    if (temp.isExecuted()) {
                        String values = QT(competitorReturnID + "-"
                                + competitor.getCompetitorpid())
                                + ","
                                + temp.getId()
                                + ","
                                + competitor.getCompetitorpid()
                                + ","
                                + QT(bmodel.getAppDataProvider().getRetailMaster()
                                .getRetailerID())
                                + ","
                                + QT(DateTimeUtils
                                .convertToServerDateFormat(
                                        temp.getFromDate(),
                                        ConfigurationMasterHelper.outDateFormat))
                                + ","
                                + QT(DateTimeUtils
                                .convertToServerDateFormat(
                                        temp.getToDate(),
                                        ConfigurationMasterHelper.outDateFormat))
                                + ","
                                + QT(temp.getFeedBack())
                                + ","
                                + QT(temp.getImagePath())
                                + ","
                                + QT(temp.getImageName())
                                + ","
                                + temp.getQty()
                                + ","
                                + temp.getReasonID()
                                + ","
                                + QT(temp.getRemarks());

                        db.insertSQL(DataMembers.tbl_CompetitorDetails,
                                detailColumns, values);
                    }
                }

            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
            db.closeDB();
        }
    }

    /**
     * Load Compertitors valus in Edit Mode
     */
    public void loadcompetitors() {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            String orderID = new String();
            // Order Header
            bmodel.setNote("");
            String sql = "select tid,competitorid,feedback,imagename,pid,Remark,imgName from "
                    + DataMembers.tbl_CompetitorHeader
                    + " where retailerid = "
                    + QT(bmodel.getRetailerMasterBO().getRetailerID())
                    + " and date="
                    + QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL))
                    + " and upload!= 'Y'";

            Cursor orderHeaderCursor = db.selectSQL(sql);

            if (orderHeaderCursor != null) {
                while (orderHeaderCursor.moveToNext()) {

                    orderID = orderHeaderCursor.getString(0);
                    int competitorid = orderHeaderCursor.getInt(1);
                    String feedback = orderHeaderCursor.getString(2);
                    String imagePath = orderHeaderCursor.getString(3);
                    int ppid = orderHeaderCursor.getInt(4);
                    bmodel.setNote(orderHeaderCursor.getString(5));
                    String imgName = orderHeaderCursor.getString(6);
                    setCompetitorDetails(competitorid, feedback, imagePath,
                            ppid, imgName);
                    // load details
                    String sql1 = "select trackinglistid,pid,FromDate,ToDate,feedback,imagename,imgName,qty,reasonID,ifnull(RField1,'') from "
                            + DataMembers.tbl_CompetitorDetails
                            + " where tid="
                            + QT(orderID) + "" + " and upload!= 'Y'";
                    Cursor orderDetailCursor = db.selectSQL(sql1);
                    if (orderDetailCursor != null) {
                        while (orderDetailCursor.moveToNext()) {

                            int trackingid = orderDetailCursor.getInt(0);
                            int prdid = orderDetailCursor.getInt(1);
                            String fromDate = orderDetailCursor.getString(2);
                            String toDate = orderDetailCursor.getString(3);
                            String mCfeedback = orderDetailCursor.getString(4);
                            String mCimagePath = orderDetailCursor.getString(5);
                            String mCimageName = orderDetailCursor.getString(6);
                            int qty = orderDetailCursor.getInt(7);
                            int reasonID = orderDetailCursor.getInt(8);
                            String remark = orderDetailCursor.getString(9);
                            setCompetitorMasterDetails(competitorid,
                                    trackingid, prdid, fromDate, toDate, mCfeedback, mCimagePath, mCimageName, qty, reasonID,remark);
                        }
                    }
                }
            }
            orderHeaderCursor.close();

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private void setCompetitorMasterDetails(int cid, int trackingid, int kid,
                                            String fromDate, String toDate, String feedback, String imagePath, String imageName, int qty, int reasonID,String remark) {
        CompetitorBO competitor;
        int siz = competitorMaster.size();
        Commons.print("B" + competitorMaster.size());
        if (siz == 0)
            return;
        for (int i = 0; i < siz; ++i) {
            competitor = (CompetitorBO) competitorMaster.get(i);
            if (competitor.getCompanyID() == cid
                    && competitor.getCompetitorpid() == kid) {

                ArrayList<CompetetorPOSMBO> trackinglist = competitor
                        .getCompetitoreason();
                for (CompetetorPOSMBO temp : trackinglist) {
                    if (temp.getId() == trackingid) {
                        temp.setExecuted(true);
                        temp.setFromDate(DateTimeUtils.convertFromServerDateToRequestedFormat(
                                fromDate,
                                bmodel.configurationMasterHelper.outDateFormat));
                        temp.setToDate(DateTimeUtils.convertFromServerDateToRequestedFormat(
                                toDate,
                                bmodel.configurationMasterHelper.outDateFormat));
                        temp.setFeedBack(feedback);
                        temp.setImagePath(imagePath);
                        temp.setImageName(imageName);
                        temp.setQty(qty);
                        temp.setReasonID(reasonID);
                        temp.setRemarks(remark);
                    }

                    competitorMaster.set(i, competitor);
                }
                competitorMaster.set(i, competitor);
                return;
            }
        }

        return;
    }

    private void setCompetitorDetails(int cid, String feedback, String imgPath,
                                      int ppid, String imgName) {
        CompetitorBO competitor;
        int siz = competitorMaster.size();
        Commons.print("C" + competitorMaster.size());
        if (siz == 0)
            return;
        for (int i = 0; i < siz; ++i) {
            competitor = (CompetitorBO) competitorMaster.get(i);
            if (competitor.getCompanyID() == cid
                    && competitor.getCompetitorpid() == ppid) {
                competitor.setFeedBack(feedback);
                competitor.setImagePath(imgPath);
                competitor.setImageName(imgName);
                competitor.setAchieved(true);
                competitorMaster.set(i, competitor);
                return;
            }
        }

        return;
    }

    public boolean hasoder() {
        CompetitorBO comp;
        int size = competitorMaster.size();
        if (size == 0)
            return false;

        for (int i = 0; i < size; ++i) {
            comp = (CompetitorBO) competitorMaster.get(i);
            ArrayList<CompetetorPOSMBO> trackinglist = comp
                    .getCompetitoreason();
            for (CompetetorPOSMBO temp : trackinglist) {
                if (temp.isExecuted() || !comp.getFeedBack().isEmpty()
                        || comp.getFeedBack().length() != 0 || !temp.getFeedBack().isEmpty()
                        || temp.getFeedBack().length() != 0
                        || !temp.getImageName().isEmpty() || temp.getImageName().length() != 0
                        || !temp.getImagePath().isEmpty() || temp.getImagePath().length() != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public void deleteImageName(String imgName) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        db.createDataBase();
        db.openDataBase();
        if (bmodel.configurationMasterHelper.IS_PHOTO_COMPETITOR)
            db.updateSQL("UPDATE " + DataMembers.tbl_CompetitorDetails
                    + " SET  imagename =" + QT("") + ",imgName=" + QT("") + " where imgName LIKE"
                    + QT(imgName + "%"));
        else
            db.updateSQL("UPDATE " + DataMembers.tbl_CompetitorHeader
                    + " SET  imagename =" + QT("") + ",imgName=" + QT("") + " where imgName LIKE"
                    + QT(imgName + "%"));
        db.closeDB();
    }

    public void deleteFiles(String folderPath, String fnamesStarts) {
        File folder = new File(folderPath);

        File files[] = folder.listFiles();
        if ((files == null) || (files.length < 1)) {
            folder = null;
            files = null;
            return;
        } else {

            for (File tempFile : files) {
                if (tempFile != null) {
                    if (tempFile.getName().startsWith(fnamesStarts))
                        tempFile.delete();
                }
            }
        }
    }

    public boolean getNoOfImages() {
        try {

            File f = new File(FileUtils.photoFolderPath);
            int count = 0;
            if (f.listFiles() != null) {
                String fnames[] = f.list();

                for (String str : fnames) {
                    if ((str != null) && (str.length() > 0)) {
                        if (str.endsWith(".jpg") || str.endsWith(".jpeg")) {
                            count++;
                        }
                    }

                }
                if (count >= bmodel.configurationMasterHelper.photocount) {
                    return true;
                }

            }

        } catch (Exception e) {
            Commons.printException(e);
        }
        return false;
    }

    public String QT(String data) // Quote
    {
        return "'" + data + "'";
    }

    public ArrayList<CompanyBO> getCompanyList() {
        return companyList;
    }

    public void setCompanyList(ArrayList<CompanyBO> companyList) {
        this.companyList = companyList;
    }

    public ArrayList<CompetitorBO> getCompetitorMaster() {
        return competitorMaster;
    }

    public void setCompetitorMaster(ArrayList<CompetitorBO> competitorMaster) {
        this.competitorMaster = competitorMaster;
    }

    public ArrayList<CompetetorPOSMBO> getTrackingList() {
        return trackingList;
    }

    public void setTrackingList(ArrayList<CompetetorPOSMBO> trackingList) {
        this.trackingList = trackingList;
    }
}
