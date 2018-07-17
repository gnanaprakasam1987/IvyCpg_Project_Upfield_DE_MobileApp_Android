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
            public ArrayList<PhotoCaptureProductBO> call() throws Exception {
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
                }

                return new ArrayList<>();
            }
        });/*.flatMap(new Function<ArrayList<PhotoCaptureProductBO>, ObservableSource<ArrayList<PhotoCaptureProductBO>>>() {
            @Override
            public ObservableSource<ArrayList<PhotoCaptureProductBO>> apply(final ArrayList<PhotoCaptureProductBO> photoCaptureProductBOS) throws Exception {
                return Observable.fromCallable(new Callable<ArrayList<PhotoCaptureProductBO>>() {
                    @Override
                    public ArrayList<PhotoCaptureProductBO> call() throws Exception {


                        try {
                            String sql1 = "SELECT phototypeid,pid,imagepath,FromDate,ToDate,LocId,sku_name,abv,lot_code,seq_num,feedback,imgName FROM Photocapture WHERE RetailerID="
                                    + retailerID + " And DistributorID=" + distributorId;
                            Cursor cursor = mDbUtil.selectSQL(sql1);

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
                                                    lbo.setFromDate(DateUtil.convertFromServerDateToRequestedFormat(
                                                            cursor.getString(3),
                                                            ConfigurationMasterHelper.outDateFormat));
                                                    lbo.setToDate(DateUtil.convertFromServerDateToRequestedFormat(
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


                            return photoCaptureProductBOS;
                        } catch (Exception ignored) {
                        } finally {
                            if (mDbUtil != null)
                                mDbUtil.closeDB();
                        }
                        return photoCaptureProductBOS;
                    }
                });
            }
        });*/
    }

    @Override
    public Observable<ArrayList<PhotoCaptureLocationBO>> fetchLocations(String retailerID, int distributorId) {
        return null;
    }


}
