package com.ivy.cpg.view.reports.dayreport;

import android.content.Context;
import android.database.Cursor;

import com.ivy.cpg.view.reports.orderreport.OrderReportBO;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.DailyReportBO;
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
    private DailyReportBO dailyRep;

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
        ArrayList<OrderReportBO> reportordbooking = null;
        try {
            OrderReportBO orderreport;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT OrderHeader.OrderID ,OrderHeader.RetailerID , RetailerMaster.RetailerName,"
                            + "OrderHeader.OrderValue,OrderHeader.LinesPerCall ,RetailerMaster.sbd_dist_stock,RetailerMaster.sbd_dist_achieve,"
                            + "OrderHeader.upload,OrderHeader.totalweight,OrderHeader.FocusPackLines,OrderHeader.MSPLines,OrderHeader.is_vansales,"
                            + "IFNULL((select sum(taxValue) from OrderTaxDetails where OrderId = OrderHeader.OrderID ),0) as taxValue,"
                            + "IFNULL((select sum(Value) from OrderDiscountDetail where OrderId = OrderHeader.OrderID ),0) as discountValue, "
                            + "IFNULL(SUM(OD.pieceqty),0),IFNULL(SUM(OD.caseQty),0),IFNULL(SUM(OD.outerQty),0),OrderHeader.orderImage "
                            + "FROM OrderHeader INNER JOIN RetailerMaster "
                            + "ON OrderHeader.RetailerId = RetailerMaster.RetailerID INNER JOIN OrderDetail OD ON  OD.OrderID = OrderHeader.OrderID where OrderHeader.upload!='X' "
                            + "GROUP BY OrderHeader.OrderID ,OrderHeader.RetailerID,RetailerMaster.RetailerName,"
                            + "OrderHeader.OrderValue, OrderHeader.LinesPerCall");

            if (c != null) {
                reportordbooking = new ArrayList<>();
                while (c.moveToNext()) {
                    orderreport = new OrderReportBO();
                    orderreport.setOrderID(c.getString(0));
                    orderreport.setRetailerId(c.getString(1));
                    orderreport.setRetailerName(c.getString(2));
                    orderreport.setOrderTotal(c.getDouble(3));
                    orderreport.setLPC(c.getString(4));
                    orderreport.setDist(c.getString(5) + "/" + c.getString(6));
                    orderreport.setUpload(c.getString(c
                            .getColumnIndex("upload")));
                    orderreport.setWeight(c.getFloat(c.getColumnIndex("totalWeight")));
                    orderreport.setFocusBrandCount(c.getInt(9));
                    orderreport.setMustSellCount(c.getInt(10));
                    orderreport.setIsVanSeller(c.getInt(11));
                    orderreport.setTaxValue(c.getDouble(c.getColumnIndex("taxValue")));
                    orderreport.setDiscountValue(c.getDouble(c.getColumnIndex("discountValue")));
                    orderreport.setVolumePcsQty(c.getInt(14));
                    orderreport.setVolumeCaseQty(c.getInt(15));
                    orderreport.setVolumeOuterQty(c.getInt(16));
                    orderreport.setOrderedImage(c.getString(17));
                    reportordbooking.add(orderreport);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return reportordbooking;
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

    /**
     * @param orderType 0 - piece; 1 - outer; 2 - case
     * @return
     */
    public ArrayList<ConfigureBO> downloadDailyReportDropSize(int orderType) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
        db.openDataBase();
        ArrayList<ConfigureBO> categoryDropSizeBO = new ArrayList<ConfigureBO>();

        int mCoveredStoreCount = 0;
        int contentLevel = 0;
        int parentLevel = 0;


        Cursor c = db
                .selectSQL("SELECT COUNT(DISTINCT Retailerid) FROM InvoiceMaster"
                        + " WHERE InvoiceDate='"
                        + SDUtil.now(SDUtil.DATE_GLOBAL)+"'");

        if (c != null) {
            if (c.moveToNext()) {
                mCoveredStoreCount = c.getInt(0);
            }
            c.close();
        }


        if (mCoveredStoreCount > 0) {

            String qty = "SUM(ID.Qty) AS QTY";

            if (orderType == 1)
                qty = "(SUM(ID.Qty)/ID.dOuomQty) AS QTY";
            else if (orderType == 2)
                qty = "(SUM(ID.Qty)/ID.uomCount) AS QTY";


            c = db.selectSQL("SELECT IFNULL(PL1.Sequence,0), IFNULL(PL3.Sequence,0), PL1.LevelName FROM ConfigActivityFilter CF " +
                    "LEFT JOIN ProductLevel PL1 ON PL1.LevelId = CF.ProductFilter1 " +
                    "LEFT JOIN ProductLevel PL3 ON PL3.LevelId = CF.ProductContent " +
                    " WHERE CF.ActivityCode = 'MENU_STK_ORD'");
            if (c != null) {
                if (c.moveToNext()) {
                    parentLevel = c.getInt(0);
                    contentLevel = c.getInt(1);
                }
            }


            int loopEnd = contentLevel - parentLevel + 1;

            StringBuilder sb = new StringBuilder();
            sb.append("select pdname,sum(qty) from (");
            sb.append("select PM1.pid as pdid,PM1.pname as pdname," + qty + "  FROM ProductMaster PM1 ");
            for (int i = 2; i <= loopEnd; i++) {
                sb.append("inner join productmaster PM" + i);
                sb.append(" ON PM" + i + ".Parentid = PM" + (i - 1) + ".pid ");
            }

            sb.append("inner join invoicedetails id on id.productid=PM" + loopEnd + ".pid ");
            sb.append(" WHERE PM1.PLid IN (SELECT levelid ");
            sb.append(" FROM productlevel WHERE sequence=" + parentLevel + ")");
            sb.append(" and ID.invoiceID IN(SELECT InvoiceNo FROM InvoiceMaster");
            sb.append(" WHERE InvoiceDate='" + mBusinessModel.userMasterHelper.getUserMasterBO().getDownloadDate() + "')");
            sb.append(" GROUP BY ID.ProductID ORDER BY PM1.Pid)");
            sb.append(" group by pdid");
            c = db.selectSQL(sb.toString());
            if (c != null) {
                while (c.moveToNext()) {
                    ConfigureBO con = new ConfigureBO();
                    con.setMenuName(c.getString(0));
                    con.setMenuNumber((c.getInt(1) / mCoveredStoreCount) + "");
                    categoryDropSizeBO.add(con);
                }
                c.close();
            }
        }


        db.closeDB();

        return categoryDropSizeBO;
    }

    public void downloadDailyReport() {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
        db.openDataBase();
        StringBuffer sb = new StringBuffer();
        dailyRep = new DailyReportBO();
        Cursor c = null;

        if (!mBusinessModel.configurationMasterHelper.IS_INVOICE) {
            sb.append("select  count(distinct retailerid),sum(linespercall),sum(ordervalue) from OrderHeader ");
            sb.append("where upload!='X' and OrderDate=" + QT(SDUtil.now(SDUtil.DATE_GLOBAL)));
            c = db
                    .selectSQL(sb.toString());
            if (c != null) {
                if (c.moveToNext()) {

                    dailyRep.setEffCoverage(c.getString(0));
                    dailyRep.setTotLines(c.getInt(1) + "");
                    dailyRep.setTotValues(c.getDouble(2) + "");
                }
                c.close();
            }
        } else {
            sb.append("select  count(distinct retailerid),sum(linespercall),sum(invoiceAmount) from Invoicemaster ");
            sb.append("where InvoiceDate=" + QT(SDUtil.now(SDUtil.DATE_GLOBAL)));
            c = db
                    .selectSQL(sb.toString());
            if (c != null) {
                if (c.moveToNext()) {
                    dailyRep.setEffCoverage(c.getString(0));
                    dailyRep.setTotLines(c.getInt(1) + "");
                    dailyRep.setTotValues(c.getDouble(2) + "");
                }
                c.close();
            }
        }
        sb = new StringBuffer();
        sb.append("select  sum(mspvalues),count(distinct orderid) from OrderHeader ");
        sb.append("where upload!='X' and OrderDate=" + QT(SDUtil.now(SDUtil.DATE_GLOBAL)));
        c = db
                .selectSQL(sb.toString());
        if (c != null) {
            if (c.moveToNext()) {

                dailyRep.setMspValues(c.getString(0));
                dailyRep.setNoofOrder(c.getString(1));

            }
            c.close();
        }

        sb = new StringBuffer();
        sb.append("select sum(pieceQty),sum(caseQty),sum(outerQty) from OrderDetail OD ");
        sb.append("inner join OrderHeader oh on oh.orderid=od.orderid ");
        sb.append("where oh.upload!='X' and OrderDate=" + QT(SDUtil.now(SDUtil.DATE_GLOBAL)));
        c = db
                .selectSQL(sb.toString());
        if (c != null) {
            if (c.moveToNext()) {

                dailyRep.setPcsQty(c.getString(0));
                dailyRep.setCsQty(c.getString(1));
                dailyRep.setOuQty(c.getString(2));

            }
            c.close();
        }


        c = db.selectSQL("select count(distinct RM.RetailerID) from RetailerMaster RM " +
                "LEFT JOIN RetailerBeatMapping RBM ON RBM.RetailerID = RM.RetailerID"
                + " where RBM.isdeviated='Y' and RBM.isVisited='Y'");

        if (c != null) {
            if (c.moveToNext()) {

                dailyRep.setTotAdhoc(c.getString(0));
            }
        }

        sb = new StringBuffer();
        sb.append("select count(oh.RetailerID) from OrderHeader oh ");
        sb.append("left join RetailerMaster rm on rm.RetailerID=oh.RetailerID ");
        sb.append("LEFT JOIN RetailerBeatMapping RBM ON RBM.RetailerID = rm.RetailerID ");
        sb.append("where oh.upload!='X' and OrderDate=" + QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + " and RBM.isdeviated='Y'");
        c = db
                .selectSQL(sb.toString());
        if (c != null) {
            if (c.moveToNext()) {

                dailyRep.setTotAdhocProductive(c.getString(0));

            }
            c.close();
        }

        c = db.selectSQL("select count(distinct RM.RetailerID) from RetailerMaster RM"
                + " inner join Retailermasterinfo RMI on RMI.retailerid= RM.retailerid"
                + " where RMI.isToday='1'");

        if (c != null) {
            if (c.moveToNext()) {

                dailyRep.setTotPlanned(c.getString(0));


            }
            c.close();
        }
        c = db.selectSQL("select count(distinct RM.RetailerID) from RetailerMaster RM"
                + " inner join Retailermasterinfo RMI on RMI.retailerid= RM.retailerid "
                + " LEFT JOIN RetailerBeatMapping RBM ON RBM.RetailerID = RM.RetailerID"
                + " where RBM.isVisited='Y' and RMI.isToday='1'");//space added before where condition

        if (c != null) {
            if (c.moveToNext()) {

                dailyRep.setTotPlannedVisit(c.getString(0));


            }
            c.close();
        }

        sb = new StringBuffer();
        sb.append("select count(oh.RetailerID) from OrderHeader oh ");
        sb.append("left join RetailerMaster rm on rm.RetailerID=oh.RetailerID ");
        sb.append(" inner join Retailermasterinfo RMI on RMI.retailerid= RM.retailerid ");
        sb.append("where oh.upload!='X' and OrderDate=" + QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + " and RMI.isToday='1'");
        c = db
                .selectSQL(sb.toString());
        if (c != null) {
            if (c.moveToNext()) {

                dailyRep.setTotPlannedProductive(c.getString(0));

            }
            c.close();
        }

        db.closeDB();
    }

    /**
     * kellogs specific
     */
    public void downloadDailyReportKellogs() {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
        db.openDataBase();
        StringBuffer sb = new StringBuffer();
        Cursor c = null;

        sb.append("select  count(distinct retailerid),sum(linespercall),sum(ordervalue) from OrderHeader ");
        sb.append("where upload!='X' and OrderDate=" + QT(SDUtil.now(SDUtil.DATE_GLOBAL)));
        c = db
                .selectSQL(sb.toString());
        if (c != null) {
            if (c.moveToNext()) {

                dailyRep.setKlgsEffCoverage(c.getString(0));
                dailyRep.setKlgsTotLines(c.getInt(1) + "");
                dailyRep.setKlgsTotValue(c.getDouble(2) + "");
            }
            c.close();
        }

        db.closeDB();
    }

    public DailyReportBO getDailyRep(){
        return dailyRep;
    }

    public String QT(String data) {
        return "'" + data + "'";
    }

}
