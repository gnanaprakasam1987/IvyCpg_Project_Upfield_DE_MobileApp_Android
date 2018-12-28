package com.ivy.ui.task.presenter;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.channel.ChannelDataManager;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.core.data.user.UserDataManager;
import com.ivy.sd.png.bo.ChannelBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.provider.ChannelMasterHelper;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.UserMasterHelper;
import com.ivy.ui.profile.edit.di.Profile;
import com.ivy.ui.task.TaskContract;
import com.ivy.ui.task.data.TaskDataManager;
import com.ivy.utils.rx.SchedulerProvider;

import java.util.ArrayList;

import io.reactivex.disposables.CompositeDisposable;

public class TaskPresenterImpl<V extends TaskContract.TaskView> extends BasePresenter<V> implements TaskContract.TaskPresenter<V>, LifecycleObserver {

    private UserDataManager mUserDataManager;
    private ChannelDataManager mChannelDataManager;
    private ConfigurationMasterHelper mConfigurationMasterHelper;
    private TaskDataManager mTaskDataManager;

    public TaskPresenterImpl(DataManager dataManager,
                             SchedulerProvider schedulerProvider,
                             CompositeDisposable compositeDisposable,
                             ConfigurationMasterHelper configurationMasterHelper,
                             V view,  UserDataManager userDataManager,
                              ChannelDataManager channelDataManager,
                             TaskDataManager taskDataManager) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, view);
        this.mConfigurationMasterHelper = configurationMasterHelper;
        this.mUserDataManager = userDataManager;
        this.mChannelDataManager = channelDataManager;
        this.mTaskDataManager = taskDataManager;

        if (view instanceof LifecycleOwner) {
            ((LifecycleOwner) view).getLifecycle().addObserver(this);
        }

    }

    @Override
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void fetchData() {
        getIvyView().showLoading();


    }

    @Override
    public void updateTask(int taskType) {

    }


    @Override
    public void saveButtonClick() {

    }

    @Override
    public String getSelectedRetailerId() {
        return null;
    }

    @Override
    public String getTaskMode() {
        return null;
    }

    @Override
    public ArrayList<ChannelBO> getTaskChannelList() {
        return null;
    }

    @Override
    public ArrayList<RetailerMasterBO> getTaskRetailerList() {
        return null;
    }

    @Override
    public ArrayList<UserMasterBO> getTaskUserList() {
        return null;
    }
}
