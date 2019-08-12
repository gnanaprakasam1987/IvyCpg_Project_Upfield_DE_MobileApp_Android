package com.ivy.ui.gallery.presenter;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.ui.gallery.GalleryContract;
import com.ivy.ui.gallery.data.GalleryDataManager;
import com.ivy.ui.gallery.model.GalleryBo;
import com.ivy.utils.rx.SchedulerProvider;

import java.util.ArrayList;
import java.util.HashMap;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

public class GalleryPresenterImpl<V extends GalleryContract.GalleryView> extends BasePresenter<V> implements GalleryContract.GalleryPresenter<V> {


    private GalleryDataManager galleryDataManager;
    private HashMap<String, ArrayList<GalleryBo>> gallListHashMap;
    private ArrayList<String> sectionFilterBoArrayList;
    private DataManager dataManager;

    @Inject
    public GalleryPresenterImpl(DataManager dataManager, SchedulerProvider schedulerProvider,
                                CompositeDisposable compositeDisposable,
                                ConfigurationMasterHelper configurationMasterHelper,
                                V view, GalleryDataManager galleryDataManager) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, view);
        this.galleryDataManager = galleryDataManager;
        this.dataManager = dataManager;
    }

    @Override
    public void fetchGalleryData(String imgDirectory, boolean isLastVisit) {
        getIvyView().showLoading();
        gallListHashMap = new HashMap<>();
        sectionFilterBoArrayList = new ArrayList<>();
        getCompositeDisposable().add(galleryDataManager.fetchImageData(imgDirectory, isLastVisit)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<HashMap<String, ArrayList<GalleryBo>>>() {
                    @Override
                    public void accept(HashMap<String, ArrayList<GalleryBo>> arrayListHashMap) throws Exception {
                        if (!arrayListHashMap.isEmpty()) {
                            gallListHashMap.putAll(arrayListHashMap);
                            sectionFilterBoArrayList.addAll(gallListHashMap.keySet());

                            getIvyView().updateGalleryView(gallListHashMap, sectionFilterBoArrayList);
                            getIvyView().hideLoading();
                        } else {
                            getIvyView().hideLoading();
                            getIvyView().showDataNotMappedMsg();
                        }

                    }
                }));
    }

    @Override
    public void updateSectionedFilterList(ArrayList<String> selectedFilterList) {

        if (selectedFilterList != null
                && !selectedFilterList.isEmpty()) {
            HashMap<String, ArrayList<GalleryBo>> filterGalleryList = new HashMap<>();
            for (String section : selectedFilterList) {
                if (gallListHashMap.get(section) != null)
                    filterGalleryList.put(section, gallListHashMap.get(section));
            }
            getIvyView().updateFilteredData(filterGalleryList, selectedFilterList);
        } else {
            getIvyView().updateGalleryView(gallListHashMap, sectionFilterBoArrayList);
        }
    }

    @Override
    public int getUserID() {
        return dataManager.getUser().getUserid();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        galleryDataManager.tearDown();
        dataManager.tearDown();
    }
}
