package com.ivy.cpg.view.retailercontract;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;

import com.ivy.lib.Utils;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.StringUtils;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import io.reactivex.Single;


public class RetailerContractHelper {
    private Context mContext;
    private BusinessModel bmodel;
    private static RetailerContractHelper instance = null;

    private ArrayList<RetailerContractBO> mRetailerContractList;
    private ArrayList<RetailerContractBO> mRenewedContractList;

    protected RetailerContractHelper(Context context) {
        this.mContext = context;
        this.bmodel = (BusinessModel) context.getApplicationContext();
    }

    public static RetailerContractHelper getInstance(Context context) {
        if (instance == null) {
            instance = new RetailerContractHelper(context);
        }
        return instance;
    }

    ArrayList<RetailerContractBO> getRetailerContractList() {
        if (mRetailerContractList != null) {
            return mRetailerContractList;
        }
        return new ArrayList<>();
    }

    ArrayList<RetailerContractBO> getmRenewedContractList() {
        if (mRenewedContractList != null) {
            return mRenewedContractList;
        }
        return new ArrayList<>();
    }

    public void downloadRetailerContract(String retailerid) {
        mRetailerContractList = new ArrayList<>();
        RetailerContractBO mRetailerContractBO;
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.createDataBase();
            db.openDataBase();
            String sb ="select contractid,ContractDesc,ContractType,StartDate,EndDate,RetailerId,Status,templateid,typelovid,cs_id " +
                    "from RetailerContract where retailerid= " + StringUtils.getStringQueryParam(retailerid);

            Cursor c = db.selectSQL(sb);
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
            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.createDataBase();
            db.openDataBase();
            String sb ="select upload from RetailerContractRenewalDetails  " +
                    "where Retailerid= " + StringUtils.getStringQueryParam(mRetailerContractBO.getRetailerid()) +
                    "AND ContractID= " + StringUtils.getStringQueryParam(mRetailerContractBO.getCs_id());

            Cursor c = db.selectSQL(sb);
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
        mRenewedContractList = new ArrayList<>();
        RetailerContractBO mRetailerContractBO;
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.createDataBase();
            db.openDataBase();
            String sb = "select RetailerId,ContractId,Tid,startdate,enddate,description,typelovid,ContractType "
                    +"from RetailerContractRenewalDetails where retailerid=" + StringUtils.getStringQueryParam(retailerid)
                    +" AND upload =" + StringUtils.getStringQueryParam("N");

            Cursor c = db.selectSQL(sb);
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

    private void saveRetailersContract(RetailerContractBO retailerContractBO) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            db.deleteSQL("RetailerContractRenewalDetails", "ContractId="
                    + StringUtils.getStringQueryParam(retailerContractBO.getContractid()), false);

            String columnsNew = "Retailerid,ContractID,Tid,startdate,enddate,utcDate,templateid,description,typelovid,ContractType";
            String values;

            values = StringUtils.getStringQueryParam(retailerContractBO.getRetailerid()) + "," +
                    StringUtils.getStringQueryParam(retailerContractBO.getCs_id()) + "," +
                    StringUtils.getStringQueryParam(bmodel.userMasterHelper.getUserMasterBO().getUserid() + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID)) + "," +
                    StringUtils.getStringQueryParam(retailerContractBO.getStartdate()) + "," +
                    StringUtils.getStringQueryParam(retailerContractBO.getEnddate()) + "," +
                    DatabaseUtils.sqlEscapeString( Utils.getGMTDateTime("yyyy/MM/dd HH:mm:ss")) + "," +
                    StringUtils.getStringQueryParam(retailerContractBO.getTemplateId()) + "," +
                    StringUtils.getStringQueryParam(retailerContractBO.getContractname()) + "," +
                    StringUtils.getStringQueryParam(retailerContractBO.getTypelovid()) + "," +
                    StringUtils.getStringQueryParam(retailerContractBO.getContracttype());

            db.insertSQL("RetailerContractRenewalDetails", columnsNew, values);
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private void deleteRenewal(String Tid) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            db.deleteSQL("RetailerContractRenewalDetails", "Tid="
                    + StringUtils.getStringQueryParam(Tid), false);


            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    Single<Boolean> saveRetailerContract(final RetailerContractBO retailerContractBO) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                saveRetailersContract(retailerContractBO);
                downloadRenewedContract(retailerContractBO.getRetailerid());
                downloadRetailerContract(retailerContractBO.getRetailerid());
                return Boolean.TRUE;
            }
        });
    }

    Single<Boolean> deleteRetailerContract(final String tid,final String retailerId) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                deleteRenewal(tid);
                downloadRenewedContract(retailerId);
                downloadRetailerContract(retailerId);
                return Boolean.TRUE;
            }
        });
    }

}
