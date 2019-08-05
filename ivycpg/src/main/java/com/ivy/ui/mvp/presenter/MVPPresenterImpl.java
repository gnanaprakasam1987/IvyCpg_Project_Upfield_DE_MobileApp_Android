package com.ivy.ui.mvp.presenter;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.ivy.core.ViewTags;
import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.core.data.label.LabelsDataManager;
import com.ivy.core.data.user.UserDataManager;
import com.ivy.core.di.scope.LabelMasterInfo;
import com.ivy.cpg.view.dashboard.DashBoardBO;
import com.ivy.cpg.view.mvp.MvpBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.ui.dashboard.data.SellerDashboardDataManager;
import com.ivy.ui.mvp.MVPContractor;
import com.ivy.ui.mvp.data.MVPDataManager;
import com.ivy.ui.mvp.model.MVPKPIGroupBO;
import com.ivy.utils.rx.SchedulerProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.CompletableObserver;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableObserver;

public class MVPPresenterImpl<V extends MVPContractor.MVPView> extends BasePresenter<V> implements MVPContractor.MVPPresenter<V>, LifecycleObserver {

    private AppDataProvider appDataProvider;
    private MVPDataManager mvpDataManager;
    private LabelsDataManager labelsDataManager;
    private UserDataManager userDataManager;
    private ConfigurationMasterHelper mConfigurationMasterHelper;
    private SellerDashboardDataManager sellerDashboardDataManager;
    private HashMap<String, String> labelsMap = new HashMap<>();

    @Inject
    public MVPPresenterImpl(DataManager dataManager, SchedulerProvider schedulerProvider,
                            CompositeDisposable compositeDisposable,
                            ConfigurationMasterHelper configurationMasterHelper,
                            V view, AppDataProvider mAppDataProvider, MVPDataManager mvpDataManager, @LabelMasterInfo LabelsDataManager labelsDataManager,
                            SellerDashboardDataManager sellerDashboardDataManager, UserDataManager userDataManager) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, view);
        this.appDataProvider = mAppDataProvider;
        this.mvpDataManager = mvpDataManager;
        this.labelsDataManager = labelsDataManager;
        this.mConfigurationMasterHelper = configurationMasterHelper;
        this.sellerDashboardDataManager = sellerDashboardDataManager;
        this.userDataManager = userDataManager;
    }

    private ArrayList<MvpBO> mvpUserList = new ArrayList<>();
    private ArrayList<MvpBO> mvpKPIList = new ArrayList<>();
    private ArrayList<DashBoardBO> dashBoardList = new ArrayList<>();

    @Override
    public ArrayList<MvpBO> getMvpUserList() {
        return mvpUserList;
    }

    public void setMvpUserList(ArrayList<MvpBO> mvpUserList) {
        this.mvpUserList = mvpUserList;
    }

    @Override
    public ArrayList<MvpBO> getMvpKPIList() {
        return mvpKPIList;
    }

    public void setMvpKPIList(ArrayList<MvpBO> mvpKPIList) {
        this.mvpKPIList = mvpKPIList;
    }

    @Override
    public void fetchSellerInfo() {
        getIvyView().showLoading();
        mvpUserList = new ArrayList<>();
        getCompositeDisposable().add(Observable.zip(mvpDataManager.
                fetchSellerInfo(), mvpDataManager.
                fetchMvpKpiAchievements(), new BiFunction<ArrayList<MvpBO>, ArrayList<MvpBO>, Object>() {
            @Override
            public Boolean apply(ArrayList<MvpBO> sellerList, ArrayList<MvpBO> kpiList) throws Exception {
                mvpUserList = sellerList;
                mvpKPIList = kpiList;
                return true;
            }
        })
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        fetchHierarchicalStructure();

                        mvpKPIGroupList = new ArrayList<>();
                        for (MvpBO mvp : mvpKPIList) {
                            MVPKPIGroupBO groupBO = new MVPKPIGroupBO();
                            groupBO.setMvpKPIID(SDUtil.convertToInt(mvp.getKpiId()));
                            groupBO.setMvpKpiName(mvp.getKpiName());
                            mvpKPIGroupList.add(groupBO);
                        }
                        HashSet<MVPKPIGroupBO> set = new HashSet<>(mvpKPIGroupList);
                        mvpKPIGroupList.clear();
                        mvpKPIGroupList.addAll(set);

                        getIvyView().populateKPIFilter();

                        getIvyView().hideLoading();
                    }
                }));
    }

    @Override
    public boolean isLocationConfigurationEnabled() {
        return false;
    }

    @Override
    public boolean isNFCConfigurationEnabled() {
        return false;
    }

    @Override
    public void fetchBadgeList() {

    }

    private ArrayList<String> mvpGroupList = new ArrayList<>();

    @Override
    public void fetchHierarchicalStructure() {
        getIvyView().showLoading();
        mvpGroupList = new ArrayList<>();
        for (MvpBO mvp : mvpUserList) {
            if (!mvpGroupList.contains(mvp.getGroupName())) {
                mvpGroupList.add(mvp.getGroupName());
            }
        }
        getIvyView().populateHierarchy(mvpGroupList);
    }

    private ArrayList<MVPKPIGroupBO> mvpKPIGroupList = new ArrayList<>();

    @Override
    public ArrayList<MVPKPIGroupBO> getMvpKPIGroupList() {
        return mvpKPIGroupList;
    }


    @Override
    public UserMasterBO getUserInfo() {
        return appDataProvider.getUser();
    }

    @Override
    public void fetchSellerDashboardDetails() {
        ArrayList<DashBoardBO> dashBoardList = new ArrayList<>();
        getCompositeDisposable().add(sellerDashboardDataManager.getKPIDashboard(String.valueOf(appDataProvider.getUser().getUserid()), "MONTH")
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<List<DashBoardBO>>() {
                    @Override
                    public void onNext(List<DashBoardBO> dashBoardBOS) {
                        dashBoardList.clear();
                        dashBoardList.addAll(dashBoardBOS);
                        getIvyView().setSellerKPIDetails(dashBoardList);
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
    public HashMap<String, String> getLabelsMap() {
        return labelsMap;
    }

    @Override
    public void fetchSellerDashboardForUserAndInterval(String selectedUser, String interval) {
        getIvyView().showLoading();
        getCompositeDisposable().add(sellerDashboardDataManager.getSellerDashboardForInterval(selectedUser, interval)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<List<DashBoardBO>>() {
                    @Override
                    public void onNext(List<DashBoardBO> dashBoardBOS) {
                        dashBoardList.clear();
                        dashBoardList.addAll(dashBoardBOS);
                        getIvyView().setDashboardListAdapter(dashBoardBOS);
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
    public void updateUserProfile(UserMasterBO userInfo) {
        getIvyView().showLoading();
        userDataManager.updateUserProfile(userInfo)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onComplete() {
                        getIvyView().hideLoading();
                        getIvyView().showUserImageAlert();
                    }

                    @Override
                    public void onError(Throwable e) {
                    }
                });
    }
}
