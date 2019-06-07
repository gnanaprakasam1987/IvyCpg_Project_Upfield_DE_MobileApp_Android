package com.ivy.cpg.view.reports.inventoryreport;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ProductTaggingHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;

import java.util.ArrayList;

/**
 * Created by abbas.a on 18/07/18.
 */

public class InventoryReportHelper {

    private Context mContext;
    private BusinessModel bmodel;

    public InventoryReportHelper(Context context, BusinessModel bmodel){
        this.mContext=context;
        this.bmodel=bmodel;
    }

    public ArrayList<InventoryBO_Proj> downloadInventoryReport(int retailerId, String type) {
        ArrayList<InventoryBO_Proj> lst = new ArrayList<>();
        try {
            bmodel.setRetailerMasterBO(bmodel.getRetailerBoByRetailerID().get(retailerId + ""));

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.openDataBase();
            int mContentLevelId = bmodel.productHelper.getContentLevel(db, "MENU_STK_ORD");

            String focusBrandIds = "";
            if (type.equalsIgnoreCase("Filt11"))
                focusBrandIds = ProductTaggingHelper.getInstance(mContext).getTaggedProductIds(mContext,"FCBND", mContentLevelId);
            else if (type.equals("Filt12"))
                focusBrandIds = ProductTaggingHelper.getInstance(mContext).getTaggedProductIds(mContext,"FCBND2", mContentLevelId);

            if(db.isDbNullOrClosed())
                db.openDataBase();

            String s = "select distinct " +
                    "CD.productid,Shelfpqty,Shelfcqty,Shelfoqty,SM.listname,PM.psname from ClosingStockDetail CD " +
                    "inner join ClosingStockHeader CH ON CD.stockid=CH.stockid " +
                    "LEFT JOIN StandardListMaster SM ON SM.listid=CD.reasonid " +
                    "LEFT JOIN Productmaster PM ON PM.pid=CD.productid " +
                    "where CH.retailerid=" + retailerId + " and CH.date=" + bmodel.QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL))
                    + " and CD.productid in(" + focusBrandIds + ")";

            Cursor c = db.selectSQL(s);
            if (c != null) {
                InventoryBO_Proj bo;
                while (c.moveToNext()) {
                    bo = new InventoryBO_Proj();
                    bo.setProductId(c.getString(0));
                    if (c.getInt(1) > 0 || c.getInt(2) > 0 || c.getInt(3) > 0) {
                        bo.setAvailability("Y");
                        bo.setReasonDesc("");
                    } else {
                        bo.setAvailability("N");
                        bo.setReasonDesc(c.getString(4));
                    }
                    bo.setProductName(c.getString(5));

                    lst.add(bo);

                }
                c.close();
            }
            db.closeDB();
        } catch (Exception ex) {
            Commons.printException(ex);
        }

        return lst;
    }

}
