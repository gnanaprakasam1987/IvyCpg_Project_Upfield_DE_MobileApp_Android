package com.ivy.core.data.user;

import com.ivy.sd.png.bo.UserMasterBO;

import io.reactivex.Completable;
import io.reactivex.Single;

public interface UserDataManager {

    Completable fetchUserDetails();

    Single<Boolean> isSynced();

    Completable fetchJoinCallDetails();

    Completable fetchDistributionDetails();

    Completable changeUserPassword(int UserID, String pwd);

    Completable updateDistributorId(String distid, String parentId, String distname);

    Single<Boolean> isPasswordEncrypted();
}
