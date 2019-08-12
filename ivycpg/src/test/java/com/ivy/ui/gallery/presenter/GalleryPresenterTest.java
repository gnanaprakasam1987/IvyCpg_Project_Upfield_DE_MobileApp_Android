package com.ivy.ui.gallery.presenter;

import com.ivy.core.data.datamanager.DataManager;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.ui.gallery.GalleryContract;
import com.ivy.ui.gallery.GalleryDataFactory;
import com.ivy.ui.gallery.data.GalleryDataManager;
import com.ivy.ui.gallery.model.GalleryBo;
import com.ivy.utils.rx.TestSchedulerProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.TestScheduler;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@RunWith(MockitoJUnitRunner.class)
public class GalleryPresenterTest {


    private GalleryContract.GalleryView galleryView;

    @Mock
    private DataManager mDataManager;

    private CompositeDisposable mockDisposable = new CompositeDisposable();

    @Mock
    private ConfigurationMasterHelper mockConfigurationHelper;

    @Mock
    private GalleryDataManager mockGalleryDataManager;

    private TestScheduler testScheduler = new TestScheduler();

    private GalleryPresenterImpl<GalleryContract.GalleryView> presenter;

    @Before
    public void setUp() {
        TestSchedulerProvider testSchedulerProvider = new TestSchedulerProvider(testScheduler);

        presenter = new GalleryPresenterImpl<>(mDataManager,
                testSchedulerProvider, mockDisposable,
                mockConfigurationHelper, galleryView,
                mockGalleryDataManager);
    }

    @Test
    public void fetchGalleryDataSuccess() {
        HashMap<String, ArrayList<GalleryBo>> mockGalleryList = GalleryDataFactory.getMockHashList();
        ArrayList<String> sectionList = new ArrayList<>(mockGalleryList.keySet());
        GalleryDataFactory.userMasterBO.setUserid(123);
        given(mDataManager.getUser()).willReturn(GalleryDataFactory.userMasterBO);
        assertEquals(presenter.getUserID(), 123);

        given(mockGalleryDataManager.fetchImageData("file/IvyDist", false))
                .willReturn(Observable.fromCallable(new Callable<HashMap<String, ArrayList<GalleryBo>>>() {
                    @Override
                    public HashMap<String, ArrayList<GalleryBo>> call() throws Exception {
                        return mockGalleryList;
                    }
                }));

        presenter.fetchGalleryData("file/IvyDist", false);
        testScheduler.triggerActions();
        then(galleryView).should().showLoading();
        then(galleryView).should().updateGalleryView(mockGalleryList, sectionList);
        then(galleryView).should().hideLoading();

    }

    @Test
    public void fetchGalleryDataFail() {

        GalleryDataFactory.userMasterBO.setUserid(123);
        given(mDataManager.getUser()).willReturn(GalleryDataFactory.userMasterBO);
        assertEquals(presenter.getUserID(), 123);

        given(mockGalleryDataManager.fetchImageData("file/IvyDist", false))
                .willReturn(Observable.fromCallable(new Callable<HashMap<String, ArrayList<GalleryBo>>>() {
                    @Override
                    public HashMap<String, ArrayList<GalleryBo>> call() throws Exception {
                        return new HashMap<>();
                    }
                }));

        presenter.fetchGalleryData("file/IvyDist", false);
        testScheduler.triggerActions();
        then(galleryView).should().showLoading();
        then(galleryView).should().showDataNotMappedMsg();

    }

    @Test
    public void updateFilteredDataSuccess() {
        GalleryDataFactory.userMasterBO.setUserid(123);
        given(mDataManager.getUser()).willReturn(GalleryDataFactory.userMasterBO);
        assertEquals(presenter.getUserID(), 123);

        HashMap<String, ArrayList<GalleryBo>> mockGalleryList = GalleryDataFactory.getMockHashList();
        ArrayList<String> sectionList = new ArrayList<>(mockGalleryList.keySet());

        given(mockGalleryDataManager.fetchImageData("file/IvyDist", false))
                .willReturn(Observable.fromCallable(new Callable<HashMap<String, ArrayList<GalleryBo>>>() {
                    @Override
                    public HashMap<String, ArrayList<GalleryBo>> call() throws Exception {
                        return mockGalleryList;
                    }
                }));

        presenter.updateSectionedFilterList(sectionList);
        testScheduler.triggerActions();
        then(galleryView).should().updateFilteredData(mockGalleryList, sectionList);
    }


    @Test
    public void updateAllDataSuccess() {
        GalleryDataFactory.userMasterBO.setUserid(123);
        given(mDataManager.getUser()).willReturn(GalleryDataFactory.userMasterBO);
        assertEquals(presenter.getUserID(), 123);

        HashMap<String, ArrayList<GalleryBo>> mockGalleryList = GalleryDataFactory.getMockHashList();
        ArrayList<String> sectionList = new ArrayList<>(mockGalleryList.keySet());

        given(mockGalleryDataManager.fetchImageData("file/IvyDist", false))
                .willReturn(Observable.fromCallable(new Callable<HashMap<String, ArrayList<GalleryBo>>>() {
                    @Override
                    public HashMap<String, ArrayList<GalleryBo>> call() throws Exception {
                        return mockGalleryList;
                    }
                }));

        presenter.updateSectionedFilterList(null);
        testScheduler.triggerActions();
        then(galleryView).should().updateGalleryView(mockGalleryList, sectionList);
    }
}
