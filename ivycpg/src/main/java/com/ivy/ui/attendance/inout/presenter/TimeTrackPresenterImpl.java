package com.ivy.ui.attendance.inout.presenter;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.cpg.view.homescreen.HomeScreenFragment;
import com.ivy.cpg.view.nonfield.NonFieldTwoBo;
import com.ivy.location.LocationUtil;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.ui.attendance.inout.TimeTrackingContract;
import com.ivy.ui.attendance.inout.data.TimeTrackDataManager;
import com.ivy.utils.StringUtils;
import com.ivy.utils.rx.SchedulerProvider;

import java.util.ArrayList;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

/**
 * Created by mansoor on 27/12/2018
 */
public class TimeTrackPresenterImpl<V extends TimeTrackingContract.TimeTrackingView> extends BasePresenter<V> implements TimeTrackingContract.TimeTrackingPresenter<V> {
    private TimeTrackDataManager timeTrackDataManager;
    private ConfigurationMasterHelper configurationMasterHelper;

    @Inject
    TimeTrackPresenterImpl(DataManager dataManager,
                           SchedulerProvider schedulerProvider,
                           CompositeDisposable compositeDisposable,
                           ConfigurationMasterHelper configurationMasterHelper,
                           V view,
                           TimeTrackDataManager timeTrackDataManager) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, view);
        this.timeTrackDataManager = timeTrackDataManager;
        this.configurationMasterHelper = configurationMasterHelper;
    }

    @Override
    public void fetchData(boolean isLoading) {
        if (!isLoading)
            getIvyView().showLoading();
        getCompositeDisposable().add(timeTrackDataManager.getTimeTrackList()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui()).subscribe(new Consumer<ArrayList<NonFieldTwoBo>>() {
                    @Override
                    public void accept(ArrayList<NonFieldTwoBo> inoutList) {
                        getIvyView().populateDataToList(inoutList);
                        getIvyView().hideLoading();
                    }
                }));
    }

    @Override
    public void stopLocationService(String reasonId) {
        getIvyView().showLoading();
        getCompositeDisposable().add(timeTrackDataManager.isWorkingStatus(Integer.parseInt(reasonId))
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean isWorkingStatus) {
                        if (isWorkingStatus) {
                            if (configurationMasterHelper.IS_REALTIME_LOCATION_CAPTURE)
                                getIvyView().updateRealTimeOut();
                            if (configurationMasterHelper.IS_UPLOAD_ATTENDANCE)
                                getIvyView().uploadAttendance("OUT");
                        }
                        getIvyView().hideLoading();
                    }
                }));
    }


    @Override
    public void updateTimeTrackDetails(NonFieldTwoBo nonFieldTwoBo) {
        getIvyView().showLoading();
        getCompositeDisposable().add(timeTrackDataManager.updateTimeTrackDetailsDb(nonFieldTwoBo)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui()).subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean isUpdated) {
                        fetchData(true);
                    }
                }));
    }

    @Override
    public boolean isRealTimeLocationOn() {
        return configurationMasterHelper.IS_REALTIME_LOCATION_CAPTURE;
    }

    @Override
    public boolean isShowCapturedLocation() {
        return configurationMasterHelper.SHOW_CAPTURED_LOCATION;
    }

    @Override
    public boolean isPreviousInOutCompleted(ArrayList<NonFieldTwoBo> timeTrackList) {
        boolean status;

        if (timeTrackList.isEmpty())
            return true;

        status = (!StringUtils.isNullOrEmpty(timeTrackList.get(timeTrackList.size() - 1).getInTime()) &&
                !StringUtils.isNullOrEmpty(timeTrackList.get(timeTrackList.size() - 1).getOutTime()));

        return status;
    }

    @Override
    public boolean isAttendanceRemark() {
        return configurationMasterHelper.IS_ATTENDANCE_REMARK;
    }

    @Override
    public void fetchInOutReason() {
        getIvyView().showLoading();
        getCompositeDisposable().add(timeTrackDataManager.getInOutReasonList()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui()).subscribe(new Consumer<ArrayList<ReasonMaster>>() {
                    @Override
                    public void accept(ArrayList<ReasonMaster> reasonList) {
                        getIvyView().showInOutDialog(reasonList);
                        getIvyView().hideLoading();
                    }
                }));
    }

    @Override
    public void saveInOutDetails(String reasonId, String remarks) {

        getIvyView().showLoading();
        getCompositeDisposable().add(timeTrackDataManager.isWorkingStatus(Integer.parseInt(reasonId))
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui()).subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean isWorkingStatus) {
                        boolean success = true;
                        if (isWorkingStatus) {
                            if (configurationMasterHelper.IS_REALTIME_LOCATION_CAPTURE)
                                success = getIvyView().isUpdateRealTimeIn();
                            if (configurationMasterHelper.IS_UPLOAD_ATTENDANCE)
                                getIvyView().uploadAttendance("IN");
                        }
                        if (success)
                            saveTimeTrackDetailsDb(reasonId,remarks);
                        else
                            getIvyView().hideLoading();
                    }
                }));
    }

    private void saveTimeTrackDetailsDb(String reasonId, String remarks) {
        getCompositeDisposable().add(timeTrackDataManager.saveTimeTrackDetailsDb(reasonId, remarks, LocationUtil.latitude, LocationUtil.longitude)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui()).subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean isSaved) {
                        if (isSaved) {
                            if (configurationMasterHelper.IS_ATTENDANCE_SYNCUPLOAD)
                                getIvyView().uploadAttendanceToServer();
                            if (configurationMasterHelper.IS_IN_OUT_MANDATE) {
                                HomeScreenFragment.isLeave_today = timeTrackDataManager.checkIsLeave();
                            }
                            fetchData(true);
                        }

                    }
                }));
    }

    @Override
    public void onDetach() {
        timeTrackDataManager.tearDown();
        super.onDetach();

    }


}
