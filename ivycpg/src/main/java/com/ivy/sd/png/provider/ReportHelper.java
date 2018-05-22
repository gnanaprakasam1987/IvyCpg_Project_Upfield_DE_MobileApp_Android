package com.ivy.sd.png.provider;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

import com.ivy.cpg.primarysale.bo.DistributorMasterBO;
import com.ivy.cpg.view.reports.OrderReportBO;
import com.ivy.cpg.view.salesreturn.SalesReturnReasonBO;
import com.ivy.lib.Utils;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.AttendanceReportBo;
import com.ivy.sd.png.bo.BeatMasterBO;
import com.ivy.sd.png.bo.ContractBO;
import com.ivy.sd.png.bo.CreditNoteListBO;
import com.ivy.sd.png.bo.InventoryBO_Proj;
import com.ivy.sd.png.bo.InvoiceReportBO;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.bo.LoadManagementBO;
import com.ivy.sd.png.bo.LogReportBO;
import com.ivy.sd.png.bo.OrderDetail;
import com.ivy.sd.png.bo.OrderTakenTimeBO;
import com.ivy.sd.png.bo.OutletReportBO;
import com.ivy.sd.png.bo.PaymentBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.ProductivityReportBO;
import com.ivy.sd.png.bo.PromotionTrackingReportBO;
import com.ivy.sd.png.bo.QuestionReportBO;
import com.ivy.sd.png.bo.ReportBrandPerformanceBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.RetailersReportBO;
import com.ivy.sd.png.bo.SKUReportBO;
import com.ivy.sd.png.bo.SalesFundamentalGapReportBO;
import com.ivy.sd.png.bo.SchemeProductBO;
import com.ivy.sd.png.bo.SpinnerBO;
import com.ivy.sd.png.bo.StockReportBO;
import com.ivy.sd.png.bo.TaskReportBo;
import com.ivy.sd.png.bo.asset.AssetTrackingBrandBO;
import com.ivy.sd.png.bo.asset.AssetTrackingReportBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.DateUtil;
import com.ivy.sd.png.util.StandardListMasterConstants;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

public class ReportHelper {

    private static ReportHelper instance = null;
    private final Context mContext;
    private final BusinessModel bmodel;
    private ArrayList<PaymentBO> paymentList;
    private List<PaymentBO> parentPaymentList;
    private List<List<PaymentBO>> childPaymentList;
    private Vector<StockReportBO> currentStock;
    private ArrayList<StockReportBO> mEODStockReportList;
    private Vector<QuestionReportBO> questionReport;
    private HashMap<String, StockReportBO> mEODReportBOByProductID;

    private ArrayList<LogReportBO> mLogReportList;
    private String webViewPlanUrl = "";
    private String webReportUrl = "";
    private ArrayList<CreditNoteListBO> creditNoteList;
    private ArrayList<AttendanceReportBo> attendanceList;
    private ArrayList<String> attendanceMonth;

    private ArrayList<RetailerMasterBO> assetRetailerList;
    private ArrayList<AssetTrackingBrandBO> assetBrandList;

    private Vector<RetailerMasterBO> retailerMaster;
    private HashMap<String, ArrayList<ProductMasterBO>> closingStkReportByRetailId;

    private ReportHelper(Context context) {
        this.mContext = context;
        this.bmodel = (BusinessModel) context;
    }

    public static ReportHelper getInstance(Context context) {
        if (instance == null) {
            instance = new ReportHelper(context);
        }
        return instance;
    }

    public ArrayList<StockReportBO> getEODStockReport() {
        return mEODStockReportList;
    }

    public ArrayList<LogReportBO> getLogReport() {
        return mLogReportList;
    }

