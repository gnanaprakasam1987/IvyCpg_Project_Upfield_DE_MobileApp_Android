package com.ivy.ui.activation.presenter;

import com.ivy.core.data.datamanager.DataManager;
import com.ivy.ui.activation.ActivationContract;
import com.ivy.utils.rx.TestSchedulerProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.TestScheduler;

@RunWith(MockitoJUnitRunner.class)
public class ActivationPresenterTest {


    @Mock
    private
    ActivationContract.ActivationView mActivationView;

    private ActivationPresenterImpl mPresenter;

    private TestScheduler testScheduler= new TestScheduler();

    @Mock
    private
    DataManager mDataManager;

    private CompositeDisposable mockDisposable = new CompositeDisposable();

    @Before
    public void setup() {

        TestSchedulerProvider testSchedulerProvider = new TestSchedulerProvider(testScheduler);
        mPresenter = new ActivationPresenterImpl(mDataManager, testSchedulerProvider, mockDisposable);
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

}
