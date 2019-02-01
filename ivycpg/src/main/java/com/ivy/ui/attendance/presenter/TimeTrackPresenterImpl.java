package com.ivy.ui.attendance.presenter;

import android.app.Activity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.cpg.view.nonfield.NonFieldTwoBo;
import com.ivy.location.LocationUtil;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.UserMasterHelper;
import com.ivy.sd.png.view.HomeScreenFragment;
import com.ivy.ui.attendance.TimeTrackingContract;
import com.ivy.ui.attendance.data.TimeTrackDataManager;
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
    private LocationUtil locationUtil;
    private ArrayList<NonFieldTwoBo> nonFieldTwoBoList;

    @Inject
    public TimeTrackPresenterImpl(DataManager dataManager,
                                  SchedulerProvider schedulerProvider,
                                  CompositeDisposable compositeDisposable,
                                  ConfigurationMasterHelper configurationMasterHelper,
                                  V view,
                                  TimeTrackDataManager timeTrackDataManager,
                                  LocationUtil locationUtil) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, view);
        this.timeTrackDataManager = timeTrackDataManager;
        this.configurationMasterHelper = configurationMasterHelper;
        this.locationUtil = locationUtil;


    }

    @Override
    public void fetchData() {
        nonFieldTwoBoList = new ArrayList<>();
        getIvyView().showLoading();

        getCompositeDisposable().add(timeTrackDataManager.getTimeTrackList()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui()).subscribe(new Consumer<ArrayList<NonFieldTwoBo>>() {
                    @Override
                    public void accept(ArrayList<NonFieldTwoBo> value) {
                        nonFieldTwoBoList = value;
                        getIvyView().populateDataToList(value);
                        getIvyView().hideLoading();
                    }
                }));

    }

    @Override
    public boolean checkConfigandWorkStatus(int reasonId) {
        return configurationMasterHelper.IS_UPLOAD_ATTENDANCE
                && timeTrackDataManager.isWorkingStatus(reasonId);
    }

    @Override
    public boolean startLocationService(String reasonId) {

        boolean success;
        if (configurationMasterHelper.IS_REALTIME_LOCATION_CAPTURE
                && timeTrackDataManager.isWorkingStatus(Integer.parseInt(reasonId)))

            success = getIvyView().updateRealTimeIn();
        else
            success = true;

        getIvyView().uploadAttendance("IN", reasonId);
        return success;
    }

    @Override
    public void stopLocationService(String reasonId) {
        if (configurationMasterHelper.IS_REALTIME_LOCATION_CAPTURE
                && timeTrackDataManager.isWorkingStatus(Integer.parseInt(reasonId)))
            getIvyView().updateRealTimeOut();

        getIvyView().uploadAttendance("OUT", reasonId);

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
    public LocationUtil getLocationUtil() {
        return locationUtil;
    }

    @Override
    public boolean isPreviousInOutCompeleted() {
        boolean status = false;

        if (nonFieldTwoBoList.size() == 0)
            return true;

        for (NonFieldTwoBo nonFieldTwoBo : nonFieldTwoBoList) {
            status = (nonFieldTwoBo.getInTime() != null && !nonFieldTwoBo.getInTime().trim().equalsIgnoreCase(""))
                    && (nonFieldTwoBo.getOutTime() != null && !nonFieldTwoBo.getOutTime().trim().equalsIgnoreCase(""));
        }


        return status;
    }

    @Override
    public boolean isAttendanceRemark() {
        return configurationMasterHelper.IS_ATTENDANCE_REMARK;
    }

    @Override
    public void fetchReasonAndShowDialog() {
        getIvyView().showLoading();
        getCompositeDisposable().add(timeTrackDataManager.getInOutReasonList()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui()).subscribe(new Consumer<ArrayList<ReasonMaster>>() {
                    @Override
                    public void accept(ArrayList<ReasonMaster> value) {
                        getIvyView().showInOutDialog(value);
                        getIvyView().hideLoading();
                    }
                }));
    }

    @Override
    public void saveInOutDetails(String reasonId, String remarks) {

        if (startLocationService(reasonId)) {
            getIvyView().showLoading();
            getCompositeDisposable().add(timeTrackDataManager.saveTimeTrackDetailsDb(reasonId, remarks, LocationUtil.latitude, LocationUtil.longitude)
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
    }

    private void checkIsLeaveToday() {
        getIvyView().showLoading();
        getCompositeDisposable().add(timeTrackDataManager.checkIsLeave()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui()).subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean isLeave) {
                        getIvyView().hideLoading();
                        if (isLeave) {
                            HomeScreenFragment.isLeave_today = isLeave;
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
