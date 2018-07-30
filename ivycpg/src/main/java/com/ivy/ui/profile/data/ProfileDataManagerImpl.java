package com.ivy.ui.profile.data;


        import android.database.Cursor;
        import android.util.SparseArray;


        import com.ivy.core.di.scope.DataBaseInfo;
        import com.ivy.lib.existing.DBUtil;
        import com.ivy.sd.png.bo.ChannelBO;
        import com.ivy.sd.png.bo.LocationBO;
        import com.ivy.sd.png.bo.NewOutletBO;
        import com.ivy.sd.png.bo.RetailerFlexBO;
        import com.ivy.sd.png.bo.RetailerMasterBO;
        import com.ivy.sd.png.bo.StandardListBO;
        import com.ivy.sd.png.commons.SDUtil;
        import com.ivy.sd.png.util.Commons;
        import com.ivy.sd.png.util.DataMembers;
        import com.ivy.utils.AppUtils;

        import java.util.ArrayList;
        import java.util.HashMap;
        import java.util.LinkedHashMap;
        import java.util.Vector;
        import java.util.concurrent.Callable;

        import javax.inject.Inject;

        import io.reactivex.Observable;
        import io.reactivex.ObservableSource;
        import io.reactivex.Single;
        import io.reactivex.functions.Function;

public class ProfileDataManagerImpl implements IProfileDataManager {

    DBUtil dbUtil;

    @Inject
    public ProfileDataManagerImpl(@DataBaseInfo DBUtil dbUtil) {
        this.dbUtil = dbUtil;
    }

    @Override
    public Observable<ArrayList<NewOutletBO>> getContactTitle() {
        return Observable.fromCallable(new Callable<ArrayList<NewOutletBO>>() {
            @Override
            public ArrayList<NewOutletBO> call() throws Exception {
                ArrayList<NewOutletBO> contactTitleList = new ArrayList<>();
                NewOutletBO contactTitle = null;
                try {
                    dbUtil.openDataBase();
                    Cursor c = dbUtil.selectSQL("SELECT ListId,ListCode,ListName from StandardListMaster where ListType='RETAILER_CONTACT_TITLE_TYPE'");
                    if (c.getCount() > 0) {
                        while (c.moveToNext()) {
                            contactTitle = new NewOutletBO();
                            contactTitle.setListId(c.getInt(0));
                            contactTitle.setListName(c.getString(2));
                            contactTitleList.add(contactTitle);
                        }
                        c.close();
                    }
                    dbUtil.closeDB();
                } catch (Exception e) {
                    Commons.printException("" + e);
                }
                return contactTitleList;
            }
        });

    }

    @Override
    public Observable<ArrayList<NewOutletBO>> getContactStatus() {
        return Observable.fromCallable(new Callable<ArrayList<NewOutletBO>>() {
            @Override
            public ArrayList<NewOutletBO> call() throws Exception {
                ArrayList<NewOutletBO> contractStatusList = new ArrayList<>();
                NewOutletBO contactStatus;
                try {
                    dbUtil.openDataBase();
                    Cursor c = dbUtil.selectSQL("SELECT ListId,ListCode,ListName from StandardListMaster where ListType='RETAILER_CONTRACT_STATUS'");
                    if (c.getCount() > 0) {
                        while (c.moveToNext()) {
                            contactStatus = new NewOutletBO();
                            contactStatus.setListId(c.getInt(0));
                            contactStatus.setListName(c.getString(2));
                            contractStatusList.add(contactStatus);
                        }
                        c.close();
                    }
                    dbUtil.closeDB();
                } catch (Exception e) {
                    Commons.printException("" + e);
                }
                return contractStatusList;
            }
        });

    }

