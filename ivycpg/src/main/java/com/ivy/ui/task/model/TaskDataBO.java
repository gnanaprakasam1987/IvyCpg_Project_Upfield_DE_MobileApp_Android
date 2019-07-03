package com.ivy.ui.task.model;

import android.os.Parcel;
import android.os.Parcelable;

public class TaskDataBO implements Parcelable {

    private String taskDesc;
    private String taskId;
    private int channelId;

    private String taskImgId;
    private String taskImg = "";
    private String taskImgPath;

    private String upload;

    private String tasktitle;
    private int rid;
    private String isdone;
    private String usercreated;
    private String taskOwner = "";
    private String createdDate;
    private String plannedDate;
    private boolean isUpload;
    private int userId;
    private String userName;
    private String taskDueDate = "";
    private String taskEndDate;
    private String taskCategoryDsc = "";
    private int taskCategoryID;
    int flag = 0;
    private String taskEvidenceImg = "";
    private String mode;
    private String sortName;
    private boolean orderByAsc;
    private int serverTask;
    private String taskExecDate;
    private int noOfDueDays;
    private String lastVisitDate;
    private String linkUserName;
    private String remark;

    protected TaskDataBO(Parcel in) {
        taskDesc = in.readString();
        taskId = in.readString();
        channelId = in.readInt();
        taskImg = in.readString();
        taskImgPath = in.readString();
        upload = in.readString();
        tasktitle = in.readString();
        rid = in.readInt();
        isdone = in.readString();
        usercreated = in.readString();
        taskOwner = in.readString();
        createdDate = in.readString();
        plannedDate = in.readString();
        isUpload = in.readByte() != 0;
        userId = in.readInt();
        userName = in.readString();
        taskDueDate = in.readString();
        taskEndDate = in.readString();
        taskCategoryDsc = in.readString();
        taskCategoryID = in.readInt();
        flag = in.readInt();
        taskEvidenceImg = in.readString();
        retailerName = in.readString();
        isChecked = in.readByte() != 0;
        mode = in.readString();
        sortName = in.readString();
        orderByAsc = in.readByte() != 0;
        serverTask = in.readInt();
        taskExecDate = in.readString();
        noOfDueDays = in.readInt();
        lastVisitDate = in.readString();
        linkUserName = in.readString();
        remark = in.readString();
        taskImgId = in.readString();
    }

    public static final Creator<TaskDataBO> CREATOR = new Creator<TaskDataBO>() {
        @Override
        public TaskDataBO createFromParcel(Parcel in) {
            return new TaskDataBO(in);
        }

        @Override
        public TaskDataBO[] newArray(int size) {
            return new TaskDataBO[size];
        }
    };

    public String getPlannedDate() {
        return plannedDate;
    }

    public void setPlannedDate(String plannedDate) {
        this.plannedDate = plannedDate;
    }

    public String getRetailerName() {
        return retailerName;
    }

    public void setRetailerName(String retailerName) {
        this.retailerName = retailerName;
    }

    private String retailerName;

    public TaskDataBO() {

    }


    public TaskDataBO(int rid, String retailername, int flag) {
        super();
        if (flag == 1) {
            this.taskCategoryID = rid;
            this.taskCategoryDsc = retailername;
        } else {
            this.rid = rid;
            this.taskOwner = retailername;
        }
        this.flag = flag;
    }

    private boolean isChecked = false;

    public String getTaskOwner() {
        return taskOwner;
    }

