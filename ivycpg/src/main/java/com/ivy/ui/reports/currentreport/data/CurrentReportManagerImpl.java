package com.ivy.ui.reports.currentreport.data;


import android.content.Context;
import android.database.Cursor;
import android.util.SparseArray;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.LoadManagementBO;
import com.ivy.sd.png.bo.StockReportBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.AppUtils;

import java.util.HashMap;
import java.util.Vector;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

public class CurrentReportManagerImpl implements CurrentReportManager {


    public Observable<Vector<StockReportBO>> downloadCurrentStockReport(final Context context, final BusinessModel businessModel) {

        return Observable.create(new ObservableOnSubscribe<Vector<StockReportBO>>() {
            @Override
            public void subscribe(final ObservableEmitter<Vector<StockReportBO>> subscriber) throws Exception {

                Vector<StockReportBO> currentStock;
                try {
                    Vector<LoadManagementBO> item = loadProducts("MENU_LOAD_MANAGEMENT", "", context, businessModel);
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


    public Vector<LoadManagementBO> loadProducts(String moduleCode,
                                                 String batchmenucode, Context mContext, BusinessModel businessModel) {
        SparseArray<LoadManagementBO> mLoadManagementBOByProductId = new SparseArray<>();
        String sql, sql1, sql2, sql3;
        Vector<LoadManagementBO> productList = new Vector<>();
        LoadManagementBO bo;
       // Vector<LoadManagementBO> batchno;
        Vector<LoadManagementBO> list;
        LoadManagementBO batchnobo;
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.openDataBase();

        int mParentLevel = 0;
        int mChildLevel = 0;
        int mContentLevel = 0;

        Cursor filterCur = db
                .selectSQL("SELECT IFNULL(PL1.Sequence,0),IFNULL(PL2.Sequence,0), IFNULL(PL3.Sequence,0)"
                        + " FROM ConfigActivityFilter CF"
                        + " LEFT JOIN ProductLevel PL1 ON PL1.LevelId = CF.ProductFilter1"
                        + " LEFT JOIN ProductLevel PL2 ON PL2.LevelId = CF.ProductFilter2"
                        + " LEFT JOIN ProductLevel PL3 ON PL3.LevelId = CF.ProductContent"
                        + " WHERE CF.ActivityCode = " + AppUtils.QT(moduleCode));

        if (filterCur != null) {
            if (filterCur.moveToNext()) {
                mParentLevel = filterCur.getInt(0);
                mChildLevel = filterCur.getInt(1);
                mContentLevel = filterCur.getInt(2);
            }
            filterCur.close();
        }
        if (batchmenucode.equals("MENU_VAN_UNLOAD")
                || batchmenucode.equals("MENU_CUR_STK_BATCH")
                || batchmenucode.equals("MENU_STOCK_ADJUSTMENT")) {
            sql = "  LEFT JOIN StockInHandMaster SIH ON SIH.pid=PM.PID"
                    + " LEFT JOIN BatchMaster BM ON (SIH.batchid = BM.batchid and PM.PID=BM.pid)";
            sql1 = ",IFNULL(BM.batchNum,'') as batchNum,SIH.qty as qty,SIH.adjusted_qty,SIH.batchid";
        } else {
            sql = "";
            sql1 = "";
        }
        if (batchmenucode.equals("MENU_STOCK_PROPOSAL")) {
            sql2 = " LEFT JOIN StockProposalMaster A ON A.pid = PM.PID";
            sql3 = " ,A.qty, A.pcsQty, A.caseQty, A.outerQty";
        } else {
            sql2 = "";
            sql3 = "";
        }
        String query;
        if (mParentLevel == 0 && mChildLevel == 0) {
            query = "SELECT  PM.ParentId, PM.PID, PM.PName,"
                    + " (select qty from StockProposalNorm PSQ  where uomid =PM.piece_uomid and PM.PID = PSQ.PID) as sugpcs, "
                    + " PM.psname, PM.dUomQty,"
                    + " PM.sih, PWHS.Qty, PM.IsAlloc, PM.mrp, PM.barcode, PM.RField1, PM.dOuomQty,"
                    + " PM.isMust, PM.maxQty,(select qty from ProductStandardStockMaster PSM  where uomid =PM.piece_uomid and PM.PID = PSM.PID) as stdpcs,(select qty from ProductStandardStockMaster PSM where uomid =PM.dUomId and PM.PID = PSM.PID) as stdcase,(select qty from ProductStandardStockMaster PSM where uomid =PM.dOuomid and PM.PID = PSM.PID) as stdouter, PM.dUomId, PM.dOuomid,"
                    + " PM.baseprice, PM.piece_uomid, PM.PLid, PM.pCode, PM.msqQty, PM.issalable" //+ ",(CASE WHEN PWHS.PID=PM.PID then 'true' else 'false' end) as IsAvailWareHouse "
                    + sql3
                    + sql1
                    + " ,(select qty from StockProposalNorm PSQ  where uomid =PM.dUomId and PM.PID = PSQ.PID) as sugcs,"
                    + " (select qty from StockProposalNorm PSQ  where uomid =PM.dOuomid and PM.PID = PSQ.PID) as sugou "
                    + " FROM ProductMaster PM"
                    + " LEFT JOIN ProductWareHouseStockMaster PWHS ON PWHS.pid=PM.pid and PWHS.UomID=PM.piece_uomid and (PWHS.DistributorId=" + businessModel.getRetailerMasterBO().getDistributorId() + " OR PWHS.DistributorId=0)"
                    + sql2
                    + sql
                    + " WHERE PM.PLid IN"
                    + " (SELECT ProductContent FROM ConfigActivityFilter WHERE ActivityCode = "
                    + AppUtils.QT(moduleCode) + ")";
        } else {

            int loopEnd;
            String parentFilter;
            if (mChildLevel != 0) {
                loopEnd = mContentLevel - mChildLevel + 1;
                parentFilter = "ProductFilter2";
            } else {
                loopEnd = mContentLevel - mParentLevel + 1;
                parentFilter = "ProductFilter1";
            }
            query = "select PM1.PID, PM"
                    + loopEnd
                    + ".PID, PM"
                    + loopEnd
                    + ".PName, (select qty from StockProposalNorm PSQ where uomid =PM"
                    + loopEnd
                    + ".piece_uomid and PSQ.PID =PM"
                    + loopEnd
                    + ".PID) as"
                    + " sugpcs, PM"
                    + loopEnd
                    + ".psname, PM"
                    + loopEnd
                    + ".dUomQty, PM"
                    + loopEnd
                    + ".sih, PWHS.Qty,PM"
                    + loopEnd
                    + ".IsAlloc, PM"
                    + loopEnd
                    + ".mrp, PM"
                    + loopEnd
                    + ".barcode, PM"
                    + loopEnd
                    + ".RField1,"
                    + " PM"
                    + loopEnd
                    + ".dOuomQty, PM"
                    + loopEnd
                    + ".isMust, PM"
                    + loopEnd
                    + ".maxQty,(select qty from ProductStandardStockMaster PSM where uomid =PM"
                    + loopEnd
                    + ".piece_uomid and PSM.PID =PM"
                    + loopEnd
                    + ".PID) as"
                    + " stdpcs,(select qty from ProductStandardStockMaster PSM where uomid =PM"
                    + loopEnd
                    + ".dUomId and PSM.PID =PM"
                    + loopEnd
                    + ".PID) as"
                    + " stdcase, (select qty from ProductStandardStockMaster PSM where uomid =PM"
                    + loopEnd + ".dOuomid and PSM.PID =PM"
                    + loopEnd
                    + ".PID) as stdouter, PM" + loopEnd
                    + ".dUomId, PM" + loopEnd + ".dOuomid," + " PM" + loopEnd
                    + ".baseprice, PM" + loopEnd + ".piece_uomid, PM" + loopEnd
                    + ".PLid, PM" + loopEnd + ".pCode," + " PM" + loopEnd
                    + ".msqQty, PM" + loopEnd + ".issalable" /*+ ",(CASE WHEN PWHS.PID=PM" + loopEnd + ".PID then 'true' else 'false' end) as IsAvailWareHouse " */ + sql3 + sql1
                    + " ,PM"
                    + loopEnd
                    + ".PName, (select qty from StockProposalNorm PSQ where uomid =PM"
                    + loopEnd
                    + ".dUomId and PSQ.PID =PM"
                    + loopEnd
                    + ".PID) as"
                    + " sugcs,PM"
                    + loopEnd
                    + ".PName, (select qty from StockProposalNorm PSQ where uomid =PM"
                    + loopEnd
                    + ".dOuomid and PSQ.PID =PM"
                    + loopEnd
                    + ".PID) as"
                    + " sugou "
                    + " FROM ProductMaster PM1";
            for (int i = 2; i <= loopEnd; i++)
                query = query + " INNER JOIN ProductMaster PM" + i + " ON PM"
                        + i + ".ParentId = PM" + (i - 1) + ".PID";
            query = query + " LEFT JOIN ProductWareHouseStockMaster PWHS ON PWHS.pid=PM" + loopEnd + ".pid and PWHS.UomID=PM" + loopEnd + ".piece_uomid and (PWHS.DistributorId=" + businessModel.getRetailerMasterBO().getDistributorId() + " OR PWHS.DistributorId=0)";
            if (batchmenucode.equals("MENU_VAN_UNLOAD")
                    || batchmenucode.equals("MENU_CUR_STK_BATCH")
                    || batchmenucode.equals("MENU_STOCK_ADJUSTMENT")) {
                sql = " LEFT JOIN StockInHandMaster SIH ON SIH.pid = PM" + loopEnd
                        + ".PID"
                        + " LEFT JOIN BatchMaster BM ON (SIH.batchid = BM .batchid AND BM.pid = PM" +
                        +loopEnd + ".PID)";
            } else {
                sql = "";
            }
            if (batchmenucode.equals("MENU_STOCK_PROPOSAL")) {
                sql2 = " LEFT JOIN StockProposalMaster A ON A.pid = PM" + loopEnd
                        + ".PID";
            } else {
                sql2 = "";
            }

            query = query + sql2 + sql + " WHERE PM1.PLid IN (SELECT "
                    + parentFilter + " FROM ConfigActivityFilter"
                    + " WHERE ActivityCode = " + AppUtils.QT(moduleCode) + ")"
                    + " ORDER BY PM" + loopEnd + ".rowid";
        }
        Cursor c = db.selectSQL(query);
        if (c != null) {
            while (c.moveToNext()) {
                //batchno = new Vector<>();
                bo = new LoadManagementBO();
                bo.setParentid(c.getInt(0));
                bo.setProductid(c.getInt(1));
                bo.setProductname(c.getString(2));
                bo.setSuggestqty(c.getInt(c.getColumnIndex("sugpcs")) +
                        (c.getInt(c.getColumnIndex("sugcs")) * c.getInt(5)) +
                        (c.getInt(c.getColumnIndex("sugou")) * c.getInt(12)));
                bo.setProductshortname(c.getString(4));
                bo.setCaseSize(c.getInt(5));
                bo.setSih(c.getInt(6));
                bo.setWsih(c.getInt(7));
                bo.setIsalloc(c.getInt(8));
                bo.setMrp(c.getDouble(9));
                bo.setBarcode(c.getString(10));
                bo.setRField1(c.getString(11));
                bo.setOuterSize(c.getInt(12));
                bo.setIsMust(c.getInt(13));
                bo.setMaxQty(c.getInt(14));
                bo.setStdpcs(c.getInt(15));
                bo.setStdcase(c.getInt(16));
                bo.setStdouter(c.getInt(17));
                bo.setdUomid(c.getInt(18));
                bo.setdOuonid(c.getInt(19));
                bo.setBaseprice(c.getDouble(20));
                bo.setPiece_uomid(c.getInt(21));
                bo.setPLid(c.getInt(22));
                bo.setpCode(c.getString(23));
                bo.setMsqQty(c.getInt(24));
                bo.setIssalable(c.getInt(25));
                if (batchmenucode.equals("MENU_STOCK_PROPOSAL")) {
                    bo.setStkprototalQty(c.getInt(c.getColumnIndex("qty")));
                    bo.setStkpropcsqty(c.getInt(c.getColumnIndex("pcsQty")));
                    bo.setStkprocaseqty(c.getInt(c.getColumnIndex("caseQty")));
                    bo.setStkproouterqty(c.getInt(c.getColumnIndex("outerQty")));
                }
                if (batchmenucode.equals("MENU_VAN_UNLOAD")
                        || batchmenucode.equals("MENU_CUR_STK_BATCH")
                        || batchmenucode.equals("MENU_STOCK_ADJUSTMENT")) {
                    bo.setBatchNo(c.getString(c.getColumnIndex("batchNum")));
                    bo.setStocksih(c.getInt(c.getColumnIndex("qty")));
                    bo.setOld_diff_sih(c.getInt(c
                            .getColumnIndex("adjusted_qty")));
                    bo.setBatchId(c.getString(c.getColumnIndex("batchid")));
                }
                if (batchmenucode.equals("MENU_MANUAL_VAN_LOAD")) {

                    list = new Vector<>();
                    LoadManagementBO ret;

                    for (int i = 0; i < 3; ++i) {
                        ret = new LoadManagementBO();
                        ret.setProductid(c.getInt(0));
                        ret.setCaseqty(0);
                        ret.setPieceqty(0);
                        ret.setOuterQty(0);
                        ret.setBatchNo("");
                        list.add(ret);
                    }

                    bo.setBatchlist(list);
                }
                mLoadManagementBOByProductId.put(bo.getProductid(), bo);
                productList.add(bo);

            }

            c.close();
        }

        if (batchmenucode.equals("MENU_MANUAL_VAN_LOAD")) {
            Cursor c1 = db
                    .selectSQL("select batchNum, batchid, Pid from BatchMaster order by Pid");

            if (c1 != null) {
                int temp = -1;
                Vector<LoadManagementBO> batchno1 = null;
                HashMap<Integer, Vector<LoadManagementBO>> prodBatchList = new HashMap<>();
                while (c1.moveToNext()) {

                    if (temp != c1.getInt(2)) {
                        prodBatchList.put(temp, batchno1);
                        batchno1 = new Vector<>();
                        temp = c1.getInt(2);
                    }

                    batchnobo = new LoadManagementBO();
                    batchnobo.setBatchNo(c1.getString(0));
                    batchnobo.setBatchId(c1.getString(1));
                    if (batchno1 != null)
                        batchno1.add(batchnobo);
                }
                c1.close();

                temp = productList.size();
                for (int i = 0; i < temp; i++) {
                    LoadManagementBO load = productList.get(i);
                    if (prodBatchList.get(load.getProductid()) != null) {
                        int s1 = load.getBatchlist().size();
                        for (int j = 0; j < s1; j++) {
                            LoadManagementBO ret = load.getBatchlist().get(j);
                            ret.setBatchnolist(cloneVanLoadList(prodBatchList.get(load.getProductid())));
                        }
                    }

                }
            }
        }


        db.closeDB();
        return productList;
    }

    private Vector<LoadManagementBO> cloneVanLoadList(
            Vector<LoadManagementBO> list) {

        Vector<LoadManagementBO> clone = new Vector<>(
                list.size());
        for (LoadManagementBO item : list)
            clone.add(new LoadManagementBO(item));
        return clone;

    }


}
