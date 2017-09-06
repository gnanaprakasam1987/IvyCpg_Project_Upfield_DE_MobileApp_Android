package com.ivy.sd.png.provider;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.preference.PreferenceManager;
import android.util.Log;

import com.amazonaws.com.google.gson.Gson;
import com.ivy.countersales.bo.CS_StockReasonBO;
import com.ivy.countersales.bo.CounterSaleBO;
import com.ivy.lib.Utils;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.ApplyBo;
import com.ivy.sd.png.bo.AttributeBO;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.CustomerVisitTableBO;
import com.ivy.sd.png.bo.DbJsonPojo;
import com.ivy.sd.png.bo.LocationBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.SchemeBO;
import com.ivy.sd.png.bo.SchemeProductBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.ApplicationConfigs;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * Created by rajkumar.s on 18-03-2016.
 */
public class CounterSalesHelper {
    Context context;
    BusinessModel bmodel;
    private static CounterSalesHelper instance = null;
    SharedPreferences sharedPrefs;
    Vector<ConfigureBO> activitymenuconfig;

    String uid = "";


    ArrayList<AttributeBO> lstConcern;

    public CounterSalesHelper(Context context) {
        this.context = context;
        bmodel = (BusinessModel) context;
    }

    public static CounterSalesHelper getInstance(Context context) {
        if (instance == null) {
            instance = new CounterSalesHelper(context);
        }
        return instance;
    }

