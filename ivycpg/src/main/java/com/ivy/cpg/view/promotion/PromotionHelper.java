package com.ivy.cpg.view.promotion;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.model.ApplicationConfigs;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

import static com.ivy.lib.Utils.QT;

public class PromotionHelper {

    private final BusinessModel businessModel;
    private static PromotionHelper instance = null;
    int mSelectedPromoID = 0;
    int mSelectedProductId=0;
    private ArrayList<PromotionBO> mPromotionList;
    private ArrayList<StandardListBO> mRatingList;
    boolean SHOW_PROMO_TYPE;
    boolean SHOW_PROMO_RATING;
    boolean SHOW_PROMO_REASON;
    boolean SHOW_PROMO_PHOTO;
    boolean SHOW_PROMO_QTY;
    boolean SHOW_PROMO_ANNOUNCER;
    boolean SHOW_PROMO_FEEDBACK;
    boolean SHOW_PROMO_PRICE;
    boolean SHOW_PROMO_LOCATION;
    boolean isNonAcceptedPromotionsAvailable = false;
    private Vector<ConfigureBO> promoFilter, promoStateFilter;

    private PromotionHelper(Context context) {
        businessModel = (BusinessModel) context.getApplicationContext();

    }

    public static PromotionHelper getInstance(Context context) {
        if (instance == null) {
            instance = new PromotionHelper(context);
        }
        return instance;
    }

    public void clearInstance() {
        instance = null;
    }

    /* load data for promotion */
    public void loadDataForPromotion(Context mContext, String mMenuCode) {
        loadPromotionConfigs(mContext);

        downloadPromoFilterList(mContext);
        downloadPromoStateFilterList();

        if (businessModel.productHelper.getInStoreLocation().size() == 0) {
            businessModel.productHelper.downloadInStoreLocations();
        }

        /*businessModel.productHelper.downloadFiveLevelFilterNonProducts(mMenuCode);*/
        businessModel.productHelper.setFilterProductLevelsRex(businessModel.productHelper.downloadFilterLevel(mMenuCode));
        businessModel.productHelper.setFilterProductsByLevelIdRex(businessModel.productHelper.downloadFilterLevelProducts(
                businessModel.productHelper.getRetailerModuleSequenceValues(), false));

        downloadPromotionMaster(mContext);
        loadPromoEntered(mContext);
    }


