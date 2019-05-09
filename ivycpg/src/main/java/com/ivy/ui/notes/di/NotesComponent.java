package com.ivy.ui.notes.di;

import com.ivy.core.di.component.IvyAppComponent;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.notes.view.NoteDetailActivity;
import com.ivy.ui.notes.view.NotesCreationActivity;
import com.ivy.ui.notes.view.NotesListFragment;

import dagger.Component;

@PerActivity
@Component(dependencies = IvyAppComponent.class, modules = {NotesModule.class})
public interface NotesComponent {

    void inject(NotesListFragment notesListFragment);

    void inject(NotesCreationActivity notesCreationActivity);

    void inject(NoteDetailActivity noteDetailActivity);
}
