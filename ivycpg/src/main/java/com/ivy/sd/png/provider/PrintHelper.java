package com.ivy.sd.png.provider;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;
import android.util.SparseArray;

import com.ivy.cpg.view.collection.CollectionHelper;
import com.ivy.cpg.view.order.OrderHelper;
import com.ivy.cpg.view.order.scheme.SchemeDetailsMasterHelper;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.BomReturnBO;
import com.ivy.sd.png.bo.PaymentBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.cpg.view.order.scheme.SchemeProductBO;
import com.ivy.sd.png.bo.StoreWiseDiscountBO;
import com.ivy.cpg.view.order.tax.TaxBO;
import com.ivy.sd.png.commons.NumberToWord;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.cpg.view.collection.CollectionFragmentNew;
import com.ivy.utils.DateTimeUtils;
import com.zebra.sdk.printer.PrinterLanguage;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

public class PrintHelper {
    private final Context mContext;
    private final BusinessModel bmodel;
    private static PrintHelper instance = null;
    OrderHelper orderHelper;

    private double mEmpTotalValue;
    private double mCaseTotalValue;
    private double mPcTotalValue;
    private double mVatValue = 0;
    private double mNhlValue = 0;
    private double mtotalExcludeTaxAmount;
    private double mTotalOrderValue;
    private double vatPercentage = 0, nhlPercentage = 0;
    private String mVatName = "";
    private String mNhilName = "";

    private Bitmap m_bmp;
    private final ArrayList<BomReturnBO> mEmptyLiaProductsForAdapter = new ArrayList<>();
    private final ArrayList<BomReturnBO> mEmptyRetProductsForAdapter = new ArrayList<>();
    private ArrayList<ProductMasterBO> mProductsForAdapter = new ArrayList<>();

    private CollectionHelper collectionHelper;

    private PrintHelper(Context context) {
        this.mContext = context;
        this.bmodel = (BusinessModel) context;
        orderHelper = OrderHelper.getInstance(context);
        collectionHelper = CollectionHelper.getInstance(context);
    }

    public static PrintHelper getInstance(Context context) {
        if (instance == null) {
            instance = new PrintHelper(context);
        }
        return instance;
    }

