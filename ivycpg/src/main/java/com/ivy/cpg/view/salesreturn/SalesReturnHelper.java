package com.ivy.cpg.view.salesreturn;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;

import com.ivy.lib.Utils;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.CreditNoteListBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.SalesReturnReportBO;
import com.ivy.sd.png.bo.TaxBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.TaxInterface;
import com.ivy.sd.png.provider.ProductHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.DateUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Vector;

public class SalesReturnHelper {

    private final BusinessModel bmodel;
    private String lpcValue;
    private double returnValue;
    private List<SalesReturnReportBO> damagedSalesReport;

    private static SalesReturnHelper instance = null;
    private double saleableValue = 0;
    private double nonsaleablevalue = 0;
    private String salesReturnID;
    private String creditNoteId;
    private double return_amt;
    public boolean SHOW_SAL_RET_OLD_MRP;
    public boolean SHOW_SAL_RET_MRP;
    public boolean SHOW_SAL_RET_MFG_DATE;
    public boolean SHOW_SAL_RET_EXP_DATE;
    public boolean SHOW_SAL_RET_SRP;
    public boolean SHOW_SRP_EDIT;
    public boolean SHOW_LOTNUMBER;
    public boolean SHOW_STOCK_REPLACE_PCS;
    public boolean SHOW_STOCK_REPLACE_CASE;
    public boolean SHOW_STOCK_REPLACE_OUTER;
    public boolean SHOW_SR_INVOICE_NUMBER;
    public boolean SHOW_SIH;
    public boolean SHOW_SALES_RET_CASE;
    public boolean SHOW_SALES_RET_PCS;
    public boolean SHOW_SALES_RET_OUTER_CASE;
    private String CODE_CHECK_MRP = "SR07";
    public boolean CHECK_MRP_VALUE;
    private String CODE_SHOW_REMARKS_SAL_RET = "REM4";
    public boolean SHOW_REMARKS_SAL_RET;
    private String CODE_SR_DISCOUNT = "SR10";
    private String CODE_SR_TAX = "SR11";
    public boolean IS_APPLY_DISCOUNT_IN_SR;
    public boolean IS_APPLY_TAX_IN_SR;
    private String CODE_SR_DIFF_CNT = "SR12";
    public boolean IS_PRD_CNT_DIFF_SR;


    public static final String CREDIT_TYPE = "CREDIT";

    private double totalValue = 0;

    private String SignaturePath;
    private boolean isSignCaptured;
    private String SignatureName;

    private SalesReturnHelper(Context context) {
        this.bmodel = (BusinessModel) context.getApplicationContext();
    }

