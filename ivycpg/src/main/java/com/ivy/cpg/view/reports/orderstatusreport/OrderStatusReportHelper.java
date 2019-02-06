package com.ivy.cpg.view.reports.orderstatusreport;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.Vector;

/**
 * Created by anandasir on 28/5/18.
 */

public class OrderStatusReportHelper {

    Context context;
    BusinessModel bmodel;
    private static OrderStatusReportHelper instance = null;

    Vector<OrderStatusReportBO> orderStatusReportList;
    Vector<OrderStatusRetailerReportBO> orderStatusRetailerReportList;

    private OrderStatusReportHelper(Context context) {
        this.context = context;
        this.bmodel = (BusinessModel) context.getApplicationContext();
    }

    public static OrderStatusReportHelper getInstance(Context context) {
        if (instance == null) {
            instance = new OrderStatusReportHelper(context);
        }
        return instance;
    }

    public void getOrderStatusList() {
        OrderStatusReportBO orderStatusReportBO;
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
        try {
            db.openDataBase();
            String query = "Select distinct C.RetailerName,A.retailerid,A.retailerCode,A.orderid,A.OrderDate,A.OrderValue,B.ListName from P4OrderHistoryMaster A " +
                    "inner join StandardListMaster B on A.reasonid = B.ListId " +
                    "inner join RetailerMaster C on C.retailerID = A.RetailerID " +
                    "--where ParentID = (select ListID from StandardListMaster where ListCode ='DLRY')";
            Cursor c = db.selectSQL(query);
            if (c.getCount() > 0) {
                orderStatusReportList = new Vector<>();
                StandardListBO standardListBO;
                while (c.moveToNext()) {
                    orderStatusReportBO = new OrderStatusReportBO();
                    orderStatusReportBO.setRetailerName(c.getString(0));
                    orderStatusReportBO.setRetailerID(c.getString(1));
                    orderStatusReportBO.setRetailerCode(c.getString(2));
                    orderStatusReportBO.setOrderID(c.getString(3));
                    orderStatusReportBO.setOrderDate(c.getString(4));
                    orderStatusReportBO.setOrderValue(c.getString(5));
                    orderStatusReportBO.setListName(c.getString(6));
                    orderStatusReportList.add(orderStatusReportBO);
                }
            }
            c.close();
            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public void getOrderStatusRetailerList() {
        OrderStatusRetailerReportBO orderStatusRetailerReportBO;
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
        try {
            db.openDataBase();
            String query = "Select distinct C.RetailerID, C.RetailerCode, C.RetailerName from P4OrderHistoryMaster A " +
                    "inner join StandardListMaster B on reasonid = ListId inner join RetailerMaster C on C.retailerID = A.RetailerID " +
                    "order by C.RetailerName";
            Cursor c = db.selectSQL(query);
            if (c.getCount() > 0) {
                orderStatusRetailerReportList = new Vector<>();
                while (c.moveToNext()) {
                    orderStatusRetailerReportBO = new OrderStatusRetailerReportBO();
                    orderStatusRetailerReportBO.setRetailerID(c.getString(0));
                    orderStatusRetailerReportBO.setRetailerCode(c.getString(1));
                    orderStatusRetailerReportBO.setRetailerName(c.getString(2));
                    orderStatusRetailerReportList.add(orderStatusRetailerReportBO);
                }
            }
            c.close();
            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public void getInvoiceStatusRetailerList() {
        OrderStatusRetailerReportBO orderStatusRetailerReportBO;
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
        try {
            db.openDataBase();
            String query = "Select distinct C.RetailerID, C.RetailerCode, C.RetailerName from P4InvoiceHistoryMaster A " +
                    "inner join StandardListMaster B on reasonid = ListId inner join RetailerMaster C on C.retailerID = A.RetailerID " +
                    "order by C.RetailerName";
            Cursor c = db.selectSQL(query);
            if (c.getCount() > 0) {
                orderStatusRetailerReportList = new Vector<>();
                while (c.moveToNext()) {
                    orderStatusRetailerReportBO = new OrderStatusRetailerReportBO();
                    orderStatusRetailerReportBO.setRetailerID(c.getString(0));
                    orderStatusRetailerReportBO.setRetailerCode(c.getString(1));
                    orderStatusRetailerReportBO.setRetailerName(c.getString(2));
                    orderStatusRetailerReportList.add(orderStatusRetailerReportBO);
                }
            }
            c.close();
            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public void getInvoiceStatusList() {
        OrderStatusReportBO orderStatusReportBO;
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
        try {
            db.openDataBase();
            String query = "Select distinct C.RetailerName,A.retailerid,A.retailerCode,A.invoiceid,A.invoicedate,A.InvoiceValue,B.ListName from P4InvoiceHistoryMaster A " +
                    "inner join StandardListMaster B on A.reasonid = B.ListId " +
                    "inner join RetailerMaster C on C.retailerID = A.RetailerID " +
                    "--where ParentID = (select ListID from StandardListMaster where ListCode ='DLRY')";
            Cursor c = db.selectSQL(query);
            if (c.getCount() > 0) {
                orderStatusReportList = new Vector<>();
                StandardListBO standardListBO;
                while (c.moveToNext()) {
                    orderStatusReportBO = new OrderStatusReportBO();
                    orderStatusReportBO.setRetailerName(c.getString(0));
                    orderStatusReportBO.setRetailerID(c.getString(1));
                    orderStatusReportBO.setRetailerCode(c.getString(2));
                    orderStatusReportBO.setOrderID(c.getString(3));
                    orderStatusReportBO.setOrderDate(c.getString(4));
                    orderStatusReportBO.setOrderValue(c.getString(5));
                    orderStatusReportBO.setListName(c.getString(6));
                    orderStatusReportList.add(orderStatusReportBO);

                }
            }
            c.close();
            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public Vector<OrderStatusReportBO> getOrderStatusReportList() {
        return orderStatusReportList;
    }

    public void setOrderStatusReportList(Vector<OrderStatusReportBO> orderStatusReportList) {
        this.orderStatusReportList = orderStatusReportList;
    }

    public Vector<OrderStatusRetailerReportBO> getOrderStatusRetailerReportList() {
        return orderStatusRetailerReportList;
    }

    public void setOrderStatusRetailerReportList(Vector<OrderStatusRetailerReportBO> orderStatusRetailerReportList) {
        this.orderStatusRetailerReportList = orderStatusRetailerReportList;
    }

    public Vector<OrderStatusReportBO> filterRetailerList(String retailerID) {
        if (retailerID.equals("0"))
            return getOrderStatusReportList();
        Vector<OrderStatusReportBO> statusReportList = new Vector<>();
        for (OrderStatusReportBO orderStatusReport : getOrderStatusReportList()) {
            if (orderStatusReport.getRetailerID().equals(retailerID)) {
                statusReportList.add(orderStatusReport);
            }
        }
        return statusReportList;
    }
}
