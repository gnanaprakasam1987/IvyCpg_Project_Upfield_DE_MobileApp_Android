package com.ivy.core.data.user;

import android.database.Cursor;

import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;

import org.mindrot.BCrypt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;

import static com.ivy.sd.png.provider.ConfigurationMasterHelper.CODE_ENABLE_USER_FILTER_DASHBOARD;
import static com.ivy.utils.StringUtils.QT;

public class UserDataManagerImpl implements UserDataManager {

    private DBUtil mDbUtil;

    private AppDataProvider appDataProvider;

    private ConfigurationMasterHelper configurationMasterHelper;

    private static final String CODE_IS_PWD_ENCRYPTED = "ISPWDENC";

    public static final String SPF_PSWD_ENCRYPT_TYPE_MD5 = "MD5";

    @Inject
    public UserDataManagerImpl(@DataBaseInfo DBUtil dbUtil, AppDataProvider appDataProvider, ConfigurationMasterHelper configurationMasterHelper) {
        this.mDbUtil = dbUtil;
        this.appDataProvider = appDataProvider;
        this.configurationMasterHelper = configurationMasterHelper;

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
    public Completable fetchUserDetails() {
        return Completable.fromCallable(new Callable() {
            @Override
            public Void call() {
                try {


                    initDb();
                    Cursor c = mDbUtil.selectSQL("select " + DataMembers.tbl_userMaster_cols
                            + " from Usermaster where isDeviceUser=1");

                    if (c != null) {
                        if (c.moveToLast()) {
                            UserMasterBO userMasterBO = new UserMasterBO();
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


                            appDataProvider.setCurrentUser(userMasterBO);

                        }
                        c.close();
                    }


                } catch (Exception ignored) {
                }

                shutDownDb();
                return null;
            }
        });
    }


    @Override
    public Single<Boolean> isSynced() {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                try {

                    initDb();

                    Cursor c = mDbUtil.selectSQL("select " + DataMembers.tbl_userMaster_cols
                            + " from " + DataMembers.tbl_userMaster
                            + " where isDeviceUser=1");

                    if (c != null) {
                        if (c.getCount() == 0) {
                            c.close();
                            return false;
                        }

                        if (c.moveToNext()) {
                            UserMasterBO userMasterBO = new UserMasterBO();
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

                            appDataProvider.setCurrentUser(userMasterBO);

                        }
                        c.close();

                    }
                    shutDownDb();
                    return true;
                } catch (Exception e) {
                    shutDownDb();
                    return false;
                }
            }
        });
    }

    /**
     *  * Method to use download joinCall users list from usermaster where isDeviceuser = 0 and relationShip = PARENT
     *  * Set the joint call users list inside UserMasterBO
     * @return joinCallUserList
     */
    @Override
    public Completable fetchJoinCallDetails() {
        return Completable.fromCallable(new Callable() {
            @Override
            public Void call() {
                try {

                    initDb();

                    StringBuilder sb = new StringBuilder();

                    sb.append("select " + DataMembers.tbl_userMaster_cols + " from Usermaster where isDeviceUser=0 AND ");
                    if (configurationMasterHelper.userLevel != null && configurationMasterHelper.userLevel.length() > 0)
                        sb.append("userLevel in (").append(configurationMasterHelper.userLevel).append(")");
                    else
                        sb.append("relationship ='PARENT'");

                    Cursor c = mDbUtil.selectSQL(sb.toString());
                    if (c != null) {
                        ArrayList<UserMasterBO> mJoinCallUserlist = new ArrayList<>();
                        while (c.moveToNext()) {
                            UserMasterBO userBO = new UserMasterBO();

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
                        appDataProvider.getUser().setJoinCallUserList(mJoinCallUserlist);
                        c.close();
                    }
                } catch (Exception ignored) {
                }

                shutDownDb();
                return null;
            }
        });
    }

    @Override
    public Completable fetchDistributionDetails() {
        return Completable.fromCallable(new Callable() {
            @Override
            public Void call() {
                try {

                    initDb();

                    String sb = "Select DName,CNumber,Address1,Address2,Address3,TinNo,CSTNo,FaxNo,code,GSTNumber from DistributorMaster " +
                            "where did=" + appDataProvider.getUser().getDistributorid();
                    Cursor c = mDbUtil.selectSQL(sb);
                    if (c.getCount() > 0) {
                        while (c.moveToNext()) {
                            appDataProvider.getUser().setDistributorName(c.getString(0));
                            appDataProvider.getUser().setDistributorContactNumber(c.getString(1));
                            appDataProvider.getUser().setDistributorAddress1(c.getString(2));
                            appDataProvider.getUser().setDistributorAddress2(c.getString(3));
                            appDataProvider.getUser().setDistributorAddress3(c.getString(4));
                            appDataProvider.getUser().setDistributorTinNumber(c.getString(5));
                            appDataProvider.getUser().setCstNo(c.getString(6));
                            appDataProvider.getUser().setFaxNo(c.getString(7));
                            appDataProvider.getUser().setDistributorCode(c.getString(8));
                            appDataProvider.getUser().setGSTNumber(c.getString(9));
                        }
                        c.close();
                    }

                } catch (Exception ignored) {
                }

                shutDownDb();
                return null;
            }
        });
    }

    @Override
    public Completable changeUserPassword(final int userID, final String pwd) {
        initDb();
        return Single.zip(Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                try {

                    StringBuffer sb = new StringBuffer();
                    sb.append("SELECT hhtcode FROM hhtmodulemaster WHERE hhtcode = ");
                    sb.append(QT(CODE_IS_PWD_ENCRYPTED));
                    sb.append(" AND Flag = 1 and ForSwitchSeller = 0");
                    Cursor c = mDbUtil.selectSQL(sb.toString());
                    if (c.getCount() > 0) {
                        if (c.moveToNext()) {
                            return true;
                        }
                    }
                    return false;
                } catch (Exception e) {
                    return false;
                }
            }
        }), Single.fromCallable(new Callable<String>() {
            @Override
            public String call() {
                try {

                    String type = "";
                    Cursor c = mDbUtil.selectSQL("select PwdEncryptType from AppVariables");
                    if (c.getCount() > 0) {
                        if (c.moveToNext()) {
                            type = c.getString(0);
                            if (type.equals("")) {
                                type = SPF_PSWD_ENCRYPT_TYPE_MD5;
                            }
                        }
                        c.close();
                    }
                    return type;
                } catch (Exception e) {
                    return "";
                }
            }
        }), new BiFunction<Boolean, String, PasswordEncryption>() {
            @Override
            public PasswordEncryption apply(Boolean isEncrypted, String encryptionType) {
                PasswordEncryption passwordEncryption = new PasswordEncryption();
                passwordEncryption.setEncrypted(isEncrypted);
                passwordEncryption.setEncryptionType(encryptionType);
                return passwordEncryption;
            }
        }).flatMapCompletable(new Function<PasswordEncryption, Completable>() {
            @Override
            public Completable apply(PasswordEncryption encrypted) {
                try {


                    String password = "";
                    if (encrypted.isEncrypted()) {
                        password = encryptPassword(pwd, encrypted.getEncryptionType());
                    } else {
                        password = pwd;
                    }
                    String query = "Update UserMaster set Password='" + password
                            + "' where userID=" + userID;
                    mDbUtil.executeQ(query);
                    appDataProvider.getUser().setPassword(password);
                    String query1 = "Update AppVariables set PasswordCreatedDate=" + QT(appDataProvider.getUser().getDownloadDate());
                    mDbUtil.executeQ(query1);


                } catch (Exception ignored) {
                }
                shutDownDb();
                return null;
            }
        });
    }

    @Override
    public Completable updateDistributorId(final String distid, final String parentId, final String distname) {
        return Completable.fromCallable(new Callable() {
            @Override
            public Void call() {
                try {


                    initDb();
                    String query = "update userMaster set distributorid=" + parentId
                            + ", branchid=" + distid + ", distributorName='" + distname + "' where userID=" + appDataProvider.getUser().getUserid();

                    mDbUtil.updateSQL(query);

                    appDataProvider.getUser().setDistributorid(SDUtil.convertToInt(parentId));
                    appDataProvider.getUser().setBranchId(SDUtil.convertToInt(distid));

                } catch (Exception ignored) {
                }

                shutDownDb();
                return null;
            }
        });
    }

    @Override
    public Observable<ArrayList<UserMasterBO>> fetchUsers() {

        return Single.fromCallable(new Callable<String>() {
            @Override
            public String call() {
                try {
                    initDb();

                    String filter = "";
                    String sql = "select RField from "
                            + DataMembers.tbl_HhtModuleMaster
                            + " where hhtCode=" + QT(CODE_ENABLE_USER_FILTER_DASHBOARD) + " and Flag=1 and ForSwitchSeller = 0";
                    Cursor c = mDbUtil.selectSQL(sql);
                    if (c != null && c.getCount() != 0) {
                        if (c.moveToNext()) {
                            filter = (c.getString(0).replaceAll("^|$", "'").replaceAll(",", "','"));
                        }
                        c.close();
                    }
                    return filter;
                } catch (Exception e) {
                    return "";
                }
            }
        }).flatMapObservable(new Function<String, Observable<ArrayList<UserMasterBO>>>() {
            @Override
            public Observable<ArrayList<UserMasterBO>> apply(final String dashboardFilter) {
                return Observable.fromCallable(new Callable<ArrayList<UserMasterBO>>() {
                    @Override
                    public ArrayList<UserMasterBO> call() throws Exception {
                        try {
                            String subQuery = "";
                            ArrayList<UserMasterBO> userList = new ArrayList<>();
                            if (configurationMasterHelper.IS_ENABLE_USER_FILTER_DASHBOARD) {
                                if (dashboardFilter.trim().length() > 0) {
                                    subQuery = " and UserLevel in (" + dashboardFilter + ")";
                                }
                            }
                            String query = "select userid,username from usermaster where isDeviceuser!=1" + subQuery;
                            Cursor c = mDbUtil.selectSQL(query);
                            if (c != null) {

                                UserMasterBO userMasterBO;
                                while (c.moveToNext()) {
                                    userMasterBO = new UserMasterBO();
                                    userMasterBO.setUserid(c.getInt(0));
                                    userMasterBO.setUserName(c.getString(1));
                                    userList.add(userMasterBO);
                                }
                                c.close();
                            }
                            shutDownDb();

                            return userList;
                        } catch (Exception ignored) {
                        }

                        shutDownDb();
                        return new ArrayList<>();
                    }
                });
            }
        });
    }

    @Override
    public Observable<ArrayList<UserMasterBO>> fetchAdhocUsers() {
        return Observable.fromCallable(new Callable<ArrayList<UserMasterBO>>() {
            @Override
            public ArrayList<UserMasterBO> call() throws Exception {
                try {

                    initDb();
                    String codeChild = "CHILD";
                    ArrayList<UserMasterBO> userList = new ArrayList<>();
                    String query = "select userid,username from usermaster where isDeviceuser!=1 AND relationship =" + QT(codeChild);
                    Cursor c = mDbUtil.selectSQL(query);
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
                    shutDownDb();

                    return userList;
                } catch (Exception ignored) {
                }

                shutDownDb();
                return new ArrayList<>();
            }
        });
    }

    @Override
    public Observable<ArrayList<UserMasterBO>> fetchUsersForDistributor(final int distributorId) {
        return Observable.fromCallable(new Callable<ArrayList<UserMasterBO>>() {
            @Override
            public ArrayList<UserMasterBO> call() throws Exception {
                try {
                    initDb();

                    ArrayList<UserMasterBO> userList = new ArrayList<>();
                    String query = "select userid,username from usermaster where distributorid = " + distributorId;
                    Cursor c = mDbUtil.selectSQL(query);
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

                    shutDownDb();
                    return userList;
                } catch (Exception ignored) {
                }

                shutDownDb();
                return new ArrayList<>();
            }
        });
    }

    @Override
    public Observable<ArrayList<UserMasterBO>> fetchUsersForDistributors(final String distributorIds) {
        return Observable.fromCallable(new Callable<ArrayList<UserMasterBO>>() {
            @Override
            public ArrayList<UserMasterBO> call() throws Exception {
                try {
                    initDb();

                    ArrayList<UserMasterBO> userList = new ArrayList<>();
                    String query = "select userid,username from usermaster where distributorid in (" + distributorIds + ")";
                    Cursor c = mDbUtil.selectSQL(query);
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

                    shutDownDb();
                    return userList;
                } catch (Exception ignored) {
                }

                shutDownDb();
                return new ArrayList<>();
            }
        });
    }

    @Override
    public Completable updateUserProfile(final UserMasterBO userMasterBO) {
        return Completable.fromCallable(new Callable() {
            @Override
            public Void call() {
                try {

                    initDb();

                    String tid = appDataProvider.getUser().getUserid()
                            + "" + appDataProvider.getRetailMaster().getRetailerID()
                            + "" + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);

                    mDbUtil.deleteSQL(DataMembers.tbl_UserEditDetail, " Code =" + QT("ProfileImagePath") + "and UserID=" + appDataProvider.getUser().getUserid(), false);

                    String imagePath = "User" + "/" + appDataProvider.getUser().getDownloadDate().replace("/", "")
                            + "/"
                            + appDataProvider.getUser().getUserid()
                            + "/" + userMasterBO.getImagePath();
                    String insertquery = "insert into UserEditDetail (Tid,UserID,Code,Value,Upload)" +
                            "values (" + QT(tid) + "," + userMasterBO.getUserid()
                            + ",'ProfileImagePath'," + QT(imagePath) + ",'N')";
                    mDbUtil.executeQ(insertquery);

                } catch (Exception ignored) {
                }
                shutDownDb();
                return null;
            }
        });
    }

    @Override
    public Observable<ArrayList<UserMasterBO>> fetchAllUsers() {
        return Observable.fromCallable(new Callable<ArrayList<UserMasterBO>>() {
            @Override
            public ArrayList<UserMasterBO> call() throws Exception {
                try {
                    initDb();

                    ArrayList<UserMasterBO> userList = new ArrayList<>();
                    String query = "select userid,username from usermaster where isDeviceuser = 1 OR relationship =" + QT("CHILD");
                    Cursor c = mDbUtil.selectSQL(query);
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
                    shutDownDb();

                    return userList;
                } catch (Exception ignored) {
                }

                shutDownDb();
                return new ArrayList<>();
            }
        });
    }

    @Override
    public Observable<ArrayList<UserMasterBO>> fetchParentUsers() {
        return Observable.fromCallable(new Callable<ArrayList<UserMasterBO>>() {
            @Override
            public ArrayList<UserMasterBO> call() throws Exception {
                try {
                    initDb();

                    ArrayList<UserMasterBO> userList = new ArrayList<>();
                    String query = "select userid,username from usermaster where relationship =" + QT("PARENT");
                    Cursor c = mDbUtil.selectSQL(query);
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
                    shutDownDb();

                    return userList;
                } catch (Exception ignored) {
                }

                shutDownDb();
                return new ArrayList<>();
            }
        });
    }

    @Override
    public Observable<ArrayList<UserMasterBO>> fetchChildUsers() {
        return Observable.fromCallable(new Callable<ArrayList<UserMasterBO>>() {
            @Override
            public ArrayList<UserMasterBO> call() throws Exception {
                try {
                    initDb();

                    ArrayList<UserMasterBO> userList = new ArrayList<>();
                    String query = "select userid,username from usermaster where relationship =" + QT("CHILD");
                    Cursor c = mDbUtil.selectSQL(query);
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
                    shutDownDb();

                    return userList;
                } catch (Exception ignored) {
                }

                shutDownDb();
                return new ArrayList<>();
            }
        });
    }

    @Override
    public Observable<ArrayList<UserMasterBO>> fetchPeerUsers() {
        return Observable.fromCallable(new Callable<ArrayList<UserMasterBO>>() {
            @Override
            public ArrayList<UserMasterBO> call() throws Exception {
                try {
                    initDb();

                    ArrayList<UserMasterBO> userList = new ArrayList<>();
                    String query = "select userid,username from usermaster where relationship =" + QT("PEER");
                    Cursor c = mDbUtil.selectSQL(query);
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
                    shutDownDb();

                    return userList;
                } catch (Exception ignored) {
                }

                shutDownDb();
                return new ArrayList<>();
            }
        });
    }

    @Override
    public Observable<HashMap<String, ArrayList<UserMasterBO>>> fetchLinkUsers(int retailerId) {
        return Observable.fromCallable(new Callable<HashMap<String, ArrayList<UserMasterBO>>>() {
            @Override
            public HashMap<String, ArrayList<UserMasterBO>> call() throws Exception {

                try {
                    HashMap<String, ArrayList<UserMasterBO>> linkUserListMap = new HashMap<>();
                    ArrayList<UserMasterBO> linkUserList = new ArrayList<>();
                    initDb();
                    String whereCond = "";

                    if (retailerId != 0)
                        whereCond = " where retailerId =" + retailerId;
                    ArrayList<UserMasterBO> userList = new ArrayList<>();
                    String query = "select userid,username,retailerId from UserRetailerMapping" + whereCond;
                    Cursor c = mDbUtil.selectSQL(query);
                    if (c != null) {
                        userList = new ArrayList<>();
                        UserMasterBO userMasterBO;
                        while (c.moveToNext()) {
                            userMasterBO = new UserMasterBO();
                            userMasterBO.setUserid(c.getInt(0));
                            userMasterBO.setUserName(c.getString(1));
                            userMasterBO.setRetailerID(c.getString(2));

                            if (linkUserListMap.get(c.getString(2)) != null) {
                                ArrayList<UserMasterBO> linkUserList2 = linkUserListMap.get(c.getString(2));
                                linkUserList2.add(userMasterBO);
                            } else {
                                userList = new ArrayList<>();
                                userList.add(userMasterBO);
                                linkUserListMap.put(c.getString(2), userList);
                            }
                        }
                        c.close();
                    }
                    shutDownDb();
                    return linkUserListMap;

                } catch (Exception e) {
                    shutDownDb();
                }
                return new HashMap<>();
            }
        });
    }

    /*@Override
    public Observable<ArrayList<UserMasterBO>> fetchLinkUsers(int retailerId) {
        return Observable.fromCallable(new Callable<ArrayList<UserMasterBO>>() {
            @Override
            public ArrayList<UserMasterBO> call() throws Exception {
                try {
                    initDb();
                    String whereCond = "";

                    if (retailerId != 0)
                        whereCond = " where retailerId =" + retailerId;
                    ArrayList<UserMasterBO> userList = new ArrayList<>();
                    String query = "select userid,username,retailerId from UserRetailerMapping" + whereCond;
                    Cursor c = mDbUtil.selectSQL(query);
                    if (c != null) {
                        userList = new ArrayList<>();
                        UserMasterBO userMasterBO;
                        while (c.moveToNext()) {
                            userMasterBO = new UserMasterBO();
                            userMasterBO.setUserid(c.getInt(0));
                            userMasterBO.setUserName(c.getString(1));
                            userMasterBO.setRetailerID(c.getString(2));
                            userList.add(userMasterBO);
                        }
                        c.close();
                    }
                    shutDownDb();

                    return userList;
                } catch (Exception ignored) {
                }

                shutDownDb();
                return new ArrayList<>();
            }
        });
    }
*/
    @Override
    public Observable<ArrayList<UserMasterBO>> fetchDashboardUsers() {
        return Observable.fromCallable(new Callable<ArrayList<UserMasterBO>>() {
            @Override
            public ArrayList<UserMasterBO> call() {
                try {
                    initDb();

                    ArrayList<UserMasterBO> userList = new ArrayList<>();
                    String query = "select userid,username from usermaster where isDeviceuser!=1 and distributorid!=0";
                    Cursor c = mDbUtil.selectSQL(query);
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
                    shutDownDb();

                    return userList;
                } catch (Exception ignored) {
                }

                shutDownDb();
                return new ArrayList<>();
            }
        });
    }

    @Override
    public Observable<ArrayList<UserMasterBO>> fetchBackupSellers() {
        return Observable.fromCallable(new Callable<ArrayList<UserMasterBO>>() {
            @Override
            public ArrayList<UserMasterBO> call() throws Exception {
                try {
                    initDb();

                    ArrayList<UserMasterBO> userList = new ArrayList<>();
                    String query = "select userid,username from usermaster where relationship =" + QT("CHILD") + " OR relationship = 'ASSOCIATE'";
                    Cursor c = mDbUtil.selectSQL(query);
                    if (c != null) {
                        UserMasterBO userMasterBO;
                        while (c.moveToNext()) {
                            userMasterBO = new UserMasterBO();
                            userMasterBO.setUserid(c.getInt(0));
                            userMasterBO.setUserName(c.getString(1));
                            userMasterBO.setBackup(false);
                            userList.add(userMasterBO);
                        }
                        c.close();
                    }

                    shutDownDb();
                    return userList;
                } catch (Exception ignored) {
                }
                shutDownDb();

                return new ArrayList<>();
            }
        });
    }

    @Override
    public Single<Boolean> hasProfileImagePath(final int userId) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                try {
                    initDb();

                    Cursor c = mDbUtil
                            .selectSQL("SELECT Value FROM UserEditDetail  where Code='ProfileImagePath' AND UserID=" + userId);
                    if (c != null) {
                        if (c.getCount() > 0) {
                            if (c.moveToNext()) {
                                return true;
                            }
                        }
                        c.close();
                    }
                    shutDownDb();
                    return false;
                } catch (Exception e) {
                    shutDownDb();
                    return false;
                }
            }
        });
    }


    private String encryptPassword(String pwd, String encrtptionType) {
        if (encrtptionType.equalsIgnoreCase(SPF_PSWD_ENCRYPT_TYPE_MD5))
            return SDUtil.convertIntoMD5hashAndBase64(pwd);
        else
            return BCrypt.hashpw(pwd, BCrypt.gensalt());
    }

    @Override
    public void tearDown() {
        if (mDbUtil != null)
            mDbUtil.closeDB();
    }

    class PasswordEncryption {
        private boolean isEncrypted;

        private String encryptionType;

        public boolean isEncrypted() {
            return isEncrypted;
        }

        public void setEncrypted(boolean encrypted) {
            isEncrypted = encrypted;
        }

        public String getEncryptionType() {
            return encryptionType;
        }

        public void setEncryptionType(String encryptionType) {
            this.encryptionType = encryptionType;
        }
    }

}
