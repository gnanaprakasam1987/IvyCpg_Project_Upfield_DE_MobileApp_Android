package com.ivy.cpg.view.basedi;

import com.ivy.core.di.component.IvyAppComponent;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.cpg.view.reports.ReportActivity;
import com.ivy.ui.announcement.view.AnnouncementActivity;
import com.ivy.ui.notes.view.NotesActivity;
import com.ivy.ui.task.view.FilterFragment;
import com.ivy.ui.reports.dynamicreport.view.DynamicReportActivity;
import com.ivy.ui.reports.dynamicreport.view.DynamicReportTabFragment;
import com.ivy.ui.task.view.TaskActivity;

import dagger.Component;

@PerActivity
@Component(dependencies = IvyAppComponent.class, modules = {BaseModule.class})
public interface BaseComponent {
    void inject(ReportActivity reportActivity);

    void inject(TaskActivity taskActivity);

    void inject(NotesActivity notesActivity);

    void inject(FilterFragment filterFragment);

    void inject(DynamicReportTabFragment tabFragment);

    void inject(DynamicReportActivity dynamicReportActivity);

    void inject(AnnouncementActivity announcementActivity);
}
