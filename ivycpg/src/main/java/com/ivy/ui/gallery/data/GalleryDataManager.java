package com.ivy.ui.gallery.data;

import com.ivy.core.data.AppDataManagerContract;
import com.ivy.ui.gallery.model.GalleryBo;

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.Observable;

public interface GalleryDataManager extends AppDataManagerContract {


    Observable<HashMap<String, ArrayList<GalleryBo>>> fetchImageData(String imgDirectory, boolean isLastVisit);

}
