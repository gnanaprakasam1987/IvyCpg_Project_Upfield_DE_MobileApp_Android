package com.ivy.ui.task.di;

import com.ivy.ui.task.TaskContract;
import com.ivy.ui.task.data.TaskDataManager;
import com.ivy.ui.task.data.TaskDataManagerImpl;
import com.ivy.ui.task.presenter.TaskPresenterImpl;
import com.ivy.utils.rx.AppSchedulerProvider;
import com.ivy.utils.rx.SchedulerProvider;

import dagger.Provides;
import io.reactivex.disposables.CompositeDisposable;

public class TaskModule {

    private TaskContract.TaskView mView;

    public TaskModule(TaskContract.TaskView mView) {
        this.mView = mView;
    }

    @Provides
    public TaskContract.TaskView provideView() {
        return mView;
    }

    @Provides
    CompositeDisposable provideCompositeDisposable() {
        return new CompositeDisposable();
    }

    @Provides
    SchedulerProvider provideSchedulerProvider() {
        return new AppSchedulerProvider();
    }

    @Provides
    TaskDataManager taskDataManager(TaskDataManagerImpl taskDataManager) {
        return taskDataManager;
    }

    @Provides
    TaskContract.TaskPresenter<TaskContract.TaskView> providesTaskPresenter(TaskPresenterImpl<TaskContract.TaskView> taskPresenter) {
        return taskPresenter;
    }


}

