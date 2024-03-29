package com.ivy.cpg.view.reports.distorderreport;

import android.content.Context;
import android.database.Cursor;

import com.ivy.cpg.primarysale.bo.DistributorMasterBO;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * Created by Hanifa on 31/7/18.
 */

public class DistOrderReportHelper {
    private static DistOrderReportHelper instance = null;

    private DistOrderReportHelper() {
    }

    public static DistOrderReportHelper getInstance() {
        if (instance == null) {
            instance = new DistOrderReportHelper();
        }
        return instance;
    }

    /**
     * Load distributor Order Report
     *
     * @return reportdistordbooking array list
     */
    public Observable<ArrayList<DistOrderReportBo>> downloadDistributorOrderReport1(final Context context) {
        return Observable.create(new ObservableOnSubscribe<ArrayList<DistOrderReportBo>>() {
            @Override
            public void subscribe(ObservableEmitter<ArrayList<DistOrderReportBo>> subscribe) throws Exception {

                DBUtil db = null;
                ArrayList<DistOrderReportBo> reportdistordbooking = null;
                try {
                    DistOrderReportBo orderreport;
                    db = new DBUtil(context, DataMembers.DB_NAME
                    );
                    db.openDataBase();
                    StringBuilder sb = new StringBuilder();
                    sb.append("select DOH.uid,DOH.distid,DM.DName,DOH.TotalValue,DOH.LPC,DOH.upload From DistOrderHeader DOH");
                    sb.append(" Inner join DistributorMaster DM on DOH.distid = DM.Did ");
                    sb.append("inner join DistOrderDetails DOD on DOH.Uid=DOD.Uid Group by DOH.uid,DM.DName");
                    Cursor c = db.selectSQL(sb.toString());
                    reportdistordbooking = new ArrayList<>();
                    if (c != null) {
                        while (c.moveToNext()) {
                            orderreport = new DistOrderReportBo();
                            orderreport.setOrderId(c.getString(0));
                            orderreport.setDistributorId(c.getInt(1));
                            orderreport.setDistributorName(c.getString(2));
                            orderreport.setOrderTotal(c.getDouble(3));
                            orderreport.setLpc(c.getString(4));

                            orderreport.setUpload(c.getString(5));
                            reportdistordbooking.add(orderreport);
                        }
                        c.close();
                    }
                    subscribe.onNext(reportdistordbooking);
                    subscribe.onComplete();
                } catch (Exception e) {
                    Commons.printException(e + "");
                    subscribe.onError(e);
                    subscribe.onComplete();
                } finally {
                    if (db != null)
                        db.closeDB();
                }
            }
        });
    }

    public Observable<ArrayList<DistOrderReportBo>> downloadDistributorOrderReport(final Context context) {
        return Observable.fromCallable(new Callable<ArrayList<DistOrderReportBo>>() {
            @Override
            public ArrayList<DistOrderReportBo> call() throws Exception {
                DBUtil db = null;
                ArrayList<DistOrderReportBo> reportdistordbooking = null;
                try {
                    DistOrderReportBo orderreport;
                    db = new DBUtil(context, DataMembers.DB_NAME
                    );
                    db.openDataBase();
                    StringBuilder sb = new StringBuilder();
                    sb.append("select DOH.uid,DOH.distid,DM.DName,DOH.TotalValue,DOH.LPC,DOH.upload From DistOrderHeader DOH");
                    sb.append(" Inner join DistributorMaster DM on DOH.distid = DM.Did ");
                    sb.append("inner join DistOrderDetails DOD on DOH.Uid=DOD.Uid Group by DOH.uid,DM.DName");
                    Cursor c = db.selectSQL(sb.toString());
                    reportdistordbooking = new ArrayList<>();
                    if (c != null) {
                        while (c.moveToNext()) {
                            orderreport = new DistOrderReportBo();
                            orderreport.setOrderId(c.getString(0));
                            orderreport.setDistributorId(c.getInt(1));
                            orderreport.setDistributorName(c.getString(2));
                            orderreport.setOrderTotal(c.getDouble(3));
                            orderreport.setLpc(c.getString(4));

                            orderreport.setUpload(c.getString(5));
                            reportdistordbooking.add(orderreport);
                        }
                        c.close();
                    }
                } catch (Exception e) {
                    Commons.printException(e + "");

                } finally {
                    if (db != null)
                        db.closeDB();
                }
                return reportdistordbooking;
            }
        });
    }


    public Observable<Double> getorderbookingCount(final String tablName, final Context context) {
        return Observable.fromCallable(new Callable<Double>() {
            @Override
            public Double call() throws Exception {
                double tot = 0;
                try {
                    DBUtil db = new DBUtil(context, DataMembers.DB_NAME
                    );
                    db.createDataBase();
                    db.openDataBase();
                    Cursor c = db.selectSQL("select count (distinct retailerid) from "
                            + tablName);
                    if (c != null) {
                        if (c.moveToNext()) {
                            tot = c.getInt(0);
                        }
                        c.close();
                    }
                    db.closeDB();
                } catch (Exception e) {
                    Commons.printException(e + "");
                }
                return tot;
            }
        });
    }


    public Observable<Double> getavglinesfororderbooking(final String tableName, final Context context) {
        return Observable.empty().fromCallable(new Callable<Double>() {
            @Override
            public Double call() throws Exception {
                double tot = 0;
                try {
                    DBUtil db = new DBUtil(context, DataMembers.DB_NAME
                    );
                    db.createDataBase();
                    db.openDataBase();
                    Cursor c = db.selectSQL("select sum(linespercall) from "
                            + tableName);
                    if (c != null) {
                        if (c.moveToNext()) {
                            tot = c.getInt(0);
                        }
                        c.close();
                    }
                    db.closeDB();
                } catch (Exception e) {
                    Commons.printException(e + "");
                }
                return tot;
            }
        });
    }


