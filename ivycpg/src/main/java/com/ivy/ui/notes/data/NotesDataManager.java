package com.ivy.ui.notes.data;

import com.ivy.core.data.AppDataManagerContract;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.ui.notes.model.NotesBo;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.Single;

public interface NotesDataManager extends AppDataManagerContract {

    Observable<ArrayList<RetailerMasterBO>> fetchRetailers();

    Observable<ArrayList<NotesBo>> fetchNotesData(String retailerId,boolean isFromHomeSrc);

    Single<Boolean> addAndUpdateNote(String retailerId, NotesBo notesBo, String userName, int userId, boolean isEdit);

    Single<Boolean> deleteNote(String retailerId, String tId, String noteID);

    Single<String> getMinMaxDate(String retailerId,boolean isFromHomeSrc);

}
