package com.ivy.ui.photocapture.presenter;

import com.ivy.core.IvyConstants;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.core.data.label.LabelsDataManager;
import com.ivy.core.data.outlettime.OutletTimeStampDataManager;
import com.ivy.ui.photocapture.model.PhotoCaptureLocationBO;
import com.ivy.ui.photocapture.model.PhotoCaptureProductBO;
import com.ivy.ui.photocapture.model.PhotoTypeMasterBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.ui.photocapture.PhotoCaptureContract;
import com.ivy.ui.photocapture.PhotoCaptureTestDataFactory;
import com.ivy.ui.photocapture.data.PhotoCaptureDataManager;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.rx.TestSchedulerProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.TestScheduler;

import static com.ivy.utils.DateTimeUtils.DATE_GLOBAL;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class PhotoCapturePresenterTest {

    @Mock
    private PhotoCaptureContract.PhotoCaptureView mView;

    @Mock
    private
    DataManager mDataManager;

    private CompositeDisposable mockDisposable = new CompositeDisposable();

    @Mock
    private ConfigurationMasterHelper mockConfigurationHelper;

    private TestScheduler testScheduler = new TestScheduler();

    @Mock
    private PhotoCaptureDataManager photoCaptureDataManager;

    @Mock
    private LabelsDataManager labelsMasterHelper;

    @Mock
    private OutletTimeStampDataManager outletTimeStampDataManager;

    private PhotoCapturePresenterImpl<PhotoCaptureContract.PhotoCaptureView> mPresenter;


    @Before
    public void setup() {

        TestSchedulerProvider testSchedulerProvider = new TestSchedulerProvider(testScheduler);
        mPresenter = new PhotoCapturePresenterImpl<>(mDataManager, testSchedulerProvider, mockDisposable,
                mockConfigurationHelper, mView, outletTimeStampDataManager, photoCaptureDataManager, labelsMasterHelper);
    }


    @Test
    public void testFetchData() {


        given(mDataManager.getRetailMaster()).willReturn(mock(RetailerMasterBO.class));

        given(photoCaptureDataManager.fetchEditedLocations(mDataManager.getRetailMaster().getRetailerID(), mDataManager.getRetailMaster().getDistributorId())).willReturn(Observable.fromCallable(new Callable<ArrayList<PhotoCaptureLocationBO>>() {
            @Override
            public ArrayList<PhotoCaptureLocationBO> call() {
                return PhotoCaptureTestDataFactory.getPhotoCaptureLocationList();
            }
        }));

        given(photoCaptureDataManager.fetchPhotoCaptureProducts()).willReturn(Observable.fromCallable(new Callable<ArrayList<PhotoCaptureProductBO>>() {
            @Override
            public ArrayList<PhotoCaptureProductBO> call() {
                return PhotoCaptureTestDataFactory.getPhotoCaptureProductList();
            }
        }));

        given(photoCaptureDataManager.fetchPhotoCaptureTypes()).willReturn(Observable.fromCallable(new Callable<ArrayList<PhotoTypeMasterBO>>() {
            @Override
            public ArrayList<PhotoTypeMasterBO> call() {
                return PhotoCaptureTestDataFactory.getPhotoCaptureTypeList();
            }
        }));

        given(photoCaptureDataManager.fetchPhotoCaptureTypes()).willReturn(Observable.fromCallable(new Callable<ArrayList<PhotoTypeMasterBO>>() {
            @Override
            public ArrayList<PhotoTypeMasterBO> call() {
                return PhotoCaptureTestDataFactory.getPhotoCaptureTypeList();
            }
        }));

        given(photoCaptureDataManager.fetchLocations()).willReturn(Observable.fromCallable(new Callable<ArrayList<PhotoCaptureLocationBO>>() {
            @Override
            public ArrayList<PhotoCaptureLocationBO> call() {
                return PhotoCaptureTestDataFactory.getPhotoCaptureLocationList();
            }
        }));

        mPresenter.fetchData();

        testScheduler.triggerActions();
        then(mView).should().showLoading();
        then(mView).should().hideLoading();


    }

    @Test
    public void testFetchEditDataFailed(){

        given(mDataManager.getRetailMaster()).willReturn(mock(RetailerMasterBO.class));

        given(photoCaptureDataManager.fetchEditedLocations(mDataManager.getRetailMaster().getRetailerID(), mDataManager.getRetailMaster().getDistributorId())).willReturn(Observable.<ArrayList<PhotoCaptureLocationBO>>error(new Throwable()));

        mPresenter.fetchEditedPhotoTypes();
        testScheduler.triggerActions();

        then(mView).shouldHaveZeroInteractions();

    }

    @Test
    public void testFetchDataProductFail() {


        given(mDataManager.getRetailMaster()).willReturn(mock(RetailerMasterBO.class));

        given(photoCaptureDataManager.fetchEditedLocations(mDataManager.getRetailMaster().getRetailerID(), mDataManager.getRetailMaster().getDistributorId())).willReturn(Observable.fromCallable(new Callable<ArrayList<PhotoCaptureLocationBO>>() {
            @Override
            public ArrayList<PhotoCaptureLocationBO> call() {
                return PhotoCaptureTestDataFactory.getPhotoCaptureLocationList();
            }
        }));

        given(photoCaptureDataManager.fetchPhotoCaptureProducts()).willReturn(Observable.<ArrayList<PhotoCaptureProductBO>>error(new Throwable()));

        given(photoCaptureDataManager.fetchPhotoCaptureTypes()).willReturn(Observable.fromCallable(new Callable<ArrayList<PhotoTypeMasterBO>>() {
            @Override
            public ArrayList<PhotoTypeMasterBO> call() {
                return PhotoCaptureTestDataFactory.getPhotoCaptureTypeList();
            }
        }));

        given(photoCaptureDataManager.fetchPhotoCaptureTypes()).willReturn(Observable.fromCallable(new Callable<ArrayList<PhotoTypeMasterBO>>() {
            @Override
            public ArrayList<PhotoTypeMasterBO> call() {
                return PhotoCaptureTestDataFactory.getPhotoCaptureTypeList();
            }
        }));

        given(photoCaptureDataManager.fetchLocations()).willReturn(Observable.fromCallable(new Callable<ArrayList<PhotoCaptureLocationBO>>() {
            @Override
            public ArrayList<PhotoCaptureLocationBO> call() {
                return PhotoCaptureTestDataFactory.getPhotoCaptureLocationList();
            }
        }));

        mPresenter.fetchData();

        testScheduler.triggerActions();
        then(mView).should().showLoading();
        then(mView).should().onError("Something went wrong");
        then(mView).should().hideLoading();

    }


    @Test
    public void testMaxPhotoLimitReached() {
        given(mDataManager.getSavedImageCount()).willReturn(6);

        mockConfigurationHelper.photocount = 5;

        assertEquals(mPresenter.isMaxPhotoLimitReached(), true);
    }


    @Test
    public void testMaxPhotoLimitNotReached() {
        given(mDataManager.getSavedImageCount()).willReturn(5);

        mockConfigurationHelper.photocount = 6;

        assertEquals(mPresenter.isMaxPhotoLimitReached(), false);
    }

    @Test
    public void testGlobalLocation() {
        mockConfigurationHelper.IS_GLOBAL_LOCATION = true;
        assertEquals(mPresenter.isGlobalLocation(), true);
    }

    @Test
    public void testGlobalNotLocation() {
        mockConfigurationHelper.IS_GLOBAL_LOCATION = false;
        assertEquals(mPresenter.isGlobalLocation(), false);
    }

    @Test
    public void testDateNotEnabled() {
        mockConfigurationHelper.SHOW_DATE_BTN = false;
        assertEquals(mPresenter.isDateEnabled(), false);
    }

    @Test
    public void testDateEnabled() {
        mockConfigurationHelper.SHOW_DATE_BTN = true;
        assertEquals(mPresenter.isDateEnabled(), true);
    }

    @Test
    public void testShouldNotNavigateToNextActivity() {
        mockConfigurationHelper.IS_PRINT_FILE_SAVE = false;
        assertEquals(mPresenter.shouldNavigateToNextActivity(), false);
    }

    @Test
    public void testShouldNavigateToNextActivity() {
        mockConfigurationHelper.IS_PRINT_FILE_SAVE = true;
        assertEquals(mPresenter.shouldNavigateToNextActivity(), true);
    }

    @Test
    public void testPhotoPathChanged() {
        mockConfigurationHelper.IS_PHOTO_CAPTURE_IMG_PATH_CHANGE = true;
        assertEquals(mPresenter.isImagePathChanged(), true);
    }

    @Test
    public void testPhotoPathNotChanged() {
        mockConfigurationHelper.IS_PHOTO_CAPTURE_IMG_PATH_CHANGE = false;
        assertEquals(mPresenter.isImagePathChanged(), false);
    }

    @Test
    public void testGetRetailerId() {

        PhotoCaptureTestDataFactory.retailerMasterBO.setRetailerID("1");

        given(mDataManager.getRetailMaster()).willReturn(PhotoCaptureTestDataFactory.retailerMasterBO);

        assertEquals(mPresenter.getRetailerId(), "1");

    }

    @Test
    public void testGetTitleLabel() {

        given(labelsMasterHelper.getLabel("menu_photo")).willReturn(Single.just("Hello"));

        mPresenter.getTitleLabel();
        testScheduler.triggerActions();

        then(mView).should().setToolBarTitle("Hello");

    }


    @Test
    public void testUpdateLocalData() {

        mockConfigurationHelper.IS_PHOTO_CAPTURE_IMG_PATH_CHANGE = true;

        UserMasterBO userMasterBO = new UserMasterBO(1, "");
        userMasterBO.setDownloadDate("abcd");

        given(mDataManager.getUser()).willReturn(userMasterBO);

        mPresenter.updateLocalData(0, 0, 0, "", "", "", "", "", "", "", "", "");

        then(mView).shouldHaveZeroInteractions();

        mockConfigurationHelper.SHOW_DATE_BTN = true;

        mPresenter.updateLocalData(0, 0, 0, "", "", "", "", "");

        then(mView).should().getFromDate();

        then(mView).should().getToDate();

        then(mView).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testSaveDataSuccess() {

        HashMap<String, PhotoCaptureLocationBO> mockMap = new HashMap<>();
        given(photoCaptureDataManager.updatePhotoCaptureDetails(mockMap)).willReturn(Single.just(true));
        String date = DateTimeUtils.now(DATE_GLOBAL) + " " + DateTimeUtils.now(DateTimeUtils.TIME);
        if (mockConfigurationHelper.IS_DISABLE_CALL_ANALYSIS_TIMER)
            date = IvyConstants.DEFAULT_TIME_CONSTANT;
        given(outletTimeStampDataManager.updateTimeStampModuleWise(date)).willReturn(Single.just(true));
        given(mDataManager.updateModuleTime(HomeScreenTwo.MENU_PHOTO)).willReturn(Single.just(true));

        mPresenter.onSaveButtonClick();
        testScheduler.triggerActions();

        then(mView).should().showLoading();
        then(mView).should().showUpdatedDialog();
        then(mView).should().hideLoading();

    }


    @Test
    public void testSaveDataUpdateDataFail() {

        HashMap<String, PhotoCaptureLocationBO> mockMap = new HashMap<>();
        given(photoCaptureDataManager.updatePhotoCaptureDetails(mockMap)).willReturn(Single.just(false));
        String date = DateTimeUtils.now(DATE_GLOBAL) + " " + DateTimeUtils.now(DateTimeUtils.TIME);
        if (mockConfigurationHelper.IS_DISABLE_CALL_ANALYSIS_TIMER)
            date = IvyConstants.DEFAULT_TIME_CONSTANT;
        given(outletTimeStampDataManager.updateTimeStampModuleWise(date)).willReturn(Single.just(true));
        given(mDataManager.updateModuleTime(HomeScreenTwo.MENU_PHOTO)).willReturn(Single.just(true));

        mPresenter.onSaveButtonClick();
        testScheduler.triggerActions();

        then(mView).should().showLoading();
        then(mView).should().hideLoading();
        then(mView).shouldHaveNoMoreInteractions();

    }


    @Test
    public void testSaveDataUpdateTimeStampFail() {

        HashMap<String, PhotoCaptureLocationBO> mockMap = new HashMap<>();
        given(photoCaptureDataManager.updatePhotoCaptureDetails(mockMap)).willReturn(Single.just(true));
        String date = DateTimeUtils.now(DATE_GLOBAL) + " " + DateTimeUtils.now(DateTimeUtils.TIME);
        if (mockConfigurationHelper.IS_DISABLE_CALL_ANALYSIS_TIMER)
            date = IvyConstants.DEFAULT_TIME_CONSTANT;
        given(outletTimeStampDataManager.updateTimeStampModuleWise(date)).willReturn(Single.just(false));
        given(mDataManager.updateModuleTime(HomeScreenTwo.MENU_PHOTO)).willReturn(Single.just(true));

        mPresenter.onSaveButtonClick();
        testScheduler.triggerActions();

        then(mView).should().showLoading();
        then(mView).should().hideLoading();
        then(mView).shouldHaveNoMoreInteractions();

    }

    @Test
    public void testSaveDataUpdateModuleTimeFail() {

        HashMap<String, PhotoCaptureLocationBO> mockMap = new HashMap<>();
        given(photoCaptureDataManager.updatePhotoCaptureDetails(mockMap)).willReturn(Single.just(true));
        String date = DateTimeUtils.now(DATE_GLOBAL) + " " + DateTimeUtils.now(DateTimeUtils.TIME);
        if (mockConfigurationHelper.IS_DISABLE_CALL_ANALYSIS_TIMER)
            date = IvyConstants.DEFAULT_TIME_CONSTANT;
        given(outletTimeStampDataManager.updateTimeStampModuleWise(date)).willReturn(Single.just(true));
        given(mDataManager.updateModuleTime(HomeScreenTwo.MENU_PHOTO)).willReturn(Single.just(false));

        mPresenter.onSaveButtonClick();
        testScheduler.triggerActions();

        then(mView).should().showLoading();
        then(mView).should().hideLoading();
        then(mView).shouldHaveNoMoreInteractions();

    }

    @Test
    public void testUpdateModuleTime() {
        String date = DateTimeUtils.now(DATE_GLOBAL) + " " + DateTimeUtils.now(DateTimeUtils.TIME);
        if (mockConfigurationHelper.IS_DISABLE_CALL_ANALYSIS_TIMER)
            date = IvyConstants.DEFAULT_TIME_CONSTANT;
        given(outletTimeStampDataManager.updateTimeStampModuleWise(date)).willReturn(Single.just(true));

        mPresenter.updateModuleTime();
        testScheduler.triggerActions();

        then(mView).shouldHaveZeroInteractions();
    }

    @Test
    public void testSetEditedPhotosListData() {

        mPresenter.setEditedPhotosListData(new HashMap<String, PhotoCaptureLocationBO>());

        then(mView).should().setSpinnerDefaults();

    }

    @Test
    public void testGetGlobalLocationIndex() {
        given(mDataManager.getGlobalLocationIndex()).willReturn(1);

        assertEquals(mPresenter.getGlobalLocationIndex(), 1);

    }

    @After
    public void tearDown() {
        mPresenter.onDetach();
    }


}



