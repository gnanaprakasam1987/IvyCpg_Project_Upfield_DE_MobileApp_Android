package com.ivy.sd.png.provider;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

import com.ivy.cpg.view.salesreturn.SalesReturnReasonBO;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.NonproductivereasonBO;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.utils.StringUtils;

import java.util.ArrayList;

public class ReasonHelper {

    private final Context context;
    private final BusinessModel bmodel;
    private ArrayList<SalesReturnReasonBO> reasonSalesReturnMaster;
    private ArrayList<ReasonMaster> reasonNonVisitMaster;
    private ArrayList<ReasonMaster> nonProductiveReasonMaster;
    private ArrayList<ReasonMaster> deviatedReturnMaster;
    private ArrayList<ReasonMaster> clcrReason;
    private ArrayList<ReasonMaster> priminvoicestatus;
    private NonproductivereasonBO reasonsWithPhoto;
    private ArrayList<ReasonMaster> reasonList = new ArrayList<>();
    private static ReasonHelper instance = null;
    private ArrayList<ReasonMaster> assetReasonsBasedOnType;
    private ArrayList<ReasonMaster> reasonPlaneDeviationMaster;
    private ArrayList<ReasonMaster> remarksReasonMaster;
    private ArrayList<ReasonMaster> remarksType;
    private ArrayList<ReasonMaster> shipmentType;
    private ArrayList<ReasonMaster> paymentType;
    private ArrayList<ReasonMaster> distributionChannelType;
    private ArrayList<ReasonMaster> reasonPlanedActivities;
    private ArrayList<ReasonMaster> nonPlanedReasons;

    private ReasonHelper(Context context) {
        this.context = context;
        this.bmodel = (BusinessModel) context;
        reasonSalesReturnMaster = new ArrayList<>();
        setDeviatedReturnMaster(new ArrayList<ReasonMaster>());
    }

    public static ReasonHelper getInstance(Context context) {
        if (instance == null) {
            instance = new ReasonHelper(context);
        }
        return instance;
    }

    /**
     * Download sales return reason from reason master.
     * Saleable reason (SR) and Non-salable reason (SRS) will be downlaoded.
     */
    public void downloadSalesReturnReason() {
        try {
            SalesReturnReasonBO reason;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            String s = "SELECT A.ListId, A.ListName, B.ListCode FROM StandardListMaster A"
                    + " INNER JOIN StandardListMaster B ON A.ParentId = B.ListId AND"
                    + " ( B.ListCode = '" + StandardListMasterConstants.SALES_RETURN_NONSALABLE_REASON_TYPE
                    + "' OR B.ListCode = '" + StandardListMasterConstants.SALES_RETURN_SALABLE_REASON_TYPE + "')"
                    + " WHERE A.ListType = 'REASON'";
            Cursor c = db.selectSQL(s);
            if (c != null) {
                reasonSalesReturnMaster = null;
                reasonSalesReturnMaster = new ArrayList<>();
                while (c.moveToNext()) {
                    reason = new SalesReturnReasonBO();
                    reason.setReasonID(c.getString(0));
                    reason.setReasonDesc(c.getString(1));
                    reason.setReasonCategory(c.getString(2));
                    reasonSalesReturnMaster.add(reason);
                }
                c.close();
            }
            db.closeDB();
        } catch (SQLException e) {
            Commons.printException(e);
        }
    }


    public void downloadNonPlannedReason() {
        try {
            ReasonMaster reason;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            String s = "SELECT A.ListId, A.ListName, A.ListCode FROM StandardListMaster A"
                    + " INNER JOIN StandardListMaster B ON A.ParentId = B.ListId AND "
                    + " ( B.ListCode = '" + StandardListMasterConstants.NON_PLANNED_REASON_TYPE
                    + "') WHERE A.ListType ='REASON'";
            Cursor c = db.selectSQL(s);
            if (c != null) {
                nonPlanedReasons = null;
                nonPlanedReasons = new ArrayList<>();
                while (c.moveToNext()) {
                    reason = new ReasonMaster();
                    reason.setReasonID(c.getString(0));
                    reason.setReasonDesc(c.getString(1));
                    reason.setReasonCategory(c.getString(2));
                    nonPlanedReasons.add(reason);
                }
                c.close();
            }
            db.closeDB();
        } catch (SQLException e) {
            Commons.printException(e);
        }
    }


