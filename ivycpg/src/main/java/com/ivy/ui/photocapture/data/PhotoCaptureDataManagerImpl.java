package com.ivy.ui.photocapture.data;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.util.Log;

import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.cpg.view.photocapture.PhotoCaptureLocationBO;
import com.ivy.cpg.view.photocapture.PhotoCaptureProductBO;
import com.ivy.cpg.view.photocapture.PhotoTypeMasterBO;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.DateUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;

import static com.ivy.utils.AppUtils.QT;

public class PhotoCaptureDataManagerImpl implements PhotoCaptureDataManager {


    private DBUtil mDbUtil;

    private AppDataProvider appDataProvider;

    @Inject
    public PhotoCaptureDataManagerImpl(@DataBaseInfo DBUtil dbUtil, AppDataProvider appDataProvider) {
        mDbUtil = dbUtil;
        this.appDataProvider = appDataProvider;
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
                    Log.d("Test","Exception");
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
                    String sql1 = "SELECT phototypeid,PC.pid,imagepath,FromDate,ToDate,LocId,sku_name,abv,lot_code,seq_num,feedback,imgName,SM.ListName,PM.PName,SML.ListName FROM Photocapture PC " +
                            "LEFT JOIN StandardListMaster SM ON SM.ListId=phototypeid " +
                            "LEFT JOIN ProductMaster PM ON PM.PID=PC.pid " +
                            "LEFT JOIN StandardListMaster SML ON SML.ListId=LocId " +
                            "WHERE RetailerID=" + retailerID + " And DistributorID=" + distributorId;
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
                            lbo.setmTypeName(cursor.getString(12));
                            lbo.setProductName(cursor.getString(13));
                            lbo.setLocationName(cursor.getString(14));

                            photoCaptureLocationBOS.add(lbo);
                        }
                        return photoCaptureLocationBOS;
                    }
                } catch (Exception ignored) {
                    Log.d("Test","Exception");
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
                    Log.d("Test","Exception");
                } finally {
                    mDbUtil.closeDB();
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

                } finally {
                    mDbUtil.closeDB();
                }

                return new ArrayList<>();
            }
        });

    }

    @Override
    public Single<Boolean> updatePhotoCaptureDetails(final HashMap<String, PhotoCaptureLocationBO> updatedData) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                try {
                    mDbUtil.createDataBase();
                    mDbUtil.openDataBase();
                    Cursor cursor = mDbUtil.selectSQL("SELECT Uid FROM "
                            + DataMembers.actPhotocapture + " WHERE RetailerId = "
                            + appDataProvider.getRetailMaster().getRetailerID()
                            + " AND DistributorID="
                            + appDataProvider.getRetailMaster().getDistributorId()
                            + " AND Date = "
                            + QT(SDUtil.now(SDUtil.DATE_GLOBAL)));

                    if (cursor.getCount() > 0) {
                        cursor.moveToNext();
                        mDbUtil.deleteSQL(DataMembers.actPhotocapture,
                                "Uid=" + QT(cursor.getString(0)), false);
                        cursor.close();
                    }
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        }).flatMap(new Function<Boolean, SingleSource<? extends Boolean>>() {
            @Override
            public SingleSource<? extends Boolean> apply(Boolean aBoolean) throws Exception {
                return Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {

                        String columns = "uid,date,phototypeid,pid,imagepath,retailerid,RetailerName,ImageCount4Retailer,FromDate,ToDate,LocId," +
                                "sku_name,abv,lot_code,seq_num,DistributorID,feedback,imgName";

                        String uid = QT(appDataProvider.getUser()
                                .getDistributorid()
                                + ""
                                + appDataProvider.getUser().getUserid()
                                + "" + SDUtil.now(SDUtil.DATE_TIME_ID));

                        try {
                            for (Map.Entry<String, PhotoCaptureLocationBO> entry : updatedData.entrySet()) {
                                System.out.println(entry.getKey() + "/" + entry.getValue());
                                PhotoCaptureLocationBO photoCaptureLocationBO = entry.getValue();

                                String sBuffer = uid +
                                        "," +
                                        QT(SDUtil.now(SDUtil.DATE_GLOBAL)) +
                                        "," +
                                        photoCaptureLocationBO.getPhotoTypeId() +
                                        "," +
                                        photoCaptureLocationBO.getProductID() +
                                        "," +
                                        QT(photoCaptureLocationBO.getImagePath()) +
                                        "," +
                                        appDataProvider.getRetailMaster().getRetailerID() +
                                        "," +
                                        DatabaseUtils
                                                .sqlEscapeString(appDataProvider.getRetailMaster()
                                                        .getRetailerName()) +
                                        "," +
                                        "1" +
                                        "," +
                                        QT(photoCaptureLocationBO.getFromDate()) +
                                        "," +
                                        QT(photoCaptureLocationBO.getToDate()) +
                                        "," +
                                        photoCaptureLocationBO.getLocationId() +
                                        "," +
                                        QT(photoCaptureLocationBO.getSKUName()) +
                                        "," +
                                        QT(photoCaptureLocationBO.getAbv()) +
                                        "," +
                                        QT(photoCaptureLocationBO.getLotCode()) +
                                        "," +
                                        QT(photoCaptureLocationBO.getSequenceNO()) +
                                        "," +
                                        appDataProvider.getRetailMaster()
                                                .getDistributorId() +
                                        "," +
                                        QT(photoCaptureLocationBO.getFeedback()) +
                                        "," +
                                        QT(photoCaptureLocationBO.getImageName());

                                mDbUtil.insertSQL(DataMembers.actPhotocapture, columns,
                                        sBuffer);

                            }

                            return true;
                        } catch (Exception ignored) {
                            return false;
                        } finally {
                            if (mDbUtil != null)
                                mDbUtil.closeDB();
                        }
                    }
                });
            }
        });
    }


}