    /* Load promotion related configs */
    private void loadPromotionConfigs(Context mContext) {
        try {
            SHOW_PROMO_TYPE = false;
            SHOW_PROMO_RATING = false;
            SHOW_PROMO_REASON = false;
            SHOW_PROMO_PHOTO = false;
            SHOW_PROMO_QTY = false;
            SHOW_PROMO_ANNOUNCER = false;
            SHOW_PROMO_FEEDBACK = false;
            SHOW_PROMO_PRICE = false;
            SHOW_PROMO_LOCATION = false;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.openDataBase();

            String sql = "SELECT RField FROM " + DataMembers.tbl_HhtModuleMaster
                    + " WHERE hhtCode='PROMO01' AND Flag=1 and ForSwitchSeller = 0";

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
                                case "FEEDBACK":
                                    this.SHOW_PROMO_FEEDBACK = true;
                                    break;
                                case "PRICE":
                                    this.SHOW_PROMO_PRICE = true;
                                    break;
                                case "LOCATION":
                                    this.SHOW_PROMO_LOCATION = true;
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


    /**
     * Download Promotion Data based on the menu level from configActivityFilter
     * <p>
     * isAccount - Retailer Account level
     * isRetailer - Exact Retailer id
     * isClass - Retailer Class Level
     * locationId - The hierarchy of the location level (Which level of location is set in ConfigActivityFiler)
     * channelId - The hierarchy of the channel level (Which level of channel is set in ConfigActivityFilter)
     */
    private void downloadPromotionMaster(Context mContext) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
        try {
            PromotionBO promotionMaster;
            db.openDataBase();
            Cursor c;
            String query = " where PM.AccId in (0," + businessModel.getRetailerMasterBO().getAccountid() + ")"
                    + " and (PM.ChId in(0," + businessModel.getRetailerMasterBO().getSubchannelid() + ") OR PM.Chid in(0," + businessModel.channelMasterHelper.getChannelHierarchy(businessModel.getRetailerMasterBO().getSubchannelid(), mContext) + "))"
                    + " and PM.retailerid in (0," + businessModel.getRetailerMasterBO().getRetailerID() + ")"
                    + " and PM.ClassId in (0," + businessModel.getRetailerMasterBO().getClassid() + ")"
                    + " and (PM.LocId in (0," + businessModel.getRetailerMasterBO().getLocationId() + ") OR PM.LocId in(0," + businessModel.channelMasterHelper.getLocationHierarchy(mContext) + "))"
                    + " GROUP BY PM.RetailerId,PM.AccId,PM.ChId,PM.LocId,PM.ClassId,PPM.PromoId,PPM.Pid,PPM.GroupName ORDER BY PM.RetailerId,PM.AccId,PM.ChId,PM.LocId,PM.ClassId,PPM.GroupName ";


            c = db.selectSQL("select DISTINCT PPM.PromoId,PPM.PId,PPM.PromoName,PM.MappingId,SLM.listname,P.PName,PMM.StartDate,PMM.EndDate,P.ParentHierarchy,PPM.TargetPrice,PPM.GroupName"
                    + "  from PromotionMapping PM"
                    + " inner join PromotionMaster PMM on PM.HId = PMM.HId and " + QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL))
                    + " between PMM.StartDate and PMM.EndDate inner join PromotionProductMapping PPM on PPM.PromoId=PM.PromoId"
                    + " left join standardlistmaster SLM on SLM.listid=PPm.PromoTypeLovId "
                    + " left join ProductMaster P on PPM.PId =  P.PID "
                    + " left join ProductLevel PL on P.PLid = PL.LevelId " + query);


            if (c != null) {
                mPromotionList = new ArrayList<>();
                while (c.moveToNext()) {
                    promotionMaster = new PromotionBO();
                    promotionMaster.setPromoId(c.getInt(0));
                    promotionMaster.setProductId(c.getInt(1));
                    promotionMaster.setPromoName(c.getString(2));
                    promotionMaster.setMappingId(c.getInt(3));
                    promotionMaster.setGroupName(c.getString(4));
                    promotionMaster.setpName(c.getString(5));
                    promotionMaster.setFromDate(c.getString(6));
                    promotionMaster.setToDate(c.getString(7));
                    promotionMaster.setParentHierarchy(c.getString(8));
                    promotionMaster.setProductPrice(c.getString(9));
                    promotionMaster.setAccepted(true);
                    promotionMaster.setPromotionGroupName(c.getString(10));
                    getPromotionList().add(promotionMaster);
                }
                c.close();
            }

            //Method to download NonAccepted Promotions
            downloadNonAcceptedPromotions(mContext);

            if (mPromotionList != null) {
                for (StandardListBO standardListBO : businessModel.productHelper.getInStoreLocation()) {
                    ArrayList<PromotionBO> clonedList = new ArrayList<>(mPromotionList.size());
                    for (PromotionBO promotionBO : mPromotionList) {
                        clonedList.add(new PromotionBO(promotionBO));
                    }
                    standardListBO.setPromotionTrackingList(clonedList);
                }
            }
            db.closeDB();

        } catch (Exception e) {
            db.closeDB();
            Commons.printException("" + e);
        }
    }

    private void downloadNonAcceptedPromotions(Context mContext) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
        try {
            PromotionBO promotionMaster;
            db.openDataBase();

            //To Retrieve Non Accepted Promotions
            Cursor c = db.selectSQL("select DISTINCT PPM.PromoId,PPM.PId,PPM.PromoName,PM.MappingId,SLM.listname,P.PName,PMM.StartDate,PMM.EndDate,P.ParentHierarchy,PPM.TargetPrice, PPM.GroupName"
                    + " from PromotionMappingApproval PM"
                    + " inner join PromotionMaster PMM on PM.HId = PMM.HId and " + QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL))
                    + " between PMM.StartDate and PMM.EndDate inner join PromotionProductMapping PPM on PPM.PromoId=PM.PromoId"
                    + " left join standardlistmaster SLM on SLM.listid=PPm.PromoTypeLovId "
                    + " left join ProductMaster P on PPM.PId =  P.PID "
                    + " left join ProductLevel PL on P.PLid = PL.LevelId where PM.retailerid in (0," + businessModel.getRetailerMasterBO().getRetailerID() + ")"
                    + " GROUP BY PM.RetailerId ORDER BY PM.RetailerId");
            if (c != null) {
                while (c.moveToNext()) {
                    promotionMaster = new PromotionBO();
                    promotionMaster.setPromoId(c.getInt(0));
                    promotionMaster.setProductId(c.getInt(1));
                    promotionMaster.setPromoName(c.getString(2));
                    promotionMaster.setMappingId(c.getInt(3));
                    promotionMaster.setGroupName(c.getString(4));
                    promotionMaster.setpName(c.getString(5));
                    promotionMaster.setFromDate(c.getString(6));
                    promotionMaster.setToDate(c.getString(7));
                    promotionMaster.setParentHierarchy(c.getString(8));
                    promotionMaster.setProductPrice(c.getString(9));
                    promotionMaster.setAccepted(false);
                    promotionMaster.setPromotionGroupName(c.getString(10));
                    getPromotionList().add(promotionMaster);
                    setNonAcceptedPromotionsAvailable(true);
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
    void savePromotionDetails(Context mContext) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
        StringBuilder sbuffer = new StringBuilder();
        String headerColumns = "UiD,Date,RetailerId,Remark,distributorid,ridSF,VisitId";
        String detailColumns = "Uid,PromotionId,BrandId,IsExecuted,RetailerId,ImageName,reasonid,flag,MappingId,Locid,ExecRatingLovId,PromoQty,imgName,HasAnnouncer,fromDate,toDate,remarks";
        try {
            db.openDataBase();
            String uid = businessModel.getAppDataProvider().getUser().getUserid() + DateTimeUtils
                    .now(DateTimeUtils.DATE_TIME_ID);

            Cursor cursor = db
                    .selectSQL("select Uid from PromotionHeader  Where RetailerId="
                            + QT(businessModel.getAppDataProvider().getRetailMaster().getRetailerID())
                            + " and Date= "
                            + QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL))
                            + " and upload='N'");

            if (cursor.getCount() > 0) {
                cursor.moveToNext();
                db.deleteSQL("PromotionHeader",
                        "Uid=" + QT(cursor.getString(0)), false);
                db.deleteSQL("PromotionDetail",
                        "Uid=" + QT(cursor.getString(0)), false);
                uid = cursor.getString(0);
            }
            cursor.close();

            double productWeightage, sum = 0;

            sbuffer.append(QT(uid));
            sbuffer.append(",");
            sbuffer.append(QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)));
            sbuffer.append(",");
            sbuffer.append(businessModel.getAppDataProvider().getRetailMaster().getRetailerID());
            sbuffer.append(",");
            sbuffer.append(QT(businessModel.getNote()));
            sbuffer.append(",");
            sbuffer.append(businessModel.getAppDataProvider().getRetailMaster().getDistributorId());
            sbuffer.append(",");
            sbuffer.append(QT(businessModel.getAppDataProvider().getRetailMaster().getRidSF()));
            sbuffer.append(",");
            sbuffer.append(businessModel.getAppDataProvider().getUniqueId());


            db.insertSQL("PromotionHeader", headerColumns, sbuffer.toString());

            if (businessModel.configurationMasterHelper.IS_FITSCORE_NEEDED) {
                detailColumns = detailColumns + ",Score";
            }

            for (StandardListBO standardListBO : businessModel.productHelper.getInStoreLocation()) {
                ArrayList<PromotionBO> promotionList = standardListBO.getPromotionTrackingList();
                if (promotionList != null) {
                    productWeightage = (double) 100 / (double) promotionList.size();
                    for (PromotionBO promotion : promotionList) {

                        if (promotion.getIsExecuted() == 1 || !"0".equals(promotion.getReasonID()) || (promotion.getRatingId() != null && !"0".equals(promotion.getRatingId()))) {
                            String fromDate = DateTimeUtils.convertToServerDateFormat(promotion.getFromDate(), "yyyy/MM/dd");
                            String toDate = DateTimeUtils.convertToServerDateFormat(promotion.getToDate(), "yyyy/MM/dd");
                            String sbDetails = QT(uid) +
                                    "," + promotion.getPromoId() +
                                    "," + promotion.getProductId() +
                                    "," + promotion.getIsExecuted() +
                                    "," + businessModel.getAppDataProvider().getRetailMaster().getRetailerID() +
                                    "," + QT(promotion.getImagePath()) +
                                    "," + promotion.getReasonID() +
                                    "," + QT(promotion.getFlag()) +
                                    "," + promotion.getMappingId() +
                                    "," + standardListBO.getListID() +
                                    "," + promotion.getRatingId() +
                                    "," + promotion.getPromoQty() +
                                    "," + QT(promotion.getImageName()) +
                                    "," + promotion.getHasAnnouncer() +
                                    "," + QT(fromDate == null ? "" : fromDate) +
                                    "," + QT(toDate == null ? "" : toDate) +
                                    "," + QT(promotion.getRemarks());

                            if (businessModel.configurationMasterHelper.IS_FITSCORE_NEEDED) {
                                sbDetails = sbDetails + "," + ((promotion.getPromoQty() > 0 || promotion.getIsExecuted() > 0) ? productWeightage : "0");
                                if (promotion.getPromoQty() > 0 || promotion.getIsExecuted() > 0)
                                    sum = sum + productWeightage;
                            }

                            db.insertSQL("PromotionDetail", detailColumns,
                                    sbDetails);
                        }
                    }
                }
            }

            if (businessModel.configurationMasterHelper.IS_FITSCORE_NEEDED) {
                businessModel.calculateFitscoreandInsert(db, sum, DataMembers.FIT_PROMO);
            }

            db.closeDB();
        } catch (Exception e) {
            db.closeDB();
            Commons.printException("Saving Promo Check exception", e);
        }

    }

    /**
     * Get values from Tables and set in Objects while going Edit Mode
     */
    private void loadPromoEntered(Context mContext) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
        try {
            db.openDataBase();
            String uid = "";
            businessModel.setNote("");
            String sql = "SELECT Uid,Remark FROM PromotionHeader WHERE RetailerId = "
                    + businessModel.getRetailerMasterBO().getRetailerID()
                    + " AND Date = "
                    + QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL))
                    + " and upload='N'";

            Cursor cursor = db.selectSQL(sql);

            if (cursor != null) {
                if (cursor.moveToNext()) {
                    uid = cursor.getString(0);
                    businessModel.setNote(cursor.getString(1));
                }
            } else
                return;

            cursor.close();

            String sql1 = "SELECT PromotionId, IsExecuted,imgName,reasonid,brandid,locid,ExecRatingLovId,promoqty,HasAnnouncer,fromDate,toDate,remarks FROM PromotionDetail WHERE Uid="
                    + QT(uid) + " and Upload ='N' and Flag = 'S'";

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
                        String fromDate = orderDetailCursor.getString(9);
                        String toDate = orderDetailCursor.getString(10);
                        String remarks = orderDetailCursor.getString(11);

                        setPromoCheckDetails(promotionID, isExecuted, isAnnounced, imgName,
                                reasonID, brandID, locid, execRatingLovid, promoQty, fromDate, toDate, remarks);
                    }
                    orderDetailCursor.close();
                } else {
                    // Loading Last visit transaction data
                    if (businessModel.configurationMasterHelper.IS_PROMOTION_RETAIN_LAST_VISIT_TRAN) {
                        sql1 = "SELECT PromotionId, IsExecuted,reasonid,locid,ExecRatingLovId,promoqty,fromDate,toDate,Remarks FROM LastVisitPromotion WHERE retailerId="
                                + businessModel.getRetailerMasterBO().getRetailerID() + " and Flag = 'S'";
                        orderDetailCursor = db.selectSQL(sql1);
                        if (orderDetailCursor != null) {
                            while (orderDetailCursor.moveToNext()) {
                                int promotionID = orderDetailCursor.getInt(0);
                                int isExecuted = orderDetailCursor.getInt(1);
                                String reasonID = orderDetailCursor.getString(2);
                                int locid = orderDetailCursor.getInt(3);
                                int execRatingLovid = orderDetailCursor.getInt(4);
                                int promoQty = orderDetailCursor.getInt(5);
                                String fromDate = orderDetailCursor.getString(6);
                                String toDate = orderDetailCursor.getString(7);
                                String remarks = orderDetailCursor.getString(8);

                                setLastVisitPromoCheckDetails(promotionID, isExecuted,
                                        reasonID, locid, execRatingLovid, promoQty, fromDate, toDate, remarks);
                            }
                            orderDetailCursor.close();
                        }
                    }
                }
            }

            sql1 = "SELECT PD.PromotionId, PD.IsExecuted,pd.ImageName,PD.reasonid,PD.brandid,pm.PromoName,pd.ExecRatingLovId,PD.HasAnnouncer,PD.fromDate,PD.toDate,P.ParentHierarchy,PD.remarks,PM.TargetPrice FROM PromotionDetail pd"
                    + " inner join PromotionProductMapping  pm on pm.PromoId = pd.PromotionId"
                    + " left join ProductMaster P on P.PID = PD.brandid"
                    + " WHERE Uid="
                    + QT(uid)
                    + " and Upload ='N' and Flag = 'I'";

            orderDetailCursor = db.selectSQL(sql1);

            if (orderDetailCursor != null) {
                while (orderDetailCursor.moveToNext()) {
                    PromotionBO promotionMaster;
                    promotionMaster = new PromotionBO();
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
                    promotionMaster.setFromDate(orderDetailCursor.getString(8));
                    promotionMaster.setToDate(orderDetailCursor.getString(9));
                    promotionMaster.setParentHierarchy(orderDetailCursor.getString(10));
                    promotionMaster.setRemarks(orderDetailCursor.getString(11));
                    promotionMaster.setProductPrice(orderDetailCursor.getString(12));

                    getPromotionList().add(promotionMaster);

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
    private void setPromoCheckDetails(int promotionID, int isExecuted, int isAnnounced,
                                      String imgName, String reasonId, int brandID, int locationId, int executeLovId, int promoQty, String fromDate, String toDate, String remarks) {

        for (StandardListBO standardListBO : businessModel.productHelper.getInStoreLocation()) {
            if (standardListBO.getListID().equals(Integer.toString(locationId))) {
                ArrayList<PromotionBO> promotionList = standardListBO.getPromotionTrackingList();
                if (promotionList != null) {
                    for (PromotionBO promo : promotionList) {
                        if (promo.getPromoId() == promotionID
                                && promo.getProductId() == brandID) {
                            promo.setIsExecuted(isExecuted);
                            promo.setHasAnnouncer(isAnnounced);
                            promo.setImageName(imgName);
                            promo.setReasonID(reasonId);
                            promo.setRatingId(Integer.toString(executeLovId));
                            promo.setPromoQty(promoQty);
                            promo.setFromDate(fromDate);
                            promo.setToDate(toDate);
                            promo.setRemarks(remarks);
                            break;
                        }

                    }
                }
                break;
            }
        }

    }

    // set the Last tran value in the PromotionMasterBo
    private void setLastVisitPromoCheckDetails(int promotionID, int isExecuted,
                                               String reasonId, int locationId, int executeLovId, int promoQty, String fromDate, String toDate, String remarks) {

        for (StandardListBO standardListBO : businessModel.productHelper.getInStoreLocation()) {
            if (standardListBO.getListID().equals(Integer.toString(locationId))) {
                ArrayList<PromotionBO> promotionList = standardListBO.getPromotionTrackingList();
                if (promotionList != null) {
                    for (PromotionBO promo : promotionList) {
                        if (promo.getPromoId() == promotionID) {
                            promo.setIsExecuted(isExecuted);
                            promo.setImageName("");
                            promo.setReasonID(reasonId);
                            promo.setRatingId(Integer.toString(executeLovId));
                            promo.setPromoQty(promoQty);
                            promo.setFromDate(fromDate);
                            promo.setToDate(toDate);
                            promo.setRemarks(remarks);
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
     * @param mPromoID promotion id
     * @param imgName  image name
     */
    void onSaveImageName(String locationId, int mPromoID, String imgName, String imagePath,int productId) {
        try {
            for (StandardListBO standardListBO : businessModel.productHelper.getInStoreLocation()) {
                if (locationId.equals(standardListBO.getListID())) {
                    ArrayList<PromotionBO> promotionList = standardListBO.getPromotionTrackingList();

                    if (promotionList != null) {
                        for (PromotionBO promotionBO : promotionList) {

                            if (promotionBO.getPromoId() == mPromoID && promotionBO.getProductId() ==productId) {
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
     * @return true if there is nay data to save
     */
    boolean hasPromoData() {
        for (StandardListBO standardListBO : businessModel.productHelper.getInStoreLocation()) {

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

    /* get list of unused images and loop through it to delete */
    void deleteUnusedImages() {

        for (PromotionBO temp : getPromotionList()) {
            if (temp.getIsExecuted() == 0 && !"".equals(temp.getImageName())) {
                String fileName = temp.getImageName();
                Commons.print("Image Delete," + "Coming In");
                deleteFiles(fileName);
            }
        }
    }

    /* Delete unused images from storage */
    private void deleteFiles(String filename) {
        File folder = new File(FileUtils.photoFolderPath + "/");

        File[] files = folder.listFiles();
        for (File tempFile : files) {
            if (tempFile != null && tempFile.getName().equals(filename)) {
                boolean isDeleted = tempFile.delete();
                if (isDeleted)
                    Commons.print("Image Delete," + "Success");
            }
        }
    }

    /* get promotion rating list from StandardListMaster */
    void downloadPromotionRating(Context mContext) {

        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
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
                standardListBO.setListName(mContext.getString(R.string.plain_select));
                mRatingList.add(0, standardListBO);
            }
            c.close();
            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    ArrayList<StandardListBO> getRatingList() {
        return mRatingList;
    }

    public ArrayList<PromotionBO> getPromotionList() {
        if (mPromotionList == null)
            return new ArrayList<PromotionBO>();
        return mPromotionList;
    }

    public Vector<ConfigureBO> getPromoFilter() {
        return promoFilter;
    }

    public boolean isNonAcceptedPromotionsAvailable() {
        return isNonAcceptedPromotionsAvailable;
    }

    public void setNonAcceptedPromotionsAvailable(boolean nonAcceptedPromotionsAvailable) {
        isNonAcceptedPromotionsAvailable = nonAcceptedPromotionsAvailable;
    }

    public void setPromoFilter(Vector<ConfigureBO> promoFilter) {
        this.promoFilter = promoFilter;
    }

    public Vector<ConfigureBO> getPromoStateFilter() {
        return promoStateFilter;
    }

    public void setPromoStateFilter(Vector<ConfigureBO> promoStateFilter) {
        this.promoStateFilter = promoStateFilter;
    }

    public Vector<ConfigureBO> downloadPromoFilterList(Context mContext) {
        setPromoFilter(new Vector<>());

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String language = sharedPrefs.getString("languagePref",
                ApplicationConfigs.LANGUAGE);

        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.openDataBase();

            String sql = "select DISTINCT GroupName from PromotionProductMapping";

            Cursor c = db.selectSQL(sql);
            int cnt = 0;
            ConfigureBO con;
            if (c != null) {
                while (c.moveToNext()) {
                    cnt++;
                    con = new ConfigureBO();
                    con.setConfigCode(c.getString(0));
                    con.setMenuName(c.getString(0));
                    con.setMenuNumber(cnt + "");
                    con.setHasLink(1);
                    con.setMandatory(0);
                    getPromoFilter().add(con);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return getPromoFilter();
    }

    public Vector<ConfigureBO> downloadPromoStateFilterList() {
        setPromoStateFilter(new Vector<>());

        ConfigureBO con = new ConfigureBO();
        con.setConfigCode("A");
        con.setMenuName("Accepted");
        con.setMenuNumber("1");
        con.setHasLink(1);
        con.setMandatory(0);
        getPromoStateFilter().add(con);

        con = new ConfigureBO();
        con.setConfigCode("NA");
        con.setMenuName("NonAccepted");
        con.setMenuNumber("2");
        con.setHasLink(1);
        con.setMandatory(0);
        getPromoStateFilter().add(con);

        return getPromoStateFilter();
    }
}