    /**
     * Download nonproductive reason for module with image.
     */
    public void downloadNpReason(String RetailerId, String modulename) {
        try {
            reasonsWithPhoto = new NonproductivereasonBO();
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();

            String s = "SELECT RetailerID,ModuleCode,ReasonID,ImagePath,ImageName FROM NonProductiveModules "
                    + " WHERE RetailerID = '" + RetailerId + "' and ModuleCode = '" + modulename + "' and upload = 'N'";

            Cursor c = db.selectSQL(s);
            if (c != null) {
                while (c.moveToNext()) {
                    reasonsWithPhoto.setRetailerid(c.getString(0));
                    reasonsWithPhoto.setModuleCode(c.getString(1));
                    reasonsWithPhoto.setReasonid(c.getString(2));
                    reasonsWithPhoto.setImagePath(c.getString(3));
                    reasonsWithPhoto.setImageName(c.getString(4));
                    break;
                }
                c.close();
            }
            db.closeDB();
        } catch (SQLException e) {
            Commons.printException(e);
        }

    }

    public void downloadRemarks() {
        try {
            ReasonMaster reason;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            String s = "SELECT ListId, ListName FROM StandardListMaster WHERE ListType = 'ORDER_REASON'";
            Cursor c = db.selectSQL(s);
            if (c != null) {
                remarksReasonMaster = null;
                remarksReasonMaster = new ArrayList<>();
                while (c.moveToNext()) {
                    reason = new ReasonMaster();
                    reason.setReasonID(c.getString(0));
                    reason.setReasonDesc(c.getString(1));
                    remarksReasonMaster.add(reason);
                }
                c.close();
            }
            db.closeDB();
        } catch (SQLException e) {
            Commons.printException(e);
        }
    }

    public ArrayList<ReasonMaster> getRemarksReasonMaster() {
        return remarksReasonMaster;
    }

    public boolean isNpReasonPhotoAvaiable(String RetailerId, String modulename) {
        boolean isAvaiable = false;
        try {

            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();

            String s = "SELECT ReasonID  FROM NonProductiveModules "
                    + " WHERE RetailerID = '" + RetailerId + "' and ModuleCode = '" + modulename + "' and upload = 'N'";

            Cursor c = db.selectSQL(s);
            if (c != null) {
                if (c.getCount() > 0) {
                    isAvaiable = true;
                }
                c.close();
            }
            db.closeDB();
        } catch (SQLException e) {
            Commons.printException(e);
            isAvaiable = false;
        }

        return isAvaiable;
    }

    public String getReasonFromStdListMaster(String mReasonTypeCode) {
        return ("SELECT ListId, ListName FROM StandardListMaster WHERE ListType = 'REASON'"
                + " AND ParentId = (SELECT ListId FROM StandardListMaster WHERE ListType ='REASON_TYPE' AND ListCode = '" + mReasonTypeCode + "')");
    }

    public void downloadDeviatedReason() {
        ReasonMaster reason;
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        db.openDataBase();

        Cursor c = db.selectSQL(getReasonFromStdListMaster("DEV"));

        if (c != null) {
            setDeviatedReturnMaster(new ArrayList<ReasonMaster>());
            //reason = new ReasonMaster();
            //reason.setReasonDesc(context.getResources().getString(R.string.select_reason));
            //getDeviatedReturnMaster().add(reason);
            while (c.moveToNext()) {
                reason = new ReasonMaster();
                reason.setReasonID(c.getString(0));
                reason.setReasonDesc(c.getString(1));
                getDeviatedReturnMaster().add(reason);
            }
            c.close();
        }
        db.closeDB();
    }

    public void downloadNonVisitReasonMaster() {
        ReasonMaster reason;
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
        db.openDataBase();

        Cursor c = db.selectSQL(getReasonFromStdListMaster("NV"));
        if (c != null) {
            setNonVisitReasonMaster(new ArrayList<ReasonMaster>());
            while (c.moveToNext()) {
                reason = new ReasonMaster();
                reason.setReasonID(c.getString(0));
                reason.setReasonDesc(c.getString(1));
                getNonVisitReasonMaster().add(reason);
            }
            c.close();
        }
        db.closeDB();
    }


