package com.ivy.cpg.view.van.vanunload;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;

import com.ivy.cpg.view.salesreturn.SalesReturnReasonBO;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LoadManagementBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class VanUnLoadModuleHelper {
    private static VanUnLoadModuleHelper instance = null;

    private BusinessModel bmodel;
    private String transactionId;

    private VanUnLoadModuleHelper(Context context) {
        this.bmodel = (BusinessModel) context.getApplicationContext();

    }

    public static VanUnLoadModuleHelper getInstance(Context context) {
        if (instance == null) {
            instance = new VanUnLoadModuleHelper(context);
        }
        return instance;
    }

    public void saveVanUnLoad(Vector<LoadManagementBO> mylist, Context context) {
        try {
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            LoadManagementBO vanunloadbo;
            String columns = "uid,pid,pname,batchid,batchno,sih,caseqty,pcsqty,outerqty,duomqty,douomqty,dUomId,dOuomid,date,type,isFree";
            String uid = bmodel.userMasterHelper.getUserMasterBO().getUserid()
                    + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);
            transactionId = uid;

            for (int i = 0; i < mylist.size(); i++) {
                vanunloadbo = mylist.get(i);
                if (vanunloadbo.getCaseqty() > 0
                        || vanunloadbo.getPieceqty() > 0
                        || vanunloadbo.getOuterQty() > 0) {
                    String values = StringUtils.getStringQueryParam(uid)
                            + ","
                            + vanunloadbo.getProductid()
                            + ","
                            + DatabaseUtils.sqlEscapeString(vanunloadbo
                            .getProductname())
                            + ","
                            + vanunloadbo.getBatchId()
                            + ","
                            + StringUtils.getStringQueryParam(vanunloadbo.getBatchNo())
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
                            + StringUtils.getStringQueryParam(bmodel.userMasterHelper
                            .getUserMasterBO().getDownloadDate()) + ","
                            + 1 + "," + vanunloadbo.getIsFree();
                    db.insertSQL(DataMembers.tbl_vanunload_details, columns,
                            values);
                }
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }


    public String getTransactionId() {
        return transactionId;
    }

    //Vector<LoadManagementBO> mNonSalableSIHList = null;

    /**
     * Method used to unload non salable qty
     */
    public void saveVanUnloadNonsalable(Vector<LoadManagementBO> mylist, Context context) {

        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        try {

            db.createDataBase();
            db.openDataBase();
            db.executeQ("delete from VanUnloadDetails where upload = 'Y' ");

            LoadManagementBO vanunloadbo;
            String columns = "uid,pid,pname,batchid,batchno,sih,caseqty,pcsqty,outerqty,duomqty,douomqty,dUomId,dOuomid,date,type,typeid";
            String uid = getTransactionId();
            for (int i = 0; i < mylist.size(); i++) {
                vanunloadbo = mylist.get(i);

                if (vanunloadbo.getNonSalableQty() > 0 && vanunloadbo.getSalesReturnReasonList() != null
                        && vanunloadbo.getSalesReturnReasonList().size() > 0) {

                    for (SalesReturnReasonBO bo : vanunloadbo.getSalesReturnReasonList()) {
                        if (bo.getPieceQty() > 0) {
                            String values = bmodel.QT(uid)
                                    + ","
                                    + vanunloadbo.getProductid()
                                    + ","
                                    + DatabaseUtils.sqlEscapeString(vanunloadbo
                                    .getProductname())
                                    + ","
                                    + vanunloadbo.getBatchId()
                                    + ","
                                    + bmodel.QT(vanunloadbo.getBatchNo())
                                    + ","
                                    + vanunloadbo.getStocksih()
                                    + ",0"
                                    + ","
                                    + bo.getPieceQty()
                                    + ",0"
                                    + ","
                                    + vanunloadbo.getCaseSize()
                                    + ","
                                    + vanunloadbo.getOuterSize()
                                    + ","
                                    + vanunloadbo.getdUomid()
                                    + ","
                                    + vanunloadbo.getdOuonid()
                                    + ","
                                    + bmodel.QT(bmodel.userMasterHelper
                                    .getUserMasterBO().getDownloadDate()) + ","
                                    + 0 + ","
                                    + bo.getReasonID();
                            db.insertSQL(DataMembers.tbl_vanunload_details, columns,
                                    values);
                        }
                    }
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.closeDB();
        }

    }

    /**
     * Method used to upadate nonsalable sih in NonSalableSIHMaster table.
     */
    public void UpdateNonSalableSIH(Vector<LoadManagementBO> mylist, Context context) {

        LoadManagementBO product;
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        db.createDataBase();
        db.openDataBase();

        for (int i = 0; i < mylist.size(); i++) {
            product = mylist.get(i);

            if (product.getNonSalableQty() > 0 && product.getSalesReturnReasonList() != null && product.getSalesReturnReasonList().size() > 0) {

                for (SalesReturnReasonBO bo : product.getSalesReturnReasonList()) {
                    if (bo.getPieceQty() > 0) {

                        int nonSalablesih = bo.getPieceQty();

                        String sql = "update NonSalableSIHMaster set upload='N',qty=qty-" + nonSalablesih
                                + " where pid=" + product.getProductid()
                                + " and reasonid = " + bo.getReasonID();

                        db.executeQ(sql);
                    }
                }
            }

        }

        db.closeDB();
    }


    public void saveVanStockAdjustment(Vector<LoadManagementBO> mylist, Context context) {
        try {
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME
            );
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


    public void UpdateSIH(Vector<LoadManagementBO> vanunloadlist, Context context) {
        LoadManagementBO product;
        DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME
        );
        db.createDataBase();
        db.openDataBase();

        for (int i = 0; i < vanunloadlist.size(); i++) {
            product = vanunloadlist.get(i);
            if (product.getCaseqty() > 0 || product.getOuterQty() > 0
                    || product.getPieceqty() > 0) {
                int sih = (product.getOuterQty() * product.getOuterSize())
                        + (product.getPieceqty())
                        + (product.getCaseqty() * product.getCaseSize());
                if (product.getIsFree() != 1) {
                    String sql = "update StockInHandMaster set upload='N',qty=qty-" + sih
                            + " where pid=" + product.getProductid()
                            + " and batchid=" + product.getBatchId();

                    db.executeQ(sql);
                    String sql1 = "update ProductMaster set sih=sih- " + sih
                            + " where PID = " + product.getProductid();
                    db.executeQ(sql1);
                    String sql2 = "update ExcessStockInHand set qty=qty- " + sih
                            + ",Upload='N' where PID = " + product.getProductid();
                    db.executeQ(sql2);
                } else {
                    String sql = "update FreeStockInHandMaster set upload='N',qty=qty-" + sih
                            + " where pid=" + product.getProductid()
                            + " and batchid=" + product.getBatchId();

                    db.executeQ(sql);
                }

            }

        }

        db.closeDB();
    }

    public void updateEmptyReconilationTable(Vector<LoadManagementBO> vanunloadlist, Context context) {
        DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME);
        try {
            LoadManagementBO product;
            db.openDataBase();
            Cursor cursor;

            for (int i = 0; i < vanunloadlist.size(); i++) {
                product = vanunloadlist.get(i);
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
                db.deleteSQL("EmptyReconciliationHeader", null, true);
            }

            cursor.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
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
                + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);

        try {
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            Cursor c = db
                    .selectSQL("select sih.pid,p.PName,sih.batchid,bt.batchNum,sih.qty,p.dUomQty,p.dOuomQty,p.dUomId,p.dOuomid,p.piece_uomid from StockInHandMaster sih left join BatchMaster bt on bt.batchid=sih.batchid and sih.pid=bt.pid left join productmaster p on p.PID=sih.pid"
                            + " where P.IsSalable=1");

            if (c != null) {
                while (c.moveToNext()) {
                    String values = StringUtils.getStringQueryParam(uid)
                            + ","
                            + c.getInt(0)
                            + ","
                            + DatabaseUtils.sqlEscapeString(c.getString(1))
                            + ","
                            + c.getInt(2)
                            + ","
                            + StringUtils.getStringQueryParam(c.getString(3))
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
                            + StringUtils.getStringQueryParam(bmodel.userMasterHelper
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


    /**
     * Automatically Unload the products while day close if STKPRO20
     * Configuration enable, Update Product Mater and StockIn Hand Master
     */
    public void vanUnloadNonSalableAutomatically(Context context) {
        String columns = "uid,pid,pname,sih,caseqty,pcsqty,outerqty,duomqty,douomqty,dUomId,dOuomid,date,type,typeid";
        String uid = bmodel.userMasterHelper.getUserMasterBO().getUserid()
                + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);

        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            Cursor c = db.selectSQL("select nsih.pid,p.PName,nsih.qty,p.dUomQty,p.dOuomQty,p.dUomId,p.dOuomid,p.piece_uomid,nsih.reasonid from " +
                    "NonSalableSIHMaster nsih left join productmaster p on p.PID=nsih.pid");

            if (c != null) {
                while (c.moveToNext()) {
                    String values = bmodel.QT(uid)
                            + ","
                            + c.getInt(0)
                            + ","
                            + DatabaseUtils.sqlEscapeString(c.getString(1))
                            + ","
                            + c.getInt(2)
                            + ","
                            + 0
                            + ","
                            + c.getInt(2)
                            + ","
                            + 0
                            + ","
                            + c.getInt(3)
                            + ","
                            + c.getInt(4)
                            + ","
                            + c.getInt(5)
                            + ","
                            + c.getInt(6)
                            + ","
                            + bmodel.QT(bmodel.userMasterHelper
                            .getUserMasterBO().getDownloadDate()) + ","
                            + 0 + ","
                            + c.getInt(8);

                    db.insertSQL(DataMembers.tbl_vanunload_details, columns,
                            values);

                    String sql = "update NonSalableSIHMaster set upload='N',qty= qty-"
                            + c.getInt(2) + " where pid=" + c.getInt(0)
                            + " and reasonid =" + c.getInt(8);

                    db.executeQ(sql);
                }

                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }

    }

    /**
     * Automatically Unload the free products while day close if STKPRO20
     * Configuration enable
     */
    public void vanUnloadFreeSiHAutomatically(Context context) {
        String columns = "uid,pid,pname,batchid,batchno,sih,caseqty,pcsqty,outerqty,duomqty,douomqty,dUomId,dOuomid,date,type,isFree";
        String uid = bmodel.userMasterHelper.getUserMasterBO().getUserid()
                + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);

        try {
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            Cursor c = db
                    .selectSQL("select sih.pid,p.PName,sih.batchid,bt.batchNum,sih.qty,p.dUomQty,p.dOuomQty,p.dUomId,p.dOuomid,p.piece_uomid from FreeStockInHandMaster sih left join BatchMaster bt on bt.batchid=sih.batchid and sih.pid=bt.pid left join productmaster p on p.PID=sih.pid");

            if (c != null) {
                while (c.moveToNext()) {
                    String values = StringUtils.getStringQueryParam(uid)
                            + ","
                            + c.getInt(0)
                            + ","
                            + DatabaseUtils.sqlEscapeString(c.getString(1))
                            + ","
                            + c.getInt(2)
                            + ","
                            + StringUtils.getStringQueryParam(c.getString(3))
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
                            + StringUtils.getStringQueryParam(bmodel.userMasterHelper
                            .getUserMasterBO().getDownloadDate()) + ","
                            + 0 + "," + 1;

                    db.insertSQL(DataMembers.tbl_vanunload_details, columns,
                            values);

                    String sql = "update FreeStockInHandMaster set upload='N',qty= qty-"
                            + c.getInt(4) + " where pid=" + c.getInt(0)
                            + " and batchid=" + c.getInt(2);

                    db.executeQ(sql);

                }

                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }

    }

    /**
     * Method used to get non salable return products.
     *
     * @param mylist
     * @return
     */
    public ArrayList<SalesReturnReasonBO> setNonSalableReturnProduct(Vector<LoadManagementBO> mylist, Context context) {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );

        ArrayList<SalesReturnReasonBO> salesReturnList = null;
        LoadManagementBO bo;
        try {

            db.createDataBase();
            db.openDataBase();

            if (mylist != null) {
                for (int i = 0; i < mylist.size(); i++) {
                    bo = mylist.get(i);

                    String query = "select nsih.pid,nsih.qty,nsih.reasonid,c.listname from NonSalableSIHMaster nsih " +
                            "left join standardlistmaster c on nsih.reasonid = c.listid where nsih.pid = " + bo.getProductid();

                    Cursor c = db.selectSQL(query);
                    if (c != null) {
                        salesReturnList = new ArrayList<>();

                        SalesReturnReasonBO salesReturnReasonBO;

                        while (c.moveToNext()) {
                            salesReturnReasonBO = new SalesReturnReasonBO();
                            salesReturnReasonBO.setProductId("" + c.getInt(0));
                            salesReturnReasonBO.setPieceQty(c.getInt(1));
                            salesReturnReasonBO.setReasonID("" + c.getInt(2));
                            salesReturnReasonBO.setReasonDesc(c.getString(3));
                            salesReturnList.add(salesReturnReasonBO);

                        }
                    }
                    c.close();
                    bo.setSalesReturnReasonList(salesReturnList);

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.closeDB();
        }
        return salesReturnList;
    }


    public HashMap<String, ArrayList<LoadManagementBO>> getVanUnLoadListHashMap() {
        return vanUnLoadListHashMap;
    }

    private HashMap<String, ArrayList<LoadManagementBO>> vanUnLoadListHashMap;
    private ArrayList<String> reasonList;

    public HashMap<String, ArrayList<LoadManagementBO>> getVanUnloadDetailsForPrint(String uid, Context context) {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        vanUnLoadListHashMap = new HashMap<>();
        ArrayList<LoadManagementBO> vanUnLaodList = new ArrayList<>();
        reasonList = new ArrayList<>();
        LoadManagementBO bo;
        String reasonName = null;
        try {
            db.createDataBase();
            db.openDataBase();
            Cursor c = db.selectSQL("select " +
                    "VUD.pname,VUD.pcsqty,VUD.caseqty,VUD.outerqty,PM.baseprice," +
                    "IFNULL(SLM.ListName," + bmodel.QT(context.getString(R.string.salable)) + ")as ListName," +
                    "IFNULL(SLM.ListId,0)as ListID" +
                    ",PM.dUomQty,PM.dOuomQty,VUD.isFree" +
                    " from VanUnloadDetails VUD" +
                    " LEFT JOIN StandardListMaster SLM ON" +
                    " VUD.TypeID = SLM.ListId" +
                    " LEFT JOIN ProductMaster PM ON VUD.Pid=PM.PID" +
                    " WHERE Uid=" + bmodel.QT(uid) +
                    " ORDER BY VUD.TypeID ");
            if (c != null) {
                while (c.moveToNext()) {

                    if (reasonName != null
                            && !c.getString(5).equalsIgnoreCase(reasonName)) {
                        vanUnLaodList = new ArrayList<>();
                    }

                    reasonName = c.getString(5);
                    bo = new LoadManagementBO();

                    if (c.getInt(0) == 1)
                        bo.setProductname("(" + context.getResources().getString(R.string.free) + ")" + c.getString(0));
                    else
                        bo.setProductname(c.getString(0));

                    bo.setOrderedPcsQty(c.getInt(1));
                    bo.setOrderedCaseQty(c.getInt(2));
                    bo.setOuterOrderedCaseQty(c.getInt(3));
                    bo.setBaseprice(c.getDouble(4));
                    bo.setCaseSize(c.getInt(7));
                    bo.setOuterSize(c.getInt(8));

                    if (vanUnLoadListHashMap.get(reasonName) == null) {
                        vanUnLaodList.add(bo);
                        vanUnLoadListHashMap.put(reasonName, vanUnLaodList);
                        reasonList.add(reasonName);
                    } else {
                        vanUnLoadListHashMap.get(reasonName).add(bo);
                    }
                }
                c.close();
            }

            return vanUnLoadListHashMap;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null)
                db.closeDB();
        }
        return new HashMap<>();
    }


    public ArrayList<String> getReasonList() {
        return reasonList;
    }


    private ArrayList<LoadManagementBO> unloadHistoryList;

    public ArrayList<LoadManagementBO> getUnloadHistory(Context context) {
        DBUtil db = null;
        unloadHistoryList = new ArrayList<>();
        HashMap<String, LoadManagementBO> mUnloadHistorybyUid = new HashMap<>();
        LoadManagementBO unloadBo;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            Cursor c = db.selectSQL("select uid," +
                    "(pcsqty+(caseqty*duomQty)+(outerqty*dOuomQty)) as totalQty,typeid" +
                    " From VanUnloadDetails order by uid");
            if (c != null) {
                while (c.moveToNext()) {
                    if (mUnloadHistorybyUid.get(c.getString(0)) != null) {
                        LoadManagementBO loadManagementBO = mUnloadHistorybyUid.get(c.getString(0));
                        if (c.getInt(2) == 0)
                            loadManagementBO.setMaxQty(loadManagementBO.getMaxQty() + c.getInt(1));
                        else
                            loadManagementBO.setNonSalableQty(loadManagementBO.getNonSalableQty() + c.getInt(1));
                    } else {
                        unloadBo = new LoadManagementBO();
                        unloadBo.setTransactionId(c.getString(0));
                        if (c.getInt(2) == 0)
                            unloadBo.setMaxQty(c.getInt(1));
                        else
                            unloadBo.setNonSalableQty(c.getInt(1));

                        mUnloadHistorybyUid.put(unloadBo.getTransactionId(), unloadBo);
                        unloadHistoryList.add(unloadBo);
                    }
                }
                c.close();
            }
            return unloadHistoryList;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null)
                db.closeDB();
        }
        return new ArrayList<>();
    }

    public ArrayList<LoadManagementBO> getUnloadHistoryList() {
        return unloadHistoryList;
    }

}
