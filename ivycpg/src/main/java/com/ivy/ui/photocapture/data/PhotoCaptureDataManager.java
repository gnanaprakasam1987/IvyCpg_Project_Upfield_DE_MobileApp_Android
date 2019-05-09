package com.ivy.ui.photocapture.data;

import com.ivy.core.data.AppDataManagerContract;
import com.ivy.ui.photocapture.model.PhotoCaptureLocationBO;
import com.ivy.ui.photocapture.model.PhotoCaptureProductBO;
import com.ivy.ui.photocapture.model.PhotoTypeMasterBO;

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.Single;

public interface PhotoCaptureDataManager extends AppDataManagerContract {

    Observable<ArrayList<PhotoCaptureProductBO>> fetchPhotoCaptureProducts();

    Observable<ArrayList<PhotoCaptureLocationBO>> fetchEditedLocations(final String retailerID, final int distributorId);

    Observable<ArrayList<PhotoCaptureLocationBO>> fetchLocations();

    Observable<ArrayList<PhotoTypeMasterBO>> fetchPhotoCaptureTypes();

    Single<Boolean> updatePhotoCaptureDetails(HashMap<String,PhotoCaptureLocationBO> updatedData);

}
