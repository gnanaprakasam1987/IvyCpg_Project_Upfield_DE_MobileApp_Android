package com.ivy.core.data.outlettime;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

public interface OutletTimeStampDataManager {

    Single<Boolean> isVisited(String retailerId);

    Completable updateTimeStampModuleWise(String timeOut);

    Completable saveTimeStampModuleWise(String date, String timeIn, String moduleCode);

    Completable deleteTimeStamps();

    Completable updateTimeStamp(String timeOut, String reasonDesc, int batteryPercentage, boolean isGPSEnabled);
}