    public void downloadPlaneDeviateReasonMaster(String listType) {
        try {
            ReasonMaster reason;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            String s = "SELECT ListId, ListName,CASE WHEN ifnull(P.id,0) >0 THEN 1 ELSE 0 END as planned FROM StandardListMaster S" +
                    " Left join PlannedNonFieldActivity P on P.id=S.ListId WHERE ListType =" + QT(listType);
            Cursor c = db.selectSQL(s);
            if (c != null) {
                reasonPlaneDeviationMaster = null;
                reasonPlaneDeviationMaster = new ArrayList<>();
                while (c.moveToNext()) {
                    reason = new ReasonMaster();
                    reason.setReasonID(c.getString(0));
                    reason.setReasonDesc(c.getString(1));
                    reason.setIsPlanned(c.getInt(2));
                    reasonPlaneDeviationMaster.add(reason);
                }
                c.close();
                reason = new ReasonMaster();
                reason.setReasonID("0");
                reason.setReasonDesc("Others");
                reason.setIsPlanned(1);
                reasonPlaneDeviationMaster.add(reason);
            }
            db.closeDB();
        } catch (SQLException e) {
            Commons.printException(e);
        }
    }


    public void downloadPlannedActivitiesReasonMaster(String listType) {
        try {
            ReasonMaster reason;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            String s = "SELECT ListId, ListName FROM StandardListMaster S INNER JOIN PlannedNonFieldActivity P on P.id=S.ListId WHERE ListType =" + QT(listType);
            Cursor c = db.selectSQL(s);
            if (c != null) {
                reasonPlanedActivities = null;
                reasonPlanedActivities = new ArrayList<>();
                while (c.moveToNext()) {
                    reason = new ReasonMaster();
                    reason.setReasonID(c.getString(0));
                    reason.setReasonDesc(c.getString(1));
                    reasonPlanedActivities.add(reason);
                }
                c.close();
            }
            db.closeDB();
        } catch (SQLException e) {
            Commons.printException(e);
        }
    }


    public void downloadNonProductiveReasonMaster() {
        ReasonMaster reason;
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        db.openDataBase();

        Cursor c = db.selectSQL(getReasonFromStdListMaster("NP"));
        if (c != null) {
            setNonProductiveReasonMaster(new ArrayList<ReasonMaster>());
            while (c.moveToNext()) {
                reason = new ReasonMaster();
                reason.setReasonID(c.getString(0));
                reason.setReasonDesc(c.getString(1));
                getNonProductiveReasonMaster().add(reason);
            }
            c.close();
        }
        reason = new ReasonMaster();
        reason.setReasonID("0");
        reason.setReasonDesc(context.getResources().getString(R.string.other_reason_with_credit));
        getNonProductiveReasonMaster().add(reason);
        db.closeDB();
    }

    public void loadAssetReasonsBasedOnType(String type) {
        ReasonMaster reason;
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        db.openDataBase();

        Cursor c = db.selectSQL(getReasonFromStdListMaster(type));
        if (c != null) {
            setAssetReasonsBasedOnType(new ArrayList<ReasonMaster>());
            while (c.moveToNext()) {
                reason = new ReasonMaster();
                reason.setReasonID(c.getString(0));
                reason.setReasonDesc(c.getString(1));
                getAssetReasonsBasedOnType().add(reason);
            }
            c.close();
        }
        db.closeDB();
    }

    public ArrayList<ReasonMaster> getNonVisitReasonMaster() {
        return reasonNonVisitMaster;
    }

    private void setNonVisitReasonMaster(
            ArrayList<ReasonMaster> reasonNonProductiveMaster) {
        this.reasonNonVisitMaster = reasonNonProductiveMaster;
    }

    public ArrayList<ReasonMaster> getReasonPlaneDeviationMaster() {
        return reasonPlaneDeviationMaster;
    }

    private void setReasonPlaneDeviationMaster(ArrayList<ReasonMaster> reasonPlaneDeviationMaster) {
        this.reasonPlaneDeviationMaster = reasonPlaneDeviationMaster;
    }

    private void setDeviatedReturnMaster(
            ArrayList<ReasonMaster> deviatedReturnMaster) {
        this.deviatedReturnMaster = deviatedReturnMaster;
    }

    public ArrayList<ReasonMaster> getReasonPlanedActivities() {
        return reasonPlanedActivities;
    }

