package com.ivy.ui.attendance.presenter;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.cpg.view.nonfield.NonFieldTwoBo;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.ui.attendance.TimeTrackingContract;
import com.ivy.ui.attendance.data.TimeTrackDataManager;
import com.ivy.utils.rx.SchedulerProvider;

import java.util.ArrayList;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

/**
 * Created by mansoor on 27/12/2018
 */
public class TimeTrackPresenterImpl<V extends TimeTrackingContract.TimeTrackingView> extends BasePresenter<V> implements TimeTrackingContract.TimeTrackingPresenter<V>, LifecycleObserver {
    private TimeTrackDataManager timeTrackDataManager;
    @Inject
    public TimeTrackPresenterImpl(DataManager dataManager,
                                  SchedulerProvider schedulerProvider,
                                  CompositeDisposable compositeDisposable,
                                  ConfigurationMasterHelper configurationMasterHelper,
                                  V view,
                                  TimeTrackDataManager timeTrackDataManager) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, view);
        this.timeTrackDataManager = timeTrackDataManager;
        if (view instanceof LifecycleOwner) {
            ((LifecycleOwner) view).getLifecycle().addObserver(this);
        }
    }

    @Override
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void fetchData() {
        getIvyView().showLoading();

        getCompositeDisposable().add(timeTrackDataManager.getTimeTrackList()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui()).subscribe(new Consumer<ArrayList<NonFieldTwoBo>>() {
            @Override
            public void accept(ArrayList<NonFieldTwoBo> value) {
                getIvyView().populateDataToList(value);
                getIvyView().hideLoading();
            }
        }));

    }
}
