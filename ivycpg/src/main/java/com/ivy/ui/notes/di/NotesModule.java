package com.ivy.ui.notes.di;

import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.notes.NotesContract;
import com.ivy.ui.notes.data.NotesDataManager;
import com.ivy.ui.notes.data.NotesDataManagerImpl;
import com.ivy.ui.notes.presenter.NotesPresenterImpl;
import com.ivy.utils.rx.AppSchedulerProvider;
import com.ivy.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;
import io.reactivex.disposables.CompositeDisposable;

@Module
public class NotesModule {
    private NotesContract.NotesView notesView;

    public NotesModule(NotesContract.NotesView notesView) {
        this.notesView = notesView;
    }

    @Provides
    NotesContract.NotesView provideView() {
        return notesView;
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
    NotesDataManager notesDataManager(NotesDataManagerImpl notesDataManagerImpl) {
        return notesDataManagerImpl;
    }

    @Provides
    @PerActivity
    NotesContract.NotesPresenter<NotesContract.NotesView> provideNotePresenter(NotesPresenterImpl<NotesContract.NotesView> notesPresenter) {
        return notesPresenter;
    }
}
