package com.ivy.core.data.outlettime;

import android.database.Cursor;

import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.util.DataMembers;

import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Single;

import static com.ivy.utils.AppUtils.QT;

public class OutletTimeStampDataManagerImpl implements OutletTimeStampDataManager {

    private AppDataProvider appDataProvider;

    private DBUtil mDbUtil;

    @Inject
    public OutletTimeStampDataManagerImpl(@DataBaseInfo DBUtil dbUtil, AppDataProvider appDataProvider) {
        this.appDataProvider = appDataProvider;
        this.mDbUtil = dbUtil;
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
    public Single<Boolean> updateTimeStampModuleWise(final String timeOut) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                try {
                    mDbUtil.createDataBase();
                    mDbUtil.openDataBase();

                    String dateTime = com.ivy.sd.png.commons.SDUtil.now(com.ivy.sd.png.commons.SDUtil.DATE_GLOBAL) + " " + timeOut;
                    String query = "UPDATE OutletTimeStampDetail SET TimeOut = '" + dateTime
                            + "'  WHERE RetailerID = '"
                            + appDataProvider.getRetailMaster().getRetailerID()
                            + "' AND TimeIn = " + appDataProvider.getModuleIntime() + " AND UID = " + appDataProvider.getUniqueId();
                    mDbUtil.updateSQL(query);

                    return true;
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
     * Used to set Time Stamp.
     *
     * @param date   module start-in date
     * @param timeIn module start-in time
     */
    @Override
    public Single<Boolean> saveTimeStampModuleWise(final String date, final String timeIn, final String moduleCode) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                appDataProvider.setModuleInTime(QT(date + " " + timeIn));
                try {
                    mDbUtil.createDataBase();
                    mDbUtil.openDataBase();

                    String values = appDataProvider.getUniqueId() + ","
                            + QT(moduleCode) + ","
                            + appDataProvider.getModuleIntime() + "," + appDataProvider.getModuleIntime()
                            + ","
                            + QT(appDataProvider.getRetailMaster().getRetailerID());
                    mDbUtil.insertSQL(DataMembers.tbl_outlet_time_stamp_detail, DataMembers.tbl_outlet_time_stamp_detail_cols, values);

                    return true;
                } catch (Exception e) {
                    return false;
                } finally {
                    if (mDbUtil != null)
                        mDbUtil.closeDB();
                }

            }
        });
    }

    @Override
    public Single<Boolean> deleteTimeStamps() {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                try {
                    mDbUtil.createDataBase();
                    mDbUtil.openDataBase();

                    mDbUtil.deleteSQL(DataMembers.tbl_OutletTimestamp, "retailerid="
                            + appDataProvider.getRetailMaster().getRetailerID(), false);
                    mDbUtil.deleteSQL(DataMembers.tbl_outlet_time_stamp_detail, "retailerid="
                            + appDataProvider.getRetailMaster().getRetailerID() + " AND UID=" + appDataProvider.getUniqueId(), false);
                    mDbUtil.deleteSQL(DataMembers.tbl_OutletTimestamp_images, "uid="
                            + appDataProvider.getUniqueId(), false);

                    mDbUtil.closeDB();

                    return true;
                } catch (Exception e) {
                    return false;
                } finally {
                    if (mDbUtil != null)
                        mDbUtil.closeDB();
                }

            }
        });
    }
}