    @Override
    public Observable<LinkedHashMap<Integer, ArrayList<LocationBO>>> getLocationListByLevId() {

        return Observable.fromCallable(new Callable<LinkedHashMap<Integer, ArrayList<LocationBO>>>() {
            @Override
            public LinkedHashMap<Integer, ArrayList<LocationBO>> call() throws Exception {

                LinkedHashMap<Integer, ArrayList<LocationBO>> mLocationListByLevelId = new LinkedHashMap<>();
                LocationBO locationBo;
                try {
                    dbUtil.createDataBase();
                    dbUtil.openDataBase();
                    String sb = "select LM.LocId,LM.LocCode,LM.LocName,LM.LocParentId,LL.id  from LocationMaster LM " +
                            "inner join  (select distinct id from LocationLevel LL1 inner join LocationMaster LM1" +
                            " on  LL1.id=LM1.LocLevelId order by Sequence desc  limit 3) LL " +
                            "on LL.id=LM.LocLevelId";
                    Cursor c = dbUtil.selectSQL(sb);
                    if (c != null) {
                        if (c.getCount() > 0) {
                            ArrayList<LocationBO> locationList = new ArrayList<>();
                            int levelId = 0;
                            while (c.moveToNext()) {
                                locationBo = new LocationBO();
                                locationBo.setLocId(c.getInt(0));
                                locationBo.setLocCode(c.getString(1));
                                locationBo.setLocName(c.getString(2));
                                locationBo.setParentId(c.getInt(3));

                                if (levelId != c.getInt(4)) {
                                    if (levelId != 0) {
                                        mLocationListByLevelId.put(levelId, locationList);
                                        locationList = new ArrayList<>();
                                        locationList.add(locationBo);
                                        levelId = c.getInt(4);

                                    } else {
                                        locationList.add(locationBo);
                                        levelId = c.getInt(4);
                                    }
                                } else {
                                    locationList.add(locationBo);
                                }
                            }
                            if (locationList.size() > 0) {
                                mLocationListByLevelId.put(levelId, locationList);
                            }
                        }
                        c.close();
                    }

                } catch (Exception e) {
                    Commons.printException("" + e);

                } finally {
                    dbUtil.closeDB();
                }
                return mLocationListByLevelId;
            }
        });
    }

    @Override
    public Observable<HashMap<String, String>> getPreviousProfileChanges(final String RetailerID) {

        return Observable.fromCallable(new Callable<HashMap<String, String>>() {
            @Override
            public HashMap<String, String> call() throws Exception {

                HashMap<String, String> mPreviousProfileChangesList = new HashMap<>();
                try {
                    dbUtil.openDataBase();
                    Cursor c, headerCursor;
                    String tid = "";
                    String currentDate;
                    currentDate = SDUtil.now(SDUtil.DATE_GLOBAL);
                    headerCursor = dbUtil.selectSQL("SELECT Tid FROM RetailerEditHeader" + " WHERE RetailerId = "
                            + RetailerID + " AND Date = " + AppUtils.QT(currentDate) + " AND Upload = " + AppUtils.QT("N"));
                    if (headerCursor.getCount() > 0) {
                        headerCursor.moveToNext();
                        tid = headerCursor.getString(0);
                        headerCursor.close();
                    }
                    c = dbUtil.selectSQL("select code, value from RetailerEditDetail RED INNER JOIN RetailerEditHeader REH ON REH.tid=RED.tid where REH.retailerid="
                            + RetailerID + " and REH.tid=" + AppUtils.QT(tid));
                    if (c != null) {
                        while (c.moveToNext()) {
                            mPreviousProfileChangesList.put(c.getString(0), c.getString(1));
                        }
                    }
                    dbUtil.closeDB();
                } catch (Exception e) {
                    Commons.printException("" + e);
                    dbUtil.closeDB();
                }

                return mPreviousProfileChangesList;
            }
        });
    }


