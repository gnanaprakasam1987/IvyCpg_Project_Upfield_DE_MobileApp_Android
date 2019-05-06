package com.ivy.ui.filter.presenter;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.ui.offlineplan.addplan.DateWisePlanBo;
import com.ivy.ui.filter.RetailerPlanFilterBo;
import com.ivy.ui.filter.RetailerPlanFilterConstants;
import com.ivy.ui.filter.RetailerPlanFilterContract;
import com.ivy.ui.filter.data.RetailerPlanFilterDataManager;
import com.ivy.utils.rx.SchedulerProvider;

import java.util.ArrayList;
import java.util.HashMap;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

import static com.ivy.ui.filter.RetailerPlanFilterConstants.CODE_IS_NOT_VISITED;
import static com.ivy.ui.filter.RetailerPlanFilterConstants.CODE_LAST_VISIT_DATE;
import static com.ivy.ui.filter.RetailerPlanFilterConstants.CODE_TASK_DUE_DATE;

public class RetailerPlanFilterPresenterImpl<V extends RetailerPlanFilterContract.RetailerPlanFilterView>
        extends BasePresenter<V> implements RetailerPlanFilterContract.RetailerPlanFilterPresenter<V> {

    private ArrayList<String> configurationList ;
    private RetailerPlanFilterDataManager retailerPlanFilterDataManager;

    @Inject
    RetailerPlanFilterPresenterImpl(DataManager dataManager, SchedulerProvider schedulerProvider,
                                    CompositeDisposable compositeDisposable,
                                    ConfigurationMasterHelper configurationMasterHelper, V view
            ,RetailerPlanFilterDataManager retailerPlanFilterDataManager) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, view);

        this.retailerPlanFilterDataManager = retailerPlanFilterDataManager;
    }

    @Override
    public void prepareConfiguration() {
        getCompositeDisposable().add(retailerPlanFilterDataManager.prepareConfigurationMaster()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<ArrayList<String>>() {
                    @Override
                    public void accept(ArrayList<String> listValues) throws Exception {

                        setConfigurationList(listValues);
                        prepareScreenData();
                    }
                }));
    }

    @Override
    public void validateFilterObject(RetailerPlanFilterBo planFilterBo) {

        if (planFilterBo.getLastVisitDate() != null
                && (planFilterBo.getLastVisitDate().getStringOne() == null
                || planFilterBo.getLastVisitDate().getStringTwo() == null)){
            getIvyView().filterValidationFailure("Please Check Last Visit Date Fields");
        }else if (planFilterBo.getTaskDate() != null
                && (planFilterBo.getTaskDate().getStringOne() == null
                || planFilterBo.getTaskDate().getStringTwo() == null)){
            getIvyView().filterValidationFailure("Please Check Task Due Date Fields");
        }else
            getIvyView().filterValidationSuccess();
    }

    @Override
    public void getRetailerFilterArray(RetailerPlanFilterBo planFilterBo) {
        getCompositeDisposable().add(retailerPlanFilterDataManager.getFilterValues(planFilterBo)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<ArrayList<String>>() {
                    @Override
                    public void accept(ArrayList<String> retailerIds) throws Exception {

                        getIvyView().filteredRetailerIds(retailerIds);

                    }
                }));
    }

    @Override
    public boolean isConfigureAvail(String configuration) {
        return configurationList !=null && configurationList.contains(configuration);
    }

    private void setConfigurationList(ArrayList<String> configurationList) {
        this.configurationList = configurationList;
    }

    private void prepareScreenData(){

        for (String configName : configurationList) {
            if (configName.equalsIgnoreCase(CODE_IS_NOT_VISITED)){
                getIvyView().showNotVisitedRow();
            }else if (configName.equalsIgnoreCase(CODE_TASK_DUE_DATE)){
                getIvyView().showTaskDueDateRow();
            }else if (configName.equalsIgnoreCase(CODE_LAST_VISIT_DATE)){
                getIvyView().showLastVisitRow();
            }
        }
    }
}
