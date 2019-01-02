package com.ivy.ui.attendance;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.cpg.view.nonfield.NonFieldTwoBo;

import java.util.ArrayList;

/**
 * Created by mansoor on 27/12/2018
 */
public interface TimeTrackingContract {

    interface  TimeTrackingView extends BaseIvyView{
        void populateDataToList(ArrayList<NonFieldTwoBo> timeTrackList);

        boolean updateRealTimeIn();

        boolean updateRealTimeOut();

        void uploadAttendance(String IN_OUT, String reasonId );
    }

    @PerActivity
    interface  TimeTrackingPresenter<V extends TimeTrackingView> extends BaseIvyPresenter<V>{
        void fetchData();

        boolean checkConfigandWorkStatus(int reasonId);

        boolean startLocationService(String reasonId);

        boolean stopLocationService(String reasonId);

        void updateTimeTrackDetails(NonFieldTwoBo nonFieldTwoBo);



    }
}
