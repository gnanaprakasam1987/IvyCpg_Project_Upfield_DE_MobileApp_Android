package com.ivy.core.base.presenter;

import com.ivy.core.base.view.BaseIvyView;
import com.ivy.core.data.datamanager.DataManager;
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

    private BasePresenter<BaseIvyView> mPresenter;


    private CompositeDisposable mockDisposable = new CompositeDisposable();

    @Before
    public void setup() {
        TestSchedulerProvider testSchedulerProvider = new TestSchedulerProvider(testScheduler);
        mPresenter = new BasePresenter<>(mDataManager, testSchedulerProvider, mockDisposable);
        mPresenter.onAttach(ivyView);
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


    @After
    public void tearDown() throws Exception {
        mPresenter.onDetach();
    }

}
