package com.ivy.ui.offlineplan.addplan.presenter;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.ui.offlineplan.addplan.AddPlanContract;
import com.ivy.ui.offlineplan.addplan.DateWisePlanBo;
import com.ivy.ui.offlineplan.addplan.data.AddPlanDataManager;
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
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (aBoolean)
                            getIvyView().showUpdatedSuccessfullyMessage();
                        else
                            getIvyView().showUpdateFailureMessage();
                    }
                })
        );
    }

    @Override
    public void updatePlan(String date, String startTime, String endTime, RetailerMasterBO retailerMasterBO) {
        DateWisePlanBo dateWisePlanBo = preparePlanObjects(date,startTime,endTime,retailerMasterBO);

        getCompositeDisposable().add(addPlanDataManager.updatePlan(dateWisePlanBo)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (aBoolean)
                            getIvyView().showUpdatedSuccessfullyMessage();
                        else
                            getIvyView().showUpdateFailureMessage();
                    }
                })
        );
    }

    @Override
    public void cancelPlan(DateWisePlanBo dateWisePlanBo) {
        addPlanDataManager.cancelPlan(dateWisePlanBo);
    }

    @Override
    public void deletePlan(DateWisePlanBo dateWisePlanBo) {
        addPlanDataManager.DeletePlan(dateWisePlanBo);
    }

    private DateWisePlanBo preparePlanObjects(String date, String startTime, String endTime,RetailerMasterBO retailerMasterBO){

        date = DateTimeUtils.convertToServerDateFormat(date, "yyyy/MM/dd");

        startTime = DateTimeUtils.convertDateTimeObjectToRequestedFormat(date + startTime, "yyyy/MM/ddHH:mm","yyyy/MM/dd HH:mm:ss");

        endTime = DateTimeUtils.convertDateTimeObjectToRequestedFormat(date + endTime, "yyyy/MM/ddHH:mm","yyyy/MM/dd HH:mm:ss");

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
}
