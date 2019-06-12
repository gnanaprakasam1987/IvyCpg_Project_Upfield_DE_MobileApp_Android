package com.ivy.ui.profile.data;


import android.database.Cursor;


import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.cpg.view.retailercontact.RetailerContactAvailBo;
import com.ivy.cpg.view.retailercontact.RetailerContactBo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.LocationBO;
import com.ivy.sd.png.bo.NewOutletAttributeBO;
import com.ivy.sd.png.bo.NewOutletBO;
import com.ivy.sd.png.bo.RetailerFlexBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Vector;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import io.reactivex.functions.Function4;

public class ProfileDataManagerImpl implements IProfileDataManager {

    private DBUtil dbUtil;

    @Inject
    public ProfileDataManagerImpl(@DataBaseInfo DBUtil dbUtil) {
        this.dbUtil = dbUtil;
        dbUtil.openDataBase();

    }


    @Override
    public Observable<ArrayList<NewOutletBO>> getContactTitle() {
        return Observable.fromCallable(new Callable<ArrayList<NewOutletBO>>() {
            @Override
            public ArrayList<NewOutletBO> call() throws Exception {
                ArrayList<NewOutletBO> contactTitleList = new ArrayList<>();
                NewOutletBO contactTitle = null;
                try {

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

                    Cursor c, headerCursor;
                    String tid = "";
                    String currentDate;
                    currentDate = DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL);
                    headerCursor = dbUtil.selectSQL("SELECT Tid FROM RetailerEditHeader" + " WHERE RetailerId = "
                            + RetailerID + " AND Date = " + StringUtils.QT(currentDate) + " AND Upload = " + StringUtils.QT("N"));
                    if (headerCursor.getCount() > 0) {
                        headerCursor.moveToNext();
                        tid = headerCursor.getString(0);
                        headerCursor.close();
                    }
                    c = dbUtil.selectSQL("select code, value from RetailerEditDetail RED INNER JOIN RetailerEditHeader REH ON REH.tid=RED.tid where REH.retailerid="
                            + RetailerID + " and REH.tid=" + StringUtils.QT(tid));
                    if (c != null) {
                        while (c.moveToNext()) {
                            mPreviousProfileChangesList.put(c.getString(0), c.getString(1));
                        }
                    }

                } catch (Exception e) {
                    Commons.printException("" + e);

                }

                return mPreviousProfileChangesList;
            }
        });
    }


    @Override
    public Single<String> generateOtpUrl() {
        return Single.fromCallable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                String downloadurl = "";
                try {
                    dbUtil.createDataBase();
                    Cursor c = dbUtil.selectSQL("select url from urldownloadmaster where mastername='OTP_GENERATION'");
                    if (c != null) {
                        if (c.getCount() > 0) {
                            while (c.moveToNext()) {
                                downloadurl = c.getString(0);
                            }
                        }
                    }
                } catch (Exception e) {
                    Commons.printException(e);
                }
                return downloadurl;
            }
        });
    }

    @Override
    public Single<Boolean> checkProfileImagePath(final RetailerMasterBO ret) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                Cursor c = dbUtil
                        .selectSQL("SELECT value FROM RetailerEditDetail  where code='PROFILE60' AND retailerid=" + ret.getRetailerID());
                if (c != null) {
                    if (c.getCount() > 0) {
                        if (c.moveToNext()) {
                            ret.setProfileImagePath(c.getString(0));
                            return true;
                        }
                    }
                    c.close();
                }
                return false;
            }
        });
    }


    /*In the old code this method will return tow array list
    but now it will return only mLinkRetailerList
    by this we need can get other data list ie mLinkRetailerListByDistributorId. Need to do in ProfileEditPresenterImp*/
    @Override
    public Observable<Vector<RetailerMasterBO>> getLinkRetailer() {

        return Observable.fromCallable(new Callable<Vector<RetailerMasterBO>>() {
            @Override
            public Vector<RetailerMasterBO> call() throws Exception {
                Vector<RetailerMasterBO> mLinkRetailerList = new Vector<>();
                try {

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
                    String sql = "select id,name from RetailerFlexValues where type = " + StringUtils.QT(type);
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
                }
                return flexValues;
            }
        });

    }


    @Override
    public void saveNearByRetailers(String id, Vector<RetailerMasterBO> NearByRetailers) {
        try {
            dbUtil.createDataBase();
            String columnsNew = "rid,nearbyrid,upload";
            String values;
            for (int j = 0; j < NearByRetailers.size(); j++) {
                values = StringUtils.QT(id) + "," + NearByRetailers.get(j).getRetailerID() + "," + StringUtils.QT("N");
                dbUtil.insertSQL("NearByRetailers", columnsNew, values);
            }

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
                    Cursor c = dbUtil
                            .selectSQL("SELECT nearbyrid from NearByRetailers where rid='" + RetailerID + "' and upload='Y'");
                    if (c.getCount() > 0) {
                        while (c.moveToNext()) {
                            lst.add(c.getString(0));
                        }
                    }
                    c.close();
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
                    Cursor c = dbUtil
                            .selectSQL("SELECT nearbyrid,status from RrtNearByEditRequest where rid=" + retailerId + " and upload='N'");
                    if (c.getCount() > 0) {
                        while (c.moveToNext()) {
                            lstEditRequests.put(c.getString(0), c.getString(1));
                        }

                    }
                    c.close();
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
                }
                return priorityproductList;
            }
        });
    }

    @Override
    public Observable<ArrayList<String>> downloadPriorityProductsForRetailerUpdate(final String retailerId) {
        return Observable.fromCallable(new Callable<ArrayList<String>>() {
            @Override
            public ArrayList<String> call() throws Exception {
                ArrayList<String> priorityproductList = new ArrayList<>();
                try {
                    String sql = "select  ProductId from RetailerPriorityProducts where retailerId=" + StringUtils.QT(retailerId);
                    Cursor c = dbUtil.selectSQL(sql);
                    if (c.getCount() > 0) {
                        priorityproductList = new ArrayList<>();
                        while (c.moveToNext()) {
                            priorityproductList.add(c.getString(0));
                        }
                    }
                } catch (Exception e) {
                    Commons.printException("" + e);
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
                    String sql = "select  ProductId from RetailerPriorityProducts where retailerId=" + StringUtils.QT(retailerId);
                    Cursor c = dbUtil.selectSQL(sql);
                    if (c.getCount() > 0) {
                        priorityproductList = new ArrayList<>();
                        while (c.moveToNext()) {
                            priorityproductList.add(c.getString(0));
                        }
                    }
                } catch (Exception e) {
                    Commons.printException("" + e);
                }
                return priorityproductList;
            }
        }).flatMap(new Function<ArrayList<String>, ObservableSource<ArrayList<String>>>() {
            @Override
            public ObservableSource<ArrayList<String>> apply(final ArrayList<String> strings) throws Exception {

                return Observable.fromCallable(new Callable<ArrayList<String>>() {
                    @Override
                    public ArrayList<String> call() throws Exception {

                        if (strings == null) {
                            ArrayList<String> priorityproductList = null;
                            try {
                                String sql = "select ProductId from RetailerEditPriorityProducts where status = 'N' and retailerId=" + StringUtils.QT(retailerId);
                                Cursor c = dbUtil.selectSQL(sql);
                                if (c.getCount() > 0) {
                                    priorityproductList = new ArrayList<>();
                                    while (c.moveToNext()) {
                                        priorityproductList.add(c.getString(0));
                                    }
                                }
                            } catch (Exception e) {
                                Commons.printException("" + e);
                            }
                            return priorityproductList;
                        } else
                            return strings;
                    }
                });
            }
        });
    }


    /*It will return CommonAttributeList*/
    @Override
    public Observable<ArrayList<Integer>> downloadCommonAttributeList() {
        return Observable.fromCallable(new Callable<ArrayList<Integer>>() {
            @Override
            public ArrayList<Integer> call() throws Exception {
                ArrayList<Integer> mCommonAttributeList = new ArrayList<>();
                try {
                    Cursor c = dbUtil
                            .selectSQL("SELECT attributeid FROM entityattributemaster where parentid =0 and attributeid not in(select attributeid from EntityCriteriaType) and IsSystemComputed=0 and IsCriteriaMapped=0 order by sequence");
                    if (c != null && c.getCount() > 0) {
                        mCommonAttributeList = new ArrayList<>();
                        while (c.moveToNext()) {
                            mCommonAttributeList.add(c.getInt(0));
                        }
                        c.close();
                    }
                } catch (Exception e) {
                    Commons.printException(e);
                }
                return mCommonAttributeList;
            }
        });
    }

    /*This method will return ChannelWiseAttributeList model class with below list
     mAttributeListByLocationID
     mAttributeBOListByLocationID*/
    @Override
    public Observable<ChannelWiseAttributeList> downloadChannelWiseAttributeList() {

        return Observable.fromCallable(new Callable<ChannelWiseAttributeList>() {
            @Override
            public ChannelWiseAttributeList call() throws Exception {

                HashMap<Integer, ArrayList<Integer>> mAttributeIdListByLocationID = null;
                HashMap<Integer, ArrayList<NewOutletAttributeBO>> mAttributeBOListByLocationID = null;
                ChannelWiseAttributeList channelWiseAttributeList = new ChannelWiseAttributeList();
                try {
                    Cursor c = dbUtil
                            .selectSQL("SELECT EAM.attributeid,CriteriaId,ECT.isMandatory,AttributeName FROM entityattributemaster EAM inner join EntityCriteriaType ECT ON EAM.attributeId=ECT.attributeId where parentid =0 and criteriaType='CHANNEL' and IsSystemComputed=0 order by sequence");
                    if (c != null && c.getCount() > 0) {

                        mAttributeBOListByLocationID = new HashMap<>();

                        mAttributeIdListByLocationID = new HashMap<>();

                        NewOutletAttributeBO newOutletAttributeBO;

                        while (c.moveToNext()) {

                            newOutletAttributeBO = new NewOutletAttributeBO();
                            newOutletAttributeBO.setAttrId(c.getInt(0));
                            newOutletAttributeBO.setIsMandatory(c.getInt(2));
                            newOutletAttributeBO.setAttrName(c.getString(3));

                            if (mAttributeBOListByLocationID.get(c.getInt(1)) != null) {
                                mAttributeBOListByLocationID.get(c.getInt(1)).add(newOutletAttributeBO);
                                mAttributeIdListByLocationID.get(c.getInt(1)).add(c.getInt(0));
                            } else {
                                ArrayList<NewOutletAttributeBO> mAtrributeList = new ArrayList<>();
                                mAtrributeList.add(newOutletAttributeBO);

                                mAttributeBOListByLocationID.put(c.getInt(1), mAtrributeList);

                                ArrayList<Integer> list = new ArrayList<>();
                                list.add(c.getInt(0));
                                mAttributeIdListByLocationID.put(c.getInt(1), list);
                            }

                        }
                        c.close();
                    }

                    channelWiseAttributeList = new ChannelWiseAttributeList(mAttributeBOListByLocationID, mAttributeIdListByLocationID);

                } catch (Exception e) {
                    Commons.printException(e);
                }
                return channelWiseAttributeList;
            }
        });

    }


    /*It will return attributeBOArrayList.
    Note:->we need to update attributeBOArrayList in RetailerMasterBO */
    @Override
    public Observable<ArrayList<NewOutletAttributeBO>> downloadAttributeListForRetailer(final String RetailerID) {

        return Observable.fromCallable(new Callable<ArrayList<NewOutletAttributeBO>>() {
            @Override
            public ArrayList<NewOutletAttributeBO> call() throws Exception {
                ArrayList<NewOutletAttributeBO> attributeBOArrayList = new ArrayList<>();
                try {

                    Cursor cursor = dbUtil.selectSQL("select RB.attributeid, RB.levelid from retailerattribute RB INNER JOIN " +
                            "EntityAttributeMaster  EAM  ON RB.attributeid=EAM.Attributeid  where RB.retailerid = " + RetailerID +
                            " order by EAM.ParentId");
                    if (cursor != null) {
                        attributeBOArrayList = new ArrayList<>();
                        NewOutletAttributeBO tempBO;
                        while (cursor.moveToNext()) {
                            tempBO = new NewOutletAttributeBO();
                            tempBO.setAttrId(cursor.getInt(0));
                            tempBO.setLevelId(cursor.getInt(1));
                            attributeBOArrayList.add(tempBO);
                        }

                        // getRetailerMasterBO().setAttributeBOArrayList(attributeBOArrayList);
                        cursor.close();
                    }

                } catch (Exception e) {
                    Commons.printException(e);
                }
                return attributeBOArrayList;
            }
        });
    }


    /*It will return the attributeBOArrayList */
    @Override
    public Observable<ArrayList<NewOutletAttributeBO>> downloadEditAttributeList(final String retailerID) {
        return Observable.fromCallable(new Callable<ArrayList<NewOutletAttributeBO>>() {
            @Override
            public ArrayList<NewOutletAttributeBO> call() throws Exception {
                ArrayList<NewOutletAttributeBO> attributeBOArrayList = new ArrayList<>();
                try {
                    Cursor cursor = dbUtil.selectSQL("select attributeid, levelid, status from retailereditattribute where retailerid = " + retailerID + " and upload='N'");
                    if (cursor != null) {
                        NewOutletAttributeBO tempBO;
                        while (cursor.moveToNext()) {
                            tempBO = new NewOutletAttributeBO();
                            tempBO.setAttrId(cursor.getInt(0));
                            tempBO.setLevelId(cursor.getInt(1));
                            tempBO.setStatus(cursor.getString(2));
                            attributeBOArrayList.add(tempBO);
                        }
                        cursor.close();

                    }
                } catch (Exception e) {

                    Commons.printException("" + e);
                    return new ArrayList<>();
                }
                return attributeBOArrayList;
            }
        });
    }


    /*It will return the RetailerAttribute Child */
    @Override
    public Observable<ArrayList<NewOutletAttributeBO>> downloadRetailerChildAttribute() {
        return Observable.fromCallable(new Callable<ArrayList<NewOutletAttributeBO>>() {
            @Override
            public ArrayList<NewOutletAttributeBO> call() throws Exception {

                ArrayList<NewOutletAttributeBO> attribListChild = new ArrayList<>();

                try {
                    NewOutletAttributeBO temp;
                    attribListChild = new ArrayList<>();

                    Cursor c = dbUtil
                            .selectSQL("SELECT attributeid, attributename, parentid, levelid, allowmultiple, iscriteriamapped FROM entityattributemaster where parentid !=" + 0 + " order by attributeid");
                    if (c != null) {
                        while (c.moveToNext()) {
                            temp = new NewOutletAttributeBO();
                            temp.setAttrId(c.getInt(0));
                            temp.setAttrName(c.getString(1));
                            temp.setParentId(c.getInt(2));
                            temp.setLevelId(c.getInt(3));
                            temp.setAllowMultiple(c.getInt(4));
                            temp.setCriteriaMapped(c.getInt(5));

                            attribListChild.add(temp);
                        }
                        c.close();
                    }

                } catch (Exception e) {
                    Commons.printException(e);
                }
                return attribListChild;
            }
        });
    }

    /*It will return the RetailerAttribute Parent */
    @Override
    public Observable<ArrayList<NewOutletAttributeBO>> downloadAttributeParentList(final ArrayList<NewOutletAttributeBO> attribList) {

        return Observable.fromCallable(new Callable<ArrayList<NewOutletAttributeBO>>() {
            @Override
            public ArrayList<NewOutletAttributeBO> call() throws Exception {

                ArrayList<NewOutletAttributeBO> attributeParentList = null;
                try {
                    attributeParentList = new ArrayList<>();
                    NewOutletAttributeBO temp;

                    Cursor c = dbUtil
                            .selectSQL("SELECT attributeid, attributename, isSystemComputed,IsMandatory FROM entityattributemaster where parentid =0 order by sequence");
                    if (c != null) {
                        //downloadRetailerAttributeChildList();
                        while (c.moveToNext()) {
                            int attribId = c.getInt(0);
                            int levelId = 0;
                            temp = new NewOutletAttributeBO();
                            temp.setAttrId(attribId);
                            temp.setAttrName(c.getString(1));
                            temp.setIsMandatory(c.getInt(3));

                            for (int i = 0; i < attribList.size(); i++) {
                                int parentID = attribList.get(i).getParentId();
                                if (attribId == parentID) {
                                    attribId = attribList.get(i).getAttrId();
                                    levelId = attribList.get(i).getLevelId();
                                }
                            }

                            temp.setLevelId(levelId);
                            attributeParentList.add(temp);
                        }
                        c.close();
                    }

                } catch (Exception e) {
                    Commons.printException(e);
                }
                return attributeParentList;
            }
        });
    }


    @Override
    public Observable<ArrayList<NewOutletAttributeBO>> updateRetailerMasterAttribute(
            final ArrayList<NewOutletAttributeBO> list,
            final ArrayList<NewOutletAttributeBO> childList,
            final ArrayList<NewOutletAttributeBO> parentList) {
        return Observable.fromCallable(new Callable<ArrayList<NewOutletAttributeBO>>() {
            @Override
            public ArrayList<NewOutletAttributeBO> call() throws Exception {
                ArrayList<NewOutletAttributeBO> tempList = new ArrayList<>();
                int attribID;
                int tempAttribID;
                int parentID;
                int tempParentID = 0;
                String attribName = "";
                String attribHeader = "";
                int levelId;
                String status;
                NewOutletAttributeBO tempBO;
                for (NewOutletAttributeBO attributeBO : list) {
                    tempBO = new NewOutletAttributeBO();
                    attribID = attributeBO.getAttrId();
                    status = attributeBO.getStatus();
                    levelId = attributeBO.getLevelId();
                    for (int i = childList.size() - 1; i >= 0; i--) {
                        NewOutletAttributeBO attributeBO1 = childList.get(i);
                        tempAttribID = attributeBO1.getAttrId();
                        if (attribID == tempAttribID) {
                            attribName = attributeBO1.getAttrName();
                            tempParentID = attributeBO1.getParentId();
                            continue;
                        }
                        if (tempAttribID == tempParentID)
                            tempParentID = attributeBO1.getParentId();
                    }

                    for (NewOutletAttributeBO attributeBO2 : parentList) {
                        parentID = attributeBO2.getAttrId();
                        if (tempParentID == parentID)
                            attribHeader = attributeBO2.getAttrName();
                    }
                    tempBO.setAttrId(attribID);
                    tempBO.setParentId(tempParentID);
                    tempBO.setAttrName(attribName);
                    tempBO.setAttrParent(attribHeader);
                    tempBO.setStatus(status);
                    tempBO.setLevelId(levelId);
                    tempList.add(tempBO);
                }
                return tempList;
            }
        });
    }


    @Override
    public Single<String> checkHeaderAvailablility(final String RetailerID, final String currentDate) {
        return Single.fromCallable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Cursor headerCursor;
                String tid = "";
                try {
                    // delete Header if exist
                    headerCursor = dbUtil.selectSQL("SELECT Tid FROM RetailerEditHeader"
                            + " WHERE RetailerId = "
                            + RetailerID
                            + " AND Date = "
                            + StringUtils.QT(currentDate)
                            + " AND Upload = "
                            + StringUtils.QT("N"));

                    if (headerCursor.getCount() > 0) {
                        headerCursor.moveToNext();
                        tid = headerCursor.getString(0);
                        headerCursor.close();
                    }
                } catch (Exception e) {
                    Commons.printException(e);
                }
                return tid;
            }
        });
    }


    @Override
    public Single<Boolean> deleteQuery(final String configCode, final String RetailerID) {

        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    dbUtil.deleteSQL(DataMembers.tbl_RetailerEditDetail, " Code =" + StringUtils.QT(configCode) + "and RetailerId=" + RetailerID, false);
                } catch (Exception e) {
                    Commons.printException("" + e);
                }
                return true;
            }
        });
    }


    @Override
    public Single<Boolean> insertNewRow(final String configCode, final String RetailerID, String mTid, String mCustomQuery) {

        final String insertquery = "insert into RetailerEditDetail (tid,Code,value,RefId,RetailerId)" + "values (" + StringUtils.QT(mTid) + "," + mCustomQuery;

        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    dbUtil.deleteSQL(DataMembers.tbl_RetailerEditDetail, " Code =" + StringUtils.QT(configCode) + "and RetailerId=" + RetailerID, false);
                } catch (Exception e) {
                    Commons.printException("" + e);
                }
                return true;
            }
        }).flatMap(new Function<Boolean, SingleSource<? extends Boolean>>() {
            @Override
            public SingleSource<? extends Boolean> apply(final Boolean aBoolean) throws Exception {
                return Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        try {
                            // if(aBoolean) if we want we can add condition. it will return the deleted row response .
                            dbUtil.executeQ(insertquery);
                        } catch (Exception e) {
                            Commons.printException("" + e);
                        }
                        return true;
                    }
                });
            }
        });

    }

    @Override
    public Single<Boolean> updateNearByRetailers(final String mTid, final String RetailerID,
                                                 final HashMap<String, String> temp) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    dbUtil.deleteSQL("RrtNearByEditRequest", " tid =" + StringUtils.QT(mTid), false);
                } catch (Exception e) {
                    Commons.printException("" + e);
                }
                return true;
            }
        }).flatMap(new Function<Boolean, SingleSource<? extends Boolean>>() {
            @Override
            public SingleSource<? extends Boolean> apply(Boolean aBoolean) throws Exception {
                return Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        try {
                            // if(aBoolean) if we want we can add condition. it will return the deleted row response .
                            if (temp != null)
                                for (String id : temp.keySet()) {
                                    String Q = "insert into RrtNearByEditRequest (tid,rid,nearbyrid,status,upload)" +
                                            "values (" + StringUtils.QT(mTid) + "," + RetailerID + "," + id
                                            + "," + StringUtils.QT(temp.get(id)) + ",'N')";
                                    dbUtil.executeQ(Q);
                                }
                        } catch (Exception e) {
                            Commons.printException("" + e);
                        }
                        return true;
                    }
                });
            }
        });
    }

    @Override
    public Single<Boolean> updateRetailerEditPriorityProducts(final String mTid, final String RetailerID
            , final ArrayList<StandardListBO> selectedPrioProducts) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    dbUtil.deleteSQL("RetailerEditPriorityProducts", " RetailerId ="
                            + StringUtils.QT(RetailerID), false);
                } catch (Exception e) {
                    Commons.printException("" + e);
                }
                return true;
            }
        }).flatMap(new Function<Boolean, SingleSource<? extends Boolean>>() {
            @Override
            public SingleSource<? extends Boolean> apply(Boolean aBoolean) throws Exception {
                return Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        try {
                            if (selectedPrioProducts != null)
                                for (StandardListBO bo : selectedPrioProducts) {
                                    String Q = "insert into RetailerEditPriorityProducts (tid,RetailerId,productId,levelid,status,upload)" +
                                            "values (" + StringUtils.QT(mTid)
                                            + "," + StringUtils.QT(RetailerID)
                                            + "," + SDUtil.convertToInt(bo.getListID())
                                            + "," + StringUtils.QT(bo.getListCode())
                                            + "," + StringUtils.QT(bo.getStatus()) + ",'N')";
                                    dbUtil.executeQ(Q);
                                }
                            return true;
                        } catch (Exception e) {
                            Commons.printException("" + e);
                            return false;
                        }

                    }
                });
            }
        });
    }

    @Override
    public Single<Boolean> updateRetailerMasterAttribute(final String mTid, final String RetailerID,
                                                         final ArrayList<NewOutletAttributeBO> tempList) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    dbUtil.deleteSQL("RetailerEditAttribute", " tid =" + StringUtils.QT(mTid), false);

                } catch (Exception e) {
                    Commons.printException("" + e);
                }
                return true;
            }
        }).flatMap(new Function<Boolean, SingleSource<? extends Boolean>>() {
            @Override
            public SingleSource<? extends Boolean> apply(Boolean aBoolean) throws Exception {

                return Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        try {
                            for (NewOutletAttributeBO id : tempList) {
                                String Q = "insert into RetailerEditAttribute (tid,retailerid,attributeid,levelid,status,upload)" +
                                        "values (" + StringUtils.QT(mTid)
                                        + "," + RetailerID
                                        + "," + id.getAttrId()
                                        + "," + id.getLevelId()
                                        + "," + StringUtils.QT(id.getStatus()) + ",'N')";
                                dbUtil.executeQ(Q);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return true;
                    }
                });
            }
        });
    }

    @Override
    public Single<Boolean> updateRetailer(final String tid, final String RetailerID, final String currentDate) {

        final String insertHeader = "insert into RetailerEditHeader (tid,RetailerId,date)" +
                "values (" + StringUtils.QT(tid) + "," + RetailerID + "," + StringUtils.QT(currentDate) + ")";

        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    dbUtil.deleteSQL(DataMembers.tbl_RetailerEditHeader, " Tid=" + StringUtils.QT(tid), false);
                } catch (Exception e) {
                    Commons.printException("" + e);
                }
                return true;
            }
        }).flatMap(new Function<Boolean, SingleSource<? extends Boolean>>() {
            @Override
            public SingleSource<? extends Boolean> apply(Boolean aBoolean) throws Exception {

                return Single.zip(
                        getRetailerEditDetailCount(tid),
                        getnearbyEditRequestCount(tid),
                        getRetailerEditPriorityProductsCount(tid),
                        getRetailerEditAttributeCount(tid),
                        new Function4<Integer, Integer, Integer, Integer, Boolean>() {
                            @Override
                            public Boolean apply(Integer integer, Integer integer2,
                                                 Integer integer3, Integer integer4) throws Exception {
                                return integer > 0 || integer2 > 0 || integer3 > 0 || integer4 > 0;
                            }
                        }).flatMap(new Function<Boolean, SingleSource<? extends Boolean>>() {
                    @Override
                    public SingleSource<? extends Boolean> apply(final Boolean aBoolean) throws Exception {

                        return Single.fromCallable(new Callable<Boolean>() {
                            @Override
                            public Boolean call() throws Exception {
                                try {
                                    if (aBoolean)
                                        dbUtil.executeQ(insertHeader);
                                } catch (Exception e) {
                                    Commons.printException("" + e);
                                }
                                return true;
                            }
                        });

                    }
                });
            }
        });
    }

    @Override
    public Single<Boolean> insertRetailerContactEdit(final String mTid, final String RetailerID,
                                                     final ArrayList<RetailerContactBo> retailerContactList) {

        final String column = "Contact_Title,Contact_Title_LovId,ContactName,ContactName_LName," +
                "ContactNumber,Email,IsPrimary,Status,CPId,RetailerId,Tid,salutationLovId,IsEmailNotificationReq";

        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    String where="RetailerId="+RetailerID;

                    Cursor getCpidCursor = dbUtil.selectSQL("Select CPId from RetailerContactEdit where retailerId=" + StringUtils.QT(RetailerID));

                    if (getCpidCursor != null && getCpidCursor.getCount() > 0) {
                        while (getCpidCursor.moveToNext()) {
                            dbUtil.deleteSQL("ContactAvailabilityEdit", "CPId=" + getCpidCursor.getString(0), false);
                        }
                    }

                    dbUtil.deleteSQL("RetailerContactEdit",where,false);
                } catch (Exception e) {
                    Commons.printException("" + e);
                }
                return true;
            }
        }).flatMap(new Function<Boolean, SingleSource<? extends Boolean>>() {
            @Override
            public SingleSource<? extends Boolean> apply(Boolean aBoolean) throws Exception {

                return Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        if (retailerContactList.size() > 0) {
                            for (RetailerContactBo retailerContactBo : retailerContactList) {
                                if (retailerContactBo.getStatus().equalsIgnoreCase("U")
                                        || retailerContactBo.getStatus().equalsIgnoreCase("I")
                                        || retailerContactBo.getStatus().equalsIgnoreCase("D")) {
                                    String value = StringUtils.QT(retailerContactBo.getTitle()) + ","
                                            + StringUtils.QT(retailerContactBo.getContactTitleLovId()) + ","
                                            + StringUtils.QT(retailerContactBo.getFistname()) + ","
                                            + StringUtils.QT(retailerContactBo.getLastname()) + ","
                                            + StringUtils.QT(retailerContactBo.getContactNumber()) + ","
                                            + StringUtils.QT(retailerContactBo.getContactMail()) + ","
                                            + retailerContactBo.getIsPrimary() + ","
                                            + StringUtils.QT(retailerContactBo.getStatus()) + ","
                                            + StringUtils.QT(retailerContactBo.getCpId()) + ","
                                            + StringUtils.QT(RetailerID) + ","
                                            + StringUtils.QT(mTid)+ ","
                                            + StringUtils.QT(retailerContactBo.getContactSalutationId())+ ","
                                            + StringUtils.QT(retailerContactBo.getIsEmailPrimary()+"");
                                    dbUtil.insertSQL("RetailerContactEdit", column, value);

                                    addContactAvail(dbUtil,retailerContactBo,RetailerID,mTid);
                                }
                            }
                        }
                        return true;
                    }
                });

            }
        });
    }

    private void addContactAvail(DBUtil db, RetailerContactBo retailerContactBo,String retailerId,String Tid){
        String column = "CPAId,CPId,Day,StartTime,EndTime,Tid,status,upload, RetailerID";

        for (RetailerContactAvailBo retailerContactAvailBo : retailerContactBo.getContactAvailList()) {

            String value = StringUtils.QT(retailerContactAvailBo.getCpaid()!=null&&!retailerContactAvailBo.getCpaid().isEmpty()?retailerContactAvailBo.getCpaid():retailerId)
                    + "," + StringUtils.QT(retailerContactBo.getCpId())
                    + "," + StringUtils.QT(retailerContactAvailBo.getDay())
                    + "," + StringUtils.QT(retailerContactAvailBo.getFrom())
                    + "," + StringUtils.QT(retailerContactAvailBo.getTo())
                    + "," + StringUtils.QT(Tid)
                    + "," + StringUtils.QT(retailerContactAvailBo.getStatus())
                    + "," + StringUtils.QT("N")
                    + "," + StringUtils.QT(retailerContactBo.getRetailerID());

            db.insertSQL("ContactAvailabilityEdit", column, value);
        }
    }

    private Single<Integer> getRetailerEditDetailCount(final String tid) {
        return Single.fromCallable(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                try {
                    Cursor c = dbUtil.selectSQL("SELECT code FROM " + DataMembers.tbl_RetailerEditDetail + " where Tid=" + StringUtils.QT(tid));
                    if (c != null)
                        return c.getCount();
                } catch (Exception e) {
                    Commons.printException("" + e);
                }
                return 0;
            }
        });
    }

    private Single<Integer> getnearbyEditRequestCount(final String tid) {
        return Single.fromCallable(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                try {
                    Cursor c = dbUtil.selectSQL("SELECT status FROM " + DataMembers.tbl_nearbyEditRequest + " where Tid=" + StringUtils.QT(tid));
                    if (c != null)
                        return c.getCount();
                } catch (Exception e) {
                    Commons.printException("" + e);
                }
                return 0;
            }
        });
    }

    private Single<Integer> getRetailerEditPriorityProductsCount(final String tid) {
        return Single.fromCallable(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                try {
                    Cursor c = dbUtil.selectSQL("SELECT status FROM " + DataMembers.tbl_RetailerEditPriorityProducts + " where Tid=" + StringUtils.QT(tid));
                    if (c != null)
                        return c.getCount();
                } catch (Exception e) {
                    Commons.printException("" + e);
                }
                return 0;
            }
        });
    }

    private Single<Integer> getRetailerEditAttributeCount(final String tid) {
        return Single.fromCallable(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                try {
                    Cursor c = dbUtil.selectSQL("SELECT status FROM RetailerEditAttribute" + " where Tid=" + StringUtils.QT(tid));
                    if (c != null)
                        return c.getCount();
                } catch (Exception e) {
                    Commons.printException("" + e);
                }
                return 0;
            }
        });
    }

    @Override
    public void closeDB() {
        dbUtil.closeDB();
    }

}
