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
import org.mockito.junit.MockitoJUnitRunner;

import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.TestScheduler;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@RunWith(MockitoJUnitRunner.class)
public class ActivationPresenterTest {


    @Mock
    private
    ActivationContract.ActivationView mActivationView;

    private ActivationPresenterImpl<ActivationContract.ActivationView> mPresenter;

    private TestScheduler testScheduler = new TestScheduler();

    @Mock
    private
    DataManager mDataManager;

    @Mock
    private ActivationDataManager mActivationDataManager;

    private CompositeDisposable mockDisposable = new CompositeDisposable();

    @Before
    public void setup() {

        TestSchedulerProvider testSchedulerProvider = new TestSchedulerProvider(testScheduler);
        mPresenter = new ActivationPresenterImpl<>(mDataManager, testSchedulerProvider, mockDisposable, mActivationDataManager);
        mPresenter.onAttach(mActivationView);
    }

    @Test
    public void testEmptyActivationKey() {
        mPresenter.validateActivationKey("");

        then(mActivationView).should().showActivationEmptyError();
    }

    @Test
    public void testInvalidActivationKey() {

        //When
        mPresenter.validateActivationKey("abcdef");

        then(mActivationView).should().showInvalidActivationError();
    }

    @Test
    public void testValidActivation(){
        mPresenter.validateActivationKey("1234567891234567");
        then(mActivationView).should().doValidationSuccess();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testTriggerImeiActivation() {
        //When
        mPresenter.triggerIMEIActivation("abcd", "abcd", "acbd");

        then(mActivationView).shouldHaveZeroInteractions();
    }

    @Test
    public void testValidUrlSuccess() {
        given(mActivationDataManager.isServerOnline("www.google.com")).willReturn(Single.just(true));

        //When
        mPresenter.checkServerStatus("www.google.com");
        testScheduler.triggerActions();

        then(mActivationView).should().navigateToLoginScreen();
    }

    @Test
    public void testValidUrlFailure() {
        given(mActivationDataManager.isServerOnline("www.google.com")).willReturn(Single.just(false));

        //When
        mPresenter.checkServerStatus("www.google.com");
        testScheduler.triggerActions();

        then(mActivationView).should().showInvalidUrlError();

    }

    @After
    public void tearDown() throws Exception {
        mPresenter.onDetach();
    }




}
