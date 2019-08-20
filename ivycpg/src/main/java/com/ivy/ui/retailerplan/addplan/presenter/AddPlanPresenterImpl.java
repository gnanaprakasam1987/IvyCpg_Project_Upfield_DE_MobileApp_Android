package com.ivy.ui.retailerplan.addplan.presenter;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.core.data.retailer.RetailerDataManager;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.ui.retailerplan.addplan.AddPlanContract;
import com.ivy.ui.retailerplan.addplan.DateWisePlanBo;
import com.ivy.ui.retailerplan.addplan.data.AddPlanDataManager;
import com.ivy.ui.retailerplan.calendar.data.CalendarPlanDataManager;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.rx.SchedulerProvider;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.inject.Inject;

import io.reactivex.SingleSource;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

import static com.ivy.ui.retailer.RetailerConstants.CANCELLED;

public class AddPlanPresenterImpl<V extends AddPlanContract.AddPlanView> extends BasePresenter<V> implements AddPlanContract.AddPlanPresenter<V> {


    private final String mEntityRetailer = "RETAILER";
    private final String mEntityDistributor = "DIST";

    private String generalPattern = "yyyy/MM/dd";

    private AddPlanDataManager addPlanDataManager;

    private DataManager dataManager;

    private RetailerDataManager retailerDataManager;

    private com.ivy.ui.retailer.viewretailers.data.RetailerDataManager planRetailerDataManager;
    private ConfigurationMasterHelper configurationMasterHelper;

    private CalendarPlanDataManager calendarPlanDataManager;

    private String planEndDate;
    private final int WEEKLY = 1, MONTHLY = 2;

