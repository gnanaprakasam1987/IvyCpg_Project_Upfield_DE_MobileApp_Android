package com.ivy.ui.offlineplan.calendar.presenter;

import android.text.format.DateUtils;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.sd.png.asean.view.R;
import com.ivy.ui.offlineplan.calendar.CalendarPlanContract;
import com.ivy.ui.offlineplan.calendar.bo.CalenderBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.ui.offlineplan.calendar.bo.TimeSlotsBo;
import com.ivy.ui.offlineplan.calendar.data.CalendarPlanDataManager;
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
public class CalendarPlanPresenterImpl<V extends CalendarPlanContract.CalendarPlanView> extends BasePresenter<V> implements CalendarPlanContract.CalendarPlanPresenter<V> {

    private CalendarPlanDataManager calendarPlanDataManager;
    private String planFromDate, planToDate;
    private ArrayList<String> mAllowedDates;
    private String generalPattern = "yyyy/MM/dd";
    private Calendar currentMonth;
    private String mSelectedDate;

    @Inject
    CalendarPlanPresenterImpl(DataManager dataManager,
                              SchedulerProvider schedulerProvider,
                              CompositeDisposable compositeDisposable,
                              ConfigurationMasterHelper configurationMasterHelper,
                              V view,
                              CalendarPlanDataManager calendarPlanDataManager) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, view);
        this.calendarPlanDataManager = calendarPlanDataManager;
    }

    /*
    To Set initial Plan dates
     */
    @Override
    public void setPlanDates() {
        currentMonth = Calendar.getInstance();
        setSelectedDate(DateTimeUtils.convertDateObjectToRequestedFormat(currentMonth.getTime(), generalPattern));

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
                currentMonth.getTime(), "MMM yyyy"));

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
    public void onNextWeekClicked() {
        currentMonth.add(Calendar.WEEK_OF_YEAR, +1);
        Calendar toCalendar = Calendar.getInstance();
        Date dateTo = DateTimeUtils.convertStringToDateObject(planToDate, generalPattern);
        toCalendar.setTime(dateTo);
        if (currentMonth.get(Calendar.MONTH) <= toCalendar.get(Calendar.MONTH)) {
            if (toCalendar.get(Calendar.YEAR) != currentMonth.get(Calendar.YEAR)) {
                if (toCalendar.get(Calendar.MONTH) - currentMonth.get(Calendar.MONTH) <= 0) {
                    calculateDayOfWeek();
                } else {
                    currentMonth.add(Calendar.WEEK_OF_YEAR, -1);
                    getIvyView().showMessage(R.string.endOfPeriod);
                }
            } else
                calculateDayOfWeek();
        } else {
            currentMonth.add(Calendar.WEEK_OF_YEAR, -1);
            getIvyView().showMessage(R.string.endOfPeriod);
        }
    }

    @Override
    public void onPreviousWeekClicked() {
        currentMonth.add(Calendar.WEEK_OF_YEAR, -1);
        Calendar fromCalendar = Calendar.getInstance();
        Date dateTo = DateTimeUtils.convertStringToDateObject(planFromDate, generalPattern);
        fromCalendar.setTime(dateTo);
        if (currentMonth.get(Calendar.MONTH) >= fromCalendar.get(Calendar.MONTH)) {
            if (fromCalendar.get(Calendar.YEAR) != currentMonth.get(Calendar.YEAR)) {
                if (fromCalendar.get(Calendar.MONTH) - currentMonth.get(Calendar.MONTH) >= 0)
                    calculateDayOfWeek();
                else {
                    currentMonth.add(Calendar.WEEK_OF_YEAR, +1);
                    getIvyView().showMessage(R.string.endOfPeriod);
                }
            } else
                calculateDayOfWeek();
        } else {
            currentMonth.add(Calendar.WEEK_OF_YEAR, +1);
            getIvyView().showMessage(R.string.endOfPeriod);
        }
    }

    @Override
    public void setSelectedDate(String selectedDate) {
        mSelectedDate = selectedDate;
        currentMonth.setTime(DateTimeUtils.convertStringToDateObject(mSelectedDate, generalPattern));
        getIvyView().updateSelectedDate(selectedDate);
    }

    private void calculateDayOfWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DateTimeUtils.convertStringToDateObject(mSelectedDate, generalPattern));
        currentMonth.set(Calendar.DAY_OF_WEEK, calendar.get(Calendar.DAY_OF_WEEK));
        setSelectedDate(DateTimeUtils.convertDateObjectToRequestedFormat(currentMonth.getTime(), generalPattern));
        // loadADay();
    }

    @Override
    public void loadADay() {
       /* ArrayList<CalenderBO> mCalendarList = getWeekDays();

        //adding week no at start position
        CalenderBO cBO = new CalenderBO();
        cBO.setDay("WK");
        cBO.setWeekDate("1");   // need to replace week no from DB
        mCalendarList.add(0, cBO);

        getIvyView().loadDayView(mAllowedDates, mCalendarList);

        //updating month name header view
        String firstDaysMonth = DateTimeUtils.convertDateTimeObjectToRequestedFormat(mCalendarList.get(1).getCal_date(), generalPattern, "MMM");
        String lastDaysMonth = DateTimeUtils.convertDateTimeObjectToRequestedFormat(mCalendarList.get(7).getCal_date(), generalPattern, "MMM");
        if (firstDaysMonth.equals(lastDaysMonth))
            getIvyView().setMonthName(DateTimeUtils.convertDateObjectToRequestedFormat(
                    currentMonth.getTime(), "MMM yyyy"));
        else
            getIvyView().setMonthName(firstDaysMonth + "-" + lastDaysMonth + " " +
                    DateTimeUtils.convertDateObjectToRequestedFormat(currentMonth.getTime(), "yyyy"));*/
        Calendar date = Calendar.getInstance();
        date.setTime(DateTimeUtils.convertStringToDateObject(mSelectedDate, generalPattern));

        getIvyView().loadDayView(date);

    }

    @Override
    public void loadAWeek() {
        Calendar date = Calendar.getInstance();
        date.setTime(DateTimeUtils.convertStringToDateObject(mSelectedDate, generalPattern));
        date.setFirstDayOfWeek(Calendar.MONDAY);
        date.set(Calendar.DAY_OF_WEEK, date.getFirstDayOfWeek());
        getIvyView().loadWeekView(date);
    }

    @Override
    public void loadDayTimeSlots() {
        getIvyView().loadTimeSlotList(timeSlots(mSelectedDate));

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
            SimpleDateFormat df1 = new SimpleDateFormat("d", Locale.US);
            String d = df1.format(result);
            SimpleDateFormat outFormat = new SimpleDateFormat("EE", Locale.getDefault());
            String goal = outFormat.format(result);
            startWeek.add(Calendar.DATE, 1);
            cBO.setCal_date(DateTimeUtils.convertDateObjectToRequestedFormat(result, generalPattern));
            cBO.setDay(goal);
            cBO.setWeekDate(d);
            cBO.setToday(DateUtils.isToday(result.getTime()));
            cBO.setSelected(mSelectedDate.equalsIgnoreCase(cBO.getCal_date()));
            calLsit.add(cBO);
        }
        return calLsit;

    }

    //test
    private ArrayList<TimeSlotsBo> timeSlots(String dateStr) {
        ArrayList<TimeSlotsBo> timeSlotList = new ArrayList<>();
        try {
            Date date = DateTimeUtils.convertStringToDateObject(dateStr, "yyyy/MM/dd");

            Calendar startCalendar = Calendar.getInstance();
            Calendar endCalendar = Calendar.getInstance();

            startCalendar.setTime(date);
            startCalendar.set(Calendar.HOUR_OF_DAY, 0);
            startCalendar.set(Calendar.MINUTE, 0);
            startCalendar.set(Calendar.SECOND, 0);
            startCalendar.set(Calendar.MILLISECOND, 0);

            endCalendar.setTime(date);
            endCalendar.set(Calendar.HOUR_OF_DAY, 23);
            endCalendar.set(Calendar.MINUTE, 59);
            endCalendar.set(Calendar.SECOND, 59);
            endCalendar.set(Calendar.MILLISECOND, 999);

            while (DateTimeUtils.isFutureDate(endCalendar, startCalendar)) {
                TimeSlotsBo timeSlotsBo = new TimeSlotsBo();
                timeSlotsBo.setTime(DateTimeUtils.convertDateObjectToRequestedFormat(startCalendar.getTime(), "H:mm"));
                timeSlotList.add(timeSlotsBo);
                startCalendar.add(Calendar.MINUTE, 60); // configurable time slots
            }

        } catch (Exception e) {
            Commons.printException(e);
        }

        return timeSlotList;

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
