package com.ivy.ui.retailerplan.addplan.presenter;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.core.data.retailer.RetailerDataManager;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.ui.retailerplan.addplan.AddPlanContract;
import com.ivy.ui.retailerplan.addplan.DateWisePlanBo;
import com.ivy.ui.retailerplan.addplan.data.AddPlanDataManager;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.rx.SchedulerProvider;

import javax.inject.Inject;

import io.reactivex.SingleSource;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

public class AddPlanPresenterImpl<V extends AddPlanContract.AddPlanView> extends BasePresenter<V> implements AddPlanContract.AddPlanPresenter<V> {


    private final String mEntityRetailer = "RETAILER";
    private final String mEntityDistributor = "DIST";

    private AddPlanDataManager addPlanDataManager;

    private DataManager dataManager;

    private RetailerDataManager retailerDataManager;

    private ConfigurationMasterHelper configurationMasterHelper;

    @Inject
    public AddPlanPresenterImpl(DataManager dataManager, SchedulerProvider schedulerProvider
            , CompositeDisposable compositeDisposable, ConfigurationMasterHelper configurationMasterHelper
            , V view, AddPlanDataManager addPlanDataManager, RetailerDataManager retailerDataManager) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, view);

        this.addPlanDataManager = addPlanDataManager;
        this.dataManager = dataManager;
        this.retailerDataManager = retailerDataManager;
        this.configurationMasterHelper = configurationMasterHelper;
    }

    @Override
    public void addNewPlan(String date, String startTime, String endTime, RetailerMasterBO retailerMasterBO, boolean isAdhoc) {

        DateWisePlanBo dateWisePlanBo = preparePlanObjects(date, startTime, endTime, retailerMasterBO, isAdhoc);

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

    private DateWisePlanBo preparePlanObjects(String date, String startTime, String endTime, RetailerMasterBO retailerMasterBO, boolean isAdhoc) {

        date = DateTimeUtils.convertToServerDateFormat(date, "yyyy/MM/dd");

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

        return dateWisePlanBo;
    }

    private DateWisePlanBo updatePlanObjects(String startTime, String endTime, DateWisePlanBo planBo) {

        planBo.setStartTime(startTime);
        planBo.setEndTime(endTime);

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


}
