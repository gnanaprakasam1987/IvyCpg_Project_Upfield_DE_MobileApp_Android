package com.ivy.ui.attendance.inout.presenter;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.cpg.view.homescreen.HomeScreenFragment;
import com.ivy.cpg.view.nonfield.NonFieldTwoBo;
import com.ivy.location.LocationUtil;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.ui.attendance.inout.TimeTrackingContract;
import com.ivy.ui.attendance.inout.data.TimeTrackDataManager;
import com.ivy.utils.AppUtils;
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
    private ArrayList<NonFieldTwoBo> nonFieldTwoBoList;

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
    public void fetchData() {
        nonFieldTwoBoList = new ArrayList<>();
        getIvyView().showLoading();
        getCompositeDisposable().add(timeTrackDataManager.getTimeTrackList()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui()).subscribe(new Consumer<ArrayList<NonFieldTwoBo>>() {
                    @Override
                    public void accept(ArrayList<NonFieldTwoBo> inoutList) {
                        nonFieldTwoBoList.addAll(inoutList);
                        getIvyView().populateDataToList(inoutList);
                        getIvyView().hideLoading();
                    }
                }));
    }


    @Override
    public void startLocationService(int position) {
        getIvyView().showLoading();
        getCompositeDisposable().add(timeTrackDataManager.isWorkingStatus(Integer.parseInt(nonFieldTwoBoList.get(position).getReason()))
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
                        if (success) {
                            nonFieldTwoBoList.get(position).setInTime(SDUtil.now(SDUtil.DATE_TIME_NEW));
                            updateTimeTrackDetails(nonFieldTwoBoList.get(position));
                        }
                        getIvyView().hideLoading();
                    }
                }));
    }

    @Override
    public void stopLocationService(String reasonId) {
        getIvyView().showLoading();
        getCompositeDisposable().add(timeTrackDataManager.isWorkingStatus(Integer.parseInt(reasonId))
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui()).subscribe(new Consumer<Boolean>() {
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
                        getIvyView().hideLoading();
                        fetchData();
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
    public boolean isPreviousInOutCompleted() {
        boolean status;

        if (nonFieldTwoBoList.size() == 0)
            return true;

        status = (!AppUtils.isNullOrEmpty(nonFieldTwoBoList.get(nonFieldTwoBoList.size() - 1).getInTime()) &&
                !AppUtils.isNullOrEmpty(nonFieldTwoBoList.get(nonFieldTwoBoList.size() - 1).getOutTime()));

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
        NonFieldTwoBo addNonFieldTwoBo = new NonFieldTwoBo();
        addNonFieldTwoBo.setId(getDataManager().getUser().getUserid()
                + SDUtil.now(SDUtil.DATE_TIME_ID) + "");
        addNonFieldTwoBo.setFromDate(SDUtil.now(SDUtil.DATE_GLOBAL));
        addNonFieldTwoBo.setInTime(SDUtil.now(SDUtil.DATE_TIME_NEW));
        addNonFieldTwoBo.setOutTime(null);
        addNonFieldTwoBo.setRemarks(remarks);
        addNonFieldTwoBo.setReason(reasonId);


        getCompositeDisposable().add(timeTrackDataManager.isWorkingStatus(Integer.parseInt(addNonFieldTwoBo.getReason()))
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
                            saveTimeTrackDetailsDb(addNonFieldTwoBo);
                        else
                            getIvyView().hideLoading();
                    }
                }));
    }

    private void saveTimeTrackDetailsDb(NonFieldTwoBo nonFieldTwoBo) {
        getCompositeDisposable().add(timeTrackDataManager.saveTimeTrackDetailsDb(nonFieldTwoBo, LocationUtil.latitude, LocationUtil.longitude)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui()).subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean isSaved) {
                        getIvyView().hideLoading();
                        if (isSaved) {
                            if (configurationMasterHelper.IS_IN_OUT_MANDATE) {
                                checkIsLeaveToday();
                            }
                            fetchData();
                        }

                    }
                }));
    }

    private void checkIsLeaveToday() {
        getIvyView().showLoading();
        getCompositeDisposable().add(timeTrackDataManager.checkIsLeave()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui()).subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean isLeave) {
                        getIvyView().hideLoading();
                        HomeScreenFragment.isLeave_today = isLeave;

                    }
                }));
    }

    @Override
    public void onDetach() {
        timeTrackDataManager.tearDown();
        super.onDetach();

    }


}
