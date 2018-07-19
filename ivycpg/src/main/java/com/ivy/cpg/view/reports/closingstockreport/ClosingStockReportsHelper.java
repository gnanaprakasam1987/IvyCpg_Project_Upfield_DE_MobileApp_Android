package com.ivy.cpg.view.reports.closingstockreport;

import android.content.Context;
import android.database.Cursor;

import com.ivy.cpg.view.reports.promotion.RetailerNamesBO;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by abbas.a on 18/07/18.
 */

public class ClosingStockReportsHelper {

    private Context mContext;


    public ClosingStockReportsHelper(Context context) {
        mContext = context;
    }

    public ArrayList<RetailerNamesBO> downloadClosingStockRetailers() {

        ArrayList<RetailerNamesBO> retailerMaster = new ArrayList<>();

        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.openDataBase();
        try {
            retailerMaster = new ArrayList<>();

            RetailerNamesBO temp;

            Cursor cursor = db.selectSQL("select RM.retailerid,RM.RetailerName from ClosingStockDetail SD " +
                    " INNER JOIN RetailerMaster RM ON RM.RetailerID = SD.RetailerID group by RM.RetailerID");

            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    temp = new RetailerNamesBO();
                    temp.setRetailerId(SDUtil.convertToInt(cursor.getString(0)));
                    temp.setRetailerName(cursor.getString(1));
                    retailerMaster.add(temp);
                }
                cursor.close();
            }

            db.closeDB();
        } catch (Exception e) {
            db.closeDB();
            Commons.printException(e);
        }
        return retailerMaster;
    }

    public HashMap<String, ArrayList<ProductMasterBO>> downloadClosingStock() {

        HashMap<String, ArrayList<ProductMasterBO>> closingStkReportByRetailId = new HashMap<>();

        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.openDataBase();

        try {

            ArrayList<ProductMasterBO> productMasterBOs;

            Cursor cursor = db.selectSQL("select PM.PName,SH.retailerid,productId,Sum(shelfpqty),Sum(shelfcqty)," +
                    "Sum(shelfoqty),Facing,PM.pCode,RM.RetailerName,SD.uomqty,SD.ouomqty from ClosingStockDetail SD " +
                    "INNER JOIN ClosingStockHeader SH ON SD.stockId=SH.stockId " +
                    "INNER JOIN ProductMaster PM ON PM.PID = SD.ProductID " +
                    "INNER JOIN RetailerMaster RM ON RM.RetailerID = SH.RetailerID " +
                    "group by SH.RetailerID,productId");

            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    ProductMasterBO temp = new ProductMasterBO();
                    temp.setProductName(cursor.getString(0));
                    temp.setProductID(cursor.getString(2));
                    temp.setCsCase(cursor.getInt(4));
                    temp.setCsPiece(cursor.getInt(3));
                    temp.setCsOuter(cursor.getInt(5));
                    temp.setProductCode(cursor.getString(7));
                    temp.setCaseSize(cursor.getInt(9));
                    temp.setOutersize(cursor.getInt(10));

                    if (closingStkReportByRetailId.get(cursor.getString(1)) != null) {
                        ArrayList<ProductMasterBO> productMasterBO1 = closingStkReportByRetailId.get(cursor.getString(1));
                        productMasterBO1.add(temp);

                    } else {
                        productMasterBOs = new ArrayList<>();
                        productMasterBOs.add(temp);
                        closingStkReportByRetailId.put(cursor.getString(1), productMasterBOs);
                    }
                }
                cursor.close();
            }

            db.closeDB();
        } catch (Exception e) {
            db.closeDB();
            Commons.printException(e);
        }
        return closingStkReportByRetailId;
    }
}
