package com.ivy.core.data.user;

import android.database.Cursor;

import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.DataMembers;

import org.mindrot.BCrypt;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;

import static com.ivy.utils.AppUtils.QT;
import static io.reactivex.Single.*;

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

    @Override
    public Completable fetchUserDetails() {
        return Completable.fromCallable(new Callable() {
            @Override
            public Void call() {
                try {
                    mDbUtil.createDataBase();
                    mDbUtil.openDataBase();

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
                    mDbUtil.closeDB();


                } catch (Exception ignored) {
                } finally {
                    if (mDbUtil != null)
                        mDbUtil.closeDB();
                }

                return null;
            }
        });
    }


    @Override
    public Single<Boolean> isSynced() {
        return fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                try {
                    mDbUtil.createDataBase();
                    mDbUtil.openDataBase();

                    Cursor c = mDbUtil.selectSQL("select " + DataMembers.tbl_userMaster_cols
                            + " from " + DataMembers.tbl_userMaster
                            + " where isDeviceUser=1");

                    if (c != null) {
                        if (c.getCount() == 0) {
                            c.close();
                            mDbUtil.closeDB();
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
                    return true;
                } catch (Exception e) {
                    return false;
                } finally {
                    if (mDbUtil != null)
                        mDbUtil.closeDB();
                }
            }
        });
    }

    @Override
    public Completable fetchJoinCallDetails() {
        return Completable.fromCallable(new Callable() {
            @Override
            public Void call() {
                try {
                    mDbUtil.createDataBase();
                    mDbUtil.openDataBase();

                    StringBuilder sb = new StringBuilder();

                    sb.append("select " + DataMembers.tbl_userMaster_cols + " from Usermaster where isDeviceUser=0 AND ");
                    if (configurationMasterHelper.userLevel != null && configurationMasterHelper.userLevel.length() > 0)
                        sb.append("userLevel in (").append(configurationMasterHelper.userLevel).append(")");
                    else
                        sb.append("relationship !='CHILD'");

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
                } finally {
                    if (mDbUtil != null)
                        mDbUtil.closeDB();
                }

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
                    mDbUtil.createDataBase();
                    mDbUtil.openDataBase();

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

                } catch (Exception e) {
                } finally {
                    if (mDbUtil != null)
                        mDbUtil.closeDB();
                }

                return null;
            }
        });
    }

    @Override
    public Completable changeUserPassword(final int UserID, final String pwd) {
        return zip(fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                try {
                    mDbUtil.createDataBase();
                    mDbUtil.openDataBase();
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
                } finally {
                    if (mDbUtil != null)
                        mDbUtil.closeDB();
                }
            }
        }), fromCallable(new Callable<String>() {
            @Override
            public String call() {
                try {
                    mDbUtil.createDataBase();
                    mDbUtil.openDataBase();
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
                } finally {
                    if (mDbUtil != null)
                        mDbUtil.closeDB();
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
                    mDbUtil.createDataBase();
                    mDbUtil.openDataBase();

                    String password = "";
                    if (encrypted.isEncrypted()) {
                        password = encryptPassword(pwd, encrypted.getEncryptionType());
                    } else {
                        password = pwd;
                    }
                    String query = "Update UserMaster set Password='" + password
                            + "' where userID=" + UserID;
                    mDbUtil.executeQ(query);
                    appDataProvider.getUser().setPassword(password);
                    String query1 = "Update AppVariables set PasswordCreatedDate=" + QT(appDataProvider.getUser().getDownloadDate());
                    mDbUtil.executeQ(query1);


                } catch (Exception ignored) {
                } finally {
                    if (mDbUtil != null)
                        mDbUtil.closeDB();
                }

                return null;
            }


        });



    }

    @Override
    public Completable updateDistributorId(String distid, String parentId, String distname) {
        return null;
    }

    @Override
    public Single<Boolean> isPasswordEncrypted() {
        return null;
    }

    private String encryptPassword(String pwd, String encrtptionType) {
        if (encrtptionType.equalsIgnoreCase(SPF_PSWD_ENCRYPT_TYPE_MD5))
            return SDUtil.convertIntoMD5hashAndBase64(pwd);
        else
            return BCrypt.hashpw(pwd, BCrypt.gensalt());
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
