package com.ivy.ui.activation.presenter;

import com.ivy.TestDataFactory;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.sd.png.bo.ActivationBO;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.ui.activation.ActivationContract;
import com.ivy.ui.activation.data.ActivationDataManager;
import com.ivy.ui.activation.data.ActivationError;
import com.ivy.utils.rx.TestSchedulerProvider;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import io.reactivex.Observable;
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

    @Mock
    private ConfigurationMasterHelper mockConfigurationHelper;

    @Before
    public void setup() {

        TestSchedulerProvider testSchedulerProvider = new TestSchedulerProvider(testScheduler);
        mPresenter = new ActivationPresenterImpl<>(mDataManager, testSchedulerProvider, mockDisposable, mockConfigurationHelper, mActivationDataManager);
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
    public void testValidActivation() {
        mPresenter.validateActivationKey("1234567891234567");
        then(mActivationView).should().doValidationSuccess();
    }


    @Test
    public void testDoActivation() {
        JSONObject jsonObject = TestDataFactory.getValidActivationObject();

        given(mActivationDataManager.doActivationAtHttp("abcd", "abcd",
                "abcd", "abcd")).willReturn(Observable.just(jsonObject));

        given(mActivationDataManager.isServerOnline("https://test2.ivymobileapps.com/Idist_my_png_msync/MobileWebService.asmx"))
                .willReturn(Single.just(true));

        mPresenter.doActivation("abcd", "abcd",
                "abcd", "abcd");
        testScheduler.triggerActions();

        then(mActivationView).should().hideLoading();
        then(mActivationView).should().showSuccessfullyActivatedAlert();
    }

    @Test
    public void testDoActivationEmptyUrl() {

        JSONObject jsonObject = TestDataFactory.getValidActivationFailureObject();

        given(mActivationDataManager.doActivationAtHttp("abcd", "abcd",
                "abcd", "abcd")).willReturn(Observable.just(jsonObject));

        mPresenter.doActivation("abcd", "abcd",
                "abcd", "abcd");
        testScheduler.triggerActions();

        then(mActivationView).should().hideLoading();
        then(mActivationView).should().showAppUrlIsEmptyError();

    }

    @Test
    public void testDoActivationFailure() {
        ActivationError activationError = new ActivationError(2001, "");
        given(mActivationDataManager.doActivationAtHttp("", "",
                "abcd", "abcd")).willReturn(Observable.error(activationError));

        mPresenter.doActivation("", "",
                "abcd", "abcd");
        testScheduler.triggerActions();

        then(mActivationView).should().hideLoading();

    }


   /* @Test(expected = UnsupportedOperationException.class)
    public void testTriggerImeiActivation() {
        //When
       // mPresenter.triggerIMEIActivation("abcd", "abcd", "acbd");

       // then(mActivationView).shouldHaveZeroInteractions();
    }*/

    @Test
    public void testTriggerImEiActivation() {

        JSONObject jsonObject = TestDataFactory.getValidImeiResponse();

        given(mActivationDataManager.doIMEIActivationAtHttp("abcd", "abcd",
                "abcd")).willReturn(Observable.just(jsonObject));
        //When
        mPresenter.triggerIMEIActivation("abcd", "abcd", "abcd");

        testScheduler.triggerActions();
        then(mActivationView).should().hideLoading();
        then(mActivationView).should().showActivationDialog();

    }

    @Test
    public void testTriggerActivationWithJsonException() {

        JSONObject jsonObject = TestDataFactory.getInValidResponse();

        given(mActivationDataManager.doActivationAtHttp("abcd", "abcd",
                "abcd", "abcd")).willReturn(Observable.just(jsonObject));
        //When
        mPresenter.doActivation("abcd", "abcd",
                "abcd", "abcd");
        testScheduler.triggerActions();
        then(mActivationView).should().hideLoading();
        then(mActivationView).should().showJsonExceptionError();

    }

    @Test
    public void testTriggerImEiActivationSingleResponse() {

        JSONObject jsonObject = TestDataFactory.getValidateSingleImEiResponse();

        given(mActivationDataManager.doIMEIActivationAtHttp("abcd", "abcd",
                "abcd")).willReturn(Observable.just(jsonObject));


        given(mDataManager.getBaseUrl()).willReturn("https://test2.ivymobileapps.com/Idist_my_png_msync/MobileWebService.asmx");


        given(mActivationDataManager.isServerOnline("https://test2.ivymobileapps.com/Idist_my_png_msync/MobileWebService.asmx"))
                .willReturn(Single.just(true));

        //When
        mPresenter.triggerIMEIActivation("abcd", "abcd", "abcd");

        testScheduler.triggerActions();
        then(mActivationView).should().hideLoading();
        then(mActivationView).should().navigateToLoginScreen();


    }

    @Test
    public void testTriggerImEiActivationError() {

        ActivationError activationError = new ActivationError(2001, "");

        given(mActivationDataManager.doIMEIActivationAtHttp
                ("", "", "")).willReturn(Observable.error(activationError));
        mPresenter.triggerIMEIActivation("", "", "");
        testScheduler.triggerActions();
        then(mActivationView).should().showActivationFailedError();

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

    @Test
    public void testInvalidActivationKeyError() {

        ActivationError myError = new ActivationError(DataMembers.IVY_CODE_CUSTOM, "6");

        given(mActivationDataManager.doActivationAtHttp("abcd", "12345", "10", "12345"))
                .willReturn(Observable.error(myError));

        mPresenter.doActivation("abcd", "12345", "10", "12345");
        testScheduler.triggerActions();
        then(mActivationView).should().showActivationFailedError();
    }


    @Test
    public void testHandleError() {
        ActivationError activationError = new ActivationError(2002, "5");
        mPresenter.handleError(activationError);
        then(mActivationView).should().showTryValidKeyError();
    }

    @Test
    public void testShowValidError() {
        mPresenter.showValidError(15);
        testScheduler.triggerActions();
        then(mActivationView).should().showToastValidKeyContactAdmin();

    }

    @Test
    public void testShowValidErrorInValid() {
        mPresenter.showValidError(5);
        testScheduler.triggerActions();
        then(mActivationView).should().showTryValidKeyError();

    }

    @Test
    public void testShowValidErrorActivationFailed() {
        mPresenter.showValidError(6);
        testScheduler.triggerActions();
        then(mActivationView).should().showActivationFailedError();

    }

    @Test
    public void testActivationDismiss() {
        given(mDataManager.getBaseUrl()).willReturn("");
        mPresenter.doActionForActivationDismiss();
        testScheduler.triggerActions();
        then(mActivationView).should().showAppUrlIsEmptyError();
    }

    @Test
    public void testActivationDismissValidUrl() {

        given(mDataManager.getBaseUrl()).willReturn("https://test2.ivymobileapps.com/Idist_my_png_msync/MobileWebService.asmx");

        given(mActivationDataManager.isServerOnline("https://test2.ivymobileapps.com/Idist_my_png_msync/MobileWebService.asmx"))
                .willReturn(Single.just(true));
        mPresenter.doActionForActivationDismiss();
        testScheduler.triggerActions();
        then(mActivationView).should().navigateToLoginScreen();
    }


    @Test
    public void testCheckServerStatusBasedOnActivationInValidUrl() {

        given(mActivationDataManager.isServerOnline("https://test2.ivymobileapps.com/Idist_my_png_msync/MobileWebService.asmx"))
                .willReturn(Single.just(false));
        mPresenter.checkServerStatusBasedOnActivation("https://test2.ivymobileapps.com/Idist_my_png_msync/MobileWebService.asmx");
        testScheduler.triggerActions();
        then(mActivationView).should().showToastAppUrlConfiguredMessage();
    }


   // showActivationDialog

    @Test
    public void  testShowActivationDialog(){
        List<ActivationBO> activationBOList = null;
        mPresenter.showActivationDialog(activationBOList);
        then(mActivationView).should().showPreviousActivationError();
    }


    @Test
    public void testHandleErrorValidCode() {
        ActivationError activationError = new ActivationError(2001, "");
        mPresenter.handleError(activationError);
        then(mActivationView).should().showActivationFailedError();

    }

    @Test
    public void testHandleErrorInValidCode() {
        ActivationError activationError = new ActivationError(0, "");
        mPresenter.handleError(activationError);
        then(mActivationView).should().showActivationError(activationError);

    }

    @After
    public void tearDown() {
        mPresenter.onDetach();
    }


}
