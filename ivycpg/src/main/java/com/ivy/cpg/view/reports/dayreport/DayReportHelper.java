package com.ivy.cpg.view.reports.dayreport;

import android.content.Context;
import android.database.Cursor;

import com.ivy.cpg.view.reports.orderreport.OrderReportBO;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.InvoiceReportBO;
import com.ivy.sd.png.bo.OrderDetail;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.Vector;


public class DayReportHelper {


    private Context mContext;
    private BusinessModel mBusinessModel;

    public DayReportHelper(Context context) {
        this.mContext = context;
        this.mBusinessModel = (BusinessModel) context.getApplicationContext();
    }

    /**
     * This method will download the orderHeader details like OrderId,RetailerId
     * and Name , OrderValue and LPC
     *
     * @return ArrayList<OrderReportBO>
     */
    public ArrayList<OrderReportBO> downloadOrderReport() {
        ArrayList<OrderReportBO> reportOrDBooking = null;
        try {
            OrderReportBO orderReport;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT OrderHeader.OrderID ,OrderHeader.RetailerID , RetailerMaster.RetailerName,"
                            + "OrderHeader.OrderValue,OrderHeader.LinesPerCall ,RetailerMaster.sbd_dist_stock,RetailerMaster.sbd_dist_achieve,"
                            + "OrderHeader.upload,OrderHeader.totalweight,OrderHeader.FocusPackLines,OrderHeader.MSPLines,OrderHeader.is_vansales,"
                            + "IFNULL((select sum(taxValue) from OrderTaxDetails where OrderId = OrderHeader.OrderID ),0) as taxValue,"
                            + "IFNULL((select sum(Value) from OrderDiscountDetail where OrderId = OrderHeader.OrderID ),0) as discountValue,OrderHeader.orderImage as orderImage "
                            + "FROM OrderHeader INNER JOIN RetailerMaster "
                            + "ON OrderHeader.RetailerId = RetailerMaster.RetailerID INNER JOIN OrderDetail OD ON  OD.OrderID = OrderHeader.OrderID where OrderHeader.upload!='X' "
                            + "GROUP BY OrderHeader.OrderID ,OrderHeader.RetailerID,RetailerMaster.RetailerName,"
                            + "OrderHeader.OrderValue, OrderHeader.LinesPerCall");

            if (c != null) {
                reportOrDBooking = new ArrayList<>();
                while (c.moveToNext()) {
                    orderReport = new OrderReportBO();
                    orderReport.setOrderID(c.getString(0));
                    orderReport.setRetailerId(c.getString(1));
                    orderReport.setRetailerName(c.getString(2));
                    orderReport.setOrderTotal(c.getDouble(3));
                    orderReport.setLPC(c.getString(4));
                    orderReport.setDist(c.getString(5) + "/" + c.getString(6));
                    orderReport.setUpload(c.getString(c
                            .getColumnIndex("upload")));
                    orderReport.setWeight(c.getFloat(c.getColumnIndex("totalWeight")));
                    orderReport.setFocusBrandCount(c.getInt(9));
                    orderReport.setMustSellCount(c.getInt(10));
                    orderReport.setIsVanSeller(c.getInt(11));
                    orderReport.setTaxValue(c.getDouble(c.getColumnIndex("taxValue")));
                    orderReport.setDiscountValue(c.getDouble(c.getColumnIndex("discountValue")));
                    orderReport.setOrderedImage(c.getString(c.getColumnIndex("orderImage")));
                    reportOrDBooking.add(orderReport);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return reportOrDBooking;
    }

    /**
     * This method download the Invoice Header details from InvoiceMaster
     *
     * @return Vector<InvoiceReportBO>
     */
    public Vector<InvoiceReportBO> downloadInvoiceReport() {

        Vector<InvoiceReportBO> invoiceReportVector = null;
        try {
            InvoiceReportBO invoiceReport;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            StringBuffer sb = new StringBuffer();
            sb.append(" SELECT IM.orderid, IM.InvoiceNo,RM.RetailerId,RM.RetailerName,IM.invNetamount,(select count (invoiceid) from InvoiceDetails where ");
            sb.append("InvoiceDetails.invoiceid=IM.InvoiceNo) as LPC,RM.sbd_dist_stock,RM.sbd_dist_achieve,RM.beatId,P.invoiceamount,IM.totalweight,IM.taxamount,(IM.priceoffAmount+IM.discount+IM.SchemeAmount), ");
            sb.append("(select sum(Qty) from InvoiceDetails where InvoiceDetails.invoiceid=IM.InvoiceNo) FROM InvoiceMaster IM lEFT JOIN ");
            sb.append("payment p on p.Billnumber=IM.invoiceno  INNER JOIN InvoiceDetails ID ON IM.InvoiceNo = ID.invoiceid INNER JOIN RetailerMaster RM ON IM.Retailerid = RM.RetailerID");
            sb.append(" group by IM.InvoiceNo,IM.RetailerId");

            Cursor c = db.selectSQL(sb.toString());

            if (c != null) {
                invoiceReportVector = new Vector<>();
                while (c.moveToNext()) {
                    invoiceReport = new InvoiceReportBO();
                    invoiceReport.setOrderID(c.getString(0));
                    invoiceReport.setInvoiceNumber(c.getString(1));
                    invoiceReport.setRetailerId(c.getString(2));
                    invoiceReport.setRetailerName(c.getString(3));
                    invoiceReport.setInvoiceAmount(c.getDouble(4));

                    invoiceReport.setLinespercall(c.getInt(5));
                    invoiceReport
                            .setDist(c.getString(6) + "/" + c.getString(7));
                    invoiceReport.setBeatId(c.getInt(8));
                    invoiceReport.setInvoicePaidAmount(c.getDouble(9));
                    invoiceReport.setTotalWeight(c.getFloat(10));
                    invoiceReport.setTaxValue(c.getDouble(11));
                    invoiceReport.setDiscountValue(c.getDouble(12));
                    invoiceReport.setQty(c.getInt(13));
                    invoiceReportVector.add(invoiceReport);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return invoiceReportVector;
    }

    // download focus brand specific order reports-- rajkumar
    protected ArrayList<OrderDetail> downloadFBOrderDetailForDayReport(String productIds) {
        ArrayList<OrderDetail> reportOrderDetBooking = null;
        try {
            OrderDetail orderDetReport;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            Cursor c = db
                    .selectSQL("select OD.OrderID,OD.ProductID,OD.Qty,OD.Rate,OD.NetAmount from OrderDetail OD INNER JOIN OrderHeader OH ON OD.OrderID=OH.OrderID"
                            + " WHERE OD.ProductID IN (" + productIds + ")"
                            + " AND OH.upload!='X' and OH.OrderDate="
                            + mBusinessModel.QT(SDUtil.now(SDUtil.DATE_GLOBAL)));

            if (c != null) {
                reportOrderDetBooking = new ArrayList<>();
                while (c.moveToNext()) {

                    orderDetReport = new OrderDetail();
                    orderDetReport.setOrderID(c.getString(0));
                    orderDetReport.setProductID(c.getString(1));
                    orderDetReport.setQty(c.getInt(2));
                    orderDetReport.setRate(c.getFloat(3));
                    orderDetReport.setTotalAmount(c.getDouble(4));

                    reportOrderDetBooking.add(orderDetReport);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return reportOrderDetBooking;
    }

}
