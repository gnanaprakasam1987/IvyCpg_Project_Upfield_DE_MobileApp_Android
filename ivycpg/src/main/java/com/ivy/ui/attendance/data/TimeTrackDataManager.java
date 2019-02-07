package com.ivy.ui.attendance.data;

import com.ivy.core.data.AppDataManagerContract;
import com.ivy.cpg.view.nonfield.NonFieldTwoBo;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.bo.StandardListBO;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Created by mansoor on 27/12/2018
 */
public interface TimeTrackDataManager extends AppDataManagerContract {

    Observable<ArrayList<NonFieldTwoBo>> getTimeTrackList();

    Single<Boolean> isWorkingStatus(int reasonId);

    Single<Boolean> updateTimeTrackDetailsDb(NonFieldTwoBo nonFieldTwoBo);

    Observable<ArrayList<ReasonMaster>> getInOutReasonList();

    Single<Boolean> saveTimeTrackDetailsDb(String reasonId, String remarks,double latitude , double longitude);

    Single<Boolean> checkIsLeave();
}