    public byte[] printDataforTitan3inchOrderprinter(List<ProductMasterBO> mOrderedProductList, int printCount) {
        byte[] printDataBytes = null;
        StringBuffer sb = new StringBuffer();
        try {
            int height = 200
                    + (mOrderedProductList.size() * 40) + 180;
            sb.append("! 0 200 200 " + (height * (printCount + 1)) + " 1\r\n"
                    + "LEFT\r\n");

            for (int j = 0; j <= printCount; j++) {
                int totalLength = height * j;
                sb.append("T 5 0 200 " + (5 + totalLength));
                sb.append(" Order Receipt " + "\r\n");
                sb.append("T 5 0 10 " + (55 + totalLength));
                sb.append(" Order No :" + orderHelper.getOrderId().replaceAll("\'", "") + "\r\n");
                sb.append("T 5 0 350 " + (55 + totalLength));
                sb.append("Date:" + DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL) + "\r\n");
                sb.append("T 5 0 10 " + (120 + totalLength));
                if (bmodel.userMasterHelper.getUserMasterBO().getDistributorName() != null) {
                    sb.append(" " + bmodel.userMasterHelper.getUserMasterBO().getDistributorName() + "\r\n");
                } else {
                    sb.append(" " + "\r\n");
                }
                sb.append("T 5 0 10 " + (150 + totalLength));
                if (bmodel.userMasterHelper.getUserMasterBO().getDistributorAddress3() != null) {
                    sb.append(" " + bmodel.userMasterHelper.getUserMasterBO().getDistributorAddress3() + " \r\n");
                } else {
                    sb.append(" " + "\r\n");
                }
                sb.append("T 5 0 10 " + (180 + totalLength));
                if (bmodel.userMasterHelper.getUserMasterBO().getDistributorContactNumber() != null) {
                    sb.append(" " + bmodel.userMasterHelper.getUserMasterBO().getDistributorContactNumber() + " \r\n");
                } else {
                    sb.append(" " + "\r\n");
                }

                sb.append("T 5 0 300 " + (85 + totalLength));
                sb.append(" Delivery on:" + bmodel.getDeliveryDate(OrderHelper.getInstance(mContext).selectedOrderId,bmodel.getRetailerMasterBO().getRetailerID()) + "\r\n");
                String retailerName = "";
                if (bmodel.getRetailerMasterBO().getRetailerName() != null && !"".equals(bmodel.getRetailerMasterBO().getRetailerName())
                        && !"null".equals(bmodel.getRetailerMasterBO().getRetailerName())) {
                    if (bmodel.getRetailerMasterBO().getRetailerName().length() > 11)
                        retailerName = bmodel.getRetailerMasterBO().getRetailerName().substring(
                                0, 11);
                    else
                        retailerName = bmodel.getRetailerMasterBO().getRetailerName();
                }

                sb.append("T 5 0 300 " + (115 + totalLength));
                sb.append(" " + bmodel.getRetailerMasterBO().getRetailerCode() + " " + retailerName + " \r\n");
                sb.append("T 5 0 300 " + (145 + totalLength));
                if (bmodel.getRetailerMasterBO().getAddress3() != null) {
                    sb.append(" " + bmodel.getRetailerMasterBO().getAddress3() + " \r\n");
                } else {
                    sb.append(" " + "\r\n");
                }
                sb.append("T 5 0 10 " + (210 + totalLength));
                sb.append("---------------------------------------------------------------------------------------" + "\r\n");
                sb.append("T 5 0 10 " + (230 + totalLength));
                sb.append(" Variant " + "\r\n");
                sb.append("T 5 0 250 " + (230 + totalLength));
                sb.append(" Order Qty " + "\r\n");
                sb.append("T 5 0 400 " + (230 + totalLength));
                sb.append("Scheme UCP " + "\r\n");
                sb.append("T 5 0 10 " + (260 + totalLength));
                sb.append("---------------------------------------------------------------------------------------" + "\r\n");

                double totalValue = 0;
                int totalQty = 0;

                int x = 260 + totalLength;
                for (ProductMasterBO productBO : mOrderedProductList) {
                    x = x + 30;
                    String productname;
                    if (productBO.getProductShortName() != null && !"".equals(productBO.getProductShortName())
                            && !"null".equals(productBO.getProductShortName())) {
                        if (productBO.getProductShortName().length() > 18)
                            productname = productBO.getProductShortName().substring(
                                    0, 18);
                        else
                            productname = productBO.getProductShortName();
                    } else {
                        if (productBO.getProductName().length() > 18)
                            productname = productBO.getProductName().substring(
                                    0, 18);
                        else
                            productname = productBO.getProductName();
                    }
                    sb.append("T 5 0 10 " + x);
                    sb.append(" " + productname + " \r\n");

                    int totalProductQty = productBO.getOrderedPcsQty() + (productBO.getOrderedCaseQty() * productBO.getCaseSize())
                            + (productBO.getOrderedOuterQty() * productBO.getOutersize());
                    totalQty = totalQty + totalProductQty;

                    sb.append("T 5 0 280 " + x);
                    sb.append(" " + totalProductQty + " \r\n");

                    double totalProductValue = totalProductQty * productBO.getSrp();
                    totalValue = totalValue + totalProductValue;

                    sb.append("T 5 0 430 " + x);
                    sb.append(" " + totalProductValue + " \r\n");
                }
                x = x + 50;
                sb.append("T 5 0 10 " + x);
                sb.append("Total " + "\r\n");
                sb.append("T 5 0 280 " + x);
                sb.append(" " + totalQty + " \r\n");
                sb.append("T 5 0 430 " + x);
                sb.append(" " + totalValue + " \r\n");

                x = x + 30;
                sb.append("T 5 0 10 " + x);
                sb.append("---------------------------------------------------------------------------------------" + "\r\n");

                x = x + 30;

                sb.append("T 5 0 10 " + x);
                sb.append("Dealer Signature " + "\r\n");

                sb.append("T 5 0 300 " + x);
                sb.append("For RS Signature " + "\r\n");
            }
            sb.append("PRINT \r\n");
            printDataBytes = sb.toString().getBytes();
        } catch (Exception e) {
            Commons.printException(e + "");
        }
        return printDataBytes;
    }


    public byte[] printCollection(boolean isOriginal) {
        byte[] PrintDataBytes = null;
        try {
            ArrayList<PaymentBO> paymentList = collectionHelper.getPaymentData(collectionHelper.collectionGroupId);
            PrinterLanguage printerLanguage = PrinterLanguage.CPCL;
            // 00:22:58:3D:7E:83 - RW420
            // AC:3F:A4:16:B9:AE - IMZ320
            byte[] configLabel = null;
            if (printerLanguage == PrinterLanguage.ZPL) {
                configLabel = "^XA^FO17,16^GB379,371,8^FS^FT65,255^A0N,135,134^FDTEST^FS^XZ"
                        .getBytes();
            } else if (printerLanguage == PrinterLanguage.CPCL) {

                if (paymentList.size() > 0) {

                    if (bmodel.configurationMasterHelper.IS_SHOW_PRINT_LANGUAGE_THAI) {
                        return printThai(isOriginal, paymentList);
                    }

                    int height = 0;
                    int x = 190;

                    int size = 1;

                    height = x + 600 + (paymentList.size() * 70);
                    // Commons.print(TAG + "Heigt:" + height);
                    String Printitem = "! 0 200 200 " + height + " 1\r\n"
                            + "CENTER\r\n";

                    if (bmodel.configurationMasterHelper.SHOW_PRINT_HEADERS) {
                        Printitem += "T 5 1 10 60 "
                                + ""
                                + "Unipal General Trading Company" + "\r\n";

                        Printitem += "T 5 0 10 110 "
                                + ""
                                + "VAT No : 562414227" + "\r\n";

                        Printitem += "T 5 0 10 130 "
                                + ""
                                + "Ramallah - Industrial zone, Tel: +972 2 2981060" + "\r\n";
                        Printitem += "T 5 0 10 150 "
                                + ""
                                + "Gaza - lndus. Zone - Carny, Tel: +972 7 2830324" + "\r\n";
                    } else {
                        Printitem += "T 5 1 10 60 "
                                + ""
                                + bmodel.userMasterHelper.getUserMasterBO()
                                .getDistributorName() + "\r\n";

                        Printitem += "T 5 0 10 110 "
                                + ""
                                + bmodel.userMasterHelper.getUserMasterBO()
                                .getDistributorTinNumber() + "\r\n";

                        Printitem += "T 5 0 10 130 "
                                + ""
                                + bmodel.userMasterHelper.getUserMasterBO()
                                .getDistributorAddress1() + "\r\n";
                        Printitem += "T 5 0 10 150 "
                                + ""
                                + bmodel.userMasterHelper.getUserMasterBO()
                                .getDistributorAddress2() + "\r\n";
                    }

                    if (isOriginal)
                        Printitem += "T 5 0 10 170 "
                                + ""
                                + "Original Print" + "\r\n";
                    else
                        Printitem += "T 5 0 10 170 "
                                + ""
                                + "Duplicate print" + "\r\n";

                    // T- Text // // Font Size // Spacing // height between lines

                    double total = 0;

                    PaymentBO payHeaderBO = paymentList.get(0);
                    x += 10;
                    Printitem += "T 5 0 10 " + x + " " + "--------------------------------------------------\r\n";

                    x += 20;
                    Printitem += "LEFT \r\n";
                    Printitem += "T 5 0 10 " + x + " "
                            + "Rcpt Date:"
                            + ""
                            + payHeaderBO.getCollectionDateTime() + "\r\n";
                    x += 40;
                    Printitem += "T 5 0 10 " + x + " "
                            + "Rcpt NO"
                            + ":"
                            + collectionHelper.collectionGroupId.replaceAll("\'", "")
                            + "\r\n";

                    x += 40;
                    Printitem += "T 5 0 260 " + x + " "
                            + "AgentName"
                            + ":"
                            + bmodel.userMasterHelper.getUserMasterBO().getUserName()
                            + "\r\n";

                    Printitem += "LEFT \r\n";
                    Printitem += "T 5 0 10 " + x + " "
                            + "AgentCode:"
                            + ""
                            + bmodel.userMasterHelper.getUserMasterBO()
                            .getUserCode() + "\r\n";
                    String retailername = "";
                    if (bmodel.getRetailerMasterBO().getRetailerName().length() > 30) {
                        retailername = bmodel.getRetailerMasterBO().getRetailerName().substring(0, 30);
                    } else {
                        retailername = bmodel.getRetailerMasterBO().getRetailerName();
                    }


                    x += 40;
                    Printitem += "T 5 0 10 " + x + " "
                            + "CustName"
                            + ":"
                            + retailername
                            + "\r\n";

                    if (bmodel.getRetailerMasterBO().getRetailerName().length() > 12) {


                        x += 40;
                        Printitem += "T 5 0 260 " + x + " "

                                + bmodel.getRetailerMasterBO().getRetailerName().substring(12)
                                + "\r\n";
                    }


                    Printitem += "LEFT \r\n";
                    Printitem += "T 5 0 10 " + x + " "
                            + "CustCode:"
                            + ""
                            + bmodel.getRetailerMasterBO().getRetailerCode()
                            + "\r\n";

                    Printitem += "\r\n";


                    x += 40;
                    Printitem += "T 5 0 10 " + x + " " + "--------------------------------------------------\r\n";

                    x += 20;
                    Printitem += "T 5 0 10 " + x + " Inv No" + "\r\n";

                    x += 20;
                    Printitem += "T 5 0 80 " + x + " Type" + "\r\n";
                    Printitem += "T 5 0 180 " + x + " Date" + "\r\n";
                    Printitem += "T 5 0 310 " + x + " Chq Num" + "\r\n";
                    Printitem += "T 5 0 450 " + x + " Total" + "\r\n";

                    x += 20;
                    Printitem += "T 5 0 10 " + x + " --------------------------------------------------\r\n";
                    double totalDiscount = 0;
                    for (PaymentBO payBO : paymentList) {

                        x += 40;

                        Printitem += "T 5 0 10 " + x + " "
                                + payBO.getBillNumber()
                                + "\r\n";

                        x += 30;
                        if (payBO.getCashMode().equals(StandardListMasterConstants.CREDIT_NOTE)) {
                            if (payBO.getReferenceNumber().startsWith("AP")) {
                                Printitem += "T 5 0 80 " + x + " "
                                        + mContext.getResources().getString(R.string.advance_payment) + "\r\n";
                            } else {
                                Printitem += "T 5 0 80 " + x + " "
                                        + mContext.getResources().getString(R.string.credit_note) + "\r\n";
                            }
                        } else {
                            if (payBO.getCashMode().equals(StandardListMasterConstants.CASH)) {
                                Printitem += "T 5 0 80 " + x + " "
                                        + mContext.getResources().getString(R.string.cash) + "\r\n";
                            } else if (payBO.getCashMode().equals(StandardListMasterConstants.CHEQUE)) {
                                Printitem += "T 5 0 80 " + x + " "
                                        + mContext.getResources().getString(R.string.cheque) + "\r\n";
                            } else if (payBO.getCashMode().equals(StandardListMasterConstants.DEMAND_DRAFT)) {
                                Printitem += "T 5 0 80 " + x + " "
                                        + "DD" + "\r\n";
                            } else if (payBO.getCashMode().equals(StandardListMasterConstants.RTGS)) {
                                Printitem += "T 5 0 80 " + x + " "
                                        + mContext.getResources().getString(R.string.rtgs) + "\r\n";
                            } else if (payBO.getCashMode().equals(StandardListMasterConstants.MOBILE_PAYMENT)) {
                                Printitem += "T 5 0 80 " + x + " "
                                        + "Mob.Pay" + "\r\n";
                            }
                        }


                        Printitem += "T 5 0 180 " + x + " "
                                + payBO.getChequeDate()
                                + "\r\n";
                        x = x - 30;

                        if (!payBO.getCashMode().equals(StandardListMasterConstants.CREDIT_NOTE) && !payBO.getCashMode().equals(StandardListMasterConstants.ADVANCE_PAYMENT))
                            Printitem += "T 5 0 310 " + x + " "
                                    + payBO.getChequeNumber() + "\r\n";
                        else {
                            Printitem += "T 5 0 310 " + x + " "
                                    + payBO.getReferenceNumber() + "\r\n";
                        }

                        x = x + 30;

                        if (!payBO.getCashMode().equals(StandardListMasterConstants.CREDIT_NOTE) && !payBO.getCashMode().equals(StandardListMasterConstants.ADVANCE_PAYMENT))
                            Printitem += "T 5 0 310 " + x + " "
                                    + payBO.getChequeNumber() + "\r\n";


                        Printitem += "T 5 0 450 " + x + " "
                                + bmodel.formatValue(payBO.getAmount())
                                + "\r\n";

                        total += payBO.getAmount();

                        totalDiscount += payBO.getAppliedDiscountAmount();

                    }

                    x += 30;
                    Printitem += "T 5 0 10 "
                            + x
                            + " --------------------------------------------------\r\n";
                    x += 30;

                    Printitem += "T 5 0 20 " + x + "Discount " + "\r\n";

                    Printitem += "T 5 0 140 " + x + " "
                            + bmodel.formatValue(totalDiscount) + "\r\n";

                    Printitem += "T 5 0 390 " + x + "Total " + "\r\n";

                    Printitem += "RIGHT \r\n";
                    Printitem += "T 5 0 460 " + x + " "
                            + bmodel.formatValue(total) + "\r\n";

                    x += 30;
                    Printitem += "T 5 0 0 "
                            + x
                            + " --------------------------------------------------\r\n";


                    x += 50;

                    Printitem += "\r\n";
                    Printitem += "\r\n";
                    Printitem += "\r\n";

                    Printitem += "T 5 0 10 " + x + "Comments:" + "\r\n";
                    Printitem += "T 5 0 150 " + x + " --------------------------\r\n";

                    x += 50;
                    Printitem += "T 5 0 10 " + x + "Signature:" + "\r\n";
                    Printitem += "T 5 0 150 " + x + " --------------------------\r\n";


                    Printitem += "PRINT\r\n";

                    PrintDataBytes = Printitem.getBytes();
                }

            }
        } catch (Exception e) {
        }
        return PrintDataBytes;
    }

    public byte[] printAdvancePayment(boolean isOriginal) {
        byte[] PrintDataBytes = null;
        try {
            ArrayList<PaymentBO> paymentList = collectionHelper.getPaymentData(collectionHelper.collectionGroupId);
            PrinterLanguage printerLanguage = PrinterLanguage.CPCL;
            // 00:22:58:3D:7E:83 - RW420
            // AC:3F:A4:16:B9:AE - IMZ320
            byte[] configLabel = null;
            if (printerLanguage == PrinterLanguage.ZPL) {
                configLabel = "^XA^FO17,16^GB379,371,8^FS^FT65,255^A0N,135,134^FDTEST^FS^XZ"
                        .getBytes();
            } else if (printerLanguage == PrinterLanguage.CPCL) {


                if (paymentList.size() > 0) {

                    int height = 0;
                    int x = 190;

                    int size = 1;

                    height = x + 600 + 70;
                    // Commons.print(TAG + "Heigt:" + height);
                    String Printitem = "! 0 200 200 " + height + " 1\r\n"
                            + "CENTER\r\n";

                    if (bmodel.configurationMasterHelper.SHOW_PRINT_HEADERS) {
                        Printitem += "T 5 1 10 60 "
                                + ""
                                + "Unipal General Trading Company" + "\r\n";

                        Printitem += "T 5 0 10 110 "
                                + ""
                                + "VAT No : 562414227" + "\r\n";

                        Printitem += "T 5 0 10 130 "
                                + ""
                                + "Ramallah - Industrial zone, Tel: +972 2 2981060" + "\r\n";
                        Printitem += "T 5 0 10 150 "
                                + ""
                                + "Gaza - lndus. Zone - Carny, Tel: +972 7 2830324" + "\r\n";
                    } else {
                        Printitem += "T 5 1 10 60 "
                                + ""
                                + bmodel.userMasterHelper.getUserMasterBO()
                                .getDistributorName() + "\r\n";

                        Printitem += "T 5 0 10 110 "
                                + ""
                                + bmodel.userMasterHelper.getUserMasterBO()
                                .getDistributorTinNumber() + "\r\n";

                        Printitem += "T 5 0 10 130 "
                                + ""
                                + bmodel.userMasterHelper.getUserMasterBO()
                                .getDistributorAddress1() + "\r\n";
                        Printitem += "T 5 0 10 150 "
                                + ""
                                + bmodel.userMasterHelper.getUserMasterBO()
                                .getDistributorAddress2() + "\r\n";
                    }

                    if (isOriginal)
                        Printitem += "T 5 0 10 170 "
                                + ""
                                + "Original Print" + "\r\n";
                    else
                        Printitem += "T 5 0 10 170 "
                                + ""
                                + "Duplicate print" + "\r\n";

                    // T- Text // // Font Size // Spacing // height between lines

                    double total = 0;

                    PaymentBO payHeaderBO = paymentList.get(0);
                    x += 10;
                    Printitem += "T 5 0 10 " + x + " " + "--------------------------------------------------\r\n";

                    x += 20;
                    Printitem += "LEFT \r\n";
                    Printitem += "T 5 0 10 " + x + " "
                            + "Rcpt Date:"
                            + ""
                            + payHeaderBO.getCollectionDateTime() + "\r\n";
                    x += 40;
                    Printitem += "T 5 0 10 " + x + " "
                            + "Rcpt NO"
                            + ":"
                            + collectionHelper.collectionGroupId
                            + "\r\n";

                    x += 40;
                    Printitem += "T 5 0 260 " + x + " "
                            + "AgentName"
                            + ":"
                            + bmodel.userMasterHelper.getUserMasterBO().getUserName()
                            + "\r\n";

                    Printitem += "LEFT \r\n";
                    Printitem += "T 5 0 10 " + x + " "
                            + "AgentCode:"
                            + ""
                            + bmodel.userMasterHelper.getUserMasterBO()
                            .getUserCode() + "\r\n";
                    String retailername = "";
                    if (bmodel.getRetailerMasterBO().getRetailerName().length() > 30) {
                        retailername = bmodel.getRetailerMasterBO().getRetailerName().substring(0, 30);
                    } else {
                        retailername = bmodel.getRetailerMasterBO().getRetailerName();
                    }


                    x += 40;
                    Printitem += "LEFT \r\n";
                    Printitem += "T 5 0 10 " + x + " "
                            + "CustName"
                            + ":"
                            + retailername
                            + "\r\n";

                    x += 40;
                    Printitem += "T 5 0 10 " + x + " "
                            + "CustCode:"
                            + ""
                            + bmodel.getRetailerMasterBO().getRetailerCode()
                            + "\r\n";

                    Printitem += "\r\n";


                    x += 40;
                    Printitem += "T 5 0 10 " + x + " " + "--------------------------------------------------\r\n";

                    x += 20;


                    x += 20;
                    Printitem += "T 5 0 20 " + x + " Mode" + "\r\n";
                    Printitem += "T 5 0 120 " + x + " Date" + "\r\n";
                    Printitem += "T 5 0 240 " + x + " Chq Num" + "\r\n";
                    Printitem += "T 5 0 370 " + x + " Total" + "\r\n";

                    x += 20;
                    Printitem += "T 5 0 10 " + x + " --------------------------------------------------\r\n";
                    double totalDiscount = 0;
                    for (PaymentBO payBO : paymentList) {
                        if (payBO.getAmount() > 0) {

                            x += 40;

                            Printitem += "T 5 0 10 " + x + " "
                                    + " Advance Payment "
                                    + "\r\n";

                            x += 30;
                            if (payBO.getCashMode().equals(StandardListMasterConstants.CREDIT_NOTE)) {
                                if (payBO.getReferenceNumber().startsWith("AP")) {
                                    Printitem += "T 5 0 20 " + x + " "
                                            + mContext.getResources().getString(R.string.advance_payment) + "\r\n";
                                } else {
                                    Printitem += "T 5 0 20 " + x + " "
                                            + mContext.getResources().getString(R.string.credit_note) + "\r\n";
                                }
                            } else {
                                if (payBO.getCashMode().equals(StandardListMasterConstants.CASH)) {
                                    Printitem += "T 5 0 20 " + x + " "
                                            + mContext.getResources().getString(R.string.cash) + "\r\n";
                                } else if (payBO.getCashMode().equals(StandardListMasterConstants.CHEQUE)) {
                                    Printitem += "T 5 0 20 " + x + " "
                                            + mContext.getResources().getString(R.string.cheque) + "\r\n";
                                } else if (payBO.getCashMode().equals(StandardListMasterConstants.DEMAND_DRAFT)) {
                                    Printitem += "T 5 0 20 " + x + " "
                                            + "DD" + "\r\n";
                                } else if (payBO.getCashMode().equals(StandardListMasterConstants.RTGS)) {
                                    Printitem += "T 5 0 20 " + x + " "
                                            + mContext.getResources().getString(R.string.rtgs) + "\r\n";
                                } else if (payBO.getCashMode().equals(StandardListMasterConstants.MOBILE_PAYMENT)) {
                                    Printitem += "T 5 0 20 " + x + " "
                                            + "Mob.Pay" + "\r\n";
                                }
                            }


                            Printitem += "T 5 0 120 " + x + " "
                                    + payBO.getChequeDate()
                                    + "\r\n";

                            if (!payBO.getCashMode().equals(StandardListMasterConstants.CASH))
                                Printitem += "T 5 0 240 " + x + " "
                                        + payBO.getChequeNumber() + "\r\n";


                            Printitem += "T 5 0 370 " + x + " "
                                    + bmodel.formatValue(payBO.getAmount())
                                    + "\r\n";

                            total += payBO.getAmount();

                            totalDiscount += payBO.getAppliedDiscountAmount();
                        }

                    }

                    x += 30;
                    Printitem += "T 5 0 10 "
                            + x
                            + " --------------------------------------------------\r\n";
                    x += 30;


                    Printitem += "T 5 0 300 " + x + "Total " + "\r\n";

                    Printitem += "RIGHT \r\n";
                    Printitem += "T 5 0 370 " + x + " "
                            + bmodel.formatValue(total) + "\r\n";

                    x += 30;
                    Printitem += "T 5 0 0 "
                            + x
                            + " --------------------------------------------------\r\n";


                    x += 50;

                    Printitem += "\r\n";
                    Printitem += "\r\n";
                    Printitem += "\r\n";

                    Printitem += "T 5 0 10 " + x + "Comments:" + "\r\n";
                    Printitem += "T 5 0 150 " + x + " --------------------------\r\n";

                    x += 50;
                    Printitem += "T 5 0 10 " + x + "Signature:" + "\r\n";
                    Printitem += "T 5 0 150 " + x + " --------------------------\r\n";


                    Printitem += "PRINT\r\n";

                    PrintDataBytes = Printitem.getBytes();
                }

            }
        } catch (Exception e) {
        }
        return PrintDataBytes;
    }

    public byte[] printDataforTitan3inchprinter(List<ProductMasterBO> mOrderedProductList, double entryLevelDis, int printCount, boolean isFromInvoiceReport) {
        byte[] printDataBytes = null;
        try {
            StringBuilder sb = new StringBuilder();
            PrinterLanguage printerLanguage = PrinterLanguage.CPCL;
            // 00:22:58:3D:7E:83 - RW420
            // AC:3F:A4:16:B9:AE - IMZ320

            if (printerLanguage == PrinterLanguage.CPCL) {
                int totalOrderedBatchCount = 0;
                int totalFrreProudctCount = 0;
                int totaltaxCount = 0;
                if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION
                        && bmodel.configurationMasterHelper.IS_INVOICE
                        && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
                    for (ProductMasterBO productBO : mOrderedProductList) {
                        if (productBO.getBatchwiseProductCount() > 0) {
                            ArrayList<ProductMasterBO> batchList = bmodel.batchAllocationHelper.getBatchlistByProductID().get(productBO.getProductID());
                            if (batchList != null) {
                                for (ProductMasterBO batchProductBO : batchList) {
                                    if (batchProductBO.getOrderedPcsQty() > 0
                                            || batchProductBO.getOrderedCaseQty() > 0
                                            || batchProductBO.getOrderedOuterQty() > 0) {
                                        totalOrderedBatchCount = totalOrderedBatchCount + 1;
                                    }
                                }
                            }
                        } else {
                            totalOrderedBatchCount = totalOrderedBatchCount + 1;
                        }
                    }
                } else {
                    totalOrderedBatchCount = mOrderedProductList.size();
                }

                for (ProductMasterBO productBO : mOrderedProductList) {
                    if (productBO.isPromo()) {
                        List<SchemeProductBO> schemeFreeList = productBO.getSchemeProducts();
                        if (schemeFreeList != null) {
                            totalFrreProudctCount = totalFrreProudctCount + schemeFreeList.size();
                        }
                    }
                }
                bmodel.productHelper.taxHelper.loadTaxDetailsForPrint(bmodel.invoiceNumber);
                bmodel.productHelper.taxHelper.loadTaxProductDetailsForPrint(bmodel.invoiceNumber);


                ArrayList<TaxBO> groupIdList = bmodel.productHelper.taxHelper.getGroupIdList();

                if (groupIdList != null) {
                    for (TaxBO taxBO : groupIdList) {
                        LinkedHashSet<TaxBO> percentagerList = bmodel.productHelper.taxHelper.getTaxBoByGroupId().get(taxBO.getGroupId());
                        if (percentagerList != null) {
                            totaltaxCount = totaltaxCount + (percentagerList.size());
                        }
                    }
                }

                int height;
                height = 460
                        + ((totalOrderedBatchCount + totalFrreProudctCount + totaltaxCount) * 50) + 650;

                sb.append("! 0 200 200 " + (height * (printCount + 1)) + " 1\r\n"
                        + "LEFT\r\n");
                boolean isOriginal = true;

                for (int j = 0; j <= printCount; j++) {
                    if (j > 0)
                        isOriginal = false;
                    int x = 100;
                    int totalLength = height * j;
                    sb.append("T 5 0 10 " + (5 + totalLength));
                    if (isFromInvoiceReport)
                        sb.append("                                " + "\r\n");
                    else {
                        if (isOriginal)
                            sb.append("Original Copy" + "\r\n");
                        else
                            sb.append("Duplicate Copy" + "\r\n");
                    }
                    sb.append("T 5 0 200 " + (20 + totalLength));
                    if (bmodel.getRetailerMasterBO().getTinnumber() != null && !"".equals(bmodel.getRetailerMasterBO().getTinnumber())) {
                        sb.append("Tax Invoice  \r\n");
                    } else {
                        sb.append("Sales/Retail Invoice  \r\n");
                    }

                    sb.append("T 5 0 200 " + (50 + totalLength));
                    sb.append("Invoice No:" + bmodel.invoiceNumber + "\r\n");


                    sb.append("T 5 0 200 " + (80 + totalLength));
                    sb.append("Date:" + DateTimeUtils.convertFromServerDateToRequestedFormat(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), ConfigurationMasterHelper.outDateFormat) + "\r\n");


                    sb.append("T 5 0 10 " + (130 + totalLength));
                    sb.append("From " + "\r\n");

                    if (bmodel.userMasterHelper.getUserMasterBO().getDistributorName() != null) {
                        sb.append("T 5 0 10 " + (160 + totalLength));
                        if (bmodel.userMasterHelper.getUserMasterBO().getDistributorName().length() > 15) {
                            sb.append(bmodel.userMasterHelper.getUserMasterBO().getDistributorName().substring(0, 15) + "\r\n");
                            sb.append("T 5 0 10 " + (190 + totalLength));
                            if (bmodel.userMasterHelper.getUserMasterBO().getDistributorName().substring(15).length() > 15)
                                sb.append(" " + bmodel.userMasterHelper.getUserMasterBO().getDistributorName().substring(15, 30) + "\r\n");
                            else
                                sb.append(" " + bmodel.userMasterHelper.getUserMasterBO().getDistributorName().substring(15) + "\r\n");
                        } else {
                            sb.append(" " + bmodel.userMasterHelper.getUserMasterBO().getDistributorName() + "\r\n");
                        }
                    }

                    if (bmodel.userMasterHelper.getUserMasterBO().getDistributorAddress3() != null) {
                        sb.append("T 5 0 10 " + (220 + totalLength));
                        if (bmodel.userMasterHelper.getUserMasterBO().getDistributorAddress3().length() > 15) {
                            sb.append(" " + bmodel.userMasterHelper.getUserMasterBO().getDistributorAddress3().substring(0, 15) + "\r\n");
                        } else {
                            sb.append(" " + bmodel.userMasterHelper.getUserMasterBO().getDistributorAddress3() + "\r\n");
                        }
                    }

                    sb.append("T 5 0 10 " + (250 + totalLength));
                    if (bmodel.userMasterHelper.getUserMasterBO().getDistributorTinNumber() != null) {
                        sb.append("TIN:-" + bmodel.userMasterHelper.getUserMasterBO().getDistributorTinNumber() + " \r\n");
                    } else {
                        sb.append("TIN:-  " + "\r\n");
                    }
                    sb.append("T 5 0 10 " + (280 + totalLength));
                    if (bmodel.userMasterHelper.getUserMasterBO().getCstNo() != null) {
                        sb.append("CST:-" + bmodel.userMasterHelper.getUserMasterBO().getCstNo() + "\r\n");
                    } else {
                        sb.append("CST:- " + "\r\n");
                    }

                    sb.append("T 5 0 300 " + (130 + totalLength));
                    sb.append("To" + "\r\n");
                    sb.append("T 5 0 300 " + (160 + totalLength));
                    if (bmodel.getRetailerMasterBO().getRetailerName().length() > 15) {
                        sb.append(" " + bmodel.getRetailerMasterBO().getRetailerName().substring(0, 15) + "\r\n");
                        sb.append("T 5 0 300 " + (190 + totalLength));
                        if (bmodel.getRetailerMasterBO().getRetailerName().substring(15).length() > 15)
                            sb.append(" " + bmodel.getRetailerMasterBO().getRetailerName().substring(15, 30) + "\r\n");
                        else
                            sb.append(" " + bmodel.getRetailerMasterBO().getRetailerName().substring(15) + "\r\n");
                    } else {
                        sb.append(" " + bmodel.getRetailerMasterBO().getRetailerName() + "\r\n");
                    }

                    sb.append("T 5 0 300 " + (220 + totalLength));

                    if (bmodel.getRetailerMasterBO().getAddress3() != null) {
                        if (bmodel.getRetailerMasterBO().getAddress3().length() > 20) {
                            sb.append(" " + bmodel.getRetailerMasterBO().getAddress3().substring(0, 20) + "\r\n");
                        } else {
                            sb.append(" " + bmodel.getRetailerMasterBO().getAddress3() + "\r\n");
                        }
                    }

                    sb.append("T 5 0 300 " + (250 + totalLength));
                    if (bmodel.getRetailerMasterBO().getTinnumber() != null) {
                        sb.append("TIN:-" + bmodel.getRetailerMasterBO().getTinnumber() + "\r\n");
                    } else {
                        sb.append("TIN:-" + "\r\n");
                    }

                    sb.append("T 5 0 300 " + (280 + totalLength));
                    if (bmodel.getRetailerMasterBO().getCredit_invoice_count() != null) {
                        sb.append("CST:-" + bmodel.getRetailerMasterBO().getCredit_invoice_count() + "\r\n");
                    } else {
                        sb.append("CST:-" + "\r\n");
                    }

                    sb.append("T 5 0 10 " + (330 + totalLength));
                    sb.append("Material" + "\r\n");
                    sb.append("T 5 0 190 " + (330 + totalLength));
                    sb.append("Qty" + "\r\n");
                    sb.append("T 5 0 280 " + (330 + totalLength));
                    sb.append("UCP" + "\r\n");
                    sb.append("T 5 0 420 " + (330 + totalLength));
                    sb.append("Payable" + "\r\n");
                    sb.append("T 5 0 420 " + (360 + totalLength));
                    sb.append("(incl.Tax)" + "\r\n");
                    x = x + 240 + totalLength;
                    double total = 0;
                    double totalExcludeTaxvalue = 0;
                    boolean isBatchwise = false;
                    double totalPriceOffValue = 0;
                    if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION
                            && bmodel.configurationMasterHelper.IS_INVOICE
                            && bmodel.configurationMasterHelper.IS_SIH_VALIDATION)
                        isBatchwise = true;
                    int totalQty = 0;
                    for (ProductMasterBO productBO : mOrderedProductList) {
                        if (isBatchwise && productBO.getBatchwiseProductCount() > 0) {
                            ArrayList<ProductMasterBO> batchList = bmodel.batchAllocationHelper.getBatchlistByProductID().get(productBO.getProductID());
                            if (batchList != null) {
                                for (ProductMasterBO batchProductBO : batchList) {

                                    int totalBatchQty = batchProductBO.getOrderedPcsQty() + batchProductBO.getOrderedCaseQty() * productBO.getCaseSize()
                                            + batchProductBO.getOrderedOuterQty() * productBO.getOutersize();

                                    if (totalBatchQty > 0) {
                                        x = x + 45;
                                        totalPriceOffValue = totalPriceOffValue + (totalBatchQty * batchProductBO.getPriceoffvalue());
                                        totalQty = totalQty + totalBatchQty;
                                        String productname;
                                        // For Printer Space issue , restriced to 10 character.
                                        if (productBO.getProductShortName() != null && !"".equals(productBO.getProductShortName())
                                                && !"null".equals(productBO.getProductShortName())) {
                                            if (productBO.getProductShortName().length() > 18)
                                                productname = productBO.getProductShortName().substring(
                                                        0, 18);
                                            else
                                                productname = productBO.getProductShortName();
                                        } else {
                                            if (productBO.getProductName().length() > 18)
                                                productname = productBO.getProductName().substring(
                                                        0, 18);
                                            else
                                                productname = productBO.getProductName();
                                        }
                                        sb.append("T 5 0 10 " + x + " ");
                                        sb.append(productname + "\r\n");

                                        sb.append("T 5 0 10 " + (x + 25) + " ");
                                        if ("NA".equalsIgnoreCase(batchProductBO.getBatchNo()) || "none".equalsIgnoreCase(batchProductBO.getBatchNo()))
                                            sb.append("" + "\r\n");
                                        else {
                                            sb.append(batchProductBO.getBatchNo() + "\r\n");
                                        }
                                        sb.append("T 5 0 190 " + x + " ");
                                        sb.append(totalBatchQty + "\r\n");
                                        sb.append("T 5 0 280 " + x + " ");
                                        sb.append(SDUtil.format((batchProductBO.getSrp() + batchProductBO.getPriceoffvalue()), 2, 0) + "\r\n");
//                                        sb.append("T 5 0 330 " + x + " ");
                                        totalExcludeTaxvalue = totalExcludeTaxvalue + (batchProductBO.getTaxableAmount() > 0 ? batchProductBO.getTaxableAmount() : batchProductBO.getNetValue());
//                                        sb.append(SDUtil.format(batchProductBO.getTaxableAmount() > 0 ? batchProductBO.getTaxableAmount() : batchProductBO.getNetValue(), 2, 0) + "\r\n");
                                        sb.append("T 5 0 420 " + x + " ");
                                        total = total + batchProductBO.getNetValue();
                                        sb.append(SDUtil.format(batchProductBO.getNetValue(), 2, 0) + "\r\n");
                                    }
                                }
                            }
                        } else {
                            x = x + 45;
                            int totalProductQty = productBO.getOrderedPcsQty() + productBO.getOrderedCaseQty() * productBO.getCaseSize()
                                    + productBO.getOrderedOuterQty() * productBO.getOutersize();
                            totalPriceOffValue = totalPriceOffValue + totalProductQty * productBO.getPriceoffvalue();

                            totalQty = totalQty + totalProductQty;
                            String productname;
                            // For Printer Space issue , restriced to 10 character.
                            if (productBO.getProductShortName() != null && !"".equals(productBO.getProductShortName())
                                    && !"null".equals(productBO.getProductShortName())) {
                                if (productBO.getProductShortName().length() > 18)
                                    productname = productBO.getProductShortName().substring(
                                            0, 18);
                                else
                                    productname = productBO.getProductShortName();
                            } else {
                                if (productBO.getProductName().length() > 18)
                                    productname = productBO.getProductName().substring(
                                            0, 18);
                                else
                                    productname = productBO.getProductName();
                            }
                            sb.append("T 5 0 10 " + x + " ");
                            sb.append(productname + "\r\n");
                            sb.append("T 5 0 190 " + x + " ");
                            sb.append(totalProductQty + "\r\n");
                            sb.append("T 5 0 280 " + x + " ");
                            sb.append(productBO.getSrp() + "\r\n");
                            totalExcludeTaxvalue = totalExcludeTaxvalue + (productBO.getTaxableAmount() > 0 ? productBO.getTaxableAmount() : productBO.getNetValue());
                            sb.append("T 5 0 420 " + x + " ");
                            total = total + productBO.getNetValue();
                            sb.append(SDUtil.format(productBO.getNetValue(), 2, 0) + "\r\n");
                        }
                    }
                    x = x + 70;
                    sb.append("T 5 0 10 " + x + " ");
                    sb.append("TOTAL" + "\r\n");

                    sb.append("T 5 0 190 " + x + " ");
                    sb.append(totalQty + "\r\n");
                    sb.append("T 5 0 420 " + x + " ");
                    sb.append(SDUtil.format(total, 2, 0) + "\r\n");
                    x = x + 50;

//                  print price off discount
                    sb.append("T 5 0 10 " + x + " ");
                    sb.append("Scheme Discount" + "\r\n");
                    sb.append("T 5 0 450 " + x + " ");
                    sb.append(SDUtil.format(totalPriceOffValue, 2, 0) + "\r\n");

                    // Print Item Level Discount
                    ArrayList<Integer> mTypeIdList = bmodel.productHelper.getTypeIdList();
                    SparseArray<ArrayList<Integer>> mDiscountIdListByTypeId = bmodel.productHelper.getDiscountIdListByTypeId();
                    if (mTypeIdList != null && mDiscountIdListByTypeId != null) {
                        x = x + 20;
                        for (Integer typeId : mTypeIdList) {
                            ArrayList<Integer> discountIdList = mDiscountIdListByTypeId.get(typeId);
                            if (discountIdList != null) {
                                String discountDescription = "";
                                double totalDiscountValue = 0;
                                for (int discountid : discountIdList) {
                                    ArrayList<StoreWiseDiscountBO> discountList = bmodel.productHelper.getProductDiscountListByDiscountID().get(discountid);
                                    if (discountList != null) {
                                        for (StoreWiseDiscountBO storeWiseDiscountBO : discountList) {
                                            discountDescription = storeWiseDiscountBO.getDescription();
                                            ProductMasterBO productMasterBO = bmodel.productHelper.getProductMasterBOById(storeWiseDiscountBO.getProductId() + "");
                                            if (productMasterBO != null) {
                                                int totalProductQty;
                                                totalProductQty = productMasterBO.getOrderedPcsQty()
                                                        + productMasterBO.getOrderedCaseQty() * productMasterBO.getCaseSize()
                                                        + productMasterBO.getOrderedOuterQty() * productMasterBO.getOutersize();
                                                if (totalProductQty > 0) {
                                                    if (productMasterBO.getBatchwiseProductCount() > 0) {

                                                        ArrayList<ProductMasterBO> batchList = bmodel.batchAllocationHelper.getBatchlistByProductID().get(productMasterBO.getProductID());
                                                        if (batchList != null) {
                                                            for (ProductMasterBO batchProductBO : batchList) {
                                                                double totalValue;
                                                                double batchDiscountValue = 0;
                                                                int totalBatchQty = batchProductBO.getOrderedPcsQty() + batchProductBO.getOrderedCaseQty() * productMasterBO.getCaseSize()
                                                                        + batchProductBO.getOrderedOuterQty() * productMasterBO.getOutersize();

                                                                if (batchProductBO.getLineValueAfterSchemeApplied() > 0) {
                                                                    totalValue = batchProductBO.getLineValueAfterSchemeApplied();
                                                                } else {
                                                                    totalValue = batchProductBO.getOrderedPcsQty()
                                                                            * batchProductBO.getSrp()
                                                                            + batchProductBO.getOrderedCaseQty()
                                                                            * batchProductBO.getCsrp()
                                                                            + batchProductBO.getOrderedOuterQty()
                                                                            * batchProductBO.getOsrp();
                                                                }
                                                                if (storeWiseDiscountBO.getIsPercentage() == 1) {
                                                                    batchDiscountValue = totalValue * storeWiseDiscountBO.getDiscount() / 100;
                                                                } else if (storeWiseDiscountBO.getIsPercentage() == 0) {
                                                                    batchDiscountValue = totalBatchQty * storeWiseDiscountBO.getDiscount();
                                                                }
                                                                totalDiscountValue = totalDiscountValue + batchDiscountValue;
                                                            }
                                                        }
                                                    } else {
                                                        double totalValue;
                                                        double productDiscount = 0;

                                                        if (productMasterBO.getLineValueAfterSchemeApplied() > 0) {
                                                            totalValue = productMasterBO.getLineValueAfterSchemeApplied();
                                                        } else {
                                                            totalValue = productMasterBO.getOrderedPcsQty() * productMasterBO.getSrp()
                                                                    + productMasterBO.getOrderedCaseQty() * productMasterBO.getCsrp()
                                                                    + productMasterBO.getOrderedOuterQty() * productMasterBO.getOsrp();
                                                        }

                                                        if (storeWiseDiscountBO.getIsPercentage() == 1) {
                                                            productDiscount = totalValue * storeWiseDiscountBO.getDiscount() / 100;


                                                        } else if (storeWiseDiscountBO.getIsPercentage() == 0) {
                                                            productDiscount = totalProductQty * storeWiseDiscountBO.getDiscount();
                                                        }

                                                        totalDiscountValue = totalDiscountValue + productDiscount;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                if (totalDiscountValue > 0) {
                                    x = x + 40;
                                    sb.append("T 5 0 10 " + x + " ");
                                    if (discountDescription.length() < 10) {
                                        sb.append(discountDescription + "\r\n");
                                    } else {
                                        sb.append(discountDescription.substring(0, 10) + "\r\n");
                                    }
                                    sb.append("T 5 0 450 " + x + " ");
                                    sb.append(SDUtil.format(totalDiscountValue, 2, 0) + "\r\n");
                                }
                            }
                        }
                    }

                    //print cash  discount value
                    x = x + 40;
                    sb.append("T 5 0 10 " + x + " ");
                    sb.append("CashDiscount" + "\r\n");
                    sb.append("T 5 0 450 " + x + " ");
                    sb.append(SDUtil.format(entryLevelDis, 2, 0) + "\r\n");

                    //print tax
                    x = x + 100;

                    HashMap<String, HashSet<String>> productListByGroupId = bmodel.productHelper.taxHelper.getProductIdByTaxGroupId();
                    SparseArray<LinkedHashSet<TaxBO>> totalTaxListByGroupId = bmodel.productHelper.taxHelper.getTaxBoByGroupId();

                    if (groupIdList != null) {
                        String taxDesc;
                        String previousTaxDesc = "";
                        for (TaxBO taxBO : groupIdList) {

                            LinkedHashSet<TaxBO> totalTaxList = totalTaxListByGroupId.get(taxBO.getGroupId());
                            if (totalTaxList != null) {
                                for (TaxBO totalTaxBO : totalTaxList) {
                                    taxDesc = totalTaxBO.getTaxDesc2();

                                    double taxpercentege = totalTaxBO.getTaxRate();
                                    HashSet<String> taxProductList = productListByGroupId.get(taxBO.getGroupId() + "" + taxpercentege);
                                    double totalTax = 0.0;
                                    double totalExcludeValue = 0.0;
                                    if (taxProductList != null) {
                                        for (String productid : taxProductList) {
                                            ProductMasterBO prodcutBO = bmodel.productHelper.getProductMasterBOById(productid);
                                            if (prodcutBO != null) {
                                                totalExcludeValue = totalExcludeValue + prodcutBO.getTaxableAmount();
                                                totalTax = totalTax + (prodcutBO.getTaxableAmount() * taxpercentege) / 100;
                                            }
                                        }
                                        if (totalTax > 0) {
                                            if (!taxDesc.equals(previousTaxDesc)) {
                                                sb.append("T 5 0 10 " + x + " ");
                                                if (taxDesc.length() > 10) {
                                                    sb.append(taxDesc.substring(0, 10) + "\r\n");
                                                } else {
                                                    sb.append(taxDesc + "\r\n");
                                                }
                                            }
                                            sb.append("T 5 0 200 " + x + " ");
                                            sb.append(taxpercentege + "% on Rs " + SDUtil.format(totalExcludeValue, 2, 0) + "\r\n");
                                            sb.append("T 5 0 450 " + x + " ");
                                            sb.append(SDUtil.format(totalTax, 2, 0) + "\r\n");
                                            x = x + 50;
                                        }
                                    }
                                    previousTaxDesc = taxDesc;
                                }
                            }
                        }
                    }
                    x = x + 40;
                    sb.append("T 5 0 10 " + x + " ");
                    sb.append("Net Payable" + "\r\n");

                    sb.append("T 5 0 450 " + x + " ");
                    String formatTotal = SDUtil.format(Math.round(total), 2, 0);
                    sb.append(formatTotal + "\r\n");

                    if (formatTotal.length() <= 12) {
                        String[] splits = formatTotal.split(Pattern.quote("."));
                        StringBuffer convertBuffer = new StringBuffer();
                        NumberToWord numberToWord = new NumberToWord();
                        for (int i = 0; i < splits.length; i++) {
                            long splitvalue = SDUtil.convertToLong(splits[i]);
                            if (i == 1 && splitvalue > 0) {
                                convertBuffer.append(" and ");
                            }
                            convertBuffer.append(numberToWord.convertNumberToWords(SDUtil.convertToLong(splits[i])));
                            if (i == 0) {
                                convertBuffer.append(" Rupees ");
                            } else if (i == 1) {
                                if (!"00".equals(splits[i]))
                                    convertBuffer.append(" Paise");
                            }
                        }
                        sb.append("T 5 0 10 " + (x + 30) + " ");
                        sb.append("In Words" + "\r\n");

                        if (convertBuffer.length() < 40) {
                            sb.append("T 7 0 10 " + (x + 70) + " ");
                            sb.append(convertBuffer.toString() + "\r\n");
                        } else {
                            sb.append("T 7 0 10 " + (x + 70) + " ");
                            sb.append(convertBuffer.substring(0, 40) + "\r\n");

                            try {
                                int startat = 100;
                                String str = convertBuffer.substring(40, convertBuffer.length());
                                while (str.length() > 0) {
                                    if (str.length() > 40) {
                                        sb.append("T 7 0 25 " + (x + startat) + " ");
                                        sb.append(str.substring(0, 40) + "\r\n");
                                        startat = startat + 30;
                                        str = str.substring(40, str.length());
                                    } else {
                                        sb.append("T 7 0 25 " + (x + startat) + " ");
                                        sb.append(str + "\r\n");
                                        str = "";
                                    }
                                }
                            } catch (Exception e) {
                                Commons.printException(e + "");
                            }
                        }
                    }

                    sb.append("T 5 0 10 " + (x + 190) + " ");
                    sb.append("For (RS Name) " + "\r\n");

                    sb.append("T 5 0 10 " + (x + 270) + " ");
                    sb.append("Received " + "\r\n");

                    sb.append("T 5 0 10 " + (x + 300) + " ");
                    sb.append("------------------------------------------------------------ " + "\r\n");
                }
                sb.append("PRINT \r\n");
                printDataBytes = sb.toString().getBytes();
            }
        } catch (Exception e) {
            Commons.printException(e + "");
        }
        return printDataBytes;
    }

    /////////////////////////////////////////////////////////////////////////////////
    private int pcount;

    public void setPrintCnt(int count) {
        this.pcount = count;
    }

    public int getPrintCnt() {
        return pcount;
    }

    public byte[] printDatafor3inchprinterForUnipal(List<ProductMasterBO> mOrderedProductList, boolean fromorder, int printCount) {
        byte[] printDataBytes = null;
        try {
            StringBuilder sb = new StringBuilder();
            PrinterLanguage printerLanguage = PrinterLanguage.CPCL;
            // 00:22:58:3D:7E:83 - RW420
            // AC:3F:A4:16:B9:AE - IMZ320

            if (printerLanguage == PrinterLanguage.
                    CPCL) {

                int totalFrreProudctCount = 0;
                for (ProductMasterBO productBO : mOrderedProductList) {
                    if (productBO.isPromo() ) {
                        List<SchemeProductBO> schemeFreeList = productBO.getSchemeProducts();
                        if (schemeFreeList != null) {
                            totalFrreProudctCount = totalFrreProudctCount + schemeFreeList.size();
                        }
                    }
                }

                int height;
                int x = 250;
                height = x + 100 + 200
                        + ((mOrderedProductList.size() + totalFrreProudctCount) * 35);
                height = height * printCount;

                sb.append("! 0 200 200 " + height + " 1\r\n"
                        + "LEFT\r\n");

                int hght = 0;
                for (int i = 0; i < printCount; i++) {
                    hght = hght + 10;
                    int heightspace = 30;
                    sb.append("T 7 0 105 " + hght);
                    sb.append("Unipal General Trading Company\n\r\n");
                    hght = hght + heightspace;
                    sb.append("T 7 0 170 " + hght);
                    sb.append("VAT No  : \r\n");

                    sb.append("T 7 0 280 " + hght + " ");
                    sb.append(" 562414227 \r\n");
//
                    hght = hght + heightspace;
                    sb.append("T 7 0 10 " + hght);
                    sb.append("Ramallah - Industrial zone, Tel: +972 2 2981060 \r\n");
                    hght = hght + heightspace;
                    sb.append("T 7 0 10 " + hght);
                    sb.append("Gaza - lndus. Zone - Carny, Tel: +972 7 2830324\n \r\n");

                    if (!fromorder) {
                        hght = hght + heightspace;
                        sb.append("T 7 0 170  " + hght);
                        sb.append("Tax Invoice -  \r\n");

                        sb.append("T 7 0 330 " + hght);

                        if (pcount == 0 && i == 0) {

                            sb.append("Original \r\n");
                        } else {

                            sb.append("Duplicate \r\n");
                        }
                    }
                    hght = hght + heightspace;
                    if (!fromorder) {
                        sb.append("T 7 0 10 " + hght + " ");
                        sb.append("Invoice Date: \r\n");

                    } else if (fromorder) {
                        sb.append("T 7 0 10 " + hght + " ");
                        sb.append("Order Date: \r\n");
                    }
                    String date = bmodel.getInvoiceDate();
                    sb.append("T 7 0 190 " + hght + " ");
                    if (bmodel.getInvoiceDate() != null) {
                        sb.append("" + date + "\r\n");
                    } else {
                        sb.append("" + date + " \r\n");
                    }
                    hght = hght + heightspace;
                    if (!fromorder) {
                        sb.append("T 7 0 10 " + hght);
                        sb.append("Invoice No:\r\n");
                        sb.append("T 7 0 190 " + hght + " ");

                        if (bmodel.invoiceNumber != null) {

                            sb.append(bmodel.invoiceNumber + "\r\n");
                        } else {
                            sb.append("\r\n");
                        }
                    } else if (fromorder) {
                        sb.append("T 7 0 10 " + hght);
                        sb.append("Order No:\r\n");
                        sb.append("T 7 0 190 " + hght + " ");

                        if (orderHelper.getOrderId() != null) {
                            sb.append(orderHelper.getOrderId() + "\r\n");
                        } else {
                            sb.append("\r\n");
                        }
                    }

                    hght = hght + heightspace;
                    sb.append("T 7 0 10 " + hght + " ");
                    sb.append("Customer Code:\r\n");

                    String code = ((bmodel.retailerMasterBO.getRetailerCode() == null) ? "" : bmodel.retailerMasterBO.getRetailerCode());
                    System.out.println("code====" + code);
                    sb.append("T 7 0 190 " + hght + " ");
                    sb.append(code + "\r\n");

                    hght = hght + heightspace;
                    sb.append("T 7 0 10 " + hght);
                    sb.append("Customer Name:\r\n");

                    String retailername = "";
                    if (bmodel.getRetailerMasterBO().getRetailerName().length() > 30) {
                        retailername = bmodel.getRetailerMasterBO().getRetailerName().substring(0, 30);
                    } else {
                        retailername = bmodel.getRetailerMasterBO().getRetailerName();
                    }

                    int h = hght;
                    sb.append("T 7 0 190 " + hght + " ");
                    sb.append(retailername + "\r\n");
                   /* String v = ((bmodel.retailerMasterBO.getRetailerName() == null) ? "" : bmodel.retailerMasterBO.getRetailerName());
                    if (v.length() > 30) {
                        sb.append("T 7 0 195 " + h);
                        sb.append(v.substring(0, 30) + "\r\n");
                        if ((v.substring(31).length()) > 60) {
                            h = h + 35;
                            sb.append("T 7 0 200 " + h);
                            sb.append(v.substring(31, 90) + "\r\n");
                            if ((v.substring(91).length()) > 120) {
                                h = h + 35;
                                sb.append("T 7 0 200 " + h);
                                sb.append(v.substring(91, 120) + "\r\n");
                            } else {
                                h = h + 35;
                                sb.append("T 7 0 200 " + h);
                                sb.append(v.substring(91) + "\r\n");
                            }
                        } else {
                            h = h + 35;
                            sb.append("T 7 0 200 " + h);
                            sb.append(v.substring(30) + "\r\n");
                        }
                    } else {
                        sb.append("T 7 0 200 " + h);
                        sb.append(v + "\r\n");
                    }*/
                    hght = h + heightspace;

                    h = hght;
                    sb.append("T 7 0 10  " + h);
                    sb.append("-------------------------------------------------------------------------------------------------------\r\n");
                    h = h + 30;
                    sb.append("T 7 0 10 " + h);
                    sb.append("# \r\n");

                    sb.append("T 7 0 120 " + h);
                    sb.append("Item Name  \r\n");
                    sb.append("T 7 0 320 " + h);
                    sb.append("Qty \r\n");
                    sb.append("T 7 0 380 " + h);
                    sb.append("Price   \r\n");
                    sb.append("T 7 0 470 " + h);
                    sb.append("Amount \r\n");

                    x = h;
                    int sno = 1;
                    double total = 0;
                    for (ProductMasterBO productBO : mOrderedProductList) {

                        x = x + 35;
                        int totalQty = productBO.getOrderedPcsQty() + productBO.getOrderedCaseQty() * productBO.getCaseSize()
                                + productBO.getOrderedOuterQty() * productBO.getOutersize();

                        total = total + productBO.getNetValue();

                        String productname;
                        // For Printer Space issue , restriced to 10 character.
                        if (productBO.getProductShortName() != null && !"".equals(productBO.getProductShortName())
                                && !"null".equals(productBO.getProductShortName())) {
                            if (productBO.getProductShortName().length() > 20)
                                productname = productBO.getProductShortName().substring(
                                        0, 20);
                            else
                                productname = productBO.getProductShortName();
                        } else {
                            if (productBO.getProductName().length() > 20)
                                productname = productBO.getProductName().substring(
                                        0, 20);
                            else
                                productname = productBO.getProductName();
                        }

                        float productPrice;
                        double amount;

                        productPrice = productBO.getSrp();

                        amount = productBO.getNetValue();
                        sb.append("T 7 0 10 " + x + " ");
                        sb.append(sno + "\r\n");

                        sb.append("T 7 0 40 " + x + " ");
                        sb.append(productname.toLowerCase() + "\r\n");
                        sb.append("T 7 0 320 " + x + " ");
                        sb.append(totalQty + "\r\n");

                        sb.append("T 7 0 390 " + x + " ");
                        sb.append(productPrice + "\r\n");
                        sb.append("T 7 0 470 " + x + " ");
                        sb.append(bmodel.formatValue(amount) + "\r\n");

                        if (productBO.isPromo() ) {
                            if (productBO.getSchemeProducts() != null && productBO.getSchemeProducts().size() > 0) {

                                for (SchemeProductBO schemeProductBo : productBO.getSchemeProducts()) {

                                    ProductMasterBO freeproductBo = bmodel.productHelper.getProductMasterBOById(schemeProductBo.getProductId());
                                    if (freeproductBo != null) {
                                        int freeQty;
                                        if (freeproductBo.getCaseUomId() == schemeProductBo.getUomID()
                                                && freeproductBo.getCaseUomId() != 0) {
                                            freeQty = schemeProductBo.getQuantitySelected() * freeproductBo.getCaseSize();
                                        } else if (freeproductBo.getOuUomid() == schemeProductBo.getUomID()
                                                && freeproductBo.getOuUomid() != 0) {
                                            freeQty = schemeProductBo.getQuantitySelected() * freeproductBo.getOutersize();
                                        } else {
                                            freeQty = schemeProductBo.getQuantitySelected();
                                        }

                                        String freeProductname;
                                        if (freeproductBo.getProductShortName() != null && !"".equals(freeproductBo.getProductShortName())
                                                && !"null".equals(freeproductBo.getProductShortName())) {
                                            if (freeproductBo.getProductShortName().length() > 20)
                                                freeProductname = freeproductBo.getProductShortName().substring(
                                                        0, 20);
                                            else
                                                freeProductname = freeproductBo.getProductShortName();
                                        } else {
                                            if (freeproductBo.getProductName().length() > 20)
                                                freeProductname = freeproductBo.getProductName().substring(
                                                        0, 20);
                                            else
                                                freeProductname = productBO.getProductName();
                                        }

                                        x = x + 35;
                                        sb.append("T 7 0 40 " + x + " ");
                                        sb.append(freeProductname.toLowerCase() + "\r\n");
                                        sb.append("T 7 0 320 " + x + " ");
                                        sb.append(freeQty + "\r\n");
                                    }
                                }
                            }
                        }
                        sno++;
                    }

                    x = x + 30;
                    sb.append("T 7 0 10 " + x + " ---------------------------------------------------------------------------------------\r\n");
                    x = x + 30;
                    sb.append("T 7 0 280 " + (x) + " ");
                    sb.append(" Discount  \r\n");

                    double discount = bmodel.productHelper.getTotalBillwiseDiscount();
                    sb.append("T 7 0 470 " + (x) + " ");
                    sb.append(bmodel.formatValue(discount) + " \r\n");
                    x = x + 30;
                    sb.append("T 7 0 280 " + (x) + " ");
                    sb.append(" VAT  \r\n");

                    sb.append("T 7 0 470 " + (x) + " ");

                    double taxAmount;

                        taxAmount = bmodel.productHelper.taxHelper.getTotalBillTaxAmount(fromorder);

                    sb.append(bmodel.formatValue(taxAmount) + " \r\n");

                    x = x + 30;
                    sb.append("T 7 0 10 " + (x) + " ");
                    sb.append("signature:  \r\n");
                    sb.append("T 7 0 280 " + (x) + " ");
                    sb.append(" Total  \r\n");

                    sb.append("T 7 0 470 " + (x) + " ");

                    sb.append(bmodel.formatBasedOnCurrency((total - SDUtil.convertToDouble(bmodel.formatValue(discount)))) + "\r\n");
                    x = x + 70;
                    hght = x;
                }

                sb.append("PRINT \r\n");
                printDataBytes = (sb.toString().getBytes());
            }
        } catch (Exception e) {
            Commons.printException(e + "");
        }
        return printDataBytes;
    }

    public double getEntryLevelDiscountValue(ArrayList<ProductMasterBO> orderList) {
        double discountTotalValue = 0.0;
        if (orderList != null) {
            for (ProductMasterBO productBO : orderList) {
                if (productBO.getBatchwiseProductCount() > 0) {
                    ArrayList<ProductMasterBO> batchList = bmodel.batchAllocationHelper.getBatchlistByProductID().get(productBO.getProductID());
                    if (batchList != null) {
                        for (ProductMasterBO batchBO : batchList) {
                            int totalQty = batchBO.getOrderedPcsQty() + (batchBO.getOrderedCaseQty() * productBO.getCaseSize())
                                    + (batchBO.getOrderedOuterQty() * productBO.getOutersize());
                            double totalValue = batchBO.getOrderedPcsQty() * batchBO.getSrp()
                                    + batchBO.getOrderedCaseQty() * batchBO.getCsrp()
                                    + batchBO.getOrderedOuterQty() * batchBO.getOsrp();

                            double percentage = batchBO.getD1() + batchBO.getD2() + batchBO.getD3();
                            double discountValue = 0;
                            if (percentage > 0) {
                                discountValue = totalValue * percentage / 100;

                            } else if (batchBO.getDA() > 0) {
                                discountValue = totalQty * batchBO.getDA();
                            }
                            discountTotalValue = discountTotalValue + discountValue;
                        }
                    }
                } else {
                    int totalQty = productBO.getOrderedPcsQty() + (productBO.getOrderedCaseQty() * productBO.getCaseSize())
                            + (productBO.getOrderedOuterQty() * productBO.getOutersize());
                    double totalValue = productBO.getOrderedPcsQty() * productBO.getSrp()
                            + productBO.getOrderedCaseQty() * productBO.getCsrp()
                            + productBO.getOrderedOuterQty() * productBO.getOsrp();

                    double percentage = productBO.getD1() + productBO.getD2() + productBO.getD3();
                    double discountValue = 0;
                    if (percentage > 0) {
                        discountValue = totalValue * percentage / 100;

                    } else if (productBO.getDA() > 0) {
                        discountValue = totalQty * productBO.getDA();
                    }
                    discountTotalValue = discountTotalValue + discountValue;
                }
            }
        }
        return discountTotalValue;
    }


    /**
     * set values in product list
     **/
    private void updateproducts() {
        Vector<ProductMasterBO> mProducts = bmodel.productHelper.getProductMaster();

        try {
            mProductsForAdapter = new ArrayList<>();
            Collections.sort(mProducts, ProductMasterBO.SKUWiseAscending);
            for (ProductMasterBO productBO : mProducts) {
                if ((productBO.getOrderedPcsQty() > 0
                        || productBO.getOrderedCaseQty() > 0 || productBO
                        .getOrderedOuterQty() > 0)) {
                    mProductsForAdapter.add(productBO);

                    if (productBO.getOrderedCaseQty() > 0) {
                        mCaseTotalValue = mCaseTotalValue
                                + (productBO.getOrderedCaseQty() * productBO.getCsrp());
                    }
                    if (productBO.getOrderedPcsQty() > 0) {
                        mPcTotalValue = mPcTotalValue
                                + (productBO.getOrderedPcsQty() * productBO.getSrp());
                    }
                    mTotalOrderValue = mTotalOrderValue
                            + productBO.getNetValue();
                }
            }

        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }

    /**
     * set values in product list
     **/
    private void updateEmptiesproducts() {
        ArrayList<BomReturnBO> mEmptyProducts;
        if (bmodel.configurationMasterHelper.SHOW_GROUPPRODUCTRETURN)
            mEmptyProducts = bmodel.productHelper
                    .getBomReturnTypeProducts();
        else
            mEmptyProducts = bmodel.productHelper.getBomReturnProducts();

        try {
            double mLiableTot = 0, mRetTot = 0;
            if (mEmptyProducts != null) {
                Collections.sort(mEmptyProducts, BomReturnBO.SKUWiseAscending);
                for (BomReturnBO productBO : mEmptyProducts) {
                    if ((productBO.getLiableQty() > 0)) {
                        mEmptyLiaProductsForAdapter.add(productBO);

                        mLiableTot = mLiableTot
                                + (productBO.getLiableQty() * productBO
                                .getpSrp());
                    }
                }
            }

            if (mEmptyProducts != null) {
                for (BomReturnBO productBO2 : mEmptyProducts) {
                    if ((productBO2.getReturnQty() > 0)) {
                        mEmptyRetProductsForAdapter.add(productBO2);
                        mRetTot = mRetTot
                                + (productBO2.getReturnQty() * productBO2.getpSrp());
                    }
                }
            }

            mEmpTotalValue = SDUtil.convertToDouble(bmodel
                    .formatValue(mLiableTot - mRetTot));
        } catch (Exception e) {
            Commons.printException(e + "");
        }

    }

    /**
     * set values in product list
     **/
    private void updateTaxDetails() {
        ArrayList<Float> taxRateList = new ArrayList<>();
        ArrayList<String> taxNameList = new ArrayList<>();
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.openDataBase();

            String sql = "Select TM.taxrate,SLM.ListName from TaxMaster TM INNER JOIN  standardlistmaster  SLM on TM.TaxType = SLM.ListID where applylevelid in (select listid from standardlistmaster where  listcode='BILL') limit 2";
            Cursor c = db.selectSQL(sql);

            if (c != null) {
                while (c.moveToNext()) {
                    taxRateList.add(c.getFloat(0));
                    taxNameList.add(c.getString(1));
                }
                c.close();
            }
            db.closeDB();
            int taxsize = taxRateList.size();
            if (taxsize > 0) {
                if (taxsize == 2) {
                    vatPercentage = taxRateList.get(0);
                    nhlPercentage = taxRateList.get(1);
                } else if (taxsize == 1) {
                    vatPercentage = taxRateList.get(0);
                    nhlPercentage = 0;
                }
            }
            int taxNamesize = taxNameList.size();
            if (taxNamesize > 0) {
                if (taxNamesize == 2) {
                    mVatName = taxNameList.get(0);
                    mNhilName = taxNameList.get(1);
                } else if (taxNamesize == 1) {
                    mVatName = taxNameList.get(0);
                    mNhilName = "";
                }
            }
        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }

    public byte[] printDatafor3inchPrinterDiageoNG(String deleveryDate) {
        byte[] PrintDataBytes = null;
        try {
            updateproducts();
            updateEmptiesproducts();
            updateTaxDetails();
            calculateTaxDetails();

            double mTotalColValue = mCaseTotalValue + mPcTotalValue + mEmpTotalValue;

            String mInvoiceno = bmodel.invoiceNumber;
            String mCustomername = bmodel.getRetailerMasterBO().getRetailerName();
            String mSalesdate = DateTimeUtils.convertFromServerDateToRequestedFormat(
                    DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                    ConfigurationMasterHelper.outDateFormat);

            PrinterLanguage printerLanguage = PrinterLanguage.CPCL;
            // 00:22:58:3D:7E:83 - RW420
            // AC:3F:A4:16:B9:AE - IMZ320
            if (printerLanguage == PrinterLanguage.CPCL) {
                File file = new File(
                        mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                                + "/"
                                + bmodel.userMasterHelper.getUserMasterBO()
                                .getUserid()
                                + DataMembers.APP_DIGITAL_CONTENT + "/"
                                + "receiptImg.png");

                m_bmp = BitmapFactory.decodeFile(file.getAbsolutePath());

                int height;
                int x = 340;
                int schemeSize = 0;
                // update free product size
                if (SchemeDetailsMasterHelper.getInstance(mContext).IS_SCHEME_ON) {
                    for (ProductMasterBO product : mProductsForAdapter) {
                        if (product.isPromo()) {
                            if (product.getSchemeProducts() != null) {
                                schemeSize = schemeSize
                                        + product.getSchemeProducts().size();
                            }
                        }
                    }
                }
                height = x
                        + (mProductsForAdapter.size() + schemeSize
                        + mEmptyLiaProductsForAdapter.size() + mEmptyRetProductsForAdapter
                        .size()) * 50 + 750;
                if (bmodel.configurationMasterHelper.TAX_SHOW_INVOICE) {
                    height = height + 200;
                }

                String Printitem = "! 0 200 200 " + height + " 1\r\n"
                        + "CENTER\r\n";
                Printitem += ExtractGraphicsDataForCPCL();

                Printitem += "T 5 1 10 140 "
                        + "PROFORMA INVOICE"
                        + "\r\n";

                Printitem += "T 5 1 10 190 "
                        + ""
                        + bmodel.userMasterHelper.getUserMasterBO()
                        .getDistributorName() + "\r\n";
                if (bmodel.userMasterHelper.getUserMasterBO().getDistributorTinNumber() != null) {

                    Printitem += "T 5 0 10 230 " + "" + bmodel.userMasterHelper.getUserMasterBO().getDistributorTinNumber() + "\r\n";
                }

                // T- Text
                //
                // Font Size
                // Spacing
                // height between lines
                Printitem += "T 5 0 10 260 --------------------------------------------------\r\n";
                Printitem += "\r\n";
                Printitem += "LEFT \r\n";
                Printitem += "T 5 0 10 320 "
                        + mContext.getResources().getString(R.string.order_number) + ":"
                        + mInvoiceno + "\r\n";

                Printitem += "T 5 0 320 290 "
                        + mContext.getResources().getString(R.string.sales_date) + ":"
                        + mSalesdate + "\r\n";

                Printitem += "LEFT \r\n";
                Printitem += "T 5 0 10 350 "
                        + mContext.getResources().getString(R.string.customer) + ""
                        + mCustomername + "\r\n";

                Printitem += "\r\n";
                Printitem += "LEFT \r\n";

                Printitem += "T 5 0 10 380 "
                        + mContext.getResources().getString(R.string.Address) + ":"
                        + bmodel.getRetailerMasterBO().getAddress1() + "\r\n";
                Printitem += "\r\n";
                Printitem += "T 5 0 10 410 "
                        + mContext.getResources().getString(R.string.tel) + ":"
                        + bmodel.getRetailerMasterBO().getContactnumber() + "\r\n";

                Printitem += "T 5 0 10 450 --------------------------------------------------\r\n";

                Printitem += "\r\n";
                Printitem += "LEFT \r\n";

                Printitem += "T 5 0 10 475 "
                        + mContext.getResources().getString(R.string.brand) + "\r\n";
                Printitem += "T 5 0 220 475 "
                        + mContext.getResources().getString(R.string.case_u) + "\r\n";
                Printitem += "T 5 0 280 475 "
                        + mContext.getResources().getString(R.string.bottle) + "\r\n";

                Printitem += "T 5 0 360 475 "
                        + mContext.getResources().getString(R.string.unit_price)
                        + "\r\n";
                Printitem += "T 5 0 450 475 "
                        + mContext.getResources().getString(R.string.Amount) + "\r\n";

                Printitem += "T 5 0 10 500 --------------------------------------------------\r\n";
                x += 190;
                for (ProductMasterBO productBO : mProductsForAdapter) {

                    x += 20;
                    String productname;
                    // For Printer Space issue , restriced to 10 character.
                    if (productBO.getProductShortName() != null && !productBO.getProductShortName().equals("")
                            && !"null".equals(productBO.getProductShortName())) {
                        if (productBO.getProductShortName().length() > 18)
                            productname = productBO.getProductShortName().substring(
                                    0, 18);
                        else
                            productname = productBO.getProductShortName();
                    } else {
                        if (productBO.getProductName().length() > 18)
                            productname = productBO.getProductName().substring(
                                    0, 18);
                        else
                            productname = productBO.getProductName();
                    }

                    Printitem += "T 5 0 10 " + x + " "
                            + productname.toLowerCase() + "\r\n";
                    Printitem += "\r\n";

                    Printitem += "T 5 0 240 " + x + " "
                            + productBO.getOrderedCaseQty() + "\r\n";

                    Printitem += "T 5 0 290 " + x + " "
                            + productBO.getOrderedPcsQty() + "\r\n";

                    Printitem += "T 5 0 360 " + x + " "
                            + bmodel.formatValue(productBO.getSrp()) + "\r\n";

                    double totalProdVal = (productBO.getOrderedOuterQty() * productBO
                            .getOsrp())
                            + (productBO.getOrderedCaseQty() * productBO
                            .getCsrp())
                            + (productBO.getOrderedPcsQty() * productBO
                            .getSrp());

                    Printitem += "RIGHT \r\n";

                    Printitem += "T 5 0 450 " + x + " "
                            + bmodel.formatValue(totalProdVal) + "\r\n";
                    x += 10;
                    // print scheme free product starts
                    if (productBO.isPromo() ) {
                        if (productBO.getSchemeProducts() != null) {

                            List<SchemeProductBO> freeProductList = productBO
                                    .getSchemeProducts();
                            if (freeProductList != null) {
                                for (SchemeProductBO schemeProductBO : freeProductList) {
                                    ProductMasterBO product = bmodel.productHelper
                                            .getProductMasterBOById(schemeProductBO
                                                    .getProductId());

                                    if (product != null) {
                                        x += 20;
                                        String pname;
                                        // For Printer Space issue , restriced to 10 character.
                                        if (product.getProductShortName() != null && !"".equals(product.getProductShortName())
                                                && !"null".equals(product.getProductShortName())) {
                                            if (product.getProductShortName().length() > 18)
                                                pname = product.getProductShortName().substring(
                                                        0, 18);
                                            else
                                                pname = product.getProductShortName();
                                        } else {
                                            if (product.getProductName().length() > 18)
                                                pname = product.getProductName().substring(
                                                        0, 18);
                                            else
                                                pname = product.getProductName();
                                        }
                                        Printitem += "T 5 0 10 " + x + " "
                                                + pname.toLowerCase() + "\r\n";
                                        Printitem += "\r\n";
                                        if (product.getCaseUomId() == schemeProductBO
                                                .getUomID()
                                                && product.getCaseUomId() != 0) {

                                            Printitem += "T 5 0 240 "
                                                    + x
                                                    + " "
                                                    + schemeProductBO
                                                    .getQuantitySelected()
                                                    + "\r\n";

                                            Printitem += "T 5 0 290 " + x + " "
                                                    + 0 + "\r\n";
                                            // case wise free quantity update
                                        } else {
                                            Printitem += "T 5 0 240 " + x + " "
                                                    + 0 + "\r\n";

                                            Printitem += "T 5 0 290 "
                                                    + x
                                                    + " "
                                                    + schemeProductBO
                                                    .getQuantitySelected()
                                                    + "\r\n";
                                        }
                                        x += 10;
                                    }
                                }
                            }
                        }
                    }
                }
                x += 30;
                Printitem += "T 5 0 10 "
                        + x
                        + " --------------------------------------------------\r\n";

                x += 30;
                Printitem += "T 5 0 10 " + x
                        + mContext.getResources().getString(R.string.totfullstockcs)
                        + "\r\n";

                Printitem += "RIGHT \r\n";
                Printitem += "T 5 0 450 " + x + " "
                        + bmodel.formatValue(mCaseTotalValue) + "\r\n";

                x += 20;
                Printitem += "T 5 0 10 "
                        + x
                        + " --------------------------------------------------\r\n";

                x += 30;
                Printitem += "T 5 0 10 " + x
                        + mContext.getResources().getString(R.string.totfullstockbtl)
                        + "\r\n";

                Printitem += "T 5 0 450 " + x + " "
                        + bmodel.formatValue(mPcTotalValue) + "\r\n";

                x += 30;
                Printitem += "T 5 0 10 "
                        + x
                        + " --------------------------------------------------\r\n";

                if (bmodel.configurationMasterHelper.TAX_SHOW_INVOICE) {

                    x += 30;
                    Printitem += "T 5 0 10 " + x
                            + mContext.getResources().getString(R.string.total_exec_tax)
                            + "\r\n";

                    Printitem += "RIGHT \r\n";
                    Printitem += "T 5 0 450 " + x + " "
                            + bmodel.formatValue(mtotalExcludeTaxAmount)
                            + "\r\n";
                    x += 20;
                    Printitem += "T 5 0 10 "
                            + x
                            + " --------------------------------------------------\r\n";
                    x += 30;
                    Printitem += "T 5 0 10 " + x + mVatName + "("
                            + vatPercentage + "%)" + "\r\n";

                    Printitem += "T 5 0 450 " + x + " "
                            + bmodel.formatValue(mVatValue) + "\r\n";

                    x += 20;
                    Printitem += "T 5 0 10 "
                            + x
                            + " --------------------------------------------------\r\n";
                    if ("".equals(mNhilName)) {
                        x += 30;
                        Printitem += "T 5 0 10 " + x + mNhilName + "("
                                + nhlPercentage + "%)" + "\r\n";

                        Printitem += "T 5 0 450 " + x + " "
                                + bmodel.formatValue(mNhlValue) + "\r\n";

                        x += 20;
                        Printitem += "T 5 0 10 "
                                + x
                                + " --------------------------------------------------\r\n";
                    }

                    x += 50;
                    Printitem += "T 5 0 10 " + x + " Total liquid inc Tax" + "\r\n";
                    Printitem += "T 5 0 450 " + x + " "
                            + bmodel.formatValue(mTotalOrderValue) + "\r\n";
                    x += 20;
                    Printitem += "T 5 0 10 "
                            + x
                            + " --------------------------------------------------\r\n";
                }

                x += 40;
                Printitem += "T 5 0 10 " + x + " Empties" + "\r\n";
                x += 20;
                Printitem += "T 5 0 10 "
                        + x
                        + " --------------------------------------------------\r\n";

                for (BomReturnBO productBO : mEmptyLiaProductsForAdapter) {
                    if ((productBO.getLiableQty() > 0)) {
                        x += 20;
                        String productname;
                        // For Printer Space issue , restriced to 10 character.
                        if (productBO.getProductShortName() != null && !"".equals(productBO.getProductShortName())
                                && !"null".equals(productBO.getProductShortName())) {
                            if (productBO.getProductShortName().length() > 18)
                                productname = productBO.getProductShortName().substring(
                                        0, 18);
                            else
                                productname = productBO.getProductShortName();
                        } else {
                            if (productBO.getProductName().length() > 18)
                                productname = productBO.getProductName().substring(
                                        0, 18);
                            else
                                productname = productBO.getProductName();
                        }

                        Printitem += "T 5 0 10 " + x + " " + productname.toLowerCase()
                                + "\r\n";
                        Printitem += "\r\n";

                        Printitem += "T 5 0 240 " + x + " " + "0" + "\r\n";

                        Printitem += "T 5 0 290 " + x + " "
                                + productBO.getLiableQty() + "\r\n";

                        Printitem += "T 5 0 360 " + x + " "
                                + bmodel.formatValue(productBO.getpSrp())
                                + "\r\n";

                        double totalEmpVal = (productBO.getLiableQty() * productBO
                                .getpSrp());

                        Printitem += "T 5 0 450 " + x + " "
                                + bmodel.formatValue(totalEmpVal) + "\r\n";
                        x += 10;
                    }
                }

                x += 40;
                Printitem += "T 5 0 10 "
                        + x
                        + " --------------------------------------------------\r\n";

                x += 30;
                Printitem += "T 5 0 10 " + x
                        + mContext.getResources().getString(R.string.totempties)
                        + "\r\n";

                Printitem += "T 5 0 450 " + x + " "
                        + bmodel.formatValue(mEmpTotalValue) + "\r\n";

                x += 20;
                Printitem += "T 5 0 10 "
                        + x
                        + " --------------------------------------------------\r\n";
                x += 30;
                Printitem += "T 5 0 190 " + x
                        + " Total Amount "
                        + "\r\n";

                Printitem += "T 5 0 450 " + x + " "
                        + bmodel.formatValue(mTotalColValue) + "\r\n";

                x += 60;
                Printitem += "T 5 0 170 " + x
                        + " Expected Delivery Date "
                        + "\r\n";
                Printitem += "T 5 0 450 " + x + " "
                        + deleveryDate + "\r\n";

                x += 60;
                Printitem += "T 5 0 10 " + x
                        + " Customer Signature "
                        + "\r\n";

                if (bmodel.userMasterHelper.getUserMasterBO().getUserName().length() > 15) {
                    Printitem += "T 5 0 350 " + x + " "
                            + bmodel.userMasterHelper.getUserMasterBO().getUserName().substring(0, 15) + "\r\n";
                    Printitem += "T 5 0 350 " + (x + 30) + " "
                            + bmodel.userMasterHelper.getUserMasterBO().getUserName().substring(15) + "\r\n";
                } else {
                    Printitem += "T 5 0 350 " + x + " "
                            + bmodel.userMasterHelper.getUserMasterBO().getUserName() + "\r\n";
                }

                Printitem += "PRINT\r\n";

                PrintDataBytes = Printitem.getBytes();
            }
        } catch (Exception e) {
            Commons.printException(e + "");
        }
        return PrintDataBytes;
    }

    private String ExtractGraphicsDataForCPCL() {
        String m_data;
        int color;
        int bit;
        int currentValue;
        int redValue;
        int blueValue;
        int greenValue;
        int _ypos = 0;

        try {
            // Make sure the width is divisible by 8
            int loopWidth = 8 - (m_bmp.getWidth() % 8);
            if (loopWidth == 8)
                loopWidth = m_bmp.getWidth();
            else
                loopWidth += m_bmp.getWidth();

            m_data = "EG" + " " + Integer.toString((loopWidth / 8)) + " "
                    + Integer.toString(m_bmp.getHeight()) + " "
                    + Integer.toString(0) + " " + Integer.toString(_ypos)
                    + " ";

            for (int y = 0; y < m_bmp.getHeight(); y++) {
                bit = 128;
                currentValue = 0;
                for (int x = 0; x < loopWidth; x++) {
                    int intensity;

                    if (x < m_bmp.getWidth()) {
                        color = m_bmp.getPixel(x, y);

                        redValue = Color.red(color);
                        blueValue = Color.blue(color);
                        greenValue = Color.green(color);

                        intensity = 255 - ((redValue + greenValue + blueValue) / 3);
                    } else
                        intensity = 0;

                    if (intensity >= 128)
                        currentValue |= bit;
                    bit = bit >> 1;
                    if (bit == 0) {
                        String hex = Integer.toHexString(currentValue);
                        hex = LeftPad(hex);
                        m_data = m_data + hex.toUpperCase();

                        bit = 128;
                        currentValue = 0;
                    }
                }// x
            }// y
            m_data = m_data + "\r\n";

        } catch (Exception e) {
            m_data = "";
            return m_data;
        }

        return m_data;
    }

    private String LeftPad(String _num) {

        String str = _num;

        if (_num.length() == 1) {
            str = "0" + _num;
        }

        return str;
    }

    public String[] getPrintCountArray() {
        int printCount = bmodel.configurationMasterHelper.printCount;
        String[] printCountArray = new String[printCount];
        for (int i = 0; i < printCount; i++) {
            printCountArray[i] = i + 1 + "";
        }
        return printCountArray;
    }

    private void calculateTaxDetails() {
        try {
            mtotalExcludeTaxAmount = mTotalOrderValue
                    / (1 + (vatPercentage / 100) + (nhlPercentage / 100));
            mVatValue = (mtotalExcludeTaxAmount * vatPercentage) / 100;
            mNhlValue = (mtotalExcludeTaxAmount * nhlPercentage) / 100;
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public String printDataforBixolon3inchCollectionprinter(boolean isFirstCollection, String groupid, boolean isOriginalCopy, boolean isFooter) {

        StringBuffer sb = new StringBuffer();

        ArrayList<PaymentBO> paymentList = collectionHelper.getPaymentData(groupid);
        ArrayList<String> invoiceNoList = collectionHelper.getBillNumber(groupid);
        if (paymentList != null && paymentList.size() > 0 && invoiceNoList != null && invoiceNoList.size() > 0) {
            try {

                double totalAmount = 0;
                for (PaymentBO paymentBO : paymentList) {
                    totalAmount = totalAmount + paymentBO.getAmount();
                }
                if (isFirstCollection) {

                    if (bmodel.userMasterHelper.getUserMasterBO().getDistributorName() != null) {
                        if (bmodel.userMasterHelper.getUserMasterBO().getDistributorName().length() > 20) {
                            sb.append(doPrintAddSpace(0, 16));
                            sb.append(bmodel.userMasterHelper.getUserMasterBO().getDistributorName().substring(0, 20).toLowerCase());
                            sb.append(doPrintFormatingLeft(" " + bmodel.userMasterHelper.getUserMasterBO().getDistributorName().substring(0, 20).toLowerCase(), 23));
                            sb.append(" ");
                        } else {
                            sb.append(doPrintAddSpace(0, 16));
                            sb.append(bmodel.userMasterHelper.getUserMasterBO().getDistributorName().toLowerCase());
                            sb.append(" ");
                        }
                    } else {

                        sb.append(" ");
                    }

                    sb.append(LineFeed(1));

                    sb.append(doPrintAddSpace(0, 16));
                    sb.append(bmodel.userMasterHelper.getUserMasterBO().getDistributorTinNumber());
                    sb.append(LineFeed(1));
                    sb.append(doPrintAddSpace(0, 16));
                    sb.append(bmodel.userMasterHelper.getUserMasterBO().getDistributorAddress1());
                    sb.append(LineFeed(1));
                    sb.append(doPrintAddSpace(0, 16));
                    sb.append(bmodel.userMasterHelper.getUserMasterBO().getDistributorAddress2());
                    sb.append(LineFeed(1));
                    sb.append(doPrintAddSpace(0, 16));
                    if (isOriginalCopy) {
                        sb.append("Original Print");
                    } else {
                        sb.append("Duplicate Print");
                    }
                    sb.append(LineFeed(1));
                }

                sb.append(" ");
                for (int i = 0; i < 47; i++) {
                    sb.append("-");
                }
                sb.append(LineFeed(1));

                PaymentBO payHeaderBO = paymentList.get(0);

                sb.append(doPrintFormatingLeft("Rept Date:" + payHeaderBO.getCollectionDateTime(), 30));
                sb.append(LineFeed(1));

                sb.append(doPrintFormatingLeft("Rept NO:" + collectionHelper.collectionGroupId, 30));
                sb.append(LineFeed(1));
                sb.append(doPrintFormatingLeft("AgentCode:" + bmodel.userMasterHelper.getUserMasterBO().getUserCode(), 25));
                sb.append(doPrintFormatingRight("AgentName:" + bmodel.userMasterHelper.getUserMasterBO().getUserName(), 26));
                sb.append(LineFeed(1));
                sb.append(doPrintFormatingLeft("CustCode:" + payHeaderBO.getRetailerCode(), 25));
                sb.append(doPrintFormatingRight("CustName:" + payHeaderBO.getRetailerName(), 26));
                sb.append(LineFeed(1));

                sb.append(" ");
                for (int i = 0; i < 47; i++) {
                    sb.append("-");
                }
                sb.append(LineFeed(1));

                sb.append("Inv No");
                sb.append(LineFeed(1));
                sb.append(doPrintAddSpace(0, 6));
                sb.append(doPrintFormatingLeft("Type ", 8));
                sb.append(doPrintFormatingLeft("Date ", 12));
                sb.append(doPrintFormatingLeft("ChqNum ", 15));
                sb.append(doPrintFormatingLeft("Total ", 10));
                sb.append(LineFeed(1));
                sb.append(" ");
                for (int i = 0; i < 47; i++) {
                    sb.append("-");
                }
                sb.append(LineFeed(1));
                double totalDiscountAmount = 0;
                double totalPaidAmount = 0;
                for (PaymentBO paymentBO : paymentList) {
                    sb.append(doPrintFormatingLeft(paymentBO.getBillNumber(), 15));
                    sb.append(LineFeed(1));
                    String modeNo = "";
                    CollectionFragmentNew.CaseMODE mode = CollectionFragmentNew.CaseMODE.valueOf(paymentBO.getCashMode());
                    switch (mode) {
                        case CN:
                            if (paymentBO.getReferenceNumber().startsWith("AP")) {
                                modeNo = mContext.getResources().getString(R.string.advance_payment);
                            } else {
                                modeNo = mContext.getResources().getString(R.string.credit_note);
                            }
                            break;
                        case CA:
                            modeNo = mContext.getResources().getString(R.string.cash);
                            break;
                        case CQ:
                            modeNo = mContext.getResources().getString(R.string.cheque);
                            break;
                        case RTGS:
                            modeNo = mContext.getResources().getString(R.string.rtgs);
                            break;
                        case DD:
                            modeNo = "DD";
                            break;
                        case CD:
                            modeNo = "Coupon";
                            break;
                        case CM:
                            modeNo = "Mob.Pay";
                            break;
                    }
                    sb.append(doPrintAddSpace(0, 6));
                    sb.append(doPrintFormatingLeft(modeNo, 8));
                    sb.append(doPrintFormatingLeft(paymentBO.getChequeDate(), 12));
                    sb.append(doPrintFormatingLeft(paymentBO.getChequeNumber(), 15));
                    sb.append(doPrintFormatingLeft(paymentBO.getAmount() + "", 10));
                    //  totalAmount=totalAmount+paymentBO.getAmount();
                    totalDiscountAmount = totalDiscountAmount + paymentBO.getAppliedDiscountAmount();
                    sb.append(LineFeed(1));
                }


                sb.append(" ");
                for (int i = 0; i < 47; i++) {
                    sb.append("-");
                }
                sb.append(LineFeed(1));

                sb.append(doPrintFormatingLeft("Discount " + totalDiscountAmount, 32));
                sb.append(doPrintFormatingRight("Total " + totalAmount, 15));
                sb.append(LineFeed(1));
                sb.append(" ");
                for (int i = 0; i < 47; i++) {
                    sb.append("-");
                }
                sb.append(LineFeed(2));

                if (isFooter) {
                    sb.append(doPrintFormatingLeft("Comments :", 10));
                    for (int i = 0; i < 37; i++) {
                        sb.append("-");
                    }
                    sb.append(LineFeed(2));
                    sb.append(doPrintFormatingLeft("Signature :", 10));
                    for (int i = 0; i < 37; i++) {
                        sb.append("-");
                    }
                    sb.append(LineFeed(5));
                }
                return sb.toString();


            } catch (Exception e) {
                Commons.printException("" + e);
            }
        }
        return sb.toString();
    }

    private String doPrintFormatingLeft(String str, int maxlength) {
        StringBuilder sb = new StringBuilder();
        if (str.length() >= maxlength) {
            sb.append(str.substring(0, maxlength));
        } else {
            sb.append(str);
            for (int i = 1; i < (maxlength - str.length()); i++)
                sb.append(" ");
        }
        return sb.toString();
    }

    private String doPrintFormatingRight(String str, int maxlength) {
        StringBuilder sb = new StringBuilder();
        if (str.length() > maxlength) {
            sb.append(str.substring(0, maxlength));
        } else {
            sb.append(str);
        }
        return sb.toString();
    }

    private String doPrintAddSpace(int space, int maxlenght) {
        StringBuffer sb = new StringBuffer();
        if (space < maxlenght) {
            for (int i = 0; i < maxlenght - space; i++) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    private String LineFeed(int line) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < line; i++) {
            sb.append("\n");
        }
        return sb.toString();
    }

    public byte[] printThai(boolean isOriginal, ArrayList<PaymentBO> paymentList) {
        byte[] PrintDataBytes = null;
        try {
            StringBuilder tempsb = new StringBuilder();

            tempsb.append("! U1 SETLP ANG12PT.CPF 0 34 \n");


            tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "CENTER\r\n");
            tempsb.append("SETBOLD 1 \r\n");

            if (bmodel.configurationMasterHelper.SHOW_PRINT_HEADERS) {
                tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 "
                        + "Unipal General Trading Company" + "\r\n");
                tempsb.append("SETBOLD 0 \r\n");
                tempsb.append("PRINT\r\n");

                tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "CENTER\r\n");
                tempsb.append("SETBOLD 1 \r\n");
                tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 " + "VAT No : 562414227" + "\r\n");
                tempsb.append("SETBOLD 0 \r\n");
                tempsb.append("PRINT\r\n");

                tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "CENTER\r\n");
                tempsb.append("SETBOLD 1 \r\n");
                tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 "
                        + "Ramallah - Industrial zone, Tel: +972 2 2981060" + "\r\n");
                tempsb.append("SETBOLD 0 \r\n");
                tempsb.append("PRINT\r\n");

                tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "CENTER\r\n");
                tempsb.append("SETBOLD 1 \r\n");
                tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 "
                        + "Gaza - lndus. Zone - Carny, Tel: +972 7 2830324" + "\r\n");
                tempsb.append("SETBOLD 0 \r\n");
                tempsb.append("PRINT\r\n");
            } else {
                tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 "
                        + bmodel.userMasterHelper.getUserMasterBO()
                        .getDistributorName() + "\r\n");
                tempsb.append("SETBOLD 0 \r\n");
                tempsb.append("PRINT\r\n");

                tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "CENTER\r\n");
                tempsb.append("SETBOLD 1 \r\n");
                tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 "
                        + bmodel.userMasterHelper.getUserMasterBO()
                        .getDistributorTinNumber() + "\r\n");
                tempsb.append("SETBOLD 0 \r\n");
                tempsb.append("PRINT\r\n");

                tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "CENTER\r\n");
                tempsb.append("SETBOLD 1 \r\n");
                tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 "
                        + bmodel.userMasterHelper.getUserMasterBO()
                        .getDistributorAddress1() + "\r\n");
                tempsb.append("SETBOLD 0 \r\n");
                tempsb.append("PRINT\r\n");

                tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "CENTER\r\n");
                tempsb.append("SETBOLD 1 \r\n");
                tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 "
                        + bmodel.userMasterHelper.getUserMasterBO()
                        .getDistributorAddress2() + "\r\n");
                tempsb.append("SETBOLD 0 \r\n");
                tempsb.append("PRINT\r\n");
            }

            tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "CENTER\r\n");
            tempsb.append("SETBOLD 1 \r\n");
            if (isOriginal)
                tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 "
                        + "Original Print" + "\r\n");
            else
                tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 "
                        + "Duplicate print" + "\r\n");

            tempsb.append("SETBOLD 0 \r\n");
            tempsb.append("PRINT\r\n");

            double total;
            double totalDiscount = 0;

            PaymentBO payHeaderBO = paymentList.get(0);
            total = 0;
            tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
            tempsb.append("SETBOLD 1 \r\n");
            tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 " + "---------------------------------------------------------------------------\r\n");
            tempsb.append("SETBOLD 0 \r\n");
            tempsb.append("PRINT\r\n");

            tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
            tempsb.append("SETBOLD 1 \r\n");
            if (payHeaderBO.getAdvancePaymentId() != null) {
                tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 "
                        + "Rcpt Date:"
                        + ""
                        + payHeaderBO.getAdvancePaymentDate() + "\r\n");
            } else {
                tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 "
                        + "Rcpt Date:"
                        + ""
                        + payHeaderBO.getCollectionDateTime() + "\r\n");
            }
            tempsb.append("SETBOLD 0 \r\n");
            tempsb.append("PRINT\r\n");

            tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
            tempsb.append("SETBOLD 1 \r\n");
            tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 "
                    + "Rcpt NO"
                    + ":"
                    + payHeaderBO.getGroupId()
                    + "\r\n");
            tempsb.append("SETBOLD 0 \r\n");
            tempsb.append("PRINT\r\n");

            tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
            tempsb.append("SETBOLD 1 \r\n");
            tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 "
                    + "AgentCode:"
                    + ""
                    + bmodel.userMasterHelper.getUserMasterBO()
                    .getUserCode() + "\r\n");
            tempsb.append("SETBOLD 0 \r\n");
            tempsb.append("PRINT");

            tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
            tempsb.append("SETBOLD 1 \r\n");
            tempsb.append("TEXT ANG12PT.CPF 0 " + 250 + " 1 "
                    + "AgentName"
                    + ":"
                    + bmodel.userMasterHelper.getUserMasterBO().getUserName()
                    + "\r\n");
            tempsb.append("SETBOLD 0 \r\n");
            tempsb.append("PRINT\r\n");

            String retailername = "";
            if (payHeaderBO.getRetailerName().length() > 30) {
                retailername = payHeaderBO.getRetailerName().substring(0, 30);
            } else {
                retailername = payHeaderBO.getRetailerName();
            }

            tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
            tempsb.append("SETBOLD 1 \r\n");
            tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 "
                    + "CustName"
                    + ":"
                    + retailername
                    + "\r\n");
            tempsb.append("SETBOLD 0 \r\n");
            tempsb.append("PRINT\r\n");

            tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
            tempsb.append("SETBOLD 1 \r\n");
            tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 "
                    + "CustCode:"
                    + ""
                    + payHeaderBO.getRetailerCode()
                    + "\r\n");
            tempsb.append("SETBOLD 0 \r\n");
            tempsb.append("PRINT\r\n");

            tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
            tempsb.append("SETBOLD 1 \r\n");
            tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 " + "---------------------------------------------------------------------------\r\n");
            tempsb.append("SETBOLD 0 \r\n");
            tempsb.append("PRINT\r\n");


            tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
            tempsb.append("SETBOLD 1 \r\n");
            tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 " + "Inv No" + "\r\n");
            tempsb.append("SETBOLD 0 \r\n");
            tempsb.append("PRINT");

            tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
            tempsb.append("SETBOLD 1 \r\n");
            tempsb.append("TEXT ANG12PT.CPF 0 " + 160 + " 1 " + "Type" + "\r\n");
            tempsb.append("SETBOLD 0 \r\n");
            tempsb.append("PRINT");

            tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
            tempsb.append("SETBOLD 1 \r\n");
            tempsb.append("TEXT ANG12PT.CPF 0 " + 240 + " 1 " + "Date" + "\r\n");
            tempsb.append("SETBOLD 0 \r\n");
            tempsb.append("PRINT");

            tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
            tempsb.append("SETBOLD 1 \r\n");
            tempsb.append("TEXT ANG12PT.CPF 0 " + 320 + " 1 " + "Chq Num" + "\r\n");
            tempsb.append("SETBOLD 0 \r\n");
            tempsb.append("PRINT");

            tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
            tempsb.append("SETBOLD 1 \r\n");
            tempsb.append("TEXT ANG12PT.CPF 0 " + 440 + " 1 " + "Total" + "\r\n");
            tempsb.append("SETBOLD 0 \r\n");
            tempsb.append("PRINT\r\n");

            tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
            tempsb.append("SETBOLD 1 \r\n");
            tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 " + " ---------------------------------------------------------------------------\r\n");
            tempsb.append("SETBOLD 0 \r\n");
            tempsb.append("PRINT\r\n");
            for (PaymentBO payBO : paymentList) {
                tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                tempsb.append("SETBOLD 1 \r\n");
                tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 "
                        + (payBO.getBillNumber() != null ? payBO.getBillNumber() : mContext.getResources().getString(R.string.advance_payment))
                        + "\r\n");
                tempsb.append("SETBOLD 0 \r\n");
                tempsb.append("PRINT\r\n");

                tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                tempsb.append("SETBOLD 1 \r\n");
                if (payBO.getCashMode().equals(StandardListMasterConstants.CREDIT_NOTE)) {
                    if (payBO.getReferenceNumber().startsWith("AP")) {
                        tempsb.append("TEXT ANG12PT.CPF 0 " + 160 + " 1 "
                                + mContext.getResources().getString(R.string.advance_payment) + "\r\n");
                    } else {
                        tempsb.append("TEXT ANG12PT.CPF 0 " + 160 + " 1 "
                                + mContext.getResources().getString(R.string.credit_note) + "\r\n");
                    }
                } else {
                    if (payBO.getCashMode().equals(StandardListMasterConstants.CASH)) {
                        tempsb.append("TEXT ANG12PT.CPF 0 " + 160 + " 1 "
                                + mContext.getResources().getString(R.string.cash) + "\r\n");
                    } else if (payBO.getCashMode().equals(StandardListMasterConstants.CHEQUE)) {
                        tempsb.append("TEXT ANG12PT.CPF 0 " + 160 + " 1 "
                                + mContext.getResources().getString(R.string.cheque) + "\r\n");
                    } else if (payBO.getCashMode().equals(StandardListMasterConstants.DEMAND_DRAFT)) {
                        tempsb.append("TEXT ANG12PT.CPF 0 " + 160 + " 1 "
                                + "DD" + "\r\n");
                    } else if (payBO.getCashMode().equals(StandardListMasterConstants.RTGS)) {
                        tempsb.append("TEXT ANG12PT.CPF 0 " + 160 + " 1 "
                                + mContext.getResources().getString(R.string.rtgs) + "\r\n");
                    } else if (payBO.getCashMode().equals(StandardListMasterConstants.MOBILE_PAYMENT)) {
                        tempsb.append("TEXT ANG12PT.CPF 0 " + 160 + " 1 "
                                + "Mob.Pay" + "\r\n");
                    }
                }
                tempsb.append("SETBOLD 0 \r\n");
                tempsb.append("PRINT");


                tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                tempsb.append("SETBOLD 1 \r\n");
                tempsb.append("TEXT ANG12PT.CPF 0 " + 240 + " 1 "
                        + payBO.getChequeDate()
                        + "\r\n");
                tempsb.append("SETBOLD 0 \r\n");
                tempsb.append("PRINT");

                tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                tempsb.append("SETBOLD 1 \r\n");
                if (!payBO.getCashMode().equals(StandardListMasterConstants.CREDIT_NOTE) && !payBO.getCashMode().equals(StandardListMasterConstants.ADVANCE_PAYMENT))
                    tempsb.append("TEXT ANG12PT.CPF 0 " + 320 + " 1 "
                            + payBO.getChequeNumber() + "\r\n");
                else {
                    tempsb.append("TEXT ANG12PT.CPF 0 " + 320 + " 1 "
                            + payBO.getReferenceNumber() + "\r\n");
                }
                tempsb.append("SETBOLD 0 \r\n");
                tempsb.append("PRINT");


                tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                tempsb.append("SETBOLD 1 \r\n");
                tempsb.append("TEXT ANG12PT.CPF 0 " + 440 + " 1 "
                        + bmodel.formatValue(payBO.getAmount())
                        + "\r\n");
                tempsb.append("SETBOLD 0 \r\n");
                tempsb.append("PRINT\r\n");

                total += payBO.getAmount();
                totalDiscount += payBO.getAppliedDiscountAmount();

            }

            tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
            tempsb.append("SETBOLD 1 \r\n");
            tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 " + "---------------------------------------------------------------------------\r\n");
            tempsb.append("SETBOLD 0 \r\n");
            tempsb.append("PRINT\r\n");

            tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
            tempsb.append("SETBOLD 1 \r\n");
            tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 " + "Discount " + "\r\n");
            tempsb.append("SETBOLD 0 \r\n");
            tempsb.append("PRINT");

            tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
            tempsb.append("SETBOLD 1 \r\n");
            tempsb.append("TEXT ANG12PT.CPF 0 " + 170 + " 1 "
                    + bmodel.formatValue(totalDiscount) + "\r\n");
            tempsb.append("SETBOLD 0 \r\n");
            tempsb.append("PRINT");

            tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
            tempsb.append("SETBOLD 1 \r\n");
            tempsb.append("TEXT ANG12PT.CPF 0 " + 360 + " 1 " + "Total " + "\r\n");
            tempsb.append("SETBOLD 0 \r\n");
            tempsb.append("PRINT");

            tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
            tempsb.append("SETBOLD 1 \r\n");
            tempsb.append("TEXT ANG12PT.CPF 0 " + 440 + " 1 "
                    + bmodel.formatValue(total) + "\r\n");
            tempsb.append("SETBOLD 0 \r\n");
            tempsb.append("PRINT\r\n");

            tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
            tempsb.append("SETBOLD 1 \r\n");
            tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 " + "---------------------------------------------------------------------------\r\n");
            tempsb.append("SETBOLD 0 \r\n");
            tempsb.append("PRINT\r\n");


            tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
            tempsb.append("SETBOLD 1 \r\n");
            tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 " + "Comments:------------------------------------------------------------\r\n");
            tempsb.append("SETBOLD 0 \r\n");
            tempsb.append("PRINT\r\n");

            tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
            tempsb.append("SETBOLD 1 \r\n");
            tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 " + "Signature:--------------------------------------------------------------\r\n");
            tempsb.append("SETBOLD 0 \r\n");
            tempsb.append("PRINT\r\n");
            tempsb.append("\r\n");

            PrintDataBytes = String.valueOf(tempsb).getBytes("ISO-8859-11");

        } catch (Exception e) {
            Commons.printException(e);
        }
        return PrintDataBytes;
    }



    /**
     * When save invoice print file created and stored in mobile
     * this method will be deleted print file while downloading
     */
    public void deletePrintFileAfterDownload(String path) {
        try {
            File folder = new File(path);

            File sfFiles[] = folder.listFiles();
            if (sfFiles != null) {
                for (int i = 0; i < sfFiles.length; i++) {
                    File deleteFile = new File(folder, "/" + sfFiles[i].getName());
                    deleteFile.delete();

                }
                folder.delete();
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }
}
