package com.ivy.sd.png.provider;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.StockReportMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class StockReportMasterHelper {

    private Context context;
    private BusinessModel bmodel;
    private static StockReportMasterHelper instance = null;
    private Vector<StockReportMasterBO> StockReportMaster = null;
    private Vector<StockReportMasterBO> StockReportMasterAll = null;
    private Vector<StockReportMasterBO> BeginingStockReport;
    private HashMap<String, ArrayList<String>> mBatchIDByProductID;

    protected StockReportMasterHelper(Context context) {
        this.context = context;
        this.bmodel = (BusinessModel) context;
    }

    public static StockReportMasterHelper getInstance(Context context) {
        if (instance == null) {
            instance = new StockReportMasterHelper(context);
        }
        return instance;
    }

    /**
     * @return
     */
    public Vector<StockReportMasterBO> downloadStockReportMaster() {
        try {
            StockReportMasterBO stock, stock1;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            String query = "select A.pid,sum(A.caseQty),SUM(A.pcsQty),B.pname,B.psname,B.mrp,B.dUomQty,"
                    + " A.uid,A.outerQty,B.dOuomQty,A.BatchId,o.isstarted,C.batchId,C.batchNum, B.baseprice,"
                    + " A.Flag,IFNULL(A.LoadNo,A.uid),A.date from VanLoad A inner join productmaster B on A.pid=B.pid"
                    + " LEFT JOIN BatchMaster C on A.BatchId=C.batchid  AND C.Pid = B.pid"
                    + " left join Odameter o"
                    + " where B.IsSalable=1 OR B.IsSalable=0 GROUP BY A.uid,A.pid,C.batchid ORDER BY B.rowid";
            Cursor c = db.selectSQL(query);

            if (c != null) {
                StockReportMaster = new Vector<StockReportMasterBO>();
                while (c.moveToNext()) {
                    stock = new StockReportMasterBO();
                    stock.setProductid(c.getInt(0));
                    stock.setCaseqty(c.getInt(1));
                    stock.setPieceqty(c.getInt(2));
                    stock.setProductname(c.getString(3));
                    stock.setProductshortname(c.getString(4));
                    stock.setMrp(c.getFloat(5));
                    stock.setCasesize(c.getInt(6));
                    stock.setUid(c.getString(7));
                    stock.setOuterQty(c.getInt(8));
                    stock.setOuterSize(c.getInt(9));
                    stock.setTotalQty((c.getInt(1) * c.getInt(6)) + c.getInt(2)
                            + (c.getInt(8) * c.getInt(9)));
                    stock.setBatchId(c.getInt(10));

                    stock.setBatch_number(c.getString(13));
                    stock.setBasePrice(c.getFloat(14));
                    stock.setIsManuvalVanload(c.getInt(15));
                    stock.setLoadNO(c.getString(16));
                    stock.setDate(c.getString(17));
                    StockReportMaster.add(stock);
                    if (c.getInt(11) == 1)
                        bmodel.startjourneyclicked = true;
                }
                c.close();
            }
            Cursor c1 = db
                    .selectSQL("select A.pid,sum(A.caseQty),sum(A.pcsQty),B.pname,B.psname,B.mrp,B.dUomQty,A.uid,sum(A.outerQty),B.dOuomQty,A.BatchId,C.batchNum, B.baseprice,A.Flag,IFNULL(A.LoadNo,A.uid),A.date from VanLoad A inner join productmaster B on A.pid=B.pid LEFT JOIN BatchMaster C on A.BatchId=C.batchid and A.pid=C.pid  group by A.pid,C.batchid ORDER BY B.rowid");
            if (c1 != null) {
                StockReportMasterAll = new Vector<StockReportMasterBO>();
                while (c1.moveToNext()) {
                    stock1 = new StockReportMasterBO();
                    stock1.setProductid(c1.getInt(0));
                    stock1.setCaseqty(c1.getInt(1));
                    stock1.setPieceqty(c1.getInt(2));
                    stock1.setProductname(c1.getString(3));
                    stock1.setProductshortname(c1.getString(4));
                    stock1.setMrp(c1.getFloat(5));
                    stock1.setCasesize(c1.getInt(6));
                    stock1.setUid(c1.getString(7));
                    stock1.setOuterQty(c1.getInt(8));
                    stock1.setOuterSize(c1.getInt(9));
                    stock1.setTotalQty((c1.getInt(1) * c1.getInt(6))
                            + c1.getInt(2) + (c1.getInt(8) * c1.getInt(9)));
                    stock1.setBatchId(c1.getInt(10));
                    stock1.setBatch_number(c1.getString(11));
                    stock1.setBasePrice(c1.getFloat(12));
                    stock1.setIsManuvalVanload(c1.getInt(13));
                    stock1.setLoadNO(c1.getString(14));
                    stock1.setDate(c1.getString(15));
                    StockReportMasterAll.add(stock1);
                }
                c1.close();
            }
            db.closeDB();
        } catch (Exception e) {

            Commons.printException(e);
        }
        return StockReportMaster;
    }

    public void setStockReportMaster(
            Vector<StockReportMasterBO> stockReportMaster) {
        StockReportMaster = stockReportMaster;
    }

    public Vector<StockReportMasterBO> getStockReportMaster() {

        return StockReportMaster;
    }

    public Vector<StockReportMasterBO> getStockReportMasterAll() {

        return StockReportMasterAll;
    }

    public void updateSIH(Vector<StockReportMasterBO> mylist,
                          Vector<String> SIHApplyById) {

        try {
            String uid;
            StockReportMasterBO product;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            int size = mylist.size();
            for (int i = 0; i < size; i++) {
                product = mylist.get(i);
                if (SIHApplyById.contains(mylist.get(i).getUid())) {
                    String sql = "update ProductMaster set sih=" +
                            +product.getTotalQty() + " where PID = "
                            + product.getProductid();
                    db.executeQ(sql);
                }
            }
            for (int i = 0; i < SIHApplyById.size(); i++) {
                uid = SIHApplyById.get(i);
                String sql1 = "insert into StockApply(uid,date) values("
                        + uid
                        + ","
                        + bmodel.QT(SDUtil.now(SDUtil.DATE_TIME)) + ")";
                db.executeQ(sql1);
            }
            db.closeDB();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Commons.printException(e);
        }
    }

    /**
     * @param mylist
     * @param SIHApplyById
     * @param uid
     * @param flag         1- manuval vanload 0 - normal vanload
     *                     <p>
     *                     method to use update stock in productmaster and stockinhandmaster,while stock apply
     */
    public void updateSIHMaster(Vector<StockReportMasterBO> mylist,
                                Vector<String> SIHApplyById, String uid, int flag) {

        ArrayList<Integer> batchIDList = new ArrayList<Integer>();
        try {

            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            StringBuffer sb = new StringBuffer();
            sb.append("select distinct A.pid,A.caseQty,A.pcsQty,B.pname,B.psname,B.mrp,B.dUomQty,A.uid,A.outerQty,B.dOuomQty,");
            sb.append("A.BatchId,o.isstarted from VanLoad A inner join productmaster B on A.pid=B.pid  ");
            sb.append(" left join Odameter o where A.uid=" + bmodel.QT(uid)
                    + " and A.upload='N'");

            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    int totalQty = c.getInt(1) * c.getInt(6) + c.getInt(2)
                            + c.getInt(8) * c.getInt(9);

                    if (isAlreadyStockAvailable(c.getString(0), c.getString(10), db)) {
                        String sql = "update StockInHandMaster set upload='N',qty=qty+"
                                + totalQty + " where pid=" + c.getString(0)
                                + " and batchid=" + bmodel.QT(c.getString(10));
                        db.executeQ(sql);
                    } else {
                        String columns = "pid,batchid,qty";
                        String values = c.getString(0) + "," + c.getString(10)
                                + "," + totalQty;
                        db.insertSQL("StockInHandMaster", columns, values);

                    }
                    // update product master
                    String sql = "update ProductMaster set sih= sih+"
                            + totalQty + " where PID = " + c.getString(0);
                    db.executeQ(sql);

                    batchIDList.add(c.getInt(10));

                }
            }

            if (batchIDList.size() == 0) {
                for (StockReportMasterBO stockReport : mylist) {
                    if (uid.equals(stockReport.getUid())) {
                        if (!isAlreadyStockAvailable(stockReport.getProductid() + "", stockReport.getBatchId() + "", db)) {
                            String columns = "pid,qty,batchid";
                            String values = stockReport.getProductid() + ","
                                    + stockReport.getTotalQty() + ","
                                    + stockReport.getBatchId();

                            db.insertSQL("StockInHandMaster", columns, values);
                        } else {
                            String sql = "update StockInHandMaster set upload='N',qty=qty+"
                                    + stockReport.getTotalQty() + " where pid="
                                    + stockReport.getProductid() + " and batchid="
                                    + stockReport.getBatchId();
                            db.executeQ(sql);
                        }
                        //update product master

                        String sql = "update ProductMaster set sih= sih+"
                                + stockReport.getTotalQty() + " where PID = "
                                + stockReport.getProductid();
                        db.executeQ(sql);

                    }
                }
            }
            // insert stock apply
            String upload = "N";
            //if flag is 1 ,it is manuval vanload, This stock Apply records not needed to send server.
            //so upload is set Y
            if (flag == 1) {
                upload = "Y";
            }
            String sql1 = "insert into StockApply(uid,date,status,upload) values("
                    + uid
                    + ","
                    + bmodel.QT(SDUtil.now(SDUtil.DATE_TIME))
                    + ",'A'," + bmodel.QT(upload) + ")";
            db.executeQ(sql1);

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    /**
     * @param uid - vanload uid
     *            Method to update upload=N in vaload table,while manuval vanload stock apply
     * @author rajesh.k
     */
    public void updateVanload(String uid) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            db.updateSQL("update vanload set upload='N' where uid=" + bmodel.QT(uid));
            db.close();
        } catch (Exception e) {

        }
    }

    public void downloadBatchwiseVanlod() {
        mBatchIDByProductID = new HashMap<String, ArrayList<String>>();
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.openDataBase();
        Cursor c = db
                .selectSQL("select A.pid,S.batchid from productmaster A inner join Stockinhandmaster S on A.pid=s.pid");
        if (c.getCount() > 0) {
            String productid = "";
            ArrayList<String> batchIDList = new ArrayList<String>();
            while (c.moveToNext()) {
                if (!productid.equals(c.getString(0))) {
                    if (!productid.equals("")) {
                        mBatchIDByProductID.put(productid, batchIDList);
                        batchIDList = new ArrayList<String>();
                        batchIDList.add(c.getString(1));
                        productid = c.getString(0);

                    } else {
                        batchIDList.add(c.getString(1));
                        productid = c.getString(0);
                    }
                } else {
                    batchIDList.add(c.getString(1));
                }
            }
            if (batchIDList.size() > 0) {
                mBatchIDByProductID.put(productid, batchIDList);
            }

            c.close();
            db.close();
        }

    }

    private boolean isAlreadySihAvailable(String productid, String batchid) {


        if (mBatchIDByProductID != null) {


            ArrayList<String> batchidList = mBatchIDByProductID.get(productid);
            if (batchidList == null) {
                return false;
            } else {
                for (String bid : batchidList) {
                    if (bid.equals(batchid)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean isAlreadyStockAvailable(String productid, String batchid, DBUtil db) {
        String query = "select count(pid) from Stockinhandmaster where pid=" + productid
                + " and batchid=" + batchid;
        Cursor c = db.selectSQL(query);
        if (c.getCount() > 0) {
            if (c.moveToNext()) {
                int count = c.getInt(0);
                if (count > 0) {
                    c.close();
                    return true;
                } else {
                    c.close();
                    return false;
                }
            }
        }
        c.close();
        return false;

    }

    public int getNoProductsCount(String uid) {
        int tot = 0;
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Cursor c = db.selectSQL("select count(distinct pid) from VanLoad" +
                    " where uid =" + bmodel.QT(uid));

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

    public Vector<StockReportMasterBO> downloadBeginingStockReport() {
        try {
            StockReportMasterBO stock;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            String query = "select DISTINCT A.pid,A.caseQty,A.pcsQty,B.pname,B.psname,B.mrp,B.dUomQty,A.uid,A.outerQty,B.dOuomQty,B.baseprice,IFNULL(A.LoadNo,A.uid) from VanLoad A inner join productmaster B on A.pid=B.pid ";
            Cursor c = db.selectSQL(query);

            if (c != null) {
                BeginingStockReport = new Vector<StockReportMasterBO>();
                while (c.moveToNext()) {
                    stock = new StockReportMasterBO();
                    stock.setProductid(c.getInt(0));
                    stock.setCaseqty(c.getInt(1));
                    stock.setPieceqty(c.getInt(2));
                    stock.setProductname(c.getString(3));
                    stock.setProductshortname(c.getString(4));
                    stock.setMrp(c.getFloat(5));
                    stock.setCasesize(c.getInt(6));
                    stock.setUid(c.getString(7));
                    stock.setOuterQty(c.getInt(8));
                    stock.setOuterSize(c.getInt(9));
                    stock.setTotalQty((c.getInt(1) * c.getInt(6)) + c.getInt(2)
                            + (c.getInt(8) * c.getInt(9)));

                    stock.setBasePrice(c.getFloat(10));
                    stock.setLoadNO(c.getString(11));
                    BeginingStockReport.add(stock);

                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {

            Commons.printException(e);
        }
        return BeginingStockReport;
    }

    /**
     * @param uid
     * @param flag 1 - reject manual vanload , 0 reject normal vanload
     *             Method to use reject vanload
     * @author rajesh.k
     */
    public void rejectVanload(String uid, int flag) {

        DBUtil db = null;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            String upload = "N";
            if (flag == 1) { // //if flag is 1 ,it is reject manuval vanload, This stock Apply records not needed to send server.
                //so upload is set Y
                upload = "Y";
            }
            String sql1 = "insert into StockApply(uid,date,status,upload) values("
                    + uid
                    + ","
                    + bmodel.QT(SDUtil.now(SDUtil.DATE_TIME))
                    + ",'C'," + bmodel.QT(upload) + ")";
            db.executeQ(sql1);
        } catch (Exception e) {
            db.close();
        }
    }
}
