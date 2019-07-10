package com.ivy.cpg.view.collection;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.Utils;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.BankMasterBO;
import com.ivy.sd.png.bo.BranchMasterBO;
import com.ivy.sd.png.bo.CreditNoteListBO;
import com.ivy.sd.png.bo.DiscontSlabBO;
import com.ivy.sd.png.bo.InvoiceHeaderBO;
import com.ivy.sd.png.bo.PaymentBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.RetailerWiseBankDetailsBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * @author vinodh.r
 */
public class CollectionHelper {

    private final Context mContext;
    private final BusinessModel bmodel;
    private static CollectionHelper instance = null;
    private List<StandardListBO> paymentModes;
    private List<CreditNoteListBO> creditNoteList;
    private ArrayList<CreditNoteListBO> mAdvancePaymentList;
    private final List<PaymentBO> paymentList;
    private boolean isCollectionView = false;

    private int mradioGroupIndex = 0;
    public String collectionGroupId = "";
    private ArrayList<PaymentBO> mPaymentList;
    private HashMap<String, PaymentBO> mPaymentBOByMode;
    private ArrayList<BankMasterBO> bankMaster;
    private ArrayList<BranchMasterBO> bankBranch;
    private ArrayList<RetailerWiseBankDetailsBO> retailerWiseBankDetailsBOArrayList;
    public String receiptno = "";
    public String collectionDate = "";

    private ArrayList<DiscontSlabBO> mDiscountSlabList;

    public PaymentBO getmAdvancePaymentBO() {
        return mAdvancePaymentBO;
    }

    public void setmAdvancePaymentBO(PaymentBO mAdvancePaymentBO) {
        this.mAdvancePaymentBO = mAdvancePaymentBO;
    }

    private PaymentBO mAdvancePaymentBO;

    private CollectionHelper(Context context) {
        this.mContext = context;
        this.bmodel = (BusinessModel) context;
        paymentList = new ArrayList<>();
        bankMaster = new ArrayList<>();
        bankBranch = new ArrayList<>();
        retailerWiseBankDetailsBOArrayList = new ArrayList<>();
    }

    public static CollectionHelper getInstance(Context context) {
        if (instance == null) {
            instance = new CollectionHelper(context);
        }
        return instance;
    }

    public int getMradioGroupIndex() {
        return mradioGroupIndex;
    }

    public void setMradioGroupIndex(int mradioGroupIndex) {
        this.mradioGroupIndex = mradioGroupIndex;
    }

