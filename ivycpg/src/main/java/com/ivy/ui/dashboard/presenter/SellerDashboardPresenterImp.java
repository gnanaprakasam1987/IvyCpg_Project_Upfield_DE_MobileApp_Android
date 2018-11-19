package com.ivy.ui.dashboard.presenter;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;

import com.ivy.core.ViewTags;
import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.data.beat.BeatDataManager;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.core.data.distributor.DistributorDataManager;
import com.ivy.core.data.label.LabelsDataManager;
import com.ivy.core.data.outlettime.OutletTimeStampDataManager;
import com.ivy.core.data.user.UserDataManager;
import com.ivy.core.di.scope.BeatInfo;
import com.ivy.core.di.scope.DistributorInfo;
import com.ivy.core.di.scope.LabelMasterInfo;
import com.ivy.core.di.scope.OutletTimeStampInfo;
import com.ivy.core.di.scope.UserInfo;
import com.ivy.cpg.primarysale.bo.DistributorMasterBO;
import com.ivy.cpg.view.dashboard.DashBoardBO;
import com.ivy.sd.png.bo.BeatMasterBO;
import com.ivy.sd.png.bo.ConfigureBO;
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
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Function9;
import io.reactivex.observers.DisposableObserver;

import static com.ivy.ui.dashboard.SellerDashboardConstants.CODE_COL;
import static com.ivy.ui.dashboard.SellerDashboardConstants.CODE_COV;
import static com.ivy.ui.dashboard.SellerDashboardConstants.CODE_COVD;
import static com.ivy.ui.dashboard.SellerDashboardConstants.CODE_DROP_SIZE_INV;
import static com.ivy.ui.dashboard.SellerDashboardConstants.CODE_DROP_SIZE_ORD;
import static com.ivy.ui.dashboard.SellerDashboardConstants.CODE_EFF_SALE;
import static com.ivy.ui.dashboard.SellerDashboardConstants.CODE_EFF_VISIT;
import static com.ivy.ui.dashboard.SellerDashboardConstants.CODE_FULLFILLMENT;
import static com.ivy.ui.dashboard.SellerDashboardConstants.CODE_INIT_VS_WEEKLY_OBJ;
import static com.ivy.ui.dashboard.SellerDashboardConstants.CODE_MSL;
import static com.ivy.ui.dashboard.SellerDashboardConstants.CODE_MSP;
import static com.ivy.ui.dashboard.SellerDashboardConstants.CODE_PDC;
import static com.ivy.ui.dashboard.SellerDashboardConstants.CODE_PRM;
import static com.ivy.ui.dashboard.SellerDashboardConstants.CODE_RETURN_RATE_INV;
import static com.ivy.ui.dashboard.SellerDashboardConstants.CODE_RETURN_RATE_ORD;
import static com.ivy.ui.dashboard.SellerDashboardConstants.CODE_SALES_VS_WEEKLY_OBJ;
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
    private BeatDataManager beatDataManager;

    private ArrayList<DashBoardBO> dashBoardList = new ArrayList<>();

    private boolean isP3M;

    private HashMap<String, String> labelsMap = new HashMap<>();

    @Inject
    public SellerDashboardPresenterImp(DataManager dataManager, SchedulerProvider schedulerProvider, CompositeDisposable compositeDisposable,
                                       ConfigurationMasterHelper configurationMasterHelper, V view, @OutletTimeStampInfo OutletTimeStampDataManager outletTimeStampDataManager,
                                       SellerDashboardDataManager sellerDashboardDataManager, @DistributorInfo DistributorDataManager distributorDataManager,
                                       @UserInfo UserDataManager userDataManager, @LabelMasterInfo LabelsDataManager labelsDataManager, AppDataProvider appDataProvider,
                                       @BeatInfo BeatDataManager beatDataManager) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, view);
        this.mOutletTimeStampDataManager = outletTimeStampDataManager;
        this.sellerDashboardDataManager = sellerDashboardDataManager;
        this.distributorDataManager = distributorDataManager;
        this.userDataManager = userDataManager;
        this.labelsDataManager = labelsDataManager;
        this.appDataProvider = appDataProvider;
        this.beatDataManager = beatDataManager;

        if (view instanceof LifecycleOwner) {
            ((LifecycleOwner) view).getLifecycle().addObserver(this);
        }

    }

    @Override
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void fetchListRowLabels() {
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
        return getConfigurationMasterHelper().IS_SMP_BASED_DASH;
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
    public boolean shouldShowInvoiceDash() {
        return getConfigurationMasterHelper().SHOW_INV_DASH;
    }

    @Override
    public boolean shouldShowP3MDash() {
        return getConfigurationMasterHelper().SHOW_P3M_DASH;
    }

    @Override
    public boolean shouldShowSMPDash() {
        return getConfigurationMasterHelper().SHOW_SMP_DASH;
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
    public void fetchSellerDashboardDataForUser(String selectedUser) {
        getCompositeDisposable().add(sellerDashboardDataManager.getP3MSellerDashboardData(selectedUser)
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
    public void fetchSellerDashboardDataForWeek(String selectedUser) {
        getCompositeDisposable().add(sellerDashboardDataManager.getSellerDashboardForWeek(selectedUser)
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
    public void fetchSellerDashboardForUserAndInterval(String selectedUser, String interval) {
        getIvyView().showLoading();
        getCompositeDisposable().add(sellerDashboardDataManager.getSellerDashboardForInterval(selectedUser, interval)
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
                        getIvyView().hideLoading();
                    }
                }));
    }

    @Override
    public void fetchRouteDashboardData(String interval) {
        getIvyView().showLoading();
        getCompositeDisposable().add(sellerDashboardDataManager.getRouteDashboardForInterval(interval)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<ArrayList<DashBoardBO>>() {
                    @Override
                    public void onNext(ArrayList<DashBoardBO> dashBoardBOS) {

                        dashBoardList.clear();
                        dashBoardList.addAll(dashBoardBOS);
                        getIvyView().setDashboardListAdapter(dashBoardList);
                        getIvyView().hideLoading();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        getIvyView().hideLoading();
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

    @Override
    public void fetchBeats() {

        getCompositeDisposable().add(beatDataManager.fetchBeats()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<ArrayList<BeatMasterBO>>() {
                    @Override
                    public void onNext(ArrayList<BeatMasterBO> beatMasterBOS) {
                        if (beatMasterBOS.size() > 0)
                            getIvyView().setupRouteSpinner(beatMasterBOS);
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


    @Override
    public void fetchP3mTrendChartData(String userId) {
        getCompositeDisposable().add(sellerDashboardDataManager.getP3MTrendChart(userId)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<ArrayList<DashBoardBO>>() {
                    @Override
                    public void onNext(ArrayList<DashBoardBO> dashBoardBOS) {

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

        final ArrayList<String> dashCodes = new ArrayList<>();


        if (dashBoardList.size() > 0)
            for (DashBoardBO dashBoardBO : dashBoardList) {
                if (!dashCodes.contains(dashBoardBO.getCode()))
                    dashCodes.add(dashBoardBO.getCode());
            }

        final DayAchievementData dayAchievementData = new DayAchievementData();
        getCompositeDisposable().add(sellerDashboardDataManager.fetchOutletDailyReport().flatMap(new Function<DailyReportBO, SingleSource<Integer>>() {
            @Override
            public SingleSource<Integer> apply(DailyReportBO dailyReportBOForOutlet) throws Exception {
                dayAchievementData.setOutletReport(dailyReportBOForOutlet);
                if (dashCodes.contains(CODE_EFF_VISIT) || dashCodes.contains(CODE_EFF_SALE))
                    return sellerDashboardDataManager.fetchTotalCallsForTheDayExcludingDeviatedVisits();
                else
                    return null;
            }
        }).flatMap(new Function<Integer, SingleSource<DailyReportBO>>() {
            @Override
            public SingleSource<DailyReportBO> apply(Integer totalCallsForDay) throws Exception {
                if (totalCallsForDay != null)
                    dayAchievementData.setTotalCallsForDay(totalCallsForDay);

                if (dashCodes.contains(CODE_DROP_SIZE_INV) || dashCodes.contains(CODE_SALES_VS_WEEKLY_OBJ) || dashCodes.contains(CODE_RETURN_RATE_INV))
                    return sellerDashboardDataManager.fetchNoOfInvoiceAndValue();
                else
                    return null;
            }
        }).flatMap(new Function<DailyReportBO, SingleSource<DailyReportBO>>() {
            @Override
            public SingleSource<DailyReportBO> apply(DailyReportBO invoiceReport) throws Exception {

                if (invoiceReport != null)
                    dayAchievementData.setInvoiceReport(invoiceReport);

                if (dashCodes.contains(CODE_DROP_SIZE_ORD) || dashCodes.contains(CODE_RETURN_RATE_ORD))
                    return sellerDashboardDataManager.fetchNoOfOrderAndValue();
                else
                    return null;
            }
        }).flatMap(new Function<DailyReportBO, SingleSource<Integer>>() {
            @Override
            public SingleSource<Integer> apply(DailyReportBO dailyReportBoWithOrderValue) throws Exception {
                if (dailyReportBoWithOrderValue != null)
                    dayAchievementData.setOrderValueReport(dailyReportBoWithOrderValue);

                if (dashCodes.contains(CODE_PDC))
                    return sellerDashboardDataManager.getProductiveCallsForDay();
                else
                    return null;
            }
        }).flatMap(new Function<Integer, SingleSource<Integer>>() {
            @Override
            public SingleSource<Integer> apply(Integer noOfProductiveCalls) throws Exception {
                if (noOfProductiveCalls != null)
                    dayAchievementData.setProductiveCalls(noOfProductiveCalls);

                if (dashCodes.contains(CODE_EFF_VISIT))
                    return sellerDashboardDataManager.getVisitedCallsForTheDayExcludingDeviatedVisits();
                else
                    return null;
            }
        }).flatMap(new Function<Integer, SingleSource<Integer>>() {
            @Override
            public SingleSource<Integer> apply(Integer visitedCalls) throws Exception {
                if (visitedCalls != null)
                    dayAchievementData.setVisitedCalls(visitedCalls);

                if (dashCodes.contains(CODE_EFF_SALE))
                    return sellerDashboardDataManager.getProductiveCallsForTheDayExcludingDeviatedVisits();
                else
                    return null;
            }
        }).flatMap(new Function<Integer, SingleSource<Double>>() {
            @Override
            public SingleSource<Double> apply(Integer productiveCalls) throws Exception {
                if (productiveCalls != null)
                    dayAchievementData.setProductiveCallsExcludingDeviation(productiveCalls);

                if (dashCodes.contains(CODE_INIT_VS_WEEKLY_OBJ))
                    return sellerDashboardDataManager.fetchFocusBrandInvoiceAmt();
                else
                    return null;
            }
        }).flatMap(new Function<Double, SingleSource<Double>>() {
            @Override
            public SingleSource<Double> apply(Double focusBrandInvoiceAmt) throws Exception {
                if (focusBrandInvoiceAmt != null)
                    dayAchievementData.setFocusBrandValue(focusBrandInvoiceAmt);

                if (dashCodes.contains(CODE_RETURN_RATE_INV) || dashCodes.contains(CODE_RETURN_RATE_ORD))

                    return sellerDashboardDataManager.fetchSalesReturnValue();
                else
                    return null;
            }
        }).flatMap(new Function<Double, SingleSource<DailyReportBO>>() {
            @Override
            public SingleSource<DailyReportBO> apply(Double salesReturnValue) throws Exception {

                if (salesReturnValue != null)
                    dayAchievementData.setSalesReturnValue(salesReturnValue);

                if (dashCodes.contains(CODE_FULLFILLMENT))
                    return sellerDashboardDataManager.fetchFulfilmentValue();
                else
                    return null;
            }
        }).flatMap(new Function<DailyReportBO, SingleSource<Integer>>() {
            @Override
            public SingleSource<Integer> apply(DailyReportBO fulfilmentReport) throws Exception {
                if (fulfilmentReport != null)
                    dayAchievementData.setFulFilmentReport(fulfilmentReport);

                if (dashCodes.contains(CODE_PRM))
                    return sellerDashboardDataManager.fetchPromotionCount();
                else
                    return null;
            }
        }).flatMap(new Function<Integer, SingleSource<Integer>>() {
            @Override
            public SingleSource<Integer> apply(Integer promotionCount) throws Exception {
                if (promotionCount != null)
                    dayAchievementData.setPromotionCount(promotionCount);

                if (dashCodes.contains(CODE_PRM))
                    return sellerDashboardDataManager.fetchPromotionExecutedCount();
                else
                    return null;
            }
        }).flatMap(new Function<Integer, SingleSource<String>>() {
            @Override
            public SingleSource<String> apply(Integer promotionExecutedCount) throws Exception {
                if (promotionExecutedCount != null)
                    dayAchievementData.setPromotionExecutionCount(promotionExecutedCount);


                if (dashCodes.contains(CODE_MSL))
                    return sellerDashboardDataManager.fetchMslCount();
                else
                    return null;
            }
        }).flatMapObservable(new Function<String, ObservableSource<ArrayList<Double>>>() {
            @Override
            public ObservableSource<ArrayList<Double>> apply(String mslCount) throws Exception {
                if (mslCount != null) {
                    dayAchievementData.setMslCount(SDUtil.convertToInt(mslCount.split(",")[0]));
                    dayAchievementData.setMslExecutedCount(SDUtil.convertToInt(mslCount.split(",")[1]));
                }

                if (dashCodes.contains(CODE_COL))
                    return sellerDashboardDataManager.getCollectedValue();
                else
                    return null;
            }
        }).flatMapSingle(new Function<ArrayList<Double>, SingleSource<DayAchievementData>>() {
            @Override
            public SingleSource<DayAchievementData> apply(ArrayList<Double> collectionData) throws Exception {

                if (collectionData != null)
                    dayAchievementData.setCollectionData(collectionData);

                return Single.fromCallable(new Callable<DayAchievementData>() {
                    @Override
                    public DayAchievementData call() throws Exception {
                        return dayAchievementData;
                    }
                });
            }
        }).subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<DayAchievementData>() {
                    @Override
                    public void accept(DayAchievementData dayAchievementData) {

                        for (DashBoardBO dashBoardBO : dashBoardList) {
                            if (dashBoardBO.getCode().equalsIgnoreCase(CODE_VAL))
                                computeDailyAchievementForValue(dashBoardBO, dayAchievementData.outletReport);
                            else if (dashBoardBO.getCode().equalsIgnoreCase(CODE_VIP))
                                computeDailyAchievementsForVip(dashBoardBO, dayAchievementData.outletReport);
                            else if (dashBoardBO.getCode().equalsIgnoreCase(CODE_TLS))
                                computeDailyAchievementsForTLS(dashBoardBO, dayAchievementData.outletReport);
                            else if (dashBoardBO.getCode().equalsIgnoreCase(CODE_PDC))
                                computeDailyAchievementsForPDC(dashBoardBO, dayAchievementData.productiveCalls);
                            else if (dashBoardBO.getCode().equalsIgnoreCase(CODE_MSP))
                                computeDailyAchievementsForMSP(dashBoardBO, dayAchievementData.outletReport);
                            else if (dashBoardBO.getCode().equalsIgnoreCase(CODE_COV))
                                computeDailyAchievementForCOV(dashBoardBO);
                            else if (dashBoardBO.getCode().equalsIgnoreCase(CODE_PRM))
                                computeDailyAchievementsForPRM(dashBoardBO, dayAchievementData.promotionCount, dayAchievementData.promotionExecutionCount);
                            else if (dashBoardBO.getCode().equalsIgnoreCase(CODE_MSL))
                                computeDailyAchievementsForMSL(dashBoardBO, dayAchievementData.mslCount, dayAchievementData.mslExecutedCount);
                            else if (dashBoardBO.getCode().equalsIgnoreCase(CODE_COVD))
                                computeDailyAchievementsForCOVD(dashBoardBO);
                            else if (dashBoardBO.getCode().equalsIgnoreCase(CODE_COL))
                                computeDailyAchievementsForCol(dashBoardBO, dayAchievementData.collectionData);
                            else if (dashBoardBO.getCode().equalsIgnoreCase(CODE_EFF_VISIT))
                                computeDailyAchievementsForEffVisit(dashBoardBO, dayAchievementData.totalCallsForDay, dayAchievementData.visitedCalls);
                            else if (dashBoardBO.getCode().equalsIgnoreCase(CODE_EFF_SALE))
                                computeDailyAchievementsForEffSale(dashBoardBO, dayAchievementData.totalCallsForDay, dayAchievementData.productiveCallsExcludingDeviation);
                            else if (dashBoardBO.getCode().equalsIgnoreCase(CODE_DROP_SIZE_ORD))
                                computeDailyAchievementsForDropSizeOrd(dashBoardBO, dayAchievementData.orderValueReport);
                            else if (dashBoardBO.getCode().equalsIgnoreCase(CODE_DROP_SIZE_INV))
                                computeDailyAchievementsForDropSizeInv(dashBoardBO, dayAchievementData.invoiceReport);
                            else if (dashBoardBO.getCode().equalsIgnoreCase(CODE_SALES_VS_WEEKLY_OBJ))
                                computeDailyAchievementsForWeeklySalesObj(dashBoardBO, dayAchievementData.invoiceReport);
                            else if (dashBoardBO.getCode().equalsIgnoreCase(CODE_INIT_VS_WEEKLY_OBJ))
                                computeDailyAchievementsForInitVsWeeklyObj(dashBoardBO, dayAchievementData.focusBrandValue);
                            else if (dashBoardBO.getCode().equalsIgnoreCase(CODE_RETURN_RATE_INV))
                                computeDailyAchievementsForReturnRateInv(dashBoardBO, dayAchievementData.salesReturnValue, dayAchievementData.invoiceReport);
                            else if (dashBoardBO.getCode().equalsIgnoreCase(CODE_RETURN_RATE_ORD))
                                computeDailyAchievementsForReturnRateOrder(dashBoardBO, dayAchievementData.salesReturnValue, dayAchievementData.orderValueReport);
                            else if (dashBoardBO.getCode().contains(CODE_FULLFILLMENT))
                                computeDailyAchievementsForFulfilment(dashBoardBO, dayAchievementData.fulFilmentReport);

                        }

                    }
                }));
    }


    private class DayAchievementData {
        private DailyReportBO outletReport; //outlet

        private DailyReportBO invoiceReport; //dailrp

        // daily_rep
        private DailyReportBO orderValueReport; //dailyrp_order

        private DailyReportBO fulFilmentReport;

        private int totalCallsForDay; //totalcalls

        private int productiveCalls;

        private int visitedCalls;

        private double focusBrandValue;

        private ArrayList<Double> collectionData;

        private int productiveCallsExcludingDeviation;

        private double salesReturnValue;

        private int promotionCount;

        private int promotionExecutionCount;

        private int mslCount;

        private int mslExecutedCount;

        public int getMslCount() {
            return mslCount;
        }

        public void setMslCount(int mslCount) {
            this.mslCount = mslCount;
        }

        public int getMslExecutedCount() {
            return mslExecutedCount;
        }

        public void setMslExecutedCount(int mslExecutedCount) {
            this.mslExecutedCount = mslExecutedCount;
        }

        public int getPromotionCount() {
            return promotionCount;
        }

        public void setPromotionCount(int promotionCount) {
            this.promotionCount = promotionCount;
        }

        public int getPromotionExecutionCount() {
            return promotionExecutionCount;
        }

        public void setPromotionExecutionCount(int promotionExecutionCount) {
            this.promotionExecutionCount = promotionExecutionCount;
        }

        public DailyReportBO getFulFilmentReport() {
            return fulFilmentReport;
        }

        public void setFulFilmentReport(DailyReportBO fulFilmentReport) {
            this.fulFilmentReport = fulFilmentReport;
        }

        public double getSalesReturnValue() {
            return salesReturnValue;
        }

        public void setSalesReturnValue(double salesReturnValue) {
            this.salesReturnValue = salesReturnValue;
        }

        public double getFocusBrandValue() {
            return focusBrandValue;
        }

        public void setFocusBrandValue(double focusBrandValue) {
            this.focusBrandValue = focusBrandValue;
        }

        public int getProductiveCallsExcludingDeviation() {
            return productiveCallsExcludingDeviation;
        }

        public void setProductiveCallsExcludingDeviation(int productiveCallsExcludingDeviation) {
            this.productiveCallsExcludingDeviation = productiveCallsExcludingDeviation;
        }

        public int getVisitedCalls() {
            return visitedCalls;
        }

        public void setVisitedCalls(int visitedCalls) {
            this.visitedCalls = visitedCalls;
        }

        public ArrayList<Double> getCollectionData() {
            return collectionData;
        }

        public void setCollectionData(ArrayList<Double> collectionData) {
            this.collectionData = collectionData;
        }

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

    private void computeDailyAchievementForCOV(DashBoardBO dashBoardBO) {
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

    private void computeDailyAchievementsForCOVD(DashBoardBO dashBoardBO) {
        int plannedRetailerCount = getTodayRetailerCount();
        int plannedRetailerVisitCount = getVisitedRetailerCountWithDeviation();

        dashBoardBO.setKpiAcheived(plannedRetailerVisitCount + "");
        dashBoardBO.setKpiTarget(plannedRetailerCount + "");
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


    private int getTodayRetailerCount() {

        int count = 0;
        for (RetailerMasterBO retailerMasterBO : appDataProvider.getRetailerMasters())
            if (retailerMasterBO.getIsToday() == 1)
                count++;

        return count;
    }

    private int getVisitedRetailerCount() {

        int count = 0;
        for (RetailerMasterBO retailerMasterBO : appDataProvider.getRetailerMasters())
            if (retailerMasterBO.getIsVisited().equals("Y"))
                count++;

        return count;
    }

    private int getVisitedRetailerCountWithDeviation() {

        int count = 0;
        for (RetailerMasterBO retailerMasterBO : appDataProvider.getRetailerMasters())
            if (retailerMasterBO.getIsVisited().equals("Y") || retailerMasterBO.getIsDeviated().equalsIgnoreCase("Y"))
                count++;

        return count;
    }

    private void computeDailyAchievementsForCol(DashBoardBO dashBoardBO, ArrayList<Double> collectionData) {
        double kpiAcheived = 0, kpiTarget = 0;
        try {
            double osAmt = collectionData.get(0);
            double paidAmt = collectionData.get(1);

            dashBoardBO.setKpiAcheived(paidAmt + "");
            dashBoardBO.setKpiTarget(osAmt + "");
            kpiAcheived = paidAmt;
            kpiTarget = osAmt;
        } catch (Exception e) {
            kpiTarget = 0;
            Commons.printException(e + "");
        }

        if (kpiTarget == 0) {
            dashBoardBO.setCalculatedPercentage(0);
        } else {
            float value = SDUtil.convertToFloat("" + (kpiAcheived * 100) / kpiTarget);
            dashBoardBO.setCalculatedPercentage(value);
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


    private void computeDailyAchievementsForEffVisit(DashBoardBO dashBoardBO, int totalCalls, int visitedCalls) {
        if (totalCalls == 0) {
            dashBoardBO.setKpiAcheived("0");
        } else {
            dashBoardBO.setKpiAcheived((((float) visitedCalls / (float) totalCalls) * 100) + "");
        }

        int kpiAcheived = 0;
        int kpiTarget;

        try {
            kpiAcheived = (int) SDUtil.convertToDouble(dashBoardBO.getKpiAcheived());
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


    private void computeDailyAchievementsForEffSale(DashBoardBO dashBoardBO, int totalCallsForDay, int productiveCallsExcludingDeviation) {
        if (totalCallsForDay == 0) {
            dashBoardBO.setKpiAcheived("0");
        } else {
            dashBoardBO.setKpiAcheived((((float) productiveCallsExcludingDeviation / (float) totalCallsForDay) * 100) + "");
        }

        int kpiAcheived = 0;
        int kpiTarget;

        try {
            kpiAcheived = (int) SDUtil.convertToDouble(dashBoardBO.getKpiAcheived());
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

    private void computeDailyAchievementsForDropSizeOrd(DashBoardBO dashBoardBO, DailyReportBO orderValueReport) {
        if (SDUtil.convertToDouble(orderValueReport.getTotLines()) == 0) {
            dashBoardBO.setKpiAcheived("0");
        } else {
            dashBoardBO.setKpiAcheived((SDUtil.convertToDouble(orderValueReport.getTotValues()) / SDUtil.convertToDouble(orderValueReport.getTotLines())) + "");
        }
        int kpiAcheived = 0;
        int kpiTarget;

        try {
            kpiAcheived = (int) SDUtil.convertToDouble(dashBoardBO.getKpiAcheived());
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

    private void computeDailyAchievementsForWeeklySalesObj(DashBoardBO dashBoardBO, DailyReportBO invoiceReport) {
        dashBoardBO.setKpiAcheived((SDUtil.convertToDouble(invoiceReport.getTotValues())) + "");
        int kpiAcheived = 0;
        int kpiTarget;

        try {
            kpiAcheived = (int) SDUtil.convertToDouble(dashBoardBO.getKpiAcheived());
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

    private void computeDailyAchievementsForInitVsWeeklyObj(DashBoardBO dashBoardBO, double focusBrandValue) {
        dashBoardBO.setKpiAcheived((SDUtil.convertToDouble(focusBrandValue + "")) + "");

        int kpiAcheived = 0;
        int kpiTarget;

        try {
            kpiAcheived = (int) SDUtil.convertToDouble(dashBoardBO.getKpiAcheived());
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

    private void computeDailyAchievementsForReturnRateInv(DashBoardBO dashBoardBO, double salesReturnValue, DailyReportBO invoiceReport) {
        if (SDUtil.convertToDouble(invoiceReport.getTotValues()) == 0) {
            dashBoardBO.setKpiAcheived("0");
        } else {
            dashBoardBO.setKpiAcheived(((salesReturnValue / SDUtil.convertToDouble(invoiceReport.getTotValues())) * 100) + "");
        }
        int kpiAcheived = 0;
        int kpiTarget;

        try {
            kpiAcheived = (int) SDUtil.convertToDouble(dashBoardBO.getKpiAcheived());
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

    private void computeDailyAchievementsForReturnRateOrder(DashBoardBO dashBoardBO, double salesReturnValue, DailyReportBO orderValueReport) {
        if (SDUtil.convertToDouble(orderValueReport.getTotValues()) == 0) {
            dashBoardBO.setKpiAcheived("0");
        } else {
            dashBoardBO.setKpiAcheived(((salesReturnValue / SDUtil.convertToDouble(orderValueReport.getTotValues())) * 100) + "");
        }
        int kpiAcheived = 0;
        int kpiTarget;

        try {
            kpiAcheived = (int) SDUtil.convertToDouble(dashBoardBO.getKpiAcheived());
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

    private void computeDailyAchievementsForDropSizeInv(DashBoardBO dashBoardBO, DailyReportBO invoiceReport) {
        if (SDUtil.convertToDouble(invoiceReport.getTotLines()) == 0) {
            dashBoardBO.setKpiAcheived("0");
        } else {
            dashBoardBO.setKpiAcheived((SDUtil.convertToDouble(invoiceReport.getTotValues()) / SDUtil.convertToDouble(invoiceReport.getTotLines())) + "");
        }

        int kpiAcheived = 0;
        int kpiTarget;

        try {
            kpiAcheived = (int) SDUtil.convertToDouble(dashBoardBO.getKpiAcheived());
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

    private void computeDailyAchievementsForFulfilment(DashBoardBO dashBoardBO, DailyReportBO fulFilmentReport) {
        if (fulFilmentReport.getLoaded() == 0) {
            dashBoardBO.setKpiAcheived("0");
        } else {
            dashBoardBO.setKpiAcheived(((fulFilmentReport.getDelivered() / fulFilmentReport.getLoaded()) * 100) + "");
        }

        int kpiAcheived = 0;
        int kpiTarget;

        try {
            kpiAcheived = (int) SDUtil.convertToDouble(dashBoardBO.getKpiAcheived());
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

    private void computeDailyAchievementsForMSL(DashBoardBO dashBoardBO, int mslCount, int mslAchievedCount) {
        dashBoardBO.setKpiAcheived(mslAchievedCount + "");
        int kpiAcheived = mslAchievedCount;
        int kpiTarget;

        try {
            kpiTarget = (mslCount);
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

    private void computeDailyAchievementsForPRM(DashBoardBO dashBoardBO, int promotionCount, int promotionAchievedCount) {

        dashBoardBO.setKpiAcheived(promotionAchievedCount + "");
        int kpiAcheived = promotionAchievedCount;
        int kpiTarget;

        try {
            kpiTarget = (promotionCount);
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

}
