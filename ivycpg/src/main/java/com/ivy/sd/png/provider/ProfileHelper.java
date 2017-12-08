package com.ivy.sd.png.provider;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.widget.LinearLayout;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.OrderHistoryBO;
import com.ivy.sd.png.bo.PlanningOutletBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.asset.AssetHistoryBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.DateUtil;
import com.ivy.sd.png.view.HomeScreenFragment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import static com.ivy.lib.Utils.QT;

/**
 * Created by nivetha.s on 28-10-2015.
 */
public class ProfileHelper {
    private static ProfileHelper instance = null;
    private Context mContext;
    private BusinessModel bmodel;

    public LinearLayout.LayoutParams commonsparams;
    private Vector<OrderHistoryBO> parent_orderHistoryLIst;
    private Vector<Vector<OrderHistoryBO>> child_orderHistoryList;
    public Vector<OrderHistoryBO> historyList;
    private Vector<AssetHistoryBO> assetHistoryList;
    private Vector<OrderHistoryBO> parent_invoiceHistoryLIst;
    private Vector<Vector<OrderHistoryBO>> child_invoiceHistoryList;
    public Vector<OrderHistoryBO> invoiceHistoryList;

    public static ProfileHelper getInstance(Context context) {
        if (instance == null) {
            instance = new ProfileHelper(context);
        }
        return instance;
    }

    private ProfileHelper(Context context) {
        this.mContext = context;
        this.bmodel = (BusinessModel) context;
        setParentOrderHistory(new Vector<OrderHistoryBO>());
        setParentInvoiceHistory(new Vector<OrderHistoryBO>());
    }

    public float getP4AvgOrderValue() {
        float i = 0;
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
        db.openDataBase();
        Cursor c = db
                .selectSQL("SELECT avg(ordervalue) from P4OrderHistoryMaster where retailerid="
                        + bmodel.getRetailerMasterBO().getRetailerID() + " AND reasonid=0");
        if (c != null) {
            if (c.moveToNext()) {
                i = c.getFloat(0);
            }
            c.close();
        }
        db.closeDB();
        return i;
    }

    public int getP4AvgOrderLines() {
        int i = 0;
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
        db.openDataBase();
        Cursor c = db
                .selectSQL("SELECT avg(lpc) from P4OrderHistoryMaster where retailerid="
                        + bmodel.getRetailerMasterBO().getRetailerID() + " AND reasonid=0");
        if (c != null) {
            if (c.moveToNext()) {
                i = c.getInt(0);
            }
            c.close();
        }
        db.closeDB();
        return i;
    }

    public float getP4AvgInvoiceValue() {
        float i = 0;
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
        db.openDataBase();
        Cursor c = db
                .selectSQL("SELECT avg(invoicevalue) from P4InvoiceHistoryMaster where retailerid="
                        + bmodel.getRetailerMasterBO().getRetailerID() + " AND reasonid=0");
        if (c != null) {
            if (c.moveToNext()) {
                i = c.getFloat(0);
            }
            c.close();
        }
        db.closeDB();
        return i;
    }

    public int getP4AvgInvoiceLines() {
        int i = 0;
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
        db.openDataBase();
        Cursor c = db
                .selectSQL("SELECT avg(lpc) from P4InvoiceHistoryMaster where retailerid="
                        + bmodel.getRetailerMasterBO().getRetailerID() + " AND reasonid=0");
        if (c != null) {
            if (c.moveToNext()) {
                i = c.getInt(0);
            }
            c.close();
        }
        db.closeDB();
        return i;
    }

    public void getOSAmtandInvoiceCount(String retailerid, String retailercode) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
        db.openDataBase();
        float osAmt = 0, osAmt1 = 0;
        int billsCount = 0;

        Cursor c1 = db
                .selectSQL("SELECT sum(invNetamount)-sum(paidAmount) FROM InvoiceMaster where Retailerid='"
                        + retailerid + "'");
        if (c1 != null) {
            if (c1.moveToNext()) {
                osAmt = c1.getFloat(0);
            }
        }
        c1.close();

