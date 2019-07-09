package com.ivy.cpg.view.reports.damageReturn;

import android.content.Context;
import android.database.Cursor;

import com.ivy.cpg.view.reports.deliveryStockReport.DeliveryStockBo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * Created by ivyuser on 2/8/18.
 */

public class DamageReturenReportHelper {
    private static DamageReturenReportHelper instance = null;

    private DamageReturenReportHelper() {
    }

    public static DamageReturenReportHelper getInstance() {
        if (instance == null) {
            instance = new DamageReturenReportHelper();
        }
        return instance;
    }

    public ArrayList<PendingDeliveryBO> pandingDeliveryBOS;

    public ArrayList<PendingDeliveryBO> getPandingDeliveryBOS() {
        return pandingDeliveryBOS;
    }

    public void setPandingDeliveryBOS(ArrayList<PendingDeliveryBO> pandingDeliveryBOS) {
        this.pandingDeliveryBOS = pandingDeliveryBOS;
    }


    public Observable<ArrayList<PendingDeliveryBO>> downloadPendingDeliveryReport(final Context context) {
        return Observable.create(new ObservableOnSubscribe<ArrayList<PendingDeliveryBO>>() {
            @Override
            public void subscribe(ObservableEmitter<ArrayList<PendingDeliveryBO>> subscribe) throws Exception {
                PendingDeliveryBO pandingDeliveryBO;
                ArrayList<PendingDeliveryBO> pandingDeliveryBOS = new ArrayList<>();
                DBUtil db = null;
                try {
                    db = new DBUtil(context, DataMembers.DB_NAME
                    );
                    db.openDataBase();
                    Cursor c = db
                            .selectSQL("select distinct InvoiceNo,InvoiceDate,invNetamount,RM.RetailerName,ifnull(vh.status,'') as status,InvoiceRefNo from InvoiceDeliveryMaster idm inner join RetailerMaster RM  on RM.RetailerID=idm.Retailerid left join VanDeliveryHeader  vh on vh.invoiceid=idm.InvoiceNo  group by idm.InvoiceNo");
                    if (c.getCount() > 0) {
                        while (c.moveToNext()) {
                            pandingDeliveryBO = new PendingDeliveryBO();
                            pandingDeliveryBO.setInvoiceNo(c.getString(0));
                            pandingDeliveryBO.setInvoiceDate(c.getString(1));
                            pandingDeliveryBO.setInvNetamount(c.getString(2));
                            pandingDeliveryBO.setRetailerName(c.getString(3));
                            pandingDeliveryBO.setStatus(c.getString(4));
                            pandingDeliveryBO.setInvoiceRefNo(c.getString(5));
                            pandingDeliveryBOS.add(pandingDeliveryBO);
                        }
                        c.close();
                    }
                    subscribe.onNext(pandingDeliveryBOS);
                    subscribe.onComplete();
                } catch (Exception e) {
                    Commons.printException(e);
                    subscribe.onError(e);
                    subscribe.onComplete();
                } finally {
                    if (db != null)
                        db.closeDB();
                }
            }
        });
    }

    HashMap<String, DeliveryStockBo> mDeliveryProductsBObyId;

    public Observable<ArrayList<DeliveryStockBo>> downloadDeliveryStockDetails(final Context context
            , final String invoiceid, final String status) {
        return Observable.create(new ObservableOnSubscribe<ArrayList<DeliveryStockBo>>() {
            @Override
            public void subscribe(ObservableEmitter<ArrayList<DeliveryStockBo>> subscriber) throws Exception {
                DBUtil db = null;

                try {
                    String sql;
                    BusinessModel businessModel = (BusinessModel) context.getApplicationContext();
                    ArrayList<DeliveryStockBo> mDeliveryStocks = new ArrayList<>();
                    db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME);

                    db.openDataBase();

                    if (StringUtils.isNullOrEmpty(status)) {
                        sql = "select productid,pm.pname,pm.psname,PM.piece_uomid as pieceUomID,PM.duomid as caseUomId,Pm.dOuomid as outerUomId,uomid as orderedUomId,qty as orderedQty,PM.dUomQty as caseSize,PM.dOuomQty as outerSize " +
                                "from InvoiceDetailUOMWise ID Left join ProductMaster pm on pm.pid=ID.productid " +
                                "where invoiceid in(select InvoiceNo from InvoiceDeliveryMaster " +
                                "where InvoiceNo in (" + invoiceid + ")) and  " +
                                "invoiceid not in(select InvoiceId from VanDeliveryHeader where InvoiceId in (" + invoiceid + "))";
                    } else if ("p".equalsIgnoreCase(status)) {
                        sql = "select ID.Pid,pm.pname,pm.psname,PM.piece_uomid as pieceUomID,PM.duomid as caseUomId,Pm.dOuomid as outerUomId,uomid as orderedUomId,Deliveredqty as orderedQty,PM.dUomQty as caseSize,PM.dOuomQty as outerSize from VanDeliveryDetail ID " +
                                " inner join VanDeliveryHeader vdh on vdh.uid=ID.uid Left join ProductMaster pm on pm.pid=ID.Pid " +
                                " where invoiceid in (" + invoiceid + ")";
                    } else {
                        sql = "select productid,pm.pname,pm.psname,PM.piece_uomid as pieceUomID,PM.duomid as caseUomId,Pm.dOuomid as outerUomId,uomid as orderedUomId,qty as orderedQty,PM.dUomQty as caseSize,PM.dOuomQty as outerSize " +
                                "from InvoiceDetailUOMWise ID Left join ProductMaster pm on pm.pid=ID.productid " +
                                "where invoiceid in(select InvoiceNo from InvoiceDeliveryMaster " +
                                "where InvoiceNo in (" + invoiceid + ")) and  " +
                                "invoiceid  in(select InvoiceId from VanDeliveryHeader where InvoiceId in (" + invoiceid + "))";
                    }

                    Cursor c = db.selectSQL(sql);
                    if (c.getCount() > 0) {
                        mDeliveryProductsBObyId = new HashMap<>();
                        DeliveryStockBo bo;
                        int pcsQty = 0, csQtyinPieces = 0, ouQtyinPieces = 0;
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
