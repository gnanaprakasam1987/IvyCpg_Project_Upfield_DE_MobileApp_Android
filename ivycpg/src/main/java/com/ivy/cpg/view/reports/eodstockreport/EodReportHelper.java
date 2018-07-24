package com.ivy.cpg.view.reports.eodstockreport;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.LoadManagementBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.StockReportBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;


public class EodReportHelper {
    private ArrayList<StockReportBO> mEODStockReportList;
    private HashMap<String, StockReportBO> mEODReportBOByProductID;
    private Context mContext;
    private BusinessModel bmodel;


    public EodReportHelper(Context mContext) {
        this.mContext = mContext;
        this.bmodel = (BusinessModel) mContext.getApplicationContext();
    }


    /**
     * Method to load EOD report details
     */
    public void downloadEODReport() {
        ArrayList<StockReportBO> tempStockReport = null;
        mEODStockReportList = new ArrayList<>();
        mEODReportBOByProductID = new HashMap<>();
        try {
            StockReportBO stockReportBO;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            StringBuffer sb = new StringBuffer();

            sb.append("SELECT PM.PID, PM.PName");
            sb.append(",PM.duomqty,PM.dOuomQty,PM.piece_uomid,PM.duomid,PM.dOuomid,PM.pcode  FROM ProductMaster PM");
            sb.append(" Where PM.TypeId NOT IN (SELECT ListID FROM StandardListMaster WHERE  ListCode ='GENERIC') ORDER BY PM.PID");

            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                tempStockReport = new ArrayList<>();
                while (c.moveToNext()) {
                    stockReportBO = new StockReportBO();
                    stockReportBO.setProductID(c.getString(0));
                    stockReportBO.setProductName(c.getString(1));
                    stockReportBO.setCaseSize(c.getInt(2));
                    stockReportBO.setOuterSize(c.getInt(3));
                    stockReportBO.setPiece_uomid(c.getInt(4));
                    stockReportBO.setdUomid(c.getInt(5));
                    stockReportBO.setdOuomid(c.getInt(6));
                    stockReportBO.setProductCode(c.getString(7));


                    tempStockReport.add(stockReportBO);
                }
            }

            // Duplicating product batchwise
            if (tempStockReport != null) {
                HashMap<String, ArrayList<ProductMasterBO>> mBatchList = downloadBatchList();
                for (StockReportBO bo : tempStockReport) {
                    if (mBatchList.get(bo.getProductID()) != null) {

                        for (ProductMasterBO batchBO : mBatchList.get(bo.getProductID())) {

                            StockReportBO newBO = new StockReportBO(bo);
                            newBO.setBatchNo(batchBO.getBatchNo());
                            newBO.setBatchId(batchBO.getBatchid());
                            mEODStockReportList.add(newBO);

                        }

                    } else {
                        mEODStockReportList.add(bo);
                    }
                }
                tempStockReport = null;
            }
            //
            c.close();

            // get vanload qty
            sb = new StringBuffer();
            sb.append("SELECT PID,batchid,Qty  FROM StockInHandMaster");
            c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {

                    int qty = c.getInt(2);
                    int batchid = c.getInt(1);

                    for (StockReportBO bo : mEODStockReportList) {
                        if (batchid == 0) {

                            if (bo.getProductID().equals(c.getString(0))) {
                                bo.setSih(bo.getSih() + qty);
                            }
                        } else {
                            if (bo.getProductID().equals(c.getString(0)) && bo.getBatchId() != null && bo.getBatchId().equals(batchid + "")) {

                                bo.setSih(bo.getSih() + qty);
                            }
                        }
                    }

                }
            }
            c.close();


