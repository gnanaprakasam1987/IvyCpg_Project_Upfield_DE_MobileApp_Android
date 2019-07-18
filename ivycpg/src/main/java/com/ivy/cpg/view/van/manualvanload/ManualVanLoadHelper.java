package com.ivy.cpg.view.van.manualvanload;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

import com.ivy.cpg.view.van.LoadManagementHelper;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.BomBO;
import com.ivy.sd.png.bo.BomMasterBO;
import com.ivy.sd.png.bo.BomReturnBO;
import com.ivy.sd.png.bo.LoadManagementBO;
import com.ivy.sd.png.bo.OrderHeader;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.cpg.view.emptyreconcil.EmptyReconciliationHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.StringUtils;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by Hanifa on 20/8/18.
 */

public class ManualVanLoadHelper {
    private Context context;
    private BusinessModel bmodel;
    private static ManualVanLoadHelper instance = null;

    public ManualVanLoadHelper(Context context) {
        this.context = context;
        this.bmodel = (BusinessModel) context;

    }

    public static ManualVanLoadHelper getInstance(Context context) {
        if (instance == null) {
            instance = new ManualVanLoadHelper(context);
        }
        return instance;
    }

    /**
     * Load Data for Manual VanLoad Screen
     *
     * @param menuCode - menu code is used to insert record into ModuleActivityDetails table
     */
    public void loadManuvalVanLoadData(String menuCode) {

        if (bmodel.configurationMasterHelper.SHOW_SUBDEPOT) {
            LoadManagementHelper.getInstance(context).downloadSubDepots();
        }

        bmodel.productHelper.downloadLoadMgmtProductsWithFiveLevel(
                "MENU_LOAD_MANAGEMENT", "MENU_MANUAL_VAN_LOAD");

        bmodel.updateProductUOM(StandardListMasterConstants.mActivityCodeByMenuCode.get(menuCode), 2);

        if (bmodel.configurationMasterHelper.SHOW_PRODUCTRETURN) {

            bmodel.productHelper.downlaodReturnableProducts("MENU_LOAD_MANAGEMENT");
            bmodel.productHelper.downloadBomMaster();
            bmodel.productHelper.downloadGenericProductID();
            loadVanLoadReturnProductValidation();
            updateModuleWiseTimeStampDetails(menuCode);

        }

        OrderHeader ordHeadBO = new OrderHeader();
        bmodel.setOrderHeaderBO(ordHeadBO);
    }


