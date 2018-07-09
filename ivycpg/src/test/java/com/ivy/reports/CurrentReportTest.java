package com.ivy.reports;


import android.content.Context;

import com.ivy.core.data.datamanager.DataManager;
import com.ivy.ui.reports.currentreport.ICurrentReportContract;
import com.ivy.ui.reports.currentreport.data.CurrentReportManager;
import com.ivy.ui.reports.currentreport.presenter.CurrentReportPresenterImpl;
import com.ivy.sd.png.bo.StockReportBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
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

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@RunWith(MockitoJUnitRunner.class)
public class CurrentReportTest {

    @Mock
    private
    ICurrentReportContract.ICurrentReportView mCurrentReportView;

    private CurrentReportPresenterImpl<ICurrentReportContract.ICurrentReportView> mPresenter;

    private TestScheduler testScheduler = new TestScheduler();

    @Mock
    private StockReportBO stockReportBO;

    @Mock
    private Context context;

    @Mock
    private BusinessModel businessModel;

    @Mock
    private
    DataManager mDataManager;

    private CompositeDisposable mockDisposable = new CompositeDisposable();

    @Mock
    private ConfigurationMasterHelper mockConfigurationHelper;

    @Mock
    private CurrentReportManager currentReportManager;

    @Mock
    private UserMasterHelper userMasterHelper;
    @Mock
    private UserMasterBO userMasterBO;

    @Before
    public void setup() {

        TestSchedulerProvider testSchedulerProvider = new TestSchedulerProvider(testScheduler);
        mPresenter = new CurrentReportPresenterImpl<>(mDataManager, testSchedulerProvider, mockDisposable,
                mockConfigurationHelper, currentReportManager, mCurrentReportView);
    }

    @Test
    public void updateStockReportGridTest() {
        Vector<StockReportBO> stockReportBOS = new Vector<>();
        stockReportBOS.add(stockReportBO);
        mPresenter.updateStockReportGrid(0, stockReportBOS);

    }

    @Test
    public void checkValidUser() {
        userMasterBO.setUserid(10);
        given(userMasterHelper.getUserMasterBO()).willReturn(userMasterBO);
        given(userMasterHelper.getUserMasterBO().getUserid()).willReturn(0);
        mPresenter.setUserMasterHelper(userMasterHelper);
        mPresenter.checkUserId();
        testScheduler.triggerActions();
        then(mCurrentReportView).should().finishActivity();
    }

    @Test
    public void setUpTitlesTest() {
        mockConfigurationHelper.IS_EOD_STOCK_SPLIT = true;
        mPresenter.setUpTitles();
        testScheduler.triggerActions();
        then(mCurrentReportView).should().setUpViewsVisible();

    }

    @Test
    public void setUpTitlesTestFailure() {
        mockConfigurationHelper.IS_EOD_STOCK_SPLIT = false;
        mPresenter.setUpTitles();
        testScheduler.triggerActions();
        then(mCurrentReportView).should().hideTitleViews();

    }

    @Test
    public void downloadCurrentStockReportTest() {
        Vector<StockReportBO> stockReportBOS = new Vector<>();
        given(currentReportManager.downloadCurrentStockReport(context, businessModel))
                .willReturn(Observable.just(stockReportBOS));

        mPresenter.downloadCurrentStockReport(context, businessModel);
        testScheduler.triggerActions();
        then(mCurrentReportView).should().setStockReportBOSList(stockReportBOS);
    }
}
