package com.ivy.cpg.view.van.vanstockapply;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class VanLoadStockApplyHelper {

    private Context context;
    private BusinessModel bmodel;
    private static VanLoadStockApplyHelper instance = null;
    private Vector<VanLoadStockApplyBO> stockReportMaster = null;
    private HashMap<String, ArrayList<String>> mBatchIDByProductID;

    protected VanLoadStockApplyHelper(Context context) {
        this.context = context;
        this.bmodel = (BusinessModel) context;
    }

    public static VanLoadStockApplyHelper getInstance(Context context) {
        if (instance == null) {
            instance = new VanLoadStockApplyHelper(context);
        }
        return instance;
    }

    /**
     * @return
     */
    public Vector<VanLoadStockApplyBO> downloadStockReportMaster() {
        DBUtil db = null;
        try {
            VanLoadStockApplyBO stock, stock1;
            db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            String query = "select A.pid,sum(A.caseQty),SUM(A.pcsQty),B.pname,B.psname,B.mrp,B.dUomQty,"
                    + " A.uid,A.outerQty,B.dOuomQty,A.BatchId,o.isstarted,C.batchId,C.batchNum, B.baseprice,"
                    + " A.Flag,IFNULL(A.LoadNo,A.uid),A.date,B.pCode,A.isFree from VanLoad A inner join productmaster B on A.pid=B.pid"
                    + " LEFT JOIN BatchMaster C on A.BatchId=C.batchid  AND C.Pid = B.pid"
                    + " left join Odameter o"
                    + " where B.IsSalable=1 OR B.IsSalable=0 GROUP BY A.uid,A.pid,C.batchid ORDER BY B.rowid";
            Cursor c = db.selectSQL(query);

            if (c != null) {
                stockReportMaster = new Vector<VanLoadStockApplyBO>();
                while (c.moveToNext()) {
                    stock = new VanLoadStockApplyBO();
                    stock.setProductId(c.getInt(0));
                    stock.setCaseQuantity(c.getInt(1));
                    stock.setPieceQuantity(c.getInt(2));
                    stock.setProductName(c.getString(3));
                    stock.setProductShortName(c.getString(4));
                    stock.setMrp(c.getFloat(5));
                    stock.setCaseSize(c.getInt(6));
                    stock.setUid(c.getString(7));
                    stock.setOuterQty(c.getInt(8));
                    stock.setOuterSize(c.getInt(9));
                    stock.setTotalQty((c.getInt(1) * c.getInt(6)) + c.getInt(2)
                            + (c.getInt(8) * c.getInt(9)));
                    stock.setBatchId(c.getInt(10));

                    stock.setBatchNumber(c.getString(13));
                    stock.setBasePrice(c.getFloat(14));
                    stock.setIsManualVanload(c.getInt(15));
                    stock.setLoadNO(c.getString(16));
                    stock.setDate(c.getString(17));
                    stock.setProductCode(c.getString(18));
                    stock.setIsFree(c.getInt(19));

                    int pcsQty = stock.getPieceQuantity();
                    int csQtyinPieces = stock.getCaseQuantity() * stock.getCaseSize();
                    int ouQtyinPieces = stock.getOuterQty() * stock.getOuterSize();
                    int totalqty = pcsQty + csQtyinPieces + ouQtyinPieces;

                    int caseQty = 0;
                    if(bmodel.configurationMasterHelper.SHOW_VAN_STK_CS) {
                        caseQty = stock.getCaseSize() != 0 ? totalqty / stock.getCaseSize() : totalqty;
                    }
                    int QtyRemaining = totalqty - (caseQty * stock.getCaseSize());

                    int outerQty = 0;
                    if(bmodel.configurationMasterHelper.SHOW_VAN_STK_OU) {
                        outerQty = stock.getOuterSize() != 0 ? QtyRemaining / stock.getOuterSize() : QtyRemaining;
                    }
                    int pieceQty = QtyRemaining - (outerQty * stock.getOuterSize());

                    stock.setPieceQuantity(pieceQty);
                    stock.setCaseQuantity(stock.getCaseSize() != 0 ? caseQty : 0);
                    stock.setOuterQty(stock.getOuterSize() != 0 ? outerQty : 0);

                    stockReportMaster.add(stock);
                    if (c.getInt(11) == 1)
                        bmodel.startjourneyclicked = true;
                }
                c.close();
            }
            return stockReportMaster;
        } catch (Exception e) {
            Commons.printException(e);
        } finally {
            if (db != null)
                db.closeDB();
        }
        return new Vector<>();
    }

    public void setStockReportMaster(
            Vector<VanLoadStockApplyBO> stockReportMaster) {
        stockReportMaster = stockReportMaster;
    }

    public Vector<VanLoadStockApplyBO> getStockReportMaster() {

        return stockReportMaster;
    }


    /**
     * @param mylist
     * @param SIHApplyById
     * @param uid
     * @param flag         1- manuval vanload 0 - normal vanload
     *                     <p>
     *                     method to use update stock in productmaster, stockinhandmaster and freeStockInHandMaster,while stock apply
     */
    public void updateSIHMaster(Vector<VanLoadStockApplyBO> mylist,
                                Vector<String> SIHApplyById, String uid, int flag) {

        ArrayList<Integer> batchIDList = new ArrayList<Integer>();
        try {

            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            StringBuffer sb = new StringBuffer();
            sb.append("select distinct A.pid,A.caseQty,A.pcsQty,B.pname,B.psname,B.mrp,B.dUomQty,A.uid,A.outerQty,B.dOuomQty,");
            sb.append("A.BatchId,o.isstarted,A.isFree from VanLoad A inner join productmaster B on A.pid=B.pid  ");
            sb.append(" left join Odameter o where A.uid=" + StringUtils.getStringQueryParam(uid)
                    + " and A.upload='N'");

            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    int totalQty = c.getInt(1) * c.getInt(6) + c.getInt(2)
                            + c.getInt(8) * c.getInt(9);
                    if (c.getInt(12) != 1) {
                        if (isAlreadyStockAvailable(c.getString(0), c.getString(10), db)) {
                            String sql = "update StockInHandMaster set upload='N',qty=qty+"
                                    + totalQty + " where pid=" + c.getString(0)
                                    + " and batchid=" + StringUtils.getStringQueryParam(c.getString(10));
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
                    } else {
                        if (isAlreadyFreeStockAvailable(c.getString(0), c.getString(10), db)) {
                            String sql = "update FreeStockInHandMaster set upload='N',qty=qty+"
                                    + totalQty + " where pid=" + c.getString(0)
                                    + " and batchid=" + StringUtils.getStringQueryParam(c.getString(10));
                            db.executeQ(sql);
                        } else {
                            String columns = "pid,batchid,qty";
                            String values = c.getString(0) + "," + c.getString(10)
                                    + "," + totalQty;
                            db.insertSQL("FreeStockInHandMaster", columns, values);

                        }
                    }

                    batchIDList.add(c.getInt(10));

                }
            }

            if (batchIDList.size() == 0) {
                for (VanLoadStockApplyBO stockReport : mylist) {
                    if (uid.equals(stockReport.getUid())) {
                        if (stockReport.getIsFree() != 1) {
                            if (!isAlreadyStockAvailable(stockReport.getProductId() + "", stockReport.getBatchId() + "", db)) {
                                String columns = "pid,qty,batchid";
                                String values = stockReport.getProductId() + ","
                                        + stockReport.getTotalQty() + ","
                                        + stockReport.getBatchId();

                                db.insertSQL("StockInHandMaster", columns, values);
                            } else {
                                String sql = "update StockInHandMaster set upload='N',qty=qty+"
                                        + stockReport.getTotalQty() + " where pid="
                                        + stockReport.getProductId() + " and batchid="
                                        + stockReport.getBatchId();
                                db.executeQ(sql);
                            }
                            //update product master

                            String sql = "update ProductMaster set sih= sih+"
                                    + stockReport.getTotalQty() + " where PID = "
                                    + stockReport.getProductId();
                            db.executeQ(sql);
                        } else {
                            if (!isAlreadyFreeStockAvailable(stockReport.getProductId() + "", stockReport.getBatchId() + "", db)) {
                                String columns = "pid,qty,batchid";
                                String values = stockReport.getProductId() + ","
                                        + stockReport.getTotalQty() + ","
                                        + stockReport.getBatchId();

                                db.insertSQL("FreeStockInHandMaster", columns, values);
                            } else {
                                String sql = "update FreeStockInHandMaster set upload='N',qty=qty+"
                                        + stockReport.getTotalQty() + " where pid="
                                        + stockReport.getProductId() + " and batchid="
                                        + stockReport.getBatchId();
                                db.executeQ(sql);
                            }
                        }

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
                    + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_TIME))
                    + ",'A'," + StringUtils.getStringQueryParam(upload) + ")";
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
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            db.updateSQL("update vanload set upload='N' where uid=" + StringUtils.getStringQueryParam(uid));
            db.close();
        } catch (Exception e) {

        }
    }

    public void downloadBatchwiseVanlod() {
        mBatchIDByProductID = new HashMap<String, ArrayList<String>>();
        DBUtil db = null;
        try {

            db = new DBUtil(context, DataMembers.DB_NAME
            );
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
            }
        } catch (Exception e) {
            Commons.printException(e);
        } finally {
            if (db != null)
                db.closeDB();
        }

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


    private boolean isAlreadyFreeStockAvailable(String productid, String batchid, DBUtil db) {
        String query = "select count(pid) from FreeStockInHandMaster where pid=" + productid
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
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            Cursor c = db.selectSQL("select count(distinct pid) from VanLoad" +
                    " where uid =" + StringUtils.getStringQueryParam(uid));

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


    /**
     * @param uid
     * @param flag 1 - reject manual vanload , 0 reject normal vanload
     *             Method to use reject vanload
     * @author rajesh.k
     */
    public void rejectVanload(String uid, int flag) {

        DBUtil db = null;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            String upload = "N";
            if (flag == 1) { // //if flag is 1 ,it is reject manuval vanload, This stock Apply records not needed to send server.
                //so upload is set Y
                upload = "Y";
            }
            String sql1 = "insert into StockApply(uid,date,status,upload) values("
                    + uid
                    + ","
                    + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_TIME))
                    + ",'C'," + StringUtils.getStringQueryParam(upload) + ")";
            db.executeQ(sql1);
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }
}
