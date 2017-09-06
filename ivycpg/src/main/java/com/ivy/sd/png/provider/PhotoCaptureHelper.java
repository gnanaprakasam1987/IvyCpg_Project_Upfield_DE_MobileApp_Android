package com.ivy.sd.png.provider;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.LocationBO;
import com.ivy.sd.png.bo.PhotoCaptureProductBO;
import com.ivy.sd.png.bo.PhotoTypeMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.DateUtil;

import java.util.ArrayList;

/**
 * Created by abbasaniefa on 11/07/17.
 */

public class PhotoCaptureHelper {

    private final Context context;
    private final BusinessModel bmodel;
    private static PhotoCaptureHelper instance = null;
    private ArrayList<PhotoCaptureProductBO> photoCaptureProductList;
    private ArrayList<PhotoTypeMasterBO> photoTypeMaster;

    private PhotoCaptureHelper(Context context) {
        this.context = context;
        bmodel = (BusinessModel) context;
    }

    public static PhotoCaptureHelper getInstance(Context context) {
        if (instance == null) {
            instance = new PhotoCaptureHelper(context);
        }
        return instance;
    }

    /**
     * Download the products for photo capture. Level will be taken from ProductFilter1 column of ConfigActivityFilter.
     */
    public void downloadPhotoCaptureProducts() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
        try {
            db.openDataBase();
            PhotoCaptureProductBO phCapture;
            String query = "SELECT DISTINCT PID, PName, ParentId FROM ProductMaster WHERE Plid IN (SELECT ProductFilter1 FROM ConfigActivityFilter  WHERE ActivityCode = 'MENU_PHOTO') ORDER BY PID";
            Cursor c = db.selectSQL(query);

            if (c != null) {
                setPhotoCaptureProductList(new ArrayList<PhotoCaptureProductBO>());
                while (c.moveToNext()) {
                    phCapture = new PhotoCaptureProductBO();
                    phCapture.setProductID(c.getInt(0));
                    phCapture.setProductName(c.getString(1));
                    getPhotoCaptureProductList().add(phCapture);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
        }
    }

    public ArrayList<PhotoCaptureProductBO> getPhotoCaptureProductList() {
        return photoCaptureProductList;
    }

    private void setPhotoCaptureProductList(ArrayList<PhotoCaptureProductBO> photoCaptureProductList) {
        this.photoCaptureProductList = photoCaptureProductList;
    }


    /**
     * Download the photo types from StandardListmaster of type PHOTO_TYPE
     */
    public void downloadPhotoTypeMaster() {
        try {
            PhotoTypeMasterBO typeMasterBO;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db.selectSQL("SELECT ListId, ListName, ListCode FROM StandardListMaster WHERE ListType = 'PHOTO_TYPE'");
            photoTypeMaster = new ArrayList<>();
            typeMasterBO = new PhotoTypeMasterBO();
            typeMasterBO.setPhotoTypeId(0);
            typeMasterBO.setPhotoTypeDesc("--Select PhotoType--");
            typeMasterBO.setPhotoTypeCode("--Select PhotoType--");
            typeMasterBO.setPhotoCaptureProductList(cloneLocationList(getPhotoCaptureProductList()));

            for (PhotoCaptureProductBO photoCaptureBO : typeMasterBO.getPhotoCaptureProductList()) {

                if (bmodel.productHelper.locations != null)
                    photoCaptureBO.setInStoreLocations(ProductHelper.cloneLocationList(bmodel.productHelper.locations));
                if (photoCaptureBO.getInStoreLocations() != null)
                    for (LocationBO lbo : photoCaptureBO.getInStoreLocations()) {
                        lbo.setProductID(photoCaptureBO.getProductID());
                        lbo.setProductName(photoCaptureBO.getProductName());
                    }
            }
            photoTypeMaster.add(typeMasterBO);
            if (c != null) {

                while (c.moveToNext()) {
                    typeMasterBO = new PhotoTypeMasterBO();
                    typeMasterBO.setPhotoTypeId(c.getInt(0));
                    typeMasterBO.setPhotoTypeDesc(c.getString(1));
                    typeMasterBO.setPhotoTypeCode(c.getString(2));

                    typeMasterBO
                            .setPhotoCaptureProductList(cloneLocationList(getPhotoCaptureProductList()));
                    for (PhotoCaptureProductBO photoCaptureBO : typeMasterBO.getPhotoCaptureProductList()) {

                        photoCaptureBO.setInStoreLocations(ProductHelper.cloneLocationList(bmodel.productHelper.locations));
                        for (LocationBO lbo : photoCaptureBO.getInStoreLocations()) {
                            lbo.setProductID(photoCaptureBO.getProductID());
                            lbo.setProductName(photoCaptureBO.getProductName());
                        }
                    }
                    photoTypeMaster.add(typeMasterBO);
                }
                c.close();
            }
            db.closeDB();


        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    /**
     * Duplicate the Product List
     *
     * @param list list
     * @return return
     */
    private static ArrayList<PhotoCaptureProductBO> cloneLocationList(
            ArrayList<PhotoCaptureProductBO> list) {
        ArrayList<PhotoCaptureProductBO> clone = new ArrayList<>(
                list.size());
        for (PhotoCaptureProductBO item : list)
            clone.add(new PhotoCaptureProductBO(item));
        return clone;
    }

    /**
     * Save the photo capture details Delete the existing photo details for the
     * retailer
     *
     * @param retailerID retailerID
     */
    public void savePhotocaptureDetails(String retailerID) {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
        Cursor cursor;
        try {
            String columns = "uid,date,phototypeid,pid,imagepath,retailerid,RetailerName,ImageCount4Retailer,FromDate,ToDate,LocId," +
                    "sku_name,abv,lot_code,seq_num,DistributorID,feedback,imgName";
            db.createDataBase();
            db.openDataBase();
            // delete transaction if exist
            cursor = db.selectSQL("SELECT Uid FROM "
                    + DataMembers.actPhotocapture + " WHERE RetailerId = "
                    + bmodel.getRetailerMasterBO().getRetailerID()
                    + " AND DistributorID="
                    + bmodel.getRetailerMasterBO().getDistributorId()
                    + " AND Date = "
                    + bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL)));

            if (cursor.getCount() > 0) {
                cursor.moveToNext();
                db.deleteSQL(DataMembers.actPhotocapture,
                        "Uid=" + bmodel.QT(cursor.getString(0)), false);
                cursor.close();
            }

            String uid = QT(bmodel.userMasterHelper.getUserMasterBO()
                    .getDistributorid()
                    + ""
                    + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                    + "" + SDUtil.now(SDUtil.DATE_TIME_ID));

            for (PhotoTypeMasterBO photoTypeBo : getPhotoTypeMaster()) {
                ArrayList<PhotoCaptureProductBO> tempPhotoBo = photoTypeBo
                        .getPhotoCaptureProductList();
                for (PhotoCaptureProductBO phcapture : tempPhotoBo) {
                    for (LocationBO lbo : phcapture.getInStoreLocations())
                        if (!"".equals(lbo.getImagepath())) {

                            StringBuilder sBuffer = new StringBuilder();

                            sBuffer.append(uid);
                            sBuffer.append(",");
                            sBuffer.append(QT(SDUtil.now(SDUtil.DATE_GLOBAL)));
                            sBuffer.append(",");
                            sBuffer.append(photoTypeBo.getPhotoTypeId());
                            sBuffer.append(",");
                            sBuffer.append(lbo.getProductID());
                            sBuffer.append(",");
                            sBuffer.append(QT(lbo.getImagepath()));
                            sBuffer.append(",");
                            sBuffer.append(retailerID);
                            sBuffer.append(",");
                            sBuffer.append(DatabaseUtils
                                    .sqlEscapeString(bmodel.retailerMasterBO
                                            .getRetailerName()));
                            sBuffer.append(",");
                            sBuffer.append("1");
                            sBuffer.append(",");
                            if (bmodel.configurationMasterHelper.SHOW_DATE_BTN) {
                                sBuffer.append(QT(DateUtil.convertToServerDateFormat(
                                        lbo.getFromDate(),
                                        ConfigurationMasterHelper.outDateFormat)));
                                sBuffer.append(",");
                                sBuffer.append(QT(DateUtil.convertToServerDateFormat(
                                        lbo.getToDate(),
                                        ConfigurationMasterHelper.outDateFormat)));
                                sBuffer.append(",");
                            } else {
                                sBuffer.append(QT(""));
                                sBuffer.append(",");
                                sBuffer.append(QT(""));
                                sBuffer.append(",");
                            }
                            sBuffer.append(lbo.getLocationId());
                            sBuffer.append(",");
                            sBuffer.append(QT(lbo.getSkuname()));
                            sBuffer.append(",");
                            sBuffer.append(QT(lbo.getAbv()));
                            sBuffer.append(",");
                            sBuffer.append(QT(lbo.getLotcode()));
                            sBuffer.append(",");
                            sBuffer.append(QT(lbo.getSeqno()));
                            sBuffer.append(",");
                            sBuffer.append(bmodel.retailerMasterBO
                                    .getDistributorId());
                            sBuffer.append(",");
                            sBuffer.append(QT(lbo.getFeedback()));
                            sBuffer.append(",");
                            sBuffer.append(QT(lbo.getImageName()));
                            db.insertSQL(DataMembers.actPhotocapture, columns,
                                    sBuffer.toString());
                        }
                }

            }
            db.closeDB();
        } catch (Exception e) {
            db.closeDB();
            Commons.printException("" + e);
        }
    }

    /**
     *
     */
    public boolean hasPhotoTaken(int productId, int typeId) {
        try {

            for (PhotoTypeMasterBO photoTypeBo :
                    getPhotoTypeMaster()) {
                ArrayList<PhotoCaptureProductBO> tempPhotoBo = photoTypeBo
                        .getPhotoCaptureProductList();
                if (photoTypeBo.getPhotoTypeId() == typeId)
                    for (PhotoCaptureProductBO phcapture : tempPhotoBo) {
                        if (phcapture.getProductID() == productId)
                            for (LocationBO lbo : phcapture.getInStoreLocations())
                                if (!"".equals(lbo.getImagepath())) {
                                    return true;

                                }
                    }
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return false;

    }

    /**
     * Load the photo capture details in Edit mode to the particular retailerID
     *
     * @param retailerID retailerID
     */
    public void loadPhotoCaptureDetailsInEditMode(String retailerID) {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
        Cursor cursor;
        try {
            db.openDataBase();
            String sql1 = "SELECT phototypeid,pid,imagepath,FromDate,ToDate,LocId,sku_name,abv,lot_code,seq_num,feedback,imgName FROM Photocapture WHERE RetailerID="
                    + retailerID + " And DistributorID=" + bmodel.getRetailerMasterBO().getDistributorId();
            cursor = db.selectSQL(sql1);

            if (cursor != null) {
                while (cursor.moveToNext()) {

                    for (PhotoTypeMasterBO tempTypeBO : getPhotoTypeMaster()) {
                        ArrayList<PhotoCaptureProductBO> tempCaptureBO = tempTypeBO
                                .getPhotoCaptureProductList();
                        for (PhotoCaptureProductBO photo : tempCaptureBO) {
                            for (LocationBO lbo : photo.getInStoreLocations())
                                if (lbo.getProductID() == cursor.getInt(1)
                                        && tempTypeBO.getPhotoTypeId() == cursor
                                        .getInt(0) && lbo.getLocationId() == cursor.getInt(5)) {
                                    lbo.setImagepath(cursor.getString(2));
                                    lbo.setFromDate(DateUtil.convertFromServerDateToRequestedFormat(
                                            cursor.getString(3),
                                            ConfigurationMasterHelper.outDateFormat));
                                    lbo.setToDate(DateUtil.convertFromServerDateToRequestedFormat(
                                            cursor.getString(4),
                                            ConfigurationMasterHelper.outDateFormat));
                                    lbo.setSkuname(cursor.getString(6));
                                    lbo.setAbv(cursor.getString(7));
                                    lbo.setLotcode(cursor.getString(8));
                                    lbo.setSeqno(cursor.getString(9));
                                    lbo.setFeedback(cursor.getString(10));
                                    lbo.setImageName(cursor.getString(11));
                                    break;
                                }
                        } // End of Capture BO
                    } // End of TypeBO

                }
                cursor.close();
            }

            db.closeDB();
        } catch (Exception e) {
            db.closeDB();
            Commons.printException("" + e);
        }
    }

    public ArrayList<PhotoTypeMasterBO> getPhotoTypeMaster() {
        return photoTypeMaster;
    }

    private String QT(String data) // Quote
    {
        return "'" + data + "'";
    }

}
