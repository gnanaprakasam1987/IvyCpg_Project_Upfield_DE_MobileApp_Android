package com.ivy.sd.png.provider;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;

public class UserMasterHelper {

    private static UserMasterHelper instance = null;
    private final Context context;
    private final BusinessModel bmodel;
    private UserMasterBO userMasterBO;

    private UserMasterHelper(Context context) {
        this.context = context;
        this.bmodel = (BusinessModel) context;
        userMasterBO = new UserMasterBO();
    }

    public static UserMasterHelper getInstance(Context context) {
        if (instance == null) {
            instance = new UserMasterHelper(context);
        }
        return instance;
    }

    public void downloadUserDetails() {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Cursor c = db.selectSQL("select " + DataMembers.tbl_userMaster_cols
                    + " from Usermaster where isDeviceUser=1");

            if (c != null) {
                if (c.moveToLast()) {
                    userMasterBO = new UserMasterBO();
                    userMasterBO.setDistributorid(c.getInt(c
                            .getColumnIndex("distributorid")));
                    userMasterBO.setBranchId(c.getInt(c
                            .getColumnIndex("branchid")));
                    userMasterBO.setVanId(c.getInt(c
                            .getColumnIndex("vanid")));
                    userMasterBO.setUserid(c.getInt(c
                            .getColumnIndex("userid")));
                    userMasterBO.setUserName(c.getString(c
                            .getColumnIndex("username")));
                    userMasterBO.setUserType(c.getString(c
                            .getColumnIndex("usertype")));
                    userMasterBO.setPassword(c.getString(c
                            .getColumnIndex("Password")));
                    userMasterBO.setLoginName(c.getString(c
                            .getColumnIndex("loginid")));
                    userMasterBO.setDistributorContactNumber(c.getString(c
                            .getColumnIndex("distContactNo")));
                    userMasterBO.setOrganizationId(c.getInt(c
                            .getColumnIndex("OrganisationId")));
                    userMasterBO.setDownloadDate(c.getString(c
                            .getColumnIndex("downloaddate")));
                    userMasterBO.setUserCode(c.getString(c
                            .getColumnIndex("UserCode")));
                    userMasterBO.setCustommsg(c.getString(c
                            .getColumnIndex("custommsg")));
                    userMasterBO.setAccountno(c.getString(c
                            .getColumnIndex("accountno")));
                    userMasterBO.setCreditlimit(c.getDouble(c
                            .getColumnIndex("credit_limit")));

                    if (c.getString(c.getColumnIndex("admincno")) == null
                            || "null".equals(c.getString(c.getColumnIndex("admincno")))) {
                        userMasterBO.setAdminContactNo("");
                    } else {
                        userMasterBO.setAdminContactNo(c.getString(c
                                .getColumnIndex("admincno")));
                    }

                    userMasterBO.setVanno(c.getString(c
                            .getColumnIndex("vanno")));
                    userMasterBO.setSchemeFactor(c.getInt(c
                            .getColumnIndex("SchemeFactor")));
                    userMasterBO.setUpliftFactor(c.getInt(c
                            .getColumnIndex("upliftFactor")));
                    userMasterBO.setImagePath(c.getString(c
                            .getColumnIndex("ProfileImagePath")));

                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public boolean getSyncStatus() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        try {
            db.createDataBase();
            db.openDataBase();
            Cursor c = db.selectSQL("select " + DataMembers.tbl_userMaster_cols
                    + " from " + DataMembers.tbl_userMaster
                    + " where isDeviceUser=1");

            if (c != null) {
                if (c.getCount() == 0) {
                    c.close();
                    db.closeDB();
                    return false;
                }

                if (c.moveToNext()) {
                    userMasterBO.setDistributorid(c.getInt(c
                            .getColumnIndex("distributorid")));
                    userMasterBO.setBranchId(c.getInt(c
                            .getColumnIndex("branchid")));
                    userMasterBO.setVanId(c.getInt(c
                            .getColumnIndex("vanid")));
                    userMasterBO.setUserid(c.getInt(c
                            .getColumnIndex("userid")));
                    userMasterBO.setUserName(c.getString(c
                            .getColumnIndex("username")));
                    userMasterBO.setUserType(c.getString(c
                            .getColumnIndex("usertype")));
                    userMasterBO.setPassword(c.getString(c
                            .getColumnIndex("Password")));
                    userMasterBO.setLoginName(c.getString(c
                            .getColumnIndex("loginid")));
                    userMasterBO.setDistributorContactNumber(c.getString(c
                            .getColumnIndex("distContactNo")));
                    userMasterBO.setOrganizationId(c.getInt(c
                            .getColumnIndex("OrganisationId")));
                    userMasterBO.setDownloadDate(c.getString(c
                            .getColumnIndex("downloaddate")));
                    userMasterBO.setUserCode(c.getString(c
                            .getColumnIndex("UserCode")));
                    userMasterBO.setSchemeFactor(c.getInt(c
                            .getColumnIndex("SchemeFactor")));
                    userMasterBO.setUpliftFactor(c.getInt(c
                            .getColumnIndex("upliftFactor")));
                }
                c.close();
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        db.closeDB();
        return true;
    }

    /**
     * Method to use download joinCall users list from usermaster where isDeviceuser = 0 and relationShip = PARENT
     * Set the joint call users list inside UserMasterBO
     */
    public void downloadJoinCallusers() {
        ArrayList<UserMasterBO> mJoinCallUserlist = new ArrayList<>();
        UserMasterBO userBO;

        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Cursor c = db.selectSQL("select " + DataMembers.tbl_userMaster_cols
                    + " from Usermaster where isDeviceUser=0 AND relationship !='CHILD'");

            if (c != null) {
                while (c.moveToNext()) {
                    userBO = new UserMasterBO();

                    userBO.setDistributorid(c.getInt(c
                            .getColumnIndex("distributorid")));
                    userBO.setDistributorTinNumber(c.getString(c
                            .getColumnIndex("distributorTinNumber")));
                    userBO.setDistributorName(c.getString(c
                            .getColumnIndex("distributorName")));
                    userBO.setBranchId(c.getInt(c
                            .getColumnIndex("branchid")));
                    userBO.setVanId(c.getInt(c
                            .getColumnIndex("vanid")));
                    userBO.setUserid(c.getInt(c
                            .getColumnIndex("userid")));
                    userBO.setUserName(c.getString(c
                            .getColumnIndex("username")));

                    userBO.setPassword(c.getString(c
                            .getColumnIndex("Password")));
                    userBO.setLoginName(c.getString(c
                            .getColumnIndex("loginid")));
                    userBO.setDistributorContactNumber(c.getString(c
                            .getColumnIndex("distContactNo")));

                    userBO.setOrganizationId(c.getInt(c
                            .getColumnIndex("OrganisationId")));
                    userBO.setDownloadDate(c.getString(c
                            .getColumnIndex("downloaddate")));
                    userBO.setUserCode(c.getString(c
                            .getColumnIndex("UserCode")));

                    userBO.setCustommsg(c.getString(c
                            .getColumnIndex("custommsg")));
                    userBO.setAccountno(c.getString(c
                            .getColumnIndex("accountno")));
                    userBO.setCreditlimit(c.getDouble(c
                            .getColumnIndex("credit_limit")));

                    if (c.getString(c.getColumnIndex("admincno")) == null
                            || "null".equals(c.getString(c.getColumnIndex("admincno")))) {
                        userBO.setAdminContactNo("");
                    } else {
                        userBO.setAdminContactNo(c.getString(c
                                .getColumnIndex("admincno")));
                    }

                    userBO.setIsJointCall(c.getInt(c
                            .getColumnIndex("isJointCall")));

                    userBO.setUserType(c.getString(c.getColumnIndex("usertype")));

                    mJoinCallUserlist.add(userBO);
                }
                userMasterBO.setJoinCallUserList(mJoinCallUserlist);
                c.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }


    public UserMasterBO getUserMasterBO() {
        return userMasterBO;
    }

    public void downloadDistributionDetails() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        try {
            db.createDataBase();
            db.openDataBase();
            String sb = "Select DName,CNumber,Address1,Address2,Address3,TinNo,CSTNo,FaxNo,code,GSTNumber from DistributorMaster " +
                    "where did=" + userMasterBO.getDistributorid();
            Cursor c = db.selectSQL(sb);
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    userMasterBO.setDistributorName(c.getString(0));
                    userMasterBO.setDistributorContactNumber(c.getString(1));
                    userMasterBO.setDistributorAddress1(c.getString(2));
                    userMasterBO.setDistributorAddress2(c.getString(3));
                    userMasterBO.setDistributorAddress3(c.getString(4));
                    userMasterBO.setDistributorTinNumber(c.getString(5));
                    userMasterBO.setCstNo(c.getString(6));
                    userMasterBO.setFaxNo(c.getString(7));
                    userMasterBO.setDistributorCode(c.getString(8));
                    userMasterBO.setGSTNumber(c.getString(9));
                }
                c.close();
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }
    }

    /**
     * Method to update changed password in SQLite
     *
     * @param UserID       -userID
     * @param pwd-password
     * @return - true or false
     */
    public boolean changePassword(int UserID, String pwd) {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        try {
            db.createDataBase();
            db.openDataBase();

            String password = "";
            if(bmodel.configurationMasterHelper.IS_PASSWORD_ENCRIPTED) {
                password=bmodel.synchronizationHelper.encryptPassword(pwd);
            }else
            {
                password=pwd;
            }
            String query = "Update UserMaster set Password='" + password
                    + "' where userID=" + UserID;
            db.executeQ(query);
            bmodel.userMasterHelper.getUserMasterBO().setPassword(password);
            String query1 = "Update AppVariables set PasswordCreatedDate=" + bmodel.QT(bmodel.userMasterHelper.getUserMasterBO().getDownloadDate());
            db.executeQ(query1);
            db.close();
            return true;
        } catch (Exception e) {
            Commons.printException("" + e);
            db.close();
            return false;
        }
    }

    public void updateDistributorId(String distid, String parentId, String distname){

        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        try {
            db.createDataBase();
            db.openDataBase();

            String query = "update userMaster set distributorid=" + parentId
                    + ", branchid="+distid+", distributorName='"+distname+"' where userID=" + userMasterBO.getUserid();

            db.executeQ(query);
            db.close();
            if (getUserMasterBO() != null) {
                getUserMasterBO().setDistributorid(Integer.parseInt(parentId));
                getUserMasterBO().setBranchId(Integer.parseInt(distid));
            }
        } catch (Exception e) {
            Commons.printException("" + e);
            db.close();
        }
    }
    public ArrayList<UserMasterBO> downloadUserList() {
        ArrayList<UserMasterBO> userList = null;
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        try {
            db.createDataBase();
            db.openDataBase();
            String query = "select userid,username from usermaster where isDeviceuser!=1";
            Cursor c = db.selectSQL(query);
            if (c != null) {
                userList = new ArrayList<>();
                UserMasterBO userMasterBO;
                while (c.moveToNext()) {
                    userMasterBO = new UserMasterBO();
                    userMasterBO.setUserid(c.getInt(0));
                    userMasterBO.setUserName(c.getString(1));
                    userList.add(userMasterBO);
                }
                c.close();
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }
        return userList;
    }

    public ArrayList<UserMasterBO> downloadAdHocUserList() {
        ArrayList<UserMasterBO> userList = null;
        String codeChild = "CHILD";
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        try {
            db.createDataBase();
            db.openDataBase();
            String query = "select userid,username from usermaster where isDeviceuser!=1 AND relationship =" + bmodel.QT(codeChild);
            Cursor c = db.selectSQL(query);
            if (c != null) {
                userList = new ArrayList<>();
                UserMasterBO userMasterBO;
                while (c.moveToNext()) {
                    userMasterBO = new UserMasterBO();
                    userMasterBO.setUserid(c.getInt(0));
                    userMasterBO.setUserName(c.getString(1));
                    userList.add(userMasterBO);
                }
                c.close();
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }
        return userList;
    }

    public ArrayList<UserMasterBO> downloadUserList(int distributorId) {
        ArrayList<UserMasterBO> userList = null;
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        try {
            db.createDataBase();
            db.openDataBase();
            String query = "select userid,username from usermaster where distributorid = " + distributorId;
            Cursor c = db.selectSQL(query);
            if (c != null) {
                userList = new ArrayList<>();
                UserMasterBO userMasterBO;
                while (c.moveToNext()) {
                    userMasterBO = new UserMasterBO();
                    userMasterBO.setUserid(c.getInt(0));
                    userMasterBO.setUserName(c.getString(1));
                    userList.add(userMasterBO);
                }
                c.close();
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }
        return userList;
    }
    public ArrayList<UserMasterBO> downloadUserList(String distributorId) {
        ArrayList<UserMasterBO> userList = null;
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        try {

            db.createDataBase();
            db.openDataBase();
            String query = "select userid,username from usermaster where distributorid in (" + distributorId + ")";
            Cursor c = db.selectSQL(query);
            if (c != null) {
                userList = new ArrayList<UserMasterBO>();
                UserMasterBO userMasterBO;
                while (c.moveToNext()) {
                    userMasterBO = new UserMasterBO();
                    userMasterBO.setUserid(c.getInt(0));
                    userMasterBO.setUserName(c.getString(1));
                    userList.add(userMasterBO);

                }
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.closeDB();
        }
        return userList;
    }

    public boolean hasProfileImagePath(UserMasterBO userMasterBO){
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT Value FROM UserEditDetail  where Code='ProfileImagePath' AND UserID=" + userMasterBO.getUserid());
            if (c != null) {
                if (c.getCount() > 0) {
                    if (c.moveToNext()) {
                        userMasterBO.setImagePath(c.getString(0));
                        return true;
                    }
                }
                c.close();
            }
            db.closeDB();
            return false;
        } catch (Exception e) {
            Commons.printException(e);
            return false;
        }
    }

    public void saveUserProfile(UserMasterBO userMasterBO) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            String tid = bmodel.userMasterHelper.getUserMasterBO().getUserid()
                    + "" + bmodel.getRetailerMasterBO().getRetailerID()
                    + "" + SDUtil.now(SDUtil.DATE_TIME_ID);
            deleteQuery(userMasterBO.getUserid());
            String imagePath = "User" + "/" + bmodel.userMasterHelper.getUserMasterBO().getDownloadDate().replace("/", "")
                    + "/"
                    + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                    + "/"+userMasterBO.getImagePath();
            String insertquery = "insert into UserEditDetail (Tid,UserID,Code,Value,Upload)" +
                    "values (" + bmodel.QT(tid)+","+userMasterBO.getUserid()
                    + ",'ProfileImagePath'," + bmodel.QT(imagePath)+ ",'N')";
            db.executeQ(insertquery);


            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);

        }
    }

    private void deleteQuery(int uid) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            db.deleteSQL(DataMembers.tbl_UserEditDetail, " Code =" + bmodel.QT("ProfileImagePath") + "and UserID=" + uid, false);
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }
}
