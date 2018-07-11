package com.ivy.core.data.outlettime;

import io.reactivex.Single;

public interface OutletTimeStampDataManager {

    Single<Boolean> isVisited(String retailerId);

    Single<Boolean> updateTimeStampModuleWise(String timeOut);

    Single<Boolean> saveTimeStampModuleWise(String date, String timeIn, String moduleCode);

    Single<Boolean> deleteTimeStamps();


}