    public Vector<ConfigureBO> downloadCustomerVisitModules() {
        activitymenuconfig = new Vector<ConfigureBO>();

        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            sharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(context);
            String language = sharedPrefs.getString("languagePref",
                    ApplicationConfigs.LANGUAGE);
            String sql = "select hhtCode, flag, RField,MName,MNumber,hasLink,RField1 from "
                    + DataMembers.tbl_HhtMenuMaster
                    + " where hhtCode like 'MENU_%' and lang="
                    + bmodel.QT(language)
                    + " and flag=1 and SubChannelId = "
                    + bmodel.retailerMasterBO.getSubchannelid()
                    + " and MenuType="
                    + bmodel.QT("MENU_CUST_VISIT_CS")
                    + " order by MNumber";

            String sql1 = "select hhtCode, flag, RField,MName,MNumber,hasLink,RField1 from "
                    + DataMembers.tbl_HhtMenuMaster
                    + " where hhtCode like 'MENU_%' and lang="
                    + bmodel.QT(language)
                    + " and flag=1 and SubChannelId =0 "
                    + " and MenuType="
                    + bmodel.QT("MENU_CUST_VISIT_CS")
                    + " order by MNumber";


            Cursor c = db.selectSQL(sql);
            if (c != null) {
                if (c.getCount() == 0) {
                    c = db.selectSQL(sql1);
                }
            }

            ConfigureBO con;
            if (c != null) {
                while (c.moveToNext()) {
                    con = new ConfigureBO();
                    con.setConfigCode(c.getString(0));
                    con.setFlag(c.getInt(1));
                    con.setModule_Order(c.getInt(2));
                    con.setMenuName(c.getString(3));
                    con.setMenuNumber(c.getString(4));
                    con.setHasLink(c.getInt(5));
                    con.setMandatory(c.getInt(6));
                    activitymenuconfig.add(con);


                }

            }

            c.close();
            db.closeDB();
            return activitymenuconfig;
        } catch (Exception ex) {
            return null;

        }
    }

    private boolean isPercentageDiscount;
    private double numberOfPercent = 0;

    public boolean isPercentageDiscount() {
        return isPercentageDiscount;
    }

    public void setPercentageDiscount(boolean percentageDiscount) {
        isPercentageDiscount = percentageDiscount;
    }

    public double getNumberOfPercent() {
        return numberOfPercent;
    }

    public void setNumberOfPercent(double numberOfPercent) {
        this.numberOfPercent = numberOfPercent;
    }

    public double getDiscountedAmount() {
        return discountedAmount;
    }

    public void setDiscountedAmount(double discountedAmount) {
        this.discountedAmount = discountedAmount;
    }

    private double discountedAmount = 0;

    public void saveCustomerVisitDetails(String flag, String refid, double mSchemeDiscountedAmountOnBill) {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        String columns;
        String values;
        try {
            db.createDataBase();
            db.openDataBase();

            if (bmodel.getCounterSaleBO().isDraft()) {
          /*  String query="select name from CS_CustomerVisitHeader where uid='"+uid+"'";
            Cursor headerCursor=db.selectSQL(query);
            if(headerCursor.getCount()>0){*/
                // headerCursor.moveToNext();
                db.deleteSQL("CS_CustomerVisitHeader", "uid='" + bmodel.getCounterSaleBO().getLastUid() + "'", false);
                db.deleteSQL("CS_CustomerConcernDetails", "uid='" + bmodel.getCounterSaleBO().getLastUid() + "'", false);
                db.deleteSQL("CS_CustomerTrialDetails", "uid='" + bmodel.getCounterSaleBO().getLastUid() + "'", false);
                db.deleteSQL("CS_CustomerSaleDetails", "uid='" + bmodel.getCounterSaleBO().getLastUid() + "'", false);
                db.deleteSQL("CS_CustomerSampleGivenDetails", "uid='" + bmodel.getCounterSaleBO().getLastUid() + "'", false);
            }

            int sequance = 0;
            String sql = "select max(sequance) from CS_CustomerVisitHeader";
            Cursor c = db.selectSQL(sql);
            if (c.getCount() > 0) {
                if (c.moveToFirst()) {
                    sequance = c.getInt(0);
                }
            }

            CounterSaleBO bo = bmodel.getCounterSaleBO();

            boolean isData = false;

            // free,test
            int freeStockTypeId = -1, testerStockTypeId = -1;
            String query = "select listid from StandardListMaster where listtype='COUNTER_STOCK_TYPE' and listcode='TESTER'";
            Cursor cursor = db.selectSQL(query);
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    testerStockTypeId = cursor.getInt(0);
                }
                cursor.close();
            }

            query = "select listid from StandardListMaster where listtype='COUNTER_STOCK_TYPE' and listcode='FREE'";
            cursor = db.selectSQL(query);
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    freeStockTypeId = cursor.getInt(0);
                }
                cursor.close();
            }

            for (int i = 0; i < bo.getmTestProducts().size(); i++) {
                ApplyBo applyBo = bo.getmTestProducts().get(i);
                ProductMasterBO productBO = bmodel.productHelper.getProductMasterBOById(applyBo.getTestedProductId() + "");
                if (!applyBo.getResult().equals("") || applyBo.getTestedProductId() != 0 && !applyBo.getTestFeedback().equals("")) {
                    columns = "uid,pid,timetaken,result,feedback,upload,ProductName";
                    values = "'" + uid + "'," + applyBo.getTestedProductId() + ",'" + (applyBo.getTesthour() + ":" + applyBo.getTestTime()) + "','" + applyBo.getResult() + "','" + applyBo.getTestFeedback() + "'," + bmodel.QT(flag) +
                            "," + bmodel.QT(productBO.getProductName());
                    db.insertSQL(DataMembers.tbl_CS_CustomerTrialDetails, columns, values);
                    isData = true;

                    if (!flag.equals("I")) {
                        //updating SIH for tester product
                        db.updateSQL("UPDATE CS_SIHDetails"
                                + " SET sih =sih-1"
                                + " , counterid = " + bmodel.getCounterId()
                                + " WHERE pid = " + applyBo.getTestedProductId()
                                + " AND stock_type =" + testerStockTypeId);
                    }
                }
            }


            if (!bo.getResolution().equals("") || bo.getAttributeId() != 0 || !bo.getConnsultingFeedback().equals("")) {
                columns = "uid,attributeid,resolution,feedback,upload";
                values = "'" + uid + "'," + bo.getAttributeId() + ",'" + bo.getResolution() + "','" + bo.getConnsultingFeedback() + "'," + bmodel.QT(flag);
                db.insertSQL(DataMembers.tbl_CS_CustomerConcernDetails, columns, values);
                isData = true;
            }

            //customer sample details
            if (bo.getmSampleProducts() != null) {
                columns = "uid,pid,uomid,qty,upload";
                for (ProductMasterBO productMasterBO : bo.getmSampleProducts()) {
                    if (productMasterBO.getCsPiece() > 0) {
                        values = "'" + uid + "'," + productMasterBO.getProductID() + "," + productMasterBO.getPcUomid() + "," + productMasterBO.getCsPiece() + "," + bmodel.QT(flag);
                        db.insertSQL(DataMembers.tbl_CS_CustomerSampleGivenDetails, columns, values);
                        isData = true;
                    }
                    if (productMasterBO.getCsCase() > 0) {
                        values = "'" + uid + "'," + productMasterBO.getProductID() + "," + productMasterBO.getCaseUomId() + "," + productMasterBO.getCsCase() + "," + bmodel.QT(flag);
                        db.insertSQL(DataMembers.tbl_CS_CustomerSampleGivenDetails, columns, values);
                        isData = true;
                    }
                    if (productMasterBO.getCsOuter() > 0) {
                        values = "'" + uid + "'," + productMasterBO.getProductID() + "," + productMasterBO.getOuUomid() + "," + productMasterBO.getCsOuter() + "," + bmodel.QT(flag);
                        db.insertSQL(DataMembers.tbl_CS_CustomerSampleGivenDetails, columns, values);
                        isData = true;
                    }

                }
            }

            double schemeAmount = 0;
            double totalValue = 0;
            //customer sale details
            if (bo.getmSalesproduct() != null) {


                //
                totalValue = 0;
                columns = "uid,pid,uomid,qty,price,value,upload,isSalable,productName,uomCode";
                for (ProductMasterBO productMasterBO : bo.getmSalesproduct()) {

                    if (productMasterBO.getCsPiece() > 0 || productMasterBO.getCsCase() > 0 || productMasterBO.getCsOuter() > 0) {

                        schemeAmount = schemeAmount + productMasterBO.getSchemeDiscAmount();

                        if (productMasterBO.getCsPiece() > 0) {
                            values = "'" + uid + "'," + productMasterBO.getProductID() + "," + productMasterBO.getPcUomid() + "," + productMasterBO.getCsPiece() + "," + productMasterBO.getMRP() + "," + (productMasterBO.getCsPiece() * productMasterBO.getMRP()) + "," + bmodel.QT(flag) + ",1"
                                    +","+bmodel.QT(productMasterBO.getProductName())+","+bmodel.QT("PIECE");
                            db.insertSQL(DataMembers.tbl_CS_CustomerSaleDetails, columns, values);
                            isData = true;

                            totalValue += (productMasterBO.getCsPiece() * productMasterBO.getMRP());

                        }
                        if (productMasterBO.getCsCase() > 0) {
                            values = "'" + uid + "'," + productMasterBO.getProductID() + "," + productMasterBO.getCaseUomId() + "," + productMasterBO.getCsCase() + "," + productMasterBO.getMRP() + "," + (productMasterBO.getMRP() * productMasterBO.getCsCase()) + "," + bmodel.QT(flag) + ",1"
                                    +","+bmodel.QT(productMasterBO.getProductName())+","+bmodel.QT("CASE");
                            db.insertSQL(DataMembers.tbl_CS_CustomerSaleDetails, columns, values);
                            isData = true;

                            totalValue += ((productMasterBO.getCsCase() * productMasterBO.getCaseSize()) * productMasterBO.getMRP());
                        }
                        if (productMasterBO.getCsOuter() > 0) {
                            values = "'" + uid + "'," + productMasterBO.getProductID() + "," + productMasterBO.getOuUomid() + "," + productMasterBO.getCsOuter() + "," + productMasterBO.getMRP() + "," + (productMasterBO.getMRP() * productMasterBO.getCsOuter()) + "," + bmodel.QT(flag) + ",1"
                                    +","+bmodel.QT(productMasterBO.getProductName())+","+bmodel.QT("OUTER");
                            db.insertSQL(DataMembers.tbl_CS_CustomerSaleDetails, columns, values);
                            isData = true;

                            totalValue += ((productMasterBO.getCsOuter() * productMasterBO.getOutersize()) * productMasterBO.getMRP());
                        }
                        if (!flag.equals("I")) {
                            //updating SIH for normal/accessory
                            db.updateSQL("UPDATE CS_SIHDetails"
                                    + " SET sih =sih-" + (productMasterBO.getCsPiece() + (productMasterBO.getCaseSize() * productMasterBO.getCsCase()) + (productMasterBO.getOutersize() * productMasterBO.getCsOuter()))
                                    + " , counterid = " + bmodel.getCounterId()
                                    + " WHERE pid = " + productMasterBO.getProductID()
                                    + " AND stock_type!=" + testerStockTypeId + " AND stock_type!=" + freeStockTypeId);// If stock type not equal to free/test then it is a normal product or accessories
                        }
                    }

                    //inserting free product
                    if (productMasterBO.getCsFreePiece() > 0) {
                        values = "'" + uid + "'," + productMasterBO.getProductID() + "," + productMasterBO.getPcUomid() + "," + productMasterBO.getCsFreePiece() + "," + productMasterBO.getMRP() + ",0" + "," + bmodel.QT(flag) + ",0";
                        db.insertSQL(DataMembers.tbl_CS_CustomerSaleDetails, columns, values);
                        isData = true;
                        if (!flag.equals("I")) {
                            //updating SIH for free product
                            db.updateSQL("UPDATE CS_SIHDetails"
                                    + " SET sih =sih-" + productMasterBO.getCsFreePiece()
                                    + " , counterid = " + bmodel.getCounterId()
                                    + " WHERE pid = " + productMasterBO.getProductID()
                                    + " AND stock_type =" + freeStockTypeId);
                        }
                    }


                }
            }

            if (isData || (!bo.getCustomerName().equals("") || bo.getAddress().equals("") || !bo.getContactNumber().equals("") || !bo.getFreqVisit().equals(""))) {
                columns = "uid,retailerid,counter_id,date,name,address,contactno,freqvisit,upload,sequance,utcdate,refno,age_group,gender,email,billDiscPerc,discAmount,totalValue,Remarks,location,retailername,countername";
                values = "'" + uid + "'," + bmodel.getRetailerMasterBO().getRetailerID() + "," + bmodel.getCounterId() + "," + bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + ",'" + bo.getCustomerName() + "'," + bmodel.QT(bo.getAddress()) + "," + bmodel.QT(bo.getContactNumber()) + ",'" + bo.getFreqVisit() + "'," + bmodel.QT(flag) + "," + (sequance + 1) + "," + DatabaseUtils.sqlEscapeString(Utils.getGMTDateTime("yyyy/MM/dd HH:mm:ss")) + ",'" + refid + "'" +
                        "," + bo.getAgeGroup() + ",'" + bo.getGender() + "','" + bo.getEmail() + "'," + (isPercentageDiscount() ? getNumberOfPercent() : "0") + "," + getDiscountedAmount() + "," + totalValue + "," + bmodel.QT(bmodel.getNote())
                        +",'"+bmodel.getRetailerMasterBO().getLocName()+"','"+bmodel.getRetailerMasterBO().getRetailerName()+"','"+bmodel.userMasterHelper.getUserMasterBO().getCounterName()+"'";
                db.insertSQL(DataMembers.tbl_CS_CustomerVisitHeader, columns, values);

                schemeAmount = schemeAmount + mSchemeDiscountedAmountOnBill;
                db.updateSQL("UPDATE CS_CustomerVisitHeader"
                        + " SET SchemeAmount = " + schemeAmount
                        + " WHERE uid = " + "'" + uid + "'");
            }

            bmodel.schemeDetailsMasterHelper.isFromCounterSale = true;
            bmodel.schemeDetailsMasterHelper.insertScemeDetails(bmodel.QT(uid), db);
            if (!flag.equals("I")) {
            updateSchemeFreeProductSIH(freeStockTypeId, db);}


            db.closeDB();

            bmodel.schemeDetailsMasterHelper.isFromCounterSale = false;


        } catch (Exception ex) {
            db.closeDB();
        }
    }

    public ProductMasterBO getProductbyId(String productid) {
        ProductMasterBO product;
        int siz = bmodel.getCounterSaleBO().getmSalesproduct().size();
        if (siz == 0)
            return null;

        for (int i = 0; i < siz; ++i) {
            product = bmodel.getCounterSaleBO().getmSalesproduct().get(i);
            if (product.getProductID().equals(productid)) {
                return product;
            }
        }
        return null;
    }

    private void updateSchemeFreeProductSIH(int freeStockTypeId, DBUtil db) {
        for (SchemeBO schemeBO : bmodel.schemeDetailsMasterHelper.getAppliedSchemeList()) {

            if (schemeBO.isQuantityTypeSelected()) {
                List<SchemeProductBO> freeProductList = schemeBO.getFreeProducts();
                if (freeProductList != null) {

                    for (SchemeProductBO freeProductBO : freeProductList) {
                        if (freeProductBO.getQuantitySelected() > 0) {

                            db.updateSQL("UPDATE CS_SIHDetails"
                                    + " SET sih =sih-" + freeProductBO.getQuantitySelected()
                                    + " , counterid = " + bmodel.getCounterId()
                                    + " WHERE pid = " + freeProductBO.getProductId()
                                    + " AND stock_type =" + freeStockTypeId);

                        }
                    }
                }
            }

        }
    }

    public CustomerVisitTableBO setCustomerVisitTableBO(CustomerVisitTableBO customerVisitTableBO, String masterText, List<String> fieldList, List<List<String>> dataList, List<CustomerVisitTableBO> custList) {
        customerVisitTableBO.setMasterString(masterText);
        customerVisitTableBO.setFieldJsonArray(fieldList);
        customerVisitTableBO.setDataJsonArray(dataList);
        customerVisitTableBO.setErrorCodeString("0");
//        custList.add(customerVisitTableBO);
        return customerVisitTableBO;
    }

    public String setCustomerVisitTableJson(List<CustomerVisitTableBO> custList) {
        DbJsonPojo dbobj = new DbJsonPojo();
        dbobj.setTableJsonArray(custList);
        Gson gson1 = new Gson();
        String json1 = gson1.toJson(dbobj);
        System.out.println(json1);
        return json1;
    }

    ArrayList<String> names = new ArrayList<>();
    ArrayList<String> uidList = new ArrayList<>();
    ArrayList<String> phnoList = new ArrayList<>();

    public ArrayList<String> getCSCustomerVisitedNames() {
        return names;
    }

    public ArrayList<String> getCSCustomerVisitedUID() {
        return uidList;
    }

    public ArrayList<String> getCSCustomerVisitedPhNo() {
        return phnoList;
    }

    public void loadVisitedCustomer() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        try {
            db.createDataBase();
            db.openDataBase();
            String sqlHeader = "select DISTINCT CVH.uid,CM.counter_name,CVH.name,CVH.contactno" +
                    " from CS_CustomerVisitHeader CVH left join CS_CounterMaster CM on CVH.counter_id=CM.counter_id " +
                    "where CVH.upload='N'";
            Cursor cHeader = db.selectSQL(sqlHeader);
            names = new ArrayList<>();
            uidList = new ArrayList<>();
            phnoList = new ArrayList<>();
            if (cHeader.getCount() > 0) {
                while (cHeader.moveToNext()) {
                    uidList.add(cHeader.getString(cHeader.getColumnIndex("uid")));
                    names.add(cHeader.getString(cHeader.getColumnIndex("name")));
                    phnoList.add(cHeader.getString(cHeader.getColumnIndex("contactno")));
                }
            }

        } catch (Exception ex) {
            db.closeDB();
        }
    }


    public String loadMethod(String uid) {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        List<String> fieldList = new ArrayList<String>();
        List<List<String>> dataList = new ArrayList<List<String>>();
        List<String> innerDataList = new ArrayList<String>();
        CustomerVisitTableBO customerVisitTableBO = new CustomerVisitTableBO();
        List<CustomerVisitTableBO> custList = new ArrayList<CustomerVisitTableBO>();
        String header_uid = "";
        String result = "";

        try {
            db.createDataBase();
            db.openDataBase();
            String sqlHeader = "select DISTINCT CVH.uid,CM.counter_name,RM.RetailerName,CVH.date,CVH.name,CVH.address,CVH.contactno,CVH.freqvisit,CVH.refno,SLM.ListCode," +
                    "CVH.gender,CVH.email from CS_CustomerVisitHeader CVH left join CS_CounterMaster CM on CVH .counter_id=CM.counter_id " +
                    "left join RetailerMaster RM on CVH.retailerid=RM.RetailerID left join StandardListMaster SLM on CVH.age_group=SLM.ListId where CVH.upload='N' and CVH.uid=" + bmodel.QT(uid);
//        String sql = "select uid,date,name,address,contactno,freqvisit,refno,age_group,gender,email from CS_CustomerVisitHeader";
            Cursor cHeader = db.selectSQL(sqlHeader);
            fieldList.add("RetailerName");
            fieldList.add("CounterName");
            fieldList.add("Date");
            fieldList.add("Name");
            fieldList.add("Address");
            fieldList.add("ContactNo");
           // fieldList.add("FreqVisit");
            fieldList.add("RefId");
            fieldList.add("age_group");
            fieldList.add("gender");
            fieldList.add("email");
            if (cHeader.getCount() > 0) {
                while (cHeader.moveToNext()) {
                    innerDataList = new ArrayList<String>();
                    header_uid = cHeader.getString(cHeader.getColumnIndex("uid"));
                    innerDataList.add(cHeader.getString(cHeader.getColumnIndex("counter_name")));
                    innerDataList.add(cHeader.getString(cHeader.getColumnIndex("RetailerName")));
                    innerDataList.add(cHeader.getString(cHeader.getColumnIndex("date")));
                    innerDataList.add(cHeader.getString(cHeader.getColumnIndex("name")));
                    innerDataList.add(cHeader.getString(cHeader.getColumnIndex("address")));
                    innerDataList.add(cHeader.getString(cHeader.getColumnIndex("contactno")));
                   // innerDataList.add(cHeader.getString(cHeader.getColumnIndex("freqvisit")));
                    innerDataList.add(String.valueOf(cHeader.getInt(cHeader.getColumnIndex("refno"))));
                    innerDataList.add(cHeader.getString(cHeader.getColumnIndex("ListCode")));
                    innerDataList.add(cHeader.getString(cHeader.getColumnIndex("gender")));
                    innerDataList.add(cHeader.getString(cHeader.getColumnIndex("email")));


                    dataList.add(innerDataList);

                    if (cHeader.isLast()) {//CS_CustomerVisitHeader

                        custList.add(setCustomerVisitTableBO(customerVisitTableBO, "CS_CustomerVisited", fieldList, dataList, custList));
                        {
                            //CS_CustomerSaleDetails
                            String sqlSales = "select PM.PName,SM.ListCode,CSD.qty,CSD.price,CSD.value from CS_CustomerSaleDetails CSD " +
                                    "left join ProductMaster PM on CSD.pid =PM.PID left join StandardListMaster SM on CSD.uomid=SM.listid " +
                                    "where CSD.upload='N' and CSD.uid=" + bmodel.QT(header_uid);
                            Cursor cSales = db.selectSQL(sqlSales);
                            dataList = new ArrayList<List<String>>();
                            fieldList = new ArrayList<String>();
                            customerVisitTableBO = new CustomerVisitTableBO();

                            fieldList.add("ProductName");
                            fieldList.add("listcode");
                            fieldList.add("Qty");
                            fieldList.add("Price");
                            fieldList.add("Value");
                            if (cSales.getCount() > 0) {
                                while (cSales.moveToNext()) {
                                    innerDataList = new ArrayList<String>();

                                    innerDataList.add(cSales.getString(cSales.getColumnIndex("PName")));
                                    innerDataList.add(cSales.getString(cSales.getColumnIndex("ListCode")));
                                    innerDataList.add(cSales.getString(cSales.getColumnIndex("qty")));
                                    innerDataList.add(cSales.getString(cSales.getColumnIndex("price")));
                                    innerDataList.add(cSales.getString(cSales.getColumnIndex("value")));

                                    dataList.add(innerDataList);

                                    if (cSales.isLast()) {
                                        custList.add(setCustomerVisitTableBO(customerVisitTableBO, "CS_CustomerVisited_SaleDetails", fieldList, dataList, custList));
                                    }
                                }
                            } else {
                                result = setCustomerVisitTableJson(custList);
                            }
                        }
                        {
//CS_CustomerConcernDetails
                            String sqlConcern = "select AM.att_name,CCD.resolution,CCD.feedback from CS_CustomerConcernDetails CCD " +
                                    "left join AttributeMaster AM on CCD.attributeid=AM.att_id where upload='N' and " +
                                    "uid=" + bmodel.QT(header_uid);
                            Cursor cConcern = db.selectSQL(sqlConcern);
                            dataList = new ArrayList<List<String>>();
                            fieldList = new ArrayList<String>();
                            customerVisitTableBO = new CustomerVisitTableBO();
                            fieldList.add("AttributeName");
                            fieldList.add("Resolution");
                            fieldList.add("FeedBack");
                            if (cConcern.getCount() > 0) {
                                while (cConcern.moveToNext()) {
                                    innerDataList = new ArrayList<String>();

                                    innerDataList.add(cConcern.getString(cConcern.getColumnIndex("att_name")));
                                    innerDataList.add(cConcern.getString(cConcern.getColumnIndex("resolution")));
                                    innerDataList.add(cConcern.getString(cConcern.getColumnIndex("feedback")));

                                    dataList.add(innerDataList);

                                    if (cConcern.isLast()) {
                                        custList.add(setCustomerVisitTableBO(customerVisitTableBO, "CS_CustomerVisited_ConcernDetails", fieldList, dataList, custList));
                                    }
                                }
                            } else {
                                result = setCustomerVisitTableJson(custList);
                            }
                        }
                        {

                            //CS_CustomerTrialDetails
                            String sqlTrial = "select PM.PName,CTD.timetaken,CTD.result,CTD.feedback from CS_CustomerTrialDetails CTD " +
                                    "left join ProductMaster PM on CTD.pid=PM.PID where CTD.upload='N' and CTD.uid=" + bmodel.QT(header_uid);
                            Cursor cTrail = db.selectSQL(sqlTrial);
                            dataList = new ArrayList<List<String>>();
                            fieldList = new ArrayList<String>();
                            customerVisitTableBO = new CustomerVisitTableBO();
                            fieldList.add("ProductName");
                            fieldList.add("TimeSpent");
                            fieldList.add("Result");
                            fieldList.add("FeedBack");
                            if (cTrail.getCount() > 0) {
                                while (cTrail.moveToNext()) {
                                    innerDataList = new ArrayList<String>();

                                    innerDataList.add(cTrail.getString(cTrail.getColumnIndex("PName")));
                                    innerDataList.add(cTrail.getString(cTrail.getColumnIndex("timetaken")));
                                    innerDataList.add(cTrail.getString(cTrail.getColumnIndex("result")));
                                    innerDataList.add(cTrail.getString(cTrail.getColumnIndex("feedback")));

                                    dataList.add(innerDataList);

                                    if (cTrail.isLast()) {
                                        custList.add(setCustomerVisitTableBO(customerVisitTableBO, "CS_CustomerVisited_TrailDetails", fieldList, dataList, custList));
                                    }
                                }
                            } else {
                                result = setCustomerVisitTableJson(custList);
                            }

                        }
                        {//CS_CustomerSampleGivenDetails
                            String sqlSample = "select PM.PName,SM.listCode,CSGD.qty from CS_CustomerSampleGivenDetails CSGD left join ProductMaster PM on CSGD.pid =PM.PID left join StandardListMaster SM on CSGD.uomid=SM.listid where CSGD.upload='N' and CSGD.uid=" + bmodel.QT(header_uid);
                            Cursor cSample = db.selectSQL(sqlSample);
                            dataList = new ArrayList<List<String>>();
                            fieldList = new ArrayList<String>();
                            customerVisitTableBO = new CustomerVisitTableBO();
                            fieldList.add("ProductName");
                            fieldList.add("listCode");
                            fieldList.add("Qty");
                            if (cSample.getCount() > 0) {
                                while (cSample.moveToNext()) {
                                    innerDataList = new ArrayList<String>();

                                    innerDataList.add(cSample.getString(cSample.getColumnIndex("PName")));
                                    innerDataList.add(cSample.getString(cSample.getColumnIndex("listCode")));
                                    innerDataList.add(cSample.getString(cSample.getColumnIndex("qty")));

                                    dataList.add(innerDataList);

                                    if (cSample.isLast()) {
                                        custList.add(setCustomerVisitTableBO(customerVisitTableBO, "CS_CustomerVisited_SampleDetails", fieldList, dataList, custList));
                                        result = setCustomerVisitTableJson(custList);//when all data are available
                                    }
                                }
                            } else {
                                result = setCustomerVisitTableJson(custList);
                            }
                        }
                    }

                }
            } else {
                result = "";
            }

            db.closeDB();
        } catch (Exception ex) {
            db.closeDB();
            result = "";
        }
        return result;
    }

    public ArrayList<CounterSaleBO> getLstDraft() {
        return lstDraft;
    }

    public void setLstDraft(ArrayList<CounterSaleBO> lstDraft) {
        this.lstDraft = lstDraft;
    }

    ArrayList<CounterSaleBO> lstDraft;

    public void downloadDrafts() {
        lstDraft = new ArrayList<>();
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        String columns;
        String values;
        try {
            db.createDataBase();
            db.openDataBase();
            String sql = "select distinct CVH.uid,name,address,contactno,freqvisit,attributeId,resolution,CCD.feedback as consultFeedback," +
                    "sequance,CVH.Email,CVH.gender,CVH.age_group,CVH.billDiscPerc,CVH.discAmount,ifnull(CSD.uid,0) as isSaleDrafted from CS_CustomerVisitHeader CVH " +
                    "LEFT JOIN CS_CustomerConcernDetails CCD ON CCD.uid=CVH.uid left join  CS_CustomerSaleDetails CSD ON CSD.uid=CVH.uid where CVH.upload='I'";
            Cursor c = db.selectSQL(sql);
            if (c.getCount() > 0) {
                CounterSaleBO bo;
                while (c.moveToNext()) {
                    bo = new CounterSaleBO();
                    bo.setUid(c.getString(0));
                    if (c.getString(1) != null)
                        bo.setCustomerName(c.getString(1));

                    if (c.getString(2) != null)
                        bo.setAddress(c.getString(2));

                    if (c.getString(3) != null)
                        bo.setContactNumber(c.getString(3));

                    if (c.getString(4) != null)
                        bo.setFreqVisit(c.getString(4));

                    bo.setAttributeId(c.getInt(5));

                    if (c.getString(6) != null)
                        bo.setResolution(c.getString(6));

                    if (c.getString(7) != null)
                        bo.setConnsultingFeedback(c.getString(7));

                    if (c.getString(9) != null)
                        bo.setEmail(c.getString(9));

                    if (c.getString(10) != null)
                        bo.setGender(c.getString(10));

                    if (c.getString(11) != null)
                        bo.setAgeGroup(c.getString(11));

                    bo.setDisPercentage(c.getDouble(12));
                    bo.setDisAmount(c.getDouble(13));

                    bo.setSequance(c.getInt(8));
                    bo.setLastUid(c.getString(0));
                    bo.setDraft(true);

                    bo.setmTestProducts(getAppliedTest(c.getString(0)));

                    if (c.getString(c.getColumnIndex("isSaleDrafted")).equals("0"))
                        bo.setSaleDrafted(false);
                    else
                        bo.setSaleDrafted(true);
//                    bo.setmSampleProducts(getDraftedSampleProducts(c.getString(0)));
//                    bo.setmSalesproduct(getDraftedSaleProducts(c.getString(0)));
                    lstDraft.add(bo);
                }
            }

            db.closeDB();
        } catch (Exception ex) {
            db.closeDB();
        }

    }

    public ArrayList<ApplyBo> getAppliedTest(String uid) {
        ArrayList<ApplyBo> lst = new ArrayList<>();
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        String columns;
        String values;
        try {
            db.createDataBase();
            db.openDataBase();
            String sql = "select pid,timetaken,result,feedback from CS_CustomerTrialDetails where upload='I' and uid='" + uid + "'";
            Cursor c = db.selectSQL(sql);
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    ApplyBo bo = new ApplyBo();
                    bo.setTestedProductId(c.getInt(0));
                    String timpeSplit[] = c.getString(1).split(":");
                    if (timpeSplit.length == 2) {
                        bo.setTesthour(Integer.parseInt(timpeSplit[0]));
                        bo.setTestTime(Integer.parseInt(timpeSplit[1]));
                    }
                    bo.setResult(c.getString(2));
                    bo.setTestFeedback(c.getString(3));
                    lst.add(bo);
                }
            }
        } catch (Exception ex) {

        }
        return lst;
    }


    public ArrayList<ProductMasterBO> getDraftedSampleProducts(String uid) {
        ArrayList<ProductMasterBO> lst = new ArrayList<>();
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        String columns;
        String values;
        try {
            db.createDataBase();
            db.openDataBase();
            String sql = "select pid,uomid,qty from CS_CustomerSampleGivenDetails where upload='I' and uid='" + uid + "'";
            Cursor c = db.selectSQL(sql);
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    ProductMasterBO bo = new ProductMasterBO(bmodel.productHelper.getProductMasterBOById(c.getString(0)));
                    if (bo.getPcUomid() == c.getInt(1)) {
                        bo.setCsPiece(c.getInt(2));
                    } else if (bo.getCaseUomId() == c.getInt(1)) {
                        bo.setCsCase(c.getInt(2));
                    } else if (bo.getOuUomid() == c.getInt(1)) {
                        bo.setCsOuter(c.getInt(2));
                    }
                    lst.add(bo);
                }
            }
        } catch (Exception ex) {

        }
        return lst;
    }


    public ArrayList<ProductMasterBO> getDraftedSaleProducts(String uid) {
        ArrayList<ProductMasterBO> lst = new ArrayList<>();
        HashMap<String, ProductMasterBO> mProductBOById;
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        double totValue;
        int values = 0;
        try {
            db.createDataBase();
            db.openDataBase();
            String sql = "select CS.pid,CS.uomid,CS.qty,PM.pcode,PM.pname,PM.parentid,PM.psname,PM.barcode"
                    + ",PM.dUomQty,PM.dOUomQty,PM.MRP,PM.piece_uomid,PM.dUomId,PM.dOuomid,CS.isSalable,CS.value from CS_CustomerSaleDetails CS"
                    + " LEFT JOIN productMaster PM ON PM.pid=CS.pid"
                    + " where CS.upload='I' and CS.uid='" + uid + "'";
            Cursor c = db.selectSQL(sql);
            if (c.getCount() > 0) {
                bmodel.getCounterSaleBO().setmSalesproduct(new ArrayList<ProductMasterBO>());
                mProductBOById = new HashMap<>();

                ProductMasterBO productBo;
                while (c.moveToNext()) {
                    if (mProductBOById.get(c.getString(0)) != null) {
                        productBo = mProductBOById.get(c.getString(0));
                    } else {
                        productBo = new ProductMasterBO();

                        productBo.setProductID(c.getString(0));
                        productBo.setProductCode(c.getString(3));
                        productBo.setProductName(c.getString(4));
                        productBo.setParentid(c.getInt(5));
                        productBo.setProductShortName(c.getString(6));
                        productBo.setBarCode(c.getString(7));
                        productBo.setCaseSize(c.getInt(8));
                        productBo.setOutersize(c.getInt(9));
                        productBo.setMRP(c.getDouble(10));
                        productBo.setPcUomid(c.getInt(11));
                        productBo.setCaseUomId(c.getInt(12));
                        productBo.setOuUomid(c.getInt(13));

                        mProductBOById.put(c.getString(0), productBo);
                    }


                    if (c.getInt(11) == c.getInt(1)) {
                        if (c.getInt(14) == 0)
                            productBo.setCsFreePiece(c.getInt(2));
                        else if (c.getInt(14) != 0)
                            productBo.setCsPiece(c.getInt(2));
                    } else if (c.getInt(12) == c.getInt(1)) {
                        productBo.setCsCase(c.getInt(2));
                    } else if (c.getInt(13) == c.getInt(1)) {
                        productBo.setCsOuter(c.getInt(2));
                    }
                    productBo.setCsTotal(c.getDouble(15));
                    lst.add(productBo);

                }
            }
        } catch (Exception ex) {
            Log.e("Exception", String.valueOf(ex));

        }
        return lst;
    }


    public ArrayList<AttributeBO> getLstConcern() {
        return lstConcern;
    }

    public void setLstConcern(ArrayList<AttributeBO> lstConcern) {
        this.lstConcern = lstConcern;
    }

    public void getConcernRaised() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        lstConcern = new ArrayList<>();
        try {

            db.openDataBase();
            String sql = "select att_id,att_name from AttributeMaster AM INNER JOIN StandardListMaster ST ON ST.listid=AM.att_type_lov_id where ST.ListCode='CONCERN' and ST.ListType='ATTRIBUTE_TYPE'";

            lstConcern.add(new AttributeBO(0, "-Select-"));
            Cursor c = db.selectSQL(sql);
            if (c != null) {
                AttributeBO bo;
                while (c.moveToNext()) {
                    bo = new AttributeBO();
                    bo.setAttributeId(c.getInt(0));
                    bo.setAttributeName(c.getString(1));
                    lstConcern.add(bo);
                }
            }

            db.closeDB();
        } catch (Exception ex) {
            db.closeDB();
        }
    }


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getItemIndex(int prodid, ArrayList<ProductMasterBO> prodList) {
        int size = prodList.size();

        for (int i = 0; i < size; i++) {
            ProductMasterBO bo = (ProductMasterBO) prodList.get(i);
            if (bo.getProductID().equals(prodid + "")) {
                return i;
            }
        }
        return -1;
    }

    public int getItemFeedBackIndex(String feedBack, ArrayList<StandardListBO> prodList) {
        int size = prodList.size();

        for (int i = 0; i < size; i++) {
            StandardListBO bo = (StandardListBO) prodList.get(i);
            if (bo.getListName().equals(feedBack)) {
                return i;
            }
        }
        return -1;
    }

    public int getAttributeItemIndex(int att_id, ArrayList<AttributeBO> attributeList) {
        int size = attributeList.size();

        for (int i = 0; i < size; i++) {
            AttributeBO bo = (AttributeBO) attributeList.get(i);
            if (bo.getAttributeId() == att_id) {
                return i;
            }
        }
        return -1;
    }


    public void discardDraft() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        try {
            db.createDataBase();
            db.openDataBase();
            db.deleteSQL("CS_CustomerVisitHeader", "upload='I'", false);
            db.deleteSQL("CS_CustomerConcernDetails", "upload='I'", false);
            db.deleteSQL("CS_CustomerTrialDetails", "upload='I'", false);
            db.deleteSQL("CS_CustomerSaleDetails", "upload='I'", false);
            db.deleteSQL("CS_CustomerSampleGivenDetails", "upload='I'", false);
            db.closeDB();
        } catch (Exception ex) {
            db.closeDB();
        }
    }

    public String downloadCustomerSearchUrl() {

        String url = "";
        DBUtil db = null;
        try {

            db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            String query = "select URL from UrlDownloadMaster where typecode='CUST_SEARCH_RT'";
            Cursor c = db.selectSQL(query);
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    url = c.getString(0);
                }
            }
            c.close();

        } catch (Exception e) {
            Commons.printException(e);
        } finally {
            db.closeDB();
        }

        return url;

    }

    public boolean isClosingStockDone() {
        boolean flag = false;
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db.selectSQL("select uid from "
                    + DataMembers.tbl_cs_closingstockheader + " where retailerid="
                    + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()));
            if (c != null) {
                if (c.getCount() > 0) {
                    flag = true;
                }
            }

            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return flag;
    }

    public boolean isCustomerVisitkDone() {
        boolean flag = false;
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db.selectSQL("select uid from "
                    + DataMembers.tbl_CS_CustomerVisitHeader + " where retailerid="
                    + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()));
            if (c != null) {
                if (c.getCount() > 0) {
                    flag = true;
                }
            }

            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return flag;
    }

    public boolean isPlanogramDone() {
        boolean flag = false;
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db.selectSQL("select uid from "
                    + DataMembers.tbl_PlanogramHeader + " where retailerid="
                    + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID())
                    + " and counterid=0");
            if (c != null) {
                if (c.getCount() > 0) {
                    flag = true;
                }
            }

            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return flag;
    }

    public boolean isCounterPlanogramDone() {
        boolean flag = false;
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db.selectSQL("select uid from "
                    + DataMembers.tbl_PlanogramHeader + " where retailerid="
                    + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID())
                    + " and counterid!=0");
            if (c != null) {
                if (c.getCount() > 0) {
                    flag = true;
                }
            }

            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return flag;
    }

    public boolean isCompetitorTrackingDone() {
        boolean flag = false;
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db.selectSQL("select tid from "
                    + DataMembers.tbl_CompetitorHeader + " where retailerid="
                    + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID())
                    + " and counterid!=0");
            if (c != null) {
                if (c.getCount() > 0) {
                    flag = true;
                }
            }

            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return flag;
    }

    public boolean downloadCSStock() {
        boolean flag = false;
        try {
            // String normalProduct="NORMAL";
            String freeProduct = "FREE";
            String testProduct = "TESTER";
            String accessory = "ACCESS";

            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db.selectSQL("select pid,sih,ifnull(SM.listcode,'') from CS_SIHDetails CS left join StandardListMaster SM ON SM.listid=CS.stock_type");
            if (c != null) {
                if (c.getCount() > 0) {
                    while (c.moveToNext()) {
                        String type = c.getString(2);
                        if (bmodel.configurationMasterHelper.IS_GROUP_PRODUCTS_IN_COUNTER_SALES) {

                            ProductMasterBO childBO = bmodel.productHelper.getProductMasterBOById(c.getString(0) + "");
                            if (childBO != null) {
                                ProductMasterBO parentBO = bmodel.productHelper.getProductMasterBOById(childBO.getParentid() + "");

                                if (type.equalsIgnoreCase(freeProduct) || type.equalsIgnoreCase(accessory)) {
                                    //Free product or Accessories
                                    childBO.setCsFreeSIH(c.getInt(1));

                                    if (parentBO != null)
                                        parentBO.setCsFreeSIH(parentBO.getCsFreeSIH() + c.getInt(1));

                                } else if (type.equalsIgnoreCase(testProduct)) {
                                    childBO.setCsTestSIH(c.getInt(1));

                                    if (parentBO != null)
                                        parentBO.setCsTestSIH(parentBO.getCsTestSIH() + c.getInt(1));
                                } else {
                                    // Normal product
                                    childBO.setSIH(c.getInt(1));

                                    if (parentBO != null)
                                        parentBO.setSIH(parentBO.getSIH() + c.getInt(1));
                                }
                            }


                        } else {

                            if (type.equalsIgnoreCase(freeProduct) || type.equalsIgnoreCase(accessory)) {
                                if (bmodel.productHelper.getProductMasterBOById(c.getString(0)) != null)
                                    bmodel.productHelper.getProductMasterBOById(c.getString(0)).setCsFreeSIH(c.getInt(1));
                            } else if (type.equalsIgnoreCase(testProduct)) {
                                if (bmodel.productHelper.getProductMasterBOById(c.getString(0)) != null)
                                    bmodel.productHelper.getProductMasterBOById(c.getString(0)).setCsTestSIH(c.getInt(1));
                            } else {
                                // Normal product
                                if (bmodel.productHelper.getProductMasterBOById(c.getString(0)) != null)
                                    bmodel.productHelper.getProductMasterBOById(c.getString(0)).setSIH(c.getInt(1));
                            }

                        }
                    }
                }
            }

            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return flag;
    }


    public boolean updatetestStock() {
        boolean flag = false;
        try {
            String testProduct = "TESTER";

            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db.selectSQL("select CS.pid,CS.sih,ifnull(SM.listcode,'') from CS_SIHDetails CS inner join ProductMaster PM on " +
                    "CS.pid=PM.pid left join StandardListMaster SM ON SM.listid=CS.stock_type");
            if (c != null) {
                if (c.getCount() > 0) {
                    while (c.moveToNext()) {
                        String type = c.getString(2);
                        if (type.equalsIgnoreCase(testProduct)) {
                            bmodel.productHelper.getProductMasterBOById(c.getString(0)).setCsTestSIH(
                                    bmodel.productHelper.getProductMasterBOById(c.getString(0)).getCsTestSIH() + c.getInt(1));
                        }

                    }

                }
            }

            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return flag;
    }

    public static ArrayList<CS_StockReasonBO> cloneReasonList(
            ArrayList<CS_StockReasonBO> list) {
        ArrayList<CS_StockReasonBO> clone = new ArrayList<CS_StockReasonBO>(list.size());
        for (CS_StockReasonBO item : list)
            clone.add(new CS_StockReasonBO(item));
        return clone;
    }

    public void updateStockReasons() {
        for (ProductMasterBO bo : bmodel.productHelper.getTaggedProducts()) {
            for (LocationBO locationBO : bo.getLocations()) {
                locationBO.setLstStockReasons(cloneReasonList(bmodel.reasonHelper.getLstCSstockReasons()));
            }

        }
    }

    public int getmNumberOfTabs() {
        return mNumberOfTabs;
    }

    private int mNumberOfTabs;
    public HashMap<String, String> downloadCustomerHeaderInformation(String uid, boolean isFromReport) {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);

        HashMap<String, String> lst = new HashMap<>();
        mNumberOfTabs = 0;
        try {
            db.createDataBase();
            db.openDataBase();
            String sqlHeader = "select DISTINCT CVH.uid,CVH.date,CVH.name,CVH.address,CVH.contactno,"+(isFromReport?"SLM.listName":"CVH.age_group") +
                    ",CVH.gender,CVH.email from CS_CustomerVisitHeader CVH " +
                    "left join RetailerMaster RM on CVH.retailerid=RM.RetailerID "+
                    (isFromReport?"left join StandardListMaster SLM on CVH.age_group=SLM.ListId ":"") +
                    (isFromReport ? "where CVH.upload='N' " : "where CVH.upload='S' ") +
                    (isFromReport ? "and CVH.uid=" + bmodel.QT(uid) : "") +
                    (isFromReport ? "" : " order by date(CVH.date) DESC Limit 1");

            Cursor c = db.selectSQL(sqlHeader);
            if (c.getCount() > 0) {
                mNumberOfTabs += 1;
                if (c.moveToNext()) {
                    lst.put("uid", c.getString(0));
                    lst.put("name", c.getString(2));
                    lst.put("address", c.getString(3));
                    lst.put("contactno", c.getString(4));
                    lst.put("age", c.getString(5));
                    lst.put("gender", c.getString(6));
                    lst.put("email", c.getString(7));

                }

            }


        } catch (Exception ex) {
            Commons.printException(ex);

        }
        return lst;
    }

    public ArrayList<HashMap<String, String>> downloadCustomerSalesInformation(String uid) {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);

        ArrayList<HashMap<String, String>> data = new ArrayList<>();
        HashMap<String, String> lst ;
        try {
            db.createDataBase();
            db.openDataBase();
            String sqlHeader = "select DISTINCT CVH.uid,CVH.date,CVH.retailerName,CVH.counterName,CVH.location,CSD.uomCode,CSD.qty," +
                    "CSD.value,CSD.ProductName from CS_CustomerVisitHeader CVH " +
                    "left join CS_CustomerSaleDetails CSD on CVH.uid=CSD.uid " +
                    "where CVH.uid=" + bmodel.QT(uid) +
                    " order by CVH.retailerName,CVH.counterName";


            Cursor c = db.selectSQL(sqlHeader);
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    lst = new HashMap<>();
                    lst.put("date", c.getString(1));
                    lst.put("retailername", c.getString(2));
                    lst.put("countername", c.getString(3));
                    lst.put("location", c.getString(4));
                    lst.put("uomCode", c.getString(5));
                    lst.put("qty", c.getString(6));
                    lst.put("value", c.getString(7));
                    lst.put("pname", c.getString(8));

                    data.add(lst);

                }

            }


        } catch (Exception ex) {
            Commons.printException(ex);

        }
        return data;
    }

    public ArrayList<HashMap<String, String>> downloadCustomerTestInformation(String uid) {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);

        ArrayList<HashMap<String, String>> data = new ArrayList<>();
        HashMap<String, String> lst;
        try {
            db.createDataBase();
            db.openDataBase();
            String sqlHeader = "select DISTINCT CSD.productName,CSD.timetaken,CSD.result,CSD.feedback " +
                    "from CS_CustomerTrialDetails CSD " +
                    "where CSD.uid=" + bmodel.QT(uid);


            Cursor c = db.selectSQL(sqlHeader);
            if (c.getCount() > 0) {
                mNumberOfTabs += 1;
                while (c.moveToNext()) {
                    lst= new HashMap<>();
                    lst.put("productName", c.getString(0));
                    lst.put("timetaken", c.getString(1));
                    lst.put("result", c.getString(2));
                    lst.put("feedback", c.getString(3));


                    data.add(lst);

                }

            }


        } catch (Exception ex) {
            Commons.printException(ex);

        }
        return data;
    }


    public void deleteSearchRecords(){
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.openDataBase();
        db.deleteSQL("CS_CustomerVisitHeader","upload='S'",false);
        db.deleteSQL("CS_CustomerTrialDetails","upload='S'",false);
        db.deleteSQL("CS_CustomerSampleGivenDetails","upload='S'",false);
        db.deleteSQL("CS_CustomerSaleDetails","upload='S'",false);
        db.deleteSQL("CS_CustomerConcernDetails","upload='S'",false);
        db.closeDB();
    }
    public void deleteCurrentDraft(){
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.openDataBase();
        if (bmodel.getCounterSaleBO()!=null&&bmodel.getCounterSaleBO().isDraft()) {
            db.deleteSQL("CS_CustomerVisitHeader", "uid='" + bmodel.getCounterSaleBO().getLastUid() + "'", false);
            db.deleteSQL("CS_CustomerConcernDetails", "uid='" + bmodel.getCounterSaleBO().getLastUid() + "'", false);
            db.deleteSQL("CS_CustomerTrialDetails", "uid='" + bmodel.getCounterSaleBO().getLastUid() + "'", false);
            db.deleteSQL("CS_CustomerSaleDetails", "uid='" + bmodel.getCounterSaleBO().getLastUid() + "'", false);
            db.deleteSQL("CS_CustomerSampleGivenDetails", "uid='" + bmodel.getCounterSaleBO().getLastUid() + "'", false);
        }
        db.closeDB();
    }

}
