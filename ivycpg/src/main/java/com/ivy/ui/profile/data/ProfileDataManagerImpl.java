package com.ivy.ui.profile.data;


import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.core.data.sync.SynchronizationDataManager;
import com.ivy.core.di.scope.ApplicationContext;
import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.cpg.view.order.OrderHelper;
import com.ivy.cpg.view.retailercontact.RetailerContactAvailBo;
import com.ivy.cpg.view.retailercontact.RetailerContactBo;
import com.ivy.cpg.view.survey.SurveyHelperNew;
import com.ivy.lib.Utils;
import com.ivy.lib.existing.DBUtil;
import com.ivy.lib.rest.JSONFormatter;
import com.ivy.sd.png.bo.AddressBO;
import com.ivy.sd.png.bo.AttributeBO;
import com.ivy.sd.png.bo.CensusLocationBO;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.GenericObjectPair;
import com.ivy.sd.png.bo.LocationBO;
import com.ivy.sd.png.bo.NewOutletAttributeBO;
import com.ivy.sd.png.bo.NewOutletBO;
import com.ivy.sd.png.bo.OrderHeader;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.RetailerFlexBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.ProductHelper;
import com.ivy.sd.png.provider.ProductTaggingHelper;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.ui.profile.ProfileConstant;
import com.ivy.ui.profile.create.NewRetailerConstant;
import com.ivy.ui.profile.create.di.NewRetailer;
import com.ivy.ui.profile.create.model.ContactTitle;
import com.ivy.ui.profile.create.model.ContractStatus;
import com.ivy.ui.profile.create.model.PaymentType;
import com.ivy.utils.AppUtils;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FileUtils;
import com.ivy.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import io.reactivex.functions.Function4;

import static com.ivy.ui.profile.create.NewRetailerConstant.MENU_NEW_RETAILER;
import static com.ivy.utils.StringUtils.getStringQueryParam;
import static com.ivy.utils.StringUtils.isNullOrEmpty;

public class ProfileDataManagerImpl implements ProfileDataManager {

    private final SurveyHelperNew surveyHelperNew;
    private final ProductHelper productHelper;
    private final ProductTaggingHelper productTaggingHelper;
    private final Context context;
    private DBUtil dbUtil;

    private DataManager dataManager;

    private ConfigurationMasterHelper configurationMasterHelper;

    private SynchronizationDataManager synchronizationDataManager;

    @Inject
    public ProfileDataManagerImpl(@DataBaseInfo DBUtil dbUtil,
                                  DataManager dataManager,
                                  ConfigurationMasterHelper configurationMasterHelper,
                                  SynchronizationDataManager synchronizationDataManager,
                                  @NewRetailer SurveyHelperNew surveyHelperNew,
                                  ProductHelper productHelper,
                                  ProductTaggingHelper productTaggingHelper,
                                  @ApplicationContext Context context) {
        this.dbUtil = dbUtil;
        this.dataManager = dataManager;
        this.configurationMasterHelper = configurationMasterHelper;
        this.synchronizationDataManager = synchronizationDataManager;
        this.surveyHelperNew = surveyHelperNew;
        this.productHelper = productHelper;
        this.productTaggingHelper = productTaggingHelper;
        this.context = context;
        dbUtil.openDataBase();

    }

    private void initDb() {
        dbUtil.createDataBase();
        if (dbUtil.isDbNullOrClosed())
            dbUtil.openDataBase();
    }

    private void shutDownDb() {
        dbUtil.closeDB();
    }

