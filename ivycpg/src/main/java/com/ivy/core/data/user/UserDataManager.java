package com.ivy.core.data.user;

import com.ivy.core.data.AppDataManagerContract;
import com.ivy.sd.png.bo.UserMasterBO;

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

public interface UserDataManager extends AppDataManagerContract {

    Completable fetchUserDetails();

    Single<Boolean> isSynced();

    Completable fetchJoinCallDetails();

    Completable fetchDistributionDetails();

    Completable changeUserPassword(int UserID, String pwd);

    Completable updateDistributorId(String distid, String parentId, String distname);

    Observable<ArrayList<UserMasterBO>> fetchUsers();

    Observable<ArrayList<UserMasterBO>> fetchAdhocUsers();

    Observable<ArrayList<UserMasterBO>> fetchUsersForDistributor(int distributorId);

    Observable<ArrayList<UserMasterBO>> fetchUsersForDistributors(String distributorIds);

    Completable updateUserProfile(UserMasterBO userMasterBO);

    Observable<ArrayList<UserMasterBO>> fetchAllUsers();

    Observable<ArrayList<UserMasterBO>> fetchParentUsers();

    Observable<ArrayList<UserMasterBO>> fetchChildUsers();

    Observable<ArrayList<UserMasterBO>> fetchPeerUsers();

    Observable<HashMap<String, ArrayList<UserMasterBO>>> fetchLinkUsers(int retailerId);

    Observable<ArrayList<UserMasterBO>> fetchDashboardUsers();

    Observable<ArrayList<UserMasterBO>> fetchBackupSellers();

    Single<Boolean> hasProfileImagePath(int userId);

}
