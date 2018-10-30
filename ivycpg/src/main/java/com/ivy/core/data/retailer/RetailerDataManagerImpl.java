package com.ivy.core.data.retailer;

import android.database.Cursor;

import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.IndicativeBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;

import static com.ivy.sd.png.provider.ConfigurationMasterHelper.CODE_SHOW_ALL_ROUTE_FILTER;
import static com.ivy.utils.AppUtils.QT;

public class RetailerDataManagerImpl implements RetailerDataManager {

    private DBUtil mDbUtil;

    private AppDataProvider appDataProvider;

    private ConfigurationMasterHelper configurationMasterHelper;

    @Inject
    public RetailerDataManagerImpl(@DataBaseInfo DBUtil dbUtil, AppDataProvider appDataProvider, ConfigurationMasterHelper configurationMasterHelper) {
        mDbUtil = dbUtil;
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
    public Observable<ArrayList<RetailerMasterBO>> fetchRetailers() {
        return Observable.zip(fetchIndicativeRetailers(), updateRouteConfig(), new BiFunction<ArrayList<IndicativeBO>, Boolean, ArrayList<IndicativeBO>>() {
            @Override
            public ArrayList<IndicativeBO> apply(ArrayList<IndicativeBO> indicativeBOS, Boolean aBoolean) throws Exception {
                return indicativeBOS;
            }
        }).flatMap(new Function<ArrayList<IndicativeBO>, Observable<ArrayList<RetailerMasterBO>>>() {
            @Override
            public Observable<ArrayList<RetailerMasterBO>> apply(final ArrayList<IndicativeBO> indicativeBOS) throws Exception {
                return Observable.fromCallable(new Callable<ArrayList<RetailerMasterBO>>() {
                    @Override
                    public ArrayList<RetailerMasterBO> call() throws Exception {
                        ArrayList<RetailerMasterBO> retailerMasterBOS = new ArrayList<>();
                        RetailerMasterBO retailer;
                        try {

                            initDb();

                            Cursor c = mDbUtil
                                    .selectSQL("SELECT DISTINCT A.RetailerID, A.RetailerCode, A.RetailerName, RBM.BeatID as beatid, A.creditlimit, A.tinnumber, A.TinExpDate, A.channelID,"
                                            + " A.classid, A.subchannelid, ifnull(A.daily_target_planned,0) as daily_target_planned, RBM.isDeviated,"
                                            + " ifnull(A.sbdMerchpercent,0) as sbdMerchpercent, A.is_new,ifnull(A.initiativePercent,0) as initiativePercent,"
                                            + " isOrdered, RBM.isProductive, isInvoiceCreated, isDigitalContent, isReviewPlan, RBM.isVisited,"
                                            + " (select count (sbdid) from SbdMerchandisingMaster where ChannelId = A.ChannelId"
                                            + " and TypeListId = (select ListId from StandardListMaster where ListCode='MERCH')) as rpstgt,"
                                            + " ifnull(A.RPS_Merch_Achieved,0) as RPS_Merch_Achieved, ifnull(RC.weekNo,0) as weekNo,A.isDeadStore,A.isPlanned,"
                                            + " ifnull((select ListCode from StandardListMaster where ListID=A.RpTypeId),'') as RpTypeCode, A.sptgt, A.isOrderMerch,"
                                            + " A.PastVisitStatus, A.isMerchandisingDone, A.isInitMerchandisingDone,"
                                            + " case when RC.WalkingSeq='' then 9999 else RC.WalkingSeq end as WalkingSeq,"
                                            + " A.sbd_dist_stock,A.RField1,"
                                            + "(select count (sbdid) from SbdMerchandisingMaster where "
                                            + "ChannelId = A.ChannelId and TypeListId=(select ListId from "
                                            + "StandardListMaster where ListCode='MERCH_INIT')) as pricetgt,"
                                            + "A.sbdMerchInitAcheived,A.sbdMerchInitPercent, A.initiative_achieved, "
                                            + "(select  count(rowid) from InitiativeHeaderMaster B where isParent=1 and B.InitID in "
                                            + "(select InitId from InitiativeDetailMaster where LocalChannelId=A.subchannelid))as init_target"
                                            + " , IFNULL(A.RField2,0) as RField2, A.radius as GPS_DIST, " +
                                            "StoreOTPActivated, SkipOTPActivated,RField3,A.RetCreditLimit," +
                                            "TaxTypeId,RField4,locationid,LM.LocName,A.VisitDays,A.accountid,A.NfcTagId,A.contractstatuslovid,A.ProfileImagePath,"
                                            + (configurationMasterHelper.IS_DIST_SELECT_BY_SUPPLIER ? "SM.sid as RetDistributorId," : +appDataProvider.getUser().getBranchId() + " as RetDistributorId,")
                                            + (configurationMasterHelper.IS_DIST_SELECT_BY_SUPPLIER ? "SM.sid as RetDistParentId," : +appDataProvider.getUser().getDistributorid() + " as RetDistParentId,")
                                            + "RA.address1, RA.address2, RA.address3, RA.City, RA.State, RA.pincode, RA.contactnumber, RA.email, IFNULL(RA.latitude,0) as latitude, IFNULL(RA.longitude,0) as longitude, RA.addressId"
                                            + " , RC1.contactname as pc_name, RC1.ContactName_LName as pc_LName, RC1.ContactNumber as pc_Number,"
                                            + " RC1.CPID as pc_CPID, IFNULL(RC1.DOB,'') as pc_DOB, RC1.contact_title as pc_title, RC1.contact_title_lovid as pc_title_lovid"
                                            + " , RC2.contactname as sc_name, RC2.ContactName_LName as sc_LName, RC2.ContactNumber as sc_Number,"
                                            + " RC2.CPID as sc_CPID, IFNULL(RC2.DOB,'') as sc_DOB, RC2.contact_title as sc_title, RC2.contact_title_lovid as sc_title_lovid,"

                                            + " IFNULL(RPG.GroupId,0) as retgroupID, RV.PlannedVisitCount, RV.VisitDoneCount, RV.VisitFrequency,"

                                            + " IFNULL(RACH.monthly_acheived,0) as MonthlyAcheived, IFNULL(creditPeriod,'') as creditPeriod,RField5,RField6,RField7,RPP.ProductId as priorityBrand,SalesType,A.isSameZone, A.GSTNumber,A.InSEZ,A.DLNo,A.DLNoExpDate,IFNULL(A.SubDId,0) as SubDId,"
                                            + " A.pan_number,A.food_licence_number,A.food_licence_exp_date,RA.Mobile,RA.FaxNo,RA.Region,RA.Country,"
                                            + "IFNULL((select EAM.AttributeCode from EntityAttributeMaster EAM where EAM.AttributeId = RAT.AttributeId and "
                                            + "(select AttributeCode from EntityAttributeMaster where AttributeId = EAM.ParentId"
                                            + " and IsSystemComputed = 1) = 'Golden_Type'),0) as AttributeCode,A.sbdDistPercent,A.retailerTaxLocId as RetailerTaxLocId,"
                                            + (configurationMasterHelper.IS_DIST_SELECT_BY_SUPPLIER ? "SM.supplierTaxLocId as SupplierTaxLocId" : "0 as SupplierTaxLocId")
                                            + " FROM RetailerMaster A"

                                            + " LEFT JOIN RetailerBeatMapping RBM ON RBM.RetailerID = A.RetailerID"

                                            + " LEFT JOIN RetailerClientMappingMaster RC " + (configurationMasterHelper.IS_BEAT_WISE_RETAILER_MAPPING ? " on RC.beatID=RBM.beatId" : " on RC.Rid = A.RetailerId")

                                            + (configurationMasterHelper.SHOW_DATE_ROUTE ? " AND RC.date = " + QT(SDUtil.now(SDUtil.DATE_GLOBAL)) : "")

                                            + " LEFT JOIN RetailerAddress RA ON RA.RetailerId = A.RetailerID AND RA.IsPrimary=1"

                                            + " LEFT JOIN RetailerContact RC1 ON RC1.RetailerId = A.RetailerID AND RC1.IsPrimary = 1"
                                            + " LEFT JOIN (SELECT RetailerId,contactname,ContactName_LName,ContactNumber,CPID,DOB,contact_title,contact_title_lovid from RetailerContact WHERE IsPrimary=0 LIMIT 1) AS RC2 ON RC2.RetailerId=A.RetailerId"

                                            + (configurationMasterHelper.IS_DIST_SELECT_BY_SUPPLIER ? " left join SupplierMaster SM ON SM.rid = A.RetailerID" : "")

                                            + " LEFT JOIN RetailerPriceGroup RPG ON RPG.RetailerID = A.RetailerID and (RPG.distributorid=RetDistributorId OR RPG.distributorid = 0)"

                                            + " LEFT JOIN RetailerVisit RV ON RV.RetailerID = A.RetailerID"

                                            + " LEFT JOIN RetailerAchievement RACH ON RACH.RetailerID = A.RetailerID"

                                            + " LEFT JOIN LocationMaster LM ON LM.LocId = A.locationid"

                                            + " LEFT JOIN RetailerPriorityProducts RPP ON RPP.retailerid = A.RetailerID"

                                            + " LEFT JOIN RetailerAttribute RAT ON A.RetailerID = RAT.RetailerId");
                            if (c != null) {
                                while (c.moveToNext()) {
                                    retailer = new RetailerMasterBO();

                                    String retID = c.getString(c.getColumnIndex("RetailerID"));
                                    retailer.setRetailerID(retID);
                                    retailer.setRetailerCode(c.getString(c.getColumnIndex("RetailerCode")));
                                    retailer.setRetailerName(c.getString(c.getColumnIndex("RetailerName")));
                                    retailer.setBeatID(c.getInt(c.getColumnIndex("beatid")));
                                    retailer.setCreditLimit(c.getDouble(c.getColumnIndex("creditlimit")));
                                    retailer.setTinnumber(c.getString(c.getColumnIndex("tinnumber")));
                                    retailer.setTinExpDate(c.getString(c.getColumnIndex("TinExpDate")));
                                    retailer.setChannelID(c.getInt(c.getColumnIndex("channelID")));
                                    retailer.setClassid(c.getInt(c.getColumnIndex("classid")));
                                    retailer.setSubchannelid(c.getInt(c.getColumnIndex("subchannelid")));
                                    retailer.setDaily_target_planned(c.getDouble(c.getColumnIndex("daily_target_planned")));
                                    retailer.setDaily_target_planned_temp(c.getDouble(c.getColumnIndex("daily_target_planned")));

                                    retailer.setIsPlanned(c.getString(c.getColumnIndex("isPlanned")));
                                    retailer.setIsDeviated(c.getString(c.getColumnIndex("isDeviated")));
                                    retailer.setIsVisited(c.getString(c.getColumnIndex("isVisited")));
                                    retailer.setOrdered(c.getString(c.getColumnIndex("isOrdered")));
                                    retailer.setProductive(c.getString(c.getColumnIndex("isProductive")));
                                    retailer.setIsNew(c.getString(c.getColumnIndex("is_new")));
                                    retailer.setIsDeadStore(c.getString(c.getColumnIndex("isDeadStore")));
                                    retailer.setIsGoldStore(c.getInt(c.getColumnIndex("AttributeCode"))); // To display golden store

                                    // Dist and merch precent
                                    retailer.setSbdMercPercent(c.getString(c.getColumnIndex("sbdMerchpercent")));

                                    retailer.setInitiativePercent(c.getString(c.getColumnIndex("initiativePercent")));

                                    retailer.setInvoiceDone(c.getString(c.getColumnIndex("isInvoiceCreated")));
                                    retailer.setIsDigitalContent(c.getString(c.getColumnIndex("isDigitalContent")));
                                    retailer.setIsReviewPlan(c.getString(c.getColumnIndex("isReviewPlan")));

                                    // Dist and merch tgt & acheivement count
                                    retailer.setSBDMerchTarget(c.getInt(c.getColumnIndex("rpstgt")));
                                    retailer.setSBDMerchAchieved(c.getInt(c.getColumnIndex("RPS_Merch_Achieved")));

                                    retailer.setWeekNo(c.getString(c.getColumnIndex("weekNo")));
                                    retailer.setWalkingSequence(c.getInt(c.getColumnIndex("WalkingSeq")));

                                    retailer.setRpTypeCode(c.getString(c.getColumnIndex("RpTypeCode")));
                                    retailer.setSpTarget(c.getFloat(c.getColumnIndex("sptgt")));
                                    retailer.setIsOrderMerch(c.getString(c.getColumnIndex("isOrderMerch")));
                                    retailer.setLastVisitStatus(c.getString(c.getColumnIndex("PastVisitStatus")));
                                    retailer.setIsMerchandisingDone(c.getString(c.getColumnIndex("isMerchandisingDone")));
                                    retailer.setIsInitMerchandisingDone(c.getString(c.getColumnIndex("isInitMerchandisingDone")));

                                    retailer.setSbdDistStock(c.getInt(c.getColumnIndex("sbd_dist_stock")));
                                    retailer.setSbdMerchInitTarget(c.getInt(c.getColumnIndex("pricetgt")));
                                    retailer.setSbdMerchInitAcheived(c.getInt(c.getColumnIndex("sbdMerchInitAcheived")));
                                    retailer.setSbdMerchInitPrecent(c.getString(c.getColumnIndex("sbdMerchInitPercent")));
                                    retailer.setInitiative_achieved(c.getInt(c.getColumnIndex("initiative_achieved")));
                                    retailer.setInitiative_target(c.getInt(c.getColumnIndex("init_target")));
                                    retailer.setRfield2(c.getString(c.getColumnIndex("RField2")));


                                    retailer.setGpsDistance(c.getInt(c.getColumnIndex("GPS_DIST")));
                                    retailer.setOtpActivatedDate(c.getString(c.getColumnIndex("StoreOTPActivated")));
                                    retailer.setSkipActivatedDate(c.getString(c.getColumnIndex("SkipOTPActivated")));
                                    try {
                                        retailer.setBottle_creditLimit(c.getDouble(c.getColumnIndex("RetCreditLimit")));
                                    } catch (Exception e) {
                                        Commons.printException(e);
                                        retailer.setBottle_creditLimit(0.0);
                                    }

                                    retailer.setProfile_creditLimit(c.getString(c.getColumnIndex("RetCreditLimit")));

                                    retailer.setTaxTypeId(c.getInt(c.getColumnIndex("TaxTypeId")));
                                    retailer.setLocationId(c.getInt(c.getColumnIndex("locationid")));
                                    retailer.setLocName(c.getString(c.getColumnIndex("LocName")));
                                    retailer.setVisitday(c.getString(c.getColumnIndex("VisitDays")));
                                    retailer.setAccountid(c.getInt(c.getColumnIndex("accountid")));
                                    retailer.setNFCTagId(c.getString(c.getColumnIndex("NfcTagId")));
                                    retailer.setContractLovid(c.getInt(c.getColumnIndex("contractstatuslovid")));
                                    retailer.setDistributorId(c.getInt(c.getColumnIndex("RetDistributorId")));
                                    retailer.setDistParentId(c.getInt(c.getColumnIndex("RetDistParentId")));
                                    try {
                                        retailer.setCredit_balance(SDUtil.convertToDouble(c.getString(c.getColumnIndex("RField1"))));
                                    } catch (Exception e) {
                                        Commons.printException(e);
                                        retailer.setCredit_balance(0.0);
                                    }
                                    retailer.setRField1(c.getString(c.getColumnIndex("RField1")));
                                    retailer.setCredit_invoice_count(c.getString(c.getColumnIndex("RField3")));
                                    retailer.setRField4(c.getString(c.getColumnIndex("RField4")));

                                    //temp_retailer_address
                                    retailer.setAddress1(c.getString(c.getColumnIndex("Address1")));
                                    retailer.setAddress2(c.getString(c.getColumnIndex("Address2")));
                                    retailer.setAddress3(c.getString(c.getColumnIndex("Address3")));
                                    retailer.setCity(c.getString(c.getColumnIndex("City")));
                                    retailer.setState(c.getString(c.getColumnIndex("State")));
                                    retailer.setPincode(c.getString(c.getColumnIndex("pincode")));
                                    retailer.setContactnumber(c.getString(c.getColumnIndex("ContactNumber")));
                                    retailer.setEmail(c.getString(c.getColumnIndex("Email")));
                                    retailer.setLatitude(c.getDouble(c.getColumnIndex("latitude")));
                                    retailer.setLongitude(c.getDouble(c.getColumnIndex("longitude")));
                                    retailer.setAddressid(c.getString(c.getColumnIndex("AddressId")));

                                    //temp_retailerContact
                                    retailer.setContactname(c.getString(c.getColumnIndex("pc_name")));
                                    retailer.setContactLname(c.getString(c.getColumnIndex("pc_LName")));
                                    retailer.setContactnumber1(c.getString(c.getColumnIndex("pc_Number")));
                                    retailer.setCp1id(c.getString(c.getColumnIndex("pc_CPID")));
                                    if (!c.getString(c.getColumnIndex("pc_DOB")).equalsIgnoreCase("null"))
                                        retailer.setDob(c.getString(c.getColumnIndex("pc_DOB")));
                                    else
                                        retailer.setDob("");
                                    retailer.setContact1_title(c.getString(c.getColumnIndex("pc_title")));
                                    retailer.setContact1_titlelovid(c.getString(c.getColumnIndex("pc_title_lovid")));

                                    retailer.setContactname2(c.getString(c.getColumnIndex("sc_name")));
                                    retailer.setContactLname2(c.getString(c.getColumnIndex("sc_LName")));
                                    retailer.setContactnumber2(c.getString(c.getColumnIndex("sc_Number")));
                                    retailer.setCp2id(c.getString(c.getColumnIndex("sc_CPID")));
                                    retailer.setContact2_title(c.getString(c.getColumnIndex("sc_title")));
                                    retailer.setContact2_titlelovid(c.getString(c.getColumnIndex("sc_title_lovid")));

                                    //temp_retailer_pricegroup
                                    retailer.setGroupId(c.getInt(c.getColumnIndex("retgroupID")));

                                    //temp_retailervisit
                                    retailer.setPlannedVisitCount(c.getInt(c
                                            .getColumnIndex("PlannedVisitCount")));
                                    retailer.setVisitDoneCount(c.getInt(c.getColumnIndex("VisitDoneCount")));
                                    retailer.setVisit_frequencey(c.getInt(c.getColumnIndex("VisitFrequency")));

                                    //temp_invoice_monthlyachievement
                                    retailer.setMonthly_acheived(c.getDouble(c.getColumnIndex("MonthlyAcheived")));

                                    retailer.setCreditDays(c.getInt(c.getColumnIndex("creditPeriod")));
                                    retailer.setRField5(c.getString(c.getColumnIndex("RField5")));
                                    retailer.setRField6(c.getString(c.getColumnIndex("RField6")));
                                    retailer.setRField7(c.getString(c.getColumnIndex("RField7")));

                                    retailer.setPrioriryProductId(c.getInt(c.getColumnIndex("priorityBrand")));
                                    retailer.setSalesTypeId(c.getInt(c.getColumnIndex("SalesType")));
                                    retailer.setProfileImagePath(c.getString(c.getColumnIndex("ProfileImagePath")));

                                    retailer.setSameZone(c.getInt(c.getColumnIndex("isSameZone")));
                                    retailer.setGSTNumber(c.getString(c.getColumnIndex("GSTNumber")));
                                    retailer.setIsSEZzone(c.getInt(c.getColumnIndex("InSEZ")));
                                    retailer.setDLNo(c.getString(c.getColumnIndex("DLNo")));
                                    retailer.setDLNoExpDate(c.getString(c.getColumnIndex("DLNoExpDate")));
                                    retailer.setSubdId(c.getInt(c.getColumnIndex("SubDId")));
                                    retailer.setPanNumber(c.getString(c.getColumnIndex("pan_number")));
                                    retailer.setFoodLicenceNo(c.getString(c.getColumnIndex("food_licence_number")));
                                    retailer.setFoodLicenceExpDate(c.getString(c.getColumnIndex("food_licence_exp_date")));
                                    retailer.setMobile(c.getString(c.getColumnIndex("Mobile")));
                                    retailer.setFax(c.getString(c.getColumnIndex("FaxNo")));
                                    retailer.setRegion(c.getString(c.getColumnIndex("Region")));
                                    retailer.setCountry(c.getString(c.getColumnIndex("Country")));
                                    retailer.setSbdPercent(c.getFloat(c.getColumnIndex("sbdDistPercent"))); // updated sbd percentage from history and ordered details
                                    retailer.setRetailerTaxLocId(c.getInt(c.getColumnIndex("RetailerTaxLocId")));
                                    retailer.setSupplierTaxLocId(c.getInt(c.getColumnIndex("SupplierTaxLocId")));

                                    retailer.setIsToday(0);
                                    retailer.setHangingOrder(false);
                                    retailer.setIndicateFlag(0);
                                    retailer.setIsCollectionView("N");
                                    if (configurationMasterHelper.IS_HANGINGORDER)
                                        updateHangingOrder(retailer);

                                    updateIndicativeOrderedRetailer(retailer, indicativeBOS);

                                    if (configurationMasterHelper.isRetailerBOMEnabled) {
                                        setIsBOMAchieved(retailer);
                                    }

                                    retailerMasterBOS.add(retailer);

                                    c.close();

                                }
                            }


                        } catch (Exception ignored) {

                        }
                        return retailerMasterBOS;
                    }
                }).flatMap(new Function<ArrayList<RetailerMasterBO>, Observable<ArrayList<RetailerMasterBO>>>() {
                    @Override
                    public Observable<ArrayList<RetailerMasterBO>> apply(ArrayList<RetailerMasterBO> retailerMasterBOS) throws Exception {
                        return getMSLValues(retailerMasterBOS);
                    }
                }).flatMap(new Function<ArrayList<RetailerMasterBO>, Observable<ArrayList<RetailerMasterBO>>>() {
                    @Override
                    public Observable<ArrayList<RetailerMasterBO>> apply(ArrayList<RetailerMasterBO> retailerMasterBOS) throws Exception {
                        if(configurationMasterHelper.SHOW_DATE_ROUTE){

                        }else {

                        }
                        return null;
                    }
                });
            }
        });
    }


    private Observable<ArrayList<RetailerMasterBO>> getMSLValues(final ArrayList<RetailerMasterBO> retailerMasterBOS) {
        return Observable.fromCallable(new Callable<ArrayList<RetailerMasterBO>>() {
            @Override
            public ArrayList<RetailerMasterBO> call() throws Exception {
                if(retailerMasterBOS.size()==0)
                    return retailerMasterBOS;
                else {
                    try {

                        Cursor c = null;

                        String sql = "Select DRD.ColumnId , Value , RowId,entityid,DRH.Columnname from DynamicReportDetail drd " +
                                "inner join DynamicReportHeader DRH on DRD.reportid=DRH.reportid  AND  DRD.columnid=DRH.columnid " +
                                "where drH.menucode = 'RETAILER_VALUE'  group by entityid,drd.columnid,rowid";

                        c = mDbUtil.selectSQL(sql);
                        if (c != null) {
                            while (c.moveToNext()) {

                                for (RetailerMasterBO retailer : retailerMasterBOS) {
                                    if (retailer.getRetailerID().equals(c.getString(3))) {
                                        if (c.getString(4).equalsIgnoreCase("MSL_ACH"))
                                            retailer.setMslAch(c.getString(1));
                                        if (c.getString(4).equalsIgnoreCase("MSL_TGT"))
                                            retailer.setMslTaget(c.getString(1));
                                        if (c.getString(4).equalsIgnoreCase("Sales"))
                                            retailer.setSalesValue(c.getString(1));
                                    }
                                }
                            }
                            c.close();
                        }

                    }catch (Exception ignored){

                    }
                }
                return retailerMasterBOS;
            }
        });
    }


    public void updateIndicativeOrderedRetailer(RetailerMasterBO retObj, ArrayList<IndicativeBO> indicativeBOS) {
        retObj.setIndicateFlag(0);
        for (IndicativeBO indBo : indicativeBOS) {
            if (indBo.getRetailerID().equals(retObj.getRetailerID())) {
                retObj.setIndicateFlag(indBo.getIsIndicative());
                break;
            }
        }
    }

    private void updateHangingOrder(RetailerMasterBO retailerMasterBO) {

        try {


            if(mDbUtil.isDbNullOrClosed())
                initDb();

            String retailerId = retailerMasterBO.getRetailerID();
            List<String> OrderId = null;

            Cursor c = mDbUtil
                    .selectSQL("SELECT OrderID FROM OrderHeader WHERE upload!='X' and RetailerID = '"
                            + retailerId + "'");
            if (c != null) {
                OrderId = new ArrayList<>();
                while (c.moveToNext()) {
                    OrderId.add(c.getString(0));
                }
                c.close();
            }

            if (OrderId == null || OrderId.size() == 0)
                retailerMasterBO.setHangingOrder(false);
            else {
                for (String ordId : OrderId) {
                    Cursor c1 = mDbUtil
                            .selectSQL("SELECT InvoiceNo FROM InvoiceMaster WHERE orderid = '"
                                    + ordId + "'");
                    if (c1 != null) {
                        if (c1.getCount() > 0) {
                            retailerMasterBO.setHangingOrder(false);
                        } else
                            retailerMasterBO.setHangingOrder(true);
                        c1.close();
                    } else {
                        retailerMasterBO.setHangingOrder(true);
                        break;
                    }

                }
            }

        } catch (Exception ignored) {

        }

    }

    private void setIsBOMAchieved(RetailerMasterBO Retailer) {
        try {
            Cursor c = null;

            String sql = "select RDP.pid,CSD.Shelfpqty,CSD.Shelfcqty,CSD.shelfoqty,OD.pieceqty,OD.caseQty,OD.outerQty from RtrWiseDeadProducts RDP " +
                    "left join ClosingStockDetail CSD on CSD.ProductID = RDP.pid And CSD.retailerid = RDP.rid " +
                    "left join OrderDetail OD on OD.retailerid = RDP.rid And OD.ProductID = RDP.pid where RDP.rid = " + SDUtil.convertToInt(Retailer.getRetailerID());

            c = mDbUtil.selectSQL(sql);
            if (c != null) {
                while (c.moveToNext()) {
                    Commons.print("qty " + c.getString(1) + ", " + c.getString(2) + "," + c.getString(3) + "," + c.getString(4) + "," + c.getString(5) + "," + c.getString(6));
                    if ((c.getString(1) != null && !c.getString(1).equals("0")) ||
                            (c.getString(2) != null && !c.getString(2).equals("0")) ||
                            (c.getString(3) != null && !c.getString(3).equals("0")) ||
                            (c.getString(4) != null && !c.getString(4).equals("0")) ||
                            (c.getString(5) != null && !c.getString(5).equals("0")) ||
                            (c.getString(6) != null && !c.getString(6).equals("0"))) {
                        Retailer.setBomAchieved(true);
                    } else {
                        Retailer.setBomAchieved(false);
                    }
                }
            }
            c.close();
        } catch (Exception ignored) {

        }
    }

    @Override
    public Observable<ArrayList<IndicativeBO>> fetchIndicativeRetailers() {
        return Observable.fromCallable(new Callable<ArrayList<IndicativeBO>>() {
            @Override
            public ArrayList<IndicativeBO> call() throws Exception {

                ArrayList<IndicativeBO> indicativeBOS = new ArrayList<>();

                try {
                    initDb();

                    StringBuffer sb = new StringBuffer();
                    sb.append("select distinct io.rid ,case when io.rid!=ifnull(oh.retailerid,0) then 1 else 0 end as flag ");
                    sb.append("from indicativeorder io left join orderHeader oh on  io.rid=oh.retailerid");
                    Cursor c = mDbUtil.selectSQL(sb.toString());
                    if (c.getCount() > 0) {
                        while (c.moveToNext()) {
                            IndicativeBO indBo = new IndicativeBO();
                            indBo.setIsIndicative(c.getShort(1));
                            indBo.setRetailerID(c.getString(0));
                            indicativeBOS.add(indBo);
                        }
                    }
                    c.close();

                } catch (Exception ignored) {

                }

                shutDownDb();

                return indicativeBOS;
            }
        });
    }

    @Override
    public Observable<Boolean> updateRouteConfig() {
        return Observable.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    initDb();
                    String sb = "select Rfield from HhtModuleMaster where flag=1 and hhtcode=" +
                            QT(CODE_SHOW_ALL_ROUTE_FILTER) + " and  ForSwitchSeller = 0";
                    Cursor c = mDbUtil.selectSQL(sb);
                    if (c.getCount() > 0) {
                        if (c.moveToNext()) {
                            int value = c.getInt(0);
                            if (value == 2) {
                                configurationMasterHelper.SHOW_DATE_ROUTE = true;
                            } else if (value == 3) {
                                configurationMasterHelper.SHOW_BEAT_ROUTE = true;
                            } else {
                                configurationMasterHelper.SHOW_WEEK_ROUTE = true;
                            }
                        }
                    }
                    c.close();
                    shutDownDb();
                    return true;
                } catch (Exception e) {
                    shutDownDb();
                    return false;
                }


            }
        });
    }

}