    public void setReasonPlanedActivities(ArrayList<ReasonMaster> reasonPlanedActivities) {
        this.reasonPlanedActivities = reasonPlanedActivities;
    }

    public ArrayList<ReasonMaster> getNonPlanedReasons() {
        return nonPlanedReasons;
    }

    public void setNonPlanedReasons(ArrayList<ReasonMaster> nonPlanedReasons) {
        this.nonPlanedReasons = nonPlanedReasons;
    }


    public ArrayList<ReasonMaster> getDeviatedReturnMaster() {
        return deviatedReturnMaster;
    }

    public ArrayList<ReasonMaster> getNonProductiveReasonMaster() {
        return nonProductiveReasonMaster;
    }

    private void setNonProductiveReasonMaster(
            ArrayList<ReasonMaster> nonProductiveReasonMaster) {
        this.nonProductiveReasonMaster = nonProductiveReasonMaster;
    }

    /**
     * this method will get the saved nonvisit reason id of a particular
     * retailerid. Date should also match.
     *
     * @param retailerObj retailer details object
     * @return reasonId
     */
    public int getSavedNonVisitReason(RetailerMasterBO retailerObj) {
        int i = 0;
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c1 = db
                    .selectSQL("SELECT ReasonID FROM Nonproductivereasonmaster where retailerid="
                            + retailerObj.getRetailerID()
                            + " and DistributorID="
                            + retailerObj.getDistributorId()
                            + " and ReasonTypes=(select listid from StandardListMaster where ListCode='NV') and Date='"
                            + bmodel.userMasterHelper.getUserMasterBO()
                            .getDownloadDate() + "'");
            if (c1 != null) {
                if (c1.moveToNext()) {
                    i = c1.getInt(0);
                }
                c1.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
            return 0;
        }
        return i;
    }

    private String QT(String data) {
        return "'" + data + "'";
    }


