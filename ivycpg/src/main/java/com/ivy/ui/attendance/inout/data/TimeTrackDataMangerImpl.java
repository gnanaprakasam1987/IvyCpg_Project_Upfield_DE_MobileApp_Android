package com.ivy.ui.attendance.inout.data;

import android.database.Cursor;

import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.cpg.view.nonfield.NonFieldTwoBo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.StringUtils;

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
                            + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) + " or outtime IS NULL and userid=" + appDataProvider.getUser().getUserid();
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
        String reasonName = "";
        try {

            initDb();
            Cursor c = mDbUtil.selectSQL("SELECT ListName FROM StandardListMaster"
                    + " WHERE ListId = " + id);
            if (c != null) {
                if (c.moveToNext()) {
                    reasonName = c.getString(0);
                }
                c.close();
            }

        } catch (Exception e) {
            Commons.printException(e);
        }
        shutDownDb();
        return reasonName;
    }

    /**
     * This Method checks the given Id is Working status
     *
     * @param id StandardListMaster ListId
     * @return returns boolean
     */
    @Override
    public Single<Boolean> isWorkingStatus(int id) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() {
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
        });
    }

    @Override
    public Single<Boolean> updateTimeTrackDetailsDb(NonFieldTwoBo nonFieldTwoBo) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                try {
                    initDb();

                    String updateSql = "update AttendanceTimeDetails " +
                            "SET intime = " + StringUtils.getStringQueryParam(nonFieldTwoBo.getInTime()) +
                            " , outtime = " + StringUtils.getStringQueryParam(nonFieldTwoBo.getOutTime()) +
                            ", upload ='N'" +
                            " WHERE rowid = " + nonFieldTwoBo.getRowid();

                    mDbUtil.updateSQL(updateSql);

                } catch (Exception e) {
                    Commons.printException(e);
                }
                shutDownDb();
                return true;
            }
        });
    }

    @Override
    public Observable<ArrayList<ReasonMaster>> getInOutReasonList() {
        return Observable.fromCallable(new Callable<ArrayList<ReasonMaster>>() {
            @Override
            public ArrayList<ReasonMaster> call() {
                try {
                    initDb();
                    ArrayList<ReasonMaster> reasonList = new ArrayList<>();
                    String query = "SELECT ListId, ListName FROM StandardListMaster WHERE ListType = 'REASON'"
                            + " AND ParentId = (SELECT ListId FROM StandardListMaster WHERE ListType ='REASON_TYPE' AND ListCode = 'ATR')";
                    ;
                    Cursor c = mDbUtil.selectSQL(query);
                    if (c != null) {
                        ReasonMaster reasonMaster;
                        while (c.moveToNext()) {
                            reasonMaster = new ReasonMaster();
                            reasonMaster.setReasonID(c.getString(0));
                            reasonMaster.setReasonDesc(c.getString(1));
                            reasonList.add(reasonMaster);
                        }
                        c.close();
                    }
                    shutDownDb();
                    return reasonList;
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }
                shutDownDb();
                return new ArrayList<>();
            }
        });
    }

    @Override
    public Single<Boolean> saveTimeTrackDetailsDb(String reasonId, String remarks, double latitude, double longitude) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                try {
                    NonFieldTwoBo addNonFieldTwoBo = new NonFieldTwoBo();
                    addNonFieldTwoBo.setId(appDataProvider.getUser().getUserid()
                            + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID) + "");
                    addNonFieldTwoBo.setFromDate(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL));
                    addNonFieldTwoBo.setInTime(DateTimeUtils.now(DateTimeUtils.DATE_TIME_NEW));
                    addNonFieldTwoBo.setOutTime(null);
                    addNonFieldTwoBo.setRemarks(remarks);
                    addNonFieldTwoBo.setReason(reasonId);

                    initDb();
                    String inTime = addNonFieldTwoBo.getInTime() != null ? addNonFieldTwoBo.getInTime() : " ";
                    String columns = "uid,date,intime,reasonid,userid,latitude,longitude,counterid,Remarks,upload";
                    String value = StringUtils.getStringQueryParam(addNonFieldTwoBo.getId()) + ","
                            + StringUtils.getStringQueryParam(addNonFieldTwoBo.getFromDate()) + ","
                            + StringUtils.getStringQueryParam(inTime) + ","
                            + addNonFieldTwoBo.getReason() + "," + appDataProvider.getUser().getUserid() + ","
                            + StringUtils.getStringQueryParam(latitude + "") + "," + StringUtils.getStringQueryParam(longitude + "") + ","
                            + 0 + "," + StringUtils.getStringQueryParam(addNonFieldTwoBo.getRemarks()) + "," + StringUtils.getStringQueryParam("N");

                    mDbUtil.insertSQL("AttendanceTimeDetails", columns, value);

                    shutDownDb();
                    return true;
                } catch (Exception e) {
                    Commons.printException(e);
                }
                shutDownDb();
                return false;
            }
        });
    }

    @Override
    public boolean checkIsLeave() {
        try {
            initDb();
            Cursor c = mDbUtil
                    .selectSQL("SELECT * FROM AttendanceTimeDetails where userid = " + appDataProvider.getUser().getUserid() +
                            " AND date = " + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) +
                            " AND upload = 'N' or upload ='Y'");
            if (c.getCount() == 0) {
                shutDownDb();
                return true;
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
        shutDownDb();
        return false;
    }

    @Override
    public void tearDown() {
        shutDownDb();
    }
}
