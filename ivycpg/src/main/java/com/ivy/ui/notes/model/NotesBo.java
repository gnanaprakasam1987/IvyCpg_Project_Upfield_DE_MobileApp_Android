package com.ivy.ui.notes.model;

import android.os.Parcel;
import android.os.Parcelable;

public class NotesBo implements Parcelable {
    private String tid;
    private String retailerId;
    private String retailerName;
    private String createdDate;
    private String time;
    private String notesTitle;
    private String notesDesc;
    private String upload;
    private String userName;
    private int userId;
    private String noteId;
    private String status;
    private String createdBy;
    private String modifiedBy;
    private String modifiedDate;


    public NotesBo() {
    }

    public NotesBo(String notesTitle, String notesDesc) {
        this.notesTitle = notesTitle;
        this.notesDesc = notesDesc;
    }

    protected NotesBo(Parcel in) {
        tid = in.readString();
        retailerId = in.readString();
        retailerName = in.readString();
        createdDate = in.readString();
        time = in.readString();
        notesTitle = in.readString();
        notesDesc = in.readString();
        upload = in.readString();
        userName = in.readString();
        userId = in.readInt();
        noteId = in.readString();
        status = in.readString();
        createdBy = in.readString();
        modifiedBy = in.readString();
        modifiedDate = in.readString();
    }

    public static final Creator<NotesBo> CREATOR = new Creator<NotesBo>() {
        @Override
        public NotesBo createFromParcel(Parcel in) {
            return new NotesBo(in);
        }

        @Override
        public NotesBo[] newArray(int size) {
            return new NotesBo[size];
        }
    };

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getRetailerId() {
        return retailerId;
    }

    public void setRetailerId(String retailerId) {
        this.retailerId = retailerId;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getNotesTitle() {
        return notesTitle;
    }

    public void setNotesTitle(String notesTitle) {
        this.notesTitle = notesTitle;
    }

    public String getNotesDesc() {
        return notesDesc;
    }

    public void setNotesDesc(String notesDesc) {
        this.notesDesc = notesDesc;
    }

    public String getUpload() {
        return upload;
    }

    public void setUpload(String upload) {
        this.upload = upload;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getRetailerName() {
        return retailerName;
    }

    public void setRetailerName(String retailerName) {
        this.retailerName = retailerName;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public String getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(String modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getNoteId() {
        return noteId;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(tid);
        dest.writeString(retailerId);
        dest.writeString(retailerName);
        dest.writeString(createdDate);
        dest.writeString(time);
        dest.writeString(notesTitle);
        dest.writeString(notesDesc);
        dest.writeString(upload);
        dest.writeString(userName);
        dest.writeInt(userId);
        dest.writeString(noteId);
        dest.writeString(status);
        dest.writeString(createdBy);
        dest.writeString(modifiedBy);
        dest.writeString(modifiedDate);
    }
}
