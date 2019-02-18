package com.ivy.cpg.view.orderfullfillment;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.OrderFulfillmentReportBO;
import com.ivy.sd.png.bo.OrderFulfillmentReportListBO;
import com.ivy.sd.png.bo.OrderFullfillmentBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Vector;

/**
 * Created by nivetha.s on 11-08-2015.
 */
public class OrderFullfillmentHelper {
    private Context context;
    private BusinessModel bmodel;
    private static OrderFullfillmentHelper instance = null;
    private ArrayList<OrderFullfillmentBO> oflist;
    private ArrayList<OrderFullfillmentBO> newlist;
    ArrayList<OrderFulfillmentReportBO> orderFulfillmentList;
    ArrayList<OrderFulfillmentReportListBO> orderFulfillmentDetailList;

    private OrderFullfillmentHelper(Context context) {
        this.context = context;
        bmodel = (BusinessModel) context.getApplicationContext();
    }

    public static OrderFullfillmentHelper getInstance(Context context) {
        if (instance == null) {
            instance = new OrderFullfillmentHelper(context);
        }
        return instance;
    }
    public void clearInstance() {
        instance = null;
    }
    /**
     * Load data from IndicativeOrder table *
     */
    public void downloadOrderFullfillmentRetailers() {
        oflist = new ArrayList<>();
        try {

            OrderFullfillmentBO ofbo;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select distinct(RM.RetailerID),RetailerName,RV.VisitFrequency from RetailerMaster RM"
                            + " inner join OrderFullfillmentHeader io on io.RetailerId=RM.RetailerID "
                            + " LEFT JOIN RetailerVisit RV ON RV.RetailerID = RM.RetailerID");
            if (c != null) {
                while (c.moveToNext()) {
                    ofbo = new OrderFullfillmentBO();
                    ofbo.setRetailerid(c.getString(0));
                    ofbo.setRetailername(c.getString(1));
                    ofbo.setVisit_frequencey(c.getDouble(2));
                    oflist.add(ofbo);
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private ArrayList<OrderFullfillmentBO> list;

    public ArrayList<OrderFullfillmentBO> getOrderFullfillment() {
        return oflist;
    }

    public ArrayList<OrderFullfillmentBO> getOrderFullfillmentHeader() {
        return list;
    }

    /**
     * Load order fullfillment header *
     */
    public ArrayList<OrderFullfillmentBO> downloadOrderFullfillment(String rid) {
        list = new ArrayList<>();
        try {
            OrderFullfillmentBO bo;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select distinct(OFH.orderid),sum(value),sum(lines),ifnull(DH.status,'D'),ifnull(DH.DeliveryDate,'" + bmodel.getResources().getString(R.string.select_date) + "'),DH.ReasonId, OFH.OrderNo from OrderFullfillmentHeader OFH left join deliveryheader dh on dh.orderid=OFH.orderid  where OFH.RetailerId=" + bmodel.QT(rid) + " group by OFH.orderid");
            if (c != null) {
                while (c.moveToNext()) {
                    bo = new OrderFullfillmentBO();
                    bo.setOrderId(c.getString(0));
                    bo.setValue(c.getDouble(1));
                    bo.setLineval(c.getInt(2));
                    bo.setRetailerid(rid);
                    bo.setStatus(c.getString(3));

                    bo.setDeliverydate(c.getString(4));

                    bo.setReasonid(c.getString(5));
                    bo.setOrderNo(c.getString(6));

                    if (newlist != null && newlist.size() > 0)
                        bo.setPartialdetailslist(newlist);
                    else
                        bo.setPartialdetailslist(new ArrayList<OrderFullfillmentBO>());
                    list.add(bo);
                }
            }

            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return list;
    }

    private ArrayList<OrderFullfillmentBO> partialfullmentdetaillist;

    private ArrayList<OrderFullfillmentBO> getPartialFullfillmentDetail() {
        return partialfullmentdetaillist;
    }

    public ArrayList<OrderFulfillmentReportBO> getOrderFulfillmentList() {
        return orderFulfillmentList;
    }

    public void setOrderFulfillmentList(ArrayList<OrderFulfillmentReportBO> orderFulfillmentList) {
        this.orderFulfillmentList = orderFulfillmentList;
    }

    public ArrayList<OrderFulfillmentReportListBO> getOrderFulfillmentDetailList() {
        return orderFulfillmentDetailList;
    }

    public void setOrderFulfillmentDetailList(ArrayList<OrderFulfillmentReportListBO> orderFulfillmentDetailList) {
        this.orderFulfillmentDetailList = orderFulfillmentDetailList;
    }

    /**
     * Load PartialFullfillment Details *
     */
    public ArrayList<OrderFullfillmentBO> downloadPartialFullfillment(String orderid) {
        Vector<Integer> pidset = new Vector<>();
        partialfullmentdetaillist = new ArrayList<>();
        ArrayList<OrderFullfillmentBO> listnew = new ArrayList<>();
        try {
            OrderFullfillmentBO bo, bonew;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select distinct(OFD.PId),OFD.Qty,OFD.UOMId,OFD.Price,(select psname  from ProductMaster where PID=OFD.PId) as pname,OFD.OrderId,ifnull(DD.DeliveredQty,-1),ifnull(DD.UOMId,0) from OrderFullfillmentDetail OFD   left join deliverydetail dd on dd.OrderId=OFD.OrderId and dd.pid=OFD.pid where OFD.OrderId=" + bmodel.QT(orderid));
            if (c != null) {
                while (c.moveToNext()) {
                    bo = new OrderFullfillmentBO();
                    bo.setPid(c.getInt(0));
                    if (c.getInt(6) >= 0) {
                        bo.setQty(c.getInt(6));
                        bo.setUomid(c.getInt(7));
                    } else {
                        bo.setQty(c.getInt(1));
                        bo.setUomid(c.getInt(2));
                    }
                    bo.setPrice(c.getDouble(3));
                    bo.setPname(c.getString(4));
                    bo.setOrderId(c.getString(5));
                    if (!pidset.contains(c.getInt(0)))
                        pidset.add(c.getInt(0));

                    bo.setFlagfullfilled(false);
                    bo.setFlagrej(false);
                    bo.setFlagpartial(false);

                    partialfullmentdetaillist.add(bo);
                }
            }
            c.close();
            db.closeDB();
            int pid;
            pidset.toArray();

            for (int j = 0; j < pidset.size(); j++) {
                bonew = new OrderFullfillmentBO();
                pid = pidset.get(j);
                for (int i = 0; i < partialfullmentdetaillist.size(); i++) {
                    if (partialfullmentdetaillist.get(i).getPid() == pid) {
                        bonew.setPname(partialfullmentdetaillist.get(i).getPname());
                        bonew.setPid(partialfullmentdetaillist.get(i).getPid());
                        bonew.setOrderId(partialfullmentdetaillist.get(i).getOrderId());
                        if (bmodel.getStandardListCode(partialfullmentdetaillist.get(i).getUomid()).equalsIgnoreCase("PIECE")) {

                            bonew.setPieceqty(partialfullmentdetaillist.get(i).getQty());
                        } else if (bmodel.getStandardListCode(partialfullmentdetaillist.get(i).getUomid()).equalsIgnoreCase("CASE")) {

                            bonew.setCaseqty(partialfullmentdetaillist.get(i).getQty());
                        } else if (bmodel.getStandardListCode(partialfullmentdetaillist.get(i).getUomid()).equalsIgnoreCase("OUTER")) {

                            bonew.setOuterqty(partialfullmentdetaillist.get(i).getQty());
                        }
                    }
                }
                listnew.add(bonew);
            }

        } catch (Exception e) {
            Commons.printException(e);
        }
        return listnew;
    }

    /**
     * save Fullfillment header and details in Deliveryheader and DeliveryDetail Table *
     */
    public void SaveOrderFullfillment(ArrayList<OrderFullfillmentBO> list) {
//OrderFullfillment Header
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            OrderFullfillmentBO ofbo;
            String values;
            //delete previous order
            for (int i = 0; i < list.size(); i++) {
                ofbo = list.get(i);
                db.deleteSQL("DeliveryHeader", "OrderId=" + bmodel.QT(ofbo.getOrderId()),
                        false);
            }
            String columns = "OrderId,Status,RetailerId,DeliveryDate,ReasonId";
            for (int i = 0; i < list.size(); i++) {
                ofbo = list.get(i);
                if (ofbo != null && !ofbo.getStatus().equals("D")) {
                    values = bmodel.QT(ofbo.getOrderId()) + "," + bmodel.QT(ofbo.getStatus()) + "," + ofbo.getRetailerid() + "," + bmodel.QT((ofbo.getDeliverydate() == null || ofbo.getDeliverydate().equalsIgnoreCase(bmodel.getResources().getString(R.string.select_date)) ? DateTimeUtils.convertDateObjectToRequestedFormat(Calendar
                            .getInstance().getTime(), bmodel.configurationMasterHelper.outDateFormat) : ofbo.getDeliverydate())) + "," + ofbo.getReasonId();
                    db.insertSQL("DeliveryHeader", columns, values);
                }
            }

            //delete previous order
            OrderFullfillmentBO pbo;
            for (int i = 0; i < list.size(); i++) {
                Commons.print("size = " + list.get(i).getPartialdetailslist().size());
                if (list.get(i).getPartialdetailslist().size() > 0) {
                    for (int j = 0; j < list.get(i).getPartialdetailslist().size(); j++) {
                        pbo = list.get(i).getPartialdetailslist().get(j);
                        db.deleteSQL("DeliveryDetail", "OrderId=" + bmodel.QT(pbo.getOrderId()),
                                false);
                    }
                }
            }
            //OrderFullfillment Detail

            columns = "OrderId,PId,OrderedQty,DeliveredQty,UOMId,Price";
            for (int i = 0; i < list.size(); i++) {
                Commons.print("size = " + list.get(i).getPartialdetailslist().size());
                if (list.get(i).getPartialdetailslist().size() > 0) {
                    for (int j = 0; j < list.get(i).getPartialdetailslist().size(); j++) {
                        pbo = list.get(i).getPartialdetailslist().get(j);
                        if (list.get(i).getStatus() != null && !list.get(i).getStatus().equals("D")) {
                            values = pbo.getOrderId() + "," + pbo.getPid() + "," + pbo.getQty() + "," + pbo.getDeliveredQty() + "," + pbo.getUomid() + "," + 0;
                            db.insertSQL("DeliveryDetail", columns, values);
                        }
                    }
                }
            }

            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    /**
     * Load PartialFullfillment Details in object *
     */
    public void savePartialFullfillment(ArrayList<OrderFullfillmentBO> list, String orderno) {
        try {


            OrderFullfillmentBO obo, newbo;
            newlist = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                obo = list.get(i);
                for (int j = 0; j < getPartialFullfillmentDetail().size(); j++) {
                    if (obo.getPid() == getPartialFullfillmentDetail().get(j).getPid()) {
                        if (bmodel.getStandardListCode(getPartialFullfillmentDetail().get(j).getUomid()).equalsIgnoreCase("PIECE")) {
                            newbo = new OrderFullfillmentBO();
                            newbo.setPid(obo.getPid());
                            newbo.setQty(getPartialFullfillmentDetail().get(j).getQty());
                            newbo.setDeliveredQty(obo.getPieceqty());
                            newbo.setUomid(getPartialFullfillmentDetail().get(j).getUomid());
                            newbo.setOrderId(obo.getOrderId());
                            newlist.add(newbo);
                        } else if (bmodel.getStandardListCode(getPartialFullfillmentDetail().get(j).getUomid()).equalsIgnoreCase("CASE")) {
                            newbo = new OrderFullfillmentBO();
                            newbo.setPid(obo.getPid());
                            newbo.setQty(getPartialFullfillmentDetail().get(j).getQty());
                            newbo.setDeliveredQty(obo.getCaseqty());
                            newbo.setUomid(getPartialFullfillmentDetail().get(j).getUomid());
                            newbo.setOrderId(obo.getOrderId());
                            newlist.add(newbo);
                        } else if (bmodel.getStandardListCode(getPartialFullfillmentDetail().get(j).getUomid()).equalsIgnoreCase("OUTER")) {
                            newbo = new OrderFullfillmentBO();
                            newbo.setPid(obo.getPid());
                            newbo.setQty(getPartialFullfillmentDetail().get(j).getQty());
                            newbo.setDeliveredQty(obo.getOuterqty());
                            newbo.setUomid(getPartialFullfillmentDetail().get(j).getUomid());
                            newbo.setOrderId(obo.getOrderId());
                            newlist.add(newbo);
                        }
                    }
                }
            }
            int size = getOrderFullfillmentHeader().size();

            for (int i = 0; i < size; i++) {
                if (getOrderFullfillmentHeader().get(i).getOrderId().equals(orderno)) {

                    getOrderFullfillmentHeader().get(i).setPartialdetailslist(newlist);
                }
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }


    public void downloadOrderFullfillmentReport(String date) {
        orderFulfillmentList = new ArrayList<>();
        try {
            OrderFulfillmentReportBO bo;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db
                    .selectSQL("Select distinct OFH.OrderID, RM.RetailerName, " +
                            "case when DH.Status = 'F' then 'Fulfilled' " +
                            "when DH.Status = 'R' then 'Rejected' " +
                            "else 'Partially Fulfilled' end as Status " +
                            "from OrderFullfillmentHeader OFH " +
                            "inner join DeliveryHeader DH on OFH.OrderId = DH.OrderId " +
                            "inner join RetailerMaster RM on RM.RetailerID = OFH.RetailerId " +
                            "where OFH.Date = '" + date + "'");
            if (c != null) {
                while (c.moveToNext()) {
                    bo = new OrderFulfillmentReportBO();
                    bo.setOrderID(c.getString(0));
                    bo.setRetailerID(c.getString(1));
                    bo.setStatus(c.getString(2));
                    orderFulfillmentList.add(bo);
                }
            }

            c.close();
            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);
        }
        setOrderFulfillmentList(orderFulfillmentList);
    }

    public void downloadOrderFullfillmentDetailReport(String Date, String orderID) {
        orderFulfillmentDetailList = new ArrayList<>();
        try {
            OrderFulfillmentReportListBO bo;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT distinct OFD.OrderID,PM.PName," +
                            "       CASE WHEN SLM.ListCode = 'CASE' THEN OFD.Qty ELSE '0' END AS CaseOrderedQty," +
                            "       CASE WHEN SLM.ListCode = 'PIECE' THEN OFD.Qty ELSE '0' END AS PieceOrderedQty," +
                            "       CASE WHEN DH.Status = 'P' AND SLM.ListCode = 'CASE' THEN DD.DeliveredQty WHEN DH.Status = 'F' AND " +
                            "                                                                                               SLM.ListCode = 'CASE' THEN OFD.Qty ELSE '0' END AS CaseFulfilledQty," +
                            "       CASE WHEN DH.Status = 'P' AND SLM.ListCode = 'PIECE' THEN DD.DeliveredQty WHEN DH.Status = 'F' AND " +
                            "                                                                                                SLM.ListCode = 'PIECE' THEN OFD.Qty ELSE '0' END AS PieceFulfilledQty" +
                            "  FROM ProductMaster PM" +
                            "       INNER JOIN" +
                            "       OrderFullfillmentDetail OFD ON OFD.PID = PM.PID" +
                            "       INNER JOIN" +
                            "       StandardListMaster SLM ON (SLM.ListID = OFD.UOMID OR " +
                            "                                  SLM.ListID = DD.UOMID) " +
                            "       LEFT JOIN" +
                            "       DeliveryHeader DH ON DH.OrderID = OFD.OrderID" +
                            "       LEFT JOIN" +
                            "       DeliveryDetail DD ON DD.PId = OFD.PId AND " +
                            "                            DD.OrderId = OFD.OrderId" +
                            "       INNER JOIN" +
                            "       OrderFullfillmentHeader OFH ON OFH.OrderId = OFD.OrderId" +
                            " WHERE OFH.Date = '" + Date + "' and DH.Status != 'R' and OFD.OrderID = '" + orderID + "'");
            if (c != null) {
                while (c.moveToNext()) {
                    bo = new OrderFulfillmentReportListBO();
                    bo.setOrderID(c.getString(0));
                    bo.setProductName(c.getString(1));
                    bo.setOrderedCases(c.getString(2));
                    bo.setOrderedPcs(c.getString(3));
                    bo.setFulfilledCases(c.getString(4));
                    bo.setFulfilledPcs(c.getString(5));
                    orderFulfillmentDetailList.add(bo);
                }
            }

            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        setOrderFulfillmentDetailList(orderFulfillmentDetailList);
    }
}
