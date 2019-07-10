package com.ivy.ui.reports.currentreport.data;


import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.LoadManagementBO;
import com.ivy.sd.png.bo.StockReportBO;
import com.ivy.sd.png.provider.ProductHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.StringUtils;

import java.util.Vector;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

public class CurrentReportManagerImpl implements CurrentReportManager {

     private Vector<StockReportBO> currentStock;

    public Observable<Vector<StockReportBO>> downloadCurrentStockReport(final ProductHelper productHelper) {

        return Observable.create(new ObservableOnSubscribe<Vector<StockReportBO>>() {
            @Override
            public void subscribe(final ObservableEmitter<Vector<StockReportBO>> subscriber) throws Exception {


                try {
                    Vector<LoadManagementBO> item = productHelper.downloadLoadMgmtProductsWithFiveLevel("MENU_LOAD_MANAGEMENT", "");
                    if (item != null) {
                        currentStock = new Vector<>();
                        for (LoadManagementBO load : item) {

                            StockReportBO temp = new StockReportBO();
                            temp.setProductName(load.getProductname());
                            temp.setProductShortName(load.getProductshortname());
                            temp.setProductCode(load.getpCode());
                            temp.setProductID(load.getProductid() + "");
                            temp.setSih(load.getSih());
                            temp.setBrandId(load.getParentid());
                            temp.setCategoryId(0);
                            temp.setBarcode(load.getBarcode());
                            temp.setRfield1(load.getRField1());
                            temp.setCaseSize(load.getCaseSize());
                            temp.setOuterSize(load.getOuterSize());
                            temp.setPiece_uomid(load.getPiece_uomid());
                            temp.setdUomid(load.getdUomid());
                            temp.setdOuomid(load.getdOuonid());
                            currentStock.add(temp);
                        }
                        setCurrentStock(currentStock);
                        subscriber.onNext(currentStock);
                    }

                } catch (Exception e) {
                    subscriber.onError(new Throwable(e.getMessage()));
                    Commons.printException(e);
                }
                subscriber.onComplete();
            }

        });

    }


    public void updateBaseUOM(Context mContext, String activity, int reportType) {
        //reportType(1)-EOD, reportType(2)-currentStock, reportType(3)-CurrentStockBatchwise
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        db.createDataBase();
        db.openDataBase();
        try {

            int contentLevel = 0;
            int contentLevelId = 0;
            Cursor c = db.selectSQL("SELECT MAX(Sequence),levelId FROM ProductLevel");
            if (c != null) {
                if (c.moveToNext()) {
                    contentLevel = c.getInt(0);
                    contentLevelId = c.getInt(1);
                }
            }

            c = db.selectSQL("SELECT productId, productLevelId, uomid from ActivityGroupMapping AGM" +
                    " INNER JOIN ActivityGroupProductMapping APM ON APM.groupid=AGM.groupid" +
                    " where AGM.activity=" + StringUtils.getStringQueryParam(activity));
            if (c != null) {
                if (c.getCount() > 0) {
                    initializeUOMmapping(reportType);
                    while (c.moveToNext()) {
                        updateProductMapping(mContext, c.getString(0), c.getInt(1), c.getInt(2), contentLevel, contentLevelId, reportType);
                    }
                } else {
                    enableUOMForAllProducts(reportType);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            db.closeDB();
            Commons.printException("" + e);
        }
    }


    private void initializeUOMmapping(int reportType) {
        if (reportType == 2) {
            for (StockReportBO bo : getCurrentStock()) {
                bo.setBaseUomCaseWise(false);
                bo.setBaseUomOuterWise(false);
                bo.setBaseUomPieceWise(false);
            }
        }
    }


    private void enableUOMForAllProducts(int reportType) {
        if (reportType == 2) {
            for (StockReportBO bo : getCurrentStock()) {
                bo.setBaseUomCaseWise(true);
                bo.setBaseUomOuterWise(true);
                bo.setBaseUomPieceWise(true);
            }
        }
    }


    private void updateProductMapping(Context mContext, String productId, int pLevelId, int uomId, int contentLevel, int contentLevelId, int reportType) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        db.createDataBase();
        db.openDataBase();

        try {

            if (contentLevelId == pLevelId) {
                if (reportType == 2) {
                    for (StockReportBO bo : getCurrentStock()) {
                        if (bo.getProductID().equals(productId)) {
                            if (bo.getPiece_uomid() == uomId)
                                bo.setBaseUomPieceWise(true);
                            else if (bo.getdUomid() == uomId)
                                bo.setBaseUomCaseWise(true);
                            else if (bo.getdOuomid() == uomId)
                                bo.setBaseUomOuterWise(true);
                        }
                    }
                }

            } else {

                int parentLevel = 0;

                Cursor c = db.selectSQL("SELECT Sequence FROM ProductLevel where levelId=" + pLevelId);
                if (c != null) {
                    if (c.moveToNext())
                        parentLevel = c.getInt(0);
                }

                int loopEnd = contentLevel - parentLevel + 1;

                StringBuilder sb = new StringBuilder();
                sb.append("select distinct PM" + loopEnd + ".pid from productmaster PM1 ");
                for (int i = 2; i <= loopEnd; i++) {
                    sb.append(" INNER JOIN ProductMaster PM" + i + " ON PM" + i
                            + ".ParentId = PM" + (i - 1) + ".PID");
                }
                sb.append(" where PM1.pid=" + productId);

                c = db.selectSQL(sb.toString());
                if (c.getCount() > 0) {
                    while (c.moveToNext()) {

                        if (reportType == 2) {
                            for (StockReportBO bo : getCurrentStock()) {
                                if (bo.getProductID().equals(c.getString(0))) {
                                    if (bo.getPiece_uomid() == uomId)
                                        bo.setBaseUomPieceWise(true);
                                    else if (bo.getdUomid() == uomId)
                                        bo.setBaseUomCaseWise(true);
                                    else if (bo.getdOuomid() == uomId)
                                        bo.setBaseUomOuterWise(true);
                                }
                            }
                        }

                    }

                }

                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            db.closeDB();
            Commons.printException("" + e);
        }
    }

    public Vector<StockReportBO> getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(Vector<StockReportBO> currentStock) {
        this.currentStock = currentStock;
    }
}
