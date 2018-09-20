package com.ivy.cpg.view.reports.damageReturn;

import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;

import com.ivy.cpg.view.reports.deliveryStockReport.DeliveryStockBo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.AppUtils;

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

    public ArrayList<PandingDeliveryBO> pandingDeliveryBOS;

    public ArrayList<PandingDeliveryBO> getPandingDeliveryBOS() {
        return pandingDeliveryBOS;
    }

    public void setPandingDeliveryBOS(ArrayList<PandingDeliveryBO> pandingDeliveryBOS) {
        this.pandingDeliveryBOS = pandingDeliveryBOS;
    }


    public Observable<ArrayList<PandingDeliveryBO>> downloadPendingDeliveryReport(final Context context) {
        return Observable.create(new ObservableOnSubscribe<ArrayList<PandingDeliveryBO>>() {
            @Override
            public void subscribe(ObservableEmitter<ArrayList<PandingDeliveryBO>> subscribe) throws Exception {
                PandingDeliveryBO pandingDeliveryBO;
                ArrayList<PandingDeliveryBO> pandingDeliveryBOS = new ArrayList<>();
                DBUtil db = null;
                try {
                    db = new DBUtil(context, DataMembers.DB_NAME,
                            DataMembers.DB_PATH);
                    db.openDataBase();
                    Cursor c = db
                            .selectSQL("select distinct InvoiceNo,InvoiceDate,invNetamount,RM.RetailerName,ifnull(vh.status,'') as status from InvoiceDeliveryMaster idm inner join RetailerMaster RM  on RM.RetailerID=idm.Retailerid left join VanDeliveryHeader  vh on vh.invoiceid=idm.InvoiceNo  group by idm.InvoiceNo");
                    if (c.getCount() > 0) {
                        while (c.moveToNext()) {
                            pandingDeliveryBO = new PandingDeliveryBO();
                            pandingDeliveryBO.setInvoiceNo(c.getString(0));
                            pandingDeliveryBO.setInvoiceDate(c.getString(1));
                            pandingDeliveryBO.setInvNetamount(c.getString(2));
                            pandingDeliveryBO.setRetailerName(c.getString(3));
                            pandingDeliveryBO.setStatus(c.getString(4));
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
            , final String invoiceid, final String status){
        return Observable.create(new ObservableOnSubscribe<ArrayList<DeliveryStockBo>>() {
            @Override
            public void subscribe(ObservableEmitter<ArrayList<DeliveryStockBo>> subscriber) throws Exception {
                DBUtil db = null;

                try {
                    String sql;
                    ArrayList<DeliveryStockBo> mDeliveryStocks = new ArrayList<>();
                    db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME, DataMembers.DB_PATH);

                    db.openDataBase();

                    if(AppUtils.isEmptyString(status)){
                        sql ="select productid,pm.pname,pm.psname,PM.piece_uomid,PM.duomid,Pm.dOuomid,uomid as orderedUomId,qty as orderedQty " +
                                "from InvoiceDetailUOMWise ID Left join ProductMaster pm on pm.pid=ID.productid " +
                                "where invoiceid in(select InvoiceNo from InvoiceDeliveryMaster " +
                                "where InvoiceNo in ("+invoiceid+")) and  " +
                                "invoiceid not in(select InvoiceId from VanDeliveryHeader where InvoiceId in ("+invoiceid+"))";
                    }else if("p".equalsIgnoreCase(status)){
                        sql="select ID.Pid,pm.pname,pm.psname,PM.piece_uomid,PM.duomid,Pm.dOuomid,uomid as orderedUomId,Deliveredqty as orderedQty from VanDeliveryDetail ID inner join VanDeliveryHeader vdh on vdh.uid=ID.uid Left join ProductMaster pm on pm.pid=ID.Pid";
                    }else{
                        sql ="select productid,pm.pname,pm.psname,PM.piece_uomid,PM.duomid,Pm.dOuomid,uomid as orderedUomId,qty as orderedQty " +
                                "from InvoiceDetailUOMWise ID Left join ProductMaster pm on pm.pid=ID.productid " +
                                "where invoiceid in(select InvoiceNo from InvoiceDeliveryMaster " +
                                "where InvoiceNo in ("+invoiceid+")) and  " +
                                "invoiceid  in(select InvoiceId from VanDeliveryHeader where InvoiceId in ("+invoiceid+"))";
                    }

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