        Cursor c2 = db
                .selectSQL("select Amount from Payment where RetailerID='"
                        + retailerid + "'");
        if (c2 != null) {
            if (c2.moveToNext()) {
                osAmt1 = c2.getFloat(0);
            }
        }
        c2.close();

        Cursor c3 = db
                .selectSQL("SELECT count(invNetamount) FROM InvoiceMaster where Retailerid='"
                        + retailerid + "'");
        if (c3 != null) {
            if (c3.moveToNext()) {
                billsCount = c3.getInt(0);
            }
        }
        c3.close();

        db.closeDB();

        setRetailerChannelName(retailerid, billsCount, (osAmt - osAmt1));
    }

    private void setRetailerChannelName(String retailerid, int billsCount,
                                        float osAmt) {
        RetailerMasterBO product;
        int siz = bmodel.getRetailerMaster().size();
        if (siz == 0)
            return;

        for (int i = 0; i < siz; ++i) {
            product = (RetailerMasterBO) bmodel.getRetailerMaster().get(i);
            if (product.getRetailerID().equals(retailerid)) {
                product.setBillsCount(billsCount);
                product.setOsAmt(osAmt);
                bmodel.getRetailerMaster().setElementAt(product, i);
                // return;
            }
        }
        return;
    }


    public Vector<Vector<OrderHistoryBO>> getChild_orderHistoryList() {
        return child_orderHistoryList;
    }

    public void setChild_orderHistoryList(Vector<Vector<OrderHistoryBO>> child_orderHistoryList) {
        this.child_orderHistoryList = child_orderHistoryList;
    }


    public Vector<OrderHistoryBO> getHistoryList() {
        return historyList;
    }

    public Vector<AssetHistoryBO> getAssetHistoryList() {
        return assetHistoryList;
    }

    /**
     * Download OrderHistory and store in the vector.
     */
    public void downloadOrderHistory() {
        try {
            OrderHistoryBO orderHistory;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            historyList = new Vector<>();

            Cursor c = db
                    .selectSQL("SELECT POH.Retailerid,RetailerCode,POH.refid,orderdate,ordervalue,lpc,Flag,POH.PaidAmount," +
                            "IFNULL(DeliveryStatus,''),rm.ListName,PM.pid, PM.pname,POD.uomid, POD.qty,PM.piece_uomid,PM.duomid,PM.dOuomid,POH.orderid,IM .RField1,IM.RField2,IM.RField3,IM.RField4,IFNULL(POH.volume,'')" +
                            " FROM P4OrderHistoryMaster POH left join P4OrderHistoryDetail POD ON POD.refid=POH.refid" +
                            " left join productMaster PM ON PM.pid=POD.productid" +
                            " left join StandardListMaster rm on POH.reasonid =  rm.ListId" +
                            " left join InvoiceMaster IM ON  POH.orderid =  IM.InvoiceNo where POH.retailerid=" + bmodel.getRetailerMasterBO().getRetailerID());
            if (c != null) {
                this.parent_orderHistoryLIst = new Vector<>();
                child_orderHistoryList = new Vector<>();
                while (c.moveToNext()) {
                    orderHistory = new OrderHistoryBO();

                    String refId = c.getString(2);
                    int pcsUomId = c.getInt(14);
                    int caseUomid = c.getInt(15);
                    int outerUomid = c.getInt(16);
                    String orderId = c.getString(17);

                    boolean isProductExist = false;
                    if (historyList != null) {
                        for (OrderHistoryBO bo : historyList) {
                            if (bo.getOrderid().equals(orderId) && bo.getProductId() == c.getInt(10)) {

                                if (c.getInt(12) == pcsUomId) {
                                    bo.setPcsQty((bo.getPcsQty() + c.getInt(13)));
                                } else if (c.getInt(12) == caseUomid) {
                                    bo.setCaseQty((bo.getCaseQty() + c.getInt(13)));
                                } else if (c.getInt(12) == outerUomid) {
                                    bo.setOuterQty((bo.getOuterQty() + c.getInt(13)));
                                }
                                isProductExist = true;
                            }
                        }
                    }

                    if (!isProductExist) {
                        orderHistory.setRetailerId(c.getString(0));
                        orderHistory.setRetailerCode(c.getString(1));


                        orderHistory.setRefId(refId);
                        orderHistory.setOrderdate(c.getString(3));
                        orderHistory.setOrderValue(c.getDouble(4));
                        orderHistory.setLpc(c.getInt(5));
                        orderHistory.setIsJointCall(c.getInt(6));
                        orderHistory.setPaidAmount(c.getDouble(7));
                        orderHistory.setDelieveryStatus(c.getString(8));
                        orderHistory.setNoorderReason(c.getString(9));

                        orderHistory.setProductId(c.getInt(10));
                        orderHistory.setProductName(c.getString(11));
                        orderHistory.setOrderid(c.getString(17));

                        if (c.getInt(12) == pcsUomId) {
                            orderHistory.setPcsQty(c.getInt(13));
                        } else if (c.getInt(12) == caseUomid) {
                            orderHistory.setCaseQty(c.getInt(13));
                        } else if (c.getInt(12) == outerUomid) {
                            orderHistory.setOuterQty(c.getInt(13));
                        }
                        orderHistory.setRF1(c.getString(18));
                        orderHistory.setRF2(c.getString(19));
                        orderHistory.setRF3(c.getString(20));
                        orderHistory.setRF4(c.getString(21));
                        orderHistory.setVolume(c.getString(22));
                        historyList.add(orderHistory);
                    }


                }
                c.close();
            }
            db.closeDB();

            OrderHistoryBO HistBOTemp = null;
            Vector<OrderHistoryBO> childItemList = null;
            for (OrderHistoryBO historyBO : historyList) {
                if (childItemList == null) {
                    childItemList = new Vector<>();
                    childItemList.add(historyBO);
                    if (!isHistoryBOAvailable(historyBO, "orderHistory")) {
                        parent_orderHistoryLIst.add(historyBO);
                    }
                    HistBOTemp = historyBO;
                } else {
                    if (childItemList.get(0).getOrderid()
                            .equals(historyBO.getOrderid())) {


                        childItemList.add(historyBO);
                        HistBOTemp = historyBO;
                        if (!isHistoryBOAvailable(historyBO, "orderHistory")) {
                            parent_orderHistoryLIst.add(historyBO);
                        }
                    } else {
                        child_orderHistoryList.add(childItemList);
                        childItemList = new Vector<>();
                        childItemList.add(historyBO);
                        HistBOTemp = historyBO;
                        if (!isHistoryBOAvailable(historyBO, "orderHistory")) {
                            parent_orderHistoryLIst.add(historyBO);
                        }
                    }
                }
            }
            if (childItemList != null) {
                if (!isHistoryBOAvailable(HistBOTemp, "orderHistory")) {
                    parent_orderHistoryLIst.add(HistBOTemp);
                }
                child_orderHistoryList.add(childItemList);
            }

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public void downloadAssetHistory(String retailerId) {
        AssetHistoryBO assetHistoryBO;
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.openDataBase();
        assetHistoryList = new Vector<>();

        Cursor c = db.selectSQL("select DISTINCT A.serialNo,A.Date,P.PosmShortDesc from AssetHistory A join PosmMaster P on A.AssetId=P.PosmId where A.RetailerId=" + QT(retailerId) + ";");
        if (c != null) {
            while (c.moveToNext()) {
                assetHistoryBO = new AssetHistoryBO();
                assetHistoryBO.setAssetSerialNo(c.getString(0));
                assetHistoryBO.setAssetDate(c.getString(1));
                assetHistoryBO.setAssetName(c.getString(2));
                assetHistoryList.add(assetHistoryBO);

            }
            c.close();
        }
        db.closeDB();
    }

    private boolean isHistoryBOAvailable(OrderHistoryBO payBOTemp, String tabName) {
        try {
            for (OrderHistoryBO pbo : ((tabName.equals("orderHistory")) ? parent_orderHistoryLIst : parent_invoiceHistoryLIst)) {
                if (pbo.getOrderid().equals(payBOTemp.getOrderid()))
                    return true;
            }
        } catch (Exception e) {
            Commons.printException(e + "");
        }
        return false;
    }

    public Vector<OrderHistoryBO> getParentOrderHistory() {
        return parent_orderHistoryLIst;
    }

    public void setParentOrderHistory(Vector<OrderHistoryBO> orderHistory) {
        this.parent_orderHistoryLIst = orderHistory;
    }

    public Vector<Vector<OrderHistoryBO>> getChild_invoiceHistoryList() {
        return child_invoiceHistoryList;
    }

    public void setChild_invoiceHistoryList(Vector<Vector<OrderHistoryBO>> child_invoiceHistoryList) {
        this.child_invoiceHistoryList = child_invoiceHistoryList;
    }

    public Vector<OrderHistoryBO> getInvoiceHistoryList() {
        return invoiceHistoryList;
    }

    /**
     * Download InvoiceHistory and store in the vector.
     */
    public void downloadInvoiceHistory() {
        try {
            OrderHistoryBO invoiceHistory;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            invoiceHistoryList = new Vector<>();

            Cursor c = db
                    .selectSQL("SELECT PIH.Retailerid,RetailerCode,PIH.refid,PIH.invoicedate,PIH.invoicevalue,lpc,Flag,PIH.PaidAmount," +
                            "IFNULL(DeliveryStatus,''),rm.ListName,PM.pid, PM.pname,PID.uomid, PID.qty,PM.piece_uomid,PM.duomid,PM.dOuomid,PIH.invoiceid,IM .RField1,IM.RField2,IM.RField3,IM.RField4,PIH.orderid" +
                            " FROM P4InvoiceHistoryMaster PIH left join P4InvoiceHistoryDetail PID ON PID.refid=PIH.refid" +
                            " left join productMaster PM ON PM.pid=PID.productid" +
                            " left join StandardListMaster rm on PIH.reasonid =  rm.ListId" +
                            " left join InvoiceMaster IM ON  PIH.invoiceid =  IM.InvoiceNo where PIH.retailerid=" + bmodel.getRetailerMasterBO().getRetailerID());
            if (c != null) {
                this.parent_invoiceHistoryLIst = new Vector<>();
                child_invoiceHistoryList = new Vector<>();
                while (c.moveToNext()) {
                    invoiceHistory = new OrderHistoryBO();

                    String refId = c.getString(2);
                    int pcsUomId = c.getInt(14);
                    int caseUomid = c.getInt(15);
                    int outerUomid = c.getInt(16);
                    String orderId = c.getString(17);

                    boolean isProductExist = false;
                    if (invoiceHistoryList != null) {
                        for (OrderHistoryBO bo : invoiceHistoryList) {
                            if (bo.getOrderid().equals(orderId) && bo.getProductId() == c.getInt(10)) {

                                if (c.getInt(12) == pcsUomId) {
                                    bo.setPcsQty((bo.getPcsQty() + c.getInt(13)));
                                } else if (c.getInt(12) == caseUomid) {
                                    bo.setCaseQty((bo.getCaseQty() + c.getInt(13)));
                                } else if (c.getInt(12) == outerUomid) {
                                    bo.setOuterQty((bo.getOuterQty() + c.getInt(13)));
                                }
                                isProductExist = true;
                            }
                        }
                    }

                    if (!isProductExist) {
                        invoiceHistory.setRetailerId(c.getString(0));
                        invoiceHistory.setRetailerCode(c.getString(1));


                        invoiceHistory.setRefId(refId);
                        invoiceHistory.setOrderdate(c.getString(3));
                        invoiceHistory.setOrderValue(c.getDouble(4));
                        invoiceHistory.setLpc(c.getInt(5));
                        invoiceHistory.setIsJointCall(c.getInt(6));
                        invoiceHistory.setPaidAmount(c.getDouble(7));
                        invoiceHistory.setDelieveryStatus(c.getString(8));
                        invoiceHistory.setNoorderReason(c.getString(9));

                        invoiceHistory.setProductId(c.getInt(10));
                        invoiceHistory.setProductName(c.getString(11));
                        invoiceHistory.setOrderid(c.getString(17));

                        if (c.getInt(12) == pcsUomId) {
                            invoiceHistory.setPcsQty(c.getInt(13));
                        } else if (c.getInt(12) == caseUomid) {
                            invoiceHistory.setCaseQty(c.getInt(13));
                        } else if (c.getInt(12) == outerUomid) {
                            invoiceHistory.setOuterQty(c.getInt(13));
                        }
                        invoiceHistory.setRF1(c.getString(18));
                        invoiceHistory.setRF2(c.getString(19));
                        invoiceHistory.setRF3(c.getString(20));
                        invoiceHistory.setRF4(c.getString(21));

                        invoiceHistory.setOrderid(c.getString(22));

                        if (bmodel.retailerMasterBO.getCreditDays() != 0) {

                            SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
                            Date date = format.parse(String.valueOf(invoiceHistory.getOrderdate()));
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(date);
                            calendar.add(Calendar.DAY_OF_YEAR, bmodel.retailerMasterBO.getCreditDays());
                            Date dueDate = format.parse(format.format(calendar.getTime()));

                            invoiceHistory.setDueDate(DateUtil.convertDateObjectToRequestedFormat(
                                    dueDate, bmodel.configurationMasterHelper.outDateFormat));

                        }
                        int due_count = 0;
                        if (bmodel.retailerMasterBO.getCreditDays() != 0) {
                            due_count = DateUtil.getDateCount(SDUtil.now(SDUtil.DATE_GLOBAL),
                                    invoiceHistory.getDueDate(), "yyyy/MM/dd");
                        } else {
                            due_count = DateUtil.getDateCount(SDUtil.now(SDUtil.DATE_GLOBAL),
                                    invoiceHistory.getOrderdate(), "yyyy/MM/dd");
                        }
                        if (due_count < 0)
                            due_count = 0;
                        invoiceHistory.setOverDueDays(String.valueOf(due_count));
                        invoiceHistory.setOutStandingAmt(invoiceHistory.getOrderValue() - invoiceHistory.getPaidAmount());
                        invoiceHistoryList.add(invoiceHistory);
                    }


                }
                c.close();
            }
            db.closeDB();

            OrderHistoryBO HistBOTemp = null;
            Vector<OrderHistoryBO> childItemList = null;
            for (OrderHistoryBO historyBO : invoiceHistoryList) {
                if (childItemList == null) {
                    childItemList = new Vector<>();
                    childItemList.add(historyBO);
                    if (!isHistoryBOAvailable(historyBO, "invoiceHistory")) {
                        parent_invoiceHistoryLIst.add(historyBO);
                    }
                    HistBOTemp = historyBO;
                } else {
                    if (childItemList.get(0).getOrderid()
                            .equals(historyBO.getOrderid())) {


                        childItemList.add(historyBO);
                        HistBOTemp = historyBO;
                        if (!isHistoryBOAvailable(historyBO, "invoiceHistory")) {
                            parent_invoiceHistoryLIst.add(historyBO);
                        }
                    } else {
                        child_invoiceHistoryList.add(childItemList);
                        childItemList = new Vector<>();
                        childItemList.add(historyBO);
                        HistBOTemp = historyBO;
                        if (!isHistoryBOAvailable(historyBO, "invoiceHistory")) {
                            parent_invoiceHistoryLIst.add(historyBO);
                        }
                    }
                }
            }
            if (childItemList != null) {
                if (!isHistoryBOAvailable(HistBOTemp, "invoiceHistory")) {
                    parent_invoiceHistoryLIst.add(HistBOTemp);
                }
                child_invoiceHistoryList.add(childItemList);
            }

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public Vector<OrderHistoryBO> getParentInvoiceHistory() {
        return parent_invoiceHistoryLIst;
    }

    public void setParentInvoiceHistory(Vector<OrderHistoryBO> invoiceHistory) {
        this.parent_invoiceHistoryLIst = invoiceHistory;
    }

    public ArrayList<PlanningOutletBO> downloadPlanningOutletCategory() {
        planningoutletlist = new ArrayList<PlanningOutletBO>();
        try {

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            PlanningOutletBO pbo, pbo1;
            Cursor c = db
                    .selectSQL("SELECT DISTINCT A.pid,P.PName,RField FROM SkuWiseTarget A inner join ProductMaster P on P.PID=A.Pid  where A.Rid="
                            + bmodel.getRetailerMasterBO().getRetailerID()
                            + "   and A.FreqType LIKE 'MONTH' and A.level="
                            + bmodel.dashBoardHelper.mMinLevel + "  ORDER BY A.pId");
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    ArrayList<PlanningOutletBO> list = new ArrayList<PlanningOutletBO>();
                    pbo = new PlanningOutletBO();
                    pbo.setPid(c.getInt(0));
                    pbo.setPname(c.getString(1));
                    pbo.setKeyBattles(c.getString(2));
                    Cursor c1 = db
                            .selectSQL("select A.Tgt,A.Ach,RField from SkuWiseTarget A where A.Pid="
                                    + c.getInt(0)
                                    + " and A.date < "
                                    + bmodel.QT(bmodel.dashBoardHelper.getFirstDateOfCurrentMonth("yyyy/MM/dd"))
                                    + "  and Rid="
                                    + bmodel.getRetailerMasterBO()
                                    .getRetailerID()
                                    + " order by A.date desc");
                    if (c1 != null) {
                        while (c1.moveToNext()) {
                            pbo1 = new PlanningOutletBO();
                            pbo1.setTargetm1(c1.getString(0));
                            pbo1.setAchievedm1(c1.getString(1));
                            pbo1.setKeyBattles(c1.getString(2));
                            list.add(pbo1);

                        }
                    }
                    pbo.setPlanlist(list);
                    Commons.print("list size in category" + list.size());
                    c1.close();
                    planningoutletlist.add(pbo);
                }
            }
            c.close();
            db.closeDB();
        } catch (SQLException e) {
            // TODO Auto-generated catch blockO
            Commons.printException(e);
        }
        return planningoutletlist;

    }

    public ArrayList<PlanningOutletBO> downloadPlanningOutletBrand(List pids) {
        planningoutletlist = new ArrayList<PlanningOutletBO>();
        try {

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            PlanningOutletBO pbo, pbo1;
            for (int i = 0; i < pids.size(); i++) {
                Cursor c = db
                        .selectSQL("SELECT DISTINCT A.pid,P.PName FROM SkuWiseTarget A inner join ProductMaster P on P.PID=A.Pid  where A.Rid="
                                + bmodel.getRetailerMasterBO().getRetailerID()
                                + "   and A.FreqType LIKE 'MONTH' and A.level="
                                + bmodel.dashBoardHelper.mMaxLevel
                                + " and A.pid="
                                + pids.get(i)
                                + "  ORDER BY A.pId");
                if (c.getCount() > 0) {
                    while (c.moveToNext()) {
                        ArrayList<PlanningOutletBO> list = new ArrayList<PlanningOutletBO>();
                        pbo = new PlanningOutletBO();
                        pbo.setPid(c.getInt(0));
                        pbo.setPname(c.getString(1));
                        Cursor c1 = db
                                .selectSQL("select A.Tgt,A.Ach,RField from SkuWiseTarget A where A.Pid="
                                        + c.getInt(0)
                                        + " and A.date < "
                                        + bmodel.QT(bmodel.dashBoardHelper.getFirstDateOfCurrentMonth("yyyy/MM/dd"))
                                        + "  and Rid="
                                        + bmodel.getRetailerMasterBO()
                                        .getRetailerID()
                                        + " order by A.date desc");
                        if (c1 != null) {
                            while (c1.moveToNext()) {
                                pbo1 = new PlanningOutletBO();
                                pbo1.setTargetm1(c1.getString(0));
                                pbo1.setAchievedm1(c1.getString(1));
                                pbo1.setKeyBattles(c1.getString(2));
                                list.add(pbo1);

                            }
                        }
                        pbo.setPlanlist(list);
                        Commons.print("list size in brand" + list.size());
                        c1.close();
                        planningoutletlist.add(pbo);
                    }
                }

                c.close();

            }
            db.closeDB();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            Commons.printException(e);
        }
        return planningoutletlist;

    }

    private ArrayList<PlanningOutletBO> planningoutletlist;


    /**
     * Check is retailer edit record available.
     */
    public boolean isRetailerEditDetailAvailable(String retailerId) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT RH.tid FROM RetailerEditDetail RD inner join RetailerEditHeader RH on RH .tid=  RD.tid where RH.retailerid=" + retailerId);
            if (c != null) {
                if (c.getCount() > 0) {
                    return true;
                }
                c.close();
            }
            db.closeDB();
            return false;
        } catch (Exception e) {
            Commons.printException(e);
            return false;
        }
    }

    public boolean isDeActivated(String retailerId) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT tid FROM RetailerEditDetail  where code='PRO23' AND retailerid=" + retailerId);
            if (c != null) {
                if (c.getCount() > 0) {
                    return true;
                }
                c.close();
            }
            db.closeDB();
            return false;
        } catch (Exception e) {
            Commons.printException(e);
            return false;
        }
    }

    public void deleteRetailerEditRecords(String retailerId) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            db.deleteSQL("RetailerEditHeader", "RetailerId=" + retailerId, false);
            db.deleteSQL("RetailerEditDetail", "RetailerId=" + retailerId, false);
            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);

        }
    }

    public void deActivateRetailer(String retailerId) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            String tid = bmodel.userMasterHelper.getUserMasterBO().getUserid()
                    + "" + bmodel.getRetailerMasterBO().getRetailerID()
                    + "" + SDUtil.now(SDUtil.DATE_TIME_ID);

            String insertHeader = "insert into RetailerEditHeader (tid,RetailerId,date)" +
                    "values (" + bmodel.QT(tid)
                    + "," + bmodel.getRetailerMasterBO().getRetailerID()
                    + "," + bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + ")";
            db.executeQ(insertHeader);
            String insertquery = "insert into RetailerEditDetail (tid,Code,value,RefId,RetailerId)" +
                    "values (" + bmodel.QT(tid)
                    + ",'PRO23','1'," + bmodel.getRetailerMasterBO().getRetailerID() + "," + bmodel.getRetailerMasterBO().getRetailerID() + ")";
            db.executeQ(insertquery);


            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);

        }
    }

    public boolean hasProfileImagePath(RetailerMasterBO ret) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT value FROM RetailerEditDetail  where code='PROFILE60' AND retailerid=" + ret.getRetailerID());
            if (c != null) {
                if (c.getCount() > 0) {
                    if (c.moveToNext()) {
                        ret.setProfileImagePath(c.getString(0));
                        return true;
                    }
                }
                c.close();
            }
            db.closeDB();
            return false;
        } catch (Exception e) {
            Commons.printException(e);
            return false;
        }
    }

    public boolean isImagePresent(String path) {
        File f = new File(path);
        return f.exists();
    }

    public Uri getUriFromFile(String path) {
        File f = new File(path);
        return Uri.fromFile(f);
    }

    public void checkFileExist(String imageName, String retailerID, boolean isLatLongImage) {
        try {
            String fName = (!isLatLongImage) ? "PRO_" : "LATLONG_" + retailerID;
            File sourceDir = new File(HomeScreenFragment.photoPath);
            File[] files = sourceDir.listFiles();
            for (File file : files) {
                if (file.getName().startsWith(fName) &&
                        !file.getName().equals(imageName))
                    file.delete();
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }
}
