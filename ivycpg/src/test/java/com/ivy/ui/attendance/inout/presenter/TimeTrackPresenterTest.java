package com.ivy.ui.attendance.inout.presenter;

import com.ivy.core.data.datamanager.DataManager;
import com.ivy.cpg.view.nonfield.NonFieldTwoBo;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.ui.attendance.inout.TimeTrackTestDataFactory;
import com.ivy.ui.attendance.inout.TimeTrackingContract;
import com.ivy.ui.attendance.inout.data.TimeTrackDataManager;
import com.ivy.utils.rx.TestSchedulerProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.TestScheduler;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TimeTrackPresenterTest {

    @Mock
    private TimeTrackingContract.TimeTrackingView mView;

    @Mock
    private
    DataManager mDataManager;

    private CompositeDisposable mockDisposable = new CompositeDisposable();

    @Mock
    private ConfigurationMasterHelper mockConfigurationHelper;

    private TestScheduler testScheduler = new TestScheduler();

    @Mock
    private TimeTrackDataManager timeTrackDataManager;


    private TimeTrackPresenterImpl<TimeTrackingContract.TimeTrackingView> mPresenter;

    @Before
    public void setup() {

        TestSchedulerProvider testSchedulerProvider = new TestSchedulerProvider(testScheduler);
        mPresenter = new TimeTrackPresenterImpl<>(mDataManager, testSchedulerProvider, mockDisposable,
                mockConfigurationHelper, mView, timeTrackDataManager);
    }

    @Test
    public void testFetchData() {

        ArrayList<NonFieldTwoBo> timeTrackList = TimeTrackTestDataFactory.getTimeTrackList();

        given(timeTrackDataManager.getTimeTrackList()).willReturn(Observable.fromCallable(new Callable<ArrayList<NonFieldTwoBo>>() {
            @Override
            public ArrayList<NonFieldTwoBo> call() {
                return timeTrackList;
            }
        }));
        mPresenter.fetchData(false);
        testScheduler.triggerActions();
        then(mView).should().showLoading();
        then(mView).should().populateDataToList(timeTrackList);
        then(mView).should().hideLoading();
    }

    @Test
    public void testStopServiceLocation() {
        boolean isWorkingStatus = true;
        mockConfigurationHelper.IS_REALTIME_LOCATION_CAPTURE = true;
        mockConfigurationHelper.IS_UPLOAD_ATTENDANCE = true;
        given(timeTrackDataManager.isWorkingStatus(1)).willReturn(Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                return isWorkingStatus;
            }
        }));

        mPresenter.stopLocationService("1");
        testScheduler.triggerActions();
        then(mView).should().showLoading();
        then(mView).should().updateRealTimeOut();
        then(mView).should().uploadAttendance("OUT");
        then(mView).should().hideLoading();
    }

    @Test
    public void testupdateTimeTrackDetails() {

        ArrayList<NonFieldTwoBo> timeTrackList = TimeTrackTestDataFactory.getTimeTrackList();

        NonFieldTwoBo nonFieldTwoBo = timeTrackList.get(0);

        given(timeTrackDataManager.updateTimeTrackDetailsDb(nonFieldTwoBo)).willReturn(Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                return true;
            }
        }));

        given(timeTrackDataManager.getTimeTrackList()).willReturn(Observable.fromCallable(new Callable<ArrayList<NonFieldTwoBo>>() {
            @Override
            public ArrayList<NonFieldTwoBo> call() {
                return timeTrackList;
            }
        }));

        mPresenter.updateTimeTrackDetails(nonFieldTwoBo);
        testScheduler.triggerActions();
        then(mView).should().showLoading();
        then(mView).should().populateDataToList(timeTrackList);
        then(mView).should().hideLoading();

    }


    @Test
    public void testisRealTimeLocationOn() {
        mockConfigurationHelper.IS_REALTIME_LOCATION_CAPTURE = true;
        assertTrue(mPresenter.isRealTimeLocationOn());
    }

    @Test
    public void testisRealTimeLocationOnFail() {
        mockConfigurationHelper.IS_REALTIME_LOCATION_CAPTURE = false;
        assertFalse(mPresenter.isRealTimeLocationOn());
    }

    @Test
    public void testisShowCapturedLocation() {
        mockConfigurationHelper.SHOW_CAPTURED_LOCATION = true;
        assertTrue(mPresenter.isShowCapturedLocation());
    }

    @Test
    public void testisShowCapturedLocationFail() {
        mockConfigurationHelper.SHOW_CAPTURED_LOCATION = false;
        assertFalse(mPresenter.isShowCapturedLocation());
    }

    @Test
    public void testisAttendanceRemark() {
        mockConfigurationHelper.IS_ATTENDANCE_REMARK = true;
        assertTrue(mPresenter.isAttendanceRemark());
    }

    @Test
    public void testisAttendanceRemarkFail() {
        mockConfigurationHelper.IS_ATTENDANCE_REMARK = false;
        assertFalse(mPresenter.isAttendanceRemark());
    }

    @Test
    public void testIsPreviousInOutCompeleted() {
        assertTrue(mPresenter.isPreviousInOutCompleted(TimeTrackTestDataFactory.getTimeTrackList()));
    }

    @Test
    public void testIsPreviousInOutCompeletedFail() {
        assertFalse(mPresenter.isPreviousInOutCompleted(TimeTrackTestDataFactory.getTimeTrackListInTime()));
    }

    @Test
    public void testIsPreviousInOutCompeletedEmpty() {
        assertTrue(mPresenter.isPreviousInOutCompleted(new ArrayList<>()));
    }

    @Test
    public void testfetchInOutReason() {
        ArrayList<ReasonMaster> reasonMasterArrayList = TimeTrackTestDataFactory.getReasonList();
        given(timeTrackDataManager.getInOutReasonList()).willReturn(Observable.fromCallable(new Callable<ArrayList<ReasonMaster>>() {
            @Override
            public ArrayList<ReasonMaster> call() throws Exception {
                return reasonMasterArrayList;
            }
        }));

        mPresenter.fetchInOutReason();
        testScheduler.triggerActions();
        then(mView).should().showLoading();
        then(mView).should().showInOutDialog(reasonMasterArrayList);
        then(mView).should().hideLoading();
    }

    @Test
    public void testSaveInOutDetails() {

        ArrayList<NonFieldTwoBo> timeTrackList = TimeTrackTestDataFactory.getTimeTrackList();

        mockConfigurationHelper.IS_REALTIME_LOCATION_CAPTURE = true;
        mockConfigurationHelper.IS_UPLOAD_ATTENDANCE = true;
        mockConfigurationHelper.IS_IN_OUT_MANDATE = true;
        mockConfigurationHelper.IS_ATTENDANCE_SYNCUPLOAD = true;

        when(mView.isUpdateRealTimeIn()).thenReturn(true);

        given(timeTrackDataManager.isWorkingStatus(Integer.parseInt("0"))).willReturn(Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return true;
            }
        }));

        given(timeTrackDataManager.saveTimeTrackDetailsDb("0", "5", 0.0, 0.0)).willReturn(Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return true;
            }
        }));

        given(timeTrackDataManager.checkIsLeave()).willReturn(Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return true;
            }
        }));
        given(timeTrackDataManager.getTimeTrackList()).willReturn(Observable.fromCallable(new Callable<ArrayList<NonFieldTwoBo>>() {
            @Override
            public ArrayList<NonFieldTwoBo> call() {
                return timeTrackList;
            }
        }));

        mPresenter.saveInOutDetails("0", "5");
        testScheduler.triggerActions();
        InOrder inOrder = Mockito.inOrder(mView);
        then(mView).should(inOrder).showLoading();
        then(mView).should(inOrder).isUpdateRealTimeIn();
        then(mView).should(inOrder).uploadAttendance("IN");
        then(mView).should(inOrder).uploadAttendanceToServer();
        then(mView).should(inOrder).populateDataToList(timeTrackList);
        then(mView).should(inOrder).hideLoading();
        then(mView).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testSaveInOutDetailsSuccessFalse() {

        mockConfigurationHelper.IS_REALTIME_LOCATION_CAPTURE = true;
        mockConfigurationHelper.IS_UPLOAD_ATTENDANCE = true;
        mockConfigurationHelper.IS_IN_OUT_MANDATE = true;

        when(mView.isUpdateRealTimeIn()).thenReturn(true);

        given(timeTrackDataManager.isWorkingStatus(Integer.parseInt("0"))).willReturn(Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return true;
            }
        }));

        given(mView.isUpdateRealTimeIn()).willReturn(false);

        mPresenter.saveInOutDetails("0", "5");
        testScheduler.triggerActions();
        InOrder inOrder = Mockito.inOrder(mView);
        then(mView).should(inOrder).showLoading();
        then(mView).should(inOrder).isUpdateRealTimeIn();
        then(mView).should(inOrder).uploadAttendance("IN");
        then(mView).should(inOrder).hideLoading();
        then(mView).shouldHaveNoMoreInteractions();

    }


    @After
    public void tearDown() {
        mockConfigurationHelper.IS_REALTIME_LOCATION_CAPTURE = false;
        mockConfigurationHelper.IS_UPLOAD_ATTENDANCE = false;
        mockConfigurationHelper.SHOW_CAPTURED_LOCATION = false;
        mockConfigurationHelper.IS_ATTENDANCE_REMARK = false;
        mockConfigurationHelper.IS_IN_OUT_MANDATE = false;
        mPresenter.onDetach();
    }


}
