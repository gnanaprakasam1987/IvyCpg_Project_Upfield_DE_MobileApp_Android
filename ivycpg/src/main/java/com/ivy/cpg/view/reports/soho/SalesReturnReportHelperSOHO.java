package com.ivy.cpg.view.reports.soho;

import android.content.Context;
import android.database.Cursor;

import com.ivy.cpg.view.salesreturn.SalesReturnReasonBO;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.StandardListMasterConstants;

import java.util.ArrayList;

/**
 * Created by abbas.a on 24/07/18.
 */

public class SalesReturnReportHelperSOHO {

    private Context mContext;

    public SalesReturnReportHelperSOHO(Context context){
        mContext=context;
    }

    public ArrayList<SalesReturnReasonBO> getSalesReturnRetailerList() {
        ArrayList<SalesReturnReasonBO> salesReturnReasonBOs = new ArrayList<>();
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select distinct srd.ProductId as ProductId,srd.retailerId as RetailerID," +
                            "pm.pname as ProductName, pm.pcode as ProductCode from SalesReturnDetails srd inner join " +
                            "ProductMaster pm ON srd.ProductID = pm.pid where srd.upload!='X' and srd.status = 2");
            if (c != null) {
                while (c.moveToNext()) {
                    SalesReturnReasonBO reasonBO = new SalesReturnReasonBO();
                    reasonBO.setRetailerId(c.getString(c.getColumnIndex("RetailerID")));
                    reasonBO.setProductId(c.getString(c.getColumnIndex("ProductId")));
                    reasonBO.setProductName(c.getString(c.getColumnIndex("ProductName")));
                    reasonBO.setProductCode(c.getString(c.getColumnIndex("ProductCode")));
                    salesReturnReasonBOs.add(reasonBO);
                }
                c.close();
            }
            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);
        }
        return salesReturnReasonBOs;
    }

    public ArrayList<SalesReturnReasonBO> getSalesReturnList(String productId, String retailerId) {
        ArrayList<SalesReturnReasonBO> salesReturnReasonBOs = new ArrayList<>();
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT distinct A.ListName as reasonDesc,srd .* from SalesReturnDetails srd"
                            + " inner join StandardListMaster A INNER JOIN StandardListMaster B ON"
                            + " A.ParentId = B.ListId AND"
                            + " ( B.ListCode = '" + StandardListMasterConstants.SALES_RETURN_NONSALABLE_REASON_TYPE
                            + "' OR B.ListCode = '" + StandardListMasterConstants.SALES_RETURN_SALABLE_REASON_TYPE + "')"
                            + " AND A.listId = srd.condition WHERE srd.upload!='X' and A.ListType = 'REASON' AND"
                            + " srd.ProductId = '" + productId + "' AND srd.RetailerId = '" + retailerId
                            + "' AND srd.status = 2");
            if (c != null) {
                while (c.moveToNext()) {
                    SalesReturnReasonBO reasonBO = new SalesReturnReasonBO();
                    reasonBO.setInvoiceno(c.getString(c.getColumnIndex("invoiceno")));
                    reasonBO.setLotNumber(c.getString(c.getColumnIndex("LotNumber")));
                    reasonBO.setSrpedit(c.getFloat(c.getColumnIndex("srpedited")));
                    reasonBO.setPieceQty(c.getInt(c.getColumnIndex("totalQty")));
                    reasonBO.setOldMrp(c.getInt(c.getColumnIndex("oldmrp")));
                    reasonBO.setReasonDesc(c.getString(c.getColumnIndex("reasonDesc")));
                    salesReturnReasonBOs.add(reasonBO);
                }
                c.close();
            }
            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);
        }
        return salesReturnReasonBOs;
    }
}
