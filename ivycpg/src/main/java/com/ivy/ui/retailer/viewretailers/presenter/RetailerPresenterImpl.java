package com.ivy.ui.retailer.viewretailers.presenter;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.ui.retailerplan.addplan.DateWisePlanBo;
import com.ivy.ui.profile.data.ProfileDataManagerImpl;
import com.ivy.ui.retailer.viewretailers.RetailerContract;
import com.ivy.ui.retailer.viewretailers.data.RetailerDataManager;
import com.ivy.ui.retailer.filter.RetailerPlanFilterBo;
import com.ivy.utils.rx.SchedulerProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

public class RetailerPresenterImpl<V extends RetailerContract.RetailerView>
        extends BasePresenter<V> implements RetailerContract.RetailerPresenter<V> {

    private AppDataProvider appDataProvider;
    private ProfileDataManagerImpl profileDataManager;
    private RetailerDataManager retailerDataManager;

    private HashMap<String, List<DateWisePlanBo>> allDateRetailerPlanList;

    private ArrayList<DateWisePlanBo> selectedDateRetailerPlanList;

    private HashMap<String, DateWisePlanBo> selectedDateRetailerPlanMap;

    private ArrayList<RetailerMasterBO> visibleRetailerList = new ArrayList<>();

    @Inject
    RetailerPresenterImpl(DataManager dataManager,
                          SchedulerProvider schedulerProvider,
                          CompositeDisposable compositeDisposable,
                          ConfigurationMasterHelper configurationMasterHelper,
                          V view,
                          AppDataProvider appDataProvider, ProfileDataManagerImpl profileDataManager, RetailerDataManager retailerDataManager) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, view);

        this.appDataProvider = appDataProvider;
        this.profileDataManager = profileDataManager;
        this.retailerDataManager = retailerDataManager;
    }

    @Override
    public List<RetailerMasterBO> loadRetailerList() {
        return appDataProvider.getRetailerMasters();
    }

    @Override
    public void fetchRetailerList() {
        getIvyView().populateRetailers(loadRetailerList());
        visibleRetailerList.clear();
        visibleRetailerList.addAll(loadRetailerList());
    }

    @Override
    public void fetchUnPlannedRetailerList() {
        visibleRetailerList.clear();

        for (RetailerMasterBO retailerMasterBO : loadRetailerList()) {
            if (getSelectedRetailerPlan(retailerMasterBO.getRetailerID()) == null) {
                visibleRetailerList.add(retailerMasterBO);
            }
        }
        getIvyView().populateRetailers(visibleRetailerList);
    }

    @Override
    public void fetchTodayPlannedRetailers() {

        visibleRetailerList.clear();

        for (RetailerMasterBO retailerMasterBO : loadRetailerList()) {
            if (getSelectedRetailerPlan(retailerMasterBO.getRetailerID()) != null) {
                getIvyView().populateTodayPlannedRetailers(retailerMasterBO);
                visibleRetailerList.add(retailerMasterBO);
            }
        }

        getIvyView().focusMarker();
    }

    @Override
    public void setRetailerMasterBo(RetailerMasterBO retailerMasterBO) {
        appDataProvider.setRetailerMaster(retailerMasterBO);
    }

    @Override
    public void fetchLinkRetailer() {
        profileDataManager.getLinkRetailer();
    }

    @Override
    public void fetchRoutePath(String url) {
        getCompositeDisposable().add(retailerDataManager.getRoutePath(url)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String path) throws Exception {
                        getIvyView().drawRoutePath(path);
                    }
                }));
    }

    @Override
    public void fetchAllDateRetailerPlan() {
        getCompositeDisposable().add(retailerDataManager.getAllDateRetailerPlanList()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<HashMap<String, List<DateWisePlanBo>>>() {
                    @Override
                    public void accept(HashMap<String, List<DateWisePlanBo>> listHashMap) throws Exception {
                        allDateRetailerPlanList = listHashMap;
                    }
                }));
    }

    @Override
    public void fetchSelectedDateRetailerPlan(String date, boolean isRetailerUpdate) {

        selectedDateRetailerPlanMap = null;
        selectedDateRetailerPlanList = null;
        getIvyView().showLoading();
        getCompositeDisposable().add(retailerDataManager.getRetailerPlanList(date)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<HashMap<String, DateWisePlanBo>>() {
                    @Override
                    public void accept(HashMap<String, DateWisePlanBo> listHashMap) throws Exception {
                        getIvyView().hideLoading();
                        selectedDateRetailerPlanMap = listHashMap;
                        selectedDateRetailerPlanList = new ArrayList<>(listHashMap.values());

                        if (isRetailerUpdate)
                            getIvyView().updateView();
                    }
                }));
    }

    @Override
    public HashMap<String, List<DateWisePlanBo>> getAllDateRetailerPlanList() {
        return allDateRetailerPlanList.isEmpty() ? new HashMap<>() : allDateRetailerPlanList;
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
    public void prepareFilteredRetailerList(RetailerPlanFilterBo planFilterBo, String filter, boolean isFromRetailerlist) {

        ArrayList<String> retailerIds = new ArrayList<>();
        if (planFilterBo != null) {
            retailerIds = planFilterBo.getRetailerIds();
        }

        ArrayList<RetailerMasterBO> filteredRetailerList = new ArrayList<>();

        for (RetailerMasterBO retailerMasterBO : this.visibleRetailerList) {

            if (planFilterBo != null && !filter.isEmpty() && planFilterBo.getRetailerIds().contains(retailerMasterBO.getRetailerID())
                    && retailerMasterBO.getRetailerName().contains(filter)) {
                getIvyView().populateTodayPlannedRetailers(retailerMasterBO);
                filteredRetailerList.add(retailerMasterBO);
            } else if (planFilterBo != null && filter.isEmpty() && retailerIds.contains(retailerMasterBO.getRetailerID())) {
                getIvyView().populateTodayPlannedRetailers(retailerMasterBO);
                filteredRetailerList.add(retailerMasterBO);
            } else if (planFilterBo == null && !filter.isEmpty() && retailerMasterBO.getRetailerName().contains(filter)) {
                getIvyView().populateTodayPlannedRetailers(retailerMasterBO);
                filteredRetailerList.add(retailerMasterBO);
            } else if (planFilterBo == null && filter.isEmpty()) {
                getIvyView().populateTodayPlannedRetailers(retailerMasterBO);
                filteredRetailerList.add(retailerMasterBO);
            }
        }

        if (isFromRetailerlist)
            getIvyView().populateRetailers(filteredRetailerList);

        getIvyView().focusMarker();
    }

    public void removeDatePlan(DateWisePlanBo planBo) {

        for (DateWisePlanBo dateWisePlanBo : selectedDateRetailerPlanList) {
            if (dateWisePlanBo.getEntityId() == planBo.getEntityId()) {
                selectedDateRetailerPlanList.remove(dateWisePlanBo);
                break;
            }
        }

        if (selectedDateRetailerPlanMap.get(planBo.getEntityId() + "") != null) {
            selectedDateRetailerPlanMap.remove(planBo.getEntityId() + "");
        }
    }

    public void addDatePlan(DateWisePlanBo planBo) {

        selectedDateRetailerPlanList.add(planBo);
        selectedDateRetailerPlanMap.put(planBo.getEntityId() + "", planBo);

    }

    public void updateDatePlan(DateWisePlanBo planBo) {

        int i = 0;
        for (DateWisePlanBo dateWisePlanBo : selectedDateRetailerPlanList) {
            if (dateWisePlanBo.getEntityId() == planBo.getEntityId()) {
                selectedDateRetailerPlanList.set(i, dateWisePlanBo);
                break;
            }
            i++;
        }
        selectedDateRetailerPlanMap.put(planBo.getEntityId() + "", planBo);

    }

    public String makeURL(double sourcelat, double sourcelog, double destlat,
                          double destlog, String key, boolean isBywalk) {
        String mode;
        if (isBywalk)
            mode = "mode=walking";
        else
            mode = "mode=driving";

        return "https://maps.googleapis.com/maps/api/directions/json" +
                "?origin=" + Double.toString(sourcelat) + "," + Double.toString(sourcelog) +
                "&destination=" + Double.toString(destlat) + "," + Double.toString(destlog) +
                "&sensor=false&" + mode + "&alternatives=true" + "&" + key;
    }

}
