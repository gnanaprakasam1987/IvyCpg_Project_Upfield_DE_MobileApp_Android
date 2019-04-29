package com.ivy.ui.task.di;

import android.content.Context;

import com.ivy.core.di.scope.PerActivity;
import com.ivy.cpg.view.survey.SurveyHelperNew;
import com.ivy.ui.task.TaskContract;
import com.ivy.ui.task.data.TaskDataManager;
import com.ivy.ui.task.data.TaskDataManagerImpl;
import com.ivy.ui.task.presenter.TaskPresenterImpl;
import com.ivy.utils.rx.AppSchedulerProvider;
import com.ivy.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;
import io.reactivex.disposables.CompositeDisposable;

@Module
public class TaskModule {

    private TaskContract.TaskView mView;
    private Context mcContex;

    public TaskModule(TaskContract.TaskView mView, Context mcContex) {
        this.mView = mView;
        this.mcContex = mcContex;
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
    @PerActivity
    TaskContract.TaskPresenter<TaskContract.TaskView> providesTaskPresenter(TaskPresenterImpl<TaskContract.TaskView> taskPresenter) {
        return taskPresenter;
    }


}

