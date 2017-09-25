package com.ivy.sd.png.provider;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.ChildLevelBo;
import com.ivy.sd.png.bo.ParentLevelBo;
import com.ivy.sd.png.bo.PromotionBO;
import com.ivy.sd.png.bo.SchemeBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.HomeScreenFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class PromotionHelper {

    private List<SchemeBO> mSchemePromotion;
    private final Context context;
    private final BusinessModel bmodel;
    private static PromotionHelper instance = null;
    // Retail Modules Part
    public int mSelectedPromoID = 0;
    private Vector<ParentLevelBo> mParentLevelBo;
    private Vector<ChildLevelBo> mChildLevelBo;
    private ArrayList<PromotionBO> mPromotionList;
    private ArrayList<PromotionBO> mAllPromotionList;
    private ArrayList<StandardListBO> mRatingList;

    private PromotionHelper(Context context) {
        this.context = context;
        bmodel = (BusinessModel) context;
    }

    public static PromotionHelper getInstance(Context context) {
        if (instance == null) {
            instance = new PromotionHelper(context);
        }
        return instance;
    }

    public void loadDataForPromotion(String mMenuCode) {
        loadPromotionConfigs();

        bmodel.productHelper.downloadInStoreLocations();

        if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER)
            bmodel.productHelper.downloadFiveLevelFilterNonProducts(mMenuCode);
        else
            downloadlFilterDataForPromotion();

        loadData(mMenuCode);
        loadPromoEntered();
    }

    public boolean SHOW_PROMO_TYPE;
    public boolean SHOW_PROMO_RATING;
    public boolean SHOW_PROMO_REASON;
    public boolean SHOW_PROMO_PHOTO;
    public boolean SHOW_PROMO_QTY;
    public boolean SHOW_PROMO_ANNOUNCER;

    private void loadPromotionConfigs() {
        try {
            SHOW_PROMO_TYPE = false;
            SHOW_PROMO_RATING = false;
            SHOW_PROMO_REASON = false;
            SHOW_PROMO_PHOTO = false;
            SHOW_PROMO_QTY = false;
            SHOW_PROMO_ANNOUNCER = false;

            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            String sql = "SELECT RField FROM " + DataMembers.tbl_HhtModuleMaster
                    + " WHERE hhtCode='PROMO01' AND Flag=1";

            Cursor c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    if (c.getString(0) != null) {
                        String codeSplit[] = c.getString(0).split(",");
                        for (String temp : codeSplit) {
                            switch (temp) {
                                case "TYPE":
                                    this.SHOW_PROMO_TYPE = true;
                                    break;
                                case "RATING":
                                    this.SHOW_PROMO_RATING = true;
                                    break;
                                case "REASON":
                                    this.SHOW_PROMO_REASON = true;
                                    break;
                                case "PHOTO":
                                    this.SHOW_PROMO_PHOTO = true;
                                    break;
                                case "QTY":
                                    this.SHOW_PROMO_QTY = true;
                                    break;
                                case "ANNOUNCER":
                                    this.SHOW_PROMO_ANNOUNCER = true;
                                    break;
                            }
                        }
                    }
                }
                c.close();
                db.closeDB();
            }

        } catch (Exception e) {
            Commons.printException("loadAssetConfigs " + e);
        }
    }

    public List<SchemeBO> getmSchemePromotion() {
        return mSchemePromotion;
    }

    /**
     * This method is used to get the next available up scheme. Designed by
     * Vinoth.R for a demo.
     *
     * @param schemeId
     * @param type
     * @param channelId
     * @param subChannelId
     * @param productID
     * @param quantity
     */
    public void loadSchemePromotion(String schemeId, String type,
                                    String channelId, String subChannelId, String productID,
                                    int quantity) {
        if (mSchemePromotion == null) {
            mSchemePromotion = new ArrayList<>();
        } else {
            mSchemePromotion.clear();
        }
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.openDataBase();
        StringBuilder sb = new StringBuilder("");
        if ("ANY".equalsIgnoreCase(type)) {

            Cursor c = db
                    .selectSQL("SELECT ProductID, BuyQty FROM SchemeBuyMaster WHERE SchemeID = '"
                            + schemeId + "'");
            if (c != null && c.getCount() > 0) {

                sb.append(" AND BD.ProductID IN (");
                while (c.moveToNext()) {
                    sb.append("'");
                    sb.append(c.getString(0));
                    sb.append("'");
                    sb.append(",");
                }
                sb.delete(sb.length() - 1, sb.length());
                sb.append(") ");
                sb.append("AND TYPE = 'ANY' ");
                c.close();
            }

        } else {
            sb = new StringBuilder(" AND BD.ProductID = '" + productID
                    + "' AND TYPE = 'ONLY' ");
        }

        Cursor c = db
                .selectSQL("SELECT SM.SchemeID, SM.Description, SM.Type, SM.ShortName, SM.ChannelID, SM.SubChannelID, "
                        + "BD.ProductID, PM.PName, BD.BuyQty, FD.FreeQty, FD.MaxQty, FD.Rate, FD.MaxRate FROM SchemeMaster SM "
                        + "INNER JOIN  SchemeBuyMaster BD ON BD.SchemeID = SM.SchemeID  INNER JOIN ProductMaster PM ON BD.ProductID = PM.PID "
                        + "INNER JOIN SchemeFreeMaster FD ON FD.FreeProductID = BD.ProductID AND FD.SchemeID = BD.SchemeID WHERE SM.ChannelID = '"
                        + channelId
                        + "' AND "
                        + "SM.SubChannelID = '"
                        + subChannelId
                        + "'"
                        + sb
                        + "AND BD.BuyQty > "
                        + quantity
                        + " ORDER BY SM.SchemeID, BD.ProductID ASC, BD.BuyQty DESC");

        if (c != null && c.getCount() > 0) {
            SchemeBO schemeBO = new SchemeBO();

            while (c.moveToNext()) {
                schemeBO.setSchemeId(c.getString(0));
                schemeBO.setSchemeDescription(c.getString(1));
                schemeBO.setType(c.getString(2));
                if (c.getString(3) != null) {
                    schemeBO.setSchemeDescription(c.getString(3));
                }
                schemeBO.setChannelId(c.getString(4));
                schemeBO.setSubChannelId(c.getString(5));
                schemeBO.setSchemeParentName(c.getString(7));
                schemeBO.setSelectedQuantity(c.getInt(8)); // Buy Qty
                schemeBO.setActualQuantity(c.getInt(9)); // Min Qty
                schemeBO.setMaximumQuantity(c.getInt(10)); // Max Qty
                schemeBO.setActualPrice(c.getInt(11)); // Min Disc Rate
                schemeBO.setMaximumPrice(c.getInt(12)); // Max Disc Rate
                mSchemePromotion.add(schemeBO);
            }
            c.close();
        }
        db.closeDB();

    }
    // Promotion Tracking for Retail Module Part

    /**
     * Download the parent level filter and child level filter content
     */
    private void downloadlFilterDataForPromotion() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
        int temp = -1;
        ParentLevelBo parentLevelBo;
        ChildLevelBo childLevelBo;
        db.openDataBase();
        int parentLevel = 0;
        int childLevel = 0;
        String parentlevelName = "";
        String childLevelName = "";
        int levelGap;
        try {
            Cursor level = db
                    .selectSQL("SELECT IFNULL(PL1.Sequence,0), IFNULL(PL2.Sequence,0),"
                            + " PL1.LevelName, PL2.LevelName FROM ConfigActivityFilter CF"
                            + " LEFT JOIN ProductLevel PL1 ON PL1.LevelId = CF.ProductFilter1"
                            + " LEFT JOIN ProductLevel PL2 ON PL2.LevelId = CF.ProductFilter2"
                            + " WHERE CF.ActivityCode = 'MENU_PROMO'");

            if (level != null) {
                if (level.moveToNext()) {
                    parentLevel = level.getInt(0);
                    childLevel = level.getInt(1);
                    parentlevelName = level.getString(2);
                    childLevelName = level.getString(3);
                }
                level.close();
            }

            levelGap = (childLevel - parentLevel) + 1;

            String query;
            if (parentLevel != 0 && childLevel != 0) {

                query = "SELECT DISTINCT P1.pid  PF_Pid, P1.Pname PF_Pname, P"
                        + levelGap + ".pid,P" + levelGap
                        + ".pname FROM ProductMaster P1";

                for (int j = 2; j <= levelGap; j++) {
                    query = query + " inner join ProductMaster P" + j + " on P"
                            + j + ".parentid =P" + (j - 1) + ".pid";
                }
                query = query

                        + " INNER JOIN PromotionProductMapping PPM on PPM.pid =P"
                        + levelGap
                        + ".pid"
                        + " INNER JOIN PromotionMapping PMM on PMM.PromoId = PPM.PromoId  INNER JOIN PromotionMaster PM on PM.HId=PMM.HId "
                        + " WHERE P1.PLID IN (SELECT ProductFilter1 FROM ConfigActivityFilter WHERE ActivityCode='MENU_PROMO'"
                        + ")" + " AND  "
                        + bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL))
                        + " between PM.StartDate and PM.EndDate";

                Cursor c = db.selectSQL(query);
                if (c != null) {
                    setmParentLevelBo(new Vector<ParentLevelBo>());
                    setmChildLevelBo(new Vector<ChildLevelBo>());
                    while (c.moveToNext()) {
                        int parentid = c.getInt(0);
                        if (temp != parentid) {
                            parentLevelBo = new ParentLevelBo();
                            parentLevelBo.setPl_productid(c.getInt(0));
                            parentLevelBo.setPl_levelName(c.getString(1));
                            parentLevelBo.setPl_productLevel(parentlevelName);
                            getmParentLevelBo().add(parentLevelBo);
                            temp = parentid;
                        }
                        childLevelBo = new ChildLevelBo();
                        childLevelBo.setParentid(c.getInt(0));
                        childLevelBo.setProductid(c.getInt(2));
                        childLevelBo.setPlevelName(c.getString(3));
                        childLevelBo.setProductLevel(childLevelName);
                        getmChildLevelBo().add(childLevelBo);
                    }

                    c.close();
                }
                db.closeDB();
            } else if (parentLevel != 0) {
                query = "select Distinct P.ParentId,P.PID,P.PName from ProductMaster P "
                        + " INNER JOIN PromotionProductMapping PPM on PPM.pid = P.pid"
                        + " INNER JOIN PromotionMapping PMM on PMM.PromoId = PPM.PromoId"
                        + " INNER JOIN PromotionMaster PM on PM.HId=PMM.HId"
                        + " WHERE P.PLID IN (SELECT ProductFilter1 FROM ConfigActivityFilter WHERE ActivityCode='MENU_PROMO')"
                        + " AND "
                        + bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL))
                        + "  between PM.StartDate and PM.EndDate ORDER BY P.PID";

                Cursor c = db.selectSQL(query);
                if (c != null) {
                    setmChildLevelBo(new Vector<ChildLevelBo>());
                    while (c.moveToNext()) {
                        childLevelBo = new ChildLevelBo();
                        childLevelBo.setParentid(c.getInt(0));
                        childLevelBo.setProductid(c.getInt(1));
                        childLevelBo.setPlevelName(c.getString(2));
                        childLevelBo.setProductLevel(parentlevelName);

                        getmChildLevelBo().add(childLevelBo);
                    }
                    c.close();
                }
                db.closeDB();
            }// end of else
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
        }
    }

    /**
     * Download Promotion Data
     */
    public void downloadAllPromotionMaster() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
        try {
            PromotionBO promotionMaster;
            db.openDataBase();
            Cursor c;
            c = db.selectSQL("select PM.PromoId,PM.PId,PM. PromoName"
                    + "  from PromotionProductMapping PM");

            if (c != null) {
                setmAllPromotionList(new ArrayList<PromotionBO>());

                while (c.moveToNext()) {
                    promotionMaster = new PromotionBO();
                    promotionMaster.setPromoId(c.getInt(0));
                    promotionMaster.setProductId(c.getInt(1));
                    promotionMaster.setPromoName(c.getString(2));

                    getmAllPromotionList().add(promotionMaster);
                }
                c.close();
            }
            db.closeDB();

        } catch (Exception e) {
            db.closeDB();
            Commons.printException("" + e);
        }
    }

    private void loadData(String mMenuCode) {
        try {
            switch (bmodel.productHelper.getRetailerlevel(mMenuCode)) {
                case 1:
                    downloadPromotionMaster(true, false, false, 0, 0);
                    break;
                case 2:
                    downloadPromotionMaster(false, true, false, 0, 0);
                    break;
                case 3:
                    downloadPromotionMaster(false, false, true, 0, 0);
                    break;
                case 4:
                    downloadPromotionMaster(false, false, false, bmodel.productHelper.locid, 0);
                    break;
                case 5:
                    downloadPromotionMaster(false, false, false, 0, bmodel.productHelper.chid);
                    break;
                case 6:
                    downloadPromotionMaster(false, false, false, bmodel.productHelper.locid, bmodel.productHelper.chid);
                    break;
                case 7:
                    downloadPromotionMaster(false, false, true, 0, bmodel.productHelper.chid);
                    break;
                case 8:
                    downloadPromotionMaster(true, false, false, 0, bmodel.productHelper.chid);
                    break;
                case -1:
                    break;
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    /**
     * Download Promotion Data
     */
    private void downloadPromotionMaster(boolean isaccount, boolean isretailer, boolean isclass, int locid, int chid) {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
        try {
            PromotionBO promotionMaster;
            db.openDataBase();
            Cursor c;
            String query = "";
            if (isaccount && chid > 0) {
                query = " where PM.AccId=" + bmodel.getRetailerMasterBO().getAccountid();
                query = query + " and (PM.ChId=" + bmodel.getRetailerMasterBO().getSubchannelid() + " OR PM.Chid in(" + bmodel.schemeDetailsMasterHelper.getChannelidForScheme(bmodel.getRetailerMasterBO().getSubchannelid()) + "))";

            } else if (isaccount)
                query = " where PM.AccId=" + bmodel.getRetailerMasterBO().getAccountid();
            else if (isretailer)
                query = " where PM.retailerid=" + bmodel.getRetailerMasterBO().getRetailerID();

            else if (isclass && chid > 0) {
                query = " where PM.ClassId=" + bmodel.getRetailerMasterBO().getClassid();
                query = query + " and (PM.ChId=" + bmodel.getRetailerMasterBO().getSubchannelid() + " OR PM.Chid in(" + bmodel.schemeDetailsMasterHelper.getChannelidForScheme(bmodel.getRetailerMasterBO().getSubchannelid()) + "))";
            } else if (isclass)
                query = " where PM.ClassId=" + bmodel.getRetailerMasterBO().getClassid();

            else if (locid > 0 && chid > 0) {
                query = " where  (PM.LocId=" + bmodel.getRetailerMasterBO().getLocationId() + " OR PM.LocId in(" + bmodel.schemeDetailsMasterHelper.getLocationIdsForScheme() + "))";
                query = query + " and (PM.ChId=" + bmodel.getRetailerMasterBO().getSubchannelid() + " OR PM.Chid in(" + bmodel.schemeDetailsMasterHelper.getChannelidForScheme(bmodel.getRetailerMasterBO().getSubchannelid()) + "))";
            } else if (locid > 0)
                query = " where  (PM.LocId=" + bmodel.getRetailerMasterBO().getLocationId() + " OR PM.LocId in(" + bmodel.schemeDetailsMasterHelper.getLocationIdsForScheme() + "))";
            else if (chid > 0)
                query = " where  (PM.ChId=" + bmodel.getRetailerMasterBO().getSubchannelid() + " OR PM.Chid in(" + bmodel.schemeDetailsMasterHelper.getChannelidForScheme(bmodel.getRetailerMasterBO().getSubchannelid()) + "))";

            if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY)
                query = query + "and PPM.PId = " + bmodel.productHelper.getmSelectedGlobalProductId();

            c = db.selectSQL("select DISTINCT PPM.PromoId,PPM.PId,PPM.PromoName,PM.MappingId,SLM.listname"
                    + "  from PromotionMapping PM"
                    + " inner join PromotionMaster PMM on PM.HId = PMM.HId and " + bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL)) +
                    " between PMM.StartDate and PMM.EndDate inner join PromotionProductMapping PPM on PPM.PromoId=PM.PromoId" +
                    " left join standardlistmaster SLM on SLM.listid=PPm.PromoTypeLovId " + query);


            if (c != null) {
                setmPromotionList(new ArrayList<PromotionBO>());
                while (c.moveToNext()) {
                    promotionMaster = new PromotionBO();
                    promotionMaster.setPromoId(c.getInt(0));
                    promotionMaster.setProductId(c.getInt(1));
                    promotionMaster.setPromoName(c.getString(2));
                    promotionMaster.setMappingId(c.getInt(3));
                    promotionMaster.setGroupName(c.getString(4));
                    getmPromotionList().add(promotionMaster);
                }

                if (mPromotionList != null) {
                    for (StandardListBO standardListBO : bmodel.productHelper.getInStoreLocation()) {

                        ArrayList<PromotionBO> clonedList = new ArrayList<>(mPromotionList.size());
                        for (PromotionBO promotionBO : mPromotionList) {
                            clonedList.add(new PromotionBO(promotionBO));
                        }
                        standardListBO.setPromotionTrackingList(clonedList);
                    }
                }
                c.close();
            }
            db.closeDB();

        } catch (Exception e) {
            db.closeDB();
            Commons.printException("" + e);
        }
    }


    /**
     * Save Promotion Details
     *
     * @return True or False
     */
    public boolean savePromotionDetails() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
        StringBuilder sbuffer = new StringBuilder();
        String headerColumns = "UiD,Date,RetailerId,Remark,distributorid";
        String detailColumns = "Uid,PromotionId,BrandId,IsExecuted,RetailerId,ImageName,reasonid,flag,MappingId,Locid,ExecRatingLovId,PromoQty,imgName,HasAnnouncer";
        try {
            db.openDataBase();
            String uid = bmodel.userMasterHelper.getUserMasterBO().getUserid() + SDUtil
                    .now(SDUtil.DATE_TIME_ID);

            Cursor cursor = db
                    .selectSQL("select Uid from PromotionHeader  Where RetailerId="
                            + bmodel.QT(bmodel.retailerMasterBO.getRetailerID())
                            + " and Date= "
                            + bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL))
                            + " and upload='N'");

            if (cursor.getCount() > 0) {
                cursor.moveToNext();
                db.deleteSQL("PromotionHeader",
                        "Uid=" + bmodel.QT(cursor.getString(0)), false);
                db.deleteSQL("PromotionDetail",
                        "Uid=" + bmodel.QT(cursor.getString(0)), false);
                uid = cursor.getString(0);
            }
            cursor.close();

            sbuffer.append(bmodel.QT(uid));
            sbuffer.append(",");
            sbuffer.append(bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL)));
            sbuffer.append(",");
            sbuffer.append(bmodel.retailerMasterBO.getRetailerID());
            sbuffer.append(",");
            sbuffer.append(bmodel.QT(bmodel.getNote()));
            sbuffer.append(",");
            sbuffer.append(bmodel.retailerMasterBO.getDistributorId());

            db.insertSQL("PromotionHeader", headerColumns, sbuffer.toString());
            for (StandardListBO standardListBO : bmodel.productHelper.getInStoreLocation()) {
                ArrayList<PromotionBO> promotionList = standardListBO.getPromotionTrackingList();
                if (promotionList != null) {

                    for (PromotionBO promotion : promotionList) {

                        if (promotion.getIsExecuted() == 1 || !"0".equals(promotion.getReasonID()) || (promotion.getRatingId() != null && !"0".equals(promotion.getRatingId()))) {
                            String sbDetails = bmodel.QT(uid) +
                                    "," + promotion.getPromoId() +
                                    "," + promotion.getProductId() +
                                    "," + promotion.getIsExecuted() +
                                    "," + bmodel.getRetailerMasterBO().getRetailerID() +
                                    "," + bmodel.QT(promotion.getImagePath()) +
                                    "," + promotion.getReasonID() +
                                    "," + bmodel.QT(promotion.getFlag()) +
                                    "," + promotion.getMappingId() +
                                    "," + standardListBO.getListID() +
                                    "," + promotion.getRatingId() +
                                    "," + promotion.getPromoQty() +
                                    "," + bmodel.QT(promotion.getImageName()) +
                                    "," + promotion.getHasAnnouncer();
                            db.insertSQL("PromotionDetail", detailColumns,
                                    sbDetails);
                        }
                    }
                }
            }

            db.closeDB();
        } catch (Exception e) {
            db.closeDB();
            Commons.printException("Saving Promo Check exception", e);
            return false;
        }

        return true;
    }

    /**
     * Get values from Tables and set in Objects while going Edit Mode
     */
    private void loadPromoEntered() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
        try {
            db.openDataBase();
            String uid = "";
            bmodel.setNote("");
            String sql = "SELECT Uid,Remark FROM PromotionHeader WHERE RetailerId = "
                    + bmodel.getRetailerMasterBO().getRetailerID()
                    + " AND Date = "
                    + bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL))
                    + " and upload='N'";

            Cursor cursor = db.selectSQL(sql);

            if (cursor != null) {
                if (cursor.moveToNext()) {
                    uid = cursor.getString(0);
                    bmodel.setNote(cursor.getString(1));
                }
            } else
                return;

            cursor.close();

            String sql1 = "SELECT PromotionId, IsExecuted,imgName,reasonid,brandid,locid,ExecRatingLovId,promoqty,HasAnnouncer FROM PromotionDetail WHERE Uid="
                    + bmodel.QT(uid) + " and Upload ='N' and Flag = 'S'";

            Cursor orderDetailCursor = db.selectSQL(sql1);

            if (orderDetailCursor != null) {
                if (orderDetailCursor.getCount() > 0) {
                    while (orderDetailCursor.moveToNext()) {
                        int promotionID = orderDetailCursor.getInt(0);
                        int isExecuted = orderDetailCursor.getInt(1);
                        String imgName = orderDetailCursor.getString(2);
                        String reasonID = orderDetailCursor.getString(3);
                        int brandID = orderDetailCursor.getInt(4);
                        int locid = orderDetailCursor.getInt(5);
                        int execRatingLovid = orderDetailCursor.getInt(6);
                        int promoQty = orderDetailCursor.getInt(7);
                        int isAnnounced = orderDetailCursor.getInt(8);

                        setPromocheckDetails(promotionID, isExecuted, isAnnounced, imgName,
                                reasonID, brandID, locid, execRatingLovid, promoQty);
                    }
                    orderDetailCursor.close();
                } else {
                    // Loading Last visit transaction data
                    if (bmodel.configurationMasterHelper.IS_PROMOTION_RETAIN_LAST_VISIT_TRAN) {
                        sql1 = "SELECT PromotionId, IsExecuted,reasonid,locid,ExecRatingLovId,promoqty FROM LastVisitPromotion WHERE retailerId="
                                + bmodel.getRetailerMasterBO().getRetailerID() + " and Flag = 'S'";
                        orderDetailCursor = db.selectSQL(sql1);
                        if (orderDetailCursor != null) {
                            while (orderDetailCursor.moveToNext()) {
                                int promotionID = orderDetailCursor.getInt(0);
                                int isExecuted = orderDetailCursor.getInt(1);
                                String reasonID = orderDetailCursor.getString(2);
                                int locid = orderDetailCursor.getInt(3);
                                int execRatingLovid = orderDetailCursor.getInt(4);
                                int promoQty = orderDetailCursor.getInt(5);

                                setLastVisitPromocheckDetails(promotionID, isExecuted,
                                        reasonID, locid, execRatingLovid, promoQty);
                            }
                            orderDetailCursor.close();
                        }
                    }
                }
            }

            sql1 = "SELECT PD.PromotionId, PD.IsExecuted,pd.ImageName,PD.reasonid,PD.brandid,pm.PromoName,pd.ExecRatingLovId,PD.HasAnnouncer FROM PromotionDetail pd"
                    + " inner join PromotionProductMapping  pm on pm.PromoId = pd.PromotionId"
                    + " WHERE Uid="
                    + bmodel.QT(uid)
                    + " and Upload ='N' and Flag = 'I'";

            orderDetailCursor = db.selectSQL(sql1);

            if (orderDetailCursor != null) {
                while (orderDetailCursor.moveToNext()) {
                    PromotionBO promotionMaster = new PromotionBO();
                    promotionMaster.setPromoId(orderDetailCursor.getInt(0));
                    promotionMaster.setIsExecuted(orderDetailCursor.getInt(1));
                    promotionMaster
                            .setImageName(orderDetailCursor.getString(2));
                    promotionMaster.setReasonID(orderDetailCursor.getString(3));
                    promotionMaster.setProductId(orderDetailCursor.getInt(4));
                    promotionMaster
                            .setPromoName(orderDetailCursor.getString(5));
                    promotionMaster
                            .setFlag("I");
                    promotionMaster.setRatingId(orderDetailCursor.getString(6));
                    promotionMaster.setHasAnnouncer(orderDetailCursor.getInt(7));

                    getmPromotionList().add(promotionMaster);

                }
                orderDetailCursor.close();
            }

            db.closeDB();
        } catch (Exception e) {
            db.closeDB();
            Commons.printException("" + e);
        }
    }

    // set the value in the PromotionMasterBo
    private void setPromocheckDetails(int promotionID, int isExecuted, int isAnnounced,
                                      String imgName, String reasonid, int brandID, int locid, int executeLovId, int promoQty) {

        for (StandardListBO standardListBO : bmodel.productHelper.getInStoreLocation()) {
            if (standardListBO.getListID().equals(Integer.toString(locid))) {
                ArrayList<PromotionBO> promotionList = standardListBO.getPromotionTrackingList();
                if (promotionList != null) {
                    for (PromotionBO promo : promotionList) {
                        if (promo.getPromoId() == promotionID
                                && promo.getProductId() == brandID) {
                            promo.setIsExecuted(isExecuted);
                            promo.setHasAnnouncer(isAnnounced);
                            promo.setImageName(imgName);
                            promo.setReasonID(reasonid);
                            promo.setRatingId(Integer.toString(executeLovId));
                            promo.setPromoQty(promoQty);
                            break;
                        }

                    }
                }
                break;
            }
        }

    }

    // set the Last tran value in the PromotionMasterBo
    private void setLastVisitPromocheckDetails(int promotionID, int isExecuted,
                                               String reasonid, int locid, int executeLovId, int promoQty) {

        for (StandardListBO standardListBO : bmodel.productHelper.getInStoreLocation()) {
            if (standardListBO.getListID().equals(Integer.toString(locid))) {
                ArrayList<PromotionBO> promotionList = standardListBO.getPromotionTrackingList();
                if (promotionList != null) {
                    for (PromotionBO promo : promotionList) {
                        if (promo.getPromoId() == promotionID) {
                            promo.setIsExecuted(isExecuted);
                            promo.setImageName("");
                            promo.setReasonID(reasonid);
                            promo.setRatingId(Integer.toString(executeLovId));
                            promo.setPromoQty(promoQty);
                            break;
                        }

                    }
                }
                break;
            }
        }

    }


    /**
     * Save Image in Objects
     *
     * @param mPromoID
     * @param imgName
     */
    public void onsaveImageName(String locid, int mPromoID, String imgName, String imagePath) {
        try {
            for (StandardListBO standardListBO : bmodel.productHelper.getInStoreLocation()) {
                if (locid.equals(standardListBO.getListID())) {
                    ArrayList<PromotionBO> promotionList = standardListBO.getPromotionTrackingList();

                    if (promotionList != null) {
                        for (PromotionBO promotionBO : promotionList) {

                            if (promotionBO.getPromoId() == mPromoID) {
                                promotionBO.setImageName(imgName);
                                promotionBO.setImagePath(imagePath);

                                break;
                            }
                        }

                    }
                    break;
                }
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    /**
     * Validation to check data is entered or not
     *
     * @return
     */
    public boolean hasPromoData() {
        for (StandardListBO standardListBO : bmodel.productHelper.getInStoreLocation()) {

            ArrayList<PromotionBO> promotionList = standardListBO.getPromotionTrackingList();
            if (promotionList != null) {
                for (PromotionBO temp : promotionList) {
                    if (temp.getIsExecuted() > 0 || !"0".equals(temp.getReasonID()) || !"0".equals(temp.getRatingId()))
                        return true;
                }
            }
        }
        return false;
    }

    public void deleteUnusedImages() {

        for (PromotionBO temp : getmPromotionList()) {
            if (temp.getIsExecuted() == 0 && !"".equals(temp.getImageName())) {
                String fileName = temp.getImageName();
                Commons.print("Image Delete," + "Coming In");
                deleteFiles(fileName);
            }
        }
    }

    private void deleteFiles(String filename) {
        File folder = new File(HomeScreenFragment.photoPath + "/");

        File[] files = folder.listFiles();
        for (File tempFile : files) {
            if (tempFile != null && tempFile.getName().equals(filename)) {
                boolean isDeleted = tempFile.delete();
                if (isDeleted)
                    Commons.print("Image Delete," + "Sucess");
            }
        }
    }

    public void downloadPromotionRating() {

        DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
        try {
            db.openDataBase();
            String query = "select listid,listCode,ListName from standardlistmaster where listType='PROMOTION_RATING'";
            Cursor c = db.selectSQL(query);
            if (c.getCount() > 0) {
                mRatingList = new ArrayList<>();
                StandardListBO standardListBO;
                while (c.moveToNext()) {
                    standardListBO = new StandardListBO();
                    standardListBO.setListID(c.getString(0));
                    standardListBO.setListCode(c.getString(1));
                    standardListBO.setListName(c.getString(2));
                    mRatingList.add(standardListBO);

                }
                standardListBO = new StandardListBO();
                standardListBO.setListID("0");
                standardListBO.setListName("-Select ");
                mRatingList.add(0, standardListBO);
            }

        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }
    }

    public ArrayList<StandardListBO> getmRatingList() {
        return mRatingList;
    }


    public ArrayList<PromotionBO> getmPromotionList() {
        return mPromotionList;
    }

    private void setmPromotionList(ArrayList<PromotionBO> mPromotionList) {
        this.mPromotionList = mPromotionList;
    }

    public ArrayList<PromotionBO> getmAllPromotionList() {
        return mAllPromotionList;
    }

    private void setmAllPromotionList(ArrayList<PromotionBO> mAllPromotionList) {
        this.mAllPromotionList = mAllPromotionList;
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

}