    /**
     * insert module wise activity details
     *
     * @param menuCode
     */
    private void updateModuleWiseTimeStampDetails(String menuCode) {
        bmodel.moduleTimeStampHelper.setTid("MTS" + bmodel.userMasterHelper.getUserMasterBO().getUserid() + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID));
        bmodel.moduleTimeStampHelper.setModuleCode(menuCode);
        bmodel.moduleTimeStampHelper.saveModuleTimeStamp("In");

    }


    /**
     * Load the Qty in EmptyReconciliationDetail and VanUnloadDetails for
     * Validation in Manaul Van Load while Collecting Return Products in sub
     * Depot
     */
    private void loadVanLoadReturnProductValidation() {
        DBUtil db = null;
        Cursor c;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME);
            db.openDataBase();
            c = db.selectSQL("SELECT Pid,(SUM(QTY)) FROM EmptyReconciliationDetail GROUP BY Pid");
            if (c != null) {
                while (c.moveToNext()) {
                    for (BomReturnBO bom : bmodel.productHelper
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

    /**
     * VanLoad Done validation
     *
     * @return
     */
    public boolean hasVanLoadDone() {

        int siz = bmodel.productHelper.getLoadMgmtProducts().size();
        if (siz == 0)
            return false;
        for (int i = 0; i < siz; ++i) {
            LoadManagementBO product = bmodel.productHelper
                    .getLoadMgmtProducts().get(i);
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
        int mPaymentTypeId = 0;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME);
            db.createDataBase();
            db.openDataBase();

            int siz = mylist.size();
            String columns = "uid,pid,caseQty,pcsQty,qty,date,upload,outerQty,duomQty,duomid,dOuomQty,dOuomId,BatchId,batchno,SubDepotId,Flag";

            String uid = bmodel.userMasterHelper.getUserMasterBO().getUserid()
                    + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);

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
                                    values = StringUtils.getStringQueryParam(uid)
                                            + ","
                                            + product.getProductid()
                                            + ","
                                            + bo.getCaseqty()
                                            + ","
                                            + bo.getPieceqty()
                                            + ","
                                            + totalsih
                                            + ","
                                            + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL))
                                            + ","
                                            + StringUtils.getStringQueryParam("Y")
                                            + ","
                                            + bo.getOuterQty() + ","
                                            + product.getCaseSize() + ","
                                            + product.getdUomid() + ","
                                            + product.getOuterSize() + ","
                                            + product.getdOuonid() + ","
                                            + StringUtils.getStringQueryParam(batchid) + ","
                                            + StringUtils.getStringQueryParam(bo.getBatchNo()) + ","
                                            + selectedSubDepotID + "," + 1;

                                    db.insertSQL(DataMembers.tbl_vanload, columns,
                                            values);
                                }
                            }

                        }
                    } else {

                        batchid = 0 + "";
                        values = StringUtils.getStringQueryParam(uid)
                                + ","
                                + product.getProductid()
                                + ","
                                + product.getCaseqty()
                                + ","
                                + product.getPieceqty()
                                + ","
                                + totalsih
                                + ","
                                + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL))
                                + ","
                                + StringUtils.getStringQueryParam("Y")
                                + ","
                                + product.getOuterQty() + ","
                                + product.getCaseSize() + ","
                                + product.getdUomid() + ","
                                + product.getOuterSize() + ","
                                + product.getdOuonid() + ","
                                + StringUtils.getStringQueryParam(batchid) + ","
                                + 0 + ","
                                + selectedSubDepotID + "," + 1;

                        db.insertSQL(DataMembers.tbl_vanload, columns,
                                values);
                    }
                }

            }

            if (bmodel.configurationMasterHelper.SHOW_PRODUCTRETURN) {
                ArrayList<BomReturnBO> returnProducts;
                String returncolumns = "Uid,Date,Pid,LiableQty,pcsqty,Price, dUomId,TypeID,LineValue,SubDepotId,RefId";

                returnProducts = bmodel.productHelper.getBomReturnProducts();

                String tranId = bmodel.userMasterHelper.getUserMasterBO()
                        .getUserid() + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);

                for (BomReturnBO bomReturnBo : returnProducts) {

                    if (bomReturnBo.getLiableQty() > 0
                            || bomReturnBo.getReturnQty() > 0) {

                        values = tranId
                                + ","
                                + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL))
                                + ","
                                + StringUtils.getStringQueryParam(bomReturnBo.getPid())
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
                        .getUserid() + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);

                values = tid + "," + mPaymentTypeId + ","
                        + SDUtil.format(getmVanLoadAmount(), 2, 0) + ","
                        + selectedSubDepotID + "," + uid + ","
                        + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL));

                db.insertSQL(DataMembers.tbl_SubDepotSettlement,
                        subDepotColumns, values);

                EmptyReconciliationHelper.getInstance(context)
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

    /**
     * get product wise batch id
     *
     * @param pid     - product id
     * @param batchno
     * @return
     */
    private String getbatchid(int pid, String batchno) {
        String batchid = null;
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT batchid from BatchMaster where batchNum="
                            + StringUtils.getStringQueryParam(batchno) + " and pid=" + pid);
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

    private float mVanLoadAmount = 0;


    public float getmVanLoadAmount() {
        return mVanLoadAmount;
    }


    public void setmVanLoadAmount(float mVanLoadAmount) {
        this.mVanLoadAmount = mVanLoadAmount;
    }


    /**
     * This method used to clear liableQty and returnableQty from getBomReturnProducts List
     */
    public void clearBomReturnProductsTable() {
        try {
            for (BomReturnBO temp : bmodel.productHelper.getBomReturnProducts()) {
                temp.setLiableQty(0);
                temp.setReturnQty(0);
            }

            // Manually clear the objects in OrderHeaderBO
            bmodel.setOrderHeaderBO(null);
        } catch (Exception e) {
            Commons.printException(e);
        }
    }


    /**
     * @return -calculateVanLoadProductPrice
     */
    public double calculateVanLoadProductPrice() {
        double totalPrice = 0;
        try {
            int siz = bmodel.productHelper.getLoadMgmtProducts().size();
            if (siz == 0)
                return 0;
            for (int i = 0; i < siz; ++i) {
                LoadManagementBO product = bmodel.productHelper.getLoadMgmtProducts().get(i);
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
     * Save Manual Van Load Batch entry details
     *
     * @param product
     */

    public void saveBatch(LoadManagementBO product) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
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

            values = batchId + "," + StringUtils.getStringQueryParam(product.getManualBatchNo())
                    + "," + product.getProductid() + ","
                    + StringUtils.getStringQueryParam(product.getMfgDate()) + ","
                    + StringUtils.getStringQueryParam(product.getExpDate()) + ", 1";

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

        for (BomReturnBO bom : bmodel.productHelper.getBomReturnProducts()) {
            bom.setLiableQty(0);
        }

        for (LoadManagementBO sku : bmodel.productHelper.getLoadMgmtProducts()) {
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

                        for (BomReturnBO returnBo : bmodel.productHelper
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

}
