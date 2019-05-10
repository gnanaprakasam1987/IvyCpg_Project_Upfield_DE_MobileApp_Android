package com.ivy.ui.retailer.filter.presenter;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.sd.png.bo.AttributeBO;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.ui.retailer.filter.RetailerPlanFilterBo;
import com.ivy.ui.retailer.filter.RetailerPlanFilterContract;
import com.ivy.ui.retailer.filter.data.RetailerPlanFilterDataManager;
import com.ivy.utils.rx.SchedulerProvider;

import java.util.ArrayList;
import java.util.HashMap;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function3;
import io.reactivex.observers.DisposableObserver;

import static com.ivy.ui.retailer.filter.RetailerPlanFilterConstants.CODE_IS_NOT_VISITED;
import static com.ivy.ui.retailer.filter.RetailerPlanFilterConstants.CODE_LAST_VISIT_DATE;
import static com.ivy.ui.retailer.filter.RetailerPlanFilterConstants.CODE_TASK_DUE_DATE;

public class RetailerPlanFilterPresenterImpl<V extends RetailerPlanFilterContract.RetailerPlanFilterView>
        extends BasePresenter<V> implements RetailerPlanFilterContract.RetailerPlanFilterPresenter<V> {

    private ArrayList<String> configurationList ;
    private RetailerPlanFilterDataManager retailerPlanFilterDataManager;
    private ArrayList<AttributeBO> attributeListValues;

    private HashMap<String,ArrayList<AttributeBO>> attributeChildLst;

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
        getCompositeDisposable().add(Observable.zip(
                retailerPlanFilterDataManager.prepareConfigurationMaster(),
                retailerPlanFilterDataManager.prepareAttributeList(),
                retailerPlanFilterDataManager.prepareChildAttributeList(),
                new Function3<ArrayList<String>, ArrayList<AttributeBO>,HashMap<String, ArrayList<AttributeBO>>,Boolean>() {
                    @Override
                    public Boolean apply(ArrayList<String> configListValues,
                                         ArrayList<AttributeBO> attributeParentList,
                                         HashMap<String,ArrayList<AttributeBO>> attributeChildList) throws Exception {

                        setConfigurationList(configListValues);
                        setAttributeListValues(attributeParentList);
                        setAttributeChildLst(attributeChildList);

                        return true;
                    }
                }).subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                        prepareScreenData();
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
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
        }else {

            if (planFilterBo.getIsNotVisited() == 0
                    && planFilterBo.getLastVisitDate() == null
                    && planFilterBo.getTaskDate() == null){
                getIvyView().clearFilter();
            }else
                getIvyView().filterValidationSuccess();
        }
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

    public ArrayList<AttributeBO> getAttributeListValues() {
        return attributeListValues;
    }

    public void setAttributeListValues(ArrayList<AttributeBO> attributeListValues) {
        this.attributeListValues = attributeListValues;
    }

    public ArrayList<AttributeBO> getAttributeChildLst(String parentId) {
        return attributeChildLst.get(parentId);
    }

    public void setAttributeChildLst(HashMap<String, ArrayList<AttributeBO>> attributeChildLst) {
        this.attributeChildLst = attributeChildLst;
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

        getIvyView().showAttributeSpinner();

    }
}
