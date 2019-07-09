package com.ivy.cpg.view.reports.collectionreport;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.PaymentBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class CollectionReportHelper {


    private ArrayList<PaymentBO> paymentList;
    private List<PaymentBO> parentPaymentList;
    private List<List<PaymentBO>> childPaymentList;

    private Context mContext;
    private BusinessModel bModel;

    public CollectionReportHelper(Context context) {
        this.mContext = context;
        this.bModel = (BusinessModel) context.getApplicationContext();
    }


    public HashMap<String, ArrayList<PaymentBO>> getLstPaymentBObyGroupId() {
        return lstPaymentBObyGroupId;
    }

    public void loadCollectionReport() {
        try {
            paymentList = new ArrayList<>();
            parentPaymentList = new ArrayList<>();
            childPaymentList = new ArrayList<>();


            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();


            StringBuffer sb = new StringBuffer();
            sb.append("SELECT DISTINCT RM.RetailerName, PY.BeatID, PY.InvoiceAmount, PY.Amount, PY.Balance,ListCode,");
            sb.append("PY.ChequeNumber, PY.ChequeDate, PY.Date, PY.BillNumber, PY.ReceiptDate, ListName,inv.paidamount,PY.uid,PY.totaldiscount,PY.GroupId,RM.RetailerCode,PY.dateTime,PY.refno FROM Payment PY ");
            sb.append("INNER JOIN InvoiceMaster inv on PY.BillNumber=Inv.InvoiceNo ");
            sb.append("INNER JOIN RetailerMaster RM on RM.RetailerID = PY .RetailerID INNER JOIN StandardListMaster ");
            sb.append("ON PY.CashMode = ListId ");

            sb.append(" UNION ALL ");

            sb.append("SELECT DISTINCT RM.RetailerName, PY.BeatID, PY.InvoiceAmount, PY.Amount, PY.Balance,ListCode,");
            sb.append("PY.ChequeNumber, PY.ChequeDate, PY.Date, PY.BillNumber, PY.ReceiptDate, ListName,(D.debitnoteamount-D.balanceamount) as paidamount,PY.uid,PY.totaldiscount,PY.GroupId,RM.RetailerCode,PY.dateTime,PY.refno FROM Payment PY ");
            sb.append("INNER JOIN DebitNoteMaster D on PY.BillNumber=D.debitnoteno ");
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
                        paybo.setUid(c.getString(13));
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
            downloadAdvancePaymentForReport();

        } catch (Exception e) {
            Commons.printException(e);
        }
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

    private double updateOutStanding(String invoiceNo, double paidTotal, double totalAppliedDiscount) {
        double balance = 0;
        try {
            for (PaymentBO tempParentBO : parentPaymentList) {
                if (tempParentBO.getBillNumber().equals(invoiceNo)) {
                    balance = SDUtil.convertToDouble(SDUtil.format((tempParentBO.getInvoiceAmount()
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


    public List<PaymentBO> getParentPaymentList() {
        return parentPaymentList;
    }

    public List<List<PaymentBO>> getChildPaymentList() {
        return childPaymentList;
    }

    public ArrayList<PaymentBO> getPaymentList() {
        return paymentList;
    }

    private HashMap<String, ArrayList<PaymentBO>> lstPaymentBObyGroupId;

    private void downloadAdvancePaymentForReport() {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        ArrayList<PaymentBO> lstAdvancePayment = null;
        try {
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select id,CN.amount,RM.retailerName,CN.date,SM.listname,RM.RetailerCode,PM.GroupId,SM.Listcode" +
                            ",PM.ChequeDate, PM.ChequeNumber from creditNote CN" +
                            " inner join Payment PM on PM.uid=CN.refno" +
                            " left join StandardListMaster SM on PM.cashMode=SM.listid" +
                            " left join RetailerMaster RM on RM.retailerid=CN.retailerID" +
                            " where id like 'AP%' and CN.date=" + bModel.QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)));
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

    public int getPaymentPrintCount(String groupId) {
        int count = 0;
        try {

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
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
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.openDataBase();
            db.updateSQL("update Payment set print_count=" + count + " where groupid = '" + groupId + "'");

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public boolean deletePayment(String uid) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.openDataBase();
            db.deleteSQL(DataMembers.tbl_Payment, "uid =" + StringUtils.getStringQueryParam(uid), false);
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
            return false;
        }

        return true;
    }
}
