package com.ivy.ui.task.di;


import com.ivy.core.di.component.IvyAppComponent;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.task.view.TaskCreationActivity;
import com.ivy.ui.task.view.TaskDetailActivity;
import com.ivy.ui.task.view.TaskFragment;

import dagger.Component;

@PerActivity
@Component(dependencies = IvyAppComponent.class, modules = {TaskModule.class})
public interface TaskComponent {

    void inject(TaskFragment taskFragment);

    void inject(TaskCreationActivity taskCreationActivity);

    void inject(TaskDetailActivity taskDetailActivity);



}
