package com.ivy.ui.announcement.presenter;

import android.arch.lifecycle.LifecycleObserver;

import com.ivy.core.IvyConstants;
import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.channel.ChannelDataManager;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.core.data.outlettime.OutletTimeStampDataManager;
import com.ivy.core.di.scope.ChannelInfo;
import com.ivy.core.di.scope.OutletTimeStampInfo;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.ui.announcement.AnnouncementContract;
import com.ivy.ui.announcement.data.AnnouncementDataManager;
import com.ivy.ui.announcement.model.AnnouncementBo;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.rx.SchedulerProvider;

import java.util.ArrayList;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

import static com.ivy.utils.DateTimeUtils.DATE_GLOBAL;

public class AnnouncementPresenterImpl<V extends AnnouncementContract.AnnouncementView> extends BasePresenter<V> implements AnnouncementContract.AnnouncementPresenter<V>, LifecycleObserver {

    private DataManager dataManager;
    private ConfigurationMasterHelper configurationMasterHelper;
    private AnnouncementDataManager announcementDataManager;
    private ChannelDataManager mChannelDataManager;

    @Inject
    public AnnouncementPresenterImpl(DataManager dataManager,
                                     SchedulerProvider schedulerProvider,
                                     CompositeDisposable compositeDisposable,
                                     ConfigurationMasterHelper configurationMasterHelper,
                                     V view, AnnouncementDataManager mannouncementDataManager,
                                     @ChannelInfo ChannelDataManager mChannelDataManager) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, view);
        this.dataManager = dataManager;
        this.configurationMasterHelper = configurationMasterHelper;
        this.announcementDataManager = mannouncementDataManager;
        this.mChannelDataManager = mChannelDataManager;

    }

    @Override
    public void fetchData(boolean isFromHomeSrc) {

        getIvyView().showLoading();
        getCompositeDisposable().add(announcementDataManager.fetchAnnouncementData(isFromHomeSrc)
                .observeOn(getSchedulerProvider().ui())
                .subscribeOn(getSchedulerProvider().io())
                .subscribe(new Consumer<ArrayList<AnnouncementBo>>() {
                    @Override
                    public void accept(ArrayList<AnnouncementBo> announcementBoArrayList) throws Exception {
                        if (!announcementBoArrayList.isEmpty()) {
                            getIvyView().updateListData(announcementBoArrayList);
                            getIvyView().hideLoading();
                        } else {
                            getIvyView().hideLoading();
                            getIvyView().onDataNotMappedMsg();
                        }
                    }
                }));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        dataManager.tearDown();
        announcementDataManager.tearDown();
    }
}
