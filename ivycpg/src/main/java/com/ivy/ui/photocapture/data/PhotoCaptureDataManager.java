package com.ivy.ui.photocapture.data;

import com.ivy.cpg.view.photocapture.PhotoCaptureLocationBO;
import com.ivy.cpg.view.photocapture.PhotoCaptureProductBO;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

public interface PhotoCaptureDataManager {

    Observable<ArrayList<PhotoCaptureProductBO>> fetchPhotoCaptureProducts();

    Observable<ArrayList<PhotoCaptureLocationBO>> fetchEditedLocations(final String retailerID, final int distributorId);


    Observable<ArrayList<PhotoCaptureLocationBO>> fetchLocations();
}
