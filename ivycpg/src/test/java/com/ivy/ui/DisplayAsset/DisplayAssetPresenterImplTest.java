package com.ivy.ui.DisplayAsset;

import com.ivy.cpg.view.DisplayAsset.DisplayAssetContractor;
import com.ivy.cpg.view.DisplayAsset.DisplayAssetHelper;
import com.ivy.cpg.view.DisplayAsset.DisplayAssetPresenterImpl;
import com.ivy.cpg.view.asset.bo.AssetTrackingBO;
import com.ivy.utils.rx.TestSchedulerProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import io.reactivex.Observable;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class DisplayAssetPresenterImplTest {

    @Mock
    private DisplayAssetPresenterImpl presenter;

    @Mock
    private DisplayAssetHelper displayAssetHelper;

    @Mock
    private DisplayAssetContractor.View view;

    @Before
    public void setup() {

        //TestSchedulerProvider testSchedulerProvider = new TestSchedulerProvider(testScheduler);
        presenter = new DisplayAssetPresenterImpl(displayAssetHelper);
    }

    @Test
    public void testRefreshStatus(){

        given(displayAssetHelper.getDisplayAssetList()).willReturn(DisplayAssetTestDataFactory.getDisplayAssetList());
        presenter.setView(view);

        presenter.refreshStatus();

        assertEquals("DISADVANTAGE",presenter.getDisplayAssetStatus());

        //assertEquals(100,presenter.getOtherCompanyMaxScore(),2);
    }
}
