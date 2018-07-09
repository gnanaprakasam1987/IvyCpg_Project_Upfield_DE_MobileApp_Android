package com.ivy.core.base.presenter;

import com.ivy.core.base.view.BaseIvyView;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.utils.rx.TestSchedulerProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.TestScheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@RunWith(MockitoJUnitRunner.class)
public class BasePresenterTest {


    @Mock
    private
    BaseIvyView ivyView;

    private TestScheduler testScheduler = new TestScheduler();

    @Mock
    private
    DataManager mDataManager;

    @Mock
    private ConfigurationMasterHelper mockConfigurationHelper;

    private BasePresenter<BaseIvyView> mPresenter;


    private CompositeDisposable mockDisposable = new CompositeDisposable();

    @Before
    public void setup() {
        TestSchedulerProvider testSchedulerProvider = new TestSchedulerProvider(testScheduler);
        mPresenter = new BasePresenter<>(mDataManager, testSchedulerProvider, mockDisposable, mockConfigurationHelper, ivyView);
    }

    @Test
    public void testBlueTheme() {
        given(mDataManager.getThemeColor()).willReturn(Single.just("blue"));

        //When
        mPresenter.getAppTheme();
        testScheduler.triggerActions();

        then(ivyView).should().setBlueTheme();
    }

    @Test
    public void testPinkTheme() {
        given(mDataManager.getThemeColor()).willReturn(Single.just("pink"));

        //When
        mPresenter.getAppTheme();
        testScheduler.triggerActions();

        then(ivyView).should().setPinkTheme();
    }

    @Test
    public void testOrangeTheme() {
        given(mDataManager.getThemeColor()).willReturn(Single.just("orange"));

        //When
        mPresenter.getAppTheme();
        testScheduler.triggerActions();

        then(ivyView).should().setOrangeTheme();
    }

    @Test
    public void testRedTheme() {
        given(mDataManager.getThemeColor()).willReturn(Single.just("red"));

        //When
        mPresenter.getAppTheme();
        testScheduler.triggerActions();

        then(ivyView).should().setRedTheme();
    }


    @Test
    public void testNavyBlueTheme() {
        given(mDataManager.getThemeColor()).willReturn(Single.just("nblue"));

        //When
        mPresenter.getAppTheme();
        testScheduler.triggerActions();

        then(ivyView).should().setNavyBlueTheme();
    }


    @Test
    public void testGreenTheme() {
        given(mDataManager.getThemeColor()).willReturn(Single.just("green"));

        //When
        mPresenter.getAppTheme();
        testScheduler.triggerActions();

        then(ivyView).should().setGreenTheme();
    }

    @Test
    public void testSetFontSize() {
        given(mDataManager.getFontSize()).willReturn(Single.just("Small"));

        //When
        mPresenter.getAppFontSize();
        testScheduler.triggerActions();

        then(ivyView).should().setFontSize("Small");
    }

    @Test
    public void testIsViewAttached() {
        assertEquals(mPresenter.isViewAttached(), true);
    }

    @Test
    public void testIsViewAttachedNotEquals() {
        assertNotEquals(mPresenter.isViewAttached(), false);
    }

    @Test
    public void testIsLocationConfigurationEnabled() {
        mockConfigurationHelper.SHOW_CAPTURED_LOCATION = true;

        assertEquals(mPresenter.isLocationConfigurationEnabled(), true);
    }

    @Test
    public void testIsNFCConfigurationEnabled() {
        mockConfigurationHelper.SHOW_NFC_VALIDATION_FOR_RETAILER = true;

        assertEquals(mPresenter.isNFCConfigurationEnabled(), true);
    }

    @Test
    public void testOnResume() {
        mockConfigurationHelper.SHOW_NFC_VALIDATION_FOR_RETAILER = true;

        mPresenter.onResume();

        then(ivyView).should().resumeNFCManager();
    }

    @Test
    public void testOnResumeNFCFalse() {
        mockConfigurationHelper.SHOW_NFC_VALIDATION_FOR_RETAILER = false;

        mPresenter.onResume();

        then(ivyView).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testOnCreate() {
        given(mDataManager.getThemeColor()).willReturn(Single.just("green"));
        given(mDataManager.getFontSize()).willReturn(Single.just("Small"));
        given(mDataManager.getPreferredLanguage()).willReturn("ar");

        mPresenter.onCreate();
        testScheduler.triggerActions();

        then(ivyView).should().setGreenTheme();
        then(ivyView).should().setFontSize("Small");
        then(ivyView).should().handleLayoutDirection("ar");

    }

    @Test
    public void testOnPause() {
        mockConfigurationHelper.SHOW_NFC_VALIDATION_FOR_RETAILER = true;

        mPresenter.onPause();

        then(ivyView).should().pauseNFCManager();
    }

    @Test
    public void testOnPauseNFCFalse() {
        mockConfigurationHelper.SHOW_NFC_VALIDATION_FOR_RETAILER = false;

        mPresenter.onPause();

        then(ivyView).shouldHaveNoMoreInteractions();
    }

    @After
    public void tearDown() throws Exception {
        mPresenter.onDetach();
    }

}
