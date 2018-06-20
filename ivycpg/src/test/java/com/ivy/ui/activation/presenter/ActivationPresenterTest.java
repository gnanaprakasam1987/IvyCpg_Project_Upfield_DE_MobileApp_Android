package com.ivy.ui.activation.presenter;

import com.ivy.core.data.datamanager.DataManager;
import com.ivy.ui.activation.ActivationContract;
import com.ivy.ui.activation.data.ActivationDataManager;
import com.ivy.utils.rx.TestSchedulerProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.TestScheduler;

import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.class)
public class ActivationPresenterTest {


    @Mock
    private
    ActivationContract.ActivationView mActivationView;

    private ActivationPresenterImpl<ActivationContract.ActivationView> mPresenter;

    private TestScheduler testScheduler= new TestScheduler();

    @Mock
    private
    DataManager mDataManager;

    @Mock
    private ActivationDataManager mActivationDataManager;

    private CompositeDisposable mockDisposable = new CompositeDisposable();

    @Before
    public void setup() {

        TestSchedulerProvider testSchedulerProvider = new TestSchedulerProvider(testScheduler);
        mPresenter = new ActivationPresenterImpl<>(mDataManager, testSchedulerProvider, mockDisposable,mActivationDataManager);
        mPresenter.onAttach(mActivationView);
    }

    @Test
    public void testEmptyActivationKey(){
        mPresenter.validateActivationKey("");
        Mockito.verify(mActivationView).showActivationEmptyError();
    }

    @Test
    public void testInvalidActivationKey(){
        mPresenter.validateActivationKey("abcdef");
        Mockito.verify(mActivationView).showInvalidActivationError();
    }

    @Test
    public void testValidUrlSuccess(){
        doReturn(Single.just(true))
                .when(mActivationDataManager)
                .isServerOnline("www.google.com");

        mPresenter.checkServerStatus("www.google.com");

        testScheduler.triggerActions();

        Mockito.verify(mActivationView).navigateToLoginScreen();
    }

    @Test
    public void testValidUrlFailure(){
        doReturn( Single.just(false))
                .when(mActivationDataManager)
                .isServerOnline("www.google.com");

        mPresenter.checkServerStatus("www.google.com");

        testScheduler.triggerActions();

        Mockito.verify(mActivationView).showInvalidUrlError();
    }

    @After
    public void tearDown() throws Exception {
        mPresenter.onDetach();
    }

}
