package com.ivy.ui.notes;

import com.ivy.ui.notes.model.NotesBo;

import java.util.ArrayList;

public interface NoteOnclickListener {

    void onClickEditNote(NotesBo noteBo);

    void onClickDeleteNote(NotesBo noteBo);

    void onClickDetailView(NotesBo notesBo);

    void onSortClick(int orderBy, boolean sortBy);
}