    @Inject
    public AddPlanPresenterImpl(DataManager dataManager, SchedulerProvider schedulerProvider
            , CompositeDisposable compositeDisposable, ConfigurationMasterHelper configurationMasterHelper
            , V view, AddPlanDataManager addPlanDataManager, RetailerDataManager retailerDataManager
            , CalendarPlanDataManager calendarPlanDataManager
            , com.ivy.ui.retailer.viewretailers.data.RetailerDataManager planRetailerDataManager) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, view);

        this.addPlanDataManager = addPlanDataManager;
        this.dataManager = dataManager;
        this.retailerDataManager = retailerDataManager;
        this.configurationMasterHelper = configurationMasterHelper;
        this.calendarPlanDataManager = calendarPlanDataManager;
        this.planRetailerDataManager = planRetailerDataManager;
    }

    @Override
    public void addNewPlan(String date, String startTime, String endTime, RetailerMasterBO retailerMasterBO, boolean isAdhoc) {

        DateWisePlanBo dateWisePlanBo = preparePlanObjects(date, startTime, endTime, retailerMasterBO, isAdhoc, "");

        getCompositeDisposable().add(addPlanDataManager.savePlan(dateWisePlanBo)
                .flatMapSingle(new Function<DateWisePlanBo, SingleSource<DateWisePlanBo>>() {
                    @Override
                    public SingleSource<DateWisePlanBo> apply(DateWisePlanBo planBo) throws Exception {
                        return retailerDataManager.updatePlanAndVisitCount(retailerMasterBO, planBo);
                    }
                }).subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<DateWisePlanBo>() {
                    @Override
                    public void accept(DateWisePlanBo planBo) throws Exception {
                        if (DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL).equalsIgnoreCase(planBo.getDate())) {
                            dataManager.getRetailMaster().setIsToday(1);
                            dataManager.getRetailMaster().setAdhoc(isAdhoc);
                        }

                        planBo.setOperationType("Add");

                        getIvyView().updateDatePlan(planBo);

                    }
                })
        );
    }

    @Override
    public void updatePlan(String startTime, String endTime, DateWisePlanBo planBo, String reasonId) {
        DateWisePlanBo dateWisePlanBo = updatePlanObjects(startTime, endTime, planBo);
        long planId = 0;

        planId = SDUtil.convertToLong(dataManager.getUser().getUserid()
                + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID));

        getCompositeDisposable().add(addPlanDataManager.updatePlan(dateWisePlanBo, reasonId, planId)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<DateWisePlanBo>() {
                    @Override
                    public void accept(DateWisePlanBo planBo) throws Exception {
                        planBo.setOperationType("Update");
                        getIvyView().updateDatePlan(planBo);
                    }
                })
        );
    }

    @Override
    public void cancelPlan(DateWisePlanBo dateWisePlanBo, RetailerMasterBO retailerMasterBO, String reasonId) {

        getCompositeDisposable().add(addPlanDataManager.cancelPlan(dateWisePlanBo, reasonId)
                .flatMapSingle(new Function<DateWisePlanBo, SingleSource<DateWisePlanBo>>() {
                    @Override
                    public SingleSource<DateWisePlanBo> apply(DateWisePlanBo planBo) throws Exception {
                        return retailerDataManager.updatePlanAndVisitCount(retailerMasterBO, planBo);
                    }
                }).subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<DateWisePlanBo>() {
                    @Override
                    public void accept(DateWisePlanBo planBo) throws Exception {
                        planBo.setOperationType("Delete");
                        getIvyView().updateDatePlan(planBo);
                    }
                })
        );

    }

    @Override
    public void deletePlan(DateWisePlanBo dateWisePlanBo, RetailerMasterBO retailerMasterBO, String reasonID) {

        getCompositeDisposable().add(addPlanDataManager.deletePlan(dateWisePlanBo, reasonID)
                .flatMapSingle(new Function<DateWisePlanBo, SingleSource<DateWisePlanBo>>() {
                    @Override
                    public SingleSource<DateWisePlanBo> apply(DateWisePlanBo planBo) throws Exception {
                        return retailerDataManager.updatePlanAndVisitCount(retailerMasterBO, planBo);
                    }
                }).subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<DateWisePlanBo>() {
                    @Override
                    public void accept(DateWisePlanBo planBo) throws Exception {
                        if (DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL).equalsIgnoreCase(planBo.getDate()))
                            dataManager.getRetailMaster().setIsToday(0);

                        planBo.setOperationType("Delete");
                        getIvyView().updateDatePlan(planBo);

                    }
                })
        );

    }

    private DateWisePlanBo preparePlanObjects(String date, String startTime, String endTime, RetailerMasterBO retailerMasterBO, boolean isAdhoc, String mode) {

        date = DateTimeUtils.convertToServerDateFormat(date, generalPattern);

        DateWisePlanBo dateWisePlanBo = new DateWisePlanBo();

        String id = dataManager.getUser().getUserid()
                + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);

        dateWisePlanBo.setPlanId(SDUtil.convertToLong(id));
        dateWisePlanBo.setDate(date);
        dateWisePlanBo.setDistributorId(retailerMasterBO.getDistributorId());
        dateWisePlanBo.setUserId(dataManager.getUser().getUserid());

        if (retailerMasterBO.getSubdId() == 0) {
            dateWisePlanBo.setEntityType(mEntityRetailer);
            dateWisePlanBo.setEntityId(SDUtil.convertToInt(retailerMasterBO.getRetailerID()));
        } else {
            dateWisePlanBo.setEntityType(mEntityDistributor);
            dateWisePlanBo.setEntityId(retailerMasterBO.getSubdId());
        }
        dateWisePlanBo.setStatus("I");
        dateWisePlanBo.setSequence(0);
        dateWisePlanBo.setStartTime(startTime);
        dateWisePlanBo.setEndTime(endTime);
        dateWisePlanBo.setName(retailerMasterBO.getRetailerName());
        dateWisePlanBo.setAdhoc(isAdhoc);
        dateWisePlanBo.setRecurringGroupMode(mode);

        return dateWisePlanBo;
    }

    private DateWisePlanBo updatePlanObjects(String startTime, String endTime, DateWisePlanBo planBo) {

        planBo.setStartTime(startTime);
        planBo.setEndTime(endTime);
        planBo.setRecurringGroupMode("");

        return planBo;
    }

    private DateWisePlanBo updatePlanObjects(String date, String startTime, String endTime, DateWisePlanBo planBo, String mode) {


        String id = dataManager.getUser().getUserid()
                + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);

        planBo.setPlanId(SDUtil.convertToLong(id));
        planBo.setDate(date);
        planBo.setStartTime(startTime);
        planBo.setEndTime(endTime);
        planBo.setRecurringGroupMode(mode);

        return planBo;

    }


    @Override
    public boolean showRescheduleToday() {
        return configurationMasterHelper.ADD_PLAN_RESCHDULE_TS;
    }

    @Override
    public boolean showRescheduleReasonToday() {
        return configurationMasterHelper.ADD_PLAN_RESCHDULE_TR;
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
    public boolean showDeleteToday() {
        return configurationMasterHelper.ADD_PLAN_DELETE_TS;
    }

    @Override
    public boolean showDeleteReasonToday() {
        return configurationMasterHelper.ADD_PLAN_DELETE_TR;
    }

    @Override
    public boolean showDeleteFuture() {
        return configurationMasterHelper.ADD_PLAN_DELETE_FS;
    }

    @Override
    public boolean showDeleteReasonFuture() {
        return configurationMasterHelper.ADD_PLAN_DELETE_FR;
    }

    @Override
    public boolean showCancelToday() {
        return configurationMasterHelper.ADD_PLAN_CANCEL_TS;
    }

    @Override
    public boolean showCancelFuture() {
        return configurationMasterHelper.ADD_PLAN_CANCEL_FS;
    }

    @Override
    public String getWeekDay(String date) {
        try {
            DateFormat formatter = new SimpleDateFormat(generalPattern, Locale.ENGLISH);
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
                    return "";
            }
        } catch (Exception e) {
            Commons.printException(e);
            return "";
        }
    }

    @Override
    public String getMonthWeekNo(String date) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(DateTimeUtils.convertStringToDateObject(date, generalPattern));

            switch (calendar.get(Calendar.WEEK_OF_MONTH)) {
                case 1:
                    return "First";
                case 2:
                    return "Second";
                case 3:
                    return "Thrid";
                case 4:
                    return "Fourth";
                case 5:
                    return "Fifth";
                case 6:
                    return "Sixth";
                default:
                    return "";
            }
        } catch (Exception e) {
            Commons.printException(e);
            return "";
        }
    }

    @Override
    public void loadPlanEndDate() {

        getCompositeDisposable().add(calendarPlanDataManager.loadAllowedDates()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe((List<String> dateList) ->
                {
                    if (!dateList.isEmpty()) {
                        Calendar zCalendar = Calendar.getInstance();
                        zCalendar.setTime(DateTimeUtils.convertStringToDateObject(dateList.get(1), generalPattern));
                        planEndDate = dateList.get(1);
                    } else {
                        loadDefaultEndDate();
                    }
                }));

    }

    private void loadDefaultEndDate() {
        getCompositeDisposable().add(calendarPlanDataManager.loadConfiguration()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe((endMonthDiff, throwable) -> {
                    Calendar zCalendar = Calendar.getInstance();
                    zCalendar.add(Calendar.MONTH, endMonthDiff);
                    zCalendar.set(Calendar.DATE, zCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                    planEndDate = DateTimeUtils.convertDateObjectToRequestedFormat(zCalendar.getTime(), generalPattern);
                }));
    }

    @Override
    public void allowedToAddRecursivePlan(int mode, String date, String startTime, String endTime, RetailerMasterBO retailerMasterBO) {
        List<DateWisePlanBo> recursivePlanList = new ArrayList<>();
        getIvyView().showLoading();
        Calendar fromCal = Calendar.getInstance();
        Calendar toCal = Calendar.getInstance();
        fromCal.setTime(DateTimeUtils.convertStringToDateObject(date, generalPattern));
        toCal.setTime(DateTimeUtils.convertStringToDateObject(planEndDate, generalPattern));

        Calendar selectedDate = Calendar.getInstance();
        selectedDate.setTime(DateTimeUtils.convertStringToDateObject(date, generalPattern));


        getCompositeDisposable().add(planRetailerDataManager.getAllDateRetailerPlanList()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(listHashMap -> {

                    if (mode == 1) {
                        //Week
                        while (!fromCal.after(toCal)) {

                            int errorMsg = checkForPlanning(listHashMap, DateTimeUtils.convertDateObjectToRequestedFormat(fromCal.getTime(), generalPattern), startTime, endTime, retailerMasterBO.getRetailerID());

                            if (errorMsg == 0) {
                                recursivePlanList.add(preparePlanObjects(DateTimeUtils.convertDateObjectToRequestedFormat(fromCal.getTime(), generalPattern), startTime, endTime, retailerMasterBO, false, "WEEK"));

                            } else {
                                recursivePlanList.clear();
                                getIvyView().hideLoading();
                                getIvyView().showMessage(errorMsg);
                                break;
                            }

                            fromCal.add(Calendar.DATE, 7);
                        }

                    } else {
                        //MONTH
                        while (!fromCal.after(toCal)) {

                            int errorMsg = checkForPlanning(listHashMap, DateTimeUtils.convertDateObjectToRequestedFormat(fromCal.getTime(), generalPattern), startTime, endTime, retailerMasterBO.getRetailerID());

                            if (errorMsg == 0) {
                                recursivePlanList.add(preparePlanObjects(DateTimeUtils.convertDateObjectToRequestedFormat(fromCal.getTime(), generalPattern), startTime, endTime, retailerMasterBO, false, "MONTH"));
                            } else {
                                recursivePlanList.clear();
                                getIvyView().hideLoading();
                                getIvyView().showMessage(errorMsg);
                                break;
                            }

                            fromCal.add(Calendar.MONTH, 1);
                            fromCal.set(Calendar.DAY_OF_WEEK, selectedDate.get(Calendar.DAY_OF_WEEK));
                            fromCal.set(Calendar.WEEK_OF_MONTH, selectedDate.get(Calendar.WEEK_OF_MONTH));
                        }
                    }

                    getIvyView().updateNewRecursivePlanList(recursivePlanList);

                }));

    }

    @Override
    public void addRecursivePlans(List<DateWisePlanBo> planList) {
        getCompositeDisposable().add(addPlanDataManager.savePlan(planList)
                .flatMapSingle(aPlanList -> retailerDataManager.updatePlanVisitCount(aPlanList))
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui()).subscribe(aBoolean -> {
                    updateIsToday();
                    getIvyView().hideLoading();
                    getIvyView().updateDatePlan(planList.get(0));

                }));

    }

    @Override
    public void updateRecursivePlan(String startTime, String endTime, DateWisePlanBo planBo, String reasonId, boolean allFuturePlan) {
        if (allFuturePlan) {
            List<DateWisePlanBo> recursivePlanList = new ArrayList<>();
            getIvyView().showLoading();
            Calendar fromCal = Calendar.getInstance();
            Calendar toCal = Calendar.getInstance();
            fromCal.setTime(DateTimeUtils.convertStringToDateObject(planBo.getDate(), generalPattern));
            toCal.setTime(DateTimeUtils.convertStringToDateObject(planEndDate, generalPattern));

            Calendar selectedDate = Calendar.getInstance();
            selectedDate.setTime(DateTimeUtils.convertStringToDateObject(planBo.getDate(), generalPattern));

            getCompositeDisposable().add(planRetailerDataManager.getAllDateRetailerPlanList()
                    .subscribeOn(getSchedulerProvider().io())
                    .observeOn(getSchedulerProvider().ui())
                    .subscribe(listHashMap -> {

                        if (planBo.getRecurringGroupMode().equalsIgnoreCase("WEEK")) {
                            //Week
                            while (!fromCal.after(toCal)) {

                                int errorMsg = checkForPlanning(listHashMap, DateTimeUtils.convertDateObjectToRequestedFormat(fromCal.getTime(), generalPattern), startTime, endTime, "" + planBo.getEntityId());

                                if (errorMsg == 0) {
                                    recursivePlanList.add(updatePlanObjects(DateTimeUtils.convertDateObjectToRequestedFormat(fromCal.getTime(), generalPattern), startTime, endTime, planBo, "WEEK"));

                                } else {
                                    recursivePlanList.clear();
                                    getIvyView().hideLoading();
                                    getIvyView().showMessage(errorMsg);
                                    break;
                                }

                                fromCal.add(Calendar.DATE, 7);
                            }

                        } else {
                            //MONTH
                            while (!fromCal.after(toCal)) {

                                int errorMsg = checkForPlanning(listHashMap, DateTimeUtils.convertDateObjectToRequestedFormat(fromCal.getTime(), generalPattern), startTime, endTime, "" + planBo.getEntityId());

                                if (errorMsg == 0) {
                                    recursivePlanList.add(updatePlanObjects(DateTimeUtils.convertDateObjectToRequestedFormat(fromCal.getTime(), generalPattern), startTime, endTime, planBo, "MONTH"));
                                } else {
                                    recursivePlanList.clear();
                                    getIvyView().hideLoading();
                                    getIvyView().showMessage(errorMsg);
                                    break;
                                }

                                fromCal.add(Calendar.MONTH, 1);
                                fromCal.set(Calendar.DAY_OF_WEEK, selectedDate.get(Calendar.DAY_OF_WEEK));
                                fromCal.set(Calendar.WEEK_OF_MONTH, selectedDate.get(Calendar.WEEK_OF_MONTH));
                            }
                        }

                        getIvyView().updateEditedRecursivePlanList(recursivePlanList, planBo, reasonId);

                    }));

        } else
            updatePlan(startTime, endTime, planBo, reasonId);

    }

    @Override
    public void saveEditedRecursiveList(List<DateWisePlanBo> planList, DateWisePlanBo planBo, String reasonID) {
        getCompositeDisposable().add(addPlanDataManager.updatePlan(planList, planBo, reasonID)
                .flatMapSingle(aPlanList -> retailerDataManager.updatePlanVisitCount(aPlanList))
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui()).subscribe(aBoolean -> {
                    updateIsToday();
                    getIvyView().hideLoading();
                    getIvyView().updateDatePlan(planList.get(0));

                }));

    }

    private int checkForPlanning(HashMap<String, List<DateWisePlanBo>> plannedListMap, String planDate, String startTime, String endTime, String retailerId) {
        int id = 0;

        List<DateWisePlanBo> planList = new ArrayList<>();
        if (plannedListMap.get(planDate) != null) {
            for (DateWisePlanBo dateWisePlanBo : Objects.requireNonNull(plannedListMap.get(planDate))) {
                if (!dateWisePlanBo.getVisitStatus().equals(CANCELLED))
                    planList.add(dateWisePlanBo);
            }
        }
        if (planList.size() > 0) {
            for (DateWisePlanBo planBo : planList) {

                if ((planBo.getEntityId() + "").equalsIgnoreCase(retailerId))
                    return R.string.recursive_retailer;

                if (DateTimeUtils.isBetweenTime(startTime, endTime, planBo.getStartTime(), true)
                        || DateTimeUtils.isBetweenTime(startTime, endTime, planBo.getEndTime(), false))
                    return R.string.recursive_time_slot;

            }
        }

        return id;
    }

    private void updateIsToday() {
        getCompositeDisposable().add(retailerDataManager.updateIsToday()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe((aBoolean, throwable) -> {

                }));
    }
}
