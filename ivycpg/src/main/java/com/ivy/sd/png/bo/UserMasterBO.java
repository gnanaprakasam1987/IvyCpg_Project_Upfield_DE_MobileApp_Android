package com.ivy.sd.png.bo;

import java.util.ArrayList;

public class UserMasterBO {
    private int distributorid;
    private String distributorName = "";
    private String distributorTinNumber = "";
    private String distributorContactNumber = "";
    private String distributorAddress1 = "";
    private String distributorAddress2 = "";
    private String distributorAddress3 = "";
    private String faxNo = "";
    private String distributorCode = "";
    private int branchId;
    private int vanId;
    private int userid;
    private String userName;
    private String userCode;
    private String loginName;
    private String password;
    private int organizationId;
    private String downloadDate;
    private String custommsg;
    private String accountno;
    private double creditlimit;
    private String adminContactNo;
    private int isJointCall;
    private String vanno;
    private int schemeFactor;
    private int upliftFactor;
    private String cstNo;
    private int counterId;
    private String counterName;
    private ArrayList<UserMasterBO> joinCallUserList;
    private String userType;
    private String imagePath;
    private String backupSellerID;
    private boolean isBackup;
    private String userlevelId;
    private String userPositionId;
    private String RetailerID;

    public UserMasterBO() {

    }

    public UserMasterBO(int userID, String userName) {

        this.userid = userID;
        this.userName = userName;
    }

    public String getFaxNo() {
        return faxNo;
    }

    public void setFaxNo(String faxNo) {
        this.faxNo = faxNo;
    }

    public String getDistributorAddress1() {
        return distributorAddress1;
    }

    public void setDistributorAddress1(String distributorAddress1) {
        this.distributorAddress1 = distributorAddress1;
    }

    public String getDistributorAddress2() {
        return distributorAddress2;
    }

    public void setDistributorAddress2(String distributorAddress2) {
        this.distributorAddress2 = distributorAddress2;
    }

    public String getDistributorAddress3() {
        return distributorAddress3;
    }

    public void setDistributorAddress3(String distributorAddress3) {
        this.distributorAddress3 = distributorAddress3;
    }

    public String getDistributorCode() {
        return distributorCode;
    }

    public void setDistributorCode(String distributorCode) {
        this.distributorCode = distributorCode;
    }

    public int getCounterId() {
        return counterId;
    }

    public void setCounterId(int counterId) {
        this.counterId = counterId;
    }

    public String getCounterName() {
        return counterName;
    }

    public void setCounterName(String counterName) {
        this.counterName = counterName;
    }

    public String getCstNo() {
        return cstNo;
    }

    public void setCstNo(String cstNo) {
        this.cstNo = cstNo;
    }

    public String getVanno() {
        return vanno;
    }


    public void setVanno(String vanno) {
        this.vanno = vanno;
    }

    public double getCreditlimit() {
        return creditlimit;
    }

    public void setCreditlimit(double creditlimit) {
        this.creditlimit = creditlimit;
    }

    public String getAccountno() {
        return accountno;
    }

    public void setAccountno(String accountno) {
        this.accountno = accountno;
    }

    public String getCustommsg() {
        return custommsg;
    }

    public void setCustommsg(String custommsg) {
        this.custommsg = custommsg;
    }


    public int getDistributorid() {
        return distributorid;
    }

    public void setDistributorid(int distributorid) {
        this.distributorid = distributorid;
    }

    public String getDistributorName() {
        return distributorName;
    }

    public void setDistributorName(String distributorName) {
        this.distributorName = distributorName;
    }

    public String getDistributorTinNumber() {
        return distributorTinNumber;
    }

    public void setDistributorTinNumber(String distributorTinNumber) {
        this.distributorTinNumber = distributorTinNumber;
    }

    public int getBranchId() {
        return branchId;
    }

    public void setBranchId(int branchId) {
        this.branchId = branchId;
    }

    public int getVanId() {
        return vanId;
    }

    public void setVanId(int vanId) {
        this.vanId = vanId;
    }

    public int getUserid() {
        return userid;
    }

    public void setUserid(int userid) {
        this.userid = userid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDistributorContactNumber() {
        return distributorContactNumber;
    }

    public void setDistributorContactNumber(String distributorContactNumber) {
        this.distributorContactNumber = distributorContactNumber;
    }

    public int getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(int organizationId) {
        this.organizationId = organizationId;
    }


    public String getDownloadDate() {
        return downloadDate;
    }

    public void setDownloadDate(String downloadDate) {
        this.downloadDate = downloadDate;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public String getAdminContactNo() {
        return adminContactNo;
    }

    public void setAdminContactNo(String adminContactNo) {
        this.adminContactNo = adminContactNo;
    }

    public ArrayList<UserMasterBO> getJoinCallUserList() {
        if (joinCallUserList == null)
            return new ArrayList<UserMasterBO>();
        return joinCallUserList;
    }

    public void setJoinCallUserList(ArrayList<UserMasterBO> joinCallUserList) {
        this.joinCallUserList = joinCallUserList;
    }

    public int getIsJointCall() {
        return isJointCall;
    }

    public void setIsJointCall(int isJointCall) {
        this.isJointCall = isJointCall;
    }

    public int getUpliftFactor() {
        return upliftFactor;
    }

    public void setUpliftFactor(int upliftFactor) {
        this.upliftFactor = upliftFactor;
    }

    public int getSchemeFactor() {
        return schemeFactor;
    }

    public void setSchemeFactor(int schemeFactor) {
        this.schemeFactor = schemeFactor;
    }

    @Override
    public String toString() {
        return userName;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getGSTNumber() {
        return GSTNumber;
    }

    public void setGSTNumber(String GSTNumber) {
        this.GSTNumber = GSTNumber;
    }

    private String GSTNumber;

    public String getBackupSellerID() {
        return backupSellerID;
    }

    public void setBackupSellerID(String backupSellerID) {
        this.backupSellerID = backupSellerID;
    }

    public boolean isBackup() {
        return isBackup;
    }

    public void setBackup(boolean backup) {
        isBackup = backup;
    }

    public String getUserlevelId() {
        return userlevelId;
    }

    public void setUserlevelId(String userlevelId) {
        this.userlevelId = userlevelId;
    }

    public String getUserPositionId() {
        return userPositionId;
    }

    public void setUserPositionId(String userPositionId) {
        this.userPositionId = userPositionId;
    }

    public String getRetailerID() {
        return RetailerID;
    }

    public void setRetailerID(String retailerID) {
        RetailerID = retailerID;
    }
}
