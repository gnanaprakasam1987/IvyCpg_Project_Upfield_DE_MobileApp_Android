package com.ivy.cpg.view.planogram;

import android.content.Context;
import android.database.Cursor;
import android.widget.Toast;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ChildLevelBo;
import com.ivy.sd.png.bo.ParentLevelBo;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.Vector;

public class PlanoGramHelper {

    private final BusinessModel mBModel;
    private static PlanoGramHelper instance = null;
    private Vector<PlanoGramBO> mPlanoGramMaster;
    private Vector<ParentLevelBo> mParentLevelBo;
    private Vector<ChildLevelBo> mChildLevelBo;
    private Vector<StandardListBO> mLocationList;
    public String mSelectedActivityName;

    private static final String CODE_LOCATION_WISE_PLANOGRAM = "FUN39";
    boolean IS_LOCATION_WISE_PLANOGRAM;


    private PlanoGramHelper(Context context) {
        mBModel = (BusinessModel) context.getApplicationContext();
    }

    public static PlanoGramHelper getInstance(Context context) {
        if (instance == null) {
            instance = new PlanoGramHelper(context);
        }
        return instance;
    }

    public void clearInstance() {
        instance = null;
    }

    /**
     * Load PlanoGram screen specific configurations
     */
    public void loadConfigurations(Context mContext) {
        try {

            IS_LOCATION_WISE_PLANOGRAM = false;

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            String sql = "SELECT hhtCode, RField FROM "
                    + DataMembers.tbl_HhtModuleMaster
                    + " WHERE flag='1' and ForSwitchSeller = 0";

            Cursor c = db.selectSQL(sql);
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    if (c.getString(0).equalsIgnoreCase(CODE_LOCATION_WISE_PLANOGRAM))
                        IS_LOCATION_WISE_PLANOGRAM = true;
                }
                c.close();
            }
            db.closeDB();


        } catch (Exception e) {
            Commons.printException(e);
        }


    }

    /**
     * Download Product Levels For filter
     *
     * @param moduleName Module Name
     * @param retailerId RetaILER iD
     */
    public void downloadLevels(Context mContext, String moduleName, String retailerId) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
        try {
            db.openDataBase();

            int mParentLevel = 0;
            int mChildLevel = 0;
            String mParentLevelName = "";
            String mChildLevelName = "";

            ParentLevelBo mParentLevelBo;
            ChildLevelBo mChildLevelBo;

            Cursor filterCur = db
                    .selectSQL("SELECT IFNULL(PL1.Sequence,0), IFNULL(PL2.Sequence,0),"
                            + "  PL1.LevelName, PL2.LevelName"
                            + " FROM ConfigActivityFilter CF"
                            + " LEFT JOIN ProductLevel PL1 ON PL1.LevelId = CF.ProductFilter1"
                            + " LEFT JOIN ProductLevel PL2 ON PL2.LevelId = CF.ProductFilter2"
                            + " LEFT JOIN ProductLevel PL3 ON PL3.LevelId = CF.ProductContent"
                            + " WHERE CF.ActivityCode = '" + moduleName + "'");

            if (filterCur != null) {
                if (filterCur.moveToNext()) {
                    mParentLevel = filterCur.getInt(0);
                    mChildLevel = filterCur.getInt(1);
                    mParentLevelName = filterCur.getString(2);
                    mChildLevelName = filterCur.getString(3);

                }
                filterCur.close();
            }
            String str;
            int level = mBModel.productHelper.getRetailerlevel("MENU_PLANOGRAM");
            if (level == -1) {
                Toast.makeText(mContext, mContext.getResources().getString(R.string.data_not_mapped_correctly), Toast.LENGTH_SHORT).show();
                return;
            } else {
                str = " and MP.AccId in (0," + mBModel.getRetailerMasterBO().getAccountid()
                        + ") and MP.RetailerId in (0," + retailerId
                        + ") and MP.ClassId in (0," + mBModel.getRetailerMasterBO().getClassid()
                        + ") and MP.LocId in (0," + mBModel.productHelper.getMappingLocationId(mBModel.productHelper.locid, mBModel.getRetailerMasterBO().getLocationId())
                        + ") and MP.ChId in (0," + mBModel.productHelper.getMappingChannelId(mBModel.productHelper.chid, mBModel.getRetailerMasterBO().getSubchannelid()) + ")";

            }

            // Two Level Filter
            if (mParentLevel != 0 && mChildLevel != 0) {

                int loopEnd = mChildLevel - mParentLevel + 1;

                String query = "SELECT DISTINCT PM1.PID, PM1.PName FROM ProductMaster PM1";

                for (int i = 2; i <= loopEnd; i++)
                    query = query + " INNER JOIN ProductMaster PM" + i
                            + " ON PM" + i + ".ParentId = PM" + (i - 1)
                            + ".PID";

                query = query
                        + " INNER JOIN PlanogramMapping MP ON MP.PId = PM"
                        + loopEnd
                        + ".PID"
                        + str
                        + " INNER JOIN PlanogramMaster P ON P.HId = MP.HId"
                        + " AND "
                        + mBModel.QT(SDUtil.now(SDUtil.DATE_GLOBAL))
                        + " BETWEEN P.StartDate AND P.EndDate"
                        + " WHERE PM1.PLid IN (SELECT ProductFilter1 FROM ConfigActivityFilter"
                        + " WHERE ActivityCode ='" + moduleName + "')";

                Cursor c = db.selectSQL(query);

                if (c != null) {
                    setmParentLevelBo(new Vector<ParentLevelBo>());
                    setmChildLevelBo(new Vector<ChildLevelBo>());
                    while (c.moveToNext()) {
                        mParentLevelBo = new ParentLevelBo();
                        mParentLevelBo.setPl_productid(c.getInt(0));
                        mParentLevelBo.setPl_levelName(c.getString(1));
                        mParentLevelBo.setPl_productLevel(mParentLevelName);
                        getmParentLevelBo().add(mParentLevelBo);
                    }

                    c.close();
                }

                int filterGap = mChildLevel - mParentLevel + 1;

                query = "SELECT DISTINCT  PM1.PID, PM" + filterGap
                        + ".PID,  PM" + filterGap
                        + ".PName FROM ProductMaster PM1";

                for (int i = 2; i <= loopEnd; i++)
                    query = query + " INNER JOIN ProductMaster PM" + i
                            + " ON PM" + i + ".ParentId = PM" + (i - 1)
                            + ".PID";

                query = query
                        + " INNER JOIN PlanogramMapping MP ON MP.PId = PM"
                        + loopEnd
                        + ".PID"
                        + str
                        + " INNER JOIN PlanogramMaster P ON P.HId = MP.HId"
                        + " AND "
                        + mBModel.QT(SDUtil.now(SDUtil.DATE_GLOBAL))
                        + " BETWEEN P.StartDate AND P.EndDate"
                        + " WHERE PM1.PLid IN (SELECT ProductFilter1 FROM ConfigActivityFilter"
                        + " WHERE ActivityCode='" + moduleName + "')";

                c = db.selectSQL(query);

                if (c != null) {
                    setmChildLevelBo(new Vector<ChildLevelBo>());
                    while (c.moveToNext()) {
                        mChildLevelBo = new ChildLevelBo();
                        mChildLevelBo.setParentid(c.getInt(0));
                        mChildLevelBo.setProductid(c.getInt(1));
                        mChildLevelBo.setPlevelName(c.getString(2));
                        mChildLevelBo.setProductLevel(mChildLevelName);
                        getmChildLevelBo().add(mChildLevelBo);
                    }
                    c.close();
                }

            } else if (mParentLevel != 0) {// One Level Filter

                String query = "SELECT DISTINCT PM1.PID, PM1.PName FROM ProductMaster PM1";

                query = query
                        + " INNER JOIN PlanogramMapping MP ON MP.PId = PM1.PID"
                        + str
                        + " INNER JOIN PlanogramMaster P ON P.HId = MP.HId"
                        + " AND "
                        + mBModel.QT(SDUtil.now(SDUtil.DATE_GLOBAL))
                        + " BETWEEN P.StartDate AND P.EndDate"
                        + " WHERE PM1.PLid IN (SELECT ProductFilter1 FROM ConfigActivityFilter"
                        + " WHERE ActivityCode= '" + moduleName + "')";

                Cursor c = db.selectSQL(query);

                if (c != null) {
                    setmChildLevelBo(new Vector<ChildLevelBo>());
                    while (c.moveToNext()) {
                        mChildLevelBo = new ChildLevelBo();
                        mChildLevelBo.setProductid(c.getInt(0));
                        mChildLevelBo.setPlevelName(c.getString(1));
                        mChildLevelBo.setProductLevel(mChildLevelName);
                        getmChildLevelBo().add(mChildLevelBo);
                    }
                    c.close();
                }
            }


            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
        }
    }


    /**
     * Download master based on mapping(Account wise OR Retailer wise ..)
     *
     * @param mMenuName Menu Name
     */
    public void downloadMaster(Context mContext, String mMenuName) {
        try {
            mBModel.productHelper.getRetailerlevel(mMenuName);
            downloadPlanoGram(mContext, mMenuName);
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    /**
     * Download PlanoGram
     *
     * @param moduleName Module Name
     */
    public void downloadPlanoGram(Context mContext, String moduleName) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
        try {
            PlanoGramBO planogram;
            db.openDataBase();
            String query;
            String query1 ;
            String retailerID;
            if (mBModel.getCounterRetailerId() != null && !mBModel.getCounterRetailerId().equals("0"))
                retailerID = mBModel.getCounterRetailerId();
            else
                retailerID = mBModel.getRetailerMasterBO().getRetailerID();

            query1 = " MP.AccId in (0 ," + mBModel.getRetailerMasterBO().getAccountid() + ") and"
                    + " MP.RetailerId in (0 ," + retailerID + ") and"
                    + " MP.ClassId in (0 ," + mBModel.getRetailerMasterBO().getClassid() + ") and"
                    + " MP.LocId in (0 ," + mBModel.productHelper.getMappingLocationId(mBModel.productHelper.locid, mBModel.getRetailerMasterBO().getLocationId()) + ") and"
                    + " MP.ChId in (0 ," + mBModel.productHelper.getMappingChannelId(mBModel.productHelper.chid, mBModel.getRetailerMasterBO().getSubchannelid()) + ")";


            if ("MENU_PLANOGRAM".equals(moduleName) || "MENU_PLANOGRAM_CS".equals(moduleName)) {
                query = "SELECT ifnull(PM.Pid,0) ,MP.MappingId as PlanogramID, P.PLDesc, PI.ImgName,STM.listid ,MP.StoreLocId, PM.PName,PI.ImgId"
                        + " FROM PlanogramMapping MP"
                        + " INNER JOIN PlanogramMaster P ON P.HId = MP.HId"
                        + " INNER JOIN PlanogramImageInfo PI on PI.ImgId=MP.ImageId"
                        + " LEFT JOIN StandardListMaster STM  on STM.Listid = MP.StoreLocId"
                        + " LEFT JOIN ProductMaster PM ON PM.PID=MP.PID"
                        + " WHERE" + query1 + " AND "
                        + mBModel.QT(SDUtil.now(SDUtil.DATE_GLOBAL))
                        + " BETWEEN P.startdate AND P.enddate";
            } else {
                query = "SELECT ifnull(PM.Pid,0) ,MP.MappingId as PlanogramID, P.PLDesc, PI.ImgName,0,0, PM.PName,PI.ImgId"
                        + " FROM PlanogramMapping MP ON MP.PId = PM.Pid"
                        + " INNER JOIN PlanogramMaster P ON P.HId = MP.HId"
                        + " INNER JOIN PlanogramImageInfo PI on PI.ImgId=MP.ImageId"
                        + " LEFT JOIN ProductMaster PM ON PM.PID=MP.PID"
                        + " WHERE"
                        + " MP.RID ='0'"
                        + " AND "
                        + mBModel.QT(SDUtil.now(SDUtil.DATE_GLOBAL))
                        + " BETWEEN P.startdate AND P.enddate";
            }

            query = query + " GROUP BY MP.RetailerId,MP.AccId,MP.ChId,MP.LocId,MP.ClassId,PM.Pid ORDER BY MP.RetailerId,MP.AccId,MP.ChId,MP.LocId,MP.ClassId";

            Cursor c = db.selectSQL(query);

            if (c != null) {
                setPlanogramMaster(new Vector<PlanoGramBO>());
                while (c.moveToNext()) {
                    planogram = new PlanoGramBO();
                    planogram.setPid(c.getInt(0));
                    planogram.setMappingID(c.getInt(1));
                    planogram.setImageName(c.getString(3));
                    planogram.setLocationID(c.getInt(4));
                    planogram.setProductName(c.getString(6));
                    planogram.setImageId(c.getInt(7));
                    getPlanogramMaster().add(planogram);
                }
                c.close();
            }
            db.closeDB();

            if (("MENU_PLANOGRAM".equals(moduleName) || "MENU_PLANOGRAM_CS".equals(moduleName)))
                downloadPlanoGramProductLocations(mContext, moduleName, null, query1);
        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
        }
    }


    /**
     * Load PlanoGram in edit mode
     *
     * @param retailerId Retailer Id
     */
    public void loadPlanoGramInEditMode(Context mContext, String retailerId) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
        try {
            db.openDataBase();
            String tid = "";
            String sql = "SELECT Tid FROM PlanogramHeader WHERE RetailerId = "
                    + retailerId + " AND Date = "
                    + mBModel.QT(SDUtil.now(SDUtil.DATE_GLOBAL));

            Cursor orderHeaderCursor = db.selectSQL(sql);
            if (orderHeaderCursor != null) {
                if (orderHeaderCursor.moveToNext())
                    tid = orderHeaderCursor.getString(0);
            } else
                return;

            orderHeaderCursor.close();

            String sql1 = "SELECT PId, PLID, ImageName, Adherence, ReasonID, LocID, IFNULL(Audit,'2')"
                    + " FROM PlanogramDetails WHERE tid=" + QT(tid);

            Cursor orderDetailCursor = db.selectSQL(sql1);

            if (orderDetailCursor != null) {
                while (orderDetailCursor.moveToNext()) {
                    int pid = orderDetailCursor.getInt(0);
                    String imageName = orderDetailCursor.getString(2);
                    String adherence = orderDetailCursor.getString(3);
                    String reasonID = orderDetailCursor.getString(4);
                    int locationID = orderDetailCursor.getInt(5);
                    int aduit = orderDetailCursor.getInt(6);
                    setPlanoGramDetails(pid, imageName, adherence, reasonID,
                            locationID, aduit,tid,mContext);
                }
                orderDetailCursor.close();
            }

            db.closeDB();
        } catch (Exception e) {
            db.closeDB();
            Commons.printException("" + e);
        }
    }

    /**
     * Set planoGram details in object
     *
     * @param planogramPId pid
     * @param imageName    imageName
     * @param adherence    adherence
     * @param reasonID     reasonID
     * @param locationID   location id
     * @param isAudit      audit
     */
    private void setPlanoGramDetails(int planogramPId, String imageName,
                                     String adherence, String reasonID, int locationID, int isAudit,String tId,Context context) {
        PlanoGramBO planogram;
        int siz = getPlanogramMaster().size();
        if (siz == 0)
            return;

        for (int i = 0; i < siz; ++i) {
            planogram = getPlanogramMaster().get(i);
            if (planogram.getPid() == planogramPId &&
                    (!IS_LOCATION_WISE_PLANOGRAM || planogram.getLocationID() == locationID)) {
                planogram.setPlanogramCameraImgName(imageName);
                planogram.setAdherence(adherence);
                planogram.setReasonID(reasonID);
                planogram.setAudit(isAudit);
                planogram.setPlanoGramCameraImgList(getPlanogramImage(planogramPId,tId,context));
                getPlanogramMaster().setElementAt(planogram, i);
                return;
            }
        }
    }

    private ArrayList<String> getPlanogramImage(int planogramId,String tId,Context context){

        ArrayList<String> planogramImagList = new ArrayList<>();

        DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
        try {
            db.openDataBase();

            String query = "Select imageName from PlanogramImageDetails where Tid ="+QT(tId)+" and PId ="+planogramId;
            Cursor planoImgCursor = db.selectSQL(query);

            if (planoImgCursor != null) {
                while (planoImgCursor.moveToNext()) {
                    planogramImagList.add(planoImgCursor.getString(0));
                }
            }

            db.closeDB();
        } catch (Exception e) {
            db.closeDB();
            Commons.printException("" + e);
        }

        return planogramImagList;

    }

    /**
     * Set Image path based on selection
     *
     * @param pID         Product Id
     * @param mImagePath  Image Path
     * @param mLocationId Location Id
     */
    public void setImagePath(int pID, String mImagePath, int mLocationId) {
        PlanoGramBO planogrambo;
        int siz = getPlanogramMaster().size();
        if (siz == 0)
            return;
        for (int i = 0; i < siz; ++i) {
            planogrambo = getPlanogramMaster().get(i);
            if ((planogrambo.getPid() == pID || IS_LOCATION_WISE_PLANOGRAM)
                    && planogrambo.getLocationID() == mLocationId) {
                planogrambo.setPlanogramCameraImgName(mImagePath);
                getPlanogramMaster().setElementAt(planogrambo, i);
                return;
            }
        }
    }

    /**
     * Save PlanoGram in transaction table
     *
     * @return Is Saved
     */
    boolean savePlanoGram(Context mContext) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
        try {
            db.openDataBase();
            String tid;
            Cursor headerCursor;

            String headerColumns = "TiD, RetailerId, Date, timezone, uid, RefId,Type,CounterId,DistributorID";
            String detailColumns = "TiD, MappingId, Pid, ImageName,ImagePath, Adherence, RetailerId, ReasonID, LocID,Audit,CounterId";

            String values;
            boolean isData;
            String refId = "0";
            String imagePath = "Planogram" + "/" + mBModel.userMasterHelper.getUserMasterBO().getDownloadDate().replace("/", "")
                    + "/"
                    + mBModel.userMasterHelper.getUserMasterBO().getUserid()
                    + "/";

            tid = mBModel.userMasterHelper.getUserMasterBO().getUserid() + ""
                    + mBModel.getRetailerMasterBO().getRetailerID() + ""
                    + SDUtil.now(SDUtil.DATE_TIME_ID);

            // delete transaction if exist
            headerCursor = db
                    .selectSQL("SELECT Tid, RefId FROM PlanogramHeader"
                            + " WHERE RetailerId = "
                            + mBModel.getRetailerMasterBO().getRetailerID()
                            + " AND DistributorID = "
                            + mBModel.getRetailerMasterBO().getDistributorId()
                            + " AND CounterId = 0"
                            + " AND Date = "
                            + mBModel.QT(SDUtil.now(SDUtil.DATE_GLOBAL)));

            if (headerCursor.getCount() > 0) {
                headerCursor.moveToNext();
                db.deleteSQL("PlanogramHeader",
                        "Tid=" + mBModel.QT(headerCursor.getString(0)), false);
                db.deleteSQL("PlanogramDetails",
                        "Tid=" + mBModel.QT(headerCursor.getString(0)), false);
                db.deleteSQL("PlanogramImageDetails",
                        "Tid=" + mBModel.QT(headerCursor.getString(0)), false);
                refId = headerCursor.getString(1);
                headerCursor.close();
            }

            // Insert Details
            isData = false;
            for (PlanoGramBO planogram : getPlanogramMaster()) {

                if (planogram.getAdherence() != null) {
                    if (planogram.getAdherence().equals("1")) {
                        planogram.setReasonID("0");
                    }
                    values = QT(tid) + "," + planogram.getMappingID() + ","
                            + planogram.getPid() + ","
                            + QT(planogram.getPlanogramCameraImgName()) + ","
                            + QT(imagePath) + ","
                            + QT(planogram.getAdherence()) + ","
                            + QT(mBModel.getRetailerMasterBO().getRetailerID())
                            + "," + planogram.getReasonID() + ","
                            + planogram.getLocationID() + ","
                            + planogram.getAudit() + ",0";

                    db.insertSQL("PlanogramDetails", detailColumns, values);

                    savePlanogramImage(db,tid,planogram.getPid(),
                            planogram.getPlanoGramCameraImgList(),planogram.getMappingID(),imagePath,planogram.getImageId());

                    isData = true;
                }

            }

            // Save Header if There is Data in Details
            if (isData) {
                values = QT(tid) + ","
                        + mBModel.getRetailerMasterBO().getRetailerID() + ","
                        + QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + ","
                        + QT(mBModel.getTimeZone()) + ","
                        + mBModel.userMasterHelper.getUserMasterBO().getUserid()
                        + "," + QT(refId) + ","
                        + QT("") + ",0" + "," + mBModel.getRetailerMasterBO().getDistributorId();


                db.insertSQL("PlanogramHeader", headerColumns, values);
            }
            db.closeDB();
            return true;
        } catch (Exception e) {
            db.closeDB();
            Commons.printException("" + e);
            return false;
        }
    }

    private void savePlanogramImage(DBUtil db,String tid,int planogramId,
                                    ArrayList<String> planogramImageList,int mappingId,String path,int imageId){

        String columns = "Tid,PId,imageName,mappingid,imagePath,imageId";

        for(int i = 0;i<planogramImageList.size();i++){

            String values = QT(tid) + "," + planogramId + ","
                    + QT(planogramImageList.get(i))+ ","+mappingId+ ","+QT(path)+","+imageId;

            db.insertSQL("PlanogramImageDetails", columns, values);

        }
    }


    private String QT(String data) {
        return "'" + data + "'";
    }

    Vector<ParentLevelBo> getmParentLevelBo() {
        return mParentLevelBo;
    }

    private void setmParentLevelBo(Vector<ParentLevelBo> mParentLevelBo) {
        this.mParentLevelBo = mParentLevelBo;
    }

    Vector<ChildLevelBo> getmChildLevelBo() {
        return mChildLevelBo;
    }

    private void setmChildLevelBo(Vector<ChildLevelBo> mChildLevelBo) {
        this.mChildLevelBo = mChildLevelBo;
    }

    public Vector<PlanoGramBO> getPlanogramMaster() {
        return mPlanoGramMaster;
    }

    private void setPlanogramMaster(Vector<PlanoGramBO> planogramMaster) {
        this.mPlanoGramMaster = planogramMaster;
    }


    /**
     * Delete image from transaction table
     *
     * @param imgName Image name
     */
    void deleteImageName(Context mContext, String imgName) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.createDataBase();
        db.openDataBase();

        db.updateSQL("delete from " + DataMembers.tbl_planogram_image_detail+ " where ImageName LIKE"
                + QT(imgName + "%"));
        db.closeDB();
    }


    public Vector<StandardListBO> getInStoreLocation() {
        return mLocationList;
    }

    /**
     * Download Module Locations
     */
    public void downloadPlanoGramProductLocations(Context mContext, String moduleName, String retailer, String query1) {
        try {

            mLocationList = new Vector<>();
            StandardListBO locations;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            String sql1 = "SELECT Distinct SL.ListId, SL.ListName"
                    + " FROM StandardListMaster SL";

            switch (moduleName) {
                case "MENU_PLANOGRAM":
                    sql1 += " inner join PlanogramMapping MP on MP.StoreLocId=SL.ListId"
                            + " inner join PlanogramMaster P on P.HId=MP.HId"
                            + " where " + query1 + " AND "
                            + " SL.Listtype='PL'"
                            + " ORDER BY SL.ListId";
                    break;
                case "MENU_VAN_PLANOGRAM":
                    sql1 += " inner join PlanogramMapping PM on PM.StoreLocId=sl.listcode"
                            + " inner join PlanogramMaster P on P.HId=PM.HId"
                            + " where SL.Listtype='SL' and PM.RetailerId= " + mBModel.QT(retailer)
                            + " ORDER BY SL.ListId";
                    break;
                default:
                    sql1 += " where SL.Listtype='PL' ORDER BY SL.ListId";
                    break;
            }

            Cursor c = db.selectSQL(sql1);
            if (c != null) {
                while (c.moveToNext()) {
                    locations = new StandardListBO();
                    locations.setListID(c.getString(0));
                    locations.setListName(c.getString(1));
                    mLocationList.add(locations);
                }
                c.close();
            }
            db.closeDB();

            if (mLocationList.size() == 0) {
                locations = new StandardListBO();
                locations.setListID("0");
                locations.setListName("Store");
                mLocationList.add(locations);
            }

        } catch (Exception e) {
            Commons.printException("Download Location", e);
        }

    }

}

