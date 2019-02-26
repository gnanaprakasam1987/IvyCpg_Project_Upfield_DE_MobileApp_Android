package com.ivy.cpg.view.reports.deliveryStockReport;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.model.BusinessModel;
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
    private static DeliveryStockHelper instance;

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
                    db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME);
                    String retailerIds = "";
                    for (RetailerMasterBO retailer : businessModel.getRetailerMaster()) {
                        if (retailer.getIsToday() == 1 || (retailer.getIsDeviated() != null && retailer.getIsDeviated().equalsIgnoreCase("Y"))) {
                            if (retailerIds.length() > 1)
                                retailerIds += "," + retailer.getRetailerID();
                            else
                                retailerIds += retailer.getRetailerID();
                        }
                    }

                    db.openDataBase();

                    String sql = "select productid,PM.pname,PM.psname,PM.piece_uomid as pieceUomID,PM.duomid as caseUomId,Pm.dOuomid as outerUomId"
                            + ",uomid as orderedUomId,qty as orderedQty,PM.dUomQty as caseSize,PM.dOuomQty as outerSize from InvoiceDetailUOMWise ID"
                            + " inner join InvoiceDeliveryMaster IDM on IDM.InvoiceNo=ID.invoiceid"
                            + " Left join ProductMaster PM on PM.pid=ID.productid"
                            + " where IDM.Retailerid in (" + retailerIds + ") and IDM.InvoiceNo not in (select invoiceid from VanDeliveryHeader)";

                    Cursor c = db.selectSQL(sql);
                    if (c.getCount() > 0) {
                        int pcsQty = 0, csQtyinPieces = 0, ouQtyinPieces = 0;
                        mDeliveryProductsBObyId = new HashMap<>();
                        DeliveryStockBo bo;
                        while (c.moveToNext()) {
                            if (mDeliveryProductsBObyId.get(c.getString(0)) != null) {

                                bo = mDeliveryProductsBObyId.get(c.getString(0));

                            } else {
                                pcsQty = 0;
                                csQtyinPieces = 0;
                                ouQtyinPieces = 0;
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

                            if (c.getInt(c.getColumnIndex("orderedUomId")) == c.getInt(c.getColumnIndex("pieceUomID"))) {
                                pcsQty = (mDeliveryProductsBObyId.get(c.getString(0)) == null) ? c.getInt(7) : (pcsQty + c.getInt(7));
                                bo.setPcUomid(c.getInt(2));
                            } else if (c.getInt(c.getColumnIndex("orderedUomId")) == c.getInt(c.getColumnIndex("caseUomId"))) {
                                csQtyinPieces = (mDeliveryProductsBObyId.get(c.getString(0)) == null) ? (c.getInt(7) * bo.getCaseSize()) :
                                        (csQtyinPieces + (c.getInt(7) * bo.getCaseSize()));
                                bo.setCaseUomId(c.getInt(2));
                            } else if (c.getInt(c.getColumnIndex("orderedUomId")) == c.getInt(c.getColumnIndex("outerUomId"))) {
                                ouQtyinPieces = (mDeliveryProductsBObyId.get(c.getString(0)) == null) ? (c.getInt(7) * bo.getOuterSize()) :
                                        (ouQtyinPieces + (c.getInt(7) * bo.getOuterSize()));
                                bo.setOuUomid(c.getInt(2));
                            }


                            bo.setCaseSize(c.getInt(c.getColumnIndex("caseSize")));
                            bo.setOuterSize(c.getInt(c.getColumnIndex("outerSize")));


                            int totalqty = pcsQty + csQtyinPieces + ouQtyinPieces;
                            int caseQty = 0;
                            if (businessModel.configurationMasterHelper.SHOW_ORDER_CASE) {
                                caseQty = bo.getCaseSize() != 0 ? totalqty / bo.getCaseSize() : totalqty;
                            }
                            int QtyRemaining = totalqty - (caseQty * bo.getCaseSize());

                            int outerQty = 0;
                            if (businessModel.configurationMasterHelper.SHOW_OUTER_CASE) {
                                outerQty = bo.getOuterSize() != 0 ? QtyRemaining / bo.getOuterSize() : QtyRemaining;
                            }
                            int pieceQty = QtyRemaining - (outerQty * bo.getOuterSize());

                            bo.setOrderedPcsQty(pieceQty);
                            bo.setOrderedCaseQty(bo.getCaseSize() != 0 ? caseQty : 0);
                            bo.setOrderedOuterQty(bo.getOuterSize() != 0 ? outerQty : 0);
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
