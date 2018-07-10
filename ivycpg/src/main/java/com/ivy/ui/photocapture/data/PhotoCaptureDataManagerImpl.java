package com.ivy.ui.photocapture.data;

import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.cpg.view.photocapture.PhotoCaptureLocationBO;
import com.ivy.cpg.view.photocapture.PhotoCaptureProductBO;
import com.ivy.lib.existing.DBUtil;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;

public class PhotoCaptureDataManagerImpl implements PhotoCaptureDataManager {


    private DBUtil mDbUtil;


    @Inject
    public PhotoCaptureDataManagerImpl(@DataBaseInfo DBUtil dbUtil) {
        mDbUtil = dbUtil;
    }

    @Override
    public Observable<List<PhotoCaptureProductBO>> fetchPhotoCaptureProducts() {
        return null;
    }

    @Override
    public Observable<List<PhotoCaptureLocationBO>> fetchLocations() {
        return null;
    }
}
