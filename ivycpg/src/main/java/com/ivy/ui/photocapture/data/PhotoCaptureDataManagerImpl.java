package com.ivy.ui.photocapture.data;

import android.database.Cursor;

import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.cpg.view.photocapture.PhotoCaptureLocationBO;
import com.ivy.cpg.view.photocapture.PhotoCaptureProductBO;
import com.ivy.cpg.view.photocapture.PhotoTypeMasterBO;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.DateUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;

public class PhotoCaptureDataManagerImpl implements PhotoCaptureDataManager {


    private DBUtil mDbUtil;

    @Inject
    public PhotoCaptureDataManagerImpl(@DataBaseInfo DBUtil dbUtil) {
        mDbUtil = dbUtil;
    }

    @Override
    public Observable<ArrayList<PhotoCaptureProductBO>> fetchPhotoCaptureProducts() {
        return Observable.fromCallable(new Callable<ArrayList<PhotoCaptureProductBO>>() {
            @Override
            public ArrayList<PhotoCaptureProductBO> call() {
                try {
                    mDbUtil.createDataBase();
                    mDbUtil.openDataBase();
                    ArrayList<PhotoCaptureProductBO> photoCaptureProductBOS = new ArrayList<>();
                    String query = "SELECT DISTINCT PID, PName, ParentId FROM ProductMaster WHERE Plid IN (SELECT ProductFilter1 FROM ConfigActivityFilter  WHERE ActivityCode = 'MENU_PHOTO') ORDER BY PID";
                    Cursor c = mDbUtil.selectSQL(query);
                    if (c != null) {
                        PhotoCaptureProductBO photoCaptureProductBO;
                        while (c.moveToNext()) {
                            photoCaptureProductBO = new PhotoCaptureProductBO();
                            photoCaptureProductBO.setProductID(c.getInt(0));
                            photoCaptureProductBO.setProductName(c.getString(1));
                            photoCaptureProductBOS.add(photoCaptureProductBO);
                        }
                        c.close();
                    }

                    return photoCaptureProductBOS;
                } catch (Exception ignored) {
                } finally {
                    if (mDbUtil != null)
                        mDbUtil.closeDB();
                }

                return new ArrayList<>();
            }
        });
    }

    @Override
    public Observable<ArrayList<PhotoCaptureLocationBO>> fetchEditedLocations(final String retailerID, final int distributorId) {
        return Observable.fromCallable(new Callable<ArrayList<PhotoCaptureLocationBO>>() {
            @Override
            public ArrayList<PhotoCaptureLocationBO> call() {
                try {
                    mDbUtil.createDataBase();
                    mDbUtil.openDataBase();
                    ArrayList<PhotoCaptureLocationBO> photoCaptureLocationBOS = new ArrayList<>();
                    String sql1 = "SELECT phototypeid,pid,imagepath,FromDate,ToDate,LocId,sku_name,abv,lot_code,seq_num,feedback,imgName FROM Photocapture WHERE RetailerID="
                            + retailerID + " And DistributorID=" + distributorId;
                    Cursor cursor = mDbUtil.selectSQL(sql1);
                    PhotoCaptureLocationBO lbo;
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            lbo = new PhotoCaptureLocationBO();
                            lbo.setPhotoTypeId(cursor
                                    .getInt(0));
                            lbo.setProductID(cursor.getInt(1));
                            lbo.setImagePath(cursor.getString(2));
                            lbo.setFromDate(DateUtil.convertFromServerDateToRequestedFormat(
                                    cursor.getString(3),
                                    ConfigurationMasterHelper.outDateFormat));
                            lbo.setToDate(DateUtil.convertFromServerDateToRequestedFormat(
                                    cursor.getString(4),
                                    ConfigurationMasterHelper.outDateFormat));
                            lbo.setLocationId(cursor.getInt(5));
                            lbo.setSKUName(cursor.getString(6));
                            lbo.setAbv(cursor.getString(7));
                            lbo.setLotCode(cursor.getString(8));
                            lbo.setSequenceNO(cursor.getString(9));
                            lbo.setFeedback(cursor.getString(10));
                            lbo.setImageName(cursor.getString(11));


                        }
                        return photoCaptureLocationBOS;
                    }
                } catch (Exception ignored) {
                } finally {
                    if (mDbUtil != null)
                        mDbUtil.closeDB();
                }


                return new ArrayList<>();
            }
        });
    }

    @Override
    public Observable<ArrayList<PhotoCaptureLocationBO>> fetchLocations() {
        return Observable.fromCallable(new Callable<ArrayList<PhotoCaptureLocationBO>>() {
            @Override
            public ArrayList<PhotoCaptureLocationBO> call() {

                try {
                    mDbUtil.createDataBase();
                    mDbUtil.openDataBase();

                    ArrayList<PhotoCaptureLocationBO> photoCaptureLocationBOS = new ArrayList<>();

                    String sql1 = "SELECT Distinct SL.ListId, SL.ListName"
                            + " FROM StandardListMaster SL  where SL.Listtype='PL' ORDER BY SL.ListId";

                    Cursor c = mDbUtil.selectSQL(sql1);

                    PhotoCaptureLocationBO locations;
                    if (c != null) {
                        while (c.moveToNext()) {
                            locations = new PhotoCaptureLocationBO();
                            locations.setLocationId(c.getInt(0));
                            locations.setLocationName(c.getString(1));
                            photoCaptureLocationBOS.add(locations);
                        }
                        c.close();
                    }


                    if (photoCaptureLocationBOS.size() == 0) {
                        locations = new PhotoCaptureLocationBO();
                        locations.setLocationId(0);
                        locations.setLocationName("Store");
                        photoCaptureLocationBOS.add(locations);
                    }

                } catch (Exception ignored) {

                }

                return new ArrayList<>();
            }
        });


    }

    @Override
    public Observable<ArrayList<PhotoTypeMasterBO>> fetchPhotoCaptureTypes() {
        return Observable.fromCallable(new Callable<ArrayList<PhotoTypeMasterBO>>() {
            @Override
            public ArrayList<PhotoTypeMasterBO> call() {

                try {
                    mDbUtil.createDataBase();
                    mDbUtil.openDataBase();

                    ArrayList<PhotoTypeMasterBO> photoTypeMasterBOS = new ArrayList<>();

                    Cursor c = mDbUtil.selectSQL("SELECT ListId, ListName, ListCode FROM StandardListMaster WHERE ListType = 'PHOTO_TYPE'");


                    PhotoTypeMasterBO typeMasterBO;
                    if (c != null) {
                        while (c.moveToNext()) {
                            typeMasterBO = new PhotoTypeMasterBO();
                            typeMasterBO.setPhotoTypeId(c.getInt(0));
                            typeMasterBO.setPhotoTypeDesc(c.getString(1));
                            typeMasterBO.setPhotoTypeCode(c.getString(2));
                            photoTypeMasterBOS.add(typeMasterBO);
                        }
                        c.close();
                    }
                    return photoTypeMasterBOS;
                } catch (Exception ignored) {

                }

                return new ArrayList<>();
            }
        });

    }


}
