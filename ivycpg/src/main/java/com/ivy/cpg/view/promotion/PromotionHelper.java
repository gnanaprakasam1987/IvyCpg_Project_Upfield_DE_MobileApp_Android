package com.ivy.cpg.view.promotion;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.HomeScreenFragment;

import java.io.File;
import java.util.ArrayList;

import static com.ivy.lib.Utils.QT;

public class PromotionHelper {

    private final Context context;
    private final BusinessModel businessModel;
    private static PromotionHelper instance = null;
    int mSelectedPromoID = 0;
    private ArrayList<PromotionBO> mPromotionList;
    private ArrayList<StandardListBO> mRatingList;
    boolean SHOW_PROMO_TYPE;
    boolean SHOW_PROMO_RATING;
    boolean SHOW_PROMO_REASON;
    boolean SHOW_PROMO_PHOTO;
    boolean SHOW_PROMO_QTY;
    boolean SHOW_PROMO_ANNOUNCER;

    private PromotionHelper(Context context) {
        this.context = context;
        businessModel = (BusinessModel) context.getApplicationContext();

    }

    public static PromotionHelper getInstance(Context context) {
        if (instance == null) {
            instance = new PromotionHelper(context);
        }
        return instance;
    }

    /* load data for promotion */
    public void loadDataForPromotion(String mMenuCode) {
        loadPromotionConfigs();

        if (businessModel.productHelper.getInStoreLocation().size() == 0) {
            businessModel.productHelper.downloadInStoreLocations();
        }

        if (businessModel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER)
            businessModel.productHelper.downloadFiveLevelFilterNonProducts(mMenuCode);

        businessModel.productHelper.getRetailerlevel(mMenuCode);
        downloadPromotionMaster();
        loadPromoEntered();
    }


    /* Load promotion related configs */
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


