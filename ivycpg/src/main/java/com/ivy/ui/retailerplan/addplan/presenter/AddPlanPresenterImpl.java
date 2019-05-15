package com.ivy.ui.retailerplan.addplan.presenter;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.ui.retailerplan.addplan.AddPlanContract;
import com.ivy.ui.retailerplan.addplan.DateWisePlanBo;
import com.ivy.ui.retailerplan.addplan.data.AddPlanDataManager;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.rx.SchedulerProvider;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

public class AddPlanPresenterImpl <V extends AddPlanContract.AddPlanView> extends BasePresenter<V> implements AddPlanContract.AddPlanPresenter<V>{


    private final String mEntityRetailer = "RETAILER";
    private final String mEntityDistributor = "DIST";

    private AddPlanDataManager addPlanDataManager;

    private DataManager dataManager;

    @Inject
    public AddPlanPresenterImpl(DataManager dataManager, SchedulerProvider schedulerProvider
            , CompositeDisposable compositeDisposable, ConfigurationMasterHelper configurationMasterHelper
            , V view, AddPlanDataManager addPlanDataManager) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, view);

        this.addPlanDataManager = addPlanDataManager;
        this.dataManager = dataManager;
    }

    @Override
    public void addNewPlan(String date, String startTime, String endTime,RetailerMasterBO retailerMasterBO) {

        DateWisePlanBo dateWisePlanBo = preparePlanObjects(date,startTime,endTime,retailerMasterBO);

        getCompositeDisposable().add(addPlanDataManager.savePlan(dateWisePlanBo)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<DateWisePlanBo>() {
                    @Override
                    public void accept(DateWisePlanBo planBo) throws Exception {

                        if (DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL).equalsIgnoreCase(planBo.getDate()))
                            dataManager.getRetailMaster().setIsToday(1);

                        planBo.setOperationType("Add");

                        getIvyView().updateDatePlan(planBo);

                    }
                })
        );
    }

    @Override
    public void updatePlan(String startTime, String endTime, DateWisePlanBo planBo) {
        DateWisePlanBo dateWisePlanBo = updatePlanObjects(startTime,endTime,planBo);

        getCompositeDisposable().add(addPlanDataManager.updatePlan(dateWisePlanBo)
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
    public void cancelPlan(DateWisePlanBo dateWisePlanBo) {

        getCompositeDisposable().add(addPlanDataManager.cancelPlan(dateWisePlanBo)
                .subscribeOn(getSchedulerProvider().io())
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
    public void deletePlan(DateWisePlanBo dateWisePlanBo) {

        getCompositeDisposable().add(addPlanDataManager.DeletePlan(dateWisePlanBo)
                .subscribeOn(getSchedulerProvider().io())
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

    private DateWisePlanBo preparePlanObjects(String date, String startTime, String endTime,RetailerMasterBO retailerMasterBO){

        date = DateTimeUtils.convertToServerDateFormat(date, "yyyy/MM/dd");

        DateWisePlanBo dateWisePlanBo = new DateWisePlanBo();

        dateWisePlanBo.setPlanId(0);
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

        return dateWisePlanBo;
    }

    private DateWisePlanBo updatePlanObjects(String startTime, String endTime,DateWisePlanBo planBo){

        planBo.setStartTime(startTime);
        planBo.setEndTime(endTime);

        return planBo;
    }
}
