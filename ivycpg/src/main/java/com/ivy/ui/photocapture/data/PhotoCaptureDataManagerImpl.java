package com.ivy.ui.photocapture.data;

import android.database.Cursor;
import android.database.DatabaseUtils;

import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.ui.photocapture.model.PhotoCaptureLocationBO;
import com.ivy.ui.photocapture.model.PhotoCaptureProductBO;
import com.ivy.ui.photocapture.model.PhotoTypeMasterBO;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;

import static com.ivy.utils.StringUtils.QT;

public class PhotoCaptureDataManagerImpl implements PhotoCaptureDataManager {


    private DBUtil mDbUtil;

    private AppDataProvider appDataProvider;

    @Inject
    public PhotoCaptureDataManagerImpl(@DataBaseInfo DBUtil dbUtil, AppDataProvider appDataProvider) {
        mDbUtil = dbUtil;
        this.appDataProvider = appDataProvider;
    }

    private void initDb() {
        mDbUtil.createDataBase();
        if(mDbUtil.isDbNullOrClosed())
            mDbUtil.openDataBase();
    }

    private void shutDownDb(){
        mDbUtil.closeDB();
    }

    @Override
    public Observable<ArrayList<PhotoCaptureProductBO>> fetchPhotoCaptureProducts() {
        return Observable.fromCallable(new Callable<ArrayList<PhotoCaptureProductBO>>() {
            @Override
            public ArrayList<PhotoCaptureProductBO> call() {
                try {
                    initDb();
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
                    shutDownDb();
                    return photoCaptureProductBOS;
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }
                shutDownDb();
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
                    initDb();
                    ArrayList<PhotoCaptureLocationBO> photoCaptureLocationBOS = new ArrayList<>();
                    String sql1 = "SELECT phototypeid,PC.pid,imagepath,FromDate,ToDate,LocId,sku_name,abv,lot_code,seq_num,feedback,imgName,SM.ListName,PM.PName,SML.ListName " +
                            "FROM Photocapture PC " +
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
                            lbo.setFromDate(DateTimeUtils.convertFromServerDateToRequestedFormat(
                                    cursor.getString(3),
                                    ConfigurationMasterHelper.outDateFormat));
                            lbo.setToDate(DateTimeUtils.convertFromServerDateToRequestedFormat(
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
                        shutDownDb();
                        return photoCaptureLocationBOS;
                    }
                } catch (Exception ignored) {
                }

                shutDownDb();
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
                    initDb();
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
                    shutDownDb();
                    return photoCaptureLocationBOS;
                } catch (Exception ignored) {
                }
                shutDownDb();
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
                    initDb();

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
                    shutDownDb();
                    return photoTypeMasterBOS;
                } catch (Exception ignored) {

                }

                shutDownDb();
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
                    initDb();
                    Cursor cursor = mDbUtil.selectSQL("SELECT Uid FROM "
                            + DataMembers.actPhotocapture + " WHERE RetailerId = "
                            + appDataProvider.getRetailMaster().getRetailerID()
                            + " AND DistributorID="
                            + appDataProvider.getRetailMaster().getDistributorId()
                            + " AND Date = "
                            + QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)));

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
                                "sku_name,abv,lot_code,seq_num,DistributorID,feedback,imgName,ridSF,VisitId";

                        String uid = QT(appDataProvider.getUser()
                                .getDistributorid()
                                + ""
                                + appDataProvider.getUser().getUserid()
                                + "" + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID));

                        try {
                            for (Map.Entry<String, PhotoCaptureLocationBO> entry : updatedData.entrySet()) {
                                System.out.println(entry.getKey() + "/" + entry.getValue());
                                PhotoCaptureLocationBO photoCaptureLocationBO = entry.getValue();

                                String sBuffer = uid +
                                        "," +
                                        QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) +
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
                                        QT(photoCaptureLocationBO.getImageName()) +
                                        "," +
                                        QT(appDataProvider.getRetailMaster().getRidSF()) +
                                        "," +
                                        appDataProvider.getUniqueId();

                                mDbUtil.insertSQL(DataMembers.actPhotocapture, columns,
                                        sBuffer);

                            }

                            shutDownDb();
                            return true;
                        } catch (Exception ignored) {
                            shutDownDb();
                            return false;
                        }
                    }
                });
            }
        });
    }

    @Override
    public void tearDown() {
        shutDownDb();
    }
}
