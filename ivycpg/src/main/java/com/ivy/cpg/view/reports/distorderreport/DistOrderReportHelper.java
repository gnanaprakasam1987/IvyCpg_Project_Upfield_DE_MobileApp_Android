package com.ivy.cpg.view.reports.distorderreport;

import android.content.Context;
import android.database.Cursor;

import com.ivy.cpg.view.reports.orderreport.OrderReportBO;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;

/**
 * Created by Hanifa on 31/7/18.
 */

public class DistOrderReportHelper {
    private BusinessModel bmodel;
    private static DistOrderReportHelper instance = null;
    private Context mContext;

    private DistOrderReportHelper(Context context) {
        this.mContext = context;
        this.bmodel = (BusinessModel) context.getApplicationContext();
    }

    public static DistOrderReportHelper getInstance(Context context){
        if(instance ==null) {
            instance = new DistOrderReportHelper(context);
        }
        return instance;
    }

    /**
     * Load distributor Order Report
     *
     * @return reportdistordbooking array list
     */
    public ArrayList<DistOrderReportBo> downloadDistributorOrderReport() {
        DBUtil db = null;
        ArrayList<DistOrderReportBo> reportdistordbooking = null;
        try {
            DistOrderReportBo orderreport;
            db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("select DOH.uid,DOH.distid,DM.DName,DOH.TotalValue,DOH.LPC,DOH.upload From DistOrderHeader DOH");
            sb.append(" Inner join DistributorMaster DM on DOH.distid = DM.Did ");
            sb.append("inner join DistOrderDetails DOD on DOH.Uid=DOD.Uid Group by DOH.uid,DM.DName");
            Cursor c = db.selectSQL(sb.toString());
            reportdistordbooking = new ArrayList<>();
            if (c != null) {
                while (c.moveToNext()) {
                    orderreport = new DistOrderReportBo();
                    orderreport.setOrderId(c.getString(0));
                    orderreport.setDistributorId(c.getInt(1));
                    orderreport.setDistributorName(c.getString(2));
                    orderreport.setOrderTotal(c.getDouble(3));
                    orderreport.setLpc(c.getString(4));

                    orderreport.setUpload(c.getString(5));
                    reportdistordbooking.add(orderreport);
                }
                c.close();
            }
        } catch (Exception e) {
            Commons.printException(e + "");
        } finally {
            if (db != null)
                db.closeDB();
        }
        return reportdistordbooking;
    }

    public int getorderbookingCount(String tableName) {
        int tot = 0;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Cursor c = db.selectSQL("select count (distinct retailerid) from "
                    + tableName);
            if (c != null) {
                if (c.moveToNext()) {
                    tot = c.getInt(0);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e + "");
        }

        return tot;
    }


    public int getavglinesfororderbooking(String tableName) {
        int tot = 0;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Cursor c = db.selectSQL("select sum(linespercall) from "
                    + tableName);
            if (c != null) {
                if (c.moveToNext()) {
                    tot = c.getInt(0);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e + "");
        }
        return tot;
    }






    public ArrayList<DistOrderReportBo> distOrderReportDetail(String orderId) {
        ArrayList<DistOrderReportBo> distributorOrderList = null;
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("select PM.pname,PM.psname,DOD.pid,DOD.qty,DOD.uomid,DOD.uomcount,DOD.batchid,BM.batchNum,DOD.LineValue from DistOrderDetails ");
            sb.append("DOD inner join ProductMaster PM on PM.pid=DOD.pid Left Join BatchMaster BM on DOD.pid=BM.pid and DOD.batchid=BM.batchid");
            sb.append(" where  DOD.uid=" + bmodel.QT(orderId));

            Cursor c = db.selectSQL(sb.toString());
            if (c != null) {
                distributorOrderList = new ArrayList<>();
                while (c.moveToNext()) {
                    String pName = c.getString(0);
                    String pSname = c.getString(1);
                    String pid = c.getString(2);
                    int qty = c.getInt(3);
                    int uomid = c.getInt(4);
                    int uomCount = c.getInt(5);
                    int batchid = c.getInt(6);
                    String batchNum = c.getString(7);
                    float totalValue = c.getFloat(8);
                    setDistOrderReportDetails(pName, pSname, pid, qty, uomid, uomCount, batchid, batchNum, totalValue, distributorOrderList);

                }
            }
        } catch (Exception e) {
            Commons.printException(e + " ");
        } finally {
            if (db != null)
                db.closeDB();
        }
        return distributorOrderList;
    }



    private void setDistOrderReportDetails(String pname, String pSname, String pid, int qty, int uomid, int uomCount, int batchid, String batchnum, float totalValue, ArrayList<DistOrderReportBo> distReportList) {
        DistOrderReportBo orderReportBO;

        if (distReportList == null) {
            distReportList = new ArrayList<>();

        }
        ProductMasterBO productBo = bmodel.productHelper.getProductMasterBOById(pid);
        if (productBo != null) {
            int size = distReportList.size();
            if (size > 0) {
                orderReportBO = distReportList.get(size - 1);
                if (pid.equals(orderReportBO.getProductId()) &&
                        batchid == orderReportBO.getBatchId()) {
                    if (uomid == productBo.getPcUomid()) {
                        orderReportBO.setPQty(qty);

                    } else if (uomid == productBo.getCaseUomId()) {
                        orderReportBO.setCQty(qty);

                    } else if (uomCount == productBo.getOuUomid()) {
                        orderReportBO.setOuterOrderedCaseQty(qty);
                    }
                    orderReportBO.setTot(orderReportBO.getTot() + totalValue);
                    distReportList.remove(size - 1);
                    distReportList.add(size - 1, orderReportBO);


                } else {
                    orderReportBO = new DistOrderReportBo();
                    orderReportBO.setProductName(pname);
                    orderReportBO.setProductShortName(pSname);
                    orderReportBO.setProductId(pid);
                    orderReportBO.setBatchId(batchid);
                    orderReportBO.setBatchNo(batchnum);
                    if (uomid == productBo.getPcUomid()) {
                        orderReportBO.setPQty(qty);

                    } else if (uomid == productBo.getCaseUomId()) {
                        orderReportBO.setCQty(qty);

                    } else if (uomCount == productBo.getOuUomid()) {
                        orderReportBO.setOuterOrderedCaseQty(qty);
                    }
                    orderReportBO.setTot(totalValue);
                    distReportList.add(orderReportBO);


                }

            } else {
                orderReportBO = new DistOrderReportBo();
                orderReportBO.setProductName(pname);
                orderReportBO.setProductShortName(pSname);
                orderReportBO.setProductId(pid);
                orderReportBO.setBatchId(batchid);
                orderReportBO.setBatchNo(batchnum);
                if (uomid == productBo.getPcUomid()) {
                    orderReportBO.setPQty(qty);

                } else if (uomid == productBo.getCaseUomId()) {
                    orderReportBO.setCQty(qty);

                } else if (uomCount == productBo.getOuUomid()) {
                    orderReportBO.setOuterOrderedCaseQty(qty);
                }
                orderReportBO.setTot(totalValue);
                distReportList.add(orderReportBO);
            }


        }


    }

}
