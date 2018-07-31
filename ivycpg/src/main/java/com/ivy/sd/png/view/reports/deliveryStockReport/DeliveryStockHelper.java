package com.ivy.sd.png.view.reports.deliveryStockReport;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ivyuser on 24/7/18.
 */

public class DeliveryStockHelper {
    public static DeliveryStockHelper instance;
    private BusinessModel bmodel;

    protected DeliveryStockHelper(Context context) {
        bmodel = (BusinessModel) context;
    }

    public static DeliveryStockHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DeliveryStockHelper(context);
        }
        return instance;
    }

    HashMap<String, DeliveryStockBo> mDeliveryProductsBObyId;

    public ArrayList<DeliveryStockBo> downloadDeliveryStock(Context context) {

        DBUtil db = null;
        ArrayList<DeliveryStockBo> mDeliveryStocks = new ArrayList<>();
        try {
            String retailerIds = "";
            for (RetailerMasterBO retailer : bmodel.getRetailerMaster()) {
                if (retailer.getIsToday() == 1 || retailer.getIsDeviated().equalsIgnoreCase("Y")) {
                    if (retailerIds.length() > 1)
                        retailerIds += "," + retailer.getRetailerID();
                    else
                        retailerIds += retailer.getRetailerID();
                }
            }
            db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();

            String sql = "select productid,pm.pname,pm.psname,PM.piece_uomid,PM.duomid,Pm.dOuomid"
                    + ",uomid as orderedUomId,qty as orderedQty from InvoiceDetailUOMWise ID"
                    + " Left join ProductMaster pm on pm.pid=ID.productid"
                    + " where invoiceid in(select invoiceno from invoicemaster where retailerid in (" + retailerIds + "))";

            Cursor c = db.selectSQL(sql);
            if (c.getCount() > 0) {
                mDeliveryProductsBObyId = new HashMap<>();
                DeliveryStockBo bo;
                while (c.moveToNext()) {
                    if (mDeliveryProductsBObyId.get(c.getString(0)) != null) {
                        DeliveryStockBo deliveryStockBo = mDeliveryProductsBObyId.get(c.getString(0));
                        if (deliveryStockBo.getPcUomid() == c.getInt(6))
                            deliveryStockBo.setOrderedPcsQty((deliveryStockBo.getOrderedPcsQty() + c.getInt(7)));
                        if (deliveryStockBo.getCaseUomId() == c.getInt(6))
                            deliveryStockBo.setOrderedCaseQty((deliveryStockBo.getOrderedCaseQty() + c.getInt(7)));
                        if (deliveryStockBo.getOuUomid() == c.getInt(6))
                            deliveryStockBo.setOrderedOuterQty((deliveryStockBo.getOrderedOuterQty() + c.getInt(7)));
                    } else {
                        bo = new DeliveryStockBo();
                        bo.setProductID(c.getString(0));
                        bo.setProductName(c.getString(1));
                        bo.setProductShortName(c.getString(2));

                        bo.setPcUomid(c.getInt(3));
                        bo.setCaseUomId(c.getInt(4));
                        bo.setOuUomid(c.getInt(5));

                        if (bo.getPcUomid() == c.getInt(6))
                            bo.setOrderedPcsQty(c.getInt(7));
                        if (bo.getCaseUomId() == c.getInt(6))
                            bo.setOrderedCaseQty(c.getInt(7));
                        if (bo.getOuUomid() == c.getInt(6))
                            bo.setOrderedOuterQty(c.getInt(7));

                        mDeliveryProductsBObyId.put(bo.getProductID(), bo);
                        mDeliveryStocks.add(bo);
                    }

                }
            }
        } catch (Exception ex) {
            Commons.printException(ex);
        } finally {
            mDeliveryProductsBObyId = null;
            db.closeDB();
        }

        return mDeliveryStocks;
    }
}
