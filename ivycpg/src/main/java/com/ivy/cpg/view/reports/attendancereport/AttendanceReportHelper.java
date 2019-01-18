package com.ivy.cpg.view.reports.attendancereport;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Callable;

import io.reactivex.Observable;

public class AttendanceReportHelper {
    private static AttendanceReportHelper instance = null;

    protected AttendanceReportHelper() {

    }

    public static AttendanceReportHelper getInstance() {
        if (instance == null) {
            return instance = new AttendanceReportHelper();
        }
        return instance;
    }

    public Observable<ArrayList<AttendanceReportBo>> downloadAttendanceReport(final int userid, final Context context) {
        return Observable.fromCallable(new Callable<ArrayList<AttendanceReportBo>>() {
            @Override
            public ArrayList<AttendanceReportBo> call() throws Exception {
                ArrayList<AttendanceReportBo> attendanceList = new ArrayList<>();
                DBUtil db = null;
                try {
                    db = new DBUtil(context, DataMembers.DB_NAME
                    );
                    db.createDataBase();
                    db.openDataBase();
                    Cursor c = db
                            .selectSQL("SELECT Date,Status,TimeIn,TimeOut,TimeSpent,Month from AttendanceReport where " +
                                    "userid =" + userid);
                    if (c != null) {
                        while (c.moveToNext()) {
                            AttendanceReportBo obj = new AttendanceReportBo();
                            obj.setDate(c.getString(0));
                            obj.setDay(getDay(c.getString(0)));
                            obj.setStatus(c.getString(1));
                            obj.setTimeIn(c.getString(2));
                            obj.setTimeOut(c.getString(3));
                            obj.setTimeSpent(c.getString(4));
                            obj.setMonth(c.getString(5));
                            attendanceList.add(obj);
                        }
                        c.close();
                    }

                    return attendanceList;
                } catch (Exception e) {
                    Commons.printException("" + e);
                } finally {
                    if (db != null)
                        db.closeDB();
                }

                return new ArrayList<>();
            }
        });
    }


    public Observable<ArrayList<String>> downloadAttendanceMonth(final int userId, final Context context) {
        return Observable.fromCallable(new Callable<ArrayList<String>>() {
            @Override
            public ArrayList<String> call() throws Exception {
                DBUtil db = null;
                ArrayList<String> attendanceMonth = new ArrayList<>();
                try {
                    db = new DBUtil(context, DataMembers.DB_NAME
                    );
                    db.createDataBase();
                    db.openDataBase();
                    Cursor c = db.selectSQL("SELECT distinct Month from AttendanceReport where " +
                            "userid =" + userId);
                    if (c != null) {
                        while (c.moveToNext()) {
                            attendanceMonth.add(c.getString(0));
                        }
                        c.close();
                    }
                    return attendanceMonth;
                } catch (Exception e) {
                    Commons.printException(e);
                } finally {
                    if (db != null)
                        db.closeDB();
                }
                return new ArrayList<>();
            }
        });
    }

    private String getDay(String date) {
        try {
            DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
            Date mdate = formatter.parse(date);
            Calendar cal = Calendar.getInstance();
            cal.setTime(mdate);
            switch (cal.get(Calendar.DAY_OF_WEEK)) {
                case 1:
                    return "Sunday";
                case 2:
                    return "Monday";
                case 3:
                    return "Tuesday";
                case 4:
                    return "Wednesday";
                case 5:
                    return "Thursday";
                case 6:
                    return "Friday";
                case 7:
                    return "Saturday";
                default:
                    return "Monday";
            }
        } catch (Exception e) {
            Commons.printException(e);
            return "Monday";
        }
    }
}
