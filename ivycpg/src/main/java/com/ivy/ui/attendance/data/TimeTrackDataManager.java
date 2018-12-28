package com.ivy.ui.attendance.data;

import com.ivy.core.data.AppDataManagerContract;
import com.ivy.cpg.view.nonfield.NonFieldTwoBo;

import java.util.ArrayList;

import io.reactivex.Observable;

/**
 * Created by mansoor on 27/12/2018
 */
public interface TimeTrackDataManager extends AppDataManagerContract {

    Observable<ArrayList<NonFieldTwoBo>> getTimeTrackList();
}
