package com.ivy.sd.png.bo;

/**
 * Created by rajesh.k on 28-04-2016.
 */
public class TeamLeadBO {
    public String getListID() {
        return ListID;
    }



    public void setListCode(String listCode) {
        ListCode = listCode;
    }



    public String getUserID() {
        return UserID;
    }

    public void setUserID(String userID) {
        UserID = userID;
    }



    public void setMerchandiserName(String merchandiserName) {
        MerchandiserName = merchandiserName;
    }

    public int getAttendance() {
        return Attendance;
    }

    public void setAttendance(int attendance) {
        Attendance = attendance;
    }

    public int getReadiness() {
        return Readiness;
    }

    public void setReadiness(int readiness) {
        Readiness = readiness;
    }



    public void setListID(String listID) {
        ListID = listID;

    }

    private String ListID,ListCode,listType,AccountID,UserID,MerchandiserName;
    private int Attendance;
    private int Readiness;

    private String ListName;

    public String getListName() {
        return ListName;
    }

    public void setListName(String listName) {
        ListName = listName;
    }


    private String status;
    private int retailerCount;
    private String userName;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getRetailerCount() {
        return retailerCount;
    }

    public void setRetailerCount(int retailerCount) {
        this.retailerCount = retailerCount;
    }

    @Override
    public String toString() {
        return userName;
    }
}