    public void downloadReasons() {
        reasonList.clear();
        try {
            ReasonMaster reason;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();

            StringBuilder sb = new StringBuilder();
            sb.append("SELECT A.ListId, A.ListName, B.ListCode FROM StandardListMaster A");
            sb.append(" INNER JOIN StandardListMaster B ON A.ParentId = B.ListId");
            sb.append(" WHERE A.ListType = 'REASON'");

            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                setReasonList(new ArrayList<ReasonMaster>());
                ReasonMaster reasonBO;
                reasonBO = new ReasonMaster();
                reasonBO.setReasonID("0");
                reasonBO.setReasonDesc(context.getResources().getString(R.string.plain_select));
                reasonBO.setReasonCategory("NONE");
                getReasonList().add(reasonBO);
                while (c.moveToNext()) {
                    reason = new ReasonMaster();
                    reason.setReasonID(c.getString(0));
                    reason.setReasonDesc(c.getString(1));
                    reason.setReasonCategory(c.getString(2));
                    getReasonList().add(reason);
                }
                c.close();
            }else
                c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("Load Reason", e);
        }
    }

    public ArrayList<ReasonMaster> getReasonList() {
        return reasonList;
    }

    private void setReasonList(ArrayList<ReasonMaster> reasonList) {
        this.reasonList = reasonList;
    }

    public ArrayList<SalesReturnReasonBO> getReasonSalesReturnMaster() {
        return reasonSalesReturnMaster;
    }

    public ArrayList<StandardListBO> getReasonSalesReturnCategory() {
        ArrayList<StandardListBO> categoryList = new ArrayList<>();
        try {

            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            String s = "SELECT A.ListId, A.ListName, A.ListCode FROM StandardListMaster A"
                    + "  Where (A.ListCode = '" + StandardListMasterConstants.SALES_RETURN_NONSALABLE_REASON_TYPE
                    + "' OR A.ListCode = '" + StandardListMasterConstants.SALES_RETURN_SALABLE_REASON_TYPE + "') and A.ListType = 'REASON_TYPE' ";
            Cursor c = db.selectSQL(s);
            if (c != null) {
                categoryList = new ArrayList<>();
                while (c.moveToNext()) {
                    StandardListBO category = new StandardListBO();
                    category.setListID(c.getString(0));
                    category.setListName(c.getString(1));
                    category.setListCode(c.getString(2));
                    categoryList.add(new StandardListBO(category));
                }
                c.close();
            }
            db.closeDB();
        } catch (SQLException e) {
            Commons.printException(e);
        }

        return categoryList;
    }


    public void downloadClosecallReasonList() {
        try {
            clcrReason = new ArrayList<>();
            ReasonMaster reasonMaster;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();

            Cursor c = db.selectSQL(getReasonFromStdListMaster("CLCR"));

            if (c != null) {
                while (c.moveToNext()) {
                    reasonMaster = new ReasonMaster();
                    reasonMaster.setReasonID(c.getString(0));
                    reasonMaster.setReasonDesc(c.getString(1));
                    clcrReason.add(reasonMaster);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }

    public ArrayList<ReasonMaster> getClosecallReasonList() {
        return clcrReason;
    }

    private ArrayList<ReasonMaster> ordfreasonlist;

    public void downloadOrderFullfillmentReason() {
        try {
            ReasonMaster reason;
            ordfreasonlist = new ArrayList<>();
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            reason = new ReasonMaster();
            reason.setReasonDesc(context.getResources().getString(R.string.select_reason));
            ordfreasonlist.add(reason);
            Cursor c = db.selectSQL(getReasonFromStdListMaster("FFR"));
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    reason = new ReasonMaster();
                    reason.setReasonID(c.getString(0));
                    reason.setReasonDesc(c.getString(1));
                    reason.setReasonCategory("FFR");
                    ordfreasonlist.add(reason);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }

    public ArrayList<ReasonMaster> getOrderFullfillmentReason() {
        return ordfreasonlist;
    }

    public void downloadPrimSaleReasonList() {
        try {
            ReasonMaster reason;
            priminvoicestatus = new ArrayList<>();
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            reason = new ReasonMaster();
            reason.setReasonDesc(context.getResources().getString(R.string.select));
            reason.setReasonID("0");
            priminvoicestatus.add(reason);
            Cursor c = db
                    .selectSQL("Select ListName,ListCode,ListId from StandardListMaster where ListType='PRIMARY_SALES_STATUS_TYPE'");
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    reason = new ReasonMaster();

                    reason.setReasonDesc(c.getString(0));
                    reason.setReasonCategory(c.getString(1));
                    reason.setReasonID(c.getString(2));
                    priminvoicestatus.add(reason);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }

    public ArrayList<ReasonMaster> getPrimSaleReasonList() {
        return priminvoicestatus;
    }

    public int getSelectedPosition(String StatusId) {
        for (int i = 0; i < priminvoicestatus.size(); i++) {
            if (priminvoicestatus.get(i).getReasonID() != null && priminvoicestatus.get(i).getReasonID().equals(StatusId)) {
                return i;
            }
        }
        return 0;
    }

    public ArrayList<ReasonMaster> downloadIndicativeReasons() {
        ArrayList<ReasonMaster> reasons = new ArrayList<>();
        try {
            ReasonMaster reasonMaster;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();

            Cursor c = db.selectSQL(getReasonFromStdListMaster("ORDER"));

            if (c != null) {
                while (c.moveToNext()) {
                    reasonMaster = new ReasonMaster();
                    reasonMaster.setReasonID(c.getString(0));
                    reasonMaster.setReasonDesc(c.getString(1));
                    reasons.add(reasonMaster);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e + "");
        }
        return reasons;
    }

    public void saveNpReasons(NonproductivereasonBO nonproductivereasonBO) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            bmodel.outletTimeStampHelper.deleteTimeStampModuleWise(nonproductivereasonBO.getModuleCode());

            db.deleteSQL(
                    "NonProductiveModules",
                    "RetailerID=" + QT(nonproductivereasonBO.getRetailerid())
                            + " and ModuleCode="
                            + QT(nonproductivereasonBO.getModuleCode()), false);


            String values = bmodel.outletTimeStampHelper.getUid() + "," + QT(nonproductivereasonBO.getRetailerid()) + ","
                    + QT(nonproductivereasonBO.getModuleCode()) + ","
                    + nonproductivereasonBO.getReasonid() + "," + QT(nonproductivereasonBO.getImagePath()) + "," + QT(nonproductivereasonBO.getImageName());
            String sql = "insert into NonProductiveModules (Tid,RetailerID,ModuleCode,ReasonID,ImagePath,ImageName) values("
                    + values + ")";

            db.executeQ(sql);

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public NonproductivereasonBO getReasonsWithPhoto() {
        return reasonsWithPhoto;
    }

    public void setReasonsWithPhoto(NonproductivereasonBO reasonsWithPhoto) {
        this.reasonsWithPhoto = reasonsWithPhoto;
    }

    public void saveImage(String imgName, String imgPath) {
        if (reasonsWithPhoto != null) {
            reasonsWithPhoto.setImageName(imgName);
            reasonsWithPhoto.setImagePath(imgPath);
        }
    }

    private void deleteNonproductiveReason(DBUtil db, String module) {
        db.deleteSQL(
                "NonProductiveModules",
                "RetailerID=" + QT(bmodel.getRetailerMasterBO().getRetailerID())
                        + " and ModuleCode="
                        + QT(module), false);

    }

    public ArrayList<StandardListBO> downloadCSAgeGroup() {
        ArrayList<StandardListBO> lst = null;
        try {
            StandardListBO reason;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            String s = "SELECT ListId, ListName, ListCode FROM StandardListMaster "
                    + " WHERE ListTYPE = '" + StandardListMasterConstants.COUNTER_SALES_AGE_GROUP_TYPE + "'";

            Cursor c = db.selectSQL(s);
            if (c != null) {
                lst = new ArrayList<>();
                while (c.moveToNext()) {
                    reason = new StandardListBO();
                    reason.setListID(c.getString(0));
                    reason.setListName(c.getString(1));
                    reason.setListCode(c.getString(2));
                    lst.add(reason);
                }
                c.close();
            }
            db.closeDB();
        } catch (SQLException e) {
            Commons.printException(e);
        }
        return lst;
    }

    public void setAssetReasonsBasedOnType(ArrayList<ReasonMaster> assetReasonsBasedOnType) {
        this.assetReasonsBasedOnType = assetReasonsBasedOnType;
    }

    public ArrayList<ReasonMaster> getAssetReasonsBasedOnType() {
        return assetReasonsBasedOnType;
    }


    public void downloadRemarksType() {
        try {
            ReasonMaster reason;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            String s = "SELECT ListId, ListName FROM StandardListMaster WHERE ListType = 'REMARKS_TYPE'";
            Cursor c = db.selectSQL(s);
            if (c != null) {
                remarksType = new ArrayList<>();
                while (c.moveToNext()) {
                    reason = new ReasonMaster();
                    reason.setReasonID(c.getString(0));
                    reason.setReasonDesc(c.getString(1));
                    remarksType.add(reason);
                }
                c.close();
            }
            db.closeDB();
        } catch (SQLException e) {
            Commons.printException(e);
        }
    }


    public ArrayList<ReasonMaster> getRemarksType() {
        if (remarksType == null) {
            remarksType = new ArrayList<>();
        }
        return remarksType;
    }

    public void downloadShipmentType() {
        try {
            ReasonMaster reason;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            String s = "SELECT ListId, ListName FROM StandardListMaster WHERE ListType = 'SHIPMENT_TYPE'";
            Cursor c = db.selectSQL(s);
            if (c != null) {
                shipmentType = new ArrayList<>();
                while (c.moveToNext()) {
                    reason = new ReasonMaster();
                    reason.setReasonID(c.getString(0));
                    reason.setReasonDesc(c.getString(1));
                    shipmentType.add(reason);
                }
                c.close();
            }
            db.closeDB();
        } catch (SQLException e) {
            Commons.printException(e);
        }
    }

    public ArrayList<ReasonMaster> getShipMentType() {
        if (shipmentType == null) {
            shipmentType = new ArrayList<>();
        }
        return shipmentType;
    }

    public void downloadPaymentType() {
        try {
            ReasonMaster reason;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            String s = "SELECT ListId, ListName FROM StandardListMaster WHERE ListType = 'PAYTERM_TYPE'";
            Cursor c = db.selectSQL(s);
            if (c != null) {
                paymentType = new ArrayList<>();
                while (c.moveToNext()) {
                    reason = new ReasonMaster();
                    reason.setReasonID(c.getString(0));
                    reason.setReasonDesc(c.getString(1));
                    paymentType.add(reason);
                }
                c.close();
            }
            db.closeDB();
        } catch (SQLException e) {
            Commons.printException(e);
        }
    }


    public ArrayList<ReasonMaster> getPayTermType() {
        if (paymentType == null) {
            paymentType = new ArrayList<>();
        }
        return paymentType;
    }

    public void downloadDistChannelType() {
        try {
            ReasonMaster reason;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            String s = "SELECT ListId, ListName FROM StandardListMaster WHERE ListType = 'DIST_CHANNEL_TYPE'";
            Cursor c = db.selectSQL(s);
            if (c != null) {
                distributionChannelType = new ArrayList<>();
                while (c.moveToNext()) {
                    reason = new ReasonMaster();
                    reason.setReasonID(c.getString(0));
                    reason.setReasonDesc(c.getString(1));
                    distributionChannelType.add(reason);
                }
                c.close();
            }
            db.closeDB();
        } catch (SQLException e) {
            Commons.printException(e);
        }
    }

    public ArrayList<ReasonMaster> getDistributionChannelType() {
        if (distributionChannelType == null) {
            distributionChannelType = new ArrayList<>();
        }
        return distributionChannelType;
    }


    public ArrayList<ReasonMaster> getRetailerAddress(String retailerId,
                                                      String addressCode) {
        DBUtil db = null;
        ArrayList<ReasonMaster> addressList = new ArrayList<>();
        StringBuilder addressSB = new StringBuilder();
        if (addressCode.contains(",")) {
            String[] addressArray = addressCode.split(",");
            int i = 0;
            for (String addCodes : addressArray) {
                if (addressSB.length() > 0)
                    addressSB.append(",");

                addressSB.append(StringUtils.getStringQueryParam(addCodes));
                i = i + 1;
            }
        } else
            addressSB.append(addressCode);
        try {
            ReasonMaster reason;
            db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            String s = "SELECT RA.AddressId,RA.Address1,RA.Address2,RA.Address3  FROM StandardListMaster SLM"
                    + " LEFT JOIN RetailerAddress RA ON SLM.ListId = RA.AddressTypeID"
                    + " WHERE RA.RetailerId =" + retailerId
                    + " AND SLM.ListCode in(" + addressSB.toString() + ")"
                    + " AND ListType = 'ADDRESS_TYPE'";
            Cursor c = db.selectSQL(s);
            if (c != null) {
                while (c.moveToNext()) {
                    reason = new ReasonMaster();
                    StringBuilder address = new StringBuilder();
                    reason.setReasonID(c.getString(0));

                    address.append(c.getString(1));
                    address.append(", ");

                    if (c.getString(2).length() > 0) {
                        address.append(c.getString(2));
                        address.append(", ");
                    }
                    if (c.getString(3).length() > 0) {
                        address.append(c.getString(3));
                        address.append(",");
                    }

                    reason.setReasonDesc(address.toString());
                    addressList.add(reason);
                }
                c.close();
            }
            return addressList;
        } catch (SQLException e) {
            Commons.printException(e);
        } finally {
            if (db != null)
                db.closeDB();
        }
        return new ArrayList<>();
    }

    public ArrayList<ReasonMaster> getDeletePlanReason() {

        ArrayList<ReasonMaster> reasonList = new ArrayList<>();
        ReasonMaster reason;
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
        db.openDataBase();

        Cursor c = db.selectSQL(getReasonFromStdListMaster("DPR"));
        if (c != null) {
            setNonVisitReasonMaster(new ArrayList<ReasonMaster>());
            while (c.moveToNext()) {
                reason = new ReasonMaster();
                reason.setReasonID(c.getString(0));
                reason.setReasonDesc(c.getString(1));
                reasonList.add(reason);
            }
            c.close();
        }
        db.closeDB();
        return reasonList;

    }

    public ArrayList<ReasonMaster> getReschedulePlanReason() {

        ArrayList<ReasonMaster> reasonList = new ArrayList<>();
        ReasonMaster reason;
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
        db.openDataBase();

        Cursor c = db.selectSQL(getReasonFromStdListMaster("RPR"));
        if (c != null) {
            setNonVisitReasonMaster(new ArrayList<ReasonMaster>());
            while (c.moveToNext()) {
                reason = new ReasonMaster();
                reason.setReasonID(c.getString(0));
                reason.setReasonDesc(c.getString(1));
                reasonList.add(reason);
            }
            c.close();
        }
        db.closeDB();
        return reasonList;

    }

}
