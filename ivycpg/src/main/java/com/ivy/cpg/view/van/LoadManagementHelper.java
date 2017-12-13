package com.ivy.cpg.view.van;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.BomBO;
import com.ivy.sd.png.bo.BomMasterBO;
import com.ivy.sd.png.bo.BomRetunBo;
import com.ivy.sd.png.bo.LoadManagementBO;
import com.ivy.sd.png.bo.SubDepotBo;
import com.ivy.sd.png.bo.VanLoadMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.Vector;

public class LoadManagementHelper {

    private static LoadManagementHelper instance = null;
    private int mPaymentTypeId;
    private float mVanLoadAmount = 0;
    private Context context;
    private BusinessModel bmodel;
    private Vector<VanLoadMasterBO> vanmasterbo = null;
    private ArrayList<SubDepotBo> subDepotList = null;
    private ArrayList<SubDepotBo> distributorList = null;

    public LoadManagementHelper(Context context) {
        this.context = context;
        this.bmodel = (BusinessModel) context;

    }

    public static LoadManagementHelper getInstance(Context context) {
        if (instance == null) {
            instance = new LoadManagementHelper(context);
        }
        return instance;
    }


    /**
     * DownLoad the SubDepots from Distribution Master
     */
    public void downloadSubDepots() {
        SubDepotBo subDepots;
        DBUtil db = null;
        Cursor cursor;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();
            db.createDataBase();

            cursor = db.selectSQL("Select Did,Dname,IFNULL(CNumber,''),IFNULL(Address1,''),IFNULL(Address2,''),IFNULL(Address3,''), type from DistributorMaster");
            if (cursor != null) {
                subDepotList = new ArrayList<>();
                distributorList = new ArrayList<>();


                subDepots = new SubDepotBo();
                subDepots.setSubDepotId(0);
                subDepots.setdName("Select Distributor");
                subDepotList.add(subDepots);
                distributorList.add(subDepots);

                while (cursor.moveToNext()) {
                    subDepots = new SubDepotBo();

                    subDepots.setSubDepotId(cursor.getInt(0));

                    subDepots.setContactNumber(cursor.getString(2));
                    subDepots.setAddress1(cursor.getString(3));
                    subDepots.setAddress2(cursor.getString(4));
                    subDepots.setAddress3(cursor.getString(5));

                    String type = cursor.getString(6);
                    if (type != null && type.equalsIgnoreCase("distributor")) {
                        if (bmodel.userMasterHelper.getUserMasterBO().getDistributorid() == subDepots.getSubDepotId()) {
                            subDepots.setdName(cursor.getString(1)
                                    + "- Primary");
                        } else {
                            subDepots.setdName(cursor.getString(1)
                                    + "- Secondary");
                        }
                        distributorList.add(subDepots);

                    } else {
                        subDepots.setdName(cursor.getString(1));
                        subDepotList.add(subDepots);
                    }


                }

                cursor.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
            if (db != null) {
                db.closeDB();
            }
        }
    }

