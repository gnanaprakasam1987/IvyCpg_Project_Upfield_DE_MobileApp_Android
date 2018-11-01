package com.ivy.ui.dashboard.presenter;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;

import com.ivy.core.ViewTags;
import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.core.data.distributor.DistributorDataManager;
import com.ivy.core.data.label.LabelsDataManager;
import com.ivy.core.data.outlettime.OutletTimeStampDataManager;
import com.ivy.core.data.user.UserDataManager;
import com.ivy.core.di.scope.DistributorInfo;
import com.ivy.core.di.scope.LabelMasterInfo;
import com.ivy.core.di.scope.OutletTimeStampInfo;
import com.ivy.core.di.scope.UserInfo;
import com.ivy.cpg.primarysale.bo.DistributorMasterBO;
import com.ivy.cpg.view.dashboard.DashBoardBO;
import com.ivy.sd.png.bo.DailyReportBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.ui.dashboard.SellerDashboardConstants;
import com.ivy.ui.dashboard.SellerDashboardContract;
import com.ivy.ui.dashboard.data.SellerDashboardDataManager;
import com.ivy.utils.rx.SchedulerProvider;

import java.util.ArrayList;
import java.util.HashMap;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function4;
import io.reactivex.functions.Function5;
import io.reactivex.observers.DisposableObserver;

import static com.ivy.ui.dashboard.SellerDashboardConstants.CODE_COV;
import static com.ivy.ui.dashboard.SellerDashboardConstants.CODE_MSP;
import static com.ivy.ui.dashboard.SellerDashboardConstants.CODE_PDC;
import static com.ivy.ui.dashboard.SellerDashboardConstants.CODE_TLS;
import static com.ivy.ui.dashboard.SellerDashboardConstants.CODE_VAL;
import static com.ivy.ui.dashboard.SellerDashboardConstants.CODE_VIP;
import static com.ivy.ui.dashboard.SellerDashboardConstants.P3M;

public class SellerDashboardPresenterImp<V extends SellerDashboardContract.SellerDashboardView> extends BasePresenter<V> implements SellerDashboardContract.SellerDashboardPresenter<V>, LifecycleObserver {

    private OutletTimeStampDataManager mOutletTimeStampDataManager;
    private SellerDashboardDataManager sellerDashboardDataManager;
    private DistributorDataManager distributorDataManager;
    private UserDataManager userDataManager;
    private LabelsDataManager labelsDataManager;
    private AppDataProvider appDataProvider;

    private ArrayList<DashBoardBO> dashBoardList = new ArrayList<>();

    private boolean isP3M;

    private HashMap<String, String> labelsMap = new HashMap<>();

