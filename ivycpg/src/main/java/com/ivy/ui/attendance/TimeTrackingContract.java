package com.ivy.ui.attendance;

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

        boolean updateRealTimeIn();

        void updateRealTimeOut();

        void uploadAttendance(String IN_OUT, String reasonId );

        void showInOutDialog(ArrayList<ReasonMaster> reasonList);
    }

    @PerActivity
    interface  TimeTrackingPresenter<V extends TimeTrackingView> extends BaseIvyPresenter<V>{
        void fetchData();

        boolean checkConfigandWorkStatus(int reasonId);

        boolean startLocationService(String reasonId);

        void stopLocationService(String reasonId);

        void updateTimeTrackDetails(NonFieldTwoBo nonFieldTwoBo);

        boolean isRealTimeLocationOn();

        boolean isShowCapturedLocation();

        LocationUtil getLocationUtil();

        boolean isPreviousInOutCompeleted();

        boolean isAttendanceRemark();

        void fetchReasonAndShowDialog();

        void saveInOutDetails(String reasonId, String remarks);

    }
}
