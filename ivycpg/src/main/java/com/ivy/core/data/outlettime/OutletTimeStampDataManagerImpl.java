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
import io.reactivex.Observable;
import io.reactivex.Single;

import static com.ivy.sd.png.commons.SDUtil.DATE_GLOBAL;
import static com.ivy.sd.png.commons.SDUtil.now;
import static com.ivy.utils.AppUtils.QT;

public class OutletTimeStampDataManagerImpl implements OutletTimeStampDataManager {


    private DBUtil mDbUtil;

    private DataManager mDataManager;

    @Inject
    public OutletTimeStampDataManagerImpl(@DataBaseInfo DBUtil dbUtil, DataManager dataManager) {
        this.mDbUtil = dbUtil;
        this.mDataManager = dataManager;
    }


    @Override
    public Single<Boolean> isVisited(final String retailerId) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                try {
                    mDbUtil.createDataBase();
                    mDbUtil.openDataBase();
                    StringBuilder sb = new StringBuilder();
                    sb.append("select VisitID from OutletTimestamp where ");
                    sb.append(" RetailerID=" + QT(retailerId));
                    Cursor c = mDbUtil.selectSQL(sb.toString());
                    return c.getCount() > 0;
                } catch (Exception e) {
                    return false;
                } finally {
                    if (mDbUtil != null)
                        mDbUtil.closeDB();
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
    public Completable updateTimeStampModuleWise(final String timeOut) {
        return Completable.fromCallable(new Callable() {
            @Override
            public Void call() {
                try {
                    mDbUtil.createDataBase();
                    mDbUtil.openDataBase();

                    String dateTime = now(DATE_GLOBAL) + " " + timeOut;
                    String query = "UPDATE OutletTimeStampDetail SET TimeOut = '" + dateTime
                            + "'  WHERE RetailerID = '"
                            + mDataManager.getRetailMaster().getRetailerID()
                            + "' AND TimeIn = " + mDataManager.getModuleIntime() + " AND UID = " + mDataManager.getUniqueId();
                    mDbUtil.updateSQL(query);

                } catch (Exception e) {
                } finally {
                    if (mDbUtil != null)
                        mDbUtil.closeDB();
                }

                return null;
            }
        });
    }

    /**
     * Used to set Time Stamp.
     *
     * @param date   module start-in date
     * @param timeIn module start-in time
     */
    @Override
    public Completable saveTimeStampModuleWise(final String date, final String timeIn, final String moduleCode) {
        return Completable.fromCallable(new Callable() {
            @Override
            public Void call() {
                mDataManager.setModuleInTime(QT(date + " " + timeIn));
                try {
                    mDbUtil.createDataBase();
                    mDbUtil.openDataBase();

                    String values = mDataManager.getUniqueId() + ","
                            + QT(moduleCode) + ","
                            + mDataManager.getModuleIntime() + "," + mDataManager.getModuleIntime()
                            + ","
                            + QT(mDataManager.getRetailMaster().getRetailerID());
                    mDbUtil.insertSQL(DataMembers.tbl_outlet_time_stamp_detail, DataMembers.tbl_outlet_time_stamp_detail_cols, values);

                } catch (Exception e) {
                } finally {
                    if (mDbUtil != null)
                        mDbUtil.closeDB();
                }

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
                    mDbUtil.createDataBase();
                    mDbUtil.openDataBase();

                    mDbUtil.deleteSQL(DataMembers.tbl_OutletTimestamp, "retailerid="
                            + mDataManager.getRetailMaster().getRetailerID(), false);
                    mDbUtil.deleteSQL(DataMembers.tbl_outlet_time_stamp_detail, "retailerid="
                            + mDataManager.getRetailMaster().getRetailerID() + " AND UID=" + mDataManager.getUniqueId(), false);
                    mDbUtil.deleteSQL(DataMembers.tbl_OutletTimestamp_images, "uid="
                            + mDataManager.getUniqueId(), false);

                    mDbUtil.closeDB();

                } catch (Exception e) {
                } finally {
                    if (mDbUtil != null)
                        mDbUtil.closeDB();
                }
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
                    mDbUtil.createDataBase();
                    mDbUtil.openDataBase();

                    String dateTime = now(DATE_GLOBAL) + " " + timeOut;
                    String query = "UPDATE OutletTimeStamp SET TimeOut = '" + dateTime
                            + "',feedback=" + QT(reasonDesc)
                            + ", OrderValue = " + QT(String.valueOf(mDataManager.getOrderValue()))
                            + ", outLatitude = " + QT(LocationUtil.latitude + "")
                            + ", outLongitude = " + QT(LocationUtil.longitude + "")
                            + ", LocationProvider = " + QT(LocationUtil.mProviderName)
                            + ", gpsAccuracy = " + QT(LocationUtil.accuracy + "")
                            + ", Battery = " + batteryPercentage
                            + ", IsLocationEnabled = " + QT(String.valueOf(isGPSEnabled))
                            + ", IsDeviated = " + QT(String.valueOf(mDataManager.getRetailMaster().getIsDeviated()))
                            + "  WHERE RetailerID = '"
                            + mDataManager.getRetailMaster().getRetailerID()
                            + "' AND TimeIn = '" + mDataManager.getInTime() + "'";
                    mDbUtil.updateSQL(query);

                    mDbUtil.closeDB();


                } catch (Exception e) {
                } finally {
                    if (mDbUtil != null)
                        mDbUtil.closeDB();
                }
                return null;
            }
        });
    }


}
