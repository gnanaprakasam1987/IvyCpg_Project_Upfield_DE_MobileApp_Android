package com.ivy.ui.photocapture.data;

import com.ivy.cpg.view.photocapture.PhotoCaptureLocationBO;
import com.ivy.cpg.view.photocapture.PhotoCaptureProductBO;

import java.util.List;

import io.reactivex.Observable;

public interface PhotoCaptureDataManager {

    Observable<List<PhotoCaptureProductBO>> fetchPhotoCaptureProducts();


    Observable<List<PhotoCaptureLocationBO>> fetchLocations();
}
