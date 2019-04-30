package com.ivy.ui.retailerplanfilter.presenter;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.ui.offlineplan.addplan.DateWisePlanBo;
import com.ivy.ui.retailerplanfilter.RetailerPlanFilterBo;
import com.ivy.ui.retailerplanfilter.RetailerPlanFilterConstants;
import com.ivy.ui.retailerplanfilter.RetailerPlanFilterContract;
import com.ivy.ui.retailerplanfilter.data.RetailerPlanFilterDataManager;
import com.ivy.utils.rx.SchedulerProvider;

import java.util.ArrayList;
import java.util.HashMap;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

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
                    }
                }));
    }

    @Override
    public void validateFilterObject(RetailerPlanFilterBo planFilterBo) {
         if (planFilterBo.getLastVisitDate() != null){

         }
    }

    public boolean isConfigureAvail(String configuration) {
        return configurationList !=null && configurationList.contains(configuration);
    }

    public void setConfigurationList(ArrayList<String> configurationList) {
        this.configurationList = configurationList;
    }

    private void prepareScreenData(){
        if (configurationList.contains(RetailerPlanFilterConstants.CODE_IS_NOT_VISITED))
            getIvyView().showNotVisitedRow();

        if (configurationList.contains(RetailerPlanFilterConstants.CODE_TASK_DUE_DATE))
            getIvyView().showTaskDueDateRow();

        if (configurationList.contains(RetailerPlanFilterConstants.CODE_LAST_VISIT_DATE))
            getIvyView().showLastVisitRow();
    }
}
