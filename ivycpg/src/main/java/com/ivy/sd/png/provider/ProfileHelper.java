package com.ivy.sd.png.provider;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import androidx.core.content.FileProvider;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ivy.cpg.view.asset.bo.AssetHistoryBO;
import com.ivy.cpg.view.dashboard.DashBoardHelper;
import com.ivy.cpg.view.retailercontact.RetailerContactAvailBo;
import com.ivy.cpg.view.retailercontact.RetailerContactBo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.BuildConfig;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.OrderHistoryBO;
import com.ivy.sd.png.bo.PlanningOutletBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.ApplicationConfigs;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FileUtils;
import com.ivy.utils.StringUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

import static com.ivy.lib.Utils.QT;

/**
 * Created by nivetha.s on 28-10-2015.
 */
public class ProfileHelper {
    private static ProfileHelper instance = null;
    private final Context mContext;
    private final BusinessModel bmodel;

    public LinearLayout.LayoutParams commonsparams;
    private Vector<OrderHistoryBO> parent_orderHistoryLIst;
    private Vector<Vector<OrderHistoryBO>> child_orderHistoryList;
    private Vector<OrderHistoryBO> historyList;
    private Vector<AssetHistoryBO> assetHistoryList;
    private Vector<OrderHistoryBO> parent_invoiceHistoryLIst;
    private Vector<Vector<OrderHistoryBO>> child_invoiceHistoryList;
    private Vector<OrderHistoryBO> invoiceHistoryList;
    private Vector<RetailerMasterBO> mSalesCategoryList;
    private String mSalesCategoryLabel = "";

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
        setmSalesCategoryList(new Vector<RetailerMasterBO>());
    }

    public String getOrderHistoryUrl() {
        String url = "";

        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
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

    public String getInvoiceHistoryUrl(String masterName) {
        String url = "";

        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("select URL from UrlDownloadMaster where MasterName =" + StringUtils.getStringQueryParam(masterName));

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

    public float getP4AvgOrderValue() {
        float i = 0;
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
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
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
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
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
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
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
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
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
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
            c1.close();
        }


        Cursor c2 = db
                .selectSQL("select Amount from Payment where RetailerID='"
                        + retailerid + "'");
        if (c2 != null) {
            if (c2.moveToNext()) {
                osAmt1 = c2.getFloat(0);
            }
            c2.close();
        }


        Cursor c3 = db
                .selectSQL("SELECT count(invNetamount) FROM InvoiceMaster where Retailerid='"
                        + retailerid + "'");
        if (c3 != null) {
            if (c3.moveToNext()) {
                billsCount = c3.getInt(0);
            }
            c3.close();
        }


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
            product = bmodel.getRetailerMaster().get(i);
            if (product.getRetailerID().equals(retailerid)) {
                product.setBillsCount(billsCount);
                product.setOsAmt(osAmt);
                bmodel.getRetailerMaster().setElementAt(product, i);
                // return;
            }
        }
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
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.openDataBase();

            historyList = new Vector<>();

            Cursor c = db
                    .selectSQL("SELECT Distinct POH.Retailerid,RetailerCode,POH.refid,orderdate,ordervalue,lpc,Flag,POH.PaidAmount," +
                            "IFNULL(DeliveryStatus,''),rm.ListName,PM.pid, PM.pname,POD.uomid, POD.qty,PM.piece_uomid,PM.duomid,PM.dOuomid,POH.orderid," +
                            "IM .RField1,IM.RField2,IM.RField3,IM.RField4,IFNULL(POH.volume,''),(ordervalue-(ifnull(POH.PaidAmount,0))) as balAmount," +
                            "POH.PONumber,POH.DeliveryDate,POH.RField1,POH.RField2,POD.FreeQty,POD.Value" +
                            " FROM P4OrderHistoryMaster POH left join P4OrderHistoryDetail POD ON POD.refid=POH.refid" +
                            " left join ProductMaster PM ON PM.pid=POD.productid" +
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
                        orderHistory.setBalanceAmount(c.getDouble(23));
                        orderHistory.setPoNumber(c.getString(24));
                        orderHistory.setDelDate(c.getString(25));
                        orderHistory.setDriverName(c.getString(26));
                        orderHistory.setDelDocNum(c.getString(27));
                        orderHistory.setFreeQty(c.getInt(28));
                        orderHistory.setValue(c.getDouble(29));
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
                    if (isHistoryBOAvailable(historyBO, "orderHistory")) {
                        parent_orderHistoryLIst.add(historyBO);
                    }
                    HistBOTemp = historyBO;
                } else {
                    if (childItemList.get(0).getOrderid()
                            .equals(historyBO.getOrderid())) {


                        childItemList.add(historyBO);
                        HistBOTemp = historyBO;
                        if (isHistoryBOAvailable(historyBO, "orderHistory")) {
                            parent_orderHistoryLIst.add(historyBO);
                        }
                    } else {
                        child_orderHistoryList.add(childItemList);
                        childItemList = new Vector<>();
                        childItemList.add(historyBO);
                        HistBOTemp = historyBO;
                        if (isHistoryBOAvailable(historyBO, "orderHistory")) {
                            parent_orderHistoryLIst.add(historyBO);
                        }
                    }
                }
            }
            if (childItemList != null) {
                if (isHistoryBOAvailable(HistBOTemp, "orderHistory")) {
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
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
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
                if (tabName.equals("orderHistory")) {
                    if (pbo.getOrderid().equals(payBOTemp.getOrderid()))
                        return false;
                } else {
                    if (pbo.getInvoiceId().equals(payBOTemp.getInvoiceId()))
                        return false;
                }
            }
        } catch (Exception e) {
            Commons.printException(e + "");
            return false;
        }
        return true;
    }

    public Vector<OrderHistoryBO> getParentOrderHistory() {
        return parent_orderHistoryLIst;
    }

    private void setParentOrderHistory(Vector<OrderHistoryBO> orderHistory) {
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
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.openDataBase();

            invoiceHistoryList = new Vector<>();

            Cursor c = db
                    .selectSQL("SELECT Distinct PIH.Retailerid,RetailerCode,PIH.refid,PIH.invoicedate,PIH.invoicevalue,lpc,Flag,PIH.PaidAmount," +
                            "IFNULL(DeliveryStatus,''),rm.ListName,PM.pid, PM.pname,PID.uomid, PID.qty,PM.piece_uomid,PM.duomid,PM.dOuomid,PIH.invoiceid,IM .RField1,IM.RField2,IM.RField3,IM.RField4,PIH.orderNo,PIH.volume,PIH.marginValue,PIH.marginPerc,PID.FreeQty,PID.Value" +
                            " FROM P4InvoiceHistoryMaster PIH left join P4InvoiceHistoryDetail PID ON PID.refid=PIH.refid" +
                            " left join ProductMaster PM ON PM.pid=PID.productid" +
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
                            if (bo.getInvoiceId().equals(orderId) && bo.getProductId() == c.getInt(10)) {

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
                        invoiceHistory.setInvoiceId(c.getString(17));

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
                        invoiceHistory.setVolume(c.getString(23));
                        invoiceHistory.setMarginValue(c.getDouble(24));
                        invoiceHistory.setMaginPerc(c.getDouble(25));
                        invoiceHistory.setFreeQty(c.getInt(26));
                        invoiceHistory.setValue(c.getDouble(27));

                        if (bmodel.retailerMasterBO.getCreditDays() != 0) {

                            SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
                            Date date = format.parse(String.valueOf(invoiceHistory.getOrderdate()));
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(date);
                            calendar.add(Calendar.DAY_OF_YEAR, bmodel.retailerMasterBO.getCreditDays());
                            Date dueDate = format.parse(format.format(calendar.getTime()));

                            invoiceHistory.setDueDate(DateTimeUtils.convertDateObjectToRequestedFormat(
                                    dueDate, ConfigurationMasterHelper.outDateFormat));

                        }
                        int due_count = 0;
                        if (bmodel.retailerMasterBO.getCreditDays() != 0) {
                            due_count = DateTimeUtils.getDateCount(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                                    invoiceHistory.getDueDate(), "yyyy/MM/dd");
                        } else {
                            due_count = DateTimeUtils.getDateCount(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
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
                    if (isHistoryBOAvailable(historyBO, "invoiceHistory")) {
                        parent_invoiceHistoryLIst.add(historyBO);
                    }
                    HistBOTemp = historyBO;
                } else {
                    if (childItemList.get(0).getInvoiceId()
                            .equals(historyBO.getInvoiceId())) {


                        childItemList.add(historyBO);
                        HistBOTemp = historyBO;
                        if (isHistoryBOAvailable(historyBO, "invoiceHistory")) {
                            parent_invoiceHistoryLIst.add(historyBO);
                        }
                    } else {
                        child_invoiceHistoryList.add(childItemList);
                        childItemList = new Vector<>();
                        childItemList.add(historyBO);
                        HistBOTemp = historyBO;
                        if (isHistoryBOAvailable(historyBO, "invoiceHistory")) {
                            parent_invoiceHistoryLIst.add(historyBO);
                        }
                    }
                }
            }
            if (childItemList != null) {
                if (isHistoryBOAvailable(HistBOTemp, "invoiceHistory")) {
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

    private void setParentInvoiceHistory(Vector<OrderHistoryBO> invoiceHistory) {
        this.parent_invoiceHistoryLIst = invoiceHistory;
    }

    public Vector<RetailerMasterBO> getmSalesCategoryList() {
        return mSalesCategoryList;
    }

    private void setmSalesCategoryList(Vector<RetailerMasterBO> mSalesCategoryList) {
        this.mSalesCategoryList = mSalesCategoryList;
    }

    public String getmSalesCategoryLabel() {
        return mSalesCategoryLabel;
    }

    public void setmSalesCategoryLabel(String mSalesCategoryLabel) {
        this.mSalesCategoryLabel = mSalesCategoryLabel;
    }

    public ArrayList<PlanningOutletBO> downloadPlanningOutletCategory() {
        planningoutletlist = new ArrayList<>();
        try {

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.openDataBase();
            PlanningOutletBO pbo, pbo1;
            Cursor c = db
                    .selectSQL("SELECT DISTINCT A.pid,P.PName,RField FROM SkuWiseTarget A inner join ProductMaster P on P.PID=A.Pid  where A.Rid="
                            + bmodel.getRetailerMasterBO().getRetailerID()
                            + "   and A.FreqType LIKE 'MONTH' and A.level="
                            + DashBoardHelper.getInstance(mContext).mMinLevel + "  ORDER BY A.pId");
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    ArrayList<PlanningOutletBO> list = new ArrayList<>();
                    pbo = new PlanningOutletBO();
                    pbo.setPid(c.getInt(0));
                    pbo.setPname(c.getString(1));
                    pbo.setKeyBattles(c.getString(2));
                    Cursor c1 = db
                            .selectSQL("select A.Tgt,A.Ach,RField from SkuWiseTarget A where A.Pid="
                                    + c.getInt(0)
                                    + " and A.date < "
                                    + StringUtils.getStringQueryParam(DashBoardHelper.getFirstDateOfCurrentMonth("yyyy/MM/dd"))
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
                        c1.close();
                    }
                    pbo.setPlanlist(list);
                    Commons.print("list size in category" + list.size());

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
        planningoutletlist = new ArrayList<>();
        try {

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.openDataBase();
            PlanningOutletBO pbo, pbo1;
            for (int i = 0; i < pids.size(); i++) {
                Cursor c = db
                        .selectSQL("SELECT DISTINCT A.pid,P.PName FROM SkuWiseTarget A inner join ProductMaster P on P.PID=A.Pid  where A.Rid="
                                + bmodel.getRetailerMasterBO().getRetailerID()
                                + "   and A.FreqType LIKE 'MONTH' and A.level="
                                + DashBoardHelper.getInstance(mContext).mMaxLevel
                                + " and A.pid="
                                + pids.get(i)
                                + "  ORDER BY A.pId");
                if (c.getCount() > 0) {
                    while (c.moveToNext()) {
                        ArrayList<PlanningOutletBO> list = new ArrayList<>();
                        pbo = new PlanningOutletBO();
                        pbo.setPid(c.getInt(0));
                        pbo.setPname(c.getString(1));
                        Cursor c1 = db
                                .selectSQL("select A.Tgt,A.Ach,RField from SkuWiseTarget A where A.Pid="
                                        + c.getInt(0)
                                        + " and A.date < "
                                        + StringUtils.getStringQueryParam(DashBoardHelper.getFirstDateOfCurrentMonth("yyyy/MM/dd"))
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
                            c1.close();
                        }
                        pbo.setPlanlist(list);
                        Commons.print("list size in brand" + list.size());

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
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
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
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
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
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
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
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.openDataBase();
            String tid = bmodel.userMasterHelper.getUserMasterBO().getUserid()
                    + "" + bmodel.getRetailerMasterBO().getRetailerID()
                    + "" + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);

            String insertHeader = "insert into RetailerEditHeader (tid,RetailerId,date)" +
                    "values (" + StringUtils.getStringQueryParam(tid)
                    + "," + bmodel.getRetailerMasterBO().getRetailerID()
                    + "," + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) + ")";
            db.executeQ(insertHeader);
            String insertquery = "insert into RetailerEditDetail (tid,Code,value,RefId,RetailerId)" +
                    "values (" + StringUtils.getStringQueryParam(tid)
                    + ",'PRO23','1'," + bmodel.getRetailerMasterBO().getRetailerID() + "," + bmodel.getRetailerMasterBO().getRetailerID() + ")";
            db.executeQ(insertquery);


            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);

        }
    }


    /**
     * @See {@link com.ivy.ui.profile.data.ProfileDataManagerImpl}
     * @since CPG131 replaced by {@link com.ivy.ui.profile.data.ProfileDataManagerImpl#checkProfileImagePath}
     * Will be removed from @version CPG133 Release
     * @deprecated This has been Migrated to MVP pattern
     */
    public boolean hasProfileImagePath(RetailerMasterBO ret) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.openDataBase();
            Cursor c = db.selectSQL("SELECT value FROM RetailerEditDetail  where code='PROFILE60' AND retailerid=" + ret.getRetailerID());
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

    /**
     * @See {@link  com.ivy.utils.AppUtils}
     * @since CPG131 replaced by {@link com.ivy.utils.AppUtils}
     * Will be removed from @version CPG133 Release
     * @deprecated This has been Migrated to MVP pattern
     */
    public boolean isImagePresent(String path) {
        File f = new File(path);
        return f.exists();
    }

    /**
     * @See {@link  com.ivy.utils.AppUtils}
     * @since CPG131 replaced by {@link com.ivy.utils.AppUtils}
     * Will be removed from @version CPG133 Release
     * @deprecated This has been Migrated to MVP pattern
     */
    public Uri getUriFromFile(String path) {
        File f = new File(path);
        if (Build.VERSION.SDK_INT >= 24) {
            return FileProvider.getUriForFile(mContext, BuildConfig.APPLICATION_ID + ".provider", f);

        } else {
            return Uri.fromFile(f);
        }
    }

    /**
     * @See {@link  com.ivy.utils.AppUtils}
     * @since CPG131 replaced by {@link FileUtils#checkFileExist}
     * Will be removed from @version CPG133 Release
     * @deprecated This has been Migrated to MVP pattern
     */
    public void checkFileExist(String imageName, String retailerID, boolean isLatLongImage) {
        try {
            String fName = (!isLatLongImage) ? "PRO_" : "LATLONG_" + retailerID;
            File sourceDir = new File(FileUtils.photoFolderPath);
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

    public void salesPerCategory() {
        mSalesCategoryList.clear();
        mSalesCategoryLabel = "";
        String givenLevelId = getGivenLovId();
        if (givenLevelId != null && !givenLevelId.equalsIgnoreCase("")) {

            int loop = getProductGroupingLevel(givenLevelId);
            if (loop > 0) {
                StringBuilder finalSql = new StringBuilder("Select   PIM.refid, PIM.InvoiceId, SUM(PIM.InvoiceValue) as InvoiceValue, PIM.lpc, PID.productid, SUM(PID.Qty) as QTY," +
                        "A" + loop
                        + ".psname,"
                        + "A" + loop
                        + ".Pname,"
                        + "A" + loop
                        + ".ParentId as finalParentId,"
                        + "A" + loop
                        + ".Plid as finalPlid"
                        + " from P4InvoiceHistoryMaster PIM INNER JOIN P4InvoiceHistoryDetail PID ON PID.refid=PIM.refid INNER JOIN ProductMaster A1 ON PID.productid=A1.PID ");

                for (int i = loop; i > 1; i--)
                    finalSql.append(" INNER JOIN ProductMaster A").append(i).append(" ON A").append(i).append(".PID = A").append(i - 1).append(".ParentId");

                finalSql.append(" where PIM.retailerid=").append(bmodel.getRetailerMasterBO().getRetailerID()).append("  group by PIM.refid");

                String sqlLabel = "Select LevelName from ProductLevel where Sequence=" + givenLevelId;

                DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
                );
                db.openDataBase();
                Cursor c = db.selectSQL(finalSql.toString());
                Cursor c1 = db.selectSQL(sqlLabel);

                if (c != null) {
                    while (c.moveToNext()) {
                        RetailerMasterBO retailerMasterBO = new RetailerMasterBO();

                        String invoiceId = c.getString(c.getColumnIndex("invoiceid"));
                        String invoiceValue = c.getString(c.getColumnIndex("InvoiceValue"));
                        String lpc = c.getString(c.getColumnIndex("lpc"));
                        String qty = c.getString(c.getColumnIndex("QTY"));
                        String pname = c.getString(c.getColumnIndex("PName"));
                        String psname = c.getString(c.getColumnIndex("psname"));

                        retailerMasterBO.setSalesInvoiceId(invoiceId);
                        retailerMasterBO.setSalesInvoiceValue(invoiceValue);
                        retailerMasterBO.setSalesLpc(lpc);
                        retailerMasterBO.setSalesQty(qty);
                        retailerMasterBO.setSalesProductSName(pname);
                        //   retailerMasterBO.setSalesProductSName(psname);
                        mSalesCategoryList.add(retailerMasterBO);
                    }
                    c.close();
                }

                if (c1 != null) {
                    while (c1.moveToNext()) {
                        mSalesCategoryLabel = c1.getString(0);
                    }
                    c1.close();
                }
                db.closeDB();
            } else
                Toast.makeText(mContext, "Data not Found", Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(mContext, "Data not Found", Toast.LENGTH_SHORT).show();
    }

    private int getProductGroupingLevel(String giveLevelID) {
        try {
            String sql = "Select Max(Sequence) from ProductLevel ";
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c1 = db.selectSQL(sql);
            String prodLevel = "0";
            if (c1 != null) {
                while (c1.moveToNext()) {
                    prodLevel = c1.getString(0);
                }
                c1.close();
            }
            db.closeDB();
            return SDUtil.convertToInt(prodLevel) - SDUtil.convertToInt(giveLevelID) + 1;
        } catch (NumberFormatException e) {
            Commons.printException(e);
            return 0;
        }
    }

    private RetailerMasterBO getProductBrand(String parentId, String givenLevelId, RetailerMasterBO retailerMasterBO) {
        String sql = "Select pid, pname, psname, plid, parentid from object1 where pid=" + parentId;
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        db.openDataBase();
        Cursor c = db.selectSQL(sql);
        if (c != null) {
            while (c.moveToNext()) {
                String levelId = c.getString(3);
                String parId = c.getString(4);
                String psname = c.getString(1);

                if (levelId.equalsIgnoreCase(givenLevelId)) {
                    retailerMasterBO.setSalesProductSName(psname);
                    return retailerMasterBO;
                } else {
                    retailerMasterBO = getProductBrand(parId, givenLevelId, retailerMasterBO);
                    return retailerMasterBO;
                }

            }
            c.close();
        }
        db.closeDB();
        return retailerMasterBO;
    }

    private String getGivenLovId() {
        String givenLovId = "";
        String sql = " Select RField from HhtModuleMaster where hhtCode = " + StringUtils.getStringQueryParam(ConfigurationMasterHelper.CODE_SHOW_AVG_SALES_PER_LEVEL) + " and flag =1 and ForSwitchSeller = 0";
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        db.openDataBase();
        Cursor c = db.selectSQL(sql);
        if (c != null) {
            while (c.moveToNext()) {
                givenLovId = c.getString(0);
            }
            c.close();
        }
        db.closeDB();
        return givenLovId;
    }

    public Observable<HashMap<String, String>> downloadRetailerContactMenu() {
        return Observable.create(new ObservableOnSubscribe<HashMap<String, String>>() {
            @Override
            public void subscribe(ObservableEmitter<HashMap<String, String>> subscriber) throws Exception {
                HashMap<String, String> contactMenuMap = new HashMap<>();

                try {
                    String sql = " Select HHTCode,MName from HhtMenuMaster where MenuType = " + StringUtils.getStringQueryParam("RETAILER_CONTACT") + " and flag =1";
                    DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
                    );
                    db.openDataBase();
                    Cursor c = db.selectSQL(sql);
                    if (c != null) {
                        while (c.moveToNext()) {
                            contactMenuMap.put(c.getString(0), c.getString(1));
                        }
                        c.close();
                    }
                    db.closeDB();
                    subscriber.onNext(contactMenuMap);
                    subscriber.onComplete();
                } catch (Exception e) {
                    Commons.printException(e);
                    subscriber.onError(e);
                    subscriber.onComplete();
                }
            }
        });
    }


    public Observable<ArrayList<RetailerContactBo>> downloadRetailerContact(final String retailerID, final boolean isEdit) {

        return Observable.fromCallable(new Callable<ArrayList<RetailerContactBo>>() {
            @Override
            public ArrayList<RetailerContactBo> call() throws Exception {
                return getContactBos(retailerID,isEdit);
            }
        });
    }

    public ArrayList<RetailerContactBo> getContactBos(final String retailerID, final boolean isEdit){
        ArrayList<RetailerContactBo> contactList = new ArrayList<>();

        String sql = "select ifnull(RC.contact_title,'') as contactTitle,ifNull(SM.ListName,'') as listName,RC.contact_title_lovid as contact_title_lovid,"
                + " ifnull(RC.contactname,'') as cName,ifnull(RC.contactname_LName,'') as cLname,ifnull(RC.ContactNumber,'') as cNumber,RC.IsPrimary as isPrimary,RC.CPID  as cpid,"
                + " ifnull(RC.Email,'') as email,ifnull(RC.salutationLovId,'') as salutationId, ifnull(RC.IsEmailNotificationReq,0) as emailPref,ifnull(RC.RetailerID,0) as retailerID from RetailerContact RC "
                + " Left join StandardListMaster SM on SM.ListId= RC.contact_title_lovid "
                + " Where RC.RetailerId =" + StringUtils.getStringQueryParam(retailerID);

                    DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
                    );
                    db.openDataBase();
                    Cursor c = db.selectSQL(sql);
                    if (c != null) {
                        while (c.moveToNext()) {
                            RetailerContactBo retailerContactBo = new RetailerContactBo();
                            if (c.getString(c.getColumnIndex("contactTitle")).length() > 0)
                                retailerContactBo.setTitle(c.getString(c.getColumnIndex("contactTitle")));
                            else
                                retailerContactBo.setTitle(c.getString(c.getColumnIndex("listName")));
                            retailerContactBo.setContactTitleLovId(c.getString(c.getColumnIndex("contact_title_lovid")));
                            retailerContactBo.setFistname(c.getString(c.getColumnIndex("cName")));
                            retailerContactBo.setLastname(c.getString(c.getColumnIndex("cLname")));
                            retailerContactBo.setContactNumber(c.getString(c.getColumnIndex("cNumber")));
                            retailerContactBo.setContactMail(c.getString(c.getColumnIndex("email")));
                            retailerContactBo.setIsPrimary(c.getInt(c.getColumnIndex("isPrimary")));
                            retailerContactBo.setCpId(c.getString(c.getColumnIndex("cpid")));
            retailerContactBo.setContactSalutationId(c.getString(c.getColumnIndex("salutationId")));
                retailerContactBo.setIsEmailPrimary(c.getInt(c.getColumnIndex("emailPref")));
                            retailerContactBo.setRetailerID(c.getString(c.getColumnIndex("retailerID")));
                setContactAvailList(db,retailerContactBo,"ContactAvailability",false);                contactList.add(retailerContactBo);
                        }
                        c.close();
                    }


        if (isEdit) {
            String retailerContactEditQuery = "select ifnull(RC.Contact_Title,'') as contactTitle, ifNull(SM.ListName,'') as listName, RC.Contact_Title_LovId as contact_title_lovid, ifnull(RC.ContactName,'') as cName,ifnull(RC.ContactName_LName,'') as cLname,ifnull(RC.ContactNumber,'') as cNumber,RC.IsPrimary as isPrimary,Rc.CPId as cpid,Rc.Status as status ,"
                    + " ifnull(RC.Email,'') as email,ifnull(RC.salutationLovId,'') as salutationId, ifnull(RC.IsEmailNotificationReq,0) as emailPref,ifnull(RC.RetailerID,0) as retailerID from RetailerContactEdit RC "
                    + " Left join StandardListMaster SM on SM.ListId= RC.Contact_Title_LovId "
                    + " Where RC.RetailerId =" + StringUtils.getStringQueryParam(retailerID);

                        Cursor retailerContactEditCurson = db.selectSQL(retailerContactEditQuery);
                        if (retailerContactEditCurson != null) {
                            ArrayList<RetailerContactBo> tempList = new ArrayList<>();
                            while (retailerContactEditCurson.moveToNext()) {
                                RetailerContactBo retailerContactBo = new RetailerContactBo();
                                if (retailerContactEditCurson.getString(retailerContactEditCurson.getColumnIndex("contactTitle")).length() > 0) {
                                    retailerContactBo.setTitle(retailerContactEditCurson.getString(retailerContactEditCurson.getColumnIndex("contactTitle")));
                                } else {
                                    retailerContactBo.setTitle(retailerContactEditCurson.getString(retailerContactEditCurson.getColumnIndex("listName")));
                                }
                                retailerContactBo.setContactTitleLovId(retailerContactEditCurson.getString(retailerContactEditCurson.getColumnIndex("contact_title_lovid")));
                                retailerContactBo.setFistname(retailerContactEditCurson.getString(retailerContactEditCurson.getColumnIndex("cName")));
                                retailerContactBo.setLastname(retailerContactEditCurson.getString(retailerContactEditCurson.getColumnIndex("cLname")));
                                retailerContactBo.setContactNumber(retailerContactEditCurson.getString(retailerContactEditCurson.getColumnIndex("cNumber")));
                                retailerContactBo.setContactMail(retailerContactEditCurson.getString(retailerContactEditCurson.getColumnIndex("email")));
                                retailerContactBo.setIsPrimary(retailerContactEditCurson.getInt(retailerContactEditCurson.getColumnIndex("isPrimary")));
                                retailerContactBo.setStatus(retailerContactEditCurson.getString(retailerContactEditCurson.getColumnIndex("status")));
                                retailerContactBo.setCpId(retailerContactEditCurson.getString(retailerContactEditCurson.getColumnIndex("cpid")));
            retailerContactBo.setContactSalutationId(retailerContactEditCurson.getString(retailerContactEditCurson.getColumnIndex("salutationId")));
                    retailerContactBo.setIsEmailPrimary(retailerContactEditCurson.getInt(retailerContactEditCurson.getColumnIndex("emailPref")));
                                retailerContactBo.setRetailerID(c.getString(c.getColumnIndex("retailerID")));
                    setContactAvailList(db,retailerContactBo,"ContactAvailabilityEdit",true);                    tempList.add(retailerContactBo);
                            }
                            retailerContactEditCurson.close();

                            /*Update the edited contact list */
                            for (int i = 0; i < contactList.size(); i++) {

                    String parentCpId = contactList.get(i).getCpId();

                    for (RetailerContactBo retailerContactBoTemp : tempList) {

                        String editedCpId = retailerContactBoTemp.getCpId();

                        if (parentCpId.equalsIgnoreCase(editedCpId)) {
                            contactList.set(i, retailerContactBoTemp);
                            break;
                        }
                    }

                }

                /*Add the new contact list */
                for (RetailerContactBo retailerContactBo :  tempList) {
                    if (retailerContactBo.getStatus().equalsIgnoreCase("I")) {
                        contactList.add(retailerContactBo);
                    }
                }
            }
        }

        db.closeDB();

        return contactList;
    }



    private void setContactAvailList(DBUtil db,RetailerContactBo retailerContactBo,String tableName,boolean isEdit){

        String appendStatusStr = "";

        if (isEdit)
            appendStatusStr = " ,status ";

        Cursor c = db.selectSQL("Select Day,StartTime,EndTime,CPAId "+appendStatusStr+" from "+tableName+" where CPId ="+ StringUtils.getStringQueryParam(retailerContactBo.getCpId()));

        if (c != null) {
            while (c.moveToNext()) {
                RetailerContactAvailBo availBo = new RetailerContactAvailBo();

                availBo.setDay(c.getString(0));
                availBo.setFrom(c.getString(1));
                availBo.setTo(c.getString(2));
                availBo.setCpaid(c.getString(3));

                if (isEdit) {
                    availBo.setStatus(c.getString(4));
                }

                retailerContactBo.getContactAvailList().add(availBo);
            }
        }
    }


    public Observable<ArrayList<ConfigureBO>> downloadContactModuleConfig(boolean isProfileEdit) {
        return Observable.create(new ObservableOnSubscribe<ArrayList<ConfigureBO>>() {
            @Override
            public void subscribe(ObservableEmitter<ArrayList<ConfigureBO>> subscriber) throws Exception {
                ArrayList<ConfigureBO> contactConfig = new ArrayList<>();
                DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
                try {

                    SharedPreferences sharedPrefs = PreferenceManager
                            .getDefaultSharedPreferences(mContext);
                    String locale = sharedPrefs.getString("languagePref",
                            ApplicationConfigs.LANGUAGE);
                    db.openDataBase();

                    String query = "select HHTCode,MName,RField,hasLink,flag,RField6,MNumber,ifnull(Regex,''),RField1 from "
                            + DataMembers.tbl_HhtMenuMaster
                            + " where flag=1";

                    if (isProfileEdit)
                        query = query + " and MenuType= 'RETAILER_CONTACT' and RField =1";
                    else
                        query = query + " and MenuType= 'NEWRETAILER_CONTACT' ";


                    query = query + " and lang=" + StringUtils.getStringQueryParam(locale) + " order by MNumber";

                    Cursor c = db.selectSQL(query);
                    ConfigureBO con;
                    if (c != null) {
                        while (c.moveToNext()) {
                            con = new ConfigureBO();
                            con.setConfigCode(c.getString(0));
                            con.setMenuName(c.getString(1));
                            con.setModule_Order(c.getInt(2));
                            con.setHasLink(c.getInt(3));
                            con.setFlag(c.getInt(4));
                            con.setMenuNumber(c.getString(6));
                            String str = c.getString(7);
                            if (str != null && !str.isEmpty()) {
                                if (str.contains("<") && str.contains(">")) {

                                    String minlen = str.substring(str.indexOf("<") + 1, str.indexOf(">"));
                                    if (!minlen.isEmpty()) {
                                        try {
                                            con.setMaxLengthNo(SDUtil.convertToInt(minlen));
                                        } catch (Exception ex) {
                                            Commons.printException("min len in new outlet helper", ex);
                                        }
                                    }
                                }
                            }
                            con.setRegex(c.getString(7));
                            con.setMandatory(c.getInt(8));// or Edit Contact in profile
                            contactConfig.add(con);

                        }
                        c.close();
                    }
                    db.closeDB();
                    subscriber.onNext(contactConfig);
                    subscriber.onComplete();


                } catch (Exception e) {
                    Commons.printException("" + e);
                    db.closeDB();
                    subscriber.onError(e);
                    subscriber.onComplete();
                }
            }
        });
    }

    public int isConfigAvail(String menu,String config){
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
        try {

            SharedPreferences sharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(mContext);
            String locale = sharedPrefs.getString("languagePref",
                    ApplicationConfigs.LANGUAGE);
            db.openDataBase();

            String query = "select RField from "
                    + DataMembers.tbl_HhtMenuMaster
                    + " where flag=1 and HHTCode = " +StringUtils.getStringQueryParam(config)
                    + " and MenuType= "+StringUtils.getStringQueryParam(menu)+" and lang=" + StringUtils.getStringQueryParam(locale);

            Cursor c = db.selectSQL(query);
            if (c != null && c.getCount() > 0 && c.moveToNext()) {

                int val = c.getInt(0);
                c.close();

                return val;
            }
            else
                return -1;

        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
            return -1;
        }
    }
}
