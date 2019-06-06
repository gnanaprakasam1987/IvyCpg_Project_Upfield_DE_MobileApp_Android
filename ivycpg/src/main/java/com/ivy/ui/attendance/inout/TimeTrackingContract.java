package com.ivy.ui.attendance.inout;

import android.app.Activity;
import android.content.Context;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.cpg.view.nonfield.NonFieldTwoBo;
import com.ivy.location.LocationUtil;
import com.ivy.sd.png.bo.ReasonMaster;

import java.util.ArrayList;

/**
 * Created by mansoor on 27/12/2018
 */
public interface TimeTrackingContract {

    interface  TimeTrackingView extends BaseIvyView{
        void populateDataToList(ArrayList<NonFieldTwoBo> timeTrackList);

        boolean isUpdateRealTimeIn();

        void updateRealTimeOut();

        void showInOutDialog(ArrayList<ReasonMaster> reasonList);

        void uploadAttendance(String inOrOut);

        void uploadAttendanceToServer();

    }

    @PerActivity
    interface  TimeTrackingPresenter<V extends TimeTrackingView> extends BaseIvyPresenter<V>{
        void fetchData(boolean isLoading);

        void stopLocationService(String reasonId);

        void updateTimeTrackDetails(NonFieldTwoBo nonFieldTwoBo);

        boolean isRealTimeLocationOn();

        boolean isShowCapturedLocation();

        boolean isPreviousInOutCompleted(ArrayList<NonFieldTwoBo> timeTrackList);

        boolean isAttendanceRemark();

        void fetchInOutReason();

        void saveInOutDetails(String reasonId, String remarks);
    }
}