    /**
     * This method will download the orderHeader details like OrderId,RetailerId
     * and Name , OrderValue and LPC
     *
     * @return ArrayList<OrderReportBO>
     */
    public ArrayList<OrderReportBO> downloadOrderreport() {
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
                            + "IFNULL((select sum(Value) from OrderDiscountDetail where OrderId = OrderHeader.OrderID ),0) as discountValue "
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

    public ArrayList<OrderReportBO> downloadDistributorOrderReport() {
        DBUtil db = null;
        ArrayList<OrderReportBO> reportdistordbooking = null;
        try {
            OrderReportBO orderreport;
            db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("select DOH.uid,DOH.distid,DM.DName,DOH.TotalValue,DOH.LPC,DOH.upload From DistOrderHeader DOH");
            sb.append(" Inner join DistributorMaster DM on DOH.distid = DM.Did ");
            sb.append("inner join DistOrderDetails DOD on DOH.Uid=DOD.Uid Group by DOH.uid,DM.DName");
            Cursor c = db.selectSQL(sb.toString());
            reportdistordbooking = new ArrayList<>();
            if (c != null) {
                while (c.moveToNext()) {
                    orderreport = new OrderReportBO();
                    orderreport.setOrderID(c.getString(0));
                    orderreport.setDistributorId(c.getInt(1));
                    orderreport.setDistributorName(c.getString(2));
                    orderreport.setOrderTotal(c.getDouble(3));
                    orderreport.setLPC(c.getString(4));

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

    public ArrayList<SalesFundamentalGapReportBO> downloadSFGreport(int BeatID, String filter) {
        ArrayList<SalesFundamentalGapReportBO> reportordbooking = null;
        try {
            SalesFundamentalGapReportBO orderreport;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            String tableName = "", pm = "";
            if (filter.equals("SOS")) {
                pm = "SOSGap";
                tableName = "SOS_Tracking_Detail";
            } else if (filter.equals("SOD")) {
                pm = "SODGap";
                tableName = "SOD_Tracking_Detail";
            } else if (filter.equals("SOSKU")) {
                pm = "SOSKUGap";
                tableName = "SOSKU_Tracking_Detail";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Select A.PName, ifnull(sum(0+B.Gap),0) as Gap, ifnull(sum(0+C." + pm + "),0) as PM from ProductMaster A ");
            sb.append("left join " + tableName + " B on B.Pid = A.Pid ");
            sb.append("left join SFGapReportMaster C on C.Brandid = A.Pid ");
            sb.append("left join RetailerMaster D on B.RetailerID = D.RetailerID and C.RetailerID = D.RetailerID ");
            sb.append("where A.PLid  = (Select LevelID from ProductLevel where LevelID not in (Select ParentID from ProductLevel)) and D.BeatID ='" + BeatID + "' ");
            sb.append("group by A.PName order by A.Pname");


//            StringBuilder sb = new StringBuilder();
//            sb.append("Select A.PName, ifnull(sum(0+B.Gap),0) as SOSGap, ifnull(sum(0+E.SOSGap),0) as SOSPM,");
//            sb.append("ifnull(sum(0+C.Gap),0) as SODGap, ifnull(sum(0+E.SODGap),0) as SODPM,");
//            sb.append("ifnull(sum(0+D.Gap),0) as SOSKUGap, ifnull(sum(0+E.SOSKUGap),0) as SOSKUPM from ProductMaster A ");
//            sb.append("left join SOS_Tracking_Detail B on B.Pid = A.Pid ");
//            sb.append("left join SOD_Tracking_Detail C on C.Pid = A.Pid ");
//            sb.append("left join SOSKU_Tracking_Detail D on D.Pid = A.Pid ");
//            sb.append("left join SFGapReportMaster E on E.Brandid = A.Pid ");
//            sb.append("left join RetailerMaster F on F.RetailerID = B.RetailerID and F.RetailerID = C.RetailerID and F.RetailerID = D.RetailerID and F.RetailerID = E.RetailerID ");
//            sb.append("where PLid  = (Select LevelID from ProductLevel where LevelID not in (Select ParentID from ProductLevel)) ");
//            if (BeatID != 0) {
//                sb.append("and F.BeatID ='" + BeatID + "' ");
//            }
//            sb.append("group by A.PName order by A.Pname");
            Cursor c = db
                    .selectSQL(sb.toString());
            if (c != null) {
                reportordbooking = new ArrayList<>();
                while (c.moveToNext()) {
                    orderreport = new SalesFundamentalGapReportBO();
                    orderreport.setPName(c.getString(0));
                    orderreport.setGap(c.getString(1));
                    orderreport.setPM(c.getString(2));
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

    public ArrayList<PromotionTrackingReportBO> downloadPromotionTrackingreport(int retailerID) {
        ArrayList<PromotionTrackingReportBO> reportordbooking = null;
        try {
            PromotionTrackingReportBO orderreport;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT D.PSName as PSName, A.PromoName, Case When (B.IsExecuted =1) then 'YES' else 'NO' End as IsExecuted, Case When (B.HasAnnouncer =1) then ");
            sb.append("'YES' else 'NO' End as HasAnnouncer, ifNull(C.ListName,'') as Reason  FROM PromotionProductMapping A ");
            sb.append("inner join PromotionDetail B on A.PID = B.BrandID ");
            sb.append("left join StandardListMaster C on C.ListID = B.ReasonID and C.ListType = 'REASON' ");
            sb.append("inner join ProductMaster D on D.Pid = B.BrandID ");
            sb.append("inner join RetailerMaster E on B.RetailerID = E.RetailerID Where E.RetailerID='" + retailerID + "'");

            Cursor c = db
                    .selectSQL(sb.toString());
            if (c != null) {
                reportordbooking = new ArrayList<>();
                while (c.moveToNext()) {
                    orderreport = new PromotionTrackingReportBO();
                    orderreport.setBrandName(c.getString(0));
                    orderreport.setPromoName(c.getString(1));
                    orderreport.setIsExecuted(c.getString(2));
                    orderreport.setHasAnnouncer(c.getString(3));
                    orderreport.setReason(c.getString(4));
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

    public ArrayList<RetailerMasterBO> downloadPromotionTrackingRetailerMaster() {
        ArrayList<RetailerMasterBO> reportordbooking = null;
        try {
            RetailerMasterBO orderreport;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("Select distinct A.RetailerID, A.RetailerName from RetailerMaster A inner join PromotionHeader B on A.RetailerID = B.RetailerID");

            Cursor c = db
                    .selectSQL(sb.toString());
            if (c != null) {
                reportordbooking = new ArrayList<>();
                while (c.moveToNext()) {
                    orderreport = new RetailerMasterBO();
                    orderreport.setRetailerID(c.getString(0));
                    orderreport.setRetailerName(c.getString(1));
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

    // download focus brand specific order reports-- rajkumar
    public ArrayList<OrderDetail> downloadFBOrderDetailForDayReport(String productIds) {
        ArrayList<OrderDetail> reportorddetbooking = null;
        try {
            OrderDetail orderdetreport;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            Cursor c = db
                    .selectSQL("select OD.OrderID,OD.ProductID,OD.Qty,OD.Rate,OD.totalDiscountedAmt from OrderDetail OD INNER JOIN OrderHeader OH ON OD.OrderID=OH.OrderID"
                            + " WHERE OD.ProductID IN (" + productIds + ")"
                            + " AND OH.upload!='X' and OH.OrderDate="
                            + bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL)));

            if (c != null) {
                reportorddetbooking = new ArrayList<>();
                while (c.moveToNext()) {

                    orderdetreport = new OrderDetail();
                    orderdetreport.setOrderID(c.getString(0));
                    orderdetreport.setProductID(c.getString(1));
                    orderdetreport.setQty(c.getInt(2));
                    orderdetreport.setRate(c.getFloat(3));
                    orderdetreport.setTotalAmount(c.getDouble(4));

                    reportorddetbooking.add(orderdetreport);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return reportorddetbooking;
    }

    /**
     * This method will downlaod the OrderDetails of a particular order.
     * ProductName,CaseQty,PcsQty,TotalAmount.
     *
     * @param orderID
     * @return ArrayList<OrderReportBO> reportorddetbooking
     */
    public ArrayList<OrderReportBO> downloadOrderreportdetail(
            String orderID) {
        ArrayList<OrderReportBO> reportorddetbooking = null;
        try {
            OrderReportBO orderdetreport;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            Cursor c = db
                    .selectSQL("select PM.Pname,PM.psname,OD.caseQty,OD.pieceqty,(OD.totalDiscountedAmt) as value,OD.outerQty,PM.pid,OD.batchid,BM.batchNum,OD.weight,OD.qty "
                            + " from OrderDetail OD INNER JOIN  ProductMaster PM ON OD.ProductID = PM.PID LEFT JOIN BatchMaster BM ON OD.batchid =  BM.batchid  and BM.pid=OD.productid where OD.OrderID="
                            + bmodel.QT(orderID) + "and OD.OrderType = 0");
            if (c != null) {
                reportorddetbooking = new ArrayList<>();
                while (c.moveToNext()) {

                    orderdetreport = new OrderReportBO();
                    orderdetreport.setProductName(c.getString(0));
                    orderdetreport.setProductShortName(c.getString(1));
                    orderdetreport.setCQty(c.getInt(2));
                    orderdetreport.setPQty(c.getInt(3));
                    orderdetreport.setTot(c.getFloat(4));
                    orderdetreport.setOuterOrderedCaseQty(c.getInt(5));
                    orderdetreport.setBatchId(c.getInt(7));
                    orderdetreport.setBatchNo(c.getString(8));
                    orderdetreport.setWeight(c.getFloat(9));
                    orderdetreport.setTotalQty(c.getInt(10));
                    reportorddetbooking.add(orderdetreport);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return reportorddetbooking;
    }

    public ArrayList<OrderReportBO> distOrderReportDetail(String orderId) {
        ArrayList<OrderReportBO> distributorOrderList = null;
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("select PM.pname,PM.psname,DOD.pid,DOD.qty,DOD.uomid,DOD.uomcount,DOD.batchid,BM.batchNum,DOD.LineValue from DistOrderDetails ");
            sb.append("DOD inner join ProductMaster PM on PM.pid=DOD.pid Left Join BatchMaster BM on DOD.pid=BM.pid and DOD.batchid=BM.batchid");
            sb.append(" where  DOD.uid=" + bmodel.QT(orderId));

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
                    setDistOrderReportDetails(pName, pSname, pid, qty, uomid, uomCount, batchid, batchNum, totalValue, distributorOrderList);

                }
            }
        } catch (Exception e) {
            Commons.printException(e + " ");
        } finally {
            if (db != null)
                db.closeDB();
        }
        return distributorOrderList;
    }

    private void setDistOrderReportDetails(String pname, String pSname, String pid, int qty, int uomid, int uomCount, int batchid, String batchnum, float totalValue, ArrayList<OrderReportBO> distReportList) {
        OrderReportBO orderReportBO;

        if (distReportList == null) {
            distReportList = new ArrayList<>();

        }
        ProductMasterBO productBo = bmodel.productHelper.getProductMasterBOById(pid);
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
                    orderReportBO = new OrderReportBO();
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
                orderReportBO = new OrderReportBO();
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

    public void updateDistributor(String did) {

        ArrayList<DistributorMasterBO> distributorList = bmodel.distributorMasterHelper.getDistributors();
        for (DistributorMasterBO distributorMasterBO : distributorList) {
            if (did.equals(distributorMasterBO.getDId())) {
                bmodel.distributorMasterHelper.setDistributor(distributorMasterBO);
                break;
            }
        }

    }

    public int getavglinesfororderbooking(String tableName) {
        int tot = 0;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
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

    public int getorderbookingCount(String tableName) {
        int tot = 0;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Cursor c = db.selectSQL("select count (distinct retailerid) from "
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

    /**
     * This method will download the previous day orderHeader details like
     * OrderId,RetailerId and Name , OrderValue and LPC
     *
     * @return ArrayList<OrderReportBO>
     */
    public ArrayList<OrderReportBO> downloadPVSOrderreport() {
        ArrayList<OrderReportBO> reportordbooking = null;
        try {
            OrderReportBO orderreport;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT OrderID ,RetailerID , RetailerName,"
                            + "OrderValue,LinesPerCall,distribution FROM PVSOrderHeader");
            if (c != null) {

                reportordbooking = new ArrayList<>();
                while (c.moveToNext()) {
                    orderreport = new OrderReportBO();
                    orderreport.setOrderID(c.getString(0));
                    orderreport.setRetailerId(c.getString(1));
                    orderreport.setRetailerName(c.getString(2));
                    orderreport.setOrderTotal(c.getDouble(3));
                    orderreport.setLPC(c.getString(4));
                    orderreport.setDist(c.getString(5));
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


    ArrayList<ContractBO> contractList;

    public ArrayList<ContractBO> getContractList() {
        return contractList;
    }

    public void setContractList(ArrayList<ContractBO> contractList) {
        this.contractList = contractList;
    }

    public void downloadContractReport() {
        ContractBO contractBO;
        ArrayList<ContractBO> contractBOArrayList = new ArrayList<>();
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("Select Distinct RM.RetailerID, RM.RetailerCode, RM.RetailerName, (select ch.ChName from channelhierarchy ch where ch.chid = RM.subchannelid) AS SubChannel, RC.ContractDesc, RC.StartDate, RC.EndDate,RC.ContractId  from RetailerMaster RM inner join  RetailerContract RC where RM.RetailerID = RC.RetailerId");
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    contractBO = new ContractBO();
                    contractBO.setOutletCode(c.getString(1));
                    contractBO.setOutletName(c.getString(2));
                    contractBO.setSubChannel(c.getString(3));
                    contractBO.setTradeName(c.getString(4));
                    contractBO.setStartDate(DateUtil.convertFromServerDateToRequestedFormat(
                            c.getString(5),
                            ConfigurationMasterHelper.outDateFormat));
                    contractBO.setEndDate(DateUtil.convertFromServerDateToRequestedFormat(
                            c.getString(6),
                            ConfigurationMasterHelper.outDateFormat));
                    contractBO.setDaysToExp(Utils.getDaysDifference(SDUtil.now(SDUtil.DATE_GLOBAL), c.getString(6), "yyyy/MM/dd"));
                    contractBO.setContractID(c.getString(7));
                    if (contractBO.getDaysToExp() <= 45)
                        contractBOArrayList.add(contractBO);
                }
                if (contractBOArrayList != null)
                    setContractList(contractBOArrayList);
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    /**
     * This method will downlaod the previous day OrderDetails of a particular
     * order. ProductName,CaseQty,PcsQty,TotalAmount.
     *
     * @param orderID
     * @return ArrayList<OrderReportBO> reportorddetbooking
     */
    public ArrayList<OrderReportBO> downloadPVSOrderreportdetail(
            String orderID) {
        ArrayList<OrderReportBO> reportorddetbooking = null;
        try {
            OrderReportBO orderdetreport;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            Cursor c = db
                    .selectSQL("SELECT  ProductName,ProductShortName,caseQty,"
                            + " pieceqty, totalamount,outerQty,productid  FROM PVSOrderDetail WHERE OrderID='"
                            + orderID + "'");
            if (c != null) {
                reportorddetbooking = new ArrayList<>();
                while (c.moveToNext()) {

                    orderdetreport = new OrderReportBO();
                    orderdetreport.setProductName(c.getString(0));
                    orderdetreport.setProductShortName(c.getString(1));
                    orderdetreport.setCQty(c.getInt(2));
                    orderdetreport.setPQty(c.getInt(3));
                    orderdetreport.setTot(c.getFloat(4));
                    orderdetreport.setOuterOrderedCaseQty(c.getInt(5));
                    orderdetreport.setProductId(c.getString(6));
                    reportorddetbooking.add(orderdetreport);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return reportorddetbooking;
    }

    /**
     * This method download the Invoice Header details from InvoiceMaster
     *
     * @return
     */
    public Vector<InvoiceReportBO> downloadInvoicereport() {

        Vector<InvoiceReportBO> invoiceReportVector = null;
        try {
            InvoiceReportBO invoicereport;
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
                    invoicereport = new InvoiceReportBO();
                    invoicereport.setOrderID(c.getString(0));
                    invoicereport.setInvoiceNumber(c.getString(1));
                    invoicereport.setRetailerId(c.getString(2));
                    invoicereport.setRetailerName(c.getString(3));
                    invoicereport.setInvoiceAmount(c.getDouble(4));

                    invoicereport.setLinespercall(c.getInt(5));
                    invoicereport
                            .setDist(c.getString(6) + "/" + c.getString(7));
                    invoicereport.setBeatId(c.getInt(8));
                    invoicereport.setInvoicePaidAmount(c.getDouble(9));
                    invoicereport.setTotalWeight(c.getFloat(10));
                    invoicereport.setTaxValue(c.getDouble(11));
                    invoicereport.setDiscountValue(c.getDouble(12));
                    invoicereport.setQty(c.getInt(13));
                    invoiceReportVector.add(invoicereport);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return invoiceReportVector;
    }

    /**
     * Check wheather the Invoice has any details or not.
     *
     * @param invoiceNumber
     * @return
     */
    public boolean hasInvoiceDetails(String invoiceNumber) {
        try {

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT  count(invoiceId) from InvoiceDetails where InvoiceId='"
                            + invoiceNumber + "'");
            if (c != null) {
                if (c.moveToNext()) {
                    if (c.getInt(0) > 0)
                        return true;
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return false;
    }

    /**
     * This method will download the previous day order details
     * SalesRepCode,SalesRepName
     * ,RetailerCode,RetailerName,OrderNo,SKUCode,SKUDescription
     * ,QrderQty(pcs),DeliveryDate for Export
     *
     * @return ArrayList<OrderReportBO>
     */
    public ArrayList<ArrayList<String>> downloadPreviousOrderForExport() {
        ArrayList<ArrayList<String>> rows = new ArrayList<>();
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select OH.RetailerCode,OH.RetailerName,OH.OrderId,OH.OrderDate,OD.PCode,"
                            + "OD.ProductName,OD.caseQty,OD.pieceQty,OH.deliveryDate,OD.outerQty  from "
                            + "PVSOrderHeader OH inner join PVSOrderDetail OD on OH.OrderId=OD.OrderId");
            if (c != null) {

                while (c.moveToNext()) {
                    ArrayList<String> row = new ArrayList<>();
                    row.add(bmodel.userMasterHelper.getUserMasterBO()
                            .getUserCode() + "");
                    row.add(bmodel.userMasterHelper.getUserMasterBO()
                            .getUserName());
                    row.add(c.getString(0));
                    row.add(c.getString(1));
                    row.add(c.getString(2));
                    row.add(c.getString(3));
                    row.add(c.getString(4));
                    row.add(c.getString(5));
                    row.add(c.getString(6));
                    row.add(c.getString(7));
                    row.add(c.getString(8));

                    rows.add(row);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return rows;
    }

    public HashMap<String, ArrayList<ArrayList<String>>> getmOrderDetailsByDistributorName() {
        return mOrderDetailsByDistributorName;
    }

    HashMap<String, ArrayList<ArrayList<String>>> mOrderDetailsByDistributorName;

    public HashMap<String, String> getmEmailIdByDistributorName() {
        return mEmailIdByDistributorName;
    }

    HashMap<String, String> mEmailIdByDistributorName;

    public void downloadOrderReportToExport() {
        ArrayList<ArrayList<String>> rows = new ArrayList<>();
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select distinct DM.dname," +
                            "RM.retailerCode,RM.retailerName,OH.orderId,OH.orderDate,PM.pCode,PM.pName,OD.pieceQty" +
                            ",OD.caseQty,OD.outerQty,OH.deliveryDate,DM.email from orderHeader OH " +
                            "INNER JOIN orderDetail OD ON OH.orderId=OD.orderId " +
                            "LEFT JOIN ProductMaster PM ON PM.pid=OD.productId " +
                            "LEFT JOIN RetailerMaster RM ON RM.retailerId=OH.retailerId " +
                            "LEFT JOIN DistributorMaster DM ON DM.Did=OH.sid" +
                            " order by OH.sid");
            if (c != null) {
                mOrderDetailsByDistributorName = new HashMap<>();
                mEmailIdByDistributorName = new HashMap<>();
                String mPreviousDistributor = "";
                while (c.moveToNext()) {


                    if (!mPreviousDistributor.equals(c.getString(0))) {
                        // Preparing new list for every distributor
                        rows = new ArrayList<>();
                    }

                    ArrayList<String> row = new ArrayList<>();

                    row.add(c.getString(0));
                    row.add(bmodel.userMasterHelper.getUserMasterBO()
                            .getUserCode() + "");
                    row.add(bmodel.userMasterHelper.getUserMasterBO()
                            .getUserName());
                    row.add(c.getString(1));
                    row.add(c.getString(2));
                    row.add(c.getString(3));
                    row.add(c.getString(4));
                    row.add(c.getString(5));
                    row.add(c.getString(6));
                    row.add(c.getString(7));
                    row.add(c.getString(8));
                    row.add(c.getString(9));
                    row.add(c.getString(10));

                    rows.add(row);

                    // distributor wise record
                    mOrderDetailsByDistributorName.put(c.getString(0), rows);

                    mEmailIdByDistributorName.put(c.getString(0), c.getString(11));

                    mPreviousDistributor = c.getString(0);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public Vector<SKUReportBO> downloadSKUReport() {
        SKUReportBO skuObject;
        Vector<SKUReportBO> skubo = null;
        try {
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
                            + " WHERE CF.ActivityCode = "
                            + bmodel.QT("MENU_STK_ORD"));

            if (filterCur != null) {
                if (filterCur.moveToNext()) {
                    mParentLevel = filterCur.getInt(0);
                    mChildLevel = filterCur.getInt(1);
                    mContentLevel = filterCur.getInt(2);
                }
                filterCur.close();
            }

            Cursor c;
            String sql;

            int loopEnd;
            String parentFilter;

            if (mChildLevel != 0) {
                loopEnd = mContentLevel - mChildLevel + 1;
                parentFilter = "ProductFilter2";
            } else {
                loopEnd = mContentLevel - mParentLevel + 1;
                parentFilter = "ProductFilter1";
            }

            sql = "SELECT P"
                    + loopEnd
                    + ".pname,SUM(OD.qty), OD.caseqty, OD.msqqty,P1.pid FROM ProductMaster P1 "
                    + "INNER JOIN OrderDetail OD ON OD.ProductID = P"
                    + loopEnd
                    + ".PID";


            for (int i = 2; i <= loopEnd; i++)
                sql = sql + " INNER JOIN ProductMaster P" + i + " ON P" + i
                        + ".ParentId = P" + (i - 1) + ".PID";

            sql = sql + " WHERE P1.PLid IN (SELECT " + parentFilter + " FROM ConfigActivityFilter"
                    + " WHERE ActivityCode = "
                    + bmodel.QT("MENU_STK_ORD")
                    + ") GROUP BY P"
                    + loopEnd + ".PName";

            c = db.selectSQL(sql);
            if (c != null) {

                skubo = new Vector<>();
                while (c.moveToNext()) {
                    skuObject = new SKUReportBO();
                    skuObject.setProdname(c.getString(0));
                    skuObject.setQty(c.getString(1));
                    skuObject.setOuqty(c.getString(2));
                    skuObject.setMsqqty(c.getString(3));
                    skuObject.setParentID(c.getInt(4));
                    skubo.add(skuObject);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return skubo;
    }

    public Vector<SKUReportBO> downloadSKUReportNew() {
        SKUReportBO skuObject;
        Vector<SKUReportBO> skubo = null;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            int mFiltrtLevel = 0;
            int mChildLevel = 0;
            int mContentLevel = 0;

            if (bmodel.productHelper.getSequenceValues() != null) {
                if (bmodel.productHelper.getSequenceValues().size() > 0) {
                    mChildLevel = bmodel.productHelper.getSequenceValues().size();
                }
            }

            if (mChildLevel == 0) {
                Cursor cur = db.selectSQL("SELECT IFNULL(PL1.Sequence,0),IFNULL(PL2.Sequence,0)"
                        + " FROM ConfigActivityFilter CF"
                        + " LEFT JOIN ProductLevel PL1 ON PL1.LevelId = CF.ProductFilter1"
                        + " LEFT JOIN ProductLevel PL2 ON PL2.LevelId =  CF.ProductContent"
                        + " WHERE CF.ActivityCode ="
                        + bmodel.QT("MENU_STK_ORD"));
                if (cur != null) {
                    if (cur.moveToNext()) {
                        mFiltrtLevel = cur.getInt(0);
                        mContentLevel = cur.getInt(1);
                    }
                    cur.close();
                }
            } else {
                Cursor filterCur = db
                        .selectSQL("SELECT IFNULL(PL1.Sequence,0), IFNULL(PL2.Sequence,0)"
                                + " FROM ConfigActivityFilter CF"
                                + " LEFT JOIN ProductLevel PL1 ON PL1.LevelId = CF.ProductFilter"
                                + mChildLevel
                                + " LEFT JOIN ProductLevel PL2 ON PL2.LevelId = CF.ProductContent"
                                + " WHERE CF.ActivityCode = "
                                + bmodel.QT("MENU_STK_ORD"));

                if (filterCur != null) {
                    if (filterCur.moveToNext()) {
                        mFiltrtLevel = filterCur.getInt(0);
                        mContentLevel = filterCur.getInt(1);
                    }
                    filterCur.close();
                }

            }

            Cursor c;
            String sql;

            int loopEnd = mContentLevel - mFiltrtLevel + 1;
            String parentFilter;

            if (mChildLevel != 0) {
                parentFilter = "ProductFilter" + mChildLevel;
            } else {
                parentFilter = "ProductFilter1";
            }

            sql = "SELECT P"
                    + loopEnd
                    + ".pname,SUM(OD.qty), OD.caseqty, OD.msqqty,P1.pid FROM ProductMaster P1 "
                    + "INNER JOIN OrderDetail OD ON OD.ProductID = P"
                    + loopEnd
                    + ".PID";


            for (int i = 2; i <= loopEnd; i++)
                sql = sql + " INNER JOIN ProductMaster P" + i + " ON P" + i
                        + ".ParentId = P" + (i - 1) + ".PID";

            sql = sql + " WHERE P1.PLid IN (SELECT " + parentFilter + " FROM ConfigActivityFilter"
                    + " WHERE ActivityCode = "
                    + bmodel.QT("MENU_STK_ORD")
                    + ") GROUP BY P"
                    + loopEnd + ".PID";

            c = db.selectSQL(sql);
            if (c != null) {

                skubo = new Vector<>();
                while (c.moveToNext()) {
                    skuObject = new SKUReportBO();
                    skuObject.setProdname(c.getString(0));
                    skuObject.setQty(c.getString(1));
                    skuObject.setOuqty(c.getString(2));
                    skuObject.setMsqqty(c.getString(3));
                    skuObject.setParentID(c.getInt(4));
                    skubo.add(skuObject);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return skubo;
    }

    public HashMap<String, ArrayList<PaymentBO>> getLstPaymentBObyGroupId() {
        return lstPaymentBObyGroupId;
    }

    public void setLstPaymentBObyGroupId(HashMap<String, ArrayList<PaymentBO>> lstPaymentBObyGroupId) {
        this.lstPaymentBObyGroupId = lstPaymentBObyGroupId;
    }

    private HashMap<String, ArrayList<PaymentBO>> lstPaymentBObyGroupId;

    public void loadCollectionReport() {
        try {
            paymentList = new ArrayList<>();
            parentPaymentList = new ArrayList<>();
            childPaymentList = new ArrayList<>();


            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();


            StringBuffer sb = new StringBuffer();
            sb.append("SELECT DISTINCT RM.RetailerName, PY.BeatID, PY.InvoiceAmount, PY.Amount, PY.Balance,ListCode,");

            sb.append("PY.ChequeNumber, PY.ChequeDate, PY.Date, PY.BillNumber, PY.ReceiptDate, ListName,inv.paidamount,PY.uid,PY.totaldiscount,PY.GroupId,RM.RetailerCode,PY.dateTime,PY.refno FROM Payment PY ");
            sb.append("INNER JOIN InvoiceMaster inv on PY.BillNumber=Inv.InvoiceNo ");
            sb.append("INNER JOIN RetailerMaster RM on RM.RetailerID = PY .RetailerID INNER JOIN StandardListMaster ");
            sb.append("ON PY.CashMode = ListId ORDER BY RM.RetailerName, PY.BillNumber,PY.GroupId");
            Cursor c = db.selectSQL(sb.toString());
            if (c != null) {
                if (c.getCount() > 0) {
                    lstPaymentBObyGroupId = new HashMap<>();
                    while (c.moveToNext()) {
                        PaymentBO paybo = new PaymentBO();
                        paybo.setRetailerName(c.getString(0));
                        paybo.setBeatID(c.getString(1));
                        paybo.setInvoiceAmount(c.getDouble(2));
                        paybo.setAmount(c.getDouble(3));
                        paybo.setBalance(c.getDouble(4));
                        paybo.setCashMode(c.getString(5));
                        paybo.setChequeNumber(c.getString(6));
                        paybo.setChequeDate(c.getString(7));
                        paybo.setCollectionDate(c.getString(8));
                        paybo.setBillNumber(c.getString(9));
                        paybo.setInvoiceDate(c.getString(10));
                        paybo.setListName(c.getString(11));
                        paybo.setPreviousPaidAmount(c.getDouble(12));
                        paybo.setAppliedDiscountAmount(c.getDouble(14));
                        paybo.setGroupId(c.getString(15));
                        paybo.setRetailerCode(c.getString(16));
                        paybo.setCollectionDateTime(c.getString(17));
                        paybo.setReferenceNumber(c.getString(18));
                        Commons.print("&&&, " + paybo.getRetailerName() + "  " + paybo.getBillNumber() + " balance :" + paybo.getBalance());

                        if (lstPaymentBObyGroupId.get(paybo.getGroupId()) != null) {
                            lstPaymentBObyGroupId.get(paybo.getGroupId()).add(paybo);
                        } else {
                            ArrayList<PaymentBO> lst = new ArrayList<>();
                            lst.add(paybo);
                            lstPaymentBObyGroupId.put(paybo.getGroupId(), lst);
                        }
                        paymentList.add(paybo);
                    }
                }
                c.close();
            }
            db.closeDB();
            List<PaymentBO> childItemList = null;
            PaymentBO payBOTemp = null;
            for (PaymentBO payBO : paymentList) {
                if (childItemList == null) {
                    childItemList = new ArrayList<>();
                    childItemList.add(payBO);
                    if (!isPaymentBOAvailable(payBO)) {
                        parentPaymentList.add(payBO);
                    }
                    payBOTemp = payBO;
                } else {
                    if (childItemList.get(0).getBillNumber()
                            .equals(payBO.getBillNumber())) {
                        childItemList.add(payBO);
                        payBOTemp = payBO;
                        if (!isPaymentBOAvailable(payBO)) {
                            parentPaymentList.add(payBO);
                        }
                    } else {
                        childPaymentList.add(childItemList);
                        childItemList = new ArrayList<>();
                        childItemList.add(payBO);
                        payBOTemp = payBO;
                        if (!isPaymentBOAvailable(payBO)) {
                            parentPaymentList.add(payBO);
                        }
                    }
                }
            }
            if (childItemList != null) {
                if (!isPaymentBOAvailable(payBOTemp)) {
                    parentPaymentList.add(payBOTemp);
                }
                childPaymentList.add(childItemList);
            }

            for (List<PaymentBO> tempChild : childPaymentList) {
                double PaidTotal = 0;
                double totalAppliedDiscount = 0;
                String invoiceNo = null;
                boolean flag = true;
                for (PaymentBO tempPaymentChild : tempChild) {
                    if (flag) {
                        flag = false;
                        invoiceNo = tempPaymentChild.getBillNumber();
                    }
                    PaidTotal = PaidTotal + tempPaymentChild.getAmount();
                    totalAppliedDiscount = totalAppliedDiscount + tempPaymentChild.getAppliedDiscountAmount();
                }
                final double balance = updateOutStanding(invoiceNo, PaidTotal, totalAppliedDiscount);
                for (PaymentBO tempPaymentChild : tempChild) {
                    tempPaymentChild.setBalance(balance);
                }
            }

            // To download advance payment
            downloadAdvanePaymentForReport();

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private void downloadAdvanePaymentForReport() {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        ArrayList<PaymentBO> lstAdvancePayment = null;
        try {
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select id,CN.amount,RM.retailerName,CN.date,SM.listname,RM.RetailerCode,PM.GroupId,SM.Listcode" +
                            ",PM.ChequeDate, PM.ChequeNumber from creditNote CN" +
                            " inner join Payment PM on PM.uid=CN.refno" +
                            " left join StandardListMaster SM on PM.cashMode=SM.listid" +
                            " left join RetailerMaster RM on RM.retailerid=CN.retailerID" +
                            " where id like 'AP%' and CN.date=" + bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL)));
            if (c != null) {
                PaymentBO bo;
                lstAdvancePayment = new ArrayList<>();
                while (c.moveToNext()) {
                    bo = new PaymentBO();
                    bo.setAdvancePaymentId(c.getString(0));
                    bo.setAmount(c.getDouble(1));
                    bo.setRetailerName(c.getString(2));
                    bo.setAdvancePaymentDate(c.getString(3));
                    bo.setListName(c.getString(4));
                    bo.setRetailerCode(c.getString(5));
                    bo.setGroupId(c.getString(6));
                    bo.setCashMode(c.getString(7));
                    bo.setChequeDate(c.getString(8));
                    bo.setChequeNumber(c.getString(9));

                    if (lstPaymentBObyGroupId == null)
                        lstPaymentBObyGroupId = new HashMap<>();
                    if (lstPaymentBObyGroupId.get(bo.getGroupId()) != null) {
                        lstPaymentBObyGroupId.get(bo.getGroupId()).add(bo);
                    } else {
                        ArrayList<PaymentBO> lst = new ArrayList<>();
                        lst.add(bo);
                        lstPaymentBObyGroupId.put(bo.getGroupId(), lst);
                    }

                    lstAdvancePayment.add(bo);
                }
                c.close();
            }
            db.closeDB();


            // If there is a advance payment then it is added to existing list
            // and list sorted by retailer name
            if (lstAdvancePayment != null && lstAdvancePayment.size() > 0) {
                if (parentPaymentList == null)
                    parentPaymentList = new ArrayList<>();

                parentPaymentList.addAll(lstAdvancePayment);

                Collections.sort(parentPaymentList, PaymentBO.RetailerNameComparator);

                // To add corresponding child items for parent list
                List<List<PaymentBO>> lstTempChild = new ArrayList<>();
                for (PaymentBO bo : parentPaymentList) {
                    if (bo.getAdvancePaymentId() != null && bo.getAdvancePaymentId().startsWith("AP")) {
                        List<PaymentBO> lst = new ArrayList<>();
                        lstTempChild.add(lst);
                    } else {
                        for (List<PaymentBO> list : childPaymentList) {
                            if (bo.getBillNumber().equals(list.get(0).getBillNumber())) {
                                lstTempChild.add(list);
                                break;
                            }

                        }
                    }
                }

                childPaymentList.clear();
                childPaymentList.addAll(lstTempChild);

            }


        } catch (Exception e) {
            Commons.printException(e);
            if (db != null)
                db.closeDB();
        }
    }

    public ArrayList<SpinnerBO> downloadCollectionReportRetailer() {
        ArrayList<SpinnerBO> retailers = null;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT distinct py.retailerid,rm.retailername from payment py inner join retailermaster rm on rm.retailerid=py.retailerid");
            if (c != null) {
                retailers = new ArrayList<>();
                while (c.moveToNext()) {

                    retailers.add(new SpinnerBO(c.getInt(0), c.getString(1)));
                }
                c.close();
            }
            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);
        }
        return retailers;
    }

    public ArrayList<SalesReturnReasonBO> getSalesReturnList(String productId, String retailerId) {
        ArrayList<SalesReturnReasonBO> salesReturnReasonBOs = new ArrayList<>();
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT distinct A.ListName as reasonDesc,srd .* from SalesReturnDetails srd"
                            + " inner join StandardListMaster A INNER JOIN StandardListMaster B ON"
                            + " A.ParentId = B.ListId AND"
                            + " ( B.ListCode = '" + StandardListMasterConstants.SALES_RETURN_NONSALABLE_REASON_TYPE
                            + "' OR B.ListCode = '" + StandardListMasterConstants.SALES_RETURN_SALABLE_REASON_TYPE + "')"
                            + " AND A.listId = srd.condition WHERE srd.upload!='X' and A.ListType = 'REASON' AND"
                            + " srd.ProductId = " + bmodel.QT(productId) + " AND srd.RetailerId = " + bmodel.QT(retailerId)
                            + " AND srd.status = 2");
            if (c != null) {
                while (c.moveToNext()) {
                    SalesReturnReasonBO reasonBO = new SalesReturnReasonBO();
                    reasonBO.setInvoiceno(c.getString(c.getColumnIndex("invoiceno")));
                    reasonBO.setLotNumber(c.getString(c.getColumnIndex("LotNumber")));
                    reasonBO.setSrpedit(c.getFloat(c.getColumnIndex("srpedited")));
                    reasonBO.setPieceQty(c.getInt(c.getColumnIndex("totalQty")));
                    reasonBO.setOldMrp(c.getInt(c.getColumnIndex("oldmrp")));
                    reasonBO.setReasonDesc(c.getString(c.getColumnIndex("reasonDesc")));
                    salesReturnReasonBOs.add(reasonBO);
                }
                c.close();
            }
            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);
        }
        return salesReturnReasonBOs;
    }

    public ArrayList<SalesReturnReasonBO> getSalesReturnRetailerList() {
        ArrayList<SalesReturnReasonBO> salesReturnReasonBOs = new ArrayList<>();
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select distinct srd.ProductId as ProductId,srd.retailerId as RetailerID," +
                            "pm.pname as ProductName, pm.pcode as ProductCode from SalesReturnDetails srd inner join " +
                            "ProductMaster pm ON srd.ProductID = pm.pid where sr.upload!='X' and srd.status = 2");
            if (c != null) {
                while (c.moveToNext()) {
                    SalesReturnReasonBO reasonBO = new SalesReturnReasonBO();
                    reasonBO.setRetailerId(c.getString(c.getColumnIndex("RetailerID")));
                    reasonBO.setProductId(c.getString(c.getColumnIndex("ProductId")));
                    reasonBO.setProductName(c.getString(c.getColumnIndex("ProductName")));
                    reasonBO.setProductCode(c.getString(c.getColumnIndex("ProductCode")));
                    salesReturnReasonBOs.add(reasonBO);
                }
                c.close();
            }
            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);
        }
        return salesReturnReasonBOs;
    }

    public ArrayList<String> getLstCollectionGroups() {
        return lstCollectionGroups;
    }

    public void setLstCollectionGroups(ArrayList<String> lstCollectionGroups) {
        this.lstCollectionGroups = lstCollectionGroups;
    }

    private ArrayList<String> lstCollectionGroups;

    public ArrayList<String> downloadCollectionReportGroups(int retailerId) {
        ArrayList<String> groups = null;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT distinct groupid, datetime from payment where retailerid='" + retailerId + "'");
            if (c != null) {
                groups = new ArrayList<>();
                lstCollectionGroups = new ArrayList<>();
                while (c.moveToNext()) {
                    groups.add(c.getString(0) + " - " + c.getString(1));
                    lstCollectionGroups.add(c.getString(0));
                }
                c.close();
            }
            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);
        }
        return groups;
    }

    /**
     * This method will download the batch wise SIH from StockInHandMaster.
     */
    public void downloadBatchwiseCurrentStockReport() {
        StockReportBO temp;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT DISTINCT P.pname,P.psname,P.pcode,SIH.qty,P.barcode,P.RField1,IFNULL(BM.batchNum,'') FROM StockInHandMaster SIH LEFT JOIN BatchMaster BM ON SIH.batchid = BM .batchid INNER JOIN ProductMaster P ON P.PID = SIH.pid"
                            + " where P.IsSalable=1 and P.PLid IN (SELECT ProductContent FROM ConfigActivityFilter WHERE ActivityCode='MENU_CURRENT_STOCK_VIEW_BATCH')");
            if (c != null) {
                Vector<StockReportBO> batchWiseCurrentStock = new Vector<>();
                while (c.moveToNext()) {
                    temp = new StockReportBO();
                    temp.setProductName(c.getString(0));
                    temp.setProductShortName(c.getString(1));
                    temp.setProductCode(c.getString(2));
                    temp.setSih(c.getInt(3));
                    temp.setBarcode(c.getString(4));
                    temp.setRfield1(c.getString(5));
                    temp.setBatchNo(c.getString(6));
                    batchWiseCurrentStock.add(temp);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public void loadQuestionReport() {
        QuestionReportBO chkReportBo;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT Text, V1,V2,V3,V4 FROM L3SuperwiserAuditReport ");
            if (c != null) {
                questionReport = new Vector<>();
                while (c.moveToNext()) {
                    chkReportBo = new QuestionReportBO();
                    chkReportBo.setText(c.getString(0));
                    chkReportBo.setV1(c.getInt(1));
                    chkReportBo.setV2(c.getInt(2));
                    chkReportBo.setV3(c.getInt(3));
                    chkReportBo.setV4(c.getInt(4));
                    questionReport.add(chkReportBo);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public Vector<QuestionReportBO> getQuestionReport() {
        return questionReport;

    }

    private double updateOutStanding(String invoiceNo, double paidTotal, double totalAppliedDiscount) {
        double balance = 0;
        try {
            for (PaymentBO tempParentBO : parentPaymentList) {
                if (tempParentBO.getBillNumber().equals(invoiceNo)) {
                    balance = Double.parseDouble(SDUtil.format((tempParentBO.getInvoiceAmount()
                            - paidTotal - tempParentBO.getPreviousPaidAmount() - totalAppliedDiscount), 2, 0));
                    tempParentBO.setBalance(balance);
                    break;
                }
            }


        } catch (Exception e) {
            Commons.printException(e);
        }
        return balance;
    }

    private boolean isPaymentBOAvailable(PaymentBO payBOTemp) {
        try {
            for (PaymentBO pbo : parentPaymentList) {
                if (pbo.getBillNumber().equals(payBOTemp.getBillNumber()))
                    return true;
            }
        } catch (Exception e) {
            Commons.printException(e + "");
        }
        return false;
    }

    public List<PaymentBO> getParentPaymentList() {
        return parentPaymentList;
    }

    public void setParentPaymentList(List<PaymentBO> parentPaymentList) {
        this.parentPaymentList = parentPaymentList;
    }

    public List<List<PaymentBO>> getChildPaymentList() {
        return childPaymentList;
    }

    public void setChildPaymentList(List<List<PaymentBO>> childPaymentList) {
        this.childPaymentList = childPaymentList;
    }

    private Vector<StockReportBO> getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(Vector<StockReportBO> currentStock) {
        this.currentStock = currentStock;
    }

    public ArrayList<PaymentBO> getPaymentList() {
        return paymentList;
    }

    public void setPaymentList(ArrayList<PaymentBO> paymentList) {
        this.paymentList = paymentList;
    }

    private ArrayList<TaskReportBo> taskretailerinfo = new ArrayList<>();

    public ArrayList<TaskReportBo> getTaskretailerinfo() {
        return taskretailerinfo;
    }

    private void setTaskretailerinfo(ArrayList<TaskReportBo> taskretailerinfo) {
        this.taskretailerinfo = taskretailerinfo;
    }

    /**
     * load data if module completed or not.
     */
    public void downloadTaskExecutionReport() {
        try {
            ArrayList<SpinnerBO> retailerList = new ArrayList<>();
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("select Retailerid,MENU_CODE from ModuleCompletionReport");

            Cursor c = db.selectSQL(sb.toString());

            if (c != null) {
                setTaskretailerinfo(new ArrayList<TaskReportBo>());
                while (c.moveToNext()) {
                    retailerList.add(new SpinnerBO(c.getInt(0), c.getString(1)));
                }
                c.close();
            }
            db.closeDB();

            TaskReportBo outlet;
            for (RetailerMasterBO retailerMasterBO : bmodel.getRetailerMaster()) {
                if (retailerMasterBO.getIsToday() == 1
                        || retailerMasterBO.getIsDeviated().equals("Y")) {
                    outlet = new TaskReportBo();
                    outlet.setmRetailerId(Integer.parseInt(retailerMasterBO.getRetailerID()));
                    outlet.setmRetailerName(retailerMasterBO.getRetailerName());
                    if (retailerList.size() > 0) {
                        HashMap<String, Boolean> menuMap = new HashMap<>();
                        for (SpinnerBO retaielers : retailerList) {
                            if (outlet.getmRetailerId() == retaielers.getId()) {
                                menuMap.put(retaielers.getSpinnerTxt(), true);
                            }
                        }
                        outlet.setmMenuCodeMap(menuMap);
                    }
                    taskretailerinfo.add(outlet);
                }
            }


        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    // Task Execution report
    private ArrayList<BeatMasterBO> beatinfo = new ArrayList<>();

    public ArrayList<BeatMasterBO> getBeatinfo() {
        return beatinfo;
    }

    public void setBeatinfo(ArrayList<BeatMasterBO> beatinfo) {
        this.beatinfo = beatinfo;
    }

    public void downloadBeatNames() {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.openDataBase();
        beatinfo.clear();

        Cursor c = db
                .selectSQL("select distinct BM.beatid,BM.BeatDescription from ModuleCompletionReport MCR INNER JOIN RetailerMaster RM on MCR.retailerid = RM.retailerid "
                        + " INNER JOIN BeatMaster BM on BM.beatid = RM.beatid");

        if (c != null) {
            while (c.moveToNext()) {
                BeatMasterBO beat;
                beat = new BeatMasterBO();
                beat.setBeatId(c.getInt(0));
                beat.setBeatDescription(c.getString(1));
                beatinfo.add(beat);
            }
            c.close();
        }
        db.closeDB();
    }

    /**
     * delete from invoice and order table for selected invoice report
     *
     * @param invoiceBO
     */
    public void deleteInvoiceDetail(InvoiceReportBO invoiceBO) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            updateSIH(invoiceBO, db);
            updateSchemeCount(invoiceBO, db);
            db.deleteSQL(DataMembers.tbl_InvoiceMaster,
                    "invoiceNo=" + bmodel.QT(invoiceBO.getInvoiceNumber())
                            + " and upload='N'", false);
            db.deleteSQL(DataMembers.tbl_InvoiceDetails,
                    "invoiceid=" + bmodel.QT(invoiceBO.getInvoiceNumber())
                            + " and upload='N'", false);
            db.deleteSQL(DataMembers.tbl_orderHeader,
                    "orderid=" + bmodel.QT(invoiceBO.getOrderID())
                            + " and upload='N'", false);
            db.deleteSQL(DataMembers.tbl_orderDetails,
                    "orderid=" + bmodel.QT(invoiceBO.getOrderID())
                            + " and upload='N'", false);

            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    /**
     * reset schemecount in SchemeMaster and schemeApplyCountMaster after delete
     * invoice
     *
     * @param invoiceBO
     * @param db
     */
    private void updateSchemeCount(InvoiceReportBO invoiceBO, DBUtil db) {
        try {

            String query = "Select Distinct SchemeID,RetailerId  From SchemeDetail Where OrderID ="
                    + bmodel.QT(invoiceBO.getOrderID());
            Cursor c = db.selectSQL(query);
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    String schemeid = c.getString(0);
                    String retailerid = c.getString(1);

                    String updateApplyCountQuery = "update SchemeApplyCountMaster set schemeApplyCount=schemeApplyCount+1 where schemeid="
                            + bmodel.QT(schemeid) + " and schemeApplyCount!=-1";
                    db.updateSQL(updateApplyCountQuery);

                    String parentidQuery = "select parentid from schememaster where SubChannelID="
                            + bmodel.QT(retailerid)
                            + " and schemeid ="
                            + bmodel.QT(schemeid) + " and count!=-1";
                    Cursor parentCursor = db.selectSQL(parentidQuery);
                    if (parentCursor.getCount() > 0) {
                        while (parentCursor.moveToNext()) {
                            int parentid = parentCursor.getInt(0);
                            String updateSchemeCountQuery = "update SchemeMaster set count=count+1 where SubChannelID="
                                    + bmodel.QT(retailerid)
                                    + " and parentid ="
                                    + parentid + " and count!=-1";
                            db.updateSQL(updateSchemeCountQuery);

                        }
                    }

                }
                c.close();

            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    /**
     * update sih value in productmaster and stockinhand master
     *
     * @param invoiceBO
     * @param db
     */
    private void updateSIH(InvoiceReportBO invoiceBO, DBUtil db) {
        try {

            String query = "select productid,Qty,batchid from invoiceDetails where invoiceid="
                    + bmodel.QT(invoiceBO.getInvoiceNumber());
            Cursor c = db.selectSQL(query);
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    String productID = c.getString(0);
                    int qty = c.getInt(1);
                    int batchid = c.getInt(2);
                    updateProductSIH(productID, batchid, qty, db);
                }
            }
            c.close();

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    /**
     * update sih value in productmaster and Stockinhandmaster for given
     * productid
     *
     * @param productID
     * @param batchid
     * @param qty
     * @param db
     */
    private void updateProductSIH(String productID, int batchid, int qty,
                                  DBUtil db) {
        db.updateSQL("update productmaster set sih=sih+" + qty + " where pid="
                + bmodel.QT(productID));
        db.updateSQL("update StockInHandMaster set upload='N',qty=qty+" + qty
                + " where pid=" + bmodel.QT(productID) + " and batchid="
                + batchid);
    }

    /**
     * Method to load EOD report details
     */
    public void downloadEODReport() {
        ArrayList<StockReportBO> tempStockReport = null;
        mEODStockReportList = new ArrayList<>();
        mEODReportBOByProductID = new HashMap<>();
        try {
            StockReportBO stockReportBO;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            StringBuffer sb = new StringBuffer();

            sb.append("SELECT PM.PID, PM.PName");
            sb.append(",PM.duomqty,PM.dOuomQty,PM.piece_uomid,PM.duomid,PM.dOuomid  FROM ProductMaster PM");
            sb.append(" Where PM.TypeId NOT IN (SELECT ListID FROM StandardListMaster WHERE  ListCode ='GENERIC') ORDER BY PM.PID");

            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                tempStockReport = new ArrayList<>();
                while (c.moveToNext()) {
                    stockReportBO = new StockReportBO();
                    stockReportBO.setProductID(c.getString(0));
                    stockReportBO.setProductName(c.getString(1));
                    stockReportBO.setCaseSize(c.getInt(2));
                    stockReportBO.setOuterSize(c.getInt(3));
                    stockReportBO.setPiece_uomid(c.getInt(4));
                    stockReportBO.setdUomid(c.getInt(5));
                    stockReportBO.setdOuomid(c.getInt(6));


                    tempStockReport.add(stockReportBO);
                }
            }

            // Duplicating product batchwise
            if (tempStockReport != null) {
                HashMap<String, ArrayList<ProductMasterBO>> mBatchList = downloadBatchList();
                for (StockReportBO bo : tempStockReport) {
                    if (mBatchList.get(bo.getProductID()) != null) {

                        for (ProductMasterBO batchBO : mBatchList.get(bo.getProductID())) {

                            StockReportBO newBO = new StockReportBO(bo);
                            newBO.setBatchNo(batchBO.getBatchNo());
                            newBO.setBatchId(batchBO.getBatchid());
                            mEODStockReportList.add(newBO);

                        }

                    } else {
                        mEODStockReportList.add(bo);
                    }
                }
                tempStockReport = null;
            }
            //
            c.close();

            // get vanload qty
            sb = new StringBuffer();
            sb.append("SELECT PID,batchid,Qty  FROM StockInHandMaster");
            c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {

                    int qty = c.getInt(2);
                    int batchid = c.getInt(1);

                    for (StockReportBO bo : mEODStockReportList) {
                        if (batchid == 0) {

                            if (bo.getProductID().equals(c.getString(0))) {
                                bo.setSih(bo.getSih() + qty);
                            }
                        } else {
                            if (bo.getProductID().equals(c.getString(0)) && bo.getBatchId() != null && bo.getBatchId().equals(batchid + "")) {

                                bo.setSih(bo.getSih() + qty);
                            }
                        }
                    }

                }
            }
            c.close();


            //load return qty
            sb = new StringBuffer();
            sb.append("select SD.Productid,sum(SD.totalQty),SD.batchid from SalesReturnDetails SD  inner join SalesReturnHeader SH on ");
            sb.append("SD.uid=SH.uid inner join Standardlistmaster SLM on SLM.listid=SD.condition  AND  SLM.ListType = 'REASON'");
            sb.append(" inner join Standardlistmaster SLM1 on SLM.parentid=SLM1.listid and SLM1.ListCode='SRS' where SH.upload!='X'");
            sb.append(" group by SD.Productid,SD.batchid");
            c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    setEODObject(c.getString(0), c.getString(2), c.getInt(1), QtyType.RETURN);
                }
            }
            c.close();

            // get OrderQty(sold) FROM ORDER DETAIL
            sb = new StringBuffer();
            sb.append("select productid,sum(Qty),batchid as Qty from orderdetail OD inner join Orderheader OH on ");
            sb.append("OH.orderid =OD.orderid where OH.is_vansales=1 and od.ordertype=0 and OH.upload!='X' group by productid,batchid");
            c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    setEODObject(c.getString(0), c.getString(2), c.getInt(1), QtyType.SOLD);
                }
            }
            c.close();

            // get freeQty for crown FROM ORDER DETAIL
            sb = new StringBuffer();
            sb.append("select OD.Productid,sum(OD.Qty),batchid from orderDetail OD  inner join OrderHeader OH on ");
            sb.append("OD.orderid=OH.orderid where OH.upload!='X' and OH.is_vansales=1 and OD.Ordertype!=0 group by OD.Productid,batchid ");
            c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    setEODObject(c.getString(0), c.getString(2), c.getInt(1), QtyType.FREE_ISSUED);
                }
            }
            c.close();

            // get freeQty for scheme free product from SchemeFreeProductDetail
            sb = new StringBuffer();
            sb.append("select FreeProductid,sum(case when uomCount!=0 then FreeQty*UomCount else FreeQty end) as Qty,batchid");
            sb.append(" from SchemeFreeproductDetail group by FreeProductid,batchid");
            c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    setEODObject(c.getString(0), c.getString(2), c.getInt(1), QtyType.FREE_ISSUED);
                }
            }
            c.close();

            //load replacement qty
            sb = new StringBuffer();
            sb.append("select pid,sum(case when uomCount!=0 then qty*UomCount else qty end) as Qty,batchid from SalesReturnReplacementDetails where upload!='X' group by pid,batchid");
            c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    setEODObject(c.getString(0), c.getString(2), c.getInt(1), QtyType.REPLACEMENT);
                }
            }
            c.close();

            // Empty qty from VanUnloadDetails and EmptyReconcilationDetail
            sb = new StringBuffer();
            sb.append("SELECT ED.PId, SUM(ED.Qty) AS Qty, VUD.QTY FROM EmptyReconciliationDetail ED Left Join ");
            sb.append("(SELECT pid, SUM(pcsqty) AS QTY  FROM VanUnloadDetails ");
            sb.append("WHERE SubDepotID != 0 Group By pid) as VUD on VUD.pid=ED.pid group by ED.pid");
            c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    String porductid = c.getString(0);
                    int eodQty = c.getInt(1);
                    int unloadQty = c.getInt(2);
                    int qty = eodQty - unloadQty;
                    setEODObject(porductid, "0", qty, QtyType.EMPTY);
                }
            }
            c.close();

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private enum QtyType {
        RETURN(1),
        SOLD(2),
        FREE_ISSUED(3),
        REPLACEMENT(4),
        EMPTY(5);
        private int value;

        QtyType(int value) {
            this.value = value;
        }
    }

    /**
     * method to set EOD object for corresponding Product
     *
     * @param productid
     * @param qty
     * @param type
     */
    private void setEODObject(String productid, String batchid, int qty, QtyType type) {

        String lastProductid = "";
        for (StockReportBO stockReportBO : mEODStockReportList) {
            if (stockReportBO != null) {
                if (batchid.equals("0")) {
                    if (stockReportBO.getProductID().equals(productid)) {

                        // To set value to first batch only
                        if (!lastProductid.equals(productid)) {

                            if (type == QtyType.FREE_ISSUED) {
                                // added free qty,crown and free issues
                                stockReportBO.setFreeIssuedQty(qty
                                        + stockReportBO.getFreeIssuedQty());
                            } else if (type == QtyType.SOLD) {
                                stockReportBO.setSoldQty(qty);
                            } else if (type == QtyType.EMPTY) {
                                stockReportBO.setEmptyBottleQty(qty);
                            } else if (type == QtyType.REPLACEMENT) {
                                stockReportBO.setReplacementQty(qty);
                            } else if (type == QtyType.RETURN) {
                                stockReportBO.setReturnQty(qty);
                            }
                            lastProductid = productid;
                        }

                    }
                } else {
                    if (stockReportBO.getProductID().equals(productid)
                            && stockReportBO.getBatchId().equals(batchid)) {

                        if (type == QtyType.FREE_ISSUED) {
                            // added free qty,crown and free issues
                            stockReportBO.setFreeIssuedQty(qty
                                    + stockReportBO.getFreeIssuedQty());
                        } else if (type == QtyType.SOLD) {
                            stockReportBO.setSoldQty(qty);
                        } else if (type == QtyType.EMPTY) {
                            stockReportBO.setEmptyBottleQty(qty);
                        } else if (type == QtyType.REPLACEMENT) {
                            stockReportBO.setReplacementQty(qty);
                        } else if (type == QtyType.RETURN) {
                            stockReportBO.setReturnQty(qty);
                        }


                    }
                }

            }
        }

    }

    public HashMap<String, ArrayList<ProductMasterBO>> downloadBatchList() {
        HashMap<String, ArrayList<ProductMasterBO>> mBatchListByproductID = new HashMap<>();
        ProductMasterBO productBO;
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        try {

            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("select  PM.pid, PM.pcode,PM.pname,PM.psname,BM.batchid,BM.batchNum,BM.MfgDate,BM.ExpDate");
            sb.append(" from BatchMAster BM inner join  ProductMaster PM on (BM.Pid= PM.pid)");
            sb.append("  order by PM.pid,BM.MfgDate asc");

            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                String productid = "";
                ArrayList<ProductMasterBO> batchList = new ArrayList<ProductMasterBO>();
                while (c.moveToNext()) {
                    productBO = new ProductMasterBO();
                    productBO.setProductID(c.getString(0));
                    productBO.setProductCode(c.getString(1));
                    productBO.setProductName(c.getString(2));
                    productBO.setProductShortName(c.getString(3));

                    productBO.setBatchid(c.getString(4));
                    productBO.setBatchNo(c.getString(5));
                    productBO.setMfgDate(c.getString(6));
                    productBO.setExpDate(c.getString(7));

                    if (!productid.equals(productBO.getProductID())) {
                        if (productid != "") {
                            mBatchListByproductID.put(productid, batchList);
                            batchList = new ArrayList<>();
                            batchList.add(productBO);
                            productid = productBO.getProductID();
                        } else {
                            batchList.add(productBO);
                            productid = productBO.getProductID();
                        }
                    } else {
                        batchList.add(productBO);
                    }

                }

                if (batchList.size() > 0) {
                    mBatchListByproductID.put(productid, batchList);
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            db.closeDB();
            return mBatchListByproductID;
        }
        return mBatchListByproductID;
    }

    public Vector<StockReportBO> downloadCurrentStockReport() {

        try {
            Vector<LoadManagementBO> item = bmodel.productHelper.loadProducts("MENU_LOAD_MANAGEMENT", "");
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

            }
            return currentStock;
        } catch (Exception e) {
            Commons.printException(e);
        }
        return null;
    }

    /// METHODS OF PIRAMAL
    public ArrayList<ReportBrandPerformanceBO> downloadBrandPerformanceReport(String today) {
        ArrayList<ReportBrandPerformanceBO> brandperformancelist = null;
        try {
            ReportBrandPerformanceBO brandPerformanceReport;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT A.PName, ifnull(B.ProductiveCall,0),ifnull(B.ValuePerDay,0),ifnull(B.Lines,0),ifnull(C.Achieve ,0),ifnull(C.Target ,0) FROM ProductMaster A " +
                            "LEFT JOIN (SELECT BM.Pid as BrandId,BM.Pname as Brand,ifnull(count(distinct OD.retailerid),0) as ProductiveCall,ifnull(SUM(OD.totalamount),0) as ValuePerDay,ifnull(SUM(Qty),0) as Lines FROM ProductMaster BM " +
                            "INNER JOIN ProductMaster PM on PM.ParentID = Bm.PId " +
                            "INNER JOIN OrderDetail OD on OD.ProductID = PM.PId " +
                            "INNER JOIN OrderHeader OH ON OH.OrderID = OD.OrderID AND OH.OrderDate = '" + today + "'" +
                            "WHERE BM.PLid = (SELECT LevelId FROM ProductLevel WHERE LevelName = 'Brand')  GROUP BY  BM.Pid,BM.Pname ) AS B on A. Pid = B.BrandId " +
                            "LEFT JOIN (SELECT ST.Pid,ifnull(ST.Ach,0) as Achieve ,ifnull(ST.Tgt,0) as Target  FROM ProductMaster PM " +
                            "INNER JOIN SkuWiseTarget ST ON PM.PID = ST.Pid where PM.PLid = (SELECT LevelId FROM ProductLevel WHERE LevelName = 'Brand')) as C ON C.Pid = A.PID " +
                            "WHERE OH.upload!='X' and A.PLid = (SELECT LevelId FROM ProductLevel WHERE LevelName = 'Brand') ORDER BY A.PName");

            if (c != null) {
                brandperformancelist = new ArrayList<>();
                while (c.moveToNext()) {
                    brandPerformanceReport = new ReportBrandPerformanceBO();
                    brandPerformanceReport.setProductname(c.getString(0));
                    brandPerformanceReport.setProductivecall(c.getInt(1));
                    brandPerformanceReport.setValueperday(c.getFloat(2));
                    brandPerformanceReport.setLines(c.getInt(3));
                    String target = c.getString(5);
                    String ach = c.getString(4);

                    double achievement = (SDUtil.convertToDouble(ach) / SDUtil.convertToDouble(target)) * 100;
                    if (String.valueOf(achievement).equals("0") || String.valueOf(achievement).equals("NaN"))
                        brandPerformanceReport.setTarget_achievement(0);
                    else
                        brandPerformanceReport.setTarget_achievement(SDUtil.convertToFloat(SDUtil.roundIt(achievement, 2)));

                    brandperformancelist.add(brandPerformanceReport);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return brandperformancelist;
    }


    public ArrayList<RetailersReportBO> downloadOppRetailersReport() {
        ArrayList<RetailersReportBO> retailersreportlist = null;
        try {
            RetailersReportBO mRetailersReportBO;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT RM.RetailerName, ifnull(DBM.ach,0)as Value,ifnull(DM.ach,0)as Line FROM RetailerMaster RM " +
                            "INNER JOIN DashboardMaster DM ON DM.RetailerID = RM.RetailerID AND DM.code = 'TLS' " +
                            "INNER JOIN DashboardMaster DBM ON DBM.RetailerID = DM.RetailerID AND DBM.code = 'SV' " +
                            "GROUP BY RM.RetailerName,Value,Line ORDER BY DBM.ach DESC limit 10,(select count(DISTINCT RetailerID) from DashboardMaster)");

            if (c != null) {
                retailersreportlist = new ArrayList<>();
                while (c.moveToNext()) {
                    mRetailersReportBO = new RetailersReportBO();
                    mRetailersReportBO.setmRetailername(c.getString(0));
                    mRetailersReportBO.setmPurchaseValue(c.getFloat(1));
                    mRetailersReportBO.setmLinesAvg(c.getInt(2));
                    retailersreportlist.add(mRetailersReportBO);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return retailersreportlist;
    }

    public ArrayList<OrderTakenTimeBO> downloadDayProcesssReport(String today) {
        ArrayList<OrderTakenTimeBO> orederTimeTakenList = null;
        try {
            OrderTakenTimeBO mOrderTakenTimeBO;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            ArrayList<String> timelist = new ArrayList<>();
            timelist.add("10-12");
            timelist.add("12-14");
            timelist.add("14-16");
            timelist.add("16-18");

            StringBuffer sql = new StringBuffer();
            String[] time;
            String starttime;
            String endtime;
            String looptime;

            if (!timelist.isEmpty()) {
                time = timelist.get(0).split("-");
                starttime = String.valueOf(Integer.parseInt(time[0].trim()) - 1);
                if (starttime.length() == 1) {
                    starttime = "0" + starttime;
                }
                sql.append("SELECT 'Before " + time[0].trim() + ":00' as PERIOD,(SELECT count(DISTINCT RetailerID)  FROM OutletTimestamp  WHERE VisitDate = '" + today + "' AND time(replace(TimeIn,'/','-')) BETWEEN '00:00:00' and '" + starttime + ":59:59') as TC,count(RetailerID) as PC,ifnull(SUM(OrderValue),0) as Value,ifnull(SUM(LinesPerCall),0) as LinesSold FROM OrderHeader WHERE upload!='X' and OrderDate = '" + today + "' AND time(replace(orderTakenTime,'/','-')) BETWEEN '00:00:00' and '" + starttime + ":59:59'");


                for (int i = 0; i < timelist.size(); i++) {
                    time = timelist.get(i).split("-");
                    looptime = String.valueOf(Integer.parseInt(time[1].trim()) - 1);
                    if (looptime.length() == 1) {
                        looptime = "0" + looptime;
                    }
                    sql.append("UNION SELECT '" + time[0].trim() + ":00 - " + String.valueOf(Integer.parseInt(looptime) + 1) + ":00' as PERIOD,(SELECT count(DISTINCT RetailerID)  FROM OutletTimestamp  WHERE VisitDate = '" + today + "' AND time(replace(TimeIn,'/','-')) BETWEEN '" + time[0].trim() + ":00:00' and '" + looptime + ":59:59') as TC,count(*) as PC,ifnull(SUM(OrderValue),0) as Value,ifnull(SUM(LinesPerCall),0) as LinesSold FROM OrderHeader WHERE upload!='X' and OrderDate = '" + today + "' AND time(replace(orderTakenTime,'/','-')) BETWEEN '" + time[0].trim() + ":00:00' and '" + looptime + ":59:59'");


                }

                time = timelist.get(timelist.size() - 1).split("-");
                endtime = String.valueOf(Integer.parseInt(time[1].trim()));
                if (endtime.length() == 1) {
                    endtime = "0" + endtime;
                }
                sql.append("UNION SELECT 'After " + time[1].trim() + ":00' as PERIOD,(SELECT count(DISTINCT RetailerID)  FROM OutletTimestamp  WHERE VisitDate = '" + today + "' AND time(replace(TimeIn,'/','-')) BETWEEN '" + endtime + ":00:00' and '23:59:59') as TC,count(*) as PC,ifnull(SUM(OrderValue),0) as Value,ifnull(SUM(LinesPerCall),0) as LinesSold FROM OrderHeader WHERE upload!='X' and OrderDate = '" + today + "' AND time(replace(orderTakenTime,'/','-')) BETWEEN '" + endtime + ":00:00' and '23:59:59'");
            }

            Cursor c = db.selectSQL(sql.toString());

            if (c != null) {
                orederTimeTakenList = new ArrayList<>();
                while (c.moveToNext()) {
                    mOrderTakenTimeBO = new OrderTakenTimeBO();
                    mOrderTakenTimeBO.setmTimePeriod(c.getString(0));
                    mOrderTakenTimeBO.setmTotalcall(c.getInt(1));
                    mOrderTakenTimeBO.setmProductiveCall(c.getInt(2));
                    mOrderTakenTimeBO.setmLinesSold(c.getInt(4));
                    mOrderTakenTimeBO.setmValues(c.getFloat(3));
                    orederTimeTakenList.add(mOrderTakenTimeBO);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return orederTimeTakenList;
    }

    public ArrayList<ProductivityReportBO> downloadProductivitysReport(String today) {
        ArrayList<ProductivityReportBO> mProductivityReportList = null;
        try {
            ProductivityReportBO mProductivityReportBO;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT ListName,ifnull(COUNT(DISTINCT OT.RetailerID),0) as TC,ifnull(TotalCount,0) as PC,ifnull(TotalOrder,0) as " +
                            "Value FROM StandardListMaster SM " +
                            "INNER JOIN RetailerMaster RM ON RM.classid = SM.ListId " +
                            "LEFT JOIN (SELECT RM.ClassId,COUNT(DISTINCT OH.RetailerId) TotalCount,SUM(OH.OrderValue) TotalOrder FROM RetailerMaster RM " +
                            "INNER JOIN OrderHeader OH ON OH.RetailerID = RM.RetailerID  AND OH.OrderDate = '" + today + "' GROUP BY RM.ClassId) OD ON OD.ClassId=RM.ClassId " +
                            "LEFT JOIN OutletTimestamp OT ON OT.RetailerID = RM.RetailerID AND OT.VisitDate = '" + today + "' " +
                            "WHERE OH.upload!='X' and SM.ListType = 'CLASS_TYPE' GROUP BY SM.ListId");

            if (c != null) {
                mProductivityReportList = new ArrayList<>();
                while (c.moveToNext()) {
                    mProductivityReportBO = new ProductivityReportBO();
                    mProductivityReportBO.setmClassName(c.getString(0));
                    mProductivityReportBO.setmTotalCall(c.getInt(1));
                    mProductivityReportBO.setmProductiveCall(c.getInt(2));
                    mProductivityReportBO.setmOrderValue(c.getFloat(3));
                    mProductivityReportList.add(mProductivityReportBO);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return mProductivityReportList;
    }

    public ArrayList<RetailersReportBO> downloadRetailersReport() {
        ArrayList<RetailersReportBO> retailersreportlist = null;
        try {
            RetailersReportBO mRetailersReportBO;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT DISTINCT RM.RetailerName, ifnull(DBM.ach,0)as Value,ifnull(DM.ach,0)as Line FROM RetailerMaster RM " +
                            "INNER JOIN DashboardMaster DM ON DM.RetailerID = RM.RetailerID AND DM.code = 'TLS' " +
                            "INNER JOIN DashboardMaster DBM ON DBM.RetailerID = DM.RetailerID AND DBM.code = 'SV' " +
                            "GROUP BY RM.RetailerName,Value,Line ORDER BY DBM.ach DESC limit 10");

            if (c != null) {
                retailersreportlist = new ArrayList<>();
                while (c.moveToNext()) {
                    mRetailersReportBO = new RetailersReportBO();
                    mRetailersReportBO.setmRetailername(c.getString(0));
                    mRetailersReportBO.setmPurchaseValue(c.getFloat(1));
                    mRetailersReportBO.setmLinesAvg(c.getInt(2));
                    retailersreportlist.add(mRetailersReportBO);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return retailersreportlist;
    }

    public void downloadLogReport() {
        mLogReportList = new ArrayList<>();
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            StringBuffer sb = new StringBuffer();

            sb.append("SELECT TimeIn from AttendanceDetail ");
            sb.append(" Where DateIn = " + bmodel.QT(getTodayDate()));

            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    LogReportBO logReportBO = new LogReportBO();
                    logReportBO.setRetailerName("Day Started");
                    logReportBO.setInTime("");
                    String time[] = c.getString(0).split(" ");
                    logReportBO.setOutTime(time[1]);
                    logReportBO.setInterval(false);

                    mLogReportList.add(logReportBO);
                }
            }

            c.close();

            // get Break details of the user
            sb = new StringBuffer();
            sb.append("select RM.ListName,AT.inTime,AT.outTime from AttendanceTimeDetails AT ");
            sb.append("inner join StandardListMaster RM on RM.ListId= AT.reasonid ");
            sb.append(" where AT.date = " + bmodel.QT(getTodayDate()));

            c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    LogReportBO logReportBO = new LogReportBO();
                    logReportBO.setRetailerName(c.getString(0));
                    String[] time1 = c.getString(1).split(" ");
                    logReportBO.setInTime(time1[1]);
                    String[] time2 = c.getString(2).split(" ");
                    logReportBO.setOutTime(time2[1]);
                    logReportBO.setInterval(true);

                    mLogReportList.add(logReportBO);
                }
            }
            c.close();

            // get Retailer wise activity log of the of the user
            sb = new StringBuffer();
            sb.append("select distinct RM.RetailerName,OT.TimeIn,OT.TimeOut from OutletTimestamp OT ");
            sb.append("inner join RetailerMaster RM on RM.RetailerID= OT.RetailerID ");
            sb.append(" where OT.VisitDate = " + bmodel.QT(getTodayDate()));

            c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    LogReportBO logReportBO = new LogReportBO();
                    logReportBO.setRetailerName(c.getString(0));
                    String[] time1 = c.getString(1).split(" ");
                    logReportBO.setInTime(time1[1]);
                    String[] time2 = c.getString(2).split(" ");
                    logReportBO.setOutTime(time2[1]);
                    logReportBO.setInterval(false);

                    mLogReportList.add(logReportBO);
                }
            }
            c.close();

            sb = new StringBuffer();
            sb.append("SELECT TimeOut from DayClose ");
            sb.append("where status = 1");

            c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    LogReportBO logReportBO = new LogReportBO();
                    logReportBO.setRetailerName("Day Closed");
                    logReportBO.setInTime("");
                    String[] time = c.getString(0).split(" ");
                    logReportBO.setOutTime(time[1]);
                    logReportBO.setInterval(false);

                    mLogReportList.add(logReportBO);
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    private String getTodayDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
        Date date = new Date();
        return formatter.format(date.getTime());
    }

    public String getWebViewPlanUrl() {
        return webViewPlanUrl;
    }

    public void setWebViewPlanUrl(String webViewPlanUrl) {
        this.webViewPlanUrl = webViewPlanUrl;
    }

    public void downloadWebViewPlanAuthUrl(String listType) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            webViewAuthUrl = "";
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select ListName from StandardListMaster where ListCode='URL' AND ListType = " + bmodel.QT(listType));
            if (c != null) {
                if (c.moveToNext()) {
                    webViewAuthUrl = c.getString(0);
                }
                c.close();
            }

            if (!"".equals(webViewAuthUrl)) {
                Cursor c1 = db
                        .selectSQL("select ListName from StandardListMaster where ListCode='ACTION_AUTH' AND ListType = " + bmodel.QT(listType));
                if (c1 != null) {
                    if (c1.moveToNext()) {
                        webViewAuthUrl += c1.getString(0);
                    }
                    c1.close();
                }
            }
            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
            webViewAuthUrl = "";
        }
    }

    public String getWebViewAuthUrl() {
        return webViewAuthUrl;
    }

    public void setWebViewAuthUrl(String webViewAuthUrl) {
        this.webViewAuthUrl = webViewAuthUrl;
    }

    private String webViewAuthUrl = "";

    public void downloadWebViewPlanUrl(String listType) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();
            webViewPlanUrl = "";
            Cursor c = db
                    .selectSQL("select ListName from StandardListMaster where ListCode='URL' AND ListType = " + bmodel.QT(listType));
            if (c != null) {
                if (c.moveToNext()) {
                    webViewPlanUrl = c.getString(0);
                }
                c.close();
            }

            if (!"".equals(webViewPlanUrl)) {
                Cursor c1 = db
                        .selectSQL("select ListName from StandardListMaster where ListCode='ACTION_PLAN' AND ListType = " + bmodel.QT(listType));
                if (c1 != null) {
                    while (c1.moveToNext()) {
                        webViewPlanUrl += c1.getString(0);
                    }
                    c1.close();
                }
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
            webViewPlanUrl = "";
        }
    }

    public void downloadWebViewReportUrl(String menuCode) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
        db.openDataBase();
        Cursor c = db
                .selectSQL("select ListName from StandardListMaster where ListCode='URL' AND ListType = 'WEBVIEW_REPORTS'");
        if (c != null) {
            if (c.moveToNext()) {
                webReportUrl = c.getString(0);
            }
            c.close();
        }

        if (!"".equals(webReportUrl)) {
            Cursor c1 = db
                    .selectSQL("select ListName from StandardListMaster where ListCode='" + menuCode + "' AND ListType = 'WEBVIEW_REPORTS'");
            if (c1 != null) {
                while (c1.moveToNext()) {
                    webReportUrl += c1.getString(0);
                }
                c1.close();
            }
        }

        db.closeDB();
    }

    public String getWebReportUrl() {
        return webReportUrl;
    }

    public void setWebReportUrl(String webReportUrl) {
        this.webReportUrl = webReportUrl;
    }

    public ArrayList<CreditNoteListBO> getCreditNoteList() {
        return creditNoteList;
    }

    public void setCreditNoteList(ArrayList<CreditNoteListBO> creditNoteList) {
        this.creditNoteList = creditNoteList;
    }

    public ArrayList<AttendanceReportBo> getAttendanceList() {

        return attendanceList;
    }

    public void setAttendanceList(ArrayList<AttendanceReportBo> attendanceList) {
        this.attendanceList = attendanceList;
    }

    public ArrayList<String> getAttendanceMonth() {
        return attendanceMonth;
    }

    public void setAttendanceMonth(ArrayList<String> attendanceMonth) {
        this.attendanceMonth = attendanceMonth;
    }

    public void loadCreditNote() {
        try {
            creditNoteList = new ArrayList<>();
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT CN.id,CN.amount,RM.RetailerName,isused FROM CreditNote CN INNER JOIN RetailerMaster RM ON RM.retailerid=CN.retailerid");
            if (c != null) {
                while (c.moveToNext()) {
                    CreditNoteListBO obj = new CreditNoteListBO();
                    obj.setId(c.getString(0));
                    obj.setAmount(c.getDouble(1));
                    obj.setRetailerName(c.getString(2));

                    boolean flag;
                    flag = c.getInt(3) == 1;
                    if (flag)
                        obj.setUsed(true);
                    else
                        obj.setUsed(false);

                    creditNoteList.add(obj);
                }
                c.close();
            }
            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e + " ");
        }

    }

    public void updateBaseUOM(String activity, int reportType) {
        //reportType(1)-EOD, reportType(2)-currentStock, reportType(3)-CurrentStockBatchwise
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
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
                    " where AGM.activity=" + bmodel.QT(activity));
            if (c != null) {
                if (c.getCount() > 0) {
                    initializeUOMmapping(reportType);
                    while (c.moveToNext()) {
                        updateProductMapping(c.getString(0), c.getInt(1), c.getInt(2), contentLevel, contentLevelId, reportType);
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
        if (reportType == 1) {
            for (StockReportBO bo : bmodel.reportHelper.getEODStockReport()) {
                bo.setBaseUomCaseWise(false);
                bo.setBaseUomOuterWise(false);
                bo.setBaseUomPieceWise(false);
            }
        } else if (reportType == 2) {
            for (StockReportBO bo : bmodel.reportHelper.getCurrentStock()) {
                bo.setBaseUomCaseWise(false);
                bo.setBaseUomOuterWise(false);
                bo.setBaseUomPieceWise(false);
            }
        } else if (reportType == 3) {
            for (LoadManagementBO bo : bmodel.productHelper.getProducts()) {
                bo.setBaseUomCaseWise(false);
                bo.setBaseUomOuterWise(false);
                bo.setBaseUomPieceWise(false);
            }
        }
    }

    private void enableUOMForAllProducts(int reportType) {
        if (reportType == 1) {
            for (StockReportBO bo : bmodel.reportHelper.getEODStockReport()) {
                bo.setBaseUomCaseWise(true);
                bo.setBaseUomOuterWise(true);
                bo.setBaseUomPieceWise(true);
            }
        } else if (reportType == 2) {
            for (StockReportBO bo : bmodel.reportHelper.getCurrentStock()) {
                bo.setBaseUomCaseWise(true);
                bo.setBaseUomOuterWise(true);
                bo.setBaseUomPieceWise(true);
            }
        } else if (reportType == 3) {
            for (LoadManagementBO bo : bmodel.productHelper.getProducts()) {
                bo.setBaseUomCaseWise(true);
                bo.setBaseUomOuterWise(true);
                bo.setBaseUomPieceWise(true);
            }
        }
    }

    private void updateProductMapping(String productId, int pLevelId, int uomId, int contentLevel, int contentLevelId, int reportType) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.createDataBase();
        db.openDataBase();

        try {

            if (contentLevelId == pLevelId) {
                if (reportType == 1) {
                    for (StockReportBO bo : bmodel.reportHelper.getEODStockReport()) {
                        if (bo.getProductID().equals(productId)) {
                            if (bo.getPiece_uomid() == uomId)
                                bo.setBaseUomPieceWise(true);
                            else if (bo.getdUomid() == uomId)
                                bo.setBaseUomCaseWise(true);
                            else if (bo.getdOuomid() == uomId)
                                bo.setBaseUomOuterWise(true);
                        }
                    }
                } else if (reportType == 2) {
                    for (StockReportBO bo : bmodel.reportHelper.getCurrentStock()) {
                        if (bo.getProductID().equals(productId)) {
                            if (bo.getPiece_uomid() == uomId)
                                bo.setBaseUomPieceWise(true);
                            else if (bo.getdUomid() == uomId)
                                bo.setBaseUomCaseWise(true);
                            else if (bo.getdOuomid() == uomId)
                                bo.setBaseUomOuterWise(true);
                        }
                    }
                } else if (reportType == 3) {
                    for (LoadManagementBO bo : bmodel.productHelper.getProducts()) {
                        if (productId.equals(bo.getProductid() + "")) {
                            if (bo.getPiece_uomid() == uomId)
                                bo.setBaseUomPieceWise(true);
                            else if (bo.getdUomid() == uomId)
                                bo.setBaseUomCaseWise(true);
                            else if (bo.getdOuonid() == uomId)
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

                        if (reportType == 1) {
                            for (StockReportBO bo : bmodel.reportHelper.getEODStockReport()) {
                                if (bo.getProductID().equals(c.getString(0))) {
                                    if (bo.getPiece_uomid() == uomId)
                                        bo.setBaseUomPieceWise(true);
                                    else if (bo.getdUomid() == uomId)
                                        bo.setBaseUomCaseWise(true);
                                    else if (bo.getdOuomid() == uomId)
                                        bo.setBaseUomOuterWise(true);
                                }
                            }
                        } else if (reportType == 2) {
                            for (StockReportBO bo : bmodel.reportHelper.getCurrentStock()) {
                                if (bo.getProductID().equals(c.getString(0))) {
                                    if (bo.getPiece_uomid() == uomId)
                                        bo.setBaseUomPieceWise(true);
                                    else if (bo.getdUomid() == uomId)
                                        bo.setBaseUomCaseWise(true);
                                    else if (bo.getdOuomid() == uomId)
                                        bo.setBaseUomOuterWise(true);
                                }
                            }
                        } else if (reportType == 3) {
                            for (LoadManagementBO bo : bmodel.productHelper.getProducts()) {
                                if (c.getString(0).equals(bo.getProductid() + "")) {
                                    if (bo.getPiece_uomid() == uomId)
                                        bo.setBaseUomPieceWise(true);
                                    else if (bo.getdUomid() == uomId)
                                        bo.setBaseUomCaseWise(true);
                                    else if (bo.getdOuonid() == uomId)
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

    public void downloadAttendanceReport() {
        try {
            attendanceList = new ArrayList<>();
            attendanceMonth = new ArrayList<>();
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT Date,Status,TimeIn,TimeOut,TimeSpent,Month from AttendanceReport where " +
                            "userid =" + bmodel.userMasterHelper.getUserMasterBO().getUserid());
            if (c != null) {
                while (c.moveToNext()) {
                    AttendanceReportBo obj = new AttendanceReportBo();
                    obj.setDate(c.getString(0));
                    obj.setDay(getDay(c.getString(0)));
                    obj.setStatus(c.getString(1));
                    obj.setTimeIn(c.getString(2));
                    obj.setTimeOut(c.getString(3));
                    obj.setTimeSpent(c.getString(4));
                    obj.setMonth(c.getString(5));


                    attendanceList.add(obj);
                }
                c.close();
            }
            c = db
                    .selectSQL("SELECT distinct Month from AttendanceReport where " +
                            "userid =" + bmodel.userMasterHelper.getUserMasterBO().getUserid());
            if (c != null) {
                while (c.moveToNext()) {
                    attendanceMonth.add(c.getString(0));
                }
                c.close();
            }
            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
        }

    }

    private String getDay(String date) {
        try {
            DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
            Date mdate = formatter.parse(date);
            Calendar cal = Calendar.getInstance();
            cal.setTime(mdate);
            switch (cal.get(Calendar.DAY_OF_WEEK)) {
                case 1:
                    return "Sunday";
                case 2:
                    return "Monday";
                case 3:
                    return "Tuesday";
                case 4:
                    return "Wednesday";
                case 5:
                    return "Thursday";
                case 6:
                    return "Friday";
                case 7:
                    return "Saturday";
                default:
                    return "Monday";
            }
        } catch (Exception e) {
            Commons.printException(e);
            return "Monday";
        }
    }

    public int getPaymentPrintCount(String groupId) {
        int count = 0;
        try {

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db.selectSQL("select print_count from Payment where groupid='" + groupId + "'");
            if (c != null) {
                if (c.moveToNext()) {
                    count = c.getInt(0);

                }
                c.close();
            }
            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return count;
    }


    public void updatePaymentPrintCount(String groupId, int count) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            db.updateSQL("update Payment set print_count=" + count + " where groupid = '" + groupId + "'");

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public int getTotalQtyfororder(String orderId) {
        int tot = 0;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Cursor c = db.selectSQL("select sum(qty) from OrderDetail where orderId='"
                    + orderId + "'");
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


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    private String userName, userPassword;

    public void downloadOrderEmailAccountCredentials() {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            String s = "SELECT ListName FROM StandardListMaster where listcode='ORDER_EMAIL' and listtype='ORDER_MAIL'";

            Cursor c = db.selectSQL(s);
            if (c != null) {
                if (c.moveToNext()) {
                    userName = c.getString(0);
                }
                c.close();
            }

            s = "SELECT ListName FROM StandardListMaster where listcode='ORDER_PWD' and listtype='ORDER_MAIL'";

            c = db.selectSQL(s);
            if (c != null) {
                if (c.moveToNext()) {
                    userPassword = c.getString(0);
                }
                c.close();
            }

            db.closeDB();
        } catch (SQLException e) {
            Commons.printException(e);
        }
    }

    public ArrayList<RetailerMasterBO> getAssetRetailerList() {
        return assetRetailerList;
    }

    public void setAssetRetailerList(ArrayList<RetailerMasterBO> assetRetailerList) {
        this.assetRetailerList = assetRetailerList;
    }

    public ArrayList<AssetTrackingBrandBO> getAssetBrandList() {
        return assetBrandList;
    }

    public void setAssetBrandList(ArrayList<AssetTrackingBrandBO> assetBrandList) {
        this.assetBrandList = assetBrandList;
    }

    public void downloadAssetTrackingRetailerMaster() {
        try {
            RetailerMasterBO orderreport;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("Select distinct A.RetailerID,B.RetailerName from SOD_Assets_Detail A inner join RetailerMaster B on A.RetailerID = B.RetailerID");

            Cursor c = db
                    .selectSQL(sb.toString());
            if (c != null) {
                assetRetailerList = new ArrayList<>();
                while (c.moveToNext()) {
                    orderreport = new RetailerMasterBO();
                    orderreport.setRetailerID(c.getString(0));
                    orderreport.setRetailerName(c.getString(1));
                    assetRetailerList.add(orderreport);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public void downloadAssetTrackingBrandMaster() {
        try {
            AssetTrackingBrandBO orderreport;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("Select distinct A.ProductID,B.PName from SOD_Assets_Detail A inner join ProductMaster B on A.ProductID = B.PID");

            Cursor c = db
                    .selectSQL(sb.toString());
            if (c != null) {
                assetBrandList = new ArrayList<>();
                while (c.moveToNext()) {
                    orderreport = new AssetTrackingBrandBO();
                    orderreport.setBrandID(c.getInt(0));
                    orderreport.setBrandName(c.getString(1));
                    assetBrandList.add(orderreport);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public ArrayList<AssetTrackingReportBO> downloadAssetTrackingreport(int retailerID, int brandID) {
        ArrayList<AssetTrackingReportBO> reportordbooking = null;
        try {
            AssetTrackingReportBO orderreport;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("Select distinct A.PosmDesc,D.PName,ifnull(B.Target,0),ifnull(B.Actual,0),ifnull(C.ListName,'') from PosmMaster A ");
            sb.append("inner join SOD_Assets_Detail B on A.PosmID = B.AssetID ");
            sb.append("left join StandardListMaster C on C.ListID = B.ReasonID and C.ListType = 'REASON' ");
            sb.append("inner join ProductMaster D on B.ProductID = D.PID ");
            sb.append("inner join RetailerMaster E on B.RetailerID = E.RetailerID ");
            sb.append("where E.RetailerID = '" + retailerID + "' ");
            if (brandID != 0) {
                sb.append("and B.ProductID = '" + brandID + "'");
            }

            Cursor c = db
                    .selectSQL(sb.toString());
            if (c != null) {
                reportordbooking = new ArrayList<>();
                while (c.moveToNext()) {
                    orderreport = new AssetTrackingReportBO();
                    orderreport.setAssetDescription(c.getString(0));
                    orderreport.setBrandname(c.getString(1));
                    orderreport.setTarget(c.getString(2));
                    orderreport.setActual(c.getString(3));
                    orderreport.setReason(c.getString(4));
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

    public ArrayList<InventoryBO_Proj> downloadInventoryReport(int retailerId, String type) {
        ArrayList<InventoryBO_Proj> lst = new ArrayList<>();
        try {
            bmodel.setRetailerMasterBO(bmodel.getRetailerBoByRetailerID().get(retailerId + ""));
            String focusBrandIds = "";
            if (type.equalsIgnoreCase("Filt11"))
                focusBrandIds = bmodel.productHelper.getTaggingDetails("FCBND");
            else if (type.equals("Filt12"))
                focusBrandIds = bmodel.productHelper.getTaggingDetails("FCBND2");

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            String s = "select distinct CD.productid,Shelfpqty,Shelfcqty,Shelfoqty,SM.listname,PM.psname from ClosingStockDetail CD inner join ClosingStockHeader CH"
                    + " ON CD.stockid=CH.stockid LEFT JOIN StandardListMaster SM ON SM.listid=CD.reasonid"
                    + " LEFT JOIN Productmaster PM ON PM.pid=CD.productid where CH.retailerid=" + retailerId + " and CH.date=" + bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL))
                    + " and CD.productid in(" + focusBrandIds + ")";

            Cursor c = db.selectSQL(s);
            if (c != null) {
                InventoryBO_Proj bo;
                while (c.moveToNext()) {
                    bo = new InventoryBO_Proj();
                    bo.setProductId(c.getString(0));
                    if (c.getInt(1) > 0 || c.getInt(2) > 0 || c.getInt(3) > 0) {
                        bo.setAvailability("Y");
                        bo.setReasonDesc("");
                    } else {
                        bo.setAvailability("N");
                        bo.setReasonDesc(c.getString(4));
                    }
                    bo.setProductName(c.getString(5));

                    lst.add(bo);

                }
                c.close();
            }
            db.closeDB();
        } catch (Exception ex) {

        }

        return lst;
    }

    /**
     * Method to retrieve transaction invoice details from invoicedetails table
     *
     * @param invoiceno to fetch selected invoiceno from detail table
     * @return ArryList<ProductmasterBO> retrieve productmasterlist to show in ui
     */
    public ArrayList<ProductMasterBO> getReportDetails(String invoiceno) {
        ArrayList<ProductMasterBO> reportList = new ArrayList<>();
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.openDataBase();
        StringBuilder sb = new StringBuilder();
        sb.append("select psname,productid,pcsQty,caseqty,outerqty,totalDiscountedAmt,BM.batchnum,ID.weight,qty from InvoiceDetails ID ");
        sb.append("inner join productmaster PM on ID.productid=PM.pid ");
        sb.append("left join BatchMaster BM on  BM.pid=ID.productid and BM.batchid=ID.batchid ");
        sb.append(" where invoiceid=" + bmodel.QT(invoiceno));
        Cursor c = db.selectSQL(sb.toString());
        if (c != null) {
            ProductMasterBO productMasterBO;
            while (c.moveToNext()) {
                productMasterBO = new ProductMasterBO();
                productMasterBO.setProductShortName(c.getString(0));
                productMasterBO.setProductID(c.getString(1));
                productMasterBO.setOrderedPcsQty(c.getInt(2));
                productMasterBO.setOrderedCaseQty(c.getInt(3));
                productMasterBO.setOrderedOuterQty(c.getInt(4));
                productMasterBO.setTotalamount(c.getDouble(5));
                productMasterBO.setBatchNo(c.getString(6));
                productMasterBO.setWeight(c.getInt(7));
                productMasterBO.setTotalQty(c.getInt(8));
                reportList.add(productMasterBO);

            }
        }
        c.close();
        db.closeDB();
        return reportList;
    }

    /**
     * if free product available for selected invoice,this method use to retrieve data from
     * scheme detail table and display in invoice detai report screen
     *
     * @param id - selected invoice
     * @return - free product list
     */
    public ArrayList<SchemeProductBO> getSchemeProductDetails(String id,boolean isInvoice) {
        ArrayList<SchemeProductBO> freeProductList = new ArrayList<>();
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.openDataBase();
        StringBuilder sb = new StringBuilder();
        sb.append("select psname,freeproductid,freeqty,BM.batchnum,");
        sb.append("CASE WHEN uomid= PM.dUomid THEN " + bmodel.QT("CASE"));
        sb.append(" WHEN uomid=PM.dOUomid THEN " + bmodel.QT("OUTER") + " ELSE " + bmodel.QT("PIECE") + " END AS you ");
        sb.append("from SchemeFreeProductDetail SFP ");
        sb.append("inner join Productmaster PM on SFP.freeproductid=PM.pid ");
        sb.append("left join Batchmaster BM on SFP.freeproductid=BM.pid and SFP.batchid=BM.batchid ");
        if(isInvoice)
            sb.append("where invoiceid=" + bmodel.QT(id));
        else // Order Report
            sb.append("where OrderID=" + bmodel.QT(id));
        Cursor c = db.selectSQL(sb.toString());
        if (c != null) {
            SchemeProductBO schemeProductBO;
            while (c.moveToNext()) {
                schemeProductBO = new SchemeProductBO();
                schemeProductBO.setProductName(c.getString(0));
                schemeProductBO.setProductId(c.getString(1));
                schemeProductBO.setQuantitySelected(c.getInt(2));
                schemeProductBO.setBatchId(c.getString(3));
                schemeProductBO.setUomDescription(c.getString(4));
                freeProductList.add(schemeProductBO);

            }
        }
        c.close();
        db.closeDB();
        return freeProductList;
    }

    public ArrayList<OutletReportBO> downloadOutletReports() {
        ArrayList<OutletReportBO> lst = new ArrayList<>();
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        try {
            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("select distinct UseriD,UserName,Retailerid,RetailerName,LocationName,Address,isPlanned,isVisited");
            sb.append(",TimeIn,TimeOut,Duration,SalesValue,VisitedLat,VisitedLong,SalesVolume from OutletPerfomanceReport order by UseriD,timein,timeout");

            Cursor c = db.selectSQL(sb.toString());
            if (c != null) {
                OutletReportBO outletReportBO;
                while (c.moveToNext()) {
                    outletReportBO = new OutletReportBO();
                    outletReportBO.setUserId(c.getInt(0));
                    outletReportBO.setUserName(c.getString(1));
                    outletReportBO.setRetailerId(c.getInt(2));
                    outletReportBO.setRetailerName(c.getString(3));
                    outletReportBO.setLocationName(c.getString(4));
                    outletReportBO.setAddress(c.getString(5));
                    outletReportBO.setIsPlanned(c.getInt(6));
                    outletReportBO.setIsVisited(c.getInt(7));
                    outletReportBO.setTimeIn(c.getString(8));
                    outletReportBO.setTimeOut(c.getString(9));
                    outletReportBO.setDuration(c.getString(10));
                    outletReportBO.setSalesValue(c.getString(11));
                    outletReportBO.setLatitude(c.getDouble(12));
                    outletReportBO.setLongitude(c.getDouble(13));
                    outletReportBO.setSalesVolume(c.getString(14));

                    lst.add(outletReportBO);

                }
            }
        } catch (Exception ex) {
            Commons.printException(ex);
        } finally {
            db.closeDB();
        }
        return lst;

    }

    public ArrayList<OutletReportBO> getLstUsers() {
        return lstUsers;
    }

    private ArrayList<OutletReportBO> lstUsers;

    public ArrayList downloadUsers() {
        lstUsers = new ArrayList<>();
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        try {

            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("select distinct UseriD,UserName from OutletPerfomanceReport");

            Cursor c = db.selectSQL(sb.toString());
            if (c != null) {
                OutletReportBO outletReportBO;
                while (c.moveToNext()) {
                    outletReportBO = new OutletReportBO();
                    outletReportBO.setUserId(c.getInt(0));
                    outletReportBO.setUserName(c.getString(1));
                    outletReportBO.setChecked(true);// to show all user by default
                    lstUsers.add(outletReportBO);
                }
            }
        } catch (Exception ex) {
            Commons.printException(ex);
        } finally {
            db.closeDB();
        }
        return lstUsers;

    }

    public Integer downloadlastVisitedRetailer(int userId) {
        int retailerid = 0;
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        try {
            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("select distinct Retailerid from OutletPerfomanceReport where userid=" + userId + " order by timein,timeout  desc limit 1");

            Cursor c = db.selectSQL(sb.toString());
            if (c != null) {
                while (c.moveToNext()) {
                    retailerid = c.getInt(0);

                }
            }
        } catch (Exception ex) {
            Commons.printException(ex);
        } finally {
            db.closeDB();
        }
        return retailerid;

    }


    public boolean isPerformReport() {
        boolean isAvailable = false;

        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("select UseriD from OutletPerfomanceReport");

            Cursor c = db.selectSQL(sb.toString());
            if (c != null) {
                while (c.moveToNext()) {
                    if (c.getCount() > 0) {
                        isAvailable = true;
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            Commons.printException(ex);
        }

        return isAvailable;
    }

    public String getPerformRptUrl() {
        String url = "";

        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("select URL from UrlDownloadMaster where MasterName = 'RPT_RETAILER_PERFORMANCE'");

            Cursor c = db.selectSQL(sb.toString());
            if (c != null) {
                while (c.moveToNext()) {
                    url = c.getString(0);
                }
            }
        } catch (Exception ex) {
            Commons.printException(ex);
        }

        return url;
    }

    public ArrayList<ProductMasterBO> getOrderedProductMaster() {
        return productMaster;
    }

    private ArrayList<ProductMasterBO> productMaster = new ArrayList<>();
    ArrayList<Integer> parentIds = new ArrayList<>();
    HashMap<Integer, String> parentIdsMap = new HashMap<>();

    public void downloadProductReportsWithFiveLevelFilter() {
        try {
            ProductMasterBO product;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);

            db.openDataBase();

            int mFiltrtLevel = 0;
            int mContentLevel = 0;


            String sql = "";


            Cursor filterCur = db
                    .selectSQL("SELECT Distinct IFNULL(PL2.Sequence,0), IFNULL(PL3.Sequence,0) FROM ProductLevel CF " +
                            "LEFT JOIN ProductLevel PL2 ON PL2.LevelId =  (Select RField from HhtModuleMaster " +
                            "where hhtCode = 'RPT03' and flag = 1 and ForSwitchSeller = 0) " +
                            "LEFT JOIN ProductLevel PL3 ON PL3.LevelId = (Select LevelId from ProductLevel " +
                            "where Sequence = (Select max(Sequence) from ProductLevel))");

            if (filterCur != null) {
                if (filterCur.moveToNext()) {
                    mFiltrtLevel = filterCur.getInt(0);
                    mContentLevel = filterCur.getInt(1);
                }
                filterCur.close();
            }

            int loopEnd = mContentLevel - mFiltrtLevel + 1;
            sql = "select A"
                    + loopEnd
                    + ".pid,A"
                    + loopEnd
                    + ".pname,sum(OD.Qty),A1.pid,A"
                    + loopEnd
                    + ".psname,A"
                    + loopEnd
                    + ".isSalable," +
                    "A1.pname as brandname,A1.parentid,sum(OD.totalDiscountedAmt) from ProductMaster A1";

            for (int i = 2; i <= loopEnd; i++)
                sql = sql + " INNER JOIN ProductMaster A" + i + " ON A" + i
                        + ".ParentId = A" + (i - 1) + ".PID";

            sql = sql + " left join OrderDetail OD on OD.ProductID = A" + loopEnd + ".pid WHERE A1.PLid IN " +
                    "(Select RField from HhtModuleMaster where hhtCode = 'RPT03' and flag = 1 and ForSwitchSeller = 0) and "
                    + " A" + loopEnd + ".pid = OD.ProductId"
                    + " group by A" + loopEnd + ".pid ORDER BY "
                    + " A" + loopEnd + ".rowid";


            Cursor c = db.selectSQL(sql);
            productMaster = new ArrayList<>();
            if (c != null) {
                while (c.moveToNext()) {
                    product = new ProductMasterBO();
                    product.setProductID(c.getString(0));
                    product.setProductName(c.getString(1));
                    product.setTotalQty(c.getInt(2));
                    product.setParentid(c.getInt(3));
                    product.setProductShortName(c.getString(4));
                    product.setIsSaleable(c.getInt(5));
                    product.setBrandname(c.getString(6));
                    product.setcParentid(c.getInt(7));
                    product.setTotalamount(Double.parseDouble(c.getString(8)));

                    productMaster.add(product);
                    parentIds.add(product.getParentid());
                    parentIdsMap.put(product.getParentid(), product.getProductID());

                }
                c.close();
            }
            db.closeDB();
            if (parentIds != null && parentIds.size() > 0) {
                downloadSellerReportFilter();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public HashMap<Integer, Vector<LevelBO>> getMfilterlevelBo() {
        return mfilterlevelBo;
    }

    private HashMap<Integer, Vector<LevelBO>> mfilterlevelBo = new HashMap<>();

    public Vector<LevelBO> getSequencevalues() {
        return sequencevalues;
    }

    private Vector<LevelBO> sequencevalues;

    public void downloadSellerReportFilter() {
        int LevelID = 0;
        sequencevalues = new Vector<>();
        try {

            LevelBO levelBo;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);

            db.openDataBase();

            String sql = "select distinct PM.PID,PM.PName," +
                    "PM.ParentId,PM.PLid,PL.LevelName from ProductMaster PM left join ProductLevel PL on PL.LevelId = PM.PLid where PM.PLid = (Select RField from HhtModuleMaster where hhtCode = 'RPT03' and flag = 1 and and ForSwitchSeller = 0)";

            Cursor c = db.selectSQL(sql);
            Vector<LevelBO> levelBOVector = new Vector<>();
            if (c != null) {
                while (c.moveToNext()) {
                    if (parentIdsMap.containsKey(c.getInt(0))) {
                        LevelID = c.getInt(3);
                        levelBo = new LevelBO();
                        levelBo.setProductID(c.getInt(0));
                        levelBo.setLevelName(c.getString(1));
                        levelBo.setParentID(c.getInt(2));
                        //levelBo.setProductLevel(c.getString(4));
                        levelBOVector.add(levelBo);
                        if (sequencevalues.size() == 0) {
                            levelBo = new LevelBO();
                            levelBo.setProductID(LevelID);
                            levelBo.setLevelName(c.getString(4));
                            levelBo.setParentID(c.getInt(2));
                            sequencevalues.add(levelBo);
                        }

                    }
                }
                mfilterlevelBo.put(LevelID, levelBOVector);
                c.close();
            }
            db.closeDB();
            //mfilterlevelBo.put()
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void prepareArchiveFileDownload(String filePath) {
        bmodel.setDigitalContentURLS(new HashMap<String, String>());

        boolean isAmazonUpload = false;

        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.createDataBase();
        db.openDataBase();
        Cursor c = db
                .selectSQL("SELECT flag FROM HHTModuleMaster where hhtCode = 'ISAMAZON_IMGUPLOAD' and flag = 1 and ForSwitchSeller = 0");
        if (c != null) {
            while (c.moveToNext()) {
                isAmazonUpload = true;
            }
        }
        c.close();

        if (!isAmazonUpload) {
            c = db
                    .selectSQL("SELECT ListName FROM StandardListMaster Where ListCode = 'AS_HOST'");
            if (c != null) {
                while (c.moveToNext()) {
                    DataMembers.img_Down_URL = c.getString(0);
                }
            }

        } else {
            c = db
                    .selectSQL("SELECT ListName FROM StandardListMaster Where ListCode = 'AS_ROOT_DIR'");
            if (c != null) {
                while (c.moveToNext()) {
                    DataMembers.img_Down_URL = c.getString(0) + "/";
                }
            }

        }

        bmodel.getDigitalContentURLS().put(
                DataMembers.img_Down_URL + filePath,
                DataMembers.PRINTFILE);

        c.close();
        c = null;
        db.closeDB();
    }


    public void downloadWebViewArchAuthUrl() {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);

            db.openDataBase();
            Cursor c = db
                    .selectSQL("select ListName from StandardListMaster where ListCode='URL' AND ListType = 'WEBVIEW_ARCH'");
            if (c != null) {
                if (c.moveToNext()) {
                    webViewAuthUrl = c.getString(0);
                }
                c.close();
            }

            if (!"".equals(webViewAuthUrl)) {
                Cursor c1 = db
                        .selectSQL("select ListName from StandardListMaster where ListCode='AUTH' AND ListType = 'WEBVIEW_ARCH'");
                if (c1 != null) {
                    if (c1.moveToNext()) {
                        webViewAuthUrl += c1.getString(0);
                    }
                    c1.close();
                }
            }
            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
            webViewAuthUrl = "";
        }
    }


    public void downloadWebViewArchUrl() {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select ListName from StandardListMaster where ListCode='URL' AND ListType = 'WEBVIEW_ARCH'");
            if (c != null) {
                if (c.moveToNext()) {
                    webViewArchUrl = c.getString(0);
                }
                c.close();
            }

            if (!"".equals(webViewArchUrl)) {
                Cursor c1 = db
                        .selectSQL("select ListName from StandardListMaster where ListCode='ACTION' AND ListType = 'WEBVIEW_ARCH'");
                if (c1 != null) {
                    while (c1.moveToNext()) {
                        webViewArchUrl += c1.getString(0);
                    }
                    c1.close();
                }
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
            webViewArchUrl = "";
        }
    }


    public String getWebViewArchUrl() {
        return webViewArchUrl;
    }

    public void setWebViewArchUrl(String webViewArchUrl) {
        this.webViewArchUrl = webViewArchUrl;
    }

    private String webViewArchUrl = "";

    public double getTotValues(Context mContext) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db.selectSQL("select ifnull(sum(ordervalue),0) from "
                    + DataMembers.tbl_orderHeader);
            if (c != null) {
                if (c.moveToNext()) {
                    double i = c.getDouble(0);
                    c.close();
                    db.closeDB();
                    return i;
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }

        return 0;
    }

    /**
     * Download retailer master for invoice reports
     *
     * @param context     Context
     * @param mRetailerId Retailer Id
     */
    public void downloadRetailerMaster(Context context, int mRetailerId) {
        try {
            RetailerMasterBO retailer = new RetailerMasterBO();
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select distinct A.retailerid, RPG.GroupId, A.subchannelid,(select ListCode from StandardListMaster where ListID = A.RpTypeId) as rp_type_code,"
                            + " A.RetailerCode, A.RetailerName, RA.Address1, A.tinnumber, A.Rfield3, RA.Address2, RA.Address3, A.TaxTypeId, A.locationid,A.Rfield2," +
                            "A.isSameZone,A.GSTNumber,A.tinExpDate,RBM.BeatID,A.accountid,IM.sid,RPP.ProductId from retailerMaster A"
                            + " LEFT JOIN RetailerPriceGroup RPG ON RPG.RetailerID = A.RetailerID"
                            + " LEFT JOIN RetailerAddress RA ON RA.RetailerId = A.RetailerID"
                            + " LEFT JOIN RetailerBeatMapping RBM ON RBM.RetailerId = A.RetailerID"
                            + " LEFT JOIN InvoiceMaster IM ON IM.Retailerid=A.RetailerID"
                            + " LEFT JOIN RetailerPriorityProducts RPP ON IM.Retailerid=A.RetailerID"
                            + " where A.retailerid=" + mRetailerId);
            if (c != null) {
                if (c.moveToNext()) {
                    retailer = new RetailerMasterBO();
                    retailer.setRetailerID(c.getString(0));
                    retailer.setGroupId(c.getInt(1));
                    retailer.setSubchannelid(c.getInt(2));
                    retailer.setRpTypeCode(c.getString(3));
                    retailer.setRetailerCode(c.getString(4));
                    retailer.setRetailerName(c.getString(5));
                    retailer.setAddress1(c.getString(6));
                    retailer.setTinnumber(c.getString(7));
                    retailer.setCredit_invoice_count(c.getString(8));
                    retailer.setAddress2(c.getString(9));
                    retailer.setAddress3(c.getString(10));
                    retailer.setTaxTypeId(c.getInt(c
                            .getColumnIndex("TaxTypeId")));
                    retailer.setLocationId(c.getInt(c
                            .getColumnIndex("locationid")));
                    retailer.setRfield2(c.getString(13));
                    retailer.setSameZone(c.getInt(14));
                    retailer.setGSTNumber(c.getString(15));
                    retailer.setTinExpDate(c.getString(16));
                    retailer.setBeatID(c.getInt(17));
                    retailer.setAccountid(c.getInt(18));
                    retailer.setDistributorId(c.getInt(19));
                    retailer.setPrioriryProductId(c.getInt(20));

                }
                c.close();
            }

            bmodel.setRetailerMasterBO(retailer);
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public Vector<RetailerMasterBO> getRetailerMaster() {
        return retailerMaster;
    }

    public void setRetailerMaster(Vector<RetailerMasterBO> retailerMaster) {
        this.retailerMaster = retailerMaster;
    }

    public void downloadClosingStockRetailers() {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.openDataBase();
        try {
            retailerMaster = new Vector<>();

            RetailerMasterBO temp;

            Cursor cursor = db.selectSQL("select RM.retailerid,RM.RetailerName from ClosingStockDetail SD " +
                    " INNER JOIN RetailerMaster RM ON RM.RetailerID = SD.RetailerID group by RM.RetailerID");

            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    temp = new RetailerMasterBO();
                    temp.setTretailerId(Integer.parseInt(cursor.getString(0)));
                    temp.setTretailerName(cursor.getString(1));
                    retailerMaster.add(temp);
                }
                cursor.close();
            }

            db.closeDB();
        } catch (Exception e) {
            db.closeDB();
            Commons.printException(e);
        }
    }

    public ArrayList<ProductMasterBO> getClosingStkReport(String retailId) {
        if (closingStkReportByRetailId == null)
            return null;
        return closingStkReportByRetailId.get(retailId);
    }

    public void downloadClosingStock() {
        closingStkReportByRetailId = new HashMap<>();

        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.openDataBase();

        try {

            ArrayList<ProductMasterBO> productMasterBOs;

            Cursor cursor = db.selectSQL("select PM.PName,SH.retailerid,productId,Sum(shelfpqty),Sum(shelfcqty)," +
                    "Sum(shelfoqty),Facing,PM.pCode,RM.RetailerName,SD.uomqty,SD.ouomqty from ClosingStockDetail SD INNER JOIN ClosingStockHeader SH ON SD.stockId=SH.stockId " +
                    "INNER JOIN ProductMaster PM ON PM.PID = SD.ProductID INNER JOIN RetailerMaster RM ON RM.RetailerID = SH.RetailerID " +
                    "group by SH.RetailerID,productId");

            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    ProductMasterBO temp = new ProductMasterBO();
                    temp.setProductName(cursor.getString(0));
                    temp.setProductID(cursor.getString(2));
                    temp.setCsCase(cursor.getInt(4));
                    temp.setCsPiece(cursor.getInt(3));
                    temp.setCsOuter(cursor.getInt(5));
                    temp.setProductCode(cursor.getString(7));
                    temp.setCaseSize(cursor.getInt(9));
                    temp.setOutersize(cursor.getInt(10));

                    if (closingStkReportByRetailId.get(cursor.getString(1)) != null) {
                        ArrayList<ProductMasterBO> productMasterBO1 = closingStkReportByRetailId.get(cursor.getString(1));
                        productMasterBO1.add(temp);

                    } else {
                        productMasterBOs = new ArrayList<>();
                        productMasterBOs.add(temp);
                        closingStkReportByRetailId.put(cursor.getString(1), productMasterBOs);
                    }
                }
                cursor.close();
            }

            db.closeDB();
        } catch (Exception e) {
            db.closeDB();
            Commons.printException(e);
        }
    }
}
