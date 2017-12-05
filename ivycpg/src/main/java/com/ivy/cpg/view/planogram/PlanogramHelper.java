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

import java.util.Vector;

public class PlanogramHelper {

    private final Context context;
    private final BusinessModel bmodel;
    private static PlanogramHelper instance = null;
    private Vector<PlanogramBO> planogramMaster;
    private Vector<CounterPlanogramBO> csPlanogramMaster;
    private Vector<ParentLevelBo> mParentLevelBo;
    private Vector<ChildLevelBo> mChildLevelBo;
    private Vector<StandardListBO> mLocationList;
    public String mSelectedActivityName;

    private static final String CODE_LOCATION_WISE_PLANOGRAM = "FUN39";
    public boolean IS_LOCATION_WISE_PLANOGRAM;


    private PlanogramHelper(Context context) {
        this.context = context;
        bmodel = (BusinessModel) context.getApplicationContext();
    }

    public static PlanogramHelper getInstance(Context context) {
        if (instance == null) {
            instance = new PlanogramHelper(context);
        }
        return instance;
    }

    public void loadConfigurations() {
        try {

            IS_LOCATION_WISE_PLANOGRAM = false;

            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            String sql = "SELECT hhtCode, RField FROM "
                    + DataMembers.tbl_HhtModuleMaster
                    + " WHERE flag='1'";

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

    public void downloadlevels(String moduleName, String retailerId) {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
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
            String str = "";
            int level = bmodel.productHelper.getRetailerlevel("MENU_PLANOGRAM");
            if (level == 1)
                str = " and MP.AccId=" + bmodel.getRetailerMasterBO().getAccountid();
            else if (level == 2)
                str = " and MP.RetailerId=" + retailerId;
            else if (level == 3)
                str = " and MP.ClassId=" + bmodel.getRetailerMasterBO().getClassid();

            if (level == 6)
                str = " and MP.LocId=" + bmodel.productHelper.getMappingLocationId(bmodel.productHelper.locid, bmodel.getRetailerMasterBO().getLocationId()) + " and MP.ChId=" + bmodel.productHelper.getMappingChannelId(bmodel.productHelper.chid, bmodel.getRetailerMasterBO().getSubchannelid());
            else if (level == 4)
                str = " and MP.LocId=" + bmodel.productHelper.getMappingLocationId(bmodel.productHelper.locid, bmodel.getRetailerMasterBO().getLocationId());
            else if (level == 5)
                str = " and MP.ChId=" + bmodel.productHelper.getMappingChannelId(bmodel.productHelper.chid, bmodel.getRetailerMasterBO().getSubchannelid());

            if (level == -1) {
                Toast.makeText(context, context.getResources().getString(R.string.data_not_mapped_correctly), Toast.LENGTH_SHORT).show();
                return;
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
                        + bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL))
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
                        + bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL))
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
                        + bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL))
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




    public void downloadPlanogram(String moduleName, boolean isaccount, boolean isretailer, boolean isclass, int locid, int chid) {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
        try {
            PlanogramBO planogram;
            db.openDataBase();
            String query;
            String query1 = "";
            String retailerID = "";
            if (bmodel.getCounterRetailerId() != null && !bmodel.getCounterRetailerId().isEmpty())
                retailerID = bmodel.getCounterRetailerId();
            else
                retailerID = bmodel.getRetailerMasterBO().getRetailerID();

            if (isaccount) {
                if (query1.isEmpty())
                    query1 = " MP.AccId=" + bmodel.getRetailerMasterBO().getAccountid() + " and";
                else
                    query1 = query1 + " MP.AccId=" + bmodel.getRetailerMasterBO().getAccountid() + " and";
            }
            if (isretailer) {
                if (query1.isEmpty())
                    query1 = " MP.RetailerId=" + retailerID + " and";
                else
                    query1 = query1 + " MP.RetailerId=" + retailerID + " and";
            }
            if (isclass) {
                if (query1.isEmpty())
                    query1 = " MP.ClassId=" + bmodel.getRetailerMasterBO().getClassid() + " and";
                else
                    query1 = query1 + " MP.ClassId=" + bmodel.getRetailerMasterBO().getClassid() + " and";
            }
            if (locid > 0) {
                if (query1.isEmpty())
                    query1 = " MP.LocId=" + bmodel.productHelper.getMappingLocationId(locid, bmodel.getRetailerMasterBO().getLocationId()) + " and";
                else
                    query1 = query1 + " MP.LocId=" + bmodel.productHelper.getMappingLocationId(locid, bmodel.getRetailerMasterBO().getLocationId()) + " and";
            }
            if (chid > 0) {
                if (query1.isEmpty())
                    query1 = " MP.ChId=" + bmodel.productHelper.getMappingChannelId(chid, bmodel.getRetailerMasterBO().getSubchannelid()) + " and";
                else
                    query1 = query1 + " MP.ChId=" + bmodel.productHelper.getMappingChannelId(chid, bmodel.getRetailerMasterBO().getSubchannelid()) + " and";
            }
            if ("MENU_PLANOGRAM".equals(moduleName) || "MENU_PLANOGRAM_CS".equals(moduleName)) {
                query = "SELECT ifnull(PM.Pid,0) ,MP.MappingId as PlanogramID, P.PLDesc, PI.ImgName,STM.listid ,MP.StoreLocId, PM.PName"
                        + " FROM PlanogramMapping MP"
                        + " INNER JOIN PlanogramMaster P ON P.HId = MP.HId"
                        + " INNER JOIN PlanogramImageInfo PI on PI.ImgId=MP.ImageId"
                        + " LEFT JOIN StandardListMaster STM  on STM.Listid = MP.StoreLocId"
                        + " LEFT JOIN ProductMaster PM ON PM.PID=MP.PID"
                        + " WHERE" + query1
                        + bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL))
                        + " BETWEEN P.startdate AND P.enddate";
            } else {
                query = "SELECT ifnull(PM.Pid,0) ,MP.MappingId as PlanogramID, P.PLDesc, PI.ImgName,0,0, PM.PName"
                        + " FROM PlanogramMapping MP ON MP.PId = PM.Pid"
                        + " INNER JOIN PlanogramMaster P ON P.HId = MP.HId"
                        + " INNER JOIN PlanogramImageInfo PI on PI.ImgId=MP.ImageId"
                        + " LEFT JOIN ProductMaster PM ON PM.PID=MP.PID"
                        + " WHERE"
                        + " MP.RID ='0'"
                        + " AND "
                        + bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL))
                        + " BETWEEN P.startdate AND P.enddate";
            }

            Cursor c = db.selectSQL(query);

            if (c != null) {
                setPlanogramMaster(new Vector<PlanogramBO>());
                while (c.moveToNext()) {
                    planogram = new PlanogramBO();
                    planogram.setPid(c.getInt(0));
                    planogram.setMappingID(c.getInt(1));
                    planogram.setImageName(c.getString(3));
                    planogram.setLocationID(c.getInt(4));
                    planogram.setProductName(c.getString(6));
                    getPlanogramMaster().add(planogram);
                }
                c.close();
            }
            db.closeDB();

            if (("MENU_PLANOGRAM".equals(moduleName) || "MENU_PLANOGRAM_CS".equals(moduleName)))
                downloadPlanogramProdutLocations(moduleName, null, query1);
        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
        }
    }

    public void downloadCounterPlanogram(int counterId) {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
        try {
            CounterPlanogramBO planogram;
            db.openDataBase();
            String query;

            query = "SELECT MP.MappingId as PlanogramID, P.PLDesc, PI.ImgName,PI.ImgId FROM PlanogramMaster P"
                    + " INNER JOIN PlanogramMapping MP ON P.HId = MP.HId "
                    + " INNER JOIN PlanogramImageInfo PI on PI.ImgId=MP.ImageId"
                    + " INNER JOIN StandardListMaster st on st.listid=P.typeid"
                    + " WHERE"
                    + " MP.RetailerId ='0'"
                    + " AND "
                    + " st.listtype ='PLANOGRAM_TYPE'"
                    + " AND "
                    + " st.listcode ='COUNTER'"
                    + " AND "
                    + bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL))
                    + " BETWEEN P.startdate AND P.enddate";


            Cursor c = db.selectSQL(query);

            if (c != null) {
                setCsPlanogramMaster(new Vector<CounterPlanogramBO>());
                while (c.moveToNext()) {
                    planogram = new CounterPlanogramBO();
                    planogram.setRetailerId(bmodel.retailerMasterBO
                            .getRetailerID());
                    planogram.setMappingID(c.getInt(0));
                    planogram.setPlanogramDesc(c.getString(1));
                    planogram.setImageName(c.getString(2));
                    planogram.setImageId(c.getInt(3));
                    planogram.setCounterId(counterId);

                    getCsPlanogramMaster().add(planogram);
                }
                c.close();
            }
            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
        }
    }

    public void loadPlanoGramInEditMode(String retailerId, int counterId) {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
        try {
            db.openDataBase();
            String tid;
            String sql = "SELECT Tid FROM PlanogramHeader WHERE RetailerId = "
                    + retailerId + " AND CounterId = " + counterId + " AND Date = "
                    + bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL));

            Cursor orderHeaderCursor = db.selectSQL(sql);
            tid = "";
            if (orderHeaderCursor != null) {
                if (orderHeaderCursor.moveToNext())
                    tid = orderHeaderCursor.getString(0);
            } else
                return;

            orderHeaderCursor.close();

            String sql1 = "SELECT ImageId, PLID, ImageName, Adherence, ReasonID, IFNULL(Audit,'2')"
                    + " FROM PlanogramDetails WHERE tid=" + QT(tid);

            Cursor orderDetailCursor = db.selectSQL(sql1);

            if (orderDetailCursor != null) {
                while (orderDetailCursor.moveToNext()) {
                    int imageId = orderDetailCursor.getInt(0);
                    String imageName = orderDetailCursor.getString(2);
                    String adherence = orderDetailCursor.getString(3);
                    String reasonID = orderDetailCursor.getString(4);
                    int aduit = orderDetailCursor.getInt(5);
                    setCounterPlanoGramDetails(imageId, imageName, adherence, reasonID,
                            aduit, counterId);
                }
                orderDetailCursor.close();
            }

            db.closeDB();
        } catch (Exception e) {
            db.closeDB();
            Commons.printException("" + e);
        }
    }

    public void loadPlanoGramInEditMode(String retailerId) {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
        try {
            db.openDataBase();
            String tid = "";
            String sql = "SELECT Tid FROM PlanogramHeader WHERE RetailerId = "
                    + retailerId + " AND Date = "
                    + bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL));

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
                            locationID, aduit);
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
     * @param planogramPId planogram_pid
     * @param imageName    imageName
     * @param adherence    adherence
     * @param reasonID     reasonID
     * @param locationID   location id
     * @param --ClientID   clientId
     * @param aduit        aduit
     */
    private void setPlanoGramDetails(int planogramPId, String imageName,
                                     String adherence, String reasonID, int locationID, int aduit) {
        PlanogramBO planogram;
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
                planogram.setAudit(aduit);
                getPlanogramMaster().setElementAt(planogram, i);
                return;
            }
        }
    }

    private void setCounterPlanoGramDetails(int imageId, String imageName,
                                            String adherence, String reasonID, int aduit, int counterId) {
        CounterPlanogramBO planogram;
        int siz = getCsPlanogramMaster().size();
        if (siz == 0)
            return;

        for (int i = 0; i < siz; ++i) {
            planogram = getCsPlanogramMaster().get(i);
            if (planogram.getImageId() == imageId) {
                planogram.setImageId(imageId);
                planogram.setPlanogramCameraImgName(imageName);
                planogram.setAdherence(adherence);
                planogram.setReasonID(reasonID);
                planogram.setAudit(aduit);
                planogram.setCounterId(counterId);
                getCsPlanogramMaster().setElementAt(planogram, i);
                return;
            }
        }

    }



    public void setImagePath(int pID, String imagepath, int locationid) {
        PlanogramBO planogrambo;
        int siz = getPlanogramMaster().size();
        if (siz == 0)
            return;
        for (int i = 0; i < siz; ++i) {
            planogrambo = getPlanogramMaster().get(i);
            if ((planogrambo.getPid() == pID || IS_LOCATION_WISE_PLANOGRAM)
                    && planogrambo.getLocationID() == locationid) {
                planogrambo.setPlanogramCameraImgName(imagepath);
                getPlanogramMaster().setElementAt(planogrambo, i);
                return;
            }
        }
    }

    public void setCSImagePath(String imagepath, int imageId) {
        CounterPlanogramBO planogrambo;
        int siz = getCsPlanogramMaster().size();
        if (siz == 0)
            return;
        for (int i = 0; i < siz; ++i) {
            planogrambo = getCsPlanogramMaster().get(i);
            if (planogrambo.getImageId() == imageId) {
                planogrambo.setPlanogramCameraImgName(imagepath);
                getCsPlanogramMaster().setElementAt(planogrambo, i);
                return;
            }
        }
    }

    public void setImageAdherence(int pID, String adherence, int locID) {
        PlanogramBO planogrambo;
        int siz = getPlanogramMaster().size();
        if (siz == 0)
            return;
        for (int i = 0; i < siz; ++i) {
            planogrambo = getPlanogramMaster().get(i);
            Commons.print("pid" + pID + " locid=" + locID);
            if ((planogrambo.getPid() == pID || IS_LOCATION_WISE_PLANOGRAM)
                    && (planogrambo.getLocationID() == locID)) {
                planogrambo.setAdherence(adherence);
                getPlanogramMaster().setElementAt(planogrambo, i);
                return;
            }
        }
    }

    public void setCSImageAdherence(String adherence, int imageId) {
        CounterPlanogramBO planogrambo;
        int siz = getCsPlanogramMaster().size();
        if (siz == 0)
            return;
        for (int i = 0; i < siz; ++i) {
            planogrambo = getCsPlanogramMaster().get(i);
            if (imageId == planogrambo.getImageId()) {
                planogrambo.setAdherence(adherence);
                getCsPlanogramMaster().setElementAt(planogrambo, i);
                return;
            }
        }
    }

    public boolean savePhotocapture() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
        try {
            db.openDataBase();
            String tid;
            Cursor headerCursor;

            String headerColumns = "TiD, RetailerId, Date, timezone, uid, RefId,Type,CounterId,DistributorID";
            String detailColumns = "TiD, MappingId, Pid, ImageName,ImagePath, Adherence, RetailerId, ReasonID, LocID,Audit,CounterId";

            String values;
            boolean isData;
            String refId = "0";
            String imagePath = "Planogram" + "/" + bmodel.userMasterHelper.getUserMasterBO().getDownloadDate().replace("/", "")
                    + "/"
                    + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                    + "/";

            tid = bmodel.userMasterHelper.getUserMasterBO().getUserid() + ""
                    + bmodel.getRetailerMasterBO().getRetailerID() + ""
                    + SDUtil.now(SDUtil.DATE_TIME_ID);

            // delete transaction if exist
            headerCursor = db
                    .selectSQL("SELECT Tid, RefId FROM PlanogramHeader"
                            + " WHERE RetailerId = "
                            + bmodel.getRetailerMasterBO().getRetailerID()
                            + " AND DistributorID = "
                            + bmodel.getRetailerMasterBO().getDistributorId()
                            + " AND CounterId = 0"
                            + " AND Date = "
                            + bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL)));

            if (headerCursor.getCount() > 0) {
                headerCursor.moveToNext();
                db.deleteSQL("PlanogramHeader",
                        "Tid=" + bmodel.QT(headerCursor.getString(0)), false);
                db.deleteSQL("PlanogramDetails",
                        "Tid=" + bmodel.QT(headerCursor.getString(0)), false);
                refId = headerCursor.getString(1);
                headerCursor.close();
            }

            // Insert Details
            isData = false;
            for (PlanogramBO planogram : getPlanogramMaster()) {

                if (planogram.getAdherence() != null) {
                    if (planogram.getAdherence().equals("1")) {
                        planogram.setReasonID("0");
                    }
                    values = QT(tid) + "," + planogram.getMappingID() + ","
                            + planogram.getPid() + ","
                            + QT(planogram.getPlanogramCameraImgName()) + ","
                            + QT(imagePath) + ","
                            + QT(planogram.getAdherence()) + ","
                            + QT(bmodel.getRetailerMasterBO().getRetailerID())
                            + "," + planogram.getReasonID() + ","
                            + planogram.getLocationID() + ","
                            + planogram.getAudit() + ",0";

                    db.insertSQL("PlanogramDetails", detailColumns, values);
                    isData = true;
                }

            }

            // Save Header if There is Data in Details
            if (isData) {
                values = QT(tid) + ","
                        + bmodel.getRetailerMasterBO().getRetailerID() + ","
                        + QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + ","
                        + QT(bmodel.getTimeZone()) + ","
                        + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                        + "," + QT(refId) + ","
                        + QT("") + ",0" + "," + bmodel.getRetailerMasterBO().getDistributorId();


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

    public boolean savePhotocapture(int counterId) {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
        try {
            db.openDataBase();
            String tid;
            Cursor headerCursor;

            String headerColumns = "TiD, RetailerId, Date, timezone, uid, RefId,Type, CounterId,DistributorID";
            String detailColumns = "TiD, MappingId, Pid, ImageName,ImagePath, Adherence, RetailerId, ReasonID, LocID,Audit, CounterId, ImageId";

            String values;
            boolean isData;
            String refId = "0";
            String imagePath = "Planogram" + "/" + bmodel.userMasterHelper.getUserMasterBO().getDownloadDate().replace("/", "")
                    + "/"
                    + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                    + "/";

            tid = bmodel.userMasterHelper.getUserMasterBO().getUserid() + ""
                    + bmodel.getRetailerMasterBO().getRetailerID() + ""
                    + SDUtil.now(SDUtil.DATE_TIME_ID);

            // delete transaction if exist
            String query = "SELECT Tid, RefId FROM PlanogramHeader"
                    + " WHERE RetailerId = "
                    + bmodel.getRetailerMasterBO().getRetailerID()
                    + " AND DistributorID = "
                    + bmodel.getRetailerMasterBO().getDistributorId()
                    + " AND CounterId = "
                    + bmodel.QT(String.valueOf(counterId))
                    + " AND Date = "
                    + bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL));
            query += " and (upload='N' OR refid!=0)";

            headerCursor = db
                    .selectSQL(query);
            if (headerCursor.getCount() > 0) {
                headerCursor.moveToNext();
                db.deleteSQL("PlanogramHeader",
                        "Tid=" + bmodel.QT(headerCursor.getString(0)), false);
                db.deleteSQL("PlanogramDetails",
                        "Tid=" + bmodel.QT(headerCursor.getString(0)), false);
                refId = headerCursor.getString(1);
                headerCursor.close();
            }

            // Insert Details
            isData = false;
            for (CounterPlanogramBO planogram : getCsPlanogramMaster()) {

                if (planogram.getAdherence() != null) {
                    values = QT(tid) + "," + planogram.getMappingID() + ","
                            + 0 + ","
                            + QT(planogram.getPlanogramCameraImgName()) + ","
                            + QT(imagePath) + ","
                            + QT(planogram.getAdherence()) + ","
                            + QT(bmodel.getRetailerMasterBO().getRetailerID())
                            + "," + planogram.getReasonID() + ","
                            + 0 + ","
                            + planogram.getAudit() + ","
                            + planogram.getCounterId() + ","
                            + planogram.getImageId();
                    db.insertSQL("PlanogramDetails", detailColumns, values);
                    isData = true;
                }

            }

            // Save Header if There is Data in Details
            if (isData) {
                values = QT(tid) + ","
                        + bmodel.getRetailerMasterBO().getRetailerID() + ","
                        + QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + ","
                        + QT(bmodel.getTimeZone()) + ","
                        + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                        + "," + QT(refId) + ","
                        + QT(getCsPlanogramMaster().get(0).getType()) + ","
                        + counterId + ","
                        + bmodel.getRetailerMasterBO().getDistributorId();

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

    public void setCSImageAdherenceReason(String adherence, int imageId) {
        CounterPlanogramBO planogrambo;
        int siz = getCsPlanogramMaster().size();
        if (siz == 0)
            return;
        for (int i = 0; i < siz; ++i) {
            planogrambo = getCsPlanogramMaster().get(i);
            if (imageId == planogrambo.getImageId()) {
                planogrambo.setReasonID(adherence);
                getCsPlanogramMaster().setElementAt(planogrambo, i);
                return;
            }
        }
    }


    private String QT(String data) // Quote
    {
        return "'" + data + "'";
    }

    public Vector<ParentLevelBo> getmParentLevelBo() {
        return mParentLevelBo;
    }

    private void setmParentLevelBo(Vector<ParentLevelBo> mParentLevelBo) {
        this.mParentLevelBo = mParentLevelBo;
    }

    public Vector<ChildLevelBo> getmChildLevelBo() {
        return mChildLevelBo;
    }

    private void setmChildLevelBo(Vector<ChildLevelBo> mChildLevelBo) {
        this.mChildLevelBo = mChildLevelBo;
    }

    public Vector<PlanogramBO> getPlanogramMaster() {
        return planogramMaster;
    }

    private void setPlanogramMaster(Vector<PlanogramBO> planogramMaster) {
        this.planogramMaster = planogramMaster;
    }




    public Vector<CounterPlanogramBO> getCsPlanogramMaster() {
        return csPlanogramMaster;
    }

    private void setCsPlanogramMaster(Vector<CounterPlanogramBO> csPlanogramMaster) {
        this.csPlanogramMaster = csPlanogramMaster;
    }

    public void deleteImageName(String imgName) {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.createDataBase();
        db.openDataBase();
        db.updateSQL("UPDATE " + DataMembers.tbl_PlanogramDetail
                + " SET  ImageName =" + QT("") + ",ImageName=" + QT("") + " where ImageName LIKE"
                + QT(imgName + "%"));
        db.closeDB();
    }


    public void loadData(String menuname) {
        try {
            int level;
            level = bmodel.productHelper.getRetailerlevel(menuname);
            if (menuname.equals("MENU_PLANOGRAM")) {
                switch (level) {
                    case 1:
                        downloadPlanogram("MENU_PLANOGRAM", true, false, false, 0, 0);
                        break;
                    case 2:
                        downloadPlanogram("MENU_PLANOGRAM", false, true, false, 0, 0);
                        break;
                    case 3:
                        downloadPlanogram("MENU_PLANOGRAM", false, false, true, 0, 0);
                        break;
                    case 4:
                        downloadPlanogram("MENU_PLANOGRAM", false, false, false, bmodel.productHelper.locid, 0);
                        break;
                    case 5:
                        downloadPlanogram("MENU_PLANOGRAM", false, false, false, 0, bmodel.productHelper.chid);
                        break;
                    case 6:
                        downloadPlanogram("MENU_PLANOGRAM", false, false, false, bmodel.productHelper.locid, bmodel.productHelper.chid);
                        break;
                    case -1:
                        Toast.makeText(context, context.getResources().getString(R.string.data_not_mapped_correctly), Toast.LENGTH_SHORT).show();
                        break;
                }
            } else if (menuname.equals("MENU_PLANOGRAM_CS")) {
                switch (level) {
                    case 1:
                        downloadPlanogram("MENU_PLANOGRAM_CS", true, false, false, 0, 0);
                        break;
                    case 2:
                        downloadPlanogram("MENU_PLANOGRAM_CS", false, true, false, 0, 0);
                        break;
                    case 3:
                        downloadPlanogram("MENU_PLANOGRAM_CS", false, false, true, 0, 0);
                        break;
                    case 4:
                        downloadPlanogram("MENU_PLANOGRAM_CS", false, false, false, bmodel.productHelper.locid, 0);
                        break;
                    case 5:
                        downloadPlanogram("MENU_PLANOGRAM_CS", false, false, false, 0, bmodel.productHelper.chid);
                        break;
                    case 6:
                        downloadPlanogram("MENU_PLANOGRAM_CS", false, false, false, bmodel.productHelper.locid, bmodel.productHelper.chid);
                        break;

                    default:
                        downloadPlanogram("MENU_PLANOGRAM_CS", false, false, false, 0, 0);
                        break;
                }
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public Vector<StandardListBO> getInStoreLocation() {
        return mLocationList;
    }

    /**
     * Download Module Locations
     */
    public void downloadPlanogramProdutLocations(String moduleName, String retailer, String query1) {
        try {

            mLocationList = new Vector<>();
            StandardListBO locations;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            String sql1 = "SELECT Distinct SL.ListId, SL.ListName"
                    + " FROM StandardListMaster SL";

            if (moduleName.equals("MENU_PLANOGRAM")) {
                sql1 += " inner join PlanogramMapping MP on MP.StoreLocId=SL.ListId"
                        + " inner join PlanogramMaster P on P.HId=MP.HId"
                        + " where " + query1
                        + " SL.Listtype='PL'"
                        + " ORDER BY SL.ListId";
            } else if (moduleName.equals("MENU_VAN_PLANOGRAM")) {
                sql1 += " inner join PlanogramMapping PM on PM.StoreLocId=sl.listcode"
                        + " inner join PlanogramMaster P on P.HId=PM.HId"
                        + " where SL.Listtype='SL' and PM.RetailerId= " + bmodel.QT(retailer)
                        + " ORDER BY SL.ListId";
            } else {
                sql1 += " where SL.Listtype='PL' ORDER BY SL.ListId";
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

