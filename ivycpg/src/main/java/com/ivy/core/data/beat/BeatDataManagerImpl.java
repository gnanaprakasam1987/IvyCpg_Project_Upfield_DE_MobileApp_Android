package com.ivy.core.data.beat;

import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.BeatMasterBO;

import java.util.ArrayList;

import javax.inject.Inject;

import io.reactivex.Observable;

public class BeatDataManagerImpl implements BeatDataManager {

    private DBUtil mDbUtil;

    private AppDataProvider appDataProvider;

    @Inject
    public BeatDataManagerImpl(@DataBaseInfo DBUtil dbUtil, AppDataProvider appDataProvider) {
        this.mDbUtil = dbUtil;
        this.appDataProvider = appDataProvider;

    }

    @Override
    public Observable<ArrayList<BeatMasterBO>> fetchBeats() {
        return null;
    }

    @Override
    public Observable<ArrayList<BeatMasterBO>> fetchBeatsForUser(int userId) {
        return null;
    }

    @Override
    public Observable<ArrayList<BeatMasterBO>> fetchAdhocPlannedBeats() {
        return null;
    }
}
