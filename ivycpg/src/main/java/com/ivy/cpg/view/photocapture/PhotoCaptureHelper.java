package com.ivy.cpg.view.photocapture;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Build;
import androidx.core.content.FileProvider;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.BuildConfig;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.ui.photocapture.model.PhotoCaptureLocationBO;
import com.ivy.ui.photocapture.model.PhotoCaptureProductBO;
import com.ivy.ui.photocapture.model.PhotoTypeMasterBO;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by abbasaniefa on 11/07/17.
 * Photo capture screen specific helper
 */

/**
 * @See {@link com.ivy.ui.photocapture.presenter.PhotoCapturePresenterImpl},{@link com.ivy.ui.photocapture.data.PhotoCaptureDataManagerImpl}
 * @deprecated
 */
public class PhotoCaptureHelper {

    private final BusinessModel mBModel;
    private static PhotoCaptureHelper instance = null;
    private ArrayList<PhotoCaptureProductBO> photoCaptureProductList;
    private ArrayList<PhotoTypeMasterBO> photoTypeMaster;
    private ArrayList<PhotoCaptureLocationBO> inStoreLocation;

    private PhotoCaptureHelper(Context context) {
        mBModel = (BusinessModel) context.getApplicationContext();
    }

    public static PhotoCaptureHelper getInstance(Context context) {
        if (instance == null) {
            instance = new PhotoCaptureHelper(context);
        }
        return instance;
    }

    public void clearInstance() {
        instance = null;
    }

