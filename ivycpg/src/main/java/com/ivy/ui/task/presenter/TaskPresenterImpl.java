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
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.ui.task.TaskContract;
import com.ivy.ui.task.data.TaskDataManager;
import com.ivy.utils.rx.SchedulerProvider;

import java.util.ArrayList;
import java.util.Vector;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function3;
import io.reactivex.observers.DisposableObserver;

public class TaskPresenterImpl<V extends TaskContract.TaskView> extends BasePresenter<V> implements TaskContract.TaskPresenter<V>, LifecycleObserver {

    private UserDataManager mUserDataManager;
    private ChannelDataManager mChannelDataManager;
    private ConfigurationMasterHelper mConfigurationMasterHelper;
    private TaskDataManager mTaskDataManager;
    private ArrayList<UserMasterBO> mUserListBos = new ArrayList<>();
    private Vector<ChannelBO> mChannelListBos = new Vector<>();
    private ArrayList<RetailerMasterBO> mRetailerListBos = new ArrayList<>();

    public TaskPresenterImpl(DataManager dataManager,
                             SchedulerProvider schedulerProvider,
                             CompositeDisposable compositeDisposable,
                             ConfigurationMasterHelper configurationMasterHelper,
                             V view, UserDataManager userDataManager,
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

        getCompositeDisposable().add(Observable.zip(mUserDataManager.fetchAllUsers(),
                mChannelDataManager.fetchChannels(), mTaskDataManager.fetchRetailers()
                , new Function3<ArrayList<UserMasterBO>, Vector<ChannelBO>, ArrayList<RetailerMasterBO>, Object>() {
                    @Override
                    public Boolean apply(ArrayList<UserMasterBO> userMasterBOS, Vector<ChannelBO> channelBOS, ArrayList<RetailerMasterBO> retailerMasterBOS) throws Exception {
                        mUserListBos.clear();
                        mUserListBos.addAll(userMasterBOS);

                        mChannelListBos.clear();
                        mChannelListBos.addAll(channelBOS);

                        mRetailerListBos.clear();
                        mRetailerListBos.addAll(retailerMasterBOS);

                        return true;
                    }
                }).subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<Object>() {
                    @Override
                    public void onNext(Object o) {
                        if (mUserListBos.size() != 0)
                            getIvyView().setTaskUserListData(mUserListBos);

                        if (mChannelListBos.size() != 0)
                            getIvyView().setTaskChannelListData(mChannelListBos);

                        if (mRetailerListBos.size() != 0)
                            getIvyView().setTaskRetailerListData(mRetailerListBos);

                        getIvyView().hideLoading();
                    }

                    @Override
                    public void onError(Throwable e) {
                        getIvyView().onError("Something went wrong");
                        getIvyView().hideLoading();
                    }

                    @Override
                    public void onComplete() {

                    }
                }));
    }

    @Override
    public void updateTask(int taskType) {

    }


    @Override
    public void onSaveButtonClick(int channelId, String taskTitleDesc, String taskDetailDesc, String mode) {
        getIvyView().showLoading();
        getCompositeDisposable().add(mTaskDataManager.addNewTask(channelId
                , taskTitleDesc, taskDetailDesc, mode).subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean isAdded) throws Exception {
                        if (isAdded) {
                            getIvyView().showUpdatedDialog();
                        }
                        getIvyView().hideLoading();
                    }
                }));
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
