package com.ivy.sd.png.provider;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;

import com.ivy.lib.Utils;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.RetailerContractBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;


public class RetailerContractHelper {
    private Context mContext;
    private BusinessModel bmodel;
    private static RetailerContractHelper instance = null;
    private static final String TAG = "RetailerContractHelper";

    private ArrayList<RetailerContractBO> mRetailerContractList;
    private ArrayList<RetailerContractBO> mRenewedContractList;

    protected RetailerContractHelper(Context context) {
        this.mContext = context;
        this.bmodel = (BusinessModel) context;
    }

    public static RetailerContractHelper getInstance(Context context) {
        if (instance == null) {
            instance = new RetailerContractHelper(context);
        }
        return instance;
    }

    public ArrayList<RetailerContractBO> getRetailerContractList() {
        if (mRetailerContractList != null) {
            return mRetailerContractList;
        }
        return new ArrayList<RetailerContractBO>();
    }

    public ArrayList<RetailerContractBO> getmRenewedContractList() {
        if (mRenewedContractList != null) {
            return mRenewedContractList;
        }
        return new ArrayList<RetailerContractBO>();
    }

    public void downloadRetailerContract(String retailerid) {
        mRetailerContractList = new ArrayList<RetailerContractBO>();
        RetailerContractBO mRetailerContractBO;
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("select contractid,ContractDesc,ContractType,StartDate,EndDate,RetailerId,Status,templateid,typelovid,cs_id " +
                    "from RetailerContract where retailerid= " + bmodel.QT(retailerid));

            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    mRetailerContractBO = new RetailerContractBO();
                    mRetailerContractBO.setContractid(c.getString(0));
                    mRetailerContractBO.setContractname(c.getString(1));
                    mRetailerContractBO.setContracttype(c.getString(2));
                    mRetailerContractBO.setStartdate(c.getString(3));
                    mRetailerContractBO.setEnddate(c.getString(4));
                    mRetailerContractBO.setRetailerid(c.getString(5));
                    mRetailerContractBO.setStatus(c.getString(6));
                    mRetailerContractBO.setTemplateId(c.getString(7));
                    mRetailerContractBO.setTypelovid(c.getString(8));
                    mRetailerContractBO.setCs_id(c.getString(9));

                    mRetailerContractList.add(checkAlreadyRenwed(mRetailerContractBO));
                }
            }
            c.close();
        } catch (Exception e) {
            Commons.printException(e);
        } finally {
            db.closeDB();
        }
    }

    //Method to get data from transaction table
    private RetailerContractBO checkAlreadyRenwed(RetailerContractBO mRetailerContractBO) {
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("select upload from RetailerContractRenewalDetails  " +
                    "where Retailerid= " + bmodel.QT(mRetailerContractBO.getRetailerid()) +
                    "AND ContractID= " + bmodel.QT(mRetailerContractBO.getCs_id()));

            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    mRetailerContractBO.setRenewed(true);
                    if (c.getString(0).equals("Y"))
                        mRetailerContractBO.setUploaded(true);

                }
            }
            c.close();
        } catch (Exception e) {
            Commons.printException(e);
        } finally {
            db.closeDB();
        }
        return mRetailerContractBO;
    }

    public void downloadRenewedContract(String retailerid) {
        mRenewedContractList = new ArrayList<RetailerContractBO>();
        RetailerContractBO mRetailerContractBO;
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("select RetailerId,ContractId,Tid,startdate,enddate,description,typelovid,ContractType "
                    +"from RetailerContractRenewalDetails where retailerid=" + bmodel.QT(retailerid)
                    +" AND upload =" + bmodel.QT("N"));

            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    mRetailerContractBO = new RetailerContractBO();
                    mRetailerContractBO.setRetailerid(c.getString(0));
                    mRetailerContractBO.setContractid(c.getString(1));
                    mRetailerContractBO.setTid(c.getString(2));
                    mRetailerContractBO.setStartdate(c.getString(3));
                    mRetailerContractBO.setEnddate(c.getString(4));
                    mRetailerContractBO.setContractname(c.getString(5));
                    mRetailerContractBO.setTypelovid(c.getString(6));
                    mRetailerContractBO.setContracttype(c.getString(7));

                    mRenewedContractList.add(mRetailerContractBO);
                }
            }
            c.close();
        } catch (Exception e) {
            Commons.printException(e);
        } finally {
            db.closeDB();
        }
    }

    public void saveRetailersContract(RetailerContractBO retailerContractBO) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            db.deleteSQL("RetailerContractRenewalDetails", "ContractId="
                    + bmodel.QT(retailerContractBO.getContractid()), false);

            String columnsNew = "Retailerid,ContractID,Tid,startdate,enddate,utcDate,templateid,description,typelovid,ContractType";
            String values;

            values = bmodel.QT(retailerContractBO.getRetailerid()) + "," +
                    bmodel.QT(retailerContractBO.getCs_id()) + "," +
                    bmodel.QT(bmodel.userMasterHelper.getUserMasterBO().getUserid() + SDUtil.now(SDUtil.DATE_TIME_ID)) + "," +
                    bmodel.QT(retailerContractBO.getStartdate()) + "," +
                    bmodel.QT(retailerContractBO.getEnddate()) + "," +
                    DatabaseUtils.sqlEscapeString( Utils.getGMTDateTime("yyyy/MM/dd HH:mm:ss")) + "," +
                    bmodel.QT(retailerContractBO.getTemplateId()) + "," +
                    bmodel.QT(retailerContractBO.getContractname()) + "," +
                    bmodel.QT(retailerContractBO.getTypelovid()) + "," +
                    bmodel.QT(retailerContractBO.getContracttype());

            db.insertSQL("RetailerContractRenewalDetails", columnsNew, values);
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public void deleteRenewal(String Tid) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            db.deleteSQL("RetailerContractRenewalDetails", "Tid="
                    + bmodel.QT(Tid), false);


            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }
}
