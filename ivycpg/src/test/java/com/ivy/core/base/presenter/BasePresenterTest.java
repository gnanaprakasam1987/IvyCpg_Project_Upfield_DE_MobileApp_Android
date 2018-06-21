package com.ivy.core.base.presenter;

import com.ivy.core.base.view.BaseIvyView;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.ui.activation.presenter.ActivationPresenterImpl;
import com.ivy.utils.rx.TestSchedulerProvider;

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
    BaseIvyView mActivationView;

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
        mPresenter.onAttach(mActivationView);
    }

    @Test
    public void testBlueTheme() {
        given(mDataManager.getThemeColor()).willReturn(Single.just("blue"));

        //When
        mPresenter.getAppTheme();
        testScheduler.triggerActions();

        then(mActivationView).should().setBlueTheme();
    }

    @Test
    public void testPinkTheme() {
        given(mDataManager.getThemeColor()).willReturn(Single.just("pink"));

        //When
        mPresenter.getAppTheme();
        testScheduler.triggerActions();

        then(mActivationView).should().setPinkTheme();
    }

    @Test
    public void testOrangeTheme() {
        given(mDataManager.getThemeColor()).willReturn(Single.just("orange"));

        //When
        mPresenter.getAppTheme();
        testScheduler.triggerActions();

        then(mActivationView).should().setOrangeTheme();
    }

    @Test
    public void testRedTheme() {
        given(mDataManager.getThemeColor()).willReturn(Single.just("red"));

        //When
        mPresenter.getAppTheme();
        testScheduler.triggerActions();

        then(mActivationView).should().setRedTheme();
    }


    @Test
    public void testNavyBlueTheme() {
        given(mDataManager.getThemeColor()).willReturn(Single.just("nblue"));

        //When
        mPresenter.getAppTheme();
        testScheduler.triggerActions();

        then(mActivationView).should().setNavyBlueTheme();
    }


    @Test
    public void testGreenTheme() {
        given(mDataManager.getThemeColor()).willReturn(Single.just("green"));

        //When
        mPresenter.getAppTheme();
        testScheduler.triggerActions();

        then(mActivationView).should().setGreenTheme();
    }

}
