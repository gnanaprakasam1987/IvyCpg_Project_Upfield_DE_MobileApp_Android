package com.ivy.core.data.outlettime;

import android.database.Cursor;

import com.ivy.core.data.datamanager.DataManager;
import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.location.LocationUtil;
import com.ivy.sd.png.util.DataMembers;

import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Single;

import static com.ivy.utils.DateTimeUtils.DATE_GLOBAL;
import static com.ivy.utils.DateTimeUtils.now;
import static com.ivy.utils.StringUtils.getStringQueryParam;

public class OutletTimeStampDataManagerImpl implements OutletTimeStampDataManager {


    private DBUtil mDbUtil;

    private DataManager mDataManager;

    @Inject
    public OutletTimeStampDataManagerImpl(@DataBaseInfo DBUtil dbUtil, DataManager dataManager) {
        this.mDbUtil = dbUtil;
        this.mDataManager = dataManager;

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
    public Single<Boolean> isVisited(final String retailerId) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                try {

                    initDb();
                    String sb = "select VisitID from OutletTimestamp where " +
                            " RetailerID=" + getStringQueryParam(retailerId);
                    Cursor c = mDbUtil.selectSQL(sb);
                    shutDownDb();
                    return c.getCount() > 0;
                } catch (Exception e) {
                    shutDownDb();
                    return false;
                }

            }
        });
    }

    /**
     * Set Time Out
     *
     * @param timeOut module exit time
     */
    @Override
    public Single<Boolean> updateTimeStampModuleWise(final String timeOut) {
        return Single.fromCallable(new Callable() {
            @Override
            public Boolean call() {
                try {

                    initDb();
                    String query = "UPDATE OutletTimeStampDetail SET TimeOut = '" + timeOut
                            + "'  WHERE RetailerID = '"
                            + mDataManager.getRetailMaster().getRetailerID()
                            + "' AND TimeIn = " + mDataManager.getModuleIntime() + " AND UID = " + mDataManager.getUniqueId();
                    mDbUtil.updateSQL(query);

                    shutDownDb();
                    return true;

                } catch (Exception ignored) {
                }
                shutDownDb();

                return false;
            }
        });
    }

    /**
     * Used to set Time Stamp.
     *
     * @param datetimeIn   module start-in date and time,
     * if FUN81(restrict to capture user time) is enabled IVYCostants default time or else current date and time.
     */
    @Override
    public Completable saveTimeStampModuleWise(final String datetimeIn, final String moduleCode) {
        return Completable.fromCallable(new Callable() {
            @Override
            public Void call() {
                mDataManager.setModuleInTime(getStringQueryParam(datetimeIn));
                try {

                    initDb();

                    String values = mDataManager.getUniqueId() + ","
                            + getStringQueryParam(moduleCode) + ","
                            + mDataManager.getModuleIntime() + "," + mDataManager.getModuleIntime()
                            + ","
                            + getStringQueryParam(mDataManager.getRetailMaster().getRetailerID());
                    mDbUtil.insertSQL(DataMembers.tbl_outlet_time_stamp_detail, DataMembers.tbl_outlet_time_stamp_detail_cols, values);

                } catch (Exception ignored) {
                }

                shutDownDb();
                return null;
            }
        });
    }

    @Override
    public Completable deleteTimeStamps() {
        return Completable.fromCallable(new Callable<Void>() {
            @Override
            public Void call() {
                try {


                    initDb();
                    mDbUtil.deleteSQL(DataMembers.tbl_OutletTimestamp, "retailerid="
                            + mDataManager.getRetailMaster().getRetailerID(), false);
                    mDbUtil.deleteSQL(DataMembers.tbl_outlet_time_stamp_detail, "retailerid="
                            + mDataManager.getRetailMaster().getRetailerID() + " AND UID=" + mDataManager.getUniqueId(), false);
                    mDbUtil.deleteSQL(DataMembers.tbl_OutletTimestamp_images, "uid="
                            + mDataManager.getUniqueId(), false);


                } catch (Exception ignored) {
                }
                shutDownDb();
                return null;
            }
        });
    }

    @Override
    public Completable updateTimeStamp(final String timeOut, final String reasonDesc, final int batteryPercentage, final boolean isGPSEnabled) {
        return Completable.fromCallable(new Callable() {
            @Override
            public Void call() {
                try {

                    initDb();
                    String dateTime = now(DATE_GLOBAL) + " " + timeOut;
                    String query = "UPDATE OutletTimeStamp SET TimeOut = '" + dateTime
                            + "',feedback=" + getStringQueryParam(reasonDesc)
                            + ", OrderValue = " + getStringQueryParam(String.valueOf(mDataManager.getOrderValue()))
                            + ", outLatitude = " + getStringQueryParam(LocationUtil.latitude + "")
                            + ", outLongitude = " + getStringQueryParam(LocationUtil.longitude + "")
                            + ", LocationProvider = " + getStringQueryParam(LocationUtil.mProviderName)
                            + ", gpsAccuracy = " + getStringQueryParam(LocationUtil.accuracy + "")
                            + ", Battery = " + batteryPercentage
                            + ", IsLocationEnabled = " + getStringQueryParam(String.valueOf(isGPSEnabled))
                            + ", IsDeviated = " + getStringQueryParam(String.valueOf(mDataManager.getRetailMaster().getIsDeviated()))
                            + "  WHERE RetailerID = '"
                            + mDataManager.getRetailMaster().getRetailerID()
                            + "' AND TimeIn = '" + mDataManager.getInTime() + "'";
                    mDbUtil.updateSQL(query);



                } catch (Exception ignored) {
                }
                shutDownDb();
                return null;
            }
        });
    }


    @Override
    public void tearDown() {
        if(mDbUtil.isDbNullOrClosed())
            mDbUtil.closeDB();
        mDataManager.tearDown();
    }
}