    /**
     * Download the products for photo capture. Level will be taken from ProductFilter1 column of ConfigActivityFilter.
     */
    public void downloadPhotoCaptureProducts(Context mContext) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
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
     * Download the photo types from StandardListMaster of type PHOTO_TYPE
     */
    public void downloadPhotoTypeMaster(Context mContext) {
        try {
            PhotoTypeMasterBO typeMasterBO;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db.selectSQL("SELECT ListId, ListName, ListCode FROM StandardListMaster WHERE ListType = 'PHOTO_TYPE'");
            photoTypeMaster = new ArrayList<>();
            typeMasterBO = new PhotoTypeMasterBO();
            typeMasterBO.setPhotoTypeId(0);
            typeMasterBO.setPhotoTypeDesc(mContext.getResources().getString(R.string.select_photo_type));
            typeMasterBO.setPhotoTypeCode(mContext.getResources().getString(R.string.select_photo_type));
            typeMasterBO.setPhotoCaptureProductList(cloneProductList(getPhotoCaptureProductList()));

            for (PhotoCaptureProductBO photoCaptureBO : typeMasterBO.getPhotoCaptureProductList()) {

                if (inStoreLocation != null)
                    photoCaptureBO.setInStoreLocations(cloneLocationList(inStoreLocation));
                if (photoCaptureBO.getInStoreLocations() != null)
                    for (PhotoCaptureLocationBO lbo : photoCaptureBO.getInStoreLocations()) {
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
                            .setPhotoCaptureProductList(cloneProductList(getPhotoCaptureProductList()));
                    for (PhotoCaptureProductBO photoCaptureBO : typeMasterBO.getPhotoCaptureProductList()) {

                        photoCaptureBO.setInStoreLocations(cloneLocationList(inStoreLocation));
                        for (PhotoCaptureLocationBO lbo : photoCaptureBO.getInStoreLocations()) {
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
    private static ArrayList<PhotoCaptureProductBO> cloneProductList(
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
    public void savePhotoCaptureDetails(Context mContext, String retailerID) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
        Cursor cursor;
        try {
            String columns = "uid,date,phototypeid,pid,imagepath,retailerid,RetailerName,ImageCount4Retailer,FromDate,ToDate,LocId," +
                    "sku_name,abv,lot_code,seq_num,DistributorID,feedback,imgName,ridSF,VisitId";
            db.createDataBase();
            db.openDataBase();
            // delete transaction if exist
            cursor = db.selectSQL("SELECT Uid FROM "
                    + DataMembers.actPhotocapture + " WHERE RetailerId = "
                    + mBModel.getRetailerMasterBO().getRetailerID()
                    + " AND DistributorID="
                    + mBModel.getRetailerMasterBO().getDistributorId()
                    + " AND Date = "
                    + mBModel.QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)));

            if (cursor.getCount() > 0) {
                cursor.moveToNext();
                db.deleteSQL(DataMembers.actPhotocapture,
                        "Uid=" + mBModel.QT(cursor.getString(0)), false);
                cursor.close();
            }

            String uid = QT(mBModel.userMasterHelper.getUserMasterBO()
                    .getDistributorid()
                    + ""
                    + mBModel.userMasterHelper.getUserMasterBO().getUserid()
                    + "" + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID));

            for (PhotoTypeMasterBO photoTypeBo : getPhotoTypeMaster()) {
                ArrayList<PhotoCaptureProductBO> tempPhotoBo = photoTypeBo
                        .getPhotoCaptureProductList();
                for (PhotoCaptureProductBO mPhotoCapture : tempPhotoBo) {
                    for (PhotoCaptureLocationBO lbo : mPhotoCapture.getInStoreLocations())
                        if (!"".equals(lbo.getImagePath())) {

                            StringBuilder sBuffer = new StringBuilder();

                            sBuffer.append(uid);
                            sBuffer.append(",");
                            sBuffer.append(QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)));
                            sBuffer.append(",");
                            sBuffer.append(photoTypeBo.getPhotoTypeId());
                            sBuffer.append(",");
                            sBuffer.append(lbo.getProductID());
                            sBuffer.append(",");
                            sBuffer.append(QT(lbo.getImagePath()));
                            sBuffer.append(",");
                            sBuffer.append(retailerID);
                            sBuffer.append(",");
                            sBuffer.append(DatabaseUtils
                                    .sqlEscapeString(mBModel.retailerMasterBO
                                            .getRetailerName()));
                            sBuffer.append(",");
                            sBuffer.append("1");
                            sBuffer.append(",");
                            if (mBModel.configurationMasterHelper.SHOW_DATE_BTN) {
                                sBuffer.append(QT(DateTimeUtils.convertToServerDateFormat(
                                        lbo.getFromDate(),
                                        ConfigurationMasterHelper.outDateFormat)));
                                sBuffer.append(",");
                                sBuffer.append(QT(DateTimeUtils.convertToServerDateFormat(
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
                            sBuffer.append(QT(lbo.getSKUName()));
                            sBuffer.append(",");
                            sBuffer.append(QT(lbo.getAbv()));
                            sBuffer.append(",");
                            sBuffer.append(QT(lbo.getLotCode()));
                            sBuffer.append(",");
                            sBuffer.append(QT(lbo.getSequenceNO()));
                            sBuffer.append(",");
                            sBuffer.append(mBModel.retailerMasterBO
                                    .getDistributorId());
                            sBuffer.append(",");
                            sBuffer.append(QT(lbo.getFeedback()));
                            sBuffer.append(",");
                            sBuffer.append(QT(lbo.getImageName()));
                            sBuffer.append(",");
                            sBuffer.append(QT(mBModel.getAppDataProvider().getRetailMaster().getRidSF()));
                            sBuffer.append(",");
                            sBuffer.append(mBModel.getAppDataProvider().getUniqueId());
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
     * To check photo taken for given product and photo type
     */
    public boolean hasPhotoTaken(int productId, int typeId) {
        try {

            for (PhotoTypeMasterBO photoTypeBo :
                    getPhotoTypeMaster()) {
                ArrayList<PhotoCaptureProductBO> tempPhotoBo = photoTypeBo
                        .getPhotoCaptureProductList();
                if (photoTypeBo.getPhotoTypeId() == typeId)
                    for (PhotoCaptureProductBO mPhotoCapture : tempPhotoBo) {
                        if (mPhotoCapture.getProductID() == productId)
                            for (PhotoCaptureLocationBO lbo : mPhotoCapture.getInStoreLocations())
                                if (!"".equals(lbo.getImagePath())) {
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
    public void loadPhotoCaptureDetailsInEditMode(Context mContext, String retailerID) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
        Cursor cursor;
        try {
            db.openDataBase();
            String sql1 = "SELECT phototypeid,pid,imagepath,FromDate,ToDate,LocId,sku_name,abv,lot_code,seq_num,feedback,imgName FROM Photocapture WHERE RetailerID="
                    + retailerID + " And DistributorID=" + mBModel.getRetailerMasterBO().getDistributorId() + " and upload = 'N'";
            cursor = db.selectSQL(sql1);

            if (cursor != null) {
                while (cursor.moveToNext()) {

                    for (PhotoTypeMasterBO tempTypeBO : getPhotoTypeMaster()) {
                        ArrayList<PhotoCaptureProductBO> tempCaptureBO = tempTypeBO
                                .getPhotoCaptureProductList();
                        for (PhotoCaptureProductBO photo : tempCaptureBO) {
                            for (PhotoCaptureLocationBO lbo : photo.getInStoreLocations())
                                if (lbo.getProductID() == cursor.getInt(1)
                                        && tempTypeBO.getPhotoTypeId() == cursor
                                        .getInt(0) && lbo.getLocationId() == cursor.getInt(5)) {
                                    lbo.setImagePath(cursor.getString(2));
                                    lbo.setFromDate(DateTimeUtils.convertFromServerDateToRequestedFormat(
                                            cursor.getString(3),
                                            ConfigurationMasterHelper.outDateFormat));
                                    lbo.setToDate(DateTimeUtils.convertFromServerDateToRequestedFormat(
                                            cursor.getString(4),
                                            ConfigurationMasterHelper.outDateFormat));
                                    lbo.setSKUName(cursor.getString(6));
                                    lbo.setAbv(cursor.getString(7));
                                    lbo.setLotCode(cursor.getString(8));
                                    lbo.setSequenceNO(cursor.getString(9));
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


    /**
     * Download locations
     */
    public void downloadLocations(Context mContext) {
        try {

            inStoreLocation = new ArrayList<>();
            PhotoCaptureLocationBO locations;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.openDataBase();

            String sql1 = "SELECT Distinct SL.ListId, SL.ListName"
                    + " FROM StandardListMaster SL  where SL.Listtype='PL' ORDER BY SL.ListId";

            Cursor c = db.selectSQL(sql1);
            if (c != null) {
                while (c.moveToNext()) {
                    locations = new PhotoCaptureLocationBO();
                    locations.setLocationId(c.getInt(0));
                    locations.setLocationName(c.getString(1));
                    inStoreLocation.add(locations);
                }
                c.close();
            }
            db.closeDB();

            if (inStoreLocation.size() == 0) {
                locations = new PhotoCaptureLocationBO();
                locations.setLocationId(0);
                locations.setLocationName("Store");
                inStoreLocation.add(locations);
            }

        } catch (Exception e) {
            Commons.printException("Download Location", e);
        }

    }

    public ArrayList<PhotoCaptureLocationBO> getLocations() {
        if (inStoreLocation == null)
            inStoreLocation = new ArrayList<>();
        return inStoreLocation;
    }

    /**
     * Clone given list
     *
     * @param list Location List
     * @return Cloned list
     */
    public static ArrayList<PhotoCaptureLocationBO> cloneLocationList(
            ArrayList<PhotoCaptureLocationBO> list) {
        ArrayList<PhotoCaptureLocationBO> clone = new ArrayList<>(list.size());
        for (PhotoCaptureLocationBO item : list)
            clone.add(new PhotoCaptureLocationBO(item));
        return clone;
    }


    /**
     * Delete image from transaction table
     *
     * @param ImageName Image name
     */
    public void deleteImageDetailsFormTable(Context mContext, String ImageName) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.openDataBase();
            db.deleteSQL(DataMembers.tbl_PhotoCapture, "imgName="
                    + QT(ImageName), false);

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    /**
     * @param path File path
     * @return Availability
     * @See {@link FileUtils#isFileExisting(String)}
     * To check file availability
     * @deprecated
     */
    public boolean isImagePresent(String path) {
        File f = new File(path);
        return f.exists();
    }

    /**
     * @param path File path
     * @return URI
     * @See {@link FileUtils#getUriFromFile(Context, String)}
     * Getting file URI
     * @deprecated
     */
    public Uri getUriFromFile(Context mContext, String path) {
        File f = new File(path);
        if (Build.VERSION.SDK_INT >= 24) {
            return FileProvider.getUriForFile(mContext, BuildConfig.APPLICATION_ID + ".provider", f);

        } else {
            return Uri.fromFile(f);
        }

    }

}
