package com.ivy.ui.retailerplan.calendar.data;

import android.database.Cursor;

import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.util.Commons;
import com.ivy.ui.retailerplan.calendar.bo.PeriodBo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by mansoor on 27/03/2019
 */
public class CalendarPlanDataManagerImpl implements CalendarPlanDataManager {
    private DBUtil mDbUtil;

    private AppDataProvider appDataProvider;

    @Inject
    CalendarPlanDataManagerImpl(@DataBaseInfo DBUtil dbUtil, AppDataProvider appDataProvider) {
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
    public void tearDown() {
        shutDownDb();
    }

    @Override
    public Observable<List<String>> loadAllowedDates() {
        return Observable.fromCallable(() -> {

            List<String> allowedDates = new ArrayList<>();
            initDb();
            try {
                String sql = "select MIN(STARTDATE),MAX(endDate) from VisitPlanPeriod where PeriodType = 'MONTH'";
                Cursor c = mDbUtil.selectSQL(sql);
                if (c != null && c.getCount() > 0) {
                    while (c.moveToNext()) {
                        if (c.getString(0) != null && c.getString(1) != null) {
                            allowedDates.add(c.getString(0));
                            allowedDates.add(c.getString(1));
                        }
                    }
                    c.close();
                }
                shutDownDb();
            } catch (Exception e) {
                Commons.printException("" + e);
                shutDownDb();
            }
            return allowedDates;
        });
    }

    @Override
    public Observable<List<PeriodBo>> loadPeriods() {
        return Observable.fromCallable(() -> {
            List<PeriodBo> periodList = new ArrayList<>();
            initDb();
            try {
                String sql = "select startDate,endDate,Description from VisitPlanPeriod  where PeriodType = 'MONTH'";
                Cursor c = mDbUtil.selectSQL(sql);
                if (c != null && c.getCount() > 0) {
                    while (c.moveToNext()) {
                        PeriodBo periodBo = new PeriodBo();
                        periodBo.setStartDate(c.getString(0));
                        periodBo.setEndDate(c.getString(1));
                        periodBo.setDescription(c.getString(2));
                        periodList.add(periodBo);
                    }
                    c.close();
                }
                shutDownDb();
            } catch (Exception e) {
                Commons.printException("" + e);
                shutDownDb();
            }
            return periodList;
        });
    }

    @Override
    public Observable<List<PeriodBo>> loadWeekData() {
        return Observable.fromCallable(() -> {
            List<PeriodBo> weekList = new ArrayList<>();
            initDb();
            try {
                String sql = "select startDate,endDate,Description from VisitPlanPeriod   where PeriodType = 'WEEK' Order by startDate asc";
                Cursor c = mDbUtil.selectSQL(sql);
                if (c != null && c.getCount() > 0) {
                    while (c.moveToNext()) {
                        PeriodBo periodBo = new PeriodBo();
                        periodBo.setStartDate(c.getString(0));
                        periodBo.setEndDate(c.getString(1));
                        periodBo.setDescription(c.getString(2));
                        weekList.add(periodBo);
                    }
                    c.close();
                }
                shutDownDb();
            } catch (Exception e) {
                Commons.printException("" + e);
                shutDownDb();
            }
            return weekList;
        });
    }
}
