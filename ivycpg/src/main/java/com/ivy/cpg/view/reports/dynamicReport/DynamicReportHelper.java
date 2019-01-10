package com.ivy.cpg.view.reports.dynamicReport;

import android.content.Context;
import android.database.Cursor;
import android.util.SparseArray;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.SpinnerBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * Created by karthikeyan.a on 3/16/2016.
 */
public class DynamicReportHelper {

    private Context context;
    private BusinessModel bmodel;
    private static DynamicReportHelper instance = null;
    private List<DynamicReportHeaderBO> dynamicReportHeaderBOs;
    private DynamicReportDetailBO dynamicReportDetailBO;
    private String CODE_RETAILER = "RETAILER";
    private String CODE_USER = "USER";
    private boolean rep_retailer; // to check is retailer type
    private ArrayList<SpinnerBO> retailerList;

    protected DynamicReportHelper(Context context) {
        this.context = context;
        bmodel = (BusinessModel) context;
    }

    public static DynamicReportHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DynamicReportHelper(context);
        }
        return instance;
    }

    public void downloadDynamicReport(String menucode) {

        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        db.openDataBase();

        dynamicReportHeaderBOs = new ArrayList<DynamicReportHeaderBO>();
        dynamicReportDetailBO = new DynamicReportDetailBO();

        try {
            String sql = "Select * from DynamicReportHeader where MenuCode = " + bmodel.QT(menucode);

            Cursor c = db.selectSQL(sql);

            if (c != null) {
                while (c.moveToNext()) {
                    DynamicReportHeaderBO reportHeaderBO = new DynamicReportHeaderBO();
                    reportHeaderBO.setReportId(c.getInt(0));
                    reportHeaderBO.setMenuCode(c.getString(1));
                    reportHeaderBO.setReportType(c.getString(2));
                    reportHeaderBO.setColumnId(c.getInt(3));
                    reportHeaderBO.setColumnName(c.getString(4));
                    reportHeaderBO.setLength(c.getInt(5));
                    reportHeaderBO.setColumnAlignment(c.getString(6));
                    dynamicReportHeaderBOs.add(reportHeaderBO);
                }
            }

            c.close();
            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);
        }

        SparseArray<SparseArray<String>> tempArray = new SparseArray<SparseArray<String>>();
        TreeSet<Integer> rowSet = new TreeSet<>();

        try {
            db.openDataBase();
            int EntityId = 0;
            if (dynamicReportHeaderBOs.size() > 0) {
                if (dynamicReportHeaderBOs.get(0).getReportType().equalsIgnoreCase(CODE_RETAILER)) {
                    setRep_retailer(true);
                    downloadRetailerList(dynamicReportHeaderBOs.get(0).getReportId());
                }

                if (dynamicReportHeaderBOs.get(0).getReportType().equalsIgnoreCase(CODE_USER)) {
                    EntityId = bmodel.userMasterHelper.getUserMasterBO().getUserid();
                    setRep_retailer(false);
                }
            }

            String sql = "Select ColumnId , Value , RowId from DynamicReportDetail where ReportId = " + dynamicReportHeaderBOs.get(0).getReportId() + " AND EntityId =" + EntityId;
            Cursor c = db.selectSQL(sql);
            if (c != null) {

                while (c.moveToNext()) {
                    int id = c.getInt(0);
                    int recordId = c.getInt(2);

                    SparseArray<String> tempList = tempArray.get(id);
                    if (tempList == null)
                        tempList = new SparseArray<String>();

                    tempList.put(recordId, c.getString(1));
                    tempArray.put(id, tempList);
                    rowSet.add(recordId);
                }
            }

            c.close();
            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);
        }


        dynamicReportDetailBO.setDetailsSparseArray(tempArray);
        dynamicReportDetailBO.setRecordSet(rowSet);

    }

    private void downloadRetailerList(int reportId) {
        retailerList = new ArrayList<SpinnerBO>();
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        db.openDataBase();

        try {
            String sql = "Select distinct DR.EntityId,RM.RetailerName from DynamicReportDetail DR "
                    + "inner join RetailerMaster RM on RM.RetailerID = DR.EntityId "
                    + "where ReportId = " + reportId;

            Cursor c = db.selectSQL(sql);

            if (c != null) {
                while (c.moveToNext()) {
                    retailerList.add(new SpinnerBO(c.getInt(0), c.getString(1)));
                }
            }

            c.close();
            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public void downloadRetailerReport(int RetailerId) {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );

        dynamicReportDetailBO = new DynamicReportDetailBO();

        SparseArray<SparseArray<String>> tempArray = new SparseArray<SparseArray<String>>();
        TreeSet<Integer> rowSet = new TreeSet<>();

        try {
            db.openDataBase();

            String sql = "Select ColumnId , Value , RowId from DynamicReportDetail where ReportId = " + dynamicReportHeaderBOs.get(0).getReportId() + " AND EntityId =" + RetailerId;
            Cursor c = db.selectSQL(sql);
            if (c != null) {

                while (c.moveToNext()) {
                    int id = c.getInt(0);
                    int recordId = c.getInt(2);

                    SparseArray<String> tempList = tempArray.get(id);
                    if (tempList == null)
                        tempList = new SparseArray<String>();

                    tempList.put(recordId, c.getString(1));
                    tempArray.put(id, tempList);
                    rowSet.add(recordId);
                }
            }

            c.close();
            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);
        }

        dynamicReportDetailBO.setDetailsSparseArray(tempArray);
        dynamicReportDetailBO.setRecordSet(rowSet);
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public BusinessModel getBmodel() {
        return bmodel;
    }

    public void setBmodel(BusinessModel bmodel) {
        this.bmodel = bmodel;
    }

    public static DynamicReportHelper getInstance() {
        return instance;
    }

    public static void setInstance(DynamicReportHelper instance) {
        DynamicReportHelper.instance = instance;
    }

    public List<DynamicReportHeaderBO> getDynamicReportHeaderBOs() {
        return dynamicReportHeaderBOs;
    }


    public DynamicReportDetailBO getDynamicReportDetailBO() {
        return dynamicReportDetailBO;
    }

    public ArrayList<SpinnerBO> getReportRetailer() {
        return retailerList;
    }

    public boolean isRep_retailer() {
        return rep_retailer;
    }

    public void setRep_retailer(boolean rep_retailer) {
        this.rep_retailer = rep_retailer;
    }
}
