package com.ivy.ui.photocapture.data;

import com.ivy.cpg.view.photocapture.PhotoCaptureLocationBO;
import com.ivy.cpg.view.photocapture.PhotoCaptureProductBO;
import com.ivy.cpg.view.photocapture.PhotoTypeMasterBO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;

public interface PhotoCaptureDataManager {

    Observable<ArrayList<PhotoCaptureProductBO>> fetchPhotoCaptureProducts();

    Observable<ArrayList<PhotoCaptureLocationBO>> fetchEditedLocations(final String retailerID, final int distributorId);

    Observable<ArrayList<PhotoCaptureLocationBO>> fetchLocations();

    Observable<ArrayList<PhotoTypeMasterBO>> fetchPhotoCaptureTypes();

    Single<Boolean> updatePhotoCaptureDetails(HashMap<String,PhotoCaptureLocationBO> updatedData);

}
