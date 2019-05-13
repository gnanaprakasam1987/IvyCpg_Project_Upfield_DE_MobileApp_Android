package com.ivy.ui.retailerplan.calendar.presenter;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
import android.text.format.DateUtils;

import com.ivy.calendarlibrary.weekview.WeekViewEvent;
import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.ui.offlineplan.addplan.DateWisePlanBo;
import com.ivy.ui.offlineplan.calendar.CalendarPlanContract;
import com.ivy.ui.offlineplan.calendar.bo.CalenderBO;
import com.ivy.ui.offlineplan.calendar.data.CalendarPlanDataManager;
import com.ivy.ui.retailer.data.RetailerDataManager;
import com.ivy.ui.retailerplan.addplan.DateWisePlanBo;
import com.ivy.ui.retailerplan.calendar.CalendarPlanContract;
import com.ivy.ui.retailerplan.calendar.bo.CalenderBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.ui.retailerplan.calendar.data.CalendarPlanDataManager;
import com.ivy.ui.retailer.viewretailers.data.RetailerDataManager;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.rx.SchedulerProvider;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

/**
 * Created by mansoor on 27/03/2019
 */
public class CalendarPlanPresenterImpl<V extends CalendarPlanContract.CalendarPlanView> extends BasePresenter<V> implements CalendarPlanContract.CalendarPlanPresenter<V> {

    private CalendarPlanDataManager calendarPlanDataManager;
    private RetailerDataManager retailerDataManager;
    private String planFromDate, planToDate;
    private ArrayList<String> mAllowedDates;
    private String generalPattern = "yyyy/MM/dd";
    private Calendar currentMonth;
    private String mSelectedDate;
    private HashMap<String, List<DateWisePlanBo>> plannedListMap = new HashMap<>();
    private List<WeekViewEvent> allEvents = new ArrayList<>();