    private ArrayList<String> loadImgList(String retailerID) {
        ArrayList<String> imgList = new ArrayList<>();
        try {
            Cursor c1;
            String query = "Select ImageName from NewOutletImage where RetailerId=" + getStringQueryParam(retailerID);
            c1 = dbUtil.selectSQL(query);
            if (c1 != null) {
                if (c1.getCount() > 0) {
                    String attrName = "";
                    while (c1.moveToNext()) {
                        attrName = c1.getString(0).substring(c1.getString(0)
                                .indexOf("/NO") + 1, c1.getString(0).indexOf(".jpg"));
                        imgList.add(attrName);
                    }
                }
                c1.close();
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return imgList;
    }

    /*
    Load Edit Attributes list
     */
    private ArrayList<String> loadEditAttributes(String retailerID) {
        ArrayList<String> attributeList = new ArrayList<>();
        try {
            Cursor c1;
            String query = "Select AttributeId from RetailerAttribute where RetailerId=" + getStringQueryParam(retailerID);
            c1 = dbUtil.selectSQL(query);
            if (c1 != null) {
                if (c1.getCount() > 0) {
                    NewOutletAttributeBO attrBo;
                    while (c1.moveToNext()) {
                        attrBo = new NewOutletAttributeBO();
                        attrBo.setAttrId(c1.getInt(0));
                        attributeList.add(String.valueOf(attrBo.getAttrId()));
                    }
                }
                c1.close();
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return attributeList;
    }


    @Override
    public Observable<ArrayList<ConfigureBO>> getProfileConfigs(int channelId, boolean isChannelSelectionNewRetailer, String language) {

        return Observable.fromCallable(new Callable<ArrayList<ConfigureBO>>() {
            @Override
            public ArrayList<ConfigureBO> call() throws Exception {
                ConfigureBO ConfigureBO;
                ArrayList<ConfigureBO> profileConfig = new ArrayList<>();
                try {
                    initDb();
                    Cursor c;

                    StringBuilder sb = new StringBuilder();
                    sb.append("select HHTCode, flag , MName , MNumber ,RField,haslink,RField6,Regex from HhtMenuMaster where MenuType= 'MENU_NEW_RET'");
                    sb.append("  and Flag = 1 and lang=");
                    sb.append(getStringQueryParam(language));
                    if (isChannelSelectionNewRetailer) {
                        sb.append(" and subchannelid=");
                        sb.append(channelId);
                        sb.append(" ");
                    }
                    sb.append(" order by Mnumber");
                    c = dbUtil.selectSQL(sb.toString());
                    if (c != null) {
                        while (c.moveToNext()) {
                            ConfigureBO = new ConfigureBO();
                            ConfigureBO.setConfigCode(c.getString(0));
                            ConfigureBO.setFlag(c.getInt(1));
                            ConfigureBO.setMenuName(c.getString(2));
                            ConfigureBO.setMenuNumber((c.getString(3)));
                            ConfigureBO.setMandatory((c.getInt(4)));
                            ConfigureBO.setHasLink(c.getInt(5));
                            ConfigureBO.setRField6(String.valueOf(c.getInt(6)));
                            String str = c.getString(7);
                            if (str != null && !str.isEmpty()) {
                                if (str.contains("<") && str.contains(">")) {

                                    String minlen = str.substring(str.indexOf("<") + 1, str.indexOf(">"));
                                    if (!minlen.isEmpty()) {
                                        try {
                                            ConfigureBO.setMaxLengthNo(SDUtil.convertToInt(minlen));
                                        } catch (Exception ex) {
                                            Commons.printException("min len in new outlet helper", ex);
                                        }
                                    }
                                }
                            }
                            ConfigureBO.setRegex(c.getString(7));
                            profileConfig.add(ConfigureBO);
                        }
                        c.close();
                    }
                    shutDownDb();
                } catch (Exception e) {
                    Commons.printException("" + e);
                }

                return profileConfig;
            }
        });
    }

    @Override
    public Observable<ArrayList<String>> downloadNearbyRetailers(String retailerId) {
        return Observable.fromCallable(new Callable<ArrayList<String>>() {
            @Override
            public ArrayList<String> call() throws Exception {
                ArrayList<String> nearByRetailers = null;
                try {
                    dbUtil.openDataBase();
                    String sql = "select  nearbyrid from NearByRetailers where rid=" + getStringQueryParam(retailerId);
                    Cursor c = dbUtil.selectSQL(sql);
                    if (c.getCount() > 0) {
                        nearByRetailers = new ArrayList<>();
                        while (c.moveToNext()) {
                            nearByRetailers.add(c.getString(0));
                        }
                    }
                    c.close();

                } catch (Exception e) {
                    Commons.printException("" + e);
                }
                return nearByRetailers;
            }
        });
    }

    @Override
    public Observable<ArrayList<StandardListBO>> downloadClassType() {
        return Observable.fromCallable(new Callable<ArrayList<StandardListBO>>() {
            @Override
            public ArrayList<StandardListBO> call() throws Exception {
                ArrayList<StandardListBO> classTypeList = new ArrayList<>();
                try {
                    dbUtil.openDataBase();
                    String sb = "select listid,listname from standardlistmaster where listtype='CLASS_TYPE'";
                    Cursor c = dbUtil.selectSQL(sb);
                    if (c.getCount() > 0) {
                        StandardListBO standardListBO;
                        while (c.moveToNext()) {
                            standardListBO = new StandardListBO();
                            standardListBO.setListID(c.getString(0));
                            standardListBO.setListName(c.getString(1));
                            classTypeList.add(standardListBO);
                        }
                    }
                    c.close();

                } catch (Exception e) {
                    Commons.printException("" + e);
                }
                return classTypeList;
            }
        });
    }

    @Override
    public Observable<ArrayList<StandardListBO>> getTaxType() {
        return Observable.fromCallable(new Callable<ArrayList<StandardListBO>>() {
            @Override
            public ArrayList<StandardListBO> call() throws Exception {
                ArrayList<StandardListBO> taxTypeList = new ArrayList<>();
                try {
                    dbUtil.openDataBase();
                    String sb = "select listid,listname from standardlistmaster where listtype='CERTIFICATE_TYPE'";
                    Cursor c = dbUtil.selectSQL(sb);
                    if (c.getCount() > 0) {
                        StandardListBO standardListBO;
                        while (c.moveToNext()) {
                            standardListBO = new StandardListBO();
                            standardListBO.setListID(c.getString(0));
                            standardListBO.setListName(c.getString(1));
                            taxTypeList.add(standardListBO);
                        }
                    }
                    c.close();

                } catch (Exception e) {
                    Commons.printException("" + e);
                }
                return taxTypeList;
            }
        });
    }


    @Override
    public Observable<ArrayList<String>> getRetailerBySupplierId(String suppilerID) {
        return Observable.fromCallable(new Callable<ArrayList<String>>() {
            @Override
            public ArrayList<String> call() throws Exception {
                ArrayList<String> mRetailerIds = new ArrayList<>();
                try {
                    initDb();
                    String sb = "select distinct rid from Suppliermaster " +
                            "where sid=" + getStringQueryParam(suppilerID);

                    Cursor c = dbUtil.selectSQL(sb);
                    if (c.getCount() > 0) {
                        while (c.moveToNext()) {
                            mRetailerIds.add(c.getString(0));
                        }
                        c.close();
                    }
                    dbUtil.closeDB();
                } catch (Exception e) {
                    if (dbUtil != null)
                        dbUtil.closeDB();
                    Commons.printException("" + e);
                    return new ArrayList<>();
                }

                return mRetailerIds;
            }
        });
    }


    @Override
    public Observable<ArrayList<PaymentType>> getRetailerType() {
        return Observable.fromCallable(new Callable<ArrayList<PaymentType>>() {
            @Override
            public ArrayList<PaymentType> call() throws Exception {
                PaymentType retailerType;
                ArrayList<PaymentType> retailerTypeList = new ArrayList<>();
                try {

                    dbUtil.openDataBase();
                    Cursor c = dbUtil
                            .selectSQL("SELECT ListId,ListCode,ListName from StandardListMaster where ListType='RETAILER_TYPE'");
                    if (c.getCount() > 0) {
                        while (c.moveToNext()) {
                            retailerType = new PaymentType();
                            retailerType.setListId(c.getInt(0));
                            retailerType.setListName(c.getString(2));
                            retailerTypeList.add(retailerType);
                        }
                        c.close();
                    }
                    dbUtil.closeDB();
                } catch (Exception e) {
                    Commons.printException("" + e);
                }
                return retailerTypeList;
            }
        });
    }

    @Override
    public Observable<ArrayList<NewOutletBO>> getNewRetailers() {
        return Observable.fromCallable(new Callable<ArrayList<NewOutletBO>>() {
            @Override
            public ArrayList<NewOutletBO> call() throws Exception {
                ArrayList<NewOutletBO> lst = new ArrayList<>();
                try {

                    Cursor c;

                    String query = "select distinct RM.RetailerID,RetailerName,subchannelid,beatid,visitDays ,locationid,creditlimit,RPTypeId,tinnumber,RField3," +
                            "distributorId,TaxTypeid,contractstatuslovid,classid,AccountId,RC1.contactname as contactName1,RC1.ContactName_LName as contactLName1,RC1.contactNumber as contactNumber1" +
                            ",RC1.contact_title as contact_title1,RC1.contact_title_lovid as contact_title_lovid1" +
                            ",RC2.contactname as contactName2,RC2.ContactName_LName as contactLName2,RC2.contactNumber as contactNumber2,RC2.contact_title as contact_title2,RC2.contact_title_lovid as contact_title_lovid2," +
                            "RA.address1,RA.address2,RA.address3,RA.City,RA.latitude,RA.longitude,RA.email,RA.FaxNo,RA.pincode,RA.State,RM.RField5,RM.RField6,RM.TinExpDate," +
                            "RM.pan_number,RM.food_licence_number,RM.food_licence_exp_date,RM.DLNo,RM.DLNoExpDate,RM.RField4,RM.RField7,RA.Mobile,RA.Region,RA.Country,RM.userid,RM.GSTNumber" +
                            " from RetailerMaster RM LEFT JOIN RetailerContact RC1 ON Rm.retailerid=RC1.retailerId AND RC1.isprimary=1" +
                            " LEFT JOIN RetailerContact RC2 ON Rm.retailerid=RC2.retailerId AND RC2.isprimary=0" +
                            " LEFT JOIN RetailerAddress RA ON RA.RetailerId=RM.retailerId" +
                            " where RM.is_new='Y' and RM.upload='N' ";
                    c = dbUtil.selectSQL(query);
                    if (c != null) {
                        if (c.getCount() > 0) {
                            NewOutletBO retailer;
                            while (c.moveToNext()) {
                                retailer = new NewOutletBO();
                                retailer.setRetailerId(c.getString(c.getColumnIndex("RetailerID")));
                                retailer.setOutletName(c.getString(c.getColumnIndex("RetailerName")));
                                retailer.setSubChannel(c.getInt(c.getColumnIndex("subchannelid")));
                                retailer.setRouteid(c.getInt(c.getColumnIndex("beatid")));
                                retailer.setVisitDays(c.getString(c.getColumnIndex("VisitDays")));
                                retailer.setLocid(c.getInt(c.getColumnIndex("locationid")));
                                retailer.setCreditLimit(c.getString(c.getColumnIndex("creditlimit")));
                                retailer.setPayment(c.getString(c.getColumnIndex("RPTypeId")));
                                retailer.setTinno(c.getString(c.getColumnIndex("tinnumber")));
                                retailer.setRfield3(c.getString(c.getColumnIndex("RField3")));
                                retailer.setDistid(c.getString(c.getColumnIndex("distributorid")));
                                retailer.setTaxTypeId(c.getString(c.getColumnIndex("TaxTypeId")));
                                retailer.setContractStatuslovid(c.getInt(c.getColumnIndex("contractstatuslovid")));
                                retailer.setClassTypeId(c.getString(c.getColumnIndex("classid")));

                                //from retailer contact
                                retailer.setContactpersonname(c.getString(c.getColumnIndex("contactName1")));
                                retailer.setContactpersonnameLastName(c.getString(c.getColumnIndex("contactLName1")));
                                retailer.setPhone(c.getString(c.getColumnIndex("contactNumber1")));
                                retailer.setContact1title(c.getString(c.getColumnIndex("contact_title1")));
                                retailer.setContact1titlelovid(c.getString(c.getColumnIndex("contact_title_lovid1")));

                                retailer.setContactpersonname2(c.getString(c.getColumnIndex("contactName2")));
                                retailer.setContactpersonname2LastName(c.getString(c.getColumnIndex("contactLName2")));
                                retailer.setPhone2(c.getString(c.getColumnIndex("contactNumber2")));
                                retailer.setContact2title(c.getString(c.getColumnIndex("contact_title2")));
                                retailer.setContact2titlelovid(c.getString(c.getColumnIndex("contact_title_lovid2")));

                                // from Retailer Address
                                retailer.setAddress(c.getString(c.getColumnIndex("Address1")));
                                retailer.setAddress2(c.getString(c.getColumnIndex("Address2")));
                                retailer.setAddress3(c.getString(c.getColumnIndex("Address3")));
                                retailer.setCity(c.getString(c.getColumnIndex("City")));
                                retailer.setNewOutletlattitude(c.getDouble(c.getColumnIndex("Latitude")));
                                retailer.setNewOutletLongitude(c.getDouble(c.getColumnIndex("Longitude")));
                                retailer.setEmail(c.getString(c.getColumnIndex("Email")));
                                retailer.setFax(c.getString(c.getColumnIndex("FaxNo")));
                                retailer.setPincode(c.getString(c.getColumnIndex("pincode")));
                                retailer.setState(c.getString(c.getColumnIndex("State")));
                                retailer.setRfield5(c.getString(c.getColumnIndex("RField5")));
                                retailer.setRfield6(c.getString(c.getColumnIndex("RField6")));
                                retailer.setTinExpDate(c.getString(c.getColumnIndex("TinExpDate")));
                                retailer.setPanNo(c.getString(c.getColumnIndex("pan_number")));
                                retailer.setFoodLicenseNo(c.getString(c.getColumnIndex("food_licence_number")));
                                retailer.setFlExpDate(c.getString(c.getColumnIndex("food_licence_exp_date")));
                                retailer.setDrugLicenseNo(c.getString(c.getColumnIndex("DLNo")));
                                retailer.setDlExpDate(c.getString(c.getColumnIndex("DLNoExpDate")));
                                retailer.setrField7(c.getString(c.getColumnIndex("RField7")));
                                retailer.setrField4(c.getString(c.getColumnIndex("RField4")));
                                retailer.setRegion(c.getString(c.getColumnIndex("Region")));
                                retailer.setCountry(c.getString(c.getColumnIndex("Country")));
                                retailer.setMobile(c.getString(c.getColumnIndex("Mobile")));
                                retailer.setUserId(c.getInt(c.getColumnIndex("userid")));
                                retailer.setGstNum(c.getString(c.getColumnIndex("GSTNumber")));
                                retailer.setImageName(loadImgList(retailer.getRetailerId()));
                                retailer.setEditAttributeList(loadEditAttributes(retailer.getRetailerId()));
                                lst.add(retailer);
                            }
                        }
                        c.close();
                    }


                } catch (Exception e) {
                    Commons.printException("" + e);

                }
                return lst;
            }
        });
    }

    @Override
    public Observable<Vector<NewOutletBO>> getImageTypeList() {
        return Observable.fromCallable(new Callable<Vector<NewOutletBO>>() {
            @Override
            public Vector<NewOutletBO> call() throws Exception {

                Vector<NewOutletBO> imageTypeList = new Vector<>();
                try {

                    Cursor c = dbUtil.selectSQL("SELECT ListId,ListCode,ListName from StandardListMaster where ListType='RETAILER_IMAGE_TYPE'");
                    if (c.getCount() > 0) {
                        NewOutletBO imageType;
                        while (c.moveToNext()) {
                            imageType = new NewOutletBO();
                            imageType.setListId(c.getInt(0));
                            imageType.setListName(c.getString(2));
                            imageTypeList.add(imageType);
                        }
                        c.close();
                    }

                } catch (Exception e) {
                    Commons.printException("" + e);
                }
                return imageTypeList;
            }
        });
    }

    @Override
    public Observable<ArrayList<ContactTitle>> getContactTitle() {
        return Observable.fromCallable(new Callable<ArrayList<ContactTitle>>() {
            @Override
            public ArrayList<ContactTitle> call() throws Exception {
                ArrayList<ContactTitle> contactTitleList = new ArrayList<>();
                ContactTitle contactTitle = null;
                try {

                    Cursor c = dbUtil.selectSQL("SELECT ListId,ListCode,ListName from StandardListMaster where ListType='RETAILER_CONTACT_TITLE_TYPE'");
                    if (c.getCount() > 0) {
                        while (c.moveToNext()) {
                            contactTitle = new ContactTitle();
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
    public Observable<ArrayList<ContractStatus>> getContactStatus() {
        return Observable.fromCallable(new Callable<ArrayList<ContractStatus>>() {
            @Override
            public ArrayList<ContractStatus> call() throws Exception {
                ArrayList<ContractStatus> contractStatusList = new ArrayList<>();
                ContractStatus contractStatus;
                try {

                    Cursor c = dbUtil.selectSQL("SELECT ListId,ListCode,ListName from StandardListMaster where ListType='RETAILER_CONTRACT_STATUS'");
                    if (c.getCount() > 0) {
                        while (c.moveToNext()) {
                            contractStatus = new ContractStatus();
                            contractStatus.setListId(c.getInt(0));
                            contractStatus.setListName(c.getString(2));
                            contractStatusList.add(contractStatus);
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
                            + RetailerID + " AND Date = " + getStringQueryParam(currentDate) + " AND Upload = " + getStringQueryParam("N"));
                    if (headerCursor.getCount() > 0) {
                        headerCursor.moveToNext();
                        tid = headerCursor.getString(0);
                        headerCursor.close();
                    }
                    c = dbUtil.selectSQL("select code, value from RetailerEditDetail RED INNER JOIN RetailerEditHeader REH ON REH.tid=RED.tid where REH.retailerid="
                            + RetailerID + " and REH.tid=" + getStringQueryParam(tid));
                    if (c != null) {
                        while (c.moveToNext()) {
                            mPreviousProfileChangesList.put(c.getString(0), c.getString(1));
                        }
                    }
                    c.close();

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
                    c.close();
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
    public Observable<ArrayList<RetailerMasterBO>> getLinkRetailerForADistributor(int distId) {
        return Observable.fromCallable(new Callable<ArrayList<RetailerMasterBO>>() {
            @Override
            public ArrayList<RetailerMasterBO> call() throws Exception {
                ArrayList<RetailerMasterBO> mLinkRetailerList = new ArrayList<>();
                try {

                    String sb = "select Distributorid ,retailerid,name,latitude,longitude,pincode from linkretailermaster where Distributorid=" + distId +
                            " order by Distributorid ";
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
                    initDb();
                    String sql = "select id,name from RetailerFlexValues where type = " + getStringQueryParam(type);
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
                    c.close();

                } catch (Exception e) {
                    Commons.printException(e);
                }
                return flexValues;
            }
        });

    }


    @Override
    public Single<Boolean> saveNearByRetailers(String id, ArrayList<String> nearByRetailers) {

        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {

                try {
                    initDb();
                    String columnsNew = "rid,nearbyrid,upload";
                    String values;
                    for (int j = 0; j < nearByRetailers.size(); j++) {
                        values = getStringQueryParam(id) + "," + nearByRetailers.get(j) + "," + getStringQueryParam("N");
                        dbUtil.insertSQL("NearByRetailers", columnsNew, values);
                    }
                    return true;

                } catch (Exception e) {
                    Commons.printException(e);
                }

                return false;
            }
        });


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
                    c.close();
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
                    String sql = "select  ProductId from RetailerPriorityProducts where retailerId=" + getStringQueryParam(retailerId);
                    Cursor c = dbUtil.selectSQL(sql);
                    if (c.getCount() > 0) {
                        priorityproductList = new ArrayList<>();
                        while (c.moveToNext()) {
                            priorityproductList.add(c.getString(0));
                        }
                    }
                    c.close();
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
                    String sql = "select  ProductId from RetailerPriorityProducts where retailerId=" + getStringQueryParam(retailerId);
                    Cursor c = dbUtil.selectSQL(sql);
                    if (c.getCount() > 0) {
                        priorityproductList = new ArrayList<>();
                        while (c.moveToNext()) {
                            priorityproductList.add(c.getString(0));
                        }
                    }
                    c.close();
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
                                String sql = "select ProductId from RetailerEditPriorityProducts where status = 'N' and retailerId=" + getStringQueryParam(retailerId);
                                Cursor c = dbUtil.selectSQL(sql);
                                if (c.getCount() > 0) {
                                    priorityproductList = new ArrayList<>();
                                    while (c.moveToNext()) {
                                        priorityproductList.add(c.getString(0));
                                    }
                                }
                                c.close();
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
    public Observable<Vector<ConfigureBO>> downloadNewOutletConfiguration() {
        return Observable.fromCallable(new Callable<Vector<ConfigureBO>>() {
            @Override
            public Vector<ConfigureBO> call() throws Exception {
                ConfigureBO ConfigureBO;
                Vector<ConfigureBO> profileConfig = new Vector<>();
               /* try {
                    dbUtil.createDataBase();
                    dbUtil.openDataBase();
                    Cursor c;
                    SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
                    String language = sharedPrefs.getString("languagePref", ApplicationConfigs.LANGUAGE);
                    StringBuilder sb = new StringBuilder();
                    sb.append("select HHTCode, flag , MName , MNumber ,RField,haslink,RField6,Regex from HhtMenuMaster where MenuType= 'MENU_NEW_RET'");
                    sb.append("  and Flag = 1 and lang=");
                    sb.append(AppUtils.getStringQueryParam(language));
                    if (configurationMasterHelper.IS_CHANNEL_SELECTION_NEW_RETAILER) {
                        sb.append(" and subchannelid=");
                        sb.append(channelid);
                        sb.append(" ");
                    }
                    sb.append(" order by Mnumber");
                    c = dbUtil.selectSQL(sb.toString());
                    if (c != null) {
                        while (c.moveToNext()) {
                            ConfigureBO = new ConfigureBO();
                            ConfigureBO.setConfigCode(c.getString(0));
                            ConfigureBO.setFlag(c.getInt(1));
                            ConfigureBO.setMenuName(c.getString(2));
                            ConfigureBO.setMenuNumber((c.getString(3)));
                            ConfigureBO.setMandatory((c.getInt(4)));
                            ConfigureBO.setHasLink(c.getInt(5));
                            ConfigureBO.setRField6(String.valueOf(c.getInt(6)));
                            String str = c.getString(7);
                            if (str != null && !str.isEmpty()) {
                                if (str.contains("<") && str.contains(">")) {

                                    String minlen = str.substring(str.indexOf("<") + 1, str.indexOf(">"));
                                    if (!minlen.isEmpty()) {
                                        try {
                                            ConfigureBO.setMaxLengthNo(SDUtil.convertToInt(minlen));
                                        } catch (Exception ex) {
                                            Commons.printException("min len in new outlet helper", ex);
                                        }
                                    }
                                }
                            }
                            ConfigureBO.setRegex(c.getString(7));
                            profileConfig.add(ConfigureBO);
                        }
                        c.close();
                    }
                    dbUtil.closeDB();
                } catch (Exception e) {
                    Commons.printException("" + e);
                }*/
                return profileConfig;
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
                            + getStringQueryParam(currentDate)
                            + " AND Upload = "
                            + getStringQueryParam("N"));

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
    public Single<Boolean> updateRetailer(final String tid, final String RetailerID, final String currentDate) {

        final String insertHeader = "insert into RetailerEditHeader (tid,RetailerId,date)" +
                "values (" + getStringQueryParam(tid) + "," + RetailerID + "," + getStringQueryParam(currentDate) + ")";

        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    dbUtil.deleteSQL(DataMembers.tbl_RetailerEditHeader, " Tid=" + getStringQueryParam(tid), false);
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
    public Single<Boolean> deleteNewRetailer(String getId) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    dbUtil.createDataBase();
                    dbUtil.openDataBase();
                    Set<String> keys = DataMembers.uploadNewRetailerColumn.keySet();

                    for (String tableName : keys) {
                        if (tableName.equals(DataMembers.tbl_nearbyRetailer) ||
                                tableName.equals(DataMembers.tbl_retailerPotential)) {
                            dbUtil.deleteSQL(tableName, "rid ='" + getId + "'", false);
                        } else if (tableName.equals(DataMembers.tbl_orderHeaderRequest)) {
                            dbUtil.executeQ("delete from " + DataMembers.tbl_orderDetailRequest + " where OrderID in (select OrderID from "
                                    + DataMembers.tbl_orderHeaderRequest
                                    + " where RetailerID = '" + getId + "')");
                            dbUtil.deleteSQL(tableName, "RetailerID ='" + getId + "'", false);
                        } else {
                            dbUtil.deleteSQL(tableName, "RetailerID ='" + getId + "'", false);
                        }
                    }
                    return true;
                } catch (Exception e) {
                    Commons.printException(e);
                    return false;
                }
            }
        });
    }

    @Override
    public Single<String> syncNewOutlet(String retailerId) {
        final boolean[] isDayClosed = {false};
        return synchronizationDataManager.getSyncUrl("UPLDRET").flatMap(uploadUrl -> {
            if (isNullOrEmpty(uploadUrl))
                return Single.fromCallable(() -> "-2");
            return dataManager.isDayClosed()
                    .flatMap((Function<Boolean, SingleSource<JSONObject>>) aBoolean -> {
                        isDayClosed[0] = aBoolean;
                        return generateBodyInformation(retailerId);
                    }).flatMap(body -> {
                        JSONFormatter headerInformation = getHeaderInformation(dataManager.getDeviceId(), isDayClosed[0], body);
                        return synchronizationDataManager.uploadDataToServer(headerInformation.getDataInJson(), body.toString(), uploadUrl)
                                .flatMap(responseVector -> Single.fromCallable(() -> {
                                    String rid = "";
                                    if (responseVector.size() > 0) {
                                        for (String s : responseVector) {
                                            JSONObject jsonObject = new JSONObject(s);

                                            Iterator itr = jsonObject.keys();
                                            while (itr.hasNext()) {
                                                String key = (String) itr.next();
                                                if (key.equals("RetailerId")) {
                                                    rid = jsonObject.getString("RetailerId");
                                                } else if (key.equals("ErrorCode")) {
                                                    String tokenResponse = jsonObject.getString("ErrorCode");
                                                    if (tokenResponse.equals(SynchronizationHelper.INVALID_TOKEN)
                                                            || tokenResponse.equals(SynchronizationHelper.TOKEN_MISSINIG)
                                                            || tokenResponse.equals(SynchronizationHelper.EXPIRY_TOKEN_CODE)) {
                                                        return "-1";
                                                    }
                                                }
                                            }

                                        }
                                    }
                                    return rid;
                                }));
                    });
        });
    }


    private Single<JSONObject> generateBodyInformation(String retailerId) {
        return Single.fromCallable(new Callable<JSONObject>() {
            @Override
            public JSONObject call() throws Exception {
                JSONObject jsonobj = new JSONObject();
                Set<String> keys = DataMembers.uploadNewRetailerColumn.keySet();
                for (String tableName : keys) {
                    JSONArray jsonArray = prepareDataForNewRetailerJSONUpload(tableName,
                            DataMembers.uploadNewRetailerColumn.get(tableName), retailerId);
                    if (jsonArray.length() > 0)
                        jsonobj.put(tableName, jsonArray);
                }
                return jsonobj;
            }
        });

    }

    private JSONFormatter getHeaderInformation(String deviceId, boolean isDayClosed, JSONObject body) {
        JSONFormatter jsonFormatter = new JSONFormatter("HeaderInformation");
        jsonFormatter.addParameter("DeviceId", deviceId);
        jsonFormatter.addParameter("LoginId", dataManager.getUser().getLoginName());
        jsonFormatter.addParameter("VersionCode", dataManager.getAppVersionNumber());
        jsonFormatter.addParameter(SynchronizationHelper.VERSION_NAME, dataManager.getAppVersionName());
        jsonFormatter.addParameter("DistributorId", dataManager.getUser().getDistributorid());
        jsonFormatter.addParameter("OrganisationId", dataManager.getUser().getOrganizationId());
        jsonFormatter.addParameter("MobileDateTime", Utils.getDate("yyyy/MM/dd HH:mm:ss"));
        jsonFormatter.addParameter("MobileUTCDateTime", Utils.getGMTDateTime("yyyy/MM/dd HH:mm:ss"));
        jsonFormatter.addParameter("DataValidationKey", synchronizationDataManager.generateChecksum(body.toString()));

        if (!"0".equals(dataManager.getUser().getBackupSellerID())) {
            jsonFormatter.addParameter("UserId", dataManager.getUser().getBackupSellerID());
            jsonFormatter.addParameter("WorkingFor", dataManager.getUser().getUserid());
        } else {
            jsonFormatter.addParameter("UserId", dataManager.getUser().getUserid());
        }
        jsonFormatter.addParameter("VanId", dataManager.getUser().getVanId());
        String LastDayClose = "";
        if (isDayClosed) {
            LastDayClose = dataManager.getUser().getDownloadDate();
        }
        jsonFormatter.addParameter("LastDayClose", LastDayClose);
        jsonFormatter.addParameter("BranchId", dataManager.getUser().getBranchId());
        jsonFormatter.addParameter("DownloadedDataDate", dataManager.getUser().getDownloadDate());


        return jsonFormatter;
    }

    @Override
    public Single<String> uploadNewOutlet(String retailerId, String deviceId, BusinessModel businessModel,
                                          AppDataProvider appDataProvider) {

        return Single.create(new SingleOnSubscribe<String>() {
            @Override
            public void subscribe(SingleEmitter<String> emitter) throws Exception {
                String rid = "";
                try {
                    dbUtil.createDataBase();
                    dbUtil.openDataBase();

                    JSONObject jsonobj = new JSONObject();
                    Set<String> keys = DataMembers.uploadNewRetailerColumn.keySet();

                    for (String tableName : keys) {
                        JSONArray jsonArray = prepareDataForNewRetailerJSONUpload(tableName,
                                DataMembers.uploadNewRetailerColumn.get(tableName), retailerId);
                        if (jsonArray.length() > 0)
                            jsonobj.put(tableName, jsonArray);
                    }

                    Commons.print("jsonObjData.toString():0:" + jsonobj.toString());

                    JSONFormatter jsonFormatter = new JSONFormatter("HeaderInformation");
                    jsonFormatter.addParameter("DeviceId", deviceId);
                    jsonFormatter.addParameter("LoginId", appDataProvider.getUser().getLoginName());
                    jsonFormatter.addParameter("VersionCode", businessModel.getApplicationVersionNumber());
                    jsonFormatter.addParameter(SynchronizationHelper.VERSION_NAME, businessModel.getApplicationVersionName());
                    jsonFormatter.addParameter("DistributorId", appDataProvider.getUser().getDistributorid());
                    jsonFormatter.addParameter("OrganisationId", appDataProvider.getUser().getOrganizationId());
                    jsonFormatter.addParameter("MobileDateTime", Utils.getDate("yyyy/MM/dd HH:mm:ss"));
                    jsonFormatter.addParameter("MobileUTCDateTime", Utils.getGMTDateTime("yyyy/MM/dd HH:mm:ss"));
                    if (!"0".equals(appDataProvider.getUser().getBackupSellerID())) {
                        jsonFormatter.addParameter("UserId", appDataProvider.getUser().getBackupSellerID());
                        jsonFormatter.addParameter("WorkingFor", appDataProvider.getUser().getUserid());
                    } else {
                        jsonFormatter.addParameter("UserId", appDataProvider.getUser().getUserid());
                    }
                    jsonFormatter.addParameter("VanId", appDataProvider.getUser().getVanId());
                    String LastDayClose = "";
                    if (businessModel.synchronizationHelper.isDayClosed()) {
                        LastDayClose = appDataProvider.getUser().getDownloadDate();
                    }
                    jsonFormatter.addParameter("LastDayClose", LastDayClose);
                    jsonFormatter.addParameter("BranchId", appDataProvider.getUser().getBranchId());
                    jsonFormatter.addParameter("DownloadedDataDate", appDataProvider.getUser().getDownloadDate());
                    jsonFormatter.addParameter("DataValidationKey", businessModel.synchronizationHelper.generateChecksum(jsonobj.toString()));
                    Commons.print(jsonFormatter.getDataInJson());
                    String appendurl = businessModel.synchronizationHelper.getUploadUrl("UPLDRET");
                    if (appendurl.length() == 0)
                        emitter.onSuccess(2 + "");
                    Vector<String> responseVector = businessModel.synchronizationHelper
                            .getUploadResponse(jsonFormatter.getDataInJson(), jsonobj.toString(), appendurl);

                    if (responseVector.size() > 0) {
                        for (String s : responseVector) {
                            JSONObject jsonObject = new JSONObject(s);

                            Iterator itr = jsonObject.keys();
                            while (itr.hasNext()) {
                                String key = (String) itr.next();
                                if (key.equals("RetailerId")) {
                                    rid = jsonObject.getString("RetailerId");
                                } else if (key.equals("ErrorCode")) {
                                    String tokenResponse = jsonObject.getString("ErrorCode");
                                    if (tokenResponse.equals(SynchronizationHelper.INVALID_TOKEN)
                                            || tokenResponse.equals(SynchronizationHelper.TOKEN_MISSINIG)
                                            || tokenResponse.equals(SynchronizationHelper.EXPIRY_TOKEN_CODE)) {
                                        emitter.onSuccess(-1 + "");
                                    }
                                }
                            }

                        }
                    }
                } catch (SQLException | JSONException e) {
                    Commons.printException("" + e);
                }
                emitter.onSuccess(rid);
            }
        });
    }

    private JSONArray prepareDataForNewRetailerJSONUpload(String tableName, String columns, String retailerID) {

        //Message msg;
        JSONArray ohRowsArray = new JSONArray();

        try {
            Cursor cursor;
            String columnArray[] = columns.split(",");

            String retailerColumn = "RetailerID = " + getStringQueryParam(retailerID);
            if (tableName.equals(DataMembers.tbl_nearbyRetailer) || tableName.equals(DataMembers.tbl_retailerPotential)) {
                retailerColumn = "rid = " + getStringQueryParam(retailerID);
            }
            initDb();

            String sql = "select " + columns + " from " + tableName
                    + " where upload='N' and " + retailerColumn;
            cursor = dbUtil.selectSQL(sql);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        JSONObject jsonObjRow = new JSONObject();
                        int count = 0;
                        for (String col : columnArray) {
                            String value = cursor.getString(count);
                            jsonObjRow.put(col, value);
                            count++;
                        }
                        ohRowsArray.put(jsonObjRow);
                    }
                }

                cursor.close();

            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        shutDownDb();
        return ohRowsArray;
    }

    private Single<Integer> getRetailerEditDetailCount(final String tid) {
        return Single.fromCallable(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                try {
                    Cursor c = dbUtil.selectSQL("SELECT code FROM " + DataMembers.tbl_RetailerEditDetail + " where Tid=" + getStringQueryParam(tid));
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
                    Cursor c = dbUtil.selectSQL("SELECT status FROM " + DataMembers.tbl_nearbyEditRequest + " where Tid=" + getStringQueryParam(tid));
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
                    Cursor c = dbUtil.selectSQL("SELECT status FROM " + DataMembers.tbl_RetailerEditPriorityProducts + " where Tid=" + getStringQueryParam(tid));
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
                    Cursor c = dbUtil.selectSQL("SELECT status FROM RetailerEditAttribute" + " where Tid=" + getStringQueryParam(tid));
                    if (c != null)
                        return c.getCount();
                } catch (Exception e) {
                    Commons.printException("" + e);
                }
                return 0;
            }
        });
    }

    public Single<Boolean> checkRetailerAlreadyAvailable(String retailerName, String pincode) {

        return Single.fromCallable(() -> {
            try {
                Cursor c = dbUtil
                        .selectSQL("SELECT A.pincode FROM  RetailerAddress A"
                                + " WHERE A.RetailerId IN (SELECT RetailerId FROM RetailerMaster WHERE RetailerName = '" + retailerName + "')"
                                + " AND A.pincode = '" + pincode + "'");
                if (c.getCount() > 0) {
                    c.close();
                    return true;
                } else
                    c.close();
            } catch (Exception e) {
                Commons.printException("" + e);
            }
            return false;
        });

    }

    @Override
    public Observable<ArrayList<CensusLocationBO>> fetchCensusLocationDetails() {
        return Observable.fromCallable(() -> {
            int pincodeLevel = 0;
            ArrayList<CensusLocationBO> pinCodeList = new ArrayList<>();
            ArrayList<CensusLocationBO> pinCodeTempList = new ArrayList<>();
            try {
                Cursor cursor = dbUtil.selectSQL("select LevelId from CensusLocationLevel  where sequence = (select MAX(Sequence) from censuslocationlevel)");
                if (cursor.getCount() > 0) {
                    if (cursor.moveToNext())
                        pincodeLevel = cursor.getInt(0);
                }
                cursor = dbUtil.selectSQL("select id,name,levelid,parentid,pincode from CensusLocationMaster where levelid =" + pincodeLevel);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        CensusLocationBO censusBO = new CensusLocationBO();
                        censusBO.setId(cursor.getString(0));
                        censusBO.setLocationName(cursor.getString(1));
                        censusBO.setLevelId(cursor.getString(2));
                        censusBO.setParentId(cursor.getString(3));
                        censusBO.setPincode(cursor.getString(4));
                        pinCodeList.add(censusBO);
                    }
                }
                cursor = dbUtil.selectSQL("select id,name,levelid,parentid,(select sequence from CensusLocationLevel where levelid = A.levelid)" +
                        " as sequence from CensusLocationMaster A where levelid !=" + pincodeLevel + " order by sequence desc");
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        CensusLocationBO censusBO = new CensusLocationBO();
                        censusBO.setId(cursor.getString(0));
                        censusBO.setLocationName(cursor.getString(1));
                        censusBO.setLevelId(cursor.getString(2));
                        censusBO.setParentId(cursor.getString(3));
                        pinCodeTempList.add(censusBO);
                    }
                }
                cursor.close();

            } catch (Exception ignored) {

            }

            String districtId;
            String stateId;
            String countryId;

            for (CensusLocationBO pinCodeBO : pinCodeList) {
                stateId = "";
                countryId = "";
                districtId = pinCodeBO.getParentId();
                for (CensusLocationBO tempBO : pinCodeTempList) {
                    if (districtId.equals(tempBO.getId())) {
                        pinCodeBO.setDistrict(tempBO.getLocationName());
                        stateId = tempBO.getParentId();
                    } else if (stateId.equals(tempBO.getId())) {
                        pinCodeBO.setState(tempBO.getLocationName());
                        countryId = tempBO.getParentId();
                    } else if (countryId.equals(tempBO.getId())) {
                        pinCodeBO.setCountry(tempBO.getLocationName());
                        break;
                    }
                }
            }


            return pinCodeList;
        });
    }

    @Override
    public Single<Boolean> saveNewOutletImages(String uId, NewOutletBO newOutletBO) {
        return Single.fromCallable(() -> {

            String column, value;

            try {
                for (int i = 0; i < newOutletBO.ImageId.size(); i++) {

                    column = "RetailerID,ListId,ImageName,upload";

                    value = getStringQueryParam(uId)
                            + ","
                            + newOutletBO.ImageId.get(i)
                            + ","
                            + getStringQueryParam("/RetailerImages/"
                            + dataManager.getUser().getDownloadDate().replace("/", "")
                            + "/"
                            + dataManager.getUser().getUserid() + "/"
                            + (newOutletBO.ImageName.get(i))) + "," + getStringQueryParam("N");

                    dbUtil.insertSQL("NewOutletImage", column, value);
                }

                return true;
            } catch (Exception ignored) {
                return false;
            }
        });
    }

    @Override
    public Single<Boolean> deleteNewRetailerTablesForEdit(String uId, String retailerId) {
        return Single.fromCallable(() -> {

            try {
                Cursor getCpidCursor = dbUtil.selectSQL("Select CPId from RetailerContact where retailerId=" + getStringQueryParam(retailerId));

                if (getCpidCursor != null && getCpidCursor.getCount() > 0) {
                    while (getCpidCursor.moveToNext()) {
                        dbUtil.deleteSQL("ContactAvailability", "CPId=" + getCpidCursor.getString(0), false);
                    }
                }
                dbUtil.deleteSQL("RetailerMaster", "retailerId=" + getStringQueryParam(retailerId), false);
                dbUtil.deleteSQL("RetailerContact", "retailerId=" + getStringQueryParam(retailerId), false);
                dbUtil.deleteSQL("RetailerAddress", "retailerId=" + getStringQueryParam(retailerId), false);
                dbUtil.deleteSQL("RetailerAttribute", "retailerId=" + getStringQueryParam(retailerId), false);
                dbUtil.updateSQL("Update NewRetailerSurveyResultHeader set retailerID = '" + uId + "' where retailerID = '" + retailerId + "'");
                dbUtil.updateSQL("Update NewRetailerSurveyResultDetail set retailerID = '" + uId + "' where retailerID = '" + retailerId + "'");

                return true;
            } catch (Exception ignored) {
                return false;
            }
        });
    }

    @Override
    public Single<Boolean> saveNewOutlet(String uId, NewOutletBO outlet) {
        return Single.fromCallable(() -> {
            String column, value;
            try {

                column = "RetailerID,RetailerName,channelID,subchannelid,beatid,"
                        + DataMembers.VISIT_DAYS_COLUMN_NAME + ",LocationId," +
                        "creditlimit,RPTypeId,tinnumber,RField3,distributorId,TaxTypeid," +
                        "contractstatuslovid,classid,AccountId,is_new,Upload,creditPeriod,inSEZ,GSTnumber,RField5,RField6,TinExpDate," +
                        "pan_number,food_licence_number,food_licence_exp_date,DLNo,DLNoExpDate,RField4,RField7,userid";

                int userid = outlet.getUserId();
                if (userid == 0)
                    userid = dataManager.getUser().getUserid();

                value = getStringQueryParam(uId)
                        + "," + getStringQueryParam(outlet.getOutletName())
                        + "," + outlet.getChannel()
                        + "," + outlet.getSubChannel()
                        + "," + getStringQueryParam(outlet.getRouteid() + "")
                        + "," + getStringQueryParam(outlet.getVisitDays())
                        + "," + outlet.getLocid()
                        + "," + outlet.getCreditLimit()
                        + "," + outlet.getPayment()
                        + "," + getStringQueryParam(outlet.getTinno())
                        + "," + getStringQueryParam(outlet.getRfield3())
                        + "," + getStringQueryParam(outlet.getDistid())
                        + "," + getStringQueryParam(outlet.getTaxTypeId())
                        + "," + outlet.getContractStatuslovid()
                        + "," + outlet.getClassTypeId()
                        + "," + 0
                        + "," + getStringQueryParam("Y")
                        + "," + getStringQueryParam("N")
                        + "," + outlet.getCreditDays()
                        + "," + outlet.getIsSEZ()
                        + "," + getStringQueryParam(outlet.getGstNum())
                        + "," + getStringQueryParam(outlet.getRfield5())
                        + "," + getStringQueryParam(outlet.getRfield6())
                        + "," + (outlet.getTinExpDate() == null || outlet.getTinExpDate().isEmpty() ? null : getStringQueryParam(outlet.getTinExpDate()))
                        + "," + getStringQueryParam(outlet.getPanNo())
                        + "," + getStringQueryParam(outlet.getFoodLicenseNo())
                        + "," + (outlet.getFlExpDate() == null || outlet.getFlExpDate().isEmpty() ? null : getStringQueryParam(outlet.getFlExpDate()))
                        + "," + getStringQueryParam(outlet.getDrugLicenseNo())
                        + "," + (outlet.getDlExpDate() == null || outlet.getDlExpDate().isEmpty() ? null : getStringQueryParam(outlet.getDlExpDate()))
                        + "," + getStringQueryParam(outlet.getrField4())
                        + "," + getStringQueryParam(outlet.getrField7())
                        + "," + getStringQueryParam(userid + "");


                dbUtil.insertSQL("RetailerMaster", column, value);


                return true;
            } catch (Exception ignored) {
                return false;
            }

        });
    }

    @Override
    public Single<Boolean> saveNewOutletContactInformation(String uId, NewOutletBO outlet) {
        return Single.fromCallable(() -> {
            String column, value;
            column = "RetailerID,contactname,ContactName_LName,contactNumber," +
                    "contact_title,contact_title_lovid,IsPrimary,Email,Upload,salutationLovId,IsEmailNotificationReq,CPID";
            try {
                if (outlet.getContactpersonname() != null && !outlet.getContactpersonname().trim().equals("")) {
                    value = getStringQueryParam(uId)
                            + "," + getStringQueryParam(outlet.getContactpersonname())
                            + "," + getStringQueryParam(outlet.getContactpersonnameLastName())
                            + "," + getStringQueryParam(outlet.getPhone())
                            + "," + getStringQueryParam(outlet.getContact1title())
                            + "," + outlet.getContact1titlelovid()
                            + "," + 1
                            + "," + getStringQueryParam("")
                            + "," + getStringQueryParam("N")
                            + "," + getStringQueryParam("")
                            + "," + getStringQueryParam("")
                            + "," + getStringQueryParam("");
                    dbUtil.insertSQL("RetailerContact", column, value);
                }
                if (outlet.getContactpersonname2() != null && !outlet.getContactpersonname2().trim().equals("")) {
                    value = getStringQueryParam(uId)
                            + "," + getStringQueryParam(outlet.getContactpersonname2())
                            + "," + getStringQueryParam(outlet.getContactpersonname2LastName())
                            + "," + getStringQueryParam(outlet.getPhone2())
                            + "," + getStringQueryParam(outlet.getContact2title())
                            + "," + outlet.getContact2titlelovid()
                            + "," + 0
                            + "," + getStringQueryParam("")
                            + "," + getStringQueryParam("N")
                            + "," + getStringQueryParam("")
                            + "," + getStringQueryParam("")
                            + "," + getStringQueryParam("");
                    dbUtil.insertSQL("RetailerContact", column, value);
                }
                return true;
            } catch (Exception ignored) {
                return false;
            }
        });
    }

    @Override
    public Single<Boolean> saveNewOutletContactTabInformation(String uId, ArrayList<RetailerContactBo> retailerContactList) {
        return Single.fromCallable(() -> {
            String column, value;
            column = "RetailerID,contactname,ContactName_LName,contactNumber," +
                    "contact_title,contact_title_lovid,IsPrimary,Email,Upload,salutationLovId,IsEmailNotificationReq,CPID";
            try {

                for (RetailerContactBo retailerContactBo : retailerContactList) {

                    value = getStringQueryParam(uId)
                            + "," + getStringQueryParam(retailerContactBo.getFistname())
                            + "," + getStringQueryParam(retailerContactBo.getLastname())
                            + "," + getStringQueryParam(retailerContactBo.getContactNumber())
                            + "," + getStringQueryParam(retailerContactBo.getTitle())
                            + "," + getStringQueryParam(retailerContactBo.getContactTitleLovId().equalsIgnoreCase("-1") ? "0" : retailerContactBo.getContactTitleLovId())
                            + "," + retailerContactBo.getIsPrimary()
                            + "," + getStringQueryParam(retailerContactBo.getContactMail())
                            + "," + getStringQueryParam("N")
                            + "," + getStringQueryParam(retailerContactBo.getContactSalutationId())
                            + "," + getStringQueryParam(retailerContactBo.getIsEmailPrimary() + "")
                            + "," + getStringQueryParam(retailerContactBo.getCpId());
                    dbUtil.insertSQL("RetailerContact", column, value);

                    if (retailerContactBo.getContactAvailList().size() > 0)
                        addContactAvail(uId, retailerContactBo);
                }

                return true;
            } catch (Exception ignored) {
                return false;
            }

        });
    }

    private void addContactAvail(String uId, RetailerContactBo retailerContactBo) {
        String column = "CPAId,CPId,Day,StartTime,EndTime,isLocal,upload";

        for (RetailerContactAvailBo retailerContactAvailBo : retailerContactBo.getContactAvailList()) {

            String value = getStringQueryParam(uId)
                    + "," + getStringQueryParam(retailerContactBo.getCpId())
                    + "," + getStringQueryParam(retailerContactAvailBo.getDay())
                    + "," + getStringQueryParam(retailerContactAvailBo.getFrom())
                    + "," + getStringQueryParam(retailerContactAvailBo.getTo())
                    + "," + getStringQueryParam("1")
                    + "," + getStringQueryParam("N");

            dbUtil.insertSQL("ContactAvailability", column, value);
        }
    }

    @Override
    public Single<Boolean> saveNewOutletAddressInformation(String uId, NewOutletBO outlet) {
        return Single.fromCallable(() -> {
            String column, value;
            column = "RetailerID,Address1,Address2,Address3,ContactNumber,City,latitude,longitude,"
                    + "email,FaxNo,pincode,State,Upload,IsPrimary,AddressTypeID,Region,Country,Mobile,District";
            try {

                String lattitude = (outlet.getNewOutletlattitude() + "").contains("E")
                        ? (SDUtil.truncateDecimal(outlet.getNewOutletlattitude(), -1) + "").substring(0, 20)
                        : ((outlet.getNewOutletlattitude() + "").length() > 20
                        ? (outlet.getNewOutletlattitude() + "").substring(0, 20)
                        : (outlet.getNewOutletlattitude() + ""));

                String longitude = (outlet.getNewOutletLongitude() + "").contains("E")
                        ? (SDUtil.truncateDecimal(outlet.getNewOutletLongitude(), -1) + "").substring(0, 20)
                        : ((outlet.getNewOutletLongitude() + "").length() > 20
                        ? (outlet.getNewOutletLongitude() + "").substring(0, 20)
                        : (outlet.getNewOutletLongitude() + ""));

                if (outlet.getmAddressByTag() != null) {
                    for (String addressType : outlet.getmAddressByTag().keySet()) {
                        AddressBO addressBO = outlet.getmAddressByTag().get(addressType);
                        value = getStringQueryParam(uId)
                                + "," + getStringQueryParam(addressBO.getAddress1())
                                + "," + getStringQueryParam(addressBO.getAddress2())
                                + "," + getStringQueryParam(addressBO.getAddress3())
                                + "," + getStringQueryParam(addressBO.getPhone())
                                + "," + getStringQueryParam(addressBO.getCity())
                                + "," + getStringQueryParam(lattitude)
                                + "," + getStringQueryParam(longitude)
                                + "," + getStringQueryParam(addressBO.getEmail())
                                + "," + getStringQueryParam(addressBO.getFax())
                                + "," + getStringQueryParam(addressBO.getPincode())
                                + "," + getStringQueryParam(addressBO.getState())
                                + "," + getStringQueryParam("N")
                                + "," + 1
                                + "," + addressType
                                + "," + getStringQueryParam(outlet.getRegion())
                                + "," + getStringQueryParam(outlet.getCountry())
                                + "," + getStringQueryParam(outlet.getMobile())
                                + "," + getStringQueryParam(outlet.getDistrict());


                        dbUtil.insertSQL("RetailerAddress", column, value);
                    }
                } else {

                    value = getStringQueryParam(uId)
                            + "," + getStringQueryParam(outlet.getAddress())
                            + "," + getStringQueryParam(outlet.getAddress2())
                            + "," + getStringQueryParam(outlet.getAddress3())
                            + "," + getStringQueryParam(outlet.getPhone())
                            + "," + getStringQueryParam(outlet.getCity())
                            + "," + getStringQueryParam(lattitude)
                            + "," + getStringQueryParam(longitude)
                            + "," + getStringQueryParam(outlet.getEmail())
                            + "," + getStringQueryParam(outlet.getFax())
                            + "," + getStringQueryParam(outlet.getPincode())
                            + "," + getStringQueryParam(outlet.getState())
                            + "," + getStringQueryParam("N")
                            + "," + 1
                            + "," + 0
                            + "," + getStringQueryParam(outlet.getRegion())
                            + "," + getStringQueryParam(outlet.getCountry())
                            + "," + getStringQueryParam(outlet.getMobile())
                            + "," + getStringQueryParam(outlet.getDistrict());

                    dbUtil.insertSQL("RetailerAddress", column, value);

                }

                return true;
            } catch (Exception ignored) {
                return false;
            }

        });
    }

    @Override
    public Single<Boolean> savePriorityProducts(String uId, ArrayList<StandardListBO> productIdList) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {

                try {

                    String columns = "RetailerId,ProductId,LevelId,upload";
                    StringBuffer sb;
                    if (productIdList != null) {
                        for (StandardListBO bo : productIdList) {
                            sb = new StringBuffer();
                            sb.append(getStringQueryParam(uId));
                            sb.append(",");
                            sb.append(bo.getListID());
                            sb.append(",");
                            sb.append(bo.getListCode());
                            sb.append(",'N'");
                            dbUtil.insertSQL("RetailerPriorityProducts", columns, sb.toString());

                        }
                    }
                    return true;
                } catch (Exception ignored) {
                    return false;
                }


            }
        });
    }

    @Override
    public Single<Boolean> saveOrderDetails(String uId, OrderHeader orderHeader, ArrayList<ProductMasterBO> productMasterBOS) {
        String id = dataManager.getUser().getUserid() + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                String column, value;

                column = "OrderID, OrderDate, RetailerID, DistributorId, OrderValue,LinesPerCall,TotalWeight,Remarks,OrderTime";


                value = id
                        + "," + getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL))
                        + "," + getStringQueryParam(uId)
                        + "," + dataManager.getUser().getDistributorid()
                        + "," + orderHeader.getOrderValue()
                        + "," + orderHeader.getLinesPerCall()
                        + "," + orderHeader.getTotalWeight()
                        + "," + getStringQueryParam(dataManager.getOrderHeaderNote())
                        + "," + getStringQueryParam(DateTimeUtils.now(DateTimeUtils.TIME));

                try {
                    dbUtil.insertSQL("OrderHeaderRequest", column, value);
                    return true;
                } catch (Exception ignored) {
                    return false;
                }

            }
        }).flatMap((Function<Boolean, SingleSource<Boolean>>) aBoolean -> {
            if (!aBoolean)
                return Single.fromCallable(() -> false);
            else {
                return Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        String column, value;

                        column = "OrderID, ProductID, Qty,uomid,Price,LineValue, Weight,uomcount,HsnCode";

                        try {
                            for (ProductMasterBO productMasterBO : productMasterBOS) {
                                if (productMasterBO.getOrderedPcsQty() > 0) {
                                    value = id
                                            + "," + getStringQueryParam(productMasterBO.getProductID())
                                            + "," + productMasterBO.getOrderedPcsQty()
                                            + "," + productMasterBO.getPcUomid()
                                            + "," + productMasterBO.getSrp()
                                            + "," + productMasterBO.getOrderedPcsQty() * productMasterBO.getSrp()
                                            + "," + productMasterBO.getOrderedPcsQty() * productMasterBO.getWeight()
                                            + ",1"
                                            + "," + getStringQueryParam(productMasterBO.getHsnCode());
                                    dbUtil.insertSQL("OrderDetailRequest", column, value);
                                }
                                if (productMasterBO.getOrderedCaseQty() > 0) {
                                    value = id
                                            + "," + getStringQueryParam(productMasterBO.getProductID())
                                            + "," + productMasterBO.getOrderedCaseQty()
                                            + "," + productMasterBO.getCaseUomId()
                                            + "," + productMasterBO.getCsrp()
                                            + "," + productMasterBO.getOrderedCaseQty() * productMasterBO.getCsrp()
                                            + "," + (productMasterBO.getOrderedCaseQty() * productMasterBO.getCaseSize()) * productMasterBO.getWeight()
                                            + "," + productMasterBO.getCaseSize()
                                            + "," + getStringQueryParam(productMasterBO.getHsnCode());
                                    dbUtil.insertSQL("OrderDetailRequest", column, value);
                                }
                                if (productMasterBO.getOrderedOuterQty() > 0) {
                                    value = id
                                            + "," + getStringQueryParam(productMasterBO.getProductID())
                                            + "," + productMasterBO.getOrderedOuterQty()
                                            + "," + productMasterBO.getOuUomid()
                                            + "," + productMasterBO.getOsrp()
                                            + "," + productMasterBO.getOrderedOuterQty() * productMasterBO.getOsrp()
                                            + "," + (productMasterBO.getOrderedOuterQty() * productMasterBO.getOutersize()) * productMasterBO.getWeight()
                                            + "," + productMasterBO.getOutersize()
                                            + "," + getStringQueryParam(productMasterBO.getHsnCode());
                                    dbUtil.insertSQL("OrderDetailRequest", column, value);
                                }
                            }

                            return true;
                        } catch (Exception ignored) {
                            return false;
                        }
                    }
                });
            }
        });
    }

    @Override
    public Single<Boolean> saveOpportunityDetails(String uId, ArrayList<ProductMasterBO> productMasterBOS) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                String column, value;
                column = "rid, pid, facing,IsOwn,Price";

                for (ProductMasterBO productMasterBO : productMasterBOS) {

                    value = getStringQueryParam(uId)
                            + "," + getStringQueryParam(productMasterBO.getProductID())
                            + "," + productMasterBO.getQty_klgs()
                            + "," + productMasterBO.getOwn()
                            + "," + productMasterBO.getOrderPricePiece();
                    dbUtil.insertSQL("RetailerPotential", column, value);
                }

                return null;
            }
        });
    }

    @Override
    public Single<Boolean> setupSurveyScreenData(String id) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {

                surveyHelperNew.setFromHomeScreen(true);
                surveyHelperNew.downloadModuleId(NewRetailerConstant.NEW_RETAILER);
                surveyHelperNew.downloadQuestionDetails(NewRetailerConstant.MENU_NEW_RETAILER);
                surveyHelperNew.loadNewRetailerSurveyAnswers(id);

                return true;
            }
        });
    }

    @Override
    public Single<Boolean> setUpOpportunityProductsData() {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {

                productTaggingHelper.downloadTaggedProducts(MENU_NEW_RETAILER);
                productHelper.downloadCompetitorProducts("MENU_STK_ORD");
                productTaggingHelper.downloadCompetitorTaggedProducts(MENU_NEW_RETAILER);

                /* Settign color **/
                configurationMasterHelper.downloadFilterList();
                productHelper.updateProductColor();
                productHelper.downloadInStoreLocations();
                return true;
            }
        });
    }

    @Override
    public Single<Boolean> setUpOrderScreen() {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                GenericObjectPair<Vector<ProductMasterBO>, Map<String, ProductMasterBO>> genericObjectPair
                        = productHelper.downloadProducts(NewRetailerConstant.MENU_NEW_RETAILER);
                if (genericObjectPair != null) {
                    productHelper.setProductMaster(genericObjectPair.object1);
                    productHelper.setProductMasterById(genericObjectPair.object2);
                }


                productHelper.setFilterProductLevels(productHelper
                        .downloadFilterLevel(NewRetailerConstant.MENU_NEW_RETAILER));
                productHelper.setFilterProductsByLevelId(productHelper
                        .downloadFilterLevelProducts(productHelper.getFilterProductLevels(), true));


                configurationMasterHelper.downloadProductDetailsList();
                /* Settign color **/
                configurationMasterHelper.downloadFilterList();
                productHelper.updateProductColor();
                productHelper.downloadInStoreLocations();

                return true;
            }
        });
    }

    @Override
    public Single<Boolean> clearExistingOrder() {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {

                if (dataManager.isEditOrder()) {
                    productHelper.clearOrderTableAndUpdateSIH();
                }
                productHelper.clearOrderTable();
                OrderHelper.getInstance(context).setSerialNoListByProductId(null);

                if (configurationMasterHelper.SHOW_PRODUCTRETURN)
                    productHelper.clearBomReturnProductsTable();

                return null;
            }
        });
    }

    @Override
    public Single<Boolean> saveEditProfileField(HashMap<String,?> retailerProfileField,String tid){

        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {

                initDb();

                try {

                    ArrayList<ConfigureBO> profileFieldList = new ArrayList<>();
                    if (retailerProfileField.get("ProfileFields") != null && retailerProfileField.get("ProfileFields") instanceof ArrayList<?>)
                        profileFieldList = (ArrayList<ConfigureBO>) retailerProfileField.get("ProfileFields");

                    profileFieldInsert(profileFieldList, tid);



                    ArrayList<StandardListBO> priorityProdList = new ArrayList<>();
                    if (retailerProfileField.get("Priority") != null && retailerProfileField.get("Priority") instanceof ArrayList<?>)
                        priorityProdList = (ArrayList<StandardListBO>) retailerProfileField.get("Priority");

                    priorityProductInsert(priorityProdList, tid);



                    HashMap<String,String> nearbyRetailerList = new HashMap<>();
                    if (retailerProfileField.get("NearByRetailer") != null && retailerProfileField.get("NearByRetailer") instanceof ArrayList<?>)
                        nearbyRetailerList = (HashMap<String,String>) retailerProfileField.get("NearByRetailer");

                    nearByRetailerInsert(nearbyRetailerList, tid);


                } catch (Exception e) {
                    Commons.printException("" + e);
                }

                shutDownDb();


                return true;
            }
        });
    }

    private void nearByRetailerInsert(HashMap<String, String> nearbyRetailerList, String tid) {
        dbUtil.deleteSQL("RrtNearByEditRequest", " tid =" + StringUtils.getStringQueryParam(tid), false);

        if (nearbyRetailerList != null)
            for (String id : nearbyRetailerList.keySet()) {
                String Q = "insert into RrtNearByEditRequest (tid,rid,nearbyrid,status,upload)" +
                        "values (" + StringUtils.getStringQueryParam(tid) + "," + StringUtils.getStringQueryParam(dataManager.getRetailMaster().getRetailerID())
                        + "," + id + "," + StringUtils.getStringQueryParam(nearbyRetailerList.get(id)) + ",'N')";
                dbUtil.executeQ(Q);
            }
    }

    private void priorityProductInsert(ArrayList<StandardListBO> priorityProdList, String tid) {
        dbUtil.deleteSQL("RetailerEditPriorityProducts", " RetailerId ="
                + StringUtils.getStringQueryParam(dataManager.getRetailMaster().getRetailerID()), false);

        if (priorityProdList != null)
            for (StandardListBO bo : priorityProdList) {
                String Q = "insert into RetailerEditPriorityProducts (tid,RetailerId,productId,levelid,status,upload)" +
                        "values (" + StringUtils.getStringQueryParam(tid)
                        + "," + StringUtils.getStringQueryParam(dataManager.getRetailMaster().getRetailerID())
                        + "," + SDUtil.convertToInt(bo.getListID())
                        + "," + StringUtils.getStringQueryParam(bo.getListCode())
                        + "," + StringUtils.getStringQueryParam(bo.getStatus()) + ",'N')";
                dbUtil.executeQ(Q);
            }
    }

    private void profileFieldInsert(ArrayList<ConfigureBO> profileFieldList, String tid) {
        for (ConfigureBO retaileProfileField : profileFieldList) {

            String value = "";
            boolean isInsert =  true;

            if (retaileProfileField.isDeleteRow())
                isInsert = false;

            if (retaileProfileField.getConfigCode().equals(ProfileConstant.PHOTO_CAPTURE)) {

                value = "Profile" + "/" + dataManager.getUser().getDownloadDate().replace("/", "")
                        + "/" + dataManager.getUser().getUserid() + "/" + retaileProfileField.getMenuNumber();

                FileUtils.checkFileExist(AppUtils.latlongImageFileName + "", dataManager.getRetailMaster().getRetailerID(), true);

            }else if(retaileProfileField.getConfigCode().equals(ProfileConstant.PROFILE_60)){

                if (isInsert) {
                    value = "Profile" + "/" + dataManager.getUser().getDownloadDate().replace("/", "")
                            + "/" + dataManager.getUser().getUserid() + "/" + retaileProfileField.getMenuNumber();

                    deleteQuery(retaileProfileField.getConfigCode());
                }

                FileUtils.checkFileExist(retaileProfileField.getMenuNumber() + "", dataManager.getRetailMaster().getRetailerID(), false);

            }else{
                value = retaileProfileField.getMenuNumber();
            }

            if (isInsert) {

                deleteQuery(retaileProfileField.getConfigCode());

                String mCustomQuery = StringUtils.getStringQueryParam(retaileProfileField.getConfigCode()) + ","
                        + StringUtils.getStringQueryParam(value) + "," + retaileProfileField.getRefId() + "," + dataManager.getRetailMaster().getRetailerID() + ")";

                insertRowQuery(tid,mCustomQuery);

            }else
                deleteQuery(retaileProfileField.getConfigCode());

        }
    }

    private void insertRowQuery(String tid, String mCustomQuery){
        final String insertquery = "insert into RetailerEditDetail (tid,Code,value,RefId,RetailerId)"
                + "values (" + getStringQueryParam(tid) + "," + mCustomQuery;

        dbUtil.executeQ(insertquery);
    }

    private void deleteQuery(String code){
        dbUtil.deleteSQL(DataMembers.tbl_RetailerEditDetail, " Code ="
                + getStringQueryParam(code)
                + "and RetailerId=" + dataManager.getRetailMaster().getRetailerID(), false);
    }

    @Override
    public Single<Boolean> saveEditContactData(ArrayList<RetailerContactBo> contactList,final String tid) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {

                initDb();

                try {
                    if (contactList != null && !contactList.isEmpty()) {
                        for (RetailerContactBo retailerContactBo : contactList) {
                            if (retailerContactBo.getStatus().equalsIgnoreCase("U")
                                    || retailerContactBo.getStatus().equalsIgnoreCase("I")
                                    || retailerContactBo.getStatus().equalsIgnoreCase("D")) {
                                updateRetailerContactEditList(tid);
                                break;
                            }
                        }
                    }

                    retailerContactEdit(contactList,tid);

                } catch (Exception e) {
                    Commons.printException("" + e);
                }

                shutDownDb();

                return true;
            }
        });
    }

    private void updateRetailerContactEditList(String tid) {
        String mCustomquery = StringUtils.getStringQueryParam("CONTACTEDIT")
                + "," + StringUtils.getStringQueryParam("1")
                + "," + StringUtils.getStringQueryParam(dataManager.getRetailMaster().getRetailerID())
                + "," + StringUtils.getStringQueryParam(dataManager.getRetailMaster().getRetailerID()) + ")";
        insertRowQuery(tid, mCustomquery);
    }

    private void retailerContactEdit(ArrayList<RetailerContactBo> retailerContactList,String tid){

        final String column = "Contact_Title,Contact_Title_LovId,ContactName,ContactName_LName," +
                "ContactNumber,Email,IsPrimary,Status,CPId,RetailerId,Tid,salutationLovId,IsEmailNotificationReq";

        String where="RetailerId="+getStringQueryParam(dataManager.getRetailMaster().getRetailerID());

        Cursor getCpidCursor = dbUtil.selectSQL("Select CPId from RetailerContactEdit where retailerId=" + getStringQueryParam(dataManager.getRetailMaster().getRetailerID()));

        if (getCpidCursor != null && getCpidCursor.getCount() > 0) {
            while (getCpidCursor.moveToNext()) {
                dbUtil.deleteSQL("ContactAvailabilityEdit", "CPId=" + getCpidCursor.getString(0), false);
            }
        }

        dbUtil.deleteSQL("RetailerContactEdit",where,false);

        if (retailerContactList != null && !retailerContactList.isEmpty()) {
            for (RetailerContactBo retailerContactBo : retailerContactList) {
                if (retailerContactBo.getStatus().equalsIgnoreCase("U")
                        || retailerContactBo.getStatus().equalsIgnoreCase("I")
                        || retailerContactBo.getStatus().equalsIgnoreCase("D")) {
                    String value = StringUtils.getStringQueryParam(retailerContactBo.getTitle()) + ","
                            + StringUtils.getStringQueryParam(retailerContactBo.getContactTitleLovId()) + ","
                            + StringUtils.getStringQueryParam(retailerContactBo.getFistname()) + ","
                            + StringUtils.getStringQueryParam(retailerContactBo.getLastname()) + ","
                            + StringUtils.getStringQueryParam(retailerContactBo.getContactNumber()) + ","
                            + StringUtils.getStringQueryParam(retailerContactBo.getContactMail()) + ","
                            + retailerContactBo.getIsPrimary() + ","
                            + StringUtils.getStringQueryParam(retailerContactBo.getStatus()) + ","
                            + StringUtils.getStringQueryParam(retailerContactBo.getCpId()) + ","
                            + StringUtils.getStringQueryParam(dataManager.getRetailMaster().getRetailerID()) + ","
                            + StringUtils.getStringQueryParam(tid)+ ","
                            + StringUtils.getStringQueryParam(retailerContactBo.getContactSalutationId())+ ","
                            + StringUtils.getStringQueryParam(retailerContactBo.getIsEmailPrimary()+"");
                    dbUtil.insertSQL("RetailerContactEdit", column, value);

                    addContactAvail(dbUtil,retailerContactBo,getStringQueryParam(dataManager.getRetailMaster().getRetailerID()),tid);
                }
            }
        }

    }

    private void addContactAvail(DBUtil db, RetailerContactBo retailerContactBo, String retailerId, String Tid) {
        String column = "CPAId,CPId,Day,StartTime,EndTime,Tid,status,upload, RetailerID";

        for (RetailerContactAvailBo retailerContactAvailBo : retailerContactBo.getContactAvailList()) {

            String value = StringUtils.getStringQueryParam(retailerContactAvailBo.getCpaid() != null && !retailerContactAvailBo.getCpaid().isEmpty() ? retailerContactAvailBo.getCpaid() : retailerId)
                    + "," + StringUtils.getStringQueryParam(retailerContactBo.getCpId())
                    + "," + StringUtils.getStringQueryParam(retailerContactAvailBo.getDay())
                    + "," + StringUtils.getStringQueryParam(retailerContactAvailBo.getFrom())
                    + "," + StringUtils.getStringQueryParam(retailerContactAvailBo.getTo())
                    + "," + StringUtils.getStringQueryParam(Tid)
                    + "," + StringUtils.getStringQueryParam(retailerContactAvailBo.getStatus())
                    + "," + StringUtils.getStringQueryParam("N")
                    + "," + StringUtils.getStringQueryParam(retailerContactBo.getRetailerID());

            db.insertSQL("ContactAvailabilityEdit", column, value);
        }
    }

    @Override
    public Single<Boolean> saveEditAttributeData(ArrayList<AttributeBO> attributeList, String mTid) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {

                initDb();

                String tid = mTid;

                Cursor headerCursor;
                try {
                    // delete Header if exist
                    headerCursor = dbUtil.selectSQL("SELECT Tid FROM RetailerEditHeader"
                            + " WHERE RetailerId = "
                            + StringUtils.getStringQueryParam(dataManager.getRetailMaster().getRetailerID())
                            + " AND Date = "
                            + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL))
                            + " AND Upload = "
                            + StringUtils.getStringQueryParam("N"));

                    if (headerCursor.getCount() > 0) {
                        headerCursor.moveToNext();
                        tid = headerCursor.getString(0);
                        headerCursor.close();
                    }
                } catch (Exception e) {
                    Commons.printException(e);
                }

                updateRetailerMasterAttributes(tid,attributeList);

                return true;
            }
        });
    }

    private void updateRetailerMasterAttributes(final String mTid,final ArrayList<AttributeBO> selectedAttribList) {

        try {
            dbUtil.deleteSQL("RetailerEditAttribute", " tid =" + StringUtils.getStringQueryParam(mTid), false);

            for (AttributeBO id : selectedAttribList) {
                String Q = "insert into RetailerEditAttribute (tid,retailerid,attributeid,levelid,status,upload)" +
                        "values (" + StringUtils.getStringQueryParam(mTid)
                        + "," + StringUtils.getStringQueryParam(dataManager.getRetailMaster().getRetailerID())
                        + "," + id.getAttributeId()
                        + "," + id.getLevelId()
                        + "," + StringUtils.getStringQueryParam(id.getStatus()) + ",'N')";
                dbUtil.executeQ(Q);
            }

            shutDownDb();

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    @Override
    public void closeDB() {
        dbUtil.closeDB();
    }

}