    public VanLoadMasterBO downloadOdameter() {
        VanLoadMasterBO temp = null;
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT o.uid,o.date,o.start,o.end,o.isended,o.isstarted,o.starttime,o.endtime FROM Odameter o");
            if (c != null) {

                while (c.moveToNext()) {
                    temp = new VanLoadMasterBO();
                    temp.setOdameteruid(c.getInt(0));
                    temp.setOdameterdate(c.getString(1));
                    temp.setOdameterstart(c.getDouble(2));
                    temp.setOdameterend(c.getDouble(3));
                    temp.setIsended(c.getInt(4));
                    temp.setIsstarted(c.getInt(5));
                    temp.setStartdatetime(c.getString(6));
                    temp.setEndtime(c.getString(7));
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return temp;
    }

    public boolean hasVanLoadDone() {

        int siz = bmodel.productHelper.getProducts().size();
        if (siz == 0)
            return false;
        for (int i = 0; i < siz; ++i) {
            LoadManagementBO product = bmodel.productHelper
                    .getProducts().get(i);
            if (product.getPieceqty() > 0 || product.getCaseqty() > 0
                    || product.getOuterQty() > 0)
                return true;
        }
        return false;
    }

    public void saveVanLoad(Vector<LoadManagementBO> mylist,
                            int selectedSubDepotID) {
        String batchid;
        String values;
        DBUtil db = null;
        LoadManagementBO product;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            int siz = mylist.size();
            String columns = "uid,pid,caseQty,pcsQty,qty,date,upload,outerQty,duomQty,duomid,dOuomQty,dOuomId,BatchId,batchno,SubDepotId,Flag";

            String uid = bmodel.userMasterHelper.getUserMasterBO().getUserid()
                    + SDUtil.now(SDUtil.DATE_TIME_ID);

            for (int i = 0; i < siz; i++) {

                product = mylist.get(i);

                int totalsih = product.getCaseqty() * product.getCaseSize();

                totalsih = totalsih + product.getPieceqty()
                        + (product.getOuterQty() * product.getOuterSize());

                if (product.getCaseqty() > 0 || product.getPieceqty() > 0
                        || product.getOuterQty() > 0) {
                    if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION) {
                        if (product.getBatchlist() != null && product.getBatchlist().size() > 0) {
                            for (LoadManagementBO bo : product.getBatchlist()) {
                                if (bo.getCaseqty() > 0 || bo.getPieceqty() > 0
                                        || bo.getOuterQty() > 0) {
                                    if (!bo.getBatchNo().equals("NA"))
                                        batchid = getbatchid(product.getProductid(),
                                                bo.getBatchNo());
                                    else {
                                        batchid = "0";
                                    }
                                    values = bmodel.QT(uid)
                                            + ","
                                            + product.getProductid()
                                            + ","
                                            + bo.getCaseqty()
                                            + ","
                                            + bo.getPieceqty()
                                            + ","
                                            + totalsih
                                            + ","
                                            + bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL))
                                            + ","
                                            + bmodel.QT("Y")
                                            + ","
                                            + bo.getOuterQty() + ","
                                            + product.getCaseSize() + ","
                                            + product.getdUomid() + ","
                                            + product.getOuterSize() + ","
                                            + product.getdOuonid() + ","
                                            + bmodel.QT(batchid) + ","
                                            + bmodel.QT(bo.getBatchNo()) + ","
                                            + selectedSubDepotID + "," + 1;

                                    db.insertSQL(DataMembers.tbl_vanload, columns,
                                            values);
                                }
                            }

                        }
                    } else {

                        batchid = 0 + "";
                        values = bmodel.QT(uid)
                                + ","
                                + product.getProductid()
                                + ","
                                + product.getCaseqty()
                                + ","
                                + product.getPieceqty()
                                + ","
                                + totalsih
                                + ","
                                + bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL))
                                + ","
                                + bmodel.QT("Y")
                                + ","
                                + product.getOuterQty() + ","
                                + product.getCaseSize() + ","
                                + product.getdUomid() + ","
                                + product.getOuterSize() + ","
                                + product.getdOuonid() + ","
                                + bmodel.QT(batchid) + ","
                                + 0 + ","
                                + selectedSubDepotID + "," + 1;

                        db.insertSQL(DataMembers.tbl_vanload, columns,
                                values);
                    }
                }

            }

            if (bmodel.configurationMasterHelper.SHOW_PRODUCTRETURN) {
                ArrayList<BomRetunBo> returnProducts;
                String returncolumns = "Uid,Date,Pid,LiableQty,pcsqty,Price, dUomId,TypeID,LineValue,SubDepotId,RefId";

                returnProducts = bmodel.productHelper.getBomReturnProducts();

                String tranId = bmodel.userMasterHelper.getUserMasterBO()
                        .getUserid() + SDUtil.now(SDUtil.DATE_TIME_ID);

                for (BomRetunBo bomReturnBo : returnProducts) {

                    if (bomReturnBo.getLiableQty() > 0
                            || bomReturnBo.getReturnQty() > 0) {

                        values = tranId
                                + ","
                                + bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL))
                                + ","
                                + bmodel.QT(bomReturnBo.getPid())
                                + ","
                                + bomReturnBo.getLiableQty()
                                + ","
                                + bomReturnBo.getReturnQty()
                                + ","
                                + bomReturnBo.getpSrp()
                                + ","
                                + bomReturnBo.getPieceUomId()
                                + ","
                                + bomReturnBo.getTypeId()
                                + ","
                                + ((bomReturnBo.getLiableQty() - bomReturnBo
                                .getReturnQty()) * bomReturnBo
                                .getpSrp()) + ","

                                + selectedSubDepotID + "," + uid;

                        db.insertSQL(DataMembers.tbl_vanunload_details,
                                returncolumns, values);
                    }

                }

                Cursor cursor = db
                        .selectSQL("Select ListId from StandardListMaster where ListCode ='CA' and ListType ='COLLECTION_PAY_TYPE'");
                if (cursor != null) {
                    if (cursor.moveToNext()) {
                        mPaymentTypeId = cursor.getInt(cursor
                                .getColumnIndex("ListId"));
                    }
                    cursor.close();
                }

                String subDepotColumns = "Uid,PaymentType,Amount,SubDepotId,RefId,Date";

                String tid = bmodel.userMasterHelper.getUserMasterBO()
                        .getUserid() + SDUtil.now(SDUtil.DATE_TIME_ID);

                values = tid + "," + mPaymentTypeId + ","
                        + SDUtil.format(getmVanLoadAmount(), 2, 0) + ","
                        + selectedSubDepotID + "," + uid + ","
                        + bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL));

                db.insertSQL(DataMembers.tbl_SubDepotSettlement,
                        subDepotColumns, values);

                bmodel.mEmptyReconciliationhelper
                        .updateEmptyReconilationTable();

            }

            db.closeDB();
        } catch (Exception e) {
            if (db != null) {
                db.closeDB();
            }
            Commons.printException(e);
        }
    }

    public String getbatchid(int pid, String batchno) {
        String batchid = null;
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT batchid from BatchMaster where batchNum="
                            + bmodel.QT(batchno) + " and pid=" + pid);
            if (c != null) {
                while (c.moveToNext()) {
                    batchid = c.getString(0);
                }

                c.close();
            }
            db.closeDB();
        } catch (SQLException e) {
            Commons.printException(e);
        }
        return batchid;

    }

    public void saveBatch(LoadManagementBO product) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            String values;
            db.createDataBase();
            db.openDataBase();
            int batchId = 1;
            Cursor c = db.selectSQL("SELECT max(batchid)+1 FROM BatchMaster");
            if (c != null) {

                while (c.moveToNext()) {
                    batchId = c.getInt(0);
                }
                c.close();
            }
            // create new VanloadMasterBO object and add batch list

            LoadManagementBO vanloadBO;
            for (LoadManagementBO batchListBO : product.getBatchlist()) {
                if (batchListBO.getBatchnolist() == null) {
                    batchListBO.setBatchnolist(new Vector<LoadManagementBO>());

                }
                vanloadBO = new LoadManagementBO();
                vanloadBO.setBatchId(batchId + "");
                vanloadBO.setBatchNo(product.getManualBatchNo());
                vanloadBO.setMfgDate(product.getMfgDate());
                vanloadBO.setExpDate(product.getExpDate());
                vanloadBO.setProductid(product.getProductid());
                batchListBO.getBatchnolist().add(vanloadBO);

            }

            String columns = "batchid,batchNum,pid,MfgDate,ExpDate,is_new";

            values = batchId + "," + bmodel.QT(product.getManualBatchNo())
                    + "," + product.getProductid() + ","
                    + bmodel.QT(product.getMfgDate()) + ","
                    + bmodel.QT(product.getExpDate()) + ", 1";

            db.insertSQL("BatchMaster", columns, values);
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    /**
     * set Return Qty to the ReturnableBomMasterBo and set
     */
    public void setReturnQty() {

        for (BomRetunBo bom : bmodel.productHelper.getBomReturnProducts()) {
            bom.setLiableQty(0);
        }

        for (LoadManagementBO sku : bmodel.productHelper.getProducts()) {
            for (BomMasterBO bomMasterBo : bmodel.productHelper.getBomMaster()) {

                int bomPid = SDUtil.convertToInt(bomMasterBo.getPid());
                if (sku.getProductid() == bomPid) {

                    for (BomBO bomBo : bomMasterBo.getBomBO()) {
                        if (bomBo.getUomID() == sku.getPiece_uomid()) {
                            bomBo.setTotalQty(bomBo.getQty()
                                    * sku.getPieceqty());
                        } else if (bomBo.getUomID() == sku.getdUomid()) {
                            bomBo.setTotalQty(bomBo.getQty() * sku.getCaseqty());
                        } else if (bomBo.getUomID() == sku.getdOuonid()) {
                            bomBo.setTotalQty(bomBo.getQty()
                                    * sku.getOuterQty());
                        }

                        for (BomRetunBo returnBo : bmodel.productHelper
                                .getBomReturnProducts()) {
                            if (bomBo.getbPid().equals(returnBo.getPid())) {
                                returnBo.setLiableQty(returnBo.getLiableQty()
                                        + bomBo.getTotalQty());
                                break;
                            }
                        }
                    }
                    break;
                }
            }
        }

    }

    /**
     * @return -calculateVanLoadProductPrice
     */
    public double calculateVanLoadProductPrice() {
        double totalPrice = 0;
        try {
            int siz = bmodel.productHelper.getProducts().size();
            if (siz == 0)
                return 0;
            for (int i = 0; i < siz; ++i) {
                LoadManagementBO product = bmodel.productHelper.getProducts().get(i);
                if (product.getPieceqty() > 0 || product.getCaseqty() > 0
                        || product.getOuterQty() > 0)
                    totalPrice = totalPrice
                            + ((product.getPieceqty()
                            + (product.getCaseqty() * product
                            .getCaseSize()) + (product
                            .getOuterQty() * product.getOuterSize())) * product
                            .getBaseprice());

            }
            return totalPrice;
        } catch (Exception e) {
            Commons.printException(e);
        }
        return totalPrice;
    }

    /**
     * Load the Qty in EmptyReconciliationDetail and VanUnloadDetails for
     * Validation in Manaul Van Load while Collecting Return Products in sub
     * Depot
     */
    public void loadVanLoadReturnProductValidation() {
        DBUtil db = null;
        Cursor c;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();
            c = db.selectSQL("SELECT Pid,(SUM(QTY)) FROM EmptyReconciliationDetail GROUP BY Pid");
            if (c != null) {
                while (c.moveToNext()) {
                    for (BomRetunBo bom : bmodel.productHelper
                            .getBomReturnProducts()) {
                        if (c.getString(0).equals(bom.getPid())) {
                            bom.setTotalReturnQty(c.getInt(1));
                            break;
                        }

                    }

                }

                c.close();
            }
            db.closeDB();
        } catch (SQLException e) {
            if (db != null) {
                db.closeDB();
            }
            Commons.printException(e);
        }

    }

    public void clearBomReturnProductsTable() {
        try {
            for (BomRetunBo temp : bmodel.productHelper.getBomReturnProducts()) {
                temp.setLiableQty(0);
                temp.setReturnQty(0);
            }

            // Manually clear the objects in OrderHeaderBO
            bmodel.setOrderHeaderBO(null);
        } catch (Exception e) {
            Commons.printException(e);
        }
    }


    public ArrayList<VanLoadMasterBO> downloadExistingUid() {
        DBUtil db = null;
        ArrayList<VanLoadMasterBO> mUidList = new ArrayList<>();
        VanLoadMasterBO vanBo;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();
            Cursor cursor = db
                    .selectSQL("SELECT Distinct Uid FROM VanLoad ORDER BY Uid");
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    vanBo = new VanLoadMasterBO();
                    vanBo.setRfield1(cursor.getString(0));
                    mUidList.add(vanBo);
                }
                cursor.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
            if (db != null) {
                db.closeDB();
            }
        }
        return mUidList;

    }


    public ArrayList<SubDepotBo> getSubDepotList() {
        return subDepotList;
    }

    public ArrayList<SubDepotBo> getDistributorList() {
        if (distributorList != null) {
            return distributorList;
        }
        return new ArrayList<>();
    }

    private Vector<VanLoadMasterBO> getVanLoad() {
        return vanmasterbo;
    }


    public float getmVanLoadAmount() {
        return mVanLoadAmount;
    }

    public void setmVanLoadAmount(float mVanLoadAmount) {
        this.mVanLoadAmount = mVanLoadAmount;
    }

    public boolean isSecondaryDistributorDone() {
        DBUtil db = null;
        Cursor cursor;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();
            db.createDataBase();
            for (SubDepotBo bo : getDistributorList()) {
                if (bmodel.userMasterHelper.getUserMasterBO()
                        .getDistributorid() != bo.getSubDepotId()) {
                    cursor = db
                            .selectSQL("Select pid from vanload where subdepotid="
                                    + bo.getSubDepotId());
                    if (cursor != null) {
                        if (cursor.getCount() > 0) {
                            return true;
                        }
                        cursor.close();

                    }
                }
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
            if (db != null) {
                db.closeDB();
            }
        }
        return false;
    }


    public void saveVanUnLoad(Vector<LoadManagementBO> mylist) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
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
                            + bmodel.QT(bmodel.userMasterHelper
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

}