    /**
     * Download payment modes from StandardListMaster of type StandardListMasterConstants.COLLECTION_PAY_TYPE
     */
    public void loadPaymentModes() {
        try {
            paymentModes = new ArrayList<>();
            paymentModes.clear();
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT ListCode, ListName FROM StandardListMaster WHERE ListType = '"
                            + StandardListMasterConstants.COLLECTION_PAY_TYPE
                            + "'");
            if (c != null) {
                while (c.moveToNext()) {
                    StandardListBO obj = new StandardListBO();
                    obj.setListCode(c.getString(0));
                    obj.setListName(c.getString(1));
                    paymentModes.add(obj);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public void loadPaymentMode() {
        boolean isAdvancePaymentAvailable = false;
        boolean isCreditNoteAvailable = false;
        mPaymentList = new ArrayList<>();
        mPaymentBOByMode = new HashMap<>();
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        db.createDataBase();
        db.openDataBase();
        Cursor c = db
                .selectSQL("SELECT ListCode, ListName,Listid FROM StandardListMaster WHERE ListType = '"
                        + StandardListMasterConstants.COLLECTION_PAY_TYPE
                        + "' order by listcode ");
        if (c != null) {
            PaymentBO paymentBO;
            while (c.moveToNext()) {
                paymentBO = new PaymentBO();
                paymentBO.setCashMode(c.getString(0));
                paymentBO.setListName(c.getString(1));
                final boolean isCreditBalanceCheck = isCreditBalancebalance(paymentBO.getCashMode());
                paymentBO.setCreditBalancePayment(isCreditBalanceCheck);
                mPaymentBOByMode.put(paymentBO.getCashMode(), paymentBO);

                if (paymentBO.getCashMode().equals(StandardListMasterConstants.ADVANCE_PAYMENT))
                    isAdvancePaymentAvailable = true;
                if (paymentBO.getCashMode().equals(StandardListMasterConstants.CREDIT_NOTE))
                    isCreditNoteAvailable = true;

                mPaymentList.add(paymentBO);

            }
            c.close();
        }
        // if advance payment available then load advance payemtn details
        if (isAdvancePaymentAvailable)
            loadAdvancePayment(db);
        if (isCreditNoteAvailable) {
            loadCreditNote();
        }
        db.closeDB();
    }

    public ArrayList<PaymentBO> getCollectionPaymentList() {
        return mPaymentList;
    }

    public HashMap<String, PaymentBO> getPaymentBoByMode() {
        return mPaymentBOByMode;
    }

    /**
     * Load credit note form CreditNote table.
     */
    public void loadCreditNote() {
        try {
            creditNoteList = new ArrayList<>();
            creditNoteList.clear();
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT id, refno, Actualamount, retailerid, isused,creditnotetype,AppliedAmount,refid FROM CreditNote");
            if (c != null) {
                while (c.moveToNext()) {
                    CreditNoteListBO obj = new CreditNoteListBO();
                    obj.setId(c.getString(0));
                    obj.setRefno(c.getString(1));
                    obj.setAmount(c.getDouble(2));
                    //obj.setUsed(false);

                    boolean flag;
                    flag = (c.getInt(4) == 1);

                    if (flag)
                        obj.setUsed(true);

                    else
                        obj.setUsed(false);

                    obj.setRetailerId(c.getString(3));
                    obj.setTypeId(c.getInt(5));
                    obj.setAppliedAmount(c.getDouble(6));
                    obj.setRefid(c.getInt(7));
                    creditNoteList.add(obj);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private void loadAdvancePayment(DBUtil db) {
        mAdvancePaymentList = new ArrayList<>();
        try {

            String modeID = bmodel.getStandardListIdAndType(
                    "CNAP",
                    StandardListMasterConstants.CREDIT_NOTE_TYPE);
            StringBuffer sb = new StringBuffer();
            sb.append("SELECT id, refno, Actualamount, retailerid, isused,creditnotetype,AppliedAmount,refid FROM CreditNote ");
            sb.append(" where creditnotetype=" + StringUtils.getStringQueryParam(modeID));
            sb.append(" and createddate!=" + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)));
            sb.append(" and retailerid=" + StringUtils.getStringQueryParam(bmodel.getRetailerMasterBO().getRetailerID()));
            Cursor c = db
                    .selectSQL(sb.toString());
            if (c != null) {
                while (c.moveToNext()) {
                    CreditNoteListBO obj = new CreditNoteListBO();
                    obj.setId(c.getString(0));
                    obj.setRefno(c.getString(1));
                    obj.setAmount(c.getDouble(2));
                    //obj.setUsed(false);

                    boolean flag;
                    flag = (c.getInt(4) == 1);
                    Commons.print("CreditNote isused : " + flag);

                    if (flag)
                        obj.setUsed(true);

                    else
                        obj.setUsed(false);

                    obj.setRetailerId(c.getString(3));
                    obj.setTypeId(c.getInt(5));
                    obj.setAppliedAmount(c.getDouble(6));
                    obj.setRefid(c.getInt(7));
                    mAdvancePaymentList.add(obj);
                }
                c.close();
            }

        } catch (Exception e) {
            Commons.printException(e);

        }

    }

    public ArrayList<CreditNoteListBO> getAdvancePaymentList() {
        if (mAdvancePaymentList != null)
            return mAdvancePaymentList;
        return new ArrayList<>();
    }

    public double calculatePendingOSTAmount() {
        double totalAmount = 0;
        DBUtil db;
        Cursor c;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            StringBuffer sb = new StringBuffer();
            sb.append("SELECT IFNULL(IM.invNetamount,0),");
            sb.append(" IFNULL(sum(pmt.Amount),0) as OS,IFNULL(IM.paidamount,0) as OS1 from InvoiceMaster IM  left join   payment pmt on  pmt.BillNumber=IM.InvoiceNo AND IM.upload = 'N' and " +
                    " pmt.CashMode in (select listid from standardlistmaster  where  listtype = 'COLLECTION_PAY_TYPE'  and parentid= (select listid from StandardListMaster where listcode = 'ICB' and listtype = 'PAYMENT_GROUP_TYPE') ) where ");
            sb.append(" IM.Retailerid =");
            sb.append(bmodel.getRetailerMasterBO().getRetailerID());
            sb.append(" and distributorid=");
            sb.append(bmodel.getRetailerMasterBO().getDistributorId());
            sb.append(" AND IM.upload = 'N' group by IM.invoiceno");

            c = db.selectSQL(sb.toString());
            if (c != null) {
                while (c.moveToNext()) {
                    if (c.getDouble(0) >= c.getDouble(1))
                        totalAmount = totalAmount + c.getDouble(0) - c.getDouble(1) - c.getDouble(2);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return totalAmount;
    }

    public void collectionBeforeInvoice() {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            String columns = "uid,BillNumber,ReceiptDate,InvoiceAmount,Balance,CashMode,ChequeNumber,Amount,RetailerID,BeatID,UserID,BankID,BranchCode,ChequeDate,Date,payType,ImageName,groupId,StatusLovId,totalDiscount,distributorid,DistParentID,ReceiptNo,datetime,refid,refno,ridSF,VisitId";
            String groupID = StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID_MILLIS) + bmodel.getAppDataProvider().getUser().getUserid());
            String groupDate = DateTimeUtils.now(DateTimeUtils.DATE_TIME);
            double calculateCredit = 0;
            for (PaymentBO paymentBO : getPaymentList()) {
                if (paymentBO.getAmount() > 0) {
                    if (bmodel.configurationMasterHelper.SHOW_INVOICE_CREDIT_BALANCE
                            && bmodel.retailerMasterBO.getCredit_balance() != -1 && paymentBO.isCreditBalancePayment()) { // update
                        // credit
                        // balance
                        calculateCredit = bmodel.retailerMasterBO
                                .getCredit_balance() + paymentBO.getAmount();
                        bmodel.retailerMasterBO
                                .setCredit_balance((float) calculateCredit);
                        bmodel.retailerMasterBO
                                .setRField1("" + (float) calculateCredit);
                    }
                    String payID = StringUtils.getStringQueryParam("P"
                            + bmodel.getAppDataProvider().getUser().getUserid()
                            + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID_MILLIS));
                    String modeID = bmodel.getStandardListIdAndType(
                            paymentBO.getCashMode(),
                            StandardListMasterConstants.COLLECTION_PAY_TYPE);
                    String payTypeID = bmodel
                            .getStandardListIdAndType(
                                    paymentBO.getPaymentTransactioMode(),
                                    StandardListMasterConstants.COLLECTION_TRANSACTION_PAYMENT_TYPE);
                    int listid;
                    if (paymentBO.isCreditBalancePayment()) {
                        listid = getCollectionProcessListId("CLOSED");
                    } else {
                        listid = getCollectionProcessListId("OPEN");
                    }
                    if (!StandardListMasterConstants.CREDIT_NOTE.equals(paymentBO.getCashMode())) {

                        Commons.print("Business Model," + "Type id :" + payTypeID);
                        String values = payID + "," + StringUtils.getStringQueryParam(bmodel.invoiceNumber) + ","
                                + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) + ","
                                + (paymentBO.getInvoiceAmount()) + ","
                                + (paymentBO.getBalance()) + "," + StringUtils.getStringQueryParam(modeID)
                                + "," + StringUtils.getStringQueryParam(paymentBO.getChequeNumber()) + ","
                                + (paymentBO.getAmount()) + ","
                                + StringUtils.getStringQueryParam(bmodel.retailerMasterBO.getRetailerID())
                                + "," + StringUtils.getStringQueryParam(paymentBO.getBeatID()) + ","
                                + StringUtils.getStringQueryParam(paymentBO.getUserId()) + ","
                                + StringUtils.getStringQueryParam(paymentBO.getBankID()) + ","
                                + StringUtils.getStringQueryParam(paymentBO.getBranchId()) + ","
                                + StringUtils.getStringQueryParam(paymentBO.getChequeDate()) + ","
                                + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) + ","
                                + StringUtils.getStringQueryParam(payTypeID) + "," + StringUtils.getStringQueryParam(paymentBO.getImageName()) + "," + (groupID) + "," + listid + ",0"
                                + "," + bmodel.getAppDataProvider().getRetailMaster().getDistributorId() + "," + bmodel.getAppDataProvider().getRetailMaster().getDistParentId() + ",''"
                                + "," + StringUtils.getStringQueryParam(groupDate) + ",0,0,"
                                + StringUtils.getStringQueryParam(bmodel.getAppDataProvider().getRetailMaster().getRidSF())
                                + "," + bmodel.getAppDataProvider().getUniqueId();

                        db.insertSQL(DataMembers.tbl_Payment, columns, values);
                    } else {
                        InvoiceHeaderBO invoiceHeaderBO = new InvoiceHeaderBO();
                        invoiceHeaderBO.setInvoiceNo(bmodel.invoiceNumber);
                        invoiceHeaderBO.setInvoiceAmount(paymentBO.getInvoiceAmount());
                        invoiceHeaderBO.setBalance(paymentBO.getInvoiceAmount());
                        final List<CreditNoteListBO> creditNoteList = getCreditNoteList();
                        for (CreditNoteListBO creditNoteListBO : creditNoteList) {
                            if (creditNoteListBO.getRetailerId().equals(
                                    bmodel.getAppDataProvider().getRetailMaster().getRetailerID())
                                    && !creditNoteListBO.isUsed() && creditNoteListBO.isChecked()) {
                                if (paymentBO.getUpdatePayableamt() > 0 && creditNoteListBO.getAmount() > 0 && invoiceHeaderBO.getBalance() > 0) {
                                    insertCreditNoteCollection(paymentBO, invoiceHeaderBO, creditNoteListBO, db, groupID, columns, groupDate, false);

                                }
                            }
                        }

                        updateCreditNote(db);
                    }
                }
                db.updateSQL("UPDATE RetailerMaster SET RField1 = '"
                        + calculateCredit + "' WHERE RetailerID = '"
                        + bmodel.retailerMasterBO.getRetailerID() + "'");
                db.closeDB();
            }
        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }

    public List<StandardListBO> getPaymentModes() {
        return paymentModes;
    }

    public List<CreditNoteListBO> getCreditNoteList() {
        return creditNoteList;
    }

    public List<PaymentBO> getPaymentList() {
        return paymentList;
    }

    public boolean isCollectionView() {
        return isCollectionView;
    }

    public void setCollectionView(boolean isCollectionView) {
        this.isCollectionView = isCollectionView;
    }

    public boolean isCreditBalancebalance(String mode) {
        boolean flag = true;
        DBUtil db;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            StringBuilder sb = new StringBuilder();
            sb.append("select listcode from StandardListMaster where listid=");
            sb.append("(select parentid from StandardListmaster where ListCode=");
            sb.append(StringUtils.getStringQueryParam(mode));
            sb.append(")");
            sb.append(" and ListType='PAYMENT_GROUP_TYPE'");
            Cursor c = db.selectSQL(sb.toString());
            if (c != null) {
                while (c.moveToNext()) {
                    String code = c.getString(0);
                    flag = !"NONICB".equals(code);
                }
                c.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.print(e.getMessage());
        }

        return flag;
    }

    private int getCollectionProcessListId(String listcode) {
        DBUtil db;
        int listid = 0;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("select listid from StandardListMaster where listcode=");
            sb.append(StringUtils.getStringQueryParam(listcode));
            sb.append("  and listtype='COLLECTION_PROCESS_STATUS_TYPE'");
            Cursor c = db.selectSQL(sb.toString());
            if (c != null) {
                while (c.moveToNext()) {
                    listid = c.getInt(0);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return listid;
    }

    /**
     * Method to update in haspayment issue value in Retailermaster object
     * @deprecated
     * @See {@link com.ivy.core.data.retailer.RetailerDataManagerImpl#updatePaymentIssue(ArrayList)}
     */
    public void updateHasPaymentIssue(DBUtil db) {
        try {
            String query = "select distinct retailerid from invoicemaster where hasPaymentIssue=1";
            Cursor c = db.selectSQL(query);
            if (c != null) {
                while (c.moveToNext()) {
                    String retailerid = c.getString(0);

                    Vector<RetailerMasterBO> retailerList = bmodel.retailerMaster;
                    for (RetailerMasterBO retailerMasterBO : retailerList) {
                        if (retailerid.equals(retailerMasterBO.getRetailerID())) {
                            retailerMasterBO.setHasPaymentIssue(1);
                            break;
                        }
                    }
                }
                c.close();
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public ArrayList<PaymentBO> getPaymentData(String groupid) {
        ArrayList<PaymentBO> paymentData = new ArrayList<>();
        DBUtil db;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            StringBuffer sb = new StringBuffer();
            sb.append("select billnumber,Amount,ChequeNumber,SLM.ListCode,dateTime,ChequeDate,ChequeNumber,totalDiscount,refno,RM.retailername,RM.retailercode from payment ");
            sb.append("inner join StandardListMaster SLM on SLM.listid=CashMode ");
            sb.append("inner join Retailermaster RM on RM.retailerid=payment.retailerid ");
            sb.append("where payment.groupid=");
            sb.append(groupid);
            sb.append(" order by billnumber");
            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                PaymentBO paymentBO;
                while (c.moveToNext()) {
                    paymentBO = new PaymentBO();
                    paymentBO.setBillNumber(c.getString(0));
                    paymentBO.setAmount(c.getDouble(1));
                    paymentBO.setChequeNumber(c.getString(2));
                    paymentBO.setCashMode(c.getString(3));
                    paymentBO.setCollectionDateTime(c.getString(4));
                    paymentBO.setChequeDate(c.getString(5));
                    paymentBO.setChequeNumber(c.getString(6));
                    paymentBO.setAppliedDiscountAmount(c.getDouble(7));
                    paymentBO.setReferenceNumber(c.getString(8));
                    paymentBO.setRetailerName(c.getString(9));
                    paymentBO.setRetailerCode(c.getString(10));
                    paymentData.add(paymentBO);
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return paymentData;
    }

    public ArrayList<String> getBillNumber(String groupid) {
        ArrayList<String> billNumberList = new ArrayList<>();
        DBUtil db;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            StringBuffer sb = new StringBuffer();
            sb.append("select distinct billnumber from payment ");
            sb.append("where groupid=");
            sb.append(groupid);
            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    billNumberList.add(c.getString(0));
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return billNumberList;
    }

    private double saveSlabWiseDiscount(String uid, double totalCollected, double discountPer, DBUtil db) {
        double discountValue;
        String columns = "uid,discountValue,discountPerc";
        discountValue = ((totalCollected * 100) / (100 - discountPer)) - totalCollected;
        if (bmodel.configurationMasterHelper.ROUND_OF_CONFIG_ENABLED) {
            discountValue = SDUtil.convertToDouble(SDUtil.format(discountValue,
                    0,
                    0, bmodel.configurationMasterHelper.IS_DOT_FOR_GROUP));
        }

        //applying currency rule config
        discountValue = SDUtil.convertToDouble(bmodel.formatBasedOnCurrency(discountValue));

        String values = uid + "," + StringUtils.getStringQueryParam(BigDecimal.valueOf(discountValue)
                .toPlainString()) + "," + discountPer;
        db.insertSQL(DataMembers.tbl_PaymentDiscount_Detail, columns, values);
        return discountValue;
    }

    private void updateInvoiceDisc(String date, ArrayList<InvoiceHeaderBO> invoiceList, DBUtil db) {
        double discountedAmount;
        double invoiceAmount;
        String updateQuery;

        if (invoiceList != null && !invoiceList.isEmpty()) {
            for (InvoiceHeaderBO invoiceHeaderBO : invoiceList) {

                int count = DateTimeUtils.getDateCount(invoiceHeaderBO.getInvoiceDate(),
                        date, "yyyy/MM/dd");
                invoiceAmount = invoiceHeaderBO.getInvoiceAmount();
                double discountpercentage = getDiscountSlabPercent(count + 1);
                double totalDisc = 0;
                double totalBalanceWithDisc = 0;
                double totalRemaingDisc = 0;
                StringBuffer sb = new StringBuffer();
                sb.append("select Round((IM.invnetAmount-ifnull(sum(P.Amount),0)");
                sb.append("-ifnull(sum(PD.discountvalue),0)-IM.paidamount),2),ifnull(sum(PD.discountvalue),0) from invoicemaster IM ");
                sb.append(" Left join Payment P on IM.Invoiceno=P.Billnumber left join paymentdiscountdetail PD on PD.uid=P.uid  ");
                sb.append(" where IM.invoiceno=");
                sb.append(StringUtils.getStringQueryParam(invoiceHeaderBO.getInvoiceNo()));
                Cursor c = db.selectSQL(sb.toString());
                if (c.getCount() > 0) {
                    while (c.moveToNext()) {
                        totalBalanceWithDisc = c.getDouble(0);
                        totalDisc = c.getDouble(1);
                    }
                }

                if (discountpercentage > 0) {
                    totalRemaingDisc = totalBalanceWithDisc * discountpercentage / 100;


                    if (bmodel.configurationMasterHelper.ROUND_OF_CONFIG_ENABLED) {
                        totalRemaingDisc = SDUtil.convertToDouble(SDUtil.format(totalRemaingDisc,
                                0,
                                0, bmodel.configurationMasterHelper.IS_DOT_FOR_GROUP));
                    } else {
                        totalRemaingDisc = SDUtil.convertToDouble(bmodel.formatBasedOnCurrency(totalRemaingDisc));
                    }
                    discountedAmount = totalBalanceWithDisc - totalRemaingDisc;

                    discountedAmount = SDUtil.convertToDouble(bmodel.formatBasedOnCurrency(discountedAmount));

                    updateQuery = "update invoicemaster set discountedAmount=invnetAmount-(paidAmount+"
                            + (totalDisc + totalRemaingDisc) + ")"
                            + ",invoiceAmount="
                            + invoiceAmount
                            + " where invoiceNo="
                            + StringUtils.getStringQueryParam(invoiceHeaderBO.getInvoiceNo());

                    db.executeQ(updateQuery);
                    invoiceHeaderBO.setBalance(discountedAmount);
                } else {

                    updateQuery = "update invoicemaster set discountedAmount="
                            + "invnetamount-(paidAmount+" + totalDisc
                            + "),invoiceAmount="
                            + bmodel.formatValue(invoiceAmount)
                            + " where invoiceNo="
                            + StringUtils.getStringQueryParam(invoiceHeaderBO.getInvoiceNo());

                    db.executeQ(updateQuery);
                    invoiceHeaderBO.setBalance(SDUtil.convertToDouble(SDUtil.roundIt(totalBalanceWithDisc, 2)));
                }
                invoiceHeaderBO.setRemainingDiscountAmt(totalRemaingDisc);
                invoiceHeaderBO.setChkBoxChecked(true);
            }
        }
    }

    public void updateCollectionList(ArrayList<InvoiceHeaderBO> invoiceList, String selectedMode) {
        int add = 0;
        for (InvoiceHeaderBO invoiceHeaderBO : invoiceList) {
            add = add + 1;

            int count = DateTimeUtils.getDateCount(invoiceHeaderBO.getInvoiceDate(),
                    DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), "yyyy/MM/dd");
            double chequeDiscPer = 0;
            double otherModeDisPer = getDiscountSlabPercent(count + 1);
            double discountAmount = 0;
            double totalInvoiceDisc = 0;
            double totalPayableAmtWithDisc = invoiceHeaderBO.getBalance() + invoiceHeaderBO.getRemainingDiscountAmt();
            if (totalPayableAmtWithDisc > 0) {

                PaymentBO paymentBO = mPaymentBOByMode.get(StandardListMasterConstants.CASH);
                if (paymentBO != null) {
                    if (add == 1) {
                        paymentBO.setUpdatePayableamt(paymentBO.getAmount());
                        paymentBO.setDiscountedAmount(0);
                    }
                    if (paymentBO.getUpdatePayableamt() > 0) {

                        discountAmount = getInvoiceDisc(totalPayableAmtWithDisc, paymentBO, otherModeDisPer);
                        totalInvoiceDisc = totalInvoiceDisc + discountAmount;

                        paymentBO.setDiscountedAmount(paymentBO.getDiscountedAmount() + discountAmount);
                        totalPayableAmtWithDisc = updatePayments(totalPayableAmtWithDisc, paymentBO);
                        totalPayableAmtWithDisc = totalPayableAmtWithDisc - discountAmount;
                        if (totalPayableAmtWithDisc == 0) {
                            updateBalanceAndDiscount(invoiceHeaderBO, totalInvoiceDisc);
                        }
                    }
                }
                paymentBO = mPaymentBOByMode.get(StandardListMasterConstants.CREDIT_NOTE);
                if (paymentBO != null) {
                    if (add == 1) {
                        paymentBO.setUpdatePayableamt(paymentBO.getAmount());
                        paymentBO.setDiscountedAmount(0);
                    }
                    if (paymentBO.getUpdatePayableamt() > 0 && totalPayableAmtWithDisc > 0) {
                        //Commented For not applying discount for creditnote
                        //discountAmount = getInvoiceDisc(totalPayableAmtWithDisc, paymentBO, otherModeDisPer);
                        discountAmount = 0;
                        totalInvoiceDisc = totalInvoiceDisc + discountAmount;
                        paymentBO.setDiscountedAmount(paymentBO.getDiscountedAmount() + discountAmount);
                        totalPayableAmtWithDisc = updatePayments(totalPayableAmtWithDisc, paymentBO);
                        totalPayableAmtWithDisc = totalPayableAmtWithDisc - discountAmount;

                        if (totalPayableAmtWithDisc == 0) {
                            updateBalanceAndDiscount(invoiceHeaderBO, totalInvoiceDisc);
                        }
                    }
                }
                paymentBO = mPaymentBOByMode.get(StandardListMasterConstants.ADVANCE_PAYMENT);
                if (paymentBO != null) {
                    if (add == 1) {
                        paymentBO.setUpdatePayableamt(paymentBO.getAmount());
                        paymentBO.setDiscountedAmount(0);
                    }
                    if (paymentBO.getUpdatePayableamt() > 0 && totalPayableAmtWithDisc > 0) {
                        discountAmount = getInvoiceDisc(totalPayableAmtWithDisc, paymentBO, otherModeDisPer);
                        totalInvoiceDisc = totalInvoiceDisc + discountAmount;
                        paymentBO.setDiscountedAmount(paymentBO.getDiscountedAmount() + discountAmount);
                        totalPayableAmtWithDisc = updatePayments(totalPayableAmtWithDisc, paymentBO);
                        totalPayableAmtWithDisc = totalPayableAmtWithDisc - discountAmount;

                        if (totalPayableAmtWithDisc == 0) {
                            updateBalanceAndDiscount(invoiceHeaderBO, totalInvoiceDisc);
                        }
                    }
                }

                paymentBO = mPaymentBOByMode.get(StandardListMasterConstants.MOBILE_PAYMENT);
                if (paymentBO != null) {
                    if (add == 1) {
                        paymentBO.setUpdatePayableamt(paymentBO.getAmount());
                        paymentBO.setDiscountedAmount(0);
                    }
                    if (paymentBO.getUpdatePayableamt() > 0 && totalPayableAmtWithDisc > 0) {
                        discountAmount = getInvoiceDisc(totalPayableAmtWithDisc, paymentBO, otherModeDisPer);
                        totalInvoiceDisc = totalInvoiceDisc + discountAmount;
                        paymentBO.setDiscountedAmount(paymentBO.getDiscountedAmount() + discountAmount);

                        totalPayableAmtWithDisc = updatePayments(totalPayableAmtWithDisc, paymentBO);
                        totalPayableAmtWithDisc = totalPayableAmtWithDisc - discountAmount;

                        if (totalPayableAmtWithDisc <= 0) {
                            updateBalanceAndDiscount(invoiceHeaderBO, totalInvoiceDisc);
                        }
                    }
                }
                paymentBO = mPaymentBOByMode.get(StandardListMasterConstants.COUPON);
                if (paymentBO != null) {
                    if (add == 1) {
                        paymentBO.setUpdatePayableamt(paymentBO.getAmount());
                        paymentBO.setDiscountedAmount(0);
                    }
                    if (paymentBO.getUpdatePayableamt() > 0 && totalPayableAmtWithDisc > 0) {
                        discountAmount = getInvoiceDisc(totalPayableAmtWithDisc, paymentBO, otherModeDisPer);
                        totalInvoiceDisc = totalInvoiceDisc + discountAmount;
                        paymentBO.setDiscountedAmount(paymentBO.getDiscountedAmount() + discountAmount);
                        totalPayableAmtWithDisc = updatePayments(totalPayableAmtWithDisc, paymentBO);
                        totalPayableAmtWithDisc = totalPayableAmtWithDisc - discountAmount;

                        if (totalPayableAmtWithDisc == 0) {
                            updateBalanceAndDiscount(invoiceHeaderBO, totalInvoiceDisc);
                        }
                    }
                }

                paymentBO = mPaymentBOByMode.get(StandardListMasterConstants.DISCOUNT);
                if (paymentBO != null) {
                    if (add == 1) {
                        paymentBO.setUpdatePayableamt(paymentBO.getAmount());
                        paymentBO.setDiscountedAmount(0);
                    }
                    if (paymentBO.getUpdatePayableamt() > 0 && totalPayableAmtWithDisc > 0) {
                        discountAmount = getInvoiceDisc(totalPayableAmtWithDisc, paymentBO, otherModeDisPer);
                        totalInvoiceDisc = totalInvoiceDisc + discountAmount;
                        paymentBO.setDiscountedAmount(paymentBO.getDiscountedAmount() + discountAmount);
                        totalPayableAmtWithDisc = updatePayments(totalPayableAmtWithDisc, paymentBO);
                        totalPayableAmtWithDisc = totalPayableAmtWithDisc - discountAmount;

                        if (totalPayableAmtWithDisc == 0) {
                            updateBalanceAndDiscount(invoiceHeaderBO, totalInvoiceDisc);
                        }
                    }
                }
                paymentBO = mPaymentBOByMode.get(StandardListMasterConstants.CHEQUE);
                if (paymentBO != null) {
                    if (add == 1) {
                        paymentBO.setUpdatePayableamt(paymentBO.getAmount());
                        paymentBO.setDiscountedAmount(0);
                    }
                    if (paymentBO.getChequeDate() != null && !"".equals(paymentBO.getChequeDate())) {
                        count = DateTimeUtils.getDateCount(invoiceHeaderBO.getInvoiceDate(),
                                paymentBO.getChequeDate(), "yyyy/MM/dd");
                    } else {
                        count = 0;
                    }
                    chequeDiscPer = getDiscountSlabPercent(count + 1);
                    if (paymentBO.getUpdatePayableamt() > 0 && totalPayableAmtWithDisc > 0) {

                        discountAmount = getInvoiceDisc(totalPayableAmtWithDisc, paymentBO, chequeDiscPer);
                        totalInvoiceDisc = totalInvoiceDisc + discountAmount;
                        paymentBO.setDiscountedAmount(paymentBO.getDiscountedAmount() + discountAmount);

                        totalPayableAmtWithDisc = updatePayments(totalPayableAmtWithDisc, paymentBO);
                        totalPayableAmtWithDisc = totalPayableAmtWithDisc - discountAmount;

                        if (totalPayableAmtWithDisc == 0) {
                            updateBalanceAndDiscount(invoiceHeaderBO, totalInvoiceDisc);
                        }
                    }
                }

                paymentBO = mPaymentBOByMode.get(StandardListMasterConstants.DEMAND_DRAFT);
                if (paymentBO != null) {
                    if (add == 1) {
                        paymentBO.setUpdatePayableamt(paymentBO.getAmount());
                        paymentBO.setDiscountedAmount(0);
                    }
                    if (paymentBO.getUpdatePayableamt() > 0 && totalPayableAmtWithDisc > 0) {
                        if (totalPayableAmtWithDisc >= paymentBO.getUpdatePayableamt())
                            discountAmount = getInvoiceDisc(totalPayableAmtWithDisc, paymentBO, otherModeDisPer);
                        totalInvoiceDisc = totalInvoiceDisc + discountAmount;
                        paymentBO.setDiscountedAmount(paymentBO.getDiscountedAmount() + discountAmount);

                        totalPayableAmtWithDisc = updatePayments(totalPayableAmtWithDisc, paymentBO);
                        totalPayableAmtWithDisc = totalPayableAmtWithDisc - discountAmount;
                        if (totalPayableAmtWithDisc == 0) {
                            updateBalanceAndDiscount(invoiceHeaderBO, totalInvoiceDisc);
                        }
                    }
                }

                paymentBO = mPaymentBOByMode.get(StandardListMasterConstants.RTGS);
                if (paymentBO != null) {
                    if (add == 1) {
                        paymentBO.setUpdatePayableamt(paymentBO.getAmount());
                        paymentBO.setDiscountedAmount(0);
                    }
                    if (paymentBO.getUpdatePayableamt() > 0 && totalPayableAmtWithDisc > 0) {
                        discountAmount = getInvoiceDisc(totalPayableAmtWithDisc, paymentBO, otherModeDisPer);
                        totalInvoiceDisc = totalInvoiceDisc + discountAmount;
                        paymentBO.setDiscountedAmount(paymentBO.getDiscountedAmount() + discountAmount);
                        totalPayableAmtWithDisc = updatePayments(totalPayableAmtWithDisc, paymentBO);
                        totalPayableAmtWithDisc = totalPayableAmtWithDisc - discountAmount;
                        if (totalPayableAmtWithDisc == 0) {
                            updateBalanceAndDiscount(invoiceHeaderBO, totalInvoiceDisc);
                        }
                    }
                }

                if (totalPayableAmtWithDisc > 0) {
                    double balanceDiscAmt;
                    if (StandardListMasterConstants.CHEQUE.equals(selectedMode)) {
                        balanceDiscAmt = totalPayableAmtWithDisc - totalPayableAmtWithDisc * (100 - chequeDiscPer) / 100;
                    } else {
                        balanceDiscAmt = totalPayableAmtWithDisc - totalPayableAmtWithDisc * (100 - otherModeDisPer) / 100;
                    }
                    totalInvoiceDisc = totalInvoiceDisc + balanceDiscAmt;
                    if (bmodel.configurationMasterHelper.ROUND_OF_CONFIG_ENABLED) {
                        totalInvoiceDisc = SDUtil.convertToDouble(SDUtil.format(totalInvoiceDisc,
                                0,
                                0, bmodel.configurationMasterHelper.IS_DOT_FOR_GROUP));
                    }

                    totalPayableAmtWithDisc = invoiceHeaderBO.getBalance() + invoiceHeaderBO.getRemainingDiscountAmt();
                    invoiceHeaderBO.setBalance(totalPayableAmtWithDisc - totalInvoiceDisc);
                    invoiceHeaderBO.setRemainingDiscountAmt(totalInvoiceDisc);
                }
            }
        }
    }

    private double updatePayments(double totalPayable, PaymentBO paymentBO) {
        if (totalPayable >= paymentBO.getUpdatePayableamt()) {
            totalPayable = totalPayable - paymentBO.getUpdatePayableamt();
            paymentBO.setUpdatePayableamt(0);
        } else if (totalPayable < paymentBO.getUpdatePayableamt()) {
            paymentBO.setUpdatePayableamt(paymentBO.getUpdatePayableamt() - totalPayable);
            totalPayable = 0;
        }
        return totalPayable;
    }

    private double getInvoiceDisc(double totalPayable, PaymentBO paymentBO, double discountPercentage) {
        double discount = 0;
        if (totalPayable >= paymentBO.getUpdatePayableamt()) {
            discount = (paymentBO.getUpdatePayableamt() / ((100 - discountPercentage) / 100)) - paymentBO.getUpdatePayableamt();
        } else if (totalPayable < paymentBO.getUpdatePayableamt()) {
            discount = (totalPayable / ((100 - discountPercentage) / 100)) - totalPayable;
        }

        return discount;
    }

    private void updateBalanceAndDiscount(InvoiceHeaderBO invoiceHeader, double totalDiscount) {
        if (bmodel.configurationMasterHelper.ROUND_OF_CONFIG_ENABLED) {
            totalDiscount = SDUtil.convertToDouble(SDUtil.format(totalDiscount,
                    0,
                    0, bmodel.configurationMasterHelper.IS_DOT_FOR_GROUP));
        }

        double totalAmount = invoiceHeader.getBalance() + invoiceHeader.getRemainingDiscountAmt();
        invoiceHeader.setBalance(totalAmount - totalDiscount);
        invoiceHeader.setRemainingDiscountAmt(totalDiscount);
    }

    public void clearPaymentObjects(PaymentBO paymentBO) {
        if (paymentBO.getAmount() == 0) {
            paymentBO.setChequeDate("");
            paymentBO.setBankID("");
            paymentBO.setBranchId("");
            paymentBO.setBillNumber("");
            paymentBO.setChequeNumber("");
            //paymentBO.setCreditBalancePayment(false);
            paymentBO.setDiscountedAmount(0);
            paymentBO.setBalance(0);
            paymentBO.setReceiptDate("");
            paymentBO.setAccountNumber("");
        }
    }

    public boolean isEnterAmountExceed(ArrayList<PaymentBO> paymentList, String selectedMode) {
        final ArrayList<InvoiceHeaderBO> invoiceList = bmodel.getInvoiceHeaderBO();

        double totalInvoiceAmt = 0;
        double totalPaidAmt = 0;
        double totalDiscountAmt = 0;
        for (InvoiceHeaderBO invoiceHeaderBO : invoiceList) {
            if (invoiceHeaderBO.isChkBoxChecked()) {
                totalInvoiceAmt = totalInvoiceAmt + invoiceHeaderBO.getBalance();
                totalDiscountAmt = totalDiscountAmt + invoiceHeaderBO.getRemainingDiscountAmt();
            }
        }
        totalInvoiceAmt = SDUtil.convertToDouble(bmodel.formatBasedOnCurrency(totalInvoiceAmt));
        if (selectedMode.equals(StandardListMasterConstants.CREDIT_NOTE)) {
            totalInvoiceAmt = totalInvoiceAmt + totalDiscountAmt;
        }

        for (PaymentBO paymentBO : paymentList) {
            totalPaidAmt = totalPaidAmt + paymentBO.getAmount();
        }
        totalPaidAmt = SDUtil.convertToDouble(SDUtil.format(totalPaidAmt,
                bmodel.configurationMasterHelper.VALUE_PRECISION_COUNT,
                0, bmodel.configurationMasterHelper.IS_DOT_FOR_GROUP));

        return totalPaidAmt > totalInvoiceAmt;
    }

    public void saveCollection(ArrayList<InvoiceHeaderBO> invoiceList, ArrayList<PaymentBO> paymentList) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            String columns = "uid,BillNumber,ReceiptDate,InvoiceAmount,Balance,CashMode,ChequeNumber,Amount,RetailerID,BeatID,UserID,BankID,BranchCode,ChequeDate,Date,payType,ImageName,groupId,StatusLovId,totalDiscount,distributorid,DistParentID,ReceiptNo,datetime,refid,refno,PrintFilePath,BankName,BranchName,ridSF,VisitId";
            double calculateCredit = bmodel.getAppDataProvider().getRetailMaster().getCredit_balance();
            String groupID;

            groupID = StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID_MILLIS) + bmodel.getAppDataProvider().getUser().getUserid());

            if (bmodel.configurationMasterHelper.SHOW_COLLECTION_SEQ_NO) {
                String seqNo;
                bmodel.insertSeqNumber("COL");
                seqNo = bmodel.downloadSequenceNo("COL");
                groupID = StringUtils.getStringQueryParam(seqNo);
            }
            collectionGroupId = groupID;

            String groupDate = DateTimeUtils.now(DateTimeUtils.DATE_TIME);
            // insert cash,creditnote,mobile payment,coupon
            boolean isCreditNotePaymentAvailable = false;

            for (InvoiceHeaderBO invoiceHeaderBO : invoiceList) {
                collectionDate = invoiceHeaderBO.getInvoiceDate().replace("/", "");
                for (PaymentBO paymentBO : paymentList) {

                    if (bmodel.configurationMasterHelper.SHOW_INVOICE_CREDIT_BALANCE
                            && bmodel.retailerMasterBO.getCredit_balance() != -1) { // update
                        // credit
                        // balance
                        if (paymentBO.isCreditBalancePayment()) {
                            calculateCredit = calculateCredit + paymentBO.getAmount();

                            bmodel.retailerMasterBO.setCredit_balance((float) calculateCredit);
                            bmodel.retailerMasterBO.setRField1("" + (float) calculateCredit);
                        }
                    }
                    if (!StandardListMasterConstants.CHEQUE.equals(paymentBO.getCashMode()) &&
                            !StandardListMasterConstants.DEMAND_DRAFT.equals(paymentBO.getCashMode()) &&
                            !StandardListMasterConstants.RTGS.equals(paymentBO.getCashMode())) {
                        if (invoiceHeaderBO.getBalance() > 0 && paymentBO.getUpdatePayableamt() > 0) {
                            if (StandardListMasterConstants.CREDIT_NOTE.equals(paymentBO.getCashMode())) {
                                isCreditNotePaymentAvailable = true;
                            }
                            insertCollectionRecords(paymentBO, invoiceHeaderBO, columns, db, groupID, groupDate);
                        }
                    }
                }

                // insert cheque,dd and rtgs
                for (PaymentBO paymentBO : paymentList) {
                    if (StandardListMasterConstants.CHEQUE.equals(paymentBO.getCashMode()) ||
                            StandardListMasterConstants.DEMAND_DRAFT.equals(paymentBO.getCashMode()) ||
                            StandardListMasterConstants.RTGS.equals(paymentBO.getCashMode())) {
                        if (invoiceHeaderBO.getBalance() > 0 && paymentBO.getUpdatePayableamt() > 0) {
                            insertCollectionRecords(paymentBO, invoiceHeaderBO, columns, db, groupID, groupDate);
                        }
                    }
                }
            }
            if (isCreditNotePaymentAvailable)
                updateCreditNote(db);
            if (bmodel.configurationMasterHelper.SHOW_DISC_AMOUNT_ALLOW) {
                updateInvoiceDisc(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), invoiceList, db);
            }

            if (bmodel.configurationMasterHelper.SHOW_INVOICE_CREDIT_BALANCE
                    && bmodel.retailerMasterBO.getCredit_balance() != -1) {
                calculateCredit = calculateCredit + bmodel.retailerMasterBO.getCredit_balance();
                db.updateSQL("UPDATE RetailerMaster SET RField1 = '"
                        + calculateCredit + "' WHERE RetailerID = '"
                        + bmodel.retailerMasterBO.getRetailerID() + "'");
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private void insertCollectionRecords(PaymentBO paymentBO, InvoiceHeaderBO invoiceHeaderBO, String columns, DBUtil db, String groupID, String groupDate) {
        double collectedAmount = 0;
        String printFilePath = "";
        if (paymentBO.getUpdatePayableamt() > 0) {
            if (bmodel.configurationMasterHelper.IS_PRINT_FILE_SAVE) {
                printFilePath = StandardListMasterConstants.PRINT_FILE_PATH + bmodel.getAppDataProvider().getUser().getDownloadDate().replace("/", "") + "/"
                        + bmodel.getAppDataProvider().getUser().getUserid() + "/" +
                        StandardListMasterConstants.PRINT_FILE_COLLECTION + groupID.replaceAll("\'", "") + ".txt";
            }

            if (!StandardListMasterConstants.CREDIT_NOTE.equals(paymentBO.getCashMode()) && !StandardListMasterConstants.ADVANCE_PAYMENT.equals(paymentBO.getCashMode())) {
                if (invoiceHeaderBO.getBalance() >= paymentBO.getUpdatePayableamt()) {
                    collectedAmount = paymentBO.getUpdatePayableamt();
                    invoiceHeaderBO.setBalance(invoiceHeaderBO.getBalance() - paymentBO.getUpdatePayableamt());
                    paymentBO.setUpdatePayableamt(0);

                } else if (invoiceHeaderBO.getBalance() < paymentBO.getUpdatePayableamt()) {
                    collectedAmount = invoiceHeaderBO.getBalance();
                    paymentBO.setUpdatePayableamt(paymentBO.getUpdatePayableamt() - invoiceHeaderBO.getBalance());
                    invoiceHeaderBO.setBalance(0);
                }

                String modeID = bmodel.getStandardListIdAndType(
                        paymentBO.getCashMode(),
                        StandardListMasterConstants.COLLECTION_PAY_TYPE);
                String payTypeID = bmodel.getStandardListIdAndType(
                        StandardListMasterConstants.COLLECTION_NORMAL_PAYMENT,
                        StandardListMasterConstants.COLLECTION_TRANSACTION_PAYMENT_TYPE);
                if (invoiceHeaderBO.isDebitNote()) {
                    payTypeID = bmodel.getStandardListIdAndType(
                            StandardListMasterConstants.COLLECTION_DEBIT_NOTE_PAYMENT,
                            StandardListMasterConstants.COLLECTION_TRANSACTION_PAYMENT_TYPE);
                }


                int listid;
                if (paymentBO.isCreditBalancePayment()) {
                    listid = getCollectionProcessListId("CLOSED");
                } else {
                    listid = getCollectionProcessListId("OPEN");
                }
                String payID = StringUtils.getStringQueryParam("P"
                        + bmodel.getAppDataProvider().getUser().getUserid()
                        + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID_MILLIS));

                double totalDiscount = 0;
                if (bmodel.configurationMasterHelper.SHOW_DISC_AMOUNT_ALLOW) {
                    String date;
                    if (StandardListMasterConstants.CHEQUE.equalsIgnoreCase(paymentBO.getCashMode()) ||
                            StandardListMasterConstants.RTGS.equalsIgnoreCase(paymentBO.getCashMode()) ||
                            StandardListMasterConstants.DEMAND_DRAFT.equalsIgnoreCase(paymentBO.getCashMode())) {
                        date = DateTimeUtils.convertToServerDateFormat(paymentBO.getChequeDate(), "yyyy/MM/dd");
                    } else {
                        date = DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL);
                    }
                    final int count = DateTimeUtils.getDateCount(invoiceHeaderBO.getInvoiceDate(),
                            date, "yyyy/MM/dd");

                    final double discountpercentage = getDiscountSlabPercent(count + 1);
                    if (discountpercentage > 0)
                        totalDiscount = saveSlabWiseDiscount(payID, collectedAmount, discountpercentage, db);
                }
                String chequeNumber = "";
                if (!paymentBO.getCashMode().equals(StandardListMasterConstants.CASH))
                    chequeNumber = paymentBO.getChequeNumber();

                String values = payID + "," + StringUtils.getStringQueryParam(invoiceHeaderBO.getInvoiceNo())
                        + "," + StringUtils.getStringQueryParam(invoiceHeaderBO.getInvoiceDate()) + ","
                        + StringUtils.getStringQueryParam(invoiceHeaderBO.getInvoiceAmount() + "") + "," + invoiceHeaderBO.getBalance()
                        + "," + StringUtils.getStringQueryParam(modeID) + ","
                        + StringUtils.getStringQueryParam(chequeNumber) + ","
                        + StringUtils.getStringQueryParam(bmodel.formatBasedOnCurrency(collectedAmount)) + ","
                        + StringUtils.getStringQueryParam(bmodel.retailerMasterBO.getRetailerID()) + ","
                        + StringUtils.getStringQueryParam(bmodel.getAppDataProvider().getRetailMaster().getBeatID() + "") + ","
                        + StringUtils.getStringQueryParam(bmodel.getAppDataProvider().getUser().getUserid() + "") + ","
                        + StringUtils.getStringQueryParam(paymentBO.getBankID()) + ","
                        + StringUtils.getStringQueryParam(paymentBO.getBranchId()) + ","
                        + StringUtils.getStringQueryParam(paymentBO.getChequeDate()) + ","
                        + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) + ","
                        + StringUtils.getStringQueryParam(payTypeID) + ","
                        + StringUtils.getStringQueryParam(paymentBO.getImageName())
                        + "," + (groupID) + "," + listid + "," + StringUtils.getStringQueryParam(BigDecimal.valueOf(totalDiscount)
                        .toPlainString())
                        + "," + bmodel.getAppDataProvider().getRetailMaster().getDistributorId()
                        + "," + bmodel.getAppDataProvider().getRetailMaster().getDistParentId()
                        + "," + StringUtils.getStringQueryParam(receiptno)
                        + "," + StringUtils.getStringQueryParam(groupDate) + ",0,0"
                        + "," + StringUtils.getStringQueryParam(printFilePath)
                        + "," + StringUtils.getStringQueryParam(paymentBO.getBankName() != null ? paymentBO.getBankName() : "")
                        + "," + StringUtils.getStringQueryParam(paymentBO.getBranchName() != null ? paymentBO.getBranchName() : "")
                        + "," + StringUtils.getStringQueryParam(bmodel.getAppDataProvider().getRetailMaster().getRidSF())
                        + "," + bmodel.getAppDataProvider().getUniqueId();
                //+ "," + AppUtils.getStringQueryParam(paymentBO.getAccountNumber());

                db.insertSQL(DataMembers.tbl_Payment, columns, values);
            } else if (StandardListMasterConstants.CREDIT_NOTE.equals(paymentBO.getCashMode())) {

                final List<CreditNoteListBO> creditNoteList = getCreditNoteList();
                for (CreditNoteListBO creditNoteListBO : creditNoteList) {
                    if (creditNoteListBO.getRetailerId().equals(
                            bmodel.getAppDataProvider().getRetailMaster().getRetailerID())
                            && !creditNoteListBO.isUsed() && creditNoteListBO.isChecked()) {
                        if (paymentBO.getUpdatePayableamt() > 0 && creditNoteListBO.getAmount() > 0 && invoiceHeaderBO.getBalance() > 0) {
                            insertCreditNoteCollection(paymentBO, invoiceHeaderBO, creditNoteListBO, db, groupID, columns, groupDate, true);
                        }
                    }
                }
            } else if (StandardListMasterConstants.ADVANCE_PAYMENT.equals(paymentBO.getCashMode())) {
                for (CreditNoteListBO creditNoteListBO : mAdvancePaymentList) {
                    if (!creditNoteListBO.isUsed() && creditNoteListBO.isChecked()) {
                        if (paymentBO.getUpdatePayableamt() > 0 && creditNoteListBO.getAmount() > 0 && invoiceHeaderBO.getBalance() > 0) {
                            insertCreditNoteCollection(paymentBO, invoiceHeaderBO, creditNoteListBO, db, groupID, columns, groupDate, false);
                        }
                    }
                }
            }
        }
    }

    private void
    insertCreditNoteCollection(PaymentBO paymentBO, InvoiceHeaderBO invoiceHeaderBO, CreditNoteListBO creditNoteListBO, DBUtil db, String groupID, String columns, String groupDate, boolean isDisNotApplyForCreditNote) {
        double collectedAmount = 0;
        String printFilePath = "";
        columns = "uid,BillNumber,ReceiptDate,InvoiceAmount,Balance,CashMode,ChequeNumber,Amount,RetailerID,BeatID,UserID,BankID,BranchCode,ChequeDate,Date,payType,ImageName,groupId,StatusLovId,totalDiscount,distributorid,DistParentID,ReceiptNo,datetime,refid,refno,PrintFilePath,ridSF,VisitId";
        if (bmodel.configurationMasterHelper.IS_PRINT_FILE_SAVE) {
            printFilePath = StandardListMasterConstants.PRINT_FILE_PATH + bmodel.userMasterHelper.getUserMasterBO().getDownloadDate().replace("/", "") + "/"
                    + bmodel.userMasterHelper.getUserMasterBO().getUserid() + "/" +
                    StandardListMasterConstants.PRINT_FILE_COLLECTION + groupID.replaceAll("\'", "") + ".txt";
            ;
        }

        if (paymentBO.getUpdatePayableamt() >= creditNoteListBO.getAmount()) {
            if (invoiceHeaderBO.getBalance() > creditNoteListBO.getAmount()) {
                collectedAmount = creditNoteListBO.getAmount();
                invoiceHeaderBO.setBalance(invoiceHeaderBO.getBalance() - creditNoteListBO.getAmount());
                creditNoteListBO.setAmount(0);

            } else if (invoiceHeaderBO.getBalance() <= creditNoteListBO.getAmount()) {
                collectedAmount = invoiceHeaderBO.getBalance();
                invoiceHeaderBO.setBalance(0);
                creditNoteListBO.setAmount(creditNoteListBO.getAmount() - collectedAmount);
            }
        } else {
            if (invoiceHeaderBO.getBalance() > paymentBO.getUpdatePayableamt()) {
                collectedAmount = paymentBO.getUpdatePayableamt();
                invoiceHeaderBO.setBalance(invoiceHeaderBO.getBalance() - paymentBO.getUpdatePayableamt());
                creditNoteListBO.setAmount(creditNoteListBO.getAmount() - collectedAmount);
            } else if (invoiceHeaderBO.getBalance() <= paymentBO.getUpdatePayableamt()) {
                collectedAmount = invoiceHeaderBO.getBalance();
                invoiceHeaderBO.setBalance(0);
                creditNoteListBO.setAmount(creditNoteListBO.getAmount() - collectedAmount);
            }
        }
        paymentBO.setUpdatePayableamt(BigDecimal.valueOf(paymentBO.getUpdatePayableamt()).subtract(BigDecimal.valueOf(collectedAmount)).doubleValue());

        String modeID = bmodel.getStandardListIdAndType(
                StandardListMasterConstants.CREDIT_NOTE,
                StandardListMasterConstants.COLLECTION_PAY_TYPE);
        String payTypeID = bmodel.getStandardListIdAndType(
                StandardListMasterConstants.COLLECTION_NORMAL_PAYMENT,
                StandardListMasterConstants.COLLECTION_TRANSACTION_PAYMENT_TYPE);

        int listid;
        if (paymentBO.isCreditBalancePayment()) {
            listid = getCollectionProcessListId("CLOSED");
        } else {
            listid = getCollectionProcessListId("OPEN");
        }
        String payID = StringUtils.getStringQueryParam("P"
                + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID_MILLIS));

        double totalDiscount = 0;
        if (bmodel.configurationMasterHelper.SHOW_DISC_AMOUNT_ALLOW) {
            String date;
            if (StandardListMasterConstants.CHEQUE.equalsIgnoreCase(paymentBO.getCashMode()) ||
                    StandardListMasterConstants.RTGS.equalsIgnoreCase(paymentBO.getCashMode()) ||
                    StandardListMasterConstants.DEMAND_DRAFT.equalsIgnoreCase(paymentBO.getCashMode())) {
                date = DateTimeUtils.convertToServerDateFormat(paymentBO.getChequeDate(), "yyyy/MM/dd");
            } else {
                date = DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL);
            }
            final int count = DateTimeUtils.getDateCount(invoiceHeaderBO.getInvoiceDate(),
                    date, "yyyy/MM/dd");

            if (!isDisNotApplyForCreditNote) {
                final double discountpercentage = getDiscountSlabPercent(count + 1);
                if (discountpercentage > 0)
                    totalDiscount = saveSlabWiseDiscount(payID, collectedAmount, discountpercentage, db);
            }
        }

        String values = payID + "," + StringUtils.getStringQueryParam(invoiceHeaderBO.getInvoiceNo())
                + "," + StringUtils.getStringQueryParam(invoiceHeaderBO.getInvoiceDate()) + ","
                + StringUtils.getStringQueryParam(invoiceHeaderBO.getInvoiceAmount() + "") + "," + invoiceHeaderBO.getBalance()
                + "," + StringUtils.getStringQueryParam(modeID) + ","
                + StringUtils.getStringQueryParam("") + ","
                + StringUtils.getStringQueryParam(collectedAmount + "") + ","
                + StringUtils.getStringQueryParam(bmodel.retailerMasterBO.getRetailerID()) + ","
                + StringUtils.getStringQueryParam(bmodel.getRetailerMasterBO().getBeatID() + "") + ","
                + bmodel.userMasterHelper.getUserMasterBO().getUserid() + ","
                + StringUtils.getStringQueryParam(paymentBO.getBankID()) + ","
                + StringUtils.getStringQueryParam(paymentBO.getBranchId()) + ","
                + StringUtils.getStringQueryParam(paymentBO.getChequeDate()) + ","
                + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) + ","
                + StringUtils.getStringQueryParam(payTypeID) + ","
                + StringUtils.getStringQueryParam(paymentBO.getImageName())
                + "," + (groupID) + "," + listid + "," + StringUtils.getStringQueryParam(BigDecimal.valueOf(totalDiscount)
                .toPlainString())
                + "," + bmodel.getRetailerMasterBO().getDistributorId()
                + "," + bmodel.getRetailerMasterBO().getDistParentId()
                + "," + StringUtils.getStringQueryParam(receiptno)
                + "," + StringUtils.getStringQueryParam(groupDate) + "," + creditNoteListBO.getRefid() + "," + StringUtils.getStringQueryParam(creditNoteListBO.getId())
                + "," + StringUtils.getStringQueryParam(printFilePath)
                + "," + StringUtils.getStringQueryParam(bmodel.getAppDataProvider().getRetailMaster().getRidSF())
                + "," + bmodel.getAppDataProvider().getUniqueId();

