package com.ivy.ui.retailerplan.calendar.presenter;

import com.ivy.calendarlibrary.weekview.WeekViewEvent;
import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.ui.retailer.viewretailers.data.RetailerDataManager;
import com.ivy.ui.retailerplan.addplan.DateWisePlanBo;
import com.ivy.ui.retailerplan.addplan.data.AddPlanDataManager;
import com.ivy.ui.retailerplan.calendar.CalendarPlanContract;
import com.ivy.ui.retailerplan.calendar.bo.CalenderBO;
import com.ivy.ui.retailerplan.calendar.bo.PeriodBo;
import com.ivy.ui.retailerplan.calendar.data.CalendarPlanDataManager;
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
import java.util.Objects;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

import static com.ivy.ui.retailer.RetailerConstants.CANCELLED;
import static com.ivy.ui.retailer.RetailerConstants.COMPLETED;
import static com.ivy.ui.retailer.RetailerConstants.PLANNED;
import static com.ivy.utils.DateTimeUtils.DATE_GLOBAL;

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
    private ArrayList<DateWisePlanBo> selectedDateRetailerPlanList;
    private HashMap<String, DateWisePlanBo> selectedDateRetailerPlanMap;
    private AddPlanDataManager addPlanDataManager;
    private com.ivy.core.data.retailer.RetailerDataManager coreRetailerDataManager;
    private int startMonthDiff, endMonthDiff;
    private List<PeriodBo> periodList = new ArrayList<>();
    private List<PeriodBo> weekList = new ArrayList<>();
    private List<String> dateList = new ArrayList<>();
    private Calendar mStartDay;
    private ConfigurationMasterHelper configurationMasterHelper;

    @Inject
    CalendarPlanPresenterImpl(DataManager dataManager,
                              SchedulerProvider schedulerProvider,
                              CompositeDisposable compositeDisposable,
                              ConfigurationMasterHelper configurationMasterHelper,
                              V view,
                              CalendarPlanDataManager calendarPlanDataManager,
                              RetailerDataManager retailerDataManager,
                              com.ivy.core.data.retailer.RetailerDataManager coreRetailerDataManager,
                              AddPlanDataManager addPlanDataManager) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, view);
        this.calendarPlanDataManager = calendarPlanDataManager;
        this.retailerDataManager = retailerDataManager;
        this.addPlanDataManager = addPlanDataManager;
        this.coreRetailerDataManager = coreRetailerDataManager;
        this.configurationMasterHelper = configurationMasterHelper;

    }


    @Override
    public void fetchEventsFromDb(boolean onCreate) {
        getIvyView().showLoading();
        getCompositeDisposable().add(retailerDataManager.getAllDateRetailerPlanList()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(listHashMap -> {
                    plannedListMap = listHashMap;
                    updateEvents(onCreate);
                }));
    }

    private void updateEvents(boolean onCreate) {
        getCompositeDisposable().add(getEvents()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(events -> {
                    getIvyView().hideLoading();
                    allEvents.clear();
                    allEvents.addAll(events);
                    if (onCreate) {
                        setPlanDates();
                    } else {
                        getIvyView().reloadView();
                    }
                }));
    }


    private Single<List<WeekViewEvent>> getEvents() {
        return Single.fromCallable(() -> {
            List<WeekViewEvent> events = new ArrayList<>();
            for (Object o : plannedListMap.entrySet()) {
                Map.Entry pair = (Map.Entry) o;
                for (DateWisePlanBo dateWisePlanBo : (ArrayList<DateWisePlanBo>) pair.getValue()) {
                    if (dateWisePlanBo.getStartTime().length() > 0 && dateWisePlanBo.getEndTime().length() > 0) {
                        Calendar startTime = Calendar.getInstance();
                        startTime.setTime(DateTimeUtils.convertStringToDateObject(dateWisePlanBo.getDate() + " " +
                                dateWisePlanBo.getStartTime(), "yyyy/MM/dd HH:mm"));
                        Calendar endTime = (Calendar) startTime.clone();
                        endTime.setTime(DateTimeUtils.convertStringToDateObject(dateWisePlanBo.getDate() + " " +
                                dateWisePlanBo.getEndTime(), "yyyy/MM/dd HH:mm"));
                        RetailerMasterBO retailerMasterBO = getPlanedRetailerBo("" + dateWisePlanBo.getEntityId());
                        if (retailerMasterBO != null) {
                            WeekViewEvent event = new WeekViewEvent(dateWisePlanBo.getPlanId(), dateWisePlanBo.getName(), retailerMasterBO.getAddress1(), startTime, endTime);

                            if (dateWisePlanBo.getVisitStatus().equalsIgnoreCase(COMPLETED))
                                event.setColor(getIvyView().getColorCode(1));
                            else if ("Y".equals(retailerMasterBO.getIsVisited()) && retailerMasterBO.getIsToday() == 1)
                                event.setColor(getIvyView().getColorCode(1));
                            else if (dateWisePlanBo.getVisitStatus().equalsIgnoreCase(PLANNED))
                                event.setColor(getIvyView().getColorCode(2));
                            if (dateWisePlanBo.getCancelReasonId() > 0 && dateWisePlanBo.getVisitStatus().equalsIgnoreCase(CANCELLED))
                                event.setColor(getIvyView().getColorCode(3));
                            else if ("P".equals(retailerMasterBO.getIsVisited()))
                                event.setColor(getIvyView().getColorCode(4));

                            event.setRetailerId(retailerMasterBO.getRetailerID());
                            events.add(event);
                        }
                    }
                }
            }
            return events;
        });
    }

    /*
        To Set initial Plan dates
         */
    @Override
    public void setPlanDates() {
        getIvyView().showLoading();
        currentMonth = Calendar.getInstance();
        setSelectedDate(DateTimeUtils.convertDateObjectToRequestedFormat(currentMonth.getTime(), generalPattern));

        getCompositeDisposable().add(Observable.zip(calendarPlanDataManager.loadAllowedDates(),
                calendarPlanDataManager.loadPeriods(),
                calendarPlanDataManager.loadWeekData(),
                (dateList, periodList, weekList) -> {
                    this.dateList = dateList;
                    this.periodList = periodList;
                    this.weekList = weekList;

                    return true;
                }).subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<Object>() {
                    @Override
                    public void onNext(Object o) {
                        if (!dateList.isEmpty()) {

                            Calendar aCalendar = Calendar.getInstance();
                            aCalendar.setTime(DateTimeUtils.convertStringToDateObject(dateList.get(0), generalPattern));
                            planFromDate = dateList.get(0);
                            Commons.print("planFromDate" + planFromDate);

                            Calendar zCalendar = Calendar.getInstance();
                            zCalendar.setTime(DateTimeUtils.convertStringToDateObject(dateList.get(1), generalPattern));
                            planToDate = dateList.get(1);
                            Commons.print("planFromDate" + planToDate);


                        } else {
                            Calendar aCalendar = Calendar.getInstance();
                            aCalendar.add(Calendar.MONTH, -startMonthDiff);
                            aCalendar.set(Calendar.DATE, 1);
                            planFromDate = DateTimeUtils.convertDateObjectToRequestedFormat(aCalendar.getTime(), generalPattern);
                            Commons.print("planFromDate" + planFromDate);

                            Calendar zCalendar = Calendar.getInstance();
                            zCalendar.add(Calendar.MONTH, endMonthDiff);
                            zCalendar.set(Calendar.DATE, zCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                            planToDate = DateTimeUtils.convertDateObjectToRequestedFormat(zCalendar.getTime(), generalPattern);
                            Commons.print("planFromDate" + planToDate);
                        }


                        mStartDay = Calendar.getInstance();
                        if (!weekList.isEmpty())
                            mStartDay.setTime(DateTimeUtils.convertStringToDateObject(weekList.get(0).getStartDate(), generalPattern));
                        else {
                            // mStartDay.setFirstDayOfWeek(Calendar.MONDAY);
                            mStartDay.set(Calendar.DAY_OF_WEEK, mStartDay.getFirstDayOfWeek());
                        }

                        udpateMonthDayText();
                        setAllowedDates();
                        getIvyView().hideLoading();
                        loadCalendar();
                    }

                    @Override
                    public void onError(Throwable e) {
                        getIvyView().onError("Something went wrong");
                        getIvyView().hideLoading();
                    }

                    @Override
                    public void onComplete() {

                    }
                }));

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
        List<String> weekNoList = getAMonthsWeekNoList(calendarDate[0], calendarDate[1]);
        getIvyView().loadCalendarView(mAllowedDates, dayInWeekCount, mCalenderAllList, weekNoList);
        getIvyView().setMonthName(DateTimeUtils.convertDateObjectToRequestedFormat(
                currentMonth.getTime(), "MMM yyyy") + ", " + getPeriodName());
        getIvyView().loadRetailerInfoBtmSheet(getADayPlan(mSelectedDate));
    }

    @Override
    public void loadADay() {
        ArrayList<CalenderBO> mCalendarList = getWeekDays();

        getIvyView().loadTopWeekView(mCalendarList, mAllowedDates);
        Calendar date = Calendar.getInstance();
        date.setTime(DateTimeUtils.convertStringToDateObject(mSelectedDate, generalPattern));
        getIvyView().loadDayView(date);
        getIvyView().setMonthName(DateTimeUtils.convertDateObjectToRequestedFormat(
                currentMonth.getTime(), "MMM yyyy") + ", " + getPeriodName());
    }

    @Override
    public void loadAWeek() {
        //data for top view horizonatal list

        ArrayList<CalenderBO> mCalendarList = getWeekDays();

        getIvyView().loadTopWeekView(mCalendarList, mAllowedDates);

        Calendar date = Calendar.getInstance();
        date.setTime(DateTimeUtils.convertStringToDateObject(mSelectedDate, generalPattern));
        date.setFirstDayOfWeek(mStartDay.get(Calendar.DAY_OF_WEEK));
        date.set(Calendar.DAY_OF_WEEK, date.getFirstDayOfWeek());
        getIvyView().loadWeekView(date);
        getIvyView().setMonthName(DateTimeUtils.convertDateObjectToRequestedFormat(
                currentMonth.getTime(), "MMM yyyy") + ", " + getPeriodName());
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
                    setSelectedDate(DateTimeUtils.convertDateObjectToRequestedFormat(currentMonth.getTime(), generalPattern));
                    loadCalendar();
                } else {
                    currentMonth.add(Calendar.MONTH, -1);
                    getIvyView().showMessage(R.string.endOfPeriod);
                }
            } else {
                setSelectedDate(DateTimeUtils.convertDateObjectToRequestedFormat(currentMonth.getTime(), generalPattern));
                loadCalendar();
            }
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
                if (fromCalendar.get(Calendar.MONTH) - currentMonth.get(Calendar.MONTH) >= 0) {
                    setSelectedDate(DateTimeUtils.convertDateObjectToRequestedFormat(currentMonth.getTime(), generalPattern));
                    loadCalendar();
                } else {
                    currentMonth.add(Calendar.MONTH, +1);
                    getIvyView().showMessage(R.string.endOfPeriod);
                }
            } else {
                setSelectedDate(DateTimeUtils.convertDateObjectToRequestedFormat(currentMonth.getTime(), generalPattern));
                loadCalendar();
            }
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
        List<DateWisePlanBo> planList = new ArrayList<>();
        if (plannedListMap.get(date) != null)
            for (DateWisePlanBo dateWisePlanBo : Objects.requireNonNull(plannedListMap.get(date))) {
                if (!dateWisePlanBo.getVisitStatus().equals(CANCELLED))
                    planList.add(dateWisePlanBo);
            }

        return planList;
    }

    @Override
    public List<WeekViewEvent> getPlannedEvents(int newYear, int newMonth) {
        List<WeekViewEvent> matchedEvents = new ArrayList<>();
        for (WeekViewEvent event : allEvents) {
            if (eventMatches(event, newYear, newMonth)) {
                matchedEvents.add(event);
            }
        }
        return matchedEvents;
    }

    @Override
    public String getSelectedDate() {
        return mSelectedDate;
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

        if (mStartDay.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY)
            count = c.get(Calendar.DAY_OF_WEEK) - 1;
        else
            count = c.get(Calendar.DAY_OF_WEEK);

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
            cBO.setToday(DateTimeUtils.isToday(result.getTime()));
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
        startWeek.setFirstDayOfWeek(mStartDay.get(Calendar.DAY_OF_WEEK));
        startWeek.set(Calendar.DAY_OF_WEEK, startWeek.getFirstDayOfWeek());
        setTimeToBeginningOfDay(startWeek);


        Calendar endWeek = Calendar.getInstance();
        endWeek.setTime(DateTimeUtils.convertStringToDateObject(mSelectedDate, generalPattern));
        endWeek.set(Calendar.DAY_OF_WEEK, startWeek.getFirstDayOfWeek());
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
            cBO.setToday(DateTimeUtils.isToday(result.getTime()));
            cBO.setSelected(mSelectedDate.equalsIgnoreCase(cBO.getCal_date()));
            calLsit.add(cBO);
        }
        //adding week no at start position
        CalenderBO cBO = new CalenderBO();
        cBO.setDay("WK");
        cBO.setWeekDate(getWeekNo(mSelectedDate));
        calLsit.add(0, cBO);

        return calLsit;

    }

    @Override
    public void fetchSelectedDateRetailerPlan(String date, RetailerMasterBO retailerMasterBO) {
        getIvyView().showLoading();
        getCompositeDisposable().add(retailerDataManager.getRetailerPlanList(date)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(listHashMap -> {
                    selectedDateRetailerPlanMap = listHashMap;
                    selectedDateRetailerPlanList = new ArrayList<>(listHashMap.values());
                    getIvyView().hideLoading();
                    getIvyView().loadAddPlanDialog(date, retailerMasterBO);
                }));
    }

    @Override
    public ArrayList<DateWisePlanBo> getSelectedDateRetailerPlanList() {
        return selectedDateRetailerPlanList.isEmpty() ? new ArrayList<>() : selectedDateRetailerPlanList;
    }

    @Override
    public DateWisePlanBo getSelectedRetailerPlan(String retailerId) {
        return selectedDateRetailerPlanMap.get(retailerId);
    }

    @Override
    public RetailerMasterBO getPlanedRetailerBo(String retailerId) {
        RetailerMasterBO retailerMasterBO = null;
        for (RetailerMasterBO rBo : getDataManager().getRetailerMasters()) {
            if (rBo.getRetailerID().equalsIgnoreCase(retailerId)) {
                retailerMasterBO = rBo;
                break;
            }
        }
        return retailerMasterBO;
    }

    @Override
    public boolean isPastDate(String selectedDate) {
        int difference = DateTimeUtils.getDateCount(selectedDate, DateTimeUtils.now(DATE_GLOBAL), "yyyy/MM/dd");
        return difference > 0;
    }

    @Override
    public long getMaxPlanDate() {
        return Objects.requireNonNull(DateTimeUtils.convertStringToDateObject(planToDate, generalPattern)).getTime();
    }

    @Override
    public void setRetailerMasterBo(RetailerMasterBO retailerMasterBo) {
        getDataManager().setRetailerMaster(retailerMasterBo);
    }

    @Override
    public List<PeriodBo> getWeekList() {
        return weekList;
    }

    @Override
    public int getWeeksPlanCount(String weekNo) {
        int count = 0;
        if (weekList.size() > 0 && weekNo.length() > 0) {
            Calendar startWeek = Calendar.getInstance();
            startWeek.setTime(DateTimeUtils.convertStringToDateObject(getWeekStartDate(weekNo), generalPattern));
            startWeek.setFirstDayOfWeek(mStartDay.get(Calendar.DAY_OF_WEEK));
            startWeek.set(Calendar.DAY_OF_WEEK, startWeek.getFirstDayOfWeek());
            setTimeToBeginningOfDay(startWeek);


            Calendar endWeek = Calendar.getInstance();
            endWeek.setTime(DateTimeUtils.convertStringToDateObject(getWeekStartDate(weekNo), generalPattern));
            endWeek.set(Calendar.DAY_OF_WEEK, startWeek.getFirstDayOfWeek());
            endWeek.add(Calendar.DATE, 7);
            setTimeToEndOfDay(endWeek);

            while (startWeek.getTime().before(endWeek.getTime())) {
                Date result = startWeek.getTime();
                count += getADayPlan(DateTimeUtils.convertDateObjectToRequestedFormat(result, generalPattern)).size();
                startWeek.add(Calendar.DATE, 1);
            }
        }

        return count;
    }

    private void updateIsToday() {
        getCompositeDisposable().add(coreRetailerDataManager.updateIsToday()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe((aBoolean, throwable) -> {

                }));
    }

    private String getPeriodName() {
        String peroidName = "";
        if (periodList.size() > 0) {
            Date selectedDate = DateTimeUtils.convertStringToDateObject(mSelectedDate, generalPattern);
            for (PeriodBo periodBo : periodList) {
                assert selectedDate != null;
                if (selectedDate.compareTo(DateTimeUtils.convertStringToDateObject(periodBo.getStartDate(), generalPattern)) >= 0
                        && selectedDate.compareTo(DateTimeUtils.convertStringToDateObject(periodBo.getEndDate(), generalPattern)) <= 0) {
                    peroidName = periodBo.getDescription();
                    break;
                }
            }
        }
        return peroidName;
    }

    @Override
    public String getWeekNo(String dateOfWeek) {
        String weekNo = "";
        if (weekList.size() > 0) {
            Date selectedDate = DateTimeUtils.convertStringToDateObject(dateOfWeek, generalPattern);
            for (PeriodBo periodBo : weekList) {
                assert selectedDate != null;
                if (selectedDate.compareTo(DateTimeUtils.convertStringToDateObject(periodBo.getStartDate(), generalPattern)) >= 0
                        && selectedDate.compareTo(DateTimeUtils.convertStringToDateObject(periodBo.getEndDate(), generalPattern)) <= 0) {
                    weekNo = periodBo.getDescription();
                    break;
                }
            }
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(DateTimeUtils.convertStringToDateObject(dateOfWeek, generalPattern));
            weekNo = "" + calendar.get(Calendar.WEEK_OF_YEAR);
        }
        return weekNo;
    }

    //to create week no list from current week no
    @Override
    public List<String> getWeekNoList() {
        List<String> weekNameList = new ArrayList<>();
        if (weekList.size() > 0) {
            Date selectedDate = new Date();
            for (PeriodBo periodBo : weekList) {
                if (weekNameList.size() > 0)
                    weekNameList.add(periodBo.getDescription());
                if (selectedDate.compareTo(DateTimeUtils.convertStringToDateObject(periodBo.getStartDate(), generalPattern)) >= 0
                        && selectedDate.compareTo(DateTimeUtils.convertStringToDateObject(periodBo.getEndDate(), generalPattern)) <= 0)
                    weekNameList.add(periodBo.getDescription());
            }
        }
        return weekNameList;
    }

    private String getWeekStartDate(String weekNo) {
        String startDate = "";
        for (PeriodBo periodBo : weekList) {
            if (periodBo.getDescription().equals(weekNo)) {
                startDate = periodBo.getStartDate();
                break;
            }
        }
        return startDate;
    }

    private Single<List<DateWisePlanBo>> getAWeekPlan(String toWeekNo) {
        return Single.fromCallable(() -> {
            List<DateWisePlanBo> deleteList = new ArrayList<>();
            if (weekList.size() > 0 && toWeekNo.length() > 0) {
                Calendar startWeek = Calendar.getInstance();
                startWeek.setTime(DateTimeUtils.convertStringToDateObject(getWeekStartDate(toWeekNo), generalPattern));
                startWeek.setFirstDayOfWeek(mStartDay.get(Calendar.DAY_OF_WEEK));
                startWeek.set(Calendar.DAY_OF_WEEK, startWeek.getFirstDayOfWeek());
                setTimeToBeginningOfDay(startWeek);


                Calendar endWeek = Calendar.getInstance();
                endWeek.setTime(DateTimeUtils.convertStringToDateObject(getWeekStartDate(toWeekNo), generalPattern));
                endWeek.set(Calendar.DAY_OF_WEEK, startWeek.getFirstDayOfWeek());
                endWeek.add(Calendar.DATE, 7);
                setTimeToEndOfDay(endWeek);

                while (startWeek.getTime().before(endWeek.getTime())) {
                    Date result = startWeek.getTime();
                    deleteList.addAll(deleteList.size(), getADayPlan(DateTimeUtils.convertDateObjectToRequestedFormat(result, generalPattern)));
                    startWeek.add(Calendar.DATE, 1);
                }
            }
            return deleteList;
        });
    }

    private Single<List<DateWisePlanBo>> getListToCopy(String fromWeekNo, String toWeekNo) {
        return Single.fromCallable(() -> {
            List<DateWisePlanBo> copyList = new ArrayList<>();
            if (weekList.size() > 0 && fromWeekNo.length() > 0 && toWeekNo.length() > 0) {
                Calendar startWeek = Calendar.getInstance();
                startWeek.setTime(DateTimeUtils.convertStringToDateObject(getWeekStartDate(fromWeekNo), generalPattern));
                startWeek.setFirstDayOfWeek(mStartDay.get(Calendar.DAY_OF_WEEK));
                startWeek.set(Calendar.DAY_OF_WEEK, startWeek.getFirstDayOfWeek());
                setTimeToBeginningOfDay(startWeek);


                Calendar endWeek = Calendar.getInstance();
                endWeek.setTime(DateTimeUtils.convertStringToDateObject(getWeekStartDate(fromWeekNo), generalPattern));
                endWeek.set(Calendar.DAY_OF_WEEK, startWeek.getFirstDayOfWeek());
                endWeek.add(Calendar.DATE, 7);
                setTimeToEndOfDay(endWeek);

                Calendar copyWeek = Calendar.getInstance();
                copyWeek.setTime(DateTimeUtils.convertStringToDateObject(getWeekStartDate(toWeekNo), generalPattern));
                copyWeek.setFirstDayOfWeek(mStartDay.get(Calendar.DAY_OF_WEEK));
                copyWeek.set(Calendar.DAY_OF_WEEK, copyWeek.getFirstDayOfWeek());
                setTimeToBeginningOfDay(copyWeek);

                while (startWeek.getTime().before(endWeek.getTime())) {
                    Date result = startWeek.getTime();
                    Date toCopyDate = copyWeek.getTime();
                    for (DateWisePlanBo dateWisePlanBo : getADayPlan(DateTimeUtils.convertDateObjectToRequestedFormat(result, generalPattern))) {
                        dateWisePlanBo.setDate(DateTimeUtils.convertDateObjectToRequestedFormat(toCopyDate, generalPattern));
                        copyList.add(dateWisePlanBo);
                    }
                    startWeek.add(Calendar.DATE, 1);
                    copyWeek.add(Calendar.DATE, 1);
                }
            }
            return copyList;
        });
    }

    @Override
    public void copyPlan(String fromDate, String toDate) {
        getIvyView().showLoading();
        List<DateWisePlanBo> planList = getADayPlan(fromDate);
        getCompositeDisposable().add(addPlanDataManager.copyPlan(planList, toDate, getDataManager().getUser().getUserid())
                .flatMap(aBoolean -> coreRetailerDataManager.updatePlanVisitCount(planList))
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe((aBoolean, throwable) ->
                {
                    if (DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL).equalsIgnoreCase(toDate))
                        updateIsToday();
                    getIvyView().hideLoading();
                    fetchEventsFromDb(false);
                }));
    }

    @Override
    public void movePlan(String fromDate, String toDate, String reasonId) {
        getIvyView().showLoading();
        List<DateWisePlanBo> planList = getADayPlan(fromDate);
        getCompositeDisposable().add(addPlanDataManager.movePlan(planList, toDate, reasonId, getDataManager().getUser().getUserid())
                .flatMap(aBoolean -> coreRetailerDataManager.updatePlanVisitCount(planList))
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe((aBoolean, throwable) ->
                {
                    if (DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL).equalsIgnoreCase(toDate))
                        updateIsToday();
                    getIvyView().hideLoading();
                    fetchEventsFromDb(false);
                }));

    }

    @Override
    public void copyWeekPlan(String fromDate, String toDate) {
        getIvyView().showLoading();
        getCompositeDisposable().add(getListToCopy(fromDate, toDate)
                .flatMap(planList -> addPlanDataManager.copyPlan(planList, getDataManager().getUser().getUserid()))
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(aBoolean -> {
                    updateIsToday();
                    fetchEventsFromDb(false);
                }));
    }

    @Override
    public void moveWeekPlan(String fromDate, String toDate, String reasonId) {
        getIvyView().showLoading();
        getCompositeDisposable().add(getListToCopy(fromDate, toDate)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(toPlanList -> {
                    moveWeekPlan(toPlanList, reasonId);
                }));

    }

    private void moveWeekPlan(List<DateWisePlanBo> toPlanList, String reasonId) {
        getCompositeDisposable().add(getAWeekPlan(getWeekNo(mSelectedDate))
                .flatMap(fromPlanList -> addPlanDataManager.movePlan(fromPlanList, toPlanList, reasonId, getDataManager().getUser().getUserid()))
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(aBoolean -> {
                    updateIsToday();
                    fetchEventsFromDb(false);

                }));
    }

    @Override
    public void loadConfiguration() {
        getCompositeDisposable().add(calendarPlanDataManager.loadConfiguration()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe((integer, throwable) -> {
                    startMonthDiff = integer;
                    endMonthDiff = integer;
                }));
    }

    @Override
    public void cancelPlans(String reasonId) {
        getIvyView().showLoading();
        getCompositeDisposable().add(addPlanDataManager.cancelPlan(getADayPlan(mSelectedDate), reasonId)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(aBoolean -> {
                    fetchEventsFromDb(false);
                }));
    }

    @Override
    public void cancelWeekPlans(String reasonId) {

        getIvyView().showLoading();
        getCompositeDisposable().add(getAWeekPlan(getWeekNo(mSelectedDate))
                .flatMap(dateWisePlanBos -> addPlanDataManager.cancelPlan(dateWisePlanBos, reasonId))
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe((aBoolean, throwable) -> {
                    fetchEventsFromDb(false);
                }));
    }

    @Override
    public boolean isCurrentDay() {
        Calendar selectedDate = Calendar.getInstance();
        selectedDate.setTime(DateTimeUtils.convertStringToDateObject(mSelectedDate, generalPattern));
        return DateTimeUtils.isToday(selectedDate.getTimeInMillis());
    }

    @Override
    public boolean isCurrentWeek() {
        Calendar startWeek = Calendar.getInstance();
        startWeek.setTime(DateTimeUtils.convertStringToDateObject(mSelectedDate, generalPattern));
        startWeek.setFirstDayOfWeek(mStartDay.get(Calendar.DAY_OF_WEEK));
        startWeek.set(Calendar.DAY_OF_WEEK, startWeek.getFirstDayOfWeek());
        setTimeToBeginningOfDay(startWeek);


        Calendar endWeek = Calendar.getInstance();
        endWeek.setTime(DateTimeUtils.convertStringToDateObject(mSelectedDate, generalPattern));
        endWeek.set(Calendar.DAY_OF_WEEK, startWeek.getFirstDayOfWeek());
        endWeek.add(Calendar.DATE, 7);
        setTimeToEndOfDay(endWeek);

        while (startWeek.getTime().before(endWeek.getTime())) {
            boolean result = DateTimeUtils.isToday(startWeek.getTimeInMillis());
            if (result)
                return true;
            startWeek.add(Calendar.DATE, 1);
        }

        return false;
    }

    private List<String> getAMonthsWeekNoList(String startDate, String endDate) {
        List<String> weekNoList = new ArrayList<>();
        Date startdate = DateTimeUtils.convertStringToDateObject(startDate, generalPattern);
        Date enddate;
        enddate = addDateNew(endDate);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startdate);

        Calendar startDayOfWeek = Calendar.getInstance();
        startDayOfWeek.setTime(startdate);

        while (calendar.getTime().before(enddate) || startDayOfWeek.getTime().before(enddate)) {
            weekNoList.add(getWeekNo(DateTimeUtils.convertDateObjectToRequestedFormat(calendar.getTime(), generalPattern)));
            calendar.add(Calendar.DATE, 7);
            startDayOfWeek = (Calendar) calendar.clone();
            startDayOfWeek.set(Calendar.DAY_OF_WEEK, mStartDay.getFirstDayOfWeek());

        }
        return weekNoList;
    }

    private void udpateMonthDayText() {
        List<String> dayTextList = new ArrayList<>();
        Calendar startWeek = (Calendar) mStartDay.clone();
        setTimeToBeginningOfDay(startWeek);


        Calendar endWeek = (Calendar) mStartDay.clone();
        endWeek.add(Calendar.DATE, 7);
        setTimeToEndOfDay(endWeek);

        while (startWeek.getTime().before(endWeek.getTime())) {
            Date result = startWeek.getTime();
            SimpleDateFormat outFormat = new SimpleDateFormat("EE", Locale.getDefault());
            String goal = outFormat.format(result);
            dayTextList.add(goal);
            startWeek.add(Calendar.DATE, 1);
        }

        getIvyView().setWeekDayText(dayTextList);

    }

    @Override
    public boolean showRescheduleFuture() {
        return configurationMasterHelper.ADD_PLAN_RESCHDULE_FS;
    }

    @Override
    public boolean showRescheduleReasonFuture() {
        return configurationMasterHelper.ADD_PLAN_RESCHDULE_FR;
    }

    @Override
    public boolean showCancelPlanReasonFuture() {
        return configurationMasterHelper.ADD_PLAN_CANCEL_FS;
    }

}
