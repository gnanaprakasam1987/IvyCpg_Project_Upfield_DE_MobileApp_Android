package com.ivy.cpg.view.van.damagestock;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class DamageStockHelper {

    private BusinessModel bmodel;
    private static DamageStockHelper instance = null;
    private List<SalesReturnReportBO> damagedSalesReport;

    public DamageStockHelper(Context context) {
        this.bmodel = (BusinessModel) context;

    }

    public static DamageStockHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DamageStockHelper(context);
        }
        return instance;
    }


    public void loadDamagedProductReport(Context mContext) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            damagedSalesReport = new ArrayList<>();
            Cursor c = db
                    .selectSQL("SELECT DISTINCT PM.PName,SRD.batchid, IFNULL(BM.batchNUM,''),RM.ListName,SRD.Pqty," +
                            " SRD.Cqty, SRD.outerQty, SRD.oldmrp, SRD.mfgdate, SRD.expdate, SRH.ReturnValue, SRH.Lpc,SRD.ProductID," +
                            " PM.dUomQty,PM.dUomId,PM.dOuomQty,PM.dOuomid,PM.sih,PM.psname FROM SalesReturnDetails SRD" +
                            " INNER JOIN SalesReturnHeader SRH ON SRD.uid = SRH.uid and SRH.upload!='X' " +
                            " INNER JOIN StandardListMaster RM ON SRD.Condition = RM.ListId" +
                            " AND SRD.reason_type=1 AND SRH.unload=0 AND RM.ParentId = (SELECT ListId FROM StandardListMaster WHERE ListType ='REASON_TYPE' AND ListCode = 'SR')" +
                            " LEFT JOIN BatchMaster BM on SRD.ProductID = BM.pid AND SRD.batchid = BM.batchid " +
                            " INNER JOIN ProductMaster PM on SRD.ProductID = PM.PID  " +
                            " GROUP BY RM.ListName, PM.PID ORDER BY RM.ListName, PM.PName");
            if (c != null && c.getCount() > 0) {
                while (c.moveToNext()) {
                    SalesReturnReportBO salBO = new SalesReturnReportBO();
                    salBO.setProductName(c.getString(0));
                    salBO.setBatchId(c.getInt(1));
                    salBO.setBatchNumber(c.getString(2));
                    salBO.setReasonDesc(c.getString(3));
                    salBO.setPieceQty(c.getInt(4));
                    salBO.setCaseQty(c.getInt(5));
                    salBO.setOuterQty(c.getInt(6));
                    salBO.setProductid(c.getString(12));
                    salBO.setdUomQty(c.getInt(13));
                    salBO.setdUomId(c.getInt(14));
                    salBO.setdOuomQty(c.getInt(15));
                    salBO.setdOuomid(c.getInt(16));
                    salBO.setSih(c.getInt(17));
                    salBO.setProductSortName(c.getString(18));
                    damagedSalesReport.add(salBO);
                }
            }
            if (c != null) {
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public List<SalesReturnReportBO> getDamagedSalesReport() {
        return damagedSalesReport;
    }


    public void UnloadDamageStock(Context mContext, ArrayList<SalesReturnReportBO> damageList) {
        try {
            SalesReturnReportBO bo;
            String uid = bmodel.userMasterHelper.getUserMasterBO().getUserid()
                    + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            String columns = "uid,pid,pname,batchid,batchno,sih,caseqty,pcsqty,outerqty,duomqty,dUomId,douomqty,dOuomid,date,type";
            for (int i = 0; i < damageList.size(); i++) {
                bo = damageList.get(i);
                String values = StringUtils.QT(uid)
                        + ","
                        + StringUtils.QT(bo.getProductid())
                        + ","
                        + StringUtils.QT(bo.getProductName())
                        + ","
                        + bo.getBatchId()
                        + ","
                        + StringUtils.QT(bo.getBatchNumber())
                        + ","
                        + bo.getSih()
                        + ","
                        + bo.getCaseQty()
                        + ","
                        + bo.getPieceQty()
                        + ","
                        + bo.getOuterQty()
                        + ","
                        + bo.getdUomQty()
                        + ","
                        + bo.getdUomId()
                        + ","
                        + bo.getdOuomQty()
                        + ","
                        + bo.getdOuomid()
                        + ","
                        + StringUtils.QT(bmodel.userMasterHelper.getUserMasterBO()
                        .getDownloadDate()) + "," + 0;
                db.insertSQL(DataMembers.tbl_vanunload_details, columns, values);
                db.executeQ("update SalesReturnHeader set unload=1 where upload!='X'");
            }

            db.closeDB();
        } catch (SQLException e) {
            Commons.printException("" + e);
        }
    }
}
