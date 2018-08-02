package com.ivy.cpg.view.reports.deliveryStockReport;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.HashMap;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * Created by Hanifa on 24/7/18.
 */

public class DeliveryStockHelper {
    public static DeliveryStockHelper instance;
    private BusinessModel bmodel;

    protected DeliveryStockHelper() {
    }

    public static DeliveryStockHelper getInstance() {
        if (instance == null) {
            instance = new DeliveryStockHelper();
        }
        return instance;
    }

    HashMap<String, DeliveryStockBo> mDeliveryProductsBObyId;


    public Observable<ArrayList<DeliveryStockBo>> downloadDeliveryStock(final Context context){
        return Observable.create(new ObservableOnSubscribe<ArrayList<DeliveryStockBo>>() {
            @Override
            public void subscribe(ObservableEmitter<ArrayList<DeliveryStockBo>> subscriber) throws Exception {
                DBUtil db = null;

                try {
                    BusinessModel businessModel = (BusinessModel) context.getApplicationContext();
                    ArrayList<DeliveryStockBo> mDeliveryStocks = new ArrayList<>();
                    db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME, DataMembers.DB_PATH);
                    String retailerIds = "";
                    for (RetailerMasterBO retailer : businessModel.getRetailerMaster()) {
                        if (retailer.getIsToday() == 1 || retailer.getIsDeviated().equalsIgnoreCase("Y")) {
                            if (retailerIds.length() > 1)
                                retailerIds += "," + retailer.getRetailerID();
                            else
                                retailerIds += retailer.getRetailerID();
                        }
                    }

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
                    subscriber.onNext(mDeliveryStocks);
                    subscriber.onComplete();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    subscriber.onError(ex);
                    subscriber.onComplete();
                } finally {
                    mDeliveryProductsBObyId = null;
                    db.closeDB();
                }
            }
        });
    }
}
