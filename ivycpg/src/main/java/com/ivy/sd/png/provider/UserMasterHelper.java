package com.ivy.sd.png.provider;

import android.content.Context;
import android.database.Cursor;

import com.ivy.core.data.app.AppDataProviderImpl;
import com.ivy.core.data.user.UserDataManagerImpl;
import com.ivy.cpg.view.login.LoginHelper;
import com.ivy.cpg.view.supervisor.mvp.models.ManagerialBO;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserMasterHelper {

    private static UserMasterHelper instance = null;
    private final Context context;
    private final BusinessModel bmodel;
    private UserMasterBO userMasterBO;
    private ArrayList<UserMasterBO> backupSellerList;
    private ArrayList<ManagerialBO> userHierarchyList;
    private ManagerialBO mPrevSelectedItem;

    public UserMasterHelper(Context context) {
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

    /**
     * @See {@link UserDataManagerImpl#fetchUserDetails()}
     * @deprecated
     */
    public void downloadUserDetails() {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
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
                    userMasterBO.setBackupSellerID(c.getString(c
                            .getColumnIndex("BackupUserId")));
                    userMasterBO.setBackup(false);
                    userMasterBO.setUserlevelId(c.getString(c
                            .getColumnIndex("UserLevelId")));
                    userMasterBO.setUserPositionId(c.getString(c
                            .getColumnIndex("UserPositionId")));


                    bmodel.codeCleanUpUtil.setUserData(userMasterBO);

                }
                c.close();
            }
            db.closeDB();
            downloadManagerialUsers();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    /**
     * @return <code>true</code> if data is synced <code>false</code> if not synced
     * @see {@link UserDataManagerImpl#isSynced()}
     * Checks if data is synced
     * @deprecated
     */

    public boolean getSyncStatus() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
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

                    /** Code cleanup data**/
                    bmodel.codeCleanUpUtil.setUserData(userMasterBO);
                }
                c.close();
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        if (db != null) db.closeDB();
        return true;
    }

    /**
     *
     */

    /**
     * @see {@link UserDataManagerImpl#fetchJoinCallDetails()}
     * Method to use download joinCall users list from usermaster where isDeviceuser = 0 and relationShip = PARENT
     * Set the joint call users list inside UserMasterBO
     * @deprecated
     */
    public void downloadJoinCallusers() {
        ArrayList<UserMasterBO> mJoinCallUserlist = new ArrayList<>();
        UserMasterBO userBO;

        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            StringBuilder sb = new StringBuilder();

            sb.append("select " + DataMembers.tbl_userMaster_cols + " from Usermaster where isDeviceUser=0 AND ");
            if (bmodel.configurationMasterHelper.userLevel != null && bmodel.configurationMasterHelper.userLevel.length() > 0)
                sb.append("userLevel in (" + bmodel.configurationMasterHelper.userLevel + ")");
            else
                sb.append("relationship ='PARENT'");

            Cursor c = db.selectSQL(sb.toString());
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

                bmodel.codeCleanUpUtil.setUserData(userMasterBO);
                c.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }


    /**
     * @return User BO
     * @See {@link AppDataProviderImpl#getUser()}
     * @deprecated Only the necessary data is stored in the {@link com.ivy.core.data.app.AppDataProviderImpl} Singleton
     */
    public UserMasterBO getUserMasterBO() {
        return userMasterBO;
    }

    /**
     * @return User BO
     * @See {@link AppDataProviderImpl#setCurrentUser(UserMasterBO)}
     * @deprecated Only the necessary data is stored in the {@link com.ivy.core.data.app.AppDataProviderImpl} Singleton
     */
    public void setUserMasterBO(UserMasterBO userMasterBO) {
        bmodel.codeCleanUpUtil.setUserData(userMasterBO);
        this.userMasterBO = userMasterBO;
    }


    /**
     * @See {@link UserDataManagerImpl#fetchDistributionDetails()} ()}
     * @deprecated
     */
    public void downloadDistributionDetails() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
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

                bmodel.codeCleanUpUtil.setUserData(userMasterBO);
                c.close();
            } else
                c.close();
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
     * @see {@link UserDataManagerImpl#changeUserPassword(int, String)}
     * @deprecated
     */
    public boolean changePassword(int UserID, String pwd) {
        LoginHelper.getInstance(context).loadPasswordConfiguration(context);
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        try {
            db.createDataBase();
            db.openDataBase();

            String password = "";
            if (LoginHelper.getInstance(context).IS_PASSWORD_ENCRYPTED) {
                password = bmodel.synchronizationHelper.encryptPassword(pwd);
            } else {
                password = pwd;
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

    /**
     * @param distid
     * @param parentId
     * @param distname
     * @See {@link UserDataManagerImpl#updateDistributorId(String, String, String)}
     * @deprecated
     */
    public void updateDistributorId(String distid, String parentId, String distname) {

        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        try {
            db.createDataBase();
            db.openDataBase();

            String query = "update userMaster set distributorid=" + parentId
                    + ", branchid=" + distid + ", distributorName='" + distname + "' where userID=" + userMasterBO.getUserid();

            db.executeQ(query);
            db.close();
            if (getUserMasterBO() != null) {
                getUserMasterBO().setDistributorid(SDUtil.convertToInt(parentId));
                getUserMasterBO().setBranchId(SDUtil.convertToInt(distid));
            }
        } catch (Exception e) {
            Commons.printException("" + e);
            db.close();
        }
    }


    /**
     * @See {@link UserDataManagerImpl#fetchUsers()}
     * @deprecated
     */
    /**
     * @See {@link  com.ivy.ui.profile.data.ProfileDataManagerImpl#downloadAllUser;}
     * @since CPG134 replaced by {@link com.ivy.ui.profile.data.ProfileDataManagerImpl#downloadAllUser}
     * Will be removed from @version CPG134 Release
     * @deprecated This has been Migrated to MVP pattern
     */
    public ArrayList<UserMasterBO> downloadUserList() {
        ArrayList<UserMasterBO> userList = null;
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        try {
            db.createDataBase();
            db.openDataBase();

            String subQuery = "";
            if (bmodel.configurationMasterHelper.IS_ENABLE_USER_FILTER_DASHBOARD) {
                if (bmodel.getDashboardUserFilterString().trim().length() > 0) {
                    subQuery = " and UserLevel in (" + bmodel.getDashboardUserFilterString() + ")";
                }
            }
            String query = "select userid,username from usermaster where isDeviceuser!=1" + subQuery;
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

    /**
     * @return
     * @See {@link UserDataManagerImpl#fetchAdhocUsers()}
     * @deprecated
     */
    public ArrayList<UserMasterBO> downloadAdHocUserList() {
        ArrayList<UserMasterBO> userList = null;
        String codeChild = "CHILD";
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
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

    /**
     * @param distributorId
     * @return
     * @See {@link UserDataManagerImpl#fetchUsersForDistributor(int)}
     * @deprecated
     */
    public ArrayList<UserMasterBO> downloadUserList(int distributorId) {
        ArrayList<UserMasterBO> userList = null;
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
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

    /**
     * @param distributorId
     * @return
     * @See {@link UserDataManagerImpl#fetchUsersForDistributors(String)}
     * @deprecated
     */
    public ArrayList<UserMasterBO> downloadUserList(String distributorId) {
        ArrayList<UserMasterBO> userList = null;
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
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

    public boolean hasProfileImageSetLocally(UserMasterBO userMasterBO) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT Value FROM UserEditDetail  where Code='ProfileImagePath' AND " +
                            "UserID=" + userMasterBO.getUserid());
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

    /**
     * @param userMasterBO
     * @See {@link UserDataManagerImpl#updateUserProfile(UserMasterBO)}
     * @deprecated
     */
    public void saveUserProfile(UserMasterBO userMasterBO) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            String tid = bmodel.userMasterHelper.getUserMasterBO().getUserid()
                    + "" + bmodel.getRetailerMasterBO().getRetailerID()
                    + "" + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);
            deleteQuery(userMasterBO.getUserid());
            String imagePath = "User" + "/" + bmodel.userMasterHelper.getUserMasterBO().getDownloadDate().replace("/", "")
                    + "/"
                    + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                    + "/" + userMasterBO.getImagePath();
            String insertquery = "insert into UserEditDetail (Tid,UserID,Code,Value,Upload)" +
                    "values (" + bmodel.QT(tid) + "," + userMasterBO.getUserid()
                    + ",'ProfileImagePath'," + bmodel.QT(imagePath) + ",'N')";
            db.executeQ(insertquery);


            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);

        }
    }

    private void deleteQuery(int uid) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            db.deleteSQL(DataMembers.tbl_UserEditDetail, " Code =" + bmodel.QT("ProfileImagePath") + "and UserID=" + uid, false);
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    /**
     * @return Current logged in user along with list of his child users
     * @See {@link UserDataManagerImpl#fetchAllUsers()}
     * @deprecated
     */
    public ArrayList<UserMasterBO> downloadAllUser() {
        ArrayList<UserMasterBO> userList = null;
        String codeChild = "CHILD";
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        try {
            db.createDataBase();
            db.openDataBase();
            String query = "select userid,username from usermaster where isDeviceuser = 1 OR relationship =" + bmodel.QT(codeChild);
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

    /**
     * @See {@link UserDataManagerImpl#fetchBackupSellers()}
     * @deprecated
     */
    public ArrayList<UserMasterBO> getBackupSellerList() {
        return backupSellerList;
    }

    public void setBackupSellerList(ArrayList<UserMasterBO> backupSellerList) {
        this.backupSellerList = backupSellerList;
    }

    /**
     * @See {@link UserDataManagerImpl#fetchBackupSellers()}
     * @deprecated
     */
    public void downloadBackupSeller() {
        String codeChild = "CHILD";
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        try {
            db.createDataBase();
            db.openDataBase();
            String query = "select userid,username from usermaster where relationship =" + bmodel.QT(codeChild) + " OR relationship = 'ASSOCIATE'";
            Cursor c = db.selectSQL(query);
            setBackupSellerList(new ArrayList<UserMasterBO>());
            if (c != null) {
                UserMasterBO userMasterBO;
                while (c.moveToNext()) {
                    userMasterBO = new UserMasterBO();
                    userMasterBO.setUserid(c.getInt(0));
                    userMasterBO.setUserName(c.getString(1));
                    userMasterBO.setBackup(false);
                    getBackupSellerList().add(userMasterBO);
                }
                c.close();
            }
        } catch (Exception e) {
            Commons.printException(e);
        } finally {
            db.closeDB();
        }
    }

    public ArrayList<ManagerialBO> getUserHierarchyList() {
        return userHierarchyList;
    }

    public void setUserHierarchyList(ArrayList<ManagerialBO> userHierarchyList) {
        this.userHierarchyList = userHierarchyList;
    }

    public void downloadManagerialUsers() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
        try {
            db.createDataBase();
            db.openDataBase();
            String query = "select UM.userpositionid,UM.username,UM.userlevelid,UM.Parentid" +
                    ",(select userlevelname from userhierarchy where userlevelid = UM.userlevelid) as userlevelname" +
                    " from UserMaster UM where UM.userlevelid >" + bmodel.getAppDataProvider().getUser().getUserlevelId() + " order by UM.userlevelid";
            Cursor c = db.selectSQL(query);
            ArrayList<ManagerialBO> userList = new ArrayList<>();
            if (c != null) {

                int level = -1;
                String tempLevel = "";

                while (c.moveToNext()) {
                    if (!tempLevel.equals(c.getString(2)))
                        level++;
                    ManagerialBO managerialBO = new ManagerialBO(level);
                    managerialBO.setLevelId(c.getString(2));
                    managerialBO.setUserId(c.getString(0));
                    managerialBO.setUserLevel(c.getString(1) + (c.getString(4) == null ? "" : " - " + c.getString(4)));
                    managerialBO.setParentId(c.getString(3));
                    userList.add(managerialBO);

                    tempLevel = c.getString(2);
                }
                c.close();
            }
            createTree(userList);
        } catch (Exception e) {
            Commons.printException(e);
        } finally {
            db.closeDB();
        }
    }

    public ManagerialBO getmPrevSelectedItem() {
        return mPrevSelectedItem;
    }

    public void setmPrevSelectedItem(ManagerialBO mPrevSelectedItem) {
        this.mPrevSelectedItem = mPrevSelectedItem;
    }

    private void createTree(List<ManagerialBO> nodes) {

        Map<String, ArrayList<ManagerialBO>> mapTmp = new HashMap<>();
        ArrayList<ManagerialBO> userHierarchy = new ArrayList<>();

        for (ManagerialBO current : nodes) {
            mapTmp.put(current.getParentId(), new ArrayList<>());
        }

        for (String parent : mapTmp.keySet()) {
            for (ManagerialBO current : nodes) {
                if (parent.equals(current.getParentId()))
                    mapTmp.get(parent).add(current);
            }
        }

        for (ManagerialBO current : nodes) {
            if (mapTmp.containsKey(current.getUserId()) && mapTmp.get(current.getUserId()) != null && current.getLevel() <= 8)
                current.addChildren(mapTmp.get(current.getUserId()));
        }

        for (ManagerialBO managerialBO : nodes) {
            if (managerialBO.getChildren() == null || managerialBO.getChildren().isEmpty())
                managerialBO.setChild(true);
            else
                managerialBO.setChild(false);

            if (bmodel.getAppDataProvider().getUser().getUserPositionId().equals(managerialBO.getParentId()))
                userHierarchy.add(managerialBO);
        }

        setUserHierarchyList(userHierarchy);

    }
}
