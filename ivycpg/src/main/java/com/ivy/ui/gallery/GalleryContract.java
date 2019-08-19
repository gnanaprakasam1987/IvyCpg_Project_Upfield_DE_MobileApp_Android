package com.ivy.ui.gallery;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.gallery.model.GalleryBo;

import java.util.ArrayList;
import java.util.HashMap;

public interface GalleryContract extends BaseIvyView {

    interface GalleryView extends BaseIvyView {

        void updateGalleryView(HashMap<String, ArrayList<GalleryBo>> galleryList, ArrayList<String> sectionList);

        void updateFilteredData(HashMap<String, ArrayList<GalleryBo>> galleryList, ArrayList<String> filteredSectionList);

        void showDataNotMappedMsg();
    }


    @PerActivity
    interface GalleryPresenter<V extends GalleryView> extends BaseIvyPresenter<V> {

        void fetchGalleryData(String imgDirectory, boolean isLastVisit);

        void updateSectionedFilterList(ArrayList<String> selectedFilterList);

        int getUserID();
    }

}
