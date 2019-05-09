package com.ivy.ui.notes;

import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.ui.notes.model.NotesBo;

import java.util.ArrayList;
import java.util.HashMap;

public class NotesTestDataFactory {

    public static RetailerMasterBO retailerMasterBO = new RetailerMasterBO(1, "abcd");

    public static UserMasterBO userMasterBO = new UserMasterBO(2, "user1");

    public static String noteTitle = "1";

    public static String noteDesc = "abc";

    public static String fromPeriod = "JAN 2019";
    public static String toPeriod = "APR 2019";


    public static ArrayList<NotesBo> getNotesList() {
        ArrayList<NotesBo> nLocalArrayList = new ArrayList<>();
        NotesBo notesBo = new NotesBo();
        notesBo.setTid("2131");
        notesBo.setNotesTitle("Note Title");
        notesBo.setNotesDesc("Note Desc");
        notesBo.setNoteId("54");
        notesBo.setCreatedDate("12/02/2019");
        notesBo.setTime("11:13:35");
        notesBo.setModifiedDate("13/05/2019 10:06:45");

        nLocalArrayList.add(notesBo);

        notesBo = new NotesBo();
        notesBo.setTid("2131");
        notesBo.setNotesTitle("Note Title");
        notesBo.setNotesDesc("Note Desc");
        notesBo.setNoteId("54");
        notesBo.setCreatedDate("14/02/2019");
        notesBo.setTime("11:13:35");
        notesBo.setModifiedDate("13/05/2019 10:06:45");
        nLocalArrayList.add(notesBo);

        notesBo = new NotesBo();
        notesBo.setTid("2131");
        notesBo.setNotesTitle("Note Title");
        notesBo.setNotesDesc("Note Desc");
        notesBo.setNoteId("54");
        notesBo.setCreatedDate("10/02/2019");
        notesBo.setTime("11:13:35");
        notesBo.setModifiedDate("13/05/2019 10:06:45");

        nLocalArrayList.add(notesBo);

        return nLocalArrayList;
    }


    public static ArrayList<NotesBo> getMonthList() {
        ArrayList<NotesBo> nLocalArrayList = new ArrayList<>();
        NotesBo notesBo = new NotesBo();
        notesBo.setTid("2131");
        notesBo.setNotesTitle("Note Title");
        notesBo.setNotesDesc("Note Desc");
        notesBo.setNoteId("54");
        notesBo.setCreatedDate("2019/02/14");
        notesBo.setTime("11:13:35");
        notesBo.setModifiedDate("13/05/2019 10:06:45");

        nLocalArrayList.add(notesBo);

        notesBo = new NotesBo();
        notesBo.setTid("2131");
        notesBo.setNotesTitle("Note Title");
        notesBo.setNotesDesc("Note Desc");
        notesBo.setNoteId("54");
        notesBo.setCreatedDate("2019/03/14");
        notesBo.setTime("11:13:35");
        notesBo.setModifiedDate("13/05/2019 10:06:45");
        nLocalArrayList.add(notesBo);

        notesBo = new NotesBo();
        notesBo.setTid("2131");
        notesBo.setNotesTitle("Note Title");
        notesBo.setNotesDesc("Note Desc");
        notesBo.setNoteId("54");
        notesBo.setCreatedDate("2019/04/14");
        notesBo.setTime("11:13:35");
        notesBo.setModifiedDate("13/05/2019 10:06:45");

        nLocalArrayList.add(notesBo);

        return nLocalArrayList;
    }



    public static ArrayList<NotesBo> getNotesEmptyList() {
        ArrayList<NotesBo> nLocalArrayList = new ArrayList<>();
        nLocalArrayList.add(new NotesBo());
        return nLocalArrayList;
    }

    public static ArrayList<RetailerMasterBO> getRetailerNames() {
        ArrayList<RetailerMasterBO> nLocalArrayList = new ArrayList<>();
        nLocalArrayList.add(new RetailerMasterBO(1, "abc"));
        nLocalArrayList.add(new RetailerMasterBO(2, "abc1"));
        nLocalArrayList.add(new RetailerMasterBO(3, "abc2"));
        nLocalArrayList.add(new RetailerMasterBO(4, "abc3"));

        return nLocalArrayList;
    }

    public static NotesBo getNoteBo() {
        NotesBo notesBo = new NotesBo();
        notesBo.setTid("2131");
        notesBo.setNotesTitle("Note Title");
        notesBo.setNotesDesc("Note Desc");
        notesBo.setNoteId("54");
        return notesBo;
    }

    public static HashMap<String, String> getLabels() {
        HashMap<String, String> labels = new HashMap<>();
        labels.put("acb", "Title");
        labels.put("abcd", "Desc");
        labels.put("abcde", "Date");
        labels.put("abcdef", "Modified");
        return labels;

    }
}
