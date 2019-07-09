package com.ivy.ui.notes.data;

import android.database.Cursor;

import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.util.Commons;
import com.ivy.ui.notes.model.NotesBo;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.StringUtils;

import java.util.ArrayList;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;

public class NotesDataManagerImpl implements NotesDataManager {
    private DBUtil mDbUtil;

    private AppDataProvider appDataProvider;

    @Inject
    NotesDataManagerImpl(@DataBaseInfo DBUtil mDbUtil, AppDataProvider appDataProvider) {
        this.mDbUtil = mDbUtil;
        this.appDataProvider = appDataProvider;
    }

    private void initDb() {
        mDbUtil.createDataBase();
        if (mDbUtil.isDbNullOrClosed())
            mDbUtil.openDataBase();
    }

    private void shutDownDb() {
        mDbUtil.closeDB();
    }

    @Override
    public Observable<ArrayList<RetailerMasterBO>> fetchRetailers() {
        return Observable.fromCallable(() -> {
            try {
                RetailerMasterBO temp;
                ArrayList<RetailerMasterBO> retailerMaster = new ArrayList<>();
                for (RetailerMasterBO retBo : appDataProvider.getRetailerMasters()) {
                    temp = new RetailerMasterBO();
                    temp.setRetailerID(retBo.getRetailerID());
                    temp.setRetailerName(retBo.getRetailerName());
                    retailerMaster.add(temp);
                }
                return retailerMaster;
            } catch (Exception e) {
                Commons.printException(e);
            }
            return new ArrayList<>();
        });
    }

    @Override
    public Observable<ArrayList<NotesBo>> fetchNotesData(String retailerId, boolean isFromSrc) {
        return Observable.fromCallable(() -> {

            try {
                String retailerQuery = "";
                if (!isFromSrc) {
                    retailerQuery = " WHERE RN.RetailerId=" + retailerId + " AND (RN.Status !='D' OR RN.Status IS NULL)";
                } else {
                    retailerQuery = " WHERE (RN.Status !='D' OR RN.Status IS NULL)";
                }

                ArrayList<NotesBo> notesBoArrayList = new ArrayList<>();
                NotesBo noteBo;
                initDb();

                String fetchQuery = "SELECT RN.RetailerId,RN.Date,RN.Time,RN.Title," +
                        "RN.Description,RN.UserName,IFNULL(RM.RetailerName,'') as RetailerName" +
                        ",RN.userId,IFNULL(RN.NoteId,''),IFNULL(RN.ModifiedDateTime,'') as ModifiedDate,IFNULL(RN.ModifiedBy,'') as ModifiedBy,RN.Status,IFNULL(RN.Tid,'0') as Tid,RN.Upload" +
                        " FROM RetailerNotes RN" +
                        " LEFT JOIN" +
                        " RetailerMaster RM ON RN.RetailerId = RM.RetailerId" +
                        retailerQuery +
                        " ORDER BY RN.rowId DESC";
                Cursor c = mDbUtil.selectSQL(fetchQuery);

                if (c != null) {
                    while (c.moveToNext()) {
                        noteBo = new NotesBo();
                        noteBo.setRetailerId(c.getString(0));
                        noteBo.setCreatedDate(c.getString(1));
                        noteBo.setTime(c.getString(2));
                        noteBo.setNotesTitle(c.getString(3));
                        noteBo.setNotesDesc(c.getString(4));
                        noteBo.setCreatedBy(c.getString(5));
                        noteBo.setRetailerName(c.getString(6));
                        noteBo.setUserId(c.getInt(7));
                        noteBo.setNoteId(c.getString(8));
                        noteBo.setModifiedDate(c.getString(9));
                        noteBo.setModifiedBy(c.getString(10));
                        noteBo.setStatus(c.getString(11));
                        noteBo.setTid(c.getString(12));
                        noteBo.setUpload(c.getString(13));
                        notesBoArrayList.add(noteBo);
                    }
                    c.close();
                }
                shutDownDb();
                return notesBoArrayList;
            } catch (Exception e) {
                Commons.printException(e);
            }
            shutDownDb();
            return new ArrayList<>();
        });
    }