    @Inject
    CalendarPlanPresenterImpl(DataManager dataManager,
                              SchedulerProvider schedulerProvider,
                              CompositeDisposable compositeDisposable,
                              ConfigurationMasterHelper configurationMasterHelper,
                              V view,
                              CalendarPlanDataManager calendarPlanDataManager,
                              RetailerDataManager retailerDataManager) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, view);
        this.calendarPlanDataManager = calendarPlanDataManager;
        this.retailerDataManager = retailerDataManager;

    }


    @Override
    public void fetchEventsFromDb() {
        getIvyView().showLoading();
        getCompositeDisposable().add(retailerDataManager.getAllDateRetailerPlanList()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<HashMap<String, List<DateWisePlanBo>>>() {
                    @Override
                    public void accept(HashMap<String, List<DateWisePlanBo>> listHashMap) throws Exception {
                        plannedListMap = listHashMap;
                        updateEvents();
                    }
                }));
    }

    private void updateEvents() {
        getCompositeDisposable().add(getEvents()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<List<WeekViewEvent>>() {
                    @Override
                    public void accept(List<WeekViewEvent> events) throws Exception {
                        getIvyView().hideLoading();
                        allEvents.addAll(events);
                        setPlanDates();
                        loadCalendar();
                    }
                }));
    }


    private Single<List<WeekViewEvent>> getEvents() {
        return Single.fromCallable(new Callable<List<WeekViewEvent>>() {
            @Override
            public List<WeekViewEvent> call() throws Exception {
                List<WeekViewEvent> events = new ArrayList<>();
                for (Object o : plannedListMap.entrySet()) {
                    Map.Entry pair = (Map.Entry) o;
                    System.out.println(pair.getKey() + " = " + pair.getValue());
                    for (DateWisePlanBo dateWisePlanBo : (ArrayList<DateWisePlanBo>) pair.getValue()) {
                        Calendar startTime = Calendar.getInstance();
                        startTime.setTime(DateTimeUtils.convertStringToDateObject(dateWisePlanBo.getDate() + " " +
                                dateWisePlanBo.getStartTime(), "yyyy/MM/dd HH:mm"));
                        Calendar endTime = (Calendar) startTime.clone();
                        endTime.setTime(DateTimeUtils.convertStringToDateObject(dateWisePlanBo.getDate() + " " +
                                dateWisePlanBo.getEndTime(), "yyyy/MM/dd HH:mm"));
                        WeekViewEvent event = new WeekViewEvent(dateWisePlanBo.getPlanId(), dateWisePlanBo.getName(), "Retailer Address", startTime, endTime);
                        event.setColor(R.attr.colorPrimary);
                        events.add(event);
                    }
                }
                return events;
            }
        });
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
    public void loadADay() {
        ArrayList<CalenderBO> mCalendarList = getWeekDays();

        getIvyView().loadTopWeekView(mCalendarList, mAllowedDates);
        Calendar date = Calendar.getInstance();
        date.setTime(DateTimeUtils.convertStringToDateObject(mSelectedDate, generalPattern));
        getIvyView().loadDayView(date);
        getIvyView().setMonthName(DateTimeUtils.convertDateObjectToRequestedFormat(
                currentMonth.getTime(), "MMM yyyy"));

    }

    @Override
    public void loadAWeek() {
        //data for top view horizonatal list

        ArrayList<CalenderBO> mCalendarList = getWeekDays();

        getIvyView().loadTopWeekView(mCalendarList, mAllowedDates);

        Calendar date = Calendar.getInstance();
        date.setTime(DateTimeUtils.convertStringToDateObject(mSelectedDate, generalPattern));
        date.setFirstDayOfWeek(Calendar.MONDAY);
        date.set(Calendar.DAY_OF_WEEK, date.getFirstDayOfWeek());
        getIvyView().loadWeekView(date);
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
    public void onNextWeekClicked(boolean isDaySelected) {
        currentMonth.add(Calendar.WEEK_OF_YEAR, 1);
        Calendar toCalendar = Calendar.getInstance();
        Date dateTo = DateTimeUtils.convertStringToDateObject(planToDate, generalPattern);
        toCalendar.setTime(dateTo);
        if (currentMonth.get(Calendar.MONTH) <= toCalendar.get(Calendar.MONTH)) {
            if (toCalendar.get(Calendar.YEAR) != currentMonth.get(Calendar.YEAR)) {
                if (toCalendar.get(Calendar.MONTH) - currentMonth.get(Calendar.MONTH) <= 0) {
                    setSelectedDate(DateTimeUtils.convertDateObjectToRequestedFormat(currentMonth.getTime(), generalPattern));
                    if (isDaySelected)
                        loadADay();
                    else
                        loadAWeek();
                } else {
                    currentMonth.add(Calendar.WEEK_OF_YEAR, -1);
                    getIvyView().showMessage(R.string.endOfPeriod);
                }
            } else {
                setSelectedDate(DateTimeUtils.convertDateObjectToRequestedFormat(currentMonth.getTime(), generalPattern));
                if (isDaySelected)
                    loadADay();
                else
                    loadAWeek();
            }
        } else {
            currentMonth.add(Calendar.WEEK_OF_YEAR, -1);
            getIvyView().showMessage(R.string.endOfPeriod);
        }
    }

    @Override
    public void onPreviousWeekClicked(boolean isDaySelected) {
        currentMonth.add(Calendar.WEEK_OF_YEAR, -1);
        Calendar fromCalendar = Calendar.getInstance();
        Date dateTo = DateTimeUtils.convertStringToDateObject(planFromDate, generalPattern);
        fromCalendar.setTime(dateTo);
        if (currentMonth.get(Calendar.MONTH) >= fromCalendar.get(Calendar.MONTH)) {
            if (fromCalendar.get(Calendar.YEAR) != currentMonth.get(Calendar.YEAR)) {
                if (fromCalendar.get(Calendar.MONTH) - currentMonth.get(Calendar.MONTH) >= 0) {
                    setSelectedDate(DateTimeUtils.convertDateObjectToRequestedFormat(currentMonth.getTime(), generalPattern));
                    if (isDaySelected)
                        loadADay();
                    else
                        loadAWeek();
                } else {
                    currentMonth.add(Calendar.WEEK_OF_YEAR, +1);
                    getIvyView().showMessage(R.string.endOfPeriod);
                }
            } else {
                setSelectedDate(DateTimeUtils.convertDateObjectToRequestedFormat(currentMonth.getTime(), generalPattern));
                if (isDaySelected)
                    loadADay();
                else
                    loadAWeek();
            }
        } else {
            currentMonth.add(Calendar.WEEK_OF_YEAR, +1);
            getIvyView().showMessage(R.string.endOfPeriod);
        }
    }


    @Override
    public List<DateWisePlanBo> getADayPlan(String date) {
        return plannedListMap.get(date) != null
                ? plannedListMap.get(date) : new ArrayList<>();
    }

    @Override
    public List<WeekViewEvent> getPlannedEvents(int newYear, int newMonth) {
        List<WeekViewEvent> matchedEvents = new ArrayList<WeekViewEvent>();
        for (WeekViewEvent event : allEvents) {
            if (eventMatches(event, newYear, newMonth)) {
                matchedEvents.add(event);
            }
        }
        return matchedEvents;
    }

    private boolean eventMatches(WeekViewEvent event, int year, int month) {
        return (event.getStartTime().get(Calendar.YEAR) == year && event.getStartTime().get(Calendar.MONTH) == month - 1) || (event.getEndTime().get(Calendar.YEAR) == year && event.getEndTime().get(Calendar.MONTH) == month - 1);
    }


    @Override
    public void setSelectedDate(String selectedDate) {
        mSelectedDate = selectedDate;
        currentMonth.setTime(DateTimeUtils.convertStringToDateObject(mSelectedDate, generalPattern));
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
            cBO.setPlanList(getADayPlan(cBO.getCal_date()));
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
        //adding week no at start position
        CalenderBO cBO = new CalenderBO();
        cBO.setDay("WK");
        cBO.setWeekDate("" + startWeek.get(Calendar.WEEK_OF_YEAR));   // need to replace week no from DB
        calLsit.add(0, cBO);

        return calLsit;

    }

}
