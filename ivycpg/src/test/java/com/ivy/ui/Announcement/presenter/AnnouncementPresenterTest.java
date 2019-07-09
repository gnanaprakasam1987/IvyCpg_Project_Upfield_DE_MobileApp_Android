package com.ivy.ui.Announcement.presenter;

import com.ivy.core.data.channel.ChannelDataManager;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.ui.Announcement.AnnouncementTestDataFactory;
import com.ivy.ui.announcement.AnnouncementContract;
import com.ivy.ui.announcement.data.AnnouncementDataManager;
import com.ivy.ui.announcement.model.AnnouncementBo;
import com.ivy.ui.announcement.presenter.AnnouncementPresenterImpl;
import com.ivy.utils.rx.TestSchedulerProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.TestScheduler;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

@RunWith(MockitoJUnitRunner.class)
public class AnnouncementPresenterTest {

    private AnnouncementContract.AnnouncementView announcementView;

    @Mock
    private
    DataManager mDataManager;

    private CompositeDisposable mockDisposable = new CompositeDisposable();

    @Mock
    private ConfigurationMasterHelper mockConfigurationHelper;

    private TestScheduler testScheduler = new TestScheduler();

    @Mock
    private AnnouncementDataManager mockAnnouncementDataManager;

    @Mock
    private ChannelDataManager mockChannelDataManager;

    private AnnouncementPresenterImpl<AnnouncementContract.AnnouncementView> announcementPresenter;


    @Before
    public void setUp() {
        TestSchedulerProvider testSchedulerProvider = new TestSchedulerProvider(testScheduler);
        announcementView = mock(AnnouncementContract.AnnouncementView.class, withSettings());
        announcementPresenter = new AnnouncementPresenterImpl<>(mDataManager, testSchedulerProvider,
                mockDisposable, mockConfigurationHelper,
                announcementView, mockAnnouncementDataManager, mockChannelDataManager);

    }

    @Test
    public void fetchSellerAnnouncementData() {
        ArrayList<AnnouncementBo> mockBoArrayList = AnnouncementTestDataFactory.getMockList();

        given(mockAnnouncementDataManager.fetchAnnouncementData(true))
                .willReturn(Observable.fromCallable(new Callable<ArrayList<AnnouncementBo>>() {
                    @Override
                    public ArrayList<AnnouncementBo> call() throws Exception {
                        return mockBoArrayList;
                    }
                }));

        announcementPresenter.fetchData(true);
        testScheduler.triggerActions();
        announcementView.showLoading();
        announcementView.updateListData(mockBoArrayList);
        announcementView.hideLoading();
    }

    @Test
    public void fetchRetailerAnnouncementData() {
        ArrayList<AnnouncementBo> getRetMockList = AnnouncementTestDataFactory.getMockList();

        given(mockAnnouncementDataManager.fetchAnnouncementData(false))
                .willReturn(Observable.fromCallable(new Callable<ArrayList<AnnouncementBo>>() {
                    @Override
                    public ArrayList<AnnouncementBo> call() throws Exception {
                        return getRetMockList;
                    }
                }));

        announcementPresenter.fetchData(false);
        testScheduler.triggerActions();
        announcementView.showLoading();
        announcementView.updateListData(getRetMockList);
        announcementView.hideLoading();
    }

    @Test
    public void fetchAnnouncementDataFailed() {
        ArrayList<AnnouncementBo> getRetMockList = new ArrayList<>();

        given(mockAnnouncementDataManager.fetchAnnouncementData(false))
                .willReturn(Observable.fromCallable(new Callable<ArrayList<AnnouncementBo>>() {
                    @Override
                    public ArrayList<AnnouncementBo> call() throws Exception {
                        return new ArrayList<>();
                    }
                }));

        announcementPresenter.fetchData(false);
        testScheduler.triggerActions();
        announcementView.showLoading();
        announcementView.hideLoading();
        announcementView.onDataNotMappedMsg();
    }
}