            //load return qty
            sb = new StringBuffer();
            sb.append("select SD.Productid,sum(SD.totalQty),SD.batchid from SalesReturnDetails SD  inner join SalesReturnHeader SH on ");
            sb.append("SD.uid=SH.uid inner join Standardlistmaster SLM on SLM.listid=SD.condition  AND  SLM.ListType = 'REASON'");
            sb.append(" inner join Standardlistmaster SLM1 on SLM.parentid=SLM1.listid and SLM1.ListCode='SRS' where SH.upload!='X'");
            sb.append(" group by SD.Productid,SD.batchid");
            c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    setEODObject(c.getString(0), c.getString(2), c.getInt(1), QtyType.RETURN);
                }
            }
            c.close();

            // get OrderQty(sold) FROM ORDER DETAIL
            sb = new StringBuffer();
            sb.append("select productid,sum(Qty),batchid as Qty from orderdetail OD inner join Orderheader OH on ");
            sb.append("OH.orderid =OD.orderid where OH.is_vansales=1 and od.ordertype=0 and OH.upload!='X' group by productid,batchid");
            c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    setEODObject(c.getString(0), c.getString(2), c.getInt(1), QtyType.SOLD);
                }
            }
            c.close();

            // get freeQty for crown FROM ORDER DETAIL
            sb = new StringBuffer();
            sb.append("select OD.Productid,sum(OD.Qty),batchid from orderDetail OD  inner join OrderHeader OH on ");
            sb.append("OD.orderid=OH.orderid where OH.upload!='X' and OH.is_vansales=1 and OD.Ordertype!=0 group by OD.Productid,batchid ");
            c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    setEODObject(c.getString(0), c.getString(2), c.getInt(1), QtyType.FREE_ISSUED);
                }
            }
            c.close();

            // get freeQty for scheme free product from SchemeFreeProductDetail
            sb = new StringBuffer();
            sb.append("select FreeProductid,sum(case when uomCount!=0 then FreeQty*UomCount else FreeQty end) as Qty,batchid");
            sb.append(" from SchemeFreeproductDetail group by FreeProductid,batchid");
            c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    setEODObject(c.getString(0), c.getString(2), c.getInt(1), QtyType.FREE_ISSUED);
                }
            }
            c.close();

            //load replacement qty
            sb = new StringBuffer();
            sb.append("select pid,sum(case when uomCount!=0 then qty*UomCount else qty end) as Qty,batchid from SalesReturnReplacementDetails where upload!='X' group by pid,batchid");
            c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    setEODObject(c.getString(0), c.getString(2), c.getInt(1), QtyType.REPLACEMENT);
                }
            }
            c.close();

            // Empty qty from VanUnloadDetails and EmptyReconcilationDetail
            sb = new StringBuffer();
            sb.append("SELECT ED.PId, SUM(ED.Qty) AS Qty, VUD.QTY FROM EmptyReconciliationDetail ED Left Join ");
            sb.append("(SELECT pid, SUM(pcsqty) AS QTY  FROM VanUnloadDetails ");
            sb.append("WHERE SubDepotID != 0 Group By pid) as VUD on VUD.pid=ED.pid group by ED.pid");
            c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    String porductid = c.getString(0);
                    int eodQty = c.getInt(1);
                    int unloadQty = c.getInt(2);
                    int qty = eodQty - unloadQty;
                    setEODObject(porductid, "0", qty, QtyType.EMPTY);
                }
            }
            c.close();

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }


    public HashMap<String, ArrayList<ProductMasterBO>> downloadBatchList() {
        HashMap<String, ArrayList<ProductMasterBO>> mBatchListByproductID = new HashMap<>();
        ProductMasterBO productBO;
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        try {

            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("select  PM.pid, PM.pcode,PM.pname,PM.psname,BM.batchid,BM.batchNum,BM.MfgDate,BM.ExpDate");
            sb.append(" from BatchMAster BM inner join  ProductMaster PM on (BM.Pid= PM.pid)");
            sb.append("  order by PM.pid,BM.MfgDate asc");

            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                String productid = "";
                ArrayList<ProductMasterBO> batchList = new ArrayList<ProductMasterBO>();
                while (c.moveToNext()) {
                    productBO = new ProductMasterBO();
                    productBO.setProductID(c.getString(0));
                    productBO.setProductCode(c.getString(1));
                    productBO.setProductName(c.getString(2));
                    productBO.setProductShortName(c.getString(3));

                    productBO.setBatchid(c.getString(4));
                    productBO.setBatchNo(c.getString(5));
                    productBO.setMfgDate(c.getString(6));
                    productBO.setExpDate(c.getString(7));

                    if (!productid.equals(productBO.getProductID())) {
                        if (productid != "") {
                            mBatchListByproductID.put(productid, batchList);
                            batchList = new ArrayList<>();
                            batchList.add(productBO);
                            productid = productBO.getProductID();
                        } else {
                            batchList.add(productBO);
                            productid = productBO.getProductID();
                        }
                    } else {
                        batchList.add(productBO);
                    }

                }

                if (batchList.size() > 0) {
                    mBatchListByproductID.put(productid, batchList);
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            db.closeDB();
            return mBatchListByproductID;
        }
        return mBatchListByproductID;
    }


    /**
     * method to set EOD object for corresponding Product
     *
     * @param productid
     * @param qty
     * @param type
     */
    private void setEODObject(String productid, String batchid, int qty, QtyType type) {

        String lastProductid = "";
        for (StockReportBO stockReportBO : mEODStockReportList) {
            if (stockReportBO != null) {
                if (batchid.equals("0")) {
                    if (stockReportBO.getProductID().equals(productid)) {

                        // To set value to first batch only
                        if (!lastProductid.equals(productid)) {

                            if (type == QtyType.FREE_ISSUED) {
                                // added free qty,crown and free issues
                                stockReportBO.setFreeIssuedQty(qty
                                        + stockReportBO.getFreeIssuedQty());
                            } else if (type == QtyType.SOLD) {
                                stockReportBO.setSoldQty(qty);
                            } else if (type == QtyType.EMPTY) {
                                stockReportBO.setEmptyBottleQty(qty);
                            } else if (type == QtyType.REPLACEMENT) {
                                stockReportBO.setReplacementQty(qty);
                            } else if (type == QtyType.RETURN) {
                                stockReportBO.setReturnQty(qty);
                            }
                            lastProductid = productid;
                        }

                    }
                } else {
                    if (stockReportBO.getProductID().equals(productid)
                            && stockReportBO.getBatchId().equals(batchid)) {

                        if (type == QtyType.FREE_ISSUED) {
                            // added free qty,crown and free issues
                            stockReportBO.setFreeIssuedQty(qty
                                    + stockReportBO.getFreeIssuedQty());
                        } else if (type == QtyType.SOLD) {
                            stockReportBO.setSoldQty(qty);
                        } else if (type == QtyType.EMPTY) {
                            stockReportBO.setEmptyBottleQty(qty);
                        } else if (type == QtyType.REPLACEMENT) {
                            stockReportBO.setReplacementQty(qty);
                        } else if (type == QtyType.RETURN) {
                            stockReportBO.setReturnQty(qty);
                        }


                    }
                }

            }
        }

    }


    private enum QtyType {
        RETURN(1),
        SOLD(2),
        FREE_ISSUED(3),
        REPLACEMENT(4),
        EMPTY(5);
        private int value;

        QtyType(int value) {
            this.value = value;
        }
    }


    public void updateBaseUOM(String activity, int reportType) {
        //reportType(1)-EOD, reportType(2)-currentStock, reportType(3)-CurrentStockBatchwise
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.createDataBase();
        db.openDataBase();
        try {

            int contentLevel = 0;
            int contentLevelId = 0;
            Cursor c = db.selectSQL("SELECT MAX(Sequence),levelId FROM ProductLevel");
            if (c != null) {
                if (c.moveToNext()) {
                    contentLevel = c.getInt(0);
                    contentLevelId = c.getInt(1);
                }
            }

            c = db.selectSQL("SELECT productId, productLevelId, uomid from ActivityGroupMapping AGM" +
                    " INNER JOIN ActivityGroupProductMapping APM ON APM.groupid=AGM.groupid" +
                    " where AGM.activity=" + bmodel.QT(activity));
            if (c != null) {
                if (c.getCount() > 0) {
                    initializeUOMmapping(reportType);
                    while (c.moveToNext()) {
                        updateProductMapping(c.getString(0), c.getInt(1), c.getInt(2), contentLevel, contentLevelId, reportType);
                    }
                } else {
                    enableUOMForAllProducts(reportType);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            db.closeDB();
            Commons.printException("" + e);
        }
    }


    private void updateProductMapping(String productId, int pLevelId, int uomId, int contentLevel,
                                      int contentLevelId, int reportType) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.createDataBase();
        db.openDataBase();

        try {

            if (contentLevelId == pLevelId) {
                if (reportType == 1) {
                    for (StockReportBO bo : getEODStockReport()) {
                        if (bo.getProductID().equals(productId)) {
                            if (bo.getPiece_uomid() == uomId)
                                bo.setBaseUomPieceWise(true);
                            else if (bo.getdUomid() == uomId)
                                bo.setBaseUomCaseWise(true);
                            else if (bo.getdOuomid() == uomId)
                                bo.setBaseUomOuterWise(true);
                        }
                    }
                } else if (reportType == 2) {
                    for (StockReportBO bo : getCurrentStock()) {
                        if (bo.getProductID().equals(productId)) {
                            if (bo.getPiece_uomid() == uomId)
                                bo.setBaseUomPieceWise(true);
                            else if (bo.getdUomid() == uomId)
                                bo.setBaseUomCaseWise(true);
                            else if (bo.getdOuomid() == uomId)
                                bo.setBaseUomOuterWise(true);
                        }
                    }
                } else if (reportType == 3) {
                    for (LoadManagementBO bo : bmodel.productHelper.getLoadMgmtProducts()) {
                        if (productId.equals(bo.getProductid() + "")) {
                            if (bo.getPiece_uomid() == uomId)
                                bo.setBaseUomPieceWise(true);
                            else if (bo.getdUomid() == uomId)
                                bo.setBaseUomCaseWise(true);
                            else if (bo.getdOuonid() == uomId)
                                bo.setBaseUomOuterWise(true);
                        }
                    }
                }

            } else {

                int parentLevel = 0;

                Cursor c = db.selectSQL("SELECT Sequence FROM ProductLevel where levelId=" + pLevelId);
                if (c != null) {
                    if (c.moveToNext())
                        parentLevel = c.getInt(0);
                }

                int loopEnd = contentLevel - parentLevel + 1;

                StringBuilder sb = new StringBuilder();
                sb.append("select distinct PM" + loopEnd + ".pid from productmaster PM1 ");
                for (int i = 2; i <= loopEnd; i++) {
                    sb.append(" INNER JOIN ProductMaster PM" + i + " ON PM" + i
                            + ".ParentId = PM" + (i - 1) + ".PID");
                }
                sb.append(" where PM1.pid=" + productId);

                c = db.selectSQL(sb.toString());
                if (c.getCount() > 0) {
                    while (c.moveToNext()) {

                        if (reportType == 1) {
                            for (StockReportBO bo : getEODStockReport()) {
                                if (bo.getProductID().equals(c.getString(0))) {
                                    if (bo.getPiece_uomid() == uomId)
                                        bo.setBaseUomPieceWise(true);
                                    else if (bo.getdUomid() == uomId)
                                        bo.setBaseUomCaseWise(true);
                                    else if (bo.getdOuomid() == uomId)
                                        bo.setBaseUomOuterWise(true);
                                }
                            }
                        } else if (reportType == 2) {
                            for (StockReportBO bo : getCurrentStock()) {
                                if (bo.getProductID().equals(c.getString(0))) {
                                    if (bo.getPiece_uomid() == uomId)
                                        bo.setBaseUomPieceWise(true);
                                    else if (bo.getdUomid() == uomId)
                                        bo.setBaseUomCaseWise(true);
                                    else if (bo.getdOuomid() == uomId)
                                        bo.setBaseUomOuterWise(true);
                                }
                            }
                        } else if (reportType == 3) {
                            for (LoadManagementBO bo : bmodel.productHelper.getLoadMgmtProducts()) {
                                if (c.getString(0).equals(bo.getProductid() + "")) {
                                    if (bo.getPiece_uomid() == uomId)
                                        bo.setBaseUomPieceWise(true);
                                    else if (bo.getdUomid() == uomId)
                                        bo.setBaseUomCaseWise(true);
                                    else if (bo.getdOuonid() == uomId)
                                        bo.setBaseUomOuterWise(true);
                                }
                            }
                        }

                    }

                }

                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            db.closeDB();
            Commons.printException("" + e);
        }
    }


    private void enableUOMForAllProducts(int reportType) {
        if (reportType == 1) {
            for (StockReportBO bo : getEODStockReport()) {
                bo.setBaseUomCaseWise(true);
                bo.setBaseUomOuterWise(true);
                bo.setBaseUomPieceWise(true);
            }
        } else if (reportType == 2) {
            for (StockReportBO bo : getCurrentStock()) {
                bo.setBaseUomCaseWise(true);
                bo.setBaseUomOuterWise(true);
                bo.setBaseUomPieceWise(true);
            }
        } else if (reportType == 3) {
            for (LoadManagementBO bo : bmodel.productHelper.getLoadMgmtProducts()) {
                bo.setBaseUomCaseWise(true);
                bo.setBaseUomOuterWise(true);
                bo.setBaseUomPieceWise(true);
            }
        }
    }

    private void initializeUOMmapping(int reportType) {
        if (reportType == 1) {
            for (StockReportBO bo : getEODStockReport()) {
                bo.setBaseUomCaseWise(false);
                bo.setBaseUomOuterWise(false);
                bo.setBaseUomPieceWise(false);
            }
        } else if (reportType == 2) {
            for (StockReportBO bo : getCurrentStock()) {
                bo.setBaseUomCaseWise(false);
                bo.setBaseUomOuterWise(false);
                bo.setBaseUomPieceWise(false);
            }
        } else if (reportType == 3) {
            for (LoadManagementBO bo : bmodel.productHelper.getLoadMgmtProducts()) {
                bo.setBaseUomCaseWise(false);
                bo.setBaseUomOuterWise(false);
                bo.setBaseUomPieceWise(false);
            }
        }
    }


    public ArrayList<StockReportBO> getEODStockReport() {
        return mEODStockReportList;
    }

    private Vector<StockReportBO> getCurrentStock() {
        return currentStock;
    }

    private Vector<StockReportBO> currentStock;

}
