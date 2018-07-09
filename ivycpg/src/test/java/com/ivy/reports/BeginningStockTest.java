package com.ivy.reports;


import android.content.Context;

import com.ivy.core.data.datamanager.DataManager;
import com.ivy.cpg.view.reports.beginstockreport.BeginningReportContract;
import com.ivy.cpg.view.reports.beginstockreport.data.BeginningReportManager;
import com.ivy.cpg.view.reports.beginstockreport.presenter.BeginningReportPresenterImpl;
import com.ivy.sd.png.bo.StockReportMasterBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.LabelsMasterHelper;
import com.ivy.sd.png.provider.UserMasterHelper;
import com.ivy.utils.rx.TestSchedulerProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Vector;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.TestScheduler;

import static junit.framework.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@RunWith(MockitoJUnitRunner.class)
public class BeginningStockTest {

    @Mock
    private BeginningReportContract.IBeginningStockView beginningStockView;

    @Mock
    private BeginningReportPresenterImpl<BeginningReportContract.IBeginningStockView> mPresenter;


    private TestScheduler testScheduler = new TestScheduler();

    @Mock
    private DataManager mDataManager;

    private CompositeDisposable mockDisposable = new CompositeDisposable();

    @Mock
    private ConfigurationMasterHelper mockConfigurationHelper;

    @Mock
    private BeginningReportManager beginningReportManager;

    @Mock
    private LabelsMasterHelper labelsMasterHelper;

    @Mock
    private Context context;

    @Mock
    private StockReportMasterBO stockReportMasterBO;

    @Mock
    private UserMasterHelper userMasterHelper;
    @Mock
    private UserMasterBO userMasterBO;


    @Before
    public void setup() {
        TestSchedulerProvider testSchedulerProvider = new TestSchedulerProvider(testScheduler);
        mPresenter = new BeginningReportPresenterImpl<>(mDataManager, testSchedulerProvider, mockDisposable,
                mockConfigurationHelper, beginningReportManager, beginningStockView);
    }

    @Test
    public void downloadBeginningStockTest() {
        Vector<StockReportMasterBO> stockReportMasterBOS = new Vector<>();
        stockReportMasterBOS.add(stockReportMasterBO);
        given(beginningReportManager.downloadBeginningStock(context)).willReturn(Observable.just(stockReportMasterBOS));
        mPresenter.downloadBeginningStock(context);
        testScheduler.triggerActions();
        beginningStockView.hideLoading();
    }

    @Test
    public void showCaseOrderTest() {
        Object o = "Case";
        mockConfigurationHelper.SHOW_ORDER_CASE = true;
        given(labelsMasterHelper.applyLabels(o)).willReturn("Case");
        mPresenter.setLabelsMasterHelper(labelsMasterHelper);
        mPresenter.showCaseOrder(o);
        testScheduler.triggerActions();
        then(beginningStockView).should().setCaseTitle(o.toString());
    }

    @Test
    public void showCaseOrderTestFailure() {

        Object o = "Case";
        mockConfigurationHelper.SHOW_ORDER_CASE = false;
        mPresenter.showCaseOrder(o);
        testScheduler.triggerActions();
        then(beginningStockView).should().hideCaseTitle();
    }


    @Test
    public void showPieceOrderTest() {
        Object o = "PS";
        mockConfigurationHelper.SHOW_ORDER_PCS = true;
        given(labelsMasterHelper.applyLabels(o)).willReturn("PS");
        mPresenter.setLabelsMasterHelper(labelsMasterHelper);
        mPresenter.showPieceOrder(o);
        testScheduler.triggerActions();
        then(beginningStockView).should().setPieceTitle(o.toString());
    }

    @Test
    public void showPieceOrderTestFailure() {

        Object o = "PS";
        mockConfigurationHelper.SHOW_ORDER_PCS = false;
        mPresenter.showPieceOrder(o);
        testScheduler.triggerActions();
        then(beginningStockView).should().hidePieceTitle();
    }

    @Test
    public void ifShowCaseTitle() {
        boolean actual = mPresenter.ifShowCase();
        assertEquals(false, actual);
    }

    @Test
    public void checkValidUser() {
        userMasterBO.setUserid(10);
        given(userMasterHelper.getUserMasterBO()).willReturn(userMasterBO);
        given(userMasterHelper.getUserMasterBO().getUserid()).willReturn(0);
        mPresenter.setUserMasterHelper(userMasterHelper);
        mPresenter.checkUserId();
        testScheduler.triggerActions();
        then(beginningStockView).should().finishActivity();
    }
}