    public void setTaskOwner(String taskOwner) {
        this.taskOwner = taskOwner;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getTasktitle() {
        return tasktitle;
    }

    public void setTasktitle(String tasktitle) {
        this.tasktitle = tasktitle;
    }

    public int getRid() {
        return rid;
    }

    public void setRid(int rid) {
        this.rid = rid;
    }

    public String getIsdone() {
        return isdone;
    }

    public void setIsdone(String isdone) {
        this.isdone = isdone;
    }

    public String getUsercreated() {
        return usercreated;
    }

    public void setUsercreated(String usercreated) {
        this.usercreated = usercreated;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    public int getChannelId() {
        return channelId;
    }

    public void setTaskDesc(String taskDesc) {
        this.taskDesc = taskDesc;
    }

    public String getTaskDesc() {
        return taskDesc;
    }

    public void setUpload(String upload) {
        this.upload = upload;
    }

    public String getUpload() {
        return upload;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    @Override
    public String toString() {
        if (flag == 1)
            return taskCategoryDsc;
        else
            return taskOwner;
    }

    public boolean isUpload() {
        return isUpload;
    }

    public void setIsUpload(boolean isUpload) {
        this.isUpload = isUpload;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTaskImg() {
        return taskImg;
    }

    public void setTaskImg(String taskImg) {
        this.taskImg = taskImg;
    }

    public String getTaskDueDate() {
        return taskDueDate;
    }

    public void setTaskDueDate(String taskDueDate) {
        this.taskDueDate = taskDueDate;
    }

    public String getTaskEndDate() {
        return taskEndDate;
    }

    public void setTaskEndDate(String taskEndDate) {
        this.taskEndDate = taskEndDate;
    }

    public String getTaskCategoryDsc() {
        return taskCategoryDsc;
    }

    public void setTaskCategoryDsc(String taskCategoryDsc) {
        this.taskCategoryDsc = taskCategoryDsc;
    }

    public int getTaskCategoryID() {
        return taskCategoryID;
    }

    public void setTaskCategoryID(int taskCategoryID) {
        this.taskCategoryID = taskCategoryID;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public String getTaskImgId() {
        return taskImgId;
    }

    public void setTaskImgId(String taskImgId) {
        this.taskImgId = taskImgId;
    }

    public String getTaskImgPath() {
        return taskImgPath;
    }

    public void setTaskImgPath(String taskImgPath) {
        this.taskImgPath = taskImgPath;
    }


    public String getTaskEvidenceImg() {
        return taskEvidenceImg;
    }

    public void setTaskEvidenceImg(String taskEvidenceImg) {
        this.taskEvidenceImg = taskEvidenceImg;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getSortName() {
        return sortName;
    }

    public void setSortName(String sortName) {
        this.sortName = sortName;
    }

    public boolean isOrderByAsc() {
        return orderByAsc;
    }

    public void setOrderByAsc(boolean orderByAsc) {
        this.orderByAsc = orderByAsc;
    }

    public int getServerTask() {
        return serverTask;
    }

    public void setServerTask(int serverTask) {
        this.serverTask = serverTask;
    }

    public String getTaskExecDate() {
        return taskExecDate;
    }

    public void setTaskExecDate(String taskExecDate) {
        this.taskExecDate = taskExecDate;
    }

    public int getNoOfDueDays() {
        return noOfDueDays;
    }

    public void setNoOfDueDays(int noOfDueDays) {
        this.noOfDueDays = noOfDueDays;
    }

    public String getLastVisitDate() {
        return lastVisitDate;
    }

    public void setLastVisitDate(String lastVisitDate) {
        this.lastVisitDate = lastVisitDate;
    }

    public String getLinkUserName() {
        return linkUserName;
    }

    public void setLinkUserName(String linkUserName) {
        this.linkUserName = linkUserName;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(taskDesc);
        parcel.writeString(taskId);
        parcel.writeInt(channelId);
        parcel.writeString(taskImg);
        parcel.writeString(taskImgPath);
        parcel.writeString(upload);
        parcel.writeString(tasktitle);
        parcel.writeInt(rid);
        parcel.writeString(isdone);
        parcel.writeString(usercreated);
        parcel.writeString(taskOwner);
        parcel.writeString(createdDate);
        parcel.writeString(plannedDate);
        parcel.writeByte((byte) (isUpload ? 1 : 0));
        parcel.writeInt(userId);
        parcel.writeString(userName);
        parcel.writeString(taskDueDate);
        parcel.writeString(taskEndDate);
        parcel.writeString(taskCategoryDsc);
        parcel.writeInt(taskCategoryID);
        parcel.writeInt(flag);
        parcel.writeString(taskEvidenceImg);
        parcel.writeString(retailerName);
        parcel.writeByte((byte) (isChecked ? 1 : 0));
        parcel.writeString(mode);
        parcel.writeString(sortName);
        parcel.writeByte((byte) (orderByAsc ? 1 : 0));
        parcel.writeInt(serverTask);
        parcel.writeString(taskExecDate);
        parcel.writeInt(noOfDueDays);
        parcel.writeString(lastVisitDate);
        parcel.writeString(linkUserName);
        parcel.writeString(remark);
        parcel.writeString(taskImgId);
    }

}
