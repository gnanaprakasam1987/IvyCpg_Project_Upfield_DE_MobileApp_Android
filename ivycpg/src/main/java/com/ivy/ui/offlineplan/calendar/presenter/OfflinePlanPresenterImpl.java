package com.ivy.ui.offlineplan.calendar.presenter;

import android.text.format.DateUtils;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.sd.png.asean.view.R;
import com.ivy.ui.offlineplan.calendar.bo.CalenderBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.ui.offlineplan.calendar.OfflinePlanContract;
import com.ivy.ui.offlineplan.calendar.data.OfflinePlanDataManager;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.rx.SchedulerProvider;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by mansoor on 27/03/2019
 */
public class OfflinePlanPresenterImpl<V extends OfflinePlanContract.OfflinePlanView> extends BasePresenter<V> implements OfflinePlanContract.OfflinePlanPresenter<V> {

    private OfflinePlanDataManager offlinePlanDataManager;
    private String planFromDate, planToDate;
    private ArrayList<String> mAllowedDates;
    private String generalPattern = "yyyy/MM/dd";
    private Calendar currentMonth = Calendar.getInstance();
    private String mSelectedDate;

    @Inject
    OfflinePlanPresenterImpl(DataManager dataManager,
                             SchedulerProvider schedulerProvider,
                             CompositeDisposable compositeDisposable,
                             ConfigurationMasterHelper configurationMasterHelper,
                             V view,
                             OfflinePlanDataManager offlinePlanDataManager) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, view);
        this.offlinePlanDataManager = offlinePlanDataManager;
    }

    /*
    To Set initial Plan dates
     */
    @Override
    public void setPlanDates() {
        Calendar aCalendar = Calendar.getInstance();
        aCalendar.add(Calendar.MONTH, -1);
        aCalendar.set(Calendar.DATE, 1);
        planFromDate = DateTimeUtils.convertDateObjectToRequestedFormat(aCalendar.getTime(), generalPattern);
        Commons.print("planFromDate" + planFromDate);

        Calendar zCalendar = Calendar.getInstance();
        zCalendar.add(Calendar.MONTH, 1);
        zCalendar.set(Calendar.DATE, zCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        planToDate = DateTimeUtils.convertDateObjectToRequestedFormat(zCalendar.getTime(), generalPattern);
        Commons.print("planFromDate" + planToDate);

        setAllowedDates();

        Calendar calendar = Calendar.getInstance();
        mSelectedDate = DateTimeUtils.convertDateObjectToRequestedFormat(calendar.getTime(), generalPattern);
    }

    private void setAllowedDates() {
        mAllowedDates = new ArrayList<>();

        Calendar fromCal = Calendar.getInstance();
        Calendar toCal = Calendar.getInstance();
        fromCal.setTime(DateTimeUtils.convertStringToDateObject(planFromDate, generalPattern));
        toCal.setTime(DateTimeUtils.convertStringToDateObject(planToDate, generalPattern));

        while (!fromCal.after(toCal)) {
            mAllowedDates.add(DateTimeUtils.convertDateObjectToRequestedFormat(fromCal.getTime(), generalPattern));
            fromCal.add(Calendar.DATE, 1);
        }
    }

    @Override
    public void loadCalendar() {
        ArrayList<CalenderBO> mCalenderAllList;
        String[] calendarDate = getMonthRange();
        int dayInWeekCount = getWeekDayCount(calendarDate[0]);
        mCalenderAllList = getDaysBetweenDates(calendarDate[0], calendarDate[1]);
        getIvyView().loadCalendarView(mAllowedDates, dayInWeekCount, mCalenderAllList);
        getIvyView().setMonthName(DateTimeUtils.convertDateObjectToRequestedFormat(
                currentMonth.getTime(), "MMMM yyyy"));

    }

    @Override
    public void onNextMonthClicked() {
        currentMonth.add(Calendar.MONTH, 1);
        Calendar toCalendar = Calendar.getInstance();
        Date dateTo = DateTimeUtils.convertStringToDateObject(planToDate, generalPattern);
        toCalendar.setTime(dateTo);
        if (currentMonth.get(Calendar.MONTH) <= toCalendar.get(Calendar.MONTH)) {
            if (toCalendar.get(Calendar.YEAR) != currentMonth.get(Calendar.YEAR)) {
                if (toCalendar.get(Calendar.MONTH) - currentMonth.get(Calendar.MONTH) <= 0) {
                    loadCalendar();
                } else {
                    currentMonth.add(Calendar.MONTH, -1);
                    getIvyView().showMessage(R.string.endOfPeriod);
                }
            } else
                loadCalendar();
        } else {
            currentMonth.add(Calendar.MONTH, -1);
            getIvyView().showMessage(R.string.endOfPeriod);
        }
    }

    @Override
    public void onPreviousMonthClicked() {
        currentMonth.add(Calendar.MONTH, -1);
        Calendar fromCalendar = Calendar.getInstance();
        Date dateTo = DateTimeUtils.convertStringToDateObject(planFromDate, generalPattern);
        fromCalendar.setTime(dateTo);
        if (currentMonth.get(Calendar.MONTH) >= fromCalendar.get(Calendar.MONTH)) {
            if (fromCalendar.get(Calendar.YEAR) != currentMonth.get(Calendar.YEAR)) {
                if (fromCalendar.get(Calendar.MONTH) - currentMonth.get(Calendar.MONTH) >= 0)
                    loadCalendar();
                else {
                    currentMonth.add(Calendar.MONTH, +1);
                    getIvyView().showMessage(R.string.endOfPeriod);
                }
            } else
                loadCalendar();
        } else {
            currentMonth.add(Calendar.MONTH, +1);
            getIvyView().showMessage(R.string.endOfPeriod);
        }

    }

    @Override
    public void setSelectedDate(String selectedDate) {
        mSelectedDate = selectedDate;
    }

    @Override
    public void loadDaysOfaWeek() {
        ArrayList<CalenderBO> mCalendarList = getWeekDays();

        //adding week no at start position
        CalenderBO cBO = new CalenderBO();
        cBO.setDay("WK");
        cBO.setCal_date("1");   // need to replace week no from DB
        mCalendarList.add(0, cBO);
        getIvyView().loadWeeks(mAllowedDates, mCalendarList);
    }

    private String[] getMonthRange() {
        Date beginning, end;
        String[] dates = new String[5];

        {
            Calendar calendar = getCalendarForNow();
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
            setTimeToBeginningOfDay(calendar);
            beginning = calendar.getTime();
            dates[0] = DateTimeUtils.convertDateObjectToRequestedFormat(beginning, generalPattern);

        }

        {

            Calendar calendar = getCalendarForNow();
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            setTimeToEndOfDay(calendar);
            end = calendar.getTime();
            dates[1] = DateTimeUtils.convertDateObjectToRequestedFormat(end, generalPattern);
        }

        return dates;
    }


    private Calendar getCalendarForNow() {
        Calendar calendar = (Calendar) currentMonth.clone();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return calendar;
    }

    private void setTimeToBeginningOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private void setTimeToEndOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
    }

    private int getWeekDayCount(String date) {
        int count;
        Calendar c = Calendar.getInstance();
        c.setTime(DateTimeUtils.convertStringToDateObject(date, generalPattern));

        count = c.get(Calendar.DAY_OF_WEEK) - 1;
        if (count == 0) {
            count = 7;
        }
        return count;
    }

    private ArrayList<CalenderBO> getDaysBetweenDates(String startDate, String endDate) {
        ArrayList<CalenderBO> calLsit = new ArrayList<>();
        Date startdate = DateTimeUtils.convertStringToDateObject(startDate, generalPattern);
        Date enddate;
        enddate = addDateNew(endDate);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startdate);

        while (calendar.getTime().before(enddate)) {
            CalenderBO cBO = new CalenderBO();
            Date result = calendar.getTime();
            SimpleDateFormat df1 = new SimpleDateFormat("dd", Locale.US);
            int d = SDUtil.convertToInt(df1.format(result));
            SimpleDateFormat outFormat = new SimpleDateFormat("EE", Locale.getDefault());
            String goal = outFormat.format(result);
            calendar.add(Calendar.DATE, 1);
            cBO.setCal_date(DateTimeUtils.convertDateObjectToRequestedFormat(result, generalPattern));
            cBO.setDay(goal);
            cBO.setDate(d);
            cBO.setToday(DateUtils.isToday(result.getTime()));
            cBO.setSelected(mSelectedDate.equalsIgnoreCase(cBO.getCal_date()));
            calLsit.add(cBO);

        }
        return calLsit;
    }


    private Date addDateNew(String dateOne) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(DateTimeUtils.convertStringToDateObject(dateOne, generalPattern));
        cal.add(Calendar.DAY_OF_MONTH, 1); // add 28 days
        return cal.getTime();
    }

    private ArrayList<CalenderBO> getWeekDays() {

        Calendar startWeek = Calendar.getInstance();
        startWeek.setTime(DateTimeUtils.convertStringToDateObject(mSelectedDate, generalPattern));
        startWeek.setFirstDayOfWeek(Calendar.MONDAY);
        startWeek.set(Calendar.DAY_OF_WEEK, startWeek.getFirstDayOfWeek());
        setTimeToBeginningOfDay(startWeek);


        Calendar endWeek = Calendar.getInstance();
        endWeek.setTime(DateTimeUtils.convertStringToDateObject(mSelectedDate, generalPattern));
        endWeek.set(Calendar.DAY_OF_WEEK, endWeek.getFirstDayOfWeek());
        endWeek.add(Calendar.DATE, 7);
        setTimeToEndOfDay(endWeek);

        ArrayList<CalenderBO> calLsit = new ArrayList<>();


        while (startWeek.getTime().before(endWeek.getTime())) {
            CalenderBO cBO = new CalenderBO();
            Date result = startWeek.getTime();
            SimpleDateFormat df1 = new SimpleDateFormat("dd", Locale.US);
            String d =df1.format(result);
            SimpleDateFormat outFormat = new SimpleDateFormat("EE", Locale.getDefault());
            String goal = outFormat.format(result);
            startWeek.add(Calendar.DATE, 1);
            cBO.setCal_date(d);
            cBO.setDay(goal);
            cBO.setToday(DateUtils.isToday(result.getTime()));
            cBO.setSelected(mSelectedDate.equalsIgnoreCase(cBO.getCal_date()));
            calLsit.add(cBO);

        }
        return calLsit;

    }


    @Override
    public void loadRetailersData() {

    }

    @Override
    public void loadAllStoresData() {

    }

    @Override
    public void loadTodayVisitData() {

    }
}