    @Override
    public Single<Boolean> checkProfileImagePath(final RetailerMasterBO ret) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                dbUtil.openDataBase();
                Cursor c = dbUtil
                        .selectSQL("SELECT value FROM RetailerEditDetail  where code='PROFILE60' AND retailerid=" +ret.getRetailerID());
                if (c != null) {
                    if (c.getCount() > 0) {
                        if (c.moveToNext()) {
                            ret.setProfileImagePath(c.getString(0));
                            return true;
                        }
                    }
                    c.close();
                }
                dbUtil.closeDB();
                return false;
            }
        });
    }


    /*In the old code this method will return tow array list
    but now it will return only mLinkRetailerList
    by this we need can get other data list ie mLinkRetailerListByDistributorId. Need to do in ProfileEditPresenterImp*/
    @Override
    public Observable<Vector<RetailerMasterBO>> downloadLinkRetailer() {

        return Observable.fromCallable(new Callable<Vector<RetailerMasterBO>>() {
            @Override
            public Vector<RetailerMasterBO> call() throws Exception {
                Vector<RetailerMasterBO> mLinkRetailerList = new Vector<>();
                try {
                    dbUtil.openDataBase();
                    String sb = "select Distributorid ,retailerid,name,latitude,longitude,pincode from linkretailermaster " +
                            "order by Distributorid ";
                    Cursor c = dbUtil.selectSQL(sb);
                    RetailerMasterBO linkRetailerBO;
                    if (c.getCount() > 0) {
                        while (c.moveToNext()) {
                            linkRetailerBO = new RetailerMasterBO();
                            linkRetailerBO.setDistributorId(c.getInt(0));
                            linkRetailerBO.setRetailerID(c.getString(1));
                            linkRetailerBO.setRetailerName(c.getString(2));
                            linkRetailerBO.setLatitude(c.getDouble(3));
                            linkRetailerBO.setLongitude(c.getDouble(4));
                            linkRetailerBO.setPincode(c.getString(5));
                            mLinkRetailerList.add(linkRetailerBO);
                        }
                    }
                    c.close();
                    dbUtil.closeDB();
                } catch (Exception e) {
                    Commons.printException("" + e);
                }
                return mLinkRetailerList;
            }
        });
    }

    @Override
    public Observable<ArrayList<RetailerFlexBO>> downloadRetailerFlexValues(final String type) {

        return Observable.fromCallable(new Callable<ArrayList<RetailerFlexBO>>() {
            @Override
            public ArrayList<RetailerFlexBO> call() throws Exception {
                ArrayList<RetailerFlexBO> flexValues = new ArrayList<>();
                try {
                    dbUtil.openDataBase();
                    String sql = "select id,name from RetailerFlexValues where type = " + AppUtils.QT(type);
                    Cursor c = dbUtil.selectSQL(sql);
                    RetailerFlexBO retailerFlexBO;
                    if (c.getCount() > 0) {
                        while (c.moveToNext()) {
                            retailerFlexBO = new RetailerFlexBO();
                            retailerFlexBO.setId(c.getString(0));
                            retailerFlexBO.setName(c.getString(1));
                            flexValues.add(retailerFlexBO);
                        }
                    }

                } catch (Exception e) {
                    Commons.printException(e);
                } finally {
                    dbUtil.closeDB();
                }
                return flexValues;
            }
        });

    }


    @Override
    public void saveNearByRetailers(String id,Vector<RetailerMasterBO> NearByRetailers) {
        try {
            dbUtil.createDataBase();
            dbUtil.openDataBase();
            String columnsNew = "rid,nearbyrid,upload";
            String values;
            for (int j = 0; j < NearByRetailers.size(); j++) {
                values = AppUtils.QT(id) + "," + NearByRetailers.get(j).getRetailerID() + "," + AppUtils.QT("N");
                dbUtil.insertSQL("NearByRetailers", columnsNew, values);
            }
            dbUtil.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }

    }


    @Override
    public Observable<ArrayList<String>> getNearbyRetailerIds(final String RetailerID) {
        return Observable.fromCallable(new Callable<ArrayList<String>>() {
            @Override
            public ArrayList<String> call() throws Exception {
                ArrayList<String> lst = new ArrayList<>();
                try {
                    dbUtil.openDataBase();
                    Cursor c = dbUtil
                            .selectSQL("SELECT nearbyrid from NearByRetailers where rid='" +RetailerID+"' and upload='Y'");
                    if (c.getCount() > 0) {
                        while (c.moveToNext()) {
                            lst.add(c.getString(0));
                        }
                    }
                    c.close();
                    dbUtil.closeDB();
                } catch (Exception e) {
                    Commons.printException("" + e);
                }
                return lst;
            }
        });
    }

    @Override
    public Observable<HashMap<String, String>> getNearbyRetailersEditRequest(final String retailerId) {
        return Observable.fromCallable(new Callable<HashMap<String, String>>() {
            @Override
            public HashMap<String, String> call() throws Exception {
                HashMap<String, String> lstEditRequests = new HashMap<>();
                try {
                    dbUtil.openDataBase();
                    Cursor c = dbUtil
                            .selectSQL("SELECT nearbyrid,status from RrtNearByEditRequest where rid=" + retailerId + " and upload='N'");
                    if (c.getCount() > 0) {
                        while (c.moveToNext()) {
                            lstEditRequests.put(c.getString(0), c.getString(1));
                        }

                    }
                    c.close();
                    dbUtil.closeDB();
                } catch (Exception e) {
                    Commons.printException("" + e);
                }
                return lstEditRequests;
            }
        });
    }

    @Override
    public Observable<ArrayList<StandardListBO>> downloadPriorityProducts() {
        return Observable.fromCallable(new Callable<ArrayList<StandardListBO>>() {
            @Override
            public ArrayList<StandardListBO> call() throws Exception {
                ArrayList<StandardListBO> priorityproductList = null;
                try {
                    dbUtil.openDataBase();
                    String sb = "select  priorityproductid,pname,ProductLevelId from PriorityProducts  pp inner join productmaster pm " +
                            " on pm.pid=pp.priorityproductid  and pm.plid=pp.productlevelid";
                    Cursor c = dbUtil.selectSQL(sb);
                    if (c.getCount() > 0) {
                        priorityproductList = new ArrayList<>();
                        StandardListBO standardListBO;
                        while (c.moveToNext()) {
                            standardListBO = new StandardListBO();
                            standardListBO.setListID(c.getString(0));
                            standardListBO.setListName(c.getString(1));
                            standardListBO.setListCode(c.getString(2));
                            priorityproductList.add(standardListBO);
                        }
                    }
                } catch (Exception e) {
                    Commons.printException("" + e);
                } finally {
                    dbUtil.closeDB();
                }
                return priorityproductList;
            }
        });
    }

    @Override
    public Observable<ArrayList<String>> downloadPriorityProductsForRetailer(final String retailerId) {
        return Observable.fromCallable(new Callable<ArrayList<String>>() {
            @Override
            public ArrayList<String> call() throws Exception {
                ArrayList<String> priorityproductList = null;
                try {
                    dbUtil.openDataBase();
                    String sql = "select  ProductId from RetailerPriorityProducts where retailerId=" + AppUtils.QT(retailerId);
                    Cursor c = dbUtil.selectSQL(sql);
                    if (c.getCount() > 0) {
                        priorityproductList = new ArrayList<>();
                        while (c.moveToNext()) {
                            priorityproductList.add(c.getString(0));
                        }
                    }
                } catch (Exception e) {
                    Commons.printException("" + e);
                } finally {
                    dbUtil.closeDB();
                }
                return priorityproductList;
            }
        }).flatMap(new Function<ArrayList<String>, ObservableSource<ArrayList<String>>>() {
            @Override
            public ObservableSource<ArrayList<String>> apply(final ArrayList<String> strings) throws Exception {
                return Observable.fromCallable(new Callable<ArrayList<String>>() {
                    @Override
                    public ArrayList<String> call() throws Exception {

                        if(strings==null) {
                            ArrayList<String> priorityproductList = null;
                            try {
                                dbUtil.openDataBase();
                                String sql = "select ProductId from RetailerEditPriorityProducts where status = 'N' and retailerId=" + AppUtils.QT(retailerId);
                                Cursor c = dbUtil.selectSQL(sql);
                                if (c.getCount() > 0) {
                                    priorityproductList = new ArrayList<>();
                                    while (c.moveToNext()) {
                                        priorityproductList.add(c.getString(0));
                                    }
                                }
                            } catch (Exception e) {
                                Commons.printException("" + e);
                            } finally {
                                dbUtil.closeDB();
                            }
                            return priorityproductList;
                        }else
                            return strings;
                    }
                });
            }
        });
    }
}