        db.insertSQL(DataMembers.tbl_Payment, columns, values);
        String created_date = StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL));
        StringBuffer sb = new StringBuffer();
        sb.append("update creditnote set Actualamount=Actualamount-" + collectedAmount + ",Appliedamount=Appliedamount+" + collectedAmount);
        sb.append(",upload='N',date=" + created_date + "where id=" + StringUtils.getStringQueryParam(creditNoteListBO.getId()));
        db.executeQ(sb.toString());

        sb = new StringBuffer();
        sb.append("update creditnote set isused=1");
        sb.append(" where amount=Appliedamount and id=" + StringUtils.getStringQueryParam(creditNoteListBO.getId()));
        db.executeQ(sb.toString());

    }

    private void updateCreditNote(DBUtil db) {
        final List<CreditNoteListBO> creditNoteList = getCreditNoteList();
        for (CreditNoteListBO creditNoteListBO : creditNoteList) {
            if (creditNoteListBO.getRetailerId().equals(
                    bmodel.getRetailerMasterBO().getRetailerID())
                    && !creditNoteListBO.isUsed() && creditNoteListBO.isChecked()) {
                StringBuffer sb = new StringBuffer();
                sb.append("update CreditNote set isUsed=1");
                sb.append(" where id=" + StringUtils.getStringQueryParam(creditNoteListBO.getId()));
                sb.append(" and retailerid=");
                sb.append(bmodel.getRetailerMasterBO().getRetailerID());
                sb.append(" and Actualamount=0");
                db.executeQ(sb.toString());
                sb = new StringBuffer();
                sb.append("update CreditNote set upload='N'");
                sb.append(" where id=" + StringUtils.getStringQueryParam(creditNoteListBO.getId()));
                sb.append(" and retailerid=");
                sb.append(bmodel.getRetailerMasterBO().getRetailerID());
                db.executeQ(sb.toString());


                creditNoteListBO.setUsed(true);
            }
        }
    }

    public void updateCreditNoteACtualAmt() {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        db.createDataBase();
        db.openDataBase();
        String query = "update creditnote set Actualamount=amount where Actualamount==0";
        db.executeQ(query);
        db.closeDB();
    }

    /**
     * Download bank details from StandardListMaster of type BANK_TYPE.
     */
    public void downloadBankDetails() {
        BankMasterBO inv;
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
        db.openDataBase();
        Cursor c = db.selectSQL("SELECT ListName, ListId FROM StandardListMaster WHERE ListType = 'BANK_TYPE'");
        if (c != null) {
            bankMaster = new ArrayList<>();
            while (c.moveToNext()) {
                inv = new BankMasterBO();
                inv.setBankName(c.getString(0));
                inv.setBankId(c.getInt(1));
                bankMaster.add(inv);
            }
            c.close();
        }
        db.closeDB();
    }

    /**
     * Download Retailer Account Details
     */
    public void downloadRetailerAccountDetails() {
        RetailerWiseBankDetailsBO retailerBankBO;
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
        db.openDataBase();
        Cursor c = db.selectSQL("SELECT retailer_id, bank_account_no FROM RetailerWiseBankInfo");
        if (c != null) {
            retailerWiseBankDetailsBOArrayList = new ArrayList<>();
            while (c.moveToNext()) {
                retailerBankBO = new RetailerWiseBankDetailsBO();
                retailerBankBO.setRetailer_id(c.getString(0));
                retailerBankBO.setBank_Account_no(c.getString(1));
                retailerWiseBankDetailsBOArrayList.add(retailerBankBO);
            }
            c.close();
        }
        db.closeDB();
    }

    /**
     * Download bank branch details from Standard Details of type BANK_BRANCH_TYPE.
     */
    public void downloadBranchDetails() {
        BranchMasterBO inv;
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
        db.openDataBase();
        Cursor c = db.selectSQL("SELECT ListId, Parentid, ListName, ListCode FROM StandardListMaster WHERE ListType = 'BANK_BRANCH_TYPE'");
        if (c != null) {
            bankBranch = new ArrayList<>();
            while (c.moveToNext()) {
                inv = new BranchMasterBO();
                inv.setBranchID(c.getString(0));
                inv.setBankID(c.getString(1));
                inv.setBranchName(c.getString(2));
                inv.setBankbranchCode(c.getString(3));
                bankBranch.add(inv);
            }
            c.close();
        }
        db.closeDB();
    }


    public ArrayList<BranchMasterBO> getBranchMasterBO() {
        return bankBranch;
    }

    public ArrayList<BankMasterBO> getBankMasterBO() {
        return bankMaster;
    }

    public ArrayList<RetailerWiseBankDetailsBO> getRetailerWiseBankDetailsBOArrayList() {
        return retailerWiseBankDetailsBOArrayList;
    }

    /**
     * set discount slab Amount in discountSlab list
     */
    public void downloadDiscountSlab() {
        mDiscountSlabList = new ArrayList<>();
        DiscontSlabBO discountSlabBO;

        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            StringBuffer sb = new StringBuffer();
            sb.append("select minvalue,maxvalue,value,SLM.listname from DiscountProductMapping DPM ");
            sb.append("inner join Discountmaster DM on DM.discountid=DPM.discountid ");
            sb.append("left join Standardlistmaster slm on SLM.listid =DM.typeid ");
            sb.append("where dm.DiscountId in (select DiscountId from DiscountMapping ");
            sb.append("where (Retailerid in(" + bmodel.getRetailerMasterBO().getRetailerID() + ",0) AND ");
            sb.append(" distributorid in(" + bmodel.getRetailerMasterBO().getDistributorId() + ",0) AND ");
            sb.append(" Channelid in (" + bmodel.getRetailerMasterBO().getSubchannelid() + ",");
            String channelid = bmodel.channelMasterHelper.getChannelHierarchy(bmodel.getRetailerMasterBO().getSubchannelid(), mContext);
            if (channelid.equals("")) {
                channelid = "0";
            }
            sb.append(channelid + ",0) AND ");
            String locid = bmodel.channelMasterHelper.getLocationHierarchy(mContext);
            if (locid.equals("")) {
                locid = "0";
            }
            sb.append(" locationid in(" + locid + ",0) AND ");
            sb.append(" Accountid =" + bmodel.getRetailerMasterBO().getAccountid() + "))");
            sb.append(" and DM.moduleid=(select ListId from StandardListMaster where ListCode='COLLECTION')");
            // String query = "select fromRange,toRange,percent,description from discountslab";
            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    discountSlabBO = new DiscontSlabBO();
                    discountSlabBO.setFromRange(c.getInt(0));
                    discountSlabBO.setToRange(c.getInt(1));
                    discountSlabBO.setPercent(c.getDouble(2));
                    discountSlabBO.setDescribtion(c.getString(3));
                    mDiscountSlabList.add(discountSlabBO);
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public double getSlabwiseDiscountpercentage() {
        if (mDiscountSlabList != null && mDiscountSlabList.size() > 0) {
            for (DiscontSlabBO discountSlabBO : mDiscountSlabList) {
                if (discountSlabBO.getFromRange() == 1) {
                    return discountSlabBO.getPercent();
                }
            }
        }
        return 0;
    }

    /**
     * Download invoices of a particular retailer where discountedAmount is 0 and upload is Y.
     * Truncate Invoice amount to two digit decimal place and update the Invoice master.
     * update invoice discounted amount using slab wise
     */
    public void updateInvoiceDiscountedAmount() {
        InvoiceHeaderBO invoiceHeaderBO;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("select InvoiceNo,InvoiceDate,invNetamount,paidAmount from invoicemaster ");
            sb.append("where retailerid = ");
            sb.append(StringUtils.getStringQueryParam(bmodel.getRetailerMasterBO().getRetailerID()));
            sb.append(" and discountedAmount==0 and upload='Y'");
            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    invoiceHeaderBO = new InvoiceHeaderBO();
                    invoiceHeaderBO.setInvoiceNo(c.getString(0));
                    invoiceHeaderBO.setInvoiceDate(c.getString(1));
                    invoiceHeaderBO.setInvoiceAmount(c.getDouble(2));
                    invoiceHeaderBO.setPaidAmount(c.getDouble(3));
                    setInvoiceDiscoutAmount(invoiceHeaderBO, db);
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    //used pending invoice report wiht retailerid condition
    public void updateInvoiceDiscountAmount() {
        InvoiceHeaderBO invoiceHeaderBO;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("select InvoiceNo,InvoiceDate,invNetamount,paidAmount from invoicemaster ");
            sb.append("where discountedAmount==0 and upload='Y'");
            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    invoiceHeaderBO = new InvoiceHeaderBO();
                    invoiceHeaderBO.setInvoiceNo(c.getString(0));
                    invoiceHeaderBO.setInvoiceDate(c.getString(1));
                    invoiceHeaderBO.setInvoiceAmount(c.getDouble(2));
                    invoiceHeaderBO.setPaidAmount(c.getDouble(3));
                    setInvoiceDiscoutAmount(invoiceHeaderBO, db);
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }


    /**
     * update discount amount in invoice header master
     *
     * @param invoiceHeaderBO header bo
     */
    private void setInvoiceDiscoutAmount(InvoiceHeaderBO invoiceHeaderBO, DBUtil db) {
        try {
            double discountedAmount;
            double invoiceAmount;
            String updateQuery;
//            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
//            );
//            db.createDataBase();
//            db.openDataBase();
            // configuration for discount slab apply or not

            invoiceAmount = Utils.round(invoiceHeaderBO.getInvoiceAmount(),
                    bmodel.configurationMasterHelper.VALUE_PRECISION_COUNT);
            int precison = bmodel.configurationMasterHelper.VALUE_PRECISION_COUNT;

            if (bmodel.configurationMasterHelper.SHOW_DISC_AMOUNT_ALLOW) {
                if (bmodel.configurationMasterHelper.ROUND_OF_CONFIG_ENABLED) {
                    precison = 0;
                }
                int count = DateTimeUtils.getDateCount(invoiceHeaderBO.getInvoiceDate(),
                        DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), "yyyy/MM/dd");

                double discountpercentage = getDiscountSlabPercent(count + 1);

                if (invoiceHeaderBO.getPaidAmount() == 0) {
                    if (discountpercentage > 0) {
                        discountedAmount = invoiceHeaderBO.getInvoiceAmount()
                                - (invoiceHeaderBO.getInvoiceAmount()
                                * discountpercentage / 100);

                        discountedAmount = Utils
                                .round(discountedAmount,
                                        precison);
                    } else {
                        discountedAmount = Utils
                                .round(invoiceHeaderBO.getInvoiceAmount(),
                                        precison);
                    }

                    updateQuery = "update invoicemaster set discountedAmount="
                            + bmodel.formatBasedOnCurrency(discountedAmount)
                            + ",invoiceAmount="
                            + invoiceAmount
                            + " where invoiceNo="
                            + StringUtils.getStringQueryParam(invoiceHeaderBO.getInvoiceNo())
                            + " and upload='Y'";
                    db.executeQ(updateQuery);
                } else {
                    double balanceAmt = invoiceHeaderBO.getInvoiceAmount() - invoiceHeaderBO.getPaidAmount();

                    discountedAmount = Utils
                            .round((balanceAmt * (100 - discountpercentage) / 100),
                                    precison);

                    updateQuery = "update invoicemaster set discountedAmount="
                            + bmodel.formatBasedOnCurrency(discountedAmount) + ",invoiceAmount="
                            + invoiceAmount + " where invoiceNo="
                            + StringUtils.getStringQueryParam(invoiceHeaderBO.getInvoiceNo())
                            + " and upload='Y'";

                    db.executeQ(updateQuery);
                }
            } else {
                discountedAmount = Utils.round(
                        invoiceHeaderBO.getInvoiceAmount() - invoiceHeaderBO.getPaidAmount(),
                        precison);

                updateQuery = "update invoicemaster set discountedAmount="
                        + bmodel.formatBasedOnCurrency(discountedAmount) + ",invoiceAmount=" + invoiceAmount
                        + " where invoiceNo="
                        + StringUtils.getStringQueryParam(invoiceHeaderBO.getInvoiceNo())
                        + " and upload='Y'";
                db.executeQ(updateQuery);
            }
//            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    /**
     * if range between fromRange to toRange,it will return percentage
     *
     * @param range range
     * @return double percent for given range
     */
    public double getDiscountSlabPercent(int range) {
        if (mDiscountSlabList != null) {
            for (DiscontSlabBO discountSlabBo : mDiscountSlabList) {
                if (discountSlabBo.getFromRange() <= range
                        && discountSlabBo.getToRange() >= range) {
                    return discountSlabBo.getPercent();
                }
            }
        }
        return 0.0;
    }

    /**
     * @param paymentList  - total payment list
     * @param isCreditNote - if  true check creditnote ,else check Advance payment
     * @return - balance amount value
     */
    public double getBalanceAmountWithOutCreditNote(ArrayList<PaymentBO> paymentList, boolean isCreditNote) {
        double balanceAmt;
        final ArrayList<InvoiceHeaderBO> invoiceList = bmodel.getInvoiceHeaderBO();

        double totalInvoiceAmt = 0;
        double totalPaidAmt = 0;
        for (InvoiceHeaderBO invoiceHeaderBO : invoiceList) {
            if (invoiceHeaderBO.isChkBoxChecked()) {
                totalInvoiceAmt = totalInvoiceAmt + invoiceHeaderBO.getBalance();
            }
        }

        for (PaymentBO paymentBO : paymentList) {

            if (isCreditNote && !StandardListMasterConstants.CREDIT_NOTE.equals(paymentBO.getCashMode()))
                totalPaidAmt = totalPaidAmt + paymentBO.getAmount();
            else if (!isCreditNote && !StandardListMasterConstants.ADVANCE_PAYMENT.equals(paymentBO.getCashMode()))
                totalPaidAmt = totalPaidAmt + paymentBO.getAmount();
        }
        balanceAmt = totalInvoiceAmt - totalPaidAmt;
        return balanceAmt;
    }

    public void saveAdvancePayment(PaymentBO paymentBO) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            final boolean isCreditBalanceCheck = isCreditBalancebalance(paymentBO.getCashMode());

            int listid;
            if (isCreditBalanceCheck) {
                listid = getCollectionProcessListId("CLOSED");
            } else {
                listid = getCollectionProcessListId("OPEN");
            }

            String creditNoteId = StringUtils.getStringQueryParam("AP"
                    + bmodel.getAppDataProvider().getUser().getUserid()
                    + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID));

            String modeID = bmodel.getStandardListIdAndType(
                    paymentBO.getCashMode(),
                    StandardListMasterConstants.COLLECTION_PAY_TYPE);
            String payTypeID = bmodel.getStandardListIdAndType(
                    StandardListMasterConstants.COLLECTION_ADVANCED_PAYMENT,
                    StandardListMasterConstants.COLLECTION_TRANSACTION_PAYMENT_TYPE);
            String payID = StringUtils.getStringQueryParam("P"
                    + bmodel.getAppDataProvider().getUser().getUserid()
                    + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID_MILLIS));
            collectionGroupId = payID;

            String columns = "uid,CashMode,ChequeNumber,Amount,RetailerID,BeatID,UserID,BankID,BranchCode,ChequeDate,Date,payType,ImageName,datetime,billnumber,refid,StatusLovId,groupid,BankName,BranchName,ridSF,VisitId";
            StringBuffer sb = new StringBuffer();
            String chequeNo = "";
            if (paymentBO.getCashMode().equals(StandardListMasterConstants.CHEQUE))
                chequeNo = paymentBO.getChequeNumber();
            else
                paymentBO.setChequeDate("");
            sb.append(payID + "," + modeID + "," + StringUtils.getStringQueryParam(chequeNo) + "," + paymentBO.getAmount());
            sb.append("," + StringUtils.getStringQueryParam(bmodel.getAppDataProvider().getRetailMaster().getRetailerID()) + "," + bmodel.getAppDataProvider().getRetailMaster().getBeatID());
            sb.append("," + bmodel.getAppDataProvider().getUser().getUserid() + "," + StringUtils.getStringQueryParam(paymentBO.getBankID()) + "," + StringUtils.getStringQueryParam(paymentBO.getBranchId()));
            sb.append("," + StringUtils.getStringQueryParam(paymentBO.getChequeDate()) + "," + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) + "," + StringUtils.getStringQueryParam(payTypeID));
            sb.append("," + StringUtils.getStringQueryParam(paymentBO.getImageName()) + "," + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_TIME)) + ",0,0");
            sb.append("," + listid + "," + payID);
            sb.append("," + StringUtils.getStringQueryParam(paymentBO.getBankName()));
            sb.append("," + StringUtils.getStringQueryParam(paymentBO.getBranchName()));
            sb.append(",");
            sb.append(StringUtils.getStringQueryParam(bmodel.getAppDataProvider().getRetailMaster().getRidSF()));
            sb.append(",");
            sb.append(bmodel.getAppDataProvider().getUniqueId());

            modeID = bmodel.getStandardListIdAndType(
                    "CNAP",
                    StandardListMasterConstants.CREDIT_NOTE_TYPE);
            db.insertSQL(DataMembers.tbl_Payment, columns, sb.toString());

            String creditNoteColumns = "id,refno,amount,retailerid,date,creditnotetype,upload,Actualamount";
            sb = new StringBuffer();
            sb.append(creditNoteId + "," + payID + "," + paymentBO.getAmount() + "," + StringUtils.getStringQueryParam(bmodel.getAppDataProvider().getRetailMaster().getRetailerID()));
            sb.append("," + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) + "," + modeID + ",'N'" + "," + paymentBO.getAmount() + "," + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)));
            db.insertSQL(DataMembers.tbl_credit_note, creditNoteColumns + ",CreatedDate", sb.toString());
            db.closeDB();

        } catch (Exception e) {
            Commons.print(e.getMessage());
        }

    }

    public boolean isUseAllAdvancePaymentAmt() {
        boolean flag;
        final ArrayList<InvoiceHeaderBO> invoiceList = bmodel.getInvoiceHeaderBO();

        double totalInvoiceAmt = 0;
        double totalCreditNoteAmt = 0;
        double totalPaidCreditAmt = 0;
        for (InvoiceHeaderBO invoiceHeaderBO : invoiceList) {
            if (invoiceHeaderBO.isChkBoxChecked()) {
                totalInvoiceAmt = totalInvoiceAmt + invoiceHeaderBO.getBalance();
            }
        }

        for (CreditNoteListBO creditNoteListBO : mAdvancePaymentList) {
            totalCreditNoteAmt = totalCreditNoteAmt + creditNoteListBO.getAmount();
            if (creditNoteListBO.isChecked()) {
                totalPaidCreditAmt = totalPaidCreditAmt + creditNoteListBO.getAmount();
            }


        }
        if (totalCreditNoteAmt == totalPaidCreditAmt) {
            flag = true;
        } else {
            if (totalPaidCreditAmt >= totalInvoiceAmt) {
                flag = true;
            } else {
                flag = false;
            }
        }
        return flag;
    }

    public boolean isPaidAmountwithoutAdvanePayment() {
        boolean flag = false;
        for (PaymentBO paymentBO : mPaymentList) {
            if (!paymentBO.equals(StandardListMasterConstants.ADVANCE_PAYMENT)) {
                if (paymentBO.getAmount() > 0) {
                    flag = true;
                }
            }
        }
        return flag;
    }

    public void downloadCollectionMethods() {
        if (bmodel.configurationMasterHelper.SHOW_DISC_AMOUNT_ALLOW) {
            downloadDiscountSlab();
        }

        downloadBankDetails();
        downloadBranchDetails();
        downloadRetailerAccountDetails();
        updateInvoiceDiscountedAmount();

        bmodel.downloadInvoice(bmodel.getRetailerMasterBO().getRetailerID(), "COL");
        loadPaymentMode();
    }

    public void loadCollectionReference() {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("select BillNumber,ContactName,ContactNumber,DocRefNo,ReasonID,Remarks,SignaturePath,IsDoc,Signatureimage from CollectionDocument ");
            sb.append("where RetailerID = ");
            sb.append(StringUtils.getStringQueryParam(bmodel.getRetailerMasterBO().getRetailerID()));
            sb.append(" and upload='N'");
            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    String billNo = c.getString(0);
                    String contactName = c.getString(1);
                    String contactNo = c.getString(2);
                    String docRefNo = c.getString(3);
                    String reasonID = c.getString(4);
                    String remark = c.getString(5);
                    String signPath = c.getString(6);
                    int isDocExchange = c.getInt(7);
                    String imgName = c.getString(8);
                    setRefDetails(billNo, contactName, contactNo, docRefNo, reasonID, remark, signPath, isDocExchange, imgName);
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private void setRefDetails(String billNo, String contactName, String contactNo, String docRefNo, String reasonId,
                               String remark, String signPath, int isDocExchange, String imgName) {

        for (InvoiceHeaderBO invoiceHeaderBO : bmodel.getInvoiceHeaderBO()) {
            if (invoiceHeaderBO.getInvoiceNo().equalsIgnoreCase(billNo)) {
                invoiceHeaderBO.setContactName(contactName);
                invoiceHeaderBO.setContactNo(contactNo);
                invoiceHeaderBO.setDocRefNo(docRefNo);
                invoiceHeaderBO.setDocReasonId(reasonId);
                invoiceHeaderBO.setDocRemark(remark);
                invoiceHeaderBO.setDocSignPath(signPath);
                invoiceHeaderBO.setDocExchange(isDocExchange);
                invoiceHeaderBO.setDocSignImage(imgName);
                break;
            }
        }
    }

    public void saveCollectionReference(ArrayList<InvoiceHeaderBO> collectionRefList) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            String columns = "uid,BillNumber,ContactName,ContactNumber,RetailerID,DocRefNo,ReasonID,Remarks,SignaturePath,IsDoc,Signatureimage";

            String payID = StringUtils.getStringQueryParam("CRF"
                    + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                    + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID_MILLIS));

            db.deleteSQL("CollectionDocument", "RetailerID=" + StringUtils.getStringQueryParam(bmodel.getRetailerMasterBO().getRetailerID()) + "and upload = 'N'", false);

            for (int i = 0; i < collectionRefList.size(); i++) {

                if ((collectionRefList.get(i).getContactName() != null && collectionRefList.get(i).getContactName().length() > 0) ||
                        (collectionRefList.get(i).getContactNo() != null && collectionRefList.get(i).getContactNo().length() > 0) ||
                        (collectionRefList.get(i).getDocRefNo() != null && collectionRefList.get(i).getDocRefNo().length() > 0) ||
                        (collectionRefList.get(i).getDocReasonId() != null && collectionRefList.get(i).getDocReasonId().length() > 0) ||
                        (collectionRefList.get(i).getDocSignPath() != null && collectionRefList.get(i).getDocSignPath().length() > 0) ||
                        (collectionRefList.get(i).getDocRemark() != null && collectionRefList.get(i).getDocRemark().length() > 0)) {

                    String values = payID
                            + ","
                            + StringUtils.getStringQueryParam(collectionRefList.get(i).getInvoiceNo())
                            + ","
                            + StringUtils.getStringQueryParam(collectionRefList.get(i).getContactName())
                            + ","
                            + StringUtils.getStringQueryParam(collectionRefList.get(i).getContactNo())
                            + ","
                            + StringUtils.getStringQueryParam(bmodel.getRetailerMasterBO().getRetailerID())
                            + ","
                            + StringUtils.getStringQueryParam(collectionRefList.get(i).getDocRefNo())
                            + ","
                            + StringUtils.getStringQueryParam(collectionRefList.get(i).getDocReasonId())
                            + ","
                            + StringUtils.getStringQueryParam(collectionRefList.get(i).getDocRemark())
                            + ","
                            + StringUtils.getStringQueryParam(collectionRefList.get(i).getDocSignPath())
                            + ","
                            + collectionRefList.get(i).getDocExchange()
                            + ","
                            + StringUtils.getStringQueryParam(collectionRefList.get(i).getDocSignImage());


                    db.insertSQL("CollectionDocument", columns, values);
                }
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("CollectionDocument insert" + e);
        }
    }

    public boolean checkRetailerWiseAccountMatched(String accountNo) {
        for (RetailerWiseBankDetailsBO retailerBankBO : getRetailerWiseBankDetailsBOArrayList()) {
            if (retailerBankBO.getRetailer_id().equals(bmodel.retailerMasterBO.getRetailerID())
                    && retailerBankBO.getBank_Account_no().equals(accountNo))
                return true;
        }
        return false;
    }

    public ArrayList<NoCollectionReasonBo> loadInvoiceList(String id, Context context) {

        ArrayList<NoCollectionReasonBo> mInvioceList = new ArrayList<>();
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();

            Cursor c = db.selectSQL("SELECT distinct Inv.InvoiceNo, Inv.InvoiceDate, Round(invNetamount,2) as Inv_amt," +
                    " Round(IFNULL((select sum(payment.Amount) from payment where payment.BillNumber=Inv.InvoiceNo),0),2) as RcvdAmt," +
                    " CDD.ReasonId,CDD.ReasonOthers,Inv.RetailerId FROM InvoiceMaster Inv LEFT JOIN payment ON payment.BillNumber = Inv.InvoiceNo " +
                    " LEFT JOIN PaymentDiscountDetail PD ON payment.uid = PD.uid left join CollectionDueDetails CDD on CDD.InvoiceNo = Inv.InvoiceNo " +
                    " WHERE inv.Retailerid ='" + id + "'  AND inv.DocStatus ='COL'  GROUP BY Inv.InvoiceNo ORDER BY Inv.InvoiceDate");

            if (c != null) {
                while (c.moveToNext()) {
                    NoCollectionReasonBo invoiceHeaderBO = new NoCollectionReasonBo();
                    invoiceHeaderBO.setInvoiceNo(c.getString(0));
                    invoiceHeaderBO.setInvoiceDate(c.getString(1));
                    invoiceHeaderBO.setInvoiceAmount(c.getDouble(2));
                    invoiceHeaderBO.setPaidAmount(c.getDouble(3));
                    invoiceHeaderBO.setNoCollectionReasonId(c.getString(4));
                    invoiceHeaderBO.setNoCollectionReason(c.getString(5));
                    invoiceHeaderBO.setRetailerId(c.getString(6));

                    if (invoiceHeaderBO.getPaidAmount() == 0)
                        mInvioceList.add(invoiceHeaderBO);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }

        return mInvioceList;
    }

    public boolean checkInvoiceWithReason(String retailId, Context context) {
        boolean isReasonGiven = true;

        for (NoCollectionReasonBo noCollectionReasonBo :
                loadInvoiceList(retailId, context)) {

            if ((noCollectionReasonBo.getNoCollectionReasonId() == null
                    || noCollectionReasonBo.getNoCollectionReasonId().equals("0") ||
                    noCollectionReasonBo.getNoCollectionReasonId().isEmpty())
                    && noCollectionReasonBo.getPaidAmount() == 0) {

                isReasonGiven = false;
                break;
            }
        }

        return isReasonGiven;
    }
}
