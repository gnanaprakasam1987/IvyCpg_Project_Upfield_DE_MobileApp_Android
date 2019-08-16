package com.ivy.ui.notes;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.ui.notes.model.NotesBo;

import java.util.ArrayList;
import java.util.HashMap;

public interface NotesContract extends BaseIvyView {

    interface NotesView extends BaseIvyView {

        void onErrorMsg();

        void onDeleteSuccess();

        void updateLabelNames(HashMap<String, String> labelMap);
    }

    interface NotesListView extends NotesView {

        void updateNotesList(ArrayList<NotesBo> notesBoArrayList, boolean isFromSort);

        void updateMinMaxDate(String minMaxDate);

        void showDateSelectionErrorMsg();
    }

    interface NoteCreationView extends NotesView {

        void setRetailerListData(ArrayList<RetailerMasterBO> retailerList);

        void showNoteTitleError();

        void showNoteDescError();

        void showRetailerSelectionError();

        void onSaveSuccess();
    }

    @PerActivity
    interface NotesPresenter<V extends NotesView> extends BaseIvyPresenter<V> {

        void fetchLabels();

        void fetchNotesData(String retailerId, boolean isFromRetailerSrc, int lastFilterSelectedPos, boolean sortBy);

        void fetchRetailerNames();

        void searchByMonthOrDateWise(String fromMonth, String toMonth, ArrayList<NotesBo> searchBoArrayList);

        void createNewNotes(String retailerId, NotesBo notesBo, boolean isEdit);

        void deleteNote(String retailerId, String tid, String noteId);

        ArrayList<NotesBo> getNoteList();

        int getUserID();

        String getRetailerID();

        String getUserName();

        String outDateFormat();

        void saveModuleCompletion(String menuCode);

        void updateModuleTime();

        boolean validateData(String noteTitle, String noteDesc, String retailerId);

        void orderBySortList(int sortCategory, boolean sortType, ArrayList<NotesBo> sortArrayList, boolean isFromSort);

        void getMinMaxDate(String retailerId, boolean isFromHomeSrc);

        boolean isNoteEditByOtherUser();

        boolean enableDisplayMode();

    }
}
