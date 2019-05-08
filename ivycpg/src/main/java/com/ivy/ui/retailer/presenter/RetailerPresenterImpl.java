package com.ivy.ui.retailer.presenter;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.ui.offlineplan.addplan.DateWisePlanBo;
import com.ivy.ui.profile.data.ProfileDataManagerImpl;
import com.ivy.ui.retailer.RetailerContract;
import com.ivy.ui.retailer.data.RetailerDataManager;
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

    private HashMap<String, ArrayList<DateWisePlanBo>> allDateRetailerPlanList;

    private ArrayList<DateWisePlanBo> selectedDateRetailerPlanList;

    private HashMap<String,DateWisePlanBo> selectedDateRetailerPlanMap;

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

    public ArrayList<RetailerMasterBO> getVisibleRetailerList() {
        return visibleRetailerList;
    }

    @Override
    public void fetchRetailerList() {
        getIvyView().populateRetailers(loadRetailerList());
        visibleRetailerList.clear();
        visibleRetailerList.addAll(loadRetailerList());
    }

    @Override
    public void fetchTodayPlannedRetailers() {

        visibleRetailerList.clear();

        for (RetailerMasterBO retailerMasterBO : loadRetailerList()) {
            if ("Y".equals(retailerMasterBO.getIsVisited())
                    || retailerMasterBO.getIsToday() == 1
                    || "Y".equals(retailerMasterBO.getIsDeviated())) {
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
                .subscribe(new Consumer<HashMap<String, ArrayList<DateWisePlanBo>>>() {
                    @Override
                    public void accept(HashMap<String, ArrayList<DateWisePlanBo>> listHashMap) throws Exception {
                        allDateRetailerPlanList = listHashMap;
                    }
                }));
    }

    @Override
    public void fetchSelectedDateRetailerPlan(String date) {
        getCompositeDisposable().add(retailerDataManager.getRetailerPlanList(date)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<HashMap<String,DateWisePlanBo>>() {
                    @Override
                    public void accept(HashMap<String,DateWisePlanBo> listHashMap) throws Exception {

                        selectedDateRetailerPlanMap = listHashMap;

                        selectedDateRetailerPlanList = new ArrayList<>(listHashMap.values());
                    }
                }));
    }

    @Override
    public HashMap<String, ArrayList<DateWisePlanBo>> getAllDateRetailerPlanList() {
        return allDateRetailerPlanList.isEmpty()?new HashMap<>(): allDateRetailerPlanList;
    }

    @Override
    public ArrayList<DateWisePlanBo> getSelectedDateRetailerPlanList() {
        return selectedDateRetailerPlanList.isEmpty()?new ArrayList<>(): selectedDateRetailerPlanList;
    }

    @Override
    public DateWisePlanBo getSelectedRetailerPlan(String retailerId) {
        return selectedDateRetailerPlanMap.get(retailerId);
    }

    @Override
    public void prepareFilteredRetailerList(RetailerPlanFilterBo planFilterBo, String filter) {

        ArrayList<String> retailerIds = new ArrayList<>();
        if (planFilterBo != null) {
            retailerIds = planFilterBo.getRetailerIds();
        }

        ArrayList<RetailerMasterBO> visibleRetailerList = new ArrayList<>();

        for (RetailerMasterBO retailerMasterBO : this.visibleRetailerList) {

            if (planFilterBo != null && !filter.isEmpty() && planFilterBo.getRetailerIds().contains(retailerMasterBO.getRetailerID())
                        && retailerMasterBO.getRetailerName().contains(filter)){

                getIvyView().populateTodayPlannedRetailers(retailerMasterBO);
                visibleRetailerList.add(retailerMasterBO);

            }else if (planFilterBo != null && filter.isEmpty() && retailerIds.contains(retailerMasterBO.getRetailerID())) {
                getIvyView().populateTodayPlannedRetailers(retailerMasterBO);
                visibleRetailerList.add(retailerMasterBO);
            }
            else if (planFilterBo == null && !filter.isEmpty() && retailerMasterBO.getRetailerName().contains(filter)) {
                getIvyView().populateTodayPlannedRetailers(retailerMasterBO);
                visibleRetailerList.add(retailerMasterBO);
            }
        }

        this.visibleRetailerList.clear();
        this.visibleRetailerList.addAll(visibleRetailerList);

        getIvyView().focusMarker();
    }

    public String makeURL(double sourcelat, double sourcelog, double destlat,
                          double destlog,String key,boolean isBywalk) {
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