    public Observable<ArrayList<DistOrderReportBo>> distOrderReportDetail(final Context context, final String orderId) {
        return Observable.create(new ObservableOnSubscribe<ArrayList<DistOrderReportBo>>() {
            @Override
            public void subscribe(ObservableEmitter<ArrayList<DistOrderReportBo>> subscriber) throws Exception {
                BusinessModel businessModel = (BusinessModel) context.getApplicationContext();
                ArrayList<DistOrderReportBo> distributorOrderList = null;
                DBUtil db = null;
                try {
                    db = new DBUtil(context, DataMembers.DB_NAME
                    );
                    db.openDataBase();
                    StringBuilder sb = new StringBuilder();
                    sb.append("select PM.pname,PM.psname,DOD.pid,DOD.qty,DOD.uomid,DOD.uomcount,DOD.batchid,BM.batchNum,DOD.LineValue from DistOrderDetails ");
                    sb.append("DOD inner join ProductMaster PM on PM.pid=DOD.pid Left Join BatchMaster BM on DOD.pid=BM.pid and DOD.batchid=BM.batchid");
                    sb.append(" where  DOD.uid=" + businessModel.QT(orderId));

                    Cursor c = db.selectSQL(sb.toString());
                    if (c != null) {
                        distributorOrderList = new ArrayList<>();
                        while (c.moveToNext()) {
                            String pName = c.getString(0);
                            String pSname = c.getString(1);
                            String pid = c.getString(2);
                            int qty = c.getInt(3);
                            int uomid = c.getInt(4);
                            int uomCount = c.getInt(5);
                            int batchid = c.getInt(6);
                            String batchNum = c.getString(7);
                            float totalValue = c.getFloat(8);
                            setDistOrderReportDetails(pName, pSname, pid, qty, uomid, uomCount, batchid, batchNum, totalValue, distributorOrderList, businessModel);

                        }
                    }

                    subscriber.onNext(distributorOrderList);
                    subscriber.onComplete();
                } catch (Exception e) {
                    Commons.printException(e + " ");
                    subscriber.onError(e);
                    subscriber.onComplete();
                } finally {
                    if (db != null)
                        db.closeDB();
                }


            }
        });
    }


    private void setDistOrderReportDetails(String pname, String pSname, String pid, int qty, int uomid, int uomCount, int batchid, String batchnum, float totalValue, ArrayList<DistOrderReportBo> distReportList, BusinessModel businessModel) {
        DistOrderReportBo orderReportBO;

        if (distReportList == null) {
            distReportList = new ArrayList<>();

        }
        ProductMasterBO productBo = businessModel.productHelper.getProductMasterBOById(pid);
        if (productBo != null) {
            int size = distReportList.size();
            if (size > 0) {
                orderReportBO = distReportList.get(size - 1);
                if (pid.equals(orderReportBO.getProductId()) &&
                        batchid == orderReportBO.getBatchId()) {
                    if (uomid == productBo.getPcUomid()) {
                        orderReportBO.setPQty(qty);

                    } else if (uomid == productBo.getCaseUomId()) {
                        orderReportBO.setCQty(qty);

                    } else if (uomCount == productBo.getOuUomid()) {
                        orderReportBO.setOuterOrderedCaseQty(qty);
                    }
                    orderReportBO.setTot(orderReportBO.getTot() + totalValue);
                    distReportList.remove(size - 1);
                    distReportList.add(size - 1, orderReportBO);


                } else {
                    orderReportBO = new DistOrderReportBo();
                    orderReportBO.setProductName(pname);
                    orderReportBO.setProductShortName(pSname);
                    orderReportBO.setProductId(pid);
                    orderReportBO.setBatchId(batchid);
                    orderReportBO.setBatchNo(batchnum);
                    if (uomid == productBo.getPcUomid()) {
                        orderReportBO.setPQty(qty);

                    } else if (uomid == productBo.getCaseUomId()) {
                        orderReportBO.setCQty(qty);

                    } else if (uomCount == productBo.getOuUomid()) {
                        orderReportBO.setOuterOrderedCaseQty(qty);
                    }
                    orderReportBO.setTot(totalValue);
                    distReportList.add(orderReportBO);


                }

            } else {
                orderReportBO = new DistOrderReportBo();
                orderReportBO.setProductName(pname);
                orderReportBO.setProductShortName(pSname);
                orderReportBO.setProductId(pid);
                orderReportBO.setBatchId(batchid);
                orderReportBO.setBatchNo(batchnum);
                if (uomid == productBo.getPcUomid()) {
                    orderReportBO.setPQty(qty);

                } else if (uomid == productBo.getCaseUomId()) {
                    orderReportBO.setCQty(qty);

                } else if (uomCount == productBo.getOuUomid()) {
                    orderReportBO.setOuterOrderedCaseQty(qty);
                }
                orderReportBO.setTot(totalValue);
                distReportList.add(orderReportBO);
            }


        }


    }

    public void updateDistributor(String did, Context context) {
        BusinessModel businessModel = (BusinessModel) context.getApplicationContext();
        ArrayList<DistributorMasterBO> distributorList = businessModel.distributorMasterHelper.getDistributors();
        for (DistributorMasterBO distributorMasterBO : distributorList) {
            if (did.equals(distributorMasterBO.getDId())) {
                businessModel.distributorMasterHelper.setDistributor(distributorMasterBO);
                break;
            }
        }

    }

}