    public static SalesReturnHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SalesReturnHelper(context);
        }
        return instance;
    }

    public String getLpcValue() {
        return lpcValue;
    }

    public void setLpcValue(String lpcValue) {
        this.lpcValue = lpcValue;
    }

    public double getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(double returnValue) {
        this.returnValue = returnValue;
    }

    public double getSaleableValue() {
        return saleableValue;
    }

    public void setSaleableValue(double saleableValue) {
        this.saleableValue = saleableValue;
    }

    public double getNonsaleablevalue() {
        return nonsaleablevalue;
    }

    public void setNonsaleablevalue(double nonsaleablevalue) {
        this.nonsaleablevalue = nonsaleablevalue;
    }

    public String getSalesReturnID() {
        return salesReturnID;
    }

    public void setSalesReturnID(String salesReturnID) {
        this.salesReturnID = salesReturnID;
    }

    public boolean isSignCaptured() {
        return isSignCaptured;
    }

    public void setIsSignCaptured(boolean isSignCaptured) {
        this.isSignCaptured = isSignCaptured;
    }

    public String getSignatureName() {
        return SignatureName;
    }

    public void setSignatureName(String signatureName) {
        SignatureName = signatureName;
    }

    public String getSignaturePath() {
        return SignaturePath;
    }

    public void setSignaturePath(String signaturePath) {
        SignaturePath = signaturePath;
    }

    public String getCreditNoteId() {
        return creditNoteId;
    }

    public void setCreditNoteId(String creditNoteId) {
        this.creditNoteId = creditNoteId;
    }

    public double getReturn_amt() {
        return return_amt;
    }

    public void setReturn_amt(double return_amt) {
        this.return_amt = return_amt;
    }

    public double getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(double totalValue) {
        this.totalValue = totalValue;
    }

    /**
     * Check weather product Master has any salesreturn.
     *
     * @return - true or false
     */
    public boolean hasSalesReturn() {
        int siz = bmodel.productHelper.getSalesReturnProducts().size();
        if (siz == 0)
            return false;

        for (int i = 0; i < siz; ++i) {
            ProductMasterBO product = bmodel.productHelper.getSalesReturnProducts().get(i);
            if (!product.getSalesReturnReasonList().isEmpty())
                for (SalesReturnReasonBO bo : product
                        .getSalesReturnReasonList()) {
                    if (bo.getCaseQty() > 0 || bo.getPieceQty() > 0 || bo.getOuterQty() > 0) {
                        return true;
                    }
                }
        }
        return false;
    }

    public float getSalesReturnValue(Context mContext) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.createDataBase();
        db.openDataBase();
        float total = 0;
        Cursor c = db
                .selectSQL("select ifnull(sum(returnvalue),0) from SalesReturnHeader where retailerid="
                        + QT(bmodel.getRetailerMasterBO().getRetailerID()) + " and distributorid=" + bmodel.getRetailerMasterBO().getDistributorId());
        if (c != null) {
            if (c.getCount() > 0) {
                c.moveToNext();
                total = c.getFloat(0);
            }
            c.close();
        }
        db.closeDB();
        return total;
    }

    /**
     * This method will load salesreturn related configurations and set the variables.
     */
    public void loadSalesReturnConfigurations(Context mContext) {
        try {
            SHOW_SAL_RET_OLD_MRP = false;
            SHOW_SAL_RET_MRP = false;
            SHOW_SAL_RET_MFG_DATE = false;
            SHOW_SAL_RET_EXP_DATE = false;
            SHOW_SAL_RET_SRP = false;
            SHOW_SRP_EDIT = false;
            SHOW_LOTNUMBER = false;
            SHOW_STOCK_REPLACE_PCS = false;
            SHOW_STOCK_REPLACE_CASE = false;
            SHOW_STOCK_REPLACE_OUTER = false;
            SHOW_SR_INVOICE_NUMBER = false;
            CHECK_MRP_VALUE = false;
            SHOW_REMARKS_SAL_RET = false;
            IS_APPLY_DISCOUNT_IN_SR = false;
            IS_APPLY_TAX_IN_SR = false;
            IS_PRD_CNT_DIFF_SR = false;
            SHOW_SALES_RET_CASE = false;
            SHOW_SALES_RET_PCS = false;
            SHOW_SALES_RET_OUTER_CASE = false;

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            String sql = "select RField from " + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode='SR01'";
            Cursor c = db.selectSQL(sql);
            String codeValue = null;
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    codeValue = c.getString(0);
                }
            }
            if (codeValue != null) {
                String[] codeSplit = codeValue.split(",");
                for (String temp : codeSplit) {
                    if ("OMRP".equals(temp))
                        SHOW_SAL_RET_OLD_MRP = true;
                    else if ("MRP".equals(temp))
                        SHOW_SAL_RET_MRP = true;
                    else if ("MFD".equals(temp))
                        SHOW_SAL_RET_MFG_DATE = true;
                    else if ("EXD".equals(temp))
                        SHOW_SAL_RET_EXP_DATE = true;
                    else if ("SRP".equals(temp))
                        SHOW_SAL_RET_SRP = true;
                    else if ("SRPET".equals(temp))
                        SHOW_SRP_EDIT = true;
                    else if ("LNO".equalsIgnoreCase(temp))
                        SHOW_LOTNUMBER = true;
                    else if ("RPS".equalsIgnoreCase(temp))
                        SHOW_STOCK_REPLACE_PCS = true;
                    else if ("RCS".equalsIgnoreCase(temp))
                        SHOW_STOCK_REPLACE_CASE = true;
                    else if ("ROU".equalsIgnoreCase(temp))
                        SHOW_STOCK_REPLACE_OUTER = true;
                    else if ("INVNO".equalsIgnoreCase(temp))
                        SHOW_SR_INVOICE_NUMBER = true;
                    else if ("SIH".equalsIgnoreCase(temp))
                        SHOW_SIH = true;
                    else if ("CS".equalsIgnoreCase(temp))
                        SHOW_SALES_RET_CASE = true;
                    else if ("PS".equalsIgnoreCase(temp))
                        SHOW_SALES_RET_PCS = true;
                    else if ("OOC".equalsIgnoreCase(temp))
                        SHOW_SALES_RET_OUTER_CASE = true;
                }
                c.close();
            }

            sql = "select RField from "
                    + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode=" + bmodel.QT(CODE_CHECK_MRP) + " and Flag=1";
            c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    this.CHECK_MRP_VALUE = true;
                }
                c.close();
            }

            sql = "select RField from "
                    + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode=" + bmodel.QT(CODE_SHOW_REMARKS_SAL_RET) + " and Flag=1";
            c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    this.SHOW_REMARKS_SAL_RET = true;
                }
                c.close();
            }

            sql = "select RField from "
                    + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode=" + bmodel.QT(CODE_SR_DISCOUNT) + " and Flag=1";
            c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    this.IS_APPLY_DISCOUNT_IN_SR = true;
                }
                c.close();
            }

            sql = "select RField from "
                    + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode=" + bmodel.QT(CODE_SR_TAX) + " and Flag=1";
            c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    this.IS_APPLY_TAX_IN_SR = true;
                }
                c.close();
            }

            sql = "select RField from "
                    + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode=" + bmodel.QT(CODE_SR_DIFF_CNT) + " and Flag=1";
            c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    this.IS_PRD_CNT_DIFF_SR = true;
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    /**
     * Save sales return details and update SIH.
     */
    public void saveSalesReturn(Context mContext,String orderId,String module) {
        try {
            ProductMasterBO product;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            setSalesReturnID(QT("SR"
                    + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                    + SDUtil.now(SDUtil.DATE_TIME_ID)));
            // To generate Seqno based Sales Return Id
            if (bmodel.configurationMasterHelper.SHOW_SR_SEQUENCE_NO) {
                String seqNo;
                bmodel.insertSeqNumber("SR");
                seqNo = bmodel.downloadSequenceNo("SR");
                setSalesReturnID(QT(seqNo));
            }
            bmodel.remarksHelper.getRemarksBO().setTid(
                    getSalesReturnID().substring(1, getSalesReturnID().length() - 1));

            // Sales return edit mode is available for presales. So delete the
            // transaction before saving new one.
            if (!bmodel.configurationMasterHelper.IS_INVOICE) {
                String sb = "select uid from SalesReturnHeader where RetailerID=" +
                        bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()) +
                        " and upload='N' and distributorid=" + bmodel.retailerMasterBO.getDistributorId();
                Cursor c = db.selectSQL(sb);
                if (c.getCount() > 0) {
                    if (c.moveToFirst()) {
                        String uid = c.getString(0);
                        db.deleteSQL(DataMembers.tbl_SalesReturnHeader, "uid="
                                + DatabaseUtils.sqlEscapeString(uid), false);
                        db.deleteSQL(DataMembers.tbl_SalesReturnDetails, "uid="
                                + DatabaseUtils.sqlEscapeString(uid), false);
                        db.deleteSQL(DataMembers.tbl_SalesReturnReplacementDetails, "uid=" + DatabaseUtils.sqlEscapeString(uid), false);
                    }
                }
            }

            // Preapre and save salesreturn header.
            String columns = "uid,date,RetailerID,BeatID,UserID,ReturnValue,lpc,RetailerCode,remark,latitude,longitude,distributorid,DistParentID,SignaturePath,imgName,RefModuleTId,RefModule";
            String values = getSalesReturnID() + ","
                    + QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + ","
                    + QT(bmodel.retailerMasterBO.getRetailerID()) + ","
                    + bmodel.retailerMasterBO.getBeatID() + ","
                    + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                    + "," + QT(SDUtil.format(returnValue,
                    bmodel.configurationMasterHelper.PERCENT_PRECISION_COUNT, 0)) + "," + lpcValue + ","
                    + QT(bmodel.retailerMasterBO.getRetailerCode()) + ","
                    + QT(bmodel.getSaleReturnNote()) + ","
                    + QT(bmodel.mSelectedRetailerLatitude + "") + ","
                    + QT(bmodel.mSelectedRetailerLongitude + "") + ","
                    + bmodel.retailerMasterBO.getDistributorId() + ","
                    + bmodel.retailerMasterBO.getDistParentId() + ","
                    + QT(getSignaturePath()) + ","
                    + QT(getSignatureName());

            if(!orderId.equals(""))
                values = values+ ","+orderId+ ","+QT(module);
            else
                values = values+ ","+QT("")+ ","+QT("");

            db.insertSQL(DataMembers.tbl_SalesReturnHeader, columns, values);

            // insert sales replacement and decrease the stock in hand.
            if (SHOW_STOCK_REPLACE_OUTER || SHOW_STOCK_REPLACE_CASE || SHOW_STOCK_REPLACE_PCS) {
                saveReplacementDetails(db, getSalesReturnID(),module);
            }

            columns = "uid,ProductID,Pqty,Cqty,Condition,duomQty,oldmrp,mfgdate,expdate,outerQty,dOuomQty,dOuomid,duomid,batchid,invoiceno,srpedited,totalQty,totalamount,RetailerID,reason_type,LotNumber,piece_uomid,status,HsnCode";

            int siz = 0;
            if (module.equals("ORDER"))
                siz = bmodel.productHelper.getProductMaster().size();
            else
                siz = bmodel.productHelper.getSalesReturnProducts().size();
            int totalQty;
            double totalvalue = 0;
            for (int i = 0; i < siz; ++i) {

                if (module.equals("ORDER"))
                    product = bmodel.productHelper
                            .getProductMaster().elementAt(i);
                else
                    product = bmodel.productHelper
                            .getSalesReturnProducts().elementAt(i);

                for (SalesReturnReasonBO bo : product
                        .getSalesReturnReasonList()) {

                    if (bo.getPieceQty() > 0 || bo.getCaseQty() > 0
                            || bo.getOuterQty() > 0 || bo.getSrPieceQty() > 0 || bo.getSrCaseQty() > 0 || bo.getSrOuterQty() > 0 || bo.getSrPieceQty() > 0 || bo.getSrCaseQty() > 0) {
                        int reasonType = bo.getReasonCategory() != null ? "SRS".equals(bo
                                .getReasonCategory()) ? 0
                                : 1 : -1;
                        totalQty = bo.getPieceQty()
                                + (bo.getCaseQty() * product
                                .getCaseSize())
                                + (bo.getOuterQty() * product
                                .getOutersize());

                        values = getSalesReturnID()
                                + ","
                                + DatabaseUtils.sqlEscapeString(product.getProductID())
                                + ","
                                + bo.getPieceQty()
                                + ","
                                + bo.getCaseQty()
                                + ","
                                + DatabaseUtils.sqlEscapeString(bo
                                .getReasonID())
                                + ","
                                + bo.getCaseSize()
                                + ","
                                + QT(Utils.formatAsTwoDecimal(bo
                                .getOldMrp()))
                                + ","
                                + DatabaseUtils
                                .sqlEscapeString(SHOW_SAL_RET_MFG_DATE ?
                                        (bo.getMfgDate() == null || bo.getMfgDate().length() == 0) ?
                                                SDUtil.now(SDUtil.DATE_GLOBAL)
                                                : DateUtil.convertToServerDateFormat(bo.getMfgDate(), "yyyy/MM/dd")
                                        : "")
                                + ","
                                + DatabaseUtils
                                .sqlEscapeString(SHOW_SAL_RET_EXP_DATE ?
                                        (bo.getExpDate() == null || bo.getExpDate().length() == 0) ?
                                                SDUtil.now(SDUtil.DATE_GLOBAL)
                                                : DateUtil.convertToServerDateFormat(bo.getExpDate(), "yyyy/MM/dd")
                                        : "")
                                + ","
                                + bo.getOuterQty()
                                + ","
                                + bo.getOuterSize()
                                + ","
                                + product.getOuUomid()
                                + ","
                                + product.getCaseUomId()
                                + ","
                                + bmodel.productHelper
                                .getOldBatchIDByMfd(product
                                        .getProductID())
                                + ","
                                + QT((bo.getInvoiceno() == null || "null".equals(bo
                                .getInvoiceno())) ? ""
                                : bo.getInvoiceno())
                                + ","
                                + bo.getSrpedit()
                                + ","
                                + totalQty
                                + ","
                                + totalvalue
                                + ","
                                + QT(bmodel.retailerMasterBO
                                .getRetailerID()) + ","
                                + reasonType + "," + QT(bo.getLotNumber()) + "," + product.getPcUomid()
                                + "," + QT(bo.getStatus()) + "," + QT(product.getHsnCode());

                        db.insertSQL(
                                DataMembers.tbl_SalesReturnDetails,
                                columns, values);

                        if (bmodel.configurationMasterHelper.SHOW_UPDATE_SIH) {
                            if ("SRS".equals(bo.getReasonCategory())) {

                                int salRetSih = bo.getPieceQty()
                                        + (bo.getCaseQty() * product
                                        .getCaseSize())
                                        + (bo.getOuterQty() * product
                                        .getOutersize());
                                int calcSih = product.getSIH() + salRetSih;
                                product.setSIH(calcSih);
                                db.updateSQL("UPDATE ProductMaster SET sih = "
                                        + calcSih
                                        + " WHERE PID = "
                                        + QT(product.getProductID()));
                                int batchid = bmodel.productHelper
                                        .getOldBatchIDByMfd(product.getProductID());

                                Cursor c = db
                                        .selectSQL("select pid,ifnull(qty,0) from StockInHandMaster where pid="
                                                + QT(product.getProductID())
                                                + " and batchid=" + batchid);
                                if (c != null && c.getCount() > 0) {
                                    while (c.moveToNext()) {
                                        salRetSih += c.getInt(1);
                                    }
                                    db.updateSQL("UPDATE StockInHandMaster SET upload='N',qty = "
                                            + salRetSih
                                            + " WHERE pid = "
                                            + QT(product.getProductID())
                                            + " AND batchid = " + batchid);
                                    c.close();
                                } else {
                                    String sihMasterColumns = "pid,qty,batchid";
                                    String sihMastervalues = QT(product.getProductID())
                                            + ","
                                            + salRetSih + "," + batchid;
                                    db.insertSQL("StockInHandMaster",
                                            sihMasterColumns,
                                            sihMastervalues);
                                }
                                // update batchwise sih in object
                                bmodel.batchAllocationHelper
                                        .setBatchwiseSIH(product, Integer.toString(batchid)
                                                , salRetSih, false);
                            }
                        }
                    }
                }
                product.setSalesReturnReasonList(ProductHelper
                        .cloneIsolateList(product));
            }

            // If credit note is generated, then tax appyled details should get saved.
            if (bmodel.configurationMasterHelper.IS_CREDIT_NOTE_CREATION || bmodel.configurationMasterHelper.TAX_SHOW_INVOICE)
                saveSalesReturnTaxAndCreditNoteDetail(db, getSalesReturnID(),module,bmodel.retailerMasterBO.getRpTypeCode());

            bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                    .now(SDUtil.TIME));

            db.closeDB();
            bmodel.setSaleReturnNote("");
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private String QT(String data) // Quote
    {
        return "'" + data + "'";
    }

    public void loadDamagedProductReport(Context mContext) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            damagedSalesReport = new ArrayList<>();
            Cursor c = db
                    .selectSQL("SELECT DISTINCT PM.PName,SRD.batchid, IFNULL(BM.batchNUM,''),RM.ListName,SRD.Pqty," +
                            " SRD.Cqty, SRD.outerQty, SRD.oldmrp, SRD.mfgdate, SRD.expdate, SRH.ReturnValue, SRH.Lpc,SRD.ProductID," +
                            " PM.dUomQty,PM.dUomId,PM.dOuomQty,PM.dOuomid,PM.sih,PM.psname FROM SalesReturnDetails SRD" +
                            " INNER JOIN SalesReturnHeader SRH ON SRD.uid = SRH.uid" +
                            " INNER JOIN StandardListMaster RM ON SRD.Condition = RM.ListId" +
                            " AND SRD.reason_type=1 AND SRH.unload=0 AND RM.ParentId = (SELECT ListId FROM StandardListMaster WHERE ListType ='REASON_TYPE' AND ListCode = 'SR')" +
                            " LEFT JOIN BatchMaster BM on SRD.ProductID = BM.pid AND SRD.batchid = BM.batchid " +
                            " INNER JOIN ProductMaster PM on SRD.ProductID = PM.PID  " +
                            " GROUP BY RM.ListName, PM.PID ORDER BY RM.ListName, PM.PName");
            if (c != null && c.getCount() > 0) {
                while (c.moveToNext()) {
                    SalesReturnReportBO salBO = new SalesReturnReportBO();
                    salBO.setProductName(c.getString(0));
                    salBO.setBatchId(c.getInt(1));
                    salBO.setBatchNumber(c.getString(2));
                    salBO.setReasonDesc(c.getString(3));
                    salBO.setPieceQty(c.getInt(4));
                    salBO.setCaseQty(c.getInt(5));
                    salBO.setOuterQty(c.getInt(6));
                    salBO.setProductid(c.getString(12));
                    salBO.setdUomQty(c.getInt(13));
                    salBO.setdUomId(c.getInt(14));
                    salBO.setdOuomQty(c.getInt(15));
                    salBO.setdOuomid(c.getInt(16));
                    salBO.setSih(c.getInt(17));
                    salBO.setProductSortName(c.getString(18));
                    damagedSalesReport.add(salBO);
                }
            }
            if (c != null) {
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public List<SalesReturnReportBO> getDamagedSalesReport() {
        return damagedSalesReport;
    }

    public void getSalesReturnGoods(Context mContext) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.createDataBase();
        db.openDataBase();
        Cursor c = db
                .selectSQL("SELECT reason_type,sum(totalamount) as saleable from SalesReturnDetails sd  inner join SalesReturnHeader  sh on sh.RetailerID="
                        + bmodel.getRetailerMasterBO().getRetailerID()
                        + " and sd.uid=sh.uid and sh.invoicecreated=0 and sh.distributorid=" + bmodel.retailerMasterBO.getDistributorId() + " group by reason_type=0");
        if (c != null && c.getCount() > 0) {
            while (c.moveToNext()) {
                if ("0".equals(c.getString(0)))
                    setSaleableValue(c.getDouble(1));
            }
        }
        if (c != null) {
            c.close();
        }
        db.closeDB();
    }

    public void getNonSaleableReturnGoods(Context mContext) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.createDataBase();
        db.openDataBase();
        Cursor c = db
                .selectSQL("SELECT reason_type,sum(totalamount) as saleable from SalesReturnDetails sd  inner join SalesReturnHeader  sh on sh.RetailerID="
                        + bmodel.getRetailerMasterBO().getRetailerID()
                        + " and sd.uid=sh.uid and sh.distributorid=" + bmodel.retailerMasterBO.getDistributorId() + " group by reason_type=0");
        if (c != null && c.getCount() > 0) {
            while (c.moveToNext()) {
                if (!("0".equals(c.getString(0))))
                    setNonsaleablevalue(c.getDouble(1));
            }
        }
        if (c != null) {
            c.close();
        }
        db.closeDB();
    }

    public boolean isStockReplacementDone(Context mContext) {
        boolean flag = false;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db.selectSQL("select uid from "
                    + DataMembers.tbl_SalesReturnHeader + " where retailerid="
                    + QT(bmodel.getRetailerMasterBO().getRetailerID())
                    + " and credit_flag = 3 and distributorid=" + bmodel.retailerMasterBO.getDistributorId());
            if (c != null) {
                if (c.getCount() > 0) {
                    flag = true;
                }
                c.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return flag;
    }

    public boolean isValueReturned(Context mContext) {
        boolean flag = false;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select invoicecreated from SalesReturnHeader where Retailerid="
                            + bmodel.getRetailerMasterBO().getRetailerID() + " and distributorid=" + bmodel.retailerMasterBO.getDistributorId());
            if (c != null) {
                while (c.moveToNext()) {
                    flag = c.getDouble(0) == 1;
                }
                c.close();
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
        return flag;
    }

    public boolean isInvoiceCreated(Context mContext) {
        boolean flag = false;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select salesreturned,return_amt from InvoiceMaster where Retailerid="
                            + bmodel.getRetailerMasterBO().getRetailerID());
            if (c != null) {
                while (c.moveToNext()) {
                    flag = c.getDouble(0) == 1;
                    setReturn_amt(c.getDouble(1));
                }
                c.close();
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
        return flag;
    }

    public boolean isInvoiceCreated(Context mContext, String invoiceno) {
        boolean flag = false;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select salesreturned,return_amt from InvoiceMaster where Retailerid="
                            + bmodel.getRetailerMasterBO().getRetailerID()
                            + " and InvoiceNo=" + invoiceno);
            if (c != null) {
                while (c.moveToNext()) {
                    flag = c.getDouble(0) == 1;
                    setReturn_amt(c.getDouble(1));
                }
                c.close();
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
        return flag;
    }

    /**
     * Load sales return transaction data into object.
     * If replacement is enbaled the replacement will also get loaded into memory.
     */
    public void loadSalesReturnData(Context mContext,String module) {
        DBUtil db = null;
        try {
            String uId ="" ;
            db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            //previously stored status fetched from DB and set to obj
            String sb = "select SI.productid,SI.batchid,SI.Condition,SI.Pqty,SI.Cqty,SI.oldmrp,SI.mfgdate,SI.expdate,SI.outerqty,Si.invoiceno," +
                    "SI.srpedited,SI.reason_type,SI.LotNumber,SI.status,SH.uid from SalesReturnDetails SI inner join SalesReturnHeader SH ON SH.uid=SI.uid " +
                    "where SH.Retailerid=" + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()) + " and SH.upload='N' and SH.RefModule = '"+module+"' and SH.distributorid=" + bmodel.getRetailerMasterBO().getDistributorId();
            Cursor c = db.selectSQL(sb);
            if (c != null && c.getCount() > 0) {
                while (c.moveToNext()) {
                    int productid = c.getInt(0);
                    String condition = c.getString(2);
                    int pqty = c.getInt(3);
                    int cqty = c.getInt(4);
                    double oldmrp = c.getDouble(5);
                    String mfgDate = c.getString(6);
                    String expDate = c.getString(7);
                    int oqty = c.getInt(8);
                    String invoiceNo = c.getString(9);
                    float srpEdited = c.getFloat(10);
                    String lotNo = c.getString(12);

                    if ("".equals(invoiceNo)) {
                        invoiceNo = "0";
                    }
                    if ("null".equals(lotNo)) {
                        lotNo = "";
                    }
                    setSalesReturnObject(productid, condition, pqty, cqty, oqty, oldmrp, mfgDate, expDate, invoiceNo, srpEdited, lotNo, c.getString(13),module);
                    Commons.print("inside sales return data load");

                    uId = c.getString(14);
                }
            }
            if (c != null) {
                c.close();
            }
            if (SHOW_STOCK_REPLACE_PCS || SHOW_STOCK_REPLACE_CASE || SHOW_STOCK_REPLACE_OUTER)
                loadSalesReplacementData(db,module,uId);
        } catch (Exception e) {
            Commons.printException(e + "");
        } finally {
            if (db != null) {
                db.closeDB();
            }
        }
    }

    private void setSalesReturnObject(int pid, String condition, int pqty, int cqty, int oqty, double oldmrp, String mfgDate, String expDate, String invoiceNo, float srpEdited, String lotNo, String status,String module) {

        ProductMasterBO productBO;
        if(module.equals("ORDER"))
            productBO = bmodel.productHelper.getProductMasterBOById(Integer.toString(pid));
        else
            productBO = bmodel.productHelper.getSalesReturnProductBOById(Integer.toString(pid));

        if (productBO != null) {
            for (SalesReturnReasonBO bo : bmodel.reasonHelper.getReasonSalesReturnMaster()) {
                if (bo.getReasonID().equals(condition)) {
                    SalesReturnReasonBO reasonBo = new SalesReturnReasonBO();
                    reasonBo.setReasonDesc(bo.getReasonDesc());
                    reasonBo.setReasonID(bo.getReasonID());
                    reasonBo.setCaseSize(productBO.getCaseSize());
                    reasonBo.setOuterSize(productBO.getOutersize());
                    reasonBo.setProductShortName(productBO.getProductShortName());
                    reasonBo.setOldMrp(productBO.getMRP());
                    reasonBo.setSrpedit(productBO.getSrp());
                    reasonBo.setPieceQty(pqty);
                    reasonBo.setCaseQty(cqty);
                    reasonBo.setOuterQty(oqty);
                    reasonBo.setOldMrp(oldmrp);
                    reasonBo.setMfgDate(mfgDate);
                    reasonBo.setExpDate(expDate);
                    reasonBo.setInvoiceno(invoiceNo);
                    reasonBo.setSrpedit(srpEdited);
                    reasonBo.setLotNumber(lotNo);
                    reasonBo.setStatus(status);
                    productBO.getSalesReturnReasonList().add(reasonBo);
                    return;
                }
            }
        }
    }

    /**
     * This will clear the sales return value from the objects.
     */
    public void clearSalesReturnTable(boolean isFromOrder) { //true -> Stock and Order --- false -> SalesReturn
        ProductMasterBO product;
        Vector<ProductMasterBO> productMaster ;

        if(isFromOrder)
            productMaster = bmodel.productHelper.getProductMaster();
        else
            productMaster = bmodel.productHelper.getSalesReturnProducts();

        int siz = productMaster.size();
        for (int i = 0; i < siz; ++i) {
            product = productMaster.get(i);

            product.setRepPieceQty(0);
            product.setRepCaseQty(0);
            product.setRepOuterQty(0);
            product.setSelectedSalesReturnPosition(0);

            if (product.getSalesReturnReasonList() != null && product.getSalesReturnReasonList().size() != 0) {
                for (SalesReturnReasonBO bo : product
                        .getSalesReturnReasonList()) {
                    if (bo.getCaseQty() > 0 || bo.getPieceQty() > 0 || bo.getOuterQty() > 0) {
                        bo.setCaseQty(0);
                        bo.setPieceQty(0);
                        bo.setOuterQty(0);
                        bo.setSrpedit(0);
                        bo.setMfgDate("");
                        bo.setExpDate("");
                        bo.setOldMrp(0);
                        bo.setLotNumber("");
                        bo.setInvoiceno("");

                    }
                }
            }
        }
    }

    // set new to add data from db
    public void removeSalesReturnTable(boolean isFromOrder) { //true -> Stock and Order --- false -> SalesReturn
        ProductMasterBO product;
        Vector<ProductMasterBO> productMaster ;
        if(isFromOrder)
            productMaster = bmodel.productHelper.getProductMaster();
        else
            productMaster = bmodel.productHelper.getSalesReturnProducts();

        int siz = productMaster.size();
        for (int i = 0; i < siz; ++i) {
            product = productMaster.get(i);
            product.setSalesReturnReasonList(new ArrayList<SalesReturnReasonBO>());
        }
    }


    public double getSalesRetunTotalValue(Context mContext) {
        double returnValue = 0;
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            String sb = "select Returnvalue from SalesReturnHeader where RetailerId=" +
                    bmodel.QT(bmodel.retailerMasterBO.getRetailerID()) + " and upload='N' and distributorid=" + bmodel.retailerMasterBO.getDistributorId();
            Cursor c = db.selectSQL(sb);
            if (c.getCount() > 0) {
                if (c.moveToFirst()) {
                    returnValue = c.getDouble(0);
                }
            }
            c.close();

        } catch (Exception e) {
            Commons.printException(e);
        } finally {
            if (db != null) {
                db.closeDB();
            }
        }
        return returnValue;
    }

    public double getOrderValue(Context mContext) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db.selectSQL("select sum(ordervalue)from "
                    + DataMembers.tbl_orderHeader + " where retailerid="
                    + bmodel.QT(bmodel.retailerMasterBO.getRetailerID()) + " and upload='N'");
            if (c != null) {
                if (c.moveToNext()) {
                    double i = c.getDouble(0);
                    c.close();
                    db.closeDB();
                    return i;
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return 0;
    }

    public double getTotalSalesReturnValue(Context mContext) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.createDataBase();
        db.openDataBase();
        double total = 0;
        Cursor c = db
                .selectSQL("select ifnull(sum(returnvalue),0) from SalesReturnHeader");
        if (c != null) {
            if (c.getCount() > 0) {
                c.moveToNext();
                total = c.getFloat(0);
            }
            c.close();
        }
        db.closeDB();

        return total;
    }

    private void saveReplacementDetails(DBUtil db, String uid,String module) {
        String clumns = "uid,returnpid,batchid,uomid,uomCount,returnQty,Retailerid,pid,price,value,qty";
        final Vector<ProductMasterBO> productMaster;
        if(module.equals("ORDER"))
            productMaster= bmodel.productHelper.getProductMaster();
        else
            productMaster= bmodel.productHelper.getSalesReturnProducts();
        StringBuffer sb;
        double totalReplacementValue = 0.0;


        for (ProductMasterBO product : productMaster) {

            if (product.getRepPieceQty() > 0 || product.getRepCaseQty() > 0 || product.getRepOuterQty() > 0) {
                int totalPcsQty = 0;
                int totalCaseQty = 0;
                int totalOuterQty = 0;
                int totalQty;
                int totalReplacementQty;
                String batchid = "0";
                double price;

                for (SalesReturnReasonBO reasonBO : product.getSalesReturnReasonList()) {
                    totalPcsQty = totalPcsQty + reasonBO.getPieceQty();
                    totalCaseQty = totalCaseQty + (reasonBO.getCaseQty() * product.getCaseSize());
                    totalOuterQty = totalOuterQty + (reasonBO.getOuterQty() * product.getOutersize());
                }
                totalQty = totalPcsQty + totalCaseQty + totalOuterQty;
                int repPcsQty = product.getRepPieceQty();
                int repCaseQty = product.getRepCaseQty() * product.getCaseSize();
                int repOuterQty = product.getRepOuterQty() * product.getOutersize();

                totalReplacementQty = product.getRepPieceQty() + repCaseQty + repOuterQty;

                if (totalQty > 0) {
                    if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION && product.getBatchwiseProductCount() > 0) {
                        if (bmodel.batchAllocationHelper.getBatchlistByProductID() != null) {
                            ArrayList<ProductMasterBO> batchList = bmodel.batchAllocationHelper.getBatchlistByProductID().get(product.getProductID());
                            if (batchList != null) {
                                int tempRepPcsQty;
                                int tempRepCaseQty;
                                int tempRepOuterQty;
                                for (ProductMasterBO batchProductBO : batchList) {
                                    batchid = batchProductBO.getBatchid();
                                    if (batchProductBO.getSIH() > repPcsQty) {
                                        tempRepPcsQty = repPcsQty;
                                        repPcsQty = 0;
                                        batchProductBO.setSIH(batchProductBO.getSIH() - tempRepPcsQty);

                                    } else {
                                        tempRepPcsQty = batchProductBO.getSIH();
                                        repPcsQty = repPcsQty - batchProductBO.getSIH();
                                        batchProductBO.setSIH(0);
                                    }

                                    if (batchProductBO.getSIH() > repCaseQty) {
                                        tempRepCaseQty = repCaseQty;
                                        batchProductBO.setSIH(batchProductBO.getSIH() - tempRepCaseQty);
                                        repCaseQty = 0;

                                    } else {
                                        tempRepCaseQty = batchProductBO.getSIH();
                                        repCaseQty = repCaseQty - batchProductBO.getSIH();
                                        batchProductBO.setSIH(0);
                                    }
                                    if (batchProductBO.getSIH() > repOuterQty) {
                                        tempRepOuterQty = repOuterQty;
                                        batchProductBO.setSIH(batchProductBO.getSIH() - tempRepCaseQty);
                                        repOuterQty = 0;

                                    } else {
                                        tempRepOuterQty = batchProductBO.getSIH();
                                        repOuterQty = totalOuterQty - batchProductBO.getSIH();
                                        batchProductBO.setSIH(0);
                                    }
                                    price = batchProductBO.getSrp();

                                    if (totalPcsQty > 0 && tempRepPcsQty > 0) {
                                        totalReplacementValue = totalReplacementValue + (tempRepPcsQty * price);
                                        sb = new StringBuffer();
                                        sb.append(uid + "," + product.getProductID() + "," + batchid);
                                        sb.append("," + product.getPcUomid() + "," + 1 + "," + totalPcsQty);
                                        sb.append("," + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()));
                                        sb.append("," + product.getProductID() + "," + price + "," + tempRepPcsQty * price + "," + tempRepPcsQty);
                                        db.insertSQL(DataMembers.tbl_SalesReturnReplacementDetails, clumns, sb.toString());
                                    }
                                    if (totalCaseQty > 0 && tempRepCaseQty > 0) {
                                        totalReplacementValue = totalReplacementValue + (tempRepCaseQty * price);
                                        sb = new StringBuffer();
                                        sb.append(uid + "," + product.getProductID() + "," + "0");
                                        sb.append("," + product.getCaseUomId() + "," + product.getCaseSize() + "," + totalCaseQty);
                                        sb.append("," + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()));
                                        sb.append("," + product.getProductID() + "," + price + "," + tempRepCaseQty * price + "," + tempRepCaseQty);
                                        db.insertSQL(DataMembers.tbl_SalesReturnReplacementDetails, clumns, sb.toString());
                                    }
                                    if (totalOuterQty > 0 && tempRepOuterQty > 0) {
                                        totalReplacementValue = totalReplacementValue + (tempRepOuterQty * price);
                                        sb = new StringBuffer();
                                        sb.append(uid + "," + product.getProductID() + "," + "0");
                                        sb.append("," + product.getOuUomid() + "," + product.getOutersize() + "," + totalOuterQty);
                                        sb.append("," + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()));
                                        sb.append("," + product.getProductID() + "," + price + "," + tempRepOuterQty * price + "," + tempRepOuterQty);
                                        db.insertSQL(DataMembers.tbl_SalesReturnReplacementDetails, clumns, sb.toString());

                                    }
                                    if (tempRepPcsQty > 0 || tempRepCaseQty > 0 || tempRepOuterQty > 0) {

                                        int totalBatchSih = tempRepPcsQty + tempRepCaseQty + tempRepOuterQty;
                                        db.updateSQL("update stockinhandmaster set qty=qty-" + totalBatchSih + " where pid=" + product.getProductID() + " and batchid=" + batchid);
                                    }
                                }
                            }
                        }
                    } else {
                        price = product.getSrp();

                        totalReplacementValue = totalReplacementValue + (totalReplacementQty * price);
                        if (totalPcsQty > 0 || product.getRepPieceQty() > 0) {
                            sb = new StringBuffer();
                            sb.append(uid + "," + product.getProductID() + "," + batchid);
                            sb.append("," + product.getPcUomid() + "," + 1 + "," + totalPcsQty);
                            sb.append("," + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()));
                            sb.append("," + product.getProductID() + "," + price + "," + product.getRepPieceQty() * price + "," + product.getRepPieceQty());
                            db.insertSQL(DataMembers.tbl_SalesReturnReplacementDetails, clumns, sb.toString());
                        }
                        if (totalCaseQty > 0 || product.getRepCaseQty() > 0) {
                            sb = new StringBuffer();
                            sb.append(uid + "," + product.getProductID() + "," + "0");
                            sb.append("," + product.getCaseUomId() + "," + product.getCaseSize() + "," + totalCaseQty);
                            sb.append("," + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()));
                            sb.append("," + product.getProductID() + "," + price + "," + product.getRepCaseQty() * product.getCaseSize() * price + "," + product.getRepCaseQty());
                            db.insertSQL(DataMembers.tbl_SalesReturnReplacementDetails, clumns, sb.toString());
                        }
                        if (totalOuterQty > 0 || product.getRepOuterQty() > 0) {

                            sb = new StringBuffer();
                            sb.append(uid + "," + product.getProductID() + "," + "0");
                            sb.append("," + product.getOuUomid() + "," + product.getOutersize() + "," + totalOuterQty);
                            sb.append("," + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()));
                            sb.append("," + product.getProductID() + "," + price + "," + product.getRepOuterQty() * product.getOutersize() * price + "," + product.getRepOuterQty());
                            db.insertSQL(DataMembers.tbl_SalesReturnReplacementDetails, clumns, sb.toString());
                        }
                    }
                }

                if (totalQty > 0) {
                    int calculateSIH = product.getSIH() - totalReplacementQty;
                    product.setSIH(calculateSIH);
                    db.updateSQL("update productmaster set sih=" + calculateSIH + " where pid=" + product.getProductID());
                    db.updateSQL("update stockinhandmaster set qty=" + calculateSIH + " where pid=" + product.getProductID() + " and batchid=" + batchid);
                }
            }
        }
        sb = new StringBuffer();
        sb.append("update salesreturnheader set ReplacedValue=").append(QT(SDUtil.format(totalReplacementValue,
                bmodel.configurationMasterHelper.PERCENT_PRECISION_COUNT, 0)));
        sb.append(" where uid=").append(getSalesReturnID());
        db.updateSQL(sb.toString());
    }

    /**
     * Method to use save CreditNote and tax inforamation for salesreturn
     * This will also update sales return table that credit note is generated
     *
     * @param db database connection
     * @return - total credit value
     */
    private double getTotalCreditNoteWithOutTAX(DBUtil db) {
        String query = "select returnvalue from SalesReturnHeader where uid=" + getSalesReturnID();
        double salesReturnvalue = 0;
        double salesReplacValue = 0;
        Cursor c = db.selectSQL(query);
        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                salesReturnvalue = c.getDouble(0);
            }
        }
        String query2 = "select replacedValue from salesReturnHeader where uid=" + getSalesReturnID();
        c = db.selectSQL(query2);
        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                salesReplacValue = c.getDouble(0);
            }
        }
        c.close();
        return salesReturnvalue - salesReplacValue;
    }

    /**
     * Method to use save CreditNote and tax inforamation for salesreturn
     *
     * @param db  db
     * @param uid uid
     */
    public void saveSalesReturnTaxAndCreditNoteDetail(DBUtil db, String uid,String module,String code) {

        String columns = "uid,Retailerid,taxRate,taxType,applyLevelId,taxValue,pid";
        setTotalValue(getTotalCreditNoteWithOutTAX(db));

        double totalTaxValue = 0;

        if (getTotalValue() > 0) {
            if (IS_APPLY_TAX_IN_SR) {
                bmodel.productHelper.taxHelper.downloadBillWiseTaxDetails();
                // Method to use Apply Tax
                final ArrayList<TaxBO> taxList = bmodel.productHelper.taxHelper.getBillTaxList();

                StringBuffer sb;
                double totalTaxRate = 0;
                double withOutTaxValue = 0;
                if (!bmodel.configurationMasterHelper.SHOW_INCLUDE_BILL_TAX) {
                    for (TaxBO taxBO : taxList) {
                        totalTaxRate = totalTaxRate + taxBO.getTaxRate();
                    }
                    withOutTaxValue = getTotalValue() + (1 + (totalTaxRate / 100));
                }
                for (TaxBO taxBO : taxList) {

                    double taxValue;
                    if (bmodel.configurationMasterHelper.SHOW_INCLUDE_BILL_TAX) {
                        taxValue = getTotalValue() * (taxBO.getTaxRate() / 100);
                    } else {
                        taxValue = withOutTaxValue * taxBO.getTaxRate() / 100;
                    }
                    totalTaxValue = totalTaxValue + taxValue;
                    sb = new StringBuffer();
                    sb.append(uid + "," + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()) + ",");
                    sb.append(taxBO.getTaxRate() + "," + taxBO.getTaxType() + "," + taxBO.getApplyLevelId() + "," + taxValue + "," + 0);

                    db.insertSQL(DataMembers.tbl_SalesReturn_tax_Details, columns, sb.toString());
                }
            }

            //Credit note will be generated only for van seller with type CREDIT from ORDER Module
            //Credit note will be generated from SalesReturn Module too
            boolean checkType=false;
            if( ((module.equals("ORDER") && code.equals(CREDIT_TYPE)) &&
                    ((bmodel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG && bmodel.getRetailerMasterBO().getIsVansales() == 1) || bmodel.configurationMasterHelper.IS_INVOICE))
                    ) {
                // from order module
                checkType = true;
            }
            else if(module.equals("")){
                //From sales return  module
                checkType=true;
            }


            if (bmodel.configurationMasterHelper.IS_CREDIT_NOTE_CREATION && checkType) {
                StringBuffer creditNoteBuffer = new StringBuffer();
                String creditNoteColumns = "id,refno,amount,retailerid,date,CreatedDate,upload,actualamount,creditnotetype";

                setCreditNoteId(QT("CR"
                        + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                        + SDUtil.now(SDUtil.DATE_TIME_ID)));

                if (bmodel.configurationMasterHelper.SHOW_CN_SEQUENCE_NO) {
                    String seqNo;
                    bmodel.insertSeqNumber("CN");
                    seqNo = bmodel.downloadSequenceNo("CN");
                    setCreditNoteId(QT(seqNo));
                }
                String modeID = bmodel.getStandardListIdAndType(
                        "SALES_RETURN",
                        "CREDIT_NOTE_TYPE");


                creditNoteBuffer.append(getCreditNoteId() + "," + uid + ",");
                creditNoteBuffer.append((getTotalValue() + totalTaxValue) + "," + bmodel.getRetailerMasterBO().getRetailerID());
                creditNoteBuffer.append("," + QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + "," + QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + ",'N'");
                creditNoteBuffer.append("," + (getTotalValue() + totalTaxValue) + "," + QT(modeID));
                db.insertSQL(DataMembers.tbl_credit_note,
                        creditNoteColumns,
                        creditNoteBuffer.toString());
                CreditNoteListBO crBo = new CreditNoteListBO();
                crBo.setId(getCreditNoteId().substring(1,
                        getCreditNoteId().length() - 1));
                crBo.setRefno(getSalesReturnID().substring(1,
                        getSalesReturnID().length() - 1));
                crBo.setAmount(getTotalValue() + totalTaxValue);
                crBo.setChecked(false);
                crBo.setRetailerId(bmodel.getRetailerMasterBO().getRetailerID());

                bmodel.collectionHelper.getCreditNoteList().add(crBo);
                db.updateSQL("UPDATE SalesReturnHeader SET IsCreditNoteApplicable = 1,credit_flag=1 WHERE uid = "
                        + getSalesReturnID());
            }
        }
    }

    /**
     * Method to load salesReturn replacement detail
     *
     * @param db connection
     */
    private void loadSalesReplacementData(DBUtil db,String module,String productId) {
        String sb = "select pid,batchid,uomid,qty from SalesReturnReplacementDetails " +
                " where Retailerid=" + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()) +
                " and upload='N' and uid = "+bmodel.QT(productId);
        Cursor c = db.selectSQL(sb);
        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                String pid = c.getString(0);
                ProductMasterBO productBO;
                if(module.equals("ORDER"))
                    productBO = bmodel.productHelper.getProductMasterBOById(pid);
                else
                    productBO = bmodel.productHelper.getSalesReturnProductBOById(pid);
                if (productBO != null) {
                    int uomid = c.getInt(2);
                    if (uomid == productBO.getPcUomid()) {
                        productBO.setRepPieceQty(c.getInt(3));
                    } else if (uomid == productBO.getCaseUomId()) {
                        productBO.setRepCaseQty(c.getInt(3));
                    } else if (uomid == productBO.getOuUomid()) {
                        productBO.setRepOuterQty(c.getInt(3));
                    }
                }
            }
        }
        c.close();
    }

    public int isCreditNoteCreated(Context mContext) {
        int flag = 0;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select credit_flag from SalesReturnHeader where RetailerID="
                            + QT(bmodel.getRetailerMasterBO().getRetailerID()));
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    flag = c.getInt(0);
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return flag;
    }

    public void clearTransaction(Context mContext) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            String sb = "select uid from SalesReturnHeader where RetailerID=" +
                    bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()) +
                    " and upload='N' and distributorid=" + bmodel.retailerMasterBO.getDistributorId();
            Cursor c = db.selectSQL(sb);
            if (c.getCount() > 0) {
                if (c.moveToFirst()) {
                    String uid = c.getString(0);
                    db.deleteSQL(DataMembers.tbl_SalesReturnHeader, "uid="
                            + DatabaseUtils.sqlEscapeString(uid), false);
                    db.deleteSQL(DataMembers.tbl_SalesReturnDetails, "uid="
                            + DatabaseUtils.sqlEscapeString(uid), false);
                    db.deleteSQL(DataMembers.tbl_SalesReturnReplacementDetails, "uid=" + DatabaseUtils.sqlEscapeString(uid), false);
                }
            }

            // Clearing sales return object
            clearSalesReturnTable(false);

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public List<ProductMasterBO> updateReplaceQtyWithOutTakingOrder(List<ProductMasterBO> orderList) {
        final Vector<ProductMasterBO> productMasterList = bmodel.productHelper.getSalesReturnProducts();
        if (orderList != null && !orderList.isEmpty() && productMasterList != null) {
            for (ProductMasterBO productMasterBO : productMasterList) {
                if (productMasterBO.getOrderedPcsQty() == 0 && productMasterBO.getOrderedCaseQty() == 0 && productMasterBO.getOrderedOuterQty() == 0) {
                    if (productMasterBO.getRepPieceQty() > 0 || productMasterBO.getRepCaseQty() > 0 || productMasterBO.getRepOuterQty() > 0) {
                        orderList.add(productMasterBO);
                    }
                }

            }
        }
        return orderList;

    }
}
