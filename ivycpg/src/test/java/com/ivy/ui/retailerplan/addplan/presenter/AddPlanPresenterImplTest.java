package com.ivy.ui.retailerplan.addplan.presenter;


import com.ivy.core.data.datamanager.DataManager;
import com.ivy.core.data.retailer.RetailerDataManager;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.ui.retailerplan.addplan.AddPlanContract;
import com.ivy.ui.retailerplan.addplan.DateWisePlanBo;
import com.ivy.ui.retailerplan.addplan.data.AddPlanDataManager;
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
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class AddPlanPresenterImplTest {

    @Mock
    private AddPlanContract.AddPlanView mView;

    private CompositeDisposable mockDisposable = new CompositeDisposable();

    private TestScheduler testScheduler = new TestScheduler();

    @Mock
    private
    DataManager mDataManager;

    @Mock
    private ConfigurationMasterHelper mockConfigurationHelper;

    @Mock
    private AddPlanDataManager addPlanDataManager;

    @Mock
    private RetailerDataManager retailerDataManager;

    private AddPlanPresenterImpl<AddPlanContract.AddPlanView> mPresenter;


    @Before
    public void setup() {
        TestSchedulerProvider testSchedulerProvider = new TestSchedulerProvider(testScheduler);
        mPresenter = new AddPlanPresenterImpl<>(mDataManager, testSchedulerProvider, mockDisposable,
                mockConfigurationHelper, mView,  addPlanDataManager,retailerDataManager);
    }

    /*@Test
    public void testSavePlanSuccess(){

        given(mDataManager.getUser()).willReturn(mock(UserMasterBO.class));

        DateWisePlanBo dateWisePlanBo = new DateWisePlanBo();

        given(addPlanDataManager.savePlan(dateWisePlanBo)).willReturn(Single.just(true));

        mPresenter.addNewPlan("","","", new RetailerMasterBO());
        testScheduler.triggerActions();

        then(mView).should().showUpdatedSuccessfullyMessage();

    }

    @Test
    public void testSavePlanFailed(){

        given(mDataManager.getUser()).willReturn(mock(UserMasterBO.class));

        DateWisePlanBo dateWisePlanBo = new DateWisePlanBo();

        given(addPlanDataManager.savePlan(dateWisePlanBo)).willReturn(Single.just(false));

        mPresenter.addNewPlan("","","", new RetailerMasterBO());
        testScheduler.triggerActions();

        then(mView).should().showUpdateFailureMessage();

    }*/


}
