package com.ivy.ui.attendance.presenter;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.cpg.locationservice.LocationConstants;
import com.ivy.cpg.locationservice.realtime.FireBaseRealtimeLocationUpload;
import com.ivy.cpg.locationservice.realtime.RealTimeLocation;
import com.ivy.cpg.view.nonfield.NonFieldTwoBo;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.ui.attendance.TimeTrackingContract;
import com.ivy.ui.attendance.data.TimeTrackDataManager;
import com.ivy.utils.rx.SchedulerProvider;

import java.util.ArrayList;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.REALTIME_LOCATION_PATH;

/**
 * Created by mansoor on 27/12/2018
 */
public class TimeTrackPresenterImpl<V extends TimeTrackingContract.TimeTrackingView> extends BasePresenter<V> implements TimeTrackingContract.TimeTrackingPresenter<V> {
    private TimeTrackDataManager timeTrackDataManager;
    private ConfigurationMasterHelper configurationMasterHelper;

    @Inject
    public TimeTrackPresenterImpl(DataManager dataManager,
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
        getIvyView().showLoading();

        getCompositeDisposable().add(timeTrackDataManager.getTimeTrackList()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui()).subscribe(new Consumer<ArrayList<NonFieldTwoBo>>() {
                    @Override
                    public void accept(ArrayList<NonFieldTwoBo> value) {
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
    public boolean stopLocationService(String reasonId) {
        if (configurationMasterHelper.IS_REALTIME_LOCATION_CAPTURE
                && timeTrackDataManager.isWorkingStatus(Integer.parseInt(reasonId)))
            getIvyView().updateRealTimeOut();

        getIvyView().uploadAttendance("OUT", reasonId);

        return false;
    }


    @Override
    public void updateTimeTrackDetails(NonFieldTwoBo nonFieldTwoBo) {
        getIvyView().showLoading();
        timeTrackDataManager.updateTimeTrackDetailsDb(nonFieldTwoBo);
        getIvyView().hideLoading();
        fetchData();
    }

    @Override
    public void onDetach() {
        timeTrackDataManager.tearDown();
        super.onDetach();

    }
}
