package com.ivy.cpg.view.van.vanunload;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.LoadManagementBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.AppUtils;

import java.util.Vector;

public class VanUnLoadModuleHelper {
    private static VanUnLoadModuleHelper instance = null;

    private BusinessModel bmodel;

    private VanUnLoadModuleHelper(Context context) {
        this.bmodel = (BusinessModel) context.getApplicationContext();

    }

    public static VanUnLoadModuleHelper getInstance(Context context) {
        if (instance == null) {
            instance = new VanUnLoadModuleHelper(context);
        }
        return instance;
    }

    public void saveVanUnLoad(Vector<LoadManagementBO> mylist,Context context) {
        try {
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            LoadManagementBO vanunloadbo;
            String columns = "uid,pid,pname,batchid,batchno,sih,caseqty,pcsqty,outerqty,duomqty,douomqty,dUomId,dOuomid,date,type";
            String uid = bmodel.userMasterHelper.getUserMasterBO().getUserid()
                    + SDUtil.now(SDUtil.DATE_TIME_ID);
            for (int i = 0; i < mylist.size(); i++) {
                vanunloadbo = mylist.get(i);
                if (vanunloadbo.getCaseqty() > 0
                        || vanunloadbo.getPieceqty() > 0
                        || vanunloadbo.getOuterQty() > 0) {
                    String values = AppUtils.QT(uid)
                            + ","
                            + vanunloadbo.getProductid()
                            + ","
                            + DatabaseUtils.sqlEscapeString(vanunloadbo
                            .getProductname())
                            + ","
                            + vanunloadbo.getBatchId()
                            + ","
                            + AppUtils.QT(vanunloadbo.getBatchNo())
                            + ","
                            + vanunloadbo.getStocksih()
                            + ","
                            + vanunloadbo.getCaseqty()
                            + ","
                            + vanunloadbo.getPieceqty()
                            + ","
                            + vanunloadbo.getOuterQty()
                            + ","
                            + vanunloadbo.getCaseSize()
                            + ","
                            + vanunloadbo.getOuterSize()
                            + ","
                            + vanunloadbo.getdUomid()
                            + ","
                            + vanunloadbo.getdOuonid()
                            + ","
                            + AppUtils.QT(bmodel.userMasterHelper
                            .getUserMasterBO().getDownloadDate()) + ","
                            + 1;
                    db.insertSQL(DataMembers.tbl_vanunload_details, columns,
                            values);
                }
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public void saveVanStockAdjustment(Vector<LoadManagementBO> mylist,Context context) {
        try {
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            LoadManagementBO vanunloadbo;

            for (int i = 0; i < mylist.size(); i++) {
                vanunloadbo = mylist.get(i);
                if (vanunloadbo.getAdjusted_sih() > 0) {
                    int oldDiff = vanunloadbo.getOld_diff_sih();
                    int newDiff = oldDiff + vanunloadbo.getDiff_sih();
                    vanunloadbo.setOld_diff_sih(newDiff);

                    String query = "UPDATE StockInHandMaster SET upload='N', ";
                    query = query + "qty=" + vanunloadbo.getAdjusted_sih()
                            + ",";
                    query = query + "adjusted_qty=" + newDiff;
                    query = query + " WHERE pid=" + vanunloadbo.getProductid()
                            + " ";
                    query = query + "AND batchid=" + vanunloadbo.getBatchId();
                    db.updateSQL(query);

                    int targetSih = (getProductSih(db,
                            vanunloadbo.getProductid()))
                            + vanunloadbo.getDiff_sih();
                    query = "UPDATE ProductMaster SET sih=" +
                            targetSih +
                            " WHERE PID=" +
                            vanunloadbo.getProductid();
                    db.updateSQL(query);
                }
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    private int getProductSih(DBUtil db, int product_id) {
        int pSih = 0;

        String sql = "SELECT sih FROM ProductMaster WHERE PID=" + product_id;
        Cursor cur = db.selectSQL(sql);
        if (cur != null) {
            if (cur.getCount() > 0) {
                cur.moveToNext();
                pSih = cur.getInt(0);
            }
        }
        return pSih;

    }


    public void UpdateSIH(Vector<LoadManagementBO> vanunloadlist,Context context) {
        LoadManagementBO product;
        DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.createDataBase();
        db.openDataBase();

        for (int i = 0; i < vanunloadlist.size(); i++) {
            product = vanunloadlist.get(i);
            if (product.getCaseqty() > 0 || product.getOuterQty() > 0
                    || product.getPieceqty() > 0) {
                int sih = (product.getOuterQty() * product.getOuterSize())
                        + (product.getPieceqty())
                        + (product.getCaseqty() * product.getCaseSize());
                String sql = "update StockInHandMaster set upload='N',qty=qty-" + sih
                        + " where pid=" + product.getProductid()
                        + " and batchid=" + product.getBatchId();

                db.executeQ(sql);
                String sql1 = "update ProductMaster set sih=sih- " + sih
                        + " where PID = " + product.getProductid();
                db.executeQ(sql1);
            }

        }

        db.closeDB();
    }

    public void updateEmptyReconilationTable(Vector<LoadManagementBO> vanunloadlist,Context context) {
        DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME, DataMembers.DB_PATH);
        try {
            LoadManagementBO product;
            db.openDataBase();
            Cursor cursor;

            for (int i = 0; i < vanunloadlist.size(); i++) {
                product=vanunloadlist.get(i);
                if (product.getCaseqty() > 0 || product.getOuterQty() > 0
                        || product.getPieceqty() > 0) {
                    int sih = (product.getOuterQty() * product.getOuterSize())
                            + (product.getPieceqty())
                            + (product.getCaseqty() * product.getCaseSize());
                    cursor = db
                            .selectSQL("SELECT Pid,Qty FROM EmptyReconciliationDetail where pid=" + product.getProductid() + " ORDER BY Pid");
                    if (cursor.getCount() > 0) {
                        while (cursor.moveToNext()) {


                            if (cursor.getInt(1)
                                    - sih == 0) {
                                db.deleteSQL("EmptyReconciliationDetail", "Pid ="
                                        + cursor.getInt(0), false);
                            } else {
                                db.updateSQL("UPDATE EmptyReconciliationDetail"
                                        + " SET Qty = Qty - "
                                        + sih
                                        + " WHERE Pid = "
                                        + product.getProductid());
                            }
                            break;
                        }
                    }

                }

            }


            cursor = db
                    .selectSQL("SELECT Pid FROM EmptyReconciliationDetail");
            if (cursor.getCount() == 0) {
                db.deleteSQL("EmptyReconciliationHeader",null,true);
            }

            cursor.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(""+e);
            db.closeDB();
        }
    }

    public boolean hasVanunload() {
        for (LoadManagementBO vanunloadBO : bmodel.productHelper.getLoadMgmtProducts()) {
            if (vanunloadBO.getCaseqty() > 0 || vanunloadBO.getPieceqty() > 0
                    || vanunloadBO.getOuterQty() > 0) {
                return true;
            }
        }

        return false;
    }

    /**
     * Automatically Unload the products while day close if STKPRO20
     * Configuration enable, Update Product Mater and StockIn Hand Master
     */
    public void vanUnloadAutomatically(Context context) {
        String columns = "uid,pid,pname,batchid,batchno,sih,caseqty,pcsqty,outerqty,duomqty,douomqty,dUomId,dOuomid,date,type";
        String uid = bmodel.userMasterHelper.getUserMasterBO().getUserid()
                + SDUtil.now(SDUtil.DATE_TIME_ID);

        try {
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            Cursor c = db
                    .selectSQL("select sih.pid,p.PName,sih.batchid,bt.batchNum,sih.qty,p.dUomQty,p.dOuomQty,p.dUomId,p.dOuomid,p.piece_uomid from StockInHandMaster sih left join BatchMaster bt on bt.batchid=sih.batchid and sih.pid=bt.pid left join productmaster p on p.PID=sih.pid"
                            + " where P.IsSalable=1");

            if (c != null) {
                while (c.moveToNext()) {
                    String values = AppUtils.QT(uid)
                            + ","
                            + c.getInt(0)
                            + ","
                            + DatabaseUtils.sqlEscapeString(c.getString(1))
                            + ","
                            + c.getInt(2)
                            + ","
                            + AppUtils.QT(c.getString(3))
                            + ","
                            + c.getInt(4)
                            + ","
                            + 0
                            + ","
                            + c.getInt(4)
                            + ","
                            + 0
                            + ","
                            + c.getInt(5)
                            + ","
                            + c.getInt(6)
                            + ","
                            + c.getInt(7)
                            + ","
                            + c.getInt(8)
                            + ","
                            + AppUtils.QT(bmodel.userMasterHelper
                            .getUserMasterBO().getDownloadDate()) + ","
                            + 1;

                    db.insertSQL(DataMembers.tbl_vanunload_details, columns,
                            values);

                    String sql = "update StockInHandMaster set upload='N',qty= qty-"
                            + c.getInt(4) + " where pid=" + c.getInt(0)
                            + " and batchid=" + c.getInt(2);

                    db.executeQ(sql);
                    String sql1 = "update ProductMaster set sih= sih- "
                            + c.getInt(4) + " where PID = " + c.getInt(0);
                    db.executeQ(sql1);
                }

                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }

    }

}