    /**
     * Download Promotion Data based on the menu level from configActivityFilter
     * <p>
     * isAccount - Retailer Account level
     * isRetailer - Exact Retailer id
     * isClass - Retailer Class Level
     * locationId - The hierarchy of the location level (Which level of location is set in ConfigActivityFiler)
     * channelId - The hierarchy of the channel level (Which level of channel is set in ConfigActivityFilter)
     */
    private void downloadPromotionMaster() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
        try {
            PromotionBO promotionMaster;
            db.openDataBase();
            Cursor c;
            String query = " where PM.AccId in (0," + businessModel.getRetailerMasterBO().getAccountid() + ")"
                    + " and (PM.ChId in(0," + businessModel.getRetailerMasterBO().getSubchannelid() + ") OR PM.Chid in(0," + businessModel.schemeDetailsMasterHelper.getChannelidForScheme(businessModel.getRetailerMasterBO().getSubchannelid()) + "))"
                    + " and PM.retailerid in (0," + businessModel.getRetailerMasterBO().getRetailerID() + ")"
                    + " and PM.ClassId in (0," + businessModel.getRetailerMasterBO().getClassid() + ")"
                    + " and (PM.LocId in (0," + businessModel.getRetailerMasterBO().getLocationId() + ") OR PM.LocId in(0," + businessModel.schemeDetailsMasterHelper.getLocationIdsForScheme() + "))"
                    + " GROUP BY PM.RetailerId,PM.AccId,PM.ChId,PM.LocId,PM.ClassId,PPM.Pid ORDER BY PM.RetailerId,PM.AccId,PM.ChId,PM.LocId,PM.ClassId ";


            if (businessModel.configurationMasterHelper.IS_GLOBAL_CATEGORY)
                query = query + "and PPM.PId = " + businessModel.productHelper.getmSelectedGlobalProductId();

            c = db.selectSQL("select DISTINCT PPM.PromoId,PPM.PId,PPM.PromoName,PM.MappingId,SLM.listname"
                    + "  from PromotionMapping PM"
                    + " inner join PromotionMaster PMM on PM.HId = PMM.HId and " + QT(SDUtil.now(SDUtil.DATE_GLOBAL)) +
                    " between PMM.StartDate and PMM.EndDate inner join PromotionProductMapping PPM on PPM.PromoId=PM.PromoId" +
                    " left join standardlistmaster SLM on SLM.listid=PPm.PromoTypeLovId " + query);


            if (c != null) {
                mPromotionList = new ArrayList<>();
                while (c.moveToNext()) {
                    promotionMaster = new PromotionBO();
                    promotionMaster.setPromoId(c.getInt(0));
                    promotionMaster.setProductId(c.getInt(1));
                    promotionMaster.setPromoName(c.getString(2));
                    promotionMaster.setMappingId(c.getInt(3));
                    promotionMaster.setGroupName(c.getString(4));
                    getPromotionList().add(promotionMaster);
                }

                if (mPromotionList != null) {
                    for (StandardListBO standardListBO : businessModel.productHelper.getInStoreLocation()) {

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
    void savePromotionDetails() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
        StringBuilder sbuffer = new StringBuilder();
        String headerColumns = "UiD,Date,RetailerId,Remark,distributorid";
        String detailColumns = "Uid,PromotionId,BrandId,IsExecuted,RetailerId,ImageName,reasonid,flag,MappingId,Locid,ExecRatingLovId,PromoQty,imgName,HasAnnouncer";
        try {
            db.openDataBase();
            String uid = businessModel.userMasterHelper.getUserMasterBO().getUserid() + SDUtil
                    .now(SDUtil.DATE_TIME_ID);

            Cursor cursor = db
                    .selectSQL("select Uid from PromotionHeader  Where RetailerId="
                            + QT(businessModel.getRetailerMasterBO().getRetailerID())
                            + " and Date= "
                            + QT(SDUtil.now(SDUtil.DATE_GLOBAL))
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

            int moduleWeightage = 0;
            double productWeightage, sum = 0;

            sbuffer.append(QT(uid));
            sbuffer.append(",");
            sbuffer.append(QT(SDUtil.now(SDUtil.DATE_GLOBAL)));
            sbuffer.append(",");
            sbuffer.append(businessModel.getRetailerMasterBO().getRetailerID());
            sbuffer.append(",");
            sbuffer.append(QT(businessModel.getNote()));
            sbuffer.append(",");
            sbuffer.append(businessModel.getRetailerMasterBO().getDistributorId());

            if (businessModel.configurationMasterHelper.IS_FITSCORE_NEEDED) {
                headerColumns = headerColumns + ",Weightage,Score";
                moduleWeightage = businessModel.fitscoreHelper.getModuleWeightage(DataMembers.FIT_PROMO);
                sbuffer.append(",");
                sbuffer.append(moduleWeightage);
                sbuffer.append(",0");
            }

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
                            String sbDetails = QT(uid) +
                                    "," + promotion.getPromoId() +
                                    "," + promotion.getProductId() +
                                    "," + promotion.getIsExecuted() +
                                    "," + businessModel.getRetailerMasterBO().getRetailerID() +
                                    "," + QT(promotion.getImagePath()) +
                                    "," + promotion.getReasonID() +
                                    "," + QT(promotion.getFlag()) +
                                    "," + promotion.getMappingId() +
                                    "," + standardListBO.getListID() +
                                    "," + promotion.getRatingId() +
                                    "," + promotion.getPromoQty() +
                                    "," + QT(promotion.getImageName()) +
                                    "," + promotion.getHasAnnouncer();

                            if (businessModel.configurationMasterHelper.IS_FITSCORE_NEEDED) {
                                sbDetails = sbDetails + "," + productWeightage;
                                sum = sum + productWeightage;
                            }

                            db.insertSQL("PromotionDetail", detailColumns,
                                    sbDetails);
                        }
                    }
                }
            }

            if (businessModel.configurationMasterHelper.IS_FITSCORE_NEEDED && sum != 0) {
                double achieved = ((sum / (double) 100) * moduleWeightage);
                db.updateSQL("Update PromotionHeader set Score = " + achieved + " where UID = " + QT(uid) + " and" +
                        " Date = " + QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + "" +
                        " and RetailerID = " + QT(businessModel.getRetailerMasterBO().getRetailerID()));
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
    private void loadPromoEntered() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
        try {
            db.openDataBase();
            String uid = "";
            businessModel.setNote("");
            String sql = "SELECT Uid,Remark FROM PromotionHeader WHERE RetailerId = "
                    + businessModel.getRetailerMasterBO().getRetailerID()
                    + " AND Date = "
                    + QT(SDUtil.now(SDUtil.DATE_GLOBAL))
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

            String sql1 = "SELECT PromotionId, IsExecuted,imgName,reasonid,brandid,locid,ExecRatingLovId,promoqty,HasAnnouncer FROM PromotionDetail WHERE Uid="
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

                        setPromoCheckDetails(promotionID, isExecuted, isAnnounced, imgName,
                                reasonID, brandID, locid, execRatingLovid, promoQty);
                    }
                    orderDetailCursor.close();
                } else {
                    // Loading Last visit transaction data
                    if (businessModel.configurationMasterHelper.IS_PROMOTION_RETAIN_LAST_VISIT_TRAN) {
                        sql1 = "SELECT PromotionId, IsExecuted,reasonid,locid,ExecRatingLovId,promoqty FROM LastVisitPromotion WHERE retailerId="
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

                                setLastVisitPromoCheckDetails(promotionID, isExecuted,
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
                                      String imgName, String reasonId, int brandID, int locationId, int executeLovId, int promoQty) {

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
                                               String reasonId, int locationId, int executeLovId, int promoQty) {

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
    void onSaveImageName(String locationId, int mPromoID, String imgName, String imagePath) {
        try {
            for (StandardListBO standardListBO : businessModel.productHelper.getInStoreLocation()) {
                if (locationId.equals(standardListBO.getListID())) {
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
        File folder = new File(HomeScreenFragment.photoPath + "/");

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
    void downloadPromotionRating() {

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

}
