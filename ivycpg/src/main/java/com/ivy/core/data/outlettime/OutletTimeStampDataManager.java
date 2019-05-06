package com.ivy.core.data.outlettime;

import com.ivy.core.data.AppDataManagerContract;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

public interface OutletTimeStampDataManager extends AppDataManagerContract {

    Single<Boolean> isVisited(String retailerId);

    Single<Boolean> updateTimeStampModuleWise(String timeOut);

    Completable saveTimeStampModuleWise(String datetimeIn, String moduleCode);

    Completable deleteTimeStamps();

    Completable updateTimeStamp(String timeOut, String reasonDesc, int batteryPercentage, boolean isGPSEnabled);
}