    @Override
    public Single<Boolean> addAndUpdateNote(String retailerId, NotesBo notesBo, String userName, int userId, boolean isEdit) {
        return Single.fromCallable(() -> {
            String id = StringUtils.getStringQueryParam(appDataProvider.getUser()
                    .getUserid() + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID));
            String date = StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL));
            String time = StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.TIME));

            String noteTitle = notesBo.getNotesTitle().replaceAll("'", " ");
            String noteDesc = notesBo.getNotesDesc().replaceAll("'", " ");
            String status = StringUtils.getStringQueryParam("I");
            String noteId = notesBo.getNoteId() == null ? null : StringUtils.getStringQueryParam(notesBo.getNoteId());
            String modifiedDateTime = null;

            String whereCond = " Tid =''";

            String column = "Tid,RetailerId,Date,Time,Title,Description,UserName," +
                    "userId,NoteId,ModifiedDateTime,Status,Upload";
            String value;

            if (isEdit) {
                date = StringUtils.getStringQueryParam(notesBo.getCreatedDate());
                time = StringUtils.getStringQueryParam(notesBo.getTime());
                status = StringUtils.getStringQueryParam("U");
                modifiedDateTime = StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_TIME_NEW));
                if (notesBo.getTid().equals("0")) {
                    whereCond = " NoteID =" + StringUtils.getStringQueryParam(notesBo.getNoteId());
                } else {
                    whereCond = " Tid =" + StringUtils.getStringQueryParam(notesBo.getTid());
                }
            }

            try {
                initDb();

                String sb = "Select count(Tid) from RetailerNotes Where" + whereCond;

                Cursor c = mDbUtil.selectSQL(sb);
                if (c.getCount() > 0) {
                    if (c.moveToNext()) {
                        mDbUtil.deleteSQL("RetailerNotes", whereCond, false);
                    }
                    c.close();
                }


                value = id + "," +
                        StringUtils.getStringQueryParam(retailerId) + "," +
                        date + "," +
                        time + "," +
                        StringUtils.getStringQueryParam(noteTitle) + "," +
                        StringUtils.getStringQueryParam(noteDesc) + "," +
                        StringUtils.getStringQueryParam(userName) + "," +
                        userId + "," +
                        noteId + "," +
                        modifiedDateTime + "," +
                        status + "," +
                        StringUtils.getStringQueryParam("N");

                mDbUtil.insertSQL("RetailerNotes", column, value);

                shutDownDb();
                return true;
            } catch (Exception e) {
                Commons.printException(e);
            }
            shutDownDb();
            return false;
        });
    }

    @Override
    public Single<Boolean> deleteNote(String retailerId, String tId, String noteId) {

        String id = "";
        if (noteId != null
                && !noteId.equals("0")) {
            id = " NoteID =" + StringUtils.getStringQueryParam(noteId);
        } else {
            id = " Tid =" + StringUtils.getStringQueryParam(tId);
        }
        String finalId = id;
        return Single.fromCallable(() -> {
            try {
                boolean isFlag;
                initDb();


                Cursor c = mDbUtil.selectSQL("Select Tid from RetailerNotes Where" + finalId + " And Upload='Y' OR NoteId !='0'");

                if (c.getCount() > 0) {
                    isFlag = true;
                    c.close();
                } else {
                    isFlag = false;
                }
                return isFlag;
            } catch (Exception e) {
                Commons.printException(e);
            }
            return false;
        }).flatMap(isUploaded -> Single.fromCallable(() -> {
            try {
                if (isUploaded)
                    mDbUtil.updateSQL("UPDATE RetailerNotes " +
                            "SET Status='D',Upload ='N' WHERE " + finalId);
                else
                    mDbUtil.deleteSQL("RetailerNotes", finalId, false);


                shutDownDb();
                return true;
            } catch (Exception ignore) {

            }
            shutDownDb();
            return false;
        }));
    }

    @Override
    public Single<String> getMinMaxDate(String retailerId, boolean isFromHomeSrc) {
        return Single.fromCallable(() -> {
            try {
                initDb();
                String appendRetCond = "";

                if (!isFromHomeSrc) {
                    appendRetCond = " WHERE RetailerId=" + retailerId + " AND (Status !='D' OR Status IS NULL)";
                } else {
                    appendRetCond = " WHERE (Status !='D' OR Status IS NULL)";
                }

                String minMaxStr = "";
                String query = "SELECT IFNULL(MIN(date),'')," +
                        "IFNULL(MAX(date),'')" +
                        " FROM RetailerNotes " +
                        appendRetCond;

                Cursor c = mDbUtil.selectSQL(query);

                if (c != null) {
                    if (c.moveToNext()) {

                        if (!c.getString(0).isEmpty()
                                && !c.getString(1).isEmpty())
                            minMaxStr = c.getString(0) + "," + c.getString(1);
                    }
                    c.close();
                }
                shutDownDb();
                return minMaxStr;

            } catch (Exception e) {
                Commons.printException(e);
            }
            shutDownDb();
            return "";
        });
    }

    @Override
    public void tearDown() {
        shutDownDb();
    }
}
