package com.ivy.ui.attendance.data;

import android.database.Cursor;

import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.cpg.view.nonfield.NonFieldTwoBo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.AppUtils;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Created by mansoor on 27/12/2018
 */
public class TimeTrackDataMangerImpl implements TimeTrackDataManager {

    private DBUtil mDbUtil;

    private AppDataProvider appDataProvider;

    @Inject
    public TimeTrackDataMangerImpl(@DataBaseInfo DBUtil dbUtil, AppDataProvider appDataProvider) {
        mDbUtil = dbUtil;
        this.appDataProvider = appDataProvider;
    }

    private void initDb() {
        mDbUtil.createDataBase();
        if (mDbUtil.isDbNullOrClosed())
            mDbUtil.openDataBase();
    }

    private void shutDownDb() {
        mDbUtil.closeDB();
    }


    @Override
    public Observable<ArrayList<NonFieldTwoBo>> getTimeTrackList() {
        return Observable.fromCallable(new Callable<ArrayList<NonFieldTwoBo>>() {
            @Override
            public ArrayList<NonFieldTwoBo> call() {
                try {
                    initDb();
                    ArrayList<NonFieldTwoBo> timeTrackList = new ArrayList<>();
                    String query = "SELECT uid , date , intime , outtime , ifnull(remarks,'') , rowid,reasonid from AttendanceTimeDetails where date ="
                            + AppUtils.QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + " or outtime IS NULL and userid=" + appDataProvider.getUser().getUserid();
                    Cursor c = mDbUtil.selectSQL(query);
                    if (c != null) {
                        NonFieldTwoBo timeTrackBo;
                        while (c.moveToNext()) {
                            timeTrackBo = new NonFieldTwoBo();
                            timeTrackBo.setId(c.getString(0));
                            timeTrackBo.setFromDate(c.getString(1));
                            timeTrackBo.setInTime(c.getString(2));
                            timeTrackBo.setOutTime(c.getString(3));
                            timeTrackBo.setRemarks(c.getString(4));
                            timeTrackBo.setRowid(c.getInt(5));

                           /* if ((timeTrackBo.getInTime() != null && !timeTrackBo.getInTime().trim().equalsIgnoreCase(""))
                                    && (timeTrackBo.getOutTime() != null && !timeTrackBo.getOutTime().trim().equalsIgnoreCase(""))) {
                                timeTrackBo.setStatus(context.getResources().getString(R.string.in_complete));
                            } else {
                                timeTrackBo.setStatus(context.getResources().getString(R.string.in_partial));
                            }*/
                            timeTrackBo.setReason(c.getString(6));
                            timeTrackBo.setReasonText(getReasonName(c.getString(6)));
                            timeTrackList.add(timeTrackBo);

                        }
                        c.close();
                    }
                    shutDownDb();
                    return timeTrackList;
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }
                shutDownDb();
                return new ArrayList<>();
            }
        });
    }

    private String getReasonName(String id) {
        try {

            initDb();
            Cursor c = mDbUtil.selectSQL("SELECT ListName FROM StandardListMaster"
                    + " WHERE ListId = " + id);
            if (c != null) {
                if (c.moveToNext()) {
                    return c.getString(0);
                }
                c.close();
            }

        } catch (Exception e) {
            Commons.printException(e);
        }
        shutDownDb();
        return "";
    }

    /**
     * This Method checks the given Id is Working status
     *
     * @param id StandardListMaster ListId
     * @return returns boolean
     */
    public boolean isWorkingStatus(int id) {

        boolean isIdWorking = false;
        try {
            initDb();

            Cursor c = mDbUtil.selectSQL("select Listid from StandardListMaster where ListCode='WORKING' and ListId = '" + id + "'");
            if (c != null && c.getCount() > 0) {
                c.close();
                isIdWorking = true;
            }

        } catch (Exception e) {
            Commons.printException(e);
        }
        shutDownDb();
        return isIdWorking;
    }

    @Override
    public Single<Boolean> updateTimeTrackDetailsDb(NonFieldTwoBo nonFieldTwoBo) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                try {
                    initDb();

                    String updateSql = "update AttendanceTimeDetails " +
                            "SET intime = " + AppUtils.QT(nonFieldTwoBo.getInTime()) +
                            " , outtime = " + AppUtils.QT(nonFieldTwoBo.getOutTime()) +
                            ", upload ='N'" +
                            " WHERE rowid = " + nonFieldTwoBo.getRowid();

                    mDbUtil.updateSQL(updateSql);

                    shutDownDb();
                } catch (Exception e) {
                    Commons.printException(e);
                }
                shutDownDb();
                return true;
            }
        });
    }


    @Override
    public void tearDown() {
        shutDownDb();
    }
}