    @Inject
    public SellerDashboardPresenterImp(DataManager dataManager, SchedulerProvider schedulerProvider, CompositeDisposable compositeDisposable,
                                       ConfigurationMasterHelper configurationMasterHelper, V view, @OutletTimeStampInfo OutletTimeStampDataManager outletTimeStampDataManager,
                                       SellerDashboardDataManager sellerDashboardDataManager, @DistributorInfo DistributorDataManager distributorDataManager,
                                       @UserInfo UserDataManager userDataManager, @LabelMasterInfo LabelsDataManager labelsDataManager, AppDataProvider appDataProvider) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, view);
        this.mOutletTimeStampDataManager = outletTimeStampDataManager;
        this.sellerDashboardDataManager = sellerDashboardDataManager;
        this.distributorDataManager = distributorDataManager;
        this.userDataManager = userDataManager;
        this.labelsDataManager = labelsDataManager;
        this.appDataProvider = appDataProvider;

        if (view instanceof LifecycleOwner) {
            ((LifecycleOwner) view).getLifecycle().addObserver(this);
        }
    }


    @Override
    public void saveModuleCompletion(String menuCode) {
        getCompositeDisposable().add(getDataManager().saveModuleCompletion(menuCode)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean value) {
                    }
                }));
    }


    @Override
    public void updateTimeStampModuleWise() {
        getCompositeDisposable().add(mOutletTimeStampDataManager.updateTimeStampModuleWise(SDUtil
                .now(SDUtil.TIME))
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) {

                    }
                }));
    }

    @Override
    public void fetchSellerDashList(SellerDashboardConstants.DashBoardType dashBoardType) {
        getCompositeDisposable().add(sellerDashboardDataManager.getDashList(dashBoardType)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<ArrayList<String>>() {
                    @Override
                    public void onNext(ArrayList<String> dashList) {
                        isP3M = dashList.contains(P3M);
                        getIvyView().updateDashSpinner(dashList);
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
    public boolean isSMPBasedDash() {
        return isP3M || getConfigurationMasterHelper().IS_SMP_BASED_DASH;
    }

    @Override
    public boolean isUserBasedDash() {
        return getConfigurationMasterHelper().IS_USER_BASED_DASH;
    }

    @Override
    public boolean isDistributorBasedDash() {
        return getConfigurationMasterHelper().IS_DISTRIBUTOR_BASED_DASH;
    }

    @Override
    public boolean isNiveaBasedDash() {
        return getConfigurationMasterHelper().IS_NIVEA_BASED_DASH;
    }

    @Override
    public boolean shouldShowTrendChart() {
        return isP3M || getConfigurationMasterHelper().IS_SMP_BASED_DASH;
    }

    @Override
    public void fetchP3MSellerDashboardData() {

    }

    @Override
    public void fetchDistributorList(final boolean isMultiSelect) {
        getCompositeDisposable().add(distributorDataManager.fetchDistributorList()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<ArrayList<DistributorMasterBO>>() {
                    @Override
                    public void onNext(ArrayList<DistributorMasterBO> distributorMasterBOS) {
                        if (isMultiSelect)
                            getIvyView().setupMultiSelectDistributorSpinner(distributorMasterBOS);
                        else
                            getIvyView().setUpDistributorSpinner(distributorMasterBOS);

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
    public void fetchUserList(String distributorIds, boolean isMultiSelect) {

        if (!distributorIds.equalsIgnoreCase("0")) {
            fetchUsersMatchingDistributor(distributorIds, isMultiSelect);
        } else {
            fetchAllUsers(isMultiSelect);
        }


    }

    @Override
    public void fetchKPIDashboardData(String userid, String interval) {
        getCompositeDisposable().add(sellerDashboardDataManager.getKPIDashboard(userid, interval)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<ArrayList<DashBoardBO>>() {
                    @Override
                    public void onNext(ArrayList<DashBoardBO> dashBoardBOS) {

                        getIvyView().setDashboardListAdapter(dashBoardBOS);
                        dashBoardList.clear();
                        dashBoardList.addAll(dashBoardBOS);

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
    public void fetchSellerDashboardDataForUser(int selectedUser) {
        getCompositeDisposable().add(sellerDashboardDataManager.getP3MSellerDashboardData(Integer.toString(selectedUser))
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<ArrayList<DashBoardBO>>() {
                    @Override
                    public void onNext(ArrayList<DashBoardBO> dashBoardBOS) {

                        getIvyView().setDashboardListAdapter(dashBoardBOS);
                        dashBoardList.clear();
                        dashBoardList.addAll(dashBoardBOS);
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
    public void fetchSellerDashboardDataForWeek(int selectedUser) {
        getCompositeDisposable().add(sellerDashboardDataManager.getSellerDashboardForWeek(Integer.toString(selectedUser))
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<ArrayList<DashBoardBO>>() {
                    @Override
                    public void onNext(ArrayList<DashBoardBO> dashBoardBOS) {

                        getIvyView().setDashboardListAdapter(dashBoardBOS);
                        dashBoardList.clear();
                        dashBoardList.addAll(dashBoardBOS);
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
    public void fetchSellerDashboardForUserAndInterval(int selectedUser, String interval) {
        getCompositeDisposable().add(sellerDashboardDataManager.getSellerDashboardForInterval(Integer.toString(selectedUser), interval)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<ArrayList<DashBoardBO>>() {
                    @Override
                    public void onNext(ArrayList<DashBoardBO> dashBoardBOS) {

                        getIvyView().setDashboardListAdapter(dashBoardBOS);
                        dashBoardList.clear();
                        dashBoardList.addAll(dashBoardBOS);
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
    public void fetchRouteDashboardData(String interval) {
        getCompositeDisposable().add(sellerDashboardDataManager.getRouteDashboardForInterval(interval)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<ArrayList<DashBoardBO>>() {
                    @Override
                    public void onNext(ArrayList<DashBoardBO> dashBoardBOS) {

                        dashBoardList.clear();
                        dashBoardList.addAll(dashBoardBOS);
                        getIvyView().setDashboardListAdapter(dashBoardList);
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
    public void fetchRetailerDashboard(String interval) {
        getCompositeDisposable().add(sellerDashboardDataManager.getRetailerDashboardForInterval(appDataProvider.getRetailMaster().getRetailerID(), interval)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<ArrayList<DashBoardBO>>() {
                    @Override
                    public void onNext(ArrayList<DashBoardBO> dashBoardBOS) {
                        dashBoardList.clear();
                        dashBoardList.addAll(dashBoardBOS);
                        getIvyView().setDashboardListAdapter(dashBoardList);

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
    public void fetchKpiMonths(boolean isFromRetailer) {
        getCompositeDisposable().add(sellerDashboardDataManager.getKpiMonths(isFromRetailer)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<ArrayList<String>>() {
                    @Override
                    public void onNext(ArrayList<String> monthList) {
                        getIvyView().setUpMonthSpinner(monthList);

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
    public void fetchWeeks() {
        getCompositeDisposable().add(sellerDashboardDataManager.getKpiWeekList()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<ArrayList<String>>() {
                    @Override
                    public void onNext(ArrayList<String> weekList) {
                        if (weekList.size() > 0)
                            fetchCurrentWeek(weekList);
                        else
                            getIvyView().setDashboardListAdapter(dashBoardList);


                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                }));
    }

    private void fetchCurrentWeek(final ArrayList<String> weekList) {
        getCompositeDisposable().add(sellerDashboardDataManager.getCurrentWeekInterval()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui()).subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {

                        getIvyView().setWeekSpinner(weekList, weekList.indexOf(s));
                    }
                }));
    }

    @Override
    public HashMap<String, String> getLabelsMap() {
        return labelsMap;
    }

    @Override
    public ArrayList<DashBoardBO> getDashboardListData() {
        return dashBoardList;
    }

    @Override
    public UserMasterBO getCurrentUser() {
        return appDataProvider.getUser();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private void fetchListRowLabels() {
        getCompositeDisposable().add(labelsDataManager.getLabels(ViewTags.DASHBOARD_ROW_ACHIEVED_TITLE, ViewTags.DASHBOARD_ROW_BALANCE_TITLE,
                ViewTags.DASHBOARD_ROW_FLEX_TITLE, ViewTags.DASHBOARD_ROW_INCENTIVE_TITLE,
                ViewTags.DASHBOARD_ROW_SCORE_TITLE, ViewTags.DASHBOARD_ROW_TARGET_TITLE).subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<HashMap<String, String>>() {
                    @Override
                    public void onNext(HashMap<String, String> labels) {

                        labelsMap.clear();
                        labelsMap.putAll(labels);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                }));
    }

    private void fetchUsersMatchingDistributor(String distributorIds, boolean isMultiSelect) {
        getCompositeDisposable().add(userDataManager.fetchUsersForDistributors(distributorIds)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<ArrayList<UserMasterBO>>() {
                    @Override
                    public void onNext(ArrayList<UserMasterBO> userMasterBOS) {
                        getIvyView().setUpMultiSelectUserSpinner(userMasterBOS);


                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                }));

    }


    private void fetchAllUsers(boolean isMultiSelect) {
        getCompositeDisposable().add(userDataManager.fetchDashboardUsers()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<ArrayList<UserMasterBO>>() {
                    @Override
                    public void onNext(ArrayList<UserMasterBO> userMasterBOS) {
                        getIvyView().setUpMultiSelectUserSpinner(userMasterBOS);
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
    public void computeDayAchievements() {
        if (dashBoardList.size() > 0)
            getCompositeDisposable().add(Single.zip(sellerDashboardDataManager.fetchOutletDailyReport(),
                    sellerDashboardDataManager.fetchTotalCallsForTheDayExcludingDeviatedVisits(),
                    sellerDashboardDataManager.fetchNoOfInvoiceAndValue(),
                    sellerDashboardDataManager.fetchNoOfOrderAndValue(),
                    sellerDashboardDataManager.getProductiveCallsForDay(),
                    new Function5<DailyReportBO, Integer, DailyReportBO, DailyReportBO,Integer, DayAchievementData>() {
                        @Override
                        public DayAchievementData apply(DailyReportBO dailyReportBOForOutlet, Integer totalCallsForDay, DailyReportBO dailyReportBoWithInvoice, DailyReportBO dailyReportBoWithOrderValue,Integer noOfProductiveCalls) throws Exception {
                            DayAchievementData dayAchievementData = new DayAchievementData();
                            dayAchievementData.setInvoiceReport(dailyReportBoWithInvoice);
                            dayAchievementData.setOrderValueReport(dailyReportBoWithOrderValue);
                            dayAchievementData.setOutletReport(dailyReportBOForOutlet);
                            dayAchievementData.setTotalCallsForDay(totalCallsForDay);
                            dayAchievementData.setProductiveCalls(noOfProductiveCalls);
                            return dayAchievementData;
                        }
                    }).subscribeOn(getSchedulerProvider().io())
                    .observeOn(getSchedulerProvider().ui())
                    .subscribe(new Consumer<DayAchievementData>() {
                        @Override
                        public void accept(DayAchievementData o) {

                            for (DashBoardBO dashBoardBO : dashBoardList) {
                                if (dashBoardBO.getCode().equalsIgnoreCase(CODE_VAL))
                                    computeDailyAchievementForValue(dashBoardBO, o.outletReport);
                                else if (dashBoardBO.getCode().equalsIgnoreCase(CODE_VIP))
                                    computeDailyAchievementsForVip(dashBoardBO, o.outletReport);
                                else if (dashBoardBO.getCode().equalsIgnoreCase(CODE_TLS))
                                    computeDailyAchievementsForTLS(dashBoardBO, o.outletReport);
                                else if(dashBoardBO.getCode().equalsIgnoreCase(CODE_PDC))
                                    computeDailyAchievementsForPDC(dashBoardBO,o.productiveCalls);
                                else if(dashBoardBO.getCode().equalsIgnoreCase(CODE_MSP))
                                    computeDailyAchievementsForMSP(dashBoardBO, o.outletReport);
                                else if(dashBoardBO.getCode().equalsIgnoreCase(CODE_COV))
                                    computeDailyAchievementForCOV(dashBoardBO);
                            }
                        }
                    }));
    }


    private class DayAchievementData {
        private DailyReportBO outletReport;

        private DailyReportBO invoiceReport;

        private DailyReportBO orderValueReport;

        private int totalCallsForDay;

        private int productiveCalls;

        public DailyReportBO getOutletReport() {
            return outletReport;
        }

        public void setOutletReport(DailyReportBO outletReport) {
            this.outletReport = outletReport;
        }

        public DailyReportBO getInvoiceReport() {
            return invoiceReport;
        }

        public void setInvoiceReport(DailyReportBO invoiceReport) {
            this.invoiceReport = invoiceReport;
        }

        public DailyReportBO getOrderValueReport() {
            return orderValueReport;
        }

        public void setOrderValueReport(DailyReportBO orderValueReport) {
            this.orderValueReport = orderValueReport;
        }

        public int getTotalCallsForDay() {
            return totalCallsForDay;
        }

        public void setTotalCallsForDay(int totalCallsForDay) {
            this.totalCallsForDay = totalCallsForDay;
        }

        public int getProductiveCalls() {
            return productiveCalls;
        }

        public void setProductiveCalls(int productiveCalls) {
            this.productiveCalls = productiveCalls;
        }
    }

    private void computeDailyAchievementForValue(DashBoardBO dashBoardBO, DailyReportBO dailyReportBOForOutlet) {
        dashBoardBO.setKpiAcheived(dailyReportBOForOutlet.getTotValues());

        int kpiAcheived = (int) SDUtil.convertToDouble(dailyReportBOForOutlet.getTotValues());
        int kpiTarget;

        try {
            kpiTarget = (int) SDUtil.convertToDouble(dashBoardBO.getKpiTarget());
        } catch (Exception e) {
            kpiTarget = 0;
            Commons.printException(e + "");
        }

        if (kpiTarget == 0) {
            dashBoardBO.setCalculatedPercentage(0);
        } else {
            dashBoardBO.setCalculatedPercentage((kpiAcheived * 100) / kpiTarget);
        }
        if (dashBoardBO.getCalculatedPercentage() >= 100) {
            dashBoardBO.setConvTargetPercentage(0);
            dashBoardBO.setConvAcheivedPercentage(100);
        } else {
            dashBoardBO.setConvTargetPercentage(100 - dashBoardBO
                    .getCalculatedPercentage());
            dashBoardBO.setConvAcheivedPercentage(dashBoardBO
                    .getCalculatedPercentage());
        }
    }

    private void computeDailyAchievementsForVip(DashBoardBO dashBoardBO, DailyReportBO dailyReportBOForOutlet) {
        dashBoardBO.setKpiAcheived(dailyReportBOForOutlet.getEffCoverage());
        int kpiAcheived = (int) SDUtil.convertToDouble(dailyReportBOForOutlet.getEffCoverage());
        int kpiTarget;

        try {
            kpiTarget = (int) SDUtil.convertToDouble(dashBoardBO.getKpiTarget());
        } catch (Exception e) {
            kpiTarget = 0;
            Commons.printException(e + "");
        }

        if (kpiTarget == 0) {
            dashBoardBO.setCalculatedPercentage(0);
        } else {
            dashBoardBO.setCalculatedPercentage((kpiAcheived * 100) / kpiTarget);
        }
        if (dashBoardBO.getCalculatedPercentage() >= 100) {
            dashBoardBO.setConvTargetPercentage(0);
            dashBoardBO.setConvAcheivedPercentage(100);
        } else {
            dashBoardBO.setConvTargetPercentage(100 - dashBoardBO
                    .getCalculatedPercentage());
            dashBoardBO.setConvAcheivedPercentage(dashBoardBO
                    .getCalculatedPercentage());
        }
    }

    private void computeDailyAchievementsForTLS(DashBoardBO dashBoardBO, DailyReportBO dailyReportBOForOutlet) {
        dashBoardBO.setKpiAcheived(dailyReportBOForOutlet.getTotLines());
        int kpiAcheived = (int) SDUtil.convertToDouble(dailyReportBOForOutlet.getTotLines());
        int kpiTarget;

        try {
            kpiTarget = (int) SDUtil.convertToDouble(dashBoardBO.getKpiTarget());
        } catch (Exception e) {
            kpiTarget = 0;
            Commons.printException(e + "");
        }

        if (kpiTarget == 0) {
            dashBoardBO.setCalculatedPercentage(0);
        } else {
            dashBoardBO.setCalculatedPercentage((kpiAcheived * 100) / kpiTarget);
        }
        if (dashBoardBO.getCalculatedPercentage() >= 100) {
            dashBoardBO.setConvTargetPercentage(0);
            dashBoardBO.setConvAcheivedPercentage(100);
        } else {
            dashBoardBO.setConvTargetPercentage(100 - dashBoardBO
                    .getCalculatedPercentage());
            dashBoardBO.setConvAcheivedPercentage(dashBoardBO
                    .getCalculatedPercentage());
        }
    }

    private void computeDailyAchievementsForPDC(DashBoardBO dashBoardBO, int productiveCalls) {

        dashBoardBO.setKpiAcheived(Integer.toString(productiveCalls));
        int kpiTarget;

        try {
            kpiTarget = (int) SDUtil.convertToDouble(dashBoardBO.getKpiTarget());
        } catch (Exception e) {
            kpiTarget = 0;
            Commons.printException(e + "");
        }

        if (kpiTarget == 0) {
            dashBoardBO.setCalculatedPercentage(0);
        } else {
            dashBoardBO.setCalculatedPercentage((productiveCalls * 100) / kpiTarget);
        }
        if (dashBoardBO.getCalculatedPercentage() >= 100) {
            dashBoardBO.setConvTargetPercentage(0);
            dashBoardBO.setConvAcheivedPercentage(100);
        } else {
            dashBoardBO.setConvTargetPercentage(100 - dashBoardBO
                    .getCalculatedPercentage());
            dashBoardBO.setConvAcheivedPercentage(dashBoardBO
                    .getCalculatedPercentage());
        }
    }

    private void computeDailyAchievementsForMSP(DashBoardBO dashBoardBO, DailyReportBO dailyReportBOForOutlet) {
        dashBoardBO.setKpiAcheived(dailyReportBOForOutlet.getMspValues());
        int kpiAcheived = (int) SDUtil.convertToDouble(dailyReportBOForOutlet.getMspValues());
        int kpiTarget;

        try {
            kpiTarget = (int) SDUtil.convertToDouble(dashBoardBO.getKpiTarget());
        } catch (Exception e) {
            kpiTarget = 0;
            Commons.printException(e + "");
        }

        if (kpiTarget == 0) {
            dashBoardBO.setCalculatedPercentage(0);
        } else {
            dashBoardBO.setCalculatedPercentage((kpiAcheived * 100) / kpiTarget);
        }

        if (dashBoardBO.getCalculatedPercentage() >= 100) {
            dashBoardBO.setConvTargetPercentage(0);
            dashBoardBO.setConvAcheivedPercentage(100);
        } else {
            dashBoardBO.setConvTargetPercentage(100 - dashBoardBO
                    .getCalculatedPercentage());
            dashBoardBO.setConvAcheivedPercentage(dashBoardBO
                    .getCalculatedPercentage());
        }
    }

    private void computeDailyAchievementForCOV(DashBoardBO dashBoardBO){
        int plannedRetailerCount = getTodayRetailerCount();
        int plannedRetailerVisitCount = getVisitedRetailerCount();

        dashBoardBO.setKpiAcheived(plannedRetailerVisitCount + "");
        int kpiAcheived = plannedRetailerVisitCount;
        int kpiTarget;

        try {
            kpiTarget = (plannedRetailerCount);
        } catch (Exception e) {
            kpiTarget = 0;
            Commons.printException(e + "");
        }

        if (kpiTarget == 0) {
            dashBoardBO.setCalculatedPercentage(0);
        } else {
            dashBoardBO.setCalculatedPercentage((kpiAcheived * 100) / kpiTarget);
        }

        if (dashBoardBO.getCalculatedPercentage() >= 100) {
            dashBoardBO.setConvTargetPercentage(0);
            dashBoardBO.setConvAcheivedPercentage(100);
        } else {
            dashBoardBO.setConvTargetPercentage(100 - dashBoardBO
                    .getCalculatedPercentage());
            dashBoardBO.setConvAcheivedPercentage(dashBoardBO
                    .getCalculatedPercentage());
        }
    }


    private int getTodayRetailerCount(){

        int count =0;
        for(RetailerMasterBO retailerMasterBO:appDataProvider.getRetailerMasters())
            if(retailerMasterBO.getIsToday()== 1)
                count++;

        return count;
    }

    private int getVisitedRetailerCount(){

        int count =0;
        for(RetailerMasterBO retailerMasterBO:appDataProvider.getRetailerMasters())
            if(retailerMasterBO.getIsVisited().equals("Y"))
                count++;

        return count;
    }


}
