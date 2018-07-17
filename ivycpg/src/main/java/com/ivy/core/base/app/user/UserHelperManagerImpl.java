package com.ivy.core.base.app.user;


import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;



import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

public class UserHelperManagerImpl implements UserHelperManager {


    public Observable<UserMasterBO> downloadUserDetails(final Context context) {
        return Observable.create(new ObservableOnSubscribe<UserMasterBO>() {
            @Override
            public void subscribe(final ObservableEmitter<UserMasterBO> subscriber) throws Exception {
                UserMasterBO userMasterBO = null;
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
                            userMasterBO.setBackupSellerID(c.getString(c
                                    .getColumnIndex("BackupUserId")));
                            userMasterBO.setBackup(false);

                        }
                        c.close();
                    }
                    db.closeDB();
                    subscriber.onNext(userMasterBO);
                } catch (Exception e) {
                    subscriber.onError(new Throwable(e.getMessage()));
                    Commons.printException("" + e);
                }
            }

        });

    }


}